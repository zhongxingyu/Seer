 package database;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import general.Debug;
 
 /**
  * Klasse zur Ermittlung von Statistischen Daten
  */
 public class DbStatistics {
 
 	private Connection dbConnection;
 	private final String PATH = System.getProperty("user.dir") + "\\Datenbank\\FIARG";
 	private String driverClass = "org.hsqldb.jdbcDriver";
 	private int countGames = 0;
 	private int countWinningGames = 0;
 	private ResultSet rs;
 	private int countFastGames = 0;
 	private String [][]arraySpielStatistik; //grobe Spielbersicht
 	private String[] arrayLog; // Log-Eintrge
 	private Statement dbStatement;
 	private float avgPlayingGame; //Durchschnittlich gespeilte Zeit
 	private float avgPlayingSets; //Durchschnittlich gespielte Sets
 	private int playedSets; //gespeilte Stze
 	private int wonSets; //gewonnene Stze
 	private float avgMoves; //durchschnittliche Anzahl von Zgen
 	private float fastestGame; //schnellstes Spiel (Dauer)
 	private String sort = "Order by GameID DESC"; //Sortierung der Ausgabe
 	private int gameID; //SpieldID vom UI
 	private boolean empty = false; //Datenbank leer?
 
 	
 	/**
 	 * Konstruktor
 	 * Aufgabe: Verbindungsherstellung zur Datenbank
 	 */
 	public DbStatistics(){
 
 		// Datenbankverbindung initiieren
 				try {
 					Class.forName(driverClass);
 				} catch (ClassNotFoundException exc) {
 					Debug.error("Driver-Class nicht gefunden");
 					
 				}
 
 				try {
 					dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:" + PATH,
 							"sa", "");
 					DatabaseMetaData databaseMetaData = dbConnection.getMetaData();
 					rs = databaseMetaData.getTables(null, null, null,
 					new String[] { "TABLE" });
 				}
 				catch (SQLException e) {
 					Debug.error("Verbindung zur Datenbank konnte nicht hergestellt werden");
 				} // Verbindungsaufbau
 
 		// falls es einen Datensatz gibt, wurde mindestens eine Tabelle gefunden
 		try {
 			if (rs.next()) {
 				// Ermittlung der werte
 				
 				statistic();
 			}
 			else{
 				empty = true;
 			}
 		
 		}
 		catch (SQLException e) {
 			Debug.error("Datenbank ist leer - Statistikdaten knnen nicht ermittlet werden");
 		}
 	}
 
 	/**
 	 * Anzahl der gespielten Spiele
 	 * @return countGame
 	 */
 	public int getCountGames(){
 		return countGames;
 	}
 
 	/**
 	 * Anzahl der gewonnenen Spiele
 	 * @return countWinningGames
 	 */
 	public int getCountWinningGames() {
 		return countWinningGames;
 	}
 	
 	/**
 	 * Anzahl der Spiele, die im Blitzsieg gewonnen wurden
 	 * @return countFastGames
 	 */	
 	public int getCountFastGames() {
 		return countFastGames;
 	}	
 
 
 	/**
 	 * Ermittlung der zu sortierenden Spalte und der Sortierrichtung
 	 * Standartwert: Sortierung der GameID (DESC)
	 * 	 @param column
 	 * 			Spalte, die Sortiert werden soll
 	 *	 @param direction
 	 * 			Sortierrichtung (0:ASC / 1:DESC)
 	 */	
 	public void setSort(int column, int direction){
 	
 		sort = "Order by ";
 		switch (column) {
 		case 0: sort = sort+"GameID"; break;
 		case 1: sort = sort+"OppPlayerID"; break;
 		case 2: sort = sort+"Winner"; break;
 		case 3: sort = sort+"Score"; break;
 		case 4: sort = sort+"Beginn"; break;
 		case 5: sort = sort+"Dauer"; break;
 		case 6: sort = sort+"Satzanzahl"; break;
 		default: sort = sort +"GameID";
 			break;
 		}
 				
 		if (direction==0){
 			sort = sort+" ASC";
 		}else
 		{
 			sort = sort+" DESC";
 		}
 		
 	}
 
 	
 	/**
 	 * Setzen der SpielID, die von der UI geliefert wird
 	 * @param GameIdUi
 	 */
 	public void setLogGameID(int GameIdUi){
 		gameID= GameIdUi;
 	}
 	
 	
 	/**
 	 * Ermittlung der statistischen Daten
 	 * 
 	 */
 	private void statistic(){
 		
 		
 		try {
 			dbStatement = dbConnection.createStatement();
 	
 		
 		//ANzahl der Spiele ermitteln
 		ResultSet rs = dbStatement.executeQuery("Select count(GameID) from Game;");
 		rs.next();
 		countGames = rs.getInt(1);
 		rs.close();
 		
 		//Anzahl der gewonnen Spiele
 		rs = dbStatement.executeQuery("Select count(GameID) from Game where Winner = 5;");
 		rs.next();
 		countWinningGames = rs.getInt(1);
 		rs.close();
 		
 		//Anzahl K.O-Siege
 		rs = dbStatement.executeQuery("Select count(*) from (Select GameID from DSet group by GameID having count(SetID)<3);");
 		rs.next();
 		countFastGames = rs.getInt(1);
 		rs.close();
 		
 		//Durchschnittliche Spieldauer
 		rs = dbStatement.executeQuery("Select avg(cast(Dauer as Float)) from (Select max(Ende)-min(Beginn) as Dauer from DSet group by GameID)");
 		rs.next();
 		avgPlayingGame = rs.getFloat(1)/60000; //Spieldauer in min
 		rs.close();
 		
 		//Durchschnittliche Anzahl gespielter Stze
 		rs = dbStatement.executeQuery("Select avg(cast(Satzanzahl as Float)) from (Select count(SetID) as Satzanzahl from DSet group by GameID)");
 		rs.next();
 		avgPlayingSets = rs.getFloat(1);
 		rs.close();
 		
 		//Anzahl der gespielten Stze
 		rs = dbStatement.executeQuery("Select count(SetID) from DSet;");
 		rs.next();
 		playedSets = rs.getInt(1);
 		rs.close();
 		
 		//Anzahl gewonnener Stze
 		rs = dbStatement.executeQuery("Select count(GameID) from DSet where Winner=5;");
 		rs.next();
 		wonSets = rs.getInt(1);
 		rs.close();
 		
 		//Durchschnittliche Zuganzahl je Satz
 		rs = dbStatement.executeQuery("Select avg(cast(Zuganzahl as Float)) from (Select count(MoveID) as Zuganzahl from Move where PlayerID=5 group by SetID, GameID);");
 		rs.next();
 		avgMoves = rs.getInt(1);
 		rs.close();
 		
 		//Schnellstes Spiel
 		rs = dbStatement.executeQuery("Select min(dauer) from (select max(Ende)-min(Beginn) as dauer from DSet group by GameID)");
 		rs.next();
 		fastestGame = rs.getInt(1)/60000;
 		}
 		catch (SQLException e) {
 			Debug.error("Fehler im Ausfhren einer SQL-Anweisung");
 		}
 	}
 	
 	
 	
 
 	/**
 	 * Grobbersicht ber gespielte Spiele
 	 * @return arraySpielStatistik
 	 */
 	public String[][] getSpielStatistik (){
 
 		int anz = 0;
 		int i = 0;
 		
 		try {
 		dbStatement = dbConnection.createStatement();
 		//Anzahl der gespielten Spiele = Gre des Arrays;
 		rs = dbStatement.executeQuery("Select count(GameID) from Game;");
 		rs.next();
 		anz = rs.getInt(1);
 		rs.close();
 		
 
 		rs = dbStatement.executeQuery("Select GameID, OppPlayerID, Winner, Score, min(Beginn) as Beginn, max(Ende)-min(Beginn) as Dauer, count(SetID) as Satzanzahl from Game inner join DSet on Game.GameID=DSet.GameID group by GameID, Winner, OppPlayerID, Score "+""+sort+";");
 		
 		
 		arraySpielStatistik = new String[anz][7];
 		
 		while (rs.next()){
 		String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(rs.getLong(5))); //Startzeit des Spieles
 		float dauer = rs.getFloat(6)/60000;
 		arraySpielStatistik[i][0] = rs.getString(1); //GameID
 		arraySpielStatistik[i][1] = rs.getString(2); //ID des Gegenspeilers
 		arraySpielStatistik[i][2] = rs.getString(3); //ID des Gewinners
 		arraySpielStatistik[i][3] = rs.getString(4); //Spielstand
 		arraySpielStatistik[i][4] = date; //Spielbeginn
 		arraySpielStatistik[i][5] = Float.toString(dauer); //Spieldauer
 		arraySpielStatistik[i][6] = rs.getString(7); //Anzahl der gespielten Stze		
 		i++;			
 		}
 		rs.close();	
 		}
 		 catch (SQLException e) {
 				Debug.error("Fehler im Ausfhten einer SQL-Anweisung");
 			}
 		
 		return arraySpielStatistik;
 	}
 
 	
 	/**
 	 * Durchschnittliche Spieldauer
 	 * @return avgPlayingGame
 	 */		
 	public float getAvgPlayingGame(){
 		return avgPlayingGame;
 	}
 	
 	
 	/**
 	 * Durchschnittliche Anzahl gespielter Stze
 	 * @return avgPlayingGame
 	 */
 	public float getAvgPlayingSets(){
 		return avgPlayingSets;
 	}
 
 	/**
 	 * Anzahl gespielter Stze
 	 * @return playedSets
 	 */
 	public int getPlayedSets(){
 		return playedSets;
 	}
 
 	
 	/**
 	 * Anzahl gewonnener Stze
 	 * @return wonSets
 	 */	
 	public int getWonSets(){
 		return wonSets;
 	}
 	
 	/**
 	 * Durchschnittliche Anzahl von Zgen in einem Satz
 	 * @return avgMoves
 	 */	
 	public float getAvgMoves(){
 		return avgMoves;
 	}
 	
 	/**
 	 * Schnellstes Spiel (Dauer)
 	 * @return fastestGame
 	 */	
 	public float getFastestGame(){
 		return fastestGame;
 	}
 	
 	
 	/**
 	 * Logeintrge
 	 * @return arrayLog
 	 */	
 	public String[] getLog(){
 	
 		String date; // Zeitstempel des Logs
 		String message;
 		int i=0;
 		int sizeLog;
 		
 		try {
 		dbStatement = dbConnection.createStatement();
 		//Anzahl der gespeicherten Log-Werte = Arraygre
 		rs = dbStatement.executeQuery("Select count(LogID) from Log where GameID = ("+ gameID + ") and level <=20;");
 		rs.next();
 		sizeLog = rs.getInt(1);
 		rs.close();
 		String[] arrayLog = new String[sizeLog];
 		
 		rs = dbStatement.executeQuery("Select * from Log where (GameID = "+ gameID + ") and (level <=20);");
 		
 		 
 		 while(rs.next()){
 			  
 			 date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(rs.getLong(2))); //Startzeit des Spieles 
 			 message = rs.getString(3);
 			 arrayLog[i]= date +", "+ message;
 			 i++;
 			 }
 		 rs.close();
 		 return arrayLog;
 		
 		} catch (SQLException e) {
 			Debug.error("Fehler beim Ausfhren einer SQL-Anweisung");
 		}
 		
 		return arrayLog;	
 			
 	}
 	
 
 	/**
 	 * Schlieen der Datenbankverbindung
 	 */	
 	public void statisticClose(){
 		try {
 			dbConnection.close();
 		} catch (SQLException e) {
 			Debug.error("Schlieen nicht mglich");
 		}
 	}
 
 	/**
 	 * Gibt an, ob DB gefllt ist
 	 * @return empty
 	 * 			TRUE: Datenbank ist leer
 	 * 			FALSE: Datenbank ist nicht leer
 	 */	
 	public boolean getDbEmpty(){
 		return empty;
 	}
 }
 
