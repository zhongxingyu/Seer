 package fi.helsinki.cs.scheduler3000.report;
 
 
 /**
  * @author Team TA's
  */
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import fi.helsinki.cs.scheduler3000.model.Event;
 import fi.helsinki.cs.scheduler3000.model.Schedule;
 import fi.helsinki.cs.scheduler3000.model.Weekday;
 import fi.helsinki.cs.scheduler3000.model.Weekday.Day;
 
 public class WeekReport extends Report {
 
 	public WeekReport(Schedule schedule, HashMap<String, Object> options) {
 		super(schedule, options);
 	}
 
 	@Override
 	public String toString() {
 		
 		if (this.options.containsKey("days")){
 			ArrayList<Weekday.Day> days = (ArrayList<Day>)this.options.get("days");			
 			String[][] res = new String[days.size() + 1][7]; // +1 for header row
 
 			res[0][0] = "\t";
 			
			for (int i = 1, j = 0; j < Event.VALID_START_TIMES.length; i++, j++){
				res[0][i] = Event.VALID_START_TIMES[j] + "\t";
 			}	
 
 			int i = 1;
 			for (Day day : days){
 				res[i][0] = day.toString() + "\t";
 				i++;
 			}
 			
 			i = 1;
 			for (Day d : days){		
 				ArrayList<Event> events = this.schedule.getSchedule().get(d); 
 				
 				if (events == null){
 					return null;
 				}
 				else if (events.size() == 0){
 					for (int x = 1; x < 7; x++) {
 						res[i][x] = "\t";
 					}
 				}
 				
 				for (Event event : events){
 					String entry = "\t"; // if event is null
 						
 					if (event.getLocation() != null) { 
 					  entry = event.getLocation()+"\t";
 					}
 					
					if (event.getStartTime().equals("08"))     { res[i][1] = entry; } 
					else if(event.getStartTime().equals("10")) { res[i][2] = entry; } 
					else if(event.getStartTime().equals("12")) { res[i][3] = entry;	} 
					else if(event.getStartTime().equals("14")) { res[i][4] = entry; } 
					else if(event.getStartTime().equals("16")) { res[i][5] = entry; } 
					else if(event.getStartTime().equals("18")) { res[i][6] = entry; }
 				
 					// fill up with empties
 					for (int x = 1; x < 7; x++) {
 						if (res[i][x] == null){
 							res[i][x] = "\t";
 						}
 					}
 					
 				}
 				i++;
 			}
 						
 			String response = "";
 			
 			for (int j = 0; j < res.length; j++){
 				for (int k = 0; k < res[0].length; k++){
 					response += res[j][k];
 				}
 				response += "\n";
 			}
 			
 			return response;
 		}
 		return null;
 	}
 	
 }
