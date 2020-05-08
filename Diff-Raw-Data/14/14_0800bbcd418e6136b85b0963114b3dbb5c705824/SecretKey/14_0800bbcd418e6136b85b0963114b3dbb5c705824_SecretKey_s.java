 /*
  * Copyright 2011 Jonathan Anderson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.footlights.core.crypto;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 
 import javax.crypto.Cipher;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.commons.codec.binary.Hex;
 
 import me.footlights.core.Preferences;
 import me.footlights.core.data.Link;
 
 
 /** A secret, symmetric key. */
 public class SecretKey
 {
 	public enum Operation
 	{
 		ENCRYPT(Cipher.ENCRYPT_MODE),
 		DECRYPT(Cipher.DECRYPT_MODE);
 
 		private Operation(int value) { this.value = value; }
 		int opcode() { return value; }
 
 		private final int value;
 	};
 
 	public String getAlgorithm() { return keySpec.getAlgorithm(); }
 	public Fingerprint getFingerprint() { return fingerprint; }
 	public SecretKeySpec getKey() { return keySpec; }
 	public URI toUri() { return uri; }
 
 	/** Generate a new secret key. */
 	public static Generator newGenerator() { return new Generator(); }
 
 	/** Get the cipher instance, which can be used for encrypting and decrypting data. */
 	public CipherBuilder newCipherBuilder() { return new CipherBuilder(); }
 
 	/** Start constructing a {@link Link} to data encrypted by this key. */
 	Link.Builder createLinkBuilder()
 	{
 		return Link.newBuilder()
 			.setFingerprint(fingerprint)
 			.setKey(this);
 	}
 
 	public static class Generator
 	{
 		public String getAlgorithm() { return algorithm; }
 		public int getKeyLength() { return keylen; }
 
 		public Generator setAlgorithm(String a) { algorithm = a; return this; }
 		public Generator setBytes(byte[] s) { secret = s; return this; }
 		public Generator setFingerprintAlgorithm(String a) throws NoSuchAlgorithmException
 		{
 			fingerprint.setAlgorithm(a);
 			return this;
 		}
 		public Generator setKey(SecretKeySpec spec) { keySpec = spec; return this; }
 		public Generator setKeyLength(int l) { keylen = l; return this; }
 
 		public SecretKey generate() throws NoSuchAlgorithmException
 		{
 			if (algorithm.contains("/"))
 				throw new NoSuchAlgorithmException(
 					"Don't need mode or padding information in " + SecretKey.class.getName());
 
 			if (secret == null)
 			{
 				secret = new byte[keylen];
 				SecureRandom.getInstance(preferences.getString("crypto.prng").get())
 					.nextBytes(secret);
 			}
 
 			if (keySpec == null) keySpec = new SecretKeySpec(secret, algorithm);
 			return new SecretKey(keySpec, fingerprint.setContent(secret).build());
 		}
 
 
 		private String algorithm = preferences.getString("crypto.sym.algorithm").get();
 
 		private int keylen = preferences.getInt("crypto.sym.keylen").get();
 		private byte[] secret = null;
 		private Fingerprint.Builder fingerprint = Fingerprint.newBuilder();
 		private SecretKeySpec keySpec;
 	}
 	
 
 
 	public class CipherBuilder
 	{
 		public Cipher build()
 			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
 			       InvalidAlgorithmParameterException
 		{
 			String fullAlgorithm = keySpec.getAlgorithm() + "/" + mode + "/" + padding;
 			Cipher cipher = Cipher.getInstance(fullAlgorithm);
 
 			IvParameterSpec iv = null;
 			if (!mode.equals("ECB")) iv = new IvParameterSpec(new byte[cipher.getBlockSize()]);
 
 			cipher.init(operation.opcode(), keySpec, iv);
 
 			return cipher;
 		}
 
 		public CipherBuilder parseAlgorithm(String a)
 		{
 			String[] parts = a.split("/");
 			if (parts.length != 3)
 				throw new IllegalArgumentException(
 					"parseAlgorithm() requires 'alg/mode/padding' string, got '" + a + "'");
 
 			if (!parts[0].equals(keySpec.getAlgorithm()))
 				throw new IllegalArgumentException(
 					"Wrong algorithm: got " + parts[0] + ", expected " + keySpec.getAlgorithm());
 
 			setMode(parts[1]);
 			setPaddingScheme(parts[2]);
 
 			return this;
 		}
 
 		public CipherBuilder setOperation(Operation o)  { operation = o; return this; }
 		public CipherBuilder setMode(String m)          { mode = m;      return this; }
 		public CipherBuilder setPaddingScheme(String p) { padding = p;   return this; }
 
 
 		private CipherBuilder() {}
 
 		private Operation operation = Operation.ENCRYPT;
 		private String mode = preferences.getString("crypto.sym.mode").get();
 		private String padding = preferences.getString("crypto.sym.padding").get();
 	}
 
 
 	// Object override.
 	@Override public String toString()
 	{
 		return SecretKey.class.getSimpleName() + " { " + fingerprint + " }";
 	}
 
 
 	private SecretKey(SecretKeySpec key, Fingerprint fingerprint)
 	{
 		this.keySpec = key;
 		this.fingerprint = fingerprint;
 
		try
		{
			String algorithm = keySpec.getAlgorithm();
			String keyData = new String(Hex.encodeHex(keySpec.getEncoded()));
			this.uri = new URI(algorithm, keyData, null);
		}
 		catch (URISyntaxException e)
 		{
			throw new IllegalArgumentException("Unable to generate URI for " + keySpec);
 		}
 	}
 
 	final SecretKeySpec keySpec;
 	private final Fingerprint fingerprint;
 	private final URI uri;
 
 	/** Footlights-wide preferences. */
 	private static Preferences preferences = Preferences.getDefaultPreferences();
 }
