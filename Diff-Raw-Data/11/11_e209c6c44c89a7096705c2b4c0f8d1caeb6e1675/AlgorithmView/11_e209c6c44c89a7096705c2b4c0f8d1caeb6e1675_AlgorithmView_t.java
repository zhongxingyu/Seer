 package org.dawnsci.algorithm.ui.views.runner;
 
import java.io.IOException;
 import java.net.URL;
 
import org.dawb.common.util.eclipse.BundleUtils;
 import org.dawnsci.algorithm.ui.Activator;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 import org.osgi.framework.Bundle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * This view can run arbitrary algorithms (e.g. workflows) with a custom UI. Use it as follows:
  * 
  * 0. Depend on this plugin.
  * 1. In your plugin declare a view with an id using this class.
  * 2. In your plugin define an extension point for org.dawnsci.algorithm.ui.processPage and
  *    give it the id of your view. It will reference a class extending AbstractAlgorithmProcessPage
  *    most likely.
  *    
  * Now your view will have the custom UI and be able to run workflows using the 
  * IAlgorithmContext passed into the run method when the run action is pressed.
  * 
  * @author fcp94556
  *
  */
 public class AlgorithmView extends ViewPart {
 	
 	private static final Logger logger = LoggerFactory.getLogger(AlgorithmView.class);
 	
 	private IAlgorithmProcessPage runner;
 	private Composite             component;
 	private IConfigurationElement configuration;
 	
 	/**
 	 *  Gets the IWorkflowRunConfiguration from the extension point.
 	 */
     public void init(IViewSite site) throws PartInitException {
         super.init(site);
         createWorkflowRunPage(site);
     }
 
     public void init(IViewSite site, IMemento memento) throws PartInitException {
     	super.init(site);
     	createWorkflowRunPage(site);
     	runner.init(site, memento);
     }
     
 	@Override
 	public Object getAdapter(final Class clazz) {
 		if (runner!=null ) {
 			Object adapter = runner.getAdapter(clazz);
 			if (adapter!=null) return adapter;
 		}
 		if (clazz == IAlgorithmProcessPage.class) return runner; // breaks encapsulation but useful sometimes.
 		return super.getAdapter(clazz);
 	}
 
 
     public void saveState(IMemento memento){
     	runner.saveState(memento);
     }
 
 	private void createWorkflowRunPage(IViewSite site) {
 		try {
 	        final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.algorithm.ui.processPage");
 	        for (IConfigurationElement e : elements) {
 	        	final String id = e.getAttribute("viewId");
 	        	if (id.equals(site.getId())) {
 	        		runner = (IAlgorithmProcessPage)e.createExecutableExtension("class");
 	        		configuration = e;
 	        		
 	        		break;
 	        	}
 	        	
 			}
 		} catch (Throwable ne) {
 			logger.error("Cannot assign the IWorkflowRunPage for '"+site.getId()+"'. Invalid view part created! Configuration error - please fix this.");
 		    return;
 		}
 		if (runner!=null) runner.setAlgorithmView(this);
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		createActions();
 		component  = runner.createPartControl(parent);
 		
 		// TODO Do we need a component at the bottom
 		// with start and stop buttons?
 		//createButtons();
 	}
 
 	private IAlgorithmProcessContext context;
 	
 	private void createActions() {
 
 		IConfigurationElement[] elements = configuration.getChildren("connection");
         if (elements!=null && elements.length>0) {
         	
         	for (IConfigurationElement e : elements) {
         		
             	final String title = e.getAttribute("title");
             	 
 		    	final String   id     = e.getContributor().getName();
 		    	final Bundle   bundle = Platform.getBundle(id);
 		    	
          		URL      entry = bundle.getEntry(e.getAttribute("algorithmFile"));
        		String filePath=null;
				try {
					filePath = BundleUtils.getBundleLocation(bundle)+entry.getFile();
				} catch (IOException e1) {
					logger.error("Cannot get algorithm file!");
				}
          
         		entry = bundle.getEntry(e.getAttribute("stopIcon"));
  		    	final ImageDescriptor stopIcon = ImageDescriptor.createFromURL(entry);
 
 		    	entry = bundle.getEntry(e.getAttribute("runIcon"));
 		    	final ImageDescriptor runIcon = ImageDescriptor.createFromURL(entry);
 
                 createRunActions(title, filePath, runIcon, stopIcon);
 			}
         	
         } else {
         	    	
             createRunActions(runner.getTitle(), null, 
             		         Activator.getImageDescriptor("icons/run_workflow.gif"), 
             		         Activator.getImageDescriptor("icons/stop_workflow.gif"));
 
         }
 		
 	}
 
 	private void createRunActions(final String          title,
 			                      final String          filePath,
 			                      final ImageDescriptor runIcon,
 			                      final ImageDescriptor stopIcon) {
 		
 		final Action stopAction = new Action("Stop "+title, stopIcon) {
 			public void run() {
 				try {
 					if (context!=null) context.stop();
 				} catch (Exception e) {
 					logger.error(e.getMessage(), e);
 				}
 			}
 		};
 		stopAction.setId(IAlgorithmProcessContext.STOP_ID_STUB+title);
 	
 		Action runAction = new Action("Run "+title, runIcon) {
 			@Override
 			public void run() {
 				try {
 					stopAction.setEnabled(true);
 					if (context!=null) context.stop();
 					context = new AlgorithmProcessContext(AlgorithmView.this, runner.getSourceProviders());
 					context.setFilePath(filePath);
 					context.setTitle(title);
 					runner.run(context);
 					
 				} catch (Exception e) {
 					logger.error(e.getMessage(), e);
 				} finally {
 					if (context!=null && context.isRunning()) {
 						stopAction.setEnabled(false);
 					}
 				}
 			}
 		};
 		runAction.setId(IAlgorithmProcessContext.RUN_ID_STUB+title);
 
 		
 		getViewSite().getActionBars().getToolBarManager().add(runAction);
 		getViewSite().getActionBars().getToolBarManager().add(stopAction);
 		getViewSite().getActionBars().getToolBarManager().add(new Separator());
 	}
 
 	@Override
 	public void setFocus() {
 		if (component!=null) component.setFocus();
 	}
 	
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (runner!=null) runner.dispose();
 	}
 
 	public IAlgorithmProcessContext getContext(){
 		return context;
 	}
 
 }
