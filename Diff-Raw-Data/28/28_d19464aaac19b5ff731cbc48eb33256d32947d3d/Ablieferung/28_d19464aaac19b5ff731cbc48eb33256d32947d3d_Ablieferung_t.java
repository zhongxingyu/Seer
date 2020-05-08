 package Automat;
 
 import java.math.BigDecimal;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import Fassade.Fassade;
 
 
 /**
  * @author Dennis
  * @version 1.0
  * @created 26-Okt-2012 07:10:54
  */
 public class Ablieferung extends Thread{
 
 	private boolean ButtonPressed = false;
 	/**
 	 * in Sekunden
 	 */
 	private int Timeout = 4;
 	private Annahme m_Annahme;
 	private Verteilung m_Verteilung;
 	private Anzeige m_Anzeige;
 	private BonDrucker m_BonDrucker;
 	private Sensor m_Druckknopf;
 	private LinkedList<Flasche> abgelieferteFlaschen;
 	private FlaschenZaehler m_KundenZaehler;
 	private FlaschenZaehler m_TagesZaehler;
 	private BigDecimal Guthaben = new BigDecimal(0.0);
 	private Fassade DieFassade;
 	private Scanner m_scanner;
 	private ParallelWarteClass workerThread;
 	public boolean BonButtonPresst = false;
 	
 	public Ablieferung(Fassade fassade, LinkedList<Flasche> ListofBottlesTag, LinkedList<Flasche> ListofBottlesKunde){
 		DieFassade = fassade;
 		m_Annahme = new Annahme();
 		m_Verteilung = new Verteilung();
 		m_Anzeige = new Anzeige(fassade);
 		m_BonDrucker= new BonDrucker(Adressen.BonDrucker.ordinal());
 		m_Druckknopf = new Sensor(Adressen.Druckknopf.ordinal());
 		//m_FlaschenZaehlerSubject = new FlaschenAbrechnungSubject();
 		m_scanner = new Scanner(Adressen.Scanner.ordinal(),10000);
 		m_KundenZaehler = new FlaschenZaehler(m_scanner,ListofBottlesTag);
 		m_TagesZaehler = new FlaschenZaehler(m_scanner,ListofBottlesKunde);
 		m_scanner.attach(m_KundenZaehler);
 		m_scanner.attach(m_TagesZaehler);
 		HWSimulation HWaccess = HWSimulation.getInstance();
 		HWaccess.write(Adressen.Leuchte_Frabe.ordinal(), 2);
 		workerThread = new ParallelWarteClass(1000);
 	}
 
 	public void AbbruchDurchButton(){
 
 	}
 	
 	public void run()
 	{
 		ablaufFlaschenablieferung();
 	}
 	
 	/**
 	 * Hier ist der UseCase Ablauf fr eine Flaschenablieferung niedergeschrieben.
 	 */
 	public void ablaufFlaschenablieferung(){
 		
 		while (true)
 		{
 			System.out.println("[ablaufFlaschenablieferung] enter main loop");
			
			// -1 alles i.o.
			m_Anzeige.FehlerMelden(-1);
			
 			DisplayAktualisieren("Warten auf Flasche.");
 			wartenAufFlasche();
 			DisplayAktualisieren("Lese Flaschencode.");
 			if (m_Annahme.Flasche_positionieren())
 			{
 				int flaschenanzahl = m_KundenZaehler.getGesamtAnzahlFlaschen();
 				
 				int versuche = 0;
 				boolean scan = false;
 				
 				while (!scan && versuche <= 6)
 				{
 					scan = m_scanner.Scan();
 					DieFassade.simKonsolenText(0, "Ablieferung: Flasche nicht erkannt.");
 					DieFassade.simKonsolenText(0, "Ablieferung: Flasche nach rechts drehen.");
 					m_Annahme.flascheDrehenRechts(300);
 					versuche++;
 				}
 				
 				//---------->>
 				// nur fr simulation, scanner kann einmal nix lesen, damit das drehband zum einsatz kommt
 				m_scanner.resetMissCounter();
 				//<<<---------
 				
 				if (scan)
 				{
 					//wenn fllstand nicht voll ist
 					if (!checkFuellstand(m_scanner.lastCode))
 					{
 						
 						//anzahl kontrollieren
 						if (m_scanner.lastCode != FlaschenType.CodeNichtValide)
 						{
 							DisplayAktualisieren("Flasche erkannt, bitte warten.");
 							DieFassade.simKonsolenText(0, "Ablieferung: Flasche weiterleiten.");
 							if (m_Annahme.flascheWeiterLeiten())
 							{
 								if (m_Verteilung.Flasche_weiterleiten(m_scanner.lastCode))
 								{
 									m_scanner.notifyObservers(); // Flaschenzhler aktualisieren
 									DieFassade.simKonsolenText(0, "Ablieferung: Flasche erfolgreich abgegeben.");
 									Guthaben = m_KundenZaehler.getGesamtGuthaben();
 									Guthaben.setScale(2,BigDecimal.ROUND_HALF_UP);
 									
 									// Display Aktualisieren
 									DisplayAktualisieren("Flasche gutgeschrieben.");
 									
 								}
 								else
 								{
 									DieFassade.simKonsolenText(0, "Ablieferung: Flasche konnte nicht an den Endbeklter "+
 																	m_KundenZaehler.getLastFlaschenType().toString() +
 																	" weitergeleitet werden.");
 									flascheZurueckGeben();
 								}
 							}
 							else
 							{
 								DieFassade.simKonsolenText(0, "Ablieferung: Flasche konnte nicht an die Auswahlklappe weitergeleitet werden.");
 								flascheZurueckGeben();
 							}
 						}
 						else
 						{
 							DieFassade.simKonsolenText(0, "Ablieferung: Flaschencode ist nicht im System.");
 							flascheZurueckGeben();
 						}
 					}
 					else
 					{
 						DisplayAktualisieren("Der "+ m_scanner.lastCode.toString() +" Behlter ist voll.");
 						DieFassade.simKonsolenText(0, "Der "+ m_scanner.lastCode.toString() +" Behlter ist voll.");
 						flascheZurueckGeben();
 					}
 				}
 				else
 				{
 					DieFassade.simKonsolenText(0, "Ablieferung: Flasche Code ist nicht im System.");
 					flascheZurueckGeben();
 				}
 					
 			}
 			else
 			{
 				DieFassade.simKonsolenText(0, "Ablieferung: Flasche konnte nicht positioniert werden.");
 				flascheZurueckGeben();
 			}
 		}
 		
 		
 	}
 
 	/**
 	 * 
 	 * @param FlaschenType
 	 */
 	public void addGuthaben(FlaschenType FlaschenType){
 
 	}
 
 	public void DisplayAktualisieren(String info)
 	{
 		String str = "";
 		str += "Guthaben:          " + NumberFormat.getCurrencyInstance().format(Guthaben) + "\n";
 		str += info;
 		DieFassade.displayAktualisieren(str);
 	
 	}
 	
 
 	public void flascheZurueckGeben()
 	{
 		Sensor s_EingangsLichtschranke = new Sensor(Adressen.Eingangslichtschranke.ordinal());
 		
 		System.out.println("[flascheZurueckGeben] enter");
 		
 		DieFassade.simKonsolenText(0, "Ablieferung: Flasche wird zurck gegeben.");
 		if (m_Annahme.Flasche_auswerfen())
 		{
 			System.out.println("[flascheZurueckGeben] Flasche_auswerfen erfolgreich");
 			DisplayAktualisieren("Bitte Flasche entnehmen.");
 			
 			// warte Zeit ndern in 10sec.
 			workerThread = new ParallelWarteClass(10000);
 
 			// warte Thread starten
 			workerThread.start();
 			
 			System.out.println("[flascheZurueckGeben] warten auf entnahme der Flasche");
 			
 			// auf entnahme der flasche warten oder timeout 10sec.
 			while (s_EingangsLichtschranke.read() && workerThread.isAlive()) {
 				try {
 					Thread.sleep(300);
 				} catch (InterruptedException e) {
 					// do nothing
 				}
 			}
 			
 			// flasche wurde nicht rechtzeitig zurck entnommen
 			if (!workerThread.isAlive()){
 				System.out.println("[flascheZurueckGeben] Flasche wurde nicht im Timeout entnommen");
 				m_Anzeige.FehlerMelden(1);
 				// now wait infinite for Eingangslichtschrnake release
 				System.out.println("[flascheZurueckGeben] unendliches warten auf entnahme der Flasche");
 				while(s_EingangsLichtschranke.read()){
 					try {
 						Thread.sleep(300);
 					} catch (InterruptedException e) {
 						// do nothing
 					}
 				}
 			}
 		}else{
 			//fehlerfall
 			System.out.println("[flascheZurueckGeben] Flasche_auswerfen returned false!");
 			DisplayAktualisieren("Es ist ein Fehler aufgetreten!");
 			m_Anzeige.FehlerMelden(1);
 			// was nun warten??? <<<<<<<<<----------------------------
 		}
 	}
 
 	/**
 	 * 
 	 * @param Flaschen
 	 */
 	public void clearList(LinkedList<Flasche> ListofBottles){
 		ListofBottles.clear();
 	}
 
 	public void timeout(){
 
 	}
 		
 	
 	public void wartenAufFlasche(){
 
 		while (! m_Annahme.getEingangsLichtschranke())
 		{
 			ParallelWarteClass warte = new ParallelWarteClass(300);
 
 			if (BonButtonPresst)
 			{
 				// Guthaben errechnen, Bon Drucken, Flaschenzhler leeren
 				m_BonDrucker.BonDrucken(m_KundenZaehler.getFlaschenListe());
 				m_KundenZaehler.reset();
 				BonButtonPresst = false;
 				//workerThread.wait(5000);
 			}
 
 			warte.start();
 			while(warte.isAlive());
 		}
 	}
 	
 	private boolean checkFuellstand(FlaschenType fType)
 	{
 		switch(fType)
 		{
 		case PET:
 			return m_Verteilung.s_FuellstandPET.read();
 		case Mehrweg:
 			return m_Verteilung.s_FuellstandMehrweg.read();
 		default:
 			return false;
 		}			
 	}
 
 	/**
 	 * 
 	 * @exception Throwable
 	 */
 	public void finalize()
 	  throws Throwable{
 
 	}
 
 }
