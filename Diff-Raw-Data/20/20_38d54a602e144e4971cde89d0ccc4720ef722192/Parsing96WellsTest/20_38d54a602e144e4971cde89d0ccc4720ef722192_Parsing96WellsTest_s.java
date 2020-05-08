 package net.bioclipse.brunn.tests.results;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import net.bioclipse.brunn.pojos.AbstractOperation;
 import net.bioclipse.brunn.pojos.Measurement;
 import net.bioclipse.brunn.pojos.Plate;
 import net.bioclipse.brunn.pojos.Result;
 import net.bioclipse.brunn.results.PlateRead;
 import net.bioclipse.brunn.results.parser96.Parser96;
 import net.bioclipse.brunn.tests.BaseTest;
 
 public class Parsing96WellsTest extends BaseTest {
 	
 	private List<Plate> plates;
 	private final String BARCODE = "barcode";
 
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		plates = new ArrayList<Plate>();
 		plates.add(plate);
 		plate.setBarcode(BARCODE);
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	@Test
 	public void test96Well1() throws FileNotFoundException {
 		
 		String dataFilePath = ClassLoader
 			                  .getSystemResource( "TestFiles" + File.separator + 
 			            		                  "96Well1.txt").getPath();
 		assertNotNull("Could not find datafile", dataFilePath);
 		Parser96 parser = new Parser96(new File(dataFilePath));
 		List<PlateRead> plateReads = parser.getPlatesInFile();
 		assertNotNull(plateReads);
 		assertEquals( 1, plateReads.size() );
 		assertEquals( "OK", plateReads.get(0).getError() );
 		plateReads.get(0).setBarCode(BARCODE);
 		parser.addResultsTo(tester, plates);
 		double resultValue = 0;
 		for( AbstractOperation op : plates.get(0).getWell(2, 'b').getSampleContainer().getWorkList().getAbstractOperations() ) {
 			resultValue = ((Measurement)op).getResults().toArray(new Result[0])[0].getResultValue()[0];
 		}
 		assertEquals( 272, resultValue);
 	}
 	
 	@Test
 	public void test96Well2() throws FileNotFoundException {
 
 		String dataFilePath = ClassLoader
                               .getSystemResource( "TestFiles" + File.separator + 
           		                                  "96Well2.txt").getPath();
         assertNotNull("Could not find datafile", dataFilePath);
         Parser96 parser = new Parser96(new File(dataFilePath));
         List<PlateRead> plateReads = parser.getPlatesInFile();
         assertNotNull(plateReads);
         assertEquals( 1, plateReads.size() );
         assertEquals( "OK", plateReads.get(0).getError() );
         plateReads.get(0).setBarCode(BARCODE);
         parser.addResultsTo(tester, plates);
         double resultValue = 0;
         for( AbstractOperation op : plates.get(0).getWell(2, 'b').getSampleContainer().getWorkList().getAbstractOperations() ) {
         	resultValue = ((Measurement)op).getResults().toArray(new Result[0])[0].getResultValue()[0];
         }
         assertEquals( 8491, resultValue);
 	}
 	
 	@Test
 	public void test97Well3() throws FileNotFoundException {
 		String dataFilePath = ClassLoader
                               .getSystemResource( "TestFiles" + File.separator + 
                                                   "96Well3.txt").getPath();
         assertNotNull("Could not find datafile", dataFilePath);
         Parser96 parser = new Parser96(new File(dataFilePath));
         List<PlateRead> plateReads = parser.getPlatesInFile();
         assertNotNull(plateReads);
         assertEquals( 1, plateReads.size() );
         assertEquals( "OK", plateReads.get(0).getError() );
         plateReads.get(0).setBarCode(BARCODE);
         parser.addResultsTo(tester, plates);
         double resultValue = 0;
         for( AbstractOperation op : plates.get(0).getWell(2, 'b').getSampleContainer().getWorkList().getAbstractOperations() ) {
         	resultValue = ((Measurement)op).getResults().toArray(new Result[0])[0].getResultValue()[0];
         }
        assertEquals( 8491, resultValue);
 	}
 }
