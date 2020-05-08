 package commons.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.math.random.RandomData;
 import org.apache.commons.math.random.RandomDataImpl;
 
 
 class TraceFilter implements FilenameFilter{
 	
 	private final String pattern;
 
 	public TraceFilter(String pattern){
 		this.pattern = pattern;
 	}
 	
 	@Override
 	public boolean accept(File dir, String name) {
 		return (name.startsWith(pattern));
 	}
 	
 }
 
 /**
  * This class is responsible for creating a users.properties file according to: number of saas clients, days to simulate and load files
  * @author David Candeia
  *
  */
 public class UserPropertiesGenerator {
 	
 	private static final int TRANSITION_PERIOD_IN_DAYS = 15;
 
	public static final long DIAMOND_STORAGE_IN_BYTES = 700;//700 MB
	public static final long GOLD_STORAGE_IN_BYTES = 500;//500 MB
	public static final long BRONZE_STORAGE_IN_BYTES = 200;//200 MB
 	
 	public static void main(String[] args) throws IOException {
 		
 		if(args.length < 6){
 			System.err.println("usage: <number of saas clients> <simulation period in days> <output file> <diamond load pool>" +
 					"<gold load pool> <bronze load pool> <peak days>");
 			System.exit(1);
 		}
 		
 		int numberOfClients = Integer.parseInt(args[0]);
 		long simulationPeriod = Long.parseLong(args[1]);
 		String outputFile = args[2];
 		
 		String diamondPool = args[3];
 		String goldPool = args[4];
 		String bronzePool = args[5];
 		
 		long[] peakDays = new long[]{-360};
 		if(args.length > 6){
 			peakDays = new long[args.length - 6];
 		}
 		int index = 0;
 		
 		for(int i = 6; i < args.length; i++){
 			peakDays[index] = Long.parseLong(args[i]);
 			index++;
 		}
 		
 		//Calculating number of users in each plan
 		RandomData random = new RandomDataImpl();
 		int numberOfBronzeClients = 0;
 		int numberOfGoldClients = 0;
 		int numberOfDiamondClients = 0;
 		
 		for(int i = 0; i < numberOfClients; i++){
 			int value = random.nextInt(0, numberOfClients);
 			if(value >= 0 && value < 0.33 * numberOfClients){//bronze client
 				numberOfBronzeClients++;
 			}else if(value >= 0.33 * numberOfClients && value < 0.66 * numberOfClients){//gold clients
 				numberOfGoldClients++;
 			}else{
 				numberOfDiamondClients++;
 			}
 		}
 		
 		Map<Integer, List<String>> workloadFilesPerUser = new HashMap<Integer, List<String>>();
 		
 		//Creating diamond trace
 		createPlanWorkload(simulationPeriod, diamondPool, peakDays, random,
 				0, numberOfDiamondClients,workloadFilesPerUser);
 		
 		//Creating gold trace
 		createPlanWorkload(simulationPeriod, goldPool, peakDays, random,
 				numberOfDiamondClients, (numberOfDiamondClients+numberOfGoldClients), workloadFilesPerUser);
 		
 		//Creating bronze trace
 		createPlanWorkload(simulationPeriod, bronzePool, peakDays, random,
 				(numberOfDiamondClients+numberOfGoldClients), (numberOfDiamondClients+numberOfGoldClients+numberOfBronzeClients),
 				workloadFilesPerUser);
 		
 		//Creating output file
 		createOutputFile(outputFile, workloadFilesPerUser);
 	}
 
 	private static void createPlanWorkload(long simulationPeriod,
 			String pool, long[] peakDays, RandomData random,
 			int initialIndex, int endIndex,
 			Map<Integer, List<String>> workloadFilesPerUser) {
 		
 		
 		String[] normalTyp = new File(pool+"/norm/").list(new TraceFilter("typ"));
 		String[] normalUnder = new File(pool+"/norm/").list(new TraceFilter("under"));
 		String[] normalPeak = new File(pool+"/norm/").list(new TraceFilter("peak"));
 		
 		String[] peakTyp = new File(pool+"/peak/").list(new TraceFilter("typ"));
 		String[] peakUnder = new File(pool+"/peak/").list(new TraceFilter("under"));
 		String[] peakPeak = new File(pool+"/peak/").list(new TraceFilter("peak"));
 		
 		String[] transTyp = new File(pool+"/trans/").list(new TraceFilter("typ"));
 		String[] transUnder = new File(pool+"/trans/").list(new TraceFilter("under"));
 		String[] transPeak = new File(pool+"/trans/").list(new TraceFilter("peak"));
 		
 		for(int user = initialIndex; user < endIndex; user++){
 			List<String> workloads = new ArrayList<String>();
 			int currentWeekDay = 0;
			int nextPeakIndex = 0;
 			
 			for(int day = 0; day < simulationPeriod; day++){
 				
 				if(currentWeekDay == 7){
 					currentWeekDay = 0;
 				}
 				
 				if(day >= peakDays[nextPeakIndex] - TRANSITION_PERIOD_IN_DAYS && day < peakDays[nextPeakIndex]){//transition workload
 					verifyDayToAdd(pool+"/trans/", random, transTyp, transUnder,
 							transPeak, workloads, currentWeekDay);
				}else if(day >= peakDays[nextPeakIndex] && day < peakDays[nextPeakIndex] + TRANSITION_PERIOD_IN_DAYS){//peak workload
 					verifyDayToAdd(pool+"/peak/", random, peakTyp, peakUnder,
 							peakPeak, workloads, currentWeekDay);
					if(day == peakDays[nextPeakIndex] + TRANSITION_PERIOD_IN_DAYS && (nextPeakIndex+1) < peakDays.length){
 						nextPeakIndex++;
 					}
 				}else{//normal workload
 					verifyDayToAdd(pool+"/norm/", random, normalTyp, normalUnder,
 							normalPeak, workloads, currentWeekDay);
 				}
 				currentWeekDay++;
 			}
 			workloadFilesPerUser.put(user, workloads);
 		}
 	}
 
 	private static void verifyDayToAdd(String pool, RandomData random,
 			String[] typPeriod, String[] underPeriod, String[] peakPeriod,
 			List<String> workloads, int currentWeekDay) {
 	
 		
 		if(currentWeekDay == 5 || currentWeekDay == 6){//under load day in week
 			int workloadFileIndex = random.nextInt(0, underPeriod.length-1);
 			workloads.add(pool+underPeriod[workloadFileIndex]);
 		}else if(currentWeekDay == 3){//peak day in week
 			int workloadFileIndex = random.nextInt(0, peakPeriod.length-1);
 			workloads.add(pool+peakPeriod[workloadFileIndex]);
 		}else{//normal day
 			int workloadFileIndex = random.nextInt(0, typPeriod.length-1);
 			workloads.add(pool+typPeriod[workloadFileIndex]);
 		}
 	}
 	
 	private static void createOutputFile(String outputFile, Map<Integer, List<String>> workloadFilesPerUser) throws IOException {
 		BufferedWriter usersPropertiesWriter = new BufferedWriter(new FileWriter(outputFile));
 		usersPropertiesWriter.write("saas.number="+workloadFilesPerUser.size()+"\n\n");
 		
 		for(Entry<Integer, List<String>> entry : workloadFilesPerUser.entrySet()){
 			
 			boolean isGold = entry.getValue().get(0).contains("gold");
 			boolean isDiamond = entry.getValue().get(0).contains("diamond");
 			
 			String plan;
 			long storage;
 			if(isGold){
 				plan = "gold";
 				storage = GOLD_STORAGE_IN_BYTES;
 			}else if(isDiamond){
 				plan = "diamond";
 				storage = DIAMOND_STORAGE_IN_BYTES;
 			}else{
 				plan = "bronze";
 				storage = BRONZE_STORAGE_IN_BYTES;
 			}
 			
 			usersPropertiesWriter.write("saas.user.id="+entry.getKey()+"\n");
 			usersPropertiesWriter.write("saas.user.plan="+plan+"\n");
 			usersPropertiesWriter.write("saas.user.storage="+storage+"\n");
 			usersPropertiesWriter.write("saas.user.workload="+entry.getKey()+".trc\n");
 			usersPropertiesWriter.write("\n");
 			
 			writeWorkloadFile(entry.getKey(), entry.getValue());
 		}
 		
 		usersPropertiesWriter.close();
 	}
 
 	private static void writeWorkloadFile(Integer key, List<String> pointers) throws IOException {
 		BufferedWriter workloadWriter = new BufferedWriter(new FileWriter(key+".trc"));
 		for(String pointer : pointers){
 			workloadWriter.write(pointer+"\n");
 		}
 		workloadWriter.close();
 	}
 
 }
