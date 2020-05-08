 package com.crowdplatform.service;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.query.Query;
 
 import com.crowdplatform.model.Project;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ProjectServiceMongoImplTest {
 
 	@InjectMocks
 	private ProjectServiceMongoImpl service = new ProjectServiceMongoImpl();
 	
 	@Mock
 	private MongoOperations mongoOperation;
 	
 	private static final String projectId = "projectId";
 	
 	@Before
 	public void setUp() {
 	    MockitoAnnotations.initMocks(this);
 	}
 	
 	@Test
 	public void testAddProject() {
 		Project project = new Project();
 		
 		service.addProject(project);
 		
 		Mockito.verify(mongoOperation).save(project);
 	}
 	
 	@Test
 	public void testSaveProject() {
 		Project project = new Project();
 		
 		service.saveProject(project);
 		
 		Mockito.verify(mongoOperation).save(project);
 	}
 	
 	@Test
 	public void testRemoveExistingProject() {
 		Project project = new Project();
 		Mockito.when(mongoOperation.findById(projectId, Project.class)).thenReturn(project);
 		
 		service.removeProject(projectId);
 		
 		Mockito.verify(mongoOperation).remove(project);
 	}
 	
 	@Test
 	public void testRemoveNonExistingProject() {
 		service.removeProject(projectId);
 		
 		Mockito.verify(mongoOperation, Mockito.never()).remove(Mockito.any(Project.class));
 	}
 	
 	@Test
 	public void testGetProject() {
 		service.getProject(projectId);
 		
 		Mockito.verify(mongoOperation).findById(projectId, Project.class);
 	}
 	
 	@Test
 	public void testGetProjectsForUser() {
 		service.getProjectsForUser("user");
 		
		Mockito.verify(mongoOperation).find(Mockito.any(Query.class), Mockito.eq(Project.class));
 	}
 }
