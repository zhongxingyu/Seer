 package vsp.servlet.handler;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import vsp.exception.SqlRequestException;
 import vsp.exception.ValidationException;
 import vsp.form.validator.FormValidator;
 import vsp.form.validator.FormValidatorFactory;
 import vsp.servlet.form.UpdatePasswordForm;
 
 public class SubmitPasswordUpdateHandler extends BaseServletHandler implements
     ServletHandler {
 
   @Override
   public void process(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException 
   {
     try{
       errors.clear();
       UpdatePasswordForm passwordForm = new UpdatePasswordForm();
       String uri = request.getRequestURI();
       int lastIndex = uri.lastIndexOf("/");
       String action = uri.substring(lastIndex + 1); 
       
       String userName = (String)request.getSession().getAttribute("password_user");      
       if(userName == null || userName.isEmpty()){
     	userName = request.getRemoteUser();
     	if(userName == null || userName.isEmpty())
     		userName = (String) request.getSession().getAttribute("userName");
       }
       if(userName != null && !userName.isEmpty())
       {
     	  
     	if(action.equals("submitUpdatePassword")){
     		if(!vsp.checkUserPassword(userName, 
     								request.getParameter("current_password")))
     		{
    			 errors.add("User Password is invalid");
     			 dispatchUrl = "updatePassword";
     			 request.setAttribute("errors", errors);
     			 return;
     		}
     	}
         passwordForm.setUserName(userName);
         passwordForm.setPassword(request.getParameter("password"));
         passwordForm.setVerifyPassword(request.getParameter("verifyPassword"));
         
         FormValidator passwordValidator = FormValidatorFactory.getUpdatePasswordValidator();
         List<String> errors = passwordValidator.validate(passwordForm);
         if(errors.isEmpty()){
           try {
             vsp.updateUserPassword(passwordForm.getUserName(), passwordForm.getPassword(), passwordForm.getVerifyPassword());
             request.setAttribute("passwordUpdate", "Password has been successfully changed");
             if(request.isUserInRole("admin")){
             	List<String> traders;
             	traders = vsp.getTraders();
                 if (traders.size() > 0){
                   request.setAttribute("traders", traders);
                 }
             	dispatchUrl = "/admin/Admin.jsp";
             }
             else if(action.equals("submitResetPassword"))
               dispatchUrl = "login";            
             else if(action.equals("submitUpdatePassword"))
               dispatchUrl = "updatePassword";            
           } catch (SQLException | SqlRequestException | ValidationException e) {
             errors.add(e.getMessage());
             request.setAttribute("errors", errors);
             if(action.equals("submitResetPassword")){
               dispatchUrl="Error.jsp";
             }
             else if(action.equals("submitUpdatePassword")){
               dispatchUrl = "updatePassword";
             }
           }
         }else{
           request.setAttribute("errors", errors);
           if(request.isUserInRole("admin")){          
             dispatchUrl="ResetUserPassword.jsp";
           }          
           else if(action.equals("submitResetPassword")){
             dispatchUrl="Error.jsp";
           }
           else if(action.equals("submitUpdatePassword")){
             dispatchUrl = "updatePassword";
           }
         }
       }else{
         errors.add("Unknown user name");
         if(action.equals("submitResetPassword")){
           dispatchUrl="Error.jsp";
         }
         else if(action.equals("submitUpdatePassword")){
           dispatchUrl = "updatePassword";
         }
       }
     } catch (SQLException e) {
 		errors.add("Error verifying user password: " + e.getMessage());
 		dispatchUrl="Error.jsp";
 		request.setAttribute("errors", errors);
 	}
     finally{
       request.getSession().removeAttribute("userName");
     }
   }
 }
