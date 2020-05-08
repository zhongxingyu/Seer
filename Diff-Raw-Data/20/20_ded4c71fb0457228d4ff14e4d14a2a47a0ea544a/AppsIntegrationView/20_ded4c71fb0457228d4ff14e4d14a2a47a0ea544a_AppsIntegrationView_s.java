 package org.iplantc.core.uiapps.integration.client.view;
 
 import java.util.List;
 
 import org.iplantc.core.uiapps.widgets.client.events.AppTemplateSelectedEvent.AppTemplateSelectedEventHandler;
 import org.iplantc.core.uiapps.widgets.client.events.AppTemplateUpdatedEvent.AppTemplateUpdatedEventHandler;
 import org.iplantc.core.uiapps.widgets.client.events.ArgumentGroupSelectedEvent.ArgumentGroupSelectedEventHandler;
 import org.iplantc.core.uiapps.widgets.client.events.ArgumentSelectedEvent.ArgumentSelectedEventHandler;
 import org.iplantc.core.uiapps.widgets.client.models.AppTemplate;
 import org.iplantc.core.uiapps.widgets.client.models.Argument;
 
 import com.google.gwt.editor.client.EditorError;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.sencha.gxt.widget.core.client.event.BeforeHideEvent.BeforeHideHandler;
 
 /**
  * @author jstroot
  *
  */
 public interface AppsIntegrationView extends IsWidget {
     
     public interface Presenter extends org.iplantc.core.uiapps.widgets.client.view.AppWizardView.BasePresenter, AppIntegrationToolbar.Presenter, AppTemplateUpdatedEventHandler, BeforeHideHandler {
 
         /**
          * Checks if the given argument should be ordered in order to be used by an App at launch.
          * 
          * @param arg
          * @return true if the property can be used at analysis execution but needs an order.
          */
         boolean orderingRequired(Argument arg);
 
         void onAppTemplateChanged();
 
         void setBeforeHideHandlerRegistration(HandlerRegistration hr);
 
         boolean isOnlyLabelEditMode();
 
         void setOnlyLabelEditMode(boolean onlyLabelEditMode);
     }
 
     void setPresenter(Presenter presenter);
 
     void setEastWidget(IsWidget widget);
 
     void edit(AppTemplate appTemplate);
 
     AppTemplate flush();
 
     AppIntegrationToolbar getToolbar();
 
     void onAppTemplateChanged();
 
     void setCmdLinePreview(String cmdLinePreview);
 
     void addAppTemplateSelectedEventHandler(AppTemplateSelectedEventHandler handler);
 
     void addAppTemplateUpdatedEventHandler(AppTemplateUpdatedEventHandler handler);
 
     void addArgumentSelectedEventHandler(ArgumentSelectedEventHandler handler);
 
     void addArgumentGroupSelectedEventHandler(ArgumentGroupSelectedEventHandler handler);
 
     /**
      * Used to update the AppTemplate with a new ID, usually after a new app has been successfully saved.
      * 
      * @param id
      */
     void updateAppTemplateId(String id);
 
     void setOnlyLabelEditMode(boolean onlyLabelEditMode);
 
     boolean hasErrors();
 
     List<EditorError> getErrors();
 
 }
