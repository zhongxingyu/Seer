 /*
 ** Author: Nikhil Handyal, Siddarth Ramesh
 ** Date: 11/26/12
 ** Project: Cs200-Factory
 ** Description: Kit Manager Code
 ** 
 ** Pre-Conditions: None
 */
 package factory.client.kitManager;
 
 // Java packages
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.BorderFactory; 
 import javax.swing.border.Border;
 import java.awt.*; 
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.TreeMap;
 import java.util.Enumeration;
 
 
 // user packages
 import factory.global.data.*; 
 import factory.global.network.*;
 
 
 public class KitManager extends JFrame implements ActionListener, ListSelectionListener, NetworkManager{
 		private static final int PAGE_WIDTH = 650;
 		private static final int PAGE_HEIGHT = 650;
 		private CardLayout c1;
 		private JPanel activeKitsPanel, kitDataPanel, kitStructPanel, partsSelectPanel;
 		private JPanel activeKitsContainer, createKitContainer, masterContainer;
 		private DefaultListModel listModel;
 		private JList kitList;
 		private JButton createNewKit, editKit, deleteKit, saveNewKit, cancelNewKit;
 		private JTextField kitName, kitID;
 		private JTextArea kitDesc;
 		private JLabel spWarning;
 		private TreeMap<Integer, Parts> parts;
 		private TreeMap<Integer, Kits> kits;
 		private int kitNumber, editKitNumber;
 		private ImageArray images;
 		private Border greyLine;
 		private boolean bEditKit;
 		private ButtonGroup[] selectedParts;
 		private NetworkBridge nb1;
 		
 		KitManager(){
 				// initialize class variables
 				masterContainer = new JPanel();
 				activeKitsContainer = new JPanel();
 				createKitContainer = new JPanel();
 				activeKitsPanel = new JPanel();
 				kitDataPanel = new JPanel();
 				kitStructPanel = new JPanel();
 				partsSelectPanel = new JPanel();
 				editKit = new JButton("Edit Kit");
 				deleteKit = new JButton("Delete Kit");
 				selectedParts = new ButtonGroup[8];
 				parts = new TreeMap<Integer, Parts>();
 				kits = new TreeMap<Integer, Kits>();
 				kitNumber = 0;
 				images = new ImageArray();
 				greyLine = BorderFactory.createLineBorder(Color.DARK_GRAY);
 				c1 = new CardLayout();
 				spWarning = new JLabel("");
 				editKit.addActionListener(this);
 				deleteKit.addActionListener(this);
 				setComponentSize(editKit,150,50);
 				setComponentSize(deleteKit,150,50);
 				spWarning.setForeground(Color.RED);
 				bEditKit = false;
 				
 				
 				// set Frame and properties
 				masterContainer.setLayout(c1);
 				
 				
 				// build Active Kits Container
 				activeKitsContainer.setLayout(new BoxLayout(activeKitsContainer, BoxLayout.X_AXIS));
 				setComponentSize(activeKitsContainer,PAGE_WIDTH,PAGE_HEIGHT);
 				setComponentSize(kitDataPanel,450,PAGE_HEIGHT);
 				buildActiveKits(activeKitsPanel);
 				activeKitsContainer.add(activeKitsPanel);
 				activeKitsContainer.add(kitDataPanel);
 				
 				
 				
 				// build Create Kit Container
 				createKitContainer.setLayout(new BoxLayout(createKitContainer, BoxLayout.X_AXIS));
 				setComponentSize(createKitContainer,PAGE_WIDTH,PAGE_HEIGHT);
 				setComponentSize(partsSelectPanel, 450, PAGE_HEIGHT);
 				buildKitStruct(kitStructPanel);
 				buildPartsSelect();
 				createKitContainer.add(kitStructPanel);
 				createKitContainer.add(partsSelectPanel);
 				
 				// add master containers to frame
 				masterContainer.add(activeKitsContainer,"akc");
 				masterContainer.add(createKitContainer,"ckc");
 				this.add(masterContainer);
 				c1.show(masterContainer,"akc");
 				
 				// initialize the network bridge after all page elements have been created
 				nb1 = new NetworkBridge(this,"localhost",8465,1);
 		}
 		
 		public static void main(String[] args){
 				KitManager km = new KitManager();
 				
 				// set frame properties
 				km.setSize(PAGE_WIDTH,PAGE_HEIGHT);
 				km.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				km.setTitle("Kit Manager");
 				km.setResizable(false);
 				km.setVisible(true);
 		}
 		
 		public void valueChanged(ListSelectionEvent le){
 				if(!le.getValueIsAdjusting()){
 						Kits currentKit = (Kits)kitList.getSelectedValue();
 						if(currentKit != null)
 								buildKitData(currentKit);
 				}
 		}
 		
 		public void actionPerformed(ActionEvent ae){				
 				if(ae.getSource() == saveNewKit){
 						// clear the warnings from the previous save attempt
 						spWarning.setText("");
 						
 						// attempt a new save
 						String KName = kitName.getText();
 						String KDesc = kitDesc.getText();
 						String KID = kitID.getText();
 						TreeMap<Integer, Parts> kitParts = new TreeMap<Integer, Parts>();
 						int nps = 0;
 						int intKID;
 						
 						// validate kit id as an integer
 						KID.trim();
 						try{  
 								intKID = Integer.parseInt(KID);
 								if(intKID <= 0){
 										spWarning.setText("Kit ID must be an integer value greater than 0");
 										return;
 								}	
 						}
 						catch( Exception e ){
 								//invalid user input
 								spWarning.setText("Kit ID must be an integer value greater than 0");
 								return;
 						}
 
 						// ensure enough parts are selected and start building the kit
 						for(int i = 0; i < 8; i++){
 								ButtonGroup group = selectedParts[i];
 								Integer partSelection = Integer.parseInt(group.getSelection().getActionCommand());
 								if(partSelection != -1){
 										kitParts.put(i, parts.get(partSelection));
 										nps++;
 								}
 						}
 						if(nps < 4){
 								spWarning.setText("You must select a minimum of 4 parts in each kit");
 						}
 						else{
 								Kits newKit = new Kits(KName, kitParts, KDesc, intKID, intKID);
 								Kits currentKit = (Kits)kitList.getSelectedValue();
 								int currentKitNumber = -1;
 								if(currentKit != null){
 										currentKitNumber = currentKit.getKitID();
 										System.out.println("ae: ckn-"+currentKitNumber);
 								}
 								if(bEditKit && currentKitNumber >= 0){
 										
 										kits.remove(currentKitNumber);
 								}
 								kits.put(intKID, newKit);
 								bEditKit = false;
 								c1.show(masterContainer,"akc");
 								nb1.sendKitData(kits);
 								populateActiveKitList();
 								clearKitData();
 						}
 				}
 				else if(ae.getSource() == cancelNewKit){
 						bEditKit = false;
 						c1.show(masterContainer,"akc");
 						clearKitData();
 				}
 				else if(ae.getSource() == editKit){
 						bEditKit = true;
 						c1.show(masterContainer,"ckc");
 						resetKitStruct();
 						buildPartsSelect();
 						buildEditKit((Kits)kitList.getSelectedValue());
 				}
 				else if(ae.getSource() == createNewKit){
 						bEditKit = false;
 						c1.show(masterContainer,"ckc");
 						resetKitStruct();
 						buildPartsSelect();
 				}
 				else if(ae.getSource() == deleteKit){
 						Kits selectedKit = (Kits)kitList.getSelectedValue();
 						int deleteIndex = selectedKit.getKitID();
 						kits.remove(deleteIndex);
 						nb1.sendKitData(kits);
 						populateActiveKitList();
 						clearKitData();
 				}
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ---------------------------------- Constructor Helpers ------------------------------- //
 		// -------------------------------------------------------------------------------------- //
 		private void buildActiveKits(JPanel container){
 				// initialize variable
 				final int WIDTH = 150;
 				
 				// set containment panel properties
 				container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
 				setComponentSize(container,150,PAGE_HEIGHT);
 				
 				
 				JLabel header = new JLabel("Active Kits");
 				header.setHorizontalAlignment(header.CENTER);
 				header.setFont(new Font("Serif", Font.BOLD, 18));
 				setComponentSize(header,WIDTH,25);
 				
 				
 				createNewKit = new JButton("Create New Kit");
 				createNewKit.addActionListener(this);
 				setComponentSize(createNewKit,WIDTH,50);
 				
 				
 				// create list model and list
 				listModel = new DefaultListModel();
 				kitList = new JList(listModel);
 				kitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         kitList.addListSelectionListener(this);
 				kitList.setFixedCellHeight(25);
         JScrollPane listScrollPane = new JScrollPane(kitList);
 				
 				// add elements to containment panel
 				container.add(header);
 				container.add(listScrollPane);
 				container.add(createNewKit);
 		}
 		
 		private void populateActiveKitList(){
 				// populate the active kits list with the current kits available in the factory
 				listModel.removeAllElements();
 				for(Integer i : kits.keySet()){
 						Kits currentKit = kits.get(i);
 						listModel.addElement(currentKit);
 				}
 		}
 		
 		private void clearKitData(){
 				kitDataPanel.removeAll();
 				JPanel blank = new JPanel();
 				Box container = Box.createVerticalBox();
 				setComponentSize(blank, 400, PAGE_HEIGHT);
 				container.add(blank);
 				kitDataPanel.add(container);
 				kitDataPanel.revalidate();
 		}
 		
 		private void buildKitData(Kits selectedKit){
 				final int ST_SPACE = 25;
 				kitDataPanel.removeAll();
 				Box container = Box.createVerticalBox();
 				setComponentSize(container, 400, PAGE_HEIGHT);
 				JLabel kitID = new JLabel(Integer.toString(selectedKit.getKitID()));
 				JLabel kitName = new JLabel(selectedKit.getName());
 				JLabel kitDesc = new JLabel(selectedKit.getDescription());
 				JLabel lp = new JLabel("----- Listed Parts -----");
 				
 				// align elements to the left collum
 				// all the arguments can be left as KitID.LEFT because we only need any instance of a JComponent to use the LEFT keyword
 				kitID.setHorizontalAlignment(kitID.LEFT);
 				kitName.setHorizontalAlignment(kitID.LEFT);
 				kitDesc.setHorizontalAlignment(kitID.LEFT);
 				lp.setHorizontalAlignment(kitID.LEFT);
 				
 				// add elements to container
 				container.add(kitID);
 				container.add(kitName);
 				container.add(kitDesc);
 				container.add(Box.createVerticalStrut(ST_SPACE));
 				container.add(lp);
 				
 				
 				// add part data
 				TreeMap<Integer, Parts> kitParts = selectedKit.getListOfParts();
 				for(Integer i : kitParts.keySet()){
 						Parts selectedPart = kitParts.get(i);
 						Box holder = Box.createHorizontalBox();
 						Box section1 = Box.createVerticalBox();
 						Box section2 = Box.createVerticalBox();
 						int imageIndex = selectedPart.getImageIndex();
 						String PID = Integer.toString(selectedPart.getPartNumber());
 						String PName = selectedPart.getName();
 						String PDesc = selectedPart.getDesc();
 						
 						setComponentSize(holder, 400, 60);
 						
 						// add elements to section1
 						section1.add(new JLabel("Part "+i));
 						section1.add(new JLabel(images.getIcon(imageIndex)));
 						
 						// add elements to section2
 						section2.add(new JLabel("Part #: "+PID));
 						section2.add(new JLabel("Part name: "+PName));
 						section2.add(new JLabel("Part desc: "+PDesc));
 						
 						// add sections to holder
 						holder.add(section1);
 						holder.add(Box.createHorizontalStrut(ST_SPACE));
 						holder.add(section2);
 						
 						// add holder to container
 						container.add(holder);
 				}
 				// add edit kit to contianer
 				Box holder2 = Box.createHorizontalBox();
                setComponentSize(holder2, 400, 60);
 				holder2.add(editKit);
 				holder2.add(Box.createHorizontalStrut(ST_SPACE));
 				holder2.add(deleteKit);
 				container.add(holder2);
 				
 				// add container to kitDataPanel
 				kitDataPanel.add(container);
 				kitDataPanel.revalidate();
 		}
 		
 		private void buildKitStruct(JPanel container){		
 				// initialize variables
 				final int WIDTH = 200;
 				final int ST_SPACE = 25;
 				kitName = new JTextField();
 				kitID = new JTextField();
 				kitDesc = new JTextArea();
 				saveNewKit = new JButton("Save Kit");
 				cancelNewKit = new JButton("Cancel");
 				JLabel kitLayout = new JLabel(images.getIcon(17));
 				kitDesc.setBorder(greyLine);
 				saveNewKit.addActionListener(this);
 				cancelNewKit.addActionListener(this);
 				Box box = Box.createVerticalBox();
 				
 				// set container properties
 				setComponentSize(container,WIDTH,600);
 				
 				// set field and button properties
 				setComponentSize(kitName,WIDTH,25);
 				setComponentSize(kitID,WIDTH,25);
 				setComponentSize(kitDesc,WIDTH,100);
 				setComponentSize(saveNewKit,WIDTH,50);
 				setComponentSize(cancelNewKit,WIDTH,50);
 				
 				resetKitStruct();
 				
 				// add elements to container
 				box.add(kitName);
 				box.add(Box.createVerticalStrut(ST_SPACE));
 				box.add(kitID);
 				box.add(Box.createVerticalStrut(ST_SPACE));
 				box.add(kitDesc);
 				box.add(Box.createVerticalStrut(ST_SPACE));
 				box.add(kitLayout);
 				box.add(Box.createVerticalStrut(ST_SPACE));
 				box.add(saveNewKit);
 				box.add(cancelNewKit);
 				container.add(box);
 		}
 		
 		private void buildPartsSelect(){
 				if(parts == null)
 						return;
 				
 				final int H_WIDTH = 450;
 				final int H_HEIGHT = 60;
 				final int n = parts.size();
 				Box box = Box.createVerticalBox();
 				partsSelectPanel.removeAll();
 				
 				
 				for(int i = 0; i < 8; i++){
 						JPanel holder = new JPanel();
 						holder.setLayout(new BorderLayout());
 						setComponentSize(holder, H_WIDTH, H_HEIGHT);
 						JLabel partLabel = new JLabel("   Part "+(i+1)+":   ");
 						JLabel unused = new JLabel("  ",images.getIcon(20), SwingConstants.LEFT);
 						unused.setHorizontalTextPosition(SwingConstants.LEFT);
 						JRadioButton unusedRadio = new JRadioButton();
 						unusedRadio.setActionCommand("-1");
 						unusedRadio.setSelected(true);
 						Box unusedBox = Box.createVerticalBox();
 						
 						// build parts selector
 						JPanel PSelectorContainer = new JPanel();
 						PSelectorContainer.setLayout(new GridLayout(1,n));
 						selectedParts[i] = new ButtonGroup();
 						selectedParts[i].add(unusedRadio);
 						unusedBox.add(unused);
 						unusedBox.add(unusedRadio);
 						PSelectorContainer.add(unusedBox);
 						for(Integer j : parts.keySet()){
 								Parts current = parts.get(j);
 								Box box2 = Box.createVerticalBox();
 								JLabel temp = new JLabel("  ",images.getIcon(current.getImageIndex()-1),SwingConstants.LEFT);
 								temp.setHorizontalTextPosition(SwingConstants.LEFT);
 								JRadioButton partButton = new JRadioButton();
 								partButton.setActionCommand(Integer.toString(current.getMapIndex()));
 								selectedParts[i].add(partButton);
 								box2.add(temp);
 								box2.add(partButton);
 								PSelectorContainer.add(box2);
 						}
 						holder.add(partLabel, BorderLayout.WEST);
 						holder.add(PSelectorContainer, BorderLayout.CENTER);
 						partsSelectPanel.add(holder);
 				}
 				partsSelectPanel.add(spWarning);
 				partsSelectPanel.revalidate();
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ---------------------------------------------- Helpers ------------------------------- //
 		// -------------------------------------------------------------------------------------- //
 		
 		private void setComponentSize(JComponent component, int w, int h){
 				component.setMinimumSize(new Dimension(w,h));
 				component.setMaximumSize(new Dimension(w,h));
 				component.setPreferredSize(new Dimension(w,h));
 				component.setAlignmentX(Component.LEFT_ALIGNMENT);
 		}
 		
 		private void resetKitStruct(){
 				kitName.setText("Enter Kit Name");
 				kitID.setText("Enter Kit ID");
 				kitDesc.setText("Enter a short kit description");
 		}
 		
 		private void buildEditKit(Kits selectedKit){
 				editKitNumber = selectedKit.getMapIndex();
 				System.out.println("ekn: "+editKitNumber);
 				kitName.setText(selectedKit.getName());
 				kitID.setText(Integer.toString(selectedKit.getKitID()));
 				kitDesc.setText(selectedKit.getDescription());
 				
 				// select the appropriate radio buttons
 				// to know how many items to shift, we are going to use the button's action command and then match it with the II stored in the part
 				TreeMap<Integer, Parts> kitParts = selectedKit.getListOfParts();
 				for(Integer i : kitParts.keySet()){
 						JRadioButton temp;
 						int partLocation = kitParts.get(i).getMapIndex();
 						Enumeration e = selectedParts[i.intValue()].getElements();
 						// this loop will select the button we need to set as picked
 						while(e.hasMoreElements()){
 								temp = (JRadioButton)e.nextElement();
 								if(Integer.parseInt(temp.getActionCommand()) == partLocation){
 										temp.setSelected(true);
 										break;
 								}
 						}
 				}
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ----------------------------------- Network Manager ---------------------------------- //
 		// -------------------------------------------------------------------------------------- //
 		
 		// server specific
 		public void registerClientListener(NetworkBridge newBridge, int cID){}
 		public void syncFrame(){}
 		public void updateBuildData(ArrayList<Kits> buildData){}
 		public void updateBreakData(String breakCommand, int cID, int x){}
 		
 		// client specific
 		public void mergeChanges(ArrayList<TreeMap<Integer, Boolean>> mapArray, ArrayList<TreeMap<Integer, FactoryObject>> dataArray){}
 		
 		public void syncChanges(ArrayList<TreeMap<Integer,FactoryObject>> dataArray){}
 		
 		// global
 		public void updatePartData(TreeMap<Integer, Parts> partData){
 				parts = partData;
 				
 				// validate kits and make sure all the parts are still present
 				
 		}
 		public void updateKitData(TreeMap<Integer, Kits> kitData){
 				if(kitData != null){
 						kits = kitData;
 						populateActiveKitList();
 				}
 		}
 		public void closeNetworkBridge(int bridgeID){
 				nb1.close();
 		}
 		
 		// -------------------------------------------------------------------------------------- //
 		// ----------------------------------- End Network Manager ------------------------------ //
 		// -------------------------------------------------------------------------------------- //
 }
