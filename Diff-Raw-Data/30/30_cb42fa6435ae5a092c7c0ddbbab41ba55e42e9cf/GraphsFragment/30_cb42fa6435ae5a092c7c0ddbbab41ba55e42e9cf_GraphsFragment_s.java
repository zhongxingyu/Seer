 /*
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.heneryh.aquanotes.ui.controllers;
 
 import java.text.DecimalFormat;
 import java.text.FieldPosition;
 import java.text.Format;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Vector;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.LinearGradient;
 import android.graphics.Paint;
 import android.graphics.PointF;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.BaseColumns;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.util.FloatMath;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.TabHost;
 
 import com.androidplot.series.XYSeries;
 import com.androidplot.xy.BoundaryMode;
 import com.androidplot.xy.LineAndPointFormatter;
 import com.androidplot.xy.LineAndPointRenderer;
 import com.androidplot.xy.SimpleXYSeries;
 import com.androidplot.xy.SimpleXYSeries.ArrayFormat;
 import com.androidplot.xy.XYPlot;
 import com.androidplot.xy.XYStepMode;
 import com.androidplot.Plot;
 import com.heneryh.aquanotes.R;
 import com.heneryh.aquanotes.provider.AquaNotesDbContract;
 import com.heneryh.aquanotes.provider.AquaNotesDbContract.Controllers;
 import com.heneryh.aquanotes.provider.AquaNotesDbContract.Data;
 import com.heneryh.aquanotes.provider.AquaNotesDbContract.Probes;
 import com.heneryh.aquanotes.ui.BaseActivity;
 import com.heneryh.aquanotes.util.NotifyingAsyncQueryHandler;
 
 /**
  * A {@link ListFragment} showing a list of controller probes.
  */
 public class GraphsFragment extends Fragment implements
         NotifyingAsyncQueryHandler.AsyncQueryListener,  OnTouchListener {
 
     private static final String TAG = "GraphsFragment";
 
     private Cursor mCursor;
     private Uri probesUri;
     private String probeName;
     private Uri probeDataUri;
     private Uri controllerUri;
     private int controllerId;
     
     boolean hackBailOut = true; // this is a bug in my code for when the dashboard updates and call this fragments refreshSelf but this is not in foreground
 
     private NotifyingAsyncQueryHandler mHandler;
 
     private ViewGroup mRootView;
     
 	private XYPlot mySimpleXYPlot;
 	private SimpleXYSeries mySeries;
 	private PointF minXY;
 	private PointF maxXY;
 	private float absMinX;
 	private float absMaxX;
 	private float minNoError;
 	private float maxNoError;
 	private double minDif;
  
 	final private double difPadding = 0.1;
 
 	List<CharSequence>  probeList;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         /**
          * The controller URI will be in the intent plus, depending on the situation
          * the probe name to graph can be in the extra.  If default, the probe name
          * will come in as "none" which will signal us to just graph the first one
          * found.
          */
         final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
         controllerUri = intent.getData();
         probeName = intent.getExtras().getString("ProbeName");
         
         if (controllerUri== null) {
             return;
         }
         controllerId = Integer.valueOf(Controllers.getControllerId(controllerUri));
         probesUri = Probes.buildQueryProbesUri(controllerUri);
 
         setHasOptionsMenu(true);
         
     }
     
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         if (probesUri == null) {
             return;
         }
 
         // Start background query to load probe list
         mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
         mHandler.startQuery(ProbesViewQuery._TOKEN, probesUri, ProbesViewQuery.PROJECTION);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
 
         mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_graphs, null);
 
         return mRootView;
     }
     
     
 
 
     /** {@inheritDoc} */
     public void onQueryComplete(int token, Object cookie, Cursor cursor) {
         if (getActivity() == null) {
             return;
         }
 
        if (token == ProbesViewQuery._TOKEN) {
             onProbesQueryComplete(cursor);
        } else if (token == ProbeDataViewQuery._TOKEN) {
         	onProbeDataQueryComplete(cursor);
         } else {
             cursor.close();
         }
     }
 
     /**
      * Handle {@link VendorsQuery} {@link Cursor}.
      */
     private void onProbesQueryComplete(Cursor cursor) {
     	probeList = new ArrayList<CharSequence>();
 
     	/**
     	 * build a list of probes which will be sent over to the main controller
     	 * activity in case the user presses the "Probes" button.
     	 */
 		try {
 			/** For each datapoint in the database, */
 			while (cursor.moveToNext()) {
 				probeList.add(cursor.getString(ProbesViewQuery.NAME));
 			} // end of while()
 		} finally {
 			cursor.close();
 		}
 		
 		/**
 		 * By default, load the first probe, but also seed the ControllerActivity with the full probe list
 		 * so that when the probe selection button is clicked, it has the list.
 		 * 
 		 * If the probe name was passed in as an extra, then use that one.
 		 */
 		if(probeName.equalsIgnoreCase("none"))
 			kickoffDataQuery(new String(probeList.get(0).toString()));
 		else
 			kickoffDataQuery(probeName);
 
 		ControllersActivity CA = (ControllersActivity) getActivity();
 		CA.prepProbeListDialog(controllerId,probeList);
     }
     
     /**
      * User selected a new probe.
      * @param probeName
      */
     void kickoffDataQuery(String probeName) {
 		probeDataUri = Data.buildQueryProbeDataByNameUri(controllerUri, probeName);
         mHandler.startQuery(ProbeDataViewQuery._TOKEN, probeDataUri, ProbeDataViewQuery.PROJECTION); 
     }
     
     /**
      * Handle {@link VendorsQuery} {@link Cursor}.
      */
     private void onProbeDataQueryComplete(Cursor cursor) {
     	if (mCursor != null) {
     		// In case cancelOperation() doesn't work and we end up with consecutive calls to this
     		// callback.
     		getActivity().stopManagingCursor(mCursor);
     		mCursor = null;
     	}
     	mySimpleXYPlot = (XYPlot) mRootView.findViewById(R.id.mySimpleXYPlot);
     	mySimpleXYPlot.setOnTouchListener(this);
     	mySimpleXYPlot.clear();
 
     	//Creation of the series
     	final Vector<Double> vector = new Vector<Double>();
     	int numDataPoints = 0;
 		String probeName = null;
 		Long timestamp = (long) 0;
 		String valueS = null;
     	try {
     		/** For each datapoint in the database, */
     		while (cursor.moveToNext()) {
     			probeName = cursor.getString(ProbeDataViewQuery.NAME);
     			timestamp = cursor.getLong(ProbeDataViewQuery.TIMESTAMP);
     			valueS = cursor.getString(ProbeDataViewQuery.VALUE);
     			Double valueD = Double.valueOf(valueS);
     			vector.add(timestamp.doubleValue());
     			vector.add(valueD);
     			numDataPoints++;
     		} // end of while()
     	} finally {
     		cursor.close();
     		if (numDataPoints<2)
     			return;
     	}
     	 // create our series from our array of nums:
     	mySeries = new SimpleXYSeries(vector, ArrayFormat.XY_VALS_INTERLEAVED,  probeName);
 
     	mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
     	mySimpleXYPlot.getGraphWidget().getGridLinePaint().setColor(Color.BLACK);
     	mySimpleXYPlot.getGraphWidget().getGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1,1}, 1));
     	mySimpleXYPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
     	mySimpleXYPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
 
     	mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
     	mySimpleXYPlot.getBorderPaint().setStrokeWidth(1);
     	mySimpleXYPlot.getBorderPaint().setAntiAlias(false);
     	mySimpleXYPlot.getBorderPaint().setColor(Color.WHITE);
 
     	// Create a formatter to use for drawing a series using LineAndPointRenderer:
     	LineAndPointFormatter series1Format = new LineAndPointFormatter(
     			Color.rgb(0, 100, 0),                   // line color
     			Color.rgb(0, 100, 0),                  // point color
     			Color.rgb(100, 200, 0));                // fill color
 
 
     	// setup our line fill paint to be a slightly transparent gradient:
     	Paint lineFill = new Paint();
     	lineFill.setAlpha(200);
     	//lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.WHITE, Color.GREEN, Shader.TileMode.MIRROR));
 
     	LineAndPointFormatter formatter  = new LineAndPointFormatter(Color.rgb(0, 0,0), Color.BLUE, Color.RED);
 //    	formatter.setFillPaint(lineFill);
     	formatter.setFillPaint(null);
 //    	formatter.setVertexPaint(null);
     	formatter.getLinePaint().setShadowLayer(0, 0, 0, 0);
     	mySimpleXYPlot.getGraphWidget().setPaddingRight(2);
     	mySimpleXYPlot.addSeries(mySeries, formatter);
 
     	// draw a domain tick for each year:
     	//mySimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE, numDataPoints);
 
     	// customize our domain/range labels
     	mySimpleXYPlot.setDomainLabel("Time");
     	mySimpleXYPlot.setRangeLabel(probeName);
 
     	// get rid of decimal points in our range labels:
     	mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("#0.00"));
 
     	mySimpleXYPlot.setDomainValueFormat(new MyDateFormat());
 
     	// by default, AndroidPlot displays developer guides to aid in laying out your plot.
     	// To get rid of them call disableAllMarkup():
     	mySimpleXYPlot.disableAllMarkup();	
     	
     	
     	//Set of internal variables for keeping track of the boundaries
     	mySimpleXYPlot.calculateMinMaxVals();
     	minXY = new PointF(mySimpleXYPlot.getCalculatedMinX().floatValue(),
     			mySimpleXYPlot.getCalculatedMinY().floatValue()); //initial minimum data point
     	absMinX = minXY.x; //absolute minimum data point
     	//absolute minimum value for the domain boundary maximum
     	minNoError = Math.round(mySeries.getX(1).floatValue() + 2);
     	maxXY = new PointF(mySimpleXYPlot.getCalculatedMaxX().floatValue(),
     			mySimpleXYPlot.getCalculatedMaxY().floatValue()); //initial maximum data point
     	absMaxX = maxXY.x; //absolute maximum data point
     	//absolute maximum value for the domain boundary minimum
     	maxNoError = (float) Math.round(mySeries.getX(mySeries.size() - 1).floatValue()) - 2;
 
     	//Check x data to find the minimum difference between two neighboring domain values
     	//Will use to prevent zooming further in than this distance
     	double temp1 = mySeries.getX(0).doubleValue();
     	double temp2 = mySeries.getX(1).doubleValue();
     	double temp3;
     	double thisDif;
     	minDif = 100000000;	//increase if necessary for domain values
     	for (int i = 2; i < mySeries.size(); i++) {
     		temp3 = mySeries.getX(i).doubleValue();
     		thisDif = Math.abs(temp1 - temp3);
     		if (thisDif < minDif)
     			minDif = thisDif;
     		temp1 = temp2;
     		temp2 = temp3;
     	}
     	minDif = minDif + difPadding; //with padding, the minimum difference
     	
     	mySimpleXYPlot.redraw();
 
     }
     
 	// Definition of the touch states
 	static final private int NONE = 0;
 	static final private int ONE_FINGER_DRAG = 1;
 	static final private int TWO_FINGERS_DRAG = 2;
 	private int mode = NONE;
  
 	private PointF firstFinger;
 	private float lastScrolling;
 	private float distBetweenFingers;
 	private float lastZooming;
  
 	@Override
 	public boolean onTouch(View arg0, MotionEvent event) {
 		switch(event.getAction() & MotionEvent.ACTION_MASK) {
 			case MotionEvent.ACTION_DOWN: // Start gesture
 				firstFinger = new PointF(event.getX(), event.getY());
 				mode = ONE_FINGER_DRAG;
 				break;
 			case MotionEvent.ACTION_UP:
 			case MotionEvent.ACTION_POINTER_UP:
 				//When the gesture ends, a thread is created to give inertia to the scrolling and zoom 
 				final Timer t = new Timer();
 				t.schedule(new TimerTask() {
 					@Override
 					public void run() {
 						while(Math.abs(lastScrolling) > 1f || Math.abs(lastZooming - 1) < 1.01) {
 							lastScrolling *= .8;	//speed of scrolling damping
 							scroll(lastScrolling);
 							lastZooming += (1 - lastZooming) * .2;	//speed of zooming damping
 							zoom(lastZooming);
 							checkBoundaries();
 							try {
 								mySimpleXYPlot.postRedraw();
 							} catch (final InterruptedException e) {
 								e.printStackTrace();
 							}
 							// the thread lives until the scrolling and zooming are imperceptible
 						}
 					}
 				}, 0);
  
 			case MotionEvent.ACTION_POINTER_DOWN: // second finger
 				distBetweenFingers = spacing(event);
 				// the distance check is done to avoid false alarms
 				if (distBetweenFingers > 5f)
 					mode = TWO_FINGERS_DRAG;
 				break;
 			case MotionEvent.ACTION_MOVE:
 				if (mode == ONE_FINGER_DRAG) {
 					final PointF oldFirstFinger = firstFinger;
 					firstFinger = new PointF(event.getX(), event.getY());
 					lastScrolling = oldFirstFinger.x - firstFinger.x;
 					scroll(lastScrolling);
 					lastZooming = (firstFinger.y - oldFirstFinger.y) / mySimpleXYPlot.getHeight();
 					if (lastZooming < 0)
 						lastZooming = 1 / (1 - lastZooming);
 					else
 						lastZooming += 1;
 					zoom(lastZooming);
 					checkBoundaries();
 					mySimpleXYPlot.redraw();
  
 				} else if (mode == TWO_FINGERS_DRAG) {
 					final float oldDist = distBetweenFingers;
 					distBetweenFingers = spacing(event);
 					lastZooming = oldDist / distBetweenFingers;
 					zoom(lastZooming);
 					checkBoundaries();
 					mySimpleXYPlot.redraw();
 				}
 				break;
 		}
 		return true;
 	}
 	
 	private void zoom(float scale) {
 		final float domainSpan = maxXY.x - minXY.x;
 		final float domainMidPoint = maxXY.x - domainSpan / 2.0f;
 		final float offset = domainSpan * scale / 2.0f;
 		minXY.x = domainMidPoint - offset;
 		maxXY.x = domainMidPoint + offset;
 	}
  
 	private void scroll(float pan) {
 		final float domainSpan = maxXY.x - minXY.x;
 		final float step = domainSpan / mySimpleXYPlot.getWidth();
 		final float offset = pan * step;
 		minXY.x += offset;
 		maxXY.x += offset;
 	}
  
 	private float spacing(MotionEvent event) {
 		final float x = event.getX(0) - event.getX(1);
 		final float y = event.getY(0) - event.getY(1);
 		return FloatMath.sqrt(x * x + y * y);
 	}
  
 	private void checkBoundaries() {
 		//Make sure the proposed domain boundaries will not cause plotting issues
 		if (minXY.x < absMinX)
 			minXY.x = absMinX;
 		else if (minXY.x > maxNoError)
 			minXY.x = maxNoError;
 		if (maxXY.x > absMaxX)
 			maxXY.x = absMaxX;
 		else if (maxXY.x < minNoError)
 			maxXY.x = minNoError;
 		if (maxXY.x - minXY.x < minDif)
 			maxXY.x = maxXY.x + (float) (minDif - (maxXY.x - minXY.x));
 		mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.AUTO);
 	}
 
 	 private class MyDateFormat extends Format {
 		 
 		 
          // create a simple date format that draws on the year portion of our timestamp.
          // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
          // for a full description of SimpleDateFormat.
          private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/d");
 
 
          @Override
          public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
              long timestamp = ((Number) obj).longValue();
              Date date = new Date(timestamp);
              return dateFormat.format(date, toAppendTo, pos);
          }
 
          @Override
          public Object parseObject(String source, ParsePosition pos) {
              return null;
 
          }
  }
 
 
 //    @Override
 //    public void onResume() {
 //        super.onResume();
 ////        getActivity().getContentResolver().registerContentObserver(
 ////                controllerUri, false, mProbeChangesObserver);
 //        if (mCursor != null) {
 //            mCursor.requery();
 //        }
 //        hackBailOut=false;
 //    }
 
 //    @Override
 //    public void onPause() {
 //        super.onPause();
 //        hackBailOut=true;
 ////        getActivity().getContentResolver().unregisterContentObserver(mProbeChangesObserver);
 //    }
 
 //    @Override
 //    public void onSaveInstanceState(Bundle outState) {
 //        super.onSaveInstanceState(outState);
 //        outState.putInt(STATE_CHECKED_POSITION, mCheckedPosition);
 //    }
 
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 //        inflater.inflate(R.menu.default_menu_items, menu);
         super.onCreateOptionsMenu(menu, inflater);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 //        if (item.getItemId() == R.id.menu_map) {
 //            // The room ID for the sandbox, in the map, is just the track ID
 //            final Intent intent = new Intent(getActivity().getApplicationContext(),
 //                    UIUtils.getMapActivityClass(getActivity()));
 //            intent.putExtra(MapFragment.EXTRA_ROOM,
 //                    ParserUtils.translateTrackIdAliasInverse(mTrackId));
 //            startActivity(intent);
 //            return true;
 //        }
         return super.onOptionsItemSelected(item);
     }
 
 
     private ContentObserver mProbeChangesObserver = new ContentObserver(new Handler()) {
         @Override
         public void onChange(boolean selfChange) {
             if (mCursor != null) {
                 mCursor.requery();
             }
         }
     };
 
 
     private interface ProbesViewQuery {
 
         int _TOKEN = 0x1;
         
         String[] PROJECTION = {
             	//  String PROBE_ID = "_id";
             	//  String PROBE_NAME = "probe_name";
             	//  String DEVICE_ID = "device_id";
             	//  String TYPE = "probe_type";
             	//  String RESOURCE_ID = "resource_id";
             	//  String CONTROLLER_ID = "controller_id";
                     BaseColumns._ID,
                     AquaNotesDbContract.ProbesView.NAME,
                     AquaNotesDbContract.ProbesView.RESOURCE_ID,
                     AquaNotesDbContract.ProbesView.CONTROLLER_ID,
 //              String CONTROLLER_ID = "_id";
 //              String TITLE = "title";
 //              String WAN_URL = "wan_url";
 //              String LAN_URL = "wifi_url";
 //              String WIFI_SSID = "wifi_ssid";
 //              String USER = "user";
 //              String PW = "pw";
 //              String LAST_UPDATED = "last_updated";
 //              String UPDATE_INTERVAL = "update_i";
 //              String DB_SAVE_DAYS = "db_save_days";
 //              String CONTROLLER_TYPE = "controller_type";
                 AquaNotesDbContract.ProbesView.TITLE,
                 AquaNotesDbContract.ProbesView.WAN_URL,
                 AquaNotesDbContract.ProbesView.LAN_URL,
                 AquaNotesDbContract.ProbesView.WIFI_SSID,
                 AquaNotesDbContract.ProbesView.USER,
                 AquaNotesDbContract.ProbesView.PW,
                 AquaNotesDbContract.ProbesView.LAST_UPDATED,
                 AquaNotesDbContract.ProbesView.UPDATE_INTERVAL,
                 AquaNotesDbContract.ProbesView.DB_SAVE_DAYS,
                 AquaNotesDbContract.ProbesView.MODEL,
         };
         int _ID = 0;
         int NAME = 1;
         int RESOURCE_ID = 2;
         int CONTROLLER_ID = 3;
         int TITLE = 4;
         int WAN_URL = 5;
         int LAN_URL = 6;
         int WIFI_SSID = 7;
         int USER = 8;
         int PW = 9;
         int LAST_UPDATED = 10;
         int UPDATE_INTERVAL = 11;
         int DB_SAVE_DAYS = 12;
         int MODEL = 13;
     }
     
     private interface ProbeDataViewQuery {
 
         int _TOKEN = 0x2;
         
         String[] PROJECTION = {
 //                String _ID = "_id";
 //                String TYPE = "type";
 //                String VALUE = "value";
 //                String TIMESTAMP = "timestamp";
 //                String PARENT_ID = "parent_id";   	
 //
 //        		//\\
 //        		Join
 //        		\\//
 //                String NAME = "name";
 //                String RESOURCE_ID = "resource_id";
 //                String CONTROLLER_ID = "controller_id";
         		BaseColumns._ID,
         		AquaNotesDbContract.ProbeDataView.TYPE,
         		AquaNotesDbContract.ProbeDataView.VALUE,
         		AquaNotesDbContract.ProbeDataView.TIMESTAMP,
         		AquaNotesDbContract.ProbeDataView.PARENT_ID,
 
         		AquaNotesDbContract.ProbeDataView.NAME,
         		AquaNotesDbContract.ProbeDataView.RESOURCE_ID,
         		AquaNotesDbContract.ProbeDataView.CONTROLLER_ID,
          };
         int _ID = 0;
         int TYPE = 1;
         int VALUE = 2;
         int TIMESTAMP = 3;
         int PARENT_ID = 4;
         int NAME = 5;
         int RESOURCE_ID = 6;
         int CONTROLLER_ID = 7;
      }
 
 
     
     
     
     
 }
