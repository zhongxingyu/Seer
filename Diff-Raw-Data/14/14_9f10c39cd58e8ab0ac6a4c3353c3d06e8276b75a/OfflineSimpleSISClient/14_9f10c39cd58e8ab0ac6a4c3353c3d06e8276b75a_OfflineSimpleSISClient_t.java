 package org.iucn.sis.client.container;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.OfflineCache;
import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.utils.HasCache;
 
 import com.google.gwt.user.client.Window;
import com.solertium.util.extjs.client.WindowUtils;
 
 public class OfflineSimpleSISClient extends SimpleSISClient {
 	
 	@Override
 	public void loadModule() {
 		SISClientBase.iAmOnline = false;
 		
 		Window.setTitle("Offline - " + Window.getTitle());
 		
 		super.loadModule();
 	}
 	
 	@Override
 	protected Collection<HasCache> getCachesToInitialize() {		
 		List<HasCache> list = new ArrayList<HasCache>(super.getCachesToInitialize());
 		list.add(OfflineCache.impl);		
 		return list;
 	}
 	
	@Override
	public void buildPostLogin() {
		if (OfflineCache.impl.get() == null)
			WindowUtils.errorAlert("Could not load working set metadata, please check your installation parameters.");
		else if (WorkingSetCache.impl.getOfflineWorkingSet() == null)
			WindowUtils.errorAlert("Sorry, you do not have permission to edit the offline working set. Ask the working set owner to share it with you.");
		else
			super.buildPostLogin();
	}
	
 }
