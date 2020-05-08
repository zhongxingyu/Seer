 package com.gentics.cr;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.util.generics.Lists;
 
 /**
  * {@link SQLRequestProcessor} fetches data from a mysql table
  * @author bigbear3001
  *
  */
 public class SQLRequestProcessor extends RequestProcessor {
 	private static Logger logger = Logger.getLogger(SQLRequestProcessor.class);
 
 	/**
 	 * configuration key for the datasource driver class.
 	 */
 	private static final String DSHDRIVERCLASS_KEY = "driverClass";
 	/**
 	 * configuration key for the datasource url.
 	 */
 	private static final String DSHURL_KEY = "url";
 	/**
 	 * configuration key for the table name.
 	 */
 	private static final String TABLEATTRIBUTE_KEY = "table";
 	/**
 	 * configuration key for the column names.
 	 */
 	private static final String COLUMNATTRIBUTE_KEY = "columns";
 	/**
 	 * configuration key for the column containing the id.
 	 */
 	private static final String IDCOLUMN_KEY = "idcolumn";
 	/**
 	 * Configuration key for the merge feature (multiple rows with the same id
 	 * are merged into one result).
 	 * @see #mergeOnIdColumn
 	 */
 	private static final String MERGE_ON_IDCOLUMN_KEY = "merge_on_idcolumn";
 
 	private String dshDriverClass = "";
 	private String dshUrl = "";
 	private String table = "";
 	private String[] columns = new String[] {};
 	private String idcolumn = "";
 
 	/**
 	 * defines whetever multiple rows with the same id should be merged into one
 	 * resolvable. if <code>false</code> the last row overrides all previous
 	 * rows in the result. this is the default behaviour.
 	 */
 	private boolean mergeOnIdColumn = false;
 
 	/**
 	* Create a new instance of SQLRequestProcessor
 	* @param config
 	* @throws CRException
 	*/
 	public SQLRequestProcessor(CRConfig config) throws CRException {
 		super(config);
 
 		Properties dshprop = ((CRConfigUtil) config).getDatasourceHandleProperties();
 		dshDriverClass = dshprop.getProperty(DSHDRIVERCLASS_KEY);
 		dshUrl = dshprop.getProperty(DSHURL_KEY);
 
 		Properties dsprops = ((CRConfigUtil) config).getDatasourceProperties();
 		table = dsprops.getProperty(TABLEATTRIBUTE_KEY);
 
 		String colatt = dsprops.getProperty(COLUMNATTRIBUTE_KEY);
 		if (colatt != null) {
 			columns = colatt.split(",");
 		}
 
 		idcolumn = dsprops.getProperty(IDCOLUMN_KEY);
 
 		mergeOnIdColumn = config.getBoolean(MERGE_ON_IDCOLUMN_KEY, mergeOnIdColumn);
 	}
 
 	private static final Pattern CONTAINSONEOFPATTERN = Pattern
 			.compile("object\\.([a-zA-Z0-9_]*)[ ]*CONTAINSONEOF[ ]*\\[(.*)\\]");
 
 	private String translate(String requestFilter) {
 		//TANSLATE CONTAINSONEOF
 		Matcher matcher = CONTAINSONEOFPATTERN.matcher(requestFilter);
 
 		StringBuffer buf = new StringBuffer();
 		while (matcher.find()) {
 			String attrib = matcher.group(1);
 			String group = matcher.group(2);
 			matcher.appendReplacement(buf, attrib + " IN (" + group + ")");
 		}
 		matcher.appendTail(buf);
 		requestFilter = buf.toString();
 
 		return requestFilter.replaceAll("==", "=").replaceAll("\"", "'");
 	}
 
 	private String getStatement(String requestFilter, String[] attributes) {
 		String statement = new String();
 		if (attributes == null || attributes.length == 0 || columns.length == 0) {
 			statement = "*";
 		} else {
 			if (!Arrays.asList(attributes).contains(idcolumn)) {
 				statement = idcolumn;
 			}
 			for (String att : attributes) {
 				if (Arrays.asList(columns).contains(att)) {
 					if (!statement.equals("")) {
 						statement += ",";
 					}
 					statement += att;
 				}
 			}
 
 		}
 		statement = "SELECT " + statement + " FROM " + this.table + " WHERE " + translate(requestFilter);
 		return statement;
 	}
 
 	/**
 	*
 	* getObjects 
 	* @param request CRRequest
 	* @param doNavigation boolean
 	* @return resulting objects
 	* @throws CRException
 	*/
 	@Override
 	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
 		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
 
 		Statement stmt = null;
 		ResultSet rset = null;
 		Connection conn = null;
 		String statementString = getStatement(request.getRequestFilter(), request.getAttributeArray(idcolumn));
 		try {
 			Class.forName(this.dshDriverClass);
 			conn = DriverManager.getConnection(this.dshUrl);
 
 			stmt = conn.createStatement();
 			logger.debug("Using statement: " + statementString);
 			rset = stmt.executeQuery(statementString);
 
 			if (mergeOnIdColumn) {
 				result = getMergedObjectsFromResultSet(rset);
 			} else {
 				result = getObjectsFromResultSet(rset);
 			}
 
 		} catch (SQLException e) {
 			logger.error("Error executing query: " + statementString, e);
 		} catch (ClassNotFoundException e) {
 			logger.error("Datasource driver not found.", e);
 			throw new CRException(e);
 		} finally {
 			try {
 				if (rset != null)
 					rset.close();
 			} catch (SQLException e) {
 			}
 			try {
 				if (stmt != null)
 					stmt.close();
 			} catch (SQLException e) {
 			}
 			try {
 				if (conn != null)
 					conn.close();
 			} catch (SQLException e) {
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Get the names of all columns in the result set.
 	 * @param rset {@link ResultSet} to get the columns for.
 	 * @return array of strings with the column names. array with length 0 in
 	 * case of an error.
 	 */
 	private String[] getColumnNamesFromResultSet(final ResultSet rset) {
 		int numcols;
 		String[] colnames;
 		try {
 			numcols = rset.getMetaData().getColumnCount();
 			colnames = new String[numcols];
 			for (int i = 1; i <= numcols; i++) {
 				colnames[i - 1] = rset.getMetaData().getColumnLabel(i);
 			}
 		} catch (SQLException e) {
 			logger.error("Error getting metadata from result.", e);
 			return new String[] {};
 		}
 		return colnames;
 	}
 
 	/**
 	 * Get merged objects as {@link CRResolvableBean}s from ResultSet. If we got more
 	 * than one row from.
 	 * @param rset {@link ResultSet} to get the objects from.
 	 * @return list of {@link CRResolvableBean}s, <code>null</code> in case of
 	 * an error.
 	 */
 	private ArrayList<CRResolvableBean> getMergedObjectsFromResultSet(final ResultSet rset) {
 		ArrayList<CRResolvableBean> objects = new ArrayList<CRResolvableBean>();
 		String[] colnames = getColumnNamesFromResultSet(rset);
 		try {
 			while (rset.next()) {
 				CRResolvableBean bean = new ComparableBean();
 				int idcolumnId = Arrays.asList(colnames).indexOf(idcolumn);
 				if (idcolumnId != -1) {
 					String id = rset.getObject(idcolumnId + 1).toString();
 					bean.setContentid(id);
 					int indexOfBeanInObjects = objects.indexOf(bean);
 					if (indexOfBeanInObjects != -1) {
 						bean = objects.get(indexOfBeanInObjects);
 					} else {
 						objects.add(bean);
 					}
 				}
 				for (int i = 1; i <= colnames.length; i++) {
 					String attributeName = colnames[i - 1];
 					if (rset.getObject(i) != null) {
 						Object attributeValue = rset.getObject(i);
 						Object oldattributeValue = bean.get(attributeName);
 						if (oldattributeValue != null && !oldattributeValue.equals(attributeValue)) {
 							List<Object> values;
 							if (oldattributeValue instanceof List) {
 								values = Lists.toSpecialList(oldattributeValue, Object.class);
 
 							} else {
 								values = new Vector<Object>();
 								values.add(oldattributeValue);
 							}
 							values.add(attributeValue);
 							attributeValue = values;
 						}
 						bean.set(attributeName, attributeValue);
 					}
 				}
 			}
 		} catch (SQLException e) {
 			logger.error("Error getting result items.", e);
 			return null;
 		}
 		return objects;
 	}
 
 	/**
 	 * Get Objects as resolvables from ResultSet.
 	 * @param rset {@link ResultSet} to get the objects from.
 	 * @return list of {@link CRResolvableBean}s, <code>null</code> in case of
 	 * an error.
 	 */
 	private ArrayList<CRResolvableBean> getObjectsFromResultSet(final ResultSet rset) {
 		ArrayList<CRResolvableBean> objects = new ArrayList<CRResolvableBean>();
 		String[] colnames = getColumnNamesFromResultSet(rset);
 
 		try {
 			while (rset.next()) {
 				CRResolvableBean bean = new CRResolvableBean();
 				for (int i = 1; i <= colnames.length; i++) {
 					String colname = colnames[i - 1];
					if(colname != null && colname.equalsIgnoreCase(idcolumn)) {
						bean.setContentid((String)rset.getObject(i));
					}
 					if (rset.getObject(i) != null) {
 						bean.set(colname, rset.getObject(i));
 					}
 				}
 				objects.add(bean);
 			}
 		} catch (SQLException e) {
 			logger.error("Error getting result items.", e);
 			return null;
 		}
 		return objects;
 	}
 
 	@Override
 	public void finalize() {
 		// TODO Auto-generated method stub
 	}
 }
