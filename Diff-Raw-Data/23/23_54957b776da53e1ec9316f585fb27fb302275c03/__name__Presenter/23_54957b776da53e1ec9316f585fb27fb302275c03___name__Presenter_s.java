package com.arcbees.ide.plugin.template.presenter{package};
 
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
 
 #if( $uihandlers )
 	#set( $implements = "implements ${name}UiHandlers" )
 	#set( $extendsView = ", HasUiHandlers<${name}UiHandlers>" )
 #else 
 	#set( $implements = "" )
 	#set( $extendsView = "" )
 #end
 
 public class ${name}Presenter extends Presenter<${name}Presenter.MyView, ${name}Presenter.MyProxy> $implements {
     interface MyView extends View $extendsView {
     }
 
     @ContentSlot
     public static final Type<RevealContentHandler<?>> SLOT_${name} = new Type<RevealContentHandler<?>>();
 
     @ProxyStandard
     public interface MyProxy extends Proxy<${name}Presenter> {
     }
 
     @Inject
     public ApplicationPresenter(
     		EventBus eventBus, 
     		MyView view, 
     		MyProxy proxy) {
         super(eventBus, view, proxy, RevealType.Root);
         
         #if( $uihandlers ) 
         getView().setUiHandlers(this);
         #end
     }
 }
