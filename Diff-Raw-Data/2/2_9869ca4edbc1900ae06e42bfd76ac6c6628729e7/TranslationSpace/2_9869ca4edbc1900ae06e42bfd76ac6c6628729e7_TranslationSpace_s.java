 package ecologylab.xml;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import ecologylab.generic.Debug;
 import ecologylab.xml.ElementState.xml_tag;
 import ecologylab.xml.types.scalar.ScalarType;
 import ecologylab.xml.types.scalar.TypeRegistry;
 
 /**
  * A set of translations between XML element names (tags) and associated Java ElementState
  * class names.
  * This is done by maintaining a HashMap containing
  * the name of the class as the key and its package as the value. If no entry is 
  * present in the map, then the default package is returned. This is used when
  * an XML is converted back to its Java State-Object
  */
 public final class TranslationSpace extends Debug
 {
    protected String			name;
    /**
 	* The default package. If an entry for a class is not found in the hashtable,
 	* this package name is returned.
 	*/
    private String			defaultPackageName;
    
    /**
 	* This boolean controls whether package names are added to the class names
 	* in the corresponding XML file. If its false, the package names are not added, otherwise
 	* every class name is prepended by its package name.
 	*/
    private boolean			emitPackageNames	= false;
    
    /**
     * Fundamentally, a TranslationSpace consists of a set of class simple names.
     * These are mapped to tag names (camel case conversion), and to Class objects.
     * Because there are many packages, globally, there could be more than one class
     * with one single name.
     * <p/>
     * Among other things, a TranslationSpace tells us *which* package's version will be used,
     * if there are multiple possibilities. This is the case when internal and external versions of
     * a message and its constituents are defined for a messaging API.
     */
    private HashMap<String, TranslationEntry>	entriesByClassSimpleName	= new HashMap<String, TranslationEntry>();
    private HashMap<String, TranslationEntry>	entriesByTag				= new HashMap<String, TranslationEntry>();
    
    private final HashMap<String, Class<? extends ElementState>>	nameSpaceClassesByURN = new HashMap<String, Class<? extends ElementState>>();
    
    private static HashMap<String, TranslationSpace>	allTranslationSpaces	= new HashMap<String, TranslationSpace>();
       
    /**
     * Create a new space that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * 
     * @param name
     */
    private TranslationSpace(String name)
    {
 	  this.name	= name;
 	  allTranslationSpaces.put(name, this);
    }
 
    /**
     * Create a new TranslationSpace that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * Begin by copying in the translations from another, pre-existing "base" TranslationSpace.
     * 
     * @param name
     * @param inheritedTranslations
     */
    private TranslationSpace(String name, TranslationSpace inheritedTranslations)
    {
 	  this(name);
 	  addTranslations(inheritedTranslations);
    }
    
    private TranslationSpace(String name, Class translation, TranslationSpace inheritedTranslations)
    {
 	  this(name, inheritedTranslations);
 	  addTranslation(translation);
    }
   
    /**
     * Create a new TranslationSpace that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * Begin by copying in the translations from another, pre-existing "base" TranslationSpace.
     * 
     * @param name
     * @param baseTranslationSet
     */
    private TranslationSpace(String name, TranslationSpace[] inheritedTranslations)
    {
 	  this(name);
 	  
 	  if (inheritedTranslations != null)
 	  {
 			int n = inheritedTranslations.length;
 			for (int i = 0; i < n; i++)
 				addTranslations(inheritedTranslations[i]);
 		}
    }
    
    /**
     * Create a new TranslationSpace that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * Begin by copying in the translations from another, pre-existing "base" TranslationSpace.
     * 
     * @param name
     * @param baseTranslationSet
     */
    private TranslationSpace(String name, ArrayList<TranslationSpace> baseTranslationsSet)
    {
 	  this(name);
 	  for (TranslationSpace thatTranslationSpace: baseTranslationsSet)
 		  addTranslations(thatTranslationSpace);
    }
    
    /**
     * Create a new space that defines how to translate xml tag names into
     * class names of subclasses of ElementState.
     * 
     * Set a new default packge.
     * 
     * @param name
     * @param defaultPackgeName
     */
    private TranslationSpace(String name, String defaultPackgeName)
    {
 	   this(name);
 	   this.setDefaultPackageName(defaultPackgeName);
    }
    
    /**
      * Create a new space that defines how to translate xml tag names into
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
     private TranslationSpace(String name, Class[] translations, 
  					String defaultPackgeName)
     {
  	   this(name, translations, (TranslationSpace[]) null, defaultPackgeName);
     }
    
    /**
     * Construct a new TranslationSpace, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslationsSet
  * @param defaultPackgeName
     */
    private TranslationSpace(String name, Class[] translations, TranslationSpace[] inheritedTranslationsSet,
 		   String defaultPackgeName)
    {
 	   this(name, inheritedTranslationsSet);
 	   this.setDefaultPackageName(defaultPackgeName);
 	   addTranslations(translations);
    }
    /**
     * Construct a new TranslationSpace, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * 
     * @param name
  * @param translations
  * @param defaultPackgeName
  * @param baseTranslations
     */
    private TranslationSpace(String name, Class[] translations, ArrayList<TranslationSpace> inheritedTranslationsSet,
 		   String defaultPackgeName)
    {
 	   this(name, inheritedTranslationsSet);
 	   this.setDefaultPackageName(defaultPackgeName);
 	   addTranslations(translations);
    }
    
    /**
     * Construct a new TranslationSpace, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslations
  * @param defaultPackgeName
     */
    private TranslationSpace(String name, Class[] translations, TranslationSpace inheritedTranslations,
 		   String defaultPackgeName)
    {
 	   this(name, inheritedTranslations);
 	   this.setDefaultPackageName(defaultPackgeName);
 	   addTranslations(translations);
    }
    /**
     * Construct a new TranslationSpace, with this name, using the baseTranslations first.
     * Then, add the array of translations, then, make the defaultPackageName available.
     * Map XML Namespace declarations.
     *      
     * @param name
  * @param translations
  * @param inheritedTranslations
  * @param defaultPackgeName
  * @param nameSpaceDecls
     */
    private TranslationSpace(String name, Class[] translations, TranslationSpace[] inheritedTranslations,
 		   String defaultPackgeName, NameSpaceDecl[] nameSpaceDecls)
    {
 	   this(name, translations, inheritedTranslations, defaultPackgeName);
 	   addNameSpaceDecls(nameSpaceDecls);
    }
    
    /**
     * Map XML Namespace ElementState subclasses to URIs.
     * 
     * @param nameSpaceDecls
     */
    private void addNameSpaceDecls(NameSpaceDecl[] nameSpaceDecls)
    {
 	   for (NameSpaceDecl nsd: nameSpaceDecls)
 	   {
 		   nameSpaceClassesByURN.put(nsd.urn, nsd.esClass);
 	   }
    }
       
    /**
     * Add translations, where each translation is defined by an actual Class object.
     * We can get both the class name and the package name from the Class object.
     * 
     * @param classes
     */
    private void addTranslations(Class[] classes)
    {
 	   if (classes != null)
 	   {
 		   int numClasses	= classes.length;
 		   for (int i=0; i<numClasses; i++)
 		   {
 			   Class thatClass	= classes[i];
 			   addTranslation(thatClass);
 		   }
 	   }
    }
    
    /**
     * Utility for composing <code>TranslationSpace</code>s.
     * Performs composition by value. That is, the entries are copied.
     * 
     * Unlike in union(), if there are duplicates, they will override identical entries in this.
     * 
     * @param otherTranslations
     */
    private void addTranslations(TranslationSpace otherTranslations)
    {
 	   if (otherTranslations != null)
 	   {
		   for (TranslationEntry translationEntry: entriesByClassSimpleName.values())
 		   {
 			   TranslationEntry existingEntry	= entriesByClassSimpleName.get(translationEntry.classSimpleName);
 
                final boolean entryExists		= existingEntry != null;
                final boolean newEntry			= existingEntry != translationEntry;
                if (entryExists && newEntry)	// look out for redundant entries
 				   warning("Overriding with " + translationEntry);
 
                if (!entryExists || newEntry)
             	   addTranslation(translationEntry);
 		   }
 	   }
    }
    /**
 	* Add a translation table entry for an ElementState derived sub-class.
 	* Assumes that the xmlTag can be derived automatically from the className,
 	* by translating case-based separators to "_"-based separators.
 	* 
 	* @param classObj		The object for the class.
 	*/
    public void addTranslation(Class classObj)
    {
 	   new TranslationEntry(classObj);
    }
 
    
    /**
     * Add translations, where each translation can be heterogeneously either a Class object,
     * or an array with 2 String elements, package name and class name.
     * 
     * @param translations
     */
    public void addTranslations(Object[] translations)
    {
 	   if (translations != null)
 	   {
 		   int		numTranslations	= translations.length;
 		   for (int i=0; i< numTranslations; i++)
 		   {
 			   Object thatTranslation		= translations[i];
 			   if (thatTranslation instanceof String[])
 			   {
 				   String[] thatStringTrans	= (String[]) thatTranslation;
 				   addTranslation(thatStringTrans[0], thatStringTrans[1]);
 			   }
 			   else if (thatTranslation instanceof Class)
 			   {
 				   addTranslation((Class) thatTranslation);
 			   }
 			  }
 		  }
 	   }
 
    /**
 	* Add a translation table entry for an ElementState derived sub-class.
 	* Assumes that the xmlTag can be derived automatically from the className,
 	* by translating case-based separators to "_"-based separators.
 	* 
 	* @param packageName	Package that the class lives in.
 	* @param className		Name of the class.
 	*/
    public void addTranslation(String packageName, String className)
    {
    	  new TranslationEntry(packageName, className);
    }
 
    /**
 	* Add a translation table entry for an ElementState derived sub-class.
 	* Use this signature when the xmlTag cannot be generated automatically from the className.
 	* 
 	* @param packageName	Package that the class lives in.
 	* @param className		Name of the class.
 	* @param xmlTag		XML tag that the class maps to.
 	*/
    public void addTranslation(String packageName, String className,
 							  String xmlTag)
    {
 //	  debugA("addTranslation: "+ className " : " + packageName);
 	  new TranslationEntry(packageName, className, xmlTag, null);
    }
    
    private void addTranslation(TranslationEntry translationEntry)
    {
 	   this.entriesByTag.put(translationEntry.tag, translationEntry);
 	   this.entriesByClassSimpleName.put(translationEntry.classSimpleName, translationEntry);
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
 	* creates a <code>Class</code> object from a given element name (aka tag) in the xml.
 	* Also keeps it in the hashtable, so that when requested for the same class again
 	* it doesnt have to create one.
 	* @param xmlTag	name of the state class along with its package name
 	* @return 						a <code>Class</code> object for the given state class
 	*/
    public Class xmlTagToClass(String xmlTag)
    {
 	  TranslationEntry entry		= entriesByTag.get(xmlTag);
 
 	  if (entry == null)
 	  {
 		 String className	= XMLTools.classNameFromElementName(xmlTag);
 		 if (defaultPackageName != null)
 		 {
 			 String packageName = defaultPackageName;
 			 entry				= new TranslationEntry(packageName, className, xmlTag, null);
 		 }
 		 else
 		 {
 			 // empty entry construction added by andruid 11/11/07
 			 entry				= new TranslationEntry(xmlTag);	// new empty entry
 			 return null;
 		 }
 		 String classSimpleName	= XMLTools.classNameFromElementName(xmlTag);
 		 String packageName = defaultPackageName;
 		 if (packageName != null)
 			 entry				= new TranslationEntry(packageName, classSimpleName, xmlTag, null);
 		 else
 			 entry				= new TranslationEntry(xmlTag);	// new empty entry
 	  }
 	  if (entry.empty)
 	  {
 //		 debug("using memorized no mapping for " + xmlTag);
 		 return null;
 	  }
 	  return entry.thisClass;
    }
    /**
     * Get the Class object associated with this tag, if there is one.
     * Unlike xmlTagToClass, this call will not generate a new blank NameEntry.
     * @param tag
     * @return
     */
    Class getClassByTag(String tag)
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
    Class getClassBySimpleName(String classSimpleName)
    {
 	   TranslationEntry entry		= entriesByClassSimpleName.get(classSimpleName);
 	   
 	   return (entry == null) ? null : entry.thisClass;
    }
    /**
     * Use this TranslationSpace to lookup a class that has the same simple name
     * as the argument passed in here. It may have a different full name, that is,
     * a different package, which could be quite convenient for overriding with 
     * subclasses.
     * 
     * @param thatClass
     * @return
     */
    Class getClassBySimpleNameOfClass(Class thatClass)
    {
 	   return getClassBySimpleName(classSimpleName(thatClass));
    }
 
 /**
  * Find an appropriate XML tag name, based on the type of the object passed.
  * 
  * @param object	Object whose type becomes the basis for the tag name we derive.
  * @deprecated appears to no longer be used -Zach
  */   
    @Deprecated public String objectToXmlTag(Object object)
    {
 	  return classToXmlTag(object.getClass());
    }
    /**
     * Find an appropriate XML tag name, based on the class object passed.
     * 
     * @param classObj	The type which becomes the basis for the tag name we derive.
     * @deprecated appears to no longer be used -Zach
     */   
    @Deprecated public String classToXmlTag(Class classObj)
    {
 	  String className	= classObj.getName();
 	  TranslationEntry entry	= entriesByClassSimpleName.get(className);
 	  if (entry == null)
 	  {
 	  	 synchronized (this) 
 	  	 {
 	  	 	 entry	= entriesByClassSimpleName.get(className);
 	  	 	 if (entry == null)
 	  	 	 {
 				 String packageName = classObj.getPackage().getName();
 				 int index			= className.lastIndexOf('.') + 1;
 				 className			= className.substring(index);
 				 entry				= new TranslationEntry(packageName, className);
 	  	 	 }
 	  	 }
 	  }
 	  return entry.getTag();
    }
    
    /**
 	* @return	true	If package names should be emitted as part of tags 
 	*  while translating to XML.
 	*					This corresponds to quite verbose XML.
 	*/
    public boolean emitPackageNames()
    {
 	  return emitPackageNames;
    }
 
    /**
     * Get the values in the entriesByClass HashMap, and form an Iterator for acessing them.
     * 
     * @return
     */
    private Iterator<TranslationEntry> entriesByClassIterator()
    {
 	   Collection<TranslationEntry> values = entriesByClassSimpleName.values();
 	   return values.iterator();
    }
 
    /**
     * Integrate xml tag into tag derivation.
     * 
     * @param classObj
     * @param classSimpleName
     * @return
     */
    private static String determineXMLTag(Class<?> classObj, String classSimpleName)
    {
        return classObj.isAnnotationPresent(xml_tag.class) ? classObj.getAnnotation(xml_tag.class).value() : 
          XMLTools.getXmlTagName(classSimpleName, "State");
    }
    
    public class TranslationEntry extends Debug
    {
 	  public final String		packageName;
 	  /**
 	   * Should this be whole class name, or simple/short class name?
 	   */
 	  public final String		classSimpleName;
 	  
 	  public final String		classWholeName;
 	  public final String		tag;
 
 	  public final String		dottedPackageName;
 	  public final String		tagWithPackage;
 	  public final Class<?>		thisClass;
 
 	  boolean					empty;
 	  
 	  /**
 	   * Construct an empty entry for tag.
 	   * This means that the TranslationSpace will map tag to no class forever.
 	   * 
 	   * @param tag
 	   */
 	  public TranslationEntry(String tag)
 	  {
 		  this.tag				= tag;
 		  this.empty			= true;
 		  this.packageName		= null;
 		  this.classSimpleName	= null;
 		  this.classWholeName	= null;
 		  this.dottedPackageName= null;
 		  this.tagWithPackage	= null;
 		  this.thisClass		= null;
 		  
 		  entriesByTag.put(tag, this);
 	  }
 	  /**
 	   * Create the entry by package name and class name.
 	   */
 	  public TranslationEntry(String packageName, String className)
 	  {
 		  this(packageName, className,
 				  XMLTools.getXmlTagName(className, "State"), null);
 	  }
 	  /**
 	   * Create the entry by package name and class name.
 	   */
 	  public TranslationEntry(Class<?> thisClass)
 	  {
 		  this(thisClass.getPackage().getName(), 
 			   thisClass.getSimpleName(), 
 			   thisClass.getName(),
 			   determineXMLTag(thisClass, thisClass.getSimpleName()), 
 			   thisClass);
 	  }
 
     
 	  public TranslationEntry(String packageName, String classSimpleName, 
 					   		  String tag, Class<?> thisClass)
 	  {
 	  	 this(packageName, classSimpleName, (packageName == null) ? null : packageName + "." + classSimpleName, 
 	  		  tag, thisClass);
 	  }
 	  
 	  public TranslationEntry(String packageName, String classSimpleName, String classWholeName,
 			   				  String tag, Class<?> thisClass)
 {
 		 this.packageName		= packageName;
 		 /*
              * changed by andruid 5/5/07 the thinking is that there is only one possible simple class name per tag per
              * TranslationSpace, so we don't ever need the whole class name. and by ussing the short one, we will be
              * able to support fancy overriding properly, where you change (override!) the mapping of a simple name,
              * because the package and whole name are differrent.
              */
 		 this.classSimpleName	= classSimpleName;
 		 this.classWholeName	= classWholeName;
 //		 this.className			= wholeClassName;
 		 this.tag				= tag;
 		 if (packageName != null)
 		 {
 			 String dottedPackageName	= packageName + ".";
 			 this.dottedPackageName	= dottedPackageName;
 			 this.tagWithPackage	= dottedPackageName + tag;
 		 }
 		 else
 		 {
 			 this.dottedPackageName	= null;
 			 this.tagWithPackage	= null;
 			 
 		 }
 		 if ((thisClass == null) && (classWholeName != null))
 		 {
 			 try
 			 {  
 				thisClass		= Class.forName(classWholeName);
 			 } catch (ClassNotFoundException e)
 			 {
 				// maybe we need to use State
 				try
 				{
 				   thisClass	= Class.forName(classWholeName+"State");
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
 	private void registerTranslation()
 	{
 		registerTranslation(this.tag, this.classSimpleName);
 	}
 	  public String getTag()
 	  {
 		 return emitPackageNames() ? tagWithPackage : tag;
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
    public String toString()
    {
       return "TranslationSpace[" + name +"]";
    }
    /**
     * Find the TranslationSpace called <code>name</code>, if there is one.
     * 
     * @param name
     * @return
     */
    public static TranslationSpace lookup(String name)
    {
 	   return (TranslationSpace) allTranslationSpaces.get(name);
    }
    /**
     * Find the TranslationSpace called <code>name</code>, if there is one.
     * Otherwise, create a new one with this name, and return it.
     * 
     * @param name
     * @return
     */
    public static TranslationSpace get(String name)
    {
 	   TranslationSpace result	= lookup(name);
 	   return (result != null) ? result : new TranslationSpace(name);
    }
    
    /**
     * Look for a TranslationSpace with this name.
     * If you find one, make sure it also has the same defaultPackageName (internal consistency check).
     * Throw a RuntimeExcpection if the consistency check fails.
     * 
     * @param 	name
     * @param 	defaultPackageName
     * @return	existing translations
     */
    private static TranslationSpace lookForExistingTS(String name, String defaultPackageName)
    {
 	   TranslationSpace result	= lookup(name);
 	   if (result != null)
 	   {  // existing TranslationSpace
 		  if (defaultPackageName != null)
 		  {	 // check for package name consistency
 			 String resultDefaultPackageName = result.defaultPackageName;
 			 if ((resultDefaultPackageName != null) && !defaultPackageName.equals(resultDefaultPackageName))
 				throw new RuntimeException("TranslationSpace Consistency Check ERROR: Existing TranslationSpace " + name +
 					   " has defaultPackageName="+resultDefaultPackageName +", not " +defaultPackageName);
 		  }
 		  else
 			  println("Returning existing TranslationSpace; " + name);
 	   }
 	   return result;
    }
    /**
     * Find the TranslationSpace called <code>name</code>, if there is one.
     * It must also have its defaultPackageName = to that passed in as the 2nd argument.
     * If there is no TranslationSpace with this name, create a new one, and set its defaultPackageName.
     * If there is one, but it has the wrong defaultPackageName, then throw a RuntimeException.
     * 
     * @param name
     * @return
     */
    public static TranslationSpace get(String name, String defaultPackageName)
    {
 	   TranslationSpace result	= lookForExistingTS(name, defaultPackageName);
 	   if (result == null)
 	   {
 		   result	= new TranslationSpace(name, defaultPackageName);
 	   }
 	   return result;
    }
 
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * 
     * @param defaultPackageName
     * @param translations
     * @return
     */
    public static TranslationSpace get(String defaultPackageName, Class[] translations)
    {
 	   TranslationSpace result	= lookForExistingTS(defaultPackageName, defaultPackageName);
 	   if (result == null)
 	   {
 		   result		= new TranslationSpace(defaultPackageName, translations, defaultPackageName);
 	   }
 	   return result;	   
    }
    
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Inherit from the previous TranslationSpace, by including all mappings from there.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslations
  * @param defaultPackageName
     * @return
     */
    public static TranslationSpace get(String name, Class[] translations, TranslationSpace inheritedTranslations,
 		   							  String defaultPackageName)
    {
 	   TranslationSpace result	= lookForExistingTS(name, defaultPackageName);
 	   if (result == null)
 	   {
 		   result		= new TranslationSpace(name, translations, inheritedTranslations, defaultPackageName);
 	   }
 	   return result;	   
    }
    
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Build on a previous TranslationSpace, by including all mappings from there.
     * Add just a single new class.
     * 
     * @param name
     * @param translation
     * @param inheritedTranslations
     * @return
     */
    public static TranslationSpace get(String name, Class translation, TranslationSpace inheritedTranslations)
    {
 	   TranslationSpace result	= lookForExistingTS(name, null);
 	   if (result == null)
 	   {
 		   result		= new TranslationSpace(name, translation, inheritedTranslations);
 	   }
 	   return result;	   
    }
   
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Build on the previous TranslationSpace, by including all mappings from there.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslations
     * @return
     */
    public static TranslationSpace get(String name, Class[] translations,
 		   							  TranslationSpace inheritedTranslations)
    {
 	   return get(name, translations, inheritedTranslations, name);
    }
  
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Build on the previous TranslationSpace, by including all mappings from there.
     * 
     * @param name
     * @param translations
     * @param inheritedTranslations
     * @return
     */
    public static TranslationSpace get(String name, Class[] translations,
 			  TranslationSpace[] inheritedTranslations)
 	{
 	return get(name, translations, inheritedTranslations, (String) null);
 	}
 
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Build on a set of inherited TranslationSpaces, by including all mappings from them.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslationsSet
  * @param defaultPackageName
     * @return
     */
    public static TranslationSpace get(String name, Class[] translations, TranslationSpace[] inheritedTranslationsSet,
 		   							  String defaultPackageName)
    {
 	   TranslationSpace result	= lookForExistingTS(name, defaultPackageName);
 	   if (result == null)
 	   {
 		   result		= new TranslationSpace(name, translations, inheritedTranslationsSet, defaultPackageName);
 	   }
 	   return result;	   
    }
 
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Build on a set of inherited TranslationSpaces, by including all mappings from them.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslationsSet
  * @param defaultPackageName
  * @param nameSpaceDecls				Array of ElementState class + URI key map entries for handling XML Namespaces.
     * @return
     */
    public static TranslationSpace get(String name, Class[] translations, TranslationSpace[] inheritedTranslationsSet,
 		   String defaultPackageName, NameSpaceDecl[] nameSpaceDecls)
    {
 	   TranslationSpace result	= lookForExistingTS(name, defaultPackageName);
 	   if (result == null)
 	   {
 		   result		= new TranslationSpace(name, translations, inheritedTranslationsSet, defaultPackageName, nameSpaceDecls);
 	   }
 	   return result;	   
    }
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Build on a set of inherited TranslationSpaces, by including all mappings from them.
     * 
     * @param name
  * @param defaultPackageName
  * @param inheritedTranslationsSet
     * @return
     */
    public static TranslationSpace get(String name, TranslationSpace[] inheritedTranslations, String defaultPackageName)
    {
 	   return get(name, null, inheritedTranslations, defaultPackageName);	   
    }
 
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Build on a set of inherited TranslationSpaces, by including all mappings from them.
     * 
     * @param name
     * @param inheritedTranslations
     * @return
     */
    public static TranslationSpace get(String name, TranslationSpace[] inheritedTranslations)
    {
 	   return get(name, null, inheritedTranslations, null);	   
    }
 
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Build on an inherited TranslationSpaces, by including all mappings from them.
     * 
     * @param name
  * @param inheritedTranslations
  * @param defaultPackageName
     * @return
     */
    public static TranslationSpace get(String name, TranslationSpace inheritedTranslations, String defaultPackageName)
    {
 	   return get(name, null, inheritedTranslations, defaultPackageName);	   
    }
 
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Build on a set of inherited TranslationSpaces, by including all mappings from them.
     * 
     * @param name
     * @param defaultPackageName
     * @param inheritedTranslationsSet
     * @param translations
     * @return
     */
    public static TranslationSpace get(String name, String defaultPackageName, ArrayList<TranslationSpace> inheritedTranslationsSet,
 		   							  Class[] translations)
    {
 	   TranslationSpace result	= lookForExistingTS(name, defaultPackageName);
 	   if (result == null)
 	   {
 		   result		= new TranslationSpace(name, translations, inheritedTranslationsSet, defaultPackageName);
 	   }
 	   return result;	   
    }
    /**
     * Find an existing TranslationSpace by this name, or create a new one.
     * Build on a set of inherited TranslationSpaces, by including all mappings from them.
     * 
     * @param name
  * @param translations
  * @param inheritedTranslationsSet
     * @return
     */
    public static TranslationSpace get(String name, Class[] translations,
 		   							  ArrayList<TranslationSpace> inheritedTranslationsSet)
    {
 	   TranslationSpace result	= lookForExistingTS(name, name);
 	   if (result == null)
 	   {
 		   result		= new TranslationSpace(name, translations, inheritedTranslationsSet, name);
 	   }
 	   return result;	   
    }
    /**
     * Find the TranslationSpace called <code>name</code>, if there is one.
     * It must also have its defaultPackageName = to that passed in as the 2nd argument.
     * If there is no TranslationSpace with this name, create a new one, and set its defaultPackageName.
     * If there is one, but it has the wrong defaultPackageName, then throw a RuntimeException.
     * 
     * Add the translations to the TranslationSpace.
     * 
     * @param name
     * @return Either an existing or new TranslationSpace, with this defaultPackageName, and these translations.
     * A RuntimeException will be thrown if there was already such a TranslationSpace, but with different defaultPackageName.
     */
    public static TranslationSpace get(String name, Class[] translations, String defaultPackageName)
    {
 	  TranslationSpace result	= lookForExistingTS(name, defaultPackageName);
 	  if (result == null)
 	  {
 		  result		= new TranslationSpace(name, translations, defaultPackageName);
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
    ScalarType getType(Class thatClass)
    {
 	   return TypeRegistry.getType(thatClass);
    }
    
    public HashMap entriesByTag()
    {
 	   return entriesByTag;
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
 
 }
