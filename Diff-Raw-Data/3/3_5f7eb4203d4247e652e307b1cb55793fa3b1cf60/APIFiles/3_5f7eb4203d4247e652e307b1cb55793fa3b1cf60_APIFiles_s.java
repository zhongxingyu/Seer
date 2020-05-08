 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import com.testdroid.api.APIException;
 import java.io.InputStream;
 import java.util.Date;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlSeeAlso;
 import javax.xml.bind.annotation.XmlTransient;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author kajdus
  */
 @XmlRootElement
 @XmlSeeAlso({AndroidFiles.class, IOSFiles.class, UIAutomatorFiles.class, RemoteControlFiles.class})
 public abstract class APIFiles extends APIEntity {
     private DataFile data;
 
     public APIFiles() {}
 
     public APIFiles(Long id, DataFile data) {
         super(id);
         this.data = data;
     }
     
     public DataFile getData() {
         return data;
     }
 
     public void setData(DataFile data) {
         this.data = data;
     }
     
     protected String getDataURI() { return selfURI + "/data"; };
     protected String getApplicationURI() { return selfURI + "/application"; };
     protected String getTestURI() { return selfURI + "/test"; };
     
     @JsonIgnore
     @XmlTransient
     public InputStream getApplicationStream() throws APIException {
         return getResource(getApplicationURI(), null).getStream();
     }
 
     @JsonIgnore
     @XmlTransient
     public InputStream getTestStream() throws APIException {
         return getResource(getTestURI(), null).getStream();
     }
     
     @JsonIgnore
     @XmlTransient
     public InputStream getDataStream() throws APIException {
         return getResource(getDataURI(), null).getStream();
     }
     
     @XmlRootElement public static class AndroidFiles extends APIFiles {
         private AndroidAppFile androidApp;
         private AndroidTestFile androidTest;
 
         public AndroidFiles() {}
         public AndroidFiles(Long id, DataFile data, AndroidAppFile androidApp, AndroidTestFile androidTest) {
             super(id, data);
             this.androidApp = androidApp;
             this.androidTest = androidTest;
         }
 
         public AndroidAppFile getAndroidApp() {
             return androidApp;
         }
 
         public void setAndroidApp(AndroidAppFile androidApp) {
             this.androidApp = androidApp;
         }
 
         public AndroidTestFile getAndroidTest() {
             return androidTest;
         }
 
         public void setAndroidTest(AndroidTestFile androidTest) {
             this.androidTest = androidTest;
         }
         
         public void uploadApp(File file) throws APIException {
             this.androidApp = client.postFile(selfURI + "/application", "application/vnd.android.package-archive", file, AndroidAppFile.class);
         }
         
         public void uploadTest(File file) throws APIException {
             this.androidTest = client.postFile(selfURI + "/test", "application/vnd.android.package-archive", file, AndroidTestFile.class);
         }
 
     }
     
     @XmlRootElement public static class IOSFiles extends APIFiles {
         private IOSAppFile iosApp;
         private IOSTestFile iosTest;
 
         public IOSFiles() {}
         public IOSFiles(Long id, DataFile data, IOSAppFile iosApp, IOSTestFile iosTest) {
             super(id, data);
             this.iosApp = iosApp;
             this.iosTest = iosTest;
         }
 
         public IOSAppFile getIosApp() {
             return iosApp;
         }
 
         public void setIosApp(IOSAppFile iosApp) {
             this.iosApp = iosApp;
         }
 
         public IOSTestFile getIosTest() {
             return iosTest;
         }
 
         public void setIosTest(IOSTestFile iosTest) {
             this.iosTest = iosTest;
         } 
         
         public void uploadApp(File file) throws APIException {
             this.iosApp = client.postFile(selfURI, "application/vnd.apple.pkpass", file, IOSAppFile.class);
         }
         
         public void uploadTest(File file) throws APIException {
             this.iosTest = client.postFile(selfURI, "application/zip", file, IOSTestFile.class);
         }
 
     }
     
     @XmlRootElement public static class UIAutomatorFiles extends APIFiles {
         private AndroidAppFile androidApp;
         private UIAutomatorTestFile uiAutomatorTest;
 
         public UIAutomatorFiles() {}
         public UIAutomatorFiles(Long id, DataFile data, AndroidAppFile androidApp, UIAutomatorTestFile uiAutomatorTest) {
             super(id, data);
             this.androidApp = androidApp;
             this.uiAutomatorTest = uiAutomatorTest;
         }
 
         public AndroidAppFile getAndroidApp() {
             return androidApp;
         }
 
         public void setAndroidApp(AndroidAppFile androidApp) {
             this.androidApp = androidApp;
         }
 
         public UIAutomatorTestFile getUiAutomatorTest() {
             return uiAutomatorTest;
         }
 
         public void setUiAutomatorTest(UIAutomatorTestFile uiAutomatorTest) {
             this.uiAutomatorTest = uiAutomatorTest;
         } 
         
         public void uploadApp(File file) throws APIException {
             this.androidApp = client.postFile(selfURI, "application/vnd.android.package-archive", file, AndroidAppFile.class);
         }
         
         public void uploadTest(File file) throws APIException {
             this.uiAutomatorTest = client.postFile(selfURI, "application/vnd.android.package-archive", file, UIAutomatorTestFile.class);
         }
     }
     
     @XmlRootElement public static class RemoteControlFiles extends APIFiles {
         private AndroidAppFile androidApp;
 
         public RemoteControlFiles() {}
         public RemoteControlFiles(Long id, DataFile data, AndroidAppFile androidApp) {
             super(id, data);
             this.androidApp = androidApp;
         }
 
         public AndroidAppFile getAndroidApp() {
             return androidApp;
         }
 
         public void setAndroidApp(AndroidAppFile androidApp) {
             this.androidApp = androidApp;
         }      
         
         public void uploadApp(File file) throws APIException {
             this.androidApp = client.postFile(selfURI, "application/vnd.android.package-archive", file, AndroidAppFile.class);
         }
         
 
         @Override
         public InputStream getTestStream() throws APIException {
             throw new APIException(HttpStatus.SC_NOT_FOUND, "Remote control project has no test file");
         }
     }
     
     @XmlRootElement public static abstract class APIFile extends APIEntity {
         private String originalName;
         private Date uploadTime;
         private String readableSize;
 
         public String getOriginalName() {
             return originalName;
         }
 
         public void setOriginalName(String originalName) {
             this.originalName = originalName;
         }
 
         public Date getUploadTime() {
             return uploadTime;
         }
 
         public void setUploadTime(Date uploadTime) {
             this.uploadTime = uploadTime;
         }
 
         public String getReadableSize() {
             return readableSize;
         }
 
         public void setReadableSize(String readableSize) {
             this.readableSize = readableSize;
         }
 
         public APIFile() {}
 
         public APIFile(Long id, String originalName, Date uploadTime, String readableSize) {
             super(id);
             this.originalName = originalName;
             this.uploadTime = uploadTime;
             this.readableSize = readableSize;
         }
 
     }
 
     @XmlRootElement public static class AndroidAppFile extends APIFile { 
         public AndroidAppFile() {}
         public AndroidAppFile(Long id, String originalName, Date uploadTime, String readableSize) {
             super(id, originalName, uploadTime, readableSize);
         }
 
     }
 
     @XmlRootElement public static class AndroidTestFile extends APIFile { 
         private String packageName;
         private String mainActivity;
         private Integer minSdk;
 
         public AndroidTestFile() {}
         public AndroidTestFile(Long id, String originalName, Date uploadTime, String readableSize, String packageName, String mainActivity, Integer minSdk) {
             super(id, originalName, uploadTime, readableSize);
             this.packageName = packageName;
             this.mainActivity = mainActivity;
             this.minSdk = minSdk;
         }
 
         public String getPackageName() {
             return packageName;
         }
 
         public void setPackageName(String packageName) {
             this.packageName = packageName;
         }
 
         public String getMainActivity() {
             return mainActivity;
         }
 
         public void setMainActivity(String mainActivity) {
             this.mainActivity = mainActivity;
         }
 
         public Integer getMinSdk() {
             return minSdk;
         }
 
         public void setMinSdk(Integer minSdk) {
             this.minSdk = minSdk;
         }
 
     }
 
     @XmlRootElement public static class IOSAppFile extends APIFile {
         private String bundleName;
         private String bundleIdentifier;
 
         public IOSAppFile() {}
         public IOSAppFile(Long id, String originalName, Date uploadTime, String readableSize, String bundleName, String bundleIdentifier) {
             super(id, originalName, uploadTime, readableSize);
             this.bundleName = bundleName;
             this.bundleIdentifier = bundleIdentifier;
         }
 
         public String getBundleName() {
             return bundleName;
         }
 
         public void setBundleName(String bundleName) {
             this.bundleName = bundleName;
         }
 
         public String getBundleIdentifier() {
             return bundleIdentifier;
         }
 
         public void setBundleIdentifier(String bundleIdentifier) {
             this.bundleIdentifier = bundleIdentifier;
         }
 
     }
 
     @XmlRootElement public static class IOSTestFile extends APIFile {
         public IOSTestFile() { }
         public IOSTestFile(Long id, String originalName, Date uploadTime, String readableSize) {
             super(id, originalName, uploadTime, readableSize);
         }
     }
 
     @XmlRootElement public static class UIAutomatorTestFile extends APIFile {
         private String jarNames;
 
         public UIAutomatorTestFile() {}
 
         public UIAutomatorTestFile(Long id, String originalName, Date uploadTime, String readableSize, String jarNames) {
             super(id, originalName, uploadTime, readableSize);
             this.jarNames = jarNames;
         }
 
         public String getJarNames() {
             return jarNames;
         }
 
         public void setJarNames(String jarNames) {
             this.jarNames = jarNames;
         }
 
     }
 
     @XmlRootElement public static class DataFile extends APIFile {
         public DataFile() { }
         public DataFile(Long id, String originalName, Date uploadTime, String readableSize) {
             super(id, originalName, uploadTime, readableSize);
         }
     }
             
 }
