 /*
  * This file is part of experimaestro.
  * Copyright (c) 2012 B. Piwowarski <benjamin@bpiwowar.net>
  *
  * experimaestro is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * experimaestro is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package sf.net.experimaestro.server;
 
 import com.sleepycat.je.DatabaseException;
 import sf.net.experimaestro.exceptions.CloseException;
 import sf.net.experimaestro.scheduler.Resource;
 import sf.net.experimaestro.scheduler.Resource.PrintConfig;
 import sf.net.experimaestro.scheduler.ResourceLocator;
 import sf.net.experimaestro.scheduler.ResourceState;
 import sf.net.experimaestro.scheduler.Scheduler;
 import sf.net.experimaestro.utils.CloseableIterable;
 import sf.net.experimaestro.utils.arrays.ListAdaptator;
 import sf.net.experimaestro.utils.log.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 /**
  * Gives the current task status
  *
  * @author B. Piwowarski <benjamin@bpiwowar.net>
  */
 public class StatusServlet extends XPMServlet {
     final static private Logger LOGGER = Logger.getLogger();
 
     private static final long serialVersionUID = 1L;
    public static final String RESOURCE_PATH = "/resource/";
     private final Scheduler scheduler;
 
     public StatusServlet(Scheduler manager) {
         this.scheduler = manager;
     }
 
     protected void doGet(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
 
         String localPath = request.getRequestURI().substring(
                 request.getServletPath().length());
 
         if (localPath.equals("")) {
             final PrintWriter out = startHTMLResponse(response);
             header(out, "Resources");
 
             ArrayList<ResourceState> values = new ArrayList<>(
                     ListAdaptator.create(ResourceState.values()));
             for (ResourceState state : values) {
                 out.format("<h1>Resources in state %s</h1>", state);
                 out.println("<ul>");
                 try (final CloseableIterable<Resource> resources = scheduler.resources()) {
                     for (Resource resource : resources) {
                         resource.init(scheduler);
                         if (resource.getState() == state) {
                             try {
                                 ResourceLocator locator = resource.getLocator();
                                 out.format(
                                        "<li><a href=\"%s/resource/%d\">%s</a></li>",
                                         request.getServletPath(),
                                        resource.getId(),
                                         locator);
                             } catch(Throwable t) {
                                 out.format("<li><b>Resource ID %s</b> without locator</li>", resource.getId());
                             }
                         }
                     }
                 } catch (CloseException e) {
                     LOGGER.warn("Error while closing the iterator");
                 }
                 out.println("</ul>");
             }
 
             out.println("</body></html>");
             return;
         }
 
        if (localPath.startsWith(RESOURCE_PATH)) {
            long resourceId = Long.parseLong(localPath.substring("/resource/".length()));
             PrintWriter out = startHTMLResponse(response);
 
            Resource resource = scheduler.getResource(resourceId);
            ResourceLocator locator = resource.getLocator();
             header(out, String.format("Details of resource %s", locator));
 
             try {
                 resource = scheduler.getResource(locator);
             } catch (DatabaseException e) {
                 throw new IOException(e);
             }
 
             if (resource != null) {
                 PrintConfig config = new PrintConfig();
                 config.detailURL = request.getServletPath();
                 resource.init(scheduler);
                 resource.printXML(out, config);
             } else {
                 out.format("Could not retrieve resource <b>%s</b>", locator);
             }
 
             out.println("</body></html>");
             return;
         }
 
         // Not found
         ContentServlet.error404(request, response);
     }
 }
