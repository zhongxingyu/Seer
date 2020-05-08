 package ecologylab.translators.java;
 
 import japa.parser.ParseException;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.ElementState.simpl_composite;
 import ecologylab.serialization.ElementState.simpl_nowrap;
 import ecologylab.serialization.ElementState.simpl_scalar;
 import ecologylab.serialization.ElementState.xml_other_tags;
 import ecologylab.serialization.ElementState.xml_tag;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.FieldTypes;
 import ecologylab.serialization.Hint;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.XMLTools;
 import ecologylab.serialization.simpl_inherit;
 import ecologylab.serialization.library.rss.Channel;
 import ecologylab.serialization.library.rss.Item;
 import ecologylab.serialization.library.rss.RssState;
 import ecologylab.serialization.types.CollectionType;
 import ecologylab.serialization.types.ScalarType;
 import ecologylab.serialization.types.SimplBaseType;
 import ecologylab.serialization.types.element.Mappable;
 import ecologylab.standalone.xmlpolymorph.BItem;
 import ecologylab.standalone.xmlpolymorph.SchmItem;
 import ecologylab.standalone.xmlpolymorph.Schmannel;
 import ecologylab.translators.net.DotNetTranslationException;
 
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
 
 	/**
 	 * These are import dependencies for the current source file.
 	 */
 	private HashMap<String, String>	currentImportDependencies		= new HashMap<String, String>();
 
 	/**
 	 * This is the global set of import dependencies for the whole TranslationScope.
 	 */
 	private HashMap<String, String>	allImportDependencies								= new HashMap<String, String>();
 	
 	/**
 	 * These will be used for generating imports, but not cleared after each source file.
 	 */
 	private HashSet<String>					globalImportDependencies			= new HashSet<String>();
 
 	private String									currentNamespace;
 
 	private boolean									implementMappableInterface	= false;
 
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
 
 		addClassDependencies(inputClass);
 		openNameSpace(inputClass, header);
 		
 		openClassFile(inputClass, classFile);
 
 		// we still need to generate the constructors for empty metadata classes!
 //		if (fieldDescriptors.size() > 0)
 //		{
 			classDescriptor.resolveUnresolvedScopeAnnotationFDs();
 
 			for (FieldDescriptor fieldDescriptor : fieldDescriptors)
 			{
 				if (fieldDescriptor.belongsTo(classDescriptor))
 				{					
 					appendFieldAsJavaAttribute(inputClass, fieldDescriptor, classFile);						
 				}
 			}
 
 			appendDefaultConstructor(inputClass, classFile);
 
 			for (FieldDescriptor fieldDescriptor : fieldDescriptors)
 			{
 				if (fieldDescriptor.belongsTo(classDescriptor))
 				{					
 					appendGetters(fieldDescriptor, classFile, null);
 					appendSetters(fieldDescriptor, classFile, null);					
 				}
 			}
 
 			if (implementMappableInterface)
 			{
 				implementMappableMethods(classFile);
 				implementMappableInterface = false;
 			}
 //		}
 
 		closeClassFile(classFile);
 		
 		generateImportStatements(header);
 		appendHeaderComments(inputClass.getDescribedClassSimpleName(), header);
 
 		currentImportDependencies.clear();
 
 		appendable.append(header);
 		appendable.append(classFile);
 	}
 
 	/**
 	 * Set-up import dependencies for the class whose source is currently being emitted.
 	 * 
 	 * @param classDescriptor
 	 */
 	private void addClassDependencies(ClassDescriptor classDescriptor)
 	{
 		Set<ClassDescriptor> compositeDependencies = classDescriptor.deriveCompositeDependencies();
 		addDependencies(compositeDependencies);
 		
 		Set<ScalarType> scalarDependencies = classDescriptor.deriveScalarDependencies();
 		System.out.println(classDescriptor.getDescribedClassName()+" HAS " + scalarDependencies.size() + " scalar dependencies\n");
 		addDependencies(scalarDependencies);
 		
 		Set<CollectionType> colletionDependencies = classDescriptor.deriveCollectionDependencies();
 		addDependencies(colletionDependencies);
 	}
 
 	/**
 	 * Add a dependency, by name, for inclusion in imports, and such.
 	 * 
 	 * @param fullClassName
 	 */
 	protected void addDependency(String fullClassName)
 	{
 		addCurrentImportTarget(fullClassName);
 		allImportDependencies.put(fullClassName, fullClassName);
 	}
 
 	/**
 	 * @param fullClassName
 	 */
 	protected void addCurrentImportTarget(String fullClassName)
 	{
 		currentImportDependencies.put(fullClassName, fullClassName);
 	}
 	protected void addCurrentImportTargets(Iterable<String> targetNames)
 	{
 		if (targetNames != null)
 			for (String fullClassName: targetNames)
 				addCurrentImportTarget(fullClassName);
 	}
 	/**
 	 * Add dependencies, using a Collection or other Iterable of class full name Strings.
 	 * 
 	 * @param fullClassNames
 	 */
 	public void addDependencies(Iterable<String> fullClassNames)
 	{
 		for (String fullClassName : fullClassNames)
 			addDependency(fullClassName);
 	}
 	/**
 	 * Add dependencies, using an array of class full name Strings.
 	 * 
 	 * @param fullClassNames
 	 */
 	public void addDependencies(String[] fullClassNames)
 	{
 		for (String fullClassName : fullClassNames)
 			addDependency(fullClassName);
 	}
 	/**
 	 * Add dependencies, using a collection of objects that implement Describable.
 	 * 
 	 * @param describables
 	 */
 	public void addDependencies(Collection<? extends SimplBaseType> simplBaseTypeSet)
 	{
 		for (SimplBaseType simplBaseType: simplBaseTypeSet)
 			addDependency(simplBaseType.getJavaTypeName());
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
 	public void translateToJava(File directoryLocation, final TranslationScope tScope, File workSpaceLocation)
 		throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		System.out.println("Parsing source files to extract comments");
 
 		//TranslationScope anotherScope = TranslationScope.augmentTranslationScopeWithClassDescriptors(tScope);
 		
 		// Parse source files for javadocs
 		//if(workSpaceLocation != null)
 		//	JavaDocParser.parseSourceFileIfExists(anotherScope, workSpaceLocation);
 
 		System.out.println("generating classes...");
 
 		// Generate header and implementation files
 		Collection<ClassDescriptor>  classes = tScope.entriesByClassName().values();
 		
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
 	 * method for generating the required import statements
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void generateImportStatements(Appendable appendable) throws IOException
 	{
 		addCurrentImportTargets(globalImportDependencies);
 		
 		for (String namespace : currentImportDependencies.values())
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
 		appendable.append(DOUBLE_LINE_BREAK);
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
 		allImportDependencies.put(currentNamespace, currentNamespace);
 		
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
 	 * @param contextCd TODO
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * 
 	 * @throws IOException
 	 * @throws DotNetTranslationException
 	 */
 	private void appendFieldAsJavaAttribute(ClassDescriptor contextCd, FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException, JavaTranslationException
 	{
 		registerNamespaces(fieldDescriptor);
 
		String javaType = fieldDescriptor.getJavaType();
		if (fieldDescriptor.getType() == FieldTypes.SCALAR)
 			javaType = fieldDescriptor.getScalarType().getSimpleName();
 		if (javaType == null)
 		{
 			System.out.println("ERROR, no valid JavaType found for : " + fieldDescriptor);
 			return;
 		}
 
 		boolean isKeyword = checkForKeywords(fieldDescriptor, appendable);
 		appendCommentBoundary(appendable, true, isKeyword);
 
 		appendFieldComments(fieldDescriptor, appendable);
 		appendFieldAnnotations(contextCd, fieldDescriptor, appendable);
 		appendable.append(TAB);
 		appendable.append(PRIVATE);
 		appendable.append(SPACE);
 		appendable.append(javaType);
 		appendable.append(SPACE);
 		appendable.append(fieldDescriptor.getName());
 		appendable.append(END_LINE);
 		appendable.append(DOUBLE_LINE_BREAK);
 
 		appendCommentBoundary(appendable, false, isKeyword);
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
 				currentImportDependencies.put(key, namespaces.get(key));
 				allImportDependencies.put(key, namespaces.get(key));
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
 		if (JavaTranslationUtilities.isKeyword(fieldAccessor.getName()))
 		{
 			Debug.warning(fieldAccessor, " Field Name: [" + fieldAccessor.getName()
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
 	private void appendCommentBoundary(Appendable appendable, boolean start, boolean isKeywrord)
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
 	 * @param contextCd TODO
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * 
 	 * @throws IOException
 	 */
 	private void appendFieldAnnotations(ClassDescriptor contextCd, FieldDescriptor fieldDescriptor, Appendable appendable)
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
 			addDependency(List.class.getName());
 			if(!fieldDescriptor.isWrapped())
 			{
 				// @simpl_nowrap
 				appendAnnotation(appendable,simpl_nowrap.class.getSimpleName(),TAB);
 			}
 			// @simpl_collection
 			if (fieldDescriptor.isPolymorphic())
 				appendAnnotation(appendable, JavaTranslationUtilities.getJavaCollectionAnnotation(null),TAB);			
 			else
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
 		String autoTagName = XMLTools.getXmlTagName(fieldDescriptor.getName(), null);
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
 				addDependency(Hint.class.getName());
 			}
 		}		
 		
 		// @simpl_classes
 		Collection<ClassDescriptor> polyClassDescriptors = fieldDescriptor.getPolymorphicClassDescriptors();
 		if (polyClassDescriptors != null)
 		{
 			HashSet<ClassDescriptor> classDescriptors = new HashSet<ClassDescriptor>(polyClassDescriptors);
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaClassesAnnotation(classDescriptors), TAB);
 		}
 		
 		// @simpl_scope
 		String polyScope = fieldDescriptor.getUnresolvedScopeAnnotation();
 		if (polyScope != null && polyScope.length() > 0)
 		{
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaScopeAnnotation(polyScope), TAB);
 		}
 		
 		appendFieldAnnotationsHook(appendable, contextCd, fieldDescriptor, TAB);
 	}
 	
 	/**
 	 * (for adding customized annotations, e.g. meta-metadata specific ones)
 	 * 
 	 * @param appendable
 	 * @param contextCd TODO
 	 * @param classDesc
 	 * @param tabSpacing
 	 * @throws IOException 
 	 */
 	protected void appendFieldAnnotationsHook(Appendable appendable, ClassDescriptor contextCd, FieldDescriptor fieldDesc, String spacing) throws IOException
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
 			addAnnotationDependency(simpl_inherit.class);
 		}
 		
 		String tagName = classDesc.getTagName();
 		String autoTagName = XMLTools.getXmlTagName(classDesc.getDescribedClassSimpleName(), null);
 		if(tagName != null && !tagName.equals("") && !tagName.equals(autoTagName))
 		{
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaTagAnnotation(tagName),"\n");
 			addAnnotationDependency(xml_tag.class);
 		}		
 		
 		// TODO @xml_other_tags
 		ArrayList<String> otherTags = classDesc.otherTags();
 		if (otherTags != null && otherTags.size() > 0)
 		{
 			appendAnnotation(appendable, JavaTranslationUtilities.getJavaOtherTagsAnnotation(otherTags), "\n");
 			addAnnotationDependency(xml_other_tags.class);
 		}
 		
 		appendable.append(SINGLE_LINE_BREAK);
 		appendClassAnnotationsHook(appendable, classDesc, tabSpacing);
 	}
 	
 	protected void addAnnotationDependency(Class annotationClass)
 	{
 		addDependency(annotationClass.getCanonicalName());
 	}
 	/**
 	 * (for adding customized annotations, e.g. meta-metadata specific ones)
 	 * 
 	 * @param appendable
 	 * @param classDesc
 	 * @param tabSpacing
 	 * @throws IOException 
 	 */
 	protected void appendClassAnnotationsHook(Appendable appendable, ClassDescriptor classDesc, String tabSpacing) throws IOException
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
 	 * @param suffix TODO
 	 * @throws IOException
 	 */
 	protected void appendGetters(FieldDescriptor fieldDescriptor, Appendable appendable, String suffix) throws IOException
 	{
 		String javaType = fieldDescriptor.getJavaType();
 		if (javaType == null)
 		{
 			System.out.println("ERROR, no valid JavaType found for : " + fieldDescriptor);
 			return;
 		}
 
 		appendGettersHelper(fieldDescriptor, javaType, appendable, suffix);
 	}
 
 	protected void appendGettersHelper(FieldDescriptor fieldDescriptor, String javaType, Appendable appendable, String suffix) throws IOException
 	{
 		appendable.append(SINGLE_LINE_BREAK);
 
 		boolean isKeyword = checkForKeywords(fieldDescriptor, appendable);
 		appendCommentBoundary(appendable, true, isKeyword);
 
 		appendable.append(TAB);
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		appendable.append(javaType);
 		appendable.append(SPACE);
 		appendable.append(JavaTranslationUtilities.getGetMethodName(fieldDescriptor) + (suffix == null ? "" : suffix));
 		appendable.append(OPENING_BRACE);
 		appendable.append(CLOSING_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(OPENING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(DOUBLE_TAB);
 		appendable.append(RETURN);
 		appendable.append(SPACE);
 		appendable.append(fieldDescriptor.getName());
 		appendable.append(END_LINE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(CLOSING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 
 		appendCommentBoundary(appendable, false, isKeyword);
 	}
 	
 	/**
 	 * A method to generate set method for the given field descriptor
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @param suffix TODO
 	 * @throws IOException
 	 */
 	protected void appendSetters(FieldDescriptor fieldDescriptor, Appendable appendable, String suffix) throws IOException
 	{
 		String javaType = fieldDescriptor.getJavaType();
 		if (javaType == null)
 		{
 			System.out.println("ERROR, no valid JavaType found for : " + fieldDescriptor);
 			return;
 		}
 
 		appendSettersHelper(fieldDescriptor, javaType, appendable, suffix);
 	}
 
 	protected void appendSettersHelper(FieldDescriptor fieldDescriptor, String javaType, Appendable appendable, String suffix) throws IOException
 	{
 		appendable.append(SINGLE_LINE_BREAK);
 
 		boolean isKeyword = checkForKeywords(fieldDescriptor, appendable);
 		appendCommentBoundary(appendable, true, isKeyword);
 
 		appendable.append(TAB);
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		appendable.append(VOID);
 		appendable.append(SPACE);
 		appendable.append(JavaTranslationUtilities.getSetMethodName(fieldDescriptor) + (suffix == null ? "" : suffix));
 		appendable.append(OPENING_BRACE);
 		appendable.append(javaType);
 		appendable.append(SPACE);
 		appendable.append(fieldDescriptor.getName());
 		appendable.append(CLOSING_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(OPENING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(DOUBLE_TAB);
 		appendable.append(THIS);
 		appendable.append(DOT);
 		appendable.append(fieldDescriptor.getName());
 		appendable.append(SPACE);
 		appendable.append(EQUALS);
 		appendable.append(SPACE);
 		appendable.append(fieldDescriptor.getName());
 		appendable.append(END_LINE);
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(CLOSING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 
 		appendCommentBoundary(appendable, false, isKeyword);
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
 
 		ClassDescriptor genericSuperclassDescriptor = inputClass.getSuperClass();
 
 		appendClassAnnotations(appendable, inputClass, "");
 
 		//appendable.append(TAB);
 //		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		appendable.append(CLASS);
 		appendable.append(SPACE);
 		appendable.append(inputClass.getDescribedClassSimpleName());
 		appendGenericTypeVariables(appendable, inputClass);
 		appendable.append(SPACE);
 		appendable.append(INHERITANCE_OPERATOR);
 		appendable.append(SPACE);
 		appendable.append(genericSuperclassDescriptor.getDescribedClassSimpleName());
 		
 		addDependency(genericSuperclassDescriptor.getDescribedClassName());
 	
 		ArrayList<String> interfaces = inputClass.getInterfaceList();
 
 		if(interfaces != null)
 		{
 			for (int i = 0; i < interfaces.size(); i++)
 			{
 				appendable.append(',');
 				appendable.append(SPACE);
 				appendable.append(interfaces.get(i));
 				implementMappableInterface = true;
 	
 				currentImportDependencies.put(Mappable.class.getPackage().getName(), Mappable.class.getPackage()
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
 					appendable.append(", ").append(typeVariable);
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
 
 	public void addGlobalImportDependency(String fullClassName)
 	{
 		globalImportDependencies.add(fullClassName);
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
