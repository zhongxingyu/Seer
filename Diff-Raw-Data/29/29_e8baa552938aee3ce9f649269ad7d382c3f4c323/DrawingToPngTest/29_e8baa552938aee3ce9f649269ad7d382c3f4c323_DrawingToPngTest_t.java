 package pt.inevo.encontra.drawing.descriptors.test;
 
 import junit.framework.TestCase;
 import org.junit.Test;
 import pt.inevo.encontra.drawing.Drawing;
 import pt.inevo.encontra.drawing.DrawingFactory;
 
 import javax.imageio.ImageIO;
 import java.io.File;
 import java.io.IOException;
 
 
 public class DrawingToPngTest extends TestCase {
 
     Drawing drawing;
     String testFilePath;
    String inputFilename = "/dandelionmood_Light_Bulb_1.svg";
    String outputFilename = "dandelionmood_Light_Bulb_1";
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         testFilePath = getClass().getResource(inputFilename).getPath();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
 
     }
 
     @Test
     public void testTopogeoExtractor() throws IOException {
         //creating the drawing from the svg file
         drawing = DrawingFactory.getInstance().drawingFromSVG(testFilePath);
         ImageIO.write(drawing.getImage(), "PNG", new File(outputFilename + ".png"));
     }
 }
 
