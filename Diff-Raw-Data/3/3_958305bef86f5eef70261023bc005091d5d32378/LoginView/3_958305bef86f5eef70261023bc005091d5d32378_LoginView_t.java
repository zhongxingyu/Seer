 package com.jtbdevelopment.e_eye_o.ria.vaadin.views;
 
 import com.jtbdevelopment.e_eye_o.DAO.ReadOnlyDAO;
 import com.jtbdevelopment.e_eye_o.entities.AppUser;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.Logo;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.views.passwordreset.ResetRequest;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.views.registration.LegalView;
 import com.vaadin.event.ShortcutAction;
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener;
 import com.vaadin.server.*;
 import com.vaadin.ui.*;
 import com.vaadin.ui.themes.Runo;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.ConfigurableBeanFactory;
 import org.springframework.context.annotation.Scope;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequestWrapper;
 
 /**
  * Date: 2/24/13
  * Time: 8:24 PM
  * <p/>
  * Largely based on Spring Security Example by Nicolas Frankel at
  * https://github.com/nfrankel/More-Vaadin/tree/master/springsecurity-integration
  */
 
 @Component
 @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
 public class LoginView extends VerticalLayout implements View {
 
     public static final String VIEW_NAME = "Login";
 
     private final TextField loginField = new TextField("Email Address");
     private final PasswordField passwordField = new PasswordField("Password");
 
     @Autowired
     private AuthenticationManager authenticationManager;
 
     @Autowired
     private ReadOnlyDAO readOnlyDAO;
 
     @Autowired
     private PersistentTokenBasedRememberMeServices rememberMeServices;
 
     @Autowired
     private Logo logo;
 
     @PostConstruct
     public void PostConstruct() {
         setSpacing(true);
         setMargin(true);
         setSizeFull();
 
         Layout titleSection = getTitleSection();
         addComponent(titleSection);
         setComponentAlignment(titleSection, Alignment.MIDDLE_CENTER);
 
         Layout loginSection = getLoginSection();
         addComponent(loginSection);
         setComponentAlignment(loginSection, Alignment.BOTTOM_CENTER);
 
         Layout helpSection = getSignUpResetSection();
         addComponent(helpSection);
         setComponentAlignment(helpSection, Alignment.TOP_CENTER);
     }
 
     private Layout getLoginSection() {
         VerticalLayout loginSection = new VerticalLayout();
 
         FormLayout form = new FormLayout();
         form.setWidth(null);
         form.setHeight(null);
         loginSection.addComponent(form);
         loginSection.setComponentAlignment(form, Alignment.MIDDLE_CENTER);
 
         form.addComponent(loginField);
         form.setComponentAlignment(loginField, Alignment.TOP_CENTER);
         form.addComponent(passwordField);
         form.setComponentAlignment(passwordField, Alignment.TOP_CENTER);
 
         final CheckBox rememberMe = new CheckBox("Remember Me");
         rememberMe.setValue(Boolean.FALSE);
         form.addComponent(rememberMe);
         form.setComponentAlignment(rememberMe, Alignment.TOP_CENTER);
 
         Button loginButton = new Button("Login");
         loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
         loginButton.addStyleName(Runo.BUTTON_DEFAULT);
         form.addComponent(loginButton);
         form.setComponentAlignment(loginButton, Alignment.TOP_CENTER);
         loginButton.addStyleName("primary");
         loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
         loginButton.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent clickEvent) {
                 Authentication authentication;
                 final String login = loginField.getValue();
                 final String password = passwordField.getValue();
                 try {
                     authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));
                     if (rememberMe.getValue()) {
                         final VaadinRequest currentRequest = VaadinService.getCurrentRequest();
                         final VaadinResponse currentResponse = VaadinService.getCurrentResponse();
                         final String key = rememberMeServices.getParameter();
                         rememberMeServices.loginSuccess(new FakeRememberMeFlag(key, (VaadinServletRequest) currentRequest), (VaadinServletResponse) currentResponse, authentication);
                     }
                 } catch (AuthenticationException e) {
                    Notification.show("Failed to login.  Username or password does not match an active account.", Notification.Type.ERROR_MESSAGE);
                     return;
                 }
                 AppUser user = readOnlyDAO.getUser(login);
                 if (user == null) {
                     Notification.show("This is embarrassing", Notification.Type.ERROR_MESSAGE);
                     return;
                 }
                 SecurityContextHolder.getContext().setAuthentication(authentication);
                 getParent().getUI().getSession().close();
                 getParent().getUI().getPage().setLocation("/E-EYE-O");
                 loginField.setValue("");
                 passwordField.setValue("");
             }
         });
         return loginSection;
     }
 
     private Layout getSignUpResetSection() {
         VerticalLayout helpSection = new VerticalLayout();
         HorizontalLayout horizontalLayout = new HorizontalLayout();
         horizontalLayout.setSpacing(true);
         Link registerLink = new Link("Register for Account", new ExternalResource("#!" + LegalView.VIEW_NAME));
         horizontalLayout.addComponent(registerLink);
         Link forgotPasswordLink = new Link("Forgot Password?", new ExternalResource("#!" + ResetRequest.VIEW_NAME));
         horizontalLayout.addComponent(forgotPasswordLink);
         helpSection.addComponent(horizontalLayout);
         helpSection.setComponentAlignment(horizontalLayout, Alignment.MIDDLE_CENTER);
         return helpSection;
     }
 
     private Layout getTitleSection() {
         VerticalLayout titleSection = new VerticalLayout();
         Label title = new Label("Welcome to");
         title.setSizeUndefined();
         title.addStyleName("bold");
         titleSection.addComponent(title);
         titleSection.setComponentAlignment(title, Alignment.MIDDLE_CENTER);
         logo.addStyleName("big-logo");
         titleSection.addComponent(logo);
         titleSection.setComponentAlignment(logo, Alignment.MIDDLE_CENTER);
         return titleSection;
     }
 
     @Override
     public void enter(final ViewChangeListener.ViewChangeEvent event) {
         getUI().setFocusedComponent(loginField);
     }
 
     private static class FakeRememberMeFlag extends HttpServletRequestWrapper {
         private final String rememberMeParameter;
 
         private FakeRememberMeFlag(final String rememberMeParameter, final VaadinServletRequest vaadinServletRequest) {
             super(vaadinServletRequest);
             this.rememberMeParameter = rememberMeParameter;
         }
 
 
         @Override
         public String getParameter(final String name) {
             if (rememberMeParameter.equals(name))
                 return "1";
             return super.getParameter(name);
         }
     }
 }
