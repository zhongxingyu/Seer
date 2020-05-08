 package reporter66.ru;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.TypedArray;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.Gallery;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class ReporterActivity extends Activity implements LocationListener {
 
 	// geo
 	private LocationManager locationManager;
 	private String provider;
 	private double longitude;
 	private double latitute;
 	// elements
 	private Button submit;
 	private ProgressDialog dialog;
 	private ImageButton add_photo;
 	// media
 	private ImageAdapter imageAdapter;
 	private Gallery gallery;
 	private List<Uri> galleryItems = new ArrayList<Uri>();
 
 	// Called at the start of the full lifetime.
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.form);
 		// Initialize activity.
 
 		gallery = (Gallery) findViewById(R.id.gallery);
 		imageAdapter = new ImageAdapter(this);
 		gallery.setAdapter(imageAdapter);
 
 		gallery.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView parent, View v, int position,
 					long id) {
 				onGalleryItemClick(position);
 			}
 		});
 
 		submit = (Button) findViewById(R.id.submit);
 		submit.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				onSubmit();
 			}
 		});
 
 		add_photo = (ImageButton) findViewById(R.id.add_photo);
 		add_photo.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				onAppend();
 			}
 		});
 
 	}
 
 	// data submit
 	protected void onSubmit() {
 		CheckBox add_geo = (CheckBox) findViewById(R.id.add_geo);
 		if (add_geo.isChecked()) {
 			dialog = ProgressDialog.show(ReporterActivity.this, "",
 					"Устанавливаем связь с космосом...", true);
 			provider = getProvider();
 			if (provider != null) {
 				locationManager.requestLocationUpdates(provider, 400, 1, this);
 				Location location = locationManager
 						.getLastKnownLocation(provider);
 
 				if (location != null) {
 					System.out.println("Provider " + provider
 							+ " has been selected.");
 					latitute = location.getLatitude();
 					longitude = location.getLongitude();
 					Toast.makeText(
 							ReporterActivity.this,
 							"Связь со спутниками установлена, ваши координаты: lat: "
 									+ latitute + ", lng: " + longitude,
 							Toast.LENGTH_SHORT).show();
 					dialog.setMessage("Связь со спутниками установлена, ваши координаты: lat: "
 							+ latitute + ", lng: " + longitude);
 
 				} else {
 					Toast.makeText(ReporterActivity.this,
 							"Не удалось найти ваше местоположение",
 							Toast.LENGTH_SHORT).show();
 				}
 			} else {
 				Toast.makeText(ReporterActivity.this,
 						"Не удалось найти ваше местоположение, включите GPS",
 						Toast.LENGTH_SHORT).show();
 			}
 
 			dialog.dismiss();
 		}
 	}
 
 	// select intents for media append
 	protected void onAppend() {
 		final CharSequence[] items = { "Фото из галереи"/*, "Открыть камеру"*/ };
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Добавить:");
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				switch (item) {
 				case 0:
 					Intent intent = new Intent();
 					intent.setType("image/*");
 					intent.setAction(Intent.ACTION_GET_CONTENT);
 					startActivityForResult(Intent.createChooser(intent,
 							"Выберите изображение"), 1);
 					break;
 				case 1:
 					Intent i = new Intent(
 							android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 					if (hasImageCaptureBug()) {
 						i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
 								Uri.fromFile(new File("/sdcard/tmp")));
 					} else {
 						i.putExtra(
 								android.provider.MediaStore.EXTRA_OUTPUT,
 								android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 					}
 					startActivityForResult(i, 2);
 					break;
 				}
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	protected void onGalleryItemClick(final int position) {
 		final CharSequence[] items = { "Открыть", "Удалить", "Отмена" };
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Действия:");
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				switch (item) {
 				case 0:
 					Intent intent = new Intent();
 					intent.setAction(android.content.Intent.ACTION_VIEW);
 					intent.setDataAndType(galleryItems.get(position), "image/*");
 					startActivity(intent);
 					break;
 				case 1:
 					GalleryItemRemove(position);
 					break;
 				}
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	protected void GalleryItemRemove(int position) {
 		galleryItems.remove(position);
 		if (galleryItems.size() < 1) {
 			gallery.setVisibility(View.GONE);
 		} else {
			imageAdapter.checkUi();
			//gallery.setAdapter(imageAdapter);
 		}
 		Toast.makeText(ReporterActivity.this, "Удалено", Toast.LENGTH_SHORT)
 				.show();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case 1:
 			if (resultCode == Activity.RESULT_OK) {
 				Uri selectedImageUri = data.getData();
 				galleryItems.add(selectedImageUri);
				//gallery.setAdapter(imageAdapter);
				imageAdapter.checkUi();
 				gallery.setVisibility(View.VISIBLE);
 			}
 			break;
 		case 2:
 			Uri u;
 			if (hasImageCaptureBug()) {
 				File fi = new File("/sdcard/tmp");
 				try {
 					u = Uri.parse(android.provider.MediaStore.Images.Media
 							.insertImage(getContentResolver(),
 									fi.getAbsolutePath(), null, null));
 					if (!fi.delete()) {
 						Log.i("logMarker", "Failed to delete " + fi);
 					}
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 			} else {
 				u = data.getData();
 				Toast.makeText(ReporterActivity.this, "uri is:" + u.toString(), Toast.LENGTH_LONG).show();
 				Log.i("logMarker", "File found " + u.toString());
 				
 				galleryItems.add(u);
				//gallery.setAdapter(imageAdapter);
				imageAdapter.checkUi();
 				gallery.setVisibility(View.VISIBLE);
 			}
 			break;
 		}
 	}
 
 	// Called after onCreate has finished, use to restore UI state
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		// Restore UI state from the savedInstanceState.
 		// This bundle has also been passed to onCreate.
 	}
 
 	// Called before subsequent visible lifetimes
 	// for an activity process.
 	@Override
 	public void onRestart() {
 		super.onRestart();
 		// Load changes knowing that the activity has already
 		// been visible within this process.
 	}
 
 	// Called at the start of the visible lifetime.
 	@Override
 	public void onStart() {
 		super.onStart();
 		// Apply any required UI change now that the Activity is visible.
 	}
 
 	// Called at the start of the active lifetime.
 	@Override
 	public void onResume() {
 		super.onResume();
 		provider = getProvider();
 		// Resume any paused UI updates, threads, or processes required
 		// by the activity but suspended when it was inactive.
 	}
 
 	// Called to save UI state changes at the
 	// end of the active lifecycle.
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		// Save UI state changes to the savedInstanceState.
 		// This bundle will be passed to onCreate if the process is
 		// killed and restarted.
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
 	// Called at the end of the active lifetime.
 	@Override
 	public void onPause() {
 		// Suspend UI updates, threads, or CPU intensive processes
 		// that don’t need to be updated when the Activity isn’t
 		// the active foreground activity.
 		super.onPause();
 		locationManager.removeUpdates(this);
 	}
 
 	protected String getProvider() {
 		if (locationManager == null)
 			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		return locationManager.getBestProvider(criteria, true);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		latitute = location.getLatitude();
 		longitude = location.getLongitude();
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		Toast.makeText(this, "Disabled provider " + provider,
 				Toast.LENGTH_SHORT).show();
 	}
 
 	// Called at the end of the visible lifetime.
 	@Override
 	public void onStop() {
 		// Suspend remaining UI updates, threads, or processing
 		// that aren’t required when the Activity isn’t visible.
 		// Persist all edits or state changes
 		// as after this call the process is likely to be killed.
 		super.onStop();
 	}
 
 	// Called at the end of the full lifetime.
 	@Override
 	public void onDestroy() {
 		// Clean up any resources including ending threads,
 		// closing database connections etc.
 		super.onDestroy();
 	}
 
 	static final private int MENU_EXIT = Menu.FIRST;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 /*		// Group ID
 		int groupId = 0;
 		// Unique menu item identifier. Used for event handling.
 		int menuItemId = MENU_EXIT;
 		// The order position of the item
 		int menuItemOrder = Menu.NONE;
 		// Text to be displayed for this menu item.
 		int menuItemText = R.string.menu_exit;
 		// Create the menu item and keep a reference to it.
 		MenuItem menuItem = menu.add(groupId, menuItemId, menuItemOrder,
 				menuItemText);
 
 		menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			public boolean onMenuItemClick(MenuItem _menuItem) {
 				
 				return true;
 			}
 		});*/
 
 		return true;
 	}
 
 	public class ImageAdapter extends BaseAdapter {
 		int mGalleryItemBackground;
 		private Context mContext;
 
 		public ImageAdapter(Context c) {
 			mContext = c;
 			TypedArray attr = mContext
 					.obtainStyledAttributes(R.styleable.DefTheme);
 			mGalleryItemBackground = attr.getResourceId(
 					R.styleable.DefTheme_android_galleryItemBackground, 0);
 			attr.recycle();
 		}
 
 		public int getCount() {
 			return galleryItems.size();
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
		public void checkUi(){
			notifyDataSetChanged();
		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ImageView imageView = new ImageView(mContext);
 
 			Uri uri = galleryItems.get(position);
 
 			imageView.setImageURI(uri);
 			imageView.setLayoutParams(new Gallery.LayoutParams(150, -1));
 			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
 			imageView.setAdjustViewBounds(true);
 			imageView.setBackgroundResource(mGalleryItemBackground);
 
 			return imageView;
 		}
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public boolean hasImageCaptureBug() {
 
 		// list of known devices that have the bug
 		ArrayList<String> devices = new ArrayList<String>();
 		devices.add("android-devphone1/dream_devphone/dream");
 		devices.add("generic/sdk/generic");
 		devices.add("vodafone/vfpioneer/sapphire");
 		devices.add("tmobile/kila/dream");
 		devices.add("verizon/voles/sholes");
 		devices.add("google_ion/google_ion/sapphire");
 
 		return devices.contains(android.os.Build.BRAND + "/"
 				+ android.os.Build.PRODUCT + "/" + android.os.Build.DEVICE);
 
 	}
 
 }
