 package spieldaten;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Stack;
 
 public class Spiel extends DBObject{
 	/**
 	 * Model-Klasse: Spiel
 	 */
 	private Spieler gegner;
 	private Spieler selbst;
 	private int punkteHeim = 0;
 	private int punkteGegner = 0;
 	private String spielstand;
 	private Spieler sieger;
 	private Stack<Satz> saetze = new Stack<Satz>();
 	
 //	Initiales Instanziieren im Spielverlauf
 	public Spiel(Spieler gegner,Spieler selbst){
 		this.gegner = gegner;
 		this.selbst = selbst;
 	}
 	public Spiel(){
 		
 	}
 	
 //	Simulation
 	public Spiel(int spielnr){
 		ResultSet spiel = HSQLConnection.getInstance().executeQuery(String.format(Strings.SPIEL,spielnr));
 		try{
 			spiel.next();
 			gegner = new Spieler(spiel.getString("name"));
 			this.id = spielnr;
 			this.punkteHeim = spiel.getInt("punkteheim");
 			this.punkteGegner = spiel.getInt("punkteGegner");
 			ResultSet saetzeSQL = HSQLConnection.getInstance().executeQuery(String.format(Strings.SAETZE_EINES_SPIELS,spielnr));
 			while(saetzeSQL.next()){
 				Spieler beginner = saetzeSQL.getInt("id") == selbst.getID() ? selbst : gegner;
 				saetze.add(new Satz(this,saetzeSQL.getInt("satznr"),beginner));
 			}
 		}catch(SQLException ex){
 			ex.printStackTrace();
 		}
 	}
 	
 	public Spieler getSieger(char kennzeichnung){
 		return selbst.getKennzeichnung() == kennzeichnung ? selbst : gegner;
 	}
 	
 	public void erhoehePunkteHeim(){
 		this.punkteHeim++;
 	}
 	
 	public void erhoehePunkteGegner(){
 		this.punkteGegner++;
 	}
 	
 	public Stack<Satz> getSaetze(){
 		return saetze;
 	}
 	
 	public void satzHinzufuegen(Satz satz){
 		saetze.add(satz);
 	}
 	
 	public Satz getAktuellenSatz(){
 		return saetze.lastElement();
 	}
 	
 	public Spieler getSpieler(int id){
 		return selbst.getID() == id ? selbst : gegner;
 	}
 	
 	public Spiel(Spieler gegner,int punkteHeim,int punkteGegner){
 		this.gegner = gegner;
 		this.punkteHeim = punkteHeim;
 		this.punkteGegner = punkteGegner;
 		this.spielstand = punkteHeim + ":" + punkteGegner;
 		this.selbst = new Spieler(Strings.NAME,'O');
 	}
 	
 	
 //	Statistik-Konstruktor
 	public Spiel(int id,Spieler gegner,int punkteHeim,int punkteGegner){
 		this.id = id;
 		this.gegner = gegner;
 		this.punkteHeim = punkteHeim;
 		this.punkteGegner = punkteGegner;
 		this.spielstand = punkteHeim + ":" + punkteGegner;
 		this.selbst = new Spieler(Strings.NAME,'O');
 		if(punkteHeim == 2)
 			this.sieger = selbst;
 		else if(punkteGegner == 2)
 			this.sieger = gegner;
 	}
 	
 	public Spiel(int id,Spieler gegner,int punkteHeim,int punkteGegner,Spieler sieger){
 		
 	}
 	
 	public Spieler getSelbst(){
 		return selbst;
 	}
 	
 	public void setSelbst(Spieler spieler){
 		this.selbst = spieler;
 	}
 	
 	public String getIdString(){
 		return ""+id;
 	}
 	
 	public Spieler getSpieler(String name){
 		return selbst.getName().equals(name) ? selbst : gegner;
 	}
 	
 	public Spieler getGegner(){
 		return gegner;
 	}
 	
 	public void setGegner(Spieler spieler){
 		this.gegner = spieler;
 	}
 	
 	public int getPunkteHeim(){
 		return punkteHeim;
 	}
 	public int getPunkteGegner(){
 		return punkteGegner;
 	}
 	public Spieler getSieger(){
 		return sieger;
 	}
 	public String getSiegerName(){
		return sieger!=null ? sieger.getName() : "";
 	}
 	public void setSieger(Spieler sieger){
 		this.sieger = sieger;
 	}
 	public String getSpielstand(){
 		return spielstand;
 	}
 	
 	public void ladeSaetze(){
 		ResultSet saetzeSQL = HSQLConnection.getInstance().executeQuery(String.format(Strings.SAETZE_EINES_SPIELS,this.id));
 		try{
 			while(saetzeSQL.next()){
 				int satznr = saetzeSQL.getInt("id");
 				String beginnerName = saetzeSQL.getString("beginner");
 				Spieler beginner = null;
 				if(beginnerName != null)
 					beginner = selbst.getName().equals(beginnerName) ? selbst : gegner;
 				Spieler gewinner = null;
 				String gewinnerName = saetzeSQL.getString("gewinner");
 				if(gewinnerName != null)
 					gewinner = selbst.getName().equals(gewinnerName) ? selbst : gegner;
 				
 				Satz satz = new Satz(this,satznr);
 				if(beginner != null)
 					satz.setBeginnendenSpieler(beginner);
 				if(gewinner != null)
 					satz.setSieger(gewinner);
 				this.saetze.add(satz);
 				satz.ladeZuege();
 			}
 		}catch(SQLException ex){
 			
 		}
 	}
 	
 //	Statistik
 	public String getGegnerName(){
 		return gegner.getName();
 	}
 	
 	@Override
 	public void speichern() throws SQLException{
 		this.id = HSQLConnection.getInstance().insert(String.format(Strings.INSERT,"spiel","gegner,punkteheim,punktegegner","'"+gegner.getName()+"',"+punkteHeim+","+punkteGegner),String.format(Strings.LETZTES_SPIEL_NR,"'"+gegner.getName()+"'"));
 	}
 	
 	@Override
 	public void aktualisieren(){
 		if(sieger != null)
 			HSQLConnection.getInstance().update(String.format(Strings.SPIEL_AKTUALISIEREN,gegner.getName(),this.punkteHeim,this.punkteGegner,sieger.getName(),this.id));
 		else
 			HSQLConnection.getInstance().update(String.format(Strings.SPIEL_AKTUALISIEREN_OHNE_SIEGER,gegner.getName(),this.punkteHeim,this.punkteGegner,this.id));
 	}
 }
