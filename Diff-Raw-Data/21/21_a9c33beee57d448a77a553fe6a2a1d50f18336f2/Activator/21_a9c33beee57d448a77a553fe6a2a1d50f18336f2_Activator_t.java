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
 		ContainerConfiguration cc = new DefaultContainerConfiguration().setClassWorld(
				new ClassWorld("b3.aggregator", Activator.class.getClassLoader())).setName("b3.plexus");
 		plexus = new DefaultPlexusContainer(cc);
 	}
 
 	public void stop(BundleContext context) {
 		plexus.dispose();
 		plexus = null;
 	}
 }
