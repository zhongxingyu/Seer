 package com.rameses.rcp.common;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public abstract class PageListModel extends AbstractListModel {
     
     protected int pageIndex = 1;
     protected int pageCount = 1;
     
     //this indicates the absolute row pos;
     protected int toprow;
     protected int maxRows = -1;
     
     //-1 means it has not been initialized yet
     protected int minlimit = 0;
     protected int maxlimit = 0;
     protected String search;
     
     
     /**
      * forceLoad is used to force the loading without emptying the dataList
      */
     protected boolean forceLoad;
     
     public PageListModel() {
     }
     
     public void load() {
         toprow = 0;
         minlimit = 0;
         maxlimit = 0;
         maxRows = -1;
         pageIndex = 1;
         pageCount = 1;
         super.load();
     }
     
     
     public String getSearch() {
         return search;
     }
     
     public void setSearch(String search) {
         this.search = search;
     }
     
     protected void fetch() {
         
         if( dataList ==null || toprow < minlimit || toprow+getRows() > maxlimit || forceLoad ) {
             
             minlimit = toprow - getRows();
             if(minlimit<0) minlimit = 0;
             
             //add extra row to see if this is last page.
             int fetchRows = (getRows() * 3) + 1;
             Map m = new HashMap();
             m.put( "_toprow", toprow );
             
             m.put("_search", search);
             m.put("_start", minlimit );
             m.put("_rowsize", fetchRows );
             m.put("_limit", fetchRows );
             dataList = fetchList(m);
             
             boolean lastPage = true;
             
             if( dataList.size() >= fetchRows ) {
                 lastPage = false;
                 dataList.remove(dataList.size()-1);
             }
             
             //calculate the maximum number of rows first.
             int tmpMaxRows = minlimit + dataList.size()-1;
             if(isAllocNewRow()) tmpMaxRows = tmpMaxRows + 1;
             if( tmpMaxRows > maxRows ) {
                 maxRows = tmpMaxRows;
             }
             
             //calculate the maximum limit to trigger next fetch.
             maxlimit = toprow + (getRows()*2)-1;
             if(maxlimit>maxRows) maxlimit = maxRows;
             
             
             //determine total page count. add extra page if not yet last page.
             pageCount = ((maxRows+1)/getRows()) + ( ((maxRows+1)%getRows())>0?1:0 );
         }
         
         //reset the paging info
         if(selectedItem!=null) {
             //pageIndex = (selectedItem.getRownum()/ getRows())+1;
             pageIndex = (toprow / getRows())+1;
         } else {
             pageIndex = 1;
             setSelectedItem(0);
         }
         
         
         //reset the force load.
         List subList = null;
         if( dataList.size() > 0 ) {
             int start = toprow - minlimit;
             int tail = start + getRows();
             if( tail > dataList.size() ) tail = dataList.size();
             subList = dataList.subList(start, tail);
         } else {
             subList = new ArrayList();
         }
         fillListItems(subList,toprow);
         
         forceLoad = false;
     }
     
     public void addItem(Object item) {
         if( item instanceof ListItem )
             throw new IllegalStateException("SubListModel.addItem error. Item passed must not be a ListItem");
         
         onAddItem(item);
         forceLoad = true;
         refresh();
     }
     
     
     public void removeItem(Object item) {
         if( item instanceof ListItem )
             throw new IllegalStateException("SubListModel.removeItem error. Item passed must not be a ListItem");
         
         onRemoveItem( item );
         forceLoad = true;
         refresh();
         if(getSelectedItem().getIndex()>0 && getSelectedItem().getState()==0) {
             setSelectedItem( items.get(getSelectedItem().getIndex()-1) );
         }
     }
     
     
     
     /**
      * for moveNextPage,moveBackPage we need to force the loading.
      * for moveNextRecord and moveBackRecord, we shouls not force the load.
      * if maxRows < 0 meaning the maxRows was not determined.
      */
     public void moveNextRecord() {
         //do not scroll when there are error in validation
         if( super.hasErrorMessages() ) return;
         
         //do not do anything if list size is the same and there is no createItem.
         if( dataList!=null && dataList.size()==getRows() && !isAllocNewRow() ) {
             return;
         }
         
         if(maxRows <0 || toprow < maxRows) {
             toprow++;
             refresh();
             refreshSelectedItem();
         }
     }
     
     public void moveBackRecord() {
         //do not scroll when there are error in validation
         if( super.hasErrorMessages() ) return;
         
         if(toprow-1>=0) {
             toprow--;
             refresh();
             refreshSelectedItem();
         }
     }
     
     public void moveNextPage() {
         //do not scroll when there are error in validation
         if( super.hasErrorMessages() ) return;
         
         if(!isLastPage()) {
             toprow = toprow+getRows();
             forceLoad = true;
             refresh();
             refreshSelectedItem();
         }
     }
     
     public void moveBackPage() {
         //do not scroll when there are error in validation
         if( super.hasErrorMessages() ) return;
         
         if(toprow-getRows() >= 0 ) {
             toprow = toprow-getRows();
             forceLoad = true;
             refresh();
             refreshSelectedItem();
         }
     }
     
     public void moveFirstPage() {
         //do not scroll when there are error in validation
         if( super.hasErrorMessages() ) return;
         
         toprow = 0;
         selectedItem = null;
         forceLoad = true;
         refresh();
         refreshSelectedItem();
     }
     
     /**
      * this method sets the top row.
      * check first if the top row is possible.
      * if the toprow value is not possible,
      * do nothing. toprow is possible only if
      * it it does not exceed getRowCount() - getRows()
      */
     public void setTopRow( int t ) {
         //do not scroll when there are error in validation
         if( super.hasErrorMessages() ) return;
         
         //if the toprow is current do not proceed.
         if( t == toprow)
             return;
         if( t <= getMaxRows()  ) {
             toprow = t;
             forceLoad = true;
             refresh();
         }
     }
     
     public int getTopRow() {
         return toprow;
     }
     
     public final void doSearch() {
         load();
     }
     
     public int getPageIndex() {
         return pageIndex;
     }
     
     public int getRowCount() {
         if(isAllocNewRow()) {
             return maxRows - 1;
         }
         return maxRows;
     }
     
     public boolean isLastPage() {
         return pageIndex >= pageCount;
     }
     
     /**
      * This function is used internally to check if we need to allocate
      * a new row for new item. If true, this will add an extra row in the
      * list.
      */
     private Boolean allocNewRow;
     protected boolean isAllocNewRow() {
         if(allocNewRow==null) {
             if(createItem()!=null) {
                 allocNewRow = new Boolean(true);
             } else {
                 allocNewRow = new Boolean(false);
             }
         }
         return allocNewRow.booleanValue();
     }
     
     public int getMaxRows() {
         return maxRows;
     }
     
     public int getPageCount() {
         return pageCount;
     }
     
 }
