 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.dt.fragments.stories;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
 import eu.trentorise.smartcampus.dt.R;
 import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
 import eu.trentorise.smartcampus.dt.fragments.events.POISelectActivity;
 import eu.trentorise.smartcampus.dt.model.POIObject;
 import eu.trentorise.smartcampus.dt.model.StepObject;
 import eu.trentorise.smartcampus.dt.model.StoryObject;
 import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
 
 /*
  * Fragment that manages a single step of a story with the POI and the note
  */
 
 public class AddStepToStoryFragment extends NotificationsSherlockFragmentDT implements OnTagsSelectedListener, TagProvider {
 
 	public static String ARG_STEP_HANDLER = "handler";
 	public static String ARG_STORY_OBJECT = "story";
 	public static String ARG_STEP_POSITION = "position";
 	private View view = null;
 	private POIObject poi = null;
 	private StepObject step = null;
 	private StepHandler stepHandler = null;
 	private StoryObject storyObject = null;
 	private Integer position = null;
 	private static final String TAG = "AddStepToStoryFragment";
 
 	@Override
 	public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
 
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle arg0) {
 		super.onSaveInstanceState(arg0);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment.onCreate ");
 		}
 
 		setHasOptionsMenu(false);
 
 		if (getArguments() != null && getArguments().containsKey(ARG_STEP_HANDLER)
 				&& getArguments().getParcelable(ARG_STEP_HANDLER) != null) {
 			stepHandler = (StepHandler) getArguments().getParcelable(ARG_STEP_HANDLER);
 		}
 		if (getArguments() != null && getArguments().containsKey(ARG_STORY_OBJECT)
 				&& getArguments().getSerializable(ARG_STORY_OBJECT) != null) {
 			storyObject = (StoryObject) getArguments().getSerializable(ARG_STORY_OBJECT);
 
 		}
 		if (getArguments() != null && getArguments().containsKey(ARG_STEP_POSITION)
 				&& getArguments().getSerializable(ARG_STEP_POSITION) != null) {
 			position = getArguments().getInt(ARG_STEP_POSITION);
 
 		}
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment.onCreateView ");
 		}
 		view = inflater.inflate(R.layout.createstepform, container, false);
 
 		// title
 		AutoCompleteTextView poiField = (AutoCompleteTextView) view.findViewById(R.id.step_place);
 		ArrayAdapter<String> stepAdapter = new ArrayAdapter<String>(getSherlockActivity(), R.layout.dd_list, R.id.dd_textview,
 				DTHelper.getAllPOITitles());
 		poiField.setAdapter(stepAdapter);
 
 		// load the poi's title
 		if (poi != null) {
 			poiField.setText(poi.getTitle());
 		} else if ((storyObject != null) && (position != null)) {
 			step = storyObject.getSteps().get(position);
 			poi = step.assignedPoi();
 			if (poi != null)
 				poiField.setText(poi.getTitle());
 
 		}
 
 		// button linked to the map where choose the POIs
 		ImageButton locationBtn = (ImageButton) view.findViewById(R.id.btn_step_locate);
 		locationBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startActivityForResult(new Intent(getActivity(), POISelectActivity.class), POISelectActivity.RESULT_SELECTED);
 			}
 		});
 
 		// note
 		TextView noteField = (TextView) view.findViewById(R.id.step_tags);
 		if (step != null) {
 			noteField.setText(step.getNote());
 		}
 
 		// cancel
 		Button cancel = (Button) view.findViewById(R.id.btn_createstep_cancel);
 		cancel.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				getSherlockActivity().getSupportFragmentManager().popBackStack();
 			}
 
 		});
 
 		// save
 		Button save = (Button) view.findViewById(R.id.btn_createstep_ok);
 		save.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// get the data from the user interface and call the correct
 				// method of the handler
 				// if the step is new or an update
 				if (Log.isLoggable(TAG, Log.VERBOSE)) {
 					Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment click on save button ");
 				}

 				AutoCompleteTextView stepPlace = (AutoCompleteTextView) view.findViewById(R.id.step_place);
 				TextView notePlace = (TextView) view.findViewById(R.id.step_tags);
 
 				if (stepPlace.getText() != null && stepPlace.getText().length() > 0) {
					step = new StepObject(DTHelper.findPOIByTitle(stepPlace.getText().toString()), notePlace.getText()
							.toString());
 					if (stepHandler != null) {
 						if ((storyObject != null) && (position != null))
 							stepHandler.updateStep(step, position);
 						else
 							stepHandler.addStep(step);
 
 					}
 
 				} else {
 					Toast.makeText(getActivity(), R.string.empty_step_title, Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 
 		return view;
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		// hide the keyboard
 		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
 		TextView noteField = (TextView) view.findViewById(R.id.step_tags);
 
 		imm.hideSoftInputFromWindow(noteField.getWindowToken(), 0);
 
 	}
 
 	// get the poi from the choosing POIs activity
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent result) {
 		super.onActivityResult(requestCode, resultCode, result);
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment.onActivityResult ");
 		}
 
 		if (resultCode == POISelectActivity.RESULT_SELECTED) {
 			poi = (POIObject) result.getSerializableExtra("poi");
 			AutoCompleteTextView text = (AutoCompleteTextView) view.findViewById(R.id.step_place);
 			text.setText(poi.getTitle());
 			for (int i = 0; i < text.getAdapter().getCount(); i++) {
 				if (poi.getTitle().equals((text.getAdapter().getItem(i)))) {
 					text.setListSelection(i);
 				}
 			}
 		}
 	}
 
 	@Override
 	public List<SemanticSuggestion> getTags(CharSequence text) {
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment.getTags ");
 		}
 
 		try {
 			return DTHelper.getSuggestions(text);
 		} catch (Exception e) {
 			return Collections.emptyList();
 		}
 	}
 
 	public interface StepHandler {
 		public void addStep(StepObject step);
 
 		public void updateStep(StepObject step, Integer position);
 	}
 
 }
