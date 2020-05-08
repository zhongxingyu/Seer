 package org.neuro4j.web.console.controller;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.neuro4j.NetworkUtils;
 import org.neuro4j.core.Entity;
 import org.neuro4j.core.Network;
 import org.neuro4j.core.Relation;
 import org.neuro4j.nms.server.NMSServerConfig;
 import org.neuro4j.storage.NQLException;
 import org.neuro4j.storage.NeuroStorage;
 import org.neuro4j.storage.StorageException;
 import org.neuro4j.web.console.utils.RequestUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class EntitiesController {
 	
 	public EntitiesController() {
 	}
 
 	@RequestMapping("/view")
 	public String viewContext(HttpServletRequest request) {
 		request.setAttribute("init_view", "true");
 		return "console/e/view";
 	}
 
 
 	@RequestMapping("/entity-details")
 	public String entityDetails(HttpServletRequest request) throws StorageException {
 
 		String view = request.getParameter("vt");
 		if (!"graph".equalsIgnoreCase(view))
 			view = "list";
 
 		request.setAttribute("view", view);
 		
 		RequestUtils.params2attributes(request, "vt", "view_depth", "eid", "storage");
 		
 		request.setAttribute("entity_view", "true");
 		
 		
 		String eid = (String) request.getParameter("eid");
 		if (null == eid)
 			return "redirect:/entities";
 
 		NeuroStorage neuroStorage = NMSServerConfig.getInstance().getStorage(request.getParameter("storage"));
 		if (null == neuroStorage)
 		{
 			request.setAttribute("storage_error", "Storage is not specified");
 			return "console/settings";
 		}
 
 		Entity e = neuroStorage.getEntityByUUID(eid);
		NetworkUtils.loadConnected(e, neuroStorage);
 		if (null == e)
 			return "redirect:/query";
 
 		request.setAttribute("entity", e);
 		int depth = 1;
 		try
 		{
 			String depthStr = request.getParameter("view_depth");
 			if (null != depthStr && depthStr.length() > 0)
 				depth = Integer.parseInt(depthStr);
 		} catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}
 		
 		String queryStr = "select e(id='" + eid + "') / [depth='" + 2*depth + "']";
 		request.setAttribute("q", queryStr);
 		
 		
 		if ("graph".equalsIgnoreCase(view))
 		{
 			request.setAttribute("include_accordion_js", "true");
 			request.setAttribute("selected_tab", "graph");
 		}		
 
 		return "console/e/details";
 	}
 	
 	@RequestMapping("/entity-details-more-data")
 	public String entityGraphDetails(HttpServletRequest request, HttpServletResponse response) throws StorageException {
 		String eid = (String) request.getParameter("eid");
 		RequestUtils.params2attributes(request, "q", "storage");
 
 		// for details 1 level of expand is enough
 		String queryStr = "select e(id='" + eid + "') / r() "; 
 		Network net;
 		try {
 			NeuroStorage neuroStorage = NMSServerConfig.getInstance().getStorage(request.getParameter("storage"));
 			if (null == neuroStorage)
 			{
 				request.setAttribute("storage_error", "Storage is not specified");
 				return "console/settings";
 			}
 			net = neuroStorage.query(queryStr);
 			Entity e = net.getEntityByUUID(eid);
 
 			request.setAttribute("entity", e);		
 
 			Map<String, List<Relation>> groupedRelationMap = e.groupRelationsByName();// NetUtils.groupRelationsByName(e.getRelations()); //  getRelationMapGroupedByType(e);
 			request.setAttribute("grouped_relation_map", groupedRelationMap);
 			
 			response.setCharacterEncoding("UTF-8");
 
 		} catch (NQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} // load
 
 		return "console/e/graph-details";
 	}
 	
 }
