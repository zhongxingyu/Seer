 package com.svanberg.household.web.spring;
 
 import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 
 /**
  * @author Andreas Svanberg (andreass) <andreas.svanberg@mensa.se>
  */
 public class DataProviderPage implements Pageable {
 
     private final long offset;
     private final long count;
     private final SortParam sort;
 
     public DataProviderPage(final long offset, final long count, final SortParam sort) {
         this.offset = offset;
         this.count = count;
         this.sort = sort;
     }
 
     @Override
     public int getPageNumber() {
         return (int) (offset / count);
     }
 
     @Override
     public int getPageSize() {
         return (int) count;
     }
 
     @Override
     public int getOffset() {
         return (int) offset;
     }
 
     @Override
     public Sort getSort() {
        if (sort == null)
            return null;

         return new Sort(
                 sort.isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                 sort.getProperty().toString());
     }
 }
