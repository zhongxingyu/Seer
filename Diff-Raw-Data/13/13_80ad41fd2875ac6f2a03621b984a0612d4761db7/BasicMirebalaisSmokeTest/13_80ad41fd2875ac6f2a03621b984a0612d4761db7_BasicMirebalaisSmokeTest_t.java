 package org.openmrs.module.mirebalais.smoke;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.TestRule;
 import org.junit.rules.TestWatcher;
 import org.junit.runner.Description;
 import org.openmrs.module.mirebalais.smoke.helper.SmokeTestDriver;
 import org.openmrs.module.mirebalais.smoke.pageobjects.AppDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientRegistrationDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.Registration;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.TakesScreenshot;
 import org.openqa.selenium.WebDriver;
 
 import java.io.File;
 import java.io.IOException;
 
 public abstract class BasicMirebalaisSmokeTest {
 
     @Rule
     public TestRule testWatcher = new TestWatcher() {
         @Override
         public void failed(Throwable t, Description test) {
             File tempFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
             try {
                 FileUtils.copyFile(tempFile, new File("screenshots/" + test.getDisplayName() + ".png"));
             } catch (IOException e) {
             }
         }
     };
 
     protected static LoginPage loginPage;
 	
 	protected static WebDriver driver;
 	
 	protected AppDashboard appDashboard;
 	
 	protected Registration registration;
 	
 	protected PatientRegistrationDashboard patientRegistrationDashboard;
 	
 	protected PatientDashboard patientDashboard;
 	
 	@BeforeClass
 	public static void startWebDriver() {
 		driver = new SmokeTestDriver().getDriver();
 	}
 	
 	@AfterClass
 	public static void stopWebDriver() {
 		driver.quit();
 	}
 	
 	protected void initBasicPageObjects() {
 		loginPage = new LoginPage(driver);
 		registration = new Registration(driver);
 		patientRegistrationDashboard = new PatientRegistrationDashboard(driver);
 		patientDashboard = new PatientDashboard(driver);
 		appDashboard = new AppDashboard(driver);
 	}
 
     protected void login() {
 		new LoginPage(driver).logInAsAdmin();
 	}

    protected void loginAsDoctor() {
        new LoginPage(driver).logInAsDoctor();
    }
 }
