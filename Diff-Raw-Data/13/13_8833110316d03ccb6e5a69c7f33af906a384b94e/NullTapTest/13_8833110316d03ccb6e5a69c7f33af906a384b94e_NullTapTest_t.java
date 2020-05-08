 package backtype.cascading.tap;
 
 import backtype.support.FSTestCase;
 import cascading.flow.hadoop.HadoopFlowConnector;
 import cascading.flow.hadoop.HadoopFlowProcess;
 import cascading.operation.Identity;
 import cascading.pipe.Each;
 import cascading.pipe.Pipe;
 import cascading.scheme.hadoop.TextLine;
 import cascading.tap.Tap;
 import cascading.tap.hadoop.Lfs;
 import cascading.tuple.Fields;
 import cascading.tuple.Tuple;
 import cascading.tuple.TupleEntryCollector;
 import org.apache.hadoop.mapred.JobConf;
 
 import static backtype.support.TestUtils.getTmpPath;
 
 public class NullTapTest extends FSTestCase {
 
     public void testNullTap() throws Exception {
         String tmp = getTmpPath(fs, "pail");
         Tap source = new Lfs(new TextLine(new Fields("line")), tmp);
         TupleEntryCollector coll = source.openForWrite(new HadoopFlowProcess(new JobConf()));
 
         coll.add(new Tuple("lalalala"));
         coll.add(new Tuple("lalalala2"));
         coll.close();
 
         Pipe p = new Pipe("pipe");
         p = new Each(p, new Fields("line"), new Identity());
 
         //just make sure there's not exception
         new HadoopFlowConnector().connect(source, new NullTap(), p).complete();
     }
 
 }
