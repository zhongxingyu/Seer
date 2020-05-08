 /*
  * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * *****************************************************************************
  */
 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.LogUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.SystemDefaultHttpClient;
 import org.cloudifysource.domain.Application;
 import org.cloudifysource.domain.Service;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.CloudifyConstants.DeploymentState;
 import org.cloudifysource.dsl.internal.CloudifyErrorMessages;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.internal.packaging.Packager;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.cloudifysource.dsl.rest.request.InstallApplicationRequest;
 import org.cloudifysource.dsl.rest.request.InstallServiceRequest;
 import org.cloudifysource.dsl.rest.response.ApplicationDescription;
 import org.cloudifysource.dsl.rest.response.ControllerDetails;
 import org.cloudifysource.dsl.rest.response.DeploymentEvent;
 import org.cloudifysource.dsl.rest.response.DeploymentEvents;
 import org.cloudifysource.dsl.rest.response.GetMachineDumpFileResponse;
 import org.cloudifysource.dsl.rest.response.GetMachinesDumpFileResponse;
 import org.cloudifysource.dsl.rest.response.GetPUDumpFileResponse;
 import org.cloudifysource.dsl.rest.response.InstallApplicationResponse;
 import org.cloudifysource.dsl.rest.response.InstallServiceResponse;
 import org.cloudifysource.dsl.rest.response.ServiceDescription;
 import org.cloudifysource.dsl.rest.response.ShutdownManagementResponse;
 import org.cloudifysource.dsl.rest.response.UninstallApplicationResponse;
 import org.cloudifysource.dsl.rest.response.UninstallServiceResponse;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.restclient.RestClient;
 import org.cloudifysource.restclient.exceptions.RestClientException;
 import org.cloudifysource.shell.ConditionLatch;
 import org.cloudifysource.shell.exceptions.CLIException;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.testng.AssertJUnit;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 public class NewRestTestUtils {
 
 	private static final int POLLING_INTERVAL_MILLISECONDS = 1000;
 
 	public static InstallApplicationResponse installApplicationUsingNewRestApi(final String restUrl,
 			final String applicationName,
 			final File applicationFolder)
 			throws IOException, DSLException, PackagingException {
 		return installApplicationUsingNewRestApi(restUrl, applicationName, applicationFolder, null, null);
 	}
 
 	public static InstallApplicationResponse installApplicationUsingNewRestApi(final String restUrl,
 			final String applicationName,
 			final File applicationFolder, final File applicationOverridesFile,
 			final String expectedFailureMsg)
 			throws IOException, DSLException, PackagingException {
 		// create application zip file
 		final Application application =
 				ServiceReader.getApplicationFromFile(applicationFolder, applicationOverridesFile).getApplication();
 		final File packedAppFolder = Packager.packApplication(application, applicationFolder);
 		// create rest client and connect
 		final RestClient restClient = NewRestTestUtils.createAndConnect(restUrl);
 		// uploads
 		final String applicationFolderUploadKey = NewRestTestUtils.upload(restClient, packedAppFolder);
 		final String applicationOverridesUploadKey = NewRestTestUtils.upload(restClient, applicationOverridesFile);
 		// create the request
 		final InstallApplicationRequest request = new InstallApplicationRequest();
 		request.setApplcationFileUploadKey(applicationFolderUploadKey);
 		request.setApplicationOverridesUploadKey(applicationOverridesUploadKey);
 		request.setApplicationName(applicationName);
 		// install application and wait
 		InstallApplicationResponse installationResponse = null;
 		try {
 			installationResponse = restClient.installApplication(applicationName, request);
 			if (expectedFailureMsg != null) {
 				Assert.fail("expectedFailureMsg: " + expectedFailureMsg);
 			}
 			final String deploymentID = installationResponse.getDeploymentID();
 			NewRestTestUtils.waitForApplicationInstallation(restClient, restUrl, applicationName, deploymentID);
 		} catch (final RestClientException e) {
 			final String actualMsg = e.getMessageFormattedText();
 			if (expectedFailureMsg != null) {
 				Assert.assertTrue("error message [" + actualMsg + "] doesn't contain the expected failure messgae [" + expectedFailureMsg + "]", 
 						actualMsg.contains(expectedFailureMsg));
 			} else {
 				Assert.fail("failed to install application [" + applicationFolder.getAbsolutePath()
 						+ "], error message: " + actualMsg);
 			}
 		}
 		return installationResponse;
 	}
 
 	public static InstallServiceResponse installServiceUsingNewRestAPI(final String restUrl, final File serviceFolder,
 			final String applicationName, final String serviceName, final File serviceOverridesFile,
 			final int timeoutInMinutes, final String expectedFailureMsg) {
 		File packedFile = null;
 		try {
 			// create zip file
 			final Service service = ServiceReader.readService(null, serviceFolder, null, true, serviceOverridesFile);
 			packedFile = Packager.pack(serviceFolder, service, new LinkedList<File>());
 		} catch (final Exception e) {
 			AssertJUnit.fail("failed to zip templates folder: " + e.getMessage());
 		}
 
 		// create rest client and connect
 		final RestClient restClient = NewRestTestUtils.createAndConnect(restUrl);
 
 		// upload
 		final InstallServiceRequest request = new InstallServiceRequest();
 		final String serviceFolderUploadKey = NewRestTestUtils.upload(restClient, packedFile);
 		request.setServiceFolderUploadKey(serviceFolderUploadKey);
 		final String serviceOverridesUploadKey = NewRestTestUtils.upload(restClient, serviceOverridesFile);
 		request.setServiceOverridesUploadKey(serviceOverridesUploadKey);
 
 		// install service and wait
 		InstallServiceResponse installService = null;
 		try {
 			installService = restClient.installService(applicationName, serviceName, request);
 			final String deploymentID = installService.getDeploymentID();
 			NewRestTestUtils
 					.waitForServiceInstallation(restClient, restUrl, applicationName, serviceName, deploymentID);
 		} catch (final RestClientException e) {
 			final String actualMsg = e.getMessageFormattedText();
 			if (expectedFailureMsg != null) {
 				Assert.assertTrue("error message " + actualMsg + " doesn't contain the expected failure messgae ["
 						+ expectedFailureMsg + "]",
 						actualMsg.contains(expectedFailureMsg));
 			} else {
 				Assert.fail("failed to install service [" + serviceFolder.getAbsolutePath()
 						+ "], error message: " + actualMsg);
 			}
 		}
 		return installService;
 	}
 
 	public static InstallServiceResponse installServiceUsingNewRestAPI(final String restUrl, final File serviceFolder,
 			final String applicationName, final String serviceName, final int timeoutInMinutes,
 			final String expectedFailureMsg) {
 		return installServiceUsingNewRestAPI(restUrl, serviceFolder, applicationName, serviceName, null,
 				timeoutInMinutes, expectedFailureMsg);
 	}
 
 	public static InstallServiceResponse installServiceUsingNewRestAPI(final String restUrl, final File serviceFolder,
 			final String applicationName, final String serviceName, final int timeoutInMinutes) {
 		return installServiceUsingNewRestAPI(restUrl, serviceFolder, applicationName, serviceName, timeoutInMinutes,
 				null);
 	}
 
 	public static UninstallServiceResponse uninstallServiceUsingNewRestClient(final String restUrl,
 			final String serviceName, final String deploymentID, final int timeoutInMinutes) {
 		// create rest client and connect
 		final RestClient restClient = NewRestTestUtils.createAndConnect(restUrl);
 		// uninstall service and wait
 		UninstallServiceResponse uninstallService = null;
 		try {
 			uninstallService =
 					restClient.uninstallService(CloudifyConstants.DEFAULT_APPLICATION_NAME, serviceName,
 							timeoutInMinutes);
 			NewRestTestUtils.waitForServiceUninstall(restUrl, CloudifyConstants.DEFAULT_APPLICATION_NAME, serviceName,
 					deploymentID);
 		} catch (final RestClientException e) {
 			Assert.fail("failed to uninstall service [" + serviceName
 					+ "], error message: " + e.getMessageFormattedText());
 		}
 		return uninstallService;
 	}
 
 	public static UninstallApplicationResponse uninstallApplicationUsingNewRestClient(
 			final String restUrl, final String appName, final String deploymentID, final int timeoutInMinutes) {
 		// create rest client and connect
 		final RestClient restClient = NewRestTestUtils.createAndConnect(restUrl);
 		// uninstall service and wait
 		UninstallApplicationResponse uninstallResposne = null;
 		try {
 			uninstallResposne = restClient.uninstallApplication(appName, timeoutInMinutes);
 			NewRestTestUtils.waitForApplicationUninstall(restUrl, appName, deploymentID);
 		} catch (final RestClientException e) {
 			Assert.fail("failed to uninstall application [" + appName
 					+ "], error message: " + e.getMessageFormattedText());
 		}
 		return uninstallResposne;
 	}
 	
 	public static ShutdownManagementResponse shutdownManagers(final String restUrl, final String expectedFailureMsg, final long timeoutMinutes, final File managersFile) 
 			throws RestClientException, InterruptedException, TimeoutException, CLIException, JsonProcessingException, IOException {
 		
 		// connect to the REST
 		RestClient restClient = createAndConnect(restUrl);
 		
 		// shutdown the managers
 		ShutdownManagementResponse response = null;
 		try {
 			 response = restClient.shutdownManagers();
 		} catch (RestClientException e) {
 			final String actualMsg = e.getMessageFormattedText();
 			if (expectedFailureMsg != null) {
 				Assert.assertTrue("error message " + actualMsg + " doesn't contain the expected failure messgae ["
 						+ expectedFailureMsg + "]",
 						actualMsg.contains(expectedFailureMsg));
 			} else {
 				throw e;
 			}
 		}
 		
 		// write managers to file
 		if (managersFile != null) {
 			final List<ControllerDetails> managers = Arrays.asList(response.getControllers());
 			final ObjectMapper mapper = new ObjectMapper();
 			final String managersAsString = mapper.writeValueAsString(managers);
 			FileUtils.writeStringToFile(managersFile, managersAsString);
 		}
 		
 		// wait for shutdown
 		waitForManagersToShutDown(response.getControllers(), new URL(restUrl).getPort(), timeoutMinutes);
 		
 		return response;
 	}
 	
 	public static File getPUDumpFile(final String restUrl, final long fileZiseLimit, final String errMessageContain) 
 			throws IOException {
 
 		// connect to the REST
 		RestClient restClient = createAndConnect(restUrl);
 
 		// get dump data using REST API
 		GetPUDumpFileResponse response = null;
 		try {
 			response = restClient.getPUDumpFile(fileZiseLimit);
 			if (errMessageContain != null) {
 				Assert.fail("RestClientException expected [" + errMessageContain + "]");
 			}
 			// write the result data to a temporary file.
 			File file = File.createTempFile("dump", ".zip");
 			FileUtils.writeByteArrayToFile(file , response.getDumpData());
 			return file;
 		} catch (RestClientException e) {
 			if (errMessageContain == null) {
 				Assert.fail("Failed to get PU dump - " + e.getMessage());
 			} else {
 				String message = e.getMessageFormattedText();
 				Assert.assertTrue("error messgae [" + message + "] does not contain " + errMessageContain, 
 						message.contains(errMessageContain));
 			}
 		}
 		return null;
 	}
 	
 	public static File getMachineDumpFile(final String restUrl, final String ip, 
 			final String processors, final long fileZiseLimit, final String errMessageContain) 
 					throws IOException {
 		// connect to the REST
 		RestClient restClient = createAndConnect(restUrl);
 
 		// get dump data using REST API
 		GetMachineDumpFileResponse response = null;
 		try {
 			response = restClient.getMachineDumpFile(ip, fileZiseLimit, processors);
 			if (errMessageContain != null) {
 				Assert.fail("RestClientException expected [" + errMessageContain + "]");
 			}
 			// write the result data to a temporary file.
 			File file = File.createTempFile("dump", ".zip");
 			FileUtils.writeByteArrayToFile(file , response.getDumpBytes());
 			return file;
 		} catch (RestClientException e) {
 			if (errMessageContain == null) {
 				Assert.fail("Failed to get machine dump - " + e.getMessage());
 			} else {
 				String message = e.getMessageFormattedText();
 				Assert.assertTrue("error messgae [" + message + "] does not contain " + errMessageContain, 
 						message.contains(errMessageContain));
 			}
 		}
 		return null;
 	}
 
 
 	public static Map<String, File> getMachinesDumpFile(final String restUrl, final long fileZiseLimit) 
 			throws IOException {
 		return getMachinesDumpFile(restUrl, "", "", null, fileZiseLimit, null);
 	}
 
 	public static Map<String, File> getMachinesDumpFile(final String restUrl) 
 			throws IOException {
 		return getMachinesDumpFile(restUrl, "", "", null, 0, null);
 	}
 
 	public static Map<String, File> getMachinesDumpFile(final String restUrl,
 			final String processors, final long fileZiseLimit, final String errMessageContain) 
 			throws IOException {
 		return getMachinesDumpFile(restUrl, "", "", processors, fileZiseLimit, errMessageContain);
 	}
 	
 	public static Map<String, File> getMachinesDumpFile(final String restUrl, 
 			final String username, final String password) 
 			throws IOException {
 		return getMachinesDumpFile(restUrl, username, password, null, 0, null);
 	}
 
 	public static Map<String, File> getMachinesDumpFile(final String restUrl, 
 			final String username, final String password,
 			final String processors, final long fileZiseLimit, final String errMessageContain) 
 					throws IOException {
 		// connect to the REST
 		RestClient restClient = createAndConnect(restUrl, username, password);
 
 		// get dump data using REST API
 		GetMachinesDumpFileResponse response = null;
 		try {
 			response = restClient.getMachinesDumpFile(processors, fileZiseLimit);
 			if (errMessageContain != null) {
 				Assert.fail("RestClientException expected [" + errMessageContain + "]");
 			}
 			// write the result data to a temporary file.
 			Map<String, byte[]> dumpBytesPerIP = response.getDumpBytesPerIP();
 			Map<String, File> dumpFilesPerIP = new HashMap<String, File>(dumpBytesPerIP.size());
 			for (Entry<String, byte[]> entry : dumpBytesPerIP.entrySet()) {
 				File file = File.createTempFile("dump", ".zip");
 				file.deleteOnExit();
 				FileUtils.writeByteArrayToFile(file , entry.getValue());
 				dumpFilesPerIP.put(entry.getKey(), file);
 			}
 			return dumpFilesPerIP;
 		} catch (RestClientException e) {
			String message = e.getMessageFormattedText();
 			if (errMessageContain == null) {
				Assert.fail("Failed to get machines dump - " + message);
 			} else {
 				Assert.assertTrue("error messgae [" + message + "] does not contain " + errMessageContain, 
 						message.contains(errMessageContain));
 			}
 		}
 		return null;
 	}
 	
 	
 	private static void waitForManagersToShutDown(final ControllerDetails[] managers, final int port, 
 			final long timeoutMinutes) throws InterruptedException, TimeoutException, CLIException {
 		final Set<ControllerDetails> managersStillUp = new HashSet<ControllerDetails>();
 		managersStillUp.addAll(Arrays.asList(managers));
 
 		final ConditionLatch conditionLatch =
 				new ConditionLatch()
 		.pollingInterval(POLLING_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS)
 		.timeout(timeoutMinutes, TimeUnit.MINUTES)
 		.timeoutErrorMessage(CloudifyErrorMessages.SHUTDOWN_MANAGERS_TIMEOUT.getName());
 
 		conditionLatch.waitFor(new ConditionLatch.Predicate() {
 
 			@Override
 			public boolean isDone() throws CLIException, InterruptedException {
 
 				final Iterator<ControllerDetails> iterator = managersStillUp.iterator();
 				while (iterator.hasNext()) {
 					final ControllerDetails manager = iterator.next();
 					final String host =
 							manager.isBootstrapToPublicIp() ? manager.getPublicIp() : manager.getPrivateIp();
 							if (ServiceUtils.isPortFree(host, port)) {
 								iterator.remove();
 								LogUtils.log("manager [" + host + "] is down.");
 								if (managersStillUp.isEmpty()) {
 									LogUtils.log("all ports are free.");
 									return true;
 								}
 								LogUtils.log(managersStillUp.size() + " managers more to check");
 							} else {
 								LogUtils.log("manager [" + host + "] is still up.");
 							}
 				}
 				return false;
 			}
 		});
 	}
 
 	static void waitForServiceInstallation(final RestClient restClient, final String restUrl, final String appName,
 			final String serviceName, final String deploymentId)
 			throws RestClientException {
 		LogUtils.log("Waiting for service deployment state to be " + DeploymentState.STARTED);
 		AssertUtils.repetitiveAssertTrue(serviceName + " service failed to deploy",
 				new AssertUtils.RepetitiveConditionProvider() {
 					@Override
 					public boolean getCondition() {
 						try {
 							final ServiceDescription serviceDescription =
 									restClient.getServiceDescription(appName, serviceName);
 							if (serviceDescription != null && serviceDescription.getServiceName().equals(serviceName)) {
 								if (DeploymentState.STARTED.equals(serviceDescription.getServiceState())) {
 									return true;
 								}
 								return false;
 							}
 						} catch (final RestClientException e) {
 							LogUtils.log("Failed getting service description with deploymentId " + deploymentId
 									+ " error message: " + e.getMessageFormattedText());
 						}
 						return false;
 					}
 				}, AbstractTestSupport.OPERATION_TIMEOUT * 3);
 	}
 
 	static void waitForApplicationInstallation(final RestClient restClient, final String restUrl,
 			final String applicationName, final String deploymentID) {
 
 		LogUtils.log("Waiting for service deployment state to be " + DeploymentState.STARTED);
 		AssertUtils.repetitiveAssertTrue(applicationName + " service failed to deploy",
 				new AssertUtils.RepetitiveConditionProvider() {
 					@Override
 					public boolean getCondition() {
 						try {
 							final ApplicationDescription appDescription =
 									restClient.getApplicationDescription(applicationName);
 							if (appDescription != null && appDescription.getApplicationName().equals(applicationName)) {
 								if (DeploymentState.STARTED.equals(appDescription.getApplicationState())) {
 									return true;
 								}
 								return false;
 							}
 						} catch (final RestClientException e) {
 							LogUtils.log("Failed getting service description with deploymentId " + deploymentID
 									+ " error message: " + e.getMessageFormattedText());
 						}
 						return false;
 					}
 				}, AbstractTestSupport.OPERATION_TIMEOUT * 3);
 	}
 
 	static void waitForServiceUninstall(final String restUrl, final String appName, final String serviceName,
 			final String deploymentId) {
 		try {
 			final RestClient restClient = NewRestTestUtils.createAndConnect(restUrl);
 			AssertUtils.repetitiveAssertTrue("uninstall service failed " + serviceName,
 					new AssertUtils.RepetitiveConditionProvider() {
 						@Override
 						public boolean getCondition() {
 							try {
 								final DeploymentEvents deploymentEvents =
 										restClient.getDeploymentEvents(deploymentId, 0, -1);
 								final List<DeploymentEvent> events = deploymentEvents.getEvents();
 								for (final DeploymentEvent deploymentEvent : events) {
 									final String description = deploymentEvent.getDescription();
 									if (description.equals(CloudifyConstants.UNDEPLOYED_SUCCESSFULLY_EVENT)) {
 										return true;
 									}
 								}
 							} catch (final RestClientException e) {
 								LogUtils.log("Failed getting deployment events with deploymentId " + deploymentId
 										+ " error message: " + e.getMessageFormattedText());
 							}
 							return false;
 						}
 					}, AbstractTestSupport.OPERATION_TIMEOUT * 3);
 		} catch (final Exception e1) {
 			Assert.fail("failed to create GSRestClient with url " + restUrl + " and version "
 					+ PlatformVersion.getVersionNumber());
 		}
 	}
 
 	static void waitForApplicationUninstall(final String restUrl, final String appName, final String deploymentId) {
 		try {
 			final RestClient restClient = NewRestTestUtils.createAndConnect(restUrl);
 			AssertUtils.repetitiveAssertTrue("uninstall application failed " + appName,
 					new AssertUtils.RepetitiveConditionProvider() {
 						@Override
 						public boolean getCondition() {
 							try {
 								final DeploymentEvents deploymentEvents =
 										restClient.getDeploymentEvents(deploymentId, 0, -1);
 								final List<DeploymentEvent> events = deploymentEvents.getEvents();
 								for (final DeploymentEvent deploymentEvent : events) {
 									final String description = deploymentEvent.getDescription();
 									if (description.equals(CloudifyConstants.UNDEPLOYED_SUCCESSFULLY_EVENT)) {
 										return true;
 									}
 								}
 							} catch (final RestClientException e) {
 								LogUtils.log("Failed getting deployment events with deploymentId " + deploymentId
 										+ " error message: " + e.getMessageFormattedText());
 							}
 							return false;
 						}
 					}, AbstractTestSupport.OPERATION_TIMEOUT * 3);
 		} catch (final Exception e1) {
 			Assert.fail("failed to create GSRestClient with url " + restUrl + " and version "
 					+ PlatformVersion.getVersionNumber());
 		}
 	}
 
 	public static RestClient createAndConnect(final String restUrl, final String username, final String password) {
 		RestClient restClient = create(restUrl, username, password);
 		connect(restClient);
 		return restClient;
 	}
 	
 	public static RestClient createAndConnect(final String restUrl) {
 		RestClient restClient = create(restUrl, "", "");
 		connect(restClient);
 		return restClient;
 	}
 	
 	public static RestClient create(final String restUrl, final String username, final String password) {
 		RestClient restClient = null;
 		try {
 			final String apiVersion = PlatformVersion.getVersion();
 			restClient = new RestClient(new URL(restUrl), username, password, apiVersion);
 		} catch (final Exception e) {
 			Assert.fail("failed to create rest client with url " + restUrl + ", error message: " + e.getMessage());
 		}
 		return restClient;
 	}
 
 	
 	public static void connect(final RestClient restClient) {
 		try {
 			restClient.connect();
 		} catch (final RestClientException e) {
 			Assert.fail("failed to connect: " + e.getMessageFormattedText());
 		}
 	}
 	
 	static String upload(final RestClient restClient, final File toUploadFile) {
 		if (toUploadFile == null) {
 			return null;
 		}
 		String serviceFolderUploadKey = null;
 		try {
 			serviceFolderUploadKey = restClient.upload(null, toUploadFile).getUploadKey();
 		} catch (final RestClientException e) {
 			Assert.fail("failed to upload [" + toUploadFile
 					+ "], error message: " + e.getMessageFormattedText());
 		}
 		return serviceFolderUploadKey;
 	}
 
 	public static HttpResponse sendGetRequest(final String uri) throws ClientProtocolException, IOException {
 		final SystemDefaultHttpClient httpClient = new SystemDefaultHttpClient();
 		final HttpGet request = new HttpGet(uri);
 		final HttpResponse response = httpClient.execute(request);
 		return response;
 	}
 
 }
