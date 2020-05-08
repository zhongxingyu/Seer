 /*
  * Soga2D
  *
  * Copyright 2011 Matúš Sulír.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package soga2d;
 
 import java.awt.Graphics2D;
 
 /**
  *
  * @author Matúš Sulír
  */
 public abstract class GraphicObject {
     protected int width;
     protected int height;
     protected int x;
     protected int y;
     private GraphicBoard board;
     
     public GraphicObject() {
         this(0, 0, 32, 32);
     }
     
     public GraphicObject(int x, int y, int width, int height) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
     }
     
     public abstract void paint(Graphics2D g);
 
     void assignBoard(GraphicBoard board) {
         this.board = board;
     }
     
     public int getWidth() {
         return width;
     }
 
     public int getHeight() {
         return height;
     }
 
     public void setSize(int width, int height) {
         this.width = width;
         this.height = height;
         update();
     }
     
     public int getX() {
         return x;
     }
     
     public int getY() {
         return y;
     }
     
     public void moveTo(int x, int y) {
         this.x = x;
         this.y = y;
         update();
     }
     
     public void moveBy(int deltaX, int deltaY) {
         x += deltaX;
         y += deltaY;
         update();
     }
     
     public void moveInFrontOf(GraphicObject what) {
         board.moveInFrontOf(this, what);
         update();
     }
     
     public void sendToBackground() {
         board.sendToBackground(this);
         update();
     }
     
     public void bringToForeground() {
         board.bringToForeground(this);
         update();
     }
     
     private void update() {
         if (board != null)
             board.notifyChanged();
     }
 }
