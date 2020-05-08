 package com.testdroid.api;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  *
  * @author Sławomir Pawluk <slawomir.pawluk@bitbar.com>
  */
 public class APIQueryBuilder {
     /**
      * Defines APIList offset. Default value: 0
      */
     private int offset;
     /**
      * Defines APIList limit. DefaultValue: 10
      */
     private int limit;
     /**
      * Defines APIList search. Default value: null
      */
     private String search;
     /**
      * Defines APIList sort. Default value: null
      */
     private APISort sort;
 
     public APIQueryBuilder() {
         this.offset = 0;
         this.limit = 10;
         this.search = null;
         this.sort = null;
     }
     
     public APIQueryBuilder offset(int offset) {
         this.offset = offset;
         return this;
     }
     
     public APIQueryBuilder limit(int limit) {
         if (limit > 0) {
             this.limit = limit;
         }
         return this;
     }
     
     public APIQueryBuilder search(String search) {
        this.search = search.replaceAll(" ", "%20");
         return this;
     }
     
     public APIQueryBuilder sort(Class<? extends APIEntity> type, APISort.SortItem... sortItems) {
         this.sort = APISort.create(type, sortItems);
         return this;
     }
     
     protected String build(String uri) {
         String formatString = "%s?limit=%s&offset=%s&search=%s&sort=%s";
         if(uri.contains("?")) {
             formatString = formatString.replace("?", "&");
         }
         return String.format(formatString, uri, limit, offset, 
                 StringUtils.isNotBlank(search) ? APIEntity.encodeURL(search) : "", 
                 sort !=null ? sort.serialize() : "");
     }
 }
