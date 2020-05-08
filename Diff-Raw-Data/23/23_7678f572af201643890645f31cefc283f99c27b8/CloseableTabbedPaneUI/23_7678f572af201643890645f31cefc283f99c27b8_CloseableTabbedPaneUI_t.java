 package org.tonic.ui.swing;
 /*
  * CloseableTabbedPaneUI.java
  *
  * Created on 14 lipiec 2006, 13:41
  *
  * This class is responsible for creating tab 
  * with a icon which when clicked closes this tab.
  */
 
 /**
  *
  *
  */
 
import javax.swing.*;
 import javax.swing.plaf.metal.MetalTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
 
 public class CloseableTabbedPaneUI extends MetalTabbedPaneUI {
     
     public final static int BUTTON_SIZE = 10;
     public final static int BORDER_SIZE = 2;
 
     
     public CloseableTabbedPaneUI() {
         super();
     }
     
 
     
    @Override
     protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        
         Graphics2D g2d = (Graphics2D)g.create();
        super.paintTab(g2d,tabPlacement,rects,tabIndex,iconRect,textRect);
         int xPosition;
         int yPosition;
         
         
         Rectangle rect = this.getTabBounds(this.tabPane, tabIndex);
         
         
         
         yPosition = (((rect.height)-BUTTON_SIZE)/2)+ rect.y;
         xPosition = rect.x+rect.width-Math.round(BUTTON_SIZE*1.5f);
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         //g2d.setColor(Color.red);
         //g2d.fillOval(xPosition, yPosition, BUTTON_SIZE +1, BUTTON_SIZE + 1);
         //g.setColor(Color.BLACK);
         //g.drawRect(xPosition,yPosition,BUTTON_SIZE,BUTTON_SIZE);
         g2d.setStroke(new BasicStroke(1f));
         g2d.setColor(UIManager.getDefaults().getColor("textText"));
         g2d.drawLine(xPosition+BORDER_SIZE,yPosition+BORDER_SIZE,xPosition+BUTTON_SIZE-BORDER_SIZE,yPosition-BORDER_SIZE+BUTTON_SIZE);
         g2d.drawLine(xPosition+BORDER_SIZE,yPosition-BORDER_SIZE+BUTTON_SIZE,xPosition+BUTTON_SIZE-BORDER_SIZE,yPosition+BORDER_SIZE);
         
         g2d.dispose();
     }
     protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
         return super.getTabLabelShiftX(tabPlacement, tabIndex, isSelected)-BUTTON_SIZE;
     }
     
     protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
         return super.calculateTabWidth(tabPlacement,tabIndex,metrics)+BUTTON_SIZE;
     }
     
     protected MouseListener createMouseListener() {
         return new MyMouseHandler();
     }
     
     class MyMouseHandler extends MouseHandler {
         public MyMouseHandler() {
             super();
         }
         private void maybeShowPopup(MouseEvent e) 
         {			
 //             if (e.isPopupTrigger())
 //
 //             {
 //                 popup.show(e.getComponent(),e.getX(), e.getY());
 //                 System.out.println("X: "+e.getX()+"  Y: "+e.getY());
 //             }
          }
 
         
         public void mouseClicked(MouseEvent e) {
            int x=e.getX();
            int y=e.getY();
            int tabIndex=-1;
            int tabCount = tabPane.getTabCount(); 
            int xPosition;
            int yPosition;
             if (e.getButton() == MouseEvent.BUTTON1){
                 
                 
                 for (int i = 0; i < tabCount; i++) {
                     if (rects[ i ].contains(x, y)) {
                         tabIndex= i;
                         break;
                     }
                 }
                 if (tabIndex >= 0) {
                     Rectangle rect = rects[tabIndex];
                     
                      yPosition = (rect.height-BUTTON_SIZE)/2 + rect.y;
                      xPosition = rect.x+rect.width-Math.round(BUTTON_SIZE*1.5f);
                     
                     if( new Rectangle(xPosition,yPosition,BUTTON_SIZE,BUTTON_SIZE).contains(x,y)) {
                         if( tabPane instanceof CloseableTabbedPane ) {
                             CloseableTabbedPane closableTabbedPane = (CloseableTabbedPane)tabPane;
                             CloseTabAction closeTabAction = closableTabbedPane.getCloseTabAction();
                             if(closeTabAction != null) {
                                 closeTabAction.act(closableTabbedPane, tabIndex);
                             }
                         } else {
 // If somebody use this class as UI like setUI(new CloseableTabbedPaneUI())
                             tabPane.removeTabAt(tabIndex);
                         }
                     }
                 }
             }else {}
             /*else if (e.getButton() == MouseEvent.BUTTON3){
                 if (tabPane instanceof CloseableTabbedPane){
                     CloseableTabbedPane closeableTabbedPane = (CloseableTabbedPane)tabPane;
                     for (int i = 0; i < tabCount; i++) {
                         if (rects[ i ].contains(x, y)) {
                             tabIndex= i;
                             break;
                         }
                     }
                     
                     JPopupMenu popup = closeableTabbedPane.getPopup();
                         if (popup != null && tabIndex != -1 && tabPane.getBoundsAt(tabIndex).contains(x,y)){
                             
                                 popup.show(e.getComponent(),e.getX(), e.getY());
                                 
                         }
                     }
                 
                 //maybeShowPopup(e);
             }*/
         }
     }
 }
 
 
