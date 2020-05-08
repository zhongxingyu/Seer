 /*
  * Der Bund ePaper Downloader - App to download ePaper issues of the Der Bund newspaper
  * Copyright (C) 2013 Adrian Gygax
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see {http://www.gnu.org/licenses/}.
  */
 
 package com.github.notizklotz.derbunddownloader;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.DownloadManager;
 import android.app.LoaderManager;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.content.FileProvider;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.DatePicker;
 import android.widget.GridView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.File;
 import java.util.Calendar;
 
 public class MainActivity extends Activity implements
         LoaderManager.LoaderCallbacks<Cursor> {
 
     private SimpleCursorAdapter issueListAdapter;
 
     private final DownloadManager.Query query = new DownloadManager.Query();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
         GridView gridView = (GridView) findViewById(R.id.gridview);
         gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Cursor selectedIssue = (Cursor) parent.getItemAtPosition(position);
                 if (selectedIssue != null) {
                     String uri = selectedIssue.getString(selectedIssue.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                     openPDF(uri);
                 }
             }
         });
 
         issueListAdapter = new SimpleCursorAdapter(this,
                 R.layout.issue, null,
                 new String[]{DownloadManager.COLUMN_DESCRIPTION, DownloadManager.COLUMN_STATUS},
                 new int[]{R.id.dateTextView, R.id.stateTextView}, 0);
 
         issueListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
             @Override
             public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                 if (view.getId() == R.id.stateTextView) {
                     String statusText = "Unbekannt";
                     int status = cursor.getInt(columnIndex);
                     switch (status) {
                         case DownloadManager.STATUS_SUCCESSFUL:
                             statusText = "Heruntergeladen";
                             break;
                         case DownloadManager.STATUS_PAUSED:
                         case DownloadManager.STATUS_PENDING:
                             statusText = "Warte auf Netzwerkverbindung...";
                             break;
                         case DownloadManager.STATUS_RUNNING:
                             statusText = "Lade herunter...";
                             break;
                         case DownloadManager.STATUS_FAILED:
                             statusText = "Fehler beim herunterladen";
                             break;
                     }
                     ((TextView) view).setText(statusText);
                     return true;
                 }
 
                 return false;
             }
         });
 
         gridView.setAdapter(issueListAdapter);
 
         getLoaderManager().initLoader(1, null, this);
     }
 
     private void openPDF(String uri) {
         Intent intent = new Intent(Intent.ACTION_VIEW);
 
         final Uri publicUri = FileProvider.getUriForFile(this, "com.github.notizklotz.derbunddownloader.publicissues", new File(uri));
         intent.setDataAndType(publicUri, "application/pdf");
         intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
         intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
         startActivity(intent);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_download:
                 new DatePickerFragment().show(getFragmentManager(), "downloadIssueDatePicker");
                 return true;
             case R.id.action_settings:
                 startActivity(new Intent(this, SettingsActivity.class));
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             final Calendar c = Calendar.getInstance();
             Activity activity = getActivity();
             if (activity == null) {
                 throw new IllegalStateException("Activity is null");
             }
 
             DatePickerDialog datePickerDialog = new DatePickerDialog(activity, this,
                     c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
 
             DatePicker datePicker = datePickerDialog.getDatePicker();
             if (datePicker != null) {
                 datePicker.setMaxDate(System.currentTimeMillis());
             }
 
             return datePickerDialog;
         }
 
 
         @Override
         public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
             Activity activity = getActivity();
             if (activity == null) {
                 throw new IllegalStateException("Activity is null");
             }
 
             Calendar selectedDate = Calendar.getInstance();
             //noinspection MagicConstant
             selectedDate.set(year, monthOfYear, dayOfMonth);
             if (selectedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                 Toast.makeText(activity, activity.getString(R.string.error_no_issue_on_sundays), Toast.LENGTH_SHORT).show();
             } else {
                 IssueDownloadService.startDownload(activity, dayOfMonth, monthOfYear + 1, year);
             }
         }
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         return new DownloadManagerLoader(this, query);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         issueListAdapter.changeCursor(data);
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         issueListAdapter.changeCursor(null);
     }
 
 
 }
