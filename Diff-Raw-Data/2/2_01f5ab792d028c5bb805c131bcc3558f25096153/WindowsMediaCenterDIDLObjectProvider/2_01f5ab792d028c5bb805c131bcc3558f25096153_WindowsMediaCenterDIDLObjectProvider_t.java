 package org.dittovideo.objectprovider.wmc;
 
 import java.util.List;
 
 import org.dittovideo.core.server.AbstractDIDLObjectProvider;
 import org.fourthline.cling.support.model.DIDLObject;
 import org.fourthline.cling.support.model.container.Container;
 
 import com.cfs.progress.ProgressableObjectMonitor;
 import com.mediaserver.wmc.WindowsMediaCenterManager;
 
 public class WindowsMediaCenterDIDLObjectProvider extends AbstractDIDLObjectProvider {
 	private WindowsMediaCenterPlaylistUPNPObject root = new WindowsMediaCenterPlaylistUPNPObject(this);
 	private WindowsMediaCenterManager<DIDLObject> manager = new WindowsMediaCenterManager<DIDLObject>(root);
 	
 	@Override
	public List<DIDLObject> buildDIDLObjects(ProgressableObjectMonitor monitor) {
 		manager.refreshCache(monitor);
 		return root.getChildren();
 	}
 	
 	@Override
 	public void refreshContainer(Container container) {
 		// TODO not implemented yet
 	}
 }
