 package org.italiangrid.voms.clients.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.italiangrid.voms.clients.strategies.VOMSCommandsParsingStrategy;
 
 public class DefaultVOMSCommandsParser implements VOMSCommandsParsingStrategy {
 
 	public static final String COMMAND_SEPARATOR = ":";
 	public static final String ALL_COMMAND_STRING = "all";
 
 	public Map<String, List<String>> parseCommands(List<String> commands) {
 
 		if (commands == null)
 			return null;
 
 		if (commands.isEmpty())
 			return Collections.emptyMap();
 
		Map<String, List<String>> commandsMap = new HashMap<String, List<String>>();
 
 		for (String cmd : commands) {
 
 			String[] cmdTokens = cmd.split(COMMAND_SEPARATOR);
 			String vo = cmdTokens[0];
 
 			if (commandsMap.containsKey(vo)) {
 
 				if (cmdTokens.length > 1
 						&& !cmdTokens[1].equals(ALL_COMMAND_STRING))
 					commandsMap.get(vo).add(cmdTokens[1]);
 			} else {
 
 				List<String> requestedFQANs = new ArrayList<String>();
 
 				if (cmdTokens.length > 1 && !cmdTokens[1].equals("all"))
 					requestedFQANs.add(cmdTokens[1]);
 
 				commandsMap.put(vo, requestedFQANs);
 			}
 		}
 
 		return commandsMap;
 	}
 
 }
