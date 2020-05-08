 package editor;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import editor.GenericSelector.CustomToString;
 import editor.GenericSelector.GenericListCallback;
 import editor.GenericSelector.SelectionChangeListener;
 
 
 public class ListGenericSelector<T> extends GenericSelector<T> {
 
 	/**
 	 * The interface for controlling the behavior of the list selector.
 	 */
 	public interface ListChangeListener<E> 
 	{
 		public void objectsRemoved(List<E> removed);
 		public void objectAdded(E added);
 		public void HighlightChange(List<E> highlighted);
 	}
 	
 	private JList list;
 	private ArrayList<T> selections;
 	private JButton remove;
 	
 	private List<ListChangeListener<T>> observers = new ArrayList<ListChangeListener<T>>();
 	
 	public ListGenericSelector(String label, GenericListCallback<T> callback)
 	{
 		this(label, callback, new CustomToString<T>() {
 			@Override
 			public String toString(T obj) {
 				return obj.toString();
 			}
 		});
 	}
 	
 	public ListGenericSelector(String label, GenericListCallback<T> callback, CustomToString<T> customToString)
 	{
 		super(label, callback, customToString);
 		removeAll();
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		
 		selections = new ArrayList<T>();
 		initTopPanel();
 		
 		list = new JList();
 		Dimension size = new Dimension(400, 75);
 		list.setPreferredSize(size);
 		list.setMinimumSize(size);
 		list.addListSelectionListener(new ListListener());
 		
 		JScrollPane scroll = new JScrollPane(list);
 		scroll.setPreferredSize(size);
 		scroll.setMaximumSize(size);
 		scroll.setMinimumSize(size);
 		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		
 		add(scroll);
 	}
 	
 	public void addListChangeListener(ListChangeListener<T> lcl)
 	{
 		observers.add(lcl);
 	}
 	
 	public void removeListChangeListener(ListChangeListener<T> lcl)
 	{
 		observers.remove(lcl);
 	}
 	
 	public List<T> getHighlightedObjects()
 	{
 		Object[] objs = list.getSelectedValues();
 		List<T> ret = new ArrayList<T>();
 		for(Object o : objs)
 			ret.add(((ListItemWrapper)o).wrapped);
 		return ret;
 	}
 	
 	public void setHighlightedObjects(List<T> objs){
 		//clear current selection
 		list.clearSelection();
 		
 		for(int i = 0; i < list.getModel().getSize(); i++)
 		{
 			for(T obj : objs)
 			{
 				if(obj == ((ListItemWrapper)list.getModel().getElementAt(i)).wrapped)
 					list.addSelectionInterval(i, i);
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<T> getSelectedObjects()
 	{
 		return (List<T>)selections.clone();
 	}
 	
 	public void setSelectedObjects(List<T> objs)
 	{
 		selections.clear();
 		List<ListItemWrapper> toDisplay = new ArrayList<ListItemWrapper>();
 		for (T obj : objs) 
 		{
 			selections.add(obj);
 			toDisplay.add(new ListItemWrapper(obj));
 		}
 		list.setListData(toDisplay.toArray());
 	}
 	
 	private void initTopPanel()
 	{
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 		
 		panel.add(label);
 		panel.add(Box.createHorizontalStrut(SPACING));
 		
 		button.setText("Add");
 		ActionListener b = button.getActionListeners()[0];
 		button.removeActionListener(b);
 		button.addActionListener(new AddButtonListener());
 		panel.add(button);
 		panel.add(Box.createHorizontalStrut(SPACING));
 		
 		remove = new JButton("Remove");
 		remove.addActionListener(new RemoveButtonListener());
 		panel.add(remove);
 		
 		add(panel);
 	}
 	
 	private List<T> removeSelected(List<T> fullList)
 	{
 		List<T> selected = getSelectedObjects();
 		List<T> ret = new ArrayList<T>();
 		for(T a : fullList)
 		{
 			boolean found = false;
 			for(T b : selected)
 				found |= (b == a);
 			if(!found)
 				ret.add(a);
 		}
 		return ret;
 	}
 	
 	private class RemoveButtonListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			List<T> removed = new ArrayList<T>();
 			List<T> saved = new ArrayList<T>();
 			
 			for(T obj : selections)
 			{
 				boolean found = false;
 				for(Object o : list.getSelectedValues())
 					found |= (obj == ((ListItemWrapper)o).wrapped);
 				if(found)
 					removed.add(obj);
 				else
 					saved.add(obj);
 			}
 			
 			setSelectedObjects(saved);
 			
 			for(ListChangeListener<T> lcl : observers)
 				lcl.objectsRemoved(removed);
 		}
 	}
 	
 	private class AddButtonListener implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			List<T> selectable = removeSelected(callback.getSelectionList());
 			if (!(selectable.size() == 0))
 			{
 				T o = showSelectionDialog(selectable);
 				List<T> selected = getSelectedObjects();
 				selected.add(o);
 				setSelectedObjects(selected);
 				for(ListChangeListener<T> lcl : observers)
 					lcl.objectAdded(o);
 			}
 		}
 	}
 	
 	private class ListListener implements ListSelectionListener {
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			List<T> selection = new ArrayList<T>();
 			for (Object o : list.getSelectedValues())
 			{
 				selection.add((T) o);
 			}
 			
 			List<T> highlighted = getHighlightedObjects();
 			for(ListChangeListener<T> lcl : observers)
 				lcl.HighlightChange(highlighted);
 			
 		}
 	}
 	
 	public static void main(String[] args)
 	{
 		JFrame frame = new JFrame();
 		ListGenericSelector<String> selector = new ListGenericSelector<String>("Strings", new GenericListCallback<String>() {
 			@Override
 			public List<String> getSelectionList() {
 				List<String> ret = new ArrayList<String>();
 				ret.add("fi");
 				ret.add("fo");
 				ret.add("fumb");
 				return ret;
 			}
 		}, new CustomToString<String>() {
 			@Override
 			public String toString(String obj) {
 				return obj.toUpperCase();
 			}
 		});
 		frame.setContentPane(selector);
 		selector.addListChangeListener(new ListChangeListener<String>(){
 
 			@Override
 			public void objectsRemoved(List<String> removed) {
 				System.out.println("removed:" + removed.toString());				
 			}
 
 			@Override
 			public void objectAdded(String added) {
 				System.out.println("added:" + added);				
 			}
 
 			@Override
 			public void HighlightChange(List<String> highlighted) {
 				System.out.println("highlighted:" + highlighted.toString());				
 			}
 			
 		});
 		
 		
 		frame.pack();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 	}
 
 }
