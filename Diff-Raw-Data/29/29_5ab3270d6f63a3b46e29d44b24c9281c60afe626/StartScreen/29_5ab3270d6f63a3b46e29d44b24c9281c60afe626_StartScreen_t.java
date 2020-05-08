 package edu.berkeley.cs160.smartnature;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.AlertDialog;
 import android.app.Application;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 class GardenGnome extends Application {
 	private static ArrayList<Garden> gardens = new ArrayList<Garden>();
 	private static DatabaseHelper dbHelper;
 	public static int garden_id;
 	public static int plot_id;
 	public static int plant_id;
 	public static int entry_id;
 	public static int photo_id;
 	
 	public static ArrayList<Garden> getGardens() { return gardens; }
 	
 	public static Garden getGarden(int index) { return gardens.get(index); }
 	
 	public static int indexOf(Garden garden) { return gardens.indexOf(garden); }
 	
 	public static void init(Context context) {
 		if (dbHelper != null)
 			return;
 		dbHelper = new DatabaseHelper(context);
 		gardens.addAll(dbHelper.selectGardens());
 		for (Garden garden : gardens) {
 			loadPhotos(garden);
 			loadPlots(garden);
 		}
 	}
 	
 	public static void loadPhotos(Garden garden) {
 		ArrayList<Photo> photos = dbHelper.selectPhotos(garden);
 		garden.getPhotos().addAll(photos);
 	}
 	
 	public static void loadPlots(Garden garden) {
 		ArrayList<Plot> plots = dbHelper.selectPlots(garden);
 		for (Plot plot : plots)
 			loadPlants(plot);
 		
 		garden.getPlots().addAll(plots);
 	}
 	
 	public static void loadPlants(Plot plot) {
 		ArrayList<Plant> plants = dbHelper.selectPlants(plot);
 		for (Plant plant : plants)
 			loadEntries(plant);
 		
 		plot.getPlants().addAll(plants);
 	}
 	
 	public static void loadEntries(Plant plant) {
 		ArrayList<Entry> entries = dbHelper.selectEntries(plant);
 		plant.getEntries().addAll(entries);
 	}
 	
 	/** adds entire garden info to database */
 	public static void addServerGarden(Garden garden) {
 		addGarden(garden);
 		
 		for (Photo photo : garden.getPhotos())
 			dbHelper.insertPhoto(garden, photo);
 		
 		for (Plot plot : garden.getPlots()) {
 			dbHelper.insertPlot(garden, plot);
 			for (Plant plant : plot.getPlants()) {
 				dbHelper.insertPlant(plot, plant);
 				for (Entry entry : plant.getEntries())
 					dbHelper.insertEntry(plant, entry);
 			}
 		}
 	}
 	
 	public static void addGarden(Garden garden) {
 		dbHelper.insertGarden(garden);
 		gardens.add(garden);
 		System.out.println("garden_id= " + garden.getId());
 	}
 	
 	public static void addPhoto(Garden garden, Photo photo) {
 		dbHelper.insertPhoto(garden, photo);
 		garden.addPhoto(photo);
 		System.out.println("photo_id= " + photo.getId());
 	}
 	
 	public static void addPlot(Garden garden, Plot plot) {
 		dbHelper.insertPlot(garden, plot);
 		garden.addPlot(plot);
 		System.out.println("plot_id= " + plot.getId());
 	}
 	
 	public static void addPlant(Plot plot, Plant plant) {
 		dbHelper.insertPlant(plot, plant);
 		plot.addPlant(plant);
 		System.out.println("plant_id= " + plant.getId());
 	}
 	
 	public static void addEntry(Plant plant, Entry entry) {
 		dbHelper.insertEntry(plant, entry);
 		plant.addEntry(entry);
 		System.out.println("entry_id= " + entry.getId());
 	}
 	
 	public static void removeGarden(int index) { removeGarden(gardens.get(index)); }
 	
 	public static void removeGarden(Garden garden) {
 		dbHelper.deleteGarden(garden);
 		gardens.remove(garden);
 	}
 	
 	public static void removePlot(Garden garden, Plot plot) {
 		dbHelper.deletePlot(plot);
 		garden.getPlots().remove(plot);
 	}
 	
 	public static void removePlant(Plot plot, Plant plant) {
 		dbHelper.deletePlant(plant);
 		plot.getPlants().remove(plant);
 	}
 	
 	public static void removeEntry(Plant plant, Entry entry) {
 		dbHelper.deleteEntry(entry);
 		plant.getEntries().remove(entry);
 	}
 	
 	public static void updateGarden(Garden garden) { dbHelper.updateGarden(garden); }
 	
 	public static void updatePhoto(Photo photo) { dbHelper.updatePhoto(photo); }
 	
 	public static void updatePlot(Plot plot) { dbHelper.updatePlot(plot); }
 	
 	public static void updatePlant(Plant plant) { dbHelper.updatePlant(plant); }
 	
 	public static void updateEntry(Entry entry) { dbHelper.updateEntry(entry); }
 	
 }
 
 public class StartScreen extends ListActivity implements DialogInterface.OnClickListener, View.OnClickListener, AdapterView.OnItemClickListener {
 
 	static GardenAdapter adapter;
 	ArrayList<Garden> gardens = GardenGnome.getGardens();
 	AlertDialog dialog;
 	View textEntryView;
 
 	LocationManager lm;
 	Geocoder geocoder;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		geocoder = new Geocoder(this, Locale.getDefault());
 		GardenGnome.init(this);
 		
 		adapter = new GardenAdapter(this, R.layout.garden_list_item, gardens);
 		setListAdapter(adapter);
 		registerForContextMenu(getListView());
 		getListView().setOnItemClickListener(this);
 		findViewById(R.id.new_garden).setOnClickListener(this);
 		findViewById(R.id.search_encyclopedia).setOnClickListener(this);
 		findViewById(R.id.find_garden).setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.new_garden:
 			showDialog(0);
 			break;
 		case R.id.search_encyclopedia:
 			startActivity(new Intent(this, Encyclopedia.class));
 			break;
 		case R.id.find_garden:
 			startActivityForResult(new Intent(this, FindGarden.class), 0);
 			break;
 		}
 	}
 
 	@Override
 	public Dialog onCreateDialog(int id) {
 		textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry_dialog, null);
 		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(textEntryView);
 
 		dialog = builder.setTitle(R.string.new_garden_prompt)
 		.setPositiveButton(R.string.alert_dialog_ok, this)
 		.setNegativeButton(R.string.alert_dialog_cancel, null) // this means cancel was pressed
 		.create();
 
 		// automatically show soft keyboard
 		EditText input = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
 		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (hasFocus)
 					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
 			}
 		});
 
 		input.setOnKeyListener(new View.OnKeyListener() {
 			@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
 				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
 					InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
 					mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
 					return true;
 				}
 				return false;
 			}
 		});
 
 		return dialog;
 	}
 
 	public void onClick(DialogInterface dialog, int whichButton) {
 		EditText textEntry = ((EditText) textEntryView.findViewById(R.id.dialog_text_entry));
 		String gardenName = textEntry.getText().toString().trim();
 		if (gardenName.length() == 0)
 			gardenName = "Untitled garden";
 		Intent intent = new Intent(this, GardenScreen.class);
 		intent.putExtra("garden_id", gardens.size());
 		Garden garden = new Garden(gardenName);
 		GardenGnome.addGarden(garden);
 		adapter.notifyDataSetChanged();
 		startActivityForResult(intent, 0);
 		new Thread(setLocation).start();
 		removeDialog(0);
 	}
 
 	/** sets garden location using user's physical location */
 	Runnable setLocation = new Runnable() {
 		@Override
 		public void run() {
 			Garden garden = gardens.get(gardens.size() - 1);
 			List<String> providers = lm.getProviders(false);
 			if (providers.isEmpty())
 				return;
 			
 			Location loc = lm.getLastKnownLocation(providers.get(0));
 			List<Address> addresses = new ArrayList<Address>();
 			try {
 				addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
 			} catch (Exception e) { e.printStackTrace(); }
 			if (!addresses.isEmpty()) {
 				Address addr = addresses.get(0);
 				System.out.println(addr.getLocality() + "," + addr.getAdminArea() + "," + addr.getCountryCode());
 				garden.setCity(addr.getLocality());
 				if (addr.getAdminArea() != null)
 					garden.setState(addr.getAdminArea());
 				else
 					garden.setState(addr.getCountryCode());
 			}
 		}
 	};
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		adapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Intent intent = new Intent(this, GardenScreen.class);
 		//System.out.println("clicked garden_index=" + gardens.get(position).getId());
 		intent.putExtra("garden_index", position);
 		startActivityForResult(intent, 0);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.m_contact:
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 			intent.setData(Uri.parse("mailto:" + getString(R.string.dev_email)));
 			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GardenGnome feedback");
 			startActivity(intent); //startActivity(Intent.createChooser(intent, "Send mail..."));
 			break;
 		case R.id.m_globaloptions:
 			startActivity(new Intent(this, GlobalSettings.class));
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, view, menuInfo);
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		menu.setHeaderTitle(gardens.get(info.position).getName());
 		menu.add(Menu.NONE, 0, Menu.NONE, "Edit info"); //menu.add(Menu.NONE).setNumericShortcut(1);
 		menu.add(Menu.NONE, 1, Menu.NONE, "Delete"); //menu.add("Delete").setNumericShortcut(2);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 		case 0:
 			Intent intent = new Intent(this, GardenAttr.class).putExtra("garden_id", info.position);
 			startActivityForResult(intent, 0);
 			break;
 		case 1:
 			GardenGnome.removeGarden(info.position);
 			adapter.notifyDataSetChanged();
 			break;
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	class GardenAdapter extends ArrayAdapter<Garden> {
 		private ArrayList<Garden> gardens;
 		private LayoutInflater li;
 
 		public GardenAdapter(Context context, int textViewResourceId, ArrayList<Garden> items) {
 			super(context, textViewResourceId, items);
 			li = ((ListActivity) context).getLayoutInflater();
 			gardens = items;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = convertView;
 			if (view == null)
 				view = li.inflate(R.layout.garden_list_item, null);
 			Garden garden = gardens.get(position);
 			((TextView) view.findViewById(R.id.garden_name)).setText(garden.getName());
 			ImageView image = (ImageView) view.findViewById(R.id.preview_img); 
 			if (garden.getPhotos().isEmpty())
 				image.setImageResource(R.drawable.preview);
 			else {
 				Uri preview = garden.getPreview();
 				
 				BitmapFactory.Options options = new BitmapFactory.Options();
				options.outWidth = (int) getResources().getDimension(R.dimen.preview_width);
				options.outHeight = (int) getResources().getDimension(R.dimen.preview_height);
				options.inSampleSize = Helper.getSampleSize(StartScreen.this, preview, getResources().getDimension(R.dimen.preview_width));
				if (options.inSampleSize > 7)
					options.inSampleSize /= 8;
 				//DisplayMetrics metrics = new DisplayMetrics();
 				//getWindowManager().getDefaultDisplay().getMetrics(metrics);
 				//options.inTargetDensity = metrics.densityDpi;
 				
 				if (preview.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
 					try {
 						InputStream stream = getContentResolver().openInputStream(preview);
 						image.setImageBitmap(BitmapFactory.decodeStream(stream, null, options));
 						stream.close();
 					} catch (Exception e) { e.printStackTrace(); }
 				}
 				else {
 					System.out.println(preview.getPath());
 					image.setImageBitmap(BitmapFactory.decodeFile(preview.getPath(), options));
 				}
 			}
 			return view;
 		}
 	}
 }
