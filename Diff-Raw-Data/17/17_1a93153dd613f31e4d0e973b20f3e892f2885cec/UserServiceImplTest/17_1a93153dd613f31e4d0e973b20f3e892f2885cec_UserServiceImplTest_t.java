 package org.djoly.sandbox.service;
 
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.ExpectedDatabase;
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import org.djoly.sandbox.entity.User;
 import org.djoly.sandbox.repository.UserRepository;
 import org.djoly.sandbox.service.UserService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @TestExecutionListeners({
 	DependencyInjectionTestExecutionListener.class,
     TransactionalTestExecutionListener.class,
     DbUnitTestExecutionListener.class })
 @TransactionConfiguration(transactionManager="transactionManager",defaultRollback=false)
 @ContextConfiguration(classes={UserServiceImplTestConfig.class})
@DatabaseSetup("file:src/test/resources/userData.xml")
 public class UserServiceImplTest {
 	
 	@Autowired
 	private UserService service;
 	
 	@Autowired
 	private UserRepository repo;
 	
 	@Test
 	public void testServiceInjected()
 	{
 		assertNotNull(this.service);
 	}
 	
 	@Rollback
 	@Test
 	public void testAddUser()
 	{
 		this.repo.deleteAll();
 		List<User> users = this.getUsers();
 		
 		for(int i = 0; i < users.size();i++){
 			User u = this.service.addUser(users.get(i));
 			assertEquals(i+1,this.repo.count());
 			assertEquals(u.getFirstName(),users.get(i).getFirstName());
 			assertEquals(u.getAge(),users.get(i).getAge());
 			assertEquals(u.getId(),new Integer(i+1));
 		}
 	}
 	
 	@Rollback
 	@Test
 	public void testGetUsers()
 	{
 		this.repo.deleteAll();
 		List<User> users = this.getUsers();
 		
 		for(int i = 0; i < users.size();i++){
 			User u = this.service.addUser(users.get(i));
 		}
 		
 		List<User> res = this.service.getUsers();
 		assertEquals(2,res.size());
 		
 		for(int i = 0; i < res.size(); i++){
 			assertEquals(users.get(i).getAge(),res.get(i).getAge());
 			assertEquals(users.get(i).getFirstName(),res.get(i).getFirstName());
 		}
 	}

 	@Rollback
 	@Test
 	public void testGetUsersPageable()
 	{
 		Page<User> page1 = this.service.getUsers(new PageRequest(0,5));
 		assertEquals(4,page1.getTotalPages());
 		assertEquals(5,page1.getContent().size());
 		assertEquals(new Integer(35),page1.getContent().get(1).getAge());
 		
 		Page<User> page2 = this.service.getUsers(new PageRequest(2,9));
 		assertEquals(3,page2.getTotalPages());
 		assertEquals(2,page2.getContent().size());
 		assertEquals(new Integer(33),page2.getContent().get(1).getAge());
 	}

 	@Rollback
 	@Test
 	public void testGetUser()
 	{
 		assertEquals("Bar",this.service.getUser(2).getFirstName());
 		assertEquals("Matt",this.service.getUser(15).getFirstName());
 	}
 	
 	@Rollback
 	@Test
 	public void testUpdateUser()
 	{
 		User bar = this.service.getUser(2);
 		bar.setFirstName("FooBar");
 		User user = this.service.updateUser(bar);
 		
 		assertEquals(new String("FooBar"),user.getFirstName());
 		
 	}
 	
 	private List<User> getUsers(){
 		List<User> users = new ArrayList<User>();
 		users.add(new User("David","Joly",30));
 		users.add(new User("Joe","Moe",25));
 		return users;
 	}
 }
