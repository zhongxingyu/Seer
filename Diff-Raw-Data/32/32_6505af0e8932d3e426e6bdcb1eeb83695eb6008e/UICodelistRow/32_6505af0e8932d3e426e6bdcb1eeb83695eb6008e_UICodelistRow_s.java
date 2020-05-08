 /**
  * 
  */
 package org.cotrix.web.codelistmanager.shared;
 
 import java.util.Collection;
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
	protected Map<String, UIAttribute> attributes;
 
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
		return attributes.values();
 	}
 	
 	public void updateAttribute(String oldName, UIAttribute attribute)
 	{
		attributes.remove(oldName);
		attributes.put(attribute.getName(), attribute);
 	}
 	
 	public UIAttribute getAttribute(String name)
 	{
		return attributes.get(name);
 	}
 	
 	public Set<String> getAttributesNames()
 	{
		return attributes.keySet();
 	}
 
 	/**
 	 * @param attributes the attributes to set
 	 */
 	public void setAttributes(Map<String, UIAttribute> attributes) {
		this.attributes = attributes;
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
