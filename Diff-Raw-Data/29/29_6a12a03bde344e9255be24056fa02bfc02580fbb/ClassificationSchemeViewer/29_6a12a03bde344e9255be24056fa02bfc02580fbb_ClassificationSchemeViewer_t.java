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
	 * @param deep - true if the data is saved to the server 
	 * and the internal hasChanged marker can be cleared, 
	 * false if this save is being done only to display the 
	 * data but it is still not saved to the server. 
 	 * @return the data
 	 */
	public List<ClassificationSchemeModelData> save(boolean deep);
 	
 	/**
 	 * Provides the initial data set for the viewer.  This 
 	 * will be called before draw()
 	 * @param models the data
 	 */
 	public void setData(List<ClassificationSchemeModelData> models);
 
 }
