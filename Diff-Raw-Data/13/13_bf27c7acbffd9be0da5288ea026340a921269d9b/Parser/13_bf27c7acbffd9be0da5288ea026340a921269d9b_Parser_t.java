 package gsingh.learnkirtan.parser;
 
 import static gsingh.learnkirtan.Constants.MAX_KEYS;
 import gsingh.learnkirtan.Main;
 import gsingh.learnkirtan.note.DoubleNote;
 import gsingh.learnkirtan.note.Note;
 import gsingh.learnkirtan.parser.exceptions.DashFirstNoteException;
 import gsingh.learnkirtan.parser.exceptions.InvalidNoteException;
 import gsingh.learnkirtan.parser.exceptions.NoStartLabelException;
 import gsingh.learnkirtan.parser.exceptions.NoteOutOfBoundsException;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Parser {
 
 	private static final Logger LOGGER = Logger.getLogger(Parser.class
 			.getName());
 
 	static {
 		LOGGER.addHandler(Main.logFile);
 		LOGGER.setLevel(Level.INFO);
 	}
 
 	/**
 	 * The default length each note is played
 	 */
 	private static final int gap = 500;
 
 	/**
 	 * If set, loop will terminate.
 	 */
 	private static boolean stop = false;
 
 	/**
 	 * If set, playback will pause
 	 */
 	private static boolean pause = false;
 
 	/**
 	 * True if playing, false otherwise.
 	 */
 	private static boolean isPlaying = false;
 
 	/**
 	 * If set, end of file or playback region has been reached. If
 	 * {@code repeat} is set, playback will repeat. Otherwise, this function
 	 * will return
 	 */
 	private static boolean finished = false;
 
 	/**
 	 * If set, playback region will repeat when finished
 	 */
 	private static boolean repeat = false;
 
 	/**
 	 * The note to play
 	 */
 	private static DoubleNote doubleNote;
 
 	private static Scanner scanner;
 
 	public static void parseAndPlay(String shabad, String start, String end,
 			double tempo, int saKey) {
 
 		isPlaying = true;
 
 		shabad = shabad.toUpperCase();
 		start = "#" + start.toUpperCase();
 		end = "#" + end.toUpperCase();
 		LOGGER.info("Shabad: " + shabad);
 		LOGGER.info("Start: " + start);
 		LOGGER.info("End: " + end);
 
 		List<String> wordList = Arrays.asList(shabad.split("\\s+"));
 		scanner = new Scanner(wordList, end);
 
 		// Find starting label if present
 		try {
 			moveCursorToStart(start);
 		} catch (NoStartLabelException e) {
			;
 		} catch (DashFirstNoteException e) {
 			stop = true;
 		}
 
 		while (!stop) {
 			if (pause)
 				pause();
 
 			// Read in note
 			getNextNote();
 
 			// Play notes if valid
 			try {
 				playNote(doubleNote.getNote1(), tempo, saKey);
 				playNote(doubleNote.getNote2(), tempo, saKey);
 			} catch (NoteOutOfBoundsException e) {
 				break;
 			} catch (InvalidNoteException e) {
 				break;
 			}
 
 			// Check for non-immediate end
 			if (finished) {
 				if (finish(start)) {
 					finished = false;
					// stop = false;
 					continue;
 				} else
 					break;
 			}
 
 		}
 
 		LOGGER.info("Left Loop. Returning.");
 		pause = false;
 		isPlaying = false;
 		stop = false;
 		finished = false;
 	}
 
 	private static void moveCursorToStart(String start)
 			throws NoStartLabelException, DashFirstNoteException {
 		String input = null;
 		if (!start.equals("#")) {
 			input = scanner.getNext();
 			while (!input.equals(start)) {
 				if (!scanner.isFinished())
 					input = scanner.getNext();
 				else
 					throw new NoStartLabelException(LOGGER);
 			}
 		} else
 			LOGGER.info("No starting label supplied.");
 
 		// Check if first note is a dash
 		input = scanner.getNext();
 		scanner.decrementIndex();
 
 		if (scanner.isFinished()) {
 			stop = true;
 			return;
 		}
 
 		if (input.equals("-"))
 			throw new DashFirstNoteException(LOGGER);
 	}
 
 	private static void getNextNote() {
 		String input = scanner.getNextNote();
 
 		// Check for end
 		if (scanner.isFinished()) {
 			finished = true;
 			return;
 		}
 
 		// Construct a note
 		doubleNote = new DoubleNote(input);
 
 		// Check if next note is a dash
 		input = scanner.getNextNote();
 
 		if (scanner.isFinished()) {
 			finished = true;
 			return;
 		}
 
 		int holdCount = 1;
 		while (input.equals("-")) {
 			holdCount++;
 			input = scanner.getNextNote();
 
 			if (scanner.isFinished()) {
 				finished = true;
 				break;
 			}
 		}
 
 		scanner.decrementIndex();
 		if (doubleNote.getNote2() == null)
 			doubleNote.getNote1().setHoldCount(holdCount);
 		else
 			doubleNote.getNote2().setHoldCount(holdCount);
 
 		LOGGER.info("holdCount: " + holdCount);
 	}
 
 	private static void playNote(Note note, double tempo, int saKey)
 			throws NoteOutOfBoundsException, InvalidNoteException {
 		if (note != null) {
 			LOGGER.info("Note is null.");
 			if (note.isValid()) {
 				play(note, tempo, saKey);
 			} else {
 				String fullNote = note.getPrefix() + note.getNote()
 						+ note.getSuffix();
 				throw new InvalidNoteException(LOGGER, fullNote);
 			}
 		}
 	}
 
 	public static void play(Note note, double tempo, int saKey)
 			throws NoteOutOfBoundsException {
 		// Calculate key for note
 		int key = calculateKey(note, saKey);
 
 		// Play note
 		double doubleMult;
 		if (doubleNote.getNote2() != null)
 			doubleMult = .5;
 		else
 			doubleMult = 1.0;
 		LOGGER.info("Playing Key: " + key);
 		Main.keys[key]
 				.playOnce((int) (doubleMult * note.getHoldCount() * gap / tempo));
 	}
 
 	public static int calculateKey(Note note, int saKey)
 			throws NoteOutOfBoundsException {
 		int key = -1;
 
 		String noteName = note.getNote();
 		// Set the key number of the note to be played
 		if (noteName.equals("SA")) {
 			key = saKey;
 		} else if (noteName.equals("RE")) {
 			key = saKey + 2;
 		} else if (noteName.equals("GA")) {
 			key = saKey + 4;
 		} else if (noteName.equals("MA")) {
 			key = saKey + 5;
 		} else if (noteName.equals("PA")) {
 			key = saKey + 7;
 		} else if (noteName.equals("DHA")) {
 			key = saKey + 9;
 		} else if (noteName.equals("NI")) {
 			key = saKey + 11;
 		}
 
 		if (note.isKomal())
 			key--;
 		if (note.isLower())
 			key -= 12;
 		if (note.isTheevra())
 			key++;
 		if (note.isUpper())
 			key += 12;
 
 		if (key >= 0 && key < MAX_KEYS)
 			return key;
 		else {
 			String fullNote = note.getPrefix() + noteName + note.getSuffix();
 			throw new NoteOutOfBoundsException(LOGGER, fullNote);
 		}
 	}
 
 	private static boolean finish(String start) {
 		if (repeat) {
 			LOGGER.info("Finished. Repeating.");
 			scanner.reset();
 			try {
 				moveCursorToStart(start);
 			} catch (NoStartLabelException e) {
 				return false;
 			} catch (DashFirstNoteException e) {
 				return false;
 			}
 
 			return true;
 		} else {
 			LOGGER.info("Finished. Returning.");
 			return false;
 		}
 	}
 
 	/**
 	 * Sets pause to false and stop to true
 	 */
 	public static void stop() {
 		stop = true;
 		pause = false;
 		isPlaying = false;
 	}
 
 	/**
 	 * Sets pause to true
 	 */
 	public static void setPause() {
 		pause = true;
 		isPlaying = false;
 	}
 
 	/**
 	 * If pause is true, the thread playing the shabad will sleep
 	 */
 	public static void pause() {
 		while (pause) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Returns true if the playback is paused, false otherwise
 	 */
 	public static boolean isPaused() {
 		return pause;
 	}
 
 	/**
 	 * Returns true if playing, false otherwise
 	 */
 	public static boolean isPlaying() {
 		return isPlaying;
 	}
 
 	/**
 	 * Sets isPlaying to true and pause to false.
 	 */
 	public static void play() {
 		pause = false;
 		isPlaying = true;
 	}
 
 	/**
 	 * Sets the repeat flag
 	 * 
 	 * @param bool
 	 *            - {@code repeat} is set to this value
 	 */
 	public static void setRepeat(boolean bool) {
 		repeat = bool;
 	}
 }
