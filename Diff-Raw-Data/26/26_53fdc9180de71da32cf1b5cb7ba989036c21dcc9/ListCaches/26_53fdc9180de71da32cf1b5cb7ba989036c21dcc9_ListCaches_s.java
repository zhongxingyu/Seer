 package com.zeyomir.ocfun.controller;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Location;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.View;
 import android.widget.ImageView;
 import com.zeyomir.ocfun.R;
 import com.zeyomir.ocfun.controller.helper.LocationHelper;
 import com.zeyomir.ocfun.dao.CacheDAO;
 import com.zeyomir.ocfun.dao.InternalResourceMapper;
 import com.zeyomir.ocfun.gui.SingleCache;
 import org.holoeverywhere.widget.TextView;
 
 public class ListCaches {
 	private static Location location = null;
 
 	private static CacheDAO dao;
 
 	static private final SimpleCursorAdapter.ViewBinder imagesBinder = new SimpleCursorAdapter.ViewBinder() {
 
 		@Override
 		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 			switch (view.getId()) {
 				case R.id.list_type_ico:
 					int i = cursor.getInt(columnIndex);
 					((ImageView) view).setImageResource(InternalResourceMapper
 							.map(i));
 					return true;
 				case R.id.list_distance:
 					if (location == null)
						((TextView) view).setText("Nieznana odleg�o��");
 					else {
 						String[] coords = cursor.getString(columnIndex)
 								.split("\\|");
 						Location l = new Location("???");
 						l.setLatitude(Double.parseDouble(coords[0]));
 						l.setLongitude(Double.parseDouble(coords[1]));
 
 						String d = LocationHelper.getDistance(location, l);
 						String a = LocationHelper.getAzimuth(location, l);
 						String distance = d + ", " + a;
 						((TextView) view).setText(distance);
 					}
 					return true;
 				case R.id.list_found:
 					if (cursor.getString(columnIndex).equals("true"))
 						((ImageView) view).setImageResource(R.drawable.found);
 					else
 						((ImageView) view).setImageResource(R.drawable.empty);
 					return true;
 				default:
 					return false;
 			}
 		}
 	};
 
 	public static SimpleCursorAdapter createAdapter(Context c, Location l) {
 		Cursor cursor = dao.list("");
 		location = l;
 		SimpleCursorAdapter la = new SimpleCursorAdapter(c,
 				R.layout.caches_row, cursor, CacheDAO.from, CacheDAO.to);
 		/*
 		 * la.setFilterQueryProvider(new FilterQueryProvider() { public Cursor
 		 * runQuery(CharSequence constraint) { String partialItemName = null; if
 		 * (constraint != null) { partialItemName = constraint.toString(); }
 		 * Cursor filtered = CacheDAO.list(partialItemName);
 		 * 
 		 * return filtered; } });
 		 */// TODO for searching
 		la.setViewBinder(imagesBinder);
 		la.setStringConversionColumn(cursor
 				.getColumnIndexOrThrow(CacheDAO.searchableColumn));
 		return la;
 	}
 
 	public static Intent createIntent(Context c, long id) {
 		Intent i = new Intent(c, SingleCache.class);
 		i.putExtra(CacheDAO.idColumn, id);
 		return i;
 	}
 
 	public static void clean() {
 		dao.clean();
 		dao.close();
 		ListImages.clean();
 		ListLogs.clean();
 
 		dao.open();
 	}
 
 	public static void openDB() {
 		if (dao == null)
 			dao = new CacheDAO();
 		dao.open();
 	}
 
 	public static void closeDB() {
 		dao.close();
 	}
 }
