 package jamm.webapp;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Arrays;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 
 
 /**
  * Holds the check boxes for the account list of the domain admin
  * form.  This is a list of check boxes for Delete, Action, and Admin.
  */
 public class DomainAccountForm extends ActionForm
 {
     public void setAccountsToDelete(String[] accountsToDelete)
     {
         mAccountsToDelete = accountsToDelete;
     }
 
     public String[] getAccountsToDelete()
     {
         return mAccountsToDelete;
     }
 
     public void setOriginalActiveAccounts(String[] originalActiveAccounts)
     {
         mOriginalActiveAccounts = originalActiveAccounts;
     }
 
     public String[] getOriginalActiveAccounts()
     {
         return mOriginalActiveAccounts;
     }
 
     public void setActiveAccounts(String[] activeAccounts)
     {
         mActiveAccounts = activeAccounts;
     }
 
     public String[] getActiveAccounts()
     {
         return mActiveAccounts;
     }
     
     public Set getCheckedActiveAccounts()
     {
         return getDifference(mActiveAccounts, mOriginalActiveAccounts);
     }
 
     public Set getUncheckedActiveAccounts()
     {
         return getDifference(mOriginalActiveAccounts, mActiveAccounts);
     }
 
     public void setOriginalAdminAccounts(String[] originalAdminAccounts)
     {
         mOriginalAdminAccounts = originalAdminAccounts;
     }
 
     public String[] getOriginalAdminAccounts()
     {
         return mOriginalAdminAccounts;
     }
 
     public void setAdminAccounts(String[] adminAccounts)
     {
         mAdminAccounts = adminAccounts;
     }
 
     public String[] getAdminAccounts()
     {
         return mAdminAccounts;
     }
 
     public Set getCheckedAdminAccounts()
     {
         return getDifference(mAdminAccounts, mOriginalAdminAccounts);
     }
 
     public Set getUncheckedAdminAccounts()
     {
         return getDifference(mOriginalAdminAccounts, mAdminAccounts);
     }
 
     private Set getDifference(String[] list1, String[] list2)
     {
         Set set1 = new HashSet(Arrays.asList(list1));
         Set set2 = new HashSet(Arrays.asList(list2));
         set1.removeAll(set2);
         return set1;
     }
 
     /**
      *
      */
     public void reset(ActionMapping mapping, HttpServletRequest request)
     {
         mAccounts = new String[0];
         mAccountsToDelete = new String[0];
         mOriginalActiveAccounts = new String[0];
         mActiveAccounts = new String[0];
         mOriginalAdminAccounts = new String[0];
         mAdminAccounts = new String[0];
     }
 
     private String[] mAccounts;
     private String[] mAccountsToDelete;
     private String[] mOriginalActiveAccounts;
     private String[] mActiveAccounts;
     private String[] mOriginalAdminAccounts;
     private String[] mAdminAccounts;
 }
