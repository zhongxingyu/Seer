 package edu.berkeley.gamesman.verification;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.lang.reflect.InvocationTargetException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Iterator;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.Value;
 import edu.berkeley.gamesman.database.Database;
 import edu.berkeley.gamesman.database.DatabaseHandle;
 import edu.berkeley.gamesman.game.TierGame;
 import edu.berkeley.gamesman.game.util.TierState;
 
 /**
  * 
  * Verifies the game tree based on a given database and outputs the erroneous
  * values to a log file.
  * 
  * @author adegtiar
  * @author rchengyue
  */
 public abstract class GameVerifier implements Iterator<GameState> {
 
 	protected Class<? extends GameState> stateClass;
 	private RandomAccessFile outFile;
 	private long incorrectStatesCount;
 	protected GameState currentGameState;
 	protected Database db;
 	protected Configuration conf;
 	protected TierGame mGame;
 	protected DatabaseHandle dbHandle;
 	protected ProgressBar progressBar;
 
 	/**
 	 * The number of <tt>GameState</tt> to verify.
 	 */
 	protected final int totalStateCount;
 	protected final int totalTimeCount;
 	protected int stateCount;
 	protected int runCount;
 	protected long initialTime;
 	protected long previousTime;
 	protected ProgressBarType progressBarType;
 
 	int nSkipped;
 	private static final NumberFormat percentFormat = new DecimalFormat("#0.0%");
 
 	protected GameVerifier(Class<? extends GameState> stateClass,
 			String database, String outputFileName, int totalStateCount,
 			int totalTimeCount) {
 		this.stateClass = stateClass;
 		try {
 			this.db = Database.openDatabase(database);
 		} catch (IOException e1) {
 			throw new RuntimeException(e1);
 		} catch (ClassNotFoundException e1) {
 			throw new RuntimeException(e1);
 		}
 		this.dbHandle = db.getHandle(true);
 		this.conf = db.conf;
 
 		if (outputFileName == null)
 			outputFileName = database.substring(0, database.lastIndexOf('.'))
 					+ "_out.txt";
 
 		File outputFile = new File(outputFileName);
 		outputFile.delete();
 		try {
 			this.outFile = new RandomAccessFile(outputFile, "rw");
 		} catch (FileNotFoundException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Cannot find file: " + outFile.toString());
 			System.exit(1);
 		}
 
 		// this.incorrectStates = new HashSet<GameState>();
 		this.mGame = (TierGame) conf.getGame();
 		this.totalStateCount = totalStateCount;
 		this.totalTimeCount = totalTimeCount;
 		this.initialTime = System.currentTimeMillis() / 1000;
 		if (this.totalTimeCount != -1) {
 			progressBarType = ProgressBarType.TIME;
 		} else if (this.totalStateCount != -1) {
 			progressBarType = ProgressBarType.STATE;
 		}
 
 		if (progressBarType == ProgressBarType.TIME) {
 			this.progressBar = new ProgressBar(totalTimeCount);
 		} else if (progressBarType == ProgressBarType.STATE) {
 			this.progressBar = new ProgressBar(totalStateCount);
 		}
 
 		// Write header to outFile to save room for update
 		writeIncorrectStatesSummaryToFile();
 	}
 
 	public GameState getInitialGameState() {
 		try {
 			return stateClass.getConstructor(Configuration.class).newInstance(
 					conf);
 		} catch (IllegalArgumentException e) {
 			throw new Error(e);
 		} catch (SecurityException e) {
 			throw new Error(e);
 		} catch (InstantiationException e) {
 			throw new Error(e);
 		} catch (IllegalAccessException e) {
 			throw new Error(e);
 		} catch (InvocationTargetException e) {
 			throw new Error(e.getCause());
 		} catch (NoSuchMethodException e) {
 			throw new Error(e);
 		}
 	}
 
 	/**
 	 * Checks the value for a <tt>GameState</tt> and ensures that it is correct.
 	 * 
 	 * @return whether or not the state is correct (i.e. win has a lose, lose
 	 *         has all wins, tie has no lose).
 	 * @throws IOException
 	 *             if cannot query the state from the database.
 	 */
 	protected boolean verifyGameState() throws IOException {
 		Record dbRecord = getRecordForState(currentGameState.getBoardString());
 		Value dbValue = dbRecord.value;
 		Value calculatedValue;
 		stateCount++;
 		if (currentGameState.isPrimitive()) {
 			calculatedValue = currentGameState.getValue();
 			if (dbRecord.remoteness != 0)
 				return false;
 		} else
 			calculatedValue = calculateValueOfCurrentState();
 
 		if (dbValue != calculatedValue) {
 			// incorrectStates.add(currentGameState);
 			incorrectStatesCount++;
 		}
 
 		return dbValue == calculatedValue;
 	}
 
 	/**
 	 * Calculates the value of a non-primitive state. Does this by checking the
 	 * values of the children in the db.
 	 * 
 	 * @return the value of the current state.
 	 */
 	private Value calculateValueOfCurrentState() {
 		assert (!currentGameState.isPrimitive());
 
 		Iterator<String> childrenStringIterator = currentGameState
 				.generateChildren();
 
 		Value bestValueSoFar = Value.LOSE;
 
 		while (childrenStringIterator.hasNext()) {
 			String childString = childrenStringIterator.next();
 
 			Value childValue = getRecordForState(childString).value;
 
 			// WIN => all losing
 			// LOSE => at least one winning
 			// TIE => all tie or losing
 
 			switch (childValue) {
 			case TIE:
 				/*
 				 * System.out.println("TIE CHILD!");
 				 * System.out.println(childString);
 				 */
 				bestValueSoFar = Value.TIE;
 				break;
 			case LOSE:
 				/*
 				 * System.out.println("LOSE CHILD!");
 				 * System.out.println(childString);
 				 */
 				return Value.WIN;
 				/*
 				 * case WIN: System.out.println("WIN CHILD!");
 				 * System.out.println(childString);
 				 */
 			}
 		}
 
 		return bestValueSoFar;
 	}
 
 	/**
 	 * Gets the Record corresponding of a state String.
 	 * 
 	 * @param stateString
 	 *            the String representation of a state.
 	 * @return the record of the state represented by the String.
 	 */
 	public Record getRecordForState(String stateString) {
 		TierState tierState = mGame.stringToState(stateString);
 
 		// index into the db
 		long hashedState = mGame.stateToHash(tierState);
 		long recordValue = 0;
 		try {
 			recordValue = db.readRecord(dbHandle, hashedState);
 		} catch (IOException e) {
 			System.err.println("Cannot read from db: " + stateString);
 			System.exit(1);
 		}
 
 		// long representation of a record (remoteness, value)
 		Record stateRecord = mGame.newRecord();
 
 		// interprets the long as a record
 		mGame.longToRecord(tierState, recordValue, stateRecord);
 
 		return stateRecord;
 	}
 
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException(
 				"GameVerifier does not support remove");
 	}
 
 	public GameState getCurrentState() {
 		return currentGameState;
 	}
 
 	public Record getCurrentRecord() {
 		return getRecordForState(currentGameState.getBoardString());
 	}
 
 	public String getCurrentValue() {
 		return getCurrentRecord().value.toString();
 	}
 
 	/*
 	 * PRINTING AND WRITING METHODS
 	 */
 
 	/**
 	 * Prints the status bar.
 	 */
 	public void printStatusBar() {
 		if (progressBarType == ProgressBarType.TIME) {
 			long currentTime = System.currentTimeMillis() / 1000 - initialTime;
 			progressBar.updateNumElements((int) currentTime);
 			if (currentTime - previousTime > 1 || currentTime >= totalTimeCount) {
 				previousTime = currentTime;
 				progressBar.printStatus();
				printSkipCount();
 			}
 		} else if (progressBarType == ProgressBarType.STATE) {
 			progressBar.updateNumElements(stateCount);
 			if (stateCount % 300 == 0 || stateCount == totalStateCount) {
 				progressBar.printStatus();
				printSkipCount();
 			}
 		} else {
 			// No argument for state or time specified.
 			if (stateCount % 500 == 0) {
 				System.out.print('\r');
 				System.out.print("States checked: " + stateCount
 						+ "\tSeconds passed: "
 						+ (System.currentTimeMillis() / 1000 - initialTime));
 			}
 		}
 	}
	
	private void printSkipCount() {
		if (Connect4CmdLineParser.debugging)
			System.out.print(". Skipped: "
					+ percentFormat.format((double) nSkipped
							/ (stateCount + 1)) + " of states.");
	}
 
 	/**
 	 * Prints the incorrect states summary (width, height, and number of
 	 * incorrect states) to standard out.
 	 */
 	public void printIncorrectStateSummary() {
 		System.out.println();
 		System.out.println("Width: "
 				+ conf.getInteger("gamesman.game.width", 7) + " Height: "
 				+ conf.getInteger("gamesman.game.height", 6));
 		System.out.println("Incorrect States: " + incorrectStatesCount);
 	}
 
 	/**
 	 * Writes the value of the current incorrect GameState and the String
 	 * representation of the current incorrect GameState to the output file.
 	 */
 	public void writeIncorrectStateToFile() {
 		// Write current GameState to outFile
 		try {
 			outFile.write(("Incorrect Value: "
 					+ getRecordForState(currentGameState.getBoardString()).value
 							.toString() + " Current Game State: "
 					+ currentGameState.toString() + '\n').getBytes());
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Cannot write to file: " + outFile.toString());
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * Write the incorrect states summary (width, height, and number of
 	 * incorrect states) to the output file.
 	 */
 	public void writeIncorrectStatesSummaryToFile() {
 		try {
 			outFile.seek(0);
 			outFile.write(("Width: "
 					+ +conf.getInteger("gamesman.game.width", 7) + " Height: "
 					+ conf.getInteger("gamesman.game.height", 6) + "\n")
 					.getBytes());
 			outFile.write(("Incorrect States: " + incorrectStatesCount + '\n')
 					.getBytes());
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Cannot write to file: " + outFile.toString());
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * Closes the output file.
 	 */
 	public void closeOutputFile() {
 		try {
 			outFile.close();
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Cannot close file: " + outFile.toString());
 			System.exit(1);
 		}
 	}
 
 	public boolean hasNext() {
 		if (progressBarType == ProgressBarType.STATE
 				&& this.stateCount > this.totalStateCount
 				|| progressBarType == ProgressBarType.TIME
 				&& System.currentTimeMillis() / 1000 - initialTime >= totalTimeCount)
 			return false;
 		return true;
 	}
 
 }
