 package com.google.code.qualitas.internal.installation.core;
 
 import java.util.Map;
 
 import org.apache.camel.Processor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 
 import com.google.code.qualitas.engines.api.component.Component;
 import com.google.code.qualitas.engines.api.core.ProcessType;
 
 /**
  * The Class AbstractProcessor.
  */
 public abstract class AbstractProcessor implements Processor {
 
     /** The context. */
     @Autowired
     private ApplicationContext context;
 
     /**
      * Find qualitas component.
      * 
      * @param <T>
      *            the generic type
      * @param type
      *            the type
      * @param processType
      *            the process type
      * @return the T
      * @throws ComponentNotFound
      *             the component not found
      */
     protected <T extends Component> T findQualitasComponent(Class<T> type, ProcessType processType)
             throws ComponentNotFound {
         Map<String, T> components = context.getBeansOfType(type);
 
         for (String key : components.keySet()) {
             T component = components.get(key);
             if (component.isSupported(processType)) {
                 return component;
             }
         }
         throw new ComponentNotFound("Could not find a component of type " + type
                 + " for process bundle of type " + processType);
     }
 
 }
