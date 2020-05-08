 package de.uniluebeck.itm.servicepublisher.cxf;
 
 import com.google.common.util.concurrent.AbstractService;
 import de.uniluebeck.itm.servicepublisher.ServicePublisherService;
 
 import javax.xml.ws.Endpoint;
 import java.net.URI;
 
 class ServicePublisherJaxWsService extends AbstractService implements ServicePublisherService {
 
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
 			endpoint = Endpoint.publish(contextPath.substring(5), endpointImpl);
 			notifyStarted();
 		} catch (Exception e) {
 			notifyFailed(e);
 		}
 	}
 
 	@Override
 	protected void doStop() {
 		try {
 			endpoint.stop();
			notifyStarted();
 		} catch (Exception e) {
 			notifyFailed(e);
 		}
 	}
 
 	@Override
 	public URI getURI() {
 		return URI.create(servicePublisher.getAddress(contextPath));
 	}
 }
