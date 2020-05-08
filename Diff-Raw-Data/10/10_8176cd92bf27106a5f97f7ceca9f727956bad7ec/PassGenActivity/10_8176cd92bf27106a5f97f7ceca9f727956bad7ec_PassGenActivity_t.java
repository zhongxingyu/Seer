 package ru.synthet.synthpass;
 /*
  * Copyright 2013 Vladimir Synthet
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.style.ForegroundColorSpan;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.inputmethod.EditorInfo;
 import android.widget.*;
 import ru.synthet.synthpass.synthkeyboard.KeyboardData;
 import ru.synthet.synthpass.synthkeyboard.KeyboardDataBuilder;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class PassGenActivity extends Activity {
 
     private volatile TextView message;
     private volatile ProgressBar progressBar;
     private volatile Button genButton;
     private EditText editMasterPassword;
     private AutoCompleteTextView editDomainName;
     private String masterPassword;
     private String domainName;
     private boolean saveDomains = true;
     private final PassGenerator passGenerator = new PassGenerator();
     private String currentMessage;
     private List<String> domainsList = new ArrayList<String>();
     private ArrayAdapter<String> adapter;
     private static final String domainNamePattern = "^[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
     private Pattern domainPattern;
     private Matcher domainMatcher;
     private Thread generationThread;
     private volatile String resultString = "password";
     private static volatile boolean isThreadRunning = false;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         initializeWindow();
         initializeApp();
 
         if (savedInstanceState != null) {
             masterPassword = savedInstanceState.getString("andServant");
             domainName     = savedInstanceState.getString("domainName");
             currentMessage = savedInstanceState.getString("currentMessage");
             updateView();
         }
     }
 
     protected void onResume() {
         super.onResume();
         getPrefs();
     }
 
     private void getPrefs() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         saveDomains = prefs.getBoolean("cbSaveDomains", true);
         PassGenerator.PassRules.requireSpecialSymbols   = prefs.getBoolean("cbReqSpecial", true);
         PassGenerator.PassRules.requireDigits           = prefs.getBoolean("cbReqDigits", true);
         PassGenerator.PassRules.generatedPasswordLength = prefs.getInt("count", 12);
         PassGenerator.PassRules.requireUppercaseLetters = prefs.getBoolean("cbUpperCase", true);
         PassGenerator.PassRules.requireLowercaseLetters = prefs.getBoolean("cbLowerCase", true);
         PassGenerator.algorithm                         = prefs.getString("lbAlgorithm", "vpass3");
         //Toast.makeText(PassGenActivity.this, PassGenerator.algorithm, Toast.LENGTH_SHORT).show();
         domainsList.clear();
         domainsList.addAll(Arrays.asList(loadArray()));
         adapter = new ArrayAdapter<String>(PassGenActivity.this, android.R.layout.simple_dropdown_item_1line, domainsList);
         editDomainName.setAdapter(adapter);
     }
 
     private String[] loadArray() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         int size = prefs.getInt("domainsList" + "_size", 0);
         String array[] = new String[size];
         for(int i=0;i<size;i++)
             array[i] = prefs.getString("domainsList" + "_" + i, null);
         return array;
     }
 
     private boolean saveArray(String[] array) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         SharedPreferences.Editor editor = prefs.edit();
         editor.putInt("domainsList" +"_size", array.length);
         for(int i=0;i<array.length;i++)
             editor.putString("domainsList" + "_" + i, array[i]);
         return editor.commit();
     }
 
     private void initializeWindow() {
         //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
         setContentView(R.layout.pass_gen);
     }
 
     private void initializeApp() {
         // инициализация элементов
         genButton          = (Button) findViewById(R.id.button);
         message            = (TextView) findViewById(R.id.messsage);
         editMasterPassword = (EditText) findViewById(R.id.editText);
         editDomainName     = (AutoCompleteTextView) findViewById(R.id.domainSearch);
         progressBar        = (ProgressBar) findViewById(R.id.progressBar);
 
         // обработчик поля ввода editDomainName
         domainPattern = Pattern.compile(domainNamePattern);
         EditText.OnEditorActionListener editorActionListener = new EditText.OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                 int eventCode = 0;
                 if (keyEvent != null) eventCode = keyEvent.getKeyCode();
                 if ((i == EditorInfo.IME_ACTION_DONE) || (eventCode == KeyEvent.KEYCODE_ENTER)) {
                     masterPassword = editMasterPassword.getText().toString();
                     if (masterPassword.length() == 0) return false;
                     domainName     = editDomainName.getText().toString();
                     domainName     = domainName.trim();
                     domainMatcher  = domainPattern.matcher(domainName);
                     if ((!domainsList.contains(domainName)) && (domainMatcher.matches()) && (saveDomains)) {
                         domainsList.add(domainName);
                         adapter = new ArrayAdapter<String>(PassGenActivity.this, android.R.layout.simple_dropdown_item_1line, domainsList);
                         editDomainName.setAdapter(adapter);
                         saveArray(domainsList.toArray(new String[domainsList.size()]));
                     }
                     touchGenButton();
                 }
                 return false;
             }
         };
         editDomainName.setOnEditorActionListener(editorActionListener);
 
         // обработчик кнопки genButton
         View.OnClickListener genTapListener = new View.OnClickListener() {
             public void onClick(View v) {
                 masterPassword = editMasterPassword.getText().toString();
                 domainName     = editDomainName.getText().toString();
                 editMasterPassword.setText("");
                 editDomainName.setText("");
                editMasterPassword.clearFocus();
                editDomainName.clearFocus();
                 touchGenButton();
             }
         };
         genButton.setOnClickListener(genTapListener);
     }
 
     private void touchGenButton() {
 
         if ((isThreadRunning) && (generationThread !=  null)) {
             generationThread.interrupt();
             return;
         }
         if ((isThreadRunning) || (masterPassword.length() == 0)) return;
         generationThread = new Thread(genThread);
         generationThread.start();
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
         genButton.setText(R.string.stop);
     }
 
     private final Runnable genThread = new Runnable() {
 
         private void vPass() {
             String inputString = masterPassword + domainName;
             do {
                 resultString = passGenerator.encrypt(inputString,
                         PassGenerator.PassRules.generatedPasswordLength);
                 inputString += '.';
                 if (generationThread.isInterrupted()) {
                     break;
                 }
             } while (!passGenerator.validate(resultString));
         }
 
         private void synthPass() {
             String inputString = masterPassword + domainName;
             do {
                 resultString = passGenerator.synthEncrypt(inputString,
                         PassGenerator.PassRules.generatedPasswordLength);
                 inputString = resultString;
                 if (generationThread.isInterrupted()) {
                     break;
                 }
             } while (!passGenerator.validate(resultString));
         }
 
         @Override
         public void run() {
             isThreadRunning = true;
             Message msg = handler.obtainMessage();
             Bundle b = new Bundle();
             resultString = getString(R.string.password);
             if (((!PassGenerator.PassRules.requireUppercaseLetters) &&
                     (!PassGenerator.PassRules.requireLowercaseLetters) &&
                     (!PassGenerator.PassRules.requireSpecialSymbols) &&
                     (!PassGenerator.PassRules.requireDigits))
                     || (masterPassword.length() == 0)) {
                 isThreadRunning = false;
                 handler.sendMessage(msg);
                 return;
             }
             if (domainName == null)
                 KeyboardData.entryName = null;
             else
                 KeyboardData.entryName = domainName;
             if (PassGenerator.algorithm.equals("vpass3"))
                 vPass();
             else if (PassGenerator.algorithm.equals("synthpass1")) {
                 synthPass();
             }
             b.putString("resultString", resultString);
             msg.setData(b);
             handler.sendMessage(msg);
             isThreadRunning = false;
         }
         final Handler handler = new Handler() {
             @Override
             public void handleMessage(Message msg) {
                 Bundle b         = msg.getData();
                 String keyString = b.getString("resultString");
                 if (keyString != null) {
                     returnPassword(keyString);
                     showDecoratedPassword(keyString, message);
                 }
                if (progressBar != null)
                    progressBar.setVisibility(View.INVISIBLE);
                 genButton.setText(R.string.generate);
             }
         };
     };
 
     void returnPassword(String keyString) {
         KeyboardDataBuilder kbdataBuilder = new KeyboardDataBuilder();
         kbdataBuilder.addPair(getString(R.string.last_password), keyString);
         kbdataBuilder.commit();
         Intent intent = getIntent();
         String id = intent.getStringExtra("SYNTHPASS_APPTASK");
         if (id != null) {
             if (id.equals("GENERATE")) {
                 Log.v("DEBUG", id);
                 finish();
             }
         }
     }
 
     static void showDecoratedPassword(String inputString, TextView textView) {
         SpannableString spannableResultString = new SpannableString(inputString);
         for(int i=0; i<inputString.length(); i++) {
             if (!Character.isLetterOrDigit(inputString.charAt(i))) {
                 spannableResultString.setSpan(new ForegroundColorSpan(Color.parseColor("#8093FF")),
                         i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             } else if (Character.isDigit(inputString.charAt(i))) {
                 spannableResultString.setSpan(new ForegroundColorSpan(Color.parseColor("#80FF93")),
                         i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             }
         }
         textView.setText(spannableResultString, TextView.BufferType.SPANNABLE);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putString("andServant", editMasterPassword.getText().toString());
         outState.putString("domainName", editDomainName.getText().toString());
         outState.putString("currentMessage", message.getText().toString());
     }
 
     private void updateView() {
         editMasterPassword.setText(masterPassword);
         editDomainName.setText(domainName);
         message.setText(currentMessage);
     }
 }
