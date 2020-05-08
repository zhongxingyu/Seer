 package ecologylab.xml;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import ecologylab.collections.Scope;
 import ecologylab.generic.Debug;
import ecologylab.xml.ElementState.xml_other_tags;
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
    private Scope<TranslationEntry>	entriesByClassSimpleName	= new Scope<TranslationEntry>();
    private Scope<TranslationEntry>	entriesByTag							= new Scope<TranslationEntry>();
    
    private final Scope<Class<? extends ElementState>>	nameSpaceClassesByURN = new Scope<Class<? extends ElementState>>();
    
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
 
    private TranslationScope(String name, TranslationScope inheritedTranslationScope, Class<? extends ElementState> translation)
    {
 	   this(name, inheritedTranslationScope);
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
    private TranslationScope(String name, TranslationScope... inheritedTranslationScopes)
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
    private TranslationScope(String name, Collection<TranslationScope> baseTranslationsSet)
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
     * Set a new default package, and
     * a set of defined translations.
     * 
     * @param name		Name of the TranslationSpace to be 
     *					A key for use in the TranslationSpace registry.
     * @param translations		Set of initially defined translations for this.
     * @param defaultPackgeName
     */
    private TranslationScope(String name, Class<? extends ElementState>... translations)
    {
 	   this(name, (TranslationScope[]) null, translations);
    }
 
    /**
     * Construct a new TranslationScope, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * 
     * @param name
    * @param inheritedTranslationScopes
    * @param translations
    * @param defaultPackgeName
     */
    private TranslationScope(String name, TranslationScope[] inheritedTranslationScopes, Class<? extends ElementState>[] translations)
    {
 	   this(name, inheritedTranslationScopes);
 	   addTranslations(translations);
    }
    /**
     * Construct a new TranslationScope, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * 
     * @param name
    * @param translations
    * @param baseTranslations
     */
    private TranslationScope(String name, Collection<TranslationScope> inheritedTranslationsSet, Class<? extends ElementState>[] translations)
    {
 	   this(name, inheritedTranslationsSet);
 	   addTranslations(translations);
    }
    
    /**
     * Construct a new TranslationScope, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * 
     * @param name
    * @param inheritedTranslationScope
    * @param translations
     */
    private TranslationScope(String name, TranslationScope inheritedTranslationScope, Class<? extends ElementState>[] translations)
    {
 	   this(name, inheritedTranslationScope);
 	   addTranslations(translations);
    }
 
    /**
     * Construct a new TranslationScope, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * Map XML Namespace declarations.
     *      
     * @param name
    * @param nameSpaceDecls
    * @param inheritedTranslationScopes
    * @param translations
    * @param defaultPackgeName
     */
    private TranslationScope(String name, NameSpaceDecl[] nameSpaceDecls, TranslationScope[] inheritedTranslationScopes, Class<? extends ElementState>[] translations)
    {
 	   this(name, inheritedTranslationScopes, translations);
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
 	  return (entry == null || entry.empty) ? null : entry.thisClass;
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
 	   return entriesByTag.get(xmlTag);
 /*
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
  */
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
    public String getTag(Class<? extends ElementState> thatClass)
    {
   	 return getTagBySimpleName(classSimpleName(thatClass));
    }
 
    public String getTagBySimpleName(String simpleName)
    {
 	   TranslationEntry entry		= entriesByClassSimpleName.get(simpleName);
 	   
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
 	    * Create the entry, deriving all from its class object, 
 	    * perhaps including an @xml_tag declaration.
 	    */
 	   TranslationEntry(Class<? extends ElementState> thisClass)
 	   {
 	  	 assert(thisClass != null);
 
 	  	 this.thisClass			= thisClass;
 		   this.classSimpleName	= thisClass.getSimpleName();
 
 		   String tag						= determineXMLTag(thisClass); // (takes @xml_tag into account)
 		   this.tag							= tag;
 	  	 Package thisPackage	= thisClass.getPackage();
 	  	 this.packageName			= thisPackage != null ? thisPackage.getName() : "";
 
 		   registerTranslation(tag, classSimpleName);
		   
		   //Using this code to get only the declared annotations from the class file
		   ElementState.xml_other_tags otherTagsAnnotation = null;
		   Annotation[] annotations = thisClass.getDeclaredAnnotations();
		   for(Annotation annotation : annotations )
		   {
			   if(annotation.annotationType() == ElementState.xml_other_tags.class){
				   otherTagsAnnotation = (xml_other_tags) annotation;
				   break;
			   }
		   }
		   
		   //commented out since getAnnotation also includes inherited annotations 
		   //ElementState.xml_other_tags otherTagsAnnotation 	= thisClass.getAnnotation(ElementState.xml_other_tags.class);
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
 		   buffy.append("TranslationEntry[").append(classSimpleName).
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
 				   result		= new TranslationScope(name, translations);
 		   }
 	   }
 	   return result;	   
    }
 
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Inherit from the previous TranslationScope, by including all mappings from there.
     * 
     * @param name
     * @param inheritedTranslations
     * @param translations
     * @return
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, TranslationScope inheritedTranslations, Class... translations)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, inheritedTranslations, translations);
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
  * @param inheritedTranslations
  * @param translation
     * @return
     */
    public static TranslationScope get(String name, TranslationScope inheritedTranslations, Class<? extends ElementState> translation)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, inheritedTranslations, translation);
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
 	   return get(name, null, translation);
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
    public static TranslationScope get(String name, TranslationScope[] inheritedTranslationsSet,
 			  Class... translations)
     {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, inheritedTranslationsSet, translations);
 		   }
 	   }
 	   return result;	   
    }
    public static TranslationScope get(String name, TranslationScope inheritedTranslations0,
   		 TranslationScope inheritedTranslations1, Class... translations)
    {
   	 TranslationScope[] inheritedArray	= new TranslationScope[2];
   	 inheritedArray[0]									= inheritedTranslations0;
   	 inheritedArray[1]									= inheritedTranslations1;
   	 return get(name, inheritedArray, translations);
    }
    public static TranslationScope get(String name, NameSpaceDecl[] nameSpaceDecls, Class... translations)
    {
 	   return get(name, nameSpaceDecls, null, translations);
    }
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on a set of inherited TranslationScopes, by including all mappings from them.
     * 
     * @param name
    * @param nameSpaceDecls				Array of ElementState class + URI key map entries for handling XML Namespaces.
    * @param inheritedTranslationsSet
    * @param translations
    * @param defaultPackageName
    * @return
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, NameSpaceDecl[] nameSpaceDecls, TranslationScope[] inheritedTranslationsSet, Class... translations)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, nameSpaceDecls, inheritedTranslationsSet, translations);
 		   }
 	   }
 	   return result;	   
    }
 
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on a set of inherited TranslationScopes, by including all mappings from them.
     * 
     * @param name
     * @param inheritedTranslations
     * @return
     */
    public static TranslationScope get(String name, TranslationScope... inheritedTranslations)
    {
 	   return get(name, inheritedTranslations, null);	   
    }
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one. Build on a set of
 	 * inherited TranslationScopes, by including all mappings from them.
 	 * 
 	 * This method exists because a call to get(String, TranslationScope, TranslationScope) is
 	 * ambiguous with the (String, TranslationScope, TranslationScope, Class...) method.
 	 * 
 	 * @param name
 	 * @param inheritedTranslations
 	 * @return
 	 */
    public static TranslationScope getWithTwoScopes(String name, TranslationScope... inheritedTranslations)
    {
 	   return get(name, inheritedTranslations);	   
    }
 
    /**
     * Find an existing TranslationScope by this name, or create a new one.
     * Build on a set of inherited TranslationScopes, by including all mappings from them.
     * 
     * @param name
    * @param inheritedTranslationsSet
    * @param translations
  * @return
     */
    @SuppressWarnings("unchecked")
    public static TranslationScope get(String name, Collection<TranslationScope> inheritedTranslationsSet,
   		 Class... translations)
    {
 	   TranslationScope result	= lookup(name);
 	   if (result == null)
 	   {
 		   synchronized (name)
 		   {
 			   result	= lookup(name);
 			   if (result == null)
 				   result		= new TranslationScope(name, inheritedTranslationsSet, translations);
 		   }
 	   }
 	   return result;	   
    }
 
    protected HashMap<String, TranslationEntry> entriesByClassName()
    {
 	   return entriesByClassSimpleName;
    }
    
    public String generateImports()
    {
   	 StringBuilder buffy	= new StringBuilder();
   	 if (inheritedTranslationScopes != null)
   	 {
   		 for (TranslationScope inheritedTScope : inheritedTranslationScopes)
   		 {
   			 inheritedTScope.generateImports(buffy);
   		 }
   	 }
   	 generateImports(buffy);
   	 return buffy.toString();
    }
    protected void generateImports(StringBuilder buffy)
    {
   	 for (TranslationEntry tEntry : entriesByClassSimpleName.values())
   	 {
   		 buffy.append("import ").append(tEntry.packageName).append('.').append(tEntry.classSimpleName).append(";\n");
   	 } 	 
    }
    
    public Collection<TranslationEntry> getEntries()
    {
   	 	return entriesByClassSimpleName.values();
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
