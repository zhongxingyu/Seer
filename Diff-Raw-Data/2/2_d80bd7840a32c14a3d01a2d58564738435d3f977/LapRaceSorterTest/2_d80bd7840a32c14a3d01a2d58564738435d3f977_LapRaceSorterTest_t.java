 package unittest;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import regressiontest.ListTest;
 import regressiontest.PVGRunner;
 
 import enduro.LapRaceSorter;
 
 @ListTest()
 @RunWith(PVGRunner.class)
 public class LapRaceSorterTest {
 
 	@Test
 	public void testReadWriteStartTimeFile() {
 		assertTrue(PVGRunner.testSuccess);
 	}
 
 	@Test
 	public void testImpossibleLap() {
 
 		try {
			BufferedReader in = new BufferedReader(new FileReader("result.temp.lap"));
 			in.readLine();
 			assertEquals(
 					"StartNr; Namn; #Varv; TotalTid; Varv1; Varv2; Varv3; Start; Varvning1; Varvning2; Mål",
 					in.readLine());
 			in.readLine();
 			assertEquals(
 					"2; Bengt Bsson; 3; 01.15.16; 00.14.00; 00.27.00; 00.34.16; 12.00.00; 12.14.00; 12.41.00; 13.15.16; Omöjlig varvtid?",
 					in.readLine());
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
