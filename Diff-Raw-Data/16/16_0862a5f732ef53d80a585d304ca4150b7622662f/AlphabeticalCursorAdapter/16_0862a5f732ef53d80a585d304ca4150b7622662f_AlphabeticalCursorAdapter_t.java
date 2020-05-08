 package edu.vanderbilt.vm.guide.ui.adapter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.vanderbilt.vm.guide.R;
 import edu.vanderbilt.vm.guide.db.GuideDBConstants;
 import edu.vanderbilt.vm.guide.util.DBUtils;
 import edu.vanderbilt.vm.guide.util.Geomancer;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 import android.location.Location;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 @SuppressLint("UseSparseArrays")
 public class AlphabeticalCursorAdapter extends BaseAdapter {
 	
 	private final Cursor mCursor;
 	private final Context mCtx;
 	private int mIdColIx;
 	private int mNameColIx;
 	private int mCatColIx;
 	private int mLatColIx;
 	private int mLngColIx;
 	private HashMap<Integer,Integer> mEnigma;
 	private final int CATEGORIES = 27;
 	private ArrayList<HeaderRecord> mRecord = new ArrayList<HeaderRecord>();
 	@SuppressWarnings("unused")
 	private static final Logger logger = LoggerFactory
 			.getLogger("ui.PlaceTabFragment");
 	
 	public AlphabeticalCursorAdapter() throws Exception {
 		throw new Exception("Do not call this constructor");
 	}
 	
 	public AlphabeticalCursorAdapter(Context ctx, Cursor cursor) {
 		mCtx = ctx;
 		mCursor = cursor;
 		
 		mIdColIx = mCursor.getColumnIndex(GuideDBConstants.PlaceTable.ID_COL);
 		if(mIdColIx == -1) {
 			throw new SQLiteException("Cursor does not have an id column");
 		}
 		mNameColIx = mCursor.getColumnIndex(GuideDBConstants.PlaceTable.NAME_COL);
 		if(mNameColIx == -1) {
 			throw new SQLiteException("Cursor does not have a name column");
 		}
 		mCatColIx = mCursor.getColumnIndex(GuideDBConstants.PlaceTable.CATEGORY_COL);
 		if(mCatColIx == -1) {
 			throw new SQLiteException("Cursor does not have a category column");
 		}
 		mLatColIx = mCursor.getColumnIndex(GuideDBConstants.PlaceTable.LATITUDE_COL);
 		if(mLatColIx == -1) {
 			throw new SQLiteException("Cursor does not have a latitude column");
 		}
 		mLngColIx = mCursor.getColumnIndex(GuideDBConstants.PlaceTable.LONGITUDE_COL);
 		if(mLngColIx == -1) {
 			throw new SQLiteException("Cursor does not have a longitude column");
 		}
 		
 		mEnigma = new HashMap<Integer,Integer>();
 		buildMap();
 	}
 
 	@Override
 	public int getCount() {
 		return mCursor.getCount() + CATEGORIES;
 	}
 
 	@Override
 	public Object getItem(int position) {
 		checkPosition(position);
 		
 		int x = mEnigma.get(position);
 		while (x < 0) {
			position++;
			x = mEnigma.get(position);
 		}
 		
 		mCursor.moveToPosition(x);
 		return DBUtils.getPlaceFromCursor(mCursor);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		checkPosition(position);
 		
 		int x = mEnigma.get(position);
 		while (x < 0) {
			position++;
			x = mEnigma.get(position);
 		}
 		
 		mCursor.moveToPosition(x);
 		return mCursor.getInt(mIdColIx);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		checkPosition(position);
 		
 		int x = 0;
 		x = mEnigma.get(position);
		boolean isHeader = (x < 0)? true : false;
 		
 		LinearLayout layout = null;
 		if (convertView == null) {
 			if (isHeader) {
 				layout = (LinearLayout) LayoutInflater.from(mCtx).inflate(
 						R.layout.place_list_header,null);
 			} else {
 				layout = (LinearLayout) LayoutInflater.from(mCtx).inflate(
 						R.layout.place_list_item, null);
 			}
 			layout.setTag(layout);
 		} else {
 			layout = (LinearLayout) convertView.getTag();
 		}
 		
 		if (isHeader) {
 			HeaderRecord record = mRecord.get(-x - 1);
 			((TextView) layout.findViewById(R.id.header_title)).setText(
 					record.mTitle);
 		} else {
 			mCursor.moveToPosition(x);
 			((TextView) layout.findViewById(R.id.placelist_item_title))
 				.setText(
 						mCursor.getString(mNameColIx));
 			
 			((ImageView) layout.findViewById(R.id.placelist_item_thunbnail))
 				.setImageResource(R.drawable.home);
 			
 			Location tmp = new Location("Temp");
 			tmp.setLatitude(Double.parseDouble(mCursor.getString(mLatColIx)));
 			tmp.setLongitude(Double.parseDouble(mCursor.getString(mLngColIx)));
 			
 			int dist = (int) tmp.distanceTo(Geomancer.getDeviceLocation());
 			
 			((TextView) layout.findViewById(R.id.placelist_item_distance))
 				.setText(Integer.toString(dist) + " m");
 			
 		}
 		return layout;
 	}
 	
 	private void checkPosition(int position) {
 		if (position < 0 || position >= mCursor.getCount() + CATEGORIES) {
 			throw new IndexOutOfBoundsException("Position " + position
 					+ " is invalid for a cursor with " + getCount() + "rows.");
 		}
 	}
 	
 	private void buildMap(){
 		
 		// Initializing the Header records
 		char c = 'A';
 		for (int i = 0;i < CATEGORIES; i++) {
 			mRecord.add(new HeaderRecord(c));
 			c++;
 		}
 		
 		HeaderRecord rec = new HeaderRecord();
 		rec.mTitle = "0-9";
 		mRecord.add(rec);
 		
 		// Scanning the database to index
 		if (!mCursor.moveToFirst()) {
 			return;
 		}
 		
 		do {
 			
 			String initial = mCursor.getString(mNameColIx).substring(0, 1);
 			
 			for (int i = 0; i < mRecord.size() - 1;i++) {
 				if (initial.equalsIgnoreCase(mRecord.get(i).mTitle)) {
 					mRecord.get(i).mChild.add(mCursor.getPosition());
 					break;
 				} else if (i == mRecord.size() - 2) {
 					mRecord.get(mRecord.size() - 1).mChild.add(
 							mCursor.getPosition());
 				}
 			}
 			
 		} while (mCursor.moveToNext());
 		
 		// Build HashMap based of the information stored in mRecord
 		int listPosition = 0;
 		for (int i = 0; i < mRecord.size(); i++) {
 			mRecord.get(i).mPosition = listPosition;
 			mEnigma.put(listPosition, -(i + 1));
 			listPosition++;
 			
 			for (Integer child : mRecord.get(i).mChild) {
 				mEnigma.put(listPosition, child);
 				listPosition++;
 			}
 			
 		}
 			
 	}
 	
 	static class HeaderRecord {
 		
 		int mPosition;
 		String mTitle;
 		ArrayList<Integer> mChild;
 		
 		public HeaderRecord(char c) {
 			mPosition = 0;
 			mTitle = String.valueOf(c);
 			mChild = new ArrayList<Integer>();
 		}
 		
 		public HeaderRecord() {
 			mPosition = 0;
 			mTitle = "";
 			mChild = new ArrayList<Integer>();
 		}
 		
 	}
 	
 }
