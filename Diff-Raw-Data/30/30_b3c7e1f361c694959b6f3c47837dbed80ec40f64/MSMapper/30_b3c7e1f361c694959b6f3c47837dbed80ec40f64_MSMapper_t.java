 package meanshift;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.util.List;
 import java.util.ArrayList;
 
 import com.asgow.ciel.executor.Ciel;
 import com.asgow.ciel.references.Reference;
 import com.asgow.ciel.tasks.FirstClassJavaTask;
 
 public class MSMapper implements FirstClassJavaTask {
 
 	private Reference clustersRef;
 	private int numDimensions;
 	private double t1;
 	private double t2;
 	private int idNum;
	private int iteration;
 
	public MSMapper(Reference clustersRef, int numDimensions,
		double t1, double t2, int idNum, int iteration) throws Exception {
 		this.clustersRef = clustersRef;
 		this.numDimensions = numDimensions;
 		this.t1 = t1;
 		this.t2 = t2;
 		this.idNum = idNum;
		this.iteration = iteration;
 	}
 	
 	@Override
 	public Reference[] getDependencies() {
 		return new Reference[] {clustersRef};
 	}
 
 	@Override
 	public void invoke() throws Exception {
 		System.out.println("Mapper id: " + idNum);
 	
 		ObjectInputStream inputStream = new ObjectInputStream
 			(new FileInputStream
 			(Ciel.RPC.getFilenameForReference(clustersRef)));
 		List<MSCluster> oldClusters =
 			(List<MSCluster>) inputStream.readObject();
 		inputStream.close();
 
 		int i = 0;
 		for (MSCluster c: oldClusters) {
 			print(c.getBoundPoints(),
				"/tmp/data/it" + iteration + "_mapper" +
				idNum + "_c" + i + ".txt");
 			i++;
 		}
 
 		//---------------------------------------------------------------------
 		// Now do the actual processing on the vectors.
 
 		List<MSCluster> migratedClusters = new ArrayList<MSCluster>();
 		for (MSCluster c: oldClusters) {
 			shiftToMean(c);
 			mergeCanopy(c, migratedClusters);
 		}
 
 		Ciel.returnObject(migratedClusters);
 	}
 
 	@Override
 	public void setup() {
 	}
 
 	/*
 	Forcing the centre to shift to the new centroid of the cluster.
 	*/
 	private void shiftToMean(MSCluster cluster) {
 		cluster.forceUpdateCentre();
		System.out.println("Mapper" + idNum + ": " + "Updated centre");
 	}
 
 	/*
 	If the cluster is within t1 distance of a cluster in the migratedList,
 	then the centres are added together and put in both clusters.
 	If the cluster is within t2 distance of a cluster in the migratedList,
 	then the clusters are merged. They are not merged right away as there
 	can potentially be a closer cluster and then it takes preference for the
 	merge.
 	*/
 	private void mergeCanopy(MSCluster cluster,
 		List<MSCluster> migratedClusters) {
 
 		MSCluster closestCoveringCluster = null;
 		double closestDistance = Double.MAX_VALUE;
 		for (MSCluster c: migratedClusters) {
			double distance = c.getDistanceToCentre(cluster.getCentre());
			System.out.println("Mapper" + idNum + ": Distance " + distance);
 			if (distance < t1) {
 				cluster.add(c.getCentre());
 				c.add(cluster.getCentre());
				System.out.println("Mapper" + idNum + ": " + "Adding centres");
 			}
 
 			if (distance < t2 && distance < closestDistance) {
 				closestCoveringCluster = c;
 				closestDistance = distance;
				System.out.println("Mapper" + idNum + ": " + "Possible merge");
 			}
 		}
 
 		if (closestCoveringCluster == null) {
 			migratedClusters.add(cluster);
			System.out.println("Mapper" + idNum + ": " + "Added to list");
 		}
 		else {
 			closestCoveringCluster.merge(cluster);
			System.out.println("Mapper" + idNum + ": " + "Merge occurring");
 		}
 	}
 
 	private void print(List<double[]> vectors, String path)
 		throws Exception {
 		File file = new File(path);
 		if (!file.exists()) {
 			file.createNewFile();
 		}
 		FileWriter fw = new FileWriter(file.getAbsoluteFile());
 		BufferedWriter bw = new BufferedWriter(fw);
 
 		for (int i = 0; i < vectors.size(); i++) {
 			for (int j = 0; j < numDimensions; j++) {
 				bw.write(vectors.get(i)[j] + " ");
 			}
 			bw.write("\n");
 		}
 		bw.close();
 	}
 }
