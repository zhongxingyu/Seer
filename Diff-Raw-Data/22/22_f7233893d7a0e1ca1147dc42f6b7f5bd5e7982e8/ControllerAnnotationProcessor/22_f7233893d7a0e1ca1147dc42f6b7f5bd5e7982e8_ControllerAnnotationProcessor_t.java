 package be.vrt.web.restdc.spring.annotation.processor.impl;
 
 import be.vrt.web.restdc.annotation.processor.AnnotationProcessor;
 import be.vrt.web.restdc.annotation.processor.OverridingAnnotationProcessor;
 import be.vrt.web.restdc.domain.ResourceDocument;
 import org.reflections.ReflectionUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author Mike Seghers
  */
 public class ControllerAnnotationProcessor implements AnnotationProcessor<Controller, List<ResourceDocument>, Class<?>> {
     private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAnnotationProcessor.class);
 
     private OverridingAnnotationProcessor<RequestMapping, ResourceDocument, Method, Class<?>> requestMappingAnnotationProcessor;
 
    /**
     * Initializes the processor with the overriding annotation processor.
     * @param requestMappingAnnotationProcessor the processor
     */
     public ControllerAnnotationProcessor(final OverridingAnnotationProcessor<RequestMapping, ResourceDocument, Method, Class<?>> requestMappingAnnotationProcessor) {
         this.requestMappingAnnotationProcessor = requestMappingAnnotationProcessor;
     }
 
     @Override
     public List<ResourceDocument> process(final Controller controllerAnnotation, final Class<?> annotatedElement) {
         LOGGER.debug("Generating resource documents for class {}", annotatedElement);
         RequestMapping classLevelRequestMapping = annotatedElement.getAnnotation(RequestMapping.class);
         Set<Method> allRequestMappingMethods = ReflectionUtils
                 .getAllMethods(annotatedElement, ReflectionUtils.withAnnotation(RequestMapping.class));
         List<ResourceDocument> result;
         if (allRequestMappingMethods.size() == 0) {
             result = null;
         } else {
             result = new ArrayList<>(allRequestMappingMethods.size());
             for (Method requestMappingMethod : allRequestMappingMethods) {
                 RequestMapping methodRequestMapping = requestMappingMethod.getAnnotation(RequestMapping.class);
                 result.add(requestMappingAnnotationProcessor
                         .process(methodRequestMapping, requestMappingMethod, classLevelRequestMapping, annotatedElement));
             }
         }
 
         LOGGER.debug("Done generating DocumentSets for class {}: {}", annotatedElement, result);
         return result;
     }
 }
