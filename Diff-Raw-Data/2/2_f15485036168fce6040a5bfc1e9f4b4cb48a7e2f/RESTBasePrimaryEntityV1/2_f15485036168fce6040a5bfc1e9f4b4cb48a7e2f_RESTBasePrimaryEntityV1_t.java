 package org.jboss.pressgang.ccms.rest.v1.entities.base;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionV1;
 
 public abstract class RESTBasePrimaryEntityV1<T extends RESTBasePrimaryEntityV1<T, U, V>, U extends RESTBaseCollectionV1<T, U, V>, V extends RESTBaseCollectionItemV1<T, U, V>> extends RESTBaseEntityV1<T, U, V>
 {
 	private String selfLink = null;
 	private String editLink = null;
 	private String deleteLink = null;
 	private String addLink = null;
 		
 	public void cloneInto(final RESTBasePrimaryEntityV1<?, ?, ?> clone, final boolean deepCopy)
 	{
 		super.cloneInto(clone, deepCopy);
 		
 		clone.setSelfLink(this.selfLink);
 		clone.setEditLink(this.editLink);
 		clone.setDeleteLink(this.deleteLink);
 		clone.setAddLink(this.addLink);
 	}
 
 	public void setLinks(final String baseUrl, final String restBasePath, final String dataType, final Object id)
 	{
 		this.setSelfLink(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "1/" + restBasePath + "/get/" + dataType + "/" + id);
 		this.setDeleteLink(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "1/" + restBasePath + "/delete/" + dataType + "/" + id);
 		this.setAddLink(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "1/" + restBasePath + "/create/" + dataType);
		this.setEditLink(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "1/" + restBasePath + "/update/" + dataType);
 	}
 	public String getSelfLink()
 	{
 		return selfLink;
 	}
 
 	public void setSelfLink(final String selfLink)
 	{
 		this.selfLink = selfLink;
 	}
 	
 	public String getEditLink()
 	{
 		return editLink;
 	}
 
 	public void setEditLink(final String editLink)
 	{
 		this.editLink = editLink;
 	}
 
 	public String getDeleteLink()
 	{
 		return deleteLink;
 	}
 
 	public void setDeleteLink(final String deleteLink)
 	{
 		this.deleteLink = deleteLink;
 	}
 
 	public String getAddLink()
 	{
 		return addLink;
 	}
 
 	public void setAddLink(final String addLink)
 	{
 		this.addLink = addLink;
 	}
 	
 	@Override
     public boolean equals(final Object other)
     {
 	    if (other == null)
             return false;
         if (this == other)
             return true;
         if (!(other instanceof RESTBasePrimaryEntityV1))
             return false;
         
         return super.equals(other);
     }
 }
