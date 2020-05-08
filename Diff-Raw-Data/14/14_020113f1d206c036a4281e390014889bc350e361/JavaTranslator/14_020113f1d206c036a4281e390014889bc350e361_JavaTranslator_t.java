 package ecologylab.translators.java;
 
 import static ecologylab.translators.java.JavaTranslationConstants.SPACE;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
import java.io.StringWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.AutoIndentWriter;
 import org.stringtemplate.v4.ST;
 import org.stringtemplate.v4.STGroup;
 import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STWriter;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.semantics.html.utils.StringBuilderUtils;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.FieldTypes;
 import ecologylab.serialization.GenericTypeVar;
 import ecologylab.serialization.MetaInformation;
 import ecologylab.serialization.MetaInformation.Argument;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.annotations.Hint;
 import ecologylab.serialization.types.CollectionType;
 import ecologylab.serialization.types.ScalarType;
 import ecologylab.serialization.types.element.IMappable;
 import ecologylab.translators.AbstractCodeTranslator;
 import ecologylab.translators.CodeTranslatorConfig;
 
 /**
  * This class is the main class which provides the functionality of translation of Translation Scope
  * into the Java implementation files.
  * 
  * @author Sumith
  * @version 1.0
  */
 @SuppressWarnings({ "unchecked", "rawtypes" })
 public class JavaTranslator extends AbstractCodeTranslator implements JavaTranslationConstants
 {
 	
 	public static STGroup templateGroup;
 	
 	static
 	{
		templateGroup = new STGroupFile("resources/codeTranslators/javaCodeTranslator.stg");
 		templateGroup.registerRenderer(String.class, new AttributeRenderer()
 		{
 			@Override
 			public String toString(Object o, String formatString, Locale locale)
 			{
 				if (formatString == null || formatString.length() == 0)
 					return o.toString();
 				
 				String s = (String) o;
 				if (formatString.equals("capFirst") && s.length() > 0)
 					return s.substring(0, 1).toUpperCase() + s.substring(1);
 							
 				return s;
 			}
 		});
 	}
 
 	/**
 	 * These are import dependencies for the current source file.
 	 */
 	private Set<String>									currentClassDependencies;
 
 	/**
 	 * This is the global set of import dependencies for the whole TranslationScope.
 	 */
 	private Set<String>									libraryTScopeDependencies			= new HashSet<String>();
 
 	/**
 	 * These will be used for generating imports, but not cleared after each source file.
 	 */
 	private Set<String>									globalDependencies						= new HashSet<String>();
 
 	private String											currentNamespace;
 
 	private boolean											implementMappableInterface		= false;
 
 	private ArrayList<ClassDescriptor>	excludeClassesFromTranslation	= new ArrayList<ClassDescriptor>();
 
 	protected CodeTranslatorConfig			config;
 
 	private String											claimString;
 	
 	protected String										typesScopeName;
 
 	public JavaTranslator()
 	{
 		super("java");
 	}
 
 	protected void initGlobalDependencies()
 	{
 		globalDependencies.clear();
 		// add generic imports, just in case
 		addGlobalDependency(List.class.getName());
 		addGlobalDependency(Map.class.getName());
 		libraryTScopeDependencies.clear();
 	}
 
 	@Override
 	public void translate(File directoryLocation, SimplTypesScope tScope, CodeTranslatorConfig config)
 			throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		debug("Generating Java classes...");
 		this.config = config;
 		this.typesScopeName = tScope.getName();
 		initGlobalDependencies();
 		
 		Collection<ClassDescriptor<? extends FieldDescriptor>> classes = tScope.entriesByClassName()
 				.values();
 		for (ClassDescriptor classDesc : classes)
 		{
 			if (excludeClassesFromTranslation.contains(classDesc))
 			{
 				debug("Excluding " + classDesc + " from translation as requested");
 				continue;
 			}
 			translate(classDesc, directoryLocation, config);
 		}
 		generateLibraryTScopeClass(directoryLocation, tScope, config.getLibraryTScopeClassPackage(), config.getLibraryTScopeClassSimpleName());
 		System.out.println("DONE !");
 	}
 
 	@Override
 	public void translate(ClassDescriptor inputClass, File directoryLocation, CodeTranslatorConfig config)
 			throws IOException, JavaTranslationException
 	{
 		String packageName = inputClass.getDescribedClassPackageName();
 		String classSimpleName = inputClass.getDescribedClassSimpleName();
 		
 		debug("Generating Java class " + packageName + "." + classSimpleName + "...");
 		if (this.typesScopeName == null)
 			this.typesScopeName = packageName + "." + classSimpleName + "_simpl_types_scope";
 		File outputFile = createFileWithDirStructure(
 				directoryLocation,
 				packageName.split(PACKAGE_NAME_SEPARATOR),
 				classSimpleName,
 				FILE_EXTENSION
 		);
 		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
 		translate(inputClass, implementMappableInterface, bufferedWriter);
 		bufferedWriter.close();
 		debug("done.");
 	}
 
 	/**
 	 * A method to convert the given class descriptor into the java code
 	 * 
 	 * @param inputClass
 	 * @param implementMappable
 	 * @param appendable
 	 * @param generateAbstractClass 
 	 * @throws IOException
 	 * @throws JavaTranslationException
 	 */
 	private void translate(ClassDescriptor inputClass, boolean implementMappable, Appendable appendable)
 			throws IOException, JavaTranslationException
 	{
 		String packageName = inputClass.getDescribedClassPackageName();
 		String classSimpleName = inputClass.getDescribedClassSimpleName();
 		
 		HashMapArrayList<String, ? extends FieldDescriptor> fieldDescriptors =
 				inputClass.getDeclaredFieldDescriptorsByFieldName();
 		inputClass.resolvePolymorphicAnnotations();
 		currentClassDependencies = new HashSet<String>(globalDependencies);
 
 		StringBuilder classBody = StringBuilderUtils.acquire();
 
 		// file header
 		StringBuilder header = StringBuilderUtils.acquire();
 
 		// unit scope
 		openUnitScope(packageName, header);
 		header.append("\n").append(getClaimString(""));
 
 		// class
 		// class: opening
 		openClassBody(inputClass, classBody);
 		// class: fields
 		for (FieldDescriptor fieldDescriptor : fieldDescriptors)
 		{
 			if (fieldDescriptor.belongsTo(inputClass) || inputClass.isCloned() && fieldDescriptor.belongsTo(inputClass.getClonedFrom()))
 				appendField(inputClass, fieldDescriptor, classBody);
 		}
 		// class: constructor(s)
 		appendConstructor(inputClass, classBody, classSimpleName);
 		// class: getters and setters
 		for (FieldDescriptor fieldDescriptor : fieldDescriptors)
 		{
 			if (fieldDescriptor.belongsTo(inputClass) || inputClass.isCloned() && fieldDescriptor.belongsTo(inputClass.getClonedFrom()))
 			{
 				appendGetters(fieldDescriptor, classBody, null);
 				appendSetters(fieldDescriptor, classBody, null);
 			}
 		}
 		// class: mappable
 		if (implementMappable)
 			implementMappable(classBody);
 		// class: closing
 		closeClassBody(classBody);
 
 		// unit scope: closing
 		closeUnitScope(classBody);
 
 		// dependencies
 		currentClassDependencies.addAll(deriveDependencies(inputClass));
 		appendDependencies(currentClassDependencies, header);
 		currentClassDependencies.clear();
 
 		// write to the file stream
 		appendable.append(header);
 		appendable.append(classBody);
 
 		StringBuilderUtils.release(header);
 		StringBuilderUtils.release(classBody);
 	}
 
 	@Override
 	protected void openUnitScope(String unitScopeName, Appendable appendable)
 			throws IOException
 	{
 		currentNamespace = unitScopeName;
 		addLibraryTScopeDependency(currentNamespace);
 
 		appendable.append(PACKAGE);
 		appendable.append(SPACE);
 		appendable.append(currentNamespace);
 		appendable.append(END_LINE);
 		appendable.append(SINGLE_LINE_BREAK);
 	}
 
 	@Override
 	protected void openClassBody(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException
 	{
 		appendClassComments(inputClass, appendable);
 
 		appendClassMetaInformation(inputClass, appendable);
 
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		if (config.isGenerateAbstractClass())
 		{
 			appendable.append("abstract ");
 		}
 		appendable.append(CLASS);
 		appendable.append(SPACE);
 		appendable.append(inputClass.getDescribedClassSimpleName());
 		appendClassGenericTypeVariables(appendable, inputClass);
 
 		ClassDescriptor superCD = inputClass.getSuperClass();
 		if (superCD != null)
 		{
 			appendable.append(SPACE);
 			appendable.append(INHERITANCE_OPERATOR);
 			appendable.append(SPACE);
 			appendable.append(superCD.getDescribedClassSimpleName());
 			appendSuperClassGenericTypeVariables(appendable, inputClass);
 //			appendGenericTypeVariablesHelper(appendable, inputClass.getSuperClassGenericTypeVars(), false);
 			addCurrentClassDependency(superCD.getDescribedClassName());
 		}
 
 		// TODO currently interfaces can only be done through reflection
 		ArrayList<String> interfaces = inputClass.getInterfaceList();
 		if (interfaces != null)
 		{
 			for (int i = 0; i < interfaces.size(); i++)
 			{
 				appendable.append(',');
 				appendable.append(SPACE);
 				appendable.append(interfaces.get(i));
 				implementMappableInterface = true;
 				addCurrentClassDependency(IMappable.class.getPackage().getName());
 			}
 		}
 
 		appendable.append(SINGLE_LINE_BREAK);
 		appendable.append(OPENING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 	}
 
 	@Override
 	protected void appendClassComments(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException
 	{
 		String comment = inputClass.getComment();
 		if (comment != null && comment.length() > 0)
 			appendStructuredComments(appendable, "", comment);
 	}
 
 	/**
 	 * Append class annotations.
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * 
 	 * @throws IOException
 	 */
 	@Override
 	protected void appendClassMetaInformation(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException
 	{
 		List<MetaInformation> metaInfo = inputClass.getMetaInformation();
 		appendClassMetaInformationHook(inputClass, appendable);
 		if (metaInfo != null)
 			for (MetaInformation piece : metaInfo)
 			{
 				appendMetaInformation(piece, appendable);
 			}
 	}
 
 	/**
 	 * (for adding customized annotations, e.g. meta-metadata specific ones)
 	 * 
 	 * @param classDesc
 	 * @param appendable
 	 * 
 	 * @throws IOException
 	 */
 	@Override
 	protected void appendClassMetaInformationHook(ClassDescriptor classDesc, Appendable appendable)
 	{
 		// empty implementation
 	}
 
 	/**
 	 * A method to generate generic type variables of the class.
 	 * 
 	 * @param appendable
 	 * @param inputClass
 	 * 
 	 * @throws IOException
 	 */
 	@Override
 	protected void appendClassGenericTypeVariables(Appendable appendable, ClassDescriptor inputClass)
 			throws IOException
 	{
 		ArrayList<GenericTypeVar> genericTypeVars = inputClass.getGenericTypeVars();
 		appendGenericTypeVariablesHelper(appendable, genericTypeVars, true);
 	}
 
 	@Override
 	protected void appendSuperClassGenericTypeVariables(Appendable appendable, ClassDescriptor inputClass)
 			throws IOException
 	{
 		ArrayList<GenericTypeVar> superClassGenericTypeVars = inputClass.getSuperClassGenericTypeVars();
 		appendGenericTypeVariablesHelper(appendable, superClassGenericTypeVars, false);
 	}
 	
 	/**
 	 * 
 	 * @param appendable
 	 * @param genericTypeVars
 	 * @param withBounds
 	 *          set to true if you need bounds, or false if you don't. typically, in superclass
 	 *          generic defs, you don't want bounds.
 	 * @throws IOException
 	 */
 	protected void appendGenericTypeVariablesHelper(Appendable appendable,
 			ArrayList<GenericTypeVar> genericTypeVars, boolean withBounds) throws IOException
 	{
 		if (genericTypeVars == null || genericTypeVars.size() == 0)
 			return;
 		
 		appendable.append('<');
 		for (int i = 0; i < genericTypeVars.size(); ++i)
 		{
 			if (i > 0)
 				appendable.append(", ");
 			GenericTypeVar genericTypeVar = genericTypeVars.get(i);
 			
 			if (genericTypeVar.getName() != null)
 			{
 				// case 1: <T extends ConstraintClass> or <T>
 				appendable.append(genericTypeVar.getName());
 				ClassDescriptor classBound = genericTypeVar.getConstraintClassDescriptor();
 				if (classBound != null && withBounds)
 					appendable.append(" extends ").append(classBound.getDescribedClassSimpleName());
 			}
 			else if (genericTypeVar.getClassDescriptor() != null)
 			{
 				// case 2: <ConcreteClass>
 				ClassDescriptor concreteClassBound = genericTypeVar.getClassDescriptor();
 				appendable.append(concreteClassBound.getDescribedClassSimpleName());
 				addCurrentClassDependency(concreteClassBound.getDescribedClassName());
 			}
 		}
 		appendable.append('>');
 	}
 
 	@Override
 	protected void appendField(ClassDescriptor contextCd, FieldDescriptor fieldDescriptor,
 			Appendable appendable)
 			throws IOException, JavaTranslationException
 	{
 		String javaType = fieldDescriptor.getJavaType();
 		if (javaType == null)
 		{
 			error("ERROR, no valid JavaType found for : " + fieldDescriptor);
 			return;
 		}
 
 		boolean isKeyword = checkForKeywords(fieldDescriptor);
 		if (isKeyword)
 			appendable.append(OPEN_BLOCK_COMMENTS).append(SINGLE_LINE_BREAK);
 		appendFieldComments(fieldDescriptor, appendable);
 		appendFieldMetaInformation(contextCd, fieldDescriptor, appendable);
 		appendable.append(TAB);
 		appendable.append(PRIVATE);
 		appendable.append(SPACE);
 		appendable.append(javaType);
 		appendFieldGenericTypeVars(contextCd, fieldDescriptor, appendable);
 		appendable.append(SPACE);
 		appendable.append(fieldDescriptor.getName());
 		appendable.append(END_LINE);
 		appendable.append(DOUBLE_LINE_BREAK);
 		if (isKeyword)
 			appendable.append(CLOSE_BLOCK_COMMENTS).append(SINGLE_LINE_BREAK);
 	}
 
 	@Override
 	protected void appendFieldGenericTypeVars(ClassDescriptor contextCd,
 			FieldDescriptor fieldDescriptor, Appendable appendable) throws IOException
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	private Set<String> deriveDependencies(ClassDescriptor inputClass)
 	{
 		Set<String> dependencies = new HashSet<String>();
 
 		Set<ScalarType> scalarDependencies = inputClass.deriveScalarDependencies();
 		for (ScalarType scalarDependency : scalarDependencies)
 			dependencies.add(scalarDependency.getJavaTypeName());
 
 		Set<ClassDescriptor> compositeDependencies = inputClass.deriveCompositeDependencies();
 		for (ClassDescriptor compositeDependency : compositeDependencies)
 			dependencies.add(compositeDependency.getJavaTypeName());
 
 		Set<CollectionType> colletionDependencies = inputClass.deriveCollectionDependencies();
 		for (CollectionType collectionDependency : colletionDependencies)
 			dependencies.add(collectionDependency.getJavaTypeName());
 
 		return dependencies;
 	}
 
 	@Override
 	protected void appendFieldComments(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException
 	{
 		String comment = fieldDescriptor.getComment();
 		if (comment != null && comment.length() > 0)
 			appendStructuredComments(appendable, TAB, comment);
 	}
 
 	@Override
 	protected void appendFieldMetaInformation(ClassDescriptor contextCd,
 			FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException
 	{
 		List<MetaInformation> metaInfo = fieldDescriptor.getMetaInformation();
 		appendFieldMetaInformationHook(contextCd, fieldDescriptor, appendable);
 		if (metaInfo != null)
 			for (MetaInformation piece : metaInfo)
 			{
 				appendable.append(TAB);
 				appendMetaInformation(piece, appendable);
 			}
 	}
 
 	@Override
 	protected void appendFieldMetaInformationHook(ClassDescriptor contextCd,
 			FieldDescriptor fieldDesc, Appendable appendable) throws IOException
 	{
 
 	}
 
 	@Override
 	protected void appendConstructor(ClassDescriptor inputClass, Appendable appendable, String classSimpleName)
 			throws IOException
 	{
 		appendDefaultConstructor(classSimpleName, appendable);
 
 		appendConstructorHook(inputClass, appendable, classSimpleName);
 	}
 
 	@Override
 	protected void appendConstructorHook(ClassDescriptor inputClass, Appendable appendable, String classSimpleName)
 			throws IOException
 	{
 		// for derived classes to use.
 	}
 
 	@Override
 	protected void appendDefaultConstructor(String className, Appendable appendable)
 			throws IOException
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
 		appendable.append("super();");
 		appendable.append(SPACE);
 		appendable.append(CLOSING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 	}
 
 	@Override
 	protected void appendGettersAndSetters(ClassDescriptor context, FieldDescriptor fieldDescriptor,
 			Appendable appendable)
 			throws IOException
 	{
 		appendGetters(fieldDescriptor, appendable, null);
 		appendSetters(fieldDescriptor, appendable, null);
 	}
 
 	@Override
 	protected void appendGettersAndSettersHook(ClassDescriptor context,
 			FieldDescriptor fieldDescriptor, Appendable appendable)
 	{
 		// for derived classes to use.
 	}
 
 	/**
 	 * A method to generate get method for the given field Descriptor
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @param suffix
 	 * @throws IOException
 	 */
 	protected void appendGetters(FieldDescriptor fieldDescriptor, Appendable appendable, String suffix)
 			throws IOException
 	{
 		String javaType = fieldDescriptor.getJavaType();
 		if (javaType == null)
 		{
 			System.out.println("ERROR, no valid JavaType found for : " + fieldDescriptor);
 			return;
 		}
 
 		appendGettersHelper(fieldDescriptor, javaType, appendable, suffix);
 		
 		int fieldType = fieldDescriptor.getType();
 		if (fieldType == FieldTypes.COLLECTION_ELEMENT || fieldType == FieldTypes.COLLECTION_SCALAR)
 		{
 			ST listMethods = templateGroup.getInstanceOf("listMethods");
 			listMethods.add("fd", fieldDescriptor);
			StringWriter sw = new StringWriter();
			STWriter writer = new AutoIndentWriter(sw, "\n");
			listMethods.write(writer);
			String listMethodsStr = sw.toString();
 			appendable.append(listMethodsStr);
 		}
 	}
 
 	protected void appendGettersHelper(FieldDescriptor fieldDescriptor, String javaType,
 			Appendable appendable, String suffix) throws IOException
 	{
 		appendable.append(SINGLE_LINE_BREAK);
 
 		boolean isKeyword = checkForKeywords(fieldDescriptor);
 		if (isKeyword)
 			appendable.append(OPEN_BLOCK_COMMENTS).append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		appendable.append(javaType);
 		appendable.append(SPACE);
 		appendable.append(JavaTranslationUtilities.getGetMethodName(fieldDescriptor)
 				+ (suffix == null ? "" : suffix));
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
 		if (isKeyword)
 			appendable.append(CLOSE_BLOCK_COMMENTS).append(SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * A method to generate set method for the given field descriptor
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @param suffix
 	 * @throws IOException
 	 */
 	protected void appendSetters(FieldDescriptor fieldDescriptor, Appendable appendable, String suffix)
 			throws IOException
 	{
 		String javaType = fieldDescriptor.getJavaType();
 		if (javaType == null)
 		{
 			System.out.println("ERROR, no valid JavaType found for : " + fieldDescriptor);
 			return;
 		}
 
 		appendSettersHelper(fieldDescriptor, javaType, appendable, suffix);
 	}
 
 	protected void appendSettersHelper(FieldDescriptor fieldDescriptor, String javaType,
 			Appendable appendable, String suffix) throws IOException
 	{
 		appendable.append(SINGLE_LINE_BREAK);
 
 		boolean isKeyword = checkForKeywords(fieldDescriptor);
 		if (isKeyword)
 			appendable.append(OPEN_BLOCK_COMMENTS).append(SINGLE_LINE_BREAK);
 		appendable.append(TAB);
 		appendable.append(PUBLIC);
 		appendable.append(SPACE);
 		appendable.append(VOID);
 		appendable.append(SPACE);
 		appendable.append(JavaTranslationUtilities.getSetMethodName(fieldDescriptor)
 				+ (suffix == null ? "" : suffix));
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
 		if (isKeyword)
 			appendable.append(CLOSE_BLOCK_COMMENTS).append(SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void implementMappable(Appendable appendable) throws IOException
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
 
 	@Override
 	protected void closeClassBody(Appendable appendable) throws IOException
 	{
 		appendable.append(CLOSING_CURLY_BRACE);
 		appendable.append(SINGLE_LINE_BREAK);
 	}
 
 	@Override
 	protected void closeUnitScope(Appendable appendable) throws IOException
 	{
 		// nothing to do for java.
 	}
 
 	@Override
 	protected void appendDependencies(Collection<String> dependencies, Appendable appendable)
 			throws IOException
 	{
 		if (dependencies != null && dependencies.size() > 0)
 		{
 			List<String> sortedDependencies = new ArrayList<String>(dependencies);
 			Collections.sort(sortedDependencies);
 			for (String dependency : sortedDependencies)
 			{
 				appendable.append(SINGLE_LINE_BREAK);
 				appendable.append(IMPORT);
 				appendable.append(SPACE);
 				appendable.append(dependency);
 				appendable.append(END_LINE);
 			}
 		}
 		appendable.append(DOUBLE_LINE_BREAK);
 	}
 
 	/**
 	 * A method to test whether fieldAccessor is a java keyword
 	 * 
 	 * @param fieldDescriptor
 	 * @return
 	 * @throws IOException
 	 */
 	protected boolean checkForKeywords(FieldDescriptor fieldDescriptor)
 			throws IOException
 	{
 		if (JavaTranslationUtilities.isKeyword(fieldDescriptor.getName()))
 		{
 			Debug.warning(fieldDescriptor, " Field Name: [" + fieldDescriptor.getName()
 					+ "]. This is a keyword in Java. Cannot translate.");
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected void appendStructuredComments(Appendable appendable, String spacing, String... comments)
 			throws IOException
 	{
 		appendable.append(spacing).append(OPEN_COMMENTS).append(SINGLE_LINE_BREAK);
 		if (comments != null && comments.length > 0)
 			for (String comment : comments)
 				appendable.append(spacing).append(XML_COMMENTS).append(comment).append(SINGLE_LINE_BREAK);
 		else
 			appendable.append(spacing).append(XML_COMMENTS).append("(missing comments)")
 					.append(SINGLE_LINE_BREAK);
 		appendable.append(spacing).append(CLOSE_COMMENTS).append(SINGLE_LINE_BREAK);
 	}
 
 	@Override
 	protected void appendMetaInformation(MetaInformation metaInfo, Appendable appendable)
 			throws IOException
 	{
 		addCurrentClassDependency(metaInfo.typeName);
 
 		appendable.append("@").append(metaInfo.simpleTypeName);
 		if (metaInfo.args != null && metaInfo.args.size() > 0)
 		{
 			appendable.append("(");
 			if (metaInfo.argsInArray)
 			{
 				appendable.append("{");
 				for (int i = 0; i < metaInfo.args.size(); ++i)
 					appendable.append(i == 0 ? "" : ", ").append(translateMetaInfoArgValue(metaInfo.args.get(i).value));
 				appendable.append("}");
 			}
 			else
 			{
 				for (int i = 0; i < metaInfo.args.size(); ++i)
 				{
 					Argument a = metaInfo.args.get(i);
 					appendable.append(i == 0 ? "" : ", ").append(metaInfo.args.size() > 1 ? a.name + " = " : "").append(translateMetaInfoArgValue(a.value));
 				}
 			}
 			appendable.append(")");
 		}
 		appendable.append(SINGLE_LINE_BREAK);
 	}
 
 	public String translateMetaInfoArgValue(Object argValue)
 	{
 		// TODO to make this extendible, use an interface MetaInfoArgValueTranslator and allow users
 		//      to inject new ones to handle different kind of cases.
 		if (argValue instanceof String)
 		{
 			return "\"" + argValue.toString() + "\"";
 		}
 		else if (argValue instanceof Hint)
 		{
 			addCurrentClassDependency(Hint.class.getName());
 			switch ((Hint) argValue)
 			{
 			case XML_ATTRIBUTE: return "Hint.XML_ATTRIBUTE"; 
 			case XML_LEAF: return "Hint.XML_LEAF"; 
 			case XML_LEAF_CDATA: return "Hint.XML_LEAF_CDATA"; 
 			case XML_TEXT: return "Hint.XML_TEXT"; 
 			case XML_TEXT_CDATA: return "Hint.XML_TEXT_CDATA"; 
 			default: return "Hint.UNDEFINED";
 			}
 		}
 		else if (argValue instanceof Class)
 		{
 			addCurrentClassDependency(((Class) argValue).getName());
 			return ((Class) argValue).getSimpleName() + ".class";
 		}
 		// eles if (argValue instanceof ClassDescriptor)
 		return null;
 	}
 
 	@Override
 	public void addGlobalDependency(String name)
 	{
 		if (name != null)
 			globalDependencies.add(name);
 	}
 
 	@Override
 	public void addCurrentClassDependency(String name)
 	{
 		if (name != null)
 			currentClassDependencies.add(name);
 	}
 
 	@Override
 	public void addLibraryTScopeDependency(String name)
 	{
 		if (name != null)
 			libraryTScopeDependencies.add(name);
 	}
 
 	public String getClaimString(String spacing)
 	{
 		if (claimString == null)
 		{
 			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
 			DateFormat yearFormat = new SimpleDateFormat("yyyy");
 			Date date = new Date();
 
 			StringBuilder sb = StringBuilderUtils.acquire();
 			sb.append(spacing).append("/**\n");
 //			sb.append(spacing).append(" * Automatically generated by ").append(this.getClassSimpleName()).append(" on ").append(dateFormat.format(date)).append("\n");
 			sb.append(spacing).append(" * Automatically generated by ").append(this.getClassSimpleName()).append("\n");
 			sb.append(spacing).append(" *\n");
 			sb.append(spacing).append(" * DO NOT modify this code manually: All your changes may get lost!\n");
 			sb.append(spacing).append(" *\n");
 			sb.append(spacing).append(" * Copyright (" + yearFormat.format(date) + ") Interface Ecology Lab.\n");
 			sb.append(spacing).append(" */\n");
 			claimString = sb.toString();
 			StringBuilderUtils.release(sb);
 		}
 		return claimString;
 	}
 
 	@Override
 	public void generateLibraryTScopeClass(File directoryLocation, SimplTypesScope tScope, String tScopeClassPackage, String tScopeClassSimpleName)
 			throws IOException
 	{
 		String packageName = tScopeClassPackage;
 		String tscopeClassName = tScopeClassSimpleName;
 
 		File sourceFile = createFileWithDirStructure(
 				directoryLocation,
 				packageName.split(PACKAGE_NAME_SEPARATOR),
 				tscopeClassName,
 				FILE_EXTENSION
 				);
 		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(sourceFile));
 
 		// package
 		bufferedWriter.append(PACKAGE);
 		bufferedWriter.append(SPACE);
 		bufferedWriter.append(packageName);
 		bufferedWriter.append(END_LINE);
 		bufferedWriter.append(SINGLE_LINE_BREAK);
 		bufferedWriter.append(SINGLE_LINE_BREAK);
 
 		// claims (as class javadoc)
 		bufferedWriter.append(getClaimString(""));
 
 		// append dependencies
 		addGlobalDependency(SimplTypesScope.class.getName());
 		appendDependencies(globalDependencies, bufferedWriter);
 
 		// write the class
 		bufferedWriter.append("public class ").append(tscopeClassName).append("\n");
 		bufferedWriter.append("{\n\n");
 		bufferedWriter.append("\tprotected static final Class TRANSLATIONS[] =\n\t{\n");
 		appendTranslatedClassList(tScope, bufferedWriter);
 		bufferedWriter.append("\t};\n\n");
 
 		// write get() method
 		generateLibraryTScopeGetter(bufferedWriter, tScope.getName());
 
 		// end the class
 		bufferedWriter.append("}\n");
 
 		bufferedWriter.close();
 	}
 
 	protected void generateLibraryTScopeGetter(Appendable appendable, String typesScopeName) throws IOException
 	{
 		appendable.append("\tpublic static ").append(JAVA_TRANSLATION_SCOPE).append(" get()\n\t{\n");
 		appendable.append("\t\treturn ").append(JAVA_TRANSLATION_SCOPE).append(".get(\"").append(typesScopeName).append("\", TRANSLATIONS);\n");
 		appendable.append("\t}\n\n");
 	}
 
 	@Override
 	protected void appendTranslatedClassList(SimplTypesScope tScope, Appendable appendable)
 			throws IOException
 	{
 		List<String> classes = new ArrayList<String>();
 		Collection<ClassDescriptor<? extends FieldDescriptor>> allClasses = tScope.entriesByClassName()
 				.values();
 		for (ClassDescriptor<? extends FieldDescriptor> myClass : allClasses)
 			if (!excludeClassesFromTranslation.contains(myClass))
 				classes.add("\t\t" + myClass.getDescribedClassName() + ".class,\n\n");
 		Collections.sort(classes);
 		for (String classItem : classes)
 			appendable.append(classItem);
 	}
 
 	@Override
 	public void excludeClassFromTranslation(ClassDescriptor someClass)
 	{
 		excludeClassesFromTranslation.add(someClass);
 	}
 
 }
