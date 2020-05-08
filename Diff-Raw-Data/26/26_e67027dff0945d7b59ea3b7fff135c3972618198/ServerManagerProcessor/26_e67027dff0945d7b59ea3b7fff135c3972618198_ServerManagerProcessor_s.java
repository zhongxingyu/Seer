 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package jipdbs.web.processors;
 
 import iddb.api.RemotePermissions;
 import iddb.core.IDDBService;
 import iddb.core.model.Server;
 import iddb.core.model.ServerPermission;
 import iddb.core.util.Functions;
 import iddb.web.security.service.UserPermission;
 import iddb.web.security.service.UserServiceFactory;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jipdbs.web.Flash;
 import jipdbs.web.MessageResource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ar.sgt.resolver.SimpleEntry;
 import ar.sgt.resolver.exception.HttpError;
 import ar.sgt.resolver.exception.ProcessorException;
 import ar.sgt.resolver.flow.ForceRedirect;
 import ar.sgt.resolver.processor.ResolverContext;
 import ar.sgt.resolver.processor.ResponseProcessor;
 import ar.sgt.resolver.utils.UrlReverse;
 
 public class ServerManagerProcessor extends ResponseProcessor {
 
 	private static final Logger log = LoggerFactory.getLogger(ServerManagerProcessor.class);
 	
 	/* (non-Javadoc)
 	 * @see ar.sgt.resolver.processor.ResponseProcessor#doProcess(ar.sgt.resolver.processor.ResolverContext)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public String doProcess(ResolverContext context) throws ProcessorException {
 
 		IDDBService app = (IDDBService) context.getServletContext().getAttribute("jipdbs");
 		
 		HttpServletRequest req = context.getRequest();
 
 		String id = context.getParameter("key");
 		
 		if (id == null) {
 			List<Server> servers = UserServiceFactory.getUserService().listUserServers(UserPermission.LEVEL_SUPERADMIN);
 			if (servers.size() == 1) {
 				UrlReverse reverse = new UrlReverse(context.getServletContext());
 				String url;
 				try {
 					url = reverse.resolve("manage-server", new Entry[]{new SimpleEntry("key", servers.get(0).getKey().toString())});
 				} catch (Exception e) {
 					log.error(e.getMessage());
 					throw new ProcessorException(e);
 				}
 				throw new ForceRedirect(req.getContextPath() + url);
 			} else {
 				if (servers.size() == 0) Flash.warn(req, MessageResource.getMessage("manager_noservers"));
 				req.setAttribute("servers", servers);
 				return null;
 			}
 		}
 		
 		Server server;
 		try {
 			server = app.getServer(Long.parseLong(id), true);
 		} catch (Exception e) {
 			StringWriter w = new StringWriter();
 			e.printStackTrace(new PrintWriter(w));
 			log.error(w.getBuffer().toString());
 			throw new HttpError(HttpServletResponse.SC_NOT_FOUND);
 		}
 
 		if (!UserServiceFactory.getUserService().hasPermission(server.getKey(), UserPermission.LEVEL_SUPERADMIN)) {
 			throw new HttpError(HttpServletResponse.SC_FORBIDDEN);
 		}
 		
 		if (context.isPost()) {
 			List<ServerPermission> lp = new ArrayList<ServerPermission>();
 			lp.add(new ServerPermission(RemotePermissions.ADD_BAN, Integer.parseInt(req.getParameter("ban"))));
 			lp.add(new ServerPermission(RemotePermissions.REMOVE_BAN, Integer.parseInt(req.getParameter("unban"))));
 			lp.add(new ServerPermission(RemotePermissions.REMOVE_NOTICE, Integer.parseInt(req.getParameter("delnote"))));
 			lp.add(new ServerPermission(RemotePermissions.ADD_NOTICE, Integer.parseInt(req.getParameter("addnote"))));
 			log.debug("Update permissions");
 			app.saveServerPermissions(server, lp);
 			String value = req.getParameter("maxban");
 			Long maxban = Functions.time2minutes(value);
 			if (maxban > 0 && maxban != server.getMaxBanDuration()) {
 				log.debug("Update max ban duration");
 				server.setMaxBanDuration(maxban);
 				app.saveServer(server);
 			}
 			if (maxban == 0) {
 				Flash.warn(req, MessageResource.getMessage("server_invalid_maxban"));
 			}
 			Flash.info(req, MessageResource.getMessage("server_updated"));
 		}
 		
 		req.setAttribute("server", server);
 		
 		return null;
 	}
 
 }
