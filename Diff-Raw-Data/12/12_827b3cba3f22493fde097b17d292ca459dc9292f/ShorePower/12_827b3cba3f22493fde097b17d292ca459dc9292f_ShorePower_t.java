 package launchcontrol;
 
 import cansocket.*;
 import widgets.*;
 
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 	
 // possible TODO: shorePowerState checkbutton can have image icon
 //  displaying a power plug if we're using shore power, 
 //  or a battery if we're using rocket battery (quite like a laptop
 //  power display). Actually we might just put that in RocketState widget.
 
 /** The ShorePower widget is used to set (and view) set of shore power.
  * For now it is just a check box, but it may eventually be either a
  * checkbox or an icon depending on if you want to control or just
  * view the state, respectively.
  */
 public class ShorePower extends JCheckBox implements ItemListener
 {
 	
 	protected CanSocket sock;
 	protected boolean powerState; // true = on, false = off.
 	protected boolean ignoreEvents; // true = disable ShorePower's event handler
 
 	/** Create a ShorePower widget.  
 	 * @param socket the socket to read from to get information
 	 * from the launch tower.
 	 */
 	public ShorePower(CanSocket socket)
 	{
 		this(socket, null);
 	}
 
 	public ShorePower(CanSocket socket, String title)
 	{
 		sock = socket;
 		if (title != null)
 			setText(title);
 		new ReaderThread().start();
		/* Request the power at startup. */
		try {
			CanMessage requestMessage = new CanMessage(CanBusIDs.LTR_GET_SPOWER,
					0, new byte[8]);
			sock.write(requestMessage);
			sock.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
 	}
 
 
 	public void itemStateChanged(ItemEvent event) {
 		if (!ignoreEvents) {
 			System.out.println("ShorePower widget acting on item event");
 			short id = CanBusIDs.LTR_SET_SPOWER;
 			int timestamp = 0;
 			byte body[] = new byte[8];
 			if (event.getStateChange() == ItemEvent.DESELECTED) {
 				body[0] = 0;
 				System.out.println("\tshore power DESELECTED, telling tower");
 			} else {
 				body[0] = 1;
 				System.out.println("\tshore power SELECTED, telling tower");
 			}
 			try {
 				CanMessage myMessage = new CanMessage(id, timestamp, body);
 				sock.write(myMessage);
 				CanMessage requestMessage = new CanMessage(CanBusIDs.LTR_GET_SPOWER,
 						0, new byte[8]);
 				sock.write(requestMessage);
 				sock.flush();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/** possibly update the widget based on the message read from tower. */
 	public void update(CanMessage msg) {
 		// if msg is a LTR_REPORT_SPOWER (XXX: ensure its not actually LTR_SPOWER)
 		// then set power to first byte.
 		if (msg.getId() == CanBusIDs.LTR_REPORT_SPOWER) {
			if (msg.getData8(0) == 1) 
 				powerState = true;
 			else 
 				powerState = false;
 
 			// change the state of this widget, ensuring our event handler isn't
 			// called, because this isn't a user generated change of state.
 			ignoreEvents = true; 
 			setSelected(powerState);
 			ignoreEvents = false;
 		}
 	}
 			
 		
 
 	private class ReaderThread extends Thread
 	{
 		public void run()
 		{
 			try {
 				CanMessage m;
 				while ((m = sock.read()) != null)
 				{
 					update(m);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
