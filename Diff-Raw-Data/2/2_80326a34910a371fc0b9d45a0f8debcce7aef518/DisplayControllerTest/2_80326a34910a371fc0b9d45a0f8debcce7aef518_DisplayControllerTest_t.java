 package unit;
 
 import static org.junit.Assert.*;
 
 import glare.ClassFactory;
 
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import resources.DatabaseManagerDummy;
 
 import dal.DatabaseHandler;
 import dal.DatabaseManager;
 
 import bll.DisplayController;
 import bll.PictureController;
 
 public class DisplayControllerTest {
 	
 	private DisplayController dc;
 	private PictureController pc;
 	private DatabaseHandler dbHandler;
 	private DatabaseManagerDummy dbManagerDummy;
 	
 	@Before
 	public void setUp() throws IOException{
 		dbHandler = new DatabaseHandler();
 		dbManagerDummy = new DatabaseManagerDummy(dbHandler);
 		pc = new PictureController(dbManagerDummy);
 		
 		dc = new DisplayController(pc);
		dc.getCurrentPicture();
 	}
 	@After
 	public void tearDown(){
 		dc = null;
 	}
 
 	@Test
 	public void test() {
 		fail("Not yet implemented");
 	}
 
 }
