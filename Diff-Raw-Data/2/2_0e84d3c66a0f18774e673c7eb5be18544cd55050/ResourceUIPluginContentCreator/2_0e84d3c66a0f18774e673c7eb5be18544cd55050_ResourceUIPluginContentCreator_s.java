 /*******************************************************************************
  * Copyright (c) 2006-2011
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany 
  *      - initial API and implementation
  ******************************************************************************/
 package org.emftext.sdk.codegen.resource.ui.creators;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.emftext.sdk.IPluginDescriptor;
 import org.emftext.sdk.OptionManager;
 import org.emftext.sdk.codegen.ArtifactDescriptor;
 import org.emftext.sdk.codegen.IArtifactCreator;
 import org.emftext.sdk.codegen.creators.BuildPropertiesCreator;
 import org.emftext.sdk.codegen.creators.DotClasspathCreator;
 import org.emftext.sdk.codegen.creators.DotProjectCreator;
 import org.emftext.sdk.codegen.creators.FileCopier;
 import org.emftext.sdk.codegen.creators.FoldersCreator;
 import org.emftext.sdk.codegen.creators.ManifestCreator;
 import org.emftext.sdk.codegen.creators.PluginXMLCreator;
 import org.emftext.sdk.codegen.parameters.ArtifactParameter;
 import org.emftext.sdk.codegen.parameters.BuildPropertiesParameters;
 import org.emftext.sdk.codegen.parameters.ClassPathParameters;
 import org.emftext.sdk.codegen.parameters.DotProjectParameters;
 import org.emftext.sdk.codegen.parameters.ManifestParameters;
 import org.emftext.sdk.codegen.parameters.XMLParameters;
 import org.emftext.sdk.codegen.parameters.xml.XMLElement;
 import org.emftext.sdk.codegen.resource.GenerationContext;
 import org.emftext.sdk.codegen.resource.GeneratorUtil;
 import org.emftext.sdk.codegen.resource.TextResourceArtifacts;
 import org.emftext.sdk.codegen.resource.creators.AbstractPluginCreator;
 import org.emftext.sdk.codegen.resource.creators.SyntaxArtifactCreator;
 import org.emftext.sdk.codegen.resource.ui.TextResourceUIArtifacts;
 import org.emftext.sdk.codegen.resource.ui.UIConstants;
 import org.emftext.sdk.concretesyntax.ConcreteSyntax;
 import org.emftext.sdk.concretesyntax.OptionTypes;
 import org.emftext.sdk.util.ConcreteSyntaxUtil;
 
 public class ResourceUIPluginContentCreator extends AbstractPluginCreator<Object> {
 
 	private ConcreteSyntaxUtil csUtil = new ConcreteSyntaxUtil();
 	private GeneratorUtil genUtil = new GeneratorUtil();
 	
 	@Override
 	public String getPluginName() {
 		return "resource.ui";
 	}
 
 	@Override
 	public List<IArtifactCreator<GenerationContext>> getCreators(
 			GenerationContext context) {
 		IPluginDescriptor resourceUIPlugin = context.getResourceUIPlugin();
 		final ConcreteSyntax syntax = context.getConcreteSyntax();
 		
 		List<IArtifactCreator<GenerationContext>> creators = new ArrayList<IArtifactCreator<GenerationContext>>();
 
 	    creators.add(new FoldersCreator<GenerationContext>(new File[] {
 	    		context.getSourceFolder(resourceUIPlugin, false),
 	    		context.getSourceFolder(resourceUIPlugin, true),
 	    		getCSSDir(resourceUIPlugin, context)
 	    }));
 
 	    ClassPathParameters<GenerationContext> cpp = new ClassPathParameters<GenerationContext>(TextResourceUIArtifacts.DOT_CLASSPATH, resourceUIPlugin);
 		String sourceFolderName = csUtil.getSourceFolderName(context.getConcreteSyntax(), OptionTypes.UI_SOURCE_FOLDER);
 		String sourceGenFolderName = csUtil.getSourceFolderName(context.getConcreteSyntax(), OptionTypes.UI_SOURCE_GEN_FOLDER);
 		
 		cpp.getSourceFolders().add(sourceFolderName);
 		cpp.getSourceFolders().add(sourceGenFolderName);
 	    creators.add(new DotClasspathCreator<GenerationContext>(cpp, doOverride(syntax, TextResourceUIArtifacts.DOT_CLASSPATH)));
 	    
 	    DotProjectParameters<GenerationContext> dpp = new DotProjectParameters<GenerationContext>(TextResourceUIArtifacts.DOT_PROJECT, resourceUIPlugin);
 	    creators.add(new DotProjectCreator<GenerationContext>(dpp, doOverride(syntax, TextResourceUIArtifacts.DOT_PROJECT)));
 
 		ArtifactDescriptor<GenerationContext, BuildPropertiesParameters<GenerationContext>> buildProperties = TextResourceUIArtifacts.BUILD_PROPERTIES;
 
 		BuildPropertiesParameters<GenerationContext> bpp = new BuildPropertiesParameters<GenerationContext>(buildProperties, resourceUIPlugin);
 	    bpp.getSourceFolders().add(sourceFolderName + "/");
 		bpp.getSourceFolders().add(sourceGenFolderName + "/");
 		bpp.getBinIncludes().add("META-INF/");
 		bpp.getBinIncludes().add(".");
 		bpp.getBinIncludes().add("icons/");
 		bpp.getBinIncludes().add("css/");
 		bpp.getBinIncludes().add("newProject.zip");
 		bpp.getBinIncludes().add("plugin.xml");
 		creators.add(new BuildPropertiesCreator<GenerationContext>(bpp, doOverride(syntax, buildProperties)));
 
 	    ManifestParameters<GenerationContext> manifestParameters = new ManifestParameters<GenerationContext>(TextResourceUIArtifacts.MANIFEST);
 	    Collection<String> exports = manifestParameters.getExportedPackages();
 		// export the generated packages
 		exports.add(context.getPackageName(TextResourceUIArtifacts.PACKAGE_UI));
 		exports.addAll(OptionManager.INSTANCE.getStringOptionValueAsCollection(syntax, OptionTypes.ADDITIONAL_UI_EXPORTS));
 		manifestParameters.getRequiredBundles().addAll(getRequiredBundles(context));
 		manifestParameters.setPlugin(resourceUIPlugin);
 		manifestParameters.setActivatorClass(context.getQualifiedClassName(TextResourceUIArtifacts.UI_PLUGIN_ACTIVATOR));
 		manifestParameters.setBundleName("EMFText UI Plugin: " + context.getConcreteSyntax().getName());
 		creators.add(new ManifestCreator<GenerationContext>(manifestParameters, OptionManager.INSTANCE.doOverride(context.getConcreteSyntax(), OptionTypes.OVERRIDE_UI_MANIFEST)));
 		
 	    add(creators, TextResourceUIArtifacts.NEW_FILE_WIZARD);
 	    add(creators, TextResourceUIArtifacts.NEW_FILE_WIZARD_PAGE);
 	    creators.add(new FileCopier<GenerationContext>(FileCopier.class.getResourceAsStream("default_new_icon.gif"), getNewIconFile(resourceUIPlugin, context), false));
 	    creators.add(new FileCopier<GenerationContext>(FileCopier.class.getResourceAsStream("default_editor_icon.gif"), getEditorIconFile(resourceUIPlugin, context), false));
 	    creators.add(new FileCopier<GenerationContext>(FileCopier.class.getResourceAsStream("default_occurrence_icon.gif"), getOccurrenceIconFile(resourceUIPlugin, context), false));
 	    if (context.isLaunchSupportEnabled()) {
 		    creators.add(new FileCopier<GenerationContext>(FileCopier.class.getResourceAsStream("default_launch_shortcut_icon.gif"), getLaunchShortcutIconFile(resourceUIPlugin, context), false));
 		    creators.add(new FileCopier<GenerationContext>(FileCopier.class.getResourceAsStream("default_launch_tab_main_icon.gif"), getLaunchTabMainIconFile(resourceUIPlugin, context), false));
 		    creators.add(new FileCopier<GenerationContext>(FileCopier.class.getResourceAsStream("default_launch_type_icon.gif"), getLaunchConfigurationTypeIconFile(resourceUIPlugin, context), false));
 		}
 	    if (context.isDebugSupportEnabled()) {
 		    creators.add(new FileCopier<GenerationContext>(FileCopier.class.getResourceAsStream("default_breakpoint_icon.gif"), getBreakpointIconFile(resourceUIPlugin, context), false));
 		}
 	    creators.add(new FileCopier<GenerationContext>(FileCopier.class.getResourceAsStream("hover_style.css"), getHoverStyleFile(resourceUIPlugin, context), false));
 
 	    // the initial newProject.zip 
 	    creators.add(new FileCopier<GenerationContext>(FileCopier.class.getResourceAsStream("newProject.zip"), getNewProjectFile(resourceUIPlugin, context), false));	    
 	    
 	    // add UI generators
 		add(creators, TextResourceUIArtifacts.HOVER_TEXT_PROVIDER);
 	    add(creators, TextResourceUIArtifacts.ANTLR_TOKEN_HELPER);
 	    add(creators, TextResourceUIArtifacts.BRACKET_SET);
 	    add(creators, TextResourceUIArtifacts.BROWSER_INFORMATION_CONTROL);
 	    add(creators, TextResourceUIArtifacts.CODE_FOLDING_MANAGER);
 	    add(creators, TextResourceUIArtifacts.COLOR_MANAGER);
 	    add(creators, TextResourceUIArtifacts.COMPLETION_PROCESSOR);
 	    add(creators, TextResourceUIArtifacts.BACKGROUND_PARSING_STRATEGY);
 	    add(creators, TextResourceUIArtifacts.DOC_BROWSER_INFORMATION_CONTROL_INPUT);
 	    add(creators, TextResourceUIArtifacts.EDITOR_CONFIGURATION);
 	    add(creators, TextResourceUIArtifacts.EDITOR);
 	    add(creators, TextResourceUIArtifacts.E_OBJECT_SELECTION);
 	    add(creators, TextResourceUIArtifacts.HIGHLIGHTING);
 	    add(creators, TextResourceUIArtifacts.HTML_PRINTER);
 	    add(creators, TextResourceUIArtifacts.HYPERLINK);
 	    add(creators, TextResourceUIArtifacts.HYPERLINK_DETECTOR);
 	    add(creators, TextResourceUIArtifacts.OCCURRENCE);
 	    add(creators, TextResourceUIArtifacts.OUTLINE_PAGE);
 	    add(creators, TextResourceUIArtifacts.OUTLINE_PAGE_TREE_VIEWER);
 	    add(creators, TextResourceUIArtifacts.POSITION_CATEGORY);
 	    add(creators, TextResourceUIArtifacts.POSITION_HELPER);
 	    add(creators, TextResourceUIArtifacts.PROPERTY_SHEET_PAGE);
 	    add(creators, TextResourceUIArtifacts.TEXT_HOVER);
 	    add(creators, TextResourceUIArtifacts.TOKEN_SCANNER);
 	    add(creators, TextResourceUIArtifacts.DEFAULT_HOVER_TEXT_PROVIDER);
 	    add(creators, TextResourceUIArtifacts.NEW_PROJECT_WIZARD);
 	    
 	    add(creators, TextResourceUIArtifacts.PREFERENCE_PAGE);
 	    add(creators, TextResourceUIArtifacts.BRACKET_PREFERENCE_PAGE);
 	    add(creators, TextResourceUIArtifacts.PREFERENCE_CONSTANTS);
 	    add(creators, TextResourceUIArtifacts.OCCURRENCE_PREFERENCE_PAGE);
 	    add(creators, TextResourceUIArtifacts.PIXEL_CONVERTER);
 	    add(creators, TextResourceUIArtifacts.PREFERENCE_INITIALIZER);
 	    add(creators, TextResourceUIArtifacts.SYNTAX_COLORING_HELPER);
 	    add(creators, TextResourceUIArtifacts.SYNTAX_COLORING_PREFERENCE_PAGE);
 
 	    add(creators, TextResourceUIArtifacts.CODE_COMPLETION_HELPER);
 	    add(creators, TextResourceUIArtifacts.PROPOSAL_POST_PROCESSOR);
 	    add(creators, TextResourceUIArtifacts.COMPLETION_PROPOSAL);
 	    add(creators, TextResourceUIArtifacts.UI_META_INFORMATION);
 	    add(creators, TextResourceUIArtifacts.UI_PLUGIN_ACTIVATOR);
 	    add(creators, TextResourceUIArtifacts.I_ANNOTATION_MODEL_PROVIDER);
 	    add(creators, TextResourceUIArtifacts.I_BACKET_HANDLER);
 	    add(creators, TextResourceUIArtifacts.I_BACKET_HANDLER_PROVIDER);
 
 	    add(creators, TextResourceUIArtifacts.ANNOTATION_MODEL);
 	    add(creators, TextResourceUIArtifacts.ANNOTATION_MODEL_FACTORY);
 	    add(creators, TextResourceUIArtifacts.MARKER_ANNOTATION);
 	    add(creators, TextResourceUIArtifacts.MARKER_RESOLUTION_GENERATOR);
 	    add(creators, TextResourceUIArtifacts.QUICK_ASSIST_ASSISTANT);
 	    add(creators, TextResourceUIArtifacts.QUICK_ASSIST_PROCESSOR);
 	    add(creators, TextResourceUIArtifacts.IMAGE_PROVIDER);
 	    
 	    add(creators, TextResourceUIArtifacts.LAUNCH_CONFIGURATION_MAIN_TAB);
 	    add(creators, TextResourceUIArtifacts.LAUNCH_CONFIGURATION_TAB_GROUP);
 	    add(creators, TextResourceUIArtifacts.LAUNCH_SHORTCUT);
 	    add(creators, TextResourceUIArtifacts.PROPERTY_TESTER);
 
 	    add(creators, TextResourceUIArtifacts.DEBUG_MODEL_PRESENTATION);
 	    add(creators, TextResourceUIArtifacts.LINE_BREAKPOINT_ADAPTER);
 	    add(creators, TextResourceUIArtifacts.ADAPTER_FACTORY);
 
 		ArtifactDescriptor<GenerationContext, XMLParameters<GenerationContext>> pluginXML = TextResourceUIArtifacts.PLUGIN_XML;
 	    creators.add(new PluginXMLCreator<GenerationContext>(getPluginXmlParamters(context), doOverride(syntax, pluginXML)));
 	    return creators;
 	}
 
 	private XMLParameters<GenerationContext> getPluginXmlParamters(GenerationContext context) {
 		final String newFileWizardCategoryID = "org.emftext.runtime.ui.EMFTextFileCategory";
 		final String newProjectWizardCategoryID = "org.emftext.runtime.ui.EMFTextProjectCategory";
 		final ConcreteSyntax concreteSyntax = context.getConcreteSyntax();
 		final String primaryConcreteSyntaxName = getPrimarySyntaxName(concreteSyntax);
 		final IPluginDescriptor resourcePlugin = context.getResourcePlugin();
 		final IPluginDescriptor resourceUIPlugin = context.getResourceUIPlugin();
 		final String resourcePluginID = resourcePlugin.getName();
 		final String uiPluginID = resourceUIPlugin.getName();
 		final String uiMetaInformationClass = context.getQualifiedClassName(TextResourceUIArtifacts.UI_META_INFORMATION);
 		final String newFileWizard = context.getQualifiedClassName(TextResourceUIArtifacts.NEW_FILE_WIZARD);
 		final String newProjectWizard = context.getQualifiedClassName(TextResourceUIArtifacts.NEW_PROJECT_WIZARD);
 		final String problemID = resourcePluginID + ".problem";
 		final String occurrenceAnnotationTypeID = context.getOccurrenceAnnotationTypeID();
 		final String declarationAnnotationTypeID = context.getDeclarationAnnotationTypeID();
 		final String markerResolutionGeneratorClassName = context.getQualifiedClassName(TextResourceUIArtifacts.MARKER_RESOLUTION_GENERATOR);
 		final String annotationModelFactoryClassName = context.getQualifiedClassName(TextResourceUIArtifacts.ANNOTATION_MODEL_FACTORY);
 
 		XMLElement root = new XMLElement("plugin");
 
 		XMLElement uiAccessExtension = root.createChild("extension");
 		uiAccessExtension.setAttribute("point", "org.emftext.access.syntax.ui");
 		XMLElement informationProvider = uiAccessExtension.createChild("metaInformationProvider");
 		informationProvider.setAttribute("class", uiMetaInformationClass);
 		informationProvider.setAttribute("id", uiMetaInformationClass);
 
 		String editorClassName = context.getQualifiedClassName(TextResourceUIArtifacts.EDITOR);
 
 		// registers the editor itself
 		XMLElement editorExtension = root.createChild("extension");
 		editorExtension.setAttribute("point", "org.eclipse.ui.editors");
 		XMLElement editor = editorExtension.createChild("editor");
 		editor.setAttribute("class", editorClassName);
 		editor.setAttribute("contributorClass", "org.eclipse.ui.texteditor.BasicTextEditorActionContributor");
 		editor.setAttribute("extensions", primaryConcreteSyntaxName);
 		editor.setAttribute("icon", "icons/" + UIConstants.DEFAULT_EDITOR_ICON_NAME);
 		editor.setAttribute("id", editorClassName);
 		editor.setAttribute("name", "EMFText " + concreteSyntax.getName() + " Editor");
 		
 		XMLElement contentTypeBinding = editor.createChild("contentTypeBinding");
 		contentTypeBinding.setAttribute("contentTypeId", resourcePluginID);
 
 		XMLElement preferencesExtension = root.createChild("extension");
 		preferencesExtension.setAttribute("point", "org.eclipse.core.runtime.preferences");
 		XMLElement initializer = preferencesExtension.createChild("initializer");
 		initializer.setAttribute("class", context.getQualifiedClassName(TextResourceUIArtifacts.PREFERENCE_INITIALIZER));
 		
 		XMLElement preferencePageExtension = root.createChild("extension");
 		preferencePageExtension.setAttribute("point", "org.eclipse.ui.preferencePages");
 		//main page
 		XMLElement mainPage = preferencePageExtension.createChild("page");
 		mainPage.setAttribute("name", context.getCapitalizedConcreteSyntaxName() + " Text Editor");
 		mainPage.setAttribute("id", context.getQualifiedClassName(TextResourceUIArtifacts.PREFERENCE_PAGE));
 		mainPage.setAttribute("class", context.getQualifiedClassName(TextResourceUIArtifacts.PREFERENCE_PAGE));
 		mainPage.setAttribute("category", "org.eclipse.ui.preferencePages.GeneralTextEditor");
 
 		//sub pages
 		XMLElement syntaxPage = preferencePageExtension.createChild("page");
 		syntaxPage.setAttribute("name", "Syntax Coloring");
 		syntaxPage.setAttribute("id", context.getQualifiedClassName(TextResourceUIArtifacts.SYNTAX_COLORING_PREFERENCE_PAGE));
 		syntaxPage.setAttribute("class", context.getQualifiedClassName(TextResourceUIArtifacts.SYNTAX_COLORING_PREFERENCE_PAGE));
 		syntaxPage.setAttribute("category", context.getQualifiedClassName(TextResourceUIArtifacts.PREFERENCE_PAGE));
 		
 		XMLElement bracketPage = preferencePageExtension.createChild("page");
 		bracketPage.setAttribute("name", "Brackets");
 		bracketPage.setAttribute("id", context.getQualifiedClassName(TextResourceUIArtifacts.BRACKET_PREFERENCE_PAGE));
 		bracketPage.setAttribute("class", context.getQualifiedClassName(TextResourceUIArtifacts.BRACKET_PREFERENCE_PAGE));
 		bracketPage.setAttribute("category", context.getQualifiedClassName(TextResourceUIArtifacts.PREFERENCE_PAGE));
 
 		XMLElement newWizardExtension = root.createChild("extension");
 		newWizardExtension.setAttribute("point", "org.eclipse.ui.newWizards");
 		
 		XMLElement newFilesCategory = newWizardExtension.createChild("category");
 		newFilesCategory.setAttribute("id", newFileWizardCategoryID);
 		newFilesCategory.setAttribute("name", "EMFText File");
 		XMLElement wizardFile = newWizardExtension.createChild("wizard");
 		wizardFile.setAttribute("category", newFileWizardCategoryID);
 		wizardFile.setAttribute("icon", getProjectRelativeNewIconPath());
 		wizardFile.setAttribute("class", newFileWizard);
 		wizardFile.setAttribute("id", newFileWizard);
 		wizardFile.setAttribute("name", "EMFText ." + context.getConcreteSyntax().getName() + " file");
 		
		if (OptionManager.INSTANCE.getBooleanOptionValue(concreteSyntax, OptionTypes.DISABLE_NEW_PROJECT_WIZARD)) {
 			XMLElement newProjectsCategory = newWizardExtension.createChild("category");
 			newProjectsCategory.setAttribute("id", newProjectWizardCategoryID);
 			newProjectsCategory.setAttribute("name", "EMFText Project");
 			XMLElement wizardProject = newWizardExtension.createChild("wizard");
 			wizardProject.setAttribute("category", newProjectWizardCategoryID);
 			wizardProject.setAttribute("icon", getProjectRelativeNewIconPath());
 			wizardProject.setAttribute("class", newProjectWizard);
 			wizardProject.setAttribute("id", newProjectWizard);
 			wizardProject.setAttribute("name", "EMFText " + context.getConcreteSyntax().getName() + " project");
 			wizardProject.setAttribute("project", "true");
 		}
 		
 		XMLElement markerResolutionExtension = root.createChild("extension");
 		markerResolutionExtension.setAttribute("point", "org.eclipse.ui.ide.markerResolution");
 		XMLElement markerResolutionGenerator = markerResolutionExtension.createChild("markerResolutionGenerator");
 		markerResolutionGenerator.setAttribute("class", markerResolutionGeneratorClassName);
 		markerResolutionGenerator.setAttribute("markerType", problemID);
 		
 		XMLElement annotationModelExtension = root.createChild("extension");
 		annotationModelExtension.setAttribute("point", "org.eclipse.core.filebuffers.annotationModelCreation");
 		XMLElement factory = annotationModelExtension.createChild("factory");
 		factory.setAttribute("class", annotationModelFactoryClassName);
 		factory.setAttribute("extensions", primaryConcreteSyntaxName);
 		
 		XMLElement contentTypeExtension = root.createChild("extension");
 		contentTypeExtension.setAttribute("point", "org.eclipse.core.contenttype.contentTypes");
 		XMLElement contentType = contentTypeExtension.createChild("content-type");
 		contentType.setAttribute("id", resourcePluginID);
 		contentType.setAttribute("name", "." + primaryConcreteSyntaxName + " File");
 		contentType.setAttribute("base-type", "org.eclipse.core.runtime.text");
 		contentType.setAttribute("file-extensions", primaryConcreteSyntaxName);
 		
 		XMLElement documentProviderExtension = root.createChild("extension");
 		documentProviderExtension.setAttribute("point", "org.eclipse.ui.editors.documentProviders");
 		XMLElement provider = documentProviderExtension.createChild("provider");
 		provider.setAttribute("class", "org.eclipse.ui.editors.text.TextFileDocumentProvider");
 		provider.setAttribute("extensions", primaryConcreteSyntaxName);
 		provider.setAttribute("id", uiPluginID + ".provider");
 		
 		XMLElement annotationTypeExtension = root.createChild("extension");
 		annotationTypeExtension.setAttribute("point", "org.eclipse.ui.editors.annotationTypes");
 		XMLElement occurrenceType = annotationTypeExtension.createChild("type");
 		occurrenceType.setAttribute("name", occurrenceAnnotationTypeID);
 
 		XMLElement declarationAnnotationType = annotationTypeExtension.createChild("type");
 		declarationAnnotationType.setAttribute("name", declarationAnnotationTypeID);
 		declarationAnnotationType.setAttribute("super", occurrenceAnnotationTypeID);
 
 		final String syntaxName = context.getConcreteSyntax().getName();
 		XMLElement markerAnnotationExtension = root.createChild("extension");
 		markerAnnotationExtension.setAttribute("point", "org.eclipse.ui.editors.markerAnnotationSpecification");
 
 		XMLElement specification = markerAnnotationExtension.createChild("specification");
 		specification.setAttribute("annotationType", occurrenceAnnotationTypeID);
 		specification.setAttribute("label", "Occurrences (in ." + syntaxName + " files)");
 		specification.setAttribute("icon", "/icons/" + UIConstants.DEFAULT_OCCURRENCE_ICON_NAME);
 		specification.setAttribute("textPreferenceKey", syntaxName + ".occurrenceIndication");
 		specification.setAttribute("textPreferenceValue", "false");
 		specification.setAttribute("highlightPreferenceKey", syntaxName + ".occurrenceHighlighting");
 		specification.setAttribute("highlightPreferenceValue", "true");
 		specification.setAttribute("contributesToHeader", "false");
 		specification.setAttribute("overviewRulerPreferenceKey", syntaxName + ".occurrenceIndicationInOverviewRuler");
 		specification.setAttribute("overviewRulerPreferenceValue", "true");
 		specification.setAttribute("verticalRulerPreferenceKey", syntaxName + ".occurrenceIndicationInVerticalRuler");
 		specification.setAttribute("verticalRulerPreferenceValue", "false");
 		specification.setAttribute("colorPreferenceKey", syntaxName + ".occurrenceIndicationColor");
 		specification.setAttribute("colorPreferenceValue", "212,212,212");
 		specification.setAttribute("presentationLayer", "4");
 		specification.setAttribute("showInNextPrevDropdownToolbarActionKey", syntaxName + ".showOccurrenceInNextPrevDropdownToolbarAction");
 		specification.setAttribute("showInNextPrevDropdownToolbarAction", "true");
 		specification.setAttribute("isGoToNextNavigationTargetKey", syntaxName + ".isOccurrenceGoToNextNavigationTarget");
 		specification.setAttribute("isGoToNextNavigationTarget", "false");
 		specification.setAttribute("isGoToPreviousNavigationTargetKey", syntaxName + ".isOccurrenceGoToPreviousNavigationTarget");
 		specification.setAttribute("isGoToPreviousNavigationTarget" , "false");
 		specification.setAttribute("textStylePreferenceKey", syntaxName + ".occurrenceTextStyle");
 		specification.setAttribute("textStylePreferenceValue", "NONE");
 
 		XMLElement specification2 = markerAnnotationExtension.createChild("specification");
 		specification2.setAttribute("annotationType", declarationAnnotationTypeID);
 		specification2.setAttribute("label", "Declarations (in ." + syntaxName + " files)");
 		specification2.setAttribute("textPreferenceKey", syntaxName + ".declarationIndication");
 		specification2.setAttribute("textPreferenceValue", "false");
 		specification2.setAttribute("highlightPreferenceKey", syntaxName + ".declarationHighlighting");
 		specification2.setAttribute("highlightPreferenceValue", "true");
 		specification2.setAttribute("overviewRulerPreferenceKey", syntaxName + ".declarationIndicationInOverviewRuler");
 		specification2.setAttribute("overviewRulerPreferenceValue", "true");
 		specification2.setAttribute("verticalRulerPreferenceKey", syntaxName + ".declarationIndicationInVerticalRuler");
 		specification2.setAttribute("verticalRulerPreferenceValue", "false");
 		specification2.setAttribute("colorPreferenceKey", syntaxName + ".declarationIndicationColor");
 		specification2.setAttribute("colorPreferenceValue", "240,216,168");
 		specification2.setAttribute("presentationLayer", "4");
 		specification2.setAttribute("textStylePreferenceKey", syntaxName + ".declarationTextStyle");
 		specification2.setAttribute("textStylePreferenceValue", "NONE");
 		
 		if (context.isGenerateTestActionEnabled()) {
 			root.addChild(generateTestActionExtension(context));
 		}
 
 		if (context.isLaunchSupportEnabled()) {
 			root.addChild(generateLaunchConfigurationTypeImageExtension(context));
 			root.addChild(generateLaunchTabGroup(context));
 			root.addChild(generateLaunchShortcutExtension(context));
 			root.addChild(generatePropertyTesterExtension(context));
 		}
 		if (context.isDebugSupportEnabled()) {
 			root.addChild(generateBreakpointAnnotationTypeExtension(context));
 			root.addChild(generateBreakpointMarkerAnnotationSpecificationExtension(context));
 			root.addChild(generateAdapterFactoryExtension(context));
 			root.addChild(generateRulerPopupExtension(context));
 			root.addChild(generateBreakpointRuleActionExtension(context));
 			root.addChild(generateDebugModelPresentationExtension(context));
 		}
 
 		XMLParameters<GenerationContext> parameters = new XMLParameters<GenerationContext>(TextResourceArtifacts.PLUGIN_XML, resourceUIPlugin, root);
 		return parameters;
 	}
 
 	private XMLElement generateDebugModelPresentationExtension(GenerationContext context) {
 		String debugModelPresentationClassName = context.getQualifiedClassName(TextResourceUIArtifacts.DEBUG_MODEL_PRESENTATION);
 		
 		XMLElement extension = new XMLElement("extension");
 		extension.setAttribute("point", "org.eclipse.debug.ui.debugModelPresentations");
 		XMLElement debugModelPresentation = extension.createChild("debugModelPresentation");
 		debugModelPresentation.setAttribute("id", context.getDebugModelID());
 		debugModelPresentation.setAttribute("class", debugModelPresentationClassName);
 		return extension;
 	}
 
 	private XMLElement generateBreakpointRuleActionExtension(GenerationContext context) {
 		String editorID = context.getQualifiedClassName(TextResourceUIArtifacts.EDITOR);
 		String uiPluginName = context.getResourceUIPlugin().getName();
 
 		XMLElement extension = new XMLElement("extension");
 		extension.setAttribute("point", "org.eclipse.ui.editorActions");
 		XMLElement editorContribution = extension.createChild("editorContribution");
 		editorContribution.setAttribute("targetID", editorID);
 		editorContribution.setAttribute("id", uiPluginName + ".BreakpointRulerActions");
 		XMLElement action = editorContribution.createChild("action");
 		action.setAttribute("id", uiPluginName + ".debug.RulerToggleBreakpointAction");
 		action.setAttribute("label", "Add breakpoint");
 		action.setAttribute("class", "org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate");
 		action.setAttribute("actionID", "RulerDoubleClick");
 		return extension;
 	}
 
 	private XMLElement generateRulerPopupExtension(GenerationContext context) {
 		String uiPluginName = context.getResourceUIPlugin().getName();
 		
 		XMLElement extension = new XMLElement("extension");
 		extension.setAttribute("point", "org.eclipse.ui.popupMenus");
 		XMLElement viewerContribution = extension.createChild("viewerContribution");
 		viewerContribution.setAttribute("id", uiPluginName + ".RulerPopupActions");
 		viewerContribution.setAttribute("targetID", context.getEditorRulerID());
 		
 		XMLElement action = viewerContribution.createChild("action"); 
 		action.setAttribute("id", uiPluginName + ".toggleBreakpointAction"); 
 		action.setAttribute("icon", "icons/breakpoint_icon.gif");
 		// TODO use %key for label
 		action.setAttribute("label", "Toggle Breakpoint");
 		action.setAttribute("class", "org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate");
 		action.setAttribute("menubarPath", "debug");
 		return extension;
 	}
 
 	private XMLElement generateAdapterFactoryExtension(GenerationContext context) {
 		String editorClassName = context.getQualifiedClassName(TextResourceUIArtifacts.EDITOR);
 		String adapterFactoryClassName = context.getQualifiedClassName(TextResourceUIArtifacts.ADAPTER_FACTORY);
 		String debugVariableClassName = context.getQualifiedClassName(TextResourceArtifacts.DEBUG_VARIABLE);
 		
 		XMLElement extension = new XMLElement("extension");
 		extension.setAttribute("point", "org.eclipse.core.runtime.adapters");
 		XMLElement factory1 = extension.createChild("factory");
 		// register generated editor
 		factory1.setAttribute("adaptableType", editorClassName);
 		factory1.setAttribute("class", adapterFactoryClassName);
 		XMLElement adapter1 = factory1.createChild("adapter");
 		adapter1.setAttribute("type", "org.eclipse.debug.ui.actions.IToggleBreakpointsTarget");
 
 		XMLElement factory2 = extension.createChild("factory");
 		factory2.setAttribute("adaptableType", debugVariableClassName);
 		factory2.setAttribute("class", adapterFactoryClassName);
 		XMLElement adapter2 = factory2.createChild("adapter");
 		adapter2.setAttribute("type", "org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider");
 
 		XMLElement factory3 = extension.createChild("factory");
 		factory3.setAttribute("adaptableType", debugVariableClassName);
 		factory3.setAttribute("class", adapterFactoryClassName);
 		XMLElement adapter3 = factory3.createChild("adapter");
 		adapter3.setAttribute("type", "org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider");
 		
 		return extension;
 	}
 
 	private XMLElement generateBreakpointMarkerAnnotationSpecificationExtension(GenerationContext context) {
 		String breakpointAnnotationTypeID = context.getLineBreakpointAnnotationID();
 		String syntaxName = context.getConcreteSyntax().getName();
 		String uiPluginName = context.getResourceUIPlugin().getName();
 
 		XMLElement extension = new XMLElement("extension");
 		extension.setAttribute("point", "org.eclipse.ui.editors.markerAnnotationSpecification");
 		XMLElement specification = extension.createChild("specification"); 
 		specification.setAttribute("annotationType", breakpointAnnotationTypeID); 
 		specification.setAttribute("label", "Breakpoints (in ." + syntaxName + " files)");
 		specification.setAttribute("icon", "/icons/breakpoint_icon.gif"); 
 		specification.setAttribute("textPreferenceKey", uiPluginName + ".lineBreakpoint"); 
 		specification.setAttribute("textPreferenceValue", "false"); 
 		specification.setAttribute("highlightPreferenceKey", uiPluginName + ".lineBreakpointHighlighting"); 
 		specification.setAttribute("highlightPreferenceValue", "true"); 
 		specification.setAttribute("contributesToHeader", "false"); 
 		specification.setAttribute("overviewRulerPreferenceKey", uiPluginName + ".lineBreakpointIndicationInOverviewRuler"); 
 		specification.setAttribute("overviewRulerPreferenceValue", "true"); 
 		specification.setAttribute("verticalRulerPreferenceKey", uiPluginName + ".lineBreakpointIndicationInVerticalRuler"); 
 		specification.setAttribute("verticalRulerPreferenceValue", "false"); 
 		specification.setAttribute("colorPreferenceKey", uiPluginName + ".lineBreakpointColor"); 
 		specification.setAttribute("colorPreferenceValue", "212,212,212"); 
 		specification.setAttribute("presentationLayer", "4"); 
 		specification.setAttribute("showInNextPrevDropdownToolbarActionKey", uiPluginName + ".showLineBreakpointInNextPrevDropdownToolbarAction"); 
 		specification.setAttribute("showInNextPrevDropdownToolbarAction", "true"); 
 		specification.setAttribute("isGoToNextNavigationTargetKey", uiPluginName + ".isLineBreakpointGoToNextNavigationTarget"); 
 		specification.setAttribute("isGoToNextNavigationTarget", "false"); 
 		specification.setAttribute("isGoToPreviousNavigationTargetKey", uiPluginName + ".isLineBreakpointGoToPreviousNavigationTarget"); 
 		specification.setAttribute("isGoToPreviousNavigationTarget", "false"); 
 		specification.setAttribute("textStylePreferenceKey", uiPluginName + ".lineBreakpointTextStyle"); 
 		specification.setAttribute("textStylePreferenceValue", "NONE");
 		return extension;
 	}
 
 	private XMLElement generateBreakpointAnnotationTypeExtension(GenerationContext context) {
 		String breakpointAnnotationTypeID = context.getLineBreakpointAnnotationID();
 
 		XMLElement extension = new XMLElement("extension");
 		extension.setAttribute("point", "org.eclipse.ui.editors.annotationTypes");
 		XMLElement type = extension.createChild("type");
 		type.setAttribute("name", breakpointAnnotationTypeID);
 		type.setAttribute("markerType", context.getLineBreakpointMarkerID());
 		type.setAttribute("super", "org.eclipse.debug.core.breakpoint");
 		return extension;
 	}
 
 	private String getPrimarySyntaxName(ConcreteSyntax concreteSyntax) {
 		 String fullConcreteSyntaxName = concreteSyntax.getName();
 		 int idx = fullConcreteSyntaxName.lastIndexOf(".");
 		 if (idx != -1) {
 			 return fullConcreteSyntaxName.substring(idx + 1);
 		 }
 		 return fullConcreteSyntaxName;
 	}
 	
 	private XMLElement generateTestActionExtension(GenerationContext context) {
 
 		final ConcreteSyntax concreteSyntax = context.getConcreteSyntax();
 		final String concreteSyntaxName = concreteSyntax.getName();
 		final String baseId = getBaseID(concreteSyntax);
 
 		XMLElement popupMenuExtension = new XMLElement("extension");
 		popupMenuExtension.setAttribute("point", "org.eclipse.ui.popupMenus");
 		XMLElement objectContribution = popupMenuExtension.createChild("objectContribution");
 		objectContribution.setAttribute("id", baseId + ".contributions");
 		objectContribution.setAttribute("objectClass", "org.eclipse.core.resources.IFile");
 		objectContribution.setAttribute("nameFilter", "*." + concreteSyntaxName);
 		XMLElement action = objectContribution.createChild("action");
 		action.setAttribute("class", "org.emftext.sdk.ui.actions.ValidateParserPrinterAction");
 		action.setAttribute("enablesFor", "1");
 		action.setAttribute("id", baseId + ".validate");
 		action.setAttribute("label", "Validate");
 		action.setAttribute("menubarPath", "org.emftext.sdk.ui.menu1/group1");
 		return popupMenuExtension;
 	}
 
 	private String getBaseID(final ConcreteSyntax concreteSyntax) {
 		final String concreteSyntaxName = concreteSyntax.getName();
 		final GenPackage concreteSyntaxPackage = concreteSyntax.getPackage();
 		final String concreteSyntaxBasePackage = concreteSyntaxPackage.getBasePackage();
 		final String baseId = (concreteSyntaxBasePackage == null ? ""
 				: concreteSyntaxBasePackage + ".")
 				+ concreteSyntaxName;
 		return baseId;
 	}
 
 	private XMLElement generateLaunchShortcutExtension(GenerationContext context) {
 		final ConcreteSyntax syntax = context.getConcreteSyntax();
 		final String launchShortcutClassName = context.getQualifiedClassName(TextResourceUIArtifacts.LAUNCH_SHORTCUT);
 		final String baseId = getBaseID(syntax);
 		final String syntaxName = syntax.getName();
 
 		XMLElement shortCutExtension = new XMLElement("extension");
 		shortCutExtension.setAttribute("point", "org.eclipse.debug.ui.launchShortcuts");
 		
 		XMLElement shortCut = shortCutExtension.createChild("shortcut");
 		// TODO use %key to allow localization of description
 	    shortCut.setAttribute("label", syntaxName + " Application");
 	    shortCut.setAttribute("icon", getProjectRelativeLaunchShortcutIconPath());
 	    shortCut.setAttribute("helpContextId", baseId + ".launch");
 		if (context.isDebugSupportEnabled()) {
 			shortCut.setAttribute("modes", "run,debug");
 		} else {
 			shortCut.setAttribute("modes", "run");
 		}
 		shortCut.setAttribute("class", launchShortcutClassName);
 	    shortCut.setAttribute("description", "Launch a " + syntaxName + " model");
 	    shortCut.setAttribute("id", baseId + ".launchShortcut");
 	    
 		XMLElement runDescription = shortCut.createChild("description");
 		runDescription.setAttribute("description", "Run " + syntaxName + " model");
 		runDescription.setAttribute("mode", "run");
 
 		XMLElement debugDescription = shortCut.createChild("description");
 		debugDescription.setAttribute("description", "Debug " + syntaxName + " model");
 		debugDescription.setAttribute("mode", "debug");
 		
 		XMLElement contextualLaunch = shortCut.createChild("contextualLaunch");
 		XMLElement enablement = contextualLaunch.createChild("enablement");
 		XMLElement with = enablement.createChild("with");
 		with.setAttribute("variable", "selection");
 		XMLElement count = with.createChild("count");
 		count.setAttribute("value", "1");
 
 		XMLElement iterate = with.createChild("iterate");
 		XMLElement test = iterate.createChild("test");
 		test.setAttribute("property", baseId + ".isLaunchable");
 
 		XMLElement configurationType = shortCut.createChild("configurationType");
 		configurationType.setAttribute("id", context.getLaunchConfigurationTypeID());
 
 		return shortCutExtension;
 	}
 	
 	private XMLElement generateLaunchConfigurationTypeImageExtension(GenerationContext context) {
 		final ConcreteSyntax syntax = context.getConcreteSyntax();
 
 		XMLElement extension = new XMLElement("extension");
 		extension.setAttribute("point", "org.eclipse.debug.ui.launchConfigurationTypeImages");
 		
 		XMLElement image = extension.createChild("launchConfigurationTypeImage");
 		image.setAttribute("icon", getProjectRelativeLaunchConfigurationTypeIconPath());
 		image.setAttribute("configTypeID", context.getLaunchConfigurationTypeID());
 		image.setAttribute("id", getBaseID(syntax) + ".launchImage");
 		
 		return extension;
 	}
 	
 	private XMLElement generatePropertyTesterExtension(GenerationContext context) {
 		final ConcreteSyntax syntax = context.getConcreteSyntax();
 		final String baseId = getBaseID(syntax);
 		final String propertyTesterClassName = context.getQualifiedClassName(TextResourceUIArtifacts.PROPERTY_TESTER);
 
 		XMLElement extension = new XMLElement("extension");
 		extension.setAttribute("point", "org.eclipse.core.expressions.propertyTesters");
 		
 		XMLElement propertyTester = extension.createChild("propertyTester");
 	    propertyTester.setAttribute("id", baseId + ".PropertyTester");
 	    propertyTester.setAttribute("type", "java.lang.Object");
 	    propertyTester.setAttribute("namespace", baseId);
 	    propertyTester.setAttribute("properties", "isLaunchable");
 		propertyTester.setAttribute("class", propertyTesterClassName);
 	    
 	    return extension;
 	}
 
 	private XMLElement generateLaunchTabGroup(GenerationContext context) {
 		final ConcreteSyntax syntax = context.getConcreteSyntax();
 		final String launchConfigurationID = context.getLaunchConfigurationTypeID();
 		final String baseId = getBaseID(syntax);
 		final String launchConfigurationTabGroupClassname = context.getQualifiedClassName(TextResourceUIArtifacts.LAUNCH_CONFIGURATION_TAB_GROUP);
 		final String syntaxName = syntax.getName();
 
 		XMLElement launchTabGroupExtension = new XMLElement("extension");
 		launchTabGroupExtension.setAttribute("point", "org.eclipse.debug.ui.launchConfigurationTabGroups");
 
 		XMLElement launchConfigurationTabGroup = launchTabGroupExtension.createChild("launchConfigurationTabGroup");
 		launchConfigurationTabGroup.setAttribute("type", launchConfigurationID);
 		launchConfigurationTabGroup.setAttribute("class", launchConfigurationTabGroupClassname);
 		launchConfigurationTabGroup.setAttribute("id", baseId  + ".launchConfigurationTabGroup");
 		launchConfigurationTabGroup.setAttribute("helpContextId", baseId + ".launchConfigHelpContext");
 		
 		XMLElement debugMode = launchConfigurationTabGroup.createChild("launchMode");
 		debugMode.setAttribute("mode", "debug");
 		debugMode.setAttribute("perspective", "org.eclipse.debug.ui.DebugPerspective");
 		// TODO use %key to allow localization of description
 		debugMode.setAttribute("description", "Debug " + syntaxName + " model.");
 
 		XMLElement runMode = launchConfigurationTabGroup.createChild("launchMode");
 		runMode.setAttribute("mode", "run");
 		// TODO use %key to allow localization of description
 		runMode.setAttribute("description", "Run " + syntaxName + " model.");
 		
 		return launchTabGroupExtension;
 	}
 
 	private String getProjectRelativeNewIconPath() {
 		// it is OK to use slashes here, because this path is put into
 		// the plugin.xml
 		return "/" + UIConstants.DEFAULT_ICON_DIR + "/" + UIConstants.DEFAULT_NEW_ICON_NAME;
 	}
 	
 	private String getProjectRelativeLaunchShortcutIconPath() {
 		// it is OK to use slashes here, because this path is put into
 		// the plugin.xml
 		return "/" + UIConstants.DEFAULT_ICON_DIR + "/" + UIConstants.DEFAULT_LAUNCH_SHORTCUT_ICON_NAME;
 	}
 	
 	private String getProjectRelativeLaunchConfigurationTypeIconPath() {
 		// it is OK to use slashes here, because this path is put into
 		// the plugin.xml
 		return "/" + UIConstants.DEFAULT_ICON_DIR + "/" + UIConstants.DEFAULT_LAUNCH_CONFIGURATION_TYPE_ICON_NAME;
 	}
 	
 	private void add(
 			List<IArtifactCreator<GenerationContext>> creators,
 			ArtifactDescriptor<GenerationContext, ArtifactParameter<GenerationContext>> hoverTextProvider) {
 		creators.add(new SyntaxArtifactCreator<ArtifactParameter<GenerationContext>>(new ArtifactParameter<GenerationContext>(hoverTextProvider)));
 	}
 
 	private boolean doOverride(
 			final ConcreteSyntax syntax,
 			ArtifactDescriptor<GenerationContext, ?> artifact) {
 		return OptionManager.INSTANCE.getBooleanOptionValue(syntax, artifact.getOverrideOption());
 	}
 
 	private Collection<String> getRequiredBundles(GenerationContext context) {
 		ConcreteSyntax syntax = context.getConcreteSyntax();
 		
 		Set<String> imports = new LinkedHashSet<String>();
 		imports.add("org.eclipse.core.resources");
 		imports.add("org.emftext.access;resolution:=optional");
 
 		if (context.isLaunchSupportEnabled() || context.isDebugSupportEnabled()) {
 			imports.add("org.eclipse.core.expressions");
 			imports.add("org.eclipse.debug.core");
 			imports.add("org.eclipse.debug.ui");
 		}
 		
 		imports.add("org.eclipse.emf");
 		imports.add("org.eclipse.emf.codegen.ecore");
 		imports.add("org.eclipse.emf.ecore");
 		imports.add("org.eclipse.emf.ecore.edit");
 		imports.add("org.eclipse.emf.edit.ui");
 		imports.add("org.eclipse.emf.workspace");
 		imports.add("org.eclipse.jface");
 		imports.add("org.eclipse.jface.text");
 		imports.add("org.eclipse.ui");
 		imports.add("org.eclipse.ui.editors");
 		imports.add("org.eclipse.ui.ide");
 		imports.add("org.eclipse.ui.views");
 		imports.add(context.getResourcePlugin().getName());
 		
 		// TODO implement extension mechanism to allow code generation plug-ins to add
 		// more imports here 
 
 		String qualifiedBasePluginName = 
 			OptionManager.INSTANCE.getStringOptionValue(syntax, OptionTypes.BASE_RESOURCE_PLUGIN);
 		if (qualifiedBasePluginName != null) {
 			imports.add(qualifiedBasePluginName);
 		}
 		
 		imports.addAll(OptionManager.INSTANCE.getStringOptionValueAsCollection(syntax, OptionTypes.ADDITIONAL_UI_DEPENDENCIES));
 
 		if (context.isGenerateTestActionEnabled()) {
 			imports.add("org.emftext.sdk.ui");
 		}
 
 		genUtil.addImports(context, imports, syntax);
 		
 		// remove the current plug-in, because we do not
 		// need to import it
 		imports.remove(context.getResourceUIPlugin().getName());
 		
 		return imports;
 	}
 
 	private File getIconsDir(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(context.getFileSystemConnector().getProjectFolder(plugin).getAbsolutePath() + File.separator + UIConstants.DEFAULT_ICON_DIR);
 	}
 
 	private File getNewIconFile(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(getIconsDir(plugin, context).getAbsolutePath() + File.separator + UIConstants.DEFAULT_NEW_ICON_NAME);
 	}
 
 	private File getEditorIconFile(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(getIconsDir(plugin, context).getAbsolutePath() + File.separator + UIConstants.DEFAULT_EDITOR_ICON_NAME);
 	}
 
 	private File getOccurrenceIconFile(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(getIconsDir(plugin, context).getAbsolutePath() + File.separator + UIConstants.DEFAULT_OCCURRENCE_ICON_NAME);
 	}
 
 	private File getLaunchShortcutIconFile(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(getIconsDir(plugin, context).getAbsolutePath() + File.separator + UIConstants.DEFAULT_LAUNCH_SHORTCUT_ICON_NAME);
 	}
 
 	private File getLaunchTabMainIconFile(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(getIconsDir(plugin, context).getAbsolutePath() + File.separator + UIConstants.DEFAULT_LAUNCH_TAB_MAIN_ICON_NAME);
 	}
 
 	private File getBreakpointIconFile(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(getIconsDir(plugin, context).getAbsolutePath() + File.separator + UIConstants.DEFAULT_BREAKPOINT_ICON_NAME);
 	}
 
 	private File getLaunchConfigurationTypeIconFile(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(getIconsDir(plugin, context).getAbsolutePath() + File.separator + UIConstants.DEFAULT_LAUNCH_CONFIGURATION_TYPE_ICON_NAME);
 	}
 
 	private File getCSSDir(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(context.getFileSystemConnector().getProjectFolder(plugin).getAbsolutePath() + File.separator + UIConstants.DEFAULT_CSS_DIR);
 	}
 
 	private File getHoverStyleFile(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(getCSSDir(plugin, context).getAbsolutePath() + File.separator + UIConstants.HOVER_STYLE_FILENAME);
 	}
 	
 	private File getNewProjectFile(IPluginDescriptor plugin, GenerationContext context) {
 		return new File(context.getFileSystemConnector().getProjectFolder(plugin).getAbsolutePath() + File.separator + UIConstants.NEW_PROJECT_FILENAME);
 	}
 }
