 /*******************************************************************************
  * Nof1 Trails helper, making life easier for clinicians and patients in N of 1 trials.
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
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package uk.co.jwlawson.nof1.activities;
 
 import java.util.ArrayList;
 
 import uk.co.jwlawson.nof1.R;
 import uk.co.jwlawson.nof1.containers.Question;
 import uk.co.jwlawson.nof1.fragments.FormBuilderList;
 import uk.co.jwlawson.nof1.fragments.QuestionBuilderFragment;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 /**
  * @author John Lawson
  * 
  */
 public class FormBuilder1 extends SherlockFragmentActivity implements FormBuilderList.OnListItemSelectedListener,
 		QuestionBuilderFragment.OnQuestionEditedListener {
 
 	private static final String TAG = "FormBuilder1";
 	private static final boolean DEBUG = true;
 
 	/** ListFragment */
 	private FormBuilderList mList;
 
 	/** true when the actionmode is active */
 	private boolean mInActionMode = false;
 
 	/** True if using dual pane layout */
 	private boolean mDualPane;
 
	/** Tracks the selected ietm in the ListFragment */
 	private int mListSelected = -1;
 
 	/** List of questions passed on to ListFragment */
 	private ArrayList<Question> mQuestionList;
 
 	/** Currently selected or last instanced QuestionBuilder */
 	private QuestionBuilderFragment mQuestionBuilder;
 
 	public FormBuilder1() {
 		mQuestionList = new ArrayList<Question>();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Load layout. This will change depending on the screen size
 		setContentView(R.layout.form_builder_list);
 
 		// Find list fragment
 		mList = (FormBuilderList) getSupportFragmentManager().findFragmentById(R.id.form_builder_list_fragment);
 
 		// Fill mQuestionList and set as the array for mList
 		for (int i = 0; i < 5; i++) {
 			Question q = new Question(Question.SCALE, "Question " + i);
 			mQuestionList.add(q);
 		}
 		for (int i = 0; i < 5; i++) {
 			Question q = new Question(Question.NUMBER, "Question " + i);
 			mQuestionList.add(q);
 		}
 		for (int i = 0; i < 5; i++) {
 			Question q = new Question(Question.CHECK, "Question " + i);
 			mQuestionList.add(q);
 		}
 		mList.setArrayList(mQuestionList);
 
 		// See if the layout has a frame for the second pane.
 		FrameLayout frame = (FrameLayout) findViewById(R.id.form_builder_details_frame);
 		mDualPane = (frame != null) && (frame.getVisibility() == View.VISIBLE);
 		if (DEBUG) Log.d(TAG, "DualPane = " + mDualPane);
 
 		if (savedInstanceState != null) {
			mListSelected = savedInstanceState.getInt(TAG + "listSelection");

			if (mDualPane) {
				mList.setSelection(mListSelected);
			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.form_builder_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_form_builder_add:
 			// Add new question to bottom of the list and load new QuestionBuilder
 			Question q = new Question(Question.SCALE);
 			mQuestionList.add(q);
 			((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
			mListSelected = mQuestionList.indexOf(q);
 			edit(mListSelected);
 			return true;
 		case R.id.menu_form_builder_preview:
 			// Build and preview a questionnaire
 			return true;
 		default:
 			return super.onMenuItemSelected(featureId, item);
 		}
 	}
 
 	@Override
 	public void onListItemSelected(ListView l, View v, int position, long id) {
 		mListSelected = position;
 		if (!mInActionMode) {
 			startActionMode(new ListActionMode());
 		}
 		if (mDualPane) {
 			edit(position);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt(TAG + "listSelection", mListSelected);
 	}
 
 	/** Show the selected question in some editable form */
 	private void edit(int selection) {
 		QuestionBuilderFragment q;
 		if (mDualPane) {
 
 			q = QuestionBuilderFragment.newInstance(QuestionBuilderFragment.VIEW, mQuestionList.get(selection));
 			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 			ft.replace(R.id.form_builder_details_frame, q, "view");
 			ft.commit();
 
 		} else {
 			q = QuestionBuilderFragment.newInstance(QuestionBuilderFragment.DIALOG, mQuestionList.get(selection));
 
 			// Shows fragment as dialog
 			q.show(getSupportFragmentManager(), "dialog");
 		}
 		mQuestionBuilder = q;
 	}
 
 	/**
 	 * ActionMode class to show when a list item is selected. Provides more
 	 * options specific for that item.
 	 */
 	private class ListActionMode implements ActionMode.Callback {
 
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			getSupportMenuInflater().inflate(R.menu.form_builder_action_mode, menu);
 			if (mDualPane) {
 				menu.removeItem(R.id.action_mode_form_builder_edit);
 				getSupportMenuInflater().inflate(R.menu.menu_config_questions, menu);
 			}
 			mInActionMode = true;
 			return true;
 		}
 
 		@Override
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			return false;
 		}
 
 		@Override
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 
 			switch (item.getItemId()) {
 			case R.id.action_mode_form_builder_edit:
 				edit(mListSelected);
 				break;
 			case R.id.action_mode_form_builder_delete:
 				mQuestionList.remove(mListSelected);
 				((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
 				break;
 			case R.id.action_mode_form_builder_move_up:
 				Question q = mQuestionList.get(mListSelected);
 				mQuestionList.remove(mListSelected);
 				mQuestionList.add(mListSelected - 1, q);
 				((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
 				break;
 			case R.id.action_mode_form_builder_move_down:
 				Question q1 = mQuestionList.get(mListSelected);
 				mQuestionList.remove(mListSelected);
 				mQuestionList.add(mListSelected + 1, q1);
 				((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
 				break;
 			case R.id.menu_config_questions_save:
 				mQuestionBuilder.save();
 				break;
 
 			}
 			mode.finish();
 			return true;
 		}
 
 		@Override
 		public void onDestroyActionMode(ActionMode mode) {
 			if (DEBUG) Log.d(TAG, "Actionmode closed");
 			if (!mDualPane) mList.clearSelected();
 			mInActionMode = false;
 		}
 
 	}
 
 	@Override
 	public void onQuestionEdited(Question question) {
 		mQuestionList.remove(mListSelected);
 		mQuestionList.add(mListSelected, question);
 		((ArrayAdapter) mList.getListAdapter()).notifyDataSetChanged();
 	}
 
 }
