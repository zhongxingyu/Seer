 package com.id.editor;
 
 import static org.junit.Assert.*;
 
 import java.awt.event.KeyEvent;
 
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
 
   @Before
   public void init() {
     setFileContents();
   }
 
   private void setFileContents(String... lines) {
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
