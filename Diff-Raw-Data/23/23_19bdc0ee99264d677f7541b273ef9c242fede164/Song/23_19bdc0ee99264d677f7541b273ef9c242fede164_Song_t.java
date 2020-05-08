 package com.lastcrusade.fanclub.model;
 
 public class Song {

     private SongMetadata metadata;
 
     private long size;
 
     private byte[] data;
 
     private boolean fullRecord = false;
 
     public long getSize() {
         return size;
     }
 
     public void setSize(long size) {
         this.size = size;
     }
 
     public byte[] getData() {
         return data;
     }
 
     public void setData(byte[] data) {
         this.data = data;
         this.fullRecord = true;
     }
 
     public boolean isFullRecord() {
         return fullRecord;
     }
 
     public void setFullRecord(boolean fullRecord) {
         this.fullRecord = fullRecord;
     }
 
     public SongMetadata getMetadata() {
        return metadata;
     }

    public void setSongMetadata(SongMetadata metadata) {
         this.metadata = metadata;
     }
 }
