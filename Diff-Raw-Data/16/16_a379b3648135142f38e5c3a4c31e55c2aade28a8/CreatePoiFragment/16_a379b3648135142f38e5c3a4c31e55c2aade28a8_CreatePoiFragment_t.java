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
 package eu.trentorise.smartcampus.dt.fragments.pois;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Address;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 
 import eu.trentorise.smartcampus.android.common.GeocodingAutocompletionHelper;
 import eu.trentorise.smartcampus.android.common.GeocodingAutocompletionHelper.OnAddressSelectedListener;
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
 import eu.trentorise.smartcampus.android.common.SCGeocoder;
 import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
 import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
 import eu.trentorise.smartcampus.dt.R;
 import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
 import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
 import eu.trentorise.smartcampus.dt.custom.CategoryHelper.CategoryDescriptor;
 import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
 import eu.trentorise.smartcampus.dt.custom.map.MapManager;
 import eu.trentorise.smartcampus.dt.fragments.search.SearchFragment;
 import eu.trentorise.smartcampus.dt.model.CommunityData;
 import eu.trentorise.smartcampus.dt.model.Concept;
 import eu.trentorise.smartcampus.dt.model.POIData;
 import eu.trentorise.smartcampus.dt.model.POIObject;
 import eu.trentorise.smartcampus.dt.model.UserPOIObject;
 import eu.trentorise.smartcampus.dt.notifications.NotificationsSherlockFragmentDT;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 
 public class CreatePoiFragment extends NotificationsSherlockFragmentDT implements OnTagsSelectedListener, TagProvider {
 
 	public static String ARG_POI = "poi";
 	public static String ARG_POI_HANDLER = "handler";
 
 	private Address mAddress = null;
 	private View view = null;
 	private PoiHandler poiHandler = null;
 
 	private POIObject poiObject = null;
 	public static final String TN_REGION = "it";
 	public static final String TN_COUNTRY = "IT";
 	public static final String TN_ADM_AREA = "TN";
 
 	private CategoryDescriptor[] categoryDescriptors;
 
 	@Override
 	public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
 		poiObject.getCommunityData().setTags(Concept.convertSS(suggestions));
 		((EditText) getView().findViewById(R.id.poi_tags)).setText(Concept.toSimpleString(poiObject.getCommunityData()
 				.getTags()));
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle arg0) {
 		super.onSaveInstanceState(arg0);
 		arg0.putSerializable(ARG_POI, poiObject);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(false);
 		if (getArguments() != null && getArguments().containsKey(ARG_POI_HANDLER)
 				&& getArguments().getParcelable(ARG_POI_HANDLER) != null) {
 			poiHandler = (PoiHandler) getArguments().getParcelable(ARG_POI_HANDLER);
 		}
 		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_POI)
 				&& savedInstanceState.getSerializable(ARG_POI) != null) {
 			poiObject = (POIObject) savedInstanceState.get(ARG_POI);
 		} else if (getArguments() != null && getArguments().containsKey(ARG_POI)
 				&& getArguments().getSerializable(ARG_POI) != null) {
 			poiObject = (POIObject) getArguments().getSerializable(ARG_POI);
 		} else {
 			poiObject = new UserPOIObject();
 			if (getArguments() != null && getArguments().containsKey(SearchFragment.ARG_CATEGORY)) {
 				poiObject.setType(getArguments().getString(SearchFragment.ARG_CATEGORY));
 			}
 
 		}
 		if (poiObject.getCommunityData() == null)
 			poiObject.setCommunityData(new CommunityData());
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		view = inflater.inflate(R.layout.createpoiform, container, false);
 
 		categoryDescriptors = CategoryHelper.getPOICategoryDescriptorsFiltered();
 
 		Spinner categories = (Spinner) view.findViewById(R.id.poi_category);
 		int selected = -1;
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.dd_list, R.id.dd_textview);
 		// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		categories.setAdapter(adapter);
 		String mainCat = CategoryHelper.getMainCategory(poiObject.getType());
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
 			categories.setSelection(categoryDescriptors.length -1);
 		}
 
 		EditText title = (EditText) view.findViewById(R.id.poi_title);
 		title.setText(poiObject.getTitle());
 
 		AutoCompleteTextView location = (AutoCompleteTextView) view.findViewById(R.id.poi_place);
 		GeocodingAutocompletionHelper locationAutocompletionHelper = new GeocodingAutocompletionHelper(getSherlockActivity(), location,
 				TN_REGION, TN_COUNTRY, TN_ADM_AREA);
 /*		locationAutocompletionHelper.setOnAddressSelectedListener(new OnAddressSelectedListener() {
 			@Override
 			public void onAddressSelected(Address address) {
 				savePosition(address, "from");
 			}
 		});*/
 		
 		//autocomplete the poi's address
 		locationAutocompletionHelper.setOnAddressSelectedListener(new OnAddressSelectedListener() {
 			@Override
 			public void onAddressSelected(Address address) {
 				mAddress = address;
 			}
 		});
 		if (poiObject.getPoi() != null) {
 			location.setText(poiObject.getPoi().getStreet());
 		} else {
 			// try to get the current position
 			GeoPoint mypos = MapManager.requestMyLocation(getSherlockActivity());
 			if (mypos != null) {
 				List<Address> addresses = new SCGeocoder(getSherlockActivity()).findAddressesAsync(mypos);
 				if (addresses != null && !addresses.isEmpty()) {
 					location.setText(addresses.get(0).getAddressLine(0));
 					mAddress = addresses.get(0);
 				}
 			}
 
 		}
 
 		ImageButton locationBtn = (ImageButton) view.findViewById(R.id.btn_poi_locate);
 
 		EditText notes = (EditText) view.findViewById(R.id.poi_notes);
 		notes.setText(poiObject.getCommunityData().getNotes());
 
 		EditText tagsEdit = (EditText) view.findViewById(R.id.poi_tags);
 		tagsEdit.setText(Concept.toSimpleString(poiObject.getCommunityData().getTags()));
 		tagsEdit.setClickable(true);
 		tagsEdit.setFocusableInTouchMode(false);
 		tagsEdit.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				TaggingDialog taggingDialog = new TaggingDialog(getActivity(), CreatePoiFragment.this, CreatePoiFragment.this,
 						Concept.convertToSS(poiObject.getCommunityData().getTags()));
 				taggingDialog.show();
 			}
 		});
 		
 		ImageButton button = (ImageButton) view.findViewById(R.id.btn_poi_locate);
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(getActivity(), AddressSelectActivity.class);
 				if (mAddress != null) {
 					intent.putExtra(AddressSelectActivity.ARG_POINT, mAddress);
 				}
 				startActivityForResult(intent, AddressSelectActivity.RESULT_SELECTED);
 			}
 		});
 
 		// cannot modify title, place, categories and notes of not-owned objects
 		if (!DTHelper.isOwnedObject(poiObject)) {
 			title.setEnabled(false);
 			location.setEnabled(false);
 			locationBtn.setEnabled(false);
 //			if (poiObject.getType() != null && !poiObject.isTypeUserDefined()) {
 				categories.setEnabled(false);
 //			}
 			notes.setEnabled(false);
 		}
 
 		Button cancel = (Button) view.findViewById(R.id.btn_createpoi_cancel);
 		cancel.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				getSherlockActivity().getSupportFragmentManager().popBackStack();
 			}
 			
 		});
 
 		Button save = (Button) view.findViewById(R.id.btn_createpoi_ok);
 		save.setOnClickListener(new SavePOI());
 
 		return view;
 	}
 
 	private Integer validate(POIObject data) {
 		Integer result = null;
		if (data.getTitle() == null || data.getTitle().trim().length()==0 )
 			return R.string.create_title;
 		if (data.getLocation() == null)
 			return R.string.create_place;
 		if (data.getType() == null || data.getType().length() == 0)
 			return R.string.create_cat;
 		return result;
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent result) {
 		super.onActivityResult(requestCode, resultCode, result);
 		if (resultCode == AddressSelectActivity.RESULT_SELECTED) {
 			mAddress = result.getParcelableExtra("address");
 			EditText text = (EditText) view.findViewById(R.id.poi_place);
 			text.setText(mAddress.getAddressLine(0));
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
 
 	/*
 	 * private class CreatePoiProcessor extends
 	 * AbstractAsyncTaskProcessor<POIObject, Boolean> {
 	 * 
 	 * public CreatePoiProcessor(Activity activity) { super(activity); }
 	 * 
 	 * @Override public Boolean performAction(POIObject... params) throws
 	 * SecurityException, Exception { return DTHelper.savePOI(params[0]); }
 	 * 
 	 * @Override public void handleResult(Boolean result) {
 	 * getSherlockActivity().getSupportFragmentManager().popBackStack(); if
 	 * (result) { Toast.makeText(getSherlockActivity(),
 	 * R.string.poi_create_success, Toast.LENGTH_SHORT).show();
 	 * 
 	 * } else { Toast.makeText(getSherlockActivity(), R.string.update_success,
 	 * Toast.LENGTH_SHORT).show(); } } }
 	 */
 
 	private CategoryDescriptor getCategoryDescriptorByDescription(String desc) {
 		for (CategoryDescriptor cd : categoryDescriptors) {
 			String catDesc = getSherlockActivity().getApplicationContext().getResources().getString(cd.description);
 			if (catDesc.equalsIgnoreCase(desc)) {
 				return cd;
 			}
 		}
 
 		return null;
 	}
 
 	private class CreatePoiProcessor extends AbstractAsyncTaskProcessor<POIObject, POIObject> {
 
 		private boolean created = false;
 		
 		public CreatePoiProcessor(Activity activity) {
 			super(activity);
 		}
 
 		@Override
 		public POIObject performAction(POIObject... params) throws SecurityException, Exception {
 			if (params[0].getId() == null)
 				created = true;
 			return DTHelper.savePOI(params[0]);
 		}
 
 		@Override
 		public void handleResult(POIObject result) {
 			getSherlockActivity().getSupportFragmentManager().popBackStack();
 			if(result!=null)
 				{
 				poiObject=result;
 				if (created)
 					Toast.makeText(getSherlockActivity(), R.string.poi_create_success, Toast.LENGTH_SHORT).show();
 				else Toast.makeText(getSherlockActivity(), R.string.update_success, Toast.LENGTH_SHORT).show();
 
 				}
 			else {
 				Toast.makeText(getSherlockActivity(), R.string.update_success, Toast.LENGTH_SHORT).show();
 			}
 			if (poiHandler!=null) {
 				Toast.makeText(getSherlockActivity(), R.string.poi_create_success, Toast.LENGTH_SHORT).show();
 					poiHandler.addPoi(poiObject);
 			}
 		}
 	}
 	
 	private class SavePOI implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			CharSequence desc = ((EditText) view.findViewById(R.id.poi_notes)).getText();
 			if (desc != null) {
 				poiObject.getCommunityData().setNotes(desc.toString());
 			}
 			CharSequence title = ((EditText) view.findViewById(R.id.poi_title)).getText();
 			if (title != null) {
 				poiObject.setTitle(title.toString());
 			}
 
 			String catString = ((Spinner) view.findViewById(R.id.poi_category)).getSelectedItem().toString();
 			String cat = getCategoryDescriptorByDescription(catString).category;
 
 			poiObject.setType(cat);
 			if (mAddress != null) {
 				POIData poiData = new POIData();
 				poiData.setStreet(mAddress.getAddressLine(0));
 				poiData.setCity(mAddress.getLocality());
 				poiData.setCountry(mAddress.getCountryName());
 				poiData.setPostalCode(mAddress.getPostalCode());
 				poiData.setDatasetId("smart");
 				poiData.setState(mAddress.getCountryCode());
 				poiData.setRegion(mAddress.getAdminArea());
 				poiData.setLatitude(mAddress.getLatitude());
 				poiData.setLongitude(mAddress.getLongitude());
 				poiObject.setPoi(poiData);
 			}
 
 			Integer missing = validate(poiObject);
 			if (missing != null) {
 				Toast.makeText(
 						getActivity(),
 						getActivity().getResources().getString(missing)
 								+ " "
 								+ getSherlockActivity().getApplicationContext().getResources()
 										.getString(R.string.toast_is_required), Toast.LENGTH_SHORT).show();
 				return;
 			}
 
 			new SCAsyncTask<POIObject, Void, POIObject>(getActivity(), new CreatePoiProcessor(getActivity()))
 					.execute(poiObject);
 
 		}
 
 	}
 
 	public interface PoiHandler {
 		public void addPoi(POIObject poi);
 	}
 
 }
