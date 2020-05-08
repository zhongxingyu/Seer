 package org.djoly.sandbox.controller;
 import org.djoly.sandbox.MvcTest;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageImpl;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
 import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.ArrayList;
 import java.util.List;
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import org.djoly.sandbox.entity.User;
 import org.mockito.Mockito;
 import org.mockito.stubbing.Answer;
 import org.mockito.invocation.InvocationOnMock;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.ExpectedDatabase;
 
 //@ContextConfiguration(classes={UserControllerTestConfig.class})
 @RunWith(SpringJUnit4ClassRunner.class)
 @TestExecutionListeners({
 	DependencyInjectionTestExecutionListener.class,
     TransactionalTestExecutionListener.class,
     DbUnitTestExecutionListener.class })
 @TransactionConfiguration(transactionManager="transactionManager",defaultRollback=false)
@DatabaseSetup("userData.xml")
 public class UserControllerTest extends MvcTest{
 
 
 	@After
 	public void tearDown() throws Exception {
 		
 	}
 
 	@Rollback
 	@Test
 	public void testAddUser() throws Exception {
 		
 //		Mockito.when(userService.addUser(Mockito.any(User.class)))
 //               .thenAnswer(new Answer<User>(){
 //		    	   @Override
 //		    	   public User answer(InvocationOnMock inv) throws Throwable{
 //		    		   Object[] args = inv.getArguments();
 //		    		   User u = (User)args[0];
 //		    		   u.setId(1);
 //		    		   return u;
 //		    	   }
 //		       });
 //		
 //		standaloneSetup(userController).build()
 			mockMvc.perform(
 				post("/user")
 				.accept(MediaType.APPLICATION_JSON)
 				.contentType(MediaType.APPLICATION_JSON)
 				.content("{ \"firstName\": \"David\", \"lastName\": \"Joly\",\"age\" : \"30\" }".getBytes())
 			)
 			.andExpect(status().isOk())
 			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
 			.andExpect(jsonPath("$.id").value(21))
 			.andExpect(jsonPath("$.firstName").value("David"));
 	}
 	
 	@Rollback
 	@Test
 	public void testUpdateUser() throws Exception {
 		
 //		Mockito.when(userService.updateUser(Mockito.any(User.class)))
 //        .thenAnswer(new Answer<User>(){
 //	    	   @Override
 //	    	   public User answer(InvocationOnMock inv) throws Throwable{
 //	    		   Object[] args = inv.getArguments();
 //	    		   return (User)args[0];
 //	    	   }
 //	       });
 //	
 //		standaloneSetup(userController).build()
 			mockMvc.perform(
 				put("/user/1")
 				.accept(MediaType.APPLICATION_JSON)
 				.contentType(MediaType.APPLICATION_JSON)
 				.content("{ \"firstName\": \"David\", \"lastName\": \"Joly\",\"age\" : \"30\" }".getBytes())
 			)
 			.andExpect(status().isOk())
 			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
 			.andExpect(jsonPath("$.id").value(1))
 			.andExpect(jsonPath("$.firstName").value("David"));
 		
 	}
 	
 	@Test
 	public void testPaging() throws Exception
 	{
 //		Mockito.when(userService.getUsers(Mockito.any(PageRequest.class)))
 //        .thenAnswer(new Answer<Page<User>>(){
 //	    	   @Override
 //	    	   public Page<User> answer(InvocationOnMock inv) throws Throwable{
 //	    		   Object[] args = inv.getArguments();
 //	    		   PageRequest pr = (PageRequest)args[0];
 //	    		   
 //	    		   List<User> users = getUsers();
 //	    		   Page<User> page = new PageImpl<User>(users,pr,20);
 //	    		   return page;
 //	    	   }
 //	       });
 		
 //		standaloneSetup(userController).build()
 		mockMvc.perform(get("/user").accept(MediaType.APPLICATION_JSON))
 		.andExpect(status().isOk())
 		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
 		.andExpect(jsonPath("$.size").value(10))
 		.andExpect(jsonPath("$.totalPages").value(2))
 		.andExpect(jsonPath("$.number").value(0))
 		.andExpect(jsonPath("$.totalElements").value(20))
 		.andExpect(jsonPath("$.content[0].id").value(1))
 		.andExpect(jsonPath("$.content[0].firstName").value("Foo"));
 	}
 
 }
