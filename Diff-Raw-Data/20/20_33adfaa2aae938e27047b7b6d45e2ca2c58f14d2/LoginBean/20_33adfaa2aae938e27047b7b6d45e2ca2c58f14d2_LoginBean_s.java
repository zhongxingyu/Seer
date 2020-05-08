 package gov.nih.nci.evs.reportwriter.bean;
 
 import gov.nih.nci.evs.reportwriter.utils.*;
 import gov.nih.nci.security.*;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.User;
 
 import java.util.*;
 
 import javax.faces.event.*;
 import javax.faces.model.*;
 import javax.naming.*;
 import javax.servlet.http.*;
 
 import org.apache.log4j.*;
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction 
  * with the National Cancer Institute, and so to the extent government 
  * employees are co-authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions 
  * are met:
  *   1. Redistributions of source code must retain the above copyright 
  *      notice, this list of conditions and the disclaimer of Article 3, 
  *      below. Redistributions in binary form must reproduce the above 
  *      copyright notice, this list of conditions and the following 
  *      disclaimer in the documentation and/or other materials provided 
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution, 
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National 
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must 
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not 
  *      authorize the recipient to use any trademarks owned by either NCI 
  *      or NGIT 
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED 
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE 
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * @author EVS Team
  * @version 1.0
  */
 
 public class LoginBean extends Object {
     private static Logger logger = Logger.getLogger(LoginBean.class);
     private static final String APP_NAME = "ncireportwriter";
 
     private String userid;
     private String password;
 
     private long roleGroupId;
     private String selectedTask = null;
     private Boolean isAdmin = null;
 
     private InitialContext ctx = null;
 
     public void setSelectedTask(String selectedTask) {
         this.selectedTask = selectedTask;
         logger.debug("selectedTask: " + this.selectedTask);
     }
 
     public String getUserid() {
         return userid;
     }
 
     public void setUserid(String newUserid) {
         this.userid = newUserid;
     }
 
     public String getPassword() {
         return this.password;
     }
 
     public void setPassword(String newPassword) {
         this.password = newPassword;
     }
 
     public long getRoleGroupId() {
         return this.roleGroupId;
     }
 
     public void setRoleGroupId(long roleGroupId) {
         this.roleGroupId = roleGroupId;
     }
 
     public Boolean hasAdminPrivilege() {
         try {
             AuthorizationManager ami =
                 SecurityServiceProvider.getAuthorizationManager(APP_NAME);
             if (ami == null)
                 return Boolean.FALSE;
 
             ami = (AuthorizationManager) ami;
             User user = ami.getUser(userid);
             Set<?> groups = ami.getGroups(user.getUserId().toString());
             if (null == groups)
                 return Boolean.FALSE;
 
             Iterator<?> iter = groups.iterator();
             while (iter.hasNext()) {
                 Group grp = (Group) iter.next();
                 if (null != grp && grp.getGroupName().startsWith("admin"))
                     return Boolean.TRUE;
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return Boolean.FALSE;
     }
 
     public List<SelectItem> getTaskList() {
         return DataUtils.getTaskList(isAdmin);
     }
 
     public gov.nih.nci.evs.reportwriter.bean.User getUser(String loginName) {
         try {
             SDKClientUtil sdkclientutil = new SDKClientUtil();
             String FQName = "gov.nih.nci.evs.reportwriter.bean.User";
             String methodName = "setLoginName";
             Object obj = sdkclientutil.search(FQName, methodName, loginName);
             if (obj == null)
                 return null;
             gov.nih.nci.evs.reportwriter.bean.User user =
                 (gov.nih.nci.evs.reportwriter.bean.User) obj;
             return user;
         } catch (Exception e) {
             // e.printStackTrace();
         }
         return null;
     }
 
     public String loginAction() {
         try {
             isAdmin = false;
             if (userid.length() <= 0)
                 throw new Exception("Please enter your login ID.");
             if (password.length() <= 0)
                 throw new Exception("Please enter your password.");
 
             AuthenticationManager authenticationManager =
                 SecurityServiceProvider.getAuthenticationManager(APP_NAME);
             if (!authenticationManager.login(userid, password))
                 throw new Exception("Incorrect login credential.");
 
             HttpServletRequest request = SessionUtil.getRequest();
             HttpSession session = request.getSession(); // true
             if (session != null) {
                 session.setAttribute("uid", userid);
                 // session.setAttribute("password", password);
             }
             isAdmin = hasAdminPrivilege();
             session.setAttribute("isAdmin", isAdmin);
 
             gov.nih.nci.evs.reportwriter.bean.User user = getUser(userid);
             if (user == null) {
                 // Synchronize with CSM User table
                 SDKClientUtil sdkclientutil = new SDKClientUtil();
                 gov.nih.nci.evs.reportwriter.bean.User newuser =
                     (gov.nih.nci.evs.reportwriter.bean.User) sdkclientutil
                         .createUser(userid);
                 sdkclientutil.insertUser(newuser);
             }
             session.setAttribute("isSessionValid", Boolean.TRUE);
             SessionUtil.getRequest().removeAttribute("loginWarning");
             return "success";
         } catch (Exception e) {
            logger.debug(StringUtils.SEPARATOR);
            logger.debug("Error logging in: " + userid);
            logger.debug("  * " + e.getClass().getSimpleName() + ": "
                + e.getMessage());
            SessionUtil.getRequest().setAttribute("loginWarning",
                e.getMessage());
             return "failure";
         }
     }
 
     public void changeTaskSelection(ValueChangeEvent vce) {
         String newValue = (String) vce.getNewValue();
         setSelectedTask(newValue);
     }
 
     public Object getService(String serviceBeanName)
             throws javax.naming.NamingException {
         return ctx.lookup(serviceBeanName);
     }
 
     public String logoutAction() {
         return logout();
     }
 
     public String logout() {
         HttpSession session = SessionUtil.getSession();
         if (session != null)
             session.invalidate();
         return "logout";
     }
 }
