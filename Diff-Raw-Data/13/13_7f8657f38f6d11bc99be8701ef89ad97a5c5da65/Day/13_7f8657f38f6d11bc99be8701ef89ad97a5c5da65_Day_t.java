 package ch.almana.android.stechkarte.model;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.os.Bundle;
 import ch.almana.android.stechkarte.provider.db.DB;
 import ch.almana.android.stechkarte.provider.db.DB.Days;
 import ch.almana.android.stechkarte.provider.db.DB.Timestamps;
 import ch.almana.android.stechkarte.utils.Settings;
 
 public class Day {
 	
 	private long id = -1;
 	private long dayRef = 0;
 	private float hoursWorked = 0;
 	private float hoursTarget = -1;
 	private float holyday = 0;
 	private float holydayLeft = 0;
 	private float overtime = 0;
 	private boolean error = false;
 	private boolean fixed = false;
 	
 	public Day() {
 		this(DayAccess.dayRefFromTimestamp(System.currentTimeMillis()));
 	}
 	
 	public Day(long dayRef) {
 		super();
 		this.dayRef = dayRef;
 		this.hoursTarget = Settings.getInstance().getHoursTarget();
 	}
 	
 	public Day(Day day) {
 		super();
 		id = day.id;
 		dayRef = day.dayRef;
 		hoursWorked = day.hoursWorked;
 		hoursTarget = day.hoursTarget;
 		holyday = day.holyday;
 		holydayLeft = day.holydayLeft;
 		overtime = day.overtime;
 		error = day.error;
 		fixed = day.fixed;
 	}
 	
 	public Day(Cursor c) {
 		super();
 		id = c.getLong(DB.INDEX_ID);
 		dayRef = c.getLong(Days.INDEX_DAYREF);
 		hoursWorked = c.getFloat(Days.INDEX_HOURS_WORKED);
 		hoursTarget = c.getFloat(Days.INDEX_HOURS_TARGET);
 		holyday = c.getFloat(Days.INDEX_HOLIDAY);
 		holydayLeft = c.getFloat(Days.INDEX_HOLIDAY_LEFT);
 		overtime = c.getFloat(Days.INDEX_OVERTIME);
 		setError(c.getInt(Days.INDEX_ERROR));
 		setFixed(c.getInt(Days.INDEX_FIXED));
 		
 	}
 	
 	public Day(Bundle instanceState) {
 		super();
 		readFromBundle(instanceState);
 	}
 	
 	public ContentValues getValues() {
 		ContentValues values = new ContentValues();
 		if (id > -1) {
 			values.put(DB.NAME_ID, id);
 		}
 		values.put(Days.NAME_DAYREF, getDayRef());
 		values.put(Days.NAME_HOURS_WORKED, getHoursWorked());
 		values.put(Days.NAME_HOURS_TARGET, getHoursTarget());
 		values.put(Days.NAME_HOLIDAY, getHolyday());
 		values.put(Days.NAME_HOLIDAY_LEFT, getHolydayLeft());
 		values.put(Days.NAME_OVERTIME, getOvertime());
 		values.put(Days.NAME_ERROR, getError());
 		values.put(Days.NAME_FIXED, getFixed());
 		return values;
 	}
 	
 	public void saveToBundle(Bundle bundle) {
 		if (id > -1) {
 			bundle.putLong(DB.NAME_ID, id);
 		} else {
 			bundle.putLong(DB.NAME_ID, -1);
 		}
 		bundle.putLong(Days.NAME_DAYREF, getDayRef());
 		bundle.putFloat(Days.NAME_HOURS_WORKED, getHoursWorked());
 		bundle.putFloat(Days.NAME_HOURS_TARGET, getHoursTarget());
 		bundle.putFloat(Days.NAME_HOLIDAY, getHolyday());
 		bundle.putFloat(Days.NAME_HOLIDAY_LEFT, getHolydayLeft());
 		bundle.putFloat(Days.NAME_OVERTIME, getOvertime());
 		bundle.putInt(Days.NAME_ERROR, getError());
 		bundle.putInt(Days.NAME_FIXED, getFixed());
 	}
 	
 	public void readFromBundle(Bundle bundle) {
 		id = bundle.getLong(DB.NAME_ID);
 		dayRef = bundle.getLong(Days.NAME_DAYREF);
 		hoursWorked = bundle.getFloat(Days.NAME_HOURS_WORKED);
 		hoursTarget = bundle.getFloat(Days.NAME_HOURS_TARGET);
 		holyday = bundle.getFloat(Days.NAME_HOLIDAY);
 		holydayLeft = bundle.getFloat(Days.NAME_HOLIDAY_LEFT);
 		overtime = bundle.getFloat(Days.NAME_OVERTIME);
 		setError(bundle.getInt(Days.NAME_ERROR));
 		setFixed(bundle.getInt(Days.NAME_FIXED));
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof Day) {
 			Day day = (Day) o;
 			
 			return dayRef == day.dayRef && error == day.error && fixed == day.fixed && holyday == day.holyday
 					&& holydayLeft == day.holydayLeft && hoursTarget == day.hoursTarget
 					&& hoursWorked == day.hoursWorked && overtime == day.overtime;
 		}
 		return super.equals(o);
 	}
 	
 	public Cursor getTimestamps() {
 		return TimestampAccess.getInstance().query(Timestamps.NAME_DAYREF + "=" + dayRef, Timestamps.REVERSE_SORTORDER);
 	}
 	
 	public long getDayRef() {
 		return dayRef;
 	}
 	
 	public void setDayRef(long dayRef) {
 		this.dayRef = dayRef;
 	}
 	
 	public float getHoursWorked() {
 		return hoursWorked;
 	}
 	
 	public void setHoursWorked(float hoursWorked) {
 		this.hoursWorked = hoursWorked;
 	}
 	
 	public float getHoursTarget() {
 		return hoursTarget;
 	}
 	
 	public void setHoursTarget(float hoursTarget) {
 		this.hoursTarget = hoursTarget;
 	}
 	
 	public float getHolyday() {
 		return holyday;
 	}
 	
 	public void setHolyday(float holyday) {
 		this.holyday = holyday;
 	}
 	
 	public float getHolydayLeft() {
 		return holydayLeft;
 	}
 	
 	public void setHolydayLeft(float holydayLeft) {
 		this.holydayLeft = holydayLeft;
 	}
 	
 	public float getDayOvertime() {
 		return hoursWorked - hoursTarget;
 	}
 
 	public float getOvertime() {
 		return overtime;
 	}
 	
 	public void setOvertime(float overtime) {
 		this.overtime = overtime;
 	}
 	
 	public void setId(long id) {
 		this.id = id;
 	}
 	
 	public long getId() {
 		return id;
 	}
 	
 	public void setError(boolean error) {
 		this.error = error;
 	}
 	
 	public void setError(int error) {
 		this.error = error > 0 ? true : false;
 	}
 	
 	public int getError() {
 		return isError() ? 1 : 0;
 	}
 	
 	public boolean isError() {
 		return error;
 	}
 	
 	public void setFixed(boolean fixed) {
 		this.fixed = fixed;
 	}
 	
 	public boolean isFixed() {
 		return fixed;
 	}
 	
 	public void setFixed(int fixed) {
 		this.fixed = fixed > 0 ? true : false;
 	}
 	
 	public int getFixed() {
 		return fixed ? 1 : 0;
 	}
 	
 	public String getDayString() {
 		return getDayRef() + "";
 	}
 	
 	public void setYear(int year) {
 		dayRef = year * 10000 + getMonth() * 100 + getDay();
 	}
 	
 	public void setDay(int dayOfMonth) {
 		dayRef = getYear() * 10000 + getMonth() * 100 + dayOfMonth;
 	}
 	
 	public void setMonth(int monthOfYear) {
 		dayRef = getYear() * 10000 + (monthOfYear) * 100 + getDay();
 	}
 	
 	public int getYear() {
 		String s = dayRef + "";
 		int i = Integer.parseInt(s.substring(0, 4));
 		return i;
 	}
 	
 	public int getMonth() {
 		String s = dayRef + "";
 		String substring = s.substring(4, 6);
		int i = Integer.parseInt(substring);
 		return i;
 	}
 	
 	public int getDay() {
 		String s = dayRef + "";
 		String substring = s.substring(6, 8);
 		int i = Integer.parseInt(substring);
 		return i;
 	}
 	
 }
