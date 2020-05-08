 /*     Copyright (c) 2012 Johannes Wikner, Anton Lindgren, Victor Lindhe,
  *         Niklas Andreasson, John Hult
  *
  *     Licensed to the Apache Software Foundation (ASF) under one
  *     or more contributor license agreements.  See the NOTICE file
  *     distributed with this work for additional information
  *     regarding copyright ownership.  The ASF licenses this file
  *     to you under the Apache License, Version 2.0 (the
  *     "License"); you may not use this file except in compliance
  *     with the License.  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing,
  *     software distributed under the License is distributed on an
  *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *     KIND, either express or implied.  See the License for the
  *     specific language governing permissions and limitations
  *     under the License.
  */
 
 package com.pifive.makemyrun;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.TextView;
 
 import com.pifive.makemyrun.database.MMRDbAdapter;
 
 /**
  * Cursor adapter class get data from MMRDbAdapter
  * 
  */
 public class RunHistory extends CursorAdapter {	
 	
 	/**
 	 * Construct
 	 * 
 	 * @param context Activity context
 	 * @param c Database table cursor
 	 */
 	public RunHistory(Context context, Cursor c) {
 		super(context, c);
 	}
 	
 	/**
 	 * Binds a toast just to make sure we can do something on click
 	 */
 	@Override
 	public void bindView(View item, final Context context, Cursor cursor) {
 		// TODO Bind to a more detailed mapview with flash graphics
 				
 	}
 
 	/**
 	 * Creates a simple textview for each listitem.
 	 * Displays ISO-format Date together with distance run/routedistance
 	 */
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup list) {
 		TextView view = new TextView(context);
 		view.setTextAppearance(context, R.style.HistoryFont);
 		
 		Long millis = cursor.getLong(cursor.getColumnIndex("dateStart"));
 		Date startDate = new Date(millis);
 		
 		// Distances
 		int distanceRan = cursor.getInt(cursor.getColumnIndex(MMRDbAdapter.KEY_RUN_DISTANCE_RAN));
 		int routeDistance = cursor.getInt(cursor.getColumnIndex(MMRDbAdapter.KEY_ROUTE_DISTANCE));
 		
 		view.setText(
				new SimpleDateFormat("yyyy-mm-dd").format(startDate) + 
 				" Distance: " +  
 				distanceRan +
 				" / " + routeDistance);
 		
 		return view;
 	}
 	
 }
