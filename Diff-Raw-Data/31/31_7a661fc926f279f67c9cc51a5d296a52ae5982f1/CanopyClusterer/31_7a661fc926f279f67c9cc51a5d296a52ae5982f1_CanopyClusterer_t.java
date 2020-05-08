 package clusterer;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IOUtils;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.util.ReflectionUtils;
 
import config.Constants;
 import vector.VectorDoubleWritable;
 import distanceMeasure.DistanceMeasure;
 import distanceMeasure.EuclideanDistance;
 
 public class CanopyClusterer {
 	protected ArrayList<CanopyCluster> canopies = new ArrayList<CanopyCluster>();
 	protected HashMap<Integer, CanopyCluster> clusterMap = 
 				new HashMap<Integer, CanopyCluster>();
 	protected DistanceMeasure dm;
 	protected double t1;
 	protected double t2;
 	protected double t3;
 	protected double t4;
 	protected int nextID;
 
 	public CanopyClusterer() {
 		canopies.clear();
 		this.dm = new EuclideanDistance();
 		nextID = 0;
 	}
 
 	public CanopyClusterer(DistanceMeasure dm, double t1, double t2) {
 		canopies.clear();
 		this.dm = dm;
 		this.t1 = t1;
 		this.t2 = t2;
 		this.t3 = t1;
 		this.t4 = t2;
 		nextID = 0;
 	}
 	public void useT3T4(double t3, double t4) {
 		this.t3 = this.t1;
 		this.t4 = this.t2;
 		this.t1 = t3; 
 		this.t2 = t4;
 	}
 	public void useT1T2() {
 		this.t1 = this.t3;
 		this.t2 = this.t4;
 	}
 
 	public CanopyClusterer(Configuration configuration) {
 		// TODO Auto-generated constructor stub
 		canopies.clear();
 		this.t1 = Double
 				.parseDouble(configuration.get(config.Constants.T1_KEY));
 		this.t2 = Double
 				.parseDouble(configuration.get(config.Constants.T2_KEY));
 		nextID = 0;
 	}
 
 	public void loadClusters(String clusterPath, Configuration conf) // need to
 																	 // be
 																	 // checked
 			throws IOException, URISyntaxException {
 		Path path = new Path(clusterPath);
 		FileSystem fs = FileSystem.get(new URI(clusterPath), conf);
 
 		SequenceFile.Reader reader = null;
 
 		reader = new SequenceFile.Reader(fs, path, conf);
 		LongWritable key = (LongWritable) ReflectionUtils.newInstance(
 				reader.getKeyClass(), conf);
 		CanopyCluster value = (CanopyCluster) ReflectionUtils.newInstance(
 				reader.getValueClass(), conf);
 
 		while (reader.next(key, value)) {
 			canopies.add(value);
 			nextID++;
 		}
 		IOUtils.closeStream(reader);
 	}
 
 	public CanopyCluster findNearestCluster(VectorDoubleWritable point)
 			throws IllegalStateException {
 		CanopyCluster nearest = null;
 		double mindist = Double.MAX_VALUE;
 		double tempdist = 0;
 		for (CanopyCluster cluster : canopies) {
 			tempdist = cluster.euclideanDistance(point);
 				if (tempdist < mindist) {
 				mindist = tempdist;
 				nearest = cluster;
 			}
 		}
 		return nearest;
 	}
 
 	public boolean addPointToCanopies(VectorDoubleWritable point,
 			ArrayList<CanopyCluster> canopies) throws IllegalStateException {
 		boolean pointStronglyBound = false;
 		double tempdist = 0;
 		for (CanopyCluster canopy : canopies) {
 			tempdist = canopy.euclideanDistance(point);
 			if (tempdist < this.t1) {
 				canopy.addPoint(point);
				pointStronglyBound = true;
			}
			if (Constants.DEBUG) {
				if (pointStronglyBound) {
					System.out.print("bound   \t");
				} else {
					System.out.print("notbound\t");
				}
				System.out.println(tempdist);
 			}
 			pointStronglyBound = pointStronglyBound || tempdist < t2;  
 		}
 		if (!pointStronglyBound) {
 			CanopyCluster newCanopy = new CanopyCluster(nextID++, point);
 			canopies.add(newCanopy);
 		}
 		return pointStronglyBound;
 	}
 
 	public ArrayList<CanopyCluster> getClusters() {
 		return this.canopies;
 	}
 }
