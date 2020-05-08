 package org.dawb.common.ui.plot.tool;
 
 import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
import org.eclipse.ui.IWorkbenchPart;
 
 /**
  * This system allows one to get the page
  * which corresponds to the tool the user would
  * like to use with their plotting system. The
  * page may contain its own plotting or it may
  * create selection regions on the main IPlottingSystem.
  * 
  * @author fcp94556
  *
  */
 public interface IToolPageSystem {
 
 	/**
 	 * Get the current tool page that the user would like to use.
 	 * Fitting, profile, derivative etc. Null if no selection has been made.
 	 * @param role, may be null to get last used page.
 	 * @return tool page with may be EmptyTool or null
 	 */
 	public IToolPage getCurrentToolPage(ToolPageRole role);
 	
 	/**
 	 * Add a tool change listener. If the user changes preferred tool
 	 * this listener will be called so that any views showing the current
 	 * tool are updated. This method is always implemented as a HashSet
 	 * to avoid duplicates being added.
 	 * 
 	 * @param l
 	 */
 	public void addToolChangeListener(IToolChangeListener l);
 	
 	/**
 	 * Remove a tool change listener if one has been addded.
 	 * @param l
 	 */
 	public void removeToolChangeListener(IToolChangeListener l);
 
 	/**
 	 * Get a tool page by id for this tool page system.
 	 * @param toolId
 	 * @return
 	 */
 	public IToolPage getToolPage(String toolId);
 	
 	/**
 	 * The tool system keeps a reference to all tools.
 	 * 
 	 * Calling this method removes this tool from the cache of tools
 	 * (and leaves a new stub in its place). It then
 	 * disposes the UI of the tool, if one has been created. The dispose()
 	 * method of the tool will also be called.
 	 */
 	public void disposeToolPage(String id);
 	
 	/**
 	 * Creates a new tool page using the id.
 	 * @param toolId
 	 * @return
 	 */
 	public IToolPage createToolPage(String toolId) throws Exception;
 
 	/**
 	 * Clears any cached tools, can be used during dispose methods.
 	 */
 	public void clearCachedTools();
 
	/**
	 * If the system is visible to the user and active,
	 * it will return true here.
	 * @param active the active part that this system may be active inside.
	 * @return
	 */
	public boolean isActive(IWorkbenchPart active);

 }
