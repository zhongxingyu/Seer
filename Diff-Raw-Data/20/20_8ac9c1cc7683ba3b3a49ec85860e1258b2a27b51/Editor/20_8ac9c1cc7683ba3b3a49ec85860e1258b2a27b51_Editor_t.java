 package logic;
 
 import java.util.*;
 import music.*;
 
 /* Editor is the class in the logic package that directly interacts with the music package and
  * can make modifications to the data structure.
  */
 public class Editor{
 	Piece _piece;				// piece being edited
 
 // these iterators are used for sequential addition to the list
 	ListIterator<Staff> _staffIter;
 	ListIterator<Measure> _measureIter;
 	ListIterator<KeySignature> _keySigIter;
 	ListIterator<TimeSignature> _timeSigIter;
 	ListIterator<ChordSymbol> _chordSymIter;
 	ListIterator<Clef> _clefIter;
 	ListIterator<Voice> _voiceIter;
 	ListIterator<MultiNote> _multiNoteIter;
 	ListIterator<Pitch> _pitchIter;
 	
 	public Editor() {
 	}
 	
 	public Editor(Piece piece) {
 		setPiece(piece);
 	}
 	
 	public Editor clearScore() {
 		_piece.getStaffs().clear();
 		_staffIter = _piece.getStaffs().listIterator();
 		return this;
 	}
 	
 	public Editor setPiece(Piece piece) {
 		_piece = piece;
 		_staffIter = _piece.getStaffs().listIterator();
 		return this;
 	}
 	
 	public Piece getPiece() {
 		return _piece;
 	}
 	
 // STAFFS
 	public Editor setStaffIter(ListIterator<Staff> iter) {
 		_staffIter = iter;
 		return this;
 	}
 	public Editor insertStaff(Staff staff) {
 		_staffIter.add(staff);
 		_measureIter = staff.getMeasures().listIterator();
 		return this;
 	}
 	public Editor removeStaff() {
 		if (_staffIter.hasNext()) {
 			_staffIter.next();
 			_staffIter.remove();
 		} 
 		else {
 			throw new RuntimeException("Tried to remove from end of list");
 		}
 		return this;
 	}
 	public Editor replaceStaff(Staff staff) {
 		this.removeStaff().insertStaff(staff);
 		_staffIter.previous();
 		return this;
 	}
 	
 // MEASURES
 	public Editor setMeasureIter(ListIterator<Measure> iter) {
 		_measureIter = iter;
 		return this;
 	}
 	public Editor insertMeasure(Measure measure) {
 		_measureIter.add(measure);
 		_keySigIter = measure.getKeySignatures().listIterator();
 		_timeSigIter = measure.getTimeSignatures().listIterator();
 		_chordSymIter = measure.getChordSymbols().listIterator();
 		_clefIter = measure.getClefs().listIterator();
 		_voiceIter = measure.getVoices().listIterator();
 		return this;
 	}
 	public Editor removeMeasure() {
 		if (_measureIter.hasNext()) {
 			_measureIter.next();
 			_measureIter.remove();
 		} 
 		else {
 			throw new RuntimeException("Tried to remove from end of list");
 		}
 		return this;
 	}
 	public Editor replaceMeasure(Measure measure) {
 		this.removeMeasure().insertMeasure(measure);
 		_measureIter.previous();
 		return this;
 	}
 	
 // KEY SIGS
 	public Editor setKeySignatureIter(ListIterator<KeySignature> iter) {
 		_keySigIter = iter;
 		return this;
 	}
 	public Editor insertKeySig(KeySignature keySig) {
 		_keySigIter.add(keySig);
 		return this;
 	}
 	public Editor removeKeySig() {
 		if (_keySigIter.hasNext()) {
 			_keySigIter.next();
 			_keySigIter.remove();
 		} 
 		else {
 			throw new RuntimeException("Tried to remove from end of list");
 		}
 		return this;
 	}
 	public Editor replaceKeySig(KeySignature keySig) {
 		this.removeKeySig().insertKeySig(keySig);
 		_keySigIter.previous();
 		return this;
 	}
 	
 // TIME SIGS (use measure methods instead?)
 	public Editor setTimeSignatureIter(ListIterator<TimeSignature> iter) {
 		_timeSigIter = iter;
 		return this;
 	}
 	public Editor insertTimeSig(TimeSignature timeSig) {
 		_timeSigIter.add(timeSig);
 		return this;
 	}
 	public Editor removeTimeSig() {
 		if (_timeSigIter.hasNext()) {
 			_timeSigIter.next();
 			_timeSigIter.remove();
 		} 
 		else {
 			throw new RuntimeException("Tried to remove from end of list");
 		}
 		return this;
 	}
 	public Editor replaceTimeSig(TimeSignature timeSig) {
 		this.removeTimeSig().insertTimeSig(timeSig);
 		_timeSigIter.previous();
 		return this;
 	}
 	
 // CHORD SYMBOLS
 	public Editor setChordSymbolIter(ListIterator<ChordSymbol> iter) {
 		_chordSymIter = iter;
 		return this;
 	}
 	public Editor insertChordSymbol(ChordSymbol chordSymbol) {
 		_chordSymIter.add(chordSymbol);
 		return this;
 	}
 	public ChordSymbol removeChordSymbol() {
 		ChordSymbol removed;
 		if (_chordSymIter.hasNext()) {
 			removed = _chordSymIter.next();
 			_chordSymIter.remove();
 		} 
 		else {
 			throw new RuntimeException("Tried to remove from end of list");
 		}
 		return removed;
 	}
 	// Places a ChordSymbol at a certain offset to the iterator
 	public void replaceChordSymbol(ChordSymbol chordSymbol, Rational offset) {
 		ChordSymbol replaced = this.removeChordSymbol();
 		Rational replacedDuration = offset;
 		Rational replacementDuration = replaced.getDuration().minus(offset);
 		
 		ChordSymbol replacedNew = new ChordSymbol(replacedDuration,
 				replaced.getScaleDegree(),
 				replaced.getChordType());
 		
 		ChordSymbol replacement = new ChordSymbol(replacementDuration,
 				chordSymbol.getScaleDegree(),
 				chordSymbol.getChordType());
 		
 		if (replacedDuration.getNumerator() != 0) {
 			this.insertChordSymbol(replacedNew);
 			// if the offset is 0
 		}
 		this.insertChordSymbol(replacement);
 		mergeWithNextChord(replaced, replacement);
 	}
 
 	// merges newChord before the current chordSymIterator with all subsequent chords
 	// that have the same value as oldChord
 	// example: I | V V V V VI -> I           VI
 	private void mergeWithNextChord(ChordSymbol oldChord, ChordSymbol newChord) {
 		Rational newDuration = newChord.getDuration();
 		
 		if (_chordSymIter.hasNext()) {
 			ChordSymbol nextChord = this.removeChordSymbol();
 			if (nextChord.equals(oldChord)) {
 				// if the next chord is a duplicate of oldChord, add its duration
 				// to newChord and update newChord
 				newDuration = newDuration.plus(nextChord.getDuration());
 				_chordSymIter.previous();
 				
 				ChordSymbol newReplacement = new ChordSymbol(newDuration, 
 						newChord.getScaleDegree(),
 						newChord.getChordType(),
 						newChord.getInversion());
 				
 				this.removeChordSymbol();
 				this.insertChordSymbol(newReplacement);
 				
 				mergeWithNextChord(oldChord, newReplacement);
 			}
 			else {
 				// otherwise put it back
 				this.insertChordSymbol(nextChord);
 			}
 		}
 	}
 	
 // CLEFS
 	public Editor setClefIter(ListIterator<Clef> iter) {
 		_clefIter = iter;
 		return this;
 	}
 	public Editor insertClef(Clef clef) {
 		_clefIter.add(clef);
 		return this;
 	}
 	public Editor removeClef() {
 		if (_clefIter.hasNext()) {
 			_clefIter.next();
 			_clefIter.remove();
 		} 
 		else {
 			throw new RuntimeException("Tried to remove from end of list");
 		}
 		return this;
 	}
 	public Editor replaceClef(Clef clef) {
 		this.removeClef().insertClef(clef);
 		_clefIter.previous();
 		return this;
 	}
 	
 // VOICES
 	public Editor setVoiceIter(ListIterator<Voice> iter) {
 		_voiceIter = iter;
 		return this;
 	}
 	public Editor insertVoice(Voice voice) {
 		_voiceIter.add(voice);
 		_multiNoteIter = voice.getMultiNotes().listIterator();
 		return this;
 	}
 	public Editor removeVoice() {
 		if (_voiceIter.hasNext()) {
 			_voiceIter.next();
 			_voiceIter.remove();
 		} 
 		else {
 			throw new RuntimeException("Tried to remove from end of list");
 		}
 		return this;
 	}
 	public Editor replaceVoice(Voice voice) {
 		this.removeVoice().insertVoice(voice);
 		_voiceIter.previous();
 		return this;
 	}
 	
 // MULTINOTES
 	public void setMultiNoteIter(ListIterator<MultiNote> iter) {
 		_multiNoteIter = iter;
 	}
 	public void insertMultiNote(MultiNote multiNote) {
 		_multiNoteIter.add(multiNote);
 		_pitchIter = multiNote.getPitches().listIterator();
 	}
 	// Remove a MultiNote and returns the removed MultiNote
 	public MultiNote removeMultiNote() {
 		MultiNote removed;
 		
 		if (_multiNoteIter.hasNext()) {
 			removed = _multiNoteIter.next();
 			_multiNoteIter.remove();
 		} 
 		else {
 			throw new RuntimeException("Tried to remove from end of list");
 		}
 		return removed;
 	}
 	
 	// Replaces a MultiNote and fills in rests/overwrites notes as necessary.
	public void replaceMultiNote(MultiNote replacement, Rational offset) {
 		MultiNote replaced = this.removeMultiNote();

		if (offset.getNumerator() > 0) {
			this.insertMultiNote(new MultiNote(offset));
			replaced = new MultiNote(replaced.getDuration().minus(offset));
		}
		
		
 		if (replacement.getDuration().compareTo(replaced.getDuration()) == 1) {
 			// If the replacement is longer
 			// Replace current note and overwrite following notes
 			this.insertMultiNote(replacement);
 			this.overwriteNotes(replacement.getDuration().minus(replaced.getDuration()));
 		}
 		else {
 			// If the replacement is of shorter/equal length:
 			this.insertMultiNote(replacement);
 			this.insertRests(replaced.getDuration().minus(replacement.getDuration()));
 		}
 	}
 	
 	// Given an amount of space, fills that space with rests
 	private void insertRests(Rational amountOfSpace) {
 		MultiNote rest;
 		int numerator = amountOfSpace.getNumerator();
 		int denominator = amountOfSpace.getDenominator();
 		if (amountOfSpace.getNumerator() == 0) {
 			return;
 		}
 		else if (amountOfSpace.getNumerator() == 1) {
 			rest = new MultiNote(amountOfSpace);
 			insertMultiNote(rest);
 		}
 		else {
 			Rational largerChunk;
 			Rational remainder;
 			remainder = new Rational(numerator % 2, denominator);
 			largerChunk = new Rational(numerator / 2, denominator / 2);
 			insertRests(remainder);
 			insertRests(largerChunk);
 		}
 	}
 	
 	// if a replacement note runs into other notes as well, those notes are removed and
 	// the space is occupied by the replacement note
 	private void overwriteNotes(Rational spaceToOverwrite) {
 		MultiNote overwritten = this.removeMultiNote();
 		if (overwritten.getDuration().compareTo(spaceToOverwrite) == -1) {
 			// if the overwritten note is smaller than the space being overwritten
 			// remove the note, and continue overwriting further in the list
 			overwriteNotes(spaceToOverwrite.minus(overwritten.getDuration()));
 		}
 		else {
 			// if the overwritten note is larger than the space being overwritten
 			// remove the note, and insert rests to compensate for the remaining space
 			insertRests(overwritten.getDuration().minus(spaceToOverwrite));
 		}
 	}
 	
 // PITCHES
 	public Editor setPitchIter(ListIterator<Pitch> iter) {
 		_pitchIter = iter;
 		return this;
 	}
 	public Editor insertPitch(Pitch pitch) {
 		if (_pitchIter.hasNext()) {
 			Pitch next = _pitchIter.next();
 			if (next.getLineNumber() > pitch.getLineNumber()) {
 				// if the inserted pitch is lower, keep iterating
 				insertPitch(pitch);
 			}
 			else {
 				_pitchIter.previous();
 				_pitchIter.add(pitch);
 			}
 		}
 		else {
 			_pitchIter.add(pitch);
 		}
 		return this;
 	}
 	public Editor removePitch() {
 		if (_pitchIter.hasNext()) {
 			_pitchIter.next();
 			_pitchIter.remove();
 		} 
 		else {
 			throw new RuntimeException("Tried to remove from end of list");
 		}
 		return this;
 	}
 	public Editor replacePitch(Pitch pitch) {
 		this.removePitch().insertPitch(pitch);
 		_pitchIter.previous();
 		return this;
 	}
 }
