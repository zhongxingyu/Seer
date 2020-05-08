 //Minh La
 
 package factory.graphics;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import factory.Bin;
 import factory.BinConfig;
 import factory.FeederAgent;
 import factory.GantryAgent;
 import factory.LaneAgent;
 import factory.NestAgent;
 import factory.Part;
 import factory.FeederAgent.DiverterState;
 import factory.FeederAgent.FeederState;
 import factory.VisionAgent;
 import factory.interfaces.Feeder;
 import factory.test.mock.MockGantry;
 
 
 public class GraphicLaneMenuPanel extends JPanel implements ActionListener {
 	// Minh's Code:
 	JButton startLaneButton, switchLaneButton, placeBinButton,stopLaneButton,dumpNestButton, feederOnButton,feederOffButton, purgeFeederButton;
 
 
 	// Ryan Cleary's Code
 	JButton normButton;
 	JButton topNestNeedsEar, topNestNeedsHelmet, bottomNestNeedsEar, bottomNestNeedsHelmet, resetButton;
 	FeederAgent feeder;
 	LaneAgent top, bottom;
 	NestAgent n0,n1,n2,n3;
 	GantryAgent gantry;
 	VisionAgent vision;
 	Part p0,p1,p2;
 
 
 	protected void setUp() {
 		feeder = new FeederAgent("feeder",0);
 		feeder.startThread();
 		feeder.glmp = this;
		/*top = new LaneAgent();
 		top.startThread();
 		bottom = new LaneAgent();
 		bottom.startThread();
 		gantry = new GantryAgent();
 		gantry.glmp = this;
 		gantry.startThread();
 		vision = new VisionAgent(null,null,null);
 		vision.startThread();
 		feeder.vision = vision;
 
 		n0 = new NestAgent();
 		n0.startThread();
 		n1 = new NestAgent();
 		n1.startThread();
 
 		n2 = new NestAgent();
 		n2.startThread();
 
 		n3 = new NestAgent();
 		n3.startThread();
 
 		p0 = new Part("ear");
 		p0.averageDelayTime = 15; // it takes 20 seconds for this part to go all the way down the lane and then 
 		// start stacking up.  after 20 seconds, the feeder should stop feeding.
 		p1 = new Part("helmet");
 		p1.averageDelayTime = 12; 
 		p2 = new Part("ear");
 		p2.averageDelayTime = 10;
 
 		n0.setLane(top);
 		top.setNest(n0);
 		top.setFeeder(feeder);
 
 		n1.setLane(bottom);
 		bottom.setNest(n1);
 		bottom.setFeeder(feeder);
 
 		feeder.setUpLanes(top, bottom);
 		feeder.setGantry(gantry); //for now
 		feeder.diverter = DiverterState.FEEDING_BOTTOM;
 
 
 		Bin bin0 = new Bin(p0);
 		HashMap<Bin, Integer> binMap = new HashMap<Bin, Integer>(); 
 		Bin bin1 = new Bin(p1);
 		binMap.put(bin0, 0);
 		binMap.put(bin1, 1);
 
		gantry.msgChangeGantryBinConfig(new BinConfig(binMap)); */
 	}
 
 
 	GraphicLaneManager lane;
 	GraphicBin bin;
 
 	public GraphicLaneMenuPanel(GraphicLaneManagerClient laneManager){
 		this.lane = laneManager.getLane(0);
 
 		// This is our button for the normative, v.0 case.  We currently have other buttons in development, 
 		// but they are not guaranteed to work perfectly, as this is a work in progress.
 		normButton = new JButton("v.0 Normative Case");
 		normButton.addActionListener(this);
 
 		topNestNeedsEar = new JButton("Top Nest: Ear");
 		topNestNeedsEar.addActionListener(this);
 
 		topNestNeedsHelmet = new JButton("Top Nest: Helmet");
 		topNestNeedsHelmet.addActionListener(this);
 
 		bottomNestNeedsEar = new JButton("Bottom Nest: Ear");
 		bottomNestNeedsEar.addActionListener(this);
 
 		bottomNestNeedsHelmet = new JButton("Bottom Nest: Helmet");
 		bottomNestNeedsHelmet.addActionListener(this);
 
 		resetButton = new JButton("Reset");
 		resetButton.addActionListener(this);
 
 
 
 //		startLaneButton = new JButton("Start Lane");
 //		startLaneButton.addActionListener(this);
 //		switchLaneButton = new JButton("Switch Lane");
 //		switchLaneButton.addActionListener(this);
 //		stopLaneButton = new JButton("Stop Lane");
 //		stopLaneButton.addActionListener(this);
 //		placeBinButton = new JButton("Place Bin");
 //		placeBinButton.addActionListener(this);
 //		feederOnButton = new JButton("Feeder On");
 //		feederOnButton.addActionListener(this);
 //		feederOffButton = new JButton("Feeder Off");
 //		feederOffButton.addActionListener(this);
 		purgeFeederButton = new JButton("Purge Feeder");
 		purgeFeederButton.addActionListener(this);
 		dumpNestButton = new JButton("Dump Nest");
 		dumpNestButton.addActionListener(this);
 
 		this.setPreferredSize(new Dimension(400,720));
 		this.setVisible(true);
 
 		this.add(normButton);
 		this.add(topNestNeedsEar);
 		this.add(topNestNeedsHelmet);
 		this.add(bottomNestNeedsEar);
 		this.add(bottomNestNeedsHelmet);
 		this.add(resetButton);
 
 		setUp();
 
 //		this.add(placeBinButton);
 //		this.add(startLaneButton);
 //		this.add(stopLaneButton);
 //		this.add(switchLaneButton);
 //		this.add(feederOnButton);
 //		this.add(feederOffButton);
 		this.add(purgeFeederButton);
 
 
 		/* CASES TO TEST:
 		 * 1) Single request.
 		 * 2) Two different part requests, different lanes
 		 * 3) Two different part requests, same lane.
 		 * 4) Two of the same kind of part, different lanes.
 		 * 5) Two of the same kind of part, same lane.
 		 */
 
 
 		//singlePartRequest(); // TEST CASE #1
 		//twoDifferentPartRequestsForDifferentLanes(); // TEST CASE #2
 		//twoDifferentPartRequestsForSameLane(); // TEST CASE #3
 		//twoSamePartRequestsForDifferentLanes(); // TEST CASE #4
 		//twoSamePartRequestsForSameLanes(); // TEST CASE #5
 	}
 
 	
 	
 	
 	public void singlePartRequest() {
 		Bin bin0 = new Bin(p0);
 		HashMap<Bin, Integer> binMap = new HashMap<Bin, Integer>(); 
 		binMap.put(bin0, 0);
 
 
 		gantry.msgChangeGantryBinConfig(new BinConfig(binMap));
 		n0.msgYouNeedPart(p0);
 	}
 
 	public void twoDifferentPartRequestsForDifferentLanes() {
 		// First Part:
 		Bin bin0 = new Bin(p0);
 		HashMap<Bin, Integer> binMap0 = new HashMap<Bin, Integer>(); 
 		binMap0.put(bin0, 0);
 
 		gantry.msgChangeGantryBinConfig(new BinConfig(binMap0));
 		n0.msgYouNeedPart(p0);
 
 		// Second Part:
 		Bin bin1 = new Bin(p1);
 		HashMap<Bin, Integer> binMap1 = new HashMap<Bin, Integer>(); 
 		binMap1.put(bin1, 1);
 
 		gantry.msgChangeGantryBinConfig(new BinConfig(binMap1));
 		n1.msgYouNeedPart(p1);
 
 	}
 
 	public void twoDifferentPartRequestsForSameLane() {
 		// First Part:
 		n0.msgYouNeedPart(p0);
 
 		// Second Part:
 		n0.msgYouNeedPart(p1);
 	}
 
 	public void twoSamePartRequestsForDifferentLanes() {
 		// First Part:
 		n0.msgYouNeedPart(p0);
 
 		// Second Part:
 		n1.msgYouNeedPart(p0); 
 	}
 
 	public void twoSamePartRequestsForSameLanes() {
 		// First Part:
 		n0.msgYouNeedPart(p0);
 
 		// Second Part:
 		n0.msgYouNeedPart(p0); 
 	}
 
 	public void actionPerformed(ActionEvent ae){
 //		if(ae.getSource() == placeBinButton){
 //			if(!lane.placedBin){
 //				GraphicBin b = new GraphicBin();
 //				lane.bin = b;
 ////				lane.currentItemCount = 0;
 ////				lane.placedBin = true;
 ////				lane.binExist = true;
 ////			}
 ////		}
 		if (ae.getSource() == normButton)
 		{
 			// Gantry config:
 			Bin bin0 = new Bin(p0);
 			Bin bin1 = new Bin(p1);
 
 			HashMap<Bin, Integer> binMap = new HashMap<Bin, Integer>(); 
 			binMap.put(bin0, 0);
 			binMap.put(bin1, 1);
 			gantry.msgChangeGantryBinConfig(new BinConfig(binMap));
 
 			// Dump part1 into bins and feed it down the lanes.
 			n0.msgYouNeedPart(p0);
 			
 			// Dump part2 into bins and feed it down the lanes.
 			n1.msgYouNeedPart(p1);
 		}
 	else if (ae.getSource() == resetButton)
 		{
 			// Clear the graphics
 			lane.lane1PurgeOn = true;
 			lane.lane2PurgeOn = true;
 			
 			bin = null;
 			lane.bin = null;
 			lane.binExist = false;
 			lane.feederOn = false;		
 
 			setUp();
 
 		}
 		else if (ae.getSource() == topNestNeedsEar)
 		{
 			Bin bin0 = new Bin(p0);
 			HashMap<Bin, Integer> binMap = new HashMap<Bin, Integer>(); 
 			binMap.put(bin0, 0);
 
 			gantry.msgChangeGantryBinConfig(new BinConfig(binMap));
 			n0.msgYouNeedPart(p0);
 		}
 		else if (ae.getSource() == topNestNeedsHelmet)
 		{
 
 			Bin bin0 = new Bin(p1);
 			HashMap<Bin, Integer> binMap = new HashMap<Bin, Integer>(); 
 			binMap.put(bin0, 0);
 
 			gantry.msgChangeGantryBinConfig(new BinConfig(binMap));
 			n0.msgYouNeedPart(p1);
 		}
 		else if (ae.getSource() == bottomNestNeedsEar)
 		{
 			Bin bin0 = new Bin(p0);
 			HashMap<Bin, Integer> binMap = new HashMap<Bin, Integer>(); 
 			binMap.put(bin0, 0);
 
 			gantry.msgChangeGantryBinConfig(new BinConfig(binMap));
 			n1.msgYouNeedPart(p0);
 		}
 		else if (ae.getSource() == bottomNestNeedsHelmet) 
 		{
 			Bin bin0 = new Bin(p1);
 			HashMap<Bin, Integer> binMap = new HashMap<Bin, Integer>(); 
 			binMap.put(bin0, 0);
 
 			gantry.msgChangeGantryBinConfig(new BinConfig(binMap));
 			n1.msgYouNeedPart(p1);
 		}
 		else if(ae.getSource() == purgeFeederButton){
 			bin = null;
 			lane.bin = null;
 			lane.binExist = false;
 			lane.feederOn = false;
 		}
 //		else if(ae.getSource() == startLaneButton){
 //			if(!lane.laneStart){
 //				//lane.feederOn = true;
 //				lane.laneStart = true;
 //				//lane.divergeUp = false;
 //				//lane.vY = 8;
 //			}
 //		}
 //		else if(ae.getSource() == switchLaneButton){
 //			lane.divergeUp = !lane.divergeUp;
 //			lane.vY = -(lane.vY);
 //		}
 //		else if(ae.getSource() == stopLaneButton){
 //			lane.laneStart = false;
 //		}
 //		else if(ae.getSource() == feederOnButton){
 //			lane.feederOn = true;
 //		}
 //		else if(ae.getSource() == feederOffButton){
 //			lane.feederOn = false;
 //		}
 
 //		else if(ae.getSource() == dumpNestButton){
 //			lane.nest1Items.clear();
 //		}
 	}
 
 
 	/** ANIMATION METHODS **/
 	public void doStartFeeding(int feederSlot, Part part) {
 		System.out.print("DOSTARTFEEDING");
 		
 //		if(!lane.laneStart)
 //		{
 //			lane.lane1PurgeOn = false;
 //			lane.lane2PurgeOn = false;
 			lane.feederOn = true;
 			lane.laneStart = true;
 	//	}
 	}
 
 	public void doStopFeeding(int feederSlot) {
 		lane.feederOn = false;
 	}
 
 	public void doPurgeFeeder(int feederSlot) {
 		bin = null;
 		lane.bin = null;
 		lane.binExist = false;
 		lane.feederOn = false;		
 	}
 
 	public void doBringRequestedBin(Integer integer, Feeder f, Part p) {
 		if(!lane.placedBin){
 			GraphicBin b = new GraphicBin(p);
 			lane.bin = b;
 			lane.currentItemCount = 0;
 			lane.placedBin = true;
 			lane.binExist = true;
 		}
 	}
 
 	public void doPurgeTopLane(int feederSlot) {
 		lane.lane1PurgeOn = true;
 		lane.feederOn = false;
 		lane.laneStart = true;
 		
 		
 
 		
 	}
 
 	public void doPurgeBottomLane(int feederSlot) {
 		lane.lane2PurgeOn = true;
 		lane.feederOn = false;
 		lane.laneStart = true;
 		
 	}
 
 	public void doSwitchLane(int feederSlot) {
 		System.out.println("DOSWITCHLANE");
 		lane.divergeUp = !lane.divergeUp;
 		lane.vY = -(lane.vY);
 	}
 
 
 
 
 	public boolean isLaneClear() {
 		
 		return (lane.lane1Items.size() == 0 && lane.lane2Items.size() == 0);
 	}
 	
 }
 
 
 
 
 
 
 
 
