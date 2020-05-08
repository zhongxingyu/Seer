 package edu.cs319.client.customcomponents;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.AbstractListModel;
 
 public class JFlexibleListModel<E> extends AbstractListModel {
 
 	private List<E> contents;
 
 	public JFlexibleListModel() {
 		contents = new ArrayList<E>();
 	}
 
 	public boolean add(E e) {
 		if (contents.contains(e))
 			return false;
 		contents.add(e);
 		fireIntervalAdded(this, 0, getSize());
		System.out.println(contents);
 		return true;
 	}
 
 	public boolean remove(E obj) {
 		int idx = contents.indexOf(obj);
 		if (idx == -1)
 			return false;
 		;
 		contents.remove(idx);
 		fireIntervalRemoved(this, 0, getSize());
 		return true;
 	}
 
 	public int indexOf(Object o) {
 		return contents.indexOf(o);
 	}
 
 	public void clearList() {
 		contents.clear();
 		fireIntervalRemoved(this, 0, getSize());
 	}
 
 	public void addAll(Collection<E> newVals) {
 		contents.addAll(newVals);
 		fireIntervalAdded(this, 0, getSize());
 	}
 
 	@Override
 	public Object getElementAt(int arg0) {
 		if (contents.size() <= arg0)
 			return null;
 		return contents.get(arg0);
 	}
 
 	public boolean contains(E e) {
		System.out.println("Checking contains e");
 		return contents.contains(e);
 	}
 
 	@Override
 	public int getSize() {
 		return contents.size();
 	}
 
 	public boolean contains(String someID) {
		System.out.println("Checking contains String: someId");
 		return contents.contains(someID);
 	}
 
 	public void update() {
 		fireContentsChanged(this, 0, getSize());
 	}
 
 }
