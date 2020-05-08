 package com.rubixconsulting.walletcracker;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.rubixconsulting.walletcracker.DeviceInfoParser.Exception;
 
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public final class BGLoader extends AsyncTask<Object, BGLoader.Progress, BGLoader.Status> {
   static protected final String TAG = BGLoader.class.getSimpleName();
   static private Set<WalletListener> _listeners = new HashSet<WalletListener>();
   static private Boolean _loadLock = false;
   static private Boolean _loading = false;
   static private Boolean _initialized = false;
   static private Boolean _parserLock = false;
   static private DeviceInfoParser _parser;
 
   public enum Progress {
     LOADING,
     WALLET,
     ROOT,
     COPYING,
     CRACKING,
   }
 
   public enum Status {
     SUCCESS,
     NO_WALLET,
     NO_ROOT,
    ERROR,
     PROGRESS_UPDATE_ONLY,
   }
 
   static public void addListener(WalletListener listener) {
     synchronized(_listeners) {
       if (listener != null) {
         _listeners.add(listener);
       }
     }
   }
 
   static public void removeListener(WalletListener listener) {
     synchronized(_listeners) {
       _listeners.remove(listener);
     }
   }
 
   // runs on the UI thread
   @Override protected void onPostExecute(Status result) {
     super.onPostExecute(result);
 
     if (result == Status.PROGRESS_UPDATE_ONLY) {
       return;
     }
 
     synchronized(_parserLock) {
       if ((result != Status.SUCCESS) || (_parser != null)) {
         synchronized(_listeners) {
           for (WalletListener listener : _listeners) {
             listener.walletLoaded(result, _parser);
           }
         }
       }
     }
 
     synchronized(_loadLock) {
       _initialized = true;
       _loading = false;
     }
   }
 
   // runs on the UI thread
   @Override protected void onProgressUpdate(Progress... params) {
     final Progress progress = params[0];
 
     synchronized(_listeners) {
       Log.i(TAG, "publishing progress "+progress.toString()+" to "+_listeners.size()+" listeners");
       for (WalletListener listener : _listeners) {
         listener.walletProgress(progress, Progress.values().length);
       }
     }
   }
 
   // runs in its own thread
   @Override protected Status doInBackground(Object... params) {
     Context context = (Context) params[0];
     Boolean force = (Boolean) params[1];
 
     synchronized(_loadLock) {
       if (_loading) {
         Log.i(TAG, "Load already in progress");
         publishProgress(Progress.LOADING);
         return Status.PROGRESS_UPDATE_ONLY;
       } else if (!force && _initialized) {
         Log.i(TAG, "Already initialized, force not requested");
         return Status.SUCCESS;
       }
 
       _loading = true;
     }
 
     publishProgress(Progress.LOADING);
 
     final PackageManager pm = context.getPackageManager();
     List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
 
     publishProgress(Progress.WALLET);
 
     Boolean walletFound = false;
     for (ApplicationInfo packageInfo : packages) {
       if (packageInfo.packageName.equals(context.getString(R.string.package_google_wallet))) {
         walletFound = true;
       }
     }
 
     if (!walletFound) {
       Log.i(TAG, "Could not find wallet app installed");
       return Status.NO_WALLET;
     }
 
     publishProgress(Progress.ROOT);
 
     if (!WalletCopier.canRunRootCommands()) {
       Log.i(TAG, "Could not gain root");
       return Status.NO_ROOT;
     }
 
     publishProgress(Progress.COPYING);
     new WalletCopier(context).execute();
 
     publishProgress(Progress.CRACKING);
     WalletDatastoreCopyDbHelper walletDb = null;
     try {
       // crack the pin in the bg thread so it is cached in the ui thread
       walletDb = new WalletDatastoreCopyDbHelper(context);
       synchronized(_parserLock) {
        byte[] di = walletDb.getDeviceInfo();
        if (di == null) {
          return Status.ERROR;
        }
        _parser = new DeviceInfoParser(context, di);
         _parser.crackPin();
       }
     } catch (Exception e) {
       // error cracking pin, ignore
       Log.e(TAG, e.getMessage());
     } finally {
       if (walletDb != null) {
         walletDb.close();
       }
     }
 
     return Status.SUCCESS;
   }
 }
