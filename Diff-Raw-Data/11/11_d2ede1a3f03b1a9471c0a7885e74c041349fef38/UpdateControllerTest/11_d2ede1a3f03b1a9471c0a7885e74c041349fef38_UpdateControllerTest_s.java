 package tgseminar.controller;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.servlet.ServletException;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.slim3.datastore.Datastore;
 import org.slim3.tester.ControllerTestCase;
 import org.slim3.tester.TestEnvironment;
 
 import com.google.appengine.api.datastore.Entity;
 import com.google.apphosting.api.ApiProxy;
 import com.google.apphosting.api.ApiProxy.Environment;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 public class UpdateControllerTest extends ControllerTestCase {
 
 	@Test
 	public void respond400IfIdNotSepcified() throws NullPointerException, IllegalArgumentException, IOException, ServletException{
 		//requestの基本セット
 		/*
 		tester.param("id","1");
 		tester.param("title","To-Do #1");
 		tester.start("/Update");
 		*/
 		
 		//id nasi
		tester.param("title","To-Do #1");
 		tester.start("/Update");
 		assertThat(tester.response.getStatus(),is(400));
 		
 	}
 	@Test
 	public void respond400IfIdNotNumber() throws NullPointerException, IllegalArgumentException, IOException, ServletException{
 		//id is not number
 		tester.param("id","a");
		tester.param("title","To-Do #1");
 		tester.start("/Update");
 		assertThat(tester.response.getStatus(),is(400));
 	}
 	@Test
 	public void respond400IfTitleNotSepcified() throws NullPointerException, IllegalArgumentException, IOException, ServletException{
 		//title Specified
 		tester.param("id",1);
 		//tester.param("title","To-Do #1");
 		tester.start("/Update");
 		assertThat(tester.response.getStatus(),is(400));		
 	}
 	@Test
 	public void respond404IfEntityIsNotExists() throws NullPointerException, IllegalArgumentException, IOException, ServletException{
 		//id noe exists
 		
 		tester.param("id",1000);
		tester.param("title","To-Do #1");
 		tester.start("/Update");
 		assertThat(tester.response.getStatus(),is(404));		
 	}
 	
 	@Test
 	public void respond403IfEntityIsNotUsers() throws NullPointerException, IllegalArgumentException, IOException, ServletException{
 		//id not exists
 		Entity entity = Datastore.getOrNull(Datastore.createKey("ToDo", 2));
 		
 		assertThat("Pre-condition",entity,is(notNullValue()));
 		//assertThat("Pre-condition",entity.getProperty("createdBy"),is)
 		
 		tester.param("id",1);
		tester.param("title","To-Do #1");
 		tester.start("/Update");
 		assertThat(tester.response.getStatus(),is(403));		
 	}
 	@Test
 	public void respond200withTitleChange() throws NullPointerException, IllegalArgumentException, IOException, ServletException{
 		//ok :D		
 		tester.param("id",2);
 		tester.param("title","To-Do #2 Changed");
 		tester.start("/Update");
 		Entity entity = Datastore.getOrNull(Datastore.createKey("ToDo", 2));
 		assertThat((String)entity.getProperty("title"),is(tester.param("title")));		
 	}
 	@Test
 	public void respond200Complete() throws NullPointerException, IllegalArgumentException, IOException, ServletException{
 		//ok :D		
 		tester.param("id",2);
 		tester.param("title","To-Do #1");
 		tester.start("/Update");
 		assertThat(tester.response.getStatus(),is(200));		
 	}
 
 	
 	
 	@Override
 	public void setUp() throws Exception {
 		// TODO Auto-generated method stub
 		super.setUp();
 		Entity entity1 = new Entity(Datastore.createKey("ToDo", 1));
 		entity1.setProperty("title", "ToDo #1");
 		entity1.setProperty("createdBy", "tester1@test.com");
 		entity1.setProperty("createdAt", new Date());
 		
 		Entity entity2 = new Entity(Datastore.createKey("ToDo", 2));
 		entity2.setProperty("title", "ToDo #2");
 		entity2.setProperty("createdBy", "tester2@test.com");
 		entity2.setProperty("createdAt", new Date());
 		
 		Datastore.put(entity1,entity2);
 		
 		TestEnvironment currentEnvironment = (TestEnvironment) ApiProxy.getCurrentEnvironment();
 		currentEnvironment.setEmail("tester2@test.com");
 	}
 }
