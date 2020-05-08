 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.morphoss.acal.weekview;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 import com.morphoss.acal.R;
 import com.morphoss.acal.acaltime.AcalDateRange;
 import com.morphoss.acal.acaltime.AcalDateTime;
 import com.morphoss.acal.davacal.AcalEvent;
 import com.morphoss.acal.davacal.SimpleAcalEvent;
 
 public class WeekViewDays extends ImageView {
 	
 	public static final String TAG = "aCal - WeekViewDays";
 	
 	private WeekViewActivity context; 
 	private AcalDateTime date;
 	
 	private SimpleAcalEvent[][] headerTimeTable;
 	
 
 	//Drawing vars - easier to set these as class fields than to send them as parmaeters
 	//All these vars are used in drawing and are recaculated each time draw() is called and co-ordinates have changed
 	
 	// These need only be calculated once 
 	private int width;				//The current screen width in pixels
 	private int TpX;				//The current Screen height in pixels
 	private int HSPP;				//Horizontal Seconds per Pixel
 	private int HNS;				//the number of visible horizontal seconds
 	private int HIH;				//The height of a Horizontal event
 
 	// These ones do change as things move around
 	private int PxD;				//The current height of days section
 	private int PxH;				//The current height of the Header section
 	private int t;					//The first second of the day that is visible in main view
 	private int scrollx;			//The current (valid) horizontal scroll amount
 	private int scrolly;			//The current (valid) vertical scroll amount
 	private long currentEpoch;		//The UTC epoch time of 0:00 on the first Visible Day
 	private long HST;				//The UTC epoch time of the first visible horizontal second
 	private long HET;				//The UTC epoch time of the last visible horizontal second
 	private int HDepth;				//The number of horizontal rows
 	private int lastMaxX;
 	private ArrayList<AcalEvent> HList;  				//The last list of events for the header
 	private ArrayList<SimpleAcalEvent> HSimpleList;  	//The last list of (simple) events for the header
 	private SimpleAcalEvent[][] HTimetable;  			//The last timetable used for the header
 	
 	//These to vars define the top left drawing position of the main area (excluded header)
 	private int x = 0;			
 	private int y = 0;
 	
 	private boolean isInitialized = false;	//Set to True once screen dimensions are calculated.
 	
 	/** Default Constructor */
 	public WeekViewDays(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		if (this.isInEditMode()) {
 			return;
 		}
 		if (!(context instanceof WeekViewActivity)) throw new IllegalStateException("Week View Started with invalid context.");
 		this.context = (WeekViewActivity) context;
 		this.date = this.context.getCurrentDate();
 	}
 	
 	/** Default Constructor */
 	public WeekViewDays(Context context, AttributeSet attrs) {
 		super(context,attrs);
 		if (this.isInEditMode()) {
 			return;
 		}
 		if (!(context instanceof WeekViewActivity)) throw new IllegalStateException("Week View Started with invalid context.");
 		this.context = (WeekViewActivity) context;
 		this.date = this.context.getCurrentDate();
 	} 
 	
 	/** Default Constructor */
 	public WeekViewDays(Context context) {
 		super(context);
 		if (this.isInEditMode()) {
 			return;
 		}
 		if (!(context instanceof WeekViewActivity)) throw new IllegalStateException("Week View Started with invalid context.");
 		this.context = (WeekViewActivity) context;
 		this.date = this.context.getCurrentDate();
 	}
 	
 	public float headerHeight() {
 		return this.PxH;
 	}
 	
 	float getHeaderHeight() {
 		return this.PxH; 
 	}
 
 	/**
 	 * Return the nearest valid 'y' to the one given, ensuring we don't scroll above / below the day.
 	 *  
 	 * @param y A position we are attempting to scroll to
 	 * @return The position closest to that which is reasonable.
 	 */
 	int checkScrollY(int y) {
 		int min = -PxH;
 		int max = ((86400/WeekViewActivity.SECONDS_PER_PIXEL)-TpX)+1;
 		return (Math.min(max, Math.max(min, y)));
 		
 	}
 	
 	private void drawHeader(Canvas canvas, Paint p) {
 		
 		//1 Calculate per frame vars
 		HST = this.currentEpoch - (scrollx*HSPP);
 		HET = HST+HNS;
 		
		AcalDateTime startTime = new AcalDateTime().setEpoch(HST).applyLocalTimeZone();
		AcalDateTime endTime = startTime.clone().setEpoch(HET);
 		AcalDateRange range = new AcalDateRange(startTime,endTime);
 		
 		//Get the current timetable
 		headerTimeTable = getTimeTable(getFullDayEventList(range));
 		if (headerTimeTable.length <=0) {	this.PxH =0; return; }
 		PxH = HDepth*HIH;
 		
 		//draw day boxes
 		p.setStyle(Paint.Style.STROKE);
 		p.setColor(context.getResources().getColor(R.color.WeekViewDayGridBorder));
 		canvas.drawRect(x, y, width, PxH, p);
 		for (int curx =0-(WeekViewActivity.DAY_WIDTH-scrollx); curx<=width; curx+= WeekViewActivity.DAY_WIDTH) {
 			canvas.drawRect(x+curx, y, x+curx+WeekViewActivity.DAY_WIDTH,y+PxH,p);
 		}
 		
 		
 		for (int i = 0; i<headerTimeTable.length;i++)  {
 			boolean hasEvent = false;
 			for(int j=0;j < headerTimeTable[i].length && headerTimeTable[i][j] != null;j++) {
 				SimpleAcalEvent event = headerTimeTable[i][j];
 					drawHorizontal(event, canvas,i);
 					hasEvent=true;
 			}
 			if (!hasEvent) break;
 		}
 		
 	}
 	
 	public boolean isInitialized() { return this.isInitialized; }
 	private void initialize() {
 		//Vars needed for drawing
 		width = this.getWidth();
 		TpX = this.getHeight();
 		x = 0;
 		y = 0;
 
 		//Seconds per pixel min/max values (MAX full day visible MIN 1 pixel per minute)
 		WeekViewActivity.SECONDS_PER_PIXEL = Math.max(WeekViewActivity.SECONDS_PER_PIXEL,60);
 		WeekViewActivity.SECONDS_PER_PIXEL = Math.min(WeekViewActivity.SECONDS_PER_PIXEL,(86400/TpX)+1);
 		
 		//Horizontal SecsPerPix and Horizontal Number Visible Seconds and Horizontal Item Height
 		HSPP = 86400/WeekViewActivity.DAY_WIDTH;
 		HNS = width*HSPP;
 		HIH = (int)WeekViewActivity.FULLDAY_ITEM_HEIGHT;
 		
 		//Scroll info
 		scrolly = context.getScrollY();
 		scrollx = context.getScrollX();
 		
 
 		this.isInitialized = true;
 		context.refresh();
 		
 	}
 	
 	@Override
 	public void draw(Canvas canvas) {
 		super.draw(canvas);
 		
 		//First check that we can draw anything at all
 		if (this.getWidth() == 0) return;
 		if (this.getHeight() == 0) return;
 		if (this.isInEditMode()) return;	//can't draw in edit mode
 		
 		if (!this.isInitialized) { this.initialize(); return; }
 		
 		//calculate variables that may change from frame to frame
 		scrolly = context.getScrollY();
 		scrollx = context.getScrollX();
 		this.currentEpoch = date.getEpoch();
 		
 		Paint p = new Paint();
 		drawBackground(canvas);
 		
 		//need to draw header first. Drawing the header also adjusts the y value and height.
 		this.drawHeader(canvas,p);
 		PxD = TpX - PxH;
 		t = (scrolly+PxH)*WeekViewActivity.SECONDS_PER_PIXEL;
 
 		drawGrid(canvas,p);
 		
 		float dayX = (int)(0-WeekViewActivity.DAY_WIDTH+context.getScrollX());
 		float dayWidth = WeekViewActivity.DAY_WIDTH;
 		p = new Paint();
 
 		AcalDateTime currentDay = this.date.clone();
 		currentDay.addDays(-1);
 		//draw events
 		while (dayX<= width) {
 		
 			SimpleAcalEvent[][] timeTable = getImprovedTimeTable(getEventList(currentDay));
 			//draw visible events
 			if (timeTable.length  <=0) {
 				currentDay.addDays(1);
 				dayX+=dayWidth;
 				continue;
 			}
 			p.reset();
 			p.setStyle(Paint.Style.FILL);
 			int size = timeTable.length;
 			Set<SimpleAcalEvent> drawn = new HashSet<SimpleAcalEvent>(); //events can show up several times in the timetable, keep a record of whats already drawn
 			for (int i = 0; i<size;i++)  {
 				int curX = 0;
 				for(int j=0;j < timeTable[i].length;j++) {
 					if (timeTable[i][j] != null) {
 						if (drawn.contains(timeTable[i][j])) {
 							curX+=timeTable[i][j].getLastWidth();
 							continue;
 						}
 						drawn.add(timeTable[i][j]);
 						
 						//calculate width
 						int depth  = 0;
 						for (int k = j; k<=lastMaxX && (timeTable[i][k] == null || timeTable[i][k] == timeTable[i][j]); k++) depth++;
 						float singleWidth = (dayWidth/(lastMaxX+1))*(depth);	
 						SimpleAcalEvent event = timeTable[i][j];
 						drawVertical(event, canvas, (int)dayX+curX, (int)singleWidth);
 						event.setLastWidth((int)singleWidth);
 						curX+=singleWidth;
 					}
 				}
 			}
 			currentDay.addDays(1);
 			dayX+=dayWidth;
 		}
 		
 		//border
 		p.reset();
 		p.setStyle(Paint.Style.STROKE);
 		p.setColor(0xff333333);
 		canvas.drawRect(x, y, x+width, y+PxH, p);
 		//draw uber-cool shading effect
 		if (PxH != 0 ) {
 			int hhh = 1800/WeekViewActivity.SECONDS_PER_PIXEL;
 		 
 			int base = 0x333333;
 			int current = 0xc0;
 			int decrement = current/(hhh-3);
 		
 			int color = (current << 24)+base; 
 			p.setColor(color);
 			for (int i = 0; i < 3; i++) {
 				canvas.drawLine(x, y+PxH+i, x+width, y+PxH+i, p);
 			}
 		
 		
 			for (int i=3; i<hhh;i++) {
 				current-=decrement;
 				color = (current << 24)+base; 
 				p.setColor(color);
 				canvas.drawLine(x, y+i+PxH, x+width, y+i+PxH, p);
 			}
 		}
 		
 
 	}
 	
 	private void drawBackground(Canvas canvas) {
 		Paint p = new Paint();
 		p.setStyle(Paint.Style.FILL);
 		p.setColor(Color.parseColor("#AAf0f0f0"));
 		canvas.drawRect(x, y, width, TpX, p);
 	}
 	
 	public void drawGrid(Canvas canvas, Paint p) {
 		p.setStyle(Paint.Style.FILL);
 		int dayX = (int)(0-WeekViewActivity.DAY_WIDTH+scrollx);
 		AcalDateTime currentDay = context.getCurrentDate().clone();
 		currentDay.addDays(-1);
 		
 		//get the grid for each day
 		Bitmap dayGrid = context.getImageCache().getDayBox(TpX+(3600/WeekViewActivity.SECONDS_PER_PIXEL));
 		int y = PxH;
 		int offset = (t%3600)/WeekViewActivity.SECONDS_PER_PIXEL;
 		//dayGrid = Bitmap.createBitmap(dayGrid, 0, offset, dayGrid.getWidth(), PxD);
 		Rect src = new Rect(0,offset,dayGrid.getWidth(),offset+PxD);
 		while ( dayX <= width) {
 			//canvas.drawBitmap(dayGrid, dayX+x, y,p);
 			Rect dst = new Rect(dayX+x,y,dayX+x+dayGrid.getWidth(),y+PxD);
 			canvas.drawBitmap(dayGrid, src, dst, p);
 			if (!(currentDay.getWeekDay() == AcalDateTime.SATURDAY || currentDay.getWeekDay() == AcalDateTime.SUNDAY)) {
 				//draw a yellow box around work hours
 			}
 			dayX += WeekViewActivity.DAY_WIDTH;
 			currentDay.addDays(1);
 		}
 	}
 
 	
 
 
 	public void drawVertical(SimpleAcalEvent event, Canvas canvas, int x,  int width) {
 		if ( width < 1f ) return;
		int top = (int) (event.start - currentEpoch);
		if ( top < 0 ) top += 86400;
		top = (int)(((top%86400)-t)/WeekViewActivity.SECONDS_PER_PIXEL)+PxH;
 		top = Math.max(top,PxH);
 		int maxHeight =(int)Math.min(TpX, (event.end-event.start)/WeekViewActivity.SECONDS_PER_PIXEL);
		maxHeight = Math.max(maxHeight, WeekViewActivity.MINIMUM_DAY_EVENT_HEIGHT );
		int height = (int)Math.min(maxHeight, ((event.end-currentEpoch)%86400-t)/WeekViewActivity.SECONDS_PER_PIXEL);
 		Paint p = new Paint();
 		p.setStyle(Paint.Style.FILL);
 		p.setColor(0xff555555);
 		if (height <= 0 || width <= 0) return;
 		canvas.drawBitmap(context.getImageCache().getEventBitmap(event.resourceId,event.summary,event.colour,
 						width, height,width, maxHeight), x,top, new Paint());
 		
 	}
 
 	//for horizontal
 	private ArrayList<SimpleAcalEvent> getEventList(AcalDateTime day) {
 		ArrayList<AcalEvent> eventList = context.getEventsForDay(day);
 		ArrayList<SimpleAcalEvent> events = new ArrayList<SimpleAcalEvent>();
 		for (AcalEvent e : eventList) {
 			//only add events that cover less than one full calendar day
 			if (e.getDuration().getDurationMillis()/1000 >= 86400) continue;	//more than 24 hours, cant go in
 			AcalDateTime start = e.getStart().clone();
 			int startSec = start.getDaySecond();
 			if (startSec != 0) {
 				start.setHour(0); start.setMinute(0);start.setSecond(0); start.addDays(1);	//move forward to 00:00:00	
 			}
 			//start is now at the first 'midnight' of the event. Duration to end MUST be < 24hours for us to want this event
 			if(start.getDurationTo(e.getEnd()).getDurationMillis()/1000 >=86400) continue;
 			SimpleAcalEvent ret =SimpleAcalEvent.getSimpleEvent(e); 
 			events.add(ret);
 		}
 		return events;
 	}
 
 	
 	public void drawHorizontal(SimpleAcalEvent event, Canvas c, int depth) {
 		event.calulateMaxWidth(width, HSPP);
 		int x = Math.max((int)(event.start-HST)/HSPP,0);
 		int y = HIH*depth;
 		int maxWidth = event.getMaxWidth();
 		int actualWidth = (int)Math.min(Math.min(event.getActualWidth(), width-x),(event.end-HST)/HSPP); 
 		int height = HIH;
 		if (height <= 0 || actualWidth<=0 || maxWidth<=0) return;
 		c.drawBitmap(context.getImageCache().getEventBitmap(event.resourceId,event.summary,event.colour,
 					actualWidth, height, maxWidth, height), x,y, new Paint());
 	}
 	
 	/**
 	 * Calculates a timetable of rows to place horizontal (multi-day) events in order
 	 * that the events do not overlap one other.
 	 * @param events A one-dimensional array of events
 	 * @return A two-dimensional array of events
 	 */
 	private SimpleAcalEvent[][] getTimeTable(List<SimpleAcalEvent> events) {
 		if (HTimetable != null && HSimpleList != null) {
 			if (events.containsAll(HSimpleList) && events.size() == HSimpleList.size()) return HTimetable;
 		}
 		Collections.sort(events);
 		SimpleAcalEvent[][] timetable = new SimpleAcalEvent[events.size()][events.size()]; //maximum possible
 		int depth = 0;
 		for (int x = 0; x < events.size(); x++) {
 			int i = 0;
 			boolean go = true;
 			while(go) {
 				SimpleAcalEvent[] row = timetable[i];
 				int j=0;
 				while(true) {
 					if (row[j] == null) { row[j] = events.get(x); go=false; break; }
 					else if (!(row[j].end > (events.get(x).start))) {j++; continue; }
 					else break;
 				}
 				i++;
 			}
 			depth = Math.max(depth,i);
 		}
 		HDepth = depth;
 		HTimetable = timetable;
 		return timetable;
 	}
 	
 	private SimpleAcalEvent[][] getImprovedTimeTable(List<SimpleAcalEvent> events) {
 		Collections.sort(events);
 		SimpleAcalEvent[][] timetable = new SimpleAcalEvent[events.size()][events.size()]; //maximum possible
 		int maxX = 0;
 		for (SimpleAcalEvent seo : events){
 			int x = 0; int y = 0;
 			while (timetable[y][x] != null) {
 				if (seo.overlaps(timetable[y][x])) {
 					
 					//if our end time is before [y][x]'s, we need to extend[y][x]'s range
 					if (seo.end < (timetable[y][x].end)) timetable[y+1][x] = timetable[y][x];
 					x++;
 				} else {
 					y++;
 				}
 			}
 			timetable[y][x] = seo;
 			if (x > maxX) maxX = x;
 		}
 		lastMaxX = maxX;
 		return timetable;
 	}
 	
 	private ArrayList<SimpleAcalEvent> getFullDayEventList(AcalDateRange range) {
 		ArrayList<AcalEvent> eventList = context.getEventsForDays(range);
 		if (HList != null && HSimpleList != null && eventList.containsAll(HList) && eventList.size() == HList.size()) {
 			return this.HSimpleList;
 		}
 		HList = eventList;
 		ArrayList<SimpleAcalEvent> events = new ArrayList<SimpleAcalEvent>();
 		
 		for (AcalEvent e : eventList) {
 			e.dtstart.applyLocalTimeZone();
 			//only add events that cover at least one full calendar day
 			if (e.getDuration().getDurationMillis()/1000 < 86400) continue;	//less than 24 hours, cant go in
 			AcalDateTime start = e.getStart().clone();
 			int startSec = start.getDaySecond();
 			if (startSec != 0) {
 				start.setHour(0); start.setMinute(0);start.setSecond(0); start.addDays(1);	//move forward to 00:00:00	
 			}
 			//start is now at the first 'midnight' of the event. Duration to end MUST be > 24hours for us to want this event
 			if(start.getDurationTo(e.getEnd()).getDurationMillis()/1000 <86400) continue;
 			SimpleAcalEvent seo = SimpleAcalEvent.getSimpleEvent(e);
 			events.add(seo);
 		}
 		return events;
 	}
 
 }
