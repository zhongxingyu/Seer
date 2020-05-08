 package com.example.aperture.core;
 
 import android.app.Activity;
 import android.app.ListActivity;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 import android.os.Bundle;
 import android.os.RemoteException;
 
 import android.preference.PreferenceManager;
 
 import android.provider.MediaStore;
 
 import android.speech.RecognitionListener;
 import android.speech.RecognizerIntent;
 import android.speech.SpeechRecognizer;
 
 import android.text.InputType;
 
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 
 import android.widget.ArrayAdapter;
 import android.widget.HeaderViewListAdapter;
 import android.widget.ListView;
 import android.widget.ListView.FixedViewInfo;
 import android.widget.RelativeLayout;
 import android.widget.SearchView;
 import android.widget.TextView;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.TreeSet;
 
 
 public class AerialFaithActivity extends ListActivity
         implements SearchView.OnQueryTextListener, RecognitionListener {
 
     private final static int REQUEST_SPEECH = 1;
     private final static int REQUEST_IMAGE = 2;
     private final static int REQUEST_VIDEO = 4;
 
     private ModuleDispatcher mDispatcher = null;
     private SpeechRecognizer mRecognizer = null;
     private Intent mSpeechIntent = null;
 
     private List<IntentWrapper> results = new ArrayList<IntentWrapper>();
 
     private RelativeLayout header = null;
     private SearchView polybox = null;
     private TextView empty = null;
 
     private boolean listening = false;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.aerial_faith);
 
         initializeSpeechRecognizer();
 
         polybox = (SearchView)findViewById(android.R.id.text1);
         polybox.setOnQueryTextListener(this);
 
         setListAdapter(new ArrayAdapter<IntentWrapper>(this,
                 android.R.layout.simple_list_item_1,
                 android.R.id.text1,
                 results));
     }
 
 
     public void onStart() {
         super.onStart();
         mDispatcher = new ModuleDispatcher(this);
         mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
         mRecognizer.setRecognitionListener(this);
         mSpeechIntent = new Intent(
                 RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
         mSpeechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
     }
 
 
     public void onStop() {
         super.onStop();
         mDispatcher.destroy();
         mDispatcher = null;
         mRecognizer.destroy();
     }
 
 
     @Override
     protected void onResume() {
         super.onResume();
         if(polybox.getQuery().toString().trim().length() > 0) {
             mDispatcher.notifyEnabledModulesChanged();
 
             // This is a terrible hack. If there are many modules, this may not
             // be enough time for the services to bind.
             // FIXME
             new android.os.Handler().postDelayed(new Runnable() {
                 public void run() {
                     AerialFaithActivity.this.onQueryTextChange(
                             AerialFaithActivity.this.polybox.getQuery().toString());
                 }
             }, 100);
         }
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu m) {
         getMenuInflater().inflate(R.menu.main, m);
         return true;
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem mi) {
         if(mi.getItemId() == R.id.menu_txt) {
             polybox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
             polybox.requestFocus();
             ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                     .showSoftInput(polybox, InputMethodManager.SHOW_FORCED);
         }
         else if(mi.getItemId() == R.id.menu_num) {
             polybox.setInputType(InputType.TYPE_CLASS_PHONE);
             polybox.requestFocus();
             ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                     .showSoftInput(polybox, InputMethodManager.SHOW_FORCED);
         }
         else if(mi.getItemId() == R.id.menu_img) {
             Intent cameraIntent = new Intent(
                     MediaStore.ACTION_IMAGE_CAPTURE);
             startActivityForResult(cameraIntent, REQUEST_IMAGE);
         }
         else if(mi.getItemId() == R.id.menu_vid) {
             Intent cameraIntent = new Intent(
                     MediaStore.ACTION_VIDEO_CAPTURE);
             startActivityForResult(cameraIntent, REQUEST_VIDEO);
         }
         else if(mi.getItemId() == R.id.resolve_stalemates) {
             Intent resolveStalematesIntent = new Intent(
                     this, StalemateResolutionActivity.class);
             startActivity(resolveStalematesIntent);
             return true;
         }
         return super.onOptionsItemSelected(mi);
     }
 
 
     @Override
     protected void onActivityResult(int request, int response, Intent data) {
         if(response != Activity.RESULT_OK) {
             return;
         }
 
         if(request == REQUEST_SPEECH) {
             // deprecated. TODO remove.
             ArrayList<String> results = data.getStringArrayListExtra(
                     RecognizerIntent.EXTRA_RESULTS);
             if(results == null || results.size() == 0) {
                 return;
             }
             String bestMatch = results.get(0);
             polybox.setQuery(bestMatch, false);
         }
         else if(request == REQUEST_IMAGE) {
             android.widget.Toast.makeText(this, "NotImplemented (img)",
             android.widget.Toast.LENGTH_SHORT).show();
         }
         else if(request == REQUEST_VIDEO) {
             android.widget.Toast.makeText(this, "NotImplemented (vid)",
             android.widget.Toast.LENGTH_SHORT).show();
         }
     }
 
 
     @Override
     public boolean onQueryTextChange(String query) {
         // TODO implement a host pool to run module queries in parallel.
 
         results.clear();
 
         if(query.trim().length() == 0) {
             ((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
             return true;
         }
 
         Intent moduleQueryIntent = new Intent();
         moduleQueryIntent.putExtra(Module.QUERY_TEXT, query);
 
         List<IntentWrapper> responses = mDispatcher.dispatch(moduleQueryIntent);
         results.addAll(responses);
 
         ((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
         return true;
     }
 
 
     @Override
     public boolean onQueryTextSubmit(String query) {
         return true; // No effect.
     }
 
 
     @Override
     public void onListItemClick(ListView list, View view, int position, long id) {
         IntentWrapper wrapper = (IntentWrapper)
                 getListView().getItemAtPosition(position);
         polybox.setQuery("", false);
         // TODO notify the module that its response was selected?
         startActivity(wrapper.mIntent);
     }
 
 
     private void initializeSpeechRecognizer() {
         empty = (TextView)findViewById(android.R.id.empty);
         empty.setText(getString(R.string.speech_try_again));
         empty.setOnClickListener(
                 new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(!AerialFaithActivity.this.listening)
                     AerialFaithActivity.this.startListening();
                 else
                     AerialFaithActivity.this.stopListening();
             }
         });
     }
 
 
     private void startListening() {
         listening = true;
         mRecognizer.startListening(mSpeechIntent);
     }
 
 
     private void stopListening() {
         listening = false;
         mRecognizer.stopListening();
         empty.setText(getString(R.string.speech_try_again));
     }
 
 
     @Override
     public void onBeginningOfSpeech() {}
     @Override
     public void onBufferReceived(byte[] buffer) {}
     @Override
     public void onEvent(int type, Bundle params) {}
     @Override
     public void onRmsChanged(float rmsdB) {}
 
 
     @Override
     public void onEndOfSpeech() {
         listening = false;
         empty.setText(getString(R.string.speech_try_again));
     }
 
 
     @Override
     public void onError(int error) {
        stopListening();
     }
 
 
     @Override
     public void onPartialResults(Bundle partialResults) {
         onResults(partialResults);
     }
 
 
     @Override
     public void onReadyForSpeech(Bundle params) {
         empty.setText(getString(R.string.speech_listening));
     }
 
 
     @Override
     public void onResults(Bundle results) {
         ArrayList<String> candidates = results.getStringArrayList(
                 SpeechRecognizer.RESULTS_RECOGNITION);
         if(candidates == null || candidates.size() == 0) return;
         polybox.setQuery(candidates.get(0), false);
     }
 
 }
