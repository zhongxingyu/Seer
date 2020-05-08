 package unittest;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
import java.io.FileNotFoundException;
 import java.io.FileReader;
import java.util.ArrayList;

import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import regressiontest.ListTest;
 import regressiontest.PVGRunner;
 
import enduro.LapRaceSorter;
import enduro.LapRaceSorter;

 @ListTest(sorter=ListTest.Sorter.lap)
 @RunWith(PVGRunner.class)
 public class LapseSorterTest {
 
 	@Test
 	public void testReadWriteTimeFile() {
 		try {
 			assertTrue(PVGRunner.testSuccess);
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 
 	}
 
 	@Test
 	public void testImpossibleLap() {
 		try {
 			BufferedReader in = new BufferedReader(new FileReader("result.temp"));
 			
 			in.readLine();
 			in.readLine();
 			in.readLine();
			//System.out.println(in.readLine());
			assertEquals("2; Bengt Bsson; 3; 01.15.16; 00.14.00; 00.27.00; 00.34.16; 12.00.00; 12.14.00; 12.41.00; 13.15.16; Omöjlig varvtid?", in.readLine());
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
