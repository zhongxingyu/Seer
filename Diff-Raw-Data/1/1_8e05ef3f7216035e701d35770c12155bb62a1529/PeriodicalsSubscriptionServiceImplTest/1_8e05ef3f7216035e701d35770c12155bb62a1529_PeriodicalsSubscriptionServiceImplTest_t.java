 // 
 // Copyright (c) 2003-2005, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
 // All rights reserved. 
 //   
 // Redistribution and use in source and binary forms, with or without modification,  
 // are permitted provided that the following conditions are met: 
 //   
 // * Redistributions of source code must retain the above copyright notice,  
 // this list of conditions and the following disclaimer. 
 // * Redistributions in binary form must reproduce the above copyright notice,  
 // this list of conditions and the following disclaimer in the documentation  
 // and/or other materials provided with the distribution. 
 // * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
 // nor the names of its contributors may be used to endorse or promote products  
 // derived from this software without specific prior written permission. 
 // 
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
 // AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 // IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 // OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
 // POSSIBILITY OF SUCH DAMAGE. 
 //
 
 package net.cyklotron.cms.periodicals.internal;
 
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.test.LedgeTestCase;
 
 import net.cyklotron.cms.confirmation.CryptographyService;
 import net.cyklotron.cms.confirmation.CryptographyServiceImpl;
 import net.cyklotron.cms.confirmation.EmailConfirmationRequestService;
 import net.cyklotron.cms.confirmation.EmailConfirmationRequestServiceImpl;
 import net.cyklotron.cms.periodicals.PeriodicalsSubscriptionService;
 import net.cyklotron.cms.periodicals.UnsubscriptionInfo;
 
 /**
  *
  *
  * @author <a href="rafal@caltha.pl">Rafał Krzewski</a>
  * @version $Id: PeriodicalsSubscriptionServiceImplTest.java,v 1.8 2007-11-18 21:23:33 rafal Exp $
  */
 public class PeriodicalsSubscriptionServiceImplTest
     extends LedgeTestCase
 {
     private FileSystem fileSystem;
     private PeriodicalsSubscriptionService service;
     private CryptographyService cipherCryptographyService;
     private EmailConfirmationRequestService confirmationRequestService;
     
     public void setUp() throws Exception
     {
         fileSystem = getFileSystem();
         initService();
     }
     
     private void initService() throws Exception
     {
         cipherCryptographyService = new CryptographyServiceImpl(fileSystem, "AES", 128, "SHA1", "12345");
         confirmationRequestService = new EmailConfirmationRequestServiceImpl(cipherCryptographyService);
         service = new PeriodicalsSubscriptionServiceImpl(cipherCryptographyService, confirmationRequestService);
     }
     
     public void testEncryption() throws Exception
     {
         int periodicalId = 799;
         String address = "rafal@caltha.pl";
         String enc = service.createUnsubscriptionToken(periodicalId, address);
         System.out.format("sample token: %s %d chars\n", enc, enc.length());
         UnsubscriptionInfo info = service.decodeUnsubscriptionToken(enc, true);
         assertEquals(periodicalId, info.getPeriodicalId());
         assertEquals(address, info.getAddress());
         assertTrue(info.isValid());
     }
     
     public void testKeyRegeneration() throws Exception
     {
         int periodicalId = 799;
         String address = "rafal@caltha.pl";
         String enc = service.createUnsubscriptionToken(periodicalId, address);
        cipherCryptographyService.createEncryptionKey();
         UnsubscriptionInfo info = service.decodeUnsubscriptionToken(enc, true);
         assertEquals(periodicalId, info.getPeriodicalId());
         assertEquals(address, info.getAddress());
         assertFalse(info.isValid());
     }
     
     public void testEncryptionPerformance() throws Exception
     {
         int periodicalId = 779;
         String address = "rafal@caltha.pl";
         long time = System.currentTimeMillis();
         int count = 1000;
         for(int i = 0; i<count; i++)
         {
             service.createUnsubscriptionToken(periodicalId, address);
         }
         time = System.currentTimeMillis() - time;
         System.out.format("generated %d tokens in %.2fs\n", count, (float) time / 1000);
     }
 }
