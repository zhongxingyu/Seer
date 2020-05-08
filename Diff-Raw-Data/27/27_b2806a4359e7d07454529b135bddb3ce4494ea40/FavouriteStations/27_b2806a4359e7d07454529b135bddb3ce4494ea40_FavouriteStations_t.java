 package com.seawolfsanctuary.tmt.stats;
 
 import java.text.DecimalFormat;
 import java.text.FieldPosition;
 import java.text.Format;
 import java.text.ParsePosition;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.TreeMap;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 
 import com.androidplot.series.XYSeries;
 import com.androidplot.xy.BarFormatter;
 import com.androidplot.xy.LineAndPointFormatter;
 import com.androidplot.xy.SimpleXYSeries;
 import com.androidplot.xy.XYPlot;
 import com.androidplot.xy.XYStepMode;
 import com.seawolfsanctuary.tmt.Helpers;
 import com.seawolfsanctuary.tmt.R;
 import com.seawolfsanctuary.tmt.database.Journey;
 
 public class FavouriteStations extends Activity {
 	private XYPlot mySimpleXYPlot;
 
 	@SuppressWarnings("serial")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.stats_favourite_stations);
 		mySimpleXYPlot = (XYPlot) findViewById(R.id.xy_FavouriteStations);
 
		Hashtable<String, Integer> departures = new Hashtable<String, Integer>();
		Hashtable<String, Integer> arrivals = new Hashtable<String, Integer>();
 
 		Journey db_journeys = new Journey(this);
 		db_journeys.open();
 		Cursor c = db_journeys.getAllJourneys();
 		// Collect count of departure stations
 		if (c.moveToFirst()) {
 			do {
 				Integer depCount = 0;
 				Integer arrCount = 0;
 
 				if (departures.containsKey(c.getString(1))) {
 					depCount += departures.get(c.getString(1));
 				}
 				departures.put(c.getString(1), (depCount + 1));
 
 				if (arrivals.containsKey(c.getString(7))) {
 					arrCount += arrivals.get(c.getString(7));
 				}
 				arrivals.put(c.getString(7), (arrCount + 1));
 			} while (c.moveToNext());
 		}
 		db_journeys.close();
 
		TreeMap<String, Integer> merged = new TreeMap<String, Integer>();
 		for (Iterator<String> i = departures.keySet().iterator(); i.hasNext();) {
 			String depStn = i.next().toString();
 
 			// Use departure count, add arrival count
 			Integer total = departures.get(depStn);
 			if (arrivals.containsKey(depStn)) {
 				total += arrivals.get(depStn);
 			}
 			merged.put(depStn, total);
 		}
 
 		// Add arrival-only counts
 		for (Iterator<String> i = arrivals.keySet().iterator(); i.hasNext();) {
 			String arrStn = i.next().toString();
 			if (!departures.containsKey(arrStn)) {
 				merged.put(arrStn, arrivals.get(arrStn));
 			}
 		}
 
		departures = null;
		arrivals = null;

 		// Create a reverse of merged: visitCounts = { :count1 => [stn1, stn2] }
 		ArrayList<Integer> countVisits = new ArrayList<Integer>();
 		Hashtable<Integer, ArrayList<String>> visitCounts = new Hashtable<Integer, ArrayList<String>>();
 		for (Iterator<String> s = merged.keySet().iterator(); s.hasNext();) {
 			String stn = s.next().toString();
 			Integer visits = merged.get(stn);
 
 			// Get any previous visit counts
 			ArrayList<String> stations = new ArrayList<String>();
 			if (visitCounts.containsKey(visits)) {
 				stations = visitCounts.get(visits);
 			}
 
 			// Update
 			stations.add(Helpers.trimNameFromStation(stn));
 
 			// Save
 			visitCounts.put(visits, stations);
 			countVisits.add(visits);
 		}
 
		merged = null;

 		final ArrayList<String> sortedKeys = new ArrayList<String>();
 		final ArrayList<Number> series1Numbers = new ArrayList<Number>();
 
 		// Sort visitCounts for iteration
 		// visitCounts = { :count1 => [stn1, stn2] }
 		Collections.sort(countVisits);
 		int count = 0;
 		for (Integer i : countVisits) {
 			if (count < 10 && (!series1Numbers.contains(i))) {
 				ArrayList<String> stns = visitCounts.get(i);
				StringBuilder names = new StringBuilder();
 				for (String stn : stns) {
					names.append(stn).append(" ");
 				}
 
				sortedKeys.add(names.toString());
 				series1Numbers.add(i);
 				count += 1;
 			}
 		}
 
		visitCounts = null;
		countVisits = null;

 		XYSeries series1 = new SimpleXYSeries(series1Numbers,
 				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
 
 		@SuppressWarnings("unused")
 		LineAndPointFormatter series1Format = new LineAndPointFormatter(
 				Color.rgb(0, 200, 0), // line color
 				Color.rgb(0, 100, 0), // point color
 				Color.rgb(0, 200, 0)); // fill color
 
 		BarFormatter series1BarFormat = new BarFormatter(Color.rgb(0, 200, 0),
 				Color.rgb(0, 100, 0));
 
 		mySimpleXYPlot.addSeries(series1, series1BarFormat);
 
 		mySimpleXYPlot.getLegendWidget().setVisible(false);
 		mySimpleXYPlot.disableAllMarkup();
 
 		mySimpleXYPlot.setRangeLabel("Count");
 		mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_PIXELS, 15);
 		mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("#"));
 
 		mySimpleXYPlot.setDomainLabel("Stn");
 		mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
 		mySimpleXYPlot.setDomainValueFormat(new Format() {
 			@Override
 			public StringBuffer format(Object object, StringBuffer buffer,
 					FieldPosition field) {
 				int pos = (int) Math.round((Double) object);
 				String station = sortedKeys.get(pos).toString();
 				StringBuffer result = new StringBuffer(station);
 				return result;
 			}
 
 			@Override
 			public Object parseObject(String string, ParsePosition position) {
 				return null;
 			}
 
 		});
 	}
 }
