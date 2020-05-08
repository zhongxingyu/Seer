 package com.id.editor;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.id.events.KeyStroke;
 import com.id.file.ModifiedListener;
 import com.id.file.Tombstone;
 import com.id.test.EditorTestBase;
 
 public class EditorTypingTest extends EditorTestBase {
 
   @Before
   public void init() {
     setFileContents();
   }
 
   @After
   public void checkConsistency() {
     assertTrue(editor.isCursorInBounds());
     ensureUndoGoesToLastFileContents();
   }
 
   @Test
   public void changeLine() {
     setFileContents("abc");
     typeString("lCabc<ESC>");
     assertFileContents("aabc");
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
     typeString("<ESC>");
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
     typeString("<C-f>");
     assertEquals(2, editor.getCursorPosition().getY());
     typeString("<C-b>");
     assertEquals(0, editor.getCursorPosition().getY());
   }
 
   @Test
   public void join() {
     setFileContents("abc", "def", "ghi");
     typeString("jVJ");
     assertFalse(editor.isInVisual());
     assertFileContents("abc", "def ghi");
   }
 
   @Test
   public void star() {
     setFileContents("abc", "def", "abc");
     typeString("*");
     assertTrue(editor.isHighlight(0, 0));
     assertFalse(editor.isHighlight(1, 0));
     assertTrue(editor.isHighlight(2, 0));
     typeString("\\\\");
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
     assertFileContents("", "abc", "abc");
   }
 
   @Test
   public void dd() {
     setFileContents("abc", "def");
     typeString("dd");
     assertFileContents("def");
     assertCursorPosition(0, 0);
   }
 
   @Test
   public void visualModePutReplacesCurrentLine() {
     setFileContents("abc", "efg");
     typeString("Vyj");
     typeString("Vp");
     assertFileContents("abc", "abc");
     assertFalse(editor.isInVisual());
   }
 
   @Test
   public void ddYankSingleLine() {
     setFileContents("abc");
     typeString("ddp");
     assertFileContents("", "abc");
     assertCursorPosition(1, 0);
   }
 
   @Test
   public void cursorPositionAfterPut() {
     setFileContents("abc", "cdf", "efg");
     typeString("jjddp");
     assertLineContents(2, "efg");
     assertCursorPosition(2, 0);
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
     typeString("<ESC>Oabc");
     assertFileContents("  abc", "  abc", "  abc");
   }
 
   @Test
   public void autoIndentWithEnter() {
     setFileContents();
     typeString("i  abc<CR>abc");
     assertFileContents("  abc", "  abc");
   }
 
   @Test
   public void tabInsertsTwoSpaces() {
     setFileContents();
     typeString("i<TAB>");
     assertFileContents("  ");
   }
 
   @Test
   public void tabAlignsToIndentationLevel() {
     setFileContents(" ");
     typeString("A<TAB>");
     assertFileContents("  ");
   }
 
   @Test
   public void tabOnEndOfLine() {
     setFileContents("abc");
     typeString("A<TAB>");
     assertFileContents("abc ");
     type(KeyStroke.tab());
     assertFileContents("abc   ");
   }
 
   @Test
   public void backspaceSoftTabsAtStartOfLine() {
     setFileContents("  abc");
     typeString("la<BS>");
     assertFileContents("abc");
     assertCursorPosition(0, 0);
   }
 
   @Test
   public void dontBackspaceSoftTabsAtEndOfLine() {
     setFileContents("abc   ");
     typeString("A<BS>");
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
     typeString("i<UP>");
     assertFileContents();
   }
 
   @Test
   public void escapeExitsInsertMode() {
     typeString("i<ESC>");
     assertFalse(editor.isInInsertMode());
   }
 
   @Test
   public void joinOnLastLineOfMultiLineFileShouldntCrash() {
     setFileContents("abc", "abc");
     typeString("jVJ");
     assertFileContents("abc", "abc");
   }
 
   @Test
   public void modifiedStatusGetsUpdatedWhenLettersAreInserted() {
     setFileContents("abc");
     ModifiedListener modifiedListener = mock(ModifiedListener.class);
     editor.addFileModifiedListener(modifiedListener);
     assertFalse(editor.isModified());
     typeString("i");
     assertFalse(editor.isModified());
     typeString("a");
     verify(modifiedListener).onModifiedStateChanged();
     assertTrue(editor.isModified());
   }
 
   @Test
   public void yankThenPutOverMultipleLinesRestoresMarkersToOriginalState() {
     setFileContents("a", "b", "c", "d", "e");
     typeString("VjjdP");
     assertTrue(editor.isMarkersClear());
   }
 
   @Test
   public void find() {
     setFileContents("abcdabcd");
     typeString("fd");
     assertCursorPosition(0, 3);
     assertFalse(editor.isInFindMode());
   }
 
   @Test
   public void findRepeat() {
     setFileContents("abcdabcd");
     typeString("fd;");
     assertCursorPosition(0, 7);
   }
 
   @Test
   public void findRepeatThenGoBack() {
     setFileContents("abcdabcd");
     typeString("fd;,");
     assertCursorPosition(0, 3);
   }
 
   @Test
   public void findNonExistentCharExitsFindMode() {
     setFileContents("abc");
     typeString("fx");
     assertFalse(editor.isInFindMode());
   }
 
   @Test
   public void findBackwards() {
     setFileContents("abc");
     typeString("$Fa");
     assertCursorPosition(0, 0);
   }
 
   @Test
   public void findNonExistentCharCausesFindToBeRemembered() {
     setFileContents("abc", "abcx");
     typeString("fxj;");
     assertCursorPosition(1, 3);
   }
 
   @Test
   public void search() {
     setFileContents("abc", "def", "ghi", "abc");
     typeString("/def");
     assertTrue(editor.isInSearchMode());
     assertTrue(editor.isSearchHighlight(1, 0));
     typeString("g");
     assertFalse(editor.isSearchHighlight(1, 0));
     typeString("<BS>");
     assertTrue(editor.isSearchHighlight(1, 0));
   }
 
   @Test
   public void searchMovement() {
     setFileContents("abc", "def", "ghi", "abc");
     typeString("/def");
     assertCursorPosition(1, 0);
   }
 
   @Test
   public void searchShouldModifyTheHighlight() {
     setFileContents("abc", "def", "def");
     typeString("/def<CR>");
     assertTrue(editor.isHighlight(2, 0));
   }
 
   @Test
   public void starAfterClear() {
     setFileContents("abc");
     typeString("*\\*");
     assertTrue(editor.isHighlight(0, 0));
   }
 
   @Test
   public void goToChangeMarkersWithNextAndPrevious() {
     setFileContents("a", "a", "a");
     typeString("jShi<ESC>kn");
     assertCursorPosition(1, 0);
     typeString("jN");
     assertCursorPosition(1, 0);
   }
 
   @Test
   public void bigInsertSkipsWhitespace() {
     setFileContents("  s  ");
     typeString("Ix");
     assertFileContents("  xs  ");
   }
 
   @Test
   public void bigInsertInsertsAtEndIfTheLineIsAllWhitespace() {
     setFileContents("    ");
     typeString("Ix");
     assertFileContents("    x");
   }
 
   @Test
   public void escapePreventsSearchFromSettingANewHighlightPattern() {
     setFileContents("abc");
     typeString("/bc<ESC>");
     assertFalse(editor.isHighlight(0, 2));
   }
 
   @Test
   public void escapeFromSearchReturnsCursorToOriginalPosition() {
     setFileContents("abc");
     typeString("/bc<ESC>");
     assertCursorPosition(0, 0);
   }
 
   @Test
   public void searchIsIncrementalAndMatchesPartsOfWords() {
     setFileContents("abc");
     typeString("/b");
     assertCursorPosition(0, 1);
   }
 
   @Test
   public void enterLeavesCursorWhereItIsInSearchMode() {
     setFileContents("abc");
     typeString("/b<CR>");
     assertCursorPosition(0, 1);
   }
 
   @Test
   public void enterOnALineWithNothingButAutoIndentationShouldClearThatLine() {
     setFileContents("  abc");
     typeString("o<CR>");
     assertEquals("", editor.getLine(1));
   }
 
   @Test
   public void enterOnALineWithNothingButAutoIndentationShouldWorkWhenSplittingLines() {
     setFileContents("  abc");
     typeString("fbi<CR>");
     assertFileContents(
         "  a",
         "  bc");
     typeString("<CR>");
     assertFileContents(
         "  a",
         "",
         "  bc");
     typeString("X");
     type(KeyStroke.enter());
     assertFileContents(
         "  a",
         "",
         "  X",
         "  bc");
   }
 
   @Test
   public void escapeFromLineWithNothingButAutoIndentationShouldClearTheLine() {
     setFileContents("  abc");
     typeString("o<ESC>");
     assertFileContents("  abc", "");
   }
 
   @Test
   public void undoLineForInsertedLine() {
     typeString("iabc<ESC>U");
     assertFileContents();
   }
 
   @Test
   public void undoLineForDeletedLines() {
     setFileContents("abc", "def", "ghi");
     typeString("jVjdU");
     assertFileContents("abc", "def", "ghi");
     assertAllStatus(Tombstone.Status.NORMAL);
   }
 
   @Test
   public void undoLineDoesntCauseFileToBecomeUnmodified() {
     setFileContents("abc", "def", "ghi");
     typeString("lCd<ESC>jU");
     assertTrue(editor.isModified());
     assertTrue(file.isModified());
   }
 
   @Test
   public void testIsCursorInBounds() {
     setFileContents("abc");
     typeString("A");
     assertTrue(editor.isCursorInBounds());
   }
 
   @Test
   public void undoLineWorksOverVisualRange() {
     setFileContents("abc", "def", "ghi");
     typeString("xjxjxVggU");
     assertFileContents("abc", "def", "ghi");
   }
 
   @Test
   public void substituteLinePreservesAutoIndenting() {
     setFileContents("  abc");
     typeString("S");
     assertFileContents("  ");
   }
 
   @Test
   public void substitutingALineConstitutesLeavingUnnecessaryAutoIndenting() {
     setFileContents("  abc");
     typeString("S<ESC>");
     assertFileContents("");
   }
 
   @Test
   public void endOfLineWorksOnEmptyLine() {
     setFileContents("");
     typeString("$");
   }
 
   @Test
   public void joinCompactsWhitespace() {
     setFileContents("abc", "    def");
     typeString("VJ");
     assertFileContents("abc def");
   }
 
   @Test
   public void wipeAwayMarkers() {
     setFileContents("abc");
     typeString("Sx<ESC>W");
     assertAllStatus(Tombstone.Status.NORMAL);
     // Wiping change markers leaves them in an inconsistent state.
     setOkForChangeMarkersToBeInconsistentAfterUndo();
   }
 
   @Test
   public void wipeAwayMarkersRange() {
     setFileContents("abc", "def", "ghi");
     typeString("xjxjxVggW");
     assertAllStatus(Tombstone.Status.NORMAL);
     // Wiping change markers leaves them in an inconsistent state.
     setOkForChangeMarkersToBeInconsistentAfterUndo();
   }
 
   @Test
   public void changeWord() {
     setFileContents("this is a test");
     typeString("cwthat");
     assertFileContents("that is a test");
     assertTrue(editor.isInInsertMode());
   }
 
   @Test
   public void changeWordAtEndOfLine() {
     setFileContents("test");
     typeString("cw");
     assertFileContents("");
     assertTrue(editor.isInInsertMode());
   }
 
   @Test
   public void changeToEndOfLine() {
     setFileContents("this is a test");
     typeString("c$that");
     assertFileContents("that");
     assertTrue(editor.isInInsertMode());
   }
 
   @Test
   public void deleteWord() {
     setFileContents("this is a test");
     typeString("dw");
     assertFileContents("is a test");
     assertFalse(editor.isInInsertMode());
   }
 
   @Test
   public void deleteWordAtEndOfLine() {
     setFileContents("blah");
     typeString("dw");
     assertFileContents("");
   }
 
   @Test
   public void deleteEndOfLine() {
     setFileContents("tabcd efgh ijkl");
     typeString("lD");
     assertFileContents("t");
     assertFalse(editor.isInInsertMode());
   }
 
   @Test
   public void outdent() {
     setFileContents("  blah");
     typeString("\\<");
     assertFileContents("blah");
   }
 
   @Test
   public void indent() {
     setFileContents("  blah");
     typeString("\\>");
     assertFileContents("    blah");
   }
 
   @Test
   public void indentWithOddNumberOfLeadingSpaces() {
     setFileContents("   blah");
     typeString("\\>");
     assertFileContents("    blah");
   }
 
   @Test
   public void creatingSnippetExitsVisual() {
     setFileContents("abc");
     typeString("V;");
     assertFalse(editor.isInVisual());
   }
 }
