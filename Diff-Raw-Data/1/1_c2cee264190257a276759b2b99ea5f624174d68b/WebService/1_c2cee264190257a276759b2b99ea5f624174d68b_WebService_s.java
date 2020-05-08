 package de.hszg.atocc.core.pluginregistry;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import org.restlet.resource.ServerResource;
 
 @Target(ElementType.TYPE)
 @Retention(RetentionPolicy.RUNTIME)
 public @interface WebService {
 
     String url();
 
     Class<? extends ServerResource> resource();
 
 }
