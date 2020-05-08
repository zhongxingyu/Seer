 package com.firetruckbowl.tgirest.processor;
 
 
 import com.firetruckbowl.tgirest.annotation.MethodError;
 import com.firetruckbowl.tgirest.annotation.ParamNote;
 import com.firetruckbowl.tgirest.annotation.ResourceDoc;
 import com.firetruckbowl.tgirest.annotation.ResourceMethod;
 import com.firetruckbowl.tgirest.model.MethodDocument;
 import com.firetruckbowl.tgirest.model.ParamDocument;
 import com.firetruckbowl.tgirest.model.ResourceDocument;
 import com.firetruckbowl.tgirest.model.ResponseError;
 import com.firetruckbowl.tgirest.resource.Documented;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * Processes TGIRest annotations and generates documentation.
  *
  * @author <a href="mailto:lochnguyen@gmail.com">Loc Nguyen</a>
  */
 public class TGIRestDocumenter implements Documenter {
   private static final Logger LOG = LoggerFactory.getLogger(TGIRestDocumenter.class);
 
   @Override
   public ResourceDocument generateResourceDocument(@Context UriInfo uriInfo, Documented resource) {
 
     if (!resource.getClass().isAnnotationPresent(ResourceDoc.class)) {
       throw new RuntimeException("Cannot generate documentation for a resource class "
           + "that is not annotated with @ResoureceDoc");
     }
 
     ResourceDocument resourceDocument = new ResourceDocument();
 
     // --- Process JAX-RS annotations
     Path path = resource.getClass().getAnnotation(Path.class);
     if (path != null) {
       resourceDocument.setPath(uriInfo.getBaseUri().toString() + path.value());
     }
 
     // --- Process TGIRest annotations
     ResourceDoc rd = resource.getClass().getAnnotation(ResourceDoc.class);
     resourceDocument.setDescription(rd.description());
 
     // -- Generate documentation for each resource method
     for (Method m : resource.getClass().getMethods()) {
       if (m.getAnnotation(ResourceMethod.class) != null) {
         resourceDocument.addMethodDocument(generateMethodDocument(uriInfo, m));
       }
     }
 
     return resourceDocument;
   }
 
   @Override
   public MethodDocument generateMethodDocument(@Context UriInfo uriInfo, Method method) {
     ResourceMethod rm = method.getAnnotation(ResourceMethod.class);
     if (rm == null) {
       throw new IllegalArgumentException("Can't generate document unless method is annotated with @ResourceMethod");
     }
 
     MethodDocument md = new MethodDocument();
 
     // --- Process JAX-RS annotations
    Path path = method.getAnnotation(Path.class);
    if (path != null) {
      md.setPath(uriInfo.getBaseUri().toString() + path.value());
     }
 
     md.setHttpMethod(getHttpMethod(method));
 
     Produces produces = method.getAnnotation(Produces.class);
     if (produces != null) {
       md.setMediaTypesProduced(produces.value());
     }
 
     Consumes consumes = method.getAnnotation(Consumes.class);
     if (consumes != null) {
       md.setMediaTypesConsumed(consumes.value());
     }
 
     MethodParamProcessor mpp = new MethodParamProcessor();
     mpp.processAnnotations(method, md);
 
 
     // --- Process TGIRest annotations
     ResourceMethod resourceMethod = method.getAnnotation(ResourceMethod.class);
     if (resourceMethod != null) {
       md.setStatus(resourceMethod.status().getStatusCode());
 
       md.setDescription(resourceMethod.description());
 
       md.setResponseErrors(getResponseErrors(resourceMethod));
     }
 
     return md;
   }
 
   /**
    * Determines the HTTP method the method is annotated with.
    *
    * @param method the resource method to check the HTTP method for
    * @return the HTTP method
    */
   public String getHttpMethod(Method method) {
     String httpMethod = "";
 
     if (method.getAnnotations().length > 0) {
       for (Annotation a : method.getAnnotations()) {
         if (a.annotationType().isAnnotationPresent(HttpMethod.class)) {
           httpMethod = a.annotationType().getAnnotation(HttpMethod.class).value();
           break;
         }
       }
     }
 
     return httpMethod;
   }
 
   /**
    * Gets the error documentation for the resource method annotation.
    *
    * @param resourceMethod
    * @return a list of ResponseErrors in the parameter
    */
   public List<ResponseError> getResponseErrors(ResourceMethod resourceMethod) {
     List<ResponseError> errors = new ArrayList<ResponseError>();
 
     if (resourceMethod != null) {
       MethodError[] methodErrors = resourceMethod.errors();
 
       if (methodErrors.length > 0) {
         for (MethodError methodError: methodErrors) {
           ResponseError e = new ResponseError();
           e.setStatus(methodError.status().getStatusCode());
           e.setCause(methodError.cause());
           errors.add(e);
         }
       }
     }
 
     return errors;
   }
 }
