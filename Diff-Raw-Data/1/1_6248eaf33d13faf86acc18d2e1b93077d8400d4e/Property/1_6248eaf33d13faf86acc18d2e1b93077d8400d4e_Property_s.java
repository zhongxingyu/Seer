 /*
  * Created on Nov 15, 2004
  */
 package zz.utils.properties;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import zz.utils.FailsafeLinkedList;
 import zz.utils.notification.ObservationCenter;
 import zz.utils.notification.Observer;
import zz.utils.properties.Property.IRef<zz.utils.properties.IPropertyListener>;
 
 /**
  * Suplement the lack of language support for properties.
  * It permits to encapsulate a class's field in an object that
  * let's the programmer overload getter and setter, and that
  * provides a notification mechanism.
  * Concrete subclasses are {@link zz.utils.properties.RWProperty}
  * and {@link zz.utils.properties.ROProperty}.
  * See {@link zz.utils.properties.Example} to learn how to
  * use properties.
  * @author gpothier
  */
 public abstract class Property<T> implements Observer
 {
 	private IPropertyContainer itsContainer;
 	private PropertyId itsPropertyId;
 	
 	private List<IRef<IPropertyVeto>> itsVetos = 
 		new FailsafeLinkedList();
 	
 	private List<IRef<IPropertyListener>> itsListeners = 
 		new FailsafeLinkedList();
 	
 	/**
 	 * The actual value of the property
 	 */
 	private T itsValue;
 	
 	public Property(IPropertyContainer aContainer, PropertyId aPropertyId)
 	{
 		itsContainer = aContainer;
 		itsPropertyId = aPropertyId;
 	}
 	
 	public Property(IPropertyContainer aContainer, PropertyId aPropertyId, T aValue)
 	{
 		itsContainer = aContainer;
 		itsPropertyId = aPropertyId;
 		itsValue = aValue;
 	}
 	
 	/**
 	 * Internal getter for the property.
 	 */
 	protected final T get0()
 	{
 		return itsValue;
 	}
 
 	/**
 	 * Internal setter for the property.
 	 * It first check if a veto rejects the new value. If not, it
 	 * sets the current value and fires notifications.
 	 * @param aValue The new value of the property.
 	 */
 	protected final void set0(T aValue)
 	{
 		if (canChangeProperty(aValue))
 		{
 			if (itsValue != null) ObservationCenter.getInstance().unregisterListener(itsValue, this);
 			itsValue = aValue;
 			if (itsValue != null) ObservationCenter.getInstance().registerListener(itsValue, this);
 			firePropertyChanged();
 			
 			ObservationCenter.getInstance().requestObservation(itsContainer, this);
 		}
 	}
 	
 	public void observe(Object aObservable, Object aData)
 	{
 		if (aObservable == itsValue) firePropertyChanged();
 	}
 	
 	protected void firePropertyChanged ()
 	{
 		for (Iterator theIterator = itsListeners.iterator();theIterator.hasNext();)
 		{
 			IRef<IPropertyListener> theRef = (IRef<IPropertyListener>) theIterator.next();
 			IPropertyListener theListener = theRef.get();
 			if (theListener == null) theIterator.remove();
 			else theListener.propertyChanged(this);
 		}
 	}
 	
 	protected boolean canChangeProperty (Object aValue)
 	{
 		for (Iterator theIterator = itsVetos.iterator();theIterator.hasNext();)
 		{
 			IRef<IPropertyVeto> theRef = (IRef<IPropertyVeto>) theIterator.next();
 			IPropertyVeto theVeto = theRef.get();
 			if (theVeto == null) theIterator.remove();
 			else if (! theVeto.canChangeProperty(this, aValue)) return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Adds a listener that will be notified each time this
 	 * property changes.
 	 * The property will maintains a weak reference to the listener,
 	 * so the programmer should ensure that the listener is strongly
 	 * referenced somewhere.
 	 * In particular, this kind of construct should not be used:
 	 * <pre>
 	 * prop.addListener (new MyListener());
 	 * </pre>
 	 * In this case, use {@link #addHardListener(IPropertyListener)}
 	 * instead.
 	 */
 	public void addListener (IPropertyListener aListener)
 	{
 		itsListeners.add (new WeakRef<IPropertyListener>(aListener));
 	}
 
 	/**
 	 * Adds a listener that will be notified each time this
 	 * property changes.
 	 * The listener will be referenced through a strong reference.
 	 * @see #addListener(IPropertyListener)
 	 */
 	public void addHardListener (IPropertyListener aListener)
 	{
 		itsListeners.add (new HardRef<IPropertyListener>(aListener));
 	}
 	
 	/**
 	 * Removes a previously added listener.
 	 */
 	public void removeListener (IPropertyListener aListener)
 	{
 		for (Iterator theIterator = itsListeners.iterator();theIterator.hasNext();)
 		{
 			IRef<IPropertyListener> theRef = (IRef<IPropertyListener>) theIterator.next();
 			IPropertyListener theListener = theRef.get();
 			if (theListener == null || theListener == aListener) theIterator.remove();
 		}
 	}
 
 	/**
 	 * Adds a veto that can reject a new value for this property.
 	 * See the comment on {@link #addListener(IPropertyListener)}
 	 * about the referencing scheme.
 	 */
 	public void addVeto (IPropertyVeto aVeto)
 	{
 		itsVetos.add (new WeakRef<IPropertyVeto>(aVeto));
 	}
 
 	/**
 	 * Adds a veto that can reject a new value for this property.
 	 * See the comment on {@link #addListener(IPropertyListener)}
 	 * about the referencing scheme.
 	 */
 	public void addHardVeto (IPropertyVeto aVeto)
 	{
 		itsVetos.add (new HardRef<IPropertyVeto>(aVeto));
 	}
 	
 	/**
 	 * Removes a previously added veto.
 	 */
 	public void removeVeto (IPropertyVeto aVeto)
 	{
 		for (Iterator theIterator = itsVetos.iterator();theIterator.hasNext();)
 		{
 			IRef<IPropertyVeto> theRef = (IRef<IPropertyVeto>) theIterator.next();
 			IPropertyVeto theVeto = theRef.get();
 			if (theVeto == null || theVeto == aVeto) theIterator.remove();
 		}
 	}
 
 	private interface IRef<T>
 	{
 		public T get();
 	}
 	
 	private static class HardRef<T> implements IRef<T>
 	{
 		private T itsValue;
 		
 		public HardRef(T aValue)
 		{
 			itsValue = aValue;
 		}
 		
 		public T get()
 		{
 			return itsValue;
 		}
 	}
 	
 	private static class WeakRef<T> extends WeakReference<T> implements IRef<T>
 	{
 
 		public WeakRef(T aValue)
 		{
 			super(aValue);
 		}
 		
 	}
 }
