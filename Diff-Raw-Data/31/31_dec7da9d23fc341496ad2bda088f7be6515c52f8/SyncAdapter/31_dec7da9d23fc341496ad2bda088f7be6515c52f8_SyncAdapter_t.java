 package com.imaginea.android.sugarcrm.sync;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.ParseException;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.AbstractThreadedSyncAdapter;
 import android.content.ContentProviderClient;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SyncResult;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.imaginea.android.sugarcrm.ModuleListActivity;
 import com.imaginea.android.sugarcrm.R;
 import com.imaginea.android.sugarcrm.RestUtilConstants;
 import com.imaginea.android.sugarcrm.SugarCrmApp;
 import com.imaginea.android.sugarcrm.provider.DatabaseHelper;
 import com.imaginea.android.sugarcrm.util.RestUtil;
 import com.imaginea.android.sugarcrm.util.SugarCrmException;
 import com.imaginea.android.sugarcrm.util.Util;
 
 /**
  * SyncAdapter implementation for syncing sugarcrm modules on the server to sugar crm provider and
  * vice versa.
  * 
  * //TODO - Stress testing for large datasets - test cases
  */
 public class SyncAdapter extends AbstractThreadedSyncAdapter {
 
     private final AccountManager mAccountManager;
 
     private final Context mContext;
 
     private Date mLastUpdated;
 
     private static final String LOG_TAG = "SyncAdapter";
 
     /**
      * <p>
      * Constructor for SyncAdapter.
      * </p>
      * 
      * @param context
      *            a {@link android.content.Context} object.
      * @param autoInitialize
      *            a boolean.
      */
     public SyncAdapter(Context context, boolean autoInitialize) {
         super(context, autoInitialize);
         mContext = context;
         mAccountManager = AccountManager.get(context);
     }
 
     /** {@inheritDoc} */
     @Override
     public void onPerformSync(Account account, Bundle extras, String authority,
                                     ContentProviderClient provider, SyncResult syncResult) {
         Log.i(LOG_TAG, "onPerformSync");
         // String authtoken = null;
         int syncType = extras.getInt(Util.SYNC_TYPE);
 
         try {
             // use the account manager to request the credentials
             // authtoken = mAccountManager.blockingGetAuthToken(account, Util.AUTHTOKEN_TYPE, true
             // /* notifyAuthFailure */);
             // Log.v(LOG_TAG, "authtoken:" + authtoken);
 
             /*
              * if we are a password based system, the SugarCRM OAuth setup is not clear yet but
              * based on preferences, we ahould select the right one -?
              */
             String userName = account.name;
             String password = mAccountManager.getPassword(account);
             if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                 Log.v(LOG_TAG, "Sync Type name: " + syncType);
                 Log.v(LOG_TAG, "user name: " + userName + " and authority: " + authority);
             }
 
             if (!Util.isNetworkOn(mContext)) {
                 Log.v(LOG_TAG, "Network is not on..skipping sync:");
                 syncResult.stats.numIoExceptions++;
                 return;
             }
             SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
             String url = pref.getString(Util.PREF_REST_URL, mContext.getString(R.string.defaultUrl));
             SugarCrmApp app = ((SugarCrmApp) SugarCrmApp.app);
             String sessionId = app != null ? app.getSessionId() : null;
             if (sessionId == null) {
                 sessionId = RestUtil.loginToSugarCRM(url, account.name, password);
             }
 
             // TODO run this list through our local DB and see if any changes have happened and sync
             // those modules and module fields
 
             switch (syncType) {
 
             case Util.SYNC_MODULE_META_DATA:
                 // use this only for testing
                 SugarSyncManager.syncModules(mContext, account.name, sessionId);
                 break;
             case Util.SYNC_ALL_META_DATA:
                 // should be used once for one time set-up
                 boolean modulesSyncd = SugarSyncManager.syncModules(mContext, account.name, sessionId);
                 // Log.d(LOG_TAG, "syncModules done. success: " + modulesSyncd);
 
                 // boolean aclAccessSyncd = SugarSyncManager.syncAclAccess(mContext, account.name,
                 // sessionId);
                 // Log.d(LOG_TAG, "ACL access aync done. success: " + aclAccessSyncd);
 
                 boolean usersSyncd = SugarSyncManager.syncUsersList(mContext, sessionId);
                 // Log.d(LOG_TAG, "Users aync done. success: " + usersSyncd);
 
                 // TODO - Need to resolve SYNC issue.
                 // if (modulesSyncd && aclAccessSyncd & usersSyncd) {
                 if (modulesSyncd && usersSyncd) {
                     Editor editor = pref.edit();
                     editor.putBoolean(Util.SYNC_METADATA_COMPLETED, true);
                     editor.commit();
                 }
                 break;
             case Util.SYNC_ACL_ACCESS_META_DATA:
                 // use this only for testing
                 // SugarSyncManager.syncAclAccess(mContext, account.name, sessionId);
                 break;
 
             case Util.SYNC_MODULES_DATA:
                 // default mode - sync all modules - from the sync screen
                 syncAllModulesData(account, extras, authority, sessionId, syncResult);
                 break;
             case Util.SYNC_MODULE_DATA:
                 // sync only one module - can be used once module based sync is provided
                 String moduleName = extras.getString(RestUtilConstants.MODULE_NAME);
                 syncModuleData(account, extras, authority, sessionId, moduleName, syncResult);
                 break;
             case Util.SYNC_ALL:
                 // testing
                 SugarSyncManager.syncModules(mContext, account.name, sessionId);
                 // SugarSyncManager.syncAclAccess(mContext, account.name, sessionId);
                 syncAllModulesData(account, extras, authority, sessionId, syncResult);
                 break;
             default:
                 // if called from accounts and sync screen, we sync only module data
                 syncAllModulesData(account, extras, authority, sessionId, syncResult);
                 break;
             }
 
         } catch (final ParseException e) {
             syncResult.stats.numParseExceptions++;
             Log.e(LOG_TAG, "ParseException", e);
         } /*
            * catch (OperationCanceledException e) { // TODO - whats the stats update here //
            * syncResult.stats.++; Log.e(LOG_TAG, e.getMessage(), e); } catch (AuthenticatorException
            * e) { // syncResult.stats.numAuthExceptions++; Log.e(LOG_TAG, e.getMessage(), e); }
            * catch (IOException e) { syncResult.stats.numIoExceptions++; Log.e(LOG_TAG,
            * e.getMessage(), e); }
            */catch (SugarCrmException se) {
             Log.e(LOG_TAG, se.getMessage(), se);
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
         }
     }
 
     /**
      * syncModulesData, syncs all modules
      * 
      * @param account
      * @param extras
      * @param authority
      * @param sessionId
      * @param syncResult
      */
     private void syncAllModulesData(Account account, Bundle extras, String authority,
                                     String sessionId, SyncResult syncResult)
                                     throws SugarCrmException {
         DatabaseHelper databaseHelper = new DatabaseHelper(mContext);
         List<String> moduleList = databaseHelper.getModuleList();
         databaseHelper.close();
 
         if (moduleList.size() == 0) {
             Log.w(LOG_TAG, "No modules to sync");
         }
         // TODO - dynamically determine the relationships and get the values
         Collections.sort(moduleList);
         for (String moduleName : moduleList) {
             syncModuleData(account, extras, authority, sessionId, moduleName, syncResult);
         }
 
         // update the last synced date.
         mLastUpdated = new Date();
         // do not use sync result status to notify, notify module specific comprehensive stats
         mContext.getApplicationContext();
         String msg = mContext.getString(R.string.syncMessage);
         Util.notify(mContext, mContext.getApplicationContext().getPackageName(), ModuleListActivity.class, R.string.syncSuccess, R.string.syncSuccess, String.format(msg, SugarSyncManager.mTotalRecords));
     }
 
     /**
      * syncModuleData
      * 
      * @param account
      * @param extras
      * @param authority
      * @param sessionId
      * @param moduleName
      * @param syncResult
      * @throws SugarCrmException
      */
     private void syncModuleData(Account account, Bundle extras, String authority, String sessionId,
                                     String moduleName, SyncResult syncResult)
                                     throws SugarCrmException {
         Log.i(LOG_TAG, "Syncing Incoming Module Data:" + moduleName);
 
         // TODO - should be catch SugarCRMException and allow processing other modules and
         // fail completely
         SugarSyncManager.syncModulesData(mContext, account.name, sessionId, moduleName, syncResult);
 
         /*
          * at this point we are done with identifying the merge conflicts in the sync table for
          * incoming module data; the remaining un-synced items in the sync table for that module can
          * be published to the server now.
          */
         Log.i(LOG_TAG, "Syncing Outgoing Module Data:" + moduleName);
         SugarSyncManager.syncOutgoingModuleData(mContext, account.name, sessionId, moduleName, syncResult);
     }
 
     /** {@inheritDoc} */
     @Override
     public void onSyncCanceled() {
         super.onSyncCanceled();
         // TODO - notify is part if sync framework, with the SyncResults giving details about the
         // last sync, we perform additional steps that are specific to our app if required
     }
 
 }
