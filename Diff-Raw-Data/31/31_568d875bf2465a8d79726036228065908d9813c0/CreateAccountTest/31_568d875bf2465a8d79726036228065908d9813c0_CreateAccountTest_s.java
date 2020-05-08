 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 /**
  * 
  * @author ilyssaw
  *
  */
 
 public class CreateAccountTest {
 
 	Game game;
 	@Before
 	public void setUp() throws Exception {
 			Driver driver = new Driver();
 			driver.run();
 			game = driver.gameScreen.game;
 	}
 
 
 	@Test
 	public void testCreateAccount() {
 		CreateAccount acc=new CreateAccount(game.getScreen());
 		acc.setText("ilyssa","tennis","tennis");
		boolean res1=acc.createAccount();
 		assertTrue("wrong",res1);
 		acc.setText(null, null, null);
		boolean res2=acc.createAccount();
 		assertFalse("wrong",res2);
 		acc.setText("ilyssa","hi","HI");
		boolean res3=acc.createAccount();
 		assertFalse("wrong",res3);
 		acc.setText(null,"ilyssa","ilyssa");
		boolean res4=acc.createAccount();
 		assertFalse("wrong",res4);
 	}
 }
