 package com.page5of4.codon.impl;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.camel.Exchange;
 
 import com.page5of4.codon.BusException;
 import com.page5of4.codon.HandlerBinding;
 
 public class HandlerDispatcher {
    private final InstanceResolver resolver;
    private final HandlerBinding binding;
    private final BusContextProvider contextProvider;
 
    public InstanceResolver getResolver() {
       return resolver;
    }
 
    public HandlerBinding getBinding() {
       return binding;
    }
 
    public HandlerDispatcher(InstanceResolver resolver, BusContextProvider contextProvider, HandlerBinding binding) {
       super();
       this.resolver = resolver;
       this.contextProvider = contextProvider;
       this.binding = binding;
    }
 
    public void dispatch(Object message, Exchange exchange) {
       Method method = binding.getMethod();
       try {
          List<Object> parameters = new ArrayList<Object>();
          for(Class<?> parameterType : method.getParameterTypes()) {
             if(parameterType.equals(BusContext.class)) {
                parameters.add(contextProvider.currentContext());
             }
             // Can probably provide a collection of ArgumentMatcher's that can do this, that way this class doesn't need
             // to depend on Camel in any way.
            if(parameterType.equals(Exchange.class)) {
                parameters.add(exchange);
             }
             else {
                parameters.add(message);
             }
          }
          binding.getMethod().invoke(resolver.resolve(binding.getHandlerType()), parameters.toArray());
       }
       catch(Exception e) {
          throw new BusException(String.format("Error invoking '%s' with '%s'", method, message), e);
       }
    }
 }
