 package de.dhbw.muehle_spiel;
 
 
 import java.sql.*;
 
 
 public class Database {
 	//Treiber laden
 	String url="jdbc:sqlite:database.db";
 	Connection c;
 	String driver="org.sqlite.JDBC";
 	Statement statement=null;
 	
 	public Database(){
 		try {
 			
 			Class.forName(driver);
 			c= DriverManager.getConnection(url);
 		} catch (SQLException | ClassNotFoundException e) {
 			
 			e.printStackTrace();
 		}
 		
 	}
 
 //Speichern der Anzahl der Spielzge
 public void anzahlzuegespeichern(Spieler spieler){
 	
 	int zuege=spieler.getAnzahlZuege();
 	//String sql=("INSERT INTO highscores(Spieler, Zge) VALUES("+Spielername+","+zuege+")");
 	try {
 		statement=c.createStatement();
 		//statement.executeQuery(sql);
 		  	
 		
 		
 	}catch (SQLException e) {
 		
 		e.printStackTrace();
 	}
 	
 }
 //Erzeugen der Tabelle highscore
 public void erzeuge_h(){
 	 try {
 	    statement=c.createStatement();	     
 	    String create="CREATE TABLE highscore (ID INTEGER PRIMARY KEY AUTOINCREMENT, Spieler varChar(20), Zge INT)";
 	    statement.executeUpdate(create);
 	    System.out.println("Tabelle highscore wurde erzeugt");		    		  		    
 	    } 
 	    catch ( Exception e ) {
 	    	System.err.println(e.getMessage());
 	        System.exit(0);
 	    }
 }
 //Lschen der Tabelle highscore
 public void lsche_h(){
 	
 	String delete=("DROP TABLE highscore");
 	try {
 		statement=c.createStatement();
 		statement.executeUpdate(delete);
 		System.out.println("Tabelle highscore wurde gelscht");	
 	} catch (SQLException e ) {		
 		e.printStackTrace();
 	}
 	
 	
 }	
 
 
 //bergabe der Position an DB
 public void zugspeichern(Bewegung bewegung,Spieler spieler,boolean muehle,Spielstein spielstein){
 	
 		// Ablegen der Positionsindexe, Spielsteinfarbe in String-Variablen & 
 		String vonEbene, vonX, vonY, nachEbene, nachX, nachY,farbe,lspielstein;
 		if(spielstein != null)
 		lspielstein=spielstein.toString();
 		else
 		lspielstein = " ";
 	    farbe=spieler.SpielsteinFarbeAsString();
	    if(bewegung.getVon() != null)
	    {
			vonEbene = bewegung.getVon().getEbene().toString();
			vonX = bewegung.getVon().getX().toString();
			vonY = bewegung.getVon().getY().toString();
	    }
	    else
	    {
	    	vonEbene = " ";
	    	vonX = " ";
	    	vonY = " ";
	    	
	    }
 		nachEbene= bewegung.getNach().getEbene().toString();
 		nachX = bewegung.getNach().getX().toString();
 		nachY = bewegung.getNach().getY().toString();
 		
 	String update=("INSERT INTO protokoll (Spielstein,E1,X1,Y1,E2,X2,Y2)"
 				+ " VALUES("+farbe+","+vonEbene+","+vonX+","+vonY+","+nachEbene+","+nachX+","+nachY+","+muehle+","+lspielstein+")");
 						
 	try {
 		statement=c.createStatement();
     	statement.executeUpdate(update);
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
 }
 
 //Ausgabe der DB
 public void zeige_p(){
 	
     try {
     	statement=c.createStatement();
 	    String ausgabe=("SELECT * FROM protokoll");
 	    ResultSet result=statement.executeQuery(ausgabe);
 	    while (result.next()) {
 	    	  System.out.println();
 	    	  int zugID = result.getInt("ID");
 	    	  System.out.print("ID: "+zugID);
 	    	  String Spielstein=result.getString("Spielstein");
 	    	  System.out.print(" Spielstein: "+Spielstein);
 	    	  int E1=result.getInt("E1");
 	    	  int X1=result.getInt("X1");
 	    	  int Y1=result.getInt("Y1");
 	    	  int E2=result.getInt("E2");
 	    	  int X2=result.getInt("X2");
 	    	  int Y2=result.getInt("Y2");
 	    	  System.out.print(" E1: "+E1);
 	    	  System.out.print(" X1: "+X1);
 	    	  System.out.print(" Y1: "+Y1);
 	    	  System.out.print(" E2: "+E2);
 	    	  System.out.print(" X2: "+X2);
 	    	  System.out.print(" Y2: "+Y2);
 	    	  
 	    		    	  
 	    	}
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
 }
 
 //Erzeugen der DB
 public void erzeuge_p(){
 		    
 		    try {
 		    	statement=c.createStatement();	     
 		    String create="CREATE TABLE protokoll(ID INTEGER PRIMARY KEY AUTOINCREMENT,"
 		    			+ "Spielstein varChar(10),"
 		    			+ "E1 char(1), X1 char(1), Y1 char(1), "
 		    			+ "E2 char(1), X2 char(1), Y2 char(1), "
 		    			+ "muehle boolean, GeloeschterStein varchar(30))";
 		    statement.executeUpdate(create);
 		    System.out.println("Tabelle protokoll wurde erzeugt");			    		  		    
 		    } 
 		    catch ( Exception e ) {
 		    	System.err.println(e.getMessage());
 		        System.exit(0);
 		    }
 		    
 		    
 		    
 }
 
 //Lschen der Tabelle protokoll	
 public void lsche_p(){
 	
 	String delete=("DROP TABLE protokoll");
 	try {
 		statement=c.createStatement();
 		statement.executeUpdate(delete);
 		System.out.println("Tabelle protokoll wurde gelscht");	
 	} catch (SQLException e ) {		
 		e.printStackTrace();
 	}
 	
 	
 }
 }
 
 
