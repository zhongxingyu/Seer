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
 
 package org.sakaiproject.sdata.services.site;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.Kernel;
 import org.sakaiproject.authz.api.AuthzGroup;
 import org.sakaiproject.authz.api.AuthzGroupService;
 import org.sakaiproject.authz.api.Role;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.EntityManager;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.event.api.EventTrackingService;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.sdata.tool.AbstractHandler;
 import org.sakaiproject.sdata.tool.ResourceFunctionFactoryImpl;
 import org.sakaiproject.sdata.tool.api.HandlerSerialzer;
 import org.sakaiproject.sdata.tool.api.ResourceDefinition;
 import org.sakaiproject.sdata.tool.api.ResourceDefinitionFactory;
 import org.sakaiproject.sdata.tool.api.ResourceFunctionFactory;
 import org.sakaiproject.sdata.tool.api.SDataException;
 import org.sakaiproject.sdata.tool.api.SDataFunction;
 import org.sakaiproject.sdata.tool.json.JsonHandlerSerializer;
 import org.sakaiproject.sdata.tool.util.NullSecurityAssertion;
 import org.sakaiproject.sdata.tool.util.ResourceDefinitionFactoryImpl;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SitePage;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.site.api.ToolConfiguration;
 import org.sakaiproject.site.api.SiteService.SelectionType;
 import org.sakaiproject.site.api.SiteService.SortType;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.Tool;
 import org.sakaiproject.tool.api.ToolManager;
 
 /**
  * Handles calls for site related data
  */
 public class SiteHandler extends AbstractHandler {
 	private SiteService siteService;
 	private ResourceDefinitionFactory resourceDefinitionFactory;
 	private ResourceFunctionFactory resourceFunctionFactory;
 	private HandlerSerialzer serializer;
 
 	private List<Site> mySites;
 	private Session currentSession;
 	private static final Log log = LogFactory.getLog(SiteBean.class);
 
 	private SessionManager sessionManager;
 	private AuthzGroupService authzGroupService;
 	private ToolManager toolManager;
 	private EntityManager entityManager;
 
 	public void init(Map<String, String> config) throws ServletException {
 		siteService = Kernel.siteService();
 		String basePath = config.get("basepath");
 		String baseUrl = config.get("baseurl");
 		resourceDefinitionFactory = new ResourceDefinitionFactoryImpl(config,
 				baseUrl, basePath, new NullSecurityAssertion());
 		resourceFunctionFactory = new ResourceFunctionFactoryImpl(config);
 		serializer = new JsonHandlerSerializer();
 		sessionManager = Kernel.sessionManager();
 		authzGroupService = Kernel.authzGroupService();
 		toolManager = (ToolManager) Kernel.componentManager().get(
 				ToolManager.class.getName());
 		entityManager = Kernel.entityManager();
 	}
 
 	@Override
 	public HandlerSerialzer getSerializer() {
 		return serializer;
 	}
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		try {
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			SDataFunction m = resourceFunctionFactory.getFunction(rp
 					.getFunctionDefinition());
			String reference = rp.getRepositoryPath();
			log.info("Site Handler Reference = "+reference);
			if ( "/site".equals(reference) ) {
				reference = reference+"/"+request.getParameter("siteid");
			}
			Reference ref = entityManager.newReference(reference);
 			Site site = (Site) ref.getEntity();
			
			
 
 			Map<String, Object> out = null;
 			if (site != null && m != null) {
 				m.call(this, request, response, site, rp);
 			} else {
 				out = siteInfo(site, request, response);
 			}
 			if (out != null) {
 				sendMap(request, response, out);
 			}
 		} catch (Exception ex) {
 			sendError(request, response, ex);
 		}
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
		try {
			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
			SDataFunction m = resourceFunctionFactory.getFunction(rp
					.getFunctionDefinition());
			Reference ref = entityManager.newReference(rp.getRepositoryPath());
			Site site = (Site) ref.getEntity();

			Map<String, Object> out = null;
			if (site != null && m != null) {
				m.call(this, request, response, site, rp);
			} else {
				throw new SDataException(HttpServletResponse.SC_BAD_REQUEST,"No function found");
			}
			if (out != null) {
				sendMap(request, response, out);
			}
		} catch (Exception ex) {
			sendError(request, response, ex);
		}
 	}
 
 	public void destroy() {
 		resourceDefinitionFactory.destroy();
 		resourceFunctionFactory.destroy();
 	}
 
 	public void setHandlerHeaders(HttpServletRequest request,
 			HttpServletResponse response) {
 		response.setHeader("x-sdata-handler", this.getClass().getName());
 		response.setHeader("x-sdata-url", request.getPathInfo());
 	}
 
 	private Site getSite(String siteId) throws IdUnusedException {
 		Site site = siteService.getSite(siteId);
 		return site;
 	}
 
 	private Map<String, Object> siteInfo(Site site, HttpServletRequest request,
 			HttpServletResponse response) {
 		String status = "900";
 		List<Map<String, Object>> arlpages = new ArrayList<Map<String, Object>>();
 		List<Map<String, Object>> tools = new ArrayList<Map<String, Object>>();
 
 		String curUser = sessionManager.getCurrentSessionUserId();
 
 		/*
 		 * Determine the sites the current user is a member of
 		 */
 		currentSession = sessionManager.getCurrentSession();
 		mySites = ((List<Site>) siteService.getSites(SelectionType.ACCESS,
 				null, null, null, SortType.TITLE_ASC, null));
 
 		try {
 			mySites.add(0, (siteService.getSite(siteService
 					.getUserSiteId(currentSession.getUserId()))));
 		} catch (IdUnusedException e) {
 		}
 
 		/*
 		 * See whether the user is allowed to see this page
 		 */
 		Map<String, Object> map2 = new HashMap<String, Object>();
 		if (site == null) {
 			status = "901";
 		} else {
 			boolean member = false;
 
 			map2.put("title", site.getTitle());
 			map2.put("id", site.getId());
 			map2.put("icon", site.getIconUrl());
 			map2.put("skin", site.getSkin());
 
 			if (!site.isPublished())
 				status = "903";
 
 			for (Site mysite : mySites)
 				if (mysite.getId().equals(site.getId())
 						|| mysite.getId().equals("!admin")
 						|| mysite.getId().equals("~admin"))
 					member = true;
 
 			if (member == false) {
 				status = "902";
 
 				if (site.isAllowed(curUser, "site.visit")) {
 					status = "904";
 					member = true;
 				} else if (request.getRemoteUser() == null) {
 					member = false;
 					status = "901";
 				} else if (site.isJoinable()) {
 					status = "905";
 				}
 			}
 
 			int number = 0;
 
 			if (member) {
 				List<SitePage> pages = (List<SitePage>) site.getOrderedPages();
 
 				for (SitePage page : pages) {
 					number++;
 
 					HashMap<String, Object> mpages = new HashMap<String, Object>();
 
 					mpages.put("id", page.getId());
 					mpages.put("name", page.getTitle());
 					mpages.put("layout", page.getLayoutTitle());
 					mpages.put("number", number);
 					mpages.put("popup", page.isPopUp());
 
 					ArrayList<HashMap<String, Object>> arltools = new ArrayList<HashMap<String, Object>>();
 					List<ToolConfiguration> lst = (List<ToolConfiguration>) page
 							.getTools();
 
 					mpages.put("iconclass", "icon-"
 							+ lst.get(0).getToolId().replaceAll("[.]", "-"));
 
 					for (ToolConfiguration conf : lst) {
 						HashMap<String, Object> tool = new HashMap<String, Object>();
 						tool.put("url", conf.getId());
 						Tool t = conf.getTool();
 
 						if (t != null && t.getId() != null) {
 							tool.put("title", conf.getTool().getTitle());
 							Set<Object> config = t.getFinalConfig().keySet();
 							tool.put("layouthint", conf.getLayoutHints());
 						} else {
 							tool.put("title", page.getTitle());
 						}
 						arltools.add(tool);
 					}
 
 					mpages.put("tools", arltools);
 
 					arlpages.add(mpages);
 
 				}
 
 				if (request.getParameter("writeevent") != null) {
 					EventTrackingService ets = org.sakaiproject.event.cover.EventTrackingService
 							.getInstance();
 					try {
 						Event event = ets.newEvent("pres.begin", "/site/"
 								+ site.getId(), true);
 						ets.post(event);
 					} catch (Exception ex) {
 						log.warn("Failed to register pres.begin event ", ex);
 					}
 					try {
 						Event event = ets.newEvent("site.visit", "/site/"
 								+ site.getId(), true);
 						ets.post(event);
 					} catch (Exception ex) {
 						log.warn("Failed to register site.visit event ", ex);
 					}
 				}
 
 				ArrayList<HashMap<String, String>> roles = new ArrayList<HashMap<String, String>>();
 				try {
 					AuthzGroup group = authzGroupService.getAuthzGroup("/site/"
 							+ site.getId());
 					for (Object o : group.getRoles()) {
 						Role r = (Role) o;
 						HashMap<String, String> map = new HashMap<String, String>();
 						map.put("id", r.getId());
 						map.put("description", r.getDescription());
 						roles.add(map);
 					}
 					map2.put("roles", roles);
 				} catch (Exception ex) {
 					log.info("Roles undefined for " + site.getId());
 				}
 				tools = buildAvailableTools(site);
 			} else {
 				if (request.getRemoteUser() == null) {
 					try {
 						response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
 								"Not Logged In");
 					} catch (IOException ex) {
 					}
 				}
 			}
 		}
 		map2.put("status", status);
 		map2.put("pages", arlpages);
 		map2.put("allTools", tools);
 		return map2;
 	}
 
 	private List<Map<String, Object>> buildAvailableTools(Site site) {
 		// set the category to the site type if site type isn't null
 		HashSet<String> cats = null;
 		if (site.getType() != null) {
 			cats = new HashSet<String>();
 			cats.add(site.getType());
 		}
 
 		// look up tools based on category (site type)
 		Set<Tool> tools = toolManager.findTools(null, null);
 
 		// create a list of maps that hold tool info
 		List<Map<String, Object>> toolsOut = new ArrayList<Map<String, Object>>();
 		for (Tool tool : tools) {
 			// put certain tool attributes into the outbound map
 			HashMap<String, Object> toolOut = new HashMap<String, Object>();
 			toolOut.put("id", tool.getId());
 			toolOut.put("title", tool.getTitle());
 			toolOut.put("description", tool.getDescription());
 			toolOut.put("iconclass", "icon-"
 					+ tool.getId().replaceAll("[.]", "-"));
 			toolsOut.add(toolOut);
 		}
 		return toolsOut;
 	}
 }
