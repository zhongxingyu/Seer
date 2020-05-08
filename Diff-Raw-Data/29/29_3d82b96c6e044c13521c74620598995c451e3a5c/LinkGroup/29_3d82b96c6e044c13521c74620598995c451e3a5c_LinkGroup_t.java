 /**
  * 
  */
 package org.cotrix.web.manage.shared;
 
 import java.util.List;
 
 import org.cotrix.web.common.client.util.ValueUtils;
 import org.cotrix.web.common.shared.codelist.UICode;
 import org.cotrix.web.common.shared.codelist.UILink;
 import org.cotrix.web.common.shared.codelist.UIQName;
 
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class LinkGroup implements Comparable<LinkGroup>, Group, HasPosition {
 	
 	private UIQName name;
 	private int position;
 	
 	private boolean isSystemGroup;
 	
 	private SafeHtml label;
 	
 	protected LinkGroup() {
 	}
 		
 	/**
 	 * @param name
 	 */
 	public LinkGroup(UIQName name, boolean isSystemGroup) {
 		this.name = name!=null?name.clone():null;
 		this.isSystemGroup = isSystemGroup;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public UIQName getName() {
 		return name;
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isSystemGroup() {
 		return isSystemGroup;
 	}
 	
 	public void calculatePosition(List<UILink> links, UILink link)
 	{
 		int position = getPosition(links, link);
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
 
 	private int getPosition(List<UILink> links, UILink otherLink)
 	{
 		int index = 0;
 		for (UILink link:links) {
 			if (accept(link)) {
 				if (otherLink.equals(link)) return index;
 				index++;
 			}
 		}
 		return -1;
 	}
 	
 	public boolean accept(List<UILink> links, UILink otherLink)
 	{
 		int index = 0;
 		for (UILink link:links) {
 			if (accept(link)) {
 				if (otherLink.equals(link) && index == position) return true;
 				index++;
 			}
 			if (index>position) return false;
 		}
 		return false;
 	}
 	
 	public UILink match(List<UILink> links)
 	{
 		int index = 0;
 		for (UILink link:links) {
 			if (accept(link)) {
 				if (index == position) return link;
 				index++;
 			}
 		}
 		return null;
 	}
 	
 	private boolean accept(UILink link)
 	{
 		if (name!=null && !name.equals(link.getTypeName())) return false;
 		return true;
 	}
 	
	public LinkGroup cloneGroup()
 	{
 		LinkGroup clone = new LinkGroup(name, isSystemGroup);
 		clone.setPosition(position);
 		return clone;
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public CodelistEditorSortInfo getSortInfo(boolean ascending) {
		return new CodelistEditorSortInfo.CodeNameSortInfo(ascending);
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public SafeHtml getLabel() {
 		if (label == null) buildLabel();
 		return label;
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getValue(UICode code)
 	{
 		List<UILink> links = code.getLinks();
 		UILink link = match(links);
 		return link!=null?link.getValue():"";
 	}
 	
 	private void buildLabel() {
 		SafeHtmlBuilder labelBuilder = new SafeHtmlBuilder();
 		labelBuilder.appendHtmlConstant("<span style=\"vertical-align:middle;padding-right: 7px;\">");
 		SafeHtml nameHtml = SafeHtmlUtils.fromString(ValueUtils.getValue(name));
 		labelBuilder.append(nameHtml);
 		labelBuilder.appendHtmlConstant("</span>");
 		
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
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + position;
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
 		LinkGroup other = (LinkGroup) obj;
 		if (isSystemGroup != other.isSystemGroup)
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (position != other.position)
 			return false;
 		return true;
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("LinkGroup [name=");
 		builder.append(name);
 		builder.append(", type=");
 		builder.append(", position=");
 		builder.append(position);
 		builder.append(", isSystemGroup=");
 		builder.append(isSystemGroup);
 		builder.append("]");
 		return builder.toString();
 	}
 
 	@Override
 	public int compareTo(LinkGroup o) {
 		int compare = (name !=null)?name.compareTo(o.name):1;
 		if (compare!=0) return compare;
 		
 		compare = position > o.position ? +1 : position < o.position ? -1 : 0;
 		if (compare!=0) return compare;
 		
 		return isSystemGroup && !o.isSystemGroup ? +1 : !isSystemGroup && o.isSystemGroup ? -1 : 0;
 	}
 
 	@Override
 	public boolean isEditable() {
 		return false;
 	}
 
 }
