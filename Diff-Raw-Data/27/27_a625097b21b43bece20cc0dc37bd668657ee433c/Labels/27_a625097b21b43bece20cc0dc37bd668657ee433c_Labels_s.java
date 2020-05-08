 package snozama.ui;
 
 public final class Labels {
 	
	private static final String[] STANDARD_LETTERS = { "A","B","C","D","E","F","G","H","I","J" };
 	private static final String[] STANDARD_NUMBERS = { "1","2","3","4","5","6","7","8","9","10" };
 	
 	private static final String[] ZERO_NUMBERS = { "0","1","2","3","4","5","6","7","8","9" };
 	
 	/**
 	 * Array [X_Labels, Y_Labels;
 	 */
 	public static final String[][] STANDARD = { STANDARD_LETTERS, STANDARD_NUMBERS };
 	public static final String[][] INVERTED = { STANDARD_NUMBERS, STANDARD_LETTERS };
 	public static final String[][] STANDARD_ZERO = { STANDARD_LETTERS, ZERO_NUMBERS };
 	public static final String[][] INVERTED_ZERO = { ZERO_NUMBERS, STANDARD_LETTERS };
 	public static final String[][] LETTERS = { STANDARD_LETTERS, STANDARD_LETTERS };
 	public static final String[][] NUMBERS = { STANDARD_NUMBERS, STANDARD_NUMBERS };
 	public static final String[][] NUMBERS_ZERO = { ZERO_NUMBERS, ZERO_NUMBERS };
 	
 	
 }
