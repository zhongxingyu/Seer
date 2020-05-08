 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.action.sql;
 
 import java.util.ArrayList;
 
 import org.gots.DatabaseHelper;
 import org.gots.action.ActionFactory;
 import org.gots.action.BaseActionInterface;
 import org.gots.garden.sql.GardenSQLite;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class ActionDBHelper {
 
 	private GardenSQLite actionSQLite;
 	private SQLiteDatabase bdd;
 	Context mContext;
 
 	public ActionDBHelper(Context mContext) {
 		actionSQLite = new GardenSQLite(mContext);
 
 		this.mContext = mContext;
 	}
 
 	public void open() {
 		// on ouvre la BDD en écriture
 		bdd = actionSQLite.getWritableDatabase();
 	}
 
 	public void close() {
 		// on ferme l'accès à la BDD
 
 		bdd.close();
 	}
 
 	public long insertAction(BaseActionInterface action) {
 		long rowid;
 		open();
 		ContentValues values = new ContentValues();
 		values.put(GardenSQLite.ACTION_NAME, action.getName());
 		values.put(GardenSQLite.ACTION_DESCRIPTION, action.getDescription());
 		values.put(GardenSQLite.ACTION_DURATION, action.getDuration());
 		try {
 			rowid = bdd.insert(GardenSQLite.ACTION_TABLE_NAME, null, values);
 		} finally {
 			close();
 		}
 		return rowid;
 	}
 
 	public ArrayList<BaseActionInterface> getActions() {
 		ArrayList<BaseActionInterface> allActions = new ArrayList<BaseActionInterface>();
 		// SeedActionInterface searchedSeed = new GrowingSeed();
 		open();
 		try {
 			Cursor cursor = bdd.query(GardenSQLite.ACTION_TABLE_NAME, null, null, null, null, null, null);
 
 			if (cursor.moveToFirst()) {
 				do {
 					BaseActionInterface action = cursorToAction(cursor);
					allActions.add(action);
 				} while (cursor.moveToNext());
 				cursor.close();
 			}
 		} finally {
 			close();
 		}
 		return allActions;
 	}
 
 	public boolean isExist(BaseActionInterface action) {
 		open();
 		boolean exists = false;
 		try {
 			Cursor cursor = bdd.query(GardenSQLite.ACTION_TABLE_NAME, null,
 					GardenSQLite.ACTION_NAME + "='" + action.getName() + "'", null, null, null, null);
 
 			if (cursor.getCount() > 0)
 				exists = true;
 			else
 				exists = false;
 
 			cursor.close();
 		} finally {
 			close();
 		}
 		return exists;
 	}
 
 	private BaseActionInterface cursorToAction(Cursor cursor) {
 		BaseActionInterface bsi;
 		ActionFactory factory = new ActionFactory();
 		bsi = factory.buildAction(mContext, cursor.getString(cursor.getColumnIndex(GardenSQLite.ACTION_NAME)));
 		bsi.setDescription(cursor.getString(cursor.getColumnIndex(GardenSQLite.ACTION_DESCRIPTION)));
 		bsi.setDuration(cursor.getInt(cursor.getColumnIndex(GardenSQLite.ACTION_DURATION)));
 		bsi.setId(cursor.getInt(cursor.getColumnIndex(GardenSQLite.ACTION_ID)));
 		return bsi;
 	}
 
 	public BaseActionInterface getActionByName(String name) {
 		BaseActionInterface action = null;
 		// SeedActionInterface searchedSeed = new GrowingSeed();
 		open();
 		try {
 			Cursor cursor = bdd.query(GardenSQLite.ACTION_TABLE_NAME, null, GardenSQLite.ACTION_NAME + "='" + name
 					+ "'", null, null, null, null);
 
 			if (cursor.moveToFirst()) {
 				action = cursorToAction(cursor);
 				cursor.close();
 			}
 		} finally {
 			close();
 		}
 		return action;
 
 	}
 
 	public BaseActionInterface getActionById(int id) {
 		BaseActionInterface action = null;
 		// SeedActionInterface searchedSeed = new GrowingSeed();
 		open();
 		try {
			Cursor cursor = bdd.query(GardenSQLite.ACTION_TABLE_NAME, null, GardenSQLite.ACTION_ID + "='" + id
					+ "'", null, null, null, null);
 
 			if (cursor.moveToFirst()) {
 				action = cursorToAction(cursor);
 				cursor.close();
 			}
 		} finally {
 			close();
 		}
 		return action;
 
 	}
 
 }
