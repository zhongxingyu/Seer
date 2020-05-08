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
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.SecureRandom;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 
 import org.zend.sdklib.SdkException;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.IZendTarget;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelSftp;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.SftpException;
 import com.openshift.client.IApplication;
 import com.openshift.client.IApplicationPortForwarding;
 import com.openshift.client.ICartridge;
 import com.openshift.client.IDomain;
 import com.openshift.client.IEmbeddedCartridge;
 import com.openshift.client.IGearProfile;
 import com.openshift.client.IOpenShiftConnection;
 import com.openshift.client.IOpenShiftSSHKey;
 import com.openshift.client.ISSHPublicKey;
 import com.openshift.client.IUser;
 import com.openshift.client.OpenShiftConnectionFactory;
 import com.openshift.client.OpenShiftEndpointException;
 import com.openshift.client.OpenShiftException;
 import com.openshift.client.SSHPublicKey;
 import com.openshift.internal.client.Cartridge;
 import com.openshift.internal.client.EmbeddableCartridge;
 import com.openshift.internal.client.GearProfile;
 import com.openshift.internal.client.response.Message;
 
 /**
  * Represents OpenShift user account. It allows to detects OpenShift
  * applications which have Zend Server support and add them as a Zend Server
  * targets.
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public class OpenShiftTarget {
 
 	public static final String LIBRA_SERVER_PROP = "org.zend.sdk.openshift.libraServer";
 	public static final String LIBRA_DOMAIN_PROP = "org.zend.sdk.openshift.libraDomain";
 
 	public static final String DEFAULT_LIBRA_SERVER = "https://openshift.redhat.com";
 	public static final String DEFAULT_LIBRA_DOMAIN = "rhcloud.com";
 
 	public static final String TARGET_CONTAINER = "openshift.container";
 	public static final String TARGET_USERNAME = "openshift.username";
 	public static final String TARGET_UUID = "openshift.uuid";
 	public static final String TARGET_INTERNAL_HOST = "openshift.internalHost";
 	public static final String TARGET_MYSQL_SUPPORT = "openshift.mysql";
 	public static final String SSH_PRIVATE_KEY_PATH = "ssh-private-key";
 
 	private static final String SSHKEY_DEFAULT_NAME = "zendStudio";
 	private static final String CARTRIDGE_NAME = "zend-5.6";
 	private static final String DEFAULT_KEY_NAME = "ZendStudioClient";
 	private static final String USER_INI = "zend-server-user.ini";
 	private static final String ZEND_SERVER_USER_INI_PATH = "zend-5.6/gui/application/data/zend-server-user.ini";
 
 	private String username;
 	private String password;
 
 	private IDomain domain;
 
 	public OpenShiftTarget(String username, String password) {
 		super();
 		this.username = username;
 		this.password = password;
 	}
 
 	/**
 	 * Detects OpenShift applications which have Zend Server support.
 	 * 
 	 * @param privateKey
 	 * @param manager
 	 * @param keyBuilder
 	 * @return array of detected Zend Targets
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 * @throws SdkException
 	 */
 	public IZendTarget[] detectTargets(String privateKey,
 			TargetsManager manager, PublicKeyBuilder keyBuilder)
 			throws FileNotFoundException, IOException, SdkException {
 		try {
 			List<IApplication> containers = getZendContainers();
 			if (containers.size() > 0) {
 				// process containers
 				return createTargets(containers, privateKey, manager,
 						keyBuilder);
 			}
 		} catch (OpenShiftException e) {
 			throw new SdkException(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Returns path to public key for provided private key.
 	 * 
 	 * @param privateKey
 	 * @param keyBuilder
 	 * @return
 	 * @throws PublicKeyNotFoundException
 	 */
 	public String getPublicKeyPath(String privateKey,
 			PublicKeyBuilder keyBuilder) throws PublicKeyNotFoundException {
 		if (keyBuilder == null) {
 			throw new PublicKeyNotFoundException(
 					"Not able to create public key.");
 		}
 		return keyBuilder.getPublicKey(privateKey);
 	}
 
 	/**
 	 * Uploads public key to OpenShift account.
 	 * 
 	 * @param privateKey
 	 * @param keyBuilder
 	 * @return key's name
 	 * @throws SdkException
 	 */
 	public String uploadPublicKey(String privateKey, PublicKeyBuilder keyBuilder)
 			throws SdkException {
 		try {
 			IOpenShiftConnection connection = getConnection();
 			IUser user = connection.getUser();
 			ISSHPublicKey sshKey = loadSshKey(privateKey, keyBuilder);
 			IOpenShiftSSHKey sshKeyResource = user.getSSHKeyByPublicKey(sshKey
 					.getPublicKey());
 			if (sshKeyResource == null) {
 				String name = getTimestampKeyname();
 				user.putSSHKey(name, sshKey);
 				return name;
 			}
 		} catch (OpenShiftException e) {
 			throw new SdkException(e.getMessage());
 		} catch (IOException e) {
 			throw new SdkException(e);
 		}
 		return null;
 	}
 
 	public List<String> getAvaliableGearProfiles() throws SdkException {
 		try {
 			IDomain d = getDomain();
 			List<String> result = new ArrayList<String>();
 			List<IGearProfile> gearProfiles = d.getAvailableGearProfiles();
 			for (IGearProfile profile : gearProfiles) {
 				result.add(profile.getName());
 			}
 			return result;
 		} catch (OpenShiftException e) {
 			throw new SdkException(e);
 		}
 	}
 
 	public boolean hasDomain() throws SdkException {
 		try {
 			IOpenShiftConnection connection = null;
 			try {
 				connection = getConnection();
 			} catch (IOException e) {
 				throw new SdkException(e);
 			}
 			IUser user = connection.getUser();
 			List<IDomain> domains = user.getDomains();
 			if (domains == null || domains.size() == 0) {
 				return false;
 			}
 		} catch (OpenShiftException e) {
 			throw new SdkException(e);
 		}
 		return true;
 	}
 
 	public List<String> getAllZendTargets() throws SdkException {
 		List<String> result = new ArrayList<String>();
 		List<IApplication> targets = getZendContainers();
 		if (targets != null) {
 			for (IApplication app : targets) {
 				result.add(app.getName());
 			}
 		}
 		return result;
 	}
 
 	public void createDomain(String domainName) throws SdkException {
 		IOpenShiftConnection connection = null;
 		try {
 			connection = getConnection();
 		} catch (IOException e) {
 			throw new SdkException(e);
 		}
 		IUser user = connection.getUser();
 		user.createDomain(domainName);
 	}
 
 	public String create(String targetName, String gearProfile, boolean mySql)
 			throws SdkException {
 		IDomain d = getDomain();
 		if (d != null) {
 			IApplication application = d
 					.createApplication(targetName,
 							new Cartridge(CARTRIDGE_NAME), new GearProfile(
 									gearProfile));
 			if (application != null && mySql) {
 				IEmbeddedCartridge cartridge = application
 						.addEmbeddableCartridge(new EmbeddableCartridge(
 								"mysql-5.1"));
 				return cartridge.getCreationLog();
 			}
 		}
 		return null;
 	}
 
 	public static boolean hasDatabaseSupport(IZendTarget target) {
 		return target.getProperty(TARGET_MYSQL_SUPPORT) != null;
 	}
 
 	public static void iniLibraServer(String value) {
 		String val = System.getProperty(LIBRA_SERVER_PROP);
 		if (val == null) {
 			setLibraServer(value);
 		}
 	}
 	
 	public static void iniLibraDomain(String value) {
 		String val = System.getProperty(LIBRA_DOMAIN_PROP);
 		if (val == null) {
 			setLibraDomain(value);
 		}
 	}
 
 	public static String getLibraServer() {
 		String val = System.getProperty(LIBRA_SERVER_PROP);
 		return val != null ? val : DEFAULT_LIBRA_SERVER;
 	}
 
 	public static String getLibraDomain() {
 		String val = System.getProperty(LIBRA_DOMAIN_PROP);
 		return val != null ? val : DEFAULT_LIBRA_DOMAIN;
 	}
 
 	public static String getDefaultLibraServer() {
 		return DEFAULT_LIBRA_SERVER;
 	}
 
 	public static String getDefaultLibraDomain() {
 		return DEFAULT_LIBRA_DOMAIN;
 	}
 
 	public static void setLibraServer(String serverURL) {
 		System.setProperty(LIBRA_SERVER_PROP, serverURL);
 	}
 
 	public static void setLibraDomain(String domain) {
 		System.setProperty(LIBRA_DOMAIN_PROP, domain);
 	}
 
 	public static String getOpenShiftMessage(Throwable throwable) {
 		if (throwable instanceof OpenShiftEndpointException) {
 			OpenShiftEndpointException e = (OpenShiftEndpointException) throwable;
 			List<Message> messages = e.getRestResponseMessages();
 			StringBuilder result = new StringBuilder();
 			for (Message message : messages) {
 				result.append(message.getText());
 				result.append(". ");
 			}
 			return result.toString();
 		}
 		return throwable.getMessage();
 	}
 
 	public String getDomainName() throws SdkException {
 		IDomain domain = getDomain();
 		if (domain != null) {
 			return domain.getId();
 		}
 		return null;
 	}
 
 	private IDomain getDomain() throws SdkException {
 		if (domain == null) {
 			IOpenShiftConnection connection = null;
 			try {
 				connection = getConnection();
 			} catch (OpenShiftException e) {
 				throw new SdkException(e);
 			} catch (IOException e) {
 				throw new SdkException(e);
 			}
 			IUser user = connection.getUser();
 			List<IDomain> domains = user.getDomains();
 			if (domains == null || domains.isEmpty()) {
 				throw new SdkException(
 						"Domain has not been created yet. "
								+ "You can create new domain by using 'Create new OpenShift Target' button below.");
 			}
 			if (domains != null && domains.size() > 0) {
 				for (IDomain domain : domains) {
 					this.domain = domain;
 					break;
 				}
 			}
 		}
 		return domain;
 	}
 
 	private String getInternalHost(IApplication container, ZendTarget target,
 			PublicKeyBuilder keyBuilder) throws SdkException {
 		Session session = null;
 		try {
 			session = createSession(target, keyBuilder);
 			session.connect();
 			if (session.isConnected()) {
 				container.setSSHSession(session);
 				List<IApplicationPortForwarding> portForwarding = container
 						.getForwardablePorts();
 				if (portForwarding != null) {
 					for (IApplicationPortForwarding forwarding : portForwarding) {
 						return forwarding.getRemoteAddress();
 					}
 				}
 			}
 		} catch (OpenShiftException e) {
 			throw new SdkException(e);
 		} catch (IOException e) {
 			throw new SdkException(e);
 		} catch (JSchException e) {
 			throw new SdkException(e);
 		} finally {
 			if (session != null && session.isConnected()) {
 				session.disconnect();
 			}
 		}
 		return null;
 	}
 
 	private Session createSession(IZendTarget target,
 			PublicKeyBuilder keyBuilder) throws IOException {
 		JSch jsch = new JSch();
 		try {
 			Session session = jsch.getSession(target.getProperty(TARGET_UUID),
 					target.getDefaultServerURL().getHost(), 22);
 			session.setConfig("StrictHostKeyChecking", "no"); //$NON-NLS-1$ //$NON-NLS-2$
 			String privateKey = target.getProperty(SSH_PRIVATE_KEY_PATH);
 			String passphrase = keyBuilder.getPassphase(privateKey);
 			if (passphrase != null && passphrase.length() > 0) {
 				jsch.addIdentity(privateKey, passphrase);
 			} else {
 				jsch.addIdentity(privateKey);
 			}
 			return session;
 		} catch (JSchException e) {
 			throw new IOException(e);
 		} catch (PublicKeyNotFoundException e) {
 			throw new IOException(e);
 		}
 	}
 
 	private String findExistingSecretKey(String key, File keysFile)
 			throws IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(keysFile));
 		Properties p = ZendTargetAutoDetect.readApiKeysSection(reader);
 		reader.close();
 		String hash = p.getProperty(key + ":hash");
 		if (hash != null) {
 			return trimQuotes(hash);
 		}
 		return null;
 	}
 
 	private String trimQuotes(final String hash) {
 		return hash.startsWith("\"") ? hash.substring(1, hash.length() - 1)
 				: hash;
 	}
 
 	private String getTimestampKeyname() {
 		return new StringBuilder(SSHKEY_DEFAULT_NAME).append(
 				new SimpleDateFormat("yyyyMMddhmS").format(new Date()))
 				.toString();
 	}
 
 	private IZendTarget[] createTargets(List<IApplication> containers,
 			String privateKey, TargetsManager manager,
 			PublicKeyBuilder keyBuilder) throws MalformedURLException,
 			SdkException {
 		List<IZendTarget> result = new ArrayList<IZendTarget>(containers.size());
 		String uniqueId = manager.createUniqueId(null);
 		int i = 0;
 		for (IApplication container : containers) {
 			String host = container.getApplicationUrl();
 			if (host.endsWith("/")) {
 				host = host.substring(0, host.length() - 1);
 			}
 			URL url = new URL(host);
 			if (url.getPort() == -1) {
 				url = new URL(url.getProtocol(), url.getHost(), 80,
 						url.getFile());
 			}
 			String id = uniqueId + '_' + i++;
 			ZendTarget target = new ZendTarget(id, url, new URL(host), "", "");
 			String name = container.getName();
 			if (name != null && name.length() > 0) {
 				target.addProperty(TARGET_CONTAINER, name);
 			}
 			String uuid = container.getUUID();
 			if (uuid != null && uuid.length() > 0) {
 				target.addProperty(TARGET_UUID, uuid);
 			}
 			if (privateKey != null) {
 				target.addProperty(SSH_PRIVATE_KEY_PATH, privateKey);
 			}
 			target.addProperty(TARGET_USERNAME, username);
 			String internalHost = getInternalHost(container, target, keyBuilder);
 			if (internalHost != null) {
 				target.addProperty(TARGET_INTERNAL_HOST, internalHost);
 			}
 			if (hasMySqlSupport(container)) {
 				target.addProperty(TARGET_MYSQL_SUPPORT, "true");
 			}
 			String secretKey = null;
 			secretKey = getApiKey(uuid, url.getHost(), privateKey, keyBuilder);
 			if (secretKey != null) {
 				target.setKey(DEFAULT_KEY_NAME);
 				target.setSecretKey(secretKey);
 				result.add(target);
 			}
 		}
 		return (IZendTarget[]) result.toArray(new IZendTarget[result.size()]);
 	}
 
 	private String getApiKey(String uuid, String host, String privateKey,
 			PublicKeyBuilder keyBuilder) throws SdkException {
 		JSch jsch = new JSch();
 		try {
 			jsch.addIdentity(privateKey, keyBuilder.getPassphase(privateKey));
 
 			Session session = jsch.getSession(uuid, host, 22);
 			session.setConfig("StrictHostKeyChecking", "no");
 			session.connect();
 
 			Channel channel = session.openChannel("sftp");
 			channel.connect();
 			ChannelSftp sftpChannel = (ChannelSftp) channel;
 			File tempUserIni = File.createTempFile(USER_INI, ".tmp");
 			sftpChannel.get(ZEND_SERVER_USER_INI_PATH,
 					tempUserIni.getCanonicalPath());
 			String secretKey = findExistingSecretKey(DEFAULT_KEY_NAME,
 					tempUserIni);
 			if (secretKey == null) {
 				secretKey = applySecretKey(tempUserIni, DEFAULT_KEY_NAME,
 						generateSecretKey());
 				sftpChannel.put(tempUserIni.getCanonicalPath(),
 						ZEND_SERVER_USER_INI_PATH);
 			}
 			sftpChannel.exit();
 			session.disconnect();
 			tempUserIni.delete();
 			return secretKey;
 		} catch (JSchException e) {
 			throw new SdkException(e);
 		} catch (IOException e) {
 			throw new SdkException(e);
 		} catch (SftpException e) {
 			throw new SdkException(e);
 		}
 	}
 
 	private String applySecretKey(File keysFile, String key, String secretKey)
 			throws IOException, FileNotFoundException {
 		// temporary file
 		final File edited = File.createTempFile(USER_INI, ".tmp");
 		if (!edited.exists()) {
 			edited.createNewFile();
 		}
 
 		// backup file
 		BufferedReader ir = new BufferedReader(new FileReader(keysFile));
 		PrintStream os = new PrintStream(edited);
 		ZendTargetAutoDetect.copyWithoutEdits(ir, os);
 		ir.close();
 		os.close();
 		os = new PrintStream(keysFile);
 		ir = new BufferedReader(new FileReader(edited));
 		secretKey = ZendTargetAutoDetect.copyWithEdits(ir, os, key, secretKey);
 		ir.close();
 		os.close();
 		edited.delete();
 		return trimQuotes(secretKey);
 	}
 
 	private List<IApplication> getZendContainers() throws SdkException {
 		IDomain d = getDomain();
 		List<IApplication> result = new ArrayList<IApplication>();
 		if (d != null) {
 			List<IApplication> apps = domain.getApplications();
 			if (apps != null && apps.size() > 0) {
 				for (IApplication app : apps) {
 					ICartridge cartridge = app.getCartridge();
 					if (CARTRIDGE_NAME.equals(cartridge.getName())) {
 						result.add(app);
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	private IOpenShiftConnection getConnection() throws OpenShiftException,
 			IOException {
 		IOpenShiftConnection connection = new OpenShiftConnectionFactory()
 				.getConnection("zend_sdk", username, password, getLibraServer());
 		return connection;
 	}
 
 	private ISSHPublicKey loadSshKey(String privateKey,
 			PublicKeyBuilder keyBuilder) throws OpenShiftException,
 			FileNotFoundException, PublicKeyNotFoundException, IOException {
 		return new SSHPublicKey(getPublicKeyFile(privateKey, keyBuilder));
 	}
 
 	private File getPublicKeyFile(String privateKey, PublicKeyBuilder keyBuilder)
 			throws PublicKeyNotFoundException, IOException {
 		String publicKey = getPublicKeyPath(privateKey, keyBuilder);
 		String tempDir = System.getProperty("java.io.tmpdir");
 		File file = new File(tempDir + File.separator + new Random().nextInt());
 		file.mkdir();
 		File publicKeyFile = new File(file, "pub");
 		if (!file.exists()) {
 			file.createNewFile();
 		}
 		FileOutputStream stream = new FileOutputStream(publicKeyFile);
 		stream.write(publicKey.getBytes());
 		stream.close();
 		return publicKeyFile;
 	}
 
 	private boolean hasMySqlSupport(IApplication container) throws SdkException {
 		List<IEmbeddedCartridge> cartridges = null;
 		try {
 			cartridges = container.getEmbeddedCartridges();
 		} catch (OpenShiftException e) {
 			throw new SdkException(e);
 		}
 		if (cartridges != null) {
 			for (IEmbeddedCartridge cartridge : cartridges) {
 				if (cartridge.getName().startsWith("mysql")) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private String generateSecretKey() {
 		SecureRandom random = new SecureRandom();
 		final BigInteger bigInteger = new BigInteger(256, random);
 		final String string = bigInteger.toString(16);
 		return string.length() == 64 ? string : pad(string, 64);
 	}
 
 	/**
 	 * Random number was prefixed with some zeros... pad it
 	 * 
 	 * @param string
 	 * @param i
 	 * @return
 	 */
 	private String pad(String string, int i) {
 		i = i - string.length();
 		StringBuilder builder = new StringBuilder(string);
 		for (int j = 0; j < i; j++) {
 			builder.append("0");
 		}
 		return builder.toString();
 	}
 
 }
