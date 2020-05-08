 package org.lucassus.jmine.enums;
 
 import java.net.URL;
 import javax.swing.ImageIcon;
 import org.testng.annotations.Test;
 import static org.testng.Assert.*;
 
 public class GameIconTest {
 
     /**
      * Test of getIcon method, of class GameIcon.
      */
     @Test
     public void getIcon() {
        URL iconLocation = getClass().getResource("/resources/flag.gif");
         ImageIcon expResult = new ImageIcon(iconLocation);
         ImageIcon result = GameIcon.FLAG.getIcon();
 
         assertTrue(result.toString().equals(expResult.toString()));
     }
 
 }
