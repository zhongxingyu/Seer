 package com.appliedanalog.javani.graphs;
 
 import com.appliedanalog.javani.HandCalculator;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Polygon;
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Iterator;
 import com.appliedanalog.javani.listeners.DepthMapListener;
 
 /**
  *
  * @author jbetker
  */
 public class ClippingBitmapView extends Canvas implements DepthMapListener{
     int[] img_data;
     int resx, resy;
     int clipx, clipy;
     
     //this enables z-axis clipping, removing data points that are gt or lt the clipping window from the specified depth
     boolean do_clipping = false;
     int clipping_depth = 0;
     int clipping_window = 1;
 
     boolean do_xpt = false;
     int num_xpts;
     int[] xpt_x, xpt_y;
     int ch_x, ch_y; //denotes the hand "center", with relation to the extension points.
 
     double hand_orientation_angle = 0.;
 
     boolean display_dbg1 = false;
     String dbg1_text;
     
     ArrayList<Polygon> splines = new ArrayList<Polygon>();   
     
     public ClippingBitmapView(){
         this(0, 0, 320, 240);
     }
     
     public ClippingBitmapView(int clipx, int clipy, int resx, int resy){
         this.resx = resx;
         this.resy = resy;
         this.clipx = clipx;
         this.clipy = clipy;
         xpt_x = new int[5];
         xpt_y = new int[5];
         img_data = new int[resx * resy];
     }
     
     public void enableDepthClipping(int cd, int cw){
         do_clipping = true;
         clipping_depth = cd;
         clipping_window = cw;
     }
     
     public void disableDepthClipping(){
         do_clipping = false;
     }
     
     public void setResolution(int rx, int ry){
         resx = rx;
         resy = ry;
         img_data = new int[rx * ry];
     }
 
     public void setXpt(int i, int x, int y, int chx, int chy, double ho){
         xpt_x[i] = x;
         xpt_y[i] = y;
         ch_x = chx;
         ch_y = chy;
         do_xpt = true;
         hand_orientation_angle = ho;
     }
 
     public void setXptAbsolute(int i, int x, int y, int chx, int chy, double ho){
         xpt_x[i] = x - clipx;
         xpt_y[i] = y - clipy;
         ch_x = chx - clipx;
         ch_y = chy - clipy;
         do_xpt = true;
         hand_orientation_angle = ho;
     }
 
     public void setNumXpts(int nxpts){
         num_xpts = nxpts;
     }
 
     public int addSpline(){
         splines.add(new Polygon());
         return splines.size()-1;
     }
     
     public void addPoint(int splineid, int x, int y){
         splines.get(splineid).addPoint(x, y);
     }
 
     /**
      * This function adds a spline point from the coordinate space of the whole bitmap,
      * rather than the clipped window
      * @param splineid
      * @param x
      * @param y
      */
     public void addAbsolutePoint(int splineid, int x, int y){
         splines.get(splineid).addPoint(x - clipx, y - clipy);
     }
     
     public Polygon getSpline(int splineid){
         return splines.get(splineid);
     }
     
     public void removeSpline(int splineid){
         splines.remove(splineid);
     }
     
     /**
      * Does NOT move the clipping window in the graph that is currently displayed,
      * will move it the next time new image data is fed in.
      * @param x
      * @param y 
      */
     public void moveClippingWindow(int x, int y){
         clipx = x;
         clipy = y;
     }
 
     public void centerClippingWindowOn(int x, int y){
         clipx = x - (resx / 2);
         clipy = y - (resy / 2);
         if(clipx < 0) clipx = 0;
         if(clipy < 0) clipy = 0;
     }
 
     public void debugText(String str){
         display_dbg1 = true;
         dbg1_text = str;
     }
 
     public void disableDebug(){
         display_dbg1 = false;
     }
     
     /**
      * Applies clipping to the new depth data and repaints.
      * @param dd 
      */
     public void newDepthMap(int[] dd, int width, int h){
         int row, col;
         int id_ptr = 0;
         for(int x = 0; x < dd.length; x++){
             col = x % width;
             row = x / width;
             if(col <= clipx || col > clipx + resx) continue;
             if(row <= clipy || row > clipy + resy) continue;
             if(!do_clipping){
                 img_data[id_ptr] = dd[x];
             }else if(Math.abs(dd[x] - clipping_depth) <= clipping_window //if there is clipping, is the specified pixel in the region?
                     ){
                 img_data[id_ptr] = (dd[x] - clipping_depth + clipping_window) * (256 * 3 / (clipping_window * 2));
                 if(img_data[id_ptr] >= 256 && img_data[id_ptr] < 512){
                     img_data[id_ptr] = 0xff | ((img_data[id_ptr] - 256) << 16);
                 }else if(img_data[id_ptr] >= 512){
                     img_data[id_ptr] = ((768 - img_data[id_ptr]) << 16);
                 }
             }else{
                 img_data[id_ptr] = 0xffffffff;
             }
             id_ptr++;
         }
         repaint();
     }
     
     /**
      * Implemented from Canvas. Manages double buffering for semi-smooth frame
      * progression.
      * @param g Graphics.
      */
     public void update(Graphics g){
         paint(g);
     }
 
     /**
      * This violates MVC standards but is really necessary in this case.
      */
     HandCalculator _calc;
     public void _bindCalculator(HandCalculator c){
         _calc = c;
     }
 
     /**
      * Implemented from Canvas. 
      * @param g2 Graphics to draw onto.
      */
     public void paint(Graphics g2){
         int w = this.getWidth();
         int h = this.getHeight();
         
         BufferedImage i = new BufferedImage(w, h, ColorSpace.TYPE_RGB);
         Graphics g = i.createGraphics();
         
         //we should be stashing the buffered image if possible
         BufferedImage img = new BufferedImage(resx, resy, BufferedImage.TYPE_3BYTE_BGR);
         for(int x = 0; x < img_data.length; x++){
             img.setRGB(x % resx, x / resx, img_data[x]);
         }
         g.drawImage(img, 0, 0, null);
 
         //orientation derivation lines
         if(_calc != null){
             for(int x = 0; x < _calc.midpoints.length; x++){
                int mp = _calc.midpoints[x] - clipx;
                int mph = _calc._mp_y - x * 2 - clipy;
                 g.setColor(Color.BLACK);
                 g.drawLine(0, mph, resx, mph);
                 g.setColor(Color.WHITE);
                 g.fillRect(mp, mph, 1, 1);
             }
         }
 
         //splines
         g.setColor(Color.YELLOW);
         Iterator<Polygon> iter = splines.iterator();
         while(iter.hasNext()){
             g.drawPolygon(iter.next());
         }
 
         //finger points
         if(do_xpt){
             for(int x = 0; x < num_xpts; x++){
                 g.setColor(Color.YELLOW);
                 g.fillRect(xpt_x[x] - 2, xpt_y[x] - 2, 5, 5);
                 g.setColor(Color.ORANGE);
                 g.drawLine(ch_x, ch_y, xpt_x[x], xpt_y[x]);
             }
         }
 
         //draw hand orientation vector
         g.setColor(Color.RED.darker());
         g.fillRect(ch_x - 2, ch_y - 2, 5, 5);
         g.drawLine(ch_x, ch_y, 
                 ch_x + (int)(200. * Math.cos(hand_orientation_angle * Math.PI / 180.)),
                 ch_y - (int)(200. * Math.sin(hand_orientation_angle * Math.PI / 180.)));
 
         if(display_dbg1){
             g.drawString(dbg1_text, 20, 20);
         }
         
         g2.drawImage(i, 0, 0, this);
     }
 }
