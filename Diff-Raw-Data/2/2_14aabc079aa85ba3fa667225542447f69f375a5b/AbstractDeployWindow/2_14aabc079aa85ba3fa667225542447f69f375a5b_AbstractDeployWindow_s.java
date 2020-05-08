 package test.webui.objects.services;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import test.webui.interfaces.IDeployWindow;
 import test.webui.resources.WebConstants;
 
 import com.thoughtworks.selenium.Selenium;
 
 /**
  * represents a generic deployment window
  * @author elip
  *
  */
 public abstract class AbstractDeployWindow implements IDeployWindow {
 	
 	protected Selenium selenium;
 	WebDriver driver;
 	
 	WebElement deployOrCloseButton;
 	
 	String clusterSchema, numberOfInst, numberOfBackups, maxInstPerVm, maxInstPerMachine, userName, password, isSecured;
 	
 	/**
 	 * constructs an instance with deployment parameters.
 	 */
 	public AbstractDeployWindow (Selenium selenium, WebDriver driver, String isSecured, String userName, 
 			String password, String numberOfInstances,
 			String numberOfBackups, String clusterSchema, String maxInstPerVM,
 			String maxInstPerMachine) {
 		this.selenium = selenium;
 		this.driver = driver;
 		this.clusterSchema = clusterSchema;
 		this.numberOfInst = numberOfInstances;
 		this.numberOfBackups = numberOfBackups;
 		this.maxInstPerVm = maxInstPerVM;
 		this.maxInstPerMachine = maxInstPerMachine;
 		this.userName = userName;
 		this.password = password;
 		this.isSecured = isSecured;
 	}
 
 	public void sumbitDeploySpecs() {
 		if (isSecured == "true") {
 			selenium.click(WebConstants.Xpath.isSecuredCheckbox);
 			selenium.type(WebConstants.ID.passwordInput, password);
 			selenium.type(WebConstants.ID.usernameInput, userName);
 		}
 		selenium.click(WebConstants.Xpath.clusterSchemaCombo);
		selenium.mouseDown(WebConstants.Xpath.getPathToComboSelection(clusterSchema));
 		selenium.type(WebConstants.ID.numberOfInstInput,numberOfInst);
 		if (clusterSchema == "partitioned_sync2backup") {
 			for (int i = 0 ; i < Integer.parseInt(numberOfBackups) ; i++) {
 				selenium.mouseDown(WebConstants.Xpath.numberOfBackupsInc);
 				selenium.mouseUp(WebConstants.Xpath.numberOfBackupsInc);
 			}
 		}
 		selenium.type(WebConstants.ID.maxInsPerVmInput, maxInstPerVm);
 		selenium.type(WebConstants.ID.maxInstPerMachineInput, maxInstPerMachine);
 		
 	}
 	
 	public void deploy() {
 		deployOrCloseButton = driver.findElement(By.xpath(WebConstants.Xpath.deployPUButton));
 		deployOrCloseButton.click();
 	}
 
 	public void closeWindow() {
 		deployOrCloseButton = driver.findElement(By.xpath(WebConstants.Xpath.closeWindowButton));
 		deployOrCloseButton.click();	
 	}
 
 }
