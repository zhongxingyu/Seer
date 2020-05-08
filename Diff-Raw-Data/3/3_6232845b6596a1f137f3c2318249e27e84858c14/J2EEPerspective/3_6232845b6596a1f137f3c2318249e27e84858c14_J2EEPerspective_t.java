 /*******************************************************************************
  * Copyright (c) 2005, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Dec 7, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.j2ee.internal.perspective;
 
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.ui.IFolderLayout;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.progress.IProgressConstants;
 import org.eclipse.ui.views.IViewDescriptor;
 
 /**
  * 
  * TODO To change the template for this generated type comment go to Window - Preferences - Java -
  * Code Style - Code Templates
  */
 public class J2EEPerspective implements org.eclipse.ui.IPerspectiveFactory {
 
 	protected static String ID_SERVERS_VIEW = "org.eclipse.wst.server.ui.ServersView"; //$NON-NLS-1$
 	protected static String ID_J2EE_HIERARCHY_VIEW = "org.eclipse.ui.navigator.ProjectExplorer"; //$NON-NLS-1$
 
 	private static String ID_WST_SNIPPETS_VIEW = "org.eclipse.wst.common.snippets.internal.ui.SnippetsView"; //$NON-NLS-1$	
 	private static String ID_EJB_PROJECT_WIZARD = "org.eclipse.jst.ejb.ui.EJBComponentCreationWizard"; //$NON-NLS-1$
 	private static String ID_RAR_PROJECT_WIZARD = "org.eclipse.jst.j2ee.jca.ui.ConnectorComponentCreationWizard"; //$NON-NLS-1$
 	private static String ID_APPCLIENT_PROJECT_WIZARD = "org.eclipse.jst.j2ee.internal.wizard.AppClientComponentCreationWizard"; //$NON-NLS-1$
 	private static String ID_EAR_PROJECT_WIZARD = "org.eclipse.jst.j2ee.internal.wizard.EARComponentCreationWizard"; //$NON-NLS-1$
     private static String ID_DYNAMIC_WEB_COMPONENT_WIZARD = "org.eclipse.jst.servlet.ui.WebComponentCreationWizard"; //$NON-NLS-1$
 	private static String ID_STATIC_WEB_COMPONENT_WIZARD = "org.eclipse.wst.web.ui.internal.wizards.SimpleWebModuleCreation"; //$NON-NLS-1$
 	private static final String ID_SEARCH_VIEW = "org.eclipse.search.ui.views.SearchView"; //$NON-NLS-1$
 	private static final String ID_DATA_VIEW = "org.eclipse.datatools.connectivity.DataSourceExplorerNavigator"; //$NON-NLS-1$
 	public static final String ID_CONSOLE_VIEW= "org.eclipse.ui.console.ConsoleView"; //$NON-NLS-1$
 
 	public J2EEPerspective() {
 		super();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
 	 */
 	public void createInitialLayout(IPageLayout layout) {
 		defineLayout(layout);
 		defineActions(layout);
 	}
 	
 	
 
 	public void defineActions(IPageLayout layout) {
 		layout.addActionSet("org.eclipse.jst.j2ee.J2eeMainActionSet"); //$NON-NLS-1$
 		layout.addActionSet("org.eclipse.jdt.ui.JavaActionSet"); //$NON-NLS-1$
 
 		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
 		layout.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
 
 		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
 
 		layout.addShowViewShortcut(ID_J2EE_HIERARCHY_VIEW);
 		layout.addShowViewShortcut(ID_SERVERS_VIEW);
 		layout.addShowViewShortcut(ID_DATA_VIEW);
 		layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);
 		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
 		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
 		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
 		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
 		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
 		layout.addShowViewShortcut(ID_WST_SNIPPETS_VIEW);
 		
 		// views - search
 		layout.addShowViewShortcut(ID_SEARCH_VIEW);
 				// views - debugging
 		layout.addShowViewShortcut(ID_CONSOLE_VIEW);
 
 		layout.addNewWizardShortcut(ID_DYNAMIC_WEB_COMPONENT_WIZARD);
         layout.addNewWizardShortcut(ID_STATIC_WEB_COMPONENT_WIZARD);
 		layout.addNewWizardShortcut(ID_EJB_PROJECT_WIZARD);
         layout.addNewWizardShortcut(ID_RAR_PROJECT_WIZARD);
         layout.addNewWizardShortcut(ID_APPCLIENT_PROJECT_WIZARD);
         layout.addNewWizardShortcut(ID_EAR_PROJECT_WIZARD);
 
 		// Add "new wizards"
         layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.project");//$NON-NLS-1$
         layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
         layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
 
 		layout.addShowInPart(ID_J2EE_HIERARCHY_VIEW);
 	}
 
 	public void defineLayout(IPageLayout layout) {
 		// Editors are placed for free.
 		String editorArea = layout.getEditorArea();
 
 		// Top left.
 		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f, editorArea);//$NON-NLS-1$
 		topLeft.addView(ID_J2EE_HIERARCHY_VIEW);
 		topLeft.addPlaceholder(IPageLayout.ID_RES_NAV);
 		topLeft.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
 		topLeft.addPlaceholder(JavaUI.ID_PACKAGES_VIEW);
 
 		// Bottom right.
 		IFolderLayout bottomRight = layout.createFolder("bottomRight", IPageLayout.BOTTOM, 0.7f, editorArea);//$NON-NLS-1$
 		bottomRight.addView(IPageLayout.ID_PROBLEM_VIEW);
 		bottomRight.addView(IPageLayout.ID_TASK_LIST);
 		bottomRight.addView(IPageLayout.ID_PROP_SHEET);
 		bottomRight.addView(ID_SERVERS_VIEW);
 		addDBViewIfPresent(layout,bottomRight);
 		bottomRight.addView(ID_WST_SNIPPETS_VIEW);
 		
 		bottomRight.addPlaceholder(ID_CONSOLE_VIEW);
 		bottomRight.addPlaceholder(IPageLayout.ID_BOOKMARKS);
 		bottomRight.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
 		bottomRight.addPlaceholder(ID_SEARCH_VIEW);
 
 		// Top right.
 		IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.7f, editorArea);//$NON-NLS-1$
 		topRight.addView(IPageLayout.ID_OUTLINE);
 	}
 	private void addDBViewIfPresent(IPageLayout page,IFolderLayout bottomRight) {
 		// TODO Auto-generated method stub
 		IViewDescriptor dbView = PlatformUI.getWorkbench().getViewRegistry().find(ID_DATA_VIEW);
 		if (dbView != null)
 			bottomRight.addView(ID_DATA_VIEW);
 	}
 }
 
 
 
