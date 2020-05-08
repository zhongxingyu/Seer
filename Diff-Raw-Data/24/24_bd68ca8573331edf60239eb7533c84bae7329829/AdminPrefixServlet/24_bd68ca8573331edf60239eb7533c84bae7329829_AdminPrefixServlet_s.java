 /*
  *  Copyright 2012 Axel Winkler, Daniel Dunér
  * 
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.server.servlets;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.daxplore.presenter.server.storage.DeleteData;
 import org.daxplore.presenter.server.storage.PMF;
 import org.daxplore.presenter.server.storage.PrefixStore;
 import org.daxplore.presenter.server.throwable.BadRequestException;
 import org.daxplore.presenter.server.throwable.InternalServerException;
 import org.daxplore.shared.SharedResourceTools;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 @SuppressWarnings("serial")
 public class AdminPrefixServlet extends HttpServlet {
 	private static final Logger logger = Logger.getLogger(AdminPrefixServlet.class.getName());
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
 		doPost(request, response);
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
 		PersistenceManager pm = null;
 		try {
 			pm = PMF.get().getPersistenceManager();
 			String responseText = "";
 			String action = request.getParameter("action");
 			if(action==null) {
 				throw new BadRequestException("No action requested");
 			}
 			switch(action) {
 			case "list":
				responseText = getPrefixListJson(pm);
 				break;
 			case "add":
 				String prefix = request.getParameter("prefix");
 				if(!SharedResourceTools.isSyntacticallyValidPrefix(prefix)){
 					throw new BadRequestException("Not a syntactically valid prefix: '" + prefix + "'");
 				}
 				pm.makePersistent(new PrefixStore(prefix));
 				logger.log(Level.INFO, "Added prefix to system: " + prefix);
				responseText = getPrefixListJson(pm);
 				break;
 			case "delete":
 				prefix = request.getParameter("prefix");
 				if(!SharedResourceTools.isSyntacticallyValidPrefix(prefix)){
 					throw new BadRequestException("Not a syntactically valid prefix: '" + prefix + "'");
 				}
 				String result = DeleteData.deleteForPrefix(pm, prefix);
 				logger.log(Level.INFO, result);
				responseText = getPrefixListJson(pm);
 				break;
 			case "metadata":
 				prefix = request.getParameter("prefix");
 				if(!SharedResourceTools.isSyntacticallyValidPrefix(prefix)){
 					throw new BadRequestException("Not a syntactically valid prefix: '" + prefix + "'");
 				}
 				responseText = getPrefixMetadata(prefix);
 				break;
 			default:
 				throw new BadRequestException("Invalid action '" + request.getParameter("action") + "' requested");
 			}
 			
 			try {
 				response.getWriter().write(responseText);
 			} catch (IOException e) {
 				throw new InternalServerException("Failed to write prefix list", e);
 			}
 			
 		} catch (BadRequestException e) {
 			logger.log(Level.WARNING, e.getMessage(), e);
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 		} catch (InternalServerException e) {
 			logger.log(Level.SEVERE, e.getMessage(), e);
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		} catch (RuntimeException e) {
 			logger.log(Level.SEVERE, "Unexpected exception: " + e.getMessage(), e);
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		} finally {
 			if (pm!=null) {
 				pm.close();
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
	private static String getPrefixListJson(PersistenceManager pm) {
		List<String> prefixes = PrefixStore.getPrefixes(pm);
 		JSONArray jsonArray = new JSONArray();
		jsonArray.addAll(prefixes);
 		jsonArray.toJSONString();
 		return jsonArray.toJSONString();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static String getPrefixMetadata(String prefix) {
 		JSONObject metaMap = new JSONObject();
 		metaMap.put("prefix", prefix);
 		
 		// or figure out some relevant statistics that's equally interesting
 		metaMap.put("statcount", "<TODO>"); //TODO count items or some such
 		return metaMap.toJSONString();
 	}
 	
 }
