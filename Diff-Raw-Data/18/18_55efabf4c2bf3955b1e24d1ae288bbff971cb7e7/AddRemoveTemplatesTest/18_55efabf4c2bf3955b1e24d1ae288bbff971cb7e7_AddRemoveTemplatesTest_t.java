 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.packaging.Packager;
 import org.cloudifysource.dsl.internal.packaging.ZipUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.GsmTestUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.SSHUtils;
 import iTests.framework.utils.ThreadBarrier;
 
 /**
  * 
  * @author yael
  *
  */
 public class AddRemoveTemplatesTest extends AbstractByonAddRemoveTemplatesTest {
 
 	private static final int NUM_OF_THREADS = 3;
 	private ThreadBarrier barrier = new ThreadBarrier(NUM_OF_THREADS + 1);
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {		
 		super.bootstrap();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addTemplatesTest() throws IOException {
 		TemplatesBatchHandler templatesHandler1 = new TemplatesBatchHandler();
 		templatesHandler1.addTemplates(5);
 		addTemplates(templatesHandler1);
 		assertExpectedListTemplates();
 
 		TemplatesBatchHandler templatesHandler2 = new TemplatesBatchHandler();
 		templatesHandler2.addTemplates(5);
 		addTemplates(templatesHandler2);
 		assertExpectedListTemplates();
 
 		TemplatesBatchHandler templatesHandler3 = new TemplatesBatchHandler();
 		templatesHandler3.addTemplates(5);
 		addTemplates(templatesHandler3);	
 		assertExpectedListTemplates();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void failedAddInstallTemplates() throws Exception {
 
 		TemplatesBatchHandler handler = new TemplatesBatchHandler();
 		TemplateDetails template = handler.addServiceTemplate();
 		String templateName = template.getTemplateName();
 		String output = addTemplates(handler);
 
 		AssertUtils.assertTrue("Failed to add " + templateName, !output.contains("Failed to add the following templates:"));
 
 		String templateRemotePath = getTemplateRemoteDirFullPath(templateName) + template.getTemplateFile().getName();
 		SSHUtils.runCommand(mngMachinesIP[0], AbstractTestSupport.OPERATION_TIMEOUT, "rm -f " + templateRemotePath, USER, PASSWORD);
 
 		int plannedNumberOfRestInstances = getService().getNumberOfManagementMachines();
 		
 		ProcessingUnit restPu = admin.getProcessingUnits().getProcessingUnit("rest");
 		
 		AssertUtils.assertTrue("Failed to discover " + plannedNumberOfRestInstances + " before grid service container restart", restPu.waitFor(plannedNumberOfRestInstances, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		
 		final ProcessingUnitInstance restInstance = restPu.getInstances()[0];
 		
 		if (restInstance.isDiscovered()) {
 			final CountDownLatch latch = new CountDownLatch(1);
 			ProcessingUnitInstanceRemovedEventListener eventListener = new ProcessingUnitInstanceRemovedEventListener() {
 
 				@Override
 				public void processingUnitInstanceRemoved(
 						ProcessingUnitInstance processingUnitInstance) {
 					if (processingUnitInstance.equals(restInstance)) {
 						latch.countDown();
 					}
 				}
 			};
 			
 			restPu.getProcessingUnitInstanceRemoved().add(eventListener);
 			try {
 				GsmTestUtils.restartContainer(restInstance.getGridServiceContainer(), true);
 				org.testng.Assert.assertTrue(latch.await(AbstractTestSupport.OPERATION_TIMEOUT,TimeUnit.MILLISECONDS));
 			} catch (InterruptedException e) {
 				org.testng.Assert.fail("Interrupted while killing container", e);
 			} finally {
 				restPu.getProcessingUnitInstanceRemoved().remove(eventListener);
 			}
 		}
 		
 		AssertUtils.assertTrue("Failed to discover " + plannedNumberOfRestInstances + " after grid service container restart", restPu.waitFor(plannedNumberOfRestInstances, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		
 
 		String serviceName = templateName + "_service";
 		output = installService(serviceName, templateName, true);
 
		AssertUtils.assertTrue("installation with non-existent template [" + templateName + "] succeeded, output was " 
		+ output, output.contains("Could not find compute template: " + templateName));
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addTemplateAndInstallService() throws IOException, InterruptedException {
 		TemplatesBatchHandler templatesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = templatesHandler.addServiceTemplate();
 		templatesHandler.addServiceTemplate();
 		// add templates
 		addTemplates(templatesHandler);
 		assertExpectedListTemplates();
 
 		final String templateName = addedTemplate.getTemplateName();
 		String serviceName = templateName + "_service";
 		try {
 			installService(serviceName, templateName, false);
 			assertRightUploadDir(serviceName, addedTemplate.getUploadDirName());
 		} finally {		
 			uninstallServiceIfFound(serviceName);
 		}
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addZippedTemplateAndInstallService() throws IOException, InterruptedException {
 		TemplatesBatchHandler templatesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = templatesHandler.addServiceTemplate();
 		File zippedTemplateFile = new File(templatesHandler.getTemplatesFolder() + "/../zipped-template.zip");
 
 		LogUtils.log("zipping " + templatesHandler.getTemplatesFolder() + " to " + zippedTemplateFile);
 		ZipUtils.zip(templatesHandler.getTemplatesFolder(), zippedTemplateFile);
 		AssertUtils.assertTrue("zip file not found,  zip failed", zippedTemplateFile.exists());
 
 		templatesHandler.setTemplatesFolder(zippedTemplateFile);
 
 		// add templates
 		addTemplates(templatesHandler);
 		assertExpectedListTemplates();
 
 		final String templateName = addedTemplate.getTemplateName();
 		String serviceName = templateName + "_service";
 		try {
 			installService(serviceName, templateName, false);
 		} finally {		
 			uninstallServiceIfFound(serviceName);
 		}
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addTemplatesUsingRestAPI() 
 			throws IllegalStateException, IOException, InterruptedException {
 		TemplatesBatchHandler templatesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = templatesHandler.addServiceTemplate();
 
 		File zipFile = Packager.createZipFile("templates", templatesHandler.getTemplatesFolder());
 		final FileBody body = new FileBody(zipFile);
 		final MultipartEntity reqEntity = new MultipartEntity();
 		reqEntity.addPart(CloudifyConstants.TEMPLATES_DIR_PARAM_NAME, body);
 		// create HttpPost
 		String postCommand = getRestUrl() + "/service/templates/";
 		final HttpPost httppost = new HttpPost(postCommand);
 		httppost.setEntity(reqEntity);
 		// execute
 		HttpResponse response = new DefaultHttpClient().execute(httppost);
 		final String templateName = addedTemplate.getTemplateName();
 		String serviceName = templateName + "_service";
 		try {
 			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
 			assertExpectedListTemplates();
 			installService(serviceName, templateName, false);
 			assertRightUploadDir(serviceName, addedTemplate.getUploadDirName());
 		} finally {
 			uninstallServiceIfFound(serviceName);
 			String removeUrl = getRestUrl() + "/service/templates/" + templateName;
 			HttpDelete httpDelete = new HttpDelete(removeUrl);
 			response = new DefaultHttpClient().execute(httpDelete);
 			assertListTemplates(defaultTemplates);
 		}
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void templatesWithTheSameUpload() throws IOException, InterruptedException {
 		TemplatesBatchHandler templatesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate1 = templatesHandler.addServiceTemplate();
 		TemplateDetails addedTemplate2 = templatesHandler.addCustomTemplate(new TemplateDetails(null, null, null, addedTemplate1.getUploadDirName(), null), true, false);
 
 		addTemplates(templatesHandler);
 		assertExpectedListTemplates();
 
 		final String template1Name = addedTemplate1.getTemplateName();
 		String service1Name = template1Name + "_service";
 		try {
 			installService(service1Name, template1Name, false);
 			assertRightUploadDir(service1Name, addedTemplate1.getUploadDirName());
 		} finally {
 			uninstallServiceIfFound(service1Name);
 		}
 		final String template2Name = addedTemplate2.getTemplateName();
 		String service2Name = template2Name + "_service";
 		try {
 			installService(service2Name, template2Name, false);
 			assertRightUploadDir(service2Name, addedTemplate2.getUploadDirName());
 		} finally {
 			uninstallServiceIfFound(service2Name);
 		}
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addExistAndNotExistTemplates() throws IOException {
 		TemplatesBatchHandler templatesHandler1 = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = templatesHandler1.addTemplates(2).get(0);
 		addTemplates(templatesHandler1);
 
 		TemplatesBatchHandler templatesHandler2 = new TemplatesBatchHandler();
 		templatesHandler2.addTemplate();
 		templatesHandler2.addExpectedToFailTemplate(addedTemplate);
 		addTemplates(templatesHandler2);
 
 		assertExpectedListTemplates();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void templateNotExists() throws IOException, InterruptedException {
 		String serviceName = "notExistTemplate_service";
 		try {
 			installService(serviceName, "notExistTemplate", true);
 		} finally {
 			uninstallServiceIfFound(serviceName);
 		} 
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void removeTemplateAndTryToInstallService() throws IOException, InterruptedException {
 		TemplatesBatchHandler templatesHandler = new TemplatesBatchHandler();
 		templatesHandler.addTemplate();
 		TemplateDetails addedTemplate = templatesHandler.addTemplate();
 		addTemplates(templatesHandler);
 		assertExpectedListTemplates();
 
 		final String templateName = addedTemplate.getTemplateName();
 		removeTemplate(templatesHandler, templateName, false, "Template " + templateName + " removed successfully");
 		assertExpectedListTemplates();
 
 		String serviceName = templateName + "_service";
 		try {
 			installService(serviceName, templateName, true);
 		} finally {
 			uninstallServiceIfFound(serviceName);
 		} 
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void removeNotExistTemplate() {
 		removeTemplate("error", true, "Failed to remove template [error]");
 		assertListTemplates(defaultTemplates);
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void illegalDuplicateTemplatesInTheSameFolder() throws IOException {
 		TemplatesBatchHandler TemplatesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = TemplatesHandler.addExpectedToFailTemplate(new TemplateDetails());
 		File duplicateTemplateFile = new File(TemplatesHandler.getTemplatesFolder(), "duplicateTemplate-template.groovy");
 		TemplateDetails duplicateTemplate = new TemplateDetails(addedTemplate.getTemplateName(), 
 				duplicateTemplateFile, null, addedTemplate.getUploadDirName(), addedTemplate.getMachineIP());
 		TemplatesHandler.addExpectedToFailTemplate(duplicateTemplate);
 
 		addTemplates(TemplatesHandler, "Template with name [" + addedTemplate.getTemplateName() + "] already exist");
 		assertExpectedListTemplates();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void illegalTemplateWithoutLocalUploadDir() throws IOException {
 		TemplatesBatchHandler TemplatesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = TemplatesHandler.addExpectedToFailTemplate();
 		// delete upload directory
 		File uploadDir = new File(TemplatesHandler.getTemplatesFolder(), addedTemplate.getUploadDirName());
 		FileUtils.deleteDirectory(uploadDir);
 		// try to add the template
 		addTemplates(TemplatesHandler, "Could not find upload directory");
 		assertListTemplates(defaultTemplates);
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void tryToRemoveUsedTemplate() throws IOException, InterruptedException {
 		TemplatesBatchHandler TemplatesHandler = new TemplatesBatchHandler();
 		final TemplateDetails addedTemplate = TemplatesHandler.addServiceTemplate();
 		String templateName = addedTemplate.getTemplateName();
 		addTemplates(TemplatesHandler);
 		assertExpectedListTemplates();
 
 		String serviceName = templateName + "_service";
 		try {
 			installService(serviceName, templateName, false);
 			assertRightUploadDir(serviceName, addedTemplate.getUploadDirName());
 			removeTemplate(TemplatesHandler, templateName, true, 
 					"the template is being used by the following services");
 			assertExpectedListTemplates();
 		} finally {		
 			uninstallServiceIfFound(serviceName);
 		}
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addRemoveAndAddAgainTemplates() throws IOException {
 		TemplatesBatchHandler handler = new TemplatesBatchHandler();
 		TemplateDetails toRemoveTemplate = handler.addTemplate();
 		addTemplates(handler);
 		removeTemplate(handler, toRemoveTemplate.getTemplateName(), false, null);
 		assertListTemplates(defaultTemplates);
 		handler.addCustomTemplate(toRemoveTemplate, false, false);		
 		addTemplates(handler);
 		assertExpectedListTemplates();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void threadedAddRemoveTemplate() throws Exception {
 
 		TemplatesBatchHandler handler = new TemplatesBatchHandler();
 		TemplateDetails template = handler.addExpectedToFailTemplate();
 		String templateName = template.getTemplateName();
 		String templateRemotePath;
 
 		LogUtils.log("starting adder threads");
 		for (int i = 0; i < NUM_OF_THREADS; i++) {
 			new Thread(new AddTemplatesThread(handler)).start();
 		}
 
 		barrier.await();
 		barrier.inspect();
 
 		templateRemotePath = getTemplateRemoteDirFullPath(templateName) + template.getTemplateFile().getName();
 		verifyTemplateExistence(mngMachinesIP[0], template, templateRemotePath, true);
 
 		LogUtils.log("starting remover threads");
 		for (int i = 0; i < NUM_OF_THREADS; i++) {
 			new Thread(new RemoveTemplatesThread(handler, templateName)).start();
 		}
 
 		barrier.await();
 		barrier.inspect();
 
 		verifyTemplateExistence(mngMachinesIP[0], template, templateRemotePath, false);
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 
 	@Override
 	public int getNumOfMngMachines() {
 		return 1;
 	}
 
 	class AddTemplatesThread implements Runnable {
 
 		private TemplatesBatchHandler handler;
 
 		public AddTemplatesThread(TemplatesBatchHandler handler) {			
 			this.handler = handler;
 		}
 
 		public void run() {
 			try {				
 				addTemplates(handler);	
 				barrier.await();
 			} catch (Exception e) {
 				barrier.reset(e);
 			}
 		}
 	}
 
 	class RemoveTemplatesThread implements Runnable {
 
 		private TemplatesBatchHandler handler;
 		private String templateName;
 
 		public RemoveTemplatesThread(TemplatesBatchHandler handler, String templateName) {			
 			this.handler = handler;
 			this.templateName = templateName;
 		}
 
 		public void run() {
 			try {				
 				removeTemplate(handler, templateName, true, null);
 				barrier.await();
 			} catch (Exception e) {
 				barrier.reset(e);
 			}
 		}
 	}
 }
