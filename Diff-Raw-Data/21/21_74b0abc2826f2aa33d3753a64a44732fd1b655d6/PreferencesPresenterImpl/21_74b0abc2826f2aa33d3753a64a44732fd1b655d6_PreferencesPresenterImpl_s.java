 package org.iplantc.de.client.preferences.presenter;
 
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.models.UserSettings;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.preferences.views.PreferencesView;
 import org.iplantc.de.client.preferences.views.PreferencesView.Presenter;
 import org.iplantc.de.client.services.UserSessionServiceFacade;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasOneWidget;
 
 /**
  * 
  * A presenter impl for user preferences
  * 
  * @author sriram
  * 
  */
 public class PreferencesPresenterImpl implements Presenter {
 
     PreferencesView view;
 
     public PreferencesPresenterImpl(PreferencesView view) {
         this.view = view;
         this.view.setPresenter(this);
         this.view.setValues();
     }
 
     @Override
     public void go(HasOneWidget container) {
         container.setWidget(view.asWidget());
     }
 
     @Override
     public boolean validateAndSave() {
         if (view.isValid()) {
             UserSettings us = view.getValues();
             UserSessionServiceFacade facade = new UserSessionServiceFacade();
             facade.saveUserPreferences(us.toJson(), new AsyncCallback<String>() {
 
                 @Override
                 public void onSuccess(String result) {
                    IplantAnnouncer.getInstance().schedule(I18N.DISPLAY.saveSettings());
                 }
 
                 @Override
                 public void onFailure(Throwable caught) {
                     ErrorHandler.post(caught);
                 }
             });
         }
 
         return view.isValid();
     }
 
     @Override
     public void setDefaults() {
         view.setDefaultValues();
     }
 
 }
