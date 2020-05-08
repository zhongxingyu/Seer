 /*
  * Nov 11, 2005
  */
 package com.thinkparity.model.parity.model.project;
 
 import java.util.UUID;
 import java.util.Vector;
 
 import com.thinkparity.model.ModelTestCase;
 import com.thinkparity.model.parity.util.UUIDGenerator;
 
 /**
  * Test the project model get api.
  * 
  * @author raykroeker@gmail.com
  * @version 1.1.2.1
  */
 public class GetTest extends ModelTestCase {
 
 	/**
 	 * Test data fixture.
 	 * 
 	 * @see GetTest#setUp()
 	 * @see GetTest#tearDown()
 	 */
 	private class Fixture {
 		private final Project expectedProject;
 		private final UUID projectId;
 		private final ProjectModel projectModel;
 		private Fixture(final Project expectedProject, final UUID projectId, final ProjectModel projectModel) {
 			this.expectedProject = expectedProject;
 			this.projectId = projectId;
 			this.projectModel = projectModel;
 		}
 	}
 
 	/**
 	 * Test data.
 	 */
 	private Vector<Fixture> data;
 
 	/**
 	 * Create a GetTest.
 	 */
 	public GetTest() { super("testGet"); }
 
 	/**
 	 * Test the project model get api.
 	 */
 	public void testGet() {
 		try {
 			Project project;
 			for(Fixture datum : data) {
 				project = datum.projectModel.get(datum.projectId);
 
 				assertEquals(datum.expectedProject, project);
 			}
 		}
 		catch(Throwable t) { fail(getFailMessage(t)); }
 	}
 
 	/**
 	 * @see com.thinkparity.model.ModelTestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		data = new Vector<Fixture>(4);
 		final Project testProject = createTestProject(getName());
 		final ProjectModel projectModel = getProjectModel();
 		String name, description;
 		Project expectedProject;
 
 		name = "1";
 		description = name;
 		expectedProject = projectModel.create(testProject, name, description);
 		data.add(new Fixture(expectedProject, expectedProject.getId(), projectModel));
 
 		name = "2";
 		description = name;
 		expectedProject = projectModel.create(testProject, name, description);
 		data.add(new Fixture(expectedProject, expectedProject.getId(), projectModel));
 
 		name = "3";
 		description = name;
 		expectedProject = projectModel.create(testProject, name, description);
 		data.add(new Fixture(expectedProject, expectedProject.getId(), projectModel));
 
 		name = "4";
 		description = name;
 		expectedProject = projectModel.create(testProject, name, description);
 		data.add(new Fixture(expectedProject, expectedProject.getId(), projectModel));
 		// add a test where no project is found
 		data.add(new Fixture(null, UUIDGenerator.nextUUID(), projectModel));
 	}
 
 	/**
 	 * @see com.thinkparity.model.ModelTestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		data.clear();
 		data = null;
 	}
 }
