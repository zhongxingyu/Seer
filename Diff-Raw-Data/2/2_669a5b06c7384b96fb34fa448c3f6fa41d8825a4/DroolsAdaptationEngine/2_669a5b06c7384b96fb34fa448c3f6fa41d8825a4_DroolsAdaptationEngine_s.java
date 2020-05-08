 package eu.indenica.adaptation.drools;
 
 import java.io.ByteArrayInputStream;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.io.ResourceFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.osoa.sca.annotations.Destroy;
 import org.osoa.sca.annotations.EagerInit;
 import org.osoa.sca.annotations.Init;
 import org.osoa.sca.annotations.Property;
 import org.osoa.sca.annotations.Scope;
 import org.slf4j.Logger;
 
 import com.google.common.collect.Maps;
 
 import eu.indenica.adaptation.AdaptationEngine;
 import eu.indenica.adaptation.AdaptationRule;
 import eu.indenica.adaptation.AdaptationRuleImpl;
 import eu.indenica.adaptation.Fact;
 import eu.indenica.common.LoggerFactory;
 import eu.indenica.common.PubSub;
 import eu.indenica.common.PubSubFactory;
 import eu.indenica.common.RuntimeComponent;
 import eu.indenica.events.ActionEvent;
 import eu.indenica.events.Event;
 
 @Scope("COMPOSITE")
 @EagerInit
 public class DroolsAdaptationEngine implements AdaptationEngine {
 	private static Logger LOG = LoggerFactory.getLogger();
 
 	private PubSub pubsub;
 	private KnowledgeBase knowledgeBase;
 	private StatefulKnowledgeSession session;
 	private KnowledgeBuilder knowledgeBuilder;
 
 	private Map<String, Fact> factBuffer = Maps.newHashMap();
 
 	// @Property
 	protected AdaptationRule[] rules;
 
 	@Property
 	protected String[] inputEventTypes;
 
 	// /**
 	// * @param rules
 	// * the rules to set
 	// */
 	// public void setRules(String[] rules) {
 	// LOG.debug("Setting rules: {}", rules);
 	// this.rules = rules;
 	// }
 
 	@Property
 	public void setRules(AdaptationRuleImpl[] rules) {
 		LOG.debug("Setting rules: {}", rules);
 		this.rules = rules;
 	}
 
 	/**
 	 * @param inputEventTypes
 	 *            the inputEventTypes to set
 	 */
 	public void setInputEventTypes(String[] inputEventTypes) {
 		LOG.debug("Setting input event types: {}", inputEventTypes);
 		this.inputEventTypes = inputEventTypes;
 	}
 
 	@Init
 	@Override
 	public void init() throws Exception {
 		LOG.info("Initializing Adaptation Engine...");
 		this.pubsub = PubSubFactory.getPubSub();
 		knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
 		for(AdaptationRule rule : rules)
 			addRule(rule);
 
		if(knowledgeBuilder.getErrors().size() > 0)
 			LOG.error("Errors: {}", knowledgeBuilder.getErrors());
 		knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
 		knowledgeBase.addKnowledgePackages(knowledgeBuilder
 				.getKnowledgePackages());
 		session = knowledgeBase.newStatefulKnowledgeSession();
 		session.setGlobal("publisher", this);
 		session.fireAllRules();
 		LOG.debug("Adaptation Engine started.");
 	}
 
 	@Destroy
 	@Override
 	public void destroy() throws Exception {
 		LOG.debug("Stopping Adaptation Engine...");
 		session.dispose();
 		executor.shutdown();
 		// executor.shutdownNow();
 		executor.awaitTermination(2, TimeUnit.SECONDS);
 		LOG.info("Adaptation Engine stopped.");
 	}
 
 	@Override
 	public void eventReceived(RuntimeComponent source, Event event) {
 		LOG.debug("Received event {} from {}", event, source);
 		updateFact(event);
 	}
 
 	/**
 	 * @param event
 	 * @return
 	 */
 	private Fact updateFact(Event event) {
 		boolean newFact = false;
 		if(!factBuffer.containsKey(event.getEventType())) {
 			factBuffer.put(event.getEventType(), new Fact());
 			newFact = true;
 		}
 
 		// TODO: probably need to update fact using session.update();
 
 		Fact fact = factBuffer.get(event.getEventType());
 		fact.setEvent(event);
 		if(newFact)
 			setFact(fact);
 		session.fireAllRules();
 		return fact;
 	}
 
 	@Override
 	public void addRule(AdaptationRule rule) {
 		LOG.debug("Adding rule: {}", rule);
 		knowledgeBuilder.add(ResourceFactory
 				.newInputStreamResource(new ByteArrayInputStream(rule
 						.getStatement().getBytes())), ResourceType.DRL);
 		registerInputEventTypes(rule);
 	}
 
 	/**
 	 * @param rule
 	 */
 	private void registerInputEventTypes(AdaptationRule rule) {
 		if(rule.getInputEventTypes() == null) {
 			LOG.warn("No input events for rule {}", rule);
 			return;
 		}
 		for(String eventType : rule.getInputEventTypes()) {
 			String source = null;
 			if(eventType.contains(",")) {
 				String[] split = eventType.split(",", 2);
 				eventType = split[1].trim();
 				source = split[0].trim();
 				// FIXME: Correctly get RuntimeComponent reference to register.
 				LOG.trace("Found source: {}", source);
 			}
 
 			pubsub.registerListener(this, null, eventType);
 		}
 	}
 
 	@Override
 	public void setFact(Fact fact) {
 		session.insert(fact);
 	}
 
 	public void performAction(ActionEvent actionEvent) {
 		LOG.info("Perform action {}", actionEvent);
 		pubsub.publish(this, actionEvent);
 	}
 
 	private ExecutorService executor = Executors.newCachedThreadPool();
 
 	public void publishEvent(final Event event) {
 		LOG.info("Publishing event {}", event);
 		final RuntimeComponent component = this;
 		executor.submit(new Callable<Void>() {
 			public Void call() throws Exception {
 				pubsub.publish(component, event);
 				return null;
 			}
 		});
 
 	}
 }
