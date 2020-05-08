 package factory;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import factory.Kit.KitState;
 import factory.interfaces.*;
 import factory.masterControl.MasterControl;
 import agent.Agent;
 
 public class PartsRobotAgent extends Agent implements PartsRobot {
 	
 /** ================================================================================ **/
 /** 									Data	 									 **/
 /** ================================================================================ **/
 		
 		// ENUM to keep track of the position of the PartsRobot
 		public enum PartsRobotPositions { CENTER, NEST_ZERO, NEST_ONE, NEST_TWO, NEST_THREE, NEST_FOUR, NEST_FIVE, NEST_SIX, NEST_SEVEN }
 		public PartsRobotPositions position;
 	
 		// ENUM to keep track of state of kitConfig
 		public enum KitConfigState { EMPTY, REQUESTED, PRODUCING }
 		public KitConfigState currentKitConfigurationState;
 		
 		// ENUM to keep state of the nests
 		public enum NestState { DOING_NOTHING, PICTURE_NEEDED, WAITING_ON_PICTURE, PICK_UP_NEEDED }
 		
 		// Configuration of Kit we are currently producing
 		public KitConfig currentKitConfiguration;
 		
 		// Kits to keep track of kits in slots
 		public KitConfig topSlot;
 		public KitConfig bottomSlot;
 
 		// ENUM to know state of the stand
 		public enum StandState { DOING_NOTHING, WAITING_FOR_RESPONSE, DELIVERY_AUTHORIZED};
 		public StandState standState;
 		
 		// ENUM to know state of the stand
 		public enum SlotState { EMPTY, BUILD_REQUESTED, BUILDING };
 		public SlotState topSlotState;
 		public SlotState bottomSlotState;
 		
 		// Nests
 		public List<MyNest> nests = Collections.synchronizedList(new ArrayList<MyNest>());
 
 		// Agents
 		public FCS fcs;
 		public Vision vision;
 		public Stand stand;
 
 		public Part armOne;
 		public Part armTwo;
 		public Part armThree;
 		public Part armFour;
 		/**
 		 * Private class to keep track of nests
 		 */
 		public class MyNest {
 			public Nest nest;
 			public NestState state;
 			public Part part;
 			public int partCoordinate;
 			public int partsTaken = 0;
 		
 			public MyNest(Nest nest){
 				this.nest = nest;
 				this.partCoordinate = -1;
 				this.state = NestState.DOING_NOTHING;
 				this.part = null;
 			}
 		}
 	
 	/**
 	 * Constructor for PartsRobot Agent
 	 * @param mc
 	 */
 	public PartsRobotAgent(MasterControl mc, FCS fcs, Vision vision, Stand stand, List<Nest> nests) {
 		
 		// Call Constructor of Parent Class
 		super(mc);
 		
 		// Parts Robot starts in the middle
 		position = PartsRobotPositions.CENTER;
 		
 		// The current configuration is null
 		this.currentKitConfiguration = null;
 		this.currentKitConfigurationState = KitConfigState.EMPTY;
 		this.topSlot = null;
 		this.bottomSlot = null;
 		this.topSlotState = SlotState.EMPTY;
 		this.bottomSlotState = SlotState.EMPTY;
 		
 		// Agents
 		this.fcs = fcs;
 		this.vision = vision;
 		this.stand = stand;
 		this.standState = StandState.DOING_NOTHING;
 		
 		for(int i = 0; i < 8; i++){
 			this.nests.add(new MyNest(nests.get(i)));
 		}
 
 		this.armOne = null;
 		this.armTwo = null;
 		this.armThree = null;
 		this.armFour = null;
 		
 	}
 
 /** ================================================================================ **/
 /** 									MESSAGES 									 **/
 /** ================================================================================ **/
 	
 	/**
 	 * Message that is received from the FCS when a new kit configuration comes in.
 	 */
 	public void msgMakeKit(KitConfig kitConfig) {
 		debug("Received msgMakeKit("+kitConfig.kitName+")");
 		this.currentKitConfiguration = kitConfig;
 		this.currentKitConfigurationState = KitConfigState.REQUESTED;
 		this.stateChanged();
 	}
 
 	/**
 	 * Message that is received from the Stand and tells us which kit to build
 	 */
 	public void msgBuildKitAtSlot(String slot) {
 		debug("Received msgBuildKitAtSlot("+slot+")");
 
 		if (slot.equals("topSlot")){
 			this.topSlotState = SlotState.BUILD_REQUESTED;
 		}
 		else {
 			this.bottomSlotState = SlotState.BUILD_REQUESTED;
 		}
 		this.stateChanged();
 	}
 
 	/**
 	 * Message received from the Stand when permission has been granted
 	 */
 	public void msgDeliverKitParts() {
 		this.standState = StandState.DELIVERY_AUTHORIZED;
 		this.stateChanged();
 	}
 	
 	/**
 	 * Message that is received from Vision to tell us to clear sight over nests to take picture
 	 */
 	public void msgClearLineOfSight(Nest nestOne, Nest nestTwo) {
 		debug("Received msgClearLineOfSight("+nestOne+", "+nestTwo+")");
 		for(int i = 0; i < nests.size(); i++){
 			
 			if(nests.get(i).nest == nestOne || nests.get(i).nest == nestTwo){
 				
 				nests.get(i).state = NestState.PICTURE_NEEDED;
 			}
 		}
 		this.stateChanged();
 	}
 
 	/**
 	 * Message that is received from Vision to tell us that a picture was taken
 	 */
 	public void msgPictureTaken(Nest nestOne, Nest nestTwo) {
 		debug("Received msgPictureTaken("+nestOne+", "+nestTwo+")");
 		for(int i = 0; i < nests.size(); i++){
 			if(nests.get(i).nest == nestOne || nests.get(i).nest == nestTwo){
 				nests.get(i).state = NestState.DOING_NOTHING;
 			}
 		}
 		this.stateChanged();
 	}
 
 	/**
 	 * Message that is received from Vision tells which nest has a good part and its coordinate
 	 */
 	public void msgHereArePartCoordinatesForNest(Nest nest, Part part, int coordinate) {
 		debug("received msgHereArePartCoordinatesForNest("+nest+"," +part.name+","+coordinate+")");
 		for(int i = 0; i < nests.size(	); i++){
 			if(nests.get(i).nest == nest && nests.get(i).part.name == part.name){
 				nests.get(i).state = NestState.PICK_UP_NEEDED;
 				nests.get(i).partCoordinate = coordinate;
 			}
 		}
 		this.stateChanged();
 	}	
 	
 	public void msgNoMoreOrders() {
 		debug("####################");
 		debug("NO MORE ORDERS");
 		debug("####################");
 		this.stand.msgClearStand();
 		this.currentKitConfiguration = null;
 		this.currentKitConfigurationState = KitConfigState.EMPTY;
 		
 		this.stateChanged();
 	}
 
 	
 /** ================================================================================ **/
 /** 									SCHEDULER 									 **/
 /** ================================================================================ **/
 
 	public boolean pickAndExecuteAnAction() {
 		
 		synchronized(this.currentKitConfigurationState){
 			
 			// If a new Kit Configuration was requested
 			if (this.currentKitConfigurationState == KitConfigState.REQUESTED) {
 				DoProcessNewKitConfiguration();
 				return true;
 			}
 			
 			// 
 			if(this.currentKitConfigurationState == KitConfigState.EMPTY && !ArmsEmpty()){
 				DoClearArms();
 				return true;
 			}
 			
 			// If stand told us to build Kit
 			if (this.currentKitConfigurationState == KitConfigState.PRODUCING && this.topSlotState == SlotState.BUILD_REQUESTED){
 				DoStartBuildingKitAtSlot("topSlot");
 				return true;
 			}
 			
 			// If stand told us to build Kit
 			if (this.currentKitConfigurationState == KitConfigState.PRODUCING && this.bottomSlotState == SlotState.BUILD_REQUESTED){
 				DoStartBuildingKitAtSlot("bottomSlot");
 				return true;
 			}
 
 			if(this.standState == StandState.DELIVERY_AUTHORIZED){
 				DoDeliverPartsToStand();
 				return true;
 			}
 			// If there is a part to be picked up and space in the arms and the PartsRobot can get there without overlapping with the camera
 			for(int i = 0; i < 8; i++){
 				if(this.standState != StandState.DELIVERY_AUTHORIZED && nests.get(i).state == NestState.PICK_UP_NEEDED 
 						&& SpaceInArms() && this.CanMoveToNest(i) && IsPartFromNestNeed(i)){
 					DoPickUpPartFromNest(i);
 					return true;
 				}
 			}
 			// If there is a part to be picked up and space in the arms and the PartsRobot can get there without overlapping with the camera
 			for(int i = 0; i < 8; i++){
 				if(nests.get(i).partsTaken > 5){
 					DoReOrderParts(i);
 					return true;
 				}
 			}
 
 			if(this.standState == StandState.DOING_NOTHING && !ArmsEmpty()){
 				DoAskPermisionToDeliverParts();
 				return true;
 			}
 			
 			if(this.position != PartsRobotPositions.CENTER){
 				DoMovePartsRobotToCenter();
 				return true;
 			}
 			
 			// If a picture is needed, check if we are not in the way and tell camera it is clear
 			if(nests.get(0).state == NestState.PICTURE_NEEDED && nests.get(1).state == NestState.PICTURE_NEEDED && IsVisionClear(0,1)){
 				DoTellVisionNestsAreClearToTakePicture(0,1);
 				return true;
 			}
 			if(nests.get(2).state == NestState.PICTURE_NEEDED && nests.get(3).state == NestState.PICTURE_NEEDED && IsVisionClear(2,3)){
 				DoTellVisionNestsAreClearToTakePicture(2,3);
 				return true;
 			}
 			if(nests.get(4).state == NestState.PICTURE_NEEDED && nests.get(5).state == NestState.PICTURE_NEEDED && IsVisionClear(4,5)){
 				DoTellVisionNestsAreClearToTakePicture(4,5);
 				return true;
 			}
 			if(nests.get(6).state == NestState.PICTURE_NEEDED && nests.get(7).state == NestState.PICTURE_NEEDED && IsVisionClear(6,7)){
 				DoTellVisionNestsAreClearToTakePicture(6,7);
 				return true;
 			}
 		}	
 		
 		
 		return false;
 	}
 
 	
 /** ================================================================================ **/
 /** 									ACTIONS 									 **/
 /** ================================================================================ **/
 	
 	/**
 	 * Action to process the new Kit Configuration
 	 */
 	public void DoProcessNewKitConfiguration() {
 		debug("Executing DoProcessNewKitConfiguration()");
 		
 		// set parts to corresponding nests - this algorithm won't replace lanes that are needed in the new one but already present in the old
 		List<Integer> newNeeded = new ArrayList<Integer>();
 		List<Integer> currentNotNeeded = new ArrayList<Integer>();
 		
 		
 		// Check which new ones are not in the old config
 		for(int i = 0; i < currentKitConfiguration.listOfParts.size(); i++){
 			boolean present = false;
 			
 			for(int j = 0; j < nests.size(); j++){
 				
 				if(nests.get(j).part != null && nests.get(j).part.name == currentKitConfiguration.listOfParts.get(i).name ){
 					present = true;
 				}
 			}
 			
 			if(!present){
 				newNeeded.add(i);
 			}
 		}
 		
 		// Check which old ones are not in the new config
 		for(int i = 0; i < nests.size(); i++){
 			boolean present = false;
 			
 			for(int j = 0; j < currentKitConfiguration.listOfParts.size(); j++){
 				if(nests.get(j).part != null && nests.get(j).part.name == currentKitConfiguration.listOfParts.get(i).name ){
 					present = true;
 				}
 			}
 			
 			if(!present){
 				currentNotNeeded.add(i);
 			}
 		} 
 		
 		
 		for(int i = 0; i < currentNotNeeded.size(); i++){
 			if(i < newNeeded.size()){
 				nests.get(currentNotNeeded.get(i)).part = currentKitConfiguration.listOfParts.get(newNeeded.get(i));
 				nests.get(currentNotNeeded.get(i)).partCoordinate = -1;
 				nests.get(currentNotNeeded.get(i)).nest.msgYouNeedPart(currentKitConfiguration.listOfParts.get(newNeeded.get(i)));
 				nests.get(currentNotNeeded.get(i)).state = NestState.DOING_NOTHING;
 				nests.get(currentNotNeeded.get(i)).partsTaken = 0;
 				nests.get(currentNotNeeded.get(i)).nest.setBeingUsed(true);
 			}
 			else {
 				nests.get(currentNotNeeded.get(i)).part = null;
 				nests.get(currentNotNeeded.get(i)).partCoordinate = -1;
 				nests.get(currentNotNeeded.get(i)).state = NestState.DOING_NOTHING;
 				nests.get(currentNotNeeded.get(i)).partsTaken = 0;
 				nests.get(currentNotNeeded.get(i)).nest.setBeingUsed(false);
 			}
 			
 			
 		}
 		
 		
 		// The new configuration is now in prodution
 		this.currentKitConfigurationState = KitConfigState.PRODUCING;
 		
 		// Clear arms
 		if(this.armOne != null || this.armTwo != null || this.armThree != null || this.armFour != null){
 			DoClearArms();
 		}
 		ArrayList<Nest> nestsToVision = new ArrayList<Nest>();
 		for(int i = 0; i < this.nests.size(); i ++)
 			nestsToVision.add(this.nests.get(i).nest);
 		this.vision.msgNewNestConfig(nestsToVision);
 		
 		// Reset the slots to empty
 		if(this.topSlotState == SlotState.BUILDING && this.stand.getSlotKit("topSlot") != null && this.stand.getSlotKit("topSlot").parts.size() != 0){
 			this.topSlot = null;
 			this.topSlotState = SlotState.EMPTY;
 		}
 		else if(this.topSlotState == SlotState.BUILDING && this.stand.getSlotKit("topSlot") != null && this.stand.getSlotKit("topSlot").parts.size() == 0){
 			this.topSlot = currentKitConfiguration;
 		}
 		
 		if(this.bottomSlotState == SlotState.BUILDING && this.stand.getSlotKit("bottomSlot") != null && this.stand.getSlotKit("bottomSlot").parts.size() != 0){
 			this.bottomSlot = null;
 			this.bottomSlotState = SlotState.EMPTY;
 		}
 		else if(this.bottomSlotState == SlotState.BUILDING && this.stand.getSlotKit("bottomSlot") != null && this.stand.getSlotKit("bottomSlot").parts.size() == 0){
 			this.bottomSlot = currentKitConfiguration;
 		}
 		
 		// Tell the stand to clear stand
 		stand.msgClearStand();
 	}
 	
 	/**
 	 * Action to start producing a Kit in Slot
 	 */
 	public void DoStartBuildingKitAtSlot(String slot){
 		debug("Executing DoStartBuildingKitAtSlot("+slot+")");
 		KitConfig newKitConfig = new KitConfig();
 		newKitConfig.kitName = this.currentKitConfiguration.kitName;
 		for(int i =0; i <  this.currentKitConfiguration.listOfParts.size(); i++){
 
 			newKitConfig.listOfParts.add(new Part(this.currentKitConfiguration.listOfParts.get(i).name, this.currentKitConfiguration.listOfParts.get(i).id, this.currentKitConfiguration.listOfParts.get(i).description, this.currentKitConfiguration.listOfParts.get(i).imagePath, this.currentKitConfiguration.listOfParts.get(i).nestStabilizationTime));
 		}
 		newKitConfig.quantity = this.currentKitConfiguration.quantity;
 		if(slot.equals("topSlot")){
 			this.topSlot = newKitConfig;
 			this.topSlotState = SlotState.BUILDING;
 		}
 		else {
 			this.bottomSlot = newKitConfig;
 			this.bottomSlotState = SlotState.BUILDING;
 		}
 	}
 	
 	/**
 	 * Action to tell vision that sight is clear for picture
 	 */
 	public void DoTellVisionNestsAreClearToTakePicture(int nestOne, int nestTwo){
 		debug("Executing DoTellVisionNestsAreClearToTakePicture("+nestOne+","+nestTwo+")");
 		
 		// Update States
 		this.nests.get(nestOne).state = NestState.WAITING_ON_PICTURE;
 		this.nests.get(nestTwo).state = NestState.WAITING_ON_PICTURE;
 		
 		// Tell Vision
 		vision.msgVisionClearForPictureInNests(this.nests.get(nestOne).nest, this.nests.get(nestTwo).nest);
 	}
 
 	/**
 	 * Function that clears the arms of the PartsRobot
 	 */
 	public void DoClearArms(){
 		debug("Executing DoClearArms()");
 
 		DoAnimationClearArms();
 		
 		// Clear the arms
 		this.armOne = null;
 		this.armTwo = null;
 		this.armThree = null;
 		this.armFour = null;
 		
 	}
 
 	/**
 	 * Function that moves PartsRobot to nest and picks up the part
 	 */
 	public void DoPickUpPartFromNest(int nest){
 		debug("Executing DoPickUpPartFromNest("+nest+")");
 
 		// Animation of Moving to the Nest
 		DoAnimationMovePartsRobotToNestAndGrabPart(nest, this.nests.get(nest).partCoordinate);
 		
 		// Update Position
 		switch(nest){
 			case 0:
 				this.position = PartsRobotPositions.NEST_ZERO;
 				break;
 			case 1:
 				this.position = PartsRobotPositions.NEST_ONE;
 				break;
 			case 2:
 				this.position = PartsRobotPositions.NEST_TWO;
 				break;
 			case 3:
 				this.position = PartsRobotPositions.NEST_THREE;
 				break;
 			case 4:
 				this.position = PartsRobotPositions.NEST_FOUR;
 				break;
 			case 5:
 				this.position = PartsRobotPositions.NEST_FIVE;
 				break;
 			case 6:
 				this.position = PartsRobotPositions.NEST_SIX;
 				break;
 			case 7:
 				this.position = PartsRobotPositions.NEST_SEVEN;
 				break;
 		}
 		
 		// Place part in arm
 		if(this.armOne == null){
 			this.armOne = (Part) this.nests.get(nest).part.clone();
 		}
 		else if(this.armTwo == null){
 			this.armTwo = (Part) this.nests.get(nest).part.clone();
 		}
 		else if(this.armThree == null){
 			this.armThree = (Part) this.nests.get(nest).part.clone();
 		}
 		else if(this.armFour == null){
 			this.armFour = (Part) this.nests.get(nest).part.clone();
 		}
 		
 		// Update the nest state
 		this.nests.get(nest).partsTaken++;
 		this.nests.get(nest).state = NestState.DOING_NOTHING;
 		this.nests.get(nest).partCoordinate = -1;
 	}
 
 	/**
 	 * Function that moves the PartsRobot to the middle
 	 */
 	public void DoMovePartsRobotToCenter(){
 		debug("Executing DoMovePartsRobotToCenter()");
 
 		// Animation 
 		DoAnimationMovePartsRobotToCenter();
 		
 		this.position = PartsRobotPositions.CENTER;
 	}
 	
 	/**
 	 * Function that asks the Stand permission to deliver items
 	 */
 	public void DoAskPermisionToDeliverParts(){
 		debug("Executing DoAskPermisionToDeliverParts()");
 		this.stand.msgPartRobotWantsToPlaceParts();
 		this.standState = StandState.WAITING_FOR_RESPONSE;
 	}
 	
 	/**
 	 * Function that delivers parts to stand and returns to the middle
 	 */
 	public void DoDeliverPartsToStand(){
 		// Animation to Stand to Kit 1
 		if(this.topSlotState != SlotState.EMPTY){
 			DoAnimationMovePartsRobotToStand(0);
 			if(this.armOne != null &&  this.topSlot != null){
 				boolean placed = false;
 				// try to place in first kit
 				for(int i = 0; !placed && i < this.topSlot.listOfParts.size(); i++){
 					if(this.topSlot.listOfParts.get(i).name == this.armOne.name){
 						this.stand.getSlotKit("topSlot").parts.add(this.armOne);
 						this.topSlot.listOfParts.remove(i);
 						placed = true;
 						this.armOne = null;
 						DoAnimationPutPartInKit(0);
 						if(this.topSlot.listOfParts.size() == 0){
 							this.stand.getSlotKit("topSlot").state = KitState.COMPLETE;
 							this.topSlot = null;
 							this.topSlotState = SlotState.EMPTY;
 						}
 					}
 				}
 			}
 			
 			if(this.armTwo != null &&  this.topSlot != null){
 				boolean placed = false;
 				// try to place in first kit
 				for(int i = 0; !placed && i < this.topSlot.listOfParts.size(); i++){
 					if(this.topSlot.listOfParts.get(i).name == this.armTwo.name){
 						this.stand.getSlotKit("topSlot").parts.add(this.armTwo);
 						this.topSlot.listOfParts.remove(i);
 						placed = true;
 						this.armTwo = null;
 						DoAnimationPutPartInKit(1);
 						if(this.topSlot.listOfParts.size() == 0){
 							this.stand.getSlotKit("topSlot").state = KitState.COMPLETE;
 							this.topSlot = null;
 							this.topSlotState = SlotState.EMPTY;
 						}
 					}
 				}
 			}
 			
 			if(this.armThree != null &&  this.topSlot != null){
 				boolean placed = false;
 				// try to place in first kit
 				for(int i = 0; !placed && i < this.topSlot.listOfParts.size(); i++){
 					if(this.topSlot.listOfParts.get(i).name == this.armThree.name){
 						this.stand.getSlotKit("topSlot").parts.add(this.armThree);
 						this.topSlot.listOfParts.remove(i);
 						placed = true;
 						this.armThree = null;
 						DoAnimationPutPartInKit(2);
 						if(this.topSlot.listOfParts.size() == 0){
 							this.stand.getSlotKit("topSlot").state = KitState.COMPLETE;
 							this.topSlot = null;
 							this.topSlotState = SlotState.EMPTY;
 						}
 					}
 				}
 			}
 			
 			if(this.armFour != null &&  this.topSlot != null){
 				boolean placed = false;
 				// try to place in first kit
 				for(int i = 0; !placed && i < this.topSlot.listOfParts.size(); i++){
 					if(this.topSlot.listOfParts.get(i).name == this.armFour.name){
 						this.stand.getSlotKit("topSlot").parts.add(this.armFour);
 						this.topSlot.listOfParts.remove(i);
 						placed = true;
 						this.armFour = null;
 						DoAnimationPutPartInKit(3);
 						if(this.topSlot.listOfParts.size() == 0){
 							this.stand.getSlotKit("topSlot").state = KitState.COMPLETE;
 							this.topSlot = null;
 							this.topSlotState = SlotState.EMPTY;
 						}
 					}
 				}
 			}
 		}
 		
 		if(this.bottomSlotState != SlotState.EMPTY && !ArmsEmpty()){
 			// Animation to Stand to Kit 2
 			DoAnimationMovePartsRobotToStand(1);
 			if(this.armOne != null &&  this.bottomSlot != null){
 				boolean placed = false;
 				// try to place in first kit
 				for(int i = 0; !placed && i < this.bottomSlot.listOfParts.size(); i++){
 					if(this.bottomSlot.listOfParts.get(i).name == this.armOne.name){
 						this.stand.getSlotKit("bottomSlot").parts.add(this.armOne);
 						this.bottomSlot.listOfParts.remove(i);
 						placed = true;
 						this.armOne = null;
 						DoAnimationPutPartInKit(0);
 						if(this.bottomSlot.listOfParts.size() == 0){
 							this.stand.getSlotKit("bottomSlot").state = KitState.COMPLETE;
 							this.bottomSlot = null;
 							this.bottomSlotState = SlotState.EMPTY;
 						}
 					}
 				}
 			}
 			
 			if(this.armTwo != null &&  this.bottomSlot != null){
 				boolean placed = false;
 				// try to place in first kit
 				for(int i = 0; !placed && i < this.bottomSlot.listOfParts.size(); i++){
 					if(this.bottomSlot.listOfParts.get(i).name == this.armTwo.name){
 						this.stand.getSlotKit("bottomSlot").parts.add(this.armTwo);
 						this.bottomSlot.listOfParts.remove(i);
 						placed = true;
 						this.armTwo = null;
 						DoAnimationPutPartInKit(1);
 						if(this.bottomSlot.listOfParts.size() == 0){
 							this.stand.getSlotKit("bottomSlot").state = KitState.COMPLETE;
 							this.bottomSlot = null;
 							this.bottomSlotState = SlotState.EMPTY;
 						}
 					}
 				}
 			}
 			
 			if(this.armThree != null &&  this.bottomSlot != null){
 				boolean placed = false;
 				// try to place in first kit
 				for(int i = 0; !placed && i < this.bottomSlot.listOfParts.size(); i++){
 					if(this.bottomSlot.listOfParts.get(i).name == this.armThree.name){
 						this.stand.getSlotKit("bottomSlot").parts.add(this.armThree);
 						this.bottomSlot.listOfParts.remove(i);
 						placed = true;
 						this.armThree = null;
 						DoAnimationPutPartInKit(2);
 						if(this.bottomSlot.listOfParts.size() == 0){
 							this.stand.getSlotKit("bottomSlot").state = KitState.COMPLETE;
 							this.bottomSlot = null;
 							this.bottomSlotState = SlotState.EMPTY;
 						}
 					}
 				}
 			}
 			
 			if(this.armFour != null &&  this.bottomSlot != null){
 				boolean placed = false;
 				// try to place in first kit
 				for(int i = 0; !placed && i < this.bottomSlot.listOfParts.size(); i++){
 					if(this.bottomSlot.listOfParts.get(i).name == this.armFour.name){
 						this.stand.getSlotKit("bottomSlot").parts.add(this.armFour);
 						this.bottomSlot.listOfParts.remove(i);
 						placed = true;
 						this.armFour = null;
 						DoAnimationPutPartInKit(3);
 						if(this.bottomSlot.listOfParts.size() == 0){
 							this.stand.getSlotKit("bottomSlot").state = KitState.COMPLETE;
 							this.bottomSlot = null;
 							this.bottomSlotState = SlotState.EMPTY;
 						}
 					}
 				}
 			}
 		}
 
 		
 		// Animation to Return
 		DoAnimationMovePartsRobotToCenter();
 		
 		// Tell stand
 		debug("Telling Stand msgPartsRobotNoLongerUsingStand()");
 		this.stand.msgPartsRobotNoLongerUsingStand();
 		
 		// Update stand state
 		this.standState = StandState.DOING_NOTHING;
 		
 		// Update position
 		this.position = PartsRobotPositions.CENTER;
 	}
 	
 	/**
 	 * Function to reorder parts
 	 */
 	public void DoReOrderParts(int nest){
 		nests.get(nest).nest.msgYouNeedPart(nests.get(nest).part);
 		this.nests.get(nest).partsTaken = 0;
 	}
 	
 /** ================================================================================ **/
 /** 									ANIMATIONS 									 **/
 /** ================================================================================ **/
 	
 	/**
 	 * Animation that clears any parts from the PartsRobotArms
 	 */
 	public void DoAnimationClearArms(){
 		debug("Executing DoAnimationClearArms()");
 		server.command(this,"pra fpm cmd droppartsrobotsitems");
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Animation that moves the PartsRobot to the nest and grab a part
 	 */
 	public void DoAnimationMovePartsRobotToNestAndGrabPart(int nest, int coordinate){
 		debug("Executing DoAnimationMovePartsRobotToNestAndGrabPart("+nest+")");
 		server.command(this,"pra fpm cmd movetonest " + nest + " " + coordinate);
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Animation that moves the PartsRobot to the center
 	 */
 	public void DoAnimationMovePartsRobotToCenter(){
 		debug("Executing DoAnimationMovePartsRobotToCenter()");
 		server.command(this,"pra fpm cmd movetocenter");
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Animation that moves the PartsRobot to the stand
 	 */
 	public void DoAnimationMovePartsRobotToStand(int kit){
 		debug("Executing DoAnimationMovePartsRobotToStand("+kit+")");
 		server.command(this,"pra fpm cmd movetostand " + kit);
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Animation that puts part in kit
 	 */
 	public void DoAnimationPutPartInKit(int arm){
 		debug("Executing DoAnimationPutPartInKit("+arm+")");
 		server.command(this,"pra fpm cmd putpartinkit " + arm);
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 /** ================================================================================ **/
 /** 									HELPER METHODS 								 **/
 /** ================================================================================ **/
 	
 	
 	/**
 	 * Function that determines if the PartsRobot can move from the current position to a specific nest
 	 * Very messy function - Ask Alfonso (@garzaa) if you need help
 	 */
 	public boolean CanMoveToNest(int nest){
 		// If nest destination is busy (should never happen) then return false
 		if(nests.get(nest).state == NestState.WAITING_ON_PICTURE){
 			return false;
 		}
 		switch(nest){
 			case 0:
 				//Check that adjacent aren't taking a picture
 				if(nests.get(1).state == NestState.WAITING_ON_PICTURE){
 					return false;
 				}
 				switch(this.position){
 					case CENTER:
 						return true;
 					case NEST_ZERO:
 						return true;
 					case NEST_ONE:
 						return true;
 					case NEST_TWO:
 						return true;
 					case NEST_THREE:
 						if(nests.get(2).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_FOUR:
 						if(nests.get(2).state != NestState.WAITING_ON_PICTURE && nests.get(3).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_FIVE:
 						if(nests.get(2).state != NestState.WAITING_ON_PICTURE && nests.get(3).state != NestState.WAITING_ON_PICTURE 
 							&& nests.get(4).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_SIX:
 						if(nests.get(2).state != NestState.WAITING_ON_PICTURE && nests.get(3).state != NestState.WAITING_ON_PICTURE 
 							&& nests.get(4).state != NestState.WAITING_ON_PICTURE && nests.get(5).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_SEVEN:
 						if(nests.get(2).state != NestState.WAITING_ON_PICTURE && nests.get(3).state != NestState.WAITING_ON_PICTURE 
 							&& nests.get(4).state != NestState.WAITING_ON_PICTURE && nests.get(5).state != NestState.WAITING_ON_PICTURE 
 								&& nests.get(6).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 				}
 			case 1:
 				//Check that adjacent aren't taking a picture
 				if(nests.get(0).state == NestState.WAITING_ON_PICTURE || nests.get(2).state == NestState.WAITING_ON_PICTURE){
 					return false;
 				}
 				switch(this.position){
 					case CENTER:
 						return true;
 					case NEST_ZERO:
 						return true;
 					case NEST_ONE:
 						return true;
 					case NEST_TWO:
 						return true;
 					case NEST_THREE:
 						return true;
 					case NEST_FOUR:
 						if(nests.get(3).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_FIVE:
 						if(nests.get(3).state != NestState.WAITING_ON_PICTURE && nests.get(4).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_SIX:
 						if(nests.get(3).state != NestState.WAITING_ON_PICTURE && nests.get(4).state != NestState.WAITING_ON_PICTURE && nests.get(5).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_SEVEN:
 						if(nests.get(3).state != NestState.WAITING_ON_PICTURE && nests.get(4).state != NestState.WAITING_ON_PICTURE && nests.get(5).state != NestState.WAITING_ON_PICTURE 
 								&& nests.get(6).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 				}
 			case 2:
 				//Check that adjacent aren't taking a picture
 				if(nests.get(1).state == NestState.WAITING_ON_PICTURE || nests.get(3).state == NestState.WAITING_ON_PICTURE){
 					return false;
 				}
 				switch(this.position){
 					case CENTER:
 						return true;
 					case NEST_ZERO:
 						return true;
 					case NEST_ONE:
 						return true;
 					case NEST_TWO:
 						return true;
 					case NEST_THREE:
 						return true;
 					case NEST_FOUR:
 						return true;
 					case NEST_FIVE:
 						if(nests.get(4).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_SIX:
 						if(nests.get(4).state != NestState.WAITING_ON_PICTURE && nests.get(5).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_SEVEN:
 						if(nests.get(4).state != NestState.WAITING_ON_PICTURE && nests.get(5).state != NestState.WAITING_ON_PICTURE && nests.get(6).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 				}
 			case 3:
 				//Check that adjacent aren't taking a picture
 				if(nests.get(2).state == NestState.WAITING_ON_PICTURE || nests.get(4).state == NestState.WAITING_ON_PICTURE){
 					return false;
 				}
 				switch(this.position){
 					case CENTER:
 						return true;
 					case NEST_ZERO:
 						if(nests.get(1).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_ONE:
 						return true;
 					case NEST_TWO:
 						return true;
 					case NEST_THREE:
 						return true;
 					case NEST_FOUR:
 						return true;
 					case NEST_FIVE:
 						return true;
 					case NEST_SIX:
 						if(nests.get(5).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_SEVEN:
 						if(nests.get(5).state != NestState.WAITING_ON_PICTURE && nests.get(6).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 				}	
 			case 4:
 				//Check that adjacent aren't taking a picture
 				if(nests.get(3).state == NestState.WAITING_ON_PICTURE || nests.get(5).state == NestState.WAITING_ON_PICTURE){
 					return false;
 				}
 				switch(this.position){
 					case CENTER:
 						return true;
 					case NEST_ZERO:
 						if(nests.get(1).state != NestState.WAITING_ON_PICTURE && nests.get(2).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_ONE:
 						if(nests.get(2).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_TWO:
 						return true;
 					case NEST_THREE:
 						return true;
 					case NEST_FOUR:
 						return true;
 					case NEST_FIVE:
 						return true;
 					case NEST_SIX:
 						return true;
 					case NEST_SEVEN:
 						if(nests.get(6).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 				}
 			case 5:
 				//Check that adjacent aren't taking a picture
 				if(nests.get(4).state == NestState.WAITING_ON_PICTURE || nests.get(6).state == NestState.WAITING_ON_PICTURE){
 					return false;
 				}
 				switch(this.position){
 					case CENTER:
 						return true;
 					case NEST_ZERO:
 						if(nests.get(1).state != NestState.WAITING_ON_PICTURE && nests.get(2).state != NestState.WAITING_ON_PICTURE
 								 && nests.get(3).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_ONE:
 						if(nests.get(2).state != NestState.WAITING_ON_PICTURE && nests.get(3).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_TWO:
 						if(nests.get(3).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_THREE:
 						return true;
 					case NEST_FOUR:
 						return true;
 					case NEST_FIVE:
 						return true;
 					case NEST_SIX:
 						return true;
 					case NEST_SEVEN:
 						return true;
 				}
 			case 6:
 				//Check that adjacent aren't taking a picture
 				if(nests.get(5).state == NestState.WAITING_ON_PICTURE || nests.get(7).state == NestState.WAITING_ON_PICTURE){
 					return false;
 				}
 				switch(this.position){
 					case CENTER:
 						return true;
 					case NEST_ZERO:
 						if(nests.get(1).state != NestState.WAITING_ON_PICTURE && nests.get(2).state != NestState.WAITING_ON_PICTURE
 								&& nests.get(3).state != NestState.WAITING_ON_PICTURE && nests.get(4).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_ONE:
 						if(nests.get(2).state != NestState.WAITING_ON_PICTURE && nests.get(3).state != NestState.WAITING_ON_PICTURE
 								&& nests.get(4).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_TWO:
 						if(nests.get(3).state != NestState.WAITING_ON_PICTURE && nests.get(4).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_THREE:
 						if(nests.get(4).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_FOUR:
 						return true;
 					case NEST_FIVE:
 						return true;
 					case NEST_SIX:
 						return true;
 					case NEST_SEVEN:
 						return true;
 				}
 			case 7:
 				//Check that adjacent aren't taking a picture
 				if(nests.get(6).state == NestState.WAITING_ON_PICTURE){
 					return false;
 				}
 				switch(this.position){
 					case CENTER:
 						return true;
 					case NEST_ZERO:
 						if(nests.get(1).state != NestState.WAITING_ON_PICTURE && nests.get(2).state != NestState.WAITING_ON_PICTURE
 								&& nests.get(3).state != NestState.WAITING_ON_PICTURE && nests.get(4).state != NestState.WAITING_ON_PICTURE
 								&& nests.get(5).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_ONE:
 						if(nests.get(2).state != NestState.WAITING_ON_PICTURE && nests.get(3).state != NestState.WAITING_ON_PICTURE
 								&& nests.get(4).state != NestState.WAITING_ON_PICTURE && nests.get(5).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_TWO:
 						if(nests.get(3).state != NestState.WAITING_ON_PICTURE && nests.get(4).state != NestState.WAITING_ON_PICTURE
 								&& nests.get(5).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_THREE:
 						if(nests.get(3).state != NestState.WAITING_ON_PICTURE && nests.get(5).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_FOUR:
 						if(nests.get(5).state != NestState.WAITING_ON_PICTURE){
 							return true;
 						}
 					case NEST_FIVE:
 						return true;
 					case NEST_SIX:
 						return true;
 					case NEST_SEVEN:
 						return true;
 				}
 		}
 		return false;
 	}
 
 	/**
 	 * Function that determines if there is space in the arms
 	 */
 	public boolean SpaceInArms(){
 		return (this.armOne == null || this.armTwo == null || this.armThree == null || this.armFour == null);
 	}
 	
 	/**
 	 * Function that determines if all arms are empty
 	 */
 	public boolean ArmsEmpty(){
 		return (this.armOne == null && this.armTwo == null && this.armThree == null && this.armFour == null);
 	}
 	
 	/**
 	 * Function that determines if vision is clear for picture in particular nests
 	 */
 	public boolean IsVisionClear(int nestOne, int nestTwo){
 		switch (nestOne) {
 	        case 0:
 	        	if(this.position != PartsRobotPositions.NEST_ZERO && this.position != PartsRobotPositions.NEST_ONE 
 	        			&& this.position != PartsRobotPositions.NEST_TWO)
 	        		return true;
 	        	break;
 	        case 2:
 	        	if(this.position != PartsRobotPositions.NEST_ONE && this.position != PartsRobotPositions.NEST_TWO 
 	        			&& this.position != PartsRobotPositions.NEST_THREE && this.position != PartsRobotPositions.NEST_FOUR )
 	        		return true;
 	    		break;
 	        case 4:
 	        	if(this.position != PartsRobotPositions.NEST_THREE && this.position != PartsRobotPositions.NEST_FOUR 
 	        			&& this.position != PartsRobotPositions.NEST_FIVE && this.position != PartsRobotPositions.NEST_SIX )
 	        		return true;
 	    		break;
 	        case 6:
 	        	if(this.position != PartsRobotPositions.NEST_FIVE && this.position != PartsRobotPositions.NEST_SIX 
 	        			&& this.position != PartsRobotPositions.NEST_SEVEN)
 	        		return true;
 	    		break;
 		}
 		return false;
 	}
 
 	/**
 	 * Function to check if the part of a particular nest is needed for the current kits in the slots
 	 */
 	public boolean IsPartFromNestNeed(int nest){
 		
 		if(this.currentKitConfiguration == null){
 			return false;
 		}
 		int countNeeded = 0;
 		
 		if(this.topSlot != null){
 			for(int i =0;i < this.topSlot.listOfParts.size(); i++){
 				if(this.topSlot.listOfParts.get(i).name.equals(this.nests.get(nest).part.name))
 					countNeeded++;
 			}
 		}
 		if(this.bottomSlot != null){
 			for(int i =0;i < this.bottomSlot.listOfParts.size(); i++){
 				if(this.bottomSlot.listOfParts.get(i).name.equals(this.nests.get(nest).part.name))
 					countNeeded++;
 			}
 		}
 		
 		if(this.armOne != null && this.armOne.name.equals(this.nests.get(nest).part.name))
 			countNeeded--;
 
 		if(this.armTwo != null && this.armTwo.name.equals(this.nests.get(nest).part.name))
 			countNeeded--;
 
 		if(this.armThree != null && this.armThree.name.equals(this.nests.get(nest).part.name))
 			countNeeded--;
 
 		if(this.armFour != null && this.armFour.name.equals(this.nests.get(nest).part.name))
 			countNeeded--;
 		
 		return countNeeded > 0; 
 	}
 }
