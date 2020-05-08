 package info.evelio.whatsnew.model;
 
 import android.content.pm.*;
 import android.graphics.drawable.Drawable;
 import com.codeslap.persistence.Column;
 import com.codeslap.persistence.Ignore;
 import com.codeslap.persistence.PrimaryKey;
 import com.codeslap.persistence.Table;
 import info.evelio.whatsnew.util.L;
 
 import java.io.File;
 
 import static info.evelio.whatsnew.util.StringUtils.defaultIfEmpty;
 import static info.evelio.whatsnew.util.StringUtils.emptyIfNull;
 import static info.evelio.whatsnew.util.StringUtils.isNotEmpty;
 
 /**
  * Full entry for certain ap even if it was removed
  * @author Evelio Tarazona Cáceres <evelio@evelio.info>
  */
 @Table(value = ApplicationEntry.Contract.TABLE_NAME)
 public class ApplicationEntry {
   private static final String TAG = "wn:appentry";
   /**
    * Package name
    */
   @PrimaryKey(autoincrement = false)
   @Column(value = Contract.COLUMN_PACKAGE_NAME, forceName = true)
   private String packageName;
   @Column(Contract.COLUMN_PACKAGE_VERSION)
   private String packageVersion;
   @Column(Contract.COLUMN_PACKAGE_VERSION_CODE)
   private long packageVersionCode;
   @Column(Contract.COLUMN_PREVIOUS_PACKAGE_VERSION)
   private String previousPackageVersion;
  @Column(value = Contract.COLUMN_PREVIOUS_PACKAGE_VERSION_CODE, defaultValue = "-1")
  private long previousPackageVersionCode;
   @Column(Contract.COLUMN_FIRST_INSTALL_TIME)
   private long firstInstallTime;
   @Column(Contract.COLUMN_LAST_UPDATE_TIME)
   private long lastUpdateTime;
   @Column(value = Contract.COLUMN_REMOVED, defaultValue = "0")
   private boolean removed;
 
   @Column(value = Contract.COLUMN_CHANGE_LOG, defaultValue = "")
   private String changeLog;
 
   @Ignore
   private CharSequence label;
   @Ignore
   private Drawable icon;
 
   /**
    * Creates an empty instance
    */
   public ApplicationEntry() {
   }
 
   /**
    * Retrieve current package name
    *
    * @return Current package name value might be null
    */
   public final String getPackageName() {
     return packageName;
   }
 
   /**
    * Sets package name to given value
    *
    * @param givenPackageName Package name to set to
    */
   public final void setPackageName(final String givenPackageName) {
     this.packageName = givenPackageName;
   }
 
   public String getPackageVersion() {
     return packageVersion;
   }
 
   public void setPackageVersion(String packageVersion) {
     this.packageVersion = packageVersion;
   }
 
   public long getPackageVersionCode() {
     return packageVersionCode;
   }
 
   public void setPackageVersionCode(long packageVersionCode) {
     this.packageVersionCode = packageVersionCode;
   }
 
   public String getPreviousPackageVersion() {
     return previousPackageVersion;
   }
 
   public void setPreviousPackageVersion(String previousPackageVersion) {
     this.previousPackageVersion = previousPackageVersion;
   }
 
   public long getPreviousPackageVersionCode() {
     return previousPackageVersionCode;
   }
 
   public void setPreviousPackageVersionCode(long previousPackageVersionCode) {
     this.previousPackageVersionCode = previousPackageVersionCode;
   }
 
   public long getFirstInstallTime() {
     return firstInstallTime;
   }
 
   public void setFirstInstallTime(long firstInstallTime) {
     this.firstInstallTime = firstInstallTime;
   }
 
   public long getLastUpdateTime() {
     return lastUpdateTime;
   }
 
   public void setLastUpdateTime(long lastUpdateTime) {
     this.lastUpdateTime = lastUpdateTime;
   }
 
   public CharSequence getLabel() {
     return label;
   }
 
   public void setLabel(CharSequence label) {
     this.label = label;
   }
 
   public Drawable getIcon() {
     return icon;
   }
 
   public void setIcon(Drawable icon) {
     this.icon = icon;
   }
 
   public boolean isRemoved() {
     return removed;
   }
 
   public void setRemoved(boolean removed) {
     this.removed = removed;
   }
 
   public String getChangeLog() {
     return changeLog;
   }
 
   public void setChangeLog(String changeLog) {
     this.changeLog = changeLog;
   }
 
   public boolean hasValidPackageName() {
     return isNotEmpty(packageName);
   }
 
   public CharSequence getDisplayableLabel() {
     if (label != null) {
       return label;
     }
     return emptyIfNull(packageName);
   }
 
   public CharSequence getDisplayableVersion() {
     return defaultIfEmpty(packageVersion, packageVersionCode);
   }
 
   public CharSequence getDisplayablePreviousVersion() {
     return defaultIfEmpty(previousPackageVersion, previousPackageVersionCode);
   }
 
   public void loadResources(final PackageManager pm, final ApplicationInfo appInfo) throws Exception {
     File sourceDir = new File(appInfo.sourceDir);
     if (sourceDir.exists()) { // its mounted
       // TODO EEE we might want to cache this in db or dir as it rarely changes
       setLabel(appInfo.loadLabel(pm));
       setIcon(appInfo.loadIcon(pm));
     }
   }
 
   @Override
   public boolean equals(Object o) {
     if (this == o) return true;
     if (!(o instanceof ApplicationEntry)) return false;
 
     ApplicationEntry entry = (ApplicationEntry) o;
 
     if (packageName != null ? !packageName.equals(entry.packageName) : entry.packageName != null) return false;
 
     return true;
   }
 
   @Override
   public int hashCode() {
     return packageName != null ? packageName.hashCode() : 0;
   }
 
   @Override
   public String toString() {
     return "ApplicationEntry{" +
         "packageName='" + packageName + '\'' +
         ", packageVersion='" + packageVersion + '\'' +
         ", packageVersionCode=" + packageVersionCode +
         ", previousPackageVersion='" + previousPackageVersion + '\'' +
         ", previousPackageVersionCode=" + previousPackageVersionCode +
         ", firstInstallTime=" + firstInstallTime +
         ", lastUpdateTime=" + lastUpdateTime +
         ", removed=" + removed +
         ", changeLog='" + changeLog + '\'' +
         ", label=" + label +
         '}';
   }
 
   public interface Contract {
     /**
      * Table name
      */
     String TABLE_NAME = "applications";
     /**
      * Text column used for storing package name
      */
     String COLUMN_PACKAGE_NAME = "package_name";
     String COLUMN_PACKAGE_VERSION = "package_version";
     String COLUMN_PREVIOUS_PACKAGE_VERSION = "previous_package_version";
     String COLUMN_PACKAGE_VERSION_CODE = "package_version_code";
     String COLUMN_PREVIOUS_PACKAGE_VERSION_CODE = "previous_package_version_code";
     String COLUMN_FIRST_INSTALL_TIME = "first_install_time";
     String COLUMN_LAST_UPDATE_TIME = "last_update_time";
     String COLUMN_REMOVED = "removed";
     String COLUMN_CHANGE_LOG = "change_log";
 
   }
 
   public static final class Builder {
     private PackageManager mPm;
     private String mPackageName;
     private boolean mTryLoadResources;
     private ApplicationEntry mPrevious;
 
     public Builder(PackageManager pm) {
       mPm = pm;
       reset();
     }
 
     public Builder reset() {
       mPackageName = null;
       mTryLoadResources = false;
       mPrevious = null;
       return this;
     }
 
     public Builder forPackage(String packageName) {
       mPackageName = packageName;
       return this;
     }
 
     public Builder loadingResources() {
       mTryLoadResources = true;
       return this;
     }
 
     public Builder from(final PackageItemInfo info) {
       return forPackage(info == null ? null : info.packageName);
     }
 
     public Builder from(final ResolveInfo resolvedInfo) {
       if (resolvedInfo == null) {
         forPackage(null);
         return this;
       }
       from(resolvedInfo.activityInfo);
       if (isNotEmpty(mPackageName)) {
         return this;
       }
       // We try a service in such case
       return from(resolvedInfo.serviceInfo);
     }
 
     public Builder with(final ApplicationEntry previous) {
       mPrevious = previous;
       return this;
     }
 
     public ApplicationEntry build() {
       ApplicationEntry appEntry = new ApplicationEntry();
       appEntry.setPackageName(mPackageName);
 
       try {
         PackageInfo packageInfo = mPm.getPackageInfo(mPackageName,
             PackageManager.GET_DISABLED_COMPONENTS
                 | PackageManager.GET_UNINSTALLED_PACKAGES
                 | PackageManager.GET_SIGNATURES);
 
         appEntry.setPackageVersion(packageInfo.versionName);
         appEntry.setPackageVersionCode(packageInfo.versionCode);
         appEntry.setFirstInstallTime(packageInfo.firstInstallTime);
         appEntry.setLastUpdateTime(packageInfo.lastUpdateTime);
 
         if (mPrevious != null) {
           appEntry.setPreviousPackageVersion( mPrevious.getPackageVersion() );
           appEntry.setPreviousPackageVersionCode( mPrevious.getPreviousPackageVersionCode() );
         }
 
         if (mTryLoadResources) {
           appEntry.loadResources(mPm, packageInfo.applicationInfo);
         }
       } catch (Exception e) {
         L.e(TAG, "Unable to get some package info", e);
       }
 
       return appEntry;
     }
 
   }
 
 }
