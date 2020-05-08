 package KMeansPackage;
 
 import java.util.ArrayList;
 
 public class KMeans
 {
 	private int num_clusters = 2;    // Total clusters.
 	private int TOTAL_DATA = 7;      // Total data points.
 
 	private ArrayList<Data> Samples;
 	private static ArrayList<Data> dataSet = new ArrayList<Data>();
 	private static ArrayList<Centroid> centroids = new ArrayList<Centroid>();
 
 	public KMeans(ArrayList<Data> alData, ArrayList<Centroid> alCentroid, int numClusters){
 		Samples = new ArrayList<Data>();
 		for(Data d: alData){
 			Samples.add(d);
 		}
 		this.num_clusters = numClusters;
 		this.TOTAL_DATA = alData.size();
 		this.initialize();
 		this.kMeanCluster();
 	}
 	
 	public KMeans(ArrayList<Data> alData, int numClusters){
 		Samples = new ArrayList<Data>();
 		for(Data d: alData){
 			Samples.add(d);
 		}
 		this.num_clusters = numClusters;
 		this.TOTAL_DATA = alData.size();
 		this.initialize();
 		this.kMeanCluster();
 	}
 
 	public int getNumberOfClusters(){
 		return num_clusters;
 	}
 
 	public int getNumerOfData(){
 		return TOTAL_DATA;
 	}
 
 	public ArrayList<Data> getProcessedData(){
 		//return SAMPLES;
 		return dataSet;
 	}
 	
 	public ArrayList<Centroid> getCentroids(){
 		return centroids;
 	}
 
 	private void initialize()
 	{
 		System.out.println("Centroids initialized at:");
 		//centroids.add(new Centroid(1.0, 1.0)); // lowest set.
 		//centroids.add(new Centroid(5.0, 7.0)); // highest set.
 		double stepX = (Math.abs(getMinX())+Math.abs(getMaxX()))/num_clusters;
 		double stepY = (Math.abs(getMinY())+Math.abs(getMaxY()))/num_clusters;
 		for(int j=0; j<this.num_clusters; j++)
 			centroids.add(new Centroid (getMinX()+((2*j+1)*(stepX/2)),getMinY()+((2*j+1)*(stepY/2))));
 		
		System.out.println(centroids.get(0));
		System.out.println(centroids.get(1));
 		System.out.print("\n");
 		return;
 	}
 	
 	private double getMaxX(){
 		double max=-Double.MAX_VALUE;
 		for(Data a:Samples){
 			if(a.X()>max)
 				max=a.X();
 		}
 		return max;
 	}
 	
 	private double getMinX(){
 		double min = Double.MAX_VALUE;
 		for(Data a: Samples){
 			if(a.X()<min)
 				min=a.X();
 		}
 		return min;
 	}
 	
 	private double getMaxY(){
 		double max=-Double.MAX_VALUE;
 		for(Data a:Samples){
 			if(a.Y()>max)
 				max=a.Y();
 		}
 		return max;
 	}
 	
 	private double getMinY(){
 		double min = Double.MAX_VALUE;
 		for(Data a: Samples){
 			if(a.Y()<min)
 				min=a.Y();
 		}
 		return min;
 	}
 
 	private void kMeanCluster()
 	{
 		final double bigNumber = Double.MAX_VALUE;    // some big number that's sure to be larger than our data range.
 		double minimum = bigNumber;                   // The minimum value to beat. 
 		double distance = 0.0;                        // The current minimum value.
 		int sampleNumber = 0;
 		int cluster = 0;
 		boolean isStillMoving = true;
 		Data newData = null;
 
 		// Add in new data, one at a time, recalculating centroids with each new one. 
 		while(dataSet.size() < TOTAL_DATA)
 		{
 			newData = new Data(Samples.get(sampleNumber));
 			dataSet.add(newData);
 			minimum = bigNumber;
 			for(int i = 0; i < num_clusters; i++)
 			{
 				distance = dist(newData, centroids.get(i));
 				if(distance < minimum){
 					minimum = distance;
 					cluster = i;
 				}
 			}
 			newData.cluster(cluster);
 
 			// calculate new centroids.
 			for(int i = 0; i < num_clusters; i++)
 			{
 				int totalX = 0;
 				int totalY = 0;
 				int totalInCluster = 0;
 				for(int j = 0; j < dataSet.size(); j++)
 				{
 					if(dataSet.get(j).cluster() == i){
 						totalX += dataSet.get(j).X();
 						totalY += dataSet.get(j).Y();
 						totalInCluster++;
 					}
 				}
 				if(totalInCluster > 0){
 					centroids.get(i).X(totalX / totalInCluster);
 					centroids.get(i).Y(totalY / totalInCluster);
 				}
 			}
 			sampleNumber++;
 		}
 
 		// Now, keep shifting centroids until equilibrium occurs.
 		while(isStillMoving)
 		{
 			// calculate new centroids.
 			for(int i = 0; i < num_clusters; i++)
 			{
 				int totalX = 0;
 				int totalY = 0;
 				int totalInCluster = 0;
 				for(int j = 0; j < dataSet.size(); j++)
 				{
 					if(dataSet.get(j).cluster() == i){
 						totalX += dataSet.get(j).X();
 						totalY += dataSet.get(j).Y();
 						totalInCluster++;
 					}
 				}
 				if(totalInCluster > 0){
 					centroids.get(i).X(totalX / totalInCluster);
 					centroids.get(i).Y(totalY / totalInCluster);
 				}
 			}
 
 			// Assign all data to the new centroids
 			isStillMoving = false;
 
 			for(int i = 0; i < dataSet.size(); i++)
 			{
 				Data tempData = dataSet.get(i);
 				minimum = bigNumber;
 				for(int j = 0; j < num_clusters; j++)
 				{
 					distance = dist(tempData, centroids.get(j));
 					if(distance < minimum){
 						minimum = distance;
 						cluster = j;
 					}
 				}
 				tempData.cluster(cluster);
 				if(tempData.cluster() != cluster){
 					tempData.cluster(cluster);
 					isStillMoving = true;
 				}
 			}
 		}
 		return;
 	}
 
 	/**
 	 * // Calculate Euclidean distance.
 	 * @param d - Data object.
 	 * @param c - Centroid object.
 	 * @return - double value.
 	 */
 	private static double dist(Data d, Centroid c)
 	{
 		return Math.sqrt(Math.pow((c.Y() - d.Y()), 2) + Math.pow((c.X() - d.X()), 2));
 	}
 }
