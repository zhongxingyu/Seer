 package com.id.editor;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import com.id.app.HighlightState;
 import com.id.data.Data;
 import com.id.editor.Visual.Mode;
 import com.id.events.EditorKeyHandler;
 import com.id.events.KeyStroke;
 import com.id.events.KeyStrokeHandler;
 import com.id.file.File;
 import com.id.file.File.Listener;
 import com.id.file.FileView;
 import com.id.file.Grave;
 import com.id.file.ModifiedListener;
 import com.id.file.Range;
 import com.id.file.Tombstone.Status;
 import com.id.git.FileDelta;
 import com.id.platform.FileSystem;
 
 public class Editor implements KeyStrokeHandler, HighlightState.Listener, File.Listener {
   private static final int TAB_SIZE = 2;
 
   public interface EditorView {
     void moveViewportToIncludePoint(Point point);
     void recenterScreenOnPoint(Point point);
     int getViewportHeight();
     boolean isVisible(Point point);
     int getTopLineVisible();
     void setTopLineVisible(int topLine);
   }
 
   public interface EditorEnvironment {
     void openFile(String filename);
     void addSnippet(FileView fileView);
   }
 
   public enum FindMode {
     NONE,
     FIND_FORWARDS,
     FIND_BACKWARDS,
     TIL_FORWARDS,
     TIL_BACKWARDS,;
 
     public FindMode opposite() {
       switch (this) {
       case FIND_BACKWARDS: return FIND_FORWARDS;
       case FIND_FORWARDS: return FIND_BACKWARDS;
       case TIL_FORWARDS: return TIL_BACKWARDS;
       case TIL_BACKWARDS: return TIL_FORWARDS;
       case NONE: return NONE;
       }
       return null;
     }
 
     private int findIn(Cursor cursor, FileView file, char letter) {
       switch (this) {
       case FIND_FORWARDS: return file.findNextLetter(cursor.getY(), cursor.getX(), letter);
       case FIND_BACKWARDS: return file.findPreviousLetter(cursor.getY(), cursor.getX(), letter);
       default: return -1;
       }
     }
   }
 
   class EmptyView implements EditorView {
     @Override
     public void moveViewportToIncludePoint(Point point) {
       // Do nothing.
     }
 
     @Override
     public void recenterScreenOnPoint(Point point) {
       // Do nothing.
     }
 
     @Override
     public int getViewportHeight() {
       return 10;
     }
 
     @Override
     public boolean isVisible(Point point) {
       return true;
     }
 
     @Override
     public int getTopLineVisible() {
       return 0;
     }
 
     @Override
     public void setTopLineVisible(int topLine) {
       // Do nothing.
     }
   }
 
   public static class EmptyEditorEnvironment implements EditorEnvironment {
     @Override
     public void openFile(String filename) {
       // Do nothing.
     }
 
     @Override
     public void addSnippet(FileView fileView) {
       // Do nothing.
     }
   }
 
   private final FileView file;
   private final Cursor cursor;
   private final Visual visual;
   private boolean inInsertMode = false;
   private EditorView view = new EmptyView();
   private EditorEnvironment environment = new EmptyEditorEnvironment();
 
   private final EditorKeyHandler keyHandler;
   private final List<File.Listener> fileListeners = new ArrayList<File.Listener>();
   private final List<ModifiedListener> fileModifiedListeners = new ArrayList<ModifiedListener>();
   private FindMode findMode = FindMode.NONE;
   private char lastFindLetter = 0;
   private FindMode lastFindMode = FindMode.NONE;
   private Search currentSearch = null;
   private Highlight highlight = new EmptyHighlight();
   private final HighlightState highlightState;
   private boolean justInsertedAutoIndent = false;
   private final Register register;
 
   public Editor(FileView fileView, HighlightState highlightState,
       Register register, EditorEnvironment editorEnvironment) {
     this.file = fileView;
     this.highlightState = highlightState;
     this.register = register;
     this.environment = editorEnvironment;
     this.cursor = new Cursor();
     this.visual = new Visual(this.cursor);
     this.highlightState.addListener(this);
     addFileListener(highlight);
     addFileListener(this);
     onHighlightStateChanged();
     cursor.addListner(new Cursor.Listener() {
       @Override
       public void onMoved(int y, int x) {
         view.moveViewportToIncludePoint(new Point(y, x));
       }
 
       @Override
       public void onJumped(int y, int x) {
         Point point = new Point(y, x);
         if (!view.isVisible(point)) {
           view.recenterScreenOnPoint(point);
         }
       }
     });
     keyHandler = new EditorKeyHandler();
   }
 
   public String getLine(int y) {
     return file.getLine(y);
   }
 
   public int getLineCount() {
     return file.getLineCount();
   }
 
   public Point getCursorPosition() {
     return cursor.getPoint();
   }
 
   public int getRealCursorY() {
     return getCursorPosition().getY() + file.getStart();
   }
 
   // Keyboard commands.
   public void down() {
     cursor.moveBy(1, 0);
     applyCursorConstraints();
   }
 
   public void up() {
     cursor.moveBy(-1, 0);
     applyCursorConstraints();
   }
 
   public void left() {
     cursor.moveBy(0, -1);
     applyCursorConstraints();
   }
 
   public void right() {
     cursor.moveBy(0, 1);
     applyCursorConstraints();
   }
 
   public void insert() {
     inInsertMode = true;
     cursor.setDefaultX(cursor.getX());
   }
 
   private void applyCursorConstraints() {
     int isInNormalMode = isInInsertMode() ? 0 : 1;
     cursor.constrainY(0, file.getLineCount() - 1);
     cursor.constrainX(0, getCurrentLineLength() - isInNormalMode);
   }
 
   public boolean isCursorInBounds() {
     if (file.getLineCount() == 0) {
       return true;
     }
     if (getCurrentLineLength() == 0) {
       return cursor.getX() == 0;
     }
     int isInNormalMode = isInInsertMode() ? 0 : 1;
     return cursor.isInRangeY(0, file.getLineCount() - 1) &&
         cursor.isInRangeX(0, getCurrentLineLength() - isInNormalMode);
   }
 
   private String getCurrentLine() {
     return file.getLine(cursor.getY());
   }
 
   public boolean isInInsertMode() {
     return inInsertMode;
   }
 
   public void escape() {
     if (visual.isOn()) {
       visual.toggleMode(Mode.NONE);
       return;
     }
     if (isInInsertMode()) {
       inInsertMode = false;
      if (justInsertedAutoIndent) {
         file.changeLine(cursor.getY(), "");
       }
       justInsertedAutoIndent = false;
       file.breakPatch();
       cursor.moveBy(0, -1);
       applyCursorConstraints();
     }
   }
 
   public void onLetterTyped(char keyChar) {
     if (!isInInsertMode()) {
       throw new IllegalStateException("Can't type letters unless in insert mode.");
     }
     startPatchIfNecessary();
 
     String text = "" + keyChar;
     file.insertText(cursor.getY(), cursor.getX(), text);
     cursor.moveBy(0, text.length());
     justInsertedAutoIndent = false;
   }
 
   private void startPatch() {
     if (isInVisual()) {
       file.startPatchAt(visual.getStartPoint().getY(), visual.getStartPoint().getX());
     } else {
       file.startPatchAt(cursor.getY(), cursor.getX());
     }
   }
 
   private void startPatchIfNecessary() {
     if (!file.isInPatch()) {
       startPatch();
     }
   }
 
   public void undo() {
     Point position = file.undo();
     if (position == null) {
       return;
     }
     cursor.moveTo(position.getY(), position.getX());
     applyCursorConstraints();
   }
 
   public void redo() {
     Point position = file.redo();
     if (position == null) {
       return;
     }
     cursor.moveTo(position.getY(), position.getX());
     applyCursorConstraints();
   }
 
   public void addEmptyLine() {
     addLineAt(1, getIndentForLine(cursor.getY()));
   }
 
   public void addEmptyLinePrevious() {
     addLineAt(0, getIndentForLine(cursor.getY()));
   }
 
   private void addLineAt(int dy, String text) {
     startPatchIfNecessary();
     if (file.isEmpty()) {
       file.insertLine(0, "");
     }
     int y = cursor.getY() + dy;
     if (justInsertedAutoIndent) {
       file.changeLine(cursor.getY(), "");
     }
     file.insertLine(Math.min(file.getLineCount(), y), text);
     justInsertedAutoIndent = true;
     cursor.moveTo(y, 0);
     appendEnd();
   }
 
   private String getIndentForLine(int y) {
     if (file.isEmpty()) {
       return "";
     }
     String line = getLine(y);
     StringBuffer indent = new StringBuffer();
     for (int i = 0; i < line.length(); i++) {
       if (!isWhitespace(line.charAt(i))) {
         break;
       }
       indent.append(line.charAt(i));
     }
     return indent.toString();
   }
 
   private static boolean isWhitespace(char c) {
     return c == ' ';
   }
 
   public void backspace() {
     startPatchIfNecessary();
     if (cursor.getX() == 0) {
       if (cursor.getY() == 0) {
         return;
       }
       String line = file.removeLine(cursor.getY());  // This moves the cursor up by one.
       int targetX = getCurrentLine().length();
       file.insertText(cursor.getY(), targetX, line);
       cursor.moveTo(cursor.getY(), targetX);
     } else {
       int charsToRemove = getSpacesBackIncludingSoftTabs(getCurrentLine(), cursor.getX(), TAB_SIZE);
       file.removeText(cursor.getY(), cursor.getX() - charsToRemove, charsToRemove);
       cursor.moveBy(0, -charsToRemove);
     }
   }
 
   public static int getSpacesBackIncludingSoftTabs(String line, int x, int tabSize) {
     if (x == 0) {
       return 0;
     }
     for (int i = 0; i < x; i++) {
       if (!isWhitespace(line.charAt(i))) {
         return 1;
       }
     }
     return x % tabSize == 0 ? tabSize : x % tabSize;
   }
 
   public void append() {
     insert();
     cursor.moveBy(0, 1);
     applyCursorConstraints();
   }
 
   public void appendEnd() {
     cursor.moveTo(cursor.getY(), getCurrentLineLength());
     insert();
   }
 
   public void insertStart() {
     cursor.moveTo(cursor.getY(), getFirstNonWhitespace(cursor.getY()));
     insert();
   }
 
   private int getFirstNonWhitespace(int y) {
     String line = getLine(y);
     for (int i = 0; i < line.length(); i++) {
       if (!isWhitespace(line.charAt(i))) {
         return i;
       }
     }
     return line.length();
   }
 
   private int getCurrentLineLength() {
     if (file.isEmpty()) {
       return 0;
     }
     return getCurrentLine().length();
   }
 
   public void enter() {
     assert isInInsertMode();
     startPatchIfNecessary();
     if (file.isEmpty()) {
       addEmptyLine();
       return;
     }
     int oldY = cursor.getY();
     String indentText = getIndentForLine(oldY);
     file.splitLine(cursor.getY(), cursor.getX(), indentText);
     cursor.moveBy(1, 0);
     cursor.moveXTo(indentText.length());
     if (justInsertedAutoIndent) {
       file.changeLine(oldY, "");
     }
     justInsertedAutoIndent = true;
   }
 
   public String getFilename() {
     return file.getFilename();
   }
 
   public void toggleVisual(Mode mode) {
     visual.toggleMode(mode);
   }
 
   public boolean isInVisual(int y, int x) {
     return visual.contains(new Point(y, x));
   }
 
   public void changeToEndOfLine() {
     startPatch();
     file.removeText(cursor.getY(), cursor.getX());
     append();
   }
 
   public void deleteToEndOfLine() {
     startPatch();
     file.removeText(cursor.getY(), cursor.getX());
     file.breakPatch();
     applyCursorConstraints();
   }
 
   public void deleteLine() {
     register.setContents(new TextFragment(Visual.Mode.LINE, false, getCurrentLine()));
     startPatch();
     int y = cursor.getY();
     file.removeLine(y);
     file.breakPatch();
     cursor.moveTo(y, 0);
     applyCursorConstraints();
   }
 
   public void moveCursorToEndOfLine() {
     int x = Math.max(0, getCurrentLineLength() - (isInInsertMode() ? 0 : 1));
     cursor.moveTo(cursor.getY(), x);
   }
 
   public void moveCursorToStartOfLine() {
     cursor.moveTo(cursor.getY(), 0);
   }
 
   public void moveCursorToNextWord() {
     int nextSpace = file.findNextSpace(cursor.getY(), cursor.getX());
     if (nextSpace == -1) {
       moveCursorToEndOfLine();
       return;
     }
 
     int nextLetter = file.findNextLetter(cursor.getY(), nextSpace);
     if (nextLetter == -1) {
       moveCursorToEndOfLine();
       return;
     }
 
     cursor.moveTo(cursor.getY(), nextLetter);
   }
 
   public Grave getGrave(int y) {
     return file.getGrave(y);
   }
 
   public Status getStatus(int y) {
     return file.getStatus(y);
   }
 
   public void delete() {
     delete(true);
   }
 
   public void delete(boolean breakPatch) {
     startPatch();
     if (isInVisual()) {
       register.setContents(visual.getRegister(file));
       Point cursorPoint = visual.getStartPoint();
       visual.removeFrom(file);
       cursor.moveTo(cursorPoint.getY(), cursorPoint.getX());
       visual.toggleMode(Visual.Mode.NONE);
     } else {
       register.setContents(new TextFragment(Visual.Mode.CHAR, false, "" + getCharUnderCursor()));
       file.removeText(cursor.getY(), cursor.getX(), 1);
     }
     if (breakPatch) {
       file.breakPatch();
     }
     applyCursorConstraints();
   }
 
   private char getCharUnderCursor() {
     return file.getLine(cursor.getY()).charAt(cursor.getX());
   }
 
   public boolean isInVisual() {
     return visual.isOn();
   }
 
   public void substitute() {
     insert();
     delete(false);
   }
 
   public boolean hasUndo() {
     return file.hasUndo();
   }
 
   public void substituteLine() {
     if (isInVisual()) {
       substitute();
       return;
     }
     int indentLength = getIndentForLine(cursor.getY()).length();
     moveCursorToStartOfLine();
     startPatch();
     file.removeText(cursor.getY(), indentLength);
     appendEnd();
     justInsertedAutoIndent = true;
   }
 
   public void addFileListener(Listener listener) {
     fileListeners.add(listener);
     file.addListener(listener);
   }
 
   private void removeFileListener(Listener listener) {
     fileListeners.remove(listener);
     file.removeListener(listener);
   }
 
   public void addFileModifiedListener(ModifiedListener listener) {
     fileModifiedListeners.add(listener);
     file.addModifiedListener(listener);
   }
 
   public void setView(EditorView view) {
     this.view = view;
   }
 
   public void downPage() {
     cursor.moveBy(view.getViewportHeight() - 1, 0);
     applyCursorConstraints();
   }
 
   public void upPage() {
     cursor.moveBy(-(view.getViewportHeight() - 1), 0);
     applyCursorConstraints();
   }
 
   public void join() {
     if (!isInVisual()) {
       throw new IllegalStateException();
     }
     startPatch();
     file.joinRange(visual.getStartPoint().getY(), visual.getEndPoint().getY());
     visual.toggleMode(Visual.Mode.NONE);
     file.breakPatch();
     applyCursorConstraints();
   }
 
   public void setHighlightPattern(Pattern pattern) {
     highlightState.setHighlightPattern(pattern);
   }
 
   private void setHighlight(Highlight highlight) {
     removeFileListener(this.highlight);
     this.highlight = highlight;
     addFileListener(highlight);
   }
 
   public boolean isHighlight(int y, int x) {
     return highlight.isHighlighted(y, x);
   }
 
   public void highlightWordUnderCursor() {
     setHighlightPattern(Patterns.wholeWord(file.getWordUnder(cursor.getY(), cursor.getX())));
   }
 
   public void clearHighlight() {
     setHighlightPattern(null);
   }
 
   @Override
   public void onHighlightStateChanged() {
     setHighlight(new CachingHighlight(highlightState.getHighlightPattern(), file.getLineList()));
   }
 
   public void recenter() {
     view.recenterScreenOnPoint(cursor.getPoint());
   }
 
   public void next() {
     // TODO(koz): We should distinguish between having a search with no matches
     // and not having a search.
     if (highlight.getMatchCount() > 0) {
       Point point = highlight.getNextMatch(cursor.getY(), cursor.getX());
       if (point != null) {
         cursor.moveTo(point);
       }
       return;
     }
     Point point = file.getNextModifiedPoint(cursor.getY(), cursor.getX());
     if (point != null) {
       cursor.moveTo(point);
     }
   }
 
   public void previous() {
     // TODO(koz): We should distinguish between having a search with no matches
     // and not having a search.
     if (highlight.getMatchCount() > 0) {
       Point point = highlight.getPreviousMatch(cursor.getY(), cursor.getX());
       if (point != null) {
         cursor.moveTo(point);
       }
       return;
     }
     Point point = file.getPreviousModifiedPoint(cursor.getY(), cursor.getX());
     if (point != null) {
       cursor.moveTo(point);
     }
   }
 
   public void moveCursorToStartOfFile() {
     cursor.moveTo(0, 0);
   }
 
   public void moveCursorToEndOfFile() {
     cursor.moveTo(getLineCount() - 1, 0);
   }
 
   public void moveCursorTo(int y, int x) {
     cursor.moveTo(y, x);
   }
 
   public void put() {
     if (register.isEmpty()) {
       return;
     }
 
     startPatch();
     if (isInVisual()) {
       file.removeLine(cursor.getY());
       register.getContents().put(cursor.getY() - 1, cursor.getX(), file);
       cursor.moveTo(cursor.getY(), 0);
     } else {
       register.getContents().put(cursor.getY(), cursor.getX(), file);
     }
 
     if (this.getLineCount() > 0 && !isInVisual() && register.getMode() == Mode.LINE) {
       cursor.moveTo(cursor.getY() + 1, 0);
     }
 
     file.breakPatch();
     toggleVisual(Visual.Mode.NONE);
   }
 
   public void putBefore() {
     if (register.isEmpty()) {
       return;
     }
     startPatch();
     register.getContents().putBefore(cursor.getY(), cursor.getX(), file);
     file.breakPatch();
   }
 
   public void yank() {
     if (!isInVisual()) {
       throw new IllegalStateException();
     }
     register.setContents(visual.getRegister(file));
     toggleVisual(Visual.Mode.NONE);
   }
 
   public void tab() {
     int spacesToAdd = TAB_SIZE - (cursor.getX() % TAB_SIZE);
     for (int i = 0; i < spacesToAdd; i++) {
       onLetterTyped(' ');
     }
   }
 
   @Override
   public boolean handleKeyStroke(KeyStroke keyStroke) {
     return keyHandler.handleKeyPress(keyStroke, this);
   }
 
   public void setDiffMarkers(FileDelta delta) {
     file.setDiffMarkers(delta);
   }
 
   public void save(FileSystem fileSystem) {
     file.save(fileSystem);
   }
 
   public boolean isModified() {
     return file.isModified();
   }
 
   public boolean isDogEared() {
     return file.isDogEared();
   }
 
   public boolean isMarkersClear() {
     return file.isMarkersClear();
   }
 
   public void enterFindMode(FindMode findMode) {
     this.findMode = findMode;
   }
 
   public boolean isInFindMode() {
     return findMode != FindMode.NONE;
   }
 
   public void exitFindMode() {
     this.findMode = FindMode.NONE;
   }
 
   public void onFindLetter(char letter) {
     findLetter(letter, this.findMode, true);
   }
 
   private void findLetter(char letter, FindMode findModeToFind, boolean remember) {
     this.findMode = FindMode.NONE;
     int x = findModeToFind.findIn(cursor, file, letter);
     if (remember) {
       this.lastFindMode = findModeToFind;
       this.lastFindLetter = letter;
     }
     if (x == -1) {
       return;
     }
     cursor.moveTo(cursor.getY(), x);
   }
 
   public void repeatLastFindForwards() {
     if (lastFindLetter == 0) {
       return;
     }
     findLetter(this.lastFindLetter, this.lastFindMode, false);
   }
 
   public void repeatLastFindBackwards() {
     if (lastFindLetter == 0) {
       return;
     }
     findLetter(this.lastFindLetter, this.lastFindMode.opposite(), false);
   }
 
   public boolean isSearchHighlight(int y, int x) {
     return currentSearch != null && currentSearch.isHighlight(y, x);
   }
 
   public void enterSearch() {
     this.currentSearch = new Search(new Minibuffer(), file.getFile(), cursor.getPoint(), new Search.Listener() {
       @Override
       public void onSearchCompleted() {
         setHighlightPattern(Patterns.partWord(currentSearch.getQuery()));
         exitSearch();
       }
 
       @Override
       public void onMoveTo(int y, int x) {
         cursor.jumpTo(y, x);
       }
 
       @Override
       public void onSearchCancelled() {
         exitSearch();
       }
     });
   }
 
   public void exitSearch() {
     this.currentSearch = null;
   }
 
   public boolean isInSearchMode() {
     return this.currentSearch != null;
   }
 
   public boolean handleSearchKeyStroke(KeyStroke keyStroke) {
     if (!isInSearchMode()) {
       throw new IllegalStateException();
     }
     return this.currentSearch.handleKeyStroke(keyStroke);
   }
 
   public int getHighlightMatchCount() {
     return highlight.getMatchCount();
   }
 
   public void undoLine() {
     startPatch();
     if (isInVisual()) {
       int startY = visual.getStartPoint().getY();
       int endY = visual.getEndPoint().getY();
       visual.toggleMode(Visual.Mode.NONE);
       file.undoLineRange(startY, endY);
     } else {
       file.undoLine(cursor.getY());
     }
     applyCursorConstraints();
     file.breakPatch();
   }
 
   public void wipe() {
     if (isInVisual()) {
       int startY = visual.getStartPoint().getY();
       int endY = visual.getEndPoint().getY();
       visual.toggleMode(Visual.Mode.NONE);
       file.wipeRange(startY, endY);
     } else {
       file.wipeRange(0, getLineCount() - 1);
     }
   }
 
   public void changeWord() {
     int x = file.findNextWordBreak(cursor.getY(), cursor.getX());
     if (x == -1) {
       changeToEndOfLine();
       return;
     }
     startPatch();
     file.removeText(cursor.getY(), cursor.getX(), x - cursor.getX());
     insert();
   }
 
   public void changeWordUnderCursor() {
     int endX = file.findNextWordBreak(cursor.getY(), cursor.getX());
     int startX = file.findPreviousWordBreak(cursor.getY(), cursor.getX());
     if (endX == -1) {
       endX = getCurrentLine().length();
     }
     if (startX == -1) {
       startX = 0;
     }
     startPatch();
     file.removeText(cursor.getY(), startX, endX - startX);
     cursor.moveTo(cursor.getY(), startX);
     insert();
   }
 
   public void deleteWord() {
     int x = file.findNextWordBreak(cursor.getY(), cursor.getX());
     if (x == -1) {
       deleteToEndOfLine();
       return;
     }
     if (getCurrentLineLength() > x && Character.isWhitespace(getCurrentLine().charAt(x))) {
       x++;
     }
     startPatch();
     file.removeText(cursor.getY(), cursor.getX(), x - cursor.getX());
     file.breakPatch();
   }
 
   public void findMatchingLetter() {
     Point point = file.findMatchingLetter(cursor.getY(), cursor.getX());
     if (point == null) {
       return;
     }
     cursor.moveTo(point);
   }
 
   public void openFileUnderCursor() {
     environment.openFile(file.getFilenameUnder(cursor.getY(), cursor.getX()));
   }
 
   public void indent() {
     startPatch();
     if (isInVisual()) {
       int startY = visual.getStartPoint().getY();
       int endY = visual.getEndPoint().getY();
       visual.toggleMode(Visual.Mode.NONE);
       for (int i = startY; i <= endY; i++) {
         indentLine(i);
       }
     } else {
       indentLine(cursor.getY());
     }
     file.breakPatch();
   }
 
   private void indentLine(int y) {
     String indent = getIndentForLine(y);
     int remainder = indent.length() % TAB_SIZE;
     int indentAmount = TAB_SIZE - remainder;
     file.changeLine(y, repeatChar(' ', indentAmount) + getLine(y));
   }
 
   private String repeatChar(char c, int n) {
     StringBuffer buffer = new StringBuffer();
     for (int i = 0; i < n; i++) {
       buffer.append(c);
     }
     return buffer.toString();
   }
 
   public void outdent() {
     startPatch();
     if (isInVisual()) {
       int startY = visual.getStartPoint().getY();
       int endY = visual.getEndPoint().getY();
       visual.toggleMode(Visual.Mode.NONE);
       for (int i = startY; i <= endY; i++) {
         outdentLine(i);
       }
     } else {
       outdentLine(cursor.getY());
     }
     file.breakPatch();
   }
 
   private void outdentLine(int y) {
     String indent = getIndentForLine(y);
     if (indent.isEmpty()) {
       return;
     }
     int remainder = indent.length() % TAB_SIZE;
     int outdentAmount = remainder == 0 ? TAB_SIZE : remainder;
     file.removeText(y, 0, outdentAmount);
   }
 
   public void makeSnippetFromVisual() {
     if (!isInVisual()) {
       throw new IllegalStateException();
     }
     environment.addSnippet(file.makeView(visual.getStartY(), visual.getEndY()));
     visual.toggleMode(Visual.Mode.NONE);
   }
 
   public void makeSnippetFromRange(Range range) {
     environment.addSnippet(file.makeView(range.getStart(), range.getEnd()));
   }
 
   @Override
   public void onLineInserted(int y, String line) {
     // If the cursor is at the end of the file already, we probably just
     // inserted the first line and so the cursor is already at the correct
     // position.
     if (cursor.getY() == file.getLineCount() - 1) {
       if (file.getLineCount() > 1) {
         throw new IllegalStateException();
       }
       return;
     }
     if (y <= cursor.getY()) {
       cursor.moveBy(1, 0);
     }
   }
 
   @Override
   public void onLineRemoved(int y, String line) {
     if (y <= cursor.getY()) {
       cursor.moveBy(-1, 0);
     }
   }
 
   @Override
   public void onLineChanged(int y, String oldLine, String newLine) {
     // Do nothing.
   }
 
   public Data.Editor getSerialized() {
     Data.Editor result = file.getSerialized();
     result = Data.Editor.newBuilder(result)
         .setCursorY(cursor.getY())
         .setCursorX(cursor.getX())
         .setTop(view.getTopLineVisible())
         .build();
     return result;
   }
 
   public List<Range> getDeltas() {
     return file.getDeltas();
   }
 
   public Range getRange() {
     return file.getRange();
   }
 
   public void growToCover(Range range) {
     file.growToCover(range);
   }
 
   public void setTopLineVisible(int top) {
     view.setTopLineVisible(top);
   }
 }
