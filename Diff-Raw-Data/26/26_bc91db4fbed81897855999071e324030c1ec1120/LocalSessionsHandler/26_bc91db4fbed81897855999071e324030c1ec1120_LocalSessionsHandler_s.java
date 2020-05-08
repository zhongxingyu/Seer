 /*
  * Copyright 2011 Google Inc.
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
 
 package org.androidtitlan.estoesgoogle.io;
 
 import static org.androidtitlan.estoesgoogle.util.ParserUtils.sanitizeId;
 import static org.androidtitlan.estoesgoogle.util.ParserUtils.splitComma;
 import static org.androidtitlan.estoesgoogle.util.ParserUtils.translateTrackIdAlias;
 import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
 import static org.xmlpull.v1.XmlPullParser.END_TAG;
 import static org.xmlpull.v1.XmlPullParser.START_TAG;
 import static org.xmlpull.v1.XmlPullParser.TEXT;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import org.androidtitlan.estoesgoogle.provider.ScheduleContract;
 import org.androidtitlan.estoesgoogle.provider.ScheduleContract.Rooms;
 import org.androidtitlan.estoesgoogle.provider.ScheduleContract.Sessions;
 import org.androidtitlan.estoesgoogle.provider.ScheduleDatabase.SessionsSpeakers;
 import org.androidtitlan.estoesgoogle.provider.ScheduleDatabase.SessionsTracks;
 import org.androidtitlan.estoesgoogle.util.Lists;
 import org.androidtitlan.estoesgoogle.util.ParserUtils;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.ContentProviderOperation;
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 
 
 public class LocalSessionsHandler extends XmlHandler {
 
     public LocalSessionsHandler() {
         super(ScheduleContract.CONTENT_AUTHORITY);
     }
 
     @Override
     public ArrayList<ContentProviderOperation> parse(XmlPullParser parser, ContentResolver resolver)
             throws XmlPullParserException, IOException {
         final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
 
         int type;
         while ((type = parser.next()) != END_DOCUMENT) {
             if (type == START_TAG && Tags.SESSION.equals(parser.getName())) {
                 parseSession(parser, batch, resolver);
             }
         }
 
         return batch;
     }
 
     private static void parseSession(XmlPullParser parser,
             ArrayList<ContentProviderOperation> batch, ContentResolver resolver)
             throws XmlPullParserException, IOException {
         final int depth = parser.getDepth();
         ContentProviderOperation.Builder builder = ContentProviderOperation
                 .newInsert(Sessions.CONTENT_URI);
         
         builder.withValue(Sessions.UPDATED, 0);
         
         String time=null;
         String date=null;
         String title = null;
         String sessionId = null;
 
         String tag = null;
         int type;
         String[] speakers=null;
         String[] tracks=null;
         String keywords=null;
         
         while (((type = parser.next()) != END_TAG ||
                 parser.getDepth() > depth) && type != END_DOCUMENT) {
             if (type == START_TAG) {
                 tag = parser.getName();
             } else if (type == END_TAG) {
                 tag = null;
             } else if (type == TEXT) {
                 final String text = parser.getText();
                 if (Tags.TIME.equals(tag)) {
                     time = text;
                 } else if (Tags.DATE.equals(tag)) {
                     date = text;
                 } else if (Tags.ROOM.equals(tag)) {
                     final String roomId = Rooms.generateRoomId(text);
                     builder.withValue(Sessions.ROOM_ID, roomId);
                 } else if (Tags.TRACK.equals(tag)) {
                 	tracks=splitComma(text);
                 } else if (Tags.ID.equals(tag)) {
                     sessionId = text;
                 } else if (Tags.TITLE.equals(tag)) {
                     title = text;
                 } else if (Tags.ABSTRACT.equals(tag)) {
                     builder.withValue(Sessions.SESSION_ABSTRACT, text);
                 } else if (Tags.SPEAKERS.equals(tag)) {
                 	speakers=ParserUtils.splitComma(text);
                 } else if(Tags.KEYWORDS.equals(tag)){
                 	keywords=text;
                 }
             }
         }
 
         if (sessionId == null) {
             sessionId = Sessions.generateSessionId(title);
         }
         
         final int timeSplit = time.indexOf("-");
         if (timeSplit == -1) {
             throw new HandlerException("Expecting sessiontime"
                     + " to express span");
         }
         final long startTime = parseTime(date, time.substring(0, timeSplit));
         final long endTime = parseTime(date, time.substring(timeSplit + 1));
 
         builder.withValue(Sessions.SESSION_ID, sessionId);
         builder.withValue(Sessions.SESSION_TITLE, title);
 
         // Use empty strings to make sure SQLite search trigger has valid data
         // for updating search index.
         builder.withValue(Sessions.SESSION_REQUIREMENTS, "");
         builder.withValue(Sessions.SESSION_KEYWORDS, keywords);
         
         final String blockId = ParserUtils.findOrCreateBlock(
                 ParserUtils.BLOCK_TITLE_BREAKOUT_SESSIONS,
                 ParserUtils.BLOCK_TYPE_SESSION,
                 startTime, endTime, batch, resolver);
         builder.withValue(Sessions.BLOCK_ID, blockId);
 
         // Propagate any existing starred value
         final Uri sessionUri = Sessions.buildSessionUri(sessionId);
         final int starred = querySessionStarred(sessionUri, resolver);
         if (starred != -1) {
             builder.withValue(Sessions.SESSION_STARRED, starred);
         }
 
         batch.add(builder.build());
         // Assign tracks
         for (String track : tracks) {
             final String trackId = translateTrackIdAlias(sanitizeId(track));
             batch.add(ContentProviderOperation.newInsert(Sessions.buildTracksDirUri(sessionId))
                     .withValue(SessionsTracks.SESSION_ID, sessionId)
                     .withValue(SessionsTracks.TRACK_ID, trackId).build());
         }
 
         // Assign speakers
         for (String speaker : speakers) {
             final String speakerId = sanitizeId(speaker, true);
             batch.add(ContentProviderOperation.newInsert(Sessions.buildSpeakersDirUri(sessionId))
                     .withValue(SessionsSpeakers.SESSION_ID, sessionId)
                     .withValue(SessionsSpeakers.SPEAKER_ID, speakerId).build());
         }
     }
 
     public static int querySessionStarred(Uri uri, ContentResolver resolver) {
         final String[] projection = { Sessions.SESSION_STARRED };
         final Cursor cursor = resolver.query(uri, projection, null, null, null);
         try {
             if (cursor.moveToFirst()) {
                 return cursor.getInt(0);
             } else {
                 return -1;
             }
         } finally {
             cursor.close();
         }
     }
     
     private static final SimpleDateFormat sTimeFormat = new SimpleDateFormat(
             "EEEE MMM d yyyy h:mma Z", Locale.US);
     
     private static long parseTime(String date, String time) throws HandlerException {
        final String composed = String.format("%s 2011 %s -0700", date, time);
         try {
             return sTimeFormat.parse(composed).getTime();
         } catch (java.text.ParseException e) {
             throw new HandlerException("Problem parsing timestamp", e);
         }
     }
 
     interface Tags {
         String SESSION = "session";
         String ID = "id";
         String TIME = "time";
         String DATE = "date";
         String ROOM = "room";
         String TRACK = "track";
         String TITLE = "title";
         String ABSTRACT = "abstract";
         String SPEAKERS = "speakers";
         String KEYWORDS = "keywords";
     }
 }
