 package org.soldomi.model.tune2;
 
 import org.apache.commons.math3.fraction.Fraction;
 
 public class Symbol {
     public final Long id;
     public final Long staffId;
     public final Long blockId;
 
     public final Fraction startTime;
     public final SymbolType symbolType;
 
     public final Segment segment;
     public final TimeSignature timeSignature;
     public final KeySignature keySignature;
 
     public Symbol(Long id,
 		  Long staffId,
 		  Long blockId,
 		  Fraction startTime,
 		  SymbolType symbolType,
 		  Segment segment,
 		  TimeSignature timeSignature,
 		  KeySignature keySignature) {
 	this.id = id;
 	this.staffId = staffId;
 	this.blockId = blockId;
 	this.startTime = startTime;
 	this.symbolType = symbolType;
 	this.segment = segment;
 	this.timeSignature = timeSignature;
 	this.keySignature = keySignature;
 
     }
 
     public Symbol withId(Long id) {
 	Segment segment = this.segment;
 	if (segment != null) {
 	    segment = segment.withSymbolId(id);
 	}
 	TimeSignature timeSignature = this.timeSignature;
 	if (timeSignature != null) {
 	    timeSignature = timeSignature.withSymbolId(id);
 	}
 	KeySignature keySignature = this.keySignature;
 	if (keySignature != null) {
 	    keySignature = keySignature.withSymbolId(id);
 	}
 	return new Symbol(id, staffId, blockId, startTime, symbolType, segment, timeSignature, keySignature);
     }
 
     public Symbol withBlockId(Long blockId) {
 	return new Symbol(id, staffId, blockId, startTime, symbolType, segment, timeSignature, keySignature);
     }
 
     public Symbol withStaffId(Long staffId) {
 	return new Symbol(id, staffId, blockId, startTime, symbolType, segment, timeSignature, keySignature);
     }
 
     public Symbol withKeySignature(KeySignature keySignature) {
 	return new Symbol(id, staffId, blockId, startTime, symbolType, segment, timeSignature, keySignature);
     }
 
     public Symbol withTimeSignature(TimeSignature timeSignature) {
 	return new Symbol(id, staffId, blockId, startTime, symbolType, segment, timeSignature, keySignature);
     }
 
     public Symbol withSegment(Segment segment) {
 	return new Symbol(id, staffId, blockId, startTime, symbolType, segment, timeSignature, keySignature);
     }
 
     public static Symbol newClef(Fraction startTime, Clef clef) {
 	return new Symbol(null, null, null, startTime, clef.symbolType, null, null, null);
     }
 
     public static Symbol newRest(Fraction startTime, DurationSymbol durationSymbol, Fraction duration, Integer dotCount) {
 	return new Symbol(null, null, null, startTime, durationSymbol.restSymbolType, new Segment(duration, dotCount), null, null);
     }
 
     public static Symbol newNote(Fraction startTime, DurationSymbol durationSymbol, Fraction duration, Integer dotCount, Pitch pitch, Accidental accidental) {
 	return new Symbol(null, null, null, startTime, durationSymbol.noteSymbolType, new Segment(duration, dotCount, new Note(pitch, accidental)), null, null);
     }
 
     public static Symbol newTimeSignature(Fraction startTime, Integer beatCount, NoteValue beatValue) {
 	return new Symbol(null, null, null, startTime, SymbolType.STANDARD_TIME_SIGNATURE, null, new TimeSignature(beatCount, beatValue), null);
     }
 
    public static Symbol newTimeSignatureAllaBreve(Fraction startTime) {
	return new Symbol(null, null, null, startTime, SymbolType.ALLA_BREVE, null, new TimeSignature(2, NoteValue.HALF), null);
    }

    public static Symbol newTimeSignatureCommonTime(Fraction startTime) {
	return new Symbol(null, null, null, startTime, SymbolType.COMMON_TIME, null, new TimeSignature(4, NoteValue.QUARTER), null);
    }

     public static Symbol newKeySignature(Fraction startTime,
 					 KeySignature.Modifier a,
 					 KeySignature.Modifier b,
 					 KeySignature.Modifier c,
 					 KeySignature.Modifier d,
 					 KeySignature.Modifier e,
 					 KeySignature.Modifier f,
 					 KeySignature.Modifier g) {
 	return new Symbol(null, null, null, startTime, SymbolType.KEY_SIGNATURE, null, null, new KeySignature(a, b, c, d, e, f, g));
     }
 }
