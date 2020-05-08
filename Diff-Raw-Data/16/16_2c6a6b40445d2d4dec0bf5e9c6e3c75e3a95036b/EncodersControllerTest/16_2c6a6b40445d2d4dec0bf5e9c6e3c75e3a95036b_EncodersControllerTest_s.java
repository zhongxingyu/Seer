 package de.fhb.polyencoder.server;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.*;
 
 import de.fhb.polyencoder.Track;
 import de.fhb.polyencoder.Util;
 
 public class EncodersControllerTest {
  private static final String DEFAULT_COORDS_GPX = Util.readFile("testfiles/threeWaypoints_GPX_1.0.gpx");
   private static final String DEFAULT_COORDS_GPX_ENCODED_POINTS = "_qo]_qo]}oR~ugCaaf@~bmA";
   private static final String DEFAULT_COORDS_GPX_ENCODED_LEVELS = "PLP";
 
 
 
   @Test
   public void testIsValidTyp() {
     assertTrue("gpx should be valid input typ", EncodersController.isValidTyp("gpx"));
     assertTrue("kml should be valid input typ", EncodersController.isValidTyp("kmz"));
     assertTrue("kmz should be valid input typ", EncodersController.isValidTyp("kml"));
     assertTrue("raw should be valid input typ", EncodersController.isValidTyp("raw"));
   }
 
 
 
   @Test
   public void testIsValidFormat() {
     assertTrue("gpx should be valid output format", EncodersController.isOutputValid("html"));
     assertTrue("kml should be valid output format", EncodersController.isOutputValid("json"));
     assertTrue("kmz should be valid output format", EncodersController.isOutputValid("xml"));
     assertTrue("raw should be valid output format", EncodersController.isOutputValid("raw"));
   }
 
 
 
   @Test
   public void testIsInvalidTyp() {
     assertFalse("foo should be invalid input typ", EncodersController.isValidTyp("foo"));
   }
 
 
 
   @Test
   public void testIsInvalidFormat() {
     assertFalse("bar should be invalid output format", EncodersController.isOutputValid("bar"));
   }
 
 
 
   @Test
   public void testIsValidData() {
     assertTrue("gpx data should be valid data", EncodersController.hasValidData(DEFAULT_COORDS_GPX));
   }
 
 
 
   @Test
   public void testIsInvalidData() {
     assertFalse("empty should be invalid data", EncodersController.hasValidData(""));
     assertFalse("null should be invalid data", EncodersController.hasValidData(null));
   }
 
 
 
   @Test
   public void testIsValidLink() {
     assertTrue("should be valid link", EncodersController.isValidLink("http://google.de/robots.txt"));
   }
 
 
 
   @Test
   public void testIsInvalidLink() {
     assertFalse("should be invalid link", EncodersController.isValidLink(""));
   }
 
 
 
   @Test
   public void testParseData() {
     List<Track> trks = EncodersController.parseData(DEFAULT_COORDS_GPX, "gpx");
     assertEquals("expect 3 track points", 3, trks.get(0).size());
   }
 }
