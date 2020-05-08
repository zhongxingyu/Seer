 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 
 import se.nicklasgavelin.sphero.Robot;
 import se.nicklasgavelin.sphero.macro.MacroObject;
 import se.nicklasgavelin.sphero.macro.command.Delay;
 import se.nicklasgavelin.sphero.macro.command.Roll;
 import se.nicklasgavelin.sphero.macro.command.SPD1;
 //import ConnectThread;
 import se.nicklasgavelin.sphero.macro.command.*;
 
 public class SpheroGUI extends JFrame implements ActionListener
 {
 	
 	private static final long serialVersionUID = 6998786554264771793L;
 	
 	// Internal storage
 	
 	private ConnectThread ct;
 	private JButton connectButton, disconnectButton, panicButton, stopButton;
 	private JButton forwardButton, leftButton, rightButton;
 
 	public SpheroGUI()
 	{
 		super( "Commander" );
 		this.setLayout( new GridLayout( 4, 1 ) );
 
 		// Connect button
 		connectButton = new JButton( "Connect to available devices" );
 		disconnectButton = new JButton( "Disconnect from all devices" );
 		panicButton = new JButton("PANIC!");
 		stopButton = new JButton("STOP");
 		
 		// Add buttons to our GUI
 		this.add( connectButton );
 		this.add( disconnectButton );
 		this.add( panicButton );
 		this.add(stopButton);
 		stopButton.addActionListener(this);
 		panicButton.addActionListener(this);
 		disconnectButton.addActionListener(this);
 		connectButton.addActionListener(this);
 		// Set some default stuff
 		this.pack();
 		this.setVisible( true );
 		this.setDefaultCloseOperation( EXIT_ON_CLOSE );
 		
 		
 		JPanel directionalKeys = new JPanel();
 		// Directional Buttons
 		forwardButton = new JButton("Forward!");
 		leftButton = new JButton("Left");
 		rightButton = new JButton("Right");
 		
 		forwardButton.addActionListener(this);
 		leftButton.addActionListener(this);
 		rightButton.addActionListener(this);
 		
 		directionalKeys.add(forwardButton, BorderLayout.CENTER);
 		directionalKeys.add(stopButton, BorderLayout.CENTER);
 		directionalKeys.add(leftButton, BorderLayout.PAGE_START);
 		directionalKeys.add(rightButton, BorderLayout.PAGE_END);
 		
 		this.add(directionalKeys);
 	}
 	
 	int currentHeading = 0;
 	double speed = 0.2; 
 	Robot rob;
 	Robot rob2;
 	//Testing with many balls
 	private void dostugg(){
 		Iterator<Robot> i = ct.getRobotArray().iterator();
 		rob = i.next();
 		if (i.hasNext()) {rob2 = i.next();}
 	}
 	
 		public void actionPerformed(ActionEvent e) {
 			
 			if (e.getSource()==leftButton){
 				MacroObject m = new MacroObject();
 				currentHeading = (currentHeading+270)%360;
 				m.addCommand(new Roll(speed, currentHeading, 0));
 				ct.r.sendCommand(m);
 			}
 			if (e.getSource()==rightButton){
 				MacroObject m = new MacroObject();
 				currentHeading = (currentHeading+90)%360;
 				m.addCommand(new Roll(speed, currentHeading, 0));
 				ct.r.sendCommand(m);
 			}
 			
 			if (e.getSource()==forwardButton){
 				MacroObject m = new MacroObject();
 				m.addCommand(new Roll(speed, currentHeading, 0));
 				dostugg();
 				rob.sendCommand(m);
 				if (rob2!=null)
 				rob2.sendCommand(m);
 				//ct.r.sendCommand(m);
 			} 
 			
 			
 			if (e.getSource()==stopButton){
 				MacroObject m = new MacroObject();
 				//m.addCommand(command);
 				m.addCommand( new Roll(0,1,10) );
 				rob.sendCommand(m);
 				if (rob2!=null)
 				rob2.sendCommand(m);
 //				ct.r.sendCommand(m);
 //				ct.r.sendCommand(new AbortMacroCommand());
 //				ct.stopThread();
 			} 
 			
 			
 			if (e.getSource()==panicButton){
 				MacroObject m = new MacroObject();
 
 				//Fun stuff
 				m.addCommand( new RGBSD2( Color.ORANGE) );
 				m.addCommand( new Delay(500) );
 				m.addCommand( new Roll(currentHeading, 1, 10) );
 				ct.r.sendCommand(m);
 			}
 				
 			
 			if (e.getSource()==connectButton){
 				if( ct != null )
 					ct.stopThread();
 
 				// Create a new thread
				ct = new ConnectThread("");
 				ct.start();
 
 				// Toggle our button
 				connectButton.setEnabled( false );
 				disconnectButton.setEnabled( true );
 			}
 			
 			
 			if(e.getSource()==disconnectButton){
 				if( ct != null )
 					ct.stopThread();
 
 				// Toggle our buttons
 				connectButton.setEnabled( true );
 				disconnectButton.setEnabled( false );
 			}
 			
 		}
 		void setConnectEnabled( boolean enabled )
 		{
 			this.connectButton.setEnabled( enabled );
 			this.disconnectButton.setEnabled( !enabled );
 		}
 		
 		
 		
 		
 		
 		/*
 		// Bind action to our connect button
 		connectButton.addActionListener( new ActionListener() {
 			@Override
 			public void actionPerformed( ActionEvent e )
 			{
 				// Check if we have something previous to stop
 				if( ct != null )
 					ct.stopThread();
 
 				// Create a new thread
 				ct = new ConnectThread();
 				ct.start();
 
 				// Toggle our button
 				connectButton.setEnabled( false );
 				disconnectButton.setEnabled( true );
 			}
 		} );
 
 		// Bind action to the disconnect button
 		disconnectButton.addActionListener( new ActionListener() {
 			@Override
 			public void actionPerformed( ActionEvent e )
 			{
 				// Check if we have something to stop
 				if( ct != null )
 					ct.stopThread();
 
 				// Toggle our buttons
 				connectButton.setEnabled( true );
 				disconnectButton.setEnabled( false );
 			}
 		} );
 		
 		// Bind action to the disconnect button
 		panicButton.addActionListener( new ActionListener() {
 			@Override
 			public void actionPerformed( ActionEvent e )
 			{
 				MacroObject m = new MacroObject();
 				//m.addCommand(command);
 				
 				
 				ct.r.setRGBLedColor(Color.GREEN);
 				
 				try {
 					ct.r.wait(2000);
 				} catch (InterruptedException e1) {
 					
 					e1.printStackTrace();
 				}
 				
 				ct.r.setRGBLedColor(Color.ORANGE);
 				
 				//can some times overwrite the next command
 //				ct.r.sendCommand(new SpinLeftCommand(100));
 				
 			}
 		} );
 		stopButton.addActionListener( new ActionListener() {
 			@Override
 			public void actionPerformed( ActionEvent e )
 			{
 				MacroObject m = new MacroObject();
 				//m.addCommand(command);
 				
 				
 				ct.r.sendCommand(new AbortMacroCommand());
 //				ct.stopThread();
 			}
 		} );
 */
 		
 	
 	/**
 	 * Set connect button state (also affects the disconnect button)
 	 * 
 	 * @param enabled True to enable, false otherwise
 	 */
 	
 	
 	
 }
 
 	/**
 	 * Handles the detection of new devices and listens on our robots for
 	 * responses and events
 	 */
