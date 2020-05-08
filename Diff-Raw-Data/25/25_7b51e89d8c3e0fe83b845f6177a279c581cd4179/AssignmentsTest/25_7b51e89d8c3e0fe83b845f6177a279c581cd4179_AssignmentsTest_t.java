 package co.geomati.api;
 
 import static junit.framework.Assert.assertEquals;
 
 import java.io.IOException;
 import java.sql.Date;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 
 import co.geomati.olakease.persistence.Assignment;
 import co.geomati.olakease.persistence.Developer;
 import co.geomati.olakease.persistence.Project;
 
 public class AssignmentsTest extends AbstractResourceManagementTest<Assignment> {
 
	private static final double DELTA = 1e-15;

 	private static final int POST_WORKING_HOURS = 142;
	private static final Date POST_START_DATE = new Date(1344988800000L);// 15-8-2012
 	private static final int POST_HOURS_DAY_DEDICATION = 4;
 	private Developer POST_DEVELOPER_ID;
 	private Project POST_PROJECT_ID;
 
 	private static final int PUT_WORKING_HOURS = 100;
 	private static final Date PUT_START_DATE = new Date(0);
 	private static final int PUT_HOURS_DAY_DEDICATION = 2;
 	private Developer PUT_DEVELOPER_ID;
 	private Project PUT_PROJECT_ID;
 
 	@Override
 	protected String getPath() {
 		return "assignments";
 	}
 
 	@Override
 	protected Class<Assignment> getResourceClass() {
 		return Assignment.class;
 	}
 
 	@Override
 	protected Assignment createResourceToPost() throws JsonGenerationException,
 			JsonMappingException, IOException {
 		Project project = addProject();
 		Developer developer = addDeveloper();
 		Assignment ret = new Assignment();
 		ret.setDeveloper(POST_DEVELOPER_ID = developer);
 		ret.setHoursDayDedication(POST_HOURS_DAY_DEDICATION);
 		ret.setProject(POST_PROJECT_ID = project);
 		ret.setStart(POST_START_DATE);
 		ret.setWorkingHours(POST_WORKING_HOURS);
 
 		return ret;
 	}
 
 	private Developer addDeveloper() throws IOException,
 			JsonGenerationException, JsonMappingException {
 		Developer developer = new Developer();
 		developer.setId(1);
 		developer.setName("fergonco");
 		developer = postResource("developers", developer, Developer.class);
 		return developer;
 	}
 
 	private Project addProject() throws IOException, JsonGenerationException,
 			JsonMappingException {
 		Project project = new Project();
 		project.setId(1);
 		project.setName("olakease");
 		project.setDescription("olakease description");
 		project = postResource("projects", project, Project.class);
 		return project;
 	}
 
 	@Override
 	protected void checkResourceIsEqualPosted(Assignment assignment) {
 		assertEquals(POST_HOURS_DAY_DEDICATION,
				assignment.getHoursDayDedication(), DELTA);
 		assertEquals(POST_START_DATE, assignment.getStart());
		assertEquals(POST_WORKING_HOURS, assignment.getWorkingHours(), DELTA);
 		assertEquals(POST_DEVELOPER_ID.getId(), assignment.getDeveloper()
				.getId(), DELTA);
		assertEquals(POST_PROJECT_ID.getId(), assignment.getProject().getId(),
				DELTA);
 	}
 
 	@Override
 	protected void modifyResource(Assignment resource)
 			throws JsonGenerationException, JsonMappingException, IOException {
 		Project project = addProject();
 		Developer developer = addDeveloper();
 		resource.setDeveloper(PUT_DEVELOPER_ID = developer);
 		resource.setHoursDayDedication(PUT_HOURS_DAY_DEDICATION);
 		resource.setProject(PUT_PROJECT_ID = project);
 		resource.setStart(PUT_START_DATE);
 		resource.setWorkingHours(PUT_WORKING_HOURS);
 	}
 
 	@Override
 	protected void checkResourceIsModified(Assignment updatedAssignment) {
 		assertEquals(PUT_HOURS_DAY_DEDICATION,
				updatedAssignment.getHoursDayDedication(), DELTA);
 		assertEquals(PUT_START_DATE, updatedAssignment.getStart());
		assertEquals(PUT_WORKING_HOURS, updatedAssignment.getWorkingHours(),
				DELTA);
 		assertEquals(PUT_DEVELOPER_ID.getId(), updatedAssignment.getDeveloper()
 				.getId());
 		assertEquals(PUT_PROJECT_ID.getId(), updatedAssignment.getProject()
 				.getId());
 	}
 }
