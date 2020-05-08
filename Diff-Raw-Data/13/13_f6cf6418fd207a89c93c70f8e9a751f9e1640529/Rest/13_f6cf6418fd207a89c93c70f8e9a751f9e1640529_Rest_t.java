 package sound;
 
 /**
  * Immutable object representing a Rest in a piece
  * @author Josh
  *
  */
 public class Rest implements MusicalElement {
 
 	private Fraction duration;
 	
 	/**
 	 * Immutable object representing a Rest
 	 * @param duration: Length of rest as Fraction
 	 */
 	public Rest(Fraction duration) {
 		this.duration = duration;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see sound.MusicalElement#getDuration()
 	 */
 	public Fraction getDuration() {
 		return this.duration;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see sound.MusicalElement#getTicksPerWholeNote()
 	 */
 	public int getTicksPerWholeNote() {
		// Denominator is how many ticks need to be in this note - then multiply by ratio of 
		// whole note to total size of this note to get ticks per whole note
		return (this.getDuration().getDenominator())*(int)(1.0/this.getDuration().evaluate());
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	public Rest clone() {
 		return new Rest(this.duration);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return "z:" + this.duration.toString(); 
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((duration == null) ? 0 : duration.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof Rest)) {
 			return false;
 		}
 		Rest other = (Rest) obj;
 		if (duration == null) {
 			if (other.duration != null) {
 				return false;
 			}
 		} else if (!duration.equals(other.duration)) {
 			return false;
 		}
 		return true;
 	}
 	
 }
