 //$URL: http://feanor:8050/svn/test/trunk/DevTest/apache-sling/adaptto/sling-cxf-integration/helloworld-application/src/main/java/adaptto/slingcxf/server/util/AbstractJaxWsServer.java $
 //$Id: AbstractJaxWsServer.java 680 2011-09-12 16:57:25Z PRO-VISION\SSeifert $
 package org.liveSense.service.cxf;
 
import java.io.IOException;

import javax.servlet.FilterChain;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.cxf.Bus;
 import org.apache.cxf.BusException;
 import org.apache.cxf.BusFactory;
 import org.apache.cxf.common.classloader.ClassLoaderUtils;
 import org.apache.cxf.common.classloader.ClassLoaderUtils.ClassLoaderHolder;
 import org.apache.cxf.resource.ResourceManager;
 import org.apache.cxf.transport.DestinationFactory;
 import org.apache.cxf.transport.DestinationFactoryManager;
 import org.apache.cxf.transport.http.AbstractHTTPDestination;
 import org.apache.cxf.transport.http.DestinationRegistry;
 import org.apache.cxf.transport.http.HTTPTransportFactory;
 import org.apache.cxf.transport.servlet.AbstractHTTPServlet;
 import org.apache.cxf.transport.servlet.ServletContextResourceResolver;
 import org.apache.cxf.transport.servlet.ServletController;
 import org.apache.cxf.transport.servlet.servicelist.ServiceListGeneratorServlet;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.ReferenceCardinality;
 import org.apache.felix.scr.annotations.ReferencePolicy;
 import org.apache.sling.auth.core.AuthenticationSupport;
 import org.liveSense.core.service.OSGIClassLoaderManager;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.packageadmin.PackageAdmin;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Deprecated
 
 /**
  * Abstract servlet-based implementation for CXF-based SOAP services. Ensures
  * that correct class loader is used is during initialization and invoking
  * phases. Via getCurrentRequest() and getCurrentResponse() it is possible to
  * access these objects from SOAP method implementations.
  * @Deprecated
  * Use @WebServiceMarkerInterface and declaretive service instead. 
  * Example: https://github.com/liveSense/org.liveSense.sample.webServiceServlet/tree/master/src/main/java/org/liveSense/sample/WebServiceServlet/ds
  */
 @Component(componentAbstract=true, metatype=true)
 public abstract class AbstractWsServer extends AbstractHTTPServlet {
 	private static final long serialVersionUID = 1L;
 
 	@Reference(bind="bindAuth", unbind="unbindAuth")
 	AuthenticationSupport auth = null;
 
 	@Reference(cardinality=ReferenceCardinality.MANDATORY_UNARY, policy=ReferencePolicy.DYNAMIC)
 	PackageAdmin packageAdmin;
 
 	@Reference(cardinality=ReferenceCardinality.MANDATORY_UNARY, policy=ReferencePolicy.DYNAMIC)
 	OSGIClassLoaderManager dynamicClassLoader = null;
 
 	private DestinationRegistry destinationRegistry;
 	private Bus bus;
 	private ServletController controller;
 	private ClassLoader loader;
 	private final boolean loadBus = true;
 
 	private String servletUrl = null;
 
 	/**
 	 * Extension for SOAP requests
 	 */
 	private final Logger log = LoggerFactory.getLogger("SOAP");
 
 	public abstract void callInit() throws Throwable;
 
 	public abstract void callFinal() throws Throwable;
 	
 	BundleContext context = null;
 
 	/**
 	 *
 	 * Allows the extending OSGi service to set its classloader.
 	 *
 	 * @param classLoader The classloader to provide to the SlingRemoteServiceServlet.
 	 */
 
 	protected void setBundleContext(BundleContext context) {
 		this.context = context;
 	}
 	
 	@Override
 	protected void invoke(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException {
 		ThreadLocalRequestContext.getThreadLocal().set(new ThreadLocalRequestContext(pRequest, pResponse, null));
 
 		try {
 			// Authenticating - OSGi context
 			if (auth != null) {
 				auth.handleSecurity(pRequest, pResponse);
 			}
 			try {
 				callInit();
 			} catch (Throwable e) {
 				throw new ServletException("Error on callInit", e);
 			}
 			// super.invoke(new RequestWrapper(pRequest), pResponse);
 
 			ClassLoaderHolder origLoader = null;
 			try {
 				if (loader != null) {
 					origLoader = ClassLoaderUtils.setThreadContextClassloader(loader);
 				}
 				if (bus != null) {
 					BusFactory.setThreadDefaultBus(bus);
 				}
 				controller.invoke(new SoapRequestWrapper(pRequest, servletUrl), pResponse);
 			} finally {
 				BusFactory.setThreadDefaultBus(null);
 				if (origLoader != null) {
 					origLoader.reset();
 				}
 			}
 
 		} finally {
 			try {
 				callFinal();
 			} catch (Throwable e) {
 				log.error("callFinal: ", e);
 			}
 			ThreadLocalRequestContext.getThreadLocal().remove();        		
 		}
 	}
 
 	/**
 	 * @return Servlet request for current threads SOAP request
 	 */
 	protected HttpServletRequest getThreadLocalRequest() {
 		ThreadLocalRequestContext requestContext = ThreadLocalRequestContext.getRequestContext();
 		if (requestContext == null) {
 			throw new IllegalStateException("No current soap request context available.");
 		}
 		return requestContext.getRequest();
 	}
 
 	/**
 	 * @return Servlet response for current threads SOAP request
 	 */
 	protected HttpServletResponse getThreadLocalResponse() {
 		ThreadLocalRequestContext requestContext = ThreadLocalRequestContext.getRequestContext();
 		if (requestContext == null) {
 			throw new IllegalStateException("No current soap request context available.");
 		}
 		return requestContext.getResponse();
 	}
 
 	protected String getUser() {
 		return (String)this.getThreadLocalRequest().getAttribute("org.osgi.service.http.authentication.remote.user");
 	}
 
 
 
 	protected String getServletUrl() {
 		return servletUrl;
 	}
 
 	protected void setServletUrl(String servletUrl) {
 		this.servletUrl = servletUrl;
 	}
 
 	/**
 	 * @return Interface of SOAP service
 	 */
 	protected abstract Class getServerInterfaceType();
 
 	protected void bindAuth(AuthenticationSupport auth) {
 		this.auth = auth;
 	}
 
 	protected void unbindAuth(AuthenticationSupport auth) {
 		this.auth = null;
 	}
 
 	@Override
 	public void init(ServletConfig sc) throws ServletException {
 		super.init(sc);
 		if (this.bus == null && loadBus) {
 			loadBus(sc);
 		}
 		if (this.bus != null) {
 			loader = bus.getExtension(ClassLoader.class);
 			ResourceManager resourceManager = bus.getExtension(ResourceManager.class);
 			resourceManager.addResourceResolver(new ServletContextResourceResolver(
 					sc.getServletContext()));
 			if (destinationRegistry == null) {
 				this.destinationRegistry = getDestinationRegistryFromBus(this.bus);
 			}
 		}
 
 		this.controller = createServletController(sc);
 	}
 
 	private static DestinationRegistry getDestinationRegistryFromBus(Bus bus) {
 		DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
 		try {
 			DestinationFactory df = dfm
 					.getDestinationFactory("http://cxf.apache.org/transports/http/configuration");
 			if (df instanceof HTTPTransportFactory) {
 				HTTPTransportFactory transportFactory = (HTTPTransportFactory)df;
 				return transportFactory.getRegistry();
 			}
 		} catch (BusException e) {
 			// why are we throwing a busexception if the DF isn't found?
 		}
 		return null;
 	}
 
 	protected void loadBus(ServletConfig sc) {
 		this.bus = BusFactory.newInstance().createBus();
 	}
 
 	private ServletController createServletController(ServletConfig servletConfig) {
 		HttpServlet serviceListGeneratorServlet = 
 				new ServiceListGeneratorServlet(destinationRegistry, bus);
 		ServletController newController =
 				new ServletController(destinationRegistry,
 						servletConfig,
 						serviceListGeneratorServlet);        
 		return newController;
 	}
 
	@Override
 	public Bus getBus() {
 		return bus;
 	}
 
 	public void setBus(Bus bus) {
 		this.bus = bus;
 	}
 
 	@Override
 	public void destroy() {
 		for (String path : destinationRegistry.getDestinationsPaths()) {
 			// clean up the destination in case the destination itself can no longer access the registry later
 			AbstractHTTPDestination dest = destinationRegistry.getDestinationForPath(path);
 			synchronized (dest) {
 				destinationRegistry.removeDestination(path);
 				dest.releaseRegistry();
 			}
 		}
 		destinationRegistry = null;
 		destroyBus();
 	}
 
 	public void destroyBus() {
 		if (bus != null) {
 			bus.shutdown(true);
 		}
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
 			throws IOException, ServletException {
 		ClassLoaderHolder origLoader = null;
 		Bus origBus = null;
 		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
 			try {
 				if (bus != null) {
 					origBus = BusFactory.getAndSetThreadDefaultBus(bus);
 				}
 				if (controller.filter((HttpServletRequest)request, (HttpServletResponse)response)) {
 					return;
 				}
 			} finally {
 				if (origLoader != null) {
 					origLoader.reset();
 				}
 			}
 		}
 		chain.doFilter(request, response);
 	}
 }
