 package com.id.editor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.id.app.HighlightPattern;
 import com.id.app.HighlightState;
 import com.id.app.ViewportTracker;
 import com.id.editor.Visual.Mode;
 import com.id.events.EditorKeyHandler;
 import com.id.events.KeyStroke;
 import com.id.events.KeyStrokeHandler;
 import com.id.file.File;
 import com.id.file.FileView;
 import com.id.file.Grave;
 import com.id.file.ModifiedListener;
 import com.id.file.Range;
 import com.id.file.Tombstone.Status;
 import com.id.git.FileDelta;
 import com.id.platform.FileSystem;
 
 public class Editor implements KeyStrokeHandler, HighlightState.Listener,
     File.Listener, Focusable, SharedEditorSettings.Listener {
   private static final int TAB_SIZE = 2;
   private static final int DELTA_PADDING = 2;
 
   public interface Listener {
     void onSizeChanged();
   }
 
   public interface Iterator {
     int getY();
     boolean isInGrave();
     String getLine();
     String getOriginal();
     boolean next();
     boolean done();
     int getLineCount();
     Point getCursorPosition();
   }
 
   public class LogicalLineIterator implements Iterator {
     private int y;
 
     public LogicalLineIterator() {
       this(0);
     }
 
     public LogicalLineIterator(int y) {
       this.y = y;
     }
 
     @Override
     public int getY() {
       return y;
     }
 
     @Override
     public boolean isInGrave() {
       return false;
     }
 
     @Override
     public String getLine() {
       return Editor.this.getLine(y);
     }
 
     @Override
     public String getOriginal() {
       return getLine();
     }
 
     @Override
     public boolean next() {
       y++;
       return !done();
     }
 
     @Override
     public boolean done() {
       return y >= getLineCount();
     }
 
     @Override
     public int getLineCount() {
       return Editor.this.getLineCount();
     }
 
     @Override
     public Point getCursorPosition() {
       return Editor.this.getCursorPosition();
     }
 
     @Override
     public String toString() {
       return "[" + y + "]";
     }
   }
 
   /**
    * The "physical" refers to things that appear on the screen, even though they
    * are not logically there. Eg, the cursor will skip over them. Physical lines
    * include lines in graves.
    */
   public class PhysicalLineIterator implements Iterator {
     private int y = 0;
     private int graveY = -1;
 
     public PhysicalLineIterator() {
     }
 
     @Override
     public int getLineCount() {
       return getPhysicalLineCount();
     }
 
     @Override
     public int getY() {
       return y;
     }
 
     @Override
     public boolean isInGrave() {
       return graveY != -1;
     }
 
     @Override
     public String getLine() {
       if (!isInGrave()) {
         return Editor.this.getLine(y);
       } else {
         return getGrave(y).get(graveY).getCurrent();
       }
     }
 
     @Override
     public String getOriginal() {
       if (!isInGrave()) {
         return Editor.this.getLine(y);
       } else {
         return getGrave(y).get(graveY).getOriginal();
       }
     }
 
     @Override
     public boolean next() {
       if (done()) {
         throw new IllegalStateException();
       }
       if (!isInGrave()) {
         if (getGrave(y).isEmpty()) {
           y++;
         } else {
           graveY = 0;
         }
       } else {
         graveY++;
         if (graveY >= getGrave(y).size()) {
           graveY = -1;
           y++;
         }
       }
       return !done();
     }
 
     @Override
     public boolean done() {
       return y >= Editor.this.getLineCount();
     }
 
     @Override
     public String toString() {
       return getClass().getName() + "[" + y + ", " + graveY + "]";
     }
 
     @Override
     public Point getCursorPosition() {
       return Editor.this.getPhysicalCursorPosition();
     }
   }
 
   public interface EditorView {
     void moveViewportToIncludePoint(Point point);
     void recenterScreenOnPoint(Point point);
     int getViewportHeight();
     boolean isVisible(Point point);
     int getTopLineVisible();
     void setTopLineVisible(int topLine);
   }
 
   public interface EditorEnvironment {
     Editor openFile(String filename);
     void openFileMatchingPattern(String pattern);
     void addSnippet(FileView fileView);
   }
 
   public enum FindMode {
     NONE,
     FIND_FORWARDS,
     FIND_BACKWARDS,
     TILL_FORWARDS,
     TILL_BACKWARDS,;
 
     public FindMode opposite() {
       switch (this) {
       case FIND_BACKWARDS: return FIND_FORWARDS;
       case FIND_FORWARDS: return FIND_BACKWARDS;
       case TILL_FORWARDS: return TILL_BACKWARDS;
       case TILL_BACKWARDS: return TILL_FORWARDS;
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
     public Editor openFile(String filename) {
       return null;
     }
 
     @Override
     public void addSnippet(FileView fileView) {
       // Do nothing.
     }
 
     @Override
     public void openFileMatchingPattern(String pattern) {
       // Do nothing.
     }
   }
 
   private final FileView file;
   private final Cursor cursor;
   private final Visual visual;
   private boolean inInsertMode = false;
   private EditorView view = new EmptyView();
   private final EditorEnvironment environment;
 
   private final EditorKeyHandler keyHandler;
   private final List<Listener> listeners = new ArrayList<Listener>();
   private final List<File.Listener> fileListeners = new ArrayList<File.Listener>();
   private final List<ModifiedListener> fileModifiedListeners = new ArrayList<ModifiedListener>();
   private final HighlightState highlightState;
   private final Register register;
   private final SharedEditorSettings settings;
 
   private FindMode findMode = FindMode.NONE;
   private char lastFindLetter = 0;
   private FindMode lastFindMode = FindMode.NONE;
   private Search currentSearch = null;
   private Highlight highlight = new EmptyHighlight();
   private boolean justInsertedAutoIndent = false;
   private Point lastInsertPoint = null;
   private Point autocompleteStart = null;
   private boolean focused = false;
   private boolean expandoDiff = false;
   private ViewportTracker viewportTracker = null;
 
   public Editor(FileView fileView, HighlightState highlightState,
       Register register, EditorEnvironment editorEnvironment,
       SharedEditorSettings settings) {
     this.file = fileView;
     this.highlightState = highlightState;
     this.register = register;
     this.environment = editorEnvironment;
     this.settings = settings;
     this.cursor = new Cursor();
     cursor.setEditor(this);
     this.visual = new Visual(this.cursor);
     this.highlightState.addListener(this);
     addFileListener(highlight);
     addFileListener(this);
     onHighlightStateChanged();
     cursor.addListner(new Cursor.Listener() {
       @Override
       public void onMoved(int y, int x) {
         view.moveViewportToIncludePoint(new Point(getPhysicalLine(y), x));
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
     settings.addListener(this);
 
     // Conform to the editor settings.
     onSettingsChanged();
   }
 
   @Override
   public void onSettingsChanged() {
     setExpandoDiff(settings.isInExpandoDiffMode());
   }
 
   public String getLine(int y) {
     return file.getLine(y);
   }
 
   public int getLineCount() {
     return file.getLineCount();
   }
 
   private Iterator getPhysicalLineIterator(int physicalY) {
     PhysicalLineIterator it = new PhysicalLineIterator();
     for (int i = 0; i < physicalY; i++) {
       it.next();
     }
     return it;
   }
 
   private Iterator getLogicalLineIterator(int y) {
     return new LogicalLineIterator(y);
   }
 
   private int getPhysicalLine(int logicalY) {
     int result = 0;
     for (int y = 0; y < logicalY; y++) {
       result += getGrave(y).size();
       result++;
     }
     return result;
   }
 
   private int getPhysicalLineCount() {
     int result = 0;
     for (int y = 0; y < getLineCount(); y++) {
       result += getGrave(y).size();
       result++;
     }
     return result;
   }
 
   public int getLineCountForMode() {
     if (isInExpandoDiffMode()) {
       return getPhysicalLineCount();
     }
     return getLineCount();
   }
 
   public Point getCursorPosition() {
     return cursor.getPoint();
   }
 
   public Point getPhysicalCursorPosition() {
     return new Point(getPhysicalLine(cursor.getY()), cursor.getX());
   }
 
   public Iterator getIterator(int physicalY) {
     return expandoDiff ? getPhysicalLineIterator(physicalY) :
                          getLogicalLineIterator(physicalY);
   }
 
   public void setExpandoDiff(boolean enabled) {
     if (expandoDiff == enabled) {
       return;
     }
     expandoDiff = enabled;
     fireSizeChanged();
   }
 
   public boolean isInExpandoDiffMode() {
     return expandoDiff;
   }
 
   public void toggleExpandoDiffMode() {
     setExpandoDiff(!expandoDiff);
   }
 
   private void fireSizeChanged() {
     for (Listener listener : listeners) {
       listener.onSizeChanged();
     }
   }
 
   public int getRealCursorY() {
     return getCursorPosition().getY() + file.getStart();
   }
 
   // Keyboard commands.
   public void down() {
     cursor.moveBy(1, 0);
   }
 
   public void up() {
     cursor.moveBy(-1, 0);
   }
 
   public void left() {
     cursor.moveBy(0, -1);
   }
 
   public void right() {
     cursor.moveBy(0, 1);
   }
 
   public void insert() {
     inInsertMode = true;
     cursor.setDefaultX(cursor.getX());
   }
 
   public boolean isCursorInBounds() {
     if (file.getLineCount() == 0) {
       return true;
     }
     if (getCurrentLineLength() == 0) {
       return cursor.getX() == 0;
     }
     return cursor.isInRangeY(0, file.getLineCount() - 1) &&
         cursor.isInRangeX(0, getEffectiveLineLength());
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
     } else if (isInInsertMode()) {
       inInsertMode = false;
       lastInsertPoint = cursor.getPoint();
       if (justInsertedAutoIndent && isAllWhitespace(getCurrentLine())) {
         file.changeLine(cursor.getY(), "");
       }
       justInsertedAutoIndent = false;
       file.breakPatch();
       cursor.moveBy(0, -1);
     }
     cursor.applyCursorConstraints();
   }
 
   public void insertAtLastInsert() {
     insert();
     if (lastInsertPoint != null) {
       cursor.moveTo(lastInsertPoint.getY(), lastInsertPoint.getX());
     }
   }
 
   public void onLetterTyped(char keyChar) {
     if (!isInInsertMode()) {
       throw new IllegalStateException("Can't type letters unless in insert mode.");
     }
     startPatchIfNecessary();
 
     String text = "" + keyChar;
     file.insertText(cursor.getY(), cursor.getX(), Arrays.asList(text));
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
   }
 
   public void redo() {
     Point position = file.redo();
     if (position == null) {
       return;
     }
     cursor.moveTo(position.getY(), position.getX());
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
 
   private static boolean isAllWhitespace(String str) {
     for (int i = 0; i < str.length(); i++) {
       if (!isWhitespace(str.charAt(i))) {
         return false;
       }
     }
     return true;
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
       file.insertText(cursor.getY(), targetX, Arrays.asList(line));
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
   }
 
   public void appendEnd() {
     insert();
     cursor.moveTo(cursor.getY(), getCurrentLineLength());
   }
 
   public void insertStart() {
     insert();
     cursor.moveTo(cursor.getY(), getFirstNonWhitespace(cursor.getY()));
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
 
   public String getBaseFilename() {
     return file.getBaseFilename();
   }
 
   public void toggleVisual(Mode mode) {
     visual.toggleMode(mode);
   }
 
   public boolean isInVisual(int y, int x) {
     return visual.contains(new Point(y, x));
   }
 
   public void changeToEndOfLine() {
     startPatch();
     String removedText = file.removeText(cursor.getY(), cursor.getX());
     register.setContents(new TextFragment(Visual.Mode.CHAR, false, removedText));
     append();
   }
 
   private int getEffectiveLineLength() {
     return getEffectiveLineLength(cursor.getY());
   }
 
   public int getEffectiveLineLength(int y) {
     int isInNormalMode = isInInsertMode() || isInVisual() ? 0 : 1;
     return getLine(y).length() - isInNormalMode;
   }
 
   public void deleteToEndOfLine() {
     changeToEndOfLine();
     escape();
   }
 
   public void deleteLine() {
     deleteAdjacentLines(0);
   }
 
   public void deleteDown() {
     deleteAdjacentLines(1);
   }
 
   public void deleteUp() {
     deleteAdjacentLines(-1);
   }
 
   /**
    * Deletes a range of lines from the current line to X lines from the current
    * line, inclusive.
    *
    * @param delta can be positive or negative.
    */
   private void deleteAdjacentLines(int delta) {
     int y = cursor.getY();
     int upToLine = clampY(y + delta);
     startPatch();
     List<String> removedLines = file.removeLineRange(y, upToLine);
     register.setContents(new TextFragment(Visual.Mode.LINE, false, removedLines));
     file.breakPatch();
     // The cursor is placed on the line following the deleted lines, having
     // the effect that deleting up moves the cursor up.
     int moveBackToLine = clampY(Math.min(upToLine, y));
     cursor.moveTo(moveBackToLine, 0);
   }
 
   private int clampY(int line) {
     return Math.max(0, Math.min(file.getLineCount() - 1, line));
   }
 
   public void moveCursorToEndOfLine() {
     int x = Math.max(0, getEffectiveLineLength());
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
     if (file.isEmpty()) {
       return;
     }
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
       cursor.applyCursorConstraints();
     }
     if (breakPatch) {
       file.breakPatch();
     }
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
     if (file.isEmpty()) {
       insert();
       return;
     }
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
 
   public void addFileListener(File.Listener listener) {
     fileListeners.add(listener);
     file.addListener(listener);
   }
 
   private void removeFileListener(File.Listener listener) {
     fileListeners.remove(listener);
     file.removeListener(listener);
   }
 
   public void addListener(Listener listener) {
     listeners.add(listener);
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
   }
 
   public void upPage() {
     cursor.moveBy(-(view.getViewportHeight() - 1), 0);
   }
 
   public void join() {
     if (!isInVisual()) {
       throw new IllegalStateException();
     }
     startPatch();
     file.joinRange(visual.getStartPoint().getY(), visual.getEndPoint().getY());
     visual.toggleMode(Visual.Mode.NONE);
     file.breakPatch();
     cursor.applyCursorConstraints();
   }
 
   public void setHighlightPattern(HighlightPattern pattern) {
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
     setHighlightPattern(Patterns.wholeWord(file.getWordAt(cursor.getY(), cursor.getX())));
   }
 
   public void highlightWordBeforeCursor() {
     if (cursor.getX() == 0) {
       return;
     }
     setHighlightPattern(Patterns.wholeWord(file.getWordAt(cursor.getY(), cursor.getX() - 1)));
   }
 
   public void insertLastHighlightedWord() {
     if (highlightState.isEmpty()) {
       return;
     }
     file.insertText(cursor.getY(), cursor.getX(), Arrays.asList(highlightState.getHighlightText()));
     cursor.moveBy(0, highlightState.getHighlightText().length());
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
     if (register.isEmpty() || isInVisual()) {
       return;
     }
 
     startPatch();
    register.getContents().put(cursor.getY(), cursor.getX(), file);
 
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
 
       @Override
       public void onSearchSoftExited() {
         exitSearch();
       }
 
       @Override
       public void onRecenter() {
         recenter();
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
     cursor.applyCursorConstraints();
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
     insert();
     cursor.moveTo(cursor.getY(), startX);
   }
 
   /**
    * Removes the content between matching left and right bounding characters,
    * exclusive of the characters, and inserts the cursor at the deleted content.
    * Works also if the cursor is on one of the bounding characters.
    *
    * NOTE: Different behaviour to vim when the left or right bounding characters
    * are the last or first character of the line, respectively. In vim, the
    * lines won't be joined, whereas this method will join the lines.
    *
    * Example 1: <code>
    * ab(c
    * def)g
    * </code>
    *
    * ci( performed on the c will join the lines in vim and also this editor.
    *
    * Example 2: <code>
    * ab(
    * def)g
    * </code>
    *
    * ci( performed on the d won't join the lines in vim but will in this editor.
    *
    */
   public void changeContentBetween(char left, char right) {
     String line = getLine(cursor.getY());
     char c = line.charAt(cursor.getX());
     // If we're already on a bounding character then don't go past it.
     int findPreviousInitialDepth = c == right ? 0 : 1;
     int findNextInitialDepth = c == left ? 0 : 1;
 
     Point leftBound = file.findPrevious(cursor.getY(), cursor.getX(), right, left,
         findPreviousInitialDepth);
     Point rightBound = file.findNext(cursor.getY(), cursor.getX(), left, right,
         findNextInitialDepth);
     if (leftBound == null || rightBound == null) {
       return;
     }
     startPatch();
     file.removeTextExclusive(leftBound, rightBound);
     cursor.moveTo(leftBound);
     append();
   }
 
   /**
    * Same as changeContentBetween(char, char) except for the same left and right
    * bounding character. Try to find the matching character forward first, then
    * try backwards.
    *
    * NOTE: Different behaviour to vim (tested with quotes), which searches
    * backwards first then forwards, and if there are an odd number of bounding
    * characters on the line, then the last one won't match anything. Also, vim
    * does not search across lines.
    */
   public void changeContentBetween(char bounding) {
     String line = getLine(cursor.getY());
     char c = line.charAt(cursor.getX());
     Point leftBound;
     Point rightBound;
     if (c != bounding) {
       // If we're not on a bounding character then simply search around.
       leftBound = file.findPrevious(cursor.getY(), cursor.getX(), bounding, bounding, 1);
       rightBound = file.findNext(cursor.getY(), cursor.getX(), bounding, bounding, 1);
     } else {
       // Otherwise try to find the matching character forward and if it fails,
       // then try to find the matching character backwards.
       leftBound = file.findPrevious(cursor.getY(), cursor.getX(), bounding, bounding, 1);
       rightBound = file.findNext(cursor.getY(), cursor.getX(), bounding, bounding, 2);
       if (rightBound == null) {
         leftBound = file.findPrevious(cursor.getY(), cursor.getX(), bounding, bounding, 2);
         rightBound = file.findNext(cursor.getY(), cursor.getX(), bounding, bounding, 1);
       }
     }
 
     if (leftBound == null || rightBound == null) {
       return;
     }
     startPatch();
     file.removeTextExclusive(leftBound, rightBound);
     cursor.moveTo(leftBound);
     append();
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
     int x = cursor.getX();
     // In visual mode it's possible to be past the end of the line, so adjust.
     if (isInVisual()) {
       x = Math.min(x, getCurrentLineLength() - 1);
     }
     Point point = file.findMatchingLetter(cursor.getY(), x);
     if (point == null) {
       return;
     }
     cursor.moveTo(point);
   }
 
   public void openFileUnderCursor() {
     if (file.isEmpty()) {
       return;
     }
     String filename = file.getFilenameAt(cursor.getY(), cursor.getX());
     if (filename != null) {
       environment.openFile(filename);
     }
   }
 
   public void openFileMatchingWordUnderCursor() {
     String pattern = file.getWordAt(cursor.getY(), cursor.getX());
     environment.openFileMatchingPattern(pattern);
   }
 
   public void indent() {
     if (file.isEmpty()) {
       return;
     }
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
     if (getLine(y).isEmpty()) {
       return;
     }
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
     startPatchIfNecessary();
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
     cursor.applyCursorConstraints();
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
     fireSizeChanged();
   }
 
   @Override
   public void onLineRemoved(int y, String line) {
     if (y <= cursor.getY()) {
       cursor.moveBy(-1, 0);
     }
     fireSizeChanged();
   }
 
   @Override
   public void onLineChanged(int y, String oldLine, String newLine) {
     // Do nothing.
   }
 
   public List<Range> getDeltas() {
     return file.getDeltas(DELTA_PADDING);
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
 
   public void jumpToLine(int lineNumber) {
     cursor.jumpTo(lineNumber, 0);
   }
 
   public void autocompleteStart() {
     if (!isInInsertMode()) {
       throw new IllegalStateException();
     }
     int x = file.findWordStart(cursor.getY(), cursor.getX());
     autocompleteStart = new Point(cursor.getY(), x);
 //    environment.autocompleteStart(file.getWordAt(cursor.getY(), cursor.getX() - 1), this);
   }
 
   public void autocompleteFinish(String item) {
     if (!isInInsertMode()) {
       throw new IllegalStateException();
     }
     int length = cursor.getX() - autocompleteStart.getX();
     file.removeText(autocompleteStart.getY(), autocompleteStart.getX(), length);
     file.insertText(cursor.getY(), autocompleteStart.getX(), item);
     cursor.moveTo(cursor.getY(), autocompleteStart.getX() + item.length());
     autocompleteStart = null;
   }
 
   @Override
   public void setFocused(boolean selected) {
     this.focused = selected;
     view.moveViewportToIncludePoint(getPhysicalCursorPosition());
   }
 
   @Override
   public boolean isFocused() {
     return focused;
   }
 
   public void reselectVisual() {
     visual.reselectLast();
   }
 
   public void saveViewport() {
     if (viewportTracker == null) {
       return;
     }
     if (viewportTracker.isTracking()) {
       viewportTracker.saveViewport(getFilename(), view.getTopLineVisible(), cursor.getY(), cursor.getX());
     } else {
       viewportTracker.restoreViewport();
     }
   }
 
   public void setViewportTracker(ViewportTracker viewportTracker) {
     this.viewportTracker = viewportTracker;
   }
 }
