 package evaluation.simulator.gui.service;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.swing.JOptionPane;
 
 import evaluation.simulator.log.LogLevel;
 import evaluation.simulator.log.Logger;
 
 public class ConfigParser {
 
 	public String clenupConfigurationForSimulator(File file) {
 		StringBuffer content = new StringBuffer();
 		BufferedReader reader = null;
 
 		Map<String, String> configNameToPluginName = new HashMap<>();
 		configNameToPluginName.put("TYPE_OF_DELAY_BOX", "delayBox");
 		configNameToPluginName
 				.put("TYPE_OF_TRAFFIC_GENERATOR", "trafficSource");
 		configNameToPluginName.put("PLOT_TYPE", "plotType");
 		configNameToPluginName.put("OUTPUT_STRATEGY", "outputStrategy");
 		configNameToPluginName.put("TOPOLOGY_SCRIPT", "topology");
 		configNameToPluginName.put("MIX_SEND_STYLE", "mixSendStyle");
 		configNameToPluginName.put("CLIENT_SEND_STYLE", "clientSendStyle");
 		
 		Properties props = new Properties();
 		
 		try {
 			props.load(new FileInputStream(file));
 		} catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(null, "File not found.");
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "Could not read from file.");
 		} catch (IllegalArgumentException e) {
 			JOptionPane.showMessageDialog(null, "Could not read from file.");
 		}
 
 		// Load simProps
 		List<String> activePlugins = new LinkedList<String>();
 		List<String> generalProperties = new LinkedList<String>();
 		
 		for (String s : configNameToPluginName.keySet() ) {
 				String tmp = props.getProperty(s);
 				Logger.Log(LogLevel.DEBUG, "Found: " + s + "=" + tmp);
 				activePlugins.add(tmp);
 				generalProperties.add(s);
 		}
 
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			String string = null;
 			String[] subStrings = { "" };
 			
 			// Some glue that we need at the moment
 			// TODO: Find a nice way to wrap them into the gui 
 			
 			content.append("DEBUG_OUTPUT=ON").append(System.getProperty("line.separator"));
 			
 			// DESIRED_EVALUATIONS
 			content.append("DESIRED_EVALUATIONS=AVG_CLIENT_LATENCY_REQUESTMIXMESSAGE,MAX_CLIENT_LATENCY_REQUESTMIXMESSAGE").append(System.getProperty("line.separator")); // See StatisticsType.java
 			content.append("START_RECORDING_STATISTICS_AT=0").append(System.getProperty("line.separator"));
 			
 			// PROPERTYS TO VARY
 			content.append("PROPERTY_TO_VARY=BATCH_SIZE").append(System.getProperty("line.separator"));
 			content.append("VALUES_FOR_THE_PROPERTY_TO_VARY=2,3,5,10,20,50,100,200,500").append(System.getProperty("line.separator"));
 			content.append("USE_SECOND_PROPERTY_TO_VARY=FALSE").append(System.getProperty("line.separator"));
 			content.append("SECOND_PROPERTY_TO_VARY=").append(System.getProperty("line.separator"));
 			content.append("VALUES_FOR_THE_SECOND_PROPERTY_TO_VARY=").append(System.getProperty("line.separator"));
 			
 			// VALIDATION RUNS
 			content.append("VALIDATION_RUNS=2").append(System.getProperty("line.separator")); // plotType?!
 			content.append("NAME_OF_PLOT_SCRIPT=defaultPlotScript.txt").append(System.getProperty("line.separator"));
 			content.append("OVERWRITABLE_PARAMETERS=").append(System.getProperty("line.separator"));
 			content.append("NONE_OVERWRITABLE_PARAMETERS=set logscale x").append(System.getProperty("line.separator"));
 			
 			content.append("SIMULATION_END=SIMULATION_TIME_END").append(System.getProperty("line.separator"));
			content.append("REAL_TIME_LIMIT_IN_SEC=1.0").append(System.getProperty("line.separator"));
 			content.append("SIMULATION_TIME_LIMIT_IN_MS=1000000").append(System.getProperty("line.separator"));
 			content.append("MESSAGE_FORMAT=BASIC_MIX_MESSAGE").append(System.getProperty("line.separator"));
 			content.append("NETWORK_PACKET_PAYLOAD_SIZE=512").append(System.getProperty("line.separator"));
 			content.append("MIX_REQUEST_PAYLOAD_SIZE=512").append(System.getProperty("line.separator"));
 			content.append("MIX_REQUEST_HEADER_SIZE=0").append(System.getProperty("line.separator"));
 			content.append("MIX_REPLY_PAYLOAD_SIZE=512").append(System.getProperty("line.separator"));
 			content.append("MIX_REPLY_HEADER_SIZE=0").append(System.getProperty("line.separator"));
 			content.append("MIX_REQUEST_CREATION_TIME=0").append(System.getProperty("line.separator"));
 			content.append("MIX_REPLY_DECRYPTION_TIME=0").append(System.getProperty("line.separator"));
 			content.append("PROCESSING_TIME_FOR_1000_REQUESTS=0").append(System.getProperty("line.separator"));
 			content.append("PROCESSING_TIME_FOR_1000_REPLIES=0").append(System.getProperty("line.separator"));
 
 			content.append("COMMUNICATION_MODE=SIMPLEX").append(System.getProperty("line.separator"));
 			
 			content.append("BASIC_DELAY_BOX_DEFAULT_CLIENT_BANDWIDTH_SEND=131072").append(System.getProperty("line.separator"));
 			content.append("BASIC_DELAY_BOX_DEFAULT_CLIENT_BANDWIDTH_RECEIVE=UNLIMITED").append(System.getProperty("line.separator"));
 			content.append("BASIC_DELAY_BOX_DEFAULT_CLIENT_LATENCY=25").append(System.getProperty("line.separator"));
 			content.append("BASIC_DELAY_BOX_DEFAULT_MIX_BANDWIDTH_SEND=UNLIMITED").append(System.getProperty("line.separator"));
 			content.append("BASIC_DELAY_BOX_DEFAULT_MIX_BANDWIDTH_RECEIVE=UNLIMITED").append(System.getProperty("line.separator"));
 			content.append("BASIC_DELAY_BOX_DEFAULT_MIX_LATENCY=7").append(System.getProperty("line.separator"));
 			content.append("BASIC_DELAY_BOX_DEFAULT_DISTANT_PROXY_BANDWIDTH_SEND=UNLIMITED").append(System.getProperty("line.separator"));
 			content.append("BASIC_DELAY_BOX_DEFAULT_DISTANT_PROXY_BANDWIDTH_RECEIVE=UNLIMITED").append(System.getProperty("line.separator"));
 			content.append("BASIC_DELAY_BOX_DEFAULT_DISTANT_PROXY_LATENCY=7").append(System.getProperty("line.separator"));
 					
 			content.append("RECEIVER_SUPPORTS_DUMMY_TRAFFIC=FALSE").append(System.getProperty("line.separator"));
 			
 			// Filter relevant lines in configuration file
 			while ((string = reader.readLine()) != null) {
 				
 				// Pluginlevel
 				for (String plugin : generalProperties) {
 					if (string.matches("^" + plugin + "=\\w*")) {
 						content.append(string).append(System.getProperty("line.separator"));
 					}
 				}
 				
 				// Simproplevel
 				for (String plugin : activePlugins) {
 					if (string.matches("^" + plugin + "\\\\:\\\\:\\w*=\\w*")) {
 						subStrings = string.split("\\\\:\\\\:");
 						content.append(subStrings[1]).append(System.getProperty("line.separator"));
 					}
 				}
 				
 				// TODO: Delete whitespace!
 			}
 
 		} catch (FileNotFoundException e) {
 
 		} catch (IOException e) {
 
 		} finally {
 			try {
 				if (reader != null) {
 					reader.close();
 				}
 			} catch (IOException e) {
 
 			}
 		}
 
 		System.out.println(content.toString());
 		
 		return content.toString();
 
 	}
 }
