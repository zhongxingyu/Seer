 package com.sunlightlabs.android.congress;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.sunlightlabs.android.congress.utils.AddressUpdater;
 import com.sunlightlabs.android.congress.utils.LegislatorImage;
 import com.sunlightlabs.android.congress.utils.LoadPhotoTask;
 import com.sunlightlabs.android.congress.utils.LoadsPhoto;
 import com.sunlightlabs.android.congress.utils.LocationUpdater;
 import com.sunlightlabs.android.congress.utils.Utils;
 import com.sunlightlabs.android.congress.utils.AddressUpdater.AddressUpdateable;
 import com.sunlightlabs.android.congress.utils.LocationUpdater.LocationUpdateable;
 import com.sunlightlabs.congress.java.Committee;
 import com.sunlightlabs.congress.java.CongressException;
 import com.sunlightlabs.congress.java.Legislator;
 
 public class LegislatorList extends ListActivity implements LoadsPhoto, LocationUpdateable<LegislatorList>, AddressUpdateable<LegislatorList> {
 	private final static int SEARCH_ZIP = 0;
 	private final static int SEARCH_LOCATION = 1;
 	private final static int SEARCH_STATE = 2;
 	private final static int SEARCH_LASTNAME = 3;
 	private final static int SEARCH_COMMITTEE = 4;
 
 	private ArrayList<Legislator> legislators = null;
 	private LoadLegislatorsTask loadLegislatorsTask = null;
 	private ShortcutImageTask shortcutImageTask = null;
 
 	private HashMap<String,LoadPhotoTask> loadPhotoTasks = new HashMap<String,LoadPhotoTask>();
 
 	private boolean shortcut;
 
 	private String zipCode, lastName, state, committeeId, committeeName;
 	private double latitude = -1;
 	private double longitude = -1;
 
 	private String address;
 
 	private LocationUpdater locationUpdater;
 	private AddressUpdater addressUpdater;
 	private boolean relocating = false;
 
 	private HeaderViewWrapper headerWrapper;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list_titled_header);
 
 		Utils.setupSunlight(this);
 
 		Bundle extras = getIntent().getExtras();
 
 		zipCode = extras.getString("zip_code");
 		latitude = extras.getDouble("latitude");
 		longitude = extras.getDouble("longitude");
 		address = extras.getString("address");
 		lastName = extras.getString("last_name");
 		state = extras.getString("state");
 		committeeId = extras.getString("committeeId");
 		committeeName = extras.getString("committeeName");
 
 		shortcut = extras.getBoolean("shortcut", false);
 
 		LegislatorListHolder holder = (LegislatorListHolder) getLastNonConfigurationInstance();
 		if (holder != null) {
 			legislators = holder.legislators;
 			loadLegislatorsTask = holder.loadLegislatorsTask;
 			shortcutImageTask = holder.shortcutImageTask;
 			loadPhotoTasks = holder.loadPhotoTasks;
 
 			if (loadPhotoTasks != null) {
 				Iterator<LoadPhotoTask> iterator = loadPhotoTasks.values().iterator();
 				while (iterator.hasNext())
 					iterator.next().onScreenLoad(this);
 			}
 
 			locationUpdater = holder.locationUpdater;
 			addressUpdater = holder.addressUpdater;
 			address = holder.address;
 			relocating = holder.relocating;
 		}
 
 		if (loadLegislatorsTask == null && shortcutImageTask == null)
 			loadLegislators();
 		else {
 			if (loadLegislatorsTask != null)
 				loadLegislatorsTask.onScreenLoad(this);
 
 			if (shortcutImageTask != null)
 				shortcutImageTask.onScreenLoad(this);
 		}
 
 		if(locationUpdater == null)
 			locationUpdater = new LocationUpdater(this);
 		else
 			locationUpdater.onScreenLoad(this);
 
 		if(addressUpdater != null)
 			addressUpdater.onScreenLoad(this);
 
 		setupControls();
 
 		if(relocating)
 			toggleRelocating(true);
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		LegislatorListHolder holder = new LegislatorListHolder();
 		holder.legislators = this.legislators;
 		holder.loadLegislatorsTask = this.loadLegislatorsTask;
 		holder.shortcutImageTask = this.shortcutImageTask;
 		holder.loadPhotoTasks = this.loadPhotoTasks;
 
 		holder.addressUpdater = addressUpdater;
 		holder.locationUpdater = locationUpdater;
 		holder.address = address;
 		holder.relocating = relocating;
 		return holder;
 	}
 
 	public void loadLegislators() {
 		if (legislators == null)
 			loadLegislatorsTask = (LoadLegislatorsTask) new LoadLegislatorsTask(this).execute();
 		else
 			displayLegislators();
 	}
 
 	public void displayLegislators() {
 		if (legislators.size() > 0)
 			setListAdapter(new LegislatorAdapter(this, legislators));
 		else {
 			switch (searchType()) {
 			case SEARCH_ZIP:
 				Utils.showBack(this, R.string.empty_zipcode);
 				break;
 			case SEARCH_LOCATION:
 				Utils.showBack(this, R.string.empty_location);
 				break;
 			case SEARCH_LASTNAME:
 				Utils.showBack(this, R.string.empty_last_name);
 				break;
 			default:
 				Utils.showBack(this, R.string.empty_general);
 			}
 		}
 	}
 
 	public void loadPhoto(String bioguide_id) {
 		if (!loadPhotoTasks.containsKey(bioguide_id))
 			loadPhotoTasks.put(bioguide_id, (LoadPhotoTask) new LoadPhotoTask(this, LegislatorImage.PIC_MEDIUM, bioguide_id).execute(bioguide_id));
 	}
 
 	public void onLoadPhoto(Drawable photo, Object tag) {
 		loadPhotoTasks.remove(tag);
 
 		View result = getListView().findViewWithTag(tag);
 		if (result != null) {
 			if (photo != null)
 				((ImageView) result.findViewById(R.id.photo)).setImageDrawable(photo);
 			else // leave as loading, no better solution I can think of right now
 				((ImageView) result.findViewById(R.id.photo)).setImageResource(R.drawable.loading_photo);
 		}
 	}
 
 	public Context getContext() {
 		return this;
 	}
 
 	public void setupControls() {
 		((Button) findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				finish();
 			}
 		});
 
 		Utils.setLoading(this, R.string.legislators_loading);
 		Utils.setTitleSize(this, 20);
 		switch (searchType()) {
 		case SEARCH_ZIP:
 			Utils.setTitle(this, "Legislators For " + zipCode);
 			break;
 		case SEARCH_LOCATION:
 			showHeader(); // make the location update header visible
 			displayAddress(address);
 			Utils.setTitle(this, "Legislators For Your Location");
 			break;
 		case SEARCH_LASTNAME:
 			Utils.setTitle(this, "Legislators Named \"" + lastName + "\"");
 			break;
 		case SEARCH_COMMITTEE:
 			Utils.setTitle(this, committeeName);
 			break;
 		case SEARCH_STATE:
 			Utils.setTitle(this, "Legislators from " + Utils.stateCodeToName(this, state));
 			break;
 		default:
 			Utils.setTitle(this, "Legislator Search");
 		}
 	}
 
 	@Override
 	public void onListItemClick(ListView parent, View v, int position, long id) {
 		selectLegislator((Legislator) parent.getItemAtPosition(position));
 	}
 
 	public void selectLegislator(Legislator legislator) {
 		if (shortcut)
 			shortcutImageTask = (ShortcutImageTask) new ShortcutImageTask(this, legislator).execute();
 		else
 			startActivity(Utils.legislatorIntent(this, legislator));
 	}
 
 	private int searchType() {
 		if (zipSearch())
 			return SEARCH_ZIP;
 		else if (locationSearch())
 			return SEARCH_LOCATION;
 		else if (lastNameSearch())
 			return SEARCH_LASTNAME;
 		else if (stateSearch())
 			return SEARCH_STATE;
 		else if (committeeSearch())
 			return SEARCH_COMMITTEE;
 		else
 			return SEARCH_LOCATION;
 	}
 
 	private boolean zipSearch() {
 		return zipCode != null;
 	}
 
 	private boolean locationSearch() {
 		// sucks for people at the intersection of the equator and prime
 		// meridian
 		return (latitude != 0.0 && longitude != 0.0);
 	}
 
 	private boolean lastNameSearch() {
 		return lastName != null;
 	}
 
 	private boolean stateSearch() {
 		return state != null;
 	}
 
 	private boolean committeeSearch() {
 		return committeeId != null;
 	}
 
 	public void returnShortcutIcon(Legislator legislator, Bitmap icon) {
 		setResult(RESULT_OK, Utils.shortcutIntent(this, legislator, icon));
 		finish();
 	}
 
 	private class LegislatorAdapter extends ArrayAdapter<Legislator> {
 		LayoutInflater inflater;
 		Activity context;
 
 		public LegislatorAdapter(Activity context, ArrayList<Legislator> items) {
 			super(context, 0, items);
 			this.context = context;
 			inflater = LayoutInflater.from(context);
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			Legislator legislator = getItem(position);
 
 			LinearLayout view;
 			if (convertView == null)
 				view = (LinearLayout) inflater.inflate(R.layout.legislator_item, null);
 			else
 				view = (LinearLayout) convertView;
 
 			// used as the hook to get the legislator image in place when it's loaded
 			view.setTag(legislator.bioguide_id);
 
 			((TextView) view.findViewById(R.id.name)).setText(nameFor(legislator));
 			((TextView) view.findViewById(R.id.position)).setText(positionFor(legislator));
 
 			ImageView photoView = (ImageView) view.findViewById(R.id.photo); 
 			BitmapDrawable photo = LegislatorImage.quickGetImage(LegislatorImage.PIC_LARGE, legislator.bioguide_id, context);
 			if (photo != null)
 				photoView.setImageDrawable(photo);
 			else {
 				photoView.setImageResource(R.drawable.loading_photo);
 				LegislatorList.this.loadPhoto(legislator.bioguide_id);
 			}
 
 			return view;
 		}
 
 
 
 		public String nameFor(Legislator legislator) {
 			return legislator.last_name + ", " + legislator.firstName();
 		}
 
 		public String positionFor(Legislator legislator) {
 			String district = legislator.district;
 			String stateName = Utils.stateCodeToName(context, legislator.state);
 
 			if (district.equals("Senior Seat"))
 				return "Senior Senator from " + stateName;
 			else if (district.equals("Junior Seat"))
 				return "Junior Senator from " + stateName;
 			else if (district.equals("0")) {
 				if (legislator.title.equals("Rep"))
 					return "Representative for " + stateName + " At-Large";
 				else
 					return legislator.fullTitle() + " for " + stateName;
 			} else
 				return "Representative for " + stateName + "-" + district;
 		}
 
 	}
 
 	private class ShortcutImageTask extends AsyncTask<Void, Void, Bitmap> {
 		public LegislatorList context;
 		public Legislator legislator;
 		private ProgressDialog dialog;
 
 		public ShortcutImageTask(LegislatorList context, Legislator legislator) {
 			super();
 			this.legislator = legislator;
 			this.context = context;
 			this.context.shortcutImageTask = this;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			loadingDialog();
 		}
 
 		public void onScreenLoad(LegislatorList context) {
 			this.context = context;
 			loadingDialog();
 		}
 
 		@Override
 		protected Bitmap doInBackground(Void... nothing) {
 			return LegislatorImage.shortcutImage(legislator.bioguide_id, context);
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap shortcutIcon) {
 			if (dialog != null && dialog.isShowing())
 				dialog.dismiss();
 
 			context.returnShortcutIcon(legislator, shortcutIcon);
 
 			context.shortcutImageTask = null;
 		}
 
 		public void loadingDialog() {
 			dialog = new ProgressDialog(context);
 			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			dialog.setMessage("Creating shortcut...");
 
 			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
 				public void onCancel(DialogInterface dialog) {
 					cancel(true);
 					context.finish();
 				}
 			});
 
 			dialog.show();
 		}
 	}
 
 	private class LoadLegislatorsTask extends AsyncTask<Void, Void, ArrayList<Legislator>> {
 		public LegislatorList context;
 
 		public LoadLegislatorsTask(LegislatorList context) {
 			super();
 			this.context = context;
 		}
 
 		public void onScreenLoad(LegislatorList context) {
 			this.context = context;
 		}
 
 		@Override
 		protected ArrayList<Legislator> doInBackground(Void... nothing) {
 			ArrayList<Legislator> legislators = new ArrayList<Legislator>();
 			ArrayList<Legislator> lower = new ArrayList<Legislator>();
 
 			ArrayList<Legislator> temp;
 			try {
 				switch (searchType()) {
 				case SEARCH_ZIP:
 					temp = Legislator.allForZipCode(zipCode);
 					break;
 				case SEARCH_LOCATION:
 					temp = Legislator.allForLatLong(latitude, longitude);
 					break;
 				case SEARCH_LASTNAME:
 					temp = Legislator.allWhere("lastname__istartswith", lastName);
 					break;
 				case SEARCH_COMMITTEE:
 					temp = Committee.find(committeeId).members;
 					break;
 				case SEARCH_STATE:
 					temp = Legislator.allWhere("state", state);
 					break;
 				default:
 					return legislators;
 				}
 
 				// sort legislators Senators-first
 				for (int i = 0; i < temp.size(); i++) {
 					if (temp.get(i).title.equals("Sen"))
 						legislators.add(temp.get(i));
 					else
 						lower.add(temp.get(i));
 				}
 				Collections.sort(legislators);
 				Collections.sort(lower);
 				legislators.addAll(lower);
 
 				return legislators;
 
 			} catch (CongressException exception) {
 				return legislators;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(ArrayList<Legislator> legislators) {
 			context.legislators = legislators;
 			context.displayLegislators();
 			context.loadLegislatorsTask = null;
 		}
 	}
 
 	static class LegislatorListHolder {
 		ArrayList<Legislator> legislators;
 		LoadLegislatorsTask loadLegislatorsTask;
 		ShortcutImageTask shortcutImageTask;
 		HashMap<String,LoadPhotoTask> loadPhotoTasks;
 
 		AddressUpdater addressUpdater;
 		LocationUpdater locationUpdater;
 		String address;
 		boolean relocating;
 	}
 
 	private class HeaderViewWrapper {
 		private TextView txt;
 		private View base;
 
 		public HeaderViewWrapper(View base) {
 			this.base = base;
 		}
 		public View getBase() {
 			return base;
 		}
 		public TextView getTxt() {
 			return (txt == null ? txt = (TextView) base.findViewById(R.id.text_2) : txt);
 		}
 	}
 
 	private void showHeader() {
 		headerWrapper = new HeaderViewWrapper(findViewById(R.id.list_header));
 		headerWrapper.getBase().setVisibility(View.VISIBLE);
 		((TextView) headerWrapper.getBase().findViewById(R.id.text_1)).setText(R.string.location_not_accurate);
 		TextView txt = headerWrapper.getTxt();
 		txt.setText(R.string.update);
 		txt.setClickable(true);
 		txt.setFocusable(true);
 		txt.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				updateLocation();
 			}
 		});
 	}
 
 	private void toggleRelocating(boolean updating) {
 		headerWrapper.getTxt().setText(updating ? R.string.updating : R.string.update);
 		headerWrapper.getTxt().setEnabled(updating ? false : true);
 	}
 
 	private void updateLocation() {
 		relocating = true;
 		locationUpdater.requestLocationUpdate();
 		toggleRelocating(true);
 	}
 
 	private void displayAddress(String address) {
 		Utils.setTitle(this, "Legislators For " + address == null || address.equals("") ? "Your Location" : address);
 	}
 
 	private void reloadLegislators() {
 		legislators = null;
 		loadLegislators();
 	}
 
 	public void onLocationUpdate(Location location) {
 		toggleRelocating(false);
 		latitude = location.getLatitude();
 		longitude = location.getLongitude();
 		addressUpdater = (AddressUpdater) new AddressUpdater(this).execute(location);
 	}
 
 	public void onLocationUpdateError(CongressException e) {
 		toggleRelocating(false);
 		relocating = false;
 		Toast.makeText(this, R.string.location_update_fail, Toast.LENGTH_SHORT).show();		
 	}
 
 	public void onAddressUpdate(String address) {
 		this.address = address;
 		displayAddress(address);
 
 		addressUpdater = null;
 		toggleRelocating(false);
 		relocating = false;
 
 		reloadLegislators();
 	}
 
 	public void onAddressUpdateError(CongressException e) {
		this.address = getString(R.string.location_update_fail);
 		displayAddress(address);
 
 		addressUpdater = null;
 		toggleRelocating(false);
 		relocating = false;
 	}
 }
