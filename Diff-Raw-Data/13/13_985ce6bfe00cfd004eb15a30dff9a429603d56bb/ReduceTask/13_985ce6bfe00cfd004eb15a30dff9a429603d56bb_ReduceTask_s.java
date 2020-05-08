 package cmu.ds.mr.mapred;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import cmu.ds.mr.conf.JobConf;
 import cmu.ds.mr.io.FileSplit;
 import cmu.ds.mr.io.LineRecordReader;
 import cmu.ds.mr.io.MapOutputCollector;
 import cmu.ds.mr.io.RedOutputCollector;
 import cmu.ds.mr.util.Util;
 
 
 public class ReduceTask extends Task {
   
   private Map<String, List<Integer>> redInputMap; 
   
   public ReduceTask(TaskID taskid, JobConf taskconf, TaskStatus taskStatus){
     super(taskid, taskconf, taskStatus);
     redInputMap = new TreeMap<String, List<Integer>>();
   }
 
   @Override
   public void startTask(Task task, TaskUmbilicalProtocol taskTrackerProxy) throws IOException,
           ClassNotFoundException, InterruptedException, RuntimeException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
     int taskNum = taskStatus.getTaskNum();
     
     // do sort/merge phase (build reduce input table)
     String mapOutBase = taskConf.get(Util.LOCAL_ROOT_DIR) + File.separator + task.getJobid().toString()+ File.separator;
     doSortMerge(mapOutBase, taskNum);
     
     // get user defined mapper
     Reducer reducer = (Reducer) Util.newInstance(taskConf.getReducerclass());
     
     // get output collector
     String basePath = taskConf.getOutpath() + File.separator;
     //int nred = taskConf.getNumReduceTasks();
     RedOutputCollector output = new RedOutputCollector(basePath, taskNum);
     
     for(Entry<String, List<Integer>> en : redInputMap.entrySet()) {
       reducer.reduce(en.getKey(), en.getValue().iterator(), output);
     } 
     
     output.writeToDisk();
     
     // TODO delete map output
 //    File mapoutFile = new File(mapOutBase);
 //    Util.delete(mapoutFile);
     
     // notify taskTracker
     taskTrackerProxy.done(taskStatus.getTaskId());
   }
   
   // sort/merge phase in reduce side
   public void doSortMerge(String mapOutBase, int taskNum) throws IOException {
     File baseFile = new File(mapOutBase);
     File[] mapFiles = baseFile.listFiles();
     BufferedReader br;
     String line;
     
     for(File f : mapFiles) {
       File[] redFiles = f.listFiles();
       for(File rf : redFiles) {
        if(rf.getName().equals(taskNum)) {
           // read whole file from each map output
           br = new BufferedReader(new FileReader(rf));
           
           try {
             while((line = br.readLine()) != null) {
               String[] strs = line.split("\\t");
               String key = strs[0];
               int val = Integer.parseInt(strs[1]);
               if(redInputMap.containsKey(key))
                 redInputMap.get(key).add(val);
               else {
                 List<Integer> list = new ArrayList<Integer>();
                 list.add(val);
                 redInputMap.put(key, list);
               }
             }
           }
           finally {
             br.close();
           }
         }
       }
     }
   }
 
 }
