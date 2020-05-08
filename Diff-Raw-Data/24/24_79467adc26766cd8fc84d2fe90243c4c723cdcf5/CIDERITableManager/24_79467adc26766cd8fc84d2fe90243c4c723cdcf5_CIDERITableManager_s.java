 
 package edu.wustl.common.query.itablemanager;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import edu.wustl.common.dao.DatabaseConnectionParams;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.query.querymanager.Count;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.Variables;
 
 /**
  * This class is responsible for
  * 
  * @author ravindra_jain
  * @version 1.0
  * @since January 7, 2009
  * 
  */
 public class CIDERITableManager extends ITableManager
 {
 
 	/**  **/
 	private static CIDERITableManager sINSTANCE;
 
 	/**
 	 * 
 	 * @return
 	 * @throws DAOException
 	 */
 	public static CIDERITableManager getInstance() throws DAOException
 	{
 		if (sINSTANCE == null)
 		{
 			sINSTANCE = new CIDERITableManager();
 		}
 		return sINSTANCE;
 	}
 
 	/**
 	 * 
 	 * @param patientDeid
 	 * @param queryExecLogId
 	 * @param upi
 	 * @throws DAOException
 	 * @throws SQLException 
 	 */
 	public void insertITableEntry(int patientDeid, int queryExecLogId, String upi)
 			throws DAOException, SQLException
 	{
 		DatabaseConnectionParams DB_CONNECTION_PARAMS = new DatabaseConnectionParams();
 		try
 		{
 			// CODE TO INSERT DATA INTO QUERY_ITABLE
 			DB_CONNECTION_PARAMS.openSession(Constants.JNDI_NAME_CIDER);
 
 			ArrayList<Object> columnValues = new ArrayList<Object>();
 
 			columnValues.add(patientDeid);
 			columnValues.add(upi);
 			columnValues.add(queryExecLogId);
 			DB_CONNECTION_PARAMS.insert(Variables.ITABLE, columnValues);
 			DB_CONNECTION_PARAMS.commit();
 		}
 		catch (SQLException ex)
 		{
 			throw ex;
 		}
 		catch (DAOException ex)
 		{
 			throw ex;
 		}
 		finally
 		{
 			DB_CONNECTION_PARAMS.closeConnectionParams();
 			DB_CONNECTION_PARAMS.closeSession();
 		}
 	}
 
 	/**
 	 * 
 	 * @param projectId
 	 * @param userId
 	 * @param query_id
 	 * @return
 	 * @throws SQLException 
 	 * @throws DAOException 
 	 */
 	public int insertNewQuery(Long projectId, Long userId, Long query_id) throws SQLException,
 			DAOException
 	{
 		int queryExecId = -1;
 		ResultSet rs = null;
 		Statement stmt = null;
 		DatabaseConnectionParams DB_CONNECTION_PARAMS = new DatabaseConnectionParams();
 
 		try
 		{
 			DB_CONNECTION_PARAMS.openSession(Constants.JNDI_NAME_CIDER);
 
 			stmt = DB_CONNECTION_PARAMS.getDatabaseStatement();
 
 			// Insert a row
 			// Indicate you want automatically 
 			// generated keys
 			String query = "INSERT INTO " + Variables.QUERY_EXECUTION_LOG + "("
 					+ Constants.CREATIONTIME + "," + Constants.USER_ID + "," + Constants.STATUS
 					+ "," + Constants.PRJCT_ID + "," + Constants.QUERY_EXECUTION_ID + ","
 					+ Constants.QRY_ID + ") VALUES " + "( CURRENT TIMESTAMP ," + userId + ",'"
 					+ Constants.QUERY_IN_PROGRESS + "'" + "," + projectId + ",default," + query_id
 					+ ")";
 
 			stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
 
 			rs = stmt.getGeneratedKeys();
 			// Retrieve the automatically
 			// generated key value in a ResultSet.
 			// Only one row is returned.
 			// Create ResultSet for query
 			while (rs.next())
 			{
 				queryExecId = rs.getInt(1);
 				// Get automatically generated key value
				System.out.println("automatically generated key value = " + queryExecId);
 			}
 			DB_CONNECTION_PARAMS.commit();
 		}
 		catch (SQLException ex)
 		{
 			throw ex;
 		}
 		catch (DAOException ex)
 		{
 			throw ex;
 		}
 		finally
 		{
 			if(rs != null)
 			{
 				rs.close();
 			}
 			stmt.close();
 			DB_CONNECTION_PARAMS.closeConnectionParams();
 			DB_CONNECTION_PARAMS.closeSession();
 		}
 
 		return queryExecId;
 	}
 
 	/**
 	 * 
 	 * @param status
 	 * @param queryExecId
 	 * @throws SQLException 
 	 * @throws DAOException 
 	 */
 	public void changeStatus(String status, int queryExecId) throws SQLException, DAOException
 	{
 		DatabaseConnectionParams DB_CONNECTION_PARAMS = new DatabaseConnectionParams();
 		Statement stmt = null;
 		try
 		{
 			DB_CONNECTION_PARAMS.openSession(Constants.JNDI_NAME_CIDER);
 			stmt = DB_CONNECTION_PARAMS.getDatabaseStatement();
 
 			String query = "UPDATE " + Variables.QUERY_EXECUTION_LOG + " SET " + Constants.STATUS
 					+ "='" + status + "' WHERE " + Constants.QUERY_EXECUTION_ID + "=" + queryExecId;
 
 			stmt.executeUpdate(query);
 			DB_CONNECTION_PARAMS.commit();
 		}
 		catch (SQLException ex)
 		{
 			throw ex;
 		}
 		catch (DAOException ex)
 		{
 			throw ex;
 		}
 		finally
 		{
 			stmt.close();
 			DB_CONNECTION_PARAMS.closeConnectionParams();
 			DB_CONNECTION_PARAMS.closeSession();
 		}
 	}
 
 	/**
 	 * 
 	 * @param queryExecId
 	 * @return
 	 * @throws SQLException
 	 * @throws DAOException
 	 */
 	public Count getCount(int queryExecId) throws SQLException, DAOException
 	{
		System.out.println();
 		Count count = new Count();
 		int actualCount = 0;
 		String status = "";
 		ResultSet rs = null;
 		DatabaseConnectionParams DB_CONNECTION_PARAMS = new DatabaseConnectionParams();
 
 		try
 		{
 			DB_CONNECTION_PARAMS.openSession(Constants.JNDI_NAME_CIDER);
 			String query = "select count(*) from " + Variables.ITABLE + " where "
 					+ Constants.QUERY_EXECUTION_ID + "= " + queryExecId;
 			rs = DB_CONNECTION_PARAMS.getResultSet(query);
 			while (rs.next())
 			{
 				actualCount = rs.getInt(1);
 			}
 			count.setCount(actualCount);
 			
 			rs = null;
 			
 			query = "select " + Constants.STATUS + " from " + Variables.QUERY_EXECUTION_LOG
 					+ " where " + Constants.QUERY_EXECUTION_ID + "= " + queryExecId;
 			rs = DB_CONNECTION_PARAMS.getResultSet(query);
 			while (rs.next())
 			{
 				status = rs.getString(1);
 			}
 			count.setQuery_exection_id(queryExecId);
 			count.setStatus(status);
 			
 			DB_CONNECTION_PARAMS.commit();
 		}
 		catch (SQLException ex)
 		{
			ex.printStackTrace();
 			throw ex;
 		}
 		catch (DAOException ex)
 		{
			ex.printStackTrace();
 			throw ex;
 		}
 		finally
 		{
 			if(rs!=null)
 			{
 				rs.close();
 			}
 			DB_CONNECTION_PARAMS.closeConnectionParams();
 			DB_CONNECTION_PARAMS.closeSession();
 		}
 		return count;
 
 	}
 }
