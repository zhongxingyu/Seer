 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *   Dzmitry [zmicer] Harachka - implementation
  * </copyright>
  *
  * $Id: ModelOperation.java,v 1.6 2011/08/29 05:22:24 mtaal Exp $
  */
 package org.eclipse.emf.texo.server.service;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.texo.component.ComponentProvider;
 import org.eclipse.emf.texo.component.TexoComponent;
 import org.eclipse.emf.texo.json.JSONValueConverter;
 import org.eclipse.emf.texo.server.model.request.Parameter;
 import org.eclipse.emf.texo.server.model.request.QueryType;
 import org.eclipse.emf.texo.server.model.request.RequestModelPackage;
 import org.eclipse.emf.texo.store.ObjectStore;
 
 /**
  * The base abstract class for model operations. Model operations can for example be crud operations on model instances.
  * 
  * @author <a href="mailto:zmicer.harachka@gmail.com">Dzmitry [zmicer] Harachka</a>
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.6 $
  */
 public abstract class ModelOperation implements TexoComponent {
 
   private static final String FALSE = Boolean.FALSE.toString();
 
   private ServiceContext serviceContext;
 
   /**
    * Execute the model operation, if an object store is set then the operation is executed within an object store
    * transaction. The {@link ObjectStore#begin()} method is called before the operation, {@link ObjectStore#commit()} is
    * called after the operation.
    * 
    * This method calls the method {@link ModelOperation#internalExecute()} which is implemented by a specific subclass.
    * 
    * If an exception occurs the transaction is rolledback ({@link ObjectStore#rollback()} and an error result is
    * returned (see {@link ServiceContext#createErrorResult(Throwable)}).
    */
   public void execute() {
     try {
       if (getObjectStore() != null) {
         getObjectStore().begin();
       }
       internalExecute();
       if (getObjectStore() != null) {
         getObjectStore().commit();
       }
     } catch (Throwable t) {
       t.printStackTrace(System.err);
       getServiceContext().createErrorResult(t);
       if (getObjectStore() != null) {
         getObjectStore().rollback();
       }
     }
   }
 
   /**
    * Method implemented by the subclass to actually execute the operation.
    */
   protected abstract void internalExecute();
 
   public ServiceContext getServiceContext() {
     return serviceContext;
   }
 
   public void setServiceContext(ServiceContext serviceContext) {
     this.serviceContext = serviceContext;
   }
 
   public ObjectStore getObjectStore() {
     return getServiceContext().getObjectStore();
   }
 
   protected QueryType getQueryType() {
     QueryType queryType = null;
     if (getServiceContext().getRequestContent() != null && getServiceContext().getRequestContent().trim().length() > 0) {
       final List<Object> requestData = getServiceContext().getRequestData();
       for (Object object : requestData) {
         if (object instanceof QueryType) {
           queryType = (QueryType) object;
           break;
         }
       }
     }
 
     // 1) there is a query!
    final String namedQueryParam = (String) getServiceContext().getRequestParameters().get(
        ServiceConstants.PARAM_NAMEDQUERY);
     final String qryStrParam = (String) getServiceContext().getRequestParameters().get(ServiceConstants.PARAM_QUERY);
    if (queryType == null && qryStrParam != null && qryStrParam.trim().length() > 0) {
       getServiceContext().getServiceOptions().checkFalse(ServiceOptions.OPTION_ALLOW_RETRIEVE_QUERIES);
       queryType = RequestModelPackage.INSTANCE.getModelFactory().createQueryType();
       queryType.setQuery(qryStrParam);
    } else if (queryType == null && namedQueryParam != null) {
      queryType = RequestModelPackage.INSTANCE.getModelFactory().createQueryType();
      queryType.setNamedQuery(namedQueryParam);
    }

    if (queryType != null) {
       queryType.setFirstResult(getFirstResult());
       queryType.setMaxResults(getMaxResults());
       final String noCountParam = (String) getServiceContext().getRequestParameters().get(
           ServiceConstants.PARAM_NO_COUNT);
       queryType.setDoCount(noCountParam == null || FALSE.equals(noCountParam));
     }
 
     if (queryType == null) {
       return queryType;
     }
 
     if (queryType.getQuery() != null) {
       // check the query
       ComponentProvider.getInstance().newInstance(QueryChecker.class).checkQuery(queryType.getQuery());
     }
 
     // if there is no limit on the max results then don't count
     if (queryType.getMaxResults() == -1) {
       queryType.setDoCount(false);
     }
 
     return queryType;
   }
 
   protected Map<String, Object> getParameters(QueryType queryType) {
     final Map<String, Object> result = new HashMap<String, Object>();
 
     // get the queryparameters from the request
     for (String key : getServiceContext().getRequestParameters().keySet()) {
       if (key.startsWith(ServiceConstants.QUERY_PARAM_PREFIX)) {
         final Object value = getServiceContext().getRequestParameters().get(key);
         final String name = key.substring(ServiceConstants.QUERY_PARAM_PREFIX.length());
         result.put(name, value);
       }
     }
     if (queryType == null) {
       return result;
     }
 
     final JSONValueConverter converter = ComponentProvider.getInstance().newInstance(JSONValueConverter.class);
     for (Parameter parameter : queryType.getParameters()) {
       final String type = parameter.getType();
       Object value = parameter.getValue();
       if (type != null) {
         if ("date".equals(type)) { //$NON-NLS-1$
           value = converter.createDateFromJSON(value);
         } else if ("dateTime".equals(type)) { //$NON-NLS-1$
           value = converter.createDateTimeFromJSON(value);
         } else {
           value = converter.createTimeFromJSON(value);
         }
       }
       result.put(parameter.getName(), value);
     }
     return result;
   }
 
   protected int getFirstResult() {
     return getNumberParameter(ServiceConstants.PARAM_FIRST_RESULT);
   }
 
   protected int getMaxResults() {
     return getNumberParameter(ServiceConstants.PARAM_MAX_RESULTS);
   }
 
   private int getNumberParameter(String paramName) {
     if (getServiceContext().getRequestParameters().containsKey(paramName)) {
       try {
         return Integer.parseInt((String) getServiceContext().getRequestParameters().get(paramName));
       } catch (NumberFormatException e) {
         // ignore on purpose
         return -1;
       }
     }
     return -1;
   }
 
   /**
    * Closes the underlying objectstore, if it was set.
    */
   public void close() {
     if (getObjectStore() != null) {
       getObjectStore().close();
     }
   }
 }
