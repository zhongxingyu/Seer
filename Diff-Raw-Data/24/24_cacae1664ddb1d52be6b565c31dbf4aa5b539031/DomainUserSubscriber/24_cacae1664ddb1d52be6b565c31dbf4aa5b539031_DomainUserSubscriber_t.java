 /*
  *  Figgo - https://www.ohloh.net/p/figgo/
  *  Copyright (C) 2011  Octahedron - FESL
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
 package br.octahedron.figgo.modules.user.manager;
 
 import static br.octahedron.figgo.modules.configuration.DomainUtils.*;
 import br.octahedron.cotopaxi.datastore.jdo.PersistenceManagerPool;
import br.octahedron.cotopaxi.datastore.namespace.NamespaceManager;
 import br.octahedron.cotopaxi.eventbus.Event;
 import br.octahedron.cotopaxi.eventbus.InterestedEvent;
 import br.octahedron.cotopaxi.eventbus.Subscriber;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.figgo.modules.admin.manager.DomainCreatedEvent;
 import br.octahedron.figgo.modules.configuration.manager.DomainChangedEvent;
 
 /**
  * This subscriber listen for domain changes (creation or update) to create an domain user account.
  * The domain user account exists only to facilitate the UX, providing auto complete feature to work
  * properly with the Domain Accounts.
  * 
  * 
  * @author Danilo Queiroz - dpenna.queiroz@gmail.com
  */
 @InterestedEvent(events = { DomainChangedEvent.class, DomainCreatedEvent.class })
 public class DomainUserSubscriber implements Subscriber {
 	// TODO review received events - events must be less specific
 	
 	@Inject
 	private UserManager userManager;
 	
	@Inject
	private NamespaceManager namespaceManager;
	
 	/**
 	 * @param userManager the userManager to set
 	 */
 	public void setUserManager(UserManager userManager) {
 		this.userManager = userManager;
 	}
	
	/**
	 * @param namespaceManager the namespaceManager to set
	 */
	public void setNamespaceManager(NamespaceManager namespaceManager) {
		this.namespaceManager = namespaceManager;
	}
 
 	/*
 	 * (non-Javadoc)
 	 */
 	@Override
 	public void eventPublished(Event event) {
 		String domain;
 		String name = "";
 		try {
 			DomainChangedEvent evt = (DomainChangedEvent) event;
 			domain = evt.getDomainConfiguration().getDomainName();
 			name = evt.getDomainConfiguration().getName();
 		} catch (ClassCastException e) {
 			DomainCreatedEvent evt = (DomainCreatedEvent) event;
 			domain = evt.getNamespace();
		} finally {
			this.namespaceManager.changeToPreviousNamespace();
 		}
 		this.persistUser(domain, name);
 	}
 
 	/**
 	 * Persists the user
 	 */
 	private void persistUser(String domain, String name) {
 		String userId = generateDomainUserID(domain);
		this.namespaceManager.changeToGlobalNamespace();
 		if (this.userManager.existsUser(userId)) { 
 			this.userManager.updateUser(userId, name, null, name);
 		} else {
 			this.userManager.createUser(userId, name, null, name);
 		}
		this.namespaceManager.changeToPreviousNamespace();
 		PersistenceManagerPool.forceClose();
 	}
 
 }
