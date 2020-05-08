 /*******************************************************************************
  * This file is part of Jane.
  * 
  *     Jane is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     Jane is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with Jane.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *     Copyright (c) 2011 Vasile Jureschi <vasile.jureschi@gmail.com>.
  *     All rights reserved. This program and the accompanying materials
  *     are made available under the terms of the GNU Public License v3.0
  *     which accompanies this distribution, and is available at
  *     
  *    http://www.gnu.org/licenses/gpl-3.0.html
  * 
  *     Contributors:
  *        Vasile Jureschi <vasile.jureschi@gmail.com> - initial API and implementation
  ******************************************************************************/
 
 package org.unchiujar.jane.preferences;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.unchiujar.jane.R;
 import org.unchiujar.jane.activities.JaneApplication;
 import org.unchiujar.jane.location.Waypoint;
 import org.unchiujar.jane.location.WaypointManager;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.location.Location;
 import android.preference.Preference;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import au.com.bytecode.opencsv.CSVReader;
 
 public class FolderSelectPreference extends Preference implements TextWatcher {
 	private static final String FROM_FILE = "from_file";
 	private final String TAG = FolderSelectPreference.class.getName();
 	private static final String DEFAULT_VALUE = "/";
 
 	private AutoCompleteTextView mTxtWaypointsFolder;
 	private Button mBtnLoadWaypoints;
 
 	private String mFolder;
 	private Context mContext;
 
 	public FolderSelectPreference(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initPreference(context, attrs);
 
 	}
 
 	public FolderSelectPreference(Context context, AttributeSet attrs,
 			int defStyle) {
 		super(context, attrs, defStyle);
 		initPreference(context, attrs);
 	}
 
 	private void initPreference(Context context, AttributeSet attrs) {
 		setValuesFromXml(attrs);
 		Log.d(TAG, "Init preferences.");
 		mContext = context;
 	}
 
 	private void setValuesFromXml(AttributeSet attrs) {
 		// mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 255);
 	}
 
 	@Override
 	protected View onCreateView(ViewGroup parent) {
 
 		LayoutInflater mInflater = (LayoutInflater) getContext()
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		return (RelativeLayout) mInflater.inflate(R.xml.folder_import, parent,
 				false);
 
 	}
 
 	@Override
 	public void onBindView(View view) {
 		super.onBindView(view);
 		Log.d(TAG, "Binding view...");
 		mTxtWaypointsFolder = (AutoCompleteTextView) view
 				.findViewById(R.id.txtSelectGpxFolder);
 		mBtnLoadWaypoints = (Button) view.findViewById(R.id.btnLoadGpx);
 
 		mTxtWaypointsFolder.addTextChangedListener(this);
 		mTxtWaypointsFolder.setText(mFolder);
 
 		mBtnLoadWaypoints.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// get list of gpx files from the folders
 				File path = new File(mTxtWaypointsFolder.getText().toString());
 				final ArrayList<File> waypointsFiles = new ArrayList<File>();
 				File[] files = path.listFiles();
 				for (File file : files) {
 					if (file.toString().toLowerCase().endsWith(".waypoints")) {
 						Log.d(TAG, "Found waypoints file: " + file.toString());
 						waypointsFiles.add(file);
 					}
 				}
 
 				final ProgressDialog progress = new ProgressDialog(mContext);
 
 				progress.setCancelable(false);
 				progress.setMax(waypointsFiles.size());
 				progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 				progress.setMessage(mContext
 						.getString(R.string.importing_locations));
 
 				progress.show();
 
 				Runnable importer = new Runnable() {
 
 					@Override
 					public void run() {
 
 						WaypointManager manager = ((JaneApplication) ((Activity) mContext)
 								.getApplication()).getWaypointManager();
 						ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
 						for (File file : waypointsFiles) {
 							try {
 								CSVReader reader = new CSVReader(
 										new FileReader(file));
 								List<String[]> raw = reader.readAll();
 								double latitude = 0;
 								double longitude = 0;
 								String info = null;
 								int index = 0;
								// parse each row read
								// if there is no info then add no info to the
								// waypoint
								// XXX in case of multiple waypoint files the
								// waypoint order is indeterminate
 								for (String[] data : raw) {
 									// latitude, longitude, optional info
 
 									latitude = Double.parseDouble(data[0]);
 									longitude = Double.parseDouble(data[1]);
									// if there is no info just ignore the third
									// position
 									if (data.length > 2) {
 										info = data[3];
 									}
 									index++;
 									Location location = new Location(FROM_FILE);
 									location.setLatitude(latitude);
 									location.setLongitude(longitude);
									Waypoint point = new Waypoint(location,
											index, false);
 									point.setInfo(info);
 									waypoints.add(point);
 								}
 							} catch (FileNotFoundException e) {
 								Log.e(TAG, "Waypoints file not found" + file);
 							} catch (IOException e) {
 								Log.e(TAG, "Error reading from waypoints file "
 										+ file);
 							}
 							progress.incrementProgressBy(1);
 						}
						// add all the waypoints at once to prevent extra work
 						manager.addWaypoints(waypoints);
 						progress.dismiss();
 					}
 				};
 				new Thread(importer).start();
 
 				Log.d(TAG, "Imported GPX data.");
 			}
 		});
 	}
 
 	@Override
 	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
 
 		if (restoreValue) {
 			mFolder = getPersistedString(DEFAULT_VALUE);
 		} else {
 			mFolder = (String) defaultValue;
 			persistString(mFolder);
 		}
 
 	}
 
 	@Override
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 		// NO-OP
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 		// NO-OP
 	}
 
 	@Override
 	public void afterTextChanged(Editable s) {
 		mFolder = s.toString();
 
 		ArrayList<String> folders = new ArrayList<String>();
 		// create array of folders
 		File path = new File(mFolder);
 		// if it is a path create array of folders
 		File[] files = path.listFiles();
 
 		if (path.isDirectory() && files != null) {
 			for (File file : files) {
 				if (file.isDirectory()) {
 					folders.add(file.getAbsolutePath());
 				}
 			}
 		}
 		Log.v(TAG, "Folders found for autocomplete:" + folders);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
 				android.R.layout.simple_dropdown_item_1line, folders);
 
 		mTxtWaypointsFolder.setAdapter(adapter);
 		// display the dropdown only if there is a list of folders
 		// to select from
 		if (folders.size() > 0) {
 			mTxtWaypointsFolder.showDropDown();
 		}
 		persistString(mFolder);
 	}
 
 }
