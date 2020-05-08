 package clusterer;
 
 import org.apache.hadoop.io.Writable;
 
 import vector.VectorDoubleWritable;
 
 public class KmeansCluster extends Cluster implements Writable, Cloneable {
 
 	public KmeansCluster() {
 		super();
 	}
 
 	public KmeansCluster(int i) {
 		super(i);
 	}
 
 	public KmeansCluster(int id, VectorDoubleWritable s1,
 			VectorDoubleWritable s2) throws IllegalStateException {
 		super(id, s1, s2);
 	}

 	public KmeansCluster(int id, int size, VectorDoubleWritable s1,
 			VectorDoubleWritable s2) throws IllegalStateException {
 		super(id, size, s1, s2);
 	}
 }
