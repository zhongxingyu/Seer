 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ssh.internal.core;
 
 import java.util.ArrayList;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jsch.core.IJSchService;
 
 import com.jcraft.jsch.ChannelSftp;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.UIKeyboardInteractive;
 import com.jcraft.jsch.UserInfo;
 
 public class ChannelPool {
 
 	private final long inactivityTimeout;
 	private final String userName;
 	private final int port;
 	private final String hostName;
 
 	private String password;
 
 	private Session session;
 	private final List<ChannelSftp> freeChannels = new ArrayList<ChannelSftp>();
 	private final Map<ChannelSftp, ChannelUsageInfo> usedChannels = new IdentityHashMap<ChannelSftp, ChannelUsageInfo>();
 
 	private static class ChannelUsageInfo {
 		final Object context;
 		final long timestamp;
 
 		public ChannelUsageInfo(Object context) {
 			this.context = context;
 			this.timestamp = System.currentTimeMillis();
 		}
 
 	}
 
 	/**
 	 * @param userName
 	 * @param hostName
 	 * @param port
 	 */
 	public ChannelPool(String userName, String hostName, int port,
 			long inactivityTimeout) {
 		this.userName = userName;
 		this.hostName = hostName;
 		this.port = port;
 		this.inactivityTimeout = inactivityTimeout;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	private final class LocalUserInfo implements UserInfo,
 			UIKeyboardInteractive {
 		public void showMessage(String arg0) {
 		}
 
 		public boolean promptYesNo(String arg0) {
 			return false;
 		}
 
 		public boolean promptPassword(String arg0) {
 			return true;
 		}
 
 		public boolean promptPassphrase(String arg0) {
 			return false;
 		}
 
 		public String getPassword() {
 			return password;
 		}
 
 		public String getPassphrase() {
 			return ""; //$NON-NLS-1$
 		}
 
 		public String[] promptKeyboardInteractive(String destination,
 				String name, String instruction, String[] prompt, boolean[] echo) {
 			final String p = password;
 			return p != null ? new String[] { p } : null;
 		}
 	}
 
 	private static boolean DEBUG = false;
 
 	protected void connectSession() throws JSchException {
 		synchronized (lock) {
 			if (session == null) {
 				IJSchService service = Activator.getDefault().getJSch();
 				session = service.createSession(hostName, port, userName);
 				session.setTimeout(0);
 				session.setServerAliveInterval(300000);
 				session.setServerAliveCountMax(6);
 				session.setPassword(password); // Set password
 				// directly
 				UserInfo ui = new LocalUserInfo();
 				session.setUserInfo(ui);
 			}
 
 			if (!session.isConnected()) {
 				// Connect with default timeout
 				if (DEBUG) {
 					log("session.connect()"); //$NON-NLS-1$
 				}
 				session.connect(60 * 1000);
 				if (DEBUG) {
 					log("...connected"); //$NON-NLS-1$
 				}
 			}
 		}
 	}
 
 	private final Object channelNotifier = new Object();
 	private final Object lock = new Object();
 
 	protected ChannelSftp acquireChannel(final Object context, long timeout) {
 		final long start = System.currentTimeMillis();
 		for (;;) {
 			try {
 				return acquireChannel(context);
 			} catch (JSchException e) {
 				if (isOutOfChannels(e)) {
 					if (tryCloseOldChannels()) {
 						continue;
 					}
 				}
 				if (System.currentTimeMillis() - start > timeout) {
 					Activator.error("Failed to create direct connection", e); //$NON-NLS-1$					
 					return null;
 				}
 				if (DEBUG) {
 					log(" <sleep>"); //$NON-NLS-1$
 				}
 				try {
 					synchronized (channelNotifier) {
 						channelNotifier.wait(1000);
 					}
 				} catch (InterruptedException e1) {
 					return null;
 				}
 			}
 		}
 	}
 
 	private boolean isOutOfChannels(JSchException e) {
 		return CHANNEL_IS_NOT_OPENED.equals(e.getMessage());
 	}
 
 	private static final String CHANNEL_IS_NOT_OPENED = "channel is not opened."; //$NON-NLS-1$
 
 	protected ChannelSftp acquireChannel(Object context) throws JSchException {
 		connectSession();
 		if (DEBUG) {
 			log("<acquireChannel> " + context); //$NON-NLS-1$
 		}
 		synchronized (lock) {
 			while (!freeChannels.isEmpty()) {
 				final ChannelSftp channel = freeChannels.remove(freeChannels
 						.size() - 1);
 				if (channel.isConnected()) {
 					usedChannels.put(channel, createUsageInfo(context));
 					return channel;
 				}
 			}
 		}
 		final ChannelSftp channel = (ChannelSftp) session.openChannel("sftp"); //$NON-NLS-1$			
 		if (!channel.isConnected()) {
 			if (DEBUG) {
 				log("channel.connect()"); //$NON-NLS-1$
 			}
			channel.connect();
 		}
 		synchronized (lock) {
 			usedChannels.put(channel, createUsageInfo(context));
 		}
 		return channel;
 		// String eToStr = e.toString();
 		// if (eToStr.indexOf("Auth cancel") >= 0 || eToStr.indexOf("Auth fail") >= 0 || eToStr.indexOf("session is down") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		// if (session.isConnected()) {
 		// session.disconnect();
 		// session = null;
 		// }
 		// }
 		// if (needLog) {
 		// Activator.error("Failed to create direct connection", e); //$NON-NLS-1$
 		// }
 		// if (session != null) {
 		// session.disconnect();
 		// session = null;
 		// }
 	}
 
 	/**
 	 * @return
 	 */
 	private ChannelUsageInfo createUsageInfo(Object context) {
 		return new ChannelUsageInfo(context);
 	}
 
 	protected void releaseChannel(ChannelSftp channel) {
 		if (DEBUG) {
 			log("<releaseChannel>"); //$NON-NLS-1$
 		}
 		synchronized (lock) {
 			if (usedChannels.remove(channel) != null) {
 				freeChannels.add(channel);
 			} else {
 				channel.disconnect();
 			}
 		}
 		synchronized (channelNotifier) {
 			channelNotifier.notifyAll();
 		}
 	}
 
 	protected void destroyChannel(ChannelSftp channel) {
 		if (DEBUG) {
 			log("<destroyChannel>"); //$NON-NLS-1$
 		}
 		synchronized (lock) {
 			usedChannels.remove(channel);
 		}
 		channel.disconnect();
 	}
 
 	private boolean tryCloseOldChannels() {
 		synchronized (lock) {
 			if (!usedChannels.isEmpty()) {
 				ChannelSftp selectedChannel = null;
 				ChannelUsageInfo selectedUsageInfo = null;
 				long selectedLastActivity = 0;
 				for (Map.Entry<ChannelSftp, ChannelUsageInfo> entry : usedChannels
 						.entrySet()) {
 					final ChannelUsageInfo usageInfo = entry.getValue();
 					if (canClose(usageInfo.context)) {
 						final long lastActivity = getLastActivity(usageInfo.context);
 						if (lastActivity != Long.MIN_VALUE) {
 							if (selectedChannel == null
 									|| lastActivity < selectedLastActivity) {
 								selectedChannel = entry.getKey();
 								selectedUsageInfo = usageInfo;
 								selectedLastActivity = lastActivity;
 							}
 						}
 					}
 				}
 				if (selectedChannel != null) {
 					final long currentTime = System.currentTimeMillis();
 					if (currentTime - selectedLastActivity > inactivityTimeout) {
 						Activator
 								.warn("Close active channel \"" + selectedUsageInfo.context + "\" created " + (currentTime - selectedUsageInfo.timestamp) + "ms ago, lastActivity=" + (currentTime - selectedLastActivity) + "ms ago"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 						if (DEBUG) {
 							log(" channel.disconnect() " + selectedUsageInfo.context); //$NON-NLS-1$
 						}
 						selectedChannel.disconnect();
 						usedChannels.remove(selectedChannel);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Tests if channel allocated for this context could be closed.
 	 * 
 	 * @param context
 	 * @return
 	 */
 	protected boolean canClose(Object context) {
 		return false;
 	}
 
 	/**
 	 * Returns the time of last activity in the channel allocated for this
 	 * context.
 	 * 
 	 * @param context
 	 * @return
 	 */
 	protected long getLastActivity(Object context) {
 		return Long.MIN_VALUE;
 	}
 
 	public void disconnect() {
 		synchronized (lock) {
 			for (ChannelSftp channel : freeChannels) {
 				if (DEBUG) {
 					log("channel.disconnect()"); //$NON-NLS-1$
 				}
 				channel.disconnect();
 			}
 			freeChannels.clear();
 			for (Map.Entry<ChannelSftp, ChannelUsageInfo> entry : usedChannels
 					.entrySet()) {
 				final ChannelUsageInfo usageInfo = entry.getValue();
 				Activator
 						.warn("Close active channel \"" + usageInfo.context + "\" created " + (System.currentTimeMillis() - usageInfo.timestamp) + "ms ago"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				if (DEBUG) {
 					log(" channel.disconnect() " + usageInfo.context); //$NON-NLS-1$
 				}
 				entry.getKey().disconnect();
 			}
 			usedChannels.clear();
 			if (session != null) {
 				if (DEBUG) {
 					log("session.disconnect()"); //$NON-NLS-1$
 				}
 				session.disconnect();
 				session = null;
 			}
 		}
 	}
 
 	private static final long loadedAt = System.currentTimeMillis();
 
 	protected void log(Object message) {
 		System.out
 				.println("[" + (System.currentTimeMillis() - loadedAt) + "] " + message); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	public boolean isConnected() {
 		return session != null && session.isConnected();
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((hostName == null) ? 0 : hostName.hashCode());
 		result = prime * result + port;
 		result = prime * result
 				+ ((userName == null) ? 0 : userName.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ChannelPool other = (ChannelPool) obj;
 		if (hostName == null) {
 			if (other.hostName != null)
 				return false;
 		} else if (!hostName.equals(other.hostName))
 			return false;
 		if (port != other.port)
 			return false;
 		if (userName == null) {
 			if (other.userName != null)
 				return false;
 		} else if (!userName.equals(other.userName))
 			return false;
 		return true;
 	}
 
 }
