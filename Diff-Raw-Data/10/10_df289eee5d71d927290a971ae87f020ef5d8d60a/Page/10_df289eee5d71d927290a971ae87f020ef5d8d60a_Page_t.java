 package org.otto.util;
 
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author damien bourdette <a href="https://github.com/dbourdette">dbourdette on github</a>
  * @version \$Revision$
  */
 public class Page<T> {
     private List<T> items = new ArrayList<T>();
 
     private int index;
 
     private int totalCount;
 
     private int pageSize;
 
     public static Page<DBObject> fromCursor(DBCursor cursor, Integer index, int pageSize) {
         if (index == null) {
            index = 1;
         }
 
        if (index < 1) {
            index = 1;
         }
 
         Page<DBObject> page = new Page<DBObject>();
 
        cursor = cursor.skip((index - 1) * pageSize).limit(pageSize);
 
         page.index = index;
         page.pageSize = pageSize;
         page.totalCount = cursor.count();
         page.items = cursor.toArray();
 
         return page;
     }
 
     private Page() {}
 
     public List<T> getItems() {
         return Collections.unmodifiableList(items);
     }
 
     public int getTotalCount() {
         return totalCount;
     }
 
     public long getPageCount() {
         return PageUtils.getPageCount(totalCount, pageSize);
     }
 
     public int getIndex() {
         return index;
     }
 
     public int getPageSize() {
         return pageSize;
     }
 }
