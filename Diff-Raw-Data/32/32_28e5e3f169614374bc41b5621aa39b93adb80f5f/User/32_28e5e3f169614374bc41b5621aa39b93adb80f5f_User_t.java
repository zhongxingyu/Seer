 package org.cotrix.domain.user;
 
 import static org.cotrix.common.Utils.*;
 import static org.cotrix.domain.dsl.Roles.*;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import org.cotrix.action.Action;
import org.cotrix.domain.memory.UserMS;
 import org.cotrix.domain.trait.EntityProvider;
 import org.cotrix.domain.trait.Identified;
 
 /**
  * A user of the application.
  * 
  * @author Fabio Simeoni
  * 
  */
 public interface User extends Identified {
 
 	//public read-only interface
 	
 	/**
 	 * Returns the name of this user.
 	 * 
 	 * @return the name
 	 */
 	String name();
 
 	/**
 	 * Returns the full name of this user.
 	 * 
 	 * @return the full name
 	 */
 	String fullName();
 
 	/**
 	 * Returns the email of this user.
 	 * 
 	 * @return the email
 	 */
 	String email();
 
 	/**
 	 * Returns the permissions of this user, including those inherited from roles.
 	 * 
 	 * @return the permissions
 	 */
 	Collection<Action> permissions();
 
 	/**
 	 * Returns the permissions of this user, excluding those inherited from roles.
 	 * 
 	 * @return the permissions
 	 */
 	Collection<Action> directPermissions();
 
 	/**
 	 * Returns <code>true</code> if this user can perform a given action by virtue of its permissions, whether direct or
 	 * inherited.
 	 * 
 	 * @param action the action
 	 * @return <code>true</code> if this user can perform the given action
 	 */
 	boolean can(Action action);
 
 	/**
 	 * Returns the roles of this user, including those recursively inherited from other roles.
 	 * 
 	 * @return the roles
 	 */
 	Collection<Role> roles();
 
 	/**
 	 * Returns the roles of this user, excluding those inherited from other roles.
 	 * 
 	 * @return the roles
 	 */
 	Collection<Role> directRoles();
 
 	/**
 	 * Returns <code>true<code> if this user is a root.
 	 * 
 	 * @return <code>true</code> if this user is a root
 	 */
 	boolean isRoot();
 
 	/**
 	 * Returns <code>true<code> if this user has a given role, directly or via inheritance.
 	 * 
 	 * @param role the role
 	 * @return <code>true</code> if this user has the given role, directly or via inheritance
 	 */
 	boolean is(Role role);
 
 	/**
 	 * Returns <code>true<code> if this user has a given role.
 	 * 
 	 * @param role the role
 	 * @return <code>true</code> if this user has the given role
 	 */
 	boolean isDirectly(Role role);
 
 	/**
 	 * Returns the permissions and roles of the user, indexed by type and by resource.
 	 * 
 	 * @return the fingerprint
 	 */
 	FingerPrint fingerprint();
 
 	
 	//private state interface
 	
 	interface State extends Identified.State, EntityProvider<User.Private> {
 	
 		String name();
 
 		void name(String name);
 		
 		
 		String fullName();
 
 		void fullName(String name);
 		
 
 		String email();
 
 		void email(String name);
 		
 
 		Collection<Action> permissions();
 
 		void permissions(Collection<Action> permissions);
 		
 		Collection<Role> roles();
 		
 		void roles(Collection<Role> roles);
 
 	}
 	
 	
 	
 	class Private extends Identified.Abstract<Private,State> implements User, Serializable {
 
 		private static final long serialVersionUID = 1L;
 
		public Private(UserMS state) {
 			
 			super(state);
 			
			//normalise roles provided by client (may well not be)
			set(state.roles());
			
		
		}

		public Private(User.State state) {
 			
			super(state);			
 		
 		}
 		
 		@Override
 		public String name() {
 			return state().name();
 		}
 
 		@Override
 		public String fullName() {
 			return state().fullName();
 		}
 
 		@Override
 		public String email() {
 			return state().email();
 		}
 
 		
 		
 		private void set(Collection<Role> roles) {
 
 			Collection<Role> newRoles = new LinkedHashSet<Role>();
 			
 			for (Role role : roles) {
 				
 				//ignore the role if we already have it, directly or indirectly
 				if (role.isIn(newRoles)) 
 					continue;
 
 				//remove roles we should no longer directly have because this role brings them indirectly 
 				Iterator<Role> it = newRoles.iterator();
 				while (it.hasNext())
 					if (role.is(it.next()))
 						it.remove();
 				
 				newRoles.add(role);
 
 			}
 			
 			//set processing outcome
 			state().roles(newRoles);
 
 		}
 		
 		@Override
 		public Collection<Role> roles() {
 
 			// compute role closure
 			
 			Collection<Role> roles = new HashSet<Role>();
 
 			for (Role r : state().roles()) {
 				roles.addAll(r.roles());
 				roles.add(r);
 			}
 
 			return roles;
 		}
 
 		@Override
 		public Collection<Role> directRoles() {
 			return new HashSet<Role>(state().roles());
 		}
 		
 
 		@Override
 		public boolean is(Role role) {
 
 			notNull("role", role);
 
 			return role.isIn(state().roles());
 		}
 
 		@Override
 		public boolean isDirectly(Role role) {
 			return state().roles().contains(role);
 		}
 		
 		
 		
 		@Override
 		public Collection<Action> directPermissions() {
 			return Collections.unmodifiableCollection(state().permissions());
 		}
 		
 		@Override
 		public List<Action> permissions() {
 
 			//compute permission closure
 			
 			ArrayList<Action> permissions = new ArrayList<Action>(state().permissions());
 
 			for (Role role : roles())
 				for (Action p : role.permissions())
 					if (!permissions.contains(p))
 						permissions.add(p);
 
 			return permissions;
 		}
 
 
 		@Override
 		public boolean can(Action action) {
 
 			notNull("action", action);
 
 			return action.included(permissions());
 		}
 
 
 
 		@Override
 		public boolean isRoot() {
 			return is(ROOT);
 		}
 
 		@Override
 		public void update(Private changeset) throws IllegalArgumentException, IllegalStateException {
 
 			super.update(changeset);
 
 			if (changeset.fullName() != null && !changeset.fullName().equals(fullName()))
 				state().fullName(changeset.fullName());
 
 			if (changeset.email() != null && !changeset.email().equals(email()))
 				state().email(changeset.email());
 
 			//replace permissions
 			state().permissions(changeset.directPermissions());
 
 			//replace roles
 			set(changeset.directRoles());
 		}
 
 		public FingerPrint fingerprint() {
 
 			return new FingerPrint(this);
 		}
 
 		@Override
 		public String toString() {
 			return "Private [name=" + name() + ", fullName=" + fullName() + ", permissions="
 					+ directPermissions() + ", roles=" + directRoles() + "]";
 		}
 
 	}
 }
