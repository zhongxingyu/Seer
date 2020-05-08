 /*******************************************************************
  * Copyright (c) 2010 Rohit Kumbhar
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  *******************************************************************/
 package net.yama.android.views.components;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import net.yama.android.response.Event;
 import net.yama.android.response.Venue;
 import net.yama.android.util.Constants;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.text.util.Linkify;
 import android.view.Gravity;
 import android.view.ViewGroup;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 /**
  * @author Rohit Kumbhar
  *
  */
 public class EventInfoView extends TableLayout {
 
 	static final SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM");
 	static final SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
 	
 	/**
 	 * @param context
 	 */
 	public EventInfoView(Context context, Event event) {
 		super(context);
 		
 		TextView titleView = new TextView(context);
 		titleView.setText(event.getName());
 		titleView.setTypeface(Typeface.DEFAULT_BOLD);
 		titleView.setPadding(2, 2, 2, 2);
 		titleView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
 		this.addView(titleView,0);
 		
 		TableRow dateVenueInfo = new TableRow(context);
 		DateDisplay dateDisplay = new DateDisplay(context, event.getEventTime());
 		dateDisplay.setPadding(10, 10, 10, 10);
 		TextView venueInfo = new TextView(context);
 		venueInfo.setPadding(10, 0, 1, 0);
 		venueInfo.setText(getVenueString(event.getVenue()));
 		Linkify.addLinks(venueInfo, Constants.LINK_MASK );
 		dateVenueInfo.addView(dateDisplay);
 		dateVenueInfo.addView(venueInfo);
 		this.addView(dateVenueInfo,1);
 		
 		
 		TextView descriptionTitle = new TextView(context);
 		descriptionTitle.setText("Description");
 		descriptionTitle.setTypeface(Typeface.DEFAULT_BOLD);
 		descriptionTitle.setPadding(2, 2, 2, 2);
 		this.addView(descriptionTitle,2);
 		
 		ScrollView scrollView = new ScrollView(context);
 		TextView descriptionView = new TextView(context);
 		descriptionView.setText(sanitize(event.getDescription()));
 		descriptionView.setPadding(2, 2, 2, 2);
 		descriptionView.setScrollContainer(true);
 		descriptionView.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
 		Linkify.addLinks(descriptionView, Constants.LINK_MASK);
 		scrollView.addView(descriptionView);
 		
 		this.addView(scrollView,3);
 	}
 
 	private CharSequence sanitize(String description) {
 		description = description.replaceAll("<br>", "\n");
 		description = description.replaceAll("<br />", "\n");
 		return description;
 	}
 
 	private String getVenueString(Venue venue) {
 		
 		StringBuilder venueString = new StringBuilder();
 		String returnVenue = "Location:\n";
 		
 		if(isNotEmpty(venue.getVenueAddress1()))
 			venueString.append(venue.getVenueAddress1()).append("\n");
 		
 		if(isNotEmpty(venue.getVenueAddress2()))
 			venueString.append(venue.getVenueAddress2()).append("\n");
 		
 		if(isNotEmpty(venue.getVenueCity()))
 			venueString.append(venue.getVenueCity()).append(", ");
 		
 		if(isNotEmpty(venue.getVenueState()))
 			venueString.append(venue.getVenueState()).append("\n");
 		
 		if(isNotEmpty(venue.getVenuePhone()))
 			venueString.append("Phone: ").append(venue.getVenuePhone());
 		
 		if(venueString.length() > 0)
 			returnVenue = returnVenue + venueString.toString();
 		else 
 			returnVenue = returnVenue + "Not chosen yet.";
 		
 		return returnVenue;
 	}
 
 	
 
 	private boolean isNotEmpty(String str) {
 		return (str != null && str.length() > 0);
 	}
 
 
 
 	public class DateDisplay extends TableLayout{
 		
 		public DateDisplay(Context context, Date theDate) {
 			super(context);
 			
 			Calendar c = Calendar.getInstance();
 			c.setTime(theDate);
 			int day = c.get(Calendar.DAY_OF_MONTH);
 			
 			String month = monthFormatter.format(theDate);
 			String dayStr = String.valueOf(day);  
 			String timeStr = timeFormatter.format(theDate);
 			
 			TextView monthView = new TextView(context);
 			monthView.setText(month);
 			monthView.setTextSize(14.0F);
 			monthView.setTypeface(Typeface.DEFAULT_BOLD);
 			monthView.setGravity(Gravity.CENTER_HORIZONTAL);
 			monthView.setTextColor(Color.WHITE);
 			monthView.setBackgroundColor(Color.rgb(204, 0, 0));
 			monthView.setMaxHeight(15);
 			
 			TextView dayView = new TextView(context);
 			dayView.setText(dayStr);
 			dayView.setTextSize(25.0F);
 			dayView.setBackgroundColor(Color.WHITE);
 			dayView.setTextColor(Color.DKGRAY);
 			dayView.setGravity(Gravity.CENTER_HORIZONTAL);
 			monthView.setMinHeight(25);
 			
 			TextView timeView = new TextView(context);
 			timeView.setText(timeStr);
 			timeView.setTextSize(14.0F);
 			timeView.setTextColor(Color.WHITE);
 			timeView.setGravity(Gravity.CENTER_HORIZONTAL);
 			timeView.setMinHeight(15);
 			
 			this.addView(monthView, 80,30);
 			this.addView(dayView, 80,60);
 			this.addView(timeView, 80,30);
 			this.setPadding(20,20,20,20);
 			
 		}
 		
 	}
 
 }
