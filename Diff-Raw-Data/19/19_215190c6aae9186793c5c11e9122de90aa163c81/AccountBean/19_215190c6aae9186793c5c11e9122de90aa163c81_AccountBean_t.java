 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.beans.admin.main;
 
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxArrayUtils;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.AccountEngine;
 import com.flexive.shared.interfaces.UserGroupEngine;
 import com.flexive.shared.security.*;
 import com.flexive.war.FxRequest;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.faces.component.UISelectBoolean;
 import javax.faces.model.SelectItem;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Management of accounts.
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class AccountBean {
     private static final Log LOG = LogFactory.getLog(AccountBean.class);
 
     private UISelectBoolean activeFilterCheckbox;
     private UISelectBoolean validatedFilterCheckbox;
 
     private long groupFilter = -1;
     private long accountIdFiler = -1;
     private AccountEngine accountInterface;
     private boolean activeFilter = true;
     private boolean validatedFilter = true;
     private String password;
     private String passwordConfirm;
     private Account account;
     private final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");
     private List<UserGroup> groups;
     private List<Role> roles;
     private Mandator mandator;
     private Hashtable<String, ArrayList<Account>> listCache;
     private static final String ID_CACHE_KEY = AccountBean.class + "_id";
     private FxContent contactData;
 
 
     public List<Role> getRoles() {
         return roles == null ? new ArrayList<Role>(0) : roles;
     }
 
     /**
      * Getter for roles implicitly set from groups
      *
      * @return roles implicitly set from groups
      * @throws FxApplicationException on errors
      */
     public List<Role> getRolesGroups() throws FxApplicationException {
         if (groups == null)
             return new ArrayList<Role>(0);
         List<Role> result = new ArrayList<Role>(Role.values().length);
         for (UserGroup grp : groups) {
             List<Role> tmp = EJBLookup.getUserGroupEngine().getRoles(grp.getId()).getList();
             result.removeAll(tmp);
             result.addAll(tmp);
         }
         return result;
     }
 
     public void setRoles(List<Role> roles) {
         this.roles = roles;
     }
 
     public List<UserGroup> getGroups() {
         return groups == null ? new ArrayList<UserGroup>(0) : groups;
     }
 
     public void setGroups(List<UserGroup> groups) {
         this.groups = groups;
     }
 
 
     public long getAccountIdFiler() {
         return accountIdFiler;
     }
 
     public void setAccountIdFiler(long accountIdFiler) {
         this.accountIdFiler = accountIdFiler;
         FxJsfUtils.setSessionAttribute(ID_CACHE_KEY, this.accountIdFiler);
     }
 
     /**
      * Constructor.
      */
     public AccountBean() {
         accountInterface = EJBLookup.getAccountEngine();
         listCache = new Hashtable<String, ArrayList<Account>>(5);
         resetFilter();
     }
 
     private void resetAccount() {
         account = new Account();
     }
 
     private void resetFilter() {
         resetAccount();
         groupFilter = -1;
         groups = null;
         roles = null;
         activeFilter = true;
         validatedFilter = true;
         UserTicket ticket = FxContext.get().getTicket();
         if( !ticket.isGlobalSupervisor() ) {
             mandator = CacheAdmin.getFilteredEnvironment().getMandator(ticket.getMandatorId());
         }
         try {
             // Default valid from: Today, but delete time part
             account.setValidFrom(SDF.parse(SDF.format(new Date())));
             // Default valid to: Way in the future
             account.setValidTo(SDF.parse("01-01-3000"));
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
     }
 
     public boolean isActiveFilter() {
         if (activeFilterCheckbox != null && activeFilterCheckbox.getSubmittedValue() != null) {
             // use submitted value during postbacks to apply the correct filters
             return Boolean.parseBoolean((String) activeFilterCheckbox.getSubmittedValue());
         }
         return activeFilter;
     }
 
     public void setActiveFilter(boolean activeFilter) {
         this.activeFilter = activeFilter;
     }
 
     public boolean isValidatedFilter() {
         if (validatedFilterCheckbox != null && validatedFilterCheckbox.getSubmittedValue() != null) {
             // use submitted value during postbacks to apply the correct filters
             return Boolean.parseBoolean((String) validatedFilterCheckbox.getSubmittedValue());
         }
         return validatedFilter;
     }
 
     public void setValidatedFilter(boolean validatedFilter) {
         this.validatedFilter = validatedFilter;
     }
 
     public Mandator getMandator() {
         return mandator;
     }
 
     public void setMandator(Mandator mandator) {
         this.mandator = mandator;
         this.account.setMandatorId(mandator == null ? -1 : mandator.getId());
     }
 
     public long getGroupFilter() {
         return groupFilter;
     }
 
     public void setGroupFilter(long groupFilter) {
         this.groupFilter = groupFilter;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getPasswordConfirm() {
         return passwordConfirm;
     }
 
     public void setPasswordConfirm(String passwordConfirm) {
         this.passwordConfirm = passwordConfirm;
     }
 
     public Account getAccount() {
         return account;
     }
 
 
     public void setAccount(Account account) {
         this.account = account;
     }
 
     public UISelectBoolean getActiveFilterCheckbox() {
         return activeFilterCheckbox;
     }
 
     public void setActiveFilterCheckbox(UISelectBoolean activeFilterCheckbox) {
         this.activeFilterCheckbox = activeFilterCheckbox;
     }
 
     public UISelectBoolean getValidatedFilterCheckbox() {
         return validatedFilterCheckbox;
     }
 
     public void setValidatedFilterCheckbox(UISelectBoolean validatedFilterCheckbox) {
         this.validatedFilterCheckbox = validatedFilterCheckbox;
     }
 
     /**
      * Returns all user groups defined for the current mandator that are not flagged as system.
      *
      * @return all user groups defined for the current mandator.
      * @throws FxApplicationException if the user groups could not be fetched successfully.
      */
     public List<SelectItem> getFilteredUserGroups() throws FxApplicationException {
         long mandatorId;
         if (getAccount().isNew()) {
             if (getMandator() == null)
                 return new ArrayList<SelectItem>(0);
             else
                 mandatorId = getMandator().getId();
         } else
             mandatorId = getAccount().getMandatorId();
         UserGroupEngine groupEngine = EJBLookup.getUserGroupEngine();
         List<UserGroup> groups = groupEngine.loadAll(mandatorId).getList();
         List<SelectItem> userGroupsNonSystem = new ArrayList<SelectItem>(groups.size());
         for (UserGroup group : groups) {
             if (group.isSystem())
                 continue;
             userGroupsNonSystem.add(new SelectItem(group, group.getName()));
         }
         return userGroupsNonSystem;
     }
 
    /**
     * Force an initialization of the current user preferences if the bean is not initialized
     * 
     * @return dummy
     */
    public String getEditUserPref() {
        if( this.account == null || this.account.getId() == -1)
            editUserPref();
        return null;
    }

     public String getParseRequestParameters() {
         try {
             String action = FxJsfUtils.getParameter("action");
             if (StringUtils.isBlank(action)) {
                 // no action requested
                 return null;
             }
             // hack!
             FxJsfUtils.resetFaceletsComponent("listForm");
 
             if ("editUserPref".equals(action)) {
                 editUserPref();
             }
         } catch (Exception e) {
             // TODO possibly pass some error message to the HTML page
             LOG.error("Failed to parse request parameters: " + e.getMessage(), e);
         }
         return null;
     }
 
     public String showContactData(){
         FxRequest request = FxJsfUtils.getRequest();        
         if(this.account.getContactData() != null){
             request.setAttribute("cdId", this.account.getContactData().getId());
             request.setAttribute("vers", this.account.getContactData().getVersion());
         } else {
             request.setAttribute("cdId", -1);
             request.setAttribute("vers", -1);
         }
         return "showContentEditor";
     }
 
     /**
      * Deletes a user, with the id specified by accountIdFiler.
      *
      * @return the next pageto render
      */
     public String deleteUser() {
         try {
             ensureAccountIdSet();
             accountInterface.remove(accountIdFiler);
             new FxFacesMsgInfo("User.nfo.deleted").addToContext();
             listCache.clear();
             resetFilter();
         } catch (Exception exc) {
             resetAccount();
             new FxFacesMsgErr(exc).addToContext();
         }
         return "accountOverview";
     }
 
     private void ensureAccountIdSet() {
         if (this.accountIdFiler <= 0) {
             this.accountIdFiler = (Long) FxJsfUtils.getSessionAttribute(ID_CACHE_KEY);
         }
     }
 
     /**
      * Loads the user specified by the parameter accountIdFilter.
      *
      * @return the next page to render
      */
     public String editUser() {
         try {
             ensureAccountIdSet();
             this.account = accountInterface.load(this.accountIdFiler);
             this.roles = accountInterface.getRoleList(this.accountIdFiler, AccountEngine.RoleLoadMode.FROM_USER_ONLY);
             this.groups = accountInterface.getGroupList(this.accountIdFiler);
             return "accountEdit";
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return "accountEdit";
         }
     }
 
     /**
      * Loads the account of the current user
      *
      * @return the next page to render
      */
     public String editUserPref() {
         try {
             this.account = accountInterface.load(FxContext.get().getTicket().getUserId());
             setAccountIdFiler(this.account.getId());
             this.roles = accountInterface.getRoleList(this.accountIdFiler, AccountEngine.RoleLoadMode.ALL);
             this.groups = accountInterface.getGroupList(this.accountIdFiler);
             this.contactData = null;
             try{
                 this.contactData = EJBLookup.getContentEngine().load(this.account.getContactData());
             } catch(NullPointerException ex){
                 // ...
             }
             return "userEdit";
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return "contentPage";
         }
     }
 
     public FxContent getContactData() {
         return contactData;
     }
 
     /**
      * Navigate back to the overview and reset all form data
      *
      * @return overview oage
      */
     public String overview() {
         //keep filter but reset account data
         resetAccount();
         return "accountOverview";
     }
 
 
     /**
      * save the user settings
      *
      * @return  content page
      */
     public String saveUserPref() {
         try {
             String newPasswd = null;
 
             // Determine if the password must be set
             if (password != null && password.trim().length() > 0) {
                 if (!password.equals(passwordConfirm)) {
                     new FxFacesMsgErr("User.err.passwordsDontMatch").addToContext();
                     return editUserPref();
                 }
                 newPasswd = password;
             }
             accountInterface.updateUser(this.accountIdFiler, newPasswd, null, null, this.account.getEmail(), this.account.getLanguage().getId());
             new FxFacesMsgInfo("User.nfo.settingsSaved").addToContext();
         } catch (FxApplicationException e) {
             new FxFacesMsgErr("User.err.settingsNotSaved").addToContext();
             new FxFacesMsgErr(e).addToContext();
             return editUserPref();
         }
        FxContext.get()._reloadUserTicket();
        return "editUserPref";
     }
 
     public String saveUser() {
         try {
             String newPasswd = null;
 
             // Determine if the password must be set
             if (password != null && password.trim().length() > 0) {
                 if (!password.equals(passwordConfirm)) {
                     new FxFacesMsgErr("User.err.passwordsDontMatch").addToContext();
                     return "accountEdit";
                 }
                 newPasswd = password;
             }
 
             // Update the user
             accountInterface.update(this.accountIdFiler, newPasswd, null, null, null, this.account.getEmail(),
                     this.account.isValidated(), this.account.isActive(), this.account.getValidFrom(),
                     this.account.getValidTo(), this.account.getLanguage().getId(), this.account.getDescription(),
                     this.account.isAllowMultiLogin(), this.account.getContactData().getId());
             new FxFacesMsgInfo("User.nfo.saved").addToContext();
             // Assign the given groups to the account
             try {
                 accountInterface.setGroupList(this.accountIdFiler, groups);
             } catch (Exception exc) {
                 new FxFacesMsgErr(exc).addToContext();
             }
 
             // Assign the given roles to the account
             try {
                 accountInterface.setRoleList(this.accountIdFiler, getRoles());
             } catch (Exception exc) {
                 new FxFacesMsgErr(exc).addToContext();
             }
             // Reload and display
             return editUser();
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return "accountEdit";
         }
     }
 
     /**
      * Creates a new user from the beans data.
      *
      * @return the next jsf page to render
      */
     public String createUser() {
 
         boolean hasError = false;
 
         // Param check
         if (password == null) {
             password = "";
         }
 
         // Passwords must match
         if (!password.equals(passwordConfirm)) {
             new FxFacesMsgErr("User.err.passwordsDontMatch").addToContext();
             hasError = true;
         }
 
         // Mandator select and check
         final UserTicket ticket = FxContext.get().getTicket();
         long mandatorId = ticket.isGlobalSupervisor() ? account.getMandatorId() : ticket.getMandatorId();
         if (mandatorId < 0) {
             FxFacesMsgErr msg = new FxFacesMsgErr("User.err.mandatorMissing");
             msg.setId("mandator");
             msg.addToContext();
             hasError = true;
         }
 
         // If we have an error abort
         if (hasError) {
             return "accountCreate";
         }
 
         // Create the new account itself
         try {
             setAccountIdFiler(accountInterface.create(account.getName(), account.getLoginName(),
                     password, account.getEmail(),
                     account.getLanguage().getId(), mandatorId,
                     account.isActive(), account.isValidated(),
                     account.getValidFrom(), account.getValidTo(), -1, account.getDescription(), false, true));
             account = accountInterface.load(this.accountIdFiler);
             new FxFacesMsgInfo("User.nfo.saved", account.getName()).addToContext();
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
             return "accountCreate";
         }
 
         // Assign the given groups to the account
         try {
             accountInterface.setGroupList(this.accountIdFiler, groups);
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
 
         // Assign the given roles to the account
         try {
             accountInterface.setRoleList(this.accountIdFiler, getRoles());
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
         listCache.clear();
         resetFilter();
 
         return "accountOverview";
     }
 
 
     /**
      * Retrieves all users, filtering by loginName, userName, email, active, vaidated and mandatorFilter.
      *
      * @return all users matching the filter criterias.
      */
     public ArrayList<Account> getList() {
         final UserTicket ticket = FxContext.get().getTicket();
         try {
             long _mandatorFilter = mandator == null ? -1 : mandator.getId();
 
             // If not supervisor fallback to the own mandator
             if (!ticket.isGlobalSupervisor()) {
                 _mandatorFilter = ticket.getMandatorId();
             }
 
             long[] userGroupIds = new long[1];
             if (groupFilter != -1) {
                 userGroupIds[0] = groupFilter;
             } else {
                 userGroupIds = null;
             }
 
             // Load list if needed, and cache within the request
             String cacheKey = account.getName() + "_" + account.getLoginName() + "_" +
                     account.getEmail() + "_" + isActiveFilter() + "_" + isValidatedFilter() + "_" + _mandatorFilter + "_" +
                     FxArrayUtils.toSeparatedList(userGroupIds, ',');
             ArrayList<Account> result = listCache.get(cacheKey);
             if (result == null) {
                 Account allUsers[] = accountInterface.loadAll(account.getName(), account.getLoginName(),
                         account.getEmail(), isActiveFilter(), isValidatedFilter(), _mandatorFilter,
                         null, userGroupIds, 0, -1);
                 result = new ArrayList<Account>(allUsers.length);
                 result.addAll(Arrays.asList(allUsers));
                 listCache.put(cacheKey, result);
             }
             return result;
 
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
             return new ArrayList<Account>(0);
         }
     }
 
 }
