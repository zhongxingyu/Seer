 /**
  * 
  */
 package org.cotrix.web.codelistmanager.shared;
 
import java.util.ArrayList;
 import java.util.Collection;
import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.cotrix.web.share.shared.UIAttribute;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class UICodelistRow implements IsSerializable {
 	
 	protected String id;
 	protected String code;
 	protected String name;
	protected Map<String, UIAttribute> index;
	protected List<UIAttribute> attributes;
 
 	protected UICodelistRow(){}
 	
 	/**
 	 * @param id
 	 * @param code
 	 * @param name
 	 */
 	public UICodelistRow(String id, String code, String name) {
 		this.id = id;
 		this.code = code;
 		this.name = name;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 	
 	public String getCode()
 	{
 		return code;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * @param code the code to set
 	 */
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Collection<UIAttribute> getAttributes()
 	{
		return attributes;
 	}
 	
 	public void updateAttribute(String oldName, UIAttribute attribute)
 	{
		//get old attribute position
		UIAttribute oldAttribute = index.get(oldName);
		int oldAttributeIndex = attributes.indexOf(oldAttribute);
		
		//replace old attribute with new one
		attributes.set(oldAttributeIndex, attribute);
		//update index removing old one (the name can be changed) and putting the new one
		index.remove(oldName);
		index.put(attribute.getName(), attribute);
 	}
 	
 	public UIAttribute getAttribute(String name)
 	{
		return index.get(name);
 	}
 	
 	public Set<String> getAttributesNames()
 	{
		return index.keySet();
 	}
 
 	/**
 	 * @param attributes the attributes to set
 	 */
 	public void setAttributes(Map<String, UIAttribute> attributes) {
		this.index = attributes;
		this.attributes = new ArrayList<UIAttribute>(attributes.values());
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		UICodelistRow other = (UICodelistRow) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("UICodeListRow [id=");
 		builder.append(id);
 		builder.append(", code=");
 		builder.append(code);
 		builder.append(", name=");
 		builder.append(name);
 		builder.append(", attributes=");
 		builder.append(attributes);
 		builder.append("]");
 		return builder.toString();
 	}
 	
 	
 }
