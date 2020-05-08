 package org.kie.jbpm.designer.client;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.Dependent;
 import javax.inject.Inject;
 
 import com.google.gwt.user.client.ui.IsWidget;
 import org.jboss.errai.bus.client.api.RemoteCallback;
 import org.jboss.errai.ioc.client.api.Caller;
 import org.kie.jbpm.designer.service.JSONService;
 import org.uberfire.backend.vfs.Path;
 import org.uberfire.client.annotations.OnStart;
 import org.uberfire.client.annotations.WorkbenchEditor;
 import org.uberfire.client.annotations.WorkbenchPartTitle;
 import org.uberfire.client.annotations.WorkbenchPartView;
 import org.uberfire.client.mvp.UberView;
 
 @Dependent
@WorkbenchEditor(identifier = "jbpm.designer", fileTypes = "bpmn2")
 public class DesignerPresenter {
 
     public interface View
             extends
             UberView<DesignerPresenter> {
 
         void setJsonModel( final String jsonModel );
 
     }
 
     @Inject
     private View view;
 
     @Inject
     private Bootstrap bootstrap;
 
     @Inject
     private Caller<JSONService> jsonService;
 
     private Path path;
 
     @PostConstruct
     private void bootstrapOryxScripts() {
         bootstrap.init();
     }
 
     @OnStart
     public void onStart( final Path path ) {
         this.path = path;
 
         jsonService.call( new RemoteCallback<String>() {
             @Override
             public void callback( final String json ) {
                 if ( json != null ) {
                     view.setJsonModel( json );
                 }
             }
         } ).getJsonModel( path );
     }
 
     @WorkbenchPartTitle
     public String getName() {
         return "jBPM Designer";
     }
 
     @WorkbenchPartView
     public IsWidget getView() {
         return view;
     }
 }
