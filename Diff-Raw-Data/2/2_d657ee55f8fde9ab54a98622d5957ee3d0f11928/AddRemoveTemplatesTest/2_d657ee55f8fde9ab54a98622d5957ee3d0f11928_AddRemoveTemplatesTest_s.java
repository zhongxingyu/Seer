 package test.cli.cloudify.cloud.byon;
 
 import java.io.File;
 import java.io.IOException;
 
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
 import org.testng.annotations.Test;
 
 import framework.utils.AssertUtils;
 import framework.utils.LogUtils;
 import framework.utils.ThreadBarrier;
 
 /**
  * 
  * @author yael
  *
  */
 public class AddRemoveTemplatesTest extends AbstractByonAddRemoveTemplatesTest {
 	
 	private static final int NUM_OF_THREADS = 3;
 	private ThreadBarrier barrier = new ThreadBarrier(NUM_OF_THREADS + 1);
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addTempaltesTest() throws IOException {
 		TemplatesBatchHandler templatesHandler1 = new TemplatesBatchHandler();
 		templatesHandler1.addTemplates(5);
 		addTempaltes(templatesHandler1);
 		assertExpectedListTempaltes();
 		
 		TemplatesBatchHandler templatesHandler2 = new TemplatesBatchHandler();
 		templatesHandler2.addTemplates(5);
 		addTempaltes(templatesHandler2);
 		assertExpectedListTempaltes();
 
 		TemplatesBatchHandler templatesHandler3 = new TemplatesBatchHandler();
 		templatesHandler3.addTemplates(5);
 		addTempaltes(templatesHandler3);	
 		assertExpectedListTempaltes();
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addTempalteAndInstallService() throws IOException, InterruptedException {
 		TemplatesBatchHandler templatesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = templatesHandler.addServiceTemplate();
 		templatesHandler.addServiceTemplate();
 		// add templates
 		addTempaltes(templatesHandler);
 		assertExpectedListTempaltes();
 		
 		final String templateName = addedTemplate.getTemplateName();
 		String serviceName = templateName + "_service";
 		try {
 			installService(serviceName, templateName, false);
 			assertRightUploadDir(serviceName, addedTemplate.getUploadDirName());
 		} finally {		
 			uninstallServiceIfFound(serviceName);
 		}
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addZippedTempalteAndInstallService() throws IOException, InterruptedException {
 		TemplatesBatchHandler templatesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = templatesHandler.addServiceTemplate();
		File zippedTemplateFile = new File(templatesHandler.getTemplatesFolder() + "\\..\\zipped-template.zip");
 		
 		LogUtils.log("zipping " + templatesHandler.getTemplatesFolder() + " to " + zippedTemplateFile);
 		ZipUtils.zip(templatesHandler.getTemplatesFolder(), zippedTemplateFile);
 		AssertUtils.assertTrue("zip file not found,  zip failed", zippedTemplateFile.exists());
 		
 		templatesHandler.setTemplatesFolder(zippedTemplateFile);
 		
 		// add templates
 		addTempaltes(templatesHandler);
 		assertExpectedListTempaltes();
 		
 		final String templateName = addedTemplate.getTemplateName();
 		String serviceName = templateName + "_service";
 		try {
 			installService(serviceName, templateName, false);
 		} finally {		
 			uninstallServiceIfFound(serviceName);
 		}
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addTempaltesUsingRestAPI() 
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
 			assertExpectedListTempaltes();
 			installService(serviceName, templateName, false);
 			assertRightUploadDir(serviceName, addedTemplate.getUploadDirName());
 		} finally {
 			uninstallServiceIfFound(serviceName);
 			String removeUrl = getRestUrl() + "/service/templates/" + templateName;
 			HttpDelete httpDelete = new HttpDelete(removeUrl);
 			response = new DefaultHttpClient().execute(httpDelete);
 			assertListTempaltes(defaultTempaltes);
 		}
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void templatesWithTheSameUpload() throws IOException, InterruptedException {
 		TemplatesBatchHandler templatesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate1 = templatesHandler.addServiceTemplate();
 		TemplateDetails addedTemplate2 = templatesHandler.addCustomTemplate(new TemplateDetails(null, null, null, addedTemplate1.getUploadDirName(), null), true, false);
 
 		addTempaltes(templatesHandler);
 		assertExpectedListTempaltes();
 
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
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addExistAndNotExistTempaltes() throws IOException {
 		TemplatesBatchHandler templatesHandler1 = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = templatesHandler1.addTemplates(2).get(0);
 		addTempaltes(templatesHandler1);
 
 		TemplatesBatchHandler templatesHandler2 = new TemplatesBatchHandler();
 		templatesHandler2.addTemplate();
 		templatesHandler2.addExpectedToFailTemplate(addedTemplate);
 		addTempaltes(templatesHandler2);
 		
 		assertExpectedListTempaltes();
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void templateNotExists() throws IOException, InterruptedException {
 		String serviceName = "notExistTempalte_service";
 		try {
 			installService(serviceName, "notExistTempalte", true);
 		} finally {
 			uninstallServiceIfFound(serviceName);
 		} 
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void removeTemplateAndTryToInstallService() throws IOException, InterruptedException {
 		TemplatesBatchHandler templatesHandler = new TemplatesBatchHandler();
 		templatesHandler.addTemplate();
 		TemplateDetails addedTemplate = templatesHandler.addTemplate();
 		addTempaltes(templatesHandler);
 		assertExpectedListTempaltes();
 		
 		final String templateName = addedTemplate.getTemplateName();
 		removeTemplate(templatesHandler, templateName, false, "Template " + templateName + " removed successfully");
 		assertExpectedListTempaltes();
 
 		String serviceName = templateName + "_service";
 		try {
 			installService(serviceName, templateName, true);
 		} finally {
 			uninstallServiceIfFound(serviceName);
 		} 
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void removeNotExistTemplate() {
 		removeTemplate("error", true, "Failed to remove template [error]");
 		assertListTempaltes(defaultTempaltes);
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void illegalDuplicateTempaltesInTheSameFolder() throws IOException {
 		TemplatesBatchHandler tempaltesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = tempaltesHandler.addExpectedToFailTemplate(new TemplateDetails());
 		File duplicateTemplateFile = new File(tempaltesHandler.getTemplatesFolder(), "duplicateTemplate-template.groovy");
 		TemplateDetails duplicateTemplate = new TemplateDetails(addedTemplate.getTemplateName(), 
 				duplicateTemplateFile, null, addedTemplate.getUploadDirName(), addedTemplate.getMachineIP());
 		tempaltesHandler.addExpectedToFailTemplate(duplicateTemplate);
 		
 		addTempaltes(tempaltesHandler, "Template with name [" + addedTemplate.getTemplateName() + "] already exist");
 		assertExpectedListTempaltes();
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void illegalTempalteWithoutLocalUploadDir() throws IOException {
 		TemplatesBatchHandler tempaltesHandler = new TemplatesBatchHandler();
 		TemplateDetails addedTemplate = tempaltesHandler.addExpectedToFailTempalte();
 		// delete upload directory
 		File uploadDir = new File(tempaltesHandler.getTemplatesFolder(), addedTemplate.getUploadDirName());
 		FileUtils.deleteDirectory(uploadDir);
 		// try to add the template
 		addTempaltes(tempaltesHandler, "Could not find upload directory");
 		assertListTempaltes(defaultTempaltes);
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void tryToRemoveUsedTempalte() throws IOException, InterruptedException {
 		TemplatesBatchHandler tempaltesHandler = new TemplatesBatchHandler();
 		final TemplateDetails addedTemplate = tempaltesHandler.addServiceTemplate();
 		String templateName = addedTemplate.getTemplateName();
 		addTempaltes(tempaltesHandler);
 		assertExpectedListTempaltes();
 
 		String serviceName = templateName + "_service";
 		try {
 			installService(serviceName, templateName, false);
 			assertRightUploadDir(serviceName, addedTemplate.getUploadDirName());
 			removeTemplate(tempaltesHandler, templateName, true, 
 					"the template is being used by the following services");
 			assertExpectedListTempaltes();
 		} finally {		
 			uninstallServiceIfFound(serviceName);
 		}
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addRemoveAndAddAgainTemplates() throws IOException {
 		TemplatesBatchHandler handler = new TemplatesBatchHandler();
 		TemplateDetails toRemoveTemplate = handler.addTemplate();
 		addTempaltes(handler);
 		removeTemplate(handler, toRemoveTemplate.getTemplateName(), false, null);
 		assertListTempaltes(defaultTempaltes);
 		handler.addCustomTemplate(toRemoveTemplate, false, false);		
 		addTempaltes(handler);
 		assertExpectedListTempaltes();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void threadedAddRemoveTemplate() throws Exception {
 		
 		TemplatesBatchHandler handler = new TemplatesBatchHandler();
 		TemplateDetails template = handler.addExpectedToFailTempalte();
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
 
 	@Override
 	public boolean isBootstrap() {
 		return true;
 	}
 
 	@Override
 	public boolean isTeardown() {
 		return true;
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
 				addTempaltes(handler);	
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
