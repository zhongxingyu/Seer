 package ecologylab.translators.java;
 
 import japa.parser.ParseException;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.FieldTypes;
 import ecologylab.serialization.Hint;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.XMLTools;
 import ecologylab.serialization.simpl_inherit;
 import ecologylab.serialization.ElementState.simpl_composite;
 import ecologylab.serialization.ElementState.simpl_nowrap;
 import ecologylab.serialization.ElementState.simpl_scalar;
 import ecologylab.serialization.ElementState.xml_other_tags;
 import ecologylab.serialization.ElementState.xml_tag;
 import ecologylab.serialization.library.rss.Channel;
 import ecologylab.serialization.library.rss.Item;
 import ecologylab.serialization.library.rss.RssState;
 import ecologylab.serialization.types.element.Mappable;
 import ecologylab.serialization.types.scalar.ScalarType;
 import ecologylab.serialization.types.scalar.TypeRegistry;
 import ecologylab.standalone.xmlpolymorph.BItem;
 import ecologylab.standalone.xmlpolymorph.SchmItem;
 import ecologylab.standalone.xmlpolymorph.Schmannel;
 import ecologylab.translators.parser.JavaDocParser;
 
 /**
  * This class is the main class which provides the functionality of translation of Translation Scope into
  * the Java implementation files.
  * 
  * @author Sumith
  * @version 1.0
  */
 
 public class JavaTranslator implements JavaTranslationConstants
 {
 	/**
 	 * Constructor method
 	 * <p>
 	 * Initializes the {@code nestedTranslationHooks} member of the class
 	 * </p>
 	 */
 	public JavaTranslator()
 	{
 	}
 
 	private HashMap<String, String>	libraryNamespaces						= new HashMap<String, String>();
 
 	private HashMap<String, String>	allNamespaces								= new HashMap<String, String>();
 
 	private String									currentNamespace;
 
 	private boolean									implementMappableInterface	= false;
 
 	private ArrayList<String>				additionalImportLines;
 
 	private ArrayList<ClassDescriptor> 				excludeClassesFromTranslation = new ArrayList<ClassDescriptor>();
 
 	/**
 	 * A method to convert the given class descriptor into the java code
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 * @throws DotNetTranslationException
 	 */
 	private void translateToJava(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException, JavaTranslationException
 	{
 		ClassDescriptor classDescriptor = inputClass;
 
 		HashMapArrayList<String, ? extends FieldDescriptor> fieldDescriptors = classDescriptor
 				.getDeclaredFieldDescriptorsByFieldName();
 
 		StringBuilder classFile = new StringBuilder();
 		StringBuilder header = new StringBuilder();
 
 		addNamespaces(inputClass);
 		openNameSpace(inputClass, header);
 		
 		openClassFile(inputClass, classFile);
 
 		if (fieldDescriptors.size() > 0)
 		{
 			classDescriptor.resolveUnresolvedScopeAnnotationFDs();
 
 			for (FieldDescriptor fieldDescriptor : fieldDescriptors)
 			{
 				if (fieldDescriptor.belongsTo(classDescriptor))
 				{					
 					appendFieldAsJavaAttribute(fieldDescriptor, classFile);						
 				}
 			}
 
 			appendDefaultConstructor(inputClass, classFile);
 
 			for (FieldDescriptor fieldDescriptor : fieldDescriptors)
 			{
 				if (fieldDescriptor.belongsTo(classDescriptor))
 				{					
 					appendGetters(fieldDescriptor, classFile);
 					appendSetters(fieldDescriptor, classFile);					
 				}
 			}
 
 			if (implementMappableInterface)
 			{
 				implementMappableMethods(classFile);
 				implementMappableInterface = false;
 			}
 		}
 
 		closeClassFile(classFile);
 		
 		importNameSpaces(header);
 		appendHeaderComments(inputClass.getDescribedClassSimpleName(), header);
 
 		libraryNamespaces.clear();
 		setAdditionalImportNamespaces(additionalImportLines);
 
 		appendable.append(header);
 		appendable.append(classFile);
 	}
 
 	private void addNamespaces(ClassDescriptor thatClass)
 	{
 		ArrayList<ClassDescriptor> dependencies =  thatClass.getCompositrDependencies();
 		for(ClassDescriptor classDesc : dependencies)
 		{
 			libraryNamespaces.put(classDesc.getDescribedClassPackageName() + "." + classDesc.getDescribedClassSimpleName() , classDesc.getDescribedClassPackageName() + "." + classDesc.getDescribedClassSimpleName());
 			allNamespaces.put(classDesc.getDescribedClassPackageName() + "." + classDesc.getDescribedClassSimpleName(), classDesc.getDescribedClassPackageName() + "." + classDesc.getDescribedClassSimpleName());
 		}
 		
 		ArrayList<String> scalarDependencies = thatClass.getScalarDependencies();
 		System.out.println("No of scalarDependencies : " + scalarDependencies.size());
 		for(String type : scalarDependencies)
 		{
 			libraryNamespaces.put(type, type);
 			allNamespaces.put(type, type);			
 		}
 		
 		ArrayList<String> colelctionDependencies = thatClass.getCollectionDependencies();
 		for(String type : colelctionDependencies)
 		{
 			System.out.println("Collection Dependencies : " + type);
 			libraryNamespaces.put(type, type);
 			allNamespaces.put(type, type);			
 		}
 	}
 	
 	/**
 	 * Takes an input class to generate an Java source files. Takes the {@code directoryLocation}
 	 * of the files where the file needs to be generated.
 	 * <p>
 	 * This function internally calls the {@code translateToJava} main entry function to generate
 	 * the required files
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 * @throws SIMPLTranslationException
 	 * @throws JavaTranslationException
 	 */
 	public void translateToJava(File directoryLocation, TranslationScope tScope)
 			throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		translateToJava(directoryLocation, tScope, null);
 	}
 
 	/**
 	 * A method generating the java source files from the given translation scope object 
 	 * at the given directlyLocation 
 	 * 
 	 * @param directoryLocation
 	 * @param tScope
 	 * @param workSpaceLocation
 	 * @throws IOException
 	 * @throws SIMPLTranslationException
 	 * @throws ParseException
 	 * @throws JavaTranslationException
 	 */
 	public void translateToJava(File directoryLocation, TranslationScope tScope, File workSpaceLocation)
 		throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		System.out.println("Parsing source files to extract comments");
 
 		//TranslationScope anotherScope = TranslationScope.augmentTranslationScopeWithClassDescriptors(tScope);
 		TranslationScope anotherScope = tScope;
 		
 		// Parse source files for javadocs
 		//if(workSpaceLocation != null)
 		//	JavaDocParser.parseSourceFileIfExists(anotherScope, workSpaceLocation);
 
 		System.out.println("generating classes...");
 
 		// Generate header and implementation files
 		Collection<ClassDescriptor>  classes = anotherScope.getClassDescriptors();
 		
 		int length = classes.size();
 		for (ClassDescriptor classDesc : classes)
 		{
 			if(excludeClassesFromTranslation.contains(classDesc))
 			{
 				System.out.println("Excluding " + classDesc + " from translation as requested");
 				continue;
 			}
 			System.out.println("Translating " + classDesc);
 			translateToJava(classDesc, directoryLocation);
 		}
 
 		// create a folder to put the translation scope getter class
 		//File tScopeDirectory = createGetTranslationScopeFolder(directoryLocation);
 		// generate translation scope getter class
 		//generateTranslationScopeGetterClass(tScopeDirectory, tScope);
 
 		System.out.println("DONE !");
 	}
 
 	/**
 	 * Generates the java file for the given classdescriptor. Takes the {@code directoryLocation}
 	 * of the files where the file needs to be generated.
 	 * <p>
 	 * This function internally calls the {@code translateToJava} main entry function to generate
 	 * the required files
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 * @throws DotNetTranslationException
 	 */
 	private void translateToJava(ClassDescriptor inputClass, File directoryLocation)
 			throws IOException, JavaTranslationException
 	{
 		File outputFile = createJavaFileWithDirectoryStructure(inputClass, directoryLocation);
 		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
 
 		translateToJava(inputClass, bufferedWriter);
 		bufferedWriter.close();
 	}
 
 	/**
 	 * A method to append the class comments
 	 * 
 	 * @param className
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendHeaderComments(String className, Appendable appendable) throws IOException
 	{
 		appendable.append(JavaTranslationUtilities.getJavaClassComments(className));
 	}
 
 	/**
 	 * Creates a directory structure from the path of the given by the {@code directoryLocation}
 	 * parameter Uses the class and package names from the parameter {@code inputClass}
 	 * <p>
 	 * This function deletes the files if the files with same class existed inside the directory
 	 * structure and creates a new file for that class
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param directoryLocation
 	 * @return
 	 * @throws IOException
 	 */
 	private File createJavaFileWithDirectoryStructure(ClassDescriptor inputClass, File directoryLocation)
 			throws IOException
 	{
 		String packageName = inputClass.getDescribedClassPackageName();
 		String className = inputClass.getDescribedClassSimpleName();
 		String currentDirectory = directoryLocation.toString() + FILE_PATH_SEPARATOR;
 
 		String[] arrayPackageNames = packageName.split(PACKAGE_NAME_SEPARATOR);
 
 		for (String directoryName : arrayPackageNames)
 		{
 			currentDirectory += directoryName + FILE_PATH_SEPARATOR;
 		}
 
 		File directory = new File(currentDirectory);
 		directory.mkdirs();
 
 		File currentFile = new File(currentDirectory + className + FILE_EXTENSION);
 
 		if (currentFile.exists())
 		{
 			currentFile.delete();
 		}
 
 		currentFile.createNewFile();
 		return currentFile;
 	}
 
 	/**
 	 * metod generating the required namespaces
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void importNameSpaces(Appendable appendable) throws IOException
 	{
 		//importDefaultNamespaces(appendable);
 		
 		// append all the registered namespace
 		if (libraryNamespaces != null && libraryNamespaces.size() > 0)
 		{
 			for (String namespace : libraryNamespaces.values())
 			{
 				// do not append if it belogns to current namespace
 				if (!namespace.equals(currentNamespace) && !namespace.startsWith("java.lang."))
 				{
 					appendable.append(SINGLE_LINE_BREAK);
 					appendable.append(IMPORT);
 					appendable.append(SPACE);
 					appendable.append(namespace);
 					appendable.append(END_LINE);
 				}
 			}
 		}
 
 		appendable.append(DOUBLE_LINE_BREAK);
 	}
 
 	/**
 	 * A method importing the default namespaces
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void importDefaultNamespaces(Appendable appendable) throws IOException
 	{
 		appendable.append(IMPORT);
 		appendable.append(SPACE);
 		appendable.append(JAVA);
 		appendable.append(DOT);
 		appendable.append(UTIL);
 		appendable.append(DOT);
 		appendable.append(JAVA_ARRAYLIST);
 		appendable.append(END_LINE);
 
 		//appendable.append(SINGLE_LINE_BREAK);
 
 		/*appendable.append(IMPORT);
 		appendable.append(SPACE);
 		appendable.append(ECOLOGYLAB_NAMESPACE);
 		appendable.append(END_LINE);*/
 	}
 
 	/**
 	 * A method to generate the package name
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void openNameSpace(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException
 	{
 		openNameSpace(inputClass.getDescribedClassPackageName(), appendable);
 	}
 
 	/**
 	 * A method to generate the package name
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void openNameSpace(String classNameSpace, Appendable appendable) throws IOException
 	{
 		currentNamespace = classNameSpace;
 		allNamespaces.put(currentNamespace, currentNamespace);
 		
 		appendable.append(PACKAGE);
 		appendable.append(SPACE);
 		appendable.append(currentNamespace);
 		appendable.append(END_LINE);	
 		appendable.append(SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * A method generating the default constructor code
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendDefaultConstructor(ClassDescriptor inputClass,
 			Appendable appendable) throws IOException
 	{
 		appendDefaultConstructor(inputClass.getDescribedClassSimpleName(), appendable);
 	}
 	
 	/**
 	 * Default constructor code generation
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendDefaultConstructor(String className,
 			Appendable appendable) throws IOException
 	{
 		appendable.append(TAB);
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		appendable.append(className);
 		appendable.append(OPENING_BRACE);
 		appendable.append(CLOSING_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(OPENING_CURLY_BRACE);
 		appendable.append(SPACE);
 		appendable.append(CLOSING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		
 		appendConstructorHook(className, appendable);
 	}
 	
 	protected void appendConstructorHook(String className, Appendable appendable) throws IOException
 	{
 		
 	}
 
 	/**
 	 * Generating code for the given field descriptor
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 * @throws DotNetTranslationException
 	 */
 	private void appendFieldAsJavaAttribute(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException, JavaTranslationException
 	{
 		registerNamespaces(fieldDescriptor);
 
 		String javaType = fieldDescriptor.getJavaType();
 		if (javaType == null)
 		{
 			System.out.println("ERROR, no valid JavaType found for : " + fieldDescriptor);
 			return;
 		}
 
 		boolean isKeyword = checkForKeywords(fieldDescriptor, appendable);
 		appendComments(appendable, true, isKeyword);
 
 		appendFieldComments(fieldDescriptor, appendable);
 		appendFieldAnnotations(fieldDescriptor, appendable);
 		appendable.append(TAB);
 		appendable.append(PRIVATE);
 		appendable.append(SPACE);
 		appendable.append(javaType);
 		appendable.append(SPACE);
 		appendable.append(fieldDescriptor.getFieldName());
 		appendable.append(END_LINE);
 		appendable.append(DOUBLE_LINE_BREAK);
 
 		appendComments(appendable, false, isKeyword);
 	}
 
 	/**
 	 * Retrieving namespaces correspinds to the given field desriptor
 	 * 
 	 * @param fieldDescriptor
 	 */
 	private void registerNamespaces(FieldDescriptor fieldDescriptor)
 	{
 		HashMap<String, String> namespaces = fieldDescriptor.getNamespaces();
 		
 		if(namespaces != null && namespaces.size() > 0)
 		{			
 			for (String key : namespaces.keySet())
 			{
 				libraryNamespaces.put(key, namespaces.get(key));
 				allNamespaces.put(key, namespaces.get(key));
 			}			
 		}	
 		
 	}
 
 	/**
 	 * A method to test whether fieldAccessor is a java keyword
 	 * 
 	 * @param fieldAccessor
 	 * @param appendable
 	 * @return
 	 * @throws IOException
 	 */
 	private boolean checkForKeywords(FieldDescriptor fieldAccessor, Appendable appendable)
 			throws IOException
 	{
 		if (JavaTranslationUtilities.isKeyword(fieldAccessor.getFieldName()))
 		{
 			Debug.warning(fieldAccessor, " Field Name: [" + fieldAccessor.getFieldName()
 					+ "]. This is a keyword in C#. Cannot translate");
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Generate stating and ending comments
 	 * 
 	 * @param appendable
 	 * @param start
 	 * @param isKeywrord
 	 * @throws IOException
 	 */
 	private void appendComments(Appendable appendable, boolean start, boolean isKeywrord)
 			throws IOException
 	{
 		if (isKeywrord)
 			if (start)
 			{
 				appendable.append("/*");
 				appendable.append(SINGLE_LINE_BREAK);
 			}
 			else
 			{
 				appendable.append("*/");
 				appendable.append(SINGLE_LINE_BREAK);
 			}
 	}
 
 	/**
 	 * A method appending the field comments
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendFieldComments(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException
 	{
 		appendable.append(TAB);
 		appendable.append(OPEN_COMMENTS);
 		appendable.append(SINGLE_LINE_BREAK);
 
 		if(fieldDescriptor.getComment() != null)
 		{
 			appendCommentsFromArray(appendable, escapeToArray(fieldDescriptor.getComment()), false);
 		}
 		appendable.append(TAB);
 		appendable.append(CLOSE_COMMENTS);
 		appendable.append(SINGLE_LINE_BREAK);
 
 	}
 
 	/**
 	 * A method to generate the annotations in the java code
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendFieldAnnotations(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException
 	{
 		int type = fieldDescriptor.getType();
 		String collectionMapTagValue = fieldDescriptor.getCollectionOrMapTagName();		
 		
 		if(type == FieldTypes.COMPOSITE_ELEMENT)
 		{
 			// @simpl_composite
 			appendAnnotation(appendable,simpl_composite.class.getSimpleName(),TAB);			
 		}
 		else if(type == FieldTypes.COLLECTION_ELEMENT || type == FieldTypes.COLLECTION_SCALAR)
 		{
 			if(!fieldDescriptor.isWrapped())
 			{
 				// @simpl_nowrap
 				appendAnnotation(appendable,simpl_nowrap.class.getSimpleName(),TAB);
 			}
 			// @simpl_collection
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaCollectionAnnotation(collectionMapTagValue),TAB);			
 		}
 		else if(type == FieldTypes.MAP_ELEMENT)
 		{
 			// @simpl_map
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaMapAnnotation(collectionMapTagValue),TAB);			
 		}
 		else
 		{
 			// @simpl_scalar
 			appendAnnotation(appendable, simpl_scalar.class.getSimpleName(),TAB);
 		}
 		
 		// @xml_tag
 		String tagName = fieldDescriptor.getTagName();
 		String autoTagName = XMLTools.getXmlTagName(fieldDescriptor.getFieldName(), null);
 		if(tagName != null && !tagName.equals("") && !tagName.equals(autoTagName))
 		{
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaTagAnnotation(tagName), TAB);			
 		}		
 		
 		// @xml_other_tags
 		ArrayList<String> otherTags = fieldDescriptor.otherTags();
 		if (otherTags != null && otherTags.size() > 0)
 		{
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaOtherTagsAnnotation(otherTags), TAB);
 		}
 		
 		if(!((type == FieldTypes.COMPOSITE_ELEMENT) || (type == FieldTypes.COLLECTION_ELEMENT) || (type == FieldTypes.COLLECTION_SCALAR)))
 		{
 			Hint hint = fieldDescriptor.getXmlHint();
 			if(hint != null)
 			{
 				// @simpl_hints
 				appendAnnotation(appendable, JavaTranslationUtilities.getJavaHintsAnnotation(hint.name()),TAB);
 			}
 		}		
 		
 		// @simpl_classes
 		HashMapArrayList<String, ClassDescriptor> polyClassDescriptors = fieldDescriptor.getTagClassDescriptors();
 		if (polyClassDescriptors != null && polyClassDescriptors.size() > 0)
 		{
 			HashSet<ClassDescriptor> classDescriptors = new HashSet<ClassDescriptor>(polyClassDescriptors.values());
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaClassesAnnotation(classDescriptors), TAB);
 		}
 		
 		// @simpl_scope
 		String polyScope = fieldDescriptor.getUnresolvedScopeAnnotation();
 		if (polyScope != null && polyScope.length() > 0)
 		{
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaScopeAnnotation(polyScope), TAB);
 		}
 		
 		appendFieldAnnotationsHook(appendable, fieldDescriptor);
 	}
 	
 	/**
 	 * (for adding customized annotations, e.g. meta-metadata specific ones)
 	 * 
 	 * @param appendable
 	 * @param classDesc
 	 * @param tabSpacing
 	 * @throws IOException 
 	 */
 	protected void appendFieldAnnotationsHook(Appendable appendable, FieldDescriptor fieldDesc) throws IOException
 	{
 		
 	}
 
 	/**
 	 * append class annptations
 	 * 
 	 * @param appendable
 	 * @param classDesc
 	 * @param tabSpacing
 	 * @throws IOException
 	 */
 	private void appendClassAnnotations(Appendable appendable, ClassDescriptor classDesc, String tabSpacing)
 			throws IOException
 	{
 		ClassDescriptor superClass = classDesc.getSuperClass();
 		if(superClass != null && !superClass.getDescribedClassSimpleName().equals("ElementState"))
 		{
 			appendAnnotation(appendable,simpl_inherit.class.getSimpleName(),"");
 			allNamespaces.put(simpl_inherit.class.getPackage().getName() + ".simpl_inherit", simpl_inherit.class.getPackage().getName() +  ".simpl_inherit");
 			libraryNamespaces.put(simpl_inherit.class.getPackage().getName() + ".simpl_inherit", simpl_inherit.class.getPackage().getName()+ ".simpl_inherit");
 		}
 		
 		String tagName = classDesc.getTagName();
 		String autoTagName = XMLTools.getXmlTagName(classDesc.getDescribedClassSimpleName(), null);
 		if(tagName != null && !tagName.equals("") && !tagName.equals(autoTagName))
 		{
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaTagAnnotation(tagName),"\n");
 			allNamespaces.put(xml_tag.class.getPackage().getName() + ".xml_tag", xml_tag.class.getPackage().getName() +  ".xml_tag");
 			libraryNamespaces.put(xml_tag.class.getPackage().getName() + ".xml_tag", xml_tag.class.getPackage().getName()+ ".xml_tag");
 		}		
 		
 		// TODO @xml_other_tags
 		ArrayList<String> otherTags = classDesc.otherTags();
 		if (otherTags != null && otherTags.size() > 0)
 		{
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaOtherTagsAnnotation(otherTags), "\n");
 			allNamespaces.put(xml_other_tags.class.getPackage().getName() + ".xml_other_tags", xml_other_tags.class.getPackage().getName() +  ".xml_other_tags");
 			libraryNamespaces.put(xml_other_tags.class.getPackage().getName() + ".xml_other_tags", xml_other_tags.class.getPackage().getName()+ ".xml_other_tags");
 		}
 		
 		appendClassAnnotationsHook(appendable, classDesc, tabSpacing);
 	}
 	
 	/**
 	 * (for adding customized annotations, e.g. meta-metadata specific ones)
 	 * 
 	 * @param appendable
 	 * @param classDesc
 	 * @param tabSpacing
 	 */
 	protected void appendClassAnnotationsHook(Appendable appendable, ClassDescriptor classDesc, String tabSpacing)
 	{
 		
 	}
 	
 	/**
 	 * Generate the code for annotation
 	 * 
 	 * @param appendable
 	 * @param annotation
 	 * @throws IOException
 	 */
 	protected void appendAnnotation(Appendable appendable,String annotation, String tab) throws IOException
 	{
 		appendable.append(tab);
 		appendable.append(AT_SIGN);
 		appendable.append(annotation);
 	}
 
 	/**
 	 * A method to generate get method for the given field Descriptor
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 */
 	protected void appendGetters(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException
 	{
 		String javaType = fieldDescriptor.getJavaType();
 		if (javaType == null)
 		{
 			System.out.println("ERROR, no valid JavaType found for : " + fieldDescriptor);
 			return;
 		}
 
 		appendGettersHelper(fieldDescriptor, javaType, appendable);
 	}
 
 	protected void appendGettersHelper(FieldDescriptor fieldDescriptor, String javaType,
 			Appendable appendable) throws IOException
 	{
 		appendable.append(SINGLE_LINE_BREAK);
 
 		boolean isKeyword = checkForKeywords(fieldDescriptor, appendable);
 		appendComments(appendable, true, isKeyword);
 
 		appendable.append(TAB);
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		appendable.append(javaType);
 		appendable.append(SPACE);
 		appendable.append(JavaTranslationUtilities.getGetMethodName(fieldDescriptor));
 		appendable.append(OPENING_BRACE);
 		appendable.append(CLOSING_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(OPENING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(DOUBLE_TAB);
 		appendable.append(RETURN);
 		appendable.append(SPACE);
 		appendable.append(fieldDescriptor.getFieldName());
 		appendable.append(END_LINE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(CLOSING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 
 		appendComments(appendable, false, isKeyword);
 	}
 	
 	/**
 	 * A method to generate set method for the given field descriptor
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 */
 	protected void appendSetters(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException
 	{
 		String javaType = fieldDescriptor.getJavaType();
 		if (javaType == null)
 		{
 			System.out.println("ERROR, no valid JavaType found for : " + fieldDescriptor);
 			return;
 		}
 
 		appendSettersHelper(fieldDescriptor, javaType, appendable);
 	}
 
 	protected void appendSettersHelper(FieldDescriptor fieldDescriptor, String javaType,
 			Appendable appendable) throws IOException
 	{
 		appendable.append(SINGLE_LINE_BREAK);
 
 		boolean isKeyword = checkForKeywords(fieldDescriptor, appendable);
 		appendComments(appendable, true, isKeyword);
 
 		appendable.append(TAB);
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		appendable.append(VOID);
 		appendable.append(SPACE);
 		appendable.append(JavaTranslationUtilities.getSetMethodName(fieldDescriptor));
 		appendable.append(OPENING_BRACE);
 		appendable.append(javaType);
 		appendable.append(SPACE);
 		appendable.append(fieldDescriptor.getFieldName());
 		appendable.append(CLOSING_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(OPENING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(DOUBLE_TAB);
 		appendable.append(THIS);
 		appendable.append(DOT);
 		appendable.append(fieldDescriptor.getFieldName());
 		appendable.append(SPACE);
 		appendable.append(EQUALS);
 		appendable.append(SPACE);
 		appendable.append(fieldDescriptor.getFieldName());
 		appendable.append(END_LINE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(CLOSING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 
 		appendComments(appendable, false, isKeyword);
 	}
 
 	/**
 	 * generates the java code for starting a class
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void openClassFile(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException
 	{
 		appendClassComments(inputClass, appendable);
 
 		ClassDescriptor genericSuperclass = inputClass.getSuperClass();
 
 		appendClassAnnotations(appendable, inputClass, "");
 
 		//appendable.append(TAB);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		appendable.append(CLASS);
 		appendable.append(SPACE);
 		appendable.append(inputClass.getDescribedClassSimpleName());
 		appendGenericTypeVariables(appendable, inputClass);
 		appendable.append(SPACE);
 		appendable.append(INHERITANCE_OPERATOR);
 		appendable.append(SPACE);
 		appendable.append(genericSuperclass.getDescribedClassSimpleName());
 		
 		
 		libraryNamespaces.put(genericSuperclass.getDescribedClassPackageName() + "."+ genericSuperclass.getDescribedClassSimpleName(), genericSuperclass.getDescribedClassPackageName()+ "." + genericSuperclass.getDescribedClassSimpleName());
 		allNamespaces.put(genericSuperclass.getDescribedClassPackageName()+ "." + genericSuperclass.getDescribedClassSimpleName(), genericSuperclass.getDescribedClassPackageName()+ "." + genericSuperclass.getDescribedClassSimpleName());		
 	
 		ArrayList<String> interfaces = inputClass.getInterfaceList();
 
 		if(interfaces != null)
 		{
 			for (int i = 0; i < interfaces.size(); i++)
 			{
 				appendable.append(',');
 				appendable.append(SPACE);
 				appendable.append(interfaces.get(i));
 				implementMappableInterface = true;
 	
 				libraryNamespaces.put(Mappable.class.getPackage().getName(), Mappable.class.getPackage()
 						.getName());
 				
 			}		
 		}		
 
 		appendable.append(SINGLE_LINE_BREAK);
 		//appendable.append(TAB);
 		appendable.append(OPENING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * A method to generate generic type variables of the class
 	 * 
 	 * @param appendable
 	 * @param inputClass
 	 * @throws IOException
 	 */
 	private void appendGenericTypeVariables(Appendable appendable,
 			ClassDescriptor inputClass) throws IOException
 	{
 		ArrayList<String> typeVariables = inputClass.getGenericTypeVariables();
 		if (typeVariables != null && typeVariables.size() > 0)
 		{
 			appendable.append('<');
 			int i = 0;
 			for (String typeVariable : typeVariables)
 			{
 				if (i == 0)
 					appendable.append(typeVariable);
 				else
					appendable.append(", " + typeVariable);
 				i++;
 			}
 			appendable.append('>');
 		}
 	}
 
 	/**
 	 * A method to append the class comments
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendClassComments(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException
 	{
 		// TODO need to generate from the serialised comment field
 		
 		//appendable.append(TAB);
 		appendable.append(OPEN_COMMENTS);
 		appendable.append(SINGLE_LINE_BREAK);
 
 		//appendCommentsFromArray(appendable, JavaDocParser.getClassJavaDocsArray(inputClass), false);
 		if(inputClass.getComment() != null)
 		{
 			appendCommentsFromArray(appendable, escapeToArray(inputClass.getComment()), false);
 		}
 		//appendable.append(TAB);
 		appendable.append(CLOSE_COMMENTS);
 		appendable.append(SINGLE_LINE_BREAK);
 
 	}
 
 	/**
 	 *  
 	 * @param appendable
 	 * @param javaDocCommentArray
 	 * @param doubleTabs
 	 * @throws IOException
 	 */
 	private void appendCommentsFromArray(Appendable appendable, String[] javaDocCommentArray,
 			boolean doubleTabs) throws IOException
 	{
 		String numOfTabs = TAB;
 		if (doubleTabs)
 			numOfTabs = DOUBLE_TAB;
 
 		if (javaDocCommentArray != null)
 		{
 			for (String comment : javaDocCommentArray)
 			{
 				appendable.append(numOfTabs);
 				appendable.append(XML_COMMENTS);
 				appendable.append(SPACE);
 				appendable.append(comment);
 				appendable.append(SINGLE_LINE_BREAK);
 			}
 		}
 		else
 		{
 			appendable.append(numOfTabs);
 			appendable.append(XML_COMMENTS);
 			appendable.append(" missing java doc comments or could not find the source file.");
 			appendable.append(SINGLE_LINE_BREAK);
 		}
 	}
 
 	/**
 	 *  
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void implementMappableMethods(Appendable appendable) throws IOException
 	{
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(DOUBLE_TAB);
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		appendable.append(JAVA_OBJECT);
 		appendable.append(SPACE);
 		appendable.append(KEY);
 		appendable.append(OPENING_BRACE);
 		appendable.append(CLOSING_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(DOUBLE_TAB);
 		appendable.append(OPENING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(DOUBLE_TAB);
 		appendable.append(TAB);
 		appendable.append(DEFAULT_IMPLEMENTATION);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(DOUBLE_TAB);
 		appendable.append(CLOSING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * generates the code corresponds to closing the class
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void closeClassFile(Appendable appendable) throws IOException
 	{
 		//appendable.append(TAB);
 		appendable.append(CLOSING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * method to exclude class from translation
 	 * 
 	 * @param someClass
 	 */
 	public void excludeClassFromTranslation(ClassDescriptor someClass)
 	{
 		excludeClassesFromTranslation.add(someClass);
 	}
 	
 	/**
 	 * Adding additional Imports in to the generated code
 	 * 
 	 * @param additionalImportLines
 	 */
 	public void setAdditionalImportNamespaces(ArrayList<String> additionalImportLines)
 	{
 		if (additionalImportLines == null)
 			return;
 
 		for (String newImport : additionalImportLines)
 		{
 			libraryNamespaces.put(newImport, newImport);
 			allNamespaces.put(newImport, newImport);
 		}
 
 		this.additionalImportLines = additionalImportLines;
 	}
 	
 	/**
 	 * build up the array of java doc comments
 	 * 
 	 * @param javaDocs
 	 * @return
 	 */
 	private static String[] escapeToArray(String javaDocs)
 	{
 		String strippedComments = javaDocs.replace("*", "").replace("/", "").trim();
 		String[] commentsArray = strippedComments.split("\n");
 
 		for (int i = 0; i < commentsArray.length; i++)
 		{
 			commentsArray[i] = commentsArray[i].trim();
 		}
 
 		return commentsArray;
 	}
 
 
 	/**
 	 * Main method to test the working of the library.
 	 * 
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String args[]) throws Exception
 	{
 		JavaTranslator c = new JavaTranslator();
 		
 		TranslationScope ts2 = TranslationScope.get("RSSTranslations", Schmannel.class, BItem.class, SchmItem.class,
 				RssState.class, Item.class, Channel.class);
 		ts2.setGraphSwitch();
 		ts2.serialize(new File("D:\\GSOC\\SIMPL\\GeneratedCode\\New\\tss3.xml"));
 		//JavaDocParser.parseSourceFileIfExists(ts,new File("D:\\GSOC\\SIMPL"));
 
 		TranslationScope ts = TranslationScope.get("tscope_tscope", TranslationScope.class, ClassDescriptor.class, FieldDescriptor.class);
 		ts.setGraphSwitch();
 		TranslationScope t = (TranslationScope)ts.deserialize("D:\\GSOC\\SIMPL\\GeneratedCode\\New\\tss3.xml");
 		TranslationScope.AddTranslationScope(t.getName(),t);
 		//t.serialize(new File("D:\\GSOC\\SIMPL\\GeneratedCode\\New\\tss2.xml"));
 		t.setGraphSwitch();
 		c.translateToJava(new File("D:\\GSOC\\SIMPL\\GeneratedCode\\Test"),t);
 		//c.translateToJava(new File("D:\\GSOC\\Output"),t);
 								
 		//c.translateToJava(System.out, Item.class);
 	}
 }
