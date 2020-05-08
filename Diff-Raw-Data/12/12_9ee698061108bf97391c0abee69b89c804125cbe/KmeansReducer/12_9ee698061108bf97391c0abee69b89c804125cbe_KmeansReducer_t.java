 package mapreduce;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import clusterer.KmeansCluster;
 import clusterer.KmeansClusterer;
 import config.Constants;
 import distanceMeasure.DistanceMeasure;
 import distanceMeasure.EuclideanDistance;
 
 public class KmeansReducer extends
 		Reducer<LongWritable, KmeansCluster, LongWritable, KmeansCluster> {
 	protected KmeansClusterer clusterer;
 	protected double threshold;
 	Map<Long, KmeansCluster> clusterMap = new HashMap<Long, KmeansCluster>();
 
 	@Override
 	public void reduce(LongWritable key, Iterable<KmeansCluster> values,
 			Context context) throws IOException {
 		KmeansCluster cluster = clusterMap.get(key.get());
 		System.out.println("Reducer cluster:\t" + cluster.getId());
 		System.out.println("BEFORE REDUCE");
		for (KmeansCluster kcluster : clusterer.getClusters()) {
 			kcluster.logSize();
 		}
 		for (KmeansCluster value : values) {
 			System.out.println("Values:\t" + key + "\t"
 					+ value.getCentroid().get().toString());
			cluster.omitCluster(value);
 		}
 		System.out.println("Cluster:\t" + cluster.getCentroid().get());
 
 		if (clusterer.isConverged(cluster, threshold))
 			context.getCounter(Constants.COUNTER_GROUP,
 					Constants.COUNTER_CONVERGED).increment(1);
 		try {
 			System.out.println("Context:\t" + key + "\t"
 					+ cluster.getCentroid());
 			context.write(key, cluster);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		System.out.println("Converaged cluster:"
 				+ context.getCounter(Constants.COUNTER_GROUP,
 						Constants.COUNTER_CONVERGED).getValue()
 				+ "\tTotal clusters:"
 				+ context.getCounter(Constants.COUNTER_GROUP,
 						Constants.COUNTER_TOTAL).getValue());
 		System.out.println("AFTER REDUCE");
		for (KmeansCluster kcluster : clusterer.getClusters()) {
 			kcluster.logSize();
 		}
 	}
 
 	@Override
 	public void setup(Context context) throws IOException, InterruptedException {
 		super.setup(context);
 		Configuration conf = context.getConfiguration();
 		threshold = conf.getFloat(Constants.THRESHOLD, 0.000000001f);
 		DistanceMeasure dm = null;
 		try {
 			dm = (DistanceMeasure) Class.forName(
 					"distanceMeasure."
 							+ conf.get(Constants.DISTANCE_MEASURE,
 									"EuclideanDistance")).newInstance();
 		} catch (InstantiationException e) {
 			dm = new EuclideanDistance();
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			dm = new EuclideanDistance();
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			dm = new EuclideanDistance();
 			e.printStackTrace();
 		}
 
 		this.clusterer = new KmeansClusterer(dm);
 
 		String clusterPath = conf.get(Constants.CLUSTER_PATH);
 		if (clusterPath != null && !clusterPath.isEmpty())
 			try {
 				this.clusterer.loadClusters(clusterPath, conf);
 				for (KmeansCluster cluster : this.clusterer.getClusters()) {
					clusterMap.put(new Long(cluster.getId()),
							new KmeansCluster(cluster.getId()));
 				}
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 			}
 
 		/*
 		 * for (KmeansCluster clu : clusterer.getClusters()) {
 		 * System.out.println("Cluster " + clu.getId() + "/" +
 		 * this.clusterer.getClusters().size() + ":\t" + clu.getCentroid()); }
 		 */
 
 	}
 }
