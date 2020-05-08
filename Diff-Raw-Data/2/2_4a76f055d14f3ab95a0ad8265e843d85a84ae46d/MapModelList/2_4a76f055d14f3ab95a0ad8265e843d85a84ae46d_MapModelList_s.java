 package org.toj.dnd.irctoolkit.map;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.toj.dnd.irctoolkit.token.Color;
 
 public class MapModelList implements Serializable, Iterable<MapModel> {
     private static final long serialVersionUID = 6853194581268144323L;
 
     private LinkedList<MapModel> list = new LinkedList<MapModel>();
 
     public void add(int arg0, MapModel arg1) {
         list.add(arg0, arg1);
     }
 
     public boolean add(MapModel arg0) {
         return list.add(arg0);
     }
 
     public boolean addAll(Collection<? extends MapModel> arg0) {
         return list.addAll(arg0);
     }
 
     public boolean addAll(int arg0, Collection<? extends MapModel> arg1) {
         return list.addAll(arg0, arg1);
     }
 
     public void addFirst(MapModel arg0) {
         list.addFirst(arg0);
     }
 
     public void addLast(MapModel arg0) {
         list.addLast(arg0);
     }
 
     public void clear() {
         list.clear();
     }
 
     public Object clone() {
         return list.clone();
     }
 
     public boolean contains(Object arg0) {
         return list.contains(arg0);
     }
 
     public boolean containsAll(Collection<?> arg0) {
         return list.containsAll(arg0);
     }
 
     public Iterator<MapModel> descendingIterator() {
         return list.descendingIterator();
     }
 
     public MapModel element() {
         return list.element();
     }
 
     public boolean equals(Object arg0) {
         return list.equals(arg0);
     }
 
     public MapModel get(int arg0) {
         return list.get(arg0);
     }
 
     public MapModel getFirst() {
         return list.getFirst();
     }
 
     public MapModel getLast() {
         return list.getLast();
     }
 
     public int hashCode() {
         return list.hashCode();
     }
 
     public int indexOf(Object arg0) {
         return list.indexOf(arg0);
     }
 
     public boolean isEmpty() {
         return list.isEmpty();
     }
 
     public Iterator<MapModel> iterator() {
         return list.iterator();
     }
 
     public int lastIndexOf(Object arg0) {
         return list.lastIndexOf(arg0);
     }
 
     public ListIterator<MapModel> listIterator() {
         return list.listIterator();
     }
 
     public ListIterator<MapModel> listIterator(int arg0) {
         return list.listIterator(arg0);
     }
 
     public boolean offer(MapModel arg0) {
         return list.offer(arg0);
     }
 
     public boolean offerFirst(MapModel arg0) {
         return list.offerFirst(arg0);
     }
 
     public boolean offerLast(MapModel arg0) {
         return list.offerLast(arg0);
     }
 
     public MapModel peek() {
         return list.peek();
     }
 
     public MapModel peekFirst() {
         return list.peekFirst();
     }
 
     public MapModel peekLast() {
         return list.peekLast();
     }
 
     public MapModel poll() {
         return list.poll();
     }
 
     public MapModel pollFirst() {
         return list.pollFirst();
     }
 
     public MapModel pollLast() {
         return list.pollLast();
     }
 
     public MapModel pop() {
         return list.pop();
     }
 
     public void push(MapModel arg0) {
         list.push(arg0);
     }
 
     public MapModel remove() {
         return list.remove();
     }
 
     public MapModel remove(int arg0) {
         return list.remove(arg0);
     }
 
     public boolean remove(Object arg0) {
         return list.remove(arg0);
     }
 
     public boolean removeAll(Collection<?> arg0) {
         return list.removeAll(arg0);
     }
 
     public MapModel removeFirst() {
         return list.removeFirst();
     }
 
     public boolean removeFirstOccurrence(Object arg0) {
         return list.removeFirstOccurrence(arg0);
     }
 
     public MapModel removeLast() {
         return list.removeLast();
     }
 
     public boolean removeLastOccurrence(Object arg0) {
         return list.removeLastOccurrence(arg0);
     }
 
     public boolean retainAll(Collection<?> arg0) {
         return list.retainAll(arg0);
     }
 
     public MapModel set(int arg0, MapModel arg1) {
         return list.set(arg0, arg1);
     }
 
     public int size() {
         return list.size();
     }
 
     public List<MapModel> subList(int arg0, int arg1) {
         return list.subList(arg0, arg1);
     }
 
     public Object[] toArray() {
         return list.toArray();
     }
 
     public <T> T[] toArray(T[] arg0) {
         return list.toArray(arg0);
     }
 
     public String toString() {
         return list.toString();
     }
 
     public MapModel createNewModel(String ch, String desc, Color foreground, Color background, int index) {
         MapModel model = new MapModel();
         model.setCh(ch);
         model.setDesc(desc);
         model.setForeground(foreground);
         model.setBackground(background);
         if (index == -1) {
             this.add(model);
         } else {
             this.add(index, model);
         }
         return model;
     }
 
     public MapModel findModelById(String ch) {
         for (MapModel model : this.list) {
             if (model.getId().equals(ch)) {
                 return model;
             }
         }
         return null;
     }
 
     public MapModel findModelByChOrDesc(String ch) {
         for (MapModel model : this.list) {
            if (model.getCh().equals(ch) || model.getDesc().equals(ch)) {
                 return model;
             }
         }
         return null;
     }
 
     public List<MapModel> asList() {
         return list;
     }
 }
