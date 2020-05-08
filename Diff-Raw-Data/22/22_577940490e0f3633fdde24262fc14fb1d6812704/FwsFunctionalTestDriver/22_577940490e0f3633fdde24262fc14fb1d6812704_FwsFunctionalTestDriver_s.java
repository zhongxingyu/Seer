 /**
  * Mule FWS Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mulesoft.demo.fws;
 
import org.mule.construct.SimpleFlowConstruct;
 import org.mule.tck.FunctionalTestCase;
 
 public class FwsFunctionalTestDriver extends FunctionalTestCase
 {
     @Override
     protected String getConfigResources()
     {
         return "mule-config.xml";
     }
 
     public void testSendInventoryStatus() throws Exception
     {
         lookupFlowConstruct("InventoryStatus").process(getTestEvent(""));
     }
 
    private SimpleFlowConstruct lookupFlowConstruct(final String name)
     {
        return (SimpleFlowConstruct) muleContext.getRegistry().lookupFlowConstruct(name);
     }
 
 }
