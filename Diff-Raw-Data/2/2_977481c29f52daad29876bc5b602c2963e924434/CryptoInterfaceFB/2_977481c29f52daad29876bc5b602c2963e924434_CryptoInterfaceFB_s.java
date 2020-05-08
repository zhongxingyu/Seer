 package it.uninsubria.dicom.cryptosocial.shared;
 
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HHVEIP08AttributesOnlySearchKeyGenerator;
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PrivateKeyParameters;
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08SearchKeyGenerationParameters;
 import it.unisa.dia.gas.crypto.engines.MultiBlockAsymmetricBlockCipher;
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.engines.HHVEIP08AttributesEngine;
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.engines.HHVEIP08Engine;
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08ParametersGenerator;
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08EncryptionParameters;
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08KeyGenerationParameters;
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;
 import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PublicKeyParameters;
 import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.URL;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.Iterator;
 
 import javax.crypto.Cipher;
 import javax.crypto.CipherOutputStream;
 import javax.crypto.KeyGenerator;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 
 import org.apache.log4j.Logger;
 import org.bouncycastle.crypto.AsymmetricBlockCipher;
 import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
 import org.bouncycastle.crypto.CipherParameters;
 import org.bouncycastle.crypto.InvalidCipherTextException;
 import org.bouncycastle.crypto.paddings.ZeroBytePadding;
 
 public class CryptoInterfaceFB implements CryptoInterface {
 	private static CryptoInterface instance;
 	private static Logger logger = Logger.getLogger(CryptoInterfaceFB.class);
 	
 	private KeyGenerator	symmetricKeyGenerator;
 	private HVEIP08KeyPairGenerator	keyPairGenerator;
 	private CommonProperties	properties;
 	
 	private CryptoInterfaceFB() {
 		properties = CommonProperties.getInstance();
 		
 		try {
 			symmetricKeyGenerator = KeyGenerator.getInstance(properties.getSymmetricAlgorithm());
 		} catch (NoSuchAlgorithmException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		symmetricKeyGenerator.init(properties.getKeySize());
 
 		URL url = properties.getParametersPath();
 
 		HVEIP08Parameters parameters = null;
 
 		if (null == url) {
 			logger.debug("not existing");
 
 			// generate parameters
 			CurveParams curveParams = new CurveParams();
 			curveParams.load(properties.getCurveParams());
 
 			HVEIP08ParametersGenerator generator = new HVEIP08ParametersGenerator();
			generator.init(curveParams, properties.getLength());
 
 			parameters = generator.generateParameters();
 
 /*			File parameterFile = new File(new File(this.getClass().getClassLoader().getResource("/").getFile() + "/../").getAbsolutePath() + properties.getParametersPathString());
 
 			logger.debug(parameterFile.getAbsolutePath());
 
 			ObjectOutputStream oos;
 			try {
 				oos = new ObjectOutputStream(new FileOutputStream(parameterFile));
 				oos.writeObject(parameters);
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}*/
 			logger.debug("Parameters generated");
 		} else {
 			logger.debug("existing");
 
 			ObjectInputStream ois;
 			try {
 				ois = new ObjectInputStream(new FileInputStream(url.getFile()));
 
 				parameters = (HVEIP08Parameters) ois.readObject();
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		keyPairGenerator = new HVEIP08KeyPairGenerator();
 		keyPairGenerator.init(new HVEIP08KeyGenerationParameters(new SecureRandom(), parameters));
 		
 		logger.debug("initialized");
 	}
 
 	/*@Override
 	public KeyGenerator getSymmetricKeyGenerator() {
 		return this.symmetricKeyGenerator;
 	}
 
 	@Override
 	public HVEIP08KeyPairGenerator getKeyPairGenerator() {
 		return this.keyPairGenerator;
 	}*/
 
 	@Override
 	public Resource encrypt(byte[] resource, int[] policy, CipherParameters publicKey) {
 		EncryptedResource eRes = null;
 		
 		try {
 			SecretKey symmetricKey = symmetricKeyGenerator.generateKey();
 			Cipher cipher;
 		
 			cipher = Cipher.getInstance(properties.getSymmetricAlgorithm());
 		
 			cipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
 	
 			ByteArrayOutputStream encryptedResource = new ByteArrayOutputStream();
 			CipherOutputStream cOut = new CipherOutputStream(encryptedResource, cipher);
 	
 			cOut.write(resource);
 			cOut.close();
 	
 			byte[] encryptedSymmetricKeyBytes = encryptSymmetricKey(convertKeysToBytes(symmetricKey), publicKey, policy);
 			
 			
 			eRes = new EncryptedResource(encryptedResource.toByteArray(), encryptedSymmetricKeyBytes);
 			} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return eRes;
 	}
 	
 	private byte[] encryptSymmetricKey(byte[] symmetricKeyBytes, CipherParameters publicKey, int[] policy) {
 		byte[] ciphertext = null;
 
 		try {
 			AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(new HHVEIP08Engine(), new ZeroBytePadding());
 
 			logger.debug("PublicKey is null: " + (publicKey == null));
 			logger.debug("Policy is null: " + (null == policy));
 			
 			engine.init(
 					true,
 					new HVEIP08EncryptionParameters((HVEIP08PublicKeyParameters) publicKey, policy));
 			ciphertext = engine.processBlock(symmetricKeyBytes, 0,
 					symmetricKeyBytes.length);
 
 		} catch (InvalidCipherTextException e) {
 			// TMCH
 			e.printStackTrace();
 		}
 
 		return ciphertext;
 	}
 	
 	private byte[] convertKeysToBytes(SecretKey symmetricKey) throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ObjectOutputStream oos = new ObjectOutputStream(baos);
 
 		oos.writeObject(symmetricKey);
 		oos.close();
 
 		return baos.toByteArray();
 	}
 	
 	public static CryptoInterface getInstance() {
 		if (null == instance)
 			instance = new CryptoInterfaceFB();
 		
 		return instance;
 	}
 
 	@Override
 	public AsymmetricCipherKeyPair generateKeyPair() {
 		return keyPairGenerator.generateKeyPair();
 	}
 
 	@Override
 	public byte[] decryptResource(Resource resource, CipherParameters key) {
 		SecretKey symmetricKey = decryptSymmetricKey(resource.getKey(), key);
 			
 		Cipher cipher = null;
 		
 		try {
 			cipher = Cipher.getInstance(properties.getSymmetricAlgorithm());
 			
 			cipher.init(Cipher.DECRYPT_MODE, symmetricKey);
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		ByteArrayOutputStream dResource = new ByteArrayOutputStream();
 		CipherOutputStream cOut = new CipherOutputStream(dResource, cipher);
 
 		try {
 			cOut.write(resource.getResource());
 		
 			cOut.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return dResource.toByteArray();
 	}
 
 	private SecretKey decryptSymmetricKey(byte[] bytes, CipherParameters privateKey) {
 		byte[] plainText = null;
 
 		try {
 			AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(new HHVEIP08Engine(), new ZeroBytePadding());
 			engine.init(false, privateKey);
 
 			plainText = engine.processBlock(bytes, 0, bytes.length);
 
 			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(plainText));
 
 			return (SecretKey) ois.readObject();
 		} catch (InvalidCipherTextException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null; // TMCH
 	}
 
 	@Override
 	public CipherParameters generateSearchKey(CipherParameters privateKey, int... policy) {
 		HHVEIP08AttributesOnlySearchKeyGenerator generator = new HHVEIP08AttributesOnlySearchKeyGenerator();
 		
 		generator.init(new HVEIP08SearchKeyGenerationParameters((HVEIP08PrivateKeyParameters) privateKey, policy));
 		
 		return generator.generateKey();
 	}
 
 	@Override
 	public CipherParameters delegate(CipherParameters oldSearch, int depth) {
 
 		return null; // TODO
 	}
 
 	@Override
 	public boolean testKey(Resource res, CipherParameters searchKey) {
 		byte[] ct = res.getKey();
 		
 		HHVEIP08AttributesEngine engine = new HHVEIP08AttributesEngine();
 		engine.init(false, searchKey);
 
 		return engine.processBlock(ct, 0, ct.length)[0] == 0;
 	}
 }
