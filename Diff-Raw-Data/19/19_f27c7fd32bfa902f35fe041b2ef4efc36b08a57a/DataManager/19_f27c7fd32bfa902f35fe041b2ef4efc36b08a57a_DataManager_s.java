 package com.appspot.manup.signup.data;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.appspot.manup.signup.ldap.MemberLdapEntry;
 
 public final class DataManager
 {
     private static final String TAG = DataManager.class.getSimpleName();
 
     public static final long OPERATION_FAILED = -1L;
 
     private static final String SIGNATURE_FILE_EXT = ".png";
 
     public interface OnChangeListener
     {
         void onChange(DataManager dataManager);
     } // interface OnChangeListener
 
     public static final class Member implements BaseColumns
     {
         private static final String TABLE_NAME = "member";
 
         public static final String PERSON_ID = "person_id";
         public static final String PERSON_ID_VALIDATED = "person_id_validated";
         public static final String LATEST_PENDING_SIGNATURE_REQUEST =
                 "latest_pending_signature_request";
         public static final String EXTRA_INFO_STATE = "extra_info_state";
         public static final String GIVEN_NAME = "given_name";
         public static final String SURNAME = "surname";
         public static final String EMAIL = "email";
         public static final String MEMBER_TYPE = "member_type";
         public static final String DEPARTMENT = "department";
         public static final String SIGNATURE_STATE = "signature_state";
 
         public static final int PERSON_ID_LENGTH = 7;
 
         public static final String SIGNATURE_STATE_UNCAPTURED = "uncaptured";
         public static final String SIGNATURE_STATE_CAPTURED = "captured";
         public static final String SIGNATURE_STATE_UPLOADED = "uploaded";
 
         public static final String PERSON_ID_VALIDATED_UNKNWON = "unknown";
         public static final String PERSON_ID_VALIDATED_VALID = "valid";
         public static final String PERSON_ID_VALIDATED_INVALID = "invalid";
 
         public static final String EXTRA_INFO_STATE_NONE = "none";
         public static final String EXTRA_INFO_STATE_RETRIEVING = "retrieving";
         public static final String EXTRA_INFO_STATE_RETRIEVED = "retrieved";
         public static final String EXTRA_INFO_STATE_ERROR = "error";
 
         private Member()
         {
             super();
             throw new AssertionError();
         } // constructor()
 
     } // class Member
 
     private static final class MemberDbOpenHelper extends SQLiteOpenHelper
     {
         private static final String DATABASE_NAME = "members.db";
         private static final int DATABASE_VERSION = 13;
 
         public MemberDbOpenHelper(final Context context)
         {
             super(context, DATABASE_NAME, null /* cursor factory */, DATABASE_VERSION);
         } // constructor(Context)
 
         @Override
         public void onCreate(final SQLiteDatabase db)
         {
             //@formatter:off
 
             final String createMemberTableSql =
 
             "CREATE TABLE " + Member.TABLE_NAME + "(" +
 
                 Member._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
 
                 Member.PERSON_ID + " TEXT NOT NULL UNIQUE "
                     + "CHECK (length(" + Member.PERSON_ID + ")=" + Member.PERSON_ID_LENGTH + ")," +
 
                 Member.LATEST_PENDING_SIGNATURE_REQUEST + " INTEGER," +
 
                 Member.PERSON_ID_VALIDATED + " TEXT NOT NULL "
                     + "DEFAULT " + Member.PERSON_ID_VALIDATED_UNKNWON + "," +
 
                 Member.EXTRA_INFO_STATE + " TEXT NOT NULL "
                     + "DEFAULT " + Member.EXTRA_INFO_STATE_NONE + "," +
 
                 Member.GIVEN_NAME + " TEXT," +
                 Member.SURNAME + " TEXT," +
                 Member.EMAIL+ " TEXT," +
                 Member.MEMBER_TYPE + " TEXT," +
                 Member.DEPARTMENT + " TEXT," +
 
                 Member.SIGNATURE_STATE + " TEXT NOT NULL "
                     + "DEFAULT " + Member.SIGNATURE_STATE_UNCAPTURED +
             ")";
 
             //@formatter:on
 
             Log.v(TAG, createMemberTableSql);
 
             db.execSQL(createMemberTableSql);
         } // onCreate
 
         @Override
         public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
         {
             db.execSQL("DROP TABLE IF EXISTS " + Member.TABLE_NAME);
             onCreate(db);
         } // onUpgrade(SQLiteDatabase, int, int)
 
     } // class MemberDbOpenHelper
 
     private static DataManager sSingleton = null;
 
     public static synchronized DataManager getDataManager(final Context context)
     {
         if (sSingleton == null)
         {
             sSingleton = new DataManager(context);
         } // if
         return sSingleton;
     } // getDataManager(Context)
 
     public static long getUnixTime()
     {
         return System.currentTimeMillis() / 1000L;
     } // getUnixTime()
 
     public static boolean isValidPersonId(final String possiblePersonId)
     {
         return possiblePersonId.length() == Member.PERSON_ID_LENGTH
                 && TextUtils.isDigitsOnly(possiblePersonId);
     } // isValidPersonId(String)
 
     private final Object mLock = new Object();
     private final Set<OnChangeListener> mListeners = new HashSet<OnChangeListener>();
     private final Context mContext;
     private SQLiteDatabase mDb = null;
 
     private DataManager(final Context context)
     {
         super();
         mContext = context.getApplicationContext();
     } // DataManager(Context)
 
     private SQLiteDatabase getDb()
     {
         synchronized (mLock)
         {
             if (mDb == null)
             {
                 mDb = new MemberDbOpenHelper(mContext).getWritableDatabase();
             } // if
         } // synchronized
         return mDb;
     } // getDb()
 
     public void registerListener(final OnChangeListener listener)
     {
         synchronized (mListeners)
         {
             mListeners.add(listener);
         } // synchronized
     } // registerListener(OnChangeListener)
 
     public void unregisterListener(final OnChangeListener listener)
     {
         synchronized (mListeners)
         {
             mListeners.remove(listener);
         } // synchronized
     } // unregisterListener(OnChangeListener)
 
     private void notifyListeners()
     {
         synchronized (mListeners)
         {
             for (final OnChangeListener listener : mListeners)
             {
                 listener.onChange(this);
             } // for
         } // synchronized
     } // notifyListeners()
 
     /**
      * Add a new member. Adding a member also implies a request for a signature.
      *
      * @param personId
      *            the person ID of the new member
      * @return the internal ID of the newly added member, or
      *         {@link #OPERATION_FAILED} if it was not possible to add the
      *         member.
      */
     public long addMember(final String personId)
     {
         final ContentValues memberValues = new ContentValues(1);
         memberValues.put(Member.PERSON_ID, personId);
         memberValues.put(Member.LATEST_PENDING_SIGNATURE_REQUEST, getUnixTime());
         final long id;
         try
         {
             id = getDb().insert(
                     Member.TABLE_NAME,
                     Member.PERSON_ID /* null column hack */,
                     memberValues);
         } // try
         catch (final SQLiteConstraintException e)
         {
             Log.d(TAG, "addMember(" + personId + ") failed", e);
             return OPERATION_FAILED;
         } // catch
         if (id != OPERATION_FAILED)
         {
             notifyListeners();
         } // if
         return id;
     } // addMember(String)
 
     /**
      * Get members without extra info. Also excludes members with invalid person
      * IDs as it will not be possible to obtain extra information for them.
      *
      * @return a cursor containing members without extra info, or null it was
      *         not possible to retrieve those members.
      */
     public Cursor getMembersWithoutExtraInfo()
     {
         return getDb().query(
                 Member.TABLE_NAME,
                 null /* all columns */,
                 Member.EXTRA_INFO_STATE + "!=? AND " + Member.PERSON_ID_VALIDATED + "!=?",
                 new String[] {
                         Member.EXTRA_INFO_STATE_RETRIEVED,
                         Member.PERSON_ID_VALIDATED_INVALID },
                 null /* group by */,
                 null /* having */,
                 null /* order by */);
     } // getMembersWithoutExtraInfo()
 
     public boolean setExtraInfoStateToRetrieving(final long id)
     {
         return setExtraInfoState(id, Member.EXTRA_INFO_STATE_RETRIEVING);
     } // setExtraInfoStateToRetrieving(long)
 
     public boolean setExtraInfoStateToError(final long id)
     {
         return setExtraInfoState(id, Member.EXTRA_INFO_STATE_ERROR);
     } // setExtraInfoState(long)
 
     private boolean setExtraInfoState(final long id, final String newState)
     {
         final ContentValues memberInfoStateValues = new ContentValues(1);
         memberInfoStateValues.put(Member.EXTRA_INFO_STATE, newState);
         return updateMember(id, memberInfoStateValues);
     } // setExtraInfoState(String)
 
     public boolean addExtraInfo(final MemberLdapEntry memberEntry)
     {
         final long id = getId(memberEntry.getPersonId());
         if (id == OPERATION_FAILED)
         {
             Log.e(TAG, "Failed to get internal ID of the member " + memberEntry.getPersonId());
             return false;
         } // if
 
         final ContentValues extraValues = memberEntry.getContentValues();
         Log.v(TAG, "Adding extra info for " + memberEntry.getPersonId() + ": "
                 + extraValues.toString());
 
         // As extra information was retrieved, the person ID must be valid.
         extraValues.put(Member.PERSON_ID_VALIDATED, Member.PERSON_ID_VALIDATED_VALID);
         extraValues.put(Member.EXTRA_INFO_STATE, Member.EXTRA_INFO_STATE_RETRIEVED);
         final boolean addedextraInfo = updateMember(id, extraValues);
 
         if (addedextraInfo)
         {
             notifyListeners();
         } // if
 
         return addedextraInfo;
     } // addExtraInfo(MemberLdapEntry)
 
     public boolean requestSignature(final String personId)
     {
         final SQLiteDatabase db = getDb();
         boolean requestMade = false;
         db.beginTransaction();
         try
         {
             long id = getId(personId);
             if (id == OPERATION_FAILED)
             {
                return addMember(personId) == OPERATION_FAILED;
             } // if
            final ContentValues requestValues = new ContentValues(1);
            requestValues.put(Member.LATEST_PENDING_SIGNATURE_REQUEST, getUnixTime());
            requestMade = updateMember(id, requestValues);
             if (requestMade)
             {
                 db.setTransactionSuccessful();
             } // if
         } // try
         finally
         {
             db.endTransaction();
         } // finally
         if (requestMade)
         {
             notifyListeners();
         } // if
         return requestMade;
     } // requestSignature(String)
 
     public Cursor getSignatureRequests(final long sinceUnixTime)
     {
         return getDb().query(
                 Member.TABLE_NAME,
                 null /* all columns */,
                 Member.LATEST_PENDING_SIGNATURE_REQUEST + ">=?",
                 new String[] { Long.toString(sinceUnixTime) },
                 null /* group by */,
                 null /* having */,
                 Member.LATEST_PENDING_SIGNATURE_REQUEST + " DESC");
     } // getSignatureRequests(long)
 
     public boolean memberHasSignature(final long id)
     {
         return Member.SIGNATURE_STATE_CAPTURED.equals(
                 getMemberField(Member._ID, Long.toString(id), Member.SIGNATURE_STATE));
     } // memberHasSignature(long)
 
     public Cursor getMembersWithPendingRequests()
     {
         return getDb().query(
                 Member.TABLE_NAME,
                 null /* all columns */,
                 Member.LATEST_PENDING_SIGNATURE_REQUEST + " IS NOT NULL",
                 null /* selection args */,
                 null /* groupBy */,
                 null /* having */,
                 Member.LATEST_PENDING_SIGNATURE_REQUEST + " DESC");
     } // getMembers()
 
     public Cursor getMembersWithSignaturesAndNoRequests()
     {
         return getDb().query(
                 Member.TABLE_NAME,
                 null /* columns */,
                 Member.SIGNATURE_STATE + "=? AND "
                         + Member.LATEST_PENDING_SIGNATURE_REQUEST + " IS NULL",
                 new String[] { Member.SIGNATURE_STATE_CAPTURED },
                 null /* group by */,
                 null /* having */,
                 null /* order by */);
     } // getMembersWithSignaturesAndNoRequests()
 
     public Cursor getMembersWithUploadedSignaturesAndNoRequests()
     {
         return getDb().query(
                 Member.TABLE_NAME,
                 null /* columns */,
                 Member.SIGNATURE_STATE + "=? AND "
                         + Member.LATEST_PENDING_SIGNATURE_REQUEST + " IS NULL",
                 new String[] { Member.SIGNATURE_STATE_UPLOADED },
                 null /* group by */,
                 null /* having */,
                 null /* order by */);
     } // getMembersWithSignaturesAndNoRequests()
 
     public Cursor getMembersWithSignatures()
     {
         return getMembersWithSignaturesInState(Member.SIGNATURE_STATE_CAPTURED);
     } // getMembersWithCapturedSignatures
 
     private Cursor getMembersWithSignaturesInState(final String signatureState)
     {
         return getDb().query(
                 Member.TABLE_NAME,
                 null /* columns */,
                 Member.SIGNATURE_STATE + "=?",
                 new String[] { signatureState },
                 null /* group by */,
                 null /* having */,
                 null /* order by */);
     } // getMembersWithSignaturesInState(String)
 
     public boolean setSignatureCaptured(final long id)
     {
         final boolean updated = updateSignatureState(id, Member.SIGNATURE_STATE_CAPTURED);
         if (updated)
         {
             notifyListeners();
         } // if
         return updated;
     } // setSignatureCaptured(long)
 
     public boolean setSignatureUploaded(final long id)
     {
         final boolean stateChanged = updateSignatureState(id, Member.SIGNATURE_STATE_UPLOADED);
         if (stateChanged)
         {
             if (deleteMemberSignature(id))
             {
                 notifyListeners();
             } // if
         } // if
         return stateChanged;
     } // setSignatureUploaded(long)
 
     private boolean updateSignatureState(final long id, final String newState)
     {
         final ContentValues newStateValue = new ContentValues(2);
         newStateValue.put(Member.SIGNATURE_STATE, newState);
         if (Member.SIGNATURE_STATE_CAPTURED.equals(newState))
         {
             newStateValue.put(Member.LATEST_PENDING_SIGNATURE_REQUEST, (String) null);
         } // if
         return updateMember(id, newStateValue);
     } // updateSignatureState(long, String)
 
     public boolean setPersonIdInvalid(final long id)
     {
         final ContentValues newPersonIdValidValue = new ContentValues(1);
         newPersonIdValidValue.put(Member.PERSON_ID_VALIDATED, Member.PERSON_ID_VALIDATED_INVALID);
         return updateMember(id, newPersonIdValidValue);
     } // setPersonIdInvalid(long)
 
     public int flushMembers()
     {
         return getDb().delete(Member.TABLE_NAME, null /* whereClause */, null /* whereArgs */);
     } // flushMembers
 
     public String getPersonId(final long id)
     {
         return getMemberField(Member._ID, Long.toString(id), Member.PERSON_ID);
     } // getPersonId(long)
 
     private long getId(final String personId)
     {
         final String id = getMemberField(Member.PERSON_ID, personId, Member._ID);
         return id != null ? Long.parseLong(id) : OPERATION_FAILED;
     } // getId(String)
 
     private String getMemberField(final String uniqueColumn, final String uniqueValue,
             final String returnColumn)
     {
         Cursor c = null;
         try
         {
             c = getDb().query(
                     Member.TABLE_NAME,
                     new String[] { returnColumn },
                     uniqueColumn + "=?",
                     new String[] { uniqueValue },
                     null /* group by */,
                     null /* having */,
                     null /* order by */);
             if (c != null && c.moveToFirst())
             {
                 return c.getString(0);
             } // if
             return null;
         } // try
         finally
         {
             if (c != null)
             {
                 c.close();
             } // if
         } // finally
     } // getMemberField(String, String, String)
 
     private boolean updateMember(final long id, final ContentValues updatedMemberValues)
     {
         final boolean updated = getDb().update(
                 Member.TABLE_NAME,
                 updatedMemberValues,
                 Member._ID + "=?",
                 new String[] { Long.toString(id) }) == 1;
         if (updated)
         {
             notifyListeners();
         } // if
         return updated;
     } // updateMember
 
     private boolean deleteMemberSignature(final long id)
     {
         final File signature = getSignatureFile(id);
         if (signature == null)
         {
             return false;
         } // if
 
         return (!signature.exists() || signature.delete());
     } // deleteMemberSignature
 
     public File getSignatureFile(final long id)
     {
         final File externalDir = mContext.getExternalFilesDir(null /* type */);
         if (externalDir == null)
         {
             return null;
         } // if
         return new File(externalDir, Long.toString(id) + SIGNATURE_FILE_EXT);
     } // getSignatureFile
 
     public long loadTestData()
     {
         flushMembers();
         final ContentValues[] cvs = TestData.getMembers();
         final SQLiteDatabase db = getDb();
         for (final ContentValues cv : cvs)
         {
             if (db.insert(Member.TABLE_NAME, null /* null column hack */, cv) == OPERATION_FAILED)
             {
                 Log.e(TAG, "Failed to insert test data. " + cv);
             } // if
         } // for
         DatabaseUtils.dumpCursor(db.query(Member.TABLE_NAME, null, null, null, null, null, null));
         return cvs.length;
     } // loadTestData()
 } // class DataManager
