 /**
  * Thanks to Victor Tatai from his collaboration at http://snippets.dzone.com/posts/show/4831
  * Thanks to Bill Burke from his his enlightening content at 
  *   http://bill.burkecentral.com/2008/01/14/scanning-java-annotations-at-runtime/
  * 
  * Copyright 2012 Miere Liniel Teixeira
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package layr;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.naming.NamingException;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.xml.parsers.ParserConfigurationException;
 
 import layr.components.IComponent;
 import layr.components.IComponentFactory;
 import layr.components.xhtml.Html;
 import layr.resources.AbstractSystemResourceLoader;
 import layr.resources.ClassPathResourceLoader;
 import layr.resources.ServletContextResourceLoader;
 import layr.util.EnterpriseJavaBeans;
 
 import org.xml.sax.SAXException;
 
 public class ApplicationContext {
 
     public static final String LAYR_CACHE_ENABLED = "layr.cache.enabled";
 
 	private HashMap<String, IComponent> resourceCache;
 	private ServletContext servletContext;
 
 	private AbstractSystemResourceLoader[] applicationContexts;
 	private Map<String, IComponentFactory> registeredTagLibs;
 	private Map<String, Object> registeredWebResources;
 	private EnterpriseJavaBeans ejbManager;
 	private Set<String> availableLocalResourceFiles;
 	private Configuration layrConfiguration;
 
 	public ApplicationContext() throws NamingException {
 		resourceCache = new HashMap<String, IComponent>();
 	}
 
 	/**
 	 * Compiles the resource. For performance improvements it stores earlier
 	 * compiled resources in a cached factory of components.
 	 * 
 	 * @param teplateName
 	 * @param layrContext
 	 * @return IComponent implementation of compiled template
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws CloneNotSupportedException
 	 * @throws ServletException 
 	 */
 	public IComponent compile(String templateName, RequestContext context)
 			throws IOException, ParserConfigurationException, SAXException,
 			CloneNotSupportedException, ServletException {
 		if (isCacheEnabled() && resourceCache.containsKey(templateName))
 			return (IComponent) resourceCache.get(templateName).clone(context);
 
 		InputStream template = getResourceAsStream(templateName);
 		if (template == null)
 			return null;
 
 		try{
 			TemplateParser parser = new TemplateParser(context);
 			IComponent application = parser.parse(template);
 
 	        if ( parser.getDoctype() != null )
 	            application.setAttribute(Html.DOCTYPE_ATTRIBUTE, parser.getDoctype());
 	        cacheCompiledPage(templateName, application);
 			return application;

		} catch (Throwable e){
			throw new ServletException("Can't parse '" + templateName + "' as XHTML.", e);
 		}
 	}
 
 	/**
 	 * @param templateName
 	 * @param application
 	 */
 	public void cacheCompiledPage(String templateName, IComponent application) {
 		synchronized (resourceCache) {
 			resourceCache.put(templateName, application);
 		}
 	}
 
 	/**
 	 * Retrieves dynamically a resource as Stream from a resourceName.
 	 * 
 	 * @param resourceName
 	 * @return
 	 * @throws IOException
 	 */
 	public InputStream getResourceAsStream(String resourceName)
 			throws IOException {
 		InputStream stream = null;
 		resourceName = resourceName.replaceAll("^/+", "");
 
 		Set<String> resourceFiles = getAvailableLocalResourceFiles();
 		if (resourceFiles != null)
 			for (String url : resourceFiles) {
 				String parsedUrl = url.replaceAll("^/+", "");
 				if (parsedUrl.equals(resourceName)) {
 					stream = openStream(parsedUrl);
 					if (stream != null)
 						return stream;
 				}
 			}
 
 		return stream;
 	}
 
 	/**
 	 * Get a resource and read it as a StringBufffer.
 	 * 
 	 * @param fileName
 	 * @return
 	 * @throws IOException
 	 */
 	public StringBuilder getResourceAsStringBuilder(String fileName) throws IOException {
 		InputStream stream = getResourceAsStream(fileName);
 		StringBuilder buffer = new StringBuilder();
 		
 		if (stream == null)
 			throw new FileNotFoundException("Can't read filename '" + fileName + "'");
 
 		byte[] b = new byte[4096];
 		for (int n; (n = stream.read(b)) != -1;) {
 			buffer.append(new String(b, 0, n));
 		}
 
 		return buffer;
 	}
 
 	/**
 	 * @param url
 	 * @return
 	 */
 	public InputStream openStream(String url) {
 		InputStream stream = getClassLoader().getResourceAsStream(url);
 		if (stream == null && getServletContext() != null)
 			stream = getServletContext().getResourceAsStream("/" + url);
 		return stream;
 	}
 
 	public ClassLoader getClassLoader() {
 		return Thread.currentThread().getContextClassLoader();
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean isCacheEnabled() {
 		return getLayrConfiguration().isCacheEnabled();
 	}
 
 	public void setServletContext(ServletContext servletContext) {
 		this.servletContext = servletContext;
 	}
 
 	public ServletContext getServletContext() {
 		return servletContext;
 	}
 
 	public Map<String, IComponentFactory> getRegisteredTagLibs() {
 		if (registeredTagLibs == null)
 			registeredTagLibs = new HashMap<String, IComponentFactory>();
 		return registeredTagLibs;
 	}
 
 	public void setRegisteredTagLibs(Map<String, IComponentFactory> registeredTagLibs) {
 		this.registeredTagLibs = registeredTagLibs;
 	}
 
 	public void setApplicationContexts(AbstractSystemResourceLoader[] applicationContexts) {
 		this.applicationContexts = applicationContexts;
 	}
 
 	public AbstractSystemResourceLoader[] getApplicationContexts() {
 		if (applicationContexts == null) {
 			initializeApplicationContexts();
 		}
 		return applicationContexts;
 	}
 
 	public void initializeApplicationContexts() {
 		applicationContexts = new AbstractSystemResourceLoader[] {
 				new ServletContextResourceLoader(servletContext, getClassLoader()),
 				new ClassPathResourceLoader(getClassLoader())
 			};
 		
 		setAvailableLocalResourceFiles(new TreeSet<String>());
 		for (AbstractSystemResourceLoader loader : getApplicationContexts())
 			try {
 				for (String url : loader.retrieveAvailableResources())
 					getAvailableLocalResourceFiles().add(url);
 			} catch (IOException e) {
 				continue;
 			}
 	}
 
 	public Map<String, Object> getRegisteredWebResources() {
 		if (registeredWebResources == null)
 			registeredWebResources = new HashMap<String, Object>();
 		return registeredWebResources;
 	}
 
 	public void setRegisteredWebResources(Map<String, Object> registeredWebResources) {
 		this.registeredWebResources = registeredWebResources;
 	}
 
 	public EnterpriseJavaBeans getEjbManager() {
 		return ejbManager;
 	}
 
 	public void setEjbManager(EnterpriseJavaBeans ejbManager) {
 		this.ejbManager = ejbManager;
 	}
 
 	public Set<String> getAvailableLocalResourceFiles() {
 		return availableLocalResourceFiles;
 	}
 
 	public void setAvailableLocalResourceFiles(
 			Set<String> availableLocalResourceFiles) {
 		this.availableLocalResourceFiles = availableLocalResourceFiles;
 	}
 
 	public Configuration getLayrConfiguration() {
 		if ( layrConfiguration == null )
 			layrConfiguration = new Configuration();
 		return layrConfiguration;
 	}
 
 	public void setLayrConfiguration(Configuration layrConfiguration) {
 		this.layrConfiguration = layrConfiguration;
 	}
 	
 	public void clearCache() {
 		applicationContexts = null;
 		registeredTagLibs.clear();
 		registeredTagLibs = null;
 		
 		registeredWebResources.clear();
 		registeredWebResources = null;
 		
 		ejbManager.getRegisteredEJBViews().clear();
 		ejbManager.setRegisteredEJBViews(null);
 		ejbManager = null;
 
 		availableLocalResourceFiles.clear();
 		availableLocalResourceFiles = null;
 	}
 }
