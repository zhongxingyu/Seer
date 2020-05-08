 package de.htwg.se.battleship;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.htwg.se.battleship.aview.tui.menuentry.Close;
 
 public class BattleshipTest {
 
     private InputStream backup;
 
     @Before
     public void setUp() throws UnsupportedEncodingException {
         backup = System.in;
 
         String s = Close.CMD + System.getProperty("line.separator");
         System.setIn(new ByteArrayInputStream(s.getBytes("UTF-8")));
     }
 
     @After
     public void tearDown() {
         System.setIn(backup);
     }
 
    //@Test
     public void test() {
         Battleship.main(new String[0]);
     }
 
    //@Test
     public void testGetInstance() {
         Battleship b1 = Battleship.getInstance();
         Battleship b2 = Battleship.getInstance();
 
         assertEquals(b1, b2);
     }
 
 }
