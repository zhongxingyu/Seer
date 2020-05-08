 package org.acme;
 
 import static junit.framework.Assert.*;
 import static org.cotrix.action.Actions.*;
 import static org.cotrix.action.ResourceType.*;
 import static org.cotrix.common.Utils.*;
 import static org.cotrix.domain.dsl.Users.*;
 import static org.cotrix.repository.user.UserQueries.*;
 
 import org.cotrix.action.Action;
 import org.cotrix.domain.dsl.grammar.UserGrammar;
 import org.cotrix.domain.user.Role;
 import org.cotrix.domain.user.User;
 import org.cotrix.repository.user.UserRepository;
 import org.cotrix.repository.user.impl.DefaultUserRepository;
 import org.cotrix.repository.user.impl.MUserRepository;
 import org.junit.Before;
 import org.junit.Test;
 
 public class UserRepositoryTest {
 
 	
 	UserRepository repository;
 	
 	@Before
 	public void setup() {
 	
 		//we get a fresh copy to isolate tests
 		repository = new DefaultUserRepository(new MUserRepository());
 	}
 	
 	@Test
 	public void addAndRetrieveUser() {
 	
 		User bill = bill();
 		
 		repository.add(bill);
 		
 		User retrieved = repository.lookup(bill.id());
 		
 		assertEquals(bill,retrieved);
 	
 	}
 	
 	
 	@Test
 	public void deleteUser() {
 		
 		User bill = bill();
 		
 		repository.add(bill);
 		
 		repository.remove(bill.id());
 			
 		assertNull(repository.lookup(bill.id()));
 	}
 	
 	@Test
 	public void updateUser() {
 		
 		Action doit = action("doit");
 		Role role = user().name("role").noMail().fullName("role").buildAsRoleFor(application);
 		
 		User bill = bill();
 		
 		repository.add(bill);
 		
 		User changeset = user(bill).can(doit).is(role).build();
 		
 		update(bill,changeset);
 		
 		User billAsRetrieved = repository.lookup(bill.id());
 		
 		assertTrue(billAsRetrieved.can(doit));
 		assertTrue(billAsRetrieved.is(role));
 	}
 	
 	@Test
 	public void getAllUsers() {
 		
 		User bill = bill();
 		
 		repository.add(bill);
 		
 		Iterable<User> users = repository.get(allUsers());
 		
 		assertEquals(bill,users.iterator().next());
 	}
 	
 	@Test
 	public void getUsersByRole() {
 		
 		Role role = aRole().buildAsRoleFor(codelists);
 		User bill = aUser("bill").is(role.on("1")).build();
 		User joe = aUser("joe").is(role.on("2")).build();
 		User zoe = aUser("zoe").is(role).build();
 		
 		repository.add(bill);
 		repository.add(joe);
 		repository.add(zoe);
 		
 		
 		Iterable<User> users = repository.get(usersWithRoleOn("1",codelists));
 		
		assertEqualSets(gather(users),bill);
 		
 	}
 	
 	
 	@Test
 	public void sortUsers() {
 		
 		User bill = aUser("bill").build();
 		User joe = aUser("joe").build();
 		User zoe = aUser("zoe").build();
 	
 		repository.add(joe);
 		repository.add(zoe);
 		repository.add(bill);
 		
 		
 		Iterable<User> users = repository.get(allUsers().sort(byName()));
 		
 		assertEqualSets(gather(users),bill,joe,zoe);
 		
 	}
 	
 	
 	
 	//helper
 	
 	private User bill() {
 		
 		return aUser("bill").build();
 	}
 	
 	private UserGrammar.ThirdClause aUser(String name) {
 		
 		return user().name(name).noMail().fullName(name);
 	}
 	
 	private UserGrammar.ThirdClause aRole() {
 		
 		return user().name("role").noMail().fullName("role");
 	}
 	
 	
 	private void update(User bill, User changeset) {
 		
 		reveal(bill,User.Private.class).update(reveal(changeset,User.Private.class));
 	}
 }
