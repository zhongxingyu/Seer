 package com.lucasian.crypt.signer;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.security.Security;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import org.bouncycastle.asn1.ASN1ObjectIdentifier;
 import org.bouncycastle.jce.X509Principal;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.junit.Test;
 
 public class BouncyTest {
 
	private String theKey = "/data/workspaces/just-cloud/signer/signer-bouncy/certs/paae780711fr3.cer";
	//private String theKey = "/data/workspaces/just-cloud/signer/signer-bouncy/certs/sasj640127pw7.cer";
 	
 	@Test
 	public void testGetData() throws Exception {
 		Security.addProvider(new BouncyCastleProvider());
 
		InputStream is = new FileInputStream(theKey);
 		CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
 
 		X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
 
 		is.close();
 
 		System.out.println(cert);
 
 		X509Principal principal = (X509Principal) cert.getSubjectDN();
 
 		Map<String, String> results = new HashMap<String, String>();
 
 		@SuppressWarnings("unchecked")
 		Vector<ASN1ObjectIdentifier> oids = principal.getOIDs();
 		@SuppressWarnings("unchecked")
 		Vector<String> value = principal.getValues();
 
 		for (int i = 0; i < oids.size(); i++) {
 			//2.5.4.41 is the name
 			//2.5.4.5 is the  CURP
 			//2.5.4.45 is the RFC
 			results.put(oids.get(i).getId(), value.get(i));
 		}
 
 		System.out.println(results);
 
 	}
 
 }
