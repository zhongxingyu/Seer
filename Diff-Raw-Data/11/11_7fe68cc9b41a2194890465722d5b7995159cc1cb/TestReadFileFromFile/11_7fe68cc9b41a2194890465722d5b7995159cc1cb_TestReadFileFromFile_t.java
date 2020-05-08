 package test;
 
 import java.io.FileNotFoundException;
 
import race.LapRace;
 import reader.ReadFinishFile;
 import reader.ReadNameFile;
 import reader.ReadStartFile;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestReadFileFromFile {
 	private ReadStartFile rsf;
 	private ReadFinishFile rff;
 	private ReadNameFile rnf;
 
 	public TestReadFileFromFile() {
		rsf = new ReadStartFile(new LapRace("Unknown", "Unknown", "Unknown",
 				"", "", 0, ""), "Unknown");
		rff = new ReadFinishFile(new LapRace("Unknown", "Unknown", "Unknown",
 				"", "", 0, ""), "Unknown");
		rnf = new ReadNameFile(new LapRace("Unknown", "Unknown", "Unknown",
 				"", "", 0, ""), "Unknown");
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test(expected = FileNotFoundException.class)
 	public void TestReadStartFileThrowsNoSuchFileException()
 			throws FileNotFoundException {
 		System.out.println("Expected: \"Hittade inte startfilen\"");
 		try {
 			rsf.readFile();
 		} catch (FileNotFoundException e) {
 			throw e;
 		}
 	}
 
 	@Test(expected = FileNotFoundException.class)
 	public void TestReadFinishFileThrowsNoSuchFileException()
 			throws FileNotFoundException {
 		System.out.println("Expected: \"Hittade inte finishfilen\"");
 		try {
 			rff.readFile();
 		} catch (FileNotFoundException e) {
 			throw e;
 		}
 	}
 
 	@Test(expected = FileNotFoundException.class)
 	public void TestReadNameFileThrowsNoSuchFileException()
 			throws FileNotFoundException {
 		System.out.println("Expected: \"Hittade inte namnfilen\"");
 		try {
 			rnf.readFile();
 		} catch (FileNotFoundException e) {
 			throw e;
 		}
 	}
 
 }
