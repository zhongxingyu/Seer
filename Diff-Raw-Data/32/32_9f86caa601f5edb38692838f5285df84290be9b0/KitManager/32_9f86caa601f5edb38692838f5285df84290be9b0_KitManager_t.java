 package factory.client.kitManager;
 import factory.global.data.*; 
 import factory.global.network.*;
 import java.util.*;
 import javax.swing.*; 
 import java.awt.*; 
 import java.awt.event.*; 
 
 public class KitManager extends JFrame implements ActionListener, ItemListener, NetworkManager {
 
 	JPanel kitPanel; 
 	KitList kL; 
 	KitInfo kI; 
 	PartSelector pS; 
 	Kits newKit; 
 	TreeMap<Integer, Parts> currentList;
 	TreeMap<Integer, Parts> finalizedList; 
 	ArrayList<Kits> listOfKits; 
 	NetworkBridge nb1;
 	int globalCounter; 
 	public KitManager(){
 		nb1 = new NetworkBridge(this, "aludra.usc.edu", 8465, 1);
 		kL = new KitList(); 
 		kI = new KitInfo(); 
 		pS = new PartSelector();
 		kitPanel = new JPanel();
 		kitPanel.setLayout(new BoxLayout(kitPanel, BoxLayout.X_AXIS));
 		kitPanel.add(kL); 
 		kitPanel.add(kI); 
 		kitPanel.add(pS);
 
 		 
 
 		kL.getCreateKit().setActionCommand("Create"); 
 		kL.getCreateKit().addActionListener(this); 
 
 
 		pS.getBoxOfPart1().addItemListener(this); 
 		pS.getBoxOfPart2().addItemListener(this); 
 		pS.getBoxOfPart3().addItemListener(this); 
 		pS.getBoxOfPart4().addItemListener(this); 
 		pS.getBoxOfPart5().addItemListener(this); 
 		pS.getBoxOfPart6().addItemListener(this); 
 		pS.getBoxOfPart7().addItemListener(this); 
 		pS.getBoxOfPart8().addItemListener(this); 
 
 
 		currentList = new TreeMap<Integer, Parts>();
 
 		updateBoxes(); 
 
 
 
 		
 		finalizedList = new TreeMap<Integer, Parts>();
 		listOfKits = new ArrayList<Kits>(); 
 		globalCounter = 0; 
 		add(kitPanel);
 		
 		
 	}
 	
 	
 	public static void main(String[] args) {
 		KitManager thisKitManager = new KitManager(); 
 		thisKitManager.setTitle("Kit Manager");
 	    thisKitManager.setSize(750, 350);	
 	    thisKitManager.setLocationRelativeTo(null);
 	    thisKitManager.setVisible(true); 
 	    thisKitManager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
 
 	}
 	
 	
 	public void actionPerformed(ActionEvent ae){
 		if("Create".equals(ae.getActionCommand())){
 			String kitIDString = kI.getKitIDNumber().getText();
 			int kitIDNumber = 0; 
 			try{
 				kitIDNumber = Integer.parseInt(kitIDString);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				kI.getKitIDNumber().setText("ID");
 			}
 			newKit = new Kits(kI.getKitNameText().getText(), finalizedList, kI.getKitDescription().getText(), kitIDNumber); 
 			listOfKits.add(newKit); 
 			kL.getKitLister().add(new JLabel(newKit.getName()));
 			System.out.println("The Kit, " + listOfKits.get(0).getName() + " was just created. Congratulations."); 
 			kI.getKitNameText().setText("Name");
 			kI.getKitDescription().setText("Brief Description of Kit");
 			kI.getKitIDNumber().setText("ID");
 
 			nb1.sendKitData(listOfKits);
 		}
 	}
 
 	public void itemStateChanged(ItemEvent ie){
 		if(ie.getStateChange() == ItemEvent.SELECTED){
 			
 			 for(int searchForRightPart=0; searchForRightPart<currentList.size(); searchForRightPart++){
 			 	if(pS.getBoxOfPart1().getSelectedItem().toString()==currentList.get(searchForRightPart).getName()){
 			 		 Parts newPart = new Parts(currentList.get(searchForRightPart).getPartNumber(), currentList.get(searchForRightPart).getName(), currentList.get(searchForRightPart).getDesc(), currentList.get(searchForRightPart).getImageIndex());
 			 		 finalizedList.put(0, newPart);			 		  			 		 
 			 	}
 			 }
 
 
 
 			for(int searchForRightPart=0; searchForRightPart<currentList.size(); searchForRightPart++){
 			 	if(pS.getBoxOfPart2().getSelectedItem().toString()==currentList.get(searchForRightPart).getName()){
 			 		 Parts newPart = new Parts(currentList.get(searchForRightPart).getPartNumber(), currentList.get(searchForRightPart).getName(), currentList.get(searchForRightPart).getDesc(), currentList.get(searchForRightPart).getImageIndex());
 			 		 finalizedList.put(1, newPart); 	
 
 			 	}
 			 }
 
 
 			 for(int searchForRightPart=0; searchForRightPart<currentList.size(); searchForRightPart++){
 			 	if(pS.getBoxOfPart3().getSelectedItem().toString()==currentList.get(searchForRightPart).getName()){
 			 		 Parts newPart = new Parts(currentList.get(searchForRightPart).getPartNumber(), currentList.get(searchForRightPart).getName(), currentList.get(searchForRightPart).getDesc(), currentList.get(searchForRightPart).getImageIndex());
 			 		 finalizedList.put(2, newPart); 			 		 
 			 	}
 			 }
 
 
 			 for(int searchForRightPart=0; searchForRightPart<currentList.size(); searchForRightPart++){
 			 	if(pS.getBoxOfPart4().getSelectedItem().toString()==currentList.get(searchForRightPart).getName()){
 			 		 Parts newPart = new Parts(currentList.get(searchForRightPart).getPartNumber(), currentList.get(searchForRightPart).getName(), currentList.get(searchForRightPart).getDesc(), currentList.get(searchForRightPart).getImageIndex());
 			 		 finalizedList.put(3, newPart); 			 		 
 			 	}
 			 }
 
 			 for(int searchForRightPart=0; searchForRightPart<currentList.size(); searchForRightPart++){
 			 	if(pS.getBoxOfPart5().getSelectedItem().toString()==currentList.get(searchForRightPart).getName()){
 			 		 Parts newPart = new Parts(currentList.get(searchForRightPart).getPartNumber(), currentList.get(searchForRightPart).getName(), currentList.get(searchForRightPart).getDesc(), currentList.get(searchForRightPart).getImageIndex());
 			 		 finalizedList.put(4, newPart); 			 		 
 			 	}
 			 }
 
 			  for(int searchForRightPart=0; searchForRightPart<currentList.size(); searchForRightPart++){
 			 	if(pS.getBoxOfPart6().getSelectedItem().toString()==currentList.get(searchForRightPart).getName()){
 			 		 Parts newPart = new Parts(currentList.get(searchForRightPart).getPartNumber(), currentList.get(searchForRightPart).getName(), currentList.get(searchForRightPart).getDesc(), currentList.get(searchForRightPart).getImageIndex());
 			 		 finalizedList.put(5, newPart); 			 		 
 			 	}
 			 }
 
 			 for(int searchForRightPart=0; searchForRightPart<currentList.size(); searchForRightPart++){
 			 	if(pS.getBoxOfPart7().getSelectedItem().toString()==currentList.get(searchForRightPart).getName()){
 			 		 Parts newPart = new Parts(currentList.get(searchForRightPart).getPartNumber(), currentList.get(searchForRightPart).getName(), currentList.get(searchForRightPart).getDesc(), currentList.get(searchForRightPart).getImageIndex());
 			 		 finalizedList.put(6, newPart); 			 		 
 			 	}
 			 }
 
 			 for(int searchForRightPart=0; searchForRightPart<currentList.size(); searchForRightPart++){
 			 	if(pS.getBoxOfPart8().getSelectedItem().toString()==currentList.get(searchForRightPart).getName()){
 			 		 Parts newPart = new Parts(currentList.get(searchForRightPart).getPartNumber(), currentList.get(searchForRightPart).getName(), currentList.get(searchForRightPart).getDesc(), currentList.get(searchForRightPart).getImageIndex());
 			 		 finalizedList.put(7, newPart); 			 		 
 			 	}
 			 }
 
 		}
 
 	}
 		
 		public void updateBoxes(){ 
 			//COMBO BOX 1
 
 			for(int i = 0; i<currentList.size(); i++){
 				String removeName = currentList.get(i).getName(); 
 				pS.getPartNamesBox1().remove(removeName); 
 			}
 			for(int i = 0; i<currentList.size(); i++){
 				String nextName = currentList.get(i).getName(); 			
 				pS.getPartNamesBox1().add(nextName); //adds names to arraylist			
 			}
 			pS.getBoxOfPart1().setEnabled(false); 			
 			pS.getBoxOfPart1().removeAllItems();
 			for(int i = 0; i<pS.getPartNamesBox1().size(); i++){
 
 				pS.getBoxOfPart1().addItem(pS.getPartNamesBox1().get(i));
 			}
 				pS.getBoxOfPart1().setEnabled(true); 
 				pS.getPartsPanel().revalidate(); 
 			//COMBO BOX 2
 			for(int i = 0; i<currentList.size(); i++){
 				String removeName = currentList.get(i).getName(); 
 				pS.getPartNamesBox2().remove(removeName); 
 			}
 			for(int i = 0; i<currentList.size(); i++){
 				String nextName = currentList.get(i).getName(); 			
 				pS.getPartNamesBox2().add(nextName); //adds names to arraylist			
 			}
 			pS.getBoxOfPart2().setEnabled(false); 
 			pS.getBoxOfPart2().removeAllItems();
 			for(int i = 0; i<pS.getPartNamesBox1().size(); i++){
 				pS.getBoxOfPart2().addItem(pS.getPartNamesBox2().get(i));
 			}
 			pS.getBoxOfPart2().setEnabled(true); 
 			pS.getPartsPanel().revalidate();
 			//COMBO BOX 3
 			for(int i = 0; i<currentList.size(); i++){
 				String removeName = currentList.get(i).getName(); 
 				pS.getPartNamesBox3().remove(removeName); 
 			}
 			for(int i = 0; i<currentList.size(); i++){
 				String nextName = currentList.get(i).getName(); 			
 				pS.getPartNamesBox3().add(nextName); //adds names to arraylist			
 			}
 			pS.getBoxOfPart3().setEnabled(false); 
 			pS.getBoxOfPart3().removeAllItems();
 			for(int i = 0; i<pS.getPartNamesBox3().size(); i++){
 				pS.getBoxOfPart3().addItem(pS.getPartNamesBox3().get(i));
 			}
 			pS.getBoxOfPart3().setEnabled(true); 
 			pS.getPartsPanel().revalidate();
 			//COMBO BOX 4
 			for(int i = 0; i<currentList.size(); i++){
 				String removeName = currentList.get(i).getName(); 
 				pS.getPartNamesBox4().remove(removeName); 
 			}
 			for(int i = 0; i<currentList.size(); i++){
 				String nextName = currentList.get(i).getName(); 			
 				pS.getPartNamesBox4().add(nextName); //adds names to arraylist			
 			}
 			pS.getBoxOfPart4().setEnabled(false); 
 			pS.getBoxOfPart4().removeAllItems();
 			for(int i = 0; i<pS.getPartNamesBox4().size(); i++){
 				pS.getBoxOfPart4().addItem(pS.getPartNamesBox4().get(i));
 			}
 			pS.getBoxOfPart4().setEnabled(true); 
 			pS.getPartsPanel().revalidate();
 			//COMBO BOX 5
 			for(int i = 0; i<currentList.size(); i++){
 				String removeName = currentList.get(i).getName(); 
 				pS.getPartNamesBox5().remove(removeName); 
 			}
 			for(int i = 0; i<currentList.size(); i++){
 				String nextName = currentList.get(i).getName(); 			
 				pS.getPartNamesBox5().add(nextName); //adds names to arraylist			
 			}
 			pS.getBoxOfPart5().setEnabled(false); 
 			pS.getBoxOfPart5().removeAllItems();
 			for(int i = 0; i<pS.getPartNamesBox5().size(); i++){
 				pS.getBoxOfPart5().addItem(pS.getPartNamesBox5().get(i));
 			}
 			pS.getBoxOfPart5().setEnabled(true); 
 			pS.getPartsPanel().revalidate();
 			//COMBO BOX 6
 			for(int i = 0; i<currentList.size(); i++){
 				String removeName = currentList.get(i).getName(); 
 				pS.getPartNamesBox6().remove(removeName); 
 			}
 			for(int i = 0; i<currentList.size(); i++){
 				String nextName = currentList.get(i).getName(); 			
 				pS.getPartNamesBox6().add(nextName); //adds names to arraylist			
 			}
 			pS.getBoxOfPart6().setEnabled(false); 
 			pS.getBoxOfPart6().removeAllItems();
 			for(int i = 0; i<pS.getPartNamesBox6().size(); i++){
 				pS.getBoxOfPart6().addItem(pS.getPartNamesBox6().get(i));
 			}
 			pS.getBoxOfPart6().setEnabled(true); 
 			pS.getPartsPanel().revalidate();
 			//COMBO BOX 7
 			for(int i = 0; i<currentList.size(); i++){
 				String removeName = currentList.get(i).getName(); 
 				pS.getPartNamesBox7().remove(removeName); 
 			}
 			for(int i = 0; i<currentList.size(); i++){
 				String nextName = currentList.get(i).getName(); 			
 				pS.getPartNamesBox7().add(nextName); //adds names to arraylist			
 			}
 			pS.getBoxOfPart7().setEnabled(false); 
 			pS.getBoxOfPart7().removeAllItems();
 			for(int i = 0; i<pS.getPartNamesBox7().size(); i++){
 				pS.getBoxOfPart7().addItem(pS.getPartNamesBox7().get(i));
 			}
 			pS.getBoxOfPart7().setEnabled(true); 
 			pS.getPartsPanel().revalidate();
 			//COMBO BOX 8
 			for(int i = 0; i<currentList.size(); i++){
 				String removeName = currentList.get(i).getName(); 
 				pS.getPartNamesBox8().remove(removeName); 
 			}
 			for(int i = 0; i<currentList.size(); i++){
 				String nextName = currentList.get(i).getName(); 			
 				pS.getPartNamesBox8().add(nextName); //adds names to arraylist			
 			}
 			pS.getBoxOfPart8().setEnabled(false); 
 			pS.getBoxOfPart8().removeAllItems();
 			for(int i = 0; i<pS.getPartNamesBox8().size(); i++){
 				pS.getBoxOfPart8().addItem(pS.getPartNamesBox8().get(i));
 			}
 			pS.getBoxOfPart8().setEnabled(true); 
 			pS.getPartsPanel().revalidate();
 		}
 
 		// -------------------------------------------------------------------------------------- //
 		// ----------------------------------- Network Manager ---------------------------------- //
 		// -------------------------------------------------------------------------------------- //
 		
 		// server specific
 		public void registerClientListener(NetworkBridge newBridge, int cID){}
 		public void syncFrame(){}
 		
 		
 		// client specific
 		public void mergeChanges(ArrayList<TreeMap<Integer, Boolean>> mapArray, ArrayList<TreeMap<Integer, FactoryObject>> dataArray){}
 		
 		public void updatePartData(TreeMap<Integer, Parts> partData){
 				currentList = partData;
 				for(Integer i : currentList.keySet()){
 						currentList.get(i).print();
 				}
				System.out.println();
				System.out.println();
 				updateBoxes(); // call combo box validation method
 				currentList.clear(); 
 				
 		}
 
 		public void updateKitData(ArrayList<Kits>kitData){};
 		
 		public void syncChanges(ArrayList<TreeMap<Integer,FactoryObject>> dataArray){}
 		
 		// global
 		public void closeNetworkBridge(int bridgeID){
 				nb1.close();
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ----------------------------------- End Network Manager ------------------------------ //
 		// -------------------------------------------------------------------------------------- //
 		
 	
 	
 
 }
