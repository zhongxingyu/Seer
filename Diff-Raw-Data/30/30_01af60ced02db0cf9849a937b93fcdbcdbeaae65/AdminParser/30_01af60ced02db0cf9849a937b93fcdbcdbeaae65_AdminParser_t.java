 package ar.edu.itba.pdc.parser;
 
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
import ar.edu.itba.pdc.filters.StatisticsFilter;
 import ar.edu.itba.pdc.utils.ConfigurationCommands;
 
 public class AdminParser {
 
 	private Map<String, CommandType> commands = new HashMap<String, CommandType>();
 	private ConfigurationCommands commandManager;
 
 	public AdminParser() {
 		commandManager = ConfigurationCommands.getInstance();
 		commands.put("silenceuser", CommandType.listCommand);
 		commands.put("statistics", CommandType.booleanCommand);
 		commands.put("monitor", CommandType.booleanCommand);
 		commands.put("getStatistics", CommandType.getCommand);
 		commands.put("transformation", CommandType.booleanCommand);
 		commands.put("interval", CommandType.booleanCommand);
 	}
 
 	public boolean parseCommand(ByteBuffer readBuffer) throws JSONException {
 		JSONObject json = new JSONObject(new String(readBuffer.array()));
 		return jsonParse(json);
 
 	}
 
 	private boolean jsonParse(JSONObject jsonObject) throws JSONException {
 		String[] keys = JSONObject.getNames(jsonObject); // Multiples mensajes
 		if (keys == null) {
 			throw new JSONException("Bad syntax");
 		}
 		for (String string : keys) {
 			for (String cmd : this.commands.keySet()) {
 				if (string.equals(cmd)) {
 					boolean accepted;
 					switch (commands.get(string)) {
 						case booleanCommand:
 							accepted = executeBooleanCommand(jsonObject, cmd);
 							break;
 						case listCommand:
 							accepted = executeListCommand(jsonObject, cmd);
 							break;
 						case getCommand:
 							/*Asignar a respuesta*/ executeGetCommand(jsonObject, cmd);
 							accepted = true;
 							break;
 						default:
 							accepted = false;
 					}
 					if (accepted)
 						commandManager.saveFile();
 					else
 						throw new JSONException("Bad Syntax");
 					break;
 				}
 			}
 		}
 		return true;
 	}
 	
 	private boolean executeListCommand(JSONObject jsonObject, String cmd) {
 		String oldValue = "";
 		if (commandManager.hasProperty(cmd))
 			oldValue = commandManager.getProperty(cmd);
 		String newValue = "";
 		try {
 			newValue = jsonObject.get(cmd).toString();
 			if (oldValue != null && oldValue.contains(newValue)) {
 				return false;
 			}
 		} catch (JSONException e) {
 			System.out.println("Error JSON");
 		}
 
 		if (oldValue != null && !oldValue.equals(""))
 			commandManager.setProperty(cmd, oldValue + ";"
 					+ newValue);
 		else
 			commandManager.setProperty(cmd, newValue);
 		return true;
 	}
 	
 	private boolean executeBooleanCommand(JSONObject jsonObject, String cmd) throws JSONException {
 		String value = jsonObject.get(cmd).toString().toLowerCase();
 		if (!value.equals("enabled") && !value.equals("disabled"))
 			return false;
 		commandManager.setProperty(cmd, value);
 		return true;
 	}
 	
 	private void executeGetCommand(JSONObject jsonObject, String cmd) {
		/* Ver como hacer el get*/
 	}
 }
