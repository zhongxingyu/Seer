 package test;
 
 import static org.junit.Assert.*;
 import main.Driver;
 import main.Sorter;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 public class TestSorter extends Sorter {
 
 	public TestSorter() {
 		super("", "", null);
 	}
 
 	@Before
 	public void setUp() {
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void testAddToTreeMap() {
 		Integer i = new Integer(1);
 		String time = "01.01.01";
 		super.addStartTime(i, time);
 		assertEquals(1, register.size());
 	}
 
 	@Test
 	public void testFirstRowAndFileWasCreated() {
 
 		try {
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
			assertEquals("StartNr; Namn; Totaltid; Starttid; Måltid", sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 
 			e.printStackTrace();
 		}
 
 	}
 
 	@Test
 	public void testOneDriverResult() {
 		try {
 			Integer i = new Integer(1);
 			String start = "12.00.00";
 			super.addStartTime(i, start);
 			String finish = "13.23.34";
 			super.addFinishTime(i, finish);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			assertEquals("1; Namn?; 1.23.34; 12.00.00; 13.23.34", sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testTwoDriverResult() {
 		try {
 			Integer i = new Integer(1);
 			String start = "12.00.00";
 			super.addStartTime(i, start);
 			String finish = "13.23.34";
 			super.addFinishTime(i, finish);
 			i = new Integer(2);
 			start = "12.01.00";
 			super.addStartTime(i, start);
 			finish = "13.15.16";
 			super.addFinishTime(i, finish);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			sc.nextLine();
 			assertEquals("2; Namn?; 1.14.16; 12.01.00; 13.15.16", sc.nextLine());
 			sc.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testAddStartTime() {
 		Integer i = new Integer(1);
 		String time = "01.01.01";
 		super.addStartTime(i, time);
 		Driver driver = register.get(i);
 		assertEquals("01.01.01", driver.startTime().get(0));
 	}
 
 	@Test
 	public void testAddFinishTime() {
 		Integer i = new Integer(1);
 		String time = "02.02.00";
 		super.addFinishTime(i, time);
 		Driver driver = register.get(i);
 		assertEquals("02.02.00", driver.finishTime().get(0));
 
 	}
 
 	@Test
 	public void testAddFirstStartThenFinishTimes() {
 		Integer i = new Integer(1);
 		String startTime = "01.01.01";
 		String finishTime = "02.02.02";
 		super.addStartTime(i, startTime);
 		super.addFinishTime(i, finishTime);
 		Driver driver = register.get(i);
 		assertEquals("Wrong start time", "01.01.01", driver.startTime().get(0));
 		assertEquals("Wrong finish time", "02.02.02", driver.finishTime().get(0));
 	}
 
 	@Test
 	public void testAddFirstFinishThenStartTimes() {
 		Integer i = new Integer(1);
 		String startTime = "01.01.01";
 		String finishTime = "02.02.02";
 		super.addFinishTime(i, finishTime);
 		super.addStartTime(i, startTime);
 		Driver driver = register.get(i);
 		assertEquals("Wrong start time", "01.01.01", driver.startTime().get(0));
 		assertEquals("Wrong finish time", "02.02.02", driver.finishTime().get(0));
 	}
 
 	@Test
 	public void testNoFinishTime() {
 		try {
 			Integer i = new Integer(1);
 			String start = "12.00.00";
 			super.addStartTime(i, start);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			assertEquals("1; Namn?; --.--.--; 12.00.00; Slut?", sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testNoStartTime() {
 		try {
 			Integer i = new Integer(1);
 			String finish = "12.00.00";
 			super.addFinishTime(i, finish);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			assertEquals("1; Namn?; --.--.--; Start?; 12.00.00", sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test(expected = FileNotFoundException.class)
 	public void TestReadStartFileThrowsNoSuchFileException()
 			throws FileNotFoundException {
 		try {
 			super.readStartFile();
 		} catch (FileNotFoundException e) {
 			throw e;
 		}
 	}
 
 	@Test(expected = FileNotFoundException.class)
 	public void TestReadFinishFileThrowsNoSuchFileException()
 			throws FileNotFoundException {
 		try {
 			super.readFinishFile();
 		} catch (FileNotFoundException e) {
 			throw e;
 		}
 	}
 
 	@Test
 	public void testMultipleStartTimes() {
 		try {
 			Integer i = new Integer(1);
 			String start = "12.00.00";
 			super.addStartTime(i, start);
 			start = "12.00.30";
 			super.addStartTime(i, start);
 			String finish = "13.00.00";
 			super.addFinishTime(i, finish);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			assertEquals(
 					"1; Namn?; 1.00.00; 12.00.00; 13.00.00; Flera starttider? 12.00.30",
 					sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	@Test
 	public void testMultipleFinishTimes(){
 		try {
 			Integer i = new Integer(1);
 			String start = "12.00.00";
 			super.addStartTime(i, start);
 			String finish = "13.00.00";
 			super.addFinishTime(i, finish);
 			finish = "13.00.30";
 			super.addFinishTime(i, finish);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			assertEquals(
 					"1; Namn?; 1.00.00; 12.00.00; 13.00.00; Flera måltider? 13.00.30",
 					sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	@Test
 	public void testImpossibleFinishTimeSmaller(){
 		try {
 			Integer i = new Integer(1);
 			String start = "12.00.00";
 			super.addStartTime(i, start);
 			String finish = "12.14.00";
 			super.addFinishTime(i, finish);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			assertEquals(
 					"1; Namn?; 0.14.00; 12.00.00; 12.14.00; Omöjlig Totaltid?",
 					sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	@Test
 	public void testOkeyFinishTime(){
 		try {
 			Integer i = new Integer(1);
 			String start = "12.00.00";
 			super.addStartTime(i, start);
 			String finish = "12.15.00";
 			super.addFinishTime(i, finish);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			assertEquals(
 					"1; Namn?; 0.15.00; 12.00.00; 12.15.00",
 					sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	@Test
 	public void testNoName(){
 		try {
 			Integer i = new Integer(1);
 			String start = "12.00.00";
 			super.addStartTime(i, start);
 			String finish = "12.15.00";
 			super.addFinishTime(i, finish);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			assertEquals(
 					"1; Namn?; 0.15.00; 12.00.00; 12.15.00",
 					sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testDriverWithNameWithoutTimes(){
 		try {
 			Integer i = new Integer(1);
 			String name = "Test";
 			Driver d=  new Driver(name);
 			register.put(i, d);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			assertEquals(
 					"1; Test; --.--.--; Start?; Slut?",
 					sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	@Test
 	public void testMultipleStartAndMultipleFinishTimes(){
 		try {
 			Integer i = new Integer(1);
 			String name = "Test";
 			Driver d=  new Driver(name);
 			register.put(i, d);
 			String start = "12.00.00";
 			super.addStartTime(i, start);
 			start = "12.00.01";
 			super.addStartTime(i, start);
 			String finish = "12.00.01";
 			super.addFinishTime(i, finish);
 			finish = "13.00.30";
 			super.addFinishTime(i, finish);
 			writeResultFile("Result.txt");
 			Scanner sc = new Scanner(new File("Result.txt"));
 			sc.nextLine();
 			assertEquals(
 					"1; Test; 0.00.01; 12.00.00; 12.00.01; Flera starttider? 12.00.01; Flera måltider? 13.00.30; Omöjlig Totaltid?",
 					sc.nextLine());
 			sc.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 }
 
