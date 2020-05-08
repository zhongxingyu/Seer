 package nl.nikhef.jgridstart.ca;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.X509Certificate;
 
 import nl.nikhef.jgridstart.util.ConnectionUtils;
 
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.jce.X509Principal;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PEMWriter;
 
 /**
  * This class is used to download a certificate from the Test CA
  * 
  * @author wvengen
  * 
  */
 public class TestCA implements CA {
     
     static final protected Logger logger = Logger.getLogger(TestCA.class.getName());
     
     /** Base URL of certificate authority */
     protected String base = System.getProperty("jgridstart.ca.base");
     /** CA certificate (cached) */
     protected static X509Certificate cacert = null;
 
     /** Create new NikhefCA plugin 
      * 
      * @throws NoSuchAlgorithmException 
      * @throws KeyManagementException */
     public TestCA() throws NoSuchAlgorithmException, KeyManagementException {
 	if (base==null)
	    base = "http://www.nikhef.nl/~wvengen/testca/";
     }
 
     /** Uploads a user certificate signing request onto the Test CA
      * 
      * @param req {@inheritDoc}
      * @param info {@inheritDoc}; only "email" is used here.
      * @return {@inheritDoc}
      */
     public String uploadCertificationRequest(
 	    PKCS10CertificationRequest req, Properties info) throws IOException {
 	
 	String name = req.getCertificationRequestInfo().getSubject().getValues(X509Principal.CN).get(0).toString();
 	
 	StringWriter reqWriter = new StringWriter();
 	PEMWriter w = new PEMWriter(reqWriter);
 	w.writeObject(req);
 	w.close();
 	
 	String[] postdata = {
 		"action", "submit",
 		"fullname", name,
 		"email", info.getProperty("email"),
 		"request", reqWriter.toString(),
 		"submit", "Submit request"
 	};
 
 	URL url = new URL(base);
 	String answer = ConnectionUtils.pageContents(url, postdata, true);
 
 	String serial = null;
 
 	// TODO dodgy but I don't know how else to do it.
 	try {
 	    String matchstr = "Saving request as";
 	    int index = answer.indexOf(matchstr);
 	    if (index == -1 || answer.indexOf("error:") != -1 || answer.indexOf("Error:") != -1) {
 		// means: not successful
 		logger.severe("Could not upload certification request.");
 		throw new /*UnableToUploadCertificationRequestException*/IOException(answer);
 	    }
 	    int index_end = answer.indexOf(".", index);
 	    serial = answer.substring(index+matchstr.length(), index_end).trim();
 	} catch (RuntimeException e) {
 	    logger.severe(e.getMessage());
 	    throw e;
 	}
 
 	logger.info("Uploaded certificate signing request with serial "+serial);
 
 	return serial;
     }
     
     public boolean isCertificationRequestProcessed(
 	    PKCS10CertificationRequest req, String reqserial) throws IOException {
 	return downloadCertificate(req, reqserial) != null;
     }
 
     /** Download a certificate from the Test CA
      * 
      * @param req {@inheritDoc} (not used by NikhefCA)
      * @param reqserial {@inheritDoc} 
      * @return {@inheritDoc}
      */
     public X509Certificate downloadCertificate(
 	    PKCS10CertificationRequest req, String reqserial) throws IOException {
 	
 	if (reqserial==null || reqserial.equals(""))
 	    throw new IOException("Cannot download certificate without request serial number");
 
 	// return certificate by serial
 	URL url = new URL(base);
 	String[] pre = new String[] {
 		"action", "retrieve_cert",
 		"serial", reqserial
 	};
 	String scert = ConnectionUtils.pageContents(url, pre, false);
 	StringReader reader = new StringReader(scert);
 	PEMReader r = new PEMReader(reader);
 	X509Certificate cert = (X509Certificate)r.readObject();
 	r.close();
 	if (cert==null)
 	    throw new IOException("Certificate could not be retrieved: "+scert);
 	return cert;
     }
     
     /** {@inheritDoc}
      * <p>
      * Test CA certificate is downloaded once each program run.
      */
     public X509Certificate getCACertificate() throws IOException {
 	if (cacert==null) {
 	    // download when not present
 	    String scert = ConnectionUtils.pageContents(new URL(base+"?action=retrieve_ca_cert"));
 	    StringReader reader = new StringReader(scert);
 	    PEMReader r = new PEMReader(reader);
 	    cacert = (X509Certificate)r.readObject();
 	    r.close();
 	    if (cacert==null)
 		throw new IOException("CA certificate could not be retrieved: "+scert);
 	}
 	return cacert;
     }
 }
