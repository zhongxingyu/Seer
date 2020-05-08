 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.resource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.Resource;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.session.SessionManager;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * A ClassPathStylesheetResource is a static resource whose content is
  * read from a classloader. It is special for its handling of occurences
  * of "url([classPath])" strings. These are read from classPath and 
  * externalized.
  *
  * @author ole
  * @version $$
  */
 public class ClassPathStylesheetResource
         extends ClassPathResource {
     private final transient static Log log = LogFactory.getLog(ClassPathStylesheetResource.class);
     /**
      * The default max size for the buffer. since we do some replacement every time we
      * read the input stream, we want this to be high, so we can cache the
      * interpreted style sheet.
      */
     private static final int MAX_BUFFER_SIZE = 24 * 1024; // 24kb
 
     private transient ExternalizeManager extManager;
 
     /**
      * A static css resource that is obtained from the default classpath.
      */
     public ClassPathStylesheetResource(String resourceFileName) {
        this(Thread.currentThread().getContextClassLoader(), resourceFileName, "unkonwn");
     }
 
     /**
      * A static css resource that is obtained from the default classpath.
      */
     public ClassPathStylesheetResource(String resourceFileName, String mimeType) {
        this(Thread.currentThread().getContextClassLoader(), resourceFileName, mimeType);
     }
 
     /**
      * A static css resource that is obtained from the specified class loader
      *
      * @param classLoader      the classLoader from which the resource is obtained
      * @param resourceFileName the css resource relative to the baseClass
      */
     public ClassPathStylesheetResource(ClassLoader classLoader, String resourceFileName) {
         this(classLoader, resourceFileName, "unknown");
     }
 
     /**
      * A static css resource that is obtained from the specified class loader
      *
      * @param classLoader      the classLoader from which the resource is obtained
      * @param resourceFileName the css resource relative to the baseClass
      * @param mimeType         the mimetype of the resource
      */
     public ClassPathStylesheetResource(ClassLoader classLoader, String resourceFileName, String mimeType) {
         this(classLoader, resourceFileName, mimeType, MAX_BUFFER_SIZE);
     }
 
     /**
      * A static css resource that is obtained from the specified class loader
      *
      * @param classLoader      the classLoader from which the resource is obtained
      * @param resourceFileName the css resource relative to the baseClass
      * @param mimeType         the mimetype of the resource
      * @param maxBufferSize    the maximum buffer size for the style sheet. If
      *                         big enough, stylesheet is cached, else parsed again. 
      */
     public ClassPathStylesheetResource(ClassLoader classLoader, String resourceFileName, String mimeType, int maxBufferSize) {
         super(classLoader, resourceFileName, mimeType);
         // need to set it here, because at this moment there is a session in the sessionManager
         extManager = SessionManager.getSession().getExternalizeManager();
         // we need a bigger buffer, so that the parsing only happens once
         setMaxBufferSize(maxBufferSize);
     }
 
     /* (non-Javadoc)
      * @see org.wings.StaticResource#getResourceStream()
      */
     @Override
     protected final InputStream getResourceStream() throws ResourceNotFoundException {
         InputStream in = getClassLoader().getResourceAsStream(resourceFileName);
         if (in != null) {
             return new CssUrlFilterInputStream(in, getExtManager());
         } else {
             throw new ResourceNotFoundException("Unable to find classpath resource "+resourceFileName);
         }
     }
 
     /*
      * the equal() and hashCode() method make sure, that the same resources
      * get the same name in the SystemExternalizer.
      */
 
     /**
      * Two ClassPathStylesheetResource are equal if both of them are instances
      * of ClassPathStylesheetResource and the equals method of ClassPathResource
      * is true.
      *
      * @return true if the two instances are equal.
      */
     public boolean equals(Object o) {
         if (o instanceof ClassPathStylesheetResource) {
             if (super.equals(o)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Simple hascode implementation
      */
     public int hashCode() {
         return super.hashCode();
     }
 
     /* (non-Javadoc)
      * @see org.wings.StaticResource#bufferResource()
      */
     @Override
     protected LimitedBuffer bufferResource() throws IOException, ResourceNotFoundException {
         try {
             return super.bufferResource();
         } catch (IOException e) {
             log.error("Unable to retrieve css file from classpath: '"+resourceFileName); 
             throw e; 
         }
     }
 
     private ExternalizeManager getExtManager() {
         if (extManager == null)
             // need to set it here, because at this moment there is a session in the sessionManager
             extManager = SessionManager.getSession().getExternalizeManager();
         return extManager;
     }
 }
 
 
