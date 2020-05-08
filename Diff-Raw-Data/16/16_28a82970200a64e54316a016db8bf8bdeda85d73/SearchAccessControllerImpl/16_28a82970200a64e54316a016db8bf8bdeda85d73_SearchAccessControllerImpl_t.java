 /*
  * regain/ACL - Implement SearchAccessController/CrawlerAccessController for regain (User/Group via URL) 
  * Copyright (C) 2012  Come_IN Computerclubs (University of Siegen)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * Contact: Come_IN-Team <come_in-team@listserv.uni-siegen.de>
  */
 
 package de.uni_siegen.wineme.come_in.acl;
 
 import java.util.Properties;
 
 import net.sf.regain.RegainException;
 import net.sf.regain.search.access.SearchAccessController;
 import net.sf.regain.util.sharedtag.PageRequest;
 
 /**
  * Get the groups the user can access.
  *
  * 
  */
 public class SearchAccessControllerImpl extends AccessControllerImpl implements SearchAccessController {
 
 	/**
 	 * Initializes the SearchAccessController.
 	 * <p>
 	 * This method is called once right after the SearchAccessController
 	 * instance was created.
 	 * 
 	 * @param config
 	 *            The configuration.
 	 * 
 	 * @throws RegainException
 	 *             If loading the config failed.
 	 */
 	@Override
 	public void init(Properties config) throws RegainException {
 		super.init(config);
 	}
 
 	/**
 	 * Gets the groups the current user has reading rights for.
 	 * <p>
 	 * Note: The group array must not be <code>null</code> and the group names
 	 * must not contain whitespace.
      * <p>
      * Note: For backwards compability, when this method returns null, ALL groups
      * are allowed. Return a non-existing group to restrict access to nothing.
 	 * 
 	 * @param request
 	 *            The page request to use for identifying the user.
 	 * @return The groups of the current user.
 	 * 
 	 * @throws RegainException
 	 *             If getting the groups failed.
 	 */
 	@Override
 	public String[] getUserGroups(PageRequest request) throws RegainException {
		String paramIsAdmin = request.getParameter("isadmin");
		if (paramIsAdmin != null)
			return new String[] {};
		
 		String groups = defaultGroups;
 		String groupsParam = request.getParameter("groups");
 		if (groupsParam != null)
 			groups = groups + groupsParam;
 
 		String[] groupArr = groupSplit(groups);
		if (groupArr.length == 0)
 			groupArr = new String[] {"some-not-existing-group"};
 		return groupArr;
 	}
 }
