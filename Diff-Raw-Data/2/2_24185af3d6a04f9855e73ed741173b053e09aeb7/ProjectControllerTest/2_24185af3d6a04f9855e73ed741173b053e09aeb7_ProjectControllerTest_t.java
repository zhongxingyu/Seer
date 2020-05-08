 package controller;
 
 import models.Project;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class ProjectControllerTest {
 
     private MockHttpServletResponse response;
     private MockHttpServletRequest request;
     private AnnotationMethodHandlerAdapter adapter;
     private ProjectController controller;
     private Project dummyProject;
     private models.ProjectDAO ProjectDAO;
 
     @Before
     public void setUp() {
         request = new MockHttpServletRequest();
         response = new MockHttpServletResponse();
         adapter = new AnnotationMethodHandlerAdapter();
         dummyProject = new Project("name", "description", "image/path");
 
        request.setRequestURI("/project_detail.ftl");
         request.setMethod("GET");
         request.setParameter("project_id", "1");
 
         controller = new ProjectController();
         ProjectDAO = mock(models.ProjectDAO.class);
         controller.setDao(ProjectDAO);
 
         when(ProjectDAO.fetch((long) 1)).thenReturn(dummyProject);
     }
 
     @Test
     public void shouldRenderTheProjectDetailsView() throws Exception {
         ModelAndView modelView = adapter.handle(request, response, controller);
         assertThat(modelView.getViewName(), is("project_detail"));
     }
 
     @Test
     public void shouldFetchAProjectBasedOnTheIDProvided() throws Exception {
         adapter.handle(request, response, controller);
         verify(ProjectDAO, Mockito.times(1)).fetch((long) 1);
     }
 
     @Test
     public void shouldExposeTheProjectFetchedToTheProjectDetailsView() throws Exception {
         ModelAndView modelView = adapter.handle(request, response, controller);
         ModelMap modelMap = (ModelMap) modelView.getModel().get("model");
         assertThat((Project)modelMap.get("project"), is(dummyProject));
     }
 }
