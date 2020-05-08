 package com.id.app;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.id.editor.Editor;
 import com.id.editor.EditorList;
 import com.id.editor.Minibuffer;
 import com.id.editor.Point;
 import com.id.editor.Register;
 import com.id.editor.StackList;
 import com.id.events.KeyStroke;
 import com.id.file.File;
 import com.id.file.ModifiedListener;
 import com.id.file.Tombstone;
 import com.id.fuzzy.Finder;
 import com.id.fuzzy.Finder.Listener;
 import com.id.fuzzy.FuzzyFinderDriver;
 import com.id.git.Diff;
 import com.id.git.FileDelta;
 import com.id.git.InMemoryRepository;
 import com.id.platform.InMemoryFileSystem;
 
 
 public class ControllerTest {
   private Controller controller;
   private EditorList editorList;
   private InMemoryFileSystem fileSystem;
   private Finder fuzzyFinder;
   private Listener fuzzyListener;
   private InMemoryRepository repo;
   private HighlightState highlightState;
   private StackList stackList;
   private Minibuffer minibuffer;
   private CommandExecutor commandExecutor;
   private ViewportTracker viewportTracker;
   private FocusManager focusManager;
   private MinibufferSubsystem minibufferSubsystem;
   private Register register;
   private EditorFactory editorFactory;
   private EditorOpener editorOpener;
 
   @Before
   public void setup() {
     File files = new File("a", "b", "src/c.h", "src/c.cc", "src/d.cc");
     editorList = new EditorList();
     stackList = new StackList();
     fileSystem = new InMemoryFileSystem();
     fuzzyFinder = new Finder(files);
     fuzzyListener = mock(Finder.Listener.class);
     repo = new InMemoryRepository();
     highlightState = new HighlightState();
     minibuffer = new Minibuffer();
     focusManager = new FocusManager(editorList, stackList);
    minibufferSubsystem = new MinibufferSubsystem(minibuffer, commandExecutor, focusManager);
     viewportTracker = new ViewportTracker(focusManager);
     register = new Register();
     editorFactory = new EditorFactory(highlightState, register, viewportTracker);
     FuzzyFinderDriver fileFinderDriver = new FuzzyFinderDriver(files);
     Finder finder = new Finder(files);
     editorOpener = new EditorOpener(editorFactory, focusManager, editorList, stackList, fileSystem, finder);
     commandExecutor = new CommandExecutor(editorOpener, focusManager);
     controller = new Controller(editorList, fileSystem, fuzzyFinder, repo,
         highlightState, stackList, minibufferSubsystem, commandExecutor, null,
         fileFinderDriver, focusManager, editorOpener);
 
     fileSystem.insertFile("a", "aaa");
     fileSystem.insertFile("b", "bbb");
     fileSystem.insertFile("src/c.cc", "ccc");
     fileSystem.insertFile("src/c.h", "chc");
     fileSystem.insertFile("src/d.cc", "ddd");
   }
 
   @Test
   public void moveBetweenFilesEditingThem() {
     editorOpener.openFile("a");
     editorOpener.openFile("b");
     assertEquals("aaa", editorList.get(0).getLine(0));
     assertEquals("bbb", editorList.get(1).getLine(0));
     typeString("SxKx");
     type(KeyStroke.escape());
     typeString("KSzJz");
     assertEquals("zJz", editorList.get(0).getLine(0));
     assertEquals("xKx", editorList.get(1).getLine(0));
   }
 
   @Test
   public void controllerCanBringUpTheFuzzyFinder() {
     fuzzyFinder.addListener(fuzzyListener);
     controller.showFileFinder();
     verify(fuzzyListener).onSetVisible(true);
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void typingGoesToTheFuzzyFinderWhenItsUp() {
     controller.showFileFinder();
     fuzzyFinder.addListener(fuzzyListener);
     typeString("hi");
     verify(fuzzyListener, times(2)).onMatchesChanged(any(List.class));
   }
 
   @Test
   public void tBringsUpFuzzyFinder() {
     typeString("t");
     assertTrue(fuzzyFinder.isVisible());
   }
 
   @Test
   public void escapeQuitsFuzzyFinder() {
     typeString("t");
     type(KeyStroke.escape());
     assertFalse(fuzzyFinder.isVisible());
   }
 
   @Test
   public void selectFromFuzzyFinderOpensFile() {
     typeString("ta<CR>");
     assertFalse(fuzzyFinder.isVisible());
     assertEquals(1, editorList.size());
     assertEquals(0, editorList.getFocusedIndex());
     assertEquals("a", editorList.get(0).getFilename());
   }
 
   @Test
   public void showFuzzyFinderClearsOldQuery() {
     typeString("ta");
     type(KeyStroke.enter());
     typeString("t");
     assertEquals("", fuzzyFinder.getCurrentQuery());
   }
 
   @Test
   public void closeCurrentFile() {
     editorOpener.openFile("a");
     editorOpener.openFile("b");
     controller.closeCurrentFile();
     assertEquals(1, editorList.size());
   }
 
   @Test
   public void canImportDiffsFromGit() {
     Map<String, FileDelta> fileDeltas = new HashMap<String, FileDelta>();
     FileDelta fileDelta = new FileDelta();
     fileDelta.addNewLine(0, "aaa");  // First line of "./a" is new.
     fileDelta.addDeletedLine(0, "deleted 1");  // We deleted two lines from the end of a.
     fileDelta.addDeletedLine(0, "deleted 2");
     fileDeltas.put("a", fileDelta);
     Diff diff = new Diff(fileDeltas);
     repo.setDiffResult(diff);
     controller.importDiffsRelativeTo("HEAD");
     assertEquals(Tombstone.Status.NEW, editorList.get(0).getStatus(0));
     assertEquals(2, editorList.get(0).getGrave(0).size());
   }
 
   @Test
   public void openingNonExistentFileShouldntCrash() {
     editorOpener.openFile("doesn't exist");
   }
 
   @Test
   public void filesGetSavedToTheFileSystem() {
     Editor editor = editorOpener.openFile("a");
     typeString("SXXX");
     type(KeyStroke.escape());
     assertTrue(editor.isModified());
     ModifiedListener listener = mock(ModifiedListener.class);
     editor.addFileModifiedListener(listener);
     type(KeyStroke.fromChar('w'));
     verify(listener).onModifiedStateChanged();
     assertFalse(editor.isModified());
     assertEquals("XXX", fileSystem.getFile("a").getLine(0));
   }
 
   @Test
   public void regression_bringUpFuzzyFinderTwice() {
     typeString("ta");
     type(KeyStroke.enter());
     typeString("ta");
   }
 
   @Test
   public void highlightIsGlobal() {
     editorOpener.openFile("a");
     typeString("*");  // Sets highlight to 'aaa'.
     editorOpener.openFile("b");
     typeString("Saaa");
     assertEquals("aaa", editorList.get(1).getLine(0));
     assertTrue(editorList.get(1).isHighlight(0, 0));
   }
 
   @Test
   public void openingTheSameFileAgainRefocusesTheSpotlightOntoThatEditor() {
     editorOpener.openFile("a");
     editorOpener.openFile("b");
     editorOpener.openFile("a");
     assertEquals(0, editorList.getFocusedIndex());
   }
 
   @Test
   public void gf() {
     editorOpener.openFile("a");
     typeString("Ssrc/c.cc<ESC>gf");
     assertEquals("src/c.cc", editorList.getFocusedItem().getFilename());
   }
 
   @Test
   public void gF() {
     editorOpener.openFile("a");
     typeString("Sd<ESC>gF");
     assertEquals("src/d.cc", editorList.getFocusedItem().getFilename());
   }
 
   @Test
   public void gFOperatesOnWordNotFilename() {
     editorOpener.openFile("a");
     typeString("Sa.d<ESC>gF");
     assertEquals("src/d.cc", editorList.getFocusedItem().getFilename());
   }
 
   @Test
   public void yankRegisterIsGlobal() {
     editorOpener.openFile("a");
     typeString("Vy");
     Editor b = editorOpener.openFile("b");
     typeString("P");
     assertEquals("aaa", b.getLine(0));
   }
 
   @Test
   public void addSnippet() {
     editorOpener.openFile("a");
     typeString("V;");
     assertEquals(1, stackList.size());
   }
 
   @Test
   public void typeInSnippet() {
     editorOpener.openFile("a");
     typeString("oabc<CR>abc<ESC>");
     assertSpotlightFocused();
     typeString("V;");  // Make a snippet out of the last line.
     typeString("k");   // Move up a line in the editors.
     typeString("L");   // Move focus to stack.
     assertStackFocused();
     typeString("oend");
     assertEquals(4, editorList.get(0).getLineCount());
     assertEquals("end", editorList.get(0).getLine(3));
   }
 
   @Test
   public void moveFocusBetweenSnippets() {
     editorOpener.openFile("a");
     typeString("V;V;");
     assertEquals(2, stackList.getFocusedItem().size());
     typeString("L");
     assertTrue(stackList.isFocused());
     assertEquals(0, stackList.getFocusedItem().getFocusedIndex());
     typeString("K");
     assertEquals(0, stackList.getFocusedItem().getFocusedIndex());
     typeString("J");
     assertEquals(1, stackList.getFocusedItem().getFocusedIndex());
   }
 
   @Test
   public void qClosesSnippetWhenFocused() {
     editorOpener.openFile("a");
     typeString("V;Lq");
     assertEquals(1, editorList.size());
     assertEquals(0, stackList.size());
   }
 
   @Test
   public void focusMovesBackToEditorWhenFinalSnippetClosed() {
     editorOpener.openFile("a");
     typeString("V;Lq");
     assertTrue(editorList.isFocused());
   }
 
   @Test
   public void cantMoveFocusToEmptyStack() {
     editorOpener.openFile("a");
     typeString("L");
     assertTrue(editorList.isFocused());
   }
 
   @Test
   public void addSnippetsFromSnippet() {
     editorOpener.openFile("a");
     createSnippetFromCurrentLine();
     typeString("L");
     createSnippetFromCurrentLine();
     assertEquals(2, stackList.getFocusedItem().size());
   }
 
   @Test
   public void addingASnippetShouldntFocusTheMostRecentlyAddedOne() {
     editorOpener.openFile("a");
     createSnippetFromCurrentLine();
     typeString("L");
     createSnippetFromCurrentLine();
     assertEquals(0, stackList.getFocusedIndex());
   }
 
   @Test
   public void closingASnippetShouldMoveFocusToTheNextOneDown() {
     editorOpener.openFile("a");
     createSnippetFromCurrentLine();
     createSnippetFromCurrentLine();
     createSnippetFromCurrentLine();
     typeString("Lq");
     assertEquals(0, stackList.getFocusedIndex());
   }
 
   @Test
   public void enterInASnippetShouldJumpToThatPointInTheRealFile() {
     editorOpener.openFile("a");
     editorOpener.openFile("b");
     assertEquals(1, editorList.getFocusedIndex());
     createSnippetFromCurrentLine();
     typeString("K");
     assertEquals(0, editorList.getFocusedIndex());
     typeString("L<CR>");
     assertEquals(1, editorList.getFocusedIndex());
   }
 
   @Test
   public void openDeltasAsSnippets() {
     editorOpener.openFile("a");
     typeString("o<ESC>@");
     assertEquals(1, stackList.size());
   }
 
   @Test
   public void openDeltasAsSnippetsDoesntCreateDupes() {
     editorOpener.openFile("a");
     typeString("o<ESC>@@");
     assertEquals(1, stackList.size());
   }
 
   @Test
   public void commandsGetExecutedWhenTyped() {
     typeString(":e b<CR>");
     assertEquals(1, editorList.size());
   }
 
   @Test
   public void eOpensNewFiles() {
     typeString(":e doesnt-exist<CR>");
     assertEquals(1, editorList.size());
     assertEquals("doesnt-exist", editorList.getFocusedItem().getFilename());
   }
 
   @Test
   public void ctrlKMovesFileUpInFilelist() {
     typeString(":e a<CR>:e b<CR><C-k>");
     assertEquals(2, editorList.size());
     assertEquals("b", editorList.getFocusedItem().getFilename());
     assertEquals(0, editorList.getFocusedIndex());
   }
 
   @Test
   public void ctrlJMovesFileDownInFilelist() {
     typeString(":e a<CR>:e b<CR>K<C-j>");
     assertEquals(2, editorList.size());
     assertEquals("a", editorList.getFocusedItem().getFilename());
     assertEquals(1, editorList.getFocusedIndex());
   }
 
   @Test
   public void BGoesToTopFileInFileList() {
     typeString(":e a<CR>:e b<CR>");
     assertEquals(1, editorList.getFocusedIndex());
     typeString("B");
     assertEquals(0, editorList.getFocusedIndex());
   }
 
   @Test
   public void QClosesAllSnippets() {
     typeString(":e a<CR>ihello<CR>world<ESC>");
     typeString("V;kV;");
     assertEquals(1, stackList.size());
     typeString("Q");
     assertEquals(0, stackList.size());
   }
 
   @Test
   public void QPutsFocusBackOnSpotlight() {
     typeString(":e a<CR>ihello<CR>world<ESC>");
     typeString("V;LQ");
     assertSpotlightFocused();
   }
 
   @Test
   public void outdentDoesntLeaveCursorPastEndOfLine() {
     typeString(":e a<CR>ia<CR>b<CR>c<CR>d<CR><ESC>");
     typeString(":3<CR>");
     Point cursor = editorList.get(0).getCursorPosition();
     assertEquals(2, cursor.getY());
   }
 
   @Test
   public void gfOpensFilesThatDontExist() {
     typeString(":e a<CR>");
     typeString("A abc<ESC>gf");
     assertEquals(2, editorList.size());
     assertEquals("abc", editorList.getFocusedItem().getFilename());
   }
 
   @Test
   public void newFilesStartModified() {
     typeString(":e doesnt-exist<CR>");
     assertEquals(1, editorList.size());
     assertTrue(editorList.getFocusedItem().isModified());
     typeString("w");
     assertFalse(editorList.getFocusedItem().isModified());
   }
 
   @Test
   public void openingAFilePutsItUnderneathTheCurrentOne() {
     typeString(":e a<CR>:e b<CR>K");
     assertEquals(0, editorList.getFocusedIndex());
     typeString(":e c<CR>");
     assertEquals(1, editorList.getFocusedIndex());
   }
 
   @Test
   public void controllerStartsWithStackInvisible() {
     assertTrue(stackList.isHidden());
   }
 
   @Test
   public void creatingASnippetMakesTheStackVisible() {
     typeString(":e a<CR>V;");
     assertFalse(stackList.isHidden());
   }
 
   @Test
   public void previousHighlightsAccessibleWithQuestionMark() {
     typeString(":e doesnt-exist<CR>");
     typeString("ia b b c<ESC>*hh*");
     // 'b' should be highlighted, so there should be two matches.
     assertEquals(2, editorList.get(0).getHighlightMatchCount());
     typeString("?<DOWN><CR>");
     // 'c' should be highlighted, so there should be one match.
     assertEquals(1, editorList.get(0).getHighlightMatchCount());
   }
 
   @Test
   public void closingTheFinalSnippetMakesTheStackInvisible() {
     typeString(":e a<CR>V;Lq");
     assertTrue(stackList.isHidden());
   }
 
   @Test
   public void ctrl6OpensOtherFilesWithDifferentExtensions() {
     typeString(":e src/c.h<CR><C-6>");
     assertEquals(2, editorList.size());
   }
 
   @Test
   public void reloadCurrentFile() {
     typeString(":e src/c.h<CR>");
     String startContents = editorList.getFocusedItem().getLine(0);
     typeString("Stest<ESC>");
     assertEquals("test", editorList.getFocusedItem().getLine(0));
     typeString(":e<CR>");
     String endContents = editorList.getFocusedItem().getLine(0);
     assertEquals(startContents, endContents);
   }
 
   @Test
   public void multipleStacks() {
     typeString(":e a<CR>");
     typeString("V;]V;");
     assertEquals(2, stackList.size());
   }
 
   private void createSnippetFromCurrentLine() {
     typeString("V;");
   }
 
   private void assertSpotlightFocused() {
     assertTrue(editorList.isFocused());
     assertFalse(stackList.isFocused());
   }
 
   private void assertStackFocused() {
     assertTrue(stackList.isFocused());
     assertFalse(editorList.isFocused());
   }
 
   private void type(KeyStroke keyStroke) {
     controller.handleKeyStroke(keyStroke);
   }
 
   private void typeString(String string) {
     for (KeyStroke keyStroke : KeyStroke.fromString(string)) {
       type(keyStroke);
     }
   }
 }
