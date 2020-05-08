 package org.nohope.typetools.json;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.junit.Test;
 
 import java.awt.*;
 
import static junit.framework.Assert.assertEquals;
 
 /**
  * Date: 11/8/12
  * Time: 3:28 PM
  */
 public class ColorModuleTest {
     @Test
     public void testColorSerialization() throws Exception {
         final Color color = new Color(10, 20, 30, 40);
 
         final ObjectMapper usualMapper = new ObjectMapper();
         usualMapper.registerModule(new ColorModule());
         usualMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
 
         final String colorSerialized = usualMapper.writeValueAsString(color);
         final Color c = usualMapper.readValue(colorSerialized, Color.class);
 
         assertEquals(color, c);
         assertEquals(color.getAlpha(), c.getAlpha());
 
     }
 }
