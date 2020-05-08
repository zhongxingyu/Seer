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
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import org.quizreader.android.database.Word;
 import org.quizreader.android.database.WordDao;
 import org.quizreader.android.qzz.FileUtil;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 
 public class BackupWordsTask extends AsyncTask<Void, String, Integer> {
 
	private static final String SAVE_FILE_NAME = "words.dat";
 	private ProgressDialog dialog;
 	private WordDao wordDao;
 
 	public BackupWordsTask(Context context) {
 		wordDao = new WordDao(context);
 
 		dialog = new ProgressDialog(context) {
 			@Override
 			public void onBackPressed() {
 				BackupWordsTask.this.cancel(true);
 				super.onBackPressed();
 			}
 		};
 		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 	}
 
 	@Override
 	protected void onPreExecute() {
 		this.dialog.setMessage("Saving Progress...");
 		this.dialog.show();
 	}
 
 	@Override
 	protected Integer doInBackground(Void... nothing) {
 		int wordCounter = 0;
 		FileWriter saveWriter = null;
 		try {
 			// try to open some storage
 			File saveFile = new File(FileUtil.getDownloadDir(), SAVE_FILE_NAME);
 			saveWriter = new FileWriter(saveFile);
 			// get all words from db
 			wordDao.open();
 			publishProgress("Seeking words in database");
 			List<Word> allWords = wordDao.getLearnedWords();
 			wordDao.close();
 			for (Word word : allWords) {
 				saveWord(saveWriter, word);
 				publishProgress("Saved " + wordCounter++ + " words");
 			}
 		} catch (Exception e) {
 			publishProgress(e.getClass() + ": " + e.getLocalizedMessage());
 			e.printStackTrace();
 		} finally {
 			if (saveWriter != null) {
 				try {
 					saveWriter.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		return wordCounter;
 	}
 
 	private void saveWord(FileWriter writer, Word word) throws IOException {
 		writer.write(word.getLanguage());
 		writer.write('\t');
 		writer.write(word.getToken());
 		writer.write('\t');
 		writer.write(Integer.toString(word.getQuizLevel()));
 		writer.write('\n');
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
