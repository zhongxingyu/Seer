 /*
 * Copyright 2012, CMM, University of Queensland.
 *
 * This file is part of Paul.
 *
 * Paul is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paul is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paul. If not, see <http://www.gnu.org/licenses/>.
 */
 package au.edu.uq.cmm.paul.grabber;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class Group implements Comparable<Group> {
     private final String basePathname;
     DatasetMetadata inFolder;
     private List<DatasetMetadata> allInDatabase = new ArrayList<DatasetMetadata>();
     
     public Group(String basePathname) {
         super();
         this.basePathname = basePathname;
     }
 
     public final String getBasePathname() {
         return basePathname;
     }
 
     public final DatasetMetadata getInFolder() {
         return inFolder;
     }
 
     public final List<DatasetMetadata> getAllInDatabase() {
         return allInDatabase;
     }
     
     public final List<DecoratedDatasetMetadata> getAllDecorated() {
         List<DecoratedDatasetMetadata> res = 
                 new ArrayList<DecoratedDatasetMetadata>(allInDatabase.size() + 1);
         if (inFolder != null) {
             res.add(new DecoratedDatasetMetadata(inFolder, inFolder, this));
         }
         for (DatasetMetadata dataset : allInDatabase) {
             res.add(new DecoratedDatasetMetadata(dataset, inFolder, this));
         }
         return res;
     }
     
     public final boolean isUnmatchedInDatabase() {
         return getMatchedDataset() == null;
     }
     
     public final DatasetMetadata getMatchedDataset() {
         if (inFolder != null) {
             for (DatasetMetadata dataset : allInDatabase) {
                if (!Analyser.matches(dataset, inFolder)) {
                     return dataset;
                 }
             }
         }
         return null;
     }
 
     public final boolean isDuplicatesInDatabase() {
         if (inFolder == null) {
             return false;
         }
         int count = 0;
         for (DatasetMetadata dataset : allInDatabase) {
             if (Analyser.matches(dataset, inFolder)) {
                 count++;
             }
         }
         return count > 1;
     }
     
     public final void setInFolder(DatasetMetadata inFolder) {
         this.inFolder = inFolder;
     }
     
     public final void addInDatabase(DatasetMetadata inDatabase) {
         this.allInDatabase.add(inDatabase);
     }
 
     @Override
     public int compareTo(Group o) {
         return basePathname.compareTo(o.getBasePathname());
     }
 
     public DatasetMetadata getLatestInDatabase() {
         Iterator<DatasetMetadata> it = allInDatabase.iterator();
         if (it.hasNext()) {
             DatasetMetadata latest = it.next();
             while (it.hasNext()) {
                 DatasetMetadata dataset = it.next();
                 if (dataset.getUpdateTimestamp().getTime() > latest.getUpdateTimestamp().getTime()) {
                     latest = dataset;
                 }
             }
             return latest;
         } else {
             return null;
         }
     }
 
     @Override
     public String toString() {
         return "Group [basePathname=" + basePathname + ", inFolder=" + inFolder
                 + ", allInDatabase=" + allInDatabase + "]";
     }
 }
