 package newsrack.database.sql;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import newsrack.database.DB_Interface;
 
 import org.apache.commons.logging.Log;
 
 import snaq.db.ConnectionPool;
 
 class GetStringResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws java.sql.SQLException { return rs.getString(1); }
 }
 
 class GetIntResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws java.sql.SQLException { return new Integer(rs.getInt(1)); }
 }
 
 class GetLongResultProcessor extends AbstractResultProcessor
 {
 	public Object processResultSet(ResultSet rs) throws java.sql.SQLException { return new Long(rs.getLong(1)); }
 }
 
 public class SQL_StmtExecutor
 {
    public static AbstractResultProcessor _longProcessor   = new GetLongResultProcessor();
    public static AbstractResultProcessor _intProcessor    = new GetIntResultProcessor();
    public static AbstractResultProcessor _stringProcessor = new GetStringResultProcessor();
 
            static Log            _log;
            static DB_Interface   _db;
    private static ConnectionPool _dbPool;
    private static boolean        _adminAlerted;
 	private static int            _stmtExecutionCount;
 	private static HashMap<String, Integer> _stmtStats;
 
    public static void init(ConnectionPool p, Log l, DB_Interface db)
    {
       _dbPool = p;
       _log    = l;
       _adminAlerted = false;
 		_db = db;
 		_stmtStats = new HashMap<String,Integer>();
    }
 
 	public static String getStats()
 	{
 		StringBuffer sb = new StringBuffer();
 		sb.append("Total SQL executions: ").append(_stmtExecutionCount).append("\n");
 		for (SQL_StmtType t: SQL_StmtType.values()) {
 			sb.append("Executions of SQL stmt type ").append(t.toString()).append(": ").append(t._executionCount).append("\n");
 		}
 		for (String s: _stmtStats.keySet()) {
 			sb.append("STMT: ").append(_stmtStats.get(s)).append(": ").append(s).append("\n");
 		}
 
 		return sb.toString();
 	}
 
 	static void closeConnection(Connection c)
 	{
 		try {
 			if (c != null) {
 				c.close();
 			}
 		}
 		catch (Exception e) {
 			_log.error("Exception closing SQL connection: " + e);
 			e.printStackTrace();
 		}
 	}
 
 	static void closeStatement(Statement s)
 	{
 		try {
 			if (s != null) {
 				s.close();
 			}
 		}
 		catch (Exception e) {
 			_log.error("Exception closing SQL statment: " + e);
 			e.printStackTrace();
 		}
 	}
 
 	static void closeResultSet(ResultSet rs)
 	{
 		try {
 			if (rs != null) {
 				rs.close();
 			}
 		}
 		catch (Exception e) {
 			_log.error("Exception closing result set: " + e);
 			e.printStackTrace();
 		}
 	}
 
 	private static Object processResults(ResultProcessor rp, Connection c, PreparedStatement stmt, ResultSet rs, boolean singleRowOutput) throws java.sql.SQLException
 	{
 			// Fetch a new instance for every query!
 			//
 			// Otherwise, we will have conflicts for those processors
 			// that use object state during result processing, and we don't
 			// want to synchronize on these objects!!
 		rp = rp.getNewInstance();
 
 		if (singleRowOutput) {
 			Object retVal = null;
 			if (rs.next()) {
 				retVal = rp.processResultSet(rs);
 					// Since this is supposed to be a single row result there cannot be multiple rows!
 				if (rs.next())
 					throw new SQL_UniquenessConstraintViolationException(retVal, "Multiple results found!");
 			}
 
 				// Close all db resources before doing further post-processing
 				// This prevents deadlock!  Otherwise, processOutput might stall
 				// waiting for db resources, while another thread might also have
 				// stalled waiting for db resources!
 			closeResultSet(rs);
 			closeStatement(stmt);
 			closeConnection(c);
 
 				// Do any necessary post-processing of the object
 			return rp.processOutput(retVal);
 		}
 		else {
 			List l = new ArrayList();
 			while (rs.next()) {
 				Object o = rp.processResultSet(rs);
 				if (o != null)
 					l.add(o);
 			}
 
 				// Close all db resources before doing further post-processing
 				// This prevents deadlock!
 			closeResultSet(rs);
 			closeStatement(stmt);
 			closeConnection(c);
 
 				// Do any necessary post-processing of the list
 			return rp.processOutputList(l);
 		}
 	}
 
    /**
     * This method executes a prepared sql statement using arguments passed in
     * and pushes the result set thorough a result processor, if any.
     *
     * @param stmtString  SQL stmt string
     * @param stmtType    Type of statement this is (SELECT, DELETE, INSERT, UPDATE)
     * @param args        Argument array for this sql statement
     * @param argTypes    Argument types for the arguments being passed in
 	 * 							FIXME: colSizes is not used right now ...
 	 * @param colSizes    Width of the column data -- used for checking whether the inserted data is too big to fit!
 	 * @param resultProcessor  How should the result set be processed?  The result processor is just a cumbersome way of passing in a closure since Java does not support closures yet.
 	 * @param singleRowOutput  Are we expecting a single row or multiple rows?
 	 *
 	 * @returns the result of executing the query, if any 
     */
    public static Object execute(String stmtString, SQL_StmtType stmtType, Object[] args, SQL_ValType[] argTypes, SQL_ColumnSize[] colSizes, ResultProcessor rp, boolean singleRowOutput)
    {
       int n = -1;
 		Connection c = null;
 		ResultSet rs = null;
 		PreparedStatement stmt = null;
 		Object retVal = null;
 
 		try {
          if (args.length != argTypes.length)
             throw new SQL_ArgumentMismatchException("Mismatch in number of arguments(" + args.length + ") and argument types(" + argTypes.length + ")");
 
 			c = _dbPool.getConnection();
			if (c == null)
 				_log.error("Got a null DB connection!");
 
 				// DBPool caches prepared statements, but not for stmts requiring auto-generated keys.
 				// So, carefully make the right call based on what we want
 			stmt = ((stmtType == SQL_StmtType.INSERT) && (rp != null)) ? c.prepareStatement(stmtString, Statement.RETURN_GENERATED_KEYS)
 			                                                           : c.prepareStatement(stmtString);
 				// Set args!
          for (int i = 0; i < args.length; i++)
             argTypes[i].setStmtArg(stmt, i+1, args[i]);
 
 				// Update stats!
 			_stmtExecutionCount++;
 			stmtType._executionCount++;
 			Integer count = _stmtStats.get(stmtString);
 			_stmtStats.put(stmtString, count == null ? 1 : count + 1);
 
 				// Do it now ...
          switch (stmtType) {
             case QUERY  :
                rs = stmt.executeQuery();
 					if (_log.isDebugEnabled()) _log.debug("Query statement " + stmt + " completed without exceptions!");
 					if (rp == null) {
 						retVal = null;
 					}
 					else {
 						retVal = processResults(rp, c, stmt, rs, singleRowOutput);
 							// processResultSet should have closed the connection, stmt, and the result set
 							// so, set them to null
 						c    = null;
 						stmt = null;
 						rs   = null;
 					}
 					return retVal;
 
             case INSERT :
                n = stmt.executeUpdate();
 					if (_log.isDebugEnabled()) _log.debug("Insert statement " + stmt + " completed without exceptions!");
 						// Don't complain if the stmt. is an insert ignore!
 					if ((n == 0) && (stmtString.toLowerCase().indexOf(" ignore ") == -1))
 						_log.error("Insert statement " + stmt + " returned 0 rows!");
 
 					if (rp == null) {
 						retVal = new Integer(n);
 					}
 					else {
 						rs = stmt.getGeneratedKeys();
 						retVal = processResults(rp, c, stmt, rs, singleRowOutput);
 							// processResultSet should have closed the connection, stmt, and the result set
 							// so, set them to null
 						c    = null;
 						stmt = null;
 						rs   = null;
 					}
 					return retVal;
 
             case UPDATE :
 					retVal = new Integer(stmt.executeUpdate());
 					if (_log.isDebugEnabled()) _log.debug("Update statement " + stmt + " completed without exceptions!");
 					return retVal;
 
             case DELETE :
 					retVal = new Integer(stmt.executeUpdate());
 					if (_log.isDebugEnabled()) _log.debug("Delete statement " + stmt + " completed without exceptions!");
 					return retVal;
 
 				default:
 					_log.error("Unknown statement type: " + stmtType + " for query: " + stmtString);
 					return null;
          }
 		}
       catch (SQL_ArgumentMismatchException e) {
 			_log.error("Exception while executing stmt " + stmt, e);
          throw e;
       }
       catch (SQL_ColumnSizeException e) {
 			_log.error("Exception while executing stmt " + stmt, e);
          throw e;
       }
       catch (SQL_UniquenessConstraintViolationException e) {
 			_log.error("Exception while executing stmt " + stmt, e);
 			throw e;
 		}
 		catch (Exception e) {
          // e can be a java.net.SocketException but no one in the body is throwing this exception
          // so, I cannot write a catch clause ... I have do the typecheck as below ...
          if ((e instanceof java.net.SocketException) && !_adminAlerted) {
             newsrack.util.MailUtils.alertAdmin("database server returning java.net.SocketException! Can you check if db server needs restarting?" + e);
             _adminAlerted = true;
          }
 			_log.error("Exception while executing stmt " + stmt, e);
 			return null;
 		}
       finally {
 			closeResultSet(rs);
 			closeStatement(stmt);
 			closeConnection(c);
       }
    }
 
    public static Object query(String stmtString, SQL_ValType[] argTypes, Object[] args, ResultProcessor rp, boolean singleRowOutput)
 	{
 		return execute(stmtString, SQL_StmtType.QUERY, args, argTypes, null, rp, singleRowOutput);
 	}
 
    public static Object update(String stmtString, SQL_ValType[] argTypes, Object[] args)
 	{
 		return execute(stmtString, SQL_StmtType.UPDATE, args, argTypes, null, null, true);
 	}
 
    public static Object delete(String stmtString, SQL_ValType[] argTypes, Object[] args)
 	{
 		return execute(stmtString, SQL_StmtType.DELETE, args, argTypes, null, null, true);
 	}
 }
