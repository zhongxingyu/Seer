 package gwtmvp.client.mvp;
 
 import gwtmvp.shared.event.GreetingSentEvent;
 import gwtmvp.shared.event.GreetingSentEventHandler;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HasHTML;
 import com.google.gwt.user.client.ui.HasText;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
 import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;
 
 public class GreetingResponsePresenter extends
 Presenter<GreetingResponsePresenter.MyView, GreetingResponsePresenter.MyProxy> {
 
 	public interface MyView extends View {
 		HasText getTextToServer();
 
 		HasHTML getServerResponse();
 
 		HasClickHandlers getClose();
 
 		DialogBox getDialogBox();
 	}
 
	@ProxyStandard
 	@NameToken(nameToken)
	public interface MyProxy extends ProxyPlace<GreetingResponsePresenter> {
 	}
 
 	public static final String nameToken = "GreetingResponse";
 
 	public final PlaceManager placeManager;
 
 	@Inject
 	public GreetingResponsePresenter(final EventBus eventBus,
 			final MyView view,
 			final MyProxy proxy,
 			final PlaceManager placeManager) {
 		super(eventBus, view, proxy);
 		this.placeManager = placeManager;
 		bind();
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	protected void onBind() {
 		super.onBind();
 		// Add a handler to close the DialogBox
 		registerHandler(getView().getClose().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(final ClickEvent event) {
 				getView().getDialogBox().hide();
 
 				// Not sure of a nice place to put these!
 				//    sendButton.setEnabled(true);
 				//    sendButton.setFocus(true);
 			}
 		}));
 
 		getEventBus().addHandler(GreetingSentEvent.TYPE, new GreetingSentEventHandler() {
 
 			@Override
 			public void onGreetingSent(final GreetingSentEvent event) {
 				Log.info("Handling GreetingSent event");
 
 				getView().getTextToServer().setText(event.getName());
 				getView().getServerResponse().setHTML(event.getMessage());
 				getView().getDialogBox().show();
 				getView().getDialogBox().center();
 			}
 		});
 	}
 
 	@Override
 	protected void revealInParent() {
 		RevealRootContentEvent.fire(this, this);
 	}
 }
