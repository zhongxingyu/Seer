 package au.org.arcs.auth.slcs;
 
 import java.io.ByteArrayInputStream;
 import java.io.StringWriter;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.PrivateKey;
 import java.security.SecureRandom;
 import java.security.Security;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.Vector;
 
 import org.bouncycastle.asn1.DERObjectIdentifier;
 import org.bouncycastle.asn1.DEROctetString;
 import org.bouncycastle.asn1.DERSet;
 import org.bouncycastle.asn1.pkcs.Attribute;
 import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
 import org.bouncycastle.asn1.x509.CertificatePolicies;
 import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
 import org.bouncycastle.asn1.x509.GeneralName;
 import org.bouncycastle.asn1.x509.GeneralNames;
 import org.bouncycastle.asn1.x509.KeyPurposeId;
 import org.bouncycastle.asn1.x509.KeyUsage;
 import org.bouncycastle.asn1.x509.X509Extension;
 import org.bouncycastle.asn1.x509.X509Extensions;
 import org.bouncycastle.asn1.x509.X509Name;
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.openssl.PEMWriter;
 import org.python.core.PyDictionary;
 import org.python.core.PyInstance;
 import org.python.core.PyList;
 import org.python.core.PyObject;
 import org.python.core.PyString;
 import org.python.core.PyUnicode;
 import org.python.util.PythonInterpreter;
 
 import au.org.arcs.auth.shibboleth.ArcsSecurityProvider;
 import au.org.arcs.auth.shibboleth.DummyIdpObject;
 import au.org.arcs.auth.shibboleth.IdpObject;
 import au.org.arcs.auth.shibboleth.Shibboleth;
 import au.org.arcs.auth.shibboleth.StaticIdpObject;
 
 public class SLCS {
 	
 	public static final String DEFAULT_SLCS_URL = "https://slcs1.arcs.org.au/SLCS/login";
 
 	private PythonInterpreter interpreter = new PythonInterpreter();
 	private KeyPairGenerator kpGen = null;
 	
 	private PrivateKey privateKey = null;
 	private X509Certificate x509Cert = null;
 
 	private final Shibboleth shib;
 	
 	public SLCS() {
 		this(DEFAULT_SLCS_URL);
 	}
 	
 	public SLCS(String url) {
 
 		Security
 				.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
 		
 		java.security.Security.addProvider(new ArcsSecurityProvider());
 
 		java.security.Security.setProperty("ssl.TrustManagerFactory.algorithm",
 				"TrustAllCertificates");
 		
 		try {
 			kpGen = KeyPairGenerator.getInstance("RSA", "BC");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchProviderException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		kpGen.initialize(1024, new SecureRandom());
 		
 		shib = new Shibboleth(url);
 
 	}
 	
 	private String submitCertificateRequest(String pem) {
 		
 		interpreter.set("certreq", pem);
 		interpreter.exec("from urllib import urlencode");
 		interpreter.exec("import urllib2");
 		interpreter.exec("data = urlencode({'AuthorizationToken': token,'CertificateSigningRequest': certreq})");
 		interpreter.exec("certResp = urllib2.urlopen(reqURL, data)");
 
 		interpreter.exec("from arcs.gsi.slcs import parse_cert_response");
 		interpreter.exec("cert = parse_cert_response(certResp)");
 		
 		PyObject cert = interpreter.get("cert");
 		
 		return cert.asString();
 		
 	}
 
 	private String createCertificateRequest(PyInstance response) {
 
 		interpreter.exec("from arcs.gsi.slcs import parse_req_response");
 
 		interpreter.set("slcsResp", response);
 		interpreter
 				.exec("token, dn, reqURL, elements = parse_req_response(slcsResp)");
 
 		PyString token = (PyString)interpreter.get("token");
 		PyString dn = (PyString)interpreter.get("dn");
 		PyUnicode reqUrl = (PyUnicode)interpreter.get("reqURL");
 		PyList elObjects = (PyList)interpreter.get("elements");
 		
 		System.out.println("Token: "+token.asString());
 		System.out.println("Dn: "+dn.asString());
 		System.out.println("reqUrl: "+reqUrl.asString());
 		
 		PyDictionary[] elements = new PyDictionary[elObjects.size()];
 		
 		for ( int i=0; i<elements.length; i++ ) {
 			elements[i] = (PyDictionary)elObjects.get(i);
 		}
 
 		try {
 
 			KeyPair pair = null;
 			pair = kpGen.generateKeyPair();
 			
 			Vector<DERObjectIdentifier> oids = new Vector<DERObjectIdentifier>();
 			Vector<X509Extension> values = new Vector<X509Extension>();
 			
 			for ( PyDictionary dic : elements ) {
 				
 				String name = null;
 				String oid = null;
 				boolean critical = false;
 				String value = null;
 				
 				for ( Object keyO : dic.keys() ) {
 					String key = (String)keyO;
 					
 					if ( "oid".equals(key) ) {
 						oid = (String)dic.get(key);
 					} else if ( "name".equals(key) ) {
 						name = (String)dic.get(key);
 					} else if ( "critical".equals(key) ) {
 						critical = (Boolean)dic.get(key);
 					} else if ( "value".equals(key) ) {
 						value = (String)dic.get(key);
 					} else {
 						throw new RuntimeException("Can't match key: "+key);
 					}
 				}
 				
 				
 //				System.out.println("Set: ");
 //				System.out.println("\tname: "+name);
 //				System.out.println("\toid: "+oid);
 //				System.out.println("\tcritical: "+critical);
 //				System.out.println("\tvalue: "+value);
 				
 				if ( "SubjectAltName".equals(name) ) {
 					String email = value.substring(value.indexOf(":")+1);
 					GeneralNames subjectAltName = new GeneralNames(
 			                   new GeneralName(GeneralName.rfc822Name, email));
 					oids.add(X509Extensions.SubjectAlternativeName);
 					values.add(new X509Extension(critical, new DEROctetString(subjectAltName)));
 					
 				} else if ( "ExtendedKeyUsage".equals(name) ) {
 					
 					ExtendedKeyUsage extendedKeyUsage = null;
 					if ( "ClientAuth".equals(value) ) {
 						extendedKeyUsage = new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth);
 					} else {
 						throw new RuntimeException("ExtendedKeyUsage: "+value+" not implemented!");
 					}
 					X509Extension extension = new X509Extension(critical, new DEROctetString(extendedKeyUsage));
 
 					oids.add(X509Extensions.ExtendedKeyUsage);
 					values.add(extension);
 					
 				} else if ( "KeyUsage".equals(name) ) {
 					
 					KeyUsage keyUsage = null;
 					if ( "DigitalSignature,KeyEncipherment".equals(value) ) {
 						keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment);
 					} else {
 						throw new RuntimeException("KeyUsage: "+value+" not implemented!");
 					}
 					X509Extension extension = new X509Extension(critical, new DEROctetString(keyUsage));
 					oids.add(X509Extensions.KeyUsage);
 					values.add(extension);
 					
 				} else if ( "CertificatePolicies".equals(name) ) {
 					
 					CertificatePolicies certPolicies = null;
 					if ( "1.3.6.1.4.1.31863.1.0.1".equals(value) ) {
 						certPolicies = new CertificatePolicies(value);
 					} else {
 						throw new RuntimeException("CertificatePolicies: "+value+" not implemented!");
 					}
 					X509Extension extension = new X509Extension(critical, new DEROctetString(certPolicies));
 					oids.add(X509Extensions.CertificatePolicies);
 					values.add(extension);
 				}
 				
 			}
 
 			X509Extensions extensions = new X509Extensions(oids, values);
 
 			Attribute attribute = new Attribute(
 					PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
 					new DERSet(extensions));
 
 			PKCS10CertificationRequest certRequest = null;
 			certRequest = new PKCS10CertificationRequest("SHA256withRSA",
 					new X509Name(false, dn.asString()), pair
 							.getPublic(), new DERSet(attribute), pair
 							.getPrivate());
 			
 			//System.out.println(certRequest.getCertificationRequestInfo().getSubject().toString());
 			
 
 			StringWriter writer = new StringWriter();
 			PEMWriter pemWrt = new PEMWriter(writer);
 			pemWrt.writeObject(certRequest);
 			pemWrt.close();
 			
 			writer.close();
 			
 			// persist the private key 
 			privateKey = pair.getPrivate();
 			
 			return writer.toString();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException("Could not create certificate request.", e);
 		}
 	}
 
 	public void init(String username, char[] password, String idpName) {
 		init(username, password, new StaticIdpObject(idpName));
 	}
 	
 	public void init(String username, char[] password, IdpObject idp) {
 
 		PyInstance returnValue = shib.shibOpen(username, password, idp);
 
 		String pem = createCertificateRequest(returnValue);
 		
 		String cert = submitCertificateRequest(pem);
 		
 		try {
 			x509Cert = (X509Certificate)CertificateFactory.getInstance("X.509", "BC").generateCertificate( new ByteArrayInputStream( cert.getBytes() ));
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new RuntimeException("Could not create X509Certificate object.", e);
 		}
 
 	}
 	
 	public X509Certificate getCertificate() {
 		return x509Cert;
 	}
 	
 	public PrivateKey getPrivateKey() {
 		return privateKey;
 	}
 	
 	public static void main(String[] args) {
 		
 		String username = args[0];
 		String password = args[1];
 //		String idp = args[2];
 		String url = "https://slcs1.arcs.org.au/SLCS/login";
 		
 		SLCS slcs = new SLCS(url);
 		
		slcs.init(username, password.toCharArray(), new DummyIdpObject());
 		
 		
 		
 	}
 
 }
