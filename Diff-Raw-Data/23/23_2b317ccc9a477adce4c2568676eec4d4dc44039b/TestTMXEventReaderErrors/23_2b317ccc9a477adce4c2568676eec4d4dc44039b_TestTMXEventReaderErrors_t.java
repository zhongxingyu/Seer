 package com.spartansoftwareinc.otter;
 
 import static com.spartansoftwareinc.otter.TestUtil.readEvents;
 import static com.spartansoftwareinc.otter.TestUtil.readTUEvents;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.stream.XMLStreamException;
 
 import net.sundell.snax.SNAXUserException;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class TestTMXEventReaderErrors {
 
     @Test
     public void testXMLError() throws Exception {
         TMXReader reader = TMXReader.createTMXEventReader(
                             new StringReader("asldjalksdjalsdj"));
         TestErrorHandler handler = new TestErrorHandler();
         reader.setErrorHandler(handler);
         readWithErrors(reader);
         assertNotNull(handler.xmlError);
     }
     
     // XXX It would be nice to have checkable error typing
     // and an easier way to verify locations
     
     @Test
     public void testDateFormatError() throws Exception {
         expectNonfatalError("/error_date_format.tmx", 1);
     }
 
     @Test
     public void testUnsupportedVersionError() throws Exception {
         expectNonfatalError("/error_tmx_version.tmx", 1);
     }
 
     @Test
     public void testMissingVersionError() throws Exception {
         expectFatalError("/error_missing_tmx_version.tmx");
     }
     
     @Test
     public void testMissingBptIAttrError() throws Exception {
        TMXReader reader = TestUtil.getTMXReader("/error_missing_bpt_i.tmx");
        TestErrorHandler handler = new TestErrorHandler();
        reader.setErrorHandler(handler);
        readWithErrors(reader);
        assertEquals(1, handler.tuErrors.size());
        assertEquals(0, handler.tuErrors.get(0).sequence);
        assertEquals(0, handler.errors.size());
        assertNull(handler.fatalError);
        assertNull(handler.xmlError);
     }
     
     @Test
     public void testMissingItAttrError() throws Exception {
         TMXReader reader = TestUtil.getTMXReader("/error_it_tag_missing_pos.tmx");
         TestErrorHandler handler = new TestErrorHandler();
         reader.setErrorHandler(handler);
         readWithErrors(reader);
         assertEquals(1, handler.tuErrors.size());
         assertEquals(0, handler.tuErrors.get(0).sequence);
         assertEquals(0, handler.errors.size());
         assertNull(handler.fatalError);
         assertNull(handler.xmlError);
     }
     
     // TODO test subflow and hi cases 
     // Also, need to test multiple <bpt> with the same @i, etc
     @Test
     public void testOutOfOrderPairedTagsError() throws Exception {
         TMXReader reader = TestUtil.getTMXReader("/error_out_of_order_pair.tmx");
         TestErrorHandler handler = new TestErrorHandler();
         reader.setErrorHandler(handler);
         readWithErrors(reader);
         assertEquals(1, handler.tuErrors.size());
         assertEquals(0, handler.tuErrors.get(0).sequence);
         assertEquals(0, handler.errors.size());
         assertNull(handler.fatalError);
         assertNull(handler.xmlError);
     }
 
     // Make sure that when we report a TU error, we don't also produce
     // that TU as an event.
     @Test
     public void testTuErrorMeansTuIsSkipped() throws Exception {
         // This file contained only one TU, so when it is reported as an
         // error, there are no TUs produced as events.
         TMXReader reader = TestUtil.getTMXReader("/error_out_of_order_pair.tmx");
         List<TUEvent> tus = readTUEvents(reader);
         assertEquals(0, tus.size());
         // This file contains two TUs, so when the first (sequence 0) has an
         // error, the second (sequence 1) is still exposed as an event.
         reader = TestUtil.getTMXReader("/error_it_tag_missing_pos.tmx");
         tus = readTUEvents(reader);
         assertEquals(1, tus.size());
         assertEquals(1, tus.get(0).getSequence());
     }
     
     void expectNonfatalError(String resource, int errorCount) throws Exception {
         TMXReader reader = TestUtil.getTMXReader(resource);
         TestErrorHandler handler = new TestErrorHandler();
         reader.setErrorHandler(handler);
         readWithErrors(reader);
         assertEquals(errorCount, handler.errors.size());
         assertNull(handler.fatalError);
         assertNull(handler.xmlError);
     }
     
     void expectFatalError(String resource) throws Exception {
         TMXReader reader = TestUtil.getTMXReader(resource);
         TestErrorHandler handler = new TestErrorHandler();
         reader.setErrorHandler(handler);
         readWithErrors(reader);
         assertEquals(0, handler.errors.size());
         assertNotNull(handler.fatalError);
         assertNull(handler.xmlError);
     }
     
     void readWithErrors(TMXReader reader) {
         try {
             readEvents(reader);
         }
         catch (OtterException e) {
             // Make sure all thrown exceptions originated from 
             // our error framework
             assertEquals("Halting on error", e.getMessage());
         }
         catch (SNAXUserException e) {
             // XXX I don't like how my exceptions are getting masked.
             assertTrue(e.getCause() instanceof OtterException);
             assertEquals("Halting on error", e.getCause().getMessage());
         }
     }
     
     class TestErrorHandler implements ErrorHandler {
         class TUError {
             int sequence;
             OtterException e;
         }
         List<TUError> tuErrors = new ArrayList<TUError>();
         List<OtterException> errors = new ArrayList<OtterException>();
         OtterException fatalError = null;
         XMLStreamException xmlError = null;
         
         @Override
         public void tuError(int tuSequence, OtterException e) {
             TUError error = new TUError();
             error.sequence = tuSequence;
             error.e = e;
             tuErrors.add(error);
         }
         
         @Override
         public void error(OtterException e) {
             errors.add(e);
         }
 
         @Override
         public void fatalError(OtterException e) {
             fatalError = e;
             throw new OtterException("Halting on error", e.getLocation());
         }
 
         @Override
         public void xmlError(XMLStreamException e) {
             xmlError = e;
             throw new OtterException("Halting on error", e.getLocation());
         }
 
        
     }
 }
