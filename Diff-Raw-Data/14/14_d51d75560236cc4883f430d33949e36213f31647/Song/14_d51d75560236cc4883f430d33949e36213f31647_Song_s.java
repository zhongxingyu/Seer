 package sound;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiUnavailableException;
 
 /**
  * Mutable Song object which represents an entire piece of ABC music
  * @author Josh
  *
  */
 public class Song {
 
 	// Header information
 	private String title, composer;
 	private Fraction defaultLength, meter, tempoNoteType;
 	private int index, tempoNPM, ticksPerWholeNote;
 	private Key key;
 	
 	private List<Measure> measures = new LinkedList<Measure>();
 	
 	
 	/**	
 	 * Song object
 	 * @param title title of piece
 	 * @param composer default is "default"
 	 * @param noteLength default duration of note
 	 * @param meter sum of durations of all notes within a bar
 	 * @param index index number to determine play order if in playlist
 	 * @param tempoNPM Amount of notes of given type per minute
 	 * @param tempoNoteType type of note that tempoNPM applies to
 	 * @param key key signature of piece
 	 */
 	public Song(int index, String title, Key key) {
 		//Required fields
 		this.index = index;
 		this.title = title;
 		this.key = key;
 		
 		//Defaults
 		this.composer = "default";
 		this.meter = new Fraction(4,4);
 		this.defaultLength = new Fraction(1,8);
 		this.tempoNoteType = this.defaultLength;
 		this.tempoNPM = 100;
 	}
 	
 	/**
 	 * Sets composer of song
 	 * @param composer
 	 */
 	public void setComposer(String composer){
 		this.composer = composer; //replaces default composer
 	}
 	
 	
 	//NOTE: the following three methods are called in this order: setMeter -> setLength -> setTempo
 	//This hierarchy was chosen because certain field default values get set by other fields' values.
 	
 	/**
 	 * Sets meter of song
 	 * @param meter
 	 */
 	public void setMeter(Fraction meter) {
 		this.meter = meter; //replaces default meter
 		double meterValue = ((double)meter.getNumerator())/((double)meter.getDenominator());
 		//default unit length is a sixteenth note if meter < 0.75, else is an eighth note
 		if (meterValue < 0.75){
 			this.defaultLength = new Fraction(1,16);
 		}
 		this.tempoNoteType = this.defaultLength;
 	}
 	
 	/**
 	 * Sets default length of song
 	 * @param length
 	 */
 	public void setLength(Fraction length){
 		this.defaultLength = length; //replaces default length
 		this.tempoNoteType = this.defaultLength;
 	}
 	
 	/**
 	 * Sets tempo of song
 	 * @param tempoNoteType
 	 * @param tempoNPM
 	 */
 	public void setTempo(Fraction tempoNoteType, int tempoNPM){
 		this.tempoNoteType = tempoNoteType;
 		this.tempoNPM = tempoNPM;
 	}
 	
 
 	/**
 	 * Returns the given note with proper accidentals for the song's key
 	 * @param note: String representing name of note
 	 * @return Pitch with proper accidentals for key
 	 */
 	public Pitch getPitchInKey(String note) {
 		return this.key.getPitch(note);
 	}
 	
 	/**
 	 * @return meter fraction's decimal evaluation
 	 */
 	public double getNotesPerMeasure() {
 		return this.meter.evaluate();
 	}
 	
 	/**
 	 * Adds measure to end of current measures
 	 * @param measure
 	 */
 	public void addMeasure(Measure measure) {
 		this.measures.add(measure);
 	}
 	
 	/**
 	 * Adds measures to end of current measures
 	 * @param measures
 	 */
 	public void addMeasures(List<Measure> measures) {
 		this.measures.addAll(measures);
 	}
 	
 	
 	/**
 	 * Returns default note length for the song
 	 * @return
 	 */
 	public Fraction getDefaultNoteLen() {
 		return this.defaultLength;
 	}
 	
 	/**
 	 * Returns the duration which is the fraction represented by the string s 
 	 * times default note length
 	 * @param s: string representing note duration (A1/4, s would be "1/4" and 
 	 * @return Fraction which is input fraction * default length
 	 */
 	public Fraction parseDurationFromString(String s) {
 		Fraction def = this.defaultLength;
 
 		if (s.matches("[0-9]*/[0-9]*")) {
 			// We know it is a fraction
 			String[] split = s.split("/");
 		
 			if (split.length == 0) {
 				// The string was just "/" we default to 1/2
 				return new Fraction(def.getNumerator(), 2*def.getDenominator());
 			} 
 			else if(split.length == 1) {
 				// "1/" => ["1"]
 				// default denominator to 2, numerator comes from string		
 				int num = Integer.parseInt(split[0]);
 				return new Fraction(num*def.getNumerator(), 2*def.getDenominator());
 			} 
 			else if(split.length == 2) {
 				if (split[0].equals("")) {				
 					// "/2" => ["", "2"]
 					// Default numerator to 1, get denom from string
 					int denom = Integer.parseInt(split[1]);
 					return new Fraction(def.getNumerator(), denom*def.getDenominator());
 				} 
 				else {
 					// "1/4" => ["1","4"]
 					// get fraction from string
 					int num = Integer.parseInt(split[0]);
 					int denom = Integer.parseInt(split[1]);
 					return new Fraction(num*def.getNumerator(), denom*def.getDenominator());
 				}
 			} 
 			else {
 				throw new IllegalArgumentException("The duration can only have one / in it");
 			}
 		} 
 		else {
 			// String is just a number
 			int num = Integer.parseInt(s);
 			return new Fraction(num*def.getNumerator(), def.getDenominator());
 		}
 	}
 	
 	/**
 	 * Play song object
 	 * @throws InvalidMidiDataException 
 	 * @throws MidiUnavailableException 
 	 */
 	public void play() throws MidiUnavailableException, InvalidMidiDataException {
 		LyricListener ll = this.getBasicLyricListener();
 		SequencePlayer sp = this.createSequencePlayer(ll);
 		this.scheduleSequence(sp);
 		sp.play();
 	}
 	
 	/**
 	 * Returns the smallest amount of ticks per whole note required of the sequence player
 	 * @return int: representing the smallest amount of ticks per note
 	 */
 	public int getTicksPerWholeNote() {
 		int maxTicks = 0;
 		for (Measure m: this.measures) {
 			int ticks = m.getTicksPerWholeNote();
 			if (ticks > maxTicks) {
 				maxTicks = ticks;
 			}
 		}
 		return maxTicks;
 	}
 	
 	/**
 	 * Creates a basic lyric listener object
 	 * @return LyricListener: basic lyric listener object
 	 */
 	public LyricListener getBasicLyricListener() {
 		LyricListener ll = new LyricListener();
         return ll;
 	}
 	
 	/**
 	 * Creates a SequencePlayer with the right timing for ticks and tempo
 	 * @param ll: LyricListener to pass into the SequencePlayer
 	 * @return SequencePlayer object with the right tempo
 	 * @throws InvalidMidiDataException 
 	 * @throws MidiUnavailableException 
 	 */
 	public SequencePlayer createSequencePlayer(LyricListener ll)
 			throws MidiUnavailableException, InvalidMidiDataException {
 		
 		SequencePlayer sp;
 		/*
 	     * @param beatsPerMinute the number of beats per minute
 	     * @param ticksPerBeat the number of ticks per beat
 	     * 
 	     * @param meter
 	     * @param tempoNPM: Amount of notes of given type per minute
 	     * @param tempoNoteType: type of note that tempoNPM applies to
 		*/
 		
 		// Derived from basic musical relationships
 		this.ticksPerWholeNote = this.getTicksPerWholeNote();
 		Fraction beat = new Fraction(1,this.meter.getDenominator());
 		int ticksPerBeat = this.ticksPerWholeNote / this.meter.getDenominator();
 		
 		int beatsPerMinute = (int)(beat.evaluate()/this.tempoNoteType.evaluate())*(this.tempoNPM);
 
 		sp = new SequencePlayer(beatsPerMinute, ticksPerBeat, ll);
 		return sp;
 	}
 	
 	/**
 	 * Schedules element and associated lyric to sequenceplayer
 	 * @param sp: sequenceplayer
 	 * @param element: Element to be scheduled
 	 * @param lyrics: List of current Lyrics
 	 * @param voiceTicks: What tick to schedule it at
 	 * @param noteDuration: Duration of element
 	 */
 	public void scheduleBasicElement(SequencePlayer sp, MusicalElement element, List<String> lyrics, int voiceTicks, int noteDuration) {
 		if (element instanceof Note) {
 			Note note = (Note) element;
 			sp.addNote(note.getPitch().toMidiNote(), voiceTicks, noteDuration);
 			scheduleLyric(sp, lyrics, voiceTicks);
 		} 
 		else if (element instanceof Chord) {
 			Chord chord = (Chord) element;
 			List<Note> notes = chord.getNotes();
 			for (Note note: notes) {
 				sp.addNote(note.getPitch().toMidiNote(), voiceTicks, noteDuration);
 				scheduleLyric(sp, lyrics, voiceTicks);
 			}	
 		}
 	}
 	
 	/**
 	 * Schedules lyric element in sequenceplayer
 	 * @param sp: SequencePlayer to schedule on
 	 * @param lyrics: List of strings
 	 * @param voiceTicks: Tick to schedule it at
 	 */
 	public void scheduleLyric(SequencePlayer sp, List<String> lyrics, int voiceTicks) {
 		if(lyrics.size() > 0) {
 			sp.addLyricEvent(lyrics.get(0), voiceTicks);
 			lyrics.remove(0);
 		}
 	}
 	
 	/**
 	 * Loops through entire song scheduling MIDI and lyric events in sequencePlayer
 	 * Precondition: this.measures is not empty
 	 * @param sp: SequencePlayer object to get scheduled on
 	 * @return void
 	 */
 	public void scheduleSequence(SequencePlayer sp) {
 		int tickTracker = 0;
 		Measure nextMeasure;
 		
 		// Build a new list of measures accounting for repeats and alternate endings
 		List<Measure> measuresWithRepeats = new LinkedList<Measure>();
 		int nextMeasureNum = 0;
 		while(nextMeasureNum < measures.size()) {
 			nextMeasure = measures.get(nextMeasureNum);
 			// Prevents 1st ending of alternate ending from being played twice
 			if(nextMeasure.shouldBePlayed()) {
 				measuresWithRepeats.add(nextMeasure);	
 			}
 			nextMeasureNum = nextMeasure.getNextMeasure();
 		}
 		
 		for(Measure measure: measuresWithRepeats) {
 
 			// Add everything within this measure to sequence
 			for(Voice voice: measure.getVoices()) {	
 				List<String> lyrics = voice.getLyrics();
 				int voiceTicks = tickTracker;
 				for(MusicalElement element: voice.getMusicalElements()) {
 					int ticks = (int)((double) this.ticksPerWholeNote*element.getDuration().evaluate());
 					
 					this.scheduleBasicElement(sp, element, lyrics, voiceTicks,
 							(int)(element.getDuration().evaluate()*this.getTicksPerWholeNote()));
 					voiceTicks += ticks;
 				}
 			}
 			tickTracker += (int) ((double) this.ticksPerWholeNote * this.meter.evaluate());
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return this.title + "\n" + this.composer + "\n" + this.meter + "\n" + this.defaultLength
 				+ "\n" + this.tempoNoteType + "\n" + this.tempoNPM + "\n" + this.key;
 	}
 
     /* (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result
                 + ((composer == null) ? 0 : composer.hashCode());
         result = prime * result
                 + ((defaultLength == null) ? 0 : defaultLength.hashCode());
         result = prime * result + index;
         result = prime * result + ((key == null) ? 0 : key.hashCode());
         result = prime * result
                 + ((measures == null) ? 0 : measures.hashCode());
         result = prime * result + ((meter == null) ? 0 : meter.hashCode());
         result = prime * result + tempoNPM;
         result = prime * result
                 + ((tempoNoteType == null) ? 0 : tempoNoteType.hashCode());
         result = prime * result + ((title == null) ? 0 : title.hashCode());
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
         if (!(obj instanceof Song)) {
             return false;
         }
         Song other = (Song) obj;
         if (composer == null) {
             if (other.composer != null) {
                 return false;
             }
         } else if (!composer.equals(other.composer)) {
             return false;
         }
         if (defaultLength == null) {
             if (other.defaultLength != null) {
                 return false;
             }
         } else if (!defaultLength.equals(other.defaultLength)) {
             return false;
         }
         if (index != other.index) {
             return false;
         }
         if (key == null) {
             if (other.key != null) {
                 return false;
             }
         } else if (!key.equals(other.key)) {
             return false;
         }
         if (measures == null) {
             if (other.measures != null) {
                 return false;
             }
         } else if (!measures.equals(other.measures)) {
             return false;
         }
         if (meter == null) {
             if (other.meter != null) {
                 return false;
             }
         } else if (!meter.equals(other.meter)) {
             return false;
         }
         if (tempoNPM != other.tempoNPM) {
             return false;
         }
         if (tempoNoteType == null) {
             if (other.tempoNoteType != null) {
                 return false;
             }
         } else if (!tempoNoteType.equals(other.tempoNoteType)) {
             return false;
         }
         if (title == null) {
             if (other.title != null) {
                 return false;
             }
         } else if (!title.equals(other.title)) {
             return false;
         }
         return true;
     }
 	
 	
 }
