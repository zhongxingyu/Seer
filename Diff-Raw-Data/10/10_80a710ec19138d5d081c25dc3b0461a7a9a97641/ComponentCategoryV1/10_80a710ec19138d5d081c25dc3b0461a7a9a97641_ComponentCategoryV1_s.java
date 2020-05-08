 package org.jboss.pressgang.ccms.rest.v1.components;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTTagInCategoryCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTCategoryV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTTagInCategoryV1;
 
 public class ComponentCategoryV1
 {
 	final RESTCategoryV1 source;
 	
 	public ComponentCategoryV1(final RESTCategoryV1 source)
 	{
 		this.source = source;
 	}
 	
	public boolean containsTag(final int tagId)
 	{
 		return containsTag(source, tagId);
 	}
 	
	static public boolean containsTag(final RESTCategoryV1 source, final int tagId)
 	{
 		if (source == null) throw new IllegalArgumentException("source cannot be null");
 		
 		if (source.getTags() != null && source.getTags().getItems() != null)
 		{
 			for (final RESTTagInCategoryCollectionItemV1 tagItem : source.getTags().getItems())
 			{
 			    final RESTTagInCategoryV1 tag = tagItem.getItem();
 				if (tag != null && tagId == tag.getId() && !tagItem.returnIsRemoveItem())
 					return true;
 			}
 		}
 
 		return false;
 	}
 }
