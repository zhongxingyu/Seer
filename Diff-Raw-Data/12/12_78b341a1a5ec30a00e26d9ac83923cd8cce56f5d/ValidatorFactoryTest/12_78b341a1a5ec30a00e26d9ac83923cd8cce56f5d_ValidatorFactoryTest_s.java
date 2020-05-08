 /*
  * Copyright (C) 2006, 2007 Carl E Harris, Jr.
  * 
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or (at
  * your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
  * License for more details.
  */
 package org.soulwing.cas.client;
 
 import org.jdom.Element;
 import org.soulwing.cas.client.jdom.ProxyValidateMappingStrategy;

import junit.framework.TestCase;
 
 public class ValidatorFactoryTest extends TestCase {
 
   protected void setUp() throws Exception {
     super.setUp();
   }
 
   public void testConfigureProxyValidateMappingStrategy() {
     ValidatorFactory.setServiceValidateMappingStrategy(
         new MockProtocolMappingStrategy());
     ProxyValidateMappingStrategy strategy = (ProxyValidateMappingStrategy)
       ValidatorFactory.getProxyValidateMappingStrategy();
     assertTrue(strategy.getServiceValidateMappingStrategy() 
         instanceof MockProtocolMappingStrategy);
   }
   
   private static class MockProtocolMappingStrategy
       implements ProtocolMappingStrategy {
 
     public Response mapResponse(Element element) {
       throw new UnsupportedOperationException();
     }
     
   }
 }
