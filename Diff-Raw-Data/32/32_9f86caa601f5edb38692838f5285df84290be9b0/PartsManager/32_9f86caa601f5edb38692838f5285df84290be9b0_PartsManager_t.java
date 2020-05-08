 package factory.client.partManager;
 
 import java.util.*;
 import javax.swing.*; 
 import java.awt.*; 
 import java.awt.event.*; 
 import factory.global.network.*;
 import factory.global.data.*;
 
 public class PartsManager extends JFrame implements ActionListener, NetworkManager{
 	
 	JPanel partsGrid; 
 	FirstColumn fC; 
 	SecondColumn sC; 
 	ThirdColumn tC;
 	PartInfo pI; 
 	Parts newPart1, newPart2, newPart3, newPart4, newPart5, newPart6, newPart7, newPart8, newPart9, newPart10;
 	boolean part1Edit, part2Edit, part3Edit, part4Edit, part5Edit, part6Edit, part7Edit, part8Edit, part9Edit, part10Edit; 
 	boolean isPart1New, isPart2New, isPart3New, isPart4New, isPart5New, isPart6New, isPart7New, isPart8New, isPart9New, isPart10New; 
 	int listCounter; 
 	JButton newKit; 
 	TreeMap<Integer, Parts> listOfParts; 
 	JMenuBar optionMenuBar; JMenu optionMenu; JMenuItem resetOption, deletePartOption;
 	NetworkBridge nb1;
 	public PartsManager(){	
 		nb1 = new NetworkBridge(this, "aludra.usc.edu", 8465, 0);
 		optionMenuBar = new JMenuBar(); 
 		optionMenu = new JMenu("Options"); 
 		resetOption = new JMenuItem("Reset"); 
 		resetOption.addActionListener(this); 
 		resetOption.setActionCommand("Reset"); 
 		deletePartOption = new JMenuItem("Delete a Part");
 		optionMenu.add(resetOption); 
 		optionMenu.add(deletePartOption);
 		optionMenuBar.add(optionMenu); 
 		add(optionMenuBar, BorderLayout.NORTH); 
 
 		partsGrid = new JPanel(new GridLayout(1, 5)); 	
 		partsGrid.setVisible(true); 
 		//First Column
 		fC = new FirstColumn();		
 		partsGrid.add(fC);		
 		//Second Column
 		sC = new SecondColumn(); 
 		partsGrid.add(sC); 		
 		//Third Column			
 		tC = new ThirdColumn(this); 
 		partsGrid.add(tC); 	
 		
 		
 		tC.getFirstPartButton().addActionListener(this); 
 		tC.getFirstPartButton().setActionCommand("1"); 
 		tC.getSecondPartButton().addActionListener(this); 
 		tC.getSecondPartButton().setActionCommand("2"); 
 		tC.getThirdPartButton().addActionListener(this); 
 		tC.getThirdPartButton().setActionCommand("3"); 
 		tC.getFourthPartButton().addActionListener(this); 
 		tC.getFourthPartButton().setActionCommand("4"); 
 		tC.getFifthPartButton().addActionListener(this); 
 		tC.getFifthPartButton().setActionCommand("5"); 
 		tC.getSixthPartButton().addActionListener(this); 
 		tC.getSixthPartButton().setActionCommand("6"); 
 		tC.getSeventhPartButton().addActionListener(this); 
 		tC.getSeventhPartButton().setActionCommand("7"); 
 		tC.getEighthPartButton().addActionListener(this); 
 		tC.getEighthPartButton().setActionCommand("8"); 
 		tC.getNinthPartButton().addActionListener(this); 
 		tC.getNinthPartButton().setActionCommand("9"); 
 		tC.getTenthPartButton().addActionListener(this); 
 		tC.getTenthPartButton().setActionCommand("10"); 
 		
 		//main panel
 		
 		pI = new PartInfo(); 
 		pI.getCreatePart().setActionCommand("Create Part"); 
 		pI.getCreatePart().addActionListener(this); 
 		partsGrid.add(pI);
 		add(partsGrid);
 		
 		part1Edit = false; 
 		part2Edit = false; 
 		part3Edit = false; 
 		part4Edit = false; 
 		part5Edit = false; 
 		part6Edit = false; 
 		part7Edit = false; 
 		part8Edit = false; 
 		part9Edit = false; 
 		part10Edit = false; 
 
 		isPart1New = true; 
 		isPart2New = true; 
 		isPart3New = true; 
 		isPart4New = true; 
 		isPart5New = true; 
 		isPart6New = true; 
 		isPart7New = true; 
 		isPart8New = true; 
 		isPart9New = true; 
 		isPart10New = true; 
 		
 
 		listCounter = 0; 
 		listOfParts = new TreeMap<Integer, Parts>(); 
 	}
 	
 	
 	public static void main(String[] args) {
 		PartsManager thisPartsManager = new PartsManager(); 
 		thisPartsManager.setTitle("Parts Manager");
 	    thisPartsManager.setSize(1000, 350);	
 	    thisPartsManager.setLocationRelativeTo(null);	    
 	    thisPartsManager.setVisible(true); 
 	    thisPartsManager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
 
 	}
 	
 	
 	public void actionPerformed(ActionEvent ae){
 		//First Button
 		if("1".equals(ae.getActionCommand())){
 			pI.setVisible(true); 
 			tC.setVisible(false); 
 			part1Edit = true; 
 
 		}
 		if("Create Part".equals(ae.getActionCommand()) && part1Edit == true){
 			String nameText = pI.getEnterNameHere().getText(); 
 			fC.getPartOneName().setText("Part 1: "+nameText); 
 			fC.setVisible(false);
 			tC.setVisible(true); 
 			fC.setVisible(true);
 			pI.setVisible(false); 
 			part1Edit = false; 
 			String stringPartIndex = pI.getPartIndexNumber().getText(); 
 			int intPartIndex = 0; 
 			try{
 				intPartIndex = Integer.parseInt(stringPartIndex);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				fC.getPartOneName().setText("Part 1"); 
 				return; 
 			}
 				int savedCounter = 0; 
 				
 				if(isPart1New){
 					newPart1 = new Parts(intPartIndex, nameText, pI.getPartDescription().getText(), 0);
 					newPart1.setMapIndex(listCounter); 
 					listOfParts.put(listCounter, newPart1); 
 					savedCounter = listCounter; 
 					listCounter++; 	
 					tC.getFirstPartButton().setText("E");
 					isPart1New = false; 
 				}
 				else{
 					int mapIndex = newPart1.getMapIndex(); 
 					if(mapIndex==-1){
 						System.out.println("Map index is -1, not valid"); 
 					}
 					else{						
 						String indexString = pI.getPartIndexNumber().getText();
 						int indexInt = Integer.parseInt(indexString); 
 						newPart1.setPartNumber(indexInt); 
 						newPart1.setName(pI.getEnterNameHere().getText());
 						newPart1.setDesc(pI.getPartDescription().getText());
 					}
 					
 				}
 				for(int i = 0; i<listOfParts.size(); i++){
 					System.out.println(listOfParts.get(i).getName()); 
 				}
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				pI.getEnterNameHere().setText("Enter Name of Part Here"); 
 				pI.getPartDescription().setText("Short Description of Part Here"); 
 
 
 				 
 
 				nb1.sendPartData(listOfParts); 
 		}
 		
 		//Second Button
 		if("2".equals(ae.getActionCommand())){
 			pI.setVisible(true); 
 			tC.setVisible(false); 
 			part2Edit = true; 
 		}
 		if("Create Part".equals(ae.getActionCommand()) && part2Edit == true){
 			String nameText = pI.getEnterNameHere().getText(); 
 			fC.getPartTwoName().setText("Part 2: "+nameText); 
 			fC.setVisible(false);
 			tC.setVisible(true); 
 			fC.setVisible(true);
 			pI.setVisible(false); 
 			part2Edit = false; 
 			String stringPartIndex = pI.getPartIndexNumber().getText(); 
 			int intPartIndex = 0; 
 			try{
 				intPartIndex = Integer.parseInt(stringPartIndex);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				fC.getPartOneName().setText("Part 2"); 
 				return; 
 			} 			
 			int savedCounter = 0; 
 				
 				if(isPart2New){
 					newPart2 = new Parts(intPartIndex, nameText, pI.getPartDescription().getText(), 1);
					newPart2.setMapIndex(listCounter); 
 					listOfParts.put(listCounter, newPart2); 
 					savedCounter = listCounter; 
 					listCounter++; 	
 					tC.getSecondPartButton().setText("E");
 					isPart2New = false; 
 				}
 				else{						
 						String indexString = pI.getPartIndexNumber().getText();
 						int indexInt = Integer.parseInt(indexString); 
 						newPart2.setPartNumber(indexInt); 
 						newPart2.setName(pI.getEnterNameHere().getText());
 						newPart2.setDesc(pI.getPartDescription().getText());
 					}
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				pI.getEnterNameHere().setText("Enter Name of Part Here"); 
 				pI.getPartDescription().setText("Short Description of Part Here"); 
 			nb1.sendPartData(listOfParts); 
 			
 		}
 		
 		//Third Button
 		if("3".equals(ae.getActionCommand())){
 			pI.setVisible(true); 
 			tC.setVisible(false); 
 			part3Edit = true; 
 		}
 		if("Create Part".equals(ae.getActionCommand()) && part3Edit == true){
 			String nameText = pI.getEnterNameHere().getText(); 
 			fC.getPartThreeName().setText("Part 3: "+nameText); 
 			fC.setVisible(false);
 			tC.setVisible(true); 
 			fC.setVisible(true);
 			pI.setVisible(false); 
 			part3Edit = false; 
 			
 			String stringPartIndex = pI.getPartIndexNumber().getText(); 
 			int intPartIndex = 0; 
 			try{
 				intPartIndex = Integer.parseInt(stringPartIndex);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				fC.getPartOneName().setText("Part 3"); 
 				return; 
 			} 			
 			int savedCounter = 0; 
 				
 				if(isPart3New){
 					newPart3 = new Parts(intPartIndex, nameText, pI.getPartDescription().getText(), 2);
					newPart3.setMapIndex(listCounter); 
 					listOfParts.put(listCounter, newPart3); 
 					savedCounter = listCounter; 
 					listCounter++; 	
 					tC.getThirdPartButton().setText("E");
 					isPart3New = false; 
 				}
 				else{						
 						String indexString = pI.getPartIndexNumber().getText();
 						int indexInt = Integer.parseInt(indexString); 
 						newPart3.setPartNumber(indexInt); 
 						newPart3.setName(pI.getEnterNameHere().getText());
 						newPart3.setDesc(pI.getPartDescription().getText());
 					}
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				pI.getEnterNameHere().setText("Enter Name of Part Here"); 
 				pI.getPartDescription().setText("Short Description of Part Here"); 
 			nb1.sendPartData(listOfParts);
 			
 		}
 		
 		//Fourth Button
 		if("4".equals(ae.getActionCommand())){
 			pI.setVisible(true); 
 			tC.setVisible(false); 
 			part4Edit = true; 
 		}
 		if("Create Part".equals(ae.getActionCommand()) && part4Edit == true){
 			String nameText = pI.getEnterNameHere().getText(); 
 			fC.getPartFourName().setText("Part 4: "+nameText); 
 			fC.setVisible(false);
 			tC.setVisible(true); 
 			fC.setVisible(true);
 			pI.setVisible(false); 
 			part4Edit = false; 
 			String stringPartIndex = pI.getPartIndexNumber().getText(); 
 			int intPartIndex = 0; 
 			try{
 				intPartIndex = Integer.parseInt(stringPartIndex);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				fC.getPartOneName().setText("Part 4"); 
 				return; 
 			} 			
 			int savedCounter = 0; 
 				
 				if(isPart4New){
 					newPart4 = new Parts(intPartIndex, nameText, pI.getPartDescription().getText(), 3);
					newPart4.setMapIndex(listCounter); 
 					listOfParts.put(listCounter, newPart4); 
 					savedCounter = listCounter; 
 					listCounter++; 	
 					tC.getFourthPartButton().setText("E");
 					isPart4New = false; 
 				}
 				else{						
 						String indexString = pI.getPartIndexNumber().getText();
 						int indexInt = Integer.parseInt(indexString); 
 						newPart4.setPartNumber(indexInt); 
 						newPart4.setName(pI.getEnterNameHere().getText());
 						newPart4.setDesc(pI.getPartDescription().getText());
 					}
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				pI.getEnterNameHere().setText("Enter Name of Part Here"); 
 				pI.getPartDescription().setText("Short Description of Part Here"); 
 			nb1.sendPartData(listOfParts);
 			
 		}
 		
 		//Fifth Button
 		
 		if("5".equals(ae.getActionCommand())){
 			pI.setVisible(true); 
 			tC.setVisible(false); 
 			part5Edit = true; 
 		}
 		if("Create Part".equals(ae.getActionCommand()) && part5Edit == true){
 			String nameText = pI.getEnterNameHere().getText(); 
 			fC.getPartFiveName().setText("Part 5: "+nameText); 
 			fC.setVisible(false);
 			tC.setVisible(true); 
 			fC.setVisible(true);
 			pI.setVisible(false); 
 			part5Edit = false; 
 			String stringPartIndex = pI.getPartIndexNumber().getText(); 
 			int intPartIndex = 0; 
 			try{
 				intPartIndex = Integer.parseInt(stringPartIndex);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				fC.getPartOneName().setText("Part 5"); 
 				return; 
 			} 			
 			int savedCounter = 0; 
 				
 				if(isPart5New){
 					newPart5 = new Parts(intPartIndex, nameText, pI.getPartDescription().getText(), 4);
					newPart5.setMapIndex(listCounter); 
 					listOfParts.put(listCounter, newPart5); 
 					savedCounter = listCounter; 
 					listCounter++; 	
 					tC.getFifthPartButton().setText("E");
 					isPart5New = false; 
 				}
 				else{						
 						String indexString = pI.getPartIndexNumber().getText();
 						int indexInt = Integer.parseInt(indexString); 
 						newPart5.setPartNumber(indexInt); 
 						newPart5.setName(pI.getEnterNameHere().getText());
 						newPart5.setDesc(pI.getPartDescription().getText());
 					}
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				pI.getEnterNameHere().setText("Enter Name of Part Here"); 
 				pI.getPartDescription().setText("Short Description of Part Here"); 
 			nb1.sendPartData(listOfParts);
 		}
 		
 		//Sixth Button
 		
 		if("6".equals(ae.getActionCommand())){
 			pI.setVisible(true); 
 			tC.setVisible(false); 
 			part6Edit = true; 
 		}
 		if("Create Part".equals(ae.getActionCommand()) && part6Edit == true){
 			String nameText = pI.getEnterNameHere().getText(); 
 			fC.getPartSixName().setText("Part 6: "+nameText); 
 			fC.setVisible(false);
 			tC.setVisible(true); 
 			fC.setVisible(true);
 			pI.setVisible(false); 
 			part6Edit = false; 
 			String stringPartIndex = pI.getPartIndexNumber().getText(); 
 			int intPartIndex = 0; 
 			try{
 				intPartIndex = Integer.parseInt(stringPartIndex);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				fC.getPartOneName().setText("Part 6"); 
 				return; 
 			}			
 			int savedCounter = 0; 
 				
 				if(isPart6New){
 					newPart6 = new Parts(intPartIndex, nameText, pI.getPartDescription().getText(), 5);
					newPart6.setMapIndex(listCounter); 
 					listOfParts.put(listCounter, newPart6); 
 					savedCounter = listCounter; 
 					listCounter++; 	
 					tC.getSixthPartButton().setText("E");
 					isPart6New = false; 
 				}
 				else{						
 						String indexString = pI.getPartIndexNumber().getText();
 						int indexInt = Integer.parseInt(indexString); 
 						newPart6.setPartNumber(indexInt); 
 						newPart6.setName(pI.getEnterNameHere().getText());
 						newPart6.setDesc(pI.getPartDescription().getText());
 					}
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				pI.getEnterNameHere().setText("Enter Name of Part Here"); 
 				pI.getPartDescription().setText("Short Description of Part Here"); 
 			nb1.sendPartData(listOfParts);
 		}
 		
 		//Seventh Button
 		if("7".equals(ae.getActionCommand())){
 			pI.setVisible(true); 
 			tC.setVisible(false); 
 			part7Edit = true; 
 		}
 		if("Create Part".equals(ae.getActionCommand()) && part7Edit == true){
 			String nameText = pI.getEnterNameHere().getText(); 
 			fC.getPartSevenName().setText("Part 7: "+nameText); 
 			fC.setVisible(false);
 			tC.setVisible(true); 
 			fC.setVisible(true);
 			pI.setVisible(false); 
 			part7Edit = false; 
 			String stringPartIndex = pI.getPartIndexNumber().getText(); 
 			int intPartIndex = 0; 
 			try{
 				intPartIndex = Integer.parseInt(stringPartIndex);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				fC.getPartOneName().setText("Part 7"); 
 				return; 
 			} 			
 			int savedCounter = 0; 
 				
 				if(isPart7New){
 					newPart7 = new Parts(intPartIndex, nameText, pI.getPartDescription().getText(), 6);
					newPart7.setMapIndex(listCounter); 
 					listOfParts.put(listCounter, newPart7); 
 					savedCounter = listCounter; 
 					listCounter++; 	
 					tC.getSeventhPartButton().setText("E");
 					isPart7New = false; 
 				}
 				else{						
 						String indexString = pI.getPartIndexNumber().getText();
 						int indexInt = Integer.parseInt(indexString); 
 						newPart7.setPartNumber(indexInt); 
 						newPart7.setName(pI.getEnterNameHere().getText());
 						newPart7.setDesc(pI.getPartDescription().getText());
 					}
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				pI.getEnterNameHere().setText("Enter Name of Part Here"); 
 				pI.getPartDescription().setText("Short Description of Part Here"); 
 			nb1.sendPartData(listOfParts); 
 		}
 		
 		//Eighth Button
 		if("8".equals(ae.getActionCommand())){
 			pI.setVisible(true); 
 			tC.setVisible(false); 
 			part8Edit = true; 
 		}
 		if("Create Part".equals(ae.getActionCommand()) && part8Edit == true){
 			String nameText = pI.getEnterNameHere().getText(); 
 			fC.getPartEightName().setText("Part 8: "+nameText); 
 			fC.setVisible(false);
 			tC.setVisible(true); 
 			fC.setVisible(true);
 			pI.setVisible(false); 
 			part8Edit = false; 
 			String stringPartIndex = pI.getPartIndexNumber().getText(); 
 			int intPartIndex = 0; 
 			try{
 				intPartIndex = Integer.parseInt(stringPartIndex);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here");
 				fC.getPartOneName().setText("Part 8"); 
 				return; 
 			} 			
 			int savedCounter = 0; 
 				
 			if(isPart8New){
 				newPart8 = new Parts(intPartIndex, nameText, pI.getPartDescription().getText(), 7);
				newPart8.setMapIndex(listCounter); 
 				listOfParts.put(listCounter, newPart8); 
 				savedCounter = listCounter; 
 				listCounter++; 	
 				tC.getEighthPartButton().setText("E");
 				isPart8New = false; 
 			}
 			else{						
 						String indexString = pI.getPartIndexNumber().getText();
 						int indexInt = Integer.parseInt(indexString); 
 						newPart8.setPartNumber(indexInt); 
 						newPart8.setName(pI.getEnterNameHere().getText());
 						newPart8.setDesc(pI.getPartDescription().getText());
 					}
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				pI.getEnterNameHere().setText("Enter Name of Part Here"); 
 				pI.getPartDescription().setText("Short Description of Part Here"); 
 			nb1.sendPartData(listOfParts); 
 			
 		}
 		//Ninth Button
 		if("9".equals(ae.getActionCommand())){
 			pI.setVisible(true); 
 			tC.setVisible(false); 
 			part9Edit = true; 
 		}
 		if("Create Part".equals(ae.getActionCommand()) && part9Edit == true){
 			String nameText = pI.getEnterNameHere().getText(); 
 			fC.getPartNineName().setText("Part 9: "+nameText); 
 			fC.setVisible(false);
 			tC.setVisible(true); 
 			fC.setVisible(true);
 			pI.setVisible(false); 
 			part9Edit = false; 
 			String stringPartIndex = pI.getPartIndexNumber().getText(); 
 			int intPartIndex = 0; 
 			try{
 				intPartIndex = Integer.parseInt(stringPartIndex);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				fC.getPartOneName().setText("Part 9"); 
 				return; 
 			} 			
 			int savedCounter = 0; 
 				
 				if(isPart9New){
 					newPart9 = new Parts(intPartIndex, nameText, pI.getPartDescription().getText(), 8);
					newPart9.setMapIndex(listCounter); 
 					listOfParts.put(listCounter, newPart9); 
 					savedCounter = listCounter; 
 					listCounter++; 	
 					tC.getNinthPartButton().setText("E");
 					isPart9New = false; 
 				}
 				else{						
 						String indexString = pI.getPartIndexNumber().getText();
 						int indexInt = Integer.parseInt(indexString); 
 						newPart9.setPartNumber(indexInt); 
 						newPart9.setName(pI.getEnterNameHere().getText());
 						newPart9.setDesc(pI.getPartDescription().getText());
 					}
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				pI.getEnterNameHere().setText("Enter Name of Part Here"); 
 				pI.getPartDescription().setText("Short Description of Part Here"); 
 			nb1.sendPartData(listOfParts); 
 		}
 		
 		//Tenth Button
 		if("10".equals(ae.getActionCommand())){
 			pI.setVisible(true); 
 			tC.setVisible(false); 
 			part10Edit = true; 
 		}
 		if("Create Part".equals(ae.getActionCommand()) && part10Edit == true){
 			String nameText = pI.getEnterNameHere().getText(); 
 			fC.getPartTenName().setText("Part 10: "+nameText); 
 			fC.setVisible(false);
 			tC.setVisible(true); 
 			fC.setVisible(true);
 			pI.setVisible(false);
 			part10Edit = false; 			
 			String stringPartIndex = pI.getPartIndexNumber().getText(); 
 			int intPartIndex = 0; 
 			try{
 				intPartIndex = Integer.parseInt(stringPartIndex);
 			}
 			catch(Exception z){
 				System.out.println("Exception found - did not enter in an integer");
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				fC.getPartOneName().setText("Part 10"); 
 				return; 
 			} 			
 			int savedCounter = 0; 
 				
 				if(isPart10New){
 					newPart10 = new Parts(intPartIndex, nameText, pI.getPartDescription().getText(), 9);
					newPart10.setMapIndex(listCounter); 
 					listOfParts.put(listCounter, newPart10); 
 					savedCounter = listCounter; 
 					listCounter++; 	
 					tC.getTenthPartButton().setText("E");
 					isPart10New = false; 
 				}
 				else{						
 						String indexString = pI.getPartIndexNumber().getText();
 						int indexInt = Integer.parseInt(indexString); 
 						newPart10.setPartNumber(indexInt); 
 						newPart10.setName(pI.getEnterNameHere().getText());
 						newPart10.setDesc(pI.getPartDescription().getText());
 					}
 				pI.getPartIndexNumber().setText("Enter Part ID Number Here"); 
 				pI.getEnterNameHere().setText("Enter Name of Part Here"); 
 				pI.getPartDescription().setText("Short Description of Part Here"); 
 			nb1.sendPartData(listOfParts); 
 		}
 		
 		if("Reset".equals(ae.getActionCommand())){
 			//everything resets and a new kit can be created
 			listOfParts.clear(); 
 			
 		}
 	}
 		
 		public void deletePart(int pID){
 				switch(pID){
 						case 1:
 								if(newPart1 != null){
 										fC.getPartOneName().setText("Unused"); 
 										listOfParts.remove(newPart1.getMapIndex());
 										isPart1New = true;
 										tC.getFirstPartButton().setText("N");
 								}
 								break;
 						case 2:
 								if(newPart2 != null){
 										fC.getPartTwoName().setText("Unused"); 
 										listOfParts.remove(newPart2.getMapIndex());
 										isPart2New = true;
 										tC.getSecondPartButton().setText("N");
 								}
 								break;
 						case 3:
 								if(newPart3 != null){
 										fC.getPartThreeName().setText("Unused"); 
 										listOfParts.remove(newPart3.getMapIndex());
 										isPart3New = true;
 										tC.getThirdPartButton().setText("N");
 								}
 								break;
 						case 4:
 								if(newPart4 != null){
 										fC.getPartFourName().setText("Unused"); 
 										listOfParts.remove(newPart4.getMapIndex());
 										isPart4New = true;
 										tC.getFourthPartButton().setText("N");
 								}
 								break;
 						case 5:
 								if(newPart5 != null){
 										fC.getPartFiveName().setText("Unused"); 
 										listOfParts.remove(newPart5.getMapIndex());
 										isPart5New = true;
 										tC.getFifthPartButton().setText("N");
 								}
 								break;
 						case 6:
 								if(newPart6 != null){
 										fC.getPartSixName().setText("Unused"); 
 										listOfParts.remove(newPart6.getMapIndex());
 										isPart6New = true;
 										tC.getSixthPartButton().setText("N");
 								}
 								break;
 						case 7:
 								if(newPart7 != null){
 										fC.getPartSevenName().setText("Unused"); 
 										listOfParts.remove(newPart7.getMapIndex());
 										isPart7New = true;
 										tC.getSeventhPartButton().setText("N");
 								}
 								break;
 						case 8:
 								if(newPart8 != null){
 										fC.getPartEightName().setText("Unused"); 
 										listOfParts.remove(newPart8.getMapIndex());
 										isPart8New = true;
 										tC.getEighthPartButton().setText("N");
 								}
 								break;
 						case 9:
 								if(newPart9 != null){
 										fC.getPartNineName().setText("Unused"); 
 										listOfParts.remove(newPart9.getMapIndex());
 										isPart9New = true;
 										tC.getNinthPartButton().setText("N");
 								}
 								break;
 						case 10:
 								if(newPart10 != null){
 										fC.getPartTenName().setText("Unused"); 
 										listOfParts.remove(newPart10.getMapIndex());
 										isPart10New = true;
 										tC.getTenthPartButton().setText("N");
 								}
 								break;
 				}
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ----------------------------------- Network Manager ---------------------------------- //
 		// -------------------------------------------------------------------------------------- //
 		
 		// server specific
 		public void registerClientListener(NetworkBridge newBridge, int cID){}
 		public void syncFrame(){}
 		public void updatePartData(TreeMap<Integer, Parts>partData){}
 		public void updateKitData(ArrayList<Kits>kitData){};
 		
 		// client specific
 		public void mergeChanges(ArrayList<TreeMap<Integer, Boolean>> mapArray, ArrayList<TreeMap<Integer, FactoryObject>> dataArray){}
 		
 		public void syncChanges(ArrayList<TreeMap<Integer,FactoryObject>> dataArray){}
 		
 		// global
 		public void closeNetworkBridge(int bridgeID){
 				nb1.close();
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ----------------------------------- End Network Manager ------------------------------ //
 		// -------------------------------------------------------------------------------------- //
 		
 
 }
