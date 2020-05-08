 /**
  * Created on 	27.04.2010
  * @author	 	BrM / MD nach Vorlagen von Hue / Sid
 * Zweck:		Beispiel f�r Nutzung des Interfaces Comparable	
  */
 package ro.inf.p2.uebung08;
 
 /**
 * Klasse f�r Intervalle definiert durch eine untere und obere Grenze.
  * <p>
 * Die Grenzen m�ssen das Interface Comparable implementieren.
 * Eine �berpr�fung ob die untere Grenze kleiner als die obere
  * Grenze ist findet nicht statt.
  */
 public class Interval {
 	private Comparable lowerBound;
 	private Comparable upperBound;
 
 	private static final Integer ZERO = new Integer(0);
 	public static final Interval EMPTY = new Interval(ZERO, ZERO);
 
 	private static Comparable min(Comparable x, Comparable y) {
 		return null; // TODO
 	}
 
 	private static Comparable max(Comparable x, Comparable y) {
 		return null;// TODO
 	}
 
 	/**
 	 *
 	 * @param lowerBound    untere Grenze des Intervalls
 	 * @param upperBound    obere Grenze des Intervalls
 	 */
 	public Interval(Comparable lowerBound, Comparable upperBound) {
 		// TODO
 	}
 
 	/**
 	 *
 	 * @param x  zu vergleichendes Intervall
 	 * @return   true falls Intervallgrenzen identisch sind
 	 */
 	public boolean equals(Object x) {
 		return false; // TODO
 	}
 
 	/**
 	 * @return true, falls lowerBound <= x < upperBound
 	 */
 	public boolean contains(Comparable x) {
 		return false; // TODO
 	}
 
 	/**
      * @param v das andere Intervall
     * @return true, wenn this das andere Intervall enth�lt
      */
 	public boolean contains(Interval v) {
 		return false; // TODO
 	}
 
 	/**
 	 * @return Durchschnitt von this und iv.
 	 */
 	public Interval intersection(Interval iv) {
 		return null; // TODO
 	}
 
 	/**
 	 * @return true, falls lowerbound >= upperbound
 	 */
 	public boolean isEmpty() {
 		return false; // TODO
 	}
 
 	/**
 	 *
 	 * @return eindeutiger Hash-Wert vom Intervall
 	 */
 	public int hashCode() {
 		return 0; // TODO
 	}
 	
 	/**
 	 *
	 * @return String-Repr�sentation vom Intervall
 	 */
 	public String toString() {
 		return super.toString(); // TODO
 	}
 }
