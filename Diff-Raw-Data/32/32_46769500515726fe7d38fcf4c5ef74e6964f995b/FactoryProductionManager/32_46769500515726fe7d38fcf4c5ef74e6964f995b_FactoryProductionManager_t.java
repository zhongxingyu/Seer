 //Contributors: Ben Mayeux,Stephanie Reagle, Joey Huang, Tobias Lee
 //CS 200
 
// Last edited: 11/15/12 12:03 by Joey Huang
 package factory.managers;
 
 import java.awt.BorderLayout;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
import java.util.Queue;
 import javax.swing.JPanel;
 import factory.client.Client;
 import factory.graphics.FactoryProductionPanel;
 import factory.swing.FactoryProdManPanel;
 import factory.Part;
 import factory.KitConfig;
 
 public class FactoryProductionManager extends Client {
 	static final long serialVersionUID = -2074747328301562732L;
 	ArrayList<KitConfig> kitConfigList;
 	ArrayList<Part> partsList;
 	FactoryProdManPanel swingPanel;
 	
 		public FactoryProductionManager(JPanel buttons, FactoryProductionPanel animation) {
 			super(Client.Type.fpm, buttons, animation);
 			setInterface();
 			
 			swingPanel = (FactoryProdManPanel)buttons;
 			kitConfigList = new ArrayList<KitConfig>();
 			loadParts();
 		}
 		public static void main(String[] args){
 		    FactoryProdManPanel buttons = new FactoryProdManPanel();
 		    FactoryProductionPanel animation = new FactoryProductionPanel(null); //TODO does not currently work but will by 11/13 -->Tobi
 			FactoryProductionManager f = new FactoryProductionManager(buttons, animation);
 			buttons.setManager(f);
		//	buttons.addKit("empty test kit");
 		}
 
 		public void setInterface() {
 			add(graphics, BorderLayout.CENTER);
 			
 			add(UI, BorderLayout.LINE_END);
 			pack();
 			setVisible(true);
 		}
 
 		
 		public void sendDone(String process) { //sends message out from graphics (Tobi's Function)
 			//Process changes depending on previous command
 			sendCommand(process);
 		}
 		
 			@Override
 	public void doCommand(ArrayList<String> pCmd) {
 		int size = pCmd.size();
 		//parameters lay between i = 2 and i = size - 2
 		String action = pCmd.get(0);
 		String identifier = pCmd.get(1);
 		if(action == "cmd"){
 			/*
 			 * fa fpm cmd startFeeding " + feederSlot + " endcmd
 			 */
 			if (identifier.equals("startFeeding"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(3));
 				((FactoryProductionPanel) graphics).turnFeederOn(feederSlot);
 			}
 			else if (identifier.equals("stopFeeding"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(3));
 				((FactoryProductionPanel) graphics).turnFeederOff(feederSlot);
 			} 
 			else if (identifier.equals("purgeFeeder"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(3));
 				((FactoryProductionPanel) graphics).purgeFeeder(feederSlot);
 			}
 			else if (identifier.equals("switchLane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(3));
 				((FactoryProductionPanel) graphics).switchFeederLane(feederSlot);
 			}
 			else if (identifier.equals("addkitname")) {		// add new kit configuration to kit configuration list
 				KitConfig newKit = new KitConfig(pCmd.get(2));
 				int count = 3;
 				
 				while(!pCmd.get(count).equals("endcmd")) {
 					String partName = pCmd.get(3);
 					for (Part part:partsList) {
 						if (part.name.equals(partName))
 							newKit.listOfParts.add(part);
 							break;
 					}
 			
 					count++;
 				}
 				kitConfigList.add(newKit); 
 				swingPanel.addKit(newKit.kitName);
 			}
 			else if (identifier.equals("rmkitname")) {		// remove kit configuration from kit configuration list
 				
 			}
 			else if (identifier.equals("addpartname")) {	// add new part to parts list
 				Part part = new Part(pCmd.get(3),Integer.parseInt(pCmd.get(4)),pCmd.get(7),pCmd.get(5),Double.parseDouble(pCmd.get(6)));
 		// server message sequence		Part part = new Part(pCmd.get(3),Integer.parseInt(pCmd.get(4)),pCmd.get(5),Double.parseDouble(pCmd.get(6)),pCmd.get(7));
 				partsList.add(part);
 			}
 			else if (identifier.equals("rmpartname")) {		// remove part from parts list
 				
 			}
 		}
 		else if(action.equals("req")){
 			/*if(identifier.equals(request1))
 			 * do(request1);
 			 * else if(identifier.equals(request2))
 			 * do(request2);
 			 */
 		}
 		else if(action.equals("get")){
 			/*if(identifier.equals(get1))
 			 * do(get1);
 			 * else if(identifier.equals(get2))
 			 * do(get2);
 			 */
 		}
 		else if(action.equals("set")){
 			if (identifier.equals("kitcontent")) { 			// modify content of a kit
 
 			}
 			else if (identifier.equals("kitsproduced")) { // updates number of kits produced for schedule
 				
 			}
 		}
 		else if(action.equals("cnf")){
 			/*if(identifier.equals(confirm1))
 			 * do(confirm1);
 			 * else if(identifier.equals(confirm2))
 			 * do(confirm2);
 			 */
 		}
 	   else if(action.equals("err")){
 			String error;
 			error = new String();
 			for(int i = 1; i<this.parsedCommand.size(); i++)
 				error.concat(parsedCommand.get(i));
 			System.out.println(error);
 		
 			
 		}
 	
 	}
 			
 
 			public void loadParts(){
 				FileInputStream f;
 				ObjectInputStream o;
 				try{    // loads previously saved player data
 					f = new FileInputStream("InitialData/initialParts.ser");
 					o = new ObjectInputStream(f);
 					partsList = (ArrayList<Part>) o.readObject();
 					System.out.println("Good");
 					for (int i = 0; i < partsList.size();i++) {
 						System.out.println(partsList.get(i).name);
 					}
 				}catch(IOException e){
 					partsList = new ArrayList<Part>();
 				} catch(ClassNotFoundException c){
 					partsList = new ArrayList<Part>();
 				}
 			}
 			
 			
 
 }
