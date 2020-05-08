 package ch.inftec.ju.testing.db;
 
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Proxy;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 
 import org.dbunit.Assertion;
 import org.dbunit.DatabaseUnitException;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.database.QueryDataSet;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.operation.DatabaseOperation;
 import org.hibernate.jdbc.Work;
 import org.w3c.dom.Document;
 
 import ch.inftec.ju.db.ConnectionInfo;
 import ch.inftec.ju.db.JuDbException;
 import ch.inftec.ju.db.JuDbUtils;
 import ch.inftec.ju.util.IOUtil;
 import ch.inftec.ju.util.ReflectUtils;
 import ch.inftec.ju.util.XString;
 import ch.inftec.ju.util.xml.XmlOutputConverter;
 
 /**
  * Utility class containing methods to import and export data from a DB.
  * <p>
  * The class needs a DbConnection instance to work with. Make sure the connection
  * is available when using the util class.
  * @author Martin
  *
  */
 public class DbDataUtil {
 	private final Connection connection;
 	private final EntityManager em;
 	
 	private String schemaName = null;
 	
 	private Map<String, Object> configProperties = new HashMap<>();
 
 	/**
 	 * Creates a new DbDataUtil instance using the specifiec Connection.
 	 * <p>
 	 * If you need to specify a DB Schema, use the DbDataUtil(Connection, String) constructor.
 	 * @param connection Connection instance
 	 */
 	public DbDataUtil(Connection connection) {
 		this(connection, (String)null);
 	}
 	
 	public DbDataUtil(Connection connection, String schema) {
 		this.em = null;
 		this.connection = connection;
 		this.schemaName = schema;
 	}
 	
 	/**
 	 * Creates a new DbDataUtil instance using the specified Connection and the Schema
 	 * from the ConnectionInfo
 	 * @param connection Connection instance
 	 * @param ConnectionInfo to get the Schema to use
 	 */
 	public DbDataUtil(Connection connection, ConnectionInfo connectionInfo) {
 		this(connection, connectionInfo.getSchema());
 	}
 	
 	/**
 	 * Create a new DbDataUtil that will use the specified EntityManager to get
 	 * a raw connection to the DB and execute SQL queries.
 	 * @param em EntityManager instance to execute SQL in a JDBC connection
 	 */
 	public DbDataUtil(EntityManager em) {
 		this.em = em;
 		this.connection = null;
 	}
 	
 	/**
 	 * Sets the DB schema name to work with.
 	 * <p>
 	 * May be necessary for DBs like oracle to avoid duplicate name problems.
 	 * @param schemaName DB schema name
 	 * @return This util to allow for chaining
 	 */
 	public DbDataUtil setSchema(String schemaName) {
 		this.schemaName = schemaName;
 		return this;
 	}
 	
 	/**
 	 * Sets a config attribute of the underlying DbUnit IDatabaseConnection instance.
 	 * @param name Name of the attribute
 	 * @param value Value of the attribute
 	 * @return This instance to allow for chaining
 	 */
 	public DbDataUtil setConfigProperty(String name, Object value) {
 		this.configProperties.put(name, value);
 		return this;
 	}
 	
 	private void execute(final DbUnitWork work) {
 		if (this.connection != null) {
 			this.doExecute(this.connection, work);
 		} else if (this.em != null){
 			JuDbUtils.doWork(em, new Work() {
 				@Override
 				public void execute(Connection connection) throws SQLException {
 					doExecute(connection, work);
 				}
 			});
 		} else {
 			throw new IllegalStateException("DbDataUtil hasn't been initialized correctly");
 		}
 	}
 	
 	private void doExecute(Connection connection, DbUnitWork work) {
 		/**
 		 * Due to a JDBC 1.4 spec imcompatibility of the Oracle driver
 		 * (doesn't return IS_AUTOINCREMENT in table meta data), we need
 		 * to unwrap the actual JDBC connection in case this is a (Hibernate)
 		 * proxy.
 		 */
 		Connection unwrappedConn = null;
 		if (connection instanceof Proxy) {
 			try {
 				unwrappedConn = connection.unwrap(Connection.class);
 			} catch (Exception ex) {
 				throw new JuDbException("Couldn't unwrap Connection", ex);
 			}
 		}
 		final Connection realConn = unwrappedConn != null
 				? unwrappedConn
 				: connection;
 		
 		try {
 			IDatabaseConnection conn = new DatabaseConnection(realConn, this.schemaName);
 			for (String key : this.configProperties.keySet()) {
 				conn.getConfig().setProperty(key, this.configProperties.get(key));
 			}
 			work.execute(conn);
 		} catch (DatabaseUnitException ex) {
 			throw new JuDbException("Couldn't execute DbUnitWork", ex);
 		}
 	}
 	
 	public void cleanImport(String resourcePath) {
 		this.buildImport().from(resourcePath).executeCleanInsert();
 	}
 	
 	/**
 	 * Returns a new ExportBuilder to configure and execute DB data exports.
 	 * @return ExportBuilder instance
 	 */
 	public ExportBuilder buildExport() {
 		return new ExportBuilder(this);
 	}
 	
 	/**
 	 * Returns a new ImportBuilder to import data from XML resources into the DB.
 	 * @return ImportBuilder instance
 	 */
 	public ImportBuilder buildImport() {
 		return new ImportBuilder(this);
 	}
 	
 	/**
 	 * Returns a new AssertBuilder to assert that table data equals expected data
 	 * specified in an XML file.
 	 * @return AssertBuilder instance
 	 */
 	public AssertBuilder buildAssert() {
 		return new AssertBuilder(this);
 	}
 	
 	/**
 	 * Helper callback interface to execute code that needs a IDatabaseConnection
 	 * instance.
 	 * @author tgdmemae
 	 * <T> Return value
 	 *
 	 */
 	private static interface DbUnitWork {
 		public void execute(IDatabaseConnection conn);
 	}
 	
 	private static abstract class DbUnitWorkWithReturn<T> implements DbUnitWork {
 		private T returnValue;
 		
 		protected void setReturnValue(T returnValue) {
 			this.returnValue = returnValue;
 		}
 		
 		public T getReturnValue() {
 			return this.returnValue;
 		}
 	}
 	
 	/**
 	 * Builder class to configure and execute DB data exports.
 	 * @author Martin
 	 *
 	 */
 	public static class ExportBuilder {
 		private final DbDataUtil dbDataUtil;
 		private QueryDataSet queryDataSet;
 		
 		private ExportBuilder(DbDataUtil dbDataUtil) {
 			this.dbDataUtil = dbDataUtil;	
 		}
 		
 		/**
 		 * Adds the specific table to the builder, exporting the table data.
 		 * @param tableName Table name
 		 * @return ExportBuilder to allow for chaining
 		 */
 		public ExportBuilder addTable(String tableName) {
 			return this.addTable(tableName, null);
 		}
 		
 		/**
 		 * Adds the specified table to the builder, exporting the table data.
 		 * <p>
 		 * If no query is specified, all table data is exported. Otherwise, only
 		 * the data returned by the query is exported.
 		 * @param tableName TableName
 		 * @param query Optional query to select sub data
 		 * @return ExportBuilder to allow for chaining
 		 */
 		public ExportBuilder addTable(final String tableName, final String query) {
 			this.dbDataUtil.execute(new DbUnitWork() {
 				@Override
 				public void execute(IDatabaseConnection conn) {
 					try {
 						if (queryDataSet == null) {
 							queryDataSet = new QueryDataSet(conn);
 						}
 						
 						if (query == null) {
 							queryDataSet.addTable(tableName);
 						} else {
 							queryDataSet.addTable(tableName, query);
 						}
 					} catch (Exception ex) {
 						throw new JuDbException("Couldn't add table", ex);
 					}
 				}
 			});
 			
 			return this;
 		}
 		
 		/**
 		 * Adds the data of the specified table, ordering by the specified columns.
 		 * @param tableName Table names
 		 * @param orderColumns List of columns to order by
 		 * @return ExportBuilder to allow for chaining
 		 */
 		public ExportBuilder addTableSorted(String tableName, String... orderColumns) {
 			if (orderColumns.length == 0) {
 				return this.addTable(tableName);
 			} else {
 				XString xs = new XString();
 				xs.addFormatted("SELECT * FROM %s ORDER BY ", tableName);
 				for (String orderColumn : orderColumns) {
 					xs.assertText("ORDER BY ", ", ");
 					xs.addText(orderColumn);
 				}
 				
 				return this.addTable(tableName, xs.toString());
 			}
 		}
 
 		private IDataSet getExportSet() {
 			if (queryDataSet != null) {
 				return queryDataSet;
 			} else {
 				DbUnitWorkWithReturn<IDataSet> work = new DbUnitWorkWithReturn<IDataSet>() {
 					@Override
 					public void execute(IDatabaseConnection conn) {
 						try {
 							try {
 								this.setReturnValue(conn.createDataSet());
 							} catch (Exception ex) {
 								throw new JuDbException("Couldn't create DataSet from DB");
 							}
 						} catch (Exception ex) {
 							throw new JuDbException("Couldn't add table", ex);
 						}
 					}
 				};
 				this.dbDataUtil.execute(work);
 				return work.getReturnValue();
 			}
 		}
 		
 		/**
 		 * Writes the DB data to an (in-memory) XML Document.
 		 * @return Xml Document instance
 		 */
 		public Document writeToXmlDocument() {
 			try {
 				XmlOutputConverter xmlConv = new XmlOutputConverter();
 				FlatXmlDataSet.write(this.getExportSet(), xmlConv.getOutputStream());
 				
 				return xmlConv.getDocument();
 			} catch (Exception ex) {
 				throw new JuDbException("Couldn't write DB data to XML document", ex);
 			}
 		}
 		
 		/**
 		 * Write the DB data to an XML file.
 		 * @param fileName Path of the file
 		 */
 		public void writeToXmlFile(String fileName) {
 			try (OutputStream stream = new BufferedOutputStream(
 							new FileOutputStream(fileName))) {
 
 				FlatXmlDataSet.write(this.getExportSet(), stream);
 			} catch (Exception ex) {
 				throw new JuDbException("Couldn't write DB data to file " + fileName, ex);
 			}
 		}		
 	}
 	
 	/**
 	 * Builder class to configure and execute DB data imports.
 	 * @author Martin
 	 *
 	 */
 	public static class ImportBuilder {
 		private final DbDataUtil dbDataUtil;
 		private FlatXmlDataSet flatXmlDataSet;
 		
 		private ImportBuilder(DbDataUtil dbDataUtil) {
 			this.dbDataUtil = dbDataUtil;	
 		}
 		
 		/**
 		 * Imports DB data from the specified XML
 		 * @param resourcePath Resource path, either absolute or relative to the current class
 		 * @return ImportBuilder
 		 */
 		public ImportBuilder from(String resourcePath) {
 			URL url = IOUtil.getResourceURL(resourcePath, ReflectUtils.getCallingClass());
 			return from(url);
 		}
 		
 		/**
 		 * Imports DB data from the specified XML
 		 * @param xmlUrl URL to XML file location
 		 */
 		public ImportBuilder from(URL xmlUrl) {
 			try {
 				flatXmlDataSet = new FlatXmlDataSetBuilder()
 					.setColumnSensing(true)
 					.build(xmlUrl);
 				return this;
 			} catch (Exception ex) {
				throw new JuDbException("Couldn't import data from XML: xmlUrl", ex);
 			}
 		}
 		
 		/**
 		 * Performs a clean import of the data into the DB, i.e. cleans any existing
 		 * data in affected tables and imports the rows specified in in this builder.
 		 */
 		public void executeCleanInsert() {
 			this.dbDataUtil.execute(new DbUnitWork() {
 				@Override
 				public void execute(IDatabaseConnection conn) {
 					try {
 						DatabaseOperation.CLEAN_INSERT.execute(conn, flatXmlDataSet);
 					} catch (Exception ex) {
 						throw new JuDbException("Couldnt clean and insert data into DB", ex);
 					}
 				}
 			});
 		}
 		
 		/**
 		 * Truncates all tables included in the data set.
 		 */
 		public void executeDeleteAll() {
 			this.dbDataUtil.execute(new DbUnitWork() {
 				@Override
 				public void execute(IDatabaseConnection conn) {
 					try {
 						DatabaseOperation.DELETE_ALL.execute(conn, flatXmlDataSet);
 					} catch (Exception ex) {
 						throw new JuDbException("Couldnt truncate data in DB", ex);
 					}
 				};
 			});
 		}
 		
 		/**
 		 * Performs an import of the data into the DB, without cleaning any data
 		 * previously.
 		 */
 		public void executeInsert() {
 			this.dbDataUtil.execute(new DbUnitWork() {
 				@Override
 				public void execute(IDatabaseConnection conn) {
 					try {
 						DatabaseOperation.INSERT.execute(conn, flatXmlDataSet);
 					} catch (Exception ex) {
 						throw new JuDbException("Couldnt insert data into DB", ex);
 					}
 				};
 			});
 			
 		}
 	}	
 	
 	/**
 	 * Builder class to configure and execute DB data asserts.
 	 * @author Martin
 	 *
 	 */
 	public static class AssertBuilder {
 		private final DbDataUtil dbDataUtil;
 		private FlatXmlDataSet flatXmlDataSet;
 		
 		private AssertBuilder(DbDataUtil dbDataUtil) {
 			this.dbDataUtil = dbDataUtil;	
 		}
 		
 		/**
 		 * URL to XML of expected data.
 		 * @param xmlUrl URL to XML file location
 		 * @return This builder to allow for chaining
 		 */
 		public AssertBuilder expected(URL xmlUrl) {
 			try {
 				flatXmlDataSet = new FlatXmlDataSetBuilder().build(xmlUrl);
 				return this;
 			} catch (Exception ex) {
 				throw new JuDbException("Couldn't import data from XML: xmlUrl", ex);
 			}
 		}
 		
 		/**
 		 * Asserts that the whole data set in the DB equals the expected data.
 		 */
 		public void assertEqualsAll() {
 			this.dbDataUtil.execute(new DbUnitWork() {
 				@Override
 				public void execute(IDatabaseConnection conn) {
 					try {
 						IDataSet  dbDataSet = conn.createDataSet();
 						Assertion.assertEquals(flatXmlDataSet, dbDataSet);
 					} catch (Exception ex) {
 						throw new JuDbException("Couldn't assert DB data", ex);
 					}
 				}
 			});
 		}
 		
 		/**
 		 * Asserts that the export from the specified table equals the expected data.
 		 * @param tableName Name of the table to assert
 		 * @param orderColumnName Name of the column to order data by for the export
 		 */
 		public void assertEqualsTable(final String tableName, final String orderColumnName) {
 			this.dbDataUtil.execute(new DbUnitWork() {
 				@Override
 				public void execute(IDatabaseConnection conn) {
 					try {
 						QueryDataSet tableDataSet = new QueryDataSet(conn);
 						tableDataSet.addTable(tableName, String.format("select * from %s order by %s", tableName, orderColumnName));
 						
 						Assertion.assertEquals(flatXmlDataSet, tableDataSet);
 					} catch (Exception ex) {
 						throw new JuDbException("Couldn't assert DB data", ex);
 					}
 				}
 			});
 		}
 	}
 }
