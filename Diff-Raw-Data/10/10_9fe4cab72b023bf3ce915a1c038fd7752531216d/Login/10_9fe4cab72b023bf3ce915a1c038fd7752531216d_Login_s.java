 package nl.sense_os.commonsense.client;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FormEvent;
 import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import java.util.Date;
 
 import nl.sense_os.commonsense.client.services.DataService;
 import nl.sense_os.commonsense.client.services.DataServiceAsync;
 import nl.sense_os.commonsense.client.utility.Log;
 import nl.sense_os.commonsense.client.utility.MD5Wrapper;
 import nl.sense_os.commonsense.dto.UserModel;
 import nl.sense_os.commonsense.dto.exceptions.DbConnectionException;
 import nl.sense_os.commonsense.dto.exceptions.WrongResponseException;
 
 public class Login extends LayoutContainer {
 
     private static final String TAG = "LoginForm";
     private final AsyncCallback<UserModel> callback;
     private boolean autoLogin;
     private final String cookieName;
     private final String cookiePass;
     private final TextField<String> email = new TextField<String>();
     private final TextField<String> pass = new TextField<String>();
     private final CheckBox rememberMe = new CheckBox();
 
     public Login(AsyncCallback<UserModel> callback) {
 
         this.callback = callback;
 
         // get user from Cookie
         this.cookieName = Cookies.getCookie("user_name");
         this.cookiePass = Cookies.getCookie("user_pass");
         if ((null != cookieName) && (null != cookiePass) && (cookieName.length() > 0)
                 && (cookiePass.length() > 0)) {
             Log.d(TAG, "Autologin");
             this.autoLogin = true;
         } else {
             this.autoLogin = false;
         }
 
         final FormPanel form = createForm();
 
         this.setLayout(new CenterLayout());
         this.add(form);
     }
 
     private void checkLogin(String name, String password) {
 
         // show progress dialog
         final MessageBox waitBox = MessageBox.wait("CommonSense Login",
                 "Logging in, please wait...", "Logging in...");
 
         final AsyncCallback<UserModel> callback = new AsyncCallback<UserModel>() {
             @Override
             public void onFailure(Throwable ex) {
                 waitBox.close();
                 MessageBox.alert("Login failure!", "Server-side failure." + ex.toString(), null);
                 pass.clear();
 
                 if (ex instanceof WrongResponseException) {
                     MessageBox.alert("Login failed!", "Invalid username or password.", null);
                 } else if (ex instanceof DbConnectionException) {
                     MessageBox.alert("Login failed!", "Failed to connect to CommonSense database.",
                             null);
                 } else {
                     MessageBox.alert("Login failed!", "Server-side failure: " + ex.getMessage(),
                             null);
                 }
             }
 
             @Override
             public void onSuccess(UserModel user) {
                 waitBox.close();
                 if (user != null) {
                     if (rememberMe.getValue()) {
                         final long DURATION = 1000 * 60 * 60 * 24 * 14; // 2 weeks
                         Date expires = new Date(System.currentTimeMillis() + DURATION);
                         Cookies.setCookie("user_name", user.getName(), expires, null, "/", false);
                         Cookies.setCookie("user_pass", user.getPassword(), expires, null, "/",
                                 false);
                     }
 
                     Login.this.callback.onSuccess(user);
 
                 } else {
                     MessageBox.alert("Login failure!", "Invalid username or password.", null);
                     pass.clear();
                 }
             }
         };
         final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);
         service.checkLogin(name, password, callback);
     }
 
     private FormPanel createForm() {
         final FormData formData = new FormData("-10");
 
         // email field
         email.setFieldLabel("Email:");
         if (this.autoLogin) {
             email.setValue(this.cookieName);
         }
         email.setAllowBlank(false);
 
         // password field
         pass.setFieldLabel("Password:");
         if (this.autoLogin) {
             pass.setValue("********");
         }
         pass.setAllowBlank(false);
         pass.setPassword(true);
 
         rememberMe.setLabelSeparator("");
         rememberMe.setBoxLabel("Remember me");
         rememberMe.setValue(true);
 
         // main form panel
         final FormPanel form = new FormPanel();
         form.setButtonAlign(HorizontalAlignment.CENTER);
         form.setLabelSeparator("");
         form.setFrame(true);
         form.setHeading("CommonSense Login");
         form.setLabelWidth(100);
        //form.setFieldWidth(250);
 
         setupSubmitAction(form);
 
         form.add(email, formData);
         form.add(pass, formData);
         form.add(rememberMe);
 
         return form;
     }
 
     private void setupSubmitAction(final FormPanel form) {
 
         // submit button
         final Button b = new Button("Submit");
         b.setType("submit");
         b.addSelectionListener(new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent be) {
                 form.submit();
             }
         });
         form.addButton(b);
         form.setButtonAlign(HorizontalAlignment.CENTER);
 
         final FormButtonBinding binding = new FormButtonBinding(form);
         binding.addButton(b);
 
         // enter key listener
        pass.addKeyListener(new KeyListener() {
             @Override
             public void componentKeyDown(ComponentEvent event) {
                 if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                     if (form.isValid()) {
                         form.submit();
                     }
                 }
             }
        });
 
         // form action
         form.setAction("javascript:;");
         form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
 
             @Override
             public void handleEvent(FormEvent be) {
                 String name = email.getValue();
                 String password = MD5Wrapper.toMD5(pass.getValue());
 
                 checkLogin(name, password);
             }
 
         });
     }
 
     @Override
     protected void onRender(Element parent, int index) {
         super.onRender(parent, index);
 
         if (this.autoLogin) {
             checkLogin(this.cookieName, this.cookiePass);
         }
         
         email.setOriginalValue(this.cookieName);
     }
 }
