 package org.motechproject.ghana.national.functional.pages.staff;
 
 import org.motechproject.ghana.national.functional.pages.home.HomePage;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.CacheLookup;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.ui.Select;
 
 public class StaffPageElements extends HomePage {
    @FindBy(id = "user_first_name")
     @CacheLookup
     WebElement firstName;
 
    @FindBy(id = "user_middle_name")
     @CacheLookup
     WebElement middleName;
 
    @FindBy(id = "user_last_name")
     @CacheLookup
     WebElement lastName;
 
     @FindBy(id = "phoneNumber")
     @CacheLookup
     WebElement phoneNumber;
 
    @FindBy(id = "user_email")
     @CacheLookup
     WebElement emailId;
 
    @FindBy(id = "user_role")
     @CacheLookup
     WebElement role;
 
     @FindBy(id = "staffId")
     @CacheLookup
     WebElement staffId;
 
     public StaffPageElements withFirstName(String firstName) {
         this.firstName.clear();
         this.firstName.sendKeys(firstName);
         return this;
     }
 
     public StaffPageElements withMiddleName(String middleName) {
         this.middleName.clear();
         this.middleName.sendKeys(middleName);
         return this;
     }
 
     public StaffPageElements withLastName(String lastName) {
         this.lastName.clear();
         this.lastName.sendKeys(lastName);
         return this;
     }
 
     public StaffPageElements withPhoneNumber(String phoneNumber) {
         this.phoneNumber.clear();
         this.phoneNumber.sendKeys(phoneNumber);
         return this;
     }
 
     public StaffPageElements withEmailId(String emailId) {
         this.emailId.clear();
         this.emailId.sendKeys(emailId);
         return this;
     }
 
     public StaffPageElements withRole(String role) {
         Select selectRole = new Select(this.role);
         selectRole.selectByVisibleText(role);
         return this;
     }
 
     public String staffId() {
         return staffId.getAttribute("value");
     }
 
     public StaffPageElements(WebDriver webDriver) {
         super(webDriver);
     }
 
 }
