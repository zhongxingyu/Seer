 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import com.acmetelecom.lolClass;
 import com.acmetelecom.time.Date;
 import com.acmetelecom.time.Duration;
 import com.acmetelecom.time.Time;
 import com.acmetelecom.time.TimeStamp;
 
 
 public class Scratch {
 	
 	public static void main(String[] args) {
 //		String startTime = "10h4m3s";
 //		String endTime = "11h5m4s";
 //		
 //		String[] start = startTime.split("[h|m|s]");
 //		String[] end = endTime.split("[h|m|s]");
 //		
 //		System.out.println("Start");
 //		for (String string : start) {
 //			System.out.println(string);
 //		}
 //		
 //		System.out.println("End");
 //		for (String string : end) {
 //			System.out.println(string);
 //		}
 		
 		
 		
 		
 //		List<Customer> c = CentralCustomerDatabase.getInstance().getCustomers();
 //		TariffLibrary t = CentralTariffDatabase.getInstance();
 //		
 //		for (Customer customer : c) {
 //			System.out.println(customer.getFullName() + " " + customer.getPhoneNumber() + " " + customer.getPricePlan());
 //			System.out.println(t.tarriffFor(customer).offPeakRate() + " " + t.tarriffFor(customer).peakRate());
 //		}
 
 		
 		
 		
 		
		Time peakStart = new Time(7, 00, 00);
		Time peakEnd = new Time(19, 00, 00);
 		
		TimeStamp startTimeStamp = new TimeStamp(2000, 1, 1, 23, 59, 00);
		TimeStamp endTimeStamp = new TimeStamp(2000, 1, 2, 8, 00, 00);
 		
 		Time startTime = startTimeStamp.getTime();
 		Time endTime = endTimeStamp.getTime();
 		
 		Date startDate = startTimeStamp.getDate();
 		
 		long peakSeconds = 0;
 		long offpeakSeconds = 0;
 		
 		SortedSet<lolClass> t = new TreeSet<lolClass>();
 		
 		TimeStamp peakStartTimeStamp = new TimeStamp(peakStart, startDate);
 		TimeStamp peakEndTimeStamp = new TimeStamp(peakEnd, startDate);
 		
 		if (peakEnd.isBefore(peakStart) || peakEnd.isBefore(startTime)) {
 			peakEndTimeStamp = peakEndTimeStamp.addDay();
 		}
 		
 		if (peakStart.isBefore(startTime)){
 			peakStartTimeStamp = peakStartTimeStamp.addDay();
 		}
 		
 		t.add(new lolClass("start", peakStartTimeStamp));
 		t.add(new lolClass("end", peakEndTimeStamp));		
 		t.add(new lolClass("final", endTimeStamp));
 		
 		int i = 0;
 		for (lolClass e : t) {
 			System.out.println(i++ + " " + e.getType() + " " + e.getTime().getTime());
 		}
 		
 		System.out.println("startTime: " + startTime + ". endTime: " + endTime);
 		
 		TimeStamp startOfPeriod = startTimeStamp;
 		for (lolClass e : t) {
 			if (!e.getTime().isBefore(startTimeStamp)) {
 				if (e.getType() == "final") {
 					
 					if (e.getTime().isBetween(peakStartTimeStamp, peakEndTimeStamp)) {
 						System.out.println("A");
 						peakSeconds += Duration.inSeconds(startOfPeriod, e.getTime());
 					} else{
 						System.out.println("B");
 						offpeakSeconds += Duration.inSeconds(startOfPeriod, e.getTime());
 					}
 					
 					break;
 				}
 				
 				if (e.getType() == "start") {
 					System.out.println("C");
 					offpeakSeconds += Duration.inSeconds(startOfPeriod, e.getTime());
 				} else {
 					System.out.println("D");
 					peakSeconds += Duration.inSeconds(startOfPeriod, e.getTime());
 				}
 				
 				startOfPeriod = e.getTime();
 				System.out.println("peak seconds: " + peakSeconds + ". off-peak seconds: " + offpeakSeconds);
 				
 			}
 		}
 		
 //		if (endTime.isBefore(t.first().getTime())) {
 //			
 //			System.out.println("A");
 //			
 //			if (t.first().getType() == "start") {
 //				offpeakSeconds += Duration.inSeconds(startTime, endTime);
 //			} else {
 //				peakSeconds += Duration.inSeconds(startTime, endTime);
 //			}
 //			
 //		} else {
 //			
 //			Time startOfPeriod = startTime;
 //			Time endOfPeriod;
 //			
 //			//TODO Is this correct?
 //			String lastType = t.first().getType();
 //			
 //			for (lolClass e : t) {
 //				endOfPeriod = e.getTime();
 //				
 //				if (endTime.isBefore(endOfPeriod) || endTime.isEqual(endOfPeriod)) {
 //					
 //					System.out.println("B");
 //					
 //					if (e.getType() == "start" || (e.getType() == "final" && lastType == "end")) {
 //						offpeakSeconds += Duration.inSeconds(startOfPeriod, endTime);
 //					} else if (e.getType() == "end"  || (e.getType() == "final" && lastType == "start")) {
 //						peakSeconds += Duration.inSeconds(startOfPeriod, endTime);
 //					}
 //					
 //					break;
 //					
 //				} else {
 //					
 //					System.out.println("C");
 //					
 //					if (e.getType() == "start") {
 //						offpeakSeconds += Duration.inSeconds(startOfPeriod, endOfPeriod);
 //					} else {
 //						peakSeconds += Duration.inSeconds(startOfPeriod, endOfPeriod);
 //					}
 //					
 //				}
 //				
 //				startOfPeriod = endOfPeriod;
 //				lastType = e.getType();
 //			}
 //			
 //		}
 		
 		System.out.println("peak seconds: " + peakSeconds + ". off-peak seconds: " + offpeakSeconds);
 		
 	}
 	
 }
