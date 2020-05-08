 package de.uniluebeck.itm.servicepublisher.cxf;
 
 import com.google.common.util.concurrent.AbstractService;
 import de.uniluebeck.itm.servicepublisher.ServicePublisherService;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nullable;
 import java.net.URI;
 
 class ServicePublisherJaxWsService extends AbstractService implements ServicePublisherService {
 
 	private static final Logger log = LoggerFactory.getLogger(ServicePublisherJaxWsService.class);
 
 	private final ServicePublisherImpl servicePublisher;
 
 	private final String contextPath;
 
 	private final Object endpointImpl;
 
	private ServletHolder servlet;
 
 	public ServicePublisherJaxWsService(final ServicePublisherImpl servicePublisher,
 										final String contextPath,
 										final Object endpointImpl) {
 		this.servicePublisher = servicePublisher;
 		this.contextPath = contextPath;
 		this.endpointImpl = endpointImpl;
 	}
 
 	@Override
 	protected void doStart() {
 		try {
 
 			final ServicePublisherCxfServlet cxfServlet = new ServicePublisherCxfServlet("", endpointImpl);
 
 			log.info(
 					"Publishing SOAP web service {} under context path /soap{}",
 					endpointImpl.getClass().getSimpleName(),
 					contextPath
 			);
 
			servlet = new ServletHolder(cxfServlet);
 			servicePublisher.getSoapContext().addServlet(servlet, contextPath);
 
 			notifyStarted();
 
 		} catch (Exception e) {
 			notifyFailed(e);
 		}
 	}
 
 	@Override
 	protected void doStop() {
 		try {
			servlet.stop();
 			notifyStopped();
 		} catch (Exception e) {
 			notifyFailed(e);
 		}
 	}
 
 	@Override
 	public URI getURI() {
 		return URI.create(servicePublisher.getAddress("/soap" + contextPath));
 	}
 
 	@Nullable
 	@Override
 	public ServletContextHandler getServletContextHandler() {
 		return null;
 	}
 }
