 package org.motechproject.ghana.national.functional.framework;
 
 import org.motechproject.ghana.national.functional.TestEnvironmentConfiguration;
 import org.motechproject.ghana.national.functional.base.WebDriverProvider;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSEncounterPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSHomePage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSLoginPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSPatientPage;
 import org.openqa.selenium.WebDriver;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class OpenMRSBrowser implements DisposableBean {
     protected WebDriver webDriver;
     protected ApplicationURLs applicationURLs;
 
     @Autowired
     public OpenMRSBrowser(WebDriverProvider webDriverProvider, TestEnvironmentConfiguration configuration) {
         applicationURLs = new ApplicationURLs(configuration);
         this.webDriver = webDriverProvider.getWebDriver();
     }
 
     public OpenMRSLoginPage toOpenMRSLoginPage() {
         webDriver.get(applicationURLs.forOpenMRSHomePage());
         return new OpenMRSLoginPage(webDriver);
     }
 
     public OpenMRSPatientPage toOpenMRSPatientPage(String openMrsId) {
        openMRSHomePage();
         webDriver.get(applicationURLs.forOpenMRSPatientPage(openMrsId));
         return new OpenMRSPatientPage(webDriver);
     }
 
     public OpenMRSEncounterPage toOpenMRSEncounterPage(String encounterId) {
         webDriver.get(applicationURLs.forOpenMRSEncounterPage(encounterId));
         return new OpenMRSEncounterPage(webDriver);
     }
 
     public OpenMRSHomePage toOpenMRSHomePage() {
         webDriver.get(applicationURLs.forOpenMRSHomePage());
         return openMRSHomePage();
     }
 
     public OpenMRSHomePage openMRSHomePage() {
         return new OpenMRSHomePage(webDriver);
     }
 
     public void quit() {
         webDriver.quit();
     }
 
     @Override
     public void destroy() throws Exception {
         quit();
     }
 }
