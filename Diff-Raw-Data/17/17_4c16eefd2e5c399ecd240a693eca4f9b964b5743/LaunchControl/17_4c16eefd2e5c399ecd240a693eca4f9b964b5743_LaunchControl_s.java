 package launchcontrol;
 
 import cansocket.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.text.*;
 import java.util.*;
 import javax.swing.*;
 
 public class LaunchControl extends JFrame
 	implements ScheduleListener, ActionListener
 {
 	// Class constants
 	protected final static String startMsg = "Start Countdown";
 	protected final static String stopMsg = "ABORT";
 	protected final static String stoppedMsg = "Countdown stopped";
 
 	// Member variables, presumably used by multiple methods
 	protected final DecimalFormat fmt = new DecimalFormat("T+0.0;T-0.0");
	protected final Dimension windowSize = new Dimension((int)(300 * 1.61803399), 300);
 	protected JButton countdownButton = new JButton();
 	protected java.util.Timer powerSequence = null; // power on or off fc
 	
 	protected JLabel clock = new JLabel();
 	protected JLabel statusLabel = new JLabel();
 	protected String startSound = Config.getString("startSound");
 	protected String abortSound = Config.getString("abortSound");
 
 	protected CanSocket rocketSocket; // rocket communication socket
 	protected TCPCanSocket towerSocket;
 
 
 
 	/** A task, which when run, will set the shore power to
 	 * a desired state. 
 	 * This can be used by any code that needs to schedule
 	 * the shore power to be set to a certain state at a given time */
 	protected class ShorePowerTask extends TimerTask {
 		private boolean power;
 		private CanMessage powerOn;
 		private CanMessage powerOff;
 		private CanMessage requestMessage;
 
 		public ShorePowerTask (boolean power_state) {
 			power = power_state;
 			short id = CanBusIDs.LTR_SET_SPOWER;
 			byte onBody[] = { 1 };
 			byte offBody[] = { 0 };
 			byte[] blank = new byte[8];
 			powerOn = new CanMessage(id, 0, onBody );
 			powerOff = new CanMessage(id, 0, offBody);
 			requestMessage = new CanMessage(CanBusIDs.LTR_GET_SPOWER, 0, blank);
 		}
 
 		public void run() {
 			CanMessage powerMessage;
 			if (power)  // set shore power on
 				powerMessage = powerOn;
 			else  // set shore power off
 				powerMessage = powerOff;
 				
 			if (towerSocket == null) {
 				System.out.println(
 					"can't send power command (towerSocket null); connected?");
 			} else {
 				try {
 					if (power) {
 						System.out.println("LC: ShorePowerTask->powerOn");
 					} else {
 						System.out.println("LC: ShorePowerTask->powerOff");
 					}
 
 					towerSocket.write(powerMessage);
 					towerSocket.write(requestMessage);
 					towerSocket.flush();
 				} catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 
 	protected final Scheduler sched = new Scheduler();
 
 	private LaunchControl() throws IOException
 	{
 
 		super("LaunchControl");
 		//possible TODO: use gridlayout (or gridbaglayout) instead
 		//of using boxes of boxes. The advantage of a gridlayout is
 		// all components could be same size, and spaces can be inserted
 		// easily and uniformly.
 		
 		rocketSocket = new UDPCanSocket(Config.getString("rocket.host"), Config.getInt("rocket.port", UDPCanSocket.PORT_SEND));
 		
 		JButton preFlightCheckButton = new JButton("Preflight Check");
 		JButton armButton = new JButton("Arm Rocket");
 		JButton abortButton = new JButton(stopMsg);
 		JButton fcPowerOnButton = new JButton("FC On");
 		JButton powerDownButton = new JButton("Powerdown FC");
 		JButton fcPowerOffButton = new JButton("FC Off");
 	
 
 		Box layout_box;
 		Dimension x_spacer_dim = new Dimension(5,0);
 		Dimension y_spacer_dim = new Dimension(0,10);
 		Component x_spacer = Box.createRigidArea(x_spacer_dim);
 		Component y_spacer = Box.createRigidArea(y_spacer_dim);
 		Container content = getContentPane();
 		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
 
 		// State monitoring box
 		  // also pass the rocket socket to a thread for listening
 		content.add(new RocketPanel(rocketSocket, this));
 		
 		layout_box = new Box(BoxLayout.X_AXIS);
 		layout_box.add(Box.createRigidArea(x_spacer_dim));
 		layout_box.add(clock);
 		layout_box.add(Box.createRigidArea(x_spacer_dim));
 		Container statusBar = new JPanel();
 		statusBar.setLayout(new BorderLayout());
 		statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
 		statusBar.add(statusLabel, BorderLayout.CENTER);
 		layout_box.add(statusBar);
 		layout_box.add(Box.createRigidArea(x_spacer_dim));
 		content.add(layout_box);
 		content.add(Box.createRigidArea(y_spacer_dim));
 
 
 		// Sequence buttons
 		layout_box = new Box(BoxLayout.X_AXIS);
 		Container countdown_pane = new JPanel();
 		countdown_pane.setLayout(new GridLayout(0,1));
 		countdown_pane.add(preFlightCheckButton);
 		preFlightCheckButton.setActionCommand("preflight");
 		preFlightCheckButton.addActionListener(this);
 
 		countdown_pane.add(armButton);
 		armButton.setActionCommand("arm");
 		armButton.addActionListener(this);
 
 		countdown_pane.add(countdownButton);
 		countdownButton.addActionListener(this);
 
 		abortButton.setPreferredSize(countdown_pane.getPreferredSize());
 		abortButton.setBackground(Color.red);
 		abortButton.setMaximumSize(countdown_pane.getPreferredSize());
 		Dimension countdown_size = countdown_pane.getPreferredSize();
 		
 		layout_box.add(Box.createRigidArea(x_spacer_dim));
 		layout_box.add(countdown_pane);
 		layout_box.add(Box.createRigidArea(x_spacer_dim));
 		layout_box.add(abortButton);
 		layout_box.add(Box.createRigidArea(x_spacer_dim));
 		abortButton.setActionCommand("abort");
 		abortButton.addActionListener(this);
 
 		content.add(layout_box); 
 		content.add(Box.createRigidArea(y_spacer_dim));
 
 	
 
 		// Power buttons 
 		layout_box = new Box(BoxLayout.X_AXIS);
 		layout_box.add(Box.createRigidArea(x_spacer_dim));
 		Container fcpower_pane = new JPanel();
 		fcpower_pane.setLayout(new GridLayout(0, 1));
 
 		fcpower_pane.add(fcPowerOnButton);
 		fcPowerOnButton.setActionCommand("fc_on");
 		fcPowerOnButton.addActionListener(this);
 
 		fcpower_pane.add(powerDownButton);
 		powerDownButton.setActionCommand("powerdown");
 		powerDownButton.addActionListener(this);
 
 		fcpower_pane.add(fcPowerOffButton);
 		fcPowerOffButton.setActionCommand("fc_off");
 		fcPowerOffButton.addActionListener(this);
 		
 		try {
 			towerSocket = new TCPCanSocket(Config.getString("tower.host"), 
 					Config.getInt("tower.port", TCPCanSocket.DEFAULT_PORT));
 			Scheduler.addSchedulableAction("tower", new SocketAction(towerSocket));
 		} catch(ConnectException e) {
 			e.printStackTrace();
 		}
 	
 		ShorePower shorePowerState = new ShorePower(towerSocket, "Shore");
 		layout_box.add(fcpower_pane);
 		layout_box.add(Box.createHorizontalGlue());
 		layout_box.add(Box.createRigidArea(new Dimension(30, 0)));
 		layout_box.add(shorePowerState);
 		layout_box.add(Box.createRigidArea(new Dimension(30, 0)));
 
 		content.add(layout_box);
 		content.add(Box.createRigidArea(y_spacer_dim));
 
 
 		Scheduler.addSchedulableAction("rocket", new SocketAction(rocketSocket));
 		ended(); // reset the button and label
 		sched.addScheduleListener(this, 100);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		pack();
 
		//System.out.println("setting window size to: " + windowSize);
 		setSize(windowSize);
 
 		show();
 	}
 
 
 
 	public void actionPerformed(ActionEvent event)
 	{
 		try {
 			if (event.getActionCommand().equals("preflight"))
 			{
 				byte[] data = { CanBusIDs.PreflightCheckState };
 				rocketSocket.write(new CanMessage(CanBusIDs.FC_REQUEST_STATE, 0, data));
 			}
 			else if (event.getActionCommand().equals("arm"))
 			{
 				byte[] data = { CanBusIDs.ArmingState };
 				rocketSocket.write(new CanMessage(CanBusIDs.FC_REQUEST_STATE, 0, data));
 			}
 			else if (event.getActionCommand().equals("powerdown"))
 			{
 				if(JOptionPane.showConfirmDialog(this,
 					"Are you sure you want to shutdown the flight computer?", 
 					"Proceed?",
 					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) 
 					return; // user wasn't sure.
 				byte[] data = { CanBusIDs.PowerDownState };
 				rocketSocket.write(new CanMessage(CanBusIDs.FC_REQUEST_STATE, 0, data));
 			} 
 			else if(event.getActionCommand().equals("fc_on"))
 			{
 				fcPower(true);
 			} else if (event.getActionCommand().equals("fc_off"))
 			{
 				if(JOptionPane.showConfirmDialog(this,
 					"Are you sure you want to cut power to flight computer?", 
 					"Proceed?",
 					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) 
 
 					return; // user wasn't sure.
 
 				fcPower(false);
 			} else if(event.getActionCommand().equals("start"))
 			{
 				if(JOptionPane.showConfirmDialog(this,
 					"Are you sure you want to start the countdown?", "Proceed?",
 					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
 
 					sched.startCountdown();
 			}
 			else if(event.getActionCommand().equals("abort"))
 			{
 				System.out.println("got abort request");
 				// XXX: if countdown is near T-0, which s/b done first?
 				sched.abortCountdown(); // tell countdown sequencer to abort 
 				// tell rocket's sequencer to abort
 				byte[] data = new byte[8];
 				rocketSocket.write(
 						new CanMessage(CanBusIDs.FC_ABORT_LAUNCH, 0, data));
 			}
 
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/** Power on or off the FC by toggling shore power. 
 	 * a new sequence of on/off toggles of the shore power is started. 
 	 * The scheduled sequence should be over in ten seconds.
 	 * @param fcPower the new desired power state of the 
 	 * FC (true=on, false=off) 
 	 */
 	protected void fcPower(boolean fcPower) {
 		/* A two second delay (2000 millisecs) between shore on/off msgs 
 		 * which are sent in sequence while trying to turn on or off the FC. */
 		int delay = 2000;
 
 		// cancel any prev. sequence
 		if (powerSequence != null) {
 			powerSequence.cancel();
 		}
 
 		// sched 5 things for new sequence
		powerSequence = new java.util.Timer(true /* daemon */);
 		for (int i = 1; i <= 5; ++i)
 		{
 			boolean power;
 			if (i % 2 != 0) // i is odd
 				power= fcPower ; // power == fcPower at 1, 3, 5 secs
 			else  // i is even
 				power= !fcPower; //on even secs, shore is set to !fcPower
 			powerSequence.schedule(
					new ShorePowerTask(power), i * delay);
 		}
 	}
 
 
 
 	public void started()
 	{
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run()
 			{
 				countdownButton.setText(stopMsg);
 				countdownButton.setActionCommand("abort");
 			}
 		});
 		setStatus("Countdown started");
 		try {
 			SoundAction.playSound(startSound);
 		} catch(Exception e) {
 			// ignore
 		}
 	}
 
 	public void disableAbort()
 	{
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run()
 			{
 				countdownButton.setEnabled(false);
 			}
 		});
 	}
 
 	public void aborted()
 	{
 		disableAbort();
 		setStatus("Countdown aborted: cleaning up");
 		try {
 			SoundAction.playSound(abortSound);
 		} catch(Exception e) {
 			// ignore
 		}
 	}
 
 	public void ended()
 	{
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run()
 			{
 				countdownButton.setText(startMsg);
 				countdownButton.setActionCommand("start");
 				countdownButton.setEnabled(true);
 				clock.setText("");
 			}
 		});
 		setStatus(stoppedMsg);
 	}
 
 	public void time(final long millis)
 	{
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run()
 			{
 				clock.setText(fmt.format((float)millis / 1000.0));
 			}
 		});
 	}
 
 	protected void setStatus(final String msg)
 	{
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run()
 			{
 				statusLabel.setText(msg);
 			}
 		});
 	}
 
 	public static void main(String args[]) throws IOException
 	{
 		new LaunchControl();
 	}
 }
