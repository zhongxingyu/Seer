 package com.gmail.at.zhuikov.aleksandr.driveddoc.service;
 
 import static org.junit.Assert.*;
 
 import java.io.InputStream;
 import java.security.Security;
 
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.junit.Before;
 import org.junit.Test;
 
 public class OCSPSignatureContainerServiceTest {
 
	InputStream container = OCSPSignCertTest.class.getResourceAsStream("504950.p12d");
 	SignatureContainerService service = new SignatureContainerService();
 	
 	@Before
 	public void initSecurityProvider() {
 		Security.addProvider(new BouncyCastleProvider());
 	}
 	
 	@Test
 	public void invalidPassword() {
 		assertFalse(service.isValid(container, "wrong"));
 	}
 
 	@Test
 	public void correctPassword() {
 		assertTrue(service.isValid(container, "13hYdz8W"));
 	}
 }
