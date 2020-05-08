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
 package eu.trentorise.smartcampus.dt.fragments.events;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.widget.Toast;
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
 import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
 import eu.trentorise.smartcampus.dt.R;
 import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
 import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
 import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
 import eu.trentorise.smartcampus.dt.custom.DatePickerDialogFragment;
 import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
 import eu.trentorise.smartcampus.dt.fragments.pois.CreatePoiFragment;
 import eu.trentorise.smartcampus.dt.fragments.pois.CreatePoiFragment.PoiHandler;
 import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
 import eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment;
 import eu.trentorise.smartcampus.dt.model.CommunityData;
 import eu.trentorise.smartcampus.dt.model.Concept;
 import eu.trentorise.smartcampus.dt.model.EventObject;
 import eu.trentorise.smartcampus.dt.model.POIObject;
 import eu.trentorise.smartcampus.dt.model.UserEventObject;
 import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 
 public class CreateEventFragment extends NotificationsSherlockFragmentDT implements TaggingDialog.OnTagsSelectedListener,
 		TaggingDialog.TagProvider {
 
 	private POIObject poi = null;
 	private View view = null;
 	private EditText dateToEdit;
 	private CheckBox moreDaysCheckbox;
 	private CreatePoiFromEvent poiHandler = new CreatePoiFromEvent();
 	private AutoCompleteTextView poiField;
 	public static String ARG_EVENT = "event";
 	public static final SimpleDateFormat FORMAT_DATE_UI = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
 	private CategoryDescriptor[] categoryDescriptors;
 
 	private EventObject eventObject;
 
 	@Override
 	public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
 		eventObject.getCommunityData().setTags(Concept.convertSS(suggestions));
 		((EditText) getView().findViewById(R.id.event_tags)).setText(Concept.toSimpleString(eventObject.getCommunityData()
 				.getTags()));
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle arg0) {
 		super.onSaveInstanceState(arg0);
 		// arg0.putSerializable(ARG_EVENT, eventObject);
 		arg0.putString(ARG_EVENT, eventObject.getId());
 	}
 
 	private EventObject getEvent(Bundle savedInstanceState) {
 		if (eventObject == null) {
 			Bundle bundle = this.getArguments();
 			String eventId = bundle.getString(ARG_EVENT);
 			eventObject = DTHelper.findEventById(eventId);
 			if (eventObject != null) {
 				poi = DTHelper.findPOIById(eventObject.getPoiId());
 				eventObject.assignPoi(poi);
 			}
 		}
 		return eventObject;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(false);
 
 		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_EVENT)
 				&& savedInstanceState.getSerializable(ARG_EVENT) != null) {
 			eventObject = getEvent(savedInstanceState);
 		} else if (getArguments() != null && getArguments().containsKey(ARG_EVENT)
 				&& getArguments().getSerializable(ARG_EVENT) != null) {
 			eventObject = getEvent(savedInstanceState);
 		} else {
 			eventObject = new UserEventObject();
 			if (getArguments() != null && getArguments().containsKey(SearchFragment.ARG_CATEGORY)) {
 				eventObject.setType(getArguments().getString(SearchFragment.ARG_CATEGORY));
 			}
 		}
 
 		if (eventObject.getPoiId() != null)
 			poi = DTHelper.findPOIById(eventObject.getPoiId());
 
 		if (eventObject.getCommunityData() == null)
 			eventObject.setCommunityData(new CommunityData());
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		// date and time will be returned as tags
 		final EditText dateFromEditText = (EditText) getView().findViewById(R.id.event_date_from);
 		if (eventObject.createdByUser() && DTHelper.isOwnedObject(eventObject)) {
 			dateFromEditText.setEnabled(true);
 			dateFromEditText.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					DialogFragment f = DatePickerDialogFragment.newInstance((EditText) v);
 					if (dateFromEditText.getText() != null)
 						f.setArguments(DatePickerDialogFragment.prepareData(dateFromEditText.getText().toString()));
 					f.show(getSherlockActivity().getSupportFragmentManager(), "datePicker");
 				}
 			});
 		} else {
 			dateFromEditText.setEnabled(false);
 		}
 		final EditText dateToEditText = (EditText) getView().findViewById(R.id.event_date_to);
 		// Date tmp = null;
 		// try {
 		// tmp = FORMAT_DATE_UI.parse(dateFromEditText.getText().toString());
 		// Calendar cal = Calendar.getInstance();
 		// cal.setTime(tmp);
 		// cal.add(Calendar.DATE, 1);
 		// dateToEditText.setText(FORMAT_DATE_UI.format(cal.getTime()));
 		// } catch (ParseException e) {
 		// Toast.makeText(
 		// getActivity(),
 		// getResources().getString(R.string.toast_incorrect) + " "
 		// + getResources().getString(R.string.createevent_date),
 		// Toast.LENGTH_SHORT).show();
 		// return;
 		// }
 		if (eventObject.createdByUser() && DTHelper.isOwnedObject(eventObject)) {
 			dateToEditText.setEnabled(true);
 			dateToEditText.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					DialogFragment f = DatePickerDialogFragment.newInstance((EditText) v);
 					if (dateToEditText.getText() != null)
 						f.setArguments(DatePickerDialogFragment.prepareData(dateToEditText.getText().toString()));
 					f.show(getSherlockActivity().getSupportFragmentManager(), "datePicker");
 				}
 			});
 
 		} else {
 			dateToEditText.setEnabled(false);
 		}
 		if (poi != null)
 			poiField.setText(poi.getTitle());
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		view = inflater.inflate(R.layout.createeventform, container, false);
 
 		categoryDescriptors = CategoryHelper.getEventCategoryDescriptorsFiltered();
 
 		Spinner categories = (Spinner) view.findViewById(R.id.event_category);
 		int selected = -1;
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.dd_list, R.id.dd_textview);
 		categories.setAdapter(adapter);
 		String mainCat = CategoryHelper.getMainCategory(eventObject.getType());
 		for (int i = 0; i < categoryDescriptors.length; i++) {
 			adapter.add(getSherlockActivity().getApplicationContext().getResources()
 					.getString(categoryDescriptors[i].description));
 			if (categoryDescriptors[i].category.equals(mainCat)) {
 				selected = i;
 			}
 		}
 		if (selected >= 0) {
 			categories.setSelection(selected);
 		} else {
 			categories.setSelection(categoryDescriptors.length - 1);
 		}
 
 		EditText title = (EditText) view.findViewById(R.id.event_title);
 		title.setText(eventObject.getTitle());
 
 		dateToEdit = (EditText) view.findViewById(R.id.event_date_to);
 		moreDaysCheckbox = (CheckBox) view.findViewById(R.id.more_days_checkbox);
 		if (eventObject.createdByUser() && DTHelper.isOwnedObject(eventObject)) {
 			moreDaysCheckbox.setEnabled(true);
 			moreDaysCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 					dateToEdit.setEnabled(isChecked);
 				}
 			});
 		} else {
 			moreDaysCheckbox.setEnabled(false);
 		}
 		EditText dateFromEdit = (EditText) view.findViewById(R.id.event_date_from);
 		if (eventObject.createdByUser() && DTHelper.isOwnedObject(eventObject)) {
 			dateFromEdit.setEnabled(true);
 		}
 		if (eventObject.getFromTime() != null && eventObject.getFromTime() > 0) {
 			dateFromEdit.setText(DatePickerDialogFragment.DATEFORMAT.format(new Date(eventObject.getFromTime())));
 		}
 
 		dateToEdit = (EditText) view.findViewById(R.id.event_date_to);
 		if (eventObject.getToTime() != null && eventObject.getToTime() > 0) {
 			dateToEdit.setText(DatePickerDialogFragment.DATEFORMAT.format(new Date(eventObject.getToTime())));
 
 			moreDaysCheckbox.setChecked(true);
 		} else {
 			Calendar c = Calendar.getInstance();
 			c.set(Calendar.MINUTE, 0);
 			dateFromEdit.setText(DatePickerDialogFragment.DATEFORMAT.format(c.getTime()));
 		}
 
 		EditText timing = (EditText) view.findViewById(R.id.event_timing_et);
 		// timing is editable only in own UserEvent
 		if (eventObject.createdByUser() && DTHelper.isOwnedObject(eventObject)) {
 			// set edit visible, set textview gone
 			if (eventObject.getTiming() != null) {
 				timing.setText(eventObject.getTimingFormatted());
 			}
 			timing.setEnabled(true);
 		} else {
 			// set edit gone, set textview visible
 			timing.setText(eventObject.getTimingFormatted());
 			timing.setEnabled(false);
 		}
 
 		poiField = (AutoCompleteTextView) view.findViewById(R.id.event_place);
 		ArrayAdapter<String> poiAdapter = new ArrayAdapter<String>(getSherlockActivity(), R.layout.dd_list, R.id.dd_textview,
 				DTHelper.getAllPOITitles());
 		poiField.setAdapter(poiAdapter);
 		if (poi != null) {
 			poiField.setText(poi.getTitle());
 		}
 
 		ImageButton addPoiBtn = (ImageButton) view.findViewById(R.id.btn_event_add_poi);
 		addPoiBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				FragmentTransaction fragmentTransaction;
 				Fragment fragment;
 
 				fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
 				fragment = new CreatePoiFragment();
 				Bundle args = new Bundle();
 				args.putParcelable(AddStepToStoryFragment.ARG_STEP_HANDLER, poiHandler);
 				fragment.setArguments(args);
 				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 				fragmentTransaction.replace(android.R.id.content, fragment, "pois");
 				fragmentTransaction.addToBackStack(fragment.getTag());
 				fragmentTransaction.commit();
 			}
 		});
 
 		ImageButton locationBtn = (ImageButton) view.findViewById(R.id.btn_event_locate);
 		locationBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startActivityForResult(new Intent(getActivity(), POISelectActivity.class), POISelectActivity.RESULT_SELECTED);
 			}
 		});
 
 		EditText notes = (EditText) view.findViewById(R.id.event_notes);
 		notes.setText(eventObject.getCommunityData().getNotes());
 
 		// Cannot edit title, date, poi, category, and notes for ServiceEvent
 		// and non-owned UserEvent
 		if (!eventObject.createdByUser() || !DTHelper.isOwnedObject(eventObject)) {
 			title.setEnabled(false);
 			// if (eventObject.getType() != null &&
 			// !eventObject.isTypeUserDefined()) {
 			categories.setEnabled(false);
 			// }
 			// if (eventObject.getFromTime() != null &&
 			// eventObject.getFromTime() > 0 &&
 			// !eventObject.isFromTimeUserDefined()) {
 			dateToEdit.setEnabled(false);
 			// }
 			// if (poi != null && !eventObject.isPoiIdUserDefined()) {
 			poiField.setEnabled(false);
 			locationBtn.setEnabled(false);
 			addPoiBtn.setEnabled(false);
 			// }
 			notes.setEnabled(false);
 		}
 
 		EditText tagsEdit = (EditText) view.findViewById(R.id.event_tags);
 		tagsEdit.setText(Concept.toSimpleString(eventObject.getCommunityData().getTags()));
 		tagsEdit.setClickable(true);
 		tagsEdit.setFocusableInTouchMode(false);
 		tagsEdit.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				TaggingDialog taggingDialog = new TaggingDialog(getActivity(), CreateEventFragment.this,
 						CreateEventFragment.this, Concept.convertToSS(eventObject.getCommunityData().getTags()));
 				taggingDialog.show();
 			}
 		});
 
 		Button cancel = (Button) view.findViewById(R.id.btn_createevent_cancel);
 		cancel.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				getSherlockActivity().getSupportFragmentManager().popBackStack();
 			}
 
 		});
 
 		Button save = (Button) view.findViewById(R.id.btn_createevent_ok);
 		save.setOnClickListener(new SaveEvent());
 
 		return view;
 	}
 
 	private Integer validate(EventObject data) {
 		Integer result = null;
		if (data.getTitle() == null || data.getTitle().length() == 0)
 			return R.string.create_title;
 		// if (data.getFromTime() == null)
 		// return R.string.createevent_timestart;
 		// if (data.getToTime() == null)
 		// return R.string.createevent_timeend;
 		// if (data.getToTime() <= data.getFromTime())
 		// return R.string.createevent_timeend;
 		if (data.getPoiId() == null)
 			return R.string.create_place;
 		if (data.getType() == null || data.getType().length() == 0)
 			return R.string.create_cat;
 		return result;
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent result) {
 		super.onActivityResult(requestCode, resultCode, result);
 		if (resultCode == POISelectActivity.RESULT_SELECTED) {
 			poi = (POIObject) result.getSerializableExtra("poi");
 			AutoCompleteTextView text = (AutoCompleteTextView) view.findViewById(R.id.event_place);
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
 		try {
 			return DTHelper.getSuggestions(text);
 		} catch (Exception e) {
 			return Collections.emptyList();
 		}
 	}
 
 	private class CreateEventProcessor extends AbstractAsyncTaskProcessor<EventObject, Boolean> {
 
 		public CreateEventProcessor(Activity activity) {
 			super(activity);
 		}
 
 		@Override
 		public Boolean performAction(EventObject... params) throws SecurityException, Exception {
 			return DTHelper.saveEvent(params[0]);
 		}
 
 		@Override
 		public void handleResult(Boolean result) {
 			getSherlockActivity().getSupportFragmentManager().popBackStack();
 			if (result) {
 				Toast.makeText(getSherlockActivity(), R.string.event_create_success, Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getSherlockActivity(), R.string.update_success, Toast.LENGTH_SHORT).show();
 			}
 		}
 
 	}
 
 	private class SaveEvent implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			CharSequence desc = ((EditText) view.findViewById(R.id.event_notes)).getText();
 			if (desc != null) {
 				eventObject.getCommunityData().setNotes(desc.toString());
 			}
 			CharSequence title = ((EditText) view.findViewById(R.id.event_title)).getText();
 			if (title != null) {
 				eventObject.setTitle(title.toString());
 			}
 
 			String catString = ((Spinner) view.findViewById(R.id.event_category)).getSelectedItem().toString();
 			String cat = getCategoryDescriptorByDescription(catString).category;
 
 			AutoCompleteTextView eventPlace = (AutoCompleteTextView) view.findViewById(R.id.event_place);
 			if ((poi == null || !poi.getTitle().equals(eventPlace.getText().toString())) && eventPlace.getText() != null
 					&& eventPlace.getText().length() > 0) {
 				poi = DTHelper.findPOIByTitle(eventPlace.getText().toString());
 			}
 
 			CharSequence dateFromstr = ((EditText) view.findViewById(R.id.event_date_from)).getText();
 			if (dateFromstr == null || dateFromstr.length() == 0) {
 				Toast.makeText(
 						getActivity(),
 						getActivity().getResources().getString(R.string.createevent_date) + " "
 								+ getActivity().getResources().getString(R.string.msg_field_required), Toast.LENGTH_SHORT)
 						.show();
 				return;
 			}
 			Calendar cal = Calendar.getInstance();
 			Date fromDate;
 			try {
 				fromDate = DatePickerDialogFragment.DATEFORMAT.parse(dateFromstr.toString());
 				cal.setTime(fromDate);
 			} catch (ParseException e) {
 				Toast.makeText(
 						getActivity(),
 						getResources().getString(R.string.toast_incorrect) + " "
 								+ getResources().getString(R.string.createevent_date), Toast.LENGTH_SHORT).show();
 				return;
 			}
 			eventObject.setFromTime(cal.getTimeInMillis());
 
 			CharSequence dateTostr = ((EditText) view.findViewById(R.id.event_date_to)).getText();
 			if (moreDaysCheckbox.isChecked()) {
 				if (dateTostr == null || dateTostr.length() == 0) {
 					Toast.makeText(
 							getActivity(),
 							getActivity().getResources().getString(R.string.createevent_ending_date) + " "
 									+ getActivity().getResources().getString(R.string.msg_field_required), Toast.LENGTH_SHORT)
 							.show();
 					return;
 				} else {
 					Date toDate;
 					try {
 						toDate = FORMAT_DATE_UI.parse(dateTostr.toString());
 						if (fromDate.after(toDate)) {
 							Toast.makeText(getActivity(), R.string.to_date_before_from_date, Toast.LENGTH_SHORT).show();
 							return;
 						}
 						cal.setTime(toDate);
 						eventObject.setToTime(cal.getTimeInMillis());
 					} catch (ParseException e) {
 						Toast.makeText(getActivity(), R.string.date_field_empty, Toast.LENGTH_SHORT).show();
 						return;
 					}
 
 				}
 			}
 
 			// if (eventObject.getTiming() == null ||
 			// eventObject.isFromTimeUserDefined() ||
 			// DTHelper.isOwnedObject(eventObject)) {
 			if (DTHelper.isOwnedObject(eventObject)) {
 				CharSequence timingstr = ((EditText) view.findViewById(R.id.event_timing_et)).getText();
 				if (timingstr == null || timingstr.length() == 0) {
 					Toast.makeText(
 							getActivity(),
 							getActivity().getResources().getString(R.string.createevent_timing) + " "
 									+ getActivity().getResources().getString(R.string.msg_field_required), Toast.LENGTH_SHORT)
 							.show();
 					return;
 				}
 				eventObject.setTiming(timingstr.toString());
 			}
 
 			eventObject.setType(cat);
 			if (poi != null) {
 				eventObject.setPoiId(poi.getId());
 			}
 			Integer missing = validate(eventObject);
 			if (missing != null) {
 				Toast.makeText(
 						getActivity(),
 						getActivity().getResources().getString(missing)
 								+ " "
 								+ getSherlockActivity().getApplicationContext().getResources()
 										.getString(R.string.toast_is_required), Toast.LENGTH_SHORT).show();
 				return;
 			}
 
 			new SCAsyncTask<EventObject, Void, Boolean>(getActivity(), new CreateEventProcessor(getActivity()))
 					.execute(eventObject);
 		}
 
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
 
 	private class CreatePoiFromEvent implements PoiHandler, Parcelable {
 		@Override
 		public void addPoi(POIObject poi) {
 
 			// add the step, notify to the adapter and go back to this fragment
 			CreateEventFragment.this.poi = poi;
 			CreateEventFragment.this.eventObject.assignPoi(poi);
 		}
 
 		@Override
 		public int describeContents() {
 			return 0;
 		}
 
 		@Override
 		public void writeToParcel(Parcel dest, int flags) {
 
 		}
 
 	}
 }
