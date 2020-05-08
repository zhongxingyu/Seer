 /**
  * 
  */
 package ecologylab.appframework.types.prefs;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Set;
 
 import ecologylab.appframework.ApplicationPropertyNames;
 import ecologylab.net.ParsedURL;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.TranslationScope;
 import ecologylab.xml.XMLTranslationException;
 import ecologylab.xml.xml_inherit;
 
 /**
  * A serial set of Pref objects. Used for reading and writing (load and save). The static
  * allPrefsMap in Pref is used for lookup.
  * 
  * @author Cae
  * @author andruid
  */
 
 @xml_inherit
 public class PrefSet extends ElementState implements ApplicationPropertyNames, Cloneable
 {
 	public static final String	PREFS_TRANSLATION_SCOPE	= "PREFS_TRANSLATION_SCOPE";
 
	@xml_map("pref_set")
 	@xml_nowrap
 //	@xml_classes({PrefInt.class})
 	@xml_scope(PREFS_TRANSLATION_SCOPE)
 	HashMap<String, Pref<?>>		preferences;
 
 	/** No-argument constructor for XML translation. */
 	public PrefSet()
 	{
 	}
 
 	/**
 	 * Register the Pref, as well as adding it to the super ArrayListState.
 	 * 
 	 * @param pref
 	 * @return
 	 */
 	public Pref<?> add(Pref<?> pref)
 	{
 		Pref<?> result = preferences.put(pref.key(), pref);
 		pref.register();
 		return result;
 	}
 
 	/**
 	 * Perform custom processing on the newly created child node, just before it is added to this.
 	 * <p/>
 	 * This is part of depth-first traversal during translateFromXML().
 	 * <p/>
 	 * Add the entry to the category map.
 	 * 
 	 * @param child
 	 */
 	// TODO -- get rid of this when we make ArrayListState implement Collection!!!
 	// (cause then this.add() will get called!)
 	@Override
 	protected void createChildHook(ElementState child)
 	{
 		Pref<?> pref = (Pref<?>) child;
 		pref.register();
 	}
 
 	/**
 	 * Read Pref declarations from a file or across the net.
 	 * 
 	 * @param purl
 	 * @param translationSpace
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	public static PrefSet load(ParsedURL purl, TranslationScope translationSpace)
 			throws XMLTranslationException
 	{
 		File file = purl.file();
 		PrefSet pS = null;
 		if ((file != null) && file.exists())
 			pS = (PrefSet) ElementState.translateFromXML(purl, translationSpace);
 
 		return pS;
 	}
 
 	/**
 	 * Read Pref declarations from a file or across the net.
 	 * 
 	 * @param prefXML
 	 *          - Preferences in an XML format; to be translated into a PrefSet.
 	 * @param translationSpace
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	public static PrefSet load(String filename, TranslationScope translationSpace)
 			throws XMLTranslationException
 	{
 		PrefSet pS = (PrefSet) ElementState.translateFromXML(filename, translationSpace);
 
 		return pS;
 	}
 
 	/**
 	 * Read Pref declarations from a file or across the net.
 	 * 
 	 * @param prefXML
 	 *          - Preferences in an XML format; to be translated into a PrefSet.
 	 * @param translationSpace
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	public static PrefSet loadFromCharSequence(String prefXML, TranslationScope translationSpace)
 			throws XMLTranslationException
 	{
 		PrefSet pS = (PrefSet) ElementState.translateFromXMLCharSequence(prefXML, translationSpace);
 
 		return pS;
 	}
 
 	/**
 	 * Remove the Pref from this, and from the global set.
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public Pref<?> clearPref(String key)
 	{
 		Pref.clearPref(key);
 		return preferences.remove(key);
 	}
 
 	/**
 	 * @see ecologylab.xml.types.element.HashMapState#clone()
 	 */
 	@Override
 	public PrefSet clone()
 	{
 		PrefSet retVal = new PrefSet();
 
 		for (Pref<?> p : preferences.values())
 		{
 			retVal.add(p.clone());
 		}
 
 		return retVal;
 	}
 
 	public Collection<Pref<?>> values()
 	{
 		return preferences.values();
 	}
 
 	public void append(PrefSet jNLPPrefSet)
 	{
 		// FIXME: iterate through set and add each to map
 
 	}
 
 	public Set<String> keySet()
 	{
 		return preferences.keySet();
 	}
 
 	public Pref<?> get(String k)
 	{
 		return preferences.get(k);
 	}
 
 	public void put(String k, Pref<?> object)
 	{
 		preferences.put(k, object);
 
 	}
 
 	public boolean containsKey(String key)
 	{
 		return preferences.containsKey(key);
 
 	}
 
 	/**
 	 * Add another prefSet to this one.
 	 * 
 	 * @param prefSet
 	 */
 	public void addPrefSet(PrefSet prefSet)
 	{
 		for (Pref<?> pref : prefSet.values())
 			add(pref);
 	}
 
 }
