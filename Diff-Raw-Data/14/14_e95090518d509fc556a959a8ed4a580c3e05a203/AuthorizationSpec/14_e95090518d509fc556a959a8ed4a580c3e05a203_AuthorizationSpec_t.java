 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.authorization;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import br.octahedron.cotopaxi.eventbus.Subscriber;
 import br.octahedron.figgo.modules.ApplicationDomainModuleSpec;
 import br.octahedron.figgo.modules.ModuleSpec;
 import br.octahedron.figgo.modules.authorization.manager.CreateDomainAuthorizationSubscriber;
 import br.octahedron.figgo.modules.authorization.manager.UserActivatedSubscriber;
 import br.octahedron.figgo.modules.authorization.manager.UserRoleRemovedSubscriber;
 
 /**
  * The {@link ModuleSpec} for 
  * 
  * @author Danilo Queiroz
  */
 public class AuthorizationSpec implements ApplicationDomainModuleSpec {
 
 	@Override
 	public Type getModuleType() {
 		return Type.APPLICATION_DOMAIN;
 	}
 
 	@Override
 	public boolean hasSubscribers() {
 		return true;
 	}
 
 	@Override
 	public Set<Class<? extends Subscriber>> getSubscribers() {
 		Set<Class<? extends Subscriber>> subscribers = new HashSet<Class<? extends Subscriber>>();
 		subscribers.add(CreateDomainAuthorizationSubscriber.class);
 		subscribers.add(UserActivatedSubscriber.class);
 		subscribers.add(UserRoleRemovedSubscriber.class);
 		return subscribers;
 	}
 
 	public Set<ActionSpec> getModuleActions() {
 		Set<ActionSpec> actions = new HashSet<ActionSpec>();
 		
 		// authorization controller actions
 		actions.add(new ActionSpec("ListRoles", true));
 		actions.add(new ActionSpec("NewRole", true));
 		actions.add(new ActionSpec("RemoveRole", true));
 		actions.add(new ActionSpec("ListUsersAndRoles", true));
 		actions.add(new ActionSpec("AddUserRole", true));
 		actions.add(new ActionSpec("RemoveUserRole", true));
 		actions.add(new ActionSpec("RemoveUserRoles", true));
 		actions.add(new ActionSpec("AddRoleActivity", true));
 		actions.add(new ActionSpec("RemoveRoleActivity", true));
 
 		// request authorization controller actions
 		actions.add(new ActionSpec("AcceptRequestUser", true));
 		actions.add(new ActionSpec("RejectRequestUser", true));
 		return actions;
 	}
 }
