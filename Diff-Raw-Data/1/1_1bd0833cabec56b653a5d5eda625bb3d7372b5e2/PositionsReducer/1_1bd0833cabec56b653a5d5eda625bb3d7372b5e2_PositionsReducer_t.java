 package com.samples.hadoop.positions;
 
import org.apache.commons.math.util.MathUtils;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.VLongWritable;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rixon
  * Date: 12/2/13
  * Time: 7:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PositionsReducer extends Reducer<LongWritable,VLongWritable,LongWritable,DoubleWritable> {
 
     @Override
     protected void reduce(LongWritable key, Iterable<VLongWritable> values, Context context) throws IOException, InterruptedException {
         long sum = 0;
         long count=0;
         for(VLongWritable returnValue:values) {
             sum+=returnValue.get();
             count++;
         }
         double averageReturn = (double)sum/(double)count;
         context.write(key,new DoubleWritable(averageReturn));
     }
 }
