 package plugins.adufour.ezplug;
 
 import icy.main.Icy;
 import icy.swimmingPool.SwimmingObject;
 import icy.swimmingPool.SwimmingPoolEvent;
 import icy.swimmingPool.SwimmingPoolListener;
 import icy.system.thread.ThreadUtil;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListDataListener;
 
 public class EzVarSwimmingObject<T> extends EzVar<SwimmingObject> implements SwimmingPoolListener
 {
 	private static final long		serialVersionUID	= 1L;
 	
 	private JComboBox				jComboBox;
 	
 	private JComboBoxListener		jComboBoxListener;
 	
 	private JComboBoxModel			jComboBoxModel;
 	
 	private JComboBoxRenderer		jComboBoxRenderer;
 	
 	final ArrayList<SwimmingObject>	validObjects		= new ArrayList<SwimmingObject>();
 	
 	public EzVarSwimmingObject(String varName)
 	{
 		super(varName);
 		
 		ThreadUtil.invoke(new Runnable()
 		{
 			public void run()
 			{
 				jComboBoxListener = new JComboBoxListener();
 				jComboBoxModel = new JComboBoxModel();
 				jComboBoxRenderer = new JComboBoxRenderer();
 				
 				jComboBox = new JComboBox(jComboBoxModel);
 				
 				for (SwimmingObject swObj : Icy.getMainInterface().getSwimmingPool().getObjects())
 					if (isValid(swObj))
 					{
 						validObjects.add(swObj);
 					}
 				
 				if (validObjects.size() > 0) jComboBox.setSelectedIndex(1);
 				
 				jComboBox.setRenderer(jComboBoxRenderer);
 				
 				jComboBox.addActionListener(jComboBoxListener);
 				
 				Icy.getMainInterface().getSwimmingPool().addListener(EzVarSwimmingObject.this);
 				
 				setComponent(jComboBox);
 			}
 			
 		}, !SwingUtilities.isEventDispatchThread());
 		
 	}
 	
 	/**
 	 * The combo box model is slightly different from that of SequenceSelector with the following
 	 * modifications: <br>
 	 * - the list of pointers to this variable is added to the combo box<br>
 	 * - no local reference to the currently selected sequence
 	 */
 	private final class JComboBoxModel implements ComboBoxModel
 	{
 		private Object	item;
 		
 		@Override
 		public Object getSelectedItem()
 		{
 			return item;
 		}
 		
 		@Override
 		public void setSelectedItem(Object anItem)
 		{
 			this.item = anItem;
 		}
 		
 		@Override
 		public void addListDataListener(ListDataListener l)
 		{
 		}
 		
 		@Override
 		public Object getElementAt(int index)
 		{
 			// first is the "no" selection
 			if (index == 0) return null;
 			
 			return validObjects.get(index - 1);
 		}
 		
 		@Override
 		public int getSize()
 		{
 			// slot 0 is dedicated to "no selection", everything else is shifted up by one increment
 			return 1 + validObjects.size();
 		}
 		
 		@Override
 		public void removeListDataListener(ListDataListener l)
 		{
 		}
 		
 	}
 	
 	private final class JComboBoxRenderer implements ListCellRenderer
 	{
 		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
 		{
 			if (value == null) return new JLabel("No selection");
 			
 			if (value instanceof SwimmingObject)
 			{
 				SwimmingObject swObj = (SwimmingObject) value;
 				String name = swObj.getName();
 				return new JLabel(name);
 			}
 			
 			return new JLabel("error"); // should never be displayed
 		}
 	}
 	
 	private final class JComboBoxListener implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			SwimmingObject newValue = (SwimmingObject) jComboBoxModel.getSelectedItem();
 			
 			if (newValue != null) jComboBox.setToolTipText(newValue.getName());
 			
 			fireVariableChanged(newValue);
 		}
 	}
 	
 	@Override
 	public SwimmingObject getValue()
 	{
 		return getValue(true);
 	}
 	
 	/**
 	 * Returns the currently selected sequence
 	 * 
 	 * @param throwExceptionIfNull
 	 *            throws a EzException if the currently selected sequence is null
 	 * @throws EzException
 	 *             if the currently selection is null and throwExceptionIfNull is true.
 	 */
 	public SwimmingObject getValue(boolean throwExceptionIfNull) throws EzException
 	{
 		if (jComboBoxModel.getSelectedItem() == null)
 		{
 			if (throwExceptionIfNull) throw new EzException("Variable \"" + name + "\": No selection", true);
 			return null;
 		}
 		
 		return (SwimmingObject) jComboBoxModel.getSelectedItem();
 	}
 	
 	@Override
 	public void setValue(SwimmingObject object)
 	{
 		jComboBoxModel.setSelectedItem(object);
 	}
 	
 	public boolean isValid(SwimmingObject swObj)
 	{
 		try
 		{
 			// if the value casts to T, the swimming object is valid
 			@SuppressWarnings({ "unchecked", "unused" })
 			T value = (T) swObj.getObject();
 			return true;
 		}
 		catch (ClassCastException ccE)
 		{
 			return false;
 		}
 	}
 	
 	@Override
 	public void swimmingPoolChangeEvent(SwimmingPoolEvent event)
 	{
 		SwimmingObject object = event.getResult();
 		
 		if (!isValid(object)) return;
 		
 		switch (event.getType())
 		{
 			case ELEMENT_ADDED:
 				validObjects.add(object);
 			break;
 			
 			case ELEMENT_REMOVED:
 				validObjects.remove(object);
 			break;
 		}
 		
		jComboBox.repaint();
		jComboBox.updateUI();
 	}
 	
 	// Dispose //
 	
 	@Override
 	public void dispose()
 	{
 		Icy.getMainInterface().getSwimmingPool().removeListener(this);
 		
 		jComboBox.removeActionListener(jComboBoxListener);
 		jComboBox.setRenderer(null);
 		jComboBoxListener = null;
 		jComboBoxRenderer = null;
 		
 		validObjects.clear();
 		
 		super.dispose();
 	}
 	
 }
