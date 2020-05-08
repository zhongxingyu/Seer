 package eu.play_project.dcep.distributedetalis;
 
 import java.io.Serializable;
 
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.util.ModelUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
 import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
 
 import eu.play_project.dcep.distributedetalis.utils.DsbHelpers;
 import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
 import eu.play_project.play_eventadapter.AbstractReceiverRest;
 import eu.play_project.play_eventadapter.NoRdfEventException;
 import fr.inria.eventcloud.api.CompoundEvent;
 
 class EcConnectionListenerVirtuoso implements INotificationConsumer, Serializable {
 
 	private static final long serialVersionUID = 100L;
 	private DistributedEtalis dEtalis;
 	private final AbstractReceiverRest rdfReceiver;
 	private final Logger logger;
 	
 	public EcConnectionListenerVirtuoso(AbstractReceiverRest rdfReceiver) {
 		this.rdfReceiver = rdfReceiver;
 		this.logger = LoggerFactory.getLogger(this.getClass());
 	}
 	
 	@Override
 	public void notify(Notify notify) throws WsnbException {
 		if (this.dEtalis == null) {
 			String msg = "Detalis was not set in " + this.getClass().getSimpleName();
 			throw new IllegalStateException(msg);
 		}
 		if (this.dEtalis.getEcConnectionManager() == null) {
 			String msg = "ecConnectionManager was not set in " + this.getClass().getSimpleName();
 			throw new IllegalStateException(msg);
 		}
 		
 	    try {
 	    	Model rdf = this.rdfReceiver.parseRdf(notify);
 	    	ModelUtils.deanonymize(rdf);
 	    	CompoundEvent event = EventCloudHelpers.toCompoundEvent(rdf);
 	    	String topic = DsbHelpers.topicToUri(notify.getNotificationMessage().get(0).getTopic());
 	    	logger.debug("Received event {} on topic {} from the DSB.", event.getGraph(), topic);
 	    	
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
