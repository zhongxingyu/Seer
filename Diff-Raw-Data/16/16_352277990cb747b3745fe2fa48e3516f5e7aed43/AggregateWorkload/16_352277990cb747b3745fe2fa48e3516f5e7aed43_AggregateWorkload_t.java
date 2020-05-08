 package commons.util;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.commons.configuration.ConfigurationException;
 
 import planning.heuristic.AGHeuristic;
 import planning.heuristic.OptimalHeuristic;
 import planning.heuristic.PlanningFitnessFunction;
 import planning.util.Summary;
 
 import commons.cloud.Request;
 import commons.config.Configuration;
 import commons.sim.util.SaaSUsersProperties;
 
 /**
  * This class reads a config.properties that is used in simulation and extracts user traces to be used. For each user trace
  * it calculates some statistics per fixed intervals (e.g. per hour) and creates new "user traces" to be used by {@link AGHeuristic} and
  * {@link OptimalHeuristic} during capacity planning.
  * @author David Candeia
  *
  */
 public class AggregateWorkload {
 	
 	private static final String DEFAULT_OUTPUT_FILE = "newUsers.properties";
 	private static int currentTick;
 	private static int tick;
 	private static ArrayList<Request> leftOver;
 	
 	public static void main(String[] args) throws ConfigurationException, IOException {
 		if(args.length != 1){
 			throw new RuntimeException("usage: <config file>");
 		}
 		
 		Configuration.buildInstance(args[0]);
 		Configuration config = Configuration.getInstance();
 		
 		String[] workloads = config.getWorkloads();
 		String[] plans = config.getStringArray(SaaSUsersProperties.SAAS_USER_PLAN);
 		int[] clientsID = config.getIntegerArray(SaaSUsersProperties.SAAS_USER_ID);
 		
 		tick = PlanningFitnessFunction.HOUR_IN_MILLIS;
 		
 		for(int i = 0; i < workloads.length; i++){	
 			BufferedReader pointersReader = new BufferedReader(new FileReader(workloads[i]));
 			List<Summary> currentSummaries = new ArrayList<Summary>();
 			
 			while(pointersReader.ready()){//Each saas client workload
 				String pointersFile = pointersReader.readLine();
 				BufferedReader workloadReader = new BufferedReader(new FileReader(pointersFile));
 				leftOver = new ArrayList<Request>();
 				currentTick = 0;
 				
				if(clientsID[i] == 93){
					System.out.println("cheguei!");
				}
				
 				while(workloadReader.ready()){
 					List<Request> requests = next(workloadReader, clientsID[i]);
 					extractSummary(requests, currentSummaries);
 				}
 				
 				workloadReader.close();
 			}
 			pointersReader.close();
 			
 			persistSummary(workloads[i], currentSummaries);
 		}
 		
 		persistProperties(workloads, plans);
 	}
 	
 	private static void persistProperties(String[] workloads, String[] plans) throws IOException {
 		BufferedWriter usersPropertiesWriter = new BufferedWriter(new FileWriter(DEFAULT_OUTPUT_FILE));
 		usersPropertiesWriter.write("saas.number="+workloads.length+"\n\n");
 		
 		int userID = 0;
 		
 		for(int i = 0; i < workloads.length; i++){	
 			long storage = 0;
 			if(plans[i].equalsIgnoreCase("gold")){
 				storage = UserPropertiesGenerator.GOLD_STORAGE_IN_BYTES;
 			}else if(plans[i].equalsIgnoreCase("diamond")){
 				storage = UserPropertiesGenerator.DIAMOND_STORAGE_IN_BYTES;
 			}else{
 				storage = UserPropertiesGenerator.BRONZE_STORAGE_IN_BYTES;
 			}
 			
 			usersPropertiesWriter.write("saas.user.id="+(userID++)+"\n");
 			usersPropertiesWriter.write("saas.user.plan="+plans[i]+"\n");
 			usersPropertiesWriter.write("saas.user.storage="+storage+"\n");
 			usersPropertiesWriter.write("saas.user.workload=new"+workloads[i]+"\n");
 			usersPropertiesWriter.write("\n");
 		}
 		
 		usersPropertiesWriter.close();
 	}
 	
 	/**
 	 * This method creates an output file containing statistics of workload instead of a file of pointers to real traces.
 	 * @param workload The name of the file containing the pointers to real traces
 	 * @param summaries Workload statistics
 	 * @param currentPlan 
 	 * @throws IOException
 	 */
 	private static void persistSummary(String workload, List<Summary> summaries) throws IOException {
 		BufferedWriter fileWriter = new BufferedWriter(new FileWriter("new"+workload));
 		for(Summary summary : summaries){
 			fileWriter.write("arrival.rate="+summary.getArrivalRate()+"\n");
 			fileWriter.write("cpu.demand="+summary.getTotalCpuHrs()+"\n");
 			fileWriter.write("service.demand="+summary.getRequestServiceDemandInMillis()+"\n");
 			fileWriter.write("user.think="+summary.getUserThinkTimeInSeconds()+"\n");
 			fileWriter.write("users.number="+summary.getNumberOfUsers()+"\n\n");
 		}
 		fileWriter.close();
 	}
 	
 	/**
 	 * This method collects statistics for a set of requests.
 	 * @param requests
 	 * @param data
 	 */
 	private static void extractSummary(List<Request> requests, List<Summary> data) {
 		double totalServiceTime = 0d;
 		for(Request request : requests){
 			totalServiceTime += request.getTotalMeanToProcess();
 		}
 		double arrivalRate = requests.size() / (60.0 * 60.0);//arrival per second
 		double totalCpuHrs = totalServiceTime / PlanningFitnessFunction.HOUR_IN_MILLIS;
		
		int size = (requests.isEmpty()) ? 1 : requests.size();
		double serviceDemandInMillis = totalServiceTime / size;

 		double userThinkTimeInSeconds = 5;
 		long numberOfUsers = Math.round(userThinkTimeInSeconds * arrivalRate);
 		
 		Summary summary = new Summary(arrivalRate , totalCpuHrs, serviceDemandInMillis, userThinkTimeInSeconds, numberOfUsers);
 		data.add(summary);
 	}
 
 	public static List<Request> next(BufferedReader workloadReader, int clientID) throws IOException{
 		List<Request> requests = new ArrayList<Request>(leftOver);
 		
 		long time = (currentTick + 1) * tick;
 		leftOver.clear();
 		for (Request request : requests) {
 			if(request.getArrivalTimeInMillis() >= time){
 				leftOver.add(request);
 			}
 		}
 		requests.removeAll(leftOver);
 		
 		while(workloadReader.ready()){
 			Request request = parseRequest(workloadReader.readLine(), clientID);
 			if(request.getArrivalTimeInMillis() < time){
 				requests.add(request);
 			}else{
 				leftOver.add(request);
 				break;
 			}
 		}
 		
 		currentTick++;
 		return requests;
 	}
 	
 	protected static Request parseRequest(String line, int saasClientID) {
 		StringTokenizer tokenizer = new StringTokenizer(line, "( +|\t+)+");
 
 		int userID = Integer.parseInt(tokenizer.nextToken());
 		long reqID = Long.parseLong(tokenizer.nextToken());
 		long arrivalTimeInMillis = Long.parseLong(tokenizer.nextToken());
 		long requestSizeInBytes = Long.parseLong(tokenizer.nextToken());
 		long responseSizeInBytes = Long.parseLong(tokenizer.nextToken());
 		
 		long [] demand = new long[tokenizer.countTokens()];
 		int index = 0;
 		while(tokenizer.hasMoreTokens()){
			demand[index++] = (long)Double.parseDouble(tokenizer.nextToken());
 		}
 		
 		return new Request(reqID, saasClientID, userID, arrivalTimeInMillis,
 				requestSizeInBytes, responseSizeInBytes, demand);
 	}
 	
 
 }
