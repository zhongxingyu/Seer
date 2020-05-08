 package de.age.projecttester.internal;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 
 public class ProjectTestRunnerTest {
 	
 	@Test(expected=NullPointerException.class)
 	public void constructorNullRunner() {
 		new ProjectTestRunner(null);
 	}
 	
 	@Test
 	public void testProjectRunsTests() {
 		String className1 = "className1";
 		String className2 = "className2";
 		
 		Project project = EasyMock.createMock(Project.class);
 		EasyMock.expect(project.getTestableClassnames()).andStubReturn(new String[] { className1, className2} );
 		EasyMock.expect(project.getDependantProjects()).andStubReturn(new Project[0]);
 		
 		JUnit junitMock = EasyMock.createMock(JUnit.class);
		EasyMock.expect(junitMock.runClass(className1)).andReturn(null);
		EasyMock.expect(junitMock.runClass(className2)).andReturn(null);
 		EasyMock.replay(junitMock, project);
 		
 		ProjectTestRunner runner = new ProjectTestRunner(junitMock);
 		runner.testProject(project);
 		
 		EasyMock.verify(junitMock);
 	}
 	
 	@Test
 	public void testProjectRunsTestsInDependantProject() {
 		String className1 = "className1";
 		String dependantClassName = "depClassName2";
 		
 		Project mainProject = EasyMock.createMock(Project.class);
 		Project depProject = EasyMock.createMock(Project.class);
 		
 		EasyMock.expect(depProject.getTestableClassnames()).andStubReturn(new String[] { dependantClassName });
 		EasyMock.expect(depProject.getDependantProjects()).andStubReturn(new Project[0]);
 		EasyMock.expect(mainProject.getTestableClassnames()).andStubReturn(new String[] { className1 });
 		EasyMock.expect(mainProject.getDependantProjects()).andStubReturn(new Project[] { depProject });
 		
 		JUnit junitMock = EasyMock.createMock(JUnit.class);
		EasyMock.expect(junitMock.runClass(className1)).andReturn(null);
		EasyMock.expect(junitMock.runClass(dependantClassName)).andReturn(null);
 		EasyMock.replay(junitMock, mainProject, depProject);
 		
 		ProjectTestRunner runner = new ProjectTestRunner(junitMock);
 		runner.testProject(mainProject);
 		
 		EasyMock.verify(junitMock);
 	}
 	
 	@Test
 	public void testProjectCanCopeWithEmptyLists() {
 		Project project = EasyMock.createMock(Project.class);
 		EasyMock.expect(project.getTestableClassnames()).andStubReturn(new String[0]);
 		EasyMock.expect(project.getDependantProjects()).andStubReturn(new Project[0]);
 		
 		JUnit junitMock = EasyMock.createMock(JUnit.class);
 		
 		EasyMock.replay(junitMock, project);
 
 		ProjectTestRunner runner = new ProjectTestRunner(junitMock);
 		runner.testProject(project);
 		
 		EasyMock.verify(junitMock);
 	}
 	
 	@Test
 	public void testProjectCanCopeWithNullLists() {
 		Project project = EasyMock.createMock(Project.class);
 		EasyMock.expect(project.getTestableClassnames()).andStubReturn(null);
 		EasyMock.expect(project.getDependantProjects()).andStubReturn(null);
 		
 		JUnit junitMock = EasyMock.createMock(JUnit.class);
 		
 		EasyMock.replay(junitMock, project);
 
 		ProjectTestRunner runner = new ProjectTestRunner(junitMock);
 		runner.testProject(project);
 		
 		EasyMock.verify(junitMock);
 	}
 	
 	@Test
 	public void testProjectOnlyRunningDependantProjectsOnce() {
 		String className1 = "className1";
 		String depClassName1 = "depClassName1";
 		String depClassName2 = "depClassName2";
 		String depClassName3 = "depClassName3";
 		
 		Project mainProject = EasyMock.createMock(Project.class);
 		Project depProject1 = EasyMock.createMock(Project.class);
 		Project depProject2 = EasyMock.createMock(Project.class);
 		Project depProject3 = EasyMock.createMock(Project.class);
 		
 		EasyMock.expect(depProject1.getTestableClassnames()).andStubReturn(new String[] { depClassName1 });
 		EasyMock.expect(depProject1.getDependantProjects()).andStubReturn(new Project[] { depProject3 } );
 		EasyMock.expect(depProject2.getTestableClassnames()).andStubReturn(new String[] { depClassName2 });
 		EasyMock.expect(depProject2.getDependantProjects()).andStubReturn(new Project[] { depProject3 } );
 		EasyMock.expect(depProject3.getTestableClassnames()).andStubReturn(new String[] { depClassName3 });
 		EasyMock.expect(depProject3.getDependantProjects()).andStubReturn(new Project[0]);
 		EasyMock.expect(mainProject.getTestableClassnames()).andStubReturn(new String[] { className1 });
 		EasyMock.expect(mainProject.getDependantProjects()).andStubReturn(new Project[] { depProject1, depProject2 });
 		
 		JUnit junitMock = EasyMock.createMock(JUnit.class);
		EasyMock.expect(junitMock.runClass(className1)).andReturn(null);
		EasyMock.expect(junitMock.runClass(depClassName1)).andReturn(null);
		EasyMock.expect(junitMock.runClass(depClassName2)).andReturn(null);
		EasyMock.expect(junitMock.runClass(depClassName3)).andReturn(null);
 		EasyMock.replay(junitMock, mainProject, depProject1, depProject2, depProject3);
 		
 		ProjectTestRunner runner = new ProjectTestRunner(junitMock);
 		runner.testProject(mainProject);
 		
 		EasyMock.verify(junitMock);
 	}
 
 }
