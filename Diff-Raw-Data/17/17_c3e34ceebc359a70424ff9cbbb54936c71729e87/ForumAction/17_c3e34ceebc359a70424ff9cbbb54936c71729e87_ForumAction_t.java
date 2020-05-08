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
  * This file creation date: Mar 28, 2003 / 8:21:56 PM
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.view.admin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.jforum.dao.CategoryDAO;
 import net.jforum.dao.DataAccessDriver;
 import net.jforum.dao.ForumDAO;
 import net.jforum.dao.TopicDAO;
 import net.jforum.dao.security.GroupSecurityDAO;
 import net.jforum.entities.Category;
 import net.jforum.entities.Forum;
 import net.jforum.repository.ForumRepository;
 import net.jforum.repository.SecurityRepository;
import net.jforum.repository.RolesRepository;
 import net.jforum.security.PermissionControl;
 import net.jforum.security.Role;
 import net.jforum.security.RoleValue;
 import net.jforum.security.RoleValueCollection;
 import net.jforum.security.SecurityConstants;
 import net.jforum.util.TreeGroup;
 import net.jforum.util.preferences.TemplateKeys;
 import net.jforum.view.admin.common.ModerationCommon;
 
 /**
  * @author Rafael Steil
 * @version $Id: ForumAction.java,v 1.24 2005/09/15 22:47:32 vmal Exp $
  */
 public class ForumAction extends AdminCommand 
 {
 	// Listing
 	public void list() throws Exception
 	{
 		this.context.put("categories", DataAccessDriver.getInstance().newCategoryDAO().selectAll());
 		this.context.put("repository", new ForumRepository());
 		this.setTemplateName(TemplateKeys.FORUM_ADMIN_LIST);
 	}
 	
 	// One more, one more
 	public void insert() throws Exception
 	{
 		CategoryDAO cm = DataAccessDriver.getInstance().newCategoryDAO();
 		
 		this.context.put("groups", new TreeGroup().getNodes());
 		this.context.put("selectedList", new ArrayList());
 		this.setTemplateName(TemplateKeys.FORUM_ADMIN_INSERT);
 		this.context.put("categories",cm.selectAll());
 		this.context.put("action", "insertSave");		
 	}
 	
 	// Edit
 	public void edit() throws Exception
 	{
 		CategoryDAO cm = DataAccessDriver.getInstance().newCategoryDAO();
 		
 		this.context.put("forum", DataAccessDriver.getInstance().newForumDAO().selectById(
 				this.request.getIntParameter("forum_id")));
 		this.context.put("categories", cm.selectAll());
 		this.setTemplateName(TemplateKeys.FORUM_ADMIN_EDIT);
 		this.context.put("action", "editSave");
 	}
 	
 	//  Save information
 	public void editSave() throws Exception
 	{
 		Forum f = new Forum(ForumRepository.getForum(this.request.getIntParameter("forum_id")));
 		boolean moderated = f.isModerated();
 		int categoryId = f.getCategoryId();
 		
 		f.setDescription(this.request.getParameter("description"));
 		f.setIdCategories(this.request.getIntParameter("categories_id"));
 		f.setName(this.request.getParameter("forum_name"));
 		f.setModerated("1".equals(this.request.getParameter("moderate")));
 
 		DataAccessDriver.getInstance().newForumDAO().update(f);
 
 		if (moderated != f.isModerated()) {
 			new ModerationCommon().setTopicModerationStatus(f.getId(), f.isModerated());
 		}
 		
 		if (categoryId != f.getCategoryId()) {
 			f.setIdCategories(categoryId);
 			ForumRepository.removeForum(f);
 			
 			f.setIdCategories(this.request.getIntParameter("categories_id"));
 			ForumRepository.addForum(f);
 		}
 		else {
 			ForumRepository.reloadForum(f.getId());
 		}
 		
 		this.list();
 	}
 	
 	public void up() throws Exception
 	{
 		this.processOrdering(true);
 	}
 	
 	public void down() throws Exception
 	{
 		this.processOrdering(false);
 	}
 	
 	private void processOrdering(boolean up) throws Exception
 	{
 		Forum toChange = new Forum(ForumRepository.getForum(Integer.parseInt(
 				this.request.getParameter("forum_id"))));
 		
 		Category category = ForumRepository.getCategory(toChange.getCategoryId());
 		List forums = new ArrayList(category.getForums());
 		int index = forums.indexOf(toChange);
 		
 		if (index == -1 || (up && index == 0) || (!up && index + 1 == forums.size())) {
 			this.list();
 			return;
 		}
 		
 		ForumDAO fm = DataAccessDriver.getInstance().newForumDAO();
 		
 		if (up) {
 			// Get the forum which comes *before* the forum we're changing
 			Forum otherForum = new Forum((Forum)forums.get(index - 1));
 			fm.setOrderUp(toChange, otherForum);
 		}
 		else {
 			// Get the forum which comes *after* the forum we're changing
 			Forum otherForum = new Forum((Forum)forums.get(index + 1));
 			fm.setOrderDown(toChange, otherForum);
 		}
 		
 		category.changeForumOrder(toChange);
 		ForumRepository.refreshCategory(category);
 		
 		this.list();
 	}
 	
 	// Delete
 	public void delete() throws Exception
 	{
 		String ids[] = this.request.getParameterValues("forum_id");
 		
 		ForumDAO fm = DataAccessDriver.getInstance().newForumDAO();
 		TopicDAO tm = DataAccessDriver.getInstance().newTopicDAO();
 		
 		if (ids != null) {
 			for (int i = 0; i < ids.length; i++) {
 				int forumId = Integer.parseInt(ids[i]);
 
 				tm.deleteByForum(forumId);
 				fm.delete(forumId);
 				
 				Forum f = new Forum(ForumRepository.getForum(forumId));
 				ForumRepository.removeForum(f);
 			}
 		}
 		
 		this.list();
 	}
 	
 	// A new one
 	public void insertSave() throws Exception
 	{
 		Forum f = new Forum();
 		f.setDescription(this.request.getParameter("description"));
 		f.setIdCategories(this.request.getIntParameter("categories_id"));
 		f.setName(this.request.getParameter("forum_name"));	
 		f.setModerated("1".equals(this.request.getParameter("moderate")));
 			
 		int forumId = DataAccessDriver.getInstance().newForumDAO().addNew(f);
 		f.setId(forumId);
 		
 		ForumRepository.addForum(f);
 		
 		GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
 		PermissionControl pc = new PermissionControl();
 		pc.setSecurityModel(gmodel);
 		
 		String[] allGroups = this.request.getParameterValues("groups");
 		
 		// Access
 		String[] groups = this.request.getParameterValues("groupsAccess");
 		if (groups != null) {
 			this.addRole(pc, SecurityConstants.PERM_FORUM, f.getId(), groups, false);
 		}
 		else {
 			this.addRole(pc, SecurityConstants.PERM_FORUM, f.getId(), allGroups, true);
 		}
 		
 		// Anonymous posts
 		groups = this.request.getParameterValues("groupsAnonymous");
 		if (groups != null) {
 			this.addRole(pc, SecurityConstants.PERM_ANONYMOUS_POST, f.getId(), groups, false);
 		}
 		else {
 			this.addRole(pc, SecurityConstants.PERM_ANONYMOUS_POST, f.getId(), allGroups, true);
 		}
 		
 		// Read-only
 		groups = this.request.getParameterValues("groupsReadOnly");
 		if (groups != null) {
 			this.addRole(pc, SecurityConstants.PERM_READ_ONLY_FORUMS, f.getId(), groups, false);
 		}
 		else {
 			this.addRole(pc, SecurityConstants.PERM_READ_ONLY_FORUMS, f.getId(), allGroups, true);
 		}
 		
 		// Reply-only
 		this.addRole(pc, SecurityConstants.PERM_REPLY_ONLY, f.getId(), allGroups, true);
 		
 		// HTML
 		groups = this.request.getParameterValues("groupsHtml");
 		if (groups != null) {
 			this.addRole(pc, SecurityConstants.PERM_HTML_DISABLED, f.getId(), groups, false);
 		}
 		else {
 			this.addRole(pc, SecurityConstants.PERM_HTML_DISABLED, f.getId(), allGroups, true);
 		}
 		
 		SecurityRepository.clean();
		RolesRepository.clear();
 
 		this.list();
 	}
 	
 	private void addRole(PermissionControl pc, String roleName, int forumId, String[] groups, boolean allow) throws Exception
 	{
 		Role role = new Role();
 		role.setName(roleName);
 		
 		for (int i = 0; i < groups.length; i++) {
 			int groupId = Integer.parseInt(groups[i]);
 			RoleValueCollection roleValues = new RoleValueCollection();
 			
 			RoleValue rv = new RoleValue();
 			rv.setType(allow ? PermissionControl.ROLE_ALLOW : PermissionControl.ROLE_DENY);
 			rv.setValue(Integer.toString(forumId));
 			roleValues.add(rv);
 			
 			pc.addRoleValue(groupId, role, roleValues);
 		}
 	}
 }
