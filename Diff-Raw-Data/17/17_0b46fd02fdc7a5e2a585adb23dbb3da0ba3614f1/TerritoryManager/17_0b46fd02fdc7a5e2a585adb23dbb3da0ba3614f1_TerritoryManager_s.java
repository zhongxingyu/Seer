 package domain;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import valueobjects.Continent;
 import valueobjects.Territory;
 
 
 /**
  * 
  * @author Jannes, Timur
  * 
  */
 public class TerritoryManager {
 
 	/**
 	 * Liste mit allen Ländernamen
 	 */
	private String[] laenderNamen = { "Alaska", "Nordwest-Territorium",
 			"Grönland", "Alberta", "Ontario", "Quebec", "Weststaaten",
 			"Oststaaten", "Mittelamerika", "Venezuela", "Peru", "Brasilien",
 			"Argentinien", "Nordwestafrika", "Ägypten", "Ostafrika", "Kongo",
 			"Südafrika", "Madagaskar", "Island", "Skandinavien", "Ukraine",
 			"Großbritannien", "Mitteleuropa", "Westeuropa", "Südeuropa",
 			"Ural", "Sibirien", "Jakutien", "Irkutsk", "Kamtschatka",
 			"Mongolei", "Japan", "Afghanistan", "China", "Mittlerer Osten",
 			"Indien", "Siam", "Indonesien", "Neu-Guinea", "West-Australien",
 			"Ost-Australien" };
 
 	/**
 	 * Eine Liste aller Grenzen, die zwischen jeweils zwei Ländern verlaufen
 	 */
	private static final String[][] grenzen = {
 			{ "Alaska", "Nordwest-Territorium" }, { "Alaska", "Alberta" },
 			{ "Alaska", "Kamtschatka" }, { "Nordwest-Territorium", "Alberta" },
 			{ "Nordwest-Territorium", "Grönland" },
 			{ "Nordwest-Territorium", "Ontario" }, { "Alberta", "Ontario" },
 			{ "Alberta", "Weststaaten" }, { "Ontario", "Oststaaten" },
 			{ "Ontario", "Quebec" }, { "Ontario", "Grönland" },
 			{ "Grönland", "Quebec" }, { "Grönland", "Island" },
 			{ "Weststaaten", "Oststaaten" },
 			{ "Weststaaten", "Mittelamerika" }, { "Oststaaten", "Quebec" },
 			{ "Oststaaten", "Mittelamerika" },
 			{ "Mittelamerika", "Venezuela" }, { "Venezuela", "Peru" },
 			{ "Venezuela", "Brasilien" }, { "Peru", "Brasilien" },
 			{ "Peru", "Argentinien" }, { "Brasilien", "Nordwestafrika" },
 			{ "Brasilien", "Argentinien" }, { "Nordwestafrika", "Westeuropa" },
 			{ "Nordwestafrika", "Südeuropa" }, { "Nordwestafrika", "Ägypten" },
 			{ "Nordwestafrika", "Ostafrika" }, { "Nordwestafrika", "Kongo" },
 			{ "Ägypten", "Mitteleuropa" }, { "Ägypten", "Südeuropa" },
 			{ "Ägypten", "Ostafrika" }, { "Ostafrika", "Mittlerer Osten" },
 			{ "Ostafrika", "Kongo" }, { "Ostafrika", "Südafrika" },
 			{ "Ostafrika", "Madagaskar" }, { "Südafrika", "Kongo" },
 			{ "Südafrika", "Madagaskar" }, { "Westeuropa", "Mitteleuropa" },
 			{ "Westeuropa", "Südeuropa" }, { "Westeuropa", "Großbritannien" },
 			{ "Großbritannien", "Skandinavien" },
 			{ "Großbritannien", "Island" },
 			{ "Großbritannien", "Mitteleuropa" },
 			{ "Mitteleuropa", "Ukraine" }, { "Mitteleuropa", "Skandinavien" },
 			{ "Island", "Skandinavien" }, { "Skandinavien", "Ukraine" },
 			{ "Südeuropa", "Mittlerer Osten" },
 			{ "Mittlerer Osten", "Indien" },
 			{ "Mittlerer Osten", "Afghanistan" },
 			{ "Mittlerer Osten", "Ukraine" }, { "Ukraine", "Afghanistan" },
 			{ "Ukraine", "Ural" }, { "Ural", "Sibirien" },
 			{ "Ural", "Afghanistan" }, { "Ural", "China" },
 			{ "Afghanistan", "China" }, { "Afghanistan", "Indien" },
 			{ "Indien", "China" }, { "Indien", "Siam" },
 			{ "China", "Mongolei" }, { "China", "Siam" },
 			{ "China", "Sibirien" }, { "Sibirien", "Mongolei" },
 			{ "Sibirien", "Irkutsk" }, { "Sibirien", "Kamtschatka" },
 			{ "Jakutien", "Irkutsk" }, { "Jakutien", "Kamtschatka" },
 			{ "Irkutsk", "Kamtschatka" }, { "Japan", "Kamtschatka" },
 			{ "Japan", "Mongolei" }, { "Mongolei", "Kamtschatka" },
 			{ "Siam", "Indonesien" }, { "Indonesien", "Neu-Guinea" },
 			{ "Indonesien", "West-Australien" },
 			{ "West-Australien", "Ost-Australien" },
 			{ "West-Australien", "Neu-Guinea" },
 			{ "Ost-Australien", "Neu-Guinea" } };
 
 	/**
 	 * Liste aller Kontinente
 	 */
 	private Continent[] kontinente = new Continent[6];
 
 	/**
 	 * Liste aller Länderobjekte als Hasthable
 	 */
 	private Hashtable<String, Territory> laender = new Hashtable<String, Territory>();
 
 	/**
 	 * Konstruktor
 	 */
 	public TerritoryManager() {
 		// Länder erstellen
 		for (String name : laenderNamen) {
 			laender.put(name, new Territory(name));
 		}
 
 		// Nachbarländer zuweisen
 		initNachbarlaender();
 
 		
 		/*
 		 * Bonuseinheiten pro Kontinent
 		 * Asien = 7
 		 * Europa = 5
 		 * Nord Amerika = 5
 		 * Süd Amerika = 2
 		 * Afrika = 3
 		 * Australien = 2
 		 */
 		
 		
 		// Nordamerika erstellen
 		kontinente[0] = new Continent("Nordamerika", 5);
 		kontinente[0].addLand(laender.get("Alaska"));
 		kontinente[0].addLand(laender.get("Nordwest-Territorium"));
 		kontinente[0].addLand(laender.get("Grönland"));
 		kontinente[0].addLand(laender.get("Alberta"));
 		kontinente[0].addLand(laender.get("Ontario"));
 		kontinente[0].addLand(laender.get("Quebec"));
 		kontinente[0].addLand(laender.get("Weststaaten"));
 		kontinente[0].addLand(laender.get("Oststaaten"));
 		kontinente[0].addLand(laender.get("Mittelamerika"));
 
 		// Südamerika erstellen
 		kontinente[1] = new Continent("Südamerika", 2);
 		kontinente[1].addLand(laender.get("Venezuela"));
 		kontinente[1].addLand(laender.get("Peru"));
 		kontinente[1].addLand(laender.get("Brasilien"));
 		kontinente[1].addLand(laender.get("Argentinien"));
 
 		// Afrika erstellen
 		kontinente[2] = new Continent("Afrika", 3);
 		kontinente[2].addLand(laender.get("Nordwestafrika"));
 		kontinente[2].addLand(laender.get("Ägypten"));
 		kontinente[2].addLand(laender.get("Ostafrika"));
 		kontinente[2].addLand(laender.get("Kongo"));
 		kontinente[2].addLand(laender.get("Südafrika"));
 		kontinente[2].addLand(laender.get("Madagaskar"));
 
 		// Europa erstellen
 		kontinente[3] = new Continent("Europa", 5);
 		kontinente[3].addLand(laender.get("Island"));
 		kontinente[3].addLand(laender.get("Skandinavien"));
 		kontinente[3].addLand(laender.get("Ukraine"));
 		kontinente[3].addLand(laender.get("Großbritannien"));
 		kontinente[3].addLand(laender.get("Mitteleuropa"));
 		kontinente[3].addLand(laender.get("Westeuropa"));
 		kontinente[3].addLand(laender.get("Südeuropa"));
 
 		// Asien erstellen
 		kontinente[4] = new Continent("Asien", 7);
 		kontinente[4].addLand(laender.get("Ural"));
 		kontinente[4].addLand(laender.get("Sibirien"));
 		kontinente[4].addLand(laender.get("Jakutien"));
 		kontinente[4].addLand(laender.get("Irkutsk"));
 		kontinente[4].addLand(laender.get("Kamtschatka"));
 		kontinente[4].addLand(laender.get("Mongolei"));
 		kontinente[4].addLand(laender.get("Japan"));
 		kontinente[4].addLand(laender.get("Afghanistan"));
 		kontinente[4].addLand(laender.get("China"));
 		kontinente[4].addLand(laender.get("Mittlerer Osten"));
 		kontinente[4].addLand(laender.get("Indien"));
 		kontinente[4].addLand(laender.get("Siam"));
 
 		// Australien erstellen
 		kontinente[5] = new Continent("Australien", 2);
 		kontinente[5].addLand(laender.get("Indonesien"));
 		kontinente[5].addLand(laender.get("Neu-Guinea"));
 		kontinente[5].addLand(laender.get("West-Australien"));
 		kontinente[5].addLand(laender.get("Ost-Australien"));
 	}
 
 	/**
 	 * Berechnet aus der Grenzliste für jedes Land alle benachbarten Länder
 	 */
 	private void initNachbarlaender() {
 		Territory land1;
 		Territory land2;
 		for (String[] grenze : grenzen) {
 			// Land-Objekte holen
 			land1 = getLandByName(grenze[0]);
 			land2 = getLandByName(grenze[1]);
 			// Gegenseitig als Nachbarn hinzufügen
 			land1.addNachbar(land2);
 			land2.addNachbar(land1);
 		}
 	}
 
 	/**
 	 * Sucht nach einem Land
 	 * 
 	 * @param name
 	 *            Landname als String
 	 * @return Landobjekt
 	 */
 	public Territory getLandByName(String name) {
 		return laender.get(name);
 	}
 
 	/**
 	 * Liefert ein Land durch Angabe einer Zahl
 	 * 
 	 * @param number
 	 * @return Land mit dieser Nummer
 	 */
 	public Territory getLandByNumber(int number) {
 		// TODO: diese Methode sollte es eigentlich nicht geben
 		return getLandByName(laenderNamen[number]);
 	}
 
 	public boolean isAlleLaenderBesetzt() {
 
 		for (int i = 0; i < 41; i++) {
 			if(getLandByName(laenderNamen[i]).getBesitzer() == null){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public ArrayList<Continent> getConqueredContinents(ArrayList<Territory> countries) {
 		// Array für das Ergebnis
 		ArrayList<Continent> conqueredContinents = new ArrayList<Continent>();
 		
 		//durchläuft jeden kotninent[] und überprüft den Besitzer
 		Continent currentContinent;
 		for(int i = 0; i < kontinente.length; i++) {
 			currentContinent = kontinente[i];
 			
 			// Überprüft, ob die Länderliste den kompletten Kontinent enthält
 			if (countries.containsAll(currentContinent.getCountries())) {
 				conqueredContinents.add(currentContinent);
 			}
 		}
 		
 		return conqueredContinents;
 		
 	}
 
 	
 }
