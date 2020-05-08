 package org.nuxeo.ide.studio.views;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.DrillDownAdapter;
 import org.eclipse.ui.part.ViewPart;
 import org.nuxeo.ide.studio.StudioPlugin;
 import org.nuxeo.ide.studio.StudioProject;
 import org.nuxeo.ide.studio.actions.AddFeatureAction;
 import org.nuxeo.ide.studio.actions.CollapseAllAction;
 import org.nuxeo.ide.studio.actions.DeleteFeatureAction;
 import org.nuxeo.ide.studio.actions.ExpandAllAction;
 import org.nuxeo.ide.studio.actions.RefreshAction;
 import org.nuxeo.ide.studio.data.Node;
 import org.nuxeo.ide.studio.data.model.Feature;
 import org.nuxeo.ide.studio.data.model.FeatureHelper;
 import org.nuxeo.ide.studio.editors.StudioEditor;
 import org.nuxeo.ide.studio.editors.StudioEditorInput;
 
 
 /**
  * This sample class demonstrates how to plug-in a new
  * workbench view. The view shows data obtained from the
  * model. The sample creates a dummy model on the fly,
  * but a real implementation would connect to the model
  * available either in this or another plug-in (e.g. the workspace).
  * The view is connected to the model using a content provider.
  * <p>
  * The view uses a label provider to define how model
  * objects should be presented in the view. Each
  * view can present the same model objects using
  * different labels and icons, if needed. Alternatively,
  * a single label provider can be shared between views
  * in order to ensure that objects of the same type are
  * presented in the same way everywhere.
  * <p>
  */
 
 public class StudioBrowserView extends ViewPart {
 
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "org.nuxeo.ide.studio.views.StudioBrowserView";
 
 	private TreeViewer viewer;
 
     private DrillDownAdapter drillDownAdapter;
 	private Action doubleClickAction;
 
 	private Combo projectList;
 
 	private Action refresh;
 	private Action addFeature;
 	private Action deleteFeature;
 	private Action expandAll;
 	private Action collapseAll;
 
 
 	class NameSorter extends ViewerSorter {
 	}
 
 	/**
 	 * The constructor.
 	 */
 	public StudioBrowserView() {
 	}
 
 	public void refresh() {
 	    String project = projectList.getText();
 	    if ( project != null && project.trim().length() > 0 ) {
 	        viewer.setInput(FeatureHelper.buildFeatureTree(project));
 	    }
 	}
 
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 
 	    Composite composite = new Composite(parent, SWT.NONE);
         composite.setLayout(new GridLayout(1, false));
 
 
 	    // project navigator
 	    projectList = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
 	    GridDataFactory.fillDefaults().grab(true, false).applyTo(projectList);
 
 	    // the navigator
 		viewer = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
 
 		drillDownAdapter = new DrillDownAdapter(viewer);
 		viewer.setContentProvider(new StudioContentProvider());
 		viewer.setLabelProvider(new StudioLabelProvider());
 		viewer.setSorter(new NameSorter());
 
 
 		// Create the help context id for the viewer's control
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.nuxeo.ide.studio.views.viewer");
 		makeActions();
 		hookContextMenu();
 		hookDoubleClickAction();
 		contributeToActionBars();
 
 
 		projectList.addSelectionListener(new SelectionListener() {
 
 		    public void widgetSelected(SelectionEvent e) {
 		        refresh();
 		    }
 
 		    public void widgetDefaultSelected(SelectionEvent e) {
 
 		    }
 		});
 		refreshProjects();
 	}
 
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				StudioBrowserView.this.fillContextMenu(manager);
 			}
 		});
 		Menu menu = menuMgr.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer);
 	}
 
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 
 	private void fillLocalPullDown(IMenuManager manager) {
 		manager.add(addFeature);
 		manager.add(deleteFeature);
 	}
 
 	private void fillContextMenu(IMenuManager manager) {
 		manager.add(addFeature);
 		manager.add(deleteFeature);
 		manager.add(new Separator());
 		drillDownAdapter.addNavigationActions(manager);
 		// Other plug-ins can contribute there actions here
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 	}
 
 	private void fillLocalToolBar(IToolBarManager manager) {
 	    manager.add(refresh);
 	    manager.add(addFeature);
 	    manager.add(deleteFeature);
 	    manager.add(new Separator());
 		manager.add(expandAll);
 		manager.add(collapseAll);
 		manager.add(new Separator());
 		drillDownAdapter.addNavigationActions(manager);
 	}
 
 	private void makeActions() {
 	    refresh = new RefreshAction(this);
 
	    addFeature = new AddFeatureAction(this);
 	    deleteFeature = new DeleteFeatureAction(viewer);
 	    expandAll = new ExpandAllAction(viewer);
         collapseAll = new CollapseAllAction(viewer);
 
 		doubleClickAction = new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 //				showMessage("Double-click detected on "+obj.toString());
 
 				if ( obj instanceof Node) {
 				    Node node = (Node)obj;
 				    if ( node.getKey() != null) {
 				        try {
 		                    IWorkbenchWindow window=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		                    IWorkbenchPage page = window.getActivePage();
 		                    page.openEditor(new StudioEditorInput((Node) obj), StudioEditor.ID);
 		                } catch (PartInitException e) {
 		                    e.printStackTrace();
 		                }
 				    } else {
 				        if ( viewer.getExpandedState(obj)) {
 				            viewer.collapseToLevel(obj, 1);
 				        } else {
 				            viewer.expandToLevel(obj, 1);
 				        }
 				    }
 				}
 
 			}
 		};
 	}
 
 	private void hookDoubleClickAction() {
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				doubleClickAction.run();
 			}
 		});
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	public void refreshProjects() {
 	    StudioProject[] projects  = StudioPlugin.getDefault().getProvider().getProjects();
 	    projectList.clearSelection();
 	    for ( StudioProject project : projects ){
 	        projectList.add(project.getId());
 	    }
 	    if ( projectList.getItemCount() > 0 ) {
             projectList.select(0);
         }
 	    refresh();
 	}
 
     /**
      * @param id2
      */
     public Node selectNode(String featureId) {
         String projectName = projectList.getText();
         if ( projectName != null) {
             Feature[] features = StudioPlugin.getDefault().getProvider().getFeatures(projectName);
             for ( Feature feature : features ){
                 if (featureId.equals(feature.getId())){
                     viewer.setExpandedState(feature, true);
                     viewer.expandToLevel(feature, 1);
                     return feature;
                 }
             }
         }
         return null;
 
     }
 
    public String getProjectName(){
        return projectList.getText();
    }
 
 
 }
