 /*
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
  */
 package org.jdesktop.wonderland.webserver;
 
 import org.glassfish.embed.*;
 import com.sun.enterprise.deploy.shared.ArchiveFactory;
 import com.sun.enterprise.module.ModuleDefinition;
 import com.sun.enterprise.module.ModuleDependency;
 import com.sun.enterprise.module.ModuleMetadata;
 import com.sun.enterprise.module.ModulesRegistry;
 import com.sun.hk2.component.InhabitantsParser;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.LinkedHashSet;
 import java.util.Properties;
 import java.util.Set;
 import java.util.jar.Manifest;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.glassfish.api.deployment.archive.ReadableArchive;
 import org.jdesktop.wonderland.webserver.launcher.WebServerLauncher;
 import org.jvnet.hk2.component.Habitat;
 
 /**
  *
  * @author jkaplan
  */
 public class WonderlandAppServer extends Server {
     private static final Logger logger =
             Logger.getLogger(WonderlandAppServer.class.getName());
 
     private boolean deployable = false;
     private WonderlandBootstrap bootstrap;
 
     public WonderlandAppServer(EmbeddedInfo info)
             throws EmbeddedException
     {
         super(info);
 
         bootstrap = new WonderlandBootstrap(this);
     }
 
     public synchronized boolean isDeployable() {
         return deployable;
     }
 
     public synchronized void setDeployable(boolean deployable) {
         this.deployable = deployable;
     }
 
     @Override
     protected EmbeddedBootstrap createBootstrap() {
         return bootstrap;
     }
 
     @Override
     public void stop() throws EmbeddedException {
         super.stop();
         setDeployable(false);
     }
 
     public String deploy(File file, Properties props)
             throws EmbeddedException
     {
         try {
             ArchiveFactory af = bootstrap.getHabitat().getComponent(ArchiveFactory.class);
             ReadableArchive ra = af.openArchive(file);
             return deploy(ra, props);
         } catch (IOException ioe) {
             ioe.printStackTrace();
             throw new EmbeddedException(ioe);
         }
     }
 
     public String deploy(File file) throws EmbeddedException {
         return deploy(file, null);
     }
 
     public String deploy(ReadableArchive a, Properties params)
             throws EmbeddedException
     {
         return getDeployer().deploy(a, params);
     }
 
     static class WonderlandBootstrap extends EmbeddedBootstrap {
         private Habitat habitat;
 
         public WonderlandBootstrap(WonderlandAppServer server) {
             super (server);
         }
 
         public Habitat getHabitat() {
             return habitat;
         }
 
         @Override
         protected InhabitantsParser createInhabitantsParser(Habitat parentHabitat) {
             InhabitantsParser out = super.createInhabitantsParser(parentHabitat);
 
             this.habitat = out.habitat;
 
             // add a module
             ModulesRegistry mr = out.habitat.getComponent(ModulesRegistry.class);
             mr.add(new ClasspathModuleDefinition(WebServerLauncher.getClassLoader()));
 
             return out;
         }
     }
 
     static class ClasspathModuleDefinition implements ModuleDefinition {
 
         private static final String[] EMPTY_STRING_ARR = new String[0];
         private static final ModuleDependency[] EMPTY_DEPEND_ARR = new ModuleDependency[0];
         private final ModuleMetadata metadata = new ModuleMetadata();
         private final Manifest manifest = new Manifest();
         private URLClassLoader classLoader;
 
         public ClasspathModuleDefinition(URLClassLoader classLoader) {
             this.classLoader = classLoader;
         }
 
         public String getName() {
             return "Wonderland web server classpath";
         }
 
         public String[] getPublicInterfaces() {
             return EMPTY_STRING_ARR;
         }
 
         public ModuleDependency[] getDependencies() {
             return EMPTY_DEPEND_ARR;
         }
 
         public URI[] getLocations() {
             URL[] in = classLoader.getURLs();
             Set<URI> out = new LinkedHashSet<URI>(in.length);
 
             for (URL u : in) {
                 try {
                     out.add(u.toURI());
                 } catch (URISyntaxException use) {
                     logger.log(Level.WARNING, "Error creating URI: " + u, use);
                 }
             }
 
             return out.toArray(new URI[0]);
         }
 
         public String getVersion() {
             return "1.0.0";
         }
 
         public String getImportPolicyClassName() {
             return null;
         }
 
         public String getLifecyclePolicyClassName() {
             return null;
         }
 
         public Manifest getManifest() {
             return manifest;
         }
 
         public ModuleMetadata getMetadata() {
             return metadata;
         }
     }
 }
