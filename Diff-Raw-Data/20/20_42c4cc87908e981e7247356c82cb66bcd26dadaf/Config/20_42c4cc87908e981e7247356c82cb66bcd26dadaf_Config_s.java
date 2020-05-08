 /*
  * Copyright 2011 - 2012 by the CloudRAID Team
  * see AUTHORS for more details
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
 
  * http://www.apache.org/licenses/LICENSE-2.0
 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package de.dhbw_mannheim.cloudraid.config.impl;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Set;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.SecretKeySpec;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.osgi.framework.BundleContext;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.miginfocom.base64.Base64;
 
 import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
 import de.dhbw_mannheim.cloudraid.config.exceptions.InvalidConfigValueException;
 import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;
 import de.dhbw_mannheim.cloudraid.passwordmgr.IPasswordManager;
 
 /**
  * Enables you to handle the program's configuration via a XML file.<br>
  * 
  * @author Florian Bausch, Markus Holtermann
  */
 public class Config extends HashMap<String, String> implements ICloudRAIDConfig {
 
 	private boolean doSaveOnShutdown = true;
 
 	/**
 	 * A HashMap of allowed allowed ciphers. The keys of the map contain the
 	 * available ciphers, while the value contains the maximum salt length.
 	 */
 	private static HashMap<String, Integer> allowedCiphers = new HashMap<String, Integer>();
 
 	/**
 	 * The top-level path to the programs config.
 	 */
 	private static String CLOUDRAID_HOME = System.getProperty("os.name")
 			.contains("windows") ? System.getenv("APPDATA") + "\\cloudraid\\"
 			: System.getProperty("user.home") + "/.config/cloudraid/";
 
 	/**
 	 * The path to the default configuration file.
 	 */
 	private static String CONFIG_PATH = Config.CLOUDRAID_HOME + "config.xml";
 
 	/**
 	 * A File object of the configuration file.
 	 */
 	private static File CONFIG_FILE = new File(Config.CONFIG_PATH);
 
 	private static final String DEFAULT_DATABASE_NAME = Config.CLOUDRAID_HOME
 			+ "database";
 
 	private static final int DEFAULT_FILEMANAGEMENT_COUNT = (int) Math
 			.ceil(Runtime.getRuntime().availableProcessors() / 2);
 	private static final int DEFAULT_FILEMANAGEMENT_INTERVALL = 60000;
 	/**
 	 * Maximum file size in MiB
 	 */
 	private static final int DEFAULT_FILESIZE_MAX = 1024 * 1024 * 512;
 	private static final String DEFAULT_MERGE_INPUT_DIR = System
 			.getProperty("java.io.tmpdir")
 			+ File.separator
 			+ "cloudraid-merge-input" + File.separator;
 	private static final String DEFAULT_MERGE_OUTPUT_DIR = System
 			.getProperty("java.io.tmpdir")
 			+ File.separator
 			+ "cloudraid-merge-output" + File.separator;
 	private static final String DEFAULT_SPLIT_INPUT_DIR = System
 			.getProperty("java.io.tmpdir")
 			+ File.separator
 			+ "cloudraid-split-input" + File.separator;
 
 	private static final String DEFAULT_SPLIT_OUTPUT_DIR = System
 			.getProperty("java.io.tmpdir")
 			+ File.separator
 			+ "cloudraid-split-output" + File.separator;
 
 	private static final String DEFAULT_UPLOAD_DIR = System
 			.getProperty("java.io.tmpdir")
 			+ File.separator
 			+ "cloudraid-upload" + File.separator;
 
 	private static HashMap<String, String> defaultData = new HashMap<String, String>();
 
 	/**
 	 * Global Random object for salt generation
 	 */
 	private static Random r = new Random(System.currentTimeMillis());
 	/**
 	 * Auto generated serialVersionUID
 	 */
 	private static final long serialVersionUID = -3740632761998756639L;
 
 	/**
 	 * The cipher instance used for de- and encryption
 	 */
 	private Cipher cipher;
 
 	/**
 	 * The encryption standard to use
 	 */
 	private String encryption = "AES";
 
 	/**
 	 * The maximum key length for the chosen {@link #encryption} standard
 	 */
 	private int keyLength;
 
 	/**
 	 * The users master password to decrypt the config
 	 */
 	private byte[] password;
 
 	/**
 	 * 
 	 */
 	private IPasswordManager passwordmgr = null;
 
 	/**
 	 * Contains the salts for configuration values that are stored encrypted
 	 */
 	private HashMap<String, String> salts = new HashMap<String, String>();
 
 	/**
 	 * Creates a Config object that stores the configuration that is stored in
 	 * the config file.
 	 */
 	public Config() {
 		Config.allowedCiphers.put("AES", 256);
 		Config.defaultData
 				.put("filesize.max", "" + Config.DEFAULT_FILESIZE_MAX);
 		Config.defaultData.put("merge.input.dir",
 				Config.DEFAULT_MERGE_INPUT_DIR);
 		Config.defaultData.put("merge.output.dir",
 				Config.DEFAULT_MERGE_OUTPUT_DIR);
 		Config.defaultData.put("split.input.dir",
 				Config.DEFAULT_SPLIT_INPUT_DIR);
 		Config.defaultData.put("split.output.dir",
 				Config.DEFAULT_SPLIT_OUTPUT_DIR);
 		Config.defaultData.put("upload.dir", Config.DEFAULT_UPLOAD_DIR);
 		Config.defaultData.put("upload.asynchronous", "true");
 		Config.defaultData.put("database.name", Config.DEFAULT_DATABASE_NAME);
 		Config.defaultData.put("filemanagement.count", ""
 				+ Config.DEFAULT_FILEMANAGEMENT_COUNT);
 		Config.defaultData.put("filemanagement.intervall", ""
 				+ Config.DEFAULT_FILEMANAGEMENT_INTERVALL);
 	}
 
 	@Override
 	public synchronized boolean delete() {
 		boolean res = false;
 		if (Config.CONFIG_FILE.exists()) {
 			res = Config.CONFIG_FILE.delete();
 			Config.CONFIG_FILE = null;
 		}
 		return res;
 	}
 
 	/**
 	 * Reads a setting from the settings list. If there is a salt available for
 	 * the certain key, we use it to decrypt the value. If any exception of the
 	 * kinds InvalidKeyException, IllegalBlockSizeException, BadPaddingException
 	 * or IOException is thrown during decryption, this function throws a
 	 * {@link InvalidConfigValueException}.
 	 * 
 	 * @param key
 	 *            The specific key of the configuration value
 	 * @return A string representation of the setting. If any exception occurs
 	 *         <code>null</code> is returned
 	 * @throws NoSuchElementException
 	 *             Thrown in case the key does not exists.
 	 * @throws NoSuchElementException
 	 *             Thrown if neither the given key nor a default value != null
 	 *             is found
 	 * @throws InvalidConfigValueException
 	 *             Thrown if the decryption process fails
 	 */
 	private synchronized String get(String key) throws NoSuchElementException,
 			InvalidConfigValueException {
 		if (!this.containsKey(key)) {
			throw new NoSuchElementException();
 		}
 		try {
 			if (this.salts.containsKey(key)) {
 				// We have to decrypt the value with the salt and the password
 				byte[] salt = this.salts.get(key).getBytes();
 				if (salt.length != this.keyLength) {
 					// the salt does not fulfill the requirements for this
 					// crypto algorithm
 					return null;
 				}
 				// we replace the first part of the salt by the user password
 				System.arraycopy(this.password, 0, salt, 0,
 						Math.min(this.password.length, salt.length));
 				SecretKeySpec skeySpec = new SecretKeySpec(salt,
 						this.encryption);
 				this.cipher.init(Cipher.DECRYPT_MODE, skeySpec);
 				byte[] value = Base64.decode(super.get(key));
 				String r = new String(this.cipher.doFinal(value));
 				return r;
 			} else {
 				// The content is available in plain text
 				String value = super.get(key).replace("&quot", "\"")
 						.replace("&lt;", "<").replace("&gt;", ">")
 						.replace("&amp;", "&");
 				return value;
 			}
 		} catch (InvalidKeyException e) {
 			e.printStackTrace();
 			throw new InvalidConfigValueException();
 		} catch (IllegalBlockSizeException e) {
 			e.printStackTrace();
 			throw new InvalidConfigValueException();
 		} catch (BadPaddingException e) {
 			e.printStackTrace();
 			throw new InvalidConfigValueException();
 		}
 	}
 
 	@Override
 	public synchronized boolean getBoolean(String key)
 			throws MissingConfigValueException {
 		try {
 			return this.getBoolean(key, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new MissingConfigValueException(e);
 		}
 	}
 
 	@Override
 	public synchronized boolean getBoolean(String key, Boolean defaultVal)
 			throws MissingConfigValueException, InvalidConfigValueException {
 		try {
 			String str = this.get(key);
 			return Boolean.parseBoolean(str);
 		} catch (NoSuchElementException e) {
 			if (defaultVal == null) {
 				if (!Config.defaultData.containsKey(key)) {
 					throw new MissingConfigValueException();
 				}
 				return Boolean.parseBoolean(Config.defaultData.get(key));
 			}
 			return defaultVal;
 		}
 	}
 
 	@Override
 	public synchronized String getCloudRAIDHome() {
 		return Config.CLOUDRAID_HOME;
 	}
 
 	@Override
 	public synchronized String getConfigPath() {
 		return Config.CONFIG_PATH;
 	}
 
 	@Override
 	public synchronized HashMap<String, String> getDefaultData() {
 		return Config.defaultData;
 	}
 
 	@Override
 	public synchronized double getDouble(String key)
 			throws MissingConfigValueException {
 		try {
 			return this.getDouble(key, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new MissingConfigValueException(e);
 		}
 	}
 
 	@Override
 	public synchronized double getDouble(String key, Double defaultVal)
 			throws MissingConfigValueException, InvalidConfigValueException {
 		try {
 			String str = this.get(key);
 			return Double.parseDouble(str);
 		} catch (NoSuchElementException e) {
 			if (defaultVal == null) {
 				if (!Config.defaultData.containsKey(key)) {
					throw new NoSuchElementException();
 				}
 				return Double.parseDouble(Config.defaultData.get(key));
 			}
 			return defaultVal;
 		}
 	}
 
 	@Override
 	public synchronized float getFloat(String key)
 			throws MissingConfigValueException {
 		try {
 			return this.getFloat(key, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new MissingConfigValueException(e);
 		}
 	}
 
 	@Override
 	public synchronized float getFloat(String key, Float defaultVal)
 			throws MissingConfigValueException, InvalidConfigValueException {
 		try {
 			String str = this.get(key);
 			return Float.parseFloat(str);
 		} catch (NoSuchElementException e) {
 			if (defaultVal == null) {
 				if (!Config.defaultData.containsKey(key)) {
					throw new NoSuchElementException();
 				}
 				return Float.parseFloat(Config.defaultData.get(key));
 			}
 			return defaultVal;
 		}
 	}
 
 	@Override
 	public synchronized int getInt(String key)
 			throws MissingConfigValueException {
 		try {
 			return this.getInt(key, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new MissingConfigValueException(e);
 		}
 	}
 
 	@Override
 	public synchronized int getInt(String key, Integer defaultVal)
 			throws MissingConfigValueException, InvalidConfigValueException {
 		try {
 			String str = this.get(key);
 			return Integer.parseInt(str);
 		} catch (NoSuchElementException e) {
 			if (defaultVal == null) {
 				if (!Config.defaultData.containsKey(key)) {
					throw new NoSuchElementException();
 				}
 				return Integer.parseInt(Config.defaultData.get(key));
 			}
 			return defaultVal;
 		}
 	}
 
 	@Override
 	public synchronized long getLong(String key)
 			throws MissingConfigValueException {
 		try {
 			return this.getLong(key, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new MissingConfigValueException(e);
 		}
 	}
 
 	@Override
 	public synchronized long getLong(String key, Long defaultVal)
 			throws NoSuchElementException, InvalidConfigValueException {
 		try {
 			String str = this.get(key);
 			return Long.parseLong(str);
 		} catch (NoSuchElementException e) {
 			if (defaultVal == null) {
 				if (!Config.defaultData.containsKey(key)) {
					throw new NoSuchElementException();
 				}
 				return Long.parseLong(Config.defaultData.get(key));
 
 			}
 			return defaultVal;
 		}
 	}
 
 	@Override
 	public synchronized String getString(String key)
 			throws MissingConfigValueException {
 		try {
 			return this.getString(key, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new MissingConfigValueException(e);
 		}
 	}
 
 	@Override
 	public synchronized String getString(String key, String defaultVal)
 			throws NoSuchElementException, InvalidConfigValueException {
 		try {
 			String str = this.get(key);
 			return str;
 		} catch (NoSuchElementException e) {
 			if (defaultVal == null) {
 				if (!Config.defaultData.containsKey(key)) {
					throw new NoSuchElementException();
 				}
 				return Config.defaultData.get(key);
 
 			}
 			return defaultVal;
 		}
 	}
 
 	/**
 	 * This function generates a salt that does not contain any characters that
 	 * might break the XML context
 	 * 
 	 * @param length
 	 *            The length that the salt should have
 	 * @return Returns a salt as byte array of length <code>length</code>
 	 */
 	private synchronized byte[] getXMLSaveSalt(int length) {
 		byte[] b = new byte[length];
 		byte[] allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#-_+"
 				.getBytes();
 		int l = allowedChars.length;
 		int i;
 		for (i = 0; i < length; i++) {
 			b[i] = allowedChars[Config.r.nextInt(l)];
 		}
 		return b;
 	}
 
 	@Override
 	public synchronized ICloudRAIDConfig init(String password) {
 		this.password = password.getBytes();
 
 		try {
 			this.cipher = Cipher.getInstance(this.encryption);
 			// TODO this might return Integer.MAX_VALUE. See
 			// http://docs.oracle.com/javase/6/docs/api/javax/crypto/Cipher.html#getMaxAllowedKeyLength(java.lang.String)
 			// this.keyLength = Cipher.getMaxAllowedKeyLength(this.encryption);
 			this.keyLength = Config.allowedCiphers.get(this.encryption) / 8;
 		} catch (NoSuchAlgorithmException e1) {
 			e1.printStackTrace();
 		} catch (NoSuchPaddingException e1) {
 			e1.printStackTrace();
 		}
 
 		return reload();
 	}
 
 	/**
 	 * Automatically detects whether the chosen algorithm is available
 	 * 
 	 * @param algorithm
 	 *            The encryption standard to check
 	 * @return <code>true</code> if the algorithm exists, otherwise
 	 *         <code>false</code>.
 	 */
 	private synchronized boolean isAlgorithmsAvailable(String algorithm) {
 		// TODO automatically detect if the chosen algorithm is available
 
 		// Provider[] providers = Security.getProviders();
 		// ArrayList<String> ciphers = new ArrayList<String>();
 		// String algo;
 		// for (int i = 0; i != providers.length; i++) {
 		// Iterator<Object> it = providers[i].keySet().iterator();
 		// while (it.hasNext()) {
 		// String entry = (String) it.next();
 		// if (entry.startsWith("Alg.Alias.Cipher.")) {
 		// algo = entry.substring("Alg.Alias.Cipher.".length());
 		// if (algo.indexOf(" ") == -1) {
 		// // The algorithm does not contain a whitespace
 		// ciphers.add(algo);
 		// }
 		// }
 		// }
 		// }
 		// return ciphers.contains(algorithm);
 
 		return Config.allowedCiphers.keySet().contains(algorithm);
 	}
 
 	@Override
 	public synchronized boolean keyExists(String key) {
 		return super.keySet().contains(key);
 	}
 
 	@Override
 	public synchronized void put(String key, boolean value) {
 		this.put(key, String.valueOf(value), false);
 	}
 
 	@Override
 	public synchronized void put(String key, boolean value, boolean encrypted) {
 		this.put(key, String.valueOf(value), encrypted);
 	}
 
 	@Override
 	public synchronized void put(String key, double value) {
 		this.put(key, String.valueOf(value), false);
 	}
 
 	@Override
 	public synchronized void put(String key, double value, boolean encrypted) {
 		this.put(key, String.valueOf(value), encrypted);
 	}
 
 	@Override
 	public synchronized void put(String key, float value) {
 		this.put(key, String.valueOf(value), false);
 	}
 
 	@Override
 	public synchronized void put(String key, float value, boolean encrypted) {
 		this.put(key, String.valueOf(value), encrypted);
 	}
 
 	@Override
 	public synchronized void put(String key, int value) {
 		this.put(key, String.valueOf(value), false);
 	}
 
 	@Override
 	public synchronized void put(String key, int value, boolean encrypted) {
 		this.put(key, String.valueOf(value), encrypted);
 	}
 
 	@Override
 	public synchronized void put(String key, long value) {
 		this.put(key, String.valueOf(value), false);
 	}
 
 	@Override
 	public synchronized void put(String key, long value, boolean encrypted) {
 		this.put(key, String.valueOf(value), encrypted);
 	}
 
 	@Override
 	public synchronized String put(String key, String value) {
 		return this.put(key, value, false);
 	}
 
 	@Override
 	public synchronized String put(String key, String value, boolean encrypted) {
 
 		if (encrypted) {
 			// We have to decrypt the value with the salt and the password
 			byte[] salt = getXMLSaveSalt(this.keyLength);
 			byte[] salt2 = new byte[salt.length];
 			System.arraycopy(salt, 0, salt2, 0, salt.length);
 
 			// we replace the first part of the salt by the user password
 			System.arraycopy(this.password, 0, salt2, 0,
 					Math.min(this.password.length, salt2.length));
 			SecretKeySpec skeySpec = new SecretKeySpec(salt2, this.encryption);
 
 			// If we run into a encryption error, we will store the value in
 			// plain text!
 			String v = Base64.encodeToString(value.getBytes(), false);
 			try {
 				// All actions after encoding the encrypted byte array are
 				// expected to work. But if something fails after adding the
 				// salt to the global salt list, we will remove that later.
 				this.cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
 				v = Base64.encodeToString(
 						this.cipher.doFinal(value.getBytes()), false);
 				this.salts.put(key, new String(salt));
 				return super.put(key, v);
 			} catch (InvalidKeyException e) {
 				e.printStackTrace();
 			} catch (IllegalBlockSizeException e) {
 				e.printStackTrace();
 			} catch (BadPaddingException e) {
 				e.printStackTrace();
 			}
 
 		}
 		if (this.salts.containsKey(key)) {
 			this.salts.remove(key);
 		}
 		return super.put(key, value.replace("&", "&amp;").replace("<", "&lt;")
 				.replace(">", "&gt;").replace("\"", "&quot;"));
 	}
 
 	@Override
 	public synchronized ICloudRAIDConfig reload() {
 		this.clear();
 		this.salts.clear();
 
 		setConfigPath(Config.CONFIG_PATH);
 
 		if (!Config.CONFIG_FILE.exists()) {
 			return this;
 		}
 
 		// Create DocumentBuilder for XML files.
 		DocumentBuilder docBuilder = null;
 		try {
 			docBuilder = DocumentBuilderFactory.newInstance()
 					.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 			return null;
 		}
 		docBuilder.setErrorHandler(null);
 
 		// Build the XML tree.
 		Document doc;
 		try {
 			doc = docBuilder.parse(Config.CONFIG_FILE);
 		} catch (SAXException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		// we read the encryption algorithm from the config file. If and only if
 		// it is given and it is an allowed cipher, we use it. Otherwize we set
 		// it to a default value <i>AES</i>!
 		this.encryption = doc.getDocumentElement().getAttribute("encryption");
 		if (this.encryption.isEmpty()
 				|| !isAlgorithmsAvailable(this.encryption)) {
 			this.encryption = "AES";
 		}
 
 		try {
 			byte[] integr_hash = Base64.decode(doc.getDocumentElement()
 					.getAttribute("hash"));
 			byte[] integr_salt = Base64.decode(doc.getDocumentElement()
 					.getAttribute("salt"));
 			if (integr_hash.length > 0 && integr_salt.length > 0) {
 				byte[] cryptinput = new byte[this.keyLength];
 				System.arraycopy(integr_salt, 0, cryptinput, 0, this.keyLength);
 				System.arraycopy(this.password, 0, cryptinput, 0,
 						Math.min(this.password.length, cryptinput.length));
 
 				SecretKeySpec skeySpec = new SecretKeySpec(integr_salt,
 						this.encryption);
 				this.cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
 				byte[] cryptoutput = this.cipher.doFinal(cryptinput);
 
 				MessageDigest digest = MessageDigest.getInstance("SHA-256");
 				digest.reset();
 				byte[] digestoutput = digest.digest(cryptoutput);
 				if (!Arrays.equals(digestoutput, integr_hash)) {
 					return null;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		// Iterate over all "entry" nodes and collect the information.
 		NodeList list = doc.getDocumentElement().getElementsByTagName("entry");
 		for (int i = 0; i < list.getLength(); i++) {
 			Element node = (Element) list.item(i);
 			this.put(node.getAttribute("name").trim(), node.getTextContent()
 					.trim());
 			// If there is a salt stored, store it in an extra HashMap.
 			if (node.hasAttribute("salt")) {
 				this.salts.put(node.getAttribute("name").trim(), node
 						.getAttribute("salt").trim());
 			}
 		}
 		return this;
 	}
 
 	@Override
 	public synchronized String remove(Object key) {
 		this.salts.remove(key);
 		return super.remove(key);
 	}
 
 	@Override
 	public synchronized void save() {
 		try {
 			if (!Config.CONFIG_FILE.getParentFile().exists()) {
 				Config.CONFIG_FILE.getParentFile().mkdirs();
 			}
 
 			// Create the output writer.
 			BufferedWriter writer = new BufferedWriter(new FileWriter(
 					Config.CONFIG_PATH));
 
 			String intgrsalt = "";
 			String intgrhash = "";
 			try {
 				byte[] integr_salt = getXMLSaveSalt(this.keyLength);
 				byte[] cryptinput = new byte[this.keyLength];
 				System.arraycopy(integr_salt, 0, cryptinput, 0, this.keyLength);
 				System.arraycopy(this.password, 0, cryptinput, 0,
 						Math.min(this.password.length, cryptinput.length));
 
 				SecretKeySpec skeySpec = new SecretKeySpec(integr_salt,
 						this.encryption);
 				this.cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
 				byte[] cryptoutput = this.cipher.doFinal(cryptinput);
 
 				MessageDigest digest = MessageDigest.getInstance("SHA-256");
 				digest.reset();
 				intgrhash = Base64.encodeToString(digest.digest(cryptoutput),
 						false);
 				intgrsalt = Base64.encodeToString(integr_salt, false);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			// Write standard conform XML code.
 			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 			writer.newLine();
 			if (intgrhash.isEmpty() || intgrsalt.isEmpty()) {
 				writer.write("<root encryption=\"" + this.encryption + "\">");
 			} else {
 				writer.write("<root encryption=\"" + this.encryption
 						+ "\" salt=\"" + intgrsalt + "\" hash=\"" + intgrhash
 						+ "\">");
 			}
 			Set<String> keys = this.keySet();
 			for (String k : keys) {
 				writer.newLine();
 				String saltString = this.salts.get(k);
 				String salt = saltString == null || saltString.equals("") ? ""
 						: " salt=\"" + saltString + "\"";
 				writer.write("\t<entry name=\"" + k + "\"" + salt + ">"
 						+ super.get(k) + "</entry>");
 			}
 			writer.newLine();
 			writer.write("</root>");
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public synchronized void setCloudRAIDHome(String path) {
 		if (path.endsWith(File.separator)) {
 			Config.CLOUDRAID_HOME = path;
 		} else {
 			Config.CLOUDRAID_HOME = path + File.separator;
 		}
 		setConfigPath(Config.CLOUDRAID_HOME + "config.xml");
 	}
 
 	@Override
 	public synchronized void setConfigPath(String path) {
 		Config.CONFIG_PATH = path;
 		Config.CONFIG_FILE = new File(Config.CONFIG_PATH);
 	}
 
 	/**
 	 * @param passwordManager
 	 */
 	protected synchronized void setPasswordManager(
 			IPasswordManager passwordManager) {
 		System.out.println("Config: setPasswordManager: begin");
 		this.passwordmgr = passwordManager;
 		System.out.println("Config: setPasswordManager: " + this.passwordmgr);
 		System.out.println("Config: setPasswordManager: end");
 	}
 
 	/**
 	 * 
 	 */
 	protected void shutdown() {
 		System.out.println("Config: shutdown: begin");
 		System.out.println("Config: shutdown: end");
 	}
 
 	/**
 	 * @throws InvalidKeyException
 	 * 
 	 */
 	protected void startup(BundleContext context) throws InvalidKeyException {
 		System.out.println("Config: startup: begin");
 		this.doSaveOnShutdown = true;
 		if (this.init(this.passwordmgr.getCredentials()) == null) {
 			this.doSaveOnShutdown = false;
 			throw new InvalidKeyException(
 					"The provided key for the configuration is invalid!");
 		}
 		System.out.println("Config: startup: end");
 	}
 
 	/**
 	 * @param passwordManager
 	 */
 	protected synchronized void unsetPasswordManager(
 			IPasswordManager passwordManager) {
 		System.out.println("Config: unsetPasswordManager: begin");
 		System.out.println("Config: unsetPasswordManager: " + passwordManager);
 		if (this.doSaveOnShutdown) {
 			this.save();
 		}
 		this.passwordmgr = null;
 		System.out.println("Config: unsetPasswordManager: " + this.passwordmgr);
 		System.out.println("Config: unsetPasswordManager: end");
 	}
 }
