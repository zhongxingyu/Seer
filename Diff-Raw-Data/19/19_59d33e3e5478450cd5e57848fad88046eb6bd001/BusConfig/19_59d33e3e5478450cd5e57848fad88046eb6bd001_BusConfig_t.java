 package com.page5of4.codon.config;
 
import org.apache.camel.osgi.OsgiSpringCamelContext;
 import org.apache.camel.spring.SpringCamelContext;
import org.osgi.framework.Bundle;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 
 import com.page5of4.codon.Bus;
 import com.page5of4.codon.BusConfiguration;
 import com.page5of4.codon.BusException;
 import com.page5of4.codon.HandlerRegistry;
 import com.page5of4.codon.Transport;
 import com.page5of4.codon.camel.CodonComponentResolver;
 import com.page5of4.codon.camel.DefaultCamelTransport;
 import com.page5of4.codon.camel.InvokeHandlerProcessor;
 import com.page5of4.codon.impl.BusContextProvider;
 import com.page5of4.codon.impl.DefaultBus;
 import com.page5of4.codon.impl.TransactionConvention;
 
 @Configuration
 @Import(value = { CoreConfig.class })
 public class BusConfig {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private HandlerRegistry handlerRegistry;
    @Autowired
    private BusContextProvider contextProvider;
    @Autowired
    private TransactionConvention transactionConvention;
    @Autowired
    private BusConfiguration configuration;
 
    @Bean
    public Bus bus() {
       return new DefaultBus(contextProvider, transport());
    }
 
    @Bean
    public Transport transport() {
       try {
         Bundle bundle = org.osgi.framework.FrameworkUtil.getBundle(CodonComponentResolver.class);
         SpringCamelContext camelContext;
         if(bundle == null) {
            camelContext = new SpringCamelContext(applicationContext);
         }
         else {
            camelContext = new OsgiSpringCamelContext(applicationContext, bundle.getBundleContext());
         }
          camelContext.setComponentResolver(new CodonComponentResolver(transactionConvention, configuration));
          camelContext.afterPropertiesSet();
          return new DefaultCamelTransport(configuration, camelContext, invokeHandlerProcessor());
       }
       catch(Exception e) {
          throw new BusException(e);
       }
    }
 
    @Bean
    public InvokeHandlerProcessor invokeHandlerProcessor() {
       return new InvokeHandlerProcessor(handlerRegistry, contextProvider, null);
    }
 }
