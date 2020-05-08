 /*
  * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
  * This is a java.net project, see https://jets3t.dev.java.net/
  * 
  * Copyright 2008 James Murty
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package org.jets3t.apps.cockpit.gui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import javax.swing.table.DefaultTableModel;
 
 import org.jets3t.service.model.S3Bucket;
 
 /**
  * A table model to store {@link S3Bucket}s.
  * 
  * @author James Murty
  */
 public class BucketTableModel extends DefaultTableModel {
     private static final long serialVersionUID = -2486904365563130393L;
     
     ArrayList bucketList = new ArrayList();
     
     public BucketTableModel() {
         super(new String[] {"Bucket Name"}, 0);
     }
     
     public int addBucket(S3Bucket bucket) {
         int insertRow = 
             Collections.binarySearch(bucketList, bucket, new Comparator() {
                 public int compare(Object o1, Object o2) {
                     String b1Name = ((S3Bucket)o1).getName();
                     String b2Name = ((S3Bucket)o2).getName();
                    int result =  b1Name.compareToIgnoreCase(b2Name);
                     return result;
                 }
             });
         if (insertRow >= 0) {
             // We already have an item with this key, replace it.
             bucketList.remove(insertRow);
             this.removeRow(insertRow);                
         } else {
             insertRow = (-insertRow) - 1;                
         }
         // New object to insert.
         bucketList.add(insertRow, bucket);
         this.insertRow(insertRow, new Object[] {bucket.getName()});
         return insertRow;
     }
     
     public void removeBucket(S3Bucket bucket) {
         int index = bucketList.indexOf(bucket);
         this.removeRow(index);
         bucketList.remove(bucket);
     }
     
     public void removeAllBuckets() {
         int rowCount = this.getRowCount();
         for (int i = 0; i < rowCount; i++) {
             this.removeRow(0);
         }
         bucketList.clear();
     }
     
     public S3Bucket getBucket(int row) {
         return (S3Bucket) bucketList.get(row);
     }
     
     public S3Bucket[] getBuckets() {
         return (S3Bucket[]) bucketList.toArray(new S3Bucket[bucketList.size()]);
     }
     
     public boolean isCellEditable(int row, int column) {
         return false;
     }
     
 }
