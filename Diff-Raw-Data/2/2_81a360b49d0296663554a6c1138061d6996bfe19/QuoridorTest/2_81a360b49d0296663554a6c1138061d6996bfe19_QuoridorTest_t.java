 package main_test;
 
 import main.*;
 import static org.junit.Assert.*;
 
 import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
 import static org.hamcrest.Matchers.*;
 import static main.Quoridor.*;
 
 import javax.swing.JButton;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import main.Quoridor;
 
 import com.objogate.wl.swing.AWTEventQueueProber;
 import com.objogate.wl.swing.driver.ComponentDriver;
 import com.objogate.wl.swing.driver.JButtonDriver;
 import com.objogate.wl.swing.driver.JFrameDriver;
 import com.objogate.wl.swing.driver.JLabelDriver;
 import com.objogate.wl.swing.gesture.GesturePerformer;
 
 import org.junit.Test;
 
 import com.objogate.wl.swing.*;
 
 import main_test.QuoridorTest;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 
 @RunWith(Suite.class)
 @Suite.SuiteClasses({
     //main_test.qBoardTest.class,
    player_test.PlayerTest.class
 })
 public class QuoridorTest {
 
     /*
 	JFrameDriver driver;
 	
 	@SuppressWarnings("unchecked")
 	@Before
 	public void setUp() throws Exception {
 		Quoridor game = new Quoridor();
 		driver = new JFrameDriver(new GesturePerformer(), new AWTEventQueueProber(), JFrameDriver.named(MAIN_WINDOW_TITLE), JFrameDriver.showingOnScreen());
 	}
 	
 	@SuppressWarnings("unchecked")
 	@After
 	public void shutDown(){
 		driver.dispose();
 	}
 	
     @SuppressWarnings("unchecked")
 	private JButtonDriver button(String name){
         return new JButtonDriver(driver, JButton.class, ComponentDriver.named(name));
 	}
     @SuppressWarnings("unchecked")
     private JLabelDriver label(String name) {
         return new JLabelDriver(driver, ComponentDriver.named(name));
     }
 	
     @Test
     public void stubTest() {
         assertEquals(1, 1);
     }
     
     @Test
     public void WindowUpWithTitle() {
 		driver.hasTitle(MAIN_WINDOW_TITLE);
     }
     
     @Test
     public void buttonsWork(){
 		JLabelDriver label = label(LABEL_NAME);
 
 		label.hasText(equalTo(INITIAL_MESSAGE));
 
 		for (int i = 0; i < BUTTON_TEXTS.length -1; ++i){		
 			String buttonName = BUTTON_NAME_PREFIX+i;
 	    	JButtonDriver bDriver = button(buttonName);
 			bDriver.click();
 			label.hasText(equalTo(MESSAGES[i]));
 		}
     	
     }
     */
 }
