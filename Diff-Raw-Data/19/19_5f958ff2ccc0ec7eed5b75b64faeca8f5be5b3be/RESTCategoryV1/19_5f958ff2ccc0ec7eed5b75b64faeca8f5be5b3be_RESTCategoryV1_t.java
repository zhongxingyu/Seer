 package org.jboss.pressgangccms.rest.v1.entities;
 
 import org.jboss.pressgangccms.rest.v1.collections.RESTCategoryCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.entities.base.RESTBaseEntityV1;
 
 public class RESTCategoryV1 extends RESTBaseEntityV1<RESTCategoryV1, RESTCategoryCollectionV1>
 {
 	public static final String NAME_NAME = "name";
 	public static final String DESCRIPTION_NAME = "description";
 	public static final String MUTUALLYEXCLUSIVE_NAME = "mutuallyExclusive";
 	public static final String SORT_NAME = "sort";
 	public static final String TAGS_NAME = "tags";
 	
 	private String name = null;
 	private String description = null;
 	private boolean mutuallyExclusive = false;
 	private Integer sort = null;
 	private RESTTagCollectionV1 tags = null;
 	/** A list of the Envers revision numbers */
 	private RESTCategoryCollectionV1 revisions = null;
 	
 	@Override
 	public RESTCategoryCollectionV1 getRevisions()
 	{
 		return revisions;
 	}
 
 	@Override
 	public void setRevisions(final RESTCategoryCollectionV1 revisions)
 	{
 		this.revisions = revisions;
 	}
 	
 	@Override
 	public RESTCategoryV1 clone(boolean deepCopy)
 	{
 		final RESTCategoryV1 retValue = new RESTCategoryV1();
 		
 		this.cloneInto(retValue, deepCopy);
 		
 		retValue.name = this.name;
 		retValue.description = description;
 		retValue.mutuallyExclusive = this.mutuallyExclusive;
 		retValue.sort = this.sort == null ? null : new Integer(this.sort);
 		
 		if (deepCopy)
 		{
 			if (this.tags != null)
 			{
 				retValue.tags = new RESTTagCollectionV1();
 				this.tags.cloneInto(retValue.tags, deepCopy);
 			}
 			
 			if (this.getRevisions() != null)
 			{
 				retValue.revisions = new RESTCategoryCollectionV1();
 				this.revisions.cloneInto(retValue.revisions, deepCopy);
 			}			
 		}
 		else
 		{
 			retValue.tags = this.tags;
 			retValue.revisions = this.revisions;
 		}
 		
 		return retValue;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public void setName(final String name)
 	{
 		this.name = name;
 	}
 	
 	public void explicitSetName(final String name)
 	{
 		this.name = name;
 		this.setParameterToConfigured(NAME_NAME);
 	}
 
 	public String getDescription()
 	{
 		return description;
 	}
 
 	public void setDescription(final String description)
 	{
 		this.description = description;
 	}
 	
 	public void explicitSetDescription(final String description)
 	{
 		this.description = description;
 		this.setParameterToConfigured(DESCRIPTION_NAME);
 	}
 
 	public boolean getMutuallyExclusive()
 	{
 		return mutuallyExclusive;
 	}
 
 	public void setMutuallyExclusive(final boolean mutuallyExclusive)
 	{
 		this.mutuallyExclusive = mutuallyExclusive;
 	}
 	
 	public void explicitSetMutuallyExclusive(final boolean mutuallyExclusive)
 	{
 		this.mutuallyExclusive = mutuallyExclusive;
 		this.setParameterToConfigured(MUTUALLYEXCLUSIVE_NAME);
 	}
 
 	public Integer getSort()
 	{
 		return sort;
 	}
 
 	public void setSort(final Integer sort)
 	{
 		this.sort = sort;
 	}
 	
 	public void setSortExplicit(final Integer sort)
 	{
 		this.sort = sort;
 		this.setParameterToConfigured(SORT_NAME);
 	}
 
 	public RESTTagCollectionV1 getTags()
 	{
 		return tags;
 	}
 
 	public void setTags(final RESTTagCollectionV1 tags)
 	{
 		this.tags = tags;
 	}
 	
	public void explicitSetTags(final RESTTagCollectionV1 tags)
 	{
 		this.tags = tags;
 		this.setParameterToConfigured(TAGS_NAME);
 	}
 }
