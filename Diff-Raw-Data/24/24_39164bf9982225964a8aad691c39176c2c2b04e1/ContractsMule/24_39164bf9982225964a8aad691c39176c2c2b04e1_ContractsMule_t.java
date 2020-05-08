 package com.roosterpark.rptime.selenium.mule;
 
 import com.roosterpark.rptime.selenium.control.complex.form.CreateContractForm;
 import com.roosterpark.rptime.selenium.page.admin.ClientPage;
 import com.roosterpark.rptime.selenium.page.admin.ContractsPage;
 import com.roosterpark.rptime.selenium.page.admin.HomePage;
 import com.roosterpark.rptime.selenium.page.admin.WorkerPage;
 import org.openqa.selenium.WebDriver;
 
 /**
  * User: John
  * Date: 11/15/13
  * Time: 2:00 PM
  */
 public class ContractsMule {
 
     private WebDriver driver;
     private ClientMule clientMule;
     private WorkerMule workerMule;
     private LoginMule loginMule;
     private HomePage homePage;
     ContractsPage contractsPage;
 
     public ContractsMule(WebDriver driver) {
         this.driver = driver;
         clientMule = new ClientMule(driver);
         workerMule = new WorkerMule(driver);
         loginMule = new LoginMule(driver);
     }
 
     public void setHomePage(HomePage homePage) {
         this.homePage = homePage;
     }
 
     public WebDriver getDriver() {
         return driver;
     }
 
     public void login() {
         homePage = loginMule.loginAsAdmin();
     }
 
     public void addWorker(String firstName, String lastName, String email, String startDate, boolean isSalaried) {
        WorkerPage workerPage;
         workerMule.setHomePage(homePage);
         if (isSalaried) {
             workerPage = workerMule.addSalariedWorker(firstName, lastName, email, startDate);
         } else {
             workerPage = workerMule.addHourlyWorker(firstName, lastName, email, startDate);
         }
         homePage = workerPage.getNavBar().clickHomeLink();
     }
 
     public void addClient(String client, String dayOfWeek, boolean isLunchRequired) {
        ClientPage clientPage;
         clientMule.setHomePage(homePage);
         if(isLunchRequired) {
             clientPage = clientMule.addLunchRequiredClient(client, dayOfWeek);
         } else {
             clientPage = clientMule.addNonLunchRequiredClient(client, dayOfWeek);
         }
         homePage = clientPage.getNavBar().clickHomeLink();
     }
 
     public ContractsPage createOnSiteContract(String client, String worker, String startDate, String endDate) {
         return createContract(client, worker, true, startDate, endDate);
     }
 
     public ContractsPage createOffSiteContract(String client, String worker, String startDate, String endDate) {
         return createContract(client, worker, false, startDate, endDate);
     }
 
     private ContractsPage createContract(String client, String worker, boolean isOnSite,
                                          String startDate, String endDate) {
         contractsPage = homePage.getNavBar().clickContractsLink();
         contractsPage.pauseForRedraw();
         CreateContractForm createContractForm = contractsPage.getCreateContractForm();
         if (isOnSite) {
             createContractForm.checkOnSite();
         }
         createContractForm.selectClient(client);
         createContractForm.selectWorker(worker);
         createContractForm.enterStartDate(startDate);
         createContractForm.enterEndDate(endDate);
         createContractForm.clickSave();
        contractsPage.pauseForRedraw();
         return contractsPage;
     }
 
 }
