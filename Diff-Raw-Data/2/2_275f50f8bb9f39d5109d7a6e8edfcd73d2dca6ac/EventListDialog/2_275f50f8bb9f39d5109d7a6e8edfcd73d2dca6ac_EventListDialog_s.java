 package event_editor;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpringLayout;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.gui.ExtendedDialog;
 
 @SuppressWarnings("serial")
 public class EventListDialog extends ExtendedDialog
 {
     private HashMap<String, Component> componentMap;
     protected EventPrimitive eventPrimitive;
     protected List<EventListener> listeners = new ArrayList<EventListener>();
     private static final String[] buttonTexts = new String[] { tr("Edit"), tr("Delete"), tr("New"), tr("Save") };
     protected JPanel panel;
     private String currentAction = null;
     private final List<Integer> eventActualId = new ArrayList<Integer>();
 
     public EventListDialog(EventPrimitive eventPrimitive)
     {
 	super(Main.parent, "List of Events", buttonTexts);
 
 	this.eventPrimitive = eventPrimitive;
 
 	loadUI();
 	createComponentMap();
 
 	setContent(panel);
 	setupDialog();
 	// showDialog();
     }
 
     @Override
     protected void buttonAction(int buttonIndex, ActionEvent evt)
     {
 	if (buttonIndex == 0)
 	{
 	    // Edit Button
 	    JList<String> eventEntityList = (JList<String>) getComponentByName("entitylist");
 	    if (eventEntityList.getSelectedValuesList().isEmpty())
 	    {
 		return;
 	    }
 	    // Convert from natural 0,1,2 id to the original event id 0, 2, 5,
 	    // .. etc
 	    Integer selectedNumber = eventActualId.get(eventEntityList.getSelectedIndex());
 	    final EventEntity eventEntity = eventPrimitive.getEventMap().get(selectedNumber);
 	    EventTagDialog dlgEventTag = new EventTagDialog(eventEntity);
 	    dlgEventTag.addEventListener(new EventListener()
 	    {
 		@Override
 		public void notify(String eventType)
 		{
 		    if (eventType.equals("save"))
 		    {
 			// We consider the passed eventEntity has been
 			// updated
 			EventListDialog.this.currentAction = "save";
 		    }
 		    else if (eventType.equals("cancel"))
 		    {
 			EventListDialog.this.currentAction = "cancel";
 		    }
 		    else
 		    {
 			EventListDialog.this.currentAction = null;
 		    }
 		}
 	    });
 	    dlgEventTag.showDialog();
 	}
 	else if (buttonIndex == 1)
 	{
 	    // Delete
 	    deleteAction();
 	}
 	else if (buttonIndex == 2)
 	{
 	    // New
 	    createAction();
 	}
 	else if (buttonIndex == 3)
 	{
 	    // Save
 	    for (EventListener listener : this.listeners)
 	    {
 		listener.notify("save");
 	    }
 	    setVisible(false);
 	}
     }
 
     /**
      * Renders the main user interface
      */
     protected void loadUI()
     {
 	SpringLayout layout = new SpringLayout();
 	panel = new JPanel(layout);
 
 	final JCheckBox chkboxIsEvent = new JCheckBox("Mark as Event");
 	chkboxIsEvent.setName("isevent");
 	chkboxIsEvent.setSelected(eventPrimitive.isEvent());
 	chkboxIsEvent.addChangeListener(new ChangeListener()
 	{
 	    @Override
 	    public void stateChanged(ChangeEvent arg0)
 	    {
 		eventPrimitive.setIsEvent(chkboxIsEvent.isSelected());
 	    }
 	});
 
 	JLabel jlEventList = new JLabel();
 	jlEventList.setName("eventlistlabel");
 
 	DefaultListModel<String> listModel = new DefaultListModel<String>();
 	final JList<String> eventEntityList = new JList<String>(listModel);
 	loadEventEntityList(eventEntityList);
 	eventEntityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 	eventEntityList.setName("entitylist");
 	if (listModel.size() > 0)
 	{
 	    jlEventList.setText("Select an Event below");
 	}
 	else
 	{
 	    jlEventList.setText("Create a new Event");
 	}
 	JScrollPane scrollPane = new JScrollPane(eventEntityList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 	        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 
 	panel.add(chkboxIsEvent);
 	panel.add(jlEventList);
 	panel.add(scrollPane);
 
 	SpringUtilities.makeCompactGrid(panel, 3, 1, 6, 6, 6, 6);
     }
 
     protected void updateUI()
     {
 	JList<String> jlList = (JList<String>) getComponentByName("entitylist");
 	loadEventEntityList(jlList);
     }
 
     protected void loadEventEntityList(JList<String> list)
     {
 	DefaultListModel<String> listModel = (DefaultListModel) list.getModel();
 	listModel.clear();
 	eventActualId.clear();
 
 	final Map<Integer, EventEntity> eventMap = eventPrimitive.getEventMap();
 	if (!eventMap.isEmpty())
 	{
 	    for (Integer i : eventMap.keySet())
 	    {
 		// The ids we have for events could be random, like 0, 2, 5
 		// They are mapped to natural 0,1,2 using evenActualId list
 		eventActualId.add(i);
 		if (eventPrimitive.getEventMap().get(i).isToBeDeleted())
 		{
 		    listModel.addElement("Event to be deleted " + i);
 		}
 		else
 		{
 		    listModel.addElement("Event Number " + i);
 		}
 	    }
 	}
     }
 
     protected EventListDialog addEventListener(EventListener listener)
     {
 	this.listeners.add(listener);
 	return this;
     }
 
     protected void createAction()
     {
 	final EventEntity eventEntity = new EventEntity();
 	EventTagDialog dlgEventTag = new EventTagDialog(eventEntity);
 	dlgEventTag.addEventListener(new EventListener()
 	{
 	    @Override
 	    public void notify(String eventType)
 	    {
 		if (eventType.equals("save"))
 		{
 		    // We consider the passed eventEntity has been updated
 		    EventListDialog.this.currentAction = "save";
 		}
 		else if (eventType.equals("cancel"))
 		{
 		    EventListDialog.this.currentAction = "cancel";
 		}
 		else
 		{
 		    EventListDialog.this.currentAction = null;
 		}
 	    }
 	});
 	dlgEventTag.showDialog();
	if (currentAction.equals("save"))
 	{
 	    Integer nextEventNumber = eventPrimitive.getNextHighestEventNumber();
 	    eventPrimitive.getEventMap().put(nextEventNumber, eventEntity);
 	    eventPrimitive.setHighestEventNumber(nextEventNumber);
 	    currentAction = null;
 
 	    updateUI();
 	}
     }
 
     private void deleteAction()
     {
 	JList<String> eventEntityList = (JList<String>) getComponentByName("entitylist");
 	if (eventEntityList.getSelectedValuesList().isEmpty())
 	{
 	    return;
 	}
 	Integer selectedNumber = eventActualId.get(eventEntityList.getSelectedIndex());
 	System.out.println("Selected event id : " + selectedNumber);
 	EventEntity blankEventEntity = new EventEntity();
 	blankEventEntity.setToBeDeleted(true);
 	eventPrimitive.getEventMap().put(selectedNumber, blankEventEntity);
 
 	updateUI();
     }
 
     private void createComponentMap()
     {
 	componentMap = new HashMap<String, Component>();
 	Component[] components = panel.getComponents();
 	for (int i = 0; i < components.length; i++)
 	{
 	    if (components[i] instanceof JScrollPane)
 	    {
 		JScrollPane jsp = (JScrollPane) components[i];
 		componentMap.put(jsp.getViewport().getView().getName(), jsp.getViewport().getView());
 	    }
 	    else
 	    {
 		componentMap.put(components[i].getName(), components[i]);
 	    }
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
