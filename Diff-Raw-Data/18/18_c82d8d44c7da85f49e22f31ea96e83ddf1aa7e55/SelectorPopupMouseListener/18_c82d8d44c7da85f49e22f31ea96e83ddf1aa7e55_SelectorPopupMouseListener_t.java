 package com.od.jtimeseries.ui.selector.shared;
 
 import com.od.jtimeseries.util.identifiable.Identifiable;
 
 import javax.swing.*;
import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 20/12/10
 * <p/>
 * To change this template use File | Settings | File Templates.
 */
 public abstract class SelectorPopupMouseListener extends MouseAdapter {
 
     private SelectorComponent selectorComponent;
 
     public SelectorPopupMouseListener(SelectorComponent selectorComponent) {
         this.selectorComponent = selectorComponent;
     }
 
     public void mousePressed(MouseEvent e) {
         showMenuIfPopupTrigger(e);
     }
 
     public void mouseClicked(MouseEvent e) {
         showMenuIfPopupTrigger(e);
 
     }
 
     public void mouseReleased(MouseEvent e) {
         showMenuIfPopupTrigger(e);
     }
 
     private void showMenuIfPopupTrigger(MouseEvent e) {
         if (e.isPopupTrigger()) {
             List<JMenuItem> menuItems = getMenuItems(e);
             if ( menuItems != null) {
                 JPopupMenu menu = new JPopupMenu();
                 for ( JMenuItem i : menuItems) {
                     menu.add(i);
                 }

                //fix for when component is in scroll pane viewport
                Point offset = new Point(0,0);
                if ( selectorComponent.getParent() instanceof JViewport) {
                    JViewport v = (JViewport)selectorComponent.getParent();
                    offset = v.getViewPosition();
                }
                menu.show(selectorComponent, e.getX() + 3 - offset.x, e.getY() + 3 - offset.y);
             }
         }
     }
 
     private List<JMenuItem> getMenuItems(MouseEvent e) {
         List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
         List<Identifiable> selectedIdentifiable = getSelectedIdentifiable(e);
         List<Action> actions = selectorComponent.getSelectorActionFactory().getActions(getSelectorComponent(), selectedIdentifiable);
         for ( Action a : actions) {
             menuItems.add(new JMenuItem(a));
         }
         return menuItems;
     }
 
     protected abstract List<Identifiable> getSelectedIdentifiable(MouseEvent mouseEvent);
 
     protected abstract SelectorComponent getSelectorComponent();
 }
