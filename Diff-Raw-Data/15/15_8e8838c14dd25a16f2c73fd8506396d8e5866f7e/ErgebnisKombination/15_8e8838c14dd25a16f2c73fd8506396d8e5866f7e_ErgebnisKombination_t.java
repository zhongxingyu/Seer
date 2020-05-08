 /**
  * 
  */
 package org.hitzemann.mms.model;
 
 /**
  * @author simon
  * 
  */
 public class ErgebnisKombination implements Comparable<ErgebnisKombination> {
 
 	/**
 	 * Anzahl der korrekten Steine.
 	 */
 	private final int schwarz;
 	/**
 	 * Anzahl der Steine mit korrekten Farben und falschen Positionen.
 	 */
 	private final int weiss;
 	
 	/**
 	 * Konstruktor für ein ErgebnisKombination Objekt.
 	 * 
 	 * @param paramSchwarz
 	 *            Anzahl der korrekten Steine
 	 * @param paramWeiss
 	 *            Anzahl der Steine mit korrekten Farben und falschen Positionen
 	 */
 	public ErgebnisKombination(final int paramSchwarz, final int paramWeiss) {
 		this.schwarz = paramSchwarz;
 		this.weiss = paramWeiss;
 	}
 
 	/**
 	 * Standard Getter für Attribut schwarz.
 	 * 
 	 * @return Anzahl der korrekten Steine.
 	 */
 	public final int getSchwarz() {
 		return schwarz;
 	}
 
 	/**
 	 * Standard Geter für Attribut weiss.
 	 * 
 	 * @return Anzahl der Steine mit korrekten Farben und falschen Positionen.
 	 */
 	public final int getWeiss() {
 		return weiss;
 	}
 
 	@Override
 	public final int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + schwarz;
 		result = prime * result + weiss;
 		return result;
 	}
 
 	@Override
 	public final boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final ErgebnisKombination other = (ErgebnisKombination) obj;
 		if (schwarz != other.schwarz) {
 			return false;
 		}
 		if (weiss != other.weiss) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public final String toString() {
 		return "ErgebnisKombination [schwarz=" + schwarz + ", weiss=" + weiss
 				+ "]";
 	}
 
 	@Override
 	public final int compareTo(final ErgebnisKombination o) {
		if (this.getSchwarz() > o.getSchwarz()) {
			return 1;
		} else if (this.getSchwarz() < o.getSchwarz()) {
			return -1;
		} else {
			if (this.getWeiss() > o.getWeiss()) {
				return 1;
			} else if (this.getWeiss() < o.getWeiss()) {
				return -1;
			}
		}
		return 0;
 	}
 
 
 }
