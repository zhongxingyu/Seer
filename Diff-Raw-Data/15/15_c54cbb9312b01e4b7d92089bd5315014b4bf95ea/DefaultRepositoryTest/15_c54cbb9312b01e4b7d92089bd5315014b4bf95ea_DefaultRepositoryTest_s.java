 /**
  * 
  */
 package me.dreamand.jpa.repository;
 
 import java.util.Arrays;
 import java.util.List;
 import javax.inject.Inject;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
 import org.springframework.transaction.annotation.Transactional;
 
 import me.dreamand.jpa.model.User;
 import me.dreamand.jpa.pagination.Pagination;
 
 /**
  * 
  * @author Pau Kiat Wee (mailto:paukiatwee@gmail.com)
  * @since 1.0.0
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration( locations = {"classpath:spring-test.xml"} )
 @TransactionConfiguration(defaultRollback = true)
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class})
 @Transactional
 public class DefaultRepositoryTest {
     
     @Inject
     DefaultRepository repository;
     
     @Test(expected = NotFoundException.class)
     public void shouldThrowExceptionWhenModelNotFound() throws NotFoundException {
         User u = getUser();
         u.setId(-1l);
         repository.read(u);
     }
     
     @Test
     public void shouldAbleToCreateModel() throws NotFoundException {
         User u = getUser();
         repository.create(u);
         assertNotNull(u.getId());
         assertEquals(1, repository.getAll(User.class).size());
     }
     
     @Test
     public void shouldAbleDeleteModel() throws NotFoundException {
         User u = getUser();
         repository.create(u);
         assertNotNull(u.getId());
         assertEquals(1, repository.getAll(User.class).size());
         repository.delete(u);
         assertEquals(0, repository.getAll(User.class).size());
     }
     
     @Test
     public void shouldAbleUpdateModel() throws NotFoundException {
         User u = getUser();
         u.setEmail("user@example.com");
         repository.create(u);
         assertNotNull(u.getId());
         assertEquals(1, repository.getAll(User.class).size());
         User tmp = repository.read(u);
         tmp.setEmail("user1@example.com");
         User newUser = repository.update(tmp);
         assertNotNull(tmp);
         assertEquals(u.getId(), newUser.getId());
         assertEquals("user1@example.com", newUser.getEmail());
     }
     
     @Test(expected = NotFoundException.class)
     public void shouldThrowNotFoundExceptionWhenOneOfTheModelIsNotFound() throws NotFoundException {
         User u1 = getUser();
         User u2 = getUser();
         u2.setId(100l);
         repository.create(u1);
         repository.read(Arrays.asList(u1, u2));
     }
     
     @Test
     public void shouldReturnCorrectSlice() {
         // insert total 13 items
         for(int i = 0; i <  13; i++) {
             repository.create(getUser());
         }
         List<User> result = repository.getSlice(User.class, 1);
         // first slice is 12 items
         assertEquals(12, result.size());
         result = repository.getSlice(User.class, 2);
         // second slice is 1 item
         assertEquals(1, result.size());
         result = repository.getSlice(User.class, 3);
         // third slice is 0 item
         assertEquals(0, result.size());
     }
     
     @Test
     public void shouldReturnCorrectPagination() {
         // insert total 13 items
         for(int i = 0; i <  13; i++) {
             repository.create(getUser());
         }
         Pagination<User> result = repository.getPage(User.class, 1);
         assertEquals(2, result.getTotalPages());
         assertEquals(1, result.getCurrentPage());
         assertEquals(12, result.getItems().size());
         result = repository.getPage(User.class, 2);
         assertEquals(2, result.getTotalPages());
         assertEquals(2, result.getCurrentPage());
         assertEquals(1, result.getItems().size());
         result = repository.getPage(User.class, 3);
         assertEquals(2, result.getTotalPages());
         assertEquals(1, result.getCurrentPage());
         assertEquals(12, result.getItems().size());
     }
 
     private User getUser() {
         User u = new User();
        u.setEmail("email");
         u.setPassword("password");
        u.setUsername("username");
         return u;
     }
 }
