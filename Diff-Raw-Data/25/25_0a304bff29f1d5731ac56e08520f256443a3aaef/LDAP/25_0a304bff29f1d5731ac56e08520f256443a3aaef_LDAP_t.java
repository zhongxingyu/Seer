 /*
  * (c) Copyright 2001 MyCorporation.
  * All Rights Reserved.
  */
 package test.prioritytech.registry;
 
 import java.io.ByteArrayInputStream;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.Hashtable;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 
 /**
  * @version 	1.0
  * @author
  */
 public class LDAP {
 	public static void main(String args[]) {
 
 		Hashtable env = new Hashtable();
 		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 		env.put(Context.PROVIDER_URL, "ldap://localhost:389/cn=meteor");
 
 		try {
 			DirContext ctx = new InitialDirContext(env);
 
 			Attributes answer = ctx.getAttributes("encryptionname=ssl,o=ED.PTI");
 			NamingEnumeration ae = answer.getAll();
 			while (ae.hasMore()) {
 
 				Attribute attr = (Attribute) ae.next();
 				System.out.println(attr.getID());
 				NamingEnumeration e = attr.getAll();
 				while (e.hasMore()) {
 
 					System.out.println("value: " + e.next());
 				}
 			}
 
 			Attribute cert = answer.get("usercertificate");
 
 			if(cert == null){
 				System.out.println("Cert is null");
 				return;
 			}
 			// Get the correct certificate factory.
 			CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
 
 			// Each file specified on the command line must contain a single
 			// DER-encoded X.509 certificate.  The DER-encoded certificate
 			// can be in either binary or ASCII format.
 			Object o = cert.get();
 			System.out.println("cert is of type: " + o.getClass().getName());
 			
 			ByteArrayInputStream bio = new ByteArrayInputStream( o.toString().getBytes());
 			
 			X509Certificate x509certificate =
 				(X509Certificate) certificatefactory.generateCertificate(bio);
 
 			// First, let's print out information about the certificate itself.
 			System.out.println("---Certificate---");
 			System.out.println("type = " + x509certificate.getType());
 			System.out.println("version = " + x509certificate.getVersion());
 			System.out.println("subject = " + x509certificate.getSubjectDN().getName());
 			System.out.println("valid from = " + x509certificate.getNotBefore());
 			System.out.println("valid to = " + x509certificate.getNotAfter());
 			System.out.println(
 				"serial number = " + x509certificate.getSerialNumber().toString(16));
 			System.out.println("issuer = " + x509certificate.getIssuerDN().getName());
 			System.out.println("signing algorithm = " + x509certificate.getSigAlgName());
 			System.out.println(
 				"public key algorithm = " + x509certificate.getPublicKey().getAlgorithm());
 			// Next, let's print out information about the extensions.
 			System.out.println("---Extensions---");
 //			//Set setCritical = x509certificate.getCriticalExtensionOIDs();
 //			if (setCritical != null && setCritical.isEmpty() == false)
 //				for (Iterator iterator = setCritical.iterator(); iterator.hasNext();)
 //					System.out.println(iterator.next().toString() + " *critical*");
 //			//Set setNonCritical = x509certificate.getNonCriticalExtensionOIDs();
 //			if (setNonCritical != null && setNonCritical.isEmpty() == false)
 //				for (Iterator iterator = setNonCritical.iterator(); iterator.hasNext();)
 //					System.out.println(iterator.next().toString());
 //			// We're done.
 			System.out.println("---");
 
 		} catch (CertificateException e) {
 			e.printStackTrace();
 		} catch (NamingException e) {
 			e.printStackTrace();
 		}
 
 	}
 }
