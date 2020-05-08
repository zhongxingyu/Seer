 package de.fhb.polyencoder;
 
 import static org.junit.Assert.*;
 import org.junit.*;
 
 public class PolylineEncoderTest {
   private static PolylineEncoder encoder;



  @Test
  public void testEncodeFalseBackslash() {
    String in = "\n";
    String out = "\n";
    assertEquals("A backslash which acts as an control character should not be encoded.", out, PolylineEncoder.encodeBackslash(in));
  }
 
 
 
   @Test
   public void testEncodeOneBackslashOnly() {
     String in = "\\";
     String out = "\\\\";
     assertEquals("One backslash must be encoded to two backslashes.", out, PolylineEncoder.encodeBackslash(in));
   }
 
 
 
   @Test
   public void testEncodeOneBackslashOnBeginning() {
     String in = "\\Test";
     String out = "\\\\Test";
     assertEquals("One backslash must be encoded to two backslashes.", out, PolylineEncoder.encodeBackslash(in));
   }
 
 
 
   @Test
   public void testEncodeOneBackslashOnEnd() {
     String in = "Test\\";
     String out = "Test\\\\";
     assertEquals("One backslash must be encoded to two backslashes.", out, PolylineEncoder.encodeBackslash(in));
   }
 
 
 
   @Test
   public void testEncodeOneBackslashMiddle() {
     String in = "Te\\st";
     String out = "Te\\\\st";
     assertEquals("One backslash must be encoded to two backslashes.", out, PolylineEncoder.encodeBackslash(in));
   }
 
 
 
   @Test
   public void testEncodeTwoBackslashes() {
     String in = "\\\\";
     String out = "\\\\\\\\";
     assertEquals("Should be four backslashes after encoding.", out, PolylineEncoder.encodeBackslash(in));
   }
 
 
 
   @Test
   public void testEncodeTwoBackslashesOnBeginning() {
     String in = "\\\\Test";
     String out = "\\\\\\\\Test";
     assertEquals("Should be four backslashes after encoding.", out, PolylineEncoder.encodeBackslash(in));
   }
 
 
 
   @Test
   public void testEncodeTwoBackslashesOnEnd() {
     String in = "Test\\\\";
     String out = "Test\\\\\\\\";
     assertEquals("Should be four backslashes after encoding.", out, PolylineEncoder.encodeBackslash(in));
   }
 
 
 
   @Test
   public void testEncodeTwoBackslashesMiddle() {
     String in = "Te\\\\st";
     String out = "Te\\\\\\\\st";
     assertEquals("Should be four backslashes after encoding.", out, PolylineEncoder.encodeBackslash(in));
   }
 
 
 
   @Test
   public void testEncodeTwoBackslashesDiverted() {
     String in = "Te\\st\\";
     String out = "Te\\\\st\\\\";
     assertEquals("Should be four backslashes after encoding.", out, PolylineEncoder.encodeBackslash(in));
   }
 
 
 
   @Test
   public void testEncodeBackslashesUneven() {
     String in = "Te\\\nst";
     String out = "Te\\\\\nst";
     assertEquals("Should be two backslashes and a linewrap after encoding.", out, PolylineEncoder.encodeBackslash(in));
   }
 
 
 
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
     encoder = new PolylineEncoder();
   }
 }
