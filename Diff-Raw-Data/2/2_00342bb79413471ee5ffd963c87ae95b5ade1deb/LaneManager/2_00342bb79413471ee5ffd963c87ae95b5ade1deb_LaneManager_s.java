 //Ben Mayeux and Stephanie Reagle
 //CS 200
 
 package factory.managers;
 
 import java.awt.BorderLayout;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 import factory.graphics.*;
 import factory.client.Client;
 
 public class LaneManager extends Client {
 
 	private static final long serialVersionUID = 6767006307991802656L;
 	
 	LanePanel lp;
 	
 	public LaneManager() {
 		
		super(Client.Type.LM, null, null); 
 		lp = new LanePanel(this);
 		graphics = lp;
 		setInterface();
 	}
 	
 	public static void main(String[] args){
 		//LaneManPanel buttons = new LaneManPanel(); //to be implemented in V.2
 		//JPanel animation = new FactoryProductionPanel(null);
 		LaneManager l = new LaneManager(); //to be implemented in V.2
 		//buttons.setManager(l);  //to be implemented in V.2
 		
 	}
 	
 	public void sendMessage(int lane, int setting, String message){
 		String set = new String("");
 		/*
 		if (message == "power"){
 			set = "lm lma lanepowertoggle "+ lane;
 		}
 		else if (message == "red"){
 			set = "lm lma set lanevibration "+ lane + " " + setting;
 		}
 		else if (message == "yellow"){
 			set = "lm lma set lanevibration "+ lane + " " + setting;
 		}
 		else if (message == "green"){
 			set = "lm lma set lanevibration "+ lane + " " + setting;
 		}*/ // to be implemented in V.2
 		sendCommand(set); 
 	}
 	
 	public void setInterface() {
 
 		add(graphics, BorderLayout.CENTER);
 		
 		pack();
 		
 		//add(UI, BorderLayout.LINE_END);  //to be implemented in V.2
 		setVisible(true);
 	}
 
 	public void doCommand(ArrayList<String> pCmd) {
 		int size = pCmd.size();
 		//parameters lay between i = 2 and i = size - 2
 		String action = pCmd.get(0);
 		String identifier = pCmd.get(1);
 		if(action == "cmd"){
 			/*if(identifier == command1)
 			 * do(command1);
 			 * else if(identifier == command2)
 			 * do(command2);
 			 */
 		}
 		else if(action == "req"){
 			/*if(identifier == request1)
 			 * do(request1);
 			 * else if(identifier == request2)
 			 * do(request2);
 			 */
 		}
 		else if(action == "get"){
 			/*if(identifier == get1)
 			 * do(get1);
 			 * else if(identifier == get2)
 			 * do(get2);
 			 */
 		}
 		else if(action == "set"){
 			/*if(identifier == set1)
 			 * do(set1);
 			 * else if(identifier == set2)
 			 * do(set2);
 			 */
 		}
 		else if(action == "cnf"){
 			/*if(identifier == confirm1)
 			 * do(confirm1);
 			 * else if(identifier == confirm2)
 			 * do(confirm2);
 			 */
 		}
 	}
 }
