 package sk.stuba.fiit.perconik.eclipse.jdt.core;
 
 import java.util.Set;
 import org.eclipse.jdt.core.IAnnotation;
 import org.eclipse.jdt.core.IClassFile;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IImportContainer;
 import org.eclipse.jdt.core.IImportDeclaration;
 import org.eclipse.jdt.core.IInitializer;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaModel;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.ILocalVariable;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IPackageDeclaration;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeParameter;
 import sk.stuba.fiit.perconik.utilities.constant.IntegralConstant;
 import sk.stuba.fiit.perconik.utilities.constant.IntegralConstantSupport;
 import sk.stuba.fiit.perconik.utilities.constant.TypeConstant;
 import sk.stuba.fiit.perconik.utilities.constant.TypeConstantSupport;
 
 /**
  * Java element types.
  * 
  * @see IJavaElement
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public enum JavaElementType implements IntegralConstant, TypeConstant<IJavaElement>
 {
 	/**
 	 * @see IJavaElement#JAVA_MODEL
 	 */
 	JAVA_MODEL(IJavaElement.JAVA_MODEL, IJavaModel.class),
 	
 	/**
 	 * @see IJavaElement#JAVA_PROJECT
 	 */
 	JAVA_PROJECT(IJavaElement.JAVA_PROJECT, IJavaProject.class),
 	
 	/**
 	 * @see IJavaElement#PACKAGE_FRAGMENT_ROOT
 	 */
 	PACKAGE_FRAGMENT_ROOT(IJavaElement.PACKAGE_FRAGMENT_ROOT, IPackageFragmentRoot.class),
 	
 	/**
 	 * @see IJavaElement#PACKAGE_FRAGMENT
 	 */
 	PACKAGE_FRAGMENT(IJavaElement.PACKAGE_FRAGMENT, IPackageFragment.class),
 	
 	/**
 	 * @see IJavaElement#COMPILATION_UNIT
 	 */
 	COMPILATION_UNIT(IJavaElement.COMPILATION_UNIT, ICompilationUnit.class),
 	
 	/**
 	 * @see IJavaElement#CLASS_FILE
 	 */
 	CLASS_FILE(IJavaElement.CLASS_FILE, IClassFile.class),
 	
 	/**
 	 * @see IJavaElement#TYPE
 	 */
 	TYPE(IJavaElement.TYPE, IType.class),
 	
 	/**
 	 * @see IJavaElement#FIELD
 	 */
 	FIELD(IJavaElement.FIELD, IField.class),
 	/**
 	 * @see IJavaElement#METHOD
 	 */
 	METHOD(IJavaElement.METHOD, IMethod.class),
 	
 	/**
 	 * @see IJavaElement#INITIALIZER
 	 */
 	INITIALIZER(IJavaElement.INITIALIZER, IInitializer.class),
 	
 	/**
 	 * @see IJavaElement#PACKAGE_DECLARATION
 	 */
 	PACKAGE_DECLARATION(IJavaElement.PACKAGE_DECLARATION, IPackageDeclaration.class),
 	
 	/**
 	 * @see IJavaElement#IMPORT_CONTAINER
 	 */
 	IMPORT_CONTAINER(IJavaElement.IMPORT_CONTAINER, IImportContainer.class),
 	
 	/**
 	 * @see IJavaElement#IMPORT_DECLARATION
 	 */
 	IMPORT_DECLARATION(IJavaElement.IMPORT_DECLARATION, IImportDeclaration.class),
 	
 	/**
 	 * @see IJavaElement#LOCAL_VARIABLE
 	 */
 	LOCAL_VARIABLE(IJavaElement.LOCAL_VARIABLE, ILocalVariable.class),
 	
 	/**
 	 * @see IJavaElement#TYPE_PARAMETER
 	 */
 	TYPE_PARAMETER(IJavaElement.TYPE_PARAMETER, ITypeParameter.class),
 	
 	/**
 	 * @see IJavaElement#ANNOTATION
 	 */
 	ANNOTATION(IJavaElement.ANNOTATION, IAnnotation.class);
 
 	private static final IntegralConstantSupport<JavaElementType> integers = IntegralConstantSupport.of(JavaElementType.class);
 	
 	private static final TypeConstantSupport<JavaElementType, IJavaElement> types = TypeConstantSupport.of(JavaElementType.class);
 
 	private final int value;
 	
 	private final Class<? extends IJavaElement> type;
 	
 	private JavaElementType(final int value, final Class<? extends IJavaElement> type)
 	{
 		assert type != null;
 		
 		this.value = value;
 		this.type  = type;
 	}
 	
 	public static final Set<Integer> valuesAsIntegers()
 	{
 		return integers.getIntegers();
 	}
 	
 	public static final Set<Class<? extends IJavaElement>> valuesAsTypes()
 	{
 		return types.getTypes();
 	}
 	
 	public static final JavaElementType valueOf(final int value)
 	{
 		return integers.getConstant(value);
 	}
 
 	public static final JavaElementType valueOf(final Class<? extends IJavaElement> type)
 	{
 		return types.getConstant(type);
 	}
 
 	public static final JavaElementType valueOf(final IJavaElement element)
 	{
		return valueOf(element.getElementType());
 	}
 	
 	public final int getValue()
 	{
 		return this.value;
 	}
 
 	public final Class<? extends IJavaElement> getType()
 	{
 		return this.type;
 	}
 }
