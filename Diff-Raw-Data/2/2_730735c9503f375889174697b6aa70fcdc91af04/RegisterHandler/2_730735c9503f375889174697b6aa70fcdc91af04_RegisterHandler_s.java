 package vsp.servlet.handler;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import vsp.dataObject.AccountData;
 import vsp.exception.SqlRequestException;
 import vsp.exception.ValidationException;
 import vsp.form.validator.FormValidator;
 import vsp.form.validator.FormValidatorFactory;
 import vsp.servlet.form.RegisterForm;
 
 
 public class RegisterHandler extends BaseServletHandler implements ServletHandler {
 
   @Override
   public void process(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException 
   {
     RegisterForm registrationForm = new RegisterForm();
     registrationForm.setUserName(request.getParameter("userName"));
     registrationForm.setPassword(request.getParameter("password"));
     registrationForm.setVerifyPassword(request.getParameter("verifyPassword"));
     registrationForm.setEmail(request.getParameter("email"));
     registrationForm.setQuestion(request.getParameter("question"));
     registrationForm.setAnswer(request.getParameter("answer"));
     
     //validate userForm Data
     FormValidator validator = FormValidatorFactory.getRegistrationValidator();
     List<String> errors = validator.validate(registrationForm);
     if(errors.isEmpty()){
       AccountData userAccount = new AccountData(registrationForm);
         try {
           vsp.createTraderAccount(userAccount);
           String loginMessage = "User Account Successfully Created!";
           request.setAttribute("user", userAccount);
           request.setAttribute("loginSuccessMessage", loginMessage);
          dispatchUrl = "/Portfolio.jsp";
         } catch (SQLException | SqlRequestException | ValidationException e) {
           errors.add("Failed to create new account for: " + 
               userAccount.getUserName() + " ." + e.getMessage());
           request.setAttribute("errors", errors);
           dispatchUrl = "/Signup.jsp";
         }
       
     }else{
       request.setAttribute("errors", errors);
       dispatchUrl = "/Signup.jsp";
     }
   }
 }
