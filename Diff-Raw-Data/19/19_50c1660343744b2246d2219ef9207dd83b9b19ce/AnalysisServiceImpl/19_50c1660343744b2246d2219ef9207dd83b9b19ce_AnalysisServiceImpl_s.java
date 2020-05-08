 package testbw.server;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import testbw.client.AnalysisService;
 import testbw.util.DBManager;
 import testbw.analysis.DataAnalyzer;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 @SuppressWarnings("serial")
 public class AnalysisServiceImpl extends RemoteServiceServlet implements AnalysisService {
 	
 	@Override
 	public ArrayList<String> analyze(String[] projectInput, String[] queryInput) {
 		
 		ArrayList<String> result = new ArrayList<String>();
 		
 		// Datenbankverbindung ------------------------------------------------
 		DBManager manager = new DBManager(projectInput);
 		try {
 			manager.connect();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			//return "Setup unsuccessful. Problem setting up connection to database.";
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			//return "Setup unsuccessful. Check JDBC Driver declaration on server side.";
 		}
 		Statement st = manager.getStatement();
 		ResultSet rs = null;
 		
 		DataAnalyzer analyzer = new DataAnalyzer(st, rs);
 		
 		try { // Get seat distribution ---------------------------------------
 			result.addAll(analyzer.getSeatDistribution(queryInput));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		// add delimiter
 		result.add("##");
 		
 		try { // Get Wahlkreis winners ----------------------------------------
 			result.addAll(analyzer.getWahlkreissieger(queryInput));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		}
 		
 		// add delimiter
 		result.add("##");
 				
 		try { // Get Bundestag members ----------------------------------------
 			result.addAll(analyzer.getMembers(queryInput));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} 
 		
 		// add delimiter
 		result.add("##");
 				
		try { // Get Ueberhangsmandate members ----------------------------------------
 			result.addAll(analyzer.getUeberhangsmandate(queryInput));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} 
 		
 		try { // Close DB connection ------------------------------------------
 			st.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 }
