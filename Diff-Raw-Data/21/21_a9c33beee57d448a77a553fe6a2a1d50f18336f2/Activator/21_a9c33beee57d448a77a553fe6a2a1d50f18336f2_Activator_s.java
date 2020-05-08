 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.aggregator.engine.maven.indexer;
 
 import org.codehaus.plexus.ContainerConfiguration;
 import org.codehaus.plexus.DefaultContainerConfiguration;
 import org.codehaus.plexus.DefaultPlexusContainer;
 import org.codehaus.plexus.PlexusContainer;
 import org.codehaus.plexus.classworlds.ClassWorld;
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * @author filip.hrbek@cloudsmith.com
  * 
  */
 public class Activator extends Plugin {
 
 	private static Activator plugin;
 
 	public static Activator getPlugin() {
 		return plugin;
 	}
 
 	private PlexusContainer plexus;
 
 	public Activator() {
 		plugin = this;
 	}
 
 	public PlexusContainer getPlexusContainer() {
 		return plexus;
 	}
 
 	public void start(BundleContext context) throws Exception {
		// TODO Unfortunately, this container does not see org/eclipse/jetty/client/security/Authorization
		// from org.sonatype.nexus.index.updater.DefaultIndexUpdater. What's needed?
		// This helps, however, it needs to modify the m2eclipse plugins:
		// 1)
		// Add following exported packages to the
		// org.maven.ide.eclipse.maven_embedder_0.10.0.20100209-0800/META-INF/MANIFEST.MF:
		// org.eclipse.jetty.client,org.eclipse.jetty.client.security,org.eclipse.jetty.client.webdav,
		// org.eclipse.jetty.http,org.eclipse.jetty.http.secutiry,org.eclipse.jetty.http.ssl,org.eclipse.jetty.io,
		// org.eclipse.jetty.io.bio,org.eclipse.jetty.io.nio,org.eclipse.jetty.util,
		// org.eclipse.jetty.util.ajax,org.eclipse.jetty.util.component,org.eclipse.jetty.util.log,org.eclipse.jetty.util.resource,
		// org.eclipse.jetty.util.thread
		// 2)
		// Add following line to the org.maven.ide.eclipse.maven_embedder_0.10.0.20100209-0800/META-INF/MANIFEST.MF:
		// Eclipse-BuddyPolicy: registered
		// 3)
		// Add following line to the org.maven.ide.eclipse.nexus_indexer_0.10.0.20100209-0800/META-INF/MANIFEST.MF:
		// Eclipse-RegisterBuddy: org.maven.ide.eclipse.maven_embedder

 		ContainerConfiguration cc = new DefaultContainerConfiguration().setClassWorld(
				new ClassWorld("b3.aggregator", Activator.class.getClassLoader())).setName("plexus");
 		plexus = new DefaultPlexusContainer(cc);
 	}
 
 	public void stop(BundleContext context) {
 		plexus.dispose();
 		plexus = null;
 	}
 }
