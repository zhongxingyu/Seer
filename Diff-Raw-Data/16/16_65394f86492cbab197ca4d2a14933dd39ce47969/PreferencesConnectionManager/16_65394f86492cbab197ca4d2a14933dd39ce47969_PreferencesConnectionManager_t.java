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
 package net.sf.rcer.conn.preferences;
 
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import net.sf.rcer.conn.Activator;
 import net.sf.rcer.conn.connections.ConnectionData;
 import net.sf.rcer.conn.connections.ConnectionNotFoundException;
 import net.sf.rcer.conn.connections.IConnectionData;
 import net.sf.rcer.conn.locales.Locale;
 import net.sf.rcer.conn.locales.LocaleNotFoundException;
 import net.sf.rcer.conn.locales.LocaleRegistry;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 
 /**
  * A singleton class that manages the connection data entries that are stored in the preferences.
  * @author vwegert
  *
  */
 public class PreferencesConnectionManager implements IPreferenceConstants {
 
 	/**
 	 * The singleton instance.
 	 */
 	private static PreferencesConnectionManager instance;
 
 	/**
 	 * Private constructor to prevent secondary instantiation.
 	 */
 	private PreferencesConnectionManager() {
 		// nothing to do
 	}
 
 	/**
 	 * @return the singleton instance
 	 */
 	public static PreferencesConnectionManager getInstance() {
 		if (instance == null) {
 			instance = new PreferencesConnectionManager();
 		}
 		return instance;
 	}
 
 	/**
 	 * @return a list of all connection IDs stored in the preferences
 	 */
 	public Set<String> getConnectionIDs() {
 		Set<String> connectionIDs = new HashSet<String>();
 		final int num = getPreferences().getInt(CONNECTION_NUMBER);
 		for (int i = 1; i <= num; i++) {
 			connectionIDs.add(Integer.toString(i));
 		}
 		return connectionIDs;
 	}
 
 	/**
 	 * @param connectionID the ID of the connection
 	 * @return the connection corresponding to the connection ID requested
 	 * @throws ConnectionNotFoundException 
 	 */
 	public ConnectionData getConnectionData(String connectionID) throws ConnectionNotFoundException {
 		return getConnectionData(getConnectionPosition(connectionID));
 	}
 
 	/**
 	 * @return the connections stored in the preferences
 	 */
 	public Collection<ConnectionData> getConnectionData() {
 		Collection<ConnectionData> connections = new Vector<ConnectionData>();
 		final int num = getPreferences().getInt(CONNECTION_NUMBER);
 		for (int i = 1; i <= num; i++) {
 			try {
 				connections.add(getConnectionData(i));
 			} catch (ConnectionNotFoundException e) {
 				logError(MessageFormat.format("Connection {0} contains errors.", i), e);
 			}
 		}
 		return connections;		
 	}
 
 	/**
 	 * @param position
 	 * @return the connection stored at the designated position
 	 * @throws ConnectionNotFoundException
 	 */
 	private ConnectionData getConnectionData(int position) throws ConnectionNotFoundException {
 		final String connectionID = Integer.toString(position);
 		ConnectionData connection;
 		if (getString(CONNECTION_TYPE, position).equals(CONNECTION_TYPE_DIRECT)) {
 			connection = new ConnectionData(connectionID,
 					getString(CONNECTION_DESCRIPTION, position),
 					getString(CONNECTION_SYSTEM_ID, position),
 					getString(CONNECTION_ROUTER, position),
 					getString(CONNECTION_APPLICATION_SERVER, position),
 					getInt(CONNECTION_SYSTEM_NUMBER, position));
 		} else if (getString(CONNECTION_TYPE, position).equals(CONNECTION_TYPE_LOAD_BALANCING)) {
 			connection = new ConnectionData(connectionID,
 					getString(CONNECTION_DESCRIPTION, position),
 					getString(CONNECTION_SYSTEM_ID, position),
 					getString(CONNECTION_ROUTER, position),
 					getString(CONNECTION_MESSAGE_SERVER, position),
 					getInt(CONNECTION_MESSAGE_SERVER_PORT, position),
 					getString(CONNECTION_LOAD_BALANCING_GROUP, position));
 		} else {
 			throw new ConnectionNotFoundException(MessageFormat.format(
 					"Connection {0} contains an invalid connection type ({1})",
 					connectionID, getString(CONNECTION_TYPE, position)));			
 		}
 		connection.setDefaultUser(getString(CONNECTION_DEFAULT_USER, position), true); 
 		connection.setDefaultClient(getString(CONNECTION_DEFAULT_CLIENT, position), true);
 		final String localeISOCode = getString(CONNECTION_DEFAULT_LOCALE, position);
 		if ((localeISOCode != null) && (!localeISOCode.equals(""))) {
 			try {
 				final Locale locale = LocaleRegistry.getInstance().getLocaleByISO(localeISOCode);
 				connection.setDefaultLocale(locale, true);
 			} catch (LocaleNotFoundException e) {
 				logError(MessageFormat.format("Connection {0} contains an invalid default locale, ignoring it.",
 						connectionID), e);
 			}
 		}
 		return connection;
 	}
 
 	/**
 	 * Saves the connections to the preferences, <b>replacing all existing connections</b>. Note that <b>the
 	 * connection IDs are not guaranteed to be stable</b> (although the registry will try to use the
 	 * connection IDs provided).
 	 * @param connections
 	 */
 	public void saveConnectionData(Collection<IConnectionData> connections) {
 
 		Map<Integer, IConnectionData> connectionMap = new TreeMap<Integer, IConnectionData>();
 		Vector<IConnectionData> remainingConnections = new Vector<IConnectionData>();
 
 		// examine the connections and try to determine the ones with a reusable connection ID
 		for (final IConnectionData connection: connections) {
 			try {
 				final Integer id = Integer.parseInt(connection.getConnectionID());
 				if (id <= 0) {
 					// we can't use values <= 0
 					remainingConnections.add(connection);
 				} else if (connectionMap.containsKey(id)) {
 					// ID is already taken - create a new one
 					remainingConnections.add(connection);
 				} else {
 					connectionMap.put(id, connection);
 				}
 			} catch (NumberFormatException e) {
 				// non-numeric ID cannot be reused
 				remainingConnections.add(connection);
 			}
 		}
 
 		// now we try to fill the "holes" in the map with the contents of remainingConnections
 		int position = 1;
 		while (!remainingConnections.isEmpty()) {
 			while (connectionMap.containsKey(position)) {
 				position++;
 			}
 			connectionMap.put(position, remainingConnections.remove(0));
 		}
 
 		// now "defragment" the map by moving entries from the rear end into the "holes"
 		for(position = 1; position <= connectionMap.size(); position++) {
 			if (!connectionMap.containsKey(position)) {
 				int oldPosition = position;
 				for (final int i: connectionMap.keySet()) {
 					if (i > oldPosition) {
 						oldPosition = i;
 					}
 				}
 				final IConnectionData entry = connectionMap.remove(oldPosition);
 				connectionMap.put(position, entry);
 			}
 		}
 
 		// now save every entry...
 		for(final Integer id: connectionMap.keySet()) {
 			final IConnectionData connection = connectionMap.get(id);
 			setValue(CONNECTION_DESCRIPTION, id, connection.getDescription());
 			setValue(CONNECTION_SYSTEM_ID, id, connection.getSystemID());
 			setValue(CONNECTION_ROUTER, id, connection.getRouter());
 			switch(connection.getConnectionType()) {
 			case DIRECT:
 				setValue(CONNECTION_TYPE, id, CONNECTION_TYPE_DIRECT);
 				setValue(CONNECTION_APPLICATION_SERVER, id, connection.getApplicationServer());
 				setValue(CONNECTION_SYSTEM_NUMBER, id, connection.getSystemNumber());
 				setToDefault(CONNECTION_MESSAGE_SERVER, id);
 				setToDefault(CONNECTION_MESSAGE_SERVER_PORT, id);
 				setToDefault(CONNECTION_LOAD_BALANCING_GROUP, id);
 				break;
 			case LOAD_BALANCED:
 				setValue(CONNECTION_TYPE, id, CONNECTION_TYPE_LOAD_BALANCING);
 				setToDefault(CONNECTION_APPLICATION_SERVER, id);
 				setToDefault(CONNECTION_SYSTEM_NUMBER, id);
 				setValue(CONNECTION_MESSAGE_SERVER, id, connection.getMessageServer());
 				setValue(CONNECTION_MESSAGE_SERVER_PORT, id, connection.getMessageServerPort());
 				setValue(CONNECTION_LOAD_BALANCING_GROUP, id, connection.getLoadBalancingGroup());	
 				break;
 			}
 			if (connection.getDefaultUser() == null) {
 				setToDefault(CONNECTION_DEFAULT_USER, id);
 			} else {
 				setValue(CONNECTION_DEFAULT_USER, id, connection.getDefaultUser());
 			}
 			if (connection.getDefaultClient() == null) {
 				setToDefault(CONNECTION_DEFAULT_CLIENT, id);
 			} else {
 				setValue(CONNECTION_DEFAULT_CLIENT, id, connection.getDefaultClient());
 			}
 			if (connection.getDefaultLocale() == null) {
 				setToDefault(CONNECTION_DEFAULT_LOCALE, id);
 			} else {
 				setValue(CONNECTION_DEFAULT_LOCALE, id, connection.getDefaultLocale().getISOCode());
 			}
 		}
 
 		// ...and update the total number
 		getPreferences().setValue(CONNECTION_NUMBER, connectionMap.size());
 		
 		Activator.getDefault().savePluginPreferences();
 
 	}
 
 	/**
 	 * Auxiliary method to parse and validate the connection ID. 
 	 * @param connectionID
 	 * @return the position designated by the connection ID
 	 * @throws ConnectionNotFoundException
 	 */
 	private int getConnectionPosition(String connectionID) throws ConnectionNotFoundException {
 		int number = getPreferences().getInt(CONNECTION_NUMBER);
 		int position = -1;
 		try {
 			position = Integer.parseInt(connectionID);
 		} catch (NumberFormatException e) {
 			throw new ConnectionNotFoundException(MessageFormat.format("The connection ID {0} is not numeric.",
 					connectionID), e);
 		}
		if ((position <= 0) || (position > number)) {
 			throw new ConnectionNotFoundException(MessageFormat.format(
 					"The connection ID {0} exceeds the maximum number of stored connections (0..{1})",
 					connectionID, number));
 		}
 		return position;
 	}
 
 	/**
 	 * Auxiliary method to log an error message
 	 * @param message the message to log
 	 * @param exception the exception that occurred
 	 */
 	private void logError(String message, Exception exception) {
 		Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, exception));
 	}
 
 	/**
 	 * Auxiliary method to get the value of a positional preference.
 	 * @param prefix
 	 * @param position
 	 * @return the string value
 	 */
 	private String getString(String prefix, int position) {
 		return getPreferences().getString(MessageFormat.format("{0}.{1}", prefix, position));
 	}
 
 	/**
 	 * Auxiliary method to get the value of a positional preference.
 	 * @param prefix
 	 * @param position
 	 * @return the integer value
 	 */
 	private int getInt(String prefix, int position) {
 		return getPreferences().getInt(MessageFormat.format("{0}.{1}", prefix, position));
 	}
 	
 	/**
 	 * Auxiliary method to reset a value to its default.
 	 * @param prefix
 	 * @param position
 	 */
 	private void setToDefault(String prefix, int position) {
 		getPreferences().setToDefault(MessageFormat.format("{0}.{1}", prefix, position));
 	}
 
 	/**
 	 * Auxiliary method to set a string value. 
 	 * @param prefix
 	 * @param position
 	 * @param value
 	 */
 	private void setValue(String prefix, int position, String value) {
 		getPreferences().setValue(MessageFormat.format("{0}.{1}", prefix, position), value);
 	}
 
 	/**
 	 * Auxliary method to set an integer value.
 	 * @param prefix
 	 * @param position
 	 * @param value
 	 */
 	private void setValue(String prefix, int position, int value) {
 		getPreferences().setValue(MessageFormat.format("{0}.{1}", prefix, position), value);
 	}
 
 	/**
 	 * @return the preferences
 	 */
 	public static Preferences getPreferences() {
 		return Activator.getDefault().getPluginPreferences();
 	}
 
 }
