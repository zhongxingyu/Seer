 package org.apache.camel.poc.dlb.console;
 
 import org.apache.camel.CamelContext;
 import org.apache.camel.Route;
 import org.apache.camel.ServiceStatus;
 import org.apache.camel.management.DefaultManagementAgent;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class HomeServlet extends HttpServlet {
 
     private BundleContext bundleContext;
 
     public void init(ServletConfig servletConfig) throws ServletException {
         ServletContext context = servletConfig.getServletContext();
         bundleContext = (BundleContext) context.getAttribute("osgi-bundlecontext");
     }
 
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doIt(request, response);
     }
 
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doIt(request, response);
     }
 
     public void doIt(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         try {
             PrintWriter writer = response.getWriter();
             writer.println(Template.header());
 
             writer.println("<div id=\"toolsbar_bc\">");
             writer.println("<div id=\"dc_refresh\" class=\"bc_btn\"><div class=\"bb\"><div><a href=\"home.do\"><img src=\"img/icons/database_refresh.gif\" alt=\"Refresh\" /><span>Refresh</span></a></div></div></div>");
             writer.println("</div>");
 
             writer.println("<div id=\"body_bc\">");
             writer.println("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" id=\"bc_grid\">");
             writer.println("\t\t  <thead>\n" +
                     "\t\t  \t<tr>\n" +
                     "\t\t  \t  <th align=\"left\">Name</th>\n" +
                     "\t\t\t  <th>Status</th>\n" +
                     "\t\t\t  <th>Info</th>\n" +
                     "\t\t\t</tr>\n" +
                     "\t\t  </thead>");
             writer.println("<tbody>");
 
             // display DLB route
             writer.println("<tr>");
             writer.println("<td colspan=\"3\"><span><b>Load Balancer</b></span></td>");
             writer.println("</tr>");
             writer.println("<tr>");
             writer.println("<td class=\"td0\"><span>DLB</span></td>");
             Route dlbRoute = this.getRoute("dlb", null);
             if (dlbRoute == null) {
                 writer.println("<td><img src=\"img/icons/stop.gif\" alt=\"Stopped\"/><span>Stopped</span></td>");
             } else {
                 writer.println("<td><img src=\"img/icons/route.gif\" alt=\"Started\"/><span>Started</span></td>");
             }
             writer.println("<td>");
             writer.println("<b>Threshold</b>: 2 msg/sec<br/>");
             // get the message rate
             CamelContext camelContext = dlbRoute.getRouteContext().getCamelContext();
             MBeanServer mBeanServer = camelContext.getManagementStrategy().getManagementAgent().getMBeanServer();
             Set<ObjectName> set = null;
             try {
                 set = mBeanServer.queryNames(new ObjectName(DefaultManagementAgent.DEFAULT_DOMAIN + ":type=routes,name=\"dlb\",*"), null);
                 Iterator<ObjectName> iterator = set.iterator();
                 if (iterator.hasNext()) {
                     ObjectName routeMBean = iterator.next();
                     Long exchangesCompleted = (Long) mBeanServer.getAttribute(routeMBean, "ExchangesCompleted");
                     writer.println("<b>Exchanges Completed</b>: " + exchangesCompleted + "<br/>");
                     Date firstExchangeCompletedDate = (Date) mBeanServer.getAttribute(routeMBean, "FirstExchangeCompletedTimestamp");
                     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                     if (firstExchangeCompletedDate != null) {
                         writer.println("<b>First Exchange</b>: " + format.format(firstExchangeCompletedDate) + "<br/>");
                     }
                     Date lastExchangeCompletedDate = (Date) mBeanServer.getAttribute(routeMBean, "LastExchangeCompletedTimestamp");
                     if (lastExchangeCompletedDate != null) {
                         writer.println("<b>Last Exchange</b>: " + format.format(lastExchangeCompletedDate) + "<br/>");
                     }
                 }
             } catch (Exception e) {
                 throw new IllegalStateException(e);
             }
             writer.println("</td>");
 
             writer.println("</tr>");
             
             // display target node routes
             writer.println("<tr>");
             writer.println("<td colspan=\"3\"><span><b>Target Nodes</b></span></td>");
             writer.println("</tr>");
 
             List<Route> nodeRoutes = this.getNodeRoutes();
             for (Route nodeRoute : nodeRoutes) {
                 writer.println("<tr>");
                 writer.println("<td class=\"td0\"><span>" + nodeRoute.getId() + "</span></td>");
                 CamelContext nodeCamelContext = nodeRoute.getRouteContext().getCamelContext();
                 ServiceStatus status = nodeRoute.getRouteContext().getRoute().getStatus(nodeCamelContext);
                 if (status == ServiceStatus.Started) {
                     writer.println("<td><img src=\"img/icons/route.gif\" alt=\"Started\"/><span>Started</span></td>");
                 } else {
                     writer.println("<td><img src=\"img/icons/stop.gif\" alt=\"Stopped\"/><span>Stopped</span></td>");
                 }
                 MBeanServer nodeMBeanServer = nodeCamelContext.getManagementStrategy().getManagementAgent().getMBeanServer();
                 Set<ObjectName> nodeSet = null;
                 writer.println("<td>");
                 try {
                     nodeSet = nodeMBeanServer.queryNames(new ObjectName(DefaultManagementAgent.DEFAULT_DOMAIN + ":type=routes,name=\"" + nodeRoute.getId() + "\",*"), null);
                    Iterator<ObjectName> iterator = nodeSet.iterator();
                     if (iterator.hasNext()) {
                         ObjectName routeMBean = iterator.next();
                         Long exchangesCompleted = (Long) nodeMBeanServer.getAttribute(routeMBean, "ExchangesCompleted");
                         writer.println("<b>Exchanges Completed</b>: " + exchangesCompleted + "<br/>");
                         Date firstExchangeCompletedDate = (Date) nodeMBeanServer.getAttribute(routeMBean, "FirstExchangeCompletedTimestamp");
                         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                         if (firstExchangeCompletedDate != null) {
                             writer.println("<b>First Exchange</b>: " + format.format(firstExchangeCompletedDate) + "<br/>");
                         }
                         Date lastExchangeCompletedDate = (Date) nodeMBeanServer.getAttribute(routeMBean, "LastExchangeCompletedTimestamp");
                         if (lastExchangeCompletedDate != null) {
                             writer.println("<b>Last Exchange</b>: " + format.format(lastExchangeCompletedDate) + "<br/>");
                         }
                     }
                 } catch (Exception e) {
                     throw new IllegalStateException(e);
                 }
                 writer.println("</td>");
                 writer.println("</tr>");
             }
             
             writer.println("</tbody>");
             writer.println("</table>");
             writer.println("</div>");
 
             writer.println("\t<div id=\"bc_box\">\n" +
                     "\t\t<h2>Deploy new bundle</h2>\n" +
                     "\t\t<table cellpadding=\"0\" cellspacing=\"5\">\n" +
                     "\t\t <tr>\n" +
                     "\t\t \t<td><span>Bundle</span></td>\n" +
                     "<form action=\"deploy.do\" method=\"POST\" enctype=\"multipart/form-data\">" +
                     "\t\t\t<td><input type=\"file\" name=\"file\" size=\"25\" /></td>\n" +
                     "\t\t </tr>\n" +
                     "\t\t <tr><td colspan=\"3\">&nbsp;</td></tr>\n" +
                     "\t\t <tr>\n" +
                     "\t\t \t<td></td>\n" +
                     "\t\t\t<td>\n" +
                     "\t\t\t\t<input type=\"submit\" value=\"Ok\" id=\"bc_box_ok\" class=\"button\"/>&nbsp;&nbsp;\n" +
                     "\t\t\t\t<input type=\"button\" value=\"Cancel\" id=\"bc_box_cancel\" class=\"button\" />\n" +
                     "</form>" +
                     "\t\t\t</td>\n" +
                     "\t\t\t<td></td>\t\t\t\n" +
                     "\t\t </tr>\n" +
                     "\t\t</table>\n" +
                     "\t</div>");
 
             if (request.getParameter("error") != null) {
                 writer.println("<script type=\"text/javascript\">");
                 writer.println("window.alert('Job/Route Action Error: " + request.getParameter("error") + "')");
                 writer.println("</script>");
             }
 
             writer.println(Template.footer());
 
             writer.flush();
             writer.close();
         } catch (Exception e) {
             throw new ServletException(e);
         }
 
         
     }
 
     private List<CamelContext> getCamelContexts() {
         List<CamelContext> camelContexts = new ArrayList<CamelContext>();
         try {
             ServiceReference[] references = bundleContext.getServiceReferences(CamelContext.class.getName(), null);
             if (references != null) {
                 for (ServiceReference reference : references) {
                     if (reference != null) {
                         CamelContext camelContext = (CamelContext) bundleContext.getService(reference);
                         if (camelContext != null) {
                             camelContexts.add(camelContext);
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return camelContexts;
     }
 
     private CamelContext getCamelContext(String name) {
         for (CamelContext camelContext : this.getCamelContexts()) {
             if (camelContext.getName().equals(name)) {
                 return camelContext;
             }
         }
         return null;
     }
 
     private List<Route> getNodeRoutes() {
         List<Route> routes = new ArrayList<Route>();
         List<CamelContext> camelContexts = this.getCamelContexts();
         for (CamelContext camelContext : camelContexts) {
             for (Route route : camelContext.getRoutes()) {
                 if (route.getId().matches("[N-n]ode\\d+")) {
                     routes.add(route);
                 }
             }
         }
         return routes;
     }
 
     private List<Route> getRoutes(String camelContextName) {
         if (camelContextName != null) {
             CamelContext context = this.getCamelContext(camelContextName);
             if (context != null) {
                 return context.getRoutes();
             }
         } else {
             List<Route> routes = new ArrayList<Route>();
             List<CamelContext> camelContexts = this.getCamelContexts();
             for (CamelContext camelContext : camelContexts) {
                 for (Route route : camelContext.getRoutes()) {
                     routes.add(route);
                 }
             }
             return routes;
         }
         return null;
     }
 
     private Route getRoute(String routeId, String camelContextName) {
         List<Route> routes = this.getRoutes(camelContextName);
         for (Route route : routes) {
             if (route.getId().equals(routeId)) {
                 return route;
             }
         }
         return null;
     }
 
 }
