 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.thura.utilities.photo.gui;
 
import com.thura.utilities.debugging.StopWatch;
 import com.thura.utilities.photo.helper.IPhoto;
 import java.awt.Color;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * A child of a PhotoControl that prints the photographic contents 
  * of a directory to the screen. The source directory as well as all the display
  * parameters reside in the control. This class simply prints the photograph, 
  * photograph name and a selection box according to the data provided.
  * 
  * @author Alexander Colvin
  */
 public class PhotoCanvas extends javax.swing.JPanel {
 
     /**
      * The parent of this canvas
      */
     private PhotoControl parent;
     
     /**
      * This method exists only to satisfy NetBeans. Do not use it
      * @deprecated
      */
     @Deprecated
     public PhotoCanvas(){
         System.out.println("BAD CONSTRUCTOR");
         //parent = new PhotoControl();
     }
     /**
      * Creates new form PhotoCanvas
      */
     public PhotoCanvas(PhotoControl parent) {
         initComponents();
         
         this.parent = parent;
         
     }
     
     @Override
     public void paintComponent(Graphics g){  
         //System.out.println("Actual update");
         parent.resizeScrollBar();
         super.paintComponent(g);
         
         //Declare null variables
         IPhoto photo; 
         List<IPhoto> subset;
         Iterator<IPhoto> photoIterator;
         int width;
         int height;
         double ratio;
         int xAdjust = 0;
         //Declare filled variables 
         int offset = parent.getScollPosition() * getPhotoPerLine();
         int rows = 0;
         int totalExpected = getNumberOfLines() * getPhotoPerLine();
         int photoNum = 0;
         int selectedID = parent.getSelectedID();
         
         drawBackground(g);
         
         /*Try to paint photos to canvas. Any erros will be met with an instant 
          * exit and warnig displayed on screen. */

         try {
             subset = parent.getPhotoSource().getPhotoRange(offset, offset + totalExpected);
             photoIterator = subset.iterator();
             
             
             
             
             while(photoIterator.hasNext() && rows <= getNumberOfLines()){
                 //Get required image data
                 photo = photoIterator.next(); 
                 Image img = photo.getIcon().getImage();
                 
                 int photoWidth = img.getWidth(null);
                 int photoHeight = img.getHeight(null);
                 xAdjust = 0;
                  
                 if(photoWidth > photoHeight){
                     ratio = (double)photoHeight / photoWidth;
                     width =  parent.getPhotoLength();
                     height = (int)((double)parent.getPhotoLength() * ratio);
                 } else if(photoHeight > photoWidth){
                     ratio = (double)photoWidth / photoHeight;
                     height =  parent.getPhotoLength();
                     width = (int)((double)parent.getPhotoLength() * ratio);
                 } else {
                     height =  parent.getPhotoLength();
                     width =  parent.getPhotoLength();
                 }
                 
                 if(width < parent.getPhotoLength()){
                     xAdjust = (parent.getPhotoLength() - width) / 2;
                 }
                 
                     
                 //Display image data
                 //TODO: Fix this mess up. Methods contain repeat code
                 DrawPhotoGraph(g, img, photoNum, rows, width, height, xAdjust);
                 DrawFileName(g, photo, photoNum, rows);
                 DrawSelectionBox(g, photo, photoNum, rows);
                 
                 //Advance and reset counters
                 photoNum++;
                 if(photoNum == getPhotoPerLine()){
                     photoNum = 0;
                     rows++;
                 }

             }
//            if(selectedID >= offset && selectedID < (offset + totalExpected)){
//                selectedID -= offset;
//                DrawHighLightBox(g, 
//                        selectedID % this.getPhotoPerLine(),
//                        selectedID / this.getPhotoPerLine() % this.getNumberOfLines());
//            }

         } catch (Exception e) {
             g.setColor(Color.red);
             g.drawString("An error has occured. PLEASE SELECT A VALID DIRECTORY", 11, 11);
         }
         
     }
 
     private void drawBackground(Graphics g){
         int x = parent.getPhotoLeftMargin() / 2;
         int y = parent.getPhotoTopMargin() / 2;
         int width = this.getWidth() - parent.getPhotoLeftMargin();
         int heigh = this.getHeight() + 5;
         
         g.setColor(Color.WHITE);
         g.fillRect(x, y, width, heigh);
     }
     
     /**
      * Draw a single photograph onto the screen
      * 
      * @param g Object to draw on
      * @param img Image to draw
      * @param photoNum number of spots along to display
      * @param rows Row on which to display
      */
     private void DrawPhotoGraph(Graphics g, Image img, int photoNum, int rows, int width, int height, int xAdjust){
         g.drawImage(
             img,
             (parent.getPhotoGap() + parent.getPhotoLength()) * photoNum + parent.getPhotoLeftMargin() + xAdjust,
             (parent.getPhotoGap() + parent.getPhotoHeight()) * rows + parent.getPhotoTopMargin() + parent.getPhotoHeight() - height,
             width, height, null);    
     }
     
     private void DrawHighLightBox(Graphics g, int photoNum, int rows){
         Color color; 
         
         int width =  parent.getPhotoLength();
         int height =  parent.getPhotoLength();
 
         color = new Color(0, 0.68f, 1, 0.17f);
         g.setColor(color);
         g.fillRect(
             (parent.getPhotoGap() + parent.getPhotoLength()) * photoNum + parent.getPhotoTopMargin(), 
             (parent.getPhotoGap() + parent.getPhotoHeight()) * rows + parent.getPhotoLeftMargin(), 
             width, height);
         color = new Color(0, 0.0f, 1, 0.56f);
         g.setColor(color);
         g.drawRect(
             (parent.getPhotoGap() + parent.getPhotoLength()) * photoNum + parent.getPhotoTopMargin(), 
             (parent.getPhotoGap() + parent.getPhotoHeight()) * rows + parent.getPhotoLeftMargin(), 
             width, height); 
     }
     
     /**
      * Draw the selection box over the photo
      * 
      * @param g Object to draw on
      * @param photo IPhoto to get name from
      * @param photoNum number of spots along to display
      * @param rows Row on which to display
      */
     private void DrawSelectionBox(Graphics g, IPhoto photo, int photoNum, int rows){
         Color color;
         int width =  parent.getPhotoLength();
         int height =  parent.getPhotoLength();
         
         if(photo.getSelected()){
             color = new Color(0, 0.68f, 1, 0.54f);
             g.setColor(color);
             g.fillRect(
                 (parent.getPhotoGap() + parent.getPhotoLength()) * photoNum + parent.getPhotoTopMargin(), 
                 (parent.getPhotoGap() + parent.getPhotoHeight()) * rows + parent.getPhotoLeftMargin(), 
                 width, height);
             color = new Color(0, 0.0f, 1, 1f);
             g.setColor(color);
             g.drawRect(
                 (parent.getPhotoGap() + parent.getPhotoLength()) * photoNum + parent.getPhotoTopMargin(), 
                 (parent.getPhotoGap() + parent.getPhotoHeight()) * rows + parent.getPhotoLeftMargin(), 
                 width, height);  
         }
     }
     
     /**
      * Draw the filename under the photo
      * 
      * @param g Object to draw on
      * @param photo IPhoto to get name from
      * @param photoNum number of spots along to display
      * @param rows Row on which to display
      */
     private void DrawFileName(Graphics g, IPhoto photo, int photoNum, int rows){
         String newName;
         int msgWidth;
         FontMetrics fm = g.getFontMetrics();
         
         Color color = new Color(1, 0, 0, 0.75f);
         g.setColor(color);
                 
         if(photo.getFileName().length() > 20){
             newName = photo.getFileName().substring(photo.getFileName().length() - 20);
         } else {
             newName = photo.getFileName();
         }
         msgWidth = fm.stringWidth(newName);
         g.drawString(newName, 
                     (parent.getPhotoGap() + parent.getPhotoLength()) * photoNum + parent.getPhotoLeftMargin() + parent.getPhotoLength() / 2 - msgWidth / 2, 
                     (parent.getPhotoGap() + parent.getPhotoHeight()) * rows + parent.getPhotoTopMargin() + parent.getPhotoHeight() + parent.getFontHeight());
 
     }
     
     /**
      * Calculate the number of photos that should be displayed on a single line
      * based on the panel dimensions
      * 
      * @return integer representing max photos to display per line
      */
     public int getPhotoPerLine(){
         double y = this.getWidth();
         
         double components = ((y + 10) / (parent.getPhotoGap() + parent.getPhotoLength()));
          
         return (int) Math.floor(components);
     }
     
     public int getNumberOfLines(){
         double y = this.getHeight();
         
         double components = ((y + 70)/ (parent.getPhotoGap() + parent.getPhotoHeight()));
         
         return (int) Math.floor(components);
     }
     
     /**
      * Update the panel with any new information
      */
     public void redraw(){
         Graphics g = this.getGraphics();
         paintComponent(g);
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 547, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 360, Short.MAX_VALUE)
         );
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
 }
