 /*
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  *
  * All other source code is copyright Tresys Technology and licensed as below.
  *
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  *
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.tresys.jalop.utils.jnltest.Config;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import com.tresys.jalop.jnl.RecordType;
 import com.tresys.jalop.jnl.Role;
 
 /**
  * The {@link Config} class is used to to parse a configuration file, and
  * configure the JNLTest program.
  */
 public class Config {
 	private static final String ADDRESS = "address";
 	private static final String AUDIT = "audit";
 	private static final String BEEP_ACTION = "beepAction";
 	private static final String CONNECT = "connect";
 	private static final String DATA_CLASS = "dataClass";
 	private static final String HOSTS = "hosts";
 	private static final String INPUT = "input";
 	private static final String JOURNAL = "journal";
 	private static final String LISTENER = "listener";
 	private static final String LOG = "log";
 	private static final String OUTPUT = "output";
 	private static final String PEERS = "peers";
 	private static final String PENDING_DGST_MAX = "pendingDigestMax";
 	private static final String PENDING_DGST_TIMEOUT = "pendingDigestTimeout";
 	private static final String PORT = "port";
 	private static final String PUBLISH_ALLOW = "publishAllow";
 	private static final String PUBLISHER = "publisher";
 	private static final String SESSION_TIMEOUT = "sessionTimeout";
 	private static final String SUBSCRIBE_ALLOW = "subscribeAllow";
 	private static final String SUBSCRIBER = "subscriber";
 
 	/**
 	 * Method to create a {@link Config} from a {@link JSONObject}
 	 *
 	 * @param cfgFile
 	 *            An identifier for the configuration file (i.e. the path).
 	 * @param parsedConfig
 	 *            The root level {@link JSONObject} for the configuration file.
 	 * @return A {@link Config} object.
 	 * @throws ConfigurationException
 	 */
 	public static Config createFromJson(final String cfgFile,
 			final JSONObject parsedConfig) throws ConfigurationException {
 		final Config config = new Config(cfgFile);
 		config.handleCommon(parsedConfig);
 
 		Object subscriber = parsedConfig.get(SUBSCRIBER);
 		Object publisher = parsedConfig.get(PUBLISHER);
 	    Object listener = parsedConfig.get(LISTENER);
 	    String exceptionMsg = new StringBuilder().append("Must specify one of '")
 	        .append(SUBSCRIBER).append("', '")
 	        .append(PUBLISHER).append("', ")
 	        .append(LISTENER).append('\'').toString();
 	    if (subscriber != null) {
 	        if (publisher != null || listener != null) {
 	            throw new ConfigurationException(cfgFile, exceptionMsg);
 	        }
 	        config.handleSubscriber(asJsonObject(cfgFile, SUBSCRIBER, subscriber));
 	    } else if (publisher != null) {
 	        if (listener != null) {
                 throw new ConfigurationException(cfgFile, exceptionMsg);
 	        }
             config.handlePublisher(asJsonObject(cfgFile, PUBLISHER, publisher));
 	    } else if (listener != null) {
             config.handleListener(asJsonObject(cfgFile, LISTENER, listener));
 	    } else {
             throw new ConfigurationException(cfgFile, exceptionMsg);
 	    }
 
 		return config;
 	}
 
 	/**
 	 * Parses a configuration file for use by the JNLTest program.
 	 * @param path
 	 *            The path to a file to use as the configuration.
 	 * @return The {@link Config}
 	 * @throws ParseException
 	 *             If there is a problem parsing the config file.
 	 * @throws IOException
 	 *             If there is a problem reading the config file.
 	 * @throws ConfigurationException
 	 */
 	public static Config parse(final String path) throws IOException,
 			ParseException, ConfigurationException {
 		final FileReader fr = new FileReader(new File(path));
 		final JSONParser jsonParser = new JSONParser();
 		final Object o = jsonParser.parse(fr);
 		JSONObject parsedConfig;
 		parsedConfig = asJsonObject(path, null, o);
 		return createFromJson(path, parsedConfig);
 	}
 
 	private InetAddress address;
 	private boolean listener;
 	private File inputPath;
 	private File outputPath;
 	private Map<InetAddress, PeerConfig> peerConfigs;
 	private short pendingDigestMax;
	private long pendingDigestTimeout;
 	private int port;
 	private Set<RecordType> recordTypes;
 	private Role role;
 	private Date sessionTimeout;
 	private final String source;
 
 	/**
 	 * Create a new {@link Config} object.
 	 *
 	 * @param source
 	 *            This string will be used in generated errors to indicate what
 	 *            resources caused a problem.
 	 */
 	Config(final String source) {
 		this.source = source;
 		this.recordTypes = new HashSet<RecordType>();
 		this.peerConfigs = new HashMap<InetAddress, PeerConfig>();
 		this.pendingDigestMax = -1;
 		this.pendingDigestTimeout = -1;
 		this.port = -1;
 	}
 
 	/**
 	 * Get the IP address.
 	 *
 	 * @return The {@link InetAddress}. Currently on IPv4 addresses are
 	 *         supported.
 	 */
 	public InetAddress getAddress() {
 		return this.address;
 	}
 
 	/**
 	 * Retrieve the directory path to use when acting as publisher.
 	 *
 	 * @return The directory specified for where to obtain records from.
 	 */
 	public File getInputPath() {
 		return this.inputPath;
 	}
 
 	/**
 	 * Retrieve the directory path to use when acting as a subscriber.
 	 *
 	 * @return The directory specified for store records into.
 	 */
 	public File getOutputPath() {
 		return this.outputPath;
 	}
 
 	/**
 	 * Obtains the {@link PeerConfig} map for this configuration.
 	 *
 	 * @return The a {@link Map} of {@link PeerConfig} objects.
 	 */
 	public Map<InetAddress, PeerConfig> getPeerConfigs() {
 		return this.peerConfigs;
 	}
 
 	/**
 	 * Obtain the indicated maximum number of digests to calculate before
 	 * sending a "digest" message. This maximum is a per session maximum.
 	 *
 	 * @return The maximum number of digests to calculate before sending a
 	 *         "digest" message.
 	 */
 	public short getPendingDigestMax() {
 		return this.pendingDigestMax;
 	}
 
 	/**
 	 * Obtain the indicated number of seconds to wait before sending a "digest"
 	 * message.
 	 *
 	 * @return The maximum number of seconds to wait before sending a digest
 	 *         message.
 	 */
	public long getPendingDigestTimeout() {
 		return this.pendingDigestTimeout;
 	}
 
 	/**
 	 * Get the port to listen on (for listeners) or connect to (for connector).
 	 *
 	 * @return The port
 	 * @see Config#isListener()
 	 */
 	public int getPort() {
 		return this.port;
 	}
 
 	/**
 	 * Obtain the set of record types to subscribe/publish. This is not
 	 * applicable for a listener.
 	 *
 	 * @return The JALoP records that should be transfered using this
 	 *         connection.
 	 * @see Config#isListener()
 	 */
 	public Set<RecordType> getRecordTypes() {
 		return this.recordTypes;
 	}
 
 	/**
 	 * Obtain the indicated role, {@link Role#Publisher} or
 	 * {@link Role#Subscriber}. This is not applicable for listeners.
 	 *
 	 * @see Config#isListener()
 	 * @return The designated role.
 	 */
 	public Role getRole() {
 		return this.role;
 	}
 
 	/**
 	 * Obtain the session timeout. The session timeout indicates how long an
 	 * connector should wait (once the connection is established) before
 	 * disconnecting from a remote.
 	 *
 	 * @return A Date that represents the amount of time to wait. This is the
 	 *         amount of time to wait, not a future date.
 	 */
 	
 	public Date getSessionTimeout() {
 		return this.sessionTimeout;
 	}
 
 	/**
 	 * Obtain the identifier (i.e. path) of the configuration used to create
 	 * this {@link Config} object.
 	 *
 	 * @see Config#Config(String)
 	 * @return The source identifier.
 	 */
 	public String getSource() {
 		return this.source;
 	}
 
 	/**
 	 * Get IP address from the {@link JSONObject}. This expects there to be a
 	 * key with the name "address" in the {@link JSONObject} obj.
 	 *
 	 * @param obj
 	 *            The context to look in.
 	 * @throws ConfigurationException
 	 *             If 'address' is not found.
 	 */
 	void handleAddress(final JSONObject obj) throws ConfigurationException {
 		final String addrString = itemAsString(ADDRESS, obj);
 		this.address = stringToAddress(addrString);
 	}
 
 	/**
 	 * Helper utility to handle the common configuration keys.
 	 *
 	 * @param obj
 	 *            The context to lookup keys in.
 	 * @throws ConfigurationException
 	 *             If an error is detected in the configuration.
 	 */
 	void handleCommon(final JSONObject obj) throws ConfigurationException {
 		handleAddress(obj);
 		setPort(itemAsNumber(PORT, obj).shortValue());
 	}
 
 	/**
 	 * Handle common configuration keys for connectors.
 	 *
 	 * @param obj
 	 *            The context to look up keys in.
 	 * @throws ConfigurationException
 	 *             If an error is detected in the configuration.
 	 */
 	void handleSessionTimeout(final JSONObject obj)
 			throws ConfigurationException {
 		final String sessionTimeout = itemAsString(SESSION_TIMEOUT, obj);
 		try {
 			setSessionTimeout(new SimpleDateFormat("hh:mm:ss")
 					.parse(sessionTimeout));
 		} catch (final java.text.ParseException e) {
 			throw new ConfigurationException(this.source, "Bad format for '"
 					+ SESSION_TIMEOUT + "' (" + sessionTimeout + ") "
 					+ e.toString());
 		}
 	}
 
 	/**
 	 * Handle parsing the dataClass field for a publisher or listener.
 	 * @param obj
 	 *            The context to look up keys in.
 	 * @throws ConfigurationException
      *            If an error is detected in the configuration.
 	 */
 	void handleDataClass(final JSONObject obj) throws ConfigurationException {
 	    final JSONArray dataClasses = itemAsArray(DATA_CLASS, obj);
 	    for (final Object o : dataClasses) {
 	        this.recordTypes.add(objectToRecordType(o));
 	    }
 	}
 
 	/**
 	 * Helper utility to process the remainder of a configuration as a
 	 * 'listener'.
 	 *
 	 * @param obj
 	 *            The context to look up keys in.
 	 * @throws ConfigurationException
 	 *             If an error is detected in the configuration.
 	 */
 	void handleListener(final JSONObject obj) throws ConfigurationException {
 	    this.listener = true;
 		setPendingDigestMax(itemAsNumber(PENDING_DGST_MAX, obj).shortValue());
 		setPendingDigestTimeout(itemAsNumber(PENDING_DGST_TIMEOUT, obj)
				.longValue());
 		setInputPath(new File(itemAsString(INPUT, obj, true)));
 		setOutputPath(new File(itemAsString(OUTPUT, obj, true)));
 		final JSONArray peers = itemAsArray(PEERS, obj);
 		for (final Object o : peers) {
 			final JSONObject elm = asJsonObject(this.source, null, o);
 			updateKnownHosts(elm);
 		}
 	}
 
 	/**
 	 * Helper utility to process a segment of the configuration as a
 	 * 'publisher'.
 	 *
 	 * @param publisher
 	 *            The context to look up keys in.
 	 * @throws ConfigurationException
 	 *             If an error is detected in the configuration.
 	 */
 	void handlePublisher(final JSONObject publisher)
 			throws ConfigurationException {
 	    this.listener = false;
         handleSessionTimeout(publisher);
         handleDataClass(publisher);
 		setRole(Role.Publisher);
 		setInputPath(new File(itemAsString(INPUT, publisher, true)));
 	}
 
 	/**
 	 * Helper utility to process a segment of the configuration as a
 	 * 'subscriber'.
 	 *
 	 * @param subscriber
 	 *            The context to look up keys in.
 	 * @throws ConfigurationException
 	 *             If an error is detected in the configuration.
 	 */
 	void handleSubscriber(final JSONObject subscriber)
 			throws ConfigurationException {
         this.listener = false;
 		setRole(Role.Subscriber);
 		handleSessionTimeout(subscriber);
 		handleDataClass(subscriber);
 		setOutputPath(new File(itemAsString(OUTPUT, subscriber, true)));
 		setPendingDigestMax(itemAsNumber(PENDING_DGST_MAX, subscriber)
 				.shortValue());
 		setPendingDigestTimeout(itemAsNumber(PENDING_DGST_TIMEOUT, subscriber)
				.longValue());
 	}
 
 	/**
 	 * Returns whether or not this Config specifies a listener, or connector.
 	 *
 	 * @return <code>true</code> if the configuration indicates to a listener.
 	 * <code>false</code> if the configuration specifies a connection should be
 	 * initiated.
 	 */
 	public boolean isListener() {
 		return this.listener;
 	}
 
 	/**
 	 * Lookup the required element named by 'key' in the {@link JSONObject} obj.
 	 *
 	 *
 	 * @see Config#itemAsArray(String, JSONObject, boolean)
 	 * @param key
 	 *            The name of the element to look up.
 	 * @param obj
 	 *            The {@link JSONObject} to find the key in.
 	 * @return The {@link JSONArray} for the key, or NULL if none exists.
 	 * @throws ConfigurationException
 	 */
 	JSONArray itemAsArray(final String key, final JSONObject obj)
 			throws ConfigurationException {
 		return itemAsArray(key, obj, true);
 	}
 
 	/**
 	 * Simple helper function to retrieve an element as a {@link JSONArray}. If
 	 * the element is not an array, this function throws an appropriate
 	 * {@link ConfigurationException}.
 	 *
 	 * @param key
 	 *            The name of the element to look up.
 	 * @param obj
 	 *            The {@link JSONObject} to find the key in.
 	 * @param required
 	 *            If this is set to true, a {@link ConfigurationException} is
 	 *            thrown if the key is not found.
 	 * @return The {@link JSONArray} for the key, or NULL if none exists.
 	 * @throws ConfigurationException
 	 */
 	JSONArray itemAsArray(final String key, final JSONObject obj,
 			final boolean required) throws ConfigurationException {
 		final Object o = obj.get(key);
 
 		if (!required && (o == null)) {
 			return (JSONArray) o;
 		}
 		return asJsonArray(this.source, key, o);
 	}
 
 	/**
 	 * Look up the required key as a Number in the {@link JSONObject} obj.
 	 *
 	 * @see Config#itemAsNumber(String, JSONObject, boolean)
 	 *
 	 * @param key
 	 *            The key to look up.
 	 * @param obj
 	 *            The {@link JSONObject} to find the key in.
 	 * @return The value of key as a {@link Number}.
 	 * @throws ConfigurationException
 	 */
 	Number itemAsNumber(final String key, final JSONObject obj)
 			throws ConfigurationException {
 		return itemAsNumber(key, obj, true);
 	}
 
 	/**
 	 * Simple helper function to retrieve an element as a {@link Number}. If the
 	 * element is not a number, this function throws an appropriate
 	 * {@link ConfigurationException}.
 	 *
 	 * @param key
 	 *            The name of the element to look up.
 	 * @param obj
 	 *            The {@link JSONObject} to find the key in.
 	 * @param required
 	 *            If this is set to true, a {@link ConfigurationException} is
 	 *            thrown if the key is not found.
 	 * @return The {@link JSONArray} for the key, or NULL if none exists.
 	 * @throws ConfigurationException
 	 */
 	Number itemAsNumber(final String key, final JSONObject obj,
 			final boolean required) throws ConfigurationException {
 		final Object o = obj.get(key);
 		if (!required && (o == null)) {
 			return (Number) o;
 		}
 		return asNumberValue(this.source, key, o);
 	}
 
 	/**
 	 * Look up the required key in the {@link JSONObject} obj.
 	 *
 	 * @param key
 	 *            The key to look up.
 	 * @param obj
 	 *            The {@link JSONObject} to find the key in.
 	 * @return The value for key.
 	 * @throws ConfigurationException
 	 * @see {@link Config#itemAsString(String, JSONObject, boolean)}
 	 */
 	String itemAsString(final String key, final JSONObject obj)
 			throws ConfigurationException {
 		return itemAsString(key, obj, true);
 	}
 
 	/**
 	 * Simple helper function to retrieve an element as a {@link String}. If the
 	 * element is not a string, this function throws an appropriate
 	 * {@link ConfigurationException}.
 	 *
 	 * @param key
 	 *            The name of the element to look up.
 	 * @param obj
 	 *            The {@link JSONObject} to find the key in.
 	 * @param required
 	 *            If this is set to true, a {@link ConfigurationException} is
 	 *            thrown if the key is not found.
 	 * @return The value for the key, or NULL if none exists.
 	 * @throws ConfigurationException
 	 */
 	String itemAsString(final String key, final JSONObject obj,
 			final boolean required) throws ConfigurationException {
 		Object o = obj.get(key);
 		if (!required && (o == null)) {
 			return (String) o;
 		}
 		return asStringValue(this.source, key, o);
 	}
 
 	/**
 	 * Helper utility to create a {@link RecordType} from a JSON Object.
 	 * 
 	 * @param o
 	 *            The object to convert to a {@link RecordType}. This is
 	 *            expected to be a value from a {@link JSONObject} or
 	 *            {@link JSONArray}.
 	 * @return The {@link RecordType}
 	 * @throws ConfigurationException
 	 *             If there is a problem converting to a {@link RecordType}
 	 */
 	RecordType objectToRecordType(final Object o) throws ConfigurationException {
 		String dataClass = asStringValue(this.source, null, o);
 		if (JOURNAL.equals(dataClass)) {
 			return RecordType.Journal;
 		} else if (AUDIT.equals(dataClass)) {
 			return RecordType.Audit;
 		} else if (LOG.equals(dataClass)) {
 			return RecordType.Log;
 		}
 		throw new ConfigurationException(this.source, "Expected '" + DATA_CLASS
 				+ "' to be one of '" + JOURNAL + "', '" + AUDIT + "', or '"
 				+ LOG + "' (found '" + dataClass + "'.");
 	}
 
 	/**
 	 * Take a JSON array of strings and convert it to a set of
 	 * {@link RecordType}.
 	 *
 	 * @param recordTypesArr
 	 * @return A {@link Set} containing all the {@link RecordType} indicated in
 	 *         recordTypesArr.
 	 * @throws ConfigurationException
 	 *             If there is an error converting the values to a
 	 *             {@link RecordType}s.
 	 */
 	Set<RecordType> recordSetFromArray(final JSONArray recordTypesArr)
 			throws ConfigurationException {
 		final Set<RecordType> rtSet = new HashSet<RecordType>();
 		if (recordTypesArr != null) {
 			for (final Object o : recordTypesArr) {
 				final RecordType rt = objectToRecordType(o);
 				rtSet.add(rt);
 			}
 		}
 		return rtSet;
 	}
 
 	/**
 	 * Set the address for this {@link Config}.
 	 *
 	 * @param address
 	 *            The address.
 	 */
 	public void setAddress(final InetAddress address) {
 		this.address = address;
 	}
 
 	/**
 	 * Configure this to initiate a connection, or listen for connetions.
 	 *
 	 * @param connect
 	 *            if set to true, specifies that a connection should be
 	 *            initiated. If set to false, specifies to listen for
 	 *            connections.
 	 */
 	public void setConnect(final boolean connect) {
 		this.listener = connect;
 	}
 
 	/**
 	 * Set the path to a directory to user for input. Not applicable to a
 	 * "Subscriber".
 	 *
 	 * @param inputPath
 	 *            The path to get records from.
 	 */
 	public void setInputPath(final File inputPath) {
 		this.inputPath = inputPath;
 	}
 
 	/**
 	 * Set the path to store recrods from remotes in. Not applicable to a
 	 * "Publisher".
 	 *
 	 * @param outputPath
 	 */
 	public void setOutputPath(final File outputPath) {
 		this.outputPath = outputPath;
 	}
 
 	/**
 	 * Set the peer configurations.
 	 *
 	 * @param peerConfigs
 	 *            a {@link Map} of {@link InetAddress} to {@link PeerConfig}
 	 *            objects indicating what records each remote is allowed to
 	 *            publish/subscribe to.
 	 */
 	public void setPeerConfigs(final Map<InetAddress, PeerConfig> peerConfigs) {
 		this.peerConfigs = peerConfigs;
 	}
 
 	/**
 	 * Set the maximum number of digests to store before sending a "digest"
 	 * message.
 	 *
 	 * @param pendingDigestMax
 	 *            The number of digests to calculate before sending a "digest"
 	 *            message.
 	 */
 	public void setPendingDigestMax(final short pendingDigestMax) {
 		this.pendingDigestMax = pendingDigestMax;
 	}
 
 	/**
 	 * Set the maximum number of seconds to wait before sending a "digest"
 	 * message.
 	 *
 	 * @param pendingDigestTimeout
 	 *            The time to wait, in seconds.
 	 */
	public void setPendingDigestTimeout(final long pendingDigestTimeout) {
 		this.pendingDigestTimeout = pendingDigestTimeout;
 	}
 
 	/**
 	 * Set the port to connect to/listen on.
 	 *
 	 * @param port
 	 *            The port number.
 	 */
 	public void setPort(final int port) {
 		this.port = port;
 	}
 
 	/**
 	 * Set the role. This is only applicable for connectors (i.e.
 	 * {@link Config#isListener()} returns <code>false</code>).
 	 *
 	 * @param role
 	 *            The role.
 	 */
 	public void setRole(final Role role) {
 		this.role = role;
 	}
 
 	/**
 	 * Configure the session timeout. This is only applicable for connectors
 	 * (i.e. {@link Config#isListener()} returns <code>false</code>).
 	 *
 	 * @param sessionTimeout
 	 */
 	public void setSessionTimeout(final Date sessionTimeout) {
 		this.sessionTimeout = sessionTimeout;
 	}
 
 	/**
 	 * Simple helper utility to convert a string to an InetAddress. Currently,
 	 * only IPv4 addresses are supported.
 	 *
 	 * @param s
 	 *            The String to convert to an {@link InetAddress}
 	 * @return The {@link InetAddresss} for <code>s</code>
 	 * @throws ConfigurationException
 	 *             If <code>s</code> cannot be converted to an
 	 *             {@link InetAddress}
 	 */
 	InetAddress stringToAddress(final String s) throws ConfigurationException {
 		try {
 			return InetAddress.getByAddress(s, new byte[4]);
 		} catch (final UnknownHostException e) {
 			throw new ConfigurationException(this.source,
 					"Bad value for key: '" + ADDRESS + "' (" + e.getMessage()
 							+ ")");
 		}
 	}
 
 	/**
 	 * Helper utility to append permissions to the {@link PeerConfig} map.
 	 *
 	 * @param elm
 	 *            A "PeerConfig" object.
 	 * @throws ConfigurationException
 	 *             If an problem is detected in the configuration file.
 	 */
 	void updateKnownHosts(final JSONObject elm) throws ConfigurationException {
 		final JSONArray hosts = itemAsArray(HOSTS, elm, false);
 		final JSONArray pubAllowArray = itemAsArray(PUBLISH_ALLOW, elm, false);
 		final Set<RecordType> pubAllow = recordSetFromArray(pubAllowArray);
 		final JSONArray subAllowArray = itemAsArray(SUBSCRIBE_ALLOW, elm, false);
 		final Set<RecordType> subAllow = recordSetFromArray(subAllowArray);
 
 		for (final Object o : hosts) {
 			String host;
 			host = asStringValue(this.source, null, o);
 			final InetAddress hostAddress = stringToAddress(host);
 			PeerConfig pc;
 			if (this.peerConfigs.containsKey(hostAddress)) {
 				pc = this.peerConfigs.get(hostAddress);
 			} else {
 				pc = new PeerConfig();
 				this.peerConfigs.put(hostAddress, pc);
 			}
 			pc.getSubscribeAllow().addAll(subAllow);
 			pc.getPublishAllow().addAll(pubAllow);
 		}
 	}
 
 	/**
 	 * Helper utility to cast a {@link Object} as a {@link String}.
 	 *
 	 * @param path
 	 *            The file where the error occurred.
 	 * @param key
 	 *            The key (if any) for <code>o</code>.
 	 * @param o
 	 *            The {@link Object} to cast.
 	 * @return <code>o</code> cast as a {@link String}.
 	 * @throws ConfigurationException
 	 *             if <code>o</code> is <code>null</code> or not a
 	 *             {@link String}.
 	 */
 	static String asStringValue(String path, String key, Object o)
 			throws ConfigurationException {
 		if (o instanceof String) {
 			return (String) o;
 		}
 		if (key != null) {
 			throw new ConfigurationException(path, "Expected String for '"
 					+ key + "', found '" + o + "'");
 		} else {
 			throw new ConfigurationException(path, "Expected String, found '"
 					+ o + "'");
 		}
 	}
 
 	/**
 	 * Helper utility to cast a {@link Object} as a {@link Number}.
 	 *
 	 * @param path
 	 *            The file where the error occurred.
 	 * @param key
 	 *            The key (if any) for <code>o</code>.
 	 * @param o
 	 *            The {@link Object} to cast.
 	 * @return <code>o</code> cast as a {@link Number}.
 	 * @throws ConfigurationException
 	 *             if <code>o</code> is <code>null</code> or not a
 	 *             {@link Number}.
 	 */
 	static Number asNumberValue(String path, String key, Object o)
 			throws ConfigurationException {
 		if (o instanceof Number) {
 			return (Number) o;
 		}
 		if (key != null) {
 			throw new ConfigurationException(path, "Expected Number for '"
 					+ key + "', found '" + o + "'");
 		} else {
 			throw new ConfigurationException(path, "Expected Number, found '"
 					+ o + "'");
 		}
 	}
 
 	/**
 	 * Helper utility to cast an {@link Object} as a {@link JSONObject}.
 	 *
 	 * @param path
 	 *            The file where the error occurred.
 	 * @param key
 	 *            The key (if any) for <code>o</code>.
 	 * @param o
 	 *            The {@link Object} to cast
 	 * @return <code>o</code> cast as a {@link JSONObject}.
 	 * @throws ConfigurationException
 	 *             if <code>o</code> is <code>null</code> or not a
 	 *             {@link JSONObject}.
 	 */
 	static JSONObject asJsonObject(String path, String key, Object o)
 			throws ConfigurationException {
 		if (o instanceof JSONObject) {
 			return (JSONObject) o;
 		}
 		if (key != null) {
 			throw new ConfigurationException(path, "Expected JSON Object for '"
 					+ key + "', found '" + o + "'");
 		} else {
 			throw new ConfigurationException(path,
 					"Expected JSON Object, found '" + o + "'");
 		}
 	}
 
 	/**
 	 * Helper utility to cast a value to a {@link JSONArray}.
 	 *
 	 * @param path
 	 *            The file where the error occurred.
 	 * @param key
 	 *            The key (if any) for <code>o</code>.
 	 * @param o
 	 *            The {@link Object} to cast.
 	 * @return <code>o</code> cast as a {@link JSONArray}
 	 * @throws ConfigurationException
 	 *             if <code>o</code> is <code>null</code> or not a
 	 *             {@link JSONArray}.
 	 */
 	static JSONArray asJsonArray(String path, String key, Object o)
 			throws ConfigurationException {
 		if (o instanceof JSONArray) {
 			return (JSONArray) o;
 		}
 		if (key != null) {
 			throw new ConfigurationException(path, "Expected JSON Array for '"
 					+ key + "', found '" + o + "'");
 		} else {
 			throw new ConfigurationException(path,
 					"Expected JSON Array, found '" + o + "'");
 		}
 	}
 }
