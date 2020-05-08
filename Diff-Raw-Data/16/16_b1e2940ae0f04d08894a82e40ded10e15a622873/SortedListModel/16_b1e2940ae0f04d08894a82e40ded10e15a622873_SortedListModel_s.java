 package SortedListModel;
 import java.util.LinkedList;
 import javax.swing.*;
 import javax.swing.event.ListDataEvent;
 import java.util.Collections;
 
 public class SortedListModel extends AbstractListModel
 {
 	static final long serialVersionUID = 1;
     
 	private LinkedList<StringIRC> list;
     
     public SortedListModel()
     {       
         list = new LinkedList<StringIRC>();
     }
     
     public SortedListModel(LinkedList<String> data)
     {
         list = new LinkedList<StringIRC>();
         
         for (int i = 0; i < data.size(); ++i)
         {
         	list.add(new StringIRC(data.get(i)));
         }
     }
     
     public void update()
     {
     	fireContentsChanged(ListDataEvent.CONTENTS_CHANGED, 0, list.size() - 1);
     }
     
     public void add(Object o)
     {
     	if (o.toString().length() < 1)
     		return;
     	
     	list.add(new StringIRC(o.toString()));
     	
     	Collections.sort(list);
     	
     	fireContentsChanged(ListDataEvent.CONTENTS_CHANGED, 0, list.size() - 1);
     }
     
     public void remove(Object o)
     {
     	list.remove(o.toString());
     	
     	fireContentsChanged(ListDataEvent.CONTENTS_CHANGED, 0, list.size() - 1);
     }
     
     public boolean contains(Object o)
     {
     	for (int i = 0; i < list.size(); ++i)
     		if (list.get(i).equals(o)) return true;
     	
     	return false;
     }
     
     public Object getElementAt(int index)
     {
         if (list.size() <= index || index < 0)
         	throw new IndexOutOfBoundsException("Index of " + index + " does not exist.");
         else
         	return list.get(index);
     }
     
     public int getSize()
     {
         return list.size();
     }
 }
