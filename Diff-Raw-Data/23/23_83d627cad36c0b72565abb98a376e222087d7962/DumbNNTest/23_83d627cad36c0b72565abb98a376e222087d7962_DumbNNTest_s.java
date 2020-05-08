 package graphrep;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 
 import org.junit.Test;
 
 public class DumbNNTest {
 
 	@Test
 	public final void testGetIDForCoordinates() {
 
 		GraphRepTextReader graphRepTextReader = new GraphRepTextReader();
 		String testFile = new String(
 				"3\n5\n0 25 15 5\n 50 30 4\n2 20 40 3\n0 1 5 0.5\n0 2 2 0.5\n1 0 5 0.5\n1 2 3 0.5\n2 0 2 0.5");
 		byte[] testFileBytes = testFile.getBytes();
 		ByteArrayInputStream testFileByteArrayStream = new ByteArrayInputStream(
 				testFileBytes);
 
 		// TODO Rework test case, adjust to changed graphrep, maybe no try/catch
 		// here
 		try {
			GraphRep graphRep = graphRepTextReader
 					.createGraphRep(testFileByteArrayStream);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 		}
 
 	}
 }
