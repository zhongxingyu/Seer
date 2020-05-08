 package algo.exercise;
 
 /**
  * Baumknoten und Token zur Darstellung einer Zahl.
  *
  * @author Sandro Kuckert
  */
 public class Num extends Token 
 {
    private int value;
 
     public Num() {} // DIESEN LEEREN STANDARDKONSTRUKTOR NICHT LOESCHEN
 
     /**
      * Konstruktor, der als Eingabewert einen int erwartet, diesen setzt
      * und den Knotentyp auf 'n' setzt
      *
      * @param value int Zahl mit dem Wert des Tokens
      */
     public Num(int value)
     {
        this.value = value;
         this.type = 'n';
     }
 
     /**
      * Funktion zur Rückgabe des Values von der Token-Instance
      *
      * @return int Value des Knotens
      */
     public int eval() 
     {
        return this.value;
     }
 
     /**
      * Funktion gibt die Zahl der Instance als String zurück für die Prefix-Notation
      *
      * @return Wert von Num als String
      */
     public String prefix() 
     {
         return outputNum();
     }
 
     /**
      * Funktion gibt die Zahl der Instance als String zurück für die Infix-Notation
      *
      * @return Wert von Num als String
      */
     public String infix() 
     {
         return outputNum();
     }
 
     /**
      * Funktion gibt die Zahl der Instance als String zurück für die Postfix-Notation
      *
      * @return Wert von Num als String
      */
     public String postfix() 
     {
         return outputNum();
     }
     
     /**
      * Numeriert den Baum ausgehend vom aktuellen Knoten unter Verwendung eines Zaehlers in Infix-Reihenfolge durch (wichtig fuer die Visualisierung).
      * 
      * @param o der Zaehler
      */
     public void order(Order o) { setOrd(++o.counter); }
 
     /**
      * Funktion gibt die Anzhal der Knoten zurück
      * Bei einer Zahl ist ein Blatt im Baum erreicht
      *
      * @return int Anzahl der Knoten
      */
     public int nodes()
     {
         return 1;
     }
 
     /**
      * Funktion zum Ermitteln der Tiefe des Baumes
      * Bei einer Zahl ist ein Blatt im Baum erreicht
      *
      * @return int Tiefe des Baumes
      */
     public int depth()
     {
         return 1;
     }
 
     private String outputNum() {
 
        return String.valueOf(this.value);
     }
 }
