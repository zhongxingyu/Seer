 package ro.undef.patois;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 
 public class Database {
     private final static String TAG = "Database";
 
     public static final String DATABASE_NAME = "patois.db";
     private static final int DATABASE_VERSION = 1;
     private static final String PREFERENCES_NAME = "patois.prefs";
 
     private Activity mActivity;
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
     private SharedPreferences mPrefs;
     private TreeMap<Long, Language> mLanguagesCache;
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         private Context mCtx;
 
         DatabaseHelper(Context ctx) {
             super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
             mCtx = ctx;
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             for (String statement : readStatementsFromAsset("sql/patois.sql"))
                 db.execSQL(statement);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             // When we'll have a new version of the database, implement this
             // method.
         }
 
         private ArrayList<String> readStatementsFromAsset(String fileName) {
             try {
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(mCtx.getAssets().open(fileName)), 4096);
 
                 try {
                     String eol = System.getProperty("line.separator");
                     ArrayList<String> schema = new ArrayList<String>();
                     StringBuffer statement = new StringBuffer("");
                     String line;
 
                     while ((line = in.readLine()) != null) {
                         String trimmed = line.trim();
 
                         // Ignore comments.
                         if (trimmed.startsWith("--"))
                             continue;
 
                         // Empty lines terminate statements.
                         if (trimmed.length() == 0) {
                             if (statement.length() != 0)
                                 schema.add(statement.toString());
                             statement.setLength(0);
                             continue;
                         }
 
                         statement.append(line);
                         statement.append(eol);  // readLine() strips the EOL characters.
                     }
                     if (statement.length() != 0)
                         schema.add(statement.toString());
 
                     return schema;
                 } finally {
                     in.close();
                 }
             } catch (java.io.IOException e) {
                 throw new RuntimeException("Could not read asset file: " + fileName, e);
             }
         }
     }
 
     Database(Activity activity) {
         mActivity = activity;
         mDbHelper = new DatabaseHelper(mActivity.getApplicationContext());
         mDb = mDbHelper.getWritableDatabase();
         mPrefs = mActivity.getSharedPreferences(PREFERENCES_NAME, 0);
         mLanguagesCache = new TreeMap<Long, Language>();
     }
 
     public void close() {
         mDbHelper.close();
         mLanguagesCache.clear();
     }
 
     public static final int LANGUAGES_ID_COLUMN = 0;
     public static final int LANGUAGES_CODE_COLUMN = 1;
     public static final int LANGUAGES_NAME_COLUMN = 2;
 
     public Cursor getLanguagesCursor() {
         Cursor cursor = mDb.query("languages", new String[] { "_id", "code", "name" },
                                   null, null, null, null, null);
         mActivity.startManagingCursor(cursor);
 
         return cursor;
     }
 
     public ArrayList<Language> getLanguages() {
         ArrayList<Language> languages = new ArrayList<Language>();
 
         Cursor cursor = mDb.query("languages", new String[] { "_id", "code", "name", "num_words" },
                                   null, null, null, null, null);
         try {
             while (cursor.moveToNext()) {
                 Language language = new Language(cursor.getLong(0), cursor.getString(1),
                                                  cursor.getString(2), cursor.getLong(3));
                 mLanguagesCache.put(language.getId(), language);
                 languages.add(language);
             }
         } finally {
             cursor.close();
         }
 
         return languages;
     }
 
     public Language getLanguage(long id) {
         Language language = mLanguagesCache.get(id);
         if (language != null)
             return language;
 
         Cursor cursor = mDb.query("languages", new String[] { "code", "name", "num_words" },
                                   "_id == ?", new String[] { Long.toString(id) },
                                   null, null, null);
         try {
             if (cursor.getCount() != 1)
                 return null;
 
             cursor.moveToFirst();
             language = new Language(id, cursor.getString(0), cursor.getString(1),
                                     cursor.getLong(2));
             mLanguagesCache.put(id, language);
 
             return language;
         } finally {
             cursor.close();
         }
     }
 
     public boolean insertLanguage(Language language) {
         mLanguagesCache.put(language.getId(), language);
 
         ContentValues values = new ContentValues();
         values.put("code", language.getCode());
         values.put("name", language.getName());
 
         long id = mDb.insert("languages", null, values);
         language.setId(id);
 
         return id != -1;
     }
 
     public boolean updateLanguage(Language language) {
         mLanguagesCache.put(language.getId(), language);
 
         ContentValues values = new ContentValues();
         values.put("code", language.getCode());
         values.put("name", language.getName());
 
         return mDb.update("languages", values, "_id == ?",
                           new String[] { language.getIdString() }) == 1;
     }
 
     public boolean deleteLanguage(Language language) {
         mLanguagesCache.remove(language.getId());
 
         return mDb.delete("languages", "_id == ?",
                           new String[] { language.getIdString() }) == 1;
     }
 
     public void clearLanguagesCache() {
         mLanguagesCache.clear();
     }
 
 
     private static final String ACTIVE_LANGUAGE_PREF = "active_language";
     private static final String SORT_ORDER_PREF = "sort_order";
 
     public Language getActiveLanguage() {
         long id = mPrefs.getLong(ACTIVE_LANGUAGE_PREF, -1);
         if (id == -1)
             return null;
 
         return getLanguage(id);
     }
 
     public void setActiveLanguageId(long id) {
         SharedPreferences.Editor editor = mPrefs.edit();
         editor.putLong(ACTIVE_LANGUAGE_PREF, id);
         editor.commit();
     }
 
     public static final int SORT_ORDER_BY_NAME = 0;
     public static final int SORT_ORDER_BY_SCORE = 1;
     public static final int SORT_ORDER_NEWEST_FIRST = 2;
     public static final int SORT_ORDER_OLDEST_FIRST = 3;
 
     public int getSortOrder() {
         return mPrefs.getInt(SORT_ORDER_PREF, 0);
     }
 
     public void setSortOrder(int order) {
         SharedPreferences.Editor editor = mPrefs.edit();
         editor.putInt(SORT_ORDER_PREF, order);
         editor.commit();
     }
 
 
     public static final String BROWSE_WORDS_NAME_COLUMN = "display_name";
     public static final String BROWSE_WORDS_TRANSLATIONS_COLUMN = "display_translations";
     public static final String BROWSE_WORDS_DUMMY_SCORE_COLUMN = "sort_level";
     public static final int BROWSE_WORDS_LEVEL_FROM_COLUMN_ID = 5;
     public static final int BROWSE_WORDS_NEXT_PRACTICE_FROM_COLUMN_ID = 6;
     public static final int BROWSE_WORDS_LEVEL_TO_COLUMN_ID = 7;
     public static final int BROWSE_WORDS_NEXT_PRACTICE_TO_COLUMN_ID = 8;
 
     public Cursor getBrowseWordsCursor(Language language, String filter) {
         String pattern = "%" + filter + "%";
         String sortCriteria = "_id";
 
         switch (getSortOrder()) {
             case SORT_ORDER_BY_NAME:
                 sortCriteria = "sort_name ASC";
                 break;
             case SORT_ORDER_BY_SCORE:
                 sortCriteria = "sort_next_practice ASC, sort_level ASC";
                 break;
             case SORT_ORDER_NEWEST_FIRST:
                 sortCriteria = "timestamp DESC";
                 break;
             case SORT_ORDER_OLDEST_FIRST:
                 sortCriteria = "timestamp ASC";
                 break;
         }
 
         Cursor cursor = mDb.rawQuery(
                 "SELECT " +
                 "    t.word_id1 AS _id, " +
                 // Simple escaping for BrowseWordsActivity.applyWordMarkup().
                 "    replace(w1.name, '.', '..') AS display_name, " +
                 "    group_concat( " +
                 "      replace(w2.name, '.', '..') || ' .c(' || " +
                 "      replace(l.code, '.', '..')  || ').C', '  ') AS display_translations, " +
                 "    lower(w1.name) AS sort_name, " +
                 "    w1.timestamp AS timestamp, " +
                 "    w1.level_from AS level_from, " +
                 "    w1.next_practice_from AS next_practice_from, " +
                 "    w1.level_to AS level_to, " +
                 "    w1.next_practice_to AS next_practice_to, " +
                 "    min(w1.level_from, w1.level_to) AS sort_level, " +
                 "    min(w1.next_practice_from, w1.next_practice_to) AS sort_next_practice " +
                 "  FROM " +
                 "    translations AS t, " +
                 "    words AS w1, " +
                 "    words AS w2, " +
                 "    languages AS l " +
                 "  WHERE " +
                 "    w1.language_id == ? AND " +
                 "    ((w1.name LIKE ?) OR (w2.name LIKE ?)) AND " +
                 "    t.word_id1 == w1._id AND " +
                 "    t.word_id2 == w2._id AND " +
                 "    w2.language_id == l._id " +
                 "  GROUP BY (t.word_id1) " +
                 "UNION " +
                 "SELECT " +
                 "    w._id AS _id, " +
                 "    '.u' || replace(w.name, '.', '..') || '.U' AS display_name, " +
                 "    '.c.0.C' AS display_translations, " +
                 "    lower(w.name) AS sort_name, " +
                 "    w.timestamp AS timestamp, " +
                 "    0 AS level_from, " +
                 "    0 AS next_practice_from, " +
                 "    0 AS level_to, " +
                 "    0 AS next_practice_to, " +
                 "    0 AS sort_level, " +
                 "    0 AS sort_next_practice " +
                 "  FROM " +
                 "    words AS w " +
                 "  WHERE " +
                 "    w.language_id == ? AND " +
                 "    w.name LIKE ? AND " +
                 "    w.num_translations == 0 " +
                 "ORDER BY " + sortCriteria,
                 new String[] {
                     language.getIdString(),
                     pattern,
                     pattern,
                     language.getIdString(),
                     pattern,
                 });
         mActivity.startManagingCursor(cursor);
 
         return cursor;
     }
 
     public static final String WORDS_NAME_COLUMN = "name";
     public static final int WORDS_NAME_COLUMN_ID = 1;
 
     public Cursor getWordsCursor(Language language, String filter, Word mainWord) {
         // We want to avoid suggesting the main word as a translation of
         // itself, so we have to explicitly filter it out here.
         Cursor cursor = mDb.query("words", new String[] { "_id", "name" },
                                   "(language_id == ?) AND (name LIKE ?) AND (_id != ?)",
                                   new String[] {
                                       language.getIdString(),
                                       "%" + filter + "%",
                                       mainWord.getIdString(),
                                   },
                                   null, null, null);
         mActivity.startManagingCursor(cursor);
 
         return cursor;
     }
 
     public ArrayList<Trainer.Weight> getWordWeights(Language language,
                                                     Trainer.Direction direction) {
         ArrayList<Trainer.Weight> weights = new ArrayList<Trainer.Weight>();
 
         Cursor cursor = mDb.rawQuery(
                 "SELECT " +
                 "    _id, " +
                 "    strftime('%s', 'now') - next_practice" + direction.getSuffix() +
                 "       AS weight " +
                 "  FROM " +
                 "    words " +
                 "  WHERE " +
                 "    language_id == ? AND " +
                 "    weight > 0 AND " +
                 "    num_translations > 0 ",
                 new String[] {
                     language.getIdString(),
                 });
 
         try {
             while (cursor.moveToNext())
                 weights.add(new Trainer.Weight(cursor.getLong(0), cursor.getLong(1)));
         } finally {
             cursor.close();
         }
 
         return weights;
     }
 
     public Word getWord(long id) {
         Cursor cursor = mDb.query("words",
                                   new String[] { "name", "language_id", },
                                   "_id == ?", new String[] { Long.toString(id) },
                                   null, null, null);
         try {
             if (cursor.getCount() != 1)
                 return null;
 
             cursor.moveToFirst();
             return new Word(id, cursor.getString(0), getLanguage(cursor.getLong(1)));
         } finally {
             cursor.close();
         }
     }
 
     public boolean insertWord(Word word) {
         final int level = 0;
         final long timestamp = System.currentTimeMillis() / 1000;
         final long next_practice = Trainer.scheduleNextPractice(timestamp, level);
 
         ContentValues values = new ContentValues();
         values.put("name", word.getName());
         values.put("language_id", word.getLanguage().getId());
         values.put("timestamp", timestamp);
         values.put("level_from", level);
         values.put("next_practice_from", next_practice);
         values.put("level_to", level);
         values.put("next_practice_to", next_practice);
 
         long id = mDb.insert("words", null, values);
         word.setId(id);
         return id != -1;
     }
 
     public boolean updateWord(Word word) {
         ContentValues values = new ContentValues();
         values.put("name", word.getName());
         values.put("language_id", word.getLanguage().getId());
 
         return mDb.update("words", values, "_id == ?",
                           new String[] { word.getIdString() }) == 1;
     }
 
     public boolean deleteWord(Word word) {
         return mDb.delete("words", "_id == ?",
                           new String[] { word.getIdString() }) == 1;
     }
 
     public boolean deleteWordById(long id) {
         return mDb.delete("words", "_id == ?",
                           new String[] { Long.toString(id) }) == 1;
     }
 
     public ArrayList<Word> getTranslations(Word word) {
         ArrayList<Word> translations = new ArrayList<Word>();
 
         Cursor cursor = mDb.query("translations", new String[] { "word_id2" },
                                   "word_id1 == ?", new String[] { word.getIdString() },
                                   null, null, null);
         try {
             while (cursor.moveToNext())
                 translations.add(getWord(cursor.getLong(0)));
         } finally {
             cursor.close();
         }
 
         return translations;
     }
 
     public void insertTranslation(Word word1, Word word2) {
         ContentValues values = new ContentValues();
         values.put("word_id1", word1.getId());
         values.put("word_id2", word2.getId());
         mDb.insert("translations", null, values);
 
         values.clear();
         values.put("word_id1", word2.getId());
         values.put("word_id2", word1.getId());
         mDb.insert("translations", null, values);
     }
 
     public void deleteTranslation(Word word1, Word word2) {
         mDb.delete("translations",
                    "(word_id1 == ? AND word_id2 == ?) OR (word_id1 == ? AND word_id2 == ?)",
                    new String[] {
                        word1.getIdString(),
                        word2.getIdString(),
                        word2.getIdString(),
                        word1.getIdString(),
                    });
     }
 
     public void insertPracticeLogEntry(int trainerVersion, Word word,
                                        Trainer.Direction direction, boolean successful) {
         ContentValues values = new ContentValues();
         values.put("trainer", trainerVersion);
         values.put("word_id", word.getId());
         values.put("direction", direction.getValue());
         values.put("successful", successful);
 
         mDb.insert("practice_log", null, values);
     }
 
     public Trainer.PracticeInfo getPracticeInfo(Word word, Trainer.Direction direction) {
         Cursor cursor = mDb.query("words",
                                   new String[] {
                                       "level" + direction.getSuffix(),
                                       "next_practice" + direction.getSuffix(),
                                   },
                                  "word_id == ?",
                                   new String[] { word.getIdString() },
                                   null, null, null);
         try {
             if (cursor.getCount() != 1)
                 return null;
 
             cursor.moveToFirst();
             return new Trainer.PracticeInfo(direction, cursor.getInt(0), cursor.getLong(1));
         } finally {
             cursor.close();
         }
     }
 
     public boolean updatePracticeInfo(Word word, Trainer.PracticeInfo info) {
         ContentValues values = new ContentValues();
         values.put("level" + info.direction.getSuffix(), info.level);
         values.put("next_practice" + info.direction.getSuffix(), info.next_practice);
 
         return mDb.update("words", values, "_id == ?",
                           new String[] { word.getIdString() }) == 1;
     }
 
     public static File getDatabaseFile(Context context) {
         return context.getDatabasePath(DATABASE_NAME);
     }
 
     public static class Lock {
         private SQLiteDatabase mDb;
 
         public Lock(String dbFileName) {
             mDb = SQLiteDatabase.openDatabase(dbFileName, null, SQLiteDatabase.OPEN_READWRITE);
 
             // As per http://www.sqlite.org/backup.html, in order to back up an
             // SQLite database, one has to:
             //   1. Establish a shared lock on the database file.
             //   2. Copy the database file using an external tool.
             //   3. Relinquish the shared lock on the database file.
             //
             // This is step 1. from above, since beginTransaction() acquires an
             // EXCLUSIVE lock, which is stronger than SHARED.
             mDb.beginTransaction();
         }
 
         public void release() {
             mDb.endTransaction();
             mDb.close();
         }
     }
 }
