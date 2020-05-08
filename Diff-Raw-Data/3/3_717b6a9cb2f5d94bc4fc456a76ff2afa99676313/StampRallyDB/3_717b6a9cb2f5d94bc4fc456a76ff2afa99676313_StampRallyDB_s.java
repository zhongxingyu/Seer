 package jag.kumamoto.apps.StampRally;
 
 
 import jag.kumamoto.apps.StampRally.Data.Item;
 import jag.kumamoto.apps.StampRally.Data.Prize;
 import jag.kumamoto.apps.StampRally.Data.StampPin;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 
 /**
  * 
  * スタンプラリーアプリケーションで使用するDBを操作するためのクラス
  * 
  * @author aharisu
  *
  */
 public class StampRallyDB extends SQLiteOpenHelper{
 	
 	private static final String DataBaseName = "StampRally";
 	private static final int Version = 1;
 	
 	
 	/*
 	 * StampLocationクラスの永続化のためのフィールド
 	 */
 	private static final String StampLocationTable = "stamp_location";
 	private static final String StampLocationID = "id";
 	private static final String StampLocationLatitude = "latitude";
 	private static final String StampLocationLongitude = "longitude";
 	private static final String StampLocationName = "name";
 	private static final String StampLocationIsArrived = "is_arrived";
 	private static final String StampLocationPoint = "point";
 	private static final String StampLocationPrefecturesCode = "prefectures";
 	private static final String StampLocationAreaCode = "area_code";
 	private static final String StampLocationType = "type";
 	private static final String StampLocationURL = "url";
 
 	
 	/*
 	 * Prizeクラスの永続化のためのフィールド
 	 */
 	private static final String PrizeTable ="prize";
 	private static final String PrizeID = "id";
 	private static final String PrizeTime = "time";
 	private static final String PrizeTitle = "title";
 	private static final String PrizeMessage = "message";
 	private static final String PrizeItemId = "item_id";
 	
 	/*
 	 * Itemクラスの永続化のためのフィールド
 	 */
 	private static final String ItemTable = "item";
 	private static final String ItemID = "id";
 	private static final String ItemName = "name";
 	private static final String ItemImageURL = "image_url";
 	private static final String ItemDescription = "description";
 	
 	/*
 	 * Singletonインスタンス
 	 */
 	private static StampRallyDB _instance = null;
 	
 	public static void createInstance(Context context) {
 		_instance = new StampRallyDB(context);
 	}
 	
 	private static StampRallyDB getInstance() {
 		if(_instance == null) {
 			throw new RuntimeException("do not create instance yet");
 		}
 		
 		return _instance;
 	}
 	
 	private StampRallyDB(Context context) {
 		super(context, DataBaseName, null, Version);
 	}
 	
 	
 	@Override public void onCreate(SQLiteDatabase db) {
 		db.beginTransaction();
 		try {
 			
 			//StampLocationテーブル作成
 			db.execSQL(new StringBuilder()
 				.append("create table ").append(StampLocationTable).append("(")
 				.append(StampLocationID).append(" integer primary key")
 				.append(",").append(StampLocationLatitude).append(" integer not null")
 				.append(",").append(StampLocationLongitude).append(" integer not null")
 				.append(",").append(StampLocationName).append(" text not null")
 				.append(",").append(StampLocationIsArrived).append(" integer not null")
 				.append(",").append(StampLocationPoint).append(" integer not null")
 				.append(",").append(StampLocationPrefecturesCode).append(" integer not null")
 				.append(",").append(StampLocationAreaCode).append(" integer not null")
 				.append(",").append(StampLocationType).append(" integer not null")
 				.append(",").append(StampLocationURL).append(" text not null")
 				.append(")")
 				.toString());
 			
 			//Prizeテーブル作成
 			db.execSQL(new StringBuilder()
 				.append("create table ").append(PrizeTable).append("(")
 				.append(PrizeID).append(" integer primary key")
 				.append(",").append(PrizeTime).append(" integer not null")
 				.append(",").append(PrizeTitle).append(" text not null")
 				.append(",").append(PrizeMessage).append(" text not null")
 				.append(",").append(PrizeItemId).append(" integer")
 				.append(")")
 				.toString());
 			
 			//Itemテーブル作成
 			db.execSQL(new StringBuilder()
 				.append("create table ").append(ItemTable).append("(")
 				.append(ItemID).append(" integer primary key")
 				.append(",").append(ItemName).append(" text not null")
 				.append(",").append(ItemImageURL).append(" text not null")
 				.append(",").append(ItemDescription).append(" text not null")
 				.append(")")
 				.toString());
 			
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 	}
 	
 	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	}
 	
 	
 	public static StampPin[] getStampPins() {
 		SQLiteOpenHelper helper = getInstance();
 		synchronized (helper) {
 			
 			SQLiteDatabase db = helper.getReadableDatabase();
 			
 			Cursor cursor = null;
 			try {
 				cursor = db.query(StampLocationTable, 
 						new String[] {
 							StampLocationID,
 							StampLocationLatitude,
 							StampLocationLongitude,
 							StampLocationName,
 							StampLocationIsArrived,
 							StampLocationPoint,
 							StampLocationPrefecturesCode,
 							StampLocationAreaCode,
 							StampLocationType,
 							StampLocationURL,
 					}, null, null, null, null, null);
 				
 				return createStampPinsFromCursor(cursor);
 			} finally {
 				if(cursor != null)
 					cursor.close();
 				
 				db.close();
 			}
 			
 		}
 	}
 	
 	public static StampPin[] getStampPinsNonArrive() {
 		SQLiteOpenHelper helper = getInstance();
 		synchronized (helper) {
 			
 			SQLiteDatabase db = helper.getReadableDatabase();
 			
 			Cursor cursor = null;
 			try {
 				cursor = db.query(StampLocationTable, 
 						new String[] {
 							StampLocationID,
 							StampLocationLatitude,
 							StampLocationLongitude,
 							StampLocationName,
 							StampLocationIsArrived,
 							StampLocationPoint,
 							StampLocationPrefecturesCode,
 							StampLocationAreaCode,
 							StampLocationType,
 							StampLocationURL,
 					}, 
 					new StringBuilder(StampLocationIsArrived).append(" = 0").toString(),
 					null, null, null, null);
 				
 				return createStampPinsFromCursor(cursor);
 			} finally {
 				if(cursor != null)
 					cursor.close();
 				
 				db.close();
 			}
 		}
 	}
 	
 	private static StampPin[] createStampPinsFromCursor(Cursor cursor) {
 		int count = cursor.getCount();
 		StampPin[] stamps = new StampPin[count];
 		if(count != 0) {
 			int idIndex = cursor.getColumnIndex(StampLocationID);
 			int latitudeIndex = cursor.getColumnIndex(StampLocationLatitude);
 			int longitudeIndex = cursor.getColumnIndex(StampLocationLongitude);
 			int nameIndex = cursor.getColumnIndex(StampLocationName);
 			int isArrivedIndex = cursor.getColumnIndex(StampLocationIsArrived);
 			int pointIndex = cursor.getColumnIndex(StampLocationPoint);
 			int prefecturesIndex = cursor.getColumnIndex(StampLocationPrefecturesCode);
 			int areaIndex = cursor.getColumnIndex(StampLocationAreaCode);
 			int typeIndex = cursor.getColumnIndex(StampLocationType);
 			int urlIndex = cursor.getColumnIndex(StampLocationURL);
 			
 			cursor.moveToFirst();
 			for(int i = 0;i < count;++i) {
 				stamps[i] = new StampPin(
 						cursor.getLong(idIndex),
 						cursor.getInt(latitudeIndex),
 						cursor.getInt(longitudeIndex),
 						cursor.getString(nameIndex),
 						cursor.getInt(pointIndex),
 						cursor.getInt(prefecturesIndex),
 						cursor.getInt(areaIndex),
 						cursor.getInt(typeIndex),
 						cursor.getString(urlIndex),
 						cursor.getInt(isArrivedIndex) != 0);
 				
 				cursor.moveToNext();
 			}
 		}
 				
 		return stamps;
 	}
 	
 	public static void insertStampPins(StampPin... pins) {
 		if(pins == null || pins.length == 0)
 			return;
 		
 		SQLiteOpenHelper helper = getInstance();
 		synchronized (helper) {
 			
 			SQLiteDatabase db = helper.getWritableDatabase();
 			db.beginTransaction();
 			
 			try {
 				ContentValues values = new ContentValues();
 				for(StampPin pin : pins) {
 					values.clear();
 					
 					values.put(StampLocationID, pin.id);
 					values.put(StampLocationLatitude, pin.latitude);
 					values.put(StampLocationLongitude, pin.longitude);
 					values.put(StampLocationName, pin.name);
 					values.put(StampLocationIsArrived, pin.isArrive);
 					values.put(StampLocationPoint, pin.point);
 					values.put(StampLocationPrefecturesCode, pin.prefCode);
 					values.put(StampLocationAreaCode, pin.areaCode);
 					values.put(StampLocationType, pin.type);
 					values.put(StampLocationURL, pin.url);
 					
 					db.insert(StampLocationTable, null, values);
 				}
 				
 				db.setTransactionSuccessful();
 			} finally {
 				db.endTransaction();
 				db.close();
 			}
 			
 		}
 	}
 	
 	public static void deleteStampPins(StampPin... pins) {
 		if(pins == null || pins.length == 0)
 			return;
 		
 		SQLiteOpenHelper helper = getInstance();
 		synchronized (helper) {
 			
 			SQLiteDatabase db = helper.getWritableDatabase();
 			
 			db.beginTransaction();
 			try {
 				StringBuilder builder = new StringBuilder()
 					.append("delete from ")
 					.append(StampLocationTable)
 					.append(" where ")
 					.append(StampLocationID)
 					.append(" in (")
 					.append(pins[0].id);
 				for(int i = 1;i< pins.length;++i) {
 					builder.append(", ").append(pins[i].id);
 				}
 				builder.append(")");
 				
 				db.execSQL(builder.toString());
 				
 				db.setTransactionSuccessful();
 			} finally {
 				db.endTransaction();
 				db.close();
 			}
 		}
 	}
 	
 	public static boolean[] checkPinArrived(long... ids) {
 		if(ids == null || ids.length == 0)
 			return new boolean[0];
 		
 		boolean[] nonArrived = new boolean[ids.length];
 		SQLiteOpenHelper helper = getInstance();
 		synchronized (helper) {
 			
 			SQLiteDatabase db = helper.getWritableDatabase();
 			
 			Cursor cursor = null;
 			db.beginTransaction();
 			try {
 				//まだ到達していない場所のidを取得
 				StringBuilder builder = new StringBuilder()
 					.append("select ")
 					.append(StampLocationID)
 					.append(" from ")
 					.append(StampLocationTable)
 					.append(" where ")
 					.append(StampLocationIsArrived).append(" = 0")
 					.append(" and ")
 					.append(StampLocationID)
 					.append(" in (")
 					.append(ids[0]);
 				for(int i = 1;i< ids.length;++i) {
 					builder.append(", ").append(ids[i]);
 				}
 				builder.append(")");
 				
 				cursor = db.rawQuery(builder.toString(), null);
 				int count = cursor.getCount();
 				if(count != 0) {
 					long[] nonArriveIds = new long[count];
 					int idIndex = cursor.getColumnIndex(StampLocationID);
 					
 					cursor.moveToFirst();
 					for(int i = 0;i < count;++i) {
 						nonArriveIds[i] = cursor.getLong(idIndex);
 						
 						cursor.moveToNext();
 					}
 					
 					//引数のid配列に対応してまだ到達していない場所であればtrueをセット
 					for(int i = 0;i < ids.length;++i) {
 						for(int j = 0;j < nonArriveIds.length;++j) {
 							if(ids[i] == nonArriveIds[j]) {
 								nonArrived[i] = true;
 								break;
 							}
 						}
 					}
 					
 					//到達していない場所の到達フラグを立てる
 					builder.delete(0, builder.length());
 					builder.append(StampLocationID).append(" in (").append(nonArriveIds[0]);
 					for(int i = 1;i < nonArriveIds.length;++i) {
 						builder.append(",").append(nonArriveIds[i]);
 					}
 					builder.append(")");
 					
 					ContentValues values = new ContentValues();
 					values.put(StampLocationIsArrived, 1);
 					db.update(StampLocationTable, values, builder.toString(), null);
 				}
 				
 				db.setTransactionSuccessful();
 			} finally {
 				db.endTransaction();
 				
 				if(cursor != null)
 					cursor.close();
 				
 				db.close();
 			}
 		}
 		
 		return nonArrived;
 	}
 	
 	public static void clearPinArrive() {
 		SQLiteOpenHelper helper = getInstance();
 		synchronized (helper) {
 			
 			SQLiteDatabase db = helper.getWritableDatabase();
 			
 			db.beginTransaction();
 			try {
 				ContentValues values = new ContentValues();
 				values.put(StampLocationIsArrived, 0);
 				
 				db.update(StampLocationTable, values, null, null);
 				
 				db.setTransactionSuccessful();
 			} finally {
 				db.endTransaction();
 				db.close();
 			}
 		}
 	}
 	
 	public static Prize[] getPrizes() {
 		SQLiteOpenHelper helper = getInstance();
 		synchronized (helper) {
 			
 			SQLiteDatabase db = helper.getReadableDatabase();
 			
 			Cursor cursor = null;
 			try {
 				cursor = db.query(PrizeTable, new String[] {
 						PrizeID,
 						PrizeTime,
 						PrizeTitle,
 						PrizeMessage,
 						PrizeItemId,
 				}, null, null, null, null, null);
 				
 				int count = cursor.getCount();
 				Prize[] prizes = new Prize[count];
 				if(count != 0) {
 					int idIndex = cursor.getColumnIndex(PrizeID);
 					int timeIndex = cursor.getColumnIndex(PrizeTime);
 					int titleIndex = cursor.getColumnIndex(PrizeTitle);
 					int messageIndex = cursor.getColumnIndex(PrizeMessage);
 					int itemIDIndex = cursor.getColumnIndex(PrizeItemId);
 					
 					cursor.moveToFirst();
 					for(int i = 0;i < count;++i) {
 						prizes[i] = new Prize(
 								cursor.getLong(idIndex),
 								cursor.getLong(timeIndex),
 								cursor.getString(titleIndex),
 								cursor.getString(messageIndex),
 								cursor.isNull(itemIDIndex) ? 
 										null :
 										getItem(db, cursor.getLong(itemIDIndex)));
 						
 						cursor.moveToNext();
 					}
 				}
 		
 				return prizes;
 			} finally {
 				if(cursor != null)
 					cursor.close();
 				
 				db.close();
 			}
 		}
 	}
 	
 	private static Item getItem(SQLiteDatabase db, long id) {
 		
 		Cursor cursor = null;
 		try {
 			cursor = db.query(ItemTable, new String[] {
 					ItemID,
 					ItemImageURL,
 					ItemName,
 					ItemDescription,
 			}, 
 			new StringBuilder(ItemID).append(" = ").append(id).toString(),
 			null, null, null, null);
 			
 			if(cursor.getCount() == 0) {
 				return null;
 			} else {
 				int idIndex = cursor.getColumnIndex(ItemID);
 				int imageURLIndex = cursor.getColumnIndex(ItemImageURL);
 				int nameIndex = cursor.getColumnIndex(ItemName);
 				int descriptionIndex = cursor.getColumnIndex(ItemDescription);
 				
 				cursor.moveToFirst();
 				return new Item(
 						cursor.getLong(idIndex),
						cursor.getString(imageURLIndex),
 						cursor.getString(nameIndex),
 						cursor.getString(descriptionIndex));
 			}
 		} finally {
 			if(cursor != null)
 				cursor.close();
 		}
 	}
 	
 	public static void insertPrizes(Prize... prizes) {
 		if(prizes == null || prizes.length == 0)
 			return;
 		
 		SQLiteOpenHelper helper = getInstance();
 		synchronized (helper) {
 			
 			SQLiteDatabase db = helper.getWritableDatabase();
 			db.beginTransaction();
 			
 			try {
 				ContentValues values = new ContentValues();
 				for(Prize prize : prizes) {
 					values.clear();
 					values.put(PrizeID, prize.id);
 					values.put(PrizeTime, prize.getTimeMilliseconds());
 					values.put(PrizeTitle, prize.title);
 					values.put(PrizeMessage, prize.message);
 					if(prize.item != null) {
 						values.put(PrizeItemId, prize.item.id);
 					}
 					db.insert(PrizeTable, null, values);
 					
 					if(prize.item != null) {
 						values.clear();
 						values.put(ItemID, prize.item.id);
 						values.put(ItemName, prize.item.name);
 						values.put(ItemImageURL, prize.item.imageUrl);
 						values.put(ItemDescription, prize.item.description);
 						
 						db.insert(ItemTable, null, values);
 					}
 				}
 		
 				db.setTransactionSuccessful();
 			} finally {
 				db.endTransaction();
 				db.close();
 			}
 		}
 	}
 	
 	
 	
 }
