 package com.bryanmarty.greenbutton.database;
 
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import com.bryanmarty.greenbutton.data.IntervalReading;
 import com.bryanmarty.greenbutton.database.DatabaseManager;
 import com.bryanmarty.greenbutton.database.TrackRequest;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils.InsertHelper;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.util.Log;
 
 public class TrackManager {
 	
 	private static boolean initialized = false;
 	private static DatabaseManager dbManager;
 	private static Context context_;
 	private static ThreadPoolExecutor threadPool_;
 	private static BlockingQueue<Runnable> queue_;
 	
 	public synchronized static boolean initialize(Context context) {
 		if(initialized) {
 			return initialized;
 		}
 		context_ = context;
 		try {
 			queue_ = new LinkedBlockingQueue<Runnable>();
 			threadPool_ = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, queue_);
 			dbManager = new DatabaseManager(context_);
 			dbManager.createDataBase();
 			dbManager.openDataBase();
 			initialized = true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			initialized = false;
 		}
 		return initialized;
 	}
 	
 	public synchronized static void shutdown() {
 		Log.i("Thread Pool","Shutting down Thread Pool");
 		if(!initialized) {
 			return;
 		}
 		threadPool_.shutdown();
 		try {
 			threadPool_.awaitTermination(10L, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		dbManager.close();
 		initialized = false;
 	}
 	
 	public static Future<Boolean> addReadings(final LinkedList<IntervalReading> readings) {
 		TrackRequest<Boolean> request = new TrackRequest<Boolean>() {
 			
 			@Override
 			public Boolean call() throws Exception {
 				boolean success = false;
 				SQLiteDatabase db = getDatabase();
 				
 				if(db == null) {
 					throw new SQLiteException("Database was null");
 				}
 				
 				InsertHelper ihelp = new InsertHelper(db,"gbdata");
 				
 				try {
 					db.beginTransaction();
 				
 					for (IntervalReading reading : readings) {
 						ihelp.prepareForInsert();
 						ihelp.bind(1, reading.getStartTime().getTime());
 						ihelp.bind(2, reading.getDuration());
 						ihelp.bind(3,reading.getValue());
 						ihelp.bind(4,reading.getCost());
 						ihelp.execute();
 					}
 					db.setTransactionSuccessful();
 					success = true;
 				} finally {
 					db.endTransaction();
 					ihelp.close();
 				}
 				return success;
 			}
 		};
 		request.setDatabase(dbManager.getDatabase());
 		return threadPool_.submit(request);
 	}
 	
 	public static Future<LinkedList<IntervalReading>> getReadingsSince(final Date beginDate) {
 		TrackRequest<LinkedList<IntervalReading>> request = new TrackRequest<LinkedList<IntervalReading>>() {
 			
 			@Override
 			public LinkedList<IntervalReading> call() throws Exception {
 				LinkedList<IntervalReading> readings = new LinkedList<IntervalReading>();
 				
 				SQLiteDatabase db = getDatabase();
 				
 				if(db == null) {
 					throw new SQLiteException("Database was null");
 				}
 				Cursor c = null;
 				try {
 					c = db.query("gbdata", new String[]{"start", "duration", "value", "cost"}, "start > ?", new String[]{String.valueOf(beginDate.getTime()/1000L)}, null, null, null);
 					//c = db.query("gbdata", new String[]{"start", "duration", "value", "cost"}, null, null, null, null, null);
 
 					if (c.moveToFirst()) {
 						do {
 							IntervalReading r = new IntervalReading();
 							r.setStartTime(new Date(c.getLong(0)));
 							r.setDuration(c.getInt(1));
 							r.setValue(c.getInt(2));
 							r.setCost(c.getInt(3));
 							readings.add(r);
 						} while (c.moveToNext());
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				} finally {
 					if(c != null) {
 						c.close();
 					}
 				}
 				return readings;
 			}
 		};
 		request.setDatabase(dbManager.getDatabase());
 		return threadPool_.submit(request);
 	}
 	
 	public static Future<LinkedList<IntervalReading>> getReadingsBetween(final Date beginDate, final Date endDate) {
 		TrackRequest<LinkedList<IntervalReading>> request = new TrackRequest<LinkedList<IntervalReading>>() {
 			
 			@Override
 			public LinkedList<IntervalReading> call() throws Exception {
 				LinkedList<IntervalReading> readings = new LinkedList<IntervalReading>();
 				
 				SQLiteDatabase db = getDatabase();
 				
 				if(db == null) {
 					throw new SQLiteException("Database was null");
 				}
 				Cursor c = null;
 				try {
					c = db.query("gbdata", new String[]{"start", "duration", "value", "cost"}, "start > ? AND start < ?", new String[]{String.valueOf(beginDate.getTime()),String.valueOf(endDate.getTime())}, null, null, null);
 					
 					if (c.moveToFirst()) {
 						do {
 							IntervalReading r = new IntervalReading();
 							r.setStartTime(new Date(c.getLong(0)));
 							r.setDuration(c.getInt(1));
 							r.setValue(c.getInt(2));
 							r.setCost(c.getInt(3));
 							readings.add(r);
 						} while (c.moveToNext());
 					}
 				} catch (Exception e) {
 					
 				} finally {
 					if(c != null) {
 						c.close();
 					}
 				}
 				return readings;
 			}
 		};
 		request.setDatabase(dbManager.getDatabase());
 		return threadPool_.submit(request);
 	}
 	
 	public static Future<Date> getLastDate() {
 		TrackRequest<Date> request = new TrackRequest<Date>() {
 			
 			@Override
 			public Date call() throws Exception {
 				Date result = new Date(0);
 				
 				SQLiteDatabase db = getDatabase();
 				
 				if(db == null) {
 					throw new SQLiteException("Database was null");
 				}
 				Cursor c = null;
 				try {
 					c = db.query("gbdata", new String[] {"start"}, null, null, null, null, "start DESC","1");
 					if (c.moveToFirst()) {
 							result = new Date(c.getLong(0));
 					}
 				} catch (Exception e) {
 					
 				} finally {
 					if(c != null) {
 						c.close();
 					}
 				}
 				return result;
 			}
 		};
 		request.setDatabase(dbManager.getDatabase());
 		return threadPool_.submit(request);
 	}
 	
 }
