 /*******************************************************************************
  * Copyright (c) 2004 Composent, Inc., Peter Nehrer, Boris Bokowski. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.provider.aim;
 
 import org.eclipse.ecf.core.ContainerConnectException;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.IContainerListener;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.Namespace;
 import org.eclipse.ecf.core.security.IConnectContext;
 import org.eclipse.ecf.presence.IAccountManager;
 import org.eclipse.ecf.presence.IMessageListener;
 import org.eclipse.ecf.presence.IMessageSender;
 import org.eclipse.ecf.presence.IPresenceContainerAdapter;
 import org.eclipse.ecf.presence.IPresenceListener;
 import org.eclipse.ecf.presence.IPresenceSender;
 import org.eclipse.ecf.presence.IRosterSubscribeListener;
 import org.eclipse.ecf.presence.chat.IChatRoomManager;
 
 public class JaimContainer implements IContainer, IPresenceContainerAdapter {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.core.IContainer#addListener(org.eclipse.ecf.core.IContainerListener, java.lang.String)
 	 */
 	public void addListener(IContainerListener listener) {
 		// TODO Auto-generated method stub
 
 	}
 
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
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.core.IContainer#dispose()
 	 */
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.core.IContainer#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class serviceType) {
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
 	 * @see org.eclipse.ecf.core.IContainer#removeListener(org.eclipse.ecf.core.IContainerListener)
 	 */
 	public void removeListener(IContainerListener listener) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.core.IIdentifiable#getID()
 	 */
 	public ID getID() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void addMessageListener(IMessageListener listener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void addPresenceListener(IPresenceListener listener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void addRosterSubscribeListener(IRosterSubscribeListener listener) {
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
 
 	public IMessageSender getMessageSender() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IPresenceSender getPresenceSender() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void removeMessageListener(IMessageListener listener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void removePresenceListener(IPresenceListener listener) {
 		// TODO Auto-generated method stub
 		
 	}
 
	public void removeSubscribeListener(IRosterSubscribeListener listener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
