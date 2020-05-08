 /*
  * Created on Dec 14, 2004
  */
 package zz.utils.properties;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import zz.utils.FailsafeLinkedList;
 import zz.utils.ReverseIteratorWrapper;
 import zz.utils.list.ICollectionListener;
 import zz.utils.list.IListListener;
 import zz.utils.references.HardRef;
 import zz.utils.references.IRef;
 import zz.utils.references.RefUtils;
 import zz.utils.references.WeakRef;
 
 /**
  * Can be used as a base for implementing {@link zz.utils.properties.IListProperty}.
  * Manages listeners.
  * TODO: Use a trait to share with AbstractList
  * @author gpothier
  */
 public abstract class AbstractListProperty<E> extends AbstractProperty<List<E>> 
 implements IListProperty<E>
 {
 	/**
 	 * We store all the listeners here, be they collection or
 	 * list listeners
 	 */
 	private List<IRef<Object>> itsListListeners;
 	
 	public AbstractListProperty(Object aContainer)
 	{
 		super(aContainer);
 	}
 	
 	public AbstractListProperty(Object aContainer, PropertyId<List<E>> aPropertyId)
 	{
 		super(aContainer, aPropertyId);
 	}
 	
 	public void addListener (IListListener<E> aListener)
 	{
 		if (itsListListeners == null) itsListListeners = new ArrayList(3);
 		itsListListeners.add (new WeakRef<Object>(aListener));
 	}
 
 	public void addHardListener (IListListener<E> aListener)
 	{
 		if (itsListListeners == null) itsListListeners = new ArrayList(3);
 		itsListListeners.add (new HardRef<Object>(aListener));
 	}
 	
 	public void removeListener (IListListener<E> aListener)
 	{
 		if (itsListListeners != null) 
 		{
 			RefUtils.remove(itsListListeners, aListener);
 			if (itsListListeners.size() == 0) itsListListeners = null;
 		}
 	}
 
 	public void addListener (ICollectionListener<E> aListener)
 	{
 		if (itsListListeners == null) itsListListeners = new ArrayList(3);
 		itsListListeners.add (new WeakRef<Object>(aListener));
 	}
 
 	public void addHardListener (ICollectionListener<E> aListener)
 	{
 		if (itsListListeners == null) itsListListeners = new ArrayList(3);
 		itsListListeners.add (new HardRef<Object>(aListener));
 	}
 	
 	public void removeListener (ICollectionListener<E> aListener)
 	{
 		if (itsListListeners != null) 
 		{
 			RefUtils.remove(itsListListeners, aListener);
 			if (itsListListeners.size() == 0) itsListListeners = null;
 		}
 	}
 
 	/**
 	 * This method is called whenever an element is added to this list.
 	 * By default it does nothing, but subclasses can override it to be notified.
 	 */
 	protected void elementAdded (int aIndex, E aElement)
 	{
 	}
 	
 	/**
 	 * This method is called whenever an element is removed from this list.
 	 * By default it does nothing, but subclasses can override it to be notified.
 	 */
 	protected void elementRemoved (int aIndex, E aElement)
 	{
 	}
 	
 	protected void fireElementAdded (int aIndex, E aElement)
 	{
 		elementAdded(aIndex, aElement);
 	
 		if (itsListListeners == null) return;
 		List<Object> theListeners = RefUtils.dereference(itsListListeners);
 		
 		for (Object theListener : theListeners)
 		{
 			if (theListener instanceof IListListener)
 			{
 				IListListener<E> theListPropertyListener = 
 					(IListListener) theListener;
 				theListPropertyListener.elementAdded(this, aIndex, aElement);
 			}
 			else if (theListener instanceof ICollectionListener)
 			{
 				ICollectionListener<E> theCollectionPropertyListener = 
 					(ICollectionListener) theListener;
 				theCollectionPropertyListener.elementAdded(this, aElement);
 			}
 		}
 	}
 	
 	protected void fireElementRemoved (int aIndex, E aElement)
 	{
 		elementRemoved(aIndex, aElement);
 
 		if (itsListListeners == null) return;
 		List<Object> theListeners = RefUtils.dereference(itsListListeners);
 		
 		for (Object theListener : theListeners)
 		{
 			if (theListener instanceof IListListener)
 			{
 				IListListener<E> theListPropertyListener = 
 					(IListListener) theListener;
 				theListPropertyListener.elementRemoved(this, aIndex, aElement);
 			}
 			else if (theListener instanceof ICollectionListener)
 			{
 				ICollectionListener<E> theCollectionPropertyListener = 
 					(ICollectionListener) theListener;
 				theCollectionPropertyListener.elementRemoved(this, aElement);
 			}
 		}
 	}
 	
 	public IListProperty<E> cloneForContainer(Object aContainer, boolean aCloneValue)
 	{
 		// Note: we don't tell super to clone value, we handle it ourselves.
 		// As we are an abstract class, we don't do anything special.
 		AbstractListProperty<E> theClone = 
 			(AbstractListProperty<E>) super.cloneForContainer(aContainer, false); 
 		
 		theClone.itsListListeners = null;
 		
 		return theClone;
 	}
 
 
 }
