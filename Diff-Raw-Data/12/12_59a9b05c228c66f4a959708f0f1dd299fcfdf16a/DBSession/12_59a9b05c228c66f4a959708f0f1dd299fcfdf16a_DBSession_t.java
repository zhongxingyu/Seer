 /*
  * Copyright (C) 2004-2005 Cluestream Ventures, LLC
  * Copyright (C) 2006-2009 Solertium Corporation
  *
  * This file is part of the open source GoGoEgo project.
  *
  * Unless you have been granted a different license in writing by the
  * copyright holders for GoGoEgo, you may only modify or redistribute
  * this code under the terms of one of the following licenses:
  * 
  * 1) The Eclipse Public License, v.1.0
  *    http://www.eclipse.org/legal/epl-v10.html
  *
  * 2) The GNU General Public License, version 2 or later
  *    http://www.gnu.org/licenses
  */
 
 package com.solertium.db;
 
 import java.security.AccessController;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.solertium.db.query.Query;
 import com.solertium.db.query.SelectQuery;
 import com.solertium.util.ElementCollection;
 import com.solertium.util.Replacer;
 import com.solertium.util.TrivialExceptionHandler;
 
 /**
  * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
  *         href="http://www.solertium.com">Solertium Corporation</a>
  */
 @ThreadSafe
 public abstract class DBSession {
 
 	class DoQueryAction implements PrivilegedExceptionAction<Object> {
 		private ExecutionContext ec;
 		private DBProcessor processor;
 		private String sql;
 
 		DoQueryAction(final String sql, final DBProcessor processor, final ExecutionContext ec) {
 			this.sql = sql;
 			this.processor = processor;
 			this.ec = ec;
 		}
 
 		public Object run() throws DBException {
 			// ec.debug("query: ["+sql+"]");
 			Connection conn = null;
 			Statement stmt = null;
 			ResultSet rs = null;
 			try {
 				conn = getDataSource().getConnection();
 				stmt = conn.createStatement();
 				rs = stmt.executeQuery(sql);
 				if (processor != null)
 					processor.process(rs, ec);
 			} catch (final Exception e) {
 				sql = null;
 				processor = null;
 				ec = null;
 				throw new DBException(e);
 			} finally {
 				try {
 					rs.close();
 				} catch (final Exception ignored) {
 				}
 				try {
 					stmt.close();
 				} catch (final Exception ignored) {
 				}
 				try {
 					conn.close();
 				} catch (final Exception ignored) {
 				}
 			}
 			sql = null;
 			processor = null;
 			ec = null;
 			return null;
 		}
 	}
 
 	/**
 	 * @deprecated Please use DBSessionFactory.getDBSession
 	 */
 	@Deprecated
 	public static DBSession get(final String name) throws NamingException {
 		return DBSessionFactory.getDBSession(name);
 	}
 
 	/**
 	 * @deprecated Please use DBSessionFactory.getDSType
 	 */
 	@Deprecated
 	public static String getDSType(final DataSource ds) {
 		return DBSessionFactory.getDSType(ds);
 	}
 
 	/**
 	 * @deprecated Please use DBSessionFactory.registerDataSource
 	 */
 	@Deprecated
 	public static void register_datasource(final String name, final DataSource ds) {
 		DBSessionFactory.registerDataSource(name, ds);
 	}
 
 	/**
 	 * @deprecated Please use DBSessionFactory.registerDataSource
 	 */
 	@Deprecated
 	public static void register_datasource(final String name, final String url, final String driverClass,
 			final String username, final String password) throws NamingException {
 		DBSessionFactory.registerDataSource(name, url, driverClass, username, password);
 	}
 
 	/**
 	 * @deprecated Please use DBSessionFactory.unregisterDataSource
 	 */
 	@Deprecated
 	public static void unregister_datasource(final String name) {
 		DBSessionFactory.unregisterDataSource(name);
 	}
 
 	public static final int CASE_SENSITIVE = 0;
 	public static final int CASE_UPPER = 1;
 	public static final int CASE_LOWER = -1;
 	public static final int CASE_UNCHECKED = -9;
 
 	private ConcurrentHashMap<String, Integer> definitions = new ConcurrentHashMap<String, Integer>();
 
 	public boolean hasDefinitionMinimum(String identifier, Integer version) {
 		Integer i = definitions.get(identifier);
 		if (i == null)
 			return false;
 		if (i < version)
 			return false;
 		return true;
 	}
 
 	public boolean hasDefinitionExact(String identifier, Integer version) {
 		Integer i = definitions.get(identifier);
 		if (i == null)
 			return false;
 		if (i != version)
 			return false;
 		return true;
 	}
 
 	private int identifierCase = CASE_SENSITIVE;
 
 	public int getIdentifierCase() {
 		return identifierCase;
 	}
 
 	public void setIdentifierCase(int identifierCase) {
 		this.identifierCase = identifierCase;
 	}
 	
 	private String schema;
 	
 	public String getSchema() {
 		return schema;
 	}
 	
 	public void setSchema(String schema) {
 		this.schema = schema;
 	}
 	
 	private String[] tableTypes = null;
 	
 	public String[] getAllowedTableTypes() {
 		return tableTypes;
 	}
 	
 	/**
 	 * Set the list of allowed table types when listing 
 	 * what tables exist in the database.  Defaults to 
 	 * { "TABLE" }.  Useful for finding views.
 	 * 
 	 * @param tableTypes
 	 */
 	public void setAllowedTableTypes(String... tableTypes) {
 		this.tableTypes = tableTypes;
 	}
 
 	private ConcurrentHashMap<String, Row> structure = null;
 	private ConcurrentHashMap<String, String> descriptions = null;
 	private ConcurrentHashMap<String, Map<String, String>> hints = null;
 
 	protected void _createTable(final String table, final Row prototype, final ExecutionContext ec) throws DBException {
 		if (ec.getExecutionLevel() < ExecutionContext.ADMIN)
 			throw new IllegalExecutionLevelException(
 					"The execution context must be elevated to ADMIN level to create or delete tables.");
 		_doUpdate("CREATE TABLE " + formatIdentifier(table) + " (" + formatCreateSpecifier(prototype) + ")", ec);
 		createIndices(table, prototype, ec);
 	}
 
 	protected void _doQuery(final String sql, final DBProcessor processor, final ExecutionContext ec)
 			throws DBException {
 		final DoQueryAction dqa = new DoQueryAction(sql, processor, ec);
 		if (ec.getDBSession() == null)
 			ec.setDBSession(this); // fix a programming oversight
 		else if (ec.getDBSession() != this) // somebody is trying to break the
 			// rules
 			throw new UnboundSessionException(
 					"ExecutionContext is connected to a different database; API error is likely");
 		try {
 			AccessController.doPrivileged(dqa);
 		} catch (final PrivilegedActionException pox) {
 			throw new DBException(pox);
 		}
 	}
 
 	protected void _doUpdate(final String sql, final ExecutionContext ec) throws DBException {
 		if (ec.getDBSession() == null)
 			ec.setDBSession(this); // fix a programming oversight
 		else if (ec.getDBSession() != this) // somebody is trying to break the
 			// rules
 			throw new UnboundSessionException(
 					"ExecutionContext is connected to a different database; API error is likely");
 		if (ec.getExecutionLevel() < ExecutionContext.READ_WRITE)
 			throw new IllegalExecutionLevelException(
 					"The execution context must be elevated to READ_WRITE level to update the database.");
 		// ec.debug("update: ["+sql+"]");
 		Connection conn = null;
 		Statement stmt = null;
 		try {
 			conn = getDataSource().getConnection();
 			stmt = conn.createStatement();
 			stmt.executeUpdate(sql);
 		} catch (final Exception e) {
 			// allow proper upstream handling of "real" errors
 			throw new DBException(e);
 		} finally {
 			try {
 				stmt.close();
 				conn.close();
 			} catch (final Exception e) {
 				// this is ok, we're just cleaning up here
 			}
 		}
 	}
 
 	public void doTesting(String tableToTest) {
 		System.out.println("_------------------ in do testing");
 		try {
 			Connection conn = getDataSource().getConnection();
 			final DatabaseMetaData md = conn.getMetaData();
 			ResultSet rs = md.getColumns(conn.getCatalog(), null, tableToTest, "%");
 
 			while (rs.next()) {
 				ResultSetMetaData rsmd = rs.getMetaData();
 				for (int i = 0; i < rsmd.getColumnCount(); i++) {
 					System.out.print(rsmd.getColumnName(i + 1));
 					System.out.println(" = " + rs.getString(i + 1));
 				}
 				System.out.println("-----------------\r\n");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected Row _listColumns(final Connection conn, final String tableName, final ExecutionContext ec,
 			final boolean captureData) throws UnsupportedOperationException {
 		throw new UnsupportedOperationException("Unsupported operation.");
 	}
 
 	protected List<String> _listTables(final ExecutionContext ec) throws DBException {
 		Connection conn = null;
 		ResultSet rs = null;
 		final List<String> s = new ArrayList<String>();
 		try {
 			conn = getDataSource().getConnection();
 			final DatabaseMetaData metaData = conn.getMetaData();
 			
 			String[] types = getAllowedTableTypes();
 			if (types == null)
 				types = new String[] { "TABLE" };
 			
 			rs = metaData.getTables(conn.getCatalog(), getSchema(), "%", types);
 			while (rs.next())
 				s.add(rs.getString("TABLE_NAME"));
 		} catch (final Exception e) {
 			throw new DBException(e);
 		} finally {
 			try {
 				rs.close();
 			} catch (final Exception ignored) {
 			}
 			try {
 				conn.close();
 			} catch (final Exception ignored) {
 			}
 		}
 		return s;
 	}
 
 	class RowFetchProcessor implements DBProcessor {
 		private Row myRow;
 
 		public void process(ResultSet rs, ExecutionContext ec) throws Exception {
 			if (!rs.isBeforeFirst())
 				return;
 
 			rs.next();
 			myRow = ec.getDBSession().rsToRow(rs, rs.getMetaData());
 		}
 	}
 
 	private Row getTableColumnAnalysisRow(final Connection conn, final String tableName, final ExecutionContext ec,
 			final boolean captureData) throws DBException, SQLException {
 		try {
 			return (conn != null) ? _listColumns(conn, tableName, ec, false) : getRowViaSelectStar(tableName, ec);
 		} catch (UnsupportedOperationException e) {
 			ec.error("List Columns is not supported, attempting a select *");
 			return getRowViaSelectStar(tableName, ec);
 		}
 	}
 
 	private final Row getRowViaSelectStar(final String tableName, final ExecutionContext ec) throws DBException {
 		final SelectQuery sq = new SelectQuery();
 		sq.select(new CanonicalColumnName(tableName, "*"));
 		final RowFetchProcessor p = new RowFetchProcessor();
 		doQuery(sq, p, ec);
 		return p.myRow;
 	}
 
 	/*
 	 * final SelectQuery sq = new SelectQuery(); sq.select(new
 	 * CanonicalColumnName(table, "*")); doQuery(sq, new DBProcessor() { public
 	 * void process(final ResultSet rs, final ExecutionContext ec) { try { if
 	 * (!rs.isBeforeFirst()) return; rs.next(); final Row r =
 	 * ec.getDBSession().rsToRow(rs, rs.getMetaData()); for (final Column c :
 	 * r.getColumns()) { final Element colEl = (Element)
 	 * tbe.appendChild(doc.createElement("column")); colEl.setAttribute("type",
 	 * c.getClass().getSimpleName()); if (c.getScale() > 0)
 	 * colEl.setAttribute("scale", "" + c.getScale());
 	 * colEl.setAttribute("name", "" + c.getLocalName()); if (c.isIndex())
 	 * colEl.setAttribute("index", "true"); if (c.isKey())
 	 * colEl.setAttribute("key", "true");
 	 * 
 	 * try { Iterator<String> iter = c.getArbitraryData().keySet().iterator();
 	 * while (iter.hasNext()) { String name = iter.next();
 	 * colEl.setAttribute(name, c.getArbitraryData().get(name)); } } catch
 	 * (Exception e) { //I don't want stupid arbitrary data to stop me from
 	 * getting //a column back! TrivialExceptionHandler.ignore(this, e); } } }
 	 * catch (final SQLException exception) { ec.error("Table " + table + " not
 	 * included due to exception", exception); } } }, ec);
 	 * 
 	 */
 
 	public Document analyzeExistingStructure(final ExecutionContext ec) throws DBException {
 		try {
 			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 			final Element rootEl = (Element) doc.appendChild(doc.createElement("structure"));
 
 			Connection conn;
 			try {
 				conn = getDataSource().getConnection();
 			} catch (SQLException e) {
 				conn = null;
 			}
 
 			for (final String table : _listTables(ec)) {
 				final Element tbe = (Element) rootEl.appendChild(doc.createElement("table"));
 				tbe.setAttribute("name", table);
 
 				final Row r;
 				try {
 					r = getTableColumnAnalysisRow(conn, table, ec, false);
 				} catch (final DBException exception) {
 					// exception.printStackTrace();
 					ec.error("Table " + table + " not included due to exception", exception);
 					continue;
 				} catch (final SQLException e) {
 					// e.printStackTrace();
 					ec.error("Table " + table + " not included due to exception", e);
 					continue;
 				}
 				if (r == null) {
 					ec.error("Table " + table + " not included due to unknown exception.");
 					continue;
 				}
 
 				for (final Column c : r.getColumns()) {
 					final Element colEl = (Element) tbe.appendChild(doc.createElement("column"));
 					colEl.setAttribute("type", c.getClass().getSimpleName());
 					if (c.getScale() > 0)
 						colEl.setAttribute("scale", "" + c.getScale());
 					colEl.setAttribute("name", "" + c.getLocalName());
 					if (c.isIndex())
 						colEl.setAttribute("index", "true");
 					if (c.isKey())
 						colEl.setAttribute("key", "true");
 
 					try {
 						Iterator<String> iter = c.getArbitraryData().keySet().iterator();
 						while (iter.hasNext()) {
 							String name = iter.next();
 							colEl.setAttribute(name, c.getArbitraryData().get(name));
 						}
 					} catch (Exception e) {
 						// I don't want stupid arbitrary data to stop me from
 						// getting
 						// a column back!
 						TrivialExceptionHandler.ignore(this, e);
 					}
 				}
 
 				if (descriptions != null) {
 					String desc = descriptions.get(table);
 					if (desc != null) {
 						Element descEl = doc.createElement("description");
 						descEl.appendChild(doc.createCDATASection(desc));
 						tbe.appendChild(descEl);
 					}
 				}
 
 			}
 			return doc;
 		} catch (final ParserConfigurationException noParser) {
 			throw new RuntimeException("XML Parser improperly configured", noParser);
 		}
 	}
 
 	public void appendStructure(final Document structureDoc, final ExecutionContext ec, final boolean create)
 			throws DBException {
 		initStructure(structureDoc, ec, create, true);
 	}
 
 	protected String assembleColumnSpecifier(final Column c, final String dbtype) {
 		return formatIdentifier(c.getLocalName()) + " " + dbtype + getKeySpec(c);
 	}
 
 	protected void createIndex(final String table, final Column c, final ExecutionContext ec) {
 		try {
 			_doUpdate("CREATE INDEX " + formatIdentifier("idx_" + c.getLocalName()) + " ON " + formatIdentifier(table)
 					+ " (" + formatIdentifier(c.getLocalName()) + ")", ec);
 		} catch (final Exception ignored) {
 			ec.error("Create index failed", ignored);
 		}
 	}
 
 	protected void createIndices(final String table, final Row prototype, final ExecutionContext ec) {
 		for (int i = 0; i < prototype.size(); i++) {
 			final Column c = prototype.get(i);
 			if (c.isIndex())
 				createIndex(table, c, ec);
 		}
 	}
 
 	public void createStructure(final Document structureDoc, final ExecutionContext ec) throws DBException {
 		initStructure(structureDoc, ec, true, false);
 	}
 
 	void createTable(final Element e, final ExecutionContext ec) throws DBException {
 		final String table = e.getAttribute("name");
 		createTable(table, getRowFromElement(e), ec);
 	}
 
 	void createTable(final String table, final Row prototype, final ExecutionContext ec) throws DBException {
 		_createTable(table, prototype, ec);
 	}
 
 	public String defaultDBColumnType() {
 		return "nvarchar(255)"; // a sensible default for most databases and
 		// datatypes
 	}
 
 	void doQuery(final Query q, final DBProcessor processor, final ExecutionContext ec) throws DBException {
 		if (ec.getDBSession() == null)
 			ec.setDBSession(this); // fix a programming oversight
 		else if (ec.getDBSession() != this) // somebody is trying to break the
 			// rules
 			throw new UnboundSessionException(
 					"ExecutionContext is connected to a different database; API error is likely");
 		_doQuery(q.getSQL(this), processor, ec);
 	}
 
 	void doQuery(final String sql, final DBProcessor processor, final ExecutionContext ec) throws DBException {
 		if (ec.getAPILevel() < ExecutionContext.SQL_ALLOWED)
 			throw new IllegalAPILevelException("Raw SQL is not allowed in this execution context.");
 		_doQuery(sql, processor, ec);
 	}
 
 	void doUpdate(final Query q, final ExecutionContext ec) throws DBException {
 		if (ec.getDBSession() == null)
 			ec.setDBSession(this); // fix a programming oversight
 		else if (ec.getDBSession() != this) // somebody is trying to break the
 			// rules
 			throw new UnboundSessionException(
 					"ExecutionContext is connected to a different database; API error is likely");
 		_doUpdate(q.getSQL(this), ec);
 	}
 
 	void doUpdate(final String sql, final ExecutionContext ec) throws DBException {
 		if (ec.getAPILevel() < ExecutionContext.SQL_ALLOWED)
 			throw new IllegalAPILevelException("Raw SQL is not allowed in this execution context.");
 		_doUpdate(sql, ec);
 	}
 
 	void dropTable(final String table, final ExecutionContext ec) throws DBException {
 		if (ec.getExecutionLevel() < ExecutionContext.ADMIN)
 			throw new IllegalExecutionLevelException(
 					"The execution context must be elevated to ADMIN level to create or delete tables.");
 		_doUpdate("DROP TABLE " + formatIdentifier(table), ec);
 	}
 
 	public String formatCanonicalColumnName(final CanonicalColumnName name) {
 		final StringBuffer ret = new StringBuffer(128);
 		final String t = name.getTable();
 		if (t != null) {
 			ret.append(formatIdentifier(t));
 			ret.append(".");
 		}
 		final String f = name.getField();
 		if ("*".equals(f))
 			ret.append("*");
 		else
 			ret.append(formatIdentifier(f));
 		return ret.toString();
 	}
 
 	public String formatColumnSpecifier(final Column c) {
 		String dbtype = null;
 		if (c instanceof CBoolean)
 			dbtype = getDBColumnType((CBoolean) c);
 		else if (c instanceof CDate)
 			dbtype = getDBColumnType((CDate) c);
 		else if (c instanceof CDateTime)
 			dbtype = getDBColumnType((CDateTime) c);
 		else if (c instanceof CDouble)
 			dbtype = getDBColumnType((CDouble) c);
 		else if (c instanceof CInteger)
 			dbtype = getDBColumnType((CInteger) c);
 		else if (c instanceof CLong)
 			dbtype = getDBColumnType((CLong) c);
 		else if (c instanceof CString)
 			dbtype = getDBColumnType((CString) c);
 		else
 			dbtype = defaultDBColumnType();
 		return assembleColumnSpecifier(c, dbtype);
 	}
 
 	protected String formatCreateSpecifier(final Row prototype) {
 		final StringBuffer sb = new StringBuffer(1024);
 		for (int i = 0; i < prototype.size(); i++) {
 			sb.append(formatColumnSpecifier(prototype.get(i)));
 			if (i < prototype.size() - 1)
 				sb.append(", ");
 		}
 		return sb.toString();
 	}
 
 	public String formatIdentifier(final String identifier) {
 		return "`" + identifier + "`";
 	}
 	
 	public String formatIdentifier(final String identifier, final boolean isColumn) {
 		return formatIdentifier(identifier);
 	}
 
 	public String formatLiteral(final Literal literal) {
 		if (literal instanceof StringLiteral)
 			return formatLiteral((StringLiteral) literal);
		else if (literal instanceof BooleanLiteral)
			return formatLiteral((BooleanLiteral) literal);
 		else
 			return formatLiteral((NumericLiteral) literal);
 	}
 
 	public String formatLiteral(final NumericLiteral literal) {
 		if (literal == null)
 			return "NULL";
 		final Number n = literal.getNumber();
 		if (n == null)
 			return "NULL";
 		return n.toString();
 	}
 
 	public String formatLiteral(final StringLiteral literal) {
 		if (literal == null)
 			return "NULL";
 		String s = literal.getString();
 		if (s == null)
 			return "NULL";
 		s = Replacer.replace(s, "'", "''");
 		return "'" + s + "'";
 	}
	
	public String formatLiteral(final BooleanLiteral literal) {
		if (literal == null)
			return "NULL";
		final Boolean b = literal.getBoolean();
		if (b == null)
			return "NULL";
		return b.toString();
	}
 
 	public String fnLowerCase(String in) {
 		return ("LOWER(" + in + ")");
 	}
 
 	public String fnUpperCase(String in) {
 		return ("UPPER(" + in + ")");
 	}
 
 	protected abstract DataSource getDataSource();
 
 	protected abstract String getDBColumnType(CBoolean c);
 
 	protected abstract String getDBColumnType(CDate c);
 
 	protected abstract String getDBColumnType(CDateTime c);
 
 	protected abstract String getDBColumnType(CDouble c);
 
 	protected abstract String getDBColumnType(CInteger c);
 
 	protected abstract String getDBColumnType(CLong c);
 
 	protected abstract String getDBColumnType(CString c);
 
 	@Deprecated
 	public String getDSType() {
 		return DBSession.getDSType(getDataSource());
 	}
 
 	protected String getKeySpec(final Column c) {
 		if (c.isKey())
 			return " PRIMARY KEY";
 		return "";
 	}
 
 	/**
 	 * Retrieves a list of tables that match the given table name. You should
 	 * call this method and get a table to use for a getRow() call to be ensured
 	 * that the table you supply is a valid table
 	 * 
 	 * @param tableName
 	 *            the table name to match
 	 * @return a list of tables
 	 */
 	public ArrayList<String> getMatchingTables(final ExecutionContext ec, final String tableName) throws DBException {
 		final ArrayList<String> list = new ArrayList<String>();
 		for (final String cur : listTables(ec))
 			if (cur.equalsIgnoreCase(tableName))
 				list.add(cur);
 		return list;
 	}
 
 	public Row getRow(final String tableName, final ExecutionContext ec) throws DBException {
 		if (structure == null)
 			setStructure(analyzeExistingStructure(ec), ec);
 		
 		Row template = structure.get(tableName);
 		if (template == null)
 			throw new DBException("The table " + tableName + " was not found.");
 		
 		return new Row(template);
 	}
 
 	private Row getRowFromElement(final Element e) throws DBException {
 		try {
 			final Row row = new Row();
 			final NodeList columns = e.getElementsByTagName("column");
 			final ArrayList<String> used = new ArrayList<String>();
 			used.add("type");
 			used.add("name");
 			used.add("scale");
 			used.add("key");
 			used.add("transient");
 			used.add("index");
 			used.add("relatedTable");
 			used.add("relatedColumn");
 			for (int i = 0; i < columns.getLength(); i++) {
 				final Element colEl = (Element) columns.item(i);
 				final String typespec = "com.solertium.db." + colEl.getAttribute("type");
 				final Class<?> colClass = Class.forName(typespec);
 				final Column col = (Column) colClass.newInstance();
 				col.setLocalName(colEl.getAttribute("name"));
 				final String scale = colEl.getAttribute("scale");
 				if ((scale != null) && (!"".equals(scale)))
 					col.setScale(Integer.parseInt(scale));
 				final String key = colEl.getAttribute("key");
 				if ((key != null) && (!"".equals(key)))
 					if ("true".equals(key))
 						col.setKey(true);
 				final String trans = colEl.getAttribute("transient");
 				if ((trans != null) && (!"".equals(trans)))
 					if ("true".equals(trans))
 						col.setTransient(true);
 				final String index = colEl.getAttribute("index");
 				if ((index != null) && (!"".equals(index)))
 					if ("true".equals(index))
 						col.setIndex(true);
 				final String relatedTable = colEl.getAttribute("relatedTable");
 				final String relatedColumn = colEl.getAttribute("relatedColumn");
 				if ((relatedTable != null) && (relatedColumn != null) && (!"".equals(relatedTable))
 						&& (!"".equals(relatedColumn)))
 					col.setRelatedColumn(new CanonicalColumnName(relatedTable, relatedColumn));
 
 				NamedNodeMap attrs = colEl.getAttributes();
 				for (int k = 0; k < attrs.getLength(); k++) {
 					Node curAttr = attrs.item(k);
 					if (!used.contains(curAttr.getNodeName()))
 						col.addArbitraryData(curAttr.getNodeName(), curAttr.getNodeValue());
 				}
 
 				row.add(col);
 			}
 			return row;
 		} catch (final ClassNotFoundException cnf) {
 			throw new DBException(cnf);
 		} catch (final InstantiationException ins) {
 			throw new DBException(ins);
 		} catch (final IllegalAccessException acc) {
 			throw new DBException(acc);
 		}
 	}
 
 	private Map<String, String> getHintsFromElement(Element e) {
 		Map<String, String> map = new HashMap<String, String>();
 		final NodeList hints = e.getElementsByTagName("hint");
 		for (int i = 0; i < hints.getLength(); i++) {
 			final Element hintEl = (Element) hints.item(i);
 			String join = hintEl.getAttribute("join");
 			String via = hintEl.getAttribute("via");
 			if (join != null && via != null) {
 				map.put(join, via);
 			}
 		}
 		return map;
 	}
 
 	private String getDescriptionFromElement(Element e) {
 		String desc = null;
 		final NodeList nodes = e.getElementsByTagName("description");
 		for (int i = 0; i < nodes.getLength(); i++) {
 			final Node node = nodes.item(i);
 			if (node.getNodeName().equals("description")) {
 				desc = node.getTextContent();
 				break;
 			}
 		}
 		return desc;
 	}
 
 	public String getHint(String fromTable, String toTable) {
 		Map<String, String> hintMap = hints.get(fromTable);
 		if (hintMap == null)
 			return null;
 		return hintMap.get(toTable);
 	}
 
 	private void initStructure(final Document structureDoc, final ExecutionContext ec, final boolean create,
 			final boolean append) throws DBException {
 		Element docEl = structureDoc.getDocumentElement();
 		String id = docEl.getAttribute("id");
 		if (id != null) {
 			String version = docEl.getAttribute("version");
 			if (version != null && !"".equals(version)) {
 				try {
 					Integer ver = Integer.parseInt(version);
 					definitions.put(id, ver);
 				} catch (NumberFormatException nf) {
 					ec.debug("Illegal version number in data definition");
 				}
 			}
 		}
 		if (append) {
 			if (structure == null) {
 				structure = new ConcurrentHashMap<String, Row>();
 				descriptions = new ConcurrentHashMap<String, String>();
 				hints = new ConcurrentHashMap<String, Map<String, String>>();
 			}
 		} else {
 			structure = new ConcurrentHashMap<String, Row>();
 			descriptions = new ConcurrentHashMap<String, String>();
 			hints = new ConcurrentHashMap<String, Map<String, String>>();
 		}
 		final ElementCollection tables = new ElementCollection(structureDoc.getElementsByTagName("table"));
 		ec.debug("Database schema has " + tables.size() + " tables");
 		for (Element e : tables) {
 			final String table = e.getAttribute("name");
 			final Row row = getRowFromElement(e);
 			structure.put(table, row);
 			final Map<String, String> hintMap = getHintsFromElement(e);
 			hints.put(table, hintMap);
 			String desc = getDescriptionFromElement(e);
 			if (desc != null)
 				descriptions.put(table, desc);
 			if (create)
 				try {
 					createTable(table, row, ec);
 					ec.debug("Table structure " + e.getAttribute("name") + " created");
 				} catch (final DBException reportOnly) {
 					ec.debug("Could not create structure for " + table + " (already exists?)");
 				}
 		}
 	}
 
 	public List<String> listTables(final ExecutionContext ec) throws DBException {
 		if (structure == null)
 			setStructure(analyzeExistingStructure(ec), ec);
 		return new ArrayList<String>(structure.keySet());
 	}
 
 	public abstract Row rsToRow(ResultSet rs, ResultSetMetaData rsmd) throws SQLException;
 
 	public void setStructure(final Document structureDoc, final ExecutionContext ec) throws DBException {
 		initStructure(structureDoc, ec, false, false);
 	}
 
 }
