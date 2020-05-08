 package org.amanzi.neo.services.ui;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.events.ShowPreparedViewEvent;
 import org.amanzi.neo.services.events.ShowViewEvent;
 import org.amanzi.neo.services.events.UpdateDrillDownEvent;
 import org.amanzi.neo.services.events.UpdateViewEvent;
 import org.amanzi.neo.services.events.UpdateViewEventType;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.neo4j.graphdb.Node;
 import org.neo4j.neoclipse.view.NeoGraphViewPart;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class NeoServicesUiPlugin extends AbstractUIPlugin  implements IUpdateViewListener {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.amanzi.neo.services.ui"; //$NON-NLS-1$
 
 	// The shared instance
 	private static NeoServicesUiPlugin plugin;
 	private  Object mon=new Object();
 	
     private UpdateViewManager updateBDManager;
     private final Object neoDataMonitor = new Object();
 
     private UpdateViewEvent lastExetutedEvent;
     private final List<UpdateViewEventType> eventList = Arrays.asList(UpdateViewEventType.values());
 	
 	/**
 	 * The constructor
 	 */
 	public NeoServicesUiPlugin() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
         updateBDManager = new UpdateViewManager();
         updateBDManager.addListener(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
     /**
      * @return UpdateBDManager
      */
     public UpdateViewManager getUpdateViewManager() {
         return updateBDManager;
     }	
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static NeoServicesUiPlugin getDefault() {
 		return plugin;
 	}
 	
     @Override
     public void updateView(UpdateViewEvent event) {
         UpdateViewEventType type = event.getType();
         if (!eventExecuted(event)) {
             // update NeoGraphViewPart
             switch (type) {
             case DRILL_DOWN:
                 UpdateDrillDownEvent ddEvent = (UpdateDrillDownEvent)event;
                 if (!ddEvent.getSource().equals(NeoGraphViewPart.ID)) {
                     Node node = ddEvent.getNodes().get(0);
                     org.neo4j.neoclipse.Activator.getDefault().updateNeoGraphView(node);
                 }
                 break;
             case SHOW_PREPARED_VIEW:
                 ShowPreparedViewEvent spvEvent = (ShowPreparedViewEvent)event;
                 if (spvEvent.isViewNeedUpdate(NeoGraphViewPart.ID)) {
                     Node node = spvEvent.getNodes().get(0);
                     org.neo4j.neoclipse.Activator.getDefault().showNeoGraphView(node);
                 }
                 break;
             case SHOW_VIEW:
                 ShowViewEvent svEvent = (ShowViewEvent)event;
                 if (svEvent.isViewNeedUpdate(NeoGraphViewPart.ID)) {
                     org.neo4j.neoclipse.Activator.getDefault().showNeoGraphView(null);
                 }
                 break;
             default:
                 org.neo4j.neoclipse.Activator.getDefault().updateNeoGraphView();
             }
             lastExetutedEvent = event;
         }
     }
 
 
     private boolean eventExecuted(UpdateViewEvent event) {
         if (lastExetutedEvent == null) {
             return false;
         }
         if (lastExetutedEvent.equals(event)) {
             return true;
         }
         if(!lastExetutedEvent.getClass().equals(event.getClass())){
             return false;
         }
         Node last = getNodeFromEvent(lastExetutedEvent);
         if (last != null) {
             Node current = getNodeFromEvent(event);
             if (current == null) {
                 return false;
             }
             return last.equals(current);
         }
         return false;
     }
 
     private Node getNodeFromEvent(UpdateViewEvent event) {
         if (event instanceof UpdateDrillDownEvent) {
             UpdateDrillDownEvent ddEvent = (UpdateDrillDownEvent)event;
             if (!ddEvent.getSource().equals(NeoGraphViewPart.ID)) {
                 return ddEvent.getNodes().get(0);
             }
         }
         if (event instanceof ShowPreparedViewEvent) {
             ShowPreparedViewEvent spvEvent = (ShowPreparedViewEvent)event;
             if (spvEvent.isViewNeedUpdate(NeoGraphViewPart.ID)) {
                 return spvEvent.getNodes().get(0);
             }
         }
         return null;
     }
 
     @Override
     public Collection<UpdateViewEventType> getType() {
         return eventList;
     }
 

     /**
     * Gets the image for type.
      *
     * @param type the type
     * @return the image for type
      */
     public Image getImageForType(NodeTypes type) {
        return    IconManager.getIconManager().getImage(type.getId());
     }
 
 }
