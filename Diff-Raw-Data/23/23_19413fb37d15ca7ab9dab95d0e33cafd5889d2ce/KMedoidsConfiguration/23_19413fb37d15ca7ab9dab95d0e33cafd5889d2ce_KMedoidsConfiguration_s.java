 package mishanesterenko.iad.lb1.core.clustering;
 
 import mishanesterenko.iad.lb1.core.plugin.ClusteringConfiguration;
 import mishanesterenko.iad.lb1.core.plugin.DistanceFunction;
 
 public class KMedoidsConfiguration extends ClusteringConfiguration {
 	private int[] indices;

 	public KMedoidsConfiguration(DistanceFunction distanceFunction) {
 		super(distanceFunction);
 	}
 
 }
