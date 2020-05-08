 /**
  * 
  */
 package ecologylab.appframework.types.prefs;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import ecologylab.net.ParsedURL;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.TranslationScope;
 import ecologylab.xml.XMLTranslationException;
 import ecologylab.xml.xml_inherit;
 /**
  * Groupings of MetaPrefs, by category. Categories
  * are also ordered in a separate ArrayList.
  * Contains functions related to adding MetaPrefs to the
  * grouping, getting MetaPrefs, getting the categories, etc.
  * 
  * @author Cae
  *
  */
 
 @xml_inherit
 public class MetaPrefSet extends ElementState
 {
 	@xml_attribute	String	title;
 	@xml_attribute	int		width;
 	@xml_attribute	int		height;
 	
	@xml_collection()
 	@xml_nowrap
 	@xml_scope(MetaPrefsTranslationScope.NAME)
 	ArrayList<MetaPref> metaPreferences;
 	
     /**
      * HashMap of category Strings to ArrayList of MetaPrefs.
      */
     private HashMap<String, ArrayList<MetaPref>> categoryToMetaPrefs = new HashMap<String, ArrayList<MetaPref>>();
     /**
      * ArrayList of category Strings, in the order they are gotten from
      * the xml file. This is used to order the tabs in GUI creation.
      */
     private ArrayList<String> orderOfTabs = new ArrayList<String>();
     
 	/**
 	 * Perform custom processing on the newly created child node,
 	 * just before it is added to this.
 	 * <p/>
 	 * This is part of depth-first traversal during translateFromXML().
 	 * <p/>
 	 * Add the entry to the category map.
 	 * <p/>
 	 * Also, create a Pref to match the MetaPref child, if there isn't one already.
 	 * 
 	 * @param child
 	 */
 	protected void createChildHook(ElementState child)
 	{
 		MetaPref metaPref = (MetaPref) child;
 		addEntryToCategoryMap(metaPref);
 		metaPref.getAssociatedPref();	// create one if needed
 	}
     
 	/**
 	 * Add entry to the Category-MetaPrefs map.
 	 * @param metaPref
 	 */
 	void addEntryToCategoryMap(MetaPref metaPref)
 	{
         ArrayList<MetaPref> metaPrefList = categoryToMetaPrefs.get(metaPref.getCategory());
         if (metaPrefList == null)
         {
             metaPrefList = new ArrayList<MetaPref>();
             categoryToMetaPrefs.put(metaPref.category, metaPrefList);
             orderOfTabs.add(metaPref.category);
         }
         metaPrefList.add(metaPref);		
 	}
     
 	/**
 	 * 
 	 */
 	public MetaPrefSet() 
 	{
 
 	}
 
     /**
      * Return the ordered list of categories.
      * 
      * @param tabList   String Array that will hold category names.
      * 
      * @return tabList  String Array that will hold category names.
      */
     public String[] getOrderedTabNames(String[] tabList)
     {
         return orderOfTabs.toArray(tabList);
     }
     
     /**
      * Return the number of categories.
      * 
      * @return Number of categories
      */
     public int getNumberOfTabs()
     {
         return orderOfTabs.size();
     }
     
     /**
      * Get the category names in a Set of Strings.
      * 
      * @return String Set of categories.
      */
     public Set<String> getCategories()
     {
         return categoryToMetaPrefs.keySet();
     }
     
     /**
      * Get the MetaPref ArrayList for a category name.
      * 
      * @param cat   Name of category
      * 
      * @return ArrayList of MetaPrefs
      */
     public ArrayList<MetaPref> getMetaPrefListByCategory(String cat)
     {
         return categoryToMetaPrefs.get(cat);
     }
 
     /**
      * Register the MetaPref in the static global map, as well as adding it to the super ArrayListState.
      * @param metaPref
      * @return
      */
     public boolean add(MetaPref metaPref)
     {
         boolean result  = metaPreferences.add(metaPref);
         metaPref.register();
         return result;
     }
     
     /**
      * Read MetaPref declarations from a file or across the net.
      * 
      * @param file
      * @param translationSpace
      * @return
      * @throws XMLTranslationException
      */
     public static MetaPrefSet load(File file, TranslationScope translationSpace) 
     throws XMLTranslationException
     {
 		 return load(new ParsedURL(file), translationSpace);
     	
     }
 	
     /**
      * Read MetaPref declarations from a file or across the net.
      * 
      * @param purl
      * @param translationSpace
      * @return
      * @throws XMLTranslationException
      */
     public static MetaPrefSet load(ParsedURL purl, TranslationScope translationSpace) 
     throws XMLTranslationException
     {
 		 return (MetaPrefSet) ElementState.translateFromXML(purl, translationSpace);
     	
     }
 
 	public int getHeight()
 	{
 		return height;
 	}
 
 	public String getTitle()
 	{
 		return title;
 	}
 
 	public int getWidth()
 	{
 		return width;
 	}
 }
