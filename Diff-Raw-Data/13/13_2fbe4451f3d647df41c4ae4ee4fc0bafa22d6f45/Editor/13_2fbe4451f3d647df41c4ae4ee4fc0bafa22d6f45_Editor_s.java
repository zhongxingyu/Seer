 package com.id.editor;
 
 import com.id.editor.Visual.Mode;
 import com.id.file.FileView;
 
 public class Editor {
   public interface Context {
     void moveScreenToIncludePoint(Point point);
     void recenterScreenOnPoint(Point point);
   }
 
   private final FileView file;
   private final Cursor cursor;
   private final Visual visual;
   private boolean inInsertMode = false;
 
   public Editor(FileView fileView) {
     this.file = fileView;
     this.cursor = new Cursor();
     this.visual = new Visual(this.cursor);
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
     file.startPatchAt(cursor.getY(), cursor.getX());
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
   }
 
   public void endOfLine() {
     if (isInInsertMode()) {
       throw new IllegalStateException();
     }
     cursor.moveTo(cursor.getY(), getCurrentLineLength() - 1);
   }
 }
