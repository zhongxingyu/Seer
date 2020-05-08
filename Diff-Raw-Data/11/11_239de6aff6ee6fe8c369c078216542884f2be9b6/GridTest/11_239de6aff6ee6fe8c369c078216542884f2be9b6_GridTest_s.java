 package edu.msoe.se2800.h4.jplot.grid;
 
 import edu.msoe.se2800.h4.jplot.Constants;
 import edu.msoe.se2800.h4.jplot.JPlot;
 import junit.framework.Assert;
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.testng.testcase.FestSwingTestngTestCase;
 import org.testng.annotations.Test;
 
 import java.awt.*;
 import java.io.File;
 
 public class GridTest extends FestSwingTestngTestCase{
 	
 	private Grid g = new Grid();
     private FrameFixture thisWindow;
    //private JFrame jf = new JFrame();
	/*
	@BeforeClass
    public void setupGridTesting() {
 
        // Ensure that we dont get null pointer exceptions from Grid child views not being initialized
        g.initSubviews();
        jf.add(g);

    }
    */
     @Override
     protected void onSetUp() {
         JPlot frame = GuiActionRunner.execute(new GuiQuery<JPlot>() {
             protected JPlot executeInEDT() {
                 return new JPlot(Constants.GridMode.ADMINISTRATOR_MODE, new Grid());
             }
         });
 
         thisWindow = new FrameFixture(robot(), frame);
         thisWindow.show();
     }
 
     @Test(description = "Test to verify that the background has been painted correctly")
     public void paintComponentTest(){
         thisWindow.background().requireEqualTo(new Color(238,238,238));
     }
 
 	/**
 	 * makes sure the getters and setters for loadedFile work
 	 */
 	@Test(description =  "Testing getters and setters for loadedFile")
 	public void testLoadedFile() {
 		File f = new File("file.txt");
 		g.setLoadedPathFile(f);
 		Assert.assertEquals(f, g.getLoadedPathFile());
 	}
 }
