 public class Main {
 	
 	public static void main (String args[]) {
 		TivooSystem s = new TivooSystem();
		//s.loadDukeCal("http://www.cs.duke.edu/courses/cps108/current/assign/02_tivoo/data/dukecal.xml");
		s.loadGoogleCal("http://www.cs.duke.edu/courses/cps108/current/assign/02_tivoo/data/googlecal.xml");
 		s.outputSummaryAndDetailsPages("/Desktop");
 		System.out.println(s.myEvents.toString());
 		s.filterByKeyword("meet with");
 		//DateTime dt=new DateTime("2011-11-16T13:30:00");
 		//s.filterByTime(dt);
 		//s.outputSummaryAndDetailsPages("/Desktop");
 	}
 }
