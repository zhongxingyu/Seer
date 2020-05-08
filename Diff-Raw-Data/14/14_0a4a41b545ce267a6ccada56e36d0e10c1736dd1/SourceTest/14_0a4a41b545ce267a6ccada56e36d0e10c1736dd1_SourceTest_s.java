 package de.weltraumschaf.caythe.frontend;
 
 import de.weltraumschaf.caythe.message.Message;
 import de.weltraumschaf.caythe.message.MessageHandler;
 import de.weltraumschaf.caythe.message.MessageType;
 import de.weltraumschaf.caythe.util.SourceHelper;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URISyntaxException;
 import java.net.URL;
 import static org.junit.Assert.*;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class SourceTest {
 
     private static final String FIXTURE_DIR = "/de/weltraumschaf/caythe/frontend";
 
     private Source createSourceFromFixture(String fixtureFile) throws FileNotFoundException, URISyntaxException {
         URL resource = getClass().getResource(FIXTURE_DIR + '/' + fixtureFile);
         return SourceHelper.createFrom(resource.toURI());
     }
 
     @Test public void walkThrouhgAllCharacters() throws IOException {
         Source sut;
 
         sut = new Source(new BufferedReader(new StringReader("this")));
         assertFalse(sut.atEof());
         assertEquals(-1, sut.getCurrentPos());
         assertEquals(0, sut.getLineNumber());
         assertEquals(0, sut.getColumnNumber());
 
         sut.nextChar();
         assertEquals(0, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(1, sut.getColumnNumber());
         assertEquals('t', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(1, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(2, sut.getColumnNumber());
         assertEquals('h', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(2, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(3, sut.getColumnNumber());
         assertEquals('i', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(3, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(4, sut.getColumnNumber());
         assertEquals('s', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(4, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(5, sut.getColumnNumber());
        assertEquals(Source2.EOF, sut.currentChar());
         assertTrue(sut.atEof());
         sut.nextChar();
         assertEquals(4, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(5, sut.getColumnNumber());
        assertEquals(Source2.EOF, sut.currentChar());
         assertTrue(sut.atEof());
 
         sut = new Source(new BufferedReader(new StringReader("this\nis")));
         assertFalse(sut.atEof());
         assertEquals(-1, sut.getCurrentPos());
         assertEquals(0, sut.getLineNumber());
         assertEquals(0, sut.getColumnNumber());
 
         sut.nextChar();
         assertEquals(0, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(1, sut.getColumnNumber());
         assertEquals('t', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(1, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(2, sut.getColumnNumber());
         assertEquals('h', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(2, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(3, sut.getColumnNumber());
         assertEquals('i', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(3, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(4, sut.getColumnNumber());
         assertEquals('s', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(4, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(5, sut.getColumnNumber());
        assertEquals(Source2.EOL, sut.currentChar());
         assertTrue(sut.atEol());
         sut.nextChar();
         assertEquals(5, sut.getCurrentPos());
         assertEquals(2, sut.getLineNumber());
         assertEquals(1, sut.getColumnNumber());
         assertEquals('i', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(6, sut.getCurrentPos());
         assertEquals(2, sut.getLineNumber());
         assertEquals(2, sut.getColumnNumber());
         assertEquals('s', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(7, sut.getCurrentPos());
         assertEquals(2, sut.getLineNumber());
         assertEquals(3, sut.getColumnNumber());
        assertEquals(Source2.EOF, sut.currentChar());
         assertTrue(sut.atEof());
         sut.nextChar();
         assertEquals(7, sut.getCurrentPos());
         assertEquals(2, sut.getLineNumber());
         assertEquals(3, sut.getColumnNumber());
        assertEquals(Source2.EOF, sut.currentChar());
         assertTrue(sut.atEof());
     }
 
     @Test public void testMessageProducer() {
         Message msg1 = new Message(MessageType.CALL, "msg1");
         Message msg2 = new Message(MessageType.CALL, "msg1");
         Message msg3 = new Message(MessageType.CALL, "msg1");
 
         MessageHandler handler    = mock(MessageHandler.class);
 
         Source src = new Source(null, handler);
         src.sendMessage(msg1);
         src.sendMessage(msg2);
         src.sendMessage(msg3);
 
         verify(handler, times(1)).sendMessage(msg1);
         verify(handler, times(1)).sendMessage(msg2);
         verify(handler, times(1)).sendMessage(msg3);
     }
 
     @Test public void testAtEol() throws Exception {
         Source sut = createSourceFromFixture("empty_source");
         assertFalse(sut.atEol());
         sut.nextChar();
         assertFalse(sut.atEol());
         sut.close();
 
         sut = createSourceFromFixture("one_line_source");
         assertFalse(sut.atEol());
         for (int i = 0; i < 20; ++i) {
             sut.nextChar();
             assertFalse(sut.atEol());
         }
         sut.nextChar();
         assertFalse(sut.atEol());
         sut.close();
 
         sut = createSourceFromFixture("multi_line_source");
         assertEquals(0, sut.getLineNumber());
         assertFalse(sut.atEol());
         for (int i = 0; i < 10; ++i) {
             sut.nextChar();
             assertEquals(1, sut.getLineNumber());
             assertFalse(sut.atEol());
         }
         sut.nextChar();
         assertEquals(1, sut.getLineNumber());
         assertTrue(sut.atEol());
 
         for (int i = 0; i < 11; ++i) {
             sut.nextChar();
             assertEquals(2, sut.getLineNumber());
             assertFalse(sut.atEol());
         }
         sut.nextChar();
         assertEquals(2, sut.getLineNumber());
         assertTrue(sut.atEol());
 
         for (int i = 0; i < 7; ++i) {
             sut.nextChar();
             assertEquals(3, sut.getLineNumber());
             assertFalse(sut.atEol());
         }
         sut.nextChar();
         assertEquals(3, sut.getLineNumber());
         assertTrue(sut.atEol());
         sut.nextChar();
         assertEquals(4, sut.getLineNumber());
         assertTrue(sut.atEol());
         for (int i = 0; i < 14; ++i) {
             sut.nextChar();
             assertEquals(5, sut.getLineNumber());
             assertFalse(sut.atEol());
         }
         sut.nextChar();
         assertEquals(5, sut.getLineNumber());
         assertTrue(sut.atEol());
 
         sut.close();
     }
 
     @Test public void testAtEof() throws Exception {
         Source sut = createSourceFromFixture("empty_source");
         assertFalse(sut.atEof());
         sut.nextChar();
         assertTrue(sut.atEof());
         assertEquals(Source.EOF, sut.currentChar());
         sut.close();
 
         sut = createSourceFromFixture("one_line_source");
         assertFalse(sut.atEof());
         for (int i = 0; i < 20; ++i) {
             sut.nextChar();
             assertFalse(sut.atEof());
         }
         sut.nextChar();
         assertTrue(sut.atEof());
         assertEquals(Source.EOF, sut.currentChar());
         sut.close();
 
         sut = createSourceFromFixture("multi_line_source");
         assertFalse(sut.atEof());
         for (int i = 0; i < 83; ++i) {
             sut.nextChar();
             assertFalse(sut.atEof());
         }
         sut.nextChar();
         assertTrue(sut.atEof());
         assertEquals(Source.EOF, sut.currentChar());
         sut.close();
 
         sut = SourceHelper.createFrom("this");
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals('t', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals('h', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals('i', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals('s', sut.currentChar());
         assertFalse(sut.atEof());
         sut.nextChar();
         assertEquals(Source.EOF, sut.currentChar());
         assertTrue(sut.atEof());
     }
 
     @Test public void testSkipToNextLine() throws Exception {
         Source sut = createSourceFromFixture("multi_line_source");
         assertEquals(-1, sut.getCurrentPos());
         assertEquals(0, sut.getLineNumber());
 
         sut.nextChar();
         assertEquals('T', sut.currentChar());
         assertEquals(0, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(1, sut.getColumnNumber());
 
         sut.nextChar();
         assertEquals('h', sut.currentChar());
         assertEquals(1, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(2, sut.getColumnNumber());
 
         sut.skipToNextLine();
         assertEquals('m', sut.currentChar());
         assertEquals(11, sut.getCurrentPos());
         assertEquals(2, sut.getLineNumber());
         assertEquals(1, sut.getColumnNumber());
 
         sut.nextChar();
         assertEquals('u', sut.currentChar());
         assertEquals(12, sut.getCurrentPos());
         assertEquals(2, sut.getLineNumber());
         assertEquals(2, sut.getColumnNumber());
 
         sut.close();
     }
 
     @Test public void testPeakChar() throws Exception {
         Source sut = createSourceFromFixture("one_line_source");
         assertEquals(-1, sut.getCurrentPos());
         assertEquals(0, sut.getLineNumber());
         assertEquals(0, sut.getColumnNumber());
 
         sut.nextChar();
         assertEquals('S', sut.currentChar());
         assertEquals(0, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(1, sut.getColumnNumber());
 
         assertEquals('o', sut.peekChar());
         assertEquals('S', sut.currentChar());
         assertEquals(0, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(1, sut.getColumnNumber());
 
         sut.nextChar();
         assertEquals('o', sut.currentChar());
         assertEquals(1, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(2, sut.getColumnNumber());
 
         sut.close();
     }
 
     @Test public void testReadWholeSource() throws Exception {
         Source sut = createSourceFromFixture("empty_source");
         assertEquals(-1, sut.getCurrentPos());
         assertEquals(0, sut.getLineNumber());
         assertEquals(0, sut.getColumnNumber());
         sut.nextChar();
         assertEquals(Source.EOF, sut.currentChar());
         assertEquals(0, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(1, sut.getColumnNumber());
         sut.close();
 
         sut = createSourceFromFixture("one_line_source");
         assertEquals(-1, sut.getCurrentPos());
         assertEquals(0, sut.getLineNumber());
         assertEquals(0, sut.getColumnNumber());
         char[] chars = "Source with one line".toCharArray();
 
         for (int i = 0; i < chars.length; ++i) {
             sut.nextChar();
             assertEquals("iteration " + i, chars[i], sut.currentChar());
             assertEquals(i, sut.getCurrentPos());
             assertEquals(1, sut.getLineNumber());
             assertEquals((i + 1), sut.getColumnNumber());
         }
 
         sut.nextChar();
         assertEquals(Source.EOF, sut.currentChar());
         sut.nextChar();
         assertEquals(Source.EOF, sut.currentChar());
         assertEquals(20, sut.getCurrentPos());
         assertEquals(1, sut.getLineNumber());
         assertEquals(21, sut.getColumnNumber());
         sut.close();
     }
 }
