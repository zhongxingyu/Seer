 package org.araqne.logstorage;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.cert.Certificate;
 
 import org.araqne.api.FieldOption;
 import org.araqne.confdb.CollectionName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 // TODO : uri handling
 @CollectionName("crypto_profiles")
 public class LogCryptoProfile {
 	/**
 	 * profile name
 	 */
 	@FieldOption(nullable = false)
 	private String name;
 
 	/**
 	 * cipher algorithm (e.g. AES/CBC/PKCS5Padding)
 	 */
 	private String cipher;
 
 	/**
 	 * digest algorithm (e.g. HmacSHA256)
 	 */
 	private String digest;
 
 	/**
 	 * pkcs#12 file path
 	 */
 	private String filePath;
 
 	/**
 	 * pkcs#12 keystore password
 	 */
 	private String password;
 
 	@FieldOption(skip = true)
 	private KeyStore keystore;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getCipher() {
 		return cipher;
 	}
 
 	public void setCipher(String cipher) {
 		this.cipher = cipher;
 	}
 
 	public String getDigest() {
 		return digest;
 	}
 
 	public void setDigest(String digest) {
 		this.digest = digest;
 	}
 
 	public String getFilePath() {
 		return filePath;
 	}
 
 	public void setFilePath(String filePath) {
 		this.filePath = filePath;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public PublicKey getPublicKey() {
 		try {
 			ensureKeystore();
 			String alias = keystore.aliases().nextElement();
 			Certificate[] cc = keystore.getCertificateChain(alias);
 			return cc[0].getPublicKey();
 		} catch (Exception e) {
 			throw new IllegalStateException("cannot load public key of crypto profile " + name, e);
 		}
 	}
 
 	public PrivateKey getPrivateKey() {
 		try {
 			ensureKeystore();
 			String alias = keystore.aliases().nextElement();
 			return (PrivateKey) keystore.getKey(alias, password.toCharArray());
 		} catch (Exception e) {
 			throw new IllegalStateException("cannot load private key of crypto profile " + name, e);
 		}
 	}
 
 	private void ensureKeystore() {
 		if (keystore != null)
 			return;
 
 		FileInputStream is = null;
 		try {
 			KeyStore pfx = KeyStore.getInstance("PKCS12");
 			is = new FileInputStream(new File(filePath));
 			pfx.load(is, password.toCharArray());
 			this.keystore = pfx;
		} catch (Throwable t) {
			// explicit logging for fast troubleshooting. query command may
			// ignore this kinds of reader open failure and just skip.
			String msg = "cannot load pfx file [" + filePath + "] of crypto profile [" + name + "]";
			Logger slog = LoggerFactory.getLogger(LogCryptoProfile.class);
			slog.error("araqne logstorage: " + msg, t);

			throw new IllegalStateException(msg, t);
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "name=" + name + ", cipher=" + cipher + ", digest=" + digest + ", path=" + filePath + ", password=" + password;
 	}
 }
