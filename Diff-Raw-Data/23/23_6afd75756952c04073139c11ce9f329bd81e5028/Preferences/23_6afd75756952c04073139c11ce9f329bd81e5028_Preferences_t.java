 package com.strategy.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.strategy.util.GameParser.GameParserException;
 
 /**
  * Stores the program's preferences that can be set through command line
  * parameters.
  * 
  * @author Ralph Dürig
  */
 public class Preferences {
 
 	private static Preferences instance;
 	private static boolean defaultGenerateFiles = false;
 	private static int defaultBoardSize = 4;
 	private static List<Turn> defaultTurns = null;
 	// defaults to interpreter mode
 	private static boolean defaultModeInterpreter = true;
	private static PrintStream defaultOut = null;
 
 	private boolean generateFiles;
 	private final int boardSize;
 	private final List<Turn> turns;
 	private boolean modeInterpreter;
 	private PrintStream out;
 
 	public static Preferences createInstance(String[] args) {
 		if (null == args || 0 == args.length) {
 			instance = getDefault();
 			return instance;
 		}
 		ArrayList<String> params = Lists.newArrayList(args);
 		boolean parGenerateFiles = parseGenerateFiles(params);
 		boolean parModeInterpreter = parseMode(params);
 		int parBoardSize = parseBoardSize(params);
 		List<Turn> parTurns = defaultTurns;
 		try {
 			GameParser parser = getParser(params);
 			if (null != parser) {
 				parBoardSize = parser.getBoardSize();
 				parTurns = parser.getTurns();
 			} else {
 				parser = getParserWithStrings(params);
 				if (null != parser) {
 					parBoardSize = parser.getBoardSize();
 					parTurns = parser.getTurns();
 				}
 			}
 		} catch (FileNotFoundException e) {
 		} catch (GameParserException e) {
 		}
 		instance = new Preferences(
 				null == parTurns || parTurns.isEmpty() ? parGenerateFiles
 						: false, parBoardSize, parTurns, parModeInterpreter);
 		return instance;
 
 	}
 
 	public static Preferences getInstance() {
 		if (null == instance) {
 			instance = getDefault();
 		}
 		return instance;
 	}
 
 	private Preferences(boolean generateFiles, int boardSize, List<Turn> turns,
 			boolean modeInterpreter) {
 		this.generateFiles = generateFiles;
 		this.boardSize = boardSize;
 		this.turns = turns;
 		this.modeInterpreter = modeInterpreter;
 		this.out = defaultOut;
 	}
 
 	/**
 	 * @return the generateFiles
 	 */
 	public boolean isGenerateFiles() {
 		return generateFiles;
 	}
 
 	/**
 	 * @param generateFiles
 	 *            the generateFiles to set
 	 */
 	public void setGenerateFiles(boolean generateFiles) {
 		this.generateFiles = generateFiles;
 	}
 
 	public boolean isModeInterpreter() {
 		return modeInterpreter;
 	}
 
 	/**
 	 * @return the boardSize
 	 */
 	public int getBoardSize() {
 		return boardSize;
 	}
 
 	/**
 	 * @return the turns
 	 */
 	public List<Turn> getTurns() {
 		return turns;
 	}
 
 	/**
 	 * @return the out
 	 */
 	public PrintStream getOut() {
 		return out;
 	}
 
 	/**
 	 * @param out
 	 *            the out to set
 	 */
 	public void setOut(PrintStream out) {
 		this.out = out;
 	}
 
 	// ************************************************************************
 
 	private static Preferences getDefault() {
 		return new Preferences(defaultGenerateFiles, defaultBoardSize,
 				defaultTurns, defaultModeInterpreter);
 	}
 
 	private static boolean parseGenerateFiles(List<String> params) {
 		Optional<String> opt = Iterables.tryFind(params,
 				new ParameterGenerateFilesPredicate());
 		if (opt.isPresent()) {
 			return true;
 		} else {
 			return defaultGenerateFiles;
 		}
 	}
 
 	private static boolean parseMode(List<String> params) {
 		Optional<String> opt = Iterables.tryFind(params,
 				new ParameterModePredicate());
 		if (opt.isPresent()) {
 			return false;
 		} else {
 			return defaultModeInterpreter;
 		}
 	}
 
 	private static int parseBoardSize(List<String> params) {
 		int parBoardSizeIndex = Iterables.indexOf(params,
 				new ParameterBoardSizePredicate());
 		if (parBoardSizeIndex < 0 || parBoardSizeIndex >= params.size() - 1) {
 			return defaultBoardSize;
 		}
 		String value = Iterables.<String> get(params, parBoardSizeIndex + 1);
 		return Integer.parseInt(value);
 	}
 
 	private static GameParser getParser(List<String> params)
 			throws FileNotFoundException, GameParserException {
 		int parTurnsIndex = Iterables.indexOf(params,
 				new ParameterTurnsPredicate());
 		if (parTurnsIndex < 0 || parTurnsIndex >= params.size() - 1) {
 			return null;
 		}
 		String value = Iterables.<String> get(params, parTurnsIndex + 1);
 		File hgfFile = new File(value);
 		if (null == hgfFile || !hgfFile.exists()) {
 			return null;
 		}
 
 		InputStream in = new FileInputStream(hgfFile);
 
 		GameParser parser = new GameParser(in);
 
 		return parser;
 	}
 
 	private static GameParser getParserWithStrings(List<String> params)
 			throws GameParserException {
 		int parTurnsIndex = Iterables.indexOf(params,
 				new ParameterTurnsStringPredicate());
 		if (parTurnsIndex < 0 || parTurnsIndex >= params.size() - 1) {
 			return null;
 		}
 		String value = Iterables.<String> get(params, parTurnsIndex + 1);
 		if (null == value || value.trim().isEmpty()) {
 			return null;
 		}
 		InputStream in = new ByteArrayInputStream(value.getBytes());
 
 		GameParser parser = new GameParser(in);
 
 		return parser;
 	}
 
 	// ************************************************************************
 
 	private static class ParameterGenerateFilesPredicate implements
 			Predicate<String> {
 
 		private static final String PAR_GENERATE_FILES = "-f";
 
 		@Override
 		public boolean apply(String input) {
 			return input.equals(PAR_GENERATE_FILES);
 		}
 
 	}
 
 	private static class ParameterModePredicate implements Predicate<String> {
 
 		private static final String PAR_MODE = "-gtp";
 
 		@Override
 		public boolean apply(String input) {
 			return input.equals(PAR_MODE);
 		}
 
 	}
 
 	private static class ParameterBoardSizePredicate implements
 			Predicate<String> {
 
 		private static final String PAR_BOARD_SIZE = "-s";
 
 		@Override
 		public boolean apply(String input) {
 			return input.equals(PAR_BOARD_SIZE);
 		}
 
 	}
 
 	private static class ParameterTurnsPredicate implements Predicate<String> {
 
 		private static final String PAR_TURNS = "-hgf";
 
 		@Override
 		public boolean apply(String input) {
 			return input.equals(PAR_TURNS);
 		}
 
 	}
 
 	private static class ParameterTurnsStringPredicate implements
 			Predicate<String> {
 
 		private static final String PAR_TURNS_STRING = "-m";
 
 		@Override
 		public boolean apply(String input) {
 			return input.equals(PAR_TURNS_STRING);
 		}
 
 	}
 }
