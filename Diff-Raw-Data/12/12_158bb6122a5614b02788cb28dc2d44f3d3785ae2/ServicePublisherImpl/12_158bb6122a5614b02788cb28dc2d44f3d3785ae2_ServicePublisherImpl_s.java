 package de.uniluebeck.itm.servicepublisher.cxf;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.assistedinject.Assisted;
 import de.uniluebeck.itm.servicepublisher.ServicePublisherBase;
 import de.uniluebeck.itm.servicepublisher.ServicePublisherConfig;
 import de.uniluebeck.itm.servicepublisher.ServicePublisherService;
 import org.apache.cxf.BusFactory;
 import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
 import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.server.session.SessionHandler;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.websocket.WebSocketServlet;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.ws.rs.core.Application;
 import java.util.Map;
 
 @Singleton
 class ServicePublisherImpl extends ServicePublisherBase {
 
 	private final ContextHandlerCollection contextHandlerCollection;
 
 	private final org.eclipse.jetty.server.Server server;
 
 	private final ServletContextHandler soapContext;
 
 	@Inject
 	public ServicePublisherImpl(@Assisted final ServicePublisherConfig config) {
 
 		super(config);
 
 		System.setProperty(BusFactory.BUS_FACTORY_PROPERTY_NAME, "org.apache.cxf.bus.CXFBusFactory");
 
 		// Start up the jetty embedded server
		server = new org.eclipse.jetty.server.Server(config.getPort());
 		contextHandlerCollection = new ContextHandlerCollection();
 		server.setHandler(contextHandlerCollection);
 
 		soapContext = new ServletContextHandler();
 		soapContext.setContextPath("/soap");
 		soapContext.setSessionHandler(new SessionHandler());
 
 		CXFNonSpringServlet cxfServlet = new CXFNonSpringServlet() {
 			@Override
 			public void init(final ServletConfig sc) throws ServletException {
 				super.init(sc);
 				BusFactory.setDefaultBus(getBus());
 			}
 		};
 		soapContext.addServlet(new ServletHolder(cxfServlet), "/*");
 
 		addHandler(soapContext);
 	}
 
 	@Override
 	protected void doStartInternal() throws Exception {
 		server.start();
 		soapContext.start();
 	}
 
 	@Override
 	protected void doStopInternal() throws Exception {
 		server.stop();
 	}
 
 	@Override
 	protected ServicePublisherService createJaxWsServiceInternal(String contextPath, final Object endpointImpl) {
 		contextPath = contextPath.startsWith("/soap") ? contextPath.substring("/soap".length()) : contextPath;
 		return new ServicePublisherJaxWsService(this, contextPath, endpointImpl);
 	}
 
 	@Override
 	protected ServicePublisherService createJaxRsServiceInternal(final String contextPath, final Application application) {
 		return new ServicePublisherJaxRsService(this, contextPath, application);
 	}
 
 	@Override
 	protected ServicePublisherService createWebSocketServiceInternal(final String contextPath,
 													 final WebSocketServlet webSocketServlet) {
 		return new ServicePublisherWebSocketService(this, contextPath, webSocketServlet);
 	}
 
 	@Override
 	public ServicePublisherService createServletServiceInternal(final String contextPath, final String resourceBase,
 																final Map<String, String> initParams) {
 		return new ServicePublisherServletService(this, contextPath, resourceBase, initParams);
 	}
 
 	String getAddress(final String contextPath) {
 		return "http://localhost:" + config.getPort() + contextPath;
 	}
 
 	public void addHandler(final ServletContextHandler contextHandler) {
 		contextHandlerCollection.addHandler(contextHandler);
 	}
 
 	public void removeHandler(final ServletContextHandler contextHandler) {
 		contextHandlerCollection.removeHandler(contextHandler);
 	}
 }
