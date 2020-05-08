 package org.jfugue.factories;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jfugue.JFugueException;
 import org.jfugue.Messages;
 import org.jfugue.elements.AbstractNote.NoteTypes;
 import org.jfugue.elements.Chord;
 import org.jfugue.elements.Note;
 import org.jfugue.elements.NoteCollection;
 import org.jfugue.parsers.ParserContext;
 import org.jfugue.parsers.ParserError;
 
 public final class NoteFactory extends JFugueElementFactory<Note> {
 	private static NoteFactory instance;
 
 	private NoteFactory() {
 	}
 
 	public static NoteFactory getInstance() {
 		if (instance == null)
 			instance = new NoteFactory();
 		return instance;
 	}
 
 	public Note createElement(ParserContext context) throws IOException,
 			IllegalArgumentException, JFugueException, ParserError {
 		return new NoteContext(context).match();
 	}
 
 	public Class<Note> type() {
 		return Note.class;
 	}
 
 	public static final String NOTE_RE = Messages
 			.getString("NoteFactory.NOTE_RE"); //$NON-NLS-1$
 	public static final String OCTAVE_RE = Messages
 			.getString("NoteFactory.OCTAVE_RE"); //$NON-NLS-1$
 	public static final String DURATION_RE = Messages
 			.getString("NoteFactory.DURATION_RE"); //$NON-NLS-1$
 	public static final String VELOCITY_RE = Messages
 			.getString("NoteFactory.VELOCITY_RE"); //$NON-NLS-1$
 
 	public static final Pattern NOTE_REGEX = java.util.regex.Pattern.compile(
 			"^" + NOTE_RE, Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
 	public static final Pattern OCTAVE_REGEX = Pattern.compile(
 			"^" + OCTAVE_RE, Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
 	public static final Pattern DURATION_REGEX = Pattern.compile(
 			"^" + DURATION_RE, Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
 	public static final Pattern VELOCITY_REGEX = Pattern.compile(
 			"^" + VELOCITY_RE, Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
 
 	public static final Map<String, byte[]> CHORDS_MAP;
 	public static final String CHORD_RE;
 	public static final Pattern CHORD_REGEX;
 
 	public static final Pattern NOTE_PAT;
 
 	private static Map<String, byte[]> chordMap = new HashMap<String, byte[]>();
 
 	protected static void addChord(String name, int... is) {
 		byte[] bs = new byte[is.length];
 		for (int i = 0; i < bs.length; i++) {
 			bs[i] = (byte) is[i];
 		}
 		chordMap.put(name.toUpperCase(), bs);
 	}
 
 	static {
 		// Build the chord map
 		addChord("MAJ", 4, 7); //$NON-NLS-1$
 		addChord("MIN", 3, 7); //$NON-NLS-1$
 		addChord("AUG", 4, 8); //$NON-NLS-1$
 		addChord("DIM", 3, 6); //$NON-NLS-1$
 
 		addChord("DOM7", 4, 7, 10); //$NON-NLS-1$
 		addChord("MAJ7", 4, 7, 11); //$NON-NLS-1$
 		addChord("MIN7", 3, 7, 10); //$NON-NLS-1$
 		addChord("SUS4", 5, 7); //$NON-NLS-1$
 		addChord("SUS2", 2, 7); //$NON-NLS-1$
 		addChord("MAJ6", 4, 7, 9); //$NON-NLS-1$
 		addChord("MIN6", 3, 7, 9); //$NON-NLS-1$
 		addChord("DOM9", 4, 7, 10, 14); //$NON-NLS-1$
 		addChord("MAJ9", 4, 7, 11, 14); //$NON-NLS-1$
 		addChord("MIN9", 3, 7, 10, 14); //$NON-NLS-1$
 		addChord("DIM7", 3, 6, 9); //$NON-NLS-1$
 		addChord("ADD9", 4, 7, 14); //$NON-NLS-1$
 		addChord("DAVE", 7, 14, 21); //$NON-NLS-1$
 
 		addChord("MIN11", 7, 10, 14, 15, 17); //$NON-NLS-1$
 		addChord("DOM11", 7, 10, 14, 17); //$NON-NLS-1$
 		addChord("DOM13", 7, 10, 14, 16, 21); //$NON-NLS-1$
 		addChord("MIN13", 7, 10, 14, 15, 21); //$NON-NLS-1$
 		addChord("MAJ13", 7, 11, 14, 16, 21); //$NON-NLS-1$
 		addChord("DOM7<5", 4, 6, 10); //$NON-NLS-1$
 		addChord("DOM7>5", 4, 8, 10); //$NON-NLS-1$
 		addChord("MAJ7<5", 4, 6, 11); //$NON-NLS-1$
 		addChord("MAJ7>5", 4, 8, 11); //$NON-NLS-1$
 		addChord("MINMAJ7", 3, 7, 11); //$NON-NLS-1$
 		addChord("DOM7<5<9", 4, 6, 10, 13); //$NON-NLS-1$
 		addChord("DOM7<5>9", 4, 6, 10, 15); //$NON-NLS-1$
 		addChord("DOM7>5<9", 4, 8, 10, 13); //$NON-NLS-1$
 		addChord("DOM7>5>9", 4, 8, 10, 15); //$NON-NLS-1$
 
 		CHORDS_MAP = Collections.unmodifiableMap(chordMap);
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("(?:("); //$NON-NLS-1$
 		for (String chord : CHORDS_MAP.keySet()) {
 			sb.append(chord + "|"); //$NON-NLS-1$
 		}
 		String chords = sb.toString();
 		if (chords.endsWith("|")) //$NON-NLS-1$
 			chords = chords.substring(0, chords.length());
 		sb.append(")(\\^|[A-G])?)"); //$NON-NLS-1$
 		// System.out.println(chords);
 		CHORD_RE = sb.toString();
 		CHORD_REGEX = java.util.regex.Pattern.compile(
 				"^" + CHORD_RE, java.util.regex.Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
 		// TODO velocity
 		NOTE_PAT = Pattern
 				.compile(
 						"^" + NOTE_RE + OCTAVE_RE + CHORD_RE + DURATION_RE, Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
 		// Register the factory
 		JFugueElementFactoryManager.addFactory(getInstance());
 	}
 
	public static class NoteContext {
 		ParserContext pContext;
 		String token, otoken;
 		int start = 0, end;
 		Matcher m;
 
 		boolean isRest = false;
 		boolean isNumericNote = false;
 		boolean isChord = false;
 		boolean isFirstNote = true;
 		boolean isSequentialNote = false;
 		boolean isParallelNote = false;
 		boolean isNatural = false;
 		boolean existAnotherNote = true;
 		boolean anotherNoteIsSequential = false;
 		boolean anotherNoteIsParallel = false;
 		boolean isStartOfTie = false;
 		boolean isEndOfTie = false;
 		NoteTypes type = NoteTypes.FIRST;
 		byte noteNumber = 0;
 		byte octave = 0;
 		String sChord = null;
 		byte[] chord = null;
 		double decimalDuration = 0.0;
 		long duration = 0L;
 		byte attackVelocity = Note.DEFAULT_VELOCITY;
 		byte decayVelocity = Note.DEFAULT_VELOCITY;
 		List<Note> notes = new LinkedList<Note>();
 
 		/**
 		 * @param context
 		 * @throws ParserError
 		 * @throws IOException
 		 */
 		protected NoteContext(ParserContext context) throws IOException,
 				ParserError {
 			pContext = context;
 			otoken = context.readToken();
 			token = otoken.toUpperCase();
 			end = token.length();
 		}
 
 		private void decideSequentialOrParallel(NoteContext context) {
 			// Test whether this note is already known to be sequential (was
 			// connected with _) or parallel (was connected with +)
 			context.isSequentialNote = false;
 			if (context.anotherNoteIsSequential) {
 				context.isSequentialNote = true;
 				context.anotherNoteIsSequential = false;
 			}
 
 			context.isParallelNote = false;
 			if (context.anotherNoteIsParallel) {
 				context.isParallelNote = true;
 				context.anotherNoteIsParallel = false;
 			}
 		}
 
 		protected void matchRoot() throws ParserError {
 			if (prepMatcher(NOTE_REGEX)) {
 				String g1 = m.group(1), g2 = m.group(2);
 				if (g1 != null)
 					parseLetterNote(g1);
 				else if (m.group(2) != null)
 					parseNumericNote(g2);
 				else
 					throw new ParserError();
 			}
 			setStart();
 		}
 
 		protected void parseLetterNote(String note) throws ParserError {
 			switch (note.charAt(0)) {
 			case 'C':
 				noteNumber = 0;
 				break;
 			case 'D':
 				noteNumber = 2;
 				break;
 			case 'E':
 				noteNumber = 4;
 				break;
 			case 'F':
 				noteNumber = 5;
 				break;
 			case 'G':
 				noteNumber = 7;
 				break;
 			case 'A':
 				noteNumber = 9;
 				break;
 			case 'B':
 				noteNumber = 11;
 				break;
 			case 'R':
 				isRest = true;
 				break;
 			default:
 				throw new ParserError();
 			}
 			for (int i = 1; i < note.length(); i++) {
 				switch (note.charAt(i)) {
 				case '#':
 					noteNumber++;
 					break;
 				case 'B':
 					noteNumber--;
 					break;
 				case 'N':
 					isNatural = true;
 					break;
 				default:
 					break;
 				}
 			}
 		}
 
 		protected void parseNumericNote(String note) {
 			noteNumber = Byte.parseByte(note);
 		}
 
 		protected void matchOctave() {
 			if (prepMatcher(OCTAVE_REGEX)) {
 				octave = Byte.parseByte(m.group());
 			}
 			setStart();
 		}
 
 		protected void matchChord() throws ParserError {
 			if (prepMatcher(CHORD_REGEX)) {
 				isChord = true;
 				sChord = m.group(1);
 				byte[] bs = CHORDS_MAP.get(sChord);
 				sChord = sChord.toLowerCase();
 				chord = Arrays.copyOf(bs, bs.length);
 				computeNoteValue();
 				parseInversion(m.group(2));
 			}
 			setStart();
 		}
 
 		protected void computeNoteValue() throws ParserError {
 			// Don't compute note value for a rest
 			if (isRest) {
 				return;
 			}
 
 			// If we happen not to have an octave yet, set it to a default
 			// value.
 			// Default octave: 5 for notes, 3 for chords
 			if ((octave == 0) && (!isNumericNote)) {
 				if (isChord) {
 					octave = 3;
 				} else {
 					octave = 5;
 				}
 			}
 
 			byte keySig = pContext.getKeySig().getKeySig();
 			// Adjust for Key Signature
 			if ((keySig != 0) && (!isNatural)) {
 				if ((keySig <= -1) && (noteNumber == 11))
 					noteNumber = 10;
 				if ((keySig <= -2) && (noteNumber == 4))
 					noteNumber = 3;
 				if ((keySig <= -3) && (noteNumber == 9))
 					noteNumber = 8;
 				if ((keySig <= -4) && (noteNumber == 2))
 					noteNumber = 1;
 				if ((keySig <= -5) && (noteNumber == 7))
 					noteNumber = 6;
 				if ((keySig <= -6) && (noteNumber == 0)) {
 					noteNumber = 11;
 					octave--;
 				}
 				if ((keySig <= -7) && (noteNumber == 5))
 					noteNumber = 4;
 				if ((keySig >= +1) && (noteNumber == 5))
 					noteNumber = 6;
 				if ((keySig >= +2) && (noteNumber == 0))
 					noteNumber = 1;
 				if ((keySig >= +3) && (noteNumber == 7))
 					noteNumber = 8;
 				if ((keySig >= +4) && (noteNumber == 2))
 					noteNumber = 3;
 				if ((keySig >= +5) && (noteNumber == 9))
 					noteNumber = 10;
 				if ((keySig >= +6) && (noteNumber == 4))
 					noteNumber = 5;
 				if ((keySig >= +7) && (noteNumber == 11)) {
 					noteNumber = 0;
 					octave++;
 				}
 			}
 
 			// Compute the actual note number, based on octave and note
 			if (!isNumericNote) {
 				int intNoteNumber = (octave * 12) + noteNumber;
 				if (intNoteNumber > 127) {
 					throw new ParserError(ParserError.NOTE_OCTAVE_EXC,
 							Integer.toString(intNoteNumber));
 				}
 				noteNumber = (byte) intNoteNumber;
 			}
 		}
 
 		protected void parseInversion(String inv) throws ParserError {
 			if (!isChord)
 				return;
 
 			int index = 0;
 			int slen = inv.length();
 			int inversionCount = 0;
 			int inversionRootNote = -1;
 			int inversionOctave = -1;
 
 			boolean checkForInversion = true;
 			while (checkForInversion) {
 				if (index < slen) {
 					switch (inv.charAt(index)) {
 					case '^':
 						index++;
 						inversionCount++;
 						break;
 					case 'C':
 						index++;
 						inversionRootNote = 0;
 						break;
 					case 'D':
 						index++;
 						inversionRootNote = 2;
 						break;
 					case 'E':
 						index++;
 						inversionRootNote = 4;
 						break;
 					case 'F':
 						index++;
 						inversionRootNote = 5;
 						break;
 					case 'G':
 						index++;
 						inversionRootNote = 7;
 						break;
 					case 'A':
 						index++;
 						inversionRootNote = 9;
 						break;
 					// For 'B', need to differentiate between B note and 'b'
 					// flat
 					case 'B':
 						index++;
 						if (inversionRootNote == -1) {
 							inversionRootNote = 11;
 						} else {
 							inversionRootNote--;
 						}
 						break;
 					case '#':
 						index++;
 						inversionRootNote++;
 						break;
 					// For '0', need to differentiate between initial 0 and 0 as
 					// a
 					// second digit (i.e., 10)
 					case '0':
 						index++;
 						if (inversionOctave == -1) {
 							inversionOctave = 0;
 						} else {
 							inversionOctave = inversionOctave * 10;
 						}
 						break;
 					case '1':
 						index++;
 						inversionOctave = 1;
 						break;
 					case '2':
 						index++;
 						inversionOctave = 2;
 						break;
 					case '3':
 						index++;
 						inversionOctave = 3;
 						break;
 					case '4':
 						index++;
 						inversionOctave = 4;
 						break;
 					case '5':
 						index++;
 						inversionOctave = 5;
 						break;
 					case '6':
 						index++;
 						inversionOctave = 6;
 						break;
 					case '7':
 						index++;
 						inversionOctave = 7;
 						break;
 					case '8':
 						index++;
 						inversionOctave = 8;
 						break;
 					case '9':
 						index++;
 						inversionOctave = 9;
 						break;
 					// If [, whoo boy, we're checking for a note number
 					case '[':
 						int indexEndBracket = inv.indexOf(']', index);
 						inversionRootNote = Integer.parseInt(inv.substring(
 								index + 1, indexEndBracket - 1));
 						index = indexEndBracket + 1;
 						break;
 					default:
 						checkForInversion = false;
 						break;
 					}
 				} else {
 					checkForInversion = false;
 				}
 			}
 
 			// Modify the note values based on the inversion
 			if (inversionCount > 0) {
 				if (inversionRootNote == -1) {
 					// The root is determined by a number of carets. Increase
 					// each half-step
 					// before the inversion by 12, the number of notes in an
 					// octave.
 					noteNumber += 12;
 					for (int i = inversionCount - 1; i < chord.length; i++) {
 						chord[i] -= 12;
 					}
 				} else {
 					// The root is determined by an inversionRoot. This is much
 					// trickier, but we can
 					// still figure it out.
 					if (inversionOctave != -1) {
 						inversionRootNote += inversionOctave * 12;
 					} else if (inversionRootNote < 12) {
 						int currentOctave = noteNumber / 12;
 						inversionRootNote += currentOctave * 12;
 					}
 					// Otherwise, inversionRootNote is a numeric note value,
 					// like [60]
 
 					if ((inversionRootNote > noteNumber
 							+ chord[chord.length - 1])
 							|| (inversionRootNote < noteNumber)) {
 						throw new ParserError(ParserError.INVERSION_EXC);
 					}
 
 					noteNumber += 12;
 					for (int i = 0; i < chord.length; i++) {
 						if (noteNumber + chord[i] >= inversionRootNote + 12) {
 							chord[i] -= 12;
 						}
 					}
 				}
 			}
 
 		}
 
 		protected void matchNoteDuration() {
 			if (prepMatcher(DURATION_REGEX)) {
 				String s = m.group();
 				switch (s.charAt(0)) {
 				case '/':
 					parseNumericDuration(s);
 					break;
 				case 'W':
 				case 'H':
 				case 'Q':
 				case 'I':
 				case 'S':
 				case 'T':
 				case 'X':
 				case 'O':
 				case '-':
 					parseLetterDuration(s);
 					break;
 				default:
 					break;
 				}
 				parseTuplet(s);
 			} else {
 				// Default duration is a quarter note
 				decimalDuration = 1.0 / 4.0;
 			}
 
 			// javax.sound.midi.Sequence resolution is 120
 			// 12/22/08 - As identified by E. Gingras, resolution should be
 			// 128, not 120, for better compatibility with 128th notes (and
 			// other durations)
 			duration = (long) (ParserContext.SEQUENCE_RES * decimalDuration);
 		}
 
 		private void parseTuplet(String s) {
 			// TODO make this less messy
 			int index = 0, slen = s.length();
 			if (index < slen) {
 				if (s.charAt(index) == '*') {
 					index++;
 
 					// Figure out tuplet ratio, or figure out when to stop
 					// looking for tuplet info
 					boolean stopTupletParsing = false;
 					int indexOfUnitsToMatch = 0;
 					int indexOfNumNotes = 0;
 					int counter = -1;
 					while (!stopTupletParsing) {
 						counter++;
 						if (slen > index + counter) {
 							if (s.charAt(index + counter) == ':') {
 								indexOfNumNotes = index + counter + 1;
 							} else if ((s.charAt(index + counter) >= '0')
 									&& (s.charAt(index + counter) <= '9')) {
 								if (indexOfUnitsToMatch == 0) {
 									indexOfUnitsToMatch = index + counter;
 								}
 							} else if ((s.charAt(index + counter) == '*')) {
 								// no op... artifact of parsing
 							} else {
 								stopTupletParsing = true;
 							}
 						} else {
 							stopTupletParsing = true;
 						}
 					}
 
 					index += counter;
 
 					double numerator = 2.0;
 					double denominator = 3.0;
 					if ((indexOfUnitsToMatch > 0) && (indexOfNumNotes > 0)) {
 						numerator = Double.parseDouble(s.substring(
 								indexOfUnitsToMatch, indexOfNumNotes - 1));
 						denominator = Double.parseDouble(s.substring(
 								indexOfNumNotes, index));
 					}
 					double tupletRatio = numerator / denominator;
 					decimalDuration = decimalDuration * tupletRatio;
 				}
 			}
 		}
 
 		private void parseLetterDuration(String s) {
 			// TODO make this less messy
 			// Check duration
 			boolean durationExists = true;
 			boolean isDotted = false;
 			int index = 0, slen = s.length();
 
 			while (durationExists == true) {
 				int durationNumber = 0;
 				// See if the note has a duration
 				// Duration is optional; default is Q (4)
 				if (index < slen) {
 					char durationChar = s.charAt(index);
 					switch (durationChar) {
 					case '-':
 						if ((decimalDuration == 0) && (!isEndOfTie)) {
 							isEndOfTie = true;
 						} else {
 							isStartOfTie = true;
 						}
 						break;
 					case 'W':
 						durationNumber = 1;
 						break;
 					case 'H':
 						durationNumber = 2;
 						break;
 					case 'Q':
 						durationNumber = 4;
 						break;
 					case 'I':
 						durationNumber = 8;
 						break;
 					case 'S':
 						durationNumber = 16;
 						break;
 					case 'T':
 						durationNumber = 32;
 						break;
 					case 'X':
 						durationNumber = 64;
 						break;
 					case 'O':
 						durationNumber = 128;
 						break;
 					default:
 						index--;
 						durationExists = false;
 						break;
 					}
 					index++;
 					if ((index < slen) && (s.charAt(index) == '.')) {
 						isDotted = true;
 						index++;
 					}
 
 					if (durationNumber > 0) {
 						double d = 1.0 / durationNumber;
 						if (isDotted) {
 							decimalDuration += d + (d / 2.0);
 						} else {
 							decimalDuration += d;
 						}
 					}
 				} else {
 					durationExists = false;
 				}
 			}
 		}
 
 		private void parseNumericDuration(String s) {
 			// TODO make this less messy
 			int index = 0;
 			// The duration has come in as a number, like 0.25 for a quarter
 			// note.
 			// Advance pointer past the initial slash (/)
 			index++;
 
 			// Decimal duration is not required to be enclosed by brackets,
 			// but since most of the other numerical input to a MusicString
 			// is required to be in brackets, we should support it.
 			if ('[' == s.charAt(index)) {
 				int indexOfEndingBracket = s.indexOf(']', index);
 				decimalDuration += pContext.getEnvironment()
 						.getDoubleFromDictionary(
 								s.substring(index + 1, indexOfEndingBracket));
 				index = indexOfEndingBracket + 1;
 			} else {
 				int endingIndex = index;
 				boolean keepAdvancingPointer = true;
 				while (keepAdvancingPointer) {
 					try {
 						char numericDurationChar = s.charAt(endingIndex);
 						if ((numericDurationChar >= '0')
 								&& (numericDurationChar <= '9')
 								|| (numericDurationChar == '.')) // Decimal dot,
 																	// not
 																	// dotted
 																	// duration
 						{
 							endingIndex++;
 						} else {
 							keepAdvancingPointer = false;
 						}
 					} catch (IndexOutOfBoundsException e) {
 						keepAdvancingPointer = false;
 					}
 				}
 				String durationNumberString = s.substring(index, endingIndex);
 				decimalDuration += Double.parseDouble(durationNumberString);
 				index = endingIndex;
 			}
 		}
 
 		protected void matchNoteVelocity() throws ParserError {
 			// TODO make less messy
 			if (prepMatcher(VELOCITY_REGEX)) {
 				// Don't compute note velocity for a rest
 				if (isRest)
 					return;
 
 				String s = m.group();
 				int index = 0, slen = s.length();
 				// Process velocity attributes, if they exist
 				while (index < slen) {
 					int startPoint = index + 1;
 					int endPoint = startPoint;
 
 					char velocityChar = s.charAt(index);
 					int lengthOfByte = 0;
 					if ((velocityChar == '+') || (velocityChar == '_'))
 						break;
 					boolean byteDone = false;
 					while (!byteDone && (index + lengthOfByte + 1 < slen)) {
 						char possibleByteChar = s.charAt(index + lengthOfByte
 								+ 1);
 						if ((possibleByteChar >= '0')
 								&& (possibleByteChar <= '9')) {
 							lengthOfByte++;
 						} else {
 							byteDone = true;
 						}
 					}
 					endPoint = index + lengthOfByte + 1;
 
 					// Or maybe a bracketed string was passed in, instead of a
 					// byte
 					if ((index + 1 < slen) && (s.charAt(index + 1) == '[')) {
 						endPoint = s.indexOf(']', startPoint) + 1;
 					}
 
 					byte velocityNumber = pContext.getEnvironment()
 							.getByteFromDictionary(
 									s.substring(startPoint, endPoint));
 
 					switch (velocityChar) {
 					case 'A':
 					case '<':
 						attackVelocity = velocityNumber;
 						break;
 					case 'D':
 					case '>':
 						decayVelocity = velocityNumber;
 						break;
 					default:
 						throw new ParserError(ParserError.NOTE_VELOCITY_EXC,
 								s.substring(startPoint, endPoint));
 					}
 					index = endPoint;
 				}
 			}
 			setStart();
 		}
 
 		/**
 		 * Returns the String of the next sub-token (the parts after + or _), if
 		 * one exists; otherwise, returns null
 		 */
 		private String parseNoteConnector(String s) {
 			int index = 0, slen = s.length();
 			existAnotherNote = false;
 			// See if there's another note to process
 			if ((index < slen)
 					&& ((s.charAt(index) == '+') || (s.charAt(index) == '_'))) {
 				if (s.charAt(index) == '_') {
 					anotherNoteIsSequential = true;
 				} else {
 					anotherNoteIsParallel = true;
 				}
 				index++;
 				existAnotherNote = true;
 				if (isFirstNote) {
 					type = NoteTypes.FIRST;
 				} else if (isSequentialNote) {
 					type = NoteTypes.SEQUENTIAL;
 				} else if (isParallelNote) {
 					type = NoteTypes.PARALLEL;
 				}
 				return s.substring(index, slen);
 			}
 			return null;
 		}
 
 		protected boolean prepMatcher(Pattern p) {
 			if (m == null)
 				m = p.matcher(token);
 			else
 				m.usePattern(p);
 			m.region(start, end);
 			return m.matches();
 		}
 
 		protected boolean setStart() {
 			start = m.end();
 			// m.region(start, end);
 			return true;
 		}
 
 		public Note match() throws ParserError {
 			while (existAnotherNote) {
 				decideSequentialOrParallel(this);
 				isRest = false;
 				matchRoot();
 				matchOctave();
 				matchChord();
 				computeNoteValue();
 				matchNoteDuration();
 				matchNoteVelocity();
 				token = parseNoteConnector(token.substring(start));
 				if (isChord)
 					notes.add(new Chord(this));
 				else
 					notes.add(new Note(this));
 			}
 			for (Note note : notes) {
 				switch (note.getType()) {
 				case FIRST:
 					pContext.fireNoteEvent(note);
 					break;
 				case SEQUENTIAL:
 					pContext.fireSequentialNoteEvent(note);
 					break;
 				case PARALLEL:
 					pContext.fireParallelNoteEvent(note);
 					break;
 				default:
 					break;
 				}
 			}
 			if (notes.size() == 1)
 				return notes.get(0);
 			else
 				return new NoteCollection(this, notes);
 		}
 
 		/**
 		 * @return the token
 		 */
 		public String getToken() {
 			return token;
 		}
 
 		/**
 		 * @return the otoken
 		 */
 		public String getOtoken() {
 			return otoken;
 		}
 
 		/**
 		 * @return the start
 		 */
 		public int getStart() {
 			return start;
 		}
 
 		/**
 		 * @return the end
 		 */
 		public int getEnd() {
 			return end;
 		}
 
 		/**
 		 * @return the noteNumber
 		 */
 		public byte getNoteNumber() {
 			return noteNumber;
 		}
 
 		/**
 		 * @return the isNatural
 		 */
 		public boolean isNatural() {
 			return isNatural;
 		}
 
 		/**
 		 * @return the isNumericNote
 		 */
 		public boolean isNumericNote() {
 			return isNumericNote;
 		}
 
 		/**
 		 * @return the isRest
 		 */
 		public boolean isRest() {
 			return isRest;
 		}
 
 		/**
 		 * @return the octave
 		 */
 		public byte getOctave() {
 			return octave;
 		}
 
 		/**
 		 * @return the sChord
 		 */
 		public String getsChord() {
 			return sChord;
 		}
 
 		/**
 		 * @return the chord
 		 */
 		public byte[] getChord() {
 			return Arrays.copyOf(chord, chord.length);
 		}
 
 		/**
 		 * @return the isChord
 		 */
 		public boolean isChord() {
 			return isChord;
 		}
 
 		/**
 		 * @return the decimalDuration
 		 */
 		public double getDecimalDuration() {
 			return decimalDuration;
 		}
 
 		/**
 		 * @return the duration
 		 */
 		public long getDuration() {
 			return duration;
 		}
 
 		/**
 		 * @return the isEndOfTie
 		 */
 		public boolean isEndOfTie() {
 			return isEndOfTie;
 		}
 
 		/**
 		 * @return the isStartOfTie
 		 */
 		public boolean isStartOfTie() {
 			return isStartOfTie;
 		}
 
 		/**
 		 * @return the attackVelocity
 		 */
 		public byte getAttackVelocity() {
 			return attackVelocity;
 		}
 
 		/**
 		 * @return the decayVelocity
 		 */
 		public byte getDecayVelocity() {
 			return decayVelocity;
 		}
 
 		/**
 		 * @return the existAnotherNote
 		 */
 		public boolean isExistAnotherNote() {
 			return existAnotherNote;
 		}
 
 		/**
 		 * @return the anotherNoteIsSequential
 		 */
 		public boolean isAnotherNoteIsSequential() {
 			return anotherNoteIsSequential;
 		}
 
 		/**
 		 * @return the anotherNoteIsParallel
 		 */
 		public boolean isAnotherNoteIsParallel() {
 			return anotherNoteIsParallel;
 		}
 
 		/**
 		 * @return the isFirstNote
 		 */
 		public boolean isFirstNote() {
 			return isFirstNote;
 		}
 
 		/**
 		 * @return the isSequentialNote
 		 */
 		public boolean isSequentialNote() {
 			return isSequentialNote;
 		}
 
 		/**
 		 * @return the isParallelNote
 		 */
 		public boolean isParallelNote() {
 			return isParallelNote;
 		}
 
 		/**
 		 * @return the type
 		 */
 		public NoteTypes getType() {
 			return type;
 		}
 	}
 }
