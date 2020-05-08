 package com.tresys.jalop.producer;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertSame;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 
 import org.junit.Test;
 
 import com.tresys.jalop.common.JALUtils.DMType;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerType;
 
 public class TestJALProducer {
 
 	@Test
 	public void testJALProducerDefaultConstructorWorks() {
 		JALProducer prod = new JALProducer();
 		assertNotNull(prod);
 	}
 	
 	@Test
 	public void testJALProducerAppMetadataXMLConstructorWorks() throws Exception {
 		LoggerType logger = new LoggerType();
 		LoggerXML xml = new LoggerXML(logger);
 		JALProducer prod = new JALProducer(xml);
 		assertNotNull(prod);
 		assertSame(xml, prod.getXml());
 	}
 
 	@Test
 	public void testJALProducerAppMetaXMLAllParamsConstructorWorks() throws Exception {
 		LoggerType logger = new LoggerType();
 		LoggerXML xml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		CertificateFactory cf = CertificateFactory.getInstance("X.509");
 		InputStream in = new FileInputStream("test-input/cert");
 		X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
 		in.close();
 		JALProducer prod = new JALProducer(xml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), cert, DMType.SHA256, "/path/to/socket");
 		assertNotNull(prod);
 		assertSame(xml, prod.getXml());
 		assertEquals("hostname", prod.getHostName());
 		assertEquals("app_name", prod.getApplicationName());
 		assertSame(kp.getPrivate(), prod.getPrivateKey());
 		assertSame(kp.getPublic(), prod.getPublicKey());
 		assertSame(cert, prod.getCertificate());
 		assertSame(DMType.SHA256, prod.getDigestMethod());
 		assertEquals("/path/to/socket", prod.getSocketFile());
 	}
 
 	@Test
 	public void testSetGetXml() throws Exception {
 		LoggerType logger = new LoggerType();
 		LoggerXML xml = new LoggerXML(logger);
 		JALProducer prod = new JALProducer();
 		prod.setXml(xml);
 		assertSame(xml, prod.getXml());
 	}
 
 	@Test
 	public void testSetGetHostName() {
 		JALProducer prod = new JALProducer();
 		prod.setHostName("hostname");
 		assertEquals("hostname", prod.getHostName());
 	}
 
 	@Test
 	public void testSetGetApplicationName() {
 		JALProducer prod = new JALProducer();
 		prod.setApplicationName("app_name");
 		assertEquals("app_name", prod.getApplicationName());
 	}
 
 	@Test
 	public void testSetGetPrivateKeyAndPublicKey() throws Exception {
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer();
 		prod.setPrivateKey(kp.getPrivate());
 		prod.setPublicKey(kp.getPublic());
 		assertSame(kp.getPrivate(), prod.getPrivateKey());
 		assertSame(kp.getPublic(), prod.getPublicKey());
 	}
 
 	@Test
 	public void testSetGetCertificate() throws Exception {
 		CertificateFactory cf = CertificateFactory.getInstance("X.509");
 		InputStream in = new FileInputStream("test-input/cert");
 		X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
 		in.close();
 		JALProducer prod = new JALProducer();
 		prod.setCertificate(cert);
 		assertSame(cert, prod.getCertificate());
 	}
 
 	@Test
 	public void testSetGetDigestMethod() {
 		JALProducer prod = new JALProducer();
 		prod.setDigestMethod(DMType.SHA256);
 		assertSame(DMType.SHA256, prod.getDigestMethod());
 	}
 
 	@Test
 	public void testSetGetSocketFile() {
 		JALProducer prod = new JALProducer();
 		prod.setSocketFile("/path/to/socket");
 		assertEquals("/path/to/socket", prod.getSocketFile());
 	}
 
 }
