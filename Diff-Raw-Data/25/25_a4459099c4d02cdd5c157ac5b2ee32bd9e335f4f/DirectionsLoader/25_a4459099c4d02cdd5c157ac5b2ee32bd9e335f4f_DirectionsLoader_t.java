 package com.cruzroja.creuroja;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.content.AsyncTaskLoader;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class DirectionsLoader extends AsyncTaskLoader<ArrayList<LatLng>> {
 	public static final String ARG_ORIG_LAT = "originLatitud";
 	public static final String ARG_ORIG_LONG = "originLongitud";
 	public static final String ARG_DEST_LAT = "destinationLatitud";
 	public static final String ARG_DEST_LONG = "destinationLongitud";
 
 	private double mOriginLatitud;
 	private double mOriginLongitud;
 	private double mDestinationLatitud;
 	private double mDestinationLongitud;
 	private boolean isStarted = false;
 
 	public DirectionsLoader(Context context, Bundle args) {
 		super(context);
 		if (args == null || !args.containsKey(ARG_DEST_LAT)
 				|| !args.containsKey(ARG_DEST_LONG)
 				|| !args.containsKey(ARG_ORIG_LAT)
 				|| !args.containsKey(ARG_ORIG_LONG)) {
 			mOriginLatitud = 0;
 			mOriginLongitud = 0;
 			mDestinationLatitud = 0;
 			mDestinationLongitud = 0;
		} else {
			mOriginLatitud = args.getDouble(ARG_ORIG_LAT);
			mOriginLongitud = args.getDouble(ARG_ORIG_LONG);
			mDestinationLatitud = args.getDouble(ARG_DEST_LAT);
			mDestinationLongitud= args.getDouble(ARG_DEST_LONG);
			
 		}
 	}
 
 	@Override
 	protected void onStartLoading() {
 		if (!isStarted) {
 			isStarted = true;
 			forceLoad();
 		}
 		super.onStartLoading();
 	}
 
 	@Override
 	public ArrayList<LatLng> loadInBackground() {
 		return ConnectionClient.getDirections(mOriginLatitud, mOriginLongitud,
 				mDestinationLatitud, mDestinationLongitud);
 	}
 
 }
