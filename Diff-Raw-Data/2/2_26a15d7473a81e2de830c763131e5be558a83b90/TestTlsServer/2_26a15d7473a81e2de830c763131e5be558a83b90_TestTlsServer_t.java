 /****************************************************************************
  * Copyright (C) 2013 HS Coburg.
  * All rights reserved.
  * Contact: ecsec GmbH (info@ecsec.de)
  *
  * This file is part of the Open eCard App.
  *
  * GNU General Public License Usage
  * This file may be used under the terms of the GNU General Public
  * License version 3.0 as published by the Free Software Foundation
  * and appearing in the file LICENSE.GPL included in the packaging of
  * this file. Please review the following information to ensure the
  * GNU General Public License version 3.0 requirements will be met:
  * http://www.gnu.org/copyleft/gpl.html.
  *
  * Other Usage
  * Alternatively, this file may be used in accordance with the terms
  * and conditions contained in a signed written agreement between
  * you and ecsec GmbH.
  *
  ***************************************************************************/
 
 package org.openecard.plugins.phrplugin.tls;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import org.openecard.bouncycastle.asn1.pkcs.RSAPrivateKey;
 import org.openecard.bouncycastle.crypto.params.AsymmetricKeyParameter;
 import org.openecard.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
 import org.openecard.bouncycastle.crypto.tls.AlertLevel;
 import org.openecard.bouncycastle.crypto.tls.Certificate;
 import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
 import org.openecard.bouncycastle.crypto.tls.ClientCertificateType;
 import org.openecard.bouncycastle.crypto.tls.DefaultTlsEncryptionCredentials;
 import org.openecard.bouncycastle.crypto.tls.DefaultTlsServer;
 import org.openecard.bouncycastle.crypto.tls.DefaultTlsSignerCredentials;
 import org.openecard.bouncycastle.crypto.tls.TlsContext;
 import org.openecard.bouncycastle.crypto.tls.TlsEncryptionCredentials;
 import org.openecard.bouncycastle.crypto.tls.TlsSignerCredentials;
 import org.openecard.bouncycastle.crypto.util.PrivateKeyFactory;
 import org.openecard.bouncycastle.util.io.pem.PemObject;
 import org.openecard.bouncycastle.util.io.pem.PemReader;
 import org.openecard.common.util.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * TLS Server for testing purposes. <br/>
  * By the use of this server it can be ensured that the TLS client is behaving properly, e.g. sends the correct
  * credentials.
  *
  * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
  */
 public class TestTlsServer extends DefaultTlsServer {
 
     private static final Logger logger = LoggerFactory.getLogger(TestTlsServer.class);
     private static final String X509_SERVER_KEY_PEM = "test-tls-server/x509-server-key.pem";
     private static final String X509_CA_PEM = "test-tls-server/x509-ca.pem";
     private static final String X509_SERVER_PEM = "test-tls-server/x509-server.pem";
 
     @Override
     public void notifyAlertRaised(short alertLevel, short alertDescription, String message, Exception cause) {
 	String msg = "TLS server raised alert (AlertLevel.{}, AlertDescription.{})";
 	if (alertLevel == AlertLevel.fatal) {
 	    logger.error(msg, alertLevel, alertDescription);
 	    logger.error(message, cause);
 	} else {
 	    logger.warn(msg, alertLevel, alertDescription);
 	    logger.warn(message, cause);
 	}
     }
 
     @Override
     public void notifyAlertReceived(short alertLevel, short alertDescription) {
 	String msg = "TLS server received alert (AlertLevel.{}, AlertDescription.{})";
 	if (alertLevel == AlertLevel.fatal) {
 	    logger.error(msg, alertLevel, alertDescription);
 	} else {
 	    logger.warn(msg, alertLevel, alertDescription);
 	}
     }
 
     @Override
     public void notifyClientCertificate(Certificate clientCertificate) throws IOException {
 	org.openecard.bouncycastle.asn1.x509.Certificate[] chain = clientCertificate.getCertificateList();
 	logger.debug("Received client certificate chain of length " + chain.length);
     }
 
     @Override
     public CertificateRequest getCertificateRequest() {
	return new CertificateRequest(new short[] { ClientCertificateType.rsa_sign }, null);
     }
 
     @Override
     protected TlsEncryptionCredentials getRSAEncryptionCredentials() throws IOException {
 	return loadEncryptionCredentials(context, new String[] { X509_SERVER_PEM, X509_CA_PEM },
 		X509_SERVER_KEY_PEM);
     }
 
     @Override
     protected TlsSignerCredentials getRSASignerCredentials() throws IOException {
 	return loadSignerCredentials(context, new String[] { X509_SERVER_PEM, X509_CA_PEM }, X509_SERVER_KEY_PEM);
     }
 
     TlsEncryptionCredentials loadEncryptionCredentials(TlsContext context, String[] certResources,
 	    String keyResource) throws IOException {
 	Certificate certificate = loadCertificateChain(certResources);
 	AsymmetricKeyParameter privateKey = loadPrivateKeyResource(keyResource);
 
 	return new DefaultTlsEncryptionCredentials(context, certificate, privateKey);
     }
 
     TlsSignerCredentials loadSignerCredentials(TlsContext context, String[] certResources, String keyResource)
 	throws IOException {
 	org.openecard.bouncycastle.crypto.tls.Certificate certificate = loadCertificateChain(certResources);
 	AsymmetricKeyParameter privateKey = loadPrivateKeyResource(keyResource);
 
 	return new DefaultTlsSignerCredentials(context, certificate, privateKey);
     }
 
     Certificate loadCertificateChain(String[] resources) throws IOException {
 	org.openecard.bouncycastle.asn1.x509.Certificate[] chain = 
 		new org.openecard.bouncycastle.asn1.x509.Certificate[resources.length];
 	for (int i = 0; i < resources.length; i++) {
 	    chain[i] = loadCertificateResource(resources[i]);
 	}
 	return new Certificate(chain);
     }
 
     org.openecard.bouncycastle.asn1.x509.Certificate loadCertificateResource(String resource) throws IOException {
 	PemObject pem = loadPemResource(resource);
 	if (pem.getType().endsWith("CERTIFICATE")) {
 	    return org.openecard.bouncycastle.asn1.x509.Certificate.getInstance(pem.getContent());
 	}
 	throw new IllegalArgumentException("'resource' doesn't specify a valid certificate");
     }
 
     PemObject loadPemResource(String resource) throws IOException {
 	InputStream s = FileUtils.resolveResourceAsStream(TestTlsServer.class, resource);
 	PemReader p = new PemReader(new InputStreamReader(s));
 	PemObject o = p.readPemObject();
 	p.close();
 	return o;
     }
 
     AsymmetricKeyParameter loadPrivateKeyResource(String resource) throws IOException {
 	PemObject pem = loadPemResource(resource);
 	if (pem.getType().endsWith("RSA PRIVATE KEY")) {
 	    RSAPrivateKey rsa = RSAPrivateKey.getInstance(pem.getContent());
 	    return new RSAPrivateCrtKeyParameters(rsa.getModulus(), rsa.getPublicExponent(), rsa.getPrivateExponent(),
 		    rsa.getPrime1(), rsa.getPrime2(), rsa.getExponent1(), rsa.getExponent2(), rsa.getCoefficient());
 	}
 	if (pem.getType().endsWith("PRIVATE KEY")) {
 	    return PrivateKeyFactory.createKey(pem.getContent());
 	}
 	throw new IllegalArgumentException("'resource' doesn't specify a valid private key");
     }
 
 }
