 package com.example.ipcplayer.provider;
 
 import com.example.ipcplayer.utils.LogUtil;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 
 public class MusicDBProvider extends ContentProvider{
 	private MusicDBHelper mDbHelper;
 	private Context mContext ;
 	private static String TAG = MusicDBProvider.class.getSimpleName();
 	
 	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH) ;
 	public static final int MUSICINFO = 1;
 	public static final int MUSICINFO_ITEM = 2;
 	
 	public static final int DOWNLOADINFO = 21;
 	public static final int DOWNLOADINFO_ITEM = 22;
 	
 	static {
 		LogUtil.d(TAG + " static code block init uri");
		URI_MATCHER.addURI(MusicDB.AUTHORITY, "/musicinfo", MUSICINFO);
		URI_MATCHER.addURI(MusicDB.AUTHORITY, "/musicinfo/#", MUSICINFO_ITEM);
 		
 		URI_MATCHER.addURI(MusicDB.AUTHORITY, "/downloadinfo",DOWNLOADINFO);
 		URI_MATCHER.addURI(MusicDB.AUTHORITY, "/downloadinfo/#", DOWNLOADINFO_ITEM);
		LogUtil.d(TAG + " static code block init uri  URI_MATCHER: " + URI_MATCHER.toString());
 	}
 	
 	@Override
 	public boolean onCreate() {
 		// TODO Auto-generated method stub
 		LogUtil.d(TAG + " onCreate ");
 		mContext = getContext();
 		mDbHelper = MusicDBHelper.getInstance(mContext);
 		return true;
 	}
 
 
 	@Override
 	public String getType(Uri uri) {
 		// TODO Auto-generated method stub
 		LogUtil.d(TAG + " getType ");
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		// TODO Auto-generated method stub
 		LogUtil.d(TAG + " insert ");
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		Uri newUri = null ;
 		long rowId ;
		
 		int match = URI_MATCHER.match(uri);
 		LogUtil.d(" match= " + match);
 		switch(match){
 		case MUSICINFO:
 		{
 			LogUtil.d(TAG + " insert MUSICINFO ");
 	    	rowId = db.insert(MusicDBHelper.TABLE_MUSICINFO, "", values);
 	    	if(rowId > 0){
 	    		newUri = ContentUris.withAppendedId(uri, rowId);
 	    		getContext().getContentResolver().notifyChange(newUri, null);
 	    		break;
 	    	}
 		}
 		
 		case DOWNLOADINFO:
 		{
 			LogUtil.d(TAG + " insert DOWNLOADINFO ");
 			rowId = db.insert(MusicDBHelper.TABLE_DOWNLOADINFO, "", values);
 			if(rowId > 0){
 				newUri = ContentUris.withAppendedId(uri,rowId);
 				getContext().getContentResolver().notifyChange(newUri,null);
 				break;
 			}
 		}
 		
 		default :
 		{
 			throw new IllegalArgumentException("insert uri error ! " + uri);
 		}
 		
 	    }
 		return newUri;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		// TODO Auto-generated method stub
 		LogUtil.d(TAG + " query ");
 		LogUtil.d(TAG + " query  uri = " + uri);
 		
 		int table = URI_MATCHER.match(uri);
 		LogUtil.d(TAG + " query table = " + table);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 		
 		switch(table){
 		case MUSICINFO :
 		{
 			LogUtil.d(TAG + " query MUSICINFO ");
 			qb.setTables(MusicDBHelper.TABLE_MUSICINFO);
 			break ;
 		}
 		
 		case MUSICINFO_ITEM:
 		{
 			LogUtil.d(TAG + " query MUSICINFO_ITEM ");
 			qb.setTables(MusicDBHelper.TABLE_MUSICINFO);
 			qb.appendWhere("_id = "+uri.getPathSegments().get(1));
 			break ;
 		}
 		
 		case DOWNLOADINFO:
 		{
 			LogUtil.d(TAG + " DONWLOADINFO ");
 			qb.setTables(MusicDBHelper.TABLE_DOWNLOADINFO);
 		    break ;
 		}
 		
 		case DOWNLOADINFO_ITEM:
 		{
 			LogUtil.d(TAG + " DOWNLOADINFO_ITEM ");
 			qb.setTables(MusicDBHelper.TABLE_DOWNLOADINFO);
 			qb.appendWhere("_id = " + uri.getPathSegments().get(1));
 		   	break ;
 		}
 		
 		default :
 		{
 			throw new IllegalArgumentException("query Uri error ! " + uri);
 		}
 		
 		}
 		
 		Cursor cursor= null;
 		try{
      		cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
 		}catch(Exception e){
 			e.printStackTrace();
 			cursor = null;
 		}
 		
 		if(cursor != null)
 		cursor.setNotificationUri(mContext.getContentResolver(), uri);
 		
 		return cursor;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		LogUtil.d(TAG + " update ");
 		int count = 0;
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		
 		int match = URI_MATCHER.match(uri);
 		switch(match){
 		case MUSICINFO:
 		{
 			LogUtil.d(TAG + " update MUSICINFO ");
 			if(values.size() > 0){
 				try{
 					count = db.update(MusicDBHelper.TABLE_MUSICINFO, values, selection, selectionArgs);
 				}catch(Exception e){
 					e.printStackTrace();
 					count = 0;
 				}
 			}else {
 				count = 0;
 			}
 			
 			break ;
 		}
 		
 		case MUSICINFO_ITEM:
 		{
 			LogUtil.d(TAG + " update MUSICINFO_ITEM ");
 			String segmen = uri.getPathSegments().get(1);
 			long rowId = Long.parseLong(segmen);
 			String where = MusicDB.MusicInfoColumns._ID + "=" + rowId ;
 			
 			if(values.size() > 0){
 				count = db.update(MusicDBHelper.TABLE_MUSICINFO, values, where, null);
 			}else {
 				count = 0;
 			}
 			getContext().getContentResolver().notifyChange(uri, null);
 			break ;
 		}
 		
 		case DOWNLOADINFO :
 		{
 			LogUtil.d(TAG + " update DOWNLOADINFO ");
 			if(values.size() > 0){
 				try{
 					count = db.update(MusicDBHelper.TABLE_DOWNLOADINFO, values, selection, selectionArgs);
 				}catch(Exception e){
 					count = 0;
 					e.printStackTrace();
 				}
 			}else{
 				count = 0;
 			}
 			
 			break ;
 		}
 		
 		case DOWNLOADINFO_ITEM:
 		{
 			LogUtil.d(TAG + " DONWLOADINFO_ITEM ");
 			String segment = uri.getPathSegments().get(1);
 			long rowId = Long.parseLong(segment);
 			String where = MusicDB.DownloadInfoColumns._ID + "=" + rowId;
 			
 			if(values.size() > 0){
 				count = db.update(MusicDBHelper.TABLE_DOWNLOADINFO, values, where, null);
 			}else{
 				count = 0;
 			}
 			
 			break ;
 		}
 		
 		default :
 			throw new IllegalArgumentException("update uri error ! " + uri);
 		
 		}
 		return count;
 	}
 	
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		LogUtil.d(TAG + " delete ");
 		int count = 0;
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		int match = URI_MATCHER.match(uri);
 		try{
 			switch(match){
 			case MUSICINFO:
 			{
 				LogUtil.d(TAG + " delete MUSICINFO ");
 				count = db.delete(MusicDBHelper.TABLE_MUSICINFO, selection, selectionArgs);
 				getContext().getContentResolver().notifyChange(uri, null);
 				break ;
 			}
 			
 			case MUSICINFO_ITEM:
 			{
 				LogUtil.d(TAG + " delete MUSICINFO_ITEM ");
 				String segment = uri.getPathSegments().get(1);
 				long rowId = Long.parseLong(segment);
 				String where = MusicDB.MusicInfoColumns._ID + "=" + rowId;
 				
 				count = db.delete(MusicDBHelper.TABLE_MUSICINFO, where, null);
 				getContext().getContentResolver().notifyChange(uri, null);
 				break ;
 			}
 			
 			case DOWNLOADINFO:
 			{
 				LogUtil.d(TAG + " delete DOWNLOADINFO ");
 				count = db.delete(MusicDBHelper.TABLE_DOWNLOADINFO, selection, selectionArgs);
 				getContext().getContentResolver().notifyChange(uri, null);
 				break ;
 			}
 			
 			case DOWNLOADINFO_ITEM:
 			{
 				LogUtil.d(TAG + " delete DOWNLOADINFO_ITEM ");
 				String segment = uri.getPathSegments().get(1);
 				long rowId = Long.parseLong(segment);
 				String where = MusicDB.DownloadInfoColumns._ID + "=" + rowId;
 				
 				count = db.delete(MusicDBHelper.TABLE_DOWNLOADINFO, where, null);
 				getContext().getContentResolver().notifyChange(uri, null);
 				break ;
 			}
 			
 			default :
 				throw new IllegalArgumentException("delete uri error ! " + uri);
 			
 			}
 		}catch(Exception e){
 			count = 0;
 			e.printStackTrace();
 		}
 		return count;
 	}
 
 }
