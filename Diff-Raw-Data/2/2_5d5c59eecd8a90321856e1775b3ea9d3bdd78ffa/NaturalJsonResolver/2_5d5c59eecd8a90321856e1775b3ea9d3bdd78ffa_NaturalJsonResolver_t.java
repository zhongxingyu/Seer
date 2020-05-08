 package com.mgl.restdemo.rest;
 
 import java.util.logging.Level;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.ext.ContextResolver;
 import javax.ws.rs.ext.Provider;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.sun.jersey.api.json.JSONJAXBContext;
 import lombok.extern.java.Log;
 
 @Provider
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 @Log
 public class NaturalJsonResolver implements ContextResolver<JAXBContext> {
 
    private static final String PACKAGE = "com.mgl.restdemo.domain";
 
     private final JAXBContext jsonContext;
 
     public NaturalJsonResolver() {
         try {
             jsonContext = new JSONJAXBContext(
                     JSONConfiguration.natural()
                         .humanReadableFormatting(true)
                         .rootUnwrapping(true)
                         .build(),
                     PACKAGE);
         } catch (JAXBException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     @Override
     public JAXBContext getContext(Class<?> type) {
         if (PACKAGE.equals(type.getPackage().getName())) {
             return jsonContext;
         } else {
             log.log(Level.WARNING, "could not find a JAXBContext for class: {0}", type.getName());
             return null;
         }
     }
 
 }
