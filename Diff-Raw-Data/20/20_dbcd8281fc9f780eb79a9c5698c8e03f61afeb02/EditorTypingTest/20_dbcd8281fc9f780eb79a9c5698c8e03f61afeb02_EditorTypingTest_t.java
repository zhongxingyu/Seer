 package com.id.editor;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.id.events.EditorKeyHandler;
 import com.id.events.KeyStroke;
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
     type(KeyStroke.escape());
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
     type(KeyStroke.escape());
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
   public void goToNextWord() {
     String text = "abc def    ghi    ";
     setFileContents(text);
     typeString("w");
     assertEquals(text.indexOf("d"), editor.getCursorPosition().getX());
     typeString("w");
     assertEquals(text.indexOf("g"), editor.getCursorPosition().getX());
     typeString("w");
     assertEquals(text.length() - 1, editor.getCursorPosition().getX());
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
     type(KeyStroke.escape());
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
 
   @Test
   public void appendTextToLine() {
 	  setFileContents("abc");
 	  typeString("Ade");
 	  assertFileContents("abcde");
   }
 
   @Test
   public void insertTextAtStartOfLine() {
   	setFileContents("abc");
   	typeString("Ide");
   	assertFileContents("deabc");
   }
 
   @Test
   public void deleteLineRange() {
     setFileContents("abc", "def");
     typeString("lVjx");
     assertFileContents();
   }
 
   @Test
   public void pageDown() {
     setFileContents("abc", "def", "ghi");
     type(KeyStroke.fromControlChar('f'));
     assertEquals(2, editor.getCursorPosition().getY());
     type(KeyStroke.fromControlChar('b'));
     assertEquals(0, editor.getCursorPosition().getY());
   }
 
   @Test
   public void join() {
     setFileContents("abc", "def", "ghi");
     typeString("jVJ");
     assertFalse(editor.isInVisual());
     assertFileContents("abc", "defghi");
   }
 
   @Test
   public void star() {
     setFileContents("abc", "def", "abc");
     typeString("*");
     assertTrue(editor.isHighlight(0, 0));
     assertFalse(editor.isHighlight(1, 0));
     assertTrue(editor.isHighlight(2, 0));
     typeString("\\");
     assertFalse(editor.isHighlight(0, 0));
     assertFalse(editor.isHighlight(1, 0));
     assertFalse(editor.isHighlight(2, 0));
   }
 
   @Test
   public void n() {
     setFileContents("abc", "def", "abc");
     typeString("*n");
     assertEquals(2, editor.getCursorPosition().getY());
     typeString("n");
     assertEquals(2, editor.getCursorPosition().getY());
     typeString("N");
     assertEquals(0, editor.getCursorPosition().getY());
   }
 
   @Test
   public void gg() {
     setFileContents("abc", "def");
     typeString("j");
     assertEquals(1, editor.getCursorPosition().getY());
     typeString("gg");
     assertEquals(0, editor.getCursorPosition().getY());
   }
 
   @Test
   public void cc() {
     setFileContents("abc", "def");
     typeString("ccggg");
     assertFileContents("ggg", "def");
   }
 
   @Test
   public void G() {
     setFileContents("abc", "def");
     typeString("G");
     assertEquals(1, editor.getCursorPosition().getY());
   }
 
  @Test
  public void regressionJoin() {
    setFileContents("abc", "def");
    typeString("VjJ");
    assertEquals(0, editor.getCursorPosition().getY());
  }

   @After
   public void checkUndo() {
     ensureUndoGoesToLastFileContents();
   }
 
   private void ensureUndoGoesToLastFileContents() {
     type(KeyStroke.escape());
     while (editor.hasUndo()) {
       typeString("u");
     }
     assertFileContents(lastFileContents);
   }
 
   private void assertFileContents(String... lines) {
     assertArrayEquals(lines, file.getLines());
   }
 
   private void type(KeyStroke event) {
     handler.handleKeyPress(event, editor);
   }
 
   private void typeString(String letters) {
     for (int i = 0; i < letters.length(); i++) {
       typeChar(letters.charAt(i));
     }
   }
 
   private void typeChar(char c) {
     handler.handleKeyPress(KeyStroke.fromChar(c), editor);
   }
 }
