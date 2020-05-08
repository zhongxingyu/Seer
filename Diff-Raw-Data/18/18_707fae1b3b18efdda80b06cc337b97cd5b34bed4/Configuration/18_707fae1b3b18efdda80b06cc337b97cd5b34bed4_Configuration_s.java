 package com.matrobot.gha;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.yaml.snakeyaml.Yaml;
 
 
 public class Configuration {
 
 	private String command;
 	private String dataPath;
 	private String repositoryName;
 	private String actor;
 	private String startDate;
 	private String endDate;
 	private String outputFilename;
 	private String orderBy;
 	private int minActivity = 0;
 	private PrintStream outputStream;
 	private List<String> eventTypes = new ArrayList<String>();
 	
 	
 	public Configuration(InputStream is){
 		
 		load(is);
 	}
 	
 	public Configuration(String filename) throws FileNotFoundException{
 		
 		load(new FileInputStream(filename));
 	}
 	
 	private void setDatapath(String path) {
 		
 		dataPath = path;
 		if(!dataPath.endsWith("/")){
 			dataPath += '/';
 		}
 	}
 
 	
 	@SuppressWarnings("unchecked")
 	private void load(InputStream inputStream) {
 
 		Yaml yaml = new Yaml();
 		Map<String, Object> config = (Map<String, Object>) yaml.load(inputStream);
 		command = config.get("command").toString();
 		setDatapath(config.get("datapath").toString());
 		if(config.get("repository") != null){
 			repositoryName = config.get("repository").toString();
 		}
 		if(config.get("actor") != null){
 			actor = config.get("actor").toString();
 		}
 		if(config.get("order_by") != null){
 			orderBy = config.get("order_by").toString();
 		}
 		if(config.get("min_activity") != null){
 			minActivity = Integer.parseInt(config.get("min_activity").toString());
 		}
 		if(config.get("event_type") != null){
 			eventTypes.add(config.get("event_type").toString());
 		}
 		outputFilename = config.get("output").toString();
 		Map<String, String> dateRange = (Map<String, String>) config.get("date");
 		startDate = dateRange.get("from");
 		endDate = dateRange.get("to");
 	}
 
 	/**
 	 * @return command
 	 */
 	public String getCommand(){
 		return command;
 	}
 	
 	/**
 	 * @return -data= parameter
 	 */
 	public String getDataPath(){
 		return dataPath;
 	}
 	
 	
 	/**
 	 * @return -from=
 	 */
 	public String getStartDate(){
 		return startDate;
 	}
 	
 	
 	/**
 	 * @return -to=
 	 */
 	public String getEndDate(){
 		return endDate;
 	}
 	
 	
 	/**
 	 * @return Full path folders based on date range
 	 */
 	public List<String> getMonthFolders(){
 		
 		List<String> folders = new ArrayList<String>();
 		
 		String[] tokens;
 		int month;
 		int year;
 		int endMonth;
 		int endYear;
 		
 		tokens = startDate.split("-");
 		if(tokens.length == 2){
 		
 			year = Integer.parseInt(tokens[0]);
 			month = Integer.parseInt(tokens[1]);
 			
 			tokens = endDate.split("-");
 			if(tokens.length == 2){
 			
 				endYear = Integer.parseInt(tokens[0]);
 				endMonth = Integer.parseInt(tokens[1]);
 		
				while(year < endYear || month <= endMonth){
 					folders.add(dataPath + year + "-" + month);
 					
 					month ++;
 					if(month > 12){
 						year ++;
 						month = 1;
 					}
 				}
 			}
 		}
 		
 		return folders;
 	}
 
 	
 	/**
 	 * @return -repo=
 	 */
 	public String getRepositoryName() {
 		return repositoryName;
 	}
 	
 	
 	public PrintStream getOutputStream(){
 		
 		try {
 			if(outputStream == null){
 				if(outputFilename != null){
 					FileOutputStream fos;
 						fos = new FileOutputStream(outputFilename, false);
 					outputStream = new PrintStream(fos);
 				}
 				else{
 					outputStream = System.out;
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			outputStream = System.out;
 		}
 		
 		return outputStream;
 	}
 
 	public String getOrderBy() {
 		return orderBy;
 	}
 
 	public int getMinActivity() {
 		return minActivity;
 	}
 
 	public String getActor() {
 		return actor;
 	}
 
 	public String getEventType() {
 		
 		if(eventTypes.size() > 0){
 			return eventTypes.get(0);
 		}
 		
 		return null;
 	}
 }
