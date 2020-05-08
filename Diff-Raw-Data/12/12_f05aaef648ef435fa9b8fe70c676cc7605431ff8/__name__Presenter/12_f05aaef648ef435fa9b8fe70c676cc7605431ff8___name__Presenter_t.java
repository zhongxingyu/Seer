 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package};
 
 import com.google.gwt.event.shared.GwtEvent.Type;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.ContentSlot;
 import com.gwtplatform.mvp.client.annotations.ProxyStandard;
 import com.gwtplatform.mvp.client.proxy.Proxy;
 import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
 import com.gwtplatform.mvp.client.HasUiHandlers;
 
 // TODO optional on uihandlers
 public class ${name}Presenter extends Presenter<${name}Presenter.MyView, ${name}Presenter.MyProxy> 
 	implements ${name}UiHandlers {
    interface MyView extends View, HasUiHandlers<${name}UiHandlers> {
     }
 
     @ContentSlot
     public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();
 
     @ProxyStandard
     public interface MyProxy extends Proxy<${name}Presenter> {
     }
 
     @Inject
     public ApplicationPresenter(EventBus eventBus, 
     							MyView view, 
     							MyProxy proxy) {
         super(eventBus, view, proxy, RevealType.Root);
         
         getView().setUiHandlers(this);
     }
 }
