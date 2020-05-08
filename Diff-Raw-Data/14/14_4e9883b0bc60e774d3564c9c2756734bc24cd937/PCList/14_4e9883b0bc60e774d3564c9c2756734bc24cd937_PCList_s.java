 /*
  * Copyright (c) 2011 Dominik Dingel
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in the
  * Software without restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
  * Software, and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * The above copyright notice and this permission notice shall be included in all 
  * copies or substantial portions of the Software.
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
  * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION 
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package de.unisiegen.informatik.bs.alvis.primitive.datatypes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  * 
  * @author Dominik Dingel
  * 
  * @param <E>
  *            Type Parameter of the stored items
  */
 public class PCList<E extends PCObject> extends PCObject implements SortableCollection<E> {
 	public static final String TYPENAME = "List";
 	private List<E> items;
 
 	/**
 	 * 
 	 * @param items
 	 */
 	public void setItems(ArrayList<E> items) {
 		this.items = items;
 	}
 
 	public List<E> getItems() {
 		return this.items;
 	}
 
 	public PCList() {
 		items = new ArrayList<E>();
 	}
 
 	public PCList(List<E> list) {
 		items = list;
 	}
 
 	public E get(int index) {
 		return items.get(index);
 	}
 	
 	public E _get_(PCInteger index) {
 		return items.get(index.getLiteralValue());
 	}
 
 	public boolean isEmtpy() {
 		return items.isEmpty();
 	}
 	
 	public PCBoolean _isEmpty_() {
 		return new PCBoolean(this.isEmpty());
 	}
 
 	public int size() {
 		return items.size();
 	}
 
 	public PCInteger _size_() {
 		return new PCInteger(size());
 	}
 
 	@Override
 	public Iterator<E> iterator() {
 		PCListIterator<E> it = new PCListIterator<E>(this);
 		return it;
 	}
 
 	@Override
 	public void shuffle() {
 	}
 
 	@Override
 	public String toString() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean equals(PCObject toCheckAgainst) {
 		if (null == toCheckAgainst)
 			return false;
 		if (this == localNull
 				&& (toCheckAgainst == localNull || toCheckAgainst == PCObject.getNull()))
 			return true;
 		if (!(toCheckAgainst instanceof PCInteger))
 			return false;
 		@SuppressWarnings("rawtypes")
 		PCList other = (PCList) toCheckAgainst;
 		return items.equals(other.items);
 	}
 	@Override
 	public boolean add(E e) {
 		return items.add(e);
 	}
 	
 	public PCBoolean _add_(E e) {
 		return new PCBoolean(this.add(e));
 	}
 
 	@Override
 	public void add(int index, E element) {
 		items.add(index, element);
 	}
 
 	/**
 	 * addAll only works with PCList
 	 */
 	@Override
 	public boolean addAll(Collection<? extends E> c) {
 		boolean result = true;
 		for (E item : c) {
 			result = result && items.add(item);
 		}
 		return result;
 	}
 
 	
 	@Override
 	public boolean addAll(int index, Collection<? extends E> c) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void clear() {
 		items.clear();
 	}
 
 	public void _clear_() {
 		clear();
 	}
 
 	@Override
 	public boolean contains(Object o) {
 		return items.contains(o);
 	}
 	
 	public PCBoolean _contains_(PCObject o) {	
 		if (items.contains(o))
 			return new PCBoolean(true);
 		return new PCBoolean(false);
 	}
 
 	@Override
 	public boolean containsAll(Collection<?> c) {
 		return items.containsAll(c);
 	}
 
 	@Override
 	public int indexOf(Object o) {
 		return items.indexOf(o);
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return items.isEmpty();
 	}
 
 	@Override
 	public int lastIndexOf(Object o) {
 		return items.lastIndexOf(o);
 	}
 
 	@Override
 	public ListIterator<E> listIterator() {
 		return items.listIterator();
 	}
 
 	@Override
 	public ListIterator<E> listIterator(int index) {
 		return items.listIterator(index);
 	}
 
 	@Override
 	public boolean remove(Object o) {
 		return items.remove(o);
 	}
 	
 	public PCBoolean _remove_(PCObject o) {
 		if (items.remove(o))
 			return new PCBoolean(true);
 		return new PCBoolean(false);
 	}
 
 	@Override
 	public E remove(int index) {
 		return items.remove(index);
 	}
 
 	@Override
 	public boolean removeAll(Collection<?> c) {
 		return items.removeAll(c);
 	}
 
 	@Override
 	public boolean retainAll(Collection<?> c) {
 		return items.retainAll(c);
 	}
 
 	@Override
 	public E set(int index, E element) {
 		return items.set(index, element);
 	}
 
 	public void _set_(PCInteger index, E value) {
 		set(index.getLiteralValue(), value);
 	}
 
 	@Override
 	public List<E> subList(int fromIndex, int toIndex) {
 		return items.subList(fromIndex, toIndex);
 	}
 
 	@Override
 	public Object[] toArray() {
 		return items.toArray();
 	}
 
 	@Override
 	public <T> T[] toArray(T[] a) {
 		return items.toArray(a);
 	}
 
 	@Override
 	public void sortOn(@SuppressWarnings("rawtypes") SortableCollection toSortFrom) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void sort() {
		@SuppressWarnings("unchecked")
		E tmp[] = (E[]) items.toArray();
 		Arrays.sort(tmp);
 		if (tmp != null) {
 			items = new ArrayList<E>();
 			for (int i = 0; i < tmp.length; i++) {
				items.add(tmp[i]);
 			}
 		}

 	}
 	
 	@Override
 	public List<String> getMethods() {
 		List<String> result = new ArrayList<String>();
 		result.add("clear");
 		result.add("isEmpty");
 		result.add("get");
 		result.add("add");
 		result.add("remove");
 		result.add("contains");
 		result.add("size");
 		return result;
 	}
 	
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public SortableCollection storeInformation() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public HashMap<String, String> getHelp() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public PCObject set(String memberName, PCObject value) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
