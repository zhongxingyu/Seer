 package org.amanzi.awe.views.network.view;
 
 import org.amanzi.awe.awe.views.view.provider.NetworkTreeContentProvider;
 import org.amanzi.awe.awe.views.view.provider.NetworkTreeLabelProvider;
 import org.amanzi.awe.views.network.NetworkTreePlugin;
 import org.amanzi.awe.views.network.property.NetworkPropertySheetPage;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.service.listener.NeoServiceProviderEventAdapter;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.*;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.jface.action.*;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.SWT;
 
 
 /**
  * NetworkTree View
  * 
  * @author Lagutko_N
  */
 
 public class NetworkTreeView extends ViewPart {
     
     /*
      * ID of this View
      */
     public static final String NETWORK_TREE_VIEW_ID = "org.amanzi.awe.networktree.views.NetworkTreeView";
     
     /*
      * TreeViewer for database Nodes
      */
 	private TreeViewer viewer;
 	
 	/*
 	 * NeoService provider
 	 */
 	private NeoServiceProvider neoServiceProvider;
 	
 	/*
 	 * PropertySheetPage for Properties of Nodes
 	 */
 	private IPropertySheetPage propertySheetPage;
 	
	/*
	 * Listener for Neo-Database Events
	 */
	private NeoServiceEventListener neoEventListener;
	
 	/**
 	 * The constructor.
 	 */
 	public NetworkTreeView() {
 	}
 	
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		
 		neoServiceProvider = NeoServiceProvider.getProvider();
		neoEventListener = new NeoServiceEventListener();
		neoServiceProvider.addServiceProviderListener(neoEventListener);
 		
 		setProviders(neoServiceProvider);
 		
 		viewer.setInput(getSite());
 		getSite().setSelectionProvider(viewer);
 		
 		hookContextMenu();
 	}
 	
 	/**
 	 * Set Label and Content providers for TreeView
 	 *
 	 * @param neoServiceProvider
 	 */
 	
 	private void setProviders(NeoServiceProvider neoServiceProvider) {	    
 	    viewer.setContentProvider(new NetworkTreeContentProvider(neoServiceProvider));
 	    viewer.setLabelProvider(new NetworkTreeLabelProvider(viewer));
 	}
 	
 	/**
 	 * Creates a popup menu
 	 */
 
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				NetworkTreeView.this.fillContextMenu(manager);
 			}
 		});
 		Menu menu = menuMgr.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);		
 		getSite().registerContextMenu(menuMgr, viewer);
 	}
 	
 	/**
 	 * Creates items for popup menu
 	 *
 	 * @param manager
 	 */
 
 	private void fillContextMenu(IMenuManager manager) {
 		manager.add(new Action("Properties") {
 		    public void run() {
 		        try {		            
 		            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(IPageLayout.ID_PROP_SHEET);
 		        } catch (PartInitException e) {
 		            NetworkTreePlugin.error(null, e);
 		        }
 		    }   
 		});
 	}
 	
 	public void dispose() {
 	    if (propertySheetPage != null) {
 	        propertySheetPage.dispose();
 	    }
 	    
	    if (neoEventListener != null) {
	        neoServiceProvider.removeServiceProviderListener(neoEventListener);
	    }
	    
 	    super.dispose();
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 	
 	/**
 	 * Returns (and creates is it need) property sheet page for this View
 	 *
 	 * @return PropertySheetPage
 	 */
 	
 	private IPropertySheetPage getPropertySheetPage() {
 	    if (propertySheetPage == null) {
 	        propertySheetPage = new NetworkPropertySheetPage();
 	    }
 	    
 	    return propertySheetPage;
 	}
 	
 	/**
      * This is how the framework determines which interfaces we implement.
      */
     @Override
     @SuppressWarnings( "unchecked" )
     public Object getAdapter( final Class key )
     {
         if ( key.equals( IPropertySheetPage.class ) )
         {
             return getPropertySheetPage();
         }
         else
         {
             return super.getAdapter( key );
         }
     }
     
     /**
      * NeoProvider Event listener for this View 
      * 
      * @author Lagutko_N
      * @since 1.1.0
      */
     
     private class NeoServiceEventListener extends NeoServiceProviderEventAdapter {
         
         public void onNeoStop(Object source) {
             neoServiceProvider.shutdown();
         }
         
         public void onNeoCommit(Object source) {
             //if some data was commited to database than we must refresh content of TreeView
             viewer.getControl().getDisplay().syncExec(new Runnable() {
                 public void run() {
                     NetworkTreeView.this.viewer.refresh();
                 }
             });            
         }
     }
 }
