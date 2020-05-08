 package edu.msoe.se2800.h4.jplot;
 
 import edu.msoe.se2800.h4.administrationFeatures.DatabaseConnection;
 import edu.msoe.se2800.h4.jplot.grid.Grid;
 
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.testng.testcase.FestSwingTestngTestCase;
 import org.testng.annotations.Test;
 
 public class StatsPanelTest extends FestSwingTestngTestCase {
     
     private FrameFixture mWindow;
 
     @Override
     protected void onSetUp() {
         JPlotInterface frame = GuiActionRunner.execute(new GuiQuery<JPlotInterface>() {
             protected JPlotInterface executeInEDT() {
                 JPlotInterface jplotInterface= new JPlotAdminDecorator(new JPlotProgrammerDecorator(new JPlot(DatabaseConnection.UserTypes.ADMIN, new Grid())));
                 jplotInterface.initSubviews();
                 return jplotInterface;
             }
         });
         // IMPORTANT: note the call to 'robot()'
         // we must use the Robot from FestSwingTestngTestCase        
         mWindow = new FrameFixture(robot(), frame.getFrame());
         mWindow.show(); // shows the frame to test
     }
     
     @Test(description = "Verify velocity display is shown")
     public void speedDisplayShouldBeAvailable() {
         mWindow.label("velocity_display").requireVisible();
     }
     
     @Test(description = "Verify battery display is shown")
     public void batteryDisplayShouldBeAvailable() {
        mWindow.label("battery_display").requireVisible();
     }
 
 }
