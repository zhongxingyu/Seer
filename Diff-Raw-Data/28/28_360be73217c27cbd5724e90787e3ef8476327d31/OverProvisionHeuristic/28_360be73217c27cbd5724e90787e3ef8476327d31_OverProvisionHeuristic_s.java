 package planning.heuristic;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import commons.cloud.MachineType;
 import commons.cloud.Provider;
 import commons.cloud.Request;
 import commons.cloud.User;
 import commons.config.Configuration;
 import commons.io.GEISTWorkloadParser;
 import commons.io.HistoryBasedWorkloadParser;
 import commons.io.TimeBasedWorkloadParser;
 import commons.sim.util.SaaSAppProperties;
 import commons.sim.util.SimulatorProperties;
 
 public class OverProvisionHeuristic implements PlanningHeuristic{
 
 	private int maximumNumberOfServers;
 	
 	public OverProvisionHeuristic(){
 		this.maximumNumberOfServers = 0;
 	}
 
 	@Override
 	public void findPlan(HistoryBasedWorkloadParser workloadParser,
 			List<Provider> cloudProviders, List<User> cloudUsers) {
 		
 		TimeBasedWorkloadParser currentParser = new TimeBasedWorkloadParser(new GEISTWorkloadParser(Configuration.getInstance().getWorkloads()), Configuration.getInstance().getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME));
 		maximumNumberOfServers = 0;
 		
 		while(currentParser.hasNext()){
 			try {
 				List<Request> requests = currentParser.next();
 				Set<String> simultaneousUsers = new HashSet<String>();
 				
 				for(Request request : requests){
 					simultaneousUsers.add(request.getUserID());
 				}
 				
 				if(simultaneousUsers.size() > maximumNumberOfServers){
 					maximumNumberOfServers = simultaneousUsers.size();
 				}
 			} catch (IOException e) {
 				throw new RuntimeException(e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public double getEstimatedProfit(int period) {
 		return 0;
 	}
 
 	@Override
 	public Map<MachineType, Integer> getPlan(List<User> cloudUsers) {
 		Map<MachineType, Integer> plan = new HashMap<MachineType, Integer>();
 		MachineType machineType = MachineType.valueOf(Configuration.getInstance().getString(SimulatorProperties.PLANNING_TYPE).toUpperCase());
		plan.put(machineType, maximumNumberOfServers);
 		return plan;
 	}
 }
