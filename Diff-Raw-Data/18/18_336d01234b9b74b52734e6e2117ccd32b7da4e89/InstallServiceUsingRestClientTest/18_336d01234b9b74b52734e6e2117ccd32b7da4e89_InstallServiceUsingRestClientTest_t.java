 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import com.j_spaces.kernel.PlatformVersion;
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.LogUtils;
import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.CloudifyConstants.DeploymentState;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.packaging.Packager;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.cloudifysource.dsl.rest.request.InstallServiceRequest;
 import org.cloudifysource.dsl.rest.response.ApplicationDescription;
 import org.cloudifysource.dsl.rest.response.ServiceDescription;
 import org.cloudifysource.dsl.rest.response.UploadResponse;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.restclient.RestClient;
 import org.cloudifysource.restclient.exceptions.RestClientException;
 import org.cloudifysource.shell.commands.CLIException;
 import org.cloudifysource.shell.rest.RestAdminFacade;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 /**
  * This test uses the rest API to install a simple service.
  * 
  * @author adaml
  *
  */
 public class InstallServiceUsingRestClientTest extends AbstractLocalCloudTest {
 	
 	private static final String SERVICE_FILE_NAME = "simple-service.groovy";
 	private static final String SERVICE_NAME = "simple";
 	private static final String SERVICE_DIR_PATH = 
 			CommandTestUtils.getPath("src/main/resources/apps/USM/usm/simple");
 	private static final int INSTALL_TIMEOUT_MILLIS = 60 * 15 * 1000;
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1")
     public void testInstallService() 
     		throws IOException, PackagingException, 
     		DSLException, RestClientException, CLIException {
 		final String version = PlatformVersion.getVersion();
 		final URL url = new URL(restUrl);
 		final RestClient client = new RestClient(url, "", "", version);
         client.connect();
 		
 		final File serviceFolder = new File(SERVICE_DIR_PATH);
 		final File packedFile = Packager.pack(serviceFolder);
 		final UploadResponse uploadResponse = client.upload(null, packedFile);
 		final String uploadKey = uploadResponse.getUploadKey();
 		
 		InstallServiceRequest request = new InstallServiceRequest();
 		request.setServiceFolderUploadKey(uploadKey);
 
 		//Test will run in unsecured mode.
 		request.setAuthGroups("");
 		//no debugging.
 		request.setDebugAll(false);
 		request.setSelfHealing(true);
 		request.setServiceFileName(SERVICE_FILE_NAME);
 		//set timeout
 		request.setTimeoutInMillis(INSTALL_TIMEOUT_MILLIS);
 		
 		//make install service API call
 		client.installService(DEFAULT_APPLICATION_NAME, SERVICE_NAME, request);
 		
 		//Now we wait for the USM service state to become RUNNING.
 		waitForServiceInstall(url);
 	}
 
 	void waitForServiceInstall(final URL url)
 			throws CLIException, RestClientException {
 		final RestAdminFacade adminFacade = new RestAdminFacade();
 		adminFacade.connect(null, null, url.toString(), false);
 		
 		LogUtils.log("Waiting for application deployment state to be " + DeploymentState.STARTED) ;
 		AssertUtils.repetitiveAssertTrue(SERVICE_NAME + " service failed to deploy", new AssertUtils.RepetitiveConditionProvider() {
 			@Override
 			public boolean getCondition() {
 				try {
 					ApplicationDescription servicesDescriptionList = adminFacade.getServicesDescriptionList(DEFAULT_APPLICATION_NAME);
 					for (ServiceDescription description : servicesDescriptionList.getServicesDescription()) {
						if (description.getServiceName().equals(CloudifyConstants.DEFAULT_APPLICATION_NAME + "." + SERVICE_NAME)) {
 							if (description.getServiceState().equals(DeploymentState.STARTED)) {
 								return true;
 							}
 						}
 					}
 				} catch (final CLIException e) {
 					LogUtils.log("Failed getting service description");
 				}
 				return false;
 			}
 		} , AbstractTestSupport.OPERATION_TIMEOUT * 3);
 	}
 }
