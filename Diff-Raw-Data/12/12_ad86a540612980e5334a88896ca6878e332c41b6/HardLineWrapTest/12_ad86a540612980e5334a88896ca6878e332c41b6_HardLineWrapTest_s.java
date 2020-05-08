 package net.sourceforge.texlipse.editor;
 
import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.Collection;
 

 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.DocumentCommand;
 import org.eclipse.jface.text.IDocument;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(Parameterized.class)
 public class HardLineWrapTest {
 
     public static class TestDocumentCommand extends DocumentCommand {
         public TestDocumentCommand(int offset, String text) {
             super();
             this.offset = offset;
             this.text = text;
             this.length = 0;
             this.caretOffset = offset;
             this.shiftsCaret = true;
         }
         
         public void execute(IDocument document){
         	try{
         		document.replace(offset, length, text);
         	} catch (BadLocationException ex) {
         		assertTrue(false);
         	}
             if (shiftsCaret == true) {
                 caretOffset += text.length();
             }
         }
         
     }
     
     static HardLineWrap hlw;
     
     @BeforeClass
     public static void setUp() throws Exception {
         hlw = new HardLineWrap();
     }
     
     private String string;
     private int position;
     private String text;
     private int lineLength;
     private String result;
     private int resultCursor;
     
     @Test
     public void testWrapping() {
         IDocument d = new Document(string);
         TestDocumentCommand c = new TestDocumentCommand(position, text);
         hlw.doWrapB(d, c, lineLength);
         c.execute(d);
         assertEquals(result, d.get());
         assertEquals(resultCursor, c.caretOffset);
     }
     
     @Parameters
     public static Collection testWrappingValues() {
     	return Arrays.asList(new Object[] [] {
     			//{Document text, insert position, ins. text, MAX_LINE_LENGTH, result, cursor position}
     			{"wrap test\n", 0, "a", 10, "awrap test\n", 1},
     			{"wrap test\n", 0, "a", 9, "awrap\ntest\n", 1},
     			{"wrap test\n", 0, "a", 1, "awrap\ntest\n", 1},
     			{"wrap test\n", 9, "a", 10, "wrap testa\n", 10},
     			{"wrap test\n", 9, "a", 9, "wrap\ntesta\n", 10},
     			{"wrap_test\n", 0, "a", 9, "awrap_test\n", 1},
     			{"wrap test  test \r\n", 10, "a", 9, "wrap test\r\na test\r\n", 12},
     			//Different line delimeters
     			{"wrap test\r", 0, "a", 1, "awrap\rtest\r", 1},
     			{"wrap test\r\n", 0, "a", 9, "awrap\r\ntest\r\n", 1},
     			{"wrap test\r\n", 9, "a", 9, "wrap\r\ntesta\r\n", 11},
     			{"wrap test \r\n", 10, "a", 9, "wrap test\r\na\r\n", 12},
     			//Simple Indentation
     			{"  wrap test\n", 2, "a", 9, "  awrap\n  test\n", 3},
     			//Whitespaces at the end of a line
     			{"  wrap test   \n", 2, "a", 9, "  awrap\n  test\n", 3},
     			{"wrap test        \n", 0, "a", 10, "awrap test        \n", 1},
     			{"wrap test        a\n", 0, "a", 10, "awrap test\na\n", 1},
     			{"wrap test        a\n", 10, "a", 10, "wrap test\na       a\n", 11},
     			//Hard case
     			{"wrap test\n", 9, " ", 9, "wrap test \n", 10},
     			{"wrap test\r\n", 9, " ", 9, "wrap test \r\n", 10},
     			//More than one character (Paste)
     			{"test\n", 0, "wrap ", 9, "wrap test\n", 5},
     			{"test\n", 0, "wrap ", 8, "wrap\ntest\n", 5},
     			{"\n", 0, "wrap test", 8, "wrap\ntest\n", 9},
     			{"test \n", 5, "wrapa", 9, "test\nwrapa\n", 10},
     			{"a test\n", 6, "wrap ", 10, "a testwrap \n", 11},
     			//With indentation
     			{"  test\n", 2, "wrap ", 11, "  wrap test\n", 7},
     			{"  test\n", 2, "wrap ", 10, "  wrap\n  test\n", 9},
     			{"  test\r\n", 2, "wrap ", 10, "  wrap\r\n  test\r\n", 10},
     			{"  \n", 2, "wrap test", 10, "  wrap\n  test\n", 13},
     			{"  test \n", 7, "wrapa", 11, "  test\n  wrapa\n", 14},
     			{"  a test\n", 8, "wrap ", 12, "  a testwrap \n", 13},
     			{" wrap test  test \r\n", 11, "a", 10, " wrap test\r\n a test\r\n", 14},
 
     			//Text on the next line
     			{"wrap test\nmore text\n", 0, "a", 9, "awrap\ntest more text\n", 1},
     			{"wrap test\n\\begin{env}\n", 0, "a", 9, "awrap\ntest\n\\begin{env}\n", 1},
     			{"wrap test\n%Comment\n", 0, "a", 9, "awrap\ntest\n%Comment\n", 1},
     			{"wrap test\n %Comment\n", 0, "a", 9, "awrap\ntest\n %Comment\n", 1},
     			
     			//Lines ending with . or : or \\ should not be merged with next line
     			{"wrap test.\nmore text\n", 0, "a", 9, "awrap\ntest.\nmore text\n", 1},
     			{"wrap test:\nmore text\n", 0, "a", 9, "awrap\ntest:\nmore text\n", 1},
     			{"wrap test\\\\\nmore text\n", 0, "a", 9, "awrap\ntest\\\\\nmore text\n", 1},
     			
     			//Comments should be wrapped correctly
     			{"%wrap test\nmore text\n", 1, "a", 9, "%awrap\n% test\nmore text\n", 2},
     			{"%wrap test\n%more text\n", 1, "a", 9, "%awrap\n% test more text\n", 2},
     			{"wrap %test\n%more text\n", 0, "a", 9, "awrap\n%test more text\n", 1},
     			{"wrap %test\nmore text\n", 0, "a", 9, "awrap\n%test\nmore text\n", 1},
     			
     			});
     }
     
     public HardLineWrapTest(String string, int p, String text, int lineLength, String result, int resultCursor) {
     	this.string = string;
     	this.position = p;
     	this.text = text;
     	this.lineLength = lineLength;
     	this.result = result;
     	this.resultCursor = resultCursor;
     }
 
 }
