 /**
  * Copyright (c) 2008 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.conn.connections;
 
 import java.security.InvalidParameterException;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import net.sf.rcer.conn.Activator;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 
 import com.sap.conn.jco.JCoDestination;
 import com.sap.conn.jco.JCoDestinationManager;
 import com.sap.conn.jco.JCoException;
 import com.sap.conn.jco.ext.DestinationDataEventListener;
 import com.sap.conn.jco.ext.DestinationDataProvider;
 import com.sap.conn.jco.ext.Environment;
 
 /**
  * The central manager for all active connections to the SAP R/3 systems. Note that at any given time,
  * zero, one or many connections may be active. The first connection that is activated becomes the so-called
  * primary connection. The primary connection can be changed using {@link ConnectionManager#s}. If the primary 
  * connection is closed, any of the secondary connections becomes the new primary connection.
  * @author vwegert
  *
  */
 public class ConnectionManager  {
 
 	// TODO add some unit tests for this class
 
 	/**
 	 * The internal implementation of the {@link DestinationDataProvider} interface.
 	 * @author vwegert
 	 *
 	 */
 	private class Provider implements DestinationDataProvider {
 
 		private ConnectionManager parent;
 		private DestinationDataEventListener listener;
 
 		/**
 		 * Default constructor.
 		 * @param connectionManager
 		 */
 		public Provider(ConnectionManager connectionManager) {
 			parent = connectionManager;
 		}
 
 		/* (non-Javadoc)
 		 * @see com.sap.conn.jco.ext.DestinationDataProvider#getDestinationProperties(java.lang.String)
 		 */
 		public Properties getDestinationProperties(String destinationId) {
 			final ICredentials credentials = parent.getCredentials(destinationId);
 			if (credentials == null) {
 				return null;
 			}
 			final IConnection connection = credentials.getConnection();
 			Properties p = new Properties();
 
 			p.setProperty(JCO_CLIENT, connection.getClient());
 			p.setProperty(JCO_USER,   connection.getUserName());
 			p.setProperty(JCO_PASSWD, credentials.getPassword());
 			if (connection.getLocale() != null) {
 				p.setProperty(JCO_LANG,   connection.getLocale().getID());
 			}
 
 			switch(connection.getConnectionType()) {
 			case DIRECT:
 				p.setProperty(JCO_ASHOST, connection.getApplicationServer());
 				p.setProperty(JCO_SYSNR,  Integer.toString(connection.getSystemNumber()));
 				break;
 			case LOAD_BALANCED:
 				p.setProperty(JCO_R3NAME, connection.getSystemID());
 				p.setProperty(JCO_MSHOST, connection.getMessageServer());
 				p.setProperty(JCO_MSSERV, Integer.toString(connection.getMessageServerPort()));
 				p.setProperty(JCO_GROUP,  connection.getLoadBalancingGroup());
 				break;
 			} 
 			p.setProperty(JCO_SAPROUTER, connection.getRouter());
 
 			// TODO Make these values configurable.
 			p.setProperty(JCO_PEAK_LIMIT, "5");    // Maximum number of active connections that can be created for a destination simultaneously
 			p.setProperty(JCO_POOL_CAPACITY, "1"); // Maximum number of idle connections kept open by the destination. A value of 0 has the effect that there is no connection pooling
 			// JCO_EXPIRATION_TIME   - Time in ms after that a free connections hold internally by the destination can be closed
 			// JCO_EXPIRATION_PERIOD - Period in ms after that the destination checks the released connections for expiration
 			// JCO_MAX_GET_TIME      - Max time in ms to wait for a connection, if the max allowed number of connections is allocated by the application
 			// JCO_CPIC_TRACE        - Enable/disable CPIC trace [0..3]
 			// JCO_TRACE             - Enable/disable RFC trace (0 or 1)
 			p.setProperty(JCO_CPIC_TRACE, "3");
 			p.setProperty(JCO_TRACE, "1");
 			return p;
 		}
 
 		/* (non-Javadoc)
 		 * @see com.sap.conn.jco.ext.DestinationDataProvider#setDestinationDataEventListener(com.sap.conn.jco.ext.DestinationDataEventListener)
 		 */
 		public void setDestinationDataEventListener(DestinationDataEventListener listener) {
 			this.listener = listener;
 		}
 
 		/* (non-Javadoc)
 		 * @see com.sap.conn.jco.ext.DestinationDataProvider#supportsEvents()
 		 */
 		public boolean supportsEvents() {
 			return true;
 		}
 
 		/**
 		 * @param destinationName
 		 * @see com.sap.conn.jco.ext.DestinationDataEventListener#deleted(java.lang.String)
 		 */
 		void fireDestinationDeleted(String destinationName) {
 			listener.deleted(destinationName);
 		}
 
 		/**
 		 * @param destinationName
 		 * @see com.sap.conn.jco.ext.DestinationDataEventListener#updated(java.lang.String)
 		 */
 		void fireDestinationUpdated(String destinationName) {
 			listener.updated(destinationName);
 		}
 	}
 
 	/**
 	 * The singleton instance.
 	 */
 	private static ConnectionManager instance;
 
 	/**
 	 * The actual {@link DestinationDataProvider} implementation.
 	 */
 	private Provider provider;
 
 	/**
 	 * The active connections (including the primary connection), ordered by the connection ID.
 	 */
 	private Map<String, ICredentials> connections = new HashMap<String, ICredentials>();
 
 	/**
 	 * A map that contains the active connections ordered by connection <b>data</b> ID. There may be
 	 * more than one active connection for a single connection data ID (think of using different users or clients)!
 	 */
 	private Map<String, Collection<ICredentials>> connectionLists = new HashMap<String, Collection<ICredentials>>();
 
 	/**
 	 * The ID of the primary connection, or <code>null</code> if none.
 	 */
 	private String primaryConnectionID;
 
 	/**
 	 * The list of {@link IConnectionStateListener} instances.
 	 */
 	private Set<IConnectionStateListener> listeners = new HashSet<IConnectionStateListener>();
 
 	static {
 		Environment.registerDestinationDataProvider(getInstance().getProvider());
 	}
 
 	/**
 	 * Private constructor to prevent secondary instantiation.
 	 */
 	private ConnectionManager() {
 		// nothing to do at the moment
 	}
 
 	/**
 	 * @return the singleton instance.
 	 */
 	public static ConnectionManager getInstance() {
 		if (instance == null) {
 			instance = new ConnectionManager();
 		}
 		return instance;
 	}
 
 	/**
 	 * Obtains the primary connection. If no connection is active, this method tries to deduce the connection to open:
 	 * If the {@link ConnectionRegistry} only contains one connection, this connection is activated using 
 	 * an {@link ICredentialsProviderWithoutSelection}. If the {@link ConnectionRegistry} contains more than one 
 	 * connection, an {@link ICredentialsProviderWithSelection} is used to determine which connection to activate.
 	 * @return the primary connection
 	 * @throws ConnectionException 
 	 * @throws JCoException 
 	 * @see #closeConnection()
 	 * @see #setPrimaryDestination(String)
 	 */
 	public JCoDestination getDestination() throws ConnectionException, JCoException {
 		if (primaryConnectionID != null) {
 			getCheckedDestination(primaryConnectionID);
 		}
 		final Set<IConnectionData> registeredConnectionData = ConnectionRegistry.getInstance().getConnectionData();
 		switch(registeredConnectionData.size()) {
 		case 0:
 			throw new ConnectionException("Unable to find connection data.");
 		case 1:
 			return getDestination(registeredConnectionData.iterator().next());
 		default: {
 			// query the credentials providers in order of priority
 			ICredentials credentials = null;
 			Iterator<ICredentialsProviderWithSelection> it = getCredentialsProvidersWithSelection().iterator();
 			while (it.hasNext() && credentials == null) {
 				try {
 					credentials = it.next().getCredentials();
 				} catch (OperationCanceledException e) {
 					throw new ConnectionException("Logon cancelled by the user.", e);
 				} catch (Exception e) {
 					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
 							"Exception occurred trying to select a connection and determine the credentials.", e));
 				}
 			}
 			if (credentials == null) {
 				throw new ConnectionException("Unable to select a connection and determine the credentials.");
 			}
 			addConnectionInternal(credentials);
 			try {
 				return getCheckedDestination(credentials.getConnectionID());
 			} catch (JCoException e) {
 				removeConnectionInternal(credentials);
 				throw e;
 			}
 		}
 		}
 	}
 
 	/**
 	 * Obtains the connection designated by the connection data. Reuses an existing connection if 
 	 * <code>reuseConnection</code> is set, forces the creation of a new connection otherwise.
 	 * If no connection for the connection data ID is active, 
 	 * a new connection is activated. If it is the first connection that is activated, it automatically becomes the 
 	 * primary connection. Note that more than one connection may be active for a set of connection data - if this is 
 	 * the case, any of the connections is chosen arbitrarily.
 	 * @param connectionData
 	 * @param reuseConnection if <code>true</code>, an existing connection is reused 
 	 * @return the connection
 	 * @throws JCoException 
 	 * @throws ConnectionException 
 	 */
 	public JCoDestination getDestination(IConnectionData connectionData, boolean reuseConnection) throws JCoException, ConnectionException {
 		if (reuseConnection) {
 			// see whether a connection for the connection data ID already exists
 			if (connectionLists.containsKey(connectionData.getConnectionDataID())) {
 				final Collection<ICredentials> coll = connectionLists.get(connectionData.getConnectionDataID());
 				if (!coll.isEmpty()) {
 					return getCheckedDestination(coll.iterator().next().getConnectionID());
 				}
 			}
 		}
 
 		// query the credentials providers in order of priority
 		ICredentials credentials = null;
 		Iterator<ICredentialsProviderWithoutSelection> it = getCredentialsProvidersWithoutSelection().iterator();
 		while (it.hasNext() && credentials == null) {
 			try {
 				credentials = it.next().getCredentials(connectionData);
 			} catch (OperationCanceledException e) {
 				throw new ConnectionException("Logon cancelled by the user.", e);
 			} catch (Exception e) {
 				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
 						MessageFormat.format(
 								"Exception occurred trying to determine the credentials for connection {0}.",
 								connectionData.getConnectionDataID()), e));
 			}
 		}
 		if (credentials == null) {
 			throw new ConnectionException(MessageFormat.format(
 					"Unable to determine the credentials for connection {0}.",
 					connectionData.getConnectionDataID()));
 		}
 		addConnectionInternal(credentials);
 		try {
 			return getCheckedDestination(credentials.getConnectionID());
 		} catch (JCoException e) {
 			removeConnectionInternal(credentials);
 			throw e;			
 		}
 	}
 
 	/**
 	 * Obtains the connection designated by the connection data. If no connection for the connection data ID is active, 
 	 * a new connection is activated. If it is the first connection that is activated, it automatically becomes the 
 	 * primary connection. Note that more than one connection may be active for a set of connection data - if this is 
 	 * the case, any of the connections is chosen arbitrarily.
 	 * @param connectionData
 	 * @return the connection
 	 * @throws JCoException 
 	 * @throws ConnectionException 
 	 */
 	public JCoDestination getDestination(IConnectionData connectionData) throws JCoException, ConnectionException {
 		return getDestination(connectionData, true);
 	}
 
 	/**
 	 * Obtains the connection designated. If the connection is not active, it is activated. If the connection is active
 	 * and <code>reuseConnection</code> is <code>true</code>, the active conneciton is returned. Otherwise a new 
 	 * connection is opened 
 	 * @param connectionID
 	 * @param reuseConnection if <code>true</code>, an existing connection is reused 
 	 * @return the connection
 	 * @throws ConnectionException 
 	 * @throws JCoException 
 	 * @throws ProviderNotFoundException 
 	 * @throws ConnectionNotFoundException 
 	 */
 	public JCoDestination getDestination(String connectionID, boolean reuseConnection) 
 	  throws ConnectionException, JCoException, ConnectionNotFoundException, ProviderNotFoundException {
 		return getDestination(ConnectionRegistry.getInstance().getConnectionData(connectionID), reuseConnection);
 	}
 
 	/**
 	 * Obtains the connection designated. If the connection is not active, it is activated. If the connection is active,
 	 * the current connection is returned. 
 	 * @param connectionID
 	 * @return the connection
 	 * @throws ConnectionException 
 	 * @throws JCoException 
 	 * @throws ProviderNotFoundException 
 	 * @throws ConnectionNotFoundException 
 	 */
 	public JCoDestination getDestination(String connectionID) 
 	  throws ConnectionException, JCoException, ConnectionNotFoundException, ProviderNotFoundException {
 		return getDestination(connectionID, true);
 	}
 
 	/**
 	 * @return the connection ID (destination name) of the primary connection, or <code>null</code> 
 	 * if no connection is active
 	 */
 	public String getPrimaryConnectionID() {
 		return primaryConnectionID;
 	}
 
 	/**
 	 * @return a list of all secondary connections ({@link #getActiveConnectionIDs()} 
 	 * without {@link #getPrimaryConnectionID()})
 	 */
 	public Set<String> getSecondaryConnectionIDs() {
 		Set<String> connIDs = getActiveConnectionIDs();
 		connIDs.remove(getPrimaryConnectionID());
 		return connIDs;
 	}
 
 	/**
 	 * @return a list of all active connection IDs 
 	 */
 	public Set<String> getActiveConnectionIDs() {
 		return new HashSet<String>(connections.keySet());
 	}
 
 	/**
 	 * @return a list of all active connections
 	 */
 	public Collection<IConnection> getActiveConnections() {
 		Vector<IConnection> result = new Vector<IConnection>();
 		for(ICredentials credentials: connections.values()) {
 			result.add(credentials.getConnection());
 		}
 		return result;
 	}
 
 	/**
 	 * @param connectionData
 	 * @return <code>true</code> if the connection is active (that is, if one or more connections for the connection 
 	 * data are active)
 	 */
 	public boolean isActive(IConnectionData connectionData) {
 		return connectionLists.containsKey(connectionData.getConnectionDataID());
 	}
 
 	/**
 	 * @param connectionID
 	 * @return <code>true</code> if the connection with this connection ID is active
 	 */
 	public boolean isActive(String connectionID) {
 		return connections.containsKey(connectionID);
 	}
 
 	/**
 	 * @param connectionID
 	 * @return <code>true</code> if the connection is the primary connection
 	 */
 	public boolean isPrimaryConnection(String connectionID) {
 		return (primaryConnectionID != null) && primaryConnectionID.equals(connectionID);
 	}
 
 	/**
 	 * @param connection
 	 * @return <code>true</code> if the connection is the primary connection
 	 */
 	public boolean isPrimaryConnection(IConnection connection) {
 		return isPrimaryConnection(connection.getConnectionID());
 	}
 
 	/**
 	 * @return <code>true</code> if at least one connection is active
 	 */
 	public boolean isConnected() {
 		return primaryConnectionID != null;
 	}
 
 
 	/**
 	 * Makes the connection the primary connection.
 	 * @param destination
 	 * @throws JCoException 
 	 * @throws ConnectionException 
 	 */
 	public void setPrimaryConnection(JCoDestination destination) throws ConnectionException, JCoException {
 		setPrimaryDestination(destination.getDestinationName());
 	}
 
 	/**
 	 * Makes the connection the primary connection.
 	 * @param connection
 	 * @throws JCoException 
 	 * @throws ConnectionException 
 	 */
 	public void setPrimaryConnection(IConnection connection) throws ConnectionException, JCoException {
 		setPrimaryDestination(connection.getConnectionID());
 	}
 
 	/**
 	 * Makes the connection the primary connection. 
 	 * @param connectionID
 	 * @throws JCoException 
 	 * @throws ConnectionException 
 	 */
 	public void setPrimaryDestination(String connectionID) throws ConnectionException, JCoException {
 		if (connectionID == null) {
 			throw new InvalidParameterException("The connection ID may not be null."); 
 		}
 		if (!connections.containsKey(connectionID)) {
 			throw new ConnectionException(MessageFormat.format("No connection with ID {0} is active.", connectionID)); 
 		}
 		setPrimaryConnectionInternal(connections.get(connectionID));
 	}
 
 	/**
 	 * Makes the connection the primary connection. 
 	 * @param connectionID
 	 */
 	private void setPrimaryConnectionInternal(ICredentials credentials) {
 		primaryConnectionID = credentials == null ? null : credentials.getConnectionID();
 		// notify listeners that the primary connection has changed
 		for (final IConnectionStateListener listener: listeners) {
 			listener.primaryConnectionChanged(credentials == null ? null : credentials.getConnection());
 		}
 	}
 
 	/**
 	 * Closes the primary connection. If no connection is active, nothing happens. If more than one connection
 	 * is active, the primary connection is closed and any of the secondary connections becomes the new primary 
 	 * connection. 
 	 */
 	public void closeConnection() {
 		if (primaryConnectionID != null) {
 			closeConnection(primaryConnectionID);
 		}
 	}
 
 	/**
 	 * Closes the designated connection. If the connection is not active, nothing happens. If more than one connection
 	 * is active and the designated connection is the primary connection, it is closed and any of the secondary 
 	 * connections becomes the new primary connection. 
 	 * @param destination
 	 */
 	public void closeConnection(JCoDestination destination) {
 		closeConnection(destination.getDestinationName());
 	}
 
 	/**
 	 * Closes the designated connection. If the connection is not active, nothing happens. If more than one connection
 	 * is active and the designated connection is the primary connection, it is closed and any of the secondary 
 	 * connections becomes the new primary connection. 
 	 * @param connection
 	 */
 	public void closeConnection(IConnection connection) {
 		closeConnection(connection.getConnectionID());
 	}
 
 	/**
 	 * Closes the designated connection. If the connection is not active, nothing happens. If more than one connection
 	 * is active and the designated connection is the primary connection, it is closed and any of the secondary 
 	 * connections becomes the new primary connection. 
 	 * @param connectionID
 	 */
 	public void closeConnection(String connectionID) {
 		if (connections.containsKey(connectionID)) {
 			final ICredentials credentials = connections.get(connectionID);
 			removeConnectionInternal(credentials);
 		}
 	}
 
 	/**
 	 * Closes all active connections. 
 	 */
 	public void closeConnections() {
 		while (isConnected()) {
 			closeConnection();
 		}
 	}
 	
 	/**
 	 * Adds an object to the list of listeners to be notified when the state of a connection changes.
 	 * @see {@link IConnectionStateListener} 
 	 * @param listener
 	 */
 	public void addConnectionStateListener(IConnectionStateListener listener) {
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	}
 
 	/**
 	 * Removes an object from the list of listeners to be notified when the state of a connection changes.
 	 * @param listener
 	 */
 	public void removeConnectionStateListener(IConnectionStateListener listener) {
 		if (listeners.contains(listener)) {
 			listeners.remove(listener);
 		}
 	}
 
 	/**
 	 * Adds the destination to the list of active destinations. Also sets the primary connection if not set.
 	 * @param credentials
 	 */
 	private void addConnectionInternal(ICredentials credentials) {
 		// store the connection in the internal maps
 		connections.put(credentials.getConnectionID(), credentials);
 		if (!connectionLists.containsKey(credentials.getConnection().getConnectionDataID())) {
 			connectionLists.put(credentials.getConnection().getConnectionDataID(), new HashSet<ICredentials>());
 		}
 		connectionLists.get(credentials.getConnection().getConnectionDataID()).add(credentials);
 		// notify the JCo that a new connection is available
 		provider.fireDestinationUpdated(credentials.getConnectionID());
 		// notify listeners that a new connection was opened
 		for (final IConnectionStateListener listener: listeners) {
 			listener.connectionActivated(credentials.getConnection());
 		}
 		// set the primary connection if it is not set
 		if (primaryConnectionID == null) {
 			setPrimaryConnectionInternal(credentials);
 		}
 	}
 
 	/**
 	 * Removes a connection from the list of active connections. Also chooses a new primary connection if required.
 	 * @param credentials
 	 */
 	private void removeConnectionInternal(ICredentials credentials) {
 		// notify the JCo that the connection will be removed
 		provider.fireDestinationDeleted(credentials.getConnectionID());
 		// remove the connection from the internal lists
 		connections.remove(credentials.getConnectionID());
 		connectionLists.get(credentials.getConnection().getConnectionDataID()).remove(credentials);
 		// notify listeners that the connection has been closed
 		for (final IConnectionStateListener listener: listeners) {
			listener.connectionDeactivated(credentials.getConnection());
 		}
 		// was this the primary connection?
 		if (primaryConnectionID.equals(credentials.getConnectionID())) {
 			// yes - choose another, if any are left
 			setPrimaryConnectionInternal(connections.isEmpty() ? null : connections.values().iterator().next());
 		}
 	}
 
 	/**
 	 * @return actual {@link DestinationDataProvider} implementation
 	 */
 	private DestinationDataProvider getProvider() {
 		if (provider == null) {
 			provider = new Provider(this);
 		}
 		return provider;
 	}
 
 	/**
 	 * @param connectionID
 	 * @return the credentials for the connection, or <code>null</code> if the connection is not active
 	 */
 	private ICredentials getCredentials(String connectionID) {
 		if (!connections.containsKey(connectionID)) {
 			return null;
 		}
 		return connections.get(connectionID);
 	}
 
 	/**
 	 * @return a list of all reachable {@link ICredentialsProviderWithoutSelection}, sorted by priority
 	 */
 	private List<ICredentialsProviderWithoutSelection> getCredentialsProvidersWithoutSelection() {
 		Map<String, ICredentialsProviderWithoutSelection> providers = 
 			new TreeMap<String, ICredentialsProviderWithoutSelection>();
 		for (final IConfigurationElement element: 
 			Platform.getExtensionRegistry().getConfigurationElementsFor(Activator.PLUGIN_ID, "credentials")) {
 			if (element.getName().equals("credentialsProviderWithoutSelection")) {
 				final String id = element.getAttribute("id");
 				try {
 					final String prio = element.getAttribute("priority");
 					final ICredentialsProviderWithoutSelection prov = 
 						(ICredentialsProviderWithoutSelection) element.createExecutableExtension("class");
 					providers.put(prio, prov);
 				} catch (CoreException e) {
 					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
 							MessageFormat.format(
 									"Exception occurred trying to instantiate connection provider {0}.", id), e));
 				}
 
 			}
 		}
 		return new Vector<ICredentialsProviderWithoutSelection>(providers.values());
 	}
 
 	/**
 	 * @return a list of all reachable {@link ICredentialsProviderWithSelection}, sorted by priority
 	 */
 	private List<ICredentialsProviderWithSelection> getCredentialsProvidersWithSelection() {
 		Map<String, ICredentialsProviderWithSelection> providers = 
 			new TreeMap<String, ICredentialsProviderWithSelection>();
 		for (final IConfigurationElement element: 
 			Platform.getExtensionRegistry().getConfigurationElementsFor(Activator.PLUGIN_ID, "credentials")) {
 			if (element.getName().equals("credentialsProviderWithSelection")) {
 				final String id = element.getAttribute("id");
 				try {
 					final String prio = element.getAttribute("priority");
 					final ICredentialsProviderWithSelection prov = 
 						(ICredentialsProviderWithSelection) element.createExecutableExtension("class");
 					providers.put(prio, prov);
 				} catch (CoreException e) {
 					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
 							MessageFormat.format(
 									"Exception occurred trying to instantiate connection provider {0}.", id), e));
 				}
 
 			}
 		}
 		return new Vector<ICredentialsProviderWithSelection>(providers.values());
 	}
 
 	/**
 	 * Requests a destination object from the JCo and executes a RFC_PING it to see if it is valid. 
 	 * @param connectionID
 	 * @return
 	 * @throws JCoException
 	 */
 	private JCoDestination getCheckedDestination(String connectionID) throws JCoException {
 		final JCoDestination dest = JCoDestinationManager.getDestination(connectionID);
 		dest.ping();
 		return dest;
 	}
 
 }
