 /**
  * 
  */
 package ecologylab.appframework.types.prefs;
 
 import java.awt.Color;
 import java.io.File;
 import java.util.LinkedList;
 
 import ecologylab.appframework.ApplicationEnvironment;
 import ecologylab.appframework.ObjectRegistry;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.xml_inherit;
 import ecologylab.xml.types.element.ArrayListState;
 
 /**
  * Generic base class for application Preference objects.
  * 
  * @author andruid
  */
 
 @xml_inherit
 public abstract class Pref<T> extends ArrayListState
 {
 	/** The global registry of Pref objects. Used for providing lookup services. */
     static final ObjectRegistry<Pref>   allPrefsMap = new ObjectRegistry<Pref>();
 
     /** The ApplicationEnvironment associated with this JVM. */
     static final ApplicationEnvironment aE          = null;
 
     /** Name of a Pref; provides index into the preferences map. */
     @xml_attribute String               name;
 
     /** Cached value */
     T                                   valueCached;
 
     /**
      * The list of PrefChangedListeners registered to respond to changes in
      * Prefs.
      */
     static LinkedList<PrefChangedListener>     listeners = new LinkedList<PrefChangedListener>();
     
 	/** No-argument constructor for XML translation. */
 	public Pref()
 	{
 		super();
 	}
 
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
     
     /**
      * Print Pref name and value
      */
     public void print()
     {
         println("Pref: name: " + name + ", value: " + this.getValue());
     }
     
     /**
      * Return String of Pref name and value
      * 
      * @return String of Pref name and value
      */
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
      * Generic value setter. Uses boxed reference objects for primitives, which
      * are a bit extra expensive.
      * 
      * @param newValue
      */
 	public abstract void setValue(T newValue);
 	
     /**
      * Performs all housekeeping associated with updating this Pref.
      * prefUpdated() should be called whenever the value of this has been
      * changed.
      * 
      * Notifies all listeners that the pref's value has changed.
      * 
      * Set valueCached to null
      */
 	protected void prefChanged()
 	{
 		valueCached	= null;
         
         Pref.firePrefChangedEvent(this);
 	}
 	
 	/**
 	 * This is for working with <code>Pref</code>s whose values you will continue to access as they
 	 * are edited, live, by the user. The result will be immediate changes in the program's behavior.
 	 * <p/>
 	 * Lookup a Pref associated with name.
 	 * If you find it return it.
 	 * If not, create a new Pref object of the correct type.
 	 * Set its value to default value.
 	 * 
 	 * @param name			Name of the Pref to lookup and find or create.
 	 * @param defaultValue	Initial value of the Pref if it didn't already exist.
 	 * 
 	 * @return	A usable Pref object associated with name, either from the registry or newly created
 	 */
     public static PrefBoolean usePrefBoolean(String name, boolean defaultValue)
     {
         PrefBoolean pref= (PrefBoolean) lookupPref(name);
         if (pref == null)
         {
         	pref		= new PrefBoolean(defaultValue);
         	pref.name	= name;
         	pref.register();
         }
         return pref;
     }
     
 	/**
 	 * This is for working with <code>Pref</code>s whose values you will continue to access as they
 	 * are edited, live, by the user. The result will be immediate changes in the program's behavior.
 	 * <p/>
 	 * Lookup a Pref associated with name.
 	 * If you find it return it.
 	 * If not, create a new Pref object of the correct type.
 	 * Set its value to default value.
 	 * 
 	 * @param name			Name of the Pref to lookup and find or create.
 	 * @param defaultValue	Initial value of the Pref if it didn't already exist.
 	 * 
 	 * @return	A usable Pref object associated with name, either from the registry or newly created
 	 */
     public static PrefFloat usePrefFloat(String name, float defaultValue)
     {
         PrefFloat pref	= (PrefFloat) lookupPref(name);
         if (pref == null)
         {
         	pref		= new PrefFloat(defaultValue);
         	pref.name	= name;
         	pref.register();
         }
         return pref;
     }
     
 	/**
 	 * This is for working with <code>Pref</code>s whose values you will continue to access as they
 	 * are edited, live, by the user. The result will be immediate changes in the program's behavior.
 	 * <p/>
 	 * Lookup a Pref associated with name.
 	 * If you find it return it.
 	 * If not, create a new Pref object of the correct type.
 	 * Set its value to default value.
 	 * 
 	 * @param name			Name of the Pref to lookup and find or create.
 	 * @param defaultValue	Initial value of the Pref if it didn't already exist.
 	 * 
 	 * @return	A usable Pref object associated with name, either from the registry or newly created
 	 */
     public static PrefString usePrefString(String name, String defaultValue)
     {
     	PrefString pref = (PrefString) lookupPref(name);
         if (pref == null)
         {
         	pref		= new PrefString(defaultValue);
         	pref.name	= name;
         	pref.register();
         }
         return pref;
     }
     
 	/**
 	 * This is for working with <code>Pref</code>s whose values you will continue to access as they
 	 * are edited, live, by the user. The result will be immediate changes in the program's behavior.
 	 * <p/>
 	 * Lookup a Pref associated with name.
 	 * If you find it return it.
 	 * If not, create a new Pref object of the correct type.
 	 * Set its value to default value.
 	 * 
 	 * @param name			Name of the Pref to lookup and find or create.
 	 * @param defaultValue	Initial value of the Pref if it didn't already exist.
 	 * 
 	 * @return	A usable Pref object associated with name, either from the registry or newly created
 	 */
     public static PrefInt usePrefInt(String name, int defaultValue)
     {
     	PrefInt pref = (PrefInt) lookupPref(name);
         if (pref == null)
         {
         	pref		= new PrefInt(defaultValue);
         	pref.name	= name;
         	pref.register();
         }
         return pref;
     }
    
     /**
 	 * This is for working with <code>Pref</code>s whose values you will continue to access as they
 	 * are edited, live, by the user. The result will be immediate changes in the program's behavior.
 	 * <p/>
 	 * Lookup a Pref associated with name.
 	 * If you find it return it.
 	 * If not, create a new Pref object of the correct type.
 	 * Set its value to default value.
 	 * 
 	 * @param name			Name of the Pref to lookup and find or create.
 	 * @param defaultValue	Initial value of the Pref if it didn't already exist.
 	 * 
 	 * @return	A usable Pref object associated with name, either from the registry or newly created
 	 */
     public static PrefColor usePrefColor(String name, Color defaultValue)
     {
     	PrefColor pref = (PrefColor) lookupPref(name);
         if (pref == null)
         {
         	pref		= new PrefColor(defaultValue);
         	pref.name	= name;
         	pref.register();
         }
         return pref;
     }
 
     /**
      * Look up a Pref by name in the map of all Prefs
      * 
      * @param name  Name of Pref
      * 
      * @return Pref with the given name
      */
     public static Pref lookupPref(String name)
     {
         Pref pref = allPrefsMap.lookupObject(name);
         return pref;
     }
     
     /**
      * Look up a PrefInt by name in the map of all Prefs.
      * Return defaultValue if PrefInt's value is null.
      * 
      * @param name          Name of PrefInt
      * @param defaultValue  default value for PrefInt
      * 
      * @return PrefInt's value or default value if doesn't exist
      */
     public static int lookupInt(String name, int defaultValue) throws ClassCastException
     {
     	/* could do this -- its heavier weight -- create a Pref if there wasn't one, and set its default value as here
         PrefInt prefInt = usePrefInt(name, defaultValue);
         return prefInt.value();
         */
         PrefInt prefInt = ((PrefInt)lookupPref(name));
 		return (prefInt == null) ? defaultValue : prefInt.value();
     }
     /**
      * Look up a PrefInt by name in the map of all Prefs.
      * 
      * @param name  Name of PrefInt
      * 
      * @return PrefInt's value or 0
      */
     public static int lookupInt(String name) throws ClassCastException
     {
         return lookupInt(name, 0);
     }
     
     /**
      * Look up a PrefBoolean by name in the map of all Prefs.
      * Return defaultValue if PrefBoolean's value is null.
      * 
      * @param name          Name of PrefBoolean
      * @param defaultValue  default value for PrefBoolean
      * 
      * @return PrefBoolean's value or default value if doesn't exist
      */
     public static boolean lookupBoolean(String name, boolean defaultValue) throws ClassCastException
     {
         PrefBoolean prefBoolean = ((PrefBoolean)lookupPref(name));
 		return (prefBoolean == null) ? defaultValue : prefBoolean.value();
     }
     /**
      * Look up a PrefBoolean by name in the map of all Prefs.
      * 
      * @param name          Name of PrefBoolean
      * 
      * @return PrefBoolean's value or false if doesn't exist
      */
     public static boolean lookupBoolean(String name) throws ClassCastException
     {
         return lookupBoolean(name, false);
     }
     
     /**
      * Look up a PrefFloat by name in the map of all Prefs.
      * Return defaultValue if PrefFloat's value is null.
      * 
      * @param name          Name of PrefFloat
      * @param defaultValue  default value to set PrefFloat to
      * 
      * @return PrefFloat's value or default value if doesn't exist
      */
     public static float lookupFloat(String name, float defaultValue) throws ClassCastException
     {
         PrefFloat prefFloat = ((PrefFloat)lookupPref(name));
 		return (prefFloat == null) ? defaultValue : prefFloat.value();
     }
     /**
      * Look up a PrefFloat by name in the map of all Prefs.
      * 
      * @param name          Name of PrefFloat
      * 
      * @return PrefFloat's value (if exists) or 1.0f
      */
     public static float lookupFloat(String name) throws ClassCastException
     {
         return lookupFloat(name, 1.0f);
     }
    
     /**
      * Look up a PrefString by name in the map of all Prefs.
      * Return defaultValue if PrefString's value is null.
      * 
      * @param name          Name of PrefString
      * @param defaultValue  default value for PrefString
      * 
      * @return PrefString's value or default value if doesn't exist
      */
     public static String lookupString(String name, String defaultValue) throws ClassCastException
     {
         PrefString prefString = ((PrefString)lookupPref(name));
 		return (prefString == null) ? defaultValue : prefString.value();
     }
     /**
      * Look up a PrefString by name in the map of all Prefs.
      * Return null if PrefString's value is null.
      * 
      * @param name          Name of PrefString
      * 
      * @return PrefString's value or null
      */
     public static String lookupString(String name) throws ClassCastException
     {
         return lookupString(name, null);
     }
     
     /**
      * Look up a PrefFile by name in the map of all Prefs. Return null if the
      * PrefFile's value is null;
      * 
      * @param name
      *            Name of the PrefFile
      * @return PrefFile's value or null, if the Pref associated with name does
      *         not exist
      * @throws ClassCastException
      *             if name does not match a PrefFile object
      */
     public static File lookupFile(String name) throws ClassCastException
     {
         PrefFile prefFile = ((PrefFile) lookupPref(name));
         return (prefFile == null) ? null : prefFile.value();
     }
     
     /**
      * Look up a PrefColor by name in the map of all Prefs.
      * Return defaultValue if PrefColor's value is null.
      * 
      * @param name          Name of PrefColor
      * @param defaultValue  default value for PrefColor
      * 
      * @return PrefColor's value or default value if doesn't exist
      */
     public static Color lookupColor(String name, Color defaultValue) throws ClassCastException
     {
         PrefColor prefColor = ((PrefColor)lookupPref(name));
 		return (prefColor == null) ? defaultValue : prefColor.value();
     }
     /**
      * Look up a PrefColor by name in the map of all Prefs.
      * 
      * @param name          Name of PrefColor
      * 
      * @return PrefColor's value or null
      */
     public static Color lookupColor(String name) throws ClassCastException
     {
         return lookupColor(name, null);
     }
     
     /**
      * Look up a PrefElementState by name in the map of all Prefs.
      * Set to defaultValue if PrefElementState's value is null.
      * 
      * @param name          Name of PrefElementState
      * 
      * @return PrefElementState's value or null if doesn't exist
      */
     public static ElementState lookupElementState(String name) throws ClassCastException
     {
         PrefElementState prefElementState = ((PrefElementState)lookupPref(name));
 		return (prefElementState == null) ? null : prefElementState.value();
     }
     
     /**
      * Check for existence / membership.
      * 
      * @param key
      * 
      * @return  true if there is a Pref already registered with name key
      */
     public static boolean hasPref(String name)
     {
         return allPrefsMap.containsKey(name);
     }
 
     /**
      * Create an entry for this in the allPrefsMap.
      *
      */
     void register()
     {
     	allPrefsMap.registerObject(this.name, this);
     }
     
     /**
      * Check for existence / membership.
      * 
      * @param key
      * 
      * @return	true if there is a Pref already registered with name key
      */
     public static boolean containsKey(String key)
     {
     	return allPrefsMap.containsKey(key);
     }
 
     public String getName()
     {
         return name;
     }
 
     public static void addPrefChangedListener(PrefChangedListener l)
     {
         listeners.add(l);
     }
     
     private static void firePrefChangedEvent(Pref pref)
     {
         for (PrefChangedListener l : listeners)
         {
             l.prefChanged(pref);
         }
     }
     
     public static void prefUpdated(Pref pref)
     {
         firePrefChangedEvent(pref);
     }
 }
