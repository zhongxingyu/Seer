 /*******************************************************************************
  * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  *******************************************************************************/
 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.GsmTestUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.SSHUtils;
 import iTests.framework.utils.ThreadBarrier;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.dsl.internal.packaging.ZipUtils;
 import org.cloudifysource.dsl.rest.AddTemplatesException;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.templates.TemplateDetails;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.templates.TemplatesCommands;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.templates.TemplatesFolderHandler;
 import org.cloudifysource.restclient.exceptions.RestClientException;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
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
 	public void addTemplatesTest() throws IOException, InterruptedException {
 		TemplatesFolderHandler folderHandler1 = templatesHandler.createFolderWithTemplates(5);
 		TemplatesFolderHandler folderHandler2 = templatesHandler.createFolderWithTemplates(5);
 		TemplatesFolderHandler folderHandler3 = templatesHandler.createFolderWithTemplates(5);
 		templatesHandler.addTemplatesToCloud(folderHandler1);
 		templatesHandler.addTemplatesToCloud(folderHandler2);
 		templatesHandler.addTemplatesToCloud(folderHandler3);
 		templatesHandler.assertExpectedList();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void failedAddInstallTemplates() throws Exception {
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails template = folderHandler.addTempalteForServiceInstallation();
 		String templateName = template.getTemplateName();
 		templatesHandler.addTemplatesToCloud(folderHandler);
 
 		// remove template's file from remote machine
 		String templateRemotePath = getTemplateRemoteDirFullPath(templateName) + template.getTemplateFile().getName();
 		SSHUtils.runCommand(mngMachinesIP[0], AbstractTestSupport.OPERATION_TIMEOUT, "rm -f " + templateRemotePath, USER, PASSWORD);
 
 		int plannedNumberOfRestInstances = getService().getNumberOfManagementMachines();
 		
 		ProcessingUnit restPu = admin.getProcessingUnits().getProcessingUnit("rest");
 		
 		AssertUtils.assertTrue("Failed to discover " + plannedNumberOfRestInstances + " before grid service container restart",
 				restPu.waitFor(plannedNumberOfRestInstances, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		
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
 		
 		AssertUtils.assertTrue("Failed to discover " + plannedNumberOfRestInstances + " after grid service container restart", 
 				restPu.waitFor(plannedNumberOfRestInstances, AbstractTestSupport.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 		
 
 		// trying to install service with the template missing its file.
 		String serviceName = templateName + "_service";
 		String output = installServiceWithCoputeTemplate(serviceName, templateName, true);
 
 		AssertUtils.assertNotNull(output);
 		AssertUtils.assertTrue("installation with non-existent template [" + templateName + "] succeeded, output was " 
 		+ output, output.contains("template [" + templateName + "] does not exist at cloud templates list"));
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addTemplateAndInstallService() throws IOException, InterruptedException {
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails template = folderHandler.addTempalteForServiceInstallation();
 		// add template
 		templatesHandler.addTemplatesToCloud(folderHandler);
 		templatesHandler.assertExpectedList();
 
 		final String templateName = template.getTemplateName();
 		String serviceName = templateName + "_service";
 		try {
 			installServiceWithCoputeTemplate(serviceName, templateName, false);
 			assertRightUploadDir(serviceName, template.getUploadDirName());
 		} finally {		
 			uninstallServiceIfFound(serviceName);
 		}
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addZippedTemplateAndInstallService() throws IOException, InterruptedException {
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails template = folderHandler.addTempalteForServiceInstallation();
 		File templateFolder = template.getTemplateFolder();
 		File zippedTemplateFile = new File(templateFolder + File.separator + ".."  + File.separator + "zipped-template.zip");
 		ZipUtils.zip(templateFolder, zippedTemplateFile);
 		AssertUtils.assertTrue("zip file not found,  zip failed", zippedTemplateFile.exists());
 		
 		// add templates
 		folderHandler.setFolder(zippedTemplateFile);
 		templatesHandler.addTemplatesToCloud(folderHandler);
 		templatesHandler.assertExpectedList();
 
 		final String templateName = template.getTemplateName();
 		String serviceName = templateName + "_service";
 		try {
 			installServiceWithCoputeTemplate(serviceName, templateName, false);
 		} finally {		
 			uninstallServiceIfFound(serviceName);
 		}
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addTemplatesUsingRestAPI() 
 			throws IllegalStateException, IOException, InterruptedException, RestClientException, AddTemplatesException {
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails template = folderHandler.addTempalteForServiceInstallation();
 		
 		templatesHandler.addTemplatesToCloudUsingRestAPI(folderHandler);
 
 		final String templateName = template.getTemplateName();
 		String serviceName = templateName + "_service";
 		try {
 			templatesHandler.assertExpectedList();
 			installServiceWithCoputeTemplate(serviceName, templateName, false);
 			assertRightUploadDir(serviceName, template.getUploadDirName());
 		} finally {
 			uninstallServiceIfFound(serviceName);
 			templatesHandler.removeTemplatesFromCloudUsingRestAPI(folderHandler, templateName, false, null);
 			templatesHandler.assertExpectedList();
 		}
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void templatesWithTheSameUpload() throws IOException, InterruptedException {
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails template1 = folderHandler.addTempalteForServiceInstallation();
 		TemplateDetails templateDetails = new TemplateDetails();
 		templateDetails.setUploadDirName(template1.getUploadDirName());
 		templateDetails.setForServiceInstallation(true);
 		TemplateDetails template2 = folderHandler.addCustomTemplate(templateDetails);
 
 		templatesHandler.addTemplatesToCloud(folderHandler);
 		templatesHandler.assertExpectedList();
 
 		final String template1Name = template1.getTemplateName();
 		String service1Name = template1Name + "_service";
 		try {
 			installServiceWithCoputeTemplate(service1Name, template1Name, false);
 			assertRightUploadDir(service1Name, template1.getUploadDirName());
 		} finally {
 			uninstallServiceIfFound(service1Name);
 		}
 		final String template2Name = template2.getTemplateName();
 		String service2Name = template2Name + "_service";
 		try {
 			installServiceWithCoputeTemplate(service2Name, template2Name, false);
 			assertRightUploadDir(service2Name, template2.getUploadDirName());
 		} finally {
 			uninstallServiceIfFound(service2Name);
 		}
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addExistAndNotExistTemplates() throws IOException {
 		TemplatesFolderHandler templatesFolder1 = templatesHandler.createFolderWithTemplates(2);
 		String templateName = templatesFolder1.getExpectedToBeAddedTempaltes().get(0);
 		TemplateDetails addedTemplate = templatesFolder1.getTemplates().get(templateName);
 		templatesHandler.addTemplatesToCloud(templatesFolder1);
 
 		templatesHandler.assertExpectedList();
 
 		addedTemplate.setTemplateFolder(null);
 		TemplatesFolderHandler templatesFolder2 = templatesHandler.createNewTemplatesFolderHandler();
 		addedTemplate.setExpectedToFailOnAdd(true);
 		templatesFolder2.addCustomTemplate(addedTemplate);
 		templatesHandler.addTemplatesToCloud(templatesFolder2);
 
 		templatesHandler.assertExpectedList();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void templateNotExists() throws IOException, InterruptedException {
 		String serviceName = "notExistTemplate_service";
 		try {
 			installServiceWithCoputeTemplate(serviceName, "notExistTemplate", true);
 		} finally {
 			uninstallServiceIfFound(serviceName);
 		} 
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void removeTemplateAndTryToInstallService() throws IOException, InterruptedException {
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails tempalte = folderHandler.createAndAddDefaultTempalte();
 		templatesHandler.addTemplatesToCloud(folderHandler);
 		templatesHandler.assertExpectedList();
 
 		final String templateName = tempalte.getTemplateName();
 		templatesHandler.removeTemplateFromCloud(folderHandler, templateName, false, null);
 		templatesHandler.assertExpectedList();
 
 		String serviceName = templateName + "_service";
 		try {
 			installServiceWithCoputeTemplate(serviceName, templateName, true);
 		} finally {
 			uninstallServiceIfFound(serviceName);
 		} 
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void removeNotExistTemplate() {
 		String output = TemplatesCommands.removeTemplateCLI(getRestUrl(), "not_exist", true);
 		Assert.assertTrue(output.contains("Failed to remove template [not_exist]"));
 		templatesHandler.assertExpectedList();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void illegalDuplicateTemplatesInTheSameFolder() throws IOException {
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails addedTemplate = folderHandler.addExpectedToFailTempalte();
 		File duplicateTemplateFile = new File(addedTemplate.getTemplateFolder(), "duplicateTemplate-template.groovy");
 		TemplateDetails duplicateTemplate = new TemplateDetails();
 		String tempalteName = addedTemplate.getTemplateName();
 		duplicateTemplate.setTemplateName(tempalteName);
 		duplicateTemplate.setTemplateFile(duplicateTemplateFile);
 		duplicateTemplate.setUploadDirName(addedTemplate.getUploadDirName());
 		duplicateTemplate.setMachineIP(addedTemplate.getMachineIP());
 		duplicateTemplate.setExpectedToFailOnAdd(true);
 		folderHandler.addCustomTemplate(duplicateTemplate);
 		String response = templatesHandler.addTemplatesToCloud(folderHandler);
 		templatesHandler.assertExpectedList();
		Assert.assertTrue(response.contains("template with the name [" + tempalteName + "] already exist in folder"));
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void illegalTemplateWithoutLocalUploadDir() throws IOException {
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails addedTemplate = folderHandler.addExpectedToFailTempalte();
 		// delete upload directory
 		File uploadDir = new File(addedTemplate.getTemplateFolder(), addedTemplate.getUploadDirName());
 		FileUtils.deleteDirectory(uploadDir);
 		// try to add the template
 		String output = templatesHandler.addTemplatesToCloud(folderHandler);
 		Assert.assertTrue(output.contains("Could not find upload directory"));
 		templatesHandler.assertExpectedList();
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void tryToRemoveUsedTemplate() throws IOException, InterruptedException {
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails addedTemplate = folderHandler.addTempalteForServiceInstallation();
 		String templateName = addedTemplate.getTemplateName();
 		templatesHandler.addTemplatesToCloud(folderHandler);
 		templatesHandler.assertExpectedList();
 
 		String serviceName = templateName + "_service";
 		try {
 			installServiceWithCoputeTemplate(serviceName, templateName, false);
 			assertRightUploadDir(serviceName, addedTemplate.getUploadDirName());
 			templatesHandler.removeTemplateFromCloud(folderHandler, templateName, true, "the template is being used by the following services");
 			templatesHandler.assertExpectedList();
 		} finally {		
 			uninstallServiceIfFound(serviceName);
 		}
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addRemoveAndAddAgainTemplates() throws IOException {
 	
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails toRemoveTemplate = folderHandler.createAndAddDefaultTempalte();
 		templatesHandler.addTemplatesToCloud(folderHandler);
 		templatesHandler.removeTemplateFromCloud(folderHandler, toRemoveTemplate.getTemplateName(), false, null);
 		
 		templatesHandler.assertExpectedList();
 		
 		folderHandler.addCustomTemplate(toRemoveTemplate);
 		templatesHandler.addTemplatesToCloud(folderHandler);
 		
 		templatesHandler.assertExpectedList();
 	}
 	
 	/**
 	 * Creates template with name templateName and with file name other than "templateName-template.groovy".
 	 * Adds and removes this template.
 	 * The remove-template command will fail if the template file was not renamed to "templateName-template.groovy" at the management machine.
 	 * @throws IOException .
 	 * @throws InterruptedException .
 	 */
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addRemoveTemplateWithWrongFileNameTest() throws IOException, InterruptedException {
 		
 		// create folder with one template
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails tempalte = folderHandler.createAndAddDefaultTempalte();
 		
 		// rename template file
 		File templateFile = tempalte.getTemplateFile();
 		File parent = templateFile.getParentFile();
 		File newNameGroovyFile = new File(parent, "myTemplate-template.groovy");
 		templateFile.renameTo(newNameGroovyFile);
 		
 		// rename properties file
 		File templatePropertiesFile = tempalte.getTemplatePropertiesFile();
 		File newNamePropertiesFile = new File(parent, "myTemplate-template.properties");
 		templatePropertiesFile.renameTo(newNamePropertiesFile);
 		
 		// add tempalte
 		templatesHandler.addTemplatesToCloud(folderHandler);
 		templatesHandler.assertExpectedList();
 		
 		// remove template
 		templatesHandler.removeTemplateFromCloud(folderHandler, tempalte.getTemplateName(), false, null);
 		templatesHandler.assertExpectedList();
 	
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void threadedAddRemoveTemplate() throws Exception {
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolderHandler();
 		TemplateDetails template = folderHandler.addExpectedToFailTempalte();
 		String templateName = template.getTemplateName();
 		String templateRemotePath;
 
 		LogUtils.log("starting adder threads");
 		for (int i = 0; i < NUM_OF_THREADS; i++) {
 			new Thread(new AddTemplatesThread(folderHandler)).start();
 		}
 
 		barrier.await();
 		barrier.inspect();
 
 		templateRemotePath = getTemplateRemoteDirFullPath(templateName) + template.getTemplateFile().getName();
 		verifyTemplateExistence(mngMachinesIP[0], template, templateRemotePath, true);
 
 		LogUtils.log("starting remover threads");
 		for (int i = 0; i < NUM_OF_THREADS; i++) {
 			new Thread(new RemoveTemplatesThread(folderHandler, templateName)).start();
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
 
 		private TemplatesFolderHandler handler;
 
 		public AddTemplatesThread(TemplatesFolderHandler handler) {			
 			this.handler = handler;
 		}
 
 		public void run() {
 			try {
 				templatesHandler.addTemplatesToCloud(handler);
 				barrier.await();
 			} catch (Exception e) {
 				barrier.reset(e);
 			}
 		}
 	}
 
 	class RemoveTemplatesThread implements Runnable {
 
 		private TemplatesFolderHandler handler;
 		private String templateName;
 
 		public RemoveTemplatesThread(TemplatesFolderHandler handler, String templateName) {			
 			this.handler = handler;
 			this.templateName = templateName;
 		}
 
 		public void run() {
 			try {				
 				templatesHandler.removeTemplateFromCloud(handler, templateName, true, null);
 				barrier.await();
 			} catch (Exception e) {
 				barrier.reset(e);
 			}
 		}
 	}
 }
