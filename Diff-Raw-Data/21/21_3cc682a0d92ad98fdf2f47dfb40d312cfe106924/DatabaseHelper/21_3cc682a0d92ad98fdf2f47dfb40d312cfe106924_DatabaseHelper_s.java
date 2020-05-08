 package com.marwinxxii.ccardstats;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 
 import com.marwinxxii.ccardstats.helpers.DateHelper;
 import com.marwinxxii.ccardstats.notifications.SmsNotification;
 import com.marwinxxii.ccardstats.notifications.SmsNotificationReader;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
     private static final String DATABASE_NAME = "ccardstats";
 
     // field and table names
     public enum t {
         cards, name, alias, available, income, outcome, changes, card, diff, date,
         card_changes_monthly, month;
     }
 
     private static final String CARDS_QUERY = String.format(
             "create table %s (%s text, %s text, %s real, %s real, %s real)",
             t.cards, t.name, t.alias, t.available, t.income, t.outcome);
 
     private static final String CARD_CHANGES_QUERY = String.format(
             "create table %s (%s text, %s real, %s integer)", t.changes,
             t.card, t.diff, t.date);
 
     private static final String MONTHLY_QUERY = String.format(
             "create table %s (%s text, %s real, %s real, %s integer)",
             t.card_changes_monthly, t.card, t.income, t.outcome, t.month);
 
     private static final int VERSION = 1;
 
     private boolean mWasCreated = false;
     private SQLiteDatabase db;
 
     public DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         db.execSQL(CARDS_QUERY);
         db.execSQL(CARD_CHANGES_QUERY);
         db.execSQL(MONTHLY_QUERY);
         mWasCreated = true;
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
     }
 
     public boolean wasCreated() {
         return mWasCreated;
     }
 
     public void close() {
        db.close();
        db = null;
     }
 
     public void init() {
         if (db == null) {
             try {
                 db = getWritableDatabase();
             } catch (SQLiteException e) {
                 db = getReadableDatabase();
             }
         }
     }
 
     public long insertNotification(SmsNotification sms) {
         init();
         ContentValues values = new ContentValues();
         values.put(t.card.name(), sms.getCard());
         values.put(t.diff.name(), sms.getAmount());
         values.put(t.date.name(), sms.getDate());
         return db.insert(t.changes.name(), null, values);
     }
 
     public long insertCard(Card card) {
         init();
         ContentValues values = new ContentValues();
         values.put(t.name.name(), card.getName());
         values.put(t.alias.name(), card.getAlias());
         values.put(t.available.name(), card.getAvailable());
         values.put(t.income.name(), card.getIncome());
         values.put(t.outcome.name(), card.getOutcome());
         return db.insert(t.cards.name(), null, values);
     }
 
     public void updateCard(SmsNotification notif) {
         init();
         db.beginTransaction();
         String amountField = "%s=%s%c%f";
         if (notif.getAmount() > 0) {
             amountField = String.format(Locale.US, amountField, t.income, t.income,
                     '+', notif.getAmount());
         } else {
             amountField = String.format(Locale.US, amountField, t.outcome, t.outcome,
                     '-', -notif.getAmount());
         }
         String query = String.format(Locale.US,
                 "update %s set %s=%f, %s where %s='%s'", t.cards, t.available,
                 notif.getAvailable(), amountField, t.name, notif.getCard());
         db.execSQL(query);
         insertNotification(notif);
         db.endTransaction();
     }
 
     public ArrayList<Card> getCards() {
         init();
         Cursor cursor = db.query(t.cards.name(), null, null, null, null, null,
                 null);
         ArrayList<Card> result = new ArrayList<Card>();
 
         while (cursor.moveToNext()) {
             result.add(new Card(cursor.getString(0), cursor.getString(1),
                     cursor.getDouble(2), cursor.getDouble(3), cursor
                             .getDouble(4)));
         }
         cursor.close();
         return result;
     }
 
     public ArrayList<CardInfo> getCardsWithInfo() {
         init();
         Cursor cursor = db.rawQuery("select * from " + t.cards, null);
         ArrayList<CardInfo> result = new ArrayList<CardInfo>();
 
         String[] args = new String[1];
         while (cursor.moveToNext()) {
             Card c = new Card(cursor.getString(0), cursor.getString(1),
                     cursor.getDouble(2), cursor.getDouble(3),
                     cursor.getDouble(4));
             args[0] = c.getName();
             String baseQuery = String.format(
                     "select sum(%s) from %s where %s%%c0 and %s=?", t.diff,
                     t.changes, t.diff, t.card);
             String query = String.format(baseQuery, '>');
             boolean empty = true;
             double monthIn = 0.0, monthOut = 0.0;
             Cursor cursor2 = db.rawQuery(query, args);
             if (cursor2.moveToNext()) {
                 monthIn = cursor2.getDouble(0);
                 empty = false;
             }
             cursor2.close();
             query = String.format(baseQuery, '<');
             cursor2 = db.rawQuery(query, args);
             if (!cursor2.moveToNext() && empty) {
                 result.add(new CardInfo(c, 0.0, 0.0, 0.0, 0.0));
                 cursor2.close();
                 continue;
             }
             monthOut = cursor2.getDouble(0);
             cursor2.close();
             double todayIn = 0.0, todayOut = 0.0;
             baseQuery = String.format("%s and %s>=? and %s<?", baseQuery,
                     t.date, t.date);
             args = new String[] { c.getName(),
                     String.valueOf(DateHelper.getToday()),
                     String.valueOf(DateHelper.getTomorrow()) };
             query = String.format(baseQuery, '>');
             cursor2 = db.rawQuery(query, args);
             if (cursor2.moveToNext())
                 todayIn = cursor2.getDouble(0);
             query = String.format(baseQuery, '<');
             cursor2.close();
             cursor2 = db.rawQuery(query, args);
             if (cursor2.moveToNext())
                 todayOut = cursor2.getDouble(0);
             cursor2.close();
             result.add(new CardInfo(c, monthIn, monthOut, todayIn, todayOut));
         }
         cursor.close();
         return result;
     }
 
    public double getMonth(String card, boolean income) {
        init();
        String query;
        if (income) {
            query = "select sum(diff) where diff>0 and card=?";
        } else {
            query = "select sum(diff) where diff<0 and card=?";
        }
        Cursor cursor = db.rawQuery(query, new String[] { card });
        double result = cursor.getDouble(0);
        cursor.close();
        return result;
    }

     public Collection<CardInfo> readCardInfoFromSMS(SmsNotificationReader reader) {
         SmsNotification notif;
         HashMap<String, CardInfo> cards = new HashMap<String, CardInfo>();
         while ((notif = reader.next()) != null) {
             CardInfo ci = cards.get(notif.getCard());
             Card card;
             if (ci == null) {
                 // check that available > 0 ?
                 card = new Card(notif.getCard());
                 ci = new CardInfo(card, 0, 0, 0, 0);
                 cards.put(notif.getCard(), ci);
             } else {
                 card = ci.card;
             }
             double diff = notif.getAmount();
             card.addMoney(diff);
             card.available = notif.getAvailable();
             if (DateHelper.isInCurrentMonth(notif.getDate())) {
                 insertNotification(notif);
                 ci.addMonthMoney(diff);
             }
             if (DateHelper.isToday(notif.getDate()))
                 ci.addTodayMoney(diff);
         }
         for (CardInfo ci : cards.values()) {
             insertCard(ci.card);
         }
         return cards.values();
     }
 
 }
