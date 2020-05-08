 package ca.ubc.ctlt.ipeerb2.integration;
 
 import static org.testng.AssertJUnit.assertFalse;
 import static org.testng.AssertJUnit.assertTrue;
 
 import java.util.Properties;
 import java.util.Set;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
 import org.openqa.selenium.support.events.WebDriverEventListener;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.Wait;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Listeners;
 import org.testng.annotations.Test;
 
 import ca.ubc.ctlt.ipeerb2.integration.page.CourseCreationPage;
 import ca.ubc.ctlt.ipeerb2.integration.page.CourseManagePage;
 import ca.ubc.ctlt.ipeerb2.integration.page.IPeerCourseHomePage;
 import ca.ubc.ctlt.ipeerb2.integration.page.LoginPage;
 
 @Listeners({ScreenshotOnFailureListener.class})
 @ContextConfiguration("functionalTestContext.xml")
 @ActiveProfiles({"dev", "integration"})
 public class CourseCreationTest extends BaseTest {
     @Autowired
     @Qualifier("courseData")
     protected Properties courseData;
 
     protected String courseId;
     protected int iPeerCourseId;
     protected TestHelper helper;
 
     @BeforeClass
     public void setUp() {
         WebDriverEventListener errorListener = new AbstractWebDriverEventListener() {
             @Override
             public void onException(Throwable throwable, WebDriver driver) {
                 takeScreenshot("iPeerB2");
             }
         };
 
 
         driver.register(errorListener);
 
         getDriver().manage().deleteAllCookies();
         getDriver().get(siteBase.toString());
         new LoginPage(getDriver(), siteBase).logon("administrator", "bblearn");
         helper = new TestHelper(driver, siteBase);
         helper.importUsers();
         courseId = helper.createCourse(courseData);
         helper.enrolUsers(courseId);
         helper.createGroups(courseId);
     }
 
     @AfterClass
     public void tearDown() {
         // clean up the course
         helper.deleteCourse(courseData);
     }
 
     @Test
     public final void testCreateCourse() {
         CourseCreationPage page = new CourseCreationPage(getDriver(), siteBase, courseId).get();
 
         assertTrue("Should be on course creation page.",
                 page.getText().contains("Creating Course in iPeer"));
 
         assertTrue("Course Name should be course ID",
                 page.getCourse().getAttribute("value").equals(courseData.getProperty("courseId")));
 
         assertTrue("Course Title should be course Name",
                 page.getTitle().getAttribute("value").equals(courseData.getProperty("courseName")));
 
         assertTrue("Push Class should be selected by default",
                 page.getSyncClass().isSelected());
 
         assertFalse("Push Group should not be selected",
                 page.getPushGroup().isSelected());
 
         assertFalse("Department should not be selected",
                 page.getDepartments1().isSelected());
         assertFalse("Department should not be selected",
                 page.getDepartments2().isSelected());
         assertFalse("Department should not be selected",
                 page.getDepartments3().isSelected());
 
         CourseManagePage managePage = page.createCourse("iPeer B2 Test Course", true, true, true, false, true);
         managePage.get();
 
         assertTrue("Should create iPeer course successfully",
                 page.getText().contains("Course " + courseData.getProperty("courseId") + " has been created successfully!"));
         assertTrue("Should sync classlist successfully",
                 page.getText().contains("Class list has been synced successfully!"));
         assertTrue("Should push groups successfully",
                 page.getText().contains("Groups have been synced successfully!"));
 
         // test bb course info
         // wait for ajax call to complete before asserts
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("bbcourse"), "Class Size"));
         WebElement bbInfo = driver.findElement(By.id("bbcourse"));
         assertTrue("Should have correct class size",
                 bbInfo.getText().contains("Class Size: " + (UserFixture.NUM_STUDENTS - 1)));
         assertTrue("Should have correct groups",
                 bbInfo.getText().contains("Empty Group (0 members)"));
         assertTrue("Should have correct groups",
                 bbInfo.getText().contains("Group 1 (3 members)"));
         assertTrue("Should have correct groups",
                 bbInfo.getText().contains("Group 2 (1 members)"));
 
         // test ipeer course info
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("ipeercourse"), "Class Size"));
         WebElement ipeerInfo = driver.findElement(By.id("ipeercourse"));
         assertTrue("Should have correct class size",
                 ipeerInfo.getText().contains("Class Size: " + (UserFixture.NUM_STUDENTS - 1)));
         assertTrue("Should have correct groups",
                 ipeerInfo.getText().contains("Empty Group (0 members)"));
         assertTrue("Should have correct groups",
                 ipeerInfo.getText().contains("Group 1 (3 members)"));
         assertTrue("Should have correct groups",
                 ipeerInfo.getText().contains("Group 2 (1 members)"));
     }
 
     @Test(dependsOnMethods = "testCreateCourse")
     public final void testSyncClass() {
         // get last user in fixture
         String[] user = UserFixture.USERS[UserFixture.USERS.length - 1];
         helper.enrolUser(courseId, user[3], user[4]);
 
         CourseManagePage page = new CourseManagePage(getDriver(), siteBase, courseId).get();
 
         // test bb course info
         // wait for ajax call to complete before asserts
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("bbcourse"), "Class Size"));
         WebElement bbInfo = driver.findElement(By.id("bbcourse"));
         assertTrue("Should have correct class size",
                 bbInfo.getText().contains("Class Size: " + UserFixture.NUM_STUDENTS));
 
         // test ipeer course info
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("ipeercourse"), "Class Size"));
         WebElement ipeerInfo = driver.findElement(By.id("ipeercourse"));
         assertTrue("Should have correct class size",
                 ipeerInfo.getText().contains("Class Size: " + (UserFixture.NUM_STUDENTS - 1)));
 
         page = page.syncClass().get();
 
         // test bb course info
         // wait for ajax call to complete before asserts
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("bbcourse"), "Class Size"));
         bbInfo = driver.findElement(By.id("bbcourse"));
         assertTrue("Should have correct class size",
                 bbInfo.getText().contains("Class Size: " + UserFixture.NUM_STUDENTS));
 
         // test ipeer course info
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("ipeercourse"), "Class Size"));
         ipeerInfo = driver.findElement(By.id("ipeercourse"));
         assertTrue("Should have correct class size",
                 ipeerInfo.getText().contains("Class Size: " + UserFixture.NUM_STUDENTS));
     }
 
     @Test(dependsOnMethods = "testSyncClass")
     public final void testPushGroup() {
         helper.enrolGroup(courseId, "Group 2", UserFixture.USERS[5]);
 
         CourseManagePage page = new CourseManagePage(getDriver(), siteBase, courseId).get();
 
         // test bb course info
         // wait for ajax call to complete before asserts
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("bbcourse"), "Class Size"));
         WebElement bbInfo = driver.findElement(By.id("bbcourse"));
         assertTrue("Should have correct groups",
                 bbInfo.getText().contains("Group 2 (2 members)"));
 
         // test ipeer course info
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("ipeercourse"), "Class Size"));
         WebElement ipeerInfo = driver.findElement(By.id("ipeercourse"));
         assertTrue("Should have correct groups",
                 ipeerInfo.getText().contains("Group 2 (1 members)"));
 
         page = page.pushGroups().get();
 
         // test bb course info
         // wait for ajax call to complete before asserts
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("bbcourse"), "Class Size"));
         bbInfo = driver.findElement(By.id("bbcourse"));
         assertTrue("Should have correct groups",
                 bbInfo.getText().contains("Group 2 (2 members)"));
 
         // test ipeer course info
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("ipeercourse"), "Class Size"));
         ipeerInfo = driver.findElement(By.id("ipeercourse"));
         assertTrue("Should have correct groups",
                 ipeerInfo.getText().contains("Group 2 (2 members)"));
     }
 
     @Test(dependsOnMethods = "testPushGroup")
    public final void testManageCourse() {
         CourseManagePage page = new CourseManagePage(getDriver(), siteBase, courseId).get();
         String currentWindow = driver.getWindowHandle();
 
         IPeerCourseHomePage iPeerPage = page.manageiPeerCourse().get();
 
         Set<String> handles = driver.getWindowHandles();
         for (String handle : handles) {
             if (!handle.equals(currentWindow)) {
                 driver.switchTo().window(handle);
                 assertTrue("Should have the same class size in iPeer",
                         iPeerPage.getText().contains(UserFixture.NUM_STUDENTS + " students"));
                 assertTrue("Should have the same group size in iPeer",
                         iPeerPage.getText().contains("3 groups"));
                 assertTrue("Should have Blackboard Administrator in iPeer as instructor",
                         iPeerPage.getText().contains("Blackboard Administrator"));
                 assertTrue("Should have Instructor iPeer in iPeer",
                         iPeerPage.getText().contains("Instructor iPeer"));
 
                 String[] user1 = UserFixture.USERS[6];
                 String[] user2 = UserFixture.USERS[7];
                 iPeerPage.createGroup("iPeer Group", new String[]{
                         user1[5] + " " + user1[0] + " " + user1[1],
                         user2[5] + " " + user2[0] + " " + user2[1]
                 });
 
                 driver.close();
                 driver.switchTo().window(currentWindow);
             }
         }
     }
 
    @Test(dependsOnMethods = "testManageCourse")
     public final void testPullGroups() {
         CourseManagePage page = new CourseManagePage(getDriver(), siteBase, courseId).get();
        // refresh the page to get the latest group info
         driver.navigate().refresh();
 
         // test bb course info
         // wait for ajax call to complete before asserts
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("bbcourse"), "Class Size"));
         WebElement bbInfo = driver.findElement(By.id("bbcourse"));
         assertTrue("Should have correct groups",
                 !bbInfo.getText().contains("iPeer Group"));
 
         // test ipeer course info
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("ipeercourse"), "Class Size"));
         WebElement ipeerInfo = driver.findElement(By.id("ipeercourse"));
         assertTrue("Should have correct groups",
                 ipeerInfo.getText().contains("iPeer Group (2 members)"));
 
         page = page.pullGroups().get();
 
         // test bb course info
         // wait for ajax call to complete before asserts
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("bbcourse"), "Class Size"));
         bbInfo = driver.findElement(By.id("bbcourse"));
         assertTrue("Should have correct groups",
                 bbInfo.getText().contains("iPeer Group (2 members)"));
 
         // test ipeer course info
         (new WebDriverWait(driver, 10)).until(ExpectedConditions.textToBePresentInElement(By.id("ipeercourse"), "Class Size"));
         ipeerInfo = driver.findElement(By.id("ipeercourse"));
         assertTrue("Should have correct groups",
                 ipeerInfo.getText().contains("iPeer Group (2 members)"));
     }
 
     @Test(dependsOnMethods = "testPullGroups")
     public final void testDisconnectCourse() {
         CourseManagePage page = new CourseManagePage(getDriver(), siteBase, courseId).get();
         iPeerCourseId = page.getiPeerCourseId();
         CourseCreationPage creationPage = page.disconnectCourse();
         creationPage.get();
 
         assertTrue("Should be on course creation page.",
                 creationPage.getText().contains("Creating Course in iPeer"));
         assertTrue("Should disconnect course successfully",
                 page.getText().contains("Course has been disconnected successfully!"));
 
     }
 
     @Test(dependsOnMethods = "testDisconnectCourse")
     public final void testLinkCourse() {
         CourseCreationPage page = new CourseCreationPage(getDriver(), siteBase, courseId).get();
         CourseManagePage managePage = page.linkCourse(iPeerCourseId).get();
 
         assertTrue("Should be on course manage page.",
                 managePage.getText().contains("Managing iPeer Course Connection"));
         assertTrue("Should link the course successfully",
                 page.getText().contains("Course has been linked successfully!"));
     }
 
 
     @Test(dependsOnMethods = "testLinkCourse")
     public final void testStudentModule() {
         CourseManagePage managePage = new CourseManagePage(driver, siteBase, courseId).get();
 
         IPeerCourseHomePage page = managePage.manageiPeerCourse().get();
 
         String bbWindow = page.switchWindowTo();
 
         page.createOpenEvent("B2 Test Event", "SIMPLE", "Module 1 Project Evaluation",
                 new String[]{"Group 1", "Group 2"});
 
         page.closeWindow(bbWindow);
 
         helper.addModule(courseId);
 
         managePage.logout();
         // login as student
         new LoginPage(getDriver(), siteBase).logon(UserFixture.USERS[1][3], "12345678");
 
         driver.get(siteBase + "webapps/portal/frameset.jsp?tab_tab_group_id=_2_1&url=%2Fwebapps%2Fblackboard%2Fexecute%2Flauncher%3Ftype%3DCourse%26id%3D" + courseId + "%26url%3D");
         driver.switchTo().frame("contentFrame");
         Wait<WebDriver> wait = new WebDriverWait(driver, 10);
         wait.until(ExpectedConditions.textToBePresentInElement(By.tagName("body"), "B2 Test Event"));
         WebElement body = driver.findElement(By.tagName("body"));
         assertTrue("Should have iPeer Events module", body.getText().contains("iPeer Events"));
         assertTrue("Should have the new event listed", body.getText().contains("B2 Test Event"));
 
         // make sure student can be transfered to iPeer
         driver.findElement(By.linkText("B2 Test Event")).click();
         bbWindow = page.switchWindowTo();
         body = driver.findElement(By.tagName("body"));
         assertTrue("Should login student into ipeer", body.getText().contains("Peer Evaluations"));
         assertTrue("Should have event in ipeer", body.getText().contains("B2 Test Event"));
         page.closeWindow(bbWindow);
 
         managePage.logout();
         // login as instructor
         new LoginPage(getDriver(), siteBase).logon("administrator", "bblearn");
     }
 
     @Test(dependsOnMethods = "testStudentModule")
     public final void testGradeSync() {
         driver.manage().window().maximize();
         CourseManagePage managePage = new CourseManagePage(driver, siteBase, courseId).get();
         iPeerCourseId = managePage.getiPeerCourseId();
         CourseCreationPage page = managePage.disconnectCourse().get();
         // link the iPeer course with grade info
         managePage = page.linkCourse(1).get();
 
         assertTrue("Should link the course successfully",
                 managePage.getText().contains("Course has been linked successfully!"));
 
         managePage = managePage.syncGrade();
         managePage.get();
 
         assertTrue("Should sync grade successfully",
                 managePage.getText().contains("Grades have been synced successfully!"));
 
         driver.get(siteBase + "webapps/portal/frameset.jsp?tab_tab_group_id=_2_1&url=%2Fwebapps%2Fblackboard%2Fexecute%2Flauncher%3Ftype%3DCourse%26id%3D"+courseId+"%26url%3D");
         driver.switchTo().frame("contentFrame");
         driver.findElement(By.linkText("Grade Center")).click();
         Wait<WebDriver> wait = new WebDriverWait(driver, 10);
         wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Full Grade Center"))).click();
 
         wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cell_0_1")));
         WebElement body = driver.findElement(By.id("nonAccessibleTableDiv"));
 
         assertTrue("Should have event Term 1 Evaluation",
                 body.getText().contains("Term 1 Evaluation"));
 
         assertTrue("Should have event Term Report Evaluation",
                 body.getText().contains("Term Report Evaluation"));
 
         assertTrue("Should have event Project Evaluation",
                 body.getText().contains("Project Evaluation"));
 
         assertTrue("Should have event simple evaluation 2",
                 body.getText().contains("simple evaluation 2"));
 
         assertTrue("Should have total score for Alex Student",
                 body.getText().contains("312.40"));
 
         assertTrue("Should have total score for Bowinn Student",
                 body.getText().contains("143.00"));
 
         assertTrue("Should have total score for Joe Student",
                 body.getText().contains("91.60"));
 
         page = managePage.get().disconnectCourse();
         page.get().linkCourse(iPeerCourseId);
     }
 
     @Test(dependsOnMethods = "testGradeSync", alwaysRun = true)
     public final void testDeleteCourse() {
         CourseManagePage page = new CourseManagePage(getDriver(), siteBase, courseId).get();
         CourseCreationPage creationPage = page.deleteCourse().get();
 
         assertTrue("Should be on course creation page.",
                 creationPage.getText().contains("Creating Course in iPeer"));
         assertTrue("Should delete the course successfully",
                 page.getText().contains("Course has been deleted successfully!"));
     }
 }
