 package event_editor;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 
 import org.openstreetmap.josm.data.SelectionChangedListener;
 import org.openstreetmap.josm.data.osm.DataSet;
 import org.openstreetmap.josm.data.osm.Node;
 import org.openstreetmap.josm.data.osm.OsmPrimitive;
 import org.openstreetmap.josm.data.osm.Way;
 import org.openstreetmap.josm.gui.MapFrame;
 import org.openstreetmap.josm.gui.SideButton;
 import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
 import org.openstreetmap.josm.tools.Shortcut;
 
 @SuppressWarnings("serial")
 public class EventRelatedSelectionDialog extends ToggleDialog
 {
     private HashMap<String, Component> componentMap;
     private OsmPrimitive rootPrimitive;
     private EventPrimitive rootEventPrimitive;
     private EventEntity rootEventEntity;
     private boolean isEventSelected = false;
 
     protected JPanel panel;
 
     public boolean isEventSelected()
     {
 	return isEventSelected;
     }
 
     public void setEventSelected(boolean isEventSelected)
     {
 	this.isEventSelected = isEventSelected;
     }
 
     public EventRelatedSelectionDialog(final MapFrame mapFrame)
     {
 	super(tr("Select Related Items"), "commandstack", "", Shortcut.registerShortcut("subwindow:commandstack",
 	        tr("Toggle: {0}", tr("Command Stack")), KeyEvent.VK_O, Shortcut.ALT_CTRL_SHIFT), 200, true);
 
 	createGUI();
 	createComponentMap();
 
 	DataSet.addSelectionListener(getRootClickListener());
 
 	createLayout(panel, true, Arrays.asList(new SideButton[] { new SideButton(new SaveAction()),
 	        new SideButton(new CancelAction()) }));
     }
 
     private void createGUI()
     {
 	GridBagLayout layout = new GridBagLayout();
 	panel = new JPanel(layout);
 
 	GridBagConstraints c = new GridBagConstraints();
 	c.fill = GridBagConstraints.HORIZONTAL;
 	c.gridx = 0;
 	c.gridy = 0;
 	c.gridwidth = 1;
 	JLabel jlSelectEvent = new JLabel("Select an event");
 	panel.add(jlSelectEvent, c);
 
 	c = new GridBagConstraints();
 	c.fill = GridBagConstraints.HORIZONTAL;
 	c.gridx = 0;
 	c.gridy = 1;
 	c.weightx = 0.0;
 	c.gridwidth = 1;
 	ComboBoxModel<EventEntity> comboModel = new DefaultComboBoxModel<EventEntity>();
 	JComboBox jcSelectEvent = new JComboBox(comboModel);
 	jcSelectEvent.setName("jcSelectEvent");
 	jcSelectEvent.addActionListener(getJCSelectEventListener(jcSelectEvent));
 	panel.add(jcSelectEvent, c);
 
 	c = new GridBagConstraints();
 	c.fill = GridBagConstraints.HORIZONTAL;
 	c.gridx = 0;
 	c.gridy = 2;
 	c.weightx = 0.0;
 	c.gridwidth = 1;
 	JButton btnSelectRoot = new JButton("Select");
 	btnSelectRoot.setName("btnSelectRoot");
 	btnSelectRoot.setEnabled(false);
 	btnSelectRoot.addActionListener(getBTNSelectRootListener());
 	panel.add(btnSelectRoot, c);
 
 	c = new GridBagConstraints();
 	c.fill = GridBagConstraints.HORIZONTAL;
 	c.gridx = 0;
 	c.gridy = 3;
 	c.weightx = 0.0;
 	c.gridwidth = 1;
 	JLabel jlInstruction = new JLabel("Select an event");
 	jlInstruction.setName("jlInstruction");
 	panel.add(jlInstruction, c);
 
 	c = new GridBagConstraints();
 	c.fill = GridBagConstraints.HORIZONTAL;
 	c.gridx = 0;
 	c.gridy = 4;
 	c.weightx = 0.0;
 	c.gridwidth = 1;
 	DefaultListModel<String> listModel = new DefaultListModel<String>();
 	JList<String> jlRelatedPrimitives = new JList<String>(listModel);
 	jlRelatedPrimitives.setName("jlRelatedPrimitives");
 	panel.add(jlRelatedPrimitives, c);
     }
 
     public void updateGUI()
     {
 	JComboBox jcSelectEvent = (JComboBox) getComponentByName("jcSelectEvent");
 	DefaultComboBoxModel<EventEntity> comboModel = (DefaultComboBoxModel<EventEntity>) jcSelectEvent.getModel();
 
 	JList relatedPrimitives = (JList) getComponentByName("jlRelatedPrimitives");
 	DefaultListModel listModel = (DefaultListModel) (relatedPrimitives).getModel();
 
 	if (this.rootPrimitive != null)
 	{
 	    this.rootEventPrimitive = Utils.createEventPrimitive(rootPrimitive);
 	    if (!this.rootEventPrimitive.getEventMap().isEmpty())
 	    {
 		comboModel.removeAllElements();
 		for (EventEntity eventEntity : this.rootEventPrimitive.getEventMap().values())
 		{
 		    comboModel.addElement(eventEntity);
 		}
 
 		// If there is a selected EventEntity already, make sure it is
 		// of current selected node
 		if (this.rootEventEntity != null
 		        && this.rootEventPrimitive.getEventMap().containsValue(this.rootEventEntity))
 		{
 		    comboModel.setSelectedItem(this.rootEventEntity);
 		    // TODO this should fire up an event to load current
 		    // selected nodes in the listbox
 		}
 		else
 		{
 		    this.rootEventEntity = null;
 		}
 	    }
 	    else
 	    {
 		this.rootEventEntity = null;
 	    }
 	}
 	else
 	{
 	    this.rootEventPrimitive = null;
 	    this.rootEventEntity = null;
 	}
 
 	if (rootEventEntity == null)
 	{
 	    listModel.removeAllElements();
 	    jcSelectEvent.setEnabled(true);
 	}
 
 	if (rootEventPrimitive == null || rootEventPrimitive.getEventMap().isEmpty())
 	{
 	    comboModel.removeAllElements();
 	    jcSelectEvent.setEnabled(true);
 	}
     }
 
     private SelectionChangedListener getRootClickListener()
     {
 	return new SelectionChangedListener()
 	{
 	    @Override
 	    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection)
 	    {
 		// rootEventEntity is not available yet
 		if (!isEventSelected())
 		{
 		    if (newSelection.size() != 1)
 		    {
 			EventRelatedSelectionDialog.this.rootPrimitive = null;
 			updateGUI();
 		    }
 		    else
 		    {
 			OsmPrimitive sel = newSelection.iterator().next();
 			EventRelatedSelectionDialog.this.rootPrimitive = sel;
 			updateGUI();
 		    }
 		}
 		else
 		{
 		    if (newSelection.size() > 0)
 		    {
 			setSelectedAsRelated(newSelection);
 		    }
 		}
 	    }
 	};
     }
 
     /**
      * Listener for the ComboBox
      * 
      * @param jcSelectEvent
      * @return
      */
     private ActionListener getJCSelectEventListener(final JComboBox jcSelectEvent)
     {
 	return new ActionListener()
 	{
 	    @Override
 	    public void actionPerformed(ActionEvent arg0)
 	    {
 		JButton btnSelectRoot = (JButton) getComponentByName("btnSelectRoot");
 		btnSelectRoot.setEnabled(jcSelectEvent.getModel().getSize() > 0);
 	    }
 	};
     }
 
     /**
      * Listener for btnSelectRootListener
      * 
      * @return
      */
     private ActionListener getBTNSelectRootListener()
     {
 	return new ActionListener()
 	{
 	    @Override
 	    public void actionPerformed(ActionEvent arg0)
 	    {
 		// Inside the button that saves the event
 		JComboBox jcSelectEvent = (JComboBox) getComponentByName("jcSelectEvent");
 		EventRelatedSelectionDialog.this.rootEventEntity = (EventEntity) jcSelectEvent.getSelectedItem();
 		setEventSelected(true);
 
 		// Disable combobox to prevent changing of event
 		getComponentByName("jcSelectEvent").setEnabled(false);
 
 		JLabel jlInstruction = (JLabel) getComponentByName("jlInstruction");
 		jlInstruction.setText("Click on any node to mark as related");
 
 		// Load existing node information
 		String eventEntityRelatedItemsText = rootEventEntity.getRelatedItems();
 		if (eventEntityRelatedItemsText != null && !eventEntityRelatedItemsText.isEmpty())
 		{
 		    List<String> eventEntityRelatedItems = explodeRelatedNodes(eventEntityRelatedItemsText);
 		    JList relatedPrimitives = (JList) getComponentByName("jlRelatedPrimitives");
 		    DefaultListModel listModel = (DefaultListModel) (relatedPrimitives).getModel();
 		    listModel.removeAllElements();
 		    for (String eventEntityRelatedItem : eventEntityRelatedItems)
 		    {
 			listModel.addElement(eventEntityRelatedItem);
 		    }
 		}
 	    }
 	};
     }
 
     private class SaveAction extends AbstractAction
     {
 	public SaveAction()
 	{
 	    super();
 	    putValue(NAME, tr("Save"));
 	    putValue(SHORT_DESCRIPTION, tr("Save the selected items for the event"));
 	    // putValue(SMALL_ICON, ImageProvider.get("dialogs", "select"));
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0)
 	{
 	    // Save the entered nodes / ways to the EventEntity
 	    JList relatedPrimitives = (JList) getComponentByName("jlRelatedPrimitives");
 	    DefaultListModel listModel = (DefaultListModel) (relatedPrimitives).getModel();
 	    if (!listModel.isEmpty())
 	    {
 		String relatedPrimitivesTagValue = implodeRelatedNodes(listModel);
		// String tag = "event:" +
		// EventRelatedSelectionDialog.this.rootEventEntity.getEventId()
		// + ":related_items";
		EventRelatedSelectionDialog.this.rootEventEntity.setRelatedItems(relatedPrimitivesTagValue);
		Utils.saveEventPrimitive(rootEventPrimitive, rootPrimitive);
		// Utils.saveEventPrimitive(EventRelatedSelectionDialog.this.rootPrimitive,
		// tag, relatedPrimitivesTagValue);
 	    }
 
 	    getComponentByName("jcSelectEvent").setEnabled(true);
 
 	    JLabel jlInstruction = (JLabel) getComponentByName("jlInstruction");
 	    jlInstruction.setText("Select an Event");
 
 	    setEventSelected(false);
 	    rootEventEntity = null;
 
 	    updateGUI();
 	}
     }
 
     private class CancelAction extends AbstractAction
     {
 	public CancelAction()
 	{
 	    super();
 	    putValue(NAME, tr("Cancel"));
 	    putValue(SHORT_DESCRIPTION, tr("Discard all selected actions"));
 	    // putValue(SMALL_ICON, ImageProvider.get("dialogs", "select"));
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 	    setEventSelected(false);
 	    EventRelatedSelectionDialog.this.rootEventEntity = null;
 
 	    updateGUI();
 	}
     }
 
     private void setSelectedAsRelated(Collection<? extends OsmPrimitive> newSelection)
     {
 	if (isEventSelected())
 	{
 	    DefaultListModel<String> listModel = (DefaultListModel<String>) ((JList<String>) getComponentByName("jlRelatedPrimitives"))
 		    .getModel();
 	    for (OsmPrimitive sel : newSelection)
 	    {
 		String selLabel = null;
 		if (sel instanceof Node)
 		{
 		    selLabel = "Node:" + sel.getId();
 		}
 		else if (sel instanceof Way)
 		{
 		    selLabel = "Way:" + sel.getId();
 		}
 		if (selLabel != null && !listModel.contains(selLabel))
 		{
 		    listModel.addElement(selLabel);
 		}
 	    }
 	}
     }
 
     private String implodeRelatedNodes(DefaultListModel listModel)
     {
 	if (listModel.size() < 1)
 	{
 	    return "";
 	}
 	else
 	{
 	    StringBuilder sb = new StringBuilder();
 	    sb.append(listModel.get(0));
 
 	    for (int i = 1; i < listModel.size(); i++)
 	    {
 		sb.append(", ");
 		sb.append(listModel.get(i));
 	    }
 
 	    return sb.toString();
 	}
     }
 
     private List<String> explodeRelatedNodes(String str)
     {
 	return Arrays.asList(str.split(", "));
     }
 
     /**********************************
      * Helper functions
      **********************************/
     private void createComponentMap()
     {
 	componentMap = new HashMap<String, Component>();
 	Component[] components = panel.getComponents();
 	for (int i = 0; i < components.length; i++)
 	{
 	    componentMap.put(components[i].getName(), components[i]);
 	}
     }
 
     public Component getComponentByName(String name)
     {
 	if (componentMap.containsKey(name))
 	{
 	    return componentMap.get(name);
 	}
 	else
 	    return null;
     }
 }
