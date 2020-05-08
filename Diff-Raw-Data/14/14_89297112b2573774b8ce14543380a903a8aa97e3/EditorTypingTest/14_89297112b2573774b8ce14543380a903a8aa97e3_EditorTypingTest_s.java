 package com.id.editor;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.id.events.KeyStroke;
 import com.id.test.EditorTestBase;
 
 public class EditorTypingTest extends EditorTestBase {
 
   @Before
   public void init() {
     setFileContents();
   }
 
   @After
   public void checkUndo() {
     ensureUndoGoesToLastFileContents();
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
 
   @Test
   public void xPullsIntoRegister() {
     setFileContents("abc");
     typeString("xp");
     assertFileContents("bac");
   }
 
   @Test
   public void visualLineRangeDeletePullsIntoRegister() {
     setFileContents("abc", "def", "ghi");
     typeString("Vjx");
     assertFileContents("ghi");
     typeString("p");
     assertFileContents("ghi", "abc", "def");
   }
 
   @Test
   public void visualDeleteOnlyLineInFile() {
     setFileContents("abc");
     typeString("Vxp");
   }
 
   @Test
   public void putBefore() {
     setFileContents("abc", "def");
     typeString("VxP");
     assertFileContents("abc", "def");
   }
 
   @Test
   public void yank() {
     setFileContents("abc");
     typeString("vy");
     assertFalse(editor.isInVisual());
     typeString("p");
     assertFileContents("aabc");
   }
 
   @Test
   public void deleteVisualYanks() {
     setFileContents("abc");
     typeString("Vdpp");
     setFileContents("abc", "abc");
   }
 
   @Test
   public void dd() {
     setFileContents("abc", "def");
     typeString("dd");
     assertFileContents("def");
   }
 
   @Test
   public void autoIndent() {
     setFileContents("  abc");
     typeString("o");
     assertFileContents("  abc", "  ");
     assertCursorPosition(1, 2);
     assertEquals(2, editor.getCursorPosition().getX());
     typeString("abc");
     assertFileContents("  abc", "  abc");
     type(KeyStroke.escape());
     typeString("Oabc");
     assertFileContents("  abc", "  abc", "  abc");
   }
 
   @Test
   public void autoIndentWithEnter() {
     setFileContents();
     typeString("i  abc");
     type(KeyStroke.enter());
     typeString("abc");
     assertFileContents("  abc", "  abc");
   }
 
   @Test
   public void tabInsertsTwoSpaces() {
     setFileContents();
     typeString("i");
     type(KeyStroke.tab());
     assertFileContents("  ");
   }
 
   @Test
   public void tabAlignsToIndentationLevel() {
     setFileContents(" ");
     typeString("A");
     type(KeyStroke.tab());
     assertFileContents("  ");
   }
 
   @Test
   public void tabOnEndOfLine() {
     setFileContents("abc");
     typeString("A");
     type(KeyStroke.tab());
     assertFileContents("abc ");
     type(KeyStroke.tab());
     assertFileContents("abc   ");
   }
 
   @Test
   public void backspaceSoftTabsAtStartOfLine() {
     setFileContents("  abc");
     typeString("la");
     type(KeyStroke.backspace());
     assertFileContents("abc");
     assertCursorPosition(0, 0);
   }
 
   @Test
   public void dontBackspaceSoftTabsAtEndOfLine() {
     setFileContents("abc   ");
     typeString("A");
     type(KeyStroke.backspace());
     assertFileContents("abc  ");
   }
 
   @Test
   public void movingPreviousShouldVisitEachOccurrenceOnTheSameLine() {
     setFileContents("abc abc abc");
     typeString("*$NN");
     assertCursorPosition(0, 4);
   }
 
   @Test
   public void moveWithArrows() {
     setFileContents("abc", "abc");
     type(KeyStroke.down());
     assertCursorPosition(1, 0);
     type(KeyStroke.right());
     assertCursorPosition(1, 1);
     type(KeyStroke.up());
     assertCursorPosition(0, 1);
     type(KeyStroke.left());
     assertCursorPosition(0, 0);
   }
 
   @Test
   public void typingNonLetterKeysInInsertModeHasNoEffect() {
     typeString("i");
     type(KeyStroke.up());
     assertFileContents();
   }
 
   @Test
   public void escapeExitsInsertMode() {
     typeString("i");
     type(KeyStroke.escape());
     assertFalse(editor.isInInsertMode());
   }
 }
