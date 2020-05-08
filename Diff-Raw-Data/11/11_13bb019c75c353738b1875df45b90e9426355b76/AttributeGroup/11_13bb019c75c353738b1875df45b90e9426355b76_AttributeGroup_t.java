 /**
  * 
  */
 package org.cotrix.web.manage.shared;
 
 import java.util.List;
 
 import org.cotrix.web.common.client.util.ValueUtils;
 import org.cotrix.web.common.shared.codelist.UIAttribute;
 import org.cotrix.web.common.shared.codelist.UICode;
 import org.cotrix.web.common.shared.codelist.UIQName;
 
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class AttributeGroup implements Comparable<AttributeGroup>, Group, HasPosition {
 	
 	private UIQName name;
 	private UIQName type;
 	private String language;
 	private int position;
 	
 	private boolean isSystemGroup;
 	
 	private SafeHtml label;
 	
 	protected AttributeGroup() {
 	}
 		
 	/**
 	 * @param name
 	 * @param type
 	 * @param language
 	 */
 	public AttributeGroup(UIQName name, UIQName type, String language, boolean isSystemGroup) {
 		this.name = name!=null?name.clone():null;
 		this.type = type!=null?type.clone():null;
 		this.language = language;
 		this.isSystemGroup = isSystemGroup;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public UIQName getName() {
 		return name;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public UIQName getType() {
 		return type;
 	}
 
 	/**
 	 * @return the language
 	 */
 	public String getLanguage() {
 		return language;
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isSystemGroup() {
 		return isSystemGroup;
 	}
 	
 	public void calculatePosition(List<UIAttribute> attributes, UIAttribute attribute)
 	{
 		int position = getPosition(attributes, attribute);
 		setPosition(position);
 	}
 	
 	public void setPosition(int position)
 	{
 		this.position = position;
 	}
 	
 
 	@Override
 	public int getPosition() {
 		return position;
 	}
 
 	private int getPosition(List<UIAttribute> attributes, UIAttribute att)
 	{
 		int index = 0;
 		for (UIAttribute attribute:attributes) {
 			if (accept(attribute)) {
 				if (att.equals(attribute)) return index;
 				index++;
 			}
 		}
 		return -1;
 	}
 	
 	public boolean accept(List<UIAttribute> attributes, UIAttribute att)
 	{
 		int index = 0;
 		for (UIAttribute attribute:attributes) {
 			if (accept(attribute)) {
 				if (att.equals(attribute) && index == position) return true;
 				index++;
 			}
 			if (index>position) return false;
 		}
 		return false;
 	}
 	
 	public UIAttribute match(List<UIAttribute> attributes)
 	{
 		int index = 0;
 		for (UIAttribute attribute:attributes) {
 			if (accept(attribute)) {
 				if (index == position) return attribute;
 				index++;
 			}
 		}
 		return null;
 	}
 	
 	private boolean accept(UIAttribute attribute)
 	{
 		if (name!=null && !name.equals(attribute.getName())) return false;
 		if (type!=null && !type.equals(attribute.getType())) return false;
 		if (language!=null && !language.equals(attribute.getLanguage())) return false;
 		return true;
 	}
 	
 	public AttributeGroup cloneGroup()
 	{
 		AttributeGroup clone = new AttributeGroup(name, type, language, isSystemGroup);
 		clone.setPosition(position);
 		return clone;
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public CodelistEditorSortInfo getSortInfo(boolean ascending) {
 		return new CodelistEditorSortInfo.AttributeGroupSortInfo(ascending, name, type, language, position);
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public SafeHtml getLabel() {
 		if (label == null) buildLabel();
 		return label;
 	}
 	
	@Override
	public boolean isEditable() {
		return !isSystemGroup;
	}
	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getValue(UICode code)
 	{
 		List<UIAttribute> attributes = code.getAttributes();
 		UIAttribute attribute = match(attributes);
 		return attribute!=null?attribute.getValue():"";
 	}
 	
 	private void buildLabel() {
 		SafeHtmlBuilder labelBuilder = new SafeHtmlBuilder();
 		labelBuilder.appendHtmlConstant("<span style=\"vertical-align:middle;padding-right: 7px;\">");
 		SafeHtml nameHtml = SafeHtmlUtils.fromString(ValueUtils.getValue(name));
 		labelBuilder.append(nameHtml);
 		labelBuilder.appendHtmlConstant("</span>");
 		if (language!=null && !language.isEmpty()) {
 			labelBuilder.appendHtmlConstant("<span style=\"vertical-align:middle;color:black;padding-left:5px;\">");
 			SafeHtml languageHtml = SafeHtmlUtils.fromString(language);
 			labelBuilder.append(languageHtml);
 			labelBuilder.appendHtmlConstant("</span>");
 		}
 		
 		label = labelBuilder.toSafeHtml();
 	}
 
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (isSystemGroup ? 1231 : 1237);
 		result = prime * result
 				+ ((language == null) ? 0 : language.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + position;
 		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
 		AttributeGroup other = (AttributeGroup) obj;
 		if (isSystemGroup != other.isSystemGroup)
 			return false;
 		if (language == null) {
 			if (other.language != null)
 				return false;
 		} else if (!language.equals(other.language))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (position != other.position)
 			return false;
 		if (type == null) {
 			if (other.type != null)
 				return false;
 		} else if (!type.equals(other.type))
 			return false;
 		return true;
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("AttributeGroup [name=");
 		builder.append(name);
 		builder.append(", type=");
 		builder.append(type);
 		builder.append(", language=");
 		builder.append(language);
 		builder.append(", position=");
 		builder.append(position);
 		builder.append(", isSystemGroup=");
 		builder.append(isSystemGroup);
 		builder.append("]");
 		return builder.toString();
 	}
 
 	@Override
 	public int compareTo(AttributeGroup o) {
 		int compare = (name !=null)?name.compareTo(o.name):1;
 		if (compare!=0) return compare;
 		
 		compare = (type !=null)?type.compareTo(o.type):1;
 		if (compare!=0) return compare;
 		
 		compare = (language !=null)?language.compareTo(o.language):1;
 		if (compare!=0) return compare;
 		
 		compare = position > o.position ? +1 : position < o.position ? -1 : 0;
 		if (compare!=0) return compare;
 		
 		return isSystemGroup && !o.isSystemGroup ? +1 : !isSystemGroup && o.isSystemGroup ? -1 : 0;
 	}
 
 }
