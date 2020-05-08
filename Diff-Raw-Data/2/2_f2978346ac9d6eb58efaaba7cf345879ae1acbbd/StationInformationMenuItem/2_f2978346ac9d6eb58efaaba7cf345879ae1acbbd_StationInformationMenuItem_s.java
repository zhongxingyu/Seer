 package dk.frv.eavdam.menus;
 
 import dk.frv.eavdam.data.ActiveStation;
 import dk.frv.eavdam.data.Address;
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISFixedStationStatus;
 import dk.frv.eavdam.data.AISFixedStationType;
 import dk.frv.eavdam.data.Antenna;
 import dk.frv.eavdam.data.AntennaType;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.data.OtherUserStations;
 import dk.frv.eavdam.data.Person;
 import dk.frv.eavdam.data.Simulation;
 import dk.frv.eavdam.io.XMLExporter;
 import dk.frv.eavdam.io.XMLImporter;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 import dk.frv.eavdam.utils.DBHandler;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.ComboBoxEditor;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.xml.bind.JAXBException;
 
 /**
  * This class represents a menu item that opens a frame where the user can edit
  * station information.
  */
 public class StationInformationMenuItem extends JMenuItem {
 
     public final static String OWN_ACTIVE_STATIONS_LABEL = "Own active stations";
     public final static String SIMULATION_LABEL = "Simulation";
     public final static String STATIONS_OF_ORGANIZATION_LABEL = "Stations of organization";
     public final static String PROPOSAL_FROM_LABEL = "Proposal from";
     public final static String PROPOSAL_TO_LABEL = "My proposal";
     public final static String OPERATIVE_LABEL = "Operative";
     public final static String PLANNED_LABEL = "Planned";
     public final static String SIMULATED_LABEL = "Simulated";
 
     public static final long serialVersionUID = 3L;
 
     public StationInformationMenuItem(EavdamMenu eavdamMenu) {
         super("Edit Station Information");       
         addActionListener(new StationInformationMenuItemActionListener(eavdamMenu, null, null));
     }
 
     public StationInformationMenuItem(EavdamMenu eavdamMenu, String dataset, String stationName) {
         super("Edit Station Information");       
         addActionListener(new StationInformationMenuItemActionListener(eavdamMenu, dataset, stationName));
     }
 }
         
         
 class StationInformationMenuItemActionListener implements ActionListener, ChangeListener, DocumentListener, ItemListener {
 
     private EavdamMenu eavdamMenu;
 
     private JDialog dialog;
 
     private JPanel selectStationPanel;
     private JTabbedPane tabbedPane;
     
     private JComboBox selectDatasetComboBox;
     private JButton deleteSimulationButton;
     private JTextField newSimulationTextField;
     private JButton addNewSimulationButton;
     
     private JComboBox selectStationComboBox;
     private JButton addStationButton;
 
     private JDialog addStationDialog;
 
     private JTextField addStationNameTextField;
     private JComboBox addStationTypeComboBox;
     private JTextField addLatitudeTextField;
     private JTextField addLongitudeTextField;    
     private JTextField addMMSINumberTextField;
     private JTextField addTransmissionPowerTextField;
     private JComboBox addStationStatusComboBox;
 
     private JComboBox addAntennaTypeComboBox;
     private JTextField addAntennaHeightTextField;
     private JTextField addTerrainHeightTextField;    
     private JTextField addHeadingTextField;
     private JTextField addFieldOfViewAngleTextField;
     private JTextField addGainTextField;   
     
     private JTextArea addAdditionalInformationJTextArea;
     
     private JButton doAddStationButton;
     private JButton cancelAddStationButton;
     
     private JTextField stationNameTextField;
     private JComboBox stationTypeComboBox;
     private JTextField latitudeTextField;
     private JTextField longitudeTextField;    
     private JTextField mmsiNumberTextField;
     private JTextField transmissionPowerTextField;
     
     private JComboBox antennaTypeComboBox;
     private JTextField antennaHeightTextField;
     private JTextField terrainHeightTextField;    
     private JTextField headingTextField;
     private JTextField fieldOfViewAngleTextField;
     private JTextField gainTextField;   
     
     private JTextArea additionalInformationJTextArea;
   
     private JButton deleteButton;
     private JButton makeOperativeButton;
     private JButton saveButton;
     private JButton acceptProposalButton;
     private JButton proposeChangesButton;
 
     private JButton exitButton;
     
     private EAVDAMData data;  
     
     private String initiallySelectedDataset;
     private String initiallySelectedStationName;
     
     private boolean ignoreListeners = false;
     private int previouslySelectedStationIndex = -1;
 
     public StationInformationMenuItemActionListener(EavdamMenu eavdamMenu, String dataset, String stationName) {
         this.eavdamMenu = eavdamMenu;
         this.initiallySelectedDataset = dataset;
         this.initiallySelectedStationName = stationName;
     }
      
     public void actionPerformed(ActionEvent e) {
         
         if (ignoreListeners) {
             return;
         }
         
         if (e.getSource() instanceof StationInformationMenuItem) {
                       
             data = DBHandler.getData();    
                                     
             // FOR TESTING -->
             /*
             if (data.getOtherUsersStations() == null || data.getOtherUsersStations().isEmpty()) {
                 List<OtherUserStations> otherUsersStations = new ArrayList<OtherUserStations>();
                 OtherUserStations otherUserStations = new OtherUserStations();
                 EAVDAMUser testUser = new EAVDAMUser();
                 testUser.setOrganizationName("test");
                 otherUserStations.setUser(testUser);
                 ActiveStation testAS = new ActiveStation();
                 AISFixedStationData testData = new AISFixedStationData();
                 testData.setStationName("test");
                 testData.setLat(62.2);
                 testData.setLon(19.2);
                 testData.setStationType(AISFixedStationType.BASESTATION);
                 AISFixedStationStatus status = new AISFixedStationStatus();
                 status.setStatusID(DerbyDBInterface.STATUS_ACTIVE);
                 testData.setStatus(status);
                 AISFixedStationData testData2 = new AISFixedStationData();
                 testData2.setStationName("test");
                 testData2.setLat(62.2);
                 testData2.setLon(19.0);
                 AISFixedStationStatus status2 = new AISFixedStationStatus();
                 status2.setStatusID(DerbyDBInterface.STATUS_PLANNED);
                 testData2.setStatus(status2);
                 List<AISFixedStationData> testList = new ArrayList<AISFixedStationData>();
                 testList.add(testData);
                 testList.add(testData2);
                 testAS.setStations(testList);
                 List<ActiveStation> asTestList = new ArrayList<ActiveStation>();            
                 asTestList.add(testAS);
                 otherUserStations.setStations(asTestList);
                 otherUsersStations.add(otherUserStations);
                 data.setOtherUsersStations(otherUsersStations);
             }
             */
             // <-- FOR TESTING
                                                              
             selectDatasetComboBox = getComboBox(null);            
             selectDatasetComboBox.addItem(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL);
             if (data != null) {
                 if (data.getSimulatedStations() != null) {
                     for (Simulation s : data.getSimulatedStations()) {
                         selectDatasetComboBox.addItem(StationInformationMenuItem.SIMULATION_LABEL + ": " + s.getName());
                     }
                 }
                 if (data.getOtherUsersStations() != null) {
                     for (OtherUserStations ous : data.getOtherUsersStations()) {
                         if (ous.getUser() != null) {
                             selectDatasetComboBox.addItem(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL + " " + ous.getUser().getOrganizationName());
                         }
                     }
                 }
             }
             if (initiallySelectedDataset != null) {
                 if (initiallySelectedDataset.startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {
                     selectDatasetComboBox.setSelectedItem(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL);
                 } else if (initiallySelectedDataset.startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {                    
                     selectDatasetComboBox.setSelectedItem(initiallySelectedDataset);
                 } else if (initiallySelectedDataset.startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {
                     selectDatasetComboBox.setSelectedItem(initiallySelectedDataset);
                 }
             }
             selectDatasetComboBox.addItemListener(this);
     
             deleteSimulationButton = getButton("Delete selected simulation", 200);        
             deleteSimulationButton.setVisible(false);
             deleteSimulationButton.addActionListener(this);
             newSimulationTextField = getTextField(20);
             addNewSimulationButton = getButton("Add new simulation dataset", 200);
             addNewSimulationButton.addActionListener(this);
     
             selectStationComboBox = getComboBox(null);
             updateSelectStationComboBox(0);
             selectStationComboBox.addItemListener(this);
             addStationButton = getButton("Add new station", 140);
             addStationButton.addActionListener(this);
             
             stationNameTextField = getTextField(16);
             stationNameTextField.getDocument().addDocumentListener(this);
             stationTypeComboBox = getComboBox(new String[] {"AIS Base Station", "AIS Repeater", "AIS Receiver station", "AIS AtoN station"});
             stationTypeComboBox.addItemListener(this);
             latitudeTextField = getTextField(16);          
             latitudeTextField.getDocument().addDocumentListener(this);
             longitudeTextField = getTextField(16);
             longitudeTextField.getDocument().addDocumentListener(this);
             mmsiNumberTextField = getTextField(16);
             mmsiNumberTextField.getDocument().addDocumentListener(this);
             transmissionPowerTextField = getTextField(16);            
             transmissionPowerTextField.getDocument().addDocumentListener(this);
     
             antennaTypeComboBox = getComboBox(new String[] {"No antenna", "Omnidirectional", "Directional"});                     
             antennaTypeComboBox.addItemListener(this);
             antennaHeightTextField = getTextField(16);           
             antennaHeightTextField.getDocument().addDocumentListener(this);
             terrainHeightTextField = getTextField(16);
             terrainHeightTextField.getDocument().addDocumentListener(this);
             headingTextField = getTextField(16);
             headingTextField.getDocument().addDocumentListener(this);
             fieldOfViewAngleTextField = getTextField(16);           
             fieldOfViewAngleTextField.getDocument().addDocumentListener(this);
             gainTextField = getTextField(16);
             gainTextField.getDocument().addDocumentListener(this);
             
             additionalInformationJTextArea = getTextArea("");
             additionalInformationJTextArea.getDocument().addDocumentListener(this);    
             
             exitButton = getButton("Exit", 80);            
             exitButton.addActionListener(this);
             makeOperativeButton = getButton("Make operative", 140);    
             makeOperativeButton.addActionListener(this);
             saveButton = getButton("Save", 80);    
             saveButton.addActionListener(this);             
             deleteButton = getButton("Delete", 100);  
             deleteButton.addActionListener(this);
             acceptProposalButton = getButton("Accept proposal", 120);                                      
             acceptProposalButton.addActionListener(this);
             proposeChangesButton = getButton("Propose changes", 140);
             proposeChangesButton.addActionListener(this);
     
             tabbedPane = new JTabbedPane();
                
             if (data != null) {
                 if (initiallySelectedDataset == null) {
                     if (data.getActiveStations() != null && !data.getActiveStations().isEmpty()) {
                         ActiveStation as = data.getActiveStations().get(0);
                         initializeTabbedPane(as);
                     } else {
                         tabbedPane.removeAll();
                     }
                 } else if (initiallySelectedDataset.startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {
                     if (data.getActiveStations() != null && !data.getActiveStations().isEmpty()) {
                         if (initiallySelectedStationName != null) {
                             boolean initialized = false;
                             for (ActiveStation as : data.getActiveStations()) {
                                 List<AISFixedStationData> stations = as.getStations();
                                 if (stations != null) {
                                     for (AISFixedStationData stationData : stations) {
                                         if (stationData.getStationName().equals(initiallySelectedStationName)) {
                                             initializeTabbedPane(as);
                                             initialized = true;
                                             break;
                                         }
                                     }
                                 }
                             }
                             if (!initialized) {                               
                                 ActiveStation as = data.getActiveStations().get(0);
                                 initializeTabbedPane(as);
                             }
                         }
                     } else {
                         tabbedPane.removeAll();
                     }  
                     ignoreListeners = true;
                     int i = initiallySelectedDataset.lastIndexOf("/");
                     if (initiallySelectedDataset.substring(i+1).equals(StationInformationMenuItem.OPERATIVE_LABEL)) {
                         if (tabbedPane.getTabCount() >= 1) {
                             tabbedPane.setSelectedIndex(0);
                         }                        
                     } else if (initiallySelectedDataset.substring(i+1).equals(StationInformationMenuItem.PLANNED_LABEL)) {
                         if (tabbedPane.getTabCount() >= 2) {
                             tabbedPane.setSelectedIndex(1);
                         }                    
                     }
                     ignoreListeners = false;
                 } else if (initiallySelectedDataset.startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {
                     String temp = StationInformationMenuItem.SIMULATION_LABEL + ": ";
                     String simulationName = initiallySelectedDataset.substring(temp.length());                   
                     if (data.getSimulatedStations() != null && !data.getSimulatedStations().isEmpty()) {
                         for (Simulation s : data.getSimulatedStations()) {
                             if (s.getName().equals(simulationName)) {
                                 List<AISFixedStationData> stations = s.getStations();
                                 if (stations != null) {
                                     for (AISFixedStationData stationData : stations) {
                                         if (stationData.getStationName().equals(initiallySelectedStationName)) {                                    
                                             initializeTabbedPane(stationData);
                                             break;
                                         }
                                     }
                                 }
                             }
                         }
                     }  
                 } else if (initiallySelectedDataset.startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {
                     String temp = StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL + " ";                                        
                     String organizationName = initiallySelectedDataset.substring(temp.length());                    
                     if (data.getOtherUsersStations() != null && !data.getOtherUsersStations().isEmpty()) {
                         for (OtherUserStations ous : data.getOtherUsersStations()) {
                             if (ous.getUser().getOrganizationName().equals(organizationName)) {
                                 if (ous.getStations() != null && !ous.getStations().isEmpty()) {  
                                     if (initiallySelectedStationName != null) {
                                         boolean initialized = false;
                                         for (ActiveStation as : ous.getStations()) {
                                             List<AISFixedStationData> stations = as.getStations();
                                             if (stations != null) {
                                                 for (AISFixedStationData stationData : stations) {
                                                     if (stationData.getStationName().equals(initiallySelectedStationName)) {                                    
                                                         initializeTabbedPane(as);
                                                         initialized = true;
                                                         break;
                                                     }
                                                 }
                                             }
                                         }
                                         if (!initialized) {
                                             ActiveStation as = data.getActiveStations().get(0);
                                             initializeTabbedPane(as);
                                         }
                                     }
                                 }
                             }
                         }
                     } 
                 }                
             } else {
                 tabbedPane.removeAll();
             }
             
             tabbedPane.addChangeListener(this);
     
             if (tabbedPane.getTabCount() == 0) {
                 tabbedPane.setVisible(false);
             } else {                                
                 updateTabbedPane();
             }
             
             initiallySelectedDataset = null;            
             initiallySelectedStationName = null;
             
             updateDialog();                                        
         
         } else if (deleteSimulationButton != null && e.getSource() == deleteSimulationButton) {
             int response = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete the current simulation?", "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
                 ignoreListeners = true;                
                 String selectedItem = (String) selectDatasetComboBox.getSelectedItem();
                 String temp = StationInformationMenuItem.SIMULATION_LABEL + ": ";
                 String selectedSimulation = selectedItem.substring(temp.length());
                 deleteSimulation(selectedSimulation);
                 selectDatasetComboBox.removeItem(selectedItem);
                 selectDatasetComboBox.setSelectedItem(0);
                 updateSelectStationComboBox(0);
                 if (selectStationComboBox.getItemCount() <= 0) {
                     selectStationComboBox.setVisible(false);
                     tabbedPane.setVisible(false);                    
                 } else {
                     selectStationComboBox.setVisible(true);
                     tabbedPane.setVisible(true);
                     updateTabbedPane();
                 }
                 if (data == null || data.getSimulatedStations() == null || data.getSimulatedStations().isEmpty()) {
                     deleteSimulationButton.setVisible(false);
                 }
                 ignoreListeners = false;
                 eavdamMenu.rebuildShowOnMapMenu();
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing
             } 
 
         } else if (addNewSimulationButton != null && e.getSource() == addNewSimulationButton) {
             if (newSimulationTextField.getText().trim().isEmpty()) {
                 JOptionPane.showMessageDialog(dialog, "No name given for the simulation!");
             } else {
                 String simulationName = newSimulationTextField.getText().trim();                
                 boolean success = addSimulation(simulationName);
                 if (success) {
                     newSimulationTextField.setText("");
                     selectDatasetComboBox.addItem(StationInformationMenuItem.SIMULATION_LABEL + ": " + simulationName);
                     selectDatasetComboBox.setSelectedItem(StationInformationMenuItem.SIMULATION_LABEL + ": " + simulationName);
                     deleteSimulationButton.setEnabled(true);
                     updateSelectStationComboBox(0);
                     if (selectStationComboBox.getItemCount() <= 0) {
                         selectStationComboBox.setVisible(false);
                         tabbedPane.setVisible(false);                    
                     } else {
                         updateTabbedPane();
                     }
                     eavdamMenu.rebuildShowOnMapMenu();
                     addStationButton.setVisible(true);         
                 }
             }
 
         } else if (addStationButton != null && e.getSource() == addStationButton) {
 
             if (data == null || data.getUser() == null || data.getUser().getOrganizationName() == null || data.getUser().getOrganizationName().isEmpty()) {
                 JOptionPane.showMessageDialog(dialog, "Please, define an organization name before adding stations!");
                 return;
             }
 
             if (selectDatasetComboBox.getSelectedIndex() == 0 && tabbedPane != null && tabbedPane.isVisible() &&
                     tabbedPane.getSelectedIndex() >= 0 && tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL)) {
                 if (selectStationComboBox != null && isChanged(selectStationComboBox.getSelectedIndex())) {
                     int response = JOptionPane.showConfirmDialog(dialog,
                         "Do you want to save the changes made to the current planned station?",
                         "Confirm action", JOptionPane.YES_NO_CANCEL_OPTION);
                     if (response == JOptionPane.YES_OPTION) {
                         boolean success = saveStation(selectStationComboBox.getSelectedIndex());
                         if (success) {
                             saveButton.setEnabled(false);
                             updateTabbedPane();
                         }
                     } else if (response == JOptionPane.NO_OPTION) {                        
                         updateTabbedPane();
                     } else if (response == JOptionPane.CANCEL_OPTION) {
                         // do nothing
                     }                    
                 }
             }
             
             addStationDialog = new JDialog(eavdamMenu.getOpenMapFrame(), "Add Station", true);
 
             addStationNameTextField = getTextField(16);
             addStationTypeComboBox = getComboBox(new String[] {"AIS Base Station", "AIS Repeater", "AIS Receiver station", "AIS AtoN station"});
             addStationTypeComboBox.addItemListener(this);
 
             addLatitudeTextField = getTextField(16);
             addLongitudeTextField = getTextField(16);
             addMMSINumberTextField = getTextField(16);
             addTransmissionPowerTextField = getTextField(16);
             addStationStatusComboBox = getComboBox(new String[] {StationInformationMenuItem.OPERATIVE_LABEL, StationInformationMenuItem.PLANNED_LABEL});
             addStationStatusComboBox.addItemListener(this);
     
             addAntennaTypeComboBox = getComboBox(new String[] {"No antenna", "Omnidirectional", "Directional"});
             addAntennaTypeComboBox.addItemListener(this);
             addAntennaTypeComboBox.setSelectedIndex(0);      
             addAntennaHeightTextField = getTextField(16);            
             addTerrainHeightTextField = getTextField(16);
             addHeadingTextField = getTextField(16);
             addFieldOfViewAngleTextField = getTextField(16);           
             addGainTextField = getTextField(16);
             
             addAdditionalInformationJTextArea = getTextArea("");
             
             doAddStationButton = getButton("Add station", 140);  
             doAddStationButton.addActionListener(this);
             cancelAddStationButton = getButton("Cancel", 100);  
             cancelAddStationButton.addActionListener(this);          
             
             updateAntennaTypeComboBox(addAntennaTypeComboBox, addAntennaHeightTextField, addTerrainHeightTextField,
                 addHeadingTextField, addFieldOfViewAngleTextField, addGainTextField);
                 
             JPanel panel = new JPanel();
             panel.setLayout(new GridBagLayout());
                               
             JPanel p2 = new JPanel(new GridBagLayout());
             p2.setBorder(BorderFactory.createTitledBorder("General information"));
             GridBagConstraints c = new GridBagConstraints();
             c = updateGBC(c, 0, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));        
             p2.add(new JLabel("Station name:"), c);
             c = updateGBC(c, 1, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));               
             p2.add(addStationNameTextField, c);                    
             c = updateGBC(c, 0, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             p2.add(new JLabel("Station type:"), c);
             c = updateGBC(c, 1, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));    
             p2.add(addStationTypeComboBox, c);                                                                       
             c = updateGBC(c, 0, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                        
             p2.add(new JLabel("Latitude (WGS84):"), c);
             c = updateGBC(c, 1, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                   
             p2.add(addLatitudeTextField, c);                    
             c = updateGBC(c, 0, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                 
             p2.add(new JLabel("Longitude (WGS84):"), c);
             c = updateGBC(c, 1, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                   
             p2.add(addLongitudeTextField, c);        
             c = updateGBC(c, 0, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                
             p2.add(new JLabel("MMSI number:"), c);
             c = updateGBC(c, 1, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             p2.add(addMMSINumberTextField, c);
             c = updateGBC(c, 0, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                
             p2.add(new JLabel("Transmission power (Watt):"), c);
             c = updateGBC(c, 1, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                   
             p2.add(addTransmissionPowerTextField, c);
             c = updateGBC(c, 0, 6, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));              
             /*
             p2.add(new JLabel("Status of the fixed AIS station:"), c);
             c = updateGBC(c, 1, 6, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));               
             p2.add(addStationStatusComboBox, c);
             */
 
             c = updateGBC(c, 0, 0, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5)); 
             panel.add(p2, c);                    
               
             JPanel p3 = new JPanel(new GridBagLayout());
             p3.setBorder(BorderFactory.createTitledBorder("Antenna information"));
             c = updateGBC(c, 0, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5)); 
             p3.add(new JLabel("Antenna type:"), c);
             c = updateGBC(c, 1, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             p3.add(addAntennaTypeComboBox, c);
             c = updateGBC(c, 0, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                      
             p3.add(new JLabel("Antenna height above terrain (m):"), c);
             c = updateGBC(c, 1, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                
             p3.add(addAntennaHeightTextField, c);                    
             c = updateGBC(c, 0, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                  
             p3.add(new JLabel("Terrain height above sealevel (m):"), c);
             c = updateGBC(c, 1, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                  
             p3.add(addTerrainHeightTextField, c); 
             c = updateGBC(c, 0, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                     
             p3.add(new JLabel("Heading (degrees - integer):"), c);
             c = updateGBC(c, 1, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                
             p3.add(addHeadingTextField, c); 
             c = updateGBC(c, 0, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                        
             p3.add(new JLabel("Field of View angle (degrees - integer)"), c);
             c = updateGBC(c, 1, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                        
             p3.add(addFieldOfViewAngleTextField, c);                                         
             c = updateGBC(c, 0, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                   
             p3.add(new JLabel("Gain (dB)"), c);
             c = updateGBC(c, 1, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                      
             p3.add(addGainTextField, c);                       
 
             c = updateGBC(c, 0, 1, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5)); 
             panel.add(p3, c);                      
                                                      
             addAdditionalInformationJTextArea.setLineWrap(true);
             addAdditionalInformationJTextArea.setWrapStyleWord(true);                    
             JScrollPane p4 = new JScrollPane(addAdditionalInformationJTextArea);
             p4.setBorder(BorderFactory.createTitledBorder("Additional information"));
             p4.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
             p4.setPreferredSize(new Dimension(580, 90));
             p4.setMaximumSize(new Dimension(580, 90));
             
             c = updateGBC(c, 0, 2, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5)); 
             panel.add(p4, c);
                         
             JPanel buttonPanel = new JPanel();
             buttonPanel.add(doAddStationButton);          
             buttonPanel.add(cancelAddStationButton);
             c = updateGBC(c, 0, 3, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));             
             panel.add(buttonPanel, c);
 
             addStationDialog.getContentPane().add(panel);
 
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             addStationDialog.setBounds((int) screenSize.getWidth()/2 - 620/2,
                 (int) screenSize.getHeight()/2 - 770/2, 620, 770);
             addStationDialog.setVisible(true);            
 
         } else if (doAddStationButton != null && e.getSource() == doAddStationButton) {                     
             boolean success = addStation();
             if (success) {
                 ignoreListeners = true;
                 initiallySelectedStationName = addStationNameTextField.getText();
                 updateSelectStationComboBox(0);
                 if (selectStationComboBox.getItemCount() >= 0) {
                     selectStationComboBox.setVisible(true);
                 }
                 int selectedIndex = selectStationComboBox.getSelectedIndex();
                 if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {
                     if (data != null && data.getActiveStations() != null && selectedIndex < data.getActiveStations().size()) {
                         initializeTabbedPane(data.getActiveStations().get(selectedIndex));
                     }
                 } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {
                     if (data != null && data.getSimulatedStations() != null) {                
                         for (Simulation s : data.getSimulatedStations()) {
                             if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(s.getName())) {
                                 List<AISFixedStationData> stations = s.getStations();
                                 if (stations != null && selectedIndex < stations.size()) {
                                     initializeTabbedPane(stations.get(selectedIndex));
                                 }
                             }
                         }
                     }            
                 } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {    
                     if (data != null && data.getOtherUsersStations() != null) {                
                         for (OtherUserStations ous : data.getOtherUsersStations()) {
                             if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(ous.getUser().getOrganizationName())) {
                                 List<ActiveStation> stations = ous.getStations();
                                 if (stations != null && selectedIndex < stations.size()) {
                                     initializeTabbedPane(stations.get(selectedIndex));
                                 }
                             }
                         }
                     }
                 }              
                 if (tabbedPane.getTabCount() > 0) {
                     tabbedPane.setVisible(true);
                 }
                 updateTabbedPane();
                 ignoreListeners = false;
                 addStationDialog.dispose();  
                 eavdamMenu.getStationLayer().updateStations();
                 updateDialog();          
             }
             
         } else if (cancelAddStationButton != null && e.getSource() == cancelAddStationButton) {
             int response = JOptionPane.showConfirmDialog(addStationDialog,
                 "Are you sure you want to cancel?", "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
                 addStationDialog.dispose();
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing
             }
 
         } else if (makeOperativeButton != null && e.getSource() == makeOperativeButton) {
             if (saveButton.isEnabled()) {
                 JOptionPane.showMessageDialog(dialog, "The planned changes must be saved first!");
             } else {
                 int response = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to turn the planned station into the operative station?", "Confirm action", JOptionPane.YES_NO_OPTION);
                 if (response == JOptionPane.YES_OPTION) {
                     ignoreListeners = true;
                     int selectedStationIndex = selectStationComboBox.getSelectedIndex();                
                     turnPlannedIntoOperativeStation(selectedStationIndex);                
                     updateSelectStationComboBox(selectStationComboBox.getSelectedIndex());                                              
                     eavdamMenu.getStationLayer().updateStations();
                     ignoreListeners = false;                
                 } else if (response == JOptionPane.NO_OPTION) {                        
                     // do nothing
                 }
             }
             
         } else if (proposeChangesButton != null && e.getSource() == proposeChangesButton) {
             ignoreListeners = true;         
             int selectedIndex = selectStationComboBox.getSelectedIndex();  
             if (data != null && data.getOtherUsersStations() != null) {                
                 for (OtherUserStations ous : data.getOtherUsersStations()) {
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(ous.getUser().getOrganizationName())) {
                         List<ActiveStation> stations = ous.getStations();
                         if (stations != null && selectedIndex < stations.size()) {
                             ActiveStation as = stations.get(selectedIndex);     
                             addProposal(selectedIndex, ous.getUser().getOrganizationName());
                             initializeTabbedPane(as);
                             tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
                             updateTabbedPane();            
                         }
                     }
                 }
             }
                 
         } else if (saveButton != null && e.getSource() == saveButton) {            
             ignoreListeners = true;         
             if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_TO_LABEL)) {
                 int selectedIndex = selectStationComboBox.getSelectedIndex();         
                 boolean success = saveStation(selectedIndex);
                 if (success) {               
                     saveButton.setEnabled(false);
                 }                        
             } else {
                 int selectedIndex = selectStationComboBox.getSelectedIndex();         
                 boolean success = saveStation(selectedIndex);
                 if (success) {
                     updateSelectStationComboBox(selectedIndex);                                              
                     saveButton.setEnabled(false);
                     eavdamMenu.getStationLayer().updateStations();
                 }            
             }
             ignoreListeners = false;
             
         } else if ((deleteButton != null && e.getSource() == deleteButton)) {
             if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_FROM_LABEL)) {
                 int response = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete the proposal?", "Confirm action", JOptionPane.YES_NO_OPTION);
                 if (response == JOptionPane.YES_OPTION) {
                     ignoreListeners = true;
                     int selectedStationIndex = selectStationComboBox.getSelectedIndex();
                     String temp = StationInformationMenuItem.PROPOSAL_FROM_LABEL + " ";
                     String organizationName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).substring(temp.length());
                     deleteProposalFrom(selectedStationIndex, organizationName);                            
                     if (data.getActiveStations() != null && !data.getActiveStations().isEmpty()) {
                         ActiveStation as = data.getActiveStations().get(selectedStationIndex);                        
                         initializeTabbedPane(as);
                     }
                     updateTabbedPane();
                     ignoreListeners = false;
                 } else if (response == JOptionPane.NO_OPTION) {                        
                     // do nothing
                 } 
             } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_TO_LABEL)) {
                 int response = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete your proposal?", "Confirm action", JOptionPane.YES_NO_OPTION);
                 if (response == JOptionPane.YES_OPTION) {
                     ignoreListeners = true;
                     int selectedStationIndex = selectStationComboBox.getSelectedIndex(); 
                     if (data != null && data.getOtherUsersStations() != null) {                
                         for (OtherUserStations ous : data.getOtherUsersStations()) {
                             if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(ous.getUser().getOrganizationName())) {
                                 List<ActiveStation> stations = ous.getStations();
                                 if (stations != null && selectedStationIndex < stations.size()) {
                                     ActiveStation as = stations.get(selectedStationIndex);                                         
                                     deleteProposalTo(selectedStationIndex, ous.getUser().getOrganizationName());
                                     initializeTabbedPane(as);                            
                                     updateTabbedPane();            
                                 }
                             }
                         }
                     }
                     ignoreListeners = false;
                 } else if (response == JOptionPane.NO_OPTION) {                        
                     // do nothing
                 }                         
 
             } else {                        
                 int response = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete the current station?", "Confirm action", JOptionPane.YES_NO_OPTION);
                 if (response == JOptionPane.YES_OPTION) {
                     ignoreListeners = true;
                     int selectedStationIndex = selectStationComboBox.getSelectedIndex();
                     deleteStation(selectedStationIndex);
                     selectStationComboBox.removeItemAt(selectedStationIndex);
                     updateSelectStationComboBox(0);
                     if (selectStationComboBox.getItemCount() <= 0) {
                         selectStationComboBox.setVisible(false);
                         tabbedPane.setVisible(false);                    
                     } else {
                         updateTabbedPane();
                     }
                     eavdamMenu.getStationLayer().updateStations();
                     ignoreListeners = false;
                 } else if (response == JOptionPane.NO_OPTION) {                        
                     // do nothing
                 }    
             }
         } else if (exitButton != null && e.getSource() == exitButton) {
             int response = JOptionPane.showConfirmDialog(dialog,
                 "Are you sure you want to exit editing the stations?",
                 "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
                 dialog.dispose();
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing
             }     
         }
     }
     
     private void updateDialog() {
                         
         if (dialog != null) {
             dialog.dispose();
         }
 
         dialog = new JDialog(eavdamMenu.getOpenMapFrame(), "Edit Station Information", true);
 
         JPanel panel = new JPanel();
         panel.setLayout(new GridBagLayout());                  
 
         JPanel p1 = new JPanel(new GridBagLayout());
         p1.setBorder(BorderFactory.createTitledBorder("Select dataset"));
         
         GridBagConstraints c = new GridBagConstraints();
         c = updateGBC(c, 0, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
         p1.add(selectDatasetComboBox, c);
         c = updateGBC(c, 1, 0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE,new Insets(5,5,5,5));
         p1.add(deleteSimulationButton, c);
         c = updateGBC(c, 2, 0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE,new Insets(5,5,5,5));
         p1.add(newSimulationTextField, c);
         c = updateGBC(c, 3, 0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE,new Insets(5,5,5,5));
         p1.add(addNewSimulationButton, c);
         
         c = updateGBC(c, 0, 0, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5));
         panel.add(p1, c);                                                             
         
         selectStationPanel = new JPanel(new GridBagLayout());
         selectStationPanel.setBorder(BorderFactory.createTitledBorder("Select station"));                
         c = updateGBC(c, 0, 0, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));        
         selectStationPanel.add(selectStationComboBox, c);
         c = updateGBC(c, 1, 0, 0.5, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, new Insets(5,5,5,5));
         selectStationPanel.add(addStationButton, c);
         if (selectStationComboBox.getItemCount() <= 0) {
             selectStationComboBox.setVisible(false);
         }
 
         c = updateGBC(c, 0, 1, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5));
         panel.add(selectStationPanel, c);                                   
         
         c = updateGBC(c, 0, 2, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5));
         panel.add(tabbedPane, c);
 
         JPanel contentPanel = new JPanel();
         contentPanel.add(panel, BorderLayout.NORTH);
         dialog.getContentPane().add(contentPanel);      
         
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         dialog.setBounds((int) screenSize.getWidth()/2 - 920/2,
             (int) screenSize.getHeight()/2 - 660/2, 920, 660);
         dialog.setVisible(true);                                       
     }
     
     private void initializeTabbedPane(ActiveStation as) {
         ignoreListeners = true;
         tabbedPane.removeAll();
         if (as.getStations() != null && !as.getStations().isEmpty()) {
             List<AISFixedStationData> stations = as.getStations();
             boolean operativeFound = false;
             boolean plannedFound = false;
             for (AISFixedStationData station : stations) {
                 if (station.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
                     operativeFound = true;
                 } else if (station.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) {
                     plannedFound = true;
                 }
             }                        
             if (operativeFound) {
                 tabbedPane.addTab(StationInformationMenuItem.OPERATIVE_LABEL, null, new JPanel(), StationInformationMenuItem.OPERATIVE_LABEL);
             }
             if (plannedFound) {
                 tabbedPane.addTab(StationInformationMenuItem.PLANNED_LABEL, null, new JPanel(), StationInformationMenuItem.PLANNED_LABEL);
             }
         }
         if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {
             if (as.getProposals() != null && !as.getProposals().isEmpty()) {
                 Map<EAVDAMUser, AISFixedStationData> proposals = as.getProposals();
                 for (Object key : proposals.keySet()) {                        
                     String organizationName = ((EAVDAMUser) key).getOrganizationName();
                     tabbedPane.addTab(StationInformationMenuItem.PROPOSAL_FROM_LABEL + " " + organizationName, null, new JPanel(), StationInformationMenuItem.PROPOSAL_FROM_LABEL + " " + organizationName);
                 }
             }
         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {    
             if (as.getProposals() != null && !as.getProposals().isEmpty()) {
                 Map<EAVDAMUser, AISFixedStationData> proposals = as.getProposals();
                 if (proposals.size() == 1) {
                     tabbedPane.addTab(StationInformationMenuItem.PROPOSAL_TO_LABEL, null, new JPanel(), StationInformationMenuItem.PROPOSAL_TO_LABEL);
                 }
             }            
         }
         tabbedPane.addChangeListener(this);
         ignoreListeners = false;
     }
 
     private void initializeTabbedPane(AISFixedStationData stationData) {
         ignoreListeners = true;
         tabbedPane.removeAll();
         if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {
             if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_SIMULATED) {
                 tabbedPane.addTab(StationInformationMenuItem.SIMULATED_LABEL, null, new JPanel(), StationInformationMenuItem.SIMULATED_LABEL);
             }
         }
         tabbedPane.addChangeListener(this);
         ignoreListeners = false;
     }
     
     public void itemStateChanged(ItemEvent e) {
         
         if (ignoreListeners) {
             return;
         }
 
         if (selectDatasetComboBox != null && e.getItemSelectable() == selectDatasetComboBox && e.getStateChange() == ItemEvent.SELECTED) {
             if (selectStationPanel != null) {
                 selectStationPanel.setVisible(true);
                 updateSelectStationComboBox(0);
                 if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {
                     addStationButton.setVisible(true);
                     deleteSimulationButton.setVisible(false);                            
                     saveButton.setVisible(true);
                     deleteButton.setVisible(true);
                     makeOperativeButton.setVisible(true);    
                     proposeChangesButton.setVisible(false);                    
                 } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) { 
                     addStationButton.setVisible(true);
                     deleteSimulationButton.setVisible(true);  
                     saveButton.setVisible(true);
                     deleteButton.setVisible(true);
                     makeOperativeButton.setVisible(false);                        
                     proposeChangesButton.setVisible(false); 
                 } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {  
                     addStationButton.setVisible(false);
                     deleteSimulationButton.setVisible(false);                                                
                     saveButton.setVisible(false);
                     deleteButton.setVisible(false);
                     makeOperativeButton.setVisible(false);
                     proposeChangesButton.setVisible(true); 
                 }
                 if (selectStationComboBox.getItemCount() <= 0) {
                     selectStationComboBox.setVisible(false);
                     tabbedPane.setVisible(false);      
                 } else {                        
                     selectStationComboBox.setVisible(true);
                     if (data != null) {
                         if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {
                             if (data.getActiveStations() != null && !data.getActiveStations().isEmpty()) {
                                 ActiveStation as = data.getActiveStations().get(0);
                                 initializeTabbedPane(as);
                             } else {
                                 tabbedPane.removeAll();
                             }
                         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {               
                             if (data.getSimulatedStations() != null && !data.getSimulatedStations().isEmpty()) {
                                 Simulation s = data.getSimulatedStations().get(0);
                                 if (s.getStations() != null && !s.getStations().isEmpty()) {
                                     AISFixedStationData stationData = s.getStations().get(0);
                                     initializeTabbedPane(stationData);
                                 } else {
                                     tabbedPane.removeAll();                            
                                 }
                             }
                         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {              
                             if (data.getOtherUsersStations() != null && !data.getOtherUsersStations().isEmpty()) {
                                 OtherUserStations ous = data.getOtherUsersStations().get(0);
                                 if (ous.getStations() != null && !ous.getStations().isEmpty()) {
                                     ActiveStation as = ous.getStations().get(0);
                                     initializeTabbedPane(as);                                
                                 } else {
                                     tabbedPane.removeAll();
                                 }
                             }
                         }
                     } else {
                         tabbedPane.removeAll();
                     }
                     tabbedPane.addChangeListener(this);    
                     if (tabbedPane.getTabCount() == 0) {
                         tabbedPane.setVisible(false);
                     } else {            
                         tabbedPane.setVisible(true);              
                         updateTabbedPane();
                     }   
                 }
             }       
 
         } else if (antennaTypeComboBox != null && e.getItemSelectable() == antennaTypeComboBox && e.getStateChange() == ItemEvent.SELECTED) {             
             updateAntennaTypeComboBox(antennaTypeComboBox, antennaHeightTextField, terrainHeightTextField,
                 headingTextField, fieldOfViewAngleTextField, gainTextField);                                            
             if (isChanged(selectStationComboBox.getSelectedIndex())) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
 
         } else if (addAntennaTypeComboBox != null && e.getItemSelectable() == addAntennaTypeComboBox && e.getStateChange() == ItemEvent.SELECTED) {            
             updateAntennaTypeComboBox(addAntennaTypeComboBox, addAntennaHeightTextField, addTerrainHeightTextField,
                 addHeadingTextField, addFieldOfViewAngleTextField, addGainTextField);
                
         } else if (selectStationComboBox != null && e.getItemSelectable() == selectStationComboBox) {            
             if (e.getStateChange() == ItemEvent.DESELECTED) {
                 for (int i=0; i<selectStationComboBox.getItemCount(); i++) {
                     if (selectStationComboBox.getItemAt(i).equals(e.getItem())) {
                         previouslySelectedStationIndex = i;
                         break;
                     }
                 }
             } else if (e.getStateChange() == ItemEvent.SELECTED) {           
                 if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {                                        
                     ActiveStation as = data.getActiveStations().get(selectStationComboBox.getSelectedIndex());                   
                     if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL)) {
                         if (isChanged(previouslySelectedStationIndex)) {
                             int response = JOptionPane.showConfirmDialog(dialog,
                                 "Do you want to save the changes made to the current planned station?",
                                 "Confirm action", JOptionPane.YES_NO_CANCEL_OPTION);
                             if (response == JOptionPane.YES_OPTION) {
                                 boolean success = saveStation(previouslySelectedStationIndex);
                                 if (success) {
                                     saveButton.setEnabled(false);
                                     initializeTabbedPane(as);  
                                     updateTabbedPane();
                                 }
                             } else if (response == JOptionPane.NO_OPTION) {                      
                                 initializeTabbedPane(as);                              
                                 updateTabbedPane();
                             } else if (response == JOptionPane.CANCEL_OPTION) {
                                 // do nothing
                             }                    
                         } else {
                             initializeTabbedPane(as);  
                             updateTabbedPane();
                         }
                     } else {
                         initializeTabbedPane(as);                                                         
                         updateTabbedPane();
                     }
                 } else {                  
                     updateTabbedPane();
                 }
             }
         }
     }    
 
     public void changedUpdate(DocumentEvent e) {
         if (ignoreListeners) {
             return;
         }
         if (selectStationComboBox != null && saveButton != null) {
             if (isChanged(selectStationComboBox.getSelectedIndex())) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
         }
     }
 
     public void	insertUpdate(DocumentEvent e) {
         if (ignoreListeners) {
             return;
         }        
         if (selectStationComboBox != null && saveButton != null) {
             if (isChanged(selectStationComboBox.getSelectedIndex())) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
         }
     }
     
     public void removeUpdate(DocumentEvent e) {
         if (ignoreListeners) {
             return;
         }
         if (selectStationComboBox != null && saveButton != null) {
             if (isChanged(selectStationComboBox.getSelectedIndex())) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
         }
     }
         
     private JButton getButton(String title, int width) {
         JButton b = new JButton(title, null);        
         b.setVerticalTextPosition(AbstractButton.BOTTOM);
         b.setHorizontalTextPosition(AbstractButton.CENTER);
         b.setPreferredSize(new Dimension(width, 20));
         b.setMaximumSize(new Dimension(width, 20));    
         return b;
     }
     
     private JComboBox getComboBox(String[] components) {
         JComboBox cb = new JComboBox();
         if (components != null) {
             for (String c : components) {
                 cb.addItem(c);
             }
         }
         return cb;
     }
     
     private JTextField getTextField(int width) {
         JTextField tf = new JTextField(width);
         return tf;
     }
     
     private JTextArea getTextArea(String contents) {
         JTextArea ta = new JTextArea("");
         return ta;
     }
 
     private GridBagConstraints updateGBC(GridBagConstraints c, int gridx, int gridy, double weightx, int anchor, int fill, Insets insets) {
         c.gridx = gridx;
         c.gridy = gridy;
         c.weightx = weightx;
         c.anchor = anchor;
         c.fill = fill;
         if (insets != null) {
             c.insets = insets;
         }
         return c;
     }    
     
     private JComponent makeStationPanel() {
         
         ignoreListeners = true;
         
         JPanel panel = new JPanel(new GridBagLayout());                      
     
         // adds form fields
     
         JPanel p1 = new JPanel(new GridBagLayout());
         p1.setBorder(BorderFactory.createTitledBorder("General information"));
         GridBagConstraints c = new GridBagConstraints();
         c = updateGBC(c, 0, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5)); 
         p1.add(new JLabel("Station name:"), c);
         c = updateGBC(c, 1, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));               
         p1.add(stationNameTextField, c);                    
         c = updateGBC(c, 0, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                
         p1.add(new JLabel("Station type:"), c);
         c = updateGBC(c, 1, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5)); 
         p1.add(stationTypeComboBox, c);                                                                       
         c = updateGBC(c, 0, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                      
         p1.add(new JLabel("Latitude (WGS84):"), c);
         c = updateGBC(c, 1, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5)); 
         p1.add(latitudeTextField, c);                    
         c = updateGBC(c, 0, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));              
         p1.add(new JLabel("Longitude (WGS84):"), c);
         c = updateGBC(c, 1, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));      
         p1.add(longitudeTextField, c);        
         c = updateGBC(c, 0, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                  
         p1.add(new JLabel("MMSI number:"), c);
         c = updateGBC(c, 1, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));       
         p1.add(mmsiNumberTextField, c);
         c = updateGBC(c, 0, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));               
         p1.add(new JLabel("Transmission power (Watt):"), c);
         c = updateGBC(c, 1, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                       
         p1.add(transmissionPowerTextField, c);
 
         c = updateGBC(c, 0, 0, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5)); 
         panel.add(p1, c);                    
           
         JPanel p2 = new JPanel(new GridBagLayout());
         p2.setBorder(BorderFactory.createTitledBorder("Antenna information"));        
         c = updateGBC(c, 0, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5)); 
         p2.add(new JLabel("Antenna type:"), c);
         c = updateGBC(c, 1, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));              
         p2.add(antennaTypeComboBox, c);
         c = updateGBC(c, 0, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));            
         p2.add(new JLabel("Antenna height above terrain (m):"), c);
         c = updateGBC(c, 1, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));               
         p2.add(antennaHeightTextField, c);                    
         c = updateGBC(c, 0, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));          
         p2.add(new JLabel("Terrain height above sealevel (m):"), c);
         c = updateGBC(c, 1, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));            
         p2.add(terrainHeightTextField, c); 
         c = updateGBC(c, 0, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5)); 
         p2.add(new JLabel("Heading (degrees - integer):"), c);
         c = updateGBC(c, 1, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));            
         p2.add(headingTextField, c); 
         c = updateGBC(c, 0, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));          
         p2.add(new JLabel("Field of View angle (degrees - integer):"), c);
         c = updateGBC(c, 1, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));              
         p2.add(fieldOfViewAngleTextField, c);                                         
         c = updateGBC(c, 0, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));          
         p2.add(new JLabel("Gain (dB):"), c);
         c = updateGBC(c, 1, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));            
         p2.add(gainTextField, c);                       
 
         c = updateGBC(c, 1, 0, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5)); 
         panel.add(p2, c);                      
                                                  
         additionalInformationJTextArea.setLineWrap(true);
         additionalInformationJTextArea.setWrapStyleWord(true);
         JScrollPane p3 = new JScrollPane(additionalInformationJTextArea);
         p3.setBorder(BorderFactory.createTitledBorder("Additional information"));
         p3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         p3.setPreferredSize(new Dimension(580, 90));
         p3.setMaximumSize(new Dimension(580, 90));
          
         c = updateGBC(c, 0, 1, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5)); 
         c.gridwidth = 2;
         panel.add(p3, c);
 
         JPanel buttonPanel = new JPanel(new GridBagLayout());            
         c = updateGBC(c, 0, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5)); 
         c.gridwidth = 1;
         if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL)) {
             buttonPanel.add(deleteButton, c);
             c.gridx = 1;
             buttonPanel.add(proposeChangesButton, c);            
             c.gridx = 2;
             buttonPanel.add(exitButton, c);                    
         } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL)) {
             saveButton.setEnabled(false);
             buttonPanel.add(saveButton, c);
             c.gridx = 1;
             buttonPanel.add(makeOperativeButton, c);
             c.gridx = 2;
             buttonPanel.add(proposeChangesButton, c);
             c.gridx = 3;
             buttonPanel.add(exitButton, c);
         } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.SIMULATED_LABEL)) {
             buttonPanel.add(deleteButton, c);
             c.gridx = 1;
             saveButton.setEnabled(false);
             buttonPanel.add(saveButton, c);
             c.gridx = 2;
             buttonPanel.add(exitButton, c);
         } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_FROM_LABEL)) {
             buttonPanel.add(deleteButton, c);
             c.gridx = 1;
             buttonPanel.add(exitButton, c);
         } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_TO_LABEL)) {
             saveButton.setVisible(true);
             saveButton.setEnabled(false);
             buttonPanel.add(saveButton, c);
             c.gridx = 1;            
             deleteButton.setVisible(true);
             buttonPanel.add(deleteButton, c);
             c.gridx = 2;
             buttonPanel.add(exitButton, c);
         }
 
         c = updateGBC(c, 0, 2, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));
         c.gridwidth = 2;
         panel.add(buttonPanel, c);
          
         // updates form fields' statuses (enabled or disabled)
         
         if ((((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL) &&
                 (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) ||
                 tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL))) ||
                 tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) ||
                 tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_FROM_LABEL)) {
             stationNameTextField.setEnabled(false);
             configureDisabledTextField(stationNameTextField);
             stationTypeComboBox.setEnabled(false);                            
             configureDisabledComboBox(stationTypeComboBox);
             latitudeTextField.setEnabled(false);                            
             configureDisabledTextField(latitudeTextField);
             longitudeTextField.setEnabled(false);                             
             configureDisabledTextField(longitudeTextField);            
             mmsiNumberTextField.setEnabled(false);
             configureDisabledTextField(mmsiNumberTextField);            
             transmissionPowerTextField.setEnabled(false);  
             configureDisabledTextField(transmissionPowerTextField);
             antennaTypeComboBox.setEnabled(false);   
             configureDisabledComboBox(antennaTypeComboBox);
             antennaHeightTextField.setEnabled(false);   
             configureDisabledTextField(antennaHeightTextField); 
             terrainHeightTextField.setEnabled(false);   
             configureDisabledTextField(terrainHeightTextField); 
             headingTextField.setEnabled(false);   
             configureDisabledTextField(headingTextField); 
             fieldOfViewAngleTextField.setEnabled(false);                                                   
             configureDisabledTextField(fieldOfViewAngleTextField); 
             gainTextField.setEnabled(false); 
             configureDisabledTextField(gainTextField); 
             additionalInformationJTextArea.setEnabled(false);               
             configureDisabledTextArea(additionalInformationJTextArea);
         } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL) ||
                 tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.SIMULATED_LABEL) ||
                 tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_TO_LABEL)) {                
             stationNameTextField.setEnabled(true);
             stationTypeComboBox.setEnabled(true);   
             stationTypeComboBox.setEditable(false);                                     
             latitudeTextField.setEnabled(true);                            
             longitudeTextField.setEnabled(true);                                         
             mmsiNumberTextField.setEnabled(true);           
             transmissionPowerTextField.setEnabled(true);         
             antennaTypeComboBox.setEnabled(true);   
             antennaTypeComboBox.setEditable(false); 
             antennaHeightTextField.setEnabled(true);   
             terrainHeightTextField.setEnabled(true);   
             headingTextField.setEnabled(true);   
             fieldOfViewAngleTextField.setEnabled(true);                                                   
             gainTextField.setEnabled(true); 
             additionalInformationJTextArea.setEnabled(true);                          
         }
 
         if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {
             if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) ||
                     tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL)) {
                 addStationButton.setVisible(false);
                 deleteSimulationButton.setVisible(false);                                                
                 saveButton.setVisible(false);
                 deleteButton.setVisible(false);
                 makeOperativeButton.setVisible(false);
                 proposeChangesButton.setVisible(true);
             } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_TO_LABEL)) {                      
                 addStationButton.setVisible(false);
                 deleteSimulationButton.setVisible(false);                                                
                 saveButton.setVisible(true);
                 deleteButton.setVisible(true);   
                 makeOperativeButton.setVisible(false);
                 proposeChangesButton.setVisible(true);                         
             }
         } else {
             proposeChangesButton.setVisible(false);                   
         }
 
         // updates form fields contents
 
         int selectedIndex = selectStationComboBox.getSelectedIndex();
 
         if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {
             if (data != null && data.getOtherUsersStations() != null) {                
                 for (OtherUserStations ous : data.getOtherUsersStations()) {
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(ous.getUser().getOrganizationName())) {
                         List<ActiveStation> stations = ous.getStations();
                         if (stations != null && selectedIndex < stations.size()) {
                             ActiveStation as = stations.get(selectedIndex);
                             if (as.getProposals() == null || as.getProposals().isEmpty()) {
                                 proposeChangesButton.setVisible(true);
                             } else {
                                 proposeChangesButton.setVisible(false);
                             }
                         }
                     }
                 }
             }               
         }
     
         AISFixedStationData selectedStationData = getSelectedStationData(selectedIndex);
         
         if (selectedStationData != null) {
 
             if (selectedStationData.getStationName() != null) {
                 stationNameTextField.setText(selectedStationData.getStationName());
             } else {
                 stationNameTextField.setText("");                
             }
             if (selectedStationData.getStationType() != null) {
                 if (selectedStationData.getStationType() == AISFixedStationType.BASESTATION) {
                     stationTypeComboBox.setSelectedIndex(0);
                 } else if (selectedStationData.getStationType() == AISFixedStationType.REPEATER) {
                     stationTypeComboBox.setSelectedIndex(1);
                 } else if (selectedStationData.getStationType() == AISFixedStationType.RECEIVER) {
                     stationTypeComboBox.setSelectedIndex(2);
                 } else if (selectedStationData.getStationType() == AISFixedStationType.ATON) {
                     stationTypeComboBox.setSelectedIndex(3);
                 }
             } else {
                 stationTypeComboBox.setSelectedIndex(0);
             }
             if (!Double.isNaN(selectedStationData.getLat())) {                
                 latitudeTextField.setText(String.valueOf(selectedStationData.getLat()));                         
             } else {
                 latitudeTextField.setText("");
             }
             if (!Double.isNaN(selectedStationData.getLon())) {  
                 longitudeTextField.setText(String.valueOf(selectedStationData.getLon()));
             } else {
                 longitudeTextField.setText("");
             }
             if (selectedStationData.getMmsi() != null) {
                 mmsiNumberTextField.setText(selectedStationData.getMmsi());
             } else {
                 mmsiNumberTextField.setText("");
             }
             if (selectedStationData.getTransmissionPower() != null) {
                 transmissionPowerTextField.setText(selectedStationData.getTransmissionPower().toString());
             } else {
                 transmissionPowerTextField.setText("");
             }
             if (selectedStationData.getAntenna() != null) {
                 Antenna antenna = selectedStationData.getAntenna();
                 if (antenna.getAntennaType() != null) {
                     if (antenna.getAntennaType() == AntennaType.OMNIDIRECTIONAL) {
                         antennaTypeComboBox.setSelectedIndex(1);
                     } else if (antenna.getAntennaType() == AntennaType.DIRECTIONAL) {
                         antennaTypeComboBox.setSelectedIndex(2);
                     }
                 } else {
                     antennaTypeComboBox.setSelectedIndex(0);
                 }
                 if (!Double.isNaN(antenna.getAntennaHeight())) {
                     antennaHeightTextField.setText(String.valueOf(antenna.getAntennaHeight()));
                 } else {
                     antennaHeightTextField.setText("");
                 }
                 if (!Double.isNaN(antenna.getTerrainHeight())) {
                     terrainHeightTextField.setText(String.valueOf(antenna.getTerrainHeight()));
                 } else {
                     terrainHeightTextField.setText("");
                 }                
                 if (antenna.getHeading() != null) {
                     headingTextField.setText(antenna.getHeading().toString());
                 } else {
                     headingTextField.setText("");
                 }
                 if (antenna.getFieldOfViewAngle() != null) {
                     fieldOfViewAngleTextField.setText(antenna.getFieldOfViewAngle().toString());
                 } else {
                     fieldOfViewAngleTextField.setText("");
                 }
                 if (antenna.getGain() != null) {
                     gainTextField.setText(antenna.getGain().toString());
                 } else {
                     gainTextField.setText("");
                 }                    
             } else {           
                 antennaTypeComboBox.setSelectedIndex(0);
             }
             if (selectedStationData.getDescription() != null) {
                 additionalInformationJTextArea.setText(selectedStationData.getDescription());
             } else {
                 additionalInformationJTextArea.setText("");
             }
             updateAntennaTypeComboBox(antennaTypeComboBox, antennaHeightTextField, terrainHeightTextField,
                 headingTextField, fieldOfViewAngleTextField, gainTextField);
         }
         
         ignoreListeners = false;
         
         return panel;    
     }
 
     private AISFixedStationData getSelectedStationData(int selectedIndex) {
     
     	if(selectedIndex < 0) return null;
     	
         AISFixedStationData selectedStationData = null;
     
         if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {
             if (data != null && data.getActiveStations() != null && selectedIndex < data.getActiveStations().size()) {
                 ActiveStation as = data.getActiveStations().get(selectedIndex);
                 if (as.getStations() != null) {
                     if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) ||
                             tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL)) {
                         for (AISFixedStationData stationData : as.getStations()) {
                             if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) && stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
                                 selectedStationData = stationData;
                                 break;
                             } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL) && stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) {
                                 selectedStationData = stationData;
                                 break;
                             }
                         }
                     } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_FROM_LABEL)) {                        
                         Map<EAVDAMUser, AISFixedStationData> proposals = as.getProposals();                        
                         String temp = StationInformationMenuItem.PROPOSAL_FROM_LABEL + " ";
                         String organizationName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).substring(temp.length());
                         for (Object key : proposals.keySet()) { 
                             EAVDAMUser user = (EAVDAMUser) key;
                             if (user.getOrganizationName().equals(organizationName)) {
                                 selectedStationData = proposals.get(user);
                             }
                         }
                     }
                 }
             }       
         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {
             if (data != null && data.getSimulatedStations() != null) {                
                 for (Simulation s : data.getSimulatedStations()) {
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(s.getName())) {
                         List<AISFixedStationData> stations = s.getStations();
                         if (stations != null && selectedIndex < stations.size()) {
                             selectedStationData = stations.get(selectedIndex);
                         }
                     }
                 }
             }
             
         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {    
             if (data != null && data.getOtherUsersStations() != null) {                
                 for (OtherUserStations ous : data.getOtherUsersStations()) {
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(ous.getUser().getOrganizationName())) {
                         List<ActiveStation> stations = ous.getStations();
                         if (stations != null && selectedIndex < stations.size()) {
                             ActiveStation as = stations.get(selectedIndex);
                             if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) ||
                                     tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL)) {
                                 if (as.getStations() != null) {
                                     for (AISFixedStationData stationData : as.getStations()) {
                                         if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) &&
                                                 stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
                                            selectedStationData = stationData;
                                             break;
                                         } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL) &&
                                                 stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) {
                                             selectedStationData = stationData;
                                             break;
                                         }
                                     }
                                 }
                             } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PROPOSAL_TO_LABEL)) {
                                 if (as.getProposals() != null && as.getProposals().size() ==1) {
                                     for (Object key : as.getProposals().keySet()) { 
                                         EAVDAMUser temp = (EAVDAMUser) key;
                                         selectedStationData = as.getProposals().get(temp);
                                     }
                                 }                      
                             }
                         }
                     }
                 }
             }
         }
         
         return selectedStationData;    
     }
 
     private void configureDisabledTextField(JTextField textField) {
         Color bgColor = UIManager.getColor("TextField.background");  
         textField.setBackground(bgColor);  
         Color fgColor = UIManager.getColor("TextField.foreground");  
         textField.setDisabledTextColor(fgColor);
     }
     
     private void configureDisabledComboBox(JComboBox comboBox) {
         comboBox.setEditable(true);
         ComboBoxEditor editor = comboBox.getEditor();
         JTextField temp = (JTextField) editor.getEditorComponent();
         temp.setDisabledTextColor(UIManager.getColor("ComboBox.foreground"));
         temp.setBackground(UIManager.getColor("ComboBox.background"));
         comboBox.setEnabled(false);
     }    
     
     private void configureDisabledTextArea(JTextArea textArea) {
         Color bgColor = UIManager.getColor("TextArea.background");  
         textArea.setBackground(bgColor);  
         Color fgColor = UIManager.getColor("TextArea.foreground");  
         textArea.setDisabledTextColor(fgColor);
     }
         
     public void stateChanged(ChangeEvent evt) {
         if (ignoreListeners) {
             return;
         }
         updateTabbedPane();
     }
     
     private void updateTabbedPane() {
         tabbedPane.setComponentAt(tabbedPane.getSelectedIndex(), makeStationPanel());
     }    
 
     private void updateAntennaTypeComboBox(JComboBox antennaTypeComboBox_, JTextField antennaHeightTextField_,
             JTextField terrainHeightTextField_, JTextField headingTextField_, JTextField fieldOfViewAngleTextField_,
             JTextField gainTextField_) {
         if (antennaTypeComboBox_.getSelectedIndex() == 0) {  // no antenna         
             antennaHeightTextField_.setText("");
             antennaHeightTextField_.setEnabled(false);
             terrainHeightTextField_.setText("");
             terrainHeightTextField_.setEnabled(false);
             headingTextField_.setText("");
             headingTextField_.setEnabled(false);
             fieldOfViewAngleTextField_.setText("");
             fieldOfViewAngleTextField_.setEnabled(false);
             gainTextField_.setText("");
             gainTextField_.setEnabled(false); 
         } else if (antennaTypeComboBox_.getSelectedIndex() == 1) {  // omnidirectional
 			if (antennaHeightTextField_ == addAntennaHeightTextField ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL) ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.SIMULATED_LABEL) ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_TO_LABEL)) {  
 				antennaHeightTextField_.setEnabled(true);
 				terrainHeightTextField_.setEnabled(true);
 			} else if ((((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL) &&
 					(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL))) ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_FROM_LABEL)) {
 				antennaHeightTextField_.setEnabled(false);
 				terrainHeightTextField_.setEnabled(false);
 			}
 			headingTextField_.setText("");
             headingTextField_.setEnabled(false);
             fieldOfViewAngleTextField_.setText("");
             fieldOfViewAngleTextField_.setEnabled(false);
             gainTextField_.setText("");
             gainTextField_.setEnabled(false); 
         } else if (antennaTypeComboBox_.getSelectedIndex() == 2) {  // directional
 			if (antennaHeightTextField_ == addAntennaHeightTextField ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL) ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.SIMULATED_LABEL) ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_TO_LABEL)) {  
 				antennaHeightTextField_.setEnabled(true);
 				terrainHeightTextField_.setEnabled(true);
 			} else if ((((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL) &&
 					(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL))) ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) ||
 					tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_FROM_LABEL)) {
 				antennaHeightTextField_.setEnabled(false);
 				terrainHeightTextField_.setEnabled(false);
 			}
             headingTextField_.setEnabled(true);
             fieldOfViewAngleTextField_.setEnabled(true);
             gainTextField_.setEnabled(true); 
         } 
     }
 
     private boolean addSimulation(String simulationName) {
         
         if (data == null) {
             data = new EAVDAMData();        
         }
         
         List<Simulation> simulatedStations = data.getSimulatedStations();
         if (simulatedStations != null) {
             for (Simulation s : simulatedStations) {
                 if (s.getName().equals(simulationName)) {
                     JOptionPane.showMessageDialog(dialog, "A simulation with the given name already exists. " +
                         "Please, select another name for the simulation.");
                     return false;
                 }
             }
         }
         
         Simulation simulation = new Simulation();
         simulation.setName(simulationName);
         simulatedStations.add(simulation);
         data.setSimulatedStations(simulatedStations);
         DBHandler.saveData(data);
         
         return true;
     }
     
     private void deleteSimulation(String simulationName) {
         
         if (data == null || data.getSimulatedStations() == null) {
             return;
         }
         
         List<Simulation> simulatedStations = data.getSimulatedStations();
         for (int i=0; i<simulatedStations.size(); i++) {
             Simulation s = simulatedStations.get(i);
             if (s.getName().equals(simulationName)) {
                 simulatedStations.remove(i);
                 break;
             }            
         }
         
         data.setSimulatedStations(simulatedStations);
         DBHandler.deleteSimulation(simulationName);
     }
 
     private boolean addStation() {
         
         if (data == null) {
             data = new EAVDAMData();        
         }
         
         if (alreadyExists(-1, addStationNameTextField.getText())) {
             JOptionPane.showMessageDialog(dialog, "A station with the given name already exists. " +
                 "Please, select another name for the station.");                 
             return false;
         }
 
         if (addLatitudeTextField.getText().trim().isEmpty()) {
             JOptionPane.showMessageDialog(addStationDialog, "Latitude is mandatory.");
             return false;
         } else {
             if (!addLatitudeTextField.getText().trim().isEmpty()) {
                 try {
                     
                     Double.parseDouble(addLatitudeTextField.getText().replace(",", ".").trim());                    
                 } catch (NumberFormatException ex) {                
                     JOptionPane.showMessageDialog(addStationDialog, "Latitude is not a valid number.");
                     return false;
                 }
             }
         }            
         if (addLongitudeTextField.getText().trim().isEmpty()) {            
             JOptionPane.showMessageDialog(addStationDialog, "Longitude is mandatory.");                    
             return false;
         } else {                                                        
             if (!addLongitudeTextField.getText().trim().isEmpty()) {
                 try {
                     Double.parseDouble(addLongitudeTextField.getText().replace(",", ".").trim());
                 } catch (NumberFormatException ex) {                
                     JOptionPane.showMessageDialog(addStationDialog, "Longitude is not a valid number.");
                     return false;
                 }
             }
         }                    
         try {
             if (!addTransmissionPowerTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(addTransmissionPowerTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(addStationDialog, "Transmission power is not a valid number.");
             return false;
         }   
         if (addAntennaTypeComboBox.getSelectedIndex() == 1 &&
                 (addAntennaHeightTextField.getText().trim().isEmpty() ||
                 addTerrainHeightTextField.getText().trim().isEmpty())) {
             JOptionPane.showMessageDialog(addStationDialog, "Antenna height and terrain height must both be given.");
             return false;
         }
         if (addAntennaTypeComboBox.getSelectedIndex() == 2 &&
                 (addAntennaHeightTextField.getText().trim().isEmpty() ||
                 addTerrainHeightTextField.getText().trim().isEmpty() ||
                 addHeadingTextField.getText().trim().isEmpty() ||
                 addFieldOfViewAngleTextField.getText().trim().isEmpty() ||
                 addGainTextField.getText().trim().isEmpty())) {
             JOptionPane.showMessageDialog(addStationDialog, "Antenna height, terrain height, heading, field of view angle and gain must all be given.");
             return false;
         }
         try {
             if (!addAntennaHeightTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(addAntennaHeightTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(addStationDialog, "Antenna height is not a valid number.");                     
             return false;
         }  
         try {
             if (!addTerrainHeightTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(addTerrainHeightTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {        
             JOptionPane.showMessageDialog(addStationDialog, "Terrain height is not a valid number.");
             return false;
         }                                 
         try {
             if (!addHeadingTextField.getText().trim().isEmpty()) {
                 Integer.parseInt(addHeadingTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {        
             JOptionPane.showMessageDialog(addStationDialog, "Heading is not a valid integer.");
             return false;
         }  
         try {
             if (!addFieldOfViewAngleTextField.getText().trim().isEmpty()) {
                 Integer.parseInt(addFieldOfViewAngleTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(addStationDialog, "Field of view angle is not a valid integer.");                        
             return false;
         }      
         try {
             if (!addGainTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(addGainTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(addStationDialog, "Gain is not a valid number.");
             return false;
         }
         
         AISFixedStationData operativeStation = new AISFixedStationData();
         AISFixedStationData plannedStation = new AISFixedStationData();        
          
         try {                 
             operativeStation.setStationName(addStationNameTextField.getText().trim());
             plannedStation.setStationName(addStationNameTextField.getText().trim());
             if (addStationTypeComboBox.getSelectedIndex() == 0) {
                 operativeStation.setStationType(AISFixedStationType.BASESTATION);
                 plannedStation.setStationType(AISFixedStationType.BASESTATION);
             } else if (addStationTypeComboBox.getSelectedIndex() == 1) {
                 operativeStation.setStationType(AISFixedStationType.REPEATER); 
                 plannedStation.setStationType(AISFixedStationType.REPEATER);
             } else if (addStationTypeComboBox.getSelectedIndex() == 2) {
                 operativeStation.setStationType(AISFixedStationType.RECEIVER); 
                 plannedStation.setStationType(AISFixedStationType.RECEIVER); 
             } else if (addStationTypeComboBox.getSelectedIndex() == 3) {
                 operativeStation.setStationType(AISFixedStationType.ATON); 
                 plannedStation.setStationType(AISFixedStationType.ATON); 
             }  
             operativeStation.setLat(new Double(addLatitudeTextField.getText().replace(",", ".").trim()).doubleValue());                                
             plannedStation.setLat(new Double(addLatitudeTextField.getText().replace(",", ".").trim()).doubleValue()); 
             operativeStation.setLon(new Double(addLongitudeTextField.getText().replace(",", ".").trim()).doubleValue());  
             plannedStation.setLon(new Double(addLongitudeTextField.getText().replace(",", ".").trim()).doubleValue());  
             if (addMMSINumberTextField.getText().trim().isEmpty()) {
                 operativeStation.setMmsi(null);
                 plannedStation.setMmsi(null);
             } else {
                 operativeStation.setMmsi(addMMSINumberTextField.getText().trim());
                 plannedStation.setMmsi(addMMSINumberTextField.getText().trim());
             }
             if (addTransmissionPowerTextField.getText().trim().isEmpty()) {
                 operativeStation.setTransmissionPower(null);
                 plannedStation.setTransmissionPower(null);
             } else {
                 operativeStation.setTransmissionPower(new Double(addTransmissionPowerTextField.getText().replace(",", ".").trim()));
                 plannedStation.setTransmissionPower(new Double(addTransmissionPowerTextField.getText().replace(",", ".").trim()));                
             }
             AISFixedStationStatus status = new AISFixedStationStatus();
             status.setStatusID(DerbyDBInterface.STATUS_ACTIVE);
             operativeStation.setStatus(status);
             status = new AISFixedStationStatus();
             status.setStatusID(DerbyDBInterface.STATUS_PLANNED);
             plannedStation.setStatus(status);
             Antenna antenna = operativeStation.getAntenna();
             if (addAntennaTypeComboBox.getSelectedIndex() == 0) {
                 operativeStation.setAntenna(null);
                 plannedStation.setAntenna(null);
             } else if (addAntennaTypeComboBox.getSelectedIndex() == 1) {
                 if (antenna == null) {
                     antenna = new Antenna();
                 }
                 antenna.setAntennaType(AntennaType.OMNIDIRECTIONAL);                    
             } else if (addAntennaTypeComboBox.getSelectedIndex() == 2) {
                 if (antenna == null) {
                     antenna = new Antenna();
                 }
                 antenna.setAntennaType(AntennaType.DIRECTIONAL);
             }
             if (addAntennaTypeComboBox.getSelectedIndex() == 1 ||
                     addAntennaTypeComboBox.getSelectedIndex() == 2) {
                 if (!addAntennaHeightTextField.getText().trim().isEmpty()) {
                     antenna.setAntennaHeight(new Double(addAntennaHeightTextField.getText().replace(",", ".").trim()).doubleValue());
                 }
                 if (!addTerrainHeightTextField.getText().trim().isEmpty()) {
                     antenna.setTerrainHeight(new Double(addTerrainHeightTextField.getText().replace(",", ".").trim()).doubleValue());
                 }
             }
             if (addAntennaTypeComboBox.getSelectedIndex() == 2) {
                 if (addHeadingTextField.getText().trim().isEmpty()) {                        
                     antenna.setHeading(null);
                 } else {
                     antenna.setHeading(new Integer(addHeadingTextField.getText().trim()));
                 }
                 if (addFieldOfViewAngleTextField.getText().trim().isEmpty()) { 
                     antenna.setFieldOfViewAngle(null);
                 } else {
                     antenna.setFieldOfViewAngle(new Integer(addFieldOfViewAngleTextField.getText().trim()));
                 }
                 if (addGainTextField.getText().trim().isEmpty()) {             
                     antenna.setGain(null);
                 } else {
                     antenna.setGain(new Double(addGainTextField.getText().replace(",", ".").trim()));
                 }
             }
             if (addAntennaTypeComboBox.getSelectedIndex() == 1 ||
                     addAntennaTypeComboBox.getSelectedIndex() == 2) {
                 operativeStation.setAntenna(antenna);
                 plannedStation.setAntenna(antenna);
             }
             if (addAdditionalInformationJTextArea.getText().trim().isEmpty()) {
                 operativeStation.setDescription(null);
                 plannedStation.setDescription(null);
             } else {
                 operativeStation.setDescription(addAdditionalInformationJTextArea.getText().trim());
                 plannedStation.setDescription(addAdditionalInformationJTextArea.getText().trim());
             }
         } catch (IllegalArgumentException e) {
             JOptionPane.showMessageDialog(addStationDialog, e.getMessage());              
             return false;
         }            
 
         if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {            
 
             ActiveStation activeStation = new ActiveStation();
             List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();
             stations.add(operativeStation);
             stations.add(plannedStation);            
             activeStation.setStations(stations);                      
             
             // FOR TESTING -->
             /*
             EAVDAMUser testUser = new EAVDAMUser();
             testUser.setOrganizationName("test");
             AISFixedStationData testData = new AISFixedStationData();
             testData.setStationName("test");
             testData.setLat(62.2);
             testData.setLon(19.2);
             Map<EAVDAMUser, AISFixedStationData> proposals = new HashMap<EAVDAMUser, AISFixedStationData>();
             proposals.put(testUser, testData);
             activeStation.setProposals(proposals);            
             */
             // <-- FOR TESTING
                         
             List<ActiveStation> activeStations = null;
             if (data == null || data.getActiveStations() == null) {
                 activeStations =  new ArrayList<ActiveStation>();
             } else {
                 activeStations =  data.getActiveStations();
             }
             activeStations.add(activeStation);                
             data.setActiveStations(activeStations);
   
         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {
             if (data != null && data.getSimulatedStations() != null) {                
                 for (Simulation s : data.getSimulatedStations()) {
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(s.getName())) {
                         List<AISFixedStationData> stations = s.getStations();
                         if (stations == null) {
                             stations = new ArrayList<AISFixedStationData>();
                         }
                         AISFixedStationStatus status = new AISFixedStationStatus();
                         status.setStatusID(DerbyDBInterface.STATUS_SIMULATED);
                         operativeStation.setStatus(status);
                         stations.add(operativeStation);                        
                     }
                 }
             }
         }
 
         DBHandler.saveData(data);
         
         return true;
     }
 
     /** 
      * Updates select station ComboBox.
      *
      * @param stationIndex Index of the station to be selected
      */    
     private void updateSelectStationComboBox(int selectedIndex) {
         
         ignoreListeners = true;
         
         selectStationComboBox.removeAllItems();
         
         if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {
             if (data != null && data.getActiveStations() != null) {
                 if (initiallySelectedStationName != null) {
                     for (int i=0; i< data.getActiveStations().size(); i++) {
                         ActiveStation as = data.getActiveStations().get(i);
                         if (as.getStations() != null) {
                             for (AISFixedStationData stationData : as.getStations()) {
                                 if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE &&
                                         initiallySelectedStationName.equals(stationData.getStationName())) {
                                     selectedIndex = i;
                                     break;                                                                    
                                 }
                             }
                         }
                     }
                 }
                 
                 for (int i=0; i< data.getActiveStations().size(); i++) {
                     ActiveStation as = data.getActiveStations().get(i);
                     if (as.getStations() != null) {
                         for (AISFixedStationData stationData : as.getStations()) {                            
                             if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
                                 selectStationComboBox.addItem(stationData.getStationName());
                                 if (selectedIndex == i) {
                                     selectStationComboBox.setSelectedIndex(i);
                                 }
                             }
                         }
                     }
                 }
                 
             }
                 
         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {
 
             if (data != null && data.getSimulatedStations() != null) {
                 
                 if (initiallySelectedStationName != null) {
                     for (Simulation s : data.getSimulatedStations()) {
                         if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(s.getName())) {
                             List<AISFixedStationData> stations = s.getStations();
                             if (stations != null) {
                                 for (int i=0; i<stations.size(); i++) {
                                     if (stations.get(i).getStationName().equals(initiallySelectedStationName)) {
                                         selectedIndex = i;
                                         break;
                                     }
                                 }                                
                             }
                         }
                     }
                 }
 
                 for (Simulation s : data.getSimulatedStations()) {
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(s.getName())) {
                         List<AISFixedStationData> stations = s.getStations();
                         if (stations != null) {
                             for (int i=0; i<stations.size(); i++) {
                                 AISFixedStationData stationData = stations.get(i);
                                 selectStationComboBox.addItem(stationData.getStationName());
                                 if (selectedIndex == i) {
                                     selectStationComboBox.setSelectedIndex(i);
                                 }
                             }                                
                         }
                     }
                 }
                 
             }
     
         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {    
 
             if (data != null && data.getOtherUsersStations() != null) {
                 
                 if (initiallySelectedStationName != null) {
                     for (OtherUserStations ous : data.getOtherUsersStations()) {
                         if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(ous.getUser().getOrganizationName())) {
                             List<ActiveStation> stations = ous.getStations();
                             if (stations != null) {
                                 for (int i=0; i<stations.size(); i++) {
                                     ActiveStation as = stations.get(i);
                                      if (as.getStations() != null) {
                                         for (AISFixedStationData stationData : as.getStations()) {
                                             if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE &&
                                                     initiallySelectedStationName.equals(stationData.getStationName())) {
                                                 selectedIndex = i;
                                                 break;                                                                                            
                                             }                   
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
 
                 for (OtherUserStations ous : data.getOtherUsersStations()) {
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(ous.getUser().getOrganizationName())) {
                         List<ActiveStation> stations = ous.getStations();
                         if (stations != null) {
                             for (int i=0; i<stations.size(); i++) {
                                 ActiveStation as = stations.get(i);
                                 if (as.getStations() != null) {
                                     for (AISFixedStationData stationData : as.getStations()) {
                                         if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {                                                                                               
                                             selectStationComboBox.addItem(stationData.getStationName());                                            
                                             if (selectedIndex == i) {
                                                 selectStationComboBox.setSelectedIndex(i);
                                             }                                      
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
                 
             }
         }    
         ignoreListeners = false;   
     }
     
     private boolean alreadyExists(int ignoreIndex, String stationName) {
         
         if (data == null) {
             return false;
         }
     
         if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {            
             if (data.getActiveStations() != null) {
                 for (int i=0; i< data.getActiveStations().size(); i++) {
                     if (i != ignoreIndex) {
                         ActiveStation as = data.getActiveStations().get(i);
                         if (as.getStations() != null && !as.getStations().isEmpty()) {
                             AISFixedStationData stationData = as.getStations().get(0);
                             if (stationData.getStationName().equals(stationName)) {
                                 return true;
                             }
                         }
                     }
                 }
             }
                    
         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {
             if (data.getSimulatedStations() != null) {
                 for (Simulation s : data.getSimulatedStations()) {
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(s.getName())) {
                         List<AISFixedStationData> stations = s.getStations();
                         if (stations != null) {
                             for (int i=0; i<stations.size(); i++) {
                                 if (i != ignoreIndex) {
                                     if (stations.get(i).getStationName().equals(stationName)) {
                                         return true;
                                     }
                                 }
                             }                                
                         }
                     }
                 }
             }
     
         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {
             if (data.getOtherUsersStations() != null) {                
                 for (OtherUserStations ous : data.getOtherUsersStations()) {
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(ous.getUser().getOrganizationName())) {
                         List<ActiveStation> stations = ous.getStations();
                         if (stations != null) {
                             for (int i=0; i<stations.size(); i++) {
                                 if (i != ignoreIndex) {
                                     ActiveStation as = stations.get(i);
                                     if (as.getStations() != null && !as.getStations().isEmpty()) {
                                         AISFixedStationData stationData = as.getStations().get(0);
                                         if (stationName.equals(stationData.getStationName())) {
                                             return true;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         
         return false;
     }    
     
     /**
      * Saves station's data.
      *
      * @param stationIndex  Index of the station in the stations list
      * @return  True if the data was saved or false if it was not
      */
     private boolean saveStation(int stationIndex) {
 
         if (data == null) {
             data = new EAVDAMData();
         }
         
         if (alreadyExists(stationIndex, stationNameTextField.getText())) {
             JOptionPane.showMessageDialog(dialog, "A station with the given name already exists. " +
                 "Please, select another name for the station.");                 
             return false;
         }
 
         if (latitudeTextField.getText().trim().isEmpty()) {
             JOptionPane.showMessageDialog(dialog, "Latitude is mandatory.");
             return false;
         } else {
             if (!latitudeTextField.getText().trim().isEmpty()) {
                 try {
                     Double.parseDouble(latitudeTextField.getText().replace(",", ".").trim());                    
                 } catch (NumberFormatException ex) {
                     JOptionPane.showMessageDialog(dialog, "Latitude is not a valid number.");
                     return false;
                 }
             }
         }            
         if (longitudeTextField.getText().trim().isEmpty()) {          
             JOptionPane.showMessageDialog(dialog, "Longitude is mandatory.");                    
             return false;
         } else {                                                        
             if (!longitudeTextField.getText().trim().isEmpty()) {
                 try {
                     Double.parseDouble(longitudeTextField.getText().replace(",", ".").trim());
                 } catch (NumberFormatException ex) {
                     JOptionPane.showMessageDialog(dialog, "Longitude is not a valid number.");
                     return false;
                 }
             }
         }                    
         try {
             if (!transmissionPowerTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(transmissionPowerTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Transmission power is not a valid number.");
             return false;
         }   
         if (antennaTypeComboBox.getSelectedIndex() == 1 &&
                 (antennaHeightTextField.getText().trim().isEmpty() ||
                 terrainHeightTextField.getText().trim().isEmpty())) {
             JOptionPane.showMessageDialog(dialog, "Antenna height and terrain height must both be given.");
             return false;
         }
         if (antennaTypeComboBox.getSelectedIndex() == 2 &&
                 (antennaHeightTextField.getText().trim().isEmpty() ||
                 terrainHeightTextField.getText().trim().isEmpty() ||
                 headingTextField.getText().trim().isEmpty() ||
                 fieldOfViewAngleTextField.getText().trim().isEmpty() ||
                 gainTextField.getText().trim().isEmpty())) {
             JOptionPane.showMessageDialog(dialog, "Antenna height, terrain height, heading, field of view angle and gain must all be given.");
             return false;
         }
         try {
             if (!antennaHeightTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(antennaHeightTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Antenna height is not a valid number.");                     
             return false;
         }  
         try {
             if (!terrainHeightTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(terrainHeightTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Terrain height is not a valid number.");
             return false;            
         }                                 
         try {
             if (!headingTextField.getText().trim().isEmpty()) {
                 Integer.parseInt(headingTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Heading is not a valid integer.");
             return false;
         }  
         try {
             if (!fieldOfViewAngleTextField.getText().trim().isEmpty()) {
                 Integer.parseInt(fieldOfViewAngleTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Field of view angle is not a valid integer.");                        
             return false;
         }      
         try {
             if (!gainTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(gainTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Gain is not a valid number.");
             return false;
         }                          
     
         AISFixedStationData station = getSelectedStationData(stationIndex);
         if (station == null) {
             station = new AISFixedStationData();
         }
 
         try {
             station.setStationName(stationNameTextField.getText().trim());
             if (stationTypeComboBox.getSelectedIndex() == 0) {
                 station.setStationType(AISFixedStationType.BASESTATION);
             } else if (stationTypeComboBox.getSelectedIndex() == 1) {
                 station.setStationType(AISFixedStationType.REPEATER); 
             } else if (stationTypeComboBox.getSelectedIndex() == 2) {
                 station.setStationType(AISFixedStationType.RECEIVER); 
             } else if (stationTypeComboBox.getSelectedIndex() == 3) {
                 station.setStationType(AISFixedStationType.ATON); 
             }  
             station.setLat(new Double(latitudeTextField.getText().replace(",", ".").trim()).doubleValue());                                
             station.setLon(new Double(longitudeTextField.getText().replace(",", ".").trim()).doubleValue());  
             if (mmsiNumberTextField.getText().trim().isEmpty()) {
                 station.setMmsi(null);
             } else {
                 station.setMmsi(mmsiNumberTextField.getText().trim());
             }
             if (transmissionPowerTextField.getText().trim().isEmpty()) {
                 station.setTransmissionPower(null);
             } else {
                 station.setTransmissionPower(new Double(transmissionPowerTextField.getText().replace(",", ".").trim()));
             }
             Antenna antenna = station.getAntenna();
             if (antennaTypeComboBox.getSelectedIndex() == 0) {
                 station.setAntenna(null);
             } else if (antennaTypeComboBox.getSelectedIndex() == 1) {
                 if (antenna == null) {
                     antenna = new Antenna();
                 }
                 antenna.setAntennaType(AntennaType.OMNIDIRECTIONAL);                    
             } else if (antennaTypeComboBox.getSelectedIndex() == 2) {
                 if (antenna == null) {
                     antenna = new Antenna();
                 }
                 antenna.setAntennaType(AntennaType.DIRECTIONAL);
             }
             if (antennaTypeComboBox.getSelectedIndex() == 1 ||
                     antennaTypeComboBox.getSelectedIndex() == 2) {
                 if (!antennaHeightTextField.getText().trim().isEmpty()) {
                     antenna.setAntennaHeight(new Double(antennaHeightTextField.getText().replace(",", ".").trim()).doubleValue());
                 }
                 if (!terrainHeightTextField.getText().trim().isEmpty()) {
                     antenna.setTerrainHeight(new Double(terrainHeightTextField.getText().replace(",", ".").trim()).doubleValue());
                 }
             }
             if (antennaTypeComboBox.getSelectedIndex() == 2) {
                 if (headingTextField.getText().trim().isEmpty()) {                        
                     antenna.setHeading(null);
                 } else {
                     antenna.setHeading(new Integer(headingTextField.getText().trim()));
                 }
                 if (fieldOfViewAngleTextField.getText().trim().isEmpty()) { 
                     antenna.setFieldOfViewAngle(null);
                 } else {
                     antenna.setFieldOfViewAngle(new Integer(fieldOfViewAngleTextField.getText().trim()));
                 }
                 if (gainTextField.getText().trim().isEmpty()) {             
                     antenna.setGain(null);
                 } else {
                     antenna.setGain(new Double(gainTextField.getText().replace(",", ".").trim()));
                 }
             }
             if (antennaTypeComboBox.getSelectedIndex() == 1 ||
                     antennaTypeComboBox.getSelectedIndex() == 2) {
                 station.setAntenna(antenna);
             }
             if (additionalInformationJTextArea.getText().trim().isEmpty()) {
                 station.setDescription(null);
             } else {
                 station.setDescription(additionalInformationJTextArea.getText().trim());
             }
         } catch (IllegalArgumentException e) {
             JOptionPane.showMessageDialog(dialog, e.getMessage());              
             return false;
         }
 
 //        System.out.println("SAVING: "+station.getLat()+" vs. "+station.getLon());
         
         if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {
             if (data != null && data.getActiveStations() != null && stationIndex < data.getActiveStations().size()) {
                 ActiveStation as = data.getActiveStations().get(stationIndex);
                 if (as.getStations() != null) {
                     for (int i=0; i<as.getStations().size(); i++) {                        
                         AISFixedStationData temp = as.getStations().get(i);
                         if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.OPERATIVE_LABEL) && temp.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
                             as.getStations().set(i, station);                            
                             break;
                         } else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL) && temp.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED) {
                             as.getStations().set(i, station);                            
                             break;
                         }
                     }
                 }
                 data.getActiveStations().set(stationIndex, as);
             }
                    
         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {
             if (data != null && data.getSimulatedStations() != null) {                
                 List<Simulation> simulatedStations = data.getSimulatedStations();
                 for (Simulation s : data.getSimulatedStations()) {
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(s.getName())) {
                         List<AISFixedStationData> stations = s.getStations();
                         if (stations != null && stationIndex < stations.size()) {                            
                             stations.set(stationIndex, station);
                             s.setStations(stations);
                             data.setSimulatedStations(simulatedStations);
                             break;
                         }
                     }
                 }
             }
         
         } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL)) {
             if (data != null && data.getOtherUsersStations() != null) {                
                 for (int i=0; i<data.getOtherUsersStations().size(); i++) {
                     OtherUserStations ous = data.getOtherUsersStations().get(i);
                     if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(ous.getUser().getOrganizationName())) {
                         List<ActiveStation> stations = ous.getStations();
                         if (stations != null && stationIndex < stations.size()) {
                             ActiveStation as = stations.get(stationIndex);     
                             EAVDAMUser user = data.getUser();                            
                             Map<EAVDAMUser, AISFixedStationData> proposals = new HashMap<EAVDAMUser, AISFixedStationData>();
                             proposals.put(user, station);                                                        
                             as.setProposals(proposals);
                             ous.getStations().set(stationIndex, as);                        
                             data.getOtherUsersStations().set(i, ous);
                             break;
                         }
                     }
                 }
             }
         }
         
         DBHandler.saveData(data);    
         
         return true;   
     }
       
     /**
      * Turns planned station into operative station.
      *
      * @param stationIndex  Index of the station in the stations list
      */
     private void turnPlannedIntoOperativeStation(int stationIndex) {
 
         if (data == null ||  data.getActiveStations() == null || data.getActiveStations().isEmpty()) {
             return;
         }
      
  
         AISFixedStationData newActiveStationData = new AISFixedStationData();
         try {
             newActiveStationData.setStationName(stationNameTextField.getText().trim());
             if (stationTypeComboBox.getSelectedIndex() == 0) {
                 newActiveStationData.setStationType(AISFixedStationType.BASESTATION);
             } else if (stationTypeComboBox.getSelectedIndex() == 1) {
                 newActiveStationData.setStationType(AISFixedStationType.REPEATER); 
             } else if (stationTypeComboBox.getSelectedIndex() == 2) {
                 newActiveStationData.setStationType(AISFixedStationType.RECEIVER); 
             } else if (stationTypeComboBox.getSelectedIndex() == 3) {
                 newActiveStationData.setStationType(AISFixedStationType.ATON); 
             }  
             newActiveStationData.setLat(new Double(latitudeTextField.getText().replace(",", ".").trim()).doubleValue());                                
             newActiveStationData.setLon(new Double(longitudeTextField.getText().replace(",", ".").trim()).doubleValue());  
             if (mmsiNumberTextField.getText().trim().isEmpty()) {
                 newActiveStationData.setMmsi(null);
             } else {
                 newActiveStationData.setMmsi(mmsiNumberTextField.getText().trim());
             }
             if (transmissionPowerTextField.getText().trim().isEmpty()) {
                 newActiveStationData.setTransmissionPower(null);
             } else {
                 newActiveStationData.setTransmissionPower(new Double(transmissionPowerTextField.getText().replace(",", ".").trim()));
             }
             Antenna antenna = new Antenna();
             if (antennaTypeComboBox.getSelectedIndex() == 0) {
                 newActiveStationData.setAntenna(null);
             } else if (antennaTypeComboBox.getSelectedIndex() == 1) {
                 antenna.setAntennaType(AntennaType.OMNIDIRECTIONAL);                    
             } else if (antennaTypeComboBox.getSelectedIndex() == 2) {
                 antenna.setAntennaType(AntennaType.DIRECTIONAL);
             }
             if (antennaTypeComboBox.getSelectedIndex() == 1 ||
                     antennaTypeComboBox.getSelectedIndex() == 2) {
                 if (!antennaHeightTextField.getText().trim().isEmpty()) {
                     antenna.setAntennaHeight(new Double(antennaHeightTextField.getText().replace(",", ".").trim()).doubleValue());
                 }
                 if (!terrainHeightTextField.getText().trim().isEmpty()) {
                     antenna.setTerrainHeight(new Double(terrainHeightTextField.getText().replace(",", ".").trim()).doubleValue());
                 }
             }
             if (antennaTypeComboBox.getSelectedIndex() == 2) {
                 if (headingTextField.getText().trim().isEmpty()) {                        
                     antenna.setHeading(null);
                 } else {
                     antenna.setHeading(new Integer(headingTextField.getText().trim()));
                 }
                 if (fieldOfViewAngleTextField.getText().trim().isEmpty()) { 
                     antenna.setFieldOfViewAngle(null);
                 } else {
                     antenna.setFieldOfViewAngle(new Integer(fieldOfViewAngleTextField.getText().trim()));
                 }
                 if (gainTextField.getText().trim().isEmpty()) {             
                     antenna.setGain(null);
                 } else {
                     antenna.setGain(new Double(gainTextField.getText().replace(",", ".").trim()));
                 }
             }
             if (antennaTypeComboBox.getSelectedIndex() == 1 ||
                     antennaTypeComboBox.getSelectedIndex() == 2) {
                 newActiveStationData.setAntenna(antenna);
             }
             if (additionalInformationJTextArea.getText().trim().isEmpty()) {
                 newActiveStationData.setDescription(null);
             } else {
                 newActiveStationData.setDescription(additionalInformationJTextArea.getText().trim());
             }
             AISFixedStationStatus status = new AISFixedStationStatus();
             status.setStatusID(DerbyDBInterface.STATUS_ACTIVE);
             status.setStartDate(new Date(System.currentTimeMillis()));
             newActiveStationData.setStatus(status);
         } catch (IllegalArgumentException e) {
             // should not occur as the station is saved
         }
      
         ActiveStation as = data.getActiveStations().get(stationIndex);        
         for (int i=0; i<as.getStations().size(); i++) {                        
             AISFixedStationData temp = as.getStations().get(i);
             if (temp.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
             	System.out.println("Found active station. Changing it to old...");
 //                temp.getStatus().setStatusID(DerbyDBInterface.STATUS_OLD);
 //                temp.getStatus().setStartDate(new Date(System.currentTimeMillis()));
 //                as.getStations().set(i, temp);
             	newActiveStationData.setStationDBID(temp.getStationDBID());
             	newActiveStationData.setOperator(temp.getOperator());
             	as.getStations().set(i, newActiveStationData);
                 break;
                 
             }
             
 //            System.out.println("ADDING: "+newActiveStationData.getLat()+" "+newActiveStationData.getLon());
 //            as.getStations().add(newActiveStationData);
             
             data.getActiveStations().set(stationIndex, as);            
         }
             
         DBHandler.saveData(data);
     }
 
     /** 
      * Deletes a station.
      *
      * @param stationIndex  Index of the station to be deleted
      */
     private void deleteStation(int stationIndex) {        
         if (data != null) {            
             if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {            
                 if (data != null && data.getActiveStations() != null && stationIndex < data.getActiveStations().size()) {
 
                 	//Create an object that tells which station is removed = Is stored as an old station...
                 	EAVDAMData remove = new EAVDAMData();
                 	remove.setUser(data.getUser());
                 	List<ActiveStation> list = new ArrayList<ActiveStation>();
                 	AISFixedStationData removeAIS = data.getActiveStations().get(stationIndex).getStations().get(0);
                 	AISFixedStationStatus status = new AISFixedStationStatus();
                 	status.setEndDate(new Date(System.currentTimeMillis()));
                 	status.setStatusID(DerbyDBInterface.STATUS_OLD);
                 	removeAIS.setStatus(status);
                 	ActiveStation as = new ActiveStation();
                 	List<AISFixedStationData> aisList = new ArrayList<AISFixedStationData>();
                 	aisList.add(removeAIS);
                 	as.setStations(aisList);
                 	list.add(as);
                 	remove.setActiveStations(list);
                 	DBHandler.saveData(remove);
                 	
                 			
                 	data.getActiveStations().remove(stationIndex);   
                     
                 }
                        
             } else if (((String) selectDatasetComboBox.getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {
                 if (data != null && data.getSimulatedStations() != null) {                
                     List<Simulation> simulatedStations = data.getSimulatedStations();
                     for (Simulation s : data.getSimulatedStations()) {
                         if (((String) selectDatasetComboBox.getSelectedItem()).endsWith(s.getName())) {
                             s.getStations().remove(stationIndex);
                             data.setSimulatedStations(simulatedStations);
                             break;
                         }
                     }
                 }
             }            
             
 //            DBHandler.saveData(data);         
         }
     }
     
     private void deleteProposalFrom(int stationIndex, String organizationName) {
         if (data != null) {
             ActiveStation as = data.getActiveStations().get(stationIndex);
             Map<EAVDAMUser, AISFixedStationData> proposals = as.getProposals();
             EAVDAMUser user  = null;
             for (Object key : proposals.keySet()) { 
                 EAVDAMUser temp = (EAVDAMUser) key;
                 if (temp.getOrganizationName().equals(organizationName)) {
                     user = temp;
                     break;                    
                 }
             }
             if (user != null) {
                 proposals.remove(user);
             }
             as.setProposals(proposals);
             data.getActiveStations().set(stationIndex, as);
             DBHandler.saveData(data);         
         }
     }
     
     private void addProposal(int stationIndex, String organizationName) {
         if (data != null) {            
             for (int i=0; i<data.getOtherUsersStations().size(); i++) {
                 OtherUserStations ous = data.getOtherUsersStations().get(i);
                 if (ous.getUser().getOrganizationName().equals(organizationName)) {
                     if (ous.getStations() != null && stationIndex < ous.getStations().size()) {            
                         ActiveStation as = ous.getStations().get(stationIndex);
                         AISFixedStationData currentData = getSelectedStationData(stationIndex);                        
                         Map<EAVDAMUser, AISFixedStationData> proposals = new HashMap<EAVDAMUser, AISFixedStationData>();            
                         EAVDAMUser user = data.getUser();
                         AISFixedStationData proposedData = new AISFixedStationData();
                         proposedData.setStationName(currentData.getStationName());
                         proposedData.setLat(currentData.getLat());
                         proposedData.setLon(currentData.getLon());
                         proposedData.setMmsi(currentData.getMmsi());
                         proposedData.setTransmissionPower(currentData.getTransmissionPower());
                         proposedData.setDescription(currentData.getDescription());
                        proposedData.setCoverage(currentData.getCoverage());
                         proposedData.setAntenna(currentData.getAntenna());
                         proposedData.setFatdmaAllocation(currentData.getFatdmaAllocation());                                                                                                                                                                        
                         proposedData.setStationType(currentData.getStationType());
                         proposedData.setOperator(currentData.getOperator());  
                         proposedData.setStatus(currentData.getStatus());  
                         proposedData.setAnything(currentData.getAnything());   
                         proposals.put(user, proposedData);
                         as.setProposals(proposals);
                         ous.getStations().set(stationIndex, as);                        
                         data.getOtherUsersStations().set(i, ous);
                         DBHandler.saveData(data);
                         break;
                     }
                 }
             }
         }
     }        
 
     private void deleteProposalTo(int stationIndex, String organizationName) {
         if (data != null) {            
             for (int i=0; i<data.getOtherUsersStations().size(); i++) {
                 OtherUserStations ous = data.getOtherUsersStations().get(i);
                 if (ous.getUser().getOrganizationName().equals(organizationName)) {
                     if (ous.getStations() != null && stationIndex < ous.getStations().size()) {            
                         ActiveStation as = ous.getStations().get(stationIndex);
                         as.setProposals(null);
                         ous.getStations().set(stationIndex, as);
                         data.getOtherUsersStations().set(i, ous);
                         DBHandler.saveData(data);
                         break;
                     }
                 }
             }
         }
     }
 
     /** 
      * Checks whether the form fields have changed.
      *
      * @param stationIndex Index of the station
      * @return  True if the fields have changed, false if not
      */
     private boolean isChanged(int stationIndex) {
         
         if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(StationInformationMenuItem.PLANNED_LABEL) ||
                 tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.SIMULATED_LABEL) ||
                 tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).startsWith(StationInformationMenuItem.PROPOSAL_TO_LABEL)) {
 
             AISFixedStationData station = getSelectedStationData(stationIndex);
 
             if (station == null) {
                 return true;
             }
             if (station.getStationName() == null && !stationNameTextField.getText().isEmpty()) {
                 return true;
             }         
             if (!station.getStationName().equals(stationNameTextField.getText())) {
                 return true;
             }          
             if (station.getStationType() == null) {
                 return true;
             }          
             if (station.getStationType() == AISFixedStationType.BASESTATION && stationTypeComboBox.getSelectedIndex() != 0) {
                 return true;
             }
             if (station.getStationType() == AISFixedStationType.REPEATER && stationTypeComboBox.getSelectedIndex() != 1) {
                 return true;
             }           
             if (station.getStationType() == AISFixedStationType.RECEIVER && stationTypeComboBox.getSelectedIndex() != 2) {
                 return true;
             }        
             if (station.getStationType() == AISFixedStationType.ATON && stationTypeComboBox.getSelectedIndex() != 3) {
                 return true;
             }           
             try {
                 if (Double.isNaN(station.getLat()) && !latitudeTextField.getText().isEmpty()) {
                     return true;
                 }
                 if (!Double.isNaN(station.getLat()) && station.getLat() != (new Double(latitudeTextField.getText().replace(",", ".").trim()).doubleValue())) {
                     return true;
                 }
             } catch (NumberFormatException ex) {
                 return true;
             }          
             try {
                 if (Double.isNaN(station.getLon()) && !longitudeTextField.getText().isEmpty()) {
                     return true;
                 }
                 if (!Double.isNaN(station.getLon()) && station.getLon() != (new Double(longitudeTextField.getText().replace(",", ".").trim()).doubleValue())) {
                     return true;
                 }
             } catch (NumberFormatException ex) {
                 return true;
             }
             if (station.getMmsi() == null && !mmsiNumberTextField.getText().isEmpty()) {
                 return true;
             }                     
             if (station.getMmsi() != null && !station.getMmsi().equals(mmsiNumberTextField.getText())) {
                 return true;
             }     
             if (station.getTransmissionPower() == null && !transmissionPowerTextField.getText().isEmpty()) {
                 return true;
             }
             try {       
                 if (station.getTransmissionPower() != null && !station.getTransmissionPower().equals(new Double(transmissionPowerTextField.getText().replace(",", ".").trim()))) {
                     return true;
                 }
             } catch (NumberFormatException ex) {
                     return true;          
             }          
             if (station.getStatus() == null) {
                 return true;
             }     
             Antenna antenna = station.getAntenna();
             if (antenna == null && antennaTypeComboBox.getSelectedIndex() != 0) {
                 return true;
             }
             if (antenna != null && antenna.getAntennaType() == null && antennaTypeComboBox.getSelectedIndex() != 0) {
                 return true;
             }
             if (antenna != null && antenna.getAntennaType() == AntennaType.OMNIDIRECTIONAL && antennaTypeComboBox.getSelectedIndex() != 1) {
                 return true;
             }
             if (antenna != null && antenna.getAntennaType() == AntennaType.DIRECTIONAL && antennaTypeComboBox.getSelectedIndex() != 2) {
                 return true;
             }       
                        
             if (antenna != null && Double.isNaN(antenna.getAntennaHeight()) && !antennaHeightTextField.getText().isEmpty()) {
                 return true;
             }        
             if (antenna != null && !Double.isNaN(antenna.getAntennaHeight())) {
                 try {       
                     if (antenna.getAntennaHeight() != new Double(antennaHeightTextField.getText().replace(",", ".").trim()).doubleValue()) {
                         return true;
                     }
                 } catch (NumberFormatException ex) {
                     return true;
                 }
             }                          
             if (antenna != null && Double.isNaN(antenna.getTerrainHeight()) && !terrainHeightTextField.getText().isEmpty()) {
                 return true;
             }
             if (antenna != null && !Double.isNaN(antenna.getTerrainHeight())) {            
                 try {       
                     if (antenna.getTerrainHeight() != new Double(terrainHeightTextField.getText().replace(",", ".").trim()).doubleValue()) {
                         return true;
                     }                
                 } catch (NumberFormatException ex) {
                     return true;
                 }
             }                                             
             if (antenna != null && antenna.getAntennaType() == AntennaType.DIRECTIONAL) {
                 if (antenna.getHeading() == null && !headingTextField.getText().isEmpty()) {
                     return true;
                 }
                 try {       
                     if (antenna.getHeading() != null && !antenna.getHeading().equals(new Integer(headingTextField.getText()))) {
                         return true;
                     }
                 } catch (NumberFormatException ex) {
                     return true;
                 }              
                 if (antenna.getFieldOfViewAngle() == null && !fieldOfViewAngleTextField.getText().isEmpty()) {
                     return true;
                 }
                 try {       
                     if (antenna.getFieldOfViewAngle() != null && !antenna.getFieldOfViewAngle().equals(new Integer(fieldOfViewAngleTextField.getText()))) {
                         return true;
                     }
                 } catch (NumberFormatException ex) {
                     return true;
                 }         
                 if (antenna.getGain() == null && !gainTextField.getText().isEmpty()) {
                     return true;
                 }
                 try {       
                     if (antenna.getGain() != null && !antenna.getGain().equals(new Integer(gainTextField.getText()))) {
                         return true;
                     }
                 } catch (NumberFormatException ex) {
                     return true;
                 }
             }         
             if (station.getDescription() == null && !additionalInformationJTextArea.getText().isEmpty()) {
                 return true;
             }         
             if (station.getDescription() != null && !station.getDescription().equals(additionalInformationJTextArea.getText())) {
                 return true;
             }
             return false;
         
         } else {
             return false;
         }
     }
 
 }
