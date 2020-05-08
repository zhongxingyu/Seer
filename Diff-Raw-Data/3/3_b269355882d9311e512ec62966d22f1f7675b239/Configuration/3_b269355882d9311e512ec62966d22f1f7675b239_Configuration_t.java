 package ch.ethz.mlmq.common;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import ch.ethz.mlmq.logging.PerformanceLoggerConfig;
 
 /**
  * Wrapper for the configuration properties file
  */
 public class Configuration {
 
 	public static final String PERFORMANCELOGGER_PATH = "common.performancelogger.logfilepath";
 	public static final String COMMANDOFILE_PATH = "common.commandofile.path";
 	public static final String COMMANDOFILE_CHECKINTERVALL = "common.commandofile.checkintervall";
 
 	public static final String COMMANDFILEHANDLER_FILEPATH = "common.commandofile.path";
 	public static final String COMMANDFILEHANDLER_CHECKINTERVALL = "common.commandofile.checkintervall";
 
 	public static final String SCENARIO_MAPPING_BROKER = "common.scenario.mapping.broker";
 	public static final String SCENARIO_MAPPING_CLIENT = "common.scenario.mapping.client";
 	public static final String SCENARIO_MYTYPE = "common.scenario.mytype";
 	public static final String SCENARIO_MYPOSITION = "common.scenario.myposition";
 	public static final String SCENARIO_MYTYPE_CLIENT_VALUE = "client";
 	public static final String SCENARIO_MYTYPE_BROKER_VALUE = "broker";
 
 	protected final PerformanceLoggerConfig performanceLoggerConfig;
 
 	/**
 	 * That's where we store all our configuration values
 	 */
 	protected Properties props;
 
 	private List<BrokerScenarioMapping> brokerScenarioMappings;
 	private List<ClientScenarioMapping> clientScenarioMappings;
 
 	private ScenarioMapping myMapping;
 
 	protected Configuration(Properties props) {
 		this.props = props;
 
 		parseScenarioMapping(props);
		performanceLoggerConfig = createPerformanceLogger();
 	}
 
 	protected PerformanceLoggerConfig createPerformanceLogger() {
 		String performanceLoggerPath = getStringConfig(PERFORMANCELOGGER_PATH) + File.separator + getStringConfig(SCENARIO_MYPOSITION);
 
 		String mappingName = null;
 		ScenarioMapping mapping = getMyMapping();
 		if (mapping != null) {
 			mappingName = mapping.getName();
 		}
 		return new PerformanceLoggerConfig(performanceLoggerPath, mappingName);
 	}
 
 	protected void parseScenarioMapping(Properties props) {
 
 		String brokersToParse = getStringConfig(SCENARIO_MAPPING_BROKER);
 		brokerScenarioMappings = parseBrokerScenarioMapping(brokersToParse);
 
 		String clientsToParse = getStringConfig(SCENARIO_MAPPING_CLIENT);
 		clientScenarioMappings = parseClientScenarioMapping(clientsToParse);
 
 		int myPosition = getIntConfig(SCENARIO_MYPOSITION);
 		String myType = getStringConfig(SCENARIO_MYTYPE);
 		if (Configuration.SCENARIO_MYTYPE_BROKER_VALUE.equals(myType)) {
 			myMapping = brokerScenarioMappings.get(myPosition);
 		} else if (Configuration.SCENARIO_MYTYPE_CLIENT_VALUE.equals(myType)) {
 			myMapping = clientScenarioMappings.get(myPosition);
 		} else {
 			throw new RuntimeException("Invalid Configuration - Unexpected myType " + myType);
 		}
 	}
 
 	public static List<ClientScenarioMapping> parseClientScenarioMapping(String toParse) {
 		List<ClientScenarioMapping> result = new ArrayList<>();
 
 		int position = 0;
 		String name = null;
 		String[] clientGroups = toParse.split(";");
 		for (String clientGroup : clientGroups) {
 			String[] clients = clientGroup.split(",");
 
 			for (String client : clients) {
 
 				int nameEndIndex = client.indexOf("#");
 				if (nameEndIndex > 0) {
 					name = client.substring(0, nameEndIndex);
 				}
 
 				String host = client.substring(nameEndIndex + 1, client.length());
 
 				ClientScenarioMapping mapping = new ClientScenarioMapping(name, host, position++);
 				result.add(mapping);
 			}
 		}
 
 		return result;
 	}
 
 	public static List<BrokerScenarioMapping> parseBrokerScenarioMapping(String toParse) {
 		List<BrokerScenarioMapping> result = new ArrayList<>();
 
 		int position = 0;
 		String name = null;
 		String[] brokerGroups = toParse.split(";");
 		for (String brokerGroup : brokerGroups) {
 			String[] brokers = brokerGroup.split(",");
 
 			for (String broker : brokers) {
 
 				int nameEndIndex = broker.indexOf("#");
 				int hostEndIndex = broker.indexOf(":");
 				if (nameEndIndex > 0) {
 					name = broker.substring(0, nameEndIndex);
 				}
 
 				String host = broker.substring(nameEndIndex + 1, hostEndIndex);
 				String portString = broker.substring(hostEndIndex + 1, broker.length());
 				int port = Integer.parseInt(portString);
 
 				BrokerScenarioMapping mapping = new BrokerScenarioMapping(name, host, port, position++);
 				result.add(mapping);
 			}
 		}
 
 		return result;
 	}
 
 	public List<BrokerScenarioMapping> getAllBrokerScenarioMapping() {
 		return brokerScenarioMappings;
 	}
 
 	public List<ClientScenarioMapping> getAllClientScenarioMapping() {
 		return clientScenarioMappings;
 	}
 
 	/**
 	 * That's where we assign clients to specific brokers
 	 * 
 	 * @param clientMapping
 	 * @return
 	 */
 	public BrokerScenarioMapping getAssignedBroker(ClientScenarioMapping clientMapping) {
 
 		// evenly spread clients
 		int pos = clientMapping.getPosition();
 		return brokerScenarioMappings.get(pos % brokerScenarioMappings.size());
 	}
 
 	public int getIntConfig(String configKey) {
 		return Integer.parseInt(getStringConfig(configKey).trim());
 	}
 
 	public long getLongConfig(String configKey) {
 		return Long.parseLong(getStringConfig(configKey).trim());
 	}
 
 	public String getStringConfig(String configKey) {
 		String value = props.getProperty(configKey);
 		if (value == null) {
 			throw new RuntimeException("ConfigurationKey [" + configKey + "] not found");
 		}
 		return value;
 	}
 
 	public ScenarioMapping getMyMapping() {
 		return myMapping;
 	}
 
 	public PerformanceLoggerConfig getPerformanceLoggerConfig() {
 		return performanceLoggerConfig;
 	}
 
 	public String getCommandFileHandlerPath() {
 		return getStringConfig(COMMANDFILEHANDLER_FILEPATH);
 	}
 
 	public int getCommandFileHandlerCheckIntervall() {
 		return getIntConfig(COMMANDFILEHANDLER_CHECKINTERVALL);
 	}
 }
