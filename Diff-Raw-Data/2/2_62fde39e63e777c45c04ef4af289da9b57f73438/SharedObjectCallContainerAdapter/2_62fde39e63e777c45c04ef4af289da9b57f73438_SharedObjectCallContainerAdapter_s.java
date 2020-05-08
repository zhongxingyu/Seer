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
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.ecf.call.CallException;
 import org.eclipse.ecf.call.ICallContainerAdapter;
 import org.eclipse.ecf.call.ICallSessionListener;
 import org.eclipse.ecf.call.ICallSessionRequestListener;
 import org.eclipse.ecf.call.IReceiverCallSession;
 import org.eclipse.ecf.call.events.ICallSessionRequestEvent;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.identity.Namespace;
 import org.eclipse.ecf.core.sharedobject.BaseSharedObject;
 import org.eclipse.ecf.core.sharedobject.SharedObjectInitException;
 import org.eclipse.ecf.core.util.Trace;
 import org.eclipse.ecf.internal.provider.skype.Activator;
 import org.eclipse.ecf.internal.provider.skype.Messages;
 import org.eclipse.ecf.internal.provider.skype.SkypeProviderDebugOptions;
 import org.eclipse.ecf.provider.skype.identity.SkypeCallSessionID;
 import org.eclipse.ecf.provider.skype.identity.SkypeUserID;
 import org.eclipse.ecf.provider.skype.identity.SkypeUserNamespace;
 
 import com.skype.Call;
 import com.skype.CallListener;
 import com.skype.ChatMessage;
 import com.skype.ChatMessageListener;
 import com.skype.Profile;
 import com.skype.Skype;
 import com.skype.SkypeException;
 import com.skype.connector.Connector;
 import com.skype.connector.ConnectorListener;
 import com.skype.connector.ConnectorMessageEvent;
 import com.skype.connector.ConnectorStatusEvent;
 
 public class SharedObjectCallContainerAdapter extends BaseSharedObject
 		implements ICallContainerAdapter {
 
	boolean debugSkype = true;
 
 	String skypeVersion;
 
 	Profile userProfile;
 
 	SkypeUserID userID;
 
 	Vector callSessionRequestListeners = new Vector();
 
 	CallListener callListener = new CallListener() {
 		public void callMaked(Call makedCall) throws SkypeException {
 			Trace.trace(Activator.PLUGIN_ID, "callMade(" + makedCall + ")"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		public void callReceived(Call receivedCall) throws SkypeException {
 			Trace.trace(Activator.PLUGIN_ID, "callReceived(" + receivedCall //$NON-NLS-1$
 					+ ")"); //$NON-NLS-1$
 
 			fireCallReceived(receivedCall);
 		}
 	};
 
 	ChatMessageListener chatMessageListener = new ChatMessageListener() {
 
 		public void chatMessageReceived(ChatMessage chatMessageReceived)
 				throws SkypeException {
 			// TODO Auto-generated method stub
 			Trace
 					.trace(
 							Activator.PLUGIN_ID,
 							"chatMessageReceived(id=" //$NON-NLS-1$
 									+ chatMessageReceived.getId()
 									+ ";content=" + chatMessageReceived.getContent() + ";senderid=" + chatMessageReceived.getSenderId() + ";sendername=" + chatMessageReceived.getSenderDisplayName() + ")"); //$NON-NLS-1$
 		}
 
 		public void chatMessageSent(ChatMessage sentChatMessage)
 				throws SkypeException {
 			// TODO Auto-generated method stub
 			Trace
 					.trace(
 							Activator.PLUGIN_ID,
 							"chatMessageSent(id=" //$NON-NLS-1$
 									+ sentChatMessage.getId()
 									+ ";content=" + sentChatMessage.getContent() + ";senderid=" + sentChatMessage.getSenderId() + ";sendername=" + sentChatMessage.getSenderDisplayName() + ")"); //$NON-NLS-1$
 		}
 
 	};
 
 	ConnectorListener connectorListener = new ConnectorListener() {
 
 		public void messageReceived(ConnectorMessageEvent event) {
 			// TODO Auto-generated method stub
 			Trace.trace(Activator.PLUGIN_ID, "messageReceived(" //$NON-NLS-1$
 					+ event.getMessage() + ")"); //$NON-NLS-1$
 		}
 
 		public void messageSent(ConnectorMessageEvent event) {
 			// TODO Auto-generated method stub
 			Trace.trace(Activator.PLUGIN_ID, "messageSent(" //$NON-NLS-1$
 					+ event.getMessage() + ")"); //$NON-NLS-1$
 		}
 
 		public void statusChanged(ConnectorStatusEvent event) {
 			// TODO Auto-generated method stub
 			Trace.trace(Activator.PLUGIN_ID, "statusChanged(" //$NON-NLS-1$
 					+ event.getStatus() + ")"); //$NON-NLS-1$
 		}
 
 	};
 
 	protected SkypeUserID getUserID() {
 		return userID;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.sharedobject.BaseSharedObject#dispose(org.eclipse.ecf.core.identity.ID)
 	 */
 	public void dispose(ID containerID) {
 		super.dispose(containerID);
 		Skype.removeCallListener(callListener);
 		Skype.removeChatMessageListener(chatMessageListener);
 		try {
 			Skype.setDebug(false);
 		} catch (SkypeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param receivedCall
 	 */
 	protected void fireCallReceived(final Call receivedCall) {
 		synchronized (callSessionRequestListeners) {
 			for (Iterator i = callSessionRequestListeners.iterator(); i
 					.hasNext();) {
 				ICallSessionRequestListener l = (ICallSessionRequestListener) i
 						.next();
 				l.handleCallSessionRequest(new ICallSessionRequestEvent() {
 
 					public IReceiverCallSession accept(
 							ICallSessionListener listener) throws CallException {
 						try {
 							SkypeReceiverCallSession session = new SkypeReceiverCallSession(
 									userID, new SkypeUserID(receivedCall
 											.getPartner()), receivedCall,
 									listener);
 							receivedCall.answer();
 							return session;
 						} catch (SkypeException e) {
 							throw new CallException("unexpected exception", e);
 						}
 					}
 
 					public Map getProperties() {
 						// XXX todo...get from Skype Call
 						return new HashMap();
 					}
 
 					public void reject() {
 						try {
 							receivedCall.cancel();
 						} catch (SkypeException e) {
 							// TODO log
 							e.printStackTrace();
 							return;
 						}
 					}
 
 					public ID getInitiator() {
 						try {
 							return new SkypeUserID(receivedCall.getPartner());
 						} catch (SkypeException e) {
 							// TODO log
 							e.printStackTrace();
 							return null;
 						}
 					}
 
 					public ID getReceiver() {
 						return userID;
 					}
 
 					public ID getSessionID() {
 						return new SkypeCallSessionID(receivedCall.getId());
 					}
 
 				});
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.sharedobject.BaseSharedObject#initialize()
 	 */
 	protected void initialize() throws SharedObjectInitException {
 		super.initialize();
 		try {
 			Connector.getInstance().addConnectorListener(connectorListener);
 			skypeVersion = Skype.getVersion();
 
 			userProfile = Skype.getProfile();
 			userID = new SkypeUserID(userProfile.getId());
 			Skype.setDeamon(true);
 			Trace
 					.trace(
 							Activator.PLUGIN_ID,
 							"ECF Skype Adapter initializing with version: " + skypeVersion); //$NON-NLS-1$
 			Skype.addCallListener(callListener);
 			Skype.addChatMessageListener(chatMessageListener);
 
 			if (debugSkype) {
 				Connector.getInstance().setDebugOut(
 						new PrintWriter(new Writer() {
 							public void write(char[] cbuf, int off, int len)
 									throws IOException {
 								// XXX TODO
 								Trace.trace(Activator.PLUGIN_ID, "SKYPEDEBUG." //$NON-NLS-1$
 										+ new String(cbuf, off, len));
 							}
 
 							public void flush() throws IOException {
 							}
 
 							public void close() throws IOException {
 							}
 						}));
 				Connector.getInstance().setDebug(true);
 
 			}
 		} catch (Exception e) {
 			Trace.catching(Activator.PLUGIN_ID,
 					SkypeProviderDebugOptions.EXCEPTIONS_CATCHING, this
 							.getClass(), "initialize", e); //$NON-NLS-1$
 			Trace.throwing(Activator.PLUGIN_ID,
 					SkypeProviderDebugOptions.EXCEPTIONS_THROWING, this
 							.getClass(), "initialize", e); //$NON-NLS-1$
 			throw new SharedObjectInitException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.call.ICallContainerAdapter#getCallSessionNamespace()
 	 */
 	public Namespace getReceiverNamespace() {
 		return IDFactory.getDefault().getNamespaceByName(
 				SkypeUserNamespace.NAMESPACE_NAME);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.call.ICallContainerAdapter#addCallSessionRequestListener(org.eclipse.ecf.call.ICallSessionRequestListener)
 	 */
 	public void addCallSessionRequestListener(
 			ICallSessionRequestListener listener) {
 		callSessionRequestListeners.add(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.call.ICallContainerAdapter#removeCallSessionRequestListener(org.eclipse.ecf.call.ICallSessionRequestListener)
 	 */
 	public void removeCallSessionRequestListener(
 			ICallSessionRequestListener listener) {
 		callSessionRequestListeners.remove(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.call.ICallContainerAdapter#sendCallRequest(org.eclipse.ecf.core.identity.ID[],
 	 *      org.eclipse.ecf.call.ICallSessionListener, java.util.Map)
 	 */
 	public void sendCallRequest(ID[] receivers, ICallSessionListener listener,
 			Map properties) throws CallException {
 		throw new CallException("conference call request not yet supported");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.call.ICallContainerAdapter#sendCallRequest(org.eclipse.ecf.core.identity.ID,
 	 *      org.eclipse.ecf.call.ICallSessionListener, java.util.Map)
 	 */
 	public void sendCallRequest(final ID receiver,
 			ICallSessionListener listener, Map options) throws CallException {
 		Assert.isNotNull(listener,
 				Messages.SharedObjectCallContainerAdapter_Exception_Not_Null);
 		if (receiver instanceof SkypeUserID) {
 			SkypeUserID rcvrID = (SkypeUserID) receiver;
 			synchronized (this) {
 				try {
 					new SkypeInitiatorCallSession(userID, rcvrID, Skype
 							.call(rcvrID.getUser()), listener);
 				} catch (SkypeException e) {
 					Trace.catching(Activator.PLUGIN_ID,
 							SkypeProviderDebugOptions.EXCEPTIONS_CATCHING, this
 									.getClass(), "sendInitiateCall", e); //$NON-NLS-1$
 					Trace.throwing(Activator.PLUGIN_ID,
 							SkypeProviderDebugOptions.EXCEPTIONS_THROWING, this
 									.getClass(), "sendInitiateCall", e); //$NON-NLS-1$
 					throw new CallException(
 							Messages.SharedObjectCallContainerAdapter_Exception_Skype,
 							e);
 				}
 			}
 		} else
 			throw new CallException(
 					Messages.SkypeCallSession_Exception_Invalid_Receiver);
 	}
 
 }
