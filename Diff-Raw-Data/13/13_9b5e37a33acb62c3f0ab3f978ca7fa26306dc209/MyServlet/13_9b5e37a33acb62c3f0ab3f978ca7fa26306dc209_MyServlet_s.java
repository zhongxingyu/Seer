 package org.coderthoughts.cloud.demo.webui.impl;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.coderthoughts.cloud.demo.api.TestService;
 import org.coderthoughts.cloud.framework.service.api.OSGiFramework;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 public class MyServlet extends HttpServlet {
     private static final long serialVersionUID = 1L;
     private final BundleContext bundleContext;
 
     MyServlet(BundleContext context) {
         bundleContext = context;
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.setContentType("text/html");
         PrintWriter out = resp.getWriter();
 
         out.println("<html><head><title>Test Web UI</title></head><body>");
 
         printFrameworks(out);
         printTestServices(out);
 
         out.println("</body></html>");
         out.close();
     }
 
     private void printFrameworks(PrintWriter out) {
         out.println("<H2>Frameworks in the Cloud Ecosystem</H2>");
         try {
            for (ServiceReference ref : bundleContext.getServiceReferences(OSGiFramework.class.getName(), null)) {
                 Map<String, Object> sortedProps = new TreeMap<String, Object>();
                 for (String key : ref.getPropertyKeys()) {
                     if (!key.startsWith("org."))
                         // Don't display all they props to keep things tidy
                         continue;
                     sortedProps.put(key, ref.getProperty(key));
                 }
 
                 out.println("OSGi Framework<UL>");
                 for (String key : sortedProps.keySet()) {
                     out.println("<li>" + key + " - " + sortedProps.get(key) + "</li>");
                 }
                 out.println("</UL>");
             }
         } catch (InvalidSyntaxException e) {
             e.printStackTrace();
         }
     }
 
     private void printTestServices(PrintWriter out) {
         out.println("<H2>TestService instances available</H2>");
 
         try {
            for (ServiceReference ref : bundleContext.getServiceReferences(TestService.class.getName(), null)) {
                 TestService svc = (TestService) bundleContext.getService(ref);
                 out.println("TestService instance<ul>");
                 out.println("<li>invoking: " + svc.doit("Hi there"));
                 out.println("</li></ul>");
                 bundleContext.ungetService(ref);
             }
         } catch (InvalidSyntaxException e) {
             e.printStackTrace();
         }
     }
 }
