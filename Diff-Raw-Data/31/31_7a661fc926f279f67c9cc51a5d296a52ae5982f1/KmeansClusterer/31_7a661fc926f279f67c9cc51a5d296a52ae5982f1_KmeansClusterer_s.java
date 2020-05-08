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
 
 public class KmeansClusterer {
 	protected ArrayList<KmeansCluster> clusters = new ArrayList<KmeansCluster>();
 	protected HashMap<Long, KmeansCluster> clusterMap = new HashMap<Long, KmeansCluster>();
 	protected DistanceMeasure dm;
 
 	public KmeansClusterer() {
 		clusters.clear();
 		this.dm = new EuclideanDistance();
 	}
 
 	public KmeansClusterer(DistanceMeasure dm) {
 		clusters.clear();
 		this.dm = dm;
 	}
 
 	public void loadClusters(String clusterPath, Configuration conf)
 			throws IOException, URISyntaxException {
 		Path path = new Path(clusterPath);
 		FileSystem fs = FileSystem.get(new URI(clusterPath), conf);
 
 		SequenceFile.Reader reader = null;
 
 		reader = new SequenceFile.Reader(fs, path, conf);
 		LongWritable key = (LongWritable) ReflectionUtils.newInstance(
 				reader.getKeyClass(), conf);
 		KmeansCluster value = (KmeansCluster) ReflectionUtils.newInstance(
 				reader.getValueClass(), conf);
 
 		while (reader.next(key, value)) {
 			clusters.add(value);
 			clusterMap.put(new Long(value.getId()),
 					(KmeansCluster) value.clone());
 			value = new KmeansCluster();
 		}
 
 		IOUtils.closeStream(reader);
 	}
 
 	public KmeansCluster findNearestCluster(VectorDoubleWritable point)
 			throws IllegalStateException {
 		KmeansCluster nearest = null;
 		double mindist = Double.MAX_VALUE;
 		double tempdist = 0;
 		System.out.println(clusters.size());
		int index = 0;
 		for (KmeansCluster cluster : clusters) {
 			tempdist = dm.distance(cluster.getCentroid(), point);
 			System.out.print(tempdist + "\t");
 			if (tempdist < mindist) {
 				mindist = tempdist;
 				nearest = cluster;
 			}
			index++;
 		}
 		System.out.println();
 		return nearest;
 	}
 
 	public boolean isConverged(KmeansCluster cluster, double threshold) {
 		KmeansCluster last = clusterMap.get(new Long(cluster.getId()));
 		double dist = dm.distance(cluster.getCentroid(), last.getCentroid());
 		if (Constants.DEBUG)
 			System.out.println("DIST:" + dist);
 		return dist < threshold;
 	}
 
 	public ArrayList<KmeansCluster> getClusters() {
 		return this.clusters;
 	}
 
 	public void setClusters(ArrayList<KmeansCluster> clusters) {
 		this.clusters.clear();
 		for (KmeansCluster data : clusters) {
 			this.clusters.add(data);
 		}
 	}
 }
