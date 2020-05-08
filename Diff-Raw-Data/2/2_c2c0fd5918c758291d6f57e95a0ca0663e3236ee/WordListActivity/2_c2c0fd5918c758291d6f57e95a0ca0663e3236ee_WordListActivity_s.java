 package com.ell.MemoRazor;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v7.app.ActionBar;
 import android.view.*;
 import android.widget.*;
 import com.ell.MemoRazor.adapters.WordAdapter;
 import com.ell.MemoRazor.data.DatabaseHelper;
 import com.ell.MemoRazor.data.Word;
 import com.ell.MemoRazor.data.WordGroup;
 import com.ell.MemoRazor.helpers.*;
 import com.ell.MemoRazor.translators.YandexOpenJSONTranslator;
 import com.j256.ormlite.dao.Dao;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class WordListActivity extends MemoRazorActivity {
     private ArrayList<Word> words;
     private Dao<Word, Integer> wordsDao;
     private Dao<WordGroup, Integer> wordGroupsDao;
 
     private WordGroup selectedGroup;
     private ListView wordsListView;
     private WordAdapter wordsAdapter;
 
     private WordPlaybackManager playbackManager;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.words);
 
         int selectedGroupId = getIntent().getIntExtra(WordGroupsActivity.EXTRA_GROUP_ID, 0);
         wordsListView = (ListView) findViewById(R.id.words_list);
 
         try {
             wordsDao = getHelper().getWordDao();
             wordGroupsDao = getHelper().getWordGroupDao();
             selectedGroup = wordGroupsDao.queryForId(selectedGroupId);
             setTitle(selectedGroup.getName());
             getSupportActionBar().setIcon(LanguageHelper.langCodeToImage(selectedGroup.getLanguage()));
 
             words = new ArrayList<Word>(wordsDao.queryBuilder()
                     .orderBy(Word.CREATED_DATE_COLUMN, false)
                     .where()
                     .eq(Word.WORDGROUP_ID_FIELD_NAME, selectedGroupId)
                     .query());
         } catch (SQLException e) {
             words = new ArrayList<Word>();
         }
         wordsAdapter = new WordAdapter(this, R.layout.word_layout, words);
         wordsListView.setAdapter(wordsAdapter);
 
         registerForContextMenu(wordsListView);
 
         playbackManager = new WordPlaybackManager(getHelper(), wordsAdapter);
         wordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Word selectedWord = words.get(i);
                playbackManager.PlayWord(selectedWord);
             }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.wordmenu, menu);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         final String GOOGLE_TRANSLATE_URL_TEMPLATE = "http://translate.google.com/m?hl=en&sl=%s&tl=%s&ie=UTF-8&prev=_m&q=%s";
         final String YANDEX_URL_TEMPLATE = "http://m.slovari.yandex.ru/translate.xml?text=%s&lang=en";
         final String MULTITRAN_URL_TEMPLATE = "http://www.multitran.ru/c/M.exe?CL=1&s=%s&l1=1";
         final String DICTIONARY_COM_URL_TEMPLATE = "http://dictionary.reference.com/browse/%s";
 
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         final Word selectedWord = words.get(info.position);
 
         switch (item.getItemId()) {
             case 0:
                 if (NetworkHelper.isNetworkOnline(this)) {
                     scheduleWordTranslate(selectedWord, true);
                 } else {
                     DialogHelper.MessageBox(this, getResources().getString(R.string.words_noNetwork));
                 }
                 break;
             case 1:
                 deleteWord(selectedWord);
                 break;
             case 2:
                 final Context context = this;
                 DialogHelper.RequestInput(this, context.getResources().getString(R.string.app_name), getResources().getString(R.string.words_editTranslation), selectedWord.getMeaning(), new DialogHelper.OnRequestInputListener() {
                     @Override
                     public void onRequestInput(String input) {
                         try {
                             if (wordsDao != null) {
                                 selectedWord.setMeaning(input);
                                 selectedWord.setTranscription("");
 
                                 wordsAdapter.notifyDataSetChanged();
                                 wordsDao.update(selectedWord);
                             }
                         } catch (SQLException e) {
                         }
                     }
                 });
                 break;
             case 3:
                 OpenUrl(String.format(GOOGLE_TRANSLATE_URL_TEMPLATE,
                         selectedWord.getLanguage(),
                         App.getFirstLanguage(),
                         selectedWord.getName()));
                 break;
             case 4:
                 OpenUrl(String.format(YANDEX_URL_TEMPLATE, selectedWord.getName()));
                 break;
             case 5:
                 OpenUrl(String.format(MULTITRAN_URL_TEMPLATE, selectedWord.getName()));
                 break;
             case 6:
                 OpenUrl(String.format(DICTIONARY_COM_URL_TEMPLATE, selectedWord.getName()));
                 break;
         }
 
         return true;
     }
 
     private void deleteWord(final Word selectedWord) {
         DialogHelper.Confirm(this, getResources().getString(R.string.words_confirmDelete), new DialogHelper.OnConfirmListener() {
             @Override
             public void onConfirm() {
                 wordsAdapter.remove(selectedWord);
                 selectedGroup.getWords().remove(selectedWord);
 
                 try {
                     if (wordsDao != null) {
                         wordsDao.delete(selectedWord);
                     }
                 } catch (SQLException e) {
                 }
             }
         });
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         if (v.getId() == R.id.words_list) {
             menu.setHeaderTitle(getResources().getString(R.string.words_performAction));
 
             menu.add(Menu.NONE, 0, 0, getResources().getString(R.string.words_translate));
             menu.add(Menu.NONE, 1, 1, getResources().getString(R.string.words_delete));
             menu.add(Menu.NONE, 2, 2, getResources().getString(R.string.words_editTranslation));
             menu.add(Menu.NONE, 3, 3, getResources().getString(R.string.words_openGoogleTranslate));
 
            if (App.getFirstLanguage() == "ru") {
                 menu.add(Menu.NONE, 4, 4, getResources().getString(R.string.words_openYandex));
                 menu.add(Menu.NONE, 5, 5, getResources().getString(R.string.words_openMultitran));
             }
             menu.add(Menu.NONE, 6, 6, getResources().getString(R.string.words_openDictionary));
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_add_word:
                 addWord();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void addWord() {
         final Context context = this;
         if (!NetworkHelper.isNetworkOnline(context)) {
             DialogHelper.MessageBox(context, getString(R.string.no_network_available));
             return;
         }
 
         DialogHelper.RequestInput(this, getResources().getString(R.string.words_addWord), new DialogHelper.OnRequestInputListener() {
             @Override
             public void onRequestInput(String input) {
                 Word word = new Word(input);
 
                 wordsAdapter.insert(word, 0);
 
                 try {
                     if (wordGroupsDao != null) {
                         word.setLanguage(selectedGroup.getLanguage());
                         selectedGroup.getWords().add(word);
                         wordGroupsDao.update(selectedGroup);
                     }
 
                     if (NetworkHelper.isNetworkOnline(context)) {
                         scheduleWordTranslate(word, false);
                     }
                 } catch (SQLException e) {
                 }
             }
         });
     }
 
     protected void scheduleWordTranslate(Word selectedWord, boolean force) {
         if (!YandexOpenJSONTranslator.isTranslatable(selectedWord, force)) {
             return;
         }
 
         final String nativeLanguage = App.getFirstLanguage();
         selectedWord.setTranscription(null);
         selectedWord.setFetchingTranslation(true);
         selectedWord.setFetchingPlayback(true);
         wordsAdapter.notifyDataSetChanged();
         new AsyncTask<Word, Void, Word>() {
             @Override
             protected Word doInBackground(Word... words) {
                 YandexOpenJSONTranslator translator = new YandexOpenJSONTranslator();
                 return translator.translateWord(words[0], words[0].getLanguage(), nativeLanguage);
             }
 
             @Override
             protected void onPostExecute(Word translatedWord) {
                 translatedWord.setFetchingTranslation(false);
                 wordsAdapter.notifyDataSetChanged();
                 try {
                     wordsDao.update(translatedWord);
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
             }
         }.execute(selectedWord);
 
         final DatabaseHelper databaseHelper = getHelper();
         new AsyncTask<Word, Void, Word>() {
             @Override
             protected Word doInBackground(Word... words) {
                 Word selectedWord = words[0];
                 WordPlaybackManager.CacheWordPlayback(databaseHelper, selectedWord);
                 return selectedWord;
             }
 
             @Override
             protected void onPostExecute(Word translatedWord) {
                 translatedWord.setFetchingPlayback(false);
                 wordsAdapter.notifyDataSetChanged();
             }
         }.execute(selectedWord);
     }
 
     protected void OpenUrl(String url) {
         Intent i = new Intent(Intent.ACTION_VIEW);
         i.setData(Uri.parse(url));
         startActivity(i);
     }
 }
