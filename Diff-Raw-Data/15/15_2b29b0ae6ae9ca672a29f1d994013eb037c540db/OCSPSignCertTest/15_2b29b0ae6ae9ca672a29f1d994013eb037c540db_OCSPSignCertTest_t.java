 package com.gmail.at.zhuikov.aleksandr.driveddoc.service;
 
 import static java.math.BigInteger.ZERO;
 import static org.junit.Assert.assertNotEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.InputStream;
 import java.security.Security;
 import java.security.cert.X509Certificate;
 
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.gmail.at.zhuikov.aleksandr.driveddoc.model.DigidocOCSPSignatureContainer;
 import com.gmail.at.zhuikov.aleksandr.driveddoc.model.SignatureContainerDescription;
 import com.google.appengine.api.blobstore.BlobKey;
 
 public class OCSPSignCertTest {
 	
	private InputStream inputStream = OCSPSignCertTest.class.getResourceAsStream("/504950.p12d");
 	private String password = "13hYdz8W";
 	private DigidocOCSPSignatureContainer ocspSignCert = new DigidocOCSPSignatureContainer(
 			inputStream,
 			new SignatureContainerDescription(new BlobKey(""), password, "user"));
 
 	@Before
 	public void initSecurityProvider() {
 		Security.addProvider(new BouncyCastleProvider());
 	}
 	
 	@Test
	public void precondition() {
		assertNotNull(inputStream);
	}
	
	@Test
 	public void getSignCert() throws Exception {
 		X509Certificate signCert = ocspSignCert.getSignCert();
 		assertNotNull(signCert);
 		assertNotEquals(ZERO, signCert.getSerialNumber());
 	}
 	
 	@Test
 	public void getSignKey() throws Exception {
 		assertNotNull(ocspSignCert.getSignKey());
 	}
 
 }
