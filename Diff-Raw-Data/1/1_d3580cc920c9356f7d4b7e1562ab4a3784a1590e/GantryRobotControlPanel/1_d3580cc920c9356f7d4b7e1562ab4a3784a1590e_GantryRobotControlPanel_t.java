 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.ArrayList;
 import javax.swing.*;
 
 /**
  * This class is the control panel inside FactoryControlManager
  * that controls the Gantry Robot device
  *
  */
 @SuppressWarnings("serial")
 public class GantryRobotControlPanel extends JPanel implements ActionListener {	
 		FactoryControlManager fcm;
 		ImageIcon gantryRobotImage, partsBoxImage, feederImage;
 		JPanel gantryRobotTitleLabelPanel, gantryRobotImageLabelPanel, robotOnOffButtonPanel, robotPauseCancelButtonPanel, partsBinsLabelPanel, sparePartsLabelPanel; 
 		JPanel blankPanel1, blankPanel2, partsBoxStoragePanel, feederPanel, sparePartsPanel;
 		JLabel gantryRobotTitleLabel, gantryRobotImageLabel, partsBinsLabel, sparePartsLabel;
 		JButton pausePlayButton, cancelMoveButton;	
 		JRadioButton gantryRobotOnButton, gantryRobotOffButton;
 		ButtonGroup onOffButtonGroup;
 		Dimension boxButtonSize, blankPanelSize, textFieldSize, boxPanelSize, feederPanelSize, controlButtonSize;
 		ArrayList<JPanel> singlePartsBoxPanels, singleFeederPanels, singlePurgeBoxPanels, singleSparePartsPanels;
 		ArrayList<JButton> partsBoxStorageButtons, feederButtons, partPurgeBoxButtons, sparePartsButtons;
 		ArrayList<JTextField> partsBoxStorageTextFields, feederTextFields, sparePartsTextFields;
 		boolean firstButtonSelected = false; // tracks if the user has already made a source selection, i.e. the next button selected will be the destination
 		int partsBoxNumber, feederNumber, purgeBoxNumber, sparePartsBoxNumber;
 		
 		/**
 		 * Constructor; sets layout for panel
 		 * 
 		 * @param fcm pointer to FactoryControlManager object
 		 */
 		public GantryRobotControlPanel( FactoryControlManager fcm ) {
 			this.fcm = fcm;
 			
 			//ImageIcons
 			gantryRobotImage = new ImageIcon( "images/guiserver_thumbs/gantry_thumb.png" );
 			partsBoxImage = new ImageIcon( "images/guiserver_thumbs/partsbox_thumb.png" );
 			feederImage = new ImageIcon( "images/guiserver_thumbs/feeder_thumb.png" );
 			
 			//Dimensions
 			boxButtonSize = new Dimension( 85, 85 );
 			blankPanelSize = new Dimension( 10, 300 );
 			textFieldSize = new Dimension( 70, 15 );
 			boxPanelSize = new Dimension( 85, 110 );
 			feederPanelSize = new Dimension( 191, 100 );
 			controlButtonSize = new Dimension( 60, 40 );
 			
 			//JPanels
 			gantryRobotTitleLabelPanel = new JPanel();
 			gantryRobotImageLabelPanel = new JPanel();
 			robotOnOffButtonPanel = new JPanel();
 			robotPauseCancelButtonPanel = new JPanel();
 			partsBinsLabelPanel = new JPanel();
 			sparePartsLabelPanel = new JPanel();
 			blankPanel1 = new JPanel();
 			blankPanel2 = new JPanel();
 			partsBoxStoragePanel = new JPanel();
 			feederPanel = new JPanel();
 			sparePartsPanel = new JPanel();
 			singlePartsBoxPanels = new ArrayList<JPanel>();
 			singleFeederPanels = new ArrayList<JPanel>();
 			singlePurgeBoxPanels = new ArrayList<JPanel>();
 			singleSparePartsPanels = new ArrayList<JPanel>();
 			
 			//JTextFields
 			partsBoxStorageTextFields = new ArrayList<JTextField>();
 			for( int i = 0; i < 8; i++ ) {
 				partsBoxStorageTextFields.add( new JTextField() );
 				partsBoxStorageTextFields.get( i ).setEditable( false );
 				partsBoxStorageTextFields.get( i ).setPreferredSize( textFieldSize );
 				partsBoxStorageTextFields.get( i ).setMaximumSize( textFieldSize );
 				partsBoxStorageTextFields.get( i ).setMinimumSize( textFieldSize );
 				partsBoxStorageTextFields.get( i ).setFont( new Font( "Sans-Serif", Font.PLAIN, 10 ) );
 				partsBoxStorageTextFields.get( i ).setHorizontalAlignment( JTextField.CENTER );
 			}
 			
 			feederTextFields = new ArrayList<JTextField>();
 			sparePartsTextFields = new ArrayList<JTextField>();
 			for( int i = 0; i < 4; i++ ) {
 								
 				feederTextFields.add( new JTextField() );
 				feederTextFields.get( i ).setEditable( false );
 				feederTextFields.get( i ).setPreferredSize( textFieldSize );
 				feederTextFields.get( i ).setMaximumSize( textFieldSize );
 				feederTextFields.get( i ).setMinimumSize( textFieldSize );
 				feederTextFields.get( i ).setFont( new Font( "Sans-Serif", Font.PLAIN, 10 ) );
 				feederTextFields.get( i ).setHorizontalAlignment( JTextField.CENTER );
 				
 				sparePartsTextFields.add( new JTextField() );
 				sparePartsTextFields.get( i ).setEditable( false );
 				sparePartsTextFields.get( i ).setPreferredSize( textFieldSize );
 				sparePartsTextFields.get( i ).setMaximumSize( textFieldSize );
 				sparePartsTextFields.get( i ).setMinimumSize( textFieldSize );
 				sparePartsTextFields.get( i ).setFont( new Font( "Sans-Serif", Font.PLAIN, 10 ) );
 				sparePartsTextFields.get( i ).setHorizontalAlignment( JTextField.CENTER );
 			}
 			
 			//JLabels
 			gantryRobotTitleLabel = new JLabel();
 			gantryRobotTitleLabel.setText( "Gantry Robot" );
 			gantryRobotTitleLabel.setFont( new Font( "Serif", Font.BOLD, 24 ) );
 			gantryRobotImageLabel = new JLabel();
 			gantryRobotImageLabel.setIcon( gantryRobotImage );
 			partsBinsLabel = new JLabel();
 			partsBinsLabel.setText( "Parts Bins" );
 			partsBinsLabel.setFont( new Font( "Serif", Font.BOLD, 20 ) );
 			sparePartsLabel = new JLabel();
 			sparePartsLabel.setText( "Spare Parts" );
 			sparePartsLabel.setFont( new Font( "Serif", Font.BOLD, 20 ) );
 			
 			//JButtons
 			pausePlayButton = new JButton();
 			pausePlayButton.setText( "Pause" );
 			pausePlayButton.setPreferredSize( controlButtonSize );
 			pausePlayButton.setMaximumSize( controlButtonSize );
 			pausePlayButton.setMinimumSize( controlButtonSize );
 			pausePlayButton.setMargin( new Insets( 0, 0, 0, 0 ) );
 			pausePlayButton.setEnabled( false );
 			pausePlayButton.addActionListener( this );
 			cancelMoveButton = new JButton();
 			cancelMoveButton.setText( "<html><body style=\"text-align:center;\">Cancel<br/>Move</body></html>" );
 			cancelMoveButton.setPreferredSize( controlButtonSize );
 			cancelMoveButton.setMaximumSize( controlButtonSize );
 			cancelMoveButton.setMinimumSize( controlButtonSize );
 			cancelMoveButton.setMargin( new Insets( 0, 0, 0, 0 ) );
 			cancelMoveButton.addActionListener( this );
 			partsBoxStorageButtons = new ArrayList<JButton>();
 			feederButtons = new ArrayList<JButton>();
 			partPurgeBoxButtons = new ArrayList<JButton>();
 			sparePartsButtons = new ArrayList<JButton>();
 			
 			for( int i = 0; i < 8; i++ ) {
 				partsBoxStorageButtons.add( new JButton() );
 				partsBoxStorageButtons.get( i ).setIcon( partsBoxImage );
 				partsBoxStorageButtons.get( i ).setPreferredSize( boxButtonSize );
 				partsBoxStorageButtons.get( i ).setMaximumSize( boxButtonSize );
 				partsBoxStorageButtons.get( i ).setMinimumSize( boxButtonSize );
 				partsBoxStorageButtons.get( i ).addActionListener( this );
 				partsBoxStorageButtons.get( i ).setActionCommand( "parts_box" );
 			}
 			
 			for( int i = 0; i < 4; i++ ) {
 								
 				feederButtons.add( new JButton() );
 				feederButtons.get( i ).setIcon( feederImage );
 				feederButtons.get( i ).setMargin( new Insets( 0, 0, 0, 0 ) );
 				feederButtons.get( i ).setContentAreaFilled( false );
 				feederButtons.get( i ).addActionListener( this );
 				feederButtons.get( i ).setActionCommand( "feeder" );
 				
 				partPurgeBoxButtons.add( new JButton() );
 				partPurgeBoxButtons.get( i ).setIcon( partsBoxImage );
 				partPurgeBoxButtons.get( i ).setPreferredSize( boxButtonSize );
 				partPurgeBoxButtons.get( i ).setMaximumSize( boxButtonSize );
 				partPurgeBoxButtons.get( i ).setMinimumSize( boxButtonSize );
 				partPurgeBoxButtons.get( i ).addActionListener( this );
 				partPurgeBoxButtons.get( i ).setActionCommand( "purge_box" );
 				
 				sparePartsButtons.add( new JButton() );
 				sparePartsButtons.get( i ).setIcon( partsBoxImage );
 				sparePartsButtons.get( i ).setPreferredSize( boxButtonSize );
 				sparePartsButtons.get( i ).setMaximumSize( boxButtonSize );
 				sparePartsButtons.get( i ).setMinimumSize( boxButtonSize );
 				sparePartsButtons.get( i ).addActionListener( this );
 				sparePartsButtons.get( i ).setActionCommand( "spare_parts" );
 			}
 			setFeederButtonsEnabled( false );
 			
 			//JRadioButtons
 			gantryRobotOnButton = new JRadioButton();
 			gantryRobotOnButton.setText( "ON" );
 			gantryRobotOnButton.addActionListener( this );
 			gantryRobotOffButton = new JRadioButton();
 			gantryRobotOffButton.setText( "OFF" );
 			gantryRobotOffButton.addActionListener( this );
 			onOffButtonGroup = new ButtonGroup();
 			onOffButtonGroup.add( gantryRobotOnButton );
 			onOffButtonGroup.add( gantryRobotOffButton );
 			
 			//Layout
 			
 			gantryRobotTitleLabelPanel.setLayout( new BoxLayout( gantryRobotTitleLabelPanel, BoxLayout.X_AXIS ) );
 			gantryRobotTitleLabelPanel.add( Box.createHorizontalStrut( 50 ) );
 			gantryRobotTitleLabelPanel.add( gantryRobotTitleLabel );
 			gantryRobotTitleLabelPanel.add( Box.createGlue() );
 			
 			partsBinsLabelPanel.setLayout( new BoxLayout( partsBinsLabelPanel, BoxLayout.X_AXIS ) );
 			partsBinsLabelPanel.add( Box.createGlue() );
 			partsBinsLabelPanel.add( partsBinsLabel );
 			partsBinsLabelPanel.add( Box.createGlue() );
 			
 			sparePartsLabelPanel.setLayout( new BoxLayout( sparePartsLabelPanel, BoxLayout.X_AXIS ) );
 			sparePartsLabelPanel.add( Box.createGlue() );
 			sparePartsLabelPanel.add( sparePartsLabel );
 			sparePartsLabelPanel.add( Box.createGlue() );
 			
 			gantryRobotImageLabelPanel.add( gantryRobotImageLabel );
 			
 			blankPanel1.setPreferredSize( blankPanelSize );
 			blankPanel1.setMaximumSize( blankPanelSize );
 			blankPanel1.setMinimumSize( blankPanelSize );
 			
 			blankPanel2.setPreferredSize( blankPanelSize );
 			blankPanel2.setMaximumSize( blankPanelSize );
 			blankPanel2.setMinimumSize( blankPanelSize );
 			
 			robotOnOffButtonPanel.setLayout( new BoxLayout( robotOnOffButtonPanel, BoxLayout.X_AXIS ) );
 			robotOnOffButtonPanel.add( Box.createGlue() );
 			robotOnOffButtonPanel.add( gantryRobotOnButton );
 			robotOnOffButtonPanel.add(Box.createHorizontalStrut( 20 ) );
 			robotOnOffButtonPanel.add( gantryRobotOffButton );
 			robotOnOffButtonPanel.add( Box.createGlue() );
 			
 			robotPauseCancelButtonPanel.setLayout( new BoxLayout( robotPauseCancelButtonPanel, BoxLayout.X_AXIS ) );
 			robotPauseCancelButtonPanel.add( Box.createGlue() );
 			robotPauseCancelButtonPanel.add( pausePlayButton );
 			robotPauseCancelButtonPanel.add(Box.createHorizontalStrut( 20 ) );
 			robotPauseCancelButtonPanel.add( cancelMoveButton );
 			robotPauseCancelButtonPanel.add( Box.createGlue() );	
 			
 			for ( int i = 0; i < 8; i++ ) {
 				
 				singlePartsBoxPanels.add( new JPanel() );
 				singlePartsBoxPanels.get( i ).setLayout( new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
 				singlePartsBoxPanels.get( i ).setPreferredSize( boxPanelSize );
 				singlePartsBoxPanels.get( i ).setMaximumSize( boxPanelSize );
 				singlePartsBoxPanels.get( i ).setMinimumSize( boxPanelSize );
 				singlePartsBoxPanels.get( i ).add( partsBoxStorageButtons.get( i ) );
 				singlePartsBoxPanels.get( i ).add( partsBoxStorageTextFields.get( i ) );
 			}
 			
 			for ( int i = 0; i < 4; i++ ) {
 				
 				singleFeederPanels.add( new JPanel() );
 				singleFeederPanels.get( i ).setLayout( new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
 				singleFeederPanels.get( i ).setPreferredSize( feederPanelSize );
 				singleFeederPanels.get( i ).setMaximumSize( feederPanelSize );
 				singleFeederPanels.get( i ).setMinimumSize( feederPanelSize );
 				singleFeederPanels.get( i ).add( feederButtons.get( i ) );
 				singleFeederPanels.get( i ).add( feederTextFields.get( i ) );
 				
 				singleSparePartsPanels.add( new JPanel() );
 				singleSparePartsPanels.get( i ).setLayout( new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
 				singleSparePartsPanels.get( i ).setPreferredSize( boxPanelSize );
 				singleSparePartsPanels.get( i ).setMaximumSize( boxPanelSize );
 				singleSparePartsPanels.get( i ).setMinimumSize( boxPanelSize );
 				singleSparePartsPanels.get( i ).add( sparePartsButtons.get( i ) );
 				singleSparePartsPanels.get( i ).add( sparePartsTextFields.get( i ) );	
 			}
 			
 			partsBoxStoragePanel.setLayout( new GridBagLayout() );
 			GridBagConstraints a = new GridBagConstraints();
 			a.gridx = a.gridy = 0;
 			a.gridwidth = 4;
 			a.fill = GridBagConstraints.HORIZONTAL;
 			partsBoxStoragePanel.add( partsBinsLabelPanel, a );
 			a.gridwidth = 1;
 			a.gridheight = 2;
 			a.fill = GridBagConstraints.NONE;
 			a.insets = new Insets( 5, 1, 0, 1 );
 			int counter = 0;
 			for( a.gridy = 1; a.gridy < 4; a.gridy += 2 ) {
 				for( a.gridx = 0; a.gridx < 4; a.gridx++ ) {
 					partsBoxStoragePanel.add( singlePartsBoxPanels.get( counter++ ), a );
 				}
 			}
 			
 			feederPanel.setLayout( new GridBagLayout() );
 			GridBagConstraints b = new GridBagConstraints();
 			b.gridx = b.gridy = 0;
 			b.insets = new Insets( 10, 0, 0, 0 );
 			for( JPanel feeder : singleFeederPanels ) {
 				feederPanel.add( feeder, b );
 				b.gridy++;
 			}
 			b.gridx++;
 			b.gridy = 0;
 			for( JButton boxButton : partPurgeBoxButtons ) {
 				feederPanel.add( boxButton, b );
 				b.gridy++;
 			}
 			
 			sparePartsPanel.setLayout( new BoxLayout( sparePartsPanel, BoxLayout.Y_AXIS ) );
 			sparePartsPanel.add( Box.createVerticalStrut( 10 ) );
 			sparePartsPanel.add( sparePartsLabelPanel );
 			for ( JPanel sparePartsBox : singleSparePartsPanels ) {
 				sparePartsPanel.add( sparePartsBox );
 				sparePartsPanel.add( Box.createGlue() );
 			}
 				
 			setBorder( BorderFactory.createLineBorder( Color.black ) );
 			setLayout( new GridBagLayout() );
 			GridBagConstraints c = new GridBagConstraints();
 			
 			c.gridx = c.gridy = 0;
 			c.gridwidth =  2;
 			c.gridheight = 4;
 			add( gantryRobotImageLabelPanel, c );
 			c.gridy = 4;
 			c.gridheight = 1;
 			c.fill = GridBagConstraints.HORIZONTAL;
 			add( robotOnOffButtonPanel, c );
 			c.gridy = 5;
 			add( robotPauseCancelButtonPanel, c );
 			c.gridy = 6;
 			c.gridwidth = 2;
 			c.gridheight = 6;
 			c.fill = GridBagConstraints.NONE;
 			add( partsBoxStoragePanel, c );
 			c.gridx = 2;
 			c.gridy = 0;
 			c.gridwidth = 7;
 			c.gridheight = 2;
 			c.fill = GridBagConstraints.HORIZONTAL;
 			add( gantryRobotTitleLabelPanel, c );
 			c.gridy = 2;
 			c.gridwidth = 1;
 			c.gridheight = 10;
 			c.fill = GridBagConstraints.VERTICAL;
 			add( blankPanel1, c );
 			c.gridx = 3;
 			c.gridwidth = 4;
 			c.gridheight = 12;
 			c.fill = GridBagConstraints.NONE;
 			add( feederPanel, c );
 			c.gridx = 7;
 			c.gridwidth = 1;
 			add( blankPanel2, c );
 			c.gridx = 8;
 			c.gridwidth = 2;
 			add( sparePartsPanel, c );
 			
 			//Initialize gantry state and box contents
 			GUIGantry gantry = fcm.server.getGantry();
 			if ( gantry.state == GUIGantry.GRState.OFF )
 				setGantryRobotOn( false );
 			else
 				setGantryRobotOn( true );
 		}
 
 		/**
 		 * Returns true if the gantry robot is on
 		 * 
 		 * @return boolean variable that is true if the gantry robot is on
 		 */
 		public boolean getGantryRobotOn() { return gantryRobotOnButton.isSelected(); }
 		
 		/**
 		 * Sets gantry robot on/off radio buttons
 		 * 
 		 * @param on boolean variable to set robot on or off
 		 */
 		public void setGantryRobotOn ( boolean on ) {
 			gantryRobotOnButton.setSelected( on );
 			gantryRobotOffButton.setSelected( !on );
 			if ( on )
 				resetMoveButtons();
 			else {
 				setCancelMoveButtonEnabled( false );
 				setPausePlayButtonEnabled( false );
 				setPartsBoxStorageButtonsEnabled( false );
 				setFeederButtonsEnabled( false );
 				setPartPurgeBoxButtonsEnabled( false );
 				setSparePartsButtonsEnabled( false );
 			}
 		}
 		
 		/**
 		 * Sets the pause/play button text
 		 * 
 		 * @param text String to set the text to
 		 */
 		public void setPausePlayButtonText( String text ) { pausePlayButton.setText( text ); }
 		
 		/**
 		 * Enables or disables the "Cancel Move" button
 		 * 
 		 * @param enabled boolean variable to set if the button is enabled
 		 */
 		public void setCancelMoveButtonEnabled( boolean enabled ) { cancelMoveButton.setEnabled( enabled ); }
 		
 		/**
 		 * Enables or disables the "Pause/Play" button
 		 * 
 		 * @param enabled boolean variable to set if the button is enabled
 		 */
 		public void setPausePlayButtonEnabled( boolean enabled ) { pausePlayButton.setEnabled( enabled ); }
 		
 		/**
 		 * Enables or disables all buttons in partsBoxStorageButtons
 		 * 
 		 * @param enabled boolean variable to set if the buttons are enabled
 		 */
 		public void setPartsBoxStorageButtonsEnabled( boolean enabled ) {
 			for ( JButton button : partsBoxStorageButtons ) {
 				button.setEnabled( enabled );
 			}
 		}
 		
 		/**
 		 * Enables or disables all buttons in feederButtons
 		 * 
 		 * @param enabled boolean variable to set if the buttons are enabled
 		 */
 		public void setFeederButtonsEnabled( boolean enabled ) {
 			for ( JButton button : feederButtons ) {
 				button.setEnabled( enabled );
 			}
 		}
 		
 		/**
 		 * Enables or disables all buttons in partPurgeBoxButtons
 		 * 
 		 * @param enabled boolean variable to set if the buttons are enabled
 		 */
 		public void setPartPurgeBoxButtonsEnabled( boolean enabled ) {
 			for ( JButton button : partPurgeBoxButtons ) {
 				button.setEnabled( enabled );
 			}
 		}
 		
 		/**
 		 * Enables or disables all buttons in sparePartsButtons
 		 * 
 		 * @param enabled boolean variable to set if the buttons are enabled
 		 */
 		public void setSparePartsButtonsEnabled( boolean enabled ) {
 			for ( JButton button : sparePartsButtons ) {
 				button.setEnabled( enabled );
 			}
 		}
 		
 		/**
 		 * Sets the text field below a specified parts storage box showing what part type is in it
 		 * 
 		 * @param partName name of part in box
 		 * @param boxNumber which box text field is to be changed
 		 */
 		public void setPartsBoxStorageContents ( String partName, int boxNumber ) {
 			partsBoxStorageTextFields.get( boxNumber ).setText( partName );
 		}
 		
 		/**
 		 * Sets the text field below a specified feeder showing what part type is in it
 		 * 
 		 * @param partName name of part in the feeder
 		 * @param boxNumber which feeder text field is to be changed
 		 */
 		public void setFeederContents ( String partName, int feederNumber ) {
 			feederTextFields.get( feederNumber ).setText( partName );
 		}
 		
 		/**
 		 * Sets the text field below a specified spare parts box showing what part type is in it
 		 * 
 		 * @param partName name of part in box
 		 * @param boxNumber which box text field is to be changed
 		 */
 		public void setSparePartsBoxContents ( String partName, int boxNumber ) {
 			sparePartsTextFields.get( boxNumber ).setText( partName );
 		}
 		
 		/**
 		 * This method resets the enabled/disabled state of all the buttons for the user
 		 * to begin inputting a new task for the robot
 		 */
 		public void resetMoveButtons() {
 			if ( getGantryRobotOn() ) {
 				setPartsBoxStorageButtonsEnabled( true );
 				setPartPurgeBoxButtonsEnabled( true );
 				setSparePartsButtonsEnabled( true );
 				firstButtonSelected = false;
 			}
 		}
 		
 		/**
 		 * Gives functionality to all the JButtons in the GantryRobotControlPanel
 		 * 
 		 */
 		public void actionPerformed( ActionEvent ae ) {
 			String cmd = "";
 			if ( ae.getActionCommand() != null) 
 				cmd = ae.getActionCommand();
 
 			// get entry corresponding to gantry robot
 			int grKey = fcm.server.gantryID;
 			GUIGantry gantry = fcm.server.getGantry();
 			
 			//This will turn the gantry robot on
 			if ( ae.getSource() == gantryRobotOnButton ) {
 				setCancelMoveButtonEnabled( true );
 				setPartsBoxStorageButtonsEnabled( true );
 				setPartPurgeBoxButtonsEnabled( true );
 				setSparePartsButtonsEnabled( true );
 				setPausePlayButtonEnabled( false );
 				firstButtonSelected = false;
 				if ( gantry.state == GUIGantry.GRState.OFF ) {
 					gantry.state = GUIGantry.GRState.IDLE;
 					// prepare factory update message
 					FactoryUpdateMsg update = new FactoryUpdateMsg(fcm.server.getState());
 					update.putItems.put(grKey, gantry); // put updated gantry robot in update message
 					fcm.server.applyUpdate(update); // apply and broadcast update message
 				}
 			}
 			
 			//This will turn the gantry robot off
 			else if ( ae.getSource() == gantryRobotOffButton ) {
 				setCancelMoveButtonEnabled( false );
 				setPausePlayButtonEnabled( false );
 				setPartsBoxStorageButtonsEnabled( false );
 				setFeederButtonsEnabled( false );
 				setPartPurgeBoxButtonsEnabled( false );
 				setSparePartsButtonsEnabled( false );
 				if ( gantry.state != GUIGantry.GRState.OFF ) {
 					gantry.state = GUIGantry.GRState.OFF;
 					// prepare factory update message
 					FactoryUpdateMsg update = new FactoryUpdateMsg(fcm.server.getState());
 					update.putItems.put(grKey, gantry); // put updated gantry robot in update message
 					fcm.server.applyUpdate(update); // apply and broadcast update message
 				}
 			}
 			
 			//This button allows the user to pause the robot mid-task
 			else if ( ae.getSource() == pausePlayButton ) {
 				if ( pausePlayButton.getText().equals( "Pause" ) )
 					setPausePlayButtonText( "Play" );
 				else 
 					setPausePlayButtonText( "Pause" );
 			}
 			
 			//This button will reset all the buttons to their original enabled/disabled state.
 			else if ( ae.getSource() == cancelMoveButton ) {
 				resetMoveButtons();
 			}
 			
 			/*
 			 * If a parts box is selected the user is only allowed to selected a feeder to put them in.
 			 * All other buttons are disabled.  The for loop finds which parts box the command originated from
 			 */
 			else if ( cmd.equals( "parts_box" ) ) {
 				setPartsBoxStorageButtonsEnabled( false );
 				setPartPurgeBoxButtonsEnabled( false );
 				setSparePartsButtonsEnabled( false );
 				setFeederButtonsEnabled( true );
 				firstButtonSelected = true;
 				for( int i = 0; i < partsBoxStorageButtons.size(); i++ ) {
 					if ( ae.getSource() == partsBoxStorageButtons.get( i ) ) {
 						partsBoxNumber = i;
 						// get entry corresponding to this parts box
						if (partsBoxNumber >= fcm.server.partBinIDs.size()) return;
 						int binKey = fcm.server.partBinIDs.get(partsBoxNumber);
 						GUIBin bin = fcm.server.getPartBin(partsBoxNumber);
 						// prepare factory update message
 						FactoryUpdateMsg update = new FactoryUpdateMsg(fcm.server.getState());
 						gantry.state = GUIGantry.GRState.PART_BIN;
 						gantry.targetID = partsBoxNumber;
 						Point2D.Double target = bin.movement.getStartPos();
 						gantry.movement = gantry.movement.moveToAtSpeed(update.timeElapsed, target, 0, GUIGantry.SPEED);
 						update.putItems.put(grKey, gantry); // put updated gantry robot in update message
 						fcm.server.applyUpdate(update); // apply and broadcast update message
 						return; // no need to check if other buttons selected
 					}
 				}
 			}
 			
 			/*
 			 * The feeder cannot be the first button selected so, if it is selected, all other buttons are disabled
 			 * until the gantry robot completes its task.  The for loop finds which feeder the command originated from
 			 */
 			else if ( cmd.equals( "feeder" ) ) {
 				setFeederButtonsEnabled( false );
 				setPausePlayButtonEnabled( true );
 				setCancelMoveButtonEnabled( false );
 				for( int i = 0; i < feederButtons.size(); i++ ) {
 					if ( ae.getSource() == feederButtons.get( i ) ) {
 						feederNumber = i;
 						// get entry corresponding to this feeder
 						int feederKey = fcm.server.feederIDs.get(feederNumber);
 						GUIFeeder feeder = fcm.server.getFeeder(feederNumber);
 						// prepare factory update message
 						FactoryUpdateMsg update = new FactoryUpdateMsg(fcm.server.getState());
 						gantry.state = GUIGantry.GRState.FEEDER;
 						gantry.targetID = feederNumber;
 						Point2D.Double target = feeder.movement.getStartPos();
 						gantry.movement = gantry.movement.moveToAtSpeed(update.timeElapsed, target, 0, GUIGantry.SPEED);
 						update.putItems.put(grKey, gantry); // put updated gantry robot in update message
 						fcm.server.applyUpdate(update); // apply and broadcast update message
 						return; // no need to check if other buttons selected
 					}
 				}
 			}
 			
 			/*
 			 * If a purge box is selected the user is only allowed to selected a spare parts box to put them in.
 			 * All other buttons are disabled.  The for loop finds which purge box the command originated from
 			 */
 			else if ( cmd.equals( "purge_box" ) ) {
 				setPartsBoxStorageButtonsEnabled( false );
 				setPartPurgeBoxButtonsEnabled( false );
 				firstButtonSelected = true;
 				for( int i = 0; i < partPurgeBoxButtons.size(); i++ ) {
 					if ( ae.getSource() == partPurgeBoxButtons.get( i ) )
 						purgeBoxNumber = i;
 				}
 			}
 			
 			/*
 			 * If the spare parts box is the first button selected, the user can only select the feeders as a
 			 * destination. All other buttons are disabled. If the spare parts box is the second button selected,
 			 * all buttons are disabled until the gantry robot completes its task. The for loop finds which 
 			 * spare parts box the command originated from. 
 			 */
 			else if ( cmd.equals( "spare_parts" ) ) {
 				if ( firstButtonSelected ) {
 					setPartPurgeBoxButtonsEnabled( false );
 					setSparePartsButtonsEnabled( false );
 					setPausePlayButtonEnabled( true );
 					setCancelMoveButtonEnabled( false );
 				}
 				else {
 					setPartsBoxStorageButtonsEnabled( false );
 					setFeederButtonsEnabled( true );
 					setPartPurgeBoxButtonsEnabled( false );
 					setSparePartsButtonsEnabled( false );
 					firstButtonSelected = true;
 				}
 				for( int i = 0; i < sparePartsButtons.size(); i++ ) {
 					if ( ae.getSource() == sparePartsButtons.get( i ) )
 						sparePartsBoxNumber = i;
 				}
 			}
 		}
 	}
