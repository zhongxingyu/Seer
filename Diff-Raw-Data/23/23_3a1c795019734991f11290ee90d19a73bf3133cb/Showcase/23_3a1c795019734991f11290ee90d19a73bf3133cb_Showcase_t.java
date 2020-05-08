 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package evopaint.gui;
 
 import evopaint.Configuration;
 import evopaint.Paint;
 import evopaint.commands.*;
 import evopaint.Selection;
 import evopaint.World;
 import evopaint.Perception;
 import evopaint.gui.util.WrappingScalableCanvas;
 import evopaint.util.logging.Logger;
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
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.event.MouseInputListener;
 
 /**
  *
  * @author tam
  */
 
 public class Showcase extends WrappingScalableCanvas implements MouseInputListener, MouseWheelListener, Observer, SelectionManager {
     private Configuration configuration;
 
     private MainFrame mainFrame;
 
     private boolean leftButtonPressed = false;
     private boolean toggleMouseButton2Drag = false;
 
     private PaintCommand paintCommand;
     private MoveCommand moveCommand;
     private SelectCommand selectCommand;
 
     private SelectionList currentSelections = new SelectionList();
     private Selection activeSelection;
 
     private boolean isDrawingSelection = false;
     private Point selectionStartPoint;
     private Point currentMouseDragPosition;
 
     private BrushIndicatorOverlay brushIndicatorOverlay;
 
     public Showcase(Configuration configuration, MainFrame mf, World world, Perception perception, CommandFactory commandFactory) {
         super(perception.getImage());
 
         this.configuration = configuration;
         this.mainFrame = mf;
         this.paintCommand = new PaintCommand(configuration);
         this.moveCommand = new MoveCommand(configuration);
         this.moveCommand.setCanvas(this);
         this.selectCommand = commandFactory.GetSelectCommand(currentSelections);
 
         this.currentSelections.addObserver(this);
 
         this.brushIndicatorOverlay = new BrushIndicatorOverlay(this,
                 new Rectangle(configuration.brush.size, configuration.brush.size));

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
         
         if (isDrawingSelection && selectionStartPoint != null && currentMouseDragPosition != null) {
             Point startPoint = selectionStartPoint;
             Point endPoint = currentMouseDragPosition;
             if (startPoint.x > endPoint.x || startPoint.y > endPoint.y) {
                 Point temp = endPoint;
                 endPoint = startPoint;
                 startPoint = temp;
             }
             // FIXME Selection.draw(g2, startPoint, endPoint, scale);
         }
 
         for(Selection selection : currentSelections) {
             // FIXME if (selection.isHighlighted())
                 // FIXME Selection.draw(g2, selection, scale);
         }
         if (activeSelection != null) {
            // FIXME Selection.draw(g2, activeSelection, scale);
         }
     }
 
     public void mouseWheelMoved(MouseWheelEvent e) {
        // needs to be checked _before_ zooming
        if (e.getSource() == this && mainFrame.getActiveTool() == PaintCommand.class) {
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
             if (mainFrame.getActiveTool() == PaintCommand.class) {
                 paintCommand.setLocation(transformToImageSpace(e.getPoint()));
                 paintCommand.execute();
             } else if (mainFrame.getActiveTool() == MoveCommand.class) {
                 moveCommand.setSource(e.getPoint());
                 //moveCommand.setScale(this.scale);
             } else if (mainFrame.getActiveTool() == SelectCommand.class) {
                 // FIXME this.selectionStartPoint = SelectCommand.TranslatePointToScale(e.getPoint(), scale);
                 this.isDrawingSelection = true;
                 // FIXME selectCommand.setLocation(e.getPoint(), scale);
                 selectCommand.execute();
             }
         } else if (e.getButton() == MouseEvent.BUTTON3) {
              if (configuration.paintHistory.size() < 1) {
                  return;
              }
             //if (false == e.isPopupTrigger()) {
             //    return;
             //}
             final JPopupMenu paintHistoryMenu = new JPopupMenu("Paint History");
             for (final Paint paint : configuration.paintHistory) {
                 JMenuItem menuItem = paint.toMenuItem();
                 menuItem.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
                         configuration.paint = paint;
                         mainFrame.setPaint(paint);
                         if (false == configuration.paintHistory.getFirst().equals(paint) &&
                                 configuration.paintHistory.contains(paint)) {
                             configuration.paintHistory.remove(paint);
                             configuration.paintHistory.addFirst(paint);
                         }
                     }
                 });
                 paintHistoryMenu.add(menuItem);
             }
             paintHistoryMenu.show(e.getComponent(), e.getX(), e.getY());
         } else if (e.getButton() == MouseEvent.BUTTON2) {
             toggleMouseButton2Drag = true;
             moveCommand.setSource(e.getPoint());
         }
     }
 
     public void mouseReleased(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {
             leftButtonPressed = false;
             if (mainFrame.getActiveTool() == SelectCommand.class) {
                 this.isDrawingSelection = false;
                 // FIXME selectCommand.setLocation(e.getPoint(), scale);
                 selectCommand.execute();
             }
         } else if (e.getButton() == MouseEvent.BUTTON2) {
             toggleMouseButton2Drag = false;
         }
     }
 
     public void mouseDragged(MouseEvent e) {
         if (leftButtonPressed == true) {
             // FIXME this.currentMouseDragPosition = SelectCommand.TranslatePointToScale(e.getPoint(), scale);
             if (mainFrame.getActiveTool() == PaintCommand.class) {
                 // TODO: paint pixels between mouse drags
                 // TODO: Maybe refactor state into PaintCommand
                 paintCommand.setLocation(transformToImageSpace(e.getPoint()));
                 paintCommand.execute();
             } else if (mainFrame.getActiveTool() == MoveCommand.class) {
                 moveCommand.setDestination(e.getPoint());
                 moveCommand.execute();
             }
         } else if (toggleMouseButton2Drag == true) {
             moveCommand.setDestination(e.getPoint());
             moveCommand.execute();
         }
         if (mainFrame.getActiveTool() == PaintCommand.class) {
             brushIndicatorOverlay.setBounds(new Rectangle(
                     transformToImageSpace(e.getPoint()),
                     new Dimension(configuration.brush.size, configuration.brush.size)));
         }
     }
 
     public void mouseClicked(MouseEvent e) {
     }
 
     public void mouseEntered(MouseEvent e) {
         if (mainFrame.getActiveTool() == PaintCommand.class) {
             subscribe(brushIndicatorOverlay);
         }
     }
 
     public void mouseExited(MouseEvent e) {
         unsubscribe(brushIndicatorOverlay);
     }
 
     public void mouseMoved(MouseEvent e) {
         if (mainFrame.getActiveTool() == PaintCommand.class) {
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
             this.activeSelection = selection;
             Logger.log.error("Selection from %s-%s to %s-%s", selection.getStartPoint().getX(), selection.getStartPoint().getY(), selection.getEndPoint().getX(), selection.getEndPoint().getY());
         }
     }
 }
