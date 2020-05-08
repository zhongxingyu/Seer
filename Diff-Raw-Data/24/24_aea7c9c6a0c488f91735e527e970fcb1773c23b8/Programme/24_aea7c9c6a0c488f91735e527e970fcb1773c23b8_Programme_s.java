 package fr.ybo.ybotv.android.modele;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import fr.ybo.database.annotation.Column;
 import fr.ybo.database.annotation.Entity;
 import fr.ybo.database.annotation.Indexed;
 import fr.ybo.database.annotation.PrimaryKey;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.database.YboTvDatabase;
 import fr.ybo.ybotv.android.util.PreferencesUtil;
 
 import java.io.Serializable;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
import java.util.*;
 
 @Entity
 public class Programme implements Serializable, Parcelable {
 
     @Column
     @PrimaryKey
     private String id;
     @Column
     @Indexed
     private String start;
     @Column
     @Indexed
     private String stop;
     @Column
     @Indexed
     private String channel;
     @Column
     private String icon;
     @Column
     private String title;
     @Column
     private String desc;
     @Column
     private String starRating;
     @Column
     private String csaRating;
     @Column( type = Column.TypeColumn.LIST_TEXT)
     private List<String> directors;
     @Column( type = Column.TypeColumn.LIST_TEXT)
     private List<String> actors;
     @Column( type = Column.TypeColumn.LIST_TEXT)
     private List<String> writers;
     @Column( type = Column.TypeColumn.LIST_TEXT)
     private List<String> presenters;
     @Column( type = Column.TypeColumn.LIST_TEXT)
     private List<String> categories;
     @Column
     private String date;
 
 
     private Map<String, String> ratings;
 
 
 
     public void fillFields() {
         if (ratings != null && ratings.containsKey("CSA")) {
             csaRating = ratings.get("CSA");
         }
     }
 
     public String getHoraires() {
 
         StringBuilder builder = new StringBuilder();
         builder.append(start.substring(8, 10));
         builder.append(':');
         builder.append(start.substring(10,12));
         builder.append(" - ");
         builder.append(stop.substring(8,10));
         builder.append(':');
         builder.append(stop.substring(10,12));
 
         return builder.toString();
 
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getStart() {
         return start;
     }
 
     public void setStart(String start) {
         this.start = start;
     }
 
     public String getStop() {
         return stop;
     }
 
     public void setStop(String stop) {
         this.stop = stop;
     }
 
     public String getChannel() {
         return channel;
     }
 
     public void setChannel(String channel) {
         this.channel = channel;
     }
 
     public String getIcon() {
         return icon;
     }
 
     public void setIcon(String icon) {
         this.icon = icon;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getDesc() {
         return desc;
     }
 
     public void setDesc(String desc) {
         this.desc = desc;
     }
 
     public String getStarRating() {
         return starRating;
     }
 
     public void setStarRating(String starRating) {
         this.starRating = starRating;
     }
 
     public String getCsaRating() {
         return csaRating;
     }
 
     public void setCsaRating(String csaRating) {
         this.csaRating = csaRating;
     }
 
     public List<String> getDirectors() {
         if (directors == null) {
             directors = new ArrayList<String>();
         }
         return directors;
     }
 
     public void fillDirectorsWithDb(String dbDirectors) {
         fillListWithCsv(dbDirectors, getDirectors());
     }
 
     private String getListInCsv(List<String> listString) {
         StringBuilder builder = new StringBuilder();
         boolean first = true;
         for (String uneValeur : listString) {
             if (!first) {
                 builder.append(';');
             }
             builder.append(uneValeur);
             first = false;
         }
         return builder.toString();
 
     }
 
     public String getDirectorsInCsv() {
         return getListInCsv(getDirectors());
     }
 
     public List<String> getActors() {
         if (actors == null) {
             actors = new ArrayList<String>();
         }
         return actors;
     }
 
     public void fillListWithCsv(String csv, List<String> list) {
         if (csv != null) {
             for (String oneValue : csv.split(";")) {
                 if (oneValue != null && oneValue.length() > 0) {
                     list.add(oneValue);
                 }
             }
         }
 
     }
 
     public void fillActorsWithDb(String dbActors) {
         fillListWithCsv(dbActors, getActors());
     }
 
     public String getActorsInCsv() {
         return getListInCsv(getActors());
     }
 
     public List<String> getWriters() {
         if (writers == null) {
             writers = new ArrayList<String>();
         }
         return writers;
     }
 
     public void fillWritersWithDb(String dbWriters) {
         fillListWithCsv(dbWriters, getWriters());
     }
 
     public String getWritersInCsv() {
         return getListInCsv(getWriters());
     }
 
     public List<String> getPresenters() {
         if (presenters == null) {
             presenters = new ArrayList<String>();
         }
         return presenters;
     }
 
     public void fillPresentersWithDb(String dbPresenters) {
         fillListWithCsv(dbPresenters, getPresenters());
     }
 
     public String getPresentersInCsv() {
         return getListInCsv(getPresenters());
     }
 
     public List<String> getCategories() {
         if (categories == null) {
             categories = new ArrayList<String>();
         }
         return categories;
     }
 
     public void fillCategoriesWithDb(String dbCategories) {
         fillListWithCsv(dbCategories, getCategories());
     }
 
     public String getCategoriesInCsv() {
         return getListInCsv(getCategories());
     }
 
     public String getDate() {
         return date;
     }
 
     public void setDate(String date) {
         this.date = date;
     }
 
     public boolean isMovie() {
         return !getCategories().isEmpty()
                 && getCategories().get(0).equalsIgnoreCase("film");
     }
 
     public String getDuree() {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
         String duree = null;
         try {
             Date dateStart = sdf.parse(start);
             Date dateEnd = sdf.parse(stop);
            Date dureeDate = new Date(dateEnd.getTime() - dateStart.getTime());
            duree = new SimpleDateFormat("HH:mm").format(dureeDate);
         } catch (ParseException e) {
             Log.e(YboTvApplication.TAG, e.getMessage(), e);
         }
         return duree;
     }
 
     private transient Boolean alert;
 
     private String getIdPref() {
         return "ybo-tv.programme.alert." + getId();
     }
 
     public boolean hasAlert(Context context) {
         if (alert != null) {
             return alert;
         }
         return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getIdPref(), false);
     }
 
     public void setHasAlert(Context context, boolean hasAlert) {
         alert = hasAlert;
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 
         Set<String> idsInNotif = PreferencesUtil.getStringSets(prefs, "ybo-tv.programme.alert.ids");
 
         if (hasAlert) {
             idsInNotif.add(id);
         } else {
             idsInNotif.remove(id);
         }
 
         PreferencesUtil.putStringSets(prefs, "ybo-tv.programme.alert.ids", idsInNotif);
 
         SharedPreferences.Editor editor = prefs.edit();
         editor.putBoolean(getIdPref(), hasAlert);
         editor.commit();
     }
 
     @Override
     public String toString() {
         return "Programme{" +
                 "id='" + id + '\'' +
                 ", start='" + start + '\'' +
                 ", stop='" + stop + '\'' +
                 ", channel='" + channel + '\'' +
                 ", title='" + title + '\'' +
                 '}';
     }
 
 
     public static List<Programme> getProgrammes(YboTvApplication application, Channel channel, Calendar calendarToday) {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
         Calendar calendarYesterday = (Calendar) calendarToday.clone();
         calendarYesterday.add(Calendar.DAY_OF_MONTH, -1);
         Calendar calendarTwomorrow = (Calendar) calendarToday.clone();
         calendarTwomorrow.add(Calendar.DAY_OF_MONTH, 1);
 
         Date today = calendarToday.getTime();
         Date yesterday  = calendarYesterday.getTime();
         Date twomorrow = calendarTwomorrow.getTime();
 
         String dateDebut;
         String dateFin;
 
         if (calendarToday.get(Calendar.HOUR_OF_DAY) < 3) {
             dateDebut = sdf.format(yesterday) + "030000";
             dateFin = sdf.format(today) + "030000";
         } else {
             dateDebut = sdf.format(today) + "030000";
             dateFin = sdf.format(twomorrow) + "030000";
         }
 
 
         YboTvDatabase database = application.getDatabase();
 
         StringBuilder sqlQuery = new StringBuilder();
 
         sqlQuery.append("SELECT ");
         sqlQuery.append("Programme.id as programmeId, ");
         sqlQuery.append("Programme.start as programmeStart, ");
         sqlQuery.append("Programme.stop as programmeStop, ");
         sqlQuery.append("Programme.icon as programmeIcon, ");
         sqlQuery.append("Programme.title as programmeTitle, ");
         sqlQuery.append("Programme.desc as programmeDesc, ");
         sqlQuery.append("Programme.starRating as programmeStarRating, ");
         sqlQuery.append("Programme.csaRating as programmeCsaRating, ");
         sqlQuery.append("Programme.directors as programmeDirectors, ");
         sqlQuery.append("Programme.actors as programmeActors, ");
         sqlQuery.append("Programme.writers as programmeWriters, ");
         sqlQuery.append("Programme.presenters as programmePresenters, ");
         sqlQuery.append("Programme.date as programmeDate, ");
         sqlQuery.append("Programme.categories as programmeCategories ");
 
         sqlQuery.append("FROM Programme ");
         sqlQuery.append("WHERE ");
         sqlQuery.append("Programme.channel = :channel ");
         sqlQuery.append("AND Programme.stop >= :beginDate ");
         sqlQuery.append("AND Programme.start <= :endDate ");
         sqlQuery.append("ORDER BY Programme.start ");
 
         List<String> selectionArgs = new ArrayList<String>(3);
 
         selectionArgs.add(channel.getId());
         selectionArgs.add(dateDebut);
         selectionArgs.add(dateFin);
 
         long startTime = System.nanoTime();
         Cursor cursor = database.executeSelectQuery(sqlQuery.toString(), selectionArgs);
 
         int nbResult = cursor.getCount();
         long elapsedTime = System.nanoTime() - startTime;
         Log.d("YboTv", "Requete executee : " + sqlQuery.toString());
         Log.d("YboTv", "Nombre de resultas : " + nbResult + " en " + (elapsedTime / 1000) + "µs");
 
         List<Programme> programmes = new ArrayList<Programme>();
 
         int programmeIdCol = cursor.getColumnIndex("programmeId");
         int programmeStartCol = cursor.getColumnIndex("programmeStart");
         int programmeStopCol = cursor.getColumnIndex("programmeStop");
         int programmeIconCol = cursor.getColumnIndex("programmeIcon");
         int programmeTitleCol = cursor.getColumnIndex("programmeTitle");
         int programmeDescCol = cursor.getColumnIndex("programmeDesc");
         int programmeStarRatingCol = cursor.getColumnIndex("programmeStarRating");
         int programmeCsaRatingCol = cursor.getColumnIndex("programmeCsaRating");
         int programmeDirectorsCol = cursor.getColumnIndex("programmeDirectors");
         int programmeActorsCol = cursor.getColumnIndex("programmeActors");
         int programmeWritersCol = cursor.getColumnIndex("programmeWriters");
         int programmePresentersCol = cursor.getColumnIndex("programmePresenters");
         int programmeDateCol = cursor.getColumnIndex("programmeDate");
         int programmeCategoriesCol = cursor.getColumnIndex("programmeCategories");
 
         while (cursor.moveToNext()) {
 
             Programme oneProgramme = new Programme();
             oneProgramme.setId(cursor.getString(programmeIdCol));
             oneProgramme.setStart(cursor.getString(programmeStartCol));
             oneProgramme.setStop(cursor.getString(programmeStopCol));
             oneProgramme.setIcon(cursor.getString(programmeIconCol));
             oneProgramme.setTitle(cursor.getString(programmeTitleCol));
             oneProgramme.setDesc(cursor.getString(programmeDescCol));
             oneProgramme.setStarRating(cursor.getString(programmeStarRatingCol));
             oneProgramme.setCsaRating(cursor.getString(programmeCsaRatingCol));
             oneProgramme.setChannel(channel.getId());
             oneProgramme.fillDirectorsWithDb(cursor.getString(programmeDirectorsCol));
             oneProgramme.fillActorsWithDb(cursor.getString(programmeActorsCol));
             oneProgramme.fillWritersWithDb(cursor.getString(programmeWritersCol));
             oneProgramme.fillPresentersWithDb(cursor.getString(programmePresentersCol));
             oneProgramme.setDate(cursor.getString(programmeDateCol));
             oneProgramme.fillCategoriesWithDb(cursor.getString(programmeCategoriesCol));
 
             programmes.add(oneProgramme);
         }
 
         cursor.close();
 
         return programmes;
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel parcel, int i) {
         parcel.writeString(id);
         parcel.writeString(start);
         parcel.writeString(stop);
         parcel.writeString(channel);
         parcel.writeString(icon);
         parcel.writeString(title);
         parcel.writeString(desc);
         parcel.writeString(starRating);
         parcel.writeString(csaRating);
         parcel.writeStringList(getDirectors());
         parcel.writeStringList(getActors());
         parcel.writeStringList(getWriters());
         parcel.writeStringList(getPresenters());
         parcel.writeString(date);
         parcel.writeStringList(getCategories());
     }
 
     public Programme() {
     }
 
     public Programme(Parcel in) {
         id = in.readString();
         start = in.readString();
         stop = in.readString();
         channel = in.readString();
         icon = in.readString();
         title = in.readString();
         desc = in.readString();
         starRating = in.readString();
         csaRating = in.readString();
         in.readStringList(getDirectors());
         in.readStringList(getActors());
         in.readStringList(getWriters());
         in.readStringList(getPresenters());
         date = in.readString();
         in.readStringList(getCategories());
     }
 
     public static final Creator<Programme> CREATOR = new Creator<Programme>() {
         @Override
         public Programme createFromParcel(Parcel parcel) {
             return new Programme(parcel);
         }
 
         @Override
         public Programme[] newArray(int size) {
             return new Programme[size];
         }
     };
 }
