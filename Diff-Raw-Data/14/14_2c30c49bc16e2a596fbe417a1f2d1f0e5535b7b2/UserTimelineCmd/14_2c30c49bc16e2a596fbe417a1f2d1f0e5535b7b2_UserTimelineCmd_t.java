 package com.matrobot.gha.archive.cmd;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.matrobot.gha.Configuration;
 import com.matrobot.gha.ICommand;
 import com.matrobot.gha.archive.event.EventReader;
 import com.matrobot.gha.archive.event.EventRecord;
 import com.matrobot.gha.archive.event.IEventReader;
 
 /**
  * Check user activity after time range
  * 
  * @author Krzysztof Langner
  */
 public class UserTimelineCmd implements ICommand{
 
 	private Set<String> firstMonthUsers = new HashSet<String>();
 	Set<String> activeUsers = new HashSet<String>();
 	private int index = 1;
 	private String currentDate;
 	private PrintStream outputStream;
 	
 	
 	@Override
 	public void run(Configuration params) throws IOException {
 
 		IEventReader reader;
 		List<String> months = params.getMonthFolders();
		reader = new EventReader(months.remove(0));
 		initFirstMonth(reader);
 		initOutputStream(params);
 		reader = new EventReader(months);
 		parse(reader);
 	}
 
 
 	private void initFirstMonth(IEventReader reader) {
 		EventRecord	event;
 		while((event = reader.next()) != null){
 			String actor = event.getActorLogin();
 			if(actor != null){
 				firstMonthUsers.add(actor);
 			}
 		}
 	}
 
 
 	private void initOutputStream(Configuration params) {
 		outputStream = params.getOutputStream(); 
		outputStream.println("index,date,users");
		outputStream.println("0,0," + firstMonthUsers.size());
 	}
 
 
 	private void parse(IEventReader reader) {
 		
 		EventRecord	event;
 		while((event = reader.next()) != null){
 			prepareCurrentRecord(event);
 			String actor = event.getActorLogin();
 			if(firstMonthUsers.contains(actor)){
 				activeUsers.add(actor);
 			}
 		}
 		
 		saveRecord();
 	}
 
 
 	private void prepareCurrentRecord(EventRecord event) {
 
 		String date = event.created_at.substring(0, 7);
 		
 		if(currentDate == null){
			System.out.println(date);
 			currentDate = date;
 		}
 		else if(!currentDate.equals(date)){
 			System.out.println(date);
 			saveRecord();
 			currentDate = date;
 			activeUsers.clear();
 		}
 	}
 
 
 	private void saveRecord(){
		outputStream.println(index + "," + currentDate + "," + activeUsers.size());
 		index++;
 	}
 
 	
 	/**
 	 * for local testing
 	 * @param args
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 
 		Configuration params = new Configuration("configs/timeline.yaml");
 		
 		UserTimelineCmd app = new UserTimelineCmd();
 		app.run(params);
 	}
 	
 }
