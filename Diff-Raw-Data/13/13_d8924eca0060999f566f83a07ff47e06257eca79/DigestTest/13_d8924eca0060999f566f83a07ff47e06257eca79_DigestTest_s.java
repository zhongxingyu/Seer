 /*
  * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.codeartisans.qipki.crypto.digest;
 
 import java.security.Security;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.codeartisans.qipki.crypto.QiCryptoActivator;
 import org.codeartisans.qipki.crypto.algorithms.DigestAlgorithm;
import org.codeartisans.qipki.crypto.codec.CryptCodex;
 import org.codeartisans.qipki.crypto.codec.CryptCodexImpl;
 import org.codeartisans.qipki.crypto.codec.CryptCodexService;
import org.codeartisans.qipki.crypto.io.CryptIOService;
 import org.junit.Ignore;
 import static org.junit.Assert.*;
 import org.junit.Test;
 import org.qi4j.bootstrap.AssemblyException;
 import org.qi4j.bootstrap.ModuleAssembly;
 import org.qi4j.test.AbstractQi4jTest;
 
 public class DigestTest
         extends AbstractQi4jTest
 {
 
     @Override
     public void assemble( ModuleAssembly module )
             throws AssemblyException
     {
         module.addServices( QiCryptoActivator.class ).instantiateOnStartup();
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
 
         Digest digester = new DigestImpl( new CryptCodexImpl() );
 
         runTest( digester );
 
         Security.removeProvider( BouncyCastleProvider.PROVIDER_NAME );
     }
 
     private void runTest( Digest digester )
     {
 
         String message = "Le nom des fous est écrit partout.\n";
 
         assertEquals( "c623709621030a5e84d703b7a160d33a",
                       digester.hexDigest( message.getBytes(), new DigestParameters( DigestAlgorithm.MD5 ) ) );
        assertEquals( "fddb1ae5b5d1ab7b8503d1b68ff84c6e507ecdf",
                       digester.hexDigest( message.getBytes(), new DigestParameters( DigestAlgorithm.SHA_1 ) ) );
         assertEquals( "2b9b4d8d02e5b9609ac1da0a7de879d0b6f41e845f0ba3f29ac3e15e7ee354ed",
                       digester.hexDigest( message.getBytes(), new DigestParameters( DigestAlgorithm.SHA_256 ) ) );
         assertEquals( "20910472dd3a886c563d9637a4e1c94858319f090e9a56f335457d902a3c01d88fc7cc5aad5c99ccb446479c08ae5824",
                       digester.hexDigest( message.getBytes(), new DigestParameters( DigestAlgorithm.SHA_384 ) ) );
         assertEquals( "cdbefb2d859f9e984a0032857469c3bba9fe03591772bbf5f1f866a4e8a31f9174e9a30e861d32ba4ff8e7c9f66bcdfed2df36f986e8388ce3cb775808de8bd4",
                       digester.hexDigest( message.getBytes(), new DigestParameters( DigestAlgorithm.SHA_512 ) ) );
 
     }
 
 }
