 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayDeque;
 import java.util.Calendar;
 import java.util.Deque;
 import java.util.GregorianCalendar;
 import java.util.Random;
 import java.util.TimeZone;
 
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.pig.ExecType;
 import org.apache.pig.impl.PigContext;
 import org.apache.pig.tools.grunt.Grunt;
 import org.apache.pig.tools.parameters.ParameterSubstitutionPreprocessor;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 
 import com.google.protobuf.ByteString;
 import com.inadco.hb.example1.codegen.Example1.CompilerInput;
 import com.inadco.hbl.client.AggregateQuery;
 import com.inadco.hbl.client.AggregateResult;
 import com.inadco.hbl.client.AggregateResultSet;
 import com.inadco.hbl.client.HblAdmin;
 import com.inadco.hbl.client.HblException;
 import com.inadco.hbl.client.HblQueryClient;
 import com.inadco.hbl.compiler.Pig8CubeIncrementalCompilerBean;
 import com.inadco.hbl.util.HblUtil;
 import com.inadco.hbl.util.IOUtil;
 
 /**
  * to run, use hadoop command line
  * 
  * @author dmitriy
  * 
  */
 
 public class Example1 extends Configured implements Tool {
 
     public static void main(String[] args) throws Throwable {
 
         // runjarArgs[0]="target/sample-0.1.0-SNAPSHOT-hadoop-job.jar";
 
         ToolRunner.run(new Example1(), args);
 
     }
 
     // choose ExecType.LOCAL to debug UDFs
     // private static ExecType EXEC_TYPE = ExecType.LOCAL;
     private static ExecType EXEC_TYPE = ExecType.MAPREDUCE;
 
     @Override
     public int run(String[] args) throws Exception {
 
         // script resource
         Resource cubeModelRsrc = new ClassPathResource("example1.yaml");
 
         // deploy cube schema (optionally dropping the existing one)
//        HblAdmin hblAdmin = new HblAdmin(cubeModelRsrc);
 //        hblAdmin.dropCube(getConf());
 //        hblAdmin.deployCube(getConf());
 
 
         // prepare incremental simulated input
         // and select work dir for the compiler job
 
         FileSystem dfs = EXEC_TYPE == ExecType.MAPREDUCE ? FileSystem.get(getConf()) : FileSystem.getLocal(getConf());
         Path workPath = new Path(dfs.getWorkingDirectory(), "hbltemp-" + System.currentTimeMillis());
         Path inputPath = new Path(dfs.getWorkingDirectory(), "sample1-input" + System.currentTimeMillis());
 
         simulateInput(dfs, inputPath);
 
 
         // run compiler for the model
         Pig8CubeIncrementalCompilerBean compiler =
             new Pig8CubeIncrementalCompilerBean(cubeModelRsrc, new ClassPathResource("example1-preambula.pig"), 5);
 
         String script = compiler.preparePigSource(workPath.toString());
 
         // ////////////////////////////////////
         // ------------- debug: dump the script
         Path dumpDir = new Path(inputPath, "__debug");
         dfs.mkdirs(dumpDir);
         Path scriptDumpPath = new Path(dumpDir, "compiler.pig");
         System.out.printf("script saved at:%s\n", scriptDumpPath.toString());
         FSDataOutputStream fsdos = dfs.create(scriptDumpPath);
         try {
             fsdos.writeUTF(script);
         } finally {
             fsdos.close();
         }
         // ------------- debug: dump the script
         // ////////////////////////////////////
 
 //        runScript(script, inputPath);
 
         testClient(cubeModelRsrc);
 
         return 0;
     }
 
     private void testClient(Resource yamlModel) throws IOException, HblException {
         Deque<Closeable> closeables = new ArrayDeque<Closeable>();
         try {
             HblQueryClient queryClient = new HblQueryClient(getConf(), yamlModel);
             closeables.addFirst(queryClient);
 
             AggregateQuery query = queryClient.createQuery();
 
             byte ids[][] = new byte[2][];
             ids[0] = new byte[16];
             ids[1] = new byte[16];
             HblUtil.incrementKey(ids[1], 0, 16);
 
             /*
              * this should be equivalent to select aggr_func(impCnt),
              * aggr_func(click) from ... where dim1<=ids[0] and dim1>=ids[0]
              * group by dim1
              */
 
             query.addMeasure("impCnt").addMeasure("click");
             query.addClosedSlice("dim1", ids[0], ids[0]).addGroupBy("dim1");
             AggregateResultSet rs = query.execute();
             closeables.addFirst(rs);
             while (rs.hasNext()) {
                 rs.next();
                 AggregateResult ar = rs.current();
                 System.out.printf("%s sum/cnt: impCnt %.4f/%.0f, click %.4f/%.0f\n",
                                   ar.getGroupMember("dim1"),
                                   ar.getDoubleAggregate("impCnt", "SUM"),
                                   ar.getDoubleAggregate("impCnt", "COUNT"),
                                   ar.getDoubleAggregate("click", "SUM"),
                                   ar.getDoubleAggregate("click", "COUNT"));
             }
 
         } finally {
             IOUtil.closeAll(closeables);
         }
 
     }
 
     private static final int    N         = 24;
     private static final double clickRate = 0.25;
 
     private void simulateInput(FileSystem fs, Path inputDir) throws IOException {
         Deque<Closeable> closeables = new ArrayDeque<Closeable>();
 
         byte[] idBytes = new byte[16];
 
         ByteString[] id = new ByteString[2];
         id[0] = ByteString.copyFrom(idBytes);
         HblUtil.incrementKey(idBytes, 0, idBytes.length);
         id[1] = ByteString.copyFrom(idBytes);
 
         Random rnd = new Random();
 
         try {
             GregorianCalendar start = new GregorianCalendar(2011, 8, 1);
             start.setTimeZone(TimeZone.getTimeZone("UTC"));
             // flush the cal
             start.getTimeInMillis();
 
             Path inpFile = new Path(inputDir, "example1");
             fs.mkdirs(inputDir);
             SequenceFile.Writer w =
                 SequenceFile.createWriter(fs, getConf(), inpFile, IntWritable.class, BytesWritable.class);
             closeables.addFirst(w);
             IntWritable iw = new IntWritable();
             BytesWritable bw = new BytesWritable();
 
             for (int i = 0; i < N; i++) {
                 for (int k = 0; k < 2; k++) {
                     for (int j = 0; j < i + k; j++) {
                         CompilerInput.Builder inp = CompilerInput.newBuilder();
                         inp.setDim1(id[k]);
                         inp.setDim2(id[k]);
                         inp.setDim3(id[k]);
                         inp.setImpressionTime(start.getTimeInMillis());
                         inp.setImpCnt(1);
                         inp.setClick(rnd.nextDouble() > clickRate ? 0 : 1);
                         byte[] b = inp.build().toByteArray();
                         bw.set(b, 0, b.length);
                         w.append(iw, bw);
                     }
                 }
                 start.add(Calendar.HOUR_OF_DAY, 1);
             }
 
         } finally {
             IOUtil.closeAll(closeables);
         }
 
     }
 
     private void runScript(String script, Path inputPath) throws IOException {
 
         try {
             /*
              * this is a pig-version-specific hack to use grunt and its
              * preprocessors in sort of embedded mode. AFAIK it's not official
              * Pig's way to do this
              */
             PigContext pc = new PigContext();
 
             pc.setExecType(EXEC_TYPE);
             pc.getProperties().setProperty("pig.logfile", "pig.log");
             pc.getProperties().setProperty(PigContext.JOB_NAME, "sample1-compiler-run");
 
             pc.addJar("target/sample-0.1.0-SNAPSHOT-hadoop-job.jar");
 
             // pig-preprocess. We specified hbl input as $input in the
             // preambula, so
             // we now need to substitute it using Grunt's preprocessor.
 
             ParameterSubstitutionPreprocessor psp = new ParameterSubstitutionPreprocessor(512);
             StringWriter sw = new StringWriter();
             BufferedReader br = new BufferedReader(new StringReader(script));
             psp.genSubstitutedFile(br, sw, new String[] { "input=" + inputPath }, null);
             sw.close();
 
             script = sw.toString();
             sw = null;
             br = null;
 
             Grunt grunt = new Grunt(new BufferedReader(new StringReader(script)), pc);
 
             int[] codes = grunt.exec();
 
             int failed = codes[1];
             int succeeded = codes[0];
 
             System.out.printf("pig jobs failed:%d, pig jobs succeeded:%d.\n", failed, succeeded);
 
             if (failed != 0)
                 throw new IOException("Pig script execution failed, some jobs failed. Check the pig log for errors.");
 
         } catch (Throwable thr) {
             // sorry, Grunt really declares Throwable to be thrown
             if (thr instanceof IOException)
                 throw (IOException) thr;
             throw new IOException(thr);
         }
 
     }
 
 }
