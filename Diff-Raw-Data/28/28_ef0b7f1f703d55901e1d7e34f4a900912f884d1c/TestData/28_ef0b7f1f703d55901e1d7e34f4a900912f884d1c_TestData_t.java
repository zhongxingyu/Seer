 package com.teamdev.projects.test;
 
 import com.teamdev.projects.test.data.AreaData;
 import com.teamdev.projects.test.data.ProjectData;
 import com.teamdev.projects.test.data.TaskData;
 import com.teamdev.projects.test.data.UserData;
 import com.teamdev.projects.test.web.businessflow.GeneralFlow;
 import com.teamdev.projects.test.web.page.AppPage;
 import com.teamdev.projects.test.web.page.LoginPage;
 
 import static com.teamdev.projects.test.util.Date.setDateWithIncrementOn;
 import static com.teamdev.projects.test.util.Strings.randomizeName;
 import static com.teamdev.projects.test.webdriver.WebDriverManager.startDriver;
 import static com.teamdev.projects.test.webdriver.WebDriverManager.stopDriver;
 
 /**
  * @author Alexander Orlov
  */
 public abstract class TestData {
 
    public static final String DEFAULT_RESOLUTION = "1024,768";
     protected GeneralFlow generalFlow;
 
     protected AreaData randomArea() {
         return new AreaData(randomizeName("Area"), randomizeName("Description"));
     }
 
     protected ProjectData randomProject() {
         return new ProjectData(randomizeName("Short"), randomizeName("Full"), randomizeName("Description"));
     }
 
     protected TaskData randomTask() {
         return new TaskData(randomizeName("Task"));
     }
 
     protected TaskData randomTaskWithSchedule() {
         return new TaskData(randomizeName("Task"), setDateWithIncrementOn(10));
     }
 
     protected String tagName = randomizeName("Tag");
     protected String childTagName = randomizeName("ChildTag");
     protected String newTagName = randomizeName("Renamed tag");
 
     protected UserData mainUser = new UserData("john.doe@teamdev.tv", "johnDOE2013", "John Doe");
     protected UserData secondUser = new UserData("projects.administrator@teamdev.tv" , "", "Projects Administrator");
     protected UserData thirdUser = new UserData("william.woe@teamdev.tv", "", "William Woe");
 
     protected GeneralFlow setup(){
        return setup(DEFAULT_RESOLUTION, mainUser);
         }
 
     protected GeneralFlow setup(String resolution){
         return setup(resolution, mainUser);
     }
 
     protected GeneralFlow setup(UserData asUser){
        return setup(DEFAULT_RESOLUTION, asUser);
     }
 
     protected GeneralFlow setup(String resolution, UserData asUser){
         LoginPage loginPage = new LoginPage();
         startDriver(resolution);
        AppPage appPage = loginPage.loginAs(asUser);
         return new GeneralFlow(appPage);
 
     }
 
     protected GeneralFlow restart(String resolution, UserData asUser){
         stopDriver();
         return setup(resolution, asUser);
     }
 
     protected GeneralFlow restart(UserData asUser){
        return restart(DEFAULT_RESOLUTION, asUser);
     }
 
     protected GeneralFlow restart(){
        return setup(DEFAULT_RESOLUTION, mainUser);
     }
 
 
 
 }
 
