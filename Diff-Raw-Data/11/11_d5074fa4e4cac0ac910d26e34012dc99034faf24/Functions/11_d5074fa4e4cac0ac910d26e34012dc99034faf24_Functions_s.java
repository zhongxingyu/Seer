 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import javax.swing.JFrame;
 
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
 	
 	public static final int K					= 1;
 	public static final int t					= 2;
 	
 	public static final int INFINITE_LAMBDA		= 0;
 	public static final int S0					= 1;
 	public static final int MU					= 1;
 	public static final int W					= 2;
 	
 	public static String[] serviceDemandOutputStrings	= {	"Service Demand Random", "Utilization",
 															"Random Seek Time", "Service Demand Sequential",
 															"Service Demand"									};
 	
 	public static String[] infiniteQueueOutputStrings   = { "\u00B5", "P0", "P1", "P2", "Utilization (U)", 
 															"Jobs in System on Average (N)", 
 															"Throughput (X)", "Service Response Time (R)", "S0"	};
 	
 	public static String[] finiteQueueOutputStrings		= { "\u00B5", "P0", "P1", "P2", "Utilization (U)", 
 															"Jobs in System on Average (N)", 
 															"Throughput (X)", "Service Response Time (R)", "S0"	};
 	
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
 	
 	public static String diskAccessTime(ArrayList<Integer> locations, int diskAccessTime)
 	{
 		int avgDiskAccessTime = 0;
 		for(int i = 0; i < locations.size()-1; i++)
 		{
 			if((locations.get(i)+1) != locations.get(i+1))
 				avgDiskAccessTime += diskAccessTime;
 		}
 		
 		// +1 more "diskAccessTime" because the FIRST Access is ALWAYS Random!
 		return "Average Disk Access Time: " + round(((avgDiskAccessTime + diskAccessTime)/((double)locations.size()))) + " ms";
 	}
 	
 	public static boolean MST(ArrayList<Point> points, int numOfClusters, int distanceType) throws IOException
 	{
 		int dataHeight = 0;
 		int dataWidth = points.size() + 1;
 		for(int i = 0; i < (points.size() - numOfClusters + 1); i++)
 			dataHeight += points.size() - i + 2;
 		dataHeight -= 1;
 		
 		String[][] data = new String[dataHeight][dataWidth];
 		for(int i = 0; i < data.length; i++)
 		{
 			for(int j = 0; j < data[i].length; j++)
 				data[i][j] = " ";
 		}
 		
 		int dataRow = 0;
 		int dataColumn = 0;
 		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
 		
 		// Make Every Point a Cluster
         for (int i = 0; i < points.size(); i++)
             clusters.add(new Cluster(points.get(i), "C" + (i+1)));
         
         // MST Algorithm (Loop until number of clusters remaining equals desired number of clusters)
         Double distance = 0.0;
 
         while (clusters.size() > numOfClusters)
         {
         	dataColumn++;
         	for(Cluster c: clusters)
         	{
         		if(c.getName().contains("|"))
             		data[dataRow][dataColumn++] = "[" + c.getName() + "]";
             	else
             		data[dataRow][dataColumn++] = c.getName();
         	}
 
         	dataColumn = 0;
         	dataRow++;
             
             int index1 = 0;
             int index2 = 0;
             double min = Double.MAX_VALUE;
             for (int i = 0; i < clusters.size(); i++)
             {
             	if(clusters.get(i).getName().contains("|"))
             		data[dataRow][dataColumn++] = "[" + clusters.get(i).getName() + "]";
             	else
             		data[dataRow][dataColumn++] = clusters.get(i).getName();
                 for (int j = 0; j < clusters.size(); j++)
                 {
                 	distance = getDistance(clusters.get(i).getCentoid(), clusters.get(j).getCentoid(), distanceType);
 
                 	data[dataRow][dataColumn++] = Double.toString(round(distance));
                     if ((i != j) && (distance < min))
                     {
                         min = distance;
                         index1 = i;
                         index2 = j;
                     }
                 }
                 dataColumn = 0;
                 dataRow++;
             }
             dataRow++;
 
             for (int i = 0; i < clusters.get(index2).getNumberOfPoints(); i++)
                 clusters.get(index1).addPoint(clusters.get(index2).getPointAt(i));
 
             clusters.get(index1).setName(clusters.get(index1).getName() + "|" + clusters.get(index2).getName());
             clusters.remove(index2);
 
         }
 
         dataColumn++;
         for (int i = 0; i < clusters.size(); i++)
         {
         	if(clusters.get(i).getName().contains("|"))
         		data[dataRow][dataColumn++] = "[" + clusters.get(i).getName() + "]";
         	else
         		data[dataRow][dataColumn++] = clusters.get(i).getName();
         }
 
         dataColumn = 0;
         dataRow++;
 
         for (int i = 0; i < clusters.size(); i++)
         {
         	if(clusters.get(i).getName().contains("|"))
         		data[dataRow][dataColumn++] = "[" + clusters.get(i).getName() + "]";
         	else
         		data[dataRow][dataColumn++] = clusters.get(i).getName();
             for (int j = 0; j < clusters.size(); j++)
             	data[dataRow][dataColumn++] = Double.toString(round(getDistance(clusters.get(i).getCentoid(), clusters.get(j).getCentoid(), distanceType)));
 
             dataColumn = 0;
             dataRow++;
         }
 		
 		SpreadSheet ss = new SpreadSheet(data);
 		JFrame jF = new JFrame();
 		jF.add(ss);
 		jF.setSize(800, 600);
 		jF.setTitle("MST Output");
 		jF.setVisible(true);
 		
 		return true;
 	}
 	
 	public static boolean K_Means(ArrayList<Point> points, int numberOfClusters, int distanceType) throws IOException
 	{
 		ArrayList<String[]> listData = new ArrayList<String[]>();
 		String[] tempData = new String[points.size() + 1];
 		for(int i = 0; i < tempData.length; i++)
 			tempData[i] = " ";
 
 		int dataColumn = 0;
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
             dataColumn++;
             for (int i = 0; i < clusters.size(); i++)
                 tempData[dataColumn++] = clusters.get(i).getName();
 
             listData.add(tempData);
             tempData = new String[points.size() + 1];
     		dataColumn = 0;
 
             for (int i = 0; i < C2.size(); i++)
             {
             	tempData[dataColumn++] = C2.get(i).getName();
                 for (int j = 0; j < clusters.size(); j++)      	                   
                     tempData[dataColumn++] = Double.toString(round(getDistance(C2.get(i).getCentoid(), clusters.get(j).getCentoid(), distanceType)));
 
                 listData.add(tempData);
                 tempData = new String[points.size() + 1];
         		dataColumn = 0;
             }
             listData.add(tempData);
             tempData = new String[points.size() + 1];
 
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
                 if (C2.get(i).isEqual(C2old.get(i)))
                     counter++;
             }
 
             // Check if we are finished
             if (counter == C2.size())
             {
                 // Print out
                 for (int i = 0; i < clusters.size(); i++)
                     tempData[++dataColumn] = clusters.get(i).getName();
 
                 listData.add(tempData);
                 tempData = new String[points.size() + 1];
         		dataColumn = 0;
 
                 for (int i = 0; i < C2.size(); i++)
                 {
                     tempData[dataColumn++] = C2.get(i).getName();
                     for (int j = 0; j < clusters.size(); j++)
                         tempData[dataColumn++] = Double.toString(round(getDistance(C2.get(i).getCentoid(), clusters.get(j).getCentoid(), distanceType)));
 
                     listData.add(tempData);
                     tempData = new String[points.size() + 1];
             		dataColumn = 0;
                 }
                 listData.add(tempData);
                 tempData = new String[points.size() + 1];
 
                 for (int i = 0; i < C2.size(); i++)
                 {
                 	tempData[dataColumn++] = C2.get(i).getName();
                 	for(int j=0;j<C2.get(i).getPointAt(0).getDimensionSize();j++)
                     	tempData[dataColumn++] = Double.toString(round(C2.get(i).getCentoid().getValueAtDimension(j)));
                 	
                 	listData.add(tempData);
                     tempData = new String[points.size() + 1];
             		dataColumn = 0;
                 }
 
                 listData.add(tempData);
                 tempData = new String[points.size() + 1];
         		dataColumn = 0;
 
                 break;
             }
         }
         
         String[][] data = new String[listData.size()][points.size() + 1];
 		for(int i = 0; i < listData.size(); i++)
 		{
 			for(int j = 0; j < listData.get(i).length; j++)
 				data[i][j] = listData.get(i)[j];
 		}
         
 		SpreadSheet ss = new SpreadSheet(data);
 		JFrame jF = new JFrame();
 		jF.add(ss);
 		jF.setSize(800, 600);
 		jF.setTitle("K-Means Output");
 		jF.setVisible(true);
 		
 		return true;
 	}
 	
 	public static boolean Z_Score(ArrayList<Point> points) throws IOException
 	{
 		int dataHeight = 0;
 		int dataWidth = points.get(0).getDimensionSize();
 		dataHeight = 2*points.size() + 13;
 		
 		String[][] data = new String[dataHeight][dataWidth];
 		for(int i = 0; i < data.length; i++)
 		{
 			for(int j = 0; j < data[i].length; j++)
 				data[i][j] = " ";
 		}
 		
 		int dataRow = 0;
 		int dataColumn = 0;
 
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
 
 		data[dataRow++][dataColumn] = "Original Points:";
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			data[dataRow][dataColumn++] = "D" + (i+1);
 		dataRow++;
 		dataColumn = 0;
 		
 		for(int i = 0; i < points.size(); i++)
 		{
 			for(int j = 0; j < points.get(0).getDimensionSize(); j++)
 				data[dataRow][dataColumn++] = Double.toString(points.get(i).getValueAtDimension(j));
 			dataRow++;
 			dataColumn = 0;
 		}
 		
 		dataRow++;
 		data[dataRow++][dataColumn++] = "Z-Score Points:";
 		dataColumn = 0;
 
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			data[dataRow][dataColumn++] = "D" + (i+1);
 		dataRow++;
 		dataColumn = 0;
 
 		for(int i = 0; i < points.size(); i++)
 		{
 			for(int j = 0; j < points.get(0).getDimensionSize(); j++)
 				data[dataRow][dataColumn++] = Double.toString(round((points.get(i).getValueAtDimension(j) - means.get(j)) / deviations.get(j)));
 			dataRow++;
 			dataColumn = 0;
 		}
 		dataRow++;
 		data[dataRow++][dataColumn] = "Means:";
 		
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			data[dataRow][dataColumn++] = "D" + (i+1);
 		dataRow++;
 		dataColumn = 0;
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			data[dataRow][dataColumn++] = Double.toString(round(means.get(i)));
 		dataRow += 2;
 		dataColumn = 0;
 
 		data[dataRow++][dataColumn] = "Deviations:";
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			data[dataRow][dataColumn++] = "D" + (i+1);
 		dataRow++;
 		dataColumn = 0;
 
 		for(int i = 0; i < points.get(0).getDimensionSize(); i++)
 			data[dataRow][dataColumn++] = Double.toString(round(deviations.get(i)));
 
 		SpreadSheet ss = new SpreadSheet(data);
 		JFrame jF = new JFrame();
 		jF.add(ss);
 		jF.setSize(800, 600);
 		jF.setTitle("Z-Score Output");
 		jF.setVisible(true);
 		
 		return true;
 	}
 	
 	public static String poisson(ArrayList<String> parameters) throws ScriptException
 	{
 		ScriptEngineManager mgr = new ScriptEngineManager();
 		ScriptEngine engine = mgr.getEngineByName("JavaScript");
 		double lambda = (double)engine.eval(parameters.get(LAMBDA));
 		String k = parameters.get(K);
 		double time = Double.parseDouble(parameters.get(t));
 		
 		// Check if K is a Range
 		boolean isARange = k.contains("..");
 
 		if(isARange)
 		{
 			int start = Integer.parseInt(k.split("\\.\\.")[0]);
 			int end = Integer.parseInt(k.split("\\.\\.")[1]);
 
 			// Swap values if Range is backwards (E.g. 4..0)
 			if(start > end)
 			{
 				int temp = start;
 				start = end;
 				end = temp;
 			}
 
 			double sum = 0.0;
 			for(int i = start; i <= end; i++)
 				sum += (Math.exp(-lambda*time)*Math.pow(lambda*time, i)) / factorial(i);
 			
 			return "Probability: " + round(sum);
 		}
 		else
 			return "Probability: " + round((Math.exp(-lambda*time)*Math.pow(lambda*time, Integer.parseInt(k))) / factorial(Integer.parseInt(k)));
 	}
 	public static ArrayList<String> infiniteQueue(ArrayList<Double> parameters, int type)
 	{
 		ArrayList<String> output = new ArrayList<String>();
 		
 		double lambda = parameters.get(INFINITE_LAMBDA);
 		double s0 = parameters.get(S0);
 		double mu = 0.0;
 		double p0 = 0.0;
 		double p1 = 0.0;
 		double p2 = 0.0;
 		double u = 0.0;
 		double n = 0.0;
 		double x = 0.0;
 		double r = 0.0;
 		
 		if(type != MU)
 			mu = 1/s0;
 		else
 			mu = s0;
 
 		String outputLabel = (type == MU)?infiniteQueueOutputStrings[8]:infiniteQueueOutputStrings[0];
 		String outputValue = Double.toString((type == MU)?round(1/mu):round(mu));
 		output.add(outputLabel + ": " + outputValue);
 		p0 = 1-(lambda/mu);
 		output.add(infiniteQueueOutputStrings[1] + ": " + round(p0));
 		p1 = p0*(lambda/mu);
 		output.add(infiniteQueueOutputStrings[2] + ": " + round(p1));
 		p2 = p0*Math.pow(lambda/mu, 2);
 		output.add(infiniteQueueOutputStrings[3] + ": " + round(p2));
 		u = (1-p0);
 		output.add(infiniteQueueOutputStrings[4] + ": " + round(u) + " (" + round(u)*100 + "%)");
 		n = (u)/(1-(u));
 		output.add(infiniteQueueOutputStrings[5] + ": " + round(n));
 		x = lambda;
 		output.add(infiniteQueueOutputStrings[6] + ": " + round(x));
 		r = 1/(mu - lambda);
 		output.add(infiniteQueueOutputStrings[7] + ": " + round(r));		
 
 		return output;
 	}
 	public static ArrayList<String> finiteQueue(ArrayList<Double> parameters, int type)
 	{
 		ArrayList<String> output = new ArrayList<String>();
 		ArrayList<Double> pVals = new ArrayList<Double>();
 		
 		double lambda = parameters.get(INFINITE_LAMBDA);
 		double s0 = parameters.get(S0);
 		double w = parameters.get(W);
 
 		double mu = 0.0;
 		if(type != MU)
 			mu = 1/s0;
 		else
 			mu = s0;
 
 		String outputLabel = (type == MU)?finiteQueueOutputStrings[8]:finiteQueueOutputStrings[0];
 		String outputValue = Double.toString((type == MU)?round(1/mu):round(mu));
 		output.add(outputLabel + ": " + outputValue);
 
		double p0 = ((1 - lambda)/(mu))/(Math.pow(((1 - lambda)/(mu)), (w + 1)));
 		output.add(finiteQueueOutputStrings[1] + ": " + round(p0));
 
 		double sum = 0.0;
 		double currentP = p0;
 		double pNum = 1;
		while(currentP > 0.0001)
 		{
			currentP = p0*((lambda)/(mu))*(pNum++);
 			sum += currentP;
			output.add("P" + (pNum-1) + ": " + currentP);
 			pVals.add(currentP);
 		}
 
 		double u = (1-p0);
 		output.add(finiteQueueOutputStrings[4] + ": " + round(u) + " (" + round(u)*100 + "%)");
 
 		double n = 0.0;
 		for(int k = 0; k < w; k++)
 			n += k*Math.pow((lambda/mu),k);
 		output.add(finiteQueueOutputStrings[5] + ": " + round(n));
 
 		double x = u*mu;
 		output.add(finiteQueueOutputStrings[6] + ": " + round(x));
 
 		double r = n/x;
 		output.add(finiteQueueOutputStrings[7] + ": " + round(r));		
 
 		return output;
 	}	
 	public static int factorial(int n)
 	{
 		if (n == 0)
 			return 1;
 		else
 			return n * factorial(n - 1);
 	}
 }
