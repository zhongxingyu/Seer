 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package app.commands;
 
 import app.entities.RegistrationForm;
 import app.entities.User;
 import app.mappers.UserMapper;
 import app.services.UserAccountService;
 import core.commands.BusinessTransactionCommand;
 import core.commands.SimpleFrontCommand;
 import core.helpers.AppSessionManager;
 import core.helpers.HttpForm;
 import core.helpers.TimeHelper;
 
 /**
  *
  * @author aldo
  */
 public class RegisterCommand extends BusinessTransactionCommand{
     public void process() {
         String method = method();
 	if(method.equals("new")){
 	    newRegistration();
 	}else if(method.equals("save")){
 	    saveRegistration();
 	}
         
     }
     public void newRegistration(){
         initializeCsfrToken();
         
         RegistrationForm regForm = new RegistrationForm();       
        render(regForm,"/register/new","homepage");
     }
     
     public void saveRegistration(){
         checkCsrfToken();        
         startNewBusinessTransaction();
         RegistrationForm regForm = (RegistrationForm)bind(RegistrationForm.class);
         if(!regForm.validate()){
             badRequest(regForm);
             return;
         }
         UserAccountService service = new UserAccountService();
         if(!service.register(regForm)){
             badRequest(regForm);
             return;
         }
         if(!commitBusinessTransaction()){
             flash("error",getLastError());
             badRequest(regForm);
             return;
         }
         flash("success","User created, please login");
         redirect("/login/index");
     }
     
 }
