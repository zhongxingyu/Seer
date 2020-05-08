 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.internal.target;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.SecureRandom;
 import java.util.Date;
 import java.util.Properties;
 
 import org.zend.sdklib.internal.utils.EnvironmentUtils;
 import org.zend.sdklib.target.IZendTarget;
 
 import com.ice.jni.registry.NoSuchKeyException;
 import com.ice.jni.registry.Registry;
 import com.ice.jni.registry.RegistryException;
 import com.ice.jni.registry.RegistryKey;
 
 /**
  * Auto detect local server
  * 
  * @author Roy, 2011
  */
 public class ZendTargetAutoDetect {
 
 	private static final String INSTALL_LOCATION = "InstallLocation";
 	private static final String USER_INI = "zend-server-user.ini";
 	private static final String NEED_TO_ELEVATE = "You need root privileges to run this script!";
 	private static final String MISSING_ZEND_SERVER = "Local Zend Server couldn't be found, "
 			+ "please refer to http://www.zend.com/server";
 
 	// linux key
 	private static final String CONFIG_FILE_LINUX = "/etc/zce.rc"; //$NON-NLS-1$
 	private static final String CONFIG_FILE_LINUX_DEB = "/etc/zce.rc-deb"; //$NON-NLS-1$
 	private static final String CONFIG_FILE_LINUX_RPM = "/etc/zce.rc-rpm"; //$NON-NLS-1$
 	private static final String ZCE_PREFIX = "ZCE_PREFIX";
 
 	// Registry
 	private static final String NODE_64 = "WOW6432node";;
 	private static final String ZEND_SERVER = "ZendServer";
 	private static final String ZEND_TECHNOLOGIES = "Zend Technologies";
 
 	public static URL localhost = null;
 	private String zendServerInstallLocation = null;
 	static {
 		try {
 			localhost = new URL("http://localhost");
 		} catch (MalformedURLException e) {
 			// ignore localhost is a valid URL
 		}
 	}
 
 	/**
 	 * 
 	 * @param targetId
 	 * @param key
 	 * @return
 	 * @throws IOException
 	 */
 	public IZendTarget createLocalhostTarget(String targetId, String key)
 			throws IOException {
 		findLocalhostInstallDirectory();
 		if (zendServerInstallLocation == null) {
 			throw new IllegalStateException(MISSING_ZEND_SERVER);
 		}
 
 		String secretKey = retrieveSecretKey(key);
 		return new ZendTarget(targetId, localhost, key, secretKey);
 	}
 
 	private String retrieveSecretKey(String key) throws IOException,
 			FileNotFoundException {
 		String secretKey = findSecretKeyInLocalhost(key);
 		if (secretKey == null) {
 			// assert permissions are elevated
 			File keysFile = getApiKeysFile();
 			if (keysFile == null) {
 				throw new IllegalStateException(MISSING_ZEND_SERVER);
 			}
 
 			if (!keysFile.canWrite()) {
 				// "Permission denied"
 				throw new IOException(NEED_TO_ELEVATE);
 			}
 
 			// write zend-server-users.ini and find key
 			BufferedReader ir = new BufferedReader(new FileReader(keysFile));
 			final File edited = new File(keysFile.getParentFile(), USER_INI
 					+ ".tmp");
 			PrintStream os = new PrintStream(edited);
 			secretKey = copyWithEdits(ir, os, key);
 			ir.close();
 			os.close();
 			String oldKeysFilePath = keysFile.getAbsolutePath();
 			keysFile.renameTo(new File(keysFile.getParentFile(), USER_INI
 					+ ".bak"));
 			File oldFile = new File(oldKeysFilePath);
 			if (oldFile.exists()) {
 				oldFile.delete();
 			}
 			edited.renameTo(new File(edited.getParentFile(), USER_INI));
 		}
 		if (secretKey.startsWith("\"")) {
 			secretKey = secretKey.substring(1, secretKey.length() - 1);
 		}
 		return secretKey;
 	}
 
 	public static String copyWithEdits(BufferedReader ir, PrintStream os,
 			String key) throws IOException {
 		String line = ir.readLine();
 
 		final String sk = generateSecretKey();
 		boolean block = false;
 		while (line != null) {
 			if ("[apiKeys]".equals(line)) {
 				writeApiKeyBlock(key, os, sk);
 				block = true;
 			} else {
 				os.println(line);
 			}
 			line = ir.readLine();
 		}
 
 		if (!block) {
 			writeApiKeyBlock(key, os, sk);
 		}
 
 		return sk;
 
 	}
 
 	public static Properties readApiKeysSection(BufferedReader reader)
 			throws IOException {
 
 		Properties properties = new Properties();
 		String line = reader.readLine();
 		while (line != null) {
 			if ("[apiKeys]".equals(line)) {
 				line = reader.readLine();
 				while (line != null && !line.startsWith("[")) {
 					final String[] split = line.split("=");
 					if (split != null && split.length == 2) {
 						properties.put(split[0].trim(), split[1].trim());
 					}
 					line = reader.readLine();
 				}
 			}
 			line = reader.readLine();
 		}
 
 		return properties;
 	}
 
 	/**
 	 * Returns a new target for the localhost target
 	 * 
 	 * @param key
 	 * @return
 	 * @throws IOException
 	 */
 	private String findSecretKeyInLocalhost(String key) throws IOException {
 		findLocalhostInstallDirectory();
 		if (zendServerInstallLocation == null) {
 			throw new IllegalStateException(MISSING_ZEND_SERVER);
 		}
 
 		// assert permissions are elevated
 		File keysFile = getApiKeysFile();
 		if (keysFile == null) {
 			throw new IllegalStateException(MISSING_ZEND_SERVER);
 		}
 
 		if (!keysFile.canRead()) {
 			// "Permission denied"
 			throw new IOException(NEED_TO_ELEVATE);
 		}
 
 		// read zend-server-users.ini and find key
 		final BufferedReader reader = new BufferedReader(new FileReader(
 				keysFile));
 		Properties p = readApiKeysSection(reader);
 		reader.close();
 		final String hash = p.getProperty(key + ":hash");
 		if (hash != null) {
 			// return secretKey if possible
 			return hash;
 		}
 
 		// key not found
 		return null;
 	}
 
 	/**
 	 * @return returns the Local installed Zend Server, null if no local Zend
 	 *         Server installed.
 	 * @throws IOException
 	 */
 	private void findLocalhostInstallDirectory() throws IOException {
 		if (EnvironmentUtils.isUnderLinux() || EnvironmentUtils.isUnderMaxOSX()) {
 			zendServerInstallLocation = getLocalZendServerFromFile();
 		}
 
 		if (EnvironmentUtils.isUnderWindows()) {
 			zendServerInstallLocation = getLocalZendServerFromRegistry();
 		}
 	}
 
 	private String getLocalZendServerFromFile() {
 		Properties props = null;
 
 		// Try to find the zend.rc-deb file.
 		try {
 			FileInputStream fileStream = new FileInputStream(
 					CONFIG_FILE_LINUX_DEB);
 			props = new Properties();
 			props.load(fileStream);
 		} catch (FileNotFoundException e) {
 		} catch (IOException e) {
 		}
 
 		// If not found, find the zend.rc-rpm file.
 		if (props == null) {
 			try {
 				FileInputStream fileStream = new FileInputStream(
 						CONFIG_FILE_LINUX_RPM);
 				props = new Properties();
 				props.load(fileStream);
 			} catch (FileNotFoundException e) {
 			} catch (IOException e) {
 			}
 		}
 
 		// if not found, find the zend.rc file.
 		if (props == null) {
 			try {
 				FileInputStream fileStream = new FileInputStream(
 						CONFIG_FILE_LINUX);
 				props = new Properties();
 				props.load(fileStream);
 			} catch (FileNotFoundException e) {
 			} catch (IOException e) {
 			}
 		}
 
 		return props != null ? props.getProperty(ZCE_PREFIX) : null;
 	}
 
 	private String getLocalZendServerFromRegistry() throws IOException {
 		RegistryKey zendServerKey = null;
 		try {
 			zendServerKey = Registry.HKEY_LOCAL_MACHINE.openSubKey("SOFTWARE")
 					.openSubKey(ZEND_TECHNOLOGIES).openSubKey(ZEND_SERVER);
 			return zendServerKey.getStringValue(INSTALL_LOCATION);
 		} catch (NoSuchKeyException e1) {
 			// try the 64 bit
 
 			try {
 				zendServerKey = Registry.HKEY_LOCAL_MACHINE.openSubKey("SOFTWARE")
 						.openSubKey(NODE_64).openSubKey(ZEND_TECHNOLOGIES)
 						.openSubKey(ZEND_SERVER);
 				return zendServerKey.getStringValue(INSTALL_LOCATION);
 			} catch (NoSuchKeyException e) {
 			} catch (RegistryException e) {
 			}
 		} catch (RegistryException e1) {
 		}
 		return null;
 	}
 
 	private static void writeApiKeyBlock(String key, PrintStream os,
 			final String sk) {
 		os.println("[apiKeys]");
 
 		// roy:name = "roy"
 		os.print(key);
 		os.print(":name = \"");
 		os.print(key);
 		os.println("\"");
 
 		// roy:creationTime = 1304968104
 		os.print(key);
 		os.print(":creationTime = ");
 		os.println(new Date().getTime()/1000);
 
 		// roy:hash = "c86ba2bc5fb62ee916031cf78..."
 		os.print(key);
 		os.print(":hash = \"");
 		os.print(sk);
 		os.println("\"");
 
 		// roy:role = "fullAccess"
 		os.print(key);
 		os.println(":role = \"fullAccess\"");
 
 		os.println();
 	}
 
 	private File getApiKeysFile() {
 		if (EnvironmentUtils.isUnderLinux() || EnvironmentUtils.isUnderMaxOSX()) {
 			return new File(zendServerInstallLocation
 					+ "/gui/application/data/zend-server-user.ini");
 		} else if (EnvironmentUtils.isUnderWindows()) {
 			return new File(zendServerInstallLocation
 					+ "ZendServer\\GUI\\application\\data\\zend-server-user.ini");
 		}
 
 		return null;
 	}
 
 	private static String generateSecretKey() {
 		SecureRandom random = new SecureRandom();
		return new BigInteger(256, random).toString(16);
 	}
 }
