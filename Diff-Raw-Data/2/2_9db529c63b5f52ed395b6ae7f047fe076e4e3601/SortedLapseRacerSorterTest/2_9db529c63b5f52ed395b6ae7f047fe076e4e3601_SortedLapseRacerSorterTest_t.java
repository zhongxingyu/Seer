 package unittest.racer;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import enduro.racer.Racer;
 import enduro.racer.RacerSorter;
 import enduro.racer.Time;
 import enduro.racer.comparators.RunnerCheckTotalTimeMax;
 import enduro.racer.comparators.RunnerLapseComparator;
 import enduro.racer.comparators.RunnerNumberComparator;
 import enduro.racer.comparators.RunnerTotalTimeComparator;
 import enduro.racer.printer.LapRacePrinter;
 import enduro.racer.printer.SortedLapRacePrinter;
 
 public class SortedLapseRacerSorterTest {
 
 	private Racer racer1, racer2, racer103;
 	private RacerSorter sorter;
 	
 	@Before public void bef() {
 		
 		SortedLapRacePrinter printer = new SortedLapRacePrinter();
 		printer.setHeaderInformation(new String[]{"startNr", "Namn", "Klubb", "annat"});
 		
 		racer1 = new Racer(new String("1; Anders Asson; FMCK Astad; ATM").split("; "));
 		racer1.addFinishTime(new Time("12.30.00"));
 		racer1.addFinishTime(new Time("13.23.34"));
 		racer1.addFinishTime(new Time("13.00.00"));
 		racer1.addStartTime(new Time("12.00.00"));
 		
 		racer2 = new Racer(new String("2; Bengt Bsson; FMCK Bstad; BTM").split("; "));
 		racer2.addFinishTime(new Time("12.15.01"));
 		racer2.addFinishTime(new Time("12.41.00"));
 		racer2.addFinishTime(new Time("13.15.16"));
 		racer2.addStartTime(new Time("12.00.00"));
 		
 		racer103 = new Racer(new String("103; Erik Esson; Estad MCK; ETM").split("; "));
 		racer103.addFinishTime(new Time("12.44.00"));
 		racer103.addFinishTime(new Time("12.24.00"));
 		racer103.addFinishTime(new Time("13.16.07"));
 		racer103.addStartTime(new Time("12.00.00"));
 		racer103.addStartTime(new Time("12.15.00"));
 		
 		sorter = new RacerSorter("random group", new RunnerCheckTotalTimeMax(new RunnerLapseComparator(new RunnerTotalTimeComparator(new RunnerNumberComparator()))), printer);
 	}
 	
 	@Test public void testAll() {
 		sorter.addRacer(racer1);
 		sorter.addRacer(racer2);
 		
 		String[] out = sorter.print().split("\n");
 		
		assertEquals("Plac; startNr; Namn; Klubb; annat; #Varv; Totaltid; Varv1; Varv2; Varv3", out[1]);
 		assertEquals("1; 2; Bengt Bsson; FMCK Bstad; BTM; 3; 01.15.16; 00.15.01; 00.25.59; 00.34.16", out[2]);
 		assertEquals("2; 1; Anders Asson; FMCK Astad; ATM; 3; 01.23.34; 00.30.00; 00.30.00; 00.23.34", out[3]);
 		
 	}
 	
 }
