 package com.mymed.model.core.wrappers.cassandra.api07;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.cassandra.thrift.AuthenticationException;
 import org.apache.cassandra.thrift.AuthenticationRequest;
 import org.apache.cassandra.thrift.AuthorizationException;
 import org.apache.cassandra.thrift.Cassandra.Client;
 import org.apache.cassandra.thrift.CfDef;
 import org.apache.cassandra.thrift.Column;
 import org.apache.cassandra.thrift.ColumnOrSuperColumn;
 import org.apache.cassandra.thrift.ColumnParent;
 import org.apache.cassandra.thrift.ColumnPath;
 import org.apache.cassandra.thrift.ConsistencyLevel;
 import org.apache.cassandra.thrift.IndexClause;
 import org.apache.cassandra.thrift.InvalidRequestException;
 import org.apache.cassandra.thrift.KeyRange;
 import org.apache.cassandra.thrift.KeySlice;
 import org.apache.cassandra.thrift.KsDef;
 import org.apache.cassandra.thrift.Mutation;
 import org.apache.cassandra.thrift.NotFoundException;
 import org.apache.cassandra.thrift.SlicePredicate;
 import org.apache.cassandra.thrift.TimedOutException;
 import org.apache.cassandra.thrift.TokenRange;
 import org.apache.cassandra.thrift.UnavailableException;
 import org.apache.thrift.TException;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.connection.Connection;
 import com.mymed.controller.core.manager.connection.ConnectionManager;
 import com.mymed.utils.MConverter;
 import com.mymed.utils.MLogger;
 
 /**
  * Wrapper for the Cassandra API v0.7.<br />
  * For more info about the API, check <a
  * href="http://wiki.apache.org/cassandra/API">the API web page</a>.
  * 
  * @author Milo Casagrande
  * 
  */
 public class CassandraWrapper implements ICassandraWrapper {
 	/*
 	 * The port number to use for interacting with Cassandra
 	 */
 	private static final int PORT_NUMBER = 9160;
 
 	/*
 	 * The name of the default keyspace we use for Cassandra
 	 */
 	private static final String KEYSPACE = "Mymed";
 
 	/*
 	 * The connection manager that provides the connection to Cassandra
 	 */
 	private static final ConnectionManager manager = ConnectionManager.getInstance();
 
 	/*
 	 * The connection instance
 	 */
 	private Connection connection;
 
 	/*
	 * Address and port tu ose for establishing a connection
 	 */
 	private final String address;
 	private final int port;
 
 	/**
 	 * Empty constructor to create a normal Cassandra client, with address the
 	 * machine address, and port the default port
 	 * 
 	 * @throws UnknownHostException
 	 * @throws InternalBackEndException
 	 */
 	public CassandraWrapper() throws UnknownHostException, InternalBackEndException {
 		this(InetAddress.getLocalHost().getHostAddress(), PORT_NUMBER);
 	}
 
 	/**
 	 * Create the Cassandra client
 	 * 
 	 * @param address
 	 *            the address to use for the connection
 	 * @param port
 	 *            the port to use for the connection
 	 * @throws InternalBackEndException
 	 */
 	public CassandraWrapper(final String address, final int port) throws InternalBackEndException {
 		this.address = address;
 		this.port = port;
 	}
 
 	/**
 	 * Retrieve a connection from the pool and return the Cassandra client
 	 * 
 	 * @return the Cassandra client of the connection
 	 * @throws InternalBackEndException
 	 */
 	private Client getClient() throws InternalBackEndException {
 		connection = (Connection) manager.checkOut(address, port);
 
 		try {
 			MLogger.getLog().info("Setting keyspace to '{}'", KEYSPACE);
 			connection.getClient().set_keyspace(KEYSPACE);
 		} catch (final InvalidRequestException ex) {
 			MLogger.getDebugLog().debug("Error setting the keyspace '{}'", KEYSPACE, ex.getCause());
 			throw new InternalBackEndException("Error setting the keyspace");
 		} catch (final TException ex) {
 			MLogger.getDebugLog().debug("Error setting the keyspace '{}'", KEYSPACE, ex.getCause());
 			throw new InternalBackEndException("Error setting the keyspace");
 		}
 
 		return connection.getClient();
 	}
 
 	@Override
 	public void login(final AuthenticationRequest authRequest) throws InternalBackEndException {
 		try {
 			getClient().login(authRequest);
 		} catch (final AuthenticationException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final AuthorizationException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 	}
 
 	@Override
 	public void set_keyspace(final String keySpace) throws InternalBackEndException {
 		try {
 			getClient().set_keyspace(keySpace);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 	}
 
 	@Override
 	public ColumnOrSuperColumn get(final String key, final ColumnPath path, final ConsistencyLevel level)
 	        throws IOBackEndException, InternalBackEndException {
 		/*
 		 * Little trick in order to avoid TimedOutException on Cassandra. We
 		 * experienced some timeouts caused by one or two nodes to be down, but
 		 * still part of the ring. Looks like Cassandra in the version we use
 		 * tries to connect to a node that is down nonetheless, resulting in
 		 * timeout exceptions. We retry three times the get operation, in order
 		 * to pass over this error.
 		 */
 		int ttl = 3;
 
 		do {
 			try {
 				return get(key, path, level, ttl--);
 			} catch (final TimedOutException ex) {
 				MLogger.getDebugLog().debug("Impossible to connect to a host", ex.getCause());
 				continue;
 			}
 		} while (ttl > 0);
 
 		throw new InternalBackEndException("TimedOutException");
 	}
 
 	/*
 	 * Where we perform the real get operation
 	 */
 	private ColumnOrSuperColumn get(final String key, final ColumnPath path, final ConsistencyLevel level, final int ttl)
 	        throws IOBackEndException, InternalBackEndException, TimedOutException {
 
 		final ByteBuffer keyToBuffer = MConverter.stringToByteBuffer(key);
 		ColumnOrSuperColumn result = null;
 
 		try {
 			result = getClient().get(keyToBuffer, path, level);
 		} catch (final NotFoundException ex) {
 			throw new IOBackEndException(ex);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TimedOutException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return result;
 	}
 
 	@Override
 	public List<ColumnOrSuperColumn> get_slice(final String key, final ColumnParent parent,
 	        final SlicePredicate predicate, final ConsistencyLevel level) throws InternalBackEndException {
 		/*
 		 * Little trick in order to avoid TimedOutException on Cassandra. We
 		 * experienced some timeouts caused by one or two nodes to be down, but
 		 * still part of the ring. Looks like Cassandra in the version we use
 		 * tries to connect to a node that is down nonetheless, resulting in
 		 * timeout exceptions. We retry three times the get operation, in order
 		 * to pass over this error.
 		 */
 		int ttl = 3;
 
 		do {
 			try {
 				return get_slice(key, parent, predicate, level, ttl--);
 			} catch (final TimedOutException ex) {
 				MLogger.getDebugLog().debug("Impossible to connect to a host", ex.getCause());
 				continue;
 			}
 		} while (ttl > 0);
 
 		throw new InternalBackEndException("TimedOutException");
 	}
 
 	/*
 	 * Where we perform the real get operation
 	 */
 	private List<ColumnOrSuperColumn> get_slice(final String key, final ColumnParent parent,
 	        final SlicePredicate predicate, final ConsistencyLevel level, final int ttl)
 	        throws InternalBackEndException, TimedOutException {
 
 		final ByteBuffer keyToBuffer = MConverter.stringToByteBuffer(key);
 		List<ColumnOrSuperColumn> result = null;
 
 		try {
 			result = getClient().get_slice(keyToBuffer, parent, predicate, level);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TimedOutException ex) {
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return result;
 	}
 
 	@Override
 	public Map<ByteBuffer, List<ColumnOrSuperColumn>> multiget_slice(final List<String> keys,
 	        final ColumnParent parent, final SlicePredicate predicate, final ConsistencyLevel level)
 	        throws InternalBackEndException {
 		/*
 		 * Little trick in order to avoid TimedOutException on Cassandra. We
 		 * experienced some timeouts caused by one or two nodes to be down, but
 		 * still part of the ring. Looks like Cassandra in the version we use
 		 * tries to connect to a node that is down nonetheless, resulting in
 		 * timeout exceptions. We retry three times the get operation, in order
 		 * to pass over this error.
 		 */
 		int ttl = 3;
 
 		do {
 			try {
 				return multiget_slice(keys, parent, predicate, level, ttl--);
 			} catch (final TimedOutException ex) {
 				MLogger.getDebugLog().debug("Impossible to connect to a host", ex.getCause());
 				continue;
 			}
 		} while (ttl > 0);
 
 		throw new InternalBackEndException("TimedOutException");
 	}
 
 	/*
 	 * Where we perform the real get operation
 	 */
 	private Map<ByteBuffer, List<ColumnOrSuperColumn>> multiget_slice(final List<String> keys,
 	        final ColumnParent parent, final SlicePredicate predicate, final ConsistencyLevel level, final int ttl)
 	        throws InternalBackEndException, TimedOutException {
 		final List<ByteBuffer> keysToBuffer = MConverter.stringToByteBuffer(keys);
 		Map<ByteBuffer, List<ColumnOrSuperColumn>> result = null;
 
 		try {
 			result = getClient().multiget_slice(keysToBuffer, parent, predicate, level);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TimedOutException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return result;
 	}
 
 	@Override
 	public int get_count(final String key, final ColumnParent parent, final SlicePredicate predicate,
 	        final ConsistencyLevel level) throws InternalBackEndException {
 		/*
 		 * Little trick in order to avoid TimedOutException on Cassandra. We
 		 * experienced some timeouts caused by one or two nodes to be down, but
 		 * still part of the ring. Looks like Cassandra in the version we use
 		 * tries to connect to a node that is down nonetheless, resulting in
 		 * timeout exceptions. We retry three times the get operation, in order
 		 * to pass over this error.
 		 */
 		int ttl = 3;
 
 		do {
 			try {
 				return get_count(key, parent, predicate, level, ttl--);
 			} catch (final TimedOutException ex) {
 				MLogger.getDebugLog().debug("Impossible to connect to a host", ex.getCause());
 				continue;
 			}
 		} while (ttl > 0);
 
 		throw new InternalBackEndException("TimedOutException");
 	}
 
 	/*
 	 * Where we perform the real get operation
 	 */
 	private int get_count(final String key, final ColumnParent parent, final SlicePredicate predicate,
 	        final ConsistencyLevel level, final int ttl) throws InternalBackEndException, TimedOutException {
 
 		final ByteBuffer keyToBuffer = MConverter.stringToByteBuffer(key);
 		int result = -1;
 
 		try {
 			result = getClient().get_count(keyToBuffer, parent, predicate, level);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TimedOutException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return result;
 	}
 
 	@Override
 	public Map<ByteBuffer, Integer> multiget_count(final List<String> keys, final ColumnParent parent,
 	        final SlicePredicate predicate, final ConsistencyLevel level) throws InternalBackEndException {
 		/*
 		 * Little trick in order to avoid TimedOutException on Cassandra. We
 		 * experienced some timeouts caused by one or two nodes to be down, but
 		 * still part of the ring. Looks like Cassandra in the version we use
 		 * tries to connect to a node that is down nonetheless, resulting in
 		 * timeout exceptions. We retry three times the get operation, in order
 		 * to pass over this error.
 		 */
 		int ttl = 3;
 
 		do {
 			try {
 				return multiget_count(keys, parent, predicate, level, ttl--);
 			} catch (final TimedOutException ex) {
 				MLogger.getDebugLog().debug("Impossible to connect to a host", ex.getCause());
 				continue;
 			}
 		} while (ttl > 0);
 
 		throw new InternalBackEndException("TimedOutException");
 	}
 
 	/*
 	 * Where we perform the real get operation
 	 */
 	private Map<ByteBuffer, Integer> multiget_count(final List<String> keys, final ColumnParent parent,
 	        final SlicePredicate predicate, final ConsistencyLevel level, final int ttl)
 	        throws InternalBackEndException, TimedOutException {
 
 		final List<ByteBuffer> keysToBuffer = MConverter.stringToByteBuffer(keys);
 		Map<ByteBuffer, Integer> result = null;
 
 		try {
 			result = getClient().multiget_count(keysToBuffer, parent, predicate, level);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TimedOutException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return result;
 	}
 
 	@Override
 	public List<KeySlice> get_range_slices(final ColumnParent parent, final SlicePredicate predicate,
 	        final KeyRange range, final ConsistencyLevel level) throws InternalBackEndException {
 		/*
 		 * Little trick in order to avoid TimedOutException on Cassandra. We
 		 * experienced some timeouts caused by one or two nodes to be down, but
 		 * still part of the ring. Looks like Cassandra in the version we use
 		 * tries to connect to a node that is down nonetheless, resulting in
 		 * timeout exceptions. We retry three times the get operation, in order
 		 * to pass over this error.
 		 */
 		int ttl = 3;
 
 		do {
 			try {
 				return get_range_slices(parent, predicate, range, level, ttl--);
 			} catch (final TimedOutException ex) {
 				MLogger.getDebugLog().debug("Impossible to connect to a host", ex.getCause());
 				continue;
 			}
 		} while (ttl > 0);
 
 		throw new InternalBackEndException("TimedOutException");
 	}
 
 	/*
 	 * Where we perform the real get operation
 	 */
 	private List<KeySlice> get_range_slices(final ColumnParent parent, final SlicePredicate predicate,
 	        final KeyRange range, final ConsistencyLevel level, final int ttl) throws InternalBackEndException,
 	        TimedOutException {
 
 		List<KeySlice> result = null;
 
 		try {
 			result = getClient().get_range_slices(parent, predicate, range, level);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TimedOutException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return result;
 	}
 
 	@Override
 	public List<KeySlice> get_indexed_slices(final ColumnParent parent, final IndexClause clause,
 	        final SlicePredicate predicate, final ConsistencyLevel level) throws InternalBackEndException {
 
 		List<KeySlice> result = null;
 
 		try {
 			result = getClient().get_indexed_slices(parent, clause, predicate, level);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TimedOutException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return result;
 	}
 
 	@Override
 	public void insert(final String key, final ColumnParent parent, final Column column, final ConsistencyLevel level)
 	        throws InternalBackEndException {
 
 		final ByteBuffer keyToBuffer = MConverter.stringToByteBuffer(key);
 
 		try {
 			getClient().insert(keyToBuffer, parent, column, level);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TimedOutException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 	}
 
 	@Override
 	public void batch_mutate(final Map<String, Map<String, List<Mutation>>> mutationMap, final ConsistencyLevel level)
 	        throws InternalBackEndException {
 
 		final Map<ByteBuffer, Map<String, List<Mutation>>> newMap = new HashMap<ByteBuffer, Map<String, List<Mutation>>>(
 		        mutationMap.size());
 
 		ByteBuffer keyToBuffer = null;
 		Map<String, List<Mutation>> value = null;
 
 		final Iterator<Entry<String, Map<String, List<Mutation>>>> iterator = mutationMap.entrySet().iterator();
 		while (iterator.hasNext()) {
 			final Entry<String, Map<String, List<Mutation>>> entry = iterator.next();
 			keyToBuffer = MConverter.stringToByteBuffer(entry.getKey());
 			value = entry.getValue();
 
 			newMap.put(keyToBuffer, value);
 		}
 
 		try {
 			getClient().batch_mutate(newMap, level);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TimedOutException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 	}
 
 	@Override
 	public void remove(final String key, final ColumnPath path, final long timeStamp, final ConsistencyLevel level)
 	        throws InternalBackEndException {
 
 		final ByteBuffer keyToBuffer = MConverter.stringToByteBuffer(key);
 
 		try {
 			getClient().remove(keyToBuffer, path, timeStamp, level);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TimedOutException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 	}
 
 	@Override
 	public void truncate(final String columnFamily) throws InternalBackEndException {
 		try {
 			getClient().truncate(columnFamily);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final UnavailableException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 	}
 
 	@Override
 	public String describe_cluster_name() throws InternalBackEndException {
 		String result = null;
 
 		try {
 			result = getClient().describe_cluster_name();
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return result;
 	}
 
 	@Override
 	public KsDef describe_keyspace(final String keySpace) throws InternalBackEndException, IOBackEndException {
 
 		KsDef keySpaceDef = null;
 
 		try {
 			keySpaceDef = getClient().describe_keyspace(keySpace);
 		} catch (final NotFoundException ex) {
 			throw new IOBackEndException(ex);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return keySpaceDef;
 	}
 
 	@Override
 	public List<KsDef> describe_keyspaces() throws InternalBackEndException {
 
 		List<KsDef> keySpaceList = null;
 
 		try {
 			keySpaceList = getClient().describe_keyspaces();
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return keySpaceList;
 	}
 
 	@Override
 	public String describe_partitioner() throws InternalBackEndException {
 
 		String partitioner = null;
 
 		try {
 			partitioner = getClient().describe_partitioner();
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return partitioner;
 	}
 
 	@Override
 	public List<TokenRange> describe_ring(final String keySpace) throws InternalBackEndException {
 
 		List<TokenRange> ring = null;
 
 		try {
 			ring = getClient().describe_ring(keySpace);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return ring;
 	}
 
 	@Override
 	public String describe_snitch() throws InternalBackEndException {
 
 		String snitch = null;
 
 		try {
 			snitch = getClient().describe_snitch();
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return snitch;
 	}
 
 	@Override
 	public String describe_version() throws InternalBackEndException {
 
 		String version = null;
 
 		try {
 			version = getClient().describe_version();
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return version;
 	}
 
 	@Override
 	public String system_add_column_family(final CfDef cfDef) throws InternalBackEndException {
 
 		String schemaId = null;
 
 		try {
 			schemaId = getClient().system_add_column_family(cfDef);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return schemaId;
 	}
 
 	@Override
 	public String system_drop_column_family(final String columnFamily) throws InternalBackEndException {
 
 		String schemaId = null;
 
 		try {
 			schemaId = getClient().system_drop_column_family(columnFamily);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return schemaId;
 	}
 
 	@Override
 	public String system_add_keyspace(final KsDef ksDef) throws InternalBackEndException {
 
 		String schemaId = null;
 
 		try {
 			schemaId = getClient().system_add_keyspace(ksDef);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return schemaId;
 	}
 
 	@Override
 	public String system_drop_keyspace(final String keySpace) throws InternalBackEndException {
 
 		String schemaId = null;
 
 		try {
 			schemaId = getClient().system_drop_keyspace(keySpace);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return schemaId;
 	}
 
 	@Override
 	public String system_update_column_family(final CfDef columnFamily) throws InternalBackEndException {
 
 		String newSchemaId = null;
 
 		try {
 			newSchemaId = getClient().system_update_column_family(columnFamily);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return newSchemaId;
 	}
 
 	@Override
 	public String system_update_keyspace(final KsDef keySpace) throws InternalBackEndException {
 
 		String newSchemaId = null;
 
 		try {
 			newSchemaId = getClient().system_update_keyspace(keySpace);
 		} catch (final InvalidRequestException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final TException ex) {
 			throw new InternalBackEndException(ex);
 		} finally {
 			manager.checkIn(connection);
 		}
 
 		return newSchemaId;
 	}
 
 	/**
 	 * Retrieve the specified column family id in the given keyspace
 	 * 
 	 * @param keySpaceName
 	 *            the keyspace where the column family is located
 	 * @param columnFamilyName
 	 *            the column family to search
 	 * @return the id of the column family
 	 * @throws InternalBackEndException
 	 * @throws IOBackEndException
 	 */
 	public int get_cf_id(final String keySpaceName, final String columnFamilyName) throws InternalBackEndException,
 	        IOBackEndException {
 
 		int cfId = 0;
 		final Iterator<CfDef> iter = describe_keyspace(keySpaceName).getCf_defsIterator();
 
 		while (iter.hasNext()) {
 			final CfDef def = iter.next();
 
 			if (columnFamilyName.equals(def.getName())) {
 				cfId = def.getId();
 				break;
 			}
 		}
 
 		return cfId;
 	}
 }
