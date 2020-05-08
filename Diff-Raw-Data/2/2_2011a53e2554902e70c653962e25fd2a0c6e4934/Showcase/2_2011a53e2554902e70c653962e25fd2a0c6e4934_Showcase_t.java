 /*
  *  Copyright (C) 2010 Markus Echterhoff <tam@edu.uni-klu.ac.at>,
  *                      Daniel Hoelbling (http://www.tigraine.at)
  *
  *  This file is part of EvoPaint.
  *
  *  EvoPaint is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with EvoPaint.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package evopaint.gui;
 
 import evopaint.Configuration;
 import evopaint.commands.*;
 import evopaint.Selection;
 import evopaint.gui.util.WrappingScalableCanvas;
 import evopaint.util.logging.Logger;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.Timer;
 import javax.swing.event.MouseInputListener;
 
 /**
  *
  * @author Markus Echterhoff <tam@edu.uni-klu.ac.at>
  * @author Daniel Hoelbling (http://www.tigraine.at)
  */
 public class Showcase extends WrappingScalableCanvas implements MouseInputListener, MouseWheelListener, Observer, SelectionManager {
     private Configuration configuration;
 
     private boolean leftButtonPressed = false;
     private boolean toggleMouseButton2Drag = false;
 
     private PaintCommand paintCommand;
     private MoveCommand moveCommand;
     private SelectCommand selectCommand;
     private FillSelectionCommand fillCommand;
     private EraseCommand eraseCommand;
 
     private SelectionList currentSelections = new SelectionList();
     private Selection activeSelection;
 
     private boolean isDrawingSelection = false;
     private Point selectionStartPoint;
     private Point currentMouseDragPosition;
 
     private BrushIndicatorOverlay brushIndicatorOverlay;
     private Timer paintingTimer;
     private Painter painter;
     
     private SelectionIndicatorOverlay draggingSelectionOverlay;
 
     public Showcase(Configuration configuration, CommandFactory commandFactory) {
         super(configuration.perception.getImage());
 
         this.configuration = configuration;
         this.paintCommand = new PaintCommand(configuration, this);
         this.moveCommand = new MoveCommand(configuration);
         this.moveCommand.setCanvas(this);
         this.selectCommand = new SelectCommand(currentSelections, this);
         this.fillCommand = new FillSelectionCommand(this);
         this.eraseCommand = new EraseCommand(configuration, this);
 
         this.currentSelections.addObserver(this);
 
         this.brushIndicatorOverlay = new BrushIndicatorOverlay(this,
                 new Rectangle(configuration.brush.size, configuration.brush.size));
 
         this.painter = new Painter();
         this.paintingTimer = new Timer(0, this.painter);
         this.paintingTimer.setDelay(10);
 
         addMouseWheelListener(this);
         addMouseListener(this);
         addMouseMotionListener(this);
     }
 
     public Configuration getConfiguration() {
         return configuration;
     }
 
     public SelectionList getCurrentSelections() {
         return currentSelections;
     }
 
     @Override
     public void paint(Graphics g) {
         super.paint(g);
 
         Graphics2D g2 = (Graphics2D) g;
     }
 
     public void mouseWheelMoved(MouseWheelEvent e) {
         // needs to be checked _before_ zooming
         if (e.getSource() == this && configuration.mainFrame.getActiveTool() == PaintCommand.class) {
             brushIndicatorOverlay.setBounds(new Rectangle(
                     transformToImageSpace(e.getPoint()),
                     new Dimension(configuration.brush.size, configuration.brush.size)));
         }
         
         ZoomCommand zoomCommand ;
         if (e.getWheelRotation() < 0) 
             zoomCommand = new ZoomInCommand(this);
         else
             zoomCommand = new ZoomOutCommand(this);
         zoomCommand.execute();
         
     }
 
     public void mousePressed(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {
             leftButtonPressed = true;
             if (configuration.mainFrame.getActiveTool() == PaintCommand.class) {
                 painter.setLocation(e.getPoint());
                 paintingTimer.start();
                 configuration.paint.rememberCurrent();
             } else if (configuration.mainFrame.getActiveTool() == MoveCommand.class) {
                 moveCommand.setSource(e.getPoint());
                 //moveCommand.setScale(this.scale);
             } else if (configuration.mainFrame.getActiveTool() == SelectCommand.class) {
                 this.selectionStartPoint = transformToImageSpace(e.getPoint());
                 draggingSelectionOverlay = new SelectionIndicatorOverlay(this, new Rectangle());
                 subscribe(draggingSelectionOverlay);
                 selectCommand.setLocation(this.selectionStartPoint);
                 selectCommand.execute();
             } else if (configuration.mainFrame.getActiveTool() == ZoomCommand.class) {
             	ZoomInCommand zoomInCommand = new ZoomInCommand(this);	
             	zoomInCommand.execute();
             	this.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
             } else if (configuration.mainFrame.getActiveTool() == FillSelectionCommand.class) {
             	fillCommand.setLocation(transformToImageSpace(e.getPoint()));
             	fillCommand.execute();
             } else if (configuration.mainFrame.getActiveTool() == PickCommand.class) {
             	PickCommand pickCommand = new PickCommand(configuration);
             	pickCommand.setLocation(transformToImageSpace(e.getPoint()));
             	pickCommand.execute();
             } else if (configuration.mainFrame.getActiveTool() == EraseCommand.class) {
             	eraseCommand.setLocation(transformToImageSpace(e.getPoint()));
             	eraseCommand.execute();
             }
         } else if (e.getButton() == MouseEvent.BUTTON3) {
         	if (configuration.mainFrame.getActiveTool() == ZoomCommand.class){
             	ZoomOutCommand zoomOutCommand = new ZoomOutCommand(this);
             	zoomOutCommand.execute();
             	this.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
             }
         	else {
         		configuration.paint.showHistory(this, e.getPoint());
         	}
         } else if (e.getButton() == MouseEvent.BUTTON2) {
             if (configuration.mainFrame.getActiveTool() == ZoomCommand.class){
                 scaleReset();
             } else {
                 toggleMouseButton2Drag = true;
                 moveCommand.setSource(e.getPoint());
             }
         }
     }
 
     public void mouseReleased(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {
             leftButtonPressed = false;
             paintingTimer.stop();
             if (configuration.mainFrame.getActiveTool() == SelectCommand.class) {
                 this.isDrawingSelection = false;
                 selectCommand.setLocation(transformToImageSpace(e.getPoint()));
                 selectCommand.execute();
                 unsubscribe(draggingSelectionOverlay);
                 draggingSelectionOverlay = null;
             }
         } else if (e.getButton() == MouseEvent.BUTTON2) {
             toggleMouseButton2Drag = false;
         }
     }
 
     public void mouseDragged(MouseEvent e) {
     	this.currentMouseDragPosition = e.getPoint();
         if (leftButtonPressed == true) {
         	if (configuration.mainFrame.getActiveTool() == SelectCommand.class) {
         		Point pointInImageSpace = transformToImageSpace(currentMouseDragPosition);
         		draggingSelectionOverlay.setBounds(new Rectangle(selectionStartPoint, new Dimension(pointInImageSpace.x - selectionStartPoint.x, pointInImageSpace.y - selectionStartPoint.y)));
         	}
             else if (configuration.mainFrame.getActiveTool() == PaintCommand.class) {
                 painter.setLocation(currentMouseDragPosition);
             } else if (configuration.mainFrame.getActiveTool() == MoveCommand.class) {
                 moveCommand.setDestination(e.getPoint());
                 moveCommand.execute();
             } else if (configuration.mainFrame.getActiveTool() == EraseCommand.class) {
            	eraseCommand.setLocation(transformToImageSpace(currentMouseDragPosition));
             	eraseCommand.execute();
             }
         } else if (toggleMouseButton2Drag == true) {
             moveCommand.setDestination(e.getPoint());
             moveCommand.execute();
         }
         if (configuration.mainFrame.getActiveTool() == PaintCommand.class) {
             brushIndicatorOverlay.setBounds(new Rectangle(
             		transformToImageSpace(e.getPoint()),
                     new Dimension(configuration.brush.size, configuration.brush.size)));
         }
     }
 
     public void mouseClicked(MouseEvent e) {
     }
 
     public void mouseEntered(MouseEvent e) {
         if (configuration.mainFrame.getActiveTool() == PaintCommand.class) {
             subscribe(brushIndicatorOverlay);
         }
     }
 
     public void mouseExited(MouseEvent e) {
         unsubscribe(brushIndicatorOverlay);
     }
 
     public void mouseMoved(MouseEvent e) {
         if (configuration.mainFrame.getActiveTool() == PaintCommand.class) {
             brushIndicatorOverlay.setBounds(new Rectangle(
                     transformToImageSpace(e.getPoint()),
                     new Dimension(configuration.brush.size, configuration.brush.size)));
         }
     }
 
     public Selection getActiveSelection() {
         return activeSelection;
     }
 
     public void clearSelections() {
         this.activeSelection = null;
         this.currentSelections.clear();
     }
 
     public void setActiveSelection(Selection selection) {
         this.activeSelection = selection;
         for(Selection sel : currentSelections) {
         	unsubscribe(sel);
         }
         subscribe(selection);
         ClearSelectionHighlight();
     }
 
     private void ClearSelectionHighlight() {
         for(Selection sel : currentSelections ){
             sel.setHighlighted(false);
         }
     }
 
     public void removeActiveSelection() {
         this.currentSelections.remove(activeSelection);
         activeSelection = null;
     }
 
     public void update(Observable o, Object arg) {
         SelectionList.SelectionListEventArgs selectionEvent = (SelectionList.SelectionListEventArgs) arg;
         if (selectionEvent.getChangeType() == SelectionList.ChangeType.ITEM_ADDED) {
             Selection selection = selectionEvent.getSelection();
             setActiveSelection(selection);
             Logger.log.error("Selection from %s-%s to %s-%s", selection.getStartPoint().getX(), selection.getStartPoint().getY(), selection.getEndPoint().getX(), selection.getEndPoint().getY());
         }
     }
 
     private class Painter implements ActionListener {
         private Point location;
 
         public Painter() {
         }
 
         public void setLocation(Point locationInUserSpace) {
             this.location = transformToImageSpace(locationInUserSpace);
         }
 
         public void actionPerformed(ActionEvent e) {
             paintCommand.setLocation(location);
             paintCommand.execute();
         }
     }
 
     /* a nice idea, but it is slow since we have to draw not only the line, but the pixels into the world
     private class ContinuousPainter implements ActionListener {
         private Point location;
         private Queue<Point> destinations;
 
         public ContinuousPainter() {
             this.destinations = new ArrayDeque<Point>();
         }
 
         public void setLocation(Point locationInUserSpace) {
             this.location = transformToImageSpace(locationInUserSpace);
             this.destinations.clear();
         }
 
         public void setDestination(Point destinationInUserSpace) {
          this.destinations.add(transformToImageSpace(destinationInUserSpace));
         }
 
         public void actionPerformed(ActionEvent e) {
             Point destination = destinations.peek();
             while (destination != null) {
                 if (destination.x != location.x) {
                     double gradient = gradient(location, destination);
                     if (gradient < 0.5) {
                         if (destination.y != location.y) {
                             if (destination.y > location.y) {
                                 location.y += gradient;
                             } else {
                                 location.y -= gradient;
                             }
                         }
                         if (destination.x > location.x) {
                             location.x += 1;
                         } else {
                             location.x -= 1;
                         }
                     } else {
                         if (destination.x > location.x) {
                             location.x += gradient;
                         } else {
                             location.x -= gradient;
                         }
                         if (destination.y > location.y) {
                             location.y += 1;
                         } else {
                             location.y -= 1;
                         }
                     }
                     break;
                 }
                 else if (destination.y != location.y) {
                     if (destination.y > location.y) {
                         location.y += 1;
                     } else {
                         location.y -= 1;
                     }
                     break;
                 } else {
                     destinations.remove();
                     destination = destinations.peek();
                 }
             }
             paintCommand.setLocation(location);
             paintCommand.execute();
         }
 
         private double gradient(Point location, Point destination) {
             assert(location.x - destination.x != 0);
             return ((double)Math.abs(destination.y - location.y)) /
                     ((double)Math.abs(destination.x - location.x));
         }
 
     }
    */
     
 }
