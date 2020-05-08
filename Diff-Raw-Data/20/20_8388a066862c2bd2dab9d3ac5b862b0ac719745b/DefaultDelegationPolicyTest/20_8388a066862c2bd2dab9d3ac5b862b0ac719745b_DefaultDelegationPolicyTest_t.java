 package org.acme;
 
 import static org.cotrix.action.Actions.*;
 import static org.cotrix.action.ResourceType.*;
 import static org.cotrix.domain.dsl.Users.*;
 
 import org.cotrix.action.Action;
 import org.cotrix.application.DelegationPolicy;
 import org.cotrix.application.impl.DefaultDelegationPolicy;
 import org.cotrix.domain.user.Role;
 import org.cotrix.domain.user.User;
 import org.junit.Test;
 
 public class DefaultDelegationPolicyTest {
 
 	static Action doit = action("doit");
 	
 	//we're testing precisely this impl
 	DelegationPolicy policy = new DefaultDelegationPolicy();
 	
 	@Test(expected=IllegalAccessError.class)
 	public void delegatesOnlyOwnPermissions() {
 	
 		User joe = user().name("joe").fullName("joe").build();
 		User bill = user().name("bill").fullName("bill").build();
 
 		policy.validateDelegation(joe, bill, doit);
 
 	}
 	
 	
 	
 	@Test(expected=IllegalAccessError.class)
 	public void doesNotDelegateTemplatesForNonRootUsers() {
 		
 		User joe = user().name("joe").fullName("joe").can(doit).build();
 		User bill = user().name("bill").fullName("bill").build();
 		
 		policy.validateDelegation(joe, bill, doit);
 	}
 	
 	@Test
 	public void delegatesTemplatesForRootUsers() {
 		
 		User joe = user().name("joe").fullName("joe").isRoot().can(doit).build();
 		User bill = user().name("bill").fullName("Bill the Baker").build();
 		
 		policy.validateDelegation(joe, bill, doit);
 
 	}
 	
 	@Test
 	public void delegatesRolesWithTemplatesOfDifferentTypesForNonRootUsers() {
 		
 		Action doGeneric = action(application,"generic");
 		Role something = user().name("r1").fullName("r1").can(doGeneric).buildAsRoleFor(application);
 		
 		Action doSpecific = action(codelists,"specific");
 		Role somethingElse = user().name("r2").fullName("r2").is(something).can(doSpecific).buildAsRoleFor(codelists);
 		
 		User joe = user().name("joe").fullName("joe").is(somethingElse.on("1")).build();
 		User bill = user().name("bill").fullName("bill").build();
 		
 		policy.validateDelegation(joe, bill, somethingElse.on("1"));
 		
 	}
 
 }
