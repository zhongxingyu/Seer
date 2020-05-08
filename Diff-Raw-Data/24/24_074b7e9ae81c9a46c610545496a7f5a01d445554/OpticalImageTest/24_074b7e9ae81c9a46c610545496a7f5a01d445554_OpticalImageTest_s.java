 package phd.image;
 
import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.Test;
 
 import phd.image.filter.StreetsFilter;
 
 public class OpticalImageTest {
 
 	@Test
 	public void test() throws IOException {
		OpticalImage opticalImage = new OpticalImage();
		opticalImage.setFile(new File("src/test/resources/testImage.jpg"));
		opticalImage.setResultFileName("src/test/resources/resultImage.jpg");
 
 		StreetsFilter filter = new StreetsFilter();
		filter.filter(opticalImage);
 
 		assertTrue(new File("src/test/resources/resultImage.jpg").exists());
 	}
 
 }
