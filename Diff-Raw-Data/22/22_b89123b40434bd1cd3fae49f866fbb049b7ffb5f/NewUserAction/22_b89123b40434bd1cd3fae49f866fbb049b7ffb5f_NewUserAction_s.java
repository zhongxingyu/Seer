 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.uniriotec.tracker.struts;
 
 import br.uniriotec.tracker.dao.DAOUser;
 import br.uniriotec.tracker.dao.DAOFactory;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  *
  * @author afonso
  */
 public class NewUserAction extends org.apache.struts.action.Action {
     
      /* forward name="success" path="" */
     private static final String SUCCESS = "success";
     private final static String FAILURE = "failure";
     
      @Override
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
          
         // extract user data
         NewUserForm formBean = (NewUserForm) form;
         String email = formBean.getEmail();
         String password = formBean.getPassword();
         String name = formBean.getName();
         String lastName = formBean.getLastName();
         String confirmedPassword = formBean.getConfirmedPassword();
 
         // perform validation
         if ((email == null) || // name parameter does not exist
                 password == null || // email parameter does not exist
                 confirmedPassword == null ||
                 lastName == null || // email parameter does not exist
                 name == null || // email parameter does not exist
                 password.equals("") || // name parameter is empty
                 !password.equals(confirmedPassword) || // name parameter is empty
                 email.indexOf("@") == -1) {   // email lacks '@'
 
             formBean.setError();
             return mapping.findForward(FAILURE);
         } else {
             DAOUser dao = DAOFactory.getDAOUser();
            dao.createUser(email, password, name, lastName);
         }

        return mapping.findForward(SUCCESS);
     }
    
}
