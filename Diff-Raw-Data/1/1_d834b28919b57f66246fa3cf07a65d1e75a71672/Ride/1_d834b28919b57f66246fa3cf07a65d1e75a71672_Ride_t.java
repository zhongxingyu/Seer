 package org.teleportr;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class Ride implements Parcelable {
 
     public static final int SEARCH = 42;
     public static final int OFFER = 47;
 
     public static enum Mode {
         CAR, TRAIN
     };
 
     Context ctx;
     ContentValues cv;
     ArrayList<ContentValues> subrides;
 
     public Ride() {
         cv = new ContentValues();
     }
 
 
 
     public Ride from(Uri from) {
         return from(Integer.parseInt(from.getLastPathSegment()));
     }
 
     public Ride from(Place from) {
         return from(from.id);
     }
 
     public Ride from(int from_id) {
         cv.put("from_id", from_id);
         if (subrides != null)
             subrides = null;
         return this;
     }
 
 
     public Ride via(Uri via) {
         return to(Integer.parseInt(via.getLastPathSegment()));
     }
 
     public Ride via(Place via) {
         return via(via.id);
     }
 
     public Ride via(long via_id) {
         if (subrides == null) {
             subrides = new ArrayList<ContentValues>();
             ContentValues sub = new ContentValues();
             sub.put("from_id", cv.getAsLong("from_id"));
             sub.put("to_id", via_id);
             subrides.add(sub);
         } else
             subrides.get(subrides.size()-1).put("to_id", via_id);
         ContentValues sub = new ContentValues();
         sub.put("from_id", via_id);
         subrides.add(sub);
         return this;
     }
 
 
     public Ride to(Uri to) {
         return to(Integer.parseInt(to.getLastPathSegment()));
     }
 
     public Ride to(Place to) {
         return to(to.id);
     }
 
     public Ride to(long to_id) {
         cv.put("to_id", to_id);
         if (subrides != null) {
             subrides.get(subrides.size()-1)
                 .put("to_id", to_id);
         }
         return this;
     }
 
 
     public Ride dep(Date dep) {
         return dep(dep.getTime());
     }
 
     public Ride dep(long dep) {
         cv.put("dep", dep);
         return this;
     }
 
     public Ride arr(Date arr) {
         return arr(arr.getTime());
     }
 
     public Ride arr(long arr) {
         cv.put("arr", arr);
         return this;
     }
 
 
     public Ride mode(Mode mode) {
         cv.put("mode", mode.name());
         return this;
     }
 
     public Ride who(String who) {
         cv.put("who", who);
         return this;
     }
 
     public Ride details(String details) {
         cv.put("details", details);
         return this;
     }
 
     public Ride type(int type) {
         cv.put("type", type);
         return this;
     }
 
     public Ride ref(String ref) {
         cv.put("ref", ref);
         return this;
     }
 
     public Ride price(long price) {
         cv.put("price", price);
         return this;
     }
 
     public Ride seats(long seats) {
         cv.put("seats", seats);
         return this;
     }
 
     public Ride marked() {
         cv.put("marked", 1);
         return this;
     }
 
     public Uri store(Context ctx) {
         Uri ride;
         cv.put("parent_id", 0);
         Uri uri = Uri.parse("content://" + ctx.getPackageName() + "/rides");
         if (!cv.containsKey("_id")) {
             ride = ctx.getContentResolver().insert(uri, cv);
         } else {
             ride = ContentUris.withAppendedId(uri, cv.getAsInteger("_id"));
             ctx.getContentResolver().update(ride, cv, null, null);
             ctx.getContentResolver().delete(ride, null, null);
         }
         if (subrides != null) {
             for (ContentValues v : subrides) {
                 v.put("parent_id", Integer.valueOf(ride.getLastPathSegment()));
                 ctx.getContentResolver().insert(uri, v);
             }
         }
         return ride;
     }
 
 
 
     public static final class COLUMNS {
         public static final short ID = 0;
         public static final short TYPE = 1;
         public static final short FROM_ID = 2;
         public static final short FROM_NAME = 3;
         public static final short FROM_ADDRESS = 4;
         public static final short TO_ID = 5;
         public static final short TO_NAME = 6;
         public static final short TO_ADDRESS = 7;
         public static final short DEPARTURE = 8;
         public static final short ARRIVAL = 9;
         public static final short MODE = 10;
         public static final short OPERATOR = 11;
         public static final short WHO = 12;
         public static final short DETAILS = 13;
         public static final short DISTANCE = 14;
         public static final short PRICE = 15;
         public static final short SEATS = 16;
         public static final short MARKED = 17;
         public static final short DIRTY = 18;
         public static final short PARENT_ID = 19;
         public static final short REF = 20;
     }
 
 
     public Ride(Context ctx) {
         this.ctx = ctx;
     }
     
     public Ride(Uri uri) {
         cv = new ContentValues();
         type(Ride.SEARCH);
         from(Integer.parseInt(uri.getQueryParameter("from_id")));
         to(Integer.parseInt(uri.getQueryParameter("to_id")));
         if (uri.getQueryParameter("dep") != null)
             dep(Long.parseLong(uri.getQueryParameter("dep")));
         if (uri.getQueryParameter("arr") != null)
             arr(Long.parseLong(uri.getQueryParameter("arr")));
     }
     
 
     public Ride(Cursor cursor, Context ctx) {
         cv = new ContentValues();
         cv.put("_id", cursor.getLong(COLUMNS.ID));
         type(cursor.getInt(COLUMNS.TYPE));
         from(cursor.getInt(COLUMNS.FROM_ID));
         to(cursor.getInt(COLUMNS.TO_ID));
         dep(cursor.getLong(COLUMNS.DEPARTURE));
         arr(cursor.getLong(COLUMNS.ARRIVAL));
        mode(Mode.valueOf(cursor.getString(COLUMNS.MODE)));
         who(cursor.getString(COLUMNS.WHO));
         details(cursor.getString(COLUMNS.DETAILS));
         price(cursor.getInt(COLUMNS.PRICE));
         seats(cursor.getInt(COLUMNS.SEATS));
         ref(cursor.getString(COLUMNS.REF));
         Cursor s = ctx.getContentResolver().query(
                 Uri.parse("content://" + ctx.getPackageName() + "/rides/"
                         + cursor.getInt(0) + "/rides/"), null, null, null, null);
         subrides = new ArrayList<ContentValues>();
         for (int i = 0; i < s.getCount(); i++) {
             s.moveToPosition(i);
             subrides.add(new Ride(s, ctx).cv);
         }
         this.ctx = ctx;
     }
 
     public List<Ride> getSubrides() {
         ArrayList<Ride> subs = new ArrayList<Ride>();
         for (ContentValues v : subrides) {
             Ride r = new Ride(ctx);
             r.cv = v;
             subs.add(r);
         }
         return subs;
     }
 
     public Place getFrom() {
         return new Place(getFromId(), ctx);
     }
 
     public Place getTo() {
         return new Place(getToId(), ctx);
     }
 
     public int getFromId() {
             return cv.getAsInteger("from_id");
     }
 
     public int getToId() {
         return cv.getAsInteger("to_id");
     }
 
     public long getDep() {
         if (cv.containsKey("dep"))
             return cv.getAsLong("dep");
         else return System.currentTimeMillis();
     }
 
     public long getArr() {
         if (cv.containsKey("arr"))
             return cv.getAsLong("arr");
         else return System.currentTimeMillis();
     }
 
     public Mode getMode() {
         return Mode.valueOf(cv.getAsString("mode"));
     }
 
     public String getWho() {
         return cv.getAsString("who");
     }
     
     public String getDetails() {
         return cv.getAsString("details");
     }
     
     public int getPrice() {
         return cv.getAsInteger("price");
     }
     
     public int getSeats() {
         return cv.getAsInteger("seats");
     }
 
 
 
     @Override
     public int describeContents() {
         return 0;
     }
 
 
 
     @Override
     public void writeToParcel(Parcel out, int flags) {
         out.writeTypedList(subrides);
         out.writeParcelable(cv, 0);
     }
 
     public static final Parcelable.Creator<Ride> CREATOR
         = new Parcelable.Creator<Ride>() {
 
             @Override
             public Ride createFromParcel(Parcel in) {
                 Ride ride = new Ride();
                 ride.subrides = new ArrayList<ContentValues>();
                 in.readTypedList(ride.subrides, ContentValues.CREATOR);
                 ride.cv = in.readParcelable(getClass().getClassLoader());
                 return ride;
             }
 
             @Override
             public Ride[] newArray(int size) {
                 return new Ride[size];
             }
         };
 }
