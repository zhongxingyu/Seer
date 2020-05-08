 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
 import au.com.bytecode.opencsv.CSVParser;
 
 
 /**
  * Implement
  *
  * @author Arathald (Greg Jackson)
  *         Created January 10, 2012.
  */
 
 public class Implement extends NoiseModule implements Serializable {
 	private static final String COLOR_POSITIVE = ""; // No color
 	private static final String COLOR_NEGATIVE = RED;
 	
 	private static final String AUTHOR = "arathald";
 	
 	private class RequestData {
 		String description;
 		String requestedBy;
 		Date requestedDate;
 		boolean isImplemented;
 		String moduleName;
 		String implementedBy;
 		Date implementedDate;
 	}
 	
 	private HashMap<String, RequestData> requests = new HashMap<String, RequestData>();
 	
 	@Command("\\.(?:implement|request|requests|requested)")
 	public void showRequests(Message message) {
 		Set<String> requestKeys = this.requests.keySet();
 		
 		for (String requestKey: requestKeys) {
 			RequestData requestData = this.requests.get(requestKey);
 			if (!requestData.isImplemented)
 			{
 				this.printRequest(requestKey, requestData);
 			}
 		}
 	}
 	
 	@Command("\\.implemented")
 	public void showImplemented(Message message) {
 		Set<String> requestKeys = this.requests.keySet();
 		
 		for (String requestKey: requestKeys) {
 			RequestData requestData = this.requests.get(requestKey);
 			if (requestData.isImplemented)
 			{
 				this.printRequest(requestKey, requestData);
 			}
 		}
 	}
 	
 	@Command("\\.(?:implement|request) (.+)")
 	public void addRequest(Message message, String argLine) {
 		Pattern requestPattern = Pattern.compile("\"(.+)\" \"(.+)\"");
 		Matcher match = requestPattern.matcher(argLine);
 		
 		if (match.matches()) {
 			
 			String requestName = match.group(1);
 			String requestDescription = match.group(2);
 		
 			if (!this.requests.containsKey(requestName)) {
 				RequestData requestData = new RequestData();
 				requestData.description = requestDescription;
 				requestData.requestedBy = message.getSender();
 				requestData.requestedDate = java.util.Calendar.getInstance().getTime();
 				requestData.isImplemented = false;
 				
 				
 				this.requests.put(requestName, requestData);
 				this.save();
 				
 				this.printRequest(requestName, requestData);
 			} else {
 				this.bot.sendMessage(RED + requestName + " already exists. Choose a different name.");
 			}
 		}
 		else {
 			this.bot.sendMessage(RED + "Invalid syntax for .implement. Example:");
 			this.bot.sendMessage(RED + ".implement \"Request Name\" \"Description\"");
 		}
 
 		
 	}
 	
 	@Command("\\.implemented (.+)")
 	public void markImplemented (Message message, String argLine) {
		Pattern requestPattern = Pattern.compile("\"(.+)\" \"(.+)\"");
 		Matcher match = requestPattern.matcher(argLine);
 		
 		if (!match.matches()) {
 			this.bot.sendMessage(RED + "Invalid syntax for .implemented. Example:");
 			this.bot.sendMessage(RED + ".implemented \"Request Name\" \"ModuleName\"");
 		}
 		
 		String requestName = match.group(1);
 		String moduleName = match.group(2);
 		
 		if (this.requests.containsKey(requestName)) {
 			RequestData requestData = this.requests.get(requestName);
 			requestData.isImplemented = true;
 			requestData.implementedBy = message.getSender();
 			requestData.implementedDate = java.util.Calendar.getInstance().getTime();
 			requestData.moduleName = moduleName;
 			
 			this.save();
 			
 			this.printRequest(requestName, requestData);
 		} else {
 			this.bot.sendMessage(RED + "I can't find the request " + requestName);
 		}
 	}
 	
 	@Command("\\.deleterequest (.+)")
 	public void deleteRequest(Message message, String requestName) {
 		
 		if (this.requests.containsKey(requestName)) {
 			RequestData requestData = this.requests.get(requestName);
 			
 			// Morasique and Arathald have permissions to delete any request
 			if (message.getSender().equalsIgnoreCase(bot.ME) || message.getSender().equalsIgnoreCase(this.AUTHOR)) {
 				this.requests.remove(requestName);
 				this.save();
 			}
 			// If it's been implemented, no one else can remove it
 			else if (requestData.isImplemented) {
 				this.bot.sendMessage(RED + requestName + "has been implemented, and you don't have permission to delete it. " +
 						"If you implemented it, try .unimplement instead");
 			// Otherwise, if it hasn't been implemented, the requester can remove it
 			} else if (message.getSender().equalsIgnoreCase(requestData.requestedBy)) {
 				this.requests.remove(requestName);
 				this.save();
 			}
 			// But no one else can
 			else {
 				this.bot.sendMessage(RED + "You don't have permission to delete " + requestName);
 			}
 		}
 		// This request doesn't exist 
 		else {
 			this.bot.sendMessage(RED + "I can't find the request " + requestName);
 		}
 	}
 	
 	@Command("\\.unimplement (.+)")
 	public void unimplementRequest(Message message, String requestName) {
 		if (this.requests.containsKey(requestName)) {
 			RequestData requestData = this.requests.get(requestName);
 			
 			// Morasique, Arathald, and the implementer have permissions to unimplement any request
 			if (message.getSender().equalsIgnoreCase(bot.ME) || message.getSender().equalsIgnoreCase(this.AUTHOR) ||
 					message.getSender().equalsIgnoreCase(requestData.implementedBy)) {
 				requestData.isImplemented = false;
 				requestData.implementedBy = null;
 				requestData.moduleName = null;
 				requestData.implementedDate = null;
 				this.save();
 			}
 			// But no one else can
 			else {
 				this.bot.sendMessage(RED + "You don't have permission to delete " + requestName);
 			}
 		}
 		// This request doesn't exist 
 		else {
 			this.bot.sendMessage(RED + "I can't find the request " + requestName);
 		}
 	}
 			
 	private void printRequest(String requestName, RequestData requestData) {
 		this.bot.sendMessage(requestName + ": \"" + requestData.description +  "\"");
 		this.bot.sendMessage("\tRequested by " + requestData.requestedBy + " on " +	this.formatDate(requestData.requestedDate));
 		if (requestData.isImplemented) {
 			this.bot.sendMessage("\tImplemented by " + requestData.implementedBy + " on " + this.formatDate(requestData.implementedDate) + 
 					" as " + requestData.moduleName);
 		}
 	}
 	
 	private String formatDate(Date date) {
 		DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
 		return dateFormat.format(date);
 	}
 	
 	@Override public String getFriendlyName() {return "Implement";}
 	@Override public String getDescription() {return "Allows users to request rhnoise features and mark them as completed";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				".implement \"Request Name\" \"Description\" -- Request a feature",
 				".implement -- Show requested features",
 				".implemented \"Request Name\" \"ModuleName\" -- Mark Request Name as implemented, in module Module Name",
 				".implemented -- Show all implemented requests, including who implemented them and the module",
 				".deleterequest Request Name -- Remove a request that you made",
 				".unimplement Request Name -- Remove the implemented flag from a request that you added it to"
 		};
 	}
 }
