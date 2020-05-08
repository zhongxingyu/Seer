 /*
  * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.qipki.crypto.digest;
 
 import java.security.Security;
 
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
 import org.qipki.crypto.QiCryptoEngine;
 import org.qipki.crypto.algorithms.DigestAlgorithm;
 import org.qipki.crypto.codec.CryptCodexImpl;
 import org.qipki.crypto.codec.CryptCodexService;
 
 import org.junit.Ignore;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 import org.qi4j.bootstrap.AssemblyException;
 import org.qi4j.bootstrap.ModuleAssembly;
 import org.qi4j.test.AbstractQi4jTest;
 import org.qipki.crypto.CryptoContext;
 
 public class DigestTest
         extends AbstractQi4jTest
 {
 
     @Override
     @SuppressWarnings( "unchecked" )
     public void assemble( ModuleAssembly module )
             throws AssemblyException
     {
         module.addServices( QiCryptoEngine.class ).instantiateOnStartup();
         module.addServices( DigestService.class, CryptCodexService.class );
     }
 
     @Test
     public void testWithQi4j()
     {
         Digest digester = serviceLocator.<Digest>findService( Digest.class ).get();
         runTest( digester );
     }
 
     @Test
     @Ignore
     public void testWithoutQi4j()
     {
         Security.addProvider( new BouncyCastleProvider() );
 
         CryptoContext cryptoContext = new CryptoContext()
         {
 
             @Override
             public String providerName()
             {
                 return BouncyCastleProvider.PROVIDER_NAME;
             }
 
         };
 
         Digest digester = new DigestImpl( cryptoContext, new CryptCodexImpl() );
 
         runTest( digester );
 
         Security.removeProvider( BouncyCastleProvider.PROVIDER_NAME );
     }
 
     private void runTest( Digest digester )
     {
 
         String message = "Le nom des fous est écrit partout.\n";
 
         assertEquals( "c623709621030a5e84d703b7a160d33a",
                      digester.hexDigest( message.getBytes(), new DigestParameters( DigestAlgorithm.MD5 ) ) );
         assertEquals( "0fddb1ae5b5d1ab7b8503d1b68ff84c6e507ecdf",
                      digester.hexDigest( message.getBytes(), new DigestParameters( DigestAlgorithm.SHA_1 ) ) );
         assertEquals( "2b9b4d8d02e5b9609ac1da0a7de879d0b6f41e845f0ba3f29ac3e15e7ee354ed",
                      digester.hexDigest( message.getBytes(), new DigestParameters( DigestAlgorithm.SHA_256 ) ) );
         assertEquals( "20910472dd3a886c563d9637a4e1c94858319f090e9a56f335457d902a3c01d88fc7cc5aad5c99ccb446479c08ae5824",
                      digester.hexDigest( message.getBytes(), new DigestParameters( DigestAlgorithm.SHA_384 ) ) );
         assertEquals( "cdbefb2d859f9e984a0032857469c3bba9fe03591772bbf5f1f866a4e8a31f9174e9a30e861d32ba4ff8e7c9f66bcdfed2df36f986e8388ce3cb775808de8bd4",
                      digester.hexDigest( message.getBytes(), new DigestParameters( DigestAlgorithm.SHA_512 ) ) );
 
     }
 
 }
