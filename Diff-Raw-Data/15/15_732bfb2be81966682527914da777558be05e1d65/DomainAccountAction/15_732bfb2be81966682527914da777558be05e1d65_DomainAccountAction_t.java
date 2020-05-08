 package jamm.webapp;
 
 import java.util.Arrays;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 
 public class DomainAccountAction extends Action
 {
     public ActionForward execute(ActionMapping mapping,
                                  ActionForm actionForm,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
         throws Exception
     {
         DomainAccountForm form = (DomainAccountForm) actionForm;
 
        System.out.println("====================================" +
                           "====================================");
        
         System.out.println("Delete: " +
                            Arrays.asList(form.getAccountsToDelete()));
         System.out.println("Unchecked active: " +
                            form.getUncheckedActiveAccounts());
         System.out.println("Checked active: " +
                            form.getCheckedActiveAccounts());
 
         System.out.println("Unchecked admin: " +
                            form.getUncheckedAdminAccounts());
         System.out.println("Checked admin: " +
                            form.getCheckedAdminAccounts());
 
         return mapping.findForward("domain_admin");
     }
 }
