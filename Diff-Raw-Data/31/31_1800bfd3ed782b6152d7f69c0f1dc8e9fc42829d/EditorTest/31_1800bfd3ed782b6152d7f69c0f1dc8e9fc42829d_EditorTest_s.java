 package com.id.editor;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.id.file.File;
 import com.id.file.FileView;
 import com.id.file.ModifiedListener;
 import com.id.test.EditorTestBase;
 
 public class EditorTest extends EditorTestBase {
 
   private ModifiedListener modifiedListener;
   private File.Listener listener;
 
   @Before
   public void init() {
     setFileContents();
     modifiedListener = mock(ModifiedListener.class);
     listener = mock(File.Listener.class);
   }
 
   @Test
   public void backspaceAtStartOfFile() {
     setFileContents("a");
 
     editor.insert();
     editor.backspace();
     assertEquals("a", file.getLine(0));
   }
 
 
   @Test
   public void backspaceOnlyLetter() {
     setFileContents("a");
 
     editor.append();
     editor.backspace();
     assertEquals("", file.getLine(0));
   }
 
   @Test
   public void undoDoesntPutCursorOutsideCorrectRange() {
     setFileContents("abcdefg");
 
     editor.appendEnd();
     editor.onLetterTyped('a');
     editor.onLetterTyped('b');
     editor.escape();
     editor.undo();
     assertEquals(6, editor.getCursorPosition().getX());
   }
 
   @Test
   public void emptyFileInsert() {
     editor.insert();
     editor.onLetterTyped('a');
     assertEquals("a", file.getLine(0));
   }
 
   @Test
   public void emptyFileAppend() {
     editor.append();
     editor.onLetterTyped('a');
     assertEquals("a", file.getLine(0));
   }
 
   @Test
   public void emptyFileAddEmptyLine() {
     editor.addEmptyLine();
     editor.onLetterTyped('a');
     assertEquals("a", file.getLine(1));
   }
 
   @Test
   public void emptyFileAddEmptyLinePrevious() {
     editor.addEmptyLinePrevious();
     editor.onLetterTyped('a');
     assertEquals("a", file.getLine(0));
   }
 
   @Test
   public void enter() {
     editor.insert();
     editor.onLetterTyped('a');
     editor.enter();
     assertEquals("a", file.getLine(0));
     assertEquals("", file.getLine(1));
   }
 
   @Test
   public void undoEnter() {
     setFileContents("a", "b");
     editor.append();
     editor.enter();
     assertEquals("a", file.getLine(0));
     assertEquals("", file.getLine(1));
     assertEquals("b", file.getLine(2));
     editor.escape();
     editor.undo();
     assertEquals("a", file.getLine(0));
     assertEquals("b", file.getLine(1));
   }
 
   @Test
   public void enterPutsCursorOnRightLine() {
     setFileContents("abc");
     editor.append();
     editor.enter();
     assertEquals(0, editor.getCursorPosition().getX());
   }
 
   @Test
   public void addEmptyLineOnEmptyFile() {
     editor.addEmptyLine();
     assertEquals("", file.getLine(0));
     assertEquals("", file.getLine(1));
     assertEquals(1, editor.getCursorPosition().getY());
     assertEquals(0, editor.getCursorPosition().getX());
   }
 
   @Test
   public void addEmptyLinePreviousOnEmptyFile() {
     editor.addEmptyLinePrevious();
     editor.onLetterTyped('a');
     assertEquals("a", file.getLine(0));
     assertEquals("", file.getLine(1));
     assertEquals(0, editor.getCursorPosition().getY());
     assertEquals(1, editor.getCursorPosition().getX());
   }
 
   @Test
   public void range() {
     setFileContents("abc", "d", "efg");
     editor.toggleVisual(Visual.Mode.LINE);
     assertTrue(editor.isInVisual(0, 0));
     editor.toggleVisual(Visual.Mode.LINE);
     assertFalse(editor.isInVisual(0, 0));
     editor.toggleVisual(Visual.Mode.LINE);
     assertTrue(editor.isInVisual(0, 0));
     editor.escape();
     assertFalse(editor.isInVisual(0, 0));
   }
 
   @Test
   public void toggleBetweenLineAndChar() {
     setFileContents("abc");
     editor.right();
     editor.toggleVisual(Visual.Mode.LINE);
     editor.right();
     editor.toggleVisual(Visual.Mode.CHAR);
     assertTrue(editor.isInVisual(0, 1));
   }
 
   @Test
   public void changeLine() {
     setFileContents("abc");
     editor.right();
     editor.changeToEndOfLine();
     assertEquals("a", file.getLine(0));
     editor.escape();
     editor.undo();
     assertEquals("abc", file.getLine(0));
   }
 
   @Test
   public void attachListener() {
     setFileContents("abc");
     editor.addFileListener(listener);
     editor.addEmptyLinePrevious();
     verify(listener).onLineInserted(0, "");
   }
 
   @Test
   public void delete() {
     setFileContents("abc");
     editor.delete();
     assertEquals("bc", file.getLine(0));
   }
 
   @Test
   public void fileModifiedListener() {
     setFileContents("abc");
     editor.addFileModifiedListener(modifiedListener);
     editor.insert();
     editor.onLetterTyped('a');
     verify(modifiedListener).onModifiedStateChanged();
   }
 
   @Test
   public void itShouldOnlyFireOneTextChangedEventForTypingALetterInInsertMode() {
     setFileContents("");
     editor.addFileListener(listener);
     editor.insert();
     editor.onLetterTyped('a');
     verify(listener).onLineChanged(anyInt(), any(String.class), any(String.class));
   }
 
   @Test
   public void highlights() {
     setFileContents("abc", "def");
     editor.setHighlightPattern(Patterns.partWord("abc"));
     assertTrue(editor.isHighlight(0, 0));
     assertFalse(editor.isHighlight(1, 0));
     fileView.changeLine(0, "babc");
     assertFalse(editor.isHighlight(0, 0));
     assertTrue(editor.isHighlight(0, 1));
     editor.clearHighlight();
     assertFalse(editor.isHighlight(0, 0));
     assertFalse(editor.isHighlight(0, 1));
   }
 
   @Test
   public void getNextHighlightPoint() {
     setFileContents("abc asdf", "def", "abc");
     editor.setHighlightPattern(Patterns.partWord("abc"));
     editor.next();
     assertEquals(2, editor.getCursorPosition().getY());
     assertEquals(0, editor.getCursorPosition().getX());
   }
 
   @Test
   public void insertingTextInADifferentViewCausesTheCursorToMoveDown() {
     setFileContents("abc", "def", "ghi");
    typeString("G");
    assertCursorPosition(2, 0);
    FileView fileView2 = new FileView(file, 0, 0);
     fileView2.insertLine(0, "hi");
    assertCursorPosition(3, 0);
   }
 
   @Test
   public void deletingTextInADifferentViewCausesTheCursorToMoveUp() {
     setFileContents("abc", "def", "ghi");
     typeString("j");
     assertCursorPosition(1, 0);
    FileView fileView2 = new FileView(file, 0, 0);
     fileView2.removeLine(0);
     assertCursorPosition(0, 0);
   }
 }
