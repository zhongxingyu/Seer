 package com.lucasian.crypt.signer.bouncy;
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.KeyFactory;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.cert.CertificateExpiredException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.CertificateNotYetValidException;
 import java.security.cert.X509Certificate;
 import java.security.spec.PKCS8EncodedKeySpec;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import org.bouncycastle.asn1.ASN1InputStream;
 import org.bouncycastle.asn1.ASN1ObjectIdentifier;
 import org.bouncycastle.asn1.ASN1OctetString;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.DERObject;
 import org.bouncycastle.asn1.DERSequence;
 import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
 import org.bouncycastle.asn1.pkcs.EncryptionScheme;
 import org.bouncycastle.asn1.pkcs.PBES2Parameters;
 import org.bouncycastle.asn1.pkcs.PBKDF2Params;
 import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
 import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
 import org.bouncycastle.asn1.pkcs.RC2CBCParameter;
 import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
 import org.bouncycastle.crypto.BufferedBlockCipher;
 import org.bouncycastle.crypto.CipherParameters;
 import org.bouncycastle.crypto.PBEParametersGenerator;
 import org.bouncycastle.crypto.engines.DESedeEngine;
 import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
 import org.bouncycastle.crypto.modes.CBCBlockCipher;
 import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
 import org.bouncycastle.crypto.params.ParametersWithIV;
 import org.bouncycastle.jce.X509Principal;
 import org.bouncycastle.operator.ContentSigner;
 import org.bouncycastle.operator.ContentVerifier;
 import org.bouncycastle.operator.ContentVerifierProvider;
 import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
 import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
 import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
 import org.bouncycastle.util.encoders.Hex;
 
 import com.lucasian.crypt.signer.CertData;
 import com.lucasian.crypt.signer.Signer;
 
 import org.apache.commons.io.IOUtils;
 
 public class BouncySigner implements Signer{
 
 	@Override
 	public String sign(String signString, InputStream certStream, 
 			InputStream keyStream, String password) throws Exception{
 		PrivateKey pk = buildPrivateKey(keyStream, password);
 
 		ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA")
 				.setProvider("BC").build(pk);
 		
 		signer.getOutputStream().write(signString.getBytes());
 		String signedString = new String(Hex.encode(signer.getSignature()));
 
 		return signedString;
 	}
 
 	@Override
 	public void verifyCert(InputStream certStream) throws CertificateNotYetValidException, 
 		CertificateExpiredException, Exception {
 		readCert(certStream).checkValidity();
 	}
 
 	@Override
 	public boolean validate(final InputStream certStream, String signString, String signedString) throws 
 		CertificateNotYetValidException, CertificateExpiredException, Exception {
 		
 		X509Certificate cert = this.readCert(certStream);
 		
 		cert.checkValidity();
 		
 		PublicKey puk = cert.getPublicKey();
 		AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder()
 				.find("SHA1withRSA");
 
 		ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder()
 			.setProvider("BC").build(puk);
 
 		ContentVerifier verifier = verifierProvider.get(sigAlgId);
 		verifier.getOutputStream().write(signString.getBytes());
 		
 		return verifier.verify(Hex.decode(signedString.getBytes()));
 	}
 	
 	@Override
 	public CertData getCertData(InputStream certStream)
 			throws IOException, Exception {
 		CertData certData = new CertData();
		Map<String, String> userData = null;
 
 		X509Certificate cert = readCert(certStream);
 		X509Principal principal = (X509Principal) cert.getSubjectDN();
 
		userData = new HashMap<String, String>();
 
 		@SuppressWarnings({ "unchecked", "deprecation" })
 		Vector<ASN1ObjectIdentifier> oids = principal.getOIDs();
 		@SuppressWarnings({ "unchecked", "deprecation" })
 		Vector<String> value = principal.getValues();
 
 		for (int i = 0; i < oids.size(); i++) {
			userData.put(oids.get(i).getId(), value.get(i));
 		}
 		certData.setSerialNumber(cert.getSerialNumber().toString());
 		certData.setExpirationDate(cert.getNotAfter());
 		
 		return certData;
 	}
 	
 	private X509Certificate readCert(InputStream is) throws Exception {
 		X509Certificate cert;
 		try {
 			CertificateFactory cf = CertificateFactory.getInstance("X.509",
 					"BC");
 			cert = (X509Certificate) cf.generateCertificate(is);
 		} finally {
 			if (is != null) {
 				is.close();
 			}
 		}
 		return cert;
 	}
 	
 	private PrivateKey buildPrivateKey(InputStream keyStream, String password) throws Exception {
 		BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
 				new CBCBlockCipher(new DESedeEngine()));
 		int keySize = 192;
 
 		ASN1InputStream asn1 = new ASN1InputStream(
 				IOUtils.toByteArray(keyStream)
 				);
 		DERObject der = asn1.readObject();
 		DERSequence sequence = (DERSequence) der;
 
 		PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
 
 		EncryptedPrivateKeyInfo info = new EncryptedPrivateKeyInfo(sequence);
 
 		PBES2Parameters alg = new PBES2Parameters((ASN1Sequence) info
 				.getEncryptionAlgorithm().getParameters());
 		PBKDF2Params func = (PBKDF2Params) alg.getKeyDerivationFunc()
 				.getParameters();
 		EncryptionScheme scheme = alg.getEncryptionScheme();
 
 		if (func.getKeyLength() != null) {
 			keySize = func.getKeyLength().intValue() * 8;
 		}
 
 		int iterationCount = func.getIterationCount().intValue();
 		byte[] salt = func.getSalt();
 
 		generator.init(PBEParametersGenerator.PKCS5PasswordToBytes(password
 				.toCharArray()), salt, iterationCount);
 
 		CipherParameters param;
 
 		if (scheme.getAlgorithm().equals(PKCSObjectIdentifiers.RC2_CBC)) {
 			RC2CBCParameter rc2Params = new RC2CBCParameter(
 					(ASN1Sequence) scheme.getObject());
 			byte[] iv = rc2Params.getIV();
 
 			param = new ParametersWithIV(
 					generator.generateDerivedParameters(keySize), iv);
 		} else {
 			byte[] iv = ((ASN1OctetString) scheme.getObject()).getOctets();
 
 			param = new ParametersWithIV(
 					generator.generateDerivedParameters(keySize), iv);
 		}
 
 		cipher.init(false, param);
 
 		byte[] data = info.getEncryptedData();
 		byte[] out = new byte[cipher.getOutputSize(data.length)];
 		int len = cipher.processBytes(data, 0, data.length, out, 0);
 
 		len += cipher.doFinal(out, len);
 
 		ASN1InputStream asn1Out = new ASN1InputStream(out);
 
 		PrivateKeyInfo keyInfo = new PrivateKeyInfo(
 				(ASN1Sequence) asn1Out.readObject());
 
 		return KeyFactory.getInstance(
 				keyInfo.getAlgorithmId().getAlgorithm().getId(), "BC")
 				.generatePrivate(new PKCS8EncodedKeySpec(keyInfo.getEncoded()));
 	}
 	
 }
