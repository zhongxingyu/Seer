 package gwtmvp.client.mvp;
 
 import gwtmvp.shared.event.GreetingSentEvent;
 import gwtmvp.shared.rpc.SendGreeting;
 import gwtmvp.shared.rpc.SendGreetingResult;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.Place;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
 import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;
 
 public class GreetingPresenter extends Presenter<GreetingPresenter.MyView,
 GreetingPresenter.MyProxy> {
 	/**
 	 * The message displayed to the user when the server cannot be reached or
 	 * returns an error.
 	 */
 	private static final String SERVER_ERROR = "An error occurred while "
 			+ "attempting to contact the server. Please check your network "
 			+ "connection and try again.";
 
 	public interface MyView extends View {
 		public HasValue<String> getName();
 		public HasClickHandlers getSend();
 	}
 
 	@ProxyStandard
 	@NameToken(nameToken)
	public interface MyProxy extends Proxy<GreetingPresenter>, Place {
 	}
 
 	public static final String nameToken = "Greeting";
 
 	private final DispatchAsync dispatcher;
 
 	private final PlaceManager placeManager;
 
 	@Inject
 	public GreetingPresenter(final EventBus eventBus,
 			final MyView view,
 			final MyProxy proxy,
 			final DispatchAsync dispatcher,
 			final PlaceManager placeManager) {
 		super(eventBus, view, proxy);
 		this.dispatcher = dispatcher;
 		this.placeManager = placeManager;
 	}
 
 	/**
 	 * Try to send the greeting message
 	 */
 	private void doSend() {
 		Log.info("Calling doSend");
 
 		dispatcher.execute(new SendGreeting(getView().getName().getValue()), new AsyncCallback<SendGreetingResult>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Handle Failure:", caught);
 				Window.alert(SERVER_ERROR);
 			}
 
 			@Override
 			public void onSuccess(SendGreetingResult result) {
 				// take the result from the server and notify client interested components
 				getEventBus().fireEvent(new GreetingSentEvent(result.getName(), result.getMessage()));
 			}
 
 		});
 	}
 
 	@Override
 	protected void onBind() {
 		super.onBind();
 		registerHandler(getView().getSend().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(final ClickEvent event) {
 				doSend();
 			}
 		}));
 	}
 
 	@Override
 	protected void revealInParent() {
 		RevealRootContentEvent.fire(this, this);
 	}
 }
