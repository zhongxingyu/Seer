 package commons.cloud;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br
  */
 public class UtilityResultEntry implements Comparable<UtilityResultEntry>{
 	
 	private static class UserEntry{
 
 		private String contractName;
 		private double totalReceipt;
 		private long consumedCPU;
 		private double cpuCost;
 		private long consumedTransference;
 		private double transferenceCost;
 		private double storageCost;
 
 		/**
 		 * @param contractName
 		 * @param total 
 		 * @param storageCost 
 		 * @param transferenceCost 
 		 * @param consumedTransference 
 		 * @param cpuCost 
 		 * @param result
 		 */
 		public void add(String contractName, long consumedCPU, double cpuCost, long consumedTransference, double transferenceCost, double storageCost, double total) {
 			this.contractName = contractName;
 			this.consumedCPU = consumedCPU;
 			this.cpuCost = cpuCost;
 			this.consumedTransference = consumedTransference;
 			this.transferenceCost = transferenceCost;
 			this.storageCost = storageCost;
 			this.totalReceipt = total;
 		}
 		
 	}
 	
 	private static class TypeEntry{
 		
 		private final MachineType type;
 		private final long onDemandCPUHours;
 		private final double onDemandCost;
 		private final long reservedCPUHours;
 		private final double reservedCost;
 
 		/**
 		 * Default constructor.
 		 * @param type2
 		 * @param onDemandCPUHours
 		 * @param onDemandCost
 		 * @param reservedCPUHours
 		 * @param reservedCost
 		 */
 		public TypeEntry(MachineType type, long onDemandCPUHours,
 				double onDemandCost, long reservedCPUHours, double reservedCost) {
 			this.type = type;
 			this.onDemandCPUHours = onDemandCPUHours;
 			this.onDemandCost = onDemandCost;
 			this.reservedCPUHours = reservedCPUHours;
 			this.reservedCost = reservedCost;
 		}
 	}
 	
 	private static class ProviderEntry{
 		
 		private final String name;
 		private double cost;
 		private double inCost;
 		private double outCost;
 		private double onDemandCost;
 		private double reservedCost;
 		
 		private long inTransference;
 		private long outTransference;
 		private long onDemandCPUHours;
 		private long reservedCPUHours;
 		
 		private Map<MachineType, TypeEntry> types;
 		private double monitoringCost;
 		
 		/**
 		 * Default constructor.
 		 */
 		public ProviderEntry(String name) {
 			this.name = name;
 			this.cost = 0;
 			this.inCost = 0;
 			this.outCost = 0;
 			this.onDemandCost = 0;
 			this.reservedCost = 0;
 			this.inTransference = 0;
 			this.outTransference = 0;
 			this.onDemandCPUHours = 0;
 			this.reservedCPUHours = 0;
 			this.monitoringCost = 0;
 			this.types = new HashMap<MachineType, TypeEntry>();
 		}
 
  		/**
 		 * @param inTransference
 		 * @param inCost
 		 * @param outTransference
 		 * @param outCost
 		 */
 		public void addTransference(long inTransference, double inCost,
 				long outTransference, double outCost) {
 			this.inTransference = inTransference;
 			this.inCost = inCost;
 			this.outTransference = outTransference;
 			this.outCost = outCost;
 			this.cost += (inCost + outCost);
 		}
 
 		/**
 		 * @param type 
 		 * @param onDemandCPUHours
 		 * @param onDemandCost
 		 * @param reservedCPUHours
 		 * @param reservedCost
 		 * @param monitoringCost 
 		 */
 		public void addUsage(MachineType type, long onDemandCPUHours, double onDemandCost,
 				long reservedCPUHours, double reservedCost, double monitoringCost) {
 			this.onDemandCPUHours += onDemandCPUHours;
 			this.onDemandCost += onDemandCost;
 			this.reservedCPUHours += reservedCPUHours;
 			this.reservedCost += reservedCost;
 			this.monitoringCost += monitoringCost;
 			this.cost += (onDemandCost + reservedCost);
 			this.types.put(type, new TypeEntry(type, onDemandCPUHours, onDemandCost,
 				reservedCPUHours, reservedCost));
 		}
 		
 	}
 
 	private final long time;
 	private double receipt;
 	private double cost;
 	private double penalty;
 	
 	private int currentUser;
 	private Map<Integer, UserEntry> users;
 	
 	private String currentProvider;
 	private Map<String, ProviderEntry> providers;
 	
 	/**
 	 * Default constructor
 	 * @param time 
 	 */
 	public UtilityResultEntry(long time) {
 		this.time = time;
 		this.receipt = 0;
 		this.cost = 0;
 		this.penalty = 0;
 		this.users = new HashMap<Integer, UserEntry>();
 		this.providers = new HashMap<String, ProviderEntry>();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	
 	@Override
 	public int compareTo(UtilityResultEntry o) {
 		return this.time < o.time? -1: 1;
 	}
 
 	/**
 	 * @param userID
 	 */
 	public void addUser(int userID) {
 		currentUser = userID;
 		users.put(userID, new UserEntry());
 	}
 
 	/**
 	 * @param contractName
 	 * @param total
 	 * @param transferenceCost 
 	 * @param consumedTransference 
 	 * @param storageCost 
 	 * @param costOfCPU 
 	 */
 	public void addToReceipt(String contractName, long consumedCPU, double cpuCost, long consumedTransference, double transferenceCost, double storageCost) {
 		double total = cpuCost + transferenceCost + storageCost;
 		users.get(currentUser).add(contractName, consumedCPU, cpuCost, consumedTransference, transferenceCost, storageCost, total);
 		receipt += total;
 	}
 
 	/**
 	 * @param providerName
 	 */
 	public void addProvider(String providerName) {
 		currentProvider = providerName;
 		providers.put(providerName, new ProviderEntry(providerName));
 	}
 
 	public void addTransferenceToCost(long inTransference, double inCost, long outTransference, double outCost) {
 		providers.get(currentProvider).addTransference(inTransference, inCost, outTransference, outCost);
 		this.cost += (inCost + outCost);
 	}
 	
 	public void addUsageToCost(MachineType type, long onDemandCPUHours, double onDemandCost, long reservedCPUHours, double reservedCost, double monitoringCost) {
 		providers.get(currentProvider).addUsage(type, onDemandCPUHours, onDemandCost, reservedCPUHours, reservedCost, monitoringCost);
 		this.cost += (onDemandCost + reservedCost);
 	}
 
 	/**
 	 * @return The utility value of this entry.
 	 */
 	public double getUtility() {
 		return receipt - cost - penalty;
 	}
 
 	/**
 	 * @return the time
 	 */
 	public long getTime() {
 		return time;
 	}
 
 	/**
 	 * @return the receipt
 	 */
 	public double getReceipt() {
 		return receipt;
 	}
 
 	/**
 	 * @return the cost
 	 */
 	public double getCost() {
 		return cost;
 	}
 
 	/**
 	 * @return the penalty
 	 */
 	public double getPenalty() {
 		return penalty;
 	}
 }
