 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package edu.rpi.cct.webdav.servlet.shared;
 
import java.util.List;
 
 /** Data for Synch Report
  *
  *   @author Mike Douglass   douglm@rpi.edu
  */
 public class WdSynchReport {
   /** The changed entity may be an event or a collection. If it is deleted then
    * it will be marked as tombstoned.
    *
    * @author douglm
    */
   public static class WdSynchReportItem implements Comparable<WdSynchReportItem> {
     /**
      */
     public String lastmod;
 
     /** The changed node
      */
     public WebdavNsNode node;
 
     /**
      * @param node
      * @throws WebdavException
      */
     public WdSynchReportItem(final WebdavNsNode node) throws WebdavException {
       this.node = node;
       lastmod = node.getLastmodDate();
     }
 
     /* (non-Javadoc)
      * @see java.lang.Comparable#compareTo(java.lang.Object)
      */
     @Override
     public int compareTo(final WdSynchReportItem that) {
       return lastmod.compareTo(that.lastmod);
     }
 
     @Override
     public int hashCode() {
       return lastmod.hashCode();
     }
 
     @Override
     public boolean equals(final Object o) {
       return compareTo((WdSynchReportItem)o) == 0;
     }
   }
 
   /**
    */
  public List<WdSynchReportItem> items;
 
   /** True if the report was truncated
    */
   public boolean truncated;
 
   /** Token for next time.
    */
   public String token;
 }
