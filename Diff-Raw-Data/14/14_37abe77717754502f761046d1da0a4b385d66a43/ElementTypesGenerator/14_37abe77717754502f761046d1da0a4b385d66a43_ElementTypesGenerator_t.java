 package org.eclipse.gmf.codegen.templates.providers;
 
 import java.util.*;
 import org.eclipse.emf.codegen.ecore.genmodel.*;
 import org.eclipse.gmf.codegen.gmfgen.*;
 import org.eclipse.gmf.common.codegen.*;
 
 public class ElementTypesGenerator
 {
   protected static String nl;
   public static synchronized ElementTypesGenerator create(String lineSeparator)
   {
     nl = lineSeparator;
     ElementTypesGenerator result = new ElementTypesGenerator();
     nl = null;
     return result;
   }
 
   protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "";
   protected final String TEXT_2 = NL + "/*" + NL + " *";
   protected final String TEXT_3 = NL + " */";
   protected final String TEXT_4 = NL;
   protected final String TEXT_5 = NL + NL + "import java.util.HashSet;" + NL + "import java.util.IdentityHashMap;" + NL + "import java.util.Map;" + NL + "import java.util.Set;" + NL + "import org.eclipse.core.runtime.IAdaptable;" + NL + "import org.eclipse.emf.ecore.EClass;" + NL + "import org.eclipse.emf.ecore.ENamedElement;" + NL + "import org.eclipse.emf.ecore.EStructuralFeature;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.IElementType;" + NL + "import org.eclipse.jface.resource.ImageDescriptor;" + NL + "import org.eclipse.jface.resource.ImageRegistry;" + NL + "import org.eclipse.swt.graphics.Image;";
   protected final String TEXT_6 = NL + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "public class ";
   protected final String TEXT_7 = " {" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_8 = "() {}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate static Map elements;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate static ImageRegistry imageRegistry;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate static ImageRegistry getImageRegistry() {" + NL + "\t\tif (imageRegistry == null) {" + NL + "\t\t\timageRegistry = new ImageRegistry();" + NL + "\t\t}" + NL + "\t\treturn imageRegistry;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate static String getImageRegistryKey(ENamedElement element) {" + NL + "\t\treturn element.getName();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate static ImageDescriptor getProvidedImageDescriptor(ENamedElement element) {" + NL + "\t\tif (element instanceof EStructuralFeature) {" + NL + "\t\t\telement = ((EStructuralFeature) element).getEContainingClass();" + NL + "\t\t}" + NL + "\t\tif (element instanceof EClass) {";
   protected final String TEXT_9 = NL + "\t\t\tEClass eClass = (EClass) element;" + NL + "\t\t\tif (!eClass.isAbstract()) {" + NL + "\t\t\t\treturn ";
   protected final String TEXT_10 = ".getInstance().getItemImageDescriptor(eClass.getEPackage().getEFactoryInstance().create(eClass));" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t\t// TODO : support structural features" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic static ImageDescriptor getImageDescriptor(ENamedElement element) {" + NL + "\t\tString key = getImageRegistryKey(element);" + NL + "\t\tImageDescriptor imageDescriptor = getImageRegistry().getDescriptor(key);" + NL + "\t\tif (imageDescriptor == null) {" + NL + "\t\t\timageDescriptor = getProvidedImageDescriptor(element);" + NL + "\t\t\tif (imageDescriptor == null) {" + NL + "\t\t\t\timageDescriptor = ImageDescriptor.getMissingImageDescriptor();" + NL + "\t\t\t}" + NL + "\t\t\tgetImageRegistry().put(key, imageDescriptor);" + NL + "\t\t}" + NL + "\t\treturn imageDescriptor;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic static Image getImage(ENamedElement element) {" + NL + "\t\tString key = getImageRegistryKey(element);" + NL + "\t\tImage image = getImageRegistry().get(key);" + NL + "\t\tif (image == null) {" + NL + "\t\t\tImageDescriptor imageDescriptor = getProvidedImageDescriptor(element);" + NL + "\t\t\tif (imageDescriptor == null) {" + NL + "\t\t\t\timageDescriptor = ImageDescriptor.getMissingImageDescriptor();" + NL + "\t\t\t}" + NL + "\t\t\tgetImageRegistry().put(key, imageDescriptor);" + NL + "\t\t\timage = getImageRegistry().get(key);" + NL + "\t\t}" + NL + "\t\treturn image;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic static ImageDescriptor getImageDescriptor(IAdaptable hint) {" + NL + "\t\tENamedElement element = getElement(hint);" + NL + "\t\tif (element == null) {" + NL + "\t\t\treturn null;" + NL + "\t\t}" + NL + "\t\treturn getImageDescriptor(element);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic static Image getImage(IAdaptable hint) {" + NL + "\t\tENamedElement element = getElement(hint);" + NL + "\t\tif (element == null) {" + NL + "\t\t\treturn null;" + NL + "\t\t}" + NL + "\t\treturn getImage(element);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * Returns 'type' of the ecore object associated with the hint." + NL + "\t * " + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic static ENamedElement getElement(IAdaptable hint) {" + NL + "\t\tObject type = hint.getAdapter(IElementType.class);" + NL + "\t\tif (elements == null) {" + NL + "\t\t\telements = new IdentityHashMap();";
   protected final String TEXT_11 = NL + "\t\t\telements.put(";
   protected final String TEXT_12 = ", ";
   protected final String TEXT_13 = ".eINSTANCE.get";
   protected final String TEXT_14 = "());";
   protected final String TEXT_15 = NL + "\t\t\telements.put(";
   protected final String TEXT_16 = ", ";
   protected final String TEXT_17 = ".eINSTANCE.get";
   protected final String TEXT_18 = "());";
   protected final String TEXT_19 = NL + "\t\t\telements.put(";
   protected final String TEXT_20 = ", ";
   protected final String TEXT_21 = ".eINSTANCE.get";
   protected final String TEXT_22 = "());";
   protected final String TEXT_23 = NL + "\t\t}" + NL + "\t\treturn (ENamedElement) elements.get(type);" + NL + "\t}" + NL;
   protected final String TEXT_24 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic static final IElementType ";
   protected final String TEXT_25 = " = getElementType(\"";
   protected final String TEXT_26 = "\"); //$NON-NLS-1$";
   protected final String TEXT_27 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate static IElementType getElementType(String id) {" + NL + "\t\treturn ElementTypeRegistry.getInstance().getType(id);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate static Set KNOWN_ELEMENT_TYPES;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic static boolean isKnownElementType(IElementType elementType) {" + NL + "\t\tif (KNOWN_ELEMENT_TYPES == null) {" + NL + "\t\t\tKNOWN_ELEMENT_TYPES = new HashSet();";
   protected final String TEXT_28 = NL + "\t\t\tKNOWN_ELEMENT_TYPES.add(";
   protected final String TEXT_29 = ");";
   protected final String TEXT_30 = NL + "\t\t}" + NL + "\t\treturn KNOWN_ELEMENT_TYPES.contains(elementType);" + NL + "\t}";
   protected final String TEXT_31 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic static class Initializers {";
   protected final String TEXT_32 = "\t\t";
   protected final String TEXT_33 = NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tpublic static final IObjectInitializer ";
   protected final String TEXT_34 = " = new ObjectInitializer(";
   protected final String TEXT_35 = ") {";
   protected final String TEXT_36 = NL + "\t\tObjectInitializer ";
   protected final String TEXT_37 = "() {" + NL + "\t\t\treturn new ObjectInitializer(";
   protected final String TEXT_38 = ") {";
   protected final String TEXT_39 = NL + "\t\t \tprotected void init() {\t\t\t\t";
   protected final String TEXT_40 = "\t\t\t" + NL + "\t\t\t\tadd(createNewElementFeatureInitializer(";
   protected final String TEXT_41 = ", new ObjectInitializer[] {";
   protected final String TEXT_42 = NL + "\t\t\t\t\t";
   protected final String TEXT_43 = "(), ";
   protected final String TEXT_44 = NL + "\t\t\t\t}));";
   protected final String TEXT_45 = "\t\t\t" + NL + "\t\t\t\tadd(createExpressionFeatureInitializer(";
   protected final String TEXT_46 = ", ";
   protected final String TEXT_47 = "null";
   protected final String TEXT_48 = ".";
   protected final String TEXT_49 = "(";
   protected final String TEXT_50 = ", //$NON-NLS-1$" + NL + "\t";
   protected final String TEXT_51 = ", ";
   protected final String TEXT_52 = ")";
   protected final String TEXT_53 = ".";
   protected final String TEXT_54 = "(";
   protected final String TEXT_55 = ", //$NON-NLS-1$" + NL + "\t";
   protected final String TEXT_56 = ")";
   protected final String TEXT_57 = "new ";
   protected final String TEXT_58 = "(";
   protected final String TEXT_59 = ") {" + NL + "\tprotected Object doEvaluate(Object context, ";
   protected final String TEXT_60 = " env) {\t" + NL + "\t\t";
   protected final String TEXT_61 = " self = (";
   protected final String TEXT_62 = ")context;";
   protected final String TEXT_63 = "\t" + NL + "\t\t";
   protected final String TEXT_64 = " ";
   protected final String TEXT_65 = " = (";
   protected final String TEXT_66 = ")env.get(\"";
   protected final String TEXT_67 = "\"); //$NON-NLS-1$";
   protected final String TEXT_68 = NL + "\t\treturn ";
   protected final String TEXT_69 = ".";
   protected final String TEXT_70 = "(self";
   protected final String TEXT_71 = ", ";
   protected final String TEXT_72 = ");" + NL + "\t}" + NL + "}";
   protected final String TEXT_73 = NL + "));";
   protected final String TEXT_74 = "\t\t\t}";
   protected final String TEXT_75 = NL + "\t\t\t\t}; // ";
   protected final String TEXT_76 = " ObjectInitializer" + NL + "\t\t\t}";
   protected final String TEXT_77 = NL + "\t\t}; // ";
   protected final String TEXT_78 = " ObjectInitializer";
   protected final String TEXT_79 = NL + "\t" + NL + "\t\t/** " + NL + "\t\t * @generated" + NL + "\t\t */\t" + NL + "\t\tprivate Initializers() {" + NL + "\t\t}" + NL + "\t\t" + NL + "\t\t/** " + NL + "\t\t * @generated" + NL + "\t\t */\t\t" + NL + "\t\tpublic static interface IObjectInitializer {" + NL + "\t\t\t/** " + NL + "\t\t\t * @generated" + NL + "\t\t\t */\t\t" + NL + "\t\t\tpublic void init(";
   protected final String TEXT_80 = " instance);" + NL + "\t\t}" + NL + "\t\t" + NL + "\t\t/** " + NL + "\t\t * @generated" + NL + "\t\t */\t" + NL + "\t\tpublic static abstract class ObjectInitializer implements IObjectInitializer {" + NL + "\t\t\t/** " + NL + "\t\t\t * @generated" + NL + "\t\t\t */\t\t" + NL + "\t\t\tfinal EClass element;\t\t" + NL + "\t\t\t/** " + NL + "\t\t\t * @generated" + NL + "\t\t\t */\t\t" + NL + "\t\t\tprivate ";
   protected final String TEXT_81 = " featureInitializers = new ";
  protected final String TEXT_82 = "();" + NL + "\t\t\t/** " + NL + "\t\t\t * @generated" + NL + "\t\t\t */" + NL + "\t\t\tObjectInitializer(EClass element) {" + NL + "\t\t\t\tthis.element = element;" + NL + "\t\t\t\tinit();" + NL + "\t\t\t}" + NL + "\t\t\t/**" + NL + "\t\t \t * @generated" + NL + "\t\t \t */" + NL + "\t\t \tprotected abstract void init();\t\t\t\t\t\t\t" + NL + "\t\t\t/** " + NL + "\t\t\t * @generated" + NL + "\t\t\t */" + NL + "\t\t\tprotected final IFeatureInitializer add(IFeatureInitializer initializer) {" + NL + "\t\t\t\tfeatureInitializers.add(initializer);" + NL + "\t\t\t\treturn initializer;" + NL + "\t\t\t}" + NL + "\t\t\t/** " + NL + "\t\t\t * @generated" + NL + "\t\t\t */\t\t" + NL + "\t\t\tpublic void init(";
  protected final String TEXT_83 = " instance) {" + NL + "\t\t\t\tfor (java.util.Iterator it = featureInitializers.iterator(); it.hasNext();) {" + NL + "\t\t\t\t\tIFeatureInitializer nextExpr = (IFeatureInitializer)it.next();" + NL + "\t\t\t\t\ttry {" + NL + "\t\t\t\t\t\tnextExpr.init(instance);" + NL + "\t\t\t\t\t} catch(RuntimeException e) {" + NL + "\t\t\t\t\t\t";
  protected final String TEXT_84 = ".getInstance().logError(\"Feature initialization failed\", e); //$NON-NLS-1$\t\t\t\t\t\t" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t}" + NL + "\t\t\t}" + NL + "\t\t} // end of ObjectInitializer" + NL + "\t\t" + NL + "\t\t/** " + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tinterface IFeatureInitializer {" + NL + "\t\t\t/**" + NL + "\t\t\t * @generated" + NL + "\t\t\t */" + NL + "\t\t\tvoid init(EObject contextInstance);" + NL + "\t\t}" + NL + "\t\t" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */\t\t" + NL + "\t\tstatic IFeatureInitializer createNewElementFeatureInitializer(EStructuralFeature initFeature, ObjectInitializer[] newObjectInitializers) {" + NL + "\t\t\tfinal EStructuralFeature feature = initFeature;" + NL + "\t\t\tfinal ObjectInitializer[] initializers = newObjectInitializers;" + NL + "\t\t\treturn new IFeatureInitializer() {" + NL + "\t\t\t\tpublic void init(EObject contextInstance) {" + NL + "\t\t\t\t\tfor (int i = 0; i < initializers.length; i++) {" + NL + "\t\t\t\t\t\tEObject newInstance = initializers[i].element.getEPackage().getEFactoryInstance().create(initializers[i].element);" + NL + "\t\t\t\t\t\tif(feature.isMany()) {" + NL + "\t\t\t\t\t\t\t((";
  protected final String TEXT_85 = ")contextInstance.eGet(feature)).add(newInstance);" + NL + "\t\t\t\t\t\t} else {" + NL + "\t\t\t\t\t\t\tcontextInstance.eSet(feature, newInstance);" + NL + "\t\t\t\t\t\t}\t\t\t\t\t\t" + NL + "\t\t\t\t\t\tinitializers[i].init(newInstance);" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t}" + NL + "\t\t\t};" + NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */\t\t" + NL + "\t\tstatic IFeatureInitializer createExpressionFeatureInitializer(EStructuralFeature initFeature, ";
   protected final String TEXT_86 = " valueExpression) {" + NL + "\t\t\tfinal EStructuralFeature feature = initFeature;" + NL + "\t\t\tfinal ";
  protected final String TEXT_87 = " expression = valueExpression;" + NL + "\t\t\treturn new IFeatureInitializer() {\t\t\t\t" + NL + "\t\t\t\tpublic void init(EObject contextInstance) {" + NL + "\t\t\t\t\texpression.assignTo(feature, contextInstance);" + NL + "\t\t\t\t}" + NL + "\t\t\t};\t\t\t" + NL + "\t\t}\t\t";
   protected final String TEXT_88 = NL + "\t\t/** " + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tstatic class ";
   protected final String TEXT_89 = " {";
   protected final String TEXT_90 = NL;
   protected final String TEXT_91 = NL + "/**" + NL + " * @generated" + NL + " */" + NL + "private static ";
   protected final String TEXT_92 = " ";
   protected final String TEXT_93 = "(";
   protected final String TEXT_94 = " self";
   protected final String TEXT_95 = ", ";
   protected final String TEXT_96 = " ";
   protected final String TEXT_97 = ") {" + NL + "\t// TODO: implement this method" + NL + "\t// Ensure that you remove @generated or mark it @generated NOT" + NL + "\t" + NL + "\tthrow new ";
   protected final String TEXT_98 = ".NoImplException(\"No user java implementation provided in '";
   protected final String TEXT_99 = "' operation\"); //$NON-NLS-1$" + NL + "}";
   protected final String TEXT_100 = NL + "\t\t} //";
   protected final String TEXT_101 = NL + "\t} // end of Initializers";
   protected final String TEXT_102 = NL + "}";
   protected final String TEXT_103 = NL;
 
   public String generate(Object argument)
   {
     final StringBuffer stringBuffer = new StringBuffer();
     
 final GenDiagram genDiagram = (GenDiagram) ((Object[]) argument)[0];
 final ImportAssistant importManager = (ImportAssistant) ((Object[]) argument)[1];
 
     stringBuffer.append(TEXT_1);
     
 String copyrightText = genDiagram.getEditorGen().getCopyrightText();
 if (copyrightText != null && copyrightText.trim().length() > 0) {
 
     stringBuffer.append(TEXT_2);
     stringBuffer.append(copyrightText.replaceAll("\n", "\n *"));
     stringBuffer.append(TEXT_3);
     }
     importManager.emitPackageStatement(stringBuffer);
     stringBuffer.append(TEXT_4);
     
 importManager.registerInnerClass("Initializers");
 importManager.registerInnerClass("IObjectInitializer");
 importManager.registerInnerClass("ObjectInitializer");
importManager.registerInnerClass("IFeatureInitializer");
 final String javaExprContainer = "Java";
 importManager.registerInnerClass(javaExprContainer);
 
     stringBuffer.append(TEXT_5);
     importManager.markImportLocation(stringBuffer);
     stringBuffer.append(TEXT_6);
     stringBuffer.append(genDiagram.getElementTypesClassName());
     stringBuffer.append(TEXT_7);
     stringBuffer.append(genDiagram.getElementTypesClassName());
     stringBuffer.append(TEXT_8);
     String pluginClassName = importManager.getImportedName(genDiagram.getEditorGen().getPlugin().getActivatorQualifiedClassName());
     stringBuffer.append(TEXT_9);
     stringBuffer.append(pluginClassName);
     stringBuffer.append(TEXT_10);
     
 GenClass diagramElement = genDiagram.getDomainDiagramElement();
 if (diagramElement != null) {
 	String semanticPackageInterfaceName = importManager.getImportedName(diagramElement.getGenPackage().getQualifiedPackageInterfaceName());
 
     stringBuffer.append(TEXT_11);
     stringBuffer.append(genDiagram.getUniqueIdentifier());
     stringBuffer.append(TEXT_12);
     stringBuffer.append(semanticPackageInterfaceName);
     stringBuffer.append(TEXT_13);
     stringBuffer.append(diagramElement.getClassifierAccessorName());
     stringBuffer.append(TEXT_14);
     
 }
 for (Iterator contents = genDiagram.eAllContents(); contents.hasNext(); ) {
 	Object next = contents.next();
 	String id = null;
 	ModelFacet modelFacet = null;
 	if (next instanceof GenNode) {
 		id = ((GenNode) next).getUniqueIdentifier();
 		modelFacet = ((GenNode) next).getModelFacet();
 	} else if (next instanceof GenLink) {
 		id = ((GenLink) next).getUniqueIdentifier();
 		modelFacet = ((GenLink) next).getModelFacet();
 	}
 	if (modelFacet instanceof TypeModelFacet) {
 		GenClass metaClass = ((TypeModelFacet) modelFacet).getMetaClass();
 		String semanticPackageInterfaceName = importManager.getImportedName(metaClass.getGenPackage().getQualifiedPackageInterfaceName());
 
     stringBuffer.append(TEXT_15);
     stringBuffer.append(id);
     stringBuffer.append(TEXT_16);
     stringBuffer.append(semanticPackageInterfaceName);
     stringBuffer.append(TEXT_17);
     stringBuffer.append(metaClass.getClassifierAccessorName());
     stringBuffer.append(TEXT_18);
     
 	} else if (modelFacet instanceof FeatureLinkModelFacet) {
 		GenFeature metaFeature = ((FeatureLinkModelFacet) modelFacet).getMetaFeature();
 		String semanticPackageInterfaceName = importManager.getImportedName(metaFeature.getGenPackage().getQualifiedPackageInterfaceName());
 
     stringBuffer.append(TEXT_19);
     stringBuffer.append(id);
     stringBuffer.append(TEXT_20);
     stringBuffer.append(semanticPackageInterfaceName);
     stringBuffer.append(TEXT_21);
     stringBuffer.append(metaFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_22);
     
 	}
 }
 
     stringBuffer.append(TEXT_23);
     
 int elementInitializerCount = 0;
 List elements = new LinkedList();
 if (genDiagram.getElementType() != null) {
 	elements.add(genDiagram);
 }
 for (Iterator contents = genDiagram.eAllContents(); contents.hasNext(); ) {
 	Object next = contents.next();
 	GenCommonBase genElement;
 	TypeModelFacet typeModelFacet = null;
 	if (next instanceof GenNode) {
 		genElement = (GenCommonBase) next;
 		typeModelFacet = ((GenNode) next).getModelFacet();
 	} else if (next instanceof GenLink) {
 		genElement = (GenCommonBase) next;
 		ModelFacet modelFacet = ((GenLink) next).getModelFacet();
 		if (modelFacet instanceof TypeLinkModelFacet) {
 			typeModelFacet = (TypeLinkModelFacet) ((GenLink) next).getModelFacet();
 		}
 	} else {
 		continue;
 	}
 	if (typeModelFacet != null && typeModelFacet.getModelElementInitializer() != null) {
 		elementInitializerCount++;
 	}
 	if (genElement.getElementType() != null) {
 		elements.add(genElement);
 	}
 }
 for (Iterator it = elements.iterator(); it.hasNext(); ) {
 	GenCommonBase genElement = (GenCommonBase) it.next();
 
     stringBuffer.append(TEXT_24);
     stringBuffer.append(genElement.getUniqueIdentifier());
     stringBuffer.append(TEXT_25);
     stringBuffer.append(genElement.getElementType().getUniqueIdentifier());
     stringBuffer.append(TEXT_26);
     }
     stringBuffer.append(TEXT_27);
     
 for (Iterator it = elements.iterator(); it.hasNext(); ) {
 	GenCommonBase genElement = (GenCommonBase) it.next();
 
     stringBuffer.append(TEXT_28);
     stringBuffer.append(genElement.getUniqueIdentifier());
     stringBuffer.append(TEXT_29);
     }
     stringBuffer.append(TEXT_30);
     
 if(elementInitializerCount > 0 && genDiagram.getEditorGen().getExpressionProviders() != null) {
 	final GenExpressionProviderContainer expressionProviders = genDiagram.getEditorGen().getExpressionProviders();
 	final LinkedHashSet<GenFeatureValueSpec> javaInitializers = new LinkedHashSet<GenFeatureValueSpec>();
 	final String __javaOperationContainer = javaExprContainer; // place java expression methods here
 	final Map __exprEnvVariables = Collections.EMPTY_MAP;
 
     stringBuffer.append(TEXT_31);
     
 for (Map.Entry<TypeModelFacet, GenCommonBase> next : genDiagram.getTypeModelFacet2GenBaseMap().entrySet()) {
 	TypeModelFacet modelFacet = next.getKey();
 	if (modelFacet.getModelElementInitializer() instanceof GenFeatureSeqInitializer) {
 		GenFeatureSeqInitializer rootFtSeqInitializer = (GenFeatureSeqInitializer)modelFacet.getModelElementInitializer();				
 		javaInitializers.addAll(rootFtSeqInitializer.getJavaExpressionFeatureInitializers(expressionProviders));
 		String id = next.getValue().getUniqueIdentifier();
 		for(GenFeatureSeqInitializer nextFtSeqInitializer : rootFtSeqInitializer.getAllFeatureSeqInitializers()) {
 			boolean isTopLevelObjInit = nextFtSeqInitializer == rootFtSeqInitializer;
 
     stringBuffer.append(TEXT_32);
     			if(isTopLevelObjInit){
     stringBuffer.append(TEXT_33);
     stringBuffer.append(nextFtSeqInitializer.getInitializerFieldName(id));
     stringBuffer.append(TEXT_34);
     stringBuffer.append(nextFtSeqInitializer.getElementClassAccessor(importManager));
     stringBuffer.append(TEXT_35);
     			} else {
     stringBuffer.append(TEXT_36);
     stringBuffer.append(nextFtSeqInitializer.getInitializerFieldName(id));
     stringBuffer.append(TEXT_37);
     stringBuffer.append(nextFtSeqInitializer.getElementClassAccessor(importManager));
     stringBuffer.append(TEXT_38);
     			} 
     stringBuffer.append(TEXT_39);
     			for(GenFeatureInitializer nextFtInitializer : (List<GenFeatureInitializer>)nextFtSeqInitializer.getInitializers()) {
 				String metaFeatureAccessor = nextFtSeqInitializer.getFeatureAccessor(nextFtInitializer, importManager);
 				if(nextFtInitializer instanceof GenReferenceNewElementSpec) {
 					GenReferenceNewElementSpec newElementSpec = (GenReferenceNewElementSpec)nextFtInitializer;
 
     stringBuffer.append(TEXT_40);
     stringBuffer.append(metaFeatureAccessor);
     stringBuffer.append(TEXT_41);
     					for(GenFeatureSeqInitializer newElemInit : (List<GenFeatureSeqInitializer>)newElementSpec.getNewElementInitializers()) { 
     stringBuffer.append(TEXT_42);
     stringBuffer.append(newElemInit.getInitializerFieldName(id));
     stringBuffer.append(TEXT_43);
     					} // end of newElementInitializers 
     stringBuffer.append(TEXT_44);
     				} // end of GenReferenceNewElementSpec
 				else if(nextFtInitializer instanceof GenFeatureValueSpec) {
 					String __outEnvVarName = ""; // no env to setup;
 					GenClassifier __genExprContext = nextFtSeqInitializer.getElementClass();
 					GenFeatureValueSpec __genValueExpression = (GenFeatureValueSpec)nextFtInitializer;
 
     stringBuffer.append(TEXT_45);
     stringBuffer.append(metaFeatureAccessor);
     stringBuffer.append(TEXT_46);
     
 { /*begin the scope*/
 /*
 ValueExpression __genValueExpression;
 GenClassifier __genExprContext
 java.util.Map __exprEnvVariables
 String __outEnvVarName;
 String __javaOperationContainer;
 */
 	org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderBase __genExprProvider = (genDiagram.getEditorGen().getExpressionProviders() != null) ? genDiagram.getEditorGen().getExpressionProviders().getProvider(__genValueExpression) : null;
 	String __ctxEClassifierAccess = importManager.getImportedName(__genExprContext.getGenPackage().getQualifiedPackageInterfaceName()) + ".eINSTANCE.get" + __genExprContext.getClassifierAccessorName()+"()";
 	String __importedAbstractClass = __genExprProvider != null ? importManager.getImportedName(__genExprProvider.getContainer().getAbstractExpressionQualifiedClassName()) : null;
 
 	if(__genExprProvider == null || __importedAbstractClass == null) {
 
     stringBuffer.append(TEXT_47);
     
 	} else if(__genExprProvider instanceof org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter) {
 		String __bodyLiteral = org.eclipse.gmf.internal.common.codegen.Conversions.toStringLiteral(__genValueExpression.getBody());
 		org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter interpreter = (org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter)__genExprProvider;
 		String __expressionAccessor = interpreter.getExpressionAccessor(__genValueExpression);
 		String providerImportedClass = importManager.getImportedName(interpreter.getQualifiedClassName());
 		if(!__exprEnvVariables.isEmpty()) {			
 
     stringBuffer.append(providerImportedClass);
     stringBuffer.append(TEXT_48);
     stringBuffer.append(__expressionAccessor);
     stringBuffer.append(TEXT_49);
     stringBuffer.append(__bodyLiteral);
     stringBuffer.append(TEXT_50);
     stringBuffer.append(__ctxEClassifierAccess);
     stringBuffer.append(TEXT_51);
     stringBuffer.append(__outEnvVarName);
     stringBuffer.append(TEXT_52);
     
 		} else { 		
 
     stringBuffer.append(providerImportedClass);
     stringBuffer.append(TEXT_53);
     stringBuffer.append(__expressionAccessor);
     stringBuffer.append(TEXT_54);
     stringBuffer.append(__bodyLiteral);
     stringBuffer.append(TEXT_55);
     stringBuffer.append(__ctxEClassifierAccess);
     stringBuffer.append(TEXT_56);
     
 		}
 	} else if(__genExprProvider instanceof org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider) { /*inlined java expression adapter*/
 		String evalCtxQualifiedName = __genExprProvider.getQualifiedInstanceClassName(__genExprContext);
 		String __exprJavaOperName = ((org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider)__genExprProvider).getOperationName(__genValueExpression);	
 
 
     stringBuffer.append(TEXT_57);
     stringBuffer.append(__importedAbstractClass);
     stringBuffer.append(TEXT_58);
     stringBuffer.append(__ctxEClassifierAccess);
     stringBuffer.append(TEXT_59);
     stringBuffer.append(importManager.getImportedName("java.util.Map"));
     stringBuffer.append(TEXT_60);
     stringBuffer.append(importManager.getImportedName(evalCtxQualifiedName));
     stringBuffer.append(TEXT_61);
     stringBuffer.append(importManager.getImportedName(evalCtxQualifiedName));
     stringBuffer.append(TEXT_62);
     	
 		for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();) {
 			String nextVariableName = (String)envVarIt.next();
 			org.eclipse.emf.codegen.ecore.genmodel.GenClassifier nextVariableType = (org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__exprEnvVariables.get(nextVariableName);
 			String qualifiedTypeName = __genExprProvider.getQualifiedInstanceClassName(nextVariableType);
 
     stringBuffer.append(TEXT_63);
     stringBuffer.append(importManager.getImportedName(qualifiedTypeName));
     stringBuffer.append(TEXT_64);
     stringBuffer.append(nextVariableName);
     stringBuffer.append(TEXT_65);
     stringBuffer.append(importManager.getImportedName(qualifiedTypeName));
     stringBuffer.append(TEXT_66);
     stringBuffer.append(nextVariableName);
     stringBuffer.append(TEXT_67);
     		} 
     stringBuffer.append(TEXT_68);
     
 		if(__javaOperationContainer != null && __javaOperationContainer.length() > 0) { 
     stringBuffer.append(__javaOperationContainer);
     stringBuffer.append(TEXT_69);
     
 		} 
     stringBuffer.append(__exprJavaOperName);
     stringBuffer.append(TEXT_70);
     
 		for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();){
     stringBuffer.append(TEXT_71);
     stringBuffer.append((String)envVarIt.next());
     }
     stringBuffer.append(TEXT_72);
     }
     } /*end of scope*/
     stringBuffer.append(TEXT_73);
     
 				} // end of GenFeatureValueSpec
 			} // end of GenFeatureInitializer iteration
 
     stringBuffer.append(TEXT_74);
     			if(!isTopLevelObjInit) { 
     stringBuffer.append(TEXT_75);
     stringBuffer.append(nextFtSeqInitializer.getInitializerFieldName(id));
     stringBuffer.append(TEXT_76);
     			} // GenFeatureInitializer iteration
 		} // All GenFeatureSeqInitializer iteration
 
     stringBuffer.append(TEXT_77);
     stringBuffer.append(rootFtSeqInitializer.getInitializerFieldName(id));
     stringBuffer.append(TEXT_78);
     
 	} // (modelFacet.getModelElementInitializer() instanceof GenFeatureSeqInitializer)
 }
 
     stringBuffer.append(TEXT_79);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_80);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_81);
     stringBuffer.append(importManager.getImportedName("java.util.ArrayList"));
     stringBuffer.append(TEXT_82);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_83);
     stringBuffer.append(pluginClassName);
     stringBuffer.append(TEXT_84);
     stringBuffer.append(importManager.getImportedName("java.util.Collection"));
     stringBuffer.append(TEXT_85);
     stringBuffer.append(importManager.getImportedName(expressionProviders.getAbstractExpressionQualifiedClassName()));
     stringBuffer.append(TEXT_86);
     stringBuffer.append(importManager.getImportedName(expressionProviders.getAbstractExpressionQualifiedClassName()));
     stringBuffer.append(TEXT_87);
     
 	if(!javaInitializers.isEmpty()) {
 
     stringBuffer.append(TEXT_88);
     stringBuffer.append(javaExprContainer);
     stringBuffer.append(TEXT_89);
     
 		for(GenFeatureValueSpec __genValueExpression : javaInitializers) {
 			GenClassifier __genExprContext = __genValueExpression.getFeatureSeqInitializer().getElementClass();
 			GenExpressionProviderBase provider = expressionProviders.getProvider(__genValueExpression);
 			if(!(provider instanceof GenJavaExpressionProvider)) continue;
 			String __genExprResultType = provider.getQualifiedInstanceClassName(__genValueExpression.getFeature());
 
     stringBuffer.append(TEXT_90);
     
 /* 
 ValueExpression __genValueExpression
 java.util.Map __exprEnvVariables
 GenClassifier __genExprContext
 GenClassifier || String/qualifiedClassName/__genExprResultType
 */
 org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderBase __genExprProvider = (genDiagram.getEditorGen().getExpressionProviders() != null) ? genDiagram.getEditorGen().getExpressionProviders().getProvider(__genValueExpression) : null;
 if(__genExprProvider instanceof org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider) {
 	String __importedAbstractExpr = importManager.getImportedName(__genExprProvider.getContainer().getAbstractExpressionQualifiedClassName());
 	String evalCtxQualifiedName = __genExprProvider.getQualifiedInstanceClassName(__genExprContext);
 	// support GenClassifier and also String based qualified java class name
 	Object __genExprResultTypeObj = __genExprResultType;
 	String __exprResultTypeQualifiedName = null;
 	if(__genExprResultTypeObj instanceof String) 
 		__exprResultTypeQualifiedName = (String)__genExprResultTypeObj;
 	else if(__genExprResultTypeObj instanceof org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)
 		__exprResultTypeQualifiedName = __genExprProvider.getQualifiedInstanceClassName((org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__genExprResultTypeObj);
 	String __exprJavaOperName = ((org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider)__genExprProvider).getOperationName(__genValueExpression);
 
     stringBuffer.append(TEXT_91);
     stringBuffer.append(importManager.getImportedName(__exprResultTypeQualifiedName));
     stringBuffer.append(TEXT_92);
     stringBuffer.append(__exprJavaOperName);
     stringBuffer.append(TEXT_93);
     stringBuffer.append(importManager.getImportedName(evalCtxQualifiedName));
     stringBuffer.append(TEXT_94);
     
 	for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();) {
 		String __nextVarName = (String)envVarIt.next();
 		org.eclipse.emf.codegen.ecore.genmodel.GenClassifier nextVariableType = (org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__exprEnvVariables.get(__nextVarName);
 		String qualifiedTypeName = __genExprProvider.getQualifiedInstanceClassName(nextVariableType);
 
 	
     stringBuffer.append(TEXT_95);
     stringBuffer.append(importManager.getImportedName(qualifiedTypeName));
     stringBuffer.append(TEXT_96);
     stringBuffer.append(__nextVarName);
     	} 
 
     stringBuffer.append(TEXT_97);
     stringBuffer.append(__importedAbstractExpr);
     stringBuffer.append(TEXT_98);
     stringBuffer.append(__exprJavaOperName);
     stringBuffer.append(TEXT_99);
     
 } /* end of GenJavaExpressionProvider */
 
     
 		}
 
     stringBuffer.append(TEXT_100);
     stringBuffer.append(javaExprContainer);
     	} /* end of javaInitializers */ 
     stringBuffer.append(TEXT_101);
     }
     stringBuffer.append(TEXT_102);
     importManager.emitSortedImports();
     stringBuffer.append(TEXT_103);
     return stringBuffer.toString();
   }
 }
