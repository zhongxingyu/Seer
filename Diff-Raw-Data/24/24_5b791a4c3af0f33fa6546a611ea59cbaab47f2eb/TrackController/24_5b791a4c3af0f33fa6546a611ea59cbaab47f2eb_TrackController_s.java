/*package TKC.util;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.LinkedList;
 
 import CTCOffice.CTCOffice;
 import TKM.Block;
 import TKM.Switch;
 
 
 
*//**
  * 
  * @author Dominic Visco
  *
 *//*
 public class TrackController {
 
 	private boolean currentLine;
 	private ArrayList<Wayside> controllerList;
 	private ArrayList<Switch> switchList;
 	private ArrayList<Block> blockList;
 	private CTCOffice ctc;
 	
 	public TrackController(ArrayList<Block> blockList, ArrayList<Switch> switchList, CTCOffice ctc) {
 		this.blockList = blockList;
 		this.switchList = switchList;
 		this.ctc = ctc;
 		this.controllerList = new ArrayList<Wayside>();
 		initControllers();
 	}
 	
 	public void nextTick() {
 		for (Wayside way : controllerList)
 		{
 			way.nextTick();
 		}
 		
 	}
 	public void initControllers() {
 		
 		
 		/*Initializing controllers for the red line*/
 		boolean switchS = Switch.STATE_STRAIGHT;
 		Component lightS = new Component(0,0);
 		SafetyInfo newInfo = new SafetyInfo(switchS, lightS, true);
 		
 		Wayside one;
 		
 		Hashtable<Integer, Block> oneBL = new Hashtable<Integer, Block>();
 		oneBL.put(0, blockList.get(0));
 		oneBL.put(7, blockList.get(7));
 		oneBL.put(8, blockList.get(8));
 		oneBL.put(9, blockList.get(9));
 		oneBL.put(10, blockList.get(10));
 		oneBL.put(11, blockList.get(11));
 		oneBL.put(12, blockList.get(12));
 		
 		ArrayList<Integer> oneEndBL = new ArrayList<Integer>();
 		oneEndBL.add(0);
 		oneEndBL.add(7);
 		oneEndBL.add(12);
 		
 		Switch oneSw = switchList.get(1);
 		
 		one = new Wayside(oneBL, oneEndBL, ctc, newInfo, oneSw);
 		controllerList.add(one);
 		
 		Wayside two;
 		
 		Hashtable<Integer, Block> twoBL = new Hashtable<Integer, Block>();
 		twoBL.put(23, blockList.get(23));
 		twoBL.put(22, blockList.get(22));
 		twoBL.put(21, blockList.get(21));
 		twoBL.put(20, blockList.get(20));
 		twoBL.put(19, blockList.get(19));
 		twoBL.put(18, blockList.get(18));
 		twoBL.put(17, blockList.get(17));
 		twoBL.put(16, blockList.get(16));
 		twoBL.put(1, blockList.get(1));
 		twoBL.put(2, blockList.get(2));
 		twoBL.put(3, blockList.get(3));
 		twoBL.put(4, blockList.get(4));
 		twoBL.put(5, blockList.get(5));
 		twoBL.put(6, blockList.get(6));
 		twoBL.put(7, blockList.get(7));
 		twoBL.put(15, blockList.get(15));
 		twoBL.put(14, blockList.get(14));
 		twoBL.put(13, blockList.get(13));
 		twoBL.put(12, blockList.get(12));
 		
 		
 		ArrayList<Integer> twoEndBL = new ArrayList<Integer>();
 		twoEndBL.add(23);
 		twoEndBL.add(7);
 		twoEndBL.add(12);
 		
 		Switch twoSw = switchList.get(0);
 		
 		two = new Wayside(twoBL, twoEndBL, ctc, newInfo, twoSw);
 		controllerList.add(two);
 		
 		Wayside three;
 		Hashtable<Integer, Block> threeBL = new Hashtable<Integer, Block>();
 		threeBL.put(23, blockList.get(23));
 		threeBL.put(24, blockList.get(24));
 		threeBL.put(25, blockList.get(25));
 		threeBL.put(26, blockList.get(26));
 		threeBL.put(27, blockList.get(27));
 		threeBL.put(76, blockList.get(76));
 		threeBL.put(75, blockList.get(75));
 		threeBL.put(28, blockList.get(28));
 		threeBL.put(29, blockList.get(29));
 		threeBL.put(30, blockList.get(30));
 		
 		ArrayList<Integer> threeEndBL = new ArrayList<Integer>();
 		threeEndBL.add(23);
 		threeEndBL.add(75);
 		threeEndBL.add(30);
 		
 		Switch threeSw = switchList.get(2);
 		
 		three = new Wayside(threeBL, threeEndBL, ctc, newInfo, threeSw);
 		controllerList.add(three);
 		
 		Wayside four;
 		Hashtable<Integer, Block> fourBL = new Hashtable<Integer, Block>();
 		fourBL.put(33, blockList.get(33));
 		fourBL.put(34, blockList.get(34));
 		fourBL.put(35, blockList.get(35));
 		fourBL.put(36, blockList.get(36));
 		fourBL.put(72, blockList.get(72));
 		fourBL.put(73, blockList.get(73));
 		fourBL.put(74, blockList.get(74));
 		fourBL.put(75, blockList.get(75));
 		fourBL.put(30, blockList.get(33));
 		fourBL.put(31, blockList.get(31));
 		fourBL.put(32, blockList.get(32));
 		
 		ArrayList<Integer> fourEndBL = new ArrayList<Integer>();
 		fourEndBL.add(36);
 		fourEndBL.add(75);
 		fourEndBL.add(30);
 		
 		Switch fourSw = switchList.get(3);
 		
 		four = new Wayside(fourBL, fourEndBL, ctc, newInfo, fourSw);
 		controllerList.add(four);
 		
 		Wayside five;
 		Hashtable<Integer, Block> fiveBL = new Hashtable<Integer, Block>();
 		fiveBL.put(36, blockList.get(36));
 		fiveBL.put(37, blockList.get(37));
 		fiveBL.put(38, blockList.get(38));
 		fiveBL.put(71, blockList.get(36));
 		fiveBL.put(70, blockList.get(36));
 		fiveBL.put(69, blockList.get(36));
 		fiveBL.put(39, blockList.get(39));
 		fiveBL.put(40, blockList.get(40));
 		fiveBL.put(41, blockList.get(41));
 		
 		ArrayList<Integer> fiveEndBL = new ArrayList<Integer>();
 		fiveEndBL.add(36);
 		fiveEndBL.add(69);
 		fiveEndBL.add(41);
 		
 		Switch fiveSw = switchList.get(4);
 		
 		five = new Wayside(fiveBL, fiveEndBL, ctc, newInfo, fiveSw);
 		controllerList.add(five);
 		
 		Wayside six;
 		Hashtable<Integer, Block> sixBL = new Hashtable<Integer, Block>();
 		sixBL.put(44, blockList.get(44));
 		sixBL.put(45, blockList.get(45));
 		sixBL.put(46, blockList.get(46));
 		sixBL.put(47, blockList.get(47));
 		sixBL.put(48, blockList.get(48));
 		sixBL.put(67, blockList.get(67));
 		sixBL.put(68, blockList.get(68));
 		sixBL.put(69, blockList.get(69));
 		sixBL.put(41, blockList.get(41));
 		sixBL.put(42, blockList.get(42));
 		sixBL.put(43, blockList.get(43));
 		
 		ArrayList<Integer> sixEndBL = new ArrayList<Integer>();
 		sixEndBL.add(48);
 		sixEndBL.add(69);
 		sixEndBL.add(41);
 		
 		Switch sixSw = switchList.get(5);
 		
 		six = new Wayside(sixBL, sixEndBL, ctc, newInfo, sixSw);
 		controllerList.add(six);
 		
 		Wayside seven;
 		Hashtable<Integer, Block> sevenBL = new Hashtable<Integer, Block>();
 		sevenBL.put(48, blockList.get(48));
 		sevenBL.put(49, blockList.get(49));
 		sevenBL.put(50, blockList.get(50));
 		sevenBL.put(51, blockList.get(51));
 		sevenBL.put(52, blockList.get(52));
 		sevenBL.put(66, blockList.get(66));
 		sevenBL.put(65, blockList.get(65));
 		sevenBL.put(64, blockList.get(64));
 		sevenBL.put(63, blockList.get(63));
 		sevenBL.put(62, blockList.get(62));
 		sevenBL.put(61, blockList.get(61));
 		sevenBL.put(53, blockList.get(53));
 		sevenBL.put(54, blockList.get(54));
 		sevenBL.put(55, blockList.get(55));
 		sevenBL.put(56, blockList.get(56));
 		sevenBL.put(57, blockList.get(57));
 		sevenBL.put(58, blockList.get(58));
 		sevenBL.put(59, blockList.get(59));
 		sevenBL.put(60, blockList.get(60));
 		
 		ArrayList<Integer> sevenEndBL = new ArrayList<Integer>();
 		sevenEndBL.add(48);
 		sevenEndBL.add(60);
 		sevenEndBL.add(61);
 		
 		Switch sevenSw = switchList.get(6);
 		
 		seven = new Wayside(sevenBL, sevenEndBL, ctc, newInfo, sevenSw);
 		controllerList.add(seven);
 		
 		
 	}
 }
*/
