 package org.jboss.pressgangccms.rest.v1.entities.base;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgangccms.rest.v1.collections.base.BaseRestCollectionV1;
 
 
 public abstract class RESTBaseEntityV1<T extends RESTBaseEntityV1<T, U>, U extends BaseRestCollectionV1<T, U>>
 {
 	public static final String REVISIONS_NAME = "revisions";
 	public static final String LOG_DETAILS_NAME = "logDetails";
 	
 	/** The id of the entity */
 	private Integer id = null;
 	/** The revision of the entity */
 	private Integer revision = null;
 	/**
 	 * Maintains a list of the database fields that have been specifically set
 	 * on this object. This allows us to distinguish them from those that are
 	 * just null by default
 	 */
 	private List<String> configuredParameters = null;
 	private String selfLink = null;
 	private String editLink = null;
 	private String deleteLink = null;
 	private String addLink = null;
 	/** The names of collections that can be expanded */
 	private List<String> expand = null;
 	/** true if the database entity this REST entity represents should be added to the collection */ 
 	private boolean addItem = false;
 	/** true if the database entity this REST entity represents should be removed from the collection */
 	private boolean removeItem = false;
 	private RESTLogDetailsV1 logDetails = null;
 
 	abstract public U getRevisions();
 
 	abstract public void setRevisions(U revisions);
 	
 	/**
 	 * @return true if this entity's state would trigger a change in the database, and false otherwise
 	 */
	public boolean isDirty()
 	{
 		if (this.addItem) return true;
 		if (this.removeItem) return true;
 		if (this.configuredParameters != null && !this.configuredParameters.isEmpty()) return true;
 		return false;
 	}
 	
 	@Override
 	public boolean equals(final Object other)
 	{
 		if (!(other instanceof RESTBaseEntityV1))
 			return false;
 		
 		@SuppressWarnings("rawtypes")
 		final RESTBaseEntityV1 otherCasted = (RESTBaseEntityV1)other;
 		
 		if (this.id == null && otherCasted.id == null)
 			return true;
 		
 		if (this.id == null || otherCasted.id == null)
 			return false;
 		
 		return (this.id.equals(otherCasted.id));
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		if (this.id == null)
 			return 0;
 		return id.hashCode();
 	}
 		
 	public void cloneInto(final RESTBaseEntityV1<T, U> clone, final boolean deepCopy)
 	{
 		clone.setId(this.id == null ? null : new Integer(this.id));
 		clone.setRevision(this.revision);
 		clone.setSelfLink(this.selfLink);
 		clone.setEditLink(this.editLink);
 		clone.setDeleteLink(this.deleteLink);
 		clone.setAddItem(this.addItem);
 		clone.setExpand(this.expand);
 		clone.setAddItem(this.addItem);
 		clone.setRemoveItem(this.removeItem);
 		
 		if (deepCopy)
 		{
 		    if (this.logDetails != null)
 		    {
 		        clone.setLogDetails(this.logDetails.clone(deepCopy));
 		    }
 		}
 		else
 		{
 		    clone.setLogDetails(this.logDetails);
 		}
 	}
 	
 	/**
 	 * @param deepCopy true if referenced objects should be copied, false if the referenced themselves should be copied
 	 * @return A clone of this object
 	 */
 	public abstract T clone(final boolean deepCopy);
 	
 	/**
 	 * This is a convenience method that adds a value to the configuredParameters collection
 	 * @param parameter The parameter to specify as configured
 	 */
 	protected void setParameterToConfigured(final String parameter)
 	{
 		if (configuredParameters == null)
 			configuredParameters = new ArrayList<String>();
 		if (!configuredParameters.contains(parameter))
 			configuredParameters.add(parameter);
 	}
 	
 	public boolean hasParameterSet(final String parameter)
 	{
 		return getConfiguredParameters() != null && getConfiguredParameters().contains(parameter);
 	}
 
 	public void setLinks(final String baseUrl, final String restBasePath, final String dataType, final Object id)
 	{
 		this.setSelfLink(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "1/" + restBasePath + "/get/" + dataType + "/" + id);
 		this.setDeleteLink(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "1/" + restBasePath + "/delete/" + dataType + "/" + id);
 		this.setAddLink(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "1/" + restBasePath + "/post/" + dataType);
 		this.setEditLink(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "1/" + restBasePath + "/put/" + dataType + "/" + id);
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
 
 	public List<String> getExpand()
 	{
 		return expand;
 	}
 
 	public void setExpand(final List<String> expand)
 	{
 		this.expand = expand;
 	}
 
 	public boolean getAddItem()
 	{
 		return addItem;
 	}
 
 	public void setAddItem(final boolean addItem)
 	{
 		this.addItem = addItem;
 	}
 
 	public boolean getRemoveItem()
 	{
 		return removeItem;
 	}
 
 	public void setRemoveItem(final boolean removeItem)
 	{
 		this.removeItem = removeItem;
 	}
 
 	public List<String> getConfiguredParameters()
 	{
 		return configuredParameters;
 	}
 
 	public void setConfiguredParameters(List<String> configuredParameters)
 	{
 		this.configuredParameters = configuredParameters;
 	}
 
 	public Integer getId()
 	{
 		return id;
 	}
 
 	public void setId(Integer id)
 	{
 		this.id = id;
 	}
 
 	public Integer getRevision()
 	{
 		return revision;
 	}
 
 	public void setRevision(final Integer revision)
 	{
 		this.revision = revision;
 	}
 
     public RESTLogDetailsV1 getLogDetails()
     {
         return logDetails;
     }
 
     public void setLogDetails(final RESTLogDetailsV1 logDetails)
     {
         this.logDetails = logDetails;
     }
 }
