 /*******************************************************************************
  * Copyright (c) 2007-2012 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.jspeditor;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.gef.ui.views.palette.PalettePage;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.IPostSelectionProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IEditorActionBarContributor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.INavigationLocation;
 import org.eclipse.ui.INavigationLocationProvider;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.contexts.IContextService;
 import org.eclipse.ui.ide.IGotoMarker;
 import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.IStatusField;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.ui.texteditor.ITextEditorExtension;
 import org.eclipse.ui.texteditor.ITextEditorExtension2;
 import org.eclipse.ui.texteditor.ITextEditorExtension3;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.eclipse.wst.sse.ui.internal.contentoutline.ConfigurableContentOutlinePage;
 import org.jboss.tools.common.core.resources.XModelObjectEditorInput;
 import org.jboss.tools.common.model.XModelException;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.event.XModelTreeEvent;
 import org.jboss.tools.common.model.event.XModelTreeListener;
 import org.jboss.tools.common.model.filesystems.impl.DiscardFileHandler;
 import org.jboss.tools.common.model.filesystems.impl.FolderImpl;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.common.model.ui.editor.EditorDescriptor;
 import org.jboss.tools.common.model.ui.editor.IModelObjectEditorInput;
 import org.jboss.tools.common.model.ui.views.palette.PaletteContents;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.model.util.XModelTreeListenerSWTASync;
 import org.jboss.tools.common.text.ext.IMultiPageEditor;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.bundle.BundleMap;
 import org.jboss.tools.jst.jsp.editor.IVisualEditor;
 import org.jboss.tools.jst.jsp.editor.IVisualEditorFactory;
 import org.jboss.tools.jst.jsp.preferences.IVpePreferencesPage;
 import org.jboss.tools.jst.jsp.selection.bar.SelectionBarHandler;
 import org.jboss.tools.jst.web.tld.VpeTaglibManager;
 import org.jboss.tools.jst.web.tld.VpeTaglibManagerProvider;
 
 // Fix for EXIN-232: The IMultiPageEditor interface implementation is added.
 public class JSPMultiPageEditor extends JSPMultiPageEditorPart implements
 		XModelTreeListener, ITextEditor, IGotoMarker, VpeTaglibManagerProvider,
 		IReusableEditor, ITextEditorExtension, ITextEditorExtension2,
 		ITextEditorExtension3, INavigationLocationProvider, IMultiPageEditor {
 
 	private static final int MINIMUM_JRE_VERSION_FOR_VPE = 6;
 
 	public static final String EDITOR_ID = "org.jboss.tools.jst.jsp.jspeditor.JSPTextEditor"; //$NON-NLS-1$
 
 	private static final String VPE_VISUAL_EDITOR_IMPL_ID = "org.jboss.tools.vpe.org.jboss.tools.vpe.editor.VpeEditorPartFactory"; //$NON-NLS-1$
 
 	private static final String VISUAL_EDITOR_IMPL_EXTENSION_POINT_NAME = "visulaEditorImplementations"; //$NON-NLS-1$
 
 	private IVisualEditor visualEditor;
 	/*
 	 * https://issues.jboss.org/browse/JBIDE-10711
 	 * Set the xulRunnerBrowser state.
 	 */
 	private boolean xulRunnerBrowserIsNotSupported = false;
 	/*
 	 * Flag that indicates that the editor 
 	 * is being creating for the first time, 
 	 * i.e. part is initializing.
 	 */
 	private boolean vpeIsCreating = true;
 
 	private int visualSourceIndex;
 	private int sourceIndex;
 	/** index of tab contain default web-browser */
 	private int previewIndex=-1;//by default 1, so if there no visual editor impl, this tab will be not available
 
 	private JSPTextEditor sourceEditor;
 
 	private BundleMap bundleMap;
 
 	protected XModelTreeListenerSWTASync syncListener = new XModelTreeListenerSWTASync(
 			this);
 
 	// private int oldPage = -1;
 
 	private ConfigurableContentOutlinePage outlinePage = null;
 
 	private XModelObject object;
 
 	private QualifiedName persistentTabQualifiedName = new QualifiedName("", //$NON-NLS-1$
 			"Selected_tab"); //$NON-NLS-1$
 
 	int selectedPageIndex = 0;
 
 	static IVisualEditorFactory visualEditorFactory;
 	
 	//added by Maksim Areshkau, notified externalize command that selection changed
 	private ISelectionChangedListener externalizeSelectionChangeListener = new ISelectionChangedListener() {
 		
 		public void selectionChanged(SelectionChangedEvent event) {
 			ICommandService commandService = (ICommandService) PlatformUI
 			.getWorkbench().getService(ICommandService.class);
 			commandService.refreshElements("org.jboss.tools.jst.jsp.commands.i18", null); //$NON-NLS-1$
 		}
 	};
 
 	static {
 		// Fix For JBIDE-2674
 		try {
 			IExtension visualEditorExtension = Platform.getExtensionRegistry()
 					.getExtension(JspEditorPlugin.PLUGIN_ID,
 							VISUAL_EDITOR_IMPL_EXTENSION_POINT_NAME,
 							VPE_VISUAL_EDITOR_IMPL_ID);
 			if (visualEditorExtension != null) {
 				IConfigurationElement[] configurationElements = visualEditorExtension
 						.getConfigurationElements();
 				if (configurationElements != null
 						&& configurationElements.length == 1) {
 					visualEditorFactory = (IVisualEditorFactory) configurationElements[0]
 							.createExecutableExtension("class"); //$NON-NLS-1$
 				} else {
 					JspEditorPlugin
 							.getPluginLog()
 							.logError(
 									"Visual Editor Extension Point not configured correctly"); //$NON-NLS-1$
 				}
 			} else {
 				if (isRequiredJreVersionForVpe()) {
 					JspEditorPlugin.getPluginLog().logError(
 							"Visual Editor Implementation not available."); //$NON-NLS-1$
 				} else {
 					JspEditorPlugin.getPluginLog().logError(
 							"Visual Editor Implementation not available." + //$NON-NLS-1$
 							" It requires JRE 1." + MINIMUM_JRE_VERSION_FOR_VPE + " or later."); //$NON-NLS-1$ //$NON-NLS-2$					
 				}
 			}
 		} catch (CoreException e) {
 			JspEditorPlugin.getPluginLog().logError(
 					"Visual Editor Implementation not available." + e); //$NON-NLS-1$
 		}
 	}
 
 	
 	
 	private void loadSelectedTab() {
 		String defaultVpeTab = JspEditorPlugin.getDefault()
 				.getPreferenceStore().getString(
 						IVpePreferencesPage.DEFAULT_VPE_TAB);
 		if (IVpePreferencesPage.DEFAULT_VPE_TAB_VISUAL_SOURCE_VALUE
 				.equalsIgnoreCase(defaultVpeTab)) {
 			selectedPageIndex = 0;
 		} else if (IVpePreferencesPage.DEFAULT_VPE_TAB_SOURCE_VALUE
 				.equalsIgnoreCase(defaultVpeTab)) {
 			selectedPageIndex = 1;
 		} else if (IVpePreferencesPage.DEFAULT_VPE_TAB_PREVIEW_VALUE
 				.equalsIgnoreCase(defaultVpeTab)) {
 			selectedPageIndex = 2;
 		} else {
 			selectedPageIndex = 0;
 		}
 		//when implementation of visual editor not available we have only one tab
 		if(visualEditor==null){
 			selectedPageIndex=0;
 		}
 	}
 
 	private static boolean isRequiredJreVersionForVpe() {
 		String javaVersion = System.getProperty("java.version"); //$NON-NLS-1$
 		Pattern pattern = Pattern.compile("1\\.(\\d{1,6})\\..*"); //$NON-NLS-1$
 		if (javaVersion != null) {
 			Matcher matcher = pattern.matcher(javaVersion);
 			if (matcher.matches()) {
 				int majorVersion = Integer.valueOf(matcher.group(1));
 				if (majorVersion < MINIMUM_JRE_VERSION_FOR_VPE) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 
 	/*
 	 * The method is @Deprecated and is not consistent with a real 
 	 * MultiPageEditorPart.pageChange(int newPageIndex) method
 	 * 
 	 * So, this one is removed as of JBIDE-JBIDE-12534
 	 * 
 
 	public void superPageChange(int newPageIndex) {
 		Control control = getControl(visualSourceIndex);
 		if (control != null) {
 			control.setVisible(true);
 		}
 		setFocus();
 		IEditorPart activeEditor = getEditor(visualSourceIndex);
 		IEditorActionBarContributor contributor = getEditorSite()
 				.getActionBarContributor();
 		if (contributor != null
 				&& contributor instanceof MultiPageEditorActionBarContributor) {
 			((MultiPageEditorActionBarContributor) contributor)
 					.setActivePage(activeEditor);
 		}
 		if (activeEditor != null) {
 			ISelectionProvider selectionProvider = activeEditor.getSite()
 					.getSelectionProvider();
 			if (selectionProvider != null) {
 				SelectionChangedEvent event = new SelectionChangedEvent(
 						selectionProvider, selectionProvider.getSelection());
 				((JSPMultiPageSelectionProvider) getSite()
 						.getSelectionProvider()).fireSelectionChanged(event);
 			}
 		}
 	}
 	 */
 	
 	@Override
 	public void pageChange(int newPageIndex) {
 		selectedPageIndex = newPageIndex;
 		if (visualEditor != null) {
 			if (selectedPageIndex == visualSourceIndex) {
 				if (visualEditor.getVisualEditor() == null) {
 					visualEditor.createVisualEditor();
 					/*
 					 * https://issues.jboss.org/browse/JBIDE-10711
 					 * XulRunnerBrowser could be not supported.
 					 * So there should be special handling when
 					 * VisualEditor is created for the first time.
 					 */
 					if (isXulRunnerBrowserNotSupported() && vpeIsCreating) {
 						/*
 						 * Set Source tab as default
 						 */
 						visualEditor.setVisualMode(IVisualEditor.SOURCE_MODE);
 						selectedPageIndex = IVisualEditor.SOURCE_MODE;
 						setActivePage(selectedPageIndex);
 					} else {
 						/*
 						 * Use default behavior for tab switching
 						 * when the JSPEditor has already been initialized,
 						 * but visual part is loaded for the first time.
 						 */
 						visualEditor.setVisualMode(IVisualEditor.VISUALSOURCE_MODE);
 					}
 				} else {
 					/*
 					 * https://issues.jboss.org/browse/JBIDE-10711
 					 * Use default behavior for tab switching
 					 * when visual editor is not null.
 					 */
 					visualEditor.setVisualMode(IVisualEditor.VISUALSOURCE_MODE);
 				}
 			} else if (selectedPageIndex == sourceIndex)
 				visualEditor.setVisualMode(IVisualEditor.SOURCE_MODE);
 			else if (selectedPageIndex == getPreviewIndex()) {
 				if (visualEditor.getPreviewWebBrowser() == null) {
 					visualEditor.createPreviewBrowser();
 					/*
 					 * https://issues.jboss.org/browse/JBIDE-10711
 					 */
 					if (isXulRunnerBrowserNotSupported() && vpeIsCreating) {
 						/*
 						 * Set Source tab as default
 						 */
 						visualEditor.setVisualMode(IVisualEditor.SOURCE_MODE);
 						selectedPageIndex = IVisualEditor.SOURCE_MODE;
 						setActivePage(selectedPageIndex);
 					} else {
 						/*
 						 * Use default behavior for tab switching
 						 * when the JSPEditor has already been initialized,
 						 * but preview part is loaded for the first time.
 						 */
 						visualEditor.setVisualMode(IVisualEditor.PREVIEW_MODE);
 					}
 				} else {
 					visualEditor.setVisualMode(IVisualEditor.PREVIEW_MODE);
 				}
 			}
 		}
 		
 		ICommandService commandService = (ICommandService) PlatformUI
 				.getWorkbench().getService(ICommandService.class);
 		commandService.refreshElements(SelectionBarHandler.COMMAND_ID, null);
 		getSelectionBar().refreshVisibility();
 		
 		super.pageChange(selectedPageIndex); 	// The call to real super.pageChange(newPageIndex) 
 												// is returned back because of JBIDE-12534
 		
 		JspEditorPlugin.getDefault().getPreferenceStore().
 			setValue(IVpePreferencesPage.DEFAULT_VPE_TAB, selectedPageIndex);
 	}
 
 	public int getSelectedPageIndex() {
 		return selectedPageIndex;
 	}
 
 	public void setInput(IEditorInput input) {
 		super.setInput(XModelObjectEditorInput.checkInput(input));
 		if (getEditorInput() instanceof IModelObjectEditorInput) {
 			object = ((IModelObjectEditorInput) getEditorInput())
 					.getXModelObject();
 		}
 		if (sourceEditor != null
 				&& sourceEditor.getEditorInput() != getEditorInput()
 				&& sourceEditor.getEditorInput() != null) {
 			if (sourceEditor instanceof AbstractTextEditor) {
 				((AbstractTextEditor) sourceEditor).setInput(getEditorInput());
 			}
 			if (visualEditor != null) {
 				visualEditor.setInput(getEditorInput());
 			}
 			updateTitle();
 		}
 		updateFile();
 	}
 
 	public void setInput0(IEditorInput input) {
 		super.setInput(XModelObjectEditorInput.checkInput(input));
 		updateFile();
 		firePropertyChange(IEditorPart.PROP_INPUT);
 	}
 
 	private void updateFile() {
 		IFile file = getFile();
 		if (file == null)
 			return;
 		try {
 			file.refreshLocal(0, null);
 		} catch (CoreException e) {
 			JspEditorPlugin.getPluginLog().logWarning(e);
 		}
 	}
 
 	private IFile getFile() {
 		IEditorInput input = getEditorInput();
 		return (input instanceof IFileEditorInput) ? ((IFileEditorInput) input)
 				.getFile() : null;
 	}
 
 	public String getContentDescription() {
 		return ""; //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 */
 	private ISelectionProvider selectionProvider = null;
 
 	private JSPMultiPageEditorSite site;
 
 	protected IEditorSite createSite(IEditorPart editor) {
 		site = new JSPMultiPageEditorSite(this, editor) {
 			private ISelectionChangedListener postSelectionChangedListener = null;
 
 			private ISelectionChangedListener getPostSelectionChangedListener() {
 				if (postSelectionChangedListener == null) {
 					postSelectionChangedListener = new ISelectionChangedListener() {
 						public void selectionChanged(SelectionChangedEvent event) {
 							handlePostSelectionChanged(event);
 						}
 					};
 				}
 				return postSelectionChangedListener;
 			}
 
 			protected void handlePostSelectionChanged(
 					SelectionChangedEvent event) {
 				ISelectionProvider parentProvider = getMultiPageEditor()
 						.getSite().getSelectionProvider();
 				ISelection s = event.getSelection();
 				if (s == null || s.isEmpty())
 					return;
 				if (s instanceof ITextSelection) {
 					if (parentProvider instanceof JSPMultiPageSelectionProvider) {
 						((JSPMultiPageSelectionProvider) parentProvider)
 								.firePostSelectionChanged(event);
 					}
 				}
 			}
 
 			public String getId() {
 				return getSite().getId();
 			}
 
 			/**
 			 * 
 			 */
 			public ISelectionProvider getSelectionProvider() {
 				return selectionProvider;
 			}
 
 			/**
 			 * 
 			 */
 			public void setSelectionProvider(ISelectionProvider provider) {
 				ISelectionProvider oldSelectionProvider = getSelectionProvider();
 				if (oldSelectionProvider != null) {
 					if (oldSelectionProvider instanceof IPostSelectionProvider) {
 						((IPostSelectionProvider) oldSelectionProvider)
 								.removePostSelectionChangedListener(getPostSelectionChangedListener());
 					}
 				}
 
 				selectionProvider = provider;
 				if (oldSelectionProvider != null) {
 					oldSelectionProvider
 							.removeSelectionChangedListener(getSelectionChangedListener());
 				}
 				if (selectionProvider != null) {
 					selectionProvider
 							.addSelectionChangedListener(getSelectionChangedListener());
 					selectionProvider.addSelectionChangedListener(externalizeSelectionChangeListener);
 				}
 
 				if (provider != null) {
 					if (provider instanceof IPostSelectionProvider) {
 						((IPostSelectionProvider) provider)
 								.addPostSelectionChangedListener(getPostSelectionChangedListener());
 					}
 				}
 			}
 
 			public void dispose() {
 				ISelectionProvider provider = getSelectionProvider();
 				if (provider != null) {
 					provider
 							.removeSelectionChangedListener(getSelectionChangedListener());
 					provider.removeSelectionChangedListener(externalizeSelectionChangeListener);
 				}
 				if (provider instanceof IPostSelectionProvider
 						&& postSelectionChangedListener != null) {
 					((IPostSelectionProvider) provider)
 							.removePostSelectionChangedListener(postSelectionChangedListener);
 
 				}
 				postSelectionChangedListener = null;
 				super.dispose();
 			}
 		};
 		return site;
 	}
 
 	protected void createPages() {
 		try {
 			createPagesForVPE();
 			loadSelectedTab();
 			switch (selectedPageIndex) {
 				case 0: {
 					// source/visual mode
 					setActivePage(selectedPageIndex);
 					break;
 				} case 1: {
 					// source mode
 					setActivePage(selectedPageIndex);
 					break;
 				} case 2: {
 					// preview mode
 					setActivePage(selectedPageIndex);
 					break;
 				} default: {
 					// by default we sets source/visual mode
 					setActivePage(0);
 					break;
 				}
 			}
 			
 			IFile f = getFile();
 			if (f != null && f.exists()) {
 				new ResourceChangeListener(this, getContainer());
 			}
 			if (getModelObject() != null) {
 				getModelObject().getModel().addModelTreeListener(syncListener);
 			}
 		} catch (PartInitException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		} finally {
 			/*
 			 * Indicate that VPE pages have been created.
 			 */
 			vpeIsCreating = false;
 		}
 		checkPalette();
 	}
 
 	/*
 	 * This Context Menu Listener fixes the 'empty menu items' issue
 	 * See JBIDE-12783. 
 	 */
     private final class ZContextMenuListener implements IMenuListener 
     {
 		@Override
 		public void menuAboutToShow(IMenuManager manager) {
             Item[] mi = getMenuItems(manager);
             for (int i = 0; i < mi.length; i++) {
                 mi[i].dispose();
             }
 		}
 		
 		Item[] getMenuItems(IMenuManager manager) {
 			Menu menu = ((MenuManager)manager).getMenu();
 			return (menu == null) ? new Item[0] : menu.getItems();
 		}
     }
     
 	private void createPagesForVPE() throws PartInitException {
 		/*
 		 * Create Source Editor and BundleMap
 		 */
 		sourceEditor = new JSPTextEditor(this) {
 			public void createPartControl(Composite parent) {
 				super.createPartControl(parent);
 				Menu m = sourceEditor.getTextViewer().getTextWidget().getMenu();
 				Object data = m.getData("org.eclipse.jface.action.MenuManager.managerKey");
 				if (data instanceof IMenuManager) {
 					(((IMenuManager)data)).addMenuListener(new ZContextMenuListener());
 				}
 			}
 		};
 		sourceEditor.setEditorPart(this);
 
 		/*
 		 * Create Bundle Map here but Initialize it  in the VpeController
 		 * or here if there is only the source part. 
 		 */
 		bundleMap = new BundleMap();
 		String sourceTabLabel = JSPEditorMessages.JSPMultiPageEditor_TabLabel_Source;
 		if (visualEditorFactory != null) {
 			visualEditor = visualEditorFactory.createVisualEditor(this,sourceEditor, IVisualEditor.VISUALSOURCE_MODE, bundleMap);
 
 			visualSourceIndex = addPage(visualEditor, getEditorInput(),sourceEditor);
 			setPageText(visualSourceIndex,JSPEditorMessages.JSPMultiPageEditor_TabLabel_VisualSource);
 			setPartName(visualEditor.getTitle());
 
 			sourceIndex = addPage(visualEditor, getEditorInput(),sourceEditor);
 			setPageText(sourceIndex, sourceTabLabel);
 			setPartName(visualEditor.getTitle());
 
 			setPreviewIndex(addPage(visualEditor, getEditorInput(),sourceEditor));
 			setPageText(getPreviewIndex(),
 					JSPEditorMessages.JSPMultiPageEditor_TabLabel_Preview);
 			setPartName(visualEditor.getTitle());
 		} else {
 			sourceIndex = addPage(sourceEditor, getEditorInput(),sourceEditor);
 			setPageText(sourceIndex, sourceTabLabel);
 			setPartName(sourceEditor.getTitle());
 			/*
 			 * When there is only the source part --
 			 * then VpeController very likely hasn't been created.
 			 * Thus Bundle Map should be initialized here instead of
 			 * VpeController.
 			 */
 			bundleMap.init(sourceEditor.getEditorInput());
 		}
 	}
 
 	public void doSave(IProgressMonitor monitor) {
 		sourceEditor.doSave(monitor);
 	}
 
 	class PCL implements IPropertyListener {
 		public void propertyChanged(Object source, int i) {
 			firePropertyChange(i);
 			if (i == IEditorPart.PROP_INPUT
 					&& getEditorInput() != sourceEditor.getEditorInput()) {
 				setInput(sourceEditor.getEditorInput());
 				setPartName(sourceEditor.getPartName());
 				setTitleToolTip(sourceEditor.getTitleToolTip());
 			}
 		}
 	}
 
 	public void doSaveAs() {
 		XModelObject old = getModelObject();
 		PCL pcl = new PCL();
 		sourceEditor.addPropertyListener(pcl);
 		sourceEditor.doSaveAs();
 		sourceEditor.removePropertyListener(pcl);
 		try {
 			if (old.isModified())
 				new DiscardFileHandler().executeHandler(old, new Properties());
 		} catch (XModelException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 	}
 
 	public void gotoMarker(final IMarker marker) {
 		// setActivePage(IVisualEditor.VISUALSOURCE_MODE);
 		// pageChange(IVisualEditor.VISUALSOURCE_MODE);
 		
 		// Fix for JBIDE-10835: In some environments we have no working XulRunner,
 		// so, as result, there is only Source Tab is in editor 
 		// (and its index is not IVisualEditor.SOURCE_MODE == 1, but 0) 
 		// 
 		int pageToActivate = (IVisualEditor.SOURCE_MODE < getPageCount() ? IVisualEditor.SOURCE_MODE : 0);
 		
 		setActivePage(pageToActivate);
 		pageChange(pageToActivate);
 		IGotoMarker adapter = (IGotoMarker) sourceEditor
 				.getAdapter(IGotoMarker.class);
 		if (adapter != null) {
 			adapter.gotoMarker(marker);
 		}
 	}
 
 	public boolean isSaveAsAllowed() {
 		return sourceEditor.isSaveAsAllowed();
 	}
 
 	public JSPTextEditor getJspEditor() {
 		return sourceEditor;
 	}
 
 	public StructuredTextEditor getSourceEditor() {
 		return sourceEditor;
 	}
 
 	public IVisualEditor getVisualEditor() {
 		return visualEditor;
 	}
 
 	public IEditorPart getActivePageEditor() {
 		return getActiveEditor();
 	}
 
 	protected XModelObject getModelObject() {
 		return object;
 	}
 
 	public void dispose() {
 		IEditorActionBarContributor contributor = getEditorSite()
 				.getActionBarContributor();
 		if (contributor != null
 				&& contributor instanceof MultiPageEditorActionBarContributor) {
 			((MultiPageEditorActionBarContributor) contributor)
 					.setActivePage(null);
 		}
 		if (visualEditor != null) {
 			visualEditor.dispose();
 		}
 		site.dispose();
 		site = null;
 		outlinePage = null;
 		XModelObject o = getModelObject();
 		if (o != null) {
 			o.getModel().removeModelTreeListener(syncListener);
 		}
 		if (syncListener != null)
 			syncListener.dispose();
 		syncListener = null;
 		if (o != null && o.isModified() && o.isActive()) {
 			try {
 				((FolderImpl) o.getParent()).discardChildFile(o);
 			} catch (XModelException e) {
 				JspEditorPlugin.getPluginLog().logError(e);
 			}
 		}
 		if(palettePage != null) {
 			palettePage.dispose();
 			palettePage = null;
 		}
 		IContextService contextService = (IContextService) getSite()
 		  .getService(IContextService.class);
 		super.dispose();
 	}
 	
 	PalettePageImpl palettePage;
 
 	public Object getAdapter(Class adapter) {
 		if(PalettePage.class == adapter) {
 			if(palettePage == null || palettePage.isDisposed()) {
 				IDocument d = getDocumentProvider().getDocument(getEditorInput());
 				if(d == null) {
 					return null;
 				}
 				palettePage = new PalettePageImpl();
 				palettePage.setPaletteContents(new PaletteContents(this));
 				palettePage.attach(d);
 			}
 			return palettePage;
 		}
 		if (IContentOutlinePage.class.equals(adapter)) {
 
 			if (visualEditor != null) {
 				if (outlinePage == null)
 					outlinePage = (ConfigurableContentOutlinePage) visualEditor
 							.getAdapter(adapter);
 				return outlinePage;
 			}
 
 		} else if (IPropertySheetPage.class.equals(adapter)) {
 			if (sourceEditor != null)
 				return sourceEditor.getAdapter(adapter);
 		} else if (adapter == EditorDescriptor.class)
 			return new EditorDescriptor(new String[] { "jsp", "html" }); //$NON-NLS-1$ //$NON-NLS-2$
 
 		if (sourceEditor != null) {
 			return sourceEditor.getAdapter(adapter);
 		}
 
 		return super.getAdapter(adapter);
 	}
 
 	public void nodeChanged(XModelTreeEvent event) {
 		if (event.getModelObject() == getModelObject()) {
			setContentDescription(getEditorInput().getName());
 			if (sourceEditor != null)
 				sourceEditor.updateModification();
 		}
 	}
 
 	public void structureChanged(XModelTreeEvent event) {
 	}
 
 	public void close(boolean save) {
 		sourceEditor.close(save);
 	}
 
 	public void doRevertToSaved() {
 		sourceEditor.doRevertToSaved();
 	}
 
 	public IAction getAction(String actionId) {
 		return sourceEditor.getAction(actionId);
 	}
 
 	public IDocumentProvider getDocumentProvider() {
 		return sourceEditor.getDocumentProvider();
 	}
 
 	public IRegion getHighlightRange() {
 		return sourceEditor.getHighlightRange();
 	}
 
 	public ISelectionProvider getSelectionProvider() {
 		return sourceEditor.getSelectionProvider();
 	}
 
 	public boolean isEditable() {
 		return sourceEditor.isEditable();
 	}
 
 	public void removeActionActivationCode(String actionId) {
 		sourceEditor.removeActionActivationCode(actionId);
 	}
 
 	public void resetHighlightRange() {
 		sourceEditor.resetHighlightRange();
 	}
 
 	public void selectAndReveal(int offset, int length) {
 		sourceEditor.selectAndReveal(offset, length);
 	}
 
 	public void setAction(String actionID, IAction action) {
 		sourceEditor.setAction(actionID, action);
 	}
 
 	public void setActionActivationCode(String actionId,
 			char activationCharacter, int activationKeyCode,
 			int activationStateMask) {
 		sourceEditor.setActionActivationCode(actionId, activationCharacter,
 				activationKeyCode, activationStateMask);
 	}
 
 	public void setHighlightRange(int offset, int length, boolean moveCursor) {
 		sourceEditor.setHighlightRange(offset, length, moveCursor);
 	}
 
 	public void showHighlightRangeOnly(boolean showHighlightRangeOnly) {
 		sourceEditor.showHighlightRangeOnly(showHighlightRangeOnly);
 	}
 
 	public boolean showsHighlightRangeOnly() {
 		return sourceEditor.showsHighlightRangeOnly();
 	}
 
 	public VpeTaglibManager getTaglibManager() {
 		if (sourceEditor != null) {
 			if (sourceEditor.getPageContext() instanceof VpeTaglibManager)
 
 				return (VpeTaglibManager) sourceEditor.getPageContext();
 		}
 		return null;
 	}
 
 	void updateTitle() {
 		setPartName(getEditorInput().getName());
 	}
 
 	public void runDropCommand(String flavor, String data) {
 		if (sourceEditor != null)
 			sourceEditor.runDropCommand(flavor, data);
 	}
 
 	public void setStatusField(IStatusField field, String category) {
 		if (sourceEditor != null)
 			sourceEditor.setStatusField(field, category);
 	}
 
 	public boolean isEditorInputReadOnly() {
 		if (sourceEditor != null) {
 			return sourceEditor.isEditorInputReadOnly();
 		}
 		return false;
 	}
 
 	public void addRulerContextMenuListener(IMenuListener listener) {
 		if (sourceEditor != null)
 			sourceEditor.addRulerContextMenuListener(listener);
 	}
 
 	public void removeRulerContextMenuListener(IMenuListener listener) {
 		if (sourceEditor != null)
 			sourceEditor.removeRulerContextMenuListener(listener);
 	}
 
 	public boolean isEditorInputModifiable() {
 		if (sourceEditor != null) {
 			return sourceEditor.isEditorInputModifiable();
 		}
 		return false;
 	}
 
 	public boolean validateEditorInputState() {
 		if (sourceEditor != null) {
 			return sourceEditor.validateEditorInputState();
 		}
 		return false;
 	}
 
 	public InsertMode getInsertMode() {
 		if (sourceEditor != null) {
 			return sourceEditor.getInsertMode();
 		}
 		return null;
 	}
 
 	public void setInsertMode(InsertMode mode) {
 		if (sourceEditor != null)
 			sourceEditor.setInsertMode(mode);
 	}
 
 	public void showChangeInformation(boolean show) {
 		if (sourceEditor != null)
 			sourceEditor.showChangeInformation(show);
 	}
 
 	public boolean isChangeInformationShowing() {
 		if (sourceEditor != null) {
 			return sourceEditor.isChangeInformationShowing();
 		}
 		return false;
 	}
 
 	public INavigationLocation createEmptyNavigationLocation() {
 		if (sourceEditor != null) {
 			return sourceEditor.createEmptyNavigationLocation();
 		}
 		return null;
 	}
 
 	public INavigationLocation createNavigationLocation() {
 		if (sourceEditor != null) {
 			return sourceEditor.createNavigationLocation();
 		}
 		return null;
 	}
 	
 	public void updatePartAccordingToPreferences() {
 		String tabIndex = JspEditorPlugin.getDefault().getPreferenceStore()
 				.getString(IVpePreferencesPage.DEFAULT_VPE_TAB);
 		try {
 			int ind = Integer.parseInt(tabIndex);
 			setActivePage(ind);
 			pageChange(ind);
 		} catch (NumberFormatException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 	}
 
 	/**
 	 * @return the previewIndex
 	 */
 	public int getPreviewIndex() {
 		return previewIndex;
 	}
 
 	/**
 	 * @param previewIndex the previewIndex to set
 	 */
 	public void setPreviewIndex(int previewIndex) {
 		this.previewIndex = previewIndex;
 	}
 	
 	public void setVisualSourceIndex(int visualSourceIndex) {
 		this.visualSourceIndex = visualSourceIndex;
 	}
 
 	public int getVisualSourceIndex() {
 		return visualSourceIndex;
 	}
 	
 	public IProject getProject() {
 		IProject pr = null;
 		IFile file = getFile();
 		if (null != file) {
 			pr = file.getProject();
 		}
 		return pr;
 	}
 
 	public boolean isXulRunnerBrowserNotSupported() {
 		return xulRunnerBrowserIsNotSupported;
 	}
 
 	public void setXulRunnerBrowserIsNotSupported(
 			boolean xulRunnerBrowserIsNotSupported) {
 		this.xulRunnerBrowserIsNotSupported = xulRunnerBrowserIsNotSupported;
 	}
 
 class ResourceChangeListener implements IResourceChangeListener {
 	IEditorPart editorPart;
 
 	Composite container;
 
 	ResourceChangeListener(IEditorPart editorPart, Composite container) {
 		this.editorPart = editorPart;
 		this.container = container;
 		IWorkspace workspace = ModelUIPlugin.getWorkspace();
 		if (workspace == null)
 			return;
 		workspace.addResourceChangeListener(this);
 		container.addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				IWorkspace workspace = ModelUIPlugin.getWorkspace();
 				if (workspace == null)
 					return;
 				workspace
 						.removeResourceChangeListener(ResourceChangeListener.this);
 			}
 		});
 	}
 
 	public void resourceChanged(IResourceChangeEvent event) {
 		final IEditorInput ei = editorPart.getEditorInput();
 		if (!(ei instanceof IFileEditorInput))
 			return;
 		IFileEditorInput fi = (IFileEditorInput) ei;
 		IFile f = fi.getFile();
 		if (f == null)
 			return;
 		IPath path = getPathChange(event, f);
 		if (path == null) {
 			if (f != null && !f.exists())
 				closeEditor();
 			return;
 		}
 		f = ModelPlugin.getWorkspace().getRoot().getFile(path);
 		XModelObject p = f == null ? null : EclipseResourceUtil
 				.getObjectByResource(f.getParent());
 		if (p instanceof FolderImpl) {
 			((FolderImpl) p).update();
 		}
 		final XModelObject o = EclipseResourceUtil.getObjectByResource(f);
 		if (f != null && f.exists() && o != null) {
 			if (editorPart instanceof JSPMultiPageEditor) {
 				final JSPMultiPageEditor e = (JSPMultiPageEditor) editorPart;
 				if (ei instanceof XModelObjectEditorInput) {
 					final IEditorInput e2 = XModelObjectEditorInput.createInstance(o);
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							e.setInput0(e2);
 							e.updateTitle();
 							if (e.getJspEditor() instanceof AbstractTextEditor) {
 								if (e.getJspEditor() != null
 										&& e.getJspEditor().getEditorInput() != e
 												.getEditorInput()) {
 									((AbstractTextEditor) e.getJspEditor())
 											.setInput(e2);
 								}
 								((XModelObjectEditorInput) ei).synchronize();
 							}
 						}
 					});
 				}
 			}
 		}
 		if (f == null || f.exists())
 			return;
 		closeEditor();
 	}
 
 	private void closeEditor() {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				editorPart.getSite().getPage().closeEditor(editorPart, false);
 			}
 		});
 	}
 
 	private IPath getPathChange(IResourceChangeEvent event, IFile f) {
 		return getPathChange(event.getDelta(), f.getFullPath());
 	}
 
 	private IPath getPathChange(IResourceDelta delta, IPath p) {
 		if (delta == null || delta.getFullPath() == null)
 			return null;
 		if (!delta.getFullPath().isPrefixOf(p))
 			return null;
 		if (delta != null && delta.getKind() == IResourceDelta.CHANGED) {
 			IResourceDelta[] ds = delta.getAffectedChildren();
 			if (ds == null)
 				return null;
 			if (ds.length > 1) {
 				IPath removed = null;
 				IPath added = null;
 				for (int i = 0; i < ds.length; i++) {
 					IPath d = ds[i].getFullPath();
 					if (ds[i].getKind() == IResourceDelta.REMOVED 
 							&& (d.equals(p) || d.isPrefixOf(p))) {
 						removed = d;
 					} else if (ds[i].getKind() == IResourceDelta.ADDED) {
 						added = d;
 					}
 				}
 				if (removed != null && added != null) {
 					IPath d = removed;
 					if (d.equals(p))
 						return added;
 					if (d.isPrefixOf(p)) {
 						return added.append(
 								p.removeFirstSegments(d.segmentCount()));
 					}
 				}
 			}
 			for (int i = 0; i < ds.length; i++) {
 				IPath ps = getPathChange(ds[i], p);
 				if (ps != null)
 					return ps;
 			}
 		}
 		return null;
 	}
 }
 
 	public static final QualifiedName PALETTE = new QualifiedName(JspEditorPlugin.PLUGIN_ID, "PaletteWasOpened");
 	public static final String PALETTE_VALUE = "true";
 	public static final String VIEW_ID = "org.eclipse.gef.ui.palette_view";
 
 	private void checkPalette() {
 		if(!wasPaletteOpened()) {
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					openPalette();
 				}
 			});
 			setPaletteOpened();
 		}
 	}
 
 	public static boolean wasPaletteOpened() {
 		try {
 			return PALETTE_VALUE.equals(ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(PALETTE));
 		} catch (CoreException e) {
 			JspEditorPlugin.getDefault().logError(e);
 			return true;
 		}
 	}
 
 	public static void resetPaletteOpened() {
 		try {
 			ResourcesPlugin.getWorkspace().getRoot().setPersistentProperty(PALETTE, null);
 		} catch (CoreException e) {
 			JspEditorPlugin.getDefault().logError(e);
 		}
 	}
 
 	private void openPalette() {
 		IWorkbenchWindow window = JspEditorPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
 		if(window != null) {
 			IWorkbenchPage page = window.getActivePage();
 			if(page != null) {
 				IViewPart part = page.findView(VIEW_ID);
 				if(part == null) {
 					try {
 						part = page.showView(VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
 					} catch (PartInitException e) {
 						JspEditorPlugin.getDefault().logError(e);
 					}
 				} else {
 					page.bringToTop(part);
 				}
 			}
 		}
 	}
 
 	private static void setPaletteOpened() {
 		try {
 			ResourcesPlugin.getWorkspace().getRoot().setPersistentProperty(PALETTE, PALETTE_VALUE);
 		} catch (CoreException e) {
 			JspEditorPlugin.getDefault().logError(e);
 		}
 	}
 
 }
