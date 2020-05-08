 package gwtmvp.client.mvp;
 
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.TokenFormatter;
 
 public class MyPlaceManager extends PlaceManagerImpl {
 
 	private final PlaceRequest defaultPlaceRequest;

 	@Inject
 	public MyPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter) {
 		super(eventBus, tokenFormatter);
		defaultPlaceRequest = new PlaceRequest(HomePresenter.nameToken);
 	}
 
 	@Override
 	public void revealDefaultPlace() {
		revealPlace(defaultPlaceRequest);
	}

	@Override
	public void revealErrorPlace(String invalidHistoryToken) {
		GWT.log("error:"+invalidHistoryToken);
		Log.debug("error:"+invalidHistoryToken);
 	}
 
 }
