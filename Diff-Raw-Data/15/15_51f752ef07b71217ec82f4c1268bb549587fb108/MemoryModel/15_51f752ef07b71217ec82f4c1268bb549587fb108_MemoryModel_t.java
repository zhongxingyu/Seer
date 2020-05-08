 import java.util.Random;
 
 /**
  * Das Spielfeld welches aus Karten,
  * den aktuellen Spieler und die Spielerpunkte
  * besteht.
  *  
  * @see Karte
  * 
  */
 public class MemoryModel {
 	private Karte[] karten;
 	private int[] spielerPunkte;
 	private int spielerAmZug;
 	
 	/**
 	 * Initialisiert das Spielfeld, die Spieler und deren Punkte.
 	 * 
 	 * @param anzahlKarten Anzahl der Karten. Muss durch zwei teilbar sein.
 	 */
 	public MemoryModel(int anzahlKarten)
 	{
 		if(anzahlKarten%2 != 0 || anzahlKarten <= 0) 
 		{
 			//TODO: Exception?
 		}
 		
 		this.spielerPunkte = new int[2];
 		this.spielerAmZug = 0;
 		
 		this.karten = new Karte[anzahlKarten];
 		
 		for(int i = 0, c = 1; i < this.karten.length; i+=2, c++)
 		{
 			this.karten[i] = new Karte(c);
 			this.karten[i+1] = new Karte(c);
 		}
 	}
 
 	/**
 	 * Mischt das Spielfeld.
 	 */
 	public void mischeKarten()
 	{
 		Random rand = new Random();
 		
 		for(int i = 0; i < this.karten.length; i++)
 		{
 			int zufall = rand.nextInt(this.karten.length);
 			Karte zwischenspeicher = this.karten[i];
 			this.karten[i] = this.karten[zufall];
 			this.karten[zufall] = zwischenspeicher;
 		}
 	}
 	
 	/**
 	 * Gibt die Nummer des aktuellen Spielers zurck.
 	 * 
 	 * @return Aktueller Spieler.
 	 */
 	public int getSpielerAmZug()
 	{
 		return this.spielerAmZug;
 	}
 
 	/**
 	 * Gibt die Punktzahl beider Spieler zurck.
 	 * 
 	 * @return Array aus zwei Integer-Werte, welche die Punkte der Spieler reprsentieren.
 	 */
 	public int[] getSpielerPunkte()
 	{
 		return this.spielerPunkte;
 	}
 	
 	/**
 	 * Gibt das Spielfeld zurck.
 	 * 
 	 * @return Array aus Karten, welches das Spielfeld reprsentieren.
 	 */
 	public Karte[] getKarten()
 	{
 		return this.karten;
 	}
 	
 	/**
 	 * Prft ob das Spiel zuende ist.
 	 * 
 	 * @return True, wenn alle Karten aufgedeckt, ansonsten False.
 	 */
 	public boolean istAbgeschlossen()
 	{
 		for(int i = 0; i < this.karten.length; i++)
 		{
 			if(!this.karten[i].istAufgedeckt()) return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Versucht, zwei Karten aufzudecken und erhht bei einem richtigen Zug
 	 * die Punkte des aktuellen Spielers.
 	 * Bei einem gltigen Zug ist der nchste Spieler an der Reihe.
 	 * 
 	 * @param ersteKarte 1D-Koordinate der ersten Karte.
 	 * @param zweiteKarte 1D-Koordinate der zweiten Karte.
 	 * 
 	 * @return Integer: -1 bei ungltigen Zug, 0 bei falschen Zug, 1 bei richtigem Zug.
 	 */
 	public int kartenAufdecken(int ersteKarte, int zweiteKarte)
 	{
		if(ersteKarte > this.karten.length || zweiteKarte > this.karten.length) return -1;
 		if(this.karten[ersteKarte].istAufgedeckt()) return -1;
 		if(this.karten[zweiteKarte].istAufgedeckt()) return -1;
 		
 		if(this.karten[ersteKarte].getKarte() == this.karten[zweiteKarte].getKarte())
 		{
 			this.karten[ersteKarte].setAufgedeckt(true);
 			this.karten[zweiteKarte].setAufgedeckt(true);
 			
 			this.spielerPunkte[this.spielerAmZug]++;
 			this.spielerAmZug = (++spielerAmZug)%2;
 			return 1;
 		}
 		else
 		{
 			this.spielerAmZug = (++spielerAmZug)%2;
 			return 0;
 		}
 	}
 }
