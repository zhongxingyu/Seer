 /*
  *  Copyright (C) 2010 Daniel Waeber <wabu@inf.fu-berlin.de>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>
  */
 
 package de.javauni.jarcade.utils;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ObjectArrays;
 import java.util.AbstractList;
 import java.util.concurrent.atomic.AtomicInteger;
 import javax.annotation.CheckForNull;
 import javax.annotation.concurrent.GuardedBy;
 import javax.annotation.concurrent.ThreadSafe;
 
 /**
  *
  * @param <E>
  * @author Daniel Waeber <wabu@inf.fu-berlin.de>
  */
 @ThreadSafe
 public class IdList<E> extends AbstractList<E>{
     private final Object mutex = new Object();
 
     @GuardedBy("mutex")
     private E[] elems;
 
     @CheckForNull
     private E[] nexts;
 
     private final AtomicInteger ids = new AtomicInteger();
 
     public IdList() {
         this(128);
     }
 
     @SuppressWarnings("unchecked")
     public IdList(int initSize) {
         super();
         elems = (E[])new Object[initSize];
     }
 
     public <S extends E> S add(Function<Integer, S> construct) {
         AddHelper<E> h = add();
         S e = construct.apply(h.getId());
         h.set(e);
         return e;
     }
 
     public <S extends E> AddHelper<S> add(){
         final int id = ids.getAndIncrement();
         assureSize(id);
         return new AddHelper<S>() {
             @Override
             public void set(S e) {
                 IdList.this.set(id, e);
             }
             @Override
             public int getId() {
                 return id;
             }
         };
     }
 
     public int getNextId() {
         return ids.getAndIncrement();
     }
 
     @Override
     public void add(int i, E e) {
         assureSize(i);
         set(i, e);
     }
 
     @Override
     public E set(int i, E e) {
         E prev = null;
 
         E[] es = elems;
         if(es.length > i) {
             es[i] = e;
             prev = es[i];
         }
 
         E[] ns = nexts;
         if(ns != null) {
             ns[i] = e;
         }
 
         if(es.length <= i && ns==null) {
             throw new IndexOutOfBoundsException();
         }
 
         return prev;
     }
 
     private void assureSize(int id) {
         if(elems.length < id) {
             synchronized(mutex) {
                 if(elems.length < id) {
                     nexts = ObjectArrays.newArray(elems, elems.length*2);
                     System.arraycopy(elems, 0, nexts, 0, elems.length);
                     elems = nexts;
                     nexts = null; // NOPMD
                 }
             }
         }
     }
 
     @Override
     public E get(int index) {
         Preconditions.checkElementIndex(index, elems.length);
         return elems[index];
     }
 
     @Override
     public int size() {
         return ids.get();
     }
 }
 
 /* * * *
  * Correctness 
  * * * *
  * thread1:
  * 1.1:  nexts = new E[...]
  * 1.2:  copy(elems, nexts)
  * 1.3:  elems = nexts
  * 1.4:  nexts = null
  * * * *
  * thread2:
  * 2.1:  es = elems
  * 2.2:  if i<es
  *          es[i]=e
  * 2.3:  ns = nexts
  * 2.4:  if ns
  *          ns[i]=e
  * * * *
 * TODO: show that id list is threadsave or make it threadsave
  */
