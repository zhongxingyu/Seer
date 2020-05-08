 package launchcontrol;
 
 import cansocket.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import javax.swing.*;
 
public class StatusPanel extends Box implements Observer, ActionListener {
     private static final String statusbits[] =
     { "ready", null, "strobe", "siren", null, null, "igniter", "arm", null, "power" };
 
     private Component btns[] = new Component[statusbits.length];
 
     private CanListener tower = null;
     private CanListener rocket = null;
     // keeping track of both tower and rocket so that I know which messages
     // can from which listener. Ie, ignore rocket messages from tower and
     // vice versa.
 
     
     public StatusPanel(CanListener aTowerListener,
                        CanListener aRocketListener) {
         // TODO: Add the tower CanAction to this? So that the LC operator can
         // manipulate tower state?
 
         super(BoxLayout.X_AXIS);
 
         tower  = aTowerListener;
         rocket = aRocketListener;
 
         JLabel ready = new JLabel("not " + statusbits[0]);
         add(ready);
         btns[0] = ready;
 
         for(int i = /*skip RR*/1; i < statusbits.length; ++i)
             {
                 if(statusbits[i] == null)
                     continue;
                 JToggleButton b = new JToggleButton(statusbits[i]);
                 b.setActionCommand(statusbits[i]);
                 b.addActionListener(this); // TODO: fix this.
                 add(b);
                 btns[i] = b;
             }
         tower.addObserver(this);
         rocket.addObserver(this);
 
     }
 
     public void update(Observable observed, Object aCanMsg) {
         
         CanMessage canMsg = (CanMessage) aCanMsg;
         // generates runtime exception if aCanMsg isn't correct object type.
 
 
         if (observed == rocket) {
             // update rocket stuff - not sure what we're want here.
         } else if (observed == tower) {
             // update tower stuff - namely current state of the four relays.
         }
 
     }
 
     public void actionPerformed(ActionEvent event) {
 	AbstractButton source = (AbstractButton)event.getSource();
 	//dispatch(event.getActionCommand() + (source.isSelected() ? "1" : "0"));
     }
 
     public void towerStatus(final String status) {
 	SwingUtilities.invokeLater(new Runnable() {
 		public void run()
 		{
 		    for(int i = 0; i < status.length(); ++i)
 			{
 			    if(btns[i] == null)
 				continue;
 			    if(btns[i] instanceof AbstractButton)
 				((AbstractButton)btns[i])
 				    .setSelected(status.charAt(i) 
 						 == '1');
 			    else if(btns[i] instanceof JLabel)
 				((JLabel)btns[i]).setText
 				    ((status.charAt(i) == '1' ? "is " 
 				      : "not ") + statusbits[i]);
 			}
 		}
 	    });
     }
 }
