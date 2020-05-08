 
 package runThrghTestNG;
 
 import org.testng.Reporter;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Listeners;
 import org.testng.annotations.Test;
 import smoketest.Actions;
 
 
 @Listeners({runThrghTestNG.TestNGCustomReport.class})
 public class TempTest extends BaseClass {
     
     Actions a = new Actions();
     
     @BeforeClass
     public void testPESAdminLgn() throws Exception {
        a.login("pesAdmin");
     }
     @Test
     public void stuff() throws Exception {
    String check = "balls";
    a.navigateToWorkingGroups();
    a.accessWrknGrp(check);
    //a.addMbrsToWrkngGrp(tchrUsrName, stdtUsrName);
     }   
 }
