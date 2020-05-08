 package GUI;
 
 import static GUI.Constants.*;
 import java.awt.*;
 
 import javax.swing.*;
 //Test Guest Commit
 public class MainWindowGUI extends JFrame {
 	Dimension parentDims;
 	
 	public MainWindowGUI(){
 		initComponents();
 	}
 	
 	private void initComponents() {
 		/* Setup bare window */
 		JFrame window = new JFrame(APP_TITLE);
 		window.setLayout(new BorderLayout());
 
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		window.setSize(GUI_DIMENSIONS);
 		window.setPreferredSize(GUI_DIMENSIONS);
 		
 		parentDims = window.getSize();
 		
 		/* Start Adding components to window */
 		JTabbedPane tabs = new JTabbedPane();
 		JPanel mapTabContents = makeMapTab();
 		JPanel itinTabContents = makeItinTab();
 		
 		tabs.addTab(MAP_TITLE, null, mapTabContents, MAP_TOOLTIP);
 		tabs.addTab(ITINERARY_TITLE, null, itinTabContents, ITINERARY_TOOLTIP);
 		window.add(tabs, BorderLayout.CENTER);
 		
 		window.validate();
 		window.pack();
 		window.setVisible(true);
 	}
 	
 	protected JPanel makeMapTab(){
 		
 		/* ---- Cyan wrapper ---- */
 		JPanel mapPanel = new JPanel(new BorderLayout());
 		mapPanel.setBackground(Color.CYAN);
 		mapPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 		
 		/* ---- North Section of wrapper ---- */		
 		JPanel northPanel = new JPanel();
 		northPanel.setBorder(BorderFactory.createEtchedBorder());
 		
 		/* Text input components */
 		GhostTextField inputField = new GhostTextField(INPUT_FIELD_FILLER, 20);
 		inputField.setForeground(GRAY_TEXT_COLOR);
 		JButton submitDestination = new JButton(SUBMIT_DESTINATION_TEXT);
 		
 		/* Dropdown components */
 		JLabel dropdownLabel = new JLabel(DROPDOWN_LABEL_TEXT);
 		JComboBox blueDropdown = new JComboBox(BLUE_STATIONS);
 		blueDropdown.setForeground(Color.BLUE);
 		JComboBox redDropdown = new JComboBox(RED_STATIONS);
 		redDropdown.setForeground(MYRED);
 		JComboBox orangeDropdown = new JComboBox(ORANGE_STATIONS);
 		orangeDropdown.setForeground(MYORANGE);
 		
 		JCheckBox sortedCheckbox = new JCheckBox(SORT_DEST_CHECKBOX_TEXT);
 		
 		northPanel.add(inputField);
 		northPanel.add(submitDestination);
 		northPanel.add(dropdownLabel);
 		northPanel.add(blueDropdown);
 		northPanel.add(redDropdown);
 		northPanel.add(orangeDropdown);
 		northPanel.add(sortedCheckbox);
 		
 		mapPanel.add(northPanel, BorderLayout.PAGE_START);
 		/* ---- End North Section ---- */
 		
 		/* Map Region Components */
 		JPanel mapRegion = new JPanel(new BorderLayout());
 		ImageIcon mapImage = new ImageIcon("Resources/Images/custom-mbta-map.png");
 		JLabel map = new JLabel(mapImage);
 		JScrollPane mapScrollPane = new JScrollPane(map);
 		setPreferredSize(getPreferredSize());
         mapScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.BLACK));
         
 		mapRegion.add(mapScrollPane, BorderLayout.CENTER);
 		
 		/* Destination List Components */
		JPanel destinationList = new JPanel();
 		destinationList.setBackground(Color.WHITE);
 		destinationList.setBorder(BorderFactory.createEtchedBorder());
 		Dimension size = getPreferredSize();
 		size.width = (int)(GUI_DIMENSIONS.width/5);
 		destinationList.setPreferredSize(size);
 		
 		mapPanel.add(mapRegion, BorderLayout.CENTER);
 		mapPanel.add(destinationList, BorderLayout.EAST);
 		
 		return mapPanel;
 	}
 	
 	protected JPanel makeItinTab(){
 		JPanel initPanel = new JPanel();
 		return initPanel;
 	}
 	
 	public static void main(String[] args) {
 		/* Makes new thread for application (to be safe) */
 		java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new MainWindowGUI();
             }
         });
 	}
 
 }
