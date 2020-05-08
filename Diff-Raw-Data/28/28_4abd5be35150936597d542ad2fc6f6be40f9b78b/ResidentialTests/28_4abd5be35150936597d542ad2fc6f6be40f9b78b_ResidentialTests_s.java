 package fowler.energybilling.sites;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 
 import org.junit.*;
 
 import fowler.energybilling.Dollars;
 import fowler.energybilling.NoReadingsException;
 import fowler.energybilling.Reading;
 import fowler.energybilling.Zone;
 
 /* Jana Koehler, Dec 13 2010
  * Several things one can do with this example code
  * - rebuild the class hierarchy and improve the charge methods as in Fowler's original sample text
  * - get rid of the deprecated Date class, probably replace it by Calendar
  * - fix the summerdays calculation in disabilitysite.charge as it gives negative values
  * - improve and embed a rounding method into Dollars calculations)
  * - improve on the tests in general, for example set up a suite and have separate test classes, one for each site
  */
 
 
 
 public class ResidentialTests {
 	
 	@Test
 	public void ResidentialSite199Summer() throws NoReadingsException {
 		Zone zoneA = new Zone("A", 0.06, 0.07, new Date(1997, 5, 15), new Date(
 				1997, 9, 10));
 		ResidentialSite subject = new ResidentialSite(zoneA);
 		subject.addReading(new Reading(300, new Date(1997, 6, 15)));
 		subject.addReading(new Reading(499, new Date(1997, 31, 8)));
		//System.out.println("199SummerCharge is: "+subject.charge().getAmount());
		assertEquals(new Dollars(4.32).getAmount(), subject.charge().getAmount());
 	}
 	
 	
 	@Test
 	public void ResidentialSite4000WholeYear() throws NoReadingsException {
 		
 		Zone zoneB = new Zone("B", 0.07, 0.06, new Date(1997, 6, 5), new Date(
 				1997, 8, 31));
 		
 		ResidentialSite subject = new ResidentialSite(zoneB);
 		subject.addReading(new Reading(1000, new Date(1997, 1, 1)));
 		subject.addReading(new Reading(5000, new Date(1997, 12, 31)));
		//System.out.println("4000WholeYearCharge is: "+subject.charge().getAmount());
		assertEquals(new Dollars(713.5).getAmount(), subject.charge().getAmount());
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 	}
 
 }
