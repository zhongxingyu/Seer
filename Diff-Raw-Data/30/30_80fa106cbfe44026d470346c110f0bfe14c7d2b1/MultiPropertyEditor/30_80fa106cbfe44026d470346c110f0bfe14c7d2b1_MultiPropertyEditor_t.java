 package zz.utils.ui.propertyeditors;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JPanel;
 
import zz.utils.Utils;
 import zz.utils.properties.IRWProperty;
 import zz.utils.properties.PropertyUtils;
 import zz.utils.properties.SimpleRWProperty;
 import zz.utils.properties.PropertyUtils.Connector;
 import zz.utils.properties.PropertyUtils.SimpleValueConnector;
 import zz.utils.ui.GridStackLayout;
 import zz.utils.ui.propertyeditors.SimplePropertyEditor.EditorNotFoundException;
 
 /**
  * A property editor that is able to edit a set of properties of the same kind
  * @author gpothier
  *
  * @param <T>
  */
 public class MultiPropertyEditor<T> extends JPanel
 {
 	private Field itsField;
 	private List<IRWProperty<T>> itsProperties = new ArrayList<IRWProperty<T>>();
 	private IRWProperty<T> itsMasterProperty;
 	
 	private SimplePropertyEditor<T> itsMasterEditor;
 	private List<Connector<T>> itsConnectors = new ArrayList<Connector<T>>();
 	private boolean itsConnected = false;
 	
 	private IRWProperty<Boolean> pEnabled = new SimpleRWProperty<Boolean>()
 	{
 		@Override
 		protected void changed(Boolean aOldValue, Boolean aNewValue)
 		{
 			if (aNewValue)
 			{
 				if (itsMasterEditor != null) 
 				{
 					itsMasterEditor.setEnabled(true);
 					connect();
 				}
 			}
 			else
 			{
 				if (itsMasterEditor != null) 
 				{
 					itsMasterEditor.setEnabled(false);
 					disconnect();
 				}
 			}
 		}
 	};
 	
 	public MultiPropertyEditor(Field aField, List<IRWProperty<T>> aProperties)
 	{
 		this(getEditorClass(aField), aProperties);
 		itsField = aField;
 	}
 	
 	private static Class getEditorClass(Field aField)
 	{
 		PropertyEditor theAnnotation = aField.getAnnotation(PropertyEditor.class);
 		if (theAnnotation != null) return theAnnotation.value();
 		else return SimplePropertyEditor.getDefaultEditorClass(PropertyUtils.getValueClass(aField));
 	}
 	
 	
 	private MultiPropertyEditor(Class<? extends SimplePropertyEditor<T>> aEditorClass, List<IRWProperty<T>> aProperties)
 	{
 		setLayout(new GridStackLayout(1));
 		itsProperties = aProperties;
 
 		// if all properties have the same value, enable the master editor
 		T theValue = null;
		boolean theFirst = true; // Don't rely on theValue being null because null is a valid property value
 		boolean theSameValue = true;
 		for (IRWProperty<T> theProperty : aProperties)
 		{
			if (theFirst) 
			{
				theValue = theProperty.get();
				theFirst = false;
			}
			else if (! Utils.equalOrBothNull(theValue, theProperty.get()))
 			{
 				theSameValue = false;
 				break;
 			}
 		}
 		
 		pEnabled.set(theSameValue);
 
 		// add checkbox only if there is more than one property
 		if (itsProperties.size() > 1)
 		{
 			BooleanPropertyEditor.CheckBox enableEditor = new BooleanPropertyEditor.CheckBox(pEnabled);
 			add(enableEditor);
 			enableEditor.setOpaque(false);
 		}
 
 		// setup editor and connectors
 		if (itsProperties.size() > 0) 
 		{
 			Iterator<IRWProperty<T>> theIterator = aProperties.iterator();
 			
 			itsMasterProperty = theIterator.next();
 			itsMasterEditor = SimplePropertyEditor.createEditor(aEditorClass, itsMasterProperty);
 			add(itsMasterEditor);
 			itsMasterEditor.setOpaque(false);
 			
 			while(theIterator.hasNext())
 			{
 				IRWProperty<T> theProperty = theIterator.next();
 				itsConnectors.add(new SimpleValueConnector<T>(itsMasterProperty, theProperty, true, true));
 			}
 		}
 
 		itsMasterEditor.setEnabled(theSameValue);
 		if (theSameValue) connect();
 	}
 	
 	public Field getField()
 	{
 		return itsField;
 	}
 	
 	@Override
 	public void addNotify()
 	{
 		super.addNotify();
 		if (pEnabled.get()) connect();
 	}
 	
 	@Override
 	public void removeNotify()
 	{
 		super.removeNotify();
 		disconnect();
 	}
 	
 	private void connect()
 	{
 		if (itsConnected) return;
 		for (Connector<T> theConnector : itsConnectors) theConnector.connect();
 		itsConnected = true;
 	}
 
 	private void disconnect()
 	{
 		if (! itsConnected) return;
 		for (Connector<T> theConnector : itsConnectors) theConnector.disconnect();
 		itsConnected = false;
 	}
 	
 	
 	
 	/**
 	 * Creates a {@link MultiPropertyEditor} for each available property in the provided collection
 	 * of objects.
 	 */
 	public static List<MultiPropertyEditor> createEditors(Collection<?> aObjects)
 	{
 		List<MultiPropertyEditor> theResult = new ArrayList<MultiPropertyEditor>();
 		List<Field> theProperties = new ArrayList<Field>();
 		theProperties.addAll(PropertyUtils.getAvailableProperties(aObjects));
 		Collections.sort(theProperties, new FieldComparator());
 		
 		for (Field theField : theProperties)
 		{
 			try
 			{
 				theResult.add(new MultiPropertyEditor(theField, PropertyUtils.getProperties(theField, aObjects)));
 			}
 			catch (EditorNotFoundException e)
 			{
 				continue;
 			}
 		}
 		return theResult;
 	}
 	
 	private static class FieldComparator implements Comparator<Field>
 	{
 		public int compare(Field aF1, Field aF2)
 		{
 			PropertyOrder theOrder1 = aF1.getAnnotation(PropertyOrder.class);
 			PropertyOrder theOrder2 = aF2.getAnnotation(PropertyOrder.class);
 			
 			if (theOrder1 == null && theOrder2 == null)
 			{
 				String theName1 = aF1.getName();
 				String theName2 = aF2.getName();
 				
 				return theName1.compareTo(theName2);
 			}
 			else if (theOrder1 != null) return -1;
 			else if (theOrder2 != null) return 1;
 			else return theOrder1.value() - theOrder2.value();
 		}
 		
 	}
 }
