 package com.id.editor;
 
 import com.id.file.FileView;
 
 public class Visual {
   public enum Mode {
     NONE,
     CHAR,
     LINE,
     BLOCK ;
     public boolean contains(Visual range, Point point) {
       switch (this) {
       case NONE:
         return false;
       case CHAR:
         return range.getStartPoint().beforeOrEqual(point) && !range.getEndPoint().before(point);
       case LINE:
         return range.getStartPoint().getY() <= point.getY() && point.getY() <= range.getEndPoint().getY();
       case BLOCK:
         Point startPoint = range.getStartPoint();
         Point endPoint = range.getEndPoint();
         int xMin = Math.min(startPoint.getX(), endPoint.getX());
         int xMax = Math.max(startPoint.getX(), endPoint.getX());
         int x = point.getX();
         return LINE.contains(range, point) && xMin <= x && x <= xMax;
       default:
         throw new IllegalStateException("Unknown Mode: " + this);
       }
     }
   };
 
   private final Cursor cursor;
   private Mode mode = Mode.NONE;
   private Point anchor;
 
   public Visual(Cursor cursor) {
     this.cursor = cursor;
   }
 
   public void toggleMode(Mode mode) {
     if (this.mode == Mode.NONE) {
       this.mode = mode;
       reset();
     } else if (this.mode == mode) {
       this.mode = Mode.NONE;
     } else {
       this.mode = mode;
     }
   }
 
   private void reset() {
     this.anchor = cursor.getPoint();
   }
 
   public boolean isOn() {
     return this.mode != Mode.NONE;
   }
 
   public Point getStartPoint() {
     return isCursorBeforeAnchor() ? cursor.getPoint() : anchor;
   }
 
   public Point getEndPoint() {
     return isCursorBeforeAnchor() ? anchor : cursor.getPoint();
   }
 
   public boolean isCursorBeforeAnchor() {
     return cursor.getPoint().before(anchor);
   }
 
   public boolean contains(Point point) {
     return mode.contains(this, point);
   }
 
   public void removeFrom(FileView file) {
     switch (mode) {
     case BLOCK:
       int left = Math.min(getStartPoint().getX(), getEndPoint().getX());
       int right = Math.max(getStartPoint().getX(), getEndPoint().getX());
 
       for (int i = getStartPoint().getY(); i < getEndPoint().getY(); i++) {
         file.removeText(i, left, right - left);
       }
       break;
     case CHAR:
       int startLine = getStartPoint().getY();
       int endLine = getEndPoint().getY();
       int startX = getStartPoint().getX();
       int endX = getEndPoint().getX();
       if (startLine == endLine) {
         file.removeText(startLine, startX, endX - startX + 1);
         return;
       }
      file.removeText(startLine, getStartPoint().getX());
      file.removeText(endLine, 0, getEndPoint().getX() + 1);
       String tail = file.getLine(endLine);
      if (startLine + 1 <= endLine) {
        file.removeLineRange(startLine + 1, endLine);
      }
       file.appendToLine(startLine, tail);
       break;
     }
   }
 }
