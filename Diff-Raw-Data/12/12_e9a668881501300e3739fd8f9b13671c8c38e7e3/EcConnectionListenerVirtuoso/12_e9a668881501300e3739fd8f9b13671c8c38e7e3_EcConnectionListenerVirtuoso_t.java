 package eu.play_project.dcep.distributedetalis;
 
 import java.io.Serializable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
 import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
 
 import eu.play_project.play_eventadapter.AbstractReceiver;
 import eu.play_project.play_eventadapter.NoRdfEventException;
 import fr.inria.eventcloud.api.CompoundEvent;
 
 class EcConnectionListenerVirtuoso implements INotificationConsumer, Serializable {
 
 	private static final long serialVersionUID = -461705400447885142L;
 	private DistributedEtalis dEtalis;
 	private final AbstractReceiver rdfReceiver;
 	private final Logger logger;
 	
 	public EcConnectionListenerVirtuoso(AbstractReceiver rdfReceiver) {
 		this.rdfReceiver = rdfReceiver;
 		this.logger = LoggerFactory.getLogger(this.getClass());
 	}
 	
 	@Override
 	public void notify(Notify notify) throws WsnbException {
 		if (this.dEtalis == null) {
 			String msg = "Detalis was not set in " + this.getClass().getSimpleName();
 			logger.error(msg);
 			throw new IllegalStateException(msg);
 		}
		if (this.dEtalis.getEcConnectionManager() == null) {
			String msg = "ecConnectionManager was not set in " + this.getClass().getSimpleName();
			logger.error(msg);
			throw new IllegalStateException(msg);
		}
 		
 	    try {
 	    	CompoundEvent event = EventCloudHelpers.toCompoundEvent(this.rdfReceiver.parseRdf(notify));
	    	// FIXME stuehmer:
	    	String topic = notify.getNotificationMessage().get(0).getTopic().getContent();
	    	logger.info("Received event {} on topic {} from the DSB.", event.getGraph(), topic);
 	    	
 		    // Forward the event to Detalis:
 		    this.dEtalis.publish(event);
 		    
 		    // Store the event in Virtuoso:
		    ((EcConnectionManagerVirtuoso)this.dEtalis.getEcConnectionManager()).putDataInCloud(event, topic);
 		    
 	    } catch (NoRdfEventException e) {
 			logger.error("Received a non-RDF event from the DSB: " + e.getMessage());
 		}
 	}
 
 	public void setDetalis(DistributedEtalis dEtalis) {
 		this.dEtalis = dEtalis;
 	}
 }
