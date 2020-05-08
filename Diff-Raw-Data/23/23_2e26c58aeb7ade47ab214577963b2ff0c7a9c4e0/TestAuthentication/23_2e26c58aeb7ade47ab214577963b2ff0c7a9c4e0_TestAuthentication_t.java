 /**
  * TestSecureMessaging.java
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright (C) Wouter Lueks, Radboud University Nijmegen, September 2012.
  * Copyright (C) Pim Vullers, Radboud University Nijmegen, September 2012.
  */
 
 package org.irmacard.credentials.idemix.test;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.PrivateKey;
 import java.security.SecureRandom;
 import java.security.Security;
 import java.security.Signature;
 import java.security.SignatureException;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateException;
 import java.security.interfaces.RSAPublicKey;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.smartcardio.CardException;
 
 import net.sourceforge.scuba.smartcards.CardService;
 import net.sourceforge.scuba.smartcards.CardServiceException;
 import net.sourceforge.scuba.util.Hex;
 
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.ejbca.cvc.AlgorithmUtil;
 import org.ejbca.cvc.CAReferenceField;
 import org.ejbca.cvc.CVCPublicKey;
 import org.ejbca.cvc.CardVerifiableCertificate;
 import org.ejbca.cvc.HolderReferenceField;
 import org.ejbca.cvc.KeyFactory;
 import org.ejbca.cvc.exception.ConstructionException;
 import org.ejbca.cvc.util.BCECUtil;
 import org.irmacard.credentials.cert.IRMACertificate;
 import org.irmacard.credentials.cert.IRMACertificateBody;
 import org.irmacard.credentials.idemix.util.CredentialInformation;
 import org.irmacard.idemix.IdemixService;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class TestAuthentication {
     static {
         Security.addProvider(new BouncyCastleProvider());
     }
     
     private RSAPublicKey caKey;
     
 	@BeforeClass
 	public static void initializeInformation() {
 		CredentialInformation.setCoreLocation(new File(System
 				.getProperty("user.dir")).toURI()
 				.resolve("irma_configuration/"));
 		java.security.Security.addProvider(new com.sun.crypto.provider.SunJCE());
 	}
 	
 	@Test
 	public void testCertificateVerification() throws CardException, CardServiceException, NoSuchAlgorithmException, NoSuchProviderException, ConstructionException, InvalidKeyException, SignatureException, IOException, CertificateException {
 		Certificate cert = constructCertificate();
 		System.out.println("Cert (" + cert.getEncoded().length  + "): " + Hex.toHexString(cert.getEncoded()));
 		cert.verify(caKey);
 		CardService terminal = TestSetup.getCardService();
 		IdemixService idemix = new IdemixService(terminal);
 		idemix.open();
		//idemix.setCAKey(caKey);
		//idemix.verifyCertificate(cert);
 	}
 	
 	private Certificate constructCertificate() throws NoSuchAlgorithmException, NoSuchProviderException, ConstructionException, InvalidKeyException, SignatureException, IOException {
         final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
         keyGen.initialize(1024, new SecureRandom());
         final KeyPair keyPair = keyGen.generateKeyPair();
         final KeyPair keyPair2 = keyGen.generateKeyPair();
         PrivateKey signerKey = keyPair2.getPrivate();
         caKey = (RSAPublicKey) keyPair2.getPublic();
         String algorithmName = "SHA1WITHRSAANDMGF1";
 
         CVCPublicKey cvcPublicKey = KeyFactory.createInstance(keyPair.getPublic(), algorithmName, null);
 
         final CAReferenceField caRef = new CAReferenceField("SE","PASS-CVCA","00111");
         final HolderReferenceField holderRef = new HolderReferenceField(caRef.getCountry(), caRef.getMnemonic(), caRef.getSequence());
         Calendar cal1 = Calendar.getInstance();
         Date validFrom = cal1.getTime();
         
         Calendar cal2 = Calendar.getInstance();
         cal2.add(Calendar.MONTH, 3);
         Date validTo = cal2.getTime();
 
         // Create the CVCertificateBody
         IRMACertificateBody body = new IRMACertificateBody(
               caRef, 
               cvcPublicKey,
               holderRef,
               validFrom,
               validTo );
 
         IRMACertificate cvc = new IRMACertificate(body);
         
         // Perform signing
         Signature signature = Signature.getInstance(AlgorithmUtil.convertAlgorithmNameToCVC(algorithmName), "BC");
         signature.initSign(signerKey);
         System.out.println("TBS (" + cvc.getTBS().length + "): " + Hex.toHexString(cvc.getTBS()));
         signature.update(cvc.getTBS());        
         byte[] signdata = signature.sign();
         
         // Now convert the X9.62 signature to a CVC signature
         byte[] sig = BCECUtil.convertX962SigToCVC(algorithmName, signdata);
         // Save the signature and return the certificate
         cvc.setSignature(sig);
 
 		return new CardVerifiableCertificate(cvc);
 	}
 }
