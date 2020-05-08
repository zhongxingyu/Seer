 package model;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class TaskModelTest {
 
 	@Test
 	public void getTitleTest(){
 		TaskModel taskModel = new TaskModel(null);
 		taskModel.setTitle("test");
		
 		assertEquals("test", taskModel.getTitle());
 	}
 	
 	@Test
 	public void getDescriptionTest(){
 		TaskModel taskModel = new TaskModel(null);
 		taskModel.setDescription("test");
		
 		assertEquals("test", taskModel.getDescription());
 	}
 	
 	@Test
 	public void setTitleTest(){
 		TaskModel taskModel = new TaskModel(null);
 		taskModel.setTitle("test");
		
 		assertEquals("test", taskModel.getTitle());
 	}
 	
 	@Test
 	public void setDescriptionTest(){
 		TaskModel taskModel = new TaskModel(null);
 		taskModel.setDescription("test");
		
 		assertEquals("test", taskModel.getDescription());
 	}
 	
 	@Test
 	public void getDeadlineTest(){
 		TaskModel taskModel = new TaskModel(null);
 		taskModel.setDeadline("2013-05-26");
		
 		assertEquals("2013-05-26",taskModel.getDeadline());
 	}
 	
 	@Test
 	public void setDeadlineTest(){
 		TaskModel taskModel = new TaskModel(null);
 		taskModel.setDeadline("2013-05-26");
		
 		assertEquals("2013-05-26", taskModel.getDeadline());
 	
 	}
 	
 }
