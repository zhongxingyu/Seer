 /*
  * New BSD license: http://opensource.org/licenses/bsd-license.php
  *
  * Copyright (c) 2010.
  * Henry Story
  * http://bblfish.net/
  *
  *  Redistribution and use in source and binary forms, with or without
  *  modification, are permitted provided that the following conditions are met:
  *
  *  - Redistributions of source code must retain the above copyright notice,
  *   this list of conditions and the following disclaimer.
  *  - Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
  *  - Neither the name of Sun Microsystems, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without specific prior written permission.
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *  POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.bblfish.dev.foafssl.xwiki.internal;
 
 import net.bblfish.dev.foafssl.xwiki.Certificate;
 import net.bblfish.dev.foafssl.xwiki.CertificateService;
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.jce.netscape.NetscapeCertRequest;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.util.encoders.Base64;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.logging.AbstractLogEnabled;
 import org.xwiki.component.phase.Initializable;
 import org.xwiki.component.phase.InitializationException;
 import org.xwiki.script.service.ScriptService;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.math.BigInteger;
 import java.net.URL;
 import java.security.*;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.Enumeration;
 
 import static net.bblfish.dev.foafssl.xwiki.internal.DefaultPubKey.create;
 
 /**
  * Component that can then be called by XWiki scripts, that can then call CertificateService.
  * <p/>
  * User: hjs
  * Date: Feb 17, 2010
  * Time: 3:46:22 PM
  */
 @Component("foafssl")
 public class CertificateScriptService extends AbstractLogEnabled implements ScriptService, CertificateService, Initializable {
     KeyStore keyStore;
     PrivateKey privateKey;
     X509Certificate certificate;
     SecureRandom numberGenerator;
 
     static {
          if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
              Security.addProvider(new BouncyCastleProvider());
          } else throw new Error("missing BouncyCastleProvider -- add jars to classpath");
      }    
 
     /**
      * partly taken from UUID class. Generates random numbers
      *
      * @return a UUID BigInteger
      */
     BigInteger nextRandom() {
         SecureRandom ng = numberGenerator;
         if (ng == null) {
             numberGenerator = ng = new SecureRandom();
         }
 
         byte[] randomBytes = new byte[16];
         ng.nextBytes(randomBytes);
         return new BigInteger(randomBytes).abs();
     }
 
     public void initialize() throws InitializationException {
         getLogger().info("initializing " + this.getClass().getCanonicalName());
         System.out.println("in " + this.getClass().getCanonicalName() + ".initialize()");
         URL certFile = CertificateScriptService.class.getResource("/cacert.p12");
        System.out.println("cert file=" + certFile);
         InputStream in;
         try {
             in = certFile.openStream();
         } catch (IOException e) {
             throw new InitializationException("could not load cert file " + certFile);
         }
         String alias = null;
 
         try {
             keyStore = KeyStore.getInstance("PKCS12");
         } catch (KeyStoreException e) {
             throw new InitializationException("could not get instance of PKCS12 keystore! SEVERE!", e);
         }
         try {
             keyStore.load(in, "testtest".toCharArray());  //the p12 keystore should really have no password! no need.
         } catch (CertificateException e) {
             throw new InitializationException("certificate extension found while loading store!", e);
         } catch (NoSuchAlgorithmException e) {
             throw new InitializationException("missing algorithm for reading store!", e);
         } catch (IOException e) {
             throw new InitializationException("Could not read keystore shipped with jar!", e);
         }
 
         // for some reason we don't have a fixed alias...
         // some tools produce aliases, others don't, so we search, though really we should have this fixed
         try {
             Enumeration<String> aliases = keyStore.aliases();
             while (aliases.hasMoreElements()) {
                 String tempAlias = aliases.nextElement();
                 if (keyStore.isKeyEntry(tempAlias)) {
                     alias = tempAlias;
                     break;
                 }
             }
         } catch (KeyStoreException e) {
             throw new InitializationException("could not find alias", e);
         }
 
 
         if (alias == null) {
             throw new InitializationException(
                     "Invalid keystore configuration: alias unspecified ");
         }
 
         try {
             privateKey = (PrivateKey) keyStore.getKey(alias, "testtest".toCharArray());
         } catch (KeyStoreException e) {
             throw new InitializationException("could not get key with alias " + alias, e);
         } catch (NoSuchAlgorithmException e) {
             throw new InitializationException("missing algorithm for reading store!", e);
         } catch (UnrecoverableKeyException e) {
             throw new InitializationException("could not recover private key in store", e);
         }
 
 
         try {
             certificate = (X509Certificate) keyStore.getCertificate(alias);
         } catch (KeyStoreException e) {
             throw new InitializationException("problem getting certificate with alias " + alias + "from keystore.", e);
         }
         getLogger().info("Initialization of " + this.getClass().getCanonicalName() + " successfull.");
        System.out.println("out " + this.getClass().getCanonicalName() + ".initialize()");
     }
 
     public Certificate createFromPEM(String pemCsr) {
         PEMReader pemReader = new PEMReader(new StringReader(pemCsr));
         Object pemObject;
         try {
             pemObject = pemReader.readObject();
             if (pemObject instanceof PKCS10CertificationRequest) {
                 PKCS10CertificationRequest pkcs10Obj = (PKCS10CertificationRequest) pemObject;
                 DefaultCertificate cert = new DefaultCertificate(this);
                 try {
                     cert.setSubjectPublicKey(create(pkcs10Obj.getPublicKey()));
                     return cert;
                 } catch (NoSuchAlgorithmException e) {
                     getLogger().error("Don't know algorithm required by certification request ", e);
                 } catch (NoSuchProviderException e) {
                     getLogger().error("Don't have provider for certification request ", e);
                 } catch (InvalidKeyException e) {
                     getLogger().warn("Invalid key sent in certificate request", e);
                 }
             }
         } catch (IOException e) {
             getLogger().error("How can this happen? Serious! An IOEXception on a StringReader?", e);
         }
         return null;
     }
 
     public Certificate createFromSpkac(String spkac) throws InvalidParameterException {
        System.out.println("in createFromSpkac");
        System.out.println("spkac=" + spkac);
         if (spkac == null) throw new InvalidParameterException("SPKAC parameter is null");
         try {
             NetscapeCertRequest certRequest = new NetscapeCertRequest(Base64.decode(spkac));
             DefaultCertificate cert = new DefaultCertificate(this);
             cert.setSubjectPublicKey(create(certRequest.getPublicKey()));
            System.out.println("leaving createFromSpkac, returning cert " + cert);
             return cert;
         } catch (IOException e) {
             getLogger().error("how can an IOError occur when reading a string?", e);
         }
        System.out.println("leaving createFromSpkac with null value");
         return null;
     }
 
 //     No I probably don't need to set this. Should leave it to the application code to create the object and set the fields.
 //     Otherwise perhaps this should be part of a publically available method, callable from the XWiki code.
 //     So that scripts can quickly set up the right pieces
 
 //        XWikiContext context = (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
 //        XWiki xwiki = context.getWiki();
 //        XWikiDocument doc;
 //        boolean needsUpdate = false;
 //
 //        try {
 //            doc = xwiki.getDocument(new DocumentReference("?", "XWiki", "RSAPublicKey"), context);
 //        } catch (Exception e) {
 //            doc = new XWikiDocument(new DocumentReference("?", "XWiki", "RSAPublicKey"));
 //            needsUpdate = true;
 //            getLogger().debug(this.class.getCanonicalName() + " initialized");
 //        }
 //
 //        BaseClass bclass = doc.getXClass();
 //        bclass.setName("XWiki.Certificate");
 //        needsUpdate |= bclass.addTextField("user", "User", 30);
 //        needsUpdate |= bclass.addDateField("startDate", "Start Date", "dd/MM/yyyy");
 //        needsUpdate |= bclass.addDateField("endDate", "End Date", "dd/MM/yyyy");
 //        needsUpdate |= bclass.addTextField("title", "Title", 30);
 //        needsUpdate |= bclass.addTextAreaField("modulus", "Modulus (hex)", 40, 5);
 //        needsUpdate |= bclass.addTextField("exponent", "Exponent (int)", 30);
 //
 //        if (needsUpdate) try {
 //            xwiki.saveDocument(doc, context);
 //        } catch (XWikiException e) {
 //            throw new InitializationException("Could not intialize RSAPublicKey class",e);
 //        }
 //       return bclass;
 
 }
