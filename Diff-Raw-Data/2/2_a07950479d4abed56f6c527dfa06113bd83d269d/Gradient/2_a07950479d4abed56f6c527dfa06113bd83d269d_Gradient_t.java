 package com.tomclaw.tcsg;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import tcsgeditor.Selector;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /**
  *
  * @author solkin
  */
 public class Gradient extends Primitive {
 
   private int x, y, width, height;
   private int colorFrom, colorFinl;
   private boolean isProportional;
   private Figure figure;
   private boolean isFill;
   private boolean isVertical;
   private int t_x, t_y, t_w, t_h;
 
   public Gradient( int x, int y, int width, int height, int colorFrom,
           int colorFinl, boolean isFill, boolean isVertical,
           boolean isProportional, Figure figure ) {
     this.x = x;
     this.y = y;
     this.width = width;
     this.height = height;
     this.colorFrom = colorFrom;
     this.colorFinl = colorFinl;
     this.isFill = isFill;
     this.isVertical = isVertical;
     this.isProportional = isProportional;
     this.figure = figure;
   }
 
   @Override
   public void paint( Graphics g ) {
     if ( isProportional ) {
       t_x = figure.getPropX( x );
       t_y = figure.getPropY( y );
       t_w = figure.getPropWidth( width );
       t_h = figure.getPropHeight( height );
     } else {
       t_x = figure.getAbsX( x );
       t_y = figure.getAbsY( y );
       t_w = figure.getAbsWidth( x, width );
       t_h = figure.getAbsHeight( y, height );
     }
     if ( isFill ) {
       if ( isVertical ) {
         ScaleGraphics.fillVerticalGradient( g, t_x, t_y, t_w, t_h, colorFrom, colorFinl );
       } else {
         ScaleGraphics.fillHorizontalGradient( g, t_x, t_y, t_w, t_h, colorFrom, colorFinl );
       }
     } else {
       if ( isVertical ) {
         ScaleGraphics.drawVerticalGradient( g, t_x, t_y, t_w, t_h, colorFrom, colorFinl );
       } else {
         ScaleGraphics.drawHorizontalGradient( g, t_x, t_y, t_w, t_h, colorFrom, colorFinl );
       }
     }
 
   }
 
   @Override
   public void setSecLocation( int x, int y ) {
     width = x - this.x;
     height = y - this.y;
   }
 
   @Override
   public void setFigure( Figure figure ) {
     this.figure = figure;
   }
 
   @Override
   public void setLocation( int x, int y ) {
     this.x = x;
     this.y = y;
   }
 
   @Override
   public Gabarite getGabarite() {
     return new Gabarite( x, y, x + width, y + height );
   }
 
   @Override
   public Object[][] getFields() {
     return new Object[][] {
               { "Положение X", Integer.valueOf( x ) },
               { "Положение Y", Integer.valueOf( y ) },
               { "Ширина", Integer.valueOf( width ) },
               { "Высота", Integer.valueOf( height ) },
               { "Цвет начальный", new Color( colorFrom ) },
               { "Цвет конечный", new Color( colorFinl ) },
               { "Пропорциональность", Boolean.valueOf( isProportional ) },
               { "Заполнение", Boolean.valueOf( isFill ) },
               { "Направление", new Selector( new String[] { "Вертикальный",
                   "Горизонтальный" }, isVertical ? 0 : 1 ) }
             };
   }
 
   @Override
   public void setFields( Object[][] fields ) {
     x = ( Integer ) fields[0][1];
     y = ( Integer ) fields[1][1];
     width = ( Integer ) fields[2][1];
     height = ( Integer ) fields[3][1];
     colorFrom = ( ( Color ) fields[4][1] ).getRGB();
     colorFinl = ( ( Color ) fields[5][1] ).getRGB();
     isProportional = ( Boolean ) fields[6][1];
     isFill = ( Boolean ) fields[7][1];
    isVertical = ( ( Selector ) fields[8][1] ).getSelectedIndex() == 0
             ? true : false;
   }
 }
