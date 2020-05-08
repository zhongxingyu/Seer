 package com.bibounde.gaemvnrepo.web.admin.statusbar;
 
 import com.bibounde.gaemvnrepo.server.service.util.ConfigurationUtil;
 import com.bibounde.gaemvnrepo.shared.domain.authentication.AuthenticatedUserInfo;
 import com.bibounde.gaemvnrepo.web.ApplicationData;
 import com.bibounde.gaemvnrepo.web.mvc.ActionEvent;
 import com.bibounde.gaemvnrepo.web.mvc.Controller;
 import com.bibounde.gaemvnrepo.web.mvc.Model;
 import com.bibounde.gaemvnrepo.web.mvc.ModelEvent;
 import com.bibounde.gaemvnrepo.web.mvc.View;
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.themes.BaseTheme;
 
 public class StatusBar extends HorizontalLayout implements View {
     
     public static final String ACTION_LOGOUT = StatusBar.class.getName() + ".action.logout";
     
     public static final String ID = StatusBar.class.getName();
     
     private StatusBarModel model;
     private Controller controller;
     
     private Embedded gravatarPicture;
     private Button logout;
     private Label login, caption, version;
 
     
     public StatusBar() {
         this.model = new StatusBarModel();
         this.model.addModelEventListener(this);
         this.setWidth(100, UNITS_PERCENTAGE);
         this.setHeight(45, UNITS_PIXELS);
         this.setSpacing(true);
         //this.setMargin(true);
         this.addStyleName("gaemvnrepo-status-bar");
         
         this.gravatarPicture = new Embedded();
         this.gravatarPicture.setType(Embedded.TYPE_IMAGE);
         this.gravatarPicture.setWidth(20, UNITS_PIXELS);
         this.gravatarPicture.setHeight(20, UNITS_PIXELS);
         
         this.login = new Label();
         this.login.addStyleName("gaemvnrepo-status-bar-label");
         
         this.logout = new Button();
         this.logout.setDescription(ApplicationData.getMessage("StatusBar.logout"));
         this.logout.setStyleName(BaseTheme.BUTTON_LINK);
         this.logout.setIcon(new ExternalResource("/static/icons/logout-16.png"));
         
         this.caption = new Label(ConfigurationUtil.INSTANCE.getCaption());
         this.caption.setStyleName("gaemvnrepo-status-bar-caption");
         
        String versionKey = "StatusBar." + ConfigurationUtil.INSTANCE.getApplicationName();
        String versionText = ConfigurationUtil.INSTANCE.getApplicationName();
        if (ApplicationData.messageExists(versionKey)) {
            versionText = ApplicationData.getMessage(versionKey); 
        }
        
        this.version = new Label(versionText);
         this.version.setStyleName("gaemvnrepo-version");
         this.version.setDescription(ConfigurationUtil.INSTANCE.getApplicationVersion() + " - " + ConfigurationUtil.INSTANCE.getApplicationBuild());
         
         this.initLayout();
         this.initListeners();
     }
     
     private void initLayout() {
         HorizontalLayout captionLayout = new HorizontalLayout();
         captionLayout.setSpacing(true);
         captionLayout.setMargin(new MarginInfo(false, true, false, true));
         this.addComponent(captionLayout);
         this.setComponentAlignment(captionLayout, Alignment.MIDDLE_LEFT);
         
         captionLayout.addComponent(this.caption);
         captionLayout.setComponentAlignment(this.caption, Alignment.MIDDLE_LEFT);
         captionLayout.addComponent(this.version);
         captionLayout.setComponentAlignment(this.version, Alignment.MIDDLE_LEFT);
         
         HorizontalLayout loginLayout = new HorizontalLayout();
         loginLayout.setSpacing(true);
         loginLayout.setMargin(new MarginInfo(false, true, false, true));
         this.addComponent(loginLayout);
         this.setComponentAlignment(loginLayout, Alignment.MIDDLE_RIGHT);
         
         
         loginLayout.addComponent(this.gravatarPicture);
         loginLayout.addComponent(this.login);
         loginLayout.addComponent(logout);
         
     }
     
     private void initListeners() {
         this.logout.addListener(new ClickListener() {
             
             @Override
             public void buttonClick(ClickEvent event) {
                 ActionEvent actionEvent = new ActionEvent(ID, ACTION_LOGOUT);
                 StatusBar.this.controller.actionPerformed(actionEvent);
             }
         });
     }
 
     @Override
     public void modelChanged(ModelEvent event) {
         if (StatusBarModel.GRAVATAR_URL_CHANGED_EVENT.equals(event.getType())) {
             this.gravatarPicture.setSource(new ExternalResource(this.model.getGravaterUrl()));
         } else if (StatusBarModel.LOGIN_CHANGED_EVENT.equals(event.getType())) {
             this.login.setValue(this.model.getLogin());
         }
     }
 
     @Override
     public Model getModel() {
         return this.model;
     }
 
     @Override
     public void setController(Controller controller) {
         this.controller = controller;
     }
 
     @Override
     public String getId() {
         return ID;
     }
     
 }
