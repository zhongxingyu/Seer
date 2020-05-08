 package rocketview;
 
 import cansocket.*;
 
 import java.util.*;
 import java.text.*;
 import javax.swing.*;
 
 /*----------------------------------------------------------------
  * -- not implemented --
  * Handles message id PowerID
  * Updates the APS box with status
  *
  *   display: APS bus: xx.xxV x.xxA  batt: xx.xxxAHr
  */
 class APSObserver extends JLabel implements Observer
 {
 	protected final DecimalFormat fmt = new DecimalFormat("0.000");
 
     protected double voltage = -1;
     protected double current = -1;
     protected double charge = -1;
 
     public APSObserver() {
 	setText();
 	this.setText( "APS bus: xx.xxV x.xxA  batt: xx.xxxAHr" );
     }
 
     protected void setText()
     {
 	StringBuffer b = new StringBuffer("APS bus: ");
 	b.append(fmt.format(voltage)).append(" V, ");
 	b.append(fmt.format(current)).append(" A, ");
 	b.append(fmt.format(charge)).append(" Ah");
 	super.setText(b.toString());
     }
 
     public void update(Observable o, Object arg)
     {
	int counts;
 	if (!(arg instanceof CanMessage))
 	    return;
 			
 	// filter on id
 	CanMessage msg = (CanMessage) arg;
 	switch(msg.getId())
 	{
 	    case CanBusIDs.APS_DATA_VOLTS:
		counts = msg.getData16(0);
		counts &= 0xffff;   // unsigned
		voltage = counts * 5 / 1024 * 0.14815;
 		break;
 	    case CanBusIDs.APS_DATA_AMPS:
		current = 768.05 / msg.getData32(0);
 		break;
 	    case CanBusIDs.APS_DATA_CHARGE:
 			// 853.4 * 10^-6 Ah / count
 		charge = 853.4e-6 * msg.getData16(0);
 		break;
 	    default:
 		return;
 	}
 	setText();
     }
 }
