 package fi.helsinki.cs.oato.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Iterator;
 
 import javax.swing.*;
 
 import fi.helsinki.cs.oato.model.*;
 import fi.helsinki.cs.oato.model.Event;
 
 import static fi.helsinki.cs.oato.Strings.*;
 
 
 /**
  * UI component for showing Events in a list. 
  **/
 public class EventList extends JScrollPane {
 
     public static final int HOVER_HGAP = 5;
     public static final int HOVER_VGAP = 0;
     public static final int ITEM_HEIGHT = 50;
     
 	/**
 	 * Serial version UID.
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * Actual container for the data.
 	 **/
	private JPanel content;
 	
 	private MainGUI parent;
 	
 	/**
 	 * Creates new empty list of events.
 	 **/
 	public EventList(MainGUI parent) {
 		this.parent = parent;
		this.content = new JPanel();
         this.content.setVisible(true);
		this.setViewportView( content );
		this.content.setLayout( new BoxLayout( this.content , BoxLayout.Y_AXIS ) );
 	}
 	
 	/**
 	 * Sets the preferred size of this UI component.
 	 * 
 	 * @param preferredSize Preferred size of UI component.
 	 **/
 	public void setPreferredSize(Dimension preferredSize) {
 		super.setPreferredSize( preferredSize );
 	}
 
 	/**
 	 * Adds new event to this UI view. 
 	 * 
 	 * Note, this does not promise, that the event would be put to correct position, it is just laid below all other events.
 	 * 
 	 * @param event The event to be added.
 	 **/
 	public void addEvent(Event event) {
 		// create a new panel for showing this item
 		JPanel item = new JPanel();
         FlowLayout layout = new FlowLayout(FlowLayout.CENTER, HOVER_HGAP, HOVER_VGAP);
         layout.setAlignOnBaseline(false);
 		item.setLayout( layout );
 		
 		// show actual event
 		JLabel text = new JLabel( event.toUIString() );
 		item.add( text );
 		
 		// button for editing this event
 		JButton edit = new JButton(localize("Edit"));
 		item.add( edit );
 		edit.setVisible(false);
 		
 		// button for deleting this event
 		JButton delete = new JButton(localize("Delete"));
 		// delete.setSize(100, 50);
 		delete.setVisible(false);
 		
 		ActionListener eventListener = new EventActionListener(event, edit, delete);
 		edit.addActionListener( eventListener );
 		delete.addActionListener( eventListener );
 		item.add( delete );
 
         item.setPreferredSize( new Dimension( (int) this.getPreferredSize().getWidth(), ITEM_HEIGHT ) );
 
 		// add mouse over listener for this item
 		// hide / display delete / edit when mouse over
 		EventDisplayListener listener = new EventDisplayListener(delete, edit, text, event);
 		item.addMouseListener( listener );
 		// XXX these needs to be added also to children events to make the experience corrext
 		delete.addMouseListener( listener );
 		edit.addMouseListener( listener );
 
         this.content.add( item );
         this.validate();
 	}
 
 	/**
 	 * Adds all the events in the iterator.
 	 * 
 	 * @param events list of events to be added.
 	 * */
 	public void addEvents(Iterator<Event> events) {
 		this.content.removeAll();
 		while( events.hasNext() ) {
 			this.addEvent( events.next() );
 		}
 		this.validate();
 	}
 	
 	/**
 	 * Handles the button pressing from "Edit"-button. 
 	 * 
 	 **/
 	private class EventActionListener implements ActionListener {
 
 		private Event event;
 		private JComponent edit;
 		private JComponent delete;
 		
 		public EventActionListener(Event event, JComponent edit, JComponent delete) {
 			this.event = event;
 			this.edit = edit;
 			this.delete = delete;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			if( e.getSource() == this.edit ) {
                 new EditEvent(EventList.this.parent, this.event);
 			}
 			if( e.getSource() == this.delete ) {
 				Schedule schedule = EventList.this.parent.getSchedule();
 				schedule.removeEvent(this.event);
 				EventList.this.parent.updateSchedule(schedule);
 			}
 		}
 		
 	}
 	
 	/**
 	 * Implements the hover-effect of "Delete" and "Edit"-buttons. 
 	 **/
 	private class EventDisplayListener implements MouseListener {
 
 		JButton delete;
 		JButton edit;
         JLabel text;
         Event event;
 		
 		public EventDisplayListener(JButton delete, JButton edit, JLabel text, Event event) {
 			this.delete = delete;
 			this.edit = edit;
             this.text = text;
             this.event = event;
 		}
 		
 		public void mouseClicked(MouseEvent e) {}
 
 		public void mouseEntered(MouseEvent e) {
 			this.delete.setVisible(true);
 			this.edit.setVisible(true);
             this.text.setText(event.toExtendedString());
 		}
 
 		public void mouseExited(MouseEvent e) {
 			// XXX don't act when inside the component
 			// XXX currently fixed with a hack
 			this.delete.setVisible(false);
 			this.edit.setVisible(false);
             this.text.setText(event.toUIString());
 		}
 
 		public void mousePressed(MouseEvent e) {
 		}
 
 		public void mouseReleased(MouseEvent e) {
 		}
 		
 	}
 	
 }
