 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  WMG, University of Warwick
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at 
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package uk.co.jwlawson.nof1.activities;
 
 import java.util.ArrayList;
 
 import uk.co.jwlawson.nof1.R;
 import uk.co.jwlawson.nof1.containers.Question;
 import uk.co.jwlawson.nof1.fragments.CheckFragment;
 import uk.co.jwlawson.nof1.fragments.NumberFragment;
 import uk.co.jwlawson.nof1.fragments.QuestionFragment;
 import uk.co.jwlawson.nof1.fragments.RadioFragment;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Window;
 
 /**
  * Activity to load and show the questionnaire presented to the patient. Can be run in preview mode, so the doctor can
  * see what the questionnaire will look like without risking saving any data.
  * 
  * @author John Lawson
  * 
  */
 public class Questionnaire extends SherlockFragmentActivity {
 
 	private static final String TAG = "Questionnaire";
 	private static final boolean DEBUG = true;
 
 	public static final int RESULT_DONE = 10;
 	public static final int RESULT_BACK = 11;
 
 	/** List of all questions included in questionnaire */
 	private ArrayList<QuestionFragment> mQuestionList;
 
 	/** True if in preview mode: no details saved */
 	private boolean mPreview;
 
 	public Questionnaire() {
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.mock_layout_data_input);
 
		setSupportProgressBarIndeterminateVisibility(false);
 
 		mQuestionList = new ArrayList<QuestionFragment>();
 
 		if (savedInstanceState == null) {
 			// Need to load fragments
 			new Loader().execute();
 		}
 
 		Intent i = getIntent();
 		mPreview = i.getBooleanExtra(FormBuilder.INTENT_PREVIEW, false);
 
 		Button btnOk = (Button) findViewById(R.id.data_input_button_ok);
 		btnOk.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				save();
 			}
 		});
 
 		Button btnCan = (Button) findViewById(R.id.data_input_button_cancel);
 		btnCan.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				cancel();
 			}
 		});
 
 		if (mPreview) {
 			Log.d(TAG, "Building questionnaire in preview mode");
 			btnOk.setText(R.string.save);
 			btnCan.setText(R.string.cancel);
 			Toast.makeText(this, R.string.questionnaire_preview_explain, Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private void save() {
 		if (mPreview) {
 			// Save the questionnaire and return to DoctorConfig
 			// Well, questions are already saved to SharedPreferenced, so just return
 			setResult(RESULT_DONE);
 			finish();
 		} else {
 			// TODO Save data to database
 		}
 	}
 
 	private void cancel() {
 		if (mPreview) {
 			// Return to FormBuilder
 			setResult(RESULT_BACK);
 			finish();
 		} else {
 			// TODO reschedule
 		}
 	}
 
 	private class Loader extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
			if (DEBUG) Log.d(TAG, "AsyncTask Loader started");
			setSupportProgressBarIndeterminateVisibility(true);
 
 			SharedPreferences sp = getSharedPreferences(FormBuilder.PREFS_NAME, MODE_PRIVATE);
 
 			// Check whether the device is large enough for 2 columns
 			boolean dualCol = (findViewById(R.id.data_input_fragment_layout2) != null);
			if (dualCol) Log.d(TAG, "Dual columns found");
 			int column = 0;
 
			// Go through question shared preference and extract each question
 			for (int i = 0; sp.contains(FormBuilder.PREFS_TEXT + i); i++) {
 
 				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 
 				QuestionFragment q;
 
 				int inputType = sp.getInt(FormBuilder.PREFS_TYPE + i, Question.SCALE);
 
				// Make QuestionFragment
 				switch (inputType) {
 				case Question.SCALE:
 					q = RadioFragment.newInstance(sp.getString(FormBuilder.PREFS_TEXT + i, null), sp.getString(FormBuilder.PREFS_MIN + i, null),
 							sp.getString(FormBuilder.PREFS_MAX + i, null));
 					break;
 				case Question.NUMBER:
 					q = NumberFragment.newInstance(sp.getString(FormBuilder.PREFS_TEXT + i, null));
 					break;
 				case Question.CHECK:
 					q = CheckFragment.newInstance(sp.getString(FormBuilder.PREFS_TEXT + i, null), false);
 					break;
 				default:
 					// Should never happen
 					q = null;
 					Log.e(TAG, "Something horrid has happened: Unknown question type");
 				}
 				mQuestionList.add(q);
 
 				// Add the questionFragment to the layout.
 				switch (column) {
 				case 0:
 					ft.add(R.id.data_input_fragment_layout, q, "quesFrag" + i);
 					break;
 				case 1:
 					ft.add(R.id.data_input_fragment_layout2, q, "quesFrag" + i);
 					break;
 				}
 				if (dualCol) {
 					column++;
 					column %= 2;
 				}
 
 				ft.commit();
 
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
			if (DEBUG) Log.d(TAG, "AsyncTask Loader finished");
			setSupportProgressBarIndeterminateVisibility(false);
 		}
 
 	}
 }
