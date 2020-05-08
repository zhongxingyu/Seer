 package scala.colleciton.distributed.hadoop;
 
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.mapred.lib.MultipleOutputs;
import scala.collection.distributed.api.dag.OutputPlanNode;
 import tasks.DistributedCollectionsCombine;
import tasks.DistributedCollectionsMap;
 import tasks.DistributedCollectionsReduce;
 
 import java.io.IOException;
 
 /**
  * User: vjovanovic
  * Date: 4/23/11
  */
 public class QuickTypeFixScalaI0 {
     public static void setJobClassesBecause210SnapshotWillNot(JobConf job, boolean setCombiner, boolean setReducer, String[] outputs) throws IOException {
         job.setInputFormat(SequenceFileInputFormat.class);
         job.setOutputFormat(SequenceFileOutputFormat.class);
        job.setMapperClass(DistributedCollectionsMap.class);
 
         if (setReducer) {
             job.setReducerClass(DistributedCollectionsReduce.class);
         }
 
         if (setCombiner) {
             job.setCombinerClass(DistributedCollectionsCombine.class);
         }
 
         for (String output : outputs) {
             MultipleOutputs.addNamedOutput(job,
                      output, SequenceFileOutputFormat.class,
                     NullWritable.class, BytesWritable.class);
         }
     }
 
     public static void hadoopOut(MultipleOutputs output, String name, Reporter rep, NullWritable k, BytesWritable v) throws IOException {
         output.getCollector(name, rep).collect(k, v);
     }
 }
