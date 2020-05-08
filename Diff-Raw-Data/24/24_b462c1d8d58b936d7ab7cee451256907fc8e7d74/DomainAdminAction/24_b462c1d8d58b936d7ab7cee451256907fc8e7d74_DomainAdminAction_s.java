 package jamm.webapp;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 
 import jamm.backend.MailManager;
 import jamm.backend.AccountInfo;
import jamm.backend.AliasInfo;
 
 public class DomainAdminAction extends JammAction
 {
     public ActionForward execute(ActionMapping mapping,
                                  ActionForm actionForm,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
         throws Exception
     {
         User user  = getUser(request);
         MailManager manager = getMailManager(user);
         String domain = domainFromMail(user.getUsername());
         request.setAttribute("domainName", domain);
 
         List accounts = manager.getAccounts(domain);
         request.setAttribute("accounts", accounts);
 
         List activeAccounts = new ArrayList();
         List adminAccounts = new ArrayList();
         Iterator i = accounts.iterator();
         while (i.hasNext())
         {
             AccountInfo account = (AccountInfo) i.next();
             if (account.isActive())
             {
                 activeAccounts.add(account.getName());
             }
             if (account.isAdministrator())
             {
                 adminAccounts.add(account.getName());
             }
         }
 
         String[] activeAccountsArray =
             (String []) activeAccounts.toArray(new String[0]);
         String[] adminAccountsArray =
             (String []) adminAccounts.toArray(new String[0]);
         DomainAccountForm daf = new DomainAccountForm();
         daf.setOriginalActiveAccounts(activeAccountsArray);
         daf.setActiveAccounts(activeAccountsArray);
         daf.setOriginalAdminAccounts(adminAccountsArray);
         daf.setAdminAccounts(adminAccountsArray);
         request.setAttribute("domainAccountForm", daf);
 
         // Prepare aliases
         List aliases = manager.getAliases(domain);
         request.setAttribute("aliases", aliases);
 
         return (mapping.findForward("domain_admin"));
     }
 
     private final String domainFromMail(String mail)
     {
         int domainIndex = mail.indexOf("@");
         return mail.substring(domainIndex + 1);
     }
 }
