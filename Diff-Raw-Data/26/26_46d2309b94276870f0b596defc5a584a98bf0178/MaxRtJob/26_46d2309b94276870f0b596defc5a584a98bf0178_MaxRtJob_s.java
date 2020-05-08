 package fr.xebia.devoxx.hadoop.mostRt;
 
 import fr.xebia.devoxx.hadoop.fr.xebia.devoxx.common.output.PushServerFormat;
 import fr.xebia.devoxx.hadoop.mostRt.model.DumbPartitioner;
 import fr.xebia.devoxx.hadoop.mostRt.model.GroupAllComparator;
 import fr.xebia.devoxx.hadoop.mostRt.model.TwitterStreamCount;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 
 import java.io.IOException;
 
 public class MaxRtJob extends Job {
     public MaxRtJob(Configuration conf) throws IOException {
 
         super(conf);
 
         this.setMapperClass(CountRtMapper.class);
         this.setMapOutputKeyClass(TwitterStreamCount.class);
         this.setMapOutputValueClass(NullWritable.class);
 
         this.setReducerClass(MaxReducer.class);
         this.setOutputKeyClass(TwitterStreamCount.class);
         this.setOutputValueClass(NullWritable.class);
 
         this.setInputFormatClass(SequenceFileInputFormat.class);
         this.setOutputFormatClass(PushServerFormat.class);
 
         this.setPartitionerClass(DumbPartitioner.class);
         this.setGroupingComparatorClass(GroupAllComparator.class);
 
         this.setJarByClass(MaxRtJob.class);
     }
 }
