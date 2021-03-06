 /*
  * Copyright (c) 2013 Data Harmonisation Panel
  * 
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this distribution. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Data Harmonisation Panel <http://www.dhpanel.eu>
  */
 
 package eu.esdihumboldt.hale.io.jdbc;
 
 import java.net.URI;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import de.cs3d.util.logging.ALogger;
 import de.cs3d.util.logging.ALoggerFactory;
 import eu.esdihumboldt.hale.common.instance.model.Filter;
 import eu.esdihumboldt.hale.common.instance.model.Instance;
 import eu.esdihumboldt.hale.common.instance.model.InstanceCollection;
 import eu.esdihumboldt.hale.common.instance.model.InstanceReference;
 import eu.esdihumboldt.hale.common.instance.model.ResourceIterator;
 import eu.esdihumboldt.hale.common.instance.model.ext.InstanceIterator;
 import eu.esdihumboldt.hale.common.instance.model.impl.FilteredInstanceCollection;
 import eu.esdihumboldt.hale.common.schema.model.TypeDefinition;
 import eu.esdihumboldt.hale.io.jdbc.constraints.DatabaseTable;
 
 /**
  * Instance collection for instances belonging to a specific database table.
  * 
  * @author Simon Templer
  */
 public class JDBCTableCollection implements InstanceCollection {
 
 	private static final ALogger log = ALoggerFactory.getLogger(JDBCTableCollection.class);
 
 	/**
 	 * Iterator other a JDBC table.
 	 */
 	private class JDBCTableIterator implements InstanceIterator {
 
 		private final TableInstanceBuilder builder = new TableInstanceBuilder();
 
 //		private static final int ROW_LIMIT = 100;
 
 		private final Connection connection;
 
 		private ResultSet currentResults;
 
 //		private final int currentOffset = 0;
 
 		/**
 		 * States if the row at the current cursor position was already
 		 * consumed.
 		 * 
 		 * Initially the result set points to the row before the first row,
 		 * which by definition is consumed.
 		 */
 		private boolean consumed = true;
 
 		private boolean hasNext = false;
 
 		private boolean done = false;
 
 		/**
 		 * Default constructor.
 		 */
 		public JDBCTableIterator() {
 			super();
 			try {
 				connection = createConnection();
 			} catch (SQLException e) {
 				throw new IllegalStateException("Could not create database connection", e);
 			}
 		}
 
 		@Override
 		public TypeDefinition typePeek() {
 			if (hasNext()) {
 				// always the same type returned in this iterator
 				return type;
 			}
 			return null;
 		}
 
 		@Override
 		public boolean supportsTypePeek() {
 			return true;
 		}
 
 		@Override
 		public boolean hasNext() {
 			proceedToNext();
 
 			return hasNext;
 		}
 
 		/**
 		 * Proceed to the next result
 		 */
 		private void proceedToNext() {
 			if (done) {
 				return;
 			}
 
 			try {
 				if (currentResults != null) {
 					// move cursor if necessary
 					if (consumed) {
 						hasNext = currentResults.next();
 						consumed = false;
 					}
 				}
 
 				if (currentResults != null && !hasNext) {
 					// currentResults has been completely processed
 
 					// set iterator to done
 					close();
 				}
 				else if (currentResults == null) {
 					// retrieve result set
 					Statement st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
 							ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
 
 					currentResults = st.executeQuery("SELECT * FROM " + fullTableName);
 
 					proceedToNext();
 				}
 			} catch (SQLException e) {
 				log.error(e.getMessage(), e);
 				close();
 			}
 		}
 
 		@Override
 		public Instance next() {
 			proceedToNext();
 
 			if (hasNext) {
 				// create instance from current cursor
 				Instance instance = builder.createInstance(type, currentResults);
 
 				consumed = true;
 
 				return instance;
 			}
 			else {
 				throw new IllegalStateException();
 			}
 		}
 
 		@Override
 		public void skip() {
 			proceedToNext();
 
 			if (hasNext) {
 				// mark as consumed
 				consumed = true;
 			}
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void close() {
 			if (currentResults != null) {
 				try {
 					currentResults.close();
 				} catch (SQLException e) {
 					// ignore
 				}
 			}
 			currentResults = null;
 			done = true;
 			hasNext = false;
 			try {
 				connection.close();
 			} catch (SQLException e) {
 				// ignore
 			}
 		}
 
 	}
 
 	private final URI jdbcURI;
 	private final String user;
 	private final String password;
 	private final TypeDefinition type;
 
 	private final String fullTableName;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param type the type definition associated to the table
 	 * @param jdbcURI the JDBC URI to access the database
 	 * @param user the database user
 	 * @param password the user's password
 	 */
 	public JDBCTableCollection(TypeDefinition type, URI jdbcURI, String user, String password) {
 		this.type = type;
 		this.jdbcURI = jdbcURI;
 		this.user = user;
 		this.password = password;
 
 		this.fullTableName = type.getConstraint(DatabaseTable.class).getFullTableName();
 	}
 
 	/**
 	 * Create a connection to the database.
 	 * 
 	 * @return the database connection
 	 * @throws SQLException if opening the connection fails
 	 */
 	protected Connection createConnection() throws SQLException {
 		return JDBCConnection.getConnection(jdbcURI, user, password);
 	}
 
 	@Override
 	public InstanceReference getReference(Instance instance) {
		// TODO Auto-generated method stub
		return null;
 	}
 
 	@Override
 	public Instance getInstance(InstanceReference reference) {
		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ResourceIterator<Instance> iterator() {
 		return new JDBCTableIterator();
 	}
 
 	@Override
 	public boolean hasSize() {
 		return true;
 	}
 
 	@Override
 	public int size() {
 		try (Connection connection = createConnection()) {
 			Statement st = connection.createStatement();
 
 			ResultSet res = st.executeQuery("SELECT COUNT(*) FROM " + fullTableName);
 			int count = 0;
 			if (res.next()) {
 				count = res.getInt(1);
 			}
 
 			st.close();
 
 			return count;
 		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			return UNKNOWN_SIZE;
 		}
 	}
 
 	@Override
 	public boolean isEmpty() {
 		int size = size();
		if (size == UNKNOWN_SIZE) {
			// say empty if size cannot be determined
			return true;
		}
 		return size <= 0;
 	}
 
 	@Override
 	public InstanceCollection select(Filter filter) {
 		// TODO apply filter to query instead!
 		return FilteredInstanceCollection.applyFilter(this, filter);
 	}
 
 }
