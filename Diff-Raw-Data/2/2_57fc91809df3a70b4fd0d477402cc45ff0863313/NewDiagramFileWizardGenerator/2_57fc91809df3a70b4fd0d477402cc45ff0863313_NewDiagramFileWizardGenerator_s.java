 package org.eclipse.gmf.codegen.templates.editor;
 
 import org.eclipse.gmf.codegen.gmfgen.*;
 import org.eclipse.emf.codegen.ecore.genmodel.*;
 import java.util.*;
 import org.eclipse.gmf.common.codegen.*;
 
 public class NewDiagramFileWizardGenerator {
  
   protected static String nl;
   public static synchronized NewDiagramFileWizardGenerator create(String lineSeparator)
   {
     nl = lineSeparator;
     NewDiagramFileWizardGenerator result = new NewDiagramFileWizardGenerator();
     nl = null;
     return result;
   }
 
   protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "";
   protected final String TEXT_2 = NL + "/*" + NL + " *";
   protected final String TEXT_3 = NL + " */";
   protected final String TEXT_4 = NL + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "public class ";
   protected final String TEXT_5 = " extends Wizard {" + NL + "" + NL + "\t/**" + NL + "     * @generated" + NL + "\t */" + NL + "\tprivate TransactionalEditingDomain myEditingDomain;" + NL + "\t\t" + NL + "\t/**" + NL + "     * @generated" + NL + "\t */" + NL + "\tprivate WizardNewFileCreationPage myFileCreationPage;" + NL + "\t\t" + NL + "\t/**" + NL + "     * @generated" + NL + "\t */" + NL + "\tprivate IFile mySelectedModelFile;" + NL + "\t\t" + NL + "\t/**" + NL + "     * @generated" + NL + "\t */" + NL + "\tprivate IWorkbenchPage myWorkbenchPage;" + NL + "\t\t" + NL + "\t/**" + NL + "     * @generated" + NL + "\t */" + NL + "\tprivate IStructuredSelection mySelection;" + NL + "\t" + NL + "\t/**" + NL + "     * @generated" + NL + "\t */" + NL + "\tprivate EObject myDiagramRoot;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic ";
   protected final String TEXT_6 = "(IFile selectedModelFile, IWorkbenchPage workbenchPage, IStructuredSelection selection, EObject diagramRoot, TransactionalEditingDomain editingDomain) {" + NL + "\t\tassert selectedModelFile != null : \"Null selectedModelFile in ";
   protected final String TEXT_7 = " constructor\"; //$NON-NLS-1$" + NL + "\t\tassert workbenchPage != null : \"Null workbenchPage in ";
   protected final String TEXT_8 = " constructor\"; //$NON-NLS-1$" + NL + "\t\tassert selection != null : \"Null selection in ";
   protected final String TEXT_9 = " constructor\"; //$NON-NLS-1$" + NL + "\t    assert diagramRoot != null : \"Null diagramRoot in ";
   protected final String TEXT_10 = " constructor\"; //$NON-NLS-1$" + NL + "\t    assert editingDomain != null : \"Null editingDomain in ";
   protected final String TEXT_11 = " constructor\"; //$NON-NLS-1$" + NL + "\t    " + NL + "\t   \tmySelectedModelFile = selectedModelFile;" + NL + "\t   \tmyWorkbenchPage = workbenchPage;" + NL + "\t   \tmySelection = selection;" + NL + "\t   \tmyDiagramRoot = diagramRoot;" + NL + "\t   \tmyEditingDomain = editingDomain;" + NL + "\t}" + NL + "\t     \t\t" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic void addPages() {" + NL + "\t\tmyFileCreationPage = new WizardNewFileCreationPage(\"Initialize new Ecore diagram file\", mySelection) {" + NL + "" + NL + "\t\t\tpublic void createControl(Composite parent) {" + NL + "\t\t\t\tsuper.createControl(parent);" + NL + "\t\t\t\t" + NL + "\t\t\t\tIContainer parentContainer = mySelectedModelFile.getParent();" + NL + "\t\t\t\tString originalFileName = mySelectedModelFile.getProjectRelativePath().removeFileExtension().lastSegment();" + NL + "\t\t\t\tString fileExtension = \".";
   protected final String TEXT_12 = "\"; //$NON-NLS-1$" + NL + "\t\t\t\tString fileName = originalFileName + fileExtension;" + NL + "\t\t\t\tfor (int i = 1; parentContainer.getFile(new Path(fileName)).exists(); i++) {" + NL + "\t\t\t\t\tfileName = originalFileName + i + fileExtension;" + NL + "\t\t\t\t}" + NL + "\t\t\t\tsetFileName(fileName);" + NL + "\t\t\t}" + NL + "\t\t" + NL + "\t\t};" + NL + "\t\tmyFileCreationPage.setTitle(\"Diagram file\");" + NL + "\t\tmyFileCreationPage.setDescription(\"Create new diagram based on \" + ";
   protected final String TEXT_13 = ".MODEL_ID + \" model content\");" + NL + "\t\taddPage(myFileCreationPage);" + NL + "\t\taddPage(new RootElementSelectorPage());" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic boolean performFinish() {" + NL + "\t\tIFile diagramFile = myFileCreationPage.createNewFile();" + NL + "\t\ttry {" + NL + "\t\t\tdiagramFile.setCharset(\"UTF-8\", new NullProgressMonitor()); //$NON-NLS-1$" + NL + "\t\t} catch (";
   protected final String TEXT_14 = " e) {" + NL + "\t\t\t";
   protected final String TEXT_15 = ".getInstance().logError(\"Unable to set charset for diagram file\", e); //$NON-NLS-1$" + NL + "\t\t}" + NL + "\t\t" + NL + "\t\tResourceSet resourceSet = myEditingDomain.getResourceSet();" + NL + "\t\tfinal Resource diagramResource = resourceSet.createResource(URI.createPlatformResourceURI(diagramFile.getFullPath().toString()));" + NL + "" + NL + "\t\tList affectedFiles = new LinkedList();" + NL + "\t\taffectedFiles.add(mySelectedModelFile);" + NL + "\t\taffectedFiles.add(diagramFile);" + NL + "\t\t\t" + NL + "\t\tAbstractTransactionalCommand command = new AbstractTransactionalCommand(myEditingDomain, \"Initializing diagram contents\", affectedFiles) { //$NON-NLS-1$" + NL + "\t\t\tprotected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {" + NL + "\t\t\t\tint diagramVID = ";
   protected final String TEXT_16 = ".getDiagramVisualID(myDiagramRoot);" + NL + "\t\t\t\tif (diagramVID != ";
   protected final String TEXT_17 = ".VISUAL_ID) {" + NL + "\t\t\t\t\treturn CommandResult.newErrorCommandResult(\"Incorrect model object stored as a root resource object\"); //$NON-NLS-1$" + NL + "\t\t\t\t}" + NL + "\t\t\t\tDiagram diagram = ViewService.createDiagram(myDiagramRoot, ";
   protected final String TEXT_18 = ".MODEL_ID, ";
   protected final String TEXT_19 = ".DIAGRAM_PREFERENCES_HINT);" + NL + "\t\t\t\tdiagramResource.getContents().add(diagram);";
   protected final String TEXT_20 = NL + "\t\t\t\tdiagramResource.getContents().add(diagram.getElement());";
  protected final String TEXT_21 = NL + "\t\t\t\tinitDiagramContents(diagram, diagramModelObject);";
   protected final String TEXT_22 = NL + "\t\t\t\treturn CommandResult.newOKCommandResult();" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t\t\t" + NL + "\t\ttry {" + NL + "\t\t\tOperationHistoryFactory.getOperationHistory().execute(command, new NullProgressMonitor(), null);" + NL + "\t\t\tdiagramResource.save(Collections.EMPTY_MAP);" + NL + "\t\t\tIDE.openEditor(myWorkbenchPage, diagramFile);" + NL + "\t\t} catch (ExecutionException e) {" + NL + "\t\t\t";
   protected final String TEXT_23 = ".getInstance().logError(\"Unable to create model and diagram\", e); //$NON-NLS-1$" + NL + "\t\t} catch (IOException ex) {" + NL + "\t\t\t";
   protected final String TEXT_24 = ".getInstance().logError(\"Save operation failed for: \" + diagramFile.getFullPath().toString(), ex); //$NON-NLS-1$" + NL + "\t\t} catch (PartInitException ex) {" + NL + "\t\t\t";
   protected final String TEXT_25 = ".getInstance().logError(\"Unable to open editor\", ex); //$NON-NLS-1$" + NL + "\t\t}\t\t\t" + NL + "\t\treturn true;" + NL + "\t}" + NL + "\t\t";
   protected final String TEXT_26 = NL;
   protected final String TEXT_27 = NL;
   protected final String TEXT_28 = NL + "\t" + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "private ";
   protected final String TEXT_29 = " myLinkDescriptors = new ";
   protected final String TEXT_30 = "();" + NL + "\t" + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "private ";
   protected final String TEXT_31 = " myEObject2NodeMap = new ";
   protected final String TEXT_32 = "();" + NL + "" + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "private void initDiagramContents(";
   protected final String TEXT_33 = " diagram, ";
   protected final String TEXT_34 = " diagramModelObject) {" + NL + "\tcreate";
   protected final String TEXT_35 = "Children(diagram, diagramModelObject);";
   protected final String TEXT_36 = NL;
   protected final String TEXT_37 = " resource = diagramModelObject.eResource();" + NL + "for (";
   protected final String TEXT_38 = " it = resource.getContents().iterator(); it.hasNext();) {" + NL + "\t";
   protected final String TEXT_39 = " nextResourceObject = (";
   protected final String TEXT_40 = ") it.next();" + NL + "\tif (nextResourceObject == diagramModelObject) {" + NL + "\t\tcontinue;" + NL + "\t}" + NL + "\tint nodeVID = ";
   protected final String TEXT_41 = ".getNodeVisualID(diagram, nextResourceObject);" + NL + "\tswitch (nodeVID) {";
   protected final String TEXT_42 = NL + "\tcase ";
   protected final String TEXT_43 = ".VISUAL_ID: {" + NL + "\t\t";
   protected final String TEXT_44 = " nextNode = ViewService.createNode(diagram, nextResourceObject, ";
   protected final String TEXT_45 = ".getType(";
   protected final String TEXT_46 = ".VISUAL_ID), ";
   protected final String TEXT_47 = ".DIAGRAM_PREFERENCES_HINT);" + NL + "\t\tmyEObject2NodeMap.put(nextResourceObject, nextNode);" + NL + "\t\tcreate";
   protected final String TEXT_48 = "Children(nextNode, nextResourceObject);";
   protected final String TEXT_49 = NL;
   protected final String TEXT_50 = "\t\tbreak;" + NL + "\t}";
   protected final String TEXT_51 = "\t}" + NL + "}";
   protected final String TEXT_52 = "\t\t" + NL + "\tcreateLinks(diagram);" + NL + "}";
   protected final String TEXT_53 = "\t" + NL + "" + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "private void create";
   protected final String TEXT_54 = "Children(";
   protected final String TEXT_55 = " viewObject, ";
   protected final String TEXT_56 = " modelObject) {";
   protected final String TEXT_57 = NL;
   protected final String TEXT_58 = "\t";
   protected final String TEXT_59 = " nextValue;" + NL + "\tint nodeVID;";
   protected final String TEXT_60 = "\tfor (";
   protected final String TEXT_61 = " values = ";
   protected final String TEXT_62 = ".iterator(); values.hasNext();) {" + NL + "\t\tnextValue = (";
   protected final String TEXT_63 = ") values.next();";
   protected final String TEXT_64 = NL + "\tnextValue = ";
   protected final String TEXT_65 = ";";
   protected final String TEXT_66 = NL + "\tnodeVID = ";
   protected final String TEXT_67 = ".getNodeVisualID(viewObject, nextValue);";
   protected final String TEXT_68 = NL + "\tswitch (nodeVID) {";
   protected final String TEXT_69 = NL + "\tcase ";
   protected final String TEXT_70 = ".VISUAL_ID: {";
   protected final String TEXT_71 = NL + "\tif (";
   protected final String TEXT_72 = ".VISUAL_ID == nodeVID) {";
   protected final String TEXT_73 = NL + "\t\t";
   protected final String TEXT_74 = " nextNode = ViewService.createNode(viewObject, nextValue, ";
   protected final String TEXT_75 = ".getType(";
   protected final String TEXT_76 = ".VISUAL_ID), ";
   protected final String TEXT_77 = ".DIAGRAM_PREFERENCES_HINT);" + NL + "\t\tmyEObject2NodeMap.put(nextValue, nextNode);" + NL + "\t\tcreate";
   protected final String TEXT_78 = "Children(nextNode, nextValue);";
   protected final String TEXT_79 = NL;
   protected final String TEXT_80 = "\t\tbreak;";
   protected final String TEXT_81 = "\t}";
   protected final String TEXT_82 = NL + "\t}";
   protected final String TEXT_83 = NL + "\t}";
   protected final String TEXT_84 = NL + "\t";
   protected final String TEXT_85 = " nextCompartment = getCompartment(viewObject, ";
   protected final String TEXT_86 = ".getType(";
   protected final String TEXT_87 = ".VISUAL_ID));" + NL + "\tif (nextCompartment != null) {" + NL + "\t\tcreate";
   protected final String TEXT_88 = "Children(nextCompartment, modelObject);" + NL + "\t}";
   protected final String TEXT_89 = NL + "\tstoreLinks(modelObject, viewObject.getDiagram());";
   protected final String TEXT_90 = NL + "}";
   protected final String TEXT_91 = NL + "\t" + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "private ";
   protected final String TEXT_92 = " getCompartment(";
   protected final String TEXT_93 = " node, String name) {" + NL + "\tfor (";
   protected final String TEXT_94 = " it = node.getChildren().iterator(); it.hasNext();) {" + NL + "\t\t";
   protected final String TEXT_95 = " nextView = (";
   protected final String TEXT_96 = ") it.next();" + NL + "\t\tif (nextView instanceof ";
   protected final String TEXT_97 = " && name.equals(nextView.getType())) {" + NL + "\t\t\treturn (";
   protected final String TEXT_98 = ") nextView;" + NL + "\t\t}" + NL + "\t}" + NL + "\treturn null;" + NL + "}";
   protected final String TEXT_99 = NL;
   protected final String TEXT_100 = NL;
   protected final String TEXT_101 = "/**" + NL + " *@generated" + NL + " */" + NL + "private void storeLinks(";
   protected final String TEXT_102 = " container, ";
   protected final String TEXT_103 = " diagram) {" + NL + "\t";
   protected final String TEXT_104 = " containerMetaclass = container.eClass();" + NL + "\tstoreFeatureModelFacetLinks(container, containerMetaclass, diagram);" + NL + "\tstoreTypeModelFacetLinks(container, containerMetaclass);" + NL + "}" + NL + "\t" + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "private void storeTypeModelFacetLinks(";
   protected final String TEXT_105 = " container, ";
   protected final String TEXT_106 = " containerMetaclass) {\t";
   protected final String TEXT_107 = "\t\t" + NL + "\tif (";
   protected final String TEXT_108 = ".eINSTANCE.get";
   protected final String TEXT_109 = "().isSuperTypeOf(containerMetaclass)) {";
   protected final String TEXT_110 = "\t\t" + NL + "\t\tfor (";
   protected final String TEXT_111 = " values = ";
   protected final String TEXT_112 = ".iterator(); values.hasNext();) {" + NL + "\t\t\t";
   protected final String TEXT_113 = " nextValue = ((";
   protected final String TEXT_114 = ") values.next());";
   protected final String TEXT_115 = NL + "\t\t";
   protected final String TEXT_116 = " nextValue = (";
   protected final String TEXT_117 = ") ";
   protected final String TEXT_118 = ";";
   protected final String TEXT_119 = NL + "\t\tint linkVID = ";
   protected final String TEXT_120 = ".getLinkWithClassVisualID(nextValue);" + NL + "\t\tif (";
   protected final String TEXT_121 = ".VISUAL_ID == linkVID) {" + NL + "\t\t\tObject structuralFeatureResult = ";
   protected final String TEXT_122 = ";" + NL + "\t\t\tif (structuralFeatureResult instanceof ";
   protected final String TEXT_123 = ") {" + NL + "\t\t\t\t";
   protected final String TEXT_124 = " dst = (";
   protected final String TEXT_125 = ") structuralFeatureResult;";
   protected final String TEXT_126 = NL + "\t\t\t\tstructuralFeatureResult = ";
   protected final String TEXT_127 = ";" + NL + "\t\t\t\tif (structuralFeatureResult instanceof ";
   protected final String TEXT_128 = ") {" + NL + "\t\t\t\t\t";
   protected final String TEXT_129 = " src = (";
   protected final String TEXT_130 = ") structuralFeatureResult;";
   protected final String TEXT_131 = NL + "\t\t\t\t";
   protected final String TEXT_132 = " src = container;";
   protected final String TEXT_133 = NL + "\t\t\t\tmyLinkDescriptors.add(new LinkDescriptor(src, dst, nextValue, linkVID));";
   protected final String TEXT_134 = NL + "\t\t\t\t}";
   protected final String TEXT_135 = NL + "\t\t\t}" + NL + "\t\t}";
   protected final String TEXT_136 = NL + "\t\t}";
   protected final String TEXT_137 = NL + "\t}";
   protected final String TEXT_138 = NL + "}" + NL + "" + NL + "/**" + NL + " *@generated" + NL + " */" + NL + "private void storeFeatureModelFacetLinks(";
   protected final String TEXT_139 = " container, ";
   protected final String TEXT_140 = " containerMetaclass, ";
   protected final String TEXT_141 = " diagram) {";
   protected final String TEXT_142 = NL;
   protected final String TEXT_143 = "\tif (";
   protected final String TEXT_144 = ".eINSTANCE.get";
   protected final String TEXT_145 = "().isSuperTypeOf(containerMetaclass)) {";
   protected final String TEXT_146 = NL + "\t\tfor (";
   protected final String TEXT_147 = " destinations = ";
   protected final String TEXT_148 = ".iterator(); destinations.hasNext();) {" + NL + "\t\t\t";
   protected final String TEXT_149 = " nextDestination = (";
   protected final String TEXT_150 = ") destinations.next();";
   protected final String TEXT_151 = NL + "\t\t";
   protected final String TEXT_152 = " nextDestination = (";
   protected final String TEXT_153 = ") ";
   protected final String TEXT_154 = ";";
   protected final String TEXT_155 = NL + "\t\tif (";
   protected final String TEXT_156 = ".VISUAL_ID == ";
   protected final String TEXT_157 = ".getNodeVisualID(diagram, nextDestination)) {";
   protected final String TEXT_158 = NL + "\t\tmyLinkDescriptors.add(new LinkDescriptor(container, nextDestination, ";
   protected final String TEXT_159 = ".";
   protected final String TEXT_160 = ", ";
   protected final String TEXT_161 = ".VISUAL_ID));";
   protected final String TEXT_162 = NL + "\t\t";
   protected final String TEXT_163 = " nextNode = ViewService.createNode(diagram, nextDestination, ";
   protected final String TEXT_164 = ".getType(";
   protected final String TEXT_165 = ".VISUAL_ID), ";
   protected final String TEXT_166 = ".DIAGRAM_PREFERENCES_HINT);" + NL + "\t\tmyEObject2NodeMap.put(nextDestination, nextNode);" + NL + "\t\tcreate";
   protected final String TEXT_167 = "Children(nextNode, nextDestination);";
   protected final String TEXT_168 = NL;
   protected final String TEXT_169 = NL + "\t\t}";
   protected final String TEXT_170 = NL + "\t\t}";
   protected final String TEXT_171 = NL + "\t}";
   protected final String TEXT_172 = NL;
   protected final String TEXT_173 = "\tif (";
   protected final String TEXT_174 = ".eINSTANCE.get";
   protected final String TEXT_175 = "().isSuperTypeOf(containerMetaclass)) {";
   protected final String TEXT_176 = NL + "\t\tfor (";
   protected final String TEXT_177 = " destinations = ";
   protected final String TEXT_178 = ".iterator(); destinations.hasNext();) {" + NL + "\t\t\t";
   protected final String TEXT_179 = " nextDestination = (";
   protected final String TEXT_180 = ") destinations.next();";
   protected final String TEXT_181 = NL + "\t\t";
   protected final String TEXT_182 = " nextDestination = (";
   protected final String TEXT_183 = ") ";
   protected final String TEXT_184 = ";";
   protected final String TEXT_185 = NL + "\t\tif (";
   protected final String TEXT_186 = ".VISUAL_ID == ";
   protected final String TEXT_187 = ".getNodeVisualID(diagram, nextDestination)) {";
   protected final String TEXT_188 = NL + "\t\tmyLinkDescriptors.add(new LinkDescriptor(container, nextDestination, ";
   protected final String TEXT_189 = ".";
   protected final String TEXT_190 = ", ";
   protected final String TEXT_191 = ".VISUAL_ID));";
   protected final String TEXT_192 = NL;
   protected final String TEXT_193 = NL + "\t\t}";
   protected final String TEXT_194 = NL + "\t\t}";
   protected final String TEXT_195 = NL + "\t}";
   protected final String TEXT_196 = NL + "}" + NL + "" + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "private void createLinks(";
   protected final String TEXT_197 = " diagram) {" + NL + "\tfor (";
   protected final String TEXT_198 = " it = myLinkDescriptors.iterator(); it.hasNext();) {" + NL + "\t\tLinkDescriptor nextLinkDescriptor = (LinkDescriptor) it.next();" + NL + "\t\t";
   protected final String TEXT_199 = " edge = (";
   protected final String TEXT_200 = ") ViewService.getInstance().createEdge(nextLinkDescriptor.getSemanticAdapter(), diagram, String.valueOf(nextLinkDescriptor.getVisualID()), ";
   protected final String TEXT_201 = ".APPEND, ";
   protected final String TEXT_202 = ".DIAGRAM_PREFERENCES_HINT);" + NL + "\t\tif (edge != null) {" + NL + "\t\t\tedge.setSource((";
   protected final String TEXT_203 = ") myEObject2NodeMap.get(nextLinkDescriptor.getSource()));" + NL + "\t\t\tedge.setTarget((";
   protected final String TEXT_204 = ") myEObject2NodeMap.get(nextLinkDescriptor.getDestination()));" + NL + "\t\t}" + NL + "\t}" + NL + "}" + NL;
   protected final String TEXT_205 = NL;
   protected final String TEXT_206 = "/**" + NL + " * @generated" + NL + " */" + NL + "private class LinkDescriptor {" + NL + "\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprivate ";
   protected final String TEXT_207 = " mySource;" + NL + "\t\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprivate ";
   protected final String TEXT_208 = " myDestination;" + NL + "\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprivate ";
   protected final String TEXT_209 = " myLinkElement;" + NL + "\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprivate int myVisualID;" + NL + "\t\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprivate ";
   protected final String TEXT_210 = " mySemanticAdapter;" + NL + "\t\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprotected LinkDescriptor(";
   protected final String TEXT_211 = " source, ";
   protected final String TEXT_212 = " destination, ";
   protected final String TEXT_213 = " linkElement, int linkVID) {" + NL + "\t\tthis(source, destination, linkVID);" + NL + "\t\tmyLinkElement = linkElement;" + NL + "\t\tmySemanticAdapter = new ";
   protected final String TEXT_214 = "(linkElement);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprotected LinkDescriptor(";
   protected final String TEXT_215 = " source, ";
   protected final String TEXT_216 = " destination, ";
   protected final String TEXT_217 = " elementType, int linkVID) {" + NL + "\t\tthis(source, destination, linkVID);" + NL + "\t\tmyLinkElement = null;" + NL + "\t\tfinal ";
   protected final String TEXT_218 = " elementTypeCopy = elementType;" + NL + "\t\tmySemanticAdapter = new ";
   protected final String TEXT_219 = "() {" + NL + "\t\t\tpublic Object getAdapter(Class adapter) {" + NL + "\t\t\t\tif (";
   protected final String TEXT_220 = ".class.equals(adapter)) {" + NL + "\t\t\t\t\treturn elementTypeCopy;" + NL + "\t\t\t\t}" + NL + "\t\t\t\treturn null;" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t}" + NL + "\t\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprivate LinkDescriptor(";
   protected final String TEXT_221 = " source, ";
   protected final String TEXT_222 = " destination, int linkVID) {" + NL + "\t\tmySource = source;" + NL + "\t\tmyDestination = destination;" + NL + "\t\tmyVisualID = linkVID;" + NL + "\t}" + NL + "\t\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprotected ";
   protected final String TEXT_223 = " getSource() {" + NL + "\t\treturn mySource;" + NL + "\t}" + NL + "\t\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprotected ";
   protected final String TEXT_224 = " getDestination() {" + NL + "\t\treturn myDestination;" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprotected ";
   protected final String TEXT_225 = " getLinkElement() {" + NL + "\t\treturn myLinkElement;" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprotected int getVisualID() {" + NL + "\t\treturn myVisualID;" + NL + "\t}" + NL + "\t\t" + NL + "\t/**" + NL + "   \t * @generated" + NL + "     */" + NL + "\tprotected ";
   protected final String TEXT_226 = " getSemanticAdapter() {" + NL + "\t\treturn mySemanticAdapter;" + NL + "\t}" + NL + "}";
   protected final String TEXT_227 = "\t" + NL + "" + NL + "\t/**" + NL + "     * @generated" + NL + "\t */" + NL + "\tprivate class RootElementSelectorPage extends ";
   protected final String TEXT_228 = " implements ";
   protected final String TEXT_229 = " {" + NL + "\t" + NL + "\t\t/**" + NL + "    \t * @generated" + NL + "\t\t */" + NL + "\t\tprotected RootElementSelectorPage() {" + NL + "\t\t\tsuper(\"Select diagram root element\");" + NL + "\t\t\tsetTitle(\"Diagram root element\");" + NL + "\t\t\tsetDescription(\"Select semantic model element to be depicted on diagram\");" + NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "    \t * @generated" + NL + "\t\t */\t\t" + NL + "\t\tpublic void createControl(";
   protected final String TEXT_230 = " parent) {" + NL + "\t\t\tinitializeDialogUnits(parent);" + NL + "\t\t\t";
   protected final String TEXT_231 = " topLevel = new ";
   protected final String TEXT_232 = "(parent, ";
   protected final String TEXT_233 = ".NONE);" + NL + "\t\t\ttopLevel.setLayout(new ";
   protected final String TEXT_234 = "());" + NL + "\t\t\ttopLevel.setLayoutData(new ";
   protected final String TEXT_235 = "(";
   protected final String TEXT_236 = ".VERTICAL_ALIGN_FILL | ";
   protected final String TEXT_237 = ".HORIZONTAL_ALIGN_FILL));" + NL + "\t\t\ttopLevel.setFont(parent.getFont());" + NL + "\t\t\tsetControl(topLevel);" + NL + "\t\t\tcreateModelBrowser(topLevel);" + NL + "\t\t\tsetPageComplete(validatePage());" + NL + "\t\t}" + NL + "\t\t" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprivate void createModelBrowser(";
   protected final String TEXT_238 = " parent) {" + NL + "\t\t\t";
   protected final String TEXT_239 = " panel = new ";
   protected final String TEXT_240 = "(parent, ";
   protected final String TEXT_241 = ".NONE);" + NL + "\t\t\tpanel.setLayoutData(new ";
   protected final String TEXT_242 = "(";
   protected final String TEXT_243 = ".FILL_BOTH));" + NL + "\t\t\t";
   protected final String TEXT_244 = " layout = new ";
   protected final String TEXT_245 = "();" + NL + "\t\t\tlayout.marginWidth = 0;" + NL + "\t\t\tpanel.setLayout(layout);" + NL + "\t\t\t" + NL + "\t\t\t";
   protected final String TEXT_246 = " label = new ";
   protected final String TEXT_247 = "(panel, ";
   protected final String TEXT_248 = ".NONE);" + NL + "\t\t\tlabel.setText(\"Select diagram root element:\");" + NL + "\t\t\tlabel.setLayoutData(new ";
   protected final String TEXT_249 = "(";
   protected final String TEXT_250 = ".HORIZONTAL_ALIGN_BEGINNING));" + NL + "\t\t" + NL + "\t\t\t";
   protected final String TEXT_251 = " treeViewer = new ";
   protected final String TEXT_252 = "(panel, ";
   protected final String TEXT_253 = ".SINGLE | ";
   protected final String TEXT_254 = ".H_SCROLL | ";
   protected final String TEXT_255 = ".V_SCROLL | ";
   protected final String TEXT_256 = ".BORDER);" + NL + "\t\t\t";
   protected final String TEXT_257 = " layoutData = new ";
   protected final String TEXT_258 = "(";
   protected final String TEXT_259 = ".FILL_BOTH);" + NL + "\t\t\tlayoutData.heightHint = 300;" + NL + "\t\t\tlayoutData.widthHint = 300;" + NL + "\t\t\ttreeViewer.getTree().setLayoutData(layoutData);" + NL + "\t\t\ttreeViewer.setContentProvider(new ";
   protected final String TEXT_260 = "(";
   protected final String TEXT_261 = ".getInstance().getItemProvidersAdapterFactory()));" + NL + "\t\t\ttreeViewer.setLabelProvider(new ";
   protected final String TEXT_262 = "(";
   protected final String TEXT_263 = ".getInstance().getItemProvidersAdapterFactory()));" + NL + "\t\t\ttreeViewer.setInput(myDiagramRoot.eResource());" + NL + "\t\t\ttreeViewer.setSelection(new ";
   protected final String TEXT_264 = "(myDiagramRoot));" + NL + "\t\t\ttreeViewer.addSelectionChangedListener(this);" + NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tpublic void selectionChanged(";
   protected final String TEXT_265 = " event) {" + NL + "\t\t\tmyDiagramRoot = null;" + NL + "\t\t\tif (event.getSelection() instanceof ";
   protected final String TEXT_266 = ") {" + NL + "\t\t\t\t";
   protected final String TEXT_267 = " selection = (";
   protected final String TEXT_268 = ") event.getSelection();" + NL + "\t\t\t\tif (selection.size() == 1 && selection.getFirstElement() instanceof EObject) {" + NL + "\t\t\t\t\tmyDiagramRoot = (EObject) selection.getFirstElement();" + NL + "\t\t\t\t}" + NL + "\t\t\t}" + NL + "\t\t\tsetPageComplete(validatePage());" + NL + "\t\t}" + NL + "\t\t" + NL + "\t\t/**" + NL + "    \t * @generated" + NL + "\t\t */\t" + NL + "\t\tprivate boolean validatePage() {" + NL + "\t\t\tif (myDiagramRoot == null) {" + NL + "\t\t\t\tsetErrorMessage(\"No diagram root element selected\");" + NL + "\t\t\t\treturn false;" + NL + "\t\t\t}" + NL + "\t\t\tboolean result = ViewService.getInstance().provides(new ";
   protected final String TEXT_269 = "(new ";
   protected final String TEXT_270 = "(myDiagramRoot), ";
   protected final String TEXT_271 = ".MODEL_ID, ";
   protected final String TEXT_272 = ".DIAGRAM_PREFERENCES_HINT));" + NL + "\t\t\tsetErrorMessage(result ? null : \"Invalid diagram root element was selected\");" + NL + "\t\t\treturn result;" + NL + "\t\t}" + NL + "\t\t" + NL + "\t}" + NL + "}";
 
 	protected final String getFeatureValueGetter(String containerName, GenFeature feature, boolean isContainerEObject, ImportAssistant importManager) {
 		StringBuffer result = new StringBuffer();
 		if (feature.getGenClass().isExternalInterface()) {
 // Using EMF reflective method to access feature value
 			result.append("((");
 			if (feature.isListType()) {
 				result.append(importManager.getImportedName("java.util.Collection"));
 			} else {
 				result.append(importManager.getImportedName(feature.getTypeGenClass().getQualifiedInterfaceName()));
 			}
 			result.append(")");
 			if (!isContainerEObject) {
 // Casting container to EObject - ExternalIntarfce could be not an instance of EObject
 				result.append("((");
 				result.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
 				result.append(")");
 			}
 			result.append(containerName);
 			if (!isContainerEObject) {
 				result.append(")");
 			}
 			result.append(".eGet(");
 			result.append(importManager.getImportedName(feature.getGenPackage().getQualifiedPackageInterfaceName()));
 			result.append(".eINSTANCE.get");
 			result.append(feature.getFeatureAccessorName());
 			result.append("()))");
 		} else {
 			if (isContainerEObject) {
 // Casting container to the typed interface
 				result.append("((");
 				result.append(importManager.getImportedName(feature.getGenClass().getQualifiedInterfaceName()));
 				result.append(")");
 			}
 			result.append(containerName);
 			if (isContainerEObject) {
 				result.append(")");
 			}
 			result.append(".");
 			result.append(feature.getGetAccessor());
 			result.append("()");
 		}
 		return result.toString();
 	}
 	
 	protected final String getFeatureValueSetterPrefix(String containerName, GenFeature feature, boolean isContainerEObject, ImportAssistant importManager) {
 		StringBuffer result = new StringBuffer();
 		if (feature.getGenClass().isExternalInterface()) {
 // Using EMF reflective method to access feature value
 			if (!isContainerEObject) {
 // Casting container to EObject - ExternalIntarfce could be not an instance of EObject
 				result.append("((");
 				result.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
 				result.append(")");
 			}
 			result.append(containerName);
 			if (!isContainerEObject) {
 				result.append(")");
 			}
 			result.append(".eSet(");
 			result.append(importManager.getImportedName(feature.getGenPackage().getQualifiedPackageInterfaceName()));
 			result.append(".eINSTANCE.get");
 			result.append(feature.getFeatureAccessorName());
 			result.append("(), ");
 		} else {
 			if (isContainerEObject) {
 // Casting container to the typed interface
 				result.append("((");
 				result.append(importManager.getImportedName(feature.getGenClass().getQualifiedInterfaceName()));
 				result.append(")");
 			}
 			result.append(containerName);
 			if (isContainerEObject) {
 				result.append(")");
 			}
 			if (feature.isListType()) {
 				result.append(".");
 				result.append(feature.getGetAccessor());
 				result.append("().add(");
 			} else {
 				result.append(".set");
 				result.append(feature.getAccessorName());
 				result.append("(");
 			}
 		}
 		return result.toString();
 	}
  
 	protected final String getMetaClassAccessor(GenClass genClass, ImportAssistant importManager) {
 		StringBuffer buf = new StringBuffer();
 		buf.append(importManager.getImportedName(genClass.getGenPackage().getQualifiedPackageInterfaceName()))
 			.append(".eINSTANCE.get") //$NON-NLS-1$
 			.append(genClass.getName())
 			.append("()"); //$NON-NLS-1$
 		return buf.toString();
 	} 
  
 	public String generate(Object argument)
   {
     final StringBuffer stringBuffer = new StringBuffer();
     
 final GenDiagram genDiagram = (GenDiagram) ((Object[]) argument)[0];
 final ImportAssistant importManager = (ImportAssistant) ((Object[]) argument)[1];
 final GenEditorGenerator editorGen = genDiagram.getEditorGen();
 final String pluginActivatorClass = importManager.getImportedName(editorGen.getPlugin().getActivatorQualifiedClassName());
 
     stringBuffer.append(TEXT_1);
     
 String copyrightText = genDiagram.getEditorGen().getCopyrightText();
 if (copyrightText != null && copyrightText.trim().length() > 0) {
 
     stringBuffer.append(TEXT_2);
     stringBuffer.append(copyrightText.replaceAll("\n", "\n *"));
     stringBuffer.append(TEXT_3);
     }
     
 importManager.emitPackageStatement(stringBuffer);
 
 importManager.addImport("java.io.IOException");
 importManager.addImport("java.util.Collections");
 importManager.addImport("java.util.List");
 importManager.addImport("java.util.LinkedList");
 importManager.addImport("org.eclipse.core.commands.ExecutionException");
 importManager.addImport("org.eclipse.core.commands.operations.OperationHistoryFactory");
 importManager.addImport("org.eclipse.core.resources.IContainer");
 importManager.addImport("org.eclipse.core.resources.IFile");
 importManager.addImport("org.eclipse.core.runtime.IProgressMonitor");
 importManager.addImport("org.eclipse.core.runtime.IAdaptable");
 importManager.addImport("org.eclipse.core.runtime.NullProgressMonitor");
 importManager.addImport("org.eclipse.core.runtime.Path");
 importManager.addImport("org.eclipse.emf.common.util.URI");
 importManager.addImport("org.eclipse.emf.ecore.EObject");
 importManager.addImport("org.eclipse.emf.ecore.resource.Resource");
 importManager.addImport("org.eclipse.emf.ecore.resource.ResourceSet");
 importManager.addImport("org.eclipse.gmf.runtime.common.core.command.CommandResult");
 importManager.addImport("org.eclipse.gmf.runtime.notation.Diagram");
 importManager.addImport("org.eclipse.gmf.runtime.diagram.core.services.ViewService");
 importManager.addImport("org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand");
 importManager.addImport("org.eclipse.emf.transaction.TransactionalEditingDomain");
 importManager.addImport("org.eclipse.jface.viewers.IStructuredSelection");
 importManager.addImport("org.eclipse.jface.wizard.Wizard");
 importManager.addImport("org.eclipse.swt.widgets.Composite");
 importManager.addImport("org.eclipse.ui.PartInitException");
 importManager.addImport("org.eclipse.ui.ide.IDE");
 importManager.addImport("org.eclipse.ui.dialogs.WizardNewFileCreationPage");
 importManager.addImport("org.eclipse.ui.IWorkbenchPage");
 
 importManager.markImportLocation(stringBuffer);
 
     stringBuffer.append(TEXT_4);
     stringBuffer.append(genDiagram.getNewDiagramFileWizardClassName());
     stringBuffer.append(TEXT_5);
     stringBuffer.append(genDiagram.getNewDiagramFileWizardClassName());
     stringBuffer.append(TEXT_6);
     stringBuffer.append(genDiagram.getNewDiagramFileWizardClassName());
     stringBuffer.append(TEXT_7);
     stringBuffer.append(genDiagram.getNewDiagramFileWizardClassName());
     stringBuffer.append(TEXT_8);
     stringBuffer.append(genDiagram.getNewDiagramFileWizardClassName());
     stringBuffer.append(TEXT_9);
     stringBuffer.append(genDiagram.getNewDiagramFileWizardClassName());
     stringBuffer.append(TEXT_10);
     stringBuffer.append(genDiagram.getNewDiagramFileWizardClassName());
     stringBuffer.append(TEXT_11);
     stringBuffer.append(editorGen.getDiagramFileExtension());
     stringBuffer.append(TEXT_12);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_13);
     stringBuffer.append(importManager.getImportedName("org.eclipse.core.runtime.CoreException"));
     stringBuffer.append(TEXT_14);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditorGen().getPlugin().getActivatorQualifiedClassName()));
     stringBuffer.append(TEXT_15);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_16);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_17);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_18);
     stringBuffer.append(pluginActivatorClass);
     stringBuffer.append(TEXT_19);
     if (editorGen.isSameFileForDiagramAndModel()) {
     stringBuffer.append(TEXT_20);
     
 }
 if (!genDiagram.isSynchronized()) {
 
     stringBuffer.append(TEXT_21);
     }
     stringBuffer.append(TEXT_22);
     stringBuffer.append(pluginActivatorClass);
     stringBuffer.append(TEXT_23);
     stringBuffer.append(pluginActivatorClass);
     stringBuffer.append(TEXT_24);
     stringBuffer.append(pluginActivatorClass);
     stringBuffer.append(TEXT_25);
     if (!genDiagram.isSynchronized()) {
     stringBuffer.append(TEXT_26);
     
 List genLinks = genDiagram.getLinks();
 
     stringBuffer.append(TEXT_27);
     
 // Collecting all phantom elements
 Map genClass2Phantom = new LinkedHashMap();
 for (Iterator topLevelNodes = genDiagram.getTopLevelNodes().iterator(); topLevelNodes.hasNext();) {
 	GenTopLevelNode nextTopLevelNode = (GenTopLevelNode) topLevelNodes.next();
 	TypeModelFacet nextModelFacet = nextTopLevelNode.getModelFacet();
 	if (nextModelFacet == null || !nextModelFacet.isPhantomElement()) {
 		continue;
 	}
 	genClass2Phantom.put(nextModelFacet.getMetaClass(), nextTopLevelNode);
 }
 
     stringBuffer.append(TEXT_28);
     stringBuffer.append(importManager.getImportedName("java.util.Collection"));
     stringBuffer.append(TEXT_29);
     stringBuffer.append(importManager.getImportedName("java.util.LinkedList"));
     stringBuffer.append(TEXT_30);
     stringBuffer.append(importManager.getImportedName("java.util.Map"));
     stringBuffer.append(TEXT_31);
     stringBuffer.append(importManager.getImportedName("java.util.HashMap"));
     stringBuffer.append(TEXT_32);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Diagram"));
     stringBuffer.append(TEXT_33);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_34);
     stringBuffer.append(genDiagram.getUniqueIdentifier());
     stringBuffer.append(TEXT_35);
     stringBuffer.append(TEXT_36);
     
 if (!genClass2Phantom.isEmpty()) {
 
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.resource.Resource"));
     stringBuffer.append(TEXT_37);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_38);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_39);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_40);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_41);
     
 }
 for (Iterator phantoms = genClass2Phantom.values().iterator(); phantoms.hasNext();) {
 	GenTopLevelNode phantomNode = (GenTopLevelNode) phantoms.next();
 
     stringBuffer.append(TEXT_42);
     stringBuffer.append(importManager.getImportedName(phantomNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_43);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_44);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_45);
     stringBuffer.append(importManager.getImportedName(phantomNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_46);
     stringBuffer.append(pluginActivatorClass);
     stringBuffer.append(TEXT_47);
     stringBuffer.append(phantomNode.getUniqueIdentifier());
     stringBuffer.append(TEXT_48);
     stringBuffer.append(TEXT_49);
     stringBuffer.append(TEXT_50);
     }
 if (!genClass2Phantom.isEmpty()) {
 
     stringBuffer.append(TEXT_51);
     }
     
 // TODO: remove "createLinks()" method - this task should be done by canonicalEditPolicies
 
     stringBuffer.append(TEXT_52);
     
 boolean generateGetCompartment = false;
 
 for (Iterator containers = genDiagram.getAllContainers().iterator(); containers.hasNext();) {
 	GenContainerBase nextContainer = (GenContainerBase) containers.next();
 
     stringBuffer.append(TEXT_53);
     stringBuffer.append(nextContainer.getUniqueIdentifier());
     stringBuffer.append(TEXT_54);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_55);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_56);
     	{
 		List genNodes = nextContainer.getContainedNodes();
     stringBuffer.append(TEXT_57);
     
 Map genFeature2genNodeMap = new LinkedHashMap();
 for (int nodeIndex = 0; nodeIndex < genNodes.size(); nodeIndex++) {
 	GenNode nextNode = (GenNode) genNodes.get(nodeIndex);
 	TypeModelFacet typeModelFacet = nextNode.getModelFacet();
 	if (typeModelFacet == null || typeModelFacet.isPhantomElement()) {
 		// Skipping top-level phantoms
 		continue;
 	}
 	GenFeature childMetaFeature = typeModelFacet.getChildMetaFeature();
 	if (!genFeature2genNodeMap.containsKey(childMetaFeature)) {
 		genFeature2genNodeMap.put(childMetaFeature, new ArrayList());
 	}
 	((Collection) genFeature2genNodeMap.get(childMetaFeature)).add(nextNode);
 }
 
 Set entrySet = genFeature2genNodeMap.entrySet();
 if (entrySet.size() > 0) {
 
     stringBuffer.append(TEXT_58);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_59);
     
 }
 
 for (Iterator entries = entrySet.iterator(); entries.hasNext();) {
 	Map.Entry nextEntry = (Map.Entry) entries.next();
 	GenFeature childMetaFeature = (GenFeature) nextEntry.getKey();
 	Collection genNodesCollection = (Collection) nextEntry.getValue();
 	if (childMetaFeature.isListType()) {
 
     stringBuffer.append(TEXT_60);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_61);
     stringBuffer.append(getFeatureValueGetter("modelObject", childMetaFeature, true, importManager));
     stringBuffer.append(TEXT_62);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_63);
     	} else {
     stringBuffer.append(TEXT_64);
     stringBuffer.append(getFeatureValueGetter("modelObject", childMetaFeature, true, importManager));
     stringBuffer.append(TEXT_65);
     	}
     stringBuffer.append(TEXT_66);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_67);
     	boolean generateSwitch = genNodesCollection.size() != 1;
 	if (generateSwitch) {
     stringBuffer.append(TEXT_68);
     	}
 	
 	for (Iterator genNodesIterator = genNodesCollection.iterator(); genNodesIterator.hasNext();) {
 		GenNode nextNode = (GenNode) genNodesIterator.next();
 		if (generateSwitch) {
     stringBuffer.append(TEXT_69);
     stringBuffer.append(importManager.getImportedName(nextNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_70);
     		} else {
     stringBuffer.append(TEXT_71);
     stringBuffer.append(importManager.getImportedName(nextNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_72);
     		}
     stringBuffer.append(TEXT_73);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_74);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_75);
     stringBuffer.append(importManager.getImportedName(nextNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_76);
     stringBuffer.append(pluginActivatorClass);
     stringBuffer.append(TEXT_77);
     stringBuffer.append(nextNode.getUniqueIdentifier());
     stringBuffer.append(TEXT_78);
     stringBuffer.append(TEXT_79);
     		if (generateSwitch) {
     stringBuffer.append(TEXT_80);
     		}
     stringBuffer.append(TEXT_81);
     	}
 	if (generateSwitch) {
     stringBuffer.append(TEXT_82);
     	}
 	if (childMetaFeature.isListType()) {
     stringBuffer.append(TEXT_83);
     	}
 }
     	}
 
 	if (nextContainer instanceof GenNode) {
 		GenNode nextNode = (GenNode) nextContainer;
 		for (int i = 0; i < nextNode.getCompartments().size(); i++) {
 			generateGetCompartment = true;
 			GenCompartment nextCompartment = (GenCompartment) nextNode.getCompartments().get(i);
 
     stringBuffer.append(TEXT_84);
     stringBuffer.append(i == 0 ? importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node") : "");
     stringBuffer.append(TEXT_85);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_86);
     stringBuffer.append(importManager.getImportedName(nextCompartment.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_87);
     stringBuffer.append(nextCompartment.getUniqueIdentifier());
     stringBuffer.append(TEXT_88);
     
 		}
 	}
 	if (false == nextContainer instanceof GenCompartment) {
 
     stringBuffer.append(TEXT_89);
     	}
     stringBuffer.append(TEXT_90);
     
 }
 
 if (generateGetCompartment) {
 
     stringBuffer.append(TEXT_91);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_92);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_93);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_94);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_95);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_96);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_97);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_98);
     
 }
 
     stringBuffer.append(TEXT_99);
     boolean createPhantoms = true;
     stringBuffer.append(TEXT_100);
     stringBuffer.append(TEXT_101);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_102);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Diagram"));
     stringBuffer.append(TEXT_103);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EClass"));
     stringBuffer.append(TEXT_104);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_105);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EClass"));
     stringBuffer.append(TEXT_106);
     
 for (Iterator it = genLinks.iterator(); it.hasNext();) {
 	GenLink nextLink = (GenLink) it.next();
 	if (nextLink.getModelFacet() instanceof TypeLinkModelFacet) {
 		TypeLinkModelFacet typeLinkModelFacet = (TypeLinkModelFacet) nextLink.getModelFacet();
 		GenFeature childMetaFeature = typeLinkModelFacet.getChildMetaFeature();
 
     stringBuffer.append(TEXT_107);
     stringBuffer.append(importManager.getImportedName(childMetaFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_108);
     stringBuffer.append(childMetaFeature.getGenClass().getClassifierAccessorName());
     stringBuffer.append(TEXT_109);
     		if (childMetaFeature.isListType()) {
     stringBuffer.append(TEXT_110);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_111);
     stringBuffer.append(getFeatureValueGetter("container", childMetaFeature, true, importManager));
     stringBuffer.append(TEXT_112);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_113);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_114);
     		} else {
     stringBuffer.append(TEXT_115);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_116);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_117);
     stringBuffer.append(getFeatureValueGetter("container", childMetaFeature, true, importManager));
     stringBuffer.append(TEXT_118);
     		}
     stringBuffer.append(TEXT_119);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_120);
     stringBuffer.append(importManager.getImportedName(nextLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_121);
     stringBuffer.append(getFeatureValueGetter("nextValue", typeLinkModelFacet.getTargetMetaFeature(), true, importManager));
     stringBuffer.append(TEXT_122);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_123);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_124);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_125);
     		if (typeLinkModelFacet.getSourceMetaFeature() != null) {
     stringBuffer.append(TEXT_126);
     stringBuffer.append(getFeatureValueGetter("nextValue", typeLinkModelFacet.getSourceMetaFeature(), true, importManager));
     stringBuffer.append(TEXT_127);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_128);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_129);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_130);
     		} else {
     stringBuffer.append(TEXT_131);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_132);
     		}
     stringBuffer.append(TEXT_133);
     		if (typeLinkModelFacet.getSourceMetaFeature() != null) {
     stringBuffer.append(TEXT_134);
     		}
     stringBuffer.append(TEXT_135);
     
 		if (childMetaFeature.isListType()) {
 
     stringBuffer.append(TEXT_136);
     
 		}
 
     stringBuffer.append(TEXT_137);
     
 	}
 }
 
     stringBuffer.append(TEXT_138);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_139);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EClass"));
     stringBuffer.append(TEXT_140);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Diagram"));
     stringBuffer.append(TEXT_141);
     
 for (int linkIndex = 0; linkIndex < genLinks.size(); linkIndex++) {
 	boolean phantomsOnly = true;
 
     stringBuffer.append(TEXT_142);
     
 GenLink nextLink = (GenLink) genLinks.get(linkIndex);
 if (false == nextLink.getModelFacet() instanceof FeatureModelFacet) {
 	continue;
 }
 FeatureModelFacet linkFMF = (FeatureModelFacet) nextLink.getModelFacet();
 GenFeature genFeature = linkFMF.getMetaFeature();
 GenTopLevelNode phantomNode = (GenTopLevelNode) genClass2Phantom.get(genFeature.getTypeGenClass());
 if (phantomsOnly ? (!genFeature.isContains() || phantomNode == null) : (genFeature.isContains())) {
 // Processing only features which are isPhantoms == phantomsOnly
 	continue;
 }
 
     stringBuffer.append(TEXT_143);
     stringBuffer.append(importManager.getImportedName(genFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_144);
     stringBuffer.append(genFeature.getGenClass().getClassifierAccessorName());
     stringBuffer.append(TEXT_145);
     	if (genFeature.isListType()) {
     stringBuffer.append(TEXT_146);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_147);
     stringBuffer.append(getFeatureValueGetter("container", genFeature, true, importManager));
     stringBuffer.append(TEXT_148);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_149);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_150);
     	} else {
     stringBuffer.append(TEXT_151);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_152);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_153);
     stringBuffer.append(getFeatureValueGetter("container", genFeature, true, importManager));
     stringBuffer.append(TEXT_154);
     	}
     	if (phantomsOnly) {
     stringBuffer.append(TEXT_155);
     stringBuffer.append(importManager.getImportedName(phantomNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_156);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_157);
     	}
     stringBuffer.append(TEXT_158);
     stringBuffer.append(importManager.getImportedName(genDiagram.getElementTypesQualifiedClassName()));
     stringBuffer.append(TEXT_159);
     stringBuffer.append(nextLink.getUniqueIdentifier());
     stringBuffer.append(TEXT_160);
     stringBuffer.append(importManager.getImportedName(nextLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_161);
     	if (createPhantoms) {
     stringBuffer.append(TEXT_162);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_163);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_164);
     stringBuffer.append(importManager.getImportedName(phantomNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_165);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditorGen().getPlugin().getActivatorQualifiedClassName()));
     stringBuffer.append(TEXT_166);
     stringBuffer.append(phantomNode.getUniqueIdentifier());
     stringBuffer.append(TEXT_167);
     	}
     stringBuffer.append(TEXT_168);
     	if (phantomsOnly) {
     stringBuffer.append(TEXT_169);
     	}
 	if (genFeature.isListType()) {
     stringBuffer.append(TEXT_170);
     	}
     stringBuffer.append(TEXT_171);
     
 }
 
 for (int linkIndex = 0; linkIndex < genLinks.size(); linkIndex++) {
 	boolean phantomsOnly = false;
 
     stringBuffer.append(TEXT_172);
     
 GenLink nextLink = (GenLink) genLinks.get(linkIndex);
 if (false == nextLink.getModelFacet() instanceof FeatureModelFacet) {
 	continue;
 }
 FeatureModelFacet linkFMF = (FeatureModelFacet) nextLink.getModelFacet();
 GenFeature genFeature = linkFMF.getMetaFeature();
 GenTopLevelNode phantomNode = (GenTopLevelNode) genClass2Phantom.get(genFeature.getTypeGenClass());
 if (phantomsOnly ? (!genFeature.isContains() || phantomNode == null) : (genFeature.isContains())) {
 // Processing only features which are isPhantoms == phantomsOnly
 	continue;
 }
 
     stringBuffer.append(TEXT_173);
     stringBuffer.append(importManager.getImportedName(genFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_174);
     stringBuffer.append(genFeature.getGenClass().getClassifierAccessorName());
     stringBuffer.append(TEXT_175);
     	if (genFeature.isListType()) {
     stringBuffer.append(TEXT_176);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_177);
     stringBuffer.append(getFeatureValueGetter("container", genFeature, true, importManager));
     stringBuffer.append(TEXT_178);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_179);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_180);
     	} else {
     stringBuffer.append(TEXT_181);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_182);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_183);
     stringBuffer.append(getFeatureValueGetter("container", genFeature, true, importManager));
     stringBuffer.append(TEXT_184);
     	}
     	if (phantomsOnly) {
     stringBuffer.append(TEXT_185);
     stringBuffer.append(importManager.getImportedName(phantomNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_186);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_187);
     	}
     stringBuffer.append(TEXT_188);
     stringBuffer.append(importManager.getImportedName(genDiagram.getElementTypesQualifiedClassName()));
     stringBuffer.append(TEXT_189);
     stringBuffer.append(nextLink.getUniqueIdentifier());
     stringBuffer.append(TEXT_190);
     stringBuffer.append(importManager.getImportedName(nextLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_191);
     stringBuffer.append(TEXT_192);
     	if (phantomsOnly) {
     stringBuffer.append(TEXT_193);
     	}
 	if (genFeature.isListType()) {
     stringBuffer.append(TEXT_194);
     	}
     stringBuffer.append(TEXT_195);
     }
     stringBuffer.append(TEXT_196);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Diagram"));
     stringBuffer.append(TEXT_197);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_198);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_199);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_200);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.diagram.core.util.ViewUtil"));
     stringBuffer.append(TEXT_201);
     stringBuffer.append(pluginActivatorClass);
     stringBuffer.append(TEXT_202);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_203);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_204);
     stringBuffer.append(TEXT_205);
     
 // an inner class 
 
     stringBuffer.append(TEXT_206);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_207);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_208);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_209);
     stringBuffer.append(importManager.getImportedName("org.eclipse.core.runtime.IAdaptable"));
     stringBuffer.append(TEXT_210);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_211);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_212);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_213);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter"));
     stringBuffer.append(TEXT_214);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_215);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_216);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.emf.type.core.IElementType"));
     stringBuffer.append(TEXT_217);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.emf.type.core.IElementType"));
     stringBuffer.append(TEXT_218);
     stringBuffer.append(importManager.getImportedName("org.eclipse.core.runtime.IAdaptable"));
     stringBuffer.append(TEXT_219);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.emf.type.core.IElementType"));
     stringBuffer.append(TEXT_220);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_221);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_222);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_223);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_224);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_225);
     stringBuffer.append(importManager.getImportedName("org.eclipse.core.runtime.IAdaptable"));
     stringBuffer.append(TEXT_226);
     }
     stringBuffer.append(TEXT_227);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.wizard.WizardPage"));
     stringBuffer.append(TEXT_228);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.viewers.ISelectionChangedListener"));
     stringBuffer.append(TEXT_229);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.widgets.Composite"));
     stringBuffer.append(TEXT_230);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.widgets.Composite"));
     stringBuffer.append(TEXT_231);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.widgets.Composite"));
     stringBuffer.append(TEXT_232);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.SWT"));
     stringBuffer.append(TEXT_233);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridLayout"));
     stringBuffer.append(TEXT_234);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridData"));
     stringBuffer.append(TEXT_235);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridData"));
     stringBuffer.append(TEXT_236);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridData"));
     stringBuffer.append(TEXT_237);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.widgets.Composite"));
     stringBuffer.append(TEXT_238);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.widgets.Composite"));
     stringBuffer.append(TEXT_239);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.widgets.Composite"));
     stringBuffer.append(TEXT_240);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.SWT"));
     stringBuffer.append(TEXT_241);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridData"));
     stringBuffer.append(TEXT_242);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridData"));
     stringBuffer.append(TEXT_243);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridLayout"));
     stringBuffer.append(TEXT_244);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridLayout"));
     stringBuffer.append(TEXT_245);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.widgets.Label"));
     stringBuffer.append(TEXT_246);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.widgets.Label"));
     stringBuffer.append(TEXT_247);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.SWT"));
     stringBuffer.append(TEXT_248);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridData"));
     stringBuffer.append(TEXT_249);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridData"));
     stringBuffer.append(TEXT_250);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.viewers.TreeViewer"));
     stringBuffer.append(TEXT_251);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.viewers.TreeViewer"));
     stringBuffer.append(TEXT_252);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.SWT"));
     stringBuffer.append(TEXT_253);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.SWT"));
     stringBuffer.append(TEXT_254);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.SWT"));
     stringBuffer.append(TEXT_255);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.SWT"));
     stringBuffer.append(TEXT_256);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridData"));
     stringBuffer.append(TEXT_257);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridData"));
     stringBuffer.append(TEXT_258);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.layout.GridData"));
     stringBuffer.append(TEXT_259);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider"));
     stringBuffer.append(TEXT_260);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditorGen().getPlugin().getActivatorQualifiedClassName()));
     stringBuffer.append(TEXT_261);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider"));
     stringBuffer.append(TEXT_262);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditorGen().getPlugin().getActivatorQualifiedClassName()));
     stringBuffer.append(TEXT_263);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.viewers.StructuredSelection"));
     stringBuffer.append(TEXT_264);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.viewers.SelectionChangedEvent"));
     stringBuffer.append(TEXT_265);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.viewers.IStructuredSelection"));
     stringBuffer.append(TEXT_266);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.viewers.IStructuredSelection"));
     stringBuffer.append(TEXT_267);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.viewers.IStructuredSelection"));
     stringBuffer.append(TEXT_268);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.diagram.core.services.view.CreateDiagramViewOperation"));
     stringBuffer.append(TEXT_269);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter"));
     stringBuffer.append(TEXT_270);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_271);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditorGen().getPlugin().getActivatorQualifiedClassName()));
     stringBuffer.append(TEXT_272);
     importManager.emitSortedImports();
     return stringBuffer.toString();
   }
 }
