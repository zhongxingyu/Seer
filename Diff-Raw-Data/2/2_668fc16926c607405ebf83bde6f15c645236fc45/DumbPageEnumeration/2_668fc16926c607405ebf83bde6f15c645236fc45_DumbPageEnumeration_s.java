 package org.melati.util;
 
 import java.util.*;
 
 public class DumbPageEnumeration implements PageEnumeration {
   
   private int pageStart, pageSize;
   private Vector page;
   private int totalCount;
   private boolean totalCountIsMinimum;
   private Enumeration us;
 
   public DumbPageEnumeration(Enumeration base,
                              int pageStart, int pageSize, int countHorizon) {
     this.pageStart = Math.max(pageStart, 1);
     this.pageSize = pageSize;
     int c = EnumUtils.skip(base, pageStart - 1);
     page = EnumUtils.initial(base, pageSize);
     totalCount = c + page.size() +
                      EnumUtils.skip(base, countHorizon - (c + page.size()));
     totalCountIsMinimum = base.hasMoreElements();
     us = page.elements();
   }
 
   public DumbPageEnumeration(SkipEnumeration base,
                              int pageStart, int pageSize, int countHorizon) {
     this((Enumeration)base, pageStart, pageSize, countHorizon);
   }
 
   // 
   // -------------
   //  Enumeration
   // -------------
   // 
 
   public boolean hasMoreElements() {
     return us.hasMoreElements();
   }
 
   public Object nextElement() {
     return us.nextElement();
   }
 
   // 
   // -----------------
   //  PageEnumeration
   // -----------------
   // 
 
   public int getPageStart() {
     return pageStart;
   }
 
   public int getPageSize() {
     return page.size();
   }
 
   public int getPageEnd() {
     return pageStart + page.size() - 1;
   }
 
   public int getTotalCount() {
     return totalCount;
   }
 
   public boolean getTotalCountIsMinimum() {
     return totalCountIsMinimum;
   }
 
   public Integer getPrevPageStart() {
     int it = pageStart - pageSize;
     return it < 0 ? null : new Integer(it);
   }
 
   public Integer getNextPageStart() {
     int it = pageStart + pageSize;
    return totalCountIsMinimum || it < totalCount ? new Integer(it) : null;
   }
 }
