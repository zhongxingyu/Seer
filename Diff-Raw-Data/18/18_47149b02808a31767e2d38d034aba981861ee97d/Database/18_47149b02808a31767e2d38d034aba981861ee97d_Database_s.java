 package bu.edu.cs673.edukid.db;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.annotation.SuppressLint;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.drawable.Drawable;
 import bu.edu.cs673.edukid.db.model.Category;
 import bu.edu.cs673.edukid.db.model.CategoryType;
 import bu.edu.cs673.edukid.db.model.UserAccount;
 import bu.edu.cs673.edukid.db.model.Letters;
 import bu.edu.cs673.edukid.db.model.Alphabets;
 import bu.edu.cs673.edukid.db.model.Themes;
 
 public class Database {
 
 	private static Database DATABASE_INSTANCE = null;
 
 	private SQLiteDatabase sqlDatabase;
 
 	private DatabaseHelper databaseHelper;
 
 	private String[] categoriesColumns = { DatabaseHelper.COLUMN_CATEGORY_ID,
 			DatabaseHelper.COLUMN_CATEGORY_NAME,
 			DatabaseHelper.COLUMN_CATEGORY_IMAGE };
 
 	private String[] userAccountColumns = { DatabaseHelper.COLUMN_USER_ID,
 			DatabaseHelper.COLUMN_USER_NAME, DatabaseHelper.COLUMN_USER_IMAGE,
 			DatabaseHelper.COLUMN_USER_SOUND };
 	
 	private String[] letterscolumns={DatabaseHelper.COLUMN_LETTERS_ID,
 			DatabaseHelper.COLUMN_LETTERS_WORD, DatabaseHelper.COLUMN_LETTERS_SOUND};
 	
 	private String[] themecolumns={DatabaseHelper.COLUMN_THEME_ID,
 			DatabaseHelper.COLUMN_THEME_NAME};
 	
 	private String[] alphabetscolumns={DatabaseHelper.COLUMN_LID,
 			DatabaseHelper.COLUMN_TID,DatabaseHelper.COLUMN_WORDS,DatabaseHelper.COLUMN_WORDS_SOUND,
 			DatabaseHelper.COLUMN_WORDS_IMAGE};
 
 	/**
 	 * Gets the database singleton instance.
 	 * 
 	 * Note: only call this method when you know the database has already been
 	 * instantiated and the application context is not available.
 	 * 
 	 * @return the database singleton instance.
 	 * @throws NullPointerException
 	 */
 	public static Database getInstance() throws NullPointerException {
 		if (DATABASE_INSTANCE == null) {
 			throw new NullPointerException(
 					"Cannot access database without instantiation!");
 		}
 
 		return DATABASE_INSTANCE;
 	}
 
 	/**
 	 * Gets the database singleton instance.
 	 * 
 	 * Note: this is the preferred way to access the singleton instance but you
 	 * need a {@link Context} to use it.
 	 * 
 	 * @param context
 	 *            the application context.
 	 * @return the database singleton instance.
 	 */
 	public static Database getInstance(Context context) {
 		if (DATABASE_INSTANCE == null) {
 			DATABASE_INSTANCE = new Database(context);
 		}
 
 		return DATABASE_INSTANCE;
 	}
 
 	private Database(Context context) {
 		databaseHelper = new DatabaseHelper(context);
 		sqlDatabase = databaseHelper.getWritableDatabase();
 	}
 
 	public List<Category> getAllCategories() {
 		List<Category> categories = new ArrayList<Category>();
 
 		Cursor cursor = sqlDatabase.query(DatabaseHelper.TABLE_CATEGORIES,
 				categoriesColumns, null, null, null, null, null);
 		cursor.moveToFirst();
 
 		while (!cursor.isAfterLast()) {
 			categories.add(DatabaseUtils.convertCursorToCategory(cursor));
 			cursor.moveToNext();
 		}
 
 		cursor.close();
 
 		return categories;
 	}
 
 	public void addCategory(String name, Drawable image) {
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(DatabaseHelper.COLUMN_CATEGORY_NAME, name);
 		contentValues.put(DatabaseHelper.COLUMN_CATEGORY_IMAGE,
 				ImageUtils.drawableToByteArray(image));
 		sqlDatabase
 				.insert(DatabaseHelper.TABLE_CATEGORIES, null, contentValues);
 	}
 
 	public String getItem(CategoryType categoryType, int itemIndex) {
 		switch (categoryType) {
 		case ALPHABET:
 			Letters let= getLetters().get(itemIndex);
 			return let.getLetter();
 		case NUMBERS:
 		case SHAPES:
 		case COLORS:
 		case CUSTOM:
 			// TODO: should be a database query because we can add to these
 			// categories or create new categories (custom)
 			return "";
 		default:
 			return "";
 		}
 	}
 
 	public int getItemCount(CategoryType categoryType) {
 		switch (categoryType) {
 		case ALPHABET:
 			return DatabaseDefaults.getAlphabet().length;
 		case NUMBERS:
 		case SHAPES:
 		case COLORS:
 		case CUSTOM:
 			// TODO: should be a database query because we can add to these
 			// categories or create new categories (custom)
 			return 0;
 		default:
 			// Should never get here.
 			return 0;
 		}
 	}
 
 	private List<String> getItemWords(CategoryType categoryType, int itemIndex) {
 		switch (categoryType) {
 		case ALPHABET:
 			// TODO: check the database first, then use the defaults if we get
 			// nothing back from the query (custom words from user).
 			return DatabaseDefaults.getDefaultAlphabetWords(itemIndex);
 		case NUMBERS:
 		case SHAPES:
 		case COLORS:
 		case CUSTOM:
 			// TODO: database query then default
 			return Collections.emptyList();
 		default:
 			// Should never get here.
 			return Collections.emptyList();
 		}
 	}
 
 	public String getItemWord(CategoryType categoryType, int itemIndex,
 			int wordIndex) {
 		return getItemWords(categoryType, itemIndex).get(wordIndex);
 	}
 
 	public int getItemWordCount(CategoryType categoryType, int itemIndex) {
 		return getItemWords(categoryType, itemIndex).size();
 	}
 
 	public int getItemImage(CategoryType categoryType, int itemIndex,
 			int imageIndex) {
 		switch (categoryType) {
 		case ALPHABET:
 			// TODO: check the database first, then use the defaults if we get
 			// nothing back from the query (voice recording from user).
 			return DatabaseDefaults.getDefaultAlphabetDrawableIds(itemIndex)
 					.get(imageIndex);
 		case NUMBERS:
 		case SHAPES:
 		case COLORS:
 		case CUSTOM:
 			// TODO: database query then default
 			return -1;
 		default:
 			// Should never get here.
 			return -1;
 		}
 	}
 
 	public String getPhoneticSound(CategoryType categoryType, int itemIndex) {
 		switch (categoryType) {
 		case ALPHABET:
 			// TODO: check the database first, then use the defaults if we get
 			// nothing back from the query (voice recording from user).
 			//return DatabaseDefaults.getDefaultAlphabetPhoneticSounds(itemIndex);
 			Letters let= getLetters().get(itemIndex);
 			return let.getLettersound();
 		case NUMBERS:
 		case SHAPES:
 		case COLORS:
 			// TODO: database query then default
 			return "";
 		default:
 			// Should never get here.
 			return "";
 		}
 	}
 
 	@SuppressLint("UseSparseArrays")
 	public Map<Long, UserAccount> getUserAccounts() {
 		Map<Long, UserAccount> userAccounts= new HashMap <Long, UserAccount>();
 		Cursor cursor = sqlDatabase.query(DatabaseHelper.TABLE_USER_ACCOUNT,
 				userAccountColumns, null, null, null, null, null);
 		cursor.moveToFirst();
 
 		while (!cursor.isAfterLast()) {
 			UserAccount userAccount = DatabaseUtils.convertCursorToUserAccount(cursor);
 			userAccounts.put(userAccount.getId(), userAccount);
 			cursor.moveToNext();
 		}
 		
 		cursor.close();
 
 		return userAccounts;
 	}
 	public UserAccount getUserAccount(long id){
 		return getUserAccounts().get(id);
 	}
 	public void addUserAccount(String userName, Drawable userImage) {
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(DatabaseHelper.COLUMN_USER_NAME, userName);
 		contentValues.put(DatabaseHelper.COLUMN_USER_IMAGE,
 				ImageUtils.drawableToByteArray(userImage));
 		contentValues.put(DatabaseHelper.COLUMN_USER_SOUND, userName);
 		sqlDatabase.insert(DatabaseHelper.TABLE_USER_ACCOUNT, null,
 				contentValues);
 	}
<<<<<<< 8679ec80b0440afb04104d68f6e8f7505c5ab433
 	public void editUserAccount(UserAccount account){
 		getUserAccount(account.getId());
 		sqlDatabase.delete(DatabaseHelper.TABLE_USER_ACCOUNT, DatabaseHelper.COLUMN_USER_ID+" = ?", new String[] { String.valueOf(account.getId()) });
 		addUserAccount(account.getUserName(), ImageUtils.byteArrayToDrawable(account.getUserImage()));
 	}
=======
 	
 	public List<Letters> getLetters() {
 		List<Letters> letters = new ArrayList<Letters>();
 
 		Cursor cursor = sqlDatabase.query(DatabaseHelper.TABLE_LETTERS,
 				letterscolumns, null, null,null, null, null);
 		cursor.moveToFirst();
 
 		while (!cursor.isAfterLast()) {
 			letters.add(DatabaseUtils.convertCursorToLetters(cursor));
 			cursor.moveToNext();
 		}
 
 		cursor.close();
 
 		return letters;
 	}
 	
 	public void addLetters(String letter) {
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(DatabaseHelper.COLUMN_LETTERS_WORD, letter);
 		contentValues.put(DatabaseHelper.COLUMN_LETTERS_SOUND, letter);
 		sqlDatabase.insert(DatabaseHelper.TABLE_LETTERS, null,
 				contentValues);
 	}
 	
 	public List<Alphabets> getAlphabets() {
 		List<Alphabets> alpha = new ArrayList<Alphabets>();
 
 		Cursor cursor = sqlDatabase.query(DatabaseHelper.TABLE_ALPHABET,
 				alphabetscolumns, null, null,null, null, null);
 		cursor.moveToFirst();
 
 		while (!cursor.isAfterLast()) {
 			alpha.add(DatabaseUtils.convertCursorToAlphabets(cursor));
 			cursor.moveToNext();
 		}
 
 		cursor.close();
 
 		return alpha;
 	}
 	
 	public void addAlphabets(int lid,int tid,String alphaWord,String alphaSound,Drawable alphaImage) {
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(DatabaseHelper.COLUMN_LID, lid);
 		contentValues.put(DatabaseHelper.COLUMN_TID, tid);
 		contentValues.put(DatabaseHelper.COLUMN_WORDS, alphaWord);
 		contentValues.put(DatabaseHelper.COLUMN_WORDS_SOUND, alphaSound);
 		contentValues.put(DatabaseHelper.COLUMN_WORDS_IMAGE,
 				ImageUtils.drawableToByteArray(alphaImage));
 		
 		sqlDatabase.insert(DatabaseHelper.TABLE_ALPHABET, null,
 				contentValues);
 	}
 	
 	public List<Themes> getThemes() {
 		List<Themes> theme = new ArrayList<Themes>();
 
 		Cursor cursor = sqlDatabase.query(DatabaseHelper.TABLE_THEME,
 				themecolumns, null, null,null, null, null);
 		cursor.moveToFirst();
 
 		while (!cursor.isAfterLast()) {
 			theme.add(DatabaseUtils.convertCursorToThemes(cursor));
 			cursor.moveToNext();
 		}
 
 		cursor.close();
 
 		return theme;
 	}
 	
	public void addThemes(String theme) {
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(DatabaseHelper.COLUMN_THEME_NAME, theme);
 		sqlDatabase.insert(DatabaseHelper.TABLE_THEME, null,
 				contentValues);
 	}
 	
>>>>>>> d6ace6df62a15761605bdf87971a25110f3bdf12
 }
