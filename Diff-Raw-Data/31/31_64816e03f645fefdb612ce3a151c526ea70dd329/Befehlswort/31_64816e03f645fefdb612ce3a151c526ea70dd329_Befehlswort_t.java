 /**
  * Diese Klasse haelt eine Aufzaehlung der akzeptierten Befehlswoerter.
  * Mit ihrer Hilfe werden eingetippte Befehle erkannt.
  *
  * @author  tebe, Dave Kramer, Simon Schwarz
  * @version 1.1
  */
 public enum Befehlswort {
 
 	UNBEKANNT("unbekannt"),
 	GEHE("gehe"),
 	HILFE("hilfe"),
 	BEENDEN("beenden");
 
 	private String befehl;
 	
 	Befehlswort(String befehl){
 		this.befehl = befehl;
 	}
 	
	/**
	 * Überprüft ob der übergebene String ein gültiges Befehlswort ist und retourniert diesen allenfalls.
	 * @param wort Usereingabe zur überprüfung
	 * @return gültiges Befehlswort Objekt
	 */
 	public static Befehlswort gibBefehlsWort(String wort) {
 		for (Befehlswort befehlswort : Befehlswort.values()) {
 			if (befehlswort.getBefehlswort().equals(wort))
 			{
 				return befehlswort;
 			}
 		}
 		return Befehlswort.UNBEKANNT;
 	}
 	
	/**
	 * Gibt alle möglichen Befehlswörter als String zurück.
	 * @return	String aller Befehlswörter
	 */
 	public static String gibBefehlsWorteAlsText() {
 		String returnString = "";
 		for (Befehlswort befehlswort : Befehlswort.values())
 		{
 			returnString += befehlswort.getBefehlswort() + ", ";
 		}
 		return returnString;
 	}
 	
	/**
	 * Gibt einen einzelnen Befehl zurück
	 * @return aktuellen Befehl
	 */
 	public String getBefehlswort() {
 		return befehl;
 	}
 	
	/**
	 * Legt einen neuen Befehl fest.
	 * @param befehl Der zu setztende Befehl
	 */
	public void setBefehlswort(String befehl) {
 		this.befehl = befehl;
 	}
 }
