 package TKC.util;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import CTCOffice.CTCOffice;
 import TKM.Block;
 import TKM.Switch;
 import TNM.Train;
 
 
 /**
  * 
  * @author Dominic Visco
  *
  */
 public class Wayside {
 
 	public PLCProgram loadedPLC;
 	public Hashtable<Integer, Block> blockTable;
 	private ArrayList<Integer> endBlocks;
 	private	LinkedList<Block> activeBlocks;
 	public LinkedList<TrainWrapper> trainList;
 
 	private CTCOffice mainCTCOffice;
 	private SafetyInfo currStateInfo;
 	public Switch	centralSwitch;
 
 
 	/**
 	 * Wayside Constructor
 	 * @param table
 	 * @param mainCTCOffice
 	 * @param currInfo
 	 */
 	public Wayside(Hashtable<Integer, Block> table, ArrayList<Integer> endBlks, CTCOffice mainCTCOffice, SafetyInfo currInfo, Switch cSwitch) {
 		this.blockTable = table;
 		this.endBlocks = endBlks;
 		this.mainCTCOffice = mainCTCOffice;
 		this.currStateInfo = currInfo;
 		this.centralSwitch = cSwitch;
 
 		trainList = new LinkedList<TrainWrapper>();
 		boolean on;
 		if (currInfo.lightState.getState() == 0)
 			on = false;
 		else
 			on = true;
 		this.loadedPLC = new PLCProgram(currInfo.lightState.getBlockLocation(), on);
 	}
 
 	/*public void setPLC(PLCProgram plc) {
 		this.loadedPLC = plc;
 	}*/
 
 	/**
 	 * Updates active block list with all currently occupied blocks in wayside's zone.
 	 * Accomplishes the task by retrieving all values in the block table and iterating
 	 * through the list to check each block's current occupied status.
 	 */
 	private void updateActiveBlocks() {
 		Iterator<Block> itr = blockTable.values().iterator();
 		Block temp;
 
 		activeBlocks = new LinkedList<Block>();
 		//System.out.println("BLOCKS: ");
 		while (itr.hasNext())
 		{
 			temp = itr.next();
 			if(temp.occupied) {
 				activeBlocks.add(temp);
 
 				//DEBUG
 				//System.out.println("\t" + temp.id + " Active");
 			} else {
 				//System.out.println("\t" + temp.id);
 			}
 		}
 	}
 
 	/**
 	 * Sets the current limits to the specified block
 	 * @param blockID
 	 * @param current
 	 */
 	private void setLimits(Integer blockID, Limits current) {
 		Block temp = blockTable.get(blockID);
 		temp.fbAuthority = current.getAuthority();
 		temp.fbSpeed = current.getSpeedLimit();
 		System.out.println("\nFB_Speed: " + current.getSpeedLimit() + "    FB_Auth : " + current.getAuthority());
 	}
 
 	/**
 	 * Updates the wayside's train list
 	 */
 	private void updateTrainList() {
 		//System.out.println("\nupdateTrainList() called");
 
 		/*If list empty than search if any end blocks currently are active */
 		if (trainList.isEmpty()) {
 			for (Block blk : activeBlocks) {
 				System.out.println("\nBlock :" + blk.id + " is active");
 				if (TrainWrapper.isEndBlk(endBlocks, blk.id)) {
 					Block fwd = blk.getNext(Block.DIRECTION_FWD);
 					Block rev = blk.getNext(Block.DIRECTION_REV);		
 
 					/*Train temp = mainCTCOffice.getTrainsInBlock(blk.id).get(0);
 					trainList.add(new TrainWrapper(temp, blk));*/ 
 
 					ArrayList<Train> tmpList = mainCTCOffice.getTrainsInBlock(blk.id);
 					if (tmpList.size() == 0) {
 						if (fwd.isOccupied()) {
 							if (blockIsInZone(fwd)) {
 								ArrayList<Train> tmpListTwo = mainCTCOffice.getTrainsInBlock(blk.id);
 								if (tmpListTwo.size() > 0) {
 									trainList.add(new TrainWrapper(tmpListTwo.get(0), fwd));
 								}
 							}
 						} else if (rev.isOccupied()) {
 							if (blockIsInZone(rev)) {
 								ArrayList<Train> tmpListTwo = mainCTCOffice.getTrainsInBlock(blk.id);
 								if (tmpListTwo.size() > 0) {
 									trainList.add(new TrainWrapper(tmpListTwo.get(0), rev));
 								}
 							}
 						}
 					} else {
 						trainList.add(new TrainWrapper(tmpList.get(0), blk));
 					}
 
 				}
 			}
 		}else {
 			//updates positions of currently tracked trains
 			LinkedList<TrainWrapper> removals = new LinkedList<TrainWrapper>();
 
 			for (TrainWrapper sTrain : trainList) {
 				//sTrain.updateLocation();
 				if (!blockIsInZone(sTrain.getBlockLocation()))
 					removals.add(sTrain);
 			}
 
 			removeFromList(removals);
 
 			//adds new trains found on endblocks that weren't in the train list
 			for (Integer blkId : endBlocks) {
 				if (isActive(blkId)) {
 					boolean flag = false;
 					for (TrainWrapper sTrain : trainList) {
 						if (sTrain.getBlockLocation().id == blkId) {
 							flag = true;
 							break;
 						}
 					}
 					if (!flag) {
 						for (Train tR : mainCTCOffice.getTrainsInBlock(blkId)) {
 							if (!inTrainList(tR))
 								trainList.add(new TrainWrapper(tR, blockTable.get(blkId)));
 						}
 					}
 				}
 			}
 		}
 	}
 
 
 	public void removeFromList(LinkedList<TrainWrapper> removals) {
 		for (TrainWrapper rTrain : removals){
 			int i = 0;
 			boolean found = false;
 			for (TrainWrapper sTrain : trainList){
 				if (rTrain.train.id == sTrain.train.id) {
 					found = true;
 					break;
 				}
 				i++;
 			}
 			if (found)
 				trainList.remove(i);
 		}
 	}
 	/**
 	 * checks if specified train is in the current trainlist
 	 * @param tr
 	 * @return
 	 */
 	public boolean inTrainList(Train tr) {
 		for (TrainWrapper sTrain : trainList) {
 			if (sTrain.train.id == tr.id)
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * checks if block id is an active block
 	 * @param id
 	 * @return
 	 */
 	private boolean isActive(int id) {
 		for (Block blk : activeBlocks) {
 			if (blk.id == id) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * checks if block is in wayside's zone
 	 * @param blk
 	 * @return
 	 */
 	private boolean blockIsInZone(Block blk) {
 
 		Iterator<Block> itr = blockTable.values().iterator();
 		Block tempBlk;
 
 		if (blk == null)
 			return false;
 
 		while(itr.hasNext())
 		{
 			tempBlk = itr.next();
 			if (tempBlk.id == blk.id) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * checks for any broken track circuits by comparing active blocks to verified trains' locations.
 	 */
 	private void checkForBrokenCircuits() {
 		for (Block blk : activeBlocks) {
 			boolean flag = false;
 			for (TrainWrapper sTrain : trainList) {
 				if (blk.id == sTrain.train.positionBlock.id) {
 					flag = true;
 					break;
 				}
 			}
 			//block doesn't have a recorded train and therefore contains a malfunction circuit
 			if (!flag) {
				/* XXX modified by Stephen */
				/* brokenRailFailure is the state of the failure, not the indicator */
				//blk.brokenRailFailure = true;

				/* Tell the CTC that there's a broken rail */
 			}
 		}
 	}
 
 
 	/**
 	 * Activates the wayside for its next cycle
 	 */
 	public void nextTick() {
 		updateActiveBlocks();
 		updateTrainList();

 		checkForBrokenCircuits();
 
 		//Now stage is set
 
 		//run PLC redundancy 
 		runRedundancy();
 
 		//set safetyInfo
 		if (currStateInfo.safetyState) {
 
 			if (currStateInfo.lightState.getBlockLocation() > 0) {
 				toggleComponent(currStateInfo.lightState.getBlockLocation(), 
 						ComponentType.LIGHT_COMP, currStateInfo.lightState.getState());
 			}
 
 			toggleSwitch(currStateInfo.switchState);
 
 			//calculate limits
 			for (TrainWrapper sTrain : trainList)
 			{
 				//if train's next block is in the zone then it is receiving the train and should edit the limits
 				//otherwise it shouldn't set the limits
 				if (sTrain.getFutureBlock() != null) {
 					
 					if (TrainWrapper.isEndBlk(endBlocks, sTrain.train.route.get(sTrain.train.routeIndex).id) && 
 							!blockIsInZone(sTrain.train.route.get(sTrain.train.routeIndex + 1))) {
 						continue;
 					} else {
 						if (blockIsInZone(sTrain.getFutureBlock())) {
 
 							Limits newLimits;
 							double auth;
 							double currSpeed = sTrain.train.curVelocity;
 
 							double distance = Double.MAX_VALUE;
 							for (TrainWrapper tTrain : trainList)
 							{
 								if (tTrain.train.id != sTrain.train.id) {
 									double temp = sTrain.distToTrain(endBlocks, tTrain);
 									if (temp < distance)
 										distance = temp;
 								}
 							}
 
 							//only train on the track or no tracks are ahead or something went wrong somehow
 							if (distance == Double.MAX_VALUE) {
 								distance = distToEndOfZone(sTrain); //make a safety judgment to end of zone
 							}
 
 							double tempAuth = 0;
 							boolean notGood = true;
 							while (currSpeed > 0 && notGood) {
 
 								tempAuth = -Math.pow(currSpeed, 2.0)/(2*-1.2);
 								if (tempAuth <= distance)
 								{
 									auth = distance - tempAuth;
 									if (auth > 250.0)
 										newLimits = new Limits(sTrain.getBlockLocation().speedLimit, auth);
 									else
 										newLimits = new Limits(currSpeed, auth);
 									sTrain.setCurrLimits(newLimits);
 									setLimits(sTrain.getBlockLocation().id,sTrain.getCurrLimits());
 									notGood = false;
 								}
 								currSpeed = currSpeed - 1.0;
 							}
 
 							if (notGood)
 							{
 								newLimits = new Limits(sTrain.getBlockLocation().speedLimit, 200.0);
 								sTrain.setCurrLimits(newLimits);
 								setLimits(sTrain.getBlockLocation().id,sTrain.getCurrLimits());
 
 								// TO-DO must adjust suggested speed
 								System.out.print("Authority Calculation may be incorrect");
 
 							}
 						}
 					}
 				}
 			}
 
 
 		}
 		else {
 			shutdown();
 		}
 	}
 
 	/**
 	 * Toggles the selected component's state in the designated block
 	 * @param blkID
 	 * @param state
 	 */
 	private void toggleComponent(Integer blkID, ComponentType type, int state) {
 		switch (type) {
 		case LIGHT_COMP:
 			//set light state
 			if (state == 0)
 				blockTable.get(blkID).isCrossingOn = false;
 			else
 				blockTable.get(blkID).isCrossingOn = true;
 			break;
 		}
 	}
 
 	private void toggleSwitch(boolean currState) {
 		this.centralSwitch.state = currState;
 	}
 	
 
 	private double distToEndOfZone(TrainWrapper sTrain) {
 		double dist = 0.0;
 		Train train = sTrain.train;
 		int index = train.routeIndex;
 		boolean flag = true;
 
 		if (TrainWrapper.isEndBlk(endBlocks, train.route.get(index).id) && 
 				!blockIsInZone(train.route.get(index + 1))) {
 			return 0.0;
 		}
 		while (flag) {
 			try {
 				if (TrainWrapper.isEndBlk(endBlocks, train.route.get(index + 1).id)) {
 					dist += train.route.get(index + 1).length;
 					flag = false;
 				}else {
 					dist += train.route.get(index + 1).length;
 					index++;
 				}
 			}catch (IndexOutOfBoundsException e) {
 				flag = false;
 			}
 		}
 
 		return dist;
 
 	}
 
 	/**
 	 * Run the triple redundancy protocol for the PLCProgram 
 	 */
 	public void runRedundancy() {
 
 		SafetyInfo runOne = loadedPLC.runPLC(blockTable, endBlocks, activeBlocks, trainList, centralSwitch);
 		SafetyInfo runTwo = loadedPLC.runPLC(blockTable, endBlocks, activeBlocks, trainList, centralSwitch);
 		SafetyInfo runThree = loadedPLC.runPLC(blockTable, endBlocks, activeBlocks, trainList, centralSwitch);
 
 		boolean isSafe;
 		boolean swState;
 		if (runOne.safetyState == runTwo.safetyState && runOne.safetyState == runThree.safetyState) {
 			isSafe = runOne.safetyState;
 		} else if (runOne.safetyState != runTwo.safetyState && runOne.safetyState == runThree.safetyState) {
 			isSafe = runOne.safetyState;
 		} else if (runOne.safetyState != runTwo.safetyState && runOne.safetyState != runThree.safetyState) {
 			isSafe = runTwo.safetyState;
 		} else {
 			isSafe = runTwo.safetyState;
 		}
 
 		if (runOne.switchState == runTwo.switchState && runOne.switchState == runThree.switchState) {
 			swState = runOne.switchState;
 		} else if (runOne.switchState != runTwo.switchState && runOne.switchState == runThree.switchState) {
 			swState = runOne.switchState;
 		} else if (runOne.switchState != runTwo.switchState && runOne.switchState != runThree.switchState) {
 			swState = runTwo.switchState;
 		} else {
 			swState = runTwo.switchState;
 		}
 
 		currStateInfo = new SafetyInfo(swState, runOne.lightState, isSafe);
 	}
 
 	/**
 	 * Shuts down the NSECS application due to unsafe conditions
 	 */
 	private void shutdown() {
 		System.out.println("Track is Unsafe and therefore must be shutdown!!!\n");
 		
 		Iterator<Block> itr = blockTable.values().iterator();
 		Block temp;
 
 		activeBlocks = new LinkedList<Block>();
 		System.out.println("BLOCKS: ");
 		Limits emerg = new Limits(0, 0);
 		while (itr.hasNext())
 		{
 			temp = itr.next();
 			setLimits(temp.id, emerg);
 		}
 	}
 
 	public enum ComponentType {
 		SWITCH_COMP, LIGHT_COMP;
 	}
 
 
 }
