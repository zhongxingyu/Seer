 /**
  * EditorWindow
  * <p>
  * The window for the Airport editor
  * 
  * @author Serjoscha Bassauer
  */
 package de.bwv_aachen.dijkstra.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.WindowConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.joda.time.Duration;
 
 import de.bwv_aachen.dijkstra.controller.Controller;
 import de.bwv_aachen.dijkstra.helpers.DateHelper;
 import de.bwv_aachen.dijkstra.model.Airport;
 import de.bwv_aachen.dijkstra.model.Connection;
 
 @SuppressWarnings("serial")
 public class EditorWindow extends View  implements ActionListener, ListSelectionListener {
 
     // Beans
     JPanel          connectionsContainer;
     JList<Airport>  locationJList;
     
     JButton lAdd;
     JButton lRem;
     JButton rAdd;
     
     // Helper Window(s)
     EditorWindow_AirportSelector airportSel;
     
     // Model(s)
     DefaultListModel<Airport> lm = new DefaultListModel<>();
 
     public EditorWindow(Controller c) {
         super(c);
         
         // generate Airport List Model
         for(Airport ca: controller.getModel().getAirportList().values()) { // assign every location to the jList Model
             this.lm.addElement(ca);
         }
     }
 
     public void draw() {
         super.getContentPane().removeAll(); // making this function being able to repaint the mainwindow
         super.setTitle("Bearbeiten");
         super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         super.setResizable(false);
 
         super.setLayout(new GridLayout(1, 2, 10, 0));
 
         ((JComponent) getContentPane()).setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.LIGHT_GRAY));
 
         // Build the UI Elems
         //locationJList = new JList<Airport>(locations); // this will create a jlist without an model -> completly unusable
         locationJList = new JList<Airport>(lm);
         //Only one airport can be selected
         locationJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         connectionsContainer = new JPanel();
         
         // Container for the left and the right side
         JPanel leftContainer = new JPanel();
         leftContainer.setLayout(new BorderLayout());
         JPanel rightContainer = new JPanel();
         rightContainer.setLayout(new BorderLayout());
         
         // Buttons
         this.lAdd = new JButton("+");
         this.lAdd.setActionCommand("lAdd");
         this.lRem = new JButton("-");
         this.lRem.setEnabled(false);
         this.lRem.setActionCommand("lRem");
         this.rAdd = new JButton("+");
         this.rAdd.setActionCommand("rAdd");
         this.rAdd.setEnabled(false);
         
         // Container for the buttons
         JPanel lButtons = new JPanel();
         lButtons.setLayout(new FlowLayout());
         JPanel rButtons = new JPanel();
         rButtons.setLayout(new FlowLayout());
         
         // Add buttons to container
         lButtons.add(lAdd);
         lButtons.add(lRem);
         
         rButtons.add(rAdd);
 
         // Add ActionListening
         //locationJList.addMouseListener(this);
         locationJList.addListSelectionListener(this);
         this.lAdd.addActionListener(this);
         this.rAdd.addActionListener(this);
         this.lRem.addActionListener(this);
         
         // Add lists and buttons to the correct jpanel
         leftContainer.add(locationJList, BorderLayout.CENTER);
         leftContainer.add(lButtons, BorderLayout.SOUTH);
         
         rightContainer.add(connectionsContainer, BorderLayout.CENTER);
         rightContainer.add(rButtons, BorderLayout.SOUTH);
         
         // Add elems (panels) to frame
         super.getContentPane().add(leftContainer);
         super.getContentPane().add(rightContainer);
 
         // Do the rest for displaying the window
         super.pack();
         super.setLocationRelativeTo(null); // center the frame
 
         // Show the window
         super.setVisible(true);
     }
 
     public void actionPerformed(ActionEvent e) {
         //JButton button = (JButton)e.getSource();
         switch(e.getActionCommand()){
             case "lAdd": // add FROM/source airport
                 String input = JOptionPane.showInputDialog("Name des Flughafens:");
                 if(input != null) { // prevents some nullpointer exceptions (which would not take any effect for the program, but disturbed me)
                     if(!input.equals("")) {
                         DefaultListModel<Airport> lm = (DefaultListModel<Airport>)this.locationJList.getModel();
                         
                         Long id = 0L;
                         try {
                           id = lm.lastElement().getId();
                         }
                         //Last element not found, so create a new airport with ID 1
                         catch (NoSuchElementException | NullPointerException ex) {
                            id = 1L;
                         }
                         
                         Airport nAp = new Airport(id, input); // create an temp airport that will later be assigned as connection  
                         
                         lm.addElement(nAp);      // add the String as given Airport to the JList Model
                         
                         //Put the new airport to the real data model
                         controller.getModel().getAirportList().put(id, nAp);
                         
                         //refresh the list
                         this.repaint();
                     }
                 }
             break;
             
             case "lRem":
                 lm.remove(this.locationJList.getSelectedIndex());
             break;
             
             case "rAdd":
                 // Show our self made selection box modal
                 this.airportSel = new EditorWindow_AirportSelector(controller, this);
                 this.airportSel.draw();
             break;
             
             case "approveAPselection":
                 int elem = this.lm.indexOf(locationJList.getSelectedValue());
                 Airport ap = this.lm.get(elem);
                 ap.getConnections().put(airportSel.getSelection(), new Connection(Duration.ZERO));
                 this.airportSel.dispose();
             break;
         }
         int selection = this.locationJList.getSelectedIndex(); // repainting makes the form lose its selection so lets manually save and restore them
         this.draw(); // repaint
         this.locationJList.setSelectedIndex(selection);
     }
 
     public void valueChanged(ListSelectionEvent e) {     
     /**
      * Triggered as soon as the list selection changes in any way
      */
         
         // first enable the action buttons
         this.lRem.setEnabled(true);
         this.rAdd.setEnabled(true);
 
         // Render Form
         connectionsContainer.removeAll();
         
         //Index points to a deleted Airport
         if (locationJList.getSelectedIndex() == -1) {
             return;
         }
         
         Airport ap = this.lm.elementAt(locationJList.getSelectedIndex());
         
         if (ap == null) {
             return;
         }
         
         connectionsContainer.setLayout(new GridLayout(ap.getConnections().size(), 4));
 
         for (Map.Entry<Airport, Connection> entry : ap.getConnections().entrySet()) {
             connectionsContainer.add(new JLabel(entry.getKey().toString()));
             JTextField textDuration = new JTextField();
             connectionsContainer.add(textDuration);
             connectionsContainer.add(new ConnectionChangeButton(entry.getValue(),textDuration));
             JButton deleteButton = new JButton("Löschen");
             
             deleteButton.addActionListener(new ActionListener() {
                 private Airport ap;
                 private Map.Entry<Airport, Connection> entry;
                 
                 @Override
                 public void actionPerformed(ActionEvent ev) {
                     ap.getConnections().remove(entry.getKey());
                     connectionsContainer.repaint();
                 }
                 
                 public ActionListener fakeConstructor(Airport ap, Map.Entry<Airport, Connection> entry) {
                     this.ap    = ap;
                     this.entry = entry;
                     return this;
                 }
             }.fakeConstructor(ap,entry));
             
             deleteButton.addActionListener(this);
             deleteButton.setActionCommand("removeConnection");
             connectionsContainer.add(deleteButton);
         }
 
         pack();
         connectionsContainer.repaint();
     }
 
 }
