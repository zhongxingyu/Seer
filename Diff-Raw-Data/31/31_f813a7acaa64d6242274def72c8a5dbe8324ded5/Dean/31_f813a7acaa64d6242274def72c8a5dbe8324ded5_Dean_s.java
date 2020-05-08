 package kornell.gui.client.personnel;
 
 import kornell.api.client.KornellClient;
 import kornell.gui.client.event.InstitutionEvent;
 import kornell.gui.client.event.InstitutionEventHandler;
 import kornell.gui.client.util.ClientProperties;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class Dean implements InstitutionEventHandler{
 	
 	KornellClient client;
 	
 	public Dean(EventBus bus, KornellClient client) { 
 		this.client = client;
 		bus.addHandler(InstitutionEvent.TYPE, this);
 	}
 
 	@Override
 	public void onEnter(InstitutionEvent event) {
 		GWT.log("Trick or Dean!");
 		try {
 			ClientProperties.setEncoded("institutionAssetsURL", event.getInstitution().getAssetsURL());
 			updateFavicon(event.getInstitution().getAssetsURL() + "favicon.ico");
 		} catch (Exception e) {
			// If it was somehow unable to fetch the favicon, use the default logo
			updateFavicon("skins/first/favicon.ico");
 		}
 	}
 
 	private static native void updateFavicon(String url) /*-{
 		$wnd.updateFavicon(url);
 	}-*/;
 }
