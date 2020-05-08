 package de.uniluebeck.itm.servicepublisher.cxf;
 
 import com.google.common.util.concurrent.AbstractService;
 import de.uniluebeck.itm.servicepublisher.ServicePublisherService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.xml.ws.Endpoint;
 import java.net.URI;
 
 class ServicePublisherJaxWsService extends AbstractService implements ServicePublisherService {
 
 	private static final Logger log = LoggerFactory.getLogger(ServicePublisherJaxWsService.class);
 
 	private final ServicePublisherImpl servicePublisher;
 
 	private final String contextPath;
 
 	private final Object endpointImpl;
 
 	private Endpoint endpoint;
 
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
 
			log.info("Publishing SOAP web service {} under context path {}", endpointImpl.getClass().getSimpleName(),
 					contextPath
 			);
 			endpoint = Endpoint.publish(contextPath, endpointImpl);
 
 			notifyStarted();
 		} catch (Exception e) {
 			notifyFailed(e);
 		}
 	}
 
 	@Override
 	protected void doStop() {
 		try {
 			endpoint.stop();
 			notifyStopped();
 		} catch (Exception e) {
 			notifyFailed(e);
 		}
 	}
 
 	@Override
 	public URI getURI() {
 		return URI.create(servicePublisher.getAddress("/soap" + contextPath));
 	}
 }
