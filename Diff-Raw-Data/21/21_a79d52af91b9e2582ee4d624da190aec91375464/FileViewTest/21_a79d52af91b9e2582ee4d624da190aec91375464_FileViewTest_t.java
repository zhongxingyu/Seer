 package com.id.file;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 import org.junit.Test;
 
 import com.id.app.HighlightState;
 import com.id.editor.Editor;
 import com.id.editor.Point;
 import com.id.editor.Register;
import com.id.events.EditorKeyHandler;
 import com.id.file.File.Listener;
 import com.id.test.EditorTestBase;
 
 public class FileViewTest extends EditorTestBase {
   private Listener listener;
 
   @Test
   public void viewShrinksWhenLinesGetRemoved() {
     FileView fileView = new FileView(new File("a", "b", "c"), 0, 1);
 
     fileView.removeLine(1);
     assertEquals(1, fileView.getLineCount());
 
     fileView.removeLine(1);
     assertEquals(1, fileView.getLineCount());
   }
 
   @Test
   public void viewGrowsWhenLinesGetInserted() {
     setFileContents("a", "b", "c");
     fileView.insertLine(0, "hi");
     assertEquals(4, fileView.getLineCount());
     fileView.insertLine(4, "there");
     assertEquals(5, fileView.getLineCount());
   }
 
   @Test
   public void splitAtEnd() {
     setFileContents("a");
     fileView.splitLine(0, 1, "xxx");
     assertEquals("a", fileView.getLine(0));
     assertEquals("xxx", fileView.getLine(1));
   }
 
   @Test
   public void splitInMiddle() {
     setFileContents("abc");
     fileView.splitLine(0, 1, "xxx");
     assertEquals("a", fileView.getLine(0));
     assertEquals("xxxbc", fileView.getLine(1));
   }
 
   @Test
   public void removeText() {
     setFileContents("abc");
     String removedText = fileView.removeText(0, 1, 1);
     assertEquals("b", removedText);
     assertEquals("ac", fileView.getLine(0));
   }
 
   @Test
   public void removeLineRange() {
     setFileContents("abc", "def");
     fileView.removeLineRange(0, 1);
   }
 
   @Test
   public void getWordUnderCursor() {
     setFileContents("abc asdf", "def");
     assertEquals("abc", fileView.getWordUnder(0, 1));
   }
 
   @Test
   public void insertText() {
     setFileContents("ab");
     fileView.insertText(0, 1, "xxx");
     assertFileContents("axxxb");
   }
 
   @Test
   public void insertMultilineText() {
     setFileContents("ab");
     fileView.insertText(0, 1, "xxx", "yyy");
     assertFileContents("axxx", "yyyb");
   }
 
   @Test
   public void insertMultilineTextWithTheLastLineIncludingALineBreak() {
     setFileContents("ab");
     fileView.insertTextWithLineBreak(0, 1, "xxx", "yyy");
     assertFileContents("axxx", "yyy", "b");
   }
 
   @Test
   public void insertLines() {
     setFileContents("abc");
     fileView.insertLines(1, "def", "ghi");
     assertFileContents("abc", "def", "ghi");
   }
 
   @Test
   public void insertMultilineTextOnEmpty() {
     setFileContents();
     fileView.insertText(0, 0, "abc", "def");
     assertFileContents("abc", "def");
   }
 
   @Test
   public void insertMultilineTextWithTrailingLine() {
     setFileContents();
     fileView.insertTextWithLineBreak(0, 0, "abc", "def");
     assertFileContents("abc", "def");
   }
 
   @Test
   public void findNextChar() {
     setFileContents("this is a test");
     assertEquals(4, fileView.findNextLetter(0, 0, ' '));
     assertEquals(7, fileView.findNextLetter(0, 4, ' '));
   }
 
   @Test
   public void findPreviousChar() {
     setFileContents("abcded");
     assertEquals(3, fileView.findPreviousLetter(0, 5, 'd'));
   }
 
   @Test
   public void itShouldFireOffsetEvents() {
     setupWith(1, 2, "a", "b", "c", "d");
     fileView.addListener(listener);
     fileView.changeLine(0, "hi");
     verify(listener).onLineChanged(0, "b", "hi");
   }
 
   @Test
   public void itCanFindTheNextModifiedRegion() {
     setFileContents("blah", "blah2");
     fileView.changeLine(1, "hi");
     Point point = fileView.getNextModifiedPoint(0, 0);
     assertNotNull(point);
     assertEquals(1, point.getY());
   }
 
   @Test
   public void itCanFindModifiedRegionsThatDontIncludeTheOneItsIn() {
     setFileContents("blah", "blah2", "blah3");
     fileView.changeLine(0, "hi");
     fileView.changeLine(2, "hi");
     Point point = fileView.getPreviousModifiedPoint(2, 0);
     assertNotNull(point);
     assertEquals(0, point.getY());
     point = fileView.getNextModifiedPoint(0, 0);
     assertNotNull(point);
     assertEquals(2, point.getY());
   }
 
   @Test
   public void underscoreIsConsideredAnIdentifierLetter() {
     setFileContents("a_c");
     assertEquals("a_c", fileView.getWordUnder(0, 0));
   }
 
  @Test
  public void undoGoesToRightPlace() {
    setupWith(1, 2, "a", "b", "c");
    typeString("o<ESC>u");
    assertCursorPosition(0, 0);
  }

   private void setupWith(int start, int end, String... lines) {
     file = new File(lines);
     fileView = new FileView(file, start, end);
     editor = new Editor(fileView, new HighlightState(), new Register(),
         new Editor.EmptyEditorEnvironment());
     listener = mock(File.Listener.class);
    handler = new EditorKeyHandler();
   }
 }
