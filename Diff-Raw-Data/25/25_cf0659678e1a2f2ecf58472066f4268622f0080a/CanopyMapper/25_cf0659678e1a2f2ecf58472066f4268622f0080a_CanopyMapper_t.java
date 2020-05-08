 package mapreduce;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapreduce.Mapper;
 
 import canopy.Canopy;
 import canopy.CanopyClusterer;
 
 import utils.VectorDoubleWritable;
 
 public class CanopyMapper extends
		Mapper<LongWritable, Text, IntWritable, VectorDoubleWritable> {
 	private VectorDoubleWritable point = null;
 	protected CanopyClusterer canopyClusterer = new CanopyClusterer();
 	private ArrayList<Canopy> canopies = new ArrayList<Canopy>();
 
 	
 
 	@Override
	protected void map(LongWritable key, Text values, Context context)
 			throws IOException, InterruptedException {
 		point = new VectorDoubleWritable(values);
 		canopyClusterer.addPointToCanopies(point, canopies);
 
 	}
 
 	@Override
 	protected void setup(Context context) throws IOException,
 			InterruptedException {
 		super.setup(context);
 		canopyClusterer = new CanopyClusterer(context.getConfiguration());
 	}
 
 	@Override
 	protected void cleanup(Context context) throws IOException,
 			InterruptedException {
 		for (Canopy canopy : canopies) { 
 			context.write(new IntWritable(canopy.getId()),
 					canopy.getCentroid());
 		}
 		super.cleanup(context);
 	}
 }
