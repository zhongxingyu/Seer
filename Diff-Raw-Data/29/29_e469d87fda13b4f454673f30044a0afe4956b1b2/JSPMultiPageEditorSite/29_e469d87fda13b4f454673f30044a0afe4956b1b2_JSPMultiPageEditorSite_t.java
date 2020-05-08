 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.jspeditor;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jst.jsp.core.internal.provisional.contenttype.ContentTypeIdForJSP;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorActionBarContributor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IKeyBindingService;
 import org.eclipse.ui.INestableKeyBindingService;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.KeyBindingService;
 import org.eclipse.ui.internal.PopupMenuExtender;
 import org.eclipse.ui.internal.WorkbenchPlugin;
 import org.eclipse.ui.internal.services.IServiceLocatorCreator;
 import org.eclipse.ui.internal.services.IWorkbenchLocationService;
 import org.eclipse.ui.internal.services.ServiceLocator;
 import org.eclipse.ui.internal.services.WorkbenchLocationService;
 import org.eclipse.ui.services.IServiceScopes;
 
 /**
  * 
  * @author eskimo(dgolovin@exadel.com)
  * 
  */
 public abstract class JSPMultiPageEditorSite implements IEditorSite {
 
 	private IEditorPart fEditorPart;
 
 	private JSPMultiPageEditorPart fEditor;
 
 	private ISelectionChangedListener fSelChangeListener = null;
 
 	//private IKeyBindingService fParentService = null;
 
 	private IKeyBindingService fService = null;
 
 	private ArrayList fMenuExts;
 
 	private final ServiceLocator serviceLocator;
 	
 	public JSPMultiPageEditorSite(JSPMultiPageEditorPart multiPageEditor,
 			IEditorPart editor) {
 		Assert.isNotNull(multiPageEditor);
 		Assert.isNotNull(editor);
 		this.fEditor = multiPageEditor;
 		this.fEditorPart = editor;
 		
		/*
		 * https://issues.jboss.org/browse/JBIDE-11578
		 * IDisposable was removed to fix compilation error in Juno.
		 */
 		this.serviceLocator = (ServiceLocator) ((IServiceLocatorCreator) multiPageEditor
 				.getSite().getService(IServiceLocatorCreator.class))
				.createServiceLocator(multiPageEditor.getSite(), null, null);
	
 		serviceLocator.registerService(IWorkbenchLocationService.class,
 				new WorkbenchLocationService(IServiceScopes.MPESITE_SCOPE,
 						getWorkbenchWindow().getWorkbench(),
 						getWorkbenchWindow(), multiPageEditor.getSite(), this,
 						null, 3));
 	}
 
 	public void dispose() {
 		if (fMenuExts != null) {
 			for (int i = 0; i < fMenuExts.size(); i++) {
 				((PopupMenuExtender) fMenuExts.get(i)).dispose();
 			}
 			fMenuExts = null;
 		}
 
 		if (fService != null) {
 			IKeyBindingService parentService = getEditor().getSite()
 					.getKeyBindingService();
 			if (parentService instanceof INestableKeyBindingService) {
 				INestableKeyBindingService nestableParent = (INestableKeyBindingService) parentService;
 				nestableParent.removeKeyBindingService(this);
 			}
 			if (fService instanceof KeyBindingService) {
 				((KeyBindingService) fService).dispose();
 			}
 			fService = null;
 		}
 		if (fSelChangeListener != null) {
 			getSelectionProvider().removeSelectionChangedListener(fSelChangeListener);
 			fSelChangeListener = null;
 		}
 		if (serviceLocator != null) {
 			serviceLocator.dispose();
 		}
 	}
 
 	public IEditorActionBarContributor getActionBarContributor() {
 		return null;
 	}
 
 	public IActionBars getActionBars() {
 		return fEditor.getEditorSite().getActionBars();
 	}
 
 	public ILabelDecorator getDecoratorManager() {
 		return getWorkbenchWindow().getWorkbench().getDecoratorManager()
 				.getLabelDecorator();
 	}
 
 	public IEditorPart getEditor() {
 		return fEditorPart;
 	}
 
 	public String getId() {
 		return ContentTypeIdForJSP.ContentTypeID_JSP + ".source"; //$NON-NLS-1$; 
 	}
 
 	public IKeyBindingService getKeyBindingService() {
 		if (fService == null) {
 			fService = getMultiPageEditor().getEditorSite()
 					.getKeyBindingService();
 			//fParentService = fService;
 			if (fService instanceof INestableKeyBindingService) {
 				INestableKeyBindingService nestableService = (INestableKeyBindingService) fService;
 				fService = nestableService.getKeyBindingService(this);
 
 			} else {
 				WorkbenchPlugin
 						.log("MultiPageEditorSite.getKeyBindingService()   Parent key binding fService was not an instance of INestableKeyBindingService.  It was an instance of " + fService.getClass().getName() + " instead."); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		return fService;
 	}
 
 	public JSPMultiPageEditorPart getMultiPageEditor() {
 		return fEditor;
 	}
 
 	public IWorkbenchPage getPage() {
 		return getMultiPageEditor().getSite().getPage();
 	}
 
 	public String getPluginId() {
 		return ""; //$NON-NLS-1$
 	}
 
 	public String getRegisteredName() {
 		return ""; //$NON-NLS-1$
 	}
 
 	protected ISelectionChangedListener getSelectionChangedListener() {
 		if (fSelChangeListener == null) {
 			fSelChangeListener = new ISelectionChangedListener() {
 				public void selectionChanged(SelectionChangedEvent event) {
 					JSPMultiPageEditorSite.this.handleSelectionChanged(event);
 				}
 			};
 		}
 		return fSelChangeListener;
 	}
 
 	public Shell getShell() {
 		return getMultiPageEditor().getSite().getShell();
 	}
 
 	public IWorkbenchWindow getWorkbenchWindow() {
 		if(getMultiPageEditor().getSite() == null) // fix JBIDE-2218
 			return null;						   // fix JBIDE-2218
 		return getMultiPageEditor().getSite().getWorkbenchWindow();
 	}
 
 	protected void handleSelectionChanged(SelectionChangedEvent event) {
 		ISelectionProvider parentProvider = getMultiPageEditor().getSite()
 				.getSelectionProvider();
 		if (parentProvider instanceof JSPMultiPageSelectionProvider) {
 			SelectionChangedEvent newEvent = new SelectionChangedEvent(
 					parentProvider, event.getSelection());
 			((JSPMultiPageSelectionProvider) parentProvider)
 					.fireSelectionChanged(newEvent);
 		}	
 	}
 
 	public void registerContextMenu(String menuID, MenuManager menuMgr,
 			ISelectionProvider selProvider) {
 		if (fMenuExts == null) {
 			fMenuExts = new ArrayList(1);
 		}
 		PopupMenuExtender extender = findMenuExtender(menuMgr, selProvider);
 		if (findMenuExtender(menuMgr, selProvider) == null) {
 			extender = new PopupMenuExtender(menuID, menuMgr,
 					selProvider, fEditorPart);
 			fMenuExts.add(extender);
 		}else {
 			extender.addMenuId(menuID);
 		}
 	}
 
 	private PopupMenuExtender findMenuExtender(MenuManager menuMgr,
 			ISelectionProvider selProvider) {
 		for (int i = 0; fMenuExts != null && i < fMenuExts.size(); i++) {
 			PopupMenuExtender extender = (PopupMenuExtender) fMenuExts
 					.get(i);
 			if (extender.matches(menuMgr, selProvider, fEditorPart))
 				return extender;
 		}
 		return null;
 	}
 
 	public void registerContextMenu(MenuManager menuManager,
 			ISelectionProvider selProvider) {
 		getMultiPageEditor().getSite().registerContextMenu(menuManager,
 				selProvider);
 	}
 
 	public void progressEnd(Job job) {
 	}
 
 	public void progressStart(Job job) {
 	}
 
 	public Object getAdapter(Class adapter) {
 		return null;
 	}
 
 	public IWorkbenchPart getPart() {
 		return this.fEditor;
 	}
 
 	public final void registerContextMenu(final MenuManager menuManager,
 			final ISelectionProvider selectionProvider,
 			final boolean includeEditorInput) {
 		registerContextMenu(getId(), menuManager, selectionProvider,
 				includeEditorInput);
 	}
 
 	public final void registerContextMenu(final String menuId,
 			final MenuManager menuManager,
 			final ISelectionProvider selectionProvider,
 			final boolean includeEditorInput) {
 		if (fMenuExts == null) {
 			fMenuExts = new ArrayList(1);
 		}
 	}
 	
 	public Object getService(Class api) {
 		// TODO megration to eclipse 3.2
 		return this.serviceLocator.getService(api);
 	}
 
 	public boolean hasService(Class api) {
 		// TODO megration to eclipse 3.2
 		return this.serviceLocator.hasService(api);
 	}
 	
 	/**
 	 * Notifies the multi page editor service that the component within which it 
 	 * exists has become active.
 	 * 
 	 * @since 3.2
 	 */
 	public final void activate() {
 		serviceLocator.activate();
 	}
 
 	/**
 	 * Notifies the multi page editor service that the component within which it 
 	 * exists has been deactived.
 	 * 
 	 * @since 3.2
 	 */
 	public final void deactivate() {
 		serviceLocator.deactivate();
 	}
 }
