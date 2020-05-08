 package nl.nikhef.jgridstart.util;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.security.GeneralSecurityException;
 import java.security.Key;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchProviderException;
 import java.security.PrivateKey;
 import java.security.Provider;
 import java.security.SecureRandom;
 import java.security.spec.AlgorithmParameterSpec;
 
 import javax.crypto.Cipher;
 import javax.crypto.CipherSpi;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.PBEParameterSpec;
 
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.DERObjectIdentifier;
 import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
 import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
 import org.bouncycastle.jce.provider.JCEBlockCipher;
 import org.bouncycastle.jce.provider.JCEPBEKey;
 import org.bouncycastle.jce.provider.JDKPKCS12KeyStore;
 
 /** Access a PKCS12 keystore without policy restrictions.
  * <p>
  * Default Java installations have restrictions on the use of strong
  * cryptograhpy. This can be solved by installing java policy files, but
  * it often is undesirable to require the a user to go through this. This
  * class provides a workaround that circumvent the Java policy restrictions
  * and allows opening PKCS#12 keystores with passwords longer than 7 characters. 
  * <p>
 * You should use {@code BCPKCS12KeyStore.getInstance()} instead of
  * {@code KeyStore.getInstance("PKCS12", "BC")}. Now {@link KeyStore#load} and
 * {@link KeyStore#store} will work with passwords longer than seven characters
 * without the unlimited strength policy files installed.
  * <p>
  * To illustrate, here is some code to read a certificate from a PKCS#12 file.
  * <pre><code>
  *   FileInputStream in = new FileInputStream("test.p12");
  *   KeyStore store = PKCS12KeyStoreUnlimited.getInstance();
  *   store.load(in, "thisismylongpassworduhoh".toCharArray());
  *   Certificate cert = store.getCertificate("certificate alias");
  * </code></pre>
  * <p>
  * This class has intricate knowledge of JCE and BouncyCastle internals. When
  * these change, this class needs to be updated appropriately.
  * <p>
  * If you are using policy files to specify permissions, you may need to add
  * the following to permit accessing JCE's private members, e.g.: 
  * <pre><code>
  * grant {
  *    // allow access to JCE internals to bypass keysize restrictions
  *    // as implemented by PKCS12KeyStoreUnlimited
  *    permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
  *    permission java.lang.RuntimePermission "accessDeclaredMembers";
 * } 
  * </code></pre>
  * <p>
  * This is tested continuously on CentOS 4, CentOS 5, Windows XP 32-bit,
  * Windows 2003 32-bit, Mac OS X 10.4 and Mac OS X 10.5 with Java5 and Java6.
  * In addition, OpenJDK is tested on CentOS 5. Many other platforms and
  * configurations are tested occasionaly as well.
  * 
  * @author Willem van Engen <wvengen@nikhef.nl>
  */
 public class PKCS12KeyStoreUnlimited extends JDKPKCS12KeyStore {
     
     /** Return a {@link KeyStore} that circumvents JCE security restrictions.
      * <p>
      * This requires using BouncyCastle algorithms, that should be ok by default.
      * Currently {@linkplain Cipher} restrictions on key length are bypassed.
      * 
      * @return a new {@linkplain KeyStore}
      */
     public static KeyStore getInstance() throws KeyStoreException, NoSuchProviderException {
 	KeyStore store = KeyStore.getInstance("PKCS12", "BC");
 	try {
 	    // retrieve private JCE fields to obtain provider
 	    Field keyStoreProvider = store.getClass().getDeclaredField("provider");
 	    keyStoreProvider.setAccessible(true);
 	    Provider provider = (Provider)keyStoreProvider.get(store);
 	    // ... and keystore implementation
 	    Field keyStoreSpi = store.getClass().getDeclaredField("keyStoreSpi");
 	    keyStoreSpi.setAccessible(true);
 	    JDKPKCS12KeyStore bcStore = (JDKPKCS12KeyStore)keyStoreSpi.get(store);
 	    // override that by using our wrapper class
 	    keyStoreSpi.set(store, new PKCS12KeyStoreUnlimited(provider, bcStore));
 	    return store;
 	} catch (RuntimeException e) {
 	    throw new NoSuchProviderException("Could not create unlimited strength KeyStore:\n"+e.getMessage());
 	} catch (NoSuchFieldException e) {
 	    throw new NoSuchProviderException("Could not create unlimited strength KeyStore:\n"+e.getMessage());
 	} catch (IllegalAccessException e) {
 	    throw new NoSuchProviderException("Could not create unlimited strength KeyStore:\n"+e.getMessage());
 	}
     }    
     
     /* we don't have access to bcProvider in the superclass */
     private Provider provider = null;
     
     /** Standard constructor */
     public PKCS12KeyStoreUnlimited(
 	        Provider provider,
 	        DERObjectIdentifier keyAlgorithm,
 	        DERObjectIdentifier certAlgorithm) {
     	super(provider, keyAlgorithm, certAlgorithm);
     	this.provider = provider;
     }
     
     /** Kind of copy constructor */
     public PKCS12KeyStoreUnlimited(Provider provider, JDKPKCS12KeyStore from) {
 	this(provider, getAlgorithm(from)[0], getAlgorithm(from)[1]);
     }
     
     /** Extract algorithms from an object.
      * <p>
      * This method needs updating when bouncycastle adds implementations.
      */
     protected static DERObjectIdentifier[] getAlgorithm(JDKPKCS12KeyStore from) {
 	// based on the original JDKPKCS12KeyStore derived classes
 	if (from instanceof org.bouncycastle.jce.provider.JDKPKCS12KeyStore.BCPKCS12KeyStore)
 	    return new DERObjectIdentifier[] {pbeWithSHAAnd3_KeyTripleDES_CBC, pbewithSHAAnd40BitRC2_CBC};
 	else if (from instanceof org.bouncycastle.jce.provider.JDKPKCS12KeyStore.BCPKCS12KeyStore3DES)
 	    return new DERObjectIdentifier[] {pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd3_KeyTripleDES_CBC};
 	else if (from instanceof org.bouncycastle.jce.provider.JDKPKCS12KeyStore.DefPKCS12KeyStore)
 	    return new DERObjectIdentifier[] {pbeWithSHAAnd3_KeyTripleDES_CBC, pbewithSHAAnd40BitRC2_CBC};
 	else if (from instanceof org.bouncycastle.jce.provider.JDKPKCS12KeyStore.DefPKCS12KeyStore3DES)
 	    return new DERObjectIdentifier[] {pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd3_KeyTripleDES_CBC};
 
 	// whoops, this shouldn't happen!
 	assert(false);
 	return new DERObjectIdentifier[] {null, null};
     }
 
     /** {@inheritDoc}
      * <p>
      * Same as BouncyCastle's implementation, but calls BouncyCastle's cipher
      * directly instead of going via JCE's {@link Cipher}.
      */
     @Override
     protected PrivateKey unwrapKey(
 	    AlgorithmIdentifier   algId,
 	    byte[]                data,
 	    char[]                password,
 	    boolean               wrongPKCS12Zero)
     throws IOException
     {
 	String              algorithm = algId.getObjectId().getId();
 	PKCS12PBEParams     pbeParams = new PKCS12PBEParams((ASN1Sequence)algId.getParameters());
 
 	PBEKeySpec          pbeSpec = new PBEKeySpec(password);
 	PrivateKey          out;
 
 	try
 	{
 	    SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
 		    				algorithm, provider);
 	    PBEParameterSpec    defParams = new PBEParameterSpec(
 		    				pbeParams.getIV(),
 		    				pbeParams.getIterations().intValue());
 
 	    SecretKey           key = keyFact.generateSecret(pbeSpec);
 
             // ((JCEPBEKey)key).setTryWrongPKCS12Zero(wrongPKCS12Zero);
             Method setTryZero = key.getClass().getDeclaredMethod("setTryWrongPKCS12Zero", new Class<?>[]{ boolean.class });
             setTryZero.setAccessible(true);
             setTryZero.invoke(key, new Object[]{ wrongPKCS12Zero });
 	    
 	    // we pass "" as the key algorithm type as it is unknown at this point
 	    out = (PrivateKey)cipherUnwrap(algorithm, key, data, defParams);
 	}
 	catch (Exception e)
 	{
 	    throw new IOException("exception unwrapping private key - " + e.toString());
 	}
 
 	return out;
     }
 
     /** {@inheritDoc}
      * <p>
      * Same as BouncyCastle's implementation, but calls BouncyCastle's cipher
      * directly instead of going via JCE's {@link Cipher}.
      */
     @Override
     protected byte[] wrapKey(
 	        String                  algorithm,
 	        Key                     key,
 	        PKCS12PBEParams         pbeParams,
 	        char[]                  password)
 	        throws IOException {
         PBEKeySpec          pbeSpec = new PBEKeySpec(password);
         byte[] out;
 
         try
         {
             SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                 algorithm, provider);
             PBEParameterSpec    defParams = new PBEParameterSpec(
                                                 pbeParams.getIV(),
                                                 pbeParams.getIterations().intValue());
 
             SecretKey cipherkey = keyFact.generateSecret(pbeSpec);
 
             out = cipherWrap(algorithm, cipherkey, key, defParams);
         }
         catch (Exception e)
         {
             throw new IOException("exception encrypting data - " + e.toString());
         }
         
         return out;
     }
 
     /** {@inheritDoc}
      * <p>
      * Same as BouncyCastle's implementation, but calls BouncyCastle's cipher
      * directly instead of going via JCE's {@link Cipher}.
      */
     @Override
     protected byte[] cryptData(
 	        boolean               forEncryption,
 	        AlgorithmIdentifier   algId,
 	        char[]                password,
 	        boolean               wrongPKCS12Zero,
 	        byte[]                data)
 	        throws IOException {
         String          algorithm = algId.getObjectId().getId();
         PKCS12PBEParams pbeParams = new PKCS12PBEParams((ASN1Sequence)algId.getParameters());
         PBEKeySpec      pbeSpec = new PBEKeySpec(password);
 
         try
         {
             SecretKeyFactory keyFact = SecretKeyFactory.getInstance(algorithm, provider);
             PBEParameterSpec defParams = new PBEParameterSpec(
                 pbeParams.getIV(),
                 pbeParams.getIterations().intValue());
             JCEPBEKey        key = (JCEPBEKey) keyFact.generateSecret(pbeSpec);
 
             // key.setTryWrongPKCS12Zero(wrongPKCS12Zero);
             Method setTryZero = key.getClass().getDeclaredMethod("setTryWrongPKCS12Zero", new Class<?>[]{ boolean.class });
             setTryZero.setAccessible(true);
             setTryZero.invoke(key, new Object[]{ wrongPKCS12Zero });
 
             return cipherCrypt(algorithm, key, forEncryption, data, defParams);
         }
         catch (Exception e)
         {
             throw new IOException("exception encrypting data - " + e.toString());
         }
     }
     
     protected byte[] cipherCrypt(String algorithm, SecretKey key, boolean encrypt, byte[] data, PBEParameterSpec defParams) throws Exception {
 	JCEBlockCipher spi = doCipherInit(algorithm, key, encrypt?Cipher.ENCRYPT_MODE:Cipher.DECRYPT_MODE, defParams);
 	// return cipher.doFinal(data);
 	Method spiDoFinal = spi.getClass().getSuperclass().getDeclaredMethod("engineDoFinal",
 		new Class<?>[] { byte[].class, int.class, int.class });
 	spiDoFinal.setAccessible(true);
 	return (byte[])spiDoFinal.invoke(spi, new Object[] { data, 0, data.length });
     }
     
     protected byte[] cipherWrap(String algorithm, SecretKey key, Key data, PBEParameterSpec defParams) throws Exception {
 	JCEBlockCipher spi = doCipherInit(algorithm, key, Cipher.WRAP_MODE, defParams);
 	// return cipher.wrap(data);
 	Method spiWrap = spi.getClass().getSuperclass().getSuperclass().getDeclaredMethod("engineWrap",
 		new Class<?>[] { Key.class });
 	spiWrap.setAccessible(true);
 	return (byte[])spiWrap.invoke(spi, new Object[] { data });
     }
 
     protected Key cipherUnwrap(String algorithm, SecretKey key, byte[] data, PBEParameterSpec defParams) throws Exception {
 	JCEBlockCipher spi = doCipherInit(algorithm, key, Cipher.UNWRAP_MODE, defParams);
 	// return cipher.unwrap(data);
 	Method spiUnwrap = spi.getClass().getSuperclass().getSuperclass().getDeclaredMethod("engineUnwrap",
 		new Class<?>[] { byte[].class, String.class, int.class });
 	spiUnwrap.setAccessible(true);
 	return (Key)spiUnwrap.invoke(spi, new Object[] { data, "", Cipher.PRIVATE_KEY });
     }
 
     private JCEBlockCipher doCipherInit(String algorithm, SecretKey key, int opmode, PBEParameterSpec defParams)
     		throws GeneralSecurityException, RuntimeException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
         Cipher cipher = Cipher.getInstance(algorithm, provider);
         // get implementation
         Field spiField = cipherFindField(cipher, "spi", CipherSpi.class);
         JCEBlockCipher spi = (JCEBlockCipher)spiField.get(cipher);
         // cipher.init(opmode, key, defParams);
         Method spiInit = spi.getClass().getSuperclass().getDeclaredMethod("engineInit",
         	new Class<?>[] { int.class, Key.class, AlgorithmParameterSpec.class, SecureRandom.class });
         spiInit.setAccessible(true);
         spiInit.invoke(spi, new Object[] { opmode, key, defParams, random  });
         return spi;
     }
     
     /** Find field in cipher object.
      * <p>
      * The Cipher object has been obfuscated in at least some versions of Java.
      * To be able to access the field anyway, we look for a variable with the
      * same type. Cipher is not very complex, so this should be ok. Just make
      * sure that you'll have just one match!
      * <p>
      * The field is set accessible before it is returned. Please see class
      * comments for granting permissions.
      *  
      * @param cipher instance to look at
      * @param name original name of the field, is attempted first
      * @param type type of variable to look for
      * @return the field
      * @throws NoSuchFieldException 
      */
     private Field cipherFindField(Cipher cipher, String name, Class<?> type) throws NoSuchFieldException {
 	try {
 	    // try name itself first
 	    Field field = cipher.getClass().getDeclaredField(name);
 	    field.setAccessible(true);
 	    return field;
 	} catch (Exception e1) {
 	    // then just find the first field that matches type
 	    try {
 		Field[] fields = cipher.getClass().getDeclaredFields();
 		for (int i=0; i<fields.length; i++) {
 		    fields[i].setAccessible(true);
 		    Object o = fields[i].get(cipher);
 		    if (type.isInstance(o))
 			return fields[i];
 		}
 		// not found
 		throw new Exception();
 	    } catch (Exception e2) {
 		throw new NoSuchFieldException("Could not find field "+name+" in Cipher");
 	    }
 	}
     }
 }
