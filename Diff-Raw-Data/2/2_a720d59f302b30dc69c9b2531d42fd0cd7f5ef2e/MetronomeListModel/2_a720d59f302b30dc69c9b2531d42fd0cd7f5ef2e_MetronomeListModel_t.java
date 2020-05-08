 package jpod.gui.basic;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.NoSuchElementException;
 
 import javax.swing.AbstractListModel;
 import javax.swing.DefaultListModel;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 /**
  * This is model for JList of MetronomeWidget that manages MetronomeActions
  * @author Mateusz Szygenda
  *
  */
 public class MetronomeListModel extends DefaultListModel {
 	private static final long serialVersionUID = 236676997338542841L;
 	private int size = 0;
 	
 	/**
 	 * Adds metronome action at correct index(Depending on bar)
 	 * @param action - Action that should be inserted
 	 */
 	@Override
 	public void addElement(Object o)
 	{
 		if(o instanceof MetronomeAction)
 		{
 			int index = 0;
 			MetronomeAction tmp;
 			MetronomeAction action = (MetronomeAction)o;
 			Enumeration<Object> items = (Enumeration<Object>)elements();
 			for(index = 0; items.hasMoreElements(); index++)
 			{
 				tmp = (MetronomeAction) items.nextElement();
 				if(tmp.getBar() > action.getBar())
 				{
 					break;
 				}
 				else if(tmp.getBar() == action.getBar())
 				{
 					removeElementAt(index);
 					break;
 				}
 			}
 			insertElementAt(action,index);
 		}
 	}
 	
 	/**
 	 * Returns current elements count
 	 * @return Elements count
 	 */
 	public int elementsSize()
 	{
 		return size;
 	}
 	/**
	 * Inserts MetronomeAction at specified index
 	 * 
 	 * @param o - Must be MetronomeAction instance otherwise it wont be added to the list
 	 */
 	@Override
 	public void insertElementAt(Object o,int i)
 	{
 		if(o instanceof MetronomeAction)
 		{
 			super.add(i, o);
 			size++;
 		}
 	}
 }
