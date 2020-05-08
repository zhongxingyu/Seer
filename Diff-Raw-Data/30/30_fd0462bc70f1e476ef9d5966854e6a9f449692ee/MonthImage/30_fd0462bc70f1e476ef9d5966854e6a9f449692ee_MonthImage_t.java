 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
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
 
 package com.morphoss.acal.views;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 
 import com.morphoss.acal.acaltime.AcalDateTime;
 
 public class MonthImage extends YearViewNode {
 
 	public static final String TAG = "aCal MonthImage";
 	
 	private AcalDateTime myDate;
 	private int x;
 	private Context context;
 	private Bitmap headerBMP;
 	private Bitmap dayHeadsBMP;
 	private Bitmap daySectionBMP;
 	
 	public MonthImage(Context context, int year, int month, int selectedDay, int x, MonthImageGenerator ig) {
 		super();
 		this.x=x;
		this.myDate = new AcalDateTime(year,month,1,0,0,0,null);
 		this.context = context;
 		this.headerBMP = ig.getMonthHeader(myDate);
 		this.dayHeadsBMP = ig.getDayHeaders();
 		this.daySectionBMP = ig.getDaySection(myDate);
 	}
 	
 	@Override
 	protected void draw(Canvas canvas, int y) {
 		Paint paint = new Paint();
 		
 		canvas.drawBitmap(headerBMP, x, y, paint);
 		canvas.drawBitmap(dayHeadsBMP, x, y+headerBMP.getHeight(), paint);
 		canvas.drawBitmap(daySectionBMP, x, y+headerBMP.getHeight()+dayHeadsBMP.getHeight(), paint);
 	}
 
 	public int getHeight() { return headerBMP.getHeight()+dayHeadsBMP.getHeight()+daySectionBMP.getHeight(); }
 
 	public int getMonth() { return this.myDate.getMonth(); }
 	public int getYear() { return this.myDate.getYear(); }
 	public AcalDateTime getDate() {
 		return myDate.clone();
 	}
 
 	@Override
 	public boolean isUnder(int x) {
 		return(x>= this.x  && x <= this.x+this.headerBMP.getWidth() );
 	}
 	
 }
