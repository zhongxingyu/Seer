 /*
  * This is a utility project for wide range of applications
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.rest.atom;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * Read entities from a feed in background within the constructor to form a list. The list can put a cap on how many at
  * most it can hold. The wrapper provided should point to the feed which is the 1st page of result; that is only going
  * next one could discover all the entities.
  * @author imyousuf
  */
 public class PaginatedFeedEntitiesList<T> extends AbstractList<T> {
 
   private final PaginatedEntitiesWrapper<T> rootWrapper;
   private PaginatedEntitiesWrapper<T> currentWrapper;
   private final int max;
   private final ArrayList<T> backedupList;
   private final Object object;
   private boolean working = false;
   private final ExecutorService service;
 
   /**
    * Same as calling <code>new PaginatedFeedEntitiesList(wrapper, Integer.MAX_VALUE);</code>
    * @see PaginatedFeedEntitiesList#PaginatedFeedEntitiesList(com.smartitengineering.util.rest.atom.PaginatedEntitiesWrapper, int) 
    */
   public PaginatedFeedEntitiesList(PaginatedEntitiesWrapper<T> wrapper) throws Exception {
     this(wrapper, Integer.MAX_VALUE);
   }
 
   /**
    * Construct the list with the root feed wrapper and the maximum number of entities to read.
    * @param wrapper Wrapper wrapping the root feed of an collection of entities.
    * @param max Maximum number entities to have.
    * @throws If wrapper is null
    */
   public PaginatedFeedEntitiesList(PaginatedEntitiesWrapper<T> wrapper, int max) throws Exception {
     if (wrapper == null) {
       throw new IllegalArgumentException("Wrapper can not be null!");
     }
     this.rootWrapper = wrapper;
     currentWrapper = rootWrapper;
     if (ClientUtil.isOpenSearchTotalResultPresent(rootWrapper.getRootFeed())) {
       int size = ClientUtil.getOpenSearchTotalResult(rootWrapper.getRootFeed());
       backedupList = new ArrayList<T>(Math.min(max, size));
     }
     else {
       backedupList = new ArrayList<T>();
     }
     this.max = max;
     this.object = new Object();
     service = Executors.newSingleThreadExecutor();
     service.execute(new EntityLoader());
   }
 
   @Override
   public T get(int index) {
     tryAndWait();
     if (index < size()) {
       return backedupList.get(index);
     }
     else {
       throw new IndexOutOfBoundsException("Size is " + size() + " but request index is " + index);
     }
   }
 
   @Override
   public int size() {
     tryAndWait();
     return backedupList.size();
   }
 
   private void tryAndWait() {
     if (working) {
       synchronized (object) {
         try {
           object.wait();
         }
         catch (Exception ex) {
           ex.printStackTrace();
         }
       }
     }
   }
 
   private class EntityLoader implements Runnable {
 
     public void run() {
       while (currentWrapper != null) {
         backedupList.addAll(currentWrapper.getEntitiesForCurrentPage());
         if (backedupList.size() >= max) {
           currentWrapper = null;
         }
         else {
           currentWrapper = currentWrapper.next();
         }
       }
       synchronized (object) {
         try {
           object.notifyAll();
         }
         catch (Exception ex) {
           ex.printStackTrace();
         }
       }
     }
   }
 }
