 package org.openmrs.module.mirebalais.smoke;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.pageobjects.AppDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.UserAdmin;
 
 public class UserAdminTest extends BasicMirebalaisSmokeTest {
 
     private LoginPage loginPage;
     private AppDashboard appDashboard;
     private UserAdmin userAdmin;
     private HeaderPage header;
     
 	private static final String DEFAULT_PASSWORD = "Teste123";
     
 	@Before
     public void setUp() {
 		loginPage = new LoginPage(driver);
 		appDashboard = new AppDashboard(driver);
 		userAdmin = new UserAdmin(driver);
 		header = new HeaderPage(driver);
     }
 
     @Test
     public void createUserWithClinicalRole() throws InterruptedException {
     	String username = createUser();
     	
 		loginPage.logIn("admin", "Admin123");
     	appDashboard.openSysAdminApp();
     	userAdmin.createClinicalAccount("Test", "User", username, DEFAULT_PASSWORD);
     	
    	assertTrue(userAdmin.isAccountCreatedSuccessfully());
     	
     	logOutAndLogInWithNewUser(username);
     	
     	assertTrue(appDashboard.isActiveVisitsAppPresented());
     	assertTrue(appDashboard.isFindAPatientAppPresented());
     	assertFalse(appDashboard.isSystemAdministrationAppPresented());
     	assertFalse(appDashboard.isPatientRegistrationAppPresented());
     	assertFalse(appDashboard.isArchivesRoomAppPresented());
 	}
     
     @Test
     public void createUserWithRadiologyRole() throws InterruptedException {
     	String username = createUser();
     	
 		loginPage.logIn("admin", "Admin123");
 		appDashboard.openSysAdminApp();
     	userAdmin.createRadiologyAccount("Test", "User", username, DEFAULT_PASSWORD);
     	
    	assertTrue(userAdmin.isAccountCreatedSuccessfully());
     	
     	logOutAndLogInWithNewUser(username);
     	
     	assertTrue(appDashboard.isActiveVisitsAppPresented());
     	assertTrue(appDashboard.isFindAPatientAppPresented());
     	assertFalse(appDashboard.isSystemAdministrationAppPresented());
     	assertFalse(appDashboard.isPatientRegistrationAppPresented());
     	assertFalse(appDashboard.isArchivesRoomAppPresented());
 	}
     
     @Test
     public void createUserWithDataArchivesRole() throws InterruptedException {
     	String username = createUser();
     	
 		loginPage.logIn("admin", "Admin123");
 		appDashboard.openSysAdminApp();
     	userAdmin.createDataArchivesAccount("Test", "User", username, DEFAULT_PASSWORD);
     	
    	assertTrue(userAdmin.isAccountCreatedSuccessfully());
     	
     	logOutAndLogInWithNewUser(username);
     	
     	assertTrue(appDashboard.isActiveVisitsAppPresented());
     	assertTrue(appDashboard.isFindAPatientAppPresented());
     	assertFalse(appDashboard.isSystemAdministrationAppPresented());
     	assertTrue(appDashboard.isPatientRegistrationAppPresented());
     	assertTrue(appDashboard.isArchivesRoomAppPresented());
 	}
     
     @Test
     public void createUserWithSysAdminRole() throws InterruptedException {
     	String username = createUser();
     	
 		loginPage.logIn("admin", "Admin123");
     	
     	assertTrue(appDashboard.isActiveVisitsAppPresented());
     	assertTrue(appDashboard.isFindAPatientAppPresented());
     	assertTrue(appDashboard.isSystemAdministrationAppPresented());
     	assertTrue(appDashboard.isPatientRegistrationAppPresented());
     	assertTrue(appDashboard.isArchivesRoomAppPresented());
     	
     	appDashboard.openSysAdminApp();
     	userAdmin.createSysAdminAccount("Test", "User", username, DEFAULT_PASSWORD);
     	
    	assertTrue(userAdmin.isAccountCreatedSuccessfully());
     	
     	logOutAndLogInWithNewUser(username);
     	
     	assertTrue(appDashboard.isActiveVisitsAppPresented());
     	assertTrue(appDashboard.isFindAPatientAppPresented());
     	assertTrue(appDashboard.isSystemAdministrationAppPresented());
     	assertTrue(appDashboard.isPatientRegistrationAppPresented());
     	assertTrue(appDashboard.isArchivesRoomAppPresented());
 	}
    
     @After
     public void tearDown() {
     	header.logOut();
     }
     
     /* TODO: atualizar para:
      * 1) apagar o usuário criado
      * 2) ter os usuários com papeis diferentes pré-determinados e só garantir que está mostrando os corretos
      */
     private String createUser() {
     	return new String("user" + System.currentTimeMillis());
     }
 
        
     private void logOutAndLogInWithNewUser(String username) {
     	header.logOut();
     	loginPage.logIn(username, DEFAULT_PASSWORD);
     }
 }
