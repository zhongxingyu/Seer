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
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.codegen.jet.JETCompiler;
 import org.eclipse.emf.codegen.merge.java.JControlModel;
 import org.eclipse.emf.codegen.merge.java.JMerger;
 import org.eclipse.emf.codegen.util.CodeGenUtil;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.gmf.common.UnexpectedBehaviourException;
 import org.eclipse.gmf.internal.common.codegen.BinaryEmitter;
 import org.eclipse.gmf.internal.common.codegen.DefaultTextMerger;
 import org.eclipse.gmf.internal.common.codegen.GIFEmitter;
 import org.eclipse.gmf.internal.common.codegen.JETGIFEmitterAdapter;
 import org.eclipse.gmf.internal.common.codegen.TextEmitter;
 import org.eclipse.gmf.internal.common.codegen.TextMerger;
 import org.eclipse.gmf.internal.common.codegen.XpandTextEmitter;
 import org.eclipse.gmf.internal.xpand.ResourceManager;
 import org.eclipse.gmf.internal.xpand.util.BundleResourceManager;
 import org.osgi.framework.Bundle;
 
 /**
  * @author artem
  */
 public class CodegenEmitters {
 
 	private static final String PATH_SEPARATOR = "::"; //$NON-NLS-1$
 
 	private final ResourceManager myResourceManager;
 
 	private final URL[] myLocations;
 
 	private Map<String, Object> myGlobals;
 
 	public CodegenEmitters(boolean useBaseTemplatesOnly, String templateDirectory, boolean includeDynamicModelTemplates) {
 		ArrayList<URL> urls = new ArrayList<URL>(5);
 		if (!useBaseTemplatesOnly) {
 			urls.add(getDynamicTemplatesURL(templateDirectory));
 		}
 		if (includeDynamicModelTemplates) {
 			urls.add(getTemplatesBundle().getEntry("/templates-dynmodel/")); //$NON-NLS-1$
 		}
 		urls.add(getTemplatesBundle().getEntry("/templates/")); //$NON-NLS-1$
 		
 		urls.add(Platform.getBundle("org.eclipse.gmf.graphdef.codegen").getEntry("/templates/")); //$NON-NLS-1$ //$NON-NLS-2$
 		
 		myLocations = urls.toArray(new URL[urls.size()]);
 		myResourceManager = new BundleResourceManager(myLocations);
 	}
 
 	/* package */void setGlobals(Map<String, Object> globals) {
 		myGlobals = globals;
 	}
 
 	/**
 	 * @return null if no merger is needed
 	 */
 	public TextMerger createMergeService() {
 		URL controlFile = getJMergeControlFile();
 		if (controlFile != null) {
 			JControlModel controlModel = new JControlModel();
 			controlModel.initialize(CodeGenUtil.instantiateFacadeHelper(JMerger.DEFAULT_FACADE_HELPER_CLASS), controlFile.toString());
 			if (!controlModel.canMerge()) {
 				throw new IllegalStateException("Can not initialize JControlModel");
 			}
 			return new DefaultTextMerger(controlModel);
 		}
 		return null;
 	}
 
 	private static Bundle getTemplatesBundle() {
 		return Platform.getBundle("org.eclipse.gmf.codegen"); //$NON-NLS-1$
 	}
 
 	private static URL getDynamicTemplatesURL(String templateDirectory) {
 		if (templateDirectory != null) {
 			URI templatesURI = templateDirectory.indexOf(":") == -1 ? URI.createPlatformResourceURI(templateDirectory, true) : URI.createURI(templateDirectory); //$NON-NLS-1$
 			try {
 				return new URL(templatesURI.toString());
 			} catch (MalformedURLException e) {
 				Platform.getLog(getTemplatesBundle()).log(new Status(IStatus.ERROR, getTemplatesBundle().getSymbolicName(), 0, "Incorrecct dynamic templates location", e)); //$NON-NLS-1$
 			}
 		}
 		return null;
 	}
 
 	public URL getJMergeControlFile() {
 		return getTemplatesBundle().getEntry("/templates/emf-merge.xml"); //$NON-NLS-1$
 	}
 
 	// commands
 
 	public TextEmitter getCreateNodeCommandEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::commands::CreateNodeCommand"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getCreateLinkCommandEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("xpt::diagram::commands::CreateLinkCommand::Main"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getReorientLinkCommandEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::commands::ReorientLinkCommand"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getReorientRefLinkCommandEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::commands::ReorientRefLinkCommand"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getReorientLinkViewCommandEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::commands::ReorientLinkViewCommand"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getCreateShortcutDecorationsCommandEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::commands::CreateShortcutDecorationsCommand"); //$NON-NLS-1$
 	}
 
 	// helpers
 
 	public TextEmitter getBaseEditHelperEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::edithelpers::BaseEditHelper"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getEditHelperEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::edithelpers::EditHelper"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getEditHelperAdviceEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::edithelpers::EditHelperAdvice"); //$NON-NLS-1$
 	}
 
 	// parts
 
 	public TextEmitter getDiagramEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainEmitter("diagram::editparts::DiagramEditPart"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNodeEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainEmitter("diagram::editparts::NodeEditPart"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNodeLabelEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainEmitter("diagram::editparts::NodeLabelEditPart"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getExternalNodeLabelEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainEmitter("diagram::editparts::ExternalNodeLabelEditPart"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getChildNodeLabelEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainEmitter("diagram::editparts::ChildNodeLabelEditPart"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getCompartmentEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainEmitter("diagram::editparts::CompartmentEditPart"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getLinkEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainEmitter("diagram::editparts::LinkEditPart"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getLinkLabelEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainEmitter("diagram::editparts::LinkLabelEditPart"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getEditPartFactoryEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::editparts::EditPartFactory"); //$NON-NLS-1$
 	}
 
 	// policies
 
 	public TextEmitter getBaseItemSemanticEditPolicyEmitter() {
 		return getPrimaryEmitter("xpt::diagram::editpolicies::BaseItemSemanticEditPolicy"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getOpenDiagramEditPolicyEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::editpolicies::OpenDiagram"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getDiagramCanonicalEditPolicyEmitter() {
 		return newXpandEmitter("diagram::editpolicies::DiagramCanonicalEditPolicy::Main"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getChildContainerCanonicalEditPolicyEmitter() {
 		return newXpandEmitter("diagram::editpolicies::ChildContainerCanonicalEditPolicy::Main"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getDiagramItemSemanticEditPolicyEmitter() {
 		return getPrimaryEmitter("xpt::diagram::editpolicies::DiagramItemSemanticEditPolicy"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getCompartmentItemSemanticEditPolicyEmitter() {
 		return getPrimaryEmitter("xpt::diagram::editpolicies::CompartmentItemSemanticEditPolicy"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getGraphicalNodeEditPolicyEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::editpolicies::GraphicalNodeEditPolicy"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNodeItemSemanticEditPolicyEmitter() {
 		return getPrimaryEmitter("xpt::diagram::editpolicies::NodeItemSemanticEditPolicy"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getLinkItemSemanticEditPolicyEmitter() {
 		return getPrimaryEmitter("xpt::diagram::editpolicies::LinkItemSemanticEditPolicy"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getTextSelectionEditPolicyEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("xpt::diagram::editpolicies::TextFeedback::TextSelectionEditPolicy"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getTextNonResizableEditPolicyEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("xpt::diagram::editpolicies::TextFeedback::TextNonResizableEditPolicy"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getVisualEffectEditPolicyEmitter() {
 		return getPrimaryEmitter("xpt::diagram::editpolicies::VisualEffectEditPolicy"); //$NON-NLS-1$
 	}
 
 	// updater
 
 	public TextEmitter getDiagramUpdaterEmitter() {
 		return getPrimaryEmitter("xpt::diagram::updater::DiagramUpdater"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getUpdateCommandEmitter() {
 		return getPrimaryEmitter("xpt::diagram::updater::UpdateCommand"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNodeDescriptorEmitter() {
 		return getPrimaryEmitter("xpt::diagram::updater::NodeDescriptor"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getLinkDescriptorEmitter() {
 		return getPrimaryEmitter("xpt::diagram::updater::LinkDescriptor"); //$NON-NLS-1$
 	}
 
 	// parsers
 
	public TextEmitter getAbstractParserEmitter() throws UnexpectedBehaviourException {
		return newXpandEmitter("impl::parsers::AbstractParser::Main"); //$NON-NLS-1$
	}
 
	/**
	 * @deprecated we don't want to generate this class anymore, but the changes in i18n may create compilation errors if we don't explicitly clear the contents of generated file. 
	 * @see comments in template impl::parsers::AbstractParser for details 
	 */
 	public String getAbstractParserName(Object... input) throws UnexpectedBehaviourException {
		return getText(newXpandEmitter("impl::parsers::AbstractParser::deprecatedQualifiedClassName"), input); //$NON-NLS-1$
 	}
 
 	public TextEmitter getPredefinedParserEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("parsers::PredefinedParser::Main"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getCustomParserEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("parsers::CustomParser::Main"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getExpressionLabelParserEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("parsers::ExpressionLabelParser::Main"); //$NON-NLS-1$
 	}
 
 	// providers
 
 	public TextEmitter getParserProviderEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("parsers::ParserProvider::Main"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getElementInitializersEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::providers::ElementInitializers"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getElementTypesEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::providers::ElementTypes"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getViewProviderEmitter() {
 		return getPrimaryEmitter("xpt::providers::ViewProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getEditPartProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::providers::EditPartProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getModelingAssistantProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::providers::ModelingAssistantProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getIconProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::providers::IconProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getMarkerNavigationProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::providers::MarkerNavigationProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getValidationProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::providers::ValidationProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getValidationDecoratorProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::providers::ValidationDecoratorProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getShortcutsDecoratorProviderEmitter() {
 		return getPrimaryEmitter("xpt::providers::ShortcutsDecoratorProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getMetricProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::providers::MetricProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getAbstractExpressionEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::expressions::AbstractExpression"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getOCLExpressionFactoryEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::expressions::OCLExpressionFactory"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getRegexpExpressionFactoryEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::expressions::RegexpExpressionFactory"); //$NON-NLS-1$
 	}
 
 	// property sheet
 
 	public TextEmitter getPropertySheetLabelProviderEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("xpt::propsheet::LabelProvider::Class"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getPropertySectionEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("xpt::propsheet::PropertySection::Class"); //$NON-NLS-1$
 	}
 
 	// editor
 
 	public TextEmitter getValidateActionEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::editor::ValidateAction"); //$NON-NLS-1$
 	}
 
 	public String getValidateActionName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::ValidateAction", input); //$NON-NLS-1$
 	}
 
 	public TextEmitter getValidationMarkerEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::editor::ValidationMarker"); //$NON-NLS-1$
 	}
 
 	public String getValidationMarkerName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::ValidationMarker", input); //$NON-NLS-1$
 	}
 
 	public TextEmitter getDiagramContentInitializerEmitter() {
 		return getPrimaryEmitter("xpt::editor::DiagramContentInitializer"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getShortcutPropertyTesterEmitter() {
 		return getPrimaryEmitter("xpt::editor::ShortcutPropertyTester"); //$NON-NLS-1$
 	}
 
 	public String getShortcutCreationWizardName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::ShortcutCreationWizard", input); //$NON-NLS-1$
 	}
 
 	public TextEmitter getShortcutCreationWizardEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::editor::ShortcutCreationWizard"); //$NON-NLS-1$
 	}
 
 	public String getModelElementSelectionPageName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::ModelElementSelectionPage", input); //$NON-NLS-1$
 	}
 
 	public TextEmitter getModelElementSelectionPageEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::editor::ModelElementSelectionPage"); //$NON-NLS-1$
 	}
 
 	public String getNewDiagramFileWizardName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::NewDiagramFileWizard", input); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNewDiagramFileWizardEmitter() {
 		return getPrimaryEmitter("xpt::editor::NewDiagramFileWizard"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getPaletteEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("xpt::editor::palette::PaletteFactory::Factory"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getDiagramEditorUtilEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::editor::DiagramEditorUtil"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getVisualIDRegistryEmitter() {
 		return getPrimaryEmitter("xpt::editor::VisualIDRegistry"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getCreationWizardEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::editor::CreationWizard"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getCreationWizardPageEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::editor::CreationWizardPage"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getDeleteElementActionEmitter() {
 		return getPrimaryEmitter("xpt::editor::DeleteElementAction"); //$NON-NLS-1$
 	}
 
 	public String getDeleteElementActionName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::DeleteElementAction", input); //$NON-NLS-1$
 	}
 
 	public TextEmitter getDiagramEditorContextMenuProviderEmitter() {
 		return getPrimaryEmitter("xpt::editor::DiagramEditorContextMenuProvider"); //$NON-NLS-1$
 	}
 
 	public String getDiagramEditorContextMenuProviderName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::DiagramEditorContextMenuProvider", input); //$NON-NLS-1$
 	}
 
 	public TextEmitter getEditorEmitter() {
 		return getPrimaryEmitter("xpt::editor::Editor"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getElementChooserEmitter() {
 		return getPrimaryEmitter("xpt::editor::ElementChooser"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getDocumentProviderEmitter() {
 		return getPrimaryEmitter("xpt::editor::DocumentProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getActionBarContributorEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::editor::ActionBarContributor"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getMatchingStrategyEmitter() {
 		return getPrimaryEmitter("xpt::editor::MatchingStrategy"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getURIEditorInputTesterEmitter() {
 		return getPrimaryEmitter("xpt::editor::UriEditorInputTester"); //$NON-NLS-1$
 	}
 
 	public BinaryEmitter getShortcutImageEmitter() throws UnexpectedBehaviourException {
 		return newGIFEmitter("/xpt/editor/shortcut.gif"); //$NON-NLS-1$
 	}
 
 	public BinaryEmitter getDiagramIconEmitter() throws UnexpectedBehaviourException {
 		return newGIFEmitterAdapter("/xpt/editor/diagram.gif"); //$NON-NLS-1$
 	}
 
 	public BinaryEmitter getWizardBannerImageEmitter() throws UnexpectedBehaviourException {
 		return newGIFEmitterAdapter("/xpt/editor/wizban.gif"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getModelAccessFacilityEmitter() {
 		return newXpandEmitter("Facility::Main"); //$NON-NLS-1$
 	}
 
 	// navigator
 
 	public TextEmitter getNavigatorContentProviderEmitter() {
 		return getPrimaryEmitter("xpt::navigator::NavigatorContentProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getDomainNavigatorContentProviderEmitter() {
 		return getPrimaryEmitter("xpt::navigator::DomainNavigatorContentProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getDomainNavigatorLabelProviderEmitter() {
 		return getPrimaryEmitter("xpt::navigator::DomainNavigatorLabelProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getDomainNavigatorItemEmitter() {
 		return getPrimaryEmitter("xpt::navigator::DomainNavigatorItem"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNavigatorLabelProviderEmitter() {
 		return getPrimaryEmitter("xpt::navigator::NavigatorLabelProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNavigatorLinkHelperEmitter() {
 		return getPrimaryEmitter("xpt::navigator::NavigatorLinkHelper"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNavigatorSorterEmitter() {
 		return getPrimaryEmitter("xpt::navigator::NavigatorSorter"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNavigatorActionProviderEmitter() {
 		return getPrimaryEmitter("xpt::navigator::NavigatorActionProvider"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getAbstractNavigatorItemEmitter() {
 		return getPrimaryEmitter("xpt::navigator::AbstractNavigatorItem"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNavigatorGroupEmitter() {
 		return getPrimaryEmitter("xpt::navigator::NavigatorGroup"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getNavigatorItemEmitter() {
 		return getPrimaryEmitter("xpt::navigator::NavigatorItem"); //$NON-NLS-1$
 	}
 
 	public BinaryEmitter getGroupIconEmitter() throws UnexpectedBehaviourException {
 		return newGIFEmitter("/xpt/navigator/navigatorGroup.gif"); //$NON-NLS-1$
 	}
 
 	// preferences
 
 	public TextEmitter getPreferenceInitializerEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::diagram::preferences::PreferenceInitializer"); //$NON-NLS-1$
 	}
 
 	public String getPreferenceInitializerName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::diagram::preferences::PreferenceInitializer", input); //$NON-NLS-1$
 	}
 
 	public TextEmitter getStandardPreferencePageEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("impl::preferences::StandardPage::Main"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getCustomPreferencePageEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("impl::preferences::CustomPage::Main"); //$NON-NLS-1$
 	}
 
 	// plugin
 
 	public TextEmitter getActivatorEmitter() throws UnexpectedBehaviourException {
 		return newXpandEmitter("plugin::Activator::Main"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getBundleManifestEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::plugin::manifest"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getPluginXmlEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::plugin::plugin"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getPluginPropertiesEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::plugin::properties"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getBuildPropertiesEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::plugin::build"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getOptionsFileEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::plugin::options"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getExternalizeEmitter() {
 		return newXpandEmitter("xpt::Externalizer::Access"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getMessagesEmitter() {
 		return newXpandEmitter("xpt::Externalizer::Values"); //$NON-NLS-1$
 	}
 
 	// application
 
 	public TextEmitter getApplicationEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::application::Application"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getActionBarAdvisorEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::application::ActionBarAdvisor"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getPerspectiveEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::application::Perspective"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getWorkbenchAdvisorEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::application::WorkbenchAdvisor"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getWorkbenchWindowAdvisorEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::application::WorkbenchWindowAdvisor"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getWizardNewFileCreationPageEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryEmitter("xpt::application::WizardNewFileCreationPage"); //$NON-NLS-1$
 	}
 
 	// actions
 
 	public TextEmitter getCustomActionEmitter() {
 		return newXpandEmitter("impl::actions::CustomAction::Main"); //$NON-NLS-1$
 	}
 
 	public TextEmitter getPredefinedActionEmitter() {
 		return newXpandEmitter("impl::actions::PredefinedAction::Main"); //$NON-NLS-1$
 	}
 
 	// util
 
 	/**
 	 * Returns emitter for the primary definition in the specified template.
 	 * Primary definition has the same name as template file.
 	 */
 	private TextEmitter getPrimaryEmitter(String templateName) {
 		String[] parts = templateName.split(PATH_SEPARATOR);
 		String definition = templateName + PATH_SEPARATOR + parts[parts.length - 1];
 		return newXpandEmitter(definition);
 	}
 
 	/**
 	 * Returns "Main" emitter for the specified template file.
 	 */
 	private TextEmitter getMainEmitter(String templateFilePath) {
 		return newXpandEmitter(templateFilePath + PATH_SEPARATOR + "Main"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns emitter for qualified class name definition in the specified template.
 	 * Definition should be named 'qualifiedClassName'.
 	 */
 	private TextEmitter getQualifiedClassNameEmitter(String templateName) throws UnexpectedBehaviourException {
 		String definition = templateName + PATH_SEPARATOR + "qualifiedClassName"; //$NON-NLS-1$
 		return newXpandEmitter(definition);
 	}
 	
 	/**
 	 * Returns emitter for qualified class name definition for specified <strong>primary template</strong>.
 	 * That is, in contrast to {@link CodegenEmitters#getQualifiedClassNameEmitter(String)} expects the name 
 	 * of invocable template 'xpt::diagram::editparts::TemplateName::PrimaryName' as an input
 	 * <p>
 	 * Definition should be named 'qualifiedClassName'.
 	 * @return
 	 * 		for 'xpt::diagram::editparts::TemplateName::PrimaryName' returns template 'xpt::diagram::editparts::TemplateName::qualifiedClassName'
 	 */
 	protected TextEmitter getQualifiedClassNameEmitterForPrimaryTemplate(String templateName) throws UnexpectedBehaviourException {
 		String[] parts = templateName.split(PATH_SEPARATOR);
 		StringBuilder withoutLastSegment = new StringBuilder();
 		for (int i = 0; i < parts.length - 1; i++){
 			if (withoutLastSegment.length() > 0){
 				withoutLastSegment.append(PATH_SEPARATOR);
 			}
 			withoutLastSegment.append(parts[i]);
 		}
 		return getQualifiedClassNameEmitter(withoutLastSegment.toString());
 	}
 
 	/**
 	 * Returns text generated by emitter.
 	 */
 	private String getText(TextEmitter emitter, Object... input) throws UnexpectedBehaviourException {
 		try {
 			return emitter.generate(new NullProgressMonitor(), input).trim();
 		} catch (InterruptedException ie) {
 			return null;
 		} catch (InvocationTargetException ite) {
 			throw new UnexpectedBehaviourException(ite.getCause());
 		}
 	}
 
 	/**
 	 * Returns qualified class name defined in template.
 	 */
 	private String getQualifiedClassName(String templateName, Object... input) throws UnexpectedBehaviourException {
 		TextEmitter emitter = getQualifiedClassNameEmitter(templateName);
 		return getText(emitter, input);
 	}
 
 	private BinaryEmitter newGIFEmitter(String relativePath) throws UnexpectedBehaviourException {
 		return new GIFEmitter(checkTemplateLocation(relativePath));
 	}
 
 	private BinaryEmitter newGIFEmitterAdapter(String relativePath) throws UnexpectedBehaviourException {
 		return new JETGIFEmitterAdapter(new org.eclipse.emf.codegen.util.GIFEmitter(checkTemplateLocation(relativePath)));
 	}
 
 	private String checkTemplateLocation(String relativePath) throws UnexpectedBehaviourException {
 		String[] templatesPath = new String[myLocations.length];
 		for (int i = 0; i < myLocations.length; i++) {
 			templatesPath[i] = myLocations[i].toString();
 		}
 		String templateLocation = JETCompiler.find(templatesPath, relativePath);
 		if (templateLocation == null) {
 			throw new UnexpectedBehaviourException("Template " + relativePath + " not found");
 		}
 		return templateLocation;
 	}
 
 	protected TextEmitter newXpandEmitter(String definition) {
 		return new XpandTextEmitter(myResourceManager, definition, myGlobals);
 	}
 }
