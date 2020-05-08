 package edu.cmu.square.server.business.implementation;
 
 import static org.junit.Assert.fail;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.cmu.square.client.exceptions.ExceptionType;
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtArtifact;
 import edu.cmu.square.client.model.GwtAsquareCase;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.server.base.AbstractSpringBase;
 import edu.cmu.square.server.business.step.interfaces.CollectArtifactsBusiness;
 import edu.cmu.square.server.dao.model.AsquareCase;
 import edu.cmu.square.server.dao.model.Project;
 
 public class CollectArtifactsBusinessImplTest extends AbstractSpringBase
 {
 
 	@Resource
 	private CollectArtifactsBusiness business;
 
 	private GwtArtifact testArtifact1;
 	private GwtArtifact testArtifact2;
 	private GwtProject testProject;
 
 	private static class Data1
 	{
 		static public String name = "The Best Misuse case";
 		static public String description = "Misuse case of how to break this tool.  This might "
 				+ "include more meta inforamtion about the documentat htat is stored in the repository.";
 		static public String revision = "1.3";
 		static public String link = "http://blah.com/blah.docx";
 	}
 
 	private static class Data2
 	{
 		static public String name = "A Use Case";
 		static public String description = "This would be more meta inforamiton about the use case.";
 		static public String revision = "Latest and Greatest";
 		static public String link = "\\\\Shared\\Server\\uber.docx";
 	}
 
 	@Before
 	public void setUp() throws Exception
 	{
 		Map<String, Object> testMap = super.createUserProjectforRole();
 		this.testProject = ((Project) testMap.get("project")).createGwtProject();
 
 		this.testArtifact1 = new GwtArtifact(Data1.name, Data1.description, Data1.revision, Data1.link);
 		this.testArtifact2 = new GwtArtifact(Data2.name, Data2.description, Data2.revision, Data2.link);
 	}
 
 	@After
 	public void tearDown() throws Exception
 	{
 		this.testProject = null;
 		this.testArtifact1 = null;
 		this.testArtifact2 = null;
 	}
 
 	@Test
 	public void testGetArtifactsForProject()
 	{
 		try
 		{
 			this.business.createArtifact(this.testArtifact1, this.testProject.getId());
 
 			List<GwtArtifact> artifacts = this.business.getArtifactsForProject(this.testProject.getId());
 			Assert.assertEquals(1, artifacts.size());
 			Assert.assertTrue(artifacts.contains(this.testArtifact1));
 			Assert.assertFalse(artifacts.contains(this.testArtifact2));
 
 			this.business.createArtifact(this.testArtifact2, this.testProject.getId());
 			artifacts = this.business.getArtifactsForProject(this.testProject.getId());
 			Assert.assertEquals(2, artifacts.size());
 			Assert.assertTrue(artifacts.contains(this.testArtifact1));
 			Assert.assertTrue(artifacts.contains(this.testArtifact2));
 		}
 		catch (SquareException ex)
 		{
 			fail();
 		}
 	}
 	
 	@Test
 	public void testGetArtifactsForProject2()
 	{
 		try
 		{
 			GwtProject project2 = this.CreateProject();
 			Assert.assertTrue(project2.getId().intValue() != this.testProject.getId().intValue());
 			
 			this.business.createArtifact(this.testArtifact1, this.testProject.getId());
 			this.business.createArtifact(this.testArtifact2, project2.getId());
 
 			List<GwtArtifact> artifacts = this.business.getArtifactsForProject(this.testProject.getId());
 			Assert.assertEquals(1, artifacts.size());
 			Assert.assertTrue(artifacts.contains(this.testArtifact1));
 			Assert.assertFalse(artifacts.contains(this.testArtifact2));
 
 			List<GwtArtifact> artifacts2 = this.business.getArtifactsForProject(project2.getId());
 			Assert.assertEquals(1, artifacts2.size());
 			Assert.assertFalse(artifacts2.contains(this.testArtifact1));
 			Assert.assertTrue(artifacts2.contains(this.testArtifact2));
 		}
 		catch (SquareException ex)
 		{
 			fail();
 		}
 	}
 
 	
 	@Test
 	public void testGetArtifactsForProjectExceptionTest()
 	{
 		try
 		{
 			// this is some random integer that is presumed to be a bad project
 			// id.
 			this.business.createArtifact(this.testArtifact1, 999999);
 			fail(); // shouldn't get here
 		}
 		catch (SquareException ex)
 		{
 			Assert.assertEquals(ExceptionType.other, ex.getType());
 			return;
 		}
 
 	}
 
 	@Test
 	public void testCreateArtifact()
 	{
 		try
 		{
 			this.business.createArtifact(this.testArtifact1, this.testProject.getId());
 			this.business.createArtifact(this.testArtifact2, this.testProject.getId());
 
 			List<GwtArtifact> artifacts = this.business.getArtifactsForProject(this.testProject.getId());
 			Assert.assertEquals(2, artifacts.size());
 			Assert.assertTrue(artifacts.contains(this.testArtifact1));
 			Assert.assertTrue(artifacts.contains(this.testArtifact2));
 		}
 		catch (SquareException ex)
 		{
 			fail(ex.getMessage());
 		}
 	}
 
 	@Test
 	public void testDeleteArtifact()
 	{
 		try
 		{
 			this.business.createArtifact(this.testArtifact1, this.testProject.getId());
 			this.business.createArtifact(this.testArtifact2, this.testProject.getId());
 
 			this.business.deleteArtifact(this.testArtifact2.getId());
 
 			List<GwtArtifact> artifacts = this.business.getArtifactsForProject(this.testProject.getId());
 			
 			Assert.assertEquals(1, artifacts.size());
 			Assert.assertTrue(artifacts.contains(this.testArtifact1));
 			Assert.assertFalse(artifacts.contains(this.testArtifact2));
 
 			this.business.deleteArtifact(this.testArtifact1.getId());
 
 			artifacts = this.business.getArtifactsForProject(this.testProject.getId());
 			
 			Assert.assertEquals(0, artifacts.size());
 			Assert.assertFalse(artifacts.contains(this.testArtifact1));
 			Assert.assertFalse(artifacts.contains(this.testArtifact2));
 
 		}
 		catch (SquareException ex)
 		{
 			fail(ex.getMessage());
 		}
 	}
 
 	@Test
 	public void testUpdateArtifact()
 	{
 		try
 		{
 			this.business.createArtifact(this.testArtifact1, this.testProject.getId());
 			this.testArtifact1.setName(Data2.name);
 			this.testArtifact1.setDescription(Data2.description);
 			
 			this.business.updateArtifact(testArtifact1);
 
 			List<GwtArtifact> artifacts = this.business.getArtifactsForProject(this.testProject.getId());
 			Assert.assertEquals(1, artifacts.size());
 			Assert.assertEquals(Data2.name, artifacts.get(0).getName());
 			Assert.assertEquals(Data2.description, artifacts.get(0).getDescription());
 			Assert.assertEquals(Data1.revision, artifacts.get(0).getRevision());
 			Assert.assertEquals(Data1.link, artifacts.get(0).getLink());
 		}
 		catch (SquareException ex)
 		{
 			fail();
 		}
 	}
 
 	@Test
 	public void testUpdateArtifactException()
 	{
 		try
 		{
 			this.business.createArtifact(this.testArtifact1, this.testProject.getId());
 			this.business.createArtifact(this.testArtifact2, this.testProject.getId());
 			this.testArtifact2.setName(Data1.name);
 
 			this.business.updateArtifact(testArtifact2);
 
 			fail();
 
 		}
 		catch (SquareException ex)
 		{
 			Assert.assertEquals(ExceptionType.duplicateName, ex.getType());
 			return;
 		}
 	}
 	
 	
 	private GwtProject CreateProject()
 	{
 		Project testProject = new Project();
 		
 		testProject.setName("The second project!");
 		testProject.setLite(false);
 		testProject.setPrivacy(false);
 		testProject.setSecurity(true);
		testProject.setPrivacyTechniqueRationale("None");
		testProject.setSecurityTechniqueRationale("None");
 		testProject.setAcquisitionOrganizationEngineer(this.testUser);
 		testProject.setCases(new AsquareCase(new GwtAsquareCase(1)));
 		projectDao.create(testProject);
 		
 		return testProject.createGwtProject();
 	}
 	
 }
