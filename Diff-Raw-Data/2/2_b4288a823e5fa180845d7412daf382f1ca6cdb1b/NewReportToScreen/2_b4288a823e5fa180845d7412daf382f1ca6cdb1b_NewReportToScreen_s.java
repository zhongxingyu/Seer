 package fi.helsinki.cs.scheduler3000.cli;
 
 import fi.helsinki.cs.scheduler3000.model.Schedule;
 
 public class NewReportToScreen extends NewReport {
 
 	NewReportToScreen(Schedule schedule) {
 		super(schedule);
 	}
 	
 	void run() {
 		super.run();
		if( report == null ) {
 			System.out.println( report );
 		}
 	}
 
 }
