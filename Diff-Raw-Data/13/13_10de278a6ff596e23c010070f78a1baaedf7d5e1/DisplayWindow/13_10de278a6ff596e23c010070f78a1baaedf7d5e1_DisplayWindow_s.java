 /*
  * Copyright (C) 2014 David McInnis
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package ewucalligraphy.gui;
 
 /**
  *
  * @author David McInnis <davidm@eagles.ewu.edu>
  */
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.util.LinkedList;
 
 
 public class DisplayWindow extends javax.swing.JFrame
 {
     private BufferedImage fileImage;
     private final int[]   imageSize = new int[2];
     private int[] imageSizeScaled = new int[2];
     
     private final int oldWindowSize[] = new int[2];
     private final int newWindowSize[] = new int[2];
     private final int topOffset  = 30;
     private final int brlOffset = 10; //Botom, Right & Left
     private boolean drawed = false;
     
     private LinkedList<Line> myLines = new LinkedList<>();
     
     
     /**
      * Creates new form DisplayWindow
      */
     public DisplayWindow()
     {
         initComponents();
     }
     
     public void setImage(BufferedImage IfileImage)
     {
         fileImage = IfileImage;
         
         imageSize[0] = fileImage.getWidth();
         imageSize[1] = fileImage.getHeight();
 	
     }
     
 
     
     	@Override
 	public void paint(Graphics g)
 	{
 		super.paint(g);
                 
                 drawImage(g);
                 
                 drawOverImage(g);
        
         }
         
         private void drawImage(Graphics g)
         {
                //This part scales the image to fit within the window
 
 		if(fileImage != null)
 		{
                         newWindowSize[0] = this.getWidth() - brlOffset * 2;
 			newWindowSize[1] = this.getHeight() - topOffset - brlOffset;
 			
 
 			boolean windowChanged = (newWindowSize[0] != oldWindowSize[0]) ||
 									(newWindowSize[1] != oldWindowSize[1]);
 
 			if(windowChanged || !drawed)
 			{
 				oldWindowSize[0] = newWindowSize[0];
 				oldWindowSize[1] = newWindowSize[1];
 
				int windowRatio = newWindowSize[0] * imageSize[0];
				int picRatio    = newWindowSize[1] * imageSize[1];
 
 				int newImageSizeWidth, newImageSizeLength;
 				newImageSizeWidth = 0; newImageSizeLength = 0;
 
				if(windowRatio < picRatio)
 				{
 					//window not long enough
 					newImageSizeLength = newWindowSize[1];
 					newImageSizeWidth = (newImageSizeLength * imageSize[0]) / imageSize[1];
 				}
 				else
 				{
 					//window not wide enough
					newImageSizeWidth = newWindowSize[1];
 					newImageSizeLength = (newImageSizeWidth * imageSize[1]) / imageSize[0];
 				}
 
 
 
 
 				if((newImageSizeWidth > 0 && newImageSizeLength > 0) || !drawed)
 				{
                                         imageSizeScaled[0] = newImageSizeWidth;
                                         imageSizeScaled[1] = newImageSizeLength;
 					Image scaledImage = fileImage.getScaledInstance(newImageSizeWidth, newImageSizeLength, Image.SCALE_FAST);
 					drawed = g.drawImage(scaledImage, brlOffset, topOffset, newImageSizeWidth, newImageSizeLength, null);
 				}
 			}
                 }
         }
         
         private void drawOverImage(Graphics g) //This function draws stuff over the actual image
         {
             drawLines(g);
         }
         
         private void drawLines(Graphics g)
         {
             int[] iStart, oStart, iEnd, oEnd;
             
             for(Line curLine : myLines)
             {
                 iStart = curLine.getStart();
                 iEnd   = curLine.getEnd();
                 
                 oStart = transformCoordinates(iStart);
                 oEnd   = transformCoordinates(iEnd);
                 
                 g.setColor(curLine.getColor());
                 
                 g.drawLine(oStart[0], oStart[1], oEnd[0], oEnd[1]);
                 
             }
         }
         
         public int[] transformCoordinates(int[] XYin)
         {
             int[] XYout = new int[2];
            
             XYout[0] = brlOffset + (XYin[0] * imageSizeScaled[0]) / imageSize[0];
                     
             XYout[1] = topOffset + (XYin[1] * imageSizeScaled[1]) / imageSize[1];
             
             
             return XYout;
         }
         
         public void addLine(Line newLine)
         {
             myLines.add(newLine);
         }
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents()
     {
 
         setMinimumSize(new java.awt.Dimension(200, 200));
         addWindowListener(new java.awt.event.WindowAdapter()
         {
             public void windowClosing(java.awt.event.WindowEvent evt)
             {
                 exitForm(evt);
             }
         });
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Exit the Application
      */
     private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
         System.exit(0);
     }//GEN-LAST:event_exitForm
 
 
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
 
     void wipeLines() {
         myLines.clear();
     }
 }
