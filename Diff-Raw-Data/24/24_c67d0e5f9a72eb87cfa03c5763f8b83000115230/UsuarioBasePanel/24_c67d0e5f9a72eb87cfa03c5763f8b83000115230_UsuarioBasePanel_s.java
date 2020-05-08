 package ar.noxit.ehockey.web.pages.usuarios;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
 import org.apache.wicket.markup.html.form.validation.IFormValidator;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.validation.validator.StringValidator.MinimumLengthValidator;
 
 public class UsuarioBasePanel extends Panel {
 
     private String password;
     private IFormValidator passwordValidator;
     private boolean estadoUsuarioEditable = true;
     private boolean passwordRequired = true;
 
     public UsuarioBasePanel(String id, IModel<UsuarioDTO> usuario) {
         super(id, usuario);
 
         FormComponent<String> passText = new PasswordTextField("contrasena",
                 new PropertyModel<String>(usuario, "password")) {
             @Override
             public boolean isRequired() {
                 return passwordRequired;
             }
         };
         passText.add(new MinimumLengthValidator(6));
         FormComponent<String> verifyText = new PasswordTextField("contrasena2",
                 new PropertyModel<String>(usuario, "password")).setRequired(false);
         add(new RequiredTextField<String>("usuario", new PropertyModel<String>(usuario, "user")) {
 
             @Override
             public boolean isVisible() {
                 return estadoUsuarioEditable;
             }
         });
         add(new Label("usuarioNE", new PropertyModel<String>(usuario, "user")) {
 
             @Override
             public boolean isVisible() {
                 return !estadoUsuarioEditable;
             }
         });
 
         add(passText);
         add(verifyText);
         add(new RequiredTextField<String>("nombre", new PropertyModel<String>(usuario, "nombre")));
         add(new RequiredTextField<String>("apellido", new PropertyModel<String>(usuario, "apellido")));
 
         passwordValidator = new EqualPasswordInputValidator(passText, verifyText);
     }
 
     public void addValidators(Form<Void> form) {
         form.add(passwordValidator);
     }
 
     public void setUsuarioEditable(boolean estado) {
         this.estadoUsuarioEditable = estado;
         this.passwordRequired = estado;
     }
 }
