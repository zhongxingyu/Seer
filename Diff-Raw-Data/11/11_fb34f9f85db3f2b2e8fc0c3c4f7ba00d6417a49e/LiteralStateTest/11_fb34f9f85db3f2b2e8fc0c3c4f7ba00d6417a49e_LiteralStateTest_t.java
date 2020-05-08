 package ch.fhnw.cbip.compiler.scanner.state;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.fhnw.cbip.compiler.error.LexicalError;
 import ch.fhnw.cbip.compiler.scanner.Scanner;
 import ch.fhnw.cbip.compiler.scanner.enums.ScannerSymbol;
 import ch.fhnw.cbip.compiler.scanner.token.Literal;
 
 public class LiteralStateTest {
   private Scanner mockScanner;
   private LiteralState state;
 
   @Before
   public void setUp() throws Exception {
     mockScanner = mock(Scanner.class);
     state = new LiteralState();
   }
 
   @Test(expected = LexicalError.class)
   public void testHandleCharFail() throws LexicalError {
     when(mockScanner.getLineNumber()).thenReturn(1);
     char[] failChar = { '1', 191 };
     state.handleChar(failChar, mockScanner);
   }
 
   @Test
   public void testHandleCharContinue() throws LexicalError {
     char[] goodChar = { '9', '0' };
     char[] returnChar = state.handleChar(goodChar, mockScanner);
     assertEquals(goodChar, returnChar);
     verify(mockScanner).setState(state); // has to stay in the same state
   }
 
   @Test
   public void testHandleCharEnd() throws LexicalError {
     char[] spaceChar = { '1', '2', ' ' };
     int lineNumber = 1;
     Literal expectedToken = new Literal(12, lineNumber);
     InitialState expectedState = new InitialState();
 
     when(mockScanner.getLineNumber()).thenReturn(lineNumber);
     char[] returnChar = state.handleChar(spaceChar, mockScanner);
 
     assertEquals(1, returnChar.length);
     assertEquals(' ', returnChar[0]);
     verify(mockScanner).addToken(expectedToken);
     verify(mockScanner).setState(expectedState);
   }
 
   @Test
   public void testHandleCharEndTab() throws LexicalError {
    char[] tabChar = { '3', '4', '5', '6', '\t' };
     int lineNumber = 1;
    Literal expectedToken = new Literal(3456, lineNumber);
     InitialState expectedState = new InitialState();
 
     when(mockScanner.getLineNumber()).thenReturn(lineNumber);
     char[] returnChar = state.handleChar(tabChar, mockScanner);
 
     assertEquals(1, returnChar.length);
    assertEquals('\t', returnChar[0]);
     verify(mockScanner).addToken(expectedToken);
     verify(mockScanner).setState(expectedState);
   }
 
   @Test
   public void testHandleCharEndSymbol() throws LexicalError {
     char[] symbolChar = { '8', '9', ScannerSymbol.SEMICOLON.getCharValue() };
     int lineNumber = 1;
     Literal expectedToken = new Literal(89, lineNumber);
     InitialState expectedState = new InitialState();
 
     when(mockScanner.getLineNumber()).thenReturn(lineNumber);
     char[] returnChar = state.handleChar(symbolChar, mockScanner);
 
     assertEquals(1, returnChar.length);
    assertEquals(symbolChar[symbolChar.length - 1], returnChar[0]);
     verify(mockScanner).addToken(expectedToken);
     verify(mockScanner).setState(expectedState);
   }
 
 }
