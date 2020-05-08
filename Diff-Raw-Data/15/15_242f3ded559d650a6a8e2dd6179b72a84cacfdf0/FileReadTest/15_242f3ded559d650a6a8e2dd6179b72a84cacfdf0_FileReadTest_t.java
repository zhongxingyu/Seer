 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 
 public class FileReadTest {
 
 	@Test
 	public void test() {
 		//Filename containing 4 test papers
 		String filepath = "filepath";

 		PaperCollection testCollection = null;
 		try {
 			testCollection = new PaperCollection(filepath);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		Assert.assertEquals(testCollection.size(), 4);
 	}	
 }
