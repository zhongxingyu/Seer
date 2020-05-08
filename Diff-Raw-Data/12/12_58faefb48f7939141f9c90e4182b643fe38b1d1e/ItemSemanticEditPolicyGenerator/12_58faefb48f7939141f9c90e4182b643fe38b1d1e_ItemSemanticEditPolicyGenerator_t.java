 package org.eclipse.gmf.codegen.templates.policies;
 
 import org.eclipse.gmf.codegen.gmfgen.*;
 import org.eclipse.gmf.common.codegen.*;
 import org.eclipse.emf.codegen.ecore.genmodel.*;
 import java.util.*;
 
 public class ItemSemanticEditPolicyGenerator {
  
   protected static String nl;
   public static synchronized ItemSemanticEditPolicyGenerator create(String lineSeparator)
   {
     nl = lineSeparator;
     ItemSemanticEditPolicyGenerator result = new ItemSemanticEditPolicyGenerator();
     nl = null;
     return result;
   }
 
   protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "";
   protected final String TEXT_2 = NL + "/**" + NL + " *";
   protected final String TEXT_3 = NL + " */";
   protected final String TEXT_4 = NL;
  protected final String TEXT_5 = NL + "import org.eclipse.emf.ecore.EClass;" + NL + "import org.eclipse.emf.ecore.EObject;" + NL + "import org.eclipse.emf.transaction.TransactionalEditingDomain;" + NL + "import org.eclipse.gef.commands.Command;" + NL + "import org.eclipse.gmf.runtime.common.core.command.ICommand;" + NL + "import org.eclipse.gmf.runtime.diagram.core.commands.DeleteCommand;" + NL + "import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;" + NL + "import org.eclipse.gmf.runtime.diagram.ui.commands.EtoolsProxyCommand;" + NL + "import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;" + NL + "import org.eclipse.gmf.runtime.diagram.ui.editpolicies.SemanticEditPolicy;" + NL + "import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.IEditHelperContext;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.IElementType;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyReferenceRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.DuplicateElementsRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.GetEditContextRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.MoveRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientReferenceRelationshipRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;" + NL + "import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;" + NL + "import org.eclipse.gmf.runtime.notation.View;";
   protected final String TEXT_6 = NL + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "public class ";
  protected final String TEXT_7 = " extends SemanticEditPolicy {" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getSemanticCommand(IEditCommandRequest request) {" + NL + "\t\tIEditCommandRequest completedRequest = completeRequest(request);" + NL + "\t\tObject editHelperContext = completedRequest.getEditHelperContext();" + NL + "\t\tif (editHelperContext instanceof View || (editHelperContext instanceof IEditHelperContext && ((IEditHelperContext) editHelperContext).getEObject() instanceof View)) {" + NL + "\t\t\t// no semantic commands are provided for pure design elements" + NL + "\t\t\treturn null;" + NL + "\t\t}" + NL + "\t\tIElementType elementType = ElementTypeRegistry.getInstance().getElementType(editHelperContext);" + NL + "\t\tif (elementType == ElementTypeRegistry.getInstance().getType(\"org.eclipse.gmf.runtime.emf.type.core.default\")) {" + NL + "\t\t\t";
   protected final String TEXT_8 = ".getInstance().logInfo(\"Failed to get element type for \" + editHelperContext);" + NL + "\t\t\telementType = null;" + NL + "\t\t}" + NL + "\t\tCommand semanticHelperCommand = null;" + NL + "\t\tif (elementType != null) {" + NL + "\t\t\tICommand semanticCommand = elementType.getEditCommand(completedRequest);" + NL + "\t\t\tif (semanticCommand != null) {" + NL + "\t\t\t\tsemanticHelperCommand = new EtoolsProxyCommand(semanticCommand);" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t\tCommand semanticPolicyCommand = getSemanticCommandSwitch(completedRequest);" + NL + "" + NL + "\t\t// combine commands from edit policy and edit helper" + NL + "\t\tif (semanticPolicyCommand == null) {" + NL + "\t\t\tif (semanticHelperCommand == null) {" + NL + "\t\t\t\treturn null;" + NL + "\t\t\t} else {" + NL + "\t\t\t\tsemanticPolicyCommand = semanticHelperCommand;" + NL + "\t\t\t}" + NL + "\t\t} else {" + NL + "\t\t\tif (semanticHelperCommand != null) {" + NL + "\t\t\t\tsemanticPolicyCommand = semanticPolicyCommand.chain(semanticHelperCommand);" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "" + NL + "\t\t// append command to delete view if necessary" + NL + "\t\tboolean shouldProceed = true;" + NL + "\t\tif (completedRequest instanceof DestroyRequest) {" + NL + "\t\t\tshouldProceed = shouldProceed((DestroyRequest) completedRequest);" + NL + "\t\t}" + NL + "\t\tif (shouldProceed) {" + NL + "\t\t\tif (completedRequest instanceof DestroyRequest) {" + NL + "\t\t\t\tCommand deleteViewCommand = new EtoolsProxyCommand(new DeleteCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), (View) getHost().getModel()));" + NL + "\t\t\t\tsemanticPolicyCommand = semanticPolicyCommand.chain(deleteViewCommand);" + NL + "\t\t\t}" + NL + "\t\t\treturn semanticPolicyCommand;" + NL + "\t\t}" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getSemanticCommandSwitch(IEditCommandRequest req) {" + NL + "\t\tif (req instanceof CreateRelationshipRequest) {" + NL + "\t\t\treturn getCreateRelationshipCommand((CreateRelationshipRequest) req);" + NL + "\t\t} else if (req instanceof CreateElementRequest) {" + NL + "\t\t\treturn getCreateCommand((CreateElementRequest) req);" + NL + "\t\t} else if (req instanceof ConfigureRequest) {" + NL + "\t\t\treturn getConfigureCommand((ConfigureRequest) req);" + NL + "\t\t} else if (req instanceof DestroyElementRequest) {" + NL + "\t\t\treturn getDestroyElementCommand((DestroyElementRequest) req);" + NL + "\t\t} else if (req instanceof DestroyReferenceRequest) {" + NL + "\t\t\treturn getDestroyReferenceCommand((DestroyReferenceRequest) req);" + NL + "\t\t} else if (req instanceof DuplicateElementsRequest) {" + NL + "\t\t\treturn getDuplicateCommand((DuplicateElementsRequest) req);" + NL + "\t\t} else if (req instanceof GetEditContextRequest) {" + NL + "\t\t\treturn getEditContextCommand((GetEditContextRequest) req);" + NL + "\t\t} else if (req instanceof MoveRequest) {" + NL + "\t\t\treturn getMoveCommand((MoveRequest) req);" + NL + "\t\t} else if (req instanceof ReorientReferenceRelationshipRequest) {" + NL + "\t\t\treturn getReorientReferenceRelationshipCommand((ReorientReferenceRelationshipRequest) req);" + NL + "\t\t} else if (req instanceof ReorientRelationshipRequest) {" + NL + "\t\t\treturn getReorientRelationshipCommand((ReorientRelationshipRequest) req);" + NL + "\t\t} else if (req instanceof SetRequest) {" + NL + "\t\t\treturn getSetCommand((SetRequest) req);" + NL + "\t\t}" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getConfigureCommand(ConfigureRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getCreateRelationshipCommand(CreateRelationshipRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getCreateCommand(CreateElementRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getSetCommand(SetRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getEditContextCommand(GetEditContextRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getDestroyElementCommand(DestroyElementRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getDestroyReferenceCommand(DestroyReferenceRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getDuplicateCommand(DuplicateElementsRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getMoveCommand(MoveRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getReorientReferenceRelationshipCommand(ReorientReferenceRelationshipRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getReorientRelationshipCommand(ReorientRelationshipRequest req) {" + NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected Command getMSLWrapper(ICommand cmd) {" + NL + "\t\tTransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();" + NL + "\t\tCompositeTransactionalCommand modelCmd = new CompositeTransactionalCommand(editingDomain, cmd.getLabel());" + NL + "\t\tmodelCmd.compose(cmd);" + NL + "\t\treturn new EtoolsProxyCommand(modelCmd);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected EObject getSemanticElement() {" + NL + "\t\treturn ViewUtil.resolveSemanticElement((View) getHost().getModel());" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * Finds container element for the new relationship of the specified type." + NL + "\t * Default implementation goes up by containment hierarchy starting from" + NL + "\t * the specified element and returns the first element that is instance of" + NL + "\t * the specified container class." + NL + "\t * " + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected EObject getRelationshipContainer(EObject element, EClass containerClass, IElementType relationshipType) {" + NL + "\t\tfor (; element != null; element = element.eContainer()) {" + NL + "\t\t\tif (containerClass.isSuperTypeOf(element.eClass())) {" + NL + "\t\t\t\treturn element;" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t\treturn null;" + NL + "\t}" + NL;
   protected final String TEXT_9 = NL + "\t/**" + NL + "\t * @generated " + NL + "\t */" + NL + "\tprotected static class ";
   protected final String TEXT_10 = " {";
   protected final String TEXT_11 = NL + "\t\t/**" + NL + "\t\t * @generated " + NL + "\t\t */" + NL + "\t\tpublic static final ";
   protected final String TEXT_12 = " ";
   protected final String TEXT_13 = " = create";
   protected final String TEXT_14 = "();";
   protected final String TEXT_15 = NL;
   protected final String TEXT_16 = NL + "\t\t/**" + NL + "\t\t * @generated " + NL + "\t\t */" + NL + "\t\tprivate static ";
   protected final String TEXT_17 = " create";
   protected final String TEXT_18 = "() {";
   protected final String TEXT_19 = NL;
   protected final String TEXT_20 = " ";
   protected final String TEXT_21 = " = new ";
   protected final String TEXT_22 = "(3);";
   protected final String TEXT_23 = ".put(\"";
   protected final String TEXT_24 = "\", ";
   protected final String TEXT_25 = "); //$NON-NLS-1$";
   protected final String TEXT_26 = "\t\t\t\t" + NL + "\t\t\t";
   protected final String TEXT_27 = " sourceExpression = ";
   protected final String TEXT_28 = "null";
   protected final String TEXT_29 = ".";
   protected final String TEXT_30 = "(\"";
   protected final String TEXT_31 = "\", //$NON-NLS-1$" + NL + "\t";
   protected final String TEXT_32 = ", ";
   protected final String TEXT_33 = ")";
   protected final String TEXT_34 = ".";
   protected final String TEXT_35 = "(\"";
   protected final String TEXT_36 = "\", //$NON-NLS-1$" + NL + "\t";
   protected final String TEXT_37 = ")";
   protected final String TEXT_38 = "new ";
   protected final String TEXT_39 = "(";
   protected final String TEXT_40 = ") {" + NL + "\tprotected Object doEvaluate(Object context, ";
   protected final String TEXT_41 = " env) {\t" + NL + "\t\t";
   protected final String TEXT_42 = " self = (";
   protected final String TEXT_43 = ")context;";
   protected final String TEXT_44 = "\t" + NL + "\t\t";
   protected final String TEXT_45 = " ";
   protected final String TEXT_46 = " = (";
   protected final String TEXT_47 = ")env.get(\"";
   protected final String TEXT_48 = "\"); //$NON-NLS-1$";
   protected final String TEXT_49 = NL + "\t\treturn ";
   protected final String TEXT_50 = ".";
   protected final String TEXT_51 = "(self";
   protected final String TEXT_52 = ", ";
   protected final String TEXT_53 = ");" + NL + "\t}" + NL + "}";
   protected final String TEXT_54 = "null";
   protected final String TEXT_55 = ";";
   protected final String TEXT_56 = NL;
   protected final String TEXT_57 = " ";
   protected final String TEXT_58 = " = new ";
   protected final String TEXT_59 = "(3);";
   protected final String TEXT_60 = ".put(\"";
   protected final String TEXT_61 = "\", ";
   protected final String TEXT_62 = "); //$NON-NLS-1$";
   protected final String TEXT_63 = NL + "\t\t\t";
   protected final String TEXT_64 = " targetExpression = ";
   protected final String TEXT_65 = "null";
   protected final String TEXT_66 = ".";
   protected final String TEXT_67 = "(\"";
   protected final String TEXT_68 = "\", //$NON-NLS-1$" + NL + "\t";
   protected final String TEXT_69 = ", ";
   protected final String TEXT_70 = ")";
   protected final String TEXT_71 = ".";
   protected final String TEXT_72 = "(\"";
   protected final String TEXT_73 = "\", //$NON-NLS-1$" + NL + "\t";
   protected final String TEXT_74 = ")";
   protected final String TEXT_75 = "new ";
   protected final String TEXT_76 = "(";
   protected final String TEXT_77 = ") {" + NL + "\tprotected Object doEvaluate(Object context, ";
   protected final String TEXT_78 = " env) {\t" + NL + "\t\t";
   protected final String TEXT_79 = " self = (";
   protected final String TEXT_80 = ")context;";
   protected final String TEXT_81 = "\t" + NL + "\t\t";
   protected final String TEXT_82 = " ";
   protected final String TEXT_83 = " = (";
   protected final String TEXT_84 = ")env.get(\"";
   protected final String TEXT_85 = "\"); //$NON-NLS-1$";
   protected final String TEXT_86 = NL + "\t\treturn ";
   protected final String TEXT_87 = ".";
   protected final String TEXT_88 = "(self";
   protected final String TEXT_89 = ", ";
   protected final String TEXT_90 = ");" + NL + "\t}" + NL + "}";
   protected final String TEXT_91 = "null";
   protected final String TEXT_92 = ";" + NL + "\t\t\treturn new ";
   protected final String TEXT_93 = "(sourceExpression, targetExpression);" + NL + "\t\t}";
   protected final String TEXT_94 = NL + "\t\t/**" + NL + "\t\t * @generated " + NL + "\t\t */\t" + NL + "\t\tprivate static final String OPPOSITE_END_VAR = \"oppositeEnd\"; //$NON-NLS-1$" + NL + "\t\t/**" + NL + "\t\t * @generated " + NL + "\t\t */\t" + NL + "\t\tprivate ";
   protected final String TEXT_95 = " srcEndInv;" + NL + "\t\t/**" + NL + "\t\t * @generated " + NL + "\t\t */\t" + NL + "\t\tprivate ";
   protected final String TEXT_96 = " targetEndInv;" + NL + "\t\t/**" + NL + "\t\t * @generated " + NL + "\t\t */\t\t" + NL + "\t\tpublic ";
   protected final String TEXT_97 = "(";
   protected final String TEXT_98 = " sourceEnd, ";
   protected final String TEXT_99 = " targetEnd) {" + NL + "\t\t\tthis.srcEndInv = sourceEnd;\t\t\t" + NL + "\t\t\tthis.targetEndInv = targetEnd;\t\t\t" + NL + "\t\t}" + NL + "\t\t" + NL + "\t\t/**" + NL + "\t\t * @generated " + NL + "\t\t */\t" + NL + "\t\tpublic boolean canCreateLink(";
   protected final String TEXT_100 = " req, boolean isBackDirected) {" + NL + "\t\t\tObject source = req.getSource();" + NL + "\t\t\tObject target = req.getTarget();" + NL + "" + NL + "\t\t\t";
   protected final String TEXT_101 = " sourceConstraint = isBackDirected ? targetEndInv : srcEndInv;" + NL + "\t\t\t";
   protected final String TEXT_102 = " targetConstraint = null;" + NL + "\t\t\tif(req.getTarget() != null) {" + NL + "\t\t\t\ttargetConstraint = isBackDirected ? srcEndInv : targetEndInv;" + NL + "\t\t\t}" + NL + "\t\t\tboolean isSourceAccepted = sourceConstraint != null ? evaluate(sourceConstraint, source, target, false) : true;" + NL + "\t\t\tif(isSourceAccepted && targetConstraint != null) {" + NL + "\t\t\t\treturn evaluate(targetConstraint, target, source, true);" + NL + "\t\t\t}" + NL + "\t\t\treturn isSourceAccepted;" + NL + "\t\t}" + NL + "\t" + NL + "\t\t/**" + NL + "\t\t * @generated " + NL + "\t\t */" + NL + "\t\tprivate static boolean evaluate(";
   protected final String TEXT_103 = " constraint, Object sourceEnd, Object oppositeEnd, boolean clearEnv) {" + NL + "\t\t\t";
   protected final String TEXT_104 = " evalEnv = ";
   protected final String TEXT_105 = ".singletonMap(OPPOSITE_END_VAR, oppositeEnd);\t\t\t" + NL + "\t\t\ttry {" + NL + "\t\t\t\tObject val = constraint.evaluate(sourceEnd, evalEnv);" + NL + "\t\t\t\treturn (val instanceof Boolean) ? ((Boolean) val).booleanValue() : false;" + NL + "\t\t\t} catch(Exception e) {\t" + NL + "\t\t\t\t";
   protected final String TEXT_106 = ".getInstance().logError(\"Link constraint evaluation error\", e); //$NON-NLS-1$" + NL + "\t\t\t\treturn false;" + NL + "\t\t\t}" + NL + "\t\t}";
   protected final String TEXT_107 = "\t\t" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate static class ";
   protected final String TEXT_108 = " {";
   protected final String TEXT_109 = NL;
   protected final String TEXT_110 = NL + "/**" + NL + " * @generated" + NL + " */" + NL + "private static ";
   protected final String TEXT_111 = " ";
   protected final String TEXT_112 = "(";
   protected final String TEXT_113 = " self";
   protected final String TEXT_114 = ", ";
   protected final String TEXT_115 = " ";
   protected final String TEXT_116 = ") {" + NL + "\t// TODO: implement this method" + NL + "\t// Ensure that you remove @generated or mark it @generated NOT" + NL + "\t" + NL + "\tthrow new UnsupportedOperationException(\"No user implementation provided in '";
   protected final String TEXT_117 = "' operation\"); //$NON-NLS-1$" + NL + "}";
   protected final String TEXT_118 = NL;
   protected final String TEXT_119 = NL + "/**" + NL + " * @generated" + NL + " */" + NL + "private static ";
   protected final String TEXT_120 = " ";
   protected final String TEXT_121 = "(";
   protected final String TEXT_122 = " self";
   protected final String TEXT_123 = ", ";
   protected final String TEXT_124 = " ";
   protected final String TEXT_125 = ") {" + NL + "\t// TODO: implement this method" + NL + "\t// Ensure that you remove @generated or mark it @generated NOT" + NL + "\t" + NL + "\tthrow new UnsupportedOperationException(\"No user implementation provided in '";
   protected final String TEXT_126 = "' operation\"); //$NON-NLS-1$" + NL + "}";
   protected final String TEXT_127 = NL + "\t} // ";
   protected final String TEXT_128 = "\t\t" + NL + "\t}";
   protected final String TEXT_129 = "\t" + NL + "}";
   protected final String TEXT_130 = NL;
 
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
 
     stringBuffer.append(TEXT_1);
     
 String copyrightText = genDiagram.getEditorGen().getCopyrightText();
 if (copyrightText != null && copyrightText.trim().length() > 0) {
 
     stringBuffer.append(TEXT_2);
     stringBuffer.append(copyrightText.replaceAll("\n", "\n *"));
     stringBuffer.append(TEXT_3);
     }
     importManager.emitPackageStatement(stringBuffer);
     stringBuffer.append(TEXT_4);
     
 String javaExprContainer = "JavaConstraints";
 importManager.registerInnerClass(javaExprContainer);
 importManager.registerInnerClass(genDiagram.getLinkCreationConstraintsClassName());
 
     stringBuffer.append(TEXT_5);
     importManager.markImportLocation(stringBuffer);
     stringBuffer.append(TEXT_6);
     stringBuffer.append(genDiagram.getBaseItemSemanticEditPolicyClassName());
     stringBuffer.append(TEXT_7);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditorGen().getPlugin().getActivatorQualifiedClassName()));
     stringBuffer.append(TEXT_8);
     
 final GenExpressionProviderContainer expressionProviders = genDiagram.getEditorGen().getExpressionProviders();
 if (genDiagram.hasLinkCreationConstraints() && expressionProviders != null) {
 	String pluginActivatorClass = importManager.getImportedName(genDiagram.getEditorGen().getPlugin().getActivatorQualifiedClassName());
 	String importedAbstractExprCls = importManager.getImportedName(expressionProviders.getAbstractExpressionQualifiedClassName());
 	boolean hasJavaConstraints = false; 
 
     stringBuffer.append(TEXT_9);
     stringBuffer.append(genDiagram.getLinkCreationConstraintsClassName());
     stringBuffer.append(TEXT_10);
     
 	for (Iterator it = genDiagram.getLinks().iterator(); it.hasNext();) {
 		GenLinkConstraints linkConstraints = ((GenLink)it.next()).getCreationConstraints();
 		if(linkConstraints == null) continue;						
 		if(linkConstraints.getSourceEndContextClass() == null || linkConstraints.getTargetEndContextClass() == null) continue;
 		hasJavaConstraints |= (linkConstraints.getSourceEnd() != null && expressionProviders.getProvider(linkConstraints.getSourceEnd()) instanceof GenJavaExpressionProvider) ||
 							(linkConstraints.getTargetEnd() != null && expressionProviders.getProvider(linkConstraints.getTargetEnd()) instanceof GenJavaExpressionProvider);
 
     stringBuffer.append(TEXT_11);
     stringBuffer.append(genDiagram.getLinkCreationConstraintsClassName());
     stringBuffer.append(TEXT_12);
     stringBuffer.append(linkConstraints.getConstraintsInstanceFieldName());
     stringBuffer.append(TEXT_13);
     stringBuffer.append(linkConstraints.getConstraintsInstanceFieldName());
     stringBuffer.append(TEXT_14);
     
 	} // end of link iteration
 
     stringBuffer.append(TEXT_15);
     
 	final String oppositeEndVarName = "oppositeEnd";
 	for (Iterator it = genDiagram.getLinks().iterator(); it.hasNext();) {
 		GenLinkConstraints linkConstraints = ((GenLink)it.next()).getCreationConstraints();
 		if(linkConstraints == null) continue;
 		GenClass srcContext = linkConstraints.getSourceEndContextClass();
 		GenClass targetContext = linkConstraints.getTargetEndContextClass();
 		if(srcContext == null || targetContext == null) continue;
 
     stringBuffer.append(TEXT_16);
     stringBuffer.append(genDiagram.getLinkCreationConstraintsClassName());
     stringBuffer.append(TEXT_17);
     stringBuffer.append(linkConstraints.getConstraintsInstanceFieldName());
     stringBuffer.append(TEXT_18);
     			
 		String __javaOperationContainer = javaExprContainer;
 		Map __exprEnvVariables = new java.util.HashMap();
 		String __outEnvVarName = "sourceEnv";
 		GenClassifier __genExprContext = srcContext;
 		ValueExpression __genValueExpression = linkConstraints.getSourceEnd();
 		__exprEnvVariables.put(oppositeEndVarName, targetContext); //$NON-NLS-1$
 
 
     
 { /*begin the scope*/
 /*
 java.util.Map __exprEnvVariables
 String __outEnvVarName;
 */
 if(!__exprEnvVariables.isEmpty() && genDiagram.getEditorGen().getExpressionProviders() != null && 
 	genDiagram.getEditorGen().getExpressionProviders().getProvider(__genValueExpression) 
 	instanceof org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter) {	
 
     stringBuffer.append(TEXT_19);
     stringBuffer.append(importManager.getImportedName("java.util.Map"));
     stringBuffer.append(TEXT_20);
     stringBuffer.append(__outEnvVarName);
     stringBuffer.append(TEXT_21);
     stringBuffer.append(importManager.getImportedName("java.util.HashMap"));
     stringBuffer.append(TEXT_22);
     
 	for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();) {
 		String nextVariableName = (String)envVarIt.next();
 		org.eclipse.emf.codegen.ecore.genmodel.GenClassifier nextVariableType = (org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__exprEnvVariables.get(nextVariableName);
 		String varTypeEClassifierAccess = nextVariableType.getGenPackage().getQualifiedPackageInterfaceName() + ".eINSTANCE.get" + nextVariableType.getClassifierAccessorName()+"()";			
 
 
     stringBuffer.append(__outEnvVarName);
     stringBuffer.append(TEXT_23);
     stringBuffer.append(nextVariableName);
     stringBuffer.append(TEXT_24);
     stringBuffer.append(varTypeEClassifierAccess);
     stringBuffer.append(TEXT_25);
     	
 	} 
 }
 
     
 } /*end the scope*/
 
     stringBuffer.append(TEXT_26);
     stringBuffer.append(importedAbstractExprCls);
     stringBuffer.append(TEXT_27);
     
 		if(linkConstraints.getSourceEnd() != null) {
 
     
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
 
     stringBuffer.append(TEXT_28);
     
 	} else if(__genExprProvider instanceof org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter) {
 		org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter interpreter = (org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter)__genExprProvider;
 		String __expressionAccessor = interpreter.getExpressionAccessor(__genValueExpression);
 		String providerImportedClass = importManager.getImportedName(interpreter.getQualifiedClassName());
 		if(!__exprEnvVariables.isEmpty()) {			
 
     stringBuffer.append(providerImportedClass);
     stringBuffer.append(TEXT_29);
     stringBuffer.append(__expressionAccessor);
     stringBuffer.append(TEXT_30);
     stringBuffer.append(__genValueExpression.getBody());
     stringBuffer.append(TEXT_31);
     stringBuffer.append(__ctxEClassifierAccess);
     stringBuffer.append(TEXT_32);
     stringBuffer.append(__outEnvVarName);
     stringBuffer.append(TEXT_33);
     
 		} else { 		
 
     stringBuffer.append(providerImportedClass);
     stringBuffer.append(TEXT_34);
     stringBuffer.append(__expressionAccessor);
     stringBuffer.append(TEXT_35);
     stringBuffer.append(__genValueExpression.getBody());
     stringBuffer.append(TEXT_36);
     stringBuffer.append(__ctxEClassifierAccess);
     stringBuffer.append(TEXT_37);
     
 		}
 	} else if(__genExprProvider instanceof org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider) { /*inlined java expression adapter*/
 		String evalCtxQualifiedName = __genExprProvider.getQualifiedInstanceClassName(__genExprContext);
 		String __exprJavaOperName = ((org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider)__genExprProvider).getOperationName(__genValueExpression);	
 
 
     stringBuffer.append(TEXT_38);
     stringBuffer.append(__importedAbstractClass);
     stringBuffer.append(TEXT_39);
     stringBuffer.append(__ctxEClassifierAccess);
     stringBuffer.append(TEXT_40);
     stringBuffer.append(importManager.getImportedName("java.util.Map"));
     stringBuffer.append(TEXT_41);
     stringBuffer.append(importManager.getImportedName(evalCtxQualifiedName));
     stringBuffer.append(TEXT_42);
     stringBuffer.append(importManager.getImportedName(evalCtxQualifiedName));
     stringBuffer.append(TEXT_43);
     	
 		for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();) {
 			String nextVariableName = (String)envVarIt.next();
 			org.eclipse.emf.codegen.ecore.genmodel.GenClassifier nextVariableType = (org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__exprEnvVariables.get(nextVariableName);
 			String qualifiedTypeName = __genExprProvider.getQualifiedInstanceClassName(nextVariableType);
 
     stringBuffer.append(TEXT_44);
     stringBuffer.append(importManager.getImportedName(qualifiedTypeName));
     stringBuffer.append(TEXT_45);
     stringBuffer.append(nextVariableName);
     stringBuffer.append(TEXT_46);
     stringBuffer.append(importManager.getImportedName(qualifiedTypeName));
     stringBuffer.append(TEXT_47);
     stringBuffer.append(nextVariableName);
     stringBuffer.append(TEXT_48);
     		} 
     stringBuffer.append(TEXT_49);
     
 		if(__javaOperationContainer != null && __javaOperationContainer.length() > 0) { 
     stringBuffer.append(__javaOperationContainer);
     stringBuffer.append(TEXT_50);
     
 		} 
     stringBuffer.append(__exprJavaOperName);
     stringBuffer.append(TEXT_51);
     
 		for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();){
     stringBuffer.append(TEXT_52);
     stringBuffer.append((String)envVarIt.next());
     }
     stringBuffer.append(TEXT_53);
     }
     } /*end of scope*/
     
 		} else 
     stringBuffer.append(TEXT_54);
     ;
     stringBuffer.append(TEXT_55);
     
 		__outEnvVarName = "targetEnv";
 		__genExprContext = targetContext;
 		__genValueExpression = linkConstraints.getTargetEnd();			
 		__exprEnvVariables.put(oppositeEndVarName, srcContext); //$NON-NLS-1$
 
     
 { /*begin the scope*/
 /*
 java.util.Map __exprEnvVariables
 String __outEnvVarName;
 */
 if(!__exprEnvVariables.isEmpty() && genDiagram.getEditorGen().getExpressionProviders() != null && 
 	genDiagram.getEditorGen().getExpressionProviders().getProvider(__genValueExpression) 
 	instanceof org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter) {	
 
     stringBuffer.append(TEXT_56);
     stringBuffer.append(importManager.getImportedName("java.util.Map"));
     stringBuffer.append(TEXT_57);
     stringBuffer.append(__outEnvVarName);
     stringBuffer.append(TEXT_58);
     stringBuffer.append(importManager.getImportedName("java.util.HashMap"));
     stringBuffer.append(TEXT_59);
     
 	for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();) {
 		String nextVariableName = (String)envVarIt.next();
 		org.eclipse.emf.codegen.ecore.genmodel.GenClassifier nextVariableType = (org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__exprEnvVariables.get(nextVariableName);
 		String varTypeEClassifierAccess = nextVariableType.getGenPackage().getQualifiedPackageInterfaceName() + ".eINSTANCE.get" + nextVariableType.getClassifierAccessorName()+"()";			
 
 
     stringBuffer.append(__outEnvVarName);
     stringBuffer.append(TEXT_60);
     stringBuffer.append(nextVariableName);
     stringBuffer.append(TEXT_61);
     stringBuffer.append(varTypeEClassifierAccess);
     stringBuffer.append(TEXT_62);
     	
 	} 
 }
 
     
 } /*end the scope*/
 
     stringBuffer.append(TEXT_63);
     stringBuffer.append(importedAbstractExprCls);
     stringBuffer.append(TEXT_64);
     
 		if(linkConstraints.getTargetEnd() != null) {
 
     
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
 
     stringBuffer.append(TEXT_65);
     
 	} else if(__genExprProvider instanceof org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter) {
 		org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter interpreter = (org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter)__genExprProvider;
 		String __expressionAccessor = interpreter.getExpressionAccessor(__genValueExpression);
 		String providerImportedClass = importManager.getImportedName(interpreter.getQualifiedClassName());
 		if(!__exprEnvVariables.isEmpty()) {			
 
     stringBuffer.append(providerImportedClass);
     stringBuffer.append(TEXT_66);
     stringBuffer.append(__expressionAccessor);
     stringBuffer.append(TEXT_67);
     stringBuffer.append(__genValueExpression.getBody());
     stringBuffer.append(TEXT_68);
     stringBuffer.append(__ctxEClassifierAccess);
     stringBuffer.append(TEXT_69);
     stringBuffer.append(__outEnvVarName);
     stringBuffer.append(TEXT_70);
     
 		} else { 		
 
     stringBuffer.append(providerImportedClass);
     stringBuffer.append(TEXT_71);
     stringBuffer.append(__expressionAccessor);
     stringBuffer.append(TEXT_72);
     stringBuffer.append(__genValueExpression.getBody());
     stringBuffer.append(TEXT_73);
     stringBuffer.append(__ctxEClassifierAccess);
     stringBuffer.append(TEXT_74);
     
 		}
 	} else if(__genExprProvider instanceof org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider) { /*inlined java expression adapter*/
 		String evalCtxQualifiedName = __genExprProvider.getQualifiedInstanceClassName(__genExprContext);
 		String __exprJavaOperName = ((org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider)__genExprProvider).getOperationName(__genValueExpression);	
 
 
     stringBuffer.append(TEXT_75);
     stringBuffer.append(__importedAbstractClass);
     stringBuffer.append(TEXT_76);
     stringBuffer.append(__ctxEClassifierAccess);
     stringBuffer.append(TEXT_77);
     stringBuffer.append(importManager.getImportedName("java.util.Map"));
     stringBuffer.append(TEXT_78);
     stringBuffer.append(importManager.getImportedName(evalCtxQualifiedName));
     stringBuffer.append(TEXT_79);
     stringBuffer.append(importManager.getImportedName(evalCtxQualifiedName));
     stringBuffer.append(TEXT_80);
     	
 		for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();) {
 			String nextVariableName = (String)envVarIt.next();
 			org.eclipse.emf.codegen.ecore.genmodel.GenClassifier nextVariableType = (org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__exprEnvVariables.get(nextVariableName);
 			String qualifiedTypeName = __genExprProvider.getQualifiedInstanceClassName(nextVariableType);
 
     stringBuffer.append(TEXT_81);
     stringBuffer.append(importManager.getImportedName(qualifiedTypeName));
     stringBuffer.append(TEXT_82);
     stringBuffer.append(nextVariableName);
     stringBuffer.append(TEXT_83);
     stringBuffer.append(importManager.getImportedName(qualifiedTypeName));
     stringBuffer.append(TEXT_84);
     stringBuffer.append(nextVariableName);
     stringBuffer.append(TEXT_85);
     		} 
     stringBuffer.append(TEXT_86);
     
 		if(__javaOperationContainer != null && __javaOperationContainer.length() > 0) { 
     stringBuffer.append(__javaOperationContainer);
     stringBuffer.append(TEXT_87);
     
 		} 
     stringBuffer.append(__exprJavaOperName);
     stringBuffer.append(TEXT_88);
     
 		for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();){
     stringBuffer.append(TEXT_89);
     stringBuffer.append((String)envVarIt.next());
     }
     stringBuffer.append(TEXT_90);
     }
     } /*end of scope*/
     
 		} else 
     stringBuffer.append(TEXT_91);
     ;
     stringBuffer.append(TEXT_92);
     stringBuffer.append(genDiagram.getLinkCreationConstraintsClassName());
     stringBuffer.append(TEXT_93);
     
 	} // end of link iteration
 
     stringBuffer.append(TEXT_94);
     stringBuffer.append(importedAbstractExprCls);
     stringBuffer.append(TEXT_95);
     stringBuffer.append(importedAbstractExprCls);
     stringBuffer.append(TEXT_96);
     stringBuffer.append(genDiagram.getLinkCreationConstraintsClassName());
     stringBuffer.append(TEXT_97);
     stringBuffer.append(importedAbstractExprCls);
     stringBuffer.append(TEXT_98);
     stringBuffer.append(importedAbstractExprCls);
     stringBuffer.append(TEXT_99);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest"));
     stringBuffer.append(TEXT_100);
     stringBuffer.append(importedAbstractExprCls);
     stringBuffer.append(TEXT_101);
     stringBuffer.append(importedAbstractExprCls);
     stringBuffer.append(TEXT_102);
     stringBuffer.append(importedAbstractExprCls);
     stringBuffer.append(TEXT_103);
     stringBuffer.append(importManager.getImportedName("java.util.Map"));
     stringBuffer.append(TEXT_104);
     stringBuffer.append(importManager.getImportedName("java.util.Collections"));
     stringBuffer.append(TEXT_105);
     stringBuffer.append(pluginActivatorClass);
     stringBuffer.append(TEXT_106);
     
 if(hasJavaConstraints) {
 
     stringBuffer.append(TEXT_107);
     stringBuffer.append(javaExprContainer);
     stringBuffer.append(TEXT_108);
     
 	for (Iterator it = genDiagram.getLinks().iterator(); it.hasNext();) {
 		GenLinkConstraints linkConstraints = ((GenLink)it.next()).getCreationConstraints();
 		if(linkConstraints == null) continue;
 		GenClass srcContext = linkConstraints.getSourceEndContextClass();
 		GenClass targetContext = linkConstraints.getTargetEndContextClass();
 		if(srcContext == null || targetContext == null) continue;
 		String __genExprResultType = "java.lang.Boolean";
 		Map __exprEnvVariables = new java.util.HashMap();
 		GenClassifier __genExprContext = srcContext;
 		ValueExpression __genValueExpression = linkConstraints.getSourceEnd();
 		if(expressionProviders.getProvider(__genValueExpression) instanceof GenJavaExpressionProvider) {				
 			__exprEnvVariables.put(oppositeEndVarName, targetContext);
 
     stringBuffer.append(TEXT_109);
     
 /* 
 ValueExpression __genValueExpression
 java.util.Map __exprEnvVariables
 GenClassifier __genExprContext
 GenClassifier || String/qualifiedClassName/__genExprResultType
 */
 org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderBase __genExprProvider = (genDiagram.getEditorGen().getExpressionProviders() != null) ? genDiagram.getEditorGen().getExpressionProviders().getProvider(__genValueExpression) : null;
 if(__genExprProvider instanceof org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider) {
 	String evalCtxQualifiedName = __genExprProvider.getQualifiedInstanceClassName(__genExprContext);
 	// support GenClassifier and also String based qualified java class name
 	Object __genExprResultTypeObj = __genExprResultType;
 	String __exprResultTypeQualifiedName = null;
 	if(__genExprResultTypeObj instanceof String) 
 		__exprResultTypeQualifiedName = (String)__genExprResultTypeObj;
 	else if(__genExprResultTypeObj instanceof org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)
 		__exprResultTypeQualifiedName = __genExprProvider.getQualifiedInstanceClassName((org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__genExprResultTypeObj);
 	String __exprJavaOperName = ((org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider)__genExprProvider).getOperationName(__genValueExpression);
 
     stringBuffer.append(TEXT_110);
     stringBuffer.append(importManager.getImportedName(__exprResultTypeQualifiedName));
     stringBuffer.append(TEXT_111);
     stringBuffer.append(__exprJavaOperName);
     stringBuffer.append(TEXT_112);
     stringBuffer.append(importManager.getImportedName(evalCtxQualifiedName));
     stringBuffer.append(TEXT_113);
     
 	for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();) {
 		String __nextVarName = (String)envVarIt.next();
 		org.eclipse.emf.codegen.ecore.genmodel.GenClassifier nextVariableType = (org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__exprEnvVariables.get(__nextVarName);
 		String qualifiedTypeName = __genExprProvider.getQualifiedInstanceClassName(nextVariableType);
 
 	
     stringBuffer.append(TEXT_114);
     stringBuffer.append(importManager.getImportedName(qualifiedTypeName));
     stringBuffer.append(TEXT_115);
     stringBuffer.append(__nextVarName);
     	} 
 
     stringBuffer.append(TEXT_116);
     stringBuffer.append(__exprJavaOperName);
     stringBuffer.append(TEXT_117);
     
 }
 
     
 		}
 		__genValueExpression = linkConstraints.getTargetEnd();
 		if(expressionProviders.getProvider(__genValueExpression) instanceof GenJavaExpressionProvider) {		
 			__genExprContext = targetContext;
 			__exprEnvVariables.put(oppositeEndVarName, srcContext);
 
     stringBuffer.append(TEXT_118);
     
 /* 
 ValueExpression __genValueExpression
 java.util.Map __exprEnvVariables
 GenClassifier __genExprContext
 GenClassifier || String/qualifiedClassName/__genExprResultType
 */
 org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderBase __genExprProvider = (genDiagram.getEditorGen().getExpressionProviders() != null) ? genDiagram.getEditorGen().getExpressionProviders().getProvider(__genValueExpression) : null;
 if(__genExprProvider instanceof org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider) {
 	String evalCtxQualifiedName = __genExprProvider.getQualifiedInstanceClassName(__genExprContext);
 	// support GenClassifier and also String based qualified java class name
 	Object __genExprResultTypeObj = __genExprResultType;
 	String __exprResultTypeQualifiedName = null;
 	if(__genExprResultTypeObj instanceof String) 
 		__exprResultTypeQualifiedName = (String)__genExprResultTypeObj;
 	else if(__genExprResultTypeObj instanceof org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)
 		__exprResultTypeQualifiedName = __genExprProvider.getQualifiedInstanceClassName((org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__genExprResultTypeObj);
 	String __exprJavaOperName = ((org.eclipse.gmf.codegen.gmfgen.GenJavaExpressionProvider)__genExprProvider).getOperationName(__genValueExpression);
 
     stringBuffer.append(TEXT_119);
     stringBuffer.append(importManager.getImportedName(__exprResultTypeQualifiedName));
     stringBuffer.append(TEXT_120);
     stringBuffer.append(__exprJavaOperName);
     stringBuffer.append(TEXT_121);
     stringBuffer.append(importManager.getImportedName(evalCtxQualifiedName));
     stringBuffer.append(TEXT_122);
     
 	for(java.util.Iterator envVarIt = __exprEnvVariables.keySet().iterator(); envVarIt.hasNext();) {
 		String __nextVarName = (String)envVarIt.next();
 		org.eclipse.emf.codegen.ecore.genmodel.GenClassifier nextVariableType = (org.eclipse.emf.codegen.ecore.genmodel.GenClassifier)__exprEnvVariables.get(__nextVarName);
 		String qualifiedTypeName = __genExprProvider.getQualifiedInstanceClassName(nextVariableType);
 
 	
     stringBuffer.append(TEXT_123);
     stringBuffer.append(importManager.getImportedName(qualifiedTypeName));
     stringBuffer.append(TEXT_124);
     stringBuffer.append(__nextVarName);
     	} 
 
     stringBuffer.append(TEXT_125);
     stringBuffer.append(__exprJavaOperName);
     stringBuffer.append(TEXT_126);
     
 }
 
     
 		}
 	} /*java constraints iteration*/
 
     stringBuffer.append(TEXT_127);
     stringBuffer.append(javaExprContainer);
     
 } /* end of hasJavaConstraints */
 
     stringBuffer.append(TEXT_128);
     } /*end of hasLinkCreationConstraints()*/ 
     stringBuffer.append(TEXT_129);
     importManager.emitSortedImports();
     stringBuffer.append(TEXT_130);
     return stringBuffer.toString();
   }
 }
