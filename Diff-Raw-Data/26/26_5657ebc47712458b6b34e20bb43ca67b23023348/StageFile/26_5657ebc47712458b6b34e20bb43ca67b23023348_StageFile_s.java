 package com.yamj.core.database.model;
 
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 import org.hibernate.annotations.ForeignKey;
 
 import com.yamj.core.database.model.type.FileType;
 import com.yamj.common.type.StatusType;
 import com.yamj.core.hibernate.usertypes.EnumStringUserType;
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.*;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import org.apache.commons.lang3.StringUtils;
 import org.hibernate.annotations.*;
 import org.hibernate.annotations.Parameter;
 
 @TypeDefs({
     @TypeDef(name = "fileType",
             typeClass = EnumStringUserType.class,
             parameters = {
         @Parameter(name = "enumClassName", value = "com.yamj.core.database.model.type.FileType")}),
     @TypeDef(name = "statusType",
             typeClass = EnumStringUserType.class,
             parameters = {
         @Parameter(name = "enumClassName", value = "com.yamj.common.type.StatusType")})
 })
 @Entity
 @Table(name = "stage_file")
 public class StageFile extends AbstractAuditable implements Serializable {
 
     private static final long serialVersionUID = -6247352843375054146L;
     @NaturalId(mutable = true)
    @Column(name = "file_name", nullable = false, length = 500)
     private String fileName;
     @NaturalId(mutable = true)
     @ManyToOne(fetch = FetchType.EAGER)
     @ForeignKey(name = "FK_FILE_DIRECTORY")
     @JoinColumn(name = "directory_id", nullable = false)
     private StageDirectory stageDirectory;
     @Temporal(value = TemporalType.TIMESTAMP)
     @Column(name = "file_date", nullable = false)
     private Date fileDate;
     @Column(name = "file_size", nullable = false)
     private long fileSize = -1;
     @Type(type = "fileType")
     @Column(name = "file_type", nullable = false, length = 30)
     private FileType fileType;
     @Type(type = "statusType")
     @Column(name = "status", nullable = false, length = 30)
     private StatusType status;
     @ManyToOne(fetch = FetchType.LAZY)
     @ForeignKey(name = "FK_FILE_MEDIAFILE")
     @Fetch(FetchMode.SELECT)
     @JoinColumn(name = "mediafile_id")
     private MediaFile mediaFile;
 
     // GETTER and SETTER
     public String getFileName() {
         return fileName;
     }
 
     public void setFileName(String fileName) {
         this.fileName = fileName;
     }
 
     public StageDirectory getStageDirectory() {
         return stageDirectory;
     }
 
     public void setStageDirectory(StageDirectory stageDirectory) {
         this.stageDirectory = stageDirectory;
     }
 
     public Date getFileDate() {
         return fileDate;
     }
 
     public void setFileDate(Date fileDate) {
         this.fileDate = fileDate;
     }
 
     public long getFileSize() {
         return fileSize;
     }
 
     public void setFileSize(long fileSize) {
         this.fileSize = fileSize;
     }
 
     public FileType getFileType() {
         return fileType;
     }
 
     public void setFileType(FileType fileType) {
         this.fileType = fileType;
     }
 
     public StatusType getStatus() {
         return status;
     }
 
     public void setStatus(StatusType status) {
         this.status = status;
     }
 
     public MediaFile getMediaFile() {
         return mediaFile;
     }
 
     public void setMediaFile(MediaFile mediaFile) {
         this.mediaFile = mediaFile;
     }
 
     // EQUALITY CHECKS
     @Override
     public int hashCode() {
         final int prime = 17;
         int result = 1;
         result = prime * result + (this.fileName == null ? 0 : this.fileName.hashCode());
         result = prime * result + (this.stageDirectory == null ? 0 : this.stageDirectory.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object other) {
         if (this == other) {
             return true;
         }
         if (other == null) {
             return false;
         }
         if (!(other instanceof StageFile)) {
             return false;
         }
         StageFile castOther = (StageFile) other;
 
         if (!StringUtils.equals(this.fileName, castOther.fileName)) {
             return false;
         }
 
         if (this.stageDirectory == null && castOther.stageDirectory == null) {
             return true;
         }
         if (this.stageDirectory == null) {
             return false;
         }
         if (castOther.stageDirectory == null) {
             return false;
         }
         return this.stageDirectory.equals(castOther.stageDirectory);
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("StageFile [ID=");
         sb.append(getId());
         sb.append(", fileName=");
         sb.append(getFileName());
         sb.append(", fileDate=");
         sb.append(getFileDate());
         sb.append(", fileSize=");
         sb.append(getFileSize());
         if (getStageDirectory() != null) {
             sb.append(", stageDirectory=");
             sb.append(getStageDirectory().getDirectoryPath());
         }
         sb.append("]");
         return sb.toString();
     }
 }
