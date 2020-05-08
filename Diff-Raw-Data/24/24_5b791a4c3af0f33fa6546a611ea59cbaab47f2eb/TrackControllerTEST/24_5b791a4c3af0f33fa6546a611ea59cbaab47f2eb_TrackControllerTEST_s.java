/*package TKC.test;
 
 import java.util.ArrayList;
 
 import TKC.util.TrackController;
 
 
 public class TrackControllerTEST {
	
 	/*public Train testTrain;
 	public CTCOffice mainOffice;
 	public ArrayList<Block> track;
 	
 	public TrackController testTrackController;
 	
 	public TrackControllerTEST() {
 		this.track = initTestBlocks();
 		this.testTrain = iniTrain(this.track);
 		this.mainOffice = new CTCOffice(this.testTrain);
 		this.testTrackController = new TrackController(track, mainOffice);
 	}
 	
 	public Train iniTrain(ArrayList<Block> blockList) {
 		
 		ArrayList<Block> route = new ArrayList<Block>();
 		
 		route.add(blockList.get(1));
 		route.add(blockList.get(2));
 		route.add(blockList.get(3));
 		route.add(blockList.get(4));
 		route.add(blockList.get(5));
 		route.add(blockList.get(10));
 		route.add(blockList.get(11));
 		route.add(blockList.get(12));
 		route.add(blockList.get(13));
 		
 		Train temp = new Train(1, "Test", (9*60*60+0*30*60)%(24*60*60), route, new Engineer(true, false, 0.0, (9*60*60+0*30*60+4*60*60)%(24*60*60)), blockList.get(4));
 		return temp;
 		
 	}
 	
 	public ArrayList<Block> initTestBlocks() {
 
 		// Create the test block loop.
 		ArrayList<Block> blockList = new ArrayList<Block>();
 		Block bYard = new Block(0, 100.0, 0.0, true, 5.0, null, null, null, null, true, false, "Welcome to the Yard!", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		Block b01 = new Block(1, 500.0, 0.0, true, 15.0, null, null, null, null, false, false, "", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		Block b02 = new Block(2, 500.0, -22.2, true, 1.0, null, null, null, null, false, false, "", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		Block b03 = new Block(3, 50.0, 0.0, true, 15.0, null, null, null, null, false, false, "", false, false, false, true, false, 0, 0.0, 0.0, 0.0, false);
 		Block b04 = new Block(4, 500.0, 11.1, true, 30.0, null, null, null, null, false, false, "", false, false, false, false, false, 0, 0.0, 0.0, 0.0, true);
 		Block b05 = new Block(5, 50.0, 0.0, true, 15.0, null, null, null, null, false, false, "", false, false, false, false, true, 1, 0.0, 0.0, 0.0, false);
 		Block b06 = new Block(6, 100.0, 0.0, true, 5.0, null, null, null, null, false, true, "Welcome to Station Alpha!", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		Block b07 = new Block(7, 500.0, -11.1, true, 30.0, null, null, null, null, false, false, "", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		Block b08 = new Block(8, 50.0, 0.0, true, 15.0, null, null, null, null, false, false, "", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		Block b09 = new Block(9, 500.0, 22.2, true, 1.0, null, null, null, null, false, false, "", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		Block b10 = new Block(10, 50.0, 0.0, true, 15.0, null, null, null, null, false, false, "", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		Block b11 = new Block(11, 100.0, 0.0, true, 5.0, null, null, null, null, false, true, "Welcome to Station Beta!", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		Block b12 = new Block(12, 50.0, 11.1, true, 15.0, null, null, null, null, false, false, "", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		Block b13 = new Block(13, 50.0, 0.0, true, 15.0, null, null, null, null, false, false, "", false, false, false, false, false, 0, 0.0, 0.0, 0.0, false);
 		
 		bYard.nextBlock = b01;
 		b01.nextBlock = b02;
 		b02.nextBlock = b03;
 		b03.nextBlock = b04;
 		b04.nextBlock = b05;
 		
 		b05.nextBlock = b06;
 		b06.nextBlock = b07;
 		b07.nextBlock = b08;
 		b08.nextBlock = b09;
 		b09.nextBlock = null;
 		
 		b05.nextBlockAlt = b10;
 		b10.nextBlock = b11;
 		b11.nextBlock = b12;
 		b12.nextBlock = b13;
 		b13.nextBlock = null;
 		
 		bYard.prevBlock = null;
 		b01.prevBlock = bYard;
 		b02.prevBlock = b01;
 		b03.prevBlock = b02;
 		b04.prevBlock = b03;
 		
 		b05.prevBlock = b04;
 		b06.prevBlock = b05;
 		b07.prevBlock = b06;
 		b08.prevBlock = b07;
 		b09.prevBlock = b08;
 		
 		b05.prevBlockAlt = b06;
 		b10.prevBlock = b05;
 		b11.prevBlock = b10;
 		b12.prevBlock = b11;
 		b13.prevBlock = b12;
 		
 		blockList.add(bYard);
 		blockList.add(b01);
 		blockList.add(b02);
 		blockList.add(b03);
 		blockList.add(b04);
 		blockList.add(b05);
 		blockList.add(b06);
 		blockList.add(b07);
 		blockList.add(b08);
 		blockList.add(b09);
 		blockList.add(b10);
 		blockList.add(b11);
 		blockList.add(b12);
 		blockList.add(b13);
 		
 
 		return blockList;
 
 	}
 	
 	public static void printBlk (Block blk) {
 		System.out.println("Block " + blk.id + ": \tSwitch State\tOccupied\n" +
 				"\t\t" + blk.switchState + "\t\t" + blk.isOccupied + "\n\n");
 	}
 	public static void main(String [] args) {
 		
 		TrackControllerTEST newTest = new TrackControllerTEST();
 		
 		//debugging
 		System.out.println(newTest.testTrain.getRoute().get(5).id);
 		
 		for (Block blk : newTest.track) {
 			printBlk(blk);
 		}
 		
 		System.out.println("Running TrackController.nextTick()......\n");
 		
 		newTest.testTrackController.nextTick();
 		
 		for (Block blk : newTest.track) {
 			printBlk(blk);
 		}
 		
 	}*/
 }
*/
