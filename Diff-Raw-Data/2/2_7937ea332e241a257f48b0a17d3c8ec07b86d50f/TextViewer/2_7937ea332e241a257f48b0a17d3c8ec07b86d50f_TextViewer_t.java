 /*
  * Copyright (c) 2013 esprit <alex.esprit@gmail.com>
  *
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 
 package app.android.textviewer;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 /**
  * @author esprit
  */
 public final class TextViewer extends Activity {
     private ScrollView scrollView;
     private TextView textView;
 
     @Override
     protected void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         setContentView(R.layout.textviewer);
 
         scrollView = (ScrollView)findViewById(R.id.scroll_view);
         textView = (TextView)findViewById(R.id.text_view);
 
         openFile(getIntent());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.textviewer_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.scroll_to_top:
                 scrollView.fullScroll(ScrollView.FOCUS_UP);
                 return true;
             case R.id.scroll_to_bottom:
                 scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private void openFile(Intent intent) {
         String action = intent.getAction();
         if (Intent.ACTION_VIEW.equals(action)) {
             String path = intent.getData().getPath();
             new OpenFileTask().execute(path);
         }
     }
 
     private void updateView(String title, String text) {
         setTitle(title);
         textView.setText(text);
     }
 
     private void showError(String path) {
         String error = String.format(getString(R.string.cant_open), path);
         Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
     }
 
     private class OpenFileTask extends AsyncTask<String, Void, String[]> {
         private ProgressDialog dialog = new ProgressDialog(TextViewer.this);
 
         @Override
         protected String[] doInBackground(String... param) {
             String path = param[0];
             String content = getFileContent(path);
             return new String[]{path, content};
         }
 
         @Override
         protected void onPreExecute() {
             dialog.setTitle(R.string.app_name);
             dialog.setMessage(getString(R.string.opening));
             dialog.show();
         }
 
         @Override
         protected void onPostExecute(String[] param) {
             String path = param[0];
             String content = param[1];
             if (null != content) {
                 updateView(path, content);
             } else {
                 showError(path);
             }
             dialog.dismiss();
         }
 
         private String getFileContent(String path) {
             if (null == path) {
                 return null;
             }
             File file = new File(path);
             if (file.exists()) {
                 String str;
                 StringBuilder sb = new StringBuilder();
                 try {
                     BufferedReader reader = new BufferedReader(new FileReader(file));
                     while ((str = reader.readLine()) != null) {
                         sb.append(str).append('\n');
                     }
                     reader.close();
                     return sb.toString();
                } catch (IOException ignored) {
                 }
             }
             return null;
         }
     }
 }
