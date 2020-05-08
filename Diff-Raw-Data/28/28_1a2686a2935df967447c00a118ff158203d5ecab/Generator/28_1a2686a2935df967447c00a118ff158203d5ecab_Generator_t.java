 /*
  * Copyright (c) 2005, 2010 Borland Software Corporation and others
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.codegen.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.codegen.util.CodeGenUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.gmf.codegen.gmfgen.Behaviour;
 import org.eclipse.gmf.codegen.gmfgen.CustomParser;
 import org.eclipse.gmf.codegen.gmfgen.ElementType;
 import org.eclipse.gmf.codegen.gmfgen.ExpressionLabelParser;
 import org.eclipse.gmf.codegen.gmfgen.ExternalParser;
 import org.eclipse.gmf.codegen.gmfgen.FeatureLinkModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenFactory;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenAction;
 import org.eclipse.gmf.codegen.gmfgen.GenApplication;
 import org.eclipse.gmf.codegen.gmfgen.GenChildContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenChildLabelNode;
 import org.eclipse.gmf.codegen.gmfgen.GenChildNode;
 import org.eclipse.gmf.codegen.gmfgen.GenCommonBase;
 import org.eclipse.gmf.codegen.gmfgen.GenCompartment;
 import org.eclipse.gmf.codegen.gmfgen.GenContributionItem;
 import org.eclipse.gmf.codegen.gmfgen.GenContributionManager;
 import org.eclipse.gmf.codegen.gmfgen.GenCustomAction;
 import org.eclipse.gmf.codegen.gmfgen.GenCustomGeneratorExtension;
 import org.eclipse.gmf.codegen.gmfgen.GenCustomPreferencePage;
 import org.eclipse.gmf.codegen.gmfgen.GenCustomPropertyTab;
 import org.eclipse.gmf.codegen.gmfgen.GenCustomTemplateInput;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorView;
 import org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter;
 import org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderBase;
 import org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenExternalNodeLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenFixedInputsTemplateInvocation;
 import org.eclipse.gmf.codegen.gmfgen.GenLanguage;
 import org.eclipse.gmf.codegen.gmfgen.GenLink;
 import org.eclipse.gmf.codegen.gmfgen.GenLinkLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenNavigatorChildReference;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.codegen.gmfgen.GenNodeLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenParserImplementation;
 import org.eclipse.gmf.codegen.gmfgen.GenPreferencePage;
 import org.eclipse.gmf.codegen.gmfgen.GenPropertyTab;
 import org.eclipse.gmf.codegen.gmfgen.GenSharedContributionItem;
 import org.eclipse.gmf.codegen.gmfgen.GenStandardPreferencePage;
 import org.eclipse.gmf.codegen.gmfgen.GenTemplateInvocation;
 import org.eclipse.gmf.codegen.gmfgen.GenTemplateInvocationBase;
 import org.eclipse.gmf.codegen.gmfgen.GenTopLevelNode;
 import org.eclipse.gmf.codegen.gmfgen.GenVisualEffect;
 import org.eclipse.gmf.codegen.gmfgen.InitDiagramAction;
 import org.eclipse.gmf.codegen.gmfgen.MetamodelType;
 import org.eclipse.gmf.codegen.gmfgen.OpenDiagramBehaviour;
import org.eclipse.gmf.codegen.gmfgen.PredefinedEnumParser;
 import org.eclipse.gmf.codegen.gmfgen.PredefinedParser;
 import org.eclipse.gmf.codegen.gmfgen.SpecializationType;
 import org.eclipse.gmf.codegen.gmfgen.StandardPreferencePages;
 import org.eclipse.gmf.codegen.gmfgen.TypeLinkModelFacet;
 import org.eclipse.gmf.common.UnexpectedBehaviourException;
 import org.eclipse.gmf.internal.common.codegen.GeneratorBase;
 import org.eclipse.gmf.internal.common.codegen.ImportUtil;
 import org.eclipse.gmf.internal.common.codegen.TextEmitter;
 import org.eclipse.gmf.internal.common.codegen.TextMerger;
 import org.eclipse.ocl.ecore.OCL;
 import org.eclipse.ocl.ecore.OCL.Helper;
 import org.eclipse.ocl.ecore.OCLExpression;
 import org.eclipse.ocl.options.ParsingOptions;
 
 /**
  * Invokes templates to populate diagram editor project.
  * 
  * @author artem
  */
 public class Generator extends GeneratorBase implements Runnable {
 
 	private final GenEditorGenerator myEditorGen;
 
 	private final GenDiagram myDiagram;
 
 	private final CodegenEmitters myEmitters;
 
 	public Generator(GenEditorGenerator genModel, CodegenEmitters emitters) {
 		assert genModel != null && emitters != null;
 		myEditorGen = genModel;
 		myDiagram = genModel.getDiagram();
 		myEmitters = emitters;
 	}
 
 	@Override
 	protected TextMerger createMergeService() {
 		TextMerger service = myEmitters.createMergeService();
 		if (service != null) {
 			return service;
 		}
 		return super.createMergeService();
 	}
 
 	protected void customRun() throws InterruptedException, UnexpectedBehaviourException {
 		final Path pluginDirectory = new Path(myEditorGen.getPluginDirectory());
 		initializeEditorProject(pluginDirectory, guessProjectLocation(pluginDirectory.segment(0)), Collections.<IProject> emptyList());
 
 		if (myEditorGen.getModelAccess() != null) {
 			myEmitters.setGlobals(Collections.<String, Object> singletonMap("DynamicModelAccess", myEditorGen.getModelAccess()));
 			generateModelAccessFacility();
 		}
 
 		// draft for messages
 		generateExternalizationSupport();
 
 		// commands
 		generateReorientLinkViewCommand();
 
 		// edit helpers
 		generateBaseEditHelper();
 
 		// parsers
 		generateParsers();
 
 		//
 		// Nodes
 		for (GenTopLevelNode node : myDiagram.getTopLevelNodes()) {
 			generateNode(node);
 		}
 		for (GenChildNode node : myDiagram.getChildNodes()) {
 			if (node instanceof GenChildLabelNode) {
 				generateChildLabelNode((GenChildLabelNode) node);
 			} else {
 				generateNode(node);
 			}
 		}
 		//
 		// Compartments
 		for (GenCompartment compartment : myDiagram.getCompartments()) {
 			generateCompartmentEditPart(compartment);
 			generateCompartmentItemSemanticEditPolicy(compartment);
 			if (compartment.needsCanonicalEditPolicy()) {
 				generateChildContainerCanonicalEditPolicy(compartment);
 			}
 		}
 		//
 		// Links
 		for (GenLink next : myDiagram.getLinks()) {
 			generateEditSupport(next);
 			generateLinkEditPart(next);
 			generateBehaviours(next);
 			generateLinkItemSemanticEditPolicy(next);
 			if (next.getModelFacet() != null && (next.isIncomingCreationAllowed() || next.isOutgoingCreationAllowed())) {
 				generateCreateLinkCommand(next);
 			}
 			if (next.getModelFacet() instanceof TypeLinkModelFacet) {
 				if (next.isTargetReorientingAllowed() || next.isSourceReorientingAllowed()) {
 					generateReorientLinkCommand(next);
 				}
 			} else if (next.getModelFacet() instanceof FeatureLinkModelFacet) {
 				if (next.isTargetReorientingAllowed() || next.isSourceReorientingAllowed()) {
 					generateReorientRefLinkCommand(next);
 				}
 			}
 			for (GenLinkLabel label : next.getLabels()) {
 				generateLinkLabelEditPart(label);
 			}
 			generateVisualEffectEditPolicies(next);
 		}
 		generateDiagram();
 		//
 		// common edit parts, edit policies and providers
 		generateBaseItemSemanticEditPolicy();
 		generateTextSelectionEditPolicy();
 		generateTextNonResizableEditPolicy();
 		generateEditPartFactory();
 		generateElementInitializers();
 		generateElementTypes();
 		generateViewProvider();
 		generateEditPartProvider();
 		generateModelingAssistantProvider();
 		generateIconProvider();
 		generateParserProvider();
 		if (myDiagram.isValidationEnabled() || myEditorGen.hasAudits()) {
 			generateValidationProvider();
 			generateValidateAction();
 			if (myEditorGen.getApplication() == null) {
 				generateMarkerNavigationProvider();
 			} else {
 				generateValidationMarker();
 			}
 			if (myDiagram.isValidationDecorators()) {
 				generateValidationDecoratorProvider();
 			}
 		}
 		if (myEditorGen.getMetrics() != null) {
 			generateMetricProvider();
 		}
 		if (myEditorGen.getExpressionProviders() != null) {
 			generateExpressionProviders();
 		}
 
 		// preferences
 		generatePreferenceInitializer();
 		generatePreferencePages(myDiagram.getPreferencePages());
 
 		// editor
 		generatePalette();
 		generateDiagramEditorUtil();
 		generateVisualIDRegistry();
 		generateCreationWizard();
 		generateCreationWizardPage();
 		generateDeleteElementAction();
 		generateDiagramEditorContextMenuProvider();
 		generateEditor();
 		generateActionBarContributor();
 		generateMatchingStrategy();
 		generateDocumentProvider();
 		if (myDiagram.generateInitDiagramAction() || myDiagram.generateCreateShortcutAction() /*FIXME use another condition here*/) {
 			generateModelElementSelectionPage();
 		}
 		if (myDiagram.generateInitDiagramAction() /*FIXME use another condition here*/) {
 			// FIXME HACK!!! until I decide how to contribute action against IFile
 			InitDiagramAction fakeAction = GMFGenFactory.eINSTANCE.createInitDiagramAction();
 			fakeAction.setQualifiedClassName(myDiagram.getInitDiagramFileActionQualifiedClassName());
 			doGenerateJavaClass(myEmitters.getPredefinedActionEmitter(), fakeAction.getQualifiedClassName(), fakeAction, myEditorGen);
 			generateNewDiagramFileWizard();
 		}
 		if (myDiagram.generateCreateShortcutAction() /*FIXME use another condition here*/) {
 			generateCreateShortcutDecorationsCommand();
 			if (myEditorGen.getApplication() == null) {
 				generateElementChooser();
 			} else {
 				generateShortcutCreationWizard();
 			}
 		}
 		//
 		// Updater
 		generateDiagramUpdater();
 		generateUpdateCommand();
 		generateNodeDescriptor();
 		generateLinkDescriptor();
 		//
 		// Navigator
 		if (myEditorGen.getNavigator() != null) {
 			generateNavigatorContentProvider();
 			generateNavigatorLabelProvider();
 			generateNavigatorLinkHelper();
 			generateNavigatorSorter();
 			generateNavigatorActionProvider();
 			generateAbstractNavigatorItem();
 			generateNavigatorGroup();
 			generateNavigatorItem();
 			generateNavigatorGroupIcons();
 			if (myEditorGen.getDomainGenModel() != null && myEditorGen.getNavigator().isGenerateDomainModelNavigator()) {
 				generateDomainNavigatorContentProvider();
 				generateDomainNavigatorLabelProvider();
 				generateDomainNavigatorItem();
 				generateURIEditorInputTester();
 			}
 		}
 		if (myEditorGen.getPropertySheet() != null) {
 			generatePropertySheetSections();
 		}
 		if (myDiagram.generateShortcutIcon()) {
 			generateShortcutIcon();
 			generateShortcutsDecoratorProvider();
 			generateShortcutPropertyTester();
 		}
 		if (isPathInsideGenerationTarget(myDiagram.getCreationWizardIconPathX())) {
 			// at the moment this may produce path that reference generated icon file, thus
 			// skip generation if the path is relative
 			generateDiagramIcon(myDiagram.getCreationWizardIconPathX());
 		}
 		if (isPathInsideGenerationTarget(myEditorGen.getEditor().getIconPathX())) {
 			// at the moment this may produce path that reference generated icon file, thus
 			// skip generation if the path is relative
 			generateDiagramIcon(myEditorGen.getEditor().getIconPathX());
 		}
 		generateWizardBanner();
 		generatePlugin();
 		generateApplication();
 		generateActions();
 		generateCustomExtensions();
 	}
 
 	private static boolean isPathInsideGenerationTarget(String path) {
 		assert path != null;
 		Path p = new Path(path);
 		return !p.isAbsolute() && !p.segment(0).equals(".."); //$NON-NLS-1$
 	}
 
 	// Diagram itself as a diagram element - editpart, editpolicies
 	private void generateDiagram() throws UnexpectedBehaviourException, InterruptedException {
 		generateBehaviours(myDiagram);
 		if (myDiagram.needsCanonicalEditPolicy()) {
 			generateDiagramCanonicalEditPolicy();
 		}
 		generateDiagramItemSemanticEditPolicy();
 		generateEditSupport(myDiagram);
 		generateDiagramEditPart();
 	}
 
 	private void generateNode(GenNode node) throws UnexpectedBehaviourException, InterruptedException {
 		generateNodeItemSemanticEditPolicy(node);
 		if (node.getModelFacet() != null) {
 			generateCreateNodeCommand(node);
 		}
 		generateEditSupport(node);
 		generateNodeEditPart(node);
 		generateBehaviours(node);
 		if (node.needsCanonicalEditPolicy()) {
 			generateChildContainerCanonicalEditPolicy(node);
 		}
 		if (needsGraphicalNodeEditPolicy(node)) {
 			generateGraphicalNodeEditPolicy(node);
 		}
 		for (GenNodeLabel label : node.getLabels()) {
 			if (label instanceof GenExternalNodeLabel) {
 				GenExternalNodeLabel extLabel = (GenExternalNodeLabel) label;
 				generateExternalNodeLabelEditPart(extLabel);
 			} else {
 				generateNodeLabelEditPart(label);
 			}
 		}
 		generateVisualEffectEditPolicies(node);
 	}
 
 	private void generateVisualEffectEditPolicies(GenCommonBase commonBase) throws InterruptedException {
 		for (Behaviour behaviour : commonBase.getBehaviour()) {
 			if (behaviour instanceof GenVisualEffect) {
 				GenVisualEffect visualEffect = (GenVisualEffect) behaviour;
 				generateVisualEffectEditPolicy(visualEffect);
 			}
 		}
 	}
 
 	private void generateVisualEffectEditPolicy(GenVisualEffect visualEffect) throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getVisualEffectEditPolicyEmitter(), visualEffect.getEditPolicyQualifiedClassName(), visualEffect);
 	}
 
 	private void generateChildLabelNode(GenChildLabelNode child) throws UnexpectedBehaviourException, InterruptedException {
 		generateNodeItemSemanticEditPolicy(child);
 		if (child.getModelFacet() != null) {
 			generateCreateNodeCommand(child);
 		}
 		generateEditSupport(child);
 		generateBehaviours(child);
 		generateChildNodeLabelEditPart(child);
 	}
 
 	// commands
 
 	private void generateReorientLinkViewCommand() throws UnexpectedBehaviourException, InterruptedException {
 		for (GenNode n : myDiagram.getAllNodes()) {
 			if (needsGraphicalNodeEditPolicy(n)) {
 				// ReorientConnectionViewCommand is neccessary only when there's GraphicalNodeEditPolicy
 				// XXX consider using some general modeling facility for reused code like that one (there's a bug for this)
 				doGenerateJavaClass(myEmitters.getReorientLinkViewCommandEmitter(), myDiagram.getReorientConnectionViewCommandQualifiedClassName(), myDiagram);
 				break;
 			}
 		}
 	}
 
 	private void generateCreateNodeCommand(GenNode node) throws InterruptedException, UnexpectedBehaviourException {
 		doGenerateJavaClass(myEmitters.getCreateNodeCommandEmitter(), node.getCreateCommandQualifiedClassName(), node);
 	}
 
 	private void generateCreateLinkCommand(GenLink link) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getCreateLinkCommandEmitter(), link.getCreateCommandQualifiedClassName(), link);
 	}
 
 	private void generateReorientLinkCommand(GenLink link) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getReorientLinkCommandEmitter(), link.getReorientCommandQualifiedClassName(), link);
 	}
 
 	private void generateReorientRefLinkCommand(GenLink link) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getReorientRefLinkCommandEmitter(), link.getReorientCommandQualifiedClassName(), link);
 	}
 
 	private void generateCreateShortcutDecorationsCommand() throws InterruptedException, UnexpectedBehaviourException {
 		doGenerateJavaClass(myEmitters.getCreateShortcutDecorationsCommandEmitter(), myDiagram.getCreateShortcutDecorationsCommandQualifiedClassName(), myDiagram);
 	}
 
 	// helpers
 
 	private void generateBaseEditHelper() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getBaseEditHelperEmitter(), myDiagram.getBaseEditHelperQualifiedClassName(), myDiagram);
 	}
 
 	private void generateEditSupport(GenCommonBase diagramElement) throws UnexpectedBehaviourException, InterruptedException {
 		ElementType genType = diagramElement.getElementType();
 		if (genType.isDefinedExternally()) {
 			return;
 		}
 		if (genType instanceof SpecializationType) {
 			generateEditHelperAdvice((SpecializationType) genType);
 		} else if (genType instanceof MetamodelType) {
 			generateEditHelper((MetamodelType) genType);
 		}
 	}
 
 	private void generateEditHelper(MetamodelType genType) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getEditHelperEmitter(), genType.getEditHelperQualifiedClassName(), genType);
 	}
 
 	private void generateEditHelperAdvice(SpecializationType genType) throws UnexpectedBehaviourException, InterruptedException {
 		if (!genType.eIsSet(GMFGenPackage.eINSTANCE.getSpecializationType_EditHelperAdviceClassName())) {
 			return;
 		}
 		doGenerateJavaClass(myEmitters.getEditHelperAdviceEmitter(), genType.getEditHelperAdviceQualifiedClassName(), genType);
 	}
 
 	// parts
 
 	private void generateDiagramEditPart() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getDiagramEditPartEmitter(), myDiagram.getEditPartQualifiedClassName(), myDiagram);
 	}
 
 	private void generateNodeEditPart(GenNode node) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getNodeEditPartEmitter(), node.getEditPartQualifiedClassName(), node);
 	}
 
 	private void generateNodeLabelEditPart(GenNodeLabel label) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getNodeLabelEditPartEmitter(), label.getEditPartQualifiedClassName(), label);
 	}
 
 	private void generateExternalNodeLabelEditPart(GenExternalNodeLabel label) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getExternalNodeLabelEditPartEmitter(), label.getEditPartQualifiedClassName(), label);
 	}
 
 	private void generateChildNodeLabelEditPart(GenChildLabelNode node) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getChildNodeLabelEditPartEmitter(), node.getEditPartQualifiedClassName(), node);
 	}
 
 	private void generateCompartmentEditPart(GenCompartment compartment) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getCompartmentEditPartEmitter(), compartment.getEditPartQualifiedClassName(), compartment);
 	}
 
 	private void generateLinkEditPart(GenLink link) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getLinkEditPartEmitter(), link.getEditPartQualifiedClassName(), link);
 	}
 
 	private void generateLinkLabelEditPart(GenLinkLabel label) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getLinkLabelEditPartEmitter(), label.getEditPartQualifiedClassName(), label);
 	}
 
 	private void generateEditPartFactory() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getEditPartFactoryEmitter(), myDiagram.getEditPartFactoryQualifiedClassName(), myDiagram);
 	}
 
 	// edit policies
 
 	private void generateBaseItemSemanticEditPolicy() throws InterruptedException {
 		Collection<GenCommonBase> allSemanticElements = new ArrayList<GenCommonBase>(myDiagram.getAllContainers());
 		allSemanticElements.addAll(myDiagram.getLinks());
 		boolean isSansDomainModel = true;
 		for (Iterator<GenCommonBase> it = allSemanticElements.iterator(); it.hasNext() && isSansDomainModel;) {
 			GenCommonBase nextCommonBase = it.next();
 			if (!nextCommonBase.isSansDomain()) {
 				isSansDomainModel = false;
 			}
 		}
 		if (isSansDomainModel) {
 			return;
 		}
 		doGenerateJavaClass(myEmitters.getBaseItemSemanticEditPolicyEmitter(), myDiagram.getBaseItemSemanticEditPolicyQualifiedClassName(), myDiagram);
 	}
 
 	/**
 	 * Generate classes for behaviours specified for the diagram element. 
 	 * As part of its job, this method tries not to generate shared policies more than once.
 	 */
 	private void generateBehaviours(GenCommonBase commonBase) throws UnexpectedBehaviourException, InterruptedException {
 		for (OpenDiagramBehaviour behaviour : commonBase.getBehaviour(OpenDiagramBehaviour.class)) {
 			if (behaviour.getSubject() == commonBase) { // extravagant way to check whether this behaviour is shared or not
 				generateOpenDiagramEditPolicy(behaviour);
 			}
 		}
 	}
 
 	private void generateOpenDiagramEditPolicy(OpenDiagramBehaviour behaviour) throws UnexpectedBehaviourException, InterruptedException {
 		internalGenerateJavaClass(myEmitters.getOpenDiagramEditPolicyEmitter(), behaviour.getEditPolicyQualifiedClassName(), behaviour);
 	}
 
 	private void generateDiagramCanonicalEditPolicy() throws InterruptedException {
 		internalGenerateJavaClass(myEmitters.getDiagramCanonicalEditPolicyEmitter(), myDiagram.getCanonicalEditPolicyQualifiedClassName(), myDiagram);
 	}
 
 	private void generateChildContainerCanonicalEditPolicy(GenChildContainer genContainer) throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getChildContainerCanonicalEditPolicyEmitter(), genContainer.getCanonicalEditPolicyQualifiedClassName(), genContainer);
 	}
 
 	private void generateDiagramItemSemanticEditPolicy() throws InterruptedException {
 		if (myDiagram.isSansDomain()) {
 			return;
 		}
 		doGenerateJavaClass(myEmitters.getDiagramItemSemanticEditPolicyEmitter(), myDiagram.getItemSemanticEditPolicyQualifiedClassName(), myDiagram);
 	}
 
 	private void generateCompartmentItemSemanticEditPolicy(GenCompartment genCompartment) throws InterruptedException {
 		if (genCompartment.isSansDomain()) {
 			return;
 		}
 		doGenerateJavaClass(myEmitters.getCompartmentItemSemanticEditPolicyEmitter(), genCompartment.getItemSemanticEditPolicyQualifiedClassName(), genCompartment);
 	}
 
 	private void generateGraphicalNodeEditPolicy(GenNode genNode) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getGraphicalNodeEditPolicyEmitter(), genNode.getGraphicalNodeEditPolicyQualifiedClassName(), genNode);
 	}
 
 	private void generateNodeItemSemanticEditPolicy(GenNode genNode) throws InterruptedException {
 		if (genNode.isSansDomain()) {
 			return;
 		}
 		doGenerateJavaClass(myEmitters.getNodeItemSemanticEditPolicyEmitter(), genNode.getItemSemanticEditPolicyQualifiedClassName(), genNode);
 	}
 
 	private void generateLinkItemSemanticEditPolicy(GenLink genLink) throws InterruptedException {
 		if (genLink.isSansDomain()) {
 			return;
 		}
 		doGenerateJavaClass(myEmitters.getLinkItemSemanticEditPolicyEmitter(), genLink.getItemSemanticEditPolicyQualifiedClassName(), genLink);
 	}
 
 	private void generateTextSelectionEditPolicy() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getTextSelectionEditPolicyEmitter(), myDiagram.getTextSelectionEditPolicyQualifiedClassName(), myDiagram);
 	}
 
 	private void generateTextNonResizableEditPolicy() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getTextNonResizableEditPolicyEmitter(), myDiagram.getTextNonResizableEditPolicyQualifiedClassName(), myDiagram);
 	}
 
 	// preferences
 
 	private void generatePreferenceInitializer() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getPreferenceInitializerEmitter(), myEmitters.getPreferenceInitializerName(myDiagram), myDiagram);
 	}
 
 	private void generatePreferencePages(List<GenPreferencePage> pages) throws UnexpectedBehaviourException, InterruptedException {
 		for (GenPreferencePage p : pages) {
 			if (p instanceof GenCustomPreferencePage) {
 				if (((GenCustomPreferencePage) p).isGenerateBoilerplate()) {
 					doGenerateJavaClass(myEmitters.getCustomPreferencePageEmitter(), p.getQualifiedClassName(), p);
 				}
 			} else if (p instanceof GenStandardPreferencePage) {
 				if (((GenStandardPreferencePage) p).getKind() != StandardPreferencePages.PATHMAPS_LITERAL) {
 					doGenerateJavaClass(myEmitters.getStandardPreferencePageEmitter(), p.getQualifiedClassName(), p);
 				}
 			} else {
 				throw new UnexpectedBehaviourException("No idea how to handle GenPreferencePage subclass:" + p);
 			}
 			generatePreferencePages(p.getChildren());
 		}
 	}
 
 	private void generateParsers() throws UnexpectedBehaviourException, InterruptedException {
 		if (myEditorGen.getLabelParsers() == null) {
 			return;
 		}
		boolean needsAbstractParser = false;
 		for (GenParserImplementation pi : myEditorGen.getLabelParsers().getImplementations()) {
 			if (pi instanceof PredefinedParser) {
				needsAbstractParser = true;
 				doGenerateJavaClass(myEmitters.getPredefinedParserEmitter(), ((PredefinedParser) pi).getQualifiedClassName(), pi);
			} else if (pi instanceof PredefinedEnumParser) {
				needsAbstractParser = true;
 			} else if (pi instanceof CustomParser && ((CustomParser) pi).isGenerateBoilerplate()) {
 				doGenerateJavaClass(myEmitters.getCustomParserEmitter(), ((CustomParser) pi).getQualifiedName(), pi);
 			} else if (pi instanceof ExpressionLabelParser) {
 				doGenerateJavaClass(myEmitters.getExpressionLabelParserEmitter(), ((ExpressionLabelParser) pi).getQualifiedClassName(), pi);
 			}
 		}
		if (needsAbstractParser) {
			doGenerateJavaClass(myEmitters.getAbstractParserEmitter(), myEmitters.getAbstractParserName(myEditorGen.getLabelParsers()), myEditorGen.getLabelParsers());
		}
 	}
 
 	// providers
 
 	private void generateParserProvider() throws UnexpectedBehaviourException, InterruptedException {
 		if (myEditorGen.getLabelParsers() != null && (myEditorGen.getLabelParsers().isExtensibleViaService() || existsNonExternalParser())) {
 			doGenerateJavaClass(myEmitters.getParserProviderEmitter(), myEditorGen.getLabelParsers().getQualifiedClassName(), myEditorGen.getLabelParsers());
 		}
 	}
 
 	// if there's no other parser than external, and provider is not contributed as a Service - 
 	// no need to generate class (only get() method would be there)
 	// XXX although adopters might want to change the logic - what if they generate smth reasonable?
 	// or if I add sort of getDescriptionParser common access method there?
 	private boolean existsNonExternalParser() {
 		for (GenParserImplementation pi : myEditorGen.getLabelParsers().getImplementations()) {
 			if (false == pi instanceof ExternalParser) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void generateElementInitializers() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getElementInitializersEmitter(), myDiagram.getElementInitializersPackageName() + '.' + myDiagram.getElementInitializersClassName(), myDiagram);
 	}
 
 	private void generateElementTypes() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getElementTypesEmitter(), myDiagram.getElementTypesQualifiedClassName(), myDiagram);
 	}
 
 	private void generateViewProvider() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getViewProviderEmitter(), myDiagram.getNotationViewProviderQualifiedClassName(), myDiagram);
 	}
 
 	private void generateEditPartProvider() throws UnexpectedBehaviourException, InterruptedException {
 		internalGenerateJavaClass(myEmitters.getEditPartProviderEmitter(), myDiagram.getProvidersPackageName(), myDiagram.getEditPartProviderClassName(), myDiagram);
 	}
 
 	private void generateModelingAssistantProvider() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getModelingAssistantProviderEmitter(), myDiagram.getModelingAssistantProviderQualifiedClassName(), myDiagram);
 	}
 
 	private void generateIconProvider() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getIconProviderEmitter(), myDiagram.getIconProviderQualifiedClassName(), myDiagram);
 	}
 
 	private void generateValidationProvider() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getValidationProviderEmitter(), myDiagram.getValidationProviderQualifiedClassName(), myDiagram);
 	}
 
 	private void generateValidationDecoratorProvider() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getValidationDecoratorProviderEmitter(), myDiagram.getValidationDecoratorProviderQualifiedClassName(), myDiagram);
 	}
 
 	private void generateShortcutsDecoratorProvider() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getShortcutsDecoratorProviderEmitter(), myDiagram.getShortcutsDecoratorProviderQualifiedClassName(), myDiagram);
 	}
 
 	private void generateShortcutPropertyTester() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getShortcutPropertyTesterEmitter(), myDiagram.getShortcutPropertyTesterQualifiedClassName(), myDiagram);
 	}
 
 	private void generateMetricProvider() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getMetricProviderEmitter(), myDiagram.getMetricProviderQualifiedClassName(), myDiagram);
 	}
 
 	private void generateMarkerNavigationProvider() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getMarkerNavigationProviderEmitter(), myDiagram.getMarkerNavigationProviderQualifiedClassName(), myDiagram);
 	}
 
 	// editor
 
 	private void generateValidateAction() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getValidateActionEmitter(), myEmitters.getValidateActionName(myDiagram), myDiagram);
 	}
 
 	private void generateValidationMarker() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getValidationMarkerEmitter(), myEmitters.getValidationMarkerName(myDiagram), myDiagram);
 	}
 
 	private void generateModelElementSelectionPage() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getModelElementSelectionPageEmitter(), myEmitters.getModelElementSelectionPageName(myDiagram), myDiagram);
 	}
 
 	private void generateNewDiagramFileWizard() throws UnexpectedBehaviourException, InterruptedException {
 		if (!myDiagram.isSynchronized()) {
 			doGenerateJavaClass(myEmitters.getDiagramContentInitializerEmitter(), myDiagram.getDiagramContentInitializerQualifiedClassName(), myDiagram);
 		}
 		doGenerateJavaClass(myEmitters.getNewDiagramFileWizardEmitter(), myEmitters.getNewDiagramFileWizardName(myDiagram), myDiagram);
 	}
 
 	private void generatePalette() throws UnexpectedBehaviourException, InterruptedException {
 		if (myDiagram.getPalette() == null) {
 			return;
 		}
 		doGenerateJavaClass(myEmitters.getPaletteEmitter(), myDiagram.getPalette().getFactoryQualifiedClassName(), myDiagram.getPalette());
 	}
 
 	private void generateDiagramEditorUtil() throws UnexpectedBehaviourException, InterruptedException {
 		internalGenerateJavaClass(myEmitters.getDiagramEditorUtilEmitter(), myEditorGen.getEditor().getPackageName(), myDiagram.getDiagramEditorUtilClassName(), myDiagram);
 	}
 
 	private void generateVisualIDRegistry() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getVisualIDRegistryEmitter(), myDiagram.getVisualIDRegistryQualifiedClassName(), myDiagram);
 	}
 
 	private void generateCreationWizard() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getCreationWizardEmitter(), myDiagram.getCreationWizardQualifiedClassName(), myDiagram);
 	}
 
 	private void generateCreationWizardPage() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getCreationWizardPageEmitter(), myDiagram.getCreationWizardPageQualifiedClassName(), myDiagram);
 	}
 
 	private void generateDeleteElementAction() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getDeleteElementActionEmitter(), myEmitters.getDeleteElementActionName(myDiagram), myDiagram);
 	}
 
 	private void generateDiagramEditorContextMenuProvider() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getDiagramEditorContextMenuProviderEmitter(), myEmitters.getDiagramEditorContextMenuProviderName(myDiagram), myDiagram);
 	}
 
 	private void generateEditor() throws InterruptedException {
 		final GenEditorView editor = myEditorGen.getEditor();
 		doGenerateJavaClass(myEmitters.getEditorEmitter(), editor.getQualifiedClassName(), editor);
 	}
 
 	private void generateElementChooser() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getElementChooserEmitter(), myDiagram.getElementChooserQualifiedClassName(), myDiagram);
 	}
 
 	private void generateShortcutCreationWizard() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getShortcutCreationWizardEmitter(), myEmitters.getShortcutCreationWizardName(myDiagram), myDiagram);
 	}
 
 	private void generateDocumentProvider() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getDocumentProviderEmitter(), myDiagram.getDocumentProviderQualifiedClassName(), myDiagram);
 	}
 
 	private void generateDiagramUpdater() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getDiagramUpdaterEmitter(), myEditorGen.getDiagramUpdater().getDiagramUpdaterQualifiedClassName(), myEditorGen.getDiagramUpdater());
 	}
 
 	private void generateUpdateCommand() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getUpdateCommandEmitter(), myEditorGen.getDiagramUpdater().getUpdateCommandQualifiedClassName(), myEditorGen.getDiagramUpdater());
 	}
 
 	private void generateNodeDescriptor() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getNodeDescriptorEmitter(), myEditorGen.getDiagramUpdater().getNodeDescriptorQualifiedClassName(), myEditorGen.getDiagramUpdater());
 	}
 
 	private void generateLinkDescriptor() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getLinkDescriptorEmitter(), myEditorGen.getDiagramUpdater().getLinkDescriptorQualifiedClassName(), myEditorGen.getDiagramUpdater());
 	}
 
 	private void generateActionBarContributor() throws UnexpectedBehaviourException, InterruptedException {
 		final GenEditorView editor = myEditorGen.getEditor();
 		doGenerateJavaClass(myEmitters.getActionBarContributorEmitter(), editor.getActionBarContributorQualifiedClassName(), editor);
 	}
 
 	private void generateMatchingStrategy() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getMatchingStrategyEmitter(), myDiagram.getMatchingStrategyQualifiedClassName(), myDiagram);
 	}
 
 	private void generateNavigatorContentProvider() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getNavigatorContentProviderEmitter(), myEditorGen.getNavigator().getContentProviderQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateDomainNavigatorContentProvider() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getDomainNavigatorContentProviderEmitter(), myEditorGen.getNavigator().getDomainContentProviderQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateDomainNavigatorLabelProvider() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getDomainNavigatorLabelProviderEmitter(), myEditorGen.getNavigator().getDomainLabelProviderQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateDomainNavigatorItem() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getDomainNavigatorItemEmitter(), myEditorGen.getNavigator().getDomainNavigatorItemQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateURIEditorInputTester() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getURIEditorInputTesterEmitter(), myEditorGen.getNavigator().getUriInputTesterQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateNavigatorLabelProvider() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getNavigatorLabelProviderEmitter(), myEditorGen.getNavigator().getLabelProviderQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateNavigatorLinkHelper() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getNavigatorLinkHelperEmitter(), myEditorGen.getNavigator().getLinkHelperQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateNavigatorSorter() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getNavigatorSorterEmitter(), myEditorGen.getNavigator().getSorterQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateNavigatorActionProvider() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getNavigatorActionProviderEmitter(), myEditorGen.getNavigator().getActionProviderQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateAbstractNavigatorItem() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getAbstractNavigatorItemEmitter(), myEditorGen.getNavigator().getAbstractNavigatorItemQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateNavigatorGroup() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getNavigatorGroupEmitter(), myEditorGen.getNavigator().getNavigatorGroupQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateNavigatorItem() throws InterruptedException {
 		doGenerateJavaClass(myEmitters.getNavigatorItemEmitter(), myEditorGen.getNavigator().getNavigatorItemQualifiedClassName(), myEditorGen.getNavigator());
 	}
 
 	private void generateNavigatorGroupIcons() throws InterruptedException, UnexpectedBehaviourException {
 		Set<String> groupIcons = new HashSet<String>();
 		for (GenNavigatorChildReference nextReference : myEditorGen.getNavigator().getChildReferences()) {
 			if (nextReference.getGroupIcon() != null && nextReference.getGroupIcon().length() > 0) {
 				groupIcons.add(nextReference.getGroupIcon());
 			}
 		}
 		for (String iconPath : groupIcons) {
 			generateGroupIcon(new Path(iconPath));
 		}
 	}
 
 	// property sheet 
 
 	protected void generatePropertySheetSections() throws UnexpectedBehaviourException, InterruptedException {
 		if (myEditorGen.getPropertySheet().isNeedsCaption()) {
 			internalGenerateJavaClass(myEmitters.getPropertySheetLabelProviderEmitter(), myEditorGen.getPropertySheet().getLabelProviderQualifiedClassName(), myEditorGen.getPropertySheet());
 		}
 		for (GenPropertyTab tab : myEditorGen.getPropertySheet().getTabs()) {
 			if (tab instanceof GenCustomPropertyTab && ((GenCustomPropertyTab) tab).isGenerateBoilerplate()) {
 				internalGenerateJavaClass(myEmitters.getPropertySectionEmitter(), ((GenCustomPropertyTab) tab).getQualifiedClassName(), tab);
 			}
 		}
 	}
 
 	// expressions
 
 	private void generateExpressionProviders() throws UnexpectedBehaviourException, InterruptedException {
 		GenExpressionProviderContainer providerContainer = myEditorGen.getExpressionProviders();
 		boolean needAbstractExpression = false;
 		for (GenExpressionProviderBase nextProvider : providerContainer.getProviders()) {
 			if (nextProvider instanceof GenExpressionInterpreter) {
 				TextEmitter providerEmitter = null;
 				if (GenLanguage.OCL_LITERAL.equals(nextProvider.getLanguage())) {
 					providerEmitter = myEmitters.getOCLExpressionFactoryEmitter();
 					needAbstractExpression = true;
 				} else if (GenLanguage.REGEXP_LITERAL.equals(nextProvider.getLanguage()) || GenLanguage.NREGEXP_LITERAL.equals(nextProvider.getLanguage())) {
 					providerEmitter = myEmitters.getRegexpExpressionFactoryEmitter();
 					needAbstractExpression = true;
 				}
 				GenExpressionInterpreter interpreter = (GenExpressionInterpreter) nextProvider;
 				if (providerEmitter != null) {
 					doGenerateJavaClass(providerEmitter, interpreter.getQualifiedClassName(), interpreter);
 				}
 			}
 		}
 		if (needAbstractExpression) {
 			// so that if there are only literal initializers, do not generate any extra class
 			doGenerateJavaClass(myEmitters.getAbstractExpressionEmitter(), providerContainer.getAbstractExpressionQualifiedClassName(), myDiagram);
 		}
 	}
 
 	private void generateModelAccessFacility() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getModelAccessFacilityEmitter(), myEditorGen.getModelAccess().getQualifiedClassName(), myEditorGen.getModelAccess());
 	}
 
 	private void generateShortcutIcon() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateBinaryFile(myEmitters.getShortcutImageEmitter(), new Path("icons/shortcut.gif"), null); //$NON-NLS-1$
 	}
 
 	private void generateGroupIcon(Path groupIconPath) throws InterruptedException, UnexpectedBehaviourException {
 		doGenerateBinaryFile(myEmitters.getGroupIconEmitter(), groupIconPath, null);
 	}
 
 	private void generateDiagramIcon(String path) throws UnexpectedBehaviourException, InterruptedException {
 		// use genModel.prefix if available to match colors of model icons and diagram icons
 		// @see GenPackageImpl#generateEditor - it passes prefix to ModelGIFEmitter 
 		Object[] args = new Object[] { myDiagram.getDomainDiagramElement() == null ? myEditorGen.getDiagramFileExtension() : myDiagram.getDomainDiagramElement().getGenPackage().getPrefix() };
 		doGenerateBinaryFile(myEmitters.getDiagramIconEmitter(), new Path(path), args);
 	}
 
 	private void generateWizardBanner() throws UnexpectedBehaviourException, InterruptedException {
 		String stem = myDiagram.getDomainDiagramElement() == null ? "" : myDiagram.getDomainDiagramElement().getGenPackage().getPrefix(); //$NON-NLS-1$
 		// @see GenPackageImpl#generateEditor - it passes prefix to ModelWizardGIFEmitter
 		Object[] args = new Object[] { stem.length() == 0 ? myEditorGen.getDiagramFileExtension() : stem };
 		doGenerateBinaryFile(myEmitters.getWizardBannerImageEmitter(), new Path("icons/wizban/New" + stem + "Wizard.gif"), args); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	private void generateExternalizationSupport() throws UnexpectedBehaviourException, InterruptedException {
 		String packageName = myEditorGen.getEditor().getPackageName();
 		String messagesClassName = "Messages"; //$NON-NLS-1$
 		doGenerateJavaClass(myEmitters.getExternalizeEmitter(), packageName, messagesClassName, new Object[] { myEditorGen });
 		doGenerateFile(myEmitters.getMessagesEmitter(), new Path(messagesClassName.toLowerCase() + ".properties"), new Object[] { myEditorGen }); //$NON-NLS-1$
 	}
 
 	// plugin
 
 	private void generatePlugin() throws UnexpectedBehaviourException, InterruptedException {
 		generateActivator();
 		generateBundleManifest();
 		generatePluginXml();
 		generatePluginProperties();
 		generateBuildProperties();
 		generateOptionsFile();
 	}
 
 	private void generateActivator() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getActivatorEmitter(), myEditorGen.getPlugin().getActivatorQualifiedClassName(), myEditorGen.getPlugin());
 	}
 
 	private void generateBundleManifest() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateFile(myEmitters.getBundleManifestEmitter(), new Path("META-INF/MANIFEST.MF"), new Object[] { myDiagram.getEditorGen().getPlugin() }); //$NON-NLS-1$
 	}
 
 	private void generatePluginXml() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateFile(myEmitters.getPluginXmlEmitter(), new Path("plugin.xml"), new Object[] { myDiagram.getEditorGen().getPlugin() }); //$NON-NLS-1$
 	}
 
 	private void generatePluginProperties() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateFile(myEmitters.getPluginPropertiesEmitter(), new Path("plugin.properties"), new Object[] { myDiagram.getEditorGen().getPlugin() }); //$NON-NLS-1$
 	}
 
 	private void generateBuildProperties() throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateFile(myEmitters.getBuildPropertiesEmitter(), new Path("build.properties"), new Object[] { myEditorGen.getPlugin() }); //$NON-NLS-1$
 	}
 
 	private void generateOptionsFile() throws InterruptedException, UnexpectedBehaviourException {
 		doGenerateFile(myEmitters.getOptionsFileEmitter(), new Path(".options"), new Object[] { myEditorGen.getPlugin() }); //$NON-NLS-1$
 	}
 
 	// application
 
 	private void generateApplication() throws UnexpectedBehaviourException, InterruptedException {
 		GenApplication application = myEditorGen.getApplication();
 		if (application != null) {
 			generateApplication(application);
 			generateActionBarAdvisor(application);
 			generatePerspective(application);
 			generateWorkbenchAdvisor(application);
 			generateWorkbenchWindowAdvisor(application);
 			generateWizardNewFileCreationPage(application);
 		}
 	}
 
 	private void generateApplication(GenApplication application) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getApplicationEmitter(), application.getQualifiedClassName(), application);
 	}
 
 	private void generateActionBarAdvisor(GenApplication application) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getActionBarAdvisorEmitter(), application.getActionBarAdvisorQualifiedClassName(), application);
 	}
 
 	private void generatePerspective(GenApplication application) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getPerspectiveEmitter(), application.getPerspectiveQualifiedClassName(), application);
 	}
 
 	private void generateWorkbenchAdvisor(GenApplication application) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getWorkbenchAdvisorEmitter(), application.getWorkbenchAdvisorQualifiedClassName(), application);
 	}
 
 	private void generateWorkbenchWindowAdvisor(GenApplication application) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getWorkbenchWindowAdvisorEmitter(), application.getWorkbenchWindowAdvisorQualifiedClassName(), application);
 	}
 
 	private void generateWizardNewFileCreationPage(GenApplication application) throws UnexpectedBehaviourException, InterruptedException {
 		doGenerateJavaClass(myEmitters.getWizardNewFileCreationPageEmitter(), application.getPackageName(), "WizardNewFileCreationPage", application); //$NON-NLS-1$
 	}
 
 	// actions
 	private void generateActions() throws UnexpectedBehaviourException, InterruptedException {
 		HashSet<GenContributionItem> processedItems = new HashSet<GenContributionItem>();
 		for (GenContributionManager m : myEditorGen.getContextMenus()) {
 			LinkedList<GenContributionItem> items = new LinkedList<GenContributionItem>(m.getItems());
 			while (!items.isEmpty()) {
 				GenContributionItem ci = items.removeFirst();
 				if (ci instanceof GenCustomAction && ((GenCustomAction) ci).isGenerateBoilerplate() && !processedItems.contains(ci)) {
 					doGenerateJavaClass(myEmitters.getCustomActionEmitter(), ((GenCustomAction) ci).getQualifiedClassName(), ci);
 					processedItems.add(ci);
 				} else if (ci instanceof GenContributionManager) {
 					items.addAll(((GenContributionManager) ci).getItems());
 				} else if (ci instanceof GenSharedContributionItem) {
 					items.addLast(((GenSharedContributionItem) ci).getActualItem());
 				} else if (ci instanceof GenAction) {
 					doGenerateJavaClass(myEmitters.getPredefinedActionEmitter(), ((GenAction) ci).getQualifiedClassName(), ci);
 					processedItems.add(ci);
 				}
 			}
 		}
 	}
 
 	private void generateCustomExtensions() throws UnexpectedBehaviourException, InterruptedException {
 		if (myEditorGen.getExtensions().isEmpty()) {
 			return;
 		}
 		List<GenTemplateInvocationBase> unresolvedInvocations = new ArrayList<GenTemplateInvocationBase>();
 		Map<GenTemplateInvocationBase, Collection<EObject>> boundInputs = bindInvocationsToInputs(unresolvedInvocations);
 		for (Map.Entry<GenTemplateInvocationBase, Collection<EObject>> nextEntry : boundInputs.entrySet()) {
 			GenTemplateInvocationBase invocation = nextEntry.getKey();
 			Collection<EObject> oclInputs = nextEntry.getValue();
 			if (oclInputs == null || oclInputs.isEmpty()) {
 				unresolvedInvocations.add(invocation);
 				continue;
 			}
 			generateTemplateInvocation(invocation, oclInputs);
 		}
 
 		if (!unresolvedInvocations.isEmpty()) {
 			throw new UnexpectedBehaviourException("There were custom templates invocations with unresolved inputs: " + unresolvedInvocations);
 		}
 	}
 
 	private void generateTemplateInvocation(GenTemplateInvocationBase invocation, Collection<EObject> oclInput) throws UnexpectedBehaviourException, InterruptedException {
 		String primaryTemplateFQN = invocation.getTemplateFqn();
 		if (primaryTemplateFQN == null) {
 			handleException(new NullPointerException("Invocation without templateFQN: " + invocation));
 			return;
 		}
 
 		TextEmitter primaryEmitter = myEmitters.newXpandEmitter(primaryTemplateFQN);
 		TextEmitter fqnEmitter = myEmitters.getQualifiedClassNameEmitterForPrimaryTemplate(primaryTemplateFQN);
 
 		Object[] templateInputs;
 		if (invocation instanceof GenFixedInputsTemplateInvocation) {
 			templateInputs = oclInput.toArray();
 		} else if (invocation instanceof GenTemplateInvocation) {
 			templateInputs = computeTemplateInputs((GenTemplateInvocation) invocation, oclInput);
 		} else {
 			throw new UnexpectedBehaviourException("Unknown invocation type: " + invocation);
 		}
 		for (Object nextTemplateInput : templateInputs) {
 			String nextFqn;
 			try {
 				nextFqn = fqnEmitter.generate(new NullProgressMonitor(), new Object[] { nextTemplateInput });
 			} catch (Exception e) {
 				handleException(new UnexpectedBehaviourException(//
 						"Error computing FQN for invocation " + invocation + //
 								" on " + nextTemplateInput, e));
 				continue;
 			}
 			if (nextFqn != null) {
 				doGenerateJavaClass(primaryEmitter, nextFqn, nextTemplateInput);
 			}
 		}
 	}
 
 	private Object[] computeTemplateInputs(GenTemplateInvocation invocation, Collection<EObject> oclInputs) throws UnexpectedBehaviourException {
 		String oclExpressionText = invocation.getOclExpression();
 		if (oclExpressionText == null || oclExpressionText.trim().length() == 0 || "self".equals(oclExpressionText.trim())) {
 			return oclInputs.toArray();
 		}
 
 		List<Object> results = new ArrayList<Object>();
 		OCL ocl = OCL.newInstance();
 		ParsingOptions.setOption(ocl.getEnvironment(), ParsingOptions.implicitRootClass(ocl.getEnvironment()), EcorePackage.eINSTANCE.getEObject());
 		for (EObject nextOclInput : oclInputs) {
 			try {
 				Helper oclHelper = ocl.createOCLHelper();
 				oclHelper.setContext(nextOclInput.eClass());
 				OCLExpression oclExpression = oclHelper.createQuery(oclExpressionText);
 				Object oclResult = ocl.evaluate(nextOclInput, oclExpression);
 				if (oclResult instanceof Collection<?>) {
 					results.addAll((Collection<?>) oclResult);
 				} else {
 					results.add(oclResult);
 				}
 			} catch (Exception e) {
 				throw new UnexpectedBehaviourException("Can't evaluate OCL " + oclExpressionText + " for context: " + nextOclInput, e);
 			}
 		}
 		return results.toArray();
 	}
 
 	private Map<GenCustomTemplateInput, Collection<EObject>> resolveCustomTemplateInputs() {
 		Map<GenCustomTemplateInput, Collection<EObject>> resolvedInputs = new HashMap<GenCustomTemplateInput, Collection<EObject>>();
 		for (GenCustomGeneratorExtension nextExtension : myEditorGen.getExtensions()) {
 			EObject nextInput = nextExtension.getRootInput();
 			if (nextInput == null) {
 				nextInput = myEditorGen;
 			}
 			resolvedInputs.put(nextExtension, Collections.singletonList(nextInput));
 		}
 		for (GenCustomGeneratorExtension nextExtension : myEditorGen.getExtensions()) {
 			for (GenTemplateInvocationBase nextInvocation : nextExtension.getInvocations()) {
 				if (nextInvocation instanceof GenFixedInputsTemplateInvocation) {
 					GenFixedInputsTemplateInvocation hasFixedInputs = (GenFixedInputsTemplateInvocation) nextInvocation;
 					Collection<EObject> nextInputs;
 					if (hasFixedInputs.getFixedInputs().isEmpty()) {
 						nextInputs = resolvedInputs.get(hasFixedInputs.getExtension());
 					} else {
 						nextInputs = new ArrayList<EObject>(hasFixedInputs.getFixedInputs());
 					}
 					resolvedInputs.put(hasFixedInputs, nextInputs);
 				}
 			}
 		}
 		return resolvedInputs;
 	}
 
 	private Map<GenTemplateInvocationBase, Collection<EObject>> bindInvocationsToInputs(List<GenTemplateInvocationBase> unresolvedInvocations) {
 		Map<GenCustomTemplateInput, Collection<EObject>> resolvedInputs = resolveCustomTemplateInputs();
 		Map<GenTemplateInvocationBase, Collection<EObject>> result = new LinkedHashMap<GenTemplateInvocationBase, Collection<EObject>>();
 		for (GenCustomGeneratorExtension extension : myEditorGen.getExtensions()) {
 			for (GenTemplateInvocationBase nextInvocation : extension.getInvocations()) {
 				if (nextInvocation instanceof GenFixedInputsTemplateInvocation) {
 					GenFixedInputsTemplateInvocation nextImpl = (GenFixedInputsTemplateInvocation) nextInvocation;
 					if (resolvedInputs.containsKey(nextImpl)) {
 						result.put(nextInvocation, resolvedInputs.get(nextImpl));
 					} else {
 						unresolvedInvocations.add(nextImpl);
 					}
 				} else if (nextInvocation instanceof GenTemplateInvocation) {
 					GenTemplateInvocation nextImpl = (GenTemplateInvocation) nextInvocation;
 					List<EObject> combinedInputs = new ArrayList<EObject>();
 
 					Collection<? extends GenCustomTemplateInput> inputsFromModel = nextImpl.getInputs();
 					if (inputsFromModel.isEmpty()) {
 						inputsFromModel = Collections.singletonList(nextImpl.getExtension());
 					}
 					boolean hasUnresolvedInputRef = false;
 					for (GenCustomTemplateInput nextInputRef : inputsFromModel) {
 						Collection<EObject> nextResolvedInput = resolvedInputs.get(nextInputRef);
 						if (nextResolvedInput == null) {
 							hasUnresolvedInputRef = true;
 							break;
 						}
 						combinedInputs.addAll(nextResolvedInput);
 					}
 					if (hasUnresolvedInputRef || combinedInputs.isEmpty()) {
 						unresolvedInvocations.add(nextImpl);
 					} else {
 						result.put(nextInvocation, combinedInputs);
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	// util
 
 	/**
 	 * Passes initialized ImportManager as second template argument
 	 */
 	private void internalGenerateJavaClass(TextEmitter emitter, String packageName, String className, Object argument) throws InterruptedException {
 		ImportUtil importUtil = new ImportUtil(packageName, className);
 		doGenerateJavaClass(emitter, packageName, className, argument, importUtil);
 	}
 
 	private void internalGenerateJavaClass(TextEmitter emitter, String qualifiedName, Object argument) throws InterruptedException {
 		internalGenerateJavaClass(emitter, CodeGenUtil.getPackageName(qualifiedName), CodeGenUtil.getSimpleClassName(qualifiedName), argument);
 	}
 
 	private IPath guessProjectLocation(String projectName) {
 		if (myEditorGen.getDomainGenModel() == null) {
 			return null;
 		}
 		Path modelProjectPath = new Path(myEditorGen.getDomainGenModel().getModelDirectory());
 		return guessNewProjectLocation(modelProjectPath, projectName);
 	}
 
 	protected void setupProgressMonitor() {
 		Counter c = new Counter();
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenNode(), 7);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenChildLabelNode(), 5);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenLink(), 6);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenLinkLabel(), 2);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenCompartment(), 3);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenDiagram(), 30);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenEditorGenerator(), 2); // i18n=2
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenPlugin(), 6);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenNavigator(), 3);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenNavigatorChildReference(), 1);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenCustomPropertyTab(), 1);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getBehaviour(), 1);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenMetricContainer(), 1);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getGenExpressionProviderContainer(), 1);
 		c.registerFactor(GMFGenPackage.eINSTANCE.getPalette(), 1);
 		setupProgressMonitor(null, c.getTotal(myEditorGen));
 	}
 
 	private static boolean needsGraphicalNodeEditPolicy(GenNode node) {
 		return node.getModelFacet() != null && !node.getReorientedIncomingLinks().isEmpty();
 	}
 }
