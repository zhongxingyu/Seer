 /**
  * 
  */
 package ecologylab.appframework.types.prefs;
 
 import java.awt.Color;
 import java.util.HashMap;
 
 import ecologylab.appframework.Environment;
 import ecologylab.appframework.ObjectRegistry;
 import ecologylab.generic.Debug;
 import ecologylab.generic.Generic;
 import ecologylab.generic.Palette;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.xml_inherit;
 
 /**
  * Generic base class for application Preference objects.
  * 
  * @author andruid
  */
 
 @xml_inherit
 public abstract class Pref<T> extends ElementState
 {
     @xml_attribute String name;
 	
 	/**
 	 * 
 	 */
 	public Pref()
 	{
 		super();
 	}
 
 	T		valueCached;
 	
 	/**
 	 * Public generic accessor for the value.
 	 * Caches autoboxed values, for efficiency.
 	 * @return
 	 */
 	public T value()
 	{
 		T result	= valueCached;
 		if (result == null)
 		{
 			result	= getValue();
 			valueCached	= result;
 		}
 		return result;
 	}
     
     public void print()
     {
         println("Pref: name: " + name + ", value: " + this.getValue());
     }
    
    public String toString()
    {
        return "Pref: name: "+name+", value: "+this.getValue();
    }
 	
 	/**
 	 * Generic get value returns the value as the actual type you want.
 	 * This version should only be called by value(), so that autoboxed types
 	 * can be cached. This method *does not* do the caching.
 	 * 
 	 * @return
 	 */
 	abstract T getValue();
 	
 	/**
 	 * Generic value setter.
 	 * Uses boxed reference objects for primitives, which are a bit extra expensive.
 	 * 
 	 * @param newValue
 	 */
 	public abstract void setValue(T newValue);
 	
 	protected void invalidate()
 	{
 		valueCached	= null;
 	}
 }
