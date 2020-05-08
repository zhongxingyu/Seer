 package me.sheimi.pig.storage;
 
 import java.io.IOException;
 
 import me.sheimi.hadoop.seq.LocalSetup;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.OutputFormat;
 import org.apache.hadoop.mapreduce.RecordWriter;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.pig.StoreFunc;
 import org.apache.pig.data.DataByteArray;
 import org.apache.pig.data.Tuple;
 
import me.sheimi.magic.image.*;
 
 public class SequenceFileStorage extends StoreFunc {
 	
 	RecordWriter writer;
 	Text key = new Text();
 	BytesWritable value = new BytesWritable();
 	
 	
 	@Override
 	public OutputFormat getOutputFormat() throws IOException {
 		return new SequenceFileOutputFormat<Text, BytesWritable>();
 	}
 
 	@Override
 	public void prepareToWrite(RecordWriter arg0) throws IOException {
 		this.writer = arg0;
 	}
 
 	@Override
 	public void putNext(Tuple tuple) throws IOException {
 		if (tuple.size() < 2) {
 			System.out.println(tuple.size());
 			return;
 		}
 		key.set(tuple.get(0).toString());
 		byte[] data = ((DataByteArray)tuple.get(1)).get(); // the real bytes
     
    Image image = new Image(data, null);
     byte[] encoded = image.encode();
     value.set(encoded, 0, encoded.length);
 
     try {
 			writer.write(key, value);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void setStoreLocation(String location, Job job) throws IOException {
 	    job.setOutputKeyClass(Text.class);
 	    job.setOutputValueClass(BytesWritable.class);
 	    job.setOutputFormatClass(SequenceFileOutputFormat.class);
 	    FileOutputFormat.setOutputPath(job, new Path(location));
 	}
 
 }
