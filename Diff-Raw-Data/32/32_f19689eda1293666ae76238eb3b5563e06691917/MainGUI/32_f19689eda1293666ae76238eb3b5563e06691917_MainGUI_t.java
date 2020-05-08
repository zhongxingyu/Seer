 package fi.helsinki.cs.oato.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.EventListener;
 
 import javax.swing.*;
 
 import fi.helsinki.cs.oato.Event;
 import static fi.helsinki.cs.oato.Strings.*;
 
 /**
  * Create main UI for the application. 
  * 
  **/
 public class MainGUI extends JFrame {
 
     private static final long serialVersionUID = 1L;
 
     private EventList futureEvents = new EventList();
     private EventList allEvents = new EventList();
 
     /**
      * Create new GUI. Defaults to 500 x 500.
      * 
      **/
     public MainGUI(){
         this(500, 500);
     }
     
     /**
      * Creates new GUI. Size given.
      * 
     * Note: the UI must be set visible in the main-method!
     * 
      *  @param width width of the screen
      *  @param height height of the screen
      **/
     public MainGUI(int width, int height) {
         super("OATO");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.pack();
         this.setSize(width, height);
         createUI();
     }
 
     /**
      * Actually creates the UI 
      **/
     private void createUI() {
     	
     	// UI buttons
         JButton addEvent = new JButton(localize("Add event"));
         JButton saveData = new JButton(localize("Save"));
         JButton openData = new JButton(localize("Open"));
         
         addEvent.addActionListener( new ActionListener() {
             
             public void actionPerformed(ActionEvent e) {
                 // show add UI
                 new EditEvent();
             }
         } );
         
         saveData.addActionListener( new ActionListener() {
             
             public void actionPerformed(ActionEvent e) {
                 JFileChooser chooser = new JFileChooser();
                 chooser.showSaveDialog( MainGUI.this );
             }
         } );
         
         openData.addActionListener( new ActionListener() {
             
             public void actionPerformed(ActionEvent e) {
                 JFileChooser chooser = new JFileChooser();
                 chooser.showOpenDialog( MainGUI.this );
             }
         });
         
         // display buttons
         this.add( addEvent );
         this.add( saveData );
         this.add( openData );
         
         Dimension eventDataSize = new Dimension( this.getWidth() - 50 , 350 );
         futureEvents.setPreferredSize( eventDataSize );
         allEvents.setPreferredSize(eventDataSize);
         
         JTabbedPane eventsPane = new JTabbedPane();
         
         eventsPane.add(localize("Future events"), futureEvents );
         eventsPane.add(localize("All events"), allEvents );
         
         this.add( eventsPane );
         
         this.setLayout( new FlowLayout() );
         
     }
 
     // should take event
     public void addEvent(Event event) {
         futureEvents.addEvent(event);
     }
 
 }
