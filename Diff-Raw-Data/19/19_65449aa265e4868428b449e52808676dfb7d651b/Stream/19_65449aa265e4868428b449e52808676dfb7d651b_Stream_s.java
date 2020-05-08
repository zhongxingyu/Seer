 package com.gm375.vidshare;
 
 import java.util.ArrayList;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.haggle.Attribute;
 import org.haggle.DataObject;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 public class Stream {
     private ConcurrentHashMap<Integer, String> chunks;
     private ArrayList<String> tags;
     private String id;
     private String androidId;
     private String startTime;
     private long startTimeLong;
     private Bitmap thumbnail;
     
     // TODO: Add logic here that can detect when the last data object comes in, so the stream can be removed.
     // Also need logic here that deals with out of order data objects.
     // Cannot just destroy the stream when the last dObj comes in -- need to check we've displayed all of them.
     
     Stream(DataObject dObj) {
         Log.d(Vidshare.LOG_TAG, "*** Stream constructor *** creating stream ***");
         chunks = new ConcurrentHashMap<Integer, String>();
         id = dObj.getAttribute("id", 0).getValue();
         Log.d(Vidshare.LOG_TAG, "*** Stream constructor *** dObj ID = "+ id +" ***");
         
        // TODO: Start here 22/03/2010 - Redo this example with Android ID.
        // Continue testing and implementing Stream viewer.
        // ID example: 0000000000000000
         // ID length:  0123456789012345 length 16
         androidId = id.substring(0, 16);
         Log.d(Vidshare.LOG_TAG, "*** Stream constructor *** Android ID = "+ androidId +" ***");
         // Start time example: 1267652761287
         // Start time length:  6789012345678 length 13
         startTime = id.substring(16, 29);
         Log.d(Vidshare.LOG_TAG, "*** Stream constructor *** start Time = "+ startTime +" ***");
         
         startTimeLong = Long.parseLong(startTime);
         
         tags = new ArrayList<String>();
        Attribute[] attributes = dObj.getAttributes();
         
         for (Attribute attr : attributes) {
             if (attr.getName() == "tag") {
                 tags.add(attr.getValue());
             }
         }
         
         /*
         int size = (int) dObj.getThumbnailSize();
         byte[] data = new byte[size];
         dObj.getThumbnail(data);
         thumbnail = BitmapFactory.decodeByteArray(data, 0, size);
         */
     }
     
     public void addDataObject(DataObject dObj) {
         Integer seqNumber = Integer.decode(dObj.getAttribute("seqNumber", 0).getValue());
        String filepath = dObj.getFilePath();
        chunks.put(seqNumber, filepath);
     }
     
     public ArrayList<String> getTags() {
         return tags;
     }
     
    public String getMacAddress() {
         return androidId;
     }
     
     public String getStartTime() {
         return startTime;
     }
     
     public long getStartTimeLong() {
         return startTimeLong;
     }
     
     public Bitmap getThumbnail() {
         return thumbnail;
     }
     
     public int hashCode() {
         if (id != null) {
             return id.hashCode();
         }
         return 0;
     }
     
     public boolean equals(Object obj) {
         Stream object = (Stream) obj;
         if (id == object.id) {
             return true;
         }
         return false;
     }
 }
