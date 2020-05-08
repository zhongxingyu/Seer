 package server.operations;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import server.entities.Timetable;
 import server.exceptions.ScheduleCreationException;
 import server.resources.TimetableResource;
 
 /**
  * Test for file printing.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 public class FilePrinterTest extends TestCase {
 
 	private FilePrinter printer;
 	private CacheCleaner cleaner;
 	private Timetable timeTable;
 
 	@Override
 	@Before
 	public void setUp() {
 		this.printer = new FilePrinter();
 		this.cleaner = new CacheCleaner();
 		this.timeTable = new TimetableResource().getClassTimetable(1);
 	}
 
 	@Test
 	public void testprintAsPDF() {
 		try {
 			FilePrinterTest.assertNotNull(this.printer.printAsPDF(this.timeTable));
 		} catch (final ScheduleCreationException e) {
 			FilePrinterTest.fail();
 		}
 	}
 
 	@Test
 	public void testprintAsPNG() {
 		String pngFileName = System.getProperty("user.home") + System.getProperty("file.separator") + "timeTable.png";
 		File f = new File(pngFileName);
 		FilePrinterTest.assertTrue(f.exists());
 	}
 
 	@After
 	public void cleanUpTestData() {
 		this.cleaner.cleanCache();
 	}
 }
