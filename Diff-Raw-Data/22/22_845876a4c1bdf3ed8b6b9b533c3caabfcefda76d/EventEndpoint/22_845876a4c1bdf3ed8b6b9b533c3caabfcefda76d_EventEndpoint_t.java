 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.spring;
 
 import java.util.List;
 import java.util.LinkedList;
 
 import org.apache.commons.lang.Validate;
 
 import org.apache.camel.model.FromDefinition;
 import org.apache.camel.model.MulticastDefinition;
 import org.apache.camel.model.RouteDefinition;
 import org.apache.camel.model.ToDefinition;
 import org.apache.camel.processor.aggregate.AggregationStrategy;
 import org.apache.camel.spring.CamelContextFactoryBean;
 
 import org.springframework.beans.factory.config.BeanPostProcessor;
 
 /** An event endpoint represents a name where event sources can raise events
  * and clients can register listeners to be notified of such events.
  *
  * This uses camel to route events. All endpoint specifications follow camel
  * notation.
  *
  * This class registers a new route in the camel context named katari.eventBus.
  * The route has a from endpoint and multicasts the events to all the
  * registered listeners.
  *
  * Each Event is sent synchronously, and a response is sent back to the source
  * of the event. The response is built aggregating the response of every
  * listener.
  */
 public class EventEndpoint implements BeanPostProcessor {
 
   /** The aggregator for event responses, never null.
    */
   private AggregationStrategy aggregationStrategy;

   /** The null endpoint, where the event is sent when there are no listeners.
   *
    * If null, we use the default endpoint (bean:katari.defaultEventListener).
    * The default is useful when the input and output of an event are of the
    * same type.
    */
   private String nullUri;

   /** The source endpoint, never null.
    */
   private String fromUri;

   /** Listener endpoints, never null.
    */
   private List<ToDefinition> toDefinitions;
 
   /** Creates a new event endpoint.
    *
    * @param theAggregationStrategy aggregates the response of each listener.
    * It cannot be null.
    *
    * @param theNullUri an endpoint where the event goes when there are no
    * listeners. This is necessary when the output and the input of the event
    * are of different types. It cannot be null.
    *
    * @param sourceUri the source endpoint name. It cannot be null.
    *
    * @param listeners the list of listeners of the events raised in the
    * provided source endpoint.
    */
   public EventEndpoint(final AggregationStrategy theAggregationStrategy,
       final String theNullUri, final String sourceUri,
       final List<ToDefinition> listeners) {
     Validate.notNull(theAggregationStrategy,
         "The aggregation strategy cannot be null.");
     Validate.notNull(theNullUri, "The null uri cannot be null.");
     Validate.notNull(sourceUri, "The source uri cannot be null.");
     Validate.notNull(listeners, "The listeners cannot be null.");
     aggregationStrategy = theAggregationStrategy;
     nullUri = theNullUri;
     fromUri = sourceUri;
     toDefinitions = listeners;
   }
 
   /** Creates a new event endpoint with a default null endpoint.
    *
    * The default null endpoint can be used when the input and output of the
    * event are of the same type.
    *
    * @param theAggregationStrategy aggregates the response of each listener.
    * It cannot be null.
    *
    * @param sourceUri the source endpoint name. It cannot be null.
    *
    * @param listeners the list of listeners of the events raised in the
    * provided source endpoint.
    */
   public EventEndpoint(final AggregationStrategy theAggregationStrategy,
       final String sourceUri, final List<ToDefinition> listeners) {
     Validate.notNull(theAggregationStrategy,
         "The aggregation strategy cannot be null.");
     Validate.notNull(sourceUri, "The source uri cannot be null.");
     Validate.notNull(listeners, "The listeners cannot be null.");
     aggregationStrategy = theAggregationStrategy;
     nullUri = "bean:katari.defaultEventListener";
     fromUri = sourceUri;
     toDefinitions = listeners;
   }
 
   /** {@inheritDoc}
    *
    * This implementation does nothing.
    */
  public Object postProcessAfterInitialization(final Object bean,
      final String beanName) {
     return bean;
   }
 
   /** {@inheritDoc}
    *
    * Attaches the routes to the camel context named katari.eventBus.
    */
   public Object postProcessBeforeInitialization(final Object bean,
      final String beanName) {
 
     if (beanName.equals("katari.eventBus")) {
 
       // From endpoint.
       List<FromDefinition> from = new LinkedList<FromDefinition>();
       from.add(new FromDefinition(fromUri));
 
       // To endpoints.
       MulticastDefinition multicast = new MulticastDefinition();
       if (toDefinitions.size() != 0) {
         for (ToDefinition to : toDefinitions) {
           multicast.addOutput(to);
         }
       } else {
         multicast.addOutput(new ToDefinition(nullUri));
       }
       multicast.aggregationStrategy(aggregationStrategy);
 
       // Chains everything.
       RouteDefinition route = new RouteDefinition();
       route.setInputs(from);
       route.addOutput(multicast);
 
       // Adds it to the context.
       CamelContextFactoryBean contextFactory = (CamelContextFactoryBean) bean;
       contextFactory.getRoutes().add(route);
       return contextFactory;
     } else {
       return bean;
     }
   }
 }
 
