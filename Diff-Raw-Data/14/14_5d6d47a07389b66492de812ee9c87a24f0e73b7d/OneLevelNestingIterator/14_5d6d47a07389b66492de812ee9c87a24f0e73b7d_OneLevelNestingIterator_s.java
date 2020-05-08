 package ecologylab.generic;
 
 import java.util.Iterator;
 
 /**
  * Iterates through a Collection of things, and then through an Iterator
  * of such (nested) Collections of things.
  * Provides flat access to all members.
  * 
  * @author andruid
  *
  * @param <I>		Class that we iterate over.
  * @param <O>		Class of objects that are applied in the context of what we iterate over.
  * 					This typically starts as this, but shifts as we iterate through 
  * 					the nested Collection of Iterators.
  */
 public class OneLevelNestingIterator<I, O extends Iterable<I>>
 implements Iterator<I>
 {
 	protected Iterator<I> firstIterator;
 	
 	protected Iterator<? extends O> collection;
 	
 	protected O			currentObject;
 	
 	protected Iterator<I>	currentIterator;
 	
	public OneLevelNestingIterator(O firstObject, Iterator<? extends O> iterableCollection)
 	{
 		this.firstIterator	= firstObject.iterator();
 		this.currentObject	= firstObject;
 		this.collection			= iterableCollection;
 	}
 	
 	public OneLevelNestingIterator(O firstObject, Iterable<? extends O> iterableCollection)
 	{
		this.firstIterator	= firstObject.iterator();
		this.currentObject	= firstObject;
 		this.collection			= (iterableCollection != null) ? iterableCollection.iterator() : null;
 	}
 	
 	private boolean collectionHasNext()
 	{
 		return collection != null && (collection.hasNext() || currentHasNext());
 	}
 
 	private boolean currentHasNext() 
 	{
 		return (currentIterator != null) && currentIterator.hasNext();
 	}
 	
 	public boolean hasNext()
 	{
 		return firstIterator.hasNext() || collectionHasNext();
 	}
 
 	public I next() 
 	{
 		if (firstIterator.hasNext())
 		{
 			I firstNext = firstIterator.next();	
 			// avoid returning the collection, itself, when it is a field in the firstIterator
 			return (firstNext != collection) ? firstNext : next();
 		}
 		// else
 		if (currentHasNext())
 			return currentIterator.next();
 		// else
 		if (collectionHasNext())
 		{
 			currentObject		= collection.next();
 			currentIterator		= currentObject.iterator();
 			return currentIterator.next();
 		}
 		return null;
 	}
 	
 	public O currentObject()
 	{
 		return currentObject;
 	}
 
 	public void remove() 
 	{
 		throw new UnsupportedOperationException();
 	}
 }
