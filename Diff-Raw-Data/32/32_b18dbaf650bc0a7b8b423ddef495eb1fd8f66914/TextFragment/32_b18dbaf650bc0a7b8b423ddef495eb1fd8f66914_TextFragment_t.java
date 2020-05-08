 package com.id.editor;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.id.editor.Visual.Mode;
 import com.id.file.FileView;
 
 public class TextFragment {
   private final Mode mode;
   private final List<String> lines;
   private final boolean lineBreakOnLast;
 
   public TextFragment(Visual.Mode mode, boolean lineBreakOnLast, String... line) {
     this(mode, lineBreakOnLast, Arrays.asList(line));
   }
 
   public TextFragment(Visual.Mode mode, boolean lineBreakOnLast, List<String> lines) {
     this.mode = mode;
     this.lineBreakOnLast = lineBreakOnLast;
     this.lines = lines;
   }
 
   public void put(int y, int x, FileView file) {
     switch (mode) {
     case CHAR:
       int cappedX = Math.min(file.getLine(y).length(), x + 1);
       file.insertText(y, cappedX, lineBreakOnLast, lines);
       break;
     case LINE:
       file.insertLines(y + 1, lines);
       break;
     default:
       throw new UnsupportedOperationException();
     }
   }
 
   public void putBefore(int y, int x, FileView file) {
     switch (mode) {
     case CHAR:
       file.insertText(y, x, lines);
       break;
     case LINE:
       file.insertLines(y, lines);
       break;
     default:
       throw new UnsupportedOperationException();
     }
   }
 
   public int getLineCount() {
     return lines.size();
   }
 
   public String getLine(int y) {
     return lines.get(y);
   }

  public Mode getMode() {
    return mode;
  }
 }
