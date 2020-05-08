 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package autenticaton;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 import javax.servlet.http.HttpSession;
 import java.util.regex.*;
 import javax.faces.component.UIInput;
 /**
  *
  * @author sunshelbi
  */
 @ManagedBean(name= "AuthBean")
 @SessionScoped
 public class AuthBean {
 
     private String pass="pass";
     private String userPass;
     private String login="login";
     private String userLogin;
     String response;
     private String imagePath="ok.jpg";
     private String registerLogin;
     private String registerEmail;
     private String registerPass;
     private UIComponent mybutton;
     private String whatPage="login";
     public AuthBean() {
          System.out.print(String.valueOf(pass));
     }
      public static final Pattern pattern = Pattern.compile("[a-zA-Z]{1}[a-zA-Z\\d\\u002E\\u005F]+@([a-zA-Z]+\\u002E){1,2}((net)|(ru)|(com)|(org))");
     public String register()
     {
         whatPage="register";
        return "response?faces-redirect=true";
      // FacesContext.getCurrentInstance().addMessage("register", new FacesMessage("Succes!"));
     }
     public String login()
     {
 //        if(userLogin.equals(login)&&userPass.equals(pass)) {
 //            return "response";
 //        }
 //        else {
 //            return "login";
 //        }
         
         boolean isValid=userLogin.equals(login)&&userPass.equals(pass);
         if (isValid) {
             whatPage="login";
             return "response?faces-redirect=true";
         }
         else {
             // invalid
             FacesMessage message;
             message = new FacesMessage("Sorry,this comb of login+pass isn't valid");
             FacesContext context = FacesContext.getCurrentInstance();
             context.addMessage(getMybutton().getClientId(context), message);
         }
         return null;
     }
      public String getPass() {
         return pass;
     }
     public void setPass(String pass) {
         this.pass = pass;
     }
     public String getUserPass() {
         return userPass;
     }
     public void setUserPass(String userPass) {
         this.userPass = userPass;
     }
     public String getLogin() {
         return login;
     }
     public void setLogin(String login) {
         this.login = login;
     }
     public String getUserLogin() {
         return userLogin;
     }
     public void setUserLogin(String userLogin) {
         this.userLogin = userLogin;
     }
    
     public String getResponse() 
     {
         if(whatPage.equals("login")) {
            
                 //invalidate user session
 //                FacesContext context = FacesContext.getCurrentInstance();
 //                HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
 //                session.invalidate();
                 setImagePath("images/lucky.jpg");
                 return " You are logged in!You are amazing";
                 
              
         }
         else {
             return registerLogin+" ,You are registered!Message with your "
                     + "login and pass was sent on your e-mail  "+registerEmail;
         }
         
     }
     public void validateMail(FacesContext context,UIComponent toValidate,Object value) throws ValidatorException  
     {
         validate(context, toValidate, value);
         String inputString = (String) value;  
         Matcher matcher = pattern.matcher(inputString);
         if (matcher.matches()) {
             System.out.println("Validation is succesful");
         }
         else
         {
               FacesMessage   message = new FacesMessage("Inputed value isn't correct!");
               throw new ValidatorException(message);
         }
             
        
     }
    public void validateConfirm(FacesContext context, UIComponent component, Object value) throws ValidatorException
     {
         // Cast the value of the entered password to String.
         String password = (String) value;
 
         // Obtain the component and submitted value of the confirm password component.
         UIInput confirmComponent = (UIInput) component.getAttributes().get("confirm");
         String confirm = (String) confirmComponent.getSubmittedValue();
 
         // Check if they both are filled in.
         if (password == null || password.isEmpty() || confirm == null || confirm.isEmpty()) {
             return; // Let required="true" do its job.
         }
 
         // Compare the password with the confirm password.
         if (!password.equals(confirm)) {
             //confirmComponent.setValid(false); // So that it's marked invalid.
             throw new ValidatorException(new FacesMessage("Passwords are not equal."));
         }
 
     }
 
     public void validate(FacesContext context,UIComponent toValidate,Object value) throws ValidatorException  
     {  
          String inputString = (String) value;  
          FacesMessage message;
           if (-1 != inputString.indexOf("хуй")) {  
                 
             message = new FacesMessage("Invalid content!");
                   throw new ValidatorException(message);
           }
           if (inputString.length()<3 ||inputString.length()>20) 
           { 
               message = new FacesMessage("minimal length is 3 chars!");
               throw new ValidatorException(message);
           }
     } 
     public String getImagePath() {
         return imagePath;
     }
     public void setImagePath(String imagePath) {
         this.imagePath = imagePath;
     }
 
     /**
      * @return the registerLogin
      */
     public String getRegisterLogin() {
         return registerLogin;
     }
 
     /**
      * @param registerLogin the registerLogin to set
      */
     public void setRegisterLogin(String registerLogin) {
         this.registerLogin = registerLogin;
     }
 
     /**
      * @return the registerEmail
      */
     public String getRegisterEmail() {
         return registerEmail;
     }
 
     /**
      * @param registerEmail the registerEmail to set
      */
     public void setRegisterEmail(String registerEmail) {
         this.registerEmail = registerEmail;
     }
 
     /**
      * @return the registerPass
      */
     public String getRegisterPass() {
         return registerPass;
     }
 
     /**
      * @param registerPass the registerPass to set
      */
     public void setRegisterPass(String registerPass) {
         this.registerPass = registerPass;
     }
 
     /**
      * @return the mybutton
      */
     public UIComponent getMybutton() {
         return mybutton;
     }
 
     /**
      * @param mybutton the mybutton to set
      */
     public void setMybutton(UIComponent mybutton) {
         this.mybutton = mybutton;
     }
 
 }
