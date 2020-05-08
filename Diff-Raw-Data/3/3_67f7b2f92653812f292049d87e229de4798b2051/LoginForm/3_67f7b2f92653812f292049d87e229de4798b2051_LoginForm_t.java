 package nl.sense_os.commonsense.client.auth.login;
 
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
 import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
 import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.event.dom.client.KeyCodes;
 
 public class LoginForm extends FormPanel {
 
     private static final Logger LOG = Logger.getLogger(LoginForm.class.getName());
     private TextField<String> username;
     private TextField<String> password;
     private CheckBox chkRememberMe;
     private Button btnSubmit;
     private Button btnGoogle;
     private LabelField btnForgotPassword;
 
     public LoginForm() {
         super();
 
         setHeading("Login");
         setScrollMode(Scroll.AUTOY);
         setSize("", "auto");
         setLabelAlign(LabelAlign.TOP);
 
         initFields();
         initButtons();
     }
 
     public LabelField getBtnForgotPassword() {
         return btnForgotPassword;
     }
 
     public Button getBtnGoogle() {
         return btnGoogle;
     }
 
     public Button getBtnSubmit() {
         return btnSubmit;
     }
 
     public String getPassword() {
         return password.getValue();
     }
 
     public String getUsername() {
         return username.getValue();
     }
 
     private void initButtons() {
 
         btnForgotPassword = new LabelField("Forgot your password?");
         btnForgotPassword.setHideLabel(true);
         btnForgotPassword.setStyleAttribute("cursor", "pointer");
 
         // btnSubmit button
         btnSubmit = new Button("Log in");
         btnSubmit.setIconStyle("sense-btn-icon-go");
         // btnSubmit.setType("submit"); // "submit" type makes the button always clickable!
         new FormButtonBinding(this).addButton(btnSubmit);
         btnSubmit.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 if (isValid()) {
                     submit();
                 }
             }
         });
 
         LayoutContainer submitWrapper = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
         submitWrapper.setSize("100%", "34px");
         HBoxLayout wrapperLayout = new HBoxLayout();
         wrapperLayout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
         submitWrapper.setLayout(wrapperLayout);
        submitWrapper.add(btnSubmit);
        btnSubmit.setWidth("75px");
         submitWrapper.add(btnForgotPassword, new HBoxLayoutData(0, 0, 0, 10));
 
         // btnGoogle login button
         btnGoogle = new Button("Log in with Google", SenseIconProvider.ICON_GOOGLE);
 
         this.add(submitWrapper, new FormData("-10"));
 
         LabelField alternative = new LabelField(
                 "Alternatively, you can use your Google Account to log in:");
         // alternative.setHideLabel(true);
         this.add(alternative, new FormData("-10"));
         add(btnGoogle);
 
         setupSubmit();
     }
 
     private void initFields() {
 
         // username field
         username = new TextField<String>();
         username.setFieldLabel("Username");
         username.setAllowBlank(false);
 
         // password field
         password = new TextField<String>();
         password.setFieldLabel("Password");
         password.setAllowBlank(false);
         password.setPassword(true);
 
         // remember me check box
         chkRememberMe = new CheckBox();
         chkRememberMe.setHideLabel(true);
         chkRememberMe.setBoxLabel("Remember username");
         chkRememberMe.setValue(true);
 
         this.add(username, new FormData("-20"));
         this.add(password, new FormData("-20"));
         this.add(chkRememberMe, new FormData("-20"));
     }
 
     public boolean isRememberMe() {
         return chkRememberMe.getValue();
     }
 
     public void setBusy(boolean busy) {
         if (busy) {
             btnSubmit.setIconStyle("sense-btn-icon-loading");
         } else {
             btnSubmit.setIconStyle("sense-btn-icon-go");
         }
     }
 
     public void setPassword(String password) {
         if (null != password) {
             this.password.setValue(password);
         } else {
             this.password.clear();
         }
     }
 
     public void setRememberMe(boolean rememberMe) {
         chkRememberMe.setValue(rememberMe);
     }
 
     /**
      * Defines how to btnSubmit the form, and the actions to take when the form is submitted.
      */
     private void setupSubmit() {
 
         // ENTER-key listener to btnSubmit the form using the keyboard
         final KeyListener submitListener = new KeyListener() {
             @Override
             public void componentKeyDown(ComponentEvent event) {
                 if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                     LOG.finest("Pressed enter to submit form");
                     if (isValid()) {
                         submit();
                     }
                 }
             }
         };
         username.addKeyListener(submitListener);
         password.addKeyListener(submitListener);
 
         // form action is not a regular URL, but we listen for the btnSubmit event instead
         setAction("javascript:;");
     }
 
     public void setUsername(String username) {
         if (null != username && !username.equalsIgnoreCase("null")) {
             this.username.setValue(username);
         } else {
             this.username.clear();
         }
     }
 }
