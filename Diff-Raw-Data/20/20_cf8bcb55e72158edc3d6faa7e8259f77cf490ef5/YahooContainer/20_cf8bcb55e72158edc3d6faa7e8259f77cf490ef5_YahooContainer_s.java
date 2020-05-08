 /**
  * Copyright (c) 2002-2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	- Initial API and implementation
  *  	- Chris Aniszczyk <zx@us.ibm.com>
  *   	- Borna Safabakhsh <borna@us.ibm.com> 
  *   
  * $Id$
  */
 package org.eclipse.ecf.provider.yahoo.container;
 
 import java.io.IOException;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 
 import org.eclipse.ecf.core.AbstractContainer;
 import org.eclipse.ecf.core.ContainerConnectException;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.identity.IDInstantiationException;
 import org.eclipse.ecf.core.identity.Namespace;
 import org.eclipse.ecf.core.security.IConnectContext;
 import org.eclipse.ecf.core.security.ObjectCallback;
 import org.eclipse.ecf.presence.IPresence;
 import org.eclipse.ecf.presence.IPresenceContainer;
 import org.eclipse.ecf.presence.IRosterEntry;
 import org.eclipse.ecf.presence.IRosterGroup;
 import org.eclipse.ecf.presence.impl.RosterEntry;
 import org.eclipse.ecf.presence.impl.RosterGroup;
 import org.eclipse.ecf.provider.yahoo.Activator;
 import org.eclipse.ecf.provider.yahoo.identity.YahooID;
 import org.eclipse.ecf.provider.yahoo.util.YahooSessionListener;
 
 import ymsg.network.AccountLockedException;
 import ymsg.network.LoginRefusedException;
 import ymsg.network.Session;
 import ymsg.network.YahooGroup;
 import ymsg.network.YahooUser;
 
 public class YahooContainer extends AbstractContainer {
 	
 	/** Represents the YMSG session object provided by the ymsg library */
 	private Session session;
 	
 	/** Entity identifier within the ECF namespace for the local Container entity */
 	private ID localID;
 	
 	/** Entity identifier within the Yahoo! namespace for the target server or group entity */
 	private YahooID targetYahooID;
 	
 	/** Presence container instance used for adapting this container to a present container */
 	private YahooPresenceContainer presenceContainer;
 	
 	public YahooContainer(ID id) {
 		this.localID = id;
 		session = new Session();
 		presenceContainer = new YahooPresenceContainer(session);
 	}
 
 	public void connect(ID targetID, IConnectContext connectContext) throws ContainerConnectException {
 		String password = getPassword(connectContext);
 		this.targetYahooID = (YahooID) targetID;
 		try {
 			session.login(targetYahooID.getUsername(), password);
 		} catch (AccountLockedException e) {
			e.printStackTrace();
 		} catch (IllegalStateException e) {
			e.printStackTrace();
 		} catch (LoginRefusedException e) {
			e.printStackTrace();
 		} catch (IOException e) {
			e.printStackTrace();
 		}
 		session.addSessionListener(new YahooSessionListener(this,presenceContainer));
 		presenceContainer.fireContainerJoined(getConnectedID());
 		populateRoster(session.getGroups());
 	}
 	
 	private void populateRoster(YahooGroup[] groups) {
 		for(int i = 0; i < groups.length; i++) {
 			IRosterGroup group = new RosterGroup(groups[i].getName());
 			for(int j = 0; j < groups[i].getMembers().size(); j++) {
 				YahooUser u = (YahooUser) groups[i].getMembers().get(j);
 				IRosterEntry entry = makeRosterEntry(u);
 				entry.add(group);
 				presenceContainer.fireRosterEntry(entry);
 			}
 		}
 	}
 	
 	/**
 	 * Creates a Roster entry in the YMSG Buddy List for each buddy.
 	 * Each roster entry includes the targetID (representing this session),
 	 * the userID (created to represent the buddy in ECF), and the userName 
 	 * @param user YahooUser to add to ECF buddy list
 	 * @return returns roster entry representing this user in the buddy list
 	 */
 	protected IRosterEntry makeRosterEntry(YahooUser user) { 
 		String userName = user.getId();
 		ID userID;
 		IRosterEntry entry;
 			try {
 				userID = IDFactory.getDefault().createID(targetYahooID.getNamespace(), userName);
 				entry = new RosterEntry(targetYahooID, userID, userName);
 				IPresence presence = presenceContainer.createPresence(userID.getName());
 				entry.setPresenceState(presence);
 				return entry;
 			} catch (IDInstantiationException e) {
 				e.printStackTrace();
 			}
 			return null;
 	}
 
 	public ID getConnectedID() {
 		return this.targetYahooID;
 	}
 
 	public Namespace getConnectNamespace() {
 		return IDFactory.getDefault().getNamespaceByName(Activator.NAMESPACE_IDENTIFIER);
 	}
 
 	public void disconnect() {
 		try {
			session.logout();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		presenceContainer.fireContainerDeparted(getConnectedID());
 	}
 
 	public Object getAdapter(Class serviceType) {
 		if (serviceType.equals(IPresenceContainer.class)) {
 			return presenceContainer;
 		}
 		return super.getAdapter(serviceType);
 	}
 	
 	public void dispose() {
 		disconnect();
 	};
 	
 	public ID getID() {
 		return this.localID;
 	}
 	
 	private String getPassword(IConnectContext connectContext) {
 		// Get password via callback in connectContext
 		String pw = null;
 		try {
 			Callback[] callbacks = new Callback[1];
 			callbacks[0] = new ObjectCallback();
 			if (connectContext != null) {
 				CallbackHandler handler = connectContext.getCallbackHandler();
 				if (handler != null) {
 					handler.handle(callbacks);
 				}
 			}
 			ObjectCallback cb = (ObjectCallback) callbacks[0];
 			pw = (String) cb.getObject();
 		} catch (Exception e) {
 			System.out.println(e.getStackTrace());
 		}
 		return pw;
 	}
 
 }
