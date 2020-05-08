 package org.iplantc.core.uiapps.integration.client.presenter;
 
 import org.iplantc.core.uiapps.integration.client.view.AppsIntegrationView;
 import org.iplantc.core.uiapps.widgets.client.events.ArgumentSelectedEvent;
 import org.iplantc.core.uiapps.widgets.client.events.ArgumentSelectedEvent.ArgumentSelectedEventHandler;
 import org.iplantc.core.uiapps.widgets.client.models.AppTemplate;
 import org.iplantc.core.uiapps.widgets.client.models.AppTemplateAutoBeanFactory;
 import org.iplantc.core.uiapps.widgets.client.presenter.AppWizardPresenterJsonAdapter;
import org.iplantc.core.uiapps.widgets.client.view.editors.ArgumentEditor;
import org.iplantc.core.uiapps.widgets.client.view.editors.ArgumentPropertyEditor;
 import org.iplantc.core.uicommons.client.events.EventBus;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.user.client.ui.HasOneWidget;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.Splittable;
 
 /**
  * @author jstroot
  *
  */
 public class AppsIntegrationPresenterImpl implements AppsIntegrationView.Presenter {
 
     private final AppsIntegrationView view;
     private AppTemplate appTemplate;
 
     public AppsIntegrationPresenterImpl(final AppsIntegrationView view, final EventBus eventBus) {
         this.view = view;
         view.setPresenter(this);
         eventBus.addHandler(ArgumentSelectedEvent.TYPE, new ArgumentSelectedEventHandler() {
 
             @Override
             public void onArgumentSelected(ArgumentSelectedEvent event) {
                if ((event.getSource() instanceof ArgumentEditor) 
                        && (((ArgumentEditor)event.getSource()).getArgumentPropertyEditor() != null)) {
                    ArgumentPropertyEditor argumentPropertyEditor = ((ArgumentEditor)event.getSource()).getArgumentPropertyEditor();
                     view.setEastWidget(argumentPropertyEditor);
                 }
             }
         });
     }
 
     @Override
     public void goLegacy(HasOneWidget container, Splittable legacyJson) {
         setAppTemplateFromLegacyJson(legacyJson);
         go(container);
     }
 
     @Override
     public void go(HasOneWidget container, AppTemplate appTemplate) {
         this.appTemplate = appTemplate;
         go(container);
     }
 
     @Override
     public void go(HasOneWidget container) {
         view.edit(appTemplate);
         container.setWidget(view);
     }
 
     @Override
     public void setAppTemplateFromLegacyJson(Splittable legacyJson) {
         Splittable appTemplateSplit = AppWizardPresenterJsonAdapter.adaptAppTemplateJsonString(legacyJson);
 
         AppTemplateAutoBeanFactory factory = GWT.create(AppTemplateAutoBeanFactory.class);
         AutoBean<AppTemplate> appTemplateAb = AutoBeanCodex.decode(factory, AppTemplate.class, appTemplateSplit);
 
         this.appTemplate = appTemplateAb.as();
     }
 
     @Override
     public AppTemplate getAppTemplate() {
         return appTemplate;
     }
 
 }
