 package com.redhat.topicindex.rest.collections;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.redhat.topicindex.rest.entities.interfaces.RESTBaseEntityV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTTagV1;
 
 /**
  * @author Matthew Casperson
  * 
  * @param <T>
  *            The REST entity type
  * @param <U>
  *            The REST Collection type
  */
 abstract public class BaseRestCollectionV1<T extends RESTBaseEntityV1<T, U>, U extends BaseRestCollectionV1<T, U>>
 {
 	private Integer size = 0;
 	private String expand = null;
 	private Integer startExpandIndex = null;
 	private Integer endExpandIndex = null;
 
 	abstract public List<T> getItems();
 
 	abstract public void setItems(final List<T> items);
 
 	/**
 	 * It is possible that a client has sent up a collection that asks to add and remove the same
 	 * child item in a collection. This method, combined with the ignoreDuplicatedAddRemoveItemRequests() method,
 	 * will weed out any duplicated requests.
 	 */
 	public void removeInvalidAddRemoveItemRequests()
 	{
 		/* ignore attempts to remove and add the same tag */
 		if (this.getItems() != null)
 		{
 			final List<T> removeChildren = new ArrayList<T>();
 
 			/* on the first loop, remove any items that are marked for both add a remove in the same item, or are not marked for any processing at all */
 			for (final T child1 : this.getItems())
 			{
 				final boolean add1 = child1.getAddItem();
 				final boolean remove1 = child1.getRemoveItem();
 
 				if ((add1 && remove1) || (!add1 && !remove1))
 				{
 					if (!removeChildren.contains(child1))
 						removeChildren.add(child1);
 				}
 			}
 
 			for (final T removeChild : removeChildren)
 				this.getItems().remove(removeChild);
 
 			ignoreDuplicatedAddRemoveItemRequests();
 
 		}
 	}
 
 	/**
 	 * This method will clear out any child items that are marked for both add and remove, or duplicated add and remove requests.
 	 * Override this method to deal with collections where the children are not uniquly identified by only their id.
 	 */
 	protected void ignoreDuplicatedAddRemoveItemRequests()
 	{
 		if (this.getItems() != null)
 		{
 			final List<T> removeChildren = new ArrayList<T>();
 		
 			/* on the second loop, remove any items that are marked for both add and remove is separate items */
 			for (int i = 0; i < this.getItems().size(); ++i)
 			{
 				final T child1 = this.getItems().get(i);
 				
 				/* at this point we know that either add1 or remove1 will be true, but not both */
 				final boolean add1 = child1.getAddItem();
 				final boolean remove1 = child1.getRemoveItem();
 				
 				/* Loop a second time, looking for duplicates */
 				for (int j = i + 1; j < this.getItems().size(); ++j)
 				{
 					final T child2 = this.getItems().get(j);
 					
 					if (child1.getId().equals(child2.getId()))
 					{
 						final boolean add2 = child2.getAddItem();
 						final boolean remove2 = child2.getRemoveItem();
 						
 						/* check for double add, double remove, add and remove, remove and add */
 						if ((add1 && add2) || (remove1 && remove2) || (add1 && remove2) || (remove1 && add2))						
 							if (!removeChildren.contains(child1))
 								removeChildren.add(child1);
 					}
 				}
 			}
 		}
 	}
 
 	public void cloneInto(final BaseRestCollectionV1<T, U> dest, final boolean deepCopy)
 	{
 		dest.size = this.size;
 		dest.expand = this.expand;
 		dest.startExpandIndex = this.startExpandIndex;
 		dest.endExpandIndex = this.endExpandIndex;
 
 		if (this.getItems() != null)
 		{
 			dest.setItems(new ArrayList<T>());
 			if (deepCopy)
 			{
 				for (final T item : this.getItems())
 					dest.getItems().add(item.clone(deepCopy));
 			}
 			else
 			{
 				dest.getItems().addAll(this.getItems());
 			}
 		}
 	}
 
 	public Integer getSize()
 	{
 		return size;
 	}
 
 	public void setSize(final Integer size)
 	{
 		this.size = size;
 	}
 
 	public String getExpand()
 	{
 		return expand;
 	}
 
 	public void setExpand(final String expand)
 	{
 		this.expand = expand;
 	}
 
 	public Integer getStartExpandIndex()
 	{
 		return startExpandIndex;
 	}
 
 	public void setStartExpandIndex(final Integer startExpandIndex)
 	{
 		this.startExpandIndex = startExpandIndex;
 	}
 
 	public Integer getEndExpandIndex()
 	{
 		return endExpandIndex;
 	}
 
 	public void setEndExpandIndex(final Integer endExpandIndex)
 	{
 		this.endExpandIndex = endExpandIndex;
 	}
 
 	public void addItem(final T item)
 	{
 		if (this.getItems() == null)
 			this.setItems(new ArrayList<T>());
 		this.getItems().add(item);
 	}
 }
