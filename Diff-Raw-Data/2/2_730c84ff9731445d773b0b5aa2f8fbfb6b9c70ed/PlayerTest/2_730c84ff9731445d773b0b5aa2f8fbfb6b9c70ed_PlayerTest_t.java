 package GourmetSnacks;
 
 import game.LocalSession;
 import game.Session;
 
 import java.util.ArrayList;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 /**
  * JUnit tests for Players
  */
 public class PlayerTest extends TestCase
 {
     public PlayerTest(String testName)
     {
         super(testName);
     }
 
     /**
      * Test that all player ids are unique
      */
     @Test
     public void testIds()
     {
     	ArrayList<String> ids = new ArrayList<String>();
 	
     	Session session = new LocalSession();
     	ids = session.createPlayers(4);
     	
     	for (int a = 0; a < ids.size(); a++)
     	{
     		for (int b = 0; b < ids.size(); b++)
     		{
    			if (a != b && ids.get(a) == ids.get(b))
     			{
     				assert(false);
     			}
     		}
     	}
     	
     	assert(true);
     }
 }
