 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.module.atom;
 
 import org.mule.api.lifecycle.CreateException;
 import org.mule.api.lifecycle.InitialisationException;
 import org.mule.api.routing.filter.Filter;
 import org.mule.api.service.Service;
 import org.mule.api.transport.Connector;
 import org.mule.transport.abdera.InboundFeedSplitter;
import org.mule.transport.abdera.filters.LastUpdatedFilter;
 import org.mule.transport.http.HttpMessageReceiver;
 
 /**
  * TODO
  */
 public class AtomMessageReceiver extends HttpMessageReceiver
 {
     public AtomMessageReceiver(Connector connector, Service service, org.mule.api.endpoint.InboundEndpoint inboundEndpoint)
             throws CreateException
     {
         super(connector, service, inboundEndpoint);
     }
 
     @Override
     protected void doInitialise() throws InitialisationException
     {
         AtomConnector con = (AtomConnector) getConnector();
         if (con.isSplitFeed())
         {
            Filter filter = new LastUpdatedFilter(con.getLastUpdate());
             InboundFeedSplitter splitter = new InboundFeedSplitter();
             splitter.setEntryFilter(filter);
             splitter.setMuleContext(connector.getMuleContext());
             splitter.setAcceptedContentTypes(con.getAcceptedContentTypes());
             splitter.initialise();
             getService().getInboundRouter().addRouter(splitter);
         }
         super.doInitialise();
     }
 }
