 package com.id.file;
 
 import com.id.editor.Point;
 import com.id.file.File.Listener;
 import com.id.file.Tombstone.Status;
 
 public class FileView implements File.Listener, ModifiedListener {
   private final File file;
   private int start;
   private int end;
 
   public FileView(File file) {
     this(file, 0, file.getLineCount() - 1);
   }
   public FileView(File file, int start, int end) {
     this.file = file;
     this.start = start;
     this.end = end;
     // TODO Make this not leak.
     file.addListener(this);
   }
 
   public String getLine(int y) {
     return file.getLine(start + y);
   }
 
   public int getLineCount() {
     return end - start + 1;
   }
 
   public void insertLine(int y, String line) {
     file.insertLine(start + y, line);
   }
 
   public void changeLine(int y, String line) {
     file.changeLine(start + y, line);
   }
 
   @Override
   public void onLineInserted(int y, String line) {
     if (y < start) {
       start++;
       end++;
     } else if (start <= y && y <= end + 1) {
       end++;
     }
   }
 
   @Override
   public void onLineRemoved(int y, String line) {
     if (y < start) {
       start--;
       end--;
     } else if (start <= y && y <= end) {
       end--;
     }
   }
 
   @Override
   public void onLineChanged(int y, String oldLine, String newLine) {
     // Do nothing.
   }
 
   @Override
   public void onModifiedStateChanged() {
     // Do nothing.
   }
 
   public boolean isInPatch() {
     return file.isInPatch();
   }
 
   public void startPatchAt(int y, int x) {
     file.startPatchAt(start + y, x);
   }
 
   public void breakPatch() {
     file.breakPatch();
   }
 
   public Point undo() {
     return file.undo();
   }
 
   public Point redo() {
     return file.redo();
   }
 
   public String removeText(int y, int x) {
     return removeText(y, x, getLine(y).length());
   }
 
   public String removeText(int y, int x, int length) {
     String line = getLine(y);
     if (x >= line.length()) {
       return "";
     }
     int substringMax = Math.min(line.length(), x + length);
     String newLine = line.substring(0, x) + line.substring(substringMax);
     changeLine(y, newLine);
     return line.substring(x, substringMax);
   }
 
   public void insertText(int y, int x, String text) {
     file.insertText(start + y, x, text);
   }
 
   public String removeLine(int y) {
     return file.removeLine(start + y);
   }
 
   public void splitLine(int y, int x) {
     String removedText = removeText(y, x);
     insertLine(y + 1, removedText);
   }
 
   public boolean isEmpty() {
     return getLineCount() == 0;
   }
 
   public String getFilename() {
     return file.getFilename();
   }
 
   public Status getStatus(int y) {
     return file.getStatus(start + y);
   }
 
   public Grave getGrave(int y) {
     return file.getGrave(start + y);
   }
 
   public void removeLineRange(int from, int to) {
     for (int i = 0; i < to - from + 1; i++) {
       removeLine(from);
     }
   }
 
   public void appendToLine(int y, String tail) {
     changeLine(y, getLine(y) + tail);
   }
 
   public boolean hasUndo() {
     return file.hasUndo();
   }
 
   public int findNextLetter(int y, int startPos) {
     String line = getLine(y);
     for (int i = startPos; i < line.length() - 1; i++) {
       if (line.charAt(i + 1) != ' ') {
         return i + 1;
       }
     }
     return -1;
   }
 
   public int findNextSpace(int y, int x) {
     String line = getLine(y);
     return line.indexOf(" ", x);
   }
 
   public void addListener(Listener listener) {
     file.addListener(listener);
   }
 
   public void joinRange(int start, int end) {
    int length = end - start + 1;
     for (int i = 0; i < length; i++) {
       join(start);
     }
   }
 
   private void join(int y) {
     String nextLine = removeLine(y + 1);
     changeLine(y, getLine(y) + nextLine);
   }
 
   public void setHighlight(String word) {
     file.setHighlight(word);
   }
 
   public boolean isHighlighted(int y, int x) {
     return file.isHighlighted(start + y, x);
   }
 
   public void clearHighlight() {
     file.clearHighlight();
   }
 
   public String getWordUnder(int y, int x) {
     int start = findWordStart(y, x);
     int end = findWordEnd(y, x);
     return getLine(y).substring(start, end + 1);
   }
 
   private int findWordEnd(int y, int x) {
     String line = getLine(y);
     int i = x;
     while (i < line.length() - 1 && isWordCharacter(line.charAt(i + 1))) {
       i++;
     }
     return i;
   }
 
   private int findWordStart(int y, int x) {
     String line = getLine(y);
     int i = x;
     while (i > 0 && isWordCharacter(line.charAt(i - 1))) {
       i--;
     }
     return i;
   }
 
   private boolean isWordCharacter(char c) {
     return Character.isLetterOrDigit(c);
   }
 
   public Point getNextHighlightPoint(int y, int x) {
     Point point = file.getNextHighlightPoint(start + y, x);
     if (point != null && isInView(point)) {
       return point.offset(-start, 0);
     }
     return null;
   }
 
   private boolean isInView(Point point) {
     return start <= point.getY() && point.getY() <= end;
   }
 
   public Point getPreviousHighlightPoint(int y, int x) {
     Point point = file.getPreviousHighlightPoint(start + y, x);
     if (point != null && isInView(point)) {
       return point.offset(-start, 0);
     }
     return null;
   }
 }
