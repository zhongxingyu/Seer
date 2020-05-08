 package com.page5of4.codon.camel;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.apache.camel.ProducerTemplate;
 import org.apache.camel.model.ModelCamelContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.page5of4.codon.BusException;
 import com.page5of4.codon.EndpointAddress;
 import com.page5of4.codon.Transport;
 
 public class CamelTransport implements Transport {
    private static final Logger logger = LoggerFactory.getLogger(CamelTransport.class);
    private final Map<EndpointAddress, HandlerRouteBuilder> listenerMap = new ConcurrentHashMap<EndpointAddress, HandlerRouteBuilder>();
    private final ComponentResolver componentTemplate;
    private final ModelCamelContext camelContext;
    private final ProducerTemplate producer;
    private final InvokeHandlerProcessor invokeHandlerProcessor;
 
    public static final String MESSAGE_TYPE_KEY = "messageType";
    public static final String REPLY_TO_ADDRESS_KEY = "replyTo";
 
    @Autowired
    public CamelTransport(ModelCamelContext camelContext, ComponentResolver componentTemplate, InvokeHandlerProcessor invokeHandlerProcessor) {
       this.camelContext = camelContext;
       this.componentTemplate = componentTemplate;
       this.invokeHandlerProcessor = invokeHandlerProcessor;
       this.producer = camelContext.createProducerTemplate();
    }
 
    @PostConstruct
    public void start() {
       try {
          camelContext.start();
       }
       catch(Exception e) {
          throw new BusException(e);
       }
    }
 
    @PreDestroy
    public void stop() {
       try {
          camelContext.stop();
       }
       catch(Exception e) {
          throw new BusException(e);
       }
    }
 
    @Override
    public void send(EndpointAddress address, Object message) {
       try {
          logger.debug("Sending {} -> {}", message, address);
          autoCreateDestination(address);
          producer.send(toEndpointUri(address), new OutgoingProcessor(message));
       }
       catch(Exception e) {
          throw new BusException(String.format("Unable to send '%s' to '%s'", message, address), e);
       }
    }
 
    @Override
    public void listen(EndpointAddress address) {
       try {
          synchronized(listenerMap) {
             if(listenerMap.containsKey(address)) {
                logger.warn("Already listening to {}", address);
                return;
             }
             autoCreateDestination(address);
             HandlerRouteBuilder builder = new HandlerRouteBuilder(invokeHandlerProcessor, toEndpointUri(address));
             listenerMap.put(address, builder);
             camelContext.addRoutes(builder);
          }
       }
       catch(Exception e) {
          throw new BusException(String.format("Unable to listen on '%s'", address), e);
       }
    }
 
    @Override
    public void unlisten(EndpointAddress address) {
       try {
          synchronized(listenerMap) {
             if(!listenerMap.containsKey(address)) {
                logger.warn("Not listening to {}", address);
                return;
             }
             camelContext.removeRouteDefinitions(listenerMap.get(address).getRouteCollection().getRoutes());
          }
       }
       catch(Exception e) {
          throw new BusException(String.format("Unable to listen on '%s'", address), e);
       }
    }
 
    private String toEndpointUri(EndpointAddress address) {
       return String.format("%s:%s", address.getHost(), address.getPath());
    }
 
    private void autoCreateDestination(EndpointAddress address) {
       try {
          String name = address.getHost();
          if(camelContext.hasComponent(name) != null) {
             return;
          }
          camelContext.addComponent(name, componentTemplate.createComponent(address, camelContext));
       }
       catch(Exception e) {
          throw new BusException(String.format("Unable to create Camel component for destination '%s'", address), e);
       }
    }
 }
