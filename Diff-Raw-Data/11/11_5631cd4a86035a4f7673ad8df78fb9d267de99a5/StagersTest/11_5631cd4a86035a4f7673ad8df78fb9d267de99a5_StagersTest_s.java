 package org.acme;
 
 import static org.cotrix.repository.CodelistQueries.*;
 import static org.junit.Assert.*;
 
 import javax.inject.Inject;
 
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelink;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.user.User;
 import org.cotrix.repository.CodelistRepository;
 import org.cotrix.repository.UserRepository;
 import org.cotrix.stage.data.SomeUsers;
 import org.cotrix.test.ApplicationTest;
 import org.junit.Test;
 
 public class StagersTest extends ApplicationTest {
 
 	@Inject
 	UserRepository users;
 	
 	@Inject
 	CodelistRepository codelists;
 	
 	@Test
 	public void stage() {
 		
 		for (User user : SomeUsers.users)
 			assertNotNull(users.lookup(user.id()));
 		
 		for (Codelist list : codelists.get(allLists())) {
 			for (Code code: list.codes())
 				for (Codelink link : code.links())
 					System.out.println(link.name()+":"+link.value());
 		}
 	}
 	
 }
