 /**
  * Copyright (c) 2011, PM Company (dector, vitalyp) All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *   - Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  *   - Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  *  - Neither the name of the nor the names of its
  * contributors may be used to endorse or promote products derived
  * from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.github.pmcompany.petri_net.editor.panels;
 
 import com.github.pmcompany.petri_net.editor.Grid;
 import com.github.pmcompany.petri_net.editor.Settings;
 import com.github.pmcompany.petri_net.editor.elements.*;
 import com.github.pmcompany.petri_net.editor.elements.Point;
 import com.github.pmcompany.petri_net.editor.listeners.GridPanelListener;
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Line2D;
 import java.awt.image.BufferedImage;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.ListIterator;
 
 /**
  * JPanel which draws P/T net components
  *
  * @author dector (dector9@gmail.com)
  * @version 0.1
  */
 public class GridPanel extends JPanel {
     /** Grid parameters */
     private Grid grid;
 
     private List<GraphicsElement> addedElements;
 
     private List<GraphicsElement> draggedElements;
     private List<GraphicsElement> selectedElements;
 
     private List<Connection> connections;
     private Connection currentConnection;
     private Point currentConnectionEnd;
 
     private Stroke elementStroke;
     private Stroke connectionStroke;
 
     private Polygon arrow;
 
     /**
      * Create new instance with determined size
      *
      * @param dimension panels's size
      */
     public GridPanel(Dimension dimension) {
         addedElements = new ArrayList<GraphicsElement>();
         draggedElements = new ArrayList<GraphicsElement>();
         selectedElements = new ArrayList<GraphicsElement>();
         connections = new ArrayList<Connection>();
 
         grid = new Grid();
         setSize(dimension);
 
         elementStroke = new BasicStroke(Settings.ELEMENT_BORDER_WIDTH);
         connectionStroke = new BasicStroke(Settings.CONNECTION_WIDTH);
 
         GridPanelListener gpl = new GridPanelListener(this);
         addMouseListener(gpl);
         addMouseMotionListener(gpl);
 
         arrow = createArrow();
     }
 
     /**
      * Draw P/T net
      *
      * @param graphics Graphics2D component to draw
      */
     @Override
     public void paintComponent(Graphics graphics) {
         super.paintComponent(graphics);
 
         Graphics2D g = (Graphics2D) graphics;
 
         //drawGrid
         g.setColor(Settings.GRID_COLOR);
         for (int x = grid.getLeftCoord(); x < getWidth(); x += grid.getStepX()) {
             for (int y = grid.getTopCoord(); y < getHeight(); y += grid.getStepY()) {
                 g.drawRect(x, y, Settings.GRID_WIDTH, Settings.GRID_WIDTH);
             }
         }
 
         // ========== [BEGIN] DRAW P/T NET
         g.setStroke(elementStroke);
 
         int elementX;
         int elementY;
         boolean dragging;
         boolean selected;
         for (GraphicsElement currElement : addedElements) {
             elementX = currElement.getX();
             elementY = currElement.getY();
             dragging = isDragging(currElement);
             selected = isSelected(currElement);
 
             switch (currElement.getType()) {
                 case PLACE: {
                     elementX -= GraphicsElement.PLACE_SIZE/2;
                     elementY -= GraphicsElement.PLACE_SIZE/2;
 
                     if (selected) {
                         g.setColor(Settings.SELECTION_FILL_COLOR);
                     } else {
                         g.setColor(Settings.PLACE_FILL_COLOR);
                     }
 
                     g.fillOval(elementX, elementY, GraphicsElement.PLACE_SIZE, GraphicsElement.PLACE_SIZE);
 
                     if (dragging) {
                         g.setColor(Settings.ELEMENT_DRAGGING_BORDER_COLOR);
                     } else {
                         g.setColor(Settings.PLACE_BORDER_COLOR);
                     }
 
                     g.drawOval(elementX, elementY, GraphicsElement.PLACE_SIZE, GraphicsElement.PLACE_SIZE);
                 } break;
                 case TRANSITION: {
                     elementX -= GraphicsElement.TRANSITION_WIDTH/2;
                     elementY -= GraphicsElement.TRANSITION_HEIGHT/2;
 
                     if (selected) {
                         g.setColor(Settings.SELECTION_FILL_COLOR);
                     } else {
                         g.setColor(Settings.TRANSITION_FILL_COLOR);
                     }
                     g.fillRect(elementX, elementY, GraphicsElement.TRANSITION_WIDTH, GraphicsElement.TRANSITION_HEIGHT);
 
                     if (dragging) {
                         g.setColor(Settings.ELEMENT_DRAGGING_BORDER_COLOR);
                     } else {
                         g.setColor(Settings.TRANSITION_BORDER_COLOR);
                     }
 
                     g.drawRect(elementX, elementY, GraphicsElement.TRANSITION_WIDTH, GraphicsElement.TRANSITION_HEIGHT);
                 } break;
                 case MOMENTAL_TRANSITION: {
                     elementX -= GraphicsElement.MOMENTAL_TRANSITION_WIDTH/2;
                     elementY -= GraphicsElement.MOMENTAL_TRANSITION_HEIGHT/2;
 
                     if (selected) {
                         g.setColor(Settings.SELECTION_FILL_COLOR);
                     } else {
                         g.setColor(Settings.MOMENTAL_TRANSITION_FILL_COLOR);
                     }
                     g.fillRect(elementX, elementY, GraphicsElement.MOMENTAL_TRANSITION_WIDTH, GraphicsElement.MOMENTAL_TRANSITION_HEIGHT);
 
                     if (dragging) {
                         g.setColor(Settings.ELEMENT_DRAGGING_BORDER_COLOR);
                     } else {
                         g.setColor(Settings.MOMENTAL_TRANSITION_BORDER_COLOR);
                     }
 
                     g.drawRect(elementX, elementY, GraphicsElement.MOMENTAL_TRANSITION_WIDTH, GraphicsElement.MOMENTAL_TRANSITION_HEIGHT);
                 } break;
             }
 
         }
 
         g.setStroke(connectionStroke);
 
         for (Connection connection : connections) {
             drawConnection(g, connection);
         }
 
         if (currentConnection != null) {
             drawConnection(g, currentConnection, currentConnection.getStart(), currentConnectionEnd);
         }
         // ========== [END] DRAW P/T NET
 
     }
 
     private void drawConnection(Graphics2D g, Connection connection) {
         drawConnection(g, connection, connection.getStart(), connection.getEnd());
     }
 
     private void drawConnection(Graphics2D g, Connection connection, Point start, Point end) {
         if (connection.getMiddlePoints() == null) {
             PTNetElements elementType = connection.getFrom().getType();
             if (elementType == PTNetElements.PLACE) {
                 g.setColor(Settings.PLACE_TRANSITION_CONNECTION_COLOR);
             } else if (elementType == PTNetElements.TRANSITION || elementType == PTNetElements.MOMENTAL_TRANSITION) {
                 g.setColor(Settings.TRANSITION_PLACE_CONNECTION_COLOR);
             }
 
             Point[] points = createArrowPoints(connection, start, end);
 
             if (points != null) {
                 int x0 = points[0].getX();
                 int y0 = points[0].getY();
 
                 int x1 = points[1].getX();
                 int y1 = points[1].getY();
 
                 g.drawLine(x0, y0, x1, y1);
 
                 double dx = x1 - x0;
                 double dy = y1 - y0;
 
                double theta = Math.atan(dy / dx);
 
 //                g.setColor(Color.RED);
 //                g.drawLine(-300, 0, 300, 0);
 //                g.drawLine(0, -300, 0, 300);
 
                 g.translate(x1, y1);
 
 //                g.setColor(Color.BLUE);
 //                g.drawLine(-300, 0, 300, 0);
 //                g.drawLine(0, -300, 0, 300);
 
                 g.rotate(theta);
 
 //                g.setColor(Color.GREEN);
 //                g.drawLine(-300, 0, 300, 0);
 //                g.drawLine(0, -300, 0, 300);
 
 //                g.translate(-x1, -y1);
 //
 //                g.setColor(Color.CYAN);
 //                g.drawLine(-300, 0, 300, 0);
 //                g.drawLine(0, -300, 0, 300);
 
                 g.fillPolygon(arrow);
                 g.rotate(-theta);
                 g.translate(-x1, -y1);
 
             }
         } else {
             System.out.println("MiddlePoints");
         }
     }
 
     private Point[] createArrowPoints(Connection connection, Point start, Point end) {
         Point[] p = null;
 
         if (! connection.isInsideFromElement(end)) {
             GraphicsElement from = connection.getFrom();
             GraphicsElement to = connection.getTo();
 
             p = from.getConnectionPointsWith(end, (to != null) ? to.getWidth() / 2 : 0);
         }
 
         return p;
     }
 
     private Polygon createArrow() {
         Polygon arrow = new Polygon();
 
         arrow.addPoint(-Connection.ARROW_INNER_LENGTH, 0);
         arrow.addPoint(-Connection.ARROW_OUTER_LENGTH,
                        (int)(-Connection.ARROW_OUTER_LENGTH * Connection.ARROW_ANGLE_TAN));
         arrow.addPoint(0, 0);
         arrow.addPoint(-Connection.ARROW_OUTER_LENGTH,
                        (int)(Connection.ARROW_OUTER_LENGTH * Connection.ARROW_ANGLE_TAN));
         arrow.addPoint(-Connection.ARROW_INNER_LENGTH, 0);
 
         return arrow;
     }
 
     /**
      * Returns preffered component size
      *
      * @return preffered component size
      */
     @Override
     public Dimension getPreferredSize() {
         return getSize();
     }
 
     public void addElement(GraphicsElement element) {
         // Place by grid
         placeElementByGrid(element);
 
         addedElements.add(element);
     }
 
     private GraphicsElement placeElementByGrid(GraphicsElement element) {
         int elementPos = element.getX() - element.getWidth()/2;
         elementPos %= grid.getElementXStep();
         if (elementPos != 0) {
             if (elementPos >= grid.getElementXStep()) {
                 elementPos = element.getX() + elementPos;
             } else {
                 elementPos = element.getX() - elementPos;
             }
             element.setX(elementPos);
         }
 
         elementPos = element.getY() - element.getHeight()/2;
         elementPos %= grid.getElementYStep();
         if (elementPos != 0) {
             if (elementPos >= grid.getElementYStep()) {
                 elementPos = element.getY() + elementPos;
             } else {
                 elementPos = element.getY() - elementPos;
             }
             element.setY(elementPos);
         }
 
         return element;
     }
 
     public void addAndSelectElement(PTNetElements type, int x, int y, boolean multiselect) {
         System.out.printf("Adding and selecting %s at %d:%d%n", type, x, y);
         GraphicsElement element = new GraphicsElement(type, x, y);
         addElement(element);
         selectElement(element, multiselect);
     }
 
     public void addElement(PTNetElements type, int x, int y) {
         addElement(new GraphicsElement(type, x, y));
     }
 
     public GraphicsElement getElementAt(int x, int y) {
         GraphicsElement element = null;
 
         ListIterator<GraphicsElement> iterator = addedElements.listIterator(addedElements.size());
 
         GraphicsElement currElement;
         int borderLeft;
         int borderRight;
         int borderTop;
         int borderBottom;
 
         while (element == null && iterator.hasPrevious()) {
             currElement = iterator.previous();
 
             borderLeft = currElement.getX() - currElement.getWidth()/2;
             borderRight = currElement.getX() + currElement.getWidth()/2;
             borderBottom = currElement.getY() - currElement.getHeight()/2;
             borderTop = currElement.getY() + currElement.getHeight()/2;
 
             if (borderLeft <= x && x <= borderRight && borderBottom <= y && y <= borderTop) {
                 element = currElement;
             }
         }
 
         return element;
     }
 
     public void startDragElements() {
         draggedElements.addAll(selectedElements);
     }
 
     public void stopDragElements() {
         draggedElements.clear();
     }
 
     public int[] dragElements(int x, int y, int x0, int y0) {
         int[] newCoords = new int[2];
         newCoords[0] = x0;
         newCoords[1] = y0;
 
         int dx = x - x0;
         int changedX = dx / grid.getElementXStep();
         int dy = y - y0;
         int changedY = dy / grid.getElementYStep();
 
         if (changedX != 0) {
             newCoords[0] = x;
             for (GraphicsElement element : draggedElements) {
                 element.setX(element.getX() + dx);
             }
         }
 
         if (changedY != 0) {
             newCoords[1] = y;
             for (GraphicsElement element : draggedElements) {
                 element.setY(element.getY() + dy);
             }
         }
 
         return newCoords;
     }
 
     private void dragElements(int dx, int dy) {
         for (GraphicsElement element : draggedElements) {
             element.setX(element.getX() + dx);
             element.setY(element.getY() + dy);
         }
     }
 
     private void selectOneElement(GraphicsElement element) {
         unselectAllElements();
         selectAnotherElement(element);
     }
 
     public void selectElement(GraphicsElement element, boolean multiselect) {
         if (element != null) {
             if (multiselect) {
                 if (isSelected(element)) {
                     unselectElement(element);
                 } else {
                     selectAnotherElement(element);
                 }
             } else {
                 selectOneElement(element);
             }
         } else {
             if (! multiselect) {
                 unselectAllElements();
             }
         }
     }
 
     private void selectAnotherElement(GraphicsElement element) {
         selectedElements.add(element);
     }
 
     private void unselectElement(GraphicsElement element) {
         selectedElements.remove(element);
     }
 
     private void unselectAllElements() {
         selectedElements.clear();
     }
 
     public boolean isAdded(GraphicsElement element) {
         return addedElements.contains(element);
     }
 
     public boolean isSelected(GraphicsElement element) {
         return selectedElements.contains(element);
     }
 
     public boolean isDragging(GraphicsElement element) {
         return draggedElements.contains(element);
     }
 
     public void startConnection(GraphicsElement element, boolean breaked) {
         System.out.printf("Connection started on %s%n", element.getType());
 
         if (breaked) {
             startBreakedConnection(element);
         } else {
             startStraightConnection(element);
         }
     }
 
     private void startBreakedConnection(GraphicsElement element) {
         throw new NotImplementedException();
     }
 
     private void startStraightConnection(GraphicsElement element) {
         currentConnection = new StraightConnection(element);
         updateConnection(element.getPosition());
     }
 
     public void updateConnection(int endX, int endY) {
         updateConnection(new Point(endX, endY));
     }
 
     public void updateConnection(Point endPosition) {
         currentConnectionEnd = endPosition;
     }
 
     public void endConnection(GraphicsElement element) {
         if (element != null) {
             PTNetElements fromType = currentConnection.getFrom().getType();
             PTNetElements toType = element.getType();
 
             if (! fromType.isSimmilar(toType)) {
                 currentConnection.setTo(element);
 //                currentConnection.countOptimaCoord();
                 connections.add(currentConnection);
             }
         }
 
         currentConnection = null;
         currentConnectionEnd = null;
     }
 }
