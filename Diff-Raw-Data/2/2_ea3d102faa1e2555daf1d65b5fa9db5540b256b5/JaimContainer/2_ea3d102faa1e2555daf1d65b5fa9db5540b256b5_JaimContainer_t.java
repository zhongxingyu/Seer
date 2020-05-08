 /*******************************************************************************
 * Copyright (c) 2009 Composent, Inc., All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.provider.aim;
 
 import org.eclipse.ecf.core.AbstractContainer;
 import org.eclipse.ecf.core.ContainerConnectException;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.Namespace;
 import org.eclipse.ecf.core.security.IConnectContext;
 import org.eclipse.ecf.core.user.IUser;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.presence.IAccountManager;
 import org.eclipse.ecf.presence.IPresenceListener;
 import org.eclipse.ecf.presence.chatroom.IChatRoomManager;
 import org.eclipse.ecf.presence.im.IChatManager;
 import org.eclipse.ecf.presence.roster.IRosterManager;
 import org.eclipse.ecf.presence.search.ICriteria;
 import org.eclipse.ecf.presence.search.IRestriction;
 import org.eclipse.ecf.presence.search.ISearch;
 import org.eclipse.ecf.presence.search.IUserSearchListener;
 import org.eclipse.ecf.presence.search.IUserSearchManager;
 import org.eclipse.ecf.presence.search.UserSearchException;
 import org.eclipse.ecf.presence.service.IPresenceService;
 
 public class JaimContainer extends AbstractContainer implements IPresenceService {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.core.IContainer#connect(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.security.IConnectContext)
 	 */
 	public void connect(ID targetID, IConnectContext connectContext)
 			throws ContainerConnectException {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.core.IContainer#disconnect()
 	 */
 	public void disconnect() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void addPresenceListener(IPresenceListener listener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public IAccountManager getAccountManager() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IChatRoomManager getChatRoomManager() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void removePresenceListener(IPresenceListener listener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public IRosterManager getRosterManager() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IUser getUser() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.presence.IPresenceContainerAdapter#getChatManager()
 	 */
 	public IChatManager getChatManager() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.core.IContainer#getConnectNamespace()
 	 */
 	public Namespace getConnectNamespace() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.core.IContainer#getConnectedID()
 	 */
 	public ID getConnectedID() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
 	 */
 	public ID getID() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IUserSearchManager getUserSearchManager() {
 		return new IUserSearchManager(){
 
 			public ICriteria createCriteria() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			public IRestriction createRestriction() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			public String[] getUserPropertiesFields() throws ECFException {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			public boolean isEnabled() {
 				return false;
 			}
 
 			public ISearch search(ICriteria criteria)
 					throws UserSearchException {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			public void search(ICriteria criteria, IUserSearchListener listener) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		};
 	}
 
 
 }
