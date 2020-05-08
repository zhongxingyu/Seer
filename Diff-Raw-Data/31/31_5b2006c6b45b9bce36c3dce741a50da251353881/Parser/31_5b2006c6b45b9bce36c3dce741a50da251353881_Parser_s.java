 package player;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiUnavailableException;
 import player.Token.Type;
 import visitors.Duration;
 import datatypes.Accidental;
 import datatypes.Body;
 import datatypes.Chord;
 import datatypes.Fraction;
 import datatypes.Header;
 import datatypes.KeySignature;
 import datatypes.Note;
 import datatypes.Player;
 import datatypes.Rest;
 import datatypes.Tuplet;
 import datatypes.Voice;
 
 public class Parser {
     
     private final Lexer lexer;
     private Header header;
     private Body body;
     private Voice[] voices;
     
     private int currentKey[]; // used when parsing 
 
     /**
      * Creates Parser over passed Lexer.
      * @param lexer Lexer object to parse
      */
     public Parser(Lexer lexer) {
         this.lexer = lexer;
         parseHeader();
         parseBody();
     }
 
     /**
      * Creates a Player object from parsed head and body of an abc file
      * @return Player object as above
      * @throws RuntimeException if InvalidMidiDataException or MidiUnvailableException encountered
      */
     public Player parse()
     {
         try
         {
             if(body == null || header == null) System.out.println("body is null in parse()");
             Player player = new Player(header, body);
             player.getBody().accept( new Duration(player) );
             return player;
         }
         catch(InvalidMidiDataException e)
         {
             throw new RuntimeException("Invalid Midi Data. Check your abc file.");
         }
        catch( MidiUnavailableException e)
         {
             throw new RuntimeException("Midi Unavailable");
         }
             
     }
     
     /**
      * Parses a String to be returned as a Fraction
      * @param str String representation of a Fraction
      * @return Fraction object corresponding to this representation
      * @throws RuntimeException if str does not represent a Fraction
      */
     private Fraction parseFraction(String str)
     {
         int split = str.indexOf("/");
         if(split<=0 || split==str.length()-1)
             throw new RuntimeException("Not a complete fraction");
         
         return new Fraction(Integer.parseInt(str.substring(0, split)),
                             Integer.parseInt(str.substring(split+1, str.length())));
     }
     
     /**
      * Parses a duration multiplier (e.g. for a Note or a Rest)
      * @param str String representation of the multiplier
      * @return double value of the multiplier
      * @throws RuntimeException if str does not represent a Fraction
      */
     private double parseMultiplier(String str)
     {
         int split = str.indexOf('/');
         
         if(split == -1) // not fraction: A2
             return Integer.parseInt(str);
         else // fraction: A3/4
         {
             int numerator,denominator;
             
             if(split>0) numerator = Integer.parseInt(str.substring(0, split)); else numerator=1;
             if(split<str.length()-1) denominator = Integer.parseInt(str.substring(split+1, str.length())); else denominator=2;
             return ((double) numerator) / denominator;
         }
     }
     
     /**
      * Reads the multiplier from the Lexer and returns its parsed value
      * @return double value of the multiplier
      * @throws RuntimeException if next Token not TYPE.NOTEMULTIPLIER
      */
     private double readMultiplier()
     {
         Token token = lexer.nextBody();
         if(token.getType() != Type.NOTEMULTIPLIER) throw new RuntimeException("multiplier expected");
         return parseMultiplier(token.getValue());
     }
     
     /**
      * Parses a Rest token
      * @return Rest object corresponding with the Rest token
      */
     private Rest readRest()
     {
         double multiplier;
         
         lexer.consumeBody(Type.REST);
         
        // Read 1/4, /3, 5/, ..
        if(lexer.peekBody()!=null && lexer.peekBody().getType()==Type.NOTEMULTIPLIER)
            multiplier = readMultiplier();
        else
            multiplier = 1;
         
         return new Rest(multiplier);
     }
     
     /**
      * Parses a Note token
      * @return Note object corresponding with the Note token
      * @throws RuntimeException in case of incorrectly formatted Note tokens
      */
     private Note readNote()
     {
         Token token = lexer.nextBody();
         Accidental accidental;
         char note;
         int octave = 0;
         double multiplier;
         
         // Read accidental if exist
         // ** Double accidentals are two tokens
         if(token.getType() == Type.ACCIDENTAL)
         {
             Token second = lexer.peekBody();
             if(second.getType() == Type.ACCIDENTAL)
             {
                 lexer.nextBody();
                 accidental = new Accidental(token.getValue() + second.getValue()); // double sharp or flat
             }
             else
                 accidental = new Accidental(token.getValue());
 
             token = lexer.nextBody();
         }
         else
             accidental = null;
 
         // Read basenote
         if(token.getType()!=Type.BASENOTE)
             throw new RuntimeException("readNote: This is not basenote: " + token.getType() + " " + token.getValue());
         note = token.getValue().charAt(0);
         if(note>='a' && note<='z') {octave++; note=(char)(note-'a'+'A');}
         
         if(accidental != null)
         {
             currentKey[note-'A'] = accidental.getIntRep();
         }
         else
         {
             accidental = new Accidental(currentKey[note-'A']);
         }
         
         // Read octave modifiers
         if(lexer.peekBody()!=null && lexer.peekBody().getType()==Type.OCTAVE)
         {
             token = lexer.nextBody();
             String str = token.getValue();
             
             if(str.equals(","))
             {
                 --octave;
                 while(lexer.peekBody()!=null && lexer.peekBody().getType()==Type.OCTAVE)
                 {
                     if(lexer.nextBody().getValue().equals(","))
                         --octave;
                     else
                         throw new RuntimeException("mixed octave");
                 }
             }
             else if(str.equals("'"))
             {
                 ++octave;
                 while(lexer.peekBody()!=null && lexer.peekBody().getType()==Type.OCTAVE)
                 {
                     if(lexer.nextBody().getValue().equals("'"))
                         ++octave;
                     else
                         throw new RuntimeException("mixed octave");
                     
                 }
             }
         }
         
         
        // Read 1/4, /3, 5/, ..
        if(lexer.peekBody()!=null && lexer.peekBody().getType()==Type.NOTEMULTIPLIER)
            multiplier = readMultiplier();
        else
            multiplier = 1;
 
         return new Note(note, octave, accidental, multiplier);
     }
 
     /**
      * Parses a Chord token
      * @return Chord object corresponding with the Chord token
      */
     private Chord readChord()
     {
         Token token;
         List<Note> list = new ArrayList<Note>();
         
         lexer.consumeBody(Type.CHORDSTART);
         
         while((token = lexer.peekBody()).getType() != Type.CHORDEND) 
             list.add(readNote());
         lexer.consumeBody(Type.CHORDEND);
 
/*        double len = list.get(0).getNoteMultiplier();
        for(Note note : list)
            if(note.getNoteMultiplier() != len)
                throw new RuntimeException("different length of nodes in a chord"); */

         return new Chord(list);
     }
     
     /**
      * Parses a Tuplet token
      * @return Tuplet object corresponding with the Tuplet token
      * @throws RuntimeException in case of invalid tuplet number or wrong tuplet length
      */
     private Tuplet readTuplet()
     {
         Token token = lexer.nextBody();
         String str = token.getValue();
         
         if(token.getType() != Type.TUPLET || str.length()!=2 || str.charAt(0)!='(')
             throw new RuntimeException("Invalid token: " + token.getType() + ": " + token.getValue());
         
         if(!(str.charAt(1)>='2' && str.charAt(1)<='4'))
             throw new RuntimeException("Invalid tuplet length.");
         
         List<Note> list = new ArrayList<Note>();
         int i, n = str.charAt(1) - '0';
         for(i=0 ; i<n; ++i)
             list.add(readNote());
         return new Tuplet(list);
         
     }
     /**
      * Parses a Voice token
      * @return Voice object corresponding with the Rest token
      * @throws RuntimeException if next token is not a Voice or if no Voice with this name exists 
      */
     private Voice readVoice()
     {
         Token token = lexer.nextBody();
         String str = token.getValue();
         str = str.substring(2,  str.length());
         
         if(token.getType() != Type.VOICE)
             throw new RuntimeException("Voice expected");
         
         for(Voice v : voices){
             if(v.getVoiceName().trim().equals(str.trim()))
                 return v;
         }
         throw new RuntimeException("No voice found with name: " + str);        
     }
     
     /**
      * Parses the header of the abc file, stores the result in this.header
      * @throws RuntimeException in case of badly formatted header
      */
     private void parseHeader()
     {
         //*****
         int index, tempo = 0;
         String title, composer = null;
         Fraction length = null,meter = null;
         KeySignature keySignature = null;
         List<String> voiceNames = new ArrayList<String>();
         //*****
         
         Token token;
         
         token=lexer.nextHeader();
         if(token.getType() != Type.INDEX) throw new RuntimeException("index expected");
         index = Integer.parseInt(token.getValue());
         
         token=lexer.nextHeader();
         if(token.getType() != Type.TITLE) throw new RuntimeException("title expected");
         title = token.getValue();
         
         while( (token=lexer.nextHeader()).getType() != Type.KEY)
         {            
             Type type = token.getType();
             
             if(type == Type.VOICE)
                 voiceNames.add(token.getValue());
             else if(type == Type.COMPOSER)
             {
                 if(composer != null) throw new RuntimeException("Duplicated composer");
                 composer = token.getValue();
             }
             else if(type == Type.NOTELENGTH)
             {
                 if(length != null) throw new RuntimeException("Duplicated Note length");
                 length = parseFraction(token.getValue());
             }
             else if(type == Type.METER)
             {
                 if(meter != null) throw new RuntimeException("Duplicated Meter");
                 String str = token.getValue();
                 if(str.charAt(0)=='C')
                     if(str.equals("C|"))
                         meter = new Fraction(2,2);
                     else if(str.equals("C")) meter = new Fraction(4,4);
                     else  throw new RuntimeException("Can't understand meter");
                 else
                     meter = parseFraction(token.getValue());
             }
             else if(type == Type.TEMPO)
             {
                 if(tempo != 0) throw new RuntimeException("Duplicated tempo");
                 tempo = Integer.parseInt(token.getValue());
             }
             else
                 throw new RuntimeException("I don't know this header token");
         }
         // last is Key
         if(token.getType() != Type.KEY) throw new RuntimeException("Last token must be the key signature.");
         keySignature = KeySignature.getType(token.getValue());
         
         header = new Header(index, title, keySignature);
         if(composer != null) header.setComposer(composer);
         if(meter != null) header.setMeter(meter);
         if(length!=null) header.setDefaultNoteLengthFraction(length);
         if(tempo>0) header.setTempo(tempo);
         header.setVoiceNames(voiceNames.toArray(new String[]{}));        
     }
     
     /**
      * Parses the body of the abc file, stores the result in this.body
      * @throws RuntimeException in case of badly formatted body
      */
     private void parseBody()
     {
         if(header == null) throw new RuntimeException("parse header first!");
 
         int i;
         Token token;
         body = new Body();
         
         String voiceNames[] = header.getVoiceNames();
         Voice currentVoice;
         
         if(voiceNames.length > 0)
         {
             voices = new Voice[voiceNames.length];
             for(i=0;i<voices.length;++i)
                 body.addVoice( voices[i] = new Voice(voiceNames[i]) );
             currentVoice = readVoice();
         }
         else
         {
             
             body.addVoice( currentVoice = new Voice("Default voice") );
             voices = new Voice[] {currentVoice};
         }
         
         currentKey = KeySignature.getType(header.getKeySignature().getStringRep()).getKeyAccidentals().clone();
 
         while( (token=lexer.peekBody()) != null)
         {            
             Type type = token.getType();
 
             if(type == Type.VOICE)
                 currentVoice = readVoice();
             else if(type == Type.CHORDSTART)
                 currentVoice.add(readChord());
             else if(type == Type.REST)
                 currentVoice.add(readRest());
             else if(type == Type.ACCIDENTAL || type == Type.BASENOTE)
                 currentVoice.add(readNote());
             else if(type == Type.TUPLET)
                 currentVoice.add(readTuplet());
             else if(type == Type.REPEATSTART)
             {
                 lexer.nextBody();
                 currentVoice.repeatStart();
             }
             else if(type == Type.REPEATSECTION) // [1 or [2
             {
                 lexer.nextBody();
                 currentVoice.repeatSection();
             }
             else if(type == Type.REPEATEND)
             {
                 lexer.nextBody();
                 currentVoice.repeatEnd();
             }
             else if(type == Type.BARLINE)
             {
                 lexer.nextBody();
                 // return to default keySignature
                 currentKey = KeySignature.getType(header.getKeySignature().getStringRep()).getKeyAccidentals().clone();
             }
             else if(type == Type.ENDMAJORSECTION)
             {
                 lexer.nextBody();
                 // over! but there can be still other voices
                 currentVoice.setClosed();
                 currentKey = KeySignature.getType(header.getKeySignature().getStringRep()).getKeyAccidentals().clone();
             }
             else
             {
                 throw new RuntimeException("Invalid token:" + " " + token.getValue() + " " + token.getType().toString());
             }
         }
     }
 }
