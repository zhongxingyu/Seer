 /*
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.peterkuterna.android.apps.devoxxsched.provider;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.Blocks;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.Notes;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.Rooms;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.SearchSuggest;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.Sessions;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.Speakers;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.Sync;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.Tracks;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleDatabase.SessionsSearchColumns;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleDatabase.SessionsSpeakers;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleDatabase.SpeakersSearchColumns;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleDatabase.Tables;
 import net.peterkuterna.android.apps.devoxxsched.service.SyncService;
 import net.peterkuterna.android.apps.devoxxsched.util.NotesExporter;
 import net.peterkuterna.android.apps.devoxxsched.util.SelectionBuilder;
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.ContentProvider;
 import android.content.ContentProviderOperation;
 import android.content.ContentProviderResult;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.OperationApplicationException;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.ParcelFileDescriptor;
 import android.provider.BaseColumns;
 import android.provider.OpenableColumns;
 import android.util.Log;
 
 
 /**
  * Provider that stores {@link ScheduleContract} data. Data is usually inserted
  * by {@link SyncService}, and queried by various {@link Activity} instances.
  */
 public class ScheduleProvider extends ContentProvider {
 
 	private static final String TAG = "ScheduleProvider";
     private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
 
     private ScheduleDatabase mOpenHelper;
 
     private static final UriMatcher sUriMatcher = buildUriMatcher();
 
     private static final int SESSIONS = 100;
     private static final int SESSIONS_STARRED = 101;
     private static final int SESSIONS_NEW = 102;
     private static final int SESSIONS_UPDATED = 103;
     private static final int SESSIONS_UPDATED_STARRED = 104;
     private static final int SESSIONS_SEARCH = 105;
     private static final int SESSIONS_AT = 106;
     private static final int SESSIONS_PARALLEL = 107;
     private static final int SESSIONS_ID = 108;
     private static final int SESSIONS_ID_SPEAKERS = 109;
     private static final int SESSIONS_ID_SPEAKERS_ID = 110;
     private static final int SESSIONS_ID_NOTES = 111;
 
     private static final int SPEAKERS = 200;
     private static final int SPEAKERS_STARRED = 201;
     private static final int SPEAKERS_SEARCH = 202;
     private static final int SPEAKERS_ID = 203;
     private static final int SPEAKERS_ID_SESSIONS = 204;
 
     private static final int ROOMS = 300;
     private static final int ROOMS_ID = 301;
     private static final int ROOMS_WITH_NAME = 302;
     private static final int ROOMS_ID_SESSIONS = 303;
 
     private static final int BLOCKS = 400;
     private static final int BLOCKS_BETWEEN = 401;
     private static final int BLOCKS_ID = 402;
     private static final int BLOCKS_ID_SESSIONS = 403;
 
     private static final int NOTES = 500;
     private static final int NOTES_EXPORT = 501;
     private static final int NOTES_ID = 502;
 
     private static final int TRACKS = 600;
     private static final int TRACKS_ID = 601;
     private static final int TRACKS_ID_SESSIONS = 602;
 
     private static final int SYNC = 700;
     private static final int SYNC_ID = 701;
 
     private static final int SEARCH_SUGGEST = 800;
 
     private static final String MIME_XML = "text/xml";
 
     /**
      * Build and return a {@link UriMatcher} that catches all {@link Uri}
      * variations supported by this {@link ContentProvider}.
      */
     private static UriMatcher buildUriMatcher() {
         final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
         final String authority = ScheduleContract.CONTENT_AUTHORITY;
 
         matcher.addURI(authority, "sessions", SESSIONS);
         matcher.addURI(authority, "sessions/starred", SESSIONS_STARRED);
         matcher.addURI(authority, "sessions/new", SESSIONS_NEW);
         matcher.addURI(authority, "sessions/updated", SESSIONS_UPDATED);
         matcher.addURI(authority, "sessions/updated/starred", SESSIONS_UPDATED_STARRED);
         matcher.addURI(authority, "sessions/search/*", SESSIONS_SEARCH);
         matcher.addURI(authority, "sessions/at/*", SESSIONS_AT);
         matcher.addURI(authority, "sessions/parallel/*", SESSIONS_PARALLEL);
         matcher.addURI(authority, "sessions/*", SESSIONS_ID);
         matcher.addURI(authority, "sessions/*/speakers", SESSIONS_ID_SPEAKERS);
         matcher.addURI(authority, "sessions/*/speakers/*", SESSIONS_ID_SPEAKERS_ID);
         matcher.addURI(authority, "sessions/*/notes", SESSIONS_ID_NOTES);
 
         matcher.addURI(authority, "speakers", SPEAKERS);
         matcher.addURI(authority, "speakers/starred", SPEAKERS_STARRED);
         matcher.addURI(authority, "speakers/search/*", SPEAKERS_SEARCH);
         matcher.addURI(authority, "speakers/*", SPEAKERS_ID);
         matcher.addURI(authority, "speakers/*/sessions", SPEAKERS_ID_SESSIONS);
 
         matcher.addURI(authority, "rooms", ROOMS);
         matcher.addURI(authority, "rooms/name/*", ROOMS_WITH_NAME);
         matcher.addURI(authority, "rooms/*", ROOMS_ID);
         matcher.addURI(authority, "rooms/*/sessions", ROOMS_ID_SESSIONS);
 
         matcher.addURI(authority, "blocks", BLOCKS);
         matcher.addURI(authority, "blocks/between/*/*", BLOCKS_BETWEEN);
         matcher.addURI(authority, "blocks/*", BLOCKS_ID);
         matcher.addURI(authority, "blocks/*/sessions", BLOCKS_ID_SESSIONS);
 
         matcher.addURI(authority, "notes", NOTES);
         matcher.addURI(authority, "notes/export", NOTES_EXPORT);
         matcher.addURI(authority, "notes/*", NOTES_ID);
 
         matcher.addURI(authority, "tracks", TRACKS);
         matcher.addURI(authority, "tracks/*", TRACKS_ID);
         matcher.addURI(authority, "tracks/*/sessions", TRACKS_ID_SESSIONS);
 
         matcher.addURI(authority, "sync", SYNC);
         matcher.addURI(authority, "sync/*", SYNC_ID);
 
         matcher.addURI(authority, "search_suggest_query", SEARCH_SUGGEST);
 
         return matcher;
     }
 
     @Override
     public boolean onCreate() {
         final Context context = getContext();
         mOpenHelper = new ScheduleDatabase(context);
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getType(Uri uri) {
         final int match = sUriMatcher.match(uri);
         switch (match) {
             case SESSIONS:
                 return Sessions.CONTENT_TYPE;
             case SESSIONS_STARRED:
                 return Sessions.CONTENT_TYPE;
             case SESSIONS_NEW:
                 return Sessions.CONTENT_TYPE;
             case SESSIONS_UPDATED:
                 return Sessions.CONTENT_TYPE;
             case SESSIONS_UPDATED_STARRED:
                 return Sessions.CONTENT_TYPE;
             case SESSIONS_SEARCH:
                 return Sessions.CONTENT_TYPE;
             case SESSIONS_AT:
                 return Sessions.CONTENT_TYPE;
             case SESSIONS_PARALLEL:
                 return Sessions.CONTENT_TYPE;
             case SESSIONS_ID:
                 return Sessions.CONTENT_ITEM_TYPE;
             case SESSIONS_ID_SPEAKERS:
                 return Speakers.CONTENT_TYPE;
             case SESSIONS_ID_NOTES:
                 return Notes.CONTENT_TYPE;
             case SPEAKERS:
                 return Speakers.CONTENT_TYPE;
             case SPEAKERS_STARRED:
                 return Speakers.CONTENT_TYPE;
             case SPEAKERS_SEARCH:
                 return Speakers.CONTENT_TYPE;
             case SPEAKERS_ID:
                 return Speakers.CONTENT_ITEM_TYPE;
             case SPEAKERS_ID_SESSIONS:
                 return Sessions.CONTENT_TYPE;
             case ROOMS:
                 return Rooms.CONTENT_TYPE;
             case ROOMS_ID:
                 return Rooms.CONTENT_ITEM_TYPE;
             case ROOMS_WITH_NAME:
                 return Rooms.CONTENT_ITEM_TYPE;
             case ROOMS_ID_SESSIONS:
                 return Sessions.CONTENT_TYPE;
             case BLOCKS:
                 return Blocks.CONTENT_TYPE;
             case BLOCKS_BETWEEN:
                 return Blocks.CONTENT_TYPE;
             case BLOCKS_ID:
                 return Blocks.CONTENT_ITEM_TYPE;
             case BLOCKS_ID_SESSIONS:
                 return Sessions.CONTENT_TYPE;
             case NOTES:
                 return Notes.CONTENT_TYPE;
             case NOTES_EXPORT:
                 return MIME_XML;
             case NOTES_ID:
                 return Notes.CONTENT_ITEM_TYPE;
             case TRACKS:
                 return Tracks.CONTENT_TYPE;
             case TRACKS_ID:
                 return Tracks.CONTENT_ITEM_TYPE;
             case SYNC:
                 return Sync.CONTENT_TYPE;
             case SYNC_ID:
                 return Sync.CONTENT_ITEM_TYPE;
             case TRACKS_ID_SESSIONS:
                 return Sessions.CONTENT_TYPE;
             default:
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
             String sortOrder) {
         if (LOGV) Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
         final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
 
         final int match = sUriMatcher.match(uri);
         switch (match) {
             default: {
                 // Most cases are handled with simple SelectionBuilder
                 final SelectionBuilder builder = buildExpandedSelection(uri, match);
                 return builder.where(selection, selectionArgs).query(db, projection, sortOrder);
             }
             case NOTES_EXPORT: {
                 // Provide query values for file attachments
                 final String[] columns = { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };
                 final MatrixCursor cursor = new MatrixCursor(columns, 1);
                 cursor.addRow(new String[] { "notes.xml", null });
                 return cursor;
             }
             case SEARCH_SUGGEST: {
                 final SelectionBuilder builder = new SelectionBuilder();
 
                 // Adjust incoming query to become SQL text match
                 selectionArgs[0] = selectionArgs[0] + "%";
                 builder.table(Tables.SEARCH_SUGGEST);
                 builder.where(selection, selectionArgs);
                 builder.map(SearchManager.SUGGEST_COLUMN_QUERY,
                         SearchManager.SUGGEST_COLUMN_TEXT_1);
 
                 projection = new String[] { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                         SearchManager.SUGGEST_COLUMN_QUERY };
 
                 final String limit = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
                 return builder.query(db, projection, null, null, SearchSuggest.DEFAULT_SORT, limit);
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         if (LOGV) Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
         final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         final int match = sUriMatcher.match(uri);
         switch (match) {
             case SESSIONS: {
                 db.insertOrThrow(Tables.SESSIONS, null, values);
                 return Sessions.buildSessionUri(values.getAsString(Sessions.SESSION_ID));
             }
             case SESSIONS_ID_SPEAKERS: {
                 db.insertOrThrow(Tables.SESSIONS_SPEAKERS, null, values);
                 return Speakers.buildSpeakerUri(values.getAsString(SessionsSpeakers.SPEAKER_ID));
             }
             case SESSIONS_ID_NOTES: {
                 final String sessionId = Sessions.getSessionId(uri);
                 values.put(Notes.SESSION_ID, sessionId);
                 final long noteId = db.insertOrThrow(Tables.NOTES, null, values);
                 return ContentUris.withAppendedId(Notes.CONTENT_URI, noteId);
             }
             case SPEAKERS: {
                 db.insertOrThrow(Tables.SPEAKERS, null, values);
                 return Speakers.buildSpeakerUri(values.getAsString(Speakers.SPEAKER_ID));
             }
             case SPEAKERS_ID_SESSIONS: {
                 db.insertOrThrow(Tables.SESSIONS_SPEAKERS, null, values);
                 return Sessions.buildSessionUri(values.getAsString(SessionsSpeakers.SESSION_ID));
             }
             case ROOMS: {
                 db.insertOrThrow(Tables.ROOMS, null, values);
                 return Rooms.buildRoomUri(values.getAsString(Rooms.ROOM_ID));
             }
             case BLOCKS: {
                 db.insertOrThrow(Tables.BLOCKS, null, values);
                 return Blocks.buildBlockUri(values.getAsString(Blocks.BLOCK_ID));
             }
             case NOTES: {
                 final long noteId = db.insertOrThrow(Tables.NOTES, null, values);
                 return ContentUris.withAppendedId(Notes.CONTENT_URI, noteId);
             }
             case TRACKS: {
                 db.insertOrThrow(Tables.TRACKS, null, values);
                 return Tracks.buildTrackUri(values.getAsString(Tracks.TRACK_ID));
             }
             case SYNC: {
                 db.insertOrThrow(Tables.SYNC, null, values);
                 return Sync.buildSyncUri(values.getAsString(Sync.URI_ID));
             }
             case SEARCH_SUGGEST: {
                 db.insertOrThrow(Tables.SEARCH_SUGGEST, null, values);
                 return SearchSuggest.CONTENT_URI;
             }
             default: {
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
         if (LOGV) Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
         final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         final SelectionBuilder builder = buildSimpleSelection(uri);
         return builder.where(selection, selectionArgs).update(db, values);
     }
 
     /** {@inheritDoc} */
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         if (LOGV) Log.v(TAG, "delete(uri=" + uri + ")");
         final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         final SelectionBuilder builder = buildSimpleSelection(uri);
         return builder.where(selection, selectionArgs).delete(db);
     }
 
     /**
      * Apply the given set of {@link ContentProviderOperation}, executing inside
      * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
      * any single one fails.
      */
     @Override
     public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
             throws OperationApplicationException {
         final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         db.beginTransaction();
         try {
             final int numOperations = operations.size();
             final ContentProviderResult[] results = new ContentProviderResult[numOperations];
             for (int i = 0; i < numOperations; i++) {
                 results[i] = operations.get(i).apply(this, results, i);
             }
             db.setTransactionSuccessful();
             return results;
         } finally {
             db.endTransaction();
         }
     }
 
     /**
      * Build a simple {@link SelectionBuilder} to match the requested
      * {@link Uri}. This is usually enough to support {@link #insert},
      * {@link #update}, and {@link #delete} operations.
      */
     private SelectionBuilder buildSimpleSelection(Uri uri) {
         final SelectionBuilder builder = new SelectionBuilder();
         final int match = sUriMatcher.match(uri);
         switch (match) {
             case SESSIONS: {
                 return builder.table(Tables.SESSIONS);
             }
             case SESSIONS_NEW: {
                 return builder.table(Tables.SESSIONS)
                 .where(Sessions.NEW + "=1");
             }
             case SESSIONS_UPDATED: {
                 return builder.table(Tables.SESSIONS)
                 .where(Sessions.UPDATED + "=1");
             }
             case SESSIONS_UPDATED_STARRED: {
                 return builder.table(Tables.SESSIONS)
                 .where(Sessions.UPDATED + "=1")
                 .where(Sessions.STARRED + "=1");
             }
             case SESSIONS_ID: {
                 final String sessionId = Sessions.getSessionId(uri);
                 return builder.table(Tables.SESSIONS)
                         .where(Sessions.SESSION_ID + "=?", sessionId);
             }
             case SESSIONS_ID_SPEAKERS: {
                 final String sessionId = Sessions.getSessionId(uri);
                 return builder.table(Tables.SESSIONS_SPEAKERS)
                         .where(SessionsSpeakers.SESSION_ID + "=?", sessionId);
             }
             case SESSIONS_ID_SPEAKERS_ID: {
                 final String sessionId = Sessions.getSessionId(uri);
                 final String speakerId = Sessions.getSpeakerId(uri);
                 return builder.table(Tables.SESSIONS_SPEAKERS)
                         .where(SessionsSpeakers.SESSION_ID + "=?", sessionId)
                         .where(SessionsSpeakers.SPEAKER_ID + "=?", speakerId);
             }
             case SPEAKERS: {
                 return builder.table(Tables.SPEAKERS);
             }
             case SPEAKERS_ID: {
                 final String speakerId = Speakers.getSpeakerId(uri);
                 return builder.table(Tables.SPEAKERS)
                         .where(Speakers.SPEAKER_ID + "=?", speakerId);
             }
             case SPEAKERS_ID_SESSIONS: {
                 final String speakerId = Speakers.getSpeakerId(uri);
                 return builder.table(Tables.SESSIONS_SPEAKERS)
                         .where(Speakers.SPEAKER_ID + "=?", speakerId);
             }
             case ROOMS: {
                 return builder.table(Tables.ROOMS);
             }
             case ROOMS_ID: {
                 final String roomId = Rooms.getRoomId(uri);
                 return builder.table(Tables.ROOMS)
                         .where(Rooms.ROOM_ID + "=?", roomId);
             }
             case ROOMS_WITH_NAME: {
                 final String roomName = Rooms.getRoomName(uri);
                 return builder.table(Tables.ROOMS)
                         .where(Rooms.NAME + "=?", roomName);
             }
             case BLOCKS: {
                 return builder.table(Tables.BLOCKS);
             }
             case BLOCKS_ID: {
                 final String blockId = Blocks.getBlockId(uri);
                 return builder.table(Tables.BLOCKS)
                         .where(Blocks.BLOCK_ID + "=?", blockId);
             }
             case NOTES: {
                 return builder.table(Tables.NOTES);
             }
             case NOTES_ID: {
                 final String noteId = uri.getPathSegments().get(1);
                 return builder.table(Tables.NOTES)
                         .where(Notes._ID + "=?", noteId);
             }
             case TRACKS: {
                 return builder.table(Tables.TRACKS);
             }
             case TRACKS_ID: {
                 final String trackId = Tracks.getTrackId(uri);
                 return builder.table(Tables.TRACKS)
                         .where(Tracks.TRACK_ID + "=?", trackId);
             }
             case SYNC: {
                 return builder.table(Tables.TRACKS);
             }
             case SYNC_ID: {
                 final String syncId = Sync.getSyncId(uri);
                 return builder.table(Tables.SYNC)
                         .where(Sync.URI_ID + "=?", syncId);
             }
             case TRACKS_ID_SESSIONS: {
                 final String trackId = Tracks.getTrackId(uri);
                 return builder.table(Tables.SESSIONS)
                         .where(Qualified.SESSIONS_TRACK_ID + "=?", trackId);
             }
             case SEARCH_SUGGEST: {
                 return builder.table(Tables.SEARCH_SUGGEST);
             }
             default: {
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
             }
         }
     }
 
     /**
      * Build an advanced {@link SelectionBuilder} to match the requested
      * {@link Uri}. This is usually only used by {@link #query}, since it
      * performs table joins useful for {@link Cursor} data.
      */
     private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
         final SelectionBuilder builder = new SelectionBuilder();
         switch (match) {
             case SESSIONS: {
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                 		.mapToTable(Sessions._ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS);
             }
             case SESSIONS_STARRED: {
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Sessions.STARRED + "=1");
             }
             case SESSIONS_NEW: {
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Sessions.NEW + "=1");
             }
             case SESSIONS_UPDATED: {
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Sessions.UPDATED + "=1");
             }
             case SESSIONS_UPDATED_STARRED: {
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Sessions.UPDATED + "=1")
                         .where(Sessions.STARRED + "=1");
             }
             case SESSIONS_SEARCH: {
                 final String query = Sessions.getSearchQuery(uri);
                 return builder.table(Tables.SESSIONS_SEARCH_JOIN_SESSIONS_BLOCKS_ROOMS_TRACKS)
                         .map(Sessions.SEARCH_SNIPPET, Subquery.SESSIONS_SNIPPET)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.SESSION_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(SessionsSearchColumns.BODY + " MATCH ?", query);
             }
             case SESSIONS_AT: {
                 final List<String> segments = uri.getPathSegments();
                 final String time = segments.get(2);
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Sessions.BLOCK_START + "<=?", time)
                         .where(Sessions.BLOCK_END + ">=?", time);
             }
             case SESSIONS_PARALLEL: {
                 final List<String> segments = uri.getPathSegments();
                 final String sessionId = segments.get(2);
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(WhereClause.SESSIONS_PARALLEL, sessionId, sessionId)
                         .where(Sessions.SESSION_ID + "<>?", sessionId);
             }
             case SESSIONS_ID: {
                 final String sessionId = Sessions.getSessionId(uri);
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Qualified.SESSIONS_SESSION_ID + "=?", sessionId);
             }
             case SESSIONS_ID_SPEAKERS: {
                 final String sessionId = Sessions.getSessionId(uri);
                 return builder.table(Tables.SESSIONS_SPEAKERS_JOIN_SPEAKERS)
                         .mapToTable(Speakers._ID, Tables.SPEAKERS)
                         .mapToTable(Speakers.SPEAKER_ID, Tables.SPEAKERS)
                         .where(Qualified.SESSIONS_SPEAKERS_SESSION_ID + "=?", sessionId);
             }
             case SESSIONS_ID_NOTES: {
                 final String sessionId = Sessions.getSessionId(uri);
                 return builder.table(Tables.NOTES_JOIN_SESSIONS_TRACKS)
 		        		.mapToTable(Notes._ID, Tables.NOTES)
 		        		.mapToTable(Notes.SESSION_ID, Tables.NOTES)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Qualified.NOTES_SESSION_ID + "=?", sessionId);
             }
             case SPEAKERS: {
                 return builder.table(Tables.SPEAKERS)
                    		.map(Speakers.CONTAINS_STARRED, Subquery.SPEAKER_CONTAINS_STARRED);
             }
             case SPEAKERS_STARRED: {
                 return builder.table(Tables.SPEAKERS)
                    		.map(Speakers.CONTAINS_STARRED, Subquery.SPEAKER_CONTAINS_STARRED)
                         .where(Speakers.CONTAINS_STARRED + "=1");
             }
             case SPEAKERS_SEARCH: {
                 final String query = Sessions.getSearchQuery(uri);
                 return builder.table(Tables.SPEAKERS_SEARCH_JOIN_SPEAKERS)
                         .map(Speakers.SEARCH_SNIPPET, Subquery.SPEAKERS_SNIPPET)
                    		.map(Speakers.CONTAINS_STARRED, Subquery.SPEAKER_CONTAINS_STARRED)
                         .mapToTable(Speakers._ID, Tables.SPEAKERS)
                         .mapToTable(Speakers.SPEAKER_ID, Tables.SPEAKERS)
                         .where(SpeakersSearchColumns.BODY + " MATCH ?", query);
             }
             case SPEAKERS_ID: {
                 final String speakerId = Speakers.getSpeakerId(uri);
                 return builder.table(Tables.SPEAKERS)
                         .where(Speakers.SPEAKER_ID + "=?", speakerId);
             }
             case SPEAKERS_ID_SESSIONS: {
                 final String speakerId = Speakers.getSpeakerId(uri);
                 return builder.table(Tables.SESSIONS_SPEAKERS_JOIN_SESSIONS_BLOCKS_ROOMS_TRACKS)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.SESSION_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                         .mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Qualified.SESSIONS_SPEAKERS_SPEAKER_ID + "=?", speakerId);
             }
             case ROOMS: {
                 return builder.table(Tables.ROOMS);
             }
             case ROOMS_ID: {
                 final String roomId = Rooms.getRoomId(uri);
                 return builder.table(Tables.ROOMS)
                         .where(Rooms.ROOM_ID + "=?", roomId);
             }
             case ROOMS_WITH_NAME: {
                 final String roomName = Rooms.getRoomName(uri);
                 return builder.table(Tables.ROOMS)
                         .where(Rooms.NAME + "=?", roomName);
             }
             case ROOMS_ID_SESSIONS: {
                 final String roomId = Rooms.getRoomId(uri);
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Qualified.SESSIONS_ROOM_ID + "=?", roomId);
             }
             case BLOCKS: {
                 return builder.table(Tables.BLOCKS);
             }
             case BLOCKS_BETWEEN: {
                 final List<String> segments = uri.getPathSegments();
                 final String startTime = segments.get(2);
                 final String endTime = segments.get(3);
                 builder.table(Tables.BLOCKS)
                         .map(Blocks.SESSIONS_COUNT, Subquery.BLOCK_SESSIONS_COUNT)
                         .map(Blocks.CONTAINS_STARRED, Subquery.BLOCK_CONTAINS_STARRED)
                         .where(Blocks.BLOCK_START + ">=?", startTime)
                         .where(Blocks.BLOCK_START + "<=?", endTime);
                 return builder;
             }
             case BLOCKS_ID: {
                 final String blockId = Blocks.getBlockId(uri);
                 return builder.table(Tables.BLOCKS)
                         .map(Blocks.SESSIONS_COUNT, Subquery.BLOCK_SESSIONS_COUNT)
                         .map(Blocks.CONTAINS_STARRED, Subquery.BLOCK_CONTAINS_STARRED)
                         .where(Blocks.BLOCK_ID + "=?", blockId);
             }
             case BLOCKS_ID_SESSIONS: {
                 final String blockId = Blocks.getBlockId(uri);
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                         .map(Blocks.SESSIONS_COUNT, Subquery.BLOCK_SESSIONS_COUNT)
                         .map(Blocks.CONTAINS_STARRED, Subquery.BLOCK_CONTAINS_STARRED)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.SESSION_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Qualified.SESSIONS_BLOCK_ID + "=?", blockId);
             }
             case NOTES: {
                 return builder.table(Tables.NOTES_JOIN_SESSIONS_TRACKS)
                 		.mapToTable(Notes._ID, Tables.NOTES)
                 		.mapToTable(Notes.SESSION_ID, Tables.NOTES)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS);
             }
             case NOTES_ID: {
                 final long noteId = Notes.getNoteId(uri);
                 return builder.table(Tables.NOTES)
                         .where(Notes._ID + "=?", Long.toString(noteId));
             }
             case TRACKS: {
                 return builder.table(Tables.TRACKS)
                         .map(Tracks.SESSIONS_COUNT, Subquery.TRACK_SESSIONS_COUNT);
             }
             case TRACKS_ID: {
                 final String trackId = Tracks.getTrackId(uri);
                 return builder.table(Tables.TRACKS)
                         .where(Tracks.TRACK_ID + "=?", trackId);
             }
             case SYNC: {
                 return builder.table(Tables.SYNC);
             }
             case SYNC_ID: {
                 final String syncId = Sync.getSyncId(uri);
                 return builder.table(Tables.SYNC)
                         .where(Sync.URI_ID + "=?", syncId);
             }
             case TRACKS_ID_SESSIONS: {
                 final String trackId = Tracks.getTrackId(uri);
                 return builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS_TRACKS)
                         .mapToTable(Sessions._ID, Tables.SESSIONS)
                         .mapToTable(Sessions.SESSION_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
                         .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                 		.mapToTable(Sessions.TRACK_ID, Tables.SESSIONS)
                         .map(Sessions.STARRED_IN_BLOCK_COUNT, Subquery.BLOCK_STARRED_SESSIONS_COUNT)
                 		.mapToTable(Tracks.TRACK_COLOR, Tables.TRACKS)
                         .where(Qualified.SESSIONS_TRACK_ID + "=?", trackId);
             }
             default: {
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
             }
         }
     }
 
     @Override
     public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
         final int match = sUriMatcher.match(uri);
         switch (match) {
             case NOTES_EXPORT: {
                 try {
                     final File notesFile = NotesExporter.writeExportedNotes(getContext());
                     return ParcelFileDescriptor
                             .open(notesFile, ParcelFileDescriptor.MODE_READ_ONLY);
                 } catch (IOException e) {
                     throw new FileNotFoundException("Unable to export notes: " + e.toString());
                 }
             }
             default: {
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
             }
         }
     }
     
     private interface Subquery {
         String BLOCK_SESSIONS_COUNT = "(SELECT COUNT(" + Qualified.SESSIONS_SESSION_ID + ") FROM "
                 + Tables.SESSIONS + " WHERE " + Qualified.SESSIONS_BLOCK_ID + "="
                 + Qualified.BLOCKS_BLOCK_ID + ")";
 
         String BLOCK_STARRED_SESSIONS_COUNT = "(SELECT COUNT(" + Qualified.SESSIONS_SESSION_ID + ") FROM "
        	+ Tables.SESSIONS + " WHERE " + Qualified.SESSIONS_STARRED + "=1 AND " 
        	+ Qualified.SESSIONS_BLOCK_ID + "=" + Qualified.BLOCKS_BLOCK_ID + ")";
 
         String BLOCK_CONTAINS_STARRED = "(SELECT MAX(" + Qualified.SESSIONS_STARRED + ") FROM "
                 + Tables.SESSIONS + " WHERE " + Qualified.SESSIONS_BLOCK_ID + "="
                 + Qualified.BLOCKS_BLOCK_ID + ")";
 
         String SPEAKER_CONTAINS_STARRED = "(SELECT MAX(" + Qualified.SESSIONS_STARRED + ") FROM "
         		+ Tables.SESSIONS + " LEFT OUTER JOIN " + Tables.SESSIONS_SPEAKERS + " ON "
         		+ Qualified.SESSIONS_SESSION_ID + "=" + Qualified.SESSIONS_SPEAKERS_SESSION_ID 
         		+ " WHERE " + Qualified.SESSIONS_SPEAKERS_SPEAKER_ID + "="
         		+ Qualified.SPEAKERS_SPEAKER_ID + ")";
 
         String TRACK_SESSIONS_COUNT = "(SELECT COUNT(" + Qualified.SESSIONS_TRACK_ID
                 + ") FROM " + Tables.SESSIONS + " WHERE "
                 + Qualified.SESSIONS_TRACK_ID + "=" + Qualified.TRACKS_TRACK_ID + ")";
         
         String SESSIONS_SNIPPET = "snippet(" + Tables.SESSIONS_SEARCH + ",'{','}','\u2026')";
         String SPEAKERS_SNIPPET = "snippet(" + Tables.SPEAKERS_SEARCH + ",'{','}','\u2026')";
     }
     
     private interface WhereClause {
     	String SESSIONS_PARALLEL = "(" + Sessions.BLOCK_START + " >= (SELECT "
     			+ Blocks.BLOCK_START + " FROM " + Tables.BLOCKS + " LEFT OUTER JOIN " 
     			+ Tables.SESSIONS + " ON " + Tables.BLOCKS + "." + Blocks.BLOCK_ID
     			+ "=" + Tables.SESSIONS + "." + Sessions.BLOCK_ID + " WHERE "
     			+ Tables.SESSIONS + "." + Sessions.SESSION_ID + " = ?) AND "
     			+ Sessions.BLOCK_END + " <= (SELECT " + Blocks.BLOCK_END + " FROM " 
     			+ Tables.BLOCKS + " LEFT OUTER JOIN " + Tables.SESSIONS + " ON " 
     			+ Tables.BLOCKS + "." + Blocks.BLOCK_ID + "=" 
     			+ Tables.SESSIONS + "." + Sessions.BLOCK_ID + " WHERE "
     			+ Tables.SESSIONS + "." + Sessions.SESSION_ID + " = ?))";
     }
 
     /**
      * {@link ScheduleContract} fields that are fully qualified with a specific
      * parent {@link Tables}. Used when needed to work around SQL ambiguity.
      */
     private interface Qualified {
         String SPEAKERS_SPEAKER_ID = Tables.SPEAKERS + "." + Speakers.SPEAKER_ID;
 
         String SESSIONS_SESSION_ID = Tables.SESSIONS + "." + Sessions.SESSION_ID;
         String SESSIONS_BLOCK_ID = Tables.SESSIONS + "." + Sessions.BLOCK_ID;
         String SESSIONS_ROOM_ID = Tables.SESSIONS + "." + Sessions.ROOM_ID;
         String SESSIONS_TRACK_ID = Tables.SESSIONS + "." + Sessions.TRACK_ID;
         
         String SPEAKERS_FIRST_NAME = Tables.SPEAKERS + "." + Speakers.FIRST_NAME;
 
         String SESSIONS_SPEAKERS_SESSION_ID = Tables.SESSIONS_SPEAKERS + "."
                 + SessionsSpeakers.SESSION_ID;
         String SESSIONS_SPEAKERS_SPEAKER_ID = Tables.SESSIONS_SPEAKERS + "."
                 + SessionsSpeakers.SPEAKER_ID;
 
         String SESSIONS_STARRED = Tables.SESSIONS + "." + Sessions.STARRED;
 
         String TRACKS_TRACK_ID = Tables.TRACKS + "." + Tracks.TRACK_ID;
 
         String BLOCKS_BLOCK_ID = Tables.BLOCKS + "." + Blocks.BLOCK_ID;
 
         String NOTES_SESSION_ID = Tables.NOTES + "." + Notes.SESSION_ID;
     }
 
 }
