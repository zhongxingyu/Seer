 
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 
 
 @RunWith(Suite.class)
 @SuiteClasses( {TourFileFilterTest.class,
 				TspFileFilterTest.class,
 				TspBerechnungsdatenTest.class,
 				TspEinlesenTest.class,
 				AmeiseTest.class,
 				AbbruchbedingungenTest.class,
				LoesungEinlesenTest.class})
 
 
 public class TestAlle {
 
 	public static final String TESTDATEN = "./src/test/resources";
 	
 	public static final int INITIAL_PHEROMONWERT = 10;
 	public static final double GEWICHTUNG_PHEROMON = 0.5;
 
 }
