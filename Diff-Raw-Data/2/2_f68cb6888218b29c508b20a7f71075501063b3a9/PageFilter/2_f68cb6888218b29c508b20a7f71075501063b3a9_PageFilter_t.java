 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.obozek.filterlib;
 
 import java.io.Serializable;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 
 /**
  *
  * @author infragile
  */
 public class PageFilter implements Pageable, Serializable {
 
     private static final long serialVersionUID = 8280485938848398236L;
     private int page;
     private int size;
     private Sort sort;
 
     /**
      * Creates a new {@link PageRequest}. Pages are zero indexed, thus providing
      * 0 for {@code page} will return the first page.
      *
      * @param size
      * @param page
      */
     public PageFilter(int page, int size) {
 
        this(page, size, null);
     }
 
     /**
      * Creates a new {@link PageRequest} with sort parameters applied.
      *
      * @param page
      * @param size
      * @param direction
      * @param properties
      */
     public PageFilter(int page, int size, Sort.Direction direction, String... properties) {
 
         this(page, size, new Sort(direction, properties));
     }
 
     /**
      * Creates a new {@link PageRequest} with sort parameters applied.
      *
      * @param page
      * @param size
      * @param sort
      */
     public PageFilter(int page, int size, Sort sort) {
 
         if (0 > page) {
             throw new IllegalArgumentException("Page index must not be less than zero!");
         }
 
         if (0 >= size) {
             throw new IllegalArgumentException("Page size must not be less than or equal to zero!");
         }
 
         this.page = page;
         this.size = size;
         this.sort = sort;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.springframework.data.domain.Pageable#getPageSize()
      */
     @Override
     public int getPageSize() {
 
         return size;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.springframework.data.domain.Pageable#getPageNumber()
      */
     @Override
     public int getPageNumber() {
 
         return page;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.springframework.data.domain.Pageable#getFirstItem()
      */
     @Override
     public int getOffset() {
 
         return page * size;
     }
 
     /**
      *
      * @param offset
      */
     public void setOffset(int offset) {
 
         page = offset / size;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.springframework.data.domain.Pageable#getSort()
      */
     @Override
     public Sort getSort() {
 
         return sort;
     }
 
     public int getPage() {
         return page;
     }
 
     public void setPage(int page) {
         this.page = page;
     }
 
     public int getSize() {
         return size;
     }
 
     public void setSize(int size) {
         this.size = size;
     }
 
     public void setSort(Sort sort) {
         this.sort = sort;
     }
 }
