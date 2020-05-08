 package eu.play_project.dcep.distributedetalis;
 
 import java.io.Serializable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
 import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
 
 import eu.play_project.play_eventadapter.AbstractReceiver;
 import eu.play_project.play_eventadapter.NoRdfEventException;
 
 class EcConnectionListenerVirtuoso implements INotificationConsumer, Serializable {
 
 	private static final long serialVersionUID = -461705400447885142L;
 	private DistributedEtalis dEtalis;
 	private AbstractReceiver rdfReceiver;
 	private Logger logger;
 	
 	public EcConnectionListenerVirtuoso(AbstractReceiver rdfReceiver) {
 		this.rdfReceiver = rdfReceiver;
 		this.logger = LoggerFactory.getLogger(this.getClass());
 	}
 	
 	@Override
 	public void notify(Notify notify) throws WsnbException {
 	    try {
 		    logger.info("Got a notify...");
 		    // Forward the event to Detalis:
		    this.dEtalis.publish(EventCloudHelpers.toCompoundEvent(this.rdfReceiver.parseRdf(notify)));
 	    } catch (NoRdfEventException e) {
 			logger.error("Received a non-RDF event from the DSB: " + e.getMessage());
 		}
 	}
 
 	public void setDetalis(DistributedEtalis dEtalis) {
 		this.dEtalis = dEtalis;
 	}
 }
