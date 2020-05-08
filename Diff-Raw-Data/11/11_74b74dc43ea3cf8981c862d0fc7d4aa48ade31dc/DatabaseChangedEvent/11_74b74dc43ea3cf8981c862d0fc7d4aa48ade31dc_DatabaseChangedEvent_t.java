 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.morphoss.acal;
 
 import android.content.ContentValues;
 
 import com.morphoss.acal.providers.DavResources;
 import com.morphoss.acal.service.aCalService;
 
 public class DatabaseChangedEvent {
 
 	public static final int DATABASE_RECORD_DELETED = 1;
 	public static final int DATABASE_RECORD_UPDATED = 2;
 	public static final int DATABASE_RECORD_INSERTED = 3;
 	public static final int DATABASE_INVALIDATED = 4;
 	public static final int DATABASE_BEGIN_RESOURCE_CHANGES = 5;
 	public static final int DATABASE_END_RESOURCE_CHANGES = 6;
 	
 	private int eventType;
	private Class<?> table;
 	private ContentValues data;
 	
 	public int getEventType() {
 		return eventType;
 	}
 
	public Class<?> getTable() {
 		return table;
 	}
 
 	public ContentValues getContentValues() {
 		return data;
 	}
 
	public DatabaseChangedEvent(int eventType, Class<?> table, ContentValues data) {
 		if (eventType < 1 || eventType > 6) throw new IllegalArgumentException("DatabaseChangedEvent instantiated with invalid event type.");
 		this.eventType = eventType;
 		this.table = table;
 		this.data = data;
 	}
 
 	public static void beginResourceChanges() {
 		aCalService.databaseDispatcher.dispatchEvent(new DatabaseChangedEvent(DatabaseChangedEvent.DATABASE_BEGIN_RESOURCE_CHANGES, DavResources.class, null));
 	}
 
 	public static void endResourceChanges() {
 		aCalService.databaseDispatcher.dispatchEvent(new DatabaseChangedEvent(DatabaseChangedEvent.DATABASE_END_RESOURCE_CHANGES, DavResources.class, null));
 	}
 }
