 package phyml.gui.model;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Maps;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import phyml.gui.control.NodeController;
 
 import javax.swing.*;
 import java.beans.PropertyChangeEvent;
 import java.util.Map;
 
 /**
  * An AbstractNode object can contain {@link AbstractProperty}s, as well as connections to other AbstractNodes.
  * <p/>
  * <p/>
  * Written by: Markus Binsteiner
  * Date: 2/10/13
  * Time: 4:37 PM
  */
 public class Node {
 
     protected static final Logger myLogger = LoggerFactory.getLogger(Node.class);
     protected final String name;
     protected final String id;
     final private Map<String, AbstractProperty> properties = Maps.newLinkedHashMap();
     private NodeController controller;
     private int layoutGroups = BoxLayout.Y_AXIS;
     private int layoutProperties = BoxLayout.Y_AXIS;
 
     public Node(String id) {
         this(id, id);
     }
 
     public Node(String id, String name) {
         this.id = id;
         this.name = name;
     }
 
     public int getLayoutGroups() {
         return layoutGroups;
     }
 
     public void setLayoutGroups(int layoutGroups) {
         this.layoutGroups = layoutGroups;
     }
 
     public int getLayoutProperties() {
         return layoutProperties;
     }
 
     public void setLayoutProperties(int layoutProperties) {
         this.layoutProperties = layoutProperties;
     }
 
     public String getName() {
         return name;
     }
 
     public String getId() {
         return id;
     }
 
     public int hashCode() {
         return Objects.hashCode(getId());
     }
 
     public boolean equals(Object obj) {
 
         if (obj == this) return true;
         if (obj == null) return false;
 
         if (obj instanceof AbstractProperty) {
             final Node other = (Node) obj;
             return Objects.equal(getId(), other.getId());
         }
 
         return false;
     }
 
     public void addProperty(AbstractProperty prop) {
        properties.put(prop.getLabel(), prop);
     }
 
     public AbstractProperty getProperty(String label) {
         return properties.get(label);
     }
 
     public Map<String, AbstractProperty> getProperties() {
         return properties;
     }
 
     public void valueChanged(PropertyChangeEvent evt) {
         AbstractProperty property = (AbstractProperty) evt.getSource();
         controller.nodeValueChanged(property.getParentNode(), property, evt);
     }
 
     public String toString() {
         return getName();
     }
 
     protected NodeController getController() {
         return controller;
     }
 
     public void setController(NodeController c) {
         this.controller = c;
     }
 }
