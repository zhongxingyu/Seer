 package evymind.vapor.deploy.providers;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import evyframework.common.io.Resource;
 import evymind.vapor.app.AppContext;
 import evymind.vapor.deploy.App;
 import evymind.vapor.deploy.utils.FileID;
 import evymind.vapor.server.handler.ContextHandler;
 
 
 /**
  * Context directory Standard App Provider.
  * <p>
  * Apps with names root or starting with root- are deployed at /. If the name is in the format root-hostname, then
  * the app is deployed at / in the virtual host hostname.
  */
 public class DefaultAppProvider extends ScanningAppProvider {
 	
 	private boolean extractSars = false;
 	private boolean parentLoaderPriority = false;
 	private String defaultsDescriptor;
 	private Filter filter;
 	private File tempDirectory;
 	private String[] configurationClasses;
 
 	public static class Filter implements FilenameFilter {
 		private File contexts;
 
 		public boolean accept(File dir, String name) {
 			if (!dir.exists()) {
 				return false;
 			}
 			String lowername = name.toLowerCase();
 
 			File file = new File(dir, name);
 			// is it not a directory and not a war ?
 			if (!file.isDirectory() && !lowername.endsWith(".sar")) {
 				return false;
 			}
 
 			// ignore hidden files
 			if (lowername.startsWith("."))
 				return false;
 
 			if (file.isDirectory()) {
 				// is it a directory for an existing war file?
 				if (new File(dir, name + ".sar").exists() || new File(dir, name + ".SAR").exists())
 
 					return false;
 
 				// is it a sccs dir?
 				if ("cvs".equals(lowername) || "cvsroot".equals(lowername))
 					return false;
 			}
 
 			// is there a contexts config file
 			if (this.contexts != null) {
 				String context = name;
 				if (!file.isDirectory()) {
 					context = context.substring(0, context.length() - 4);
 				}
 				if (new File(this.contexts, context + ".xml").exists() || new File(this.contexts, context + ".XML").exists()) {
 					return false;
 				}
 			}
 
 			return true;
 		}
 	}
 
 
 	public DefaultAppProvider() {
 		super(new Filter());
 		this.filter = (Filter) _filenameFilter;
 		setScanInterval(0);
 	}
 
 
 	/**
 	 * Get the extractSars.
 	 * 
 	 * @return the extractSars
 	 */
 	public boolean isExtractSars() {
 		return this.extractSars;
 	}
 
 
 	/**
 	 * Set the extractSars.
 	 * 
 	 * @param extractSars
 	 *            the extractSars to set
 	 */
 	public void setExtractSars(boolean extractSars) {
 		this.extractSars = extractSars;
 	}
 
 
 	/**
 	 * Get the parentLoaderPriority.
 	 * 
 	 * @return the parentLoaderPriority
 	 */
 	public boolean isParentLoaderPriority() {
 		return this.parentLoaderPriority;
 	}
 
 
 	/**
 	 * Set the parentLoaderPriority.
 	 * 
 	 * @param parentLoaderPriority
 	 *            the parentLoaderPriority to set
 	 */
 	public void setParentLoaderPriority(boolean parentLoaderPriority) {
 		this.parentLoaderPriority = parentLoaderPriority;
 	}
 
 
 	/**
 	 * Get the defaultsDescriptor.
 	 * 
 	 * @return the defaultsDescriptor
 	 */
 	public String getDefaultsDescriptor() {
 		return this.defaultsDescriptor;
 	}
 
 
 	/**
 	 * Set the defaultsDescriptor.
 	 * 
 	 * @param defaultsDescriptor
 	 *            the defaultsDescriptor to set
 	 */
 	public void setDefaultsDescriptor(String defaultsDescriptor) {
 		this.defaultsDescriptor = defaultsDescriptor;
 	}
 
 
 	public String getContextDir() {
 		return this.filter.contexts == null ? null : this.filter.contexts.toString();
 	}
 
 
 	/**
 	 * Set the directory in which to look for context config files.
 	 * <p>
 	 * If a app call "foo/" or "foo.sar" is discovered in the monitored directory, then the ContextDir is examined
 	 * to see if a foo.xml file exists. If it does, then this deployer will not deploy the app and the
 	 * ContextProvider should be used to act on the foo.xml file.
 	 * 
 	 * @see ContextProvider
 	 * @param contextsDir
 	 */
 	public void setContextDir(String contextsDir) {
 		try {
 			this.filter.contexts = resolver.getResource(contextsDir).getFile();
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 
 	/**
 	 * @param configurations
 	 *            The configuration class names.
 	 */
 	public void setConfigurationClasses(String[] configurations) {
 		this.configurationClasses = configurations == null ? null : (String[]) configurations.clone();
 	}
 
 
 	/**
      * 
      */
 	public String[] getConfigurationClasses() {
 		return this.configurationClasses;
 	}
 
 	/**
 	 * Set the Work directory where unpacked SAR files are managed from.
 	 * <p>
 	 * Default is the same as the <code>java.io.tmpdir</code> System Property.
 	 * 
 	 * @param directory
 	 *            the new work directory
 	 */
 	public void setTempDir(File directory) {
 		this.tempDirectory = directory;
 	}
 
 	/**
 	 * Get the user supplied Work Directory.
 	 * 
 	 * @return the user supplied work directory (null if user has not set Temp Directory yet)
 	 */
 	public File getTempDir() {
 		return this.tempDirectory;
 	}
 
 
 	public ContextHandler createContextHandler(final App app) throws Exception {
 		Resource resource = resolver.getResource(app.getOriginId());
 		File file = resource.getFile();
 		if (!resource.exists())
 			throw new IllegalStateException("App resouce does not exist " + resource);
 
 		String context = file.getName();
 
 		if (file.isDirectory()) {
 			// must be a directory
 		} else if (FileID.isAppArchiveFile(file)) {
 			// Context Path is the same as the archive.
 			context = context.substring(0, context.length() - 4);
 		} else {
 			throw new IllegalStateException("unable to create ContextHandler for " + app);
 		}
 
 		// Ensure "/" is Not Trailing in context paths.
 		if (context.endsWith("/") && context.length() > 0) {
 			context = context.substring(0, context.length() - 1);
 		}
 
 		// Start building the application
 		AppContext sah = new AppContext();
 		sah.setDisplayName(context);
 
 		// special case of archive (or dir) named "root" is / context
 		if (context.equalsIgnoreCase("root")) {
 			context = "/";
 		}
 
 		// Ensure "/" is Prepended to all context paths.
 		if (context.charAt(0) != '/') {
 			context = "/" + context;
 		}
 
 		sah.setContextPath(context);
		sah.setSar(file.getAbsolutePath());
 		if (this.defaultsDescriptor != null) {
 			sah.setDefaultsDescriptor(this.defaultsDescriptor);
 		}
 		sah.setExtractSAR(this.extractSars);
 		sah.setParentLoaderPriority(this.parentLoaderPriority);
 		if (this.configurationClasses != null) {
 			sah.setConfigurationClasses(this.configurationClasses);
 		}
 
 		if (this.tempDirectory != null) {
 			/*
 			 * Since the Temp Dir is really a context base temp directory, Lets set the Temp Directory in a way similar
 			 * to how WebInfConfiguration does it, instead of setting the AppContext.setTempDirectory(File). If we used
 			 * .setTempDirectory(File) all webapps will wind up in the same temp / work directory, overwriting each
 			 * others work.
 			 */
 			sah.setAttribute(AppContext.BASETEMPDIR, this.tempDirectory);
 		}
 		return sah;
 	}
 
 }
