 package de.htwg.se.battleship;
 
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
import java.io.UnsupportedEncodingException;
 
 import org.junit.Test;
 
 import de.htwg.se.battleship.aview.tui.menuEntry.Close;
 
 public class BattleshipTest {
 
     @Test
     public void test() {
 
         InputStream oldIn = System.in;
 
         Close close = new Close(null);
         String s = close.command() + System.getProperty("line.separator");
 
         try {
             System.setIn(new ByteArrayInputStream(s.getBytes("UTF-8")));
            Battleship.main();
         } catch (Exception e) {
             System.setIn(oldIn);
             fail(e.getMessage());
         }
 
         System.setIn(oldIn);
     }
 
 }
