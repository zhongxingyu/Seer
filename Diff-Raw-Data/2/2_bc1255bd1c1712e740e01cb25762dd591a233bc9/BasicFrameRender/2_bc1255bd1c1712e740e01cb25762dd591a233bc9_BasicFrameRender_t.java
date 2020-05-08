 /*
  * Copyright (C) 2012 Lasse Dissing Hansen
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  */
 
 package volpes.ldk;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.util.Stack;
 
 /**
  * A basic render using the java2D api.
  * Mainly for fallback usage
  * @author Lasse Dissing Hansen
  * @since 0.2
  */
 public class BasicFrameRender extends  Render {
 
     private JFrame frame;
     private Canvas canvas;
 
     private Settings settings;
 
     private int width;
     private int height;
     private String windowName;
 
    private Stack<AffineTransform> matrixStack = new Stack<AffineTransform>();
 
     public BasicFrameRender(Settings s) {
         this.width = s.has("width") ? s.getInt("width") : 640;
         this.height = s.has("height") ? s.getInt("height") : 480;
         this.windowName = s.has("windowName") ? s.getString("windowName") : "LDK";
 
     }
 
     private BufferStrategy bs;
     private Graphics2D g;
 
     @Override
     protected void preRender() {
         bs = canvas.getBufferStrategy();
         g = (Graphics2D)bs.getDrawGraphics();
         g.setColor(Color.BLACK);
         g.fillRect(0,0,width,height);
     }
 
     @Override
     protected void postRender() {
         g.dispose();
         bs.show();
         Toolkit.getDefaultToolkit().sync();
     }
 
     @Override
     protected void initScreen(){
         frame = new JFrame(windowName);
         JPanel panel = new JPanel(new BorderLayout());
         canvas = new Canvas();
         panel.add(canvas);
 
         frame.setContentPane(panel);
         frame.pack();
         frame.setLocationRelativeTo(null);
         frame.setSize(width,height);
         frame.setResizable(false);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setVisible(true);
     }
 
     @Override
     protected void initRender() {
         canvas.createBufferStrategy(2);
     }
 
     @Override
     protected void attachInput(Input input) {
         canvas.addMouseListener(input);
         canvas.addMouseMotionListener(input);
         canvas.addKeyListener(input);
     }
 
     @Override
     protected void updateSettings(Settings s) {
         if (width != (s.has("width") ? s.getInt("width") : 640) || height != (s.has("height") ? s.getInt("height") : 480)) {
             this.width = s.has("width") ? s.getInt("width") : 640;
             this.height = s.has("height") ? s.getInt("height") : 480;
             frame.setSize(width,height);
         }
     }
 
     @Override
     public void drawImage(BufferedImage img, int x, int y) {
         g.drawImage(img,x,y,null);
     }
 
     @Override
     public void drawString(String str, int x, int y) {
         g.drawString(str,x,y);
     }
 
     @Override
     public void drawLine(int x1, int y1, int x2, int y2) {
         g.drawLine(x1,y1,x2,y2);
     }
 
     @Override
     public void drawRect(int x, int y, int width, int height) {
         g.drawRect(x,y,width,height);
     }
 
     @Override
     public void fillRect(int x, int y, int width, int height) {
         g.fillRect(x,y,width,height);
     }
 
     @Override
     public void setColor(Color color) {
         g.setColor(color);
     }
 
     @Override
     public void scale(float sx, float sy) {
         g.scale(sx,sy);
     }
 
     @Override
     public void translate(int tx, int ty) {
         g.translate(tx,ty);
     }
 
     @Override
     public void rotate(double theta) {
         g.rotate(theta);
     }
 
     @Override
     public void rotate(double theta, int x, int y) {
         g.rotate(theta,x,y);
     }
 
     @Override
     public void push() {
         matrixStack.push(g.getTransform());
     }
 
     @Override
     public void pop() {
         g.setTransform(matrixStack.pop());
     }
 }
