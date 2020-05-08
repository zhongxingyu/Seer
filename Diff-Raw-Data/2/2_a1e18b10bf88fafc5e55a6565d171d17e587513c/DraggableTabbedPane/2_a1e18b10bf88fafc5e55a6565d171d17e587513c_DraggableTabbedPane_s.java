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
 import java.util.HashMap;
 import java.util.Map;
 
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
 	  private boolean draggingPlus = false;
 	  private Image tabImage = null;
 	  private Point currentMouseLocation = null;
 	  private int draggedTabIndex = 0;
 	  private int mouseRelX;
 	  private int mouseRelY;
 	  private Rectangle bounds;
 	  private FrameMain frame;
 
   /**
    *  Im Konstruktor wird ein neuen MouseMotionListener angelegt, welcher schaut ob
    *  ich, wenn ich mit der Maus klicke(mouseDragged) über einem tab bin.
    *  Wenn Ja wird ein Bild des "gedragten" Tabs in den Buffer gezeichnet.
    *  
    *  @param parentFrame Referenz auf GUI-Frame
    */
   public DraggableTabbedPane(final FrameMain parentFrame) {
     super();
     frame = parentFrame;
     
     addMouseMotionListener(new MouseMotionAdapter() {
       public void mouseDragged(MouseEvent e) {
 
         if(!dragging && !draggingPlus) {
           // Gets the tab index based on the mouse position
           int tabNumber = getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), e.getY());
           
           //TabCount-1 is the Plus Tab therefore set an extra flag and not dragging
           if(tabNumber==getTabCount()-1){
         	  draggingPlus = true;
           }else if(tabNumber >= 0 ) {
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
         
         if(draggingPlus) draggingPlus = false;
 
         dragging = false;
         tabImage = null;
       }
     });
   }
 
   private int insertIt(MouseEvent e){
       int tabNumber = getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), 10);
 
       if(tabNumber >= 0 && tabNumber != getTabCount()-1 && draggedTabIndex != getTabCount()-1  && !draggingPlus) {
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
   
   public void openTab(String command){
 		Map<String, Integer> openTabs = new HashMap<String, Integer>();
 		
 		int openTabsCount = getTabCount();
 		
 		for(int i =0; i < openTabsCount; i++)
 			openTabs.put(getTitleAt(i),i);
 		
 		if(command.equals("open_layer3_r")){
 			//Prüfen ob Tab bereits geöffnet ist
 			if(openTabs.containsKey(" L3 Receiver "))
 				//Wenn ja holen wir uns die ID und focusieren(öffnen) ihn
 				setSelectedIndex(openTabs.get(" L3 Receiver "));
 			else{
 				insertTab(" L3 Receiver ", null, frame.getPanel_rec_lay3(), null, openTabsCount-1);
 				setTabComponentAt(openTabsCount-1, new ButtonTabComponent(this, "/zisko/multicastor/resources/images/ipv6receiver.png"));
 				setSelectedIndex(openTabsCount-1);
 			}
 			frame.getMi_open_l3r().setSelected(true);
 		}else if(command.equals("open_layer3_s")){
 			if(openTabs.containsKey(" L3 Sender "))
 				setSelectedIndex(openTabs.get(" L3 Sender "));
 			else{
 				insertTab(" L3 Sender ", null, frame.getPanel_sen_lay3(), null, openTabsCount-1);
 				setTabComponentAt(openTabsCount-1, new ButtonTabComponent(this, "/zisko/multicastor/resources/images/ipv6sender.png"));
 				setSelectedIndex(openTabsCount-1);
 			}
 			frame.getMi_open_l3s().setSelected(true);
 		}else if(command.equals("open_layer2_s")){
 			if(openTabs.containsKey(" L2 Sender "))
 				setSelectedIndex(openTabs.get(" L2 Sender "));
 			else{
 				insertTab(" L2 Sender ", null, frame.getPanel_sen_lay2(), null, openTabsCount-1);
 				setTabComponentAt(openTabsCount-1, new ButtonTabComponent(this, "/zisko/multicastor/resources/images/ipv4sender.png"));
 				setSelectedIndex(openTabsCount-1);
 			}
 			frame.getMi_open_l2s().setSelected(true);
 		}else if(command.equals("open_layer2_r")){
 			if(openTabs.containsKey(" L2 Receiver "))
 				setSelectedIndex(openTabs.get(" L2 Receiver "));
 			else{
				insertTab(" L2 Receiver ", null, frame.getPanel_sen_lay2(), null, openTabsCount-1);
 				setTabComponentAt(openTabsCount-1, new ButtonTabComponent(this, "/zisko/multicastor/resources/images/ipv4receiver.png"));
 				setSelectedIndex(openTabsCount-1);
 			}
 			frame.getMi_open_l2r().setSelected(true);
 		}else if(command.equals("open_about")){
 			if(openTabs.containsKey(" About "))
 				setSelectedIndex(openTabs.get(" About "));
 			else{
 				insertTab(" About ", null, frame.getPanel_about(), null, openTabsCount-1);
 				setTabComponentAt(openTabsCount-1, new ButtonTabComponent(this, "/zisko/multicastor/resources/images/about.png"));
 				setSelectedIndex(openTabsCount-1);
 			}
 			frame.getMi_open_about().setSelected(true);
 		}  
   }
   
   public void closeTab(String command){
 	  if(command.equals(" L3 Receiver ")){
 			frame.getMi_open_l3r().setSelected(false);
 	  }else if(command.equals(" L3 Sender ")){
 			frame.getMi_open_l3s().setSelected(false);
 	  }else if(command.equals(" L2 Receiver ")){
 			frame.getMi_open_l2r().setSelected(false);
 	  }else if(command.equals(" L2 Sender ")){
 			frame.getMi_open_l2s().setSelected(false);
 	  }else if(command.equals(" About "))
 		  	frame.getMi_open_about().setSelected(false);
   }
 
 }
