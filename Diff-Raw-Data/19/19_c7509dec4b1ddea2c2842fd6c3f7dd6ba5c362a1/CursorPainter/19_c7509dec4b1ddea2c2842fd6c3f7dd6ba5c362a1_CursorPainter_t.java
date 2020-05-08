 package com.github.dreamrec.gcomponent;
 
 import java.awt.*;
 
 /**
  *
  */
 public class CursorPainter implements IPainter<ICursorPainterModel> {
     public void paint(Graphics2D g, ICursorPainterModel paintModel) {
         int x = paintModel.getCursorPosition();
        int width = paintModel.getCursorWidth()-1;
         int height = paintModel.getYSize() - 10;
         g.drawRect(x,0,width,height);
     }
 }
