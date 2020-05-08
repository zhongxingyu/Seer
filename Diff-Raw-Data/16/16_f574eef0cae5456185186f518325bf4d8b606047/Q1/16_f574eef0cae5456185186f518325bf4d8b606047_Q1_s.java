 package queries;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import queries.GoogleChart.ChartType;
 import database.DB;
 
 
 public class Q1 extends Query {
 	
 	public Q1(String headline) {
 		super(headline);
 	}
 	
 	@Override
 	protected ResultSet doQuery() throws SQLException {
 			
 		String zweitStimmenNachBundeslandTable = createZweitStimmenNachBundeslandTable();
		String zweitStimmenNachParteiTable = createZweitStimmenNachParteiTable();
 
     db.printTable(zweitStimmenNachBundeslandTable);
     db.printTable(zweitStimmenNachParteiTable);
 
     db.printResultSet(db.executeSQL("SELECT " + DB.kForeignKeyParteiID + ", SUM("
         + DB.kAnzahlStimmen + ") FROM " + zweitStimmenNachBundeslandTable + " GROUP BY "
         + DB.kForeignKeyParteiID));
 
     String direktMandateTable = createDirektmandateTable();
 	  db.printResultSet(db.executeSQL("SELECT COUNT(*) FROM " + direktMandateTable));
 
 	  String fuenfProzentParteienTable = createFuenfProzentParteienTable(zweitStimmenNachBundeslandTable);
 	  
 	  String dreiDirektMandateParteienTable = createDreiDirektmandateParteienTable(direktMandateTable);
 
     db.printResultSet(db.executeSQL("SELECT p." + DB.kParteiKuerzel + " FROM " + db.partei()
         + " p, " + dreiDirektMandateParteienTable + " ddmp" + " WHERE p." + DB.kID + " = ddmp."
         + DB.kForeignKeyParteiID));
 
 	  String parteienImBundestagTable = createParteienImBundestagTable(fuenfProzentParteienTable, dreiDirektMandateParteienTable);
 	  String sitzeNachParteiTable = createSitzeNachParteiTable(zweitStimmenNachParteiTable, parteienImBundestagTable);
     
 	  String sitzeNachLandesListenTable = createSitzeNachLandeslistenTable(parteienImBundestagTable, zweitStimmenNachBundeslandTable, sitzeNachParteiTable);
     
 		String ueberhangsmandateTable = createUeberhangsmandateTable(direktMandateTable, sitzeNachLandesListenTable);
   
     String qry = "WITH SumUeberhang AS ( " +
     	"SELECT " + DB.kForeignKeyParteiID + ", SUM(" + DB.kAnzahlUeberhangsmandate + ") AS " + DB.kAnzahlUeberhangsmandate + " " +
     	"FROM " + ueberhangsmandateTable + " GROUP BY " + DB.kForeignKeyParteiID + " " + 
     ") " + 
     "SELECT p." + DB.kParteiKuerzel + ", (s." + DB.kAnzahlSitze + " + COALESCE(u." + DB.kAnzahlUeberhangsmandate + ", 0)) AS " + DB.kAnzahlSitze + " " +
     "FROM " + db.partei() + " p, " + sitzeNachParteiTable + " s " +
     "LEFT OUTER JOIN SumUeberhang u ON s." + DB.kForeignKeyParteiID + " = u." + DB.kForeignKeyParteiID + " " +
     "WHERE p." + DB.kID + " = s." + DB.kForeignKeyParteiID;
 
     return db.executeSQL(qry);
 	}
 	
 	@Override
 	protected String generateBody(ResultSet resultSet) throws SQLException {
 		
 		String[] tableHeaders = new String[] { "Partei", "Anzahl Sitze" };
 		List<List<String>> rows = new ArrayList<List<String>>();
 		
 		int sum = 0;
 		
 		List<Integer> data = new ArrayList<Integer>();
 		List<String> labels = new ArrayList<String>();
 		
 		while(resultSet.next()) {
 			String partei = resultSet.getString(DB.kParteiKuerzel);
 			int sitze = resultSet.getInt(DB.kAnzahlSitze);
 			sum += sitze;
 			
 			List<String> row = new ArrayList<String>();
 			row.add(partei);
 			row.add(String.valueOf(sitze));
 			rows.add(row);
 			
 			data.add(sitze * 100 / 598);
 			labels.add(partei + " (" + sitze + ")");
 		}
 		GoogleChart chart = new GoogleChart(ChartType.PIE, 400, 240);
 		chart.setData(data);
 		chart.setLabels(labels);
 		
 		List<String> finalRow = new ArrayList<String>();
 		finalRow.add("Summe");
 		finalRow.add(String.valueOf(sum));
 		rows.add(finalRow);
 		
 		Table table = new Table(tableHeaders, rows);
 		return "<p>" +  chart.getHtml() + "</p>" + table.getHtml();
 	}
 	
 }
