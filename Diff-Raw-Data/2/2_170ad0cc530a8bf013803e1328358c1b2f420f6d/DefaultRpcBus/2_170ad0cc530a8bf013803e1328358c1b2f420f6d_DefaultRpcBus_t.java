 package cmf.eventing.berico;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.joda.time.Duration;
 
 import cmf.bus.Envelope;
 import cmf.bus.EnvelopeHeaderConstants;
 import cmf.bus.IEnvelopeBus;
 import cmf.bus.berico.EnvelopeHelper;
 import cmf.eventing.berico.EventContext.Directions;
 import cmf.eventing.patterns.rpc.IRpcEventBus;
 
 public class DefaultRpcBus extends DefaultEventBus implements IRpcEventBus, IInboundProcessorCallback {
 
 	
     public DefaultRpcBus(IEnvelopeBus envelopeBus) {
         super(envelopeBus);
     }
 
     public DefaultRpcBus(IEnvelopeBus envelopeBus, List<IEventProcessor> inboundProcessors,
                     List<IEventProcessor> outboundProcessors) {
         super(envelopeBus, inboundProcessors, outboundProcessors);
     }
 
     
     @Override
     public <TResponse> Collection<TResponse> gatherResponsesTo(Object request, Duration timeout) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     @SuppressWarnings("rawtypes")
     public Collection gatherResponsesTo(Object request, Duration timeout, String... expectedTopics) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <TResponse> TResponse getResponseTo(Object request, Duration timeout, Class<TResponse> expectedType) {
         Object responseObject = getResponseTo(request, timeout, expectedType.getCanonicalName());
 
         return (TResponse) responseObject;
     }
 
     @Override
     public Object getResponseTo(Object request, final Duration timeout, final String expectedTopic) {
         log.debug("Enter GetResponseTo");
 
         // guard clause
         if (null == request) {
             throw new IllegalArgumentException("Cannot get response to a null request");
         }
 
         // the container for the response we're going to get since you
         // can't assign the value of this within the continuation callback
         final EventContext responseContext = new EventContext(Directions.In);
         
 
         try {
         	// create the ID for the request
         	final UUID requestId = UUID.randomUUID();
         	
         	// Get an appropriately setup envelope
         	Envelope env = this.buildRequestEnvelope(requestId, timeout);
 
             // create an event context for processing
             final EventContext context = new EventContext(Directions.Out, env, request);
             
             // need a final-scoped handle to an IInboundProcessorCallback
             final IInboundProcessorCallback envelopeOpener = this;
             
             // process the event
             this.processEvent(
         		context, 
         		outboundProcessors, 
             	new IContinuationCallback() {
 
 					@Override
 					public void continueProcessing() throws Exception {
 					
 						log.debug("successfully processed outgoing request");
 						
 						// create an RPC registration
 			            RpcRegistration rpcRegistration = new RpcRegistration(requestId, expectedTopic, envelopeOpener);
 
 			            // register with the envelope bus
 			            envelopeBus.register(rpcRegistration);
 
 			            // now that we're setup to receive the response, send the request
 			            envelopeBus.send(context.getEnvelope());
 
 			            // get the envelope from the registraton
 			            responseContext.setEvent(rpcRegistration.getResponse(timeout));
 
 			            // unregister from the bus
 			            envelopeBus.unregister(rpcRegistration);
 					} // end of final continuation
 					
             }); // end of outbound processing
         } catch (Exception ex) {
             log.error("Exception publishing an event", ex);
             throw new RuntimeException("Exception publishing an event", ex);
         }
 
         log.debug("Leave GetResponseTo");
         return responseContext.getEvent();
     }
 
     @Override
     public void respondTo(Map<String, String> headers, Object response) {
         log.debug("Enter RespondTo");
 
         if (null == response) {
             throw new IllegalArgumentException("Cannot respond with a null event");
         }
         if (null == headers) {
             throw new IllegalArgumentException("Must provide non-null request headers");
         }
 
         Envelope envelope = new Envelope();
         envelope.setHeaders(headers);
         EnvelopeHelper originalHeadersHelper = new EnvelopeHelper(envelope);
         if (null == originalHeadersHelper.getMessageId()) {
             throw new IllegalArgumentException(
                             "Cannot respond to a request because the provided request headers do not contain a message ID");
         }
 
         try {
             Envelope env = new Envelope();
             new EnvelopeHelper(env).setCorrelationId(originalHeadersHelper.getMessageId());
 
             final EventContext context = new EventContext(Directions.Out, env, response);
 
            this.processEvent(context, this.outboundProcessors, new IContinuationCallback() {
             
             	@Override
             	public void continueProcessing() throws Exception {
             		envelopeBus.send(context.getEnvelope());
             	}
             });
 
             
         } catch (Exception ex) {
             log.error("Exception responding to an event", ex);
             throw new RuntimeException("Exception responding to an event", ex);
         }
 
         log.debug("Leave RespondTo");
     }
 
 
     protected Envelope buildRequestEnvelope(UUID requestId, Duration timeout) {
     	
     	// create a new envelope
         Envelope env = new Envelope();
         EnvelopeHelper envelopeHelper = new EnvelopeHelper(env);
 
         // set the requestId on the envelope
         envelopeHelper.setMessageId(requestId);
 
         // add pattern & timeout information to the headers
         envelopeHelper.setMessagePattern(EnvelopeHeaderConstants.MESSAGE_PATTERN_RPC);
         envelopeHelper.setRpcTimeout(timeout);
         
         return env;
     }
 }
