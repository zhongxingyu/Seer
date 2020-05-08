 /*
  * Copyright (C) 2010, Zenoss Inc.  All Rights Reserved.
  */
 
 package org.zenoss.zep.dao;
 
 import org.zenoss.zep.ZepUtils;
 
 /**
  * Bean for storing the meta data associated with an event index.
  */
 public class IndexMetadata {
     private String zepInstance;
     private String indexName;
     private int indexVersion;
     private byte[] indexVersionHash;
 
     public IndexMetadata() {
     }
 
     public String getZepInstance() {
         return zepInstance;
     }
 
     public void setZepInstance(String zepInstance) {
         this.zepInstance = zepInstance;
     }
 
     public String getIndexName() {
         return indexName;
     }
 
     public void setIndexName(String indexName) {
         this.indexName = indexName;
     }
 
     public int getIndexVersion() {
         return indexVersion;
     }
 
     public void setIndexVersion(int indexVersion) {
         this.indexVersion = indexVersion;
     }
 
     public byte[] getIndexVersionHash() {
        return (this.indexVersionHash != null) ? this.indexVersionHash.clone() : null;
     }
 
    public void setIndexVersionHash(byte[] indexVersionHash) {
        this.indexVersionHash = (indexVersionHash != null) ? indexVersionHash.clone() : null;
     }
 
     @Override
     public String toString() {
         return "IndexMetadata{" +
                 "zepInstance='" + zepInstance + '\'' +
                 ", indexName='" + indexName + '\'' +
                 ", indexVersion=" + indexVersion +
                 ", indexVersionHash='" + ZepUtils.hexstr(indexVersionHash) + '\'' +
                 '}';
     }
 }
