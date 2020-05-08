 package com.id.ui.editor;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JPanel;
 
 import com.id.app.App;
 import com.id.editor.Editor;
 import com.id.editor.Point;
 import com.id.events.EditorKeyHandler;
 
 @SuppressWarnings("serial")
 public class TextPanel extends JPanel {
   private final Editor editor;
 
   public TextPanel(Editor editor) {
     this.editor = editor;
 //    setPreferredSize(new Dimension(600, 768));
     setFont(new Font("system", Font.PLAIN, 14));
   }
 
   @Override
   public void setSize(Dimension d) {
     super.setSize(d);
   }
 
   @Override
   public void paint(Graphics g) {
     super.paint(g);
     App.configureFont(g);
 
     int fontDescentPx = g.getFontMetrics().getDescent();
     int fontWidthPx = g.getFontMetrics().getWidths()[70];
     int fontHeightPx = g.getFontMetrics().getHeight();
     int linesHigh = (int) Math.floor(g.getClipBounds().getHeight() / fontHeightPx);
     int linesWide = (int) Math.floor(g.getClipBounds().getWidth() / fontWidthPx);
 
     Point point = editor.getCursorPosition();
     int cursorWidthPx = editor.isInInsertMode() ? 2 : fontWidthPx;
     g.drawRect(point.getX() * fontWidthPx, point.getY() * fontHeightPx, cursorWidthPx, fontHeightPx);
     for (int y = 0; y < linesHigh && y < editor.getLineCount(); y++) {
       for (int x = 0; x < linesWide && x < editor.getLine(y).length(); x++) {
         if (editor.isInVisual(y, x)) {
           Color color = g.getColor();
           g.setColor(Color.GRAY);
           g.fillRect(x * fontWidthPx, y * fontHeightPx - fontDescentPx, fontWidthPx, fontHeightPx + fontDescentPx);
           g.setColor(color);
         }
       }
       g.drawString(editor.getLine(y), 0, (y + 1) * fontHeightPx - fontDescentPx);
     }
    int x = g.getClipBounds().x;
    int y = g.getClipBounds().y;
    int height = g.getClipBounds().height - 1;
    int width = g.getClipBounds().width - 1;
    int realWidth = getSize().width - 1;
    int realHeight = getSize().height - 1;
    if (x == 0) {
      g.drawLine(x, y, x, y + height);
    }
    if (x + width == realWidth) {
      g.drawLine(x + width, y, x + width, y + height);
    }
    if (y == 0) {
      g.drawLine(x, y, x + width, y);
    }
    if (y + height == realHeight) {
      g.drawLine(x, y + height, x + width, y + height);
    }
   }
 
   public boolean handleKeyPress(KeyEvent e) {
     return new EditorKeyHandler().handleKeyPress(e, editor);
   }
 }
