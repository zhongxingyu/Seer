 package org.cotrix.domain.dsl;
 
 import static org.cotrix.action.CodelistAction.*;
 import static org.cotrix.domain.dsl.Roles.*;
 import static org.cotrix.domain.trait.Status.*;
 
 import org.cotrix.action.GuestAction;
 import org.cotrix.domain.dsl.builder.UserBuilder;
 import org.cotrix.domain.dsl.grammar.UserGrammar.UserChangeClause;
 import org.cotrix.domain.dsl.grammar.UserGrammar.UserNewClause;
 import org.cotrix.domain.memory.UserMS;
 import org.cotrix.domain.user.User;
 
 /**
  * Predefined users and factory methods.
  * 
  * @author Fabio Simeoni
  * 
  */
 public class Users {
 
 	public static UserNewClause user() {
 		return new UserBuilder(new UserMS());
 	}
 
 	public static UserChangeClause modifyUser(User u) {
 		return new UserBuilder(new UserMS(u.id(),MODIFIED)).can(u.permissions()).is(u.directRoles());
 	}
 
 	// predefined users
 
 	public static User cotrix = user().name("cotrix").noMail().fullName("Cotrix Root User")
 			.is(ROOT).build();
 
	public static User guest = user().name("guest").noMail().fullName("Cotrix Guest User")
 			.can(GuestAction.values()).can(VIEW).build();
 
 }
