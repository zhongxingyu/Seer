 package fi.helsinki.cs.scheduler3000.cli;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import fi.helsinki.cs.scheduler3000.model.Schedule;
 import fi.helsinki.cs.scheduler3000.model.Weekday.Day;
 import fi.helsinki.cs.scheduler3000.report.Report;
 import fi.helsinki.cs.scheduler3000.report.ReportFactory;
 
 public class NewReport extends CliCommand {
 
 	private Schedule schedule;
 	protected Report report = null;
 	
 	NewReport(Schedule schedule) {
 		this.schedule = schedule;
 	}
 
 	void run() {
 		while( this.report == null ) {
 			printReportDialog();
 		}
 	}
 	
 	
 	private static HashMap<String, Object> getOptions(String key, Day value) {
 		HashMap<String, Object> ret = new HashMap<String, Object>();
 		ret.put(key, value);
 		return ret;
 	}
 
 	private static HashMap<String, Object> getOptions(String key, ArrayList<Day> value) {
 		HashMap<String, Object> ret = new HashMap<String, Object>();
 		ret.put(key, value);
 		return ret;
 	}
 	
 	private Report printReportDialog() {
 		Character command = null;
 
 		while (true) {
 			System.out.print("Which type of report do you want to print? Options are: ");
 			for (ReportFactory.ReportType type : ReportFactory.ReportType.values()){
 				// make types the way every other option is shown to user
 				System.out.print("[" + type.toString().charAt(0) + "]" + type.toString().substring(1).toLowerCase()); 
 				System.out.print(" ");
 			}
 			System.out.println("[N]one");
 			printPrompt();
 			command = sanitize(input.nextLine());
 			String in = null;
 
 			switch (command) {
 
 			case 'd':
 
 				System.out.println("Which day you want to see your schedule for?");
 				Helpers.printDates();
 				printPrompt();
 				in = input.nextLine();
 				Day day = Helpers.getDay(in);
 				if (day == null){
					System.out.println("Invalid date");
 					break;
 				}
 				report = ReportFactory.makeReport(ReportFactory.ReportType.DAY, schedule, getOptions("day", day));
 
 			case 'f':
 				report = ReportFactory.makeReport(ReportFactory.ReportType.FULL, schedule, null); // full report doesen't need options
 
 			case 'w':
 				ArrayList<Day> days = new ArrayList<Day>();
 
 				System.out.println("Which days you want to include in this report? You can end with \""+endCommand+"\"");
 				System.out.println("One at the time, please");
 
 				while (true){
 					// print only available dates
 					Helpers.printDates(schedule);
 					printPrompt();
 
 					in = input.nextLine();
 					if (in.equals(endCommand)) {
 						break;
 					}
 					else {
 						// validate date
 						Day dayT = Helpers.getDay(in);
 						if( dayT == null || schedule.getDays().contains( (dayT) ) ) {
 							System.out.println("Unvalid date");
 						} else {
 							days.add( Helpers.getDay(in) );
 						}
 					}
 				}
 
 				report = ReportFactory.makeReport(ReportFactory.ReportType.WEEK, schedule, getOptions("days", days));
 
 
 			case 'n':
 				System.out.println("Returning back to main menu");
 				return null;
 				
 			default:
 				System.out.println("Cannot parse " + command);
 				break;
 			}
 
 		}
 	}
 
 }
