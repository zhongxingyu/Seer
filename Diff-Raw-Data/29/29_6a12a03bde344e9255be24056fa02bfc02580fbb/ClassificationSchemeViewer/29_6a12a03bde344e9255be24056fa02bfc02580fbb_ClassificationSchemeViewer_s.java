 package org.iucn.sis.shared.api.schemes;
 
 import java.util.List;
 
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 
 public interface ClassificationSchemeViewer {
 	
 	/**
 	 * Draw the interface.
 	 * @param isViewOnly true if it should be drawn 
 	 * 		in view-only mode, false otherwise
 	 * @return the widget containing your interface
 	 */
 	public LayoutContainer draw(final boolean isViewOnly);
 	
 	/**
 	 * Determine if changes have been made.
 	 * @return true if so, false otherwise
 	 */
 	public boolean hasChanged();
 	
 	/**
 	 * Undo any changes and revert back to the last 
 	 * saved state.
 	 */
 	public void revert();
 	
 	/**
 	 * Return the current set of data and update 
	 * and change markers. 
 	 * @return the data
 	 */
	public List<ClassificationSchemeModelData> save();
 	
 	/**
 	 * Provides the initial data set for the viewer.  This 
 	 * will be called before draw()
 	 * @param models the data
 	 */
 	public void setData(List<ClassificationSchemeModelData> models);
 
 }
