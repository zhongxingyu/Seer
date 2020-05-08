 /*
  * Created on 	23.03.2010
  * @author 		BrM / MD nach Vorlagen von Hue / FdR / Sid / Weigend
  * Zweck:		Klasse Rational mit vielfältigen Methoden
  */
 package ro.inf.p2.uebung04;
 
 import java.util.StringTokenizer;
 
 
 /**
  * Einfache Implementierung für rationale Zahlen als Vorbereitung auf die Klasse BigRational.
  * Diese Klasse ist immutable. Instanzen können nach der Konstruktion nicht mehr verändert werden
  * (es gibt keine set-Methoden).
  */
 public class Rational {
     // Konstanten
     public static final String DELIMITER = "/"; // Bruchstrich
     private final static long PRECISION = 10000000l;  // am Ende l wie long
 
 
     //  Zähler und Nenner
     private long numerator;
     private long denominator;
 
     // private Hilfsmethoden
 
     /**
      * Ermittlung des größten gemeinsamen Teilers (GCD = greatest common devisor) mittels des Euclidschen Alg
      *
      * @param a (positive) ganze Zahl
      * @param b (positive) ganze Zahl
      * @return größter gemeinsamer Teiler
      */
     private static long gcd(long a, long b) {
         a = Math.abs(a);
         b = Math.abs(b);
         if (a < b)
             return gcd(b, a);
         if (b == 0)
             return a; // Null wird von jeder Zahl geteilt
         long c = 1;
         while (c != 0) {
             c = a % b;
             a = b;
             b = c;
         }
         return a;
     }
 
     /**
      * normiert den Bruch (kürzt und macht den Nenner positiv)
      */
     private void norm() {
         long gcd = gcd(numerator, denominator);
         // kürzen
 
         numerator = numerator / gcd;
 
         denominator = denominator / gcd;
 
         // Nenner positiv machen
 
         if (denominator < 0) {
             numerator = -numerator;
             denominator = -denominator;
         }
     }
 
 
     // Konstruktoren
 
     /**
      * default Vorbelegung (0/1)
      */
     public Rational() {
 
         numerator = 0;
         denominator = 1;
 
     }
 
     /**
      * Rationalzahl mit Zähler und Nenner vom Typ long
      *
      * @param num Zähler
      * @param den Nenner
      */
     public Rational(long num, long den) {
 
         numerator = num;
         denominator = den;
 
         norm();
     }
 
     /**
      * Rationalzahl aus Long-Integer mit Nenner 1
      *
      * @param val long
      */
     public Rational(long val) {
         this(val, 1);
     }
 
     /**
      * Rationalzahl aus double
      *
      * @param val double-Wert
      */
     public Rational(double val) {
         this((long) (val * PRECISION), PRECISION);
 
         norm();
     }
 
     /**
      * Rationalzahl aus String der Form "x/y"
      *
      * @param val String
      */
     public Rational(String val) {
         StringTokenizer tok = new StringTokenizer(val, DELIMITER);
 
         numerator = Long.parseLong(tok.nextToken());
         denominator = Long.parseLong(tok.nextToken());
 
         norm();
     }
 
     // get-Methoden
 
     /**
      * gibt den privaten Zähler
      *
      * @return Zähler
      */
     public long getNumerator() {
 
         return numerator;
 
     }
 
     /**
      * gibt den privaten Nenner
      *
      * @return Nenner
      */
     public long getDenominator() {
 
         return denominator;
 
     }
 
     // einfache Umwandlungen
 
     /**
      * Gibt die Rationalzahl als double zurück indem der Zähler durch den Nenner dividiert wird.
      *
      * @return die Rationalzahl als Double
      */
     public double doubleValue() {
         return ((double) numerator / denominator);
     }
 
     /**
      * Ermittlung der Stringdarstellung
      *
      * @return eine Stringdarstellung der Rationalzahl der Form "a/b"
      */
     public String toString() {
         return numerator + "/" + denominator;
     }
 
     /**
      * Negation = Multiplikation mit -1
      *
      * @return den negativen Bruch
      */
     public Rational negate() {
         return new Rational(-numerator, denominator);
     }
 
     /**
      * Kehrbruch
      *
      * @return den Kehrbruch (aus a/b wird b/a)
      */
     public Rational invert() {
         return new Rational(denominator, numerator);
     }
 
     // Bruchrechnen
 
     /**
      * Zwei Brüche (a,b) werden addiert. Dabei wird der Bruch erweitert
      *
      * @param val der Bruch (b), welcher addiert werden soll.
      * @return Eine neue Rationalzahl als Ergebnis der Operation
      */
     public Rational add(Rational val) {
         return new Rational(
                 numerator * val.getDenominator() + val.getNumerator() * denominator,
                 denominator * val.getDenominator()
         );
         // die Normierung erfolgt im Konstruktor
     }
 
 
     /**
      * Zieht von einem Bruch einen anderen ab (a-b) indem mit dem negierten Bruch
      * addiert wird. Dieses Methode ist ein Beispiel für gute, wenig redundante Programmierung,
      * indem die substract-Methode auf eine bestehende Methode abgebildet wird, ohne die Logik
      * erneut zu programmieren.
      *
      * @param val der Bruch (b), der abgezogen wird
      * @return Eine neue Rationalzahl als Ergebnis der Operation
      */
     public Rational subtract(Rational val) {
        return this.add(val.negate());
     }
 
     /**
      * Multipliziert zwei Brüche (a,b), indem die Zähler und Nenner jeweils miteinander
      * multipliziert werden.
      *
      * @param val der Bruch (b)
      * @return Eine neue Rationalzahl als Ergebnis der Operation
      */
     public Rational multiply(Rational val) {
         return new Rational(
                 numerator * val.getNumerator(),
                 denominator * val.getDenominator()
         );
     }
 
     /**
      * Teilt einen Bruch durch einen anderen, indem mit dem Kehrbruch multipliziert wird.
      *
      * @param val der Bruch durch den geteilt wird.
      * @return Eine neue Rationalzahl als Ergebnis der Operation
      */
     public Rational divide(Rational val) {
        return this.multiply(val.invert());
     }
 
 }
