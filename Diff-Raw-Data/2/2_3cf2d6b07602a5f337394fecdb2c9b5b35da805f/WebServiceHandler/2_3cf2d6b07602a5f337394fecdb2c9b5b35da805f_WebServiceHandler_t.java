 /**
  * <copyright>
  *
  * Copyright (c) 2011 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: WebServiceHandler.java,v 1.8 2011/09/14 15:35:48 mtaal Exp $
  */
 package org.eclipse.emf.texo.server.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.eclipse.emf.texo.component.ComponentProvider;
 import org.eclipse.emf.texo.component.TexoComponent;
 import org.eclipse.emf.texo.server.service.DeleteModelOperation;
 import org.eclipse.emf.texo.server.service.ModelOperation;
 import org.eclipse.emf.texo.server.service.RetrieveModelOperation;
 import org.eclipse.emf.texo.server.service.ServiceContext;
 import org.eclipse.emf.texo.server.service.ServiceUtils;
 import org.eclipse.emf.texo.server.service.UpdateInsertModelOperation;
 import org.eclipse.emf.texo.server.store.EntityManagerObjectStore;
 import org.eclipse.emf.texo.server.store.EntityManagerProvider;
 
 /**
  * The base implementation of a CRUD Rest WS. It is the basis of the XML and JSON CRUD REST functions.
  * 
  * It is controlled by one servlet instance. One instance can be called by multiple requests, so it should not hold
  * state.
  * 
  * This class make use of the {@link ModelOperation} and the {@link ServiceContext} classes to execute the operation and
  * compute the result. The {@link ServiceContext} results are processed through a {@link ServiceContextResultProcessor},
  * you can set the class to use for the {@link ServiceContextResultProcessor} to for example process the resulting XML
  * through a XSLT script.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  */
 public abstract class WebServiceHandler implements TexoComponent {
   private String uri = null;
   private ServiceContextResultProcessor serviceContextResultProcessor = ComponentProvider.getInstance().newInstance(
       ServiceContextResultProcessor.class);
 
   public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     final ServiceContext serviceContext = createServiceContext(req);
 
     try {
       final DeleteModelOperation operation = ComponentProvider.getInstance().newInstance(DeleteModelOperation.class);
       operation.setServiceContext(serviceContext);
 
       operation.execute();
 
       setResultInResponse(serviceContext, resp);
 
       operation.close();
     } finally {
       releaseEntityManager((EntityManager) serviceContext.getObjectStore().getDelegate());
     }
   }
 
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     final ServiceContext serviceContext = createServiceContext(req);
 
     try {
       final RetrieveModelOperation retrieveModelOperation = ComponentProvider.getInstance().newInstance(
           RetrieveModelOperation.class);
       retrieveModelOperation.setServiceContext(serviceContext);
 
       retrieveModelOperation.execute();
 
       setResultInResponse(serviceContext, resp);
 
       retrieveModelOperation.close();
     } finally {
       releaseEntityManager((EntityManager) serviceContext.getObjectStore().getDelegate());
     }
   }
 
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     ServiceContext serviceContext = createServiceContext(req);
 
     if (serviceContext.isErrorOccured()) {
       setResultInResponse(serviceContext, resp);
       return;
     }
     try {
       final UpdateInsertModelOperation operation = ComponentProvider.getInstance().newInstance(
           UpdateInsertModelOperation.class);
       operation.setServiceContext(serviceContext);
 
       operation.execute();
 
       setResultInResponse(serviceContext, resp);
 
       operation.close();
     } finally {
       releaseEntityManager((EntityManager) serviceContext.getObjectStore().getDelegate());
     }
   }
 
   /**
    * This implementation calls the {@link WebServiceHandler#doPost(HttpServletRequest, HttpServletResponse)} method.
    */
   public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     doPost(req, resp);
   }
 
   /**
    * Create a web service specific implementation of the {@link ServiceContext}. Calls {@link #createServiceContext()}
    * and then copies information from the {@link HttpServletRequest} to the service context. Is called once for each
    * request.
    */
   protected ServiceContext createServiceContext(HttpServletRequest request) {
     final String requestUrl = request.getRequestURL().toString();
     EntityManager entityManager = null;
     ServiceContext serviceContext = null;
 
     try {
       serviceContext = createServiceContext();
 
      entityManager = createEntityManager();
       serviceContext.setRequestURI(requestUrl);
 
       serviceContext.setServiceRequestURI(request.getPathInfo());
 
       final EntityManagerObjectStore emObjectStore = ComponentProvider.getInstance().newInstance(
           EntityManagerObjectStore.class);
       emObjectStore.setEntityManager(entityManager);
 
       // find the uri on the basis of the request uri
       String objectStoreUri = getUri() == null ? request.getContextPath() : getUri();
       if (getUri() == null) {
         final int contextIndex = requestUrl.indexOf(request.getContextPath() + "/"); //$NON-NLS-1$ 
         objectStoreUri = requestUrl.substring(0, contextIndex) + request.getContextPath() + request.getServletPath();
       } else {
         objectStoreUri = getUri();
       }
       emObjectStore.setUri(objectStoreUri);
 
       final Map<String, Object> params = new HashMap<String, Object>();
       for (Enumeration<String> enumeration = request.getParameterNames(); enumeration.hasMoreElements();) {
         final String name = enumeration.nextElement();
         final String[] vals = request.getParameterValues(name);
         if (vals.length == 1) {
           params.put(name, vals[0]);
         } else {
           params.put(name, vals);
         }
       }
       serviceContext.setObjectStore(emObjectStore);
       serviceContext.setRequestParameters(params);
 
       serviceContext.setRequestContent(ServiceUtils.toString(request.getInputStream(), request.getCharacterEncoding()));
 
       return serviceContext;
     } catch (Throwable t) {
       if (entityManager != null) {
         releaseEntityManager(entityManager);
       }
 
       if (serviceContext != null) {
         serviceContext.createErrorResult(t);
         return serviceContext;
       }
       throw new IllegalStateException(t);
     }
   }
 
   /**
    * Calls the {@link ServiceContext#getResponseContent()} and {@link ServiceContext#getResponseCode()} and the
    * {@link ServiceContext#getResponseContentType()}.
    * 
    * The {@link ServiceContextResultProcessor} is used to post process the results of these methods before writing them
    * to the {@link HttpServletResponse}.
    */
   protected void setResultInResponse(ServiceContext serviceContext, HttpServletResponse resp) throws IOException {
 
     serviceContextResultProcessor.startOfRequest();
 
     serviceContextResultProcessor.setServiceContext(serviceContext);
 
     final String result = serviceContextResultProcessor.getResponseContent();
 
     // must be done before writing and closing the writer
     resp.setContentType(serviceContextResultProcessor.getResponseContentType());
     resp.setStatus(serviceContextResultProcessor.getResponseCode());
 
     resp.getWriter().write(result);
     resp.getWriter().close();
 
     serviceContextResultProcessor.endOfRequest();
   }
 
   /**
    * Create a web service specific service context.
    */
   protected abstract ServiceContext createServiceContext();
 
   /**
    * This method is called once at the beginning of the processing of a request. It should return a new instance of the
    * entity manager normally.
    * 
    * As a default calls {@link EntityManagerProvider#createEntityManager()}.
    * 
    * Can be overridden for specific logic to get the entity manager. In that case also override the
    * {@link #releaseEntityManager(EntityManager)}.
    */
   protected EntityManager createEntityManager() {
     return EntityManagerProvider.getInstance().createEntityManager();
   }
 
   /**
    * Is called once at the end of the processing of a request.
    * 
    * As a default calls {@link EntityManagerProvider#releaseEntityManager(EntityManager)}.
    */
   protected void releaseEntityManager(EntityManager entityManager) {
     EntityManagerProvider.getInstance().releaseEntityManager(entityManager);
   }
 
   public String getUri() {
     return uri;
   }
 
   public void setUri(String uri) {
     this.uri = uri;
   }
 
   /**
    * Can be used to let the response be processed before it is written to the {@link HttpServletResponse}.
    * 
    * This owning {@link WebServiceHandler} has one instance of this class which is re-used in different methods. Note
    * the {@link WebServiceHandler} is not thread-safe and should only be used in one request/thread and then discarded.
    * The same applies to the {@link ServiceContextResultProcessor}. The {@link ServiceContextResultProcessor} is
    * notified of the start of the request and the end of the request processing.
    * 
    * The ServletContextResultWrapper can also override the status code and the response type.
    * 
    * @author mtaal
    */
   public static class ServiceContextResultProcessor implements TexoComponent {
 
     private ServiceContext serviceContext;
 
     /**
      * Calls the {@link ServiceContext#getResponseContent()} and returns the result.
      */
     public String getResponseContent() {
       return serviceContext.getResponseContent();
     }
 
     /**
      * Calls the {@link ServiceContext#getResponseCode()} and returns the result.
      */
     public int getResponseCode() {
       return serviceContext.getResponseCode();
     }
 
     /**
      * Calls the {@link ServiceContext#getResponseContentType()} and returns the result.
      */
     public String getResponseContentType() {
       return serviceContext.getResponseContentType();
     }
 
     public ServiceContext getServiceContext() {
       return serviceContext;
     }
 
     public void setServiceContext(ServiceContext serviceContext) {
       this.serviceContext = serviceContext;
     }
 
     /**
      * Is called at the beginning of the request.
      */
     public void startOfRequest() {
     }
 
     /**
      * Is called at the end of the request.
      */
     public void endOfRequest() {
     }
   }
 
   /**
    * A subclass of the {@link ServletContextResultWrapper} which processes the result through a XSLT template.
    * 
    * @author mtaal
    */
   public static class XSLTServiceContextResultProcessor extends ServiceContextResultProcessor {
 
     private boolean errorOccured = false;
 
     private String templateClassPathLocation = null;
 
     private Map<String, Object> parameters = new HashMap<String, Object>();
 
     @Override
     public String getResponseContent() {
       try {
         return applyTemplate(getServiceContext().getResponseContent(), getTemplateClassPathLocation());
       } catch (Exception e) {
         errorOccured = true;
         throw new IllegalStateException(e);
       }
     }
 
     @Override
     public int getResponseCode() {
       if (errorOccured) {
         return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
       }
       return getServiceContext().getResponseCode();
     }
 
     public void addParameter(String name, Object value) {
       parameters.put(name, value);
     }
 
     private String applyTemplate(String xml, String template) {
       try {
         final InputStream is = this.getClass().getResourceAsStream(template);
         final TransformerFactory factory = TransformerFactory.newInstance();
         final Transformer transformer = factory.newTransformer(new StreamSource(is));
         for (String key : parameters.keySet()) {
           transformer.setParameter(key, parameters.get(key));
         }
         final StringWriter sw = new StringWriter();
         final StreamResult response = new StreamResult(sw);
         transformer.transform(new StreamSource(new StringReader(xml)), response);
 
         return sw.toString();
       } catch (final Exception e) {
         throw new IllegalStateException(e);
       }
     }
 
     public String getTemplateClassPathLocation() {
       return templateClassPathLocation;
     }
 
     public void setTemplateClassPathLocation(String templateClassPathLocation) {
       this.templateClassPathLocation = templateClassPathLocation;
     }
   }
 
   public ServiceContextResultProcessor getServiceContextResultProcessor() {
     return serviceContextResultProcessor;
   }
 
   public void setServiceContextResultProcessor(ServiceContextResultProcessor serviceContextResultProcessor) {
     this.serviceContextResultProcessor = serviceContextResultProcessor;
   }
 }
