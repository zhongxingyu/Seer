 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.pubsubhubbub;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.lang.StringUtils;
 import org.mule.RequestContext;
 import org.mule.api.MuleContext;
 import org.mule.api.MuleEvent;
 import org.mule.api.MuleException;
 import org.mule.api.MuleMessage;
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Module;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.param.Payload;
 import org.mule.api.context.MuleContextAware;
 import org.mule.api.retry.RetryPolicyTemplate;
 import org.mule.api.store.PartitionableObjectStore;
 import org.mule.module.pubsubhubbub.data.DataStore;
 import org.mule.module.pubsubhubbub.handler.AbstractHubActionHandler;
 import org.mule.retry.async.AsynchronousRetryTemplate;
 import org.mule.retry.policies.SimpleRetryPolicyTemplate;
 import org.mule.transport.http.HttpConnector;
 import org.mule.transport.http.HttpConstants;
 
 @Module(name = "pubsubhubbub", namespace = "http://www.mulesoft.org/schema/mule/pubsubhubbub", schemaLocation = "http://www.mulesoft.org/schema/mule/pubsubhubbub/3.2/mule-pubsubhubbub.xsd")
 public class HubModule implements MuleContextAware
 {
     private MuleContext muleContext;
 
     private DataStore dataStore;
 
     private Map<HubMode, AbstractHubActionHandler> requestHandlers;
 
     @Configurable
     private PartitionableObjectStore<Serializable> objectStore;
 
     @Configurable
     private int retryFrequency;
 
     @Configurable
     private int retryCount;
 
     @PostConstruct
     public void wireResources()
     {
         dataStore = new DataStore(objectStore);
 
         final RetryPolicyTemplate hubRetryPolicyTemplate = new AsynchronousRetryTemplate(
             new SimpleRetryPolicyTemplate(retryFrequency, retryCount));
 
         requestHandlers = new HashMap<HubMode, AbstractHubActionHandler>();
         for (final HubMode hubMode : HubMode.values())
         {
             requestHandlers.put(hubMode, hubMode.newHandler(muleContext, dataStore, hubRetryPolicyTemplate));
         }
     }
 
     @Processor(name = "hub")
     public MuleMessage handleRequest(@Payload final Object payload) throws MuleException, DecoderException
     {
         return handleRequest(RequestContext.getEvent(), payload).buildMuleMessage(muleContext);
     }
 
     private HubResponse handleRequest(final MuleEvent muleEvent, final Object payload)
         throws MuleException, DecoderException
     {
         final MuleMessage message = muleEvent.getMessage();
 
         if (!StringUtils.equalsIgnoreCase(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY, ""),
             HttpConstants.METHOD_POST))
         {
 
             return HubResponse.badRequest("HTTP method must be: POST");
         }
 
         if (!StringUtils.startsWith(message.getInboundProperty(HttpConstants.HEADER_CONTENT_TYPE, ""),
             Constants.WWW_FORM_URLENCODED_CONTENT_TYPE))
         {
             return HubResponse.badRequest("Content type must be: application/x-www-form-urlencoded");
         }
 
         final Map<String, List<String>> parameters = HubUtils.getHttpPostParameters(muleEvent);
 
         for (final Entry<String, List<String>> param : parameters.entrySet())
         {
             if ((param.getValue().size() > 1)
                 && (!Constants.SUPPORTED_MULTIVALUED_PARAMS.contains(param.getKey())))
             {
                 return HubResponse.badRequest("Multivalued parameters are only supported for: "
                                               + StringUtils.join(Constants.SUPPORTED_MULTIVALUED_PARAMS, ','));
             }
         }
 
         // carry the request encoding as a parameters for usage downstream
         parameters.put(Constants.REQUEST_ENCODING_PARAM, Collections.singletonList(muleEvent.getEncoding()));
 
         try
         {
             return handleRequest(parameters);
         }
         catch (final IllegalArgumentException exception)
         {
             return HubResponse.badRequest(exception.getMessage());
         }
     }
 
     private HubResponse handleRequest(final Map<String, List<String>> parameters)
     {
         final HubMode hubMode = HubMode.parse(HubUtils.getMandatoryStringParameter(Constants.HUB_MODE_PARAM,
             parameters));
 
         return requestHandlers.get(hubMode).handle(parameters);
     }
 
     public void setMuleContext(final MuleContext muleContext)
     {
         this.muleContext = muleContext;
     }
 
     public MuleContext getMuleContext()
     {
         return muleContext;
     }
 
     public void setObjectStore(final PartitionableObjectStore<Serializable> objectStore)
     {
         this.objectStore = objectStore;
     }
 
     public PartitionableObjectStore<Serializable> getObjectStore()
     {
         return objectStore;
     }
 
     public int getRetryFrequency()
     {
         return retryFrequency;
     }
 
     public void setRetryFrequency(final int retryFrequency)
     {
         this.retryFrequency = retryFrequency;
     }
 
     public int getRetryCount()
     {
         return retryCount;
     }
 
     public void setRetryCount(final int retryCount)
     {
         this.retryCount = retryCount;
     }
 }
