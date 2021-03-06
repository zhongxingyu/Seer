 /*
  * Copyright 2002-2006 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mockey.ui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.mockey.model.ClientRequest;
 import com.mockey.model.Service;
 import com.mockey.model.Url;
 import com.mockey.storage.IMockeyStorage;
 import com.mockey.storage.InMemoryMockeyStorage;
 
 /**
  * <code>MockHistoryPerServiceByIpServlet</code> manages the display of past
  * requests to a service by calling IP address.
  * 
  * @author Chad Lafontaine (chad.lafontaine)
  * @version $Id: MockHistoryRequestLogServlet.java,v 1.2 2005/05/03 22:28:54
  *          clafonta Exp $
  */
 public class HistoryPerServiceByIpServlet extends HttpServlet {
 
     private static final long serialVersionUID = -2255013290808524662L;
 
     private static IMockeyStorage store = InMemoryMockeyStorage.getInstance();
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		String iprequest = req.getParameter("iprequest");
 		Long serviceId = new Long(req.getParameter("serviceId"));		
 		String action = req.getParameter("action");
 		boolean didDelete = false;
 		if(action!=null && "delete".equals(action)){
 		    // Delete from history
 		    Long scenarioId = new Long(req.getParameter("scenarioId"));		    
 		    store.deleteLoggedClientRequest(scenarioId);
 
 		    // don't allow reloads to re-delete.  crappy hack.
 		    didDelete = true;
 		    
		}else if(action!=null && "delete_all".equals(action)){
		    store.deleteAllLoggedClientRequestForService(serviceId);
         } else if (action != null && "delete_all".equals(action)) {
             store.deleteAllLoggedClientRequestForService(serviceId);
 
 		Service ms = store.getServiceById(serviceId);
 		List<ClientRequest> scenarios = store.getClientRequests();
 		List scenarioHistoryList = new ArrayList();
 		if(scenarios!=null){
             for (ClientRequest type : scenarios) {
                 if (type.getServiceInfo().getServiceId().equals(serviceId) && type.getServiceInfo().getRequestorIP().equals(iprequest)) {
                     scenarioHistoryList.add(type);
                 }
             }
         }
 
         req.setAttribute("scenarioHistoryList", scenarioHistoryList);
         req.setAttribute("mockservice", ms);
         req.setAttribute("iprequest", iprequest);
 
 
         // don't allow reloads to re-delete. crappy hack.
         if (didDelete) {
             String contextRoot = req.getContextPath();
             resp.sendRedirect(Url.getContextAwarePath("/history/detail?serviceId=" + ms.getId() + "&iprequest=" + iprequest, contextRoot));
             return;
         } else {

             RequestDispatcher dispatch = req.getRequestDispatcher("/service_history_ip.jsp");
             dispatch.forward(req, resp);
         }
     }
}}
