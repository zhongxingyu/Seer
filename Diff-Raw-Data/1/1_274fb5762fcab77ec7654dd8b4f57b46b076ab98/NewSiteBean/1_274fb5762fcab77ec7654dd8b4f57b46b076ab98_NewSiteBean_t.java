 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/tfd/trunk/sdata/sdata-tool/impl/src/java/org/sakaiproject/sdata/tool/JCRDumper.java $
  * $Id: JCRDumper.java 45207 2008-02-01 19:01:06Z ian@caret.cam.ac.uk $
  ***********************************************************************************
  *
  * Copyright (c) 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sdata.services.newsite;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.authz.api.AuthzGroup;
 import org.sakaiproject.authz.api.AuthzGroupService;
 import org.sakaiproject.authz.api.Role;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.event.api.EventTrackingService;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.sdata.tool.api.ServiceDefinition;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SitePage;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.site.api.ToolConfiguration;
 import org.sakaiproject.site.api.SiteService.SelectionType;
 import org.sakaiproject.site.api.SiteService.SortType;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.Tool;
 
 /**
  * TODO Javadoc
  * 
  * @author
  */
 public class NewSiteBean implements ServiceDefinition {
 
 	private List<Site> mysites;
 
 	private Session currentSession;
 
 	private List<Map> MyMappedSites = new ArrayList<Map>();
 
 	private Map<String, Object> map2 = new HashMap<String, Object>();;
 
 	private static final Log log = LogFactory.getLog(NewSiteBean.class);
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param sessionManager
 	 * @param siteService
 	 */
 	public NewSiteBean(SessionManager sessionManager, SiteService siteService,
 			AuthzGroupService authzGroupService, String siteId, String name,
 			String description, boolean writeevent, HttpServletRequest request,
 			HttpServletResponse response) {
 		boolean siteExists = true;
 		String status = "900";
 		ArrayList<HashMap<String, Object>> arlpages = new ArrayList<HashMap<String, Object>>();
 
 		String curUser = sessionManager.getCurrentSessionUserId();
 
 		try {
 
 			Site site = siteService.addSite(getPassword(8), "project");
 			site.setTitle(name);
 			site.setDescription(description);
			siteService.save(site);
 
 			map2.put("status", "success");
 			map2.put("id", site.getId());
 
 		} catch (Exception ex) {
 			map2.put("status", "failed");
 			map2.put("message", ex.getMessage());
 		}
 
 	}
 
 	public static String getPassword(int n) {
 		char[] pw = new char[n];
 		int c = 'A';
 		int r1 = 0;
 		for (int i = 0; i < n; i++) {
 			r1 = (int) (Math.random() * 3);
 			switch (r1) {
 			case 0:
 				c = '0' + (int) (Math.random() * 10);
 				break;
 			case 1:
 				c = 'a' + (int) (Math.random() * 26);
 				break;
 			case 2:
 				c = 'A' + (int) (Math.random() * 26);
 				break;
 			}
 			pw[i] = (char) c;
 		}
 		return new String(pw);
 	}
 
 	protected class SDataSiteRole {
 
 		private String id;
 
 		private String description;
 
 		public void setId(String id) {
 			this.id = id;
 		}
 
 		public String getId() {
 			return id;
 		}
 
 		public void setDescription(String description) {
 			this.description = description;
 		}
 
 		public String getDescription() {
 			return description;
 		}
 
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param mysites
 	 */
 	public void setMysites(List<Site> mysites) {
 		this.mysites = mysites;
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @return
 	 */
 	public List<Site> getMysites() {
 		return mysites;
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param currentSession
 	 */
 	public void setCurrentSession(Session currentSession) {
 		this.currentSession = currentSession;
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @return
 	 */
 	public Session getCurrentSession() {
 		return currentSession;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.ServiceDefinition#getResponseMap()
 	 */
 	public Map<String, Object> getResponseMap() {
 
 		return map2;
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @param myMappedSites
 	 */
 	public void setMyMappedSites(List<Map> myMappedSites) {
 		MyMappedSites = myMappedSites;
 	}
 
 	/**
 	 * TODO Javadoc
 	 * 
 	 * @return
 	 */
 	public List<Map> getMyMappedSites() {
 		return MyMappedSites;
 	}
 
 }
