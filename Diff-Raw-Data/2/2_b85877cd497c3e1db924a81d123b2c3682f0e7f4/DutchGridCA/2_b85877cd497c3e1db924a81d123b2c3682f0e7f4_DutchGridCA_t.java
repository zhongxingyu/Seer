 package nl.nikhef.jgridstart.ca;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import java.security.GeneralSecurityException;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.cert.CertStore;
 import java.security.cert.CollectionCertStoreParameters;
 import java.security.cert.X509Certificate;
 import java.security.interfaces.RSAPublicKey;
 
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import nl.nikhef.jgridstart.util.ConnectionUtils;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.WordUtils;
 import org.bouncycastle.cms.CMSException;
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.mail.smime.SMIMEException;
 import org.bouncycastle.mail.smime.SMIMESignedGenerator;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PEMWriter;
 import org.bouncycastle.util.Strings;
 import org.bouncycastle.util.encoders.Base64;
 import org.bouncycastle.x509.NoSuchStoreException;
 import org.bouncycastle.x509.X509CollectionStoreParameters;
 import org.bouncycastle.x509.X509Store;
 
 import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack;
 
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
 
     /** Create new DutchGridCA 
      * 
      * @throws NoSuchAlgorithmException 
      * @throws KeyManagementException */
     public DutchGridCA() throws NoSuchAlgorithmException, KeyManagementException {
 	if (baseSubmit==null)
 	    baseSubmit = "http://ra.dutchgrid.nl/ra/public/submit";
 	if (baseQuery==null)
 	    baseQuery = "http://ca.dutchgrid.nl/medium/query/";
 	if (baseCaCert==null)
 	    baseCaCert = "https://ca.dutchgrid.nl/cgi-bin/nikhef-ms?certder";
     }
     
     /** Just returns the PEM encoded version of the request. */
     public String encodeCertificationRequest(
 	    PKCS10CertificationRequest req, Properties info) throws IOException {
 	StringWriter out = new StringWriter();
 	PEMWriter writer = new PEMWriter(out);
 	writer.writeObject(req);
 	writer.close();
 	return out.toString();
     }
     
     /** PEM-encodes request and puts it in a S/MIME signed form.
      * <p>
      * Sets the property {@code renewal} to {@code true}.
      */
     public String signCertificationRequest(
 	    PKCS10CertificationRequest req, Properties info,
 	    PrivateKey oldKey, X509Certificate oldCert) throws IOException {
 	
 	String reqstring = encodeCertificationRequest(req, info);
 	
 	try {
 	    // create S/MIME message from it
 	    MimeBodyPart data = new MimeBodyPart();
 	    data.setText(reqstring);
 	    // add signature
 	    SMIMESignedGenerator gen = new SMIMESignedGenerator();
 	    gen.addSigner(oldKey, oldCert, SMIMESignedGenerator.DIGEST_SHA1);
 	    CertStore certStore = CertStore.getInstance("Collection",
 		    new CollectionCertStoreParameters(Arrays.asList(oldCert)), "BC");
 	    gen.addCertificatesAndCRLs(certStore);
 	    MimeMultipart multipart = gen.generate(data, "BC");
 
 	    MimeMessage msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
 	    msg.setContent(multipart, multipart.getContentType());
 	    msg.saveChanges();
 	    
 	    ByteArrayOutputStream out = new ByteArrayOutputStream();
 	    msg.writeTo(out);
 	    out.close();
 
 	    info.setProperty("renewal", Boolean.toString(true));
 	    
	    return out.toString().replace("Content-Type: application/pkcs7-signature", "Content-Type: application/x-pkcs7-signature");
 	    
 	} catch(MessagingException e) {
 	    throw new IOException(e.getMessage());
 	} catch (GeneralSecurityException e) {
 	    throw new IOException(e.getMessage());
 	} catch (SMIMEException e) {
 	    throw new IOException(e.getMessage());
 	}
     }
 
     /** Uploads a user certificate signing request onto the DutchGrid CA.
      * <p>
      * The field {@code email} is used to supply the user's email address to the CA
      * for notifying the user when the request is processed, and the field {@code agreecps}
      * must be {@code true} to succeed; the latter corresponds to the "I agree to the
      * privacy policy" checkbox on the website.
      * 
      * @param req {@inheritDoc}
      * @param info {@inheritDoc}; {@code email}, {@code fullname} and {@code agreecps} are used here.
      */
     public void uploadCertificationRequest(String req, Properties info) throws IOException {
 	
 	// renewal needs to be packed in a base64 encoded renewal blob
 	String sendreq = req;
 	if (Boolean.valueOf(info.getProperty("renewal"))) {
 	    sendreq =
 		"===== BEGIN DUTCHGRID RENEWAL BLOB =====\n" +
 		WordUtils.wrap(new String(Base64.encode(req.getBytes())), 64, "\n", true) + "\n" +
 		"===== END DUTCHGRID RENEWAL BLOB =====\n";
 		logger.finest("Submitting DutchGrid renewal blob:\n" + sendreq);
 	} else {
 	    logger.finest("Submitting request:\n" + req);
 	}
 	
 	String[] postdata = {
 		"action", "submit",
 		"fullname", info.getProperty("fullname"),
 		"email_1", info.getProperty("email"),
 		"email_2", info.getProperty("email"),
 		"requesttext", sendreq,
 		"Publish", "Upload Publishing Data",
 		"robot", "yes",
 		"dummy", "dummy"
 	};
 	if (Boolean.valueOf(info.getProperty("agreecps"))) {
 	    postdata[postdata.length-2] = "confirm-submit";
 	    postdata[postdata.length-1] = "ja";
 	}
 
 	URL url = new URL(baseSubmit);
 	String answer = ConnectionUtils.pageContents(url, postdata, true);
 
 	// returns "200 ..." on success
 	// TODO CAException instead of IOException
 	if (!answer.startsWith("200")) {
 	    logger.warning("Certificate signing request upload failed:" +answer);
 	    throw new IOException("Certificate signing request upload failed:\n"+answer);
 	}
 
 	logger.info("Uploaded certificate signing request");
     }
     
     public boolean isCertificationRequestProcessed(
 	    PKCS10CertificationRequest req, Properties info) throws IOException {
 	return downloadCertificate(req, info) != null;
     }
 
     /** Download a certificate from the DutchGrid CA
      * 
      * @param req {@inheritDoc}
      * @param info {@inheritDoc} (not used by DutchGridCA)
      * @return {@inheritDoc}
      */
     public X509Certificate downloadCertificate(
 	    PKCS10CertificationRequest req, Properties info) throws IOException {
 	
 	// return certificate by serial
 	URL url = new URL(baseQuery);
 	String[] pre;
 	try {
 	    pre = new String[] {
 	    	"id", ((RSAPublicKey)req.getPublicKey()).getModulus().toString(16),
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
      * CA certificate is downloaded once each program run.
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
