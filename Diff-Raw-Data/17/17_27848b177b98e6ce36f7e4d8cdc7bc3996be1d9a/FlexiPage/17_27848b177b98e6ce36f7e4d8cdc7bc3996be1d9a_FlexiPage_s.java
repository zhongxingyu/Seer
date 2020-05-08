 /*
  * This file (FlexiPage.java) is part of the Echolot Project (hereinafter "Echolot").
  * Copyright (C) 2008-2010 eXXcellent Solutions GmbH.
  *
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  */
 
 package de.exxcellent.echolot.model.flexi;
 
 import java.io.Serializable;
 import java.util.Arrays;
 
 /**
  *
  * @author sieskei (XSoft Ltd.)
  */
 public class FlexiPage implements Serializable {
     private static final long serialVersionUID = 201110109l;
     
     private final int page;
     private final int total;
     
     private final FlexiRow[] rows;
 
     public FlexiPage(int page, int total, FlexiRow[] rows) {
         this.page = page;
         this.total = total;
         this.rows = rows;
     }
 
     public int getPage() {
         return page;
     }
 
     public int getTotal() {
         return total;
     }
 
     public FlexiRow[] getRows() {
         return rows;
     }
     
     public Integer[] getRowsIds() {
       Integer[] ids = new Integer[rows.length];
       for(int r = 0; r < rows.length; r++) {
         ids[r] = rows[r].getId();
       }
       return ids;
     }
 
     @Override
     public int hashCode() {
         return page;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final FlexiPage other = (FlexiPage) obj;
         if (this.page != other.page) {
             return false;
         }
         if (this.total != other.total) {
             return false;
         }
         if (!Arrays.deepEquals(this.rows, other.rows)) {
             return false;
         }
         return true;
     }
 }
