 package com.ell.MemoRazor.helpers;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import com.ell.MemoRazor.AppSettings;
 import com.ell.MemoRazor.R;
 import com.ell.MemoRazor.data.DatabaseHelper;
 import com.ell.MemoRazor.data.Word;
 import com.ell.MemoRazor.data.WordGroup;
 import com.ell.MemoRazor.translators.YandexOpenJSONTranslator;
 
 import java.sql.SQLException;
 
 public class BulkTranslationDialogHelper {
     public static void bulkTranslateGroup(final Context context, final DatabaseHelper databaseHelper, final WordGroup wordGroup) {
         if (!NetworkHelper.isNetworkOnline(context)) {
             if (!NetworkHelper.isNetworkOnline(context)) {
                 DialogHelper.messageBox(context, context.getResources().getString(R.string.no_network_available));
                 return;
             }
         }
 
         final ProgressDialog progressDialog = new ProgressDialog(context);
         progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         progressDialog.setMessage(context.getResources().getString(R.string.initializing));
         progressDialog.setTitle(context.getResources().getString(R.string.app_name));
         progressDialog.setCancelable(true);
         progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getText(R.string.common_cancel), new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });
 
         final AsyncTask<WordGroup, Pair<String, Integer>, WordGroup> task = new AsyncTask<WordGroup, Pair<String, Integer>, WordGroup>() {
             @Override
             protected WordGroup doInBackground(WordGroup... wordGroups) {
                 try {
                     YandexOpenJSONTranslator translator = new YandexOpenJSONTranslator();
 
                     WordGroup groupToProcess = wordGroups[0];
                     final String nativeLanguage = AppSettings.getFirstLanguage();
 
 
                     int i = 1;
                     int totalWords = groupToProcess.getWords().size();
                     progressDialog.setMax(totalWords);
                     for (Word currentWord : groupToProcess.getWords()) {
                         publishProgress(new Pair(String.format(context.getString(R.string.bulk_checking_word), currentWord.getName()), i));
 
                         if (isCancelled())
                             break;
 
                         if (currentWord.getMeaning() == null || currentWord.getMeaning().equals("")) {
                             publishProgress(new Pair(String.format(context.getString(R.string.bulk_translation_progress), currentWord.getName()), i));
                             currentWord.setFetchingTranslation(true);
                             translator.translateWord(currentWord, currentWord.getLanguage(), nativeLanguage);
                             currentWord.setFetchingTranslation(false);
                             try {
                                 databaseHelper.getWordDao().update(currentWord);
                             } catch (SQLException e) {
                                 e.printStackTrace();
                             }
                         }
 
                         if (!databaseHelper.isPlaybackCached(currentWord)) {
                             publishProgress(new Pair(String.format(context.getString(R.string.bulk_playback_fetch_progress), currentWord.getName()), i));
                             WordPlaybackManager.cacheWordPlayback(databaseHelper, currentWord);
                         }
 
                         i++;
                     }
 
                     return groupToProcess;
                 }
                 catch (Exception e) {
                     e.printStackTrace();
 
                     return null;
                 }
             }
 
             @Override
             protected void onPostExecute(WordGroup processedGroup) {
                 if (processedGroup != null) {
                     progressDialog.setProgress(progressDialog.getMax());
                     progressDialog.setMessage(context.getString(R.string.finished));
                 } else {
                     progressDialog.setProgress(progressDialog.getMax());
                     progressDialog.setMessage(context.getString(R.string.error_occurred));
                 }
 
                 Button button = progressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
                 button.setText(context.getResources().getText(R.string.common_ok));
                 button.invalidate();
             }
 
             @Override
             protected void onProgressUpdate(Pair<String, Integer>... values) {
                 Pair<String, Integer> dataTuple = values[0];
 
                 progressDialog.setMessage(dataTuple.getLeft());
                 progressDialog.setProgress(dataTuple.getRight());
             }
         };
 
         progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
             public void onCancel(DialogInterface dialog) {
                 task.cancel(true);
             }
         });
 
         progressDialog.show();
         task.execute(wordGroup);
     }
 }
