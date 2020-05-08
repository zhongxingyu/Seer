 package divination;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import org.powerbot.event.PaintListener;
 import org.powerbot.script.PollingScript;
 import org.powerbot.script.methods.MethodContext;
 import org.powerbot.script.wrappers.Tile;
 import org.powerbot.script.util.Timer;
 
 import divination.DivineData.animation;
 import divination.DivineData.convert;
 import divination.DivineData.memories;
 import divination.DivineData.wisps;
 
 
 
 @org.powerbot.script.Manifest(authors = { "Delta Scripter" }, name = "Delta Divinity", 
 description = "Trains the Divination skill, harvests and converts energy to your choosing.",
 website = "", version = 1.01)
 public class DivineBody extends PollingScript implements PaintListener{
 
 	public DivineBody(){
 		getExecQueue(State.START).add(new Runnable() {
 			@Override
 			public void run() {
 				initiateGUI();
 				runtime = new Timer(0);
 				secondsA = new Timer(0);
 				addNode(new harvestWisp(ctx));
 				addNode(new convertMemories(ctx));
 			}
 		});
 	}
 	
 	private final List<DivineNode> nodeList = Collections.synchronizedList(new ArrayList<DivineNode>());
 	public static String state;
 	private boolean harvest = false;
 	private int convertType;
 	private int animationType;
 	private String wispKind;
 	private String wispSpring;
 	private String memoryType;
 	private Tile riftArea;
 	private int paleECount;
 	private boolean start = false;
 	private Timer waiting = new Timer(0);
 	private Timer runtime;
 	private Timer secondsA;
 	
 	DivineMethod Method = new DivineMethod(ctx);
 	
 	
 	
 	@Override
 	public int poll() {
 		
 		if(start){
 		for(DivineNode node: nodeList){
 			if(node.activate()){
 				node.execute();
 				
 			}
 		}
 		}
 		return 400;
 	}
 	
 	   private void addNode(final DivineNode...nodes) {
 		   
 	        for(DivineNode node : nodes) {
 	            if(!this.nodeList.contains(node)) {
 	                this.nodeList.add(node);
 	            }
 	        }
 	    }
 	   class convertMemories extends DivineNode{
 
 		public convertMemories(MethodContext ctx) {
 			super(ctx);
 		}
 
 		@Override
 		public boolean activate() {
 			return !harvest;
 		}
 
 		@Override
 		public void execute() {
 			while(riftArea==null && Method.objIsNotNull("Energy Rift")){
 				state = "Setting rift area";
 				riftArea = Method.getObject("Energy Rift").getLocation();
 				riftArea = new Tile(riftArea.getX(), riftArea.getY()+3,riftArea.getPlane());
 			}
 			
 			while(ctx.players.local().getAnimation()==animationType){
 				updateCounts();
 				state = "Converting memories..";
 				waiting = new Timer(4000);
 			}
 			if(!waiting.isRunning())
 			if(!Method.inventoryContains(memoryType)){
 				harvest = true;
 			}else
 			if(closeToObj(riftArea,"Walking to rift")){
 				if(ctx.widgets.get(131,convertType).isVisible()){
 					if(convertType == 7 && ctx.widgets.get(131,39).getTextureId()==13827){
 						ctx.widgets.get(131,6).click();
 					}else ctx.widgets.get(131,convertType).click();
 					ctx.game.sleep(3800,4500);
 				}else if(ctx.widgets.get(1186,2).isVisible()){
 					state = "Closing dialogue";
 					Method.clickOnMap(ctx.players.local().getLocation());
 				}else Method.interactO("Energy Rift", "Convert memories", "Interacting with rift");
 			}
 			
 		}
 	   }
 		class harvestWisp extends DivineNode{
 
 			public harvestWisp(MethodContext ctx) {
 				super(ctx);
 			}
 
 			@Override
 			public boolean activate() {
 				return harvest;
 			}
 			@Override
 			public void execute() {
 				while(ctx.players.local().getAnimation()==animation.HARVESTINGSPRING2.getID()){
 					state = "Harvesting spring";
 					updateCounts();
 					ctx.game.sleep(700,1500);
 				}
 				while(Method.backPackIsFull()){
 					state = "backpack is full";
 					harvest = false;
 					break;
 				}
 				
 				if(Method.npcIsNotNull("Enriched glowing spring")){
 					System.out.println("Found an enriched spring!");
 					if(closeToNpc("Enriched glowing spring","Walking to enriched spring")){
 						state = "Attempting to harvest enriched spring";
 						Method.npcInteract("Enriched glowing spring", "Harvest");
 					}
 				}else
 				if(Method.npcIsNotNull(wispSpring)){
 					if(closeToNpc(wispSpring,"Walking to spring")){
 						state = "Attempting to harvest spring";
 						Method.npcInteract(wispSpring, "Harvest");
 					}
 				}else
 				if(Method.npcIsNotNull(wispKind)){
 					if(closeToNpc(wispKind,"Walking to wisp")){
 						state = "Attempting to convert wisp to spring";
 						Method.npcInteract(wispKind, "Harvest");
 					}
 				}else ctx.movement.findPath(riftArea).traverse();
 				
 			}
 			
 		
 
 			private boolean closeToNpc(String name, String string) {
 				if(Method.getNPC(name).getLocation().distanceTo(ctx.players.local().getLocation())<7){
 					return true;
 				}else {
 					state = string;
 					ctx.movement.findPath(Method.getNPC(name).getLocation()).traverse();
 					ctx.game.sleep(2000,2400);
 				}
 				return false;
 			}
 			
 		}
 		private void updateCounts() {
 			paleECount = Method.inventoryGetCount(memoryType);
 			
 		}
 		private boolean closeToObj(Tile loc, String string) {
 			if(loc.distanceTo(ctx.players.local().getLocation())<5){
 				return true;
 			}else if(loc.distanceTo(ctx.players.local().getLocation())<8){
 				Method.clickOnMap(loc);
 			}else{
 				state = string;
 				ctx.movement.findPath(loc).traverse();
 			}
 			return false;
 		}
 	  
 private Font myFont = new Font("Consolas",Font.BOLD,14);
 
 
 
 	@Override
 	public void repaint(Graphics g) {
 		int seconds = (int)(runtime.getElapsed()/1000);
 		int minutes = (int)(seconds/60);
 		int hours = (int)(minutes/60);
 		int secHold = (int)(secondsA.getElapsed()/1000);
 		if(secHold>=60)
 			secondsA = new Timer(0);
 		
 		
 		g.setFont(myFont);
 		g.setColor(Color.green);
 		g.drawString("State: "+state, 20, 130);
		g.drawString("Runtime: " +hours+":"+minutes +":" + secHold, 20, 150);
 		g.drawString("Energy in inventory: " + paleECount, 20, 170);
 	}
 	public void initiateGUI() {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				
 				final DeltaDivinityGUI deltagui = new DeltaDivinityGUI();
 				deltagui.setVisible(true);
 				deltagui.setResizable(false);
 			
 			}
 		});
 	}
 	class DeltaDivinityGUI extends JFrame {
 		public DeltaDivinityGUI() {
 			initComponents();
 		}
 
 		private void strtBtnActionPerformed(ActionEvent e) {
 			String convChoice = convertList.getSelectedItem().toString();
 			String riftChoice = riftLocation.getSelectedItem().toString();
 			if(convChoice=="Divine Energy"){
 				convertType = convert.DIVINEENERGY.getID();
 				animationType = animation.TODIVINEENERGY.getID();
 			}
 			if(convChoice=="Divinity Experience"){
 				convertType = convert.DIVINITYEXP.getID();
 				animationType = animation.TODIVINEEXP.getID();
 			}
 			if(convChoice=="Enhanced Experience"){
 				convertType = convert.BOTH.getID();
 				animationType = animation.TODIVINEEXP.getID();
 			}
 			if(riftChoice=="Lummbridge"){
 				wispKind = wisps.PALEWISP.getName();
 				wispSpring = wisps.PALESPRING.getName();
 				memoryType = memories.PALEMEMORY.getName();
 			}
 			if(riftChoice=="Falador"){
 				wispKind = wisps.FLICKERINGWISP.getName();
 				wispSpring = wisps.FLICKERINGSPRING.getName();
 				memoryType = memories.FLICKERINGMEMORY.getName();
 			}
 			if(riftChoice=="Varrock"){
 				wispKind = wisps.BRIGHTWISP.getName();
 				wispSpring = wisps.BRIGHTSPRING.getName();
 				memoryType = memories.BRIGHTMEMORY.getName();
 			}
 			if(riftChoice=="Seers' Village"){
 				wispKind = wisps.GLOWINGWISP.getName();
 				wispSpring = wisps.GLOWINGSPRING.getName();
 				memoryType = memories.GLOWINGMEMORY.getName();
 			}
 			if(riftChoice=="Golden Apple Tree"){
 				wispKind = wisps.SPARKLINGWISP.getName();
 				wispSpring = wisps.SPARKLINGSPRING.getName();
 				memoryType = memories.SPARKLINGMEMORY.getName();
 			}
 			if(riftChoice=="Shilo Village"){
 				wispKind = wisps.GLEAMINGWISP.getName();
 				wispSpring = wisps.GLEAMINGSPRING.getName();
 				memoryType = memories.GLEAMINGMEMORY.getName();
 			}
 			if(riftChoice=="Mobilising Armies"){
 				wispKind = wisps.VIBRANTWISP.getName();
 				wispSpring = wisps.VIBRANTSPRING.getName();
 				memoryType = memories.VIBRANTMEMORY.getName();
 			}
 			if(riftChoice=="Slayer Tower"){
 				wispKind = wisps.LUSTROUSWISP.getName();
 				wispSpring = wisps.LUSTROUSSPRING.getName();
 				memoryType = memories.LUSTROUSMEMORY.getName();
 			}
 			if(riftChoice=="Mage Training Arena"){
 				wispKind = wisps.BRILLIANTWISP.getName();
 				wispSpring = wisps.BRILLIANTSPRING.getName();
 				memoryType = memories.BRILLIANTMEMORY.getName();
 			}
 			start = true;
 			this.dispose();
 		}
 
 		private void initComponents() {
 			label1 = new JLabel();
 			convertList = new JComboBox<>();
 			label2 = new JLabel();
 			riftLocation = new JComboBox<>();
 			strtBtn = new JButton();
 
 			//======== this ========
 			setTitle("DeltaDivinity");
 			Container contentPane = getContentPane();
 
 			//---- label1 ----
 			label1.setText("Convert memories into: ");
 
 			//---- convertList ----
 			convertList.setModel(new DefaultComboBoxModel<>(new String[] {
 					"Divine Energy","Divinity Experience","Enhanced Experience"
 			}));
 
 			//---- label2 ----
 			label2.setText("Location of rift: ");
 
 			//---- riftLocation ----
 			riftLocation.setModel(new DefaultComboBoxModel<>(new String[] {
 				"Lummbridge","Falador","Varrock","Seers' Village",
 				"Golden Apple Tree","Shilo Village","Mobilising Armies",
 				"Slayer Tower","Mage Training Arena"
 			}));
 
 			//---- strtBtn ----
 			strtBtn.setText("START");
 			strtBtn.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					strtBtnActionPerformed(e);
 				}
 			});
 
 			GroupLayout contentPaneLayout = new GroupLayout(contentPane);
 			contentPane.setLayout(contentPaneLayout);
 			contentPaneLayout.setHorizontalGroup(
 				contentPaneLayout.createParallelGroup()
 					.addGroup(contentPaneLayout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
 							.addComponent(strtBtn)
 							.addGroup(contentPaneLayout.createSequentialGroup()
 								.addGroup(contentPaneLayout.createParallelGroup()
 									.addComponent(label1)
 									.addComponent(label2))
 								.addGap(19, 19, 19)
 								.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
 									.addComponent(convertList, GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
 									.addComponent(riftLocation, GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))))
 						.addContainerGap(3, Short.MAX_VALUE))
 			);
 			contentPaneLayout.setVerticalGroup(
 				contentPaneLayout.createParallelGroup()
 					.addGroup(contentPaneLayout.createSequentialGroup()
 						.addGap(19, 19, 19)
 						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 							.addComponent(convertList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addComponent(label1))
 						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 							.addComponent(riftLocation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addComponent(label2))
 						.addGap(18, 18, 18)
 						.addComponent(strtBtn)
 						.addContainerGap(6, Short.MAX_VALUE))
 			);
 			pack();
 			setLocationRelativeTo(getOwner());
 		}
         private JLabel label1;
 		private JComboBox<String> convertList;
 		private JLabel label2;
 		private JComboBox<String> riftLocation;
 		private JButton strtBtn;
 	}
 
 	
 
 }
