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
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
 import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
 import eu.trentorise.smartcampus.dt.R;
 import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
 import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
 import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
 import eu.trentorise.smartcampus.dt.custom.StepAdapter;
 import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
 import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
 import eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment.StepHandler;
 import eu.trentorise.smartcampus.dt.model.CommunityData;
 import eu.trentorise.smartcampus.dt.model.Concept;
 import eu.trentorise.smartcampus.dt.model.StepObject;
 import eu.trentorise.smartcampus.dt.model.StoryObject;
 import eu.trentorise.smartcampus.dt.model.UserStoryObject;
 import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 
 /*
  * Fragment for the creation of the story: title, description, category, tags and the set of steps
  */
 
 public class CreateStoryFragment extends NotificationsSherlockFragmentDT implements OnTagsSelectedListener, TagProvider {
 
 	public static String ARG_STORY = "story";
 	private View view = null;
 	private ListView list = null;
 	private StoryObject storyObject = null;
 	private FragmentManager fragmentManager;
 	private Context context;
 	private StepAdapter stepAdapter;
 	private AddStep stepHandler = new AddStep();
 	private static final String TAG = "CreateStoryFragment";
 
 	private CategoryDescriptor[] categoryDescriptors;
 
 	@Override
 	public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment.onTagsSelected ");
 		}
 
 		storyObject.getCommunityData().setTags(Concept.convertSS(suggestions));
 		((EditText) getView().findViewById(R.id.story_tags)).setText(Concept.toSimpleString(storyObject.getCommunityData()
 				.getTags()));
 
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle arg0) {
 		super.onSaveInstanceState(arg0);
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment.onSaveInstanceState ");
 		}
 
 		arg0.putSerializable(ARG_STORY, storyObject);
 	}
 
 	/*
 	 * on create load the story from the arguments if it is present. In this
 	 * case the user has choosen to edit a story. If the arg is empty, this is a
 	 * new story and the fields should be empty
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment.onCreate ");
 		}
 
 		setHasOptionsMenu(false);
 		fragmentManager = getSherlockActivity().getSupportFragmentManager();
 		context = this.getSherlockActivity();
 
 		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_STORY)
 				&& savedInstanceState.getSerializable(ARG_STORY) != null) {
 			storyObject = (StoryObject) savedInstanceState.get(ARG_STORY);
 		} else if (getArguments() != null && getArguments().containsKey(ARG_STORY)
 				&& getArguments().getSerializable(ARG_STORY) != null) {
 			storyObject = (StoryObject) getArguments().getSerializable(ARG_STORY);
 		} else {
 			storyObject = new UserStoryObject();
 			if (getArguments() != null && getArguments().containsKey(SearchFragment.ARG_CATEGORY)) {
 				storyObject.setType(getArguments().getString(SearchFragment.ARG_CATEGORY));
 			}
 		}
 		if (storyObject.getCommunityData() == null)
 			storyObject.setCommunityData(new CommunityData());
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment.onCreateView ");
 		}
 
 		view = inflater.inflate(R.layout.createstoryform, container, false);
 
 		// list of step with the header (title,description,category and tags)
 		// and the footer (buttons)
 		list = (ListView) view.findViewById(R.id.steps_list);
 		View headerView = View.inflate(context, R.layout.createstoryheader, null);
 		list.addHeaderView(headerView);
 		View footerView = View.inflate(context, R.layout.createstoryfooter, null);
 		list.addFooterView(footerView);
 		stepAdapter = new StepAdapter(context, R.layout.steps_row, storyObject.getSteps(), storyObject, fragmentManager,
 				getActivity());
 		list.setAdapter(stepAdapter);
 
 		// categories
 		categoryDescriptors = CategoryHelper.getStoryCategoryDescriptorsFiltered();
 
 		Spinner categories = (Spinner) view.findViewById(R.id.story_category);
 		int selected = 0;
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.dd_list, R.id.dd_textview);
 		categories.setAdapter(adapter);
 		for (int i = 0; i < categoryDescriptors.length; i++) {
 			adapter.add(getSherlockActivity().getApplicationContext().getResources()
 					.getString(categoryDescriptors[i].description));
 			if (categoryDescriptors[i].category.equals(storyObject.getType())) {
 				selected = i;
 			}
 		}
 		categories.setSelection(selected);
 
 		// title
 		EditText title = (EditText) view.findViewById(R.id.story_title);
 		title.setText(storyObject.getTitle());
 
 		// description
 		EditText description = (EditText) view.findViewById(R.id.story_description);
 		description.setText(storyObject.getDescription());
 
 		// tags
 		EditText tagsEdit = (EditText) view.findViewById(R.id.story_tags);
 		tagsEdit.setText(Concept.toSimpleString(storyObject.getCommunityData().getTags()));
 		tagsEdit.setClickable(true);
 		tagsEdit.setFocusableInTouchMode(false);
 		tagsEdit.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				TaggingDialog taggingDialog = new TaggingDialog(getActivity(), CreateStoryFragment.this,
 						CreateStoryFragment.this, Concept.convertToSS(storyObject.getCommunityData().getTags()));
 				taggingDialog.show();
 			}
 		});
 
 		Button addStep = (Button) view.findViewById(R.id.btn_createstory_addstep);
 		addStep.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				if (Log.isLoggable(TAG, Log.VERBOSE)) {
 					Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment clicked on save button ");
 				}
 
 				// load the new fragment passing the handler manages the step
 				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 				AddStepToStoryFragment fragment = new AddStepToStoryFragment();
 				Bundle args = new Bundle();
 				args.putParcelable(AddStepToStoryFragment.ARG_STEP_HANDLER, stepHandler);
 				fragment.setArguments(args);
 				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 				fragmentTransaction.replace(android.R.id.content, fragment, "stories");
 				fragmentTransaction.addToBackStack(fragment.getTag());
 				fragmentTransaction.commit();
 			}
 
 		});
 
 		// cannot edit title, categories, description for non-owned objects
 		if (!DTHelper.isOwnedObject(storyObject)) {
 			title.setEnabled(false);
 
 			// if (storyObject.getType() != null
 			// && !storyObject.isTypeUserDefined()) {
 			categories.setEnabled(false);
 			// }
 			addStep.setEnabled(false);
 			description.setEnabled(false);
 		}
 
 		Button cancel = (Button) view.findViewById(R.id.btn_createstory_cancel);
 		cancel.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				if (Log.isLoggable(TAG, Log.VERBOSE)) {
 					Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment clicked on cancel button ");
 				}
 
 				getSherlockActivity().getSupportFragmentManager().popBackStack();
 			}
 
 		});
 
 		Button save = (Button) view.findViewById(R.id.btn_createstory_ok);
 		save.setOnClickListener(new SaveStory());
 
 		return view;
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment.onPause ");
 		}
 
 		// title
 		EditText title = (EditText) view.findViewById(R.id.story_title);
 		storyObject.setTitle(title.getText().toString());
 
 		// description
 		EditText description = (EditText) view.findViewById(R.id.story_description);
 		storyObject.setDescription(description.getText().toString());
 
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent result) {
 		super.onActivityResult(requestCode, resultCode, result);
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment.onActivityResult ");
 		}
 
 	}
 
 	@Override
 	public List<SemanticSuggestion> getTags(CharSequence text) {
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment.getTags ");
 		}
 
 		try {
 			return DTHelper.getSuggestions(text);
 		} catch (Exception e) {
 			return Collections.emptyList();
 		}
 	}
 
 	/*
 	 * The asinch task used by SaveStory for creating or updating the story
 	 */
 	private class CreateStoryProcessor extends AbstractAsyncTaskProcessor<StoryObject, Boolean> {
 
 		public CreateStoryProcessor(Activity activity) {
 			super(activity);
 		}
 
 		@Override
 		public Boolean performAction(StoryObject... params) throws SecurityException, Exception {
 
 			if (Log.isLoggable(TAG, Log.VERBOSE)) {
 				Log.v(TAG,
 						"eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment CreateStoryProcessor.performAction ");
 			}
 
 			return DTHelper.saveStory(params[0]);
 		}
 
 		@Override
 		public void handleResult(Boolean result) {
 
 			if (Log.isLoggable(TAG, Log.VERBOSE)) {
 				Log.v(TAG,
 						"eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment CreateStoryProcessor.handleResult ");
 			}
 
 			if (result) {
 				Toast.makeText(getSherlockActivity(), R.string.story_create_success, Toast.LENGTH_SHORT).show();
 				getSherlockActivity().getSupportFragmentManager().popBackStack();
 
 			} else {
 				Toast.makeText(getSherlockActivity(), R.string.update_success, Toast.LENGTH_SHORT).show();
 				getSherlockActivity().getSupportFragmentManager().popBackStack();
 
 			}
 		}
 	}
 
 	/*
 	 * the listener stores the data in the ui's fields and save the story
 	 */
 	private class SaveStory implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 
 			if (Log.isLoggable(TAG, Log.VERBOSE)) {
 				Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment SaveStory.onClick ");
 			}
 			CharSequence desc = ((EditText) view.findViewById(R.id.story_description)).getText();
 			if (desc != null) {
 				storyObject.setDescription(desc.toString());
 			}
 			CharSequence title = ((EditText) view.findViewById(R.id.story_title)).getText();
 			if (title != null) {
 				storyObject.setTitle(title.toString());
 			}
 
 			String catString = ((Spinner) view.findViewById(R.id.story_category)).getSelectedItem().toString();
 			String cat = getCategoryDescriptorByDescription(catString).category;
 
 			storyObject.setType(cat);
 			for (int i = 0; i < storyObject.getSteps().size(); i++) {
 				StepObject step = storyObject.getSteps().get(i);
 				if ((step != null) && (step.assignedPoi() != null)) {
 					storyObject.getSteps().get(i).setId((step.assignedPoi().getId()));
 
 				}
 			}
 			// check if some important field is missing and, if it is, show a
 			// message
 			Integer missing = validate(storyObject);
 			if (missing != null) {
 				Toast.makeText(getSherlockActivity(), getString(missing) + " " + getString(R.string.toast_is_required),
 						Toast.LENGTH_SHORT).show();
 				return;
 			}
 
 			new SCAsyncTask<StoryObject, Void, Boolean>(getActivity(), new CreateStoryProcessor(getActivity()))
 					.execute(storyObject);
 
 		}
 
 	}
 
 	private Integer validate(StoryObject data) {
 		Integer result = null;
 
 		if (Log.isLoggable(TAG, Log.VERBOSE)) {
 			Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment.validate");
 		}
 
		if (data.getTitle() == null || data.getTitle().trim().length()==0)
 			return R.string.create_title;
 		return result;
 	}
 
 	private CategoryDescriptor getCategoryDescriptorByDescription(String desc) {
 		for (CategoryDescriptor cd : categoryDescriptors) {
 			String catDesc = getSherlockActivity().getApplicationContext().getResources().getString(cd.description);
 			if (catDesc.equalsIgnoreCase(desc)) {
 				return cd;
 			}
 		}
 
 		return null;
 	}
 
 	/*
 	 * implements the interface used by AddStep... and manage adding or updating
 	 * a step in this fragment
 	 */
 	private class AddStep implements StepHandler, Parcelable {
 
 		@Override
 		public void addStep(StepObject step) {
 
 			if (Log.isLoggable(TAG, Log.VERBOSE)) {
 				Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment AddStep.addStep");
 			}
 			// add the step, notify to the adapter and go back to this fragment
 			storyObject.getSteps().add(step);
 			stepAdapter.notifyDataSetChanged();
 			getSherlockActivity().getSupportFragmentManager().popBackStack();
 
 		}
 
 		@Override
 		public int describeContents() {
 			return 0;
 		}
 
 		@Override
 		public void writeToParcel(Parcel dest, int flags) {
 
 		}
 
 		@Override
 		public void updateStep(StepObject step, Integer position) {
 
 			if (Log.isLoggable(TAG, Log.VERBOSE)) {
 				Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.CreateStoryFragment AddStep.updateStep");
 			}
 
 			// generate dialog box for confirming the update
 			storyObject.getSteps().set(position, step);
 			stepAdapter.notifyDataSetChanged();
 			getSherlockActivity().getSupportFragmentManager().popBackStack();
 
 		}
 
 	}
 }
