 package mapreduce;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import clusterer.CanopyCluster;
 import clusterer.CanopyClusterer;
 import vector.VectorDoubleWritable;
 import config.Constants;
 
 public class CanopyReducer extends
		Reducer<Text, VectorDoubleWritable, LongWritable, Text> {
 
 	private final ArrayList<CanopyCluster> canopies = new ArrayList<CanopyCluster>();
 	private Text outvalue = new Text();
 	private CanopyClusterer canopyClusterer;
 
 	CanopyClusterer getCanopyClusterer() {
 		return canopyClusterer;
 	}
 
 	@Override
 	public void reduce(Text key, Iterable<VectorDoubleWritable> values,
 			Context context) throws IOException, InterruptedException {
		System.out.println("reducer:" + key.toString());
 		for (VectorDoubleWritable value : values) {
 			//System.out.println("1\n");
 			//value.print();
 			canopyClusterer.addPointToCanopies(value, canopies);
 		}
 		
 		for (CanopyCluster canopy: this.canopies) {
			outvalue.set(canopy.getCentroid().toString());
			System.out.println(canopy.getId().toString() + " " + outvalue);
			context.write(new LongWritable(canopy.getId()), outvalue); 
 		}
 	
 	}
 
 	@Override
 	public void setup(Context context) throws IOException, InterruptedException {
 		super.setup(context);
 		
 		canopyClusterer = new CanopyClusterer();
 		canopyClusterer.useT3T4(Constants.T3, Constants.T4);
 	}
 }
