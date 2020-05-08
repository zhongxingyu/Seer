 /*******************************************************************************
  * Copyright (c) 2005-2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 /**
  * 
  */
 package org.eclipse.vjet.eclipse.core.test.bug;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.apache.tools.ant.util.FileUtils;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.resources.IWorkspaceDescription;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.mod.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.mod.compiler.problem.ProblemCollector;
 import org.eclipse.dltk.mod.core.DLTKLanguageManager;
 import org.eclipse.dltk.mod.core.IBuildpathEntry;
 import org.eclipse.dltk.mod.core.ICodeAssist;
 import org.eclipse.dltk.mod.core.IModelElement;
 import org.eclipse.dltk.mod.core.IPackageDeclaration;
 import org.eclipse.dltk.mod.core.ISourceModule;
 import org.eclipse.dltk.mod.core.IType;
 import org.eclipse.dltk.mod.core.ModelException;
 import org.eclipse.dltk.mod.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.mod.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.mod.core.search.SearchEngine;
 import org.eclipse.dltk.mod.core.search.SearchPattern;
 import org.eclipse.dltk.mod.core.search.TypeNameMatch;
 import org.eclipse.dltk.mod.debug.ui.breakpoints.BreakpointUtils;
 import org.eclipse.dltk.mod.internal.core.ScriptProject;
 import org.eclipse.dltk.mod.internal.core.builder.BuildProblemReporter;
 import org.eclipse.dltk.mod.internal.ui.actions.refactoring.RefactorActionGroup;
 import org.eclipse.dltk.mod.internal.ui.callhierarchy.SearchUtil;
 import org.eclipse.dltk.mod.internal.ui.editor.EditorUtility;
 import org.eclipse.dltk.mod.internal.ui.filters.CustomFiltersDialog;
 import org.eclipse.dltk.mod.internal.ui.filters.FilterDescriptor;
 import org.eclipse.dltk.mod.internal.ui.scriptview.ScriptExplorerPart;
 import org.eclipse.dltk.mod.internal.ui.search.DLTKSearchScopeFactory;
 import org.eclipse.dltk.mod.internal.ui.search.GroupAction;
 import org.eclipse.dltk.mod.ui.DLTKUILanguageManager;
 import org.eclipse.dltk.mod.ui.IContextMenuConstants;
 import org.eclipse.dltk.mod.ui.editor.ScriptMarkerAnnotation;
 import org.eclipse.dltk.mod.ui.search.ElementQuerySpecification;
 import org.eclipse.dltk.mod.ui.search.ISearchQueryFactory;
 import org.eclipse.dltk.mod.ui.search.QuerySpecification;
 import org.eclipse.dltk.mod.ui.text.IColorManager;
 import org.eclipse.dltk.mod.ui.text.folding.AbstractASTFoldingStructureProvider;
 import org.eclipse.dltk.mod.ui.text.folding.IFoldingStructureProvider;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.SubContributionItem;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.jface.text.source.projection.ProjectionViewer;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.search.ui.ISearchQuery;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.internal.PartSite;
 import org.eclipse.ui.internal.about.AboutAction;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.progress.IProgressService;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeReference;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jstojava.translator.JstUtil;
 import org.eclipse.vjet.dsf.ts.type.TypeName;
 import org.eclipse.vjet.eclipse.codeassist.CodeassistUtils;
 import org.eclipse.vjet.eclipse.core.IImportContainer;
 import org.eclipse.vjet.eclipse.core.IJSSourceModule;
 import org.eclipse.vjet.eclipse.core.IJSType;
 import org.eclipse.vjet.eclipse.core.VjoNature;
 import org.eclipse.vjet.eclipse.core.parser.VjoParserToJstAndIType;
 import org.eclipse.vjet.eclipse.core.search.SearchQueryParameters;
 import org.eclipse.vjet.eclipse.core.search.VjoSearchEngine;
 import org.eclipse.vjet.eclipse.core.test.parser.AbstractVjoModelTests;
 import org.eclipse.vjet.eclipse.core.ts.EclipseTypeLoadMonitor;
 import org.eclipse.vjet.eclipse.internal.parser.VjoSourceParser;
 import org.eclipse.vjet.eclipse.internal.ui.actions.AddVjoNatureAction;
 import org.eclipse.vjet.eclipse.internal.ui.actions.VjoValidationAction;
 import org.eclipse.vjet.eclipse.internal.ui.dialogs.VjoTypeInfoViewer;
 import org.eclipse.vjet.eclipse.internal.ui.editor.VjoEditor;
 import org.eclipse.vjet.eclipse.internal.ui.editor.VjoOutlinePage;
 import org.eclipse.vjet.eclipse.internal.ui.text.VjetColorConstants;
 import org.eclipse.vjet.eclipse.internal.ui.text.folding.VjoFoldingStructureProvider;
 import org.eclipse.vjet.eclipse.ui.VjetPreferenceConstants;
 import org.eclipse.vjet.eclipse.ui.VjetUIPlugin;
 import org.eclipse.vjet.eclipse.ui.actions.nature.AddVjoNaturePolicyManager;
 import org.eclipse.vjet.eclipse.ui.actions.nature.IAddVjoNaturePolicy;
 import org.eclipse.vjet.testframework.view.EditorUtil;
 import org.eclipse.vjet.testframework.view.SyntaxHighlightUtil;
 import org.eclipse.vjet.vjo.tool.typespace.TypeSpaceMgr;
 import org.junit.Ignore;
 import org.osgi.framework.Bundle;
 
 /**
  * verify bugs that window tester can not handle
  * 
  * 
  *
  */
 public class BugVerifyTests extends AbstractVjoModelTests {
 	private static final String PROJECT_NAME = "BugVerifyProject";
 	
 	@Override
 	protected void setUp() throws Exception {
 		mgr.setAllowChanges(false);
 		try {
 			//close welcome view
 			IViewReference[] references = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
 			for (int i = 0; i < references.length; i++) {
 				if ("org.eclipse.ui.internal.introview".equals(references[i].getId()))
 					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(references[i]);
 			}
 			
 			// ensure autobuilding is turned off
 			IWorkspaceDescription description = getWorkspace().getDescription();
 			if (description.isAutoBuilding()) {
 				description.setAutoBuilding(true);
 				getWorkspace().setDescription(description);
 			}
 			
 			//set up project
 			setUpScriptProjectTo(PROJECT_NAME, PROJECT_NAME);					
 		
 			//refresh typespace
 			mgr.reload(this);	
 			waitTypeSpaceLoaded();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/* 
 	 * specify bug verify workspace
 	 * 
 	 * @see org.eclipse.vjet.eclipse.core.test.parser.AbstractVjoModelTests#getSourceWorkspacePath()
 	 */
 	public File getSourceWorkspacePath() {
 		return new File(getPluginDirectoryPath(), "workspace_bugVerify");
 	}
 	
 	/**
 	 * verify bug 1511, the js source as followed:
 	 *   vjo.ctype('Bug1511') //< public
 		.needs('org.eclipse.vjet.vjo.CType')
 		.props({
   
 		})
 		.protos({
 
 		})
 		.endType();
 	 * 
 	 *  Note: info about the '.needs(' key word in editor: offset is 33, length is 7.
 	 *  
 	 * @throws Exception
 	 */
 	@Ignore("todo reenable vjo color syntax highlighting")
 	public void ignoretest1511() throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug1511.js");
 		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
 		
 		VjoEditor vjoEditor = (VjoEditor)EditorUtil.getActiveEditor();
 		
 		//pre-defined key word color
 		IColorManager colorManager = VjetUIPlugin.getDefault().getTextTools().getColorManager();
 		Color keywordColor = colorManager.getColor(VjetColorConstants.VJET_KEYWORD);
 		
 		//NOTE: the corresponding keyword text is '.needs(', not 'needs' or '.needs'
 		Color backgroundColor = SyntaxHighlightUtil.getBackgroundColor(vjoEditor.getScriptSourceViewer(), 34, 6);
 		Color foregroundColor = SyntaxHighlightUtil.getForegroundColor(vjoEditor.getScriptSourceViewer(), 34, 6);
 		final boolean isBackColorWright = (backgroundColor == null);
 		final boolean isForeColorWright = keywordColor.equals(foregroundColor);
 		
 		assertTrue("color of '.needs' is not wright", isBackColorWright && isForeColorWright);
 	}
 	
 	/**
 	 * verify bug 4929
 	 * 
 	 * @throws Exception
 	 */
 	public void test4929() throws Exception {
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug4929.js");
 			
 			ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 			char[] source = sourceModule.getSourceAsCharArray();
 			ModuleDeclaration moduleDeclaration = new ModuleDeclaration(source.length);
 			
 			String groupName = file.getProject().getName();
 			String typeName = CodeassistUtils.getClassName(file);
 
 			IJstType type = TypeSpaceMgr.findType(groupName, typeName);
 			
 			new VjoSourceParser().processType(type, moduleDeclaration);
 		} catch (NullPointerException e) {
 			//check if NPE happens
 			assertTrue("NPE happens", e == null);
 		}
 	}
 	
 	/**
 	 * verify bug 4005
 	 * 
 	 * @throws Exception
 	 */
 	public void test4005() throws Exception {
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug4005.vjo");
 			
 			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
             IDE.openEditor(workbenchPage, file, "org.eclipse.ui.DefaultTextEditor");
 		} catch (NullPointerException e) {
 			//check if NPE happens
 			assertTrue("NPE happens", e == null);
 		}
 	}
 	
 	/**
 	 * verify bug 4137, 2088 (outline children order related bugs)
 	 * 
 	 * @throws Exception
 	 */
 	public void testOutlineChildrenOrder() throws Exception {
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/bug/Bug4137.js");
 			
 			//open js file
 			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 			IEditorPart editorPart = IDE.openEditor(workbenchPage, file);
 			
 			//fetch outline page
 			workbenchPage.showView("org.eclipse.ui.views.ContentOutline");
 			VjoOutlinePage outlinePage = (VjoOutlinePage)editorPart.getAdapter(IContentOutlinePage.class);
 			assertTrue("outline page not open", outlinePage != null);
 			
 			//fetch children and verify order
 			TreeViewer treeViewer = outlinePage.getOutlineViewer();
 			ITreeContentProvider contentProvider = (ITreeContentProvider)treeViewer.getContentProvider();
 			Object[] children = contentProvider.getChildren(treeViewer.getInput());
 			assertTrue("children in outline is not correct", children.length == 3);
 			
 			final boolean isFirstPackage = children[0] instanceof IPackageDeclaration;
 			final boolean isSecondImport = children[1] instanceof IImportContainer;
 			final boolean isThirdType = children[2] instanceof IJSType;
 			assertTrue("children order is wrong", isFirstPackage && isSecondImport && isThirdType);
 		} catch (Exception e) {
 			assertTrue("exception occurs", e == null);
 		}
 	}
 	
 	
 	/**
 	 * verify bug 2546
 	 * 
 	 * @throws Exception
 	 */
 	public void test2146() throws Exception {
 		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.perspectives");
 		for (int i = 0; i < configurationElements.length; i++) {
 			if ("org.eclipse.vjet.eclipse.ui.JavascriptBrowsingPerspective".equals(configurationElements[i].getAttribute("id"))) {
 				String iconPath = configurationElements[i].getAttribute("icon");
 				assertTrue("wrong icon", "icons/full/eview16/javascript_persp.gif".equals(iconPath));
 				return;
 			}
 		}
 		
 		assertTrue("org.eclipse.vjet.eclipse.ui.JavascriptBrowsingPerspective extension not found", false);
 	}
 	
 	/**
 	 * verify bug 5009
 	 * 
 	 * @throws Exception
 	 */
 	public void test5009() throws Exception {
 		
 		//verify reference
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug5009.js");
 		ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 		IType booleanType = CodeassistUtils.findType((ScriptProject)sourceModule.getScriptProject(), "Boolean");
 		SearchQueryParameters searchQueryParameters = this.createReferencesSearchQueryParameters((IJSSourceModule)sourceModule, booleanType);
 		VjoSearchEngine seacher = new VjoSearchEngine();
 		List matches = seacher.search(searchQueryParameters);
 		assertTrue("Can not fetch references for Boolean", matches.size() > 0);
 		
 		//verify hierarchy
 		IType[] superTypes = booleanType.newTypeHierarchy(null).getSupertypes(booleanType);
 		final boolean isWright = superTypes.length == 1 && "Object".equals(superTypes[0].getElementName());
 		assertTrue("Boolen Type Hierarchy is not correct!", isWright);
 	}
 	
 	
 	/**
 	 * verify bug 2445
 	 * 
 	 * @throws Exception
 	 */
 	public void test2445() throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug2445.js");
 		ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 		
 		IModelElement[] modelElements = ((ICodeAssist)sourceModule).codeSelect(215, 0);
 		if (modelElements.length == 0)
 			assertTrue("can not fetch dltk element", false);
 		
 		SearchQueryParameters searchQueryParameters = this.createReferencesSearchQueryParameters((IJSSourceModule)sourceModule, modelElements[0]);
 		VjoSearchEngine seacher = new VjoSearchEngine();
 		List matches = seacher.search(searchQueryParameters);
 		assertTrue("can not fetch references by constructors method", matches.size() > 0);
 	}
 	
 	/**
 	 * verify bug 4166
 	 * 
 	 * @throws Exception
 	 */
 	public void test4166() throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/C4166Type.js");
 		ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 		
 		IModelElement[] modelElements = ((ICodeAssist)sourceModule).codeSelect(52, 0);
 		if (modelElements.length == 0)
 			assertTrue("can not fetch dltk element", false);
 		
 		SearchQueryParameters searchQueryParameters = this.createReferencesSearchQueryParameters((IJSSourceModule)sourceModule, modelElements[0]);
 		VjoSearchEngine seacher = new VjoSearchEngine();
 		List matches = seacher.search(searchQueryParameters);
 		assertTrue("can not fetch references for IType I4166Interface", matches.size() > 0);
 	}
 	
 	/**
 	 * verify bug 3168
 	 * 
 	 * @throws Exception
 	 */
 	public void test3168() throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug3168.js");
 		ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 			
 		IModelElement[] modelElements = ((ICodeAssist)sourceModule).codeSelect(62, 0);
 		if (modelElements.length == 0)
 			assertTrue("can not fetch dltk element", false);
 			
 		SearchQueryParameters searchQueryParameters = this.createReferencesSearchQueryParameters((IJSSourceModule)sourceModule, modelElements[0]);
 		VjoSearchEngine seacher = new VjoSearchEngine();
 		List matches = seacher.search(searchQueryParameters);
 		assertTrue("references number is not excepted, occurence marker/annotation will be not correct!", matches.size() == 3);
 	}
 	
 	/**
 	 * verify bug 2028
 	 * 
 	 * @throws Exception
 	 */
 	public void test2028() throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug2028.js");
 		ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 			
 		IModelElement[] modelElements = ((ICodeAssist)sourceModule).codeSelect(157, 0);
 		if (modelElements.length == 0)
 			assertTrue("can not fetch dltk element", false);
 			
 		SearchQueryParameters searchQueryParameters = this.createReferencesSearchQueryParameters((IJSSourceModule)sourceModule, modelElements[0]);
 		VjoSearchEngine seacher = new VjoSearchEngine();
 		List matches = seacher.search(searchQueryParameters);
 		assertTrue("can not find references for constructor from base.constructs()", matches.size() > 0);
 	}
 	
 	/**
 	 * verify bug 1264
 	 * 
 	 * @throws Exception
 	 */
 	public void test1264() throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug1264.js");
 		ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 			
 		IModelElement[] modelElements = ((ICodeAssist)sourceModule).codeSelect(140, 0);
 		if (modelElements.length == 0)
 			assertTrue("can not fetch dltk element", false);
 			
 		SearchQueryParameters searchQueryParameters = this.createDeclarSearchQueryParameters((IJSSourceModule)sourceModule, modelElements[0]);
 		VjoSearchEngine seacher = new VjoSearchEngine();
 		List matches = seacher.search(searchQueryParameters);
 		assertTrue("can not fetch vjo match", matches.size() == 1);
 	}
 	
 	//create query parameters for VjoSearchEngine
 	private SearchQueryParameters createDeclarSearchQueryParameters(
 			IJSSourceModule module, IModelElement element) {
 		SearchQueryParameters params = new SearchQueryParameters();
 		if (SearchPattern.createPattern(element,
 				IDLTKSearchConstants.DECLARATIONS) != null) {
 			params.setPattern(SearchPattern.createPattern(element,
 					IDLTKSearchConstants.DECLARATIONS));
 		}
 
 		params.setElementSpecification(true);
 		params.setElement(element);
 		params.setScope(SearchEngine.createSearchScope(module
 				.getScriptProject()));
 		params.setLimitTo(IDLTKSearchConstants.DECLARATIONS);
 
 		return params;
 	}
 	
 	//create query parameters for VjoSearchEngine
 	private SearchQueryParameters createReferencesSearchQueryParameters(
 			IJSSourceModule module, IModelElement element) {
 		SearchQueryParameters params = new SearchQueryParameters();
 		if (SearchPattern.createPattern(element,
 				IDLTKSearchConstants.ALL_OCCURRENCES) != null) {
 			params.setPattern(SearchPattern.createPattern(element,
 					IDLTKSearchConstants.ALL_OCCURRENCES));
 		}
 
 		params.setElementSpecification(true);
 		params.setElement(element);
 		params.setScope(SearchEngine.createSearchScope(module
 				.getScriptProject()));
 		params.setLimitTo(IDLTKSearchConstants.ALL_OCCURRENCES);
 
 		return params;
 	}
 	
 	
 	/**
 	 * verify bug 2709
 	 * 
 	 * @throws Exception
 	 */
 	public void test2709() throws Exception {
 		IEditorPart editorPart = null;
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug2709.js");
 			
 			//open js file and fetch IDocument
 			editorPart = IDE.openEditor(workbenchPage, file);
 			IDocument document = ((VjoEditor)editorPart).getDocumentProvider().getDocument(editorPart.getEditorInput());
 			assertTrue("document NULL", document != null);
 			
 			document.replace(114, 0, "var date = new Date(); //< Date");
 			((VjoEditor)editorPart).selectAndReveal(131, 0);
 		}
 		catch (NullPointerException npe) {
 			assertTrue("NPE appear!", false);
 		}
 		finally {
 			workbenchPage.closeEditor(editorPart, false);
 		}
 	}
 	
 	/**
 	 * verify bug 3331
 	 * 
 	 * @throws Exception
 	 */
 	public void test3331() throws Exception {
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		IViewPart viewPart = workbenchPage.showView("org.eclipse.debug.ui.VariableView");
 		
 		IMenuManager menuManager = ((PartSite)viewPart.getSite()).getActionBars().getMenuManager();
 		IContributionItem[] contributionItems = menuManager.getItems();
 		for (int i = 0; i < contributionItems.length; i++) {
 			String id = contributionItems[i].getId();
 			if ("org.eclipse.vjet.eclipse.debug.ui.menu.VariableView".equals(id)) {
 				MenuManager vjetMenuManager = (MenuManager)contributionItems[i];
 				assertTrue("menu text should be VJET!", "VJET".equals(vjetMenuManager.getMenuText()));
 			}
 		}
 	}
 	
 	/**
 	 * verify bug 2519
 	 * 
 	 * @throws Exception
 	 */
 	public void test2519() throws Exception {
 		IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
 			@Override
 			public void run(IProgressMonitor monitor) throws CoreException {
 				// create project
 				IProject project = ResourcesPlugin.getWorkspace().getRoot()
 						.getProject("JavaProject");
 				if (project.exists())
 					project.delete(false, monitor);
 				project.create(monitor);
 
 				if (!project.isOpen())
 					project.open(monitor);
 				
 				// add nature
 				IProjectDescription description = project.getDescription();
 				String[] prevNatures = description.getNatureIds();
 				String[] newNatures = new String[prevNatures.length + 1];
 				System.arraycopy(prevNatures, 0, newNatures, 0,
 						prevNatures.length);
 				newNatures[prevNatures.length] = JavaCore.NATURE_ID;
 				description.setNatureIds(newNatures);
 				project.setDescription(description, monitor);
 
 				// add src
 				project.getFolder("src").create(false, true, monitor);
 				IClasspathEntry srcEntry = JavaCore.newSourceEntry(project
 						.getFullPath().append("src"));
 				IClasspathEntry[] entries = new IClasspathEntry[] { srcEntry };
 				IJavaProject javaProject = JavaCore.create(project);
 				javaProject.setRawClasspath(entries, monitor);
 
 				// add VJET Nature
 				IAddVjoNaturePolicy policy = AddVjoNaturePolicyManager
 						.getInstance().getPolicy(project);
 				policy.addVjoNature(project);
 
 				// verify VJET .buildpath
 				IFile buildPathFile = project
 						.getFile(ScriptProject.BUILDPATH_FILENAME);
 				if (!buildPathFile.exists())
 					assertTrue("VJET .buildpath file not exist!", false);
 
 				ScriptProject scriptProject = new ScriptProject(project, null);
 				IBuildpathEntry buildpathEntry = scriptProject.getBuildpathEntryFor(project.getFullPath().append("src"));
 				
 				// delete java project
 				project.delete(true, null);
 				
 				if (buildpathEntry == null)
 					assertTrue("no src build path entry", false);
 			}
 		};
 		ResourcesPlugin.getWorkspace().run(workspaceRunnable, null);
 	}
 	
 
 	public void test1424() {
 		String tempPlainProjectName = "PlainProject";
 		String tempJsFileName = "testFile.js";
 		IEditorPart editorPart = null;
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage();
 		try {
 			IProject project = createPlainProject(tempPlainProjectName,
 					tempJsFileName);
 
 			IFile file = project.getFile(tempJsFileName);
 
 			//open js file and fetch IDocument
 			editorPart = IDE.openEditor(workbenchPage, file);
 			IDocument document = ((VjoEditor) editorPart).getDocumentProvider()
 					.getDocument(editorPart.getEditorInput());
 			assertTrue("document NULL", document != null);
 			workbenchPage.closeEditor(editorPart, false);
 		} catch (NullPointerException npe) {
 			assertTrue("NPE appear!", false);
 		} catch (CoreException e) {
 			assertTrue("NPE appear!", false);
 		} finally {
 		}
 	}
 
 	private IProject createPlainProject(String projectName, String jsFileName)
 			throws CoreException {
 		IProgressMonitor monitor = new NullProgressMonitor();
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
 				PROJECT_NAME);
 		if (!project.exists()) {
 			project.create(monitor);
 		}
 		if (jsFileName != null) {
 			IFile file = project.getFile("testFile.js");
 			if (!file.exists()) {
 				file.create(new ByteArrayInputStream(
 						"vjo.ctype('testFile').endType();".getBytes()), true,
 						monitor);
 			}
 		}
 		return project;
 	}
 
 	/**
 	 * Test the method count from a jst type which mixes other mtypes.
 	 */
 	public void test2585() {
 		String typeName = "mixinCompletion.test2";
 		IJstType type = TypeSpaceMgr.getInstance().findType(
 				new TypeName(PROJECT_NAME, typeName));
 		assertNotNull("Can not find the type : " + typeName, type);
 		IJstMethod method = type.getMethod("doM");
 		assertTrue("The mixed method should be JstProxyMethod",
 				method instanceof JstProxyMethod);
 		IJstProperty property = type.getProperty("m1");
 		assertTrue("The mixed property should be JstProxyProperty",
 				property instanceof JstProxyProperty);
 	}
 	
 	/**
 	 * @throws Exception
 	 */
 	public void test2533() throws Exception {
 		VjoEditor vjoEditor = null;
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug2533.js");
 			ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 			
 			//open js file and fetch IDocument
 			vjoEditor = (VjoEditor)IDE.openEditor(workbenchPage, file);
 			
 			IModelElement[] modelElements = ((ICodeAssist)sourceModule).codeSelect(46, 0);
 			if (modelElements.length == 0)
 				assertTrue("can not fetch dltk element", false);
 				
 			//fetch references after redraw
 			SearchQueryParameters searchQueryParameters = this.createReferencesSearchQueryParameters((IJSSourceModule)sourceModule, modelElements[0]);
 			VjoSearchEngine seacher = new VjoSearchEngine();
 			List matches = seacher.search(searchQueryParameters);
 			assertTrue("can not find references before redraw", matches.size() > 0);
 			
 			//fetch references before redraw
 			vjoEditor.getScriptSourceViewer().getTextWidget().redraw();
 			matches = seacher.search(searchQueryParameters);
 			assertTrue("can not find references after redraw", matches.size() > 0);
 		}
 		finally {
 			workbenchPage.closeEditor(vjoEditor, false);
 		}
 	}
 	
 	/**
 	 * verify bug 3161
 	 * 
 	 * @throws Exception
 	 */
 	public void test3161() throws Exception {
 		VjoEditor vjoEditor = null;
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug2533.js");
 			
 			//open vjoeditor and outline view
 			vjoEditor = (VjoEditor)IDE.openEditor(workbenchPage, file);
 			IViewPart viewPart = workbenchPage.showView("org.eclipse.ui.views.ContentOutline");
 			
 			//verify toolbar
 			IToolBarManager toolBarManager = ((PartSite)viewPart.getSite()).getActionBars().getToolBarManager();
 			IContributionItem[] contributionItems = toolBarManager.getItems();
 			
 			Set<String> labelSet = new HashSet<String>();
 			for (int i = 0; i < contributionItems.length; i++) {
 				if (!(contributionItems[i] instanceof SubContributionItem)) {
 				continue;	
 				}
 				ActionContributionItem actionContributionItem = (ActionContributionItem)((SubContributionItem)contributionItems[i]).getInnerItem();
 				String toolTip = actionContributionItem.getAction().getToolTipText();
 				labelSet.add(toolTip.trim());
 			}
 			
 			assertTrue("Missing:Sort", labelSet.contains("Sort"));
 			assertTrue("Missing:Hide Fields", labelSet.contains("Hide Fields"));
 			assertTrue("Missing:Hide Static Fields and Methods", labelSet.contains("Hide Static Fields and Methods"));
 			assertTrue("Missing:Hide Non-Public Members", labelSet.contains("Hide Non-Public Members"));
 			assertTrue("Missing:Hide Local Types", labelSet.contains("Hide Local Types"));
 		}
 		finally {
 			workbenchPage.closeEditor(vjoEditor, false);
 		}
 	}
 	
 	/**
 	 * verify bug 3162
 	 * 
 	 * @throws Exception
 	 */
 	public void test3162() throws Exception {
 		VjoEditor vjoEditor = null;
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug2533.js");
 			
 			//open vjoeditor and outline view
 			vjoEditor = (VjoEditor)IDE.openEditor(workbenchPage, file);
 			IViewPart viewPart = workbenchPage.showView("org.eclipse.ui.views.ContentOutline");
 			
 			//verify toolbar
 			IMenuManager menuManager = ((PartSite)viewPart.getSite()).getActionBars().getMenuManager();
 			IContributionItem[] contributionItems = menuManager.getItems();
 			
 			boolean goToTopLevelExist = false;
 			for (int i = 0; i < contributionItems.length; i++) {
 				if (!(contributionItems[i] instanceof SubContributionItem)) {
 					continue;	
 					}
 				SubContributionItem subContributionItem = (SubContributionItem)contributionItems[i];
 				if (subContributionItem.getInnerItem() instanceof ActionContributionItem) {
 					ActionContributionItem actionContributionItem = (ActionContributionItem)subContributionItem.getInnerItem();
 					if ("Go Into Top Level Type".equals(actionContributionItem.getAction().getText()))
 						goToTopLevelExist = true;
 				}
 			}
 			
 			assertTrue("No 'Go Into Top Level Type' Menu Item!", goToTopLevelExist);
 		}
 		finally {
 			workbenchPage.closeEditor(vjoEditor, false);
 		}
 	}
 	
 	/**
 	 * verify bug 3173
 	 * 
 	 * @throws Exception
 	 */
 	public void test3173() throws Exception {
 		//veirify dialog title
 		Shell shell = new Shell(Display.getDefault().getShells()[0]);
 		CustomFiltersDialog customFiltersDialog = new CustomFiltersDialog(
 				shell, "org.eclipse.dltk.mod.ui.ScriptOutlinePage", false, new String[0], new String[0]);
 		customFiltersDialog.configureShell(shell);
 		String dialogTitle = shell.getText();
 		assertTrue("Dialog title is not 'VJO Element Filters'", "VJO Element Filters".equals(dialogTitle));
 		
 		//verify filters
 		boolean hasImportFilter = false;
 		boolean hasPackageFilter = false;
 		FilterDescriptor[] filterDescriptors = FilterDescriptor.getFilterDescriptors("org.eclipse.dltk.mod.ui.ScriptOutlinePage");
 		for (int i = 0; i < filterDescriptors.length; i++) {
 			if ("Import declarations".equals(filterDescriptors[i].getName()))
 				hasImportFilter = true;
 			else if ("Package declarations".equals(filterDescriptors[i].getName()))
 				hasPackageFilter = true;
 		}
 		
 		assertTrue("No Import Filter", hasImportFilter);
 		assertTrue("No Package Filter", hasPackageFilter);
 	}
 	
     /**
      * Test the method count from a jst type which mixes other mtypes.
      */
     public void testBug8675() {
         // ensure autobuilding is turned off
         IWorkspaceDescription description = getWorkspace().getDescription();
 //        assertFalse(description.isAutoBuilding());
         IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                 PROJECT_NAME);
         IFile file = project.getFile("src/Bug8675.js");
         try {
             VjoSourceParser parser = new VjoSourceParser();
             parser.parse(file.getName().toCharArray(), file.getContents()
                     .toString().toCharArray(), new BuildProblemReporter(file));
         } catch (Exception e) {
         	
         }
         assertTrue("TypeSpace Loaded", mgr.existGroup(PROJECT_NAME));
     }
 	
 	/**
 	 * verify bug 690
 	 * 
 	 * testcase based on below configuration:
 	 * <page category="org.eclipse.vjet.eclipse.propertyPage" class="org.eclipse.vjet.eclipse.internal.ui.preferences.VjetBuildPathPropertyPage" id="org.eclipse.vjet.eclipse.ui.BuildpathProperties" name="%VjetBuildPathPropertyPage.name">
       <enabledWhen>
         <adapt type="org.eclipse.core.resources.IProject">
           <test property="org.eclipse.core.resources.projectNature" value="com.ebay.tools.vjet.core.nature"/>
         </adapt>         
       </enabledWhen>
       <keywordReference id="org.eclipse.dltk.mod.ui.buildpath"/>
     </page>    
 	 * 
 	 * @throws Exception
 	 */
 	public void test690() throws Exception {
 		try {
 			IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.propertyPages");
 			for (int i = 0; i < configurationElements.length; i++) {
 				if (!"org.eclipse.vjet.eclipse.ui.BuildpathProperties".equals(configurationElements[i].getAttribute("id")))
 					continue;
 				
 				//fetch 'enabledWhen' configuration
 				IConfigurationElement enableElement = configurationElements[i].getChildren("enabledWhen")[0];
 				IConfigurationElement adaptElement = enableElement.getChildren("adapt")[0];
 				String adaptType = adaptElement.getAttribute("type");
 				assertTrue("adapt type is not correct, should be IProject", "org.eclipse.core.resources.IProject".equals(adaptType));
 				
 				IConfigurationElement testElement = adaptElement.getChildren("test")[0];
 				String property = testElement.getAttribute("property");
 				assertTrue("test property NOT be 'org.eclipse.core.resources.projectNature'", "org.eclipse.core.resources.projectNature".equals(property));
 				
 				String value = testElement.getAttribute("value");
 				assertTrue("test value NOT be 'com.ebay.tools.vjet.core.nature'", "org.eclipse.vjet.core.nature".equals(value));
 			}
 		} catch (Exception e) {
 			assertTrue("exception occurs when parsing extension element", false);
 		}
 	}
 	
 	/**
 	 * verify bug 3163
 	 * 
 	 * @throws Exception
 	 */
 	public void test3163() throws Exception {
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		IViewPart viewPart = workbenchPage.showView("org.eclipse.dltk.mod.ui.ScriptExplorer");
 		
 		IMenuManager menuManager = ((PartSite)viewPart.getSite()).getActionBars().getMenuManager();
 		IContributionItem[] contributionItems = menuManager.getItems();
 		
 		boolean hasLibraryMenu = false;
 		for (int i = 0; i < contributionItems.length; i++) {
 			if (contributionItems[i] instanceof ActionContributionItem) {
 				String actionText = ((ActionContributionItem)contributionItems[i]).getAction().getText();
 				if ("Show 'Referenced Libraries' Node".equals(actionText))
 					hasLibraryMenu = true;
 			}
 		}
 		
 		assertTrue("NO Show 'Referenced Libraries' Node menu item", hasLibraryMenu);
 	}
 	
 	/**
 	 * verify bug 2014
 	 * 
 	 * @throws Exception
 	 */
 	public void test2014() throws Exception {
 		VjoEditor vjoEditor = null;
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug2533.js");
 			
 			//open vjoeditor and outline view
 			vjoEditor = (VjoEditor)IDE.openEditor(workbenchPage, file);
 			
 			//add breakpoint and record time cost
 			long start = System.currentTimeMillis();
 			BreakpointUtils.addLineBreakpoint(vjoEditor, 8);
 			long timeCost = System.currentTimeMillis() - start;
 			
 			//verify breakpoint and time coast
 			assertTrue("No breakpoint", BreakpointUtils.findLineBreakpoint(vjoEditor, 8) != null);
 			assertTrue("Adding breakpoint time cost > 3s, actual time: " + timeCost, timeCost<3000);
 		}
 		finally {
 			workbenchPage.closeEditor(vjoEditor, false);
 		}
 	}
 	
 	/**
 	 * verify bug 6664
 	 * 
 	 * @throws Exception
 	 */
 	public void test6664() throws Exception {
 		TypeNameMatch[] typeNameMatchs = VjoTypeInfoViewer.getSearchResults("*");
 		assertTrue("No search result for wildcard '*'", typeNameMatchs.length > 0);
 	}
 	
 	/**
 	 * verify bug 3796
 	 * 
 	 * @throws Exception
 	 */
 	public void test3796() throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug3796.js");
 		ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 			
 		IModelElement[] modelElements = ((ICodeAssist)sourceModule).codeSelect(65, 0);
 		if (modelElements.length == 0)
 			assertTrue("can not fetch dltk element", false);
 			
 		SearchQueryParameters searchQueryParameters = this.createReferencesSearchQueryParameters((IJSSourceModule)sourceModule, modelElements[0]);
 		VjoSearchEngine seacher = new VjoSearchEngine();
 		List matches = seacher.search(searchQueryParameters);
 		assertTrue("references number is not excepted, occurence marker/annotation will be not correct!", matches.size() == 3);
 	}
 	
 	/**
 	 * verify bug 1422
 	 * 
 	 * @throws Exception
 	 */
 	public void test1442() throws Exception {
 		VjoEditor vjoEditor = null;
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug1422.js");
 			
 			//open vjoeditor
 			vjoEditor = (VjoEditor)IDE.openEditor(workbenchPage, file);
 			Thread.currentThread().sleep(500);
 			Iterator it = vjoEditor.getDocumentProvider().getAnnotationModel(new FileEditorInput(file)).getAnnotationIterator();
 			int count = 0;
 			while (it.hasNext()) {
 				Object obj = it.next();
 				if (obj instanceof Annotation) {
 					Annotation anno = (Annotation)obj;
 					if (ScriptMarkerAnnotation.TASK_ANNOTATION_TYPE.equals(anno.getType())) {
 						count++;
 					}
 				}
 			}
 			
 			//verify dltk todo annotation number
 			// TODO look into TODO markers 
 			 //assertEquals("should be TWO todo markers",  2, count);
 		}
 		finally {
 			workbenchPage.closeEditor(vjoEditor, false);
 		}
 	}
 	
 	/**
 	 * verify bug 3239. two steps:
 	 * first, invoke vjo search in FRONT-END
 	 * second, verify tool bar
 	 * 
 	 * @throws Exception
 	 */
 	public void test3239() throws Exception {
 		//open search view
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		IViewPart viewPart = workbenchPage.showView("org.eclipse.search.ui.views.SearchView");
 		
 		//fetech dltk element
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug3239.js");
 		ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 			
 		IModelElement[] modelElements = ((ICodeAssist)sourceModule).codeSelect(131, 0);
 		if (modelElements.length == 0)
 			assertTrue("can not fetch dltk element", false);
 		
 		//create search query
 		ISearchQueryFactory queryFactory = DLTKUILanguageManager.getSearchQueryFactory(VjoNature.NATURE_ID);
 		ISearchQuery query = queryFactory.createQuery(createQuery(modelElements[0]));
 		
 		//run search in front-end
 		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
 		SearchUtil.runQueryInForeground(progressService, query);
 		
 		//verify search view toolbar
 		IToolBarManager toolBarManager = ((PartSite)viewPart.getSite()).getActionBars().getToolBarManager();
 		IContributionItem[] contributionItems = toolBarManager.getItems();
 		Set<String> labelSet = new HashSet<String>();
 		for (int i = 0; i < contributionItems.length; i++) {
 			if (!(contributionItems[i] instanceof SubContributionItem))
 				continue;
 			
 			if (!(((SubContributionItem)contributionItems[i]).getInnerItem() instanceof ActionContributionItem))
 				continue;
 			
 			ActionContributionItem actionContributionItem = (ActionContributionItem)((SubContributionItem)contributionItems[i]).getInnerItem();
 			if (actionContributionItem.getAction() instanceof GroupAction)
 				labelSet.add(actionContributionItem.getAction().getToolTipText());
 		}
 		assertTrue("Missing:Group by Project", labelSet.contains("Group by Project"));
 		assertTrue("Missing:Group by Package", labelSet.contains("Group by Package"));
 		assertTrue("Missing:Group by File", labelSet.contains("Group by File"));
 		assertTrue("Missing:Group by Type", labelSet.contains("Group by Type"));
 	}
 	
 	private QuerySpecification createQuery(IModelElement element) throws ModelException {
 		DLTKSearchScopeFactory factory = DLTKSearchScopeFactory.getInstance();
 		IDLTKSearchScope scope = factory.createWorkspaceScope(true, DLTKLanguageManager.getLanguageToolkit(VjoNature.NATURE_ID));
 		String description = factory.getWorkspaceScopeDescription(true);
 		return new ElementQuerySpecification(element, 1, scope,
 				description);
 	}
 	
 	/**
 	 * verify bug 5681
 	 * 
 	 * @throws Exception
 	 */
 	public void test5681() throws Exception {
 //		try {
 //			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 //			ScriptProject scriptProject = new ScriptProject(project, null);
 //			NativeElementFileAdvisor advisor = new NativeElementFileAdvisor();
 //			advisor.getFile(scriptProject);
 //		} catch (ClassCastException e) {
 //			assertTrue("ClassCastException occur!", false);
 //		}
 	}
 	
 	/**
 	 * verify bug 7594, directly check the build.properties file contenct in source ENV
 	 * 
 	 * @throws Exception
 	 */
 	public void test7594() throws Exception {
 		  Bundle bundle = Platform.getBundle("org.eclipse.vjet.eclipse.debug.ui");
 		  URL url = bundle.getEntry("build.properties");
 		  
 		  //in binary serenget env, pass it!
 		  if (url == null)
 			  return;
 		  
 		  InputStream inputStream = url.openStream();
 		  Properties properties = new Properties();
 		  properties.load(inputStream);
 		  
 		  String includes = properties.getProperty("bin.includes");
 		  inputStream.close();
 		  assertTrue("No icons folder in bin.includes", includes.indexOf("icons/") > 0);
 	}
 	
 	public void test1441() {
 		VjoEditor vjoEditor = null;
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage();
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot()
 					.getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug2533.js");
 
 			vjoEditor = (VjoEditor) IDE.openEditor(workbenchPage, file);
 			IFoldingStructureProvider provider = (IFoldingStructureProvider) vjoEditor
 					.getAdapter(IFoldingStructureProvider.class);
 			assertTrue(provider instanceof VjoFoldingStructureProvider);
 			ProjectionViewer viewer = (ProjectionViewer) vjoEditor
 					.getScriptSourceViewer();
 			IRegion region = viewer.getVisibleRegion();
 			assertNotNull(region);
 			ISourceModule module = EditorUtility.getEditorInputModelElement(
 					vjoEditor, false);
 			String content = module.getSource();
 			Method method = AbstractASTFoldingStructureProvider.class
 					.getDeclaredMethod("computeCommentsRanges", String.class);
 			method.setAccessible(true);
 			Object obj = method.invoke(provider, content);
 			assertTrue(obj.getClass().isArray());
 			IRegion[] regions = (IRegion[]) obj;
 			assertTrue(content.substring(regions[0].getLength() + 1).trim()
 					.startsWith("vjo"));
 
 		} catch (PartInitException e) {
 			assertNull("Exception was thrown during test", e);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (ModelException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} finally {
 			workbenchPage.closeEditor(vjoEditor, false);
 		}
 
 	}
 
 	public void test2787() {
 		IPreferenceStore store = VjetUIPlugin.getDefault().getPreferenceStore();
 		assertTrue(store
 				.isDefault(VjetPreferenceConstants.EDITOR_JAVADOC_TAGS_BOLD));
 		assertTrue(store
 				.isDefault(VjetPreferenceConstants.EDITOR_JAVADOC_HTML_MARKUP_BOLD));
 		assertTrue(store
 				.isDefault(VjetPreferenceConstants.EDITOR_TASK_TAGS_COMMENT_BOLD));
 	}
 	
 	public void test4984() {
 		String tempPlainProjectName = "PlainProject";
 		String tempJsFileName = "testFile.js";
 		IEditorPart editorPart = null;
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage();
 		IProject project = null;
 		try {
 			 project = createPlainProject(tempPlainProjectName,
 					tempJsFileName);
 
 			IFile file = project.getFile(tempJsFileName);
 
 			// open js file and fetch IDocument
 			editorPart = IDE.openEditor(workbenchPage, file);
 			project.close(new NullProgressMonitor());
 			assertTrue(!file.exists());
 		} catch (NullPointerException npe) {
 			assertTrue("NPE appear!", false);
 		} catch (CoreException e) {
 			assertTrue("NPE appear!", false);
 		} finally {
 			try {
 				project.open(new NullProgressMonitor());
 			} catch (CoreException e) {
 				assertTrue("Excpetion was thrown, maybe affect other test cases", false);
 			}
 		}
 	}
 
 	public void test7585() throws Exception {
 		try {
 			// Use the new parse method to add it into typespace.
 			new VjoSourceParser().parse(
 					"/BugVerifyProject/src/jsFileFromNormalFile".toCharArray(),
 					"vjo.ctype('jsFileFromNormalFile').endType();"
 							.toCharArray(), new ProblemCollector());
 
 			// Find it from the type space.
 			IJstType type = TypeSpaceMgr.findType("BugVerifyProject",
 					"jsFileFromNormalFile");
 			assertTrue("We can find the type", type != null);
 		} catch (Exception e) {
 			assertTrue("Exception happens", e == null);
 		}
 	}
 	
 	/**
 	 * For V1.0, vjo editor not support .vjo file type
 	 * 
 	 * @throws Exception
 	 */
 	public void test8653() throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug8653.vjo");
 		
 		IEditorDescriptor editorDescriptor = IDE.getEditorDescriptor(file);
 		String editorID = editorDescriptor.getId();
 		assertTrue("should not use vjo eidtor for .vjo file type", !"org.eclipse.vjet.ui.VjetJsEditor".equals(editorID));
 	}
 
 	/**
 	 * For V1.0, vjet not support js refactoring
 	 * 
 	 * @throws Exception
 	 */
 	public void test7829() throws Exception {
 		//modify by patrick for support eclipse 3.5
 		final String vjetJSPerspectiveId = "org.eclipse.vjet.eclipse.ui.JavascriptPerspective";
 		final String VIEW_ID = "org.eclipse.dltk.mod.ui.ScriptExplorer";
 		IWorkbench workbench = VjetUIPlugin.getDefault().getWorkbench();
 		IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
 		IWorkbenchPage activePage = workbench.showPerspective(vjetJSPerspectiveId, activeWindow);
 		IViewPart viewPart = activePage.showView(VIEW_ID);
 		ScriptExplorerPart scriptExplorer = (ScriptExplorerPart) viewPart;
 		
 		MenuManager menuManager = new MenuManager();
 		scriptExplorer.menuAboutToShow(menuManager);
 		
 		IContributionItem newMenuContributionItem = menuManager.find(IContextMenuConstants.GROUP_NEW);
 		assertNotNull("The menu should be registered!",newMenuContributionItem);
 		
 		IContributionItem reorgContributionItem = menuManager.find(RefactorActionGroup.MENU_ID);
		assertNull("The menu should not be registered!",reorgContributionItem);
 	}
 
 	/**
 	 * verify bug 2363
 	 * 
 	 * @throws Exception
 	 */
 	public void test2363() throws Exception {
 		VjoEditor vjoEditor = null;
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug2363.js");
 			
 			//fetch SourceViewerConfiguration
 			vjoEditor = (VjoEditor)IDE.openEditor(workbenchPage, file);
 			Field configurationField = AbstractTextEditor.class.getDeclaredField("fConfiguration");
 			configurationField.setAccessible(true);
 			SourceViewerConfiguration configuration = (SourceViewerConfiguration)configurationField.get(vjoEditor);
 			
 			//fetch completion proposals
 			IContentAssistProcessor assistProcessor = configuration.getContentAssistant(vjoEditor.getScriptSourceViewer()).getContentAssistProcessor("__dftl_partition_content_type");
 			ICompletionProposal[] completionProposals = assistProcessor.computeCompletionProposals(vjoEditor.getScriptSourceViewer(), 121);
 			assertTrue("NO completion proposals", completionProposals.length > 0);
 			assertTrue("NO JavaDoc", completionProposals[0].getAdditionalProposalInfo() != null);
 		}
 		finally {
 			workbenchPage.closeEditor(vjoEditor, false);
 		}
 	}
 	
 	   
     /**
      * verify bug 8908
      * 
      * @throws Exception
      */
     public void test8908() throws Exception {
         VjoEditor vjoEditor = null;
         VjoValidationAction action = new VjoValidationAction();
         IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
         try {
             IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
             IStructuredSelection selection = new StructuredSelection(project);
             action.selectionChanged(new AboutAction(workbenchPage.getWorkbenchWindow()), selection);
             action.run(null);
             project = action.getM_project().get(0);
             setUpProjectTo("TestProject", "TestProject");
             IProject project2 = ResourcesPlugin.getWorkspace().getRoot().getProject("TestProject");
             IStructuredSelection selection2 = new StructuredSelection(project2);
             action.selectionChanged(new AboutAction(workbenchPage.getWorkbenchWindow()), selection2);
             action.run(null);
             project2 = action.getM_project().get(0);
             Assert.assertNotSame(project, project2);
         }
         finally {
             deleteProject("TestProject");
             workbenchPage.closeEditor(vjoEditor, false);
         }
     }
     
     /**
      * vefify bug 5832
      * 
      * @throws Exception
      */
     public void test5832() throws Exception {
     	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug5832.js");
 		ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 			
 		IModelElement[] modelElements = ((ICodeAssist)sourceModule).codeSelect(70, 0);
 		assertTrue("NO model element for the first 'Array'", modelElements.length > 0);
 		
 		modelElements = ((ICodeAssist)sourceModule).codeSelect(77, 0);
 		assertTrue("NO model element for 'Date'", modelElements.length > 0);
 		
 		modelElements = ((ICodeAssist)sourceModule).codeSelect(110, 0);
 		assertTrue("NO model element for the second 'Array'", modelElements.length > 0);
 		
 		modelElements = ((ICodeAssist)sourceModule).codeSelect(118, 0);
 		assertTrue("NO model element for 'String'", modelElements.length > 0);
 	}
 	
     /**
      * verify bug 6249
      * 
      * @throws Exception
      */
     public void test6249() throws Exception {
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug6249.js");
 			ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
 				
 			((ICodeAssist)sourceModule).codeSelect(317, 0);
 			((ICodeAssist)sourceModule).codeSelect(323, 0);
 		} catch (ModelException e) {
 			assertTrue("DLTK ModelException occurs!", false);
 		}
 	}
     
     /**
      * verify bug 5069
      * 
      * @throws Exception
      */
     public void test5069() throws Exception {
     	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug5069.js");
 		
 		//fetch jst identifier
 		String groupName = file.getProject().getName();
 		String typeName = CodeassistUtils.getClassName(file);
 
 		IJstType jstType = TypeSpaceMgr.findType(groupName, typeName);
 		JstIdentifier identifier = (JstIdentifier)JstUtil.getLeafNode(jstType, 122, 122);
 		
 		//verify jst binding
 		IJstNode jstBinding = identifier.getJstBinding();
 		assertTrue("JstBinding for 'eval' identifier is not JstMethod", jstBinding instanceof IJstMethod);
 	}
     
     /**
      * verify bug 8632: iterate overloaded methods to check stack over flow error.
      * 
      * @throws Exception
      */
     public void test8632() throws Exception {
     	try {
     		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
     		IFile file = project.getFile("src/Bug8632.js");
     		ISourceModule sourceModule = getSourceModule(file.getFullPath().toString());
     			
     		IModelElement[] modelElements = ((ICodeAssist)sourceModule).codeSelect(128, 0);
     		if (modelElements.length == 0)
     			assertTrue("can not fetch dltk element", false);
     			
     		SearchQueryParameters searchQueryParameters = this.createReferencesSearchQueryParameters((IJSSourceModule)sourceModule, modelElements[0]);
     		VjoSearchEngine seacher = new VjoSearchEngine();
     		seacher.search(searchQueryParameters);
 		} catch (StackOverflowError e) {
 			assertTrue("StatckOverflowError when iterating overloaded methods", false);
 		}
 	}
     
     /**
      * verify bug 5173
      * 
      * @throws Exception
      */
     public void test5173() throws Exception {
     	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		IFile file = project.getFile("src/Bug5173.js");
 		
 		//fetch JstType
 		String groupName = file.getProject().getName();
 		String typeName = CodeassistUtils.getClassName(file);
 		IJstType jstType = TypeSpaceMgr.findType(groupName, typeName);
 		
 		//fetch JstTypeReference node and verify owner type
 		JstTypeReference stringNeed = (JstTypeReference)JstUtil.getLeafNode(jstType, 44, 44);
 		JstTypeReference dateNeed = (JstTypeReference)JstUtil.getLeafNode(jstType, 61, 61);
 		assertTrue("'String' needs has no owner-type", stringNeed.getOwnerType() != null);
 		assertTrue("'Date' needs has no owner-type", dateNeed.getOwnerType() != null);
 	}
     
     /**
      * verify bug 8128
      * 
      * @throws Exception
      */
     public void test8128() throws Exception {
     	FileReader fileReader = null;
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 			IFile file = project.getFile("src/Bug8128.js");
 			
 			String groupName = file.getProject().getName();
 			String typeName = CodeassistUtils.getClassName(file);
 			fileReader = new FileReader(file.getLocation().toFile());
 			String source = FileUtils.readFully(fileReader);
 			
 			VjoParserToJstAndIType m_parser = new VjoParserToJstAndIType();
 			m_parser.parse(groupName, typeName, source);
 		} catch (Exception e) {
 			e.printStackTrace();
 			assertTrue("Exception occurs when parsing Bug8128.js", false);
 		} finally {
 			if (fileReader != null)
 				fileReader.close();
 		}
 	}
     /**
      * verify bug 8633
      * There are two issues for this bug:
      * 1. Multi thread issue
      * 2. One file was parsed several times
      * 
      * For point 1: Yubin has added synchronized keyword.
      * This method test point 2.
      * 
      * @throws Exception
      */
     public void test8633() throws Exception {
     	FileReader fileReader = null;
     	try {
     		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
     		IFile file = project.getFile("src/Bug8128.js");
     		
     		String groupName = file.getProject().getName();
     		String typeName = CodeassistUtils.getClassName(file);
     		fileReader = new FileReader(file.getLocation().toFile());
     		String source = FileUtils.readFully(fileReader);
     		IJstType unit1 = null;
     		IJstType unit2 = null;
     		VjoParserToJstAndIType m_parser = new VjoParserToJstAndIType();
     		unit1 = m_parser.parse(groupName, typeName, source);
     		unit2 = m_parser.parse(groupName, typeName, source);
     		assertTrue(unit1 == unit2);
     	} catch (Exception e) {
     		e.printStackTrace();
     		assertTrue("Exception occurs when parsing Bug8128.js", false);
     	} finally {
     		if (fileReader != null)
     			fileReader.close();
     	}
     }
     
     /**
      * Test add vjo nature action's logic
      */
     public void testAddVjoNature() {
         IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
         AddVjoNatureAction ava = new AddVjoNatureAction();
         ava.selectionChanged(new AboutAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow()),
                 new StructuredSelection(project));
         try {
             IProjectNature nature = project.getNature(VjoNature.NATURE_ID);
             assertNotNull(nature);
         } catch (CoreException e) {
             e.printStackTrace();
         }
     }
 
 
     /**
      * http://quickbugstage.arch.ebay.com/show_bug.cgi?id=3912
      */
     public void testTypeLoadMonitor() {
     	InnerProgressMonitor monitor = new InnerProgressMonitor();
     	EclipseTypeLoadMonitor emonitor = new EclipseTypeLoadMonitor(monitor);
     	emonitor.loadTypeStarted(10);
     	assertEquals(10, monitor.getWorked());
     	emonitor.loadTypeStarted(20);
     	assertEquals(20, monitor.getWorked());
     	emonitor.loadTypeStarted(100);
     	assertEquals(100, monitor.getWorked());
     }
     
 
 	@Override
 	protected void tearDown() throws Exception {
 		deleteProject(PROJECT_NAME);
 		mgr.clean();
 	}
 	
 	class InnerProgressMonitor extends NullProgressMonitor {
 		private int worked = 0;
 		@Override
 		public void worked(int work) {
 			this.worked = work + this.worked;
 		}
 		public Object getWorked() {
 			return worked;
 		}
 	}
 }
