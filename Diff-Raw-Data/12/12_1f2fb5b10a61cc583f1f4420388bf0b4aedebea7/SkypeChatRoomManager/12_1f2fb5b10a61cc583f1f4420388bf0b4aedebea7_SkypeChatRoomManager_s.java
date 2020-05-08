 /****************************************************************************
  * Copyright (c) 2004 Composent, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Composent, Inc. - initial API and implementation
  *****************************************************************************/
 
 package org.eclipse.ecf.provider.skype;
 
 import java.util.Map;
 
 import org.eclipse.ecf.presence.chatroom.ChatRoomCreateException;
 import org.eclipse.ecf.presence.chatroom.IChatRoomInfo;
 import org.eclipse.ecf.presence.chatroom.IChatRoomInvitationListener;
 import org.eclipse.ecf.presence.chatroom.IChatRoomManager;
 import org.eclipse.ecf.presence.history.IHistoryManager;
 
 /**
  *
  */
 public class SkypeChatRoomManager implements IChatRoomManager {
 
 	public SkypeChatRoomManager() {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.presence.chatroom.IChatRoomManager#addInvitationListener(org.eclipse.ecf.presence.chatroom.IChatRoomInvitationListener)
 	 */
 	public void addInvitationListener(IChatRoomInvitationListener listener) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.presence.chatroom.IChatRoomManager#createChatRoom(java.lang.String, java.util.Map)
 	 */
 	public IChatRoomInfo createChatRoom(String roomname, Map properties)
 			throws ChatRoomCreateException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.presence.chatroom.IChatRoomManager#getChatRoomInfo(java.lang.String)
 	 */
 	public IChatRoomInfo getChatRoomInfo(String roomname) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.presence.chatroom.IChatRoomManager#getChatRoomInfos()
 	 */
 	public IChatRoomInfo[] getChatRoomInfos() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.presence.chatroom.IChatRoomManager#getChildren()
 	 */
 	public IChatRoomManager[] getChildren() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.presence.chatroom.IChatRoomManager#getHistoryManager()
 	 */
 	public IHistoryManager getHistoryManager() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.presence.chatroom.IChatRoomManager#getParent()
 	 */
 	public IChatRoomManager getParent() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.presence.chatroom.IChatRoomManager#removeInvitationListener(org.eclipse.ecf.presence.chatroom.IChatRoomInvitationListener)
 	 */
 	public void removeInvitationListener(IChatRoomInvitationListener listener) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class adapter) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * 
 	 */
 	protected void dispose() {
 	}
 
 	/**
 	 * 
 	 */
 	public void disconnect() {
		// TODO Auto-generated method stub	
 	}
 
 }
