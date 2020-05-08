 import java.util.ArrayList;
 import java.util.Vector;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 
 
 public class Kolonie extends Thread {
 
 	private Vector<Ameise> ameisenVector;
 	private int anzahlAmeisen;
 	private int pheromonUpdate;
 	private double verdunstungsGradPheromon;
 	private TspBerechnungsdaten tspBerechnungsdaten;
 	private Abbruchbedingungen abbruchbedingungen;
 	
 	private double besteLaengeIteration;
 	private ArrayList<Integer> besteRouteIterationArrayList;
 	private double gesamtstreckeAmeisenIteration;
 	
 	private double besteLaengeGlobal;
 	private ArrayList<Integer> besteRouteGlobalArrayList;
 	private double durchschnittGlobal;
 	
 	private ThreadGroup ameisenGruppeThreadGroup;
 	private ConcurrentLinkedQueue<Ameise> fertigeAmeisenWarteschlangeConcurrentLinkedQueue;
 	private GrafikPanel grafikPanel;
 	private StatistikPanel statistikPanel;
 	private double pheromonWert;
 	private int aktuelleAnzahlIteration;
 	private long anfangszeit;
 	
 	public Kolonie(int anzahlAmeisen, double verdunstungsGradPheromon, int initialePheromonwerte, int pheromonUpdate, double pheromonWert, TspBerechnungsdaten tspBerechnungsdaten, Abbruchbedingungen abbruchbedingungen, GrafikPanel grafikPanel, StatistikPanel statistikPanel) throws IllegalArgumentException {
 		if (tspBerechnungsdaten == null) {
 			throw new IllegalArgumentException("Es wurden keine Daten von Stdten gefunden. Bitte whlen Sie ein TSP zum Berechnen aus.");
 		}
 		
 		this.anzahlAmeisen = anzahlAmeisen;
 		this.verdunstungsGradPheromon = verdunstungsGradPheromon;
 		this.pheromonUpdate = pheromonUpdate;
 		this.pheromonWert = pheromonWert;
 		this.tspBerechnungsdaten = tspBerechnungsdaten;
 		this.abbruchbedingungen = abbruchbedingungen;
 		this.grafikPanel = grafikPanel;
 		this.statistikPanel = statistikPanel;
 		tspBerechnungsdaten.initialisiereBerechnungsdaten(initialePheromonwerte);
 
 		ameisenVector = new Vector<Ameise>();
 		besteLaengeIteration = Double.MAX_VALUE;
 		besteRouteIterationArrayList = null;
 		gesamtstreckeAmeisenIteration = 0;
 		
 		besteLaengeGlobal = Double.MAX_VALUE;
 		besteRouteGlobalArrayList = new ArrayList<Integer>(0);
 		durchschnittGlobal = 0;
 		
 		ameisenGruppeThreadGroup = new ThreadGroup("ameisenGruppeThreadGroup");
 		fertigeAmeisenWarteschlangeConcurrentLinkedQueue = new ConcurrentLinkedQueue<Ameise>();
 		
 		this.start();
 	}
 	
 	@Override
 	public void run() {
 		anfangszeit = System.currentTimeMillis();
 		
 		aktuelleAnzahlIteration = 0;
 		//TODO Abbruchbedingungen!
 		double aktuelleDurchschnittlicheTourenLaenge = Double.MAX_VALUE;
 
 		while (!abbruchbedingungen.sindAbbruchbedingungenErfuellt(aktuelleAnzahlIteration , besteLaengeGlobal , aktuelleDurchschnittlicheTourenLaenge , besteRouteGlobalArrayList)) {
			//System.out.println("Kolonie.run() "+ aktuelleAnzahlIteration);
 			// Anlegen der Ameisen
 			legeAmeisenAn();
 
 			// Laufenlassen der Ameisen
 			lassAmeisenLaufen();
 
 			// Ameisen in empfang nehmen
 			empfangeAmeise();
 
 			// Pheromonupdate
 			aktualisierePheromon();
 
 			
 			//nchste Iteration vorbereiten
 			durchschnittGlobal = berechneDurchschnittAktualisiereAnsicht(anzahlAmeisen);
 			
 			aktuelleDurchschnittlicheTourenLaenge = durchschnittGlobal; // TODO Verbesserung
 			setzeErgebnisseZurueck();
 			aktuelleAnzahlIteration++;
 		}
		//System.out.println("Kolonie.run() Ende");
 	}
 	
 	private void legeAmeisenAn() {
 		for (int i = 0; i < anzahlAmeisen; i++) {
 			ameisenVector.add(new Ameise(ameisenGruppeThreadGroup, i, pheromonWert, tspBerechnungsdaten, this));
 		}
 	}
 	
 	private void lassAmeisenLaufen() {
 		// TODO nicht alle gleichzeitig laufen lassen
 		for (int i = 0; i < anzahlAmeisen; i++) {
 			ameisenVector.get(i).start();
 		}
 		
 	}
 
 	private void empfangeAmeise() {
 		int bereitsEmpfangeneAmeisen = 0;
 		while (bereitsEmpfangeneAmeisen < anzahlAmeisen) {
 			
 			if (! fertigeAmeisenWarteschlangeConcurrentLinkedQueue.isEmpty()) {
 				// wenn noch Ameisen auf Behandlung warten
 				Ameise aktuelleAmeise = fertigeAmeisenWarteschlangeConcurrentLinkedQueue.poll();
 				
 				bereitsEmpfangeneAmeisen++;
 				vergleicheRouten(aktuelleAmeise, bereitsEmpfangeneAmeisen);
 			} else {
 				// CPU freigeben, damit Ameisen laufen knnen
 				Kolonie.yield();
 			}
 		}
 	}
 
 	private void vergleicheRouten(Ameise aktuelleAmeise, int bereitsEmpfangeneAmeisen) {
 		double aktuelleAmeiseRouteLaenge = aktuelleAmeise.getRouteLaenge();
 		gesamtstreckeAmeisenIteration = gesamtstreckeAmeisenIteration + aktuelleAmeiseRouteLaenge;
 		
 		if (besteLaengeIteration > aktuelleAmeiseRouteLaenge) {
 			besteLaengeIteration = aktuelleAmeiseRouteLaenge;
 			besteRouteIterationArrayList = aktuelleAmeise.getRoute();
 			
 			if (besteLaengeGlobal > besteLaengeIteration) {
 				besteLaengeGlobal = besteLaengeIteration;
 				besteRouteGlobalArrayList = besteRouteIterationArrayList;
 			}
 			
 			double aktuellerDurchschnittGlobal = berechneDurchschnittAktualisiereAnsicht(bereitsEmpfangeneAmeisen);
 			
 			if (abbruchbedingungen.sindAbbruchbedingungenErfuellt(besteLaengeIteration, aktuellerDurchschnittGlobal, besteRouteIterationArrayList)) {
 				// TODO Implementierung
				//System.out.println("Kolonie.vergleicheRouten() = Abbruch");
 			}
 		}
 	}
 
 	private double berechneDurchschnittAktualisiereAnsicht(int bereitsEmpfangeneAmeisen) {
 		double durchschnittIteraion = berechneDurchschnittIteration(bereitsEmpfangeneAmeisen);
 		double aktuellerDurchschnittGlobal = berechneDurchschnittGlobal(durchschnittIteraion);
 		
 		aktualisiereAnsicht(durchschnittIteraion, aktuellerDurchschnittGlobal);
 		return aktuellerDurchschnittGlobal;
 	}
 
 	private double berechneDurchschnittIteration(int bereitsEmpfangeneAmeisen) {
 		return gesamtstreckeAmeisenIteration / bereitsEmpfangeneAmeisen;
 	}
 
 	private double berechneDurchschnittGlobal(double durchschnittIteraion) {
 		double neuerDurchschnittGlobal = (((durchschnittGlobal*aktuelleAnzahlIteration)+durchschnittIteraion)/(aktuelleAnzahlIteration+1));
 		return neuerDurchschnittGlobal;
 	}
 
 	private void aktualisiereAnsicht(double aktuellerDurchschnittIteraion, double aktuellerDurchschnittGlobal) {
 		long aktuellVergangeneZeit = System.currentTimeMillis() - anfangszeit;
 		grafikPanel.aktualisiereGrafik(besteRouteIterationArrayList, besteRouteGlobalArrayList);
 		statistikPanel.aktualisiereStatistik(besteLaengeIteration, besteRouteIterationArrayList, aktuellerDurchschnittIteraion, besteLaengeGlobal, besteRouteGlobalArrayList, aktuellerDurchschnittGlobal, aktuellVergangeneZeit);
 	}
 	
 	
 	private void aktualisierePheromon() {
 		//verdunsten des Pheromons aus vorherigen Iterationen
 		tspBerechnungsdaten.verdunstePheromon(verdunstungsGradPheromon);
 
 		//setzen des Pheromons aus aktueller Iteration
 		for (int i = 0; i < anzahlAmeisen; i++) {
 			Ameise aktuelleAmeise = ameisenVector.get(i);
 			double pheromonProLaengenEinheit = pheromonUpdate / aktuelleAmeise.getRouteLaenge();
 			ArrayList<Integer> aktuelleRouteAktuellerAmeise = aktuelleAmeise.getRoute();
 			
 			for (int j = 0; j < aktuelleRouteAktuellerAmeise.size() - 1; j++) {
 				tspBerechnungsdaten.addierePheoromonengehalt(pheromonProLaengenEinheit, aktuelleRouteAktuellerAmeise.get(j), aktuelleRouteAktuellerAmeise.get(j+1));
 			}
 		}
 	}
 
 	private void setzeErgebnisseZurueck() {
 		ameisenVector.clear();
 		besteLaengeIteration = Double.MAX_VALUE;
 		besteRouteIterationArrayList = null;
 		gesamtstreckeAmeisenIteration = 0;
 	}
 	
 	
 	public boolean fuegeAmeisenHinzu(Ameise fertigeAmeise) {
 		
 		return fertigeAmeisenWarteschlangeConcurrentLinkedQueue.offer(fertigeAmeise);
 	}
 
 	
 }
