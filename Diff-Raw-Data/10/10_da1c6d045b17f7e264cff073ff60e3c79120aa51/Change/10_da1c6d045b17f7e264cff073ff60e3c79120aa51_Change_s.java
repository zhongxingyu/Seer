 package de.consistec.doubleganger.common.data;
 
 /*
  * #%L
  * Project - doppelganger
  * File - Change.java
  * %%
  * Copyright (C) 2011 - 2013 consistec GmbH
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 import static de.consistec.doubleganger.common.util.CollectionsUtil.newHashMap;
 
 import de.consistec.doubleganger.common.util.HashCalculator;
 
 import java.io.Serializable;
 import java.security.NoSuchAlgorithmException;
 import java.util.Comparator;
 import java.util.Map;
 
 /**
  * Represents a single database base row.
  * Used for internal communication between framework components
  *
  * @author Markus Backes
  * @company consistec Engineering and Consulting GmbH
  * @date unknown
  * @serial
  * @since 0.0.1-SNAPSHOT
  */
 public class Change implements Serializable {
 
     /**
      * A Comparator which compares to Change objects through their primarey keys.
      */
     private static PrimaryKeyComparator pkComparator = new PrimaryKeyComparator();
     /**
      * The message digest entry for this change.
      * Not null.
      * <p/>
      *
      * @serial
      */
     private MDEntry mdEntry;
     /**
      * The row data.
      * Not null.
      * <p/>
      *
      * @serial
      */
     private Map<String, Object> rowData = newHashMap();
 
     /**
      * Instantiates a new change.
      */
     public Change() {
     }
 
     /**
      * Instantiates a new change populated with provided data.
      *
      * @param mdEntry the md entry
      * @param rowData the row data
      */
     public Change(MDEntry mdEntry, Map<String, Object> rowData) {
         this.mdEntry = mdEntry;
         this.rowData = rowData;
     }
 
     /**
      * Gets the md entry.
      *
      * @return the md entry
      */
     public MDEntry getMdEntry() {
         return mdEntry;
     }
 
     /**
      * Sets the md entry.
      *
      * @param mdEntry the new md entry
      */
     public void setMdEntry(MDEntry mdEntry) {
         this.mdEntry = mdEntry;
     }
 
     /**
      * Gets the row data.
      *
      * @return the row data
      */
     public Map<String, Object> getRowData() {
         return rowData;
     }
 
     /**
      * Sets the row data.
      *
      * @param rowData the row data
      */
     public void setRowData(Map<String, Object> rowData) {
         this.rowData = rowData;
     }
 
     /**
      * Brief description of instance state.
      * Result looks like {@code Change{mdEntry=MDEntry{...}, rowData=[]}} but could be changed in future releases.
      *
      * @return Object's state short description.
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return getClass().getSimpleName() + "{ mdEntry="
            + mdEntry.toString()
             + ", rowData= "
            + rowData.toString()
             + " }";
     }
 
     /**
      * Computes the doubleganger hash value for this change.
      * <p/>
      *
      * @return hash value as string
      * @throws NoSuchAlgorithmException
      */
     public String calculateHash() throws NoSuchAlgorithmException {
         return new HashCalculator().getHash(rowData);
     }
 
     @Override
     public boolean equals(Object object) { //NOSONAR
 
         if (this == object) {
             return true;
         }
         if (object == null || getClass() != object.getClass()) {
             return false;
         }
 
         Change other = (Change) object;
 
         if (mdEntry != null ? !mdEntry.equals(other.mdEntry) : other.mdEntry != null) {
             return false;
         }
 
         if (rowData != null && other.rowData != null) {
             if (!rowData.keySet().equals(other.rowData.keySet())) {
                 return false;
             }
             for (Map.Entry<String, Object> entry : rowData.entrySet()) {
 
                 String key = entry.getKey();
 
                 if (other.getRowData().get(key) == null && entry.getValue() == null) {
                     //needed to prevent nullpointerexception
                     continue;
                 } else if (other.getRowData().get(key) == null ^ entry.getValue() == null) {
                     return false;
                 } else if (!other.getRowData().get(key).toString().equals(entry.getValue().toString())) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = mdEntry == null ? 0 : mdEntry.hashCode();
         result = 31 * result + (rowData == null ? 0 : rowData.hashCode());
         return result;
     }
 
     /**
      * Returns the primary key comparator of the Change class.
      *
      * @return PrimaryKeyComparator
      */
     public static final PrimaryKeyComparator getPrimaryKeyComparator() {
         return pkComparator;
     }
 
     private static class PrimaryKeyComparator implements Comparator<Change> {
 
         @Override
         public int compare(final Change o1, final Change o2) {
             if (o1.getMdEntry() == null || o2.getMdEntry() == null) {
                 throw new IllegalStateException("md entry must not be null!");
             }
             int pk1 = Integer.valueOf(o1.getMdEntry().getPrimaryKey().toString()).intValue();
             int pk2 = Integer.valueOf(o2.getMdEntry().getPrimaryKey().toString()).intValue();
 
             if (pk1 == pk2) {
                 return 0;
             } else if (pk1 > pk2) {
                 return 1;
             } else {
                 return -1;
             }
         }
     }
 }
