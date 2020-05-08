 package dk.frv.eavdam.menus;
 
 import dk.frv.eavdam.data.ActiveStation;
 import dk.frv.eavdam.data.AISAtonStationFATDMAChannel;
 import dk.frv.eavdam.data.AISBaseAndReceiverStationFATDMAChannel;
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISFixedStationStatus;
 import dk.frv.eavdam.data.AISFixedStationType;
 import dk.frv.eavdam.data.AtonMessageBroadcastRate;
 import dk.frv.eavdam.data.Antenna;
 import dk.frv.eavdam.data.AntennaType;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.FATDMACell;
 import dk.frv.eavdam.data.FATDMAChannel;
 import dk.frv.eavdam.data.FATDMADefaultChannel;
 import dk.frv.eavdam.data.FATDMANode;
 import dk.frv.eavdam.data.FATDMAReservation;
 import dk.frv.eavdam.data.Simulation;
 import dk.frv.eavdam.io.DefaultFATDMAReader;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 import dk.frv.eavdam.layers.FATDMAGridLayer;
 import dk.frv.eavdam.utils.DBHandler;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 
 public class AddStationDialog extends JDialog implements ActionListener, ItemListener {
 
     public static final long serialVersionUID = 1L;
 
 	public static int WINDOW_WIDTH = 980;
 	public static int WINDOW_HEIGHT = 840;		
 	
 	private StationInformationMenuItem menuItem;
 	
     private JTextField addStationNameTextField;
     private JComboBox addStationTypeComboBox;
 	private JComboBox addStationStatusComboBox;
     private JTextField addLatitudeTextField;
     private JTextField addLongitudeTextField;    
     private JTextField addMMSINumberTextField;
     private JTextField addTransmissionPowerTextField;
 
     private JComboBox addAntennaTypeComboBox;
     private JTextField addAntennaHeightTextField;
     private JTextField addTerrainHeightTextField;    
     private JTextField addHeadingTextField;
     private JTextField addFieldOfViewAngleTextField;
     private JTextField addGainTextField;   
 	
 	private JPanel addStationFATDMAPanel;
 	private JComboBox addStationChannelAComboBox;
 	private JScrollPane addStationChannelAScrollPane;
 	private JPanel channelAPanel;
 	private JComboBox addStationChannelBComboBox;
 	private JScrollPane addStationChannelBScrollPane;
 	private JPanel channelBPanel;
 	
 	private JButton chooseDefaultFATDMASchemeButton;
 	private JDialog chooseDefaultFATDMASchemeDialog;
 	private JComboBox selectIALADefaultFATDMASchemeComboBox;
 	private JRadioButton semaphoreModeRadioButton;
 	private JRadioButton nonSemaphoreModeRadioButton;
 	private JCheckBox fatdmaReservationOnChACheckBox;
 	private JCheckBox fatdmaReservationOnChBCheckBox;
 	private JCheckBox oneAdditionalTimeslotOnChACheckBox;
 	private JCheckBox oneAdditionalTimeslotOnChBCheckBox;
 	private JComboBox additionalBlocksForChAComboBox;
 	private JComboBox additionalBlocksForChBComboBox;
 	private JComboBox additionalTimeslotsForChAComboBox;
 	private JComboBox additionalTimeslotsForChBComboBox;
 	private JTextField chATimeslotsReservedTextField;
 	private JTextField chBTimeslotsReservedTextField;	
 	private JButton okDefaultFATDMASchemeButton;
 	private JButton cancelDefaultFATDMASchemeButton;
 	
     private JTextArea addAdditionalInformationJTextArea;
     
     private JButton doAddStationButton;
     private JButton cancelAddStationButton;	
 	
 	private int oldAddStationTypeIndex = -1;
 	private int oldAddStationChannelAIndex = -1;
 	private int oldAddStationChannelBIndex = -1;
 
 	public AddStationDialog(StationInformationMenuItem menuItem) {
 
 		super(menuItem.getEavdamMenu().getOpenMapFrame(), "Add Station", false);  // true for modal dialog
 		
 		this.menuItem = menuItem;
 
 		addStationNameTextField = menuItem.getTextField(16);
 		addStationTypeComboBox = menuItem.getComboBox(new String[] {"AIS Base Station", "AIS Repeater", "AIS Receiver station", "AIS AtoN station"});
 		addStationTypeComboBox.addItemListener(this);
 		addStationStatusComboBox = menuItem.getComboBox(new String[] {"Operative", "Planned"});
 		addStationStatusComboBox.addItemListener(this);
 
 		addLatitudeTextField = menuItem.getTextField(16);
 		addLongitudeTextField = menuItem.getTextField(16);
 		addMMSINumberTextField = menuItem.getTextField(16);
 		addTransmissionPowerTextField = menuItem.getTextField(16);
 		addStationStatusComboBox = menuItem.getComboBox(new String[] {StationInformationMenuItem.OPERATIVE_LABEL, StationInformationMenuItem.PLANNED_LABEL});
 		addStationStatusComboBox.addItemListener(this);
 
 		addAntennaTypeComboBox = menuItem.getComboBox(new String[] {"No antenna", "Omnidirectional", "Directional"});
 		addAntennaTypeComboBox.addItemListener(this);
 		addAntennaTypeComboBox.setSelectedIndex(0);      
 		addAntennaHeightTextField = menuItem.getTextField(16);            
 		addTerrainHeightTextField = menuItem.getTextField(16);
 		addHeadingTextField = menuItem.getTextField(16);
 		addFieldOfViewAngleTextField = menuItem.getTextField(16);           
 		addGainTextField = menuItem.getTextField(16);
 		
 		addStationChannelAComboBox = menuItem.getComboBox(new String[] {"NULL", "AIS1", "AIS2"});
 		addStationChannelAComboBox.addItemListener(this);
 		addStationChannelBComboBox = menuItem.getComboBox(new String[] {"NULL", "AIS1", "AIS2"});
 		addStationChannelBComboBox.addItemListener(this);
 		chooseDefaultFATDMASchemeButton = menuItem.getButton("Choose IALA default FATDMA scheme", 250);
 		chooseDefaultFATDMASchemeButton.addActionListener(this);
 		selectIALADefaultFATDMASchemeComboBox = menuItem.getComboBox(new String[] {"1-I", "1-II", "2-I", "2-II",
 			"3-I", "3-II", "4-I", "4-II", "5-I", "5-II", "6-I", "6-II", "7-I", "7-II", "8-I", "8-II",
 			"9-I", "9-II", "10-I", "10-II", "11-I", "11-II", "12-I", "12-II", "13-I", "13-II", "14-I", "14-II", 
 			"15-I", "15-II", "16-I", "16-II", "17-I", "17-II", "18-I", "18-II", "19-I", "19-II", "20-I", "20-II", 
 			"21-I", "21-II", "22-I", "22-II", "23-I", "23-II", "24-I", "24-II", "25-I", "25-II", "26-I", "26-II",
 			"27-I", "27-II", "28-I", "28-II", "29-I", "29-II", "30-I", "30-II", "31-I", "31-II", "32-I", "32-II",
 			"33-I", "33-II", "34-I", "34-II", "35-I", "35-II", "36-I", "36-II"});
 		semaphoreModeRadioButton = new JRadioButton("Semaphore mode");
 		semaphoreModeRadioButton.addItemListener(this);
 		nonSemaphoreModeRadioButton = new JRadioButton("Non-semaphore mode");	
 		nonSemaphoreModeRadioButton.setSelected(true);
 		nonSemaphoreModeRadioButton.addItemListener(this);
 		ButtonGroup group = new ButtonGroup();
 		group.add(semaphoreModeRadioButton);
 		group.add(nonSemaphoreModeRadioButton);
 		fatdmaReservationOnChACheckBox = new JCheckBox("FATDMA reservation on CH A");
 		fatdmaReservationOnChACheckBox.setSelected(true);
 		fatdmaReservationOnChACheckBox.addItemListener(this);
 		fatdmaReservationOnChBCheckBox = new JCheckBox("FATDMA reservation on CH B");
 		fatdmaReservationOnChBCheckBox.setSelected(true);
 		fatdmaReservationOnChBCheckBox.addItemListener(this);
 		oneAdditionalTimeslotOnChACheckBox = new JCheckBox("One additional timeslot on CH A");
 		oneAdditionalTimeslotOnChACheckBox.addItemListener(this);
 		oneAdditionalTimeslotOnChBCheckBox = new JCheckBox("One additional timeslot on CH B");
 		oneAdditionalTimeslotOnChBCheckBox.addItemListener(this);
 		additionalBlocksForChAComboBox = menuItem.getComboBox(new String[] {"0", "1", "2"});
 		additionalBlocksForChAComboBox.addItemListener(this);
 		additionalBlocksForChBComboBox = menuItem.getComboBox(new String[] {"0", "1", "2"});
 		additionalBlocksForChBComboBox.addItemListener(this);
 		additionalTimeslotsForChAComboBox = menuItem.getComboBox(new String[] {"1", "2", "3"});
 		additionalTimeslotsForChAComboBox.addItemListener(this);
 		additionalTimeslotsForChBComboBox = menuItem.getComboBox(new String[] {"1", "2", "3"});
 		additionalTimeslotsForChBComboBox.addItemListener(this);
 		chATimeslotsReservedTextField = menuItem.getTextField(4);
 		chATimeslotsReservedTextField.setEditable(false);
 		chBTimeslotsReservedTextField = menuItem.getTextField(4);
 		chBTimeslotsReservedTextField.setEditable(false);
 	    okDefaultFATDMASchemeButton = menuItem.getButton("Ok", 100);
 		okDefaultFATDMASchemeButton.addActionListener(this);
 	    cancelDefaultFATDMASchemeButton = menuItem.getButton("Cancel", 100);
 		cancelDefaultFATDMASchemeButton.addActionListener(this);		
 				
 		addAdditionalInformationJTextArea = menuItem.getTextArea("");
 		
 		doAddStationButton = menuItem.getButton("Add station", 140);  
 		doAddStationButton.addActionListener(this);
 		cancelAddStationButton = menuItem.getButton("Cancel", 100);  
 		cancelAddStationButton.addActionListener(this);          
 		
 		updateAntennaTypeComboBox();
 			
 		if (((String) menuItem.getSelectDatasetComboBox().getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {   
 			addStationStatusComboBox = menuItem.getComboBox(new String[] {"Operative", "Planned"});	
 		} else if (((String) menuItem.getSelectDatasetComboBox().getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {				
 			addStationStatusComboBox = menuItem.getComboBox(new String[] {"Simulation"});	
 		}						
 					
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridBagLayout());
 						  
 		JPanel p2 = new JPanel(new GridBagLayout());
 		p2.setBorder(BorderFactory.createTitledBorder("General information"));
 		GridBagConstraints c = new GridBagConstraints();
 		c = menuItem.updateGBC(c, 0, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));        
 		p2.add(new JLabel("Station name:"), c);
 		c = menuItem.updateGBC(c, 1, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));               
 		p2.add(addStationNameTextField, c);                    
 		c = menuItem.updateGBC(c, 0, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
 		p2.add(new JLabel("Station type:"), c);
 		c = menuItem.updateGBC(c, 1, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));    
 		p2.add(addStationTypeComboBox, c);     
 		c = menuItem.updateGBC(c, 0, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
 		p2.add(new JLabel("Station status:"), c);
 		c = menuItem.updateGBC(c, 1, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));    
 		p2.add(addStationStatusComboBox, c);     			
 		c = menuItem.updateGBC(c, 0, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                        
 		p2.add(new JLabel("Latitude (WGS84):"), c);
 		c = menuItem.updateGBC(c, 1, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                   
 		p2.add(addLatitudeTextField, c);                    
 		c = menuItem.updateGBC(c, 0, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                 
 		p2.add(new JLabel("Longitude (WGS84):"), c);
 		c = menuItem.updateGBC(c, 1, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                   
 		p2.add(addLongitudeTextField, c);        
 		c = menuItem.updateGBC(c, 0, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                
 		p2.add(new JLabel("MMSI number:"), c);
 		c = menuItem.updateGBC(c, 1, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
 		p2.add(addMMSINumberTextField, c);
 		c = menuItem.updateGBC(c, 0, 6, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                
 		p2.add(new JLabel("Transmission power (Watt):"), c);
 		c = menuItem.updateGBC(c, 1, 6, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                   
 		p2.add(addTransmissionPowerTextField, c);
 		c = menuItem.updateGBC(c, 0, 7, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));              
 		/*
 		p2.add(new JLabel("Status of the fixed AIS station:"), c);
 		c = updateGBC(c, 1, 6, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));               
 		p2.add(addStationStatusComboBox, c);
 		*/
 
 		c = menuItem.updateGBC(c, 0, 0, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5)); 
 		panel.add(p2, c);                    
 		  
 		JPanel p3 = new JPanel(new GridBagLayout());
 		p3.setBorder(BorderFactory.createTitledBorder("Antenna information"));
 		c = menuItem.updateGBC(c, 0, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5)); 
 		p3.add(new JLabel("Antenna type:"), c);
 		c = menuItem.updateGBC(c, 1, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
 		p3.add(addAntennaTypeComboBox, c);
 		c = menuItem.updateGBC(c, 0, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                      
 		p3.add(new JLabel("Antenna height above terrain (m):"), c);
 		c = menuItem.updateGBC(c, 1, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                
 		p3.add(addAntennaHeightTextField, c);                    
 		c = menuItem.updateGBC(c, 0, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                  
 		p3.add(new JLabel("Terrain height above sealevel (m):"), c);
 		c = menuItem.updateGBC(c, 1, 2, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                  
 		p3.add(addTerrainHeightTextField, c); 
 		c = menuItem.updateGBC(c, 0, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                     
 		p3.add(new JLabel("Heading (degrees - integer):"), c);
 		c = menuItem.updateGBC(c, 1, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                
 		p3.add(addHeadingTextField, c); 
 		c = menuItem.updateGBC(c, 0, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                        
 		p3.add(new JLabel("Field of View angle (degrees - integer)"), c);
 		c = menuItem.updateGBC(c, 1, 4, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                        
 		p3.add(addFieldOfViewAngleTextField, c);                                         
 		c = menuItem.updateGBC(c, 0, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                                   
 		p3.add(new JLabel("Gain (dB)"), c);
 		c = menuItem.updateGBC(c, 1, 5, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                      
 		p3.add(addGainTextField, c);                       
 
 		c = menuItem.updateGBC(c, 1, 0, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5)); 
 		panel.add(p3, c);                      
 
 		addStationFATDMAPanel = new JPanel(new GridBagLayout());
 		addStationFATDMAPanel.setBorder(BorderFactory.createTitledBorder("FATDMA information"));
 		c = menuItem.updateGBC(c, 0, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 		addStationFATDMAPanel.add(new JLabel("Channel A:"), c);
 		c = menuItem.updateGBC(c, 1, 0, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
 		addStationFATDMAPanel.add(addStationChannelAComboBox, c);
 
 		c = menuItem.updateGBC(c, 0, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 		addStationFATDMAPanel.add(new JLabel("Channel B:"), c);
 		c = menuItem.updateGBC(c, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
 		addStationFATDMAPanel.add(addStationChannelBComboBox, c);
 
 		c = menuItem.updateGBC(c, 0, 1, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5)); 
 		c.gridwidth = 2; 
 		panel.add(addStationFATDMAPanel, c); 
 		c.gridwidth = 1;
 		
 		addAdditionalInformationJTextArea.setLineWrap(true);
 		addAdditionalInformationJTextArea.setWrapStyleWord(true);                    
 		JScrollPane p5 = new JScrollPane(addAdditionalInformationJTextArea);
 		p5.setBorder(BorderFactory.createTitledBorder("Additional information"));
 		p5.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		p5.setPreferredSize(new Dimension(580, 90));
 		p5.setMaximumSize(new Dimension(580, 90));
 
 		c = menuItem.updateGBC(c, 0, 2, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(5,5,5,5)); 
 		c.gridwidth = 2; 
 		panel.add(p5, c);
 					
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.add(doAddStationButton);          
 		buttonPanel.add(cancelAddStationButton);
 		c = menuItem.updateGBC(c, 0, 3, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));             
 		c.gridwidth = 2; 
 		panel.add(buttonPanel, c);
 		
 		JScrollPane scrollPane = new JScrollPane(panel);
         scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
         scrollPane.setMaximumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
 
         getContentPane().add(scrollPane);
 		
 		addStationChannelAComboBox.setSelectedIndex(1);
 		addStationChannelBComboBox.setSelectedIndex(2);		
 	}
 	
     public void itemStateChanged(ItemEvent e) {
         
         if (menuItem.ignoreListeners) {
             return;
         }	
         if (addAntennaTypeComboBox != null && e.getItemSelectable() == addAntennaTypeComboBox && e.getStateChange() == ItemEvent.SELECTED) {            
             updateAntennaTypeComboBox();
 
 		} else if (addStationTypeComboBox != null && e.getItemSelectable() == addStationTypeComboBox && e.getStateChange() == ItemEvent.DESELECTED) {
 			if (oldAddStationTypeIndex == -1) {
 				for (int i=0; i< addStationTypeComboBox.getItemCount(); i++) {			
 					if (e.getItem() == addStationTypeComboBox.getItemAt(i)) {
 						oldAddStationTypeIndex = i;
 						break;
 					}	
 				}		
 			}		
 
 		} else if ((addStationTypeComboBox != null && e.getItemSelectable() == addStationTypeComboBox && e.getStateChange() == ItemEvent.SELECTED)) {
 			if (oldAddStationTypeIndex != -1) {
 				if ((channelAPanel == null || channelAPanel.getComponents().length < 2) &&
 						(channelBPanel == null || channelBPanel.getComponents().length < 2)) {
 					updateAddStationChannelComboBoxesAndScrollPanes(null, null, true, true, true, true, false, false);
 				} else {
 					int response = JOptionPane.showConfirmDialog(this, "Are you sure you want change the station type? This will reset the FATDMA information.", "Confirm action", JOptionPane.YES_NO_OPTION);
 					if (response == JOptionPane.YES_OPTION) {
 						updateAddStationChannelComboBoxesAndScrollPanes(null, null, true, true, true, true, false, false);
 					} else {
 						menuItem.ignoreListeners = true;
 						addStationTypeComboBox.setSelectedIndex(oldAddStationTypeIndex);						
 						menuItem.ignoreListeners = false;						
 					}				
 				}
 				oldAddStationTypeIndex = -1;	
 			}		
 			
 		} else if (addStationChannelAComboBox != null && e.getItemSelectable() == addStationChannelAComboBox && e.getStateChange() == ItemEvent.DESELECTED) {
 			if (oldAddStationChannelAIndex == -1) {
 				for (int i=0; i< addStationChannelAComboBox.getItemCount(); i++) {			
 					if (e.getItem() == addStationChannelAComboBox.getItemAt(i)) {
 						oldAddStationChannelAIndex = i;
 						break;
 					}
 				}
 			}			
 			
 		} else if (addStationChannelAComboBox != null && e.getItemSelectable() == addStationChannelAComboBox && e.getStateChange() == ItemEvent.SELECTED) {			
 			if (oldAddStationChannelAIndex != -1) {
 				if (channelAPanel == null || channelAPanel.getComponents().length < 2) {
 					updateAddStationChannelComboBoxesAndScrollPanes(null, null, true, false, true, false, false, false);
 				} else {
 					int response = JOptionPane.showConfirmDialog(this, "Are you sure you want change the channel A? This will reset the FATDMA information for it.", "Confirm action", JOptionPane.YES_NO_OPTION);
 					if (response == JOptionPane.YES_OPTION) {
 						updateAddStationChannelComboBoxesAndScrollPanes(null, null, true, false, true, false, false, false);
 					} else {
 						menuItem.ignoreListeners = true;
 						addStationChannelAComboBox.setSelectedIndex(oldAddStationChannelAIndex);
 						menuItem.ignoreListeners = false;
 					}
 				}
 				oldAddStationChannelAIndex = -1;				
 			}		
 		
 		} else if (addStationChannelBComboBox != null && e.getItemSelectable() == addStationChannelBComboBox && e.getStateChange() == ItemEvent.DESELECTED) {
 			if (oldAddStationChannelBIndex == -1) {
 				for (int i=0; i< addStationChannelBComboBox.getItemCount(); i++) {			
 					if (e.getItem() == addStationChannelBComboBox.getItemAt(i)) {
 						oldAddStationChannelBIndex = i;
 						break;
 					}
 				}
 			}					
 			
 		} else if (addStationChannelBComboBox != null && e.getItemSelectable() == addStationChannelBComboBox && e.getStateChange() == ItemEvent.SELECTED) {     
 			if (oldAddStationChannelBIndex != -1) {
 				if (channelBPanel == null || channelBPanel.getComponents().length < 2) {
 					updateAddStationChannelComboBoxesAndScrollPanes(null, null, false, true, false, true, false, false);
 				} else {
 					int response = JOptionPane.showConfirmDialog(this, "Are you sure you want change the channel B? This will reset the FATDMA information for it.", "Confirm action", JOptionPane.YES_NO_OPTION);
 					if (response == JOptionPane.YES_OPTION) {
 						updateAddStationChannelComboBoxesAndScrollPanes(null, null, false, true, false, true, false, false);
 					} else {
 						menuItem.ignoreListeners = true;					
 						addStationChannelBComboBox.setSelectedIndex(oldAddStationChannelBIndex);
 						menuItem.ignoreListeners = false;
 					}		
 				}
 				oldAddStationChannelBIndex = -1;				
 			}
 		
 		} else if ((semaphoreModeRadioButton != null && e.getItemSelectable() == semaphoreModeRadioButton) ||
 				(nonSemaphoreModeRadioButton != null && e.getItemSelectable() == nonSemaphoreModeRadioButton) ||
 				(fatdmaReservationOnChACheckBox != null && e.getItemSelectable() == fatdmaReservationOnChACheckBox) ||		
 				(fatdmaReservationOnChBCheckBox != null && e.getItemSelectable() == fatdmaReservationOnChBCheckBox) ||
 				(oneAdditionalTimeslotOnChACheckBox != null && e.getItemSelectable() == oneAdditionalTimeslotOnChACheckBox) ||
 				(oneAdditionalTimeslotOnChBCheckBox != null && e.getItemSelectable() == oneAdditionalTimeslotOnChBCheckBox) ||
 				(additionalBlocksForChAComboBox != null && e.getItemSelectable() == additionalBlocksForChAComboBox) ||						
 				(additionalBlocksForChBComboBox != null && e.getItemSelectable() == additionalBlocksForChBComboBox) ||	
 				(additionalTimeslotsForChAComboBox != null && e.getItemSelectable() == additionalTimeslotsForChAComboBox) ||
 				(additionalTimeslotsForChBComboBox != null && e.getItemSelectable() == additionalTimeslotsForChBComboBox)) {	
 			updateTimeslotsReservedTextFields();
 		}
 		
 	}
 	
     public void actionPerformed(ActionEvent e) {
         
         if (menuItem.ignoreListeners) {
             return;
         }
         
         if (doAddStationButton != null && e.getSource() == doAddStationButton) {                     
             boolean success = addStation();
             if (success) {
 				menuItem.stationAdded(addStationNameTextField.getText());
             }
             
         } else if (cancelAddStationButton != null && e.getSource() == cancelAddStationButton) {
             int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel?", "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
                 this.dispose();
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing
             }	
 			
 		} else if (chooseDefaultFATDMASchemeButton != null && e.getSource() == chooseDefaultFATDMASchemeButton) {
 		
 		    chooseDefaultFATDMASchemeDialog = new JDialog(menuItem.getEavdamMenu().getOpenMapFrame(), "Choose IALA default FATDMA scheme", true);
 			
 			JPanel panel = new JPanel();
 			panel.setLayout(new GridBagLayout());
 
 			GridBagConstraints c = new GridBagConstraints();
 			c = menuItem.updateGBC(c, 0, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			panel.add(new JLabel("Select from IALA default FATDMA scheme:"), c);
 
 			if (!addLatitudeTextField.getText().trim().isEmpty() && !addLongitudeTextField.getText().trim().isEmpty()) {
 				int cellNumber = getCellNumber();
 				if (cellNumber != -1) {
 					selectIALADefaultFATDMASchemeComboBox.setSelectedItem(String.valueOf(cellNumber) + "-I");
 				}
 			}
 			c = menuItem.updateGBC(c, 1, 0, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));               
 			panel.add(selectIALADefaultFATDMASchemeComboBox, c);
 	
 			/*	
 			c = menuItem.updateGBC(c, 0, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			c.gridwidth = 2;
 			panel.add(new JLabel("Reserve timeslots for transmission of:"), c);	
 		    */			
 						
 			c = menuItem.updateGBC(c, 0, 2, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			c.gridwidth = 1;
 			panel.add(new JLabel("Base Station Report in:"), c);
 			c = menuItem.updateGBC(c, 1, 2, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,0,5));			
 			panel.add(semaphoreModeRadioButton, c);
 			c = menuItem.updateGBC(c, 1, 3, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			panel.add(nonSemaphoreModeRadioButton, c);
 
 			c = menuItem.updateGBC(c, 0, 4, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			panel.add(fatdmaReservationOnChACheckBox, c);
 			c = menuItem.updateGBC(c, 1, 4, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			panel.add(fatdmaReservationOnChBCheckBox, c);
 
 			c = menuItem.updateGBC(c, 0, 5, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			panel.add(oneAdditionalTimeslotOnChACheckBox, c);
 			c = menuItem.updateGBC(c, 1, 5, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			panel.add(oneAdditionalTimeslotOnChBCheckBox, c);			
 
 			JPanel temp1 = new JPanel(new GridBagLayout());
 			c.gridwidth = 1;			
 			c = menuItem.updateGBC(c, 0, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			temp1.add(additionalBlocksForChAComboBox, c);
 			c = menuItem.updateGBC(c, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			temp1.add(new JLabel("Additional blocks of"), c);
 			c = menuItem.updateGBC(c, 0, 6, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));
 			panel.add(temp1, c);
 
 			JPanel temp2 = new JPanel(new GridBagLayout());
 			c.gridwidth = 1;			
 			c = menuItem.updateGBC(c, 0, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			temp2.add(additionalBlocksForChBComboBox, c);
 			c = menuItem.updateGBC(c, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			temp2.add(new JLabel("Additional blocks of"), c);
 			c = menuItem.updateGBC(c, 1, 6, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));
 			panel.add(temp2, c);			
 			
 			JPanel temp3 = new JPanel(new GridBagLayout());
 			c.gridwidth = 1;			
 			c = menuItem.updateGBC(c, 0, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			temp3.add(additionalTimeslotsForChAComboBox, c);
 			c = menuItem.updateGBC(c, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			temp3.add(new JLabel("timeslots on CH A"), c);
 			c = menuItem.updateGBC(c, 0, 7, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));
 			panel.add(temp3, c);
 
 			JPanel temp4 = new JPanel(new GridBagLayout());
 			c.gridwidth = 1;			
 			c = menuItem.updateGBC(c, 0, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			temp4.add(additionalTimeslotsForChBComboBox, c);
 			c = menuItem.updateGBC(c, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			temp4.add(new JLabel("timeslots on CH B"), c);
 			c = menuItem.updateGBC(c, 1, 7, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));
 			panel.add(temp4, c);			
 
 			c = menuItem.updateGBC(c, 0, 8, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			c.gridwidth = 2;
 			panel.add(new JLabel("Total timeslots reserved:"), c);				
 			
 			JPanel temp5 = new JPanel(new GridBagLayout());
 			c.gridwidth = 1;			
 			c = menuItem.updateGBC(c, 0, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			temp5.add(new JLabel("CH A:"), c);
 			c = menuItem.updateGBC(c, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			temp5.add(chATimeslotsReservedTextField, c);
 			c = menuItem.updateGBC(c, 0, 9, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));
 			panel.add(temp5, c);
 
 			JPanel temp6 = new JPanel(new GridBagLayout());
 			c = menuItem.updateGBC(c, 0, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 			temp6.add(new JLabel("CH B:"), c);			
 			c = menuItem.updateGBC(c, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			temp6.add(chBTimeslotsReservedTextField, c);
 			c = menuItem.updateGBC(c, 1, 9, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));
 			panel.add(temp6, c);
 
 			JPanel buttonPanel = new JPanel(new GridBagLayout());            
 			c = menuItem.updateGBC(c, 0, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5)); 
 			c.gridwidth = 1;     
             buttonPanel.add(okDefaultFATDMASchemeButton, c);
 			c = menuItem.updateGBC(c, 1, 0, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5)); 
 			buttonPanel.add(cancelDefaultFATDMASchemeButton, c);
 			
 			c = menuItem.updateGBC(c, 0, 10, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			c.gridwidth = 2;
 			panel.add(buttonPanel, c);		
 			
 			JScrollPane scrollPane = new JScrollPane(panel);
 			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 			scrollPane.setPreferredSize(new Dimension(540, 440));
 			scrollPane.setMaximumSize(new Dimension(540, 440));
 
 			chooseDefaultFATDMASchemeDialog.getContentPane().add(scrollPane);			
 			
 			updateTimeslotsReservedTextFields();
 			
      		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             chooseDefaultFATDMASchemeDialog.setBounds((int) screenSize.getWidth()/2 - 540/2, (int) screenSize.getHeight()/2 - 440/2, 540, 440);
             chooseDefaultFATDMASchemeDialog.setVisible(true);
 			
 		} else if (okDefaultFATDMASchemeButton != null && e.getSource() == okDefaultFATDMASchemeButton) {
 			menuItem.ignoreListeners = true;
 			Map<String,List<FATDMACell>> fatdmaCellsMap = DefaultFATDMAReader.readDefaultValues(null, null);
 			List<FATDMACell> fatdmaCells = fatdmaCellsMap.get((String) selectIALADefaultFATDMASchemeComboBox.getSelectedItem());
 			List<Component> initialChannelAComponents = new ArrayList<Component>();				
 			List<Component> initialChannelBComponents = new ArrayList<Component>();	
 			if (fatdmaCells.size() == 3) {				
 				FATDMACell baseStationFATDMACell = fatdmaCells.get(0);
 				FATDMADefaultChannel baseStationChannelA = baseStationFATDMACell.getChannelA();
 				FATDMANode channelAFATDMANode = null;
 				if (semaphoreModeRadioButton.isSelected()) {
 					channelAFATDMANode = baseStationChannelA.getSemaphoreNode();
 				} else {
 					channelAFATDMANode = baseStationChannelA.getNonSemaphoreNode(); 
 				}
 				FATDMADefaultChannel baseStationChannelB = baseStationFATDMACell.getChannelB();
 				FATDMANode channelBFATDMANode = null;
 				if (semaphoreModeRadioButton.isSelected()) {
 					channelBFATDMANode = baseStationChannelB.getSemaphoreNode();
 				} else {
 					channelBFATDMANode = baseStationChannelB.getNonSemaphoreNode(); 
 				}
 				if (addStationTypeComboBox.getSelectedIndex() == 0 || addStationTypeComboBox.getSelectedIndex() == 1) {  // base station or repeater	
 					initialChannelAComponents.add(new JTextField(channelAFATDMANode.getStartingSlot().toString(), 8));
 					initialChannelAComponents.add(new JTextField(channelAFATDMANode.getBlockSize().toString(), 8));
 					initialChannelAComponents.add(new JTextField(channelAFATDMANode.getIncrement().toString(), 8));
 					Component component = new JComboBox();									
 					((JComboBox) component).addItem("Local");
 					((JComboBox) component).addItem("Remote");
 					((JComboBox) component).setBorder(new EmptyBorder(0, 3, 0, 3));
 					((JComboBox) component).setSelectedItem("Local");						
 					initialChannelAComponents.add(component);
 					initialChannelAComponents.add(new JTextField(channelAFATDMANode.getUsage(), 8));
 					Component component2 = new JButton("Clear");	
 					((JButton) component2).setMargin(new Insets(0, 3, 0, 3));
 					((JButton) component2).addActionListener(this);
 					initialChannelAComponents.add(component2);						
 					initialChannelBComponents.add(new JTextField(channelBFATDMANode.getStartingSlot().toString(), 8));
 					initialChannelBComponents.add(new JTextField(channelBFATDMANode.getBlockSize().toString(), 8));
 					initialChannelBComponents.add(new JTextField(channelBFATDMANode.getIncrement().toString(), 8));
 					Component component3 = new JComboBox();									
 					((JComboBox) component3).addItem("Local");
 					((JComboBox) component3).addItem("Remote");
 					((JComboBox) component3).setBorder(new EmptyBorder(0, 3, 0, 3));
 					((JComboBox) component3).setSelectedItem("Local");					
 					initialChannelBComponents.add(component3);
 					initialChannelBComponents.add(new JTextField(channelBFATDMANode.getUsage(), 8));
 					Component component4 = new JButton("Clear");	
 					((JButton) component4).setMargin(new Insets(0, 3, 0, 3));
 					((JButton) component4).addActionListener(this);
 					initialChannelBComponents.add(component4);
 				} else if (addStationTypeComboBox.getSelectedIndex() == 3) {  // aton station
 					Component component = new JComboBox();									
 					((JComboBox) component).addItem("FATDMA");
 					((JComboBox) component).addItem("RATDMA");
 					((JComboBox) component).addItem("CSTDMA");
 					((JComboBox) component).setBorder(new EmptyBorder(0, 3, 0, 3));
 					((JComboBox) component).setSelectedItem("FATDMA");	
 					initialChannelAComponents.add(component);		
 					initialChannelAComponents.add(new JTextField("", 8));			
 					initialChannelAComponents.add(new JTextField("", 8));			
 					initialChannelAComponents.add(new JTextField("", 8));
 					initialChannelAComponents.add(new JTextField(channelAFATDMANode.getStartingSlot().toString(), 8));
 					initialChannelAComponents.add(new JTextField(channelAFATDMANode.getBlockSize().toString(), 8));
 					initialChannelAComponents.add(new JTextField(channelAFATDMANode.getIncrement().toString(), 8));
 					initialChannelAComponents.add(new JTextField(channelAFATDMANode.getUsage(), 8));
 					Component component2 = new JButton("Clear");	
 					((JButton) component2).setMargin(new Insets(0, 3, 0, 3));
 					((JButton) component2).addActionListener(this);
 					initialChannelAComponents.add(component2);		
 					Component component3 = new JComboBox();									
 					((JComboBox) component3).addItem("FATDMA");
 					((JComboBox) component3).addItem("RATDMA");
 					((JComboBox) component3).addItem("CSTDMA");
 					((JComboBox) component3).setBorder(new EmptyBorder(0, 3, 0, 3));
 					((JComboBox) component3).setSelectedItem("FATDMA");	
 					initialChannelBComponents.add(component3);		
 					initialChannelBComponents.add(new JTextField("", 8));			
 					initialChannelBComponents.add(new JTextField("", 8));			
 					initialChannelBComponents.add(new JTextField("", 8));
 					initialChannelBComponents.add(new JTextField(channelBFATDMANode.getStartingSlot().toString(), 8));
 					initialChannelBComponents.add(new JTextField(channelBFATDMANode.getBlockSize().toString(), 8));
 					initialChannelBComponents.add(new JTextField(channelBFATDMANode.getIncrement().toString(), 8));
 					initialChannelBComponents.add(new JTextField(channelBFATDMANode.getUsage(), 8));
 					Component component4 = new JButton("Clear");	
 					((JButton) component4).setMargin(new Insets(0, 3, 0, 3));
 					((JButton) component4).addActionListener(this);
 					initialChannelBComponents.add(component4);
 				}
 				 			
 				FATDMACell datalinkManagementFATDMACell = fatdmaCells.get(1);				
 				FATDMADefaultChannel datalinkManagementChannelA = datalinkManagementFATDMACell.getChannelA();
 				FATDMADefaultChannel datalinkManagementChannelB = datalinkManagementFATDMACell.getChannelB();
 				if (addStationTypeComboBox.getSelectedIndex() == 0 || addStationTypeComboBox.getSelectedIndex() == 1) {  // base station or repeater	
 					if (fatdmaReservationOnChACheckBox.isSelected()) {
 						initialChannelAComponents.add(new JTextField(datalinkManagementChannelA.getSemaphoreNode().getStartingSlot().toString(), 8));
 						initialChannelAComponents.add(new JTextField(datalinkManagementChannelA.getSemaphoreNode().getBlockSize().toString(), 8));
 						if (!oneAdditionalTimeslotOnChACheckBox.isSelected()) {
 							initialChannelAComponents.add(new JTextField(datalinkManagementChannelA.getSemaphoreNode().getIncrement().toString(), 8));
 					 	} else {
 							initialChannelAComponents.add(new JTextField(datalinkManagementChannelA.getNonSemaphoreNode().getIncrement().toString(), 8));
 						}
 						Component component = new JComboBox();									
 						((JComboBox) component).addItem("Local");
 						((JComboBox) component).addItem("Remote");
 						((JComboBox) component).setBorder(new EmptyBorder(0, 3, 0, 3));
 						((JComboBox) component).setSelectedItem("Local");						
 						initialChannelAComponents.add(component);
 						initialChannelAComponents.add(new JTextField(datalinkManagementChannelA.getSemaphoreNode().getUsage(), 8));
 						Component component2 = new JButton("Clear");	
 						((JButton) component2).setMargin(new Insets(0, 3, 0, 3));
 						((JButton) component2).addActionListener(this);
 						initialChannelAComponents.add(component2);
 					}
 					if (fatdmaReservationOnChBCheckBox.isSelected()) {					
 						initialChannelBComponents.add(new JTextField(datalinkManagementChannelB.getSemaphoreNode().getStartingSlot().toString(), 8));
 						initialChannelBComponents.add(new JTextField(datalinkManagementChannelB.getSemaphoreNode().getBlockSize().toString(), 8));
 						if (!oneAdditionalTimeslotOnChBCheckBox.isSelected()) {
 							initialChannelBComponents.add(new JTextField(datalinkManagementChannelB.getSemaphoreNode().getIncrement().toString(), 8));
 					 	} else {
 							initialChannelBComponents.add(new JTextField(datalinkManagementChannelB.getNonSemaphoreNode().getIncrement().toString(), 8));
 						}
 						Component component3 = new JComboBox();									
 						((JComboBox) component3).addItem("Local");
 						((JComboBox) component3).addItem("Remote");
 						((JComboBox) component3).setBorder(new EmptyBorder(0, 3, 0, 3));
 						((JComboBox) component3).setSelectedItem("Local");					
 						initialChannelBComponents.add(component3);
 						initialChannelBComponents.add(new JTextField(datalinkManagementChannelB.getNonSemaphoreNode().getUsage(), 8));
 						Component component4 = new JButton("Clear");	
 						((JButton) component4).setMargin(new Insets(0, 3, 0, 3));
 						((JButton) component4).addActionListener(this);
 						initialChannelBComponents.add(component4);
 					}
 				} else if (addStationTypeComboBox.getSelectedIndex() == 3) {  // aton station
 					if (fatdmaReservationOnChACheckBox.isSelected()) {
 						Component component = new JComboBox();									
 						((JComboBox) component).addItem("FATDMA");
 						((JComboBox) component).addItem("RATDMA");
 						((JComboBox) component).addItem("CSTDMA");
 						((JComboBox) component).setBorder(new EmptyBorder(0, 3, 0, 3));
 						((JComboBox) component).setSelectedItem("FATDMA");	
 						initialChannelAComponents.add(component);		
 						initialChannelAComponents.add(new JTextField("", 8));			
 						initialChannelAComponents.add(new JTextField("", 8));			
 						initialChannelAComponents.add(new JTextField("", 8));
 						initialChannelAComponents.add(new JTextField(datalinkManagementChannelA.getSemaphoreNode().getStartingSlot().toString(), 8));
 						initialChannelAComponents.add(new JTextField(datalinkManagementChannelA.getSemaphoreNode().getBlockSize().toString(), 8));
 						if (!oneAdditionalTimeslotOnChACheckBox.isSelected()) {
 							initialChannelAComponents.add(new JTextField(datalinkManagementChannelA.getSemaphoreNode().getIncrement().toString(), 8));
 					 	} else {
 							initialChannelAComponents.add(new JTextField(datalinkManagementChannelA.getNonSemaphoreNode().getIncrement().toString(), 8));
 						}						
 						initialChannelAComponents.add(new JTextField(datalinkManagementChannelA.getSemaphoreNode().getUsage(), 8));
 						Component component2 = new JButton("Clear");	
 						((JButton) component2).setMargin(new Insets(0, 3, 0, 3));
 						((JButton) component2).addActionListener(this);
 						initialChannelAComponents.add(component2);
 					}
 					if (fatdmaReservationOnChBCheckBox.isSelected()) {		
 						Component component3 = new JComboBox();									
 						((JComboBox) component3).addItem("FATDMA");
 						((JComboBox) component3).addItem("RATDMA");
 						((JComboBox) component3).addItem("CSTDMA");
 						((JComboBox) component3).setBorder(new EmptyBorder(0, 3, 0, 3));
 						((JComboBox) component3).setSelectedItem("FATDMA");	
 						initialChannelBComponents.add(component3);		
 						initialChannelBComponents.add(new JTextField("", 8));			
 						initialChannelBComponents.add(new JTextField("", 8));
 						initialChannelBComponents.add(new JTextField("", 8));					
 						initialChannelBComponents.add(new JTextField(datalinkManagementChannelB.getSemaphoreNode().getStartingSlot().toString(), 8));
 						initialChannelBComponents.add(new JTextField(datalinkManagementChannelB.getSemaphoreNode().getBlockSize().toString(), 8));
 						if (!oneAdditionalTimeslotOnChBCheckBox.isSelected()) {
 							initialChannelBComponents.add(new JTextField(datalinkManagementChannelB.getSemaphoreNode().getIncrement().toString(), 8));
 					 	} else {
 							initialChannelBComponents.add(new JTextField(datalinkManagementChannelB.getNonSemaphoreNode().getIncrement().toString(), 8));
 						}						
 						initialChannelBComponents.add(new JTextField(datalinkManagementChannelB.getNonSemaphoreNode().getUsage(), 8));
 						Component component4 = new JButton("Clear");	
 						((JButton) component4).setMargin(new Insets(0, 3, 0, 3));
 						((JButton) component4).addActionListener(this);
 						initialChannelBComponents.add(component4);
 					}
 				}
 
 				FATDMACell generalPurposeFATDMACell = fatdmaCells.get(2);
 				FATDMADefaultChannel generalPurposeChannelA = generalPurposeFATDMACell.getChannelA();
 				FATDMADefaultChannel generalPurposeChannelB = generalPurposeFATDMACell.getChannelB();
 				if (addStationTypeComboBox.getSelectedIndex() == 0 || addStationTypeComboBox.getSelectedIndex() == 1) {  // base station or repeater
 					if (Integer.parseInt((String) additionalBlocksForChAComboBox.getSelectedItem()) > 0) {
 						initialChannelAComponents.add(new JTextField(generalPurposeChannelA.getSemaphoreNode().getStartingSlot().toString(), 8));
 						initialChannelAComponents.add(new JTextField((String) additionalTimeslotsForChAComboBox.getSelectedItem(), 8));
 						if (Integer.parseInt((String) additionalBlocksForChAComboBox.getSelectedItem()) == 1) {
 							initialChannelAComponents.add(new JTextField(generalPurposeChannelA.getSemaphoreNode().getIncrement().toString(), 8));
 					 	} else if (Integer.parseInt((String) additionalBlocksForChAComboBox.getSelectedItem()) == 2) {
 							initialChannelAComponents.add(new JTextField(generalPurposeChannelA.getNonSemaphoreNode().getIncrement().toString(), 8));
 						}
 						Component component = new JComboBox();									
 						((JComboBox) component).addItem("Local");
 						((JComboBox) component).addItem("Remote");
 						((JComboBox) component).setBorder(new EmptyBorder(0, 3, 0, 3));
 						((JComboBox) component).setSelectedItem("Local");						
 						initialChannelAComponents.add(component);
 						initialChannelAComponents.add(new JTextField(generalPurposeChannelA.getSemaphoreNode().getUsage(), 8));
 						Component component2 = new JButton("Clear");	
 						((JButton) component2).setMargin(new Insets(0, 3, 0, 3));
 						((JButton) component2).addActionListener(this);
 						initialChannelAComponents.add(component2);
 					}
 					if (Integer.parseInt((String) additionalBlocksForChBComboBox.getSelectedItem()) > 0) {
 						initialChannelBComponents.add(new JTextField(generalPurposeChannelB.getSemaphoreNode().getStartingSlot().toString(), 8));
 						initialChannelBComponents.add(new JTextField((String) additionalTimeslotsForChBComboBox.getSelectedItem(), 8));
 						if (Integer.parseInt((String) additionalBlocksForChBComboBox.getSelectedItem()) == 1) {
 							initialChannelBComponents.add(new JTextField(generalPurposeChannelB.getSemaphoreNode().getIncrement().toString(), 8));
 					 	} else if (Integer.parseInt((String) additionalBlocksForChBComboBox.getSelectedItem()) == 2) {
 							initialChannelBComponents.add(new JTextField(generalPurposeChannelB.getNonSemaphoreNode().getIncrement().toString(), 8));
 						}
 						Component component3 = new JComboBox();									
 						((JComboBox) component3).addItem("Local");
 						((JComboBox) component3).addItem("Remote");
 						((JComboBox) component3).setBorder(new EmptyBorder(0, 3, 0, 3));
 						((JComboBox) component3).setSelectedItem("Local");					
 						initialChannelBComponents.add(component3);
 						initialChannelBComponents.add(new JTextField(generalPurposeChannelB.getNonSemaphoreNode().getUsage(), 8));
 						Component component4 = new JButton("Clear");	
 						((JButton) component4).setMargin(new Insets(0, 3, 0, 3));
 						((JButton) component4).addActionListener(this);
 						initialChannelBComponents.add(component4);
 					}
 				} else if (addStationTypeComboBox.getSelectedIndex() == 3) {  // aton station
 					if (Integer.parseInt((String) additionalBlocksForChAComboBox.getSelectedItem()) > 0) {
 						Component component = new JComboBox();	
 						((JComboBox) component).addItem("FATDMA");
 						((JComboBox) component).addItem("RATDMA");
 						((JComboBox) component).addItem("CSTDMA");
 						((JComboBox) component).setBorder(new EmptyBorder(0, 3, 0, 3));
 						((JComboBox) component).setSelectedItem("FATDMA");	
 						initialChannelAComponents.add(component);		
 						initialChannelAComponents.add(new JTextField("", 8));			
 						initialChannelAComponents.add(new JTextField("", 8));			
 						initialChannelAComponents.add(new JTextField("", 8));
 						initialChannelAComponents.add(new JTextField(generalPurposeChannelA.getSemaphoreNode().getStartingSlot().toString(), 8));
 						initialChannelAComponents.add(new JTextField((String) additionalTimeslotsForChAComboBox.getSelectedItem(), 8));
 						if (Integer.parseInt((String) additionalBlocksForChAComboBox.getSelectedItem()) == 1) {
 							initialChannelAComponents.add(new JTextField(generalPurposeChannelA.getSemaphoreNode().getIncrement().toString(), 8));
 					 	} else if (Integer.parseInt((String) additionalBlocksForChAComboBox.getSelectedItem()) == 2) {
 							initialChannelAComponents.add(new JTextField(generalPurposeChannelA.getNonSemaphoreNode().getIncrement().toString(), 8));
 						}
 						initialChannelAComponents.add(new JTextField(generalPurposeChannelA.getSemaphoreNode().getUsage(), 8));
 						Component component2 = new JButton("Clear");	
 						((JButton) component2).setMargin(new Insets(0, 3, 0, 3));
 						((JButton) component2).addActionListener(this);
 						initialChannelAComponents.add(component2);
 					}
 					if (Integer.parseInt((String) additionalBlocksForChBComboBox.getSelectedItem()) > 0) {					
 						Component component3 = new JComboBox();									
 						((JComboBox) component3).addItem("FATDMA");
 						((JComboBox) component3).addItem("RATDMA");
 						((JComboBox) component3).addItem("CSTDMA");
 						((JComboBox) component3).setBorder(new EmptyBorder(0, 3, 0, 3));
 						((JComboBox) component3).setSelectedItem("FATDMA");	
 						initialChannelBComponents.add(component3);		
 						initialChannelBComponents.add(new JTextField("", 8));			
 						initialChannelBComponents.add(new JTextField("", 8));
 						initialChannelBComponents.add(new JTextField("", 8));	
 						initialChannelBComponents.add(new JTextField(generalPurposeChannelB.getSemaphoreNode().getStartingSlot().toString(), 8));
 						initialChannelBComponents.add(new JTextField((String) additionalTimeslotsForChBComboBox.getSelectedItem(), 8));
 						if (Integer.parseInt((String) additionalBlocksForChBComboBox.getSelectedItem()) == 1) {
 							initialChannelBComponents.add(new JTextField(generalPurposeChannelB.getSemaphoreNode().getIncrement().toString(), 8));
 					 	} else if (Integer.parseInt((String) additionalBlocksForChBComboBox.getSelectedItem()) == 2) {
 							initialChannelBComponents.add(new JTextField(generalPurposeChannelB.getNonSemaphoreNode().getIncrement().toString(), 8));
 						}
 						initialChannelBComponents.add(new JTextField(generalPurposeChannelB.getNonSemaphoreNode().getUsage(), 8));
 						Component component4 = new JButton("Clear");	
 						((JButton) component4).setMargin(new Insets(0, 3, 0, 3));
 						((JButton) component4).addActionListener(this);
 						initialChannelBComponents.add(component4);
 					}
 				}
 			}
 			
 			updateAddStationChannelComboBoxesAndScrollPanes(initialChannelAComponents, initialChannelBComponents, true, true, true, true, false, false);			
 			chooseDefaultFATDMASchemeDialog.dispose();
 			menuItem.ignoreListeners = false;			
 		
 		} else if (cancelDefaultFATDMASchemeButton != null && e.getSource() == cancelDefaultFATDMASchemeButton) {
 			chooseDefaultFATDMASchemeDialog.dispose();
 
 		} else {
 				
 			if (channelAPanel != null) {
 				Component[] components = channelAPanel.getComponents();
 				for (int i=0; i<components.length; i++) {
 					if (e.getSource() instanceof JButton && e.getSource() == components[i]) {				
 						JButton button = (JButton) e.getSource();
 						if (button.getText().equals("Add row")) {						
 							updateAddStationChannelComboBoxesAndScrollPanes(null, null, false, false, false, false, true, false);					
 						} else if (button.getText().equals("Clear")) {
 							//int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the row?", "Confirm action", JOptionPane.YES_NO_OPTION);
 							//if (response == JOptionPane.YES_OPTION) {
 								clearRow("A", i);
 							//}
 						}
 					}
 				}
 			}
 
 			if (channelBPanel != null) {
 				Component[] components = channelBPanel.getComponents();
 				for (int i=0; i<components.length; i++) {						
 					if (e.getSource() instanceof JButton && e.getSource() == components[i]) {
 						JButton button = (JButton) e.getSource();
 						if (button.getText().equals("Add row")) {
 							updateAddStationChannelComboBoxesAndScrollPanes(null, null, false, false, false, false, false, true);
 						} else if (button.getText().equals("Clear")) {
 							//int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the row?", "Confirm action", JOptionPane.YES_NO_OPTION);
 							//if (response == JOptionPane.YES_OPTION) {							
 								clearRow("B", i);
 							//}
 						}			
 					}				
 				}
 			}
 		}
 	}
 	
 	private void updateTimeslotsReservedTextFields() {
 	
 		int timeslotsReservedForChannelA = 0;
 		int timeslotsReservedForChannelB = 0;		
 	
 		Map<String,List<FATDMACell>> fatdmaCellsMap = DefaultFATDMAReader.readDefaultValues(null, null);
 		List<FATDMACell> fatdmaCells = fatdmaCellsMap.get((String) selectIALADefaultFATDMASchemeComboBox.getSelectedItem());			
 
 		if (fatdmaCells.size() == 3) {			
 		
 			FATDMACell baseStationFATDMACell = fatdmaCells.get(0);
 
 			FATDMADefaultChannel baseStationChannelA = baseStationFATDMACell.getChannelA();
 			FATDMANode channelAFATDMANode = null;
 			if (semaphoreModeRadioButton.isSelected()) {
 				channelAFATDMANode = baseStationChannelA.getSemaphoreNode();
 			} else {
 				channelAFATDMANode = baseStationChannelA.getNonSemaphoreNode(); 
 			}
 			int startslot = channelAFATDMANode.getStartingSlot().intValue();
 			int blockSize = channelAFATDMANode.getBlockSize().intValue();
 			int increment = channelAFATDMANode.getIncrement().intValue();
 			timeslotsReservedForChannelA = increaseTimeslotsReserved(startslot, blockSize, increment, timeslotsReservedForChannelA);		
 			
 			FATDMADefaultChannel baseStationChannelB = baseStationFATDMACell.getChannelB();
 			FATDMANode channelBFATDMANode = null;
 			if (semaphoreModeRadioButton.isSelected()) {
 				channelBFATDMANode = baseStationChannelB.getSemaphoreNode();
 			} else {
 				channelBFATDMANode = baseStationChannelB.getNonSemaphoreNode(); 
 			}
 			startslot = channelBFATDMANode.getStartingSlot().intValue();
 			blockSize = channelBFATDMANode.getBlockSize().intValue();
 			increment = channelBFATDMANode.getIncrement().intValue();
 			timeslotsReservedForChannelB = increaseTimeslotsReserved(startslot, blockSize, increment, timeslotsReservedForChannelB);
 				
 			FATDMACell datalinkManagementFATDMACell = fatdmaCells.get(1);			
 			
 			FATDMADefaultChannel datalinkManagementChannelA = datalinkManagementFATDMACell.getChannelA();
 			if (fatdmaReservationOnChACheckBox.isSelected()) {
 				startslot = datalinkManagementChannelA.getSemaphoreNode().getStartingSlot().intValue();
 				blockSize = datalinkManagementChannelA.getSemaphoreNode().getBlockSize().intValue();
 				if (!oneAdditionalTimeslotOnChACheckBox.isSelected()) {
 					increment = datalinkManagementChannelA.getSemaphoreNode().getIncrement().intValue();
 				} else {
 					increment = datalinkManagementChannelA.getNonSemaphoreNode().getIncrement().intValue();
 				}
 				timeslotsReservedForChannelA = increaseTimeslotsReserved(startslot, blockSize, increment, timeslotsReservedForChannelA);
 			}								
 			FATDMADefaultChannel datalinkManagementChannelB = datalinkManagementFATDMACell.getChannelB();								
 			if (fatdmaReservationOnChBCheckBox.isSelected()) {
 				startslot = datalinkManagementChannelB.getSemaphoreNode().getStartingSlot().intValue();
 				blockSize = datalinkManagementChannelB.getSemaphoreNode().getBlockSize().intValue();
 				if (!oneAdditionalTimeslotOnChBCheckBox.isSelected()) {
 					increment = datalinkManagementChannelB.getSemaphoreNode().getIncrement().intValue();
 				} else {
 					increment = datalinkManagementChannelB.getNonSemaphoreNode().getIncrement().intValue();
 				}
 				timeslotsReservedForChannelB = increaseTimeslotsReserved(startslot, blockSize, increment, timeslotsReservedForChannelB);
 			}					
 
 			FATDMACell generalPurposeFATDMACell = fatdmaCells.get(2);
 			
 			FATDMADefaultChannel generalPurposeChannelA = generalPurposeFATDMACell.getChannelA();
 			if (Integer.parseInt((String) additionalBlocksForChAComboBox.getSelectedItem()) > 0) {
 				startslot = generalPurposeChannelA.getSemaphoreNode().getStartingSlot().intValue();
 				blockSize = Integer.parseInt((String) additionalTimeslotsForChAComboBox.getSelectedItem());
 				if (Integer.parseInt((String) additionalBlocksForChAComboBox.getSelectedItem()) == 1) {
 					increment = generalPurposeChannelA.getSemaphoreNode().getIncrement().intValue();
 				} else if (Integer.parseInt((String) additionalBlocksForChAComboBox.getSelectedItem()) == 2) {
 					increment = generalPurposeChannelA.getNonSemaphoreNode().getIncrement().intValue();
 				}
 				timeslotsReservedForChannelA = increaseTimeslotsReserved(startslot, blockSize, increment, timeslotsReservedForChannelA);				
 			}
 			
 			FATDMADefaultChannel generalPurposeChannelB = generalPurposeFATDMACell.getChannelB();			
 			if (Integer.parseInt((String) additionalBlocksForChBComboBox.getSelectedItem()) > 0) {
 				startslot = generalPurposeChannelB.getSemaphoreNode().getStartingSlot().intValue();
 				blockSize = Integer.parseInt((String) additionalTimeslotsForChBComboBox.getSelectedItem());
 				if (Integer.parseInt((String) additionalBlocksForChBComboBox.getSelectedItem()) == 1) {
 					increment = generalPurposeChannelB.getSemaphoreNode().getIncrement().intValue();
 				} else if (Integer.parseInt((String) additionalBlocksForChBComboBox.getSelectedItem()) == 2) {
 					increment = generalPurposeChannelB.getNonSemaphoreNode().getIncrement().intValue();
 				}
 				timeslotsReservedForChannelB = increaseTimeslotsReserved(startslot, blockSize, increment, timeslotsReservedForChannelB);				
 			}
 
 		}			
 		
 		chATimeslotsReservedTextField.setText(String.valueOf(timeslotsReservedForChannelA));
 		chBTimeslotsReservedTextField.setText(String.valueOf(timeslotsReservedForChannelB));
 	}
 	
 	private int increaseTimeslotsReserved(int startslot, int blockSize, int increment, int timeslotsReserved) {
 		if (increment == 0) {
 			for (int i=0; i<blockSize; i++) {
 				timeslotsReserved++;
 			}	
 		} else if (increment > 0) {					
 			int i = 0;
 			while (i*increment <= 2249) {							
 				for (int j=0; j<blockSize; j++) {
 					timeslotsReserved++;
 				}
 				i++;
 			}		
 		}
 		return timeslotsReserved;
 	}	
 
     private void updateAntennaTypeComboBox() {
         if (addAntennaTypeComboBox.getSelectedIndex() == 0) {  // no antenna         
             addAntennaHeightTextField.setText("");
             addAntennaHeightTextField.setEnabled(false);
             addTerrainHeightTextField.setText("");
             addTerrainHeightTextField.setEnabled(false);
             addHeadingTextField.setText("");
             addHeadingTextField.setEnabled(false);
             addFieldOfViewAngleTextField.setText("");
             addFieldOfViewAngleTextField.setEnabled(false);
             addGainTextField.setText("");
             addGainTextField.setEnabled(false); 
         } else if (addAntennaTypeComboBox.getSelectedIndex() == 1) {  // omnidirectional
 			addAntennaHeightTextField.setEnabled(true);
 			addTerrainHeightTextField.setEnabled(true);
 			addHeadingTextField.setText("");
             addHeadingTextField.setEnabled(false);
             addFieldOfViewAngleTextField.setText("");
             addFieldOfViewAngleTextField.setEnabled(false);
             addGainTextField.setText("");
             addGainTextField.setEnabled(false); 
         } else if (addAntennaTypeComboBox.getSelectedIndex() == 2) {  // directional 
 			addAntennaHeightTextField.setEnabled(true);
 			addTerrainHeightTextField.setEnabled(true);
             addHeadingTextField.setEnabled(true);
             addFieldOfViewAngleTextField.setEnabled(true);
             addGainTextField.setEnabled(true); 
         } 
     }	
 	
 	private int getCellNumber() {
 
 		if (!addLatitudeTextField.getText().trim().isEmpty() && !addLongitudeTextField.getText().trim().isEmpty()) {
 		
 			try {			
 				float lat = Float.valueOf(addLatitudeTextField.getText());
 				float lon = Float.valueOf(addLongitudeTextField.getText());
 		
 				int singleCellSizeInNauticalMiles = 30;
 				int noOfSingleCellsAlongOneSideOfMasterCell = 6;
 				int masterCellSizeInNauticalMiles = singleCellSizeInNauticalMiles * noOfSingleCellsAlongOneSideOfMasterCell;		
 				int noOfMasterCellsAroundEquator = (int) (360.0d * 60.0d / masterCellSizeInNauticalMiles);
 				float masterCellSizeInDegreesLatitude = (float) masterCellSizeInNauticalMiles / 60;  	
 				float singleCellHeightInDegrees = masterCellSizeInDegreesLatitude / noOfSingleCellsAlongOneSideOfMasterCell;
 
 				int masterCellRowNo = (int) (Math.abs(lat + (float) 0.5*singleCellHeightInDegrees) / masterCellSizeInDegreesLatitude);
 				double masterCellMeanLatitude = (masterCellRowNo + 0.5) * masterCellSizeInDegreesLatitude;
 				int noOfMasterCellsAroundMasterCellRow = (int) (noOfMasterCellsAroundEquator * Math.cos(2*Math.PI*masterCellMeanLatitude/360.0));
 				float singleCellWidthInDegrees = (float) 360/(noOfSingleCellsAlongOneSideOfMasterCell*noOfMasterCellsAroundMasterCellRow);	
 									
 				return FATDMAGridLayer.getCellNo(lat, lon, singleCellSizeInNauticalMiles, noOfSingleCellsAlongOneSideOfMasterCell, masterCellRowNo, singleCellWidthInDegrees);
 			
 			} catch (NumberFormatException e) {}	
 		}
 		return -1;
 	}
 	
 	private void updateAddStationChannelComboBoxesAndScrollPanes(List<Component> initialChannelAComponents, List<Component> initialChannelBComponents,
 			boolean clearChannelA, boolean clearChannelB, boolean initChannelA, boolean initChannelB, boolean addRowToChannelA, boolean addRowToChannelB) {
 	
 		menuItem.ignoreListeners = true;
 	
 		GridBagConstraints c = new GridBagConstraints();	
 
 		Component[] channelAComponents = new Component[0];
 
 		if (channelAPanel != null) {
 			if (!clearChannelA) {
 				channelAComponents = channelAPanel.getComponents();		
 			}
 			channelAPanel.removeAll();
 		}
 		
 		Component[] channelBComponents = new Component[0];	
 		
 		if (channelBPanel != null) {
 			if (!clearChannelB) {
 				channelBComponents = channelBPanel.getComponents();				
 			}
 			channelBPanel.removeAll();	
 		}
 		
 		if (addStationFATDMAPanel != null) {
 			addStationFATDMAPanel.removeAll();		
 		}
 	
 		if (addStationTypeComboBox.getSelectedIndex() == 2) {  // receiver
 			
 			// receivers don't have any FATDMA information
 			
 		} else if (addStationTypeComboBox.getSelectedIndex() == 0 || addStationTypeComboBox.getSelectedIndex() == 1) {  // base station or repeater	
 
             c = menuItem.updateGBC(c, 0, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
             addStationFATDMAPanel.add(new JLabel("Channel A:"), c);
             c = menuItem.updateGBC(c, 1, 0, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             addStationFATDMAPanel.add(addStationChannelAComboBox, c);
             c = menuItem.updateGBC(c, 2, 0, 0.5, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             addStationFATDMAPanel.add(chooseDefaultFATDMASchemeButton, c);
 			
 			if (!((String) addStationChannelAComboBox.getSelectedItem()).equals("NULL")) {
 
 				JPanel channelATitlePanel = getChannelTitlePanelForBaseStationOrRepeater();
 				channelAPanel = getChannelPanelForBaseStationOrRepeater(initialChannelAComponents, channelAComponents, initChannelA, addRowToChannelA);
 				
 				addStationChannelAScrollPane = new JScrollPane(channelAPanel);
 				addStationChannelAScrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));
 				addStationChannelAScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 				addStationChannelAScrollPane.setPreferredSize(new Dimension(630, 100));
 				addStationChannelAScrollPane.setMaximumSize(new Dimension(630, 100));
 				addStationChannelAScrollPane.setMinimumSize(new Dimension(630, 100));	
 				
 				JPanel containerPanel = new JPanel(new GridBagLayout());
 				containerPanel.setBorder(BorderFactory.createLineBorder(new Color(184, 207, 229)));
 				c = menuItem.updateGBC(c, 0, 0, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));  
 				containerPanel.add(channelATitlePanel, c);
 				c = menuItem.updateGBC(c, 0, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));  
 				containerPanel.add(addStationChannelAScrollPane, c);				
 				
 				c = menuItem.updateGBC(c, 0, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));   				
 				c.gridwidth = 3;
 				addStationFATDMAPanel.add(containerPanel, c);
 				c.gridwidth = 1;
 			}
 
 			c = menuItem.updateGBC(c, 0, 2, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
             addStationFATDMAPanel.add(new JLabel("Channel B:"), c);
             c = menuItem.updateGBC(c, 1, 2, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             addStationFATDMAPanel.add(addStationChannelBComboBox, c);
             c = menuItem.updateGBC(c, 2, 2, 0.5, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             addStationFATDMAPanel.add(new JLabel(""), c);		
 			
 			if (!((String) addStationChannelBComboBox.getSelectedItem()).equals("NULL")) {
 
 				JPanel channelBTitlePanel = getChannelTitlePanelForBaseStationOrRepeater();
 				channelBPanel = getChannelPanelForBaseStationOrRepeater(initialChannelBComponents, channelBComponents, initChannelB, addRowToChannelB);
 				
 				addStationChannelBScrollPane = new JScrollPane(channelBPanel);
 				addStationChannelBScrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));
 				addStationChannelBScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 				addStationChannelBScrollPane.setPreferredSize(new Dimension(630, 100));
 				addStationChannelBScrollPane.setMaximumSize(new Dimension(630, 100));
 				addStationChannelBScrollPane.setMinimumSize(new Dimension(630, 100));	
 				
 				JPanel containerPanel = new JPanel(new GridBagLayout());
 				containerPanel.setBorder(BorderFactory.createLineBorder(new Color(184, 207, 229)));
 				c = menuItem.updateGBC(c, 0, 0, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));  
 				containerPanel.add(channelBTitlePanel, c);
 				c = menuItem.updateGBC(c, 0, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));  
 				containerPanel.add(addStationChannelBScrollPane, c);				
 				
 				c = menuItem.updateGBC(c, 0, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));   				
 				c.gridwidth = 3;
 				addStationFATDMAPanel.add(containerPanel, c);
 				c.gridwidth = 1;
 			}
 			
 		} else if (addStationTypeComboBox.getSelectedIndex() == 3) {  // aton station
 
             c = menuItem.updateGBC(c, 0, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
             addStationFATDMAPanel.add(new JLabel("Channel A:"), c);
             c = menuItem.updateGBC(c, 1, 0, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             addStationFATDMAPanel.add(addStationChannelAComboBox, c);
             c = menuItem.updateGBC(c, 2, 0, 0.5, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             addStationFATDMAPanel.add(chooseDefaultFATDMASchemeButton, c);
 			
 			if (!((String) addStationChannelAComboBox.getSelectedItem()).equals("NULL")) {				
 			
 				JPanel channelATitlePanel = getChannelTitlePanelForAtonStation();
 				channelAPanel = getChannelPanelForAtonStation(initialChannelAComponents, channelAComponents, initChannelA, addRowToChannelA);
 				
 				addStationChannelAScrollPane = new JScrollPane(channelAPanel);
 				addStationChannelAScrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));
 				addStationChannelAScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 				addStationChannelAScrollPane.setPreferredSize(new Dimension(930, 100));
 				addStationChannelAScrollPane.setMaximumSize(new Dimension(930, 100));
 				addStationChannelAScrollPane.setMinimumSize(new Dimension(930, 100));	
 				
 				JPanel containerPanel = new JPanel(new GridBagLayout());
 				containerPanel.setBorder(BorderFactory.createLineBorder(new Color(184, 207, 229)));
 				c = menuItem.updateGBC(c, 0, 0, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));  
 				containerPanel.add(channelATitlePanel, c);
 				c = menuItem.updateGBC(c, 0, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));  
 				containerPanel.add(addStationChannelAScrollPane, c);				
 				
 				c = menuItem.updateGBC(c, 0, 1, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));   				
 				c.gridwidth = 3;
 				addStationFATDMAPanel.add(containerPanel, c);
 				c.gridwidth = 1;
 			}
 
 			c = menuItem.updateGBC(c, 0, 2, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));
             addStationFATDMAPanel.add(new JLabel("Channel B:"), c);
             c = menuItem.updateGBC(c, 1, 2, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             addStationFATDMAPanel.add(addStationChannelBComboBox, c);			
             c = menuItem.updateGBC(c, 2, 2, 0.5, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(5,5,5,5));                    
             addStationFATDMAPanel.add(new JLabel(""), c);	
 			
 			if (!((String) addStationChannelBComboBox.getSelectedItem()).equals("NULL")) {
 		 
 				JPanel channelBTitlePanel = getChannelTitlePanelForAtonStation();
 				channelBPanel = getChannelPanelForAtonStation(initialChannelBComponents, channelBComponents, initChannelB, addRowToChannelB);			
 							
 				addStationChannelBScrollPane = new JScrollPane(channelBPanel);
 				addStationChannelBScrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));				
 				addStationChannelBScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 				addStationChannelBScrollPane.setPreferredSize(new Dimension(930, 100));
 				addStationChannelBScrollPane.setMaximumSize(new Dimension(930, 100));
 				addStationChannelBScrollPane.setMinimumSize(new Dimension(930, 100));	
 
 				JPanel containerPanel = new JPanel(new GridBagLayout());
 				containerPanel.setBorder(BorderFactory.createLineBorder(new Color(184, 207, 229)));
 				c = menuItem.updateGBC(c, 0, 0, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));  
 				containerPanel.add(channelBTitlePanel, c);
 				c = menuItem.updateGBC(c, 0, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0));  
 				containerPanel.add(addStationChannelBScrollPane, c);				
 				
 				c = menuItem.updateGBC(c, 0, 3, 0.5, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5,5,5,5));   				
 				c.gridwidth = 3;
 				addStationFATDMAPanel.add(containerPanel, c);
 				c.gridwidth = 1;
 			}
 		
 		}
 		
 		this.pack();		
 		addStationFATDMAPanel.repaint();
 		addStationFATDMAPanel.validate();
 		
 		// scrolls to bottom
 		if (addRowToChannelA) {
 			addStationChannelAScrollPane.getViewport().setViewPosition(new Point(0, 10000));
 		}
 		if (addRowToChannelB) {
 			addStationChannelBScrollPane.getViewport().setViewPosition(new Point(0, 10000));
 		}
 		
 		menuItem.ignoreListeners = false;
 	}	
 	
 	private JPanel getChannelTitlePanelForBaseStationOrRepeater() {
 		JPanel channelTitlePanel = new JPanel(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();			
 		c = menuItem.updateGBC(c, 0, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,33,5,5));			
 		JLabel startSlotLabel = new JLabel("<html><u>Startslot</u></html>");
 		startSlotLabel.setToolTipText("FATDMA_startslot (0..2249)");
 		channelTitlePanel.add(startSlotLabel, c);			
 		c = menuItem.updateGBC(c, 1, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,50,5,5));			
 		JLabel blockSizeLabel = new JLabel("<html><u>Block size</u></html>");
 		blockSizeLabel.setToolTipText("FATDMA_block_size (1..5)");				
 		channelTitlePanel.add(blockSizeLabel, c);	
 		c = menuItem.updateGBC(c, 2, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,45,5,5));			
 		JLabel incrementLabel = new JLabel("<html><u>Increment</u></html>");
 		incrementLabel.setToolTipText("FATDMA_increment (0..1125, recommended values 0,2,3,5,6,9,10,15,18,25,30,45,50,75,90,125,225,250,375,450,750,1125)");		
 		channelTitlePanel.add(incrementLabel, c);	
 		c = menuItem.updateGBC(c, 3, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,37,5,5));			
 		JLabel ownershipLabel = new JLabel("<html><u>Ownership</u></html>");
 		ownershipLabel.setToolTipText("FATDMA_ownership (L: use by local station, R: use by remote station)");
 		channelTitlePanel.add(ownershipLabel, c);
 		c = menuItem.updateGBC(c, 4, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,48,5,5));				
 		JLabel usageLabel = new JLabel("<html><u>Usage</u></html>");
 		channelTitlePanel.add(usageLabel, c);
 		c = menuItem.updateGBC(c, 5, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 		channelTitlePanel.add(new JLabel(""), c);	
 		return channelTitlePanel;
 	}	
 	
 	private JPanel getChannelPanelForBaseStationOrRepeater(List<Component> initialComponents, Component[] previousChannelComponents, boolean init, boolean addRowToChannel) {
 	
 		JPanel channelPanel = new JPanel(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 				
 		if (initialComponents != null) {
 			for (int i=0; i<initialComponents.size(); i++) {
 				Component component = initialComponents.get(i);
 				if (component instanceof JButton) {
 					c = menuItem.updateGBC(c, i%6, (channelPanel.getComponents().length)/6, 0.5, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5));
 				} else {
 					c = menuItem.updateGBC(c, i%6, (channelPanel.getComponents().length)/6, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));						
 				}
 				if (component instanceof JTextField) {
 					((JTextField) component).setCaretPosition(0);
 				}				
 				channelPanel.add(component, c);
 			}
 		}				
 				
 		for (int i=0; i<previousChannelComponents.length-1; i++) {
 			if (previousChannelComponents[i] instanceof JButton) {
 				c = menuItem.updateGBC(c, i%6, (channelPanel.getComponents().length)/6, 0.5, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			} else {
 				c = menuItem.updateGBC(c, i%6, (channelPanel.getComponents().length)/6, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));						
 			}
 			if (previousChannelComponents[i] instanceof JTextField) {
 				((JTextField) previousChannelComponents[i]).setCaretPosition(0);
 			}			
 			channelPanel.add(previousChannelComponents[i], c);
 		}
 				
 		int addNoOfRows = 0;
 		if (init) {
 			addNoOfRows = 3-(channelPanel.getComponents().length/6);
 			if (addNoOfRows < 0) {
 				addNoOfRows = 0;
 			}
 		}
 		if (addRowToChannel) {
 			addNoOfRows++;
 		}
 		
 		for (int i=0; i<addNoOfRows; i++) {
 			for (int cols=0; cols<6; cols++) {						
 				JComponent component = null;						
 				if (cols < 3) {
 					component = new JTextField(8);				
 					c = menuItem.updateGBC(c, cols, (channelPanel.getComponents().length)/6, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));	
 				} else if (cols == 3) {
 					component = new JComboBox();									
 					((JComboBox) component).addItem("Local");
 					((JComboBox) component).addItem("Remote");
 					((JComboBox) component).setBorder(new EmptyBorder(0, 3, 0, 3));
 					c = menuItem.updateGBC(c, cols, (channelPanel.getComponents().length)/6, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));					
 				} else if (cols == 4) {
 					component = new JTextField(8);				
 					c = menuItem.updateGBC(c, cols, (channelPanel.getComponents().length)/6, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));					
 				} else if (cols == 5) {
 					component = new JButton("Clear");	
 					((JButton) component).setMargin(new Insets(0, 3, 0, 3));
 					((JButton) component).addActionListener(this);
 					c = menuItem.updateGBC(c, cols, (channelPanel.getComponents().length)/6, 0.5, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5));
 				}		
 				channelPanel.add(component, c);						
 			}
 		}
 				
 		JButton button = new JButton("Add row");
 		button.addActionListener(this);				
 		button.setMargin(new Insets(0, 3, 0, 3));
 		c = menuItem.updateGBC(c, 5, (channelPanel.getComponents().length)/6, 0.5, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 		channelPanel.add(button, c);	
 		
 		return channelPanel;
 	}
 	
 	private JPanel getChannelTitlePanelForAtonStation() {
 		JPanel channelTitlePanel = new JPanel(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();		
 		c = menuItem.updateGBC(c, 0, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,4,1,5));			
 		JLabel accessSchemeLabel = new JLabel("<html><u>Access Scheme</u></html>");
 		accessSchemeLabel.setToolTipText("Access_scheme (FATDMA, RATDMA, CSTDMA)");
 		channelTitlePanel.add(accessSchemeLabel, c);
 		c = menuItem.updateGBC(c, 1, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,18,1,5));			
 		JLabel messageIDLabel = new JLabel("<html><u>Message ID</u></html>");
 		messageIDLabel.setToolTipText("Message_ID (0..64) (Identifies which message type this transmission relates to)");
 		channelTitlePanel.add(messageIDLabel, c);
 		c = menuItem.updateGBC(c, 2, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,40,1,5));			
 		JLabel utcHourLabel = new JLabel("<html><u>UTC Hour</u></html>");
 		utcHourLabel.setToolTipText("UTC_Hour (0-23; 24 = UTC hour not available) (UTC hour of first transmission of the day)");
 		channelTitlePanel.add(utcHourLabel, c);	
 		c = menuItem.updateGBC(c, 3, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,42,1,5));	
 		JLabel utcMinuteLabel = new JLabel("<html><u>UTC Minute</u></html>");
 		utcMinuteLabel.setToolTipText("UTC_Minute (0-59; 60 = UTC minute not available) (UTC minute of first transmission of the day)");
 		channelTitlePanel.add(utcMinuteLabel, c);		
 		c = menuItem.updateGBC(c, 4, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,42,1,5));			
 		JLabel startslotLabel = new JLabel("<html><u>Startslot</u></html>");
 		startslotLabel.setToolTipText("startslot (0-2249; 4095 = discontinue broadcast) (Only relevant for FATDMA)");
 		channelTitlePanel.add(startslotLabel, c);
 		c = menuItem.updateGBC(c, 5, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,47,1,5));			
 		JLabel blockSizeLabel = new JLabel("<html><u>Block size</u></html>");
 		blockSizeLabel.setToolTipText("block_size (1..5)");
 		channelTitlePanel.add(blockSizeLabel, c);							
 		c = menuItem.updateGBC(c, 6, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,43,1,5));			
 		JLabel incrementLabel = new JLabel("<html><u>Increment</u></html>");
 		incrementLabel.setToolTipText("increment (0..324000), (No. of slots in FATDMA, no. of seconds in RATDMA/CSTDMA)");
 		channelTitlePanel.add(incrementLabel, c);	
 		c = menuItem.updateGBC(c, 7, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,51,1,5));			
 		JLabel usageLabel = new JLabel("<html><u>Usage</u></html>");
 		channelTitlePanel.add(usageLabel, c);			
 		c = menuItem.updateGBC(c, 8, 0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,1,5));			
 		channelTitlePanel.add(new JLabel(""), c);	
 		return channelTitlePanel;
 	}
 	
 	private JPanel getChannelPanelForAtonStation(List<Component> initialComponents, Component[] previousChannelComponents, boolean init, boolean addRowToChannel) {
 	
 		JPanel channelPanel = new JPanel(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 
 		if (initialComponents != null) {
 			for (int i=0; i<initialComponents.size(); i++) {
 				Component component = initialComponents.get(i);		
 				if (component instanceof JButton) {
 					c = menuItem.updateGBC(c, i%9, (channelPanel.getComponents().length)/9, 0.5, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5));
 				} else {
 					c = menuItem.updateGBC(c, i%9, (channelPanel.getComponents().length)/9, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));						
 				}
 				if (component instanceof JTextField) {
 					((JTextField) component).setCaretPosition(0);
 				}
 				channelPanel.add(component, c);
 			}
 		}	
 		
 		for (int i=0; i<previousChannelComponents.length-1; i++) {
 			if (previousChannelComponents[i] instanceof JButton) {
 				c = menuItem.updateGBC(c, i%9, (channelPanel.getComponents().length)/9, 0.5, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5));						
 			} else {
 				c = menuItem.updateGBC(c, i%9, (channelPanel.getComponents().length)/9, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));
 			}
 			if (previousChannelComponents[i] instanceof JTextField) {
 				((JTextField) previousChannelComponents[i]).setCaretPosition(0);
 			}			
 			channelPanel.add(previousChannelComponents[i], c);
 		}
 		
 		int addNoOfRows = 0;
 		if (init) {		
 			addNoOfRows = 3-(channelPanel.getComponents().length/9);
 			if (addNoOfRows < 0) {
 				addNoOfRows = 0;
 			}
 		}
 		if (addRowToChannel) {
 			addNoOfRows++;
 		}
 		
 		for (int i=0; i<addNoOfRows; i++) {
 			for (int cols=0; cols<9; cols++) {						
 				JComponent component = null;
 				if  (cols == 0) {
 					component = new JComboBox();									
 					((JComboBox) component).addItem("FATDMA");
 					((JComboBox) component).addItem("RATDMA");
 					((JComboBox) component).addItem("CSTDMA");
 					((JComboBox) component).setBorder(new EmptyBorder(0, 3, 0, 3));
 					c = menuItem.updateGBC(c, cols, (channelPanel.getComponents().length)/9, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));		
 				} else if (cols > 0 && cols < 8) {
 					component = new JTextField(8);				
 					c = menuItem.updateGBC(c, cols, (channelPanel.getComponents().length)/9, 0.5, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5));
 				} else if (cols == 8) {
 					component = new JButton("Clear");	
 					((JButton) component).setMargin(new Insets(0, 3, 0, 3));
 					((JButton) component).addActionListener(this);
 					c = menuItem.updateGBC(c, cols, (channelPanel.getComponents().length)/9, 0.5, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5));
 				}		
 				channelPanel.add(component, c);						
 			}
 		}				
 				
 		JButton button = new JButton("Add row");
 		button.addActionListener(this);				
 		button.setMargin(new Insets(0, 3, 0, 3));
 		c = menuItem.updateGBC(c, 8, (channelPanel.getComponents().length)/9, 0.5, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5));			
 		channelPanel.add(button, c);		
 
 		return channelPanel;
 	}
 	
 	private void clearRow(String channel, int clearRowButtonIndex) {
 			
 		menuItem.ignoreListeners = true;			
 			
 		if (addStationTypeComboBox.getSelectedIndex() == 0 || addStationTypeComboBox.getSelectedIndex() == 1) {  // base station or repeater
 			for (int i=clearRowButtonIndex; i>clearRowButtonIndex-6; i--) {
 				if (channel.equals("A")) {
 					Component component = channelAPanel.getComponent(i);
 					if (component instanceof JTextField) {
 						((JTextField) component).setText("");
 					} else if (component instanceof JComboBox) {
 						((JComboBox) component).setSelectedIndex(0);
 					}
 				} else if (channel.equals("B")) {
 					Component component = channelBPanel.getComponent(i);
 					if (component instanceof JTextField) {
 						((JTextField) component).setText("");
 					} else if (component instanceof JComboBox) {
 						((JComboBox) component).setSelectedIndex(0);
 					}
 				}
 			}
 		} else if (addStationTypeComboBox.getSelectedIndex() == 3) {  // aton station
 			for (int i=clearRowButtonIndex; i>clearRowButtonIndex-9; i--) {
 				if (channel.equals("A")) {
 					Component component = channelAPanel.getComponent(i);
 					if (component instanceof JTextField) {
 						((JTextField) component).setText("");
 					} else if (component instanceof JComboBox) {
 						((JComboBox) component).setSelectedIndex(0);
 					}
 				} else if (channel.equals("B")) {
 					Component component = channelBPanel.getComponent(i);
 					if (component instanceof JTextField) {
 						((JTextField) component).setText("");
 					} else if (component instanceof JComboBox) {
 						((JComboBox) component).setSelectedIndex(0);
 					}
 				}
 			}
 		}
 		updateAddStationChannelComboBoxesAndScrollPanes(null, null, false, false, false, false, false, false);
 		
 		menuItem.ignoreListeners = false;
 	}			
 	
 	/*
 	private void deleteRow(String channel, int deleteRowButtonIndex) {
 			
 		menuItem.ignoreListeners = true;			
 			
 		if (addStationTypeComboBox.getSelectedIndex() == 0 || addStationTypeComboBox.getSelectedIndex() == 1) {  // base station or repeater
 			for (int i=deleteRowButtonIndex; i>deleteRowButtonIndex-5; i--) {
 				if (channel.equals("A")) {
 					channelAPanel.remove(i);
 				} else if (channel.equals("B")) {
 					channelBPanel.remove(i);
 				}
 			}
 		} else if (addStationTypeComboBox.getSelectedIndex() == 3) {  // aton station
 			for (int i=deleteRowButtonIndex; i>deleteRowButtonIndex-8; i--) {
 				if (channel.equals("A")) {
 					channelAPanel.remove(i);
 				} else if (channel.equals("B")) {
 					channelBPanel.remove(i);
 				}
 			}
 		}
 		updateAddStationChannelComboBoxesAndScrollPanes(false, false, false, false);
 		
 		menuItem.ignoreListeners = false;
 	}			
 	*/
 		
 	private List<FATDMAReservation> getFATDMAScheme(Component[] components) throws NumberFormatException, IllegalArgumentException {
 		List<FATDMAReservation> fatdmaScheme = new ArrayList<FATDMAReservation>();
 		int i = 0;
 		while (i+5 < components.length-1) {
 			JTextField startslotTextField = (JTextField) components[i];
 			JTextField blockSizeTextField = (JTextField) components[i+1];
 			JTextField incrementTextField = (JTextField) components[i+2];
 			JComboBox ownershipComboBox = (JComboBox) components[i+3];
 			String ownership = null;
 			if (ownershipComboBox.getSelectedIndex() == 0) {
 				ownership = "L";
 			} else if (ownershipComboBox.getSelectedIndex() == 1) {
 				ownership = "R";
 			}
 			JTextField usageTextField = (JTextField) components[i+4];
 			if ((startslotTextField.getText().isEmpty() || blockSizeTextField.getText().isEmpty() || incrementTextField.getText().isEmpty() || usageTextField.getText().isEmpty()) &&
 					!(startslotTextField.getText().isEmpty() && blockSizeTextField.getText().isEmpty() && incrementTextField.getText().isEmpty() && usageTextField.getText().isEmpty())) {
 				throw new IllegalArgumentException("Rows exist that do not have all parameters defined.");
 			}
 			if (!startslotTextField.getText().isEmpty() && !blockSizeTextField.getText().isEmpty() && !incrementTextField.getText().isEmpty() && !usageTextField.getText().isEmpty()) {
 				FATDMAReservation fatdmaReservation = new FATDMAReservation(new Integer(startslotTextField.getText()),
 					new Integer(blockSizeTextField.getText()), new Integer(incrementTextField.getText()), ownership, usageTextField.getText());
 				fatdmaScheme.add(fatdmaReservation);
 			}						
 			// i+5 is clear button
 			i = i+6;  // go to next row
 		}
 		return fatdmaScheme;
 	}
 
 	private List<AtonMessageBroadcastRate> getAtonMessageBroadcastList(Component[] components) throws NumberFormatException, IllegalArgumentException {
 		List<AtonMessageBroadcastRate> atonMessageBroadcastList = new ArrayList<AtonMessageBroadcastRate>();
 		int i = 0;
 		while (i+8 < components.length-1) {
 			JComboBox accessSchemeComboBox = (JComboBox) components[i];
 			JTextField messageIDTextField = (JTextField) components[i+1];
 			JTextField utcHourTextField = (JTextField) components[i+2];
 			JTextField utcMinuteTextField = (JTextField) components[i+3];
 			JTextField startslotTextField = (JTextField) components[i+4];
 			JTextField blockSizeTextField = (JTextField) components[i+5];
 			JTextField incrementTextField = (JTextField) components[i+6];			
 			JTextField usageTextField = (JTextField) components[i+7];
 			if ((messageIDTextField.getText().isEmpty() || utcHourTextField.getText().isEmpty() || utcMinuteTextField.getText().isEmpty() ||
 					startslotTextField.getText().isEmpty() || blockSizeTextField.getText().isEmpty() || incrementTextField.getText().isEmpty() ||
 					usageTextField.getText().isEmpty()) &&
 					!(messageIDTextField.getText().isEmpty() && utcHourTextField.getText().isEmpty() && utcMinuteTextField.getText().isEmpty() &&
 					startslotTextField.getText().isEmpty() && blockSizeTextField.getText().isEmpty() && incrementTextField.getText().isEmpty() &&
 					usageTextField.getText().isEmpty())) {
 				throw new IllegalArgumentException("Rows exist that do not have all parameters defined.");				
 			}				
 			if (!messageIDTextField.getText().isEmpty() && !utcHourTextField.getText().isEmpty() && !utcMinuteTextField.getText().isEmpty() &&
 					!startslotTextField.getText().isEmpty() && !blockSizeTextField.getText().isEmpty() && !incrementTextField.getText().isEmpty() &&
 					!usageTextField.getText().isEmpty()) {			
 				AtonMessageBroadcastRate atonMessageBroadcastRate = new AtonMessageBroadcastRate((String) accessSchemeComboBox.getSelectedItem(),				
 					new Integer(messageIDTextField.getText()), new Integer(utcHourTextField.getText()), new Integer(utcMinuteTextField.getText()),
 					new Integer(startslotTextField.getText()), new Integer(blockSizeTextField.getText()), new Integer(incrementTextField.getText()),
 					usageTextField.getText());					
 				atonMessageBroadcastList.add(atonMessageBroadcastRate);
 			}						
 			// i+8 is clear button
 			i = i+9;  // go to next row
 		}
 		return atonMessageBroadcastList;
 	}
 	
     private boolean addStation() {
         
         if (menuItem.getData() == null) {
             menuItem.setData(new EAVDAMData());
         }
         
         if (menuItem.alreadyExists(-1, addStationNameTextField.getText())) {
             JOptionPane.showMessageDialog(this, "A station with the given name already exists. " +
                 "Please, select another name for the station.");                 
             return false;
         }
 
         if (addLatitudeTextField.getText().trim().isEmpty()) {
             JOptionPane.showMessageDialog(this, "Latitude is mandatory.");
             return false;
         } else {
             if (!addLatitudeTextField.getText().trim().isEmpty()) {
                 try {
                     
                     Double.parseDouble(addLatitudeTextField.getText().replace(",", ".").trim());                    
                 } catch (NumberFormatException ex) {                
                     JOptionPane.showMessageDialog(this, "Latitude is not a valid number.");
                     return false;
                 }
             }
         }            
         if (addLongitudeTextField.getText().trim().isEmpty()) {            
             JOptionPane.showMessageDialog(this, "Longitude is mandatory.");                    
             return false;
         } else {                                                        
             if (!addLongitudeTextField.getText().trim().isEmpty()) {
                 try {
                     Double.parseDouble(addLongitudeTextField.getText().replace(",", ".").trim());
                 } catch (NumberFormatException ex) {                
                     JOptionPane.showMessageDialog(this, "Longitude is not a valid number.");
                     return false;
                 }
             }
         }                    
         try {
             if (!addTransmissionPowerTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(addTransmissionPowerTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(this, "Transmission power is not a valid number.");
             return false;
         }   
         if (addAntennaTypeComboBox.getSelectedIndex() == 1 &&
                 (addAntennaHeightTextField.getText().trim().isEmpty() ||
                 addTerrainHeightTextField.getText().trim().isEmpty())) {
             JOptionPane.showMessageDialog(this, "Antenna height and terrain height must both be given.");
             return false;
         }
         if (addAntennaTypeComboBox.getSelectedIndex() == 2 &&
                 (addAntennaHeightTextField.getText().trim().isEmpty() ||
                 addTerrainHeightTextField.getText().trim().isEmpty() ||
                 addHeadingTextField.getText().trim().isEmpty() ||
                 addFieldOfViewAngleTextField.getText().trim().isEmpty() ||
                 addGainTextField.getText().trim().isEmpty())) {
             JOptionPane.showMessageDialog(this, "Antenna height, terrain height, heading, field of view angle and gain must all be given.");
             return false;
         }
         try {
             if (!addAntennaHeightTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(addAntennaHeightTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(this, "Antenna height is not a valid number.");                     
             return false;
         }  
         try {
             if (!addTerrainHeightTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(addTerrainHeightTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {        
             JOptionPane.showMessageDialog(this, "Terrain height is not a valid number.");
             return false;
         }                                 
         try {
             if (!addHeadingTextField.getText().trim().isEmpty()) {
                 Integer.parseInt(addHeadingTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {        
             JOptionPane.showMessageDialog(this, "Heading is not a valid integer.");
             return false;
         }  
         try {
             if (!addFieldOfViewAngleTextField.getText().trim().isEmpty()) {
                 Integer.parseInt(addFieldOfViewAngleTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(this, "Field of view angle is not a valid integer.");                        
             return false;
         }      
         try {
             if (!addGainTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(addGainTextField.getText().replace(",", ".").trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(this, "Gain is not a valid number.");
             return false;
         }
 		
 		FATDMAChannel fatdmaChannelA = null;
 		FATDMAChannel fatdmaChannelB = null;		
 		
 		if (addStationTypeComboBox.getSelectedIndex() == 0 || addStationTypeComboBox.getSelectedIndex() == 1) {  // base station or repeater
 		
 			if (!((String) addStationChannelAComboBox.getSelectedItem()).equals("NULL")) { 
 				Component[] channelAComponents = channelAPanel.getComponents();				
 				if (channelAComponents.length > 7) {				
 					try {
 						List<FATDMAReservation> fatdmaScheme = getFATDMAScheme(channelAComponents);
 						if (!fatdmaScheme.isEmpty()) {
 							fatdmaChannelA = new AISBaseAndReceiverStationFATDMAChannel((String) addStationChannelAComboBox.getSelectedItem(), fatdmaScheme);
 						}
 					} catch (NumberFormatException e) {
 						JOptionPane.showMessageDialog(this, "All FATDMA values of channel A are not valid integers.");
 						return false;
 					} catch (IllegalArgumentException e) {
 						JOptionPane.showMessageDialog(this, "The following error occurred when validating FATDMA values of channel A:\n" + e.getMessage());              
 						return false;							
 					}						
 				}
 				Component[] channelBComponents = channelBPanel.getComponents();				
 				if (channelBComponents.length > 7) {				
 					try {
 						List<FATDMAReservation> fatdmaScheme = getFATDMAScheme(channelBComponents);
 						if (!fatdmaScheme.isEmpty()) {
 							fatdmaChannelB = new AISBaseAndReceiverStationFATDMAChannel((String) addStationChannelBComboBox.getSelectedItem(), fatdmaScheme);
 						}			
 					} catch (NumberFormatException e) {
 						JOptionPane.showMessageDialog(this, "All FATDMA values of channel B are not valid integers.");
 						return false;
 					} catch (IllegalArgumentException e) {
 						JOptionPane.showMessageDialog(this, "The following error occurred when validating FATDMA values of channel B:\n" + e.getMessage());              
 						return false;							
 					}
 				}				
 			}
 		
 		} else if (addStationTypeComboBox.getSelectedIndex() == 3) {  // aton station
 			Component[] channelAComponents = channelAPanel.getComponents();				
 			if (channelAComponents.length > 10) {				
 				try {
 					List<AtonMessageBroadcastRate> atonMessageBroadcastList = getAtonMessageBroadcastList(channelAComponents);
 					if (!atonMessageBroadcastList.isEmpty()) {
 						fatdmaChannelA = new AISAtonStationFATDMAChannel((String) addStationChannelAComboBox.getSelectedItem(), atonMessageBroadcastList);					
 					}
 				} catch (NumberFormatException e) {
 					JOptionPane.showMessageDialog(this, "All FATDMA values of channel A are not valid integers.");
 					return false;
 				} catch (IllegalArgumentException e) {
 					JOptionPane.showMessageDialog(this, "The following error occurred when validating FATDMA values of channel A:\n" + e.getMessage());              
 					return false;							
 				}					
 			}
 			Component[] channelBComponents = channelBPanel.getComponents();				
 			if (channelBComponents.length > 10) {				
 				try {
 					List<AtonMessageBroadcastRate> atonMessageBroadcastList = getAtonMessageBroadcastList(channelBComponents);
 					if (!atonMessageBroadcastList.isEmpty()) {
 						fatdmaChannelB = new AISAtonStationFATDMAChannel((String) addStationChannelBComboBox.getSelectedItem(), atonMessageBroadcastList);
 					}
 				} catch (NumberFormatException e) {
 					JOptionPane.showMessageDialog(this, "All FATDMA values of channel B are not valid integers.");
 					return false;
 				} catch (IllegalArgumentException e) {
 					JOptionPane.showMessageDialog(this, "The following error occurred when validating FATDMA values of channel B:\n" + e.getMessage());              
 					return false;							
 				}												
 			}				
 		}
 		
 		int nonRecommendedValueForIncrementForChannelA = 0;
 		int nonRecommendedValueForIncrementForChannelB = 0;
 		if (fatdmaChannelA instanceof AISBaseAndReceiverStationFATDMAChannel) {
 			List<FATDMAReservation> fatdmaScheme = ((AISBaseAndReceiverStationFATDMAChannel) fatdmaChannelA).getFATDMAScheme();
 			for (FATDMAReservation fatdmaReservation : fatdmaScheme) {
 				int i = fatdmaReservation.getIncrement().intValue();
 				if (i != 0 && i != 2 && i != 3 && i != 5 && i != 6 && i != 9 && i != 10 && i != 15 && i != 18 && i != 25 && i != 30 && i != 45
 						&& i != 50 && i != 75 && i != 90 && i != 125 && i != 225 && i != 250 && i != 375 && i != 450 && i != 750 && i != 1125) {
 					nonRecommendedValueForIncrementForChannelA = i;
 					break;
 				}
 			}
 		}
 		if (fatdmaChannelB instanceof AISBaseAndReceiverStationFATDMAChannel) {
 			List<FATDMAReservation> fatdmaScheme = ((AISBaseAndReceiverStationFATDMAChannel) fatdmaChannelB).getFATDMAScheme();
 			for (FATDMAReservation fatdmaReservation : fatdmaScheme) {
 				int i = fatdmaReservation.getIncrement().intValue();
 				if (i != 0 && i != 2 && i != 3 && i != 5 && i != 6 && i != 9 && i != 10 && i != 15 && i != 18 && i != 25 && i != 30 && i != 45
 						&& i != 50 && i != 75 && i != 90 && i != 125 && i != 225 && i != 250 && i != 375 && i != 450 && i != 750 && i != 1125) {
 					nonRecommendedValueForIncrementForChannelB = i;
 					break;
 				}
 			}
 		}		
 		
 		boolean doContinue = true;
 		if (nonRecommendedValueForIncrementForChannelA != 0 || nonRecommendedValueForIncrementForChannelB != 0) {
 			String msg = "FATDMA reservations for channel ";
 			if (nonRecommendedValueForIncrementForChannelA != 0) {
 				msg += "A contains a reservation with increment " + nonRecommendedValueForIncrementForChannelA;
 			}
 			if (nonRecommendedValueForIncrementForChannelA != 0 && nonRecommendedValueForIncrementForChannelB != 0) {
 				msg += " and\nFATDMA reservations for channel ";
 			}
 			if (nonRecommendedValueForIncrementForChannelB != 0) {
 				msg += "B contains a reservation with increment " + nonRecommendedValueForIncrementForChannelB;
 			}
 			msg += ".\n";
 			msg += "According to ITU and IALA recommendations, the following values are recommended\n" +
 				"to ensure symmetric reservations accross the frame: 0, 2, 3, 5, 6, 9, 10, 15, 18,\n" +
 				"25, 30, 45, 50, 75, 90, 125, 225, 250, 375, 450, 750, 1125. Are you sure you wish to\n" +
 				"save this reservation?";				
 			int response = JOptionPane.showConfirmDialog(this, msg, "Confirm action", JOptionPane.YES_NO_OPTION);
 			if (response == JOptionPane.NO_OPTION) {
 				doContinue = false;
 			}
 		}
 		
 		if (doContinue) {
 			AISFixedStationData stationData = new AISFixedStationData();
 
 			try {                 
 				stationData.setStationName(addStationNameTextField.getText().trim());
 				if (addStationTypeComboBox.getSelectedIndex() == 0) {
 					stationData.setStationType(AISFixedStationType.BASESTATION);
 				} else if (addStationTypeComboBox.getSelectedIndex() == 1) {
 					stationData.setStationType(AISFixedStationType.REPEATER); 
 				} else if (addStationTypeComboBox.getSelectedIndex() == 2) {
 					stationData.setStationType(AISFixedStationType.RECEIVER); 
 				} else if (addStationTypeComboBox.getSelectedIndex() == 3) {
 					stationData.setStationType(AISFixedStationType.ATON); 
 				}  
 				stationData.setLat(new Double(addLatitudeTextField.getText().replace(",", ".").trim()).doubleValue());                                
 				stationData.setLon(new Double(addLongitudeTextField.getText().replace(",", ".").trim()).doubleValue());  
 				if (addMMSINumberTextField.getText().trim().isEmpty()) {
 					stationData.setMmsi(null);
 				} else {
 					stationData.setMmsi(addMMSINumberTextField.getText().trim());
 				}
 				if (addTransmissionPowerTextField.getText().trim().isEmpty()) {
 					stationData.setTransmissionPower(null);
 				} else {
 					stationData.setTransmissionPower(new Double(addTransmissionPowerTextField.getText().replace(",", ".").trim()));               
 				}
 				Antenna antenna = stationData.getAntenna();
 				if (addAntennaTypeComboBox.getSelectedIndex() == 0) {
 					stationData.setAntenna(null);
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
 					stationData.setAntenna(antenna);
 				}
 				stationData.setFATDMAChannelA(fatdmaChannelA);				
 				stationData.setFATDMAChannelB(fatdmaChannelB);
 				if (addAdditionalInformationJTextArea.getText().trim().isEmpty()) {
 					stationData.setDescription(null);
 				} else {
 					stationData.setDescription(addAdditionalInformationJTextArea.getText().trim());
 				}
 			} catch (IllegalArgumentException e) {
 				JOptionPane.showMessageDialog(this, e.getMessage());              
 				return false;
 			}            
 
 			if (((String) menuItem.getSelectDatasetComboBox().getSelectedItem()).startsWith(StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL)) {            
 
 				if (addStationStatusComboBox.getSelectedIndex() == 0) {  // operative
 					AISFixedStationStatus status = new AISFixedStationStatus();
 					status.setStatusID(DerbyDBInterface.STATUS_ACTIVE);
 					stationData.setStatus(status);
 				} else if (addStationStatusComboBox.getSelectedIndex() == 1) {  // planned
 					AISFixedStationStatus status = new AISFixedStationStatus();
 					status.setStatusID(DerbyDBInterface.STATUS_PLANNED);				
 					stationData.setStatus(status);
 				}
 			
 				ActiveStation activeStation = new ActiveStation();
 				List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();
 				stations.add(stationData);            
 				activeStation.setStations(stations);
 							
 				List<ActiveStation> activeStations = null;
 				if (menuItem.getData() == null || menuItem.getData().getActiveStations() == null) {
 					activeStations = new ArrayList<ActiveStation>();
 				} else {
 					activeStations = menuItem.getData().getActiveStations();
 				}
 				activeStations.add(activeStation);                
 				menuItem.getData().setActiveStations(activeStations);  
 	  
 			} else if (((String) menuItem.getSelectDatasetComboBox().getSelectedItem()).startsWith(StationInformationMenuItem.SIMULATION_LABEL)) {
 
 				AISFixedStationStatus status = new AISFixedStationStatus();
 				status.setStatusID(DerbyDBInterface.STATUS_SIMULATED);
 				stationData.setStatus(status);
 
 				if (menuItem.getData() != null && menuItem.getData().getSimulatedStations() != null) {                
 					for (Simulation s : menuItem.getData().getSimulatedStations()) {
 						if (((String) menuItem.getSelectDatasetComboBox().getSelectedItem()).endsWith(s.getName())) {
 							List<AISFixedStationData> stations = s.getStations();
 							if (stations == null) {
 								stations = new ArrayList<AISFixedStationData>();
 							}
 							stations.add(stationData);
 						}
 					}
 				}
 			}
 
 			DBHandler.saveData(menuItem.getData());
 
 			return true;
 			
 		} else {
 			return false;
 		}
     }
 	
 }
