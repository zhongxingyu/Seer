package org.cagrid.dorian.service.tools;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.security.KeyPair;
 import java.security.KeyStore;
 import java.security.cert.X509Certificate;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import org.cagrid.core.commandline.BaseCommandLine;
 import org.cagrid.dorian.ca.impl.CertificateAuthority;
 import org.cagrid.dorian.ca.impl.CertificateAuthorityManager;
 import org.cagrid.dorian.ca.impl.CertificateAuthorityProperties;
 import org.cagrid.dorian.federation.impl.IdentityAssignmentPolicy;
 import org.cagrid.dorian.ifs.HostCertificateRecord;
 import org.cagrid.dorian.ifs.HostCertificateRequest;
 import org.cagrid.dorian.ifs.PublicKey;
 import org.cagrid.dorian.service.impl.DorianImpl;
 import org.cagrid.gaards.pki.CertUtil;
 import org.cagrid.gaards.pki.KeyUtil;
 
 public class Bootstrapper extends BaseCommandLine {
 
 	public static final String PROPERTIES_FILE = "src/main/resources/bootstrapper.properties";
 	private static final String DORIAN_SERVICE_DIR = "cagrid-dorian";
 	private static final String DORIAN_WSRF_CFG = "cagrid.dorian.wsrf.cfg";
 	public static final String HOSTNAME_PROMPT = "Please enter a hostname";
 	public static final String HOSTNAME_PROPERTY = "org.cagrid.dorian.hostname";
 	public static final String ADMIN_USER_ID_PROMPT = "Please enter the user id of admin";
 	public static final String ADMIN_USER_ID_PROPERTY = "org.cagrid.dorian.admin.user.id";
 	public static final String KEYSTORE_ALIAS_PROMPT = "Please enter a alias for the keystore";
 	public static final String KEYSTORE_ALIAS_PROPERTY = "org.cagrid.dorian.keystore.alias";
 	public static final String KEYSTORE_PASSWORD_PROMPT = "Please enter a password for the keystore";
 	public static final String KEYSTORE_PASSWORD_PROPERTY = "org.cagrid.dorian.keystore.password";
 	public static final String KEY_PASSWORD_PROMPT = "Please enter a password for the private key";
 	public static final String KEY_PASSWORD_PROPERTY = "org.cagrid.dorian.key.password";
 	public static final String KEYSTORE_FILE_NAME = "dorian-host.jks";
 	public static final String TRUSTSTORE_FILE_NAME = "truststore.jks";
 	public static final String TRUSTSTORE_ALIAS_PROMPT = "Please enter a alias for the truststore";
 	public static final String TRUSTSTORE_PASSWORD_PROMPT = "Please enter a password for the truststore";
 	public static final String TRUSTSTORE_PASSWORD_PROPERTY = "org.cagrid.dorian.truststore.password";
 	public static final String WSRF_KEYSTORE_PATH_PROPERTY = "cagrid.dorian.wsrf.keystore.path";
 	public static final String WSRF_KEYSTORE_PATH = "${karaf.base}/etc/" + DORIAN_SERVICE_DIR + "/" + KEYSTORE_FILE_NAME;
 	public static final String WSRF_KEYSTORE_PASSWORD_PROPERTY = "cagrid.dorian.wsrf.keystore.password";
 	public static final String WSRF_KEYSTORE_ALIAS_PROPERTY = "cagrid.dorian.wsrf.keystore.alias";
 	public static final String WSRF_KEY_PASSWORD_PROPERTY = "cagrid.dorian.wsrf.key.password";
 	public static final String WSRF_TRUSTSTORE_PATH_PROPERTY = "cagrid.dorian.wsrf.truststore.path";
 	public static final String WSRF_TRUSTSTORE_PATH = "${karaf.base}/etc/" + DORIAN_SERVICE_DIR + "/" + TRUSTSTORE_FILE_NAME;
 	public static final String WSRF_TRUSTSTORE_PASSWORD_PROPERTY = "cagrid.dorian.wsrf.truststore.password";
 
 	private String adminIdentity;
 	private String keystorePassword;
 	private String hostname;
 	private String keystoreAlias;
 	private String keyPassword;
 	private String truststorePassword;
 	private File dorianEtcDir;
 	private BootstrapperSpringUtils utils;
 
 	public Bootstrapper(File propertiesFile) throws Exception {
 		super(propertiesFile);
 		utils = new BootstrapperSpringUtils();
 	}
 
 	public Bootstrapper(Properties properties) throws Exception {
 		super(properties);
 		utils = new BootstrapperSpringUtils();
 	}
 
 	public String getAdminIdentity() throws Exception {
 		if (adminIdentity == null) {
 			String userId = getValue(ADMIN_USER_ID_PROMPT, ADMIN_USER_ID_PROPERTY);
 			X509Certificate cacert = utils.getDorian().getCACertificate();
 			String caSubject = cacert.getSubjectDN().getName();
 			int index = caSubject.lastIndexOf(",");
 			String subjectPrefix = caSubject.substring(0, index);
 			if (utils.getIdentityFederationProperties().getIdentityAssignmentPolicy().equals(IdentityAssignmentPolicy.NAME)) {
 				adminIdentity = CertUtil.subjectToIdentity(subjectPrefix + ",OU=" + utils.getIdentityProviderProperties().getName() + "/CN=" + userId);
 			} else {
 				adminIdentity = CertUtil.subjectToIdentity(subjectPrefix + ",OU=IdP [1]/CN=" + userId);
 			}
 			System.out.println(adminIdentity);
 		}
 		return adminIdentity;
 	}
 
 	public String getHostname() {
 		if (hostname == null) {
 			hostname = getValue(HOSTNAME_PROMPT, HOSTNAME_PROPERTY);
 		}
 		return hostname;
 	}
 
 	public String getKeystorePassword() {
 		if (keystorePassword == null) {
 			keystorePassword = getValue(KEYSTORE_PASSWORD_PROMPT, KEYSTORE_PASSWORD_PROPERTY);
 		}
 		return keystorePassword;
 	}
 
 	public String getKeystoreAlias() {
 		if (keystoreAlias == null) {
 			keystoreAlias = getValue(KEYSTORE_ALIAS_PROMPT, KEYSTORE_ALIAS_PROPERTY);
 		}
 		return keystoreAlias;
 	}
 
 	public String getKeyPassword() {
 		if (this.keyPassword == null) {
 			this.keyPassword = getValue(KEY_PASSWORD_PROMPT, KEY_PASSWORD_PROPERTY);
 		}
 		return this.keyPassword;
 	}
 
 	public String getTruststorePassword() {
 		if (truststorePassword == null) {
 			truststorePassword = getValue(TRUSTSTORE_PASSWORD_PROMPT, TRUSTSTORE_PASSWORD_PROPERTY);
 		}
 		return truststorePassword;
 	}
 
 	@Override
 	public void execute() throws Exception {
 		System.out.println("*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*");
 		System.out.println("*                   Dorian Bootstrapper                     *");
 		System.out.println("*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*");
 		System.out.println("");
 		((DorianImpl) utils.getDorian()).initialize();
 		dorianEtcDir = new File(getServiceMixEtc().getAbsolutePath() + File.separator + DORIAN_SERVICE_DIR);
 		dorianEtcDir.mkdirs();
 		createTruststore();
 		getAndSaveDorianHostCredentials();
 		configureDorianWSRF();
 
 	}
 
 	private void getAndSaveDorianHostCredentials() throws Exception {
 		File keystoreFile = new File(this.dorianEtcDir.getAbsolutePath() + File.separator + KEYSTORE_FILE_NAME);
 		writeHostCertificate(getHostname(), getKeystoreAlias(), keystoreFile.getAbsolutePath(), getKeystorePassword(), getKeyPassword());
 	}
 
 	private void configureDorianWSRF() throws Exception {
 		Properties dorianWSRFProperties = new Properties();
 		dorianWSRFProperties.setProperty(WSRF_TRUSTSTORE_PATH_PROPERTY, WSRF_TRUSTSTORE_PATH);
 		dorianWSRFProperties.setProperty(WSRF_TRUSTSTORE_PASSWORD_PROPERTY, getTruststorePassword());
 		dorianWSRFProperties.setProperty(WSRF_KEYSTORE_PATH_PROPERTY, WSRF_KEYSTORE_PATH);
 		dorianWSRFProperties.setProperty(WSRF_KEYSTORE_PASSWORD_PROPERTY, getKeystorePassword());
 		dorianWSRFProperties.setProperty(WSRF_KEYSTORE_ALIAS_PROPERTY, getKeystoreAlias());
 		dorianWSRFProperties.setProperty(WSRF_KEY_PASSWORD_PROPERTY, getKeyPassword());
 		File wsrfConfig = new File(getServiceMixEtc(), DORIAN_WSRF_CFG);
 		dorianWSRFProperties.store(new FileOutputStream(wsrfConfig), "Dorian Service Configuration saved by bootstrapper on " + new Date());
 	}
 
 	private void createTruststore() {
 
 		try {
 			File f = new File(this.dorianEtcDir.getAbsolutePath() + File.separator + TRUSTSTORE_FILE_NAME);
 			CertificateAuthorityManager caManager = utils.getCertificateAuthorityManager();
 
 			List<CertificateAuthority> list = caManager.getCertificateAuthorities();
 
 			KeyStore keyStore = KeyStore.getInstance("jks");
 			keyStore.load(null);
 			int count = 1;
 			for (CertificateAuthority ca : list) {
 				X509Certificate cert = ca.getCACertificate();
 				keyStore.setEntry("dorianca" + count, new KeyStore.TrustedCertificateEntry(cert), null);
 				count = count + 1;
 				System.out.println("Adding " + cert.getSubjectDN().getName() + " to the dorian truststore.");
 			}
 
 			FileOutputStream fos = new FileOutputStream(f);
 			keyStore.store(fos, getTruststorePassword().toCharArray());
 			fos.close();
 
 			System.out.println("Truststore created for Dorian at " + f.getAbsolutePath());
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void writeHostCertificate(String host, String hostAlias, String hostPath, String keystorePassword, String keyPassword) throws Exception {
 		CertificateAuthorityProperties caProperties = utils.getCertificateAuthorityProperties();
 		KeyPair pair = KeyUtil.generateRSAKeyPair(caProperties.getIssuedCertificateKeySize());
 
 		HostCertificateRequest req = new HostCertificateRequest();
 		req.setHostname(host);
 		PublicKey publicKey = new PublicKey();
 		publicKey.setKeyAsString(KeyUtil.writePublicKey(pair.getPublic()));
 		req.setPublicKey(publicKey);
 		HostCertificateRecord record = utils.getDorian().requestHostCertificate(this.getAdminIdentity(), req);
 		X509Certificate cert = CertUtil.loadCertificate(record.getCertificate().getCertificateAsString());
 		System.out.println("Successfully created the host certificate:");
 		System.out.println("Subject: " + cert.getSubjectDN());
 		System.out.println("Created: " + cert.getNotBefore());
 		System.out.println("Expires: " + cert.getNotAfter());
 
 		java.security.cert.Certificate[] hostCertChain = { cert };
 		KeyStore hks = KeyStore.getInstance("jks");
 		hks.load(null);
 
 		hks.setKeyEntry(hostAlias, pair.getPrivate(), keyPassword.toCharArray(), hostCertChain);
 		FileOutputStream out = new FileOutputStream(hostPath);
 		hks.store(out, keystorePassword.toCharArray());
 		out.close();
 		System.out.println("Keystore created for " + cert.getSubjectDN() + " at " + hostPath);
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Bootstrapper main = new Bootstrapper(new File(PROPERTIES_FILE));
 			main.execute();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }
