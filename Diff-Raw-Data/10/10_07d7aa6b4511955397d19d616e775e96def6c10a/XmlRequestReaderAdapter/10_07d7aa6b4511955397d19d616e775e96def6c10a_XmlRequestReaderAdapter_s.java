 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.ows.adapters;
 
 import org.geoserver.config.ServiceInfo;
 import org.geoserver.ows.HttpServletRequestAware;
 import org.vfny.geoserver.servlets.AbstractService;
 import org.vfny.geoserver.util.requests.readers.XmlRequestReader;
 import java.io.Reader;
 import java.lang.reflect.Constructor;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.namespace.QName;
 
 
 public class XmlRequestReaderAdapter extends org.geoserver.ows.XmlRequestReader
     implements HttpServletRequestAware {
     Class delegateClass;
     ServiceInfo service;
     HttpServletRequest httpRequest;
 
     public XmlRequestReaderAdapter(QName element, ServiceInfo service, Class delegate) {
         super(element);
         this.service = service;
         this.delegateClass = delegate;
     }
 
     public XmlRequestReaderAdapter(String namespace, String local, ServiceInfo service,
         Class delegate) {
         this(new QName(namespace, local), service, delegate);
     }
 
     public void setHttpRequest(HttpServletRequest request) {
         this.httpRequest = request;
     }
 
     public Object read(Object request, Reader reader, Map kvp) throws Exception {
         //look for a constructor, may have to walk up teh class hierachy
        Class clazz = service.getClass();
         Constructor constructor = null;
 
         while (clazz != null && constructor == null) {
             try {
                constructor = delegateClass.getConstructor(new Class[] { Map.class, clazz });
             } catch (NoSuchMethodException e) {
                 Class[] classes = clazz.getInterfaces();
                 for (Class c : classes) {
                         try {
                                constructor = delegateClass.getConstructor(new Class[] { Map.class, c });
                         } catch(NoSuchMethodException e2) {
                                 // no harm done
                         }
                                 }
                 clazz = clazz.getSuperclass();
             }
         }
 
         if (constructor == null) {
             throw new IllegalStateException("No appropriate constructor");
         }
 
         XmlRequestReader delegate = (XmlRequestReader) constructor.newInstance(new Object[] { service });
         
         return delegate.read(reader, httpRequest);
     }
 }
