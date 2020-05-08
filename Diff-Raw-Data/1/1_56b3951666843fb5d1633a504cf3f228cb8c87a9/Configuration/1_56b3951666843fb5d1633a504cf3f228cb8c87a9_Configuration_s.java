 package commons.config;
 
 import static commons.sim.util.IaaSPlanProperties.*;
 import static commons.sim.util.IaaSProvidersProperties.*;
 import static commons.sim.util.SaaSAppProperties.*;
 import static commons.sim.util.SaaSPlanProperties.*;
 import static commons.sim.util.SaaSUsersProperties.*;
 import static commons.sim.util.SimulatorProperties.*;
 
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.ConfigurationRuntimeException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 import planning.heuristic.AGHeuristic;
 import provisioning.DynamicProvisioningSystem;
 import provisioning.ProfitDrivenProvisioningSystem;
 import provisioning.RanjanProvisioningSystem;
 import provisioning.RanjanProvisioningSystemForHeterogeneousMachines;
 
 import commons.cloud.Contract;
 import commons.cloud.MachineType;
 import commons.cloud.Provider;
 import commons.cloud.TypeProvider;
 import commons.cloud.User;
 import commons.sim.schedulingheuristics.ProfitDrivenHeuristic;
 import commons.sim.schedulingheuristics.RanjanHeuristic;
 import commons.sim.schedulingheuristics.RoundRobinHeuristic;
 import commons.sim.schedulingheuristics.RoundRobinHeuristicForHeterogenousMachines;
 import commons.sim.util.SaaSUsersProperties;
 
 
 /**
  * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br
  *
  */
 public class Configuration	extends PropertiesConfiguration{
 	
 	public static final String ARRAY_SEPARATOR = "\\|";
 
 	/**
 	 * Unique instance.
 	 */
 	private static Configuration instance;
 	
 	private List<Provider> providers;
 	
 	private List<User> users;
 
 	private HashMap<MachineType, Double> relativePower;
 	
 	/**
 	 * Builds the single instance of this configuration.
 	 * @param propertiesFileName
 	 * @throws ConfigurationException
 	 */
 	public static void buildInstance(String propertiesFileName) throws ConfigurationException{
 		instance = new Configuration(propertiesFileName);
 	}
 
 	/**
 	 * Returns the single instance of this configuration.
 	 * @return
 	 */
 	public static Configuration getInstance(){
 		if(instance == null){
 			throw new ConfigurationRuntimeException();
 		}
 		return instance;
 	}
 	
 	public int[] getIntegerArray(String propertyName) {
 		String[] stringArray = getStringArray(propertyName);
 		int [] values = new int[stringArray.length];
 		for (int i = 0; i < values.length; i++) {
 			values[i] = Integer.valueOf(stringArray[i]);
 		}
 		return values;
 	}
 
 	public long[] getLongArray(String propertyName) {
 		return parseLongArray(getStringArray(propertyName));
 	}
 
 	public long[][] getLong2DArray(String propertyName) {
 		String[] stringArray = getStringArray(propertyName);
 		long [][] values = new long[stringArray.length][];
 		for (int i = 0; i < values.length; i++) {
 			values[i] = parseLongArray(stringArray[i].split(ARRAY_SEPARATOR));
 		}
 		return values;
 	}
 
 	public double[] getDoubleArray(String propertyName) {
 		return parseDoubleArray(getStringArray(propertyName));
 	}
 
 	public double[][] getDouble2DArray(String propertyName) {
 		String[] stringArray = getStringArray(propertyName);
 		double [][] values = new double[stringArray.length][];
 		for (int i = 0; i < values.length; i++) {
 			values[i] = parseDoubleArray(stringArray[i].split(ARRAY_SEPARATOR));
 		}
 		return values;
 	}
 
 	private static double[] parseDoubleArray(String[] stringValues) {
 		double [] doubleValues = new double[stringValues.length];
 		for (int j = 0; j < doubleValues.length; j++) {
 			doubleValues[j] = Double.valueOf(stringValues[j]);
 		}
 		return doubleValues;
 	}
 
 	private static long[] parseLongArray(String[] stringValues) {
 		long [] doubleValues = new long[stringValues.length];
 		for (int j = 0; j < doubleValues.length; j++) {
 			doubleValues[j] = Long.valueOf(stringValues[j]);
 		}
 		return doubleValues;
 	}
 
 	public <T extends Enum<T>> T[] getEnumArray(String propertyName, Class<T> enumClass) {
 		return parseEnum(getStringArray(propertyName), enumClass);
 	}
 	
 	public String[][] getString2DArray(String propertyName) {
 		String[] stringArray = getStringArray(propertyName);
 		String[][] values = new String[stringArray.length][];
 		for (int i = 0; i < values.length; i++) {
 			values[i] = stringArray[i].split(ARRAY_SEPARATOR);
 		}
 		return values;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static <T extends Enum<T>> T[] parseEnum(String[] stringValues, Class<T> enumClass) {
 		T [] enumValues = (T[]) Array.newInstance(enumClass, stringValues.length);
 		for (int j = 0; j < enumValues.length; j++) {
 			enumValues[j] = Enum.valueOf(enumClass, stringValues[j].trim().toUpperCase());
 		}
 		return enumValues;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T extends Enum<T>> T[][] getEnum2DArray(String propertyName, Class<T> enumClass) {
 		String[] machines = getStringArray(propertyName);
 		T[][] machineTypes = (T[][]) Array.newInstance(enumClass, machines.length, machines.length);
 		for(int i = 0; i < machines.length; i++){
 			machineTypes[i] = parseEnum(machines[i].split(ARRAY_SEPARATOR), enumClass);
 		}
 		return machineTypes;
 	}
 
 	/**
 	 * @return
 	 */
 	public Class<?>[] getApplicationHeuristics() {
 		String[] strings = getStringArray(APPLICATION_HEURISTIC);
 		Class<?> [] heuristicClasses = new Class<?>[strings.length]; 
 		
 		for (int i = 0; i < strings.length; i++) {
 			try {
 				heuristicClasses[i] = Class.forName(strings[i]);
 			} catch (ClassNotFoundException e) {
 				throw new ConfigurationRuntimeException("Problem loading " + strings[i], e);
 			}
 		}
 		return heuristicClasses;
 	}
 
 	public Class<?> getDPSHeuristicClass() {
 		String heuristicName = getString(DPS_HEURISTIC);
 		try {
 			return Class.forName(heuristicName);
 		} catch (ClassNotFoundException e) {
 			throw new ConfigurationRuntimeException("Problem loading " + heuristicName, e);
 		}
 	}
 
 	/**
 	 * This method is responsible for reading providers properties and creating the
 	 * cloud providers to be used in simulation.
 	 * @return
 	 * @throws IOException
 	 */
 	public List<Provider> getProviders() {
 		return this.providers;
 	}
 
 	public Class<?> getPlanningHeuristicClass(){
 		String heuristicName = getString(PLANNING_HEURISTIC);
 		try {
 			return Class.forName(heuristicName);
 		} catch (ClassNotFoundException e) {
 			throw new ConfigurationRuntimeException("Problem loading " + heuristicName, e);
 		}
 	}
 
 	/**
 	 * This method is responsible for reading contracts properties and creating the
 	 * associations between contracts and users that requested the services of each contract
 	 * @return A map containing each contract name and its characterization
 	 * @throws IOException
 	 */
 	public List<User> getUsers() {
 		return this.users;
 	}
 
 	/**
 	 * Private constructor.
 	 * 
 	 * @param propertiesFileName
 	 * @throws ConfigurationException
 	 */
 	private Configuration(String propertiesFileName) throws ConfigurationException {
 		super(propertiesFileName);
 		verifyProperties();
 		parseProperties();
 	}
 	
 	private void verifyProperties() throws ConfigurationException{
 		verifySimulatorProperties();
 		verifySaaSAppProperties();
 		verifySaaSUsersProperties();
 		verifySaaSPlansProperties();
 		verifyIaaSProvidersProperties();
 		verifyIaaSPlanProperties();
 	}
 	
 	private void parseProperties() throws ConfigurationException {
 		readUsers();
 		readProviders();
 	}
 	
 
 	private void readUsers() throws ConfigurationException {
 		
 		int numberOfPlans = getInt(NUMBER_OF_PLANS);
 		String[] planNames = getStringArray(PLAN_NAME);
 		int[] planPriorities = getIntegerArray(PLAN_PRIORITY);
 		double[] prices = getDoubleArray(PLAN_PRICE);
 		double[] setupCosts = getDoubleArray(PLAN_SETUP);
 		long[] cpuLimits = getLongArray(PLAN_CPU_LIMIT);
 		double[] extraCpuCosts = getDoubleArray(PLAN_EXTRA_CPU_COST);
 		long[][] planTransferLimits = getLong2DArray(PLAN_TRANSFER_LIMIT);
 		double[][] planExtraTransferCost = getDouble2DArray(PLAN_EXTRA_TRANSFER_COST);
 		long[] planStorageLimits = getLongArray(PLAN_STORAGE_LIMIT);
 		double[] planExtraStorageCosts = getDoubleArray(PLAN_EXTRA_STORAGE_COST);
 		
 		Map<String, Contract> contractsPerName = new HashMap<String, Contract>();
 		for(int i = 0; i < numberOfPlans; i++){
 			if(planTransferLimits[i].length != planExtraTransferCost[i].length - 1){
 				throw new ConfigurationException("Check values of " + PLAN_TRANSFER_LIMIT + " and " + PLAN_EXTRA_TRANSFER_COST);
 			}
 			contractsPerName.put(planNames[i], 
 					new Contract(planNames[i], planPriorities[i], setupCosts[i], prices[i], 
 							cpuLimits[i], extraCpuCosts[i], planTransferLimits[i], planExtraTransferCost[i],
 							planStorageLimits[i], planExtraStorageCosts[i]));
 		}
 		
 		String[] plans = getStringArray(SAAS_USER_PLAN);
 		long[] storage = getLongArray(SAAS_USER_STORAGE);
 		
 		users = new ArrayList<User>();
 		for (int i = 0; i < plans.length; i++) {
 			if(!contractsPerName.containsKey(plans[i])){
 				throw new ConfigurationException("Cannot find configuration for plan " + plans[i] + ". Check contracts file.");
 			}
 			
 			users.add(new User(contractsPerName.get(plans[i]), storage[i]));
 		}
 	}
 
 	private void readProviders() throws ConfigurationException {
 		int numberOfProviders = getInt(IAAS_NUMBER_OF_PROVIDERS);
 		
 		relativePower = new HashMap<MachineType, Double>();
 		MachineType[] allTypes = getEnumArray(IAAS_TYPES, MachineType.class);
 		double[] power = getDoubleArray(IAAS_POWER);
 		for (int i = 0; i < allTypes.length; i++) {
 			relativePower.put(allTypes[i], power[i]);
 		}
 	
 		String[] names = getStringArray(IAAS_PROVIDER_NAME);
 		int[] onDemandLimits = getIntegerArray(IAAS_PROVIDER_ONDEMAND_LIMIT);
 		int[] reservedLimits = getIntegerArray(IAAS_PROVIDER_RESERVED_LIMIT);
 		double[] monitoringCosts = getDoubleArray(IAAS_PROVIDER_MONITORING);
 		long[][] transferInLimits = getLong2DArray(IAAS_PROVIDER_TRANSFER_IN);
 		double[][] transferInCosts = getDouble2DArray(IAAS_PROVIDER_COST_TRANSFER_IN);
 		long[][] transferOutLimits = getLong2DArray(IAAS_PROVIDER_TRANSFER_OUT);
 		double[][] transferOutCosts = getDouble2DArray(IAAS_PROVIDER_COST_TRANSFER_OUT);
 		
 		MachineType[][] machinesType = getEnum2DArray(IAAS_PROVIDER_TYPES, MachineType.class);
 		double[][] onDemandCpuCosts = getDouble2DArray(IAAS_PROVIDER_ONDEMAND_CPU_COST);
 		double[][] reservedCpuCosts = getDouble2DArray(IAAS_PROVIDER_RESERVED_CPU_COST);
 		double[][] reservationOneYearFees = getDouble2DArray(IAAS_PROVIDER_ONE_YEAR_FEE);
 		double[][] reservationThreeYearsFees = getDouble2DArray(IAAS_PROVIDER_THREE_YEARS_FEE);
 	
 		List<String> providersWithPlan = Arrays.asList(getStringArray(IAAS_PLAN_PROVIDER_NAME));
 		MachineType[][] machines = getEnum2DArray(IAAS_PLAN_PROVIDER_TYPES, MachineType.class);
 		long[][] reservations = getLong2DArray(IAAS_PLAN_PROVIDER_RESERVATION);
 		
 		providers = new ArrayList<Provider>();
 		
 		for(int i = 0; i < numberOfProviders; i++){
 			
 			List<TypeProvider> types = new ArrayList<TypeProvider>();
 			
 			int providerIndex = providersWithPlan.indexOf(names[i]);
 			List<MachineType> typeList = null;
 			
 			if(providerIndex != -1){
 				typeList = Arrays.asList(machines[providerIndex]);
 			}
 			
 			if(machinesType[i].length != onDemandCpuCosts[i].length){
 				throw new ConfigurationException("Check values of " + IAAS_PROVIDER_TYPES + " and " + IAAS_PROVIDER_ONDEMAND_CPU_COST);
 			}
 
 			if(machinesType[i].length != reservedCpuCosts[i].length){
 				throw new ConfigurationException("Check values of " + IAAS_PROVIDER_TYPES + " and " + IAAS_PROVIDER_RESERVED_CPU_COST);
 			}
 
 			if(machinesType[i].length != reservationOneYearFees[i].length){
 				throw new ConfigurationException("Check values of " + IAAS_PROVIDER_TYPES + " and " + IAAS_PROVIDER_ONE_YEAR_FEE);
 			}
 
 			if(machinesType[i].length != reservationThreeYearsFees[i].length){
 				throw new ConfigurationException("Check values of " + IAAS_PROVIDER_TYPES + " and " + IAAS_PROVIDER_THREE_YEARS_FEE);
 			}
 
 			for (int j = 0; j < machinesType[i].length; j++) {
 				long reservation = 0;
 				if(typeList != null){
 					int index = typeList.indexOf(machinesType[i][j]);
 					reservation = (index == -1)? 0: reservations[providerIndex][index];
 				}
 				types.add(new TypeProvider(machinesType[i][j], onDemandCpuCosts[i][j], reservedCpuCosts[i][j], 
 						reservationOneYearFees[i][j], reservationThreeYearsFees[i][j], reservation));
 			}
 			
 			if(transferInLimits[i].length != transferInCosts[i].length - 1){
 				throw new ConfigurationException("Check values of " + IAAS_PROVIDER_TRANSFER_IN + " and " + IAAS_PROVIDER_COST_TRANSFER_IN);
 			}
 
 			if(transferOutLimits[i].length != transferOutCosts[i].length - 1){
 				throw new ConfigurationException("Check values of " + IAAS_PROVIDER_TRANSFER_OUT + " and " + IAAS_PROVIDER_COST_TRANSFER_OUT);
 			}
 			
 			providers.add(new Provider(names[i], onDemandLimits[i], reservedLimits[i],
 							monitoringCosts[i], transferInLimits[i], transferInCosts[i], 
 							transferOutLimits[i], transferOutCosts[i], types));
 		}
 		
 		
 	}
 	
 	public double getRelativePower(MachineType type){
 		return this.relativePower.get(type);
 	}
 
 	// ************************************* SIMULATOR ************************************/
 	
 	private void verifySimulatorProperties() throws ConfigurationException {
 		checkDPSHeuristic();
 		checkPlanningHeuristic();
 		Validator.checkPositive(PLANNING_PERIOD, getString(PLANNING_PERIOD));
 	}
 	
 	private void checkDPSHeuristic() throws ConfigurationException {
 		
 		String heuristicName = getString(DPS_HEURISTIC);
 		Validator.checkNotEmpty(DPS_HEURISTIC, getString(DPS_HEURISTIC));
 		
 		String customHeuristicClass = getString(DPS_CUSTOM_HEURISTIC);
 		try{
 			DPSHeuristicValues value = DPSHeuristicValues.valueOf(heuristicName);
 			switch (value) {
 				case STATIC:
 					heuristicName = DynamicProvisioningSystem.class.getCanonicalName();
 					break;
 				case RANJAN:
 					heuristicName = RanjanProvisioningSystem.class.getCanonicalName();
 					checkRanjanProperties();
 					break;
 				case RANJAN_HET:
 					heuristicName = RanjanProvisioningSystemForHeterogeneousMachines.class.getCanonicalName();
 					checkRanjanProperties();
 					break;
 				case PROFITDRIVEN:
 					heuristicName = ProfitDrivenProvisioningSystem.class.getCanonicalName();
 					break;
 				case CUSTOM:
 					heuristicName = Class.forName(customHeuristicClass).getCanonicalName();
 					break;
 				default:
 					throw new ConfigurationException("Unsupported value: " + value + " for DPSHeuristicValues.");
 			}
 			setProperty(DPS_HEURISTIC, heuristicName);
 		} catch (ClassNotFoundException e) {
 			throw new ConfigurationException("Problem loading " + customHeuristicClass, e);
 		}
 	}
 
 	private void checkPlanningHeuristic() throws ConfigurationException {
 		String heuristicName = getString(PLANNING_HEURISTIC);
 		Validator.checkNotEmpty(PLANNING_HEURISTIC, getString(PLANNING_HEURISTIC));
 		
 		PlanningHeuristicValues value = PlanningHeuristicValues.valueOf(heuristicName.toUpperCase());
 		switch (value) {
 			case EVOLUTIONARY:
 				heuristicName = AGHeuristic.class.getCanonicalName();
 				break;
 			default:
 				throw new ConfigurationException("Unsupported value: " + value + " for PlanningHeuristicValues.");
 		}
 		setProperty(PLANNING_HEURISTIC, heuristicName);
 	}
 
 
 	private void checkRanjanProperties() throws ConfigurationException {
 		Validator.checkPositive(RANJAN_HEURISTIC_NUMBER_OF_TOKENS, getString(RANJAN_HEURISTIC_NUMBER_OF_TOKENS));
 		Validator.checkNonNegative(RANJAN_HEURISTIC_BACKLOG_SIZE, getString(RANJAN_HEURISTIC_BACKLOG_SIZE));
 	}
 
 	// ************************************* SaaS APP ************************************/
 	
 	private void verifySaaSAppProperties() throws ConfigurationException {
 
 		Validator.checkNotEmpty(APPLICATION_FACTORY, getString(APPLICATION_FACTORY));
 		Validator.checkPositive(APPLICATION_NUM_OF_TIERS, getString(APPLICATION_NUM_OF_TIERS));
 		Validator.checkNonNegative(APPLICATION_SETUP_TIME, getString(APPLICATION_SETUP_TIME));
 		
 		checkSize(APPLICATION_HEURISTIC, APPLICATION_NUM_OF_TIERS);
 		checkSize(APPLICATION_INITIAL_SERVER_PER_TIER, APPLICATION_NUM_OF_TIERS);
 		
 		Validator.checkIsPositiveArray(APPLICATION_INITIAL_SERVER_PER_TIER, getStringArray(APPLICATION_INITIAL_SERVER_PER_TIER));
 
 //		checkSize(APPLICATION_MAX_SERVER_PER_TIER, APPLICATION_NUM_OF_TIERS);
 //		Validator.checkIsPositiveIntegerArray(getStringArray(APPLICATION_MAX_SERVER_PER_TIER));
 		
 		checkSchedulingHeuristicNames();
 		
 	}
 	
 	private void checkSchedulingHeuristicNames() throws ConfigurationException {
 		String[] strings = getStringArray(APPLICATION_HEURISTIC);
 		String customHeuristic = getString(APPLICATION_CUSTOM_HEURISTIC);
 		for (int i = 0; i < strings.length; i++) {
 			AppHeuristicValues value = AppHeuristicValues.valueOf(strings[i]);
 			switch (value) {
 				case ROUNDROBIN:
 					strings[i] = RoundRobinHeuristic.class.getCanonicalName();
 					break;
 				case ROUNDROBIN_HET:
 					strings[i] = RoundRobinHeuristicForHeterogenousMachines.class.getCanonicalName();
 				case RANJAN:
 					strings[i] = RanjanHeuristic.class.getCanonicalName();
 					break;
 				case PROFITDRIVEN:
 					strings[i] = ProfitDrivenHeuristic.class.getCanonicalName();
 					break;
 				case CUSTOM:
 					try {
 						strings[i] = Class.forName(customHeuristic).getCanonicalName();
 					} catch (ClassNotFoundException e) {
 						throw new ConfigurationException("Problem loading " + customHeuristic, e);
 					}
 					break;
 				default:
 					throw new ConfigurationException("Unsupported value for " + APPLICATION_HEURISTIC + ": " + strings[i]);
 			}
 		}
 		
 		setProperty(APPLICATION_HEURISTIC, strings);
 	}
 
 	// ************************************* SaaS Users ************************************/
 	
 	private void verifySaaSUsersProperties() throws ConfigurationException {
 		
 		checkSize(SAAS_USER_STORAGE, SAAS_NUMBER_OF_USERS);
 		
 		Validator.checkPositive(SAAS_NUMBER_OF_USERS, getString(SAAS_NUMBER_OF_USERS));
 		Validator.checkIsNonNegativeArray(SAAS_USER_STORAGE, getStringArray(SAAS_USER_STORAGE));
 		
 		String workload = getString(SAAS_WORKLOAD);
 		if(workload == null || workload.isEmpty()){
 			checkSize(SAAS_USER_WORKLOAD, SAAS_NUMBER_OF_USERS);
 			Validator.checkIsNonEmptyStringArray(SAAS_USER_WORKLOAD, getStringArray(SAAS_USER_WORKLOAD));
 		}else{
 			if(getStringArray(SAAS_USER_WORKLOAD).length != 0){
 				throw new ConfigurationException("Cannot define user specific workload when " +
 						"an unique workload has been already specified.");
 			}
 		}
 	}
 	
 	private void checkSize(String propertyName, String sizePropertyName) throws ConfigurationException {
 		String[] values = getStringArray(propertyName);
 		int size = getInt(sizePropertyName);
 		if (values.length != size){
 			throw new ConfigurationException("Check number of values in " + 
 					propertyName + ". It must be equals to what is specified at " + 
 					sizePropertyName);
 		}
 	}
 
 	
 	
 	
 	// ******************************** IAAS PROVIDERS ************************************/
 	
 	private void verifyIaaSProvidersProperties() throws ConfigurationException {
 		
 		Validator.checkPositive(IAAS_NUMBER_OF_PROVIDERS, getString(IAAS_NUMBER_OF_PROVIDERS));
 		
 		Validator.checkIsEnumArray(IAAS_TYPES, getStringArray(IAAS_TYPES), MachineType.class);
 		Validator.checkIsPositiveDoubleArray(IAAS_POWER, getStringArray(IAAS_POWER));
 		
 		if(getStringArray(IAAS_TYPES).length != getStringArray(IAAS_POWER).length){
 			throw new ConfigurationException(IAAS_TYPES + " must have the same size of " + IAAS_POWER);
 		}
 		
 		checkSize(IAAS_PROVIDER_NAME, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_ONDEMAND_CPU_COST, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_RESERVED_CPU_COST, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_ONE_YEAR_FEE, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_THREE_YEARS_FEE, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_TRANSFER_IN, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_COST_TRANSFER_IN, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_TRANSFER_OUT, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_COST_TRANSFER_OUT, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_ONDEMAND_LIMIT, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_RESERVED_LIMIT, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_MONITORING, IAAS_NUMBER_OF_PROVIDERS);
 		checkSize(IAAS_PROVIDER_TYPES, IAAS_NUMBER_OF_PROVIDERS);
 		
 		Validator.checkIsNonEmptyStringArray(IAAS_PROVIDER_NAME, getStringArray(IAAS_PROVIDER_NAME));
 		Validator.checkIsNonNegativeDouble2DArray(IAAS_PROVIDER_ONDEMAND_CPU_COST, getStringArray(IAAS_PROVIDER_ONDEMAND_CPU_COST), ARRAY_SEPARATOR);
 		Validator.checkIsNonNegativeDouble2DArray(IAAS_PROVIDER_RESERVED_CPU_COST, getStringArray(IAAS_PROVIDER_RESERVED_CPU_COST), ARRAY_SEPARATOR);
 		Validator.checkIsNonNegativeDouble2DArray(IAAS_PROVIDER_ONE_YEAR_FEE, getStringArray(IAAS_PROVIDER_ONE_YEAR_FEE), ARRAY_SEPARATOR);
 		Validator.checkIsNonNegativeDouble2DArray(IAAS_PROVIDER_THREE_YEARS_FEE, getStringArray(IAAS_PROVIDER_THREE_YEARS_FEE), ARRAY_SEPARATOR);
 		Validator.checkIsNonNegative2DArray(IAAS_PROVIDER_TRANSFER_IN, getStringArray(IAAS_PROVIDER_TRANSFER_IN), ARRAY_SEPARATOR);
 		Validator.checkIsNonNegativeDouble2DArray(IAAS_PROVIDER_COST_TRANSFER_IN, getStringArray(IAAS_PROVIDER_COST_TRANSFER_IN), ARRAY_SEPARATOR);
 		Validator.checkIsNonNegative2DArray(IAAS_PROVIDER_TRANSFER_OUT, getStringArray(IAAS_PROVIDER_TRANSFER_OUT), ARRAY_SEPARATOR);
 		Validator.checkIsNonNegativeDouble2DArray(IAAS_PROVIDER_COST_TRANSFER_OUT, getStringArray(IAAS_PROVIDER_COST_TRANSFER_OUT), ARRAY_SEPARATOR);
 		Validator.checkIsNonNegativeArray(IAAS_PROVIDER_ONDEMAND_LIMIT, getStringArray(IAAS_PROVIDER_ONDEMAND_LIMIT));
 		Validator.checkIsNonNegativeArray(IAAS_PROVIDER_RESERVED_LIMIT, getStringArray(IAAS_PROVIDER_RESERVED_LIMIT));
 		Validator.checkIsNonNegativeDoubleArray(IAAS_PROVIDER_MONITORING, getStringArray(IAAS_PROVIDER_MONITORING));
 		Validator.checkIsEnum2DArray(IAAS_PROVIDER_TYPES, getStringArray(IAAS_PROVIDER_TYPES), MachineType.class, ARRAY_SEPARATOR);
 	}
 	
 	
 	
 	// ************************************* SAAS ************************************/
 
 
 	private void verifySaaSPlansProperties() throws ConfigurationException {
 		Validator.checkPositive(NUMBER_OF_PLANS, getString(NUMBER_OF_PLANS));
 
 		checkSize(PLAN_NAME, NUMBER_OF_PLANS);
 		checkSize(PLAN_PRIORITY, NUMBER_OF_PLANS);
 		checkSize(PLAN_PRICE, NUMBER_OF_PLANS);
 		checkSize(PLAN_SETUP, NUMBER_OF_PLANS);
 		checkSize(PLAN_CPU_LIMIT, NUMBER_OF_PLANS);
 		checkSize(PLAN_EXTRA_CPU_COST, NUMBER_OF_PLANS);
 		checkSize(PLAN_TRANSFER_LIMIT, NUMBER_OF_PLANS);
 		checkSize(PLAN_EXTRA_TRANSFER_COST, NUMBER_OF_PLANS);
 		checkSize(PLAN_STORAGE_LIMIT, NUMBER_OF_PLANS);
 		checkSize(PLAN_EXTRA_STORAGE_COST, NUMBER_OF_PLANS);
 		
 		Validator.checkIsNonNegativeDoubleArray(PLAN_PRICE, getStringArray(PLAN_PRICE));
 		Validator.checkIsNonNegativeDoubleArray(PLAN_SETUP, getStringArray(PLAN_SETUP));
 		Validator.checkIsNonNegativeDoubleArray(PLAN_EXTRA_CPU_COST, getStringArray(PLAN_EXTRA_CPU_COST));
 		Validator.checkIsNonNegativeArray(PLAN_CPU_LIMIT, getStringArray(PLAN_CPU_LIMIT));
 		Validator.checkIsNonNegativeArray(PLAN_PRIORITY, getStringArray(PLAN_PRIORITY));
 		Validator.checkIsNonNegativeDouble2DArray(PLAN_EXTRA_CPU_COST, getStringArray(PLAN_EXTRA_CPU_COST), ARRAY_SEPARATOR);
 		Validator.checkIsNonNegative2DArray(PLAN_CPU_LIMIT, getStringArray(PLAN_CPU_LIMIT), ARRAY_SEPARATOR);
 	}
 	
 	// ******************************** IAAS PLAN ************************************/
 	
 	private void verifyIaaSPlanProperties() throws ConfigurationException {
 		String[] providersWithPlan = getStringArray(IAAS_PLAN_PROVIDER_NAME);
 		String[][] machines = getString2DArray(IAAS_PLAN_PROVIDER_TYPES);
 		long[][] reservation = getLong2DArray(IAAS_PLAN_PROVIDER_RESERVATION);
 		
 		if(providersWithPlan.length != machines.length){
 			throw new ConfigurationException("Number of values in " + IAAS_PLAN_PROVIDER_NAME + " must be the same of " + IAAS_PLAN_PROVIDER_TYPES);
 		}
 		
 		if(providersWithPlan.length != reservation.length){
 			throw new ConfigurationException("Number of values in " + IAAS_PLAN_PROVIDER_NAME + " must be the same of " + IAAS_PLAN_PROVIDER_RESERVATION);
 		}
 		
 		for (int i = 0; i < machines.length; i++) {
 			if(machines[i].length != reservation[i].length){
 				throw new ConfigurationException("Number of values in " + IAAS_PLAN_PROVIDER_TYPES + " must be the same of " + IAAS_PLAN_PROVIDER_RESERVATION);
 			}
 		}
 		
 		Validator.checkIsNonEmptyStringArray(IAAS_PLAN_PROVIDER_NAME, providersWithPlan);
 		Validator.checkIsNonEmptyString2DArray(IAAS_PLAN_PROVIDER_TYPES, providersWithPlan, ARRAY_SEPARATOR);
 		
 		HashSet<String> set = new HashSet<String>(Arrays.asList(getStringArray(IAAS_PROVIDER_NAME)));
 		for (String provider : providersWithPlan) {
 			if(!set.contains(provider)){
 				throw new ConfigurationException("Provider " + provider + " need to be defined at providers configuration file.");
 			}
 		}
 		
 	}
 
 	public String[] getWorkloads() {
 		String[] stringArray = this.getStringArray(SaaSUsersProperties.SAAS_USER_WORKLOAD);
 		if(stringArray == null || stringArray.length == 0){
 			String workload = getString(SAAS_WORKLOAD);
 			stringArray = new String[]{workload};
 		}
 		return stringArray;
 	}
 }
