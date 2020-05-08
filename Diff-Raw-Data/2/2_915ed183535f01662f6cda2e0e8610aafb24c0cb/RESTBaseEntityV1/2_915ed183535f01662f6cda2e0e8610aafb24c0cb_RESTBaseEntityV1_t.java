 package org.jboss.pressgangccms.rest.v1.entities.base;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgangccms.rest.v1.collections.base.BaseRestCollectionV1;
 
 
 public abstract class RESTBaseEntityV1<T extends RESTBaseEntityV1<T, U>, U extends BaseRestCollectionV1<T, U>>
 {
 	public static final String REVISIONS_NAME = "revisions";
 	
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
 
 	abstract public U getRevisions();
 
 	abstract public void setRevisions(U revisions);
 		
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
 	}
 	
 	/**
 	 * @param deepCopy true if referenced objects should be copied, false if the referenced themselves should be copied
 	 * @return A clone of this object
 	 */
 	public abstract T clone(final boolean deepCopy);
 	
 	/**
 	 * This is a convenience method that adds a value to the configuredParameters collection
 	 * @param paramater The parameter to specify as configured
 	 */
 	protected void setParamaterToConfigured(final String paramater)
 	{
 		if (configuredParameters == null)
 			configuredParameters = new ArrayList<String>();
 		if (!configuredParameters.contains(paramater))
 			configuredParameters.add(paramater);
 	}
 	
 	public boolean hasParameterSet(final String parameter)
 	{
 		return getConfiguredParameters() != null && getConfiguredParameters().contains(parameter);
 	}
 
 	public void setLinks(final String baseUrl, final String restBasePath, final String dataType, final Object id)
 	{
 		this.setSelfLink(baseUrl + "/1/" + restBasePath + "/get/" + dataType + "/" + id);
 		this.setDeleteLink(baseUrl + "/1/" + restBasePath + "/delete/" + dataType + "/" + id);
		this.setAddLink(baseUrl + "/1/" + restBasePath + "/post/" + dataType);
 		this.setEditLink(baseUrl + "/1/" + restBasePath + "/put/" + dataType + "/" + id);
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
 }
