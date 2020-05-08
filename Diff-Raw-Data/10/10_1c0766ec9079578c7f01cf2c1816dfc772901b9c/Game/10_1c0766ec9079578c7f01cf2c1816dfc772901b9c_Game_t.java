 package ch.unibe.scg.team3.game;
 
 import ch.unibe.scg.team3.board.*;
 import ch.unibe.scg.team3.localDatabase.WordlistHandler;
 import ch.unibe.scg.team3.path.ColoredPath;
 import ch.unibe.scg.team3.path.Path;
 import ch.unibe.scg.team3.token.*;
 import ch.unibe.scg.team3.wordfinder.R;
 
 /**
  * The Game class is responsible for the proper execution of the game. The Game
  * can be observed by the components of the user interface.
  * 
  * @author adrian
  * @author faerber
  */
 
 public class Game extends AbstractGame {
 
 	public static final int DEFAULT_MIN_WORDS_TO_FIND = 30;
 	public static final long TIME_LIMIT = 5 * 60000;
 
 	private final WordlistHandler wordlistHandler;
 
 	private Timer timer;
 	private boolean timeOver;
 	private Board board;
 
 	/**
 	 * Creates a game for a given board
 	 * 
 	 * @param board
 	 *            The board to be played on, not null The size of the board must
 	 *            be greater than zero
 	 * @param wordlistHandler
 	 *            A DataHandler to access the database, not null
 	 * @param wordlistId
 	 *            The Id of the wordlist to be used
 	 */
 	public Game(Board board, WordlistHandler handler, int wordlistId) {
 		super();
 
 		this.board = board;
 		wordlistHandler = handler;
 		this.wordlistId = wordlistId;
 		initTimer(TIME_LIMIT);
 	}
 
 	/**
 	 * Creates a new game with a board generated with words from the given
 	 * wordlist
 	 * 
 	 * @param boardSize
 	 *            The size of the board must be greater than zero
 	 * @param wordlistHandler
 	 *            A DataHandler to access the database, not null
 	 * @param wordlistId
 	 *            The Id of the wordlist to be used
 	 */
 	public Game(int boardSize, WordlistHandler handler, int wordlistId) {
 		this(new Board(boardSize), handler, wordlistId);
 		generateBoard(boardSize);
 	}
 
 	/**
 	 * Creates a game with default board size
 	 * 
 	 * @param wordlistHandler
 	 *            A DataHandler to access the database, not null
 	 * @param wordlistId
 	 *            The Id of the wordlist to be used
 	 */
 	public Game(WordlistHandler data, int wordlistId) {
 		this(Board.DEFAULT_SIZE, data, wordlistId);
 	}
 
 	/**
 	 * Creates a game based on a saved game so it can be raplayed.
 	 * 
 	 * @param wordlistHandler
 	 *            A DataHandler to access the database, not null
 	 */
 	public Game(final SavedGame game, WordlistHandler handler) {
 		super(game);
 		wordlistHandler = handler;
 		id = game.getId();
 		board = game.getBoard();
 		initTimer(TIME_LIMIT);
 	}
 
 	private void generateBoard(int boardSize) {
 
 		AbstractBoardGenerator generator = new FastBoardGenerator(boardSize, wordlistHandler,
 				DEFAULT_MIN_WORDS_TO_FIND);
 
 		BoardGenerationTask task = new BoardGenerationTask(generator) {
 
 			@Override
 			protected void onPostExecute(Board result) {
 				board = result;
 				notifyObservers(new Event(Event.BOARD_CREATED));
 			}
 		};
 
 		task.execute();
 	}
 
 	/**
 	 * The method reads the ch.unibe.scg.team3.path and selects the tokens on the board. It creates
 	 * the word according to the ch.unibe.scg.team3.path and validates this word.
 	 * 
 	 * @param ch.unibe.scg.team3.path
 	 *            The ch.unibe.scg.team3.path to be checked, not null.
 	 */
 	public void submitPath(ColoredPath<? extends IElement> path) {
 		assert path != null;
 		attempts++;
 		WordSelection selection = makeSelection(path);
 
 		String word = selection.toString();
 
 		if (!wordlistHandler.isWordInWordlist(word, wordlistId)) {
 
 			path.setColor(R.drawable.not_valid_button_animation);
 
 		} else if (found.contains(selection)) {
 			path.setColor(R.drawable.already_button_animation);
 
 		} else {
 			found.add(selection);
 			path.setColor(R.drawable.valid_button_animation);
 			updateScore(selection);
 		}
 
 		notifyObservers(new Event(Event.WORD_FOUND));
 
 		if (isOver()) {
 			notifyObservers(new Event(Event.GAME_OVER));
 		}
 	}
 
 	private void updateScore(WordSelection selection) {
 		score += selection.getScore();
 	}
 
 	/**
 	 * Creates a selection based on the paths coordinate points.
 	 * 
 	 * @param ch.unibe.scg.team3.path
 	 *            A ch.unibe.scg.team3.path which is not null
 	 * @return The word selection with, with token from the board according to
 	 *         the ch.unibe.scg.team3.path.
 	 */
 	private WordSelection makeSelection(Path<? extends IElement> path) {
 
 		assert path != null;
 
 		WordSelection selection = new WordSelection();
 
 		for (IElement b : path) {
 			Point point = b.getCoordinates();
 			IToken tok = board.getToken(point);
 			selection.addToken(tok);
 		}
 
 		return selection;
 	}
 
 	private void initTimer(long timeInMillis) {
 		timeOver = false;
 
 		timer = new Timer(timeInMillis) {
 
 			@Override
 			public void onFinish() {
 				notifyObservers(new Event(Event.GAME_OVER));
 				timeOver = true;
 			}
 
 			@Override
 			public void onTick(long millisUntilFinished) {
 				super.onTick(millisUntilFinished);
 				notifyObservers(new Event(Event.TIME_TICK));
 			}
 		};
 	}
 
 	/**
 	 * Starts the countdown timer.
 	 */
 	public void startTime() {
 		timer.start();
 	}
 
 	/**
 	 * Stops the timer and makes the game over.
 	 * 
 	 * @return Returns the remaining time of the game
 	 */
 	public long stopTime() {
 		timer.cancel();
 		timeOver = true;
 		return timer.getRemainingTime();
 	}
 
	public void setTimeOver(boolean timeOver) {
		this.timeOver = timeOver;
	}

 	/**
 	 * Pauses the timer.
 	 * 
 	 * @return Returns the remaining time of the game.
 	 */
 	public long pauseTime() {
 		timer.cancel();
 		initTimer(timer.getRemainingTime());
 		return timer.getRemainingTime();
 	}
 
 	@Override
 	public long getRemainingTime() {
 		return timer.getRemainingTime();
 	}
 	
 	@Override
 	public long getElapsedTime() {
 		return timer.getElapsedTime();
 	}
 
 	/**
 	 * The game is over when enough words were found or the time runs out.
 	 */
 	@Override
 	public boolean isOver() {
 		return found.size() == DEFAULT_MIN_WORDS_TO_FIND || timeOver;
 	}
 
 	@Override
 	public Board getBoard() {
 		return board.clone();
 	}
 
 	@Override
 	public int getBoardSize() {
 		return board.getSize();
 	}
 
 	/**
 	 * Saves all relevant data such as score, board, found words etc. in a
 	 * {@link SavedGame}.
 	 * 
 	 * @return The saved state of the game.
 	 */
 	public SavedGame save() {
 		SavedGame saved = new SavedGame();
 		saved.setId(this.id);
 		saved.setScore(getScore());
 		saved.setStringBoard(board.toString());
 		saved.setRemainingTime(timer.getRemainingTime());
 		saved.setAttempts(getNumberOfAttempts());
 		saved.setWordlistId(getWordlistId());
 		saved.setTimesPlayed(getTimesPlayed() + 1);
 		saved.setFoundWords(found);
 		saved.setNumberOfFoundWords(found.size());
 		saved.setRemainingTime(getRemainingTime());
 
 		return saved;
 	}
 }
