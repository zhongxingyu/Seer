 package com.guidewire.com.guidewire.accel.deployment.impl;
 
 import com.guidewire.accel.parser.AcceleratorParser;
 import junit.framework.TestCase;
 
 import java.io.File;
 
 /**
  * User: afogleson
  * Date: 3/11/12
  * Time: 10:42 AM
  */
 public class AcceleratorParserTest extends TestCase {
 
   public void testParser() {
     AcceleratorParser parser = new AcceleratorParser(new File("src/test/resources/sample"));
     parser.parseAccelerator();
     assertNotNull(parser.getAcceleratorComponents());
    assertEquals(2, parser.getAcceleratorComponents().getAllComponents().length);
   }
 }
