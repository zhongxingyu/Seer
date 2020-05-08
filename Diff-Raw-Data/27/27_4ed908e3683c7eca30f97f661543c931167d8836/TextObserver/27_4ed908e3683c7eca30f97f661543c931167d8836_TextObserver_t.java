 package rocketview;
 
 import cansocket.*;
 
 import java.awt.Font;
 import java.util.*;
 import javax.swing.*;
 
 /*----------------------------------------------------------------
  * Prints all message to the message text box
  */
 class TextObserver extends JTextArea implements Observer
 {
 
     public TextObserver() {
 
 	// construct a JTextArea
 	super( 30, 40 ); // row, column
 	this.setLineWrap( true );
 	this.setFont( new Font( "Monospaced", Font.PLAIN, 10 ));
     }
 
     public void update(Observable o, Object arg)
     {
 	if (!(arg instanceof CanMessage))
 		return;
 			
 	CanMessage msg = (CanMessage) arg;
 
 	// filter out all id's that are handled elsewhere
 	switch(msg.getId11())
 	{
 		case CanBusIDs.FC_REPORT_STATE >> 5:
		case CanBusIDs.FC_REPORT_NODE_STATUS >> 5:
 		case CanBusIDs.FC_REPORT_LINK_QUALITY >> 5:
 		case CanBusIDs.FC_GPS_NAVSOL >> 5:
 		case CanBusIDs.FC_GPS_LATLON >> 5:
 		case CanBusIDs.FC_GPS_HEIGHT >> 5:
 		case CanBusIDs.FC_GPS_TIME >> 5:
 		case CanBusIDs.FC_GPS_SATS_USED >> 5:
 		case CanBusIDs.FC_GPS_SATS_VIS >> 5:
 		case CanBusIDs.IMU_ACCEL_DATA >> 5:
 		case CanBusIDs.IMU_GYRO_DATA >> 5:
 		case CanBusIDs.PRESS_REPORT_DATA >> 5:
 		case CanBusIDs.TEMP_REPORT_DATA >> 5:
 		case CanBusIDs.APS_DATA_VOLTS >> 5:
 		case CanBusIDs.APS_DATA_AMPS >> 5:
 		case CanBusIDs.APS_DATA_CHARGE >> 5:
 			return;
 	}
 
 	append( msg.toString() );
 	append( "\n" );
 	//Try to keep the scrollpane looking at the tail of the log
 	JScrollPane scrollpane = (JScrollPane) getParent().getParent();
 	final JScrollBar vertBar = scrollpane.getVerticalScrollBar();
 	SwingUtilities.invokeLater(new Runnable() {
 	  public void run() {
 	    if (! vertBar.getValueIsAdjusting()) 
 	      vertBar.setValue(vertBar.getMaximum());
 	  }
 	});
     }
 }
