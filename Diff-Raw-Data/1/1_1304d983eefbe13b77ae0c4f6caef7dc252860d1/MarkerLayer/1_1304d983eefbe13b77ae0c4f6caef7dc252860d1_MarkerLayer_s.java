 /**
  * 
  */
 package org.vaadin.vol;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.ui.AbstractComponentContainer;
 import com.vaadin.ui.ClientWidget;
 import com.vaadin.ui.Component;
 
 @ClientWidget(org.vaadin.vol.client.ui.VMarkerLayer.class)
 public class MarkerLayer extends AbstractComponentContainer implements Layer {
 
     private List<Marker> markers = new LinkedList<Marker>();
 
     private String displayName = "Markers";
 
     public void addMarker(Marker m) {
         addComponent(m);
     }
 
     @Override
     public void paintContent(PaintTarget target) throws PaintException {
         target.addAttribute("name", getDisplayName());
         for (Marker m : markers) {
             m.paint(target);
         }
     }
 
     public void replaceComponent(Component oldComponent, Component newComponent) {
         throw new UnsupportedOperationException();
     }
 
     public Iterator<Component> getComponentIterator() {
         LinkedList<Component> list = new LinkedList<Component>(markers);
         return list.iterator();
     }
 
     @Override
     public void addComponent(Component c) {
         if (c instanceof Marker) {
             markers.add((Marker) c);
             super.addComponent(c);
         } else {
             throw new IllegalArgumentException(
                     "MarkerLayer supports only markers");
         }
     }
 
     @Override
     public void removeComponent(Component c) {
         super.removeComponent(c);
         markers.remove(c);
     }
 
     public void setDisplayName(String displayName) {
         this.displayName = displayName;
     }
 
     public String getDisplayName() {
         return displayName;
     }
 }
