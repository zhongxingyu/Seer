 package de.uni_hamburg.informatik.sep.zuul;
 
 import java.util.Arrays;
 
 import javax.swing.SwingUtilities;
 
 import de.uni_hamburg.informatik.sep.zuul.befehle.Befehl;
 import de.uni_hamburg.informatik.sep.zuul.befehle.BefehlFactory;
 import de.uni_hamburg.informatik.sep.zuul.spiel.SpielKontext;
 import de.uni_hamburg.informatik.sep.zuul.spiel.SpielLogik;
 import de.uni_hamburg.informatik.sep.zuul.spiel.TextVerwalter;
 
 /**
  * Dies ist die Hauptklasse der Anwendung "Die Welt von Zuul". "Die Welt von
  * Zuul" ist ein sehr einfaches, textbasiertes Adventure-Game. Ein Spieler kann
  * sich in einer Umgebung bewegen, mehr nicht. Das Spiel sollte auf jeden Fall
  * ausgebaut werden, damit es interessanter wird!
  * 
  * Zum Spielen muss eine Instanz dieser Klasse erzeugt werden und an ihr die
  * Methode "spielen" aufgerufen werden.
  * 
  * Diese Instanz erzeugt und initialisiert alle anderen Objekte der Anwendung:
  * Sie legt alle Räume und einen Parser an und startet das Spiel. Sie wertet
  * auch die Befehle aus, die der Parser liefert, und sorgt für ihre Ausführung.
  * 
  * Das Ausgangssystem basiert auf einem Beispielprojekt aus dem Buch
  * "Java lernen mit BlueJ" von D. J. Barnes und M. Kölling.
  */
 public abstract class Spiel implements ISchreiber
 {
 	protected SpielKontext _kontext;
 
 	/**
 	 * Schablonenmethode für Aktionen bei beendetem Spiel.
 	 */
	public void beendeSpiel()
 	{
 
 	}
 
 	/**
 	 * Führt das Spiel aus.
 	 */
 	protected void spielen()
 	{
 		_kontext = SpielLogik.erstelleKontext();
 
 		zeigeWillkommenstext(_kontext);
 	}
 
 	/**
 	 * Gibt einen Begrüßungstext für den Spieler aus.
 	 */
 	protected void zeigeWillkommenstext(SpielKontext kontext)
 	{
 		schreibeNL(TextVerwalter.EINLEITUNGSTEXT);
 		schreibeNL("");
 		SpielLogik.zeigeRaumbeschreibung(kontext);
 		SpielLogik.zeigeAusgaenge(kontext);
 	}
 
 	protected void verarbeiteEingabe(String eingabezeile)
 	{
 		//		String eingabezeile = leseZeileEin();
 
 		Befehl befehl = parseEingabezeile(eingabezeile);
 		befehl.ausfuehren(_kontext);
 
		if(!_kontext.isSpielZuende())
			_kontext.fireTickEvent();
 	}
 
 	//	protected abstract String leseZeileEin();
 
 	protected void restart()
 	{
 		spielen();
 	}
 
 	/**
 	 * @param eingabezeile
 	 * @return geparster Befehl
 	 */
 	public static Befehl parseEingabezeile(String eingabezeile)
 	{
 		String[] input = eingabezeile.split(" +");
 
 		String[] parameter = new String[0];
 		String befehl = "";
 
 		if(input.length > 0)
 		{
 			befehl = input[0];
 			if(input.length > 1)
 				parameter = Arrays.copyOfRange(input, 1, input.length);
 
 		}
 
 		return BefehlFactory.get(befehl, parameter);
 	}
 
 	/**
 	 * Privates Klassenattribut, wird beim erstmaligen Gebrauch (nicht beim
 	 * Laden) der Klasse erzeugt
 	 */
 	private static Spiel instance;
 
 	/** Konstruktor ist privat, Klasse darf nicht von außen instanziiert werden. */
 	protected Spiel()
 	{
 	}
 
 	/**
 	 * Statische Methode „getInstance()“ liefert die einzige Instanz der Klasse
 	 * zurück. Ist synchronisiert und somit thread-sicher.
 	 */
 	public synchronized static Spiel getInstance()
 	{
 		if(instance == null)
 		{
 			if(!Programm.isOnconsole())
 			{
 				instance = new SpielGUI();
 			}
 			else
 			{
 				instance = new SpielConsole();
 			}
 		}
 		return instance;
 	}
 }
