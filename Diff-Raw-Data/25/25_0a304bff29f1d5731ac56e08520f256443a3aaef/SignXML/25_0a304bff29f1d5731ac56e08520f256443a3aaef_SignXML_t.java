 package test.prioritytech.registry;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.net.UnknownHostException;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.util.Date;
 
 import org.apache.xml.security.utils.XMLUtils;
 import org.nchelp.meteor.security.Signature;
 import org.nchelp.meteor.security.saml.Assertion;
 import org.nchelp.meteor.security.saml.AssertionFactory;
 import org.nchelp.meteor.security.saml.SAMLUtil;
 import org.nchelp.meteor.util.exception.SignatureException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 
 
 public class SignXML {
 
 	private static String workingDir = "workspace/LDAPTesting/src";
 	
 	public static void main(String[] args){
 		String userid = "ED.303";
 		File f = new File(workingDir + "/signed.xml");
 				
 		String hostAddress = null;
 		String hostName = null;
 		
 		try {
 			hostAddress = java.net.InetAddress.getLocalHost().getHostAddress();
 			hostName = java.net.InetAddress.getLocalHost().getHostName();
 		} catch(UnknownHostException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		Assertion aa = AssertionFactory.newInstance(
 			new String( new Long(System.currentTimeMillis()).toString() ),
 			"nchelp.org/meteor",
 			new Date(),
 			userid,
 			"nchelp.org/meteor",
 			"http://nchelp.org",
 			new Date(),
 			hostAddress,
 			hostName
 			);
 	
 		aa.setAttributeValue("Role", "nchelp.org/meteor", "FAA");
 			
 		
 		Document doc = SAMLUtil.newDocument();
 		Element elem = doc.createElement("AssertionSpecifier");
 		doc.appendChild(elem);
 		
 		aa.serialize(elem);
 		
 	    String keystoreType = "JKS";
 	    String keystoreFile = workingDir + "/meteor.jks";
 	    String keystorePass = "secret";
 		String privateKeyAlias = "meteor";
 		String privateKeyPass = "secret";
 		String certificateAlias = "meteor";
 //	    String keystoreFile = "C:/Temp/XML/dsig/keystore.jks";
 //	    String keystorePass = "xmlsecurity";
 //		String privateKeyAlias = "test";
 //		String privateKeyPass = "xmlsecurity";
 //		String certificateAlias = "test";
 	    
 	    PrivateKey privateKey = null;
 		try {
 			KeyStore ks = KeyStore.getInstance(keystoreType);
 			FileInputStream fis = new FileInputStream(keystoreFile);
 			
 			ks.load(fis, keystorePass.toCharArray());
 			
 			privateKey = (PrivateKey) ks.getKey(privateKeyAlias,
 			                                             privateKeyPass.toCharArray());
 	         //X509Certificate cert =
 	         //   (X509Certificate) ks.getCertificate(certificateAlias);
 	
 	         //dsig.addKeyInfo(cert);
 	         //dsig.addKeyInfo(cert.getPublicKey());
 		    
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 
 
 		try {
 			Signature.sign(doc, privateKey);
 			System.out.println("Done signing document at: " + new Date());
 		} catch(SignatureException e) {
 			e.printStackTrace();
 		}
 
 		//System.out.println("Signed Document: \n" + XMLParser.XMLToString(doc));
 		
 		try {
 			//OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f));
 			
 			
 			//writeXML(doc, writer);
 			FileOutputStream fos = new FileOutputStream(f);
 			XMLUtils.outputDOMc14nWithComments(doc, fos);
 			
 		} catch(FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 
 		
 	}
 	
 }
