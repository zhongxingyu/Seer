 package gwtmvp.client.mvp;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.shared.GwtEvent.Type;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.ContentSlot;
 import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.Place;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
 import com.gwtplatform.mvp.client.proxy.Proxy;
 import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
 import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;
 
 public class LayoutPresenter extends
 Presenter<LayoutPresenter.MyView, LayoutPresenter.MyProxy> {
 
 	public interface MyView extends View {
 	}
 
 	@ProxyStandard
	public interface MyProxy extends Proxy<LayoutPresenter>, Place {
 	}
 
 	@ContentSlot
 	public static final Type<RevealContentHandler<?>> TYPE_SetContent = new Type<RevealContentHandler<?>>();
 
 	private final DispatchAsync dispatcher;
 
 	private final PlaceManager placeManager;
 
 	@Inject
 	public LayoutPresenter(final EventBus eventBus,
 			final MyView view,
 			final MyProxy proxy,
 			final DispatchAsync dispatcher,
 			final PlaceManager placeManager) {
 		super(eventBus, view, proxy);
 		Log.info("contruction");
 		this.dispatcher = dispatcher;
 		this.placeManager = placeManager;
 	}
 
 	@Override
 	protected void onBind() {
 		super.onBind();
 		Log.info("onBind()");
 	}
 
 	@Override
 	protected void onReveal() {
 		super.onReveal();
		placeManager.revealCurrentPlace();
 	}
 
 	@Override
 	protected void revealInParent() {
 		RevealRootContentEvent.fire(this, this);
 	}
 }
