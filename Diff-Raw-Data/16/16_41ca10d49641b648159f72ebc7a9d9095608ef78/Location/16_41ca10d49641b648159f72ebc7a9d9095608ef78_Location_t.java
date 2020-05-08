 package org.creuroja.android.app;
 
 import android.annotation.SuppressLint;
 import android.content.ContentResolver;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.text.TextUtils;
 
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import org.creuroja.android.R;
 import org.creuroja.android.app.utils.ConnectionClient;
 import org.creuroja.android.database.CreuRojaContract;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created by lapuente on 29.10.13.
  */
 public class Location {
 	public final double mLatitude;
 	public final double mLongitude;
 	public final String mName;
 	public final Type mType;
 	public final String mAddress;
 	public final String mDescription;
 	public final String mLastModified;
 	public final long mExpireDate;
 	public Marker mMarker;
 
 	public Location(double latitude, double longitude, String name, String type, String address,
 					String details, String lastModified, long expireDate) {
 		mLatitude = latitude;
 		mLongitude = longitude;
 		mName = name;
 		mType = Type.getType(type);
 		mAddress = address;
 		mDescription = details;
 		mLastModified = lastModified;
 		mExpireDate = expireDate;
 	}
 
 	public Location(JSONObject object) throws JSONException {
 		mLatitude = object.getDouble(LoginResponse.sLatitude);
 		mLongitude = object.getDouble(LoginResponse.sLongitude);
 		mName = object.getString(LoginResponse.sName);
 		mType = Type.getType(object.getString(LoginResponse.sType));
 		mAddress = object.getString(LoginResponse.sAddress);
		if (object.isNull(LoginResponse.sDescription) ||
			object.getString(LoginResponse.sDescription) == "null") {
			mDescription = "";
		} else {
			mDescription = object.getString(LoginResponse.sDescription);
		}

 		mLastModified = object.getString(LoginResponse.sLastUpdateTime);
 		mExpireDate = (object.has(LoginResponse.sExpireDate) &&
 					   !object.isNull(LoginResponse.sExpireDate)) ?
 				object.getLong(LoginResponse.sExpireDate) : 0;
 	}
 
 	public Location(Cursor cursor) {
 		mLatitude = cursor.getDouble(cursor.getColumnIndex(CreuRojaContract.Locations.LATITUD));
 		mLongitude = cursor.getDouble(cursor.getColumnIndex(CreuRojaContract.Locations.LONGITUD));
 		mName = cursor.getString(cursor.getColumnIndex(CreuRojaContract.Locations.NAME));
 		mType = Type.getType(
 				cursor.getString(cursor.getColumnIndex(CreuRojaContract.Locations.ICON)));
 		mAddress = cursor.getString(cursor.getColumnIndex(CreuRojaContract.Locations.ADDRESS));
 		mDescription = cursor.getString(cursor.getColumnIndex(CreuRojaContract.Locations.DETAILS));
 		mLastModified =
 				cursor.getString(cursor.getColumnIndex(CreuRojaContract.Locations.LAST_MODIFIED));
 		mExpireDate = cursor.getLong(cursor.getColumnIndex(CreuRojaContract.Locations.EXPIRE_DATE));
 	}
 
 	public boolean newerThan(String lastUpdate) {
 		if (TextUtils.isEmpty(lastUpdate)) {
 			return true;
 		}
 		try {
 			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
 			Date updatedAt = format.parse(mLastModified);
 			Date saved = format.parse(lastUpdate);
 			return updatedAt.after(saved);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	@Override
 	public boolean equals(Object location) {
 		Location loc = (Location) location;
 		return ((Double.compare(loc.mLatitude, this.mLatitude) == 0) &&
 				(Double.compare(loc.mLongitude, this.mLongitude) == 0));
 	}
 
 	public LatLng getPosition() {
 		return new LatLng(mLatitude, mLongitude);
 	}
 
 	public List<LatLng> getDirections(double latitudeStart, double longitudeStart)
 			throws IOException {
 		return new ConnectionClient().getDirections(latitudeStart, longitudeStart, this);
 	}
 
 	public MarkerOptions getMarker() {
 		MarkerOptions marker = new MarkerOptions().position(getPosition());
 		if (mType.mIcon != 0) {
 			marker.icon(BitmapDescriptorFactory.fromResource(mType.mIcon));
 		}
 
 		return marker;
 	}
 
 	public boolean shouldBeShown(String filter, SharedPreferences prefs) {
 		if (!matchesFilter(filter)) {
 			return false;
 		}
 		switch (mType) {
 			case ADAPTADAS:
 				return prefs.getBoolean(Settings.SHOW_ADAPTADAS, true);
 			case ASAMBLEA:
 				return prefs.getBoolean(Settings.SHOW_ASAMBLEA, true);
 			case BRAVO:
 				return prefs.getBoolean(Settings.SHOW_BRAVO, true);
 			case CUAP:
 				return prefs.getBoolean(Settings.SHOW_CUAP, true);
 			case HOSPITAL:
 				return prefs.getBoolean(Settings.SHOW_HOSPITAL, true);
 			case MARITIMO:
 				return prefs.getBoolean(Settings.SHOW_MARITIMO, true);
 			case NOSTRUM:
 				return prefs.getBoolean(Settings.SHOW_NOSTRUM, true);
 			case SOCIAL:
 				return prefs.getBoolean(Settings.SHOW_SOCIAL, true);
 			case TERRESTRE:
 				return prefs.getBoolean(Settings.SHOW_TERRESTRE, true);
 			default:
 				return true;
 		}
 	}
 
 	private boolean matchesFilter(String filter) {
 		if (TextUtils.isEmpty(filter)) {
 			return true;
 		} else {
 			filter = dehyphenize(filter);
 			String address = null;
 			if (mAddress != null) {
 				address = dehyphenize(mAddress);
 			}
 			return (dehyphenize(mName).contains(filter) ||
 					((address != null) && (address.contains(filter))));
 		}
 	}
 
 	@SuppressLint("DefaultLocale")
 	private String dehyphenize(String input) {
 		if (TextUtils.isEmpty(input)) {
 			return "";
 		}
 		input = input.toLowerCase();
 		return input.replace("à", "a").replace("á", "a").replace("é", "e").replace("è", "e")
 				.replace("í", "i").replace("ì", "i").replace("ó", "o").replace("ò", "o")
 				.replace("ú", "u").replace("ù", "u");
 	}
 
 	public enum Type {
 		NONE(R.string.no_marker_type, 0),
 		ADAPTADAS(R.string.marker_type_adaptadas, R.drawable.adaptadas),
 		ASAMBLEA(R.string.marker_type_asamblea, R.drawable.asamblea),
 		BRAVO(R.string.marker_type_bravo, R.drawable.bravo),
 		CUAP(R.string.marker_type_cuap, R.drawable.cuap),
 		HOSPITAL(R.string.marker_type_hospital, R.drawable.hospital),
 		MARITIMO(R.string.marker_type_maritimo, R.drawable.maritimo),
 		NOSTRUM(R.string.marker_type_nostrum, R.drawable.nostrum),
 		SOCIAL(R.string.marker_type_social, R.drawable.social),
 		TERRESTRE(R.string.marker_type_terrestre, R.drawable.terrestre);
 		public final int mIcon;
 		public final int mMarkerType;
 
 		Type(int markerType, int icon) {
 			mMarkerType = markerType;
 			mIcon = icon;
 		}
 
 		public static Type getType(String type) {
 			if (type.equals(Settings.SHOW_ADAPTADAS)) {
 				return ADAPTADAS;
 			} else if (type.equals(Settings.SHOW_ASAMBLEA)) {
 				return ASAMBLEA;
 			} else if (type.equals(Settings.SHOW_BRAVO)) {
 				return BRAVO;
 			} else if (type.equals(Settings.SHOW_CUAP)) {
 				return CUAP;
 			} else if (type.equals(Settings.SHOW_HOSPITAL)) {
 				return HOSPITAL;
 			} else if (type.equals(Settings.SHOW_MARITIMO)) {
 				return MARITIMO;
 			} else if (type.equals(Settings.SHOW_NOSTRUM)) {
 				return NOSTRUM;
 			} else if (type.equals(Settings.SHOW_SOCIAL)) {
 				return SOCIAL;
 			} else if (type.equals(Settings.SHOW_TERRESTRE)) {
 				return TERRESTRE;
 			} else {
 				return NONE;
 			}
 		}
 
 		public static List<Type> getAvailableTypes(ContentResolver cr) {
 			ArrayList<Type> typeList = new ArrayList<>();
 			String[] projection = {"DISTINCT " + CreuRojaContract.Locations.ICON};
 			Cursor cursor =
 					cr.query(CreuRojaContract.Locations.CONTENT_LOCATIONS, projection, null, null,
 							null);
 			if (cursor.moveToFirst()) {
 				do {
 					typeList.add(getType(cursor
 							.getString(cursor.getColumnIndex(CreuRojaContract.Locations.ICON))));
 				} while (cursor.moveToNext());
 			}
 			cursor.close();
 			return typeList;
 		}
 	}
 }
