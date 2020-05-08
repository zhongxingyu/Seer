 /**
  * Mule Magento Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mulesoft.demo.magento;
 
 import org.mule.api.MuleEvent;
 import org.mule.api.transport.PropertyScope;
 import org.mule.construct.SimpleFlowConstruct;
 import org.mule.tck.FunctionalTestCase;
 
 import java.util.Collections;
 
 public class MagentoFunctionalTestDriver extends FunctionalTestCase
 {
 
     @Override
     protected String getConfigResources()
     {
         return "mule-config.xml";
     }
 
    public void testSearchAndUploadMedia() throws Exception
     {
         System.out.println(lookupFlowConstruct("MainFlow").process(
             getTestEvent(Collections.singletonMap("productType", "simple"))));
     }
    
    /**
     * Creates some products for this test. Run this test only 
     * once
     */
    public void ignoreTestSetupS3BucketFlow() throws Exception
    {
        lookupFlowConstruct("SetupMagentoFlow").process(getTestEvent(""));
    }
    
    /**
     * Uploads an image to S3. Run this test only once
     */
    public void ignoreTestSetupS3ImageFlow() throws Exception
    {
        lookupFlowConstruct("SetupS3ImageFlow").process(getTestEvent(""));
    }
    
    /**
     * Creates some images for this test. Run this test only 
     * once
     */
    public void ignoreTestSetupS3Flow() throws Exception
    {
        lookupFlowConstruct("SetupS3BucketFlow").process(getTestEvent(""));
    }
 
     private SimpleFlowConstruct lookupFlowConstruct(final String name)
     {
         return (SimpleFlowConstruct) muleContext.getRegistry().lookupFlowConstruct(name);
     }
 
 }
