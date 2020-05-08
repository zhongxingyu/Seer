 package info.plagiatsjaeger;
 
 import info.plagiatsjaeger.enums.ErrorCode;
 import info.plagiatsjaeger.types.CompareResult;
 import info.plagiatsjaeger.types.Settings;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * Stellt Methoden zur Kommunikation mit der MySqlDatenbank zur Verfuegung.
  * 
  * @author Michael
  */
 public class MySqlDatabaseHelper
 {
 	private static final String	SERVERNAME	= "jdbc:mysql://localhost/plagiatsjaeger?useUnicode=true&characterEncoding=utf-8";
 	private static final String	DBDRIVER	= "com.mysql.jdbc.Driver";
 
 	private Connection			_connection	= null;
 	private Statement			_statement	= null;
 	public static final Logger	_logger		= Logger.getLogger(MySqlDatabaseHelper.class.getName());
 
 	/**
 	 * Konstruktor wird noch nicht verwendet
 	 */
 	public MySqlDatabaseHelper()
 	{
 	}
 
 	/**
 	 * Stellt eine Verbindung mit der SQL Datenbank her.
 	 * 
 	 * @throws ClassNotFoundException
 	 * @throws SQLException
 	 */
 	private void connect() throws ClassNotFoundException, SQLException
 	{
 		try
 		{
 			_connection = null;
 			_statement = null;
 			// Laedt den MySQL Treiber
 			Class.forName(DBDRIVER);
 			// Verbindung mit DB herstellen
 			String strPassword = this.readPassword();
 			_connection = DriverManager.getConnection(SERVERNAME, "root", strPassword);
 			// Statements erlauben SQL Abfragen
 			_statement = _connection.createStatement();
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Trennt eine bestehende Verbindung mit der Datenbank
 	 * 
 	 * @throws SQLException
 	 */
 	private void disconnect()
 	{
 		try
 		{
 			if (!_statement.isClosed()) _statement.close();
 			if (!_connection.isClosed()) _connection.close();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Ermittelt das Passwort aus dem Passwordfile
 	 * 
 	 * @return result Passwort als String
 	 */
 	public String readPassword()
 	{
 		String result = "";
 		String strFilepath = System.getProperty("user.home") + File.separator + "password.txt";
 		result = SourceLoader.loadFile(strFilepath).trim();
 		return result;
 	}
 
 	/**
 	 * Fuegt eine Liste von Suchergebnissen, die in einem lokalen Dokument
 	 * gefunden wurden, zu einem Report ein.
 	 * 
 	 * @param compareResults
 	 *            Gefundene Resultate
 	 * @param dID
 	 *            Zu Resultat gehoerende DocId
 	 */
 	public void insertCompareResults(ArrayList<CompareResult> compareResults, int dID)
 	{
 		try
 		{
 			String strStatement = "";
 			connect();
 			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
 			otherSymbols.setDecimalSeparator('.');
 			DecimalFormat df = new DecimalFormat("###.##", otherSymbols);
 			for (CompareResult result : compareResults)
 			{
 				String text = new String(result.getSourceText().getBytes("UTF-8"), "UTF-8");
 				strStatement = "INSERT INTO result VALUES(DEFAULT, '" + text + "' , '" + "' , '" + dID + "' , '" + result.getCheckStart() + "' , '" + result.getCheckEnd() + "' , '" + df.format(result.getSimilarity() * 100) + "' , '" + result.getReportID() + "' )";
 				_statement.executeUpdate(strStatement);
 			}
 			disconnect();
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Fuegt eine Liste von Suchergebnissen, die auf einer Internetseite
 	 * gefunden wurden, zu einem Report ein.
 	 * 
 	 * @param compareResults
 	 *            gefundene Resultate
 	 * @param sourceLink
 	 *            zu Resultaten gehoerender Link
 	 */
 	public void insertCompareResults(ArrayList<CompareResult> compareResults, String sourceLink)
 	{
 		try
 		{
 			String strStatement = "";
 			connect();
 			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
 			otherSymbols.setDecimalSeparator('.');
 			DecimalFormat df = new DecimalFormat("###.##", otherSymbols);
 			for (CompareResult result : compareResults)
 			{
 				String text = new String(result.getSourceText().getBytes("UTF-8"), "UTF-8");
 				strStatement = "INSERT INTO result VALUES(DEFAULT, '" + text + "','" + sourceLink + "' , " + "null" + " , '" + result.getCheckStart() + "' , '" + result.getCheckEnd() + "','" + df.format(result.getSimilarity() * 100) + "' , '" + result.getReportID() + "' )";
 				_statement.executeUpdate(strStatement);
 			}
 			disconnect();
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Liest die DocumentId aus einem Report aus.
 	 * 
 	 * @param rID
 	 * @return return
 	 */
 	public int getDocumentID(int rID)
 	{
 		int result = 0;
 
 		String strStatement = "SELECT dID FROM report WHERE rID = " + rID;
 		try
 		{
 			ResultSet rstResultSet = startQuery(strStatement);
 			if (rstResultSet.next())
 			{
 				result = rstResultSet.getInt("dID");
 			}
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
		finally
		{
			disconnect();
		}
		
 		return result;
 	}
 
 	/**
 	 * @param query
 	 * @return return
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 */
 	public ResultSet startQuery(String query) throws ClassNotFoundException, SQLException
 	{
 		ResultSet rstResultSet = null;
 		try
 		{
 			connect();
 			rstResultSet = _statement.executeQuery(query);
			
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
		finally
		{
			disconnect();
		}
 		return rstResultSet;
 	}
 
 	/**
 	 * Laedt die Einstellungen zu einem Report aus der Datenbank.
 	 * 
 	 * @param rId
 	 *            DocumentId des aktuellen Reports
 	 * @return result Gibt Settingsobjekt zurueck
 	 */
 	public Settings getSettings(int rId)
 	{
 		Settings result = null;
 		String strStatement = "SELECT r.rThreshold, sl.slSearchSentenceLength, sl.slSearchJumpLength, sl.slCompareSentenceLength, sl.slCompareJumpLength, r.rCheckWWW FROM report AS r LEFT JOIN settinglevel AS sl ON r.slID = sl.slID WHERE r.rID = " + rId;
 		ResultSet rstResultSet = null;
 		try
 		{
 			rstResultSet = startQuery(strStatement);
 			if (rstResultSet.next())
 			{
 				result = new Settings(rstResultSet.getInt("r.rThreshold"), rstResultSet.getInt("sl.slSearchSentenceLength"), rstResultSet.getInt("sl.slSearchJumpLength"), rstResultSet.getInt("sl.slCompareSentenceLength"), rstResultSet.getInt("sl.slCompareJumpLength"), rstResultSet.getBoolean("r.rCheckWWW"));
 			}
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (Exception e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
		finally
		{
			disconnect();
		}
 
 		return result;
 	}
 
 	/**
 	 * 
 	 */
 	public void setReportState(int rId, ErrorCode state)
 	{
 		try
 		{
 			connect();
 			String strStatement = "UPDATE report SET rID=" + rId;
 			_statement.executeUpdate(strStatement);
 			disconnect();
 			_logger.info("State changed for: " + rId + "to " + state);
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Noch nicht implementiert! Setzt ein Document als fertig geparst.
 	 * 
 	 * @param docId
 	 */
 	public void setDocumentAsParsed(int docId)
 	{
 		try
 		{
 			connect();
 			String strStatement = "UPDATE document SET dIsParsed=1 WHERE dID=" + docId;
 			_statement.executeUpdate(strStatement);
 			disconnect();
 			_logger.info("Setting document parsed in DB: " + docId);
 		}
 		catch (ClassNotFoundException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			_logger.fatal(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 }
