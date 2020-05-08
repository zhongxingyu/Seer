 package com.teamdev.projects.test;
 
 import com.teamdev.projects.test.data.*;
 import com.teamdev.projects.test.web.page.AppPage;
 import com.teamdev.projects.test.web.page.LoginPage;
 import com.teamdev.projects.test.web.businessflow.TheProjects;
 
 import static com.teamdev.projects.test.util.StringUtil.incrementDateOn;
 import static com.teamdev.projects.test.util.StringUtil.randomizeName;
 import static com.teamdev.projects.test.webdriver.WebDriverManager.startDriver;
 
 /**
  * @author Alexander Orlov
  */
 public abstract class AbstractTest {
 
     protected TheProjects theProjects;
 
     protected AreaData randomArea() {
         return new AreaData(randomizeName("Area"), randomizeName("Description"));
     }
 
     protected ProjectData randomProject() {
         return new ProjectData(randomizeName("Short"), randomizeName("Full"));
     }
 
     protected TaskData randomTask() {
         return new TaskData(randomizeName("Task"));
     }
 
     protected TaskData randomTaskWithSchedule() {
         return new TaskData(randomizeName("Task"), incrementDateOn(10));
     }
 
     protected String tagName = randomizeName("Tag");
     protected String childTagName = randomizeName("ChildTag");
     protected String newTagName = randomizeName("Renamed tag");
 
     protected UserData mainUser = new UserData("alexander.orlov@teamdev.tv", "sepulki3");
     protected UserData secondUser = new UserData("projects.administrator@teamdev.tv" , "GetMyTasksDONE");
     protected UserData thirdUser = new UserData("sergii.moroz@teamdev.com");
     protected PersonData projectAdministrator = new PersonData("projects.administrator@teamdev.tv", "Project Administrator");
 
 
     protected TheProjects setup(){
             LoginPage loginPage = new LoginPage();
             startDriver();
            AppPage appPage = loginPage.loginLocally(thirdUser);
             return new TheProjects(appPage);
         }
 }
