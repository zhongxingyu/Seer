 package com.etsy.rdb;
 
 import cascading.flow.FlowProcess;
 import cascading.scheme.Scheme;
 import cascading.scheme.SinkCall;
 import cascading.scheme.SourceCall;
 import cascading.tap.Tap;
 import cascading.tuple.Fields;
 import cascading.tuple.Tuple;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.RecordReader;
 import java.io.IOException;
 
 public class RDBScheme extends Scheme<JobConf, RecordReader, OutputCollector, Object[], Void>
   {
     public RDBScheme( Fields fields )
     {
     super( fields, fields );
     }
 
   @Override
   public void sourceConfInit( FlowProcess<JobConf> flowProcess, Tap<JobConf, RecordReader, OutputCollector> tap, JobConf conf )
     {
     }
 
   @Override
   public void sourcePrepare( FlowProcess<JobConf> flowProcess, SourceCall<Object[], RecordReader> sourceCall )
     {
     }
 
   @Override
   public void sinkConfInit( FlowProcess<JobConf> flowProcess, Tap<JobConf, RecordReader, OutputCollector> tap, JobConf conf )
     {
     conf.setOutputKeyClass( RDBString.class );
     conf.setOutputValueClass( RDBString.class );
     conf.setOutputFormat( RDBOutputFormat.class );
     }
 
   @Override
   public boolean source( FlowProcess<JobConf> flowProcess, SourceCall<Object[], RecordReader> sourceCall ) throws IOException
     {
       return false;
     }
 
   @Override
   public void sink( FlowProcess<JobConf> flowProcess, SinkCall<Void, OutputCollector> sinkCall ) throws IOException
     {
       Tuple tuple = sinkCall.getOutgoingEntry().getTuple();
      sinkCall.getOutput().collect( RDBString.uncompressed(tuple.getObject(0).toString().getBytes()), 
                                    RDBString.uncompressed(tuple.getObject(1).toString().getBytes()));
     }
  }
