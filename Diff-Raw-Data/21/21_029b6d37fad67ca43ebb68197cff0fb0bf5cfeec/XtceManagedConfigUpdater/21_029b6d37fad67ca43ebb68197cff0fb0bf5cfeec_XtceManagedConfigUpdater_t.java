 package org.hbird.osgi.xtceosgi;
 
 import java.util.Dictionary;
 import java.util.List;
 
 import org.hbird.core.spacesystemmodel.interfaces.SpaceSystemModelUpdateListener;
 import org.hbird.core.xtce.XtceSpaceSystemModelFactory;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
 
public class XtceManagedConfigUpdater implements ManagedService {
 
 	private XtceSpaceSystemModelFactory factory;
 
 	private List<SpaceSystemModelUpdateListener> modelUpdateListeners;
 
 	private String spaceSystemModelFilename;
 
 	private final Object lock = new Object();
 
 	public void setFactory(final XtceSpaceSystemModelFactory factory) {
 		this.factory = factory;
 	}
 
 	public void setModelUpdateListeners(final List<SpaceSystemModelUpdateListener> modelUpdateListeners) {
 		this.modelUpdateListeners = modelUpdateListeners;
 	}
 
 	public void setSpaceSystemModelFilename(final String spaceSystemModelFilename) {
		System.out.println("XtceManagedConfigUpdater.setSpaceSystemModelFilename called.");
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
 
	@Override
	public void updated(Dictionary props) throws ConfigurationException {
		System.out.println("XtceManagedConfigUpdater.updated called.");
		//final String fileName = (String) props.get("spaceSystemModelFilename");
	}

 }
