 package factory.server;
 
 import java.awt.event.*;
 import java.awt.*;
 import javax.swing.*;
 import java.util.ArrayList;
 
 import factory.global.data.*;
 import factory.server.managers.GuiManager;
 import factory.server.managers.laneManager.*;
 import factory.server.managers.kitAssemblyManager.*;
 import factory.server.managers.gantryManager.*;
 import factory.server.managers.factoryState.*;
 
 public class ServerControl extends JPanel implements ActionListener{
 	
 	UpdateServer KitASM;
 	LaneManager LM;
 	GantryManager GM;
 	FactoryState fs;
 	
 	JPanel overPanel = new JPanel();
 	JPanel laneControl = new JPanel();
 	JPanel kitAssemblyControl = new JPanel();
 	JPanel lComboBoxPanel1 = new JPanel();
 	JPanel lComboBoxPanel2 = new JPanel();
 	JPanel[] kComboBoxPanel = new JPanel[4];
 	JPanel laneButtonPanel = new JPanel();
 	JPanel gantryControl = new JPanel();
 	JPanel gComboBoxPanel1 = new JPanel();
 	JPanel gComboBoxPanel2 = new JPanel();
 	JTabbedPane tp = new JTabbedPane();
 	FactoryView fv = new FactoryView();
 	
 	JButton conveyorToStand = new JButton("Move from Conveyor to First Empty Stand");
 	JButton build1 = new JButton("Build Kit on 1st Stand");
 	JButton build2 = new JButton("Build Kit 2nd Stand");
 	JButton inspectionPicture = new JButton("Take Picture of Kit on Inspection Stand");
 	JButton inspection = new JButton("Move Kit on 1st Stand to Inspection Stand");
     JButton inspection2 = new JButton("Move Kit on 2nd Stand to Inspection Stand");
 	JButton standToConveyor = new JButton("Move Good Kit from Inspection Stand to Conveyor or Trash Bad Kit");
 	JLabel kitAssemblyDesc = new JLabel("Kit Assembly Manager");
 	JComboBox[] indexChooser = new JComboBox[4];
 	String[] indexStrings = new String[9];
 	JComboBox[] nestChooser = new JComboBox[4];
 	String[] nestStrings = new String[9];
 	JButton bringKit = new JButton("Bring Kit in on Conveyor");
 	JButton takeKit = new JButton("Take Kit out on Conveyor");
 	JLabel kamdropDownInfo = new JLabel("Get Part from Selected Nests and Add that Part to the Selected Section of the Kit for Build Kit Buttons");
 	JLabel badKitPicLabel = new JLabel("No Kit Picture Data");
 	
 	JLabel laneDesc = new JLabel("Lane Manager");
 	String[] laneStrings = new String[8];
 	JComboBox laneChooser;
 	JComboBox feederChooser2;
 	JButton toggleLane = new JButton("Turn Selected Lane On/Off");
 	JButton toggleDivider = new JButton("Toggle Divider in Selected Feeder Up/Down");
 	JButton nestPicture = new JButton("Picture of Nest of Selected Lane");
 	JButton purgeNest = new JButton("Purge Nest of Selected Lane");
 	JButton purgeLane = new JButton("Purge Selected Lane");
 	JButton purgeFeeder = new JButton("Purge Selected Feeder");
 	JLabel badNestPicLabel = new JLabel("No Nest Picture Data");
 	
 	JLabel gantryControlDesc = new JLabel("Gantry Manager");
 	JComboBox binChooser;
 	JComboBox feederChooser;
 	String[] binStrings = new String[10];
 	String[] feederStrings = new String[4];
 	JButton moveToBin = new JButton("Move to Selected Bin");
 	JButton moveToFeeder = new JButton("Move to Selected Feeder");
 	Server server;
 	
 	public ServerControl(GuiManager kit, GuiManager LM, GuiManager GM, FactoryState fs, Server serv){
 		server = serv;
 		
 		this.KitASM = (UpdateServer)kit;
 		this.LM = (LaneManager)LM;
 		this.GM = (GantryManager)GM;
 		this.fs = fs;
 		
 		for (int i = 1; i < laneStrings.length+1; i++){
 				laneStrings[i-1] = "Lane " + i;
 		}
 		for (int i = 0; i < indexStrings.length; i++){
 			if (i == 0)
 				indexStrings[i] = "Section -";
 			else
 				indexStrings[i] = "Section " + i;
 		}
 		for (int i = 0; i < nestStrings.length; i++){
 			if (i == 0)
 				nestStrings[i] = "Nest -";
 			else
 				nestStrings[i] = "Nest " + i;
 		}
 		for (int i = 1; i < binStrings.length+1; i++){
 			binStrings[i-1] = "Bin " + i;
 		}
 		for (int i = 1; i < feederStrings.length+1; i++){
 			feederStrings[i-1] = "Feeder " + i;
 		}
 		laneChooser = new JComboBox(laneStrings);
 		for (int i = 0; i < nestChooser.length; i++)
 			nestChooser[i] = new JComboBox(nestStrings);
 		for (int i = 0; i < indexChooser.length; i++)
 			indexChooser[i] = new JComboBox(indexStrings);
 		for (int i = 0; i < kComboBoxPanel.length; i++)
 			kComboBoxPanel[i] = new JPanel();
 		binChooser = new JComboBox(binStrings);
 		feederChooser = new JComboBox(feederStrings);
 		feederChooser2 = new JComboBox(feederStrings);
 		
 		overPanel.setLayout(new BoxLayout(overPanel,BoxLayout.X_AXIS));
 		kitAssemblyControl.setLayout(new BoxLayout(kitAssemblyControl,BoxLayout.Y_AXIS));
 		for (int i = 0; i < kComboBoxPanel.length; i++)
 			kComboBoxPanel[i].setLayout(new FlowLayout());
 		laneControl.setLayout(new BoxLayout(laneControl,BoxLayout.Y_AXIS));
 		lComboBoxPanel1.setLayout(new FlowLayout());
 		lComboBoxPanel2.setLayout(new FlowLayout());
 		laneButtonPanel.setLayout(new BoxLayout(laneButtonPanel,BoxLayout.Y_AXIS));
 		gantryControl.setLayout(new BoxLayout(gantryControl,BoxLayout.Y_AXIS));
 		gComboBoxPanel1.setLayout(new FlowLayout());
 		gComboBoxPanel2.setLayout(new FlowLayout());
 		
 		
 		conveyorToStand.addActionListener(this);
 		build1.addActionListener(this);
 		build2.addActionListener(this);
 		inspectionPicture.addActionListener(this);
 		inspection.addActionListener(this);
         inspection2.addActionListener(this);
 		standToConveyor.addActionListener(this);
 		bringKit.addActionListener(this);
 		takeKit.addActionListener(this);
 		
 		toggleLane.addActionListener(this);
 		toggleDivider.addActionListener(this);
 		nestPicture.addActionListener(this);
 		purgeNest.addActionListener(this);
 		purgeLane.addActionListener(this);
 		purgeFeeder.addActionListener(this);
 		
 		moveToBin.addActionListener(this);
 		moveToFeeder.addActionListener(this);
 		
 		for (int i = 0; i < kComboBoxPanel.length; i++){
 			kComboBoxPanel[i].add(nestChooser[i]);
 			kComboBoxPanel[i].add(indexChooser[i]);
 		}
 		kitAssemblyControl.add(kitAssemblyDesc);
 		kitAssemblyControl.add(bringKit);
 		kitAssemblyControl.add(conveyorToStand);
 		kitAssemblyControl.add(inspection);
 		kitAssemblyControl.add(inspection2);
 		kitAssemblyControl.add(inspectionPicture);
 		kitAssemblyControl.add(standToConveyor);
 		kitAssemblyControl.add(takeKit);
 		kitAssemblyControl.add(new JSeparator(SwingConstants.HORIZONTAL));
 		kitAssemblyControl.add(kamdropDownInfo);
 		for (int i = 0; i < kComboBoxPanel.length; i++)
 			kitAssemblyControl.add(kComboBoxPanel[i]);
 		kitAssemblyControl.add(build1);
 		kitAssemblyControl.add(build2);
        
 		kitAssemblyControl.add(badKitPicLabel);
 		
 		lComboBoxPanel1.add(laneChooser);
 		lComboBoxPanel2.add(feederChooser2);
 		laneControl.add(laneDesc);
 		laneControl.add(lComboBoxPanel1);
 		laneButtonPanel.add(toggleLane);
 		laneButtonPanel.add(nestPicture);
 		laneButtonPanel.add(purgeNest);
 		laneButtonPanel.add(purgeLane);
 		laneControl.add(laneButtonPanel);
 		laneControl.add(new JSeparator(SwingConstants.HORIZONTAL));
 		laneControl.add(lComboBoxPanel2);
 		laneControl.add(toggleDivider);
 		laneControl.add(badNestPicLabel);
 		
 		gComboBoxPanel1.add(binChooser);
 		gComboBoxPanel2.add(feederChooser);
 		gantryControl.add(gantryControlDesc);
 		gantryControl.add(gComboBoxPanel1);
 		gantryControl.add(moveToBin);
 		gantryControl.add(new JSeparator(SwingConstants.HORIZONTAL));
 		gantryControl.add(gComboBoxPanel2);
 		gantryControl.add(moveToFeeder);
 		gantryControl.add(purgeFeeder);
 		
 		tp.addTab("Kit Assembly",kitAssemblyControl);
 		tp.addTab("Lane M",laneControl);
 		tp.addTab("Factory Production M",fv);
 		tp.addTab("Gantry M",gantryControl);
 		
 		overPanel.add(tp);
 		
 		add(tp);
 	}
 	
 	public void actionPerformed(ActionEvent e){
 		if (e.getSource() == bringKit){
 			if (KitASM.isFinished()){
 				KitASM.bringKit();
 			}
 		}
 		if (e.getSource() == conveyorToStand){
 			if (KitASM.isFinished()){
 
 				for (int i = 0; i < 2; i++){
 					if (KitASM.getStands().get(i).getKit() == null){
 						KitASM.moveToStand(i);
 						break;
 					}
 				}
 			}
 		}
 		
 		if (e.getSource() == build1){
 			server.sync = true;
             if (KitASM.isFinished() ){
 				int[] nests = new int[4];
 				int[] indexes = new int[4];
 				for (int i = 0; i < 4; i++){
 					nests[i] = -1;
 					indexes[i] = -1;
 				}
 				for (int i = 0; i < nestChooser.length; i++){
 					String nest = (String)nestChooser[i].getSelectedItem();
 					nest = nest.substring(5);
 					if (!nest.equals("-")){
 						int j = Integer.parseInt(nest);
 						j -= 1;
 						nests[i] = j;
 					}
 				}
 				for (int i = 0; i < indexChooser.length; i++){
 					String index = (String)indexChooser[i].getSelectedItem();
 					index = index.substring(8);
 					if (!index.equals("-")){
 						int j = Integer.parseInt(index);
 						j -= 1;
 						indexes[i] = j;
 					}
 				}
				if (goodArray(nests,indexes))
					KitASM.movePartstoStand(0, 0, nests, indexes);
 			}
 		}
 		if (e.getSource() == build2){
 			server.sync = true;
             if (KitASM.isFinished() ){
 				int[] nests = new int[4];
 				int[] indexes = new int[4];
 				for (int i = 0; i < 4; i++){
 					nests[i] = -1;
 					indexes[i] = -1;
 				}
 				for (int i = 0; i < nestChooser.length; i++){
 					String nest = (String)nestChooser[i].getSelectedItem();
 					nest = nest.substring(5);
 					if (!nest.equals("-")){
 						int j = Integer.parseInt(nest);
 						j -= 1;
 						nests[i] = j;
 					}
 				}
 				for (int i = 0; i < indexChooser.length; i++){
 					String index = (String)indexChooser[i].getSelectedItem();
 					index = index.substring(8);
 					if (!index.equals("-")){
 						int j = Integer.parseInt(index);
 						j -= 1;
 						indexes[i] = j;
 					}
 				}
				if (goodArray(nests,indexes))
					KitASM.movePartstoStand(0, 1, nests, indexes);
 			}
 		}
 		
 		if (e.getSource() == inspectionPicture){
 			if (KitASM.isFinished()){
 				KitASM.takePic();
 				if (KitASM.isBadKit())
 					badKitPicLabel.setText("Kit missing parts");
 				else
 					badKitPicLabel.setText("Kit is complete");
 			}
 		}
 		if (e.getSource() == inspection){
 			if (KitASM.isFinished() ){
 				KitASM.moveToInspection(0);
             }
 			
 		}
         if (e.getSource() == inspection2){
                 if (KitASM.isFinished() ){
                     KitASM.moveToInspection(1);
                     
                 }
         }
 		if (e.getSource() == standToConveyor){
 			if (KitASM.isFinished() ){
 				KitASM.takeToConveyor();
 			}
 		}
 		if (e.getSource() == takeKit){
 			if (KitASM.isFinished() ){
 				KitASM.takeKit();
 			}
 		}
 		if (e.getSource() == toggleLane){
 			server.sync = true;
 			String l = (String)laneChooser.getSelectedItem();
 			l = l.substring(5);
 			int lane = Integer.parseInt(l);
 			lane -= 1;
 			LM.laneToggle(lane);
 		}
 		if (e.getSource() == nestPicture){
 			server.sync = true;
 			String n = (String)laneChooser.getSelectedItem();
 			n = n.substring(5);
 			int nest = Integer.parseInt(n);
 			nest -= 1;
 			int nest1 = nest + 1;
 			LM.takePicture(nest);
 			if (LM.testNest(nest))
 				badNestPicLabel.setText("Nest " + nest1 + " is good");
 			else
 				badNestPicLabel.setText("Nest " + nest1 + " is bad");
 		}
 		if (e.getSource() == moveToBin){
 			String b = (String)binChooser.getSelectedItem();
 			b = b.substring(4);
 			int bin = Integer.parseInt(b);
 			bin -= 1;
 			GM.robot.moveToBin(bin);
 		}
 		if (e.getSource() == moveToFeeder){
 			String f = (String)feederChooser.getSelectedItem();
 			f = f.substring(7);
 			int feeder = Integer.parseInt(f);
 			feeder -= 1;
 			GM.robot.moveToFeeder(feeder);
 		}
 		if (e.getSource() == purgeNest){
 			server.sync = true;
 			String n = (String)laneChooser.getSelectedItem();
 			n = n.substring(5);
 
 			if (!n.equals("-")){
 				int nest = Integer.parseInt(n);
 				nest -= 1;
 				LM.purgeNest(nest);
 			}
 			
 		}
 		if (e.getSource() == purgeLane){
 			server.sync = true;
 			String l = (String)laneChooser.getSelectedItem();
 			l = l.substring(5);
 			int lane = Integer.parseInt(l);
 			lane -= 1;
 			LM.purgeLane(lane);
 		}
 		if (e.getSource() == purgeFeeder){
 			server.sync = true;
 			String f = (String)feederChooser.getSelectedItem();
 			f = f.substring(7);
 			int feeder = Integer.parseInt(f);
 			feeder -= 1;
 			GM.robot.removeBin(feeder);
 			LM.removeBin(feeder);
 		}
 		if (e.getSource() == toggleDivider){
 			server.sync = true;
 			String f = (String)feederChooser2.getSelectedItem();
 			f = f.substring(7);
 			int feeder = Integer.parseInt(f);
 			feeder -= 1;
 			LM.dividerToggle(feeder);
 		}
 		/*
 		if (e.getSource() == addBin){
 			server.sync = true;
 			String f = (String)feederChooser.getSelectedItem();
 			f = f.substring(7);
 			int feeder = Integer.parseInt(f);
 			feeder -= 1;
 			String b = (String)binChooser.getSelectedItem();
 			b = b.substring(4);
 			int bin = Integer.parseInt(b);
 			bin -= 1;
 			LM.addToFeeder(feeder,bin,36);
 		}
 		*/
 	}
 	
 	public void updateFactoryView(ArrayList<Kits> kits){
 		fv.setKitData(kits);
 	}
	
	public boolean goodArray(int[] ar1, int[] ar2){
		for (int i = 0; i < ar1.length; i++){
			if (ar1[i] == -1){
				for (int j = i+1; j < ar1.length; j++){
					if (ar1[j] != -1)
						return false;
				}
			}
		}
		for (int i = 0; i < ar1.length; i++){
			if ((ar1[i] == -1 && ar2[i] != -1) || (ar1[i] != -1 && ar2[i] == -1))
				return false;
		}
		return true;
	}
 		
 }
