 package sound;
 
 /**
  * Note is an immutable object representing a note.  It contains a chord
  * and the duration to play it for
  * @author Josh
  *
  */
 public class Note implements MusicalElement {
 	private Chord chord;
 	private Fraction duration; // ie quarter note, whole note, etc
 	
 	/**
 	 * Constructor of Note object
 	 * @param chord: Chord object to be played as note 
 	 * @param duration: Double representing type of note (ie .25 = quarter note)
 	 */
 	public Note(Chord chord, Fraction duration) {
 		this.chord = chord;
 		this.duration = duration;
 	}
 	
 	/**
 	 * Gets list of all pitches in note
 	 * @return chord for note
 	 */
 	public Chord getChord() {
 		return this.chord;
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
		return (int)(1.0/this.getDuration().evaluate());
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	public Note clone() {
 		return new Note(this.chord, this.duration);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return this.chord.toString() + ":" + this.duration.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((chord == null) ? 0 : chord.hashCode());
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
 		if (!(obj instanceof Note)) {
 			return false;
 		}
 		Note other = (Note) obj;
 		if (chord == null) {
 			if (other.chord != null) {
 				return false;
 			}
 		} else if (!chord.equals(other.chord)) {
 			return false;
 		}
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
