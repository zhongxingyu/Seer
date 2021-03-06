 package com.brainydroid.daydreaming.db;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import com.brainydroid.daydreaming.background.Logger;
 import com.brainydroid.daydreaming.background.StatusManager;
 import com.google.gson.JsonSyntaxException;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 @Singleton
 public class QuestionsStorage {
 
     private static String TAG = "QuestionsStorage";
 
     public static final String COL_NAME = "questionName";
     public static final String COL_CATEGORY = "questionCategory";
     public static final String COL_SUB_CATEGORY = "questionSubCategory";
     public static final String COL_DETAILS = "questionDetails";
     public static final String COL_SLOT = "questionSlot";
 
     public static final String COL_STATUS = "questionStatus";
     public static final String COL_ANSWER = "questionAnswer";
     public static final String COL_LOCATION = "questionLocation";
     public static final String COL_NTP_TIMESTAMP = "questionNtpTimestamp";
     public static final String COL_SYSTEM_TIMESTAMP =
             "questionSystemTimestamp";
 
     private static String TABLE_QUESTIONS = "Questions";
 
     private static final String SQL_CREATE_TABLE_QUESTIONS =
             "CREATE TABLE IF NOT EXISTS {}" + TABLE_QUESTIONS + " (" +
                     COL_NAME + " TEXT NOT NULL, " +
                     COL_CATEGORY + " TEXT NOT NULL, " +
                     COL_SUB_CATEGORY + " TEXT, " +
                     COL_DETAILS + " TEXT NOT NULL, " +
                     COL_SLOT + " TEXT NOT NULL" +
                     ");";
 
     @Inject Json json;
     @Inject QuestionFactory questionFactory;
     @Inject ProfileStorage profileStorage;
     @Inject SlottedQuestionsFactory slottedQuestionsFactory;
     @Inject StatusManager statusManager;
 
     private final SQLiteDatabase db;
 
     @Inject
     public QuestionsStorage(Storage storage, StatusManager statusManager) {
         Logger.d(TAG, "{} - Building QuestionsStorage: creating table if it " +
                 "doesn't exist", statusManager.getCurrentModeName());
 
         db = storage.getWritableDatabase();
        for (String modeName : StatusManager.AVAILABLE_MODE_NAMES) {
            db.execSQL(MessageFormat.format(SQL_CREATE_TABLE_QUESTIONS, modeName));
        }
     }
 
     private synchronized void setQuestionsVersion(int questionsVersion) {
         Logger.d(TAG, "{} - Setting questions version to {}", statusManager.getCurrentModeName(), questionsVersion);
         profileStorage.setQuestionsVersion(questionsVersion);
     }
 
     private synchronized void setNSlotsPerPoll(int nSlotsPerPoll) {
         Logger.d(TAG, "{} - Setting nSlotsPerPoll to {}",statusManager.getCurrentModeName(),  nSlotsPerPoll);
         statusManager.setNSlotsPerPoll(nSlotsPerPoll);
     }
 
     public synchronized int getNSlotsPerPoll() {
         return statusManager.getNSlotsPerPoll();
     }
 
     // get question from id in db
     public synchronized Question create(String questionName) {
         Logger.d(TAG, "{} - Retrieving question {} from db", statusManager.getCurrentModeName(), questionName);
 
         Cursor res = db.query(statusManager.getCurrentModeName() + TABLE_QUESTIONS, null,
                 COL_NAME + "=?", new String[]{questionName},
                 null, null, null);
         if (!res.moveToFirst()) {
             res.close();
             return null;
         }
 
         Question q = questionFactory.create();
         q.setName(res.getString(res.getColumnIndex(COL_NAME)));
         q.setCategory(res.getString(res.getColumnIndex(COL_CATEGORY)));
         q.setSubCategory(res.getString(
                 res.getColumnIndex(COL_SUB_CATEGORY)));
         q.setDetailsFromJson(res.getString(
                 res.getColumnIndex(COL_DETAILS)));
         q.setSlot(res.getString(res.getColumnIndex(COL_SLOT)));
         res.close();
 
         return q;
     }
 
     public synchronized SlottedQuestions getSlottedQuestions() {
         Logger.d(TAG, "{} - Retrieving all questions from db", statusManager.getCurrentModeName());
 
         Cursor res = db.query(statusManager.getCurrentModeName() + TABLE_QUESTIONS, null, null, null, null, null,
                 null);
         if (!res.moveToFirst()) {
             res.close();
             return null;
         }
 
         SlottedQuestions slottedQuestions = slottedQuestionsFactory.create();
         do {
             Question q = questionFactory.create();
             q.setName(res.getString(res.getColumnIndex(COL_NAME)));
             q.setCategory(res.getString(res.getColumnIndex(COL_CATEGORY)));
             q.setSubCategory(res.getString(
                     res.getColumnIndex(COL_SUB_CATEGORY)));
             q.setDetailsFromJson(res.getString(
                     res.getColumnIndex(COL_DETAILS)));
             q.setSlot(res.getString(res.getColumnIndex(COL_SLOT)));
 
             slottedQuestions.add(q);
         } while (res.moveToNext());
         res.close();
 
         return slottedQuestions;
     }
 
     public synchronized void flush() {
         Logger.d(TAG, "{} - Flushing questions from db", statusManager.getCurrentModeName());
         db.delete(statusManager.getCurrentModeName() + TABLE_QUESTIONS, null, null);
     }
 
     private synchronized void add(ArrayList<Question> questions) {
         Logger.d(TAG, "{} - Storing an array of questions to db", statusManager.getCurrentModeName());
         for (Question q : questions) {
             add(q);
         }
     }
 
     // add question in database
     private synchronized void add(Question question) {
         Logger.d(TAG, "{} - Storing question {} to db", statusManager.getCurrentModeName(), question.getName());
         db.insert(statusManager.getCurrentModeName() + TABLE_QUESTIONS, null, getQuestionValues(question));
     }
 
     private synchronized ContentValues getQuestionValues(Question question) {
         Logger.d(TAG, "{} - Building question values", statusManager.getCurrentModeName());
 
         ContentValues qValues = new ContentValues();
         qValues.put(COL_NAME, question.getName());
         qValues.put(COL_CATEGORY, question.getCategory());
         qValues.put(COL_SUB_CATEGORY,
                 question.getSubCategory());
         qValues.put(COL_DETAILS, question.getDetailsAsJson());
         qValues.put(COL_SLOT, question.getSlot());
         return qValues;
     }
 
     // import questions from json file into database
     public synchronized void importQuestions(String jsonQuestionsString)
             throws QuestionsSyntaxException {
         Logger.d(TAG, "{} - Importing questions from JSON", statusManager.getCurrentModeName());
 
         try {
             ServerQuestionsJson serverQuestionsJson = json.fromJson(
                     jsonQuestionsString, ServerQuestionsJson.class);
 
             // Check nSlotsPerPoll is set
             int nSlotsPerPoll = serverQuestionsJson.getNSlotsPerPoll();
             if (nSlotsPerPoll == -1) {
                 throw new JsonSyntaxException("nSlotsPerPoll can't be -1");
             }
 
             // Get all question slots and check there are at least as many as
             // nSlotsPerPoll
             HashSet<String> slots = new HashSet<String>();
             for (Question q : serverQuestionsJson.getQuestionsArrayList()) {
                 slots.add(q.getSlot());
             }
             if (slots.size() < nSlotsPerPoll) {
                 throw new JsonSyntaxException("There must be at least as many" +
                         " slots defined in the questions as nSlotsPerPoll");
             }
 
             // All is good, do the real import
             flush();
             setQuestionsVersion(serverQuestionsJson.getVersion());
             setNSlotsPerPoll(nSlotsPerPoll);
             add(serverQuestionsJson.getQuestionsArrayList());
         } catch (JsonSyntaxException e) {
             throw new QuestionsSyntaxException();
         }
     }
 
 }
