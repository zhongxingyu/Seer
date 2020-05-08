 package info.evelio.whatsnew.task;
 
 import android.content.Context;
 import info.evelio.whatsnew.model.ApplicationEntry;
 import info.evelio.whatsnew.util.L;
 
 /**
  * @author Evelio Tarazona Cáceres <evelio@evelio.info>
  */
 public class PackageReplace extends PackageTask {
   private static final String TAG = "wn:PReplace";
 
   @Override
   protected boolean doInBackground() {
     final String packageName = getPackageName();
     L.d(TAG, "Replacing package " + packageName);
     final Context context = getContext();
     final ApplicationEntry.Builder builder = new ApplicationEntry.Builder(context.getPackageManager())
         .forPackage(packageName);
     final ApplicationEntry oldEntry = readApplicationEntry();
     final ApplicationEntry applicationEntry = builder.with(oldEntry).build();
 
     L.d(TAG, "Replacing " + oldEntry + " with " + applicationEntry);
     if (applicationEntry.getPackageVersionCode() == oldEntry.getPackageVersionCode()) {
       L.e(TAG, "Package replaced but version code remains equals");
       return false;
     }
 
     makeSnapshot(oldEntry);
     L.d(TAG, "Updating " + applicationEntry);
    boolean result = getSqlAdapter().update(applicationEntry, oldEntry) == 1;
 
     updateChangeLogAsync();
 
     pingNotification();
 
     return result;
   }
 
 }
