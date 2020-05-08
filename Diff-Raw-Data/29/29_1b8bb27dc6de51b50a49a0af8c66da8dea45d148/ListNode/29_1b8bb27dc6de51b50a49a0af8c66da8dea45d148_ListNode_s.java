 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.core;
 
 import com.objectgraph.core.ListChange.ListChangeType;
 
 import java.util.*;
 
 /**
  *
  * @param <E>
  */
 public class ListNode<E> extends Node implements List<E> {
 
     private final List<E> list = new ArrayList<>();
 
     private final Class<E> elementType;
 
     private final List<String> properties = new ArrayList<>();
 
     /**
      *
      * @param elementType
      */
     public ListNode(Class<E> elementType) {
         this.elementType = elementType;
     }
 
     /**
      *
      * @param elementType
      * @param elements
      */
     @SafeVarargs
     public ListNode(Class<E> elementType, E... elements) {
         this.elementType = elementType;
         addAll(Arrays.asList(elements));
     }
 
     @Override
     public boolean add(E e) {
         return addAll(list.size(), Collections.singleton(e));
     }
 
     @Override
     public void add(int index, E element) {
         addAll(index, Collections.singleton(element));
     }
 
     @Override
     public boolean addAll(Collection<? extends E> c) {
         return addAll(list.size(), c);
     }
 
     @Override
     public boolean addAll(int index, Collection<? extends E> c) {
         if (c.isEmpty()) {
             return false;
         }
 
         List<E> elements = new ArrayList<>(c);
         List<Integer> indices = new ArrayList<>(c.size());
 
         for (E element : c) {
             list.add(index, element);
             if (element instanceof Node) {
                 ((Node) element).addParentPath(this, String.valueOf(index));
             }
             indices.add(index++);
         }
 
         for (; index < list.size(); index++) {
             E current = list.get(index);
             if (current instanceof Node) {
                 String oldPath = String.valueOf(index - indices.size());
                 String newPath = String.valueOf(index);
                 ((Node) current).removeParentPath(this, oldPath);
                 ((Node) current).addParentPath(this, newPath);
             }
         }
 
         updatePropertyList();
 
         fireEvent(new Event("", new ListChange(ListChangeType.ADD, this, elements, indices)));
 
         return true;
     }
 
     @Override
     public void clear() {
         List<Integer> indices = new ArrayList<>(list.size());
         List<E> elements = new ArrayList<>(list);
         int index = 0;
         while (!list.isEmpty()) {
             E element = list.remove(0);
 
             String path = String.valueOf(index);
             if (element instanceof Node) {
                 ((Node) element).removeParentPath(this, path);
             }
             indices.add(index++);
         }
 
         if (!elements.isEmpty()) {
             updatePropertyList();
             fireEvent(new Event("", new ListChange(ListChangeType.REMOVE, this, elements, indices)));
         }
     }
 
     @Override
     public boolean contains(Object o) {
         return list.contains(o);
     }
 
     @Override
     public boolean containsAll(Collection<?> c) {
         return list.containsAll(c);
     }
 
     @Override
     public E get(int index) {
         return list.get(index);
     }
 
     @Override
     public int indexOf(Object o) {
         return list.indexOf(o);
     }
 
     @Override
     public boolean isEmpty() {
         return list.isEmpty();
     }
 
     @Override
     public Iterator<E> iterator() {
         return listIterator(0);
     }
 
     @Override
     public int lastIndexOf(Object o) {
         return list.lastIndexOf(o);
     }
 
     @Override
     public ListIterator<E> listIterator() {
         return listIterator(0);
     }
 
     @Override
     public ListIterator<E> listIterator(int index) {
         return new ListNodeIterator<>(this, list.listIterator(index));
     }
 
     @Override
     public boolean remove(Object o) {
         int index = list.indexOf(o);
         if (index < 0) {
             return false;
         } else {
             remove(index);
         }
         return true;
     }
 
     @Override
     public E remove(int index) {
         E element = list.remove(index);
 
         if (element instanceof Node) {
             ((Node) element).removeParentPath(this, String.valueOf(index));
         }
 
         for (int i = index; i < list.size(); i++) {
             E current = list.get(i);
             if (current instanceof Node) {
                 String oldPath = String.valueOf(i + 1);
                 String newPath = String.valueOf(i);
                 ((Node) current).removeParentPath(this, oldPath);
                 ((Node) current).addParentPath(this, newPath);
             }
         }
 
         updatePropertyList();
 
         fireEvent(new Event("", new ListChange(ListChangeType.REMOVE, this, element, index)));
 
         return element;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public boolean removeAll(Collection<?> c) {
         List<E> elements = new ArrayList<>();
         List<Integer> indices = new ArrayList<>();
 
         for (Object element : c) {
             int index = list.indexOf(element);
             while (index >= 0) {
                 elements.add((E) element);
                 indices.add(index);
 
                 int next = list.subList(index + 1, list.size()).indexOf(element);
                 if (next < 0) {
                     index = -1;
                 } else {
                     index = index + next + 1;
                 }
             }
         }
 
         if (!elements.isEmpty()) {
             for (int index = 0; index < list.size(); index++) {
                 E element = list.get(index);
                 if (element instanceof Node) {
                     ((Node) element).removeParentPath(this, String.valueOf(index));
                 }
             }
 
             list.removeAll(c);
 
             updatePropertyList();
             initialiseNode();
 
             fireEvent(new Event("", new ListChange(ListChangeType.REMOVE, this, elements, indices)));
 
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public boolean retainAll(Collection<?> c) {
         List<E> elements = new ArrayList<>(list);
         List<Integer> indices = new ArrayList<>(list.size());
         for (int i = 0; i < list.size(); i++) {
             indices.add(i);
         }
 
         for (Object element : c) {
             int index = list.indexOf(element);
             while (index >= 0) {
                 elements.remove(element);
                indices.remove(index);
 
                 int next = list.subList(index + 1, list.size()).indexOf(element);
                 if (next < 0) {
                     index = -1;
                 } else {
                     index = index + next + 1;
                 }
             }
         }
 
         if (!elements.isEmpty()) {
             for (int index = 0; index < list.size(); index++) {
                 E element = list.get(index);
                 if (element instanceof Node) {
                     ((Node) element).removeParentPath(this, String.valueOf(index));
                 }
             }
 
             list.retainAll(c);
 
             updatePropertyList();
             initialiseNode();
 
             fireEvent(new Event("", new ListChange(ListChangeType.REMOVE, this, elements, indices)));
 
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public E set(int index, E element) {
         E previous = get(index);
         set(String.valueOf(index), element);
         return previous;
     }
 
     @Override
     public int size() {
         return list.size();
     }
 
     @Override
     public List<E> subList(int fromIndex, int toIndex) {
         // TODO Make a sublist that emits ChangeEvents to the backing list
         return list.subList(fromIndex, toIndex);
     }
 
     @Override
     public Object[] toArray() {
         return list.toArray();
     }
 
     @Override
     public <T> T[] toArray(T[] a) {
         return list.toArray(a);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected void setLocal(String property, Object content) {
         list.set(Integer.parseInt(property), (E) content);
     }
 
     @Override
     protected <T> T getLocal(String property) {
         return (T) list.get(Integer.parseInt(property));
     }
 
     @Override
     public List<String> getProperties() {
         return Collections.unmodifiableList(properties);
     }
 
     @Override
     protected Class<?> getDeclaredPropertyType(String property) {
         return elementType;
     }
 
     // Used by ListNodeIterator
     protected void updatePropertyList() {
         properties.clear();
         for (int i = 0; i < list.size(); i++) {
             properties.add(String.valueOf(i));
         }
     }
 
     @Override
     public void set(String path, Object content) {
         if (path.startsWith("*.")) {
             String remainingPath = path.substring(2);
             for (E element : list) {
                 if (element != null) {
                     ((Node) element).set(remainingPath, content);
                 }
             }
         } else {
             super.set(path, content);
         }
     }
 
     @SuppressWarnings({"rawtypes", "unchecked"})
     @Override
     public <T> T get(String path) {
         if (path.startsWith("*")) {
             List ret = new ArrayList<>();
             if (path.equals("*")) {
                 ret.addAll(list);
             } else {
                 String remainingPath = path.substring(2);
                 for (E element : list) {
                     if (element != null) {
                         ret.add(((Node) element).get(remainingPath));
                     } else {
                         ret.add(null);
                     }
                 }
             }
             return (T) ret;
         } else {
             return super.get(path);
         }
     }
 
     @Override
     public String toString() {
         return list.toString();
     }
 
     /**
      *
      * @return
      */
     public Class<E> getElementType() {
         return elementType;
     }
 }
