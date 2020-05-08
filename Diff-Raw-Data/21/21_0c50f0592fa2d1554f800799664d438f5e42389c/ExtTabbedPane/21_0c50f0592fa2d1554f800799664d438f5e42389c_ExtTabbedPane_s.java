 /*
  * ExtTabbedPane.java
  *
  * Created on July 20, 2011, 10:04 PM
  */
 
 package com.rameses.rcp.impl;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.Hashtable;
 import java.util.Map;
 import javax.swing.Icon;
 import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 /**
  *
  * @author jaycverg
  */
 public class ExtTabbedPane extends JTabbedPane {
     
     private Map<String,Component> tabIndex;
     private Rectangle closeIconBounds;
     private boolean closeIconHover;
     
     
     public ExtTabbedPane() {
         tabIndex = new Hashtable();
         closeIconBounds = new Rectangle(0,0,10,10);
         setFocusable(false);
         
         TabSupport support = new TabSupport();
         addMouseListener(support);
         addMouseMotionListener(support);
     }
     
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         
         Component comp = getSelectedComponent();
         if( comp instanceof PlatformTabWindow && !((PlatformTabWindow)comp).isCanClose() ) return;
         
         int idx = getSelectedIndex();
         if( idx < 0 ) return;
         Rectangle rec = getBoundsAt( idx );
         
         Graphics2D g2 = (Graphics2D) g.create();
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         int w = closeIconBounds.width;
         int h = closeIconBounds.height;
         int x = rec.x + rec.width - w - 8;
         int y = rec.y + (rec.height/2) - (h/2);
         
         g2.setColor(closeIconHover? Color.RED : Color.BLACK);
         g2.drawLine(x+2, y+2, x+w-3, y+h-3);
         g2.drawLine(x+w-3, y+2, x+2, y+h-3);
         
         closeIconBounds.x = x;
         closeIconBounds.y = y;
         
         g2.setColor(closeIconHover? UIManager.getColor("Separator.shadow") : UIManager.getColor("control"));
         Rectangle cib = closeIconBounds;
         g2.drawRoundRect(cib.x, cib.y, cib.width-1, cib.height-1, 3,3);
         g2.dispose();
     }
     
     public void insertTab(String title, Icon icon, Component component, String tip, int index) {
         final String _title = title + "          ";
         final Icon _icon = icon;
         final Component _component = component;
         final String _tip = tip;
         final int _index = index;
         
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                Component old = tabIndex.get(_title);
                 if( old != null ) {
                     if( indexOfComponent(old) >= 0 ) {
                         setSelectedComponent(old);
                         return;
                     }
                 }
                 
                 ExtTabbedPane.super.insertTab(_title, _icon, _component, _tip, _index);
                 setSelectedIndex(_index);
                tabIndex.put(_title, _component);
             }
         });
     }
     
     public void remove(Component component) {
         int idx = indexOfComponent(component);
         if( idx >= 0 ) {
             String title = getTitleAt(idx);
             tabIndex.remove(title);
         }
         super.remove(component);
     }
     
    
     private class TabSupport implements MouseListener, MouseMotionListener {
         
         public void mouseClicked(MouseEvent e) {
             if( closeIconBounds.contains(e.getPoint()) ) {
                 Component comp = getSelectedComponent();
                 if( comp instanceof PlatformTabWindow ) {
                     ((PlatformTabWindow)comp).close();
                     closeIconHover = false;
                 }
             }
         }
         
         public void mouseExited(MouseEvent e) {
             closeIconHover = false;
         }
         
         public void mouseMoved(MouseEvent e) {
             if( closeIconBounds.contains(e.getPoint()) ) {
                 closeIconHover = true;
             } else {
                 closeIconHover = false;
             }
         }
         
         public void mousePressed(MouseEvent e) {}
         public void mouseReleased(MouseEvent e) {}
         public void mouseEntered(MouseEvent e) {}
         public void mouseDragged(MouseEvent e) {}
         
     }
     
 }
