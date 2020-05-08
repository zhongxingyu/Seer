 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.servlet;
 
 import java.util.Vector;
 
 /**
  * @author Eva Indal
  * @version $Revision$
  *
  */
 public class StatisticsCollection {
     private String orgname = "";
     private Vector stats = new Vector(); 
     private Vector allmonths = new Vector();
     
     public StatisticsCollection(final String orgname_in) {
         this.orgname = orgname_in;
     }
     public String getOrgName() {
         return this.orgname;
     }
     public int getNumMonths() {
         return this.allmonths.size();
     }
     public String getMonthName(final int idx) {
        StatisticsMonth month = (StatisticsMonth) this.allmonths.get(idx);
        return month.getMonthName();
     }
     public int getNumStatisticsData() {
         return this.stats.size();
     }
     public StatisticsData getStatisticsData(final int idx) {
         return (StatisticsData) this.stats.get(idx);
     }
     public void addMonth(final String name, final String month, final int count) {
         this.addUniqueMonth(month);
         for (int i = 0; i < this.stats.size(); i++) {
             StatisticsData data = (StatisticsData) this.stats.get(i);
             if (data.getName().equals(name)) {
                 data.addMonth(month, count);
                 return;
             }
         }
         StatisticsData data = new StatisticsData();
         data.setName(name);
         data.addMonth(month, count);
         this.stats.add(data);
     }
     
     /**
      * Adds a month to the list of unique months (if not already in list)
      * 
      * @param monthname
      */
     private void addUniqueMonth(final String monthname) {
        StatisticsMonth month = new StatisticsMonth(monthname);
         final int n = this.allmonths.size();
        
        // first check if month is already in Vector
         for (int i = 0; i < n; i++) {
            StatisticsMonth tmp = (StatisticsMonth) this.allmonths.get(i);
            if (month.equals(tmp)) return;
         }
        // insert the month in sorted order
        for (int i = 0; i < n; i++) {
            StatisticsMonth tmp = (StatisticsMonth) this.allmonths.get(i);
            if (month.getMonthNum() <= tmp.getMonthNum()) {
                this.allmonths.add(i, month);
                return;
            }
        }
        // just add at end of Vector if we get here
        this.allmonths.add(month);
     }
 }
