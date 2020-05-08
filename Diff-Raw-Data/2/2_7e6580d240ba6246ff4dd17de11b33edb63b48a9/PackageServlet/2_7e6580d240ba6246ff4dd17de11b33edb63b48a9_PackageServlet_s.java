 package org.protege.osgi.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.wiring.BundleRevision;
 import org.osgi.framework.wiring.BundleWire;
 import org.osgi.framework.wiring.BundleWiring;
 
 public class PackageServlet extends HttpServlet {
     private static final long serialVersionUID = -2083051888153213291L;
     
     public static final String PATH="/debug/package";
     
     public static final String BUNDLE_LIST_MENU = "BundleList";
     
     private BundleContext context;
     
     public PackageServlet(BundleContext context) {
         this.context = context;
     }
     
     protected void doGet(HttpServletRequest request, 
                          HttpServletResponse response) throws IOException, ServletException {
         response.setContentType("text/html");
         Bundle b = getBundle(request);
         PrintWriter out = response.getWriter();
         doExplanation(out);
         doForm(out, b == null ? -1 : b.getBundleId());
         doResults(out, b);
     }
     
     private void doExplanation(PrintWriter out) {
         out.println("<H1>Package Import/Export Debug Utility</H1>");
         out.println("<P>This servlet helps one see which import statements from one bundle get");
         out.println("hooked up with which export statements from other bundles.<P>");
     }
     
     private Bundle getBundle(HttpServletRequest request) {
         String[] ids  = request.getParameterValues(BUNDLE_LIST_MENU);
         if (ids == null) {
             return null;
         }
         for (String id : ids) {
             id = id.trim();
             long bundleId = Long.parseLong(id);
             return context.getBundle(bundleId);
         }
         return null;
     }
     
     private void doForm(PrintWriter out, long bundleId) {
         out.println("<HTML><HEAD><TITLE>Bundle Package Debugger</TITLE>"
                        + "</HEAD><BODY>");
         out.println("<FORM METHOD=\"GET\"/>");
         out.println("<SELECT NAME=\"" + BUNDLE_LIST_MENU + "\">");
         for (Bundle b : context.getBundles()) {
             out.println("<OPTION VALUE=\"" + b.getBundleId() + "\" ");
             if (b.getBundleId() == bundleId) {
                 out.println("SELECTED=\"TRUE\"");
             }
             out.println(">");
             printBundle(out, b, null);
             out.println("</OPTION>");
         }
         out.println("</SELECT>");
         out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"SUBMIT\"/>");
         out.println("</FORM>");
     }
     
     
     private void doResults(PrintWriter out, Bundle b) {
         if (b != null) {
             out.println("Information for bundle ");
             printBundle(out, b, "");
             doImports(out, b);
             doExports(out, b);
         }
     }
 
     private void doImports(PrintWriter out, Bundle b) {
         Set<BundleWire> imports = new TreeSet<BundleWire>(new Comparator<BundleWire>() {
 
             public int compare(BundleWire p1, BundleWire p2) {
                 String p1Name = (String) p1.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);
                String p2Name = (String) p1.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);
                 return p1Name.compareTo(p2Name);
             }
             
         });
         BundleWiring wiring = b.adapt(BundleWiring.class);
         List<BundleWire> importsList = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
         if (importsList != null) {
             imports.addAll(importsList);
         }
         out.println("<P><B>Imports</B><P>");
         if (imports.isEmpty()) {
             out.println("No imports");
         }
         else {
             out.println("<UL>");
             for (BundleWire p : imports) {
                 String packageName = (String) p.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);
                 out.println("<li> " + packageName + " imported from bundle ");
                 printBundle(out, p.getProviderWiring().getBundle(), "");
             }
             out.println("</UL>");
         }
     }
     
     private void doExports(PrintWriter out, Bundle b) {
         BundleWiring wiring = b.adapt(BundleWiring.class);
         List<BundleWire> packages = wiring.getProvidedWires(BundleRevision.PACKAGE_NAMESPACE);
         out.println("<P><B>Exports</B><P>");
         if (packages != null && packages.size() > 0) {
             for (BundleWire p : packages) {
                 String packageName = (String) p.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);
                 out.println("<li> " + packageName);
                 Bundle importer = p.getRequirerWiring().getBundle();
                 printBundle(out, importer, "");
             }
             out.println("</UL>");
         } else {
             out.println("No exports.");
         }
     }
     
     public static void printBundle(PrintWriter out, Bundle b, String hyperlink) {
         String bundleString = b.getSymbolicName() + " - " + b.getBundleId() + " (" + b.getHeaders().get(Constants.BUNDLE_NAME) + ")";
         if (hyperlink != null) {
             out.print("<A HREF=\"" + hyperlink + "?" + BUNDLE_LIST_MENU + "=" + b.getBundleId() + "\">" 
                             + bundleString + "</A>");
         }
         else {
             out.print(bundleString);
         }
     }
 
 }
