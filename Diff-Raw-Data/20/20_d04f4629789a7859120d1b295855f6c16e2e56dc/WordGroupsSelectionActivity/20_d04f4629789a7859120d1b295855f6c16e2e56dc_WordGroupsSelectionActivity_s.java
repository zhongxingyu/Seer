 package com.ell.MemoRazor;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v7.app.ActionBar;
 import android.view.*;
 import android.widget.*;
 import com.ell.MemoRazor.adapters.WordGroupSelectionAdapter;
 import com.ell.MemoRazor.data.Word;
 import com.ell.MemoRazor.data.WordGroup;
 import com.ell.MemoRazor.helpers.DialogHelper;
 import com.ell.MemoRazor.translators.YandexOpenJSONTranslator;
 import com.j256.ormlite.dao.Dao;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class WordGroupsSelectionActivity extends MemoRazorActivity {
     public static final String EXTRA_SELECTED_WORDS = "com.ell.SELECTED_WORDS";
     private ArrayList<WordGroup> wordGroups;
     private Dao<WordGroup, Integer> wordGroupsDao;
     private ListView groupsListView;
     private WordGroupSelectionAdapter wordGroupSelectionAdapter;
     private String action;
     private MenuItem startItem;
 
     @Override
     protected void configureActionBar(ActionBar actionBar) {
         super.configureActionBar(actionBar);
         actionBar.setIcon(R.drawable.group);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.wordgroups_selection);
     }
 
     @Override
     protected void bindControls() {
         super.bindControls();
 
         groupsListView = (ListView) findViewById(R.id.groups_selection);
     }
 
     @Override
     protected void initialize() {
         super.initialize();
 
         setTitle(getResources().getString(R.string.wordGroups_selectWordGroups));
         generateEmptyView(groupsListView, getString(R.string.no_word_groups));
 
         try {
             wordGroupsDao = getHelper().getWordGroupDao();
 
             wordGroups = new ArrayList<WordGroup>(wordGroupsDao.queryBuilder().orderBy(WordGroup.CREATED_DATE_COLUMN, false).query());
         } catch (SQLException e) {
             wordGroups = new ArrayList<WordGroup>();
         }
 
 
         wordGroupSelectionAdapter = new WordGroupSelectionAdapter(this, R.layout.word_group_selection_layout, wordGroups);
         groupsListView.setAdapter(wordGroupSelectionAdapter);
         groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 CheckBox checkBox = (CheckBox)view.findViewById(R.id.selection_group_checkbox);
                 checkBox.setChecked(!checkBox.isChecked());
             }
         });
 
         action = getIntent().getStringExtra(MainActivity.EXTRA_SELECTEDWORDS_ACTION);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.wordgroupselectionmenu, menu);
         startItem = menu.getItem(0);
         startItem.setEnabled(false);
         wordGroupSelectionAdapter.setMenuItem(startItem);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_start:
                 startQuiz();
                 break;
             default:
                 return super.onOptionsItemSelected(item);
         }
         return true;
     }
 
     private void startQuiz() {
         ArrayList<WordGroup> selectedWordGroups = wordGroupSelectionAdapter.getSelectedWordGroups();
         if (selectedWordGroups.size() == 0) {
             DialogHelper.MessageBox(this, getResources().getString(R.string.select_atleast_one_group));
         } else {
             ArrayList<Word> allWords = new ArrayList<Word>();
             for (WordGroup wg : selectedWordGroups) {
                 for (Word w : wg.getWords()) {
                     if (!(w.getMeaning() == null || w.getMeaning().length() == 0 ||
                             w.getMeaning().equals(YandexOpenJSONTranslator.YANDEX_TRANSLATION_NOT_AVAILABLE) ||
                             w.getFetchingTranslation())) {
                         allWords.add(w);
                     }
                 }
             }
             if (allWords.size() == 0) {
                 DialogHelper.MessageBox(this, getResources().getString(R.string.no_words_selected));
             } else {
                 Intent quizIntent;
                if (action.equals("quiz")) {
                     quizIntent = (App.getSimplifiedQuiz()) ? new Intent(this, SimpleQuizActivity.class) : new Intent(this, QuizActivity.class);
                } else if (action.equals("cards")) {
                     quizIntent = new Intent(this, CardsActivity.class);
                 } else {
                     return;
                 }
 
                 quizIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                 quizIntent.putExtra(EXTRA_SELECTED_WORDS, allWords);
                 startActivity(quizIntent);
             }
         }
     }
 }
