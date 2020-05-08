 package org.jboss.jbossesb.eclipse.plugin.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.draw2d.geometry.Rectangle;
 
 /**
  * <h3>Class represents a XML Element</h3>
  * <ul>
  * <li><i>address</i> - an unique path to the element (enum of xml elements from the root, / - used as a separator)</li>
  * <li><i>name</i> - text representation of the element for displaying in GUI</li>
  * <li><i>hint</i> - complex description of the element for displaying in GUI</li>
  * <li><i>attributes</i> - a list of attributes attach to the element</li>
  * <li><i>children</i> - a list of children XML elements attach to the element</li>
  * <li><i>rectangle</i> - position of element in GUI</li>
  * </ul>
  * 
  * @author Tomas Sedmik, tomas.sedmik@gmail.com
  * @since 2012-11-04
  */
 public class XMLElement {
 
 	private String address;
 	private String name;
 	private String hint;
 	private List<XMLAttribute> attributes;
 	private List<XMLElement> children;
 	private Rectangle rectangle;
 	private PropertyChangeSupport listeners;
 	
 	public void addPropertyChangeListener(PropertyChangeListener listener){
 		listeners.addPropertyChangeListener(listener);
 	}
 	
 	public void removePropertyChangeListener(PropertyChangeListener listener){
 		listeners.removePropertyChangeListener(listener);
 	}
 
 	public XMLElement(String address, String name, String hint,
 			List<XMLAttribute> attributes) {
 		super();
 		listeners = new PropertyChangeSupport(this);
 		this.address = address;
 		this.name = name;
 		this.hint = hint;
 		this.attributes = attributes;
 	}
 	
 	public XMLElement() {
 		super();
 		listeners = new PropertyChangeSupport(this);
 		attributes = new ArrayList<XMLAttribute>();
 		children = new ArrayList<XMLElement>();
 	}
 
 	public void addChildElement(XMLElement element) {
 		children.add(element);
 	}
 	
 	public void addAttribute(XMLAttribute attr) {
 		attributes.add(attr);
 	}
 
 	public List<XMLElement> getChildren() {
 		return children;
 	}
 
 	public void setChildren(List<XMLElement> children) {
 		this.children = children;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 	
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getHint() {
 		return hint;
 	}
 	
 	public void setHint(String hint) {
 		this.hint = hint;
 	}
 	
 	public List<XMLAttribute> getAttributes() {
 		return attributes;
 	}
 	
 	public XMLAttribute getAttribute(String xmlName) {
 		
 		if (attributes == null) {
 			return null;
 		}
 		
 		for (XMLAttribute attr : attributes) {
 			if (attr.getXmlName().equals(xmlName)) {
 				return attr;
 			}
 		}
 		
 		return null;
 	}
 	
 	public void setAttributes(List<XMLAttribute> attributes) {
 		this.attributes = attributes;
 	}
 
 	public Rectangle getRectangle() {
 		return rectangle;
 	}
 
 	public void setRectangle(Rectangle rectangle) {
 		this.rectangle = rectangle;
 		listeners.firePropertyChange("move", null, rectangle);
 	}
 	
 	public boolean removeChild(XMLElement child) {
 		return children.remove(child);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((address == null) ? 0 : address.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		XMLElement other = (XMLElement) obj;
 		if (address == null) {
 			if (other.address != null)
 				return false;
 		} else if (!address.equals(other.address))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "XMLElement [address=" + address + ", name=" + name + "]";
 	}
 
 }
