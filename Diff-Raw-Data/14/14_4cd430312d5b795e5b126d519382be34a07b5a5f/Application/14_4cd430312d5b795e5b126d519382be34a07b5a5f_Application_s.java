 /*
  * Androzic - android navigation client that uses OziExplorer maps (ozf2, ozfx3).
  * Copyright (C) 2010-2013 Andrey Novikov <http://andreynovikov.info/>
  * 
  * This file is part of Androzic application.
  * 
  * Androzic is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Androzic is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Androzic. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.androzic.plugin.tracker;
 
 import java.util.HashMap;
 import java.util.Map;
 
import com.androzic.BaseApplication;
import com.androzic.data.Tracker;
import com.androzic.provider.DataContract;

 import android.content.ContentProviderClient;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.RemoteException;
 
 public class Application extends BaseApplication
 {
 	private Map<String, Long> mapObjectIds = new HashMap<String, Long>();
 
 	/**
 	 * Sends tracker to Androzic map
 	 * @throws RemoteException
 	 */
 	void sendMapObject(Tracker tracker) throws RemoteException
 	{
 		ContentProviderClient contentProvider = getContentResolver().acquireContentProviderClient(DataContract.MAPOBJECTS_URI);
 		ContentValues values = new ContentValues();
 		values.put(DataContract.MAPOBJECT_COLUMNS[DataContract.MAPOBJECT_LATITUDE_COLUMN], tracker.latitude);
 		values.put(DataContract.MAPOBJECT_COLUMNS[DataContract.MAPOBJECT_LONGITUDE_COLUMN], tracker.longitude);
 		values.put(DataContract.MAPOBJECT_COLUMNS[DataContract.MAPOBJECT_NAME_COLUMN], tracker.name);
 		synchronized (mapObjectIds)
 		{
 			Long id = mapObjectIds.get(tracker.imei);
 			if (id == null)
 			{
 				Uri uri = contentProvider.insert(DataContract.MAPOBJECTS_URI, values);
 				id = ContentUris.parseId(uri);
 				mapObjectIds.put(tracker.imei, id);
 			}
 			else
 			{
 				Uri uri = ContentUris.withAppendedId(DataContract.MAPOBJECTS_URI, id);
 				contentProvider.update(uri, values, null, null);
 			}
 		}
 		contentProvider.release();
 	}
 
 	/**
 	 * Sends all known trackers to Androzic map
 	 * @throws RemoteException
 	 */
 	void sendMapObjects() throws RemoteException
 	{
 		synchronized (mapObjectIds)
 		{
 			TrackerDataAccess dataAccess = new TrackerDataAccess(this);
 			Cursor cursor = dataAccess.getTrackers();
 			if (! cursor.moveToFirst())
 			{
 				dataAccess.close();
 				return;
 			}
			while (cursor.moveToNext())
 			{
 				Tracker tracker = dataAccess.getTracker(cursor);
 				sendMapObject(tracker);
 			}
 			dataAccess.close();
 		}
 	}
 
 	/**
 	 * Removes tracker from Androzic map
 	 * @throws RemoteException 
 	 */
 	void removeMapObject(Tracker tracker) throws RemoteException
 	{
 		synchronized (mapObjectIds)
 		{
 			Long id = mapObjectIds.get(tracker.imei);
 			if (id == null)
 				return;
 			mapObjectIds.remove(tracker.imei);
 			ContentProviderClient contentProvider = getContentResolver().acquireContentProviderClient(DataContract.MAPOBJECTS_URI);
 			Uri uri = ContentUris.withAppendedId(DataContract.MAPOBJECTS_URI, id);
 			contentProvider.delete(uri, null, null);
 			contentProvider.release();
 		}
 	}
 
 	/**
 	 * Removes all previously sent trackers from Androzic map
 	 * @throws RemoteException
 	 */
 	void removeMapObjects() throws RemoteException
 	{
 		ContentProviderClient contentProvider = getContentResolver().acquireContentProviderClient(DataContract.MAPOBJECTS_URI);
 		synchronized (mapObjectIds)
 		{
 			String[] args = new String[mapObjectIds.size()];
 			int i = 0;
 			for (Map.Entry<String,Long> entry : mapObjectIds.entrySet())
 			{
 				args[i] = String.valueOf(entry.getValue());
 				i++;
 			}
 			mapObjectIds.clear();
 			contentProvider.delete(DataContract.MAPOBJECTS_URI, DataContract.MAPOBJECT_ID_SELECTION, args);
 		}
 		contentProvider.release();
 	}
 	
 	@Override
 	public String getRootPath()
 	{
 		throw new java.lang.RuntimeException("getRootPath() not implemented");
 	}
 
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 		setInstance(this);
 	}
 }
