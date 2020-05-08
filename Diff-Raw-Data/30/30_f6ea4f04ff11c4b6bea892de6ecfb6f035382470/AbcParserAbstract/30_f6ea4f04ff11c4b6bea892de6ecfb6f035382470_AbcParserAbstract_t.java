 package abc.parser;
 
 import java.util.Vector;
 
 import scanner.CharStreamPosition;
 import scanner.FinaleStateAutomata;
 import scanner.InvalidCharacterEvent;
 import scanner.NoSuchTokenException;
 import scanner.Scanner;
 import scanner.ScannerListenerInterface;
 import scanner.Set;
 import scanner.Token;
 import scanner.TokenEvent;
 import scanner.TokenType;
 import abc.notation.AccidentalType;
 import abc.notation.BarLine;
 import abc.notation.Fraction;
 import abc.notation.KeySignature;
 import abc.notation.MultiPartsDefinition;
 import abc.notation.Note;
 import abc.notation.NoteAbstract;
 import abc.notation.RepeatBarLine;
 import abc.notation.RepeatedPart;
 import abc.notation.RepeatedPartAbstract;
 import abc.notation.ScorePresentationElementInterface;
 import abc.notation.StaffEndOfLine;
 import abc.notation.Tempo;
 import abc.notation.TimeSignature;
 import abc.notation.Tune;
 import abc.notation.Tuplet;
 import abc.notation.Words;
 import abc.parser.AbcToolkit.DurationDescription;
 import abc.parser.def.DefinitionFactory;
 
 /** Abstract class from which all abc notation parsers inherit.
  * Known limitations:
  * ELEMSKIP is not supported. 
  * Missing support for tex command and file fields. */
  //
  // The context to switch from abc header parsing to abc music parsing
  // is done in <PRE>private KeySignature parseFieldKey(Set follow)</PRE>.*/
 public class AbcParserAbstract
 {
   protected static final Set FIRST_END_OF_LINE = new Set(AbcTokenType.LINE_FEED).union(AbcTokenType.SPACE)
       .union(AbcTokenType.COMMENT);
   protected static final Set FIRST_PART_SPEC = new Set(AbcTokenType.PART).union(AbcTokenType.PARENTHESIS_OPEN);
   protected static final Set FIRST_PARTS = FIRST_PART_SPEC;
 
   protected static final Set FIRST_NOTE_LENGTH_STRICT = new Set(AbcTokenType.NUMBER);
 
   protected static final Set FIRST_TEMPO = new Set(AbcTokenType.NUMBER).union(AbcTokenType.C_TEMPO).union(FIRST_NOTE_LENGTH_STRICT);
 
   protected static final Set FIRST_METER_FRACTION = new Set(AbcTokenType.NUMBER);
   protected static final Set FIRST_METER = new Set(FIRST_METER_FRACTION).union(AbcTokenType.C_METER);
 
   protected Set FIRST_GLOBAL_ACCIDENTAL = new Set(FIRST_ACCIDENTAL);
   protected static final Set FIRST_MODE = new Set(AbcTokenType.MODE);
   protected static final Set FIRST_MODE_SPEC = new Set(FIRST_MODE);//.union(AbcTokenType.SPACE);
   protected static final Set FIRST_KEY_ACCIDENTAL = new Set(AbcTokenType.KEY_ACCIDENTAL);
   protected static final Set FIRST_KEYNOTE = new Set(AbcTokenType.BASE_NOTE);
   protected static final Set FIRST_KEYSPEC = new Set(FIRST_KEYNOTE);
   protected static final Set FIRST_KEY = new Set(FIRST_KEYSPEC).union(AbcTokenType.KEY_HP);
 
   protected static final Set FIRST_FIELD_KEY = new Set(AbcTokenType.FIELD_KEY);
   protected static final Set FIRST_FIELD_TRANSCRNOTES = new Set(AbcTokenType.FIELD_TRANSCRNOTES);
   protected static final Set FIRST_FIELD_SOURCE = new Set(AbcTokenType.FIELD_SOURCE);
   protected static final Set FIRST_FIELD_RHYTHM = new Set(AbcTokenType.FIELD_RHYTHM);
   protected static final Set FIRST_FIELD_TEMPO = new Set(AbcTokenType.FIELD_TEMPO);
   protected static final Set FIRST_FIELD_PARTS = new Set(AbcTokenType.FIELD_PARTS);
   protected static final Set FIRST_FIELD_ORIGIN = new Set(AbcTokenType.FIELD_ORIGIN);
   protected static final Set FIRST_FIELD_NOTES = new Set(AbcTokenType.FIELD_NOTES);
   protected static final Set FIRST_FIELD_METER = new Set(AbcTokenType.FIELD_METER);
   protected static final Set FIRST_FIELD_DEFAULT_LENGTH = new Set(AbcTokenType.FIELD_DEFAULT_LENGTH);
   protected static final Set FIRST_FIELD_INFORMATION = new Set(AbcTokenType.FIELD_INFORMATION);
   protected static final Set FIRST_FIELD_HISTORY = new Set(AbcTokenType.FIELD_HISTORY);
   protected static final Set FIRST_FIELD_GROUP = new Set(AbcTokenType.FIELD_GROUP);
   //protected static final Set FIRST_FIELD_ELEMSKIP = new Set(AbcTokenType.FIELD_ELEMSKIP);
   protected static final Set FIRST_FIELD_DISCOGRAPHY = new Set(AbcTokenType.FIELD_DISCOGRAPHY);
   protected static final Set FIRST_FIELD_COMPOSER = new Set(AbcTokenType.FIELD_COMPOSER);
   protected static final Set FIRST_FIELD_BOOK = new Set(AbcTokenType.FIELD_BOOK);
   protected static final Set FIRST_FIELD_AREA = new Set(AbcTokenType.FIELD_AREA);
   protected static Set FIRST_COMMENT = new Set(AbcTokenType.COMMENT);
   protected static final Set FIRST_OTHER_FIELDS = new Set(FIRST_FIELD_AREA).union(FIRST_FIELD_BOOK).union(FIRST_FIELD_COMPOSER)
                                                             .union(FIRST_FIELD_DISCOGRAPHY)/*.union(FIRST_FIELD_ELEMSKIP)*/
                                                             .union(FIRST_FIELD_GROUP).union(FIRST_FIELD_HISTORY)
                                                             .union(FIRST_FIELD_INFORMATION).union(FIRST_FIELD_DEFAULT_LENGTH)
                                                             .union(FIRST_FIELD_METER).union(FIRST_FIELD_NOTES).union(FIRST_FIELD_ORIGIN)
                                                             .union(FIRST_FIELD_PARTS).union(FIRST_FIELD_RHYTHM).union(FIRST_FIELD_SOURCE)
                                                             .union(FIRST_FIELD_TEMPO).union(FIRST_FIELD_TRANSCRNOTES).union(FIRST_COMMENT);
     protected static final Set FIRST_FIELD_TITLE = new Set(AbcTokenType.FIELD_TITLE);
     protected static final Set FIRST_FIELD_NUMBER = new Set(AbcTokenType.FIELD_NUMBER);
     protected static Set FIRST_ABCHEADER = new Set(FIRST_FIELD_NUMBER);
 
     //==========================================================================
     protected static Set FIRST_TEXT_CHAR = new Set(AbcTokenType.TEXT);
     protected static Set FIRST_TEXT = new Set(FIRST_TEXT_CHAR);
     protected static Set FIRST_LINE_FEED = new Set(AbcTokenType.LINE_FEED);
     protected static Set FIRST_NO_LINE_BREAK = new Set(AbcTokenType.NO_LINE_BREAK);
     protected static Set FIRST_LINE_BREAK = new Set(AbcTokenType.LINE_BREAK);
 
     protected static Set FIRST_SPACE = new Set(AbcTokenType.SPACE);
     //protected static Set FIRST_TEX_COMMAND = new Set(AbcTokenType.TEX_COMMAND);
 
     protected static Set FIRST_USER_DEFINED = new Set(AbcTokenType.USER_DEFINED);
 
     protected static Set FIRST_FIELD_WORDS = new Set(AbcTokenType.FIELD_WORDS);
     protected static Set FIRST_FIELD_PART = new Set(AbcTokenType.FIELD_PARTS);
     protected static Set FIRST_TUNE_FIELD = new Set(/*FIRST_FIELD_ELEMSKIP).union(*/FIRST_FIELD_KEY).union(FIRST_FIELD_DEFAULT_LENGTH)
         .union(FIRST_FIELD_METER).union(FIRST_FIELD_PART).union(FIRST_FIELD_TEMPO).union(FIRST_FIELD_TITLE).union(FIRST_FIELD_WORDS);
     protected static Set FIRST_MID_TUNE_FIELD = FIRST_TUNE_FIELD;
     protected static Set FIRST_END_SLUR = new Set(AbcTokenType.END_SLUR);
     protected static Set FIRST_BEGIN_SLUR = new Set(AbcTokenType.BEGIN_SLUR);
     protected static Set FIRST_NTH_REPEAT = new Set(AbcTokenType.NTH_REPEAT);
     protected static Set FIRST_BARLINE = new Set(AbcTokenType.BARLINE);
 
     protected static Set FIRST_CHORD_TYPE = new Set(AbcTokenType.CHORD_TYPE);
     protected Set FIRST_FORMAL_CHORD = new Set(FIRST_BASE_NOTE);
     protected static Set FIRST_GUITAR_CHORD = new Set(AbcTokenType.GUITAR_CHORD);
 
     protected static Set FIRST_GRACE_NOTES = new Set(AbcTokenType.GRACING_BEGIN);
     protected static Set FIRST_GRACINGS = new Set(AbcTokenType.GRACING);
     protected static Set FIRST_TIE = new Set(AbcTokenType.TIE);
     protected static Set FIRST_BROKEN_RHYTHM = new Set(AbcTokenType.BROKEN_RHYTHM);
     protected static Set FIRST_REST = new Set(AbcTokenType.REST);
     protected static Set FIRST_BASE_NOTE = new Set(AbcTokenType.BASE_NOTE);
     protected static Set FIRST_ACCIDENTAL = new Set(AbcTokenType.ACCIDENTAL);
     protected static Set FIRST_NOTE_LENGTH = new Set(AbcTokenType.NUMBER).union(AbcTokenType.FRACTION);
     protected static Set FIRST_OCTAVE = new Set(AbcTokenType.OCTAVE);
     protected static Set FIRST_PITCH = new Set(FIRST_ACCIDENTAL).union(FIRST_BASE_NOTE);
     protected static Set FIRST_NOTE_OR_REST = new Set(FIRST_PITCH).union(FIRST_REST);
     protected static Set FIRST_NOTE = new Set(FIRST_NOTE_OR_REST);
     protected static Set FIRST_MULTI_NOTE = new Set(AbcTokenType.MULTI_NOTE_BEGIN);
     protected static Set FIRST_NOTE_STEM = new Set(FIRST_GUITAR_CHORD).union(FIRST_GRACE_NOTES).union(FIRST_GRACINGS).union(FIRST_NOTE).union(FIRST_MULTI_NOTE);
     protected static Set FIRST_NOTE_ELEMENT = new Set(FIRST_NOTE_STEM);
 
     protected static Set FIRST_TUPLET_SPEC = new Set(AbcTokenType.TUPLET_SPEC);
     protected static Set FIRST_TUPLET_ELEMENT = new Set(FIRST_TUPLET_SPEC);
 
     protected static Set FIRST_LINE_ENDER = new Set(AbcTokenType.COMMENT).union(AbcTokenType.LINE_FEED).union(AbcTokenType.LINE_BREAK)
         .union(AbcTokenType.NO_LINE_BREAK);
     protected static Set FIRST_ELEMENT = new Set(FIRST_NOTE_ELEMENT).union(FIRST_TUPLET_ELEMENT).union(AbcTokenType.BARLINE)
         .union(AbcTokenType.NTH_REPEAT).union(AbcTokenType.BEGIN_SLUR).union(AbcTokenType.END_SLUR).union(AbcTokenType.SPACE)
         /*.union(AbcTokenType.USER_DEFINED)*/;
     protected Set FIRST_ABC_LINE = new Set(FIRST_ELEMENT).union(FIRST_MID_TUNE_FIELD)/*.union(FIRST_COMMENT).union(FIRST_TEX_COMMAND)*/;
     protected Set FIRST_ABC_MUSIC = new Set(FIRST_ABC_LINE);
 
     //==========================================================================
     protected static Set FIRST_ABCTUNE = new Set(FIRST_ABCHEADER);
     /** The scanner used for parsing. */
     protected Scanner m_scanner;
     /** */
     protected FinaleStateAutomata m_automata = null;
     /** */
     protected ScannerListenerInterface m_scannerListener = null;
     /** Listeners of this parser. */
     protected Vector m_listeners;
     /** the current token used by this parser. */
     protected Token m_token = null;
     /** The type of the current token 
      * @see #m_token */
     protected TokenType m_tokenType = TokenType.UNKNOWN;
     /** The number of dots inherited from the previous note broken rythm. */
     private byte brknRthmDotsCorrection = 0;
     /** */
     private boolean m_isPartOfSlur = false;
     /** The current default note length. */
     private short m_defaultNoteLength = Note.EIGHTH;
 
     private TimeSignature m_timeSignature = null;
     /** The score of the current part. */
     private Tune.Score m_score = null;
     /** The tune resulting of the last parsing. */
     protected Tune m_tune = null;
 
     /** Constructs a new tune parser. */
     public AbcParserAbstract()
     {
       m_scanner = new Scanner();
       m_automata = new FinaleStateAutomata();
       //m_scanner.setFinaleStateAutomata(m_automata);
       m_scannerListener = new ScannerListenerInterface()
       {
         //=======================================SCANNER LISTENER BEGIN
         public void tokenGenerated(TokenEvent evt)
         {}
 
         public void invalidCharacter(InvalidCharacterEvent evt)
         { notifyListenersForInvalidCharacter(evt); }
 
         public void lineProcessed(String line)
         { }
         //=======================================SCANNER LISTENER END
       };
       m_scanner.addListener(m_scannerListener);
       m_listeners = new Vector();
     }
 
     /** Returns the scanner internally used for parsing.
      * @return The scanner internally used for parsing. */
     public Scanner getScanner()
     { return m_scanner; }
 
     /** Adds a listener to catch events thrown by the parser durin tune parsing.
      * @param listener Object that implements the TuneParserListenerInterface. */
     public void addListener (TuneParserListenerInterface listener)
     { m_listeners.addElement(listener); }
 
     /** Removes a listener from this parser.
      * @param listener The listener to be removed. */
     public void removeListener (TuneParserListenerInterface listener)
     { m_listeners.removeElement(listener); }
 
     /** abc-file ::= *(abc-tune / comment / linefeed / tex-command / file-fields) */
 	protected void parseAbcFile(Set follow) {
 		Set current = FIRST_ABCTUNE.createUnion(FIRST_COMMENT).createUnion(FIRST_LINE_FEED);
 		// are missing TEX COMMAND and FILE FIELDS
 		//.createUnion(FIRST_TEX_COMMAND);//.createUnion(FIRST_FILE_FIELDS);
 		m_scanner.setFinaleStateAutomata(AutomataFactory.getAutomata(current.getTypes()));
 		m_token = m_scanner.nextToken();
 		m_tokenType = m_token.getType();
 		while (m_token!=null) {
 			if (FIRST_ABCTUNE.contains(m_token.getType())) {
 				notifyListenersForTuneBegin();
 				Tune tune = parseAbcTune(current.createUnion(follow));
 				notifyListenersForTuneEnd(tune);
 			}
 			else if (FIRST_COMMENT.contains(m_token.getType()))
 				parseComment(current.createUnion(follow));
 			else if (FIRST_LINE_FEED.contains(m_token.getType()))
 				accept(AbcTokenType.LINE_FEED, current, (current.createUnion(follow)));
         //else if (Syntax.FIRST_TEX_COMMAND.contains(token.getType())) parseAbcTune(current.createUnion(follow));
         //else if (Syntax.FIRST_FILE_FIELDS.contains(token.getType())) parseAbcTune(current.createUnion(follow));
 		}
 	}
 	
 	/** Inits the parsing : sets the starting state of the current Set +
 	 * sets the finale state automata of the scanner + retrieves the first 
 	 * token and its type.
 	 * @return The starting state of the current Set as it should
 	 * be at the begining of the parsing. */
 	/*protected Set initParsing() {
 		Set startingSet = FIRST_ABCTUNE.createUnion(FIRST_COMMENT).createUnion(FIRST_LINE_FEED);
 		// are missing TEX COMMAND and FILE FIELDS
 		//.createUnion(FIRST_TEX_COMMAND);//.createUnion(FIRST_FILE_FIELDS);
 		m_scanner.setFinaleStateAutomata(AutomataFactory.getAutomata(startingSet.getTypes()));
 		m_token = m_scanner.nextToken();
 		m_tokenType = m_token.getType();
 		return startingSet;
 	}*/
 
     /** abc-tune ::= abc-header abc-music */
     protected Tune parseAbcTune(Set follow) {
       Set current = new Set()./*union(FIRST_ABCHEADER).*/union(FIRST_ABC_MUSIC);
       parseAbcHeader(current.createUnion(follow));
       current.remove(FIRST_ABC_MUSIC);
       parseAbcMusic(current.createUnion(follow));
       return m_tune;
     }
 
     /** other-fields ::= field-area / field-book / field-composer / field-discography /
      * field-elemskip / field-group / field-history / field-information / 
      * field-default-length / field-meter / field-notes / field-origin / field-parts / 
      * field-tempo / field-rhythm / field-source / field-transcrnotes / comment */
     private void parseOtherFields(Set follow) //throws TuneNotationException
     {
         //System.out.println("AbcScoreParser - parse()");
         // CURRENT, for any given rule S contains all terminals accepted by S
         // as well as the set of all the FIRSTs for all other rules invoked by S.
         //Object returnedObject = null;
 /*        Set current = FIRST_FIELD_AREA.union(FIRST_FIELD_BOOK).union(FIRST_FIELD_COMPOSER)
             .createUnion(FIRST_FIELD_DISCOGRAPHY).createUnion(FIRST_FIELD_ELEMSKIP).createUnion(FIRST_FIELD_GROUP)
             .createUnion(FIRST_FIELD_HISTORY).createUnion(FIRST_FIELD_INFORMATION).createUnion(FIRST_FIELD_NOTES)
             .createUnion(FIRST_FIELD_ORIGIN).createUnion(FIRST_FIELD_RHYTHM).createUnion(FIRST_FIELD_SOURCE)
             .createUnion(FIRST_FIELD_TRANSCRNOTES).createUnion(FIRST_COMMENT);*/
         //Set current = new Set();
 /*        if (m_tokenType.equals(AbcTokenType.FIELD_AREA)) current.remove(FIRST_FIELD_AREA);
         else if (m_tokenType.equals(AbcTokenType.FIELD_BOOK)) current.remove(FIRST_FIELD_BOOK);
         else if (m_tokenType.equals(AbcTokenType.FIELD_COMPOSER)) current.remove(FIRST_FIELD_COMPOSER);
         else if (m_tokenType.equals(AbcTokenType.FIELD_DISCOGRAPHY))
           current.remove(FIRST_FIELD_DISCOGRAPHY);
         else if (m_tokenType.equals(AbcTokenType.FIELD_ELEMSKIP)) current.remove(FIRST_FIELD_ELEMSKIP);
         else if (m_tokenType.equals(AbcTokenType.FIELD_GROUP)) current.remove(FIRST_FIELD_GROUP);
         else if (m_tokenType.equals(AbcTokenType.FIELD_HISTORY)) current.remove(FIRST_FIELD_HISTORY);
         else if (m_tokenType.equals(AbcTokenType.FIELD_INFORMATION)) current.remove(FIRST_FIELD_INFORMATION);
         else if (m_tokenType.equals(AbcTokenType.FIELD_DEFAULT_LENGTH)) current.remove(FIRST_FIELD_DEFAULT_LENGTH);
         else if (m_tokenType.equals(AbcTokenType.FIELD_METER)) current.remove(FIRST_FIELD_METER);
         else if (m_tokenType.equals(AbcTokenType.FIELD_NOTES)) current.remove(FIRST_FIELD_NOTES);
         else if (m_tokenType.equals(AbcTokenType.FIELD_ORIGIN)) current.remove(FIRST_FIELD_ORIGIN);
         else if (m_tokenType.equals(AbcTokenType.FIELD_RHYTHM)) current.remove(FIRST_FIELD_RHYTHM);
         else if (m_tokenType.equals(AbcTokenType.FIELD_SOURCE)) current.remove(FIRST_FIELD_SOURCE);
         else if (m_tokenType.equals(AbcTokenType.FIELD_TRANSCRNOTES)) current.remove(FIRST_FIELD_TRANSCRNOTES);*/
 
         if (m_tokenType.equals(AbcTokenType.FIELD_HISTORY))
           parseFieldHistory(follow);
         else
         if (m_tokenType.equals(AbcTokenType.FIELD_DEFAULT_LENGTH))
         {
           short defaultNoteLength =  parseFieldDefaultLength(follow);
           if (defaultNoteLength!=-1)
             m_defaultNoteLength = defaultNoteLength;
         }
         else
         if (m_tokenType.equals(AbcTokenType.FIELD_METER))
         {
           TimeSignature meter = parseFieldMeter(follow);
           if (meter!=null)
           {
             m_score.addElement(meter);
             m_defaultNoteLength = meter.getDefaultNoteLength();
             m_timeSignature = meter;
           }
         }
         else
         if (m_tokenType.equals(AbcTokenType.FIELD_PARTS))
         {
           MultiPartsDefinition parts = parseFieldParts(follow);
           if (parts!=null) m_tune.setMultiPartsDefinition(parts);
         }
         else
         if (m_tokenType.equals(AbcTokenType.FIELD_TEMPO))
         {
           Tempo tempo = parseFieldTempo(follow);
           if (tempo!=null) m_score.addElement(tempo);
         }
         else
         if (m_tokenType.equals(AbcTokenType.COMMENT))
         {
           //current.remove(AbcTokenType.COMMENT);
           parseComment(follow);
         }
         else
         {
           AbcTextField field = parseField(m_tokenType, follow);
           if (field!=null)
             if (field.getType() == AbcTextField.AREA) m_tune.setArea(field.getText());
             else if (field.getType() == AbcTextField.BOOK) m_tune.setBook(field.getText());
             else if (field.getType() ==  AbcTextField.COMPOSER) m_tune.setComposer(field.getText());
             else if (field.getType() == AbcTextField.DISCOGRAPHY) m_tune.setDiscography(field.getText());
             else if (field.getType() == AbcTextField.GROUP) m_tune.setGroup(field.getText());
             else if (field.getType() == AbcTextField.HISTORY) m_tune.addHistory(field.getText());
             else if (field.getType() == AbcTextField.INFORMATION) m_tune.setInformation(field.getText());
             else if (field.getType() == AbcTextField.NOTES) m_tune.setNotes(field.getText());
             else if (field.getType() == AbcTextField.ORIGIN) m_tune.setOrigin(field.getText());
             else if (field.getType() == AbcTextField.RHYTHM) m_tune.setRhythm(field.getText());
             else if (field.getType() == AbcTextField.SOURCE) m_tune.setSource(field.getText());
             else if (field.getType() == AbcTextField.TRANSCRNOTES) m_tune.addTranscriptionNotes(field.getText());
         }
     }
 
     private AbcTextField parseField(TokenType tokenType, Set follow)
     {
         //System.out.println("TuneParser - parseFieldArea(" + token + ")");
         Set current= new Set().union(FIRST_END_OF_LINE).union(AbcTokenType.TEXT);
 
         String ret = accept(tokenType, current, follow);
 
         current.remove(AbcTokenType.TEXT);
         String text = accept(AbcTokenType.TEXT, current, follow);
 
         current.remove(FIRST_END_OF_LINE);
         parseEndOfLine(current.createUnion(follow));
 
         if (ret!=null && text!=null)
         {
           if (tokenType.equals(AbcTokenType.FIELD_AREA)) return new AbcTextField(AbcTextField.AREA, text);
           else if (tokenType.equals(AbcTokenType.FIELD_BOOK)) return new AbcTextField(AbcTextField.BOOK, text);
           else if (tokenType.equals(AbcTokenType.FIELD_COMPOSER)) return new AbcTextField(AbcTextField.COMPOSER, text);
           else if (tokenType.equals(AbcTokenType.FIELD_DISCOGRAPHY)) return new AbcTextField(AbcTextField.DISCOGRAPHY, text);
           //else if (tokenType.equals(AbcTokenType.FIELD_ELEMSKIP)) return new AbcTextField(AbcTextField.ELEMSKIP, text);
           else if (tokenType.equals(AbcTokenType.FIELD_GROUP)) return new AbcTextField(AbcTextField.GROUP, text);
           else if (tokenType.equals(AbcTokenType.FIELD_INFORMATION)) return new AbcTextField(AbcTextField.INFORMATION, text);
           else if (tokenType.equals(AbcTokenType.FIELD_NOTES)) return new AbcTextField(AbcTextField.NOTES, text);
           else if (tokenType.equals(AbcTokenType.FIELD_ORIGIN)) return new AbcTextField(AbcTextField.ORIGIN, text);
           else if (tokenType.equals(AbcTokenType.FIELD_RHYTHM)) 
         	  return new AbcTextField(AbcTextField.RHYTHM, text);
           else if (tokenType.equals(AbcTokenType.FIELD_SOURCE)) return new AbcTextField(AbcTextField.SOURCE, text);
           else if (tokenType.equals(AbcTokenType.FIELD_TITLE)) return new AbcTextField(AbcTextField.TITLE, text);
           else if (tokenType.equals(AbcTokenType.FIELD_TRANSCRNOTES)) return new AbcTextField(AbcTextField.TRANSCRNOTES, text);
           else if (tokenType.equals(AbcTokenType.FIELD_WORDS)) return new AbcTextField(AbcTextField.WORDS, text);
           return null;
         }
         else
           return null;
     }
 
     /** field-default-length ::= "L:" note-length-strict end-of-line */
     private short parseFieldDefaultLength(Set follow)
     {
         //System.out.println("TuneParser - parseFieldArea(" + token + ")");
         Set current = new Set().union(FIRST_NOTE_LENGTH_STRICT).union(FIRST_END_OF_LINE);
 
         accept(AbcTokenType.FIELD_DEFAULT_LENGTH, current, follow);
 
         current.remove(FIRST_NOTE_LENGTH_STRICT);
         short noteLength = parseNoteLengthStrict(current.createUnion(follow));
 
         current.remove(FIRST_END_OF_LINE);
         parseEndOfLine(current.createUnion(follow));
 
         return noteLength;
     }
 
     /** note-length ::= [1*DIGIT] ["/" [1*DIGIT]] */
     private Fraction parseNoteLength(Set follow)
     {
       Set current = new Set(AbcTokenType.NUMBER).union(AbcTokenType.FRACTION);
       byte num = 1;
       byte denom = 1;
 
       if (m_tokenType.equals(AbcTokenType.NUMBER))
       {
         String acc = accept(AbcTokenType.NUMBER, current, follow, true);
         if (acc!=null)
           num = Byte.parseByte(acc);
       }
       if (m_tokenType.equals(AbcTokenType.FRACTION))
       {
         current.remove(AbcTokenType.FRACTION);
         accept(AbcTokenType.FRACTION, current, follow, true);
         denom=2; // If only '/' in the notation, means divided by 2.
         current.remove(AbcTokenType.NUMBER);
         if (m_tokenType.equals(AbcTokenType.NUMBER))
         {
           String acc = accept(AbcTokenType.NUMBER, current, follow);
           if (acc!=null)
             denom = Byte.parseByte(acc);
         }
       }
        return new Fraction(num, denom);
     }
 
     /** note-length-strict ::= 1*DIGIT "/" 1*DIGIT */
     private short parseNoteLengthStrict(Set follow)
     {
         Set current = new Set(AbcTokenType.NUMBER).union(AbcTokenType.FRACTION);
         short noteLength = -1;
         String numString = accept(AbcTokenType.NUMBER, current, follow);
 
         current.remove(AbcTokenType.FRACTION);
         accept(AbcTokenType.FRACTION, current, follow);
 
         current.remove(AbcTokenType.NUMBER);
         String denomString = accept(AbcTokenType.NUMBER, current, follow);
         if (numString!=null && denomString!=null)
         {
           int num = Integer.parseInt(numString);
           int denom = Integer.parseInt(denomString);
           noteLength = Note.convertToNoteLengthStrict(num, denom);
         }
         return noteLength;
     }
 
     /** field-meter ::= "M:" meter end-of-line */
     private TimeSignature parseFieldMeter(Set follow)
     {
         Set current = new Set().union(FIRST_METER).union(FIRST_END_OF_LINE);
         TimeSignature meter = null;
 
         accept(AbcTokenType.FIELD_METER, current, follow);
 
         current.remove(FIRST_METER);
         meter = parseMeter(current.createUnion(follow));
 
         current.remove(FIRST_END_OF_LINE);
         parseEndOfLine(current.createUnion(follow));
 
         return meter;
     }
 
     /** meter ::= "C" / "C|" / meter-fraction */
     private TimeSignature parseMeter(Set follow)
     {
       //Set current = new Set();
       TimeSignature meter = null;
       if (m_tokenType.equals(AbcTokenType.C_METER))
       {
         String C = accept(AbcTokenType.C_METER, null, follow);
         if (C.equals("C"))
           meter = new TimeSignature(4,4);
         else
           meter = new TimeSignature(2,2);
       }
       else
         meter = parseMeterFraction(follow);
       return meter;
     }
 
     /** meter-fraction ::= 1*DIGIT "/" 1*DIGIT */
     private TimeSignature parseMeterFraction(Set follow)
     {
         Set current = new Set(AbcTokenType.NUMBER).union(AbcTokenType.FRACTION);
         TimeSignature fraction = null;
         String numString = accept(AbcTokenType.NUMBER, current, follow);
 
         current.remove(AbcTokenType.FRACTION);
         accept(AbcTokenType.FRACTION, current, follow);
 
         current.remove(AbcTokenType.NUMBER);
         String denomString = accept(AbcTokenType.NUMBER, current, follow);
         if (numString!=null && denomString!=null)
         {
           int num = Integer.parseInt(numString);
           int denom = Integer.parseInt(denomString);
           fraction = new TimeSignature(num, denom);
         }
         return fraction;
     }
 
     /** field-parts ::= "P:" parts end-of-line */
     private MultiPartsDefinition parseFieldParts(Set follow)
     {
         Set current = new Set().union(FIRST_PARTS).union(FIRST_END_OF_LINE);
 
         accept(AbcTokenType.FIELD_PARTS, current, follow);
 
         current.remove(FIRST_PARTS);
         MultiPartsDefinition parts = parseParts(current.createUnion(follow));
 
         current.remove(FIRST_END_OF_LINE);
         parseEndOfLine(current.createUnion(follow));
         return parts;
     }
 
     /** field-part ::= "P:" part end-of-line */
     private char parseFieldPart(Set follow)
     {
         Set current = new Set().union(FIRST_END_OF_LINE).union(AbcTokenType.PART);
         accept(AbcTokenType.FIELD_PARTS, current, follow);
         current.remove(AbcTokenType.PART);
         String partLabelString = accept(AbcTokenType.PART, current, follow);
         char partLabel = ' ';
         if (partLabelString!=null)
           partLabel = partLabelString.charAt(0);
 
         current.remove(FIRST_END_OF_LINE);
         parseEndOfLine(current.createUnion(follow));
         return partLabel;
     }
 
     /** parts ::= 1*part-spec */
     private MultiPartsDefinition parseParts(Set follow)
     {
       Set current = new Set().union(FIRST_PART_SPEC);
       MultiPartsDefinition parts = new MultiPartsDefinition();
       do
       {
          RepeatedPartAbstract partSpec = parsePartSpec(current.createUnion(follow));
          if (partSpec!=null)
            parts.addPart(partSpec);
       }
       while (FIRST_PART_SPEC.contains(m_tokenType));
       return parts;
     }
 
     /** part-spec ::= (part / ( "(" 1*part-spec ")" ) ) *DIGIT */
     private RepeatedPartAbstract parsePartSpec(Set follow)
     {
       Set current = new Set().union(FIRST_PART_SPEC).union(AbcTokenType.PART).union(AbcTokenType.PARENTHESIS_OPEN)
           .union(AbcTokenType.PARENTHESIS_CLOSE).union(AbcTokenType.DIGIT);
       RepeatedPartAbstract parts = null;
       if (m_tokenType.equals(AbcTokenType.PARENTHESIS_OPEN))
       {
         current.remove(AbcTokenType.PARENTHESIS_OPEN);
         accept(AbcTokenType.PARENTHESIS_OPEN, current, follow);
         parts = new MultiPartsDefinition();
         do
         {
           RepeatedPartAbstract partSpec = parsePartSpec(current.createUnion(follow));
           if (partSpec!=null)
             ((MultiPartsDefinition)parts).addPart(partSpec);
         }
         while(FIRST_PART_SPEC.contains(m_tokenType));
         current.remove(FIRST_PART_SPEC);
         current.remove(AbcTokenType.PARENTHESIS_CLOSE);
         accept(AbcTokenType.PARENTHESIS_CLOSE, current, follow, true);
       }
       else
       // we suppose it's a AbcTokenType.PART then
       {
         current.remove(AbcTokenType.PART);
         String partNameAsString = accept(AbcTokenType.PART, current, follow, true);
         current.remove(AbcTokenType.PARENTHESIS_OPEN);
         current.remove(AbcTokenType.PARENTHESIS_CLOSE);
         if (partNameAsString!=null)
         {
           char partName = partNameAsString.charAt(0);
           //does this part already exist ?
           if (m_tune.getPart(partName)!=null)
             parts = new RepeatedPart(m_tune.getPart(partName));
           else
             parts = new RepeatedPart(m_tune.createPart(partName));
         }
       }
       if (m_tokenType.equals(AbcTokenType.DIGIT))
       {
         current.remove(AbcTokenType.DIGIT);
         String digit = accept(AbcTokenType.DIGIT, current, follow);
         if (digit!=null && parts!=null)
           parts.setNumberOfRepeats(Integer.parseInt(digit));
       }
       return parts;
     }
 
     private Tempo parseFieldTempo(Set follow)
     {
       Set current = new Set().union(FIRST_TEMPO).union(FIRST_END_OF_LINE);
       accept(AbcTokenType.FIELD_TEMPO, current, follow);
 
       current.remove(FIRST_TEMPO);
       Tempo tempo = parseTempo(current.createUnion(follow));
 
       current.remove(FIRST_END_OF_LINE);
       parseEndOfLine(current.createUnion(follow));
 
       return tempo;
     }
 
     /** field-tempo ::= "Q:" tempo end-of-line */
     private Tempo parseTempo(Set follow)
     {
       Set current = null;
       Tempo tempo = null;
       int tempoValue = -1;
       if (m_tokenType.equals(AbcTokenType.C_TEMPO))
       {
         current = new Set().union(FIRST_NOTE_LENGTH).union(AbcTokenType.EQUALS).union(AbcTokenType.NUMBER);
         short refLength = -1;
         accept(AbcTokenType.C_TEMPO, current, follow);
         current = new Set(AbcTokenType.EQUALS).union(AbcTokenType.NUMBER);
         Fraction length = new Fraction(1,1);
         if (FIRST_NOTE_LENGTH.contains(m_tokenType))
         {
           length = parseNoteLength(current.createUnion(follow));
           if (length!=null)
             refLength = (short)(m_defaultNoteLength * length.floatValue());
         }
         current.remove(AbcTokenType.EQUALS);
         accept(AbcTokenType.EQUALS, current, follow);
         current.remove(AbcTokenType.NUMBER);
         String tempoString = accept(AbcTokenType.NUMBER, current, follow);
         if (tempoString!=null)
           tempoValue = new Integer(tempoString).intValue();
         if (refLength!=-1 && tempoValue!=-1)
           tempo = new Tempo(refLength, tempoValue);
       }
       else
       {
         current = new Set(AbcTokenType.FRACTION);
         String tempoString = accept(AbcTokenType.NUMBER, current, follow, true);
         if (m_tokenType.equals(AbcTokenType.FRACTION))
         {
           //tempo has the form noteLengthStrict = 1*digit
           String num = tempoString;
           current = new Set(AbcTokenType.NUMBER).union(AbcTokenType.EQUALS);
           accept(AbcTokenType.FRACTION, current, follow);
           String denom = accept(AbcTokenType.NUMBER, current, follow);
           current.remove(AbcTokenType.EQUALS);
           accept(AbcTokenType.EQUALS, current, follow);
           current.remove(AbcTokenType.NUMBER);
           tempoString = accept(AbcTokenType.NUMBER, current, follow);
           if (num!=null && denom!=null && tempoString!=null)
           try
           { tempo = new Tempo(Note.convertToNoteLengthStrict(new Integer(num).intValue(), new Integer(denom).intValue()), new Integer(tempoString).intValue()); }
           catch (IllegalArgumentException e)
           {
             //Invalid tempo => just ignore it.
           }
         }
         else
         //tempo has the form 1*digit
         if (tempoString!=null)
           tempo = new Tempo(m_defaultNoteLength, new Integer(tempoString).intValue());
       }
       return tempo;
     }
 
     /** field-history ::= "H:" 1*(text end-of-line) */
     private AbcTextField parseFieldHistory(Set follow)
     {
         //System.out.println("TuneParser - parseFieldArea(" + token + ")");
         Set current= new Set().union(FIRST_END_OF_LINE).union(AbcTokenType.TEXT);
         AbcTextField history = null;
 
         accept(AbcTokenType.FIELD_HISTORY, current, follow);
 
         do
         {
           String acc = accept(AbcTokenType.TEXT, current, follow);
           if (acc!=null)
           {
             if (history==null)
               history = new AbcTextField(AbcTextField.HISTORY, acc);
             else
             {
               String text = history.getText().concat(acc);
               history = new AbcTextField(AbcTextField.HISTORY, text);
             }
           }
           parseEndOfLine(current.createUnion(follow));
         }
         while(m_tokenType.equals(AbcTokenType.TEXT));
         return history;
     }
 
     /** field-key ::= "K:" key end-of-line */
     private KeySignature parseFieldKey(Set follow)
     {
       KeySignature key = null;
       Set current = new Set().union(FIRST_KEY).union(FIRST_END_OF_LINE);
       accept(AbcTokenType.FIELD_KEY, current, follow);
 
       current.remove(FIRST_KEY);
       key = parseKey(current.createUnion(follow));
 
       current.remove(FIRST_END_OF_LINE);
       current = current.union(FIRST_ABC_MUSIC);
       parseEndOfLine(current.createUnion(follow));
       return key;
     }
 
     /** key ::= key-spec / "HP" / "Hp" */
     private KeySignature parseKey(Set follow)
     {
       //Set current = new Set();
       if (m_tokenType.equals(AbcTokenType.KEY_HP))
       {
         accept(AbcTokenType.KEY_HP, null, follow);
         return null;
       }
       else
         return parseKeySpec(follow);
     }
 
     /** key-spec ::= keynote [mode-spec] *(" " global-accidental) */
     private KeySignature parseKeySpec(Set follow)
     {
         Set current = new Set().union(FIRST_MODE_SPEC).union(AbcTokenType.SPACE).union(FIRST_GLOBAL_ACCIDENTAL);
         KeySignature key = null;
         Note note = null;
         byte modeSpec = KeySignature.MAJOR;
 
         note = parseKeyNote(current.createUnion(follow));
         if (FIRST_MODE_SPEC.contains(m_tokenType))
         {
           current = new Set(AbcTokenType.SPACE).union(FIRST_GLOBAL_ACCIDENTAL);
           modeSpec = parseModeSpec(current.createUnion(follow));
         }
         if (note!=null && modeSpec!=-1)
           key = new KeySignature(note.getHeight(), note.getAccidental(), modeSpec);
 
         while(m_tokenType.equals(AbcTokenType.SPACE))
         {
           accept(AbcTokenType.SPACE, current, follow, true);
           if (FIRST_GLOBAL_ACCIDENTAL.contains(m_tokenType))
           {
             byte[] ga = parseGlobalAccidental(current.createUnion(follow));
             if (ga!=null && key!=null)
               key.setAccidental(ga[0], ga[1]);
           }
         }
         return key;
     }
 
     /** keynote ::= basenote [key-accidental] */
     private Note parseKeyNote(Set follow)
     {
         Set current= new Set(AbcTokenType.KEY_ACCIDENTAL);
         Note keyNote = null;
         String note = null;
         String accidental = null;
 
         note = accept(AbcTokenType.BASE_NOTE, current, follow, true);
 
         if (m_tokenType.equals(AbcTokenType.KEY_ACCIDENTAL))
         {
           current.remove(AbcTokenType.KEY_ACCIDENTAL);
           accidental = accept(AbcTokenType.KEY_ACCIDENTAL, current, follow);
         }
 
         if (note!=null)
           keyNote = new Note(Note.convertToNoteType(note), KeySignature.convertToAccidentalType(accidental));
         return keyNote;
     }
 
     /** mode-spec ::= [" "] mode [extratext] */
     private byte parseModeSpec(Set follow)
     {
         Set current = new Set().union(FIRST_MODE).union(AbcTokenType.TEXT);
         byte modeType = KeySignature.MAJOR;
 /*        if (m_tokenType.equals(AbcTokenType.SPACE))
            accept(AbcTokenType.SPACE, current, follow);*/
         current.remove(FIRST_MODE);
         String stringMode = accept(AbcTokenType.MODE, current, follow, true);
         modeType = KeySignature.convertToModeType(stringMode);
 
         if (m_tokenType.equals(AbcTokenType.TEXT))
         {
           current.remove(AbcTokenType.TEXT);
           accept(AbcTokenType.TEXT, current, follow);
         }
         return modeType;
     }
 
     /** end-of-line ::= *(" " / HTAB) ["%" text] linefeed */
     private void parseEndOfLine(Set follow)
     {
         Set current = new Set(AbcTokenType.SPACE).union(AbcTokenType.COMMENT).union(AbcTokenType.TEXT).union(AbcTokenType.LINE_FEED);
         while (m_tokenType.equals(AbcTokenType.SPACE))
           accept(AbcTokenType.SPACE, current, follow);
         current.remove(AbcTokenType.SPACE);
         if (m_tokenType.equals(AbcTokenType.COMMENT))
         {
           current.remove(AbcTokenType.COMMENT);
           accept(AbcTokenType.COMMENT, current, follow);
           current.remove(AbcTokenType.TEXT);
           accept(AbcTokenType.TEXT, current, follow);
         }
         else
         {
           current.remove(AbcTokenType.COMMENT);
           current.remove(AbcTokenType.TEXT);
         }
         current.remove(AbcTokenType.LINE_FEED);
         accept(AbcTokenType.LINE_FEED, current, follow);
     }
 
     /** abc-header ::= field-number *comment 1*field-title *other-fields field-key
      * In practice, many tunes are e-mailed without field-number, 
      * so those wishing to implement an abc parser should treat this ; field as optional. */
     protected Tune parseAbcHeader(Set follow)
     {
       m_tune = new Tune();
       m_score = m_tune.getScore();
       Set current= new Set().union(FIRST_COMMENT).union(FIRST_FIELD_TITLE).union(FIRST_OTHER_FIELDS).union(FIRST_FIELD_KEY);
 
       Integer number = parseFieldNumber(current.createUnion(follow));
       if (number!=null)
         m_tune.setReferenceNumber(number.intValue());
 
       while (m_tokenType.equals(AbcTokenType.COMMENT))
         parseComment(current.createUnion(follow));
 
       //Work around : if the COMMENT is removed from the current
       //COMMENT is not considered as part as the follow when parsing the title
       //=> false because a comment can occur after a title : it's part of other
       //fields.
       current = new Set().union(FIRST_FIELD_TITLE).union(FIRST_OTHER_FIELDS).union(FIRST_FIELD_KEY);
 
       do
       {
         AbcTextField title = parseField(AbcTokenType.FIELD_TITLE, current.createUnion(follow));
         if (title!=null) m_tune.addTitle(title.getText());
       }
       while (m_tokenType.equals(AbcTokenType.FIELD_TITLE));
 
       current.remove(FIRST_FIELD_TITLE);
 
       while (FIRST_OTHER_FIELDS.contains(m_tokenType))
         parseOtherFields(current.createUnion(follow));
       
       current.remove(FIRST_OTHER_FIELDS);
 
       current.remove(FIRST_FIELD_KEY);
       //current = current.createUnion(FIRST_ABC_MUSIC);
       KeySignature key = parseFieldKey(current.createUnion(follow));
       if (key!=null) m_score.addElement(key);
       return m_tune;
     }
 
     /** field-number ::= "X:" 1*DIGIT end-of-line */
     private Integer parseFieldNumber(Set follow)
     {
         Set current= new Set(AbcTokenType.NUMBER).union(FIRST_END_OF_LINE);
         Integer number = null;
 
         accept(AbcTokenType.FIELD_NUMBER, current, follow);
 
         current.remove(AbcTokenType.NUMBER);
         String acc= accept(AbcTokenType.NUMBER, current, follow);
         if (acc!=null)
           number = new Integer(acc);
 
         current.remove(FIRST_END_OF_LINE);
         parseEndOfLine(current.createUnion(follow));
         return number;
     }
 
     /** global-accidental ::= accidental basenote */
     private byte[] parseGlobalAccidental(Set follow)
     {
       Set current= new Set(AbcTokenType.BASE_NOTE);
       byte[] globalAccidental = new byte[2];
 
       String keyAcc = accept(AbcTokenType.ACCIDENTAL, current, follow);
       byte accidentalType = Note.convertToAccidentalType(keyAcc);
 
       current.remove(AbcTokenType.BASE_NOTE);
       String noteHeigthString = accept(AbcTokenType.BASE_NOTE, current, follow);
       //byte index = 0;
       byte noteHeigth = Note.convertToNoteType(noteHeigthString);
 
       globalAccidental[0] = noteHeigth;
       globalAccidental[1] = accidentalType;
       return globalAccidental;
     }
 
     /** comment ::= "%" text (linefeed / no-line-break / line-break) */
     protected void parseComment(Set follow)
     {
       Set current = new Set(AbcTokenType.TEXT).union(AbcTokenType.LINE_FEED)
           .union(AbcTokenType.LINE_BREAK).union(AbcTokenType.NO_LINE_BREAK);
       accept(AbcTokenType.COMMENT, current, follow);
 
       current.remove(AbcTokenType.TEXT);
       accept(AbcTokenType.TEXT, current, follow);
 
       current.remove(AbcTokenType.LINE_FEED);
       current.remove(AbcTokenType.LINE_BREAK);
       current.remove(AbcTokenType.NO_LINE_BREAK);
       //String[] endOfLine = {AbcTokenType.LINE_FEED,AbcTokenType.LINE_BREAK,AbcTokenType.NO_LINE_BREAK};
       if (m_tokenType.equals(AbcTokenType.LINE_BREAK)) accept(AbcTokenType.LINE_BREAK, current, follow);
       else if (m_tokenType.equals(AbcTokenType.NO_LINE_BREAK)) accept(AbcTokenType.NO_LINE_BREAK, current, follow);
       else accept(AbcTokenType.LINE_FEED, current, follow);
     }
     
     //============================================================================================
     //================================== MUSIC PART ==============================================
     //============================================================================================
     /** abc-music ::= 1*abc-line linefeed */
     protected void parseAbcMusic(Set follow)
     {
       Set current = new Set().union(FIRST_ABC_LINE).union(AbcTokenType.LINE_FEED);
       do
         parseAbcLine(current.createUnion(follow));
       while (FIRST_ABC_LINE.contains(m_tokenType));
       //current = new Set(AbcTokenType.LINE_FEED);
       //accept(AbcTokenType.LINE_FEED, current, follow);
     }
 
     /** abc-line ::= (1*element line-ender) / mid-tune-field / tex-command */
     protected void parseAbcLine(Set follow)
     {
       //Set current = new Set();
       if (FIRST_ELEMENT.contains(m_tokenType))
       {
       	Set current = new Set().union(FIRST_ELEMENT).union(FIRST_LINE_ENDER);
         Set currentUnionFollow = current.createUnion(follow);
         do
           parseElement(currentUnionFollow);
         while (FIRST_ELEMENT.contains(m_tokenType));
         current.remove(FIRST_ELEMENT);
         current.remove(FIRST_LINE_ENDER);
         Object lineEnder = parseLineEnder(current.createUnion(follow));
         if (lineEnder!=null)
         	m_score.addElement(lineEnder);
       }
       else
         if (FIRST_MID_TUNE_FIELD.contains(m_tokenType))
           parseMidTuneField(follow);
 //        else
 //          parseTexCommand(current.createUnion(follow));
     }
 
     /** mid-tune-field ::= tune-field */
     private void parseMidTuneField(Set follow)
     {
       //Set current = new Set();//.union(FIRST_END_OF_LINE);
       parseTuneField(follow);
       //current.remove(FIRST_END_OF_LINE);
       //parseEndOfLine(current.createUnion(follow));
     }
 
     /** tune-field ::= field-elemskip / field-key / field-default-length / field-meter / 
      * field-part / field-tempo / field-title / field-words
      * field-rhythm may not be in tune (?) field-voice not defined yet */
     private void parseTuneField(Set follow)
     {
       //Set current = new Set();
 
       /*if (FIRST_FIELD_ELEMSKIP.contains(m_tokenType))
         parseField(AbcTokenType.FIELD_ELEMSKIP, follow);
       else*/
       if (FIRST_FIELD_KEY.contains(m_tokenType))
       {
         KeySignature key = parseFieldKey(follow);
         if (key!=null) m_score.addElement(key);
       }
       else
       if (FIRST_FIELD_DEFAULT_LENGTH.contains(m_tokenType))
       {
         short defaultNoteLength = parseFieldDefaultLength(follow);
         if (defaultNoteLength!=-1)
           m_defaultNoteLength = defaultNoteLength;
       }
       else
       if (FIRST_FIELD_METER.contains(m_tokenType))
       {
         TimeSignature meter = parseFieldMeter(follow);
         if (meter!=null)
         {
           m_score.addElement(meter);
           m_defaultNoteLength = meter.getDefaultNoteLength();
           m_timeSignature = meter;
         }
       }
       else
       if (FIRST_FIELD_PART.contains(m_tokenType))
       {
         char partLabel = parseFieldPart(follow);
         if (m_tune.getPart(partLabel)!=null)
           m_score= m_tune.getPart(partLabel).getScore();
         else
           // this part hasn't been used in the multi part definition but it's defined.
           m_score = m_tune.createPart(partLabel).getScore();
       }
       else
       if (FIRST_FIELD_TEMPO.contains(m_tokenType))
       {
         Tempo tempo = parseFieldTempo(follow);
         if (tempo!=null) m_score.addElement(tempo);
       }
       else
       if (FIRST_FIELD_WORDS.contains(m_tokenType)) {
         AbcTextField text = parseField(AbcTokenType.FIELD_WORDS, follow);
         if (text!=null)
         	m_score.addElement(new Words(text.getText()));
       }
       else
         parseField(AbcTokenType.FIELD_TITLE, follow);
     }
 
     /** element ::= note-element / tuplet-element / barline / nth-repeat /
      * begin-slur / end-slur / space / user-defined */
     private void parseElement(Set follow)
     {
       //Set current = new Set();
       if (FIRST_NOTE_ELEMENT.contains(m_tokenType))
       {
         //NoteAbstract note = parseNoteElement(current.createUnion(follow));
         //Experimentation
         NoteAbstract note = parseNoteElement(follow);
         m_score.addElement(note);
       }
       else
       if (FIRST_TUPLET_ELEMENT.contains(m_tokenType))
       {
         //Experimentation
         //Tuplet tuplet = parseTupletElement(current.createUnion(follow));
         Tuplet tuplet = parseTupletElement(follow);
         // tuplet is not put in the score itself, but notes composing the tuplet
         Vector notes = tuplet.getNotesAsVector();
         for (int i=0; i<notes.size(); i++)
           m_score.addElement(notes.elementAt(i));
       }
       else
       if (FIRST_BARLINE.contains(m_tokenType))
       {
         byte[] barLineTypes = BarLine.convertToBarLine(accept(AbcTokenType.BARLINE, null, follow));
         for (int i=0; i<barLineTypes.length; i++)
           m_score.addElement(new BarLine(barLineTypes[i]));
       }
       else
       if (FIRST_NTH_REPEAT.contains(m_tokenType))
       {
         byte repeatNumber = convertToRepeatBarLine(accept(AbcTokenType.NTH_REPEAT, null, follow));
         m_score.addElement(new RepeatBarLine(repeatNumber));
       }
       else
       if (m_tokenType.equals(AbcTokenType.BEGIN_SLUR))
       {
         accept(AbcTokenType.BEGIN_SLUR, null, follow);
         m_isPartOfSlur = true;
       }
       else
       if (m_tokenType.equals(AbcTokenType.END_SLUR))
       {
         accept(AbcTokenType.END_SLUR, null, follow);
         m_isPartOfSlur = false;
       }
       else
       if (m_tokenType.equals(AbcTokenType.SPACE))
         accept(AbcTokenType.SPACE, null, follow);
     }
 
     /** tuplet-element ::= tuplet-spec 1*note-element
      * */
     private Tuplet parseTupletElement(Set follow)
     {
       Set current = new Set().union(FIRST_NOTE_ELEMENT);
       int[] tupletSpec = parseTupletSpec(current.createUnion(follow));
       if (tupletSpec[1]==-1)
       {
    	 /*
         if (m_timeSignature!=null)
          if (m_timeSignature.isCoumpound())
            tupletSpec[1]=3;
          else
            tupletSpec[1]=2;
         else
        */
    	  /*
   	   		* The values of the particular tuplets are (to quote the abc specification)
			(2 	2 notes in the time of 3
			(3 	3 notes in the time of 2
			(4 	4 notes in the time of 3
			(5 	5 notes in the time of n
			(6 	6 notes in the time of 2
			(7 	7 notes in the time of n
			(8 	8 notes in the time of 3
			(9 	9 notes in the time of n
			n is 3 in compound time signatures (3/4, 3/8, 9/8 etc), 
			and 2 in simple time signatures (C, 4/4, 2/4 etc.)
    	   */
    	  if (tupletSpec[0]==2 || tupletSpec[0]==4 || tupletSpec[0]==8)
    		  tupletSpec[1]=3;
    	  else
    		  if (tupletSpec[0]==3 || tupletSpec[0]==6)
        		  tupletSpec[1]=2;
    		  else
    			  if (tupletSpec[0]==5 || tupletSpec[0]==7 || tupletSpec[0]==9)
    				  if (m_timeSignature.isCoumpound())
    			           tupletSpec[1]=3;
    			         else
    			           tupletSpec[1]=2;
    			  else
    				  System.err.println("Cannot evaluate tuplet : no time signature");
       }
       if (tupletSpec[2]==-1)
         tupletSpec[2]=tupletSpec[0];
 
       Vector notes = new Vector();
       int notesNumber = tupletSpec[2];
       do
       {
         NoteAbstract note = parseNoteElement(current.createUnion(follow));
         if (note!=null)
         	notes.addElement(note);
         notesNumber--;
       }
       while(FIRST_NOTE_ELEMENT.contains(m_tokenType) && notesNumber>0);
       return new Tuplet(notes, tupletSpec[1], m_defaultNoteLength);
     }
 
     /** tuplet-spec ::= "(" DIGIT [":" [DIGIT] [":" [DIGIT]]]
      * ([0]:[1]:[2]  == (p:q:r 
      * p = the number of notes to be put into time q
      * q = the time that p notes will be played in
      * r = the number of notes to continue to do this action for.
      * If q is not specified, it defaults to 3 in compound time
      * signatures and 2 in simple time signatures. If r is not specified, 
      * it is taken to be the same as p. */
     private int[] parseTupletSpec(Set follow)
     {
       Set current = new Set(AbcTokenType.COMA).union(AbcTokenType.DIGIT);
       int[] tupletDesc = {-1,-1,-1};
       String t = accept(AbcTokenType.TUPLET_SPEC, current, follow, true);
       if (t!=null)
       {
         tupletDesc[0] = Character.getNumericValue(t.charAt(t.length()-1));
         //System.out.println("TUPLET , first : " + tupletDesc[0]);
       }
       if(m_tokenType.equals(AbcTokenType.COMA))
       {
         accept(AbcTokenType.COMA, current, follow, true);
         if (m_tokenType.equals(AbcTokenType.DIGIT))
         {
           t = accept(AbcTokenType.DIGIT, current, follow);
           tupletDesc[1] = Integer.parseInt(t);
           //System.out.println("TUPLET , second  : " + tupletDesc[1]);
         }
         if(m_tokenType.equals(AbcTokenType.COMA))
         {
           accept(AbcTokenType.COMA, current, follow, true);
           if (m_tokenType.equals(AbcTokenType.DIGIT))
           {
             t = accept(AbcTokenType.DIGIT, current, follow, true);
             tupletDesc[2] = Integer.parseInt(t);
             //System.out.println("TUPLET , third : " + tupletDesc[2]);
           }
         }
       }
       return tupletDesc;
     }
 
     /** note-element ::= note-stem [broken-rhythm] */
     private NoteAbstract parseNoteElement(Set follow)
     {
       Set current = new Set().union(FIRST_BROKEN_RHYTHM);
       //CharStreamPosition beginPosition = m_token.getPosition();
       NoteAbstract note = parseNoteStem(current.createUnion(follow));
       if (note!=null) {
     	  //Apply the previously inherited broken rythm if any.
     	  if (brknRthmDotsCorrection!=0) {
     		  if (brknRthmDotsCorrection>0)
     			  note.setDotted(brknRthmDotsCorrection);
     		  else
     			  if (brknRthmDotsCorrection<0) {
     				  short correctedDuration = (short)(((Note)note).getStrictDuration() / (Math.pow(2,-brknRthmDotsCorrection)));
     				  try {
     					  ((Note)note).setStrictDuration(correctedDuration);
     				  } catch (IllegalArgumentException e) {
     					  ((Note)note).setDuration(correctedDuration);
     				  }
     			  }
     		  //Once the inherited broken rythm has been applied, no broken 
     		  //should be applied further.
     		  brknRthmDotsCorrection = 0;
     	  }
       }
       if (m_tokenType.equals(AbcTokenType.BROKEN_RHYTHM)) {
     	  current.remove(FIRST_BROKEN_RHYTHM);
     	  String brokenRhythmString = accept(AbcTokenType.BROKEN_RHYTHM, current, follow);
     	  byte brokenRhythm = convertBrokenRhythm(brokenRhythmString);
     	  if (note!=null) {
     		  if (brokenRhythm>0) {
     			  note.setDotted(brokenRhythm);
     			  brknRthmDotsCorrection = (byte)-brokenRhythm; 
     		  }
     		  else
     			  if (brokenRhythm<0) {
     				  short correctedDuration = (short)(((Note)note).getStrictDuration() / (Math.pow(2,-brokenRhythm)));
             		  try {
             			  ((Note)note).setStrictDuration(correctedDuration);
             		  } catch (IllegalArgumentException e) {
             			  ((Note)note).setDuration(correctedDuration);
             		  }
             		  brknRthmDotsCorrection = (byte)-brokenRhythm;
         		  }
     	  }
       }
       /*if (note!=null)
       {
         int length = m_scanner.getPosition().getCharactersOffset()-beginPosition.getCharactersOffset();
         try
         {
           ((PositionableNote)note).setBeginPosition(beginPosition);
           ((PositionableNote)note).setLength(length);
         }
         catch(ClassCastException e)
         //this was a multi note instance not a single note.
         {
           ((PositionableMultiNote)note).setBeginPosition(beginPosition);
           ((PositionableMultiNote)note).setLength(length);
         }
       }*/
       return note;
     }
 
     /** note-stem ::= [guitar-chord] [grace-notes] *gracings (note / multi-note) */
     private NoteAbstract parseNoteStem(Set follow)
     {
       Set current = new Set().union(FIRST_GUITAR_CHORD).union(FIRST_GRACE_NOTES).union(FIRST_GRACINGS)
           .union(FIRST_NOTE).union(FIRST_MULTI_NOTE);
       NoteAbstract note = null;
       Note[] graceNotes = null;
       boolean hasGeneralOrnament = false;
       boolean up = false;
       boolean down = false;
       boolean staccato = false;
 
       String chordName = null;
       //======================guitar chord
       current.remove(FIRST_GUITAR_CHORD);
       if(FIRST_GUITAR_CHORD.contains(m_tokenType))
         chordName = parseGuitarChord(current.createUnion(follow));
       //======================grace notes
       current.remove(FIRST_GRACE_NOTES);
       if(FIRST_GRACE_NOTES.contains(m_tokenType))
       {
         graceNotes = parseGraceNotes(current.createUnion(follow));
       }
       //======================gracings
       while (m_tokenType.equals(AbcTokenType.GRACING))
       {
         String acc = accept(AbcTokenType.GRACING, current, follow);
         if (acc!=null)
           if (acc.equals(".")) staccato = true;
           else if (acc.equals("~")) hasGeneralOrnament = true;
           else if (acc.equals("u")) up = true;
           else if (acc.equals("v")) down = true;
       }
       current.remove(FIRST_GRACINGS);
       current.remove(FIRST_NOTE);
       current.remove(FIRST_MULTI_NOTE);
       //======================note or multi note
       if (m_tokenType.equals(AbcTokenType.MULTI_NOTE_BEGIN))
       {
           Vector notes = parseMultiNote(current.createUnion(follow));
           note = new PositionableMultiNote(notes);
       }
       else
       {
         note = parseNote(current.createUnion(follow));
         if (note!=null)
         {
           if (staccato) note.setStaccato(true);
           if (hasGeneralOrnament) note.setGeneralGracing(true);
           if (up) note.setBow(Note.UP);
           if (down) note.setBow(Note.DOWN);
           if (chordName!=null)
             note.setChordName(chordName);
           if (graceNotes!=null)
             note.setGracingNotes(graceNotes);
         }
       }
       return note;
     }
 
     /** multi-note ::= "[" 1*note "]" 
      * @return a Vector containing Note objects */
     private Vector parseMultiNote(Set follow)
     {
       Set current = new Set().union(FIRST_NOTE).union(AbcTokenType.MULTI_NOTE_END);
       Vector notes = new Vector();
       accept(AbcTokenType.MULTI_NOTE_BEGIN, current, follow);
       while (FIRST_NOTE.contains(m_tokenType))
       {
         Note note = parseNote(current.createUnion(follow));
         notes.addElement(note);
       }
       current.remove(FIRST_NOTE);
       current.remove(AbcTokenType.MULTI_NOTE_END);
       accept(AbcTokenType.MULTI_NOTE_END, current, follow);
       return notes;
     }
 
     /** grace-notes ::= "{" 1*pitch "}" */
     private Note[] parseGraceNotes(Set follow)
     {
       Set current = new Set().union(FIRST_PITCH).union(AbcTokenType.GRACING_END);
       accept(AbcTokenType.GRACING_BEGIN, current, follow);
       Vector gracingNotes = new Vector();
       while(FIRST_PITCH.contains(m_tokenType))
       {
         Note note = parsePitch(current.createUnion(follow));
         if (note!=null)
           gracingNotes.addElement(note);
       }
       current.remove(FIRST_PITCH);
       current.remove(AbcTokenType.GRACING_END);
       accept(AbcTokenType.GRACING_END, current, follow);
       if (gracingNotes.isEmpty())
         return null;
       else
       {
         Note[] gracings = new Note[gracingNotes.size()];
         for (int i=0; i<gracingNotes.size();i++)
           gracings[i] = (Note)gracingNotes.elementAt(i);
         //System.arraycopy(gracingNotes.toArray(), 0, gracings,0, gracingNotes.size());
         return gracings;
       }
     }
 
     /** guitar-chord ::= <"> (text <"> / formal-chord */
     private String parseGuitarChord(Set follow)
     {
       Set current = new Set().union(AbcTokenType.GUITAR_CHORD).union(AbcTokenType.CHORD_NAME);
       String chordName = null;
       accept(AbcTokenType.GUITAR_CHORD, current, follow);
       current.remove(AbcTokenType.CHORD_NAME);
       chordName = accept(AbcTokenType.CHORD_NAME, current, follow);
       current.remove(AbcTokenType.GUITAR_CHORD);
       accept(AbcTokenType.GUITAR_CHORD, current, follow);
       return chordName;
     }
 
     /** note ::= note-or-rest [note-length] [tie] */
     private Note parseNote(Set follow)
     {
       Set current = new Set().union(FIRST_NOTE_LENGTH).union(FIRST_TIE);
       PositionableNote note = null;
       CharStreamPosition startPosition = m_token.getPosition();
       note = (PositionableNote)parseNoteOrRest(current.createUnion(follow));
       current.remove(FIRST_NOTE_LENGTH);
       if (FIRST_NOTE_LENGTH.contains(m_tokenType)) {
         Fraction length = parseNoteLength(current.createUnion(follow));
         if (note!=null) {
         	try {
         		//Try to convert the abc duration into standard note duration
         		DurationDescription d = AbcToolkit.getAbsoluteDurationFor(length, m_defaultNoteLength);
         		note.setStrictDuration(d.getStrictDuration());
         		note.setDotted(d.countDots());
         	} catch (IllegalArgumentException e) {
         		//The duration cannot be converted into standard duration
         		//So use the absolute duration instead.
         		note.setDuration((short)(m_defaultNoteLength*length.floatValue()));
         	}
         }
         
       }
       else
         if (note!=null)
         	//The note duration is equal to the default note duration.
         	note.setStrictDuration(m_defaultNoteLength);
       current.remove(FIRST_TIE);
       if (m_tokenType.equals(AbcTokenType.TIE))
       {
         accept(AbcTokenType.TIE, current, follow);
         if (note!=null) note.setIsTied(true);
       }
       if (note!=null)
       {
         CharStreamPosition endPosition = m_scanner.getPosition();
         int length = endPosition.getCharactersOffset() -
             startPosition.getCharactersOffset();
         note.setBeginPosition(startPosition);
         note.setLength(length);
       }
       return note;
     }
 
     /** note-or-rest ::= pitch / rest */
     private Note parseNoteOrRest(Set follow)
     {
       //Set current = new Set();
       if (m_tokenType.equals(AbcTokenType.REST))
       {
         PositionableNote note = new PositionableNote(Note.convertToNoteType(accept(AbcTokenType.REST, null, follow)), AccidentalType.NONE);
         note.setPartOfSlur(m_isPartOfSlur);
         return note;
       }
       else
       //experimentation
         //return parsePitch(current.createUnion(follow));
         return parsePitch(follow);
     }
 
     /** pitch ::= [accidental] basenote [octave] */
     private Note parsePitch(Set follow)
     {
       Set current = new Set().union(FIRST_BASE_NOTE).union(FIRST_OCTAVE);
       Note note = null;
       byte accidental = AccidentalType.NONE;
       byte noteHeigth = 0;
       byte octaveTransposition = 0;
 
       if (m_tokenType.equals(AbcTokenType.ACCIDENTAL))
         accidental = Note.convertToAccidentalType(accept(AbcTokenType.ACCIDENTAL, current, follow));
       current.remove(FIRST_BASE_NOTE);
       String heigth = accept(AbcTokenType.BASE_NOTE, current, follow, true);
       if (heigth!=null) noteHeigth = Note.convertToNoteType(heigth);
       current.remove(FIRST_OCTAVE);
       if (FIRST_OCTAVE.contains(m_tokenType))
       {
         String octave = accept(AbcTokenType.OCTAVE, current, follow);
         if (octave!=null) octaveTransposition = convertToOctaveTransposition(octave);
       }
 
       if (heigth!=null)
         note = new PositionableNote(noteHeigth, accidental, octaveTransposition);
       if (note!=null)
         note.setPartOfSlur(m_isPartOfSlur);
       return note;
     }
 
     /** line-ender ::= comment / linefeed / line-break / no-line-break */
     private ScorePresentationElementInterface parseLineEnder(Set follow) {
     	ScorePresentationElementInterface lineEnder = null;
     	//Set current = new Set();
     	if (FIRST_COMMENT.contains(m_tokenType))
     		parseComment(follow);
     	else
     		if (m_tokenType.equals(AbcTokenType.LINE_BREAK))
     			lineEnder=(accept(AbcTokenType.LINE_BREAK, null, follow)==null)?null:new StaffEndOfLine();
     		else
     			if (m_tokenType.equals(AbcTokenType.NO_LINE_BREAK))
     				accept(AbcTokenType.NO_LINE_BREAK, null, follow);
     			else//if (m_tokenType.equals(AbcTokenType.LINE_FEED))
     				lineEnder=(accept(AbcTokenType.LINE_FEED, null, follow)==null)?null:new StaffEndOfLine();
     				//accept(AbcTokenType.LINE_FEED, null, follow);
     	return lineEnder; 
     }
 
 /*    private void parseTexCommand(Set follow)
     {
       Set current = new Set(AbcTokenType.TEXT).createUnion(AbcTokenType.LINE_FEED);
       accept(AbcTokenType.TEX_COMMAND, current, follow);
       current.remove(AbcTokenType.TEXT);
       accept(AbcTokenType.TEXT, current, follow);
       current.remove(AbcTokenType.LINE_FEED);
       accept(AbcTokenType.LINE_FEED, current, follow);
     }*/
 
 
     //==================================================================================
 
     protected String accept(TokenType tokenType, Set current, Set follow)
     { return accept(tokenType, current, follow, false); }
 
     protected String accept(TokenType tokenType, Set current, Set follow, boolean isCurrentOptional)
     {
       //System.out.println("AbcHeaderParser - Try to accept " + token + " with " + set);
       String value2return =null;
       // should be .equals() in theory but as there's only one instance per token
       // type, we can directly compare instances instead of values.
       if (m_tokenType==tokenType)
       {
         notifyListenersForValidToken(m_token);
         value2return = m_token.getValue();
         //System.out.println("AbcHeaderParser - ACCEPTED: " + token.getValue()+ "(" + ParserTools.convertToTypeName(token.getType())+")");
         if (isCurrentOptional)
         {
           Set union = getSetResultingUnionFrom(current, follow);
           //Set union = current.createUnion(follow);
 /*          int alreadyCreatedSetIndex = m_setsForAccept.indexOf(union);
           if (alreadyCreatedSetIndex!=-1)
           {
             union= (Set)m_setsForAccept.elementAt(alreadyCreatedSetIndex);
             //System.out.println("A set has already been created " + m_recreationNb++ + " times for " + union );
           }
           else
             m_setsForAccept.addElement(union);*/
           m_automata.setDefinition(DefinitionFactory.getDefinition(union.getTypes()));
           m_scanner.setFinaleStateAutomata(m_automata);
           //TokenType[] unionArray = union.getTypes();
           //m_automata.setDefinition(DefinitionFactory.getDefinition(unionArray));
           //m_scanner.setFinaleStateAutomata(m_automata);
           //FinaleStateAutomata fsa = getAutomataFor(unionArray);
           //m_scanner.setFinaleStateAutomata(fsa);
 
           //typesForAutomata = union;
         }
         else
         if (current!=null && current.size()!=0)
         {
           m_automata.setDefinition(DefinitionFactory.getDefinition(current.getTypes()));
           m_scanner.setFinaleStateAutomata(m_automata);
           //TokenType[] array = current.getTypes();
           //m_scanner.setFinaleStateAutomata(getAutomataFor(array));
         }
         else
         {
           m_automata.setDefinition(DefinitionFactory.getDefinition(follow.getTypes()));
           m_scanner.setFinaleStateAutomata(m_automata);
           //TokenType[] array = follow.getTypes();
           //m_scanner.setFinaleStateAutomata(getAutomataFor(array));
         }
         //if (scanner.hasNext())
         try
         {
           m_token = m_scanner.nextToken();
           m_tokenType=m_token.getType();
         }
         catch (NoSuchTokenException e)
         {
           m_token=null;//new Token(null,-1,null);
           m_tokenType=TokenType.UNKNOWN;
         }
       }
       else
       {
         //System.out.println(ParserTools.convertToTypeName(tokenType) + " EXPECTED HERE !!!!!!!! @" + scanner.getLineNumber() + ", " + m_scanner.getColumnNumber());
         if (m_token!=null)
           notifyListenersForInvalidToken(m_token, m_token.getPosition(), tokenType);
         else
           notifyListenersForInvalidToken(null, m_scanner.getPosition(), tokenType);
 
         skipTo(current, follow, isCurrentOptional);
       }
       //System.out.println(m_scanner.getPosition());
       return value2return;
     }
 
     private Set getSetResultingUnionFrom(Set current, Set follow)
     {
       /*Set foundSet = null;
       for (int i=0; i<m_setsForAccept.size() && foundSet==null; i++)
       {
         Set currentSet = (Set)m_setsForAccept.elementAt(i);
         if (currentSet.size()==(set1.size()+set2.size())
             && currentSet.contains(set1) && currentSet.contains(set2)
             )
         foundSet = currentSet;
       }
       if (foundSet==null)
       {
         //System.out.println("creating one set");
         foundSet = set1.createUnion(set2);
         m_setsForAccept.addElement(foundSet);
       }*/
       //else
         //System.out.println("one creation economized : " + m_recreationNb++);
        //old algorythm:
        if (current==null)
        	return follow;
       return current.createUnion(follow);
       //return foundSet;
     }
 
     protected void skipTo(Set current, Set follow, boolean isCurrentOptionnal)
     {
         Set targetSet = null;
         if (!isCurrentOptionnal && current!=null && current.size()!=0)
           targetSet = current;
         else
           targetSet = getSetResultingUnionFrom(current, follow);
         //System.out.println("Parser - skipTo("+ targetSet + ") from " + token.getValue() );
         m_automata.setDefinition(DefinitionFactory.getDefinition(targetSet.getTypes()));
         m_scanner.setFinaleStateAutomata(m_automata);
         //m_scanner.setFinaleStateAutomata(getAutomataFor(targetSet.getTypes()));
         //===old
         /*while (!targetSet.contains(token.getType()))
             token = m_scanner.nextToken();*/
         try
         {
           boolean tokenFound = false;
           while (!tokenFound)
           {
             tokenFound = targetSet.contains(m_tokenType);//||follow.contains(m_tokenType);
             if (!tokenFound)
             {
               m_token = m_scanner.nextToken();
               m_tokenType=m_token.getType();
             }
           }
           /*do
           {
             token = m_scanner.nextToken();
             m_tokenType=token.getType();
           }
           while (!targetSet.contains(m_tokenType));*/
         }
         catch (NoSuchTokenException e)
         {
           m_token=null;
           m_tokenType=TokenType.UNKNOWN;
         }
         //System.out.println("AbcHeaderParser - skipedTo : "+ ParserTools.convertToTypeName(token.getType()));
     }
 
     protected void notifyListenersForTuneBegin() {
       for (int i=0; i<m_listeners.size();i++)
         ((TuneParserListenerInterface)m_listeners.elementAt(i)).tuneBegin();
     }
 
     protected void notifyListenersForTuneEnd(Tune tune) {
       for (int i=0; i<m_listeners.size();i++)
         ((TuneParserListenerInterface)m_listeners.elementAt(i)).tuneEnd(tune);
     }
 
     protected void notifyListenersForValidToken(Token token) {
       TokenEvent evt = new TokenEvent(this, token);
       for (int i=0; i<m_listeners.size();i++)
         ((TuneParserListenerInterface)m_listeners.elementAt(i)).validToken(evt);
     }
 
     protected void notifyListenersForInvalidToken(Token token, CharStreamPosition position, TokenType expectedTokenType)
     {
       InvalidTokenEvent evt = null;
       if (token!=null)
         evt = new InvalidTokenEvent(this, token, expectedTokenType);
       else
         evt = new InvalidTokenEvent(this, position, expectedTokenType);
       for (int i=0; i<m_listeners.size();i++)
         ((TuneParserListenerInterface)m_listeners.elementAt(i)).invalidToken(evt);
     }
 
     protected void notifyListenersForInvalidCharacter(InvalidCharacterEvent evt)
     {
       //System.out.println("Parser - ivalid char " + evt + " for " + typesForAutomata);
       for (int i=0; i<m_listeners.size();i++)
         ((TuneParserListenerInterface)m_listeners.elementAt(i)).invalidCharacter(evt);
     }
 
     protected static byte convertToRepeatBarLine(String barLine)
     {
       if (barLine.equals("[1")) return 1;
       else if (barLine.equals("[2")) return 2;
       else if (barLine.equals("|1")) return 1;
       else if (barLine.equals(":|2")) return 2;
       else return -1;
     }
 
     protected static byte convertBrokenRhythm(String brokenRhythm)
     {
       byte br = (byte)brokenRhythm.length();
       if (brokenRhythm.charAt(0)=='<') return (byte)-br;
       else if (brokenRhythm.charAt(0)=='>') return (byte)br;
       else return 0;
     }
 
     protected static byte convertToOctaveTransposition(String octave)
     {
       if (octave.charAt(0)=='\'')
         return (byte)octave.length();
       else if (octave.charAt(0)==',')
         return (byte)(-octave.length());
         else return 0;
     }
 
 }
 
