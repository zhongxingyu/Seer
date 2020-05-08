 /**
  * 
  */
 package de.questmaster.tudmensa;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.CharArrayBuffer;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.database.DataSetObserver;
 import android.net.Uri;
 import android.os.Bundle;
 
 /**
  * This Wrapper replaces the picture type with its picture resource id.
  * 
  * @author Daniel
  */
 public class MealCursorWrapper implements Cursor {
 
 	private Cursor mCursor;
 	private MensaMealsSettings.Settings mSettings = new MensaMealsSettings.Settings();
 
 	/**
 	 * 
 	 */
 	public MealCursorWrapper(Cursor c, Context ct) {
 		mCursor = c;
 
 		// Read settings
 		mSettings.ReadSettings(ct);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#close()
 	 */
 	@Override
 	public void close() {
 		mCursor.close();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#copyStringToBuffer(int,
 	 * android.database.CharArrayBuffer)
 	 */
 	@Override
 	public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
 		mCursor.copyStringToBuffer(columnIndex, buffer);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#deactivate()
 	 */
 	@Override
 	public void deactivate() {
 		mCursor.deactivate();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getBlob(int)
 	 */
 	@Override
 	public byte[] getBlob(int columnIndex) {
 		return mCursor.getBlob(columnIndex);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getColumnCount()
 	 */
 	@Override
 	public int getColumnCount() {
 		return mCursor.getColumnCount();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getColumnIndex(java.lang.String)
 	 */
 	@Override
 	public int getColumnIndex(String columnName) {
 		return mCursor.getColumnIndex(columnName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getColumnIndexOrThrow(java.lang.String)
 	 */
 	@Override
 	public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
 		return mCursor.getColumnIndexOrThrow(columnName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getColumnName(int)
 	 */
 	@Override
 	public String getColumnName(int columnIndex) {
 		return mCursor.getColumnName(columnIndex);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getColumnNames()
 	 */
 	@Override
 	public String[] getColumnNames() {
 		return mCursor.getColumnNames();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getCount()
 	 */
 	@Override
 	public int getCount() {
 		return mCursor.getCount();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getDouble(int)
 	 */
 	@Override
 	public double getDouble(int columnIndex) {
 		return mCursor.getDouble(columnIndex);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getExtras()
 	 */
 	@Override
 	public Bundle getExtras() {
 		return mCursor.getExtras();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getFloat(int)
 	 */
 	@Override
 	public float getFloat(int columnIndex) {
 		return mCursor.getFloat(columnIndex);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getInt(int)
 	 */
 	@Override
 	public int getInt(int columnIndex) {
 		return mCursor.getInt(columnIndex);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getLong(int)
 	 */
 	@Override
 	public long getLong(int columnIndex) {
 		return mCursor.getLong(columnIndex);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getPosition()
 	 */
 	@Override
 	public int getPosition() {
 		return mCursor.getPosition();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getShort(int)
 	 */
 	@Override
 	public short getShort(int columnIndex) {
 		return mCursor.getShort(columnIndex);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getString(int)
 	 */
 	@Override
 	public String getString(int columnIndex) {
 		String result = mCursor.getString(columnIndex);
 
 		if (columnIndex == this.getColumnIndex(MealsDbAdapter.KEY_TYPE)) {
			// Setup Theme
 			if (mSettings.m_sThemes.equals("dark")) {
 				// create type drawable
 				if (result.equals("F")) {
 					result = String.valueOf(R.drawable.meal_f_d);
 				} else if (result.equals("G")) {
 					result = String.valueOf(R.drawable.meal_g_d);
 				} else if (result.equals("K")) {
 					result = String.valueOf(R.drawable.meal_k_d);
 				} else if (result.equals("R")) {
 					result = String.valueOf(R.drawable.meal_r_d);
 				} else if (result.equals("RS")) {
 					result = String.valueOf(R.drawable.meal_rs_d);
 				} else if (result.equals("S")) {
 					result = String.valueOf(R.drawable.meal_s_d);
 				} else if (result.equals("V")) {
 					result = String.valueOf(R.drawable.meal_v_d);
 				} else {
 					result = String.valueOf(R.drawable.essen_d);
 				}
 			} else if (mSettings.m_sThemes.equals("light")) {
 				// create type drawable
 				if (result.equals("F")) {
 					result = String.valueOf(R.drawable.meal_f);
 				} else if (result.equals("G")) {
 					result = String.valueOf(R.drawable.meal_g);
 				} else if (result.equals("K")) {
 					result = String.valueOf(R.drawable.meal_k);
 				} else if (result.equals("R")) {
 					result = String.valueOf(R.drawable.meal_r);
 				} else if (result.equals("RS")) {
 					result = String.valueOf(R.drawable.meal_rs);
 				} else if (result.equals("S")) {
 					result = String.valueOf(R.drawable.meal_s);
 				} else if (result.equals("V")) {
 					result = String.valueOf(R.drawable.meal_v);
 				} else {
 					result = String.valueOf(R.drawable.essen);
 				}
 			}
 
 		}
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#getWantsAllOnMoveCalls()
 	 */
 	@Override
 	public boolean getWantsAllOnMoveCalls() {
 		return mCursor.getWantsAllOnMoveCalls();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#isAfterLast()
 	 */
 	@Override
 	public boolean isAfterLast() {
 		return mCursor.isAfterLast();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#isBeforeFirst()
 	 */
 	@Override
 	public boolean isBeforeFirst() {
 		return mCursor.isBeforeFirst();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#isClosed()
 	 */
 	@Override
 	public boolean isClosed() {
 		return mCursor.isClosed();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#isFirst()
 	 */
 	@Override
 	public boolean isFirst() {
 		return mCursor.isFirst();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#isLast()
 	 */
 	@Override
 	public boolean isLast() {
 		return mCursor.isLast();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#isNull(int)
 	 */
 	@Override
 	public boolean isNull(int columnIndex) {
 		return mCursor.isNull(columnIndex);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#move(int)
 	 */
 	@Override
 	public boolean move(int offset) {
 		return mCursor.move(offset);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#moveToFirst()
 	 */
 	@Override
 	public boolean moveToFirst() {
 		return mCursor.moveToFirst();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#moveToLast()
 	 */
 	@Override
 	public boolean moveToLast() {
 		return mCursor.moveToLast();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#moveToNext()
 	 */
 	@Override
 	public boolean moveToNext() {
 		return mCursor.moveToNext();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#moveToPosition(int)
 	 */
 	@Override
 	public boolean moveToPosition(int position) {
 		return mCursor.moveToPosition(position);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#moveToPrevious()
 	 */
 	@Override
 	public boolean moveToPrevious() {
 		return mCursor.moveToPrevious();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#registerContentObserver(android.database.
 	 * ContentObserver)
 	 */
 	@Override
 	public void registerContentObserver(ContentObserver observer) {
 		mCursor.registerContentObserver(observer);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#registerDataSetObserver(android.database.
 	 * DataSetObserver)
 	 */
 	@Override
 	public void registerDataSetObserver(DataSetObserver observer) {
 		mCursor.registerDataSetObserver(observer);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#requery()
 	 */
 	@Override
 	public boolean requery() {
 		return mCursor.requery();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#respond(android.os.Bundle)
 	 */
 	@Override
 	public Bundle respond(Bundle extras) {
 		return mCursor.respond(extras);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.database.Cursor#setNotificationUri(android.content.ContentResolver
 	 * , android.net.Uri)
 	 */
 	@Override
 	public void setNotificationUri(ContentResolver cr, Uri uri) {
 		mCursor.setNotificationUri(cr, uri);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#unregisterContentObserver(android.database.
 	 * ContentObserver)
 	 */
 	@Override
 	public void unregisterContentObserver(ContentObserver observer) {
 		mCursor.unregisterContentObserver(observer);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.database.Cursor#unregisterDataSetObserver(android.database.
 	 * DataSetObserver)
 	 */
 	@Override
 	public void unregisterDataSetObserver(DataSetObserver observer) {
 		mCursor.unregisterDataSetObserver(observer);
 	}
 
 }
