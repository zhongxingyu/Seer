 package org.sukrupa.student;
 
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.sukrupa.platform.config.SpringContextLoaderForTesting;
 import org.sukrupa.app.students.UpdateStudentPage;
 import org.sukrupa.app.students.ViewStudentPage;
 import org.sukrupa.platform.db.DatabaseHelper;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.is;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(loader = SpringContextLoaderForTesting.class)
 public class UpdateStudentTest {
 
     private Student shefali = new StudentBuilder().name("shefali").studentId("1234567").build();
 
     private WebDriver driver = new HtmlUnitDriver();
 
     @Autowired
     private DatabaseHelper databaseHelper;
 
     @After
     public void tearDown() throws Exception {
         databaseHelper.deleteAllCreatedObjects();
     }
 
     @Test
     public void shouldAddNotes() {
         databaseHelper.saveAndCommit(shefali);
         UpdateStudentPage page = new UpdateStudentPage(driver, shefali.getStudentId());
         page.addNote("new note");
         assertThat(page.getNoteAddedConfirmation(), is("Note Added Successfully"));
         ViewStudentPage viewPage = new ViewStudentPage(driver, shefali.getStudentId());
         assertThat(viewPage.getNotes(), hasItem("new note"));
     }
 
 }
