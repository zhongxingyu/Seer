 package factory;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.net.InetSocketAddress;
 
 import network.ObjectSocketClient;
 
 import factory.machine.Machine;
 import factory.plant.*;
 import factory.dimension.ConveyorBeltDimension;
 import factory.dimension.ExtendedPlatformDimension;
 import factory.dimension.HoldingBayDimension;
 import factory.dimension.PointXY;
 import factory.dimension.SorterDimension;
 
 import draw.server.DrawCommandList;
 
 /**
  * Factory.java
  * 
  * @author:			Devin Barry
  * @date:			01.10.2012
  * @lastModified: 	23.10.2012
  * 
  * Factory.java - rewritten in 2013
  * 
  * @author:			Devin Barry
  * @date:			16.01.2013
  * @lastModified: 	17.01.2013
  *
  * This class contains all the machines required on the entire
  * production line. It is a merge of
  *  Segment1.java
  *  Segment2.java
  *  Segment3.java
  *  Segment4.java
  *  
  * Now all the classes are stored here and there is a single static
  * map from which all machines can be accessed.
  * 
  * The objective of this class is to position all the machines on the
  * factory floor. Many of the machines call methods that help them
  * automatically position themselves relative to the other machines.
  * 
  * Most of the y dimensions for all the machines are automatically determined.
  * The x dimensions are often hard-wired and are thus a TODO
  * 
  */
 
 public class Factory {
 	
 	public static Map<String,Machine> machines1 = new HashMap<String,Machine>(15);
 	public static Map<String,Machine> machines2 = new HashMap<String,Machine>(15);
 	public static Map<String,Machine> machines3 = new HashMap<String,Machine>(15);
 	public static Map<String,Machine> machines4 = new HashMap<String,Machine>(15);
 	
 	public static final PointXY base = new PointXY(0, 0);
 	
 	private DrawCommandList dcl;
 	private InetSocketAddress drawServerAddress;
 	
 	public Factory(InetSocketAddress drawServerAddress) {
 		this.drawServerAddress = drawServerAddress;
 		this.createSegment1();
 		this.createSegment2();
 		this.createSegment3();
 		this.createSegment4();
 	}
 	
 	/**
 	 * This should probably be made to be thread safe because
 	 * this paint method may be called from multiple different
 	 * threads inside the SystemJ program
 	 * TODO
 	 * 
 	 * @param delay
 	 */
 	public void paint1(int delay) {
 		dcl = new DrawCommandList(); //start with a new list each time
 		
 		//Draw all machines in the factory
 		Iterator<Machine> i = machines1.values().iterator();
 		while (i.hasNext()) {
 			i.next().draw(dcl, base);
 		}
 		
 		//Show everything that has been drawn
 		dcl.addCommand("show", delay);
 		
 		//optimally, we create a queue and we put the new dcl on the queue
 		//the network client reads from the queue and send items on queue
 		//to the server
 		
 		//right now we will just send item straight to server
 		InetSocketAddress serverAddress = new InetSocketAddress(drawServerAddress.getAddress(), 55551);
 		ObjectSocketClient osc = new ObjectSocketClient(serverAddress);
 		osc.setSendObject(dcl);
 		
 		//start the ObjectSocketClient in a new thread
 		Thread t = new Thread(osc);
 		t.start();
 	}
 	
 	/**
 	 * This should probably be made to be thread safe because
 	 * this paint method may be called from multiple different
 	 * threads inside the SystemJ program
 	 * TODO
 	 * 
 	 * @param delay
 	 */
 	public void paint2(int delay) {
 		dcl = new DrawCommandList(); //start with a new list each time
 		
 		//Draw all machines in the factory
 		Iterator<Machine> i = machines2.values().iterator();
 		while (i.hasNext()) {
 			i.next().draw(dcl, base);
 		}
 		
 		//Show everything that has been drawn
 		dcl.addCommand("show", delay);
 		
 		//optimally, we create a queue and we put the new dcl on the queue
 		//the network client reads from the queue and send items on queue
 		//to the server
 		
 		//right now we will just send item straight to server
 		InetSocketAddress serverAddress = new InetSocketAddress(drawServerAddress.getAddress(), 55552);
 		ObjectSocketClient osc = new ObjectSocketClient(serverAddress);
 		osc.setSendObject(dcl);
 		
 		//start the ObjectSocketClient in a new thread
 		Thread t = new Thread(osc);
 		t.start();
 	}
 	
 	/**
 	 * This should probably be made to be thread safe because
 	 * this paint method may be called from multiple different
 	 * threads inside the SystemJ program
 	 * TODO
 	 * 
 	 * @param delay
 	 */
 	public void paint3(int delay) {
 		dcl = new DrawCommandList(); //start with a new list each time
 		
 		//Draw all machines in the factory
 		Iterator<Machine> i = machines3.values().iterator();
 		while (i.hasNext()) {
 			i.next().draw(dcl, base);
 		}
 		
 		//Show everything that has been drawn
 		dcl.addCommand("show", delay);
 		
 		//optimally, we create a queue and we put the new dcl on the queue
 		//the network client reads from the queue and send items on queue
 		//to the server
 		
 		//right now we will just send item straight to server
 		InetSocketAddress serverAddress = new InetSocketAddress(drawServerAddress.getAddress(), 55553);
 		ObjectSocketClient osc = new ObjectSocketClient(serverAddress);
 		osc.setSendObject(dcl);
 		
 		//start the ObjectSocketClient in a new thread
 		Thread t = new Thread(osc);
 		t.start();
 	}
 	
 	/**
 	 * This should probably be made to be thread safe because
 	 * this paint method may be called from multiple different
 	 * threads inside the SystemJ program
 	 * TODO
 	 * 
 	 * @param delay
 	 */
 	public void paint4(int delay) {
 		dcl = new DrawCommandList(); //start with a new list each time
 		
 		//Draw all machines in the factory
 		Iterator<Machine> i = machines4.values().iterator();
 		while (i.hasNext()) {
 			i.next().draw(dcl, base);
 		}
 		
 		//Show everything that has been drawn
 		dcl.addCommand("show", delay);
 		
 		//optimally, we create a queue and we put the new dcl on the queue
 		//the network client reads from the queue and send items on queue
 		//to the server
 		
 		//right now we will just send item straight to server
 		InetSocketAddress serverAddress = new InetSocketAddress(drawServerAddress.getAddress(), 55554);
 		ObjectSocketClient osc = new ObjectSocketClient(serverAddress);
 		osc.setSendObject(dcl);
 		
 		//start the ObjectSocketClient in a new thread
 		Thread t = new Thread(osc);
 		t.start();
 	}
 
 	/**
 	 * Create the components in their correct locations
 	 */
 	private void createSegment1() {
 		//Sorter Eight
 		PointXY s8_Pos = new PointXY(0,600);
 		Sorter s8 = new Sorter(s8_Pos);
 		machines1.put("s8", s8);
 		
 		
 		//================= BOTTOM ROW =================
 		double conveyorLine1Y = s8.nextMachineStartPoint().getY() - 10;
 		
 		//ConveyorBelt Twenty
 		PointXY cb20_location = new PointXY(s8.nextMachineStartPoint().getX(), conveyorLine1Y);
 		ConveyorBelt cb20 = new ConveyorBelt(cb20_location, 18, 35); //size 12, 35 angle
 		machines1.put("cb20", cb20);
 		
 		//HoldingBay 6
 		double hbl1_1_y = cb20.nextMachineStartPoint().getY() + HoldingBayDimension.RADIUS; // + ConveyorBeltDimension.WIDTH;
 		PointXY hbl1_1Pos = new PointXY(cb20.nextMachineStartPoint().getX(), hbl1_1_y);
 		HoldingBay hb6 = new HoldingBay(hbl1_1Pos);
 		machines1.put("hb6", hb6);
 		
 		//Sorter Five
 		PointXY sl1_1Pos = new PointXY(hb6.nextMachineStartPoint().getX(), hb6.nextMachineStartPoint().getY() + SorterDimension.RADIUS);
 		Sorter sorter5 = new Sorter(sl1_1Pos);
 		machines1.put("s5", sorter5);
 		
 		//ConveyorBelt 16
 		double yPosL1 = sorter5.nextMachineStartPoint().getY() + (ConveyorBeltDimension.WIDTH / 2);
 		PointXY cbl1_1Pos = new PointXY(sorter5.nextMachineStartPoint().getX() - 10, yPosL1 + 30);
 		ConveyorBelt conveyor16 = new ConveyorBelt(cbl1_1Pos, 10, -60); //size 10, -60 angle
 		machines1.put("cb16", conveyor16);
 		
 		//ConveyorBelt 15
 		PointXY cbl1_2Pos = new PointXY(sorter5.nextMachineStartPoint().getX(), yPosL1 - 30);
 		ConveyorBelt conveyor15 = new ConveyorBelt(cbl1_2Pos, 8, 0); //size 8, 0 degree angle
 		machines1.put("cb15", conveyor15);
 		
 		
 		//================= TOP ROW =================
 		double conveyorLine2Y = s8.nextMachineStartPoint().getY() + 40;
 		
 		//ConveyorBelt One
 		double cb1_x = s8.nextMachineStartPoint().getX() - 20; //this is a hack until we to the TODO s
 		PointXY cbl2_4Pos = new PointXY(cb1_x, conveyorLine2Y);
 		ConveyorBelt conveyor1 = new ConveyorBelt(cbl2_4Pos, 18, -35); //size 12, -35 angle
 		machines1.put("cb1", conveyor1);
 		
 		//HoldingBay 2
 		double hbl2_1_y = conveyor1.nextMachineStartPoint().getY() + HoldingBayDimension.RADIUS - ConveyorBeltDimension.WIDTH;
 		PointXY hbl2_1Pos = new PointXY(conveyor1.nextMachineStartPoint().getX(), hbl2_1_y);
 		HoldingBay hbl2_1 = new HoldingBay(hbl2_1Pos);
 		machines1.put("hb2", hbl2_1);
 		
 		//Sorter Two
 		PointXY sl2_1Pos = new PointXY(hbl2_1.nextMachineStartPoint().getX(), hbl2_1.nextMachineStartPoint().getY() + SorterDimension.RADIUS);
 		Sorter sl2_1 = new Sorter(sl2_1Pos);
 		machines1.put("s2", sl2_1);
 		
 		//ConveyorBelt Two
 		double yPosL2 = sl2_1.nextMachineStartPoint().getY() + (ConveyorBeltDimension.WIDTH / 2);
 		PointXY cbl2_1Pos = new PointXY(sl2_1.nextMachineStartPoint().getX(), yPosL2 + 30);
 		ConveyorBelt conveyor2 = new ConveyorBelt(cbl2_1Pos, 8, 0); //size 8, 0 angle
 		machines1.put("cb2", conveyor2);
 		
 		//ConveyorBelt Three
 		PointXY cbl2_2Pos = new PointXY(sl2_1.nextMachineStartPoint().getX(), yPosL2 - 30);
 		ConveyorBelt conveyor3 = new ConveyorBelt(cbl2_2Pos, 10, 60); //size 10, 60 angle
 		machines1.put("cb3", conveyor3);
 		
 		//System.out.println("Machines in segment1: " + machines1.size());
 	}
 	
 	/**
 	 * Create the components in their correct locations
 	 */
 	private void createSegment2() {
 		ConveyorBelt cb2 = (ConveyorBelt)machines1.get("cb2");
 		ConveyorBelt cb15 = (ConveyorBelt)machines1.get("cb15");
 		//================= TOP ROW =================
 		
 		//Sorter Three
 		Sorter sorterThree = new Sorter(cb2.nextMachineStartPoint());
 		machines2.put("s3", sorterThree);
 		
 		//Conveyor Four
 		//Conveyor belt is drawn from the top left corner
 		double c4_y = sorterThree.nextMachineStartPoint().getY() + SorterDimension.RADIUS + ConveyorBeltDimension.WIDTH;
 		PointXY startPointCFour = new PointXY(sorterThree.nextMachineStartPoint().getX(), c4_y);
 		ConveyorBelt conveyorFour = new ConveyorBelt(startPointCFour, 8, 0);
 		machines2.put("cb4", conveyorFour);
 		
 		//Conveyor Five
 		double c5_y = sorterThree.nextMachineStartPoint().getY() - SorterDimension.RADIUS;
 		PointXY startPointCFive = new PointXY(sorterThree.nextMachineStartPoint().getX(), c5_y);
 		ConveyorBelt conveyorFive = new ConveyorBelt(startPointCFive, 8, 0);
 		machines2.put("cb5", conveyorFive);
 		
 		//Extended Platform One
 		double ep1_y = sorterThree.nextMachineStartPoint().getY() + ExtendedPlatformDimension.RADIUS;
 		PointXY startPointEOne = new PointXY(conveyorFour.nextMachineStartPoint().getX() - 15, ep1_y);
 		ExtendedPlatform extendedPlatformOne = new ExtendedPlatform(startPointEOne);
 		machines2.put("ep1", extendedPlatformOne);
 		
 		//HoldingBay Three
 		double hb3_y = extendedPlatformOne.nextMachineStartPoint().getY() + HoldingBayDimension.RADIUS;
 		PointXY startPointHBThree = new PointXY(extendedPlatformOne.nextMachineStartPoint().getX(), hb3_y);
 		HoldingBay holdingBayThree = new HoldingBay(startPointHBThree);
 		machines2.put("hb3", holdingBayThree);
 		
 		
 		//================= BOTTOM ROW =================
 		//Sorter Six
 		Sorter sorterSix = new Sorter(cb15.nextMachineStartPoint());
 		machines2.put("s6", sorterSix);
 		
 		//Conveyor Eighteen
 		double c18_y = sorterSix.nextMachineStartPoint().getY() + SorterDimension.RADIUS + ConveyorBeltDimension.WIDTH;
 		PointXY startPointCEighteen = new PointXY(sorterSix.nextMachineStartPoint().getX(), c18_y);
 		ConveyorBelt conveyorEighteen = new ConveyorBelt(startPointCEighteen, 8, 0);
 		machines2.put("cb18", conveyorEighteen);
 		
 		//Conveyor Nineteen
 		double c19_y = sorterSix.nextMachineStartPoint().getY() - SorterDimension.RADIUS;
 		PointXY startPointCNineteen = new PointXY(sorterSix.nextMachineStartPoint().getX(), c19_y);
 		ConveyorBelt conveyorNineteen = new ConveyorBelt(startPointCNineteen, 8, 0);
 		machines2.put("cb19", conveyorNineteen);
 		
 		//ExtendedPlatform Six
 		double ep6_y = sorterSix.nextMachineStartPoint().getY() + ExtendedPlatformDimension.RADIUS;
 		PointXY startPointESix = new PointXY(conveyorEighteen.nextMachineStartPoint().getX() - 15, ep6_y);
 		ExtendedPlatform extendedPlatformSix = new ExtendedPlatform(startPointESix);
 		machines2.put("ep6", extendedPlatformSix);
 		
 		//HoldingBay Seven
 		double hb7_y = extendedPlatformSix.nextMachineStartPoint().getY() + HoldingBayDimension.RADIUS;
 		PointXY startPointHBSeven = new PointXY(extendedPlatformSix.nextMachineStartPoint().getX(), hb7_y);
 		HoldingBay holdingBaySeven = new HoldingBay(startPointHBSeven);
 		machines2.put("hb7", holdingBaySeven);
 		
 		//System.out.println("Machines in segment2: " + machines2.size());
 	}
 	
 	/**
 	 * Create the components in their correct locations
 	 */
 	private void createSegment3() {
 		HoldingBay hb3 = (HoldingBay)machines2.get("hb3");
 		HoldingBay hb7 = (HoldingBay)machines2.get("hb7");
 		//================= TOP ROW =================
 		
 		//Conveyor Six
 		ConveyorBelt conveyorSix = new ConveyorBelt(hb3.nextMachineStartPoint(), 15, 0);
 		machines3.put("cb6", conveyorSix);
 		
 		//Conveyor Seven
 		double c7_y = conveyorSix.getEndPoint().getY() - (ConveyorBeltDimension.WIDTH * 2);
 		PointXY startPointCSeven = new PointXY(conveyorSix.getEndPoint().getX(), c7_y);
 		ConveyorBelt conveyorSeven = new ConveyorBelt(startPointCSeven, 12, 137);
 		machines3.put("cb7", conveyorSeven);
 		
 		//Sorter Four
 		PointXY startPointSFour = new PointXY(conveyorSix.nextMachineStartPoint());
 		Sorter sorterFour = new Sorter(startPointSFour);
 		machines3.put("s4", sorterFour);
 		
 		//Conveyor Nine
 		double c9_y = sorterFour.nextMachineStartPoint().getY() + (ConveyorBeltDimension.WIDTH/2);
 		PointXY startPointCNine = new PointXY(sorterFour.nextMachineStartPoint().getX(), c9_y);
 		ConveyorBelt conveyorNine = new ConveyorBelt(startPointCNine, 8, 10); //10 degrees
 		machines3.put("cb9", conveyorNine);
 		
 		//HoldingBay Four
 		double hb4_y = conveyorNine.nextMachineStartPoint().getY() + HoldingBayDimension.RADIUS;
 		PointXY startPointHBFour = new PointXY(conveyorNine.nextMachineStartPoint().getX(), hb4_y);
 		HoldingBay holdingBayFour = new HoldingBay(startPointHBFour);
 		machines3.put("hb4", holdingBayFour);
 		
 		//Conveyor Ten
 		double cb10_x = holdingBayFour.nextMachineStartPoint().getX() + ConveyorBeltDimension.WIDTH;
 		PointXY startPointcb10 = new PointXY(cb10_x, holdingBayFour.nextMachineStartPoint().getY());
 		ConveyorBelt conveyorTen = new ConveyorBelt(startPointcb10, 11, 45); //45 degrees
 		machines3.put("cb10", conveyorTen);
 		
 		//================= BOTTOM ROW =================
 		//Conveyor Thirteen
 		ConveyorBelt conveyorThirteen = new ConveyorBelt(hb7.nextMachineStartPoint(), 15, 0);
 		machines3.put("cb13", conveyorThirteen);
 		
 		//Conveyor Fourteen
 		double c14_y = conveyorThirteen.getEndPoint().getY();
 		PointXY startPointCFourteen = new PointXY(conveyorThirteen.getEndPoint().getX(), c14_y);
 		ConveyorBelt conveyorFourteen = new ConveyorBelt(startPointCFourteen, 14, -135);
 		machines3.put("cb14", conveyorFourteen);
 		
 		//Sorter Seven
 		PointXY startPointSSeven = new PointXY(conveyorThirteen.nextMachineStartPoint());
 		Sorter sorterSeven = new Sorter(startPointSSeven);
 		machines3.put("s7", sorterSeven);
 		
 		//Conveyor Twelve
 		double c12_y = sorterSeven.nextMachineStartPoint().getY() + (ConveyorBeltDimension.WIDTH/2);
 		PointXY startPointCTwelve = new PointXY(sorterSeven.nextMachineStartPoint().getX(), c12_y);
 		ConveyorBelt conveyorTwelve = new ConveyorBelt(startPointCTwelve, 8, -10); //-10 degrees
 		machines3.put("cb12", conveyorTwelve);
 		
 		//HoldingBay Eight
 		double hb8_y = conveyorTwelve.nextMachineStartPoint().getY() + HoldingBayDimension.RADIUS;
 		PointXY startPointHBEight = new PointXY(conveyorTwelve.nextMachineStartPoint().getX(), hb8_y);
 		HoldingBay holdingBayEight = new HoldingBay(startPointHBEight);
 		machines3.put("hb8", holdingBayEight);
 		
 		//Conveyor Eleven
 		ConveyorBelt conveyorEleven = new ConveyorBelt(holdingBayEight.nextMachineStartPoint(), 12, -50); //-50 degrees
 		machines3.put("cb11", conveyorEleven);
 		
 		
 		//================= CENTRAL =================
 		//ExtendedPlatform Three
 		double ep3_y = conveyorTen.nextMachineStartPoint().getY() - (ExtendedPlatformDimension.RADIUS / 2);
 		double ep3_x = conveyorTen.nextMachineStartPoint().getX() - ExtendedPlatformDimension.RADIUS;
 		PointXY startPointEThree = new PointXY(ep3_x, ep3_y);
 		ExtendedPlatform extendedPlatformThree = new ExtendedPlatform(startPointEThree);
 		machines3.put("ep3", extendedPlatformThree);
 		
 		//HoldingBay Five
 		double hb5_y = extendedPlatformThree.nextMachineStartPoint().getY() + HoldingBayDimension.RADIUS;
 		PointXY startPointHBFive = new PointXY(extendedPlatformThree.nextMachineStartPoint().getX(), hb5_y);
 		HoldingBay holdingBayFive = new HoldingBay(startPointHBFive);
 		machines3.put("hb5", holdingBayFive);
 		
 		//System.out.println("Machines in segment3: " + machines3.size());
 	}
 	
 	/**
 	 * Create the components in their correct locations
 	 */
 	private void createSegment4() {
 		ConveyorBelt cb3 = (ConveyorBelt)machines1.get("cb3");
 		ConveyorBelt cb7 = (ConveyorBelt)machines3.get("cb7");
 		
 		//================= LEFT =================
 		//ExtendedPlatform Two
 		PointXY startPointETwo = new PointXY(cb3.getEndPoint());
 		ExtendedPlatform extendedPlatformTwo = new ExtendedPlatform(startPointETwo);
 		machines4.put("ep2", extendedPlatformTwo);
 		
 		//Conveyor Eight
		PointXY startPointCEight = new PointXY(extendedPlatformTwo.nextMachineStartPoint());
 		ConveyorBelt conveyorEight = new ConveyorBelt(startPointCEight, 8, 0);
 		machines4.put("cb8", conveyorEight);
 		
 		//================= MIDDLE =================
 		//ExtendedPlatform Four
 		PointXY startPointEFour = new PointXY(conveyorEight.nextMachineStartPoint());
 		ExtendedPlatform extendedPlatformFour = new ExtendedPlatform(startPointEFour);
 		machines4.put("ep4", extendedPlatformFour);
 		
 		//HoldingBay Nine
 		double hb9_x = conveyorEight.nextMachineStartPoint().getX() + ExtendedPlatformDimension.RADIUS - HoldingBayDimension.RADIUS;
 		double hb9_y = conveyorEight.nextMachineStartPoint().getY() - (2 * ExtendedPlatformDimension.RADIUS);
 		PointXY startPointHBNine = new PointXY(hb9_x, hb9_y);
 		HoldingBay holdingBayNine = new HoldingBay(startPointHBNine);
 		machines4.put("hb9", holdingBayNine);
 		
 		//================= RIGHT =================
 		//ExtendedPlatform Five
 		double ep5_x = cb7.getEndPoint().getX() - (2 * ExtendedPlatformDimension.RADIUS);
 		double ep5_y = cb7.getEndPoint().getY() + (ExtendedPlatformDimension.RADIUS / 2);
 		PointXY startPointEFive = new PointXY(ep5_x, ep5_y);
 		ExtendedPlatform extendedPlatformFive = new ExtendedPlatform(startPointEFive);
 		machines4.put("ep5", extendedPlatformFive);
 		
 		//Conveyor Seventeen
		double cb17_y = extendedPlatformFour.nextMachineStartPoint().getY() + ConveyorBeltDimension.WIDTH;
		PointXY startPointCSeventeen = new PointXY(extendedPlatformFour.nextMachineStartPoint().getX(), cb17_y);
		ConveyorBelt conveyorSeventeen = new ConveyorBelt(startPointCSeventeen, 8, 0);
 		machines4.put("cb17", conveyorSeventeen);
 		
 		//System.out.println("Machines in segment4: " + machines4.size());
 	}
 }
