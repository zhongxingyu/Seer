 /*
  This file is part of QuizReader.
 
  QuizReader is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  QuizReader is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with QuizReader.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.quizreader.android;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.quizreader.android.database.QRDatabaseHelper;
 import org.quizreader.android.database.WordDao;
 import org.quizreader.android.qzz.FileUtil;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 
 public class BackupWordsRestoreTask extends AsyncTask<Void, String, Integer> {
 
 	private ProgressDialog dialog;
 	private QRDatabaseHelper databaseHelper;
 
 	public BackupWordsRestoreTask(Context context) {
 		databaseHelper = new QRDatabaseHelper(context);
 
 		dialog = new ProgressDialog(context) {
 			@Override
 			public void onBackPressed() {
 				BackupWordsRestoreTask.this.cancel(true);
 				super.onBackPressed();
 			}
 		};
 		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 	}
 
 	@Override
 	protected void onPreExecute() {
 		this.dialog.setMessage("Restoring Saved Progress...");
 		this.dialog.show();
 	}
 
 	@Override
 	protected Integer doInBackground(Void... nothing) {
 		int wordCounter = 0;
 		BufferedReader saveReader = null;
 
 		SQLiteDatabase db = databaseHelper.getWritableDatabase();
 		db.beginTransaction();
 
 		try {
 			// try to open the storage
			File saveFile = new File(FileUtil.getDownloadDir(), BackupWordsTask.SAVE_FILE_NAME);
 			saveReader = new BufferedReader(new FileReader(saveFile));
 			// get all words from db
 			String line = saveReader.readLine();
 			while (line != null) {
 				String[] split = line.split("\t");
 				int quizLevel = Integer.parseInt(split[2]);
 				long wordId = WordDao.insertOrGetWordId(db, split[0], split[1], quizLevel);
 				publishProgress("Restoring " + (wordCounter++) + " words from backup");
 				line = saveReader.readLine();
 			}
 			db.setTransactionSuccessful();
 		} catch (Exception e) {
 			publishProgress(e.getClass() + ": " + e.getLocalizedMessage());
 			e.printStackTrace();
 		} finally {
 			db.endTransaction();
 			db.close();
 			if (saveReader != null) {
 				try {
 					saveReader.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		return wordCounter;
 	}
 
 	@Override
 	protected void onProgressUpdate(String... values) {
 		for (String value : values) {
 			dialog.setMessage(value);
 		}
 	}
 
 	@Override
 	protected void onPostExecute(Integer result) {
 		if (dialog.isShowing()) {
 			dialog.dismiss();
 		}
 	}
 
 }
