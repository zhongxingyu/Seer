 package testing;
 import java.sql.SQLException;
 
 import database.*;
 import game.*;
 import java.util.Date;
 
 /**
  * TestMethode fr die Datenbank (Erzeugt ein Spiel und lst das Schreiben in die Datenbank aus)
  * @author Jenny Geipel
  */
 public class DB_Test {
 
 
 	public static void main(String[] args) throws ClassNotFoundException, SQLException {
 
 	FourInARowGame Game = new FourInARowGame(3);
 	
 	//Simulation eines Spieles mit 3 Stzen a 3 Zgen
 	Game.setEnemyGroup(3);
 	
 	Game.startNewSet(Player.X);
 	Game.getSets().get(0).setEndTime(new Date());
 	Game.getSets().get(0).setEnemyPlayer(Player.X);
 	Game.getSets().get(0).setOurPlayer(Player.O);
 	Game.getSets().get(0).setStartingPlayer(Player.X);
 	Game.getSets().get(0).setStartTime(new Date());
 	Game.getSets().get(0).setWinnerPlayer(Player.X);
 
 	
 	Game.getSets().get(0).getBoard().makeDrop(Player.X, 1);
 	Game.getSets().get(0).getBoard().makeDrop(Player.O, 2);
 	Game.getSets().get(0).getBoard().makeDrop(Player.X, 1);
 	Game.endSet();
 
 	
 	Game.startNewSet(Player.O);
 	Game.getSets().get(1).setEndTime(new Date());
 	Game.getSets().get(1).setEnemyPlayer(Player.X);
 	Game.getSets().get(1).setOurPlayer(Player.O);
 	Game.getSets().get(1).setStartingPlayer(Player.O);
 	Game.getSets().get(1).setStartTime(new Date());
 	Game.getSets().get(1).setWinnerPlayer(Player.X);
 
 	
 	Game.getSets().get(1).getBoard().makeDrop(Player.O, 1);
 	Game.getSets().get(1).getBoard().makeDrop(Player.X, 2);
 	Game.getSets().get(1).getBoard().makeDrop(Player.O, 1);
 
 	
 	Game.startNewSet(Player.X);
 	Game.getSets().get(2).setEndTime(new Date());
 	Game.getSets().get(2).setEnemyPlayer(Player.X);
 	Game.getSets().get(2).setOurPlayer(Player.O);
 	Game.getSets().get(2).setStartingPlayer(Player.X);
 	Game.getSets().get(2).setStartTime(new Date());
 	Game.getSets().get(2).setWinnerPlayer(Player.O);
 
 	
 	Game.getSets().get(2).getBoard().makeDrop(Player.X, 4);
 	Game.getSets().get(2).getBoard().makeDrop(Player.O, 1);
 	Game.getSets().get(2).getBoard().makeDrop(Player.X, 3);
 	Game.endSet();
 	
 	
 	
 	
 
 	
 	//Ausgabe des Spieles
 	for (int i = 0; i < Game.getSets().size(); i++) {
 		System.out.println(i +" EIN: " + Game.getSets().get(i).getStartTime());
 		System.out.println(i +" AUS: " + Game.getSets().get(i).getEndTime());
 		System.out.println(i +" OurPlayer: " + Game.getSets().get(i).getOurPlayer());
 		System.out.println(i +" EnemyPlayer: " + Game.getSets().get(i).getEnemyPlayer());
 		System.out.println(i +" WinningPlayer: " + Game.getSets().get(i).getWinnerPlayer());
 		System.out.println(i +" StartingPlayer: " + Game.getSets().get(i).getStartingPlayer());
 		//System.out.println(i +" Move: " + Game.getSets().get(i).getBoard().getPlayerMoves().get(i).getColumn());
 	}
 	
 	
 	//Verbindung zu Datenbank und zur Statistik herstellen
 	DbPlay Datenbank = new DbPlay();
 	
 		//gepsielte Daten in DB speichern
 	Datenbank.play(Game);
 		//Log-Eintrge zum Spiel speichern
 	Datenbank.setLog();
 	
 	DbStatistics stat = new DbStatistics();
 	
 	
 	System.out.println("------Datenbank Spiel speichern -----");
 	
 	System.out.println("Einfgen war: "+ Datenbank.getSuccess());
 
 	
 	System.out.println("-----Statistik-----");
 	
 	//Ausgabe allgemeiner statistischer Daten
 	if (stat.getDbEmpty() == false)
 	{
 		System.out.println("Anzahl der Spiele: "+stat.getCountGames());
 		System.out.println("Anzahl gewonnene Spiele: "+stat.getCountWinningGames());
 	
 	//Ausgabe der Grobberischt ber die Spiele (mit Sortierung)
 	stat.setSort(6, 1);	
 	
 	
 	int length = stat.getSpielStatistik().length;
 	String array[][] = new String[length][7];
 	array = stat.getSpielStatistik();
 	for (int i=0; i<length;i++)
 	{
 		System.out.println("Zeile: "+i+"-->"+array[i][0]+","+array[i][1]+","+array[i][2]+","+array[i][3]+","+array[i][4]+","+array[i][5]+","+array[i][6]);
 	}
 	
 	
 	//Ausgabe weiterer statistischer Daten
 	System.out.println("Durchschnittliche Spieldauer: "+ stat.getAvgPlayingGame());
 	System.out.println("durchschnittliche Anzahl gespielter Stez: " + stat.getAvgPlayingSets());
 	System.out.println("Gespielte Stze: "+stat.getPlayedSets());
 	System.out.println("Gewonnene Stze: "+ stat.getWonSets());
 	System.out.println("durchschnittliche Zuganzahl je Satz: "+ stat.getAvgMoves());
 	System.out.println("Schnellste Spiel: "+ stat.getFastestGame());
 	
 	
 	//Ausgabe von Log-Eintrgen (hier zu Spiel mit SpielID = 0=
 	stat.setLogGameID(0);
 	int loglength = stat.getLog().length;
 	String [] array3 = new String[loglength];
 	array3 = stat.getLog();
 	
 	for (int k=0; k<loglength;k++)
 	{
 		System.out.println("LogEintrag: "+array3[k]);
 	}
 	}
 	
 	//Schlieen der Datenbankverbindung
 	stat.statisticClose();
 	}
 }
