 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.log.core.internal;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.core.listeners.interfaces.IChannelStateChangeListener;
 import org.eclipse.tcf.te.tcf.core.listeners.interfaces.IProtocolStateChangeListener;
 import org.eclipse.tcf.te.tcf.log.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.log.core.events.MonitorEvent;
 import org.eclipse.tcf.te.tcf.log.core.interfaces.IPreferenceKeys;
 import org.eclipse.tcf.te.tcf.log.core.internal.listener.ChannelStateChangeListener;
 import org.eclipse.tcf.te.tcf.log.core.internal.nls.Messages;
 
 
 /**
  * TCF logging log manager implementation.
  */
 public final class LogManager implements IProtocolStateChangeListener {
 	/**
 	 * Time format representing date and time with milliseconds.
 	 */
 	public final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //$NON-NLS-1$
 
 	// Reference to the channel state change listener
 	private IChannelStateChangeListener channelStateChangeListener;
 
 	// Maps file writer per log file base name
 	private final Map<String, FileWriter> fileWriterMap = new HashMap<String, FileWriter>();
 
 	// Maximum log file size in bytes
 	private long maxFileSize;
 	// Maximum number of files in cycle
 	private int maxInCycle;
 
 	/*
 	 * Thread save singleton instance creation.
 	 */
 	private static class LazyInstance {
 		public static LogManager instance = new LogManager();
 	}
 
 	/**
 	 * Constructor.
 	 */
 	/* default */ LogManager() {
 		super();
 
 		// initialize from preferences
 		initializeFromPreferences();
 	}
 
 	/**
 	 * Returns the singleton instance.
 	 */
 	public static LogManager getInstance() {
 		return LazyInstance.instance;
 	}
 
 	/**
 	 * Dispose the log manager instance.
 	 */
 	public void dispose() {
 		String message = NLS.bind(Messages.ChannelTraceListener_logManagerDispose_message,
 								  DATE_FORMAT.format(new Date(System.currentTimeMillis())));
 		for (FileWriter writer : fileWriterMap.values()) {
 			try {
 				writer.write(message);
 				writer.write("\n"); //$NON-NLS-1$
 			} catch (IOException e) {
 				/* ignored on purpose */
 			} finally {
 				try {
 					writer.flush();
 					writer.close();
 				} catch (IOException e) {
 					/* ignored on purpose */
 				}
 			}
 		}
 		fileWriterMap.clear();
 	}
 
 	/**
 	 * Initialize the log manager based on the current
 	 * preference settings
 	 */
 	private void initializeFromPreferences() {
 		String fileSize = CoreBundleActivator.getScopedPreferences().getString(IPreferenceKeys.PREF_MAX_FILE_SIZE);
 		if (fileSize == null) fileSize = "5M"; //$NON-NLS-1$
 
 		try {
 			// If the last character is either K, M or G -> convert to bytes
 			char lastChar = fileSize.toUpperCase().charAt(fileSize.length() - 1);
 			if ('K' == lastChar || 'M' == lastChar || 'G' == lastChar) {
 				maxFileSize = Long.parseLong(fileSize.substring(0, fileSize.length() - 1));
 				switch (lastChar) {
 					case 'K':
 						maxFileSize = maxFileSize * 1024;
 						break;
 					case 'M':
 						maxFileSize = maxFileSize * 1024 * 1024;
 						break;
 					case 'G':
 						maxFileSize = maxFileSize * 1024 * 1024 * 1024;
 						break;
 				}
 			} else {
 				maxFileSize = Long.parseLong(fileSize);
 			}
 		} catch (NumberFormatException e) {
 			maxFileSize = 5242880L;
 		}
 
 		maxInCycle = CoreBundleActivator.getScopedPreferences().getInt(IPreferenceKeys.PREF_MAX_FILES_IN_CYCLE);
 		if (maxInCycle <= 0) maxInCycle = 5;
 	}
 
 	/**
 	 * Create, register and initialize the listeners.
 	 * <p>
 	 * <b>Note:</b> This method is supposed to be called from {@link Startup} only!
 	 */
 	/* default */ final void initListeners() {
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// If the channel state change listener instance has been created
 		// already, there is nothing left to do here
 		if (channelStateChangeListener != null) return;
 
 		// Register ourself as protocol change listener
 		Tcf.addProtocolStateChangeListener(this);
 
 		// Create and register the channel state change listener
 		channelStateChangeListener = new ChannelStateChangeListener();
 		Tcf.addChannelStateChangeListener(channelStateChangeListener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.listeners.interfaces.IProtocolStateChangeListener#stateChanged(boolean)
 	 */
 	@Override
 	public void stateChanged(boolean state) {
 		Assert.isTrue(Protocol.isDispatchThread());
 
 		// On shutdown, get the listener removed and disposed
 		if (!state) {
 			Tcf.removeChannelStateChangeListener(channelStateChangeListener);
 			channelStateChangeListener = null;
 
 			Tcf.removeProtocolStateChangeListener(this);
 		}
 	}
 
 	/**
 	 * Returns the file writer instance to use for the given channel.
 	 *
 	 * @param channel The channel. Must not be <code>null</code>.
 	 * @return The file writer instance or <code>null</code>.
 	 */
 	public FileWriter getWriter(IChannel channel) {
 		Assert.isNotNull(channel);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Before looking up the writer, check the file limits
 		checkLimits(channel);
 
 		String logName = getLogName(channel);
 		FileWriter writer = logName != null ? fileWriterMap.get(logName) : null;
 		if (writer == null && logName != null) {
 			// Create the writer
 			IPath path = getLogDir();
 			if (path != null) {
 				path = path.append(logName + ".log"); //$NON-NLS-1$
 				try {
 					writer = new FileWriter(path.toFile(), true);
 					fileWriterMap.put(logName, writer);
 				} catch (IOException e) {
 					/* ignored on purpose */
 				}
 			}
 		}
 
 		return writer;
 	}
 
 	/**
 	 * Close the writer instance used for the given channel.
 	 *
 	 * @param channel The channel. Must not be <code>null</code>.
 	 * @param message The last message to write or <code>null</code>.
 	 */
 	public void closeWriter(IChannel channel, String message) {
 		Assert.isNotNull(channel);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// Remove the writer from the map
 		String logName = getLogName(channel);
 		FileWriter writer = logName != null ? fileWriterMap.remove(logName) : null;
 		if (writer != null) {
 			try {
 				// If specified, write the last message.
 				if (message != null) {
 					writer.write(message);
 					writer.write("\n"); //$NON-NLS-1$
 				}
 			} catch (IOException e) {
 				/* ignored on purpose */
 			} finally {
 				try {
 					writer.flush();
 					writer.close();
 				} catch (IOException e) {
 					/* ignored on purpose */
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns the log file base name for the given peer id.
 	 *
 	 * @param channel The channel. Must not be <code>null</code>.
 	 * @return The log file base name.
 	 */
 	public String getLogName(IChannel channel) {
 		Assert.isNotNull(channel);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		String logName = null;
 
 		IPeer peer = channel.getRemotePeer();
 		if (peer != null) {
 			// Get the peer name
 			logName = peer.getName();
 
 			// Get the peer host IP address
 			String ip = peer.getAttributes().get(IPeer.ATTR_IP_HOST);
 			// Fallback: The peer id
 			if (ip == null || "".equals(ip.trim())) { //$NON-NLS-1$
 				ip = peer.getID();
 			}
 
 			// Append the peer host IP address
 			if (ip != null && !"".equals(ip.trim())) { //$NON-NLS-1$
 				logName += " " + ip.trim(); //$NON-NLS-1$
 			}
 
 			// Unify name and replace all undesired characters with '_'
 			logName = makeValid(logName);
 		}
 
 		return logName;
 	}
 
 	/**
 	 * Replaces a set of predefined patterns with underscore to
 	 * make a valid name.
 	 *
 	 * @param name The name. Must not be <code>null</code>.
 	 * @return The modified name.
 	 */
 	private String makeValid(String name) {
 		Assert.isNotNull(name);
 
 		String result = name.replaceAll("\\s", "_"); //$NON-NLS-1$ //$NON-NLS-2$
 		result = result.replaceAll("[:/\\;,]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		return result;
 	}
 
 	/**
 	 * Returns the log directory.
 	 *
 	 * @return The log directory.
 	 */
 	public IPath getLogDir() {
 		IPath logDir = null;
 
 		try {
 			File file = CoreBundleActivator.getDefault().getStateLocation().append(".logs").toFile(); //$NON-NLS-1$
			boolean exists = file.exists();
			if (!exists) exists = file.mkdirs();
			if (exists && file.canRead() && file.isDirectory()) {
 				logDir = new Path(file.toString());
 			}
 		} catch (IllegalStateException e) {
 			// Ignored: Workspace less environment (-data @none)
 		}
 
 		if (logDir == null) {
 			// First fallback: ${HOME}/.tcf/.logs
 			File file = new Path(System.getProperty("user.home")).append(".tcf/.logs").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
			boolean exists = file.exists();
			if (!exists) exists = file.mkdirs();
			if (exists && file.canRead() && file.isDirectory()) {
 				logDir = new Path(file.toString());
 			}
 		}
 
 		if (logDir == null) {
 			// Second fallback: ${TEMP}/.tcf/.logs
 			File file = new Path(System.getProperty("java.io.tmpdir")).append(".tcf/.logs").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
			boolean exists = file.exists();
			if (!exists) exists = file.mkdirs();
			if (exists && file.canRead() && file.isDirectory()) {
 				logDir = new Path(file.toString());
 			}
 		}
 
 		return logDir;
 	}
 
 	/**
 	 * Checks the limits set by the preferences.
 	 *
 	 * @param channel The channel. Must not be <code>null</code>.
 	 * @return The checked file writer instance.
 	 */
 	private void checkLimits(IChannel channel) {
 		Assert.isNotNull(channel);
 
 		String logName = getLogName(channel);
 		if (logName != null && !"".equals(logName.trim())) { //$NON-NLS-1$
 			IPath path = getLogDir();
 			if (path != null) {
 				IPath fullPath = path.append(logName + ".log"); //$NON-NLS-1$
 				File file = fullPath.toFile();
 				if (file.exists()) {
 					long size = file.length();
 					if (size >= maxFileSize) {
 						// Max log file size reached -> cycle files
 
 						// If there is an active writer, flush and close the writer
 						closeWriter(channel, null);
 
 						// Determine if the maximum number of files in the cycle has been reached
 						File maxFileInCycle = path.append(logName + "_" + maxInCycle + ".log").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
 						if (maxFileInCycle.exists()) {
 							// We have to rotate the full cycle, first in cycle to be removed.
 							int no = 1;
 							File fileInCycle = path.append(logName + "_" + no + ".log").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
 							fileInCycle.delete();
 
 							while (no <= maxInCycle) {
 								no++;
 								fileInCycle = path.append(logName + "_" + no + ".log").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
 								File renameTo = path.append(logName + "_" + (no - 1) + ".log").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
 								fileInCycle.renameTo(renameTo);
 							}
 
 							// Rename the log file
 							file.renameTo(maxFileInCycle);
 						} else {
 							// Not at the limit, find the next file name in the cycle
 							int no = 1;
 							File fileInCycle = path.append(logName + "_" + no + ".log").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
 							while (fileInCycle.exists()) {
 								no++;
 								fileInCycle = path.append(logName + "_" + no + ".log").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
 							}
 							Assert.isTrue(no <= maxInCycle);
 
 							// Rename the log file
 							file.renameTo(fileInCycle);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sends an event to the monitor signaling the given message and type.
 	 *
 	 * @param channel The channel. Must not be <code>null</code>.
 	 * @param type The message type. Must not be <code>null</code>.
 	 * @param message The message. Must not be <code>null</code>.
 	 */
 	public void monitor(IChannel channel, MonitorEvent.Type type, MonitorEvent.Message message) {
 		Assert.isNotNull(channel);
 		Assert.isNotNull(type);
 		Assert.isNotNull(message);
 		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 
 		// If monitoring is not enabled, return immediately
 		if (!CoreBundleActivator.getScopedPreferences().getBoolean(IPreferenceKeys.PREF_MONITOR_ENABLED)) {
 			return;
 		}
 
 		// The source of a monitor event is the peer.
 		IPeer peer = channel.getRemotePeer();
 		if (peer != null) {
 			MonitorEvent event = new MonitorEvent(peer, type, message);
 			EventManager.getInstance().fireEvent(event);
 		}
 	}
 
 }
