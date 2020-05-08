 package edu.cmu.square.server.business.implementation;
 
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtTerm;
 import edu.cmu.square.server.base.AbstractSpringBase;
 import edu.cmu.square.server.business.step.interfaces.AgreeOnDefinitionsBusiness;
 import edu.cmu.square.server.dao.model.Project;
 
 public class AgreeOnDefinitionsBusinessImplTest extends AbstractSpringBase
 {
 
 	private GwtProject testProject;
 	@Resource
 	private AgreeOnDefinitionsBusiness agreeOnDefinitionsBusiness;
 
 	@Before
 	public void setupTest()
 	{
 		Map<String, Object> testMap = super.createUserProjectforRole();
 		testProject = ((Project) testMap.get("project")).createGwtProject();
 	}
 	@Test
 	public void testloadDefaultTerms()
 	{
 		try
 		{
 			agreeOnDefinitionsBusiness.loadDefaultTerms(testProject.getId(), createDefaultTerms());
 			
 			List<GwtTerm> terms = agreeOnDefinitionsBusiness.getTerms(new GwtProject(testProject.getId()));
 			
 			Assert.assertEquals(5, terms.size()); //This is the amount of terms in the XML file.
 
 		}
 		catch (SquareException e)
 		{
			fail("error" + e);

 		}
 	}
 	
 
 	@Test
 	public void testAddTerm()
 	{
 		try
 		{
 			GwtTerm gwtTerm = new GwtTerm();
 			gwtTerm.setTerm("Marco");
 			gwtTerm.setDefinition("Marco is sleeping when co-piloting.");
 
 			GwtProject gwtProject = testProject;
 
 			agreeOnDefinitionsBusiness.addTerm(gwtProject, gwtTerm);
 
 			List<GwtTerm> terms = agreeOnDefinitionsBusiness.getTerms(gwtProject);
 			assertTrue(terms.size() == 1);
 		}
 		catch (SquareException e)
 		{
			fail("error" + e);

 		}
 	}
 	@Test
 	public void testRemoveTerm()
 	{
 		try
 		{
 			GwtTerm gwtTerm = new GwtTerm();
 			gwtTerm.setTerm("Marco");
 			gwtTerm.setDefinition("Marco is sleeping when co-piloting.");
 
 			GwtTerm gwtTerm2 = new GwtTerm();
 			gwtTerm2.setTerm("Marco");
 			gwtTerm2.setDefinition("Marco is sleeping when co-piloting.");
 
 			GwtProject gwtProject = testProject;
 
 			gwtTerm = agreeOnDefinitionsBusiness.addTerm(gwtProject, gwtTerm);
 			gwtTerm2 = agreeOnDefinitionsBusiness.addTerm(gwtProject, gwtTerm2);
 			List<GwtTerm> terms = agreeOnDefinitionsBusiness.getTerms(gwtProject);
 			assertTrue(terms.size() == 2);
 
 			agreeOnDefinitionsBusiness.removeTerm(gwtTerm);
 
 			terms = agreeOnDefinitionsBusiness.getTerms(gwtProject);
 			assertTrue(terms.size() == 1);
 
 		}
 		catch (SquareException e)
 		{
			fail("error" + e);

 		}
 	}
 	@Test
 	public void testUpdateTerm()
 	{
 		try
 		{
 			GwtTerm gwtTerm = new GwtTerm();
 			gwtTerm.setTerm("Marco");
 			gwtTerm.setDefinition("Marco is sleeping when co-piloting.");
 
 			GwtProject gwtProject = testProject;
 
 			gwtTerm = agreeOnDefinitionsBusiness.addTerm(gwtProject, gwtTerm);
 			gwtTerm.setTerm("Finish it!!!");
 			gwtTerm.setDefinition("Marco want to code");
 			agreeOnDefinitionsBusiness.updateTerm(gwtProject, gwtTerm);
 
 			List<GwtTerm> terms = agreeOnDefinitionsBusiness.getTerms(gwtProject);
 			terms = agreeOnDefinitionsBusiness.getTerms(gwtProject);
 			assertTrue(terms.get(0).getTerm().equals(gwtTerm.getTerm()));
 
 		}
 		catch (SquareException e)
 		{
			fail("error" + e);

 		}
 	}
 
 }
