 package nl.sense_os.commonsense.client.common.components;
 
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.constants.Constants;
 import nl.sense_os.commonsense.client.utility.SenseIconProvider;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.Validator;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.google.gwt.event.dom.client.KeyCodes;
 
 public class RegisterForm extends FormPanel {
 
     private class PhoneValidator implements Validator {
 
         @Override
         public String validate(Field<?> field, String value) {
 
             if (!country.isValid(true)) {
                 return "please select a country";
             }
 
             ModelData countryValue = country.getValue();
             if (countryValue == null) {
                 return "please select a country";
             }
 
             String validated = validatePhoneNumber(value, countryValue.<String> get("code"));
             if (validated.equals("not valid")) {
                 return "invalid phone number";
             } else if (validated.equals("error")) {
                 return "invalid phone number";
             } else {
                 mobile.setRawValue(validated);
                 return null;
             }
         }
     }
 
     private static final Logger LOG = Logger.getLogger(RegisterForm.class.getName());
    private static final String EMAIL_REGEX = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+(?:[a-zA-Z]{2}|com|org|net|edu|gov|mil|biz|info|mobi|name|aero|asia|jobs|museum)";
 
     private static native final String validatePhoneNumber(String phoneNumber, String regionCode) /*-{
         try {
             var phoneUtil = $wnd.i18n.phonenumbers.PhoneNumberUtil.getInstance();
             var number = phoneUtil.parseAndKeepRawInput(phoneNumber, regionCode);
             var isValid = phoneUtil.isValidNumber(number);
             if (isValid) {
                 //                var phoneType = phoneUtil.getNumberType(number);
                 //                var PNT = $wnd.i18n.phonenumbers.PhoneNumberType;
                 //                if (phoneType == PNT.MOBILE) {
                 var PNF = $wnd.i18n.phonenumbers.PhoneNumberFormat;
                 return phoneUtil.format(number, PNF.E164);
                 //                } else {
                 //                    return "not mobile";
                 //                }
             } else {
                 return "not valid";
             }
         } catch (e) {
             return "error";
         }
     }-*/;
 
     private TextField<String> username;
     private TextField<String> password;
     private TextField<String> name;
     private TextField<String> surname;
     private TextField<String> email;
     private TextField<String> mobile;
     private Button submit;
     private ComboBox<ModelData> country;
 
     public RegisterForm() {
         super();
 
         setLabelSeparator("");
         setBodyBorder(false);
         setHeaderVisible(false);
         setScrollMode(Scroll.AUTOY);
         setLabelAlign(LabelAlign.TOP);
 
         initFields();
         initButtons();
     }
 
     public String getEmail() {
         return email.getValue();
     }
 
     public String getMobile() {
         return mobile.getValue();
     }
 
     public String getName() {
         return name.getValue();
     }
 
     public String getPassword() {
         return password.getValue();
     }
 
     public String getSurname() {
         return surname.getValue();
     }
 
     public String getUsername() {
         return username.getValue();
     }
 
     private void initButtons() {
 
         SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent be) {
                 final Button b = be.getButton();
                 if (b.equals(submit)) {
                     submit();
                 } else {
                     LOG.warning("Unexpected button pressed!");
                 }
             }
         };
 
         // submit button
         submit = new Button("Register", SenseIconProvider.ICON_BUTTON_GO, l);
         submit.setType("submit");
 
         setButtonAlign(HorizontalAlignment.CENTER);
         addButton(submit);
 
         final FormButtonBinding binding = new FormButtonBinding(this);
         binding.addButton(submit);
 
         setupSubmit();
     }
 
     private void initFields() {
 
         final FormData formData = new FormData("-10");
 
         // username field
         username = new TextField<String>();
         username.setFieldLabel("Username:");
         username.setAllowBlank(false);
 
         // password field
         password = new TextField<String>();
         password.setFieldLabel("Password:");
         password.setMinLength(4);
         password.setPassword(true);
 
         // name field
         name = new TextField<String>();
         name.setFieldLabel("First name:");
         name.setAllowBlank(false);
 
         // surname field
         surname = new TextField<String>();
         surname.setFieldLabel("Surname:");
         surname.setAllowBlank(false);
 
         // email field
         email = new TextField<String>();
         email.setFieldLabel("Email:");
         email.setRegex(EMAIL_REGEX);
         email.getMessages().setRegexText("Invalid email address");
 
         // country field
         ListStore<ModelData> countries = new ListStore<ModelData>();
         countries.add(Constants.getCountries());
         country = new ComboBox<ModelData>();
         country.setFieldLabel("Country:");
         country.setStore(countries);
         country.setTriggerAction(TriggerAction.ALL);
         country.setAllowBlank(false);
         country.setEmptyText("Select a country...");
 
         // phone field
         mobile = new TextField<String>();
         mobile.setFieldLabel("Phone:");
         mobile.setAllowBlank(false);
         mobile.setValidator(new PhoneValidator());
 
         this.add(username, formData);
         this.add(password, formData);
         this.add(password, formData);
         this.add(name, formData);
         this.add(surname, formData);
         this.add(email, formData);
         this.add(country, formData);
         this.add(mobile, formData);
     }
 
     @Override
     public void reset() {
         setUsername(null);
         setPassword(null);
         setName(null);
         setSurname(null);
         setEmail(null);
         setCountry(null);
         setMobile(null);
     }
 
     public void setBusy(boolean busy) {
         if (busy) {
             submit.setIcon(SenseIconProvider.ICON_LOADING);
         } else {
             submit.setIcon(SenseIconProvider.ICON_BUTTON_GO);
         }
     }
 
     public void setCountry(String code) {
         country.select(0);
     }
 
     public void setEmail(String email) {
         if (null != email) {
             this.email.setValue(email);
         } else {
             this.email.clear();
         }
     }
 
     public void setMobile(String mobile) {
         if (null != mobile) {
             this.mobile.setValue(mobile);
         } else {
             this.mobile.clear();
         }
     }
 
     public void setName(String name) {
         if (null != name) {
             this.name.setValue(name);
         } else {
             this.name.clear();
         }
     }
 
     public void setPassword(String password) {
         if (null != password) {
             this.password.setValue(password);
         } else {
             this.password.clear();
         }
     }
 
     public void setSurname(String surname) {
         if (null != surname) {
             this.surname.setValue(surname);
         } else {
             this.surname.clear();
         }
     }
 
     /**
      * Defines how to submit the form, and the actions to take when the form is submitted.
      */
     private void setupSubmit() {
 
         // ENTER-key listener to submit the form using the keyboard
         final KeyListener submitListener = new KeyListener() {
             @Override
             public void componentKeyDown(ComponentEvent event) {
                 if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                     if (isValid()) {
                         submit();
                     }
                 }
             }
         };
         mobile.addKeyListener(submitListener);
 
         // form action is not a regular URL, but we listen for the submit event instead
         setAction("javascript:;");
     }
 
     public void setUsername(String username) {
         if (null != username) {
             this.username.setValue(username);
         } else {
             this.username.clear();
         }
     }
 }
