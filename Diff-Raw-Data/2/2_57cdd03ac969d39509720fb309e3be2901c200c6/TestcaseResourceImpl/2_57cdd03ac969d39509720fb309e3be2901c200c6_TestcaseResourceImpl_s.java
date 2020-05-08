 package org.tcrun.slickij.data;
 
 import com.google.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 import org.bson.types.ObjectId;
 import org.tcrun.slickij.api.TestcaseResource;
 import org.tcrun.slickij.api.data.Component;
 import org.tcrun.slickij.api.data.DataExtension;
 import org.tcrun.slickij.api.data.InvalidDataError;
 import org.tcrun.slickij.api.data.Project;
 import org.tcrun.slickij.api.data.Testcase;
 import org.tcrun.slickij.api.data.testqueries.BelongsToComponent;
 import org.tcrun.slickij.api.data.testqueries.BelongsToProject;
 import org.tcrun.slickij.api.data.testqueries.ContainsTags;
 import org.tcrun.slickij.api.data.testqueries.FieldContains;
 import org.tcrun.slickij.api.data.testqueries.FieldEquals;
 import org.tcrun.slickij.api.data.testqueries.IsAutomated;
 import org.tcrun.slickij.api.data.testqueries.IsNotAutomated;
 import org.tcrun.slickij.api.data.testqueries.RequireAll;
 import org.tcrun.slickij.api.data.testqueries.TestcaseQuery;
 import org.tcrun.slickij.api.data.dao.ProjectDAO;
 import org.tcrun.slickij.api.data.dao.TestcaseDAO;
 
 /**
  *
  * @author jcorbett
  */
 public class TestcaseResourceImpl implements TestcaseResource
 {
 	private TestcaseDAO m_testcaseDAO;
 	private ProjectDAO m_projectDAO;
 
 	@Inject
 	public TestcaseResourceImpl(TestcaseDAO p_testcaseDAO, ProjectDAO p_projectDAO)
 	{
 		m_testcaseDAO = p_testcaseDAO;
 		m_projectDAO = p_projectDAO;
 	}
 
 	@Override
 	public List<Testcase> getMatchingTestcases(UriInfo uriInfo)
 	{
 		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
 		RequireAll query = new RequireAll();
 		query.setCriteria(new ArrayList<TestcaseQuery>());
 		if(queryParameters.containsKey("projectid"))
 		{
 			BelongsToProject crit = new BelongsToProject();
 			crit.setProjectId(new ObjectId(queryParameters.getFirst("projectid")));
 			query.getCriteria().add(crit);
 		}
 
 		if(queryParameters.containsKey("componentid"))
 		{
 			BelongsToComponent crit = new BelongsToComponent();
 			crit.setComponentId(new ObjectId(queryParameters.getFirst("componentid")));
 			query.getCriteria().add(crit);
 		}
 
 		if(queryParameters.containsKey("automationKey"))
 		{
 			FieldEquals crit = new FieldEquals();
 			crit.setFieldName("automationKey");
 			crit.setFieldValue(queryParameters.getFirst("automationKey"));
 			query.getCriteria().add(crit);
 		}
 
 		if(queryParameters.containsKey("automationId"))
 		{
 			FieldEquals crit = new FieldEquals();
 			crit.setFieldName("automationId");
 			crit.setFieldValue(queryParameters.getFirst("automationId"));
 			query.getCriteria().add(crit);
 		}
 
 		if(queryParameters.containsKey("automationTool"))
 		{
 			FieldEquals crit = new FieldEquals();
 			crit.setFieldName("automationTool");
 			crit.setFieldValue(queryParameters.getFirst("automationTool"));
 			query.getCriteria().add(crit);
 		}
 
 		if(queryParameters.containsKey("tag"))
 		{
 			ContainsTags crit = new ContainsTags();
 			crit.setTagnames(queryParameters.get("tag"));
 			query.getCriteria().add(crit);
 		}
 
 		if(queryParameters.containsKey("automated"))
 		{
 			if(queryParameters.getFirst("automated").equalsIgnoreCase("true"))
 				query.getCriteria().add(new IsAutomated());
 			else
 				query.getCriteria().add(new IsNotAutomated());
 		}
 
 		if(queryParameters.containsKey("author"))
 		{
 			FieldEquals crit = new FieldEquals();
 			crit.setFieldName("author");
 			crit.setFieldValue(queryParameters.getFirst("author"));
 			query.getCriteria().add(crit);
 		}
 
 		if(queryParameters.containsKey("namecontains"))
 		{
 			FieldContains crit = new FieldContains();
 			crit.setFieldName("name");
 			crit.setFieldValue(queryParameters.getFirst("namecontains"));
 			query.getCriteria().add(crit);
 		}
 
 		if(queryParameters.containsKey("name"))
 		{
 			FieldEquals crit = new FieldEquals();
 			crit.setFieldName("name");
 			crit.setFieldValue(queryParameters.getFirst("name"));
 			query.getCriteria().add(crit);
 		}
 
 		return m_testcaseDAO.findTestsByTestcaseQuery(query);
 	}
 
 	@Override
 	public Testcase addNewTestcase(Testcase testcase)
 	{
 		try
 		{
 			testcase.validate();
 		} catch(InvalidDataError error)
 		{
 			throw new WebApplicationException(error, Status.BAD_REQUEST);
 		}
 		Project project = m_projectDAO.findByReference(testcase.getProject());
 		if(project == null)
 			throw new WebApplicationException(new InvalidDataError("Testcase", "project", "cannot find project."), Status.BAD_REQUEST);
 		if(testcase.getComponent() != null)
 		{
 			Component component = project.findComponentByReference(testcase.getComponent());
 			if(component == null)
 				throw new WebApplicationException(new InvalidDataError("Testcase", "component", "unable to find component."));
 		}
 		m_testcaseDAO.save(testcase);
 		return testcase;
 	}
 
 	@Override
 	public Testcase getTestcase(String testcaseId)
 	{
 		Testcase retval = null;
 		try
 		{
 			retval = m_testcaseDAO.get(new ObjectId(testcaseId));
 		} catch(RuntimeException ex)
 		{
 			throw new WebApplicationException(ex, Status.BAD_REQUEST);
 		}
 		if(retval == null)
 			throw new NotFoundError(Testcase.class, testcaseId);
 		return retval;
 	}
 
 	@Override
 	public Testcase updateTestcase(String testcaseId, Testcase updatedTestcase)
 	{
 		Testcase real = getTestcase(testcaseId);
 		if(updatedTestcase.getAttributes() != null)
 			real.setAttributes(updatedTestcase.getAttributes());
 		if(updatedTestcase.getAuthor() != null && !updatedTestcase.getAuthor().equals(""))
 			real.setAuthor(updatedTestcase.getAuthor());
 		if(updatedTestcase.getAutomationConfiguration() != null && !updatedTestcase.getAutomationConfiguration().equals(""))
 			real.setAutomationConfiguration(updatedTestcase.getAutomationConfiguration());
 		if(updatedTestcase.getAutomationId() != null && !updatedTestcase.getAutomationId().equals(""))
 			real.setAutomationId(updatedTestcase.getAutomationId());
 		if(updatedTestcase.getAutomationKey() != null && !updatedTestcase.getAutomationKey().equals(""))
 			real.setAutomationKey(updatedTestcase.getAutomationKey());
 		if(updatedTestcase.getAutomationTool() != null && !updatedTestcase.getAutomationTool().equals(""))
 			real.setAutomationTool(updatedTestcase.getAutomationTool());
 		if(updatedTestcase.getAutomationPriority() > 0 && updatedTestcase.getAutomationPriority() <= 100)
 			real.setAutomationPriority(updatedTestcase.getAutomationPriority());
 		if(updatedTestcase.getProject() != null && real.getProject().equals(updatedTestcase.getProject()))
 		{
 			Project project = m_projectDAO.findByReference(updatedTestcase.getProject());
 			if(project != null)
 			{
 				real.setProject(updatedTestcase.getProject());
				if(project.findComponentByReference(real.getComponent()) == null)
 					real.setComponent(null);
 			}
 		}
 		if(updatedTestcase.getComponent() != null)
 		{
 			Project project = m_projectDAO.findByReference(real.getProject());
 			Component component = project.findComponentByReference(updatedTestcase.getComponent());
 			if(component != null)
 				real.setComponent(updatedTestcase.getComponent());
 		}
 		if(updatedTestcase.getDataDriven() != null)
 			real.setDataDriven(updatedTestcase.getDataDriven());
 		if(updatedTestcase.getName() != null && !updatedTestcase.getName().equals(""))
 			real.setName(updatedTestcase.getName());
 		if(updatedTestcase.getPurpose() != null && !updatedTestcase.getPurpose().equals(""))
 			real.setPurpose(updatedTestcase.getPurpose());
 		if(updatedTestcase.getRequirements() != null && !updatedTestcase.getRequirements().equals(""))
 			real.setRequirements(updatedTestcase.getRequirements());
 		if(updatedTestcase.getSteps() != null)
 			real.setSteps(updatedTestcase.getSteps());
 		if(updatedTestcase.getTags() != null)
 			real.setTags(updatedTestcase.getTags());
 		if(updatedTestcase.isAutomated() != null)
 			real.setAutomated(updatedTestcase.isAutomated());
 
 		m_testcaseDAO.save(real);
 		return real;
 	}
 
 	@Override
 	public Testcase deleteTestcase(String testcaseId)
 	{
 		Testcase testcase = getTestcase(testcaseId);
 		testcase.setDeleted(true);
 		m_testcaseDAO.save(testcase);
 		return testcase;
 	}
 
 	@Override
 	public DataExtension<Testcase> addDataExtension(String testcaseId, DataExtension<Testcase> dataExtension)
 	{
 		Testcase testcase = getTestcase(testcaseId);
 		dataExtension.setParent(testcase);
 		try
 		{
 			dataExtension.validate();
 		} catch(InvalidDataError error)
 		{
 			throw new WebApplicationException(error, Status.BAD_REQUEST);
 		}
 		testcase.getExtensions().add(dataExtension);
 		m_testcaseDAO.save(testcase);
 		return dataExtension;
 	}
 
 	protected DataExtension<Testcase> findDataExtension(Testcase tc, String extensionId)
 	{
 		DataExtension<Testcase> real = null;
 		for(DataExtension<Testcase> potential : tc.getExtensions())
 		{
 			if(potential.getId().equals(extensionId))
 			{
 				real = potential;
 				break;
 			}
 		}
 		if(real == null)
 			throw new NotFoundError(DataExtension.class, extensionId);
 		return real;
 	}
 
 	@Override
 	public DataExtension<Testcase> updateDataExtension(String testcaseId, String extensionId, DataExtension<Testcase> dataExtension)
 	{
 		Testcase testcase = getTestcase(testcaseId);
 		DataExtension<Testcase> real = findDataExtension(testcase, extensionId);
 		real.update(dataExtension);
 		m_testcaseDAO.save(testcase);
 		return real;
 	}
 
 	@Override
 	public List<DataExtension<Testcase>> deleteDataExtension(String testcaseId, String extensionId)
 	{
 		Testcase testcase = getTestcase(testcaseId);
 		DataExtension<Testcase> real = findDataExtension(testcase, extensionId);
 		testcase.getExtensions().remove(real);
 		m_testcaseDAO.save(testcase);
 		return testcase.getExtensions();
 	}
 
 	@Override
 	public List<Testcase> getMatchingTestcases(TestcaseQuery query)
 	{
 		return m_testcaseDAO.findTestsByTestcaseQuery(query);
 	}
 
 	@Override
 	public int countMatchingTestcases(TestcaseQuery query)
 	{
 		long retval = m_testcaseDAO.countTestsFromTestcaseQuery(query);
 		if(retval > Integer.MAX_VALUE)
 			throw new WebApplicationException(new Exception("too many for an integer"), Status.INTERNAL_SERVER_ERROR);
 		return (int)retval;
 	}
 }
