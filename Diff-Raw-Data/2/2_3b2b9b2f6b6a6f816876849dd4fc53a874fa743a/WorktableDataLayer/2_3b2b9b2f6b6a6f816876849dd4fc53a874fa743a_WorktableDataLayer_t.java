 /*
  * Copyright () 2012 The Johns Hopkins University Applied Physics Laboratory.
  * All Rights Reserved.  
  */
 package org.rapidandroid.data.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.commons.lang.StringUtils;
 import org.rapidandroid.data.RapidSmsDBConstants;
 import org.rapidandroid.data.SmsDbHelper;
 import org.rapidsms.java.core.model.Field;
 import org.rapidsms.java.core.model.Form;
 import org.rapidsms.java.core.parser.IParseResult;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 /**
  * Database access layer for the sages_multisms_worktable
  * 
  * @author POKUAM1
  * @created Feb 8, 2012
  */
 public class WorktableDataLayer {
 
 	private static SmsDbHelper mDbHelper;
 	private static SQLiteDatabase mDb;
 	
 	public static SQLiteDatabase getDb(){
 		return mDb;
 	};
 	private static long timer_threshold = 300;
 	
 	public static void setTimerThreshold(long timer_threshold) {
 		WorktableDataLayer.timer_threshold = timer_threshold;
 	}
 
 	private static final String case_status = "status";
 	private static final String case_ttlTimer = "ttl_timer";
 	public static final String label_bad = "bad";
 	public static final String label_incomplete = "missing";
 	public static final String label_complete = "complete";
 	public static final String label_ttlStale = "staledata";
 	public static final String label_ttlLive = "livedata";
 	public static final String label_ttlFuture = "futuredata";
 	public static final String label_blob = "blob";
 	
 	private static final String total_segments = RapidSmsDBConstants.MultiSmsWorktable.TOTAL_SEGMENTS;
 	private static final String sages_multisms_worktable = RapidSmsDBConstants.MultiSmsWorktable.TABLE;
 	private static final String tx_id = RapidSmsDBConstants.MultiSmsWorktable.TX_ID;
 	private static final String tx_timestamp = RapidSmsDBConstants.MultiSmsWorktable.TX_TIMESTAMP;
 	private static final String payload = RapidSmsDBConstants.MultiSmsWorktable.PAYLOAD;
 	private static final String segment_number = RapidSmsDBConstants.MultiSmsWorktable.SEGMENT_NUMBER;
 	private static final String rapidandroid_message = RapidSmsDBConstants.Message.TABLE;
 	private static final String phone = RapidSmsDBConstants.Message.PHONE;
 	private static final String monitor_msg_id = RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID;
 	private static final String rapidandroid_monitor = RapidSmsDBConstants.Monitor.TABLE;
 	
 	private static  StringBuilder bldrCaseExprIsComplete = new StringBuilder();
 	static	{/* SELECT CASE 
 				WHEN SEGTOTAL = total_segments THEN 'complete' 
 			    WHEN SEGTOTAL < total_segments THEN 'missing' 
 			    ELSE 'bad-too many' END AS status, * FROM
 			    (SELECT count(*) AS SEGTOTAL, * FROM sages_multisms_worktable GROUP BY tx_id) */
 				
 			bldrCaseExprIsComplete.append("SELECT CASE WHEN SEGTOTAL = " +total_segments+ " THEN '"+label_complete+"' ");
 			bldrCaseExprIsComplete.append("WHEN SEGTOTAL < " +total_segments+ " THEN '"+label_incomplete+"' ");
 			bldrCaseExprIsComplete.append("ELSE '"+label_bad+"' END AS " +case_status+ ", * FROM ");
 			bldrCaseExprIsComplete.append("(SELECT COUNT(*) AS SEGTOTAL, * FROM " +sages_multisms_worktable+ " GROUP BY " +tx_id+ ")");
 		}
 		
 	private static StringBuilder bldrTTLToggle = new StringBuilder();
 	static {/* SELECT strftime('%s','now') - strftime('%s',tx_timestamp)
 				AS DIFF , * FROM sages_multisms_worktable WHERE tx_timestamp
 				IN (SELECT max(tx_timestamp) FROM sages_multisms_worktable
 				GROUP BY tx_id) AND DIFF < 300 GROUP BY tx_id */
 			bldrTTLToggle.append("SELECT strftime('%s','now') - strftime('%s'," +tx_timestamp+ ") ");
 			bldrTTLToggle.append("AS DIFF , * FROM " +sages_multisms_worktable+ " WHERE " +tx_timestamp);
 			bldrTTLToggle.append("IN (SELECT max(" +tx_timestamp+ ") FROM " +sages_multisms_worktable);
 			bldrTTLToggle.append("GROUP BY " +tx_id+ ") AND DIFF #?# " +timer_threshold+ "  GROUP BY " +tx_id);
 		}
 	
 	private static  StringBuilder bldrCaseExprTTLOfIncompletes = new StringBuilder();
 	static {/* SELECT CASE 
 			WHEN DIFF <= 300 THEN 'livedata'
 			WHEN DIFF > 300 THEN 'staledata'
 			END AS timer, * from
 			(select datetime('now','localtime'), strftime('%s','now','localtime') - strftime('%s',tx_timestamp) as DIFF , 
 			* from sages_multisms_worktable where tx_timestamp in 
 			(select max(tx_timestamp) from sages_multisms_worktable group by tx_id) group by tx_id) */
 		
 		bldrCaseExprTTLOfIncompletes.append("SELECT CASE ");
 		bldrCaseExprTTLOfIncompletes.append("WHEN DIFF <= " +timer_threshold+ " THEN '"+label_ttlLive+"' ");
 		bldrCaseExprTTLOfIncompletes.append("WHEN DIFF > " +timer_threshold+ " THEN '"+label_ttlStale+"' ");
 		bldrCaseExprTTLOfIncompletes.append("WHEN DIFF < 0 THEN '"+label_ttlFuture+"' ");
 		bldrCaseExprTTLOfIncompletes.append("END AS " +case_ttlTimer+ ", * FROM ");
 		bldrCaseExprTTLOfIncompletes.append("(SELECT datetime('now','localtime'), strftime('%s','now','localtime') - strftime('%s'," +tx_timestamp+ ") AS DIFF , "); 
 		bldrCaseExprTTLOfIncompletes.append("* FROM " +sages_multisms_worktable+ " WHERE " +tx_timestamp+ " IN "); 
 		bldrCaseExprTTLOfIncompletes.append("(SELECT max(" +tx_timestamp+ ") FROM " +sages_multisms_worktable+ " GROUP BY " +tx_id+ ") ");
 		bldrCaseExprTTLOfIncompletes.append("GROUP BY " +tx_id+ ")");
 		
 	}
 	
 	private static StringBuilder bldrConcatMessageSet = new StringBuilder();
 	static {/* SELECT tx_id, group_concat(payload, "") FROM 
 			(SELECT tx_id, payload FROM 
 			sages_multisms_worktable ORDER BY tx_id DESC, segment_number ASC) GROUP BY tx_id 
 			
 			* WHERE tx_id IN (?..) gets inserted dynamically see concatMessageForTxId()
 			*/
 		bldrConcatMessageSet.append("SELECT "+tx_id+", group_concat("+payload+", \"\") AS "+label_blob+" FROM ");
 		bldrConcatMessageSet.append("(SELECT "+tx_id+", "+payload+" FROM ");
 		bldrConcatMessageSet.append(sages_multisms_worktable+ " ORDER BY "+tx_id+" DESC, "+segment_number+" ASC) GROUP BY "+tx_id);
 		
 	}
 	
 	public static Cursor concatMessagesForTxIds(Context context, List<Long> txIds){
 		openDbInterfaces(context);
 		String[] txIdsAsArray = null;
 		StringBuilder query = new StringBuilder(bldrConcatMessageSet);
 		if (txIds != null && !txIds.isEmpty()){
 			String txIdString = StringUtils.join(txIds, ",");
 			txIdsAsArray = txIdString.split(",");
 			String paramStr = StringUtils.repeat("?", ",", txIds.size());
 			
 			int insertionIndex = query.indexOf("GROUP BY") - 1;
 			query.insert(insertionIndex, "WHERE "+tx_id+" IN (" +paramStr+ ")");
 		}
 		Log.d("WorktableDataLayer", "concat string: " + query.toString());
 
 		Cursor cursor = mDb.rawQuery(query.toString(), txIdsAsArray);
 		return cursor;
 	}
 	
 	public static Map<Long, String> getConcatenatedMessagesForTxIds(Context context, List<Long> txIds){
 		Cursor cursor = concatMessagesForTxIds(context, txIds);
 		Map<Long, String> concatMap = new HashMap<Long, String>();
 		
 		int idx_txId = cursor.getColumnIndex(tx_id);
 		int idx_blob = cursor.getColumnIndex(label_blob);
 		while (cursor.moveToNext()){
 			concatMap.put(cursor.getLong(idx_txId), cursor.getString(idx_blob));
 		}
 		cursor.close();
 		return concatMap;
 	}
 
 	
 	/**
 	 * TODO:
 	 * @param context
 	 * @return
 	 */
 	public static Cursor getCompleteVsIncompleteTxIds(Context context){
 		
 		openDbInterfaces(context);
 		
 		StringBuilder query = new StringBuilder(bldrCaseExprIsComplete);
 		//String str = "CASE complete, missing, bad";
 		Cursor cr = mDb.rawQuery(query.toString(), null);
 		return cr;
 		// cursor handler would separate complete from missing/bad:
 		// - completes get passed off for processing
 		// - missing get passed off to next query for staleness check
 		// - bad gets nacked and deleted
 	}
 
 	/**
 	 * @param context
 	 */
 	protected static void openDbInterfaces(Context context) {
 		if (mDbHelper == null){
 			resetDbInterfaces(context);
 		}
 		if (mDb==null || !mDb.isOpen()){
 			mDb = mDbHelper.getWritableDatabase(); //TODO good move?
 		}
 	}
 	/**
 	 * TODO:
 	 * @param context
 	 * @return
 	 */
 	public static Cursor getStaleVsLiveTxIds(Context context, List<Long> txIds){
 		
 		openDbInterfaces(context);
 		String[] txIdsAsArray = null;
 		StringBuilder query = new StringBuilder(bldrCaseExprTTLOfIncompletes);
 		if (txIds != null && !txIds.isEmpty()){
 			String txIdString = StringUtils.join(txIds, ",");
 			String paramStr = StringUtils.repeat("?", ",", txIds.size());
 			query.append(" WHERE " +tx_id+ " IN (" + paramStr + ")");
 			txIdsAsArray = txIdString.split(",");
 		}
 		Log.d("WDLayer.getStaleVsLiveIncompleteTxIds()", query.toString());
 		Cursor cr = mDb.rawQuery(query.toString(), txIdsAsArray);
 		return cr;
 		// cursor handler would separate stales from live
 		// stales get nacked and deleted
 		// lives just sit
 	}
 
 	
 	public static Map<String, List<Long>> categorizeCompleteVsIncomplete(Context context) throws Exception {
 		Cursor cursor = getCompleteVsIncompleteTxIds(context);
 		
 		int col_tx_id = cursor.getColumnIndex(tx_id);
 		int col_status = cursor.getColumnIndex(case_status);
 		List<Long> completeTxIds = new ArrayList<Long>();
 		List<Long> incompleteTxIds = new ArrayList<Long>();
 		List<Long> badTxIds = new ArrayList<Long>();
 		
 		while (cursor.moveToNext()){
 			long txId = cursor.getLong(col_tx_id);
 			String status = cursor.getString(col_status);
 			
 			if (label_complete.equals(status)){
 				completeTxIds.add(txId);
 			} else if (label_incomplete.equals(status)) {
 				incompleteTxIds.add(txId);
 			} else if (label_bad.equals(status)) {
 				badTxIds.add(txId);
 			} else {
 				throw new Exception();
 			}
 		}
 		
 		if (cursor != null)cursor.close();
 		
 		Map<String, List<Long>> statusMap = new HashMap<String, List<Long>>();
 		statusMap.put(label_complete, completeTxIds);
 		statusMap.put(label_incomplete, incompleteTxIds);
 		statusMap.put(label_bad, badTxIds);
 		return statusMap;
 	}
 	
 	/**
 	 * 
 	 * @param context
 	 * @param txIds optional txIds of the Incomplete messages -- used in optional where clause
 	 * @return
 	 * @throws Exception
 	 */
 	public static Map<String, List<Long>> categorizeStaleVsLive(Context context, List<Long> txIds) throws Exception {
 		Cursor cursor = getStaleVsLiveTxIds(context, txIds);
 		
 		int col_tx_id = cursor.getColumnIndex(tx_id);
 		int col_timer = cursor.getColumnIndex(case_ttlTimer);
 		List<Long> liveTxIds = new ArrayList<Long>();
 		List<Long> staleTxIds = new ArrayList<Long>();
 		List<Long> futureTxIds = new ArrayList<Long>();
 		
 		while (cursor.moveToNext()){
 			long txId = cursor.getLong(col_tx_id);
 			String ttlTimer = cursor.getString(col_timer);
 			@SuppressWarnings("unused")
 			long diff = cursor.getLong(2);
 			@SuppressWarnings("unused")
 			String txtimestamp = cursor.getString(6);
 			@SuppressWarnings("unused")
 			String dateNow = cursor.getString(1);
 			@SuppressWarnings("unused")
 			String ttltimer = cursor.getString(0);
 			
 			// {ttl_timer=0, datetime('now','localtime')=1, _id=3, segment_number=4, tx_id=6, payload=8, tx_timestamp=7, DIFF=2, total_segments=5}
 			System.out.println(cursor.getString(0)); //ttl_timer
 			System.out.println(cursor.getString(1)); //now local
 			System.out.println(cursor.getString(2)); //DIFF
 			System.out.println(cursor.getString(3)); //_id
 			System.out.println(cursor.getString(4)); //seg num
 			System.out.println(cursor.getString(5)); //tot segs
 			System.out.println(cursor.getString(6)); //tx_id
 			System.out.println(cursor.getString(7)); //tx_timestamp
 			System.out.println(cursor.getString(8)); //payload
 			if (label_ttlLive.equals(ttlTimer)){
 				liveTxIds.add(txId);
 			} else if (label_ttlStale.equals(ttlTimer)) {
 				staleTxIds.add(txId);
 			} else if (label_ttlFuture.equals(ttlTimer)) {
 				futureTxIds.add(txId);
 			} else {
 				throw new Exception();
 			}
 		}
 		
 		if (cursor != null)cursor.close();
 		Map<String, List<Long>> ttlMap = new HashMap<String, List<Long>>();
 		ttlMap.put(label_ttlStale, staleTxIds);
 		ttlMap.put(label_ttlLive, liveTxIds);
 		return ttlMap;
 	}
 
 	public static Map<Long, String> buildSenderPhonesLookupForTxIds(Context context, List<Long> txIds){
 		Map<Long, String> phonelookup = new HashMap<Long, String>();
 		Cursor c = getSenderPhonesForTxIds(context, txIds);
 		int col_tx_id = c.getColumnIndex(tx_id);
 		int col_phone = c.getColumnIndex(phone);
 		while (c.moveToNext()){
 			phonelookup.put(c.getLong(col_tx_id), c.getString(col_phone));
 			Log.d("WorktableDataLayer:buildsenderphones", "tx_id= " + c.getLong(col_tx_id)+"  , phone= "+ c.getString(col_phone));
 		}
 		if (c != null)c.close();
 		return phonelookup;
 		
 	}
 	/**
 	 * @param list
 	 */
 	public static void deleteTxIds(Context context, List<Long> txIds) {
 		if (txIds == null || txIds.isEmpty()) return;
 		openDbInterfaces(context);
 //		mDb = mDbHelper.getWritableDatabase();
 		
 		StringBuilder query = new StringBuilder();
 		String txIdString = StringUtils.join(txIds, ",");
 		String paramStr = StringUtils.repeat("?", ",", txIds.size());
 		String str = "DELETE FROM " +sages_multisms_worktable+ " WHERE " +tx_id+ " IN (" + paramStr + ")";
 		
 		Log.d("WorktableDataLayer", "Delete string: " + str);
 		query.append(str);
 		String[] txIdsAsArray = txIdString.split(",");
 		@SuppressWarnings("unused")
 		int val = mDb.delete(sages_multisms_worktable, tx_id+ " IN (" + paramStr + ")", txIdsAsArray);
 	}
 	
 	public static Cursor getAvailableMessagesForTxId(Context context, List<Long> txIds){
 		openDbInterfaces(context);
 //		mDb = mDbHelper.getReadableDatabase();
 		
 		StringBuilder query = new StringBuilder();
 		String txIdString = StringUtils.join(txIds, ",");
 		String paramStr = StringUtils.repeat("?", ",", txIds.size());
 		String str = "SELECT count(*) AS COUNT FROM " +sages_multisms_worktable+ " WHERE " +tx_id+ " IN (" + paramStr + ")";
 		Log.d("WorktableDataLayer", "Lookup messages for txIds string: " + str);
 		query.append(str);
 		String[] txIdsAsArray = txIdString.split(",");
 		Cursor cursor = mDb.rawQuery(query.toString(), txIdsAsArray);
 		return cursor;
 	}
 	public static Cursor getSenderPhonesForTxIds(Context context, List<Long> txIds){
 		openDbInterfaces(context);
 		//txIds = (txIds == null) ? new ArrayList<Long>(): txIds; 
 		StringBuilder query = new StringBuilder();
 //		String str = "SELECT m._id, "+tx_id+", "+phone+" FROM "+rapidandroid_message+" m " +
 //		"INNER JOIN " +sages_multisms_worktable+ " w ON m._id = w._id";
 		String str = "SELECT w._id, "+tx_id+", "+monitor_msg_id+", mo.phone as "+phone+", mo._id from "+sages_multisms_worktable+" w" + 
 				" INNER JOIN "+rapidandroid_message+" m ON m._id = w.monitor_msg_id " + 
 				" INNER JOIN "+rapidandroid_monitor+" mo ON m.monitor_id = mo._id GROUP BY "+tx_id;
 		String[] txIdsAsArray = null;
 		if (txIds != null) {
 			String txIdString = StringUtils.join(txIds, ",");
 			String paramStr = StringUtils.repeat("?", ",", txIds.size());
 			str += " WHERE " +tx_id+ " IN (" + paramStr + ")";
 			txIdsAsArray = txIdString.split(",");
 		}
 		Log.d("WorktableDataLayer", "Get sender phones for all txIds");
 		query.append(str);
 		Cursor cursor = mDb.rawQuery(query.toString(), txIdsAsArray);
 		return cursor;
 	}
 	
 	/**
 	 * Called after a message is parsed. For a given form, the results are put
 	 * into a ParseResult for each field, typed out according to the fieldtype
 	 * 
 	 * @param context
 	 * @param f
 	 * @param message_id
 	 * @param results
 	 * @return
 	 */
 	public static long InsertFormData(Context context, Form f, int message_id, Vector<IParseResult> results) {
 		openDbInterfaces(context);
 	
 		ContentValues cv = new ContentValues();
 		cv.put(RapidSmsDBConstants.FormData.MESSAGE, message_id);
 		Field[] fields = f.getFields();
 		int len = fields.length;
 //		Random r = new Random();
 
 		for (int i = 0; i < len; i++) {
 			Field field = fields[i];
 			IParseResult res = results.get(i);
 			if (res != null) {
 				cv.put(RapidSmsDBConstants.FormData.COLUMN_PREFIX + field.getName(), res.getValue().toString());
 			} else {
 				cv.put(RapidSmsDBConstants.FormData.COLUMN_PREFIX + field.getName(), "");
 			}
 		}
 		/*Uri inserted = context.getContentResolver().insert(
 															Uri.parse(RapidSmsDBConstants.FormData.CONTENT_URI_PREFIX
 																	+ f.getFormId()), cv);*/
		return mDb.insert(RapidSmsDBConstants.FormData.TABLE_PREFIX + f.getPrefix(), null, cv);
 		//return true;
 	}
 
 	public static void beginTransaction(Context context){
 		if (mDb == null) openDbInterfaces(context);
 		mDb.beginTransaction();
 	}
 	
 	public static void setTransactionSuccessful(){
 		mDb.setTransactionSuccessful();
 	}
 	public static void endTransaction(){
 		mDb.endTransaction();
 	}
 	
 	public static void shutdownDbInterfaces(){
 		if (mDb != null){
 			if (mDb.isOpen()) {
 				mDb.close();
 			}
 			mDb = null;
 		}
 		
 		if (mDbHelper != null){
 			mDbHelper.close();
 			mDbHelper = null;
 		}
 	}
 
 	/**
 	 * @param context
 	 */
 	protected static void resetDbInterfaces(Context context) {
 		if (mDb != null){
 			if (mDb.isOpen()) {
 				mDb.close();
 			}
 			mDb = null;
 		}
 		
 		if (mDbHelper != null){
 			mDbHelper.close();
 			mDbHelper = null;
 		}
 		
 		mDbHelper = new SmsDbHelper(context);
 	}
 }
