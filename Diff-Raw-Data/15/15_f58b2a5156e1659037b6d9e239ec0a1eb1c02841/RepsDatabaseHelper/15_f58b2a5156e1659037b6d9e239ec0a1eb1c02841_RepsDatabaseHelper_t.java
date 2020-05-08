 /* DbHelper is part of a CodeShane™ solution.
  * Copyright © 2013 Shane Ian Robinson. All Rights Reserved.
  * See LICENSE file or visit codeshane.com for more information. */
 
 package com.codeshane.representing.providers;
 
 import static com.codeshane.util.DbUtils.execSqlAsTransaction;
 import static com.codeshane.util.Tables.createTableStatement;
 import static com.codeshane.util.Tables.dropTableStatement;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteTransactionListener;
 import android.util.Log;
 
 import com.codeshane.representing.meta.Table;
import static com.codeshane.representing.providers.RepsContract.Tables;
 
 /**
  * @author  Shane Ian Robinson <shane@codeshane.com>
  * @since   Aug 22, 2013
  * @version 1
  */
 public class RepsDatabaseHelper extends SQLiteOpenHelper {
 	public static final String	TAG	= RepsDatabaseHelper.class.getPackage().getName() + "." + RepsDatabaseHelper.class.getSimpleName();
 
 	public static final String DATABASE_NAME = RepsContract.DATABASE_NAME;
 
 	public static final int DATABASE_VERSION = 6;
 
 	RepsDatabaseHelper ( Context context ) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
     /** Called only once in application life to create and populate the initial database. */
 	@Override public void onCreate ( SQLiteDatabase db ) {
 		StringBuilder sb = new StringBuilder();
		for (Table table : Tables.values()) {
 			// Give each table the option to create itself.
 //			if(!table.onCreation(db)){
 				// create automatically
 				sb = createTableStatement(table, sb);
 //			}
 		}
 		Log.v(TAG,"Create Transaction: "+sb.toString());
 		if (sb.length() > 15) {
 			execSqlAsTransaction(sb.toString(), db);
 		} else {
 			Log.e(TAG,"Invalid Transaction.");
 		}
 	}
 
 	public static final SQLiteTransactionListener transactionListener = new SQLiteTransactionListener() {
 		@Override public void onBegin () { Log.w(TAG,"Begin Sqlite transaction."); }
 		@Override public void onCommit () { Log.w(TAG,"Successful Sqlite transaction."); }
 		@Override public void onRollback () { Log.e(TAG,"Create tables - Rollback Sqlite transaction."); }
 	};
 
 	/** Give the table the opportunity to upgrade itself. If it returns true, the
 	 * table will be deleted and re-created.
 	 * */
 	@Override public void onUpgrade ( SQLiteDatabase db, int oldVersion, int newVersion ) {
		for (Table table : Tables.values()) {
 			StringBuilder sb = new StringBuilder();
 			// Give each table the option to upgrade itself.
 //			if (table.onUpgrade(db, oldVersion, newVersion)) {
 				// TODO simple migrations - Upgrades should be handled in a more realistic fashion, not just drop & replace...
 				// default to drop-create
 				sb = dropTableStatement(table, sb);
 				sb = createTableStatement(table, sb);
 //			}
 			Log.v(TAG,"Upgrade Transaction: "+sb.toString());
 			if (sb.length() > 15) {
 				execSqlAsTransaction(sb.toString(), db);
 			} else {
 				Log.e(TAG,"Invalid Transaction.");
 			}
 		}
 	}
 }
