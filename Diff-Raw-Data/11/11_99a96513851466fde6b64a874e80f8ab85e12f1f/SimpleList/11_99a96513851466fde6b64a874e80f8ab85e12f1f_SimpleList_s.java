package cs271.hw.liststackqueue.interfaces;
 
 public interface SimpleList<T> extends SimpleCollection {
 	public void add(T item);
 	public T get(int index);
 	public T remove(int index);
 }
