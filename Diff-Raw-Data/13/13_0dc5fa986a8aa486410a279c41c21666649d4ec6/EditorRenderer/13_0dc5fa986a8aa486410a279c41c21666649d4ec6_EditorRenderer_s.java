 package com.id.rendering;
 
 import java.awt.Rectangle;
 
 import com.id.editor.Editor;
 
 public class EditorRenderer {
   private final int fontWidthPx;
   private final int fontHeightPx;
   private final Rectangle screen;
   private final Editor editor;
   private final int fontDescentPx;
 
   public EditorRenderer(Editor editor, Rectangle screen, int fontWidthPx, int fontHeightPx, int fontDescentPx) {
     this.editor = editor;
     this.screen = screen;
     this.fontWidthPx = fontWidthPx;
     this.fontHeightPx = fontHeightPx;
     this.fontDescentPx = fontDescentPx;
   }
 
   public Matrix render() {
     int startX = screen.x / fontWidthPx;
     int startY = screen.y / fontHeightPx;
     int endX = (screen.x + screen.width - 1) / fontWidthPx;
     int endY = (screen.y + screen.height - 1) / fontHeightPx;
 
     int linesToDraw = endY - startY + 1;
     int charsToDraw = endX - startX + 1;
 
     return matrix(startY, startX, linesToDraw, charsToDraw);
   }
 
   private Matrix matrix(int startY, int startX, int linesToDraw, int charsToDraw) {
     int height = Math.min(linesToDraw, editor.getLineCount() - startY);
     int width = charsToDraw;
     Matrix matrix = new Matrix(height, width, fontDescentPx, fontHeightPx, fontWidthPx, startY, startX);
     for (int i = 0; i < height; i++) {
       int lineY = startY + i;
      setLine(i, startX, charsToDraw, matrix, editor.getLine(lineY));
     }
     return matrix;
   }
 
   private String safeSubstring(String line, int x, int length) {
     int endIndex = Math.min(line.length(), x + length);
     return line.substring(x, endIndex);
   }
 
  private void setLine(int y, int startX, int length, Matrix matrix, String line) {
     line = safeSubstring(line, startX, length);
     for (int i = 0; i < Math.min(matrix.getWidth(), line.length()); i++) {
      matrix.setLetter(y, i, line.charAt(i));
      if (editor.isInVisual(y, i)) {
        matrix.setVisual(y, i, true);
       }
     }
   }
 }
