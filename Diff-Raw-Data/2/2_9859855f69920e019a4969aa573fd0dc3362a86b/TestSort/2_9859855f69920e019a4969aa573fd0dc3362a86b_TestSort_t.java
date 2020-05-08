 package test;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Scanner;
 
 import org.junit.*;
 
 import sort.Competitor;
 import sort.Sorter;
 import sort.SorterMain;
 import sort.Time;
 import static org.junit.Assert.*;
 
 public class TestSort {
 	List<Competitor> competitors;
 
 	@Before
 	public void Initialize() {
 		competitors = new ArrayList<Competitor>();
 	}
 
 	/**
 	 * Private helpmethod for testing the first line in the resultfile.
 	 * 
 	 * @param scan
 	 *            The scanner reading the resultfile.
 	 */
 	private void testFirstLineInResult(Scanner scan) {
 		String firstLine = "StartNr; TotalTid; StartTid; Måltid";
 
 		assertTrue(scan.hasNext());
 		assertEquals("First line is missing, empty result list",
 				scan.nextLine(), firstLine);
 	}
 
 	@Test
 	public void testSorterCreatesFile() {
 		Competitor fastest = new Competitor(3);
 		Competitor slowest = new Competitor(1);
 		Competitor secondFastest = new Competitor(2);
 		
 		fastest.addStartTime(new Time(10000));
 		fastest.addFinishTime(new Time(30000));
 		
 		slowest.addStartTime(new Time(20000));
 		slowest.addFinishTime(new Time(70000));
 		
 		secondFastest.addStartTime(new Time(30000));
 		secondFastest.addFinishTime(new Time(60000));
 		
 		competitors.add(secondFastest);
 		competitors.add(slowest);
 		competitors.add(fastest);
 		
 		File file = new File("sorted_result.txt");
 
 		Scanner scan = null;
 		try {
 			scan = new Scanner(file);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Collections.sort(competitors);
 		
		SorterMain.printResults(competitors, "sorted_result.txt");
 		
 		testFirstLineInResult(scan);
 		
 		assertTrue(file.exists());
 		
 		assertTrue(scan.hasNext());
 		assertEquals(scan.nextLine(), fastest.toString());
 		
 		assertTrue(scan.hasNext());
 		assertEquals(scan.nextLine(), secondFastest.toString());
 		
 		assertTrue(scan.hasNext());
 		assertEquals(scan.nextLine(), slowest.toString());
 	}
 
 
 }
