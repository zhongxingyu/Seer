 package nl.nikhef.jgridstart.ca;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import java.security.InvalidKeyException;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.cert.X509Certificate;
 import java.security.interfaces.RSAPublicKey;
 
 import nl.nikhef.jgridstart.util.ConnectionUtils;
 
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.jce.X509Principal;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PEMWriter;
 
 /**
  * This class is used interface with the DutchGrid production CA
  * 
  * @author wvengen
  */
 public class DutchGridCA implements CA {
     
     static final protected Logger logger = Logger.getLogger(DutchGridCA.class.getName());
     
     /** CA entry point: submission */
     protected String baseSubmit = System.getProperty("jgridstart.ca.base.submit");
     /** CA entry point: query */
     protected String baseQuery = System.getProperty("jgridstart.ca.base.query");
     /** CA entry point: CA cert */
     protected String baseCaCert = System.getProperty("jgridstart.ca.base.cacert");
     
     /** CA certificate (cached) */
     protected static X509Certificate cacert = null;
 
     /** Create new NikhefCA plugin 
      * 
      * @throws NoSuchAlgorithmException 
      * @throws KeyManagementException */
     public DutchGridCA() throws NoSuchAlgorithmException, KeyManagementException {
 	if (baseSubmit==null)
 	    baseSubmit = "http://ra.dutchgrid.nl/ra/public/submit";
 	if (baseQuery==null)
 	    baseQuery = "http://ca.dutchgrid.nl/medium/query/";
 	if (baseCaCert==null)
 	    baseCaCert = "https://ca.dutchgrid.nl/medium/cacert.pem";
     }
 
     /** Uploads a user certificate signing request onto the DutchGrid CA
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
 		"email_1", info.getProperty("email"),
 		"email_2", info.getProperty("email"),
 		"requesttext", reqWriter.toString(),
 		"Public", "Upload Publishing Data",
 		"robot", "true"
 	};
 
 	URL url = new URL(baseSubmit);
 	String answer = ConnectionUtils.pageContents(url, postdata, true);
 
 	String serial = null;
 
 	// TODO finish, what to expect?
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
 
 	logger.info("Uploaded certificate signing request");
 
 	return serial;
     }
     
     public boolean isCertificationRequestProcessed(
 	    PKCS10CertificationRequest req, String reqserial) throws IOException {
 	return downloadCertificate(req, reqserial) != null;
     }
 
     /** Download a certificate from the DutchGrid CA
      * 
      * @param req {@inheritDoc}
      * @param reqserial {@inheritDoc} (not used by DutchGridCA)
      * @return {@inheritDoc}
      */
     public X509Certificate downloadCertificate(
 	    PKCS10CertificationRequest req, String reqserial) throws IOException {
 	
 	// return certificate by serial
 	URL url = new URL(baseQuery);
 	String[] pre;
 	try {
 	    pre = new String[] {
	    	"id", ((RSAPublicKey)req.getPublicKey()).getModulus().toString(16).substring(0,20),
 	    	"fmt", "single"
 	    };
 	} catch (Exception e) {
 	    throw new IOException(e.getLocalizedMessage());
 	}
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
 	    String scert = ConnectionUtils.pageContents(new URL(baseCaCert));
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
