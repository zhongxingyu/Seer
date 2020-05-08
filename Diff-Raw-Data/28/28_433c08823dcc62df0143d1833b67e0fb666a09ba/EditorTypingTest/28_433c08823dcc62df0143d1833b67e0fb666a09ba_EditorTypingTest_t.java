 package com.id.editor;
 
 import static org.junit.Assert.*;
 
 import java.awt.event.KeyEvent;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.id.events.EditorKeyHandler;
 import com.id.file.File;
 import com.id.file.FileView;
 
 public class EditorTypingTest {
   private EditorKeyHandler handler;
   private Editor editor;
   private FileView fileView;
   private File file;
   private String[] lastFileContents;
 
   @Before
   public void init() {
     setFileContents();
   }
 
   private void setFileContents(String... lines) {
     lastFileContents = lines;
     file = new File(lines);
     fileView = new FileView(file);
     editor = new Editor(fileView);
     handler = new EditorKeyHandler();
   }
 
   @Test
   public void changeLine() {
     setFileContents("abc");
     typeString("lCabc");
     type(handler.escape());
     assertEquals("aabc", file.getLine(0));
     assertFalse(editor.isInInsertMode());
   }
 
   @Test
   public void deleteLine() {
     setFileContents("abc");
     typeString("D");
     assertFileContents("");
     assertFalse(editor.isInInsertMode());
     typeString("u");
     assertFileContents("abc");
   }
 
   @Test
   public void deleteAndRetype() {
     setFileContents("abc", "def");
     typeString("Dadefg");
     type(handler.escape());
     assertFileContents("defg", "def");
   }
 
   @Test
   public void dollars() {
     setFileContents("abc");
     typeString("$D");
     assertFileContents("ab");
   }
 
   @Test
   public void deleteCursorPosition() {
     setFileContents("abc");
     typeString("lD");
     assertEquals(0, editor.getCursorPosition().getX());
   }
 
   @Test
   public void goToStartOfLine() {
     setFileContents("abc");
     typeString("$0");
     assertEquals(0, editor.getCursorPosition().getX());
   }
 
   @Test
   public void changesMade() {
     setFileContents("abc");
     typeString("D");
     assertTrue(file.isModified());
     typeString("u");
     assertFalse(file.isModified());
   }
 
   @Test
   public void delete() {
     setFileContents("abc");
     typeString("lx");
     assertFileContents("ac");
   }
 
   @Test
   public void deleteVisual() {
     setFileContents("abcdef");
     typeString("vlx");
     assertFileContents("cdef");
     assertFalse(editor.isInVisual());
     assertEquals(0, editor.getCursorPosition().getX());
     typeString("u");
     assertFileContents("abcdef");
     assertEquals(0, editor.getCursorPosition().getX());
   }
 
   @Test
   public void noInsertFromVisual() {
     setFileContents("abc");
     typeString("vi");
     assertFalse(editor.isInInsertMode());
     assertTrue(editor.isInVisual());
     type(handler.makeEventFromVKey(KeyEvent.VK_ESCAPE));
     assertFalse(editor.isInVisual());
   }
 
   @Test
   public void replaceChar() {
     setFileContents("abc");
     typeString("lsd");
     assertTrue(editor.isInInsertMode());
     assertFileContents("adc");
   }
 
   @Test
   public void substituteVisual() {
     setFileContents("abc");
     typeString("vls");
     assertFileContents("c");
     typeString("X");
     assertFileContents("Xc");
   }
 
   @Test
   public void substituteLine() {
     setFileContents("abc", "def");
     typeString("Sddd");
     assertFileContents("ddd", "def");
   }
 
   @Test
   public void subsituteLineFromMiddle() {
     setFileContents("abcdef");
     typeString("llSabc");
     assertFileContents("abc");
     ensureUndoGoesToLastFileContents();
     assertEquals(0, editor.getCursorPosition().getX());
   }
 
  @Test
  public void xOverMultipleLines() {
    setFileContents("abc", "def", "ghi");
    typeString("lvjjx");
    assertFileContents("ai");
  }

   @After
   public void checkUndo() {
     ensureUndoGoesToLastFileContents();
   }
 
   private void ensureUndoGoesToLastFileContents() {
     type(handler.escape());
     while (editor.hasUndo()) {
       typeString("u");
     }
     assertFileContents(lastFileContents);
   }
 
   private void assertFileContents(String... lines) {
     assertArrayEquals(lines, file.getLines());
   }
 
   private void type(KeyEvent event) {
     handler.handleKeyPress(event, editor);
   }
 
   private void typeString(String letters) {
     for (int i = 0; i < letters.length(); i++) {
       typeChar(letters.charAt(i));
     }
   }
 
   private void typeChar(char c) {
     handler.handleChar(c, editor);
   }
 }
