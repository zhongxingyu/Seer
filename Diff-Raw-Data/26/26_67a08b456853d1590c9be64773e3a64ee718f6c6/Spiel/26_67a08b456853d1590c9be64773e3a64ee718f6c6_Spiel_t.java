 package de.uni_hamburg.informatik.sep.zuul;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.Arrays;
 
 import javax.swing.SwingUtilities;
 
 import de.uni_hamburg.informatik.sep.zuul.befehle.Befehl;
 import de.uni_hamburg.informatik.sep.zuul.gui.HauptfensterWerkzeug;
 
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
 public class Spiel
 {
 	private Parser _parser;
 	private SpielKontext _kontext;
 	private HauptfensterWerkzeug _hauptwerkzeug;
 
 	/**
 	 * Erzeugt ein Spiel und initialisiert die interne Raumkarte.
 	 * 
 	 * @param in
 	 *            InputStream
 	 * @param out
 	 *            OutputStream
 	 */
 	public Spiel()
 	{
 		
 		_hauptwerkzeug = new HauptfensterWerkzeug();
 
 		
 		_parser = new Parser();
 
 		_kontext = new SpielKontext(this);
 
		
		
 		_hauptwerkzeug.addNeueEingabeListener(new ActionListener()
 		{
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				String str = _hauptwerkzeug.leseEingabeString();
 				
 				Befehl befehl = _parser.liefereBefehl(str);
 				befehl.ausfuehren(_kontext);
 				
 				if(_kontext.isSpielZuende())
 					beendeSpiel();
 				
 			}
 		});
 	}
 	
 
 
 	private void beendeSpiel()
 	{
 
 		_kontext.schreibeNL("Danke für dieses Spiel. Auf Wiedersehen.");
 		//TODO: Hauptfenster ausschalten (wie auch immer) Buttons + Eingabe sperren
 	}
 	
 	/**
 	 * Führt das Spiel aus.
 	 */
 	public void spielen()
 	{
 		_hauptwerkzeug.zeigeFenster();
 		
 		zeigeWillkommenstext();
 	}
 
 	/**
 	 * Gibt einen Begrüßungstext für den Spieler aus.
 	 */
 	private void zeigeWillkommenstext()
 	{
 		_kontext.schreibeNL("");
 		_kontext.schreibeNL("Willkommen zu Zuul!");
 		_kontext.schreibeNL("Zuul ist ein neues, unglaublich langweiliges Spiel.");
 		_kontext.schreibeNL("Tippen sie 'help', wenn Sie Hilfe brauchen.");
 		_kontext.schreibeNL("");
 		_kontext.zeigeRaumbeschreibung();
 	}
 
 	/**
 	 * main-Methode zum Ausführen.
 	 */
 	public static void main(String[] args)
 	{
 		SwingUtilities.invokeLater(new Runnable()
 		{			
 			@Override
 			public void run()
 			{
 				Spiel spiel = new Spiel();
 
 				spiel.spielen();				
 			}
 		});
 
 	}
 
 
 
 	public void schreibeNL(String nachricht)
 	{
 		_hauptwerkzeug.schreibeNL(nachricht);
 		
 	}
 
 
 
 	public void schreibe(String nachricht)
 	{
 		_hauptwerkzeug.schreibe(nachricht);
 		
 	}
 }
