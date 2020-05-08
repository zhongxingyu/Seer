 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.internet.monitor.core.internal;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.List;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.wst.internet.monitor.core.internal.provisional.*;
 /**
  * 
  */
 public class Monitor implements IMonitor {
 	private static final String MEMENTO_ID = "id";
 	private static final String MEMENTO_LOCAL_PORT = "local-port";
 	private static final String MEMENTO_REMOTE_HOST = "remote-host";
 	private static final String MEMENTO_REMOTE_PORT = "remote-port";
 	private static final String MEMENTO_TYPE_ID = "type-id";
 	private static final String MEMENTO_TIMEOUT = "timeout";
 
 	private static final int ADD = 0;
 	private static final int CHANGE = 1;
 
 	protected String id;
 	protected String remoteHost;
 	protected int remotePort = 80;
 	protected int localPort = 80;
 	protected String protocolId;
 	protected int timeout;
 
 	protected List requestListeners = new ArrayList(2);
 
 	/**
 	 * Create a new monitor.
 	 */
 	public Monitor() {
 		protocolId = MonitorPlugin.getInstance().getDefaultType();
 	}
 	
 	/** (non-Javadoc)
 	 * @see IMonitor#getId()
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/** (non-Javadoc)
 	 * @see IMonitor#getRemoteHost()
 	 */
 	public String getRemoteHost() {
 		return remoteHost;
 	}
 
 	/** (non-Javadoc)
 	 * @see IMonitor#getRemotePort()
 	 */
 	public int getRemotePort() {
 		return remotePort;
 	}
 
 	/** (non-Javadoc)
 	 * @see IMonitor#getLocalPort()
 	 */
 	public int getLocalPort() {
 		return localPort;
 	}
 
 	/** (non-Javadoc)
 	 * @see IMonitor#getProtocol()
 	 */
 	public String getProtocol() {
 		return protocolId;
 	}
 
 	/** (non-Javadoc)
 	 * @see IMonitor#getTimeout()
 	 */
 	public int getTimeout() {
 		return timeout;
 	}
 
 	/** (non-Javadoc)
 	 * @see IMonitor#isRunning()
 	 */
 	public boolean isRunning() {
 		if (isWorkingCopy())
 			return false;
 		return MonitorManager.getInstance().isRunning(this);
 	}
 	
 	/**
 	 * @see IMonitor#delete()
 	 */
 	public void delete() {
 		if (isWorkingCopy())
 			return;
 		MonitorManager.getInstance().removeMonitor(this);
 	}
 
 	/**
 	 * @see IMonitor#isWorkingCopy()
 	 */
 	public boolean isWorkingCopy() {
 		return false;
 	}
 	
 	/**
 	 * @see IMonitor#createWorkingCopy()
 	 */
 	public IMonitorWorkingCopy createWorkingCopy() {
 		return new MonitorWorkingCopy(this);
 	}
 
 	protected void setInternal(IMonitor monitor) {
 		id = monitor.getId();
 		remoteHost = monitor.getRemoteHost();
 		remotePort = monitor.getRemotePort();
 		localPort = monitor.getLocalPort();
 		protocolId = monitor.getProtocol();
 		timeout = monitor.getTimeout();
 	}
 
 	protected void save(IMemento memento) {
 		memento.putString(MEMENTO_ID, id);
 		memento.putString(MEMENTO_TYPE_ID, protocolId);
 		memento.putInteger(MEMENTO_LOCAL_PORT, localPort);
 		memento.putString(MEMENTO_REMOTE_HOST, remoteHost);
 		memento.putInteger(MEMENTO_REMOTE_PORT, remotePort);
 		memento.putInteger(MEMENTO_TIMEOUT, timeout);
 	}
 
 	protected void load(IMemento memento) {
 		id = memento.getString(MEMENTO_ID);
 		protocolId = memento.getString(MEMENTO_TYPE_ID);
 		Integer temp = memento.getInteger(MEMENTO_LOCAL_PORT);
 		if (temp != null)
 			localPort = temp.intValue();
 		remoteHost = memento.getString(MEMENTO_REMOTE_HOST);
 		temp = memento.getInteger(MEMENTO_REMOTE_PORT);
 		if (temp != null)
 			remotePort = temp.intValue();
 		temp = memento.getInteger(MEMENTO_TIMEOUT);
 		if (temp != null)
 			timeout = temp.intValue();
 	}
 	
 	/**
 	 * @see IMonitor#start()
 	 */
 	public synchronized void start() throws CoreException {
 		if (isRunning())
 			return;
 		if (isWorkingCopy() || !MonitorManager.getInstance().exists(this))
 			throw new IllegalArgumentException();
 		
 		IStatus status = validate();
 		if (!status.isOK())
 			throw new CoreException(status);
 		
 		MonitorManager.getInstance().startMonitor(this);
 	}
 	
 	/**
 	 * @see IMonitor#start()
 	 */
 	public synchronized void stop() {
 		if (isWorkingCopy() || !MonitorManager.getInstance().exists(this))
 			throw new IllegalArgumentException();
 		if (!isRunning())
 			return;
 		MonitorManager.getInstance().stopMonitor(this);
 	}
 	
 	/**
 	 * @see IMonitor#addRequestListener(IRequestListener)
 	 */
 	public synchronized void addRequestListener(IRequestListener listener) {
 		if (listener == null)
 			throw new IllegalArgumentException();
 		if (!requestListeners.contains(listener))
 			requestListeners.add(listener);
 	}
 	
 	/**
 	 * @see IMonitor#removeRequestListener(IRequestListener)
 	 */
 	public synchronized void removeRequestListener(IRequestListener listener) {
 		if (listener == null)
 			throw new IllegalArgumentException();
 		requestListeners.remove(listener);
 	}
 	
 	/**
 	 * Fire a request event.
 	 * @param rr
 	 * @param type
 	 */
 	protected void fireRequestEvent(Request rr, int type) {
 		int size = requestListeners.size();
 		IRequestListener[] rl = new IRequestListener[size];
 		requestListeners.toArray(rl);
 
 		for (int i = 0; i < size; i++) {
 			IRequestListener listener = rl[i];
 			if (type == ADD)
 				listener.requestAdded(this, rr);
 			else if (type == CHANGE)
 				listener.requestChanged(this, rr);
 		}
 	}
 	
 	/**
 	 * Add a new request response pair.
 	 * 
 	 * @param request a request
 	 */
 	public void addRequest(Request request) {
 		fireRequestEvent(request, ADD);
 	}
 
 	/**
 	 * A request response pair has been changed.
 	 * 
 	 * @param request a request
 	 */
 	public void requestChanged(Request request) {
 		fireRequestEvent(request, CHANGE);
 	}
 	
 	/**
 	 * @see IMonitor#validate()
 	 */
 	public IStatus validate() {
 		if (localPort < 0)
 			return new Status(IStatus.ERROR, MonitorPlugin.PLUGIN_ID, 0, Messages.errorInvalidLocalPort, null);
 		
 		if (remotePort < 0)
 			return new Status(IStatus.ERROR, MonitorPlugin.PLUGIN_ID, 0, Messages.errorInvalidRemotePort, null);
 		
 		if (remoteHost == null || remoteHost.length() == 0 || !isValidHostname(remoteHost))
 			return new Status(IStatus.ERROR, MonitorPlugin.PLUGIN_ID, 0, Messages.errorInvalidRemoteHost, null);
 		
 		if (isLocalhost(remoteHost) && localPort == remotePort)
 			return new Status(IStatus.ERROR, MonitorPlugin.PLUGIN_ID, 0, Messages.errorInvalidLocalPort, null);
 	
 		return Status.OK_STATUS;
 	}
 
 	protected static boolean isValidHostname(String host) {
 		if (host == null || host.trim().length() < 1)
 			return false;
 		
 		int length = host.length();
 		for (int i = 0; i < length; i++) {
 			char c = host.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != ':' && c != '.')
 				return false;
 		}
 		if (host.endsWith(":"))
 			return false;
 		return true;
 	}
 
 	protected static boolean isLocalhost(String host) {
 		if (host == null)
 			return false;
 		try {
 			if ("localhost".equals(host) || "127.0.0.1".equals(host))
 				return true;
 			InetAddress localHostaddr = InetAddress.getLocalHost();
 			if (localHostaddr.getHostName().equals(host))
 				return true;
 		} catch (Exception e) {
 			Trace.trace(Trace.WARNING, "Error checking for localhost", e);
 		}
 		return false;
 	}
 
 	/**
 	 * @see Object#toString()
 	 */
 	public String toString() {
 		return "Monitor [" + getId() + ", " + getProtocol() + ", " + getLocalPort() + ", "
 			+ getRemoteHost() + ", " + getRemotePort() + "]";
 	}
 	
 	/**
 	 * @see Object#equals(Object)
 	 */
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Monitor))
 			return false;
 		
 		IMonitor m = (IMonitor) obj;
 		if (m.isWorkingCopy()) {
 			m = ((IMonitorWorkingCopy) m).getOriginal();
 			if (m == null)
 				return false;
 		}
 		if (id == null && m.getId() != null)
 			return false;
 		if (id != null && !id.equals(m.getId()))
 			return false;
 		
 		if (localPort != m.getLocalPort())
 			return false;
 		if (remotePort != m.getRemotePort())
 			return false;
 		
 		if (remoteHost == null && m.getRemoteHost() != null)
 			return false;
 		if (remoteHost != null && !remoteHost.equals(m.getRemoteHost()))
 			return false;
 		
 		if (protocolId == null && m.getProtocol() != null)
 			return false;
 		if (protocolId != null && !protocolId.equals(m.getProtocol()))
 			return false;
 		
 		return true;
 	}
 }
