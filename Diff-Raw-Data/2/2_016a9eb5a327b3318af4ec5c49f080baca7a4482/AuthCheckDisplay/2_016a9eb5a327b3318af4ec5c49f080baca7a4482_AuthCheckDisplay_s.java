 package ru.alepar.tdt.gwt.client.view;
 
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Panel;
 import ru.alepar.tdt.gwt.client.action.auth.AuthAction;
 import ru.alepar.tdt.gwt.client.presenter.AuthCheck;
 
 /**
  * User: alepar
  * Date: Jul 22, 2010
  * Time: 6:49:29 AM
  */
 public abstract class AuthCheckDisplay extends Composite implements AuthCheck.Display {
 
     private final Panel panel;
     private final HTML html;
 
     public AuthCheckDisplay(Panel panel) {
         this.panel = panel;
         
         this.html = new HTML(); 
         initWidget(this.html);
         panel.add(this);
     }
 
     protected abstract void appEntryPoint();
 
     @Override
     public void onSuccessAuth(AuthAction.AuthResponse authResponse) {
        html.setHTML("<a href=\"" + authResponse.getLogOutUrl() + "\">" + authResponse.getUserAccount().getLogin() + "</a>");
         appEntryPoint();
     }
 
     @Override
     public void onNotLoggedIn(AuthAction.AuthResponse authResponse) {
         Window.Location.replace(authResponse.getLogInUrl());
     }
 
     @Override
     public void onFailure(Throwable throwable) {
         Window.alert("failed to auth\n" + throwable.toString());
     }
 }
