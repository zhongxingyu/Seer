 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.servlets;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.RoleMap;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class RolesServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(RolesServlet.class.getName());
 	/** The name of the JSP file used to render the servlet output when searching. */
 	protected static final String JSP_ADMIN_ROLES = "admin-roles.jsp";
 
 	/**
 	 * This method handles the request after its parent class receives control.
 	 *
 	 * @param request - Standard HttpServletRequest object.
 	 * @param response - Standard HttpServletResponse object.
 	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String function = request.getParameter("function");
 		if (StringUtils.isBlank(function)) {
 			view(request, next, pageInfo);
 		} else if (function.equals("modifyRole")) {
 			modifyRole(request, next, pageInfo);
 		} else if (function.equals("searchRole")) {
 			searchRole(request, next, pageInfo);
 		} else if (function.equals("assignRole")) {
 			assignRole(request, next, pageInfo);
 		}
 		return next;
 	}
 	
 	/**
 	 * Utility method for converting a processing an array of "userid|groupid|role" values
 	 * into a List of roles for a specific id value.
 	 *
 	 * @return A List of role names for the given id, or an empty
 	 *  List if no matching values are found.
 	 */
 	private static List<String> buildRoleArray(int userId, int groupId, String[] valueArray) {
 		List<String> results = new ArrayList<String>();
 		if (valueArray == null) {
 			return results;
 		}
 		for (int i = 0; i < valueArray.length; i++) {
 			String[] tokens = valueArray[i].split("\\|");
 			String parsedUserId = tokens[0];
 			int userInt = -1;
 			try {
 				userInt = Integer.parseInt(parsedUserId);
 			} catch (Exception ignore) {}
 			String parsedGroupId = tokens[1];
 			int groupInt = -1;
 			try {
 				groupInt = Integer.parseInt(parsedGroupId);
 			} catch (Exception ignore) {}
 			String valueRole = tokens[2];
 			if ((userId > 0 && userId == userInt) || (groupId > 0 && groupId == groupInt)) {
 				results.add(valueRole);
 			}
 		}
 		return results;
 	}
 
 	/**
 	 *
 	 */
 	private void assignRole(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		// the way this works is that there will be an array of candidate
 		// groups - these are all groups that could have been modified.  there
 		// will also be a groupRole array containing values of the form
 		// "userid|groupid|role".  process both, deleting all old roles for the
 		// candidate group array and adding the new roles in the groupRole
 		// array.
 		ArrayList<WikiMessage> errors = new ArrayList<WikiMessage>();
 		try {
 			String[] candidateGroups = request.getParameterValues("candidateGroup");
 			String[] groupRoles = request.getParameterValues("groupRole");
 			if (candidateGroups != null) {
 				for (int i = 0; i < candidateGroups.length; i++) {
 					int groupId = Integer.parseInt(candidateGroups[i]);
 					List<String> roles = buildRoleArray(-1, groupId, groupRoles);
 					WikiBase.getDataHandler().writeRoleMapGroup(groupId, roles);
 				}
 				next.addObject("message", new WikiMessage("roles.message.grouproleupdate"));
 			}
 			// now do the same for user roles.
 			String[] candidateUsers = request.getParameterValues("candidateUser");
 			String[] candidateUsernames = request.getParameterValues("candidateUsername");
 			String[] userRoles = request.getParameterValues("userRole");
 			if (candidateUsers != null) {
 				for (int i = 0; i < candidateUsers.length; i++) {
 					int userId = Integer.parseInt(candidateUsers[i]);
 					String username = candidateUsernames[i];
 					List<String> roles = buildRoleArray(userId, -1, userRoles);
					if (userId == ServletUtil.currentWikiUser().getUserId() && !roles.contains(Role.ROLE_SYSADMIN.getAuthority())) {
 						errors.add(new WikiMessage("roles.message.sysadminremove"));
 						roles.add(Role.ROLE_SYSADMIN.getAuthority());
 					}
 					WikiBase.getDataHandler().writeRoleMapUser(username, roles);
 				}
 				next.addObject("message", new WikiMessage("roles.message.userroleupdate"));
 			}
 		} catch (WikiException e) {
 			errors.add(e.getWikiMessage());
 		} catch (Exception e) {
 			logger.severe("Failure while adding role", e);
 			errors.add(new WikiMessage("roles.message.rolefail", e.getMessage()));
 		}
 		if (!errors.isEmpty()) {
 			next.addObject("errors", errors);
 		}
 		this.view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void modifyRole(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String updateRole = request.getParameter("updateRole");
 		Role role = null;
 		if (!StringUtils.isBlank(request.getParameter("Submit"))) {
 			try {
 				// once created a role name cannot be modified, so the text field
 				// will be disabled in the form.
 				boolean update = StringUtils.isBlank(request.getParameter("roleName"));
 				String roleName = (update) ? updateRole : request.getParameter("roleName");
 				role = new Role(roleName);
 				role.setDescription(request.getParameter("roleDescription"));
 				WikiUtil.validateRole(role);
 				WikiBase.getDataHandler().writeRole(role, update);
 				if (!StringUtils.isBlank(updateRole) && updateRole.equals(role.getAuthority())) {
 					next.addObject("message", new WikiMessage("roles.message.roleupdated", role.getAuthority()));
 				} else {
 					next.addObject("message", new WikiMessage("roles.message.roleadded", role.getAuthority()));
 				}
 			} catch (WikiException e) {
 				next.addObject("message", e.getWikiMessage());
 			} catch (Exception e) {
 				logger.severe("Failure while adding role", e);
 				next.addObject("message", new WikiMessage("roles.message.rolefail", e.getMessage()));
 			}
 		} else if (!StringUtils.isBlank(updateRole)) {
 			// FIXME - use a cached list of roles instead of iterating
 			// load details for the selected role
 			List<Role> roles = WikiBase.getDataHandler().getAllRoles();
 			for (Role tempRole : roles) {
 				if (tempRole.getAuthority().equals(updateRole)) {
 					role = tempRole;
 				}
 			}
 		}
 		if (role != null) {
 			next.addObject("roleName", role.getAuthority());
 			next.addObject("roleDescription", role.getDescription());
 		}
 		this.view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void searchRole(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		try {
 			String searchLogin = request.getParameter("searchLogin");
 			List<RoleMap> roleMapUsers = null;
 			if (!StringUtils.isBlank(searchLogin)) {
 				roleMapUsers = WikiBase.getDataHandler().getRoleMapByLogin(searchLogin);
 				next.addObject("searchLogin", searchLogin);
 			} else {
 				String searchRole = request.getParameter("searchRole");
 				roleMapUsers = WikiBase.getDataHandler().getRoleMapByRole(searchRole);
 				next.addObject("searchRole", searchRole);
 			}
 			next.addObject("roleMapUsers", roleMapUsers);
 		} catch (Exception e) {
 			logger.severe("Failure while retrieving role", e);
 			next.addObject("message", new WikiMessage("roles.message.rolesearchfail", e.getMessage()));
 		}
 		this.view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		List<Role> roles = WikiBase.getDataHandler().getAllRoles();
 		next.addObject("roles", roles);
 		next.addObject("roleCount", roles.size());
 		List<RoleMap> roleMapGroups = WikiBase.getDataHandler().getRoleMapGroups();
 		next.addObject("roleMapGroups", roleMapGroups);
 		pageInfo.setAdmin(true);
 		pageInfo.setContentJsp(JSP_ADMIN_ROLES);
 		pageInfo.setPageTitle(new WikiMessage("roles.title"));
 	}
 }
