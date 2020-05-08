 package unittest.printers;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import enduro.racer.Racer;
 import enduro.racer.Time;
 import enduro.racer.configuration.ConfigParser;
 import enduro.racer.printer.LapRacePrinter;
 
 public class LapRacePrinterTest {
 
 	private Racer racer;
 	private LapRacePrinter printer;
 	
 	@Before public void doBefore() {
 		ConfigParser.delete();
 		printer = new LapRacePrinter();
 		printer.setHeaderInformation(new String[]{"startNr", "Namn", "Klubb", "annat"});
 	}
 	
 	@Test public void testNoErrors() {
 		racer = new Racer(new String("1; Anders Asson; FMCK Astad; ATM").split("; "));
 		racer.addFinishTime(new Time("12.30.00"), 1);
 		racer.addFinishTime(new Time("13.23.34"), 1);
 		racer.addFinishTime(new Time("13.00.00"), 1);
 		racer.addStartTime(new Time("12.00.00"), 1);
 		
 		assertEquals("1; Anders Asson; FMCK Astad; ATM; 3; 01.23.34; 00.30.00; 00.30.00; 00.23.34; 12.00.00; 12.30.00; 13.00.00; 13.23.34", printer.print(racer, null));
 	}
 	
 	@Test public void testImpossibleLapse() {
 		racer = new Racer(new String("2; Bengt Bsson; FMCK Bstad; BTM").split("; "));
 		racer.addFinishTime(new Time("12.14.00"), 1);
 		racer.addFinishTime(new Time("12.41.00"), 1);
 		racer.addFinishTime(new Time("13.15.16"), 1);
 		racer.addStartTime(new Time("12.00.00"), 1);
 		
 		assertEquals("2; Bengt Bsson; FMCK Bstad; BTM; 3; 01.15.16; 00.14.00; 00.27.00; 00.34.16; 12.00.00; 12.14.00; 12.41.00; 13.15.16; Omöjlig varvtid?", printer.print(racer, null));
 	}
 	
 	@Test public void testManyStarts() {
 		racer = new Racer(new String("103; Erik Esson; Estad MCK; ETM").split("; "));
 		racer.addStartTime(new Time("12.00.00"), 1);
 		racer.addStartTime(new Time("12.15.00"), 1);
 		racer.addFinishTime(new Time("12.44.00"), 1);
 		racer.addFinishTime(new Time("12.24.00"), 1);
 		racer.addFinishTime(new Time("13.16.07"), 1);
 		
 		assertEquals("103; Erik Esson; Estad MCK; ETM; 3; 01.16.07; 00.24.00; 00.20.00; 00.32.07; 12.00.00; 12.24.00; 12.44.00; 13.16.07; Flera starttider? 12.15.00", printer.print(racer, null));
 	}
 	@Test public void testTooManyLaps() {
 		racer = new Racer(new String("103; Erik Esson; Estad MCK; ETM").split("; "));
 		racer.addStartTime(new Time("12.00.00"), 1);
 		racer.addFinishTime(new Time("12.15.00"), 1);
 		racer.addFinishTime(new Time("12.31.00"), 1);
 		racer.addFinishTime(new Time("13.16.07"), 1);
 		racer.addFinishTime(new Time("13.30.07"), 1);
 		
		assertEquals("103; Erik Esson; Estad MCK; ETM; 4; 01.30.07; 00.15.00; 00.16.00; 00.45.07; 12.00.00; 12.15.00; 12.31.00; ; för många varv 13.30.07", printer.print(racer, null));
 	}
 	@Test public void testMissingFinishTime() {
 		racer = new Racer(new String("103; Erik Esson; Estad MCK; ETM").split("; "));
 		racer.addStartTime(new Time("12.00.00"), 1);
 		
 		assertEquals("103; Erik Esson; Estad MCK; ETM; 0; --:--:--; ; ; ; 12.00.00; ; ;", printer.print(racer, null));
 	}
 
 }
