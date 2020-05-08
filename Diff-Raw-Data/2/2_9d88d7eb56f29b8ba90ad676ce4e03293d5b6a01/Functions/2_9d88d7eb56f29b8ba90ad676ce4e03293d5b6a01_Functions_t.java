 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 public class Functions
 {
 	// Static Variables
 	public static final int MANHATTEN			= 0;
 	public static final int EUCLIDEAN			= 1;
 	
 	public static final int MST					= 0;
 	public static final int KMEANS				= 1;
 	public static final int ZSCORE				= 2;
 	
 	public static final int ZSCORE_LARGE_VALUE	= 100;
 	
 	public static final int LAMBDA				= 0;
 	public static final int RANDOM				= 1;
 	public static final int BLOCK_SIZE			= 2;
 	public static final int RUN_LENGTH			= 3;
 	public static final int RPM					= 4;
 	public static final int SEEK_RANDOM			= 5;
 	public static final int TRANSFER_RATE		= 6;
 	public static final int CONTROLLER_TIME		= 7;
 	public static final int ITERATIONS			= 8;
 	
 	public static String[] serviceDemandOutputStrings	= {	"Service Demand Random", "Utilization",
 															"Random Seek Time", "Service Demand Sequential",
 															"Service Demand"									};
 
 	public static ArrayList<String> serviceDemand(ArrayList<Double> parameters)
 	{
 		ArrayList<String> output = new ArrayList<String>();
 		double serviceDemandRandom =	parameters.get(CONTROLLER_TIME) + parameters.get(SEEK_RANDOM) +
 										(0.5 * (60/parameters.get(RPM)) * 1000) +
 										((parameters.get(BLOCK_SIZE)*1000)/(parameters.get(TRANSFER_RATE)*1000000));
 		output.add(serviceDemandOutputStrings[0] + ": " + round(serviceDemandRandom));
 		double utilization = 0.0;
 		double randomSeekTime = 0.0;
 		double serviceDemandSequential = 0.0;
 		double serviceDemand = 0.0;
 		for(int i = 0; i < parameters.get(ITERATIONS); i++)
 		{
 			utilization = (i > 0)?(parameters.get(LAMBDA)/1000)*serviceDemand:(parameters.get(LAMBDA)/1000)*serviceDemandRandom;
 			output.add(serviceDemandOutputStrings[1] + ": " + round(utilization));
 			randomSeekTime = ((0.5 + (parameters.get(RUN_LENGTH)-1)*((1+utilization)/(2)))/(parameters.get(RUN_LENGTH)))*(60/parameters.get(RPM))*1000;
 			output.add(serviceDemandOutputStrings[2] + ": " + round(randomSeekTime));
 			serviceDemandSequential = parameters.get(CONTROLLER_TIME) + (1/parameters.get(RUN_LENGTH))*((parameters.get(BLOCK_SIZE)*1000)/(parameters.get(TRANSFER_RATE)*1000000)) + parameters.get(SEEK_RANDOM)/parameters.get(RUN_LENGTH) + randomSeekTime;
 			output.add(serviceDemandOutputStrings[3] + ": " + round(serviceDemandSequential));
 			serviceDemand = ((parameters.get(RANDOM))/(100))*serviceDemandRandom + ((100-parameters.get(RANDOM))/(100))*serviceDemandSequential;
 			output.add(serviceDemandOutputStrings[4] + ": " + round(serviceDemand));
 		}
 
 		return output;
 	}
 	
 	public static double round(Double value)
 	{
 		return Double.valueOf(new DecimalFormat("#.###").format(value));
 	}
 	
 	public static double getDistance(Point p1, Point p2, int distanceType)
 	{
 		double sum = 0.0;
 
 		switch(distanceType)
 		{
 			case MANHATTEN:
 				for(int i = 0; i < p1.getDimensionSize(); i++)
 					sum += Math.abs(p2.getValueAtDimension(i) - p1.getValueAtDimension(i));
 				
 				return sum;
 				
 			case EUCLIDEAN:
 				for(int i = 0; i < p1.getDimensionSize(); i++)
 					sum += Math.pow(p2.getValueAtDimension(i) - p1.getValueAtDimension(i), 2);
 				
 				return Math.sqrt(sum);
 				
 			default:
 				return -1.0;
 		}
 	}
 	
 	public static String getOperatingSystemPath()
 	{
		if (System.getProperty("os.name").split(" ")[0].equals("Windows"))
 		{
 			if(Double.parseDouble(System.getProperty("os.version")) > 5.1)
 				return "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\Result.csv";
 			else
 				return "C:\\Documents and Settings\\" + System.getProperty("user.name") + "\\Desktop\\Result.csv";
 		}
 		else if (System.getProperty("os.name").equals("Linux"))
 			return "/home/" + System.getProperty("user.name") + "/Desktop/Results.csv";
 
 		return null;
 	}
 	
 	public static boolean MST(ArrayList<Point> points, int numOfClusters, int distanceType) throws IOException
 	{
 		FileWriter file = new FileWriter(getOperatingSystemPath());
 		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
 		
 		// Make Every Point a Cluster
         for (int i = 0; i < points.size(); i++)
             clusters.add(new Cluster(points.get(i), "C" + (i+1)));
         
         // MST Algorithm (Loop until number of clusters remaining equals desired number of clusters)
         Double distance = 0.0;
         while (clusters.size() > numOfClusters)
         {
         	for(Cluster c: clusters)
         		file.append("," + c.getName());
 
         	file.append("\n");
             
             int index1 = 0;
             int index2 = 0;
             double min = Double.MAX_VALUE;
             for (int i = 0; i < clusters.size(); i++)
             {
                 file.append(clusters.get(i).getName() + ",");
                 for (int j = 0; j < clusters.size(); j++)
                 {
                 	distance = getDistance(clusters.get(i).getCentoid(), clusters.get(j).getCentoid(), distanceType);
 
                     file.append(distance + ",");
                     if ((i != j) && (distance < min))
                     {
                         min = distance;
                         index1 = i;
                         index2 = j;
                     }
                 }
                 file.append("\n");
             }
             file.append("\n");
 
             for (int i = 0; i < clusters.get(index2).getNumberOfPoints(); i++)
                 clusters.get(index1).addPoint(clusters.get(index2).getPointAt(i));
 
             clusters.get(index1).setName("[" + clusters.get(index1).getName() + "|" + clusters.get(index2).getName() + "]");
             clusters.remove(index2);
 
         }
 
         for (int i = 0; i < clusters.size(); i++)
             file.append("," + clusters.get(i).getName());
 
         file.append("\n");
 
         for (int i = 0; i < clusters.size(); i++)
         {
             file.append(clusters.get(i).getName() + ",");
             for (int j = 0; j < clusters.size(); j++)
                 file.append(getDistance(clusters.get(i).getCentoid(), clusters.get(j).getCentoid(), distanceType) + ",");
 
             file.append("\n");
         }
 		
 		file.close();
 		return true;
 	}
 
 	public static boolean K_Means(ArrayList<Point> points, int numberOfClusters, int distanceType) throws IOException
 	{
 		FileWriter file = new FileWriter(getOperatingSystemPath());
 		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
 		
 		// Make Every Point a Cluster
         for (int i = 0; i < points.size(); i++)
         	clusters.add(new Cluster(points.get(i), "C" + (i+1)));
         
         ArrayList<Cluster> C2 = new ArrayList<Cluster>();
         ArrayList<Cluster> C2old = new ArrayList<Cluster>();
 
         for (int i = 0; i < numberOfClusters; i++)
         {
         	C2.add(new Cluster(points.get(i), "C" + (i+1)));
         	C2old.add(new Cluster(points.get(i), "C" + (i+1)));
         }
 
         while (true)
         {
         	double distance = 0.0;
 
             ArrayList<Integer> index1 = new ArrayList<Integer>();
             ArrayList<Integer> index2 = new ArrayList<Integer>();
             ArrayList<Double> mins = new ArrayList<Double>();
 
             for (int i = 0; i < clusters.size(); i++)
             {
 
                 double min = Double.MAX_VALUE;
                 int index11 = 0;
                 int index22 = 0;
                 for (int j = 0; j < C2.size(); j++)
                 {
                 	distance = getDistance(clusters.get(i).getCentoid(), clusters.get(j).getCentoid(), distanceType);
 
                     if (distance <= min)
                     {
                         min = distance;
                         index11 = i;
                         index22 = j;
                     }
                 }
                 mins.add(min);
                 index1.add(index11);
                 index2.add(index22);
             }
 
             // Print Out
             for (int i = 0; i < clusters.size(); i++)
                 file.append(" ," + clusters.get(i).getName());
 
             file.append("\n");
 
             for (int i = 0; i < C2.size(); i++)
             {
                 file.append(C2.get(i).getName() + ",");
                 for (int j = 0; j < clusters.size(); j++)      	                   
                     file.append(getDistance(C2.get(i).getCentoid(), clusters.get(j).getCentoid(), distanceType) + ",");
 
                 file.append("\n");
             }
             file.append("\n");
 
             // Wipe out all points in each cluster
             for (int i = 0; i < C2.size(); i++)
             {
                 C2old.get(i).removeAllPoints();
                 C2old.get(i).setName(C2.get(i).getName());
                 for (int k = 0; k < C2.get(i).getNumberOfPoints(); k++)
                     C2old.get(i).addPoint(C2.get(i).getPointAt(k));
 
                 C2.get(i).removeAllPoints();
                 C2.get(i).setName("");
             }
 
             // Re-Assign points to correct clusters
             for (int i = 0; i < mins.size(); i++)
             {
                 C2.get(index2.get(i)).addPoint(clusters.get(index1.get(i)).getPointAt(0));
                 C2.get(index2.get(i)).setName(C2.get(index2.get(i)).getName() + clusters.get(index1.get(i)).getName() + " | ");
             }
 
             int counter = 0;
             for (int i = 0; i < C2.size(); i++)
             {
                 if (C2old.get(i) == C2.get(i))
                     counter++;
             }
 
             // Check if we are finished
             if (counter == C2.size())
             {
                 // Print out
                 for (int i = 0; i < clusters.size(); i++)
                     file.append(" ," + clusters.get(i).getName());
 
                 file.append("\n");
 
                 for (int i = 0; i < C2.size(); i++)
                 {
                     file.append(C2.get(i).getName() + ",");
                     for (int j = 0; j < clusters.size(); j++)
                         file.append(getDistance(C2.get(i).getCentoid(), clusters.get(j).getCentoid(), distanceType) + ",");
 
                     file.append("\n");
                 }
                 file.append("\n");
 
                 for (int i = 0; i < C2.size(); i++)
                 {
                 	file.append(C2.get(i).getName());
                 	for(int j=0;j<C2.get(i).getPointAt(0).getDimensionSize();j++)
                     	file.append(",CenterD"+(i+1)+"= " + C2.get(i).getCentoid().getValueAtDimension(j));
                 	
                     file.append("\n");
                 }
 
                 file.append("\n");
                 break;
             }
         }
         
 		file.close();
 		return true;
 	}
 	
 	public static boolean Z_Score(ArrayList<Point> points) throws IOException
 	{
 		FileWriter file = new FileWriter(getOperatingSystemPath());
 		ArrayList<Double> means = new ArrayList<Double>();
 		ArrayList<Double> deviations = new ArrayList<Double>();
 
 		for(int i = 0; i < points.get(i).getDimensionSize(); i++)
 		{
 			double sum = 0.0;
 			for(int j = 0; j < points.size(); j++)
 				sum += points.get(j).getValueAtDimension(i);
 			
 			means.add(sum/points.size());
 			sum = 0.0;
 			for(int j = 0; j < points.size(); j++)
 				sum += Math.pow(points.get(j).getValueAtDimension(i) - means.get(i), 2);
 			deviations.add(Math.sqrt(sum / (points.size() - ((points.size() < ZSCORE_LARGE_VALUE)?1:0))));
 		}
 		
 		file.append("Z-Score:\n\n");
 		file.append("Original Points:\n");
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			file.append("D" + (i+1) + ",");
 		file.append("\n");
 		
 		for(int i = 0; i < points.size(); i++)
 		{
 			for(int j = 0; j < points.get(0).getDimensionSize(); j++)
 				file.append(points.get(i).getValueAtDimension(j) + ",");
 			file.append("\n");
 		}
 		
 		file.append("\nZ-Score Points:\n");
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			file.append("D" + (i+1) + ",");
 		file.append("\n");
 
 		for(int i = 0; i < points.size(); i++)
 		{
 			for(int j = 0; j < points.get(0).getDimensionSize(); j++)
 				file.append((points.get(i).getValueAtDimension(j) - means.get(j)) / deviations.get(j) + ",");
 			file.append("\n");
 		}
 
 		file.append("\nMeans:\n");
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			file.append("D" + (i+1) + ",");
 		file.append("\n");
 		
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			file.append(means.get(i) + ",");
 
 		file.append("\n\nDeviations:\n");
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			file.append("D" + (i+1) + ",");
 		file.append("\n");
 
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			file.append(deviations.get(i) + ",");
 
         file.close();
 		return true;
 	}
 }
