 package models;
 
 import java.math.BigDecimal;
 import java.util.Arrays;
 import java.util.List;
 
 import com.audiveris.proxymusic.EmptyPlacement;
 import com.audiveris.proxymusic.Pitch;
 
 public class Note extends Symbol implements Cloneable, Comparable<Note> {
 	
 	private NotePitch pitch = NotePitch.REST;
 	private int octave = 0;
 	private NoteType type = null;
 	private int duration = 0;
 	private int alter = 0;
 	private int dots = 0;
 	private boolean chord;
 
 	protected Note() {}
 	
 	public Note(NotePitch pitch, int octave, NoteType type) {
 		this.pitch = pitch;
 		this.type = type;
 		this.octave = octave;
 	}
 
 	public boolean chord() {
 		return chord;
 	}
 	
 	@Override
 	public int getDuration() {
 		return duration;
 	}
 
 	@Override
 	public String getName() {
 		
 		String name = (pitch != null ? pitch.getTranslation() : "");
 
 		if (Math.abs(alter) == 2) {
 			name += " doble ";
 		}
 		if (alter < 0) {
 			name += " bemol ";
 		}
 		if (alter > 0) {
 			name += " sostenido ";
 		}
 		
 		if (octave > 0) {
 			name += " " + NoteOctave.valueOf("_" + octave).getTranslation();
 		}
 		
 		if (type != null) {
 			name += " " + type.getTranslation();
 		}
 		
 		if (dots > 0) {
 			if (dots == 1) {
 				name += " puntillo";
 			} else {
 				name += " " + dots + " puntillos";
 			}
 		}
 		
 		return name;
 	}
 	
 	public static Note fromXmlNote(com.audiveris.proxymusic.Note xmlNote) {
 		Note note = new Note();
 
 		Pitch pitch = xmlNote.getPitch();
 		if (pitch != null) {
 			note.pitch = NotePitch.valueOf(pitch.getStep().value().toUpperCase());
 			note.octave = Integer.valueOf(pitch.getOctave());
 			BigDecimal alter = pitch.getAlter();
 			if (alter != null) {
 				note.alter = alter.intValue();
 			}
 		}
 
 		com.audiveris.proxymusic.NoteType type = xmlNote.getType();
 		if (type != null) {
 			note.type = NoteType.valueOf("_" + type.getValue().toUpperCase());
 		}
 		
 		note.duration = xmlNote.getDuration().intValue();
 		
 		List<EmptyPlacement> dots = xmlNote.getDot();
 		if (dots != null) {
 			for(@SuppressWarnings("unused") EmptyPlacement dot : dots) {
 				note.dots++;
 			}
 		}
 		
 		note.chord = (xmlNote.getChord() != null);
 		
 		return note;
 	}
 
 	@Override
 	public List<String> getSounds() {
 		return Arrays.asList(this.flatOrSquare().getSound());
 	}
 
 	private String getSound() {
 		return  pitch.toString() + ((alter == -1) ? "b" : "")+ octave;
 	}
 
 	private Note flatOrSquare() {
 		return (alter == 0 || alter == -1) ? this : this.consumeAlter().flatOrSquare();
 	}
 
 	private Note consumeAlter() {
 		Note result = doClone();
 		int sign = (alter > 0) ? -1 : 1;
 		result.pitch = pitch.move(sign);				
 		result.alter += sign;
 		if (pitch.changesOctaveWithMove(sign)) {
 			result.octave += sign;
 		} else {
 			result.alter += sign;			
 		}
 		return result;
 	}
 
 	private Note doClone() {
 		try {
 			return (Note) this.clone();
 		} catch (CloneNotSupportedException e) {} // Not gona happen!
 		return null;
 	}
 
 	@Override
 	public int compareTo(Note o) {
		int compare = Integer.compare(octave, o.octave);
 		return compare == 0 ? pitch.compareTo(o.pitch) : compare;
 	}
 
 	public Note getDiff(Note other) {
 		if (other == null) {
 			return this;
 		} else {
 			Note diff = new Note();
 			if (!pitch.equals(other.pitch) || alter != other.alter) {
 				diff.pitch = pitch;
 				diff.alter = alter;
 			} else {
 				diff.pitch = null;
 			}
 			
 			if (!type.equals(other.type) || dots != other.dots) {
 				diff.type = type;
 				diff.dots = dots;
 			}
 
 			diff.octave = (octave != other.octave) ? octave : 0;
 			diff.duration = duration; // Even if they are equal, I wan't to know the duration
 			diff.chord = chord;
 
 			return diff;
 		}
 	}
 
 }
