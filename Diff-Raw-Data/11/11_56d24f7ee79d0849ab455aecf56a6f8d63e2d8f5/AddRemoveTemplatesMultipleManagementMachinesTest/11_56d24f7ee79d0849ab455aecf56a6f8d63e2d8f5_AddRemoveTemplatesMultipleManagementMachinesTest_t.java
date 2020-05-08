 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.SSHUtils;
 
import java.net.InetAddress;

 import org.cloudifysource.dsl.utils.IPUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.templates.TemplateDetails;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.templates.TemplatesFolderHandler;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.googlecode.ipv6.IPv6Address;
 
 public class AddRemoveTemplatesMultipleManagementMachinesTest extends AbstractByonAddRemoveTemplatesTest{
 
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {		
 		super.bootstrap();
 	}
 
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void addRemoveTemplates() throws Exception {
 				
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolder();
 		TemplateDetails template = folderHandler.addDefaultTempalte();
 		String templateName = template.getTemplateName();
 		
 		templatesHandler.addTemplatesToCloud(folderHandler);
 		templatesHandler.assertExpectedList();
 				
 		String templateRemotePath = getTemplateRemoteDirFullPath(templateName) + template.getTemplateFile().getName();	
 
 		verifyTemplateExistence(mngMachinesIP[0], template, templateRemotePath, true);
 		verifyTemplateExistence(mngMachinesIP[1], template, templateRemotePath, true);
 							
 		templatesHandler.removeTemplateFromCloud(folderHandler, templateName, false, null);
 		templatesHandler.assertExpectedList();
 		
 		verifyTemplateExistence(mngMachinesIP[0], template, templateRemotePath, false);
 		verifyTemplateExistence(mngMachinesIP[1], template, templateRemotePath, false);
 			
 	}
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void failedAddRemoveTemplates() throws Exception {
 		
 		TemplatesFolderHandler folderHandler = templatesHandler.createNewTemplatesFolder();
 		TemplateDetails template = folderHandler.addDefaultTempalte();
 		String templateName = template.getTemplateName();
 		templatesHandler.addTemplatesToCloud(folderHandler);
 		
 		String templateRemotePath = getTemplateRemoteDirFullPath(templateName) + template.getTemplateFile().getName();
 		
 		LogUtils.log(SSHUtils.runCommand(mngMachinesIP[1], AbstractTestSupport.OPERATION_TIMEOUT, "rm -f " + templateRemotePath, USER, PASSWORD));
 		
 		String output = templatesHandler.removeTemplateFromCloud(folderHandler, templateName, true, null);
 		
 		AssertUtils.assertTrue("successfully removed template from " + mngMachinesIP[1], 
 				output.contains("Failed to remove template [" + templateName + "]"));
 		String hostAddress0 = InetAddress.getByName(mngMachinesIP[0]).getHostAddress();
 		String hostAddress1 = InetAddress.getByName(mngMachinesIP[1]).getHostAddress();
 
 		if (IPUtils.isIPv6Address(mngMachinesIP[1])) {
 			AssertUtils.assertTrue("successfully removed template from " + hostAddress1 + "(output = " + output + ")", 
 					output.contains(IPv6Address.fromString(hostAddress1).toInetAddress().toString().replaceAll("/", "")));
 		} else {
 			AssertUtils.assertTrue("successfully removed template from " + hostAddress1 + " (output = " + output + ")", 
 					output.contains(hostAddress1));
 		}
 		
 		template.setExpectedToFail(true);
 		folderHandler.addCustomTemplate(template);
 		output = templatesHandler.addTemplatesToCloud(folderHandler);
 		
 		int failedIndex = output.indexOf("failed to add to : ");
 		AssertUtils.assertTrue("successfully added " + templateName + " to " + hostAddress1 + " (output = " + output + ")", 
 				failedIndex != -1);
 
 		int successIndex = output.indexOf("successfully added to : ");
 		AssertUtils.assertTrue("failed to add " + templateName + " to " + hostAddress0 + " (output = " + output + ")", 
 				successIndex != -1);
 		
 		String failedOutput = output.substring(failedIndex);
 		String successOutput = output.substring(successIndex, failedIndex);
 		
 		if (IPUtils.isIPv6Address(hostAddress1)) {
 			AssertUtils.assertTrue("successfully added " + templateName + " to " + hostAddress1 + " (output = " + output + ")", 
 					failedOutput.contains(IPv6Address.fromString(hostAddress1).toInetAddress().toString().replaceAll("/", "")));
 		} else {
 			AssertUtils.assertTrue("successfully added " + templateName + " to " + hostAddress1 + " (output = " + output + ")", 
 					failedOutput.contains(hostAddress1));
 		}
 
		if (IPUtils.isIPv6Address(hostAddress0)) {
 			AssertUtils.assertTrue("failed to add " + templateName + " to " + hostAddress0 + " (output = " + output + ")", 
					successOutput.contains(IPv6Address.fromString(hostAddress0).toInetAddress().toString().replaceAll("/", "")));
 		} else {
 			AssertUtils.assertTrue("failed to add " + templateName + " to " + hostAddress0 + " (output = " + output + ")", 
 					successOutput.contains(hostAddress0));
 		}
 		AssertUtils.assertTrue("failed to add " + templateName + " to " + hostAddress0 + " (output = " + output + ")", 
 				output.contains(templateName));
 		
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 
 
 	@Override
 	public int getNumOfMngMachines() {
 		return 2;
 	}
 
 
 
 }
