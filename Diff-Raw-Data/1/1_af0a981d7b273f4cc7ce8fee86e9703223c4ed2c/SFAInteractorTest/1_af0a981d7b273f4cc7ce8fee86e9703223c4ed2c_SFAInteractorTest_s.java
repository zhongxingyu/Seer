 package org.fiteagle.interactors.sfa;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.fiteagle.interactors.sfa.common.AMCode;
 import org.fiteagle.interactors.sfa.common.AMResult;
 import org.fiteagle.interactors.sfa.common.AMValue;
 import org.fiteagle.interactors.sfa.common.Authorization;
 import org.fiteagle.interactors.sfa.common.Credentials;
 import org.fiteagle.interactors.sfa.common.GENI_CodeEnum;
 import org.fiteagle.interactors.sfa.common.Geni_RSpec_Version;
 import org.fiteagle.interactors.sfa.common.ListCredentials;
 import org.fiteagle.interactors.sfa.getversion.GeniAPIVersion;
 import org.fiteagle.interactors.sfa.getversion.GeniRequestRSpecVersions;
 import org.fiteagle.interactors.sfa.getversion.GetVersionResult;
 import org.fiteagle.interactors.sfa.getversion.GetVersionValue;
 import org.fiteagle.interactors.sfa.listresources.ListResourceOptions;
 import org.fiteagle.interactors.sfa.listresources.ListResourcesResult;
 import org.junit.Before;
 import org.junit.Test;
 
 public class SFAInteractorTest {
 	public static final String EXPECTED_TYPE = "GENI";
 	public static final int EXPECTED_VERSION = 2;
 	public static final String EXPECTED_API_URL = "https://fiteagle.org/api/sfa/v2/xmlrpc/am";
 
 	private transient ISFA sfaInteractor;
 
 	@Before
 	public void setUp() {
 		this.sfaInteractor = new SFAInteractor_v3();
 	}
 
 	@Test
 	public void testGetVersion() throws IOException {
 		final GetVersionResult getVersionResult = this.getGeniVersion();
 		this.validateGeniCode(getVersionResult);
 
 		final GetVersionValue value = (GetVersionValue) this
 				.getGeniValue(getVersionResult);
 
 		// TODO to be tested on delivery layer =>
 		// this.validateGetVersionGeniValue(value);
 		// TODO to be tested on delivery layer =>
 		// this.valudateGeniAPIs(value);
 		this.validateRSpecVersions(value);
 	}
 
 	@Test
 	public void testListResources() throws IOException {
 		ListResourceOptions options = createMinimalListResourceOptions("GENI",
 				"3");
 		final ListResourcesResult listResourcesResult = this.sfaInteractor
 				.listResources(getListCredentials(), options);
 
 	}
 
 	@Test
 	public void testCombinedGetVersionAndListResources() throws IOException {
 		final GetVersionResult getVersionResult = this.getGeniVersion();
 		ListResourceOptions options = createMinimalListResourceOptions(
 				getVersionResult.getValue().getGeni_ad_rspec_versions().get(0)
 						.getType(), getVersionResult.getValue()
 						.getGeni_ad_rspec_versions().get(0).getVersion());
 
 		final ListResourcesResult listResourcesResult = this.sfaInteractor
 				.listResources(getListCredentials(), options);
 		Assert.assertEquals(0, listResourcesResult.getCode().getGeni_code());
 
 	}
 
 	@Test
 	public void testInvalidListResourcesVersion() throws IOException {
 
 		ListResourceOptions options = createMinimalListResourceOptions("GENI",
 				"2");
 
 		ListResourcesResult listResourcesResult = this.sfaInteractor
 				.listResources(getListCredentials(), options);
 		Assert.assertEquals(listResourcesResult.getCode().retrieveGeni_code(),
 				GENI_CodeEnum.BADVERSION);
 
 	}
 
 	
 	private ListResourceOptions createMinimalListResourceOptions(String type,
 			String version) {
 		ListResourceOptions options = new ListResourceOptions();
 		Geni_RSpec_Version geni_rspec_version = new Geni_RSpec_Version();
 		geni_rspec_version.setType(type);
 		geni_rspec_version.setVersion(version);
 		options.setGeni_rspec_version(geni_rspec_version);
 		return options;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void validateRSpecVersions(final GetVersionValue value) {
 		final List<GeniRequestRSpecVersions> request_rspec_versions = value
 				.getGeni_request_rspec_versions();
 		Assert.assertNotNull(request_rspec_versions);
 		Assert.assertFalse(request_rspec_versions.isEmpty());
 		Assert.assertEquals(request_rspec_versions.get(0).getType(),
 				SFAInteractorTest.EXPECTED_TYPE);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void valudateGetVersionGeniAPIs(final GetVersionValue value) {
 		final Map<String, GeniAPIVersion> api_versions = value
 				.getGeni_api_versions();
 		Assert.assertNotNull(api_versions);
 		// Assert.assertEquals(SFAInteractorTest.EXPECTED_API_URL, api_versions.
 		// .get(String.valueOf(SFAInteractorTest.EXPECTED_VERSION)));
 	}
 
 	@SuppressWarnings("unchecked")
 	private Object getGeniValue(final AMResult amResult) {
 		final Object value = amResult.getValue();
 		return value;
 	}
 
 	private void validateGetVersionGeniValue(final GetVersionValue value) {
 		Assert.assertNotNull(value);
 
 		int resultedVersion = value.getGeni_api();
 
 		Assert.assertEquals(SFAInteractorTest.EXPECTED_VERSION, Integer
 				.valueOf(resultedVersion).intValue());
 	}
 
	@SuppressWarnings("unchecked")
 	private void validateGeniCode(final GetVersionResult getVersionResult) {
 		final AMCode code = getVersionResult.getCode();
 
 		Assert.assertNotNull(code);
 		Assert.assertEquals(ISFA.ERRORCODE_SUCCESS, code.getGeni_code());
 	}
 
 	private GetVersionResult getGeniVersion() throws IOException {
 		final GetVersionResult version = this.sfaInteractor.getVersion();
 		Assert.assertNotNull(version);
 		// version is to be supplied by the according delivery mechanism
 		// Assert.assertEquals(SFAInteractorTest.EXPECTED_VERSION,
 		// version.getGeniApiVersion());
 		return version;
 	}
 
 	private ListCredentials getListCredentials() {
 		Credentials credentials = new Credentials();
 		credentials.setGeni_type(Authorization.GENI_TYPE);
 		credentials.setGeni_version(Authorization.GENI_VERSION);
 		ListCredentials listCredentials = new ListCredentials();
 		listCredentials.getCredentialsList().add(credentials);
 		return listCredentials;
 	}
 
 }
