 package com.imaginea.android.sugarcrm.sync;
 
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SyncResult;
 import android.database.Cursor;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.imaginea.android.sugarcrm.ModuleFields;
 import com.imaginea.android.sugarcrm.R;
 import com.imaginea.android.sugarcrm.provider.DatabaseHelper;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ACLActions;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ACLRoles;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Contacts;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Sync;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Users;
 import com.imaginea.android.sugarcrm.util.Module;
 import com.imaginea.android.sugarcrm.util.RelationshipStatus;
 import com.imaginea.android.sugarcrm.util.RestUtil;
 import com.imaginea.android.sugarcrm.util.SugarBean;
 import com.imaginea.android.sugarcrm.util.SugarCrmException;
 import com.imaginea.android.sugarcrm.util.Util;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 /**
  * Class for managing sugar crm sync related mOperations. should be capable of updating the
  * SyncStats in SyncResult object.
  * <ul>
  * <li>Syncs Modules</li>
  * <li>Syncs AclAccess data</li>
  * <li>Syncs Module Data and Relationship Data</li>
  * </ul>
  * 
  * //TODO - handling statistics of SyncResult, Merge conflicts
  */
 public class SugarSyncManager {
 
     public static int mTotalRecords;
 
     private static DatabaseHelper databaseHelper;
 
     private static String mSelection = SugarCRMContent.SUGAR_BEAN_ID + "=?";
 
     private static String mBeanIdField = Contacts.BEAN_ID;
 
     private static String mQuery = "";
 
     // for every module, linkName to field Array is retrieved from db cache and then cleared
     private static Map<String, List<String>> mLinkNameToFieldsArray = new HashMap<String, List<String>>();
 
     /**
      * make the date formatter static as we are synchronized even though its not thread-safe
      */
     private static DateFormat mDateFormat;
 
     private static final String LOG_TAG = SugarSyncManager.class.getSimpleName();
 
     /**
      * Synchronize raw contacts
      * 
      * @param context
      *            The context of Authenticator Activity
      * @param account
      *            The username for the account
      * @param sessionId
      *            The session Id associated with sugarcrm session
      * @param moduleName
      *            The name of the module to sync
      * @param syncResult
      *            a {@link android.content.SyncResult} object.
      * @throws com.imaginea.android.sugarcrm.util.SugarCrmException
      *             if any.
      */
     public static synchronized void syncModulesData(Context context, String account,
                                     String sessionId, String moduleName, SyncResult syncResult)
                                     throws SugarCrmException {
         long rawId = 0;
         final ContentResolver resolver = context.getContentResolver();
         final BatchOperation batchOperation = new BatchOperation(resolver);
         if (databaseHelper == null)
             databaseHelper = new DatabaseHelper(context);
         int offset = 0;
         int maxResults = 20;
         String deleted = "";
         SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
         // TODO use a constant and remove this as we start from the login screen
         String url = pref.getString(Util.PREF_REST_URL, context.getString(R.string.defaultUrl));
 
         String[] projections = databaseHelper.getModuleProjections(moduleName);
         String orderBy = databaseHelper.getModuleSortOrder(moduleName);
         setLinkNameToFieldsArray(moduleName);
 
         // TODO - Fetching based on dates
         if (mDateFormat == null)
             mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         // mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 
         Date startDate = new Date();
         startDate.setMonth(startDate.getMonth() - 1);
         long time = pref.getLong(Util.PREF_SYNC_START_TIME, startDate.getTime());
         startDate.setTime(time);
 
         Date endDate = new Date();
         endDate.setTime(System.currentTimeMillis());
         time = pref.getLong(Util.PREF_SYNC_END_TIME, endDate.getTime());
         endDate.setTime(time);
 
         mQuery = moduleName + "." + ModuleFields.DATE_MODIFIED + ">'"
                                         + mDateFormat.format(startDate) + "' AND " + moduleName
                                         + "." + ModuleFields.DATE_MODIFIED + "<='"
                                         + mDateFormat.format(endDate) + "'";
 
         while (true) {
             if (projections == null || projections.length == 0)
                 break;
 
             SugarBean[] sBeans = RestUtil.getEntryList(url, sessionId, moduleName, mQuery, orderBy, ""
                                             + offset, projections, mLinkNameToFieldsArray, ""
                                             + maxResults, deleted);
             if (Log.isLoggable(LOG_TAG, Log.DEBUG))
                 Log.d(LOG_TAG, "fetching " + offset + "to " + (offset + maxResults));
             if (sBeans == null || sBeans.length == 0)
                 break;
             if (Log.isLoggable(LOG_TAG, Log.DEBUG))
                 Log.d(LOG_TAG, "In Syncmanager");
 
             for (SugarBean sBean : sBeans) {
 
                 String beandIdValue = sBean.getFieldValue(mBeanIdField);
                 // Check to see if the contact needs to be inserted or updated
                 rawId = lookupRawId(resolver, moduleName, beandIdValue);
                 if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
                     Log.v(LOG_TAG, "beanId/rawid:" + beandIdValue + "/" + rawId);
 
                 String name = TextUtils.isEmpty(sBean.getFieldValue(ModuleFields.NAME)) ? sBean.getFieldValue(ModuleFields.FIRST_NAME)
                                                 : sBean.getFieldValue(ModuleFields.NAME);
 
                 if (rawId != 0) {
                     if (!sBean.getFieldValue(ModuleFields.DELETED).equals(Util.DELETED_ITEM)) {
                         Log.i(LOG_TAG, "updating... " + moduleName + ": " + rawId + ") " + name);
                         updateModuleItem(context, resolver, account, moduleName, sBean, rawId, batchOperation);
                     } else {
                         // delete module item - never delete the item here, just update the deleted
                         // flag
                         Log.i(LOG_TAG, "deleting... " + moduleName + ": " + rawId + ") " + name);
                         deleteModuleItem(context, rawId, moduleName, batchOperation);
                     }
                 } else {
                     // add new moduleItem
                     // Log.v(LOG_TAG, "In addModuleItem");
                     if (!sBean.getFieldValue(ModuleFields.DELETED).equals(Util.DELETED_ITEM)) {
                         Log.i(LOG_TAG, "inserting... " + moduleName + ": " + " " + name);
                         addModuleItem(context, account, sBean, moduleName, batchOperation);
                     }
                 }
                 // syncRelationships(context, account, sessionId, moduleName, sBean,
                 // batchOperation);
                 // A sync adapter should batch operations on multiple contacts,
                 // because it will make a dramatic performance difference.
                 if (batchOperation.size() >= 50) {
                     batchOperation.execute();
                 }
             }
             batchOperation.execute();
             offset = offset + maxResults;
             for (SugarBean sBean : sBeans) {
                 syncRelationshipsData(context, account, sessionId, moduleName, sBean, batchOperation);
                 // A sync adapter should batch operations on multiple contacts,
                 // because it will make a dramatic performance difference.
                 if (batchOperation.size() >= 50) {
                     batchOperation.execute();
                 }
             }
             batchOperation.execute();
 
         }
         mLinkNameToFieldsArray.clear();
         // syncRelationships(context, account, sessionId, moduleName);
         databaseHelper.close();
         databaseHelper = null;
     }
 
     /**
      * syncRelationships
      * 
      * @param context
      *            a {@link android.content.Context} object.
      * @param account
      *            a {@link java.lang.String} object.
      * @param sessionId
      *            a {@link java.lang.String} object.
      * @param moduleName
      *            a {@link java.lang.String} object.
      * @param bean
      *            a {@link com.imaginea.android.sugarcrm.util.SugarBean} object.
      * @param batchOperation
      *            a {@link com.imaginea.android.sugarcrm.sync.BatchOperation} object.
      * @throws com.imaginea.android.sugarcrm.util.SugarCrmException
      *             if any.
      */
     public static void syncRelationshipsData(Context context, String account, String sessionId,
                                     String moduleName, SugarBean bean, BatchOperation batchOperation)
                                     throws SugarCrmException {
         String[] relationships = databaseHelper.getModuleRelationshipItems(moduleName);
         if (relationships == null) {
             if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
                 Log.v(LOG_TAG, "relationships is null");
             return;
         }
         String beandIdValue = bean.getFieldValue(mBeanIdField);
         if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
             Log.v(LOG_TAG, "syncRelationshipsData: beanId:" + beandIdValue);
 
         long rawId = lookupRawId(context.getContentResolver(), moduleName, beandIdValue);
         if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
             Log.v(LOG_TAG, "syncRelationshipsData: RawId:" + rawId);
 
         for (String relation : relationships) {
             String linkFieldName = databaseHelper.getLinkfieldName(relation);
             // for a particular module-link field name
             SugarBean[] relationshipBeans = bean.getRelationshipBeans(linkFieldName);
             // Log.v(LOG_TAG, "linkFieldName:" + linkFieldName);
             if (relationshipBeans == null || relationshipBeans.length == 0) {
                 if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
                     Log.v(LOG_TAG, "relationship beans is null or empty");
                 continue;
             }
 
             for (SugarBean relationbean : relationshipBeans) {
                 // long relationRawId = lookupRawId(context.getContentResolver(), relation,
                 // relationbean.getBeanId());
                 String relationBeanId = relationbean.getFieldValue(mBeanIdField);
 
                 if (relationBeanId == null)
                     continue;
 
                 long relationRawId = lookupRawId(context.getContentResolver(), relation, relationBeanId);
                 if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
                     Log.v(LOG_TAG, "RelationBeanId/RelatedRawid:" + relationRawId + "/"
                                                     + relationBeanId);
 
                 if (relationRawId != 0) {
                     if (!relationbean.getFieldValue(ModuleFields.DELETED).equals(Util.DELETED_ITEM)) {
                         // update module Item
                         Log.i(LOG_TAG, "updating... " + moduleName + "_" + relation
                                                         + ": relationRawId - " + relationRawId
                                                         + ")");
                         updateRelatedModuleItem(context, context.getContentResolver(), account, moduleName, rawId, relation, relationbean, relationRawId, batchOperation);
                     } else {
                         // delete module item
                         Log.i(LOG_TAG, "deleting... " + moduleName + "_" + relation
                                                         + ": relationRawId - " + relationRawId
                                                         + ") ");
                         deleteRelatedModuleItem(context, rawId, relationRawId, moduleName, relation, batchOperation);
                     }
                 } else {
                     // add new moduleItem
                     // Log.v(LOG_TAG, "In addModuleItem");
                     if (!relationbean.getFieldValue(ModuleFields.DELETED).equals(Util.DELETED_ITEM)) {
                         Log.i(LOG_TAG, "inserting... " + moduleName + "_" + relation);
                         addRelatedModuleItem(context, account, rawId, bean, relationbean, moduleName, relation, batchOperation);
                     }
                 }
 
             }
         }
     }
 
     /**
      * set LinkNameToFieldsArray sets the array of link names to get for a given module
      * 
      * @param moduleName
      *            a {@link java.lang.String} object.
      * @throws com.imaginea.android.sugarcrm.util.SugarCrmException
      *             if any.
      */
     public static void setLinkNameToFieldsArray(String moduleName) throws SugarCrmException {
         String[] relationships = databaseHelper.getModuleRelationshipItems(moduleName);
         if (relationships == null)
             return;
         for (String relation : relationships) {
             // get the relationships for a user only if access is allowed
             if (databaseHelper.isModuleAccessAvailable(relation)) {
                 String linkFieldName = databaseHelper.getLinkfieldName(relation);
                 String[] relationProj = databaseHelper.getModuleProjections(relation);
 
                 // remove ACCOUNT_NAME from the projection
                 List<String> projList = new ArrayList<String>(Arrays.asList(relationProj));
                 projList.remove(ModuleFields.ACCOUNT_NAME);
                 mLinkNameToFieldsArray.put(linkFieldName, projList);
             }
         }
     }
 
     /**
      * Adds a single sugar bean to the sugar crm provider.
      * 
      * @param context
      *            the Authenticator Activity context
      * @param accountName
      *            the account the contact belongs to
      * @param sBean
      *            the SyncAdapter SugarBean object
      */
     private static void addModuleItem(Context context, String accountName, SugarBean sBean,
                                     String moduleName, BatchOperation batchOperation) {
         // Put the data in the contacts provider
         if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
             Log.v(LOG_TAG, "In addModuleItem");
         final SugarCRMOperations moduleItemOp = SugarCRMOperations.createNewModuleItem(context, moduleName, accountName, sBean, batchOperation);
         moduleItemOp.addSugarBean(sBean);
 
     }
 
     /**
      * Adds a single sugar bean to the sugar crm provider.
      * 
      * @param context
      *            the Authenticator Activity context
      * @param accountName
      *            the account the contact belongs to
      * @param sBean
      *            the SyncAdapter SugarBean object
      */
     private static void addRelatedModuleItem(Context context, String accountName, long rawId,
                                     SugarBean sBean, SugarBean relatedBean, String moduleName,
                                     String relationModuleName, BatchOperation batchOperation) {
         // Put the data in the contacts provider
         if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
             Log.v(LOG_TAG, "In addRelatedModuleItem");
             Log.v(LOG_TAG, " addRelatedModuleItem Rawid:" + rawId);
         }
         final SugarCRMOperations moduleItemOp = SugarCRMOperations.createNewRelatedModuleItem(context, moduleName, relationModuleName, accountName, rawId, sBean, relatedBean, batchOperation);
         moduleItemOp.addRelatedSugarBean(sBean, relatedBean);
 
     }
 
     /**
      * Updates a single module item to the sugar crm content provider.
      * 
      * @param context
      *            the Authenticator Activity context
      * @param resolver
      *            the ContentResolver to use
      * @param accountName
      *            the account the module item belongs to
      * @param moduleName
      *            the name of the module being synced
      * @param sBean
      *            the sugar crm sync adapter object.
      * @param rawId
      *            the unique Id for this raw module item in sugar crm content provider
      */
     private static void updateModuleItem(Context context, ContentResolver resolver,
                                     String accountName, String moduleName, SugarBean sBean,
                                     long rawId, BatchOperation batchOperation)
                                     throws SugarCrmException {
         if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
             Log.v(LOG_TAG, "In updateModuleItem");
         Uri contentUri = databaseHelper.getModuleUri(moduleName);
         String[] projections = databaseHelper.getModuleProjections(moduleName);
         Uri uri = ContentUris.withAppendedId(contentUri, rawId);
         // check the changes from server and mark them for merge in sync table
         SyncRecord syncRecord = databaseHelper.getSyncRecord(rawId, moduleName);
         if (syncRecord != null) {
             ContentValues values = new ContentValues();
             values.put(Sync.SYNC_STATUS, Util.SYNC_CONFLICTS);
             databaseHelper.updateSyncRecord(syncRecord._id, values);
         } else {
 
             final Cursor c = resolver.query(contentUri, projections, mSelection, new String[] { String.valueOf(rawId) }, null);
             // TODO - do something here with cursor, create update only for values that have changed
             c.close();
             final SugarCRMOperations moduleItemOp = SugarCRMOperations.updateExistingModuleItem(context, moduleName, sBean, rawId, batchOperation);
             moduleItemOp.updateSugarBean(sBean, uri);
         }
 
     }
 
     private static void updateRelatedModuleItem(Context context, ContentResolver resolver,
                                     String accountName, String moduleName, long rawId,
                                     String relatedModuleName, SugarBean relatedBean,
                                     long relationRawId, BatchOperation batchOperation)
                                     throws SugarCrmException {
         if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
             Log.v(LOG_TAG, "In updateRelatedModuleItem");
         // Uri contentUri = databaseHelper.getModuleUri(relatedModuleName);
         String[] projections = databaseHelper.getModuleProjections(relatedModuleName);
         // Uri uri = ContentUris.withAppendedId(contentUri, relationRawId);
 
         // modified the uri to have moduleName/#/relatedModuleName/# so the uri would take care of
         // updates
         Uri contentUri = Uri.withAppendedPath(databaseHelper.getModuleUri(moduleName), rawId + "");
         contentUri = Uri.withAppendedPath(contentUri, relatedModuleName);
         contentUri = Uri.withAppendedPath(contentUri, relationRawId + "");
         // if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
         // Log.v(LOG_TAG, "updateRelatedModuleItem URI:" + uri.toString());
         // check the changes from server and mark them for merge in sync table
         SyncRecord syncRecord = databaseHelper.getSyncRecord(relationRawId, moduleName, relatedModuleName);
         if (syncRecord != null) {
             ContentValues values = new ContentValues();
             values.put(Sync.SYNC_STATUS, Util.SYNC_CONFLICTS);
             databaseHelper.updateSyncRecord(syncRecord._id, values);
         } else {
             // TODO - is this query resolver needed to query here
 
             // final Cursor c = resolver.query(contentUri, projections, mSelection, new String[] {
             // String.valueOf(relationRawId) }, null);
             // TODO - do something here with cursor
             // c.close();
             final SugarCRMOperations moduleItemOp = SugarCRMOperations.updateExistingModuleItem(context, relatedModuleName, relatedBean, relationRawId, batchOperation);
             moduleItemOp.updateSugarBean(relatedBean, contentUri);
         }
 
     }
 
     /**
      * Deletes a module item from the sugar crm provider.
      * 
      * @param context
      *            the Authenticator Activity context
      * @param rawId
      *            the unique Id for this rawId in the sugar crm provider
      */
     private static void deleteModuleItem(Context context, long rawId, String moduleName,
                                     BatchOperation batchOperation) {
         Uri contentUri = databaseHelper.getModuleUri(moduleName);
 
         batchOperation.add(SugarCRMOperations.newDeleteCpo(ContentUris.withAppendedId(contentUri, rawId), true).build());
     }
 
     /**
      * Deletes a module item from the sugar crm provider.
      * 
      * @param context
      *            the Authenticator Activity context
      * @param rawId
      *            the unique Id for this rawId in the sugar crm provider
      */
     private static void deleteRelatedModuleItem(Context context, long rawId, long relatedRawId,
                                     String moduleName, String relaledModuleName,
                                     BatchOperation batchOperation) {
         Uri contentUri = databaseHelper.getModuleUri(moduleName);
         Uri parentUri = ContentUris.withAppendedId(contentUri, rawId);
         Uri relatedUri = Uri.withAppendedPath(parentUri, relaledModuleName);
         Uri deleteUri = ContentUris.withAppendedId(relatedUri, relatedRawId);
         batchOperation.add(SugarCRMOperations.newDeleteCpo(deleteUri, true).build());
     }
 
     /**
      * Returns the Raw Module item id for a sugar crm SyncAdapter , or 0 if the item is not found.
      * 
      * @param context
      *            the Authenticator Activity context
      * @param userId
      *            the SyncAdapter bean ID to lookup
      * @return the Raw item id, or 0 if not found
      */
     private static long lookupRawId(ContentResolver resolver, String moduleName, String beanId) {
         long rawId = 0;
         Uri contentUri = databaseHelper.getModuleUri(moduleName);
         String[] projection = new String[] { SugarCRMContent.RECORD_ID };
         final Cursor c = resolver.query(contentUri, projection, mSelection, new String[] { beanId }, null);
         try {
             if (c.moveToFirst()) {
                 rawId = c.getLong(0);
             }
         } finally {
             if (c != null) {
                 c.close();
             }
         }
         return rawId;
     }
 
     /**
      * syncModules, syncs the changes to any modules that are associated with the user
      * 
      * @param context
      *            a {@link android.content.Context} object.
      * @param account
      *            a {@link java.lang.String} object.
      * @param sessionId
      *            a {@link java.lang.String} object.
      * @return a boolean.
      * @throws com.imaginea.android.sugarcrm.util.SugarCrmException
      *             if any.
      */
     public static synchronized boolean syncModules(Context context, String account, String sessionId)
                                     throws SugarCrmException {
         if (databaseHelper == null)
             databaseHelper = new DatabaseHelper(context);
         List<String> userModules = databaseHelper.getUserModules();
         SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
         String url = pref.getString(Util.PREF_REST_URL, context.getString(R.string.defaultUrl));
 
         try {
             if (userModules == null || userModules.size() == 0) {
             	//Log.d(LOG_TAG, "No user modules available in the database. Trying to get available modules from the server.");
                 userModules = RestUtil.getAvailableModules(url, sessionId);
             }
             // Log.i(LOG_TAG, "userModules : " + userModules.size());
             databaseHelper.setUserModules(userModules);
         } catch (SugarCrmException e) {
             Log.e(LOG_TAG, e.getMessage(), e);
             return false;
         }
 
         Set<Module> moduleFieldsInfo = new HashSet<Module>();
         // exclude the modules now that are not part of the supported modules for user
         List<String> moduleList = databaseHelper.getModuleList();
         for (String moduleName : moduleList) {
             String[] fields = {};
             try {
                 // TODO: check if the module is already there in the db. make the rest call
                 // only if it isn't
                 Module module = RestUtil.getModuleFields(url, sessionId, moduleName, fields);
                 moduleFieldsInfo.add(module);
                 Log.i(LOG_TAG, "loaded module fields for : " + moduleName);
             } catch (SugarCrmException sce) {
                 Log.e(LOG_TAG, "failed to load module fields for : " + moduleName);
             }
         }
         try {
             databaseHelper.setModuleFieldsInfo(moduleFieldsInfo);
             return true;
         } catch (SugarCrmException sce) {
             Log.e(LOG_TAG, sce.getMessage(), sce);
         }
         databaseHelper.close();
         databaseHelper = null;
         return false;
     }
 
     /**
      * syncOutgoingModuleData, should be only run after incoming changes are synced and the sync
      * status flag is set for the modules that have merge conflicts
      * 
      * @param context
      *            a {@link android.content.Context} object.
      * @param account
      *            a {@link java.lang.String} object.
      * @param sessionId
      *            a {@link java.lang.String} object.
      * @param moduleName
      *            a {@link java.lang.String} object.
      * @param syncResult
      *            a {@link android.content.SyncResult} object.
      * @throws com.imaginea.android.sugarcrm.util.SugarCrmException
      *             if any.
      */
     public static synchronized void syncOutgoingModuleData(Context context, String account,
                                     String sessionId, String moduleName, SyncResult syncResult)
                                     throws SugarCrmException {
         if (databaseHelper == null)
             databaseHelper = new DatabaseHelper(context);
         // get outgoing items with no merge conflicts
         Cursor cursor = databaseHelper.getSyncRecordsToSync(moduleName);
         int num = cursor.getCount();
         Log.d(LOG_TAG, "UNSYNCD Item count:" + num);
         // Log.d(LOG_TAG, "UNSYNCD Column count:" + cursor.getColumnCount());
         String selectFields[] = databaseHelper.getModuleProjections(moduleName);
         cursor.moveToFirst();
         for (int i = 0; i < num; i++) {
             long syncRecordId = cursor.getLong(Sync.ID_COLUMN);
             long syncId = cursor.getLong(Sync.SYNC_ID_COLUMN);
             long syncRelatedId = cursor.getLong(Sync.SYNC_RELATED_ID_COLUMN);
             int command = cursor.getInt(Sync.SYNC_COMMAND_COLUMN);
             String relatedModuleName = cursor.getString(Sync.RELATED_MODULE_NAME_COLUMN);
             syncOutgoingModuleItem(context, sessionId, moduleName, relatedModuleName, command, syncRecordId, syncRelatedId, syncId, selectFields);
         }
         cursor.close();
         databaseHelper.close();
         databaseHelper = null;
         // databaseHelper.getS
     }
 
     private static void syncOutgoingModuleItem(Context context, String sessionId,
                                     String moduleName, String relatedModuleName, int command,
                                     long syncRecordId, long syncRelatedId, long syncId,
                                     String[] selectedFields) throws SugarCrmException {
         SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
         String url = pref.getString(Util.PREF_REST_URL, context.getString(R.string.defaultUrl));
         String updatedBeanId = null;
 
         Uri uri = ContentUris.withAppendedId(databaseHelper.getModuleUri(relatedModuleName), syncId);
 
         Map<String, String> moduleItemValues = new LinkedHashMap<String, String>();
         Cursor cursor = context.getContentResolver().query(uri, selectedFields, null, null, null);
         String relatedModuleLinkedFieldName = null;
         // we are storing the same values for moduleName and related moduleName - if there is no
         // relationship
         if (!relatedModuleName.equals(moduleName)) {
             relatedModuleLinkedFieldName = databaseHelper.getLinkfieldName(relatedModuleName);
             uri = ContentUris.withAppendedId(uri, syncRelatedId);
         }
         String beanId = null;
         cursor.moveToFirst();
         if (cursor.getCount() == 0) {
             Log.w(LOG_TAG, "No module data found for the module:" + relatedModuleName);
             return;
         }
 
         switch (command) {
         case Util.INSERT:
 
             // discard the RECORD_ID at column index -0 and the random Sync BeanId we generated
 
             for (int i = 2; i < selectedFields.length; i++) {
                 moduleItemValues.put(selectedFields[i], cursor.getString(cursor.getColumnIndex(selectedFields[i])));
             }
             // inserts with a relationship
             if (relatedModuleLinkedFieldName != null) {
 
                 // TODO - get the parents beanId - requires change to sync table
                 beanId = "";
                 updatedBeanId = RestUtil.setEntry(url, sessionId, relatedModuleName, moduleItemValues);
                 RelationshipStatus status = RestUtil.setRelationship(url, sessionId, moduleName, beanId, relatedModuleLinkedFieldName, new String[] { updatedBeanId }, new LinkedHashMap<String, String>(), Util.EXCLUDE_DELETED_ITEMS);
                 if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                     Log.i(LOG_TAG, "created: " + status.getCreatedCount() + " failed: "
                                                     + status.getFailedCount() + " deleted: "
                                                     + status.getDeletedCount());
                 }
 
                 if (status.getCreatedCount() >= 1) {
                     int count = databaseHelper.deleteSyncRecord(syncRecordId);
                     if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                         Log.i(LOG_TAG, "Relationship is also set!");
                         Log.v(LOG_TAG, "Sync--insert bean on server successful");
                         Log.v(LOG_TAG, "Sync--record deleted:" + (count > 0 ? true : false));
                     }
                 } else {
                     if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                         Log.i(LOG_TAG, "setRelationship failed!");
                     }
                 }
             } else {
                 // insert case for an orphan module add without any relationship, the
                 // updatedBeanId is actually a new beanId returned by server
                 updatedBeanId = RestUtil.setEntry(url, sessionId, relatedModuleName, moduleItemValues);
                 if (updatedBeanId != null) {
                     int count = databaseHelper.deleteSyncRecord(syncRecordId);
                     if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                         Log.v(LOG_TAG, "Sync--insert bean on server successful");
                         Log.v(LOG_TAG, "Sync--record deleted:" + (count > 0 ? true : false));
                     }
 
                 } else {
                     // TODO - we keep the item with last sync_failure date - status
                 }
 
             }
             break;
 
         // make the same calls for update and delete as delete only changes the DELETED flag
         // to 1
         case Util.UPDATE:
 
             if (relatedModuleLinkedFieldName != null) {
                 String rowId = syncId + "";
                 String parentBeanId = databaseHelper.lookupBeanId(moduleName, rowId);
 
                 // related BeanId
 
                 beanId = cursor.getString(cursor.getColumnIndex(SugarCRMContent.SUGAR_BEAN_ID));
                 moduleItemValues.put(SugarCRMContent.SUGAR_BEAN_ID, beanId);
 
                 String serverUpdatedBeanId = RestUtil.setEntry(url, sessionId, relatedModuleName, moduleItemValues);
                 if (serverUpdatedBeanId.equals(beanId)) {
                     RelationshipStatus status = RestUtil.setRelationship(url, sessionId, moduleName, parentBeanId, relatedModuleLinkedFieldName, new String[] { beanId }, new LinkedHashMap<String, String>(), Util.EXCLUDE_DELETED_ITEMS);
                     if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                         Log.i(LOG_TAG, "created: " + status.getCreatedCount() + " failed: "
                                                         + status.getFailedCount() + " deleted: "
                                                         + status.getDeletedCount());
                     }
 
                     if (status.getCreatedCount() >= 1) {
                         int count = databaseHelper.deleteSyncRecord(syncRecordId);
                         if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                             Log.i(LOG_TAG, "Relationship is also set!");
                             Log.v(LOG_TAG, "sync --updated server successful");
                             Log.v(LOG_TAG, "Sync--record deleted:" + (count > 0 ? true : false));
                         }
 
                     } else {
                         if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                             Log.i(LOG_TAG, "setRelationship failed!");
                         }
                         // TODO - we keep the item with last sync_failure date - status
                     }
                 } else {
                     // a new bean was created instead of sending back the same updated bean
                     // TODO - we keep the item with last sync_failure date - status
                 }
             } else {
                 beanId = cursor.getString(cursor.getColumnIndex(SugarCRMContent.SUGAR_BEAN_ID));
                 moduleItemValues.put(SugarCRMContent.SUGAR_BEAN_ID, beanId);
                 updatedBeanId = RestUtil.setEntry(url, sessionId, relatedModuleName, moduleItemValues);
                 if (beanId.equals(updatedBeanId)) {
 
                     int count = databaseHelper.deleteSyncRecord(syncRecordId);
                     if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                         Log.v(LOG_TAG, "sync --updated server successful");
                         Log.v(LOG_TAG, "Sync--record deleted:" + (count > 0 ? true : false));
                     }
                 } else {
                     // TODO - we keep the item with last sync_failure date - status
                 }
             }
             break;
 
         case Util.DELETE:
             beanId = cursor.getString(cursor.getColumnIndex(SugarCRMContent.SUGAR_BEAN_ID));
             moduleItemValues.put(SugarCRMContent.SUGAR_BEAN_ID, beanId);
             moduleItemValues.put(ModuleFields.DELETED, Util.DELETED_ITEM);
 
             if (relatedModuleLinkedFieldName != null) {
                 String rowId = syncId + "";
                 String parentBeanId = databaseHelper.lookupBeanId(moduleName, rowId);
 
                 // related BeanId
 
                 moduleItemValues.put(SugarCRMContent.SUGAR_BEAN_ID, beanId);
                 String serverUpdatedBeanId = RestUtil.setEntry(url, sessionId, relatedModuleName, moduleItemValues);
                 if (serverUpdatedBeanId.equals(updatedBeanId)) {
                     RelationshipStatus status = RestUtil.setRelationship(url, sessionId, moduleName, parentBeanId, relatedModuleLinkedFieldName, new String[] { beanId }, new LinkedHashMap<String, String>(), Util.EXCLUDE_DELETED_ITEMS);
                     if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                         Log.i(LOG_TAG, "created: " + status.getCreatedCount() + " failed: "
                                                         + status.getFailedCount() + " deleted: "
                                                         + status.getDeletedCount());
                     }
 
                     if (status.getCreatedCount() >= 1) {
                         int count = databaseHelper.deleteSyncRecord(syncRecordId);
                         if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                             Log.i(LOG_TAG, "Relationship is also set!");
                             Log.v(LOG_TAG, "sync --updated server successful");
                             Log.v(LOG_TAG, "Sync--record deleted:" + (count > 0 ? true : false));
                         }
 
                     } else {
                         if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                             Log.i(LOG_TAG, "setRelationship failed!");
                         }
                         // TODO - we keep the item with last sync_failure date - status
                     }
                 } else {
                     // a new bean was created instead of sending back the same updated bean
                     // TODO - we keep the item with last sync_failure date - status
                 }
             } else {
                 beanId = cursor.getString(cursor.getColumnIndex(SugarCRMContent.SUGAR_BEAN_ID));
                 moduleItemValues.put(SugarCRMContent.SUGAR_BEAN_ID, beanId);
                 updatedBeanId = RestUtil.setEntry(url, sessionId, relatedModuleName, moduleItemValues);
                 if (beanId.equals(updatedBeanId)) {
 
                     int count = databaseHelper.deleteSyncRecord(syncRecordId);
                     if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                         Log.v(LOG_TAG, "sync --updated server successful");
                         Log.v(LOG_TAG, "Sync--record deleted:" + (count > 0 ? true : false));
                     }
                 } else {
                     // TODO - we keep the item with last sync_failure date - status
                 }
             }
             break;
         }
         cursor.close();
 
     }
 
     /**
      * syncAclAccess
      * 
      * @param context
      *            a {@link android.content.Context} object.
      * @param account
      *            a {@link java.lang.String} object.
      * @param sessionId
      *            a {@link java.lang.String} object.
      * @return a boolean.
      */
     public synchronized static boolean syncAclAccess(Context context, String account,
                                     String sessionId) {
         try {
             if (Log.isLoggable(LOG_TAG, Log.DEBUG))
                 Log.d(LOG_TAG, "Sync Acl Access");
             SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
             // TODO use a constant and remove this as we start from the login screen
             String url = pref.getString(Util.PREF_REST_URL, context.getString(R.string.defaultUrl));
             HashMap<String, List<String>> linkNameToFieldsArray = new HashMap<String, List<String>>();
 
             String[] userSelectFields = { ModuleFields.ID };
             if (databaseHelper == null)
                 databaseHelper = new DatabaseHelper(context);
             String moduleName = Util.USERS;
             String aclLinkNameField = databaseHelper.getLinkfieldName(Util.ACLROLES);
             linkNameToFieldsArray.put(aclLinkNameField, Arrays.asList(ACLRoles.INSERT_PROJECTION));
 
             String actionsLinkNameField = databaseHelper.getLinkfieldName(Util.ACLACTIONS);
             HashMap<String, List<String>> linkNameToFieldsArrayForActions = new HashMap<String, List<String>>();
             linkNameToFieldsArrayForActions.put(actionsLinkNameField, Arrays.asList(ACLActions.INSERT_PROJECTION));
 
             // this gives the user bean for the logged in user along with the acl roles associated
             SugarBean[] userBeans = RestUtil.getEntryList(url, sessionId, moduleName, "Users.user_name='"
                                             + account + "'", "", "", userSelectFields, linkNameToFieldsArray, "", "");
             // userBeans always contains only one bean as we use getEntryList with the logged in
             // user name as the query parameter
             for (SugarBean userBean : userBeans) {
                 // get the acl roles
                 SugarBean[] roleBeans = userBean.getRelationshipBeans(aclLinkNameField);
                 List<String> roleIds = new ArrayList<String>();
                 // get the beanIds of the roles that are inserted
                 if (roleBeans != null) {
                     roleIds = databaseHelper.insertRoles(roleBeans);
 
                     // get the acl actions for each roleId
                     for (String roleId : roleIds) {
                         if (Log.isLoggable(LOG_TAG, Log.DEBUG))
                             Log.d(LOG_TAG, "roleId - " + roleId);
 
                         // get the aclRole along with the acl actions associated
                         SugarBean roleBean = RestUtil.getEntry(url, sessionId, Util.ACLROLES, roleId, ACLRoles.INSERT_PROJECTION, linkNameToFieldsArrayForActions);
                         SugarBean[] roleRelationBeans = roleBean.getRelationshipBeans(actionsLinkNameField);
                         if (roleRelationBeans != null) {
                             databaseHelper.insertActions(roleId, roleRelationBeans);
                         }
                     }
                 }
             }
             return true;
         } catch (SugarCrmException sce) {
             Log.e(LOG_TAG, "" + sce.getMessage(), sce);
         }
         return false;
     }
 
     /**
      * syncUsersList
      * 
      * @param context
      *            a {@link android.content.Context} object.
      * @param sessionId
      *            a {@link java.lang.String} object.
      * @return a boolean.
      */
     public synchronized static boolean syncUsersList(Context context, String sessionId) {
         try {
             if (Log.isLoggable(LOG_TAG, Log.DEBUG))
                 Log.d(LOG_TAG, "Sync Acl Access");
             SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
             // TODO use a constant and remove this as we start from the login screen
             String url = pref.getString(Util.PREF_REST_URL, context.getString(R.string.defaultUrl));
 
             HashMap<String, List<String>> linkNameToFieldsArray = new HashMap<String, List<String>>();
             SugarBean[] userBeans = RestUtil.getEntryList(url, sessionId, Util.USERS, null, null, "0", Users.INSERT_PROJECTION, linkNameToFieldsArray, null, "0");
 
             Map<String, Map<String, String>> usersMap = new TreeMap<String, Map<String, String>>();
             for (SugarBean userBean : userBeans) {
                 Map<String, String> userBeanValues = getUserBeanValues(userBean);
                 String userName = userBean.getFieldValue(ModuleFields.USER_NAME);
                if (userBeanValues != null && userBeanValues.size() > 0)
                     usersMap.put(userName, userBeanValues);
             }
 
             if (databaseHelper == null)
                 databaseHelper = new DatabaseHelper(context);
             databaseHelper.insertUsers(usersMap);
 
             return true;
         } catch (SugarCrmException sce) {
             Log.e(LOG_TAG, "" + sce.getMessage(), sce);
         }
         return false;
     }
 
     private static Map<String, String> getUserBeanValues(SugarBean userBean) {
         Map<String, String> userBeanValues = new TreeMap<String, String>();
         for (String fieldName : Users.INSERT_PROJECTION) {
             String fieldValue = userBean.getFieldValue(fieldName);
             userBeanValues.put(fieldName, fieldValue);
         }
         if (userBeanValues.size() > 0)
             return userBeanValues;
         return null;
     }
 }
