 package org.wheelmap.android.fragment;
 
 import java.util.Map;
 
 import org.wheelmap.android.app.WheelmapApp;
 import org.wheelmap.android.manager.SupportManager;
 import org.wheelmap.android.manager.SupportManager.NodeType;
 import org.wheelmap.android.manager.SupportManager.WheelchairAttributes;
 import org.wheelmap.android.model.Extra;
 import org.wheelmap.android.model.POIHelper;
 import org.wheelmap.android.model.PrepareDatabaseHelper;
 import org.wheelmap.android.model.UserCredentials;
 import org.wheelmap.android.model.Wheelmap.POIs;
 import org.wheelmap.android.online.R;
 import org.wheelmap.android.service.SyncServiceHelper;
 
 import roboguice.inject.InjectView;
 import wheelmap.org.WheelchairState;
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
 
 import de.akquinet.android.androlog.Log;
 
 public class POIDetailEditableFragment extends RoboSherlockFragment implements
 		OnClickListener, LoaderCallbacks<Cursor> {
 	public final static String TAG = POIDetailEditableFragment.class
 			.getSimpleName();
 
 	private final static int LOADER_CONTENT = 0;
 
 	@InjectView(R.id.title_container)
 	private LinearLayout title_container;
 	@InjectView(R.id.name)
 	private EditText nameText;
 	@InjectView(R.id.nodetype)
 	private TextView nodetypeText;
 	@InjectView(R.id.comment)
 	private EditText commentText;
 	@InjectView(R.id.street)
 	private EditText streetText;
 	@InjectView(R.id.housenum)
 	private EditText housenumText;
 	@InjectView(R.id.postcode)
 	private EditText postcodeText;
 	@InjectView(R.id.city)
 	private EditText cityText;
 	@InjectView(R.id.website)
 	private EditText websiteText;
 	@InjectView(R.id.phone)
 	private EditText phoneText;
 	@InjectView(R.id.state_icon)
 	private ImageView state_icon;
 	@InjectView(R.id.state_text)
 	private TextView state_text;
 	@InjectView(R.id.edit_position_text)
 	private TextView position_text;
 
 	@InjectView(R.id.wheelchair_state_layout)
 	private RelativeLayout edit_state_container;
 	@InjectView(R.id.edit_geolocation)
 	private RelativeLayout edit_geolocation_touchable_container;
 	@InjectView(R.id.edit_nodetype)
 	private RelativeLayout edit_nodetype_container;
 
 	@InjectView(R.id.edit_geolocation_container)
 	private LinearLayout edit_geolocation_container;
 
 	private Long poiID = Extra.ID_UNKNOWN;
 	private WheelchairState mWheelchairState;
 	private double mLatitude;
 	private double mLongitude;
 	private int mNodeType;
 
 	private Map<WheelchairState, WheelchairAttributes> mWSAttributes;
 	private OnPOIDetailEditableListener mListener;
 
 	public interface OnPOIDetailEditableListener {
 		public void onEditSave();
 
 		public void onEditWheelchairState(WheelchairState state);
 
 		public void onEditGeolocation(double latitude, double longitude);
 
 		public void onEditNodetype(int nodetype);
 
 		public void requestExternalEditedState(
 				POIDetailEditableFragment fragment);
 	}
 
 	public static POIDetailEditableFragment newInstance(long poiId) {
 		Bundle b = new Bundle();
 		b.putLong(Extra.POI_ID, poiId);
 
 		POIDetailEditableFragment fragment = new POIDetailEditableFragment();
 		fragment.setArguments(b);
 
 		return fragment;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 
 		if (activity instanceof OnPOIDetailEditableListener) {
 			mListener = (OnPOIDetailEditableListener) activity;
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 
 		mWSAttributes = SupportManager.wsAttributes;
 		poiID = getArguments().getLong(Extra.POI_ID);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.fragment_detail_editable, container,
 				false);
 
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		edit_state_container.setOnClickListener(this);
 		edit_geolocation_touchable_container.setOnClickListener(this);
 		edit_nodetype_container.setOnClickListener(this);
 
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		retrieve(savedInstanceState);
 		UserCredentials credentials = new UserCredentials(getActivity()
 				.getApplicationContext());
 		if (!credentials.isLoggedIn()) {
 			FragmentManager fm = getFragmentManager();
 			LoginDialogFragment loginDialog = new LoginDialogFragment();
 			loginDialog.show(fm, LoginDialogFragment.TAG);
 		}
 
 	}
 
 	private void retrieve(Bundle bundle) {
 		getLoaderManager().initLoader(LOADER_CONTENT, getArguments(), this);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		storeExternalEditedState();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 	}
 
 	private void storeExternalEditedState() {
 		if (mListener != null)
 			mListener.requestExternalEditedState(this);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putBoolean(Extra.EDITABLE_TEMPORARY_STORE, true);
 		storeTemporary();
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.ab_detaileditable_fragment, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int id = item.getItemId();
 
 		switch (id) {
 		case R.id.menu_save:
 			save();
 			break;
 		default:
 			// noop
 		}
 
 		return false;
 	}
 
 	@Override
 	public void onClick(View v) {
 		closeKeyboard();
 
 		int id = v.getId();
 		switch (id) {
 		case R.id.wheelchair_state_layout: {
 			if (mListener != null)
 				mListener.onEditWheelchairState(mWheelchairState);
 			break;
 		}
 		case R.id.edit_geolocation: {
 			if (mListener != null)
 				mListener.onEditGeolocation(mLatitude, mLongitude);
 			break;
 		}
 		case R.id.edit_nodetype: {
 			if (mListener != null)
 				mListener.onEditNodetype(mNodeType);
 			break;
 		}
 
 		default:
 			// do nothing
 		}
 	}
 
 	public void save() {
 
 		ContentValues values = retrieveContentValues();
 
 		if (!values.containsKey(POIs.NODETYPE_ID)) {
 			showErrorMessage(getString(R.string.error_category_missing_title),
 					getString(R.string.error_category_missing_message));
 			return;
 		}
 		values.put(POIs.DIRTY, POIs.DIRTY_ALL);
 
 		PrepareDatabaseHelper.editCopy(getActivity().getContentResolver(),
 				poiID, values);
 		SyncServiceHelper.executeUpdateServer(getActivity(), null);
 
 		if (mListener != null) {
 			mListener.onEditSave();
 		}
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
 		Uri uri = ContentUris.withAppendedId(POIs.CONTENT_URI_COPY, poiID);
 		return new CursorLoader(WheelmapApp.get(), uri, null, null, null, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		load(cursor);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 
 	}
 
 	private void load(Cursor cursor) {
 
 		if (cursor == null || cursor.getCount() < 1)
 			return;
 
 		cursor.moveToFirst();
 
 		WheelchairState state = POIHelper.getWheelchair(cursor);
 		String name = POIHelper.getName(cursor);
 		String comment = POIHelper.getComment(cursor);
 		double latitude = POIHelper.getLatitude(cursor);
 		double longitude = POIHelper.getLongitude(cursor);
 		int nodeType = POIHelper.getNodeTypeId(cursor);
 
 		setGeolocation(latitude, longitude);
 		setNodetype(nodeType);
 		setWheelchairState(state);
 		nameText.setText(name);
 		commentText.setText(comment);
 
 		streetText.setText(POIHelper.getStreet(cursor));
 		housenumText.setText(POIHelper.getHouseNumber(cursor));
 		postcodeText.setText(POIHelper.getPostcode(cursor));
 		cityText.setText(POIHelper.getCity(cursor));
 
 		websiteText.setText(POIHelper.getWebsite(cursor));
 		phoneText.setText(POIHelper.getPhone(cursor));
 
 		if (nodeType == SupportManager.UNKNOWN_TYPE)
 			showGeolocationEditor(true);
 	}
 
 	private void showGeolocationEditor(boolean show) {
 		if (show)
 			edit_geolocation_container.setVisibility(View.VISIBLE);
 		else
 			edit_geolocation_container.setVisibility(View.GONE);
 
 	}
 
 	private ContentValues retrieveContentValues() {
 		ContentValues values = new ContentValues();
 		values.put(POIs.NAME, nameText.getText().toString());
 
 		SupportManager sm = WheelmapApp.getSupportManager();
 
		if (mNodeType != Extra.UNKNOWN) {
 			int categoryId = sm.lookupNodeType(mNodeType).categoryId;
 			values.put(POIs.CATEGORY_ID, categoryId);
 			values.put(POIs.NODETYPE_ID, mNodeType);
 		}
 
 		values.put(POIs.LATITUDE, mLatitude);
 		values.put(POIs.LONGITUDE, mLongitude);
 
 		values.put(POIs.WHEELCHAIR, mWheelchairState.getId());
 		String description = commentText.getText().toString();
 		if (!TextUtils.isEmpty(description))
 			values.put(POIs.DESCRIPTION, description);
 
 		String street = streetText.getText().toString();
 		if (!TextUtils.isEmpty(street))
 			values.put(POIs.STREET, street);
 		String housenum = housenumText.getText().toString();
 		if (!TextUtils.isEmpty(housenum))
 			values.put(POIs.HOUSE_NUM, housenum);
 		String postcode = postcodeText.getText().toString();
 		if (!TextUtils.isEmpty(postcode))
 			values.put(POIs.POSTCODE, postcode);
 		String city = cityText.getText().toString();
 		if (!TextUtils.isEmpty(city))
 			values.put(POIs.CITY, city);
 
 		String website = websiteText.getText().toString();
 		if (!TextUtils.isEmpty(website))
 			values.put(POIs.WEBSITE, website);
 		String phone = phoneText.getText().toString();
 		if (!TextUtils.isEmpty(phone))
 			values.put(POIs.PHONE, phone);
 		return values;
 	}
 
 	private void storeTemporary() {
 		ContentValues values = retrieveContentValues();
 		PrepareDatabaseHelper.editCopy(getActivity().getContentResolver(),
 				poiID, values);
 	}
 
 	public void setWheelchairState(WheelchairState state) {
 		if (state == null)
 			return;
 
 		mWheelchairState = state;
 
 		int stateColor = getResources().getColor(
 				mWSAttributes.get(state).colorId);
 
 		title_container.setBackgroundColor(stateColor);
 		state_icon.setImageResource(mWSAttributes.get(state).drawableId);
 		state_text.setTextColor(stateColor);
 		state_text.setText(mWSAttributes.get(state).titleStringId);
 	}
 
 	public void setGeolocation(double latitude, double longitude) {
 		if (latitude == Extra.UNKNOWN || longitude == Extra.UNKNOWN)
 			return;
 
 		Log.d(TAG, "onResult: mLatitude = " + mLatitude + " mLongitude = "
 				+ mLongitude);
 		mLatitude = latitude;
 		mLongitude = longitude;
 
 		String positionText = String.format("%s: (%.6f:%.6f)", getResources()
 				.getString(R.string.position_geopoint), mLatitude, mLongitude);
 		position_text.setText(positionText);
 	}
 
 	public void setNodetype(int nodetype) {
 		if (nodetype == Extra.UNKNOWN)
 			return;
 
 		mNodeType = nodetype;
 		SupportManager manager = WheelmapApp.getSupportManager();
 		NodeType nodeType = manager.lookupNodeType(nodetype);
 		nodetypeText.setText(nodeType.localizedName);
 	}
 
 	public void showErrorMessage(String title, String message) {
 		FragmentManager fm = getFragmentManager();
 		ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(
 				title, message);
 		if (errorDialog == null)
 			return;
 
 		errorDialog.show(fm, ErrorDialogFragment.TAG);
 	}
 
 	public void closeKeyboard() {
 		View view = getView().findFocus();
 		if (view == null || !(view instanceof EditText))
 			return;
 
 		InputMethodManager imm = (InputMethodManager) getActivity()
 				.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
 
 	}
 }
