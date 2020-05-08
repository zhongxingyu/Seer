 /**
  * Copyright (C) 2010 Ian C. Smith <m4r35n357@gmail.com>
  *
  * This file is part of JavaEE6Webapp.
  *
  *     JavaEE6Webapp is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     JavaEE6Webapp is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with JavaEE6Webapp.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.me.doitto.webapp.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.me.doitto.webapp.dao.AbstractEntity;
 
 /**
  *
  * @param <T> the entity class
  * @author ian
  */
 @SuppressWarnings("serial")
 public abstract class ControllerBase<T extends AbstractEntity> implements IJsfCrud<Long>,IPager {
 
     protected enum Pager {
     	FIRST {
             @Override
             protected <U extends AbstractEntity> void setCurrentPage (final ControllerBase<U> base) {
                 base.pageNo = 0;
             }
         },
     	PREVIOUS {
             @Override
             protected <U extends AbstractEntity> void setCurrentPage (final ControllerBase<U> base) {
                 if (base.pageNo > 0) {
                     base.pageNo--;
                 }
             }
         },
     	NEXT {
             @Override
             protected <U extends AbstractEntity> void setCurrentPage (final ControllerBase<U> base) {
                 if (base.pageNo < base.getTotalPages()) {
                     base.pageNo++;
                 }
             }
         },
     	LAST {
             @Override
             protected <U extends AbstractEntity> void setCurrentPage (final ControllerBase<U> base) {
                 base.pageNo = base.getTotalPages();
             }
         };
 
         // nb. can't use the class type parameter T as enum instances are static, so we define a generic method 
         protected abstract <U extends AbstractEntity> void setCurrentPage (final ControllerBase<U> base);
     }
 
     protected T item;
 
     protected List<T> items = new ArrayList<T>();
     
     protected List<T> pagedItems = new ArrayList<T>();
 
     protected int pageNo = 0;
 
     protected int pagesize = 10;
 
     public T getItem () {
         return item;
     }
 
     public void setItem (final T item) {
         this.item = item;
     }
 
     public List<T> getItems () {
         return items;
     }
 
     public void setItems (final List<T> items) {
         this.items = items;
     }
 
     public List<T> getPagedItems () {
         return pagedItems;
     }
 
     public void setPagedItems (List<T> pagedItems) {
         this.pagedItems = pagedItems;
     }
 
     public int getPageNo () {
         return pageNo;
     }
 
     public void setPageNo (int pageNo) {
         this.pageNo = pageNo;
     }
 
     public int getPagesize () {
         return pagesize;
     }
 
     public int getTotalPages () {
         return items.size() / pagesize;
     }
 
     public void setPagesize (int pagesize) {
         this.pagesize = pagesize;
     }
 
     protected List<T> getFirstPage () {
         Pager.FIRST.setCurrentPage(this);
         return listPagedItems();
     }
 
     protected List<T> getPreviousPage () {
         Pager.PREVIOUS.setCurrentPage(this);
         return listPagedItems();
     }
 
     protected List<T> getNextPage () {
         Pager.NEXT.setCurrentPage(this);
         return listPagedItems();
     }
 
     protected List<T> getLastPage () {
         Pager.LAST.setCurrentPage(this);
         return listPagedItems();
     }
 
     protected List<T> listPagedItems () {
         ArrayList<T> list = new ArrayList<T>();
         for (int i = 0, j = pagesize; i < j; i++) {
             int itemNo = pageNo * pagesize + i;
             if (itemNo < items.size()) {
                 list.add(items.get(itemNo));
             } else {
                 break;
             }
         }
         return list;
     }
    
	@Override
	public boolean isFirst() {
		return pageNo == 0;
	}

	@Override
	public boolean isLast() {
		return pageNo == getTotalPages();
	}
 }
