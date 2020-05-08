 /**
  * package controller in MABIS
  * by kornicameister
  */
 package controller;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.TreeSet;
 import java.util.logging.Level;
 
 import logger.MabisLogger;
 import model.BaseTable;
 import database.MySQLAccess;
 
 /**
  * Abstract class defining only common methods that allows to build valid SQL
  * statement. Nevertheless, <b>notice</b> that this is the job of inheriting
  * Classes to build the query which will be valid in the context of certain
  * table
  * 
  * @author kornicameister
  * 
  */
 public abstract class SQLFactory implements StatementFactory {
 	protected SQLStamentType type;
 	protected String sqlQuery = null;
 	private TreeSet<WhereClause> wheres = null;
 	protected String questionMarkFieldList = null;
 	protected BaseTable table = null;
 	protected boolean localDatabase = false;
 	protected Integer lastAffactedId = 0;
 
 	/**
 	 * Constructs a SQL factory
 	 * 
 	 * @param type
 	 */
 	public SQLFactory(SQLStamentType type, BaseTable table) {
 		this.table = table;
 		this.wheres = new TreeSet<SQLFactory.WhereClause>();
 		this.setStatementType(type);
 	}
 
 	@Override
 	public void setStatementType(SQLStamentType type) {
 		this.type = type;
 	}
 
 	@Override
 	public void addWhereClause(String attribute, String value) {
 		if (this.type == null || this.type.equals(SQLStamentType.INSERT)) {
 			return;
 		}
 		this.wheres.add(new WhereClause(attribute, value));
 	}
 
 	protected String createSQL() {
 		String rawQueryCopy = null;
 		switch (this.type) {
 		case INSERT:
 			rawQueryCopy = StatementFactory.insertPattern;
 			break;
 		case DELETE:
 			rawQueryCopy = StatementFactory.deletePattern;
 			break;
 		case UPDATE:
 			rawQueryCopy = StatementFactory.updatePattern;
 			break;
 		case SELECT:
 			rawQueryCopy = StatementFactory.selectPattern;
 			break;
 		default:
 			break;
 		}
		String fieldList = null;
 		rawQueryCopy = rawQueryCopy.replaceFirst("!", this.table.getTableType()
 				.toString());
		fieldList = this.buildFieldList(this.table.metaData());
 		// first pass
 		switch (this.type) {
 		case INSERT:
			rawQueryCopy = rawQueryCopy.replaceFirst("!", fieldList);
 			rawQueryCopy = rawQueryCopy.replaceFirst("!",
 					this.questionMarkFieldList);
 			break;
 		case UPDATE:
			rawQueryCopy = rawQueryCopy.replaceFirst("!", fieldList);
 			rawQueryCopy = rawQueryCopy.replaceAll(",", " = ?, ");
 			break;
 		default:
 			break;
 		}
 		// second pass
 		switch (this.type) {
 		case UPDATE:
 		case DELETE:
 			rawQueryCopy = rawQueryCopy.replaceFirst("!",
 					this.buildWhereChunk());
 			break;
 		case SELECT:
 			String where = this.buildWhereChunk();
 			if (where.equals("")) {
 				rawQueryCopy = rawQueryCopy.substring(0,
 						rawQueryCopy.lastIndexOf("where") - 1);
 			} else {
 				rawQueryCopy = rawQueryCopy.replaceAll("!", where);
 			}
 			break;
 		default:
 			break;
 		}
 
 		return rawQueryCopy;
 	}
 
 	@Override
 	public Integer executeSQL(boolean local) throws SQLException {
 		this.localDatabase = local;
 		try {
 			if (!MySQLAccess.getConnection().isValid(1000)) {
 				MabisLogger.getLogger().log(Level.SEVERE,
 						"Database connection lost");
 				return -1;
 			}
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 
 		PreparedStatement st = null;
 		MySQLAccess msql = null;
 		this.sqlQuery = this.createSQL();
 
 		if (localDatabase) {
 			st = MySQLAccess.getConnection().prepareStatement(this.sqlQuery,
 					Statement.RETURN_GENERATED_KEYS);
 		} else {
 			msql = new MySQLAccess();
 			st = msql.connectToOnlineDatabase().prepareStatement(this.sqlQuery,
 					Statement.RETURN_GENERATED_KEYS);
 		}
 
 		this.executeByTableAndType(st);
 
 		st.close();
 		st = null;
 
 		if (msql != null) {
 			msql.disconnectFromOnlineDatabase();
 			msql = null;
 		}
 
 		System.gc();
 		return lastAffactedId;
 	}
 
 	/**
 	 * This method is always called by extended class. This is caused by further
 	 * inconsistency in executing sql queries, every class maps itself to
 	 * another tables that consist different attributes, therefore
 	 * {@link SQLFactory#executeByTableAndType(PreparedStatement)} method is
 	 * declared abstract to allow other factories define it's own table depended
 	 * statements processing
 	 * 
 	 * <b>Declared package, because it is for internal usage only of the factory
 	 * classes </b>
 	 * 
 	 * @param st
 	 *            {@link PreparedStatement} object that internal sql query is
 	 *            already defined and awaits to have it's values added and/or
 	 *            being executed
 	 * @throws SQLException
 	 *             if anything goes wrong, exception is thrown and caught in
 	 *             propagation way by upper calling method
 	 *             {@link SQLFactory#executeSQL(boolean)}
 	 */
 	protected abstract void executeByTableAndType(PreparedStatement st)
 			throws SQLException;
 
 	/**
 	 * This method is always called by the extended class </br> It exists to
 	 * parse data coming from database and stored in <i>set</i>
 	 * 
 	 * <b>Declared package, because it is for internal usage only of the factory
 	 * classes </b>
 	 * 
 	 * @param set
 	 *            content of sql statement
 	 * @throws SQLException
 	 */
 	protected abstract void parseResultSet(ResultSet set) throws SQLException;
 
 	/**
 	 * Metoda zapewnia odbiera ilość usuniętych krotek z tabeli identyfikowanej
 	 * przez {@link SQLFactory#table} i zapewnia zapisanie nowej pozycji w
 	 * logach aplikacji
 	 * 
 	 * @param executeUpdate
 	 *            ilość usuniętych krotek
 	 */
 	protected void parseDeleteSet(int executeUpdate) {
 		if (executeUpdate > 0) {
 			Object params[] = { executeUpdate, this.table.getTableType() };
 			MabisLogger.getLogger().log(Level.INFO,
 					"Successfully deleted {0} rows from {1}", params);
 		} else {
 			MabisLogger.getLogger()
 					.log(Level.SEVERE, "Failed to delete rows from {0}",
 							this.table.getTableType());
 			// TODO dodać wyjątek
 		}
 	}
 
 	@Override
 	public String buildWhereChunk() {
 		String a = new String();
 		for (WhereClause where : this.wheres) {
			a += where.attribute + "=" + where.value + ",";
 		}
 		if (a.length() == 0) {
 			return "";
 		}
 		return a.substring(0, a.length() - 1);
 	}
 
 	@Override
 	public String buildFieldList(String[] fieldList) {
 		questionMarkFieldList = "";
 
 		String fieldList2 = new String();
 		for (short i = (short) (fieldList.length - 1); i != 0; i--) {
 			fieldList2 += fieldList[i] + ",";
 			questionMarkFieldList += "?" + ",";
 		}
 		questionMarkFieldList = questionMarkFieldList.substring(0,
 				questionMarkFieldList.lastIndexOf(","));
 		return fieldList2.substring(0, fieldList2.lastIndexOf(","));
 	}
 
 	/**
 	 * Metoda ustawia referencję do tabeli {@link SQLFactory#table} na null. W
 	 * dalszym ciągu blokuje to możliwość wykonywania operacji bazodanowych.
 	 */
 	public void reset() {
 		this.table = null;
 		this.wheres.clear();
 	}
 
 	/**
 	 * Ustawię tabelę, która następnie używana jest w operacjach bazodanowych
 	 * 
 	 * @param table
 	 */
 	public void setTable(BaseTable table) {
 		this.table = table;
 	}
 
 	/**
 	 * Private nested class of {@link SQLFactory} encapsulating
 	 * WhereClause.</br> Class consists two publicly visible fields. Using
 	 * getter schema in private nested class is negligble.
 	 * 
 	 * @author kornicameister
 	 * 
 	 */
 	private class WhereClause implements Comparable<WhereClause> {
 		String attribute = null;
 		String value = null;
 
 		/**
 		 * Constructs where clause with given value of attribute
 		 * 
 		 * @param attribute
 		 * @param value
 		 */
 		public WhereClause(String attribute, String value) {
 			this.attribute = attribute;
 			this.value = value;
 		}
 
 		@Override
 		public int compareTo(WhereClause o) {
 			return this.attribute.compareTo(o.attribute);
 		}
 	}
 }
