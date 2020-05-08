 package edu.ucsb.cs290.touch.to.text.crypto;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.security.GeneralSecurityException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.Security;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.spongycastle.jce.provider.BouncyCastleProvider;
 import org.spongycastle.jce.spec.ElGamalParameterSpec;
 
 import android.util.Log;
 
 public class KeyPairsProvider implements Serializable {
 	/**
 	 * One ElGamal keypair, and two DSA keypairs.
 	 */
 	private static final long serialVersionUID = 1L;
 
 	static {
 		Security.addProvider(new BouncyCastleProvider());
 	}
 	private KeyPair signingKeyPair;
 	private KeyPair transmissonKeyPair;
 	private KeyPair tokenSigningKeyPair;
 
 	public KeyPairsProvider() {
 		ExecutorService e = Executors.newCachedThreadPool();
 		Future<KeyPair> signingFut = e.submit(new Generate("DSA", 1024));
 		Future<KeyPair> transFut = e.submit(new Generate("ElGamal", 1024));
 		Future<KeyPair> tokenFut = e.submit(new Generate("DSA", 1024));
 		try {
 			signingKeyPair = signingFut.get();
 			transmissonKeyPair = transFut.get();
 			tokenSigningKeyPair = tokenFut.get();
 		} catch (Exception e1) {
 			Log.wtf("touch-to-text", "Interrupted key generation", e1);
 		}
 	}
 
 	private static KeyPair generate(String algorithm, int bits) {
		Log.d("touch-to-chat", "starting generation of " + algorithm);
 		long time = System.currentTimeMillis();
 		KeyPairGenerator gen = null;
 		try {
 			gen = KeyPairGenerator.getInstance(algorithm, "SC");
 		} catch (GeneralSecurityException e) {
 			Log.wtf("touch-to-text", e);
 		}
 		if(algorithm == "ElGamal") {
 			String[] key = "FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1 29024E08 8A67CC74 020BBEA6 3B139B22 514A0879 8E3404DDEF9519B3 CD3A431B 302B0A6D F25F1437 4FE1356D 6D51C245E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7EDEE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3DC2007CB8 A163BF05 98DA4836 1C55D39A 69163FA8 FD24CF5F83655D23 DCA3AD96 1C62F356 208552BB 9ED52907 7096966D670C354E 4ABC9804 F1746C08 CA18217C 32905E46 2E36CE3B E39E772C 180E8603 9B2783A2 EC07A28F B5C55DF0 6F4C52C9DE2BCBF6 95581718 3995497C EA956AE5 15D22618 98FA051015728E5A 8AACAA68 FFFFFFFF FFFFFFFF".split(" ");
 			String finalKey = "";
 			for (String string : key) {
 				finalKey = finalKey + string;
 			}
 			try {
 				gen.initialize(new ElGamalParameterSpec(new BigInteger(finalKey, 16), new BigInteger("2")));
 			} catch(Exception e) {
 				//fuick
 			}
 		} else {
 			gen.initialize(bits);
 		}
 		KeyPair kp = gen.generateKeyPair();
 		Log.d("touch-to-text", "Done generating " + algorithm + "took "
 				+ (System.currentTimeMillis() - time)  + " ms.");
 		return kp;
 	}
 
 	private class Generate implements Callable<KeyPair> {
 		private final String algorithm;
 		private final int bits;
 
 		public Generate(String algorithm, int bits) {
 			this.algorithm = algorithm;
 			this.bits = bits;
 		}
 
 		@Override
 		public KeyPair call() throws Exception {
 			return generate(algorithm, bits);
 		}
 	}
 
 	public SealablePublicKey getExternalKey() {
 		return new SealablePublicKey(signingKeyPair.getPublic(),
 				transmissonKeyPair.getPublic(), tokenSigningKeyPair);
 	}
 
 	KeyPair getSigningKey() {
 		// TODO Auto-generated method stub
 		return signingKeyPair;
 	}
 	
 	KeyPair getTokenKey() {
 		return tokenSigningKeyPair;
 	}
 	
 	KeyPair getEncryptionKey() {
 		return transmissonKeyPair;
 	}
 }
