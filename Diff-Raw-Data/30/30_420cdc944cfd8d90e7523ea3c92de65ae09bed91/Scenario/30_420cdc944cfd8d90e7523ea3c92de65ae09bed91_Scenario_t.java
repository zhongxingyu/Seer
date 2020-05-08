 package cz.zcu.kiv.eeg.mobile.base.data.container.xml;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import cz.zcu.kiv.eeg.mobile.base.data.Values;
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.Root;
 
 /**
  * Data container for holding scenario information.
  *
  * @author Petr Miko
  */
 @Root(name = "scenario")
 public class Scenario implements Parcelable {
 
     public static final Parcelable.Creator<Scenario> CREATOR
             = new Parcelable.Creator<Scenario>() {
         public Scenario createFromParcel(Parcel in) {
             return new Scenario(in);
         }
 
         public Scenario[] newArray(int size) {
             return new Scenario[size];
         }
     };
     @Element
     private int scenarioId;
     @Element
     private String scenarioName;
     @Element
     private String ownerName;
     @Element
     private String researchGroupName;
     @Element(required = false)
     private String description;
     @Element(required = false, name = "private")
     private boolean isPrivate;
     @Element(required = false)
     private String mimeType;
     @Element(required = false)
     private String fileName;
     @Element(required = false)
     private String fileLength;
     @Element
     private Integer researchGroupId;
     private String filePath;
 
     public Scenario() {
     }
 
     public Scenario(Parcel in) {
         scenarioId = in.readInt();
         scenarioName = in.readString();
         researchGroupId = in.readInt();
         researchGroupName = in.readString();
         ownerName = in.readString();
         description = in.readString();
         mimeType = in.readString();
        filePath = in.readString();
         fileName = in.readString();
         fileLength = in.readString();
         isPrivate = in.readByte() == Values.TRUE;
     }
 
     public int getScenarioId() {
         return scenarioId;
     }
 
     public void setScenarioId(int scenarioId) {
         this.scenarioId = scenarioId;
     }
 
     public String getOwnerName() {
         return ownerName;
     }
 
     public void setOwnerName(String ownerName) {
         this.ownerName = ownerName;
     }
 
     public String getResearchGroupName() {
         return researchGroupName;
     }
 
     public void setResearchGroupName(String researchGroupName) {
         this.researchGroupName = researchGroupName;
     }
 
     public String getScenarioName() {
         return scenarioName;
     }
 
     public void setScenarioName(String scenarioName) {
         this.scenarioName = scenarioName;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public boolean isPrivate() {
         return isPrivate;
     }
 
     public void setPrivate(boolean aPrivate) {
         isPrivate = aPrivate;
     }
 
     public String getMimeType() {
         return mimeType;
     }
 
     public void setMimeType(String mimeType) {
         this.mimeType = mimeType;
     }
 
     public String getFileName() {
         return fileName;
     }
 
     public void setFileName(String fileName) {
         this.fileName = fileName;
     }
 
     public String getFileLength() {
         return fileLength;
     }
 
     public void setFileLength(String fileLength) {
         this.fileLength = fileLength;
     }
 
     public Integer getResearchGroupId() {
         return researchGroupId;
     }
 
     public void setResearchGroupId(Integer researchGroupId) {
         this.researchGroupId = researchGroupId;
     }
 
     public String getFilePath() {
         return filePath;
     }
 
     public void setFilePath(String filePath) {
         this.filePath = filePath;
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeInt(scenarioId);
         dest.writeString(scenarioName);
         dest.writeInt(researchGroupId);
         dest.writeString(researchGroupName);
         dest.writeString(ownerName);
         dest.writeString(description);
         dest.writeString(mimeType);
         dest.writeString(fileName);
         dest.writeString(filePath);
         dest.writeString(fileLength);
         dest.writeByte(isPrivate ? Values.TRUE : Values.FALSE);
     }
 }
