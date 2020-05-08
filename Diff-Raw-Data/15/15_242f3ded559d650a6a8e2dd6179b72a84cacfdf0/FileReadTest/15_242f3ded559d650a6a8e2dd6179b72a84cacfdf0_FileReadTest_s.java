
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 
 public class FileReadTest {
 
 	@Test
 	public void test() {
 		//Filename containing 4 test papers
 		String filepath = "filepath";
<<<<<<< HEAD
 		PaperCollection testCollection = null;
 		try {
 			testCollection = new PaperCollection(filepath);
 		} catch (IOException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Assert.assertEquals(testCollection.size(), 4);
 	}	
 }
=======
		PaperCollection testCollection = new PaperCollection(filepath);
		
		Assert.assertEquals(testCollection.size(), 4);
	}

}
>>>>>>> e972512aa0199e92921f0a127a5ee7f647517272
