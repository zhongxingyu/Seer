 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import static com.testdroid.api.APIEntity.createUri;
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
 @XmlSeeAlso({AndroidFiles.class, IOSFiles.class, UIAutomatorFiles.class, RemoteControlFiles.class, CalabashFiles.class})
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
     
     protected String getDataURI() { return createUri(selfURI, "/data"); };
     protected String getApplicationURI() { return createUri(selfURI, "/application"); };
     protected String getTestURI() { return createUri(selfURI, "/test"); };
     
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
 
     @Override
     @JsonIgnore
     protected <T extends APIEntity> void clone(T from) {
         APIFiles apiFiles = (APIFiles) from;
         cloneBase(from);
         this.data = apiFiles.data;
     }
     
     @XmlRootElement 
     @XmlSeeAlso({ APIFiles.APIFile.class, APIFiles.AndroidAppFile.class, APIFiles.AndroidTestFile.class, APIFiles.DataFile.class, 
        APIFiles.IOSAppFile.class, APIFiles.IOSTestFile.class, APIFiles.UIAutomatorTestFile.class, APIFiles.RecorderOnlineScreenpleyFile.class })
     public static abstract class APIFile extends APIEntity {
         private String originalName;
         private String readableSize;
         private Date uploadTime;
         
         public APIFile() {}
 
         public APIFile(Long id, String originalName, Date uploadTime, String readableSize) {
             super(id);
             this.originalName = originalName;
             this.uploadTime = uploadTime;
             this.readableSize = readableSize;
         }
         
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
 
         @Override
         @JsonIgnore
         protected <T extends APIEntity> void clone(T from) {
             APIFile apiFile = (APIFile) from;
             cloneBase(from);
             this.originalName = apiFile.originalName;
             this.readableSize = apiFile.readableSize;
             this.uploadTime = apiFile.uploadTime;
         }
 
     }
 
     @XmlRootElement 
     public static class AndroidAppFile extends APIFile { 
         public AndroidAppFile() {}
         
         public AndroidAppFile(Long id, String originalName, Date uploadTime, String readableSize) {
             super(id, originalName, uploadTime, readableSize);
         }
     }
 
     @XmlRootElement 
     public static class AndroidTestFile extends APIFile { 
         private String mainActivity;
         private Integer minSdk;
         private String packageName;
         
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
 
         @Override
         @JsonIgnore
         protected <T extends APIEntity> void cloneBase(T from) {
             super.cloneBase(from);
             AndroidTestFile androidTestFile = (AndroidTestFile) from;
             this.mainActivity = androidTestFile.mainActivity;
             this.minSdk = androidTestFile.minSdk;
             this.packageName = androidTestFile.packageName;
         }
         
     }
 
     @XmlRootElement 
     public static class IOSAppFile extends APIFile {
         private String bundleIdentifier;
         private String bundleName;
         
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
 
         @Override
         @JsonIgnore
         protected <T extends APIEntity> void clone(T from) {
             super.clone(from);
             IOSAppFile iosAppFile = (IOSAppFile) from;
             this.bundleIdentifier = iosAppFile.bundleIdentifier;
             this.bundleName = iosAppFile.bundleName;
         }
 
     }
 
     @XmlRootElement 
     public static class IOSTestFile extends APIFile {
         public IOSTestFile() {}
         
         public IOSTestFile(Long id, String originalName, Date uploadTime, String readableSize) {
             super(id, originalName, uploadTime, readableSize);
         }
     }
 
     @XmlRootElement 
     public static class CalabashTestFile extends APIFile {
         public CalabashTestFile() {}
         public CalabashTestFile(Long id, String originalName, Date uploadTime, String readableSize) {
             super(id, originalName, uploadTime, readableSize);
         }
     }
 
     @XmlRootElement 
     public static class UIAutomatorTestFile extends APIFile {
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
 
         @Override
         @JsonIgnore
         protected <T extends APIEntity> void clone(T from) {
             super.clone(from);
             UIAutomatorTestFile uiAutomatorTestFile = (UIAutomatorTestFile) from;
             this.jarNames = uiAutomatorTestFile.jarNames;
         }
 
     }
 
     @XmlRootElement 
     public static class DataFile extends APIFile {
         public DataFile() {}
         
         public DataFile(Long id, String originalName, Date uploadTime, String readableSize) {
             super(id, originalName, uploadTime, readableSize);
         }
     }
     
     @XmlRootElement
    public static class RecorderOnlineScreenpleyFile extends APIFile {
         public static enum Format {
             JAVA,JSON;
         }
         
        public RecorderOnlineScreenpleyFile() {
         }
 
        public RecorderOnlineScreenpleyFile(Long id, String originalName, Date uploadTime, String readableSize) {
             super(id, originalName, uploadTime, readableSize);
         }
     }
     
 }
