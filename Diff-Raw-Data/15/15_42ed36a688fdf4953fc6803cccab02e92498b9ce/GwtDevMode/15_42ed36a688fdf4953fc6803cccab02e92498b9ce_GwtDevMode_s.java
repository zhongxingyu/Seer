 /**
  * 
  */
 package com.agiliumlabs.arquillian.gwt;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import org.mortbay.jetty.webapp.WebAppContext;
 
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.dev.BootStrapPlatform;
 import com.google.gwt.dev.DevMode;
 import com.google.gwt.dev.shell.jetty.JettyLauncher;
 
 /**
  * @author roman
  *
  */
 public class GwtDevMode extends DevMode {
 
 	private WebAppContext webAppContext;
 	
 	@Override
 	public void doShutDownServer() {
 		super.doShutDownServer();
 	}
 	
 	public void start(GwtDevModeDeploymentConfiguration config, GwtArchive archive) throws IOException {
 		String[] baseArgs = new String[] {"-port", Integer.toString(config.getPort()), "-logdir", config.getLogDir(), "-logLevel", config.getLogLevel().name(), "-bindAddress", config.getBindAddress(), "-codeServerPort", Integer.toString(config.getCodeServerPort()), "-war", config.getWar()};
 		String[] args = new String[baseArgs.length + config.getModulesArray().length];
 		System.arraycopy(baseArgs, 0, args, 0, baseArgs.length);
 		System.arraycopy(config.getModulesArray(), 0, args, baseArgs.length, config.getModulesArray().length);
 	    if (new ArgProcessor(super.options).processArgs(args)) {
 			super.options.setServletContainerLauncher(new ContextCaptureServletContainerLauncher());
 			for (File sourceDir : archive.getSources())
 				if (sourceDir.isDirectory())
					addToClasspath(sourceDir);			
 	    	launch();
 	    }
 	}
 
 	public void launch() {
 		// Eager AWT init for OS X to ensure safe coexistence with SWT.
 		BootStrapPlatform.initGui();
 
 		boolean success = startUp();
 		// The web server is running now, so launch browsers for startup urls.
 		ui.moduleLoadComplete(success);
 	}
 	
 	public WebAppContext getWebAppContext() {
 		return webAppContext;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void addToClasspath(File file) throws IOException {
 		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
 		Class<?> sysclass = URLClassLoader.class;
 		try {
 			Method method = sysclass.getDeclaredMethod("addURL", URL.class);
 			method.setAccessible(true);
 			method.invoke(sysloader, new Object[]{file.toURL()});
 		} catch (Throwable t) {
 			t.printStackTrace();
 			throw new IOException("Error, could not add URL to system classloader");
 		}
 	}
 
 	public class ContextCaptureServletContainerLauncher extends JettyLauncher {
 
 		@Override
 		protected WebAppContext createWebAppContext(TreeLogger logger, File appRootDir) {
 			webAppContext = super.createWebAppContext(logger, appRootDir); webAppContext.getServletHandler().getServlets();
 			return webAppContext;
 		}
 
 	}
 	
 }
