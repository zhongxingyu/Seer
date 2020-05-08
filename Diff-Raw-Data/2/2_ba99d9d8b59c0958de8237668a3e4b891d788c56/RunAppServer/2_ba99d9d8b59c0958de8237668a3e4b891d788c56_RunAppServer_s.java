 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.webserver;
 
 import org.jdesktop.wonderland.utils.RunUtil;
 import org.jdesktop.wonderland.webserver.launcher.WebServerLauncher;
 import com.sun.hk2.component.InhabitantsParser;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 import org.glassfish.api.deployment.archive.ReadableArchive;
 import org.glassfish.embed.App;
 import org.glassfish.embed.AppServer;
 import org.glassfish.embed.EmbeddedException;
 import org.glassfish.embed.EmbeddedHttpListener;
 import org.glassfish.embed.EmbeddedVirtualServer;
 import org.glassfish.server.ServerEnvironmentImpl;
 import org.jdesktop.wonderland.modules.service.AddedModule;
 import org.jdesktop.wonderland.modules.service.ModuleManager;
 import org.jdesktop.wonderland.utils.SystemPropertyUtil;
 import org.jvnet.hk2.component.Habitat;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  *
  * @author jkaplan
  */
 public class RunAppServer {
     // logger
     private static final Logger logger =
             Logger.getLogger(RunAppServer.class.getName());
     
     // singleton instance
     private static WonderlandAppServer appServer;
     private static int port;
 
     static {
         String portStr = SystemPropertyUtil.getProperty(WebServerLauncher.WEBSERVER_PORT_PROP,
                                                         "8080");
         port = Integer.parseInt(portStr);
     }
     
     public RunAppServer() throws IOException {
         // install the default modules
         installModules();
         
         // now deploy web apps
         deployWebApps();
     }
 
     private void deployWebApps() throws IOException {
         WonderlandAppServer as = getAppServer();
                 
         // copy files to document root of web server
         InputStream is = WebServerLauncher.class.getResourceAsStream("/META-INF/docroot.files");
         BufferedReader in = in = new BufferedReader(new InputStreamReader(is));
         
        File docDir = new File(RunUtil.getRunDir(), "docroot");
         docDir.mkdirs();
         
         String line;
         while ((line = in.readLine()) != null) {
             InputStream fileIs = WebServerLauncher.class.getResourceAsStream(line);
             if (line.lastIndexOf("/") != -1) {
                 line = line.substring(line.lastIndexOf("/"));
             }
             RunUtil.writeToFile(fileIs, new File(docDir, line));
         }
         
         // read the list of .war files to deploy
         is = WebServerLauncher.class.getResourceAsStream("/META-INF/deploy.jars");
         in = new BufferedReader(new InputStreamReader(is));
 
         // write to a subdirectory of the default temp directory
         File deployDir = new File(RunUtil.getRunDir(), "deploy");
         deployDir.mkdirs();
         
         while ((line = in.readLine()) != null) {
             File f = RunUtil.extractJar(getClass(), line, deployDir);
             try {
                 as.deploy(f);
             } catch (Exception excp) {
                 // ignore any exception and continue
             }
         }
     }
     
     private void installModules() throws IOException {
         // read the list of .war files to deploy
         InputStream is = WebServerLauncher.class.getResourceAsStream("/META-INF/module.jars");
         BufferedReader in = new BufferedReader(new InputStreamReader(is));
     
         // extract modules to a directory, and make a list of the extracted
         // modules
         File moduleDir = ModuleManager.getModuleManager().getModuleStateDirectory(ModuleManager.State.ADD);
         Collection<AddedModule> modules = new ArrayList<AddedModule>();
         
         String line;
         while ((line = in.readLine()) != null) {
             File f = RunUtil.extract(getClass(), line, moduleDir);
             modules.add(new AddedModule(f));
         }
         
         // add all modules at once to the module manager.  This will ensure
         // that dependency checks take all modules into account.
         ModuleManager.getModuleManager().addAll(modules, true);
     }
     
     // get the main instance
     private synchronized static WonderlandAppServer getAppServer() {
         if (appServer == null) {
             appServer = new WonderlandAppServer(port);
         }
 
         return appServer;
     }
     
     static class WonderlandAppServer extends AppServer {
         public WonderlandAppServer(int port) {
             super (port);
         }
         
         public Habitat getHabitat() {
             return habitat;
         }
         
         public DirServerEnvironment getServerEnvironment() {
             return (DirServerEnvironment) this.env;
         }
         
         @Override
         protected InhabitantsParser decorateInhabitantsParser(InhabitantsParser parser) {
             InhabitantsParser out = super.decorateInhabitantsParser(parser);
             parser.replace(ServerEnvironmentImpl.class, DirServerEnvironment.class);
             return out;
         }
 
         @Override
         public App deploy(File archive) throws IOException {
             // override because we know this will always be a
             // a directory, and we don't want it extracted to
             // the current working directory
             ReadableArchive a = archiveFactory.openArchive(archive);
             return deploy(a);
         }
 
         @Override
         public EmbeddedVirtualServer createVirtualServer(final EmbeddedHttpListener listener) {
             EmbeddedVirtualServer out = super.createVirtualServer(listener);
 
             try {
                 File docRoot = new File(RunUtil.getRunDir(), "docRoot");
                 docRoot.mkdirs();
 
                 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                 Document d = dbf.newDocumentBuilder().parse(domainXmlUrl.toExternalForm());
                 XPath xpath = XPathFactory.newInstance().newXPath();
 
                 Element v = (Element) xpath.evaluate("//http-service/virtual-server/property[@name='docroot']", d, XPathConstants.NODE);
                 v.setAttribute("value", docRoot.getCanonicalPath());
 
                 /**
                  * Write domain.xml to a temporary file. UGLY UGLY UGLY.
                  */
                 File domainFile = File.createTempFile("wonderlanddomain", "xml");
                 domainFile.deleteOnExit();
                 Transformer t = TransformerFactory.newInstance().newTransformer();
                 t.transform(new DOMSource(d), new StreamResult(domainFile));
                 domainXmlUrl = domainFile.toURI().toURL();
             } catch (Exception ex) {
                 throw new EmbeddedException(ex);
             }
 
             return out;
         }
     }
 }
