 /*
  * #%L
  * Bitrepository Reference Pillar
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.pillar.checksumpillar;
 
 import org.bitrepository.bitrepositorymessages.MessageResponse;
 import org.bitrepository.pillar.cache.ChecksumStore;
 import org.bitrepository.pillar.checksumpillar.messagehandler.ChecksumPillarMessageHandler;
 import org.bitrepository.pillar.common.MessageHandlerContext;
 import org.bitrepository.service.exception.RequestHandlerException;
 import org.testng.Assert;
 
 public class ChecksumPillarMessageHandlerTest extends ChecksumPillarTest {
    //@Test( groups = {"regressiontest", "pillartest"})
     public void invalidChecksumCase() throws Exception {
         addDescription("Tests that the ChecksumPillar does not start with an invalid checksum.");
         addStep("Test with checksum type 'OTHER'", "Should be invalid and throw an exception.");
         componentSettings.getCollectionSettings().getProtocolSettings().setDefaultChecksumType("OTHER");
 
         try {
             new MockChecksumMessageHandler(context, cache);
             Assert.fail("Should throw an IllegalArgumentException here.");
         } catch (IllegalArgumentException e) {
             // expected
             System.err.println(e);
         }
         
         addStep("Test with invalid checksum type", "Should be invalid and throw an exception.");
         componentSettings.getCollectionSettings().getProtocolSettings().setDefaultChecksumType("ERROR");
 
         try {
             new MockChecksumMessageHandler(context, cache);
             Assert.fail("Should throw an IllegalArgumentException here.");
         } catch (IllegalArgumentException e) {
             // expected
             System.err.println(e);
         }
 
         addStep("Check with valid checksum type (MD5)", "Should create message handler.");
         componentSettings.getCollectionSettings().getProtocolSettings().setDefaultChecksumType("MD5");
 
         ChecksumPillarMessageHandler<MessageResponse> handler = new MockChecksumMessageHandler(context, cache);
         Assert.assertNotNull(handler);
     }
 
     private class MockChecksumMessageHandler extends ChecksumPillarMessageHandler<MessageResponse> {
 
         protected MockChecksumMessageHandler(MessageHandlerContext context, ChecksumStore refCache) {
             super(context, refCache);
         }
 
         @Override
         public Class<MessageResponse> getRequestClass() {
             return MessageResponse.class;
         }
 
         @Override
         public void processRequest(MessageResponse request) throws RequestHandlerException {}
 
         @Override
         public MessageResponse generateFailedResponse(MessageResponse request) {
             return null;
         }
     }
 }
