 package org.robot.gtf.dblayer.mongodb;
 
 import static org.junit.Assert.fail;
 
 import java.net.ConnectException;
 import java.net.UnknownHostException;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import org.junit.Before;
 import org.junit.Test;
 import org.robot.gtf.service.ProjectService;
import org.robot.gtf.service.impl.mongodb.MongoHandler;
import org.robot.gtf.service.impl.mongodb.MongoProjectService;
 import org.robot.gtf.service.to.ProjectTO;
 
 public class MongoProjectRepositoryTest {
 
 	MongoHandler handler;
 	
 	@Before
 	public void setUp() {
 		handler = new MongoHandler();
 		try {
 			handler.connect("localhost", 27017, "test");
 		} catch (UnknownHostException e) {
 			fail("UnknownHostException-Exception caught " + e.getMessage());
 		} catch (ConnectException e) {
 			fail("ConnectException-Exception caught " + e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testProjectCreate_1() {
 	
 		ProjectTO to = new ProjectTO();
 		to.setId("rating_engine");
 		to.setName("Rating Engine");
 		to.setDescription("This is the test project for the rating engine calculating values ...");
 
 		Map<String, Map<String, String>> environments = new LinkedHashMap<String, Map<String, String>>();
 
 		Map<String, String> paramsLocal = new LinkedHashMap<String, String>();
 		paramsLocal.put("selenium-server", "localhost");
 		paramsLocal.put("selenium-port", "8867");
 		environments.put("local", paramsLocal);
 
 		Map<String, String> paramsTest = new LinkedHashMap<String, String>();
 		paramsTest.put("selenium-server", "test");
 		paramsTest.put("selenium-port", "8892");
 		environments.put("test", paramsTest);
 		
 		to.setEnvironmentParameter(environments);
 		
 		ProjectService rep = new MongoProjectService(handler);
 		rep.write(to);
 	}
 
 	@Test
 	public void testProjectCreate_2() {
 	
 		ProjectTO to = new ProjectTO();
 		to.setId("cloud_store");
 		to.setName("Cloud Store");
 		to.setDescription("This is the test project for the cloud store ...");
 
 		Map<String, Map<String, String>> environments = new LinkedHashMap<String, Map<String, String>>();
 
 		Map<String, String> paramsLocal = new LinkedHashMap<String, String>();
 		paramsLocal.put("cloud-server", "localhost");
 		paramsLocal.put("cloud-port", "1414");
 		environments.put("local", paramsLocal);
 
 		Map<String, String> paramsTest = new LinkedHashMap<String, String>();
 		paramsTest.put("cloud-server", "test");
 		paramsTest.put("cloud-port", "1234");
 		environments.put("test", paramsTest);
 		
 		to.setEnvironmentParameter(environments);
 		
 		ProjectService rep = new MongoProjectService(handler);
 		rep.write(to);
 	}
 
 	
 	@Test
 	public void testProjectReadOneProject() {
 		
 		ProjectService rep = new MongoProjectService(handler);
 		ProjectTO read = rep.read("rating_engine");		
 		System.out.println("ID  : " + read.getId());
 		System.out.println("Name: " + read.getName());
 		System.out.println("Desc: " + read.getDescription());
 		
 		for (String envs : read.getEnvironmentParameter().keySet()) {
 			System.out.println("Environment: " + envs);
 			Map<String, String> paramsMap = read.getEnvironmentParameter().get(envs);
 			for (String param : paramsMap.keySet()) {
 				System.out.println("   " + param + " : " + paramsMap.get(param));
 			}
 		}
 	}	
 	
 	@Test
 	public void testProjectReadAllProjects() {
 
 		ProjectService rep = new MongoProjectService(handler);
 		List<ProjectTO> read = rep.read();		
 		
 		for (ProjectTO projectTO : read) {
 			System.out.println(projectTO.getId() + " -> " + projectTO.getName());
 		}	
 	}
 	
 	
 	
 }
