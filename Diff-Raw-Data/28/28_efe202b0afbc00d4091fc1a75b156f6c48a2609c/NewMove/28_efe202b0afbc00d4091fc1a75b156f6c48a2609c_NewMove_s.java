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
  * Servlet implementation class NewMove
  */
 public class NewMove extends HttpServlet
 {
 
 
 	private final static long serialVersionUID = 1L;
 
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public NewMove()
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
 		printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_ERROR_DATA_NOT_DETECTED));
 	}
 
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 	{
 		response.setContentType(Utilities.CONTENT_TYPE_JSON);
 		PrintWriter printWriter = response.getWriter();
 
 		final String game_id = request.getParameter(Utilities.POST_DATA_GAME_ID);
 		final Long user_id = new Long(request.getParameter(Utilities.POST_DATA_ID));
 		final Long user_opponent = new Long(request.getParameter(Utilities.POST_DATA_USER_OPPONENT));
 		final String board = request.getParameter(Utilities.POST_DATA_BOARD);
 
 		if (game_id == null || game_id.isEmpty() || user_id < 0 || user_opponent < 0 || board == null || board.isEmpty())
 		{
 			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_EMPTY));
 		}
 		else
 		{
 			Connection sqlConnection = null;
 			PreparedStatement sqlStatement = null;
 
 			try
 			{
 				sqlConnection = Utilities.getSQLConnection();
 
 				// find the given game ID
 				// see if the game has already been finished
 				// see if the we're the user who's turn it is
 				// update the data
 
 				// prepare a SQL statement to be run on the database
				final String sqlStatementString = "SELECT " + Utilities.DATABASE_TABLE_GAMES_COLUMN_FINISHED + ", " + Utilities.DATABASE_TABLE_GAMES_COLUMN_TURN + " FROM " + Utilities.DATABASE_TABLE_GAMES + " WHERE " + Utilities.DATABASE_TABLE_GAMES_COLUMN_ID + " = ?";
 				sqlStatement = sqlConnection.prepareStatement(sqlStatementString);
 
 				// prevent SQL injection by inserting data this way
 				sqlStatement.setString(1, game_id);
 
 				// run the SQL statement and acquire any return information
 				final ResultSet sqlResult = sqlStatement.executeQuery();
 
 				if (sqlResult.next())
 				{
 					if (sqlResult.getByte(Utilities.DATABASE_TABLE_GAMES_COLUMN_FINISHED) == Utilities.DATABASE_TABLE_GAMES_FINISHED_FALSE)
 					// make sure that the game has not been finished
 					{
 						final long user_creator = sqlResult.getLong(Utilities.DATABASE_TABLE_GAMES_COLUMN_USER_CREATOR);
 						final long user_challenged = sqlResult.getLong(Utilities.DATABASE_TABLE_GAMES_COLUMN_USER_CHALLENGED);
 						final byte turn = sqlResult.getByte(Utilities.DATABASE_TABLE_GAMES_COLUMN_TURN);
 
 						if ((user_id == user_creator && turn == Utilities.DATABASE_TABLE_GAMES_TURN_CREATOR) || (user_id == user_challenged && turn == Utilities.DATABASE_TABLE_GAMES_TURN_CHALLENGED))
 						{
							printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_GENERIC));
 						}
 						else
 						{
 							printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_ITS_NOT_YOUR_TURN));
 						}
 					}
 					else
 					// we are trying to add a new move to a game that is already finished. this should never happen
 					{
 						printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GAME_IS_ALREADY_OVER));
 					}
 				}
 				else
 				{
 					printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_ERROR_DATABASE_COULD_NOT_FIND_GAME_WITH_SPECIFIED_ID));
 				}
 			}
 			catch (final ClassNotFoundException e)
 			{
 				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_LOAD));
 			}
 			catch (final SQLException e)
 			{
 				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_CONNECT));
 			}
 			finally
 			{
 				Utilities.closeSQL(sqlConnection, sqlStatement);
 			}
 		}
 	}
 
 
 }
