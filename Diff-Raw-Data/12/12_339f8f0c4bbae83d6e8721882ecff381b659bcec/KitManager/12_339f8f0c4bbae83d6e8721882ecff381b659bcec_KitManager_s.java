 //Ben Mayeux
 
 package factory.managers;
 
 import java.awt.BorderLayout;
 import java.util.ArrayList;
 
 import factory.client.Client;
 import factory.swing.KitManPanel;
 
 public class KitManager extends Client {
 	
 	private static final long serialVersionUID = -3161852324870948654L;
 	
 	KitManPanel buttons;
 
 	public KitManager() {
 		super(Client.Type.km, null, null);
 		
	    KitManPanel buttons = new KitManPanel(this);
 
 		setInterface();
 	}
 	public static void main(String[] args){
 		KitManager k = new KitManager();
 	}
 
 	public void sendMessage(String kitname, int setting, String message){
 		String cmd = new String("");
 		if (message == "power"){
 			cmd =  "km kma set kitcontent #kitname #itemnumber #itemname"; //hi
 			output.println(cmd);
 		}
 		
 	}
 	
 	public void setInterface() {
 		UI = buttons;
 		
 		add(UI, BorderLayout.LINE_END);
 		pack();
 		setVisible(true);
 	}
 	
 	
 	public void doCommand(ArrayList<String> pCmd) {
 		
 	}
 }
