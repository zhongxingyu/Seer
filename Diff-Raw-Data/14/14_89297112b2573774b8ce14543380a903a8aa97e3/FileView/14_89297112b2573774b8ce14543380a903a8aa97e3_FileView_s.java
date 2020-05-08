 package com.id.file;
 
 import java.util.ArrayList;
 import java.util.List;
 
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
 
   public String removeLine(int y) {
     return file.removeLine(start + y);
   }
 
   public void splitLine(int y, int x, String paddingText) {
     String removedText = removeText(y, x);
     insertLine(y + 1, paddingText + removedText);
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
    int length = Math.min(getLineCount() - 1, end - start + 1);
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
 
   public void appendText(int y, String text) {
     changeLine(y, getLine(y) + text);
 
   }
 
   public void insertText(int y, int x, String... lines) {
     insertText(y, x, false, lines);
   }
 
   public void insertTextWithLineBreak(int y, int x, String... lines) {
     insertText(y, x, true, lines);
   }
 
   public void insertText(int y, int x, boolean lineBreakOnLast, String... lines) {
     if (lines.length == 0) {
       return;
     }
     if (file.isEmpty()) {
       insertLines(0, lines);
       return;
     }
     String topLine = getLine(y);
     String head = topLine.substring(0, x);
     String tail = topLine.substring(x);
     changeLine(y, head + lines[0]);
     int lastInsertedLine = y;
     for (int i = 1; i < lines.length; i++) {
       insertLine(y + i, lines[i]);
       lastInsertedLine = y + i;
     }
     if (lineBreakOnLast) {
       insertLine(lastInsertedLine + 1, tail);
     } else {
       appendText(lastInsertedLine, tail);
     }
   }
 
   public void insertLines(int y, String... lines) {
     int i = y;
     for (String line : lines) {
       insertLine(i, line);
       i++;
     }
   }
 
   public List<String> getLineRange(int startY, int endY) {
     List<String> result = new ArrayList<String>();
     for (int i = startY; i <= endY; i++) {
       result.add(getLine(i));
     }
     return result;
   }
 }
