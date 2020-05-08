 /*
  * Copyright (c) 2003, 2004 Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: Apr 19, 2003 / 9:13:16 PM
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.view.admin;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.jforum.Command;
 import net.jforum.JForum;
 import net.jforum.entities.Group;
 import net.jforum.entities.User;
 import net.jforum.model.DataAccessDriver;
 import net.jforum.model.GroupModel;
 import net.jforum.model.UserModel;
 import net.jforum.model.security.UserSecurityModel;
 import net.jforum.repository.SecurityRepository;
 import net.jforum.security.PermissionControl;
 import net.jforum.security.XMLPermissionControl;
 import net.jforum.util.I18n;
 import net.jforum.util.TreeGroup;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 import net.jforum.view.forum.ViewCommon;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 
 /**
  * @author Rafael Steil
 * @version $Id: UserAction.java,v 1.2 2004/09/22 16:37:14 jamesyong Exp $
  */
 public class UserAction extends Command 
 {
 	// Listing
 	public void list() throws Exception
 	{
 		String s = JForum.getRequest().getParameter("start");
 		int start = 0;
 		
 		if (s == null || s.equals("")) {
 			start = 0;
 		}
 		else {
 			start = Integer.parseInt(s);
 			
 			if (start < 0) {
 				start = 0;
 			}
 		}
 		
 		int usersPerPage = SystemGlobals.getIntValue(ConfigKeys.USERS_PER_PAGE);
 		int totalUsers = DataAccessDriver.getInstance().newUserModel().getTotalUsers();
 
 		JForum.getContext().put("users", DataAccessDriver.getInstance().newUserModel().selectAll(start ,usersPerPage));
 		JForum.getContext().put("moduleAction", "user_list.htm");
 		
 		// Pagination
 		JForum.getContext().put("totalPages", new Double(Math.floor(totalUsers / usersPerPage)));
 		JForum.getContext().put("recordsPerPage", new Integer(usersPerPage));
 		JForum.getContext().put("totalRecords", new Integer(totalUsers));
 		JForum.getContext().put("thisPage", new Integer(start));
 	}
 	
 	// Permissions
 	public void permissions() throws Exception
 	{
 		int id = Integer.parseInt(JForum.getRequest().getParameter("id"));
 		
 		User user = DataAccessDriver.getInstance().newUserModel().selectById(id);
 		
 		UserSecurityModel umodel = DataAccessDriver.getInstance().newUserSecurityModel();
 		PermissionControl pc = new PermissionControl();
 		pc.setSecurityModel(umodel);
 		pc.setRoles(umodel.loadRoles(user));
 		
 		List sections = new XMLPermissionControl(pc).loadConfigurations(SystemGlobals.getApplicationResourceDir() +"/config/permissions.xml");
 		
 		JForum.getContext().put("sections", sections);
 		JForum.getContext().put("user", user);
 		JForum.getContext().put("moduleAction", "user_security_form.htm");
 	}
 	
 	// Permissions Save
 	public void permissionsSave() throws Exception
 	{
 		int id = Integer.parseInt(JForum.getRequest().getParameter("id"));
 		User user = DataAccessDriver.getInstance().newUserModel().selectById(id);
 		
 		UserSecurityModel umodel = DataAccessDriver.getInstance().newUserSecurityModel();
 		PermissionControl pc = new PermissionControl();
 		pc.setSecurityModel(umodel);
 
 		new PermissionProcessHelper(pc, user.getId()).processData();
 		
 		// Reload it
 		pc.setRoles(umodel.loadRoles(user));
 
 		// Update Security Repository
 		SecurityRepository.remove(user.getId());
 		SecurityRepository.add(user.getId(), pc);
 		
 		this.list();
 	}
 	
 	public void edit() throws Exception
 	{
 		int userId = Integer.parseInt(JForum.getRequest().getParameter("id"));	
 		UserModel um = DataAccessDriver.getInstance().newUserModel();
 		User u = um.selectById(userId);
 		
 		JForum.getContext().put("u", u);
 		JForum.getContext().put("action", "editSave");		
 		JForum.getContext().put("moduleAction", "user_form.htm");
 		JForum.getContext().put("admin", true);
 	}
 	
 	public void editSave() throws Exception
 	{
 		int userId = Integer.parseInt(JForum.getRequest().getParameter("user_id"));
 		ViewCommon.saveUser(userId);
 
 		this.list();
 	}
 	
 	// Groups
 	public void groups() throws Exception
 	{
 		int userId = Integer.parseInt(JForum.getRequest().getParameter("id"));
 		
 		UserModel um = DataAccessDriver.getInstance().newUserModel();
 		GroupModel gm = DataAccessDriver.getInstance().newGroupModel();
 		
 		User u = um.selectById(userId);
 		
 		ArrayList selectedList = new ArrayList();
 		for (Iterator iter = u.getGroupsList().iterator(); iter.hasNext(); ) {
 			selectedList.add(new Integer(((Group)iter.next()).getId()));
 		}
 		
 		JForum.getContext().put("selectedList", selectedList);
 		JForum.getContext().put("groups", new TreeGroup().getNodes());
 		JForum.getContext().put("user", u);
 		JForum.getContext().put("userId", new Integer(userId));
 		JForum.getContext().put("moduleAction", "user_groups.htm");
 		JForum.getContext().put("groupFor", I18n.getMessage("User.GroupsFor", new String[] { u.getUsername() }));
 	}
 	
 	// Groups Save
 	public void groupsSave() throws Exception
 	{
 		int userId = Integer.parseInt(JForum.getRequest().getParameter("user_id"));
 		
 		UserModel um = DataAccessDriver.getInstance().newUserModel();
 		GroupModel gm = DataAccessDriver.getInstance().newGroupModel();
 		
 		// Remove the old groups
 		ArrayList allGroupsList = gm.selectAll();
 		int[] allGroups = new int[allGroupsList.size()];
 		
 		int counter = 0;
 		for (Iterator iter = allGroupsList.iterator(); iter.hasNext(); counter++) {
 			Group g = (Group)iter.next();
 			
 			allGroups[counter] = g.getId();
 		}
 		
 		um.removeFromGroup(userId, allGroups);
 		
 		// Associate the user to the selected groups
 		String[] selectedGroups = JForum.getRequest().getParameterValues("groups");
 		int[] newGroups = new int[selectedGroups.length];
 		
 		for (int i = 0; i < selectedGroups.length; i++) {
 			newGroups[i] = Integer.parseInt(selectedGroups[i]);
 		}
 		
 		um.addToGroup(userId, newGroups);
 		
 		this.list();
 	}
 	
 	/* 
 	 * @see net.jforum.Command#process()
 	 */
 	public Template process() throws Exception 
 	{
 		if (AdminAction.isAdmin()) {
 			super.process();
 		}
 		
 		return Configuration.getDefaultConfiguration().getTemplate("admin/index_main.htm");
 	}
 
 }
