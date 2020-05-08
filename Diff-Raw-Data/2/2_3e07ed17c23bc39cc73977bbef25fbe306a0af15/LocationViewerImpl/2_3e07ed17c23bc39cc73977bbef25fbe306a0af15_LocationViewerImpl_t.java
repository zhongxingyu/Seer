 package com.google.code.geobeagle;
 
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationProvider;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class LocationViewerImpl implements LocationViewer {
 	static class LocationViewerOnClickListener implements OnClickListener {
 		private final LocationSetter locationSetter;
 		private final LocationViewer locationViewer;
 
 		public LocationViewerOnClickListener(LocationViewer locationViewer,
 				LocationSetter locationSetter) {
 			this.locationSetter = locationSetter;
 			this.locationViewer = locationViewer;
 		}
 
 		public void onClick(View v) {
 			locationSetter.setLocation(locationViewer.getLocation());
 		}
 	}
 
 	private final MockableButton caption;
 	private final MockableTextView coordinates;
 
 	public LocationViewerImpl(final MockableButton button, MockableTextView coordinates,
 			Location initialLocation) {
 		this.coordinates = coordinates;
 		this.caption = button;
 		// disabled until coordinates come in.
 		button.setEnabled(false);
 		if (initialLocation == null) {
 			this.coordinates.setText("getting location from gps...");
 		} else {
 			setLocation(initialLocation);
 		}
 	}
 
 	public String getLocation() {
 		final String desc = (String) caption.getText();
 		return coordinates.getText() + " # " + desc.substring(0, desc.length());
 	}
 
 	public void setLocation(Location location) {
 		setLocation(location, location.getTime());
 	}
 
 	public void setLocation(Location location, long time) {
 		caption.setEnabled(true);
 		coordinates.setText(Util.degreesToMinutes(location.getLatitude()) + " "
 				+ Util.degreesToMinutes(location.getLongitude()));
 		caption.setText("GPS@" + Util.formatTime(time));
 	}
 
 	public void setOnClickListener(OnClickListener onClickListener) {
 		caption.setOnClickListener(onClickListener);
 	}
 
 	public void setStatus(int status) {
 		switch (status) {
 		case LocationProvider.OUT_OF_SERVICE:
 			caption.setTextColor(Color.RED);
 			break;
 		case LocationProvider.AVAILABLE:
 			caption.setTextColor(Color.BLACK);
 			break;
 		case LocationProvider.TEMPORARILY_UNAVAILABLE:
 			caption.setTextColor(Color.DKGRAY);
 			break;
 		}
 	}
 }
