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
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.lang.StringUtils;
 import org.mule.module.pubsubhubbub.handler.AbstractHubActionHandler;
 
 /**
  * Resource that is the main entry point to the hub.
  */
 @Path("/hub")
 @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 @Produces(MediaType.TEXT_PLAIN)
 public class HubResource
 {
     private Map<HubMode, AbstractHubActionHandler> handlers;
 
     @POST
     public Response handleRequest(final MultivaluedMap<String, String> formParams)
     {
         for (final Entry<String, List<String>> param : formParams.entrySet())
         {
             if ((param.getValue().size() > 1) && (!param.getKey().equals(Constants.HUB_VERIFY_PARAM))
                 && (!param.getKey().equals(Constants.HUB_TOPIC_PARAM)))
             {
                 throw new IllegalArgumentException("Multivalued parameters are only supported for: "
                                                    + StringUtils.join(
                                                        new String[]{Constants.HUB_VERIFY_PARAM,
                                                            Constants.HUB_TOPIC_PARAM}, ','));
             }
         }
 
         final HubMode hubMode = HubMode.parse(getMandatoryStringParameter(Constants.HUB_MODE_PARAM,
             formParams));
 
         final AbstractHubActionHandler handler = handlers.get(hubMode);
 
         if (handler == null)
         {
             throw new UnsupportedOperationException(hubMode.toString());
         }
 
         return handler.handle(formParams);
     }
 
     public void setHandlers(final Map<HubMode, AbstractHubActionHandler> handlers)
     {
         this.handlers = handlers;
     }
 
     public Map<HubMode, AbstractHubActionHandler> getHandlers()
     {
         return handlers;
     }
 
     public static String getMandatoryStringParameter(final String name,
                                                      final MultivaluedMap<String, String> formParams)
     {
         final String value = formParams.getFirst(name);
 
         if (StringUtils.isEmpty(value))
         {
             throw new IllegalArgumentException("Missing mandatory parameter: " + name);
         }
 
         return value;
     }
 
     public static URI getMandatoryUrlParameter(final String name,
                                                final MultivaluedMap<String, String> formParams)
     {
         final String value = getMandatoryStringParameter(name, formParams);
 
         try
         {
             final URI uri = new URI(value);
 
             if (StringUtils.isNotEmpty(uri.getFragment()))
             {
                 throw new IllegalArgumentException("Fragment found in URL parameter: " + name);
             }
 
             return uri;
         }
         catch (final URISyntaxException use)
         {
             use.printStackTrace();
             throw new IllegalArgumentException("Invalid URL parameter: " + name, use);
         }
     }
 
     public static List<URI> getMandatoryUrlParameters(final String name,
                                                       final MultivaluedMap<String, String> formParams)
     {
 
         final List<String> values = formParams.get(name);
 
         if ((values == null) || (values.isEmpty()))
         {
             throw new IllegalArgumentException("Missing mandatory parameter: " + name);
         }
 
         try
         {
             final List<URI> uris = new ArrayList<URI>();
 
             for (final String value : values)
             {
 
                 final URI uri = new URI(value);
 
                 if (StringUtils.isNotEmpty(uri.getFragment()))
                 {
                     throw new IllegalArgumentException("Fragment found in URL parameter: " + name);
                 }
 
                 uris.add(uri);
             }
             return uris;
         }
         catch (final URISyntaxException use)
         {
             use.printStackTrace();
             throw new IllegalArgumentException("Invalid URL parameter: " + name, use);
         }
     }
 }
