 package ro.undef.patois;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.text.InputType;
 import android.text.Spanned;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.FilterQueryProvider;
 import android.widget.LinearLayout;
 import android.widget.SimpleCursorAdapter;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 
 public class EditWordActivity extends Activity {
     private final static String TAG = "EditWordActivity";
 
     private PatoisDatabase mDb;
 
     private LinearLayout mTranslationsLayout;
     private LayoutInflater mInflater;
     private View mAddButton;
     private View mDoneButton;
     private View mNewWordButton;
     private View mCancelButton;
     private WordEntry mLanguageListener;
 
     // These fields are saved across restarts.
     private WordEntry mMainWordEntry;
     private ArrayList<TranslationEntry> mTranslationEntries;
     private boolean mAddButtonHasFocus;
     private boolean mDoneButtonHasFocus;
     private boolean mNewWordButtonHasFocus;
     private boolean mCancelButtonHasFocus;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mDb = new PatoisDatabase(this);
 
         Intent intent = getIntent();
         if (savedInstanceState != null) {
             loadStateFromBundle(savedInstanceState);
         } else if (intent.getAction().equals(Intent.ACTION_EDIT)) {
             loadStateFromDatabase(intent.getExtras().getLong("word_id"));
         } else {
             resetState();
         }
 
         setContentView(R.layout.edit_word);
         setupViews();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         mDb.close();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	MenuInflater inflater = getMenuInflater();
 	inflater.inflate(R.menu.edit_word_activity_menu, menu);
 	return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.delete_word: {
                 if (mMainWordEntry.getWord().isInDatabase())
                     mDb.deleteWord(mMainWordEntry.getWord());
                 finish();
                 return true;
             }
         }
         return false;
     }
 
     private void resetState() {
         mMainWordEntry = new WordEntry(mDb.getActiveLanguage());
 
         mTranslationEntries = new ArrayList<TranslationEntry>();
         mTranslationEntries.add(new TranslationEntry(pickTranslationLanguage()));
 
         mLanguageListener = null;
 
         mAddButtonHasFocus = false;
         mDoneButtonHasFocus = false;
         mNewWordButtonHasFocus = false;
         mCancelButtonHasFocus = false;
     }
 
     private void loadStateFromDatabase(long wordId) {
         Word mainWord = mDb.getWord(wordId);
         mMainWordEntry = new WordEntry(mainWord);
 
         ArrayList<TranslationEntry> entries = new ArrayList<TranslationEntry>();
         for (Word word : mDb.getTranslations(mainWord))
             entries.add(new TranslationEntry(word, true));
         mTranslationEntries = entries;
 
         mLanguageListener = null;
 
         mAddButtonHasFocus = false;
         mDoneButtonHasFocus = false;
         mNewWordButtonHasFocus = false;
         mCancelButtonHasFocus = false;
     }
 
     private void saveStateToDatabase() {
         mMainWordEntry.saveToDatabase(mDb);
 
         for (TranslationEntry entry : mTranslationEntries) {
             entry.saveToDatabase(mDb, mMainWordEntry.getWord());
         }
     }
 
     @SuppressWarnings("unchecked")
     private void loadStateFromBundle(Bundle savedInstanceState) {
         mMainWordEntry = (WordEntry) savedInstanceState.getSerializable("main_word");
 
         mTranslationEntries = (ArrayList<TranslationEntry>)
             savedInstanceState.getSerializable("translations");
 
         mLanguageListener = null;
 
         mAddButtonHasFocus = savedInstanceState.getBoolean("add_translation");
         mDoneButtonHasFocus = savedInstanceState.getBoolean("done");
         mNewWordButtonHasFocus = savedInstanceState.getBoolean("new_word");
         mCancelButtonHasFocus = savedInstanceState.getBoolean("cancel");
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         mMainWordEntry.syncFromView();
         outState.putSerializable("main_word", mMainWordEntry);
 
         for (TranslationEntry entry : mTranslationEntries)
             entry.syncFromView();
         outState.putSerializable("translations", mTranslationEntries);
 
         outState.putBoolean("add_translation", mAddButton.hasFocus());
         outState.putBoolean("done", mDoneButton.hasFocus());
         outState.putBoolean("new_word", mNewWordButton.hasFocus());
         outState.putBoolean("cancel", mCancelButton.hasFocus());
     }
 
     private void setupViews() {
         mMainWordEntry.setupView(this, findViewById(R.id.main_word));
         if (mMainWordEntry.hasLanguageDialogOpen())
             mLanguageListener = mMainWordEntry;
 
         LayoutInflater inflater = mInflater = getLayoutInflater();
         LinearLayout layout = mTranslationsLayout = (LinearLayout) findViewById(R.id.translations);
         layout.removeAllViews();
         for (TranslationEntry entry : mTranslationEntries) {
             entry.addViewToList(this, layout, inflater);
             if (entry.hasLanguageDialogOpen())
                 mLanguageListener = entry;
         }
 
         mAddButton = findViewById(R.id.add_translation);
         mAddButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 addNewTranslation();
             }
         });
         if (mAddButtonHasFocus)
             mAddButton.requestFocus();
 
         mDoneButton = findViewById(R.id.done);
         mDoneButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 saveStateToDatabase();
                 finish();
             }
         });
         if (mDoneButtonHasFocus)
             mDoneButton.requestFocus();
 
         mNewWordButton = findViewById(R.id.new_word);
         mNewWordButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 saveStateToDatabase();
                 Intent intent = new Intent();
                 intent.setClass(EditWordActivity.this, EditWordActivity.class);
                 intent.setAction(Intent.ACTION_INSERT);
                 startActivity(intent);
                 finish();
             }
         });
         if (mNewWordButtonHasFocus)
             mNewWordButton.requestFocus();
 
         mCancelButton = findViewById(R.id.cancel);
         mCancelButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 finish();
             }
         });
         if (mCancelButtonHasFocus)
             mCancelButton.requestFocus();
     }
 
     private void addNewTranslation() {
         TranslationEntry entry = new TranslationEntry(pickTranslationLanguage());
         mTranslationEntries.add(entry);
         entry.addViewToList(this, mTranslationsLayout, mInflater);
     }
 
     private void reloadTranslations(Word mainWord) {
         // First, clear all entries that don't contain any user-entered
         // information.
         for (TranslationEntry entry : mTranslationEntries)
             if (entry.isEmpty())
                 entry.markAsDeleted();
 
         // Then, add all the translations of new mainWord.
         for (Word word : mDb.getTranslations(mainWord)) {
             boolean duplicate = false;
             for (TranslationEntry entry : mTranslationEntries)
                 if (entry.getWord().equals(word)) {
                     duplicate = true;
                     entry.setIsInDatabase(true);
                 }
 
             if (!duplicate) {
                 TranslationEntry entry = new TranslationEntry(word, true);
                 mTranslationEntries.add(entry);
                 entry.addViewToList(this, mTranslationsLayout, mInflater);
             }
         }
     }
 
     private Language pickTranslationLanguage() {
         // The "selected" language is the language with most words that is not
         // used already.
         Language selected = null;
         // The "most_words" language is the language with most words,
         // regardless if it was already used or not.  We only return it if no
         // language satisfies the requirements for "selected" (i.e., all
         // languages are used).
         Language most_words = null;
 
 language_search:
         for (Language language : mDb.getLanguages()) {
             if (most_words == null || most_words.getNumWords() < language.getNumWords())
                 most_words = language;
 
             if (language.equals(mMainWordEntry.getWord().getLanguage()))
                 continue language_search;
             for (TranslationEntry entry : mTranslationEntries) {
                 if (!entry.isDeleted() && language.equals(entry.getWord().getLanguage()))
                     continue language_search;
             }
             if (selected == null || selected.getNumWords() < language.getNumWords())
                 selected = language;
         }
 
         if (selected != null)
             return selected;
 
         return most_words;
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_BACK: {
                 saveStateToDatabase();
                 finish();
                 return true;
             }
         }
         return super.onKeyDown(keyCode, event);
     }
 
     private void showSelectLanguageDialog(WordEntry listener) {
         mLanguageListener = listener;
         showDialog(R.id.select_language);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case R.id.select_language:
                 final Cursor cursor = mDb.getLanguagesCursor();
                 return new AlertDialog.Builder(this)
                     .setTitle(R.string.select_language)
                     .setCursor(cursor, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                             cursor.moveToPosition(which);
                             Language language = mDb.getLanguage(
                                     cursor.getLong(PatoisDatabase.LANGUAGES_ID_COLUMN));
                             mLanguageListener.setLanguage(language);
                             mLanguageListener = null;
                         }
                     }, "name")
                     .create();
         }
         return null;
     }
 
     private CursorAdapter getWordsAdapter(final Word word) {
         SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                 this,
                 android.R.layout.simple_dropdown_item_1line,
                 mDb.getWordsCursor(word.getLanguage(), word.getName(),
                                    mMainWordEntry.getWord()),
                 new String[] {
                     PatoisDatabase.WORDS_NAME_COLUMN,
                 },
                 new int[] {
                     android.R.id.text1,
                 });
         adapter.setStringConversionColumn(PatoisDatabase.WORDS_NAME_COLUMN_ID);
         adapter.setFilterQueryProvider(new FilterQueryProvider() {
             public Cursor runQuery(CharSequence constraint) {
                 return mDb.getWordsCursor(word.getLanguage(),
                                           (constraint != null) ? constraint.toString() : "",
                                           mMainWordEntry.getWord());
             }
         });
         return adapter;
     }
 
     private Word getWord(long id) {
         return mDb.getWord(id);
     }
 
 
     private static class WordEntry implements Serializable {
         protected Word mWord;
         public Word getWord() { return mWord; }
 
         protected int mNameSelectionStart;
         protected int mNameSelectionEnd;
         protected boolean mLanguageButtonHasFocus;
         protected boolean mHasLanguageDialogOpen;
 
         protected boolean mEditable;
 
         transient protected Button mLanguageButton;
         transient protected AutoCompleteTextView mNameEditText;
         transient protected EditWordActivity mActivity;
 
         public WordEntry(Word word) {
             mWord = word;
             mNameSelectionStart = -1;
             mNameSelectionEnd = -1;
             mLanguageButtonHasFocus = false;
             mHasLanguageDialogOpen = false;
             mEditable = true;
         }
 
         public WordEntry(Language language) {
             this(new Word(language));
         }
 
         public void setupView(final EditWordActivity activity, View view) {
             mActivity = activity;
 
             mLanguageButton = (Button) view.findViewById(R.id.language);
             mLanguageButton.setText(mWord.getLanguage().getCode());
             mLanguageButton.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     mHasLanguageDialogOpen = true;
                     mActivity.showSelectLanguageDialog(WordEntry.this);
                 }
             });
             if (mLanguageButtonHasFocus)
                 mLanguageButton.requestFocus();
 
             mNameEditText = (AutoCompleteTextView) view.findViewById(R.id.name);
             if (mEditable && !mWord.isInDatabase()) {
                 mNameEditText.setAdapter(mActivity.getWordsAdapter(mWord));
                 mNameEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                     public void onItemClick(AdapterView<?> parent, View view,
                                             int position, long id) {
                         changeWord(mActivity.getWord(id));
                     }
                 });
             }
             mNameEditText.setText(mWord.getName());
             if (mNameSelectionStart != -1 && mNameSelectionEnd != -1) {
                 mNameEditText.requestFocus();
                 mNameEditText.setSelection(mNameSelectionStart, mNameSelectionEnd);
             }
 
             if (!mEditable)
                 disableEditing();
         }
 
         protected void disableEditing() {
             Resources res = mActivity.getResources();
 
             mEditable = false;
 
             mLanguageButton.setEnabled(false);
             mLanguageButton.setTextColor(res.getColor(android.R.color.primary_text_dark));
 
             // Stop the soft-keyboard from showing up.
             mNameEditText.setInputType(InputType.TYPE_NULL);
             // Reject all modifications to the text.
             mNameEditText.setFilters(new InputFilter[] { new InputFilter() {
                 public CharSequence filter(CharSequence src, int start, int end,
                                            Spanned dst, int dstart, int dend) {
                    return src.length() < 1 ? dst.subSequence(dstart, dend) : "";
                 }
             } });
             disableAutocompletion();
         }
 
         protected void disableAutocompletion() {
             mNameEditText.setAdapter((CursorAdapter)null);
         }
 
         protected void changeWord(Word word) {
             mWord = word;
             // The main word should stay editable even when it's already in the
             // database.  However, we don't want its ID to change, so we
             // disable the autocompletion.
             disableAutocompletion();
             mActivity.reloadTranslations(mWord);
         }
 
         public void syncFromView() {
             if (mEditable)
                 mWord.setName(mNameEditText.getText().toString());
 
             if (mNameEditText.hasFocus()) {
                 mNameSelectionStart = mNameEditText.getSelectionStart();
                 mNameSelectionEnd = mNameEditText.getSelectionEnd();
             } else {
                 mNameSelectionStart = -1;
                 mNameSelectionEnd = -1;
             }
             mLanguageButtonHasFocus = mLanguageButton.hasFocus();
         }
 
         public void saveToDatabase(PatoisDatabase db) {
             syncFromView();
 
             if (mWord.isEmpty())
                 return;
 
             if (mWord.isInDatabase()) {
                 if (mEditable)
                     db.updateWord(mWord);
             } else {
                 db.insertWord(mWord);
             }
         }
 
         public void setLanguage(Language language) {
             mWord.setLanguage(language);
             mLanguageButton.setText(mWord.getLanguage().getCode());
             mHasLanguageDialogOpen = false;
         }
 
         public boolean hasLanguageDialogOpen() {
             return mHasLanguageDialogOpen;
         }
 
         // Required for the Serializable interface.
         static final long serialVersionUID = 506921538917961504L;
     }
 
 
     private static class TranslationEntry extends WordEntry {
         private boolean mDeleted;
         private boolean mDeleteButtonHasFocus;
         // True if there is an entry (actually, two rows) in the "translations"
         // table for this translation entry.  In that case, the "translations"
         // table doesn't need updating.
         private boolean mIsInDatabase;
 
         transient private View mView;
         transient private View mDeleteButton;
 
         public TranslationEntry(Word word, boolean isInDatabase) {
             super(word);
             mEditable = !word.isInDatabase();
             mDeleted = false;
             mDeleteButtonHasFocus = false;
             mIsInDatabase = isInDatabase;
         }
 
         public TranslationEntry(Language language) {
             this(new Word(language), false);
         }
 
         public void addViewToList(EditWordActivity activity,
                                   LinearLayout parent, LayoutInflater inflater) {
             if (mDeleted)
                 return;
 
             View view = inflater.inflate(R.layout.edit_word_entry, parent, false);
             setupView(activity, view);
             parent.addView(view);
         }
 
         public void setupView(EditWordActivity activity, View view) {
             super.setupView(activity, view);
 
             mView = view;
 
             mDeleteButton = view.findViewById(R.id.delete);
             mDeleteButton.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     markAsDeleted();
                 }
             });
             if (mDeleteButtonHasFocus)
                 mDeleteButton.requestFocus();
         }
 
         protected void changeWord(Word word) {
             mWord = word;
             disableEditing();
         }
 
         public void syncFromView() {
             if (mDeleted)
                 return;
 
             super.syncFromView();
 
             mDeleteButtonHasFocus = mDeleteButton.hasFocus();
         }
 
         private void markAsDeleted() {
             LinearLayout parent = (LinearLayout) mView.getParent();
             parent.removeView(mView);
             mDeleted = true;
         }
 
         public boolean isDeleted() {
             return mDeleted;
         }
 
         public void setIsInDatabase(boolean isInDatabase) {
             mIsInDatabase = isInDatabase;
         }
 
         public boolean isEmpty() {
             // An "empty" TranslationEntry is one that doesn't contain any
             // information that needs to be saved to persistent storage.
             if (mDeleted)
                 return false;
             if (mWord.isInDatabase() && !mEditable)
                 return false;
             if (mNameEditText.getText().length() != 0)
                 return false;
             return true;
         }
 
         public void saveToDatabase(PatoisDatabase db, Word mainWord) {
             if (mDeleted) {
                 if (mainWord.isInDatabase() && mWord.isInDatabase() && mIsInDatabase)
                     db.deleteTranslation(mainWord, mWord);
             } else {
                 super.saveToDatabase(db);
                 if (mainWord.isInDatabase() && mWord.isInDatabase() && !mIsInDatabase)
                     db.insertTranslation(mainWord, mWord);
             }
         }
 
         // Required for the Serializable interface.
         static final long serialVersionUID = -4401068042075884608L;
     }
 }
