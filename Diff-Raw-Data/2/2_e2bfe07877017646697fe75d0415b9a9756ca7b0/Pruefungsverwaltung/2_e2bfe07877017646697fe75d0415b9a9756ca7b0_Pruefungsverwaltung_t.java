 import java.util.HashMap;
 import java.util.HashSet;
 
 /**
  * Bietet Funktionalitaeten zum Speichern von Pruefungsergebnissen von einer
  * Vielzahl von Studenten. Aus den gespeicherten Ergebnissen lassen sich
  * personalisierte Antworttext generieren.
  */
 public class Pruefungsverwaltung {
 
 	HashSet<String> studentenErgebnis = new HashSet<String>();
 	HashMap<Double, String> dictionaryDoubleText = new HashMap<Double, String>();
 	
 	/*
 	 * Setzt eine HashMap mit Uebersetzungen von Zahl zu Text auf.
 	 */
 	public Pruefungsverwaltung () {
 		//Dictionary aufsetzen
 		dictionaryDoubleText.put(1.0, "eins");
 		dictionaryDoubleText.put(1.5, "eins punkt fuenf");
 		dictionaryDoubleText.put(2.0, "zwei");
 		dictionaryDoubleText.put(2.5, "zwei punkt fuenf");
 		dictionaryDoubleText.put(3.0, "drei");
 		dictionaryDoubleText.put(3.5, "drei punkt fuenf");
 		dictionaryDoubleText.put(4.0, "vier");
 		dictionaryDoubleText.put(4.5, "vier punkt fuenf");
 		dictionaryDoubleText.put(5.0, "fuenf");
 		dictionaryDoubleText.put(5.5, "fuenf punkt fuenf");
 		dictionaryDoubleText.put(6.0, "sechs");
 	}
 	
 /**
 * Speichert ein Pruefungsergebnis.
 * 
 * @param ergebnis Das Pruefungsergebnis.
 */
   public void speichereErgebnis(Pruefungsergebnis ergebnis) {
 	  pruefungsergebnisToHashSet(ergebnis);
   }
 
   /**
    * Gibt pro gespeichert Ergebnis einen Text auf die Konsole aus.
    * Je nachdem ob der Kandidate die Pruefung bestanden (>= 4.0) oder nicht
    * bestanden (< 4.0) hat, wird ein Text in folgendem Format ausgegeben:
    * 
    * Sie haben an der Pruefung eine 3.0 (drei punkt null) erzielt und 
    * sind somit durchgefallen!
    * 
    * Herzliche Gratulation Max Muster! Sie haben an der Pruefung eine 4.5
    * (vier pounkt fuenf) erzielt und somit bestanden!
    */
   	
   public void druckeAntworttexte() {
 	  for (String studentErgebnis : studentenErgebnis)
 	  {
 		  System.out.println(studentErgebnis);
 	  }
   }
 
  //Rundet die uebergebene Zahl auf 0.5
   private double rundeAufHalbeNote(double note) {
     return Math.round(note * 2) / 2.0;
   }
   
   /*
   * Speichert den Antworttext des Pruefungsergebnisses in ein HashSet 
   */
   private void pruefungsergebnisToHashSet(Pruefungsergebnis ergebnis) {
 
 	  String antwortText = "";
 	  
 	  if (ergebnis.getNote() < 4.0)
 	  {
 		 antwortText =  ergebnis.getStudent() + ", Sie haben an der Pruefung eine " +
 				 		rundeAufHalbeNote(ergebnis.getNote()) + " (" +
 				 		dictionaryDoubleText.get(rundeAufHalbeNote(ergebnis.getNote())) + ") " +
 				 		"erzielt und sind somit durchgefallen!";
 	  }
 	  else if(ergebnis.getNote() >= 4.0)
 	  {
 			 antwortText = "Herzliche Gratulation " + ergebnis.getStudent() +
 					 	"! Sie haben an der Pruefung eine " +
 				 		rundeAufHalbeNote(ergebnis.getNote()) + " (" +
 				 		dictionaryDoubleText.get(rundeAufHalbeNote(ergebnis.getNote())) + ")" +
 				 		" erzielt und somit bestanden!";
 	  }
 	  
 	  studentenErgebnis.add(antwortText);
   }
 }
