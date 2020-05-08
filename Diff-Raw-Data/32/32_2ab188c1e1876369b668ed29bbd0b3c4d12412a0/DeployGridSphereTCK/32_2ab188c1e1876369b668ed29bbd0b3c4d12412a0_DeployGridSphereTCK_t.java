 package org.gridlab.gridsphere.tools;
 
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.BuildException;
 
 import java.util.jar.JarFile;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.List;
 import java.io.*;
 
 
 public class DeployGridSphereTCK extends Task {
 
     private String warPath = null;
     private List portlets = new Vector();
     private List portletapps = new Vector();
 
     private class WARFilenameFilter implements  FilenameFilter {
 
       public boolean accept(File dir, String name) {
           if (name.endsWith(".war")) return true;
           return false;
       }
     }
 
     public void setWarDir(String warDir) {
         this.warPath = warDir;
         //System.out.println("Setting configdir to: "+this.configDir);
     }
 
     /**
      * Tool to transform Sun TCK portlet WAR's to GridSphere JSR model
      */
     public void execute() throws BuildException {
 
         System.out.println("GridSphere tool to deploy Sun TCK portlet WAR files as ");
         System.out.println(" GridSphere JSR portlet applications and create a test layout descriptor");
 
         try {
             loadWars(warPath);
             createLayout();
             createPortletMaster();
         } catch (IOException e) {
             System.err.println("Error converting WARS:");
             e.printStackTrace();
         }
     }
 
     private void createPortletMaster() throws IOException {
 
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("PortletMaster-tck.xml")));
         out.println("<portlet-services>\n" +
                 "\n" +
                 "    <service>\n" +
                 "        <name>Portlet Manager Service</name>\n" +
                 "        <user-required>true</user-required>\n" +
                 "        <description>Provides Administration Capabilities for Portlet Web Applications</description>\n" +
                 "        <interface>org.gridlab.gridsphere.services.core.registry.PortletManagerService</interface>\n" +
                 "        <implementation>org.gridlab.gridsphere.services.core.registry.impl.PortletManagerServiceImpl</implementation>\n" +
                 "        <service-config>\n" +
                 "            <param-name>startup-portlet-webapps</param-name>\n");
                 out.print("<param-value>gridsphere");
                 for (int i = 0; i < portletapps.size(); i++) {
                     out.print(", " + portletapps.get(i));
                 }
 
 
                 out.print("</param-value>\n" +
                 "        </service-config>\n" +
                 "    </service>\n" +
                 "\n" +
                 "</portlet-services>");
         out.close();
     }
 
 
     private void createLayout() throws IOException {
 
 
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("guest-layout-tck.xml")));
 
         out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         out.println("<page-layout theme=\"xp\" title=\"GridSphere Portal\">");
         out.println("<portlet-header/>");
         out.print("<portlet-tabbed-pane selected=\"0\" style=\"menu\">\n" +
                 "    <portlet-tab label=\"gridsphere\">\n" +
                 "        <title lang=\"en\">GridSphere</title>");
         System.err.println("Number of portlets: " + portlets.size());
         for (int i = 0; i < portlets.size(); i++) {
             System.err.println((String)portlets.get(i));
             out.println("<portlet-frame>");
             out.println("<portlet-class>" + (String)portlets.get(i) + "</portlet-class>");
             out.println("</portlet-frame>");
         }
         out.println("</portlet-tab></portlet-tabbed-pane></page-layout");
 
         out.close();
 
     }
 
     private void loadWars(String warPath) throws IOException {
 
         File warDir = new File(warPath);
 
         if (!warDir.isDirectory()) {
             throw new IOException("Specified TCK directory not valid: " + warPath);
         }
 
         String[] warFiles = warDir.list(new WARFilenameFilter());
 
         byte[] buffer = new byte[1024];
                 int bytesRead;
 
         // loop thru all WARs
         for (int i = 0; i < warFiles.length; i++) {
             //System.err.println(warPath + File.separator + warFiles[i].toString());
 
             String war = warFiles[i].substring(0, warFiles[i].indexOf(".war"));
             portletapps.add(war);
             JarFile jarFile = new JarFile(warPath + File.separator + warFiles[i]);
             JarOutputStream tempJar = new JarOutputStream(new FileOutputStream("/tmp/" + warFiles[i].toString()));
 
             addGridSphereJSRDescriptor(tempJar);
             addGridSphereUILibs(tempJar);
             addGridSphereJSRLibs(tempJar);
 
             // loop thru all jars
             Enumeration files = jarFile.entries();
             while (files.hasMoreElements()) {
                 JarEntry entry = (JarEntry) files.nextElement();
 
                 //System.err.println("\t\t" + entry.getName());
                 if (entry.getName().equals("WEB-INF/web.xml")) {
                     InputStream entryStream = jarFile.getInputStream(entry);
                     modifyWebXML(entryStream, tempJar, warFiles[i]);
                     break;
                 }
                 if (entry.getName().equals("WEB-INF/portlet.xml")) {
                     InputStream entryStream = jarFile.getInputStream(entry);
                     collectPortletNames(entryStream);
                 }
 
                 tempJar.putNextEntry(entry);
 
                 InputStream entryStream = jarFile.getInputStream(entry);
 
                 // Read the entry and write it to the temp jar.
                 while ((bytesRead = entryStream.read(buffer)) != -1) {
                     tempJar.write(buffer, 0, bytesRead);
                 }
 
 
 
             /*
                 File file = new File(destination, entry.getName());
                 File dirF = new File(file.getParent());
                     dirF.mkdirs();
                     if (entry.isDirectory()) {
                         file.mkdirs();
                     } else {
                         byte[] buffer = new byte[1024];
                         int length = 0;
                         InputStream fis = jarFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(file);
                         while ((length = fis.read(buffer)) >= 0) {
                             fos.write(buffer, 0, length);
                         }
                         fos.close();
                     }
                  */
 
 
                 }
             //tempJar.putNextEntry(new JarEntry("stub"));
 
             tempJar.close();
             jarFile.close();
         }
 
     }
 
     public synchronized void modifyWebXML(InputStream webxmlStream, JarOutputStream tempJar, String warname) throws IOException {
         BufferedReader bis = new BufferedReader(new InputStreamReader(webxmlStream));
         String line = null;
         String war = warname.substring(0, warname.indexOf(".war"));
         JarEntry entry = new JarEntry("WEB-INF/web.xml");
         tempJar.putNextEntry(entry);
 
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("tmp.xml")));
         boolean hasServletEntry = false;
         boolean domapping = false;
        while ((line = bis.readLine()) != null)  {
            if (line.endsWith("<servlet-mapping>") && (!hasServletEntry)) {
                 out.println("  <servlet>");
                 out.println("    <servlet-name>PortletServlet</servlet-name>");
                 out.println("    <servlet-class>org.gridlab.gridsphere.provider.portlet.jsr.PortletServlet</servlet-class>");
                 out.println("  </servlet>");
                 out.println(line);
                 domapping = true;
                 hasServletEntry = true;
             } else
             if (line.endsWith("<session-config>") && (!hasServletEntry)) {
                 out.println("  <servlet>");
                 out.println("    <servlet-name>PortletServlet</servlet-name>");
                 out.println("    <servlet-class>org.gridlab.gridsphere.provider.portlet.jsr.PortletServlet</servlet-class>");
                 out.println("  </servlet>");
                 out.println("  <servlet-mapping>");
                 out.println("    <servlet-name>PortletServlet</servlet-name>");
                 out.println("    <url-pattern>/jsr/" + war + "</url-pattern>");
                 out.println("  </servlet-mapping>");
                 out.println(line);
                 hasServletEntry = true;
             } else
             if ((line.endsWith("</web-app>")) && (!hasServletEntry)) {
                 out.println("  <servlet>");
                 out.println("    <servlet-name>PortletServlet</servlet-name>");
                 out.println("    <servlet-class>org.gridlab.gridsphere.provider.portlet.jsr.PortletServlet</servlet-class>");
                 out.println("  </servlet>");
                 out.println("  <servlet-mapping>");
                 out.println("    <servlet-name>PortletServlet</servlet-name>");
                 out.println("    <url-pattern>/jsr/" + war + "</url-pattern>");
                 out.println("  </servlet-mapping>");
                 out.println("</web-app>");
             } else if ((domapping) && (line.endsWith("<session-config>") || (line.endsWith("<web-app>")))) {
                 out.println("  <servlet-mapping>");
                 out.println("    <servlet-name>PortletServlet</servlet-name>");
                 out.println("    <url-pattern>/jsr/" + war + "</url-pattern>");
                 out.println("  </servlet-mapping>");
                 out.println(line);
                 hasServletEntry = true;
             }  else {
                 out.println(line);
             }
         }
         out.close();
         bis.close();
 
         // Open the given file.
 
         FileInputStream file = new FileInputStream("tmp.xml");
         byte[] buffer = new byte[1024];
         int bytesRead;
 
         try {
 
 // Read the file and write it to the jar.
 
             while ((bytesRead = file.read(buffer)) != -1) {
                 tempJar.write(buffer, 0, bytesRead);
             }
 
             System.out.println(entry.getName() + " added.");
         }
         finally {
             file.close();
         }
 
     }
 
 
     public void addGridSphereJSRDescriptor(JarOutputStream tempJar) throws IOException {
 
         String fileName = "config/template/gridsphere-portlet-jsr.xml";
         byte[] buffer = new byte[1024];
         int bytesRead;
 
 
         // Open the given file.
 
         FileInputStream file = new FileInputStream(fileName);
 
         try {
 // Create a jar entry and add it to the temp jar.
 
             JarEntry entry = new JarEntry("WEB-INF/gridsphere-portlet.xml");
             tempJar.putNextEntry(entry);
 
 // Read the file and write it to the jar.
 
             while ((bytesRead = file.read(buffer)) != -1) {
                 tempJar.write(buffer, 0, bytesRead);
             }
 
             System.out.println(entry.getName() + " added.");
 
         }
         finally {
             file.close();
         }
     }
 
     public void addGridSphereJSRLibs(JarOutputStream tempJar) throws IOException {
 
         String fileName = "build/lib/gridsphere-jsr-tags.jar";
         byte[] buffer = new byte[1024];
         int bytesRead;
 
 
         // Open the given file.
 
         FileInputStream file = new FileInputStream(fileName);
 
         try {
 // Create a jar entry and add it to the temp jar.
 
             JarEntry entry = new JarEntry("WEB-INF/lib/gridsphere-jsr-tags.jar");
             tempJar.putNextEntry(entry);
 
 // Read the file and write it to the jar.
 
             while ((bytesRead = file.read(buffer)) != -1) {
                 tempJar.write(buffer, 0, bytesRead);
             }
 
             System.out.println(entry.getName() + " added.");
 
         }
         finally {
             file.close();
         }
     }
 
     public void addGridSphereUILibs(JarOutputStream tempJar) throws IOException {
 
         String fileName = "build/lib/gridsphere-ui-tags.jar";
         byte[] buffer = new byte[1024];
         int bytesRead;
 
 
         // Open the given file.
 
         FileInputStream file = new FileInputStream(fileName);
 
         try {
 // Create a jar entry and add it to the temp jar.
 
             JarEntry entry = new JarEntry("WEB-INF/lib/gridsphere-ui-tags.jar");
             tempJar.putNextEntry(entry);
 
 // Read the file and write it to the jar.
 
             while ((bytesRead = file.read(buffer)) != -1) {
                 tempJar.write(buffer, 0, bytesRead);
             }
 
             System.out.println(entry.getName() + " added.");
 
         }
         finally {
             file.close();
         }
     }
 
     public void collectPortletNames(InputStream portletxmlStream) throws IOException {
         BufferedReader bis = new BufferedReader(new InputStreamReader(portletxmlStream));
         String line = null;
         boolean done = false;
         String portlet = "";
         while (((line = bis.readLine()) != null) && (!done)) {
             //System.err.println("portlet= " + line);
             if (line.indexOf("<portlet-class>") > 0) {
                 int d = line.indexOf("<portlet-class>");
                 String p = line.substring(d + "<portlet-class>".length());
                 int e = p.indexOf("</portlet-class>");
                 portlet = p.substring(0, e);
                 portlets.add(portlet);
                 done = true;
             }
         }
     }
 }
