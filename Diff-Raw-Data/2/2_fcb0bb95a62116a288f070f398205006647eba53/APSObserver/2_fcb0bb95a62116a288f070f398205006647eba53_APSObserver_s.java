 package rocketview;
 
 import cansocket.*;
 import widgets.*;
 
 import java.text.*;
 import javax.swing.*;
 import javax.swing.border.*;
 
 class APSObserver extends JPanel implements CanObserver
 {
 	protected final DecimalFormat fmt = new DecimalFormat("0.000");
 
 	protected final NameDetailLabel busLabel = new NameDetailLabel("Battery");
 
 	protected String voltage = "-";
 	protected String current = "-";
 	protected String charge = "-";
 	
 	protected void add(CanDispatch dispatch, BooleanStateLabel label)
 	{
 		dispatch.add(label);
 		add(label);
 	}
 
 	public APSObserver(CanDispatch dispatch)
 	{
 		setLayout(new GridBoxLayout());
 
 		dispatch.add(this);
 
 		add(StateGrid.getLabel("APS"));
 		add("UMB_CONNECTOR", "Umbilical");
 		add(dispatch, new BooleanStateLabel("Shore power",  CanBusIDs.UMB_REPORT_SHORE_POWER));
 		add("UMB_ROCKETREADY", "Rocket Ready");
 		add(busLabel);
 		add(dispatch, new BooleanStateLabel("Charging",     CanBusIDs.PWR_REPORT_CHARGER));
 		add(dispatch, new BooleanStateLabel("S1 (FC)",      CanBusIDs.APS_REPORT_SWITCH_1));
 		add("APS_SWITCH_2", "S2 (CAN)");
 		add("APS_SWITCH_3", "S3 (ATV)");
 		add("APS_SWITCH_4", "S4 (WIFI)");
 		
 		add(StateGrid.getLabel("APS_DATA_VOLTS"));
 		add(StateGrid.getLabel("APS_DATA_AMPS"));
 		add(StateGrid.getLabel("APS_DATA_CHARGE"));
 		//add(StateGrid.getLabel("APS"));
 		
 		setText();
 	}
 
 	protected void add(String name, String description)
 	{
 		JLabel label = StateGrid.getLabel(name);
 		label.setText(description);
 		add(label);
 	}
 
 	protected void setText()
 	{
 		busLabel.setDetail(voltage + "V " + current + "A " + charge + "Ah");
 	}
 
     public void message(CanMessage msg)
     {
 	switch(msg.getId())
 	{
 	    case CanBusIDs.APS_DATA_VOLTS:
 		short counts = msg.getData16(0);
 		//counts &= 0xffff;   // unsigned.  What the heck?
		voltage = fmt.format(counts * (5 / 1024 / 0.14581));
 		break;
 	    case CanBusIDs.APS_DATA_AMPS:
 		current = fmt.format(768.05 / msg.getData32(0));
 		break;
 	    case CanBusIDs.APS_DATA_CHARGE:
 			// 853.4 * 10^-6 Ah / count
 		charge = fmt.format(853.4e-6 * msg.getData16(0));
 		break;
 	    default:
 		return;
 	}
 	setText();
     }
 }
