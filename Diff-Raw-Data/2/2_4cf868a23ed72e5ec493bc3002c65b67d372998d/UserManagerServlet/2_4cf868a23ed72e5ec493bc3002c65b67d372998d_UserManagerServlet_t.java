 package org.levi.web;
 
 
 import org.levi.engine.db.DBManager;
 import org.levi.engine.identity.Group;
 import org.levi.engine.identity.User;
 import org.levi.engine.impl.identity.GroupImpl;
 import org.levi.engine.impl.identity.UserImpl;
 import org.levi.engine.persistence.hibernate.user.hobj.GroupBean;
 import org.levi.engine.persistence.hibernate.user.hobj.UserBean;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: umashanthi
  * Date: 6/9/11
  * Time: 1:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class UserManagerServlet extends HttpServlet {
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doGet(request, response);
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // Retrieve the action
         String action = request.getParameter("action");
         assert request.getSession().getAttribute("dbManager") != null;
         DBManager dbManager = (DBManager) request.getSession().getAttribute("dbManager");
         if (action == null) {         // list users and groups -- default action
             List<UserBean> userBeanList = dbManager.getUserList();
             List<GroupBean> groupBeanList = dbManager.getGroupList();
             request.setAttribute("usersList", userBeanList);
             request.setAttribute("groupList", groupBeanList);
             //response.sendRedirect("usermanagement.jsp");
             request.getRequestDispatcher("usermanagement.jsp").forward(request, response);
         } else if (action.equals("addgroup")) {  // add group
             String groupName = request.getParameter("groupname");
             String description = request.getParameter("description");
             Group group = new GroupImpl();
             group.setGroupId(groupName);
             group.setGroupName(groupName);
             group.setGroupDescription(description);
             dbManager.saveGroup(group);
             response.sendRedirect("usrmng");
 
         } else if (action.equals("adduser")) {    // add user
             String username = request.getParameter("username");
             String password = request.getParameter("password");
             User user = new UserImpl();
             user.setUserId(username);
             user.setPassword(password);
             user.setUserGroups(null);
             dbManager.saveUser(user);
             // retrieve selected groups for this user
             // get the group name lists, get the request parameter for checkbox & radio, , get groups, and add groups to the user bean
             List<String> groupIdList = dbManager.getGroupIdList();
             for (String grpId : groupIdList) {
                 if (request.getParameter(grpId) != null) {
                     dbManager.addUserToGroup(username, grpId);
                 } else {
                     dbManager.removeUserFromGroup(username, grpId);
                 }
             }
 
             response.sendRedirect("usrmng");
         } else if (action.equals("editUser")) {
             String username = request.getParameter("username");
             // retrieve user details from user bean
             UserBean userBean = dbManager.getUser(username);
             assert userBean != null;
             request.setAttribute("user", userBean);
             request.getRequestDispatcher("edituser.jsp").forward(request, response); // TODO: Check whether this works
         } else if (action.equals("updateUser")) {
             String username = request.getParameter("username");
             String password = request.getParameter("password");
             User user = new UserImpl();
             user.setUserId(username);
             user.setPassword(password);
             dbManager.saveUser(user);
             List<String> groupIdList = dbManager.getGroupIdList();
             for (String grpId : groupIdList) {
                 if (request.getParameter(grpId) != null) {
                     dbManager.addUserToGroup(username, grpId);
                 } else {
                     dbManager.removeUserFromGroup(username, grpId);
                 }
             }
             /* */
            UserBean userBean = dbManager.getUser(request.getSession().getAttribute("username").toString());
             String userGroupsString = "";
             if (userBean.getUserGroups() != null && userBean.getUserGroups().size() > 0) {
                 for (GroupBean grp : userBean.getUserGroups()) {
                     userGroupsString += grp.getGroupName() + " , ";
                 }
             }
             request.getSession().setAttribute("userGroupList", userGroupsString.substring(0, userGroupsString.length() - 2));
 
             /* */
             response.sendRedirect("usrmng");
         }
     }
 }
