 package com.roosterpark.rptime.selenium.mule;
 
 import com.roosterpark.rptime.selenium.control.complex.form.CreateClientForm;
 import com.roosterpark.rptime.selenium.control.complex.navbar.AdminNavBar;
 import com.roosterpark.rptime.selenium.page.admin.ClientPage;
 import com.roosterpark.rptime.selenium.page.admin.HomePage;
 import org.openqa.selenium.WebDriver;
 
 /**
  * User: John
  * Date: 11/5/13
  * Time: 10:52 AM
  */
 public class ClientMule {
 
     private WebDriver driver;
 
     private LoginMule loginMule;
     private HomePage homePage;
 
     public ClientMule(WebDriver driver) {
         this.driver = driver;
         loginMule = new LoginMule(driver);
     }
 
     public WebDriver getDriver() {
         return driver;
     }
 
     public void login() {
         homePage = loginMule.loginAsAdmin();
     }
 
     public void setHomePage(HomePage homePage) {
         this.homePage = homePage;
     }
 
     public ClientPage addLunchRequiredClient(String name, String dayOfWeek) {
         return addClient(name, true, dayOfWeek);
     }
 
     public ClientPage addNonLunchRequiredClient(String name, String dayOfWeek) {
         return addClient(name, false, dayOfWeek);
     }
 
     private ClientPage addClient(String name, boolean isLunchRequired, String dayOfWeek) {
         AdminNavBar navBar = homePage.getNavBar();
         ClientPage clientPage = navBar.clickClientsLink();
         clientPage.pauseForRedraw();
         CreateClientForm createClientForm = clientPage.getCreateClientForm();
         createClientForm.enterName(name);
         if(isLunchRequired) {
             createClientForm.checkLunchRequired();
         }
         switch (dayOfWeek) {
             case "Sunday":
                 createClientForm.selectSunday();
                 break;
             case "Monday":
                 createClientForm.selectMonday();
                 break;
             case "Tuesday":
                 createClientForm.selectTuesday();
                 break;
             case "Wednesday":
                 createClientForm.selectWednesday();
                 break;
             case "Thursday":
                 createClientForm.selectThursday();
                 break;
             case "Friday":
                 createClientForm.selectFriday();
                 break;
             case "Saturday":
                 createClientForm.selectSaturday();
                 break;
             default:
                 createClientForm.selectMonday();
         }
         createClientForm.clickSave();
        clientPage.pauseForRedraw();
         return clientPage;
     }
 
 }
