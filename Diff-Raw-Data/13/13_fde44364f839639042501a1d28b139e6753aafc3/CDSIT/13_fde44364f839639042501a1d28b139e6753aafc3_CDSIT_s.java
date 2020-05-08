 package org.cagrid.cds.inttest;
 
 import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
 
 import java.security.KeyPair;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.net.ssl.KeyManager;
 
 import org.apache.cxf.configuration.security.KeyStoreType;
 import org.cagrid.core.soapclient.SingleEntityKeyManager;
 import org.cagrid.dorian.DorianPortType;
 import org.cagrid.dorian.FindGridUsersRequest;
 import org.cagrid.dorian.FindGridUsersRequest.Filter;
 import org.cagrid.dorian.FindGridUsersResponse;
 import org.cagrid.dorian.FindLocalUsersRequest;
 import org.cagrid.dorian.FindLocalUsersResponse;
 import org.cagrid.dorian.RegisterLocalUserRequest;
 import org.cagrid.dorian.RequestUserCertificateRequest;
 import org.cagrid.dorian.RequestUserCertificateRequest.Key;
 import org.cagrid.dorian.RequestUserCertificateRequest.Lifetime;
 import org.cagrid.dorian.RequestUserCertificateRequest.Saml;
 import org.cagrid.dorian.RequestUserCertificateResponse;
 import org.cagrid.dorian.UpdateLocalUserRequest;
 import org.cagrid.dorian.UpdateLocalUserRequest.User;
 import org.cagrid.dorian.idp.Application;
 import org.cagrid.dorian.idp.CountryCode;
 import org.cagrid.dorian.idp.LocalUser;
 import org.cagrid.dorian.idp.LocalUserFilter;
 import org.cagrid.dorian.idp.LocalUserStatus;
 import org.cagrid.dorian.idp.StateCode;
 import org.cagrid.dorian.ifs.CertificateLifetime;
 import org.cagrid.dorian.ifs.GridUser;
 import org.cagrid.dorian.ifs.GridUserFilter;
 import org.cagrid.dorian.ifs.PublicKey;
 import org.cagrid.dorian.service.Dorian;
 import org.cagrid.dorian.soapclient.DorianSoapClientFactory;
 import org.cagrid.gaards.authentication.AuthenticateUserRequest;
 import org.cagrid.gaards.authentication.AuthenticateUserRequest.Credential;
 import org.cagrid.gaards.authentication.AuthenticateUserResponse;
 import org.cagrid.gaards.authentication.BasicAuthentication;
 import org.cagrid.gaards.pki.CertUtil;
 import org.cagrid.gaards.pki.KeyUtil;
 import org.cagrid.systest.ContextLoader;
 import org.cagrid.systest.TestBase;
 import org.junit.Assert;
 import org.junit.Test;
 import org.oasis.names.tc.saml.assertion.AssertionType;
 import org.ops4j.pax.exam.MavenUtils;
 import org.ops4j.pax.exam.Option;
 import org.osgi.framework.Bundle;
 
 public class CDSIT extends TestBase {
 
 	private final static String USERNAME = "dorian";
 	private final static String PASSWORD = "DorianAdmin$1";
 
 	@Inject
 	private Dorian dorian;
 
 	@Override
 	protected void prePAX() {
 		DorianBootstrap dorianBootstrap = null;
 		try {
 			dorianBootstrap = new DorianBootstrap();
 			dorianBootstrap.createKeyAndTrustStores();
 		} catch (Exception e) {
 			throw new RuntimeException("Exception bootstrapping Dorian", e);
 		} finally {
 			dorianBootstrap.close();
 		}
 	}
 
 	@Override
 	@SuppressWarnings("rawtypes")
 	public List<Class> getAdditionalClasses() {
 		List<Class> additionalClasses = new ArrayList<Class>(2);
 		additionalClasses.add(DorianBootstrap.class);
 		additionalClasses.add(UserInfo.class);
 		return additionalClasses;
 	}
 
 	@Override
 	public List<Option> getTestBundles() {
 		List<Option> options = new ArrayList<Option>();
 		String featureVersion = MavenUtils.getArtifactVersion("org.cagrid",
 				"cagrid-features");
 		String featureURL = "mvn:org.cagrid/cagrid-features/" + featureVersion
 				+ "/xml/features";
		options.add(features(featureURL, "cagrid-dorian", "cagrid-gridgrouper"));
 		return options;
 	}
 
 	@Test
 	public void testCDS() throws Exception {
 		Assert.assertNotNull(bundleContext);
 
 		for (Bundle bundle : bundleContext.getBundles()) {
 			String bundleState = bundleStates.get(bundle.getState());
 			System.out.println(bundle.getBundleId() + ": "
 					+ bundle.getSymbolicName() + " - " + bundle.getLocation()
 					+ " [" + bundleState + "]");
 		}
 
 		final String dorianURL = "https://localhost:7734/dorian";
 
 		Assert.assertNotNull(dorian);
 
 		String karafBase = System.getProperty(ContextLoader.KARAF_BASE_KEY);
 		KeyStoreType truststore = new KeyStoreType();
 		truststore.setFile(karafBase + "/etc/dorian/truststore.jks");
 		truststore.setType("JKS");
 		truststore.setPassword("changeit");
 
 		DorianPortType dorianSoapAnon = DorianSoapClientFactory
 				.createSoapClient(dorianURL, truststore, (KeyManager) null);
 
 		BasicAuthentication basicAuthentication = new BasicAuthentication();
 		basicAuthentication.setUserId(USERNAME);
 		basicAuthentication.setPassword(PASSWORD);
 		Credential credential = new Credential();
 		credential.setCredential(basicAuthentication);
 		AuthenticateUserRequest authenticateUserRequest = new AuthenticateUserRequest();
 		authenticateUserRequest.setCredential(credential);
 		AuthenticateUserResponse authenticateUserResponse = dorianSoapAnon
 				.authenticateUser(authenticateUserRequest);
 		AssertionType assertion = authenticateUserResponse.getAssertion();
 		Assert.assertNotNull(assertion);
 
 		KeyPair keyPair = KeyUtil.generateRSAKeyPair(2048);
 		Saml saml = new Saml();
 		saml.setAssertion(assertion);
 		PublicKey caPublicKey = new PublicKey();
 		caPublicKey.setKeyAsString(KeyUtil.writePublicKey(keyPair.getPublic()));
 		RequestUserCertificateRequest userCertificateRequest = new RequestUserCertificateRequest();
 		userCertificateRequest.setSaml(saml);
 		Key caKey = new Key();
 		caKey.setPublicKey(caPublicKey);
 		userCertificateRequest.setKey(caKey);
 		CertificateLifetime certificateLifetime = new CertificateLifetime();
 		certificateLifetime.setHours(6);
 		Lifetime lifetime = new Lifetime();
 		lifetime.setCertificateLifetime(certificateLifetime);
 		userCertificateRequest.setLifetime(lifetime);
 		RequestUserCertificateResponse requestUserCertificateResponse = dorianSoapAnon
 				.requestUserCertificate(userCertificateRequest);
 		String certificateString = requestUserCertificateResponse
 				.getX509Certificate().getCertificateAsString();
 		X509Certificate certificate = CertUtil
 				.loadCertificate(certificateString);
 		Assert.assertNotNull(certificate);
 
 		KeyManager keyManager = new SingleEntityKeyManager("client",
 				new X509Certificate[] { certificate }, keyPair.getPrivate());
 		DorianPortType dorianSoapAuth = DorianSoapClientFactory
 				.createSoapClient("https://localhost:7734/dorian", truststore,
 						keyManager);
 
 		UserInfo endUserInfo = createLocalUser(dorianSoapAuth, "endUser", "End");
 		Assert.assertNotNull(endUserInfo.gridId);
 
 		UserInfo proxyUserInfo = createLocalUser(dorianSoapAuth, "proxyUser",
 				"Proxy");
 		Assert.assertNotNull(proxyUserInfo.gridId);
 	}
 
 	private UserInfo createLocalUser(DorianPortType dorianSoap, String userId,
 			String firstName) throws Exception {
 		final String password = "$D0ct0rC0de$";
 
 		Application application = new Application();
 		application.setUserId(userId);
 		application.setPassword(password);
 		application.setFirstName(firstName);
 		application.setLastName("User");
 		application.setEmail(firstName + ".User@test.org");
 		application.setAddress("123 Fake St.");
 		application.setCity("Columbus");
 		application.setState(StateCode.OH);
 		application.setCountry(CountryCode.US);
 		application.setZipcode("43210");
 		application.setPhoneNumber("614-555-5555");
 		application.setOrganization("organization");
 		RegisterLocalUserRequest.A a = new RegisterLocalUserRequest.A();
 		a.setApplication(application);
 		RegisterLocalUserRequest registerLocalUserRequest = new RegisterLocalUserRequest();
 		registerLocalUserRequest.setA(a);
 		dorianSoap.registerLocalUser(registerLocalUserRequest);
 
 		LocalUserFilter localUserFilter = new LocalUserFilter();
 		localUserFilter.setUserId(userId);
 		FindLocalUsersRequest.F f = new FindLocalUsersRequest.F();
 		f.setLocalUserFilter(localUserFilter);
 		FindLocalUsersRequest findLocalUsersRequest = new FindLocalUsersRequest();
 		findLocalUsersRequest.setF(f);
 		FindLocalUsersResponse findLocalUsersResponse = dorianSoap
 				.findLocalUsers(findLocalUsersRequest);
 		List<LocalUser> localUsers = findLocalUsersResponse.getLocalUser();
 		if (localUsers.size() != 1) {
 			throw new Exception("findLocalUsers returned " + localUsers.size()
 					+ " users!");
 		}
 
 		LocalUser localUser = localUsers.get(0);
 		localUser.setStatus(LocalUserStatus.ACTIVE);
 		User user = new User();
 		user.setLocalUser(localUser);
 		UpdateLocalUserRequest updateLocalUserRequest = new UpdateLocalUserRequest();
 		updateLocalUserRequest.setUser(user);
 		dorianSoap.updateLocalUser(updateLocalUserRequest);
 
 		BasicAuthentication basicAuthentication = new BasicAuthentication();
 		basicAuthentication.setUserId(userId);
 		basicAuthentication.setPassword(password);
 		Credential credential = new Credential();
 		credential.setCredential(basicAuthentication);
 		AuthenticateUserRequest authenticateUserRequest = new AuthenticateUserRequest();
 		authenticateUserRequest.setCredential(credential);
 		AuthenticateUserResponse authenticateUserResponse = dorianSoap
 				.authenticateUser(authenticateUserRequest);
 		AssertionType assertion = authenticateUserResponse.getAssertion();
 
 		KeyPair keyPair = KeyUtil.generateRSAKeyPair(2048);
 		Saml saml = new Saml();
 		saml.setAssertion(assertion);
 		PublicKey caPublicKey = new PublicKey();
 		caPublicKey.setKeyAsString(KeyUtil.writePublicKey(keyPair.getPublic()));
 		RequestUserCertificateRequest userCertificateRequest = new RequestUserCertificateRequest();
 		userCertificateRequest.setSaml(saml);
 		Key caKey = new Key();
 		caKey.setPublicKey(caPublicKey);
 		userCertificateRequest.setKey(caKey);
 		CertificateLifetime certificateLifetime = new CertificateLifetime();
 		certificateLifetime.setHours(6);
 		Lifetime lifetime = new Lifetime();
 		lifetime.setCertificateLifetime(certificateLifetime);
 		userCertificateRequest.setLifetime(lifetime);
 		RequestUserCertificateResponse requestUserCertificateResponse = dorianSoap
 				.requestUserCertificate(userCertificateRequest);
 		String certificateString = requestUserCertificateResponse
 				.getX509Certificate().getCertificateAsString();
 		X509Certificate x509Certificate = CertUtil
 				.loadCertificate(certificateString);
 
 		GridUserFilter gridUserFilter = new GridUserFilter();
 		gridUserFilter.setUID(userId);
 		Filter filter = new Filter();
 		filter.setGridUserFilter(gridUserFilter);
 		FindGridUsersRequest findGridUsersRequest = new FindGridUsersRequest();
 		findGridUsersRequest.setFilter(filter);
 		FindGridUsersResponse findGridUsersResponse = dorianSoap
 				.findGridUsers(findGridUsersRequest);
 		List<GridUser> gridUsers = findGridUsersResponse.getGridUser();
 		if (gridUsers.size() != 1) {
 			throw new Exception("findGridUsers returned " + gridUsers.size()
 					+ " users!");
 		}
 
 		GridUser gridUser = gridUsers.get(0);
 		String gridId = gridUser.getGridId();
 
 		UserInfo userInfo = new UserInfo(gridId, x509Certificate,
 				keyPair.getPrivate());
 		return userInfo;
 	}
 }
