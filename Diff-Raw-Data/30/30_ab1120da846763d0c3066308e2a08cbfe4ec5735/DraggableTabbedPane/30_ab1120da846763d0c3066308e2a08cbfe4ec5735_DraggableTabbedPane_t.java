 package zisko.multicastor.program.view;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 import javax.swing.JTabbedPane;
 
 /**
  * Die Klasse DraggableTabbedPane erbt von JTabbedPane und lässt zusätzlich zu JTabbed Pane
  * ein grafish ansprechendes verschieben von Tabs per Drag&Drop zu.
  * 
  * @version 1.5
  * @author Filip Haase
  * @author Jonas Traub
  * @author Matthis Hauschild
  *
  */
 @SuppressWarnings("serial")
 public class DraggableTabbedPane extends JTabbedPane {
 
 	  private boolean dragging = false;
 	  private Image tabImage = null;
 	  private Point currentMouseLocation = null;
 	  private int draggedTabIndex = 0;
 	  private int mouseRelX;
 	  private int mouseRelY;
 	  private Rectangle bounds;
 
   /**
    *  Im Konstruktor wird ein neuen MouseMotionListener angelegt, welcher schaut ob
    *  ich, wenn ich mit der Maus klicke(mouseDragged) über einem tab bin.
    *  Wenn Ja wird ein Bild des "gedragten" Tabs in den Buffer gezeichnet.
    *  
    *  @param parentFrame Referenz auf GUI-Frame
    */
   public DraggableTabbedPane(final FrameMain parentFrame) {
     super();
     addMouseMotionListener(new MouseMotionAdapter() {
       public void mouseDragged(MouseEvent e) {
 
         if(!dragging) {
           // Gets the tab index based on the mouse position
           int tabNumber = getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), e.getY());
 
           if(tabNumber >= 0) {
             draggedTabIndex = tabNumber;
             bounds = getUI().getTabBounds(DraggableTabbedPane.this, tabNumber);
             
             // Paint the tabbed pane to a buffer
             Image totalImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
             Graphics totalGraphics = totalImage.getGraphics();
             totalGraphics.setClip(bounds);
             
             // Don't be double buffered when painting to a static image.
             setDoubleBuffered(false);
             paintComponent(totalGraphics);
 
             // Paint just the dragged tab to the buffer
             tabImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
             Graphics graphics = tabImage.getGraphics();
             graphics.drawImage(totalImage, 0, 0, bounds.width, bounds.height, bounds.x, bounds.y, bounds.x+bounds.width, bounds.y+bounds.height, DraggableTabbedPane.this);
 
             mouseRelX = e.getX()-bounds.x;
             mouseRelY = bounds.y;
             
             dragging = true;
             repaint();
           }
         } else {
         	int X = (int)e.getPoint().getX()-mouseRelX;
         	int Y = mouseRelY;
         	currentMouseLocation = new Point(X,Y);
 
         	if(getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), 10) != draggedTabIndex){
         		int returnValue = insertIt(e);
         		if(returnValue!=-1)
         			draggedTabIndex = returnValue;
         	}
 
         	// Need to repaint
         	repaint();
         }
 
         super.mouseDragged(e);
       }
     });
 
     /**
      *  Beim Mauswieder loslassen wird nun (falls gedragged wird) alles Nötige zum Tab gespeichert
      *  Dazu gehören die Componente, der Titel und das Icon.
      *  Außerdem wird der SelectedIndex der TabbedPane(also der ausgewählte Tab)
      *  auf den neuen Index gesetzt (damit der gedraggte Tab im Vordergrund ist,
      *  wie man es von modernen Browsern ebenfalls gewöhnt ist)
      */
     addMouseListener(new MouseAdapter() {
       public void mouseReleased(MouseEvent e) {
     	
         if(dragging) {
           int tabNumber = getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), 10);
           if(tabNumber >= 0)
         	  insertIt(e);
 
 
         }
 
         dragging = false;
         tabImage = null;
       }
     });
   }
 
   private int insertIt(MouseEvent e){
       int tabNumber = getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), 10);
 
       if(tabNumber >= 0 && tabNumber != getTabCount()-1 && draggedTabIndex != getTabCount()-1) {
         Component comp = getComponentAt(draggedTabIndex);
   	  	Component buttonTabComp = getTabComponentAt(draggedTabIndex);
         String title = getTitleAt(draggedTabIndex);
         removeTabAt(draggedTabIndex);
         
         insertTab(title, null, comp, null, tabNumber);
   	  	setTabComponentAt(tabNumber, buttonTabComp);
   	  	setSelectedIndex(tabNumber);
         return tabNumber;
       }  
       return -1;
   }
 
 
   /**
    * Diese Methode dient dazu das Bild des Tabs zu zeichnen der derzeit gedraggt wird.
    * Sie wird in der mouseDragged (s.O.) Methode verwendet
    */
   protected void paintComponent(Graphics g) {
     super.paintComponent(g);
 
     // Are we dragging?
     if(dragging && currentMouseLocation != null && tabImage != null) {
       // Draw the dragged tab
       g.drawImage(tabImage, currentMouseLocation.x, currentMouseLocation.y, this);
     }
   }
 }
