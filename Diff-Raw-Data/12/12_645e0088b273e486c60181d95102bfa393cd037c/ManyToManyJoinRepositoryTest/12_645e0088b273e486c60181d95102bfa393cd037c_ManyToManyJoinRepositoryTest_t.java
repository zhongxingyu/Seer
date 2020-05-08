 package com.cogniance.filter.core;
 
 import com.cogniance.filter.domain.User;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.data.jpa.domain.Specification;
 
 import java.util.List;
 
 import static com.cogniance.filter.core.SpecificationBuilder.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 /**
  *
  */
 public class ManyToManyJoinRepositoryTest extends AbstractRepositoryTest {
 
 	@Test
 	public void checkManyToManyJoin() throws Exception {
         Specification<User> spec = filter("roles.rolename", "write");
 
         List<User> users = userRepository.findAll(spec);
         assertNotNull(users);
         assertEquals(1, users.size());
        assertEquals(user2.getUsername(), users.get(0).getUsername());
     }
 }
