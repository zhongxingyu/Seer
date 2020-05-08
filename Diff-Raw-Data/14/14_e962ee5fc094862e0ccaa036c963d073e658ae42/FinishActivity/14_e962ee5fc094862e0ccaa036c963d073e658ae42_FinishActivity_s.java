 package ch.unibe.yala;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class FinishActivity extends MapActivity {
 
 	static GeoPoint[] movingPoints;
 	static Long[] movingTimes;
 	static Double[] altitudes;
 	static GeoPoint[] pausePoints;
 	static Long[] pauseTime;
 	static boolean save;
 	int viewSize;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_finish);
 
 		List<Double> myDistances = new ArrayList<Double>();
 		List<Double> myTimes = new ArrayList<Double>();
 		List<Double> myHeight = new ArrayList<Double>();
 
 		List<Double> sectionTime = new ArrayList<Double>();
 		List<Double> sectionDistance = new ArrayList<Double>();
 		List<Double> sectionHeight = new ArrayList<Double>();
 		int lastSectionEnd = 0;
 
 		Double[] distances;
 		Double[] times;
 		Double[] speeds;
 		Double[] height;
 
 		GraphViewData[] gvdS;
 		GraphViewData[] gvdA;
 
 		String[] vLabSpeed = new String[5];
 		String[] vLabAlt = new String[5];
 
 		double maxS;
 		double maxA;
 		double minS;
 		double minA;
 
 		viewSize = 0;
 
 		TextView stats = (TextView) findViewById(R.id.stats);
 		int ptsLen = movingPoints.length;
 		if (ptsLen > 1) {
 			int j = 0;
 			for (int i = 0; i < ptsLen - 1; i++) {
 				float[] result = new float[3];
 				Location.distanceBetween(movingPoints[i].getLatitudeE6() / 1E6,
 						movingPoints[i].getLongitudeE6() / 1E6, movingPoints[i + 1].getLatitudeE6() / 1E6,
 						movingPoints[i + 1].getLongitudeE6() / 1E6, result);
 				myDistances.add(Double.valueOf(result[0]));
 				double timeDiff = movingTimes[i + 1] - movingTimes[i];
 				myTimes.add(timeDiff / 1000);
 				myHeight.add(altitudes[i]);
 				if (j <= pauseTime.length) {
 					if (j< pauseTime.length)
 						Location.distanceBetween(movingPoints[i].getLatitudeE6() / 1E6,
 								movingPoints[i].getLongitudeE6() / 1E6,
 								pausePoints[j].getLatitudeE6() / 1E6,
 								pausePoints[j].getLongitudeE6() / 1E6, result);
 					else if (i == movingPoints.length - 2)
 						result[0] = 1;
 					else
 						result[0] = 5;
 					if (result[0] < 2) {
 						Double secTimeDiff = 0d;
 						for (int k = lastSectionEnd; k <= i; k++)
 							secTimeDiff += myTimes.get(k);
 						Double distDiff = 0d;
 						for (int k = lastSectionEnd; k <= i; k++)
 							distDiff += myDistances.get(k);
 						Double[] secHeight = new Double[myHeight.size() - lastSectionEnd];
 						for (int k = lastSectionEnd; k <= i; k++)
 							secHeight[k - lastSectionEnd] = myHeight.get(k);
 						sectionDistance.add(distDiff);
 						sectionTime.add(secTimeDiff);
 						sectionHeight.add(maxValue(secHeight) - minValue(secHeight));
 						lastSectionEnd = i;
 						j++;
 					}
 				}
 			}
 			myHeight.add(altitudes[ptsLen - 1]);
 
 			times = new Double[myTimes.size()];
 			times = myTimes.toArray(times);
 			distances = new Double[myDistances.size()];
 			distances = myDistances.toArray(distances);
 			height = new Double[myHeight.size()];
 			height = myHeight.toArray(height);
 
 			gvdS = new GraphViewData[distances.length];
 			gvdA = new GraphViewData[height.length];
 			speeds = new Double[distances.length];
 			double thatTime = 0;
 			for (int i = 0; i < distances.length; i++) {
 				speeds[i] = distances[i] / times[i];
 				if (i > 0)
 					speeds[i] = (speeds[i] + speeds[i - 1]) / 2;
 				gvdS[i] = new GraphViewData(thatTime, speeds[i]);
 				thatTime += times[i];
 			}
 			thatTime = 0;
 			for (int i = 0; i < height.length; i++) {
 				if (i > 0) {
 					thatTime += times[i - 1];
 					height[i] = (height[i] + height[i - 1]) / 2;
 				}
 				gvdA[i] = new GraphViewData(thatTime, height[i]);
 			}
 			maxS = maxValue(speeds);
 			minS = minValue(speeds);
 			if (maxS - minS == 0) {
 				minS--;
 				maxS++;
 			}
 			vLabSpeed[0] = Math.round(minS) + " km/h";
 			vLabSpeed[1] = Math.round((maxS - (maxS - minS) * 3 / 4) * 3.6) + " km/h";
 			vLabSpeed[2] = Math.round((maxS - (maxS - minS) / 2) * 3.6) + " km/h";
 			vLabSpeed[3] = Math.round((maxS - (maxS - minS) / 4) * 3.6) + " km/h";
 			vLabSpeed[4] = Math.round(maxS * 3.6) + " km/h";
 			maxA = maxValue(height) + 2;
 			minA = minValue(height) - 2;
 			if (maxA - minA == 0) {
 				minA--;
 				maxA++;
 			}
 			vLabAlt[0] = Math.round(minA) + " m";
 			vLabAlt[1] = Math.round(maxA - (maxA - minA) * 3 / 4) + " m";
 			vLabAlt[2] = Math.round(maxA - (maxA - minA) / 2) + " m";
 			vLabAlt[3] = Math.round(maxA - (maxA - minA) / 4) + " m";
 			vLabAlt[4] = Math.round(maxA) + " m";
 
 			if (sectionHeight.size() > 1) {
 				stats.setText(getString(R.string.overallStat));
 				stats.append(makeStatsFromArray(height, distances, movingTimes));
 				for (int k = 0; k < sectionHeight.size(); k++) {
 					stats.append("\n\n");
 					stats.append(getString(R.string.sectionStat, k + 1));
 					stats.append(makeStats(sectionHeight.get(k), sectionDistance.get(k),
 							sectionTime.get(k)));
 				}
 			} else
 				stats.setText(makeStatsFromArray(height, distances, movingTimes));
 
 		} else {
 			gvdS = new GraphViewData[] { new GraphViewData(1, 2.0d), new GraphViewData(2, 3.0d) };
 			gvdA = new GraphViewData[] { new GraphViewData(1, 3.0d), new GraphViewData(2, 2.0d) };
 			vLabSpeed = new String[] { "slow", "", "average", "", "fast" };
 			maxS = 5;
 			minS = 0;
 			vLabAlt = new String[] { "low", "", "average", "", "high" };
 			maxA = 5;
 			minA = 0;
 			stats.setText("Duration: 0:0:0 (h:m:s)\n");
 			stats.append("Elevation: 0 m\n");
 			stats.append("Average Speed: 0 km/h (0 m/s)\n");
 			stats.append("Distance: 0 m");
 		}
 
 		String[] hLab = new String[] { "Start", "Middle", "End" };
 		GraphView graphSpeed = new GraphView(this, gvdS, "Speed/Time", hLab, vLabSpeed, minS, maxS);
 		LinearLayout layoutSpeed = (LinearLayout) findViewById(R.id.graphSpeed);
 		layoutSpeed.addView(graphSpeed);
 		GraphView graphAlti = new GraphView(this, gvdA, "Altitude/Time", hLab, vLabAlt, minA, maxA);
 		LinearLayout layoutAlti = (LinearLayout) findViewById(R.id.graphAlti);
 		layoutAlti.addView(graphAlti);
 
 		MapView routeMap = (MapView) findViewById(R.id.route);
 		MapController mMapController = routeMap.getController();
 		mMapController.setZoom(17);
 		if (ptsLen > 0) {
 			mMapController.setCenter(movingPoints[0]);
 			MyMapOverlay mapOvlay = new MyMapOverlay(movingPoints);
 			routeMap.getOverlays().add(mapOvlay);
 		}
 		if (ptsLen > 1) {
 			Drawable startPin = getResources().getDrawable(R.drawable.startpin);
 			Drawable endPin = getResources().getDrawable(R.drawable.endpin);
 			MyItemOverlay startOverlay = new MyItemOverlay(startPin, this);
 			MyItemOverlay endOverlay = new MyItemOverlay(endPin, this);
 			OverlayItem startItem = new OverlayItem(movingPoints[0], "Start", "");
 			OverlayItem endItem = new OverlayItem(movingPoints[ptsLen - 1], "End", "");
 			startOverlay.addOverlay(startItem);
 			endOverlay.addOverlay(endItem);
 			routeMap.getOverlays().add(startOverlay);
 			routeMap.getOverlays().add(endOverlay);
 		}
 
 		Drawable pausePin = getResources().getDrawable(R.drawable.pausepin);
 		MyItemOverlay pauseOverlay = new MyItemOverlay(pausePin, this);
 		for (int i = 0; i < pauseTime.length; i++) {
 			long sec;
 			if (i==0)
 				sec = pauseTime[i] / 1000;
 			else
 				sec = (pauseTime[i] - pauseTime[i - 1]) / 1000;
 			String locTitle = "Paused: " + secToTimeString(sec) + "\n";
 			String locSnippet = "";
 			GeoPoint point = pausePoints[i];
 			if (isConnected())
 				try {
 					List<Address> addresses = new Geocoder(this).getFromLocation(
 							point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6, 10);
 					for (Address address : addresses) {
 						locSnippet += address.getAddressLine(0);
 						if (addresses.get(addresses.size() - 1) != address)
 							locSnippet += "\n";
 					}
 				} catch (IOException e) {
 				}
 			OverlayItem pauseItem = new OverlayItem(point, locTitle, locSnippet);
 			pauseOverlay.addOverlay(pauseItem);
 			routeMap.getOverlays().add(pauseOverlay);
 		}
 
 		if (save) {
 			DataLayer d = new DataLayer(getBaseContext());
 			d.addRun(new Date().toString(), convertGeoPointToString(movingPoints),
 					convertLongToString(movingTimes), convertDoubleToString(altitudes),
 					convertLongToString(pauseTime), convertGeoPointToString(pausePoints));
 		}
 
 	}
 
 	private String makeStatsFromArray(Double[] h, Double[] d, Long[] t) {
 		String s = "";
 		long sec = 0;
 		double dist = 0;
 		sec = (t[t.length - 1] - t[0]) / 1000;
 		double elev = Math.round(maxValue(h)) - Math.round(minValue(h));
 		for (Double distance : d)
 			dist += distance;
 		s += "Duration:     " + secToTimeString(sec) + "\n";
 		s += "Elevation:    " + elev + " m\n";
 		s += "Avg Speed: " + Math.round(dist / sec * 3.6) + " km/h (" + Math.round(dist / sec)
 				+ " m/s)\n";
 		if (dist > 1000)
 			s += "Distance:    " + Math.round(dist / 10) / 100f + " km";
 		else
 			s += "Distance:    " + (int) dist + " m";
 		return s;
 	}
 
 	private String makeStats(Double h, Double dist, Double sec) {
 		String s = "";
 		double elev = Math.round(h);
 		s += "Duration:     " + secToTimeString(sec.intValue()) + "\n";
 		s += "Elevation:    " + elev + " m\n";
 		s += "Avg Speed: " + Math.round(dist / sec * 3.6) + " km/h (" + Math.round(dist / sec)
 				+ " m/s)\n";
 		if (dist > 1000)
 			s += "Distance:    " + Math.round(dist / 10) / 100f + " km";
 		else
 			s += "Distance:    " + dist.intValue() + " m";
 		return s;
 	}
 
 	public void viewClick(View view) {
 		ScrollView scrollV = (ScrollView) findViewById(R.id.scroll);
 		LinearLayout routeV = (LinearLayout) findViewById(R.id.routeView);
 		LinearLayout.LayoutParams lpSmall = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
 				0, 1);
 		LinearLayout.LayoutParams lpBig = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
 				0, 9);
 		LinearLayout.LayoutParams lpHalf = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
 				0, 5);
 		if (viewSize == 0) {
 			scrollV.setLayoutParams(lpBig);
 			routeV.setLayoutParams(lpSmall);
 			viewSize = 1;
 		} else if (viewSize == 1) {
 			scrollV.setLayoutParams(lpSmall);
 			routeV.setLayoutParams(lpBig);
 			viewSize = 2;
 		} else {
 			scrollV.setLayoutParams(lpHalf);
 			routeV.setLayoutParams(lpHalf);
 			viewSize = 0;
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	private double minValue(Double[] myArray) {
 		List<Double> b = Arrays.asList(myArray);
 		return Collections.min(b);
 	}
 
 	private double maxValue(Double[] myArray) {
 		List<Double> b = Arrays.asList(myArray);
 		return Collections.max(b);
 	}
 
 	private String secToTimeString(long s) {
 		long sec = s;
 		long min = 0;
 		long hour = 0;
 		if (sec / 60 >= 1) {
 			min = (long) Math.floor(sec / 60);
 			sec = sec - min * 60;
 			if (min / 60 >= 1) {
 				hour = (long) Math.floor(min / 60);
 				min = min - hour * 60;
 			}
 		}
 		return hour + ":" + min + ":" + sec + " (h:m:s)";
 	}
 
 	@Override
 	public void onBackPressed() {
 		finish();
 	}
 
 	public boolean isConnected() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isAvailable() && netInfo.isConnected())
 			return true;
 		else
 			return false;
 	}
 
 	public static void setPoints(GeoPoint[] geoPoints) {
 		movingPoints = geoPoints;
 	}
 
 	public static void setTimes(Long[] times) {
 		movingTimes = times;
 	}
 
 	public static void setAltitude(Double[] alti) {
 		altitudes = alti;
 	}
 
 	public static void setPausePoints(GeoPoint[] pauseGeoPoints) {
 		pausePoints = pauseGeoPoints;
 	}
 
 	public static void setPauseTimes(Long[] pauseTimes) {
 		pauseTime = pauseTimes;
 	}
 
 	public static String convertLongToString(Long[] array) {
 		String s = "";
 		for (int i = 0; i < array.length; i++) {
 			s = s + array[i];
 			if (i < array.length - 1)
 				s = s + ",";
 		}
 		return s;
 	}
 
 	public static String convertDoubleToString(Double[] array) {
 		String s = "";
 		for (int i = 0; i < array.length; i++) {
 			s = s + array[i];
 			if (i < array.length - 1)
 				s = s + ",";
 		}
 		return s;
 	}
 
 	public static String convertGeoPointToString(GeoPoint[] array) {
 		String s = "";
 		for (int i = 0; i < array.length; i++) {
 			s = s + array[i].getLatitudeE6();
 			s = s + ";";
 			s = s + array[i].getLongitudeE6();
 			if (i < array.length - 1)
 				s = s + ",";
 		}
 		return s;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.finishmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.backup:
 			try {
 				File sd = Environment.getExternalStorageDirectory();
 				File data = Environment.getDataDirectory();
 
 				if (sd.canWrite()) {
 					String currentDBPath = "//data//ch.unibe.yala//databases//yala.db";
 					String backupDBPath = "yala.db";
 					File currentDB = new File(data, currentDBPath);
 					File backupDB = new File(sd, backupDBPath);
 
 					if (currentDB.exists()) {
 						FileChannel src = new FileInputStream(currentDB).getChannel();
 						FileChannel dst = new FileOutputStream(backupDB).getChannel();
 						dst.transferFrom(src, 0, src.size());
 						src.close();
 						dst.close();
 					}
 				}
 			} catch (Exception e) {
 			}
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 
 }
