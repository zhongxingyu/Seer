 package edu.msoe.se2800.h4.jplot;
 
import dagger.ObjectGraph;

 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.exception.ComponentLookupException;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.testng.testcase.FestSwingTestngTestCase;
 import org.testng.annotations.Test;
 
import edu.msoe.se2800.h4.LejosModule;
 import edu.msoe.se2800.h4.administrationFeatures.DatabaseConnection;
 import edu.msoe.se2800.h4.jplot.grid.Grid;
 
 public class JPlotProgrammerTest extends FestSwingTestngTestCase {
 	
 	private FrameFixture mWindow;
 
 	@Override
     protected void onSetUp() {
	    
	 // Setup dependency injection
        ObjectGraph objectGraph = ObjectGraph.create(new LejosModule());
        objectGraph.injectStatics();
	    
     	JPlotInterface frame = GuiActionRunner.execute(new GuiQuery<JPlotInterface>() {
             protected JPlotInterface executeInEDT() {
             	JPlotInterface jplotInterface= new JPlotProgrammerDecorator(new JPlot(DatabaseConnection.UserTypes.PROGRAMMER, new Grid()));
             	jplotInterface.initSubviews();
             	return jplotInterface;
             }
         });
         // IMPORTANT: note the call to 'robot()'
         // we must use the Robot from FestSwingTestngTestCase        
         mWindow = new FrameFixture(robot(), frame.getFrame());
         mWindow.show(); // shows the frame to test
     }
     
     @Test
     public void pMenuItemLogoutShouldBeAvailable() {
         mWindow.menuItem("logout").requireVisible().requireEnabled();
     }
     
     @Test
     public void pMenuItemImmediateModeShouldBeAvailable() {
         mWindow.menuItem("immediate_mode").requireVisible().requireEnabled();
     }
     
     @Test
     public void pMenuItemAdministratorModeShouldBeAvailable() {
         mWindow.menuItem("administrator_mode").requireVisible().requireEnabled();
     }
     
     @Test(expectedExceptions = ComponentLookupException.class)
     public void pMenuItemCreateUserShouldNotBeAvailable() {
         mWindow.menuItem("create_user").requireNotVisible();
     }
     
     @Test(expectedExceptions = ComponentLookupException.class)
     public void pMenuItemListUserShouldNotBeAvailable() {
         mWindow.menuItem("list_user").requireNotVisible();
     }
 
 }
