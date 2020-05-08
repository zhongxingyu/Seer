 package org.dawb.common.ui.plot;
 
 import java.net.URL;
 import java.util.List;
 
 import org.dawb.common.ui.Activator;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.plot.tool.IToolChangeListener;
 import org.dawb.common.ui.plot.tool.IToolPage;
 import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.plot.tool.ToolChangeEvent;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.osgi.framework.Bundle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Class to deal with the extra actions we need in the plotting system.
  * 
  * Some actions are provided by the plotting system implementation, others are dealt with here.
  * 
  * You may override this class to add additional actions.
  * 
  * @author fcp94556
  *
  */
 public class PlottingActionBarManager {
 
 	private static final Logger logger = LoggerFactory.getLogger(PlottingActionBarManager.class);
 	
 	// Extrac actions for 1D and image viewing
 	protected List<IAction> extraImageActions;
 	protected List<IAction> extra1DActions;
 	protected AbstractPlottingSystem system;
 	
 	public PlottingActionBarManager(AbstractPlottingSystem system) {
 		this.system = system;
 	}
 	
 
 	public List<IAction> getExtraImageActions() {
 		return extraImageActions;
 	}
 
 	public void setExtraImageActions(List<IAction> extraImageActions) {
 		this.extraImageActions = extraImageActions;
 	}
 
 	public List<IAction> getExtra1DActions() {
 		return extra1DActions;
 	}
 
 	public void setExtra1DActions(List<IAction> extra1dActions) {
 		extra1DActions = extra1dActions;
 	}
 
 	
 	public void dispose() {
 		
 		if (extraImageActions!=null) extraImageActions.clear();
 		extraImageActions = null;
 		
 		if (extra1DActions!=null) extra1DActions.clear();
 		extra1DActions = null;
 
 	}
 	
 	private boolean isToolsRequired = true;
 	
 	public void setToolsRequired(boolean isToolsRequired) {
 		this.isToolsRequired = isToolsRequired;
 	}
 	
 
 	/**
 	 * Return a MenuAction which can be attached to the part using the plotting system.
 	 * 
 	 * 
 	 * @return
 	 */
 	protected MenuAction createToolActions(final ToolPageRole role, final String viewId) throws Exception {
 		
 		if (!isToolsRequired) return null;
 		
 		final IWorkbenchPart part = system.getPart();
		if (part==null)  return null;
 		
 		final MenuAction toolActions = new MenuAction(role.getLabel());
 		toolActions.setToolTipText(role.getTooltip());
 		toolActions.setImageDescriptor(Activator.getImageDescriptor(role.getImagePath()));
 		toolActions.setId(role.getId());
 	       	
 		// This list will not be large so we loop over it more than once for each ToolPageRole type
 	    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.toolPage");
 	    boolean foundSomeActions = false;
 	    for (IConfigurationElement e : configs) {
 			
 	    	final IToolPage page  = (IToolPage)e.createExecutableExtension("class");
 	    	if (page.getToolPageRole()!=role) continue;
 		    
 	    	foundSomeActions = true;
 	    	final String    label = e.getAttribute("label");
 	    	page.setToolSystem(system);
 	    	page.setPlottingSystem(system);
 	    	page.setTitle(label);
 	    	page.setPart(system.getPart());
 	    	
 	    	final Action    action= new Action(label) {
 	    		public void run() {		
 	    			
 	    			IViewPart viewPart=null;
 	    			try {
 	    				viewPart = EclipseUtils.getActivePage().showView(viewId);
 						
 						if (viewPart!=null && viewPart instanceof IToolChangeListener) {
 							system.addToolChangeListener((IToolChangeListener)viewPart);
 						}
 					} catch (PartInitException e) {
 						logger.error("Cannot find a view with id org.dawb.workbench.plotting.views.ToolPageView", e);
 					}
 	    			
 	    			final IToolPage old = system.getCurrentToolPage(role);
 	    			system.setCurrentToolPage(page);
 	    			system.clearRegionTool();
 	    			system.fireToolChangeListeners(new ToolChangeEvent(this, old, page, system.getPart()));
 	    			
 	    			toolActions.setSelectedAction(this);
 	    			
 	    		}
 	    	};
 	    	
 	    	action.setId(e.getAttribute("id"));
 	    	final String   icon  = e.getAttribute("icon");
 	    	if (icon!=null) {
 		    	final String   id    = e.getContributor().getName();
 		    	final Bundle   bundle= Platform.getBundle(id);
 		    	final URL      entry = bundle.getEntry(icon);
 		    	final ImageDescriptor des = ImageDescriptor.createFromURL(entry);
 		    	action.setImageDescriptor(des);
 		    	page.setImageDescriptor(des);
 	    	}
 	    	
 	    	final String    tooltip = e.getAttribute("tooltip");
 	    	if (tooltip!=null) action.setToolTipText(tooltip);
 	    	
 	    	toolActions.add(action);
 		}
 	
 	    if (!foundSomeActions) return null;
 	    
      	final Action    clear = new Action("Clear tool") {
 
 			public void run() {		
     			
     			final IToolPage old = system.getCurrentToolPage(role);
     			
     			system.setCurrentToolPage(system.getEmptyTool());
     			system.clearRegionTool();
     			system.fireToolChangeListeners(new ToolChangeEvent(this, old, system.getEmptyTool(), system.getPart()));
      			
     			toolActions.setSelectedAction(this);
     		}
     	};
     	clear.setImageDescriptor(Activator.getImageDescriptor("icons/axis.png"));
     	clear.setToolTipText("Clear tool previously used if any.");
 	    toolActions.add(clear);
 
 	    return toolActions;
 	}
 
 }
