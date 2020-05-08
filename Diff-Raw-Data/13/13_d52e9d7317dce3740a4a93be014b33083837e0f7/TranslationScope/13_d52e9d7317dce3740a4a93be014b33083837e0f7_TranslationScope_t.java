 package ecologylab.xml;
 
import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import ecologylab.generic.Debug;
 import ecologylab.xml.ElementState.xml_tag;
 import ecologylab.xml.types.scalar.ScalarType;
 import ecologylab.xml.types.scalar.TypeRegistry;
 
 /**
  * A set of bindings between XML element names (tags) and associated simple (without package) class names,
  * and associated Java ElementState classes. Inheritance is supported.
  */
 public final class TranslationScope extends Debug
 {
    private final String			name;
    /**
 	* The default package. If an entry for a class is not found in the hashtable,
 	* this package name is returned.
 	*/
    private String				defaultPackageName;
    
    private TranslationScope[]	inheritedTranslationScopes;
    
    /**
     * Fundamentally, a TranslationScope consists of a set of class simple names.
     * These are mapped to tag names (camel case conversion), and to Class objects.
     * Because there are many packages, globally, there could be more than one class
     * with one single name.
     * <p/>
     * Among other things, a TranslationScope tells us *which* package's version will be used,
     * if there are multiple possibilities. This is the case when internal and external versions of
     * a message and its constituents are defined for a messaging API.
     */
    private HashMap<String, TranslationEntry>	entriesByClassSimpleName	= new HashMap<String, TranslationEntry>();
    private HashMap<String, TranslationEntry>	entriesByTag				= new HashMap<String, TranslationEntry>();
    
    private final HashMap<String, Class<? extends ElementState>>	nameSpaceClassesByURN = new HashMap<String, Class<? extends ElementState>>();
    
    private static HashMap<String, TranslationScope>	allTranslationScopes	= new HashMap<String, TranslationScope>();
       
    /**
     * Create a new TranslationScope that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * 
     * @param name
     */
    private TranslationScope(String name)
    {
 	  this.name	= name;
 	  allTranslationScopes.put(name, this);
    }
 
    /**
     * Create a new TranslationScope that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * Begin by copying in the translations from another, pre-existing "base" TranslationScope.
     * 
     * @param name
     * @param inheritedTranslationScope
     */
    private TranslationScope(String name, TranslationScope inheritedTranslationScope)
    {
 	   this(name);
 	   addTranslations(inheritedTranslationScope);
 	   TranslationScope[] inheritedTranslationScopes	= new TranslationScope[1];
 	   inheritedTranslationScopes[0]					= inheritedTranslationScope;
 	   this.inheritedTranslationScopes					= inheritedTranslationScopes;
    }
 
    private TranslationScope(String name, Class<? extends ElementState> translation, TranslationScope inheritedTranslationScopes)
    {
 	   this(name, inheritedTranslationScopes);
 	   addTranslation(translation);
    }
 
    /**
     * Create a new TranslationScope that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * Begin by copying in the translations from another, pre-existing "base" TranslationScope.
     * 
     * @param name
     * @param baseTranslationSet
     */
    private TranslationScope(String name, TranslationScope[] inheritedTranslationScopes)
    {
 	   this(name);
 
 	   if (inheritedTranslationScopes != null)
 	   {
 		   this.inheritedTranslationScopes		= inheritedTranslationScopes;
 		   int n = inheritedTranslationScopes.length;
 		   for (int i = 0; i < n; i++)
 			   addTranslations(inheritedTranslationScopes[i]);
 	   }
    }
    
    /**
     * Create a new TranslationScope that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * Begin by copying in the translations from another, pre-existing "base" TranslationScope.
     * 
     * @param name
     * @param baseTranslationSet
     */
    private TranslationScope(String name, ArrayList<TranslationScope> baseTranslationsSet)
    {
 	  this(name);
 	  for (TranslationScope thatTranslationScope: baseTranslationsSet)
 		  addTranslations(thatTranslationScope);
 	  inheritedTranslationScopes		= (TranslationScope[]) baseTranslationsSet.toArray();
    }
    
    /**
     * Create a new TranslationScope that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * 
     * Set a new default package.
     * 
     * @param name
     * @param defaultPackgeName
     */
    private TranslationScope(String name, String defaultPackgeName)
    {
 	   this(name);
 	   this.setDefaultPackageName(defaultPackgeName);
    }
    
    /**
     * Create a new TranslationScope that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * 
     * Set a new default package, and
     * a set of defined translations.
     * 
     * @param name		Name of the TranslationSpace to be 
     *					A key for use in the TranslationSpace registry.
     * @param translations		Set of initially defined translations for this.
     * @param defaultPackgeName
     */
    private TranslationScope(String name, Class<? extends ElementState>[] translations, 
 		   String defaultPackgeName)
    {
 	   this(name, translations, (TranslationScope[]) null, defaultPackgeName);
    }
 
    /**
     * Construct a new TranslationScope, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslationScopes
  * @param defaultPackgeName
     */
    private TranslationScope(String name, Class<? extends ElementState>[] translations, TranslationScope[] inheritedTranslationScopes,
 		   String defaultPackgeName)
    {
 	   this(name, inheritedTranslationScopes);
 	   this.setDefaultPackageName(defaultPackgeName);
 	   addTranslations(translations);
    }
    /**
     * Construct a new TranslationScope, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * 
     * @param name
  * @param translations
  * @param defaultPackgeName
  * @param baseTranslations
     */
    private TranslationScope(String name, Class<? extends ElementState>[] translations, ArrayList<TranslationScope> inheritedTranslationsSet,
 		   String defaultPackgeName)
    {
 	   this(name, inheritedTranslationsSet);
 	   this.setDefaultPackageName(defaultPackgeName);
 	   addTranslations(translations);
    }
    
    /**
     * Construct a new TranslationScope, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * 
     * @param name
     * @param translations
     * @param inheritedTranslationScope
     * @param defaultPackgeName
     */
    private TranslationScope(String name, Class<? extends ElementState>[] translations, TranslationScope inheritedTranslationScope,
 		   String defaultPackgeName)
    {
 	   this(name, inheritedTranslationScope);
 	   this.setDefaultPackageName(defaultPackgeName);
 	   addTranslations(translations);
    }
 
    /**
     * Construct a new TranslationScope, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * Map XML Namespace declarations.
     *      
     * @param name
     * @param translations
     * @param inheritedTranslationScopes
     * @param defaultPackgeName
     * @param nameSpaceDecls
     */
    private TranslationScope(String name, Class<? extends ElementState>[] translations, TranslationScope[] inheritedTranslationScopes,
 		   String defaultPackgeName, NameSpaceDecl[] nameSpaceDecls)
    {
 	   this(name, translations, inheritedTranslationScopes, defaultPackgeName);
 	   addNameSpaceDecls(nameSpaceDecls);
    }
    
    /**
     * Map XML Namespace ElementState subclasses to URIs.
     * 
     * @param nameSpaceDecls
     */
    private void addNameSpaceDecls(NameSpaceDecl[] nameSpaceDecls)
    {
 	   if (nameSpaceDecls != null)
 		   for (NameSpaceDecl nsd: nameSpaceDecls)
 		   {
 			   registerNameSpaceDecl(nsd);
 		   }
    }
    
    /**
     * Enter a NameSpaceDecl into nameSpaceClassesByURN.
     * @param nsd
     */
    private void registerNameSpaceDecl(NameSpaceDecl nsd)
    {
 	   nameSpaceClassesByURN.put(nsd.urn, nsd.esClass);
    }
       
    /**
     * Add translations, where each translation is defined by an actual Class object.
     * We can get both the class name and the package name from the Class object.
     * 
     * @param classes
     */
    private void addTranslations(Class<? extends ElementState>[] classes)
    {
 	   if (classes != null)
 	   {
 		   int numClasses	= classes.length;
 		   for (int i=0; i<numClasses; i++)
 		   {
 			   Class<? extends ElementState> thatClass	= classes[i];
 			   addTranslation(thatClass);
 		   }
 	   }
    }
 
    /**
     * Utility for composing <code>TranslationScope</code>s.
     * Performs composition by value. That is, the entries are copied.
     * 
     * Unlike in union(), if there are duplicates, they will override identical entries in this.
     * 
     * @param inheritedTranslationScope
     */
    private void addTranslations(TranslationScope inheritedTranslationScope)
    {
 	   if (inheritedTranslationScope != null)
 	   {
 		   // copy map entries from inherited maps into new maps
 		   updateMapWithValues(inheritedTranslationScope.entriesByClassSimpleName, entriesByClassSimpleName, "classSimpleName");
 		   updateMapWithValues(inheritedTranslationScope.entriesByTag, entriesByTag, "tagName");
 		   
 		   HashMap<String, Class<? extends ElementState>> inheritedNameSpaceClassesByURN = inheritedTranslationScope.nameSpaceClassesByURN;
 		   if (inheritedNameSpaceClassesByURN != null)
 		   {
 			   for (String urn : inheritedNameSpaceClassesByURN.keySet())
 			   {
 				   nameSpaceClassesByURN.put(urn, inheritedNameSpaceClassesByURN.get(urn));
 			   }
 		   }
 	   }
    }
    
    /**
     * Update the Map with all the entries in the inherited Map.
     * 
     * @param inheritedMap
  * @param warn
     */
    private void updateMapWithValues(Map<String, TranslationEntry> inheritedMap, Map<String, TranslationEntry> newMap, String warn)
    {
    	// XXX ANDRUID + ZACH -> concurrent modification exception can occur here (for loop) if inheritedMap is modified elsewhere
 	   for (String key : inheritedMap.keySet())
 	   {
 		   TranslationEntry translationEntry	= inheritedMap.get(key);
 		   updateMapWithEntry(newMap, key, translationEntry, warn);
 	   }
    }
 
    /**
     * Update the Map with the entry.
     * 
     * @param newMap
     * @param key
     * @param translationEntry		Must be non-null.
     * @param warn
     */
    private void updateMapWithEntry(Map<String, TranslationEntry> newMap, String key, TranslationEntry translationEntry, String warn)
    {
 	   TranslationEntry existingEntry	= newMap.get(key);
 
 //	   final boolean entryExists		= existingEntry != null;
 //	   final boolean newEntry			= existingEntry != translationEntry;
 
 	   final boolean entryExists		= existingEntry != null;
 	   final boolean newEntry			= !entryExists ? true : existingEntry.thisClass != translationEntry.thisClass;
 	   
 	   if (newEntry)
 	   {
 		   if (entryExists)	// look out for redundant entries
 			   warning("Overriding " + warn + " " + key + " with " + translationEntry);
 		   
 		   newMap.put(key, translationEntry);
 	   }
 //	   if (entryExists && newEntry)	// look out for redundant entries
 //		   warning("Overriding " + warn + " " + key + " with " + translationEntry);
 //
 //	   if (/** !entryExists || **/ newEntry)
 //		   newMap.put(key, translationEntry);
    }
    /**
 	* Add a translation table entry for an ElementState derived sub-class.
 	* Assumes that the xmlTag can be derived automatically from the className,
 	* by translating case-based separators to "_"-based separators.
 	* 
 	* @param classObj		The object for the class.
 	*/
    public void addTranslation(Class<? extends ElementState> classObj)
    {
 	   new TranslationEntry(classObj);
    }
 
    /**
     * Find a prior TranslationEntry for thatClass.
     * Add an alternative tag mapping for it.
     * 
     * @param thatClass
     * @param alternativeXmlTag
     */
    public void addTranslation(Class<? extends ElementState> thatClass, String alternativeXmlTag)
    {
 	   String classSimpleName		= thatClass.getSimpleName();
 	   TranslationEntry thatEntry	= entriesByClassSimpleName.get(classSimpleName);
 	   entriesByTag.put(alternativeXmlTag, thatEntry);
    }
    
    /**
 	* Set the default package name for XML tag to ElementState sub-class translations.
 	* 
 	* @param packageName	The new default package name.
 	*/
    private void setDefaultPackageName(String packageName)
    {
 	  defaultPackageName	= packageName;
    }
    /**
 	* Look-up a <code>Class</code> object for the xmlTag, using translations in this,
 	* and in inherited TranslationScopes.
 	* Will use defaultPackage name here and, recursivley, in inherited scopes, as necessary.
 	* 
 	* @param	xmlTag	XML node name that we're seeking a Class for.
 	* @return 			Class object, or null if there is no associated translation.
 	*/
    public Class<? extends ElementState>  xmlTagToClass(String xmlTag)
    {
 	  TranslationEntry entry = xmlTagToTranslationEntry(xmlTag);
 	  return entry.empty ? null : entry.thisClass;
    }
 
    /**
     * Seek the entry associated with the tag.
     * Recurse through inherited TranslationScopes, if necessary.
     * 
     * @param xmlTag
     * @return
     */
    private TranslationEntry xmlTagToTranslationEntry(String xmlTag)
    {
 	   TranslationEntry entry		= entriesByTag.get(xmlTag);
 
 	   if (entry == null)
 	   {
 		   String defaultPackageName	= this.defaultPackageName;
 		   if (defaultPackageName != null)
 		   {
 			   String classSimpleName	= XMLTools.classNameFromElementName(xmlTag);
 			   entry					= new TranslationEntry(defaultPackageName, classSimpleName, xmlTag);
 			   if (entry.empty)
 			   {
 				   if (inheritedTranslationScopes != null)
 				   {   // recurse through inherited, continuing to seek a translation
 					   for (TranslationScope inherited : inheritedTranslationScopes)
 					   {
 						   entry			= inherited.xmlTagToTranslationEntry(xmlTag);
 						   if (entry != null)
 						   {   // got one from an inherited TranslationScope
 							   // register translation for the inherited entry in this
 							   entriesByTag.put(xmlTag, entry);
 							   entriesByClassSimpleName.put(classSimpleName, entry);
 							   break;
 						   }
 					   }
 				   }
 			   }
 		   }
 		   else
 		   {
 			   // empty entry construction added by andruid 11/11/07
 			   entry					= new TranslationEntry(xmlTag);	// new empty entry
 		   }
 	   }
 	   return entry;
    }
 
    /**
     * Get the Class object associated with this tag, if there is one.
     * Unlike xmlTagToClass, this call will not generate a new blank NameEntry.
     * @param tag
     * @return
     */
    public Class<? extends ElementState> getClassByTag(String tag)
    {
 	   TranslationEntry entry		= entriesByTag.get(tag);
 	   
 	   return (entry == null) ? null : entry.thisClass;
    }
    /**
     * Get the Class object associated with the provided class name, if there is one.
     * Unlike xmlTagToClass, this call will not generate a new blank NameEntry.
     * 
     * @param classSimpleName	Simple name of the class (no package).
     * @return
     */
    public Class<? extends ElementState>  getClassBySimpleName(String classSimpleName)
    {
 	   TranslationEntry entry		= entriesByClassSimpleName.get(classSimpleName);
 	   
 	   return (entry == null) ? null : entry.thisClass;
    }
    /**
     * Use this TranslationScope to lookup a class that has the same simple name
     * as the argument passed in here. It may have a different full name, that is,
     * a different package, which could be quite convenient for overriding with 
     * subclasses.
     * 
     * @param thatClass
     * @return
     */
    public Class<? extends ElementState>  getClassBySimpleNameOfClass(Class<? extends ElementState> thatClass)
    {
 	   return getClassBySimpleName(classSimpleName(thatClass));
    }
    
    /**
     * Lookup the tag for the class in question, using this.
     * 
     * @param thatClass
     * @return
     */
    public String lookupTag(Class<? extends ElementState> thatClass)
    {
 	   TranslationEntry entry		= entriesByClassSimpleName.get(classSimpleName(thatClass));
 	   
 	   return (entry == null) ? null : entry.tag;
    }
 
    /**
     * Derive the XML tag from the Class object, using camel case conversion, or
     * the @xml_tag annotation that may be present in a class declaration.
     * 
     * @param thatClass
     * @return
     */
    private static String determineXMLTag(Class<? extends ElementState>  thatClass)
    {
   	Annotation[] annotations = thatClass.getDeclaredAnnotations();
   	for(Annotation annotation: annotations)
   	{
   		if(annotation.annotationType().equals(xml_tag.class))
   		{
   			return xml_tag.class.cast(annotation).value();
   		}
   	}
      return XMLTools.getXmlTagName(thatClass.getSimpleName(), "State");
    }
    
    public class TranslationEntry extends Debug
    {
 	   public final String		packageName;
 	   /**
 	    * Should this be whole class name, or simple/short class name?
 	    */
 	   public final String		classSimpleName;
 
 	   public final String		tag;
 
 	   public final Class<? extends ElementState>	thisClass;
 
 	   boolean					empty;
 
 	   /**
 	    * Construct an empty entry for tag.
 	    * This means that the TranslationScope will map tag to no class forever.
 	    * 
 	    * @param tag
 	    */
 	   public TranslationEntry(String tag)
 	   {
 		   this.tag				= tag;
 		   this.packageName		= null;
 		   this.classSimpleName	= null;
 		   this.thisClass		= null;
 
 		   this.empty			= true;
 
 		   entriesByTag.put(tag, this);
 	   }
 
 	   /**
 	    * Create the entry, deriving all from its class object, perhaps including an @xml_tag declaration.
 	    */
 	   TranslationEntry(Class<? extends ElementState> thisClass)
 	   {
 		   this(thisClass, determineXMLTag(thisClass));
 	   }
 
 	   /**
 	    * Create the entry using Class object and a tag passed in.
 	    * This is used for alternative mappings.
 	    */
 	   TranslationEntry(Class<? extends ElementState> thisClass, String tag)
 	   {
 		   this(thisClass.getPackage().getName(), 
 				   thisClass.getSimpleName(), 
 				   thisClass.getName(),
 				   tag, 
 				   thisClass);
 	   }
 
 
 	   /**
 	    * Form an entry, using packageName, classSimpleName, and tag.
 	    * It may turn out to be an empty one -- if class.forName() fails.
 	    * 
 	    * @param packageName
 	    * @param classSimpleName
 	    * @param tag
 	    */
 	   TranslationEntry(String packageName, String classSimpleName, String tag)
 	   {
 		   this(packageName, classSimpleName, (packageName == null) ? null : packageName + "." + classSimpleName, 
 				   tag, null);
 	   }
 
 	   @SuppressWarnings("unchecked")
 	   TranslationEntry(String packageName, String classSimpleName, String classWholeName,
 			   String tag, Class<? extends ElementState> thisClass)
 	   {
 		   this.packageName		= packageName;
 		   /*
 		    * changed by andruid 5/5/07 the thinking is that there is only one possible simple class name per tag per
 		    * TranslationScope, so we don't ever need the whole class name. and by ussing the short one, we will be
 		    * able to support fancy overriding properly, where you change (override!) the mapping of a simple name,
 		    * because the package and whole name are differrent.
 		    */
 		   this.classSimpleName	= classSimpleName;
 
 		   this.tag				= tag;
 
 		   // look for a class if we dont' have one already
 		   if ((thisClass == null) && (classWholeName != null))
 		   {
 			   try
 			   {  
 				   thisClass		= (Class<? extends ElementState>) Class.forName(classWholeName);
 			   } catch (ClassNotFoundException e)
 			   {
 				   // maybe we need to use State
 				   try
 				   {
 					   thisClass	= (Class<? extends ElementState>) Class.forName(classWholeName + "State");
 				   } catch (ClassNotFoundException e2)
 				   {
 				   }
 			   }
 		   }
 		   if (thisClass == null)
 		   {
 			   debug("WARNING: can't find class object, create empty entry.");
 
 			   this.empty		= true;
 		   }
 
 		   this.thisClass			= thisClass;
 		   registerTranslation(tag, classSimpleName);
 
 		   if (thisClass != null)
 		   {
 			   ElementState.xml_other_tags otherTagsAnnotation 	= thisClass.getAnnotation(ElementState.xml_other_tags.class);
 			   if (otherTagsAnnotation != null)
 			   {
 				   String[] otherTags	= XMLTools.otherTags(otherTagsAnnotation);
 				   for (String otherTag : otherTags)
 				   {
 					   if ((otherTag != null) && (otherTag.length() > 0))
 					   {
 						   entriesByTag.put(otherTag, this);
 					   }
 				   }
 			   }
 		   }
 	   }
 
 	   /**
 	    * @param tag
 	    * @param classSimpleName
 	    */
 	   private void registerTranslation(String tag, String classSimpleName)
 	   {
 		   entriesByTag.put(tag, this);
 		   entriesByClassSimpleName.put(classSimpleName, this);
 		   /*
 		    * i dont see why this is here. it looks wrong. -- andruid 5/6/07.
 		if (classSimpleName.endsWith("State"))
 		{
 			int beforeState		= classSimpleName.length() - 5;
 			String wholeClassNameNoState = 
 				classSimpleName.substring(0, beforeState);
 //			debug("create entry including " + wholeClassNameNoState);
 			entriesByClassSimpleName.put(wholeClassNameNoState, this);
 		}
 		    */
 	   }
 
 	   public String getTag()
 	   {
 		   return tag;
 	   }
 	   public String toString()
 	   {
 		   StringBuilder buffy = new StringBuilder(50);
 		   buffy.append("NameEntry[").append(classSimpleName).
 		   append(" <").append(tag).append('>');
 		   if (thisClass != null)
 			   buffy.append(' ').append(thisClass);
 		   buffy.append(']');
 		   return XMLTools.toString(buffy);
 	   }
    }
 
    private String toStringCache;
    
    public String toString()
    {
 	   if (toStringCache == null)
 	   {
 		   toStringCache	= "TranslationScope[" + name +"]";
 	   }
       return toStringCache;
    }
    /**
     * Find the TranslationScope called <code>name</code>, if there is one.
     * 
     * @param name
     * @return
     */
    public static TranslationScope lookup(String name)
    {
 	   return (TranslationScope) allTranslationScopes.get(name);
    }
    
    /**
     * Find the TranslationScope called <code>name</code>, if there is one.
     * It must also have its defaultPackageName = to that passed in as the 2nd argument.
     * If there is no TranslationScope with this name, create a new one, and set its defaultPackageName.
     * <p/>
     * If the lookup fails initially, it is performed again, in a synchronized block, using the name String literal
     * as the lock for mutual exclusion. Thus, make sure to use *the same string literal* across calls, to ensure
     * proper concurrency support.
     * 
     * @param name	String literal for lookup of the TranslationScope. 
     * 				Also used for locking construction of the initial instance.
     * @return
     */
    public static TranslationScope get(String name, String defaultPackageName)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result	= new TranslationScope(name, defaultPackageName);
 		   }
 	   }
 	   return result;
    }
 
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * 
     * @param name the name of the TranslationScope
     * @param translations a set of Classes to be used as a part of this TranslationScope
     * @return
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, Class... translations)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, translations, name);
 		   }
 	   }
 	   return result;	   
    }
    
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Inherit from the previous TranslationScope, by including all mappings from there.
     * 
     * @param name
     * @param translations
     * @param inheritedTranslations
     * @param defaultPackageName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, Class[] translations, TranslationScope inheritedTranslations,
 		   							  String defaultPackageName)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, translations, inheritedTranslations, defaultPackageName);
 		   }
 	   }
 	   return result;	   
    }
 
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on a previous TranslationScope, by including all mappings from there.
     * Add just a single new class.
     * 
     * @param name
     * @param translation
     * @param inheritedTranslations
     * @return
     */
    public static TranslationScope get(String name, Class<? extends ElementState> translation, TranslationScope inheritedTranslations)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, translation, inheritedTranslations);
 		   }
 	   }
 	   return result;	   
    }
  
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Add just a single new class.
     * 
     * @param name
     * @param translation
     * @return
     */
    public static TranslationScope get(String name, Class<? extends ElementState> translation)
    {
 	   return get(name, translation, null);
    }
  
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on the previous TranslationScope, by including all mappings from there.
     * 
     * @param name the name of the TranslationScope to acquire.
     * @param translations an array of translations to add to the scope.
     * @param inheritedTranslations a list of previous translation scopes to build upon.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, Class[] translations,
 			  TranslationScope... inheritedTranslations)
    {
 	   return get(name, translations, inheritedTranslations, (String) null);
    }
 
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on a set of inherited TranslationScopes, by including all mappings from them.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslationsSet
  * @param defaultPackageName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, Class[] translations, TranslationScope[] inheritedTranslationsSet,
 		   							  String defaultPackageName)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, translations, inheritedTranslationsSet, defaultPackageName);
 		   }
 	   }
 	   return result;	   
    }
 
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on a set of inherited TranslationScopes, by including all mappings from them.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslationsSet
  * @param defaultPackageName
  * @param nameSpaceDecls				Array of ElementState class + URI key map entries for handling XML Namespaces.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, Class[] translations, TranslationScope[] inheritedTranslationsSet,
 		   String defaultPackageName, NameSpaceDecl[] nameSpaceDecls)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, translations, inheritedTranslationsSet, defaultPackageName, nameSpaceDecls);
 		   }
 	   }
 	   return result;	   
    }
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on a set of inherited TranslationScopes, by including all mappings from them.
     * 
     * @param name
     * @param defaultPackageName
     * @param inheritedTranslationsSet
     * @return
     */
    public static TranslationScope get(String name, TranslationScope[] inheritedTranslations, String defaultPackageName)
    {
 	   return get(name, null, inheritedTranslations, defaultPackageName);	   
    }
 
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on a set of inherited TranslationScopes, by including all mappings from them.
     * 
     * @param name
     * @param inheritedTranslations
     * @return
     */
    public static TranslationScope get(String name, TranslationScope[] inheritedTranslations)
    {
 	   return get(name, null, inheritedTranslations, null);	   
    }
 
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on an inherited TranslationScopes, by including all mappings from them.
     * 
     * @param name
     * @param inheritedTranslations
     * @param defaultPackageName
     * @return
     */
    public static TranslationScope get(String name, TranslationScope inheritedTranslations, String defaultPackageName)
    {
 	   return get(name, null, inheritedTranslations, defaultPackageName);	   
    }
 
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on a set of inherited TranslationScopes, by including all mappings from them.
     * 
     * @param name
     * @param defaultPackageName
     * @param inheritedTranslationsSet
     * @param translations
     * @return
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, String defaultPackageName, ArrayList<TranslationScope> inheritedTranslationsSet,
 		   							  Class[] translations)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, translations, inheritedTranslationsSet, defaultPackageName);
 		   }
 	   }
 	   return result;	   
    }
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on a set of inherited TranslationScopes, by including all mappings from them.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslationsSet
     * @return
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, Class[] translations,
 		   							  ArrayList<TranslationScope> inheritedTranslationsSet)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, translations, inheritedTranslationsSet, name);
 		   }
 	   }
 	   return result;	   
    }
    /**
     * Find the TranslationScope called <code>name</code>, if there is one.
     * It must also have its defaultPackageName = to that passed in as the 2nd argument.
     * If there is no TranslationScope with this name, create a new one, and set its defaultPackageName.
     * If there is one, but it has the wrong defaultPackageName, then throw a RuntimeException.
     * 
     * Add the translations to the TranslationScope.
     * 
     * @param name
     * @return Either an existing or new TranslationScope, with this defaultPackageName, and these translations.
     * A RuntimeException will be thrown if there was already such a TranslationScope, but with different defaultPackageName.
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, Class[] translations, String defaultPackageName)
    {
 	  TranslationScope result	= lookup(name);
 	  if (result == null)
 	  {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, translations, defaultPackageName);
 		   }
 	  }
 	  return result;
    }
 
    protected HashMap<String, TranslationEntry> entriesByClassName()
    {
 	   return entriesByClassSimpleName;
    }
    
 	/**
 	 * Get the Scalar Type corresponding to the Class.
 	 * 
 	 * @param thatClass
 	 * @return	Type associated with thatClass
 	 */
    <U> ScalarType<U> getType(Class<U> thatClass)
    {
 	   return TypeRegistry.getType(thatClass);
    }
    	
    /**
     * Lookup a NameSpace ElementState subclass, with a URN as the key.
     * 
     * @param urn
     * @return
     */
    public Class<? extends ElementState> lookupNameSpaceByURN(String urn)
    {
 	   return nameSpaceClassesByURN.get(urn);
    }
 
    /**
     * 
     * @return	The unique name of this.
     */
    public String getName()
    {
 	   return name;
    }
 
 }
