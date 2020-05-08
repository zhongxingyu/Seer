 import java.util.*;
 
 /**
  * Diese Klasse modelliert Raeume in der Welt von Zuul.
  * 
  * Ein "Raum" repraesentiert einen Ort in der virtuellen Landschaft des
  * Spiels. Ein Raum ist mit anderen Raeumen ueber Ausgaenge verbunden.
  * Moegliche Ausgaenge liegen im Norden, Osten, Sueden und Westen.
  * Fuer jede Richtung haelt ein Raum eine Referenz auf den 
  * benachbarten Raum.
  * 
  * @author  Michael Kuelling und David J. Barnes, Dave Kramer, Simon Schwarz
  * @version 07.11.2013
  */
 class Raum 
 {
 	private String beschreibung;
 	private HashMap<String, Raum> ausgaenge;
 
     /**
      * Erzeuge einen Raum mit einer Beschreibung. Ein Raum
      * hat anfangs keine Ausgaenge.
      * @param beschreibung enthaelt eine Beschreibung in der Form
      *        "in einer Kueche" oder "auf einem Sportplatz".
      */
     public Raum(String beschreibung) 
     {
     	ausgaenge = new HashMap<String, Raum>();
         this.beschreibung = beschreibung;
     }
 
     /**
      * Definiere die Ausgaenge dieses Raums. Jede Richtung
      * fuehrt entweder in einen anderen Raum oder ist 'null'
      * (kein Ausgang).
      * @param richtung Norden, Osten, Sueden, Westen.
      * @param raum	Objekt des Raumes für welchen der Ausgang definiert werden soll
      */
     public void setzeAusgaenge(String richtung, Raum raum) 
     {
         ausgaenge.put(richtung, raum);
     }
 
     public Raum gibAusgang(String richtung)
     {
     	return ausgaenge.get(richtung);
     }
     
     /**
     * Liefere eine Beschreibung der Ausgänge dieses Raumes, bespielsweise * "Ausgänge: north west".
     *
     * @return eine Beschreibung der verfügbaren Ausgänge
     */
     public String gibAusgaengeAlsString()
     {
    	boolean ersterDatensatz = true;
     	String ausgangsRichtungen = "";
     	

     	for(String ausgabe: ausgaenge.keySet())
     	{
        	if(ersterDatensatz)
        	{
        		ausgangsRichtungen = ausgabe;
        		ersterDatensatz = false;
        	}
        	else
        	{
        		ausgangsRichtungen += ", "+ausgabe;
        	}
     	}
     	
 		return ausgangsRichtungen;	
     }
     
     /**
      * @return die Beschreibung dieses Raums.
      */
     public String gibBeschreibung()
     {
         return beschreibung;
     }
 }
