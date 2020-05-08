 /*
  * This file is part of the Lokalizator grobów project.
  *
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation v3; 
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  */
 
 package pl.itiner.nutiteq;
 
 import static android.provider.BaseColumns._ID;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_CEMENTERY_ID;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_DATE_BIRTH;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_DATE_BURIAL;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_DATE_DEATH;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_FIELD;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_LAT;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_LON;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_NAME;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_PLACE;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_QUARTER;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_ROW;
 import static pl.itiner.db.GraveFinderProvider.Columns.COLUMN_SURENAME;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import pl.itiner.commons.Commons;
 import pl.itiner.db.DepartedCursor;
 import pl.itiner.db.GraveFinderProvider;
 import pl.itiner.grave.R;
 import pl.itiner.model.Departed;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.drawable.BitmapDrawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.view.View;
 import android.widget.TextView;
import android.widget.Toast;
 import android.widget.ZoomControls;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.mgmaps.utils.Tools;
 import com.nutiteq.BasicMapComponent;
 import com.nutiteq.android.MapView;
 import com.nutiteq.cache.Cache;
 import com.nutiteq.cache.CachingChain;
 import com.nutiteq.cache.MemoryCache;
 import com.nutiteq.components.MapTile;
 import com.nutiteq.components.OnMapElement;
 import com.nutiteq.components.Place;
 import com.nutiteq.components.WgsPoint;
 import com.nutiteq.listeners.OnMapElementListener;
 import com.nutiteq.maps.GeoMap;
 import com.nutiteq.maps.MapTileOverlay;
 import com.nutiteq.ui.ThreadDrivenPanning;
 import com.nutiteq.wrappers.AppContext;
 import com.nutiteq.wrappers.Image;
 
 public class NutiteqMap extends SherlockFragmentActivity implements
 		LoaderCallbacks<Cursor> {
	private static final int FILE_CACHE_SIZE = 5 * 1024 * 1024; // I assume that
																// it is in
																// bytes no docs
																// from nutiteq
	private static final int MEM_CACHE_SIZE = 1024 * 1024; // I assume that it
															// is in bytes no
															// docs from nutiteq
 	public static final String DEPARTED_ID_BUND = "DEPARTED_ID_BUND";
 	private BasicMapComponent mapComponent;
 	private GeoMap map;
 	private boolean onRetainCalled;
 	private Image gps;
 	private WgsPoint userLocation;
 	private Place userPlace;
 	private LocationManager locManager;
 	private LocationListener locListener;
 
 	private OnMapElementListener elemListener = new OnMapElementListener() {
 
 		@Override
 		public void elementLeft(OnMapElement arg0) {
 		}
 
 		@Override
 		public void elementEntered(OnMapElement arg0) {
 		}
 
 		@Override
 		public void elementClicked(OnMapElement mapElem) {
 			BalloonLabel tmp = (BalloonLabel) mapElem.getLabel();
 			if (tmp.name.equals(getResources().getString(R.string.grave))) {
 				mapComponent.setMiddlePoint(mapElem.getPoints()[0]);
 				mapComponent.zoomIn();
 			}
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		onRetainCalled = false;
 		setContentView(R.layout.nutiteq);
 		gps = new Image(BitmapFactory.decodeResource(getResources(),
 				R.drawable.dot));
 		// Get death person data
 		getSupportLoaderManager().initLoader(0, getIntent().getExtras(), this);
 		// setup position listeners
 		locListener = new NutiteqLocationListener();
 		locManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
		try {
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
					20, locListener);
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, getText(R.string.warning_turn_on_gps),
					Toast.LENGTH_LONG).show();
		}
 		// setup map
 		mapComponent = (BasicMapComponent) getLastCustomNonConfigurationInstance();
 		if (mapComponent == null) {
 			createMapComponent();
 		}
 		MapView mapView = (MapView) findViewById(R.id.nutiteq_mapview);
 		mapView.setMapComponent(mapComponent);
 		setupZoom();
 	}
 
 	private void createMapComponent() {
 		final Resources res = getResources();
 		final int initialZoom = res.getInteger(R.integer.initial_zoom);
 		final String mapKey = res.getString(R.string.nutiteq_key);
 		final WgsPoint center = new WgsPoint(Double.parseDouble(res
 				.getString(R.string.poznan_centre_lon)), Double.parseDouble(res
 				.getString(R.string.poznan_centre_lat)));
 		final BalloonLabel userLocationLabel = new BalloonLabel(
 				res.getString(R.string.your_location), "");
 
 		mapComponent = new BasicMapComponent(mapKey, new AppContext(this), 1,
 				1, center, initialZoom);
 		final MemoryCache memoryCache = new MemoryCache(MEM_CACHE_SIZE);
 		final File cacheDir = getCacheDir();
 		if (!cacheDir.exists()) {
 			cacheDir.mkdir();
 		}
		final FileCache fileSystemCache = new FileCache(this, "network_cache",
				cacheDir, FILE_CACHE_SIZE);
 		mapComponent.setNetworkCache(new CachingChain(new Cache[] {
 				memoryCache, fileSystemCache }));
 		map = getMap();
 		mapComponent.setMap(map);
 		mapComponent.setSmoothZoom(true);
 		mapComponent.setPanningStrategy(new ThreadDrivenPanning());
 		mapComponent.startMapping();
 
 		userPlace = new Place(0, userLocationLabel, gps, userLocation);
 		mapComponent.setOnMapElementListener(elemListener);
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		locManager.removeUpdates(locListener);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
 				locListener);
 	}
 
 	private void placeGravePin(Departed d) {
 		final Resources res = getResources();
 		final Image graveImg = new Image(BitmapFactory.decodeResource(
 				getResources(), R.drawable.graveloc));
 		final BalloonLabel graveLocationLabel = new BalloonLabel(
 				res.getString(R.string.grave), res.getString(R.string.zoom_map));
 		final WgsPoint graveLoc = new WgsPoint(d.getLocation().getLongitude(),
 				d.getLocation().getLatitude());
 		final Place gravePlace = new Place(0, graveLocationLabel, graveImg,
 				graveLoc);
 		mapComponent.addPlace(gravePlace);
 	}
 
 	private void fillHeaderWithData(Departed departed) {
 		final TextView mapSurnameName;
 		final TextView mapBirthDate;
 		final TextView mapDeathDate;
 		final TextView mapFunrealDate;
 		final TextView mapCementry;
 		final TextView mapRow;
 		final TextView mapQuater;
 		final TextView mapField;
 		final String[] cementeries = getResources().getStringArray(
 				R.array.necropolises);
 
 		mapSurnameName = (TextView) findViewById(R.id.map_surname_name);
 		mapSurnameName
 				.setText(Commons.capitalizeFirstLetter(departed.getName())
 						+ " "
 						+ Commons.capitalizeFirstLetter(departed.getSurname()));
 
 		mapBirthDate = (TextView) findViewById(R.id.map_value_dateBirth);
 		mapBirthDate.setText(formatDate(departed.getBirthDate()));
 
 		mapDeathDate = (TextView) findViewById(R.id.map_value_dateDeath);
 		mapDeathDate.setText(formatDate(departed.getDeathDate()));
 
 		mapFunrealDate = (TextView) findViewById(R.id.map_value_dateFunreal);
 		mapFunrealDate.setText(formatDate(departed.getBurialDate()));
 
 		mapField = (TextView) findViewById(R.id.map_field_value);
 		mapField.setText(departed.getField());
 
 		mapRow = (TextView) findViewById(R.id.map_row_value);
 		mapRow.setText(departed.getRow());
 
 		mapQuater = (TextView) findViewById(R.id.map_quater_value);
 		mapQuater.setText(departed.getQuater());
 
 		mapCementry = (TextView) findViewById(R.id.map_value_cementry);
 		String cm_name = cementeries[(int) departed.getCmId()];
 		mapCementry.setText(cm_name);
 	}
 
 	private static final SimpleDateFormat f = new SimpleDateFormat(
 			"dd.MM.yyyy", Locale.getDefault());
 
 	private static String formatDate(Date d) {
 		if (d == null) {
 			return null;
 		}
 		return f.format(d);
 	}
 
 	private void setupZoom() {
 		ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoomcontrols);
 		zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
 			public void onClick(final View v) {
 				mapComponent.zoomIn();
 			}
 		});
 		zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
 			public void onClick(final View v) {
 				mapComponent.zoomOut();
 			}
 		});
 	}
 
 	private PoznanAPIMap getMap() {
 		Resources res = getResources();
 		final String baseUrl = res.getString(R.string.base_url);
 		final int tileSize = res.getInteger(R.integer.tile_size);
 		final double[] resolutions = createResolutionsArray(res
 				.getStringArray(R.array.resolutions));
 		final int minZoom = res.getInteger(R.integer.min_zoom);
 		final int maxZoom = minZoom + resolutions.length - 1;
 		final String layerName = res.getString(R.string.base_layer_name);
 		final String gravesLayer = res.getString(R.string.graves_layer_name);
 		final String imageType = res.getString(R.string.png_type);
 		final String getString = res.getString(R.string.map_get_string);
 		final String copyrightTxt = res.getString(R.string.map_copyright_txt);
 		final int minEpsgX = res.getInteger(R.integer.epsg_min_x);
 		final int minEpsgY = res.getInteger(R.integer.epsg_min_y);
 
 		PoznanAPIMap mainMap = new PoznanAPIMap(baseUrl, tileSize, minZoom,
 				maxZoom, layerName, imageType, "", getString, copyrightTxt,
 				resolutions, minEpsgX, minEpsgY);
 		mainMap.setMissingTileImage(Image.createImage(createMissingTileBitmap(
 				mainMap.getTileSize(), getString(R.string.map_no_connection),
 				getResources())));
 		mainMap.addTileOverlay(new MapTileOverlay() {
 			@Override
 			public String getOverlayTileUrl(MapTile tile) {
 				String url = tile.getIDString();
 				url = url.replaceFirst(Tools.urlEncode(layerName),
 						Tools.urlEncode(gravesLayer));
 				return url;
 			}
 		});
 
 		return mainMap;
 	}
 
 	private static double[] createResolutionsArray(String[] stringArray) {
 		double[] arr = new double[stringArray.length];
 		for (int i = 0; i < stringArray.length; i++) {
 			arr[i] = Double.parseDouble(stringArray[i]);
 		}
 		return arr;
 	}
 
 	@Override
 	public Object onRetainCustomNonConfigurationInstance() {
 		onRetainCalled = true;
 		return mapComponent;
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (!onRetainCalled) {
 			mapComponent.stopMapping();
 		}
 	}
 
 	private class NutiteqLocationListener implements LocationListener {
 		private BalloonLabel userLocationLabel = new BalloonLabel(
 				getResources().getString(R.string.your_location), "");
 
 		@Override
 		public void onLocationChanged(Location loc) {
 			userLocation = new WgsPoint(loc.getLongitude(), loc.getLatitude());
 			if (userLocation != null) {
 				try {
 					mapComponent.removePlace(userPlace);
 				} catch (NullPointerException nue) { /* ignored */
 				}
 				userPlace = new Place(0, userLocationLabel, gps, userLocation);
 				mapComponent.addPlace(userPlace);
 			}
 		}
 
 		@Override
 		public void onProviderDisabled(String arg0) {
 		}
 
 		@Override
 		public void onProviderEnabled(String arg0) {
 		}
 
 		@Override
 		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		}
 
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int loaderId, Bundle b) {
 		long id = b.getLong(DEPARTED_ID_BUND);
 		final Uri uri = ContentUris.withAppendedId(
 				GraveFinderProvider.CONTENT_URI, id);
 		return new CursorLoader(this, uri, new String[] { COLUMN_CEMENTERY_ID,
 				COLUMN_DATE_BIRTH, COLUMN_DATE_BURIAL, COLUMN_DATE_DEATH,
 				COLUMN_NAME, COLUMN_SURENAME, COLUMN_FIELD, COLUMN_LAT,
 				COLUMN_LON, _ID, COLUMN_PLACE, COLUMN_QUARTER, COLUMN_ROW },
 				null, null, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		if (cursor.getCount() == 0) {
 			final CursorLoader l = (CursorLoader) loader;
 			throw new RuntimeException(
 					"Cursor has 0 results for loader with URI: " + l.getUri());
 		}
 		Departed d = new DepartedCursor(cursor);
 		cursor.moveToFirst();
 		fillHeaderWithData(d);
 		placeGravePin(d);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 
 	}
 
 	public Bitmap createMissingTileBitmap(final int tileSize,
 			final String bitmapText, Resources res) {
 		Bitmap canvasBitmap = Bitmap.createBitmap(tileSize, tileSize,
 				Bitmap.Config.RGB_565);
 		Canvas imageCanvas = new Canvas(canvasBitmap);
 
 		Paint textPaint = new Paint();
 		textPaint.setTextAlign(Align.CENTER);
 		textPaint.setTextSize(16f);
 
 		Paint backgroundPaint = new Paint();
 		backgroundPaint.setColor(Color.WHITE);
 		backgroundPaint.setStrokeWidth(3);
 		imageCanvas.drawRect(0, 0, tileSize, tileSize, backgroundPaint);
 
 		imageCanvas.drawText(bitmapText, tileSize / 2, tileSize / 2, textPaint);
 
 		BitmapDrawable finalImage = new BitmapDrawable(res, canvasBitmap);
 		return finalImage.getBitmap();
 	}
 
 }
