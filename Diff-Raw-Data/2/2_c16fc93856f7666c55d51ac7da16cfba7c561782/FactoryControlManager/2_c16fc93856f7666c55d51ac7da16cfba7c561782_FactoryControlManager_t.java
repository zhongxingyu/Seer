 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 import javax.swing.*;
 /**
  * This class is a parent class for each of the control panels for the
  * various devices in the factory
  *
  */
 @SuppressWarnings("serial")
 public class FactoryControlManager extends JFrame implements ActionListener {
 	Server server;
 	ImageIcon kitStandImage;
 	JPanel mainGUIPanel, nestLaneFeederPanel, controlPanel, cardLayoutAndControlPanel, kitQueuePanel;
 	KitRobotControlPanel kitRobotPanel;
 	GantryRobotControlPanel gantryRobotPanel;
 	PartRobotControlPanel partRobotPanel;
 	NestControlPanel nestPanel;
 	LaneControlPanel lanePanel;
 	FeederControlPanel feederPanel;
 	JButton kitRobotButton, partRobotButton, gantryRobotButton, nestLaneFeederButton;
 	Dimension mainGUIPanelSize, controlPanelSize, kitQueueSize, controlButtonSize;
 	CardLayout cl;
 	ArrayList<Kit> kits; //kits in production queue to be displayed in the kit queue panel
 	ArrayList<JButton> scheduleButtons;
 	
 	/**
 	 * Constructor
 	 * @param server pointer to Server object
 	 */
 	public FactoryControlManager(Server server) {
 		//store reference to server
 		this.server = server;
 		
 		//Kits
 		kits = new ArrayList<Kit>();
 
 		//ImageIcons
 		kitStandImage = new ImageIcon( "images/guiserver_thumbs/kit_table_thumb.png" );
 		
 		//JPanels
 		mainGUIPanel = new JPanel();
 		nestLaneFeederPanel = new JPanel();
 		controlPanel = new JPanel();
 		cardLayoutAndControlPanel = new JPanel();
 		kitQueuePanel = new JPanel();
 		updateSchedule(server.getKits(),server.getStatus());
 		kitRobotPanel = new KitRobotControlPanel( this );
 		gantryRobotPanel = new GantryRobotControlPanel( this );
 		partRobotPanel = new PartRobotControlPanel( this );
 		nestPanel = new NestControlPanel( this );
 		lanePanel = new LaneControlPanel( this );
 		feederPanel = new FeederControlPanel( this );
 		
 		//Dimensions
 		mainGUIPanelSize = new Dimension( 750, 532 );
 		controlPanelSize = new Dimension( 750, 40 );
 		kitQueueSize = new Dimension( 300, 572 );
 		controlButtonSize = new Dimension( 160, 30 );
 		
 		//JButtons
 		kitRobotButton = new JButton();
 		kitRobotButton.setText( "Kit Robot" );
 		kitRobotButton.setPreferredSize( controlButtonSize );
 		kitRobotButton.setMaximumSize( controlButtonSize );
 		kitRobotButton.setMinimumSize( controlButtonSize );
 		kitRobotButton.addActionListener( this );
 		partRobotButton = new JButton();
 		partRobotButton.setText( "Part Robot" );
 		partRobotButton.setPreferredSize( controlButtonSize );
 		partRobotButton.setMaximumSize( controlButtonSize );
 		partRobotButton.setMinimumSize( controlButtonSize );
 		partRobotButton.addActionListener( this );
 		gantryRobotButton = new JButton();
 		gantryRobotButton.setText( "Gantry Robot" );
 		gantryRobotButton.setPreferredSize( controlButtonSize );
 		gantryRobotButton.setMaximumSize( controlButtonSize );
 		gantryRobotButton.setMinimumSize( controlButtonSize );
 		gantryRobotButton.addActionListener( this );
 		nestLaneFeederButton = new JButton();
 		nestLaneFeederButton.setText( "Nests Lanes Feeders" );
 		nestLaneFeederButton.setMargin( new Insets( 0, 0, 0, 0 ) );
 		nestLaneFeederButton.setPreferredSize( controlButtonSize );
 		nestLaneFeederButton.setMaximumSize( controlButtonSize );
 		nestLaneFeederButton.setMinimumSize( controlButtonSize );
 		nestLaneFeederButton.addActionListener( this );
 		
 		//Layout
 		cl = new CardLayout();
 	
 		nestLaneFeederPanel.setLayout( new BoxLayout( nestLaneFeederPanel, BoxLayout.X_AXIS ) );
 		nestLaneFeederPanel.add( nestPanel );
 		nestLaneFeederPanel.add( lanePanel );
 		nestLaneFeederPanel.add( feederPanel );
 		
 		mainGUIPanel.setLayout( cl );
 		mainGUIPanel.setPreferredSize( mainGUIPanelSize );
 		mainGUIPanel.setMaximumSize( mainGUIPanelSize );
 		mainGUIPanel.setMinimumSize( mainGUIPanelSize );
 		mainGUIPanel.add( kitRobotPanel, "kit_robot_panel" );
 		mainGUIPanel.add( partRobotPanel, "part_robot_panel" );
 		mainGUIPanel.add( gantryRobotPanel, "gantry_robot_panel" );
 		mainGUIPanel.add( nestLaneFeederPanel, "nest_lane_feeder_panel" );
 		
 		controlPanel.setLayout( new BoxLayout( controlPanel, BoxLayout.X_AXIS ) );
 		controlPanel.setBorder( BorderFactory.createLineBorder( Color.black ) );
 		controlPanel.setPreferredSize( controlPanelSize );
 		controlPanel.setMaximumSize( controlPanelSize );
 		controlPanel.setMinimumSize( controlPanelSize );
 		controlPanel.add( Box.createGlue() );
 		controlPanel.add( kitRobotButton );
 		controlPanel.add( Box.createGlue() );
 		controlPanel.add( partRobotButton );
 		controlPanel.add( Box.createGlue() );
 		controlPanel.add( gantryRobotButton );
 		controlPanel.add( Box.createGlue() );
 		controlPanel.add( nestLaneFeederButton );
 		controlPanel.add( Box.createGlue() );
 		
 		cardLayoutAndControlPanel.setLayout( new BoxLayout( cardLayoutAndControlPanel, BoxLayout.Y_AXIS ) );
 		cardLayoutAndControlPanel.add( mainGUIPanel );
 		cardLayoutAndControlPanel.add( controlPanel );
 		
 		kitQueuePanel.setBorder( BorderFactory.createLineBorder( Color.black ) );
 		kitQueuePanel.setPreferredSize( kitQueueSize );
 		kitQueuePanel.setMaximumSize( kitQueueSize );
 		kitQueuePanel.setMinimumSize( kitQueueSize );
 		kitQueuePanel.setLayout(new BoxLayout(kitQueuePanel, BoxLayout.Y_AXIS));
 		
 		setLayout( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
 		add( kitQueuePanel );
 		add( cardLayoutAndControlPanel );
 
		setSize( 1056, 600 );
 		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 		setResizable( false );
 		setVisible( true );
 		addWindowListener(new WindowCloseListener());
 	}
 
 	public void actionPerformed( ActionEvent ae ) {
 		if ( ae.getSource() == kitRobotButton ) {
 			cl.show( mainGUIPanel,  "kit_robot_panel" );
 		}
 		else if ( ae.getSource() == partRobotButton ) {
 			cl.show( mainGUIPanel,  "part_robot_panel" );
 		}
 		else if ( ae.getSource() == gantryRobotButton ) {
 			cl.show( mainGUIPanel,  "gantry_robot_panel" );
 		}
 		else if ( ae.getSource() == nestLaneFeederButton ) {
 			cl.show( mainGUIPanel,  "nest_lane_feeder_panel" );
 		}
 		else {
 			// change production status
 			for (int i = 0; i < scheduleButtons.size(); i++) {
 				if (ae.getSource() == scheduleButtons.get(i)) {
 					if (i < server.getStatus().status.size()) {
 						ProduceStatusMsg.KitStatus kitStatus = server.getStatus().status.get(i);
 						if (kitStatus == ProduceStatusMsg.KitStatus.QUEUED) {
 							server.getStatus().status.set(i, ProduceStatusMsg.KitStatus.PRODUCTION);
 						}
 						else if (kitStatus == ProduceStatusMsg.KitStatus.PRODUCTION) {
 							server.getStatus().status.set(i, ProduceStatusMsg.KitStatus.COMPLETE);
 						}
 						else if (kitStatus == ProduceStatusMsg.KitStatus.COMPLETE) {
 							// remove task from list
 							server.getStatus().cmds.remove(i);
 							server.getStatus().status.remove(i);
 						}
 						server.broadcast(Server.WantsEnum.STATUS);
 					}
 					break;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * This method calls a method in the kitRobotPanel to reset the move buttons
 	 * for the Kit Robot after its movement is finished
 	 */
 	public void enableKitRobotControls() {
 		kitRobotPanel.resetMoveButtons();
 	}
 	
 	/**
 	 * This method calls a method in the partRobotPanel to reset the move buttons
 	 * for the Part Robot after its movement is finished
 	 */
 	public void enablePartRobotControls() {
 		partRobotPanel.resetMoveButtons();
 	}
 	
 	/**
 	 * This method calls a method in the gantryRobotPanel to reset the move buttons
 	 * for the Gantry Robot after its movement is finished
 	 */
 	public void enableGantryRobotControls() {
 		gantryRobotPanel.resetMoveButtons();
 	}
 	
 	/**
 	 * Turns on a light for the kit inspection.
 	 * Passing a "0" will turn on the red light signifying an incorrectly assembled kit
 	 * Passing a "1" will turn on the yellow light signifying an incomplete kit
 	 * Passing a "2" will turn on the green light signifying a correctly assembled kit
 	 * 
 	 * @param status The status of the kit on the inspection stand
 	 */
 	public void kitInspectionStatus( int status ) {
 		if ( status == 0 ) {
 			kitRobotPanel.redLightOn( true );
 		}
 		else if ( status == 1 ) {
 			kitRobotPanel.yellowLightOn( true );
 		}
 		else if ( status == 2 ) {
 			kitRobotPanel.greenLightOn( true );
 		}
 		else
 			System.out.println( "Invalid Kit Inspection Status Received" );
 	}
 	
 	/**
 	 * Turns on a light for the nest inspection.
 	 * Passing a "0" will turn on the red light signifying that the nest pair has 1 or more parts in it that it should not
 	 * Passing a "1" will turn on the yellow light signifying a nest pair that is not full or has not settled completely
 	 * Passing a "2" will turn on the green light signifying a nest pair that is full and has the correct parts
 	 * The PartRobotControlPanel holds a variable that remembers which camera was triggered so specifying the nest pair pair is unnecessary
 	 * 
 	 * @param status The status of the nest pair
 	 */
 	public void nestInspectionStatus( int status ) {
 		if ( status == 0 ) {
 			partRobotPanel.redLightOn( true );
 		}
 		else if ( status == 1 ) {
 			partRobotPanel.yellowLightOn( true );
 		}
 		else if ( status == 2 ) {
 			partRobotPanel.greenLightOn( true );
 		}
 		else
 			System.out.println( "Invalid Nest Inspection Status Received" );
 	}
 	
 	public void updateSchedule(ArrayList<Kit> kitList, ProduceStatusMsg status1 ){
 		ProduceStatusMsg status = status1;
 		kits = kitList;
 		String kitname = "";
 		scheduleButtons = new ArrayList<JButton>();
 		kitQueuePanel.removeAll();
 		if (status.cmds.size() > 0) {
 			for (int i = 0; i < status.cmds.size(); i++) {
 				for (int j = 0; j < kits.size(); j++) {
 					kitname = kits.get(j).getName();
 
 					if (kits.get(j).getNumber() == status.cmds.get(i).kitNumber) {
 						scheduleButtons.add(new JButton(kitname + " - "
 								+ status.cmds.get(i).howMany + " - "
 								+ status.status.get(i)));
 						scheduleButtons.get(scheduleButtons.size() - 1).addActionListener(this);
 						kitQueuePanel.add(scheduleButtons.get(scheduleButtons.size() - 1));
 						
 					}
 				}
 			}
 		} 
 	
 		validate();
 		repaint();
 		
 	}
 	
 	/** class to handle window close event */
 	private class WindowCloseListener extends WindowAdapter {
 		/** handle window close event */
 		public void windowClosing(WindowEvent e) {
 			server.saveSettings();
 		}
 	}
 	
 }
