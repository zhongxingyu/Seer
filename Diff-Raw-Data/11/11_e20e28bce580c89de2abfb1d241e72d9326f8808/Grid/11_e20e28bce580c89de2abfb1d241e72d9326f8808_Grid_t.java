 /*
  * This file is part of GAIL.
  *
  * GAIL is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GAIL is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with GAIL.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright © 2011 Eneko Sanz Blanco <nkogear@gmail.com>
  *
  */
 
 package gail;
 
 import java.awt.*;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import javax.swing.JComponent;
 
 /**
  *
  * @author eneko
  */
 public class Grid extends JComponent {
     
     private int horizCellNumber;
     private int vertCellNumber;
     private int cellWidth;
     private int cellHeight;
     private int lineStroke = 2;
     
     /**
      * Create a 2D grid with the size passed as parameters.
      * 
      * @param horizCellNumber
      * @param vertCellNumber 
      */
     public Grid(int horizCellNumber, int vertCellNumber) {
         this.horizCellNumber = horizCellNumber;
         this.vertCellNumber = vertCellNumber;
         this.setLayout(null);
         this.addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent e) {
                 pauseRunningAnimations();
                 layComponents();
                 cancelRunningAnimations();
             }
 
         });
     }
     
     public int getCellHeight() {
         return cellHeight;
     }
 
     public int getCellWidth() {
         return cellWidth;
     }
 
     /**
      * Add a GridElement to this Grid in the specified position.
      * 
      * It's a shorthand method, the equivalent of calling add(elem) and then
      * elem.setPositionOnGrid(position).
      * 
      * Note that the row/columns start with 0. The first is (0,0), so a 2x2 
      * grid would have {(0,0)(0,1)(1,0)(1,1)} positions.
      * 
      * @param ge
      * @param positionOnGrid 
      */
     public void add(GridElement ge, Point positionOnGrid) {
         ge.setPositionOnGrid(positionOnGrid);
         layComponent(ge);
         this.add((Component)ge);
         repaint(ge.getBounds());
     }
     
     /**
      * Add a GridElement to this Grid
      * 
      * @param ge
      * @param gridPosition 
      */
     public void add(GridElement ge) {
         layComponent(ge);
         this.add((Component)ge);
         repaint(ge.getBounds());
     }
 
     /**
      * Remove a GridElement from this Grid
      * 
      * @param ge
      * @param gridPosition 
      */
     public void remove(GridElement ge) {
         super.remove(ge);
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                             RenderingHints.VALUE_RENDER_QUALITY);
         g2.setColor(this.getBackground());
         g2.fillRect(0, 0, getWidth(), getHeight());
         g2.setColor(this.getForeground());
         g2.setStroke(new BasicStroke(lineStroke));
        cellWidth = ((getWidth() + lineStroke) / horizCellNumber);
         cellHeight =  ((getHeight() + lineStroke) / vertCellNumber);
         int tempDrawingPos = 0;
         for(int i=0; i < vertCellNumber; i++){
             g2.drawLine(0, tempDrawingPos, getWidth(), tempDrawingPos);
             tempDrawingPos += cellHeight;
         }
         g2.drawLine(0, getHeight() - lineStroke, getWidth(), getHeight()-lineStroke);
         tempDrawingPos = 0;
         for(int i=0; i < horizCellNumber; i++){
             g2.drawLine(tempDrawingPos, 0, tempDrawingPos, getHeight());
             tempDrawingPos += cellWidth;
         }
         g2.drawLine(getWidth() - lineStroke, 0, getWidth() - lineStroke, getHeight());
     }
 
     private void layComponents() {
        cellWidth = ((getWidth() + lineStroke) / horizCellNumber);
         cellHeight =  ((getHeight() + lineStroke) / vertCellNumber);
         Component[] components =  this.getComponents();
         for(int i = 0; i < components.length; i++) {
             GridElement elem = (GridElement) components[i];
            elem.setSize(cellWidth + 2, cellHeight);
             elem.setLocation((cellWidth * elem.getPositionOnGrid().x),
                              cellHeight * elem.getPositionOnGrid().y);
         }
     }
 
     public void layComponent(GridElement elem) {
         cellWidth = ((getWidth() + lineStroke) / horizCellNumber);
         cellHeight =  ((getHeight() + lineStroke) / vertCellNumber);
        elem.setSize(cellWidth + 2, cellHeight);
         elem.setLocation(cellWidth * elem.getPositionOnGrid().x,
                          cellHeight * elem.getPositionOnGrid().y);
     }
 
     private void pauseRunningAnimations() {
         Component[] components =  this.getComponents();
         for(int i = 0; i < components.length; i++) {
             ((GridElement) components[i]).pauseAnimations();
         }
     }
     
     private void cancelRunningAnimations() {
         Component[] components =  this.getComponents();
         for(int i = 0; i < components.length; i++) {
             ((GridElement) components[i]).cancelAnimations();
         }
     }
 
 }
