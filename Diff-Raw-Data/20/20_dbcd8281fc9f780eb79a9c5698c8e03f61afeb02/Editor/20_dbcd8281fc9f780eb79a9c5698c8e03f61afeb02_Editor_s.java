 package com.id.editor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.id.editor.Visual.Mode;
 import com.id.file.File;
 import com.id.file.File.Listener;
 import com.id.file.FileView;
 import com.id.file.Grave;
 import com.id.file.Tombstone.Status;
 
 public class Editor {
   public interface Context {
     void moveViewportToIncludePoint(Point point);
     void recenterScreenOnPoint(Point point);
     int getViewportHeight();
   }
 
   class EmptyContext implements Context {
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
   }
 
   private final FileView file;
   private final Cursor cursor;
   private final Visual visual;
   private boolean inInsertMode = false;
   private final List<File.Listener> fileListeners = new ArrayList<File.Listener>();
   private Context context = new EmptyContext();
 
   public Editor(FileView fileView) {
     this.file = fileView;
     this.cursor = new Cursor();
     this.visual = new Visual(this.cursor);
     cursor.addListner(new Cursor.Listener() {
       @Override
       public void onMoved(int y, int x) {
         context.moveViewportToIncludePoint(new Point(y, x));
       }
 
       @Override
       public void onJumped(int y, int x) {
         // TODO Recenter the screen on (y, x).
       }
     });
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
     int isInNormalMode = !isInInsertMode() ? 1 : 0;
     cursor.constrainY(0, file.getLineCount() - 1);
     cursor.constrainX(0, getCurrentLineLength() - isInNormalMode);
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
     startPatchIfNecessary();
     if (file.isEmpty()) {
       file.insertLine(0, "");
     }
     file.insertLine(Math.min(file.getLineCount(), cursor.getY() + 1), "");
     cursor.moveBy(1, 0);
     applyCursorConstraints();
     insert();
   }
 
   public void addEmptyLinePrevious() {
     startPatch();
     if (file.isEmpty()) {
       file.insertLine(0, "");
     }
     file.insertLine(cursor.getY(), "");
     applyCursorConstraints();
     insert();
   }
 
   public void backspace() {
     startPatchIfNecessary();
     if (cursor.getX() == 0) {
       if (cursor.getY() == 0) {
         return;
       }
       String line = getCurrentLine();
       file.removeLine(cursor.getY());
       cursor.moveBy(-1, 0);
       int targetX = getCurrentLine().length();
       file.insertText(cursor.getY(), targetX, line);
       cursor.moveTo(cursor.getY(), targetX);
     } else {
       file.removeText(cursor.getY(), cursor.getX() - 1, 1);
       cursor.moveBy(0, -1);
     }
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
     cursor.moveTo(cursor.getY(), 0);
     insert();
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
     file.splitLine(cursor.getY(), cursor.getX());
     cursor.moveBy(1, 0);
     cursor.moveTo(cursor.getY(), 0);
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
 
   public void changeLine() {
     startPatch();
     file.removeText(cursor.getY(), cursor.getX());
     append();
   }
 
   public void deleteLine() {
     startPatch();
     file.removeText(cursor.getY(), cursor.getX());
     file.breakPatch();
     applyCursorConstraints();
   }
 
   public void moveCursorToEndOfLine() {
     if (isInInsertMode()) {
       throw new IllegalStateException();
     }
     cursor.moveTo(cursor.getY(), getCurrentLineLength() - 1);
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
       visual.removeFrom(file);
       cursor.moveTo(visual.getStartPoint().getY(), visual.getStartPoint().getX());
       visual.toggleMode(Visual.Mode.NONE);
     } else {
       file.removeText(cursor.getY(), cursor.getX(), 1);
     }
     if (breakPatch) {
       file.breakPatch();
     }
     applyCursorConstraints();
   }
 
   public boolean isInVisual() {
     return visual.isOn();
   }
 
   public void substitute() {
     delete(false);
     insert();
   }
 
   public boolean hasUndo() {
     return file.hasUndo();
   }
 
   public void substituteLine() {
     if (isInVisual()) {
       substitute();
       return;
     }
     moveCursorToStartOfLine();
     startPatch();
     file.removeText(cursor.getY(), 0);
     insert();
   }
 
   public void addFileListener(Listener listener) {
     fileListeners.add(listener);
     file.addListener(listener);
   }
 
   public void setContext(Context context) {
     this.context = context;
   }
 
   public void downPage() {
     cursor.moveBy(context.getViewportHeight() - 1, 0);
     applyCursorConstraints();
   }
 
   public void upPage() {
     cursor.moveBy(-(context.getViewportHeight() - 1), 0);
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
   }
 
   public void setHighlight(String word) {
     file.setHighlight(word);
   }
 
   public boolean isHighlight(int y, int x) {
     return file.isHighlighted(y, x);
   }
 
   public void highlightWordUnderCursor() {
     file.setHighlight(file.getWordUnder(cursor.getY(), cursor.getX()));
   }
 
   public void clearHighlight() {
     file.clearHighlight();
   }
 
   public void recenter() {
     context.recenterScreenOnPoint(cursor.getPoint());
   }
 
   public void next() {
     Point point = file.getNextHighlightPoint(cursor.getY(), cursor.getX());
     if (point != null) {
       cursor.moveTo(point);
     }
   }
 
   public void previous() {
     Point point = file.getPreviousHighlightPoint(cursor.getY(), cursor.getX());
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
 }
