 /**************************************************************************************
  * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
  * http://fusesource.com                                                              *
  * ---------------------------------------------------------------------------------- *
  * The software in this package is published under the terms of the AGPL license      *
  * a copy of which has been included with this distribution in the license.txt file.  *
  **************************************************************************************/
 package org.fusesource.cloudmix.common.jaxrs;
 
 import javax.ws.rs.ext.ContextResolver;
 import javax.ws.rs.ext.Provider;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 
 /**
  * A resolver of the JAXB context primed for our XML languages
  *
  * @version $Revision: 1.1 $
  */
 @Provider
 public class JAXBContextResolver implements ContextResolver<JAXBContext> {
    private static final String JAXB_PACKAGES = "org.fusesource.cloudmix.common.dto";
 
     private final String packages = JAXB_PACKAGES;
     private final JAXBContext context;
 
     public JAXBContextResolver() throws JAXBException {
         this.context = JAXBContext.newInstance(packages);
     }
 
     public JAXBContext getContext(Class<?> objectType) {
         Package aPackage = objectType.getPackage();
         if (aPackage != null) {
             String name = aPackage.getName();
             if (name.length() > 0) {
                 if (packages.contains(name)) {
                     return context;
                 }
             }
         }
         return null;
     }
 
     public String getPackages() {
         return packages;
     }
 
     public JAXBContext getContext() {
         return context;
     }
 }
 
