 package edu.selu.android.classygames;
 
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  * Servlet implementation class NewRegId
  */
 public class NewRegId extends HttpServlet
 {
 
 
 	private final static long serialVersionUID = 1L;
 
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public NewRegId()
 	{
 		super();
 	}
 
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 	{
 		response.setContentType(Utilities.CONTENT_TYPE_JSON);
 		PrintWriter printWriter = response.getWriter();
 		printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_NOT_DETECTED));
 	}
 
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 	{
 		response.setContentType(Utilities.CONTENT_TYPE_JSON);
 		PrintWriter printWriter = response.getWriter();
 
 		final Long id = new Long(request.getParameter(Utilities.POST_DATA_ID));
 		final String name = request.getParameter(Utilities.POST_DATA_NAME);
 		final String reg_id = request.getParameter(Utilities.POST_DATA_REG_ID);
 
		if (id < 0 || name == null || name.isEmpty() || reg_id == null || reg_id.isEmpty())
 		// check for invalid inputs
 		{
 			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_MALFORMED));
 		}
 		else
 		{
 			int part = -4;
 			Connection sqlConnection = null;
 			part = -3;
 			PreparedStatement sqlStatement = null;
 			part = -2;
 
 			try
 			{
 				part = -1;
 				sqlConnection = Utilities.getSQLConnection();
 				part = 0;
 
 				// prepare a SQL statement to be run on the database
 				String sqlStatementString = "SELECT * FROM " + Utilities.DATABASE_TABLE_USERS + " WHERE " + Utilities.DATABASE_TABLE_USERS_COLUMN_ID + " = ?";
 				part = 2;
 				sqlStatement = sqlConnection.prepareStatement(sqlStatementString);
 				part = 3;
 
 				// prevent SQL injection by inserting user data this way
 				sqlStatement.setLong(1, id);
 				part = 4;
 
 				// run the SQL statement and acquire any return information
 				final ResultSet sqlResult = sqlStatement.executeQuery();
 				part = 7;
 
 				if (sqlResult.next())
 				// the id already exists in the table therefore it's data needs to be updated
 				{
 					part = 71;
 
 					// prepare a SQL statement to be run on the database
 					sqlStatementString = "UPDATE " + Utilities.DATABASE_TABLE_USERS + " SET " + Utilities.DATABASE_TABLE_USERS_COLUMN_ID + " = ?, " + Utilities.DATABASE_TABLE_USERS_COLUMN_NAME + " = ?, " + Utilities.DATABASE_TABLE_USERS_COLUMN_REG_ID + " = ? " + " WHERE " + Utilities.DATABASE_TABLE_USERS_COLUMN_ID + " = ?";
 					part = 72;
 					sqlStatement = sqlConnection.prepareStatement(sqlStatementString);
 					part = 73;
 
 					// prevent SQL injection by inserting user data this way
 					sqlStatement.setLong(1, id);
 					part = 74;
 					sqlStatement.setString(2, name);
 					part = 75;
 					sqlStatement.setString(3, reg_id);
 					part = 76;
 					sqlStatement.setLong(4, id);
 					part = 77;
 				}
 				else
 				// id does not already exist in the table. let's insert it
 				{
 					part = 81;
 					// prepare a SQL statement to be run on the database
 					sqlStatementString = "INSERT INTO " + Utilities.DATABASE_TABLE_USERS + " " + Utilities.DATABASE_TABLE_USERS_FORMAT + " " + Utilities.DATABASE_TABLE_USERS_VALUES;
 					part = 82;
 					sqlStatement = sqlConnection.prepareStatement(sqlStatementString);
 					part = 83;
 
 					// prevent SQL injection by inserting user data this way
 					sqlStatement.setLong(1, id);
 					part = 84;
 					sqlStatement.setString(2, name);
 					part = 85;
 					sqlStatement.setString(3, reg_id);
 					part = 86;
 				}
 
 				part = 9;
 
 				// run the SQL statement
 				sqlStatement.executeUpdate();
 
 				part = 10;
 				printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_GENERIC));
 				part = 11;
 			}
 			catch (final ClassNotFoundException e)
 			{
 				part = part - 100;
 				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_LOAD));
 			}
 			catch (final SQLException e)
 			{
 				part = part + 1000;
 				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_CONNECT + " part: \"" + part + "\" message: \"" + e.getMessage() + "\" state: \"" + e.getSQLState() + "\" code: \"" + e.getErrorCode() + "\" reg_id:\"" + reg_id + "\""));
 			}
 			finally
 			// it's best to release SQL resources in reverse order of their creation as seen here
 			// https://dev.mysql.com/doc/refman/5.0/en/connector-j-usagenotes-statements.html#connector-j-examples-execute-select
 			{
 				Utilities.closeSQL(sqlConnection, sqlStatement);
 			}
 		}
 	}
 
 
 }
