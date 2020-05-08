 package general;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import general.Settings;
 import vehicle.Vehicle;
 
 public class StatisticInterface
 {
 	ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
 	
 	private long startTime = System.currentTimeMillis();
 	private long endTime = System.currentTimeMillis();
 	private long runTime = 0;
 	
 	private double TotalCarsThroug = 0;
 	private double TotalWaitTime = 0;
 	
 	private double ThroughputCars = 0;
 	private double ThroughputWaitTime = 0;
 	
 	private double meanWaitTime = 0;
 	private double varianceWaitTime = 0;
 	
 
 	/***** Constructer - Singleton pattern **********/
 	private static StatisticInterface statisticClass = new StatisticInterface();
 
 	public static StatisticInterface getInstance() 
 	{
 		return statisticClass;
 	}
 	
 	private StatisticInterface() 
 	{
 		//writeStatisticToFile();
 	}
 	
 	/***** Support functions *************************/
 	public void addVehicle(Vehicle vehicle)
 	{
 		vehicles.add(vehicle);
 	}
 	
 	public void calculateAndSaveStatistics()
 	{
 		endTime = System.currentTimeMillis();
 		runTime = (endTime - startTime)/1000;
 		
 		TotalCarsThroug = vehicles.size();
 		
 		TotalWaitTime = 0;
 		for(int i = 0; i < vehicles.size(); i++)
			TotalWaitTime += vehicles.get(i).getWaitTime()/1000;
 		
 		ThroughputCars =     TotalCarsThroug / ( (double) runTime );
 		ThroughputWaitTime = TotalWaitTime / ( (double) runTime );
 		
 		meanWaitTime = TotalWaitTime / TotalCarsThroug;
 		
 		varianceWaitTime = 0;
 		for(int i = 0; i < vehicles.size(); i++)
			varianceWaitTime += Math.pow( (((double)vehicles.get(i).getWaitTime()) /1000) - meanWaitTime , 2.0);
 		varianceWaitTime *= 1/TotalCarsThroug;
 		
 		varianceWaitTime = Math.sqrt(varianceWaitTime);
 		
 		
 		writeStatisticToFile();
 	}
 
 	private void writeStatisticToFile()
 	{
 		try
 		{
 		// Create file 
 		FileWriter fstream = new FileWriter("statisticDataOut.txt");
 		BufferedWriter out = new BufferedWriter(fstream);
 		
 		//writeLineInFile(out, "Start time:     ", (int) Start time);
 		//writeLineInFile(out, "End time: ", (int) End time);
 		writeLineInFile(out, "Grid size x:       ", Settings.sizex);
 		writeLineInFile(out, "Grid size y:       ", Settings.sizey);
 
 		writeLineInFile(out, "Run time:       ", (int) runTime);
 
 		writeLineInFile(out, "Total cars through: ", TotalCarsThroug);
 		writeLineInFile(out, "Total wait time: ", TotalWaitTime);
 
 		writeLineInFile(out, "Throughput cars: ", ThroughputCars);
 		writeLineInFile(out, "Throughput wait time: ", ThroughputWaitTime);
 
 		writeLineInFile(out, "Wait time mean: ", meanWaitTime);
 		writeLineInFile(out, "Wait time variance: ", varianceWaitTime);
 
 		//Close the output stream
 		out.close();
 		}catch (Exception e){}
 	}
 	
 	private void writeLineInFile(BufferedWriter out, String s, int value) throws IOException
 	{
 		out.write(s);
 		out.write("\t");
 		out.write(Integer.toString(value));
 		out.write("\n");
 	}
 	
 	private void writeLineInFile(BufferedWriter out, String s, double value) throws IOException
 	{
 		out.write(s);
 		out.write("\t");
 		out.write(Double.toString(value));
 		out.write("\n");
 	}
 	
 	
 }	
