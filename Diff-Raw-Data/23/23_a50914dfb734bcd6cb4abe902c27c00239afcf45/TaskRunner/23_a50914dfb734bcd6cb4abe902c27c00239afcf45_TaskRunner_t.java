 package edu.illinois.cs.mr.te;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import java.util.concurrent.Semaphore;
 
 import edu.illinois.cs.mapreduce.api.Context;
 import edu.illinois.cs.mapreduce.api.InputFormat;
 import edu.illinois.cs.mapreduce.api.Mapper;
 import edu.illinois.cs.mapreduce.api.OutputFormat;
 import edu.illinois.cs.mapreduce.api.RecordReader;
 import edu.illinois.cs.mapreduce.api.RecordWriter;
 import edu.illinois.cs.mapreduce.api.Reducer;
import edu.illinois.cs.mapreduce.api.Split;
 import edu.illinois.cs.mr.Node;
 import edu.illinois.cs.mr.fs.FileSystem;
 import edu.illinois.cs.mr.fs.FileSystemService;
 import edu.illinois.cs.mr.fs.Path;
 import edu.illinois.cs.mr.fs.QualifiedPath;
 import edu.illinois.cs.mr.jm.JobDescriptor;
 import edu.illinois.cs.mr.util.ReflectionUtil;
 import edu.illinois.cs.mr.util.Status.State;
 
 /**
  * Runs map and reduce tasks
  * 
  * @author benjamin
  */
 class TaskRunner implements Runnable {
 
    private static final int OBJECT_CHUNKS = 1000;

     private final TaskExecutorTask task;
     private final Node node;
     private final Semaphore completion;
     private final TaskExecutor parent;
 
     private FileSystem fileSystem;
     private ClassLoader classLoader;
     private JobDescriptor descriptor;
 
     public TaskRunner(TaskExecutor parent, TaskExecutorTask task, Semaphore completion, Node node) {
         this.parent = parent;
         this.task = task;
         this.completion = completion;
         this.node = node;
     }
 
     private void init() throws IOException {
         this.fileSystem = node.getFileSystem();
         Path jarPath = task.getJarPath();
         URL jarURL = fileSystem.toURL(jarPath);
         this.classLoader = ReflectionUtil.createClassLoader(jarURL);
         this.descriptor = task.getDescriptor();
     }
 
     @Override
     public void run() {
         try {
             task.setState(State.RUNNING);
             init();
             if (task.isMap())
                 runMap((TaskExecutorMapTask)task);
             else
                 runReduce((TaskExecutorReduceTask)task);
             task.setState(State.SUCCEEDED);
         } catch (InterruptedException e) {
             task.setState(State.CANCELED);
         } catch (Throwable t) {
             task.setFailed(t.getMessage());
             t.printStackTrace();
             if (t instanceof Error)
                 throw (Error)t;
         } finally {
             completion.release();
             try {
                 Thread.sleep(this.parent.done(task));
             } catch (InterruptedException e) {
                 // empty
             }
         }
     }
 
     private <K1, V1, K2, V2> void runMap(TaskExecutorMapTask mapTask) throws Exception {
         // create object instances
         Mapper<K1, V1, K2, V2> mapper = newInstance(descriptor.getMapperClass());
         InputFormat<K1, V1, ? super Split> inputFormat = newInstance(descriptor.getInputFormatClass());
         Split split = mapTask.getSplit();
         Reducer<K2, V2, K2, V2> combiner = null;
         String combinerClass = descriptor.getCombinerClass();
         if (combinerClass != null)
             combiner = newInstance(combinerClass);
         // run map
         InputStream is = fileSystem.read(mapTask.getInputPath());
         try {
             RecordReader<K1, V1> reader = inputFormat.createRecordReader(split, is);
             try {
                 OutputStream os = fileSystem.write(mapTask.getOutputPath());
                 try {
                     MapOutputContext<K2, V2> context = new MapOutputContext<K2, V2>(combiner, os);
                     try {
                         while (reader.next()) {
                             if (Thread.interrupted())
                                 throw new InterruptedException();
                             K1 key = reader.getKey();
                             V1 value = reader.getValue();
                             mapper.map(key, value, context);
                         }
                     } finally {
                         context.close();
                     }
                 } finally {
                     os.close();
                 }
             } finally {
                 reader.close();
             }
         } finally {
             is.close();
         }
     }
 
     private <T> T newInstance(String className) throws IOException {
         return ReflectionUtil.newInstance(className, classLoader);
     }
 
     // TODO avoid keeping whole set in memory: limit buffer and spill to
     // separate files once full. Merge spills at the end.
     static class MapOutputContext<K, V> implements Context<K, V>, Closeable {
 
         private final Reducer<K, V, K, V> combiner;
         private final OutputStream os;
         private final Map<K, List<V>> map;
 
         public MapOutputContext(Reducer<K, V, K, V> combiner, OutputStream os) {
             this.combiner = combiner;
             this.os = os;
             this.map = new TreeMap<K, List<V>>();
         }
 
         @Override
         public void write(K key, V value) throws IOException {
             List<V> values = map.get(key);
             if (values == null)
                 map.put(key, values = new LinkedList<V>());
             values.add(value);
         }
 
         @Override
         public void close() throws IOException {
             if (map.isEmpty())
                 return;
             if (combiner != null) {
                 MapOutputContext<K, V> subContext = new MapOutputContext<K, V>(null, os);
                 try {
                     for (Entry<K, List<V>> entry : map.entrySet())
                         combiner.reduce(entry.getKey(), entry.getValue(), subContext);
                 } finally {
                     subContext.close();
                 }
             } else {
                 ObjectOutputStream oos = new ObjectOutputStream(os);
                 try {
                    int chunk = OBJECT_CHUNKS;
                     for (Entry<K, List<V>> entry : map.entrySet()) {
                         oos.writeObject(entry.getKey());
                         oos.writeObject(entry.getValue());
                        if (chunk-- == 0) {
                            oos.reset();
                            chunk = OBJECT_CHUNKS;
                        }
                     }
                     oos.writeObject(null);// sentinel
                 } finally {
                     oos.close();
                 }
             }
         }
     }
 
     private <K1, V1, K2, V2> void runReduce(TaskExecutorReduceTask reduceTask) throws Exception {
         // 1. merge sorted files into one big sorted file
         Path outputPath = reduceTask.getOutputPath();
         Path mergedPath = outputPath.beforeLast().append(outputPath.last() + "-merged");
         List<QualifiedPath> inputPaths = reduceTask.getInputPaths();
         shuffle(inputPaths, mergedPath);
 
         // 3. create object instances
         Reducer<K1, V1, K2, V2> reducer = newInstance(descriptor.getReducerClass());
         OutputFormat<K2, V2> outputFormat = newInstance(descriptor.getOutputFormatClass());
 
         // 2. run actual reducer
         try {
             InputStream is = fileSystem.read(mergedPath);
             try {
                 ObjectInputStream ois = new ObjectInputStream(is);
                 try {
                     OutputStream os = fileSystem.write(outputPath);
                     try {
                         RecordWriter<K2, V2> writer = outputFormat.createRecordWriter(os, descriptor.getProperties());
                         try {
                             ReducerOutputContext<K2, V2> context = new ReducerOutputContext<K2, V2>(writer);
                             K1 key = null;
                             List<V1> values = new ArrayList<V1>();
                             while (true) {
                                 @SuppressWarnings("unchecked")
                                 K1 curKey = (K1)ois.readObject();
                                 if (curKey == null)
                                     break;
                                 @SuppressWarnings("unchecked")
                                 List<V1> curValues = (List<V1>)ois.readObject();
                                 if (key == null) {
                                     key = curKey;
                                     values.addAll(curValues);
                                 } else if (curKey.equals(key)) {
                                     values.addAll(curValues);
                                 } else {
                                     reducer.reduce(key, values, context);
                                     key = curKey;
                                     values = new ArrayList<V1>(curValues);
                                 }
                             }
                             reducer.reduce(key, values, context);
                         } finally {
                             writer.close();
                         }
                     } finally {
                         os.close();
                     }
                 } finally {
                     ois.close();
                 }
             } finally {
                 is.close();
             }
         } finally {
             fileSystem.delete(mergedPath);
         }
     }
 
     private Path shuffle(Iterable<QualifiedPath> inputPaths, Path mergedPath) throws IOException,
         ClassNotFoundException {
         List<InputStream> iss = new ArrayList<InputStream>();
         try {
             for (QualifiedPath qPath : inputPaths) {
                 FileSystemService fs = node.getFileSystemService(qPath.getNodeId());
                 InputStream is = fs.read(qPath.getPath());
                 iss.add(is);
             }
             OutputStream os = fileSystem.write(mergedPath);
             try {
                 merge(iss, os);
             } finally {
                 os.close();
             }
         } finally {
             for (InputStream is : iss)
                 is.close();
         }
         return mergedPath;
     }
 
     private static void merge(List<InputStream> inputStreams, OutputStream outputStream) throws IOException,
         ClassNotFoundException {
         List<ObjectInputStream> iss = new ArrayList<ObjectInputStream>(inputStreams.size());
         for (InputStream is : inputStreams)
             iss.add(new ObjectInputStream(is));
         ObjectOutputStream os = new ObjectOutputStream(outputStream);
         try {
             List<Object[]> pairs = new ArrayList<Object[]>(iss.size());
             for (int i = 0; i < iss.size(); i++)
                 pairs.add(null);
             Object[] lowest;
             int idx;
            int chunk = OBJECT_CHUNKS;
             while (!iss.isEmpty()) {
                 lowest = null;
                 idx = -1;
                 for (int i = 0; i < iss.size(); i++) {
                     Object[] pair = pairs.get(i);
                     if (pair == null) {
                         ObjectInputStream ois = iss.get(i);
                         pair = new Object[2];
                         pair[0] = ois.readObject();
                         if (pair[0] == null) {
                            ois.close();
                             iss.remove(i);
                             pairs.remove(i);
                             continue;
                         }
                         pair[1] = ois.readObject();
                         pairs.set(i, pair);
                     }
                     @SuppressWarnings("unchecked")
                     Comparable<Object> key = (Comparable<Object>)pair[0];
                     if (lowest == null || key.compareTo(lowest[0]) <= 0) {
                         lowest = pair;
                         idx = i;
                     }
                 }
                 if (lowest != null) {
                     os.writeObject(lowest[0]);
                     os.writeObject(lowest[1]);
                     pairs.set(idx, null);
                 }
                if (chunk-- == 0) {
                    os.flush();
                    os.reset();
                    chunk = OBJECT_CHUNKS;
                }
             }
             os.writeObject(null);
         } finally {
             os.flush();
         }
     }
 
     static class ReducerOutputContext<K, V> implements Context<K, V> {
 
         private final RecordWriter<K, V> writer;
 
         public ReducerOutputContext(RecordWriter<K, V> writer) {
             this.writer = writer;
         }
 
         @Override
         public void write(K key, V value) throws IOException {
             writer.write(key, value);
         }
 
     }
 }
