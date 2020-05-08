 /*
  * libxjava -- utility library for cross-Java-platform development
  *             ${project.name}
  *
  * Copyright (c) 2010 Marcel Patzlaff (marcel.patzlaff@gmail.com)
  *
  * This library is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.libxjava.util;
 
 

 /**
  * Simple ring buffer set.
  * It uses the standard Java hash mechanism to detect already
  * inserted objects.
  * 
  * @author Marcel Patzlaff
  * @version ${project.artifactId} - ${project.version}
  */
 public class HashRingSet {
     private static final int LOAD_FACTOR = 75;
     
     private final HashRingSetEntry[] _table;
     private final Object[] _content;
     
     private int _count;
     private int _pos;
     
     public HashRingSet(int capacity) {
         _content= new Object[capacity];
         _table= new HashRingSetEntry[(100 * capacity) / LOAD_FACTOR];
         _count= 0;
         _pos= 0;
     }
     
     public boolean add(Object obj) {
         synchronized(_content) {
             int hash= obj.hashCode();
             int index= (hash & 0x7FFFFFFF) % _table.length;
             for (HashRingSetEntry e = _table[index] ; e != null ; e = e.next) {
                 if ((e.hash == hash) && _content[e.index].equals(obj)) {
                     return false;
                 }
             }
             
             HashRingSetEntry e= new HashRingSetEntry();
             e.hash= hash;
             e.index= _pos;
             e.next= _table[index];
             _table[index]= e;
 
             if(_count >= _content.length) {
                 if(!freePosition()) {
                     throw new Error("implementation bug");
                 }
             } else {
                 ++_count;
             }
             
             _content[_pos]= obj;
             _pos= (_pos + 1) % _content.length;
             
             return true;
         }
     }
     
     public boolean contains(Object obj) {
         synchronized(_content) {
             int hash= obj.hashCode();
             int index= (hash & 0x7FFFFFFF) % _table.length;
             for (HashRingSetEntry e = _table[index] ; e != null ; e = e.next) {
                 if ((e.hash == hash) && _content[e.index].equals(obj)) {
                     return true;
                 }
             }
             
             return false;
         }
     }
     
     public String toString() {
         synchronized(_content) {
             StringBuffer buffer= new StringBuffer();
             buffer.append('[');
             final int preLast= _count - 1;
             for(int c= 0, i= _content.length + _pos - _count; c <= preLast; ++c, ++i) {
                 buffer.append(String.valueOf(_content[i % _content.length]));
                 
                 if(c < preLast) {
                     buffer.append(", ");
                 }
             }
             
             buffer.append(']');
             return buffer.toString();
         }
     }
         
     private boolean freePosition() {
         int hash= _content[_pos].hashCode();
         int tableIndex= (hash & 0x7FFFFFFF) % _table.length;
         for (HashRingSetEntry e= _table[tableIndex], prev= null; e != null; prev=e, e= e.next) {
             if ((e.hash == hash) && (e.index == _pos)) {
                 if (prev != null) {
                     prev.next = e.next;
                 } else {
                     _table[tableIndex] = e.next;
                 }
                 
                 return true;
             }
         }
         
         return false;
     }
 }
 
 class HashRingSetEntry {
     int index;
     int hash;
     HashRingSetEntry next;
 }
