 /*
  * Created on Dec 14, 2004
  */
 package zz.utils.properties;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import zz.utils.IPublicCloneable;
 import zz.utils.ReverseIteratorWrapper;
 
 /**
  * @author gpothier
  */
 public class ArrayListProperty<E> extends AbstractListProperty<E> 
 {
 	private List<E> itsList = new MyList ();
 	
 	public ArrayListProperty(Object aOwner)
 	{
 		super(aOwner);
 	}
 	
 	public ArrayListProperty(Object aOwner, PropertyId<List<E>> aPropertyId)
 	{
 		super(aOwner, aPropertyId);
 	}
 	
 	public List<E> get()
 	{
 		return itsList;
 	}
 	
 	/**
 	 * Changes the list that backs the property.
 	 * This should be used with care, as it will not send any notification.
 	 */
 	protected void set (List<E> aList)
 	{
 		// The below code is a workaround for a strange jdk compile error.
 		if (aList != null && MyList.class.isAssignableFrom(aList.getClass()))
 //		if (aList instanceof MyList) This is the original code
 			itsList = ((MyList) aList);
 
 		else itsList = aList != null ? new MyList (aList) : null;
 	}
 	
 	/**
 	 * Resets the list.
 	 * This should be used with care, as it will not send any notification.
 	 */
 	protected void reset()
 	{
 		set(new MyList());
 	}
 	
 	public void clear()
 	{
 		for (int i = size()-1;i>=0;i--)
 			remove(i);
 	}
 
 	public Iterator<E> reverseIterator()
 	{
 		return new ReverseIteratorWrapper (get());
 	}
 	
 	
 	public IListProperty<E> cloneForOwner(Object aOwner, boolean aCloneValue)
 	{
 		// Note: we don't tell super to clone value, we handle it ourselves.
 		ArrayListProperty<E> theClone = 
 			(ArrayListProperty) super.cloneForOwner(aOwner, false);
 		
 		if (aCloneValue)
 		{
 			theClone.itsList = new MyList();
 			for (E theElement : itsList)
 			{
 				IPublicCloneable theClonable = (IPublicCloneable) theElement;
 				E theClonedElement = (E) theClonable.clone();
 				theClone.itsList.add (theClonedElement);
 			}
 		}
 		else
 		{
			theClone.itsList = itsList != null ? new MyList(itsList) : null;
 		}
 		
 		return theClone;
 	}
 	
 	
 	/**
 	 * This is our implementation of List, which override some methods in
 	 * order to send notifications. 
 	 * @author gpothier
 	 */
 	protected class MyList extends ArrayList<E>
 	{
 		public MyList()
 		{
 		}
 		
 		public MyList(Collection<? extends E> aContent)
 		{
 			addAll(aContent);
 		}
 		
 		public boolean remove(Object aO)
 		{
 			int theIndex = indexOf(aO);
 			if (theIndex >= 0) 
 			{
 				remove(theIndex);
 				return true;
 			}
 			else return false;
 		}
 		
 		public E remove(int aIndex)
 		{
 			E theElement = super.remove(aIndex);
 			fireElementRemoved(aIndex, theElement);
 			return theElement;
 		}
 		
 		public boolean add(E aElement)
 		{
 			boolean theResult = super.add(aElement);
 			fireElementAdded(size()-1, aElement);
 			return theResult;
 		}
 		
 		public void add(int aIndex, E aElement)
 		{
 			super.add(aIndex, aElement);
 			fireElementAdded(aIndex, aElement);
 		}
 		
 		public boolean addAll(Collection<? extends E> aCollection)
 		{
 			return addAll(0, aCollection);
 		}
 		
 		public boolean addAll(int aIndex, Collection<? extends E> aCollection)
 		{
 			int theIndex = aIndex;
 			for (E theElement : aCollection)
 				add (theIndex++, theElement);
 			
 			return aCollection.size() > 0;
 		}
 		
 		public E set(int aIndex, E aElement)
 		{
 			E theElement = super.set(aIndex, aElement);
 
 			fireElementRemoved(aIndex, theElement);
 			fireElementAdded(aIndex, aElement);
 
 			return theElement;
 		}
 	}
 
 }
