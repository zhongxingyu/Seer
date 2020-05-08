 /**
  * TODO: PROPER TESTING
  * TODO: STATICTICS CALCULATION!
  * @author Thomas
  *
  */
 @Creator(name = "Thomas", lastUpdate = "08.12.2012")
 public class BauernhofTester implements Tester {
 
 	@Override
 	@Creator(name = "Thomas", lastUpdate = "08.12.2012")
 	public void runTests() {
 		Bauernhof b = new Bauernhof("Tony's Freilandeier");
 
 		Traktor bio = new BiogasTraktor("Bio #1", 15);
 		bio.setEinsatzart(new Duengerstreuer(5.5));
 
 		Traktor t = new DieselTraktor("Diesel #1", 14);
 
 		Traktor t2 = new DieselTraktor("Diesel #2", 1);
 		t2.setEinsatzart(new Duengerstreuer(6.5));
 
 		Traktor t3 = new DieselTraktor("Diesel #3", 3);
 		t3.setEinsatzart(new Drillmaschine(10));
 
 		Traktor t4 = new DieselTraktor("Diesel #4", 4);
 		t4.setEinsatzart(new Drillmaschine(1));
 
 		Traktor t5 = new BiogasTraktor("Bio #5", 5);
 		t5.setEinsatzart(new Drillmaschine(2));
 
 		Traktor t6 = new BiogasTraktor("Bio #6", 6);
 		t6.setEinsatzart(new Drillmaschine(15));
 
 		b.addTraktor(t);
 		b.addTraktor(t2);
 		b.addTraktor(t3);
 		b.addTraktor(t4);
 		b.addTraktor(t5);
 		b.addTraktor(t6);
 		b.addTraktor(bio);
 		
 		System.out.println(b);
 
 		b.increaseHoursOfTraktor(15, 10);
		b.increaseFuelUsage(14, 10);
		b.increaseFuelUsage(15, 10);
		b.increaseFuelUsage(10, 10);
		b.increaseFuelUsage(1, 5);
		b.increaseFuelUsage(4, 3);
		b.increaseFuelUsage(15, 10);
		b.increaseFuelUsage(14, 10);
		b.increaseFuelUsage(10, 10);
		b.increaseFuelUsage(5, 20);
		b.increaseFuelUsage(3, 25);
 
 		b.changeUsageOfTraktor(14, new Drillmaschine(10));
 
 		System.out.println(b);
 
 		System.out.println(b.getSaescharenStats());
 
 		System.out.println(b.getCapacityStats());
 		
 		b.changeUsageOfTraktor(14, null);
 		b.changeUsageOfTraktor(5, null);
 		
 		System.out.println(b.avgDiesel());
 		
 		System.out.println(b.avgGas());
 
 	}
 
 }
