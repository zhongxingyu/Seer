 package bu.edu.cs673.edukid.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * This class defines the database schema and contract.
  * 
  * @author Kevin Graue
  * 
  */
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	private static final String EDUKID_DATABASE = "EDUkid.db";
	private static final int EDUKID_DATABASE_VERSION = 50;
 
 	private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
 
 	private static final String EQUALS = " = ";
 	private static final String AND = " AND ";
 
 	protected static final String TABLE_USER_ACCOUNT = "useraccount";
 	protected static final String COLUMN_USER_ID = "uid";
 	protected static final String COLUMN_USER_NAME = "username";
 	protected static final String COLUMN_USER_IMAGE = "userimage";
 	protected static final String COLUMN_USER_SOUND = "usersound";
 
 	protected static final String TABLE_LETTERS = "letter";
 	protected static final String COLUMN_LETTERS_ID = "letterid";
 	protected static final String COLUMN_LETTERS_WORD = "letterword";
 	protected static final String COLUMN_LETTERS_SOUND = "lettersound";
 
 	protected static final String TABLE_WORDS = "words";
 	protected static final String COLUMN_WORDS_ITEM_ID = "wordsitemid";
 	protected static final String COLUMN_WORDS_WORD_ID = "wordswordid";
 	protected static final String COLUMN_WORDS_WORD = "wordsword";
 	protected static final String COLUMN_WORDS_SOUND = "wordssound";
 	protected static final String COLUMN_WORDS_IMAGE = "wordsimage";
 	protected static final String COLUMN_WORDS_IMAGE_ID = "wordsimageid";
 	protected static final String COLUMN_WORDS_CHECKED = "wordschecked";
 
 	protected static final String TABLE_DEFAULT_WORD_MAP = "defaultwordmap";
 	protected static final String COLUMN_DEFAULT_WORD_MAP_CATEGORY_ID = "defaultwordmapcategoryid";
 	protected static final String COLUMN_DEFAULT_WORD_MAP_ITEM_ID = "defaultwordmapitemid";
 	protected static final String COLUMN_DEFAULT_WORD_MAP_WORD_ID = "defaultwordmapwordid";
 	protected static final String COLUMN_DEFAULT_WORD_MAP_CHECKED = "defaultwordmapchecked";
 
 	protected static final String TABLE_NUMBER = "number";
 	protected static final String COLUMN_NUMBER_ID = "numberid";
 	protected static final String COLUMN_NUMBER_WORD = "num";
 	protected static final String COLUMN_NUMBER_SOUND = "numbersound";
 
 	protected static final String TABLE_NUM_TYPE = "ntype";
 	protected static final String COLUMN_TYPE_ID = "ntypeid";
 	protected static final String COLUMN_TYPE_NAME = "ntypename";
 
 	protected static final String TABLE_NUMBERS = "numbers";
 	protected static final String COLUMN_NID = "nid";
 	protected static final String COLUMN_NTID = "ntid";
 	protected static final String COLUMN_NUMBERS = "numbers";
 	protected static final String COLUMN_NUMBERS_SOUND = "numberssound";
 	protected static final String COLUMN_NUMBERS_IMAGE = "numberimage";
 
 	protected static final String TABLE_SHAPE = "shape";
 	protected static final String COLUMN_SHAPE_ID = "shapeid";
 	protected static final String COLUMN_SHAPE_WORD = "shapeword";
 	protected static final String COLUMN_SHAPE_IMAGE = "shapeimage";
 	protected static final String COLUMN_SHAPE_SOUND = "shapesound";
 
 	protected static final String TABLE_COLOUR = "colour";
 	protected static final String COLUMN_COLOUR_ID = "colourid";
 	protected static final String COLUMN_COLOUR_WORD = "colourword";
 	protected static final String COLUMN_COLOUR_IMAGE = "colourimage";
 	protected static final String COLUMN_COLOUR_SOUND = "coloursound";
 
 	protected static final String TABLE_TIMER = "timer";
 	protected static final String COLUMN_TIMER_EXPIRED = "expired";
 	protected static final String COLUMN_TIMER_ENABLED = "enabled";
 	protected static final String COLUMN_TIMER_LEFT = "timeleft";
 	protected static final String COLUMN_LEARN_TIME = "learntime";
 
 	private static final String CREATE_USER_ACCOUNT_TABLE = "create table "
 			+ TABLE_USER_ACCOUNT + "(" + COLUMN_USER_ID
 			+ " integer primary key autoincrement, " + COLUMN_USER_NAME
 			+ " text not null, " + COLUMN_USER_IMAGE + " text not null, "
 			+ COLUMN_USER_SOUND + " text not null);";
 
 	private static final String CREATE_LETTERS_TABLE = "create table "
 			+ TABLE_LETTERS + "(" + COLUMN_LETTERS_ID
 			+ " integer primary key autoincrement, " + COLUMN_LETTERS_WORD
 			+ " text, " + COLUMN_LETTERS_SOUND + " text );";
 
 	private static final String CREATE_ALPHABET_TABLE = "create table "
 			+ TABLE_WORDS + "(" + COLUMN_WORDS_ITEM_ID + " integer, "
 			+ COLUMN_WORDS_WORD_ID + " integer , " + COLUMN_WORDS_WORD
 			+ " text, " + COLUMN_WORDS_SOUND + " text, " + COLUMN_WORDS_IMAGE
 			+ " text, " + COLUMN_WORDS_IMAGE_ID + " long, "
 			+ COLUMN_WORDS_CHECKED + " text );";
 
 	private static final String CREATE_DEFAUT_WORD_MAP_TABLE = "create table "
 			+ TABLE_DEFAULT_WORD_MAP + "("
 			+ COLUMN_DEFAULT_WORD_MAP_CATEGORY_ID + " integer, "
 			+ COLUMN_DEFAULT_WORD_MAP_ITEM_ID + " integer, "
 			+ COLUMN_DEFAULT_WORD_MAP_WORD_ID + " integer, "
 			+ COLUMN_DEFAULT_WORD_MAP_CHECKED + " integer );";
 
 	private static final String CREATE_SHAPE_TABLE = "create table "
 			+ TABLE_SHAPE + "(" + COLUMN_SHAPE_ID
 			+ " integer primary key autoincrement, " + COLUMN_SHAPE_WORD
 			+ " text, " + COLUMN_SHAPE_IMAGE + " text, " + COLUMN_SHAPE_SOUND
 			+ " text );";
 
 	private static final String CREATE_COLOUR_TABLE = "create table "
 			+ TABLE_COLOUR + "(" + COLUMN_COLOUR_ID
 			+ " integer primary key autoincrement, " + COLUMN_COLOUR_WORD
 			+ " text, " + COLUMN_COLOUR_IMAGE + " text, " + COLUMN_COLOUR_SOUND
 			+ " text );";
 
 	private static final String CREATE_NUMBER_TABLE = "create table "
 			+ TABLE_NUMBER + "(" + COLUMN_NUMBER_ID
 			+ " integer primary key autoincrement, " + COLUMN_NUMBER_WORD
 			+ " text, " + COLUMN_NUMBER_SOUND + " text );";
 
 	private static final String CREATE_NUMTYPE_TABLE = "create table "
 			+ TABLE_NUM_TYPE + "(" + COLUMN_TYPE_ID
 			+ " integer primary key autoincrement, " + COLUMN_TYPE_NAME
 			+ " text not null);";
 
 	private static final String CREATE_NUMBERS_TABLE = "create table "
 			+ TABLE_NUMBERS + "(" + COLUMN_NID + " integer, " + COLUMN_NTID
 			+ " integer, " + COLUMN_NUMBERS + " text, " + COLUMN_NUMBERS_SOUND
 			+ " text, " + COLUMN_NUMBERS_IMAGE + " text );";
 
 	private static final String CREATE_TIMER_TABLE = " create table "
 			+ TABLE_TIMER + " ( " + COLUMN_TIMER_ENABLED + " integer,"
 			+ COLUMN_TIMER_EXPIRED + " integer," + COLUMN_TIMER_LEFT
 			+ " integer," + COLUMN_LEARN_TIME + " integer);";
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param context
 	 *            the context.
 	 */
 	public DatabaseHelper(Context context) {
 		super(context, EDUKID_DATABASE, null, EDUKID_DATABASE_VERSION);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_USER_ACCOUNT_TABLE);
 		db.execSQL(CREATE_LETTERS_TABLE);
 		db.execSQL(CREATE_ALPHABET_TABLE);
 		db.execSQL(CREATE_DEFAUT_WORD_MAP_TABLE);
 		db.execSQL(CREATE_NUMBER_TABLE);
 		db.execSQL(CREATE_NUMTYPE_TABLE);
 		db.execSQL(CREATE_NUMBERS_TABLE);
 		db.execSQL(CREATE_SHAPE_TABLE);
 		db.execSQL(CREATE_COLOUR_TABLE);
 		db.execSQL(CREATE_TIMER_TABLE);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL(DROP_TABLE + TABLE_USER_ACCOUNT);
 		db.execSQL(DROP_TABLE + TABLE_LETTERS);
 		db.execSQL(DROP_TABLE + TABLE_WORDS);
 		db.execSQL(DROP_TABLE + TABLE_DEFAULT_WORD_MAP);
 		db.execSQL(DROP_TABLE + TABLE_NUMBER);
 		db.execSQL(DROP_TABLE + TABLE_NUM_TYPE);
 		db.execSQL(DROP_TABLE + TABLE_NUMBERS);
 		db.execSQL(DROP_TABLE + TABLE_SHAPE);
 		db.execSQL(DROP_TABLE + TABLE_COLOUR);
 		db.execSQL(DROP_TABLE + TABLE_TIMER);
 
 		onCreate(db);
 	}
 
 	// TODO
 	public static String generateWordsSelection(int itemIndex) {
 		return COLUMN_WORDS_ITEM_ID + EQUALS + itemIndex;
 	}
 
 	// TODO
 	public static String generateWordsSelection(int itemIndex, int wordIndex) {
 		return generateWordsSelection(itemIndex) + AND + COLUMN_WORDS_WORD_ID
 				+ EQUALS + wordIndex;
 	}
 
 	// TODO
 	public static String generateDefaultMappingSelection(int categoryIndex,
 			int itemIndex) {
 		String categoryIdSelection = COLUMN_DEFAULT_WORD_MAP_CATEGORY_ID
 				+ EQUALS + categoryIndex;
 		String itemIdSelection = COLUMN_DEFAULT_WORD_MAP_ITEM_ID + EQUALS
 				+ itemIndex;
 
 		return categoryIdSelection + AND + itemIdSelection;
 	}
 
 	// TODO
 	public static String generateDefaultMappingSelection(int categoryIndex,
 			int itemIndex, int wordIndex) {
 		String wordIdSelection = COLUMN_DEFAULT_WORD_MAP_WORD_ID + EQUALS
 				+ wordIndex;
 
 		return generateDefaultMappingSelection(categoryIndex, itemIndex) + AND
 				+ wordIdSelection;
 	}
 }
