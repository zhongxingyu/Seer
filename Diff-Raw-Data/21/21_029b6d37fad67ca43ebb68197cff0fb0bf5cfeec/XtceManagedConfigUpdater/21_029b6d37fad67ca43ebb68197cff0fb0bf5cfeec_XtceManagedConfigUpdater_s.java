 package org.hbird.osgi.xtceosgi;
 
 import java.util.Dictionary;
 import java.util.List;
 
 import org.hbird.core.spacesystemmodel.interfaces.SpaceSystemModelUpdateListener;
 import org.hbird.core.xtce.XtceSpaceSystemModelFactory;
 
public class XtceManagedConfigUpdater {
 
 	private XtceSpaceSystemModelFactory factory;
 
 	private List<SpaceSystemModelUpdateListener> modelUpdateListeners;
 
 	private String spaceSystemModelFilename;
 
 	private final Object lock = new Object();
 
	public void update(final Dictionary props) {
		System.out.println("Config updated");
		final String fileName = (String) props.get("spaceSystemModelFilename");
	}

 	public void setFactory(final XtceSpaceSystemModelFactory factory) {
 		this.factory = factory;
 	}
 
 	public void setModelUpdateListeners(final List<SpaceSystemModelUpdateListener> modelUpdateListeners) {
 		this.modelUpdateListeners = modelUpdateListeners;
 	}
 
 	public void setSpaceSystemModelFilename(final String spaceSystemModelFilename) {
		System.out.println("GAYYYYBEN");
 		factory.setSpaceSystemModelFilename(spaceSystemModelFilename);
 
 		synchronized (lock) {
 			for (final SpaceSystemModelUpdateListener listener : modelUpdateListeners) {
 				listener.modelChanged();
 			}
 		}
 	}
 
 	public String getSpaceSystemModelFilename() {
 		return spaceSystemModelFilename;
 	}
 
 }
