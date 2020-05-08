 package com.page5of4.codon.camel;
 
 import org.apache.camel.CamelContext;
 import org.apache.camel.Component;
 import org.apache.camel.component.jms.JmsComponent;
 import org.apache.camel.impl.DefaultComponentResolver;
 import org.apache.camel.spi.ComponentResolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.page5of4.codon.BusConfiguration;
 import com.page5of4.codon.BusConfiguration.CommunicationConfiguration;
 import com.page5of4.codon.BusException;
 import com.page5of4.codon.impl.TransactionConvention;
 
 public class CodonComponentResolver implements ComponentResolver {
    private static final Logger logger = LoggerFactory.getLogger(CodonComponentResolver.class);
    private final DefaultComponentResolver resolver = new DefaultComponentResolver();
    private final TransactionConvention transactionConvention;
    private final BusConfiguration configuration;
 
    public CodonComponentResolver(TransactionConvention transactionConvention, BusConfiguration configuration) {
       this.transactionConvention = transactionConvention;
       this.configuration = configuration;
    }
 
    @Override
    public Component resolveComponent(String name, CamelContext camelContext) throws Exception {
       logger.info("Resolving '{}'", name);
       CommunicationConfiguration cc = configuration.findCommunicationConfiguration(name);
       if(cc == null) {
          throw new BusException(String.format("No CommunicationConfiguration available for '%s'", name));
       }
       logger.info("Configuration {}", cc);
       Component resolved = resolver.resolveComponent(cc.getComponentName(), camelContext);
       if(resolved instanceof JmsComponent) {
          JmsComponent jmsComponent = (JmsComponent)resolved;
          jmsComponent.setTransactionManager(transactionConvention.locate(name, jmsComponent.getConfiguration().getConnectionFactory()));
          jmsComponent.setTransacted(true);
          logger.info("Have {} {}", jmsComponent, jmsComponent.getConfiguration().getConnectionFactory());
       }
       else if(resolved == null) {
          logger.info("No component for '{}'", cc.getComponentName());
         logger.info("Used {} / {}", camelContext, camelContext.getFactoryFinder(DefaultComponentResolver.RESOURCE_PATH));
       }
       return resolved;
    }
 }
