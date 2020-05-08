 package utils;
 
 import sun.security.x509.*;
 import java.security.cert.*;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.SecureRandom;
 import java.security.GeneralSecurityException;
 
 import java.math.BigInteger;
 import java.util.Date;
 import java.io.IOException;
 import java.io.FileInputStream;
 
 
 public class KeyStoreUtils {
 
 	public KeyStore loadHubKeyStore( String ksFilename ) throws GeneralSecurityException, IOException {
 		KeyStore ks = KeyStore.getInstance("JKS");
 		FileInputStream input = new FileInputStream(ksFilename);
 		ks.load(input, "hubadmin".toCharArray());
 		input.close();
 		return ks;
 	}
 
 	public KeyStore loadHubTrustStore( String tsFilename ) throws GeneralSecurityException, IOException {
 		KeyStore ts = KeyStore.getInstance("JKS");
 		FileInputStream input = new FileInputStream(tsFilename);
 		ts.load(input, "hubadmin".toCharArray());
 		input.close();
 		return ts;
 	}
 
 	public KeyStore genUserKeyStore( String username, String password ) throws GeneralSecurityException, IOException {
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance(Constants.KEYGEN_ALGORITHM);
 		SecureRandom sRand = SecureRandom.getInstance(Constants.RANDOM_ALGORITHM);
 		sRand.setSeed(password.getBytes());
 		kpg.initialize(1024, sRand);
 		KeyPair keys = kpg.generateKeyPair();
 
 		String distinguishedName = username + " O=P2PChineseCheckers";
 		X509Certificate cert = generateCertificate(distinguishedName, keys, 10);
 
 		KeyStore ks = KeyStore.getInstance("JKS");
 		ks.load(null, null);
 		ks.setKeyEntry(username, keys.getPrivate(), password.toCharArray(), new Certificate[]{cert});
 		return ks;
 	}
 	
 	public KeyStore genUserTrustStore( String tsFilename ) throws GeneralSecurityException, IOException {
 		KeyStore ts = KeyStore.getInstance("JKS");
 		FileInputStream input = new FileInputStream(tsFilename);
		ts.load(input, "public".toCharArray());
 		input.close();
 		return ts;
 	}
 
 	/** 
 	 * Create a self-signed X.509 Certificate
 	 * @param dn the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
 	 * @param pair the KeyPair
 	 * @param days how many days from now the Certificate is valid for
 	 * @author STACKOVERFLOW
 	 */ 
 	private X509Certificate generateCertificate(String dn, KeyPair pair, int days) throws GeneralSecurityException, IOException {
 		String algorithm = Constants.SIGN_ALGORITHM;
 		PrivateKey privkey = pair.getPrivate();
 		X509CertInfo info = new X509CertInfo();
 		Date from = new Date();
 		Date to = new Date(from.getTime() + days * 86400000l);
 		CertificateValidity interval = new CertificateValidity(from, to);
 		BigInteger sn = new BigInteger(64, new SecureRandom());
 		X500Name owner = new X500Name(dn);
 
 		info.set(X509CertInfo.VALIDITY, interval);
 		info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
 		info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
 		info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
 		info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
 		info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
 		AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
 		info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
 
 		// Sign the certificate to identify the algorithm that's used.
 		X509CertImpl cert = new X509CertImpl(info);
 		cert.sign(privkey, algorithm);
 
 		// Update the algorithm, and resign.
 		algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
 		info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
 		cert = new X509CertImpl(info);
 		cert.sign(privkey, algorithm);
 		return cert;
 	}   
 
 }
