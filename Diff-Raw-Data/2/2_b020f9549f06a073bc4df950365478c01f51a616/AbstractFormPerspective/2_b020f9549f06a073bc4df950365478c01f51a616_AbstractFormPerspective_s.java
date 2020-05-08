 package com.netxforge.netxstudio.ui.perspective;
 
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 
 /**
  * Fixed perspective. 
  * Editor area is not visible. 
  * 
  * @author Christophe Bouhier christophe.bouhier@netxforge.com
  */
 public abstract class AbstractFormPerspective implements IPerspectiveFactory {
 	
 	/**
 	 * Creates the initial layout for a page.
 	 */
 	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(true);
 		layout.setEditorAreaVisible(false);
 		addFastViews(layout);
 		addViewShortcuts(layout);
 		addPerspectiveShortcuts(layout);
 	}
 
 	/**
 	 * Add fast views to the perspective.
 	 */
 	private void addFastViews(IPageLayout layout) {
 	}
 
 	/**
 	 * Add view shortcuts to the perspective.
 	 */
 	private void addViewShortcuts(IPageLayout layout) {
 	}
 
 	/**
 	 * Add perspective shortcuts to the perspective.
 	 */
 	private void addPerspectiveShortcuts(IPageLayout layout) {
 		layout.addPerspectiveShortcut(DesignPerspective.ID);
 		layout.addPerspectiveShortcut(MonitoringPerspective.ID);
 		layout.addPerspectiveShortcut(AdminPerspective.ID);
 		layout.addPerspectiveShortcut(LibraryPerspective.ID);
 	}
 
 }
