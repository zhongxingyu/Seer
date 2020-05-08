 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.views;
 
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IDataChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProjectLoadedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.filter.JBPatternFilter;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.controllers.ComponentNameTreeViewerUpdater;
 import org.eclipse.jubula.client.ui.rcp.editors.PersistentObjectComparer;
 import org.eclipse.jubula.client.ui.rcp.filter.JBFilteredTree;
 import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.ComponentNameBrowserContentProvider;
 import org.eclipse.jubula.client.ui.rcp.sorter.ComponentNameNameViewerSorter;
 import org.eclipse.jubula.client.ui.rcp.utils.UIIdentitiyElementComparer;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.client.ui.views.ITreeViewerContainer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.dialogs.FilteredTree;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 
 
 /**
  * Browser to manage all component names in a project
  * 
  * @author BREDEX GmbH
  * @created 06.02.2009
  */
 public class ComponentNameBrowser extends ViewPart implements
         IProjectLoadedListener, ITreeViewerContainer, IJBPart,
         IDataChangedListener {
     /** Default expansion for the tree */
     public static final int DEFAULT_EXPANSION = 2;
 
     /**
      * Context menu id for the component names browser, required for declaration
      * of menu entries in plugin.xml
      */
     public static final String CONTEXT_MENU_ID = 
         "ComponentNameBrowserContextMenuID"; //$NON-NLS-1$
     
     /** the tree viewer */
     private TreeViewer m_treeViewer;
 
     /** updater for tree viewer based on changes to Component Names */
     private ComponentNameTreeViewerUpdater m_treeViewerUpdater;
     
     /**
      * {@inheritDoc}
      */
     public void createPartControl(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         composite.setLayout(GridLayoutFactory.fillDefaults().create());
         composite.setLayoutData(GridDataFactory.fillDefaults().create());
 
         final FilteredTree ft = new JBFilteredTree(composite, SWT.MULTI
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 
                new JBPatternFilter(), true);
         ComponentNameBrowserContentProvider cp = 
             new ComponentNameBrowserContentProvider();
 
         setTreeViewer(ft.getViewer());
        ColumnViewerToolTipSupport.enableFor(getTreeViewer());
 
         getTreeViewer().setContentProvider(cp);
         getTreeViewer().setComparer(new UIIdentitiyElementComparer());
 
        DecoratingLabelProvider lp = new DecoratingLabelProvider(cp, Plugin
                .getDefault().getWorkbench().getDecoratorManager()
                .getLabelDecorator());

        getTreeViewer().setLabelProvider(lp);
         
         getTreeViewer().setUseHashlookup(true);
         getTreeViewer().setAutoExpandLevel(DEFAULT_EXPANSION);
         getTreeViewer().setSorter(new ComponentNameNameViewerSorter());
         getTreeViewer().setComparer(new PersistentObjectComparer());
         getViewSite().setSelectionProvider(getTreeViewer());
 
         createTreeContextMenu(getViewSite());
 
         Plugin.getHelpSystem().setHelp(getTreeViewer().getControl(),
                 ContextHelpIds.COMPONENT_NAMES_BROWSER);
 
         m_treeViewerUpdater = new ComponentNameTreeViewerUpdater(
                 getTreeViewer());
 
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.addProjectLoadedListener(this, true);
         ded.addDataChangedListener(this, true);
         ded.addDataChangedListener(m_treeViewerUpdater, true);
 
         if (GeneralStorage.getInstance().getProject() != null) {
             handleProjectLoaded();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void setFocus() {
         getTreeViewer().getControl().setFocus();
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleProjectLoaded() {
         initTree();
     }
     
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.removeProjectLoadedListener(this);
         ded.removeDataChangedListener(this);
         ded.removeDataChangedListener(m_treeViewerUpdater);
         m_treeViewerUpdater = null;
         super.dispose();
     }
     
     /**
      * initializes the tree input
      */
     protected void initTree() {
         final IProjectPO cProject = GeneralStorage.getInstance().getProject();
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 getTreeViewer().setInput(cProject);
             }
         });
     }
 
     /**
      * Create context menu for the tree-based editor view.
      * 
      * @param viewSite
      *            the viewSite
      */
     private void createTreeContextMenu(IViewSite viewSite) {
         // Create menu manager and menu
         MenuManager menuMgr = new MenuManager();
         Menu menu = menuMgr.createContextMenu(getTreeViewer().getControl());
         getTreeViewer().getControl().setMenu(menu);
         // Register menu for extension.
         viewSite.registerContextMenu(CONTEXT_MENU_ID, menuMgr, getTreeViewer());
     }
 
     /**
      * {@inheritDoc}
      */
     public TreeViewer getTreeViewer() {
         return m_treeViewer;
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getAdapter(Class adapter) {
         if (adapter.equals(IPropertySheetPage.class)) {
             return new JBPropertiesView(false, null);
         }
         return super.getAdapter(adapter);
     }
     
     /**
      * @param treeViewer
      *            the treeViewer to set
      */
     private void setTreeViewer(TreeViewer treeViewer) {
         m_treeViewer = treeViewer;
     }
 
     /** {@inheritDoc} */
     public void handleDataChanged(DataChangedEvent... events) {
         boolean refreshView = false;
         for (DataChangedEvent e : events) {
             if (e.getUpdateState() != UpdateState.onlyInEditor
                     && e.getPo() instanceof IComponentNamePO) {
                 refreshView = true;
                 break;
             }
         }
         if (refreshView) {
             // retrieve tree state
             Object[] expandedElements = getTreeViewer().getExpandedElements();
             ISelection selection = getTreeViewer().getSelection();
 
             // refresh treeview
             getTreeViewer().refresh();
 
             // restore tree status
             getTreeViewer().setExpandedElements(expandedElements);
             getTreeViewer().setSelection(selection);
         }
     }
 }
