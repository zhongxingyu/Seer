 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.Keys;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 public abstract class AbstractPageObject {
 
     protected SmokeTestProperties properties = new SmokeTestProperties();
 
     protected WebDriver driver;
     private String baseServerUrl;
     private LoginPage loginPO;
 
     public AbstractPageObject(WebDriver driver) {
         this.driver = driver;
         this.loginPO = new LoginPage(driver);
         setBaseServerUrl();
     }
 
     protected void gotoPage(String addressSufix) {
         driver.get(baseServerUrl + addressSufix);
     }
 
     protected void login(String username, String password) {
         gotoPage("");
         loginPO.logIn(username, password);
     }
 
     private void setBaseServerUrl() {
         String serverUrl = System.getProperty("baseServerUrl");
         this.baseServerUrl = (serverUrl == null || serverUrl.isEmpty() ? properties.getWebAppUrl() : serverUrl);
     }
     
     public void clickNext() {
         driver.findElement(By.id("right-arrow-yellow")).click();
     }
     
     public void clickYellowCheckMark() {
         driver.findElement(By.id("checkmark-yellow")).click();
     }
     
     public void setClearTextToField(String textFieldId, String text) {
         setText(driver.findElement(By.id(textFieldId)), text);
     }
     
     public void setClearTextToFieldThruSpan(String spanId, String text) {
     	setText(findTextFieldInsideSpan(spanId), text);
 	}
     
     private void setText(WebElement element, String text) {
     	element.clear();
 		element.sendKeys(text);
         element.sendKeys(Keys.RETURN);
     }
     
     private WebElement findTextFieldInsideSpan(String spanId) {
     	return driver.findElement(By.id(spanId)).findElement(By.tagName("input"));
     }
    
    public String format(String patientName) {
		int index = patientName.indexOf(" ");
		return new StringBuffer()
					.append(patientName.substring(index).trim())
					.append(", ")
					.append(patientName.substring(0,index).trim())
					.toString();
	}
 }
