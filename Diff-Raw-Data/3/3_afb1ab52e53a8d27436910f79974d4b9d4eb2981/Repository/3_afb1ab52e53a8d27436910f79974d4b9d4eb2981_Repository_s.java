 package com.bibounde.gaemvnrepo.model;
 
 import java.util.List;
 
import javax.jdo.annotations.Extension;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.IdentityType;
 import javax.jdo.annotations.NullValue;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 @PersistenceCapable(identityType = IdentityType.APPLICATION)
 //Not supported by GAE @Unique(name="REPOSITORY_UNIQUE_IDX", members={"name"})
 public class Repository implements Disposable {
 
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Long id;
 
     @Persistent(nullValue=NullValue.EXCEPTION)
     private String name;
     
     @Persistent
     private boolean snapshots = false;
     
     @Persistent
    @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
     private List<File> files;
     
     @Persistent
     private boolean disposable;
 
     /**
      * @return the files
      */
     public List<File> getFiles() {
         return files;
     }
 
     /**
      * @param files the files to set
      */
     public void setFiles(List<File> files) {
         this.files = files;
     }
 
     /**
      * @return the id
      */
     public Long getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @return the snapshots
      */
     public boolean isSnapshots() {
         return snapshots;
     }
 
     /**
      * @param snapshots the snapshots to set
      */
     public void setSnapshots(boolean snapshots) {
         this.snapshots = snapshots;
     }
 
     /**
      * @return the disposable
      */
     public boolean isDisposable() {
         return disposable;
     }
 
     /**
      * @param disposable the disposable to set
      */
     public void setDisposable(boolean disposable) {
         this.disposable = disposable;
     }
 }
