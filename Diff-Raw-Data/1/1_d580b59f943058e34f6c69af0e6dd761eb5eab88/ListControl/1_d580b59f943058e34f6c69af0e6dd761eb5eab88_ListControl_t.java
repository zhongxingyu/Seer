 package org.whizu.jquery.mobile.list;
 
 import java.beans.PropertyChangeListener;
 
 import org.whizu.content.Content;
 import org.whizu.jquery.ClickListener;
 
 public interface ListControl<T> extends Iterable<T> {
 
 	public void addPropertyChangeListener(PropertyChangeListener listener);
 	
 	public Content build(T item);
 	
 	public T get(int index);
 
	
 	public void handleAddEvent();
 	
 	public void handleClickEvent(T element);
 	
 	public String id(T item);
 
 	public boolean isClickable();
 
 	public boolean isClickable(T element);
 	
 	public int size();
 
 	public ClickListener addEvent();
 }
