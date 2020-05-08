 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker;
 
 import java.util.HashMap;
 import junit.framework.TestCase;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.urbancamper.audiobookmarker.audio.AudioFileRecognizerInterface;
 import ru.urbancamper.audiobookmarker.audio.AudioFileRecognizerStub;
 import ru.urbancamper.audiobookmarker.context.BeansAnnotationsForTests;
 import ru.urbancamper.audiobookmarker.document.MarkedDocument;
 import ru.urbancamper.audiobookmarker.text.BookText;
 
 /**
  *
  * @author pozpl
  */
 public class AudioBookMarkerUtilTest extends TestCase {
 
     private String RECOGNIZED_AND_ALIGNED_STUB_TEXT = "some(2.1, 2.7) kind(3.0, 4.0) of(4.5, 5.0)"
             + " text(6.1, 7.0) here(9.0, 10.0)";
     private String BOOK_TEXT = "there is some kind of magnificent pease of literature text here";
 
 
     public AudioBookMarkerUtilTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
 
     public void testMakeMarkers() {
 
         ApplicationContext ctxt = new AnnotationConfigApplicationContext(BeansAnnotationsForTests.class);
         AudioFileRecognizerStub recognizer = (AudioFileRecognizerStub) ctxt.getBean("audioFileRecognizerStub");
         recognizer.setStubText(RECOGNIZED_AND_ALIGNED_STUB_TEXT);
         BookText bookText = ctxt.getBean(BookText.class);
 
         AudioBookMarkerUtil util = new AudioBookMarkerUtil(bookText, recognizer);
         String[] filePaths = {"/some/fictional/path"};
         MarkedDocument markedDocument = util.makeMarkers(filePaths, this.BOOK_TEXT);
         String markedText =  markedDocument.getMarkedText();
 
         assertEquals("there is <1:2.1/>some <1:3.0/>kind of magnificent pease <1:4.5/>of literature <1:6.1/>text <1:9.0/>here", markedText);
 
         HashMap<String, String> filesNamesToUidMap = markedDocument.getFileNamesToUidsMap();
         HashMap<String, String> filesNamesToUidMapExpected = new HashMap<String, String>();
         filesNamesToUidMapExpected.put("path", String.valueOf(0));
         assertEquals(filesNamesToUidMapExpected, filesNamesToUidMap);
 
     }
 }
