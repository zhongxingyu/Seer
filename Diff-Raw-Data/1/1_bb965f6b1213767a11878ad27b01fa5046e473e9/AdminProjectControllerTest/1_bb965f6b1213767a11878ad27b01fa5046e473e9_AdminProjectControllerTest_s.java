 package com.mike.controller;
 
 import com.mike.domain.Project;
 import com.mike.service.ProjectService;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.matches;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 /**
  * Created with IntelliJ IDEA.
  * <p/>
  * User: mike
  * Date: 2/2/13
  * Time: 4:28 PM
  */
 @RunWith(MockitoJUnitRunner.class)
 public class AdminProjectControllerTest
 {
     @InjectMocks
     private AdminProjectController adminProjectController = new AdminProjectController();
 
     @Mock
     private ProjectService projectService;
 
     @Test
     public void testListProjects() throws Exception
     {
         String result = adminProjectController.listProjects();

         assertEquals("admin", result);
     }
 
     @Test
     public void testNewProjectForm() throws Exception
     {
         Project emptyProject = new Project();
 
         ModelMap modelMap = new ModelMap();
         String result = adminProjectController.newProjectForm(modelMap);
 
         assertEquals(emptyProject, modelMap.get("project"));
         assertEquals("project.new", result);
     }
 
     @Test
     public void testNewProjectHasErrors() throws Exception
     {
         BindingResult bindingResult = mock(BindingResult.class);
         when(bindingResult.hasErrors()).thenReturn(true);
 
         Project project = new Project();
         String result = adminProjectController.newProject(project, bindingResult);
 
         assertEquals("project.new", result);
     }
 
     @Test
     public void testNewProjectNoErrors() throws Exception
     {
         BindingResult bindingResult = mock(BindingResult.class);
         when(bindingResult.hasErrors()).thenReturn(false);
 
         Project project = new Project();
         project.setName("Fred");
         project.setDescription("Descr");
         String result = adminProjectController.newProject(project, bindingResult);
 
         assertEquals("admin", result);
 
         verify(projectService).saveProject(project);
     }
 }
