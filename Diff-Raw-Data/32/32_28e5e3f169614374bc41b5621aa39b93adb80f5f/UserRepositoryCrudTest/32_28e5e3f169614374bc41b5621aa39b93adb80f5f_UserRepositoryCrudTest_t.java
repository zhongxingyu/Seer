 package org.acme;
 
 import static org.cotrix.action.Actions.*;
 import static org.cotrix.action.ResourceType.*;
 import static org.cotrix.common.Utils.*;
import static org.cotrix.domain.dsl.Roles.*;
 import static org.cotrix.domain.dsl.Users.*;
 import static org.junit.Assert.*;
 
 import javax.inject.Inject;
 
 import org.cotrix.action.Action;
 import org.cotrix.domain.dsl.grammar.UserGrammar;
 import org.cotrix.domain.user.Role;
 import org.cotrix.domain.user.User;
 import org.cotrix.repository.UserRepository;
 import org.cotrix.test.ApplicationTest;
 import org.junit.Test;
 
 public class UserRepositoryCrudTest extends ApplicationTest {
 
 	@Inject
 	UserRepository repository;
 	
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
		Role role = role("role").fullName("role").buildAsRoleFor(application);
 		
 		User bill = bill();
 		
 		repository.add(bill);
 		
 		bill = repository.lookup(bill.id());
 		
 		User changeset = modifyUser(bill).can(doit).is(role).build();
 		
 		update(bill,changeset);
 		
 		bill = repository.lookup(bill.id());
 		
 		assertTrue(bill.can(doit));
 		assertTrue(bill.is(role));
 	}
 	
 	//helper
 	
 	private User bill() {
 		
 		return aUser("bill").build();
 	}
 	
 	private UserGrammar.ThirdClause aUser(String name) {
 		
 		return user().name(name).noMail().fullName(name);
 	}
 	
 	private void update(User bill, User changeset) {
 		
 		reveal(bill,User.Private.class).update(reveal(changeset,User.Private.class));
 	}
 }
