 package sound;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Parser {
     private final Lexer lex;
     
     /**
      * Creates a new parser over the lexer.  This parser will use the passed
      * lexer to get tokens--which it will then parse.
      * 
      * @param lexer The lexer.
      * @return none
      */
     
     public final static HashMap<Key, HashMap<NoteType, Accidental>> KEY_MAPPINGS = 
             new HashMap<Key, HashMap<NoteType, Accidental>>();
     
     private final HashMap<String, List<Playable>> voiceMappings;
    
     private HashMap<NoteType, Accidental> currentKeyMappings;
 
     private HashMap<String, int[]> indices;
     
     public Parser(Lexer lexer) {
         this.lex = lexer;  
         Parser.initializeKeyMappingsConstant();
         voiceMappings = new HashMap<String, List<Playable>>();
         indices = new HashMap<String, int[]>();
     }
     
     private static void initializeKeyMappingsConstant()
     {   
         //C major and A Minor 
         KEY_MAPPINGS.put(Key.C_MAJOR, new HashMap<NoteType, Accidental>());
         KEY_MAPPINGS.put(Key.A_MINOR, new HashMap<NoteType, Accidental>());
         
         //G major and E minor
         HashMap<NoteType, Accidental> map = new HashMap<NoteType, Accidental>(); 
         map.put(NoteType.F,  Accidental.SHARP);
         KEY_MAPPINGS.put(Key.G_MAJOR, map);
         KEY_MAPPINGS.put(Key.E_MINOR, new HashMap<NoteType, Accidental>(map));
         
         //F major and D minor
         map = new HashMap<NoteType, Accidental>(); 
         map.put(NoteType.B, Accidental.FLAT);
         KEY_MAPPINGS.put(Key.F_MAJOR, map);
         KEY_MAPPINGS.put(Key.D_MINOR, new HashMap<NoteType, Accidental>(map));
         
         //D major and B minor
         map = new HashMap<NoteType, Accidental>(); 
         map.put(NoteType.C, Accidental.SHARP);
         map.put(NoteType.F, Accidental.SHARP);
         KEY_MAPPINGS.put(Key.D_MAJOR, map); 
         KEY_MAPPINGS.put(Key.B_MINOR, new HashMap<NoteType, Accidental>(map));
         
         //G minor 
         map = new HashMap<NoteType, Accidental>(); 
         map.put(NoteType.B, Accidental.FLAT);
         map.put(NoteType.E, Accidental.FLAT);
         KEY_MAPPINGS.put(Key.G_MINOR, map); 
         
         //A major 
         map = new HashMap<NoteType, Accidental>(); 
         map.put(NoteType.C, Accidental.SHARP);
         map.put(NoteType.F, Accidental.SHARP);
         map.put(NoteType.G,  Accidental.SHARP);
         KEY_MAPPINGS.put(Key.A_MAJOR, map); 
         
         //C minor
         map = new HashMap<NoteType, Accidental>(); 
         map.put(NoteType.A, Accidental.FLAT);
         map.put(NoteType.B, Accidental.FLAT);
         map.put(NoteType.E, Accidental.FLAT);
         KEY_MAPPINGS.put(Key.C_MINOR, map); 
         
         //E major
         map = new HashMap<NoteType, Accidental>(); 
         map.put(NoteType.C, Accidental.SHARP);
         map.put(NoteType.D, Accidental.SHARP);
         map.put(NoteType.F, Accidental.SHARP);
         map.put(NoteType.G, Accidental.SHARP);
         KEY_MAPPINGS.put(Key.E_MAJOR, map);
         
         //F minor
         map = new HashMap<NoteType, Accidental>();
         map.put(NoteType.A, Accidental.FLAT);
         map.put(NoteType.B, Accidental.FLAT);
         map.put(NoteType.D, Accidental.FLAT);
         map.put(NoteType.E, Accidental.FLAT);
         KEY_MAPPINGS.put(Key.F_MINOR, map); 
         
         //B major
         map = new HashMap<NoteType, Accidental>();
         map.put(NoteType.A, Accidental.SHARP);
         map.put(NoteType.C, Accidental.SHARP);
         map.put(NoteType.D, Accidental.SHARP);
         map.put(NoteType.F, Accidental.SHARP);
         map.put(NoteType.G, Accidental.SHARP);
         KEY_MAPPINGS.put(Key.B_MAJOR, map);  
         
         //NEED TO ADD MAPPINGS FOR OTHER UNUSUAL KEYS
     }
     
     public Piece Parse()
     {
         Header header = this.parseHeader();
         String currentVoiceName = null;
         if (voiceMappings.size() == 1)
         {
             currentVoiceName = (String) voiceMappings.keySet().toArray()[0];
         }
         
         this.currentKeyMappings = new HashMap<NoteType, Accidental> (KEY_MAPPINGS.get(header.getKeySignature()));
 
         
         for (Token tok = this.lex.next(); tok.getTokenType() != Token.TokenType.END_OF_PIECE; tok = this.lex.next()) {
             switch (tok.getTokenType()) {
   
             // Body Tokens
             case NOTE:
                 voiceMappings.get(currentVoiceName).add(parseNote(tok.getTokenName()));
                 break;
             case REST:
                 voiceMappings.get(currentVoiceName).add(parseRest(tok.getTokenName()));
                 break;
             case CHORD:
                 voiceMappings.get(currentVoiceName).add(parseChord(tok.getTokenName()));
                 break;
             case DUPLET:
                 voiceMappings.get(currentVoiceName).add(parseDuplet(tok.getTokenName()));
                 break;
             case TRIPLET:
                 voiceMappings.get(currentVoiceName).add(parseTriplet(tok.getTokenName()));
                 break;
             case QUADRUPLET:
                 voiceMappings.get(currentVoiceName).add(parseQuadruplet(tok.getTokenName()));
                 break;
             case BARLINE:
                 this.currentKeyMappings = new HashMap<NoteType, Accidental>(KEY_MAPPINGS.get(header.getKeySignature())); 
                 break;
 
             case VOICE:
                 
                 currentVoiceName = tok.getTokenName();
                 if (!indices.containsKey(currentVoiceName)) {              
                     int[] index = {0, 0, 0, 0}; // startIndex, endIndex, firstRepeatIndex, secondRepeatIndex
                     indices.put(currentVoiceName, index);
                 }
 
                 break;
             case START_REPEAT:
                 indices.get(currentVoiceName)[0] = voiceMappings.get(currentVoiceName).size();
                 
                 break; 
             case END_REPEAT:
                 int[] voiceIndices = indices.get(currentVoiceName);
                 voiceIndices[1] = voiceMappings.get(currentVoiceName).size();
                 
                 if (voiceIndices[2] != -1) {
                     for (int i = voiceIndices[0]; i<voiceIndices[2]; i++) {
                         voiceMappings.get(currentVoiceName).add(voiceMappings.get(currentVoiceName).get(i));
                     }
                     voiceIndices[2] = -1;
                 }
                 else {
                     for (int i = voiceIndices[0]; i < voiceIndices[1]; i++) {
                         voiceMappings.get(currentVoiceName).add(voiceMappings.get(currentVoiceName).get(i));
                     }
                 }                 
                 break; 
             case REPEAT_FIRST_ENDING:    
                 indices.get(currentVoiceName)[2] = voiceMappings.get(currentVoiceName).size();          
                 break;
             case REPEAT_SECOND_ENDING:
                 indices.get(currentVoiceName)[3] = voiceMappings.get(currentVoiceName).size();
                 
                 break;          
             default:
                 throw new RuntimeException("Unrecognized token!");
             }
             
         }
         
         List<Voice> voicesInPiece = new ArrayList<Voice>();
         for(Map.Entry<String, List<Playable>> entry: voiceMappings.entrySet())
         {
             Voice toAdd = new Voice(entry.getKey(), entry.getValue());
             voicesInPiece.add(toAdd);
         }
         
         return new Piece(voicesInPiece, header);
     }
   
     public Rest parseRest(String noteToken) {
         RatNum restLength = getLength(noteToken);
         Rest rest = new Rest(restLength);
         return rest;
     }
     
     public Note parseNote(String noteToken) {
         Accidental noteAccidental = getAccidental(noteToken);
         NoteType noteName = getNote(noteToken);
         int octave = getOctave(noteToken);
         RatNum noteLength = getLength(noteToken);
         
         if (noteAccidental == Accidental.ABSENT)
         {
             if (currentKeyMappings.containsKey(noteName)) {
                 noteAccidental = currentKeyMappings.get(noteName);
             }   
         }
         
         else
         {
             currentKeyMappings.put(noteName, noteAccidental);
         }
         
         Note parsedNote = (noteAccidental == Accidental.ABSENT) ? new Note(noteName, octave, noteLength) 
                             : new Note(noteName, octave, noteLength, noteAccidental);
         return parsedNote;
     }
     final String NOTE_EXPRESSION = "(__?|\\^\\^?|=)?[A-Ga-g]['+,+]*([0-9]*/[0-9]*|[0-9]+)?";
     
     public Chord parseChord(String noteToken) {
         List<Note> chords = new ArrayList<Note>();
         
         Pattern chordPattern = Pattern.compile(NOTE_EXPRESSION);
         Matcher chordMatcher = chordPattern.matcher(noteToken);
         int index = 0;
         while (chordMatcher.find(index)) {
             chords.add(parseNote(chordMatcher.group(0)));
             index = chordMatcher.end();
         } 
         
         return new Chord(chords);
     }
     
     public Tuplet parseDuplet(String noteToken) {
         List<Note> duplet = new ArrayList<Note>();
 
         Pattern notePattern = Pattern.compile(NOTE_EXPRESSION);
         Matcher noteMatcher = notePattern.matcher(noteToken);
         int index = 0;
         
         while (noteMatcher.find(index)) {
             Note dupletNote = parseNote(noteMatcher.group(0));
             RatNum newNoteLength = new RatNum(dupletNote.getLength().getNumer()*3, dupletNote.getLength().getDenom()*2);
             dupletNote.setNoteLength(newNoteLength);
             duplet.add(dupletNote);
             index = noteMatcher.end();
         }
         if (duplet.size() != 2)
             throw new RuntimeException("Duplet does not contain 2 notes");
         
         return new Tuplet(TupletType.DUPLET, duplet);
     }
 
     public Tuplet parseTriplet(String noteToken) {
         List<Note> triplet = new ArrayList<Note>();
 
         Pattern notePattern = Pattern.compile(NOTE_EXPRESSION);
         Matcher noteMatcher = notePattern.matcher(noteToken);
         int index = 0;
         
         while (noteMatcher.find(index)) {
             Note tripletNote = parseNote(noteMatcher.group(0));
             RatNum newNoteLength = new RatNum(tripletNote.getLength().getNumer()*2, tripletNote.getLength().getDenom()*3);
             tripletNote.setNoteLength(newNoteLength);
             triplet.add(tripletNote);
             index = noteMatcher.end();
         }
         if (triplet.size() != 3)
             throw new RuntimeException("Triplet does not contain 3 notes");
         
         return new Tuplet(TupletType.TRIPLET, triplet);
     }
     
     public Tuplet parseQuadruplet(String noteToken) {
         List<Note> quadruplet = new ArrayList<Note>();
 
         Pattern notePattern = Pattern.compile(NOTE_EXPRESSION);
         Matcher noteMatcher = notePattern.matcher(noteToken);
         int index = 0;
         
         while (noteMatcher.find(index)) {
             Note quadrupletNote = parseNote(noteMatcher.group(0));
             RatNum newNoteLength = new RatNum(quadrupletNote.getLength().getNumer()*2, quadrupletNote.getLength().getDenom()*3);
             quadrupletNote.setNoteLength(newNoteLength);
             quadruplet.add(quadrupletNote);
             index = noteMatcher.end();
         }
         if (quadruplet.size() != 4)
             throw new RuntimeException("Quadruplet does not contain 4 notes");
         
         return new Tuplet(TupletType.QUADRUPLET, quadruplet);
     }
     
     private Accidental getAccidental(String noteToken) {
     	String ACCIDENTAL_REGEX = "(__)|(_)|(\\^\\^)|(\\^)|(=)";
     	Pattern accidentalPattern = Pattern.compile(ACCIDENTAL_REGEX);
     	Matcher accidentalMatcher = accidentalPattern.matcher(noteToken);
     	
     	int groupMatch = 0;
     	for (int i=1; i<=accidentalMatcher.groupCount(); ++i) {
     		if (accidentalMatcher.find(0)) {
 	    		if (accidentalMatcher.group(i) != null) {
 	    			groupMatch = i;
 	    			break;
 	    		}
     		}
     	}
 
     	switch (groupMatch) {
     		case 1: return Accidental.DOUBLEFLAT; 
     		case 2: return Accidental.FLAT;
     		case 3: return Accidental.DOUBLESHARP;
     		case 4: return Accidental.SHARP;
     		case 5: return Accidental.NATURAL;
     		default: return Accidental.ABSENT;
     	}
     }
     
     private NoteType getNote(String noteToken) {
     	String NOTE_REGEX = "([Aa])|([Bb])|([Cc])|([Dd])|([Ee])|([Ff])|([Gg])";
     	Pattern notePattern = Pattern.compile(NOTE_REGEX);
     	Matcher noteMatcher = notePattern.matcher(noteToken);
 
     	int groupMatch = 0;
     	for (int i=1; i<=noteMatcher.groupCount(); ++i) {
     		if (noteMatcher.find(0)) {
 	    		if (noteMatcher.group(i) != null) {
 	    			groupMatch = i;
 	    			break;
 	    		}
     		}
     	}
     	switch (groupMatch) {
     		case 1:
     			return NoteType.A; 
     		case 2:
     			return NoteType.B;
     		case 3:
     			return NoteType.C;
     		case 4:
     			return NoteType.D;
     		case 5:
     			return NoteType.E;
     		case 6:
     			return NoteType.F;
     		case 7:
     			return NoteType.G;
     		default:
     			throw new IllegalArgumentException("Illegal Note");
     	}
     }
     
     private int getOctave(String noteToken) {
     	String HIGHER_OCTAVE_REGEX = "[a-g]";
     	String OCTAVE_REGEX = "('+)|(,+)";
     	Pattern higherOctave = Pattern.compile(HIGHER_OCTAVE_REGEX);
     	Pattern octavePattern = Pattern.compile(OCTAVE_REGEX);
     	
     	Matcher higherOctaveMatcher = higherOctave.matcher(noteToken);
     	Matcher octaveMatcher = octavePattern.matcher(noteToken);
     	
     	int octave = higherOctaveMatcher.find() ? 1 : 0;
     	String octaves = octaveMatcher.find() ? octaveMatcher.group(0) : "";
     	
     	if (octaveMatcher.find(0)) {
 	    	if (octaveMatcher.group(1) != null) octave += octaves.length();
 	    	if (octaveMatcher.group(2) != null) octave -= octaves.length();
     	}
     	
     	return octave;
     }
     
     private RatNum getLength(String noteToken) {
     	String LENGTH_REGEX = "([0-9]+/[0-9]+)|(/[0-9]+)|([0-9]+/)|(/)|([0-9]+)";
     	Pattern lengthPattern = Pattern.compile(LENGTH_REGEX);
     	
     	Matcher lengthMatcher = lengthPattern.matcher(noteToken);
     	String length = lengthMatcher.find() ? lengthMatcher.group(0) : "";
 
     	if (lengthMatcher.find(0)) {
 	    	if (lengthMatcher.group(1) != null) {
 	    		String[] rational = length.split("/");
 	    		if (rational.length == 2) {
	    			System.out.println(length);
 	    			return new RatNum(Integer.parseInt(rational[0]), Integer.parseInt(rational[1]));
 	    		}
 	    		throw new IllegalArgumentException("Should not reach here");
 	    	}
 	    	
 	    	if (lengthMatcher.group(2) != null) {
 	    		return new RatNum(1,Integer.parseInt(length.substring(1)));
 	    	}
 	    	
 	    	if (lengthMatcher.group(3) != null) {
 	    		return new RatNum(Integer.parseInt(length.substring(0, length.indexOf('/'))),2);
 	    	}
 	    	
 	    	if (lengthMatcher.group(4)!= null) {
 	    		return new RatNum(1,2);
 	    	}
     	
 	    	if (lengthMatcher.group(5) != null) {
 	    		return new RatNum(Integer.parseInt(length));
 	    	}
     	}
     	return new RatNum(1);
     }
     
     private Header parseHeader() {
         //First two fields must be index number and title 
         Integer indexNumber = null;
         String title = null; 
        
         //Optional fields
         String composerName = null; 
         Integer tempo = null; 
         RatNum defaultNoteLength = null;
         //NEED TO ADD ONE FOR METER
         
         //Last field must be the key 
         Key keySignature = null;
       
         int iterationCount = 0; 
         
         Outer:
         while (true) {
             Token tok = lex.next(); 
             
             switch(tok.getTokenType()) {
                 case INDEX_NUMBER: 
                     if (iterationCount != 0) {
                         throw new IllegalArgumentException("Index number did not appear as the first element in the header");
                     }
                     
                     indexNumber = Integer.valueOf(tok.getTokenName());
                     break;
                 
                 case TITLE: 
                     if (iterationCount != 1)  {
                         throw new IllegalArgumentException("Title did not appear as the second element in the header");
                     }
                     
                     title = tok.getTokenName(); 
                     break;
                 
                 case COMPOSER_NAME: 
                     composerName = tok.getTokenName(); 
                     break;
                 
                 case METER: 
                     //Need to handle this
                     break;
                 
                 case TEMPO: 
                     tempo = Integer.valueOf(tok.getTokenName());
                     break;
                 
                 case VOICE: 
                     String voiceName = tok.getTokenName(); 
                     voiceMappings.put(voiceName, new ArrayList<Playable>());
                     break;
                 
                 case KEY: 
                     HashMap<String, Key> helperMappings = new HashMap<String, Key>(); 
                     
                     //Standard major keys
                     helperMappings.put("A", Key.A_MAJOR); 
                     helperMappings.put("B", Key.B_MAJOR);
                     helperMappings.put("C", Key.C_MAJOR);
                     helperMappings.put("D", Key.D_MAJOR);
                     helperMappings.put("E", Key.E_MAJOR);
                     helperMappings.put("F", Key.F_MAJOR);
                     helperMappings.put("G", Key.G_MAJOR);
                     
                     //Standard minor keys
                     helperMappings.put("Am", Key.A_MINOR); 
                     helperMappings.put("Bm", Key.B_MINOR);
                     helperMappings.put("Cm", Key.C_MINOR);
                     helperMappings.put("Dm", Key.D_MINOR);
                     helperMappings.put("Em", Key.E_MINOR);
                     helperMappings.put("Fm", Key.F_MINOR);
                     helperMappings.put("Gm", Key.G_MINOR);
                     
                     //Other keys 
                     helperMappings.put("B_", Key.B_FLAT_MAJOR); 
                     helperMappings.put("F^m", Key.F_SHARP_MINOR);
                     helperMappings.put("E_", Key.E_FLAT_MAJOR);
                     helperMappings.put("C^m", Key.C_SHARP_MINOR);
                     helperMappings.put("A_", Key.A_FLAT_MAJOR);
                     helperMappings.put("G^m", Key.G_SHARP_MINOR);
                     helperMappings.put("D_", Key.D_FLAT_MAJOR);
                     
                     helperMappings.put("B_m", Key.B_FLAT_MINOR); 
                     helperMappings.put("F^", Key.F_SHARP_MAJOR);
                     helperMappings.put("D^", Key.D_SHARP_MAJOR);
                     helperMappings.put("G_", Key.G_FLAT_MAJOR);
                     helperMappings.put("E_m", Key.E_FLAT_MINOR);
                     helperMappings.put("C_", Key.C_FLAT_MAJOR);
                     helperMappings.put("A_m", Key.A_FLAT_MINOR);
                    
                     String keyText = tok.getTokenName().trim(); 
                     if (!helperMappings.containsKey(keyText)) {
                         keySignature = null;
                     }
                     
                     else {
                         keySignature = helperMappings.get(keyText);
                     }
                     
                     break Outer;
                 
                 case LENGTH: 
                     defaultNoteLength = this.getLength(tok.getTokenName());
                     break;
                     
                 default:
                 	break;
            
               
             }
    
             iterationCount++;
         }
         
         if (indexNumber == null || title == null || keySignature == null) {
             throw new IllegalArgumentException("Required fields were not present in the header");
         }
         
         Header headerToReturn = new Header(title, keySignature, indexNumber);
         
         if (composerName != null) {
             headerToReturn.setComposer(composerName); 
         } else {
             headerToReturn.setComposer("Unspecified");
         }
         
         if (tempo != null) {
             headerToReturn.setTempo(tempo.intValue()); 
         } else {
             headerToReturn.setTempo(Header.DEFAULT_TEMPO);
         }
         
         if (defaultNoteLength != null) {
             headerToReturn.setNoteLength(defaultNoteLength);
         } else {
             headerToReturn.setNoteLength(Header.DEFAULT_NOTE_LENGTH);
         }
         
         if (voiceMappings.entrySet().isEmpty()) {
             voiceMappings.put(Voice.DEFAULT_VOICE_NAME, new ArrayList<Playable>());
         }
         
         return headerToReturn;
     }
 }
