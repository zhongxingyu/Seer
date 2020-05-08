 /**
  * 
  */
 package ecologylab.appframework.types.prefs;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Set;
 
 import ecologylab.appframework.ApplicationPropertyNames;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.simpl_inherit;
 
 /**
  * A serial set of Pref objects. Used for reading and writing (load and save). The static
  * allPrefsMap in Pref is used for lookup.
  * 
  * @author Cae
  * @author andruid
  */
 
 @simpl_inherit
 public class PrefSet extends ElementState implements ApplicationPropertyNames, Cloneable
 {
 	public static final String	PREFS_TRANSLATION_SCOPE	= "PREFS_TRANSLATION_SCOPE";
 
 	@simpl_map
 	@simpl_nowrap
 	// @xml_classes({PrefInt.class})
 	@simpl_scope(PREFS_TRANSLATION_SCOPE)
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
 		constructPreferencesIfNeeded();
 
 		Pref<?> result = preferences.put(pref.key(), pref);
 		pref.register();
 		return result;
 	}
 
 	/**
 	 * 
 	 */
 	private void constructPreferencesIfNeeded()
 	{
 		if (preferences == null)
 			preferences = new HashMap<String, Pref<?>>();
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
 	 * @param translationScope
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	public static PrefSet load(ParsedURL purl, TranslationScope translationScope)
 			throws SIMPLTranslationException
 	{
 		File file = purl.file();
 		PrefSet pS = null;
 		if ((file != null) && file.exists())
 			pS = (PrefSet) translationScope.deserialize(purl);
 
 		return pS;
 	}
 
 	/**
 	 * Read Pref declarations from a file or across the net.
 	 * 
 	 * @param prefXML
 	 *          - Preferences in an XML format; to be translated into a PrefSet.
 	 * @param translationScope
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	public static PrefSet load(String filename, TranslationScope translationScope)
 			throws SIMPLTranslationException
 	{
 		PrefSet pS = (PrefSet) translationScope.deserialize(filename);
 
 		return pS;
 	}
 
 	/**
 	 * Read Pref declarations from a file or across the net.
 	 * 
 	 * @param prefXML
 	 *          - Preferences in an XML format; to be translated into a PrefSet.
 	 * @param translationScope
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	public static PrefSet loadFromCharSequence(String prefXML, TranslationScope translationScope)
 			throws SIMPLTranslationException
 	{
 		PrefSet pS = (PrefSet) translationScope.deserializeCharSequence(prefXML);
 
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
 		return preferences == null ? null : preferences.remove(key);
 	}
 
 	/**
 	 * @see ecologylab.serialization.types.element.HashMapState#clone()
 	 */
 	@Override
 	public PrefSet clone()
 	{
 		PrefSet retVal = new PrefSet();
 
		if (preferences != null)
 		{
 			for (Pref<?> p : preferences.values())
 			{
 				retVal.add(p.clone());
 			}
 		}
 		return retVal;
 	}
 
 	public static final Collection<Pref<?>>	EMPTY_ARRAY_LIST	= new ArrayList<Pref<?>>(0);
 
 	public Collection<Pref<?>> values()
 	{
 		return preferences == null ? EMPTY_ARRAY_LIST : preferences.values();
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
 		return (preferences == null) ? null : preferences.get(k);
 	}
 
 	public void put(String k, Pref<?> object)
 	{
 		constructPreferencesIfNeeded();
 
 		preferences.put(k, object);
 	}
 
 	public boolean containsKey(String key)
 	{
 		return (preferences == null) ? false : preferences.containsKey(key);
 
 	}
 
 	/**
 	 * Add another prefSet to this one.
 	 * 
 	 * @param prefSet
 	 */
 	public void addPrefSet(PrefSet prefSet)
 	{
 		constructPreferencesIfNeeded();
 
 		for (Pref<?> pref : prefSet.values())
 			add(pref);
 	}
 
 	public int size()
 	{
 		return this.preferences.size();
 	}
 }
