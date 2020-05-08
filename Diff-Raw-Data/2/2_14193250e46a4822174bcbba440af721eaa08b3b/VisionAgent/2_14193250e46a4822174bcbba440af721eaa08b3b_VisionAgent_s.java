 package factory;
 
 import java.util.ArrayList;
 
 import agent.Agent;
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 
 import factory.Kit.KitState;
 import factory.interfaces.*;
 import factory.interfaces.Nest.NestState;
 import factory.masterControl.MasterControl;
 
 public class VisionAgent extends Agent implements Vision {
 
 	public enum kitPictureRequeststate { NEED_TO_INSPECT, INSPECTED }
 	public enum PictureRequestState { NESTS_READY, ANALYZED, ASKED_PARTS_ROBOT, PARTS_ROBOT_CLEAR, PICTURE_TAKEN }
 
 	public ArrayList<PictureRequest> pictureRequests = new ArrayList<PictureRequest>(); 
 	public ArrayList<KitPicRequest> kitPictureRequests = new ArrayList<KitPicRequest>();     
 	
 	public PartsRobot partsRobot;
 	public Stand stand;
 	public Random r = new Random();
 	
 	public Semaphore pictureAllowed = new Semaphore(1);
 	
 	public ArrayList<Nest> nests;
 
 	public int[] nestJammedWaiting = new int[8];
 	
 	public Feeder feeder_zero;
 	public Feeder feeder_one;
 	public Feeder feeder_two;
 	public Feeder feeder_three;
 	
 	public VisionAgent(PartsRobot partsRobot, Stand stand, MasterControl mc){
 		super(mc);
 		this.partsRobot = partsRobot;
 		this.stand = stand;
 	}
 
 	public class KitPicRequest {
 
 		public kitPictureRequeststate state;
 		public boolean forceFail = false;
 
 		public KitPicRequest(boolean forceFail) { 
 			state = kitPictureRequeststate.NEED_TO_INSPECT;
 			this.forceFail = forceFail;
 		}
 	}
 
 	public class PictureRequest {
 
 		public Nest nestOne;
 		public Nest nestTwo;
 		public int nestOneState = 0;
 		public int nestTwoState = 0;
 		public PictureRequestState state;
 		public Feeder feeder;
 
 		public PictureRequest(Nest nestOne, Nest nestTwo, Feeder feeder){
 			this.state = PictureRequestState.NESTS_READY;
 			this.nestOne = nestOne;
 			this.nestTwo = nestTwo;
 			this.feeder = feeder;
 		}
 	} 
 
 /** ================================================================================ **/
 /** 									MESSAGES 									 **/
 /** ================================================================================ **/
 
 	public void msgNewNestConfig(ArrayList<Nest> nests){
 		this.nests = nests;
 	}
 
 	public void msgMyNestsReadyForPicture(Nest nestOne, Nest nestTwo, Feeder feeder) {
 		synchronized (pictureRequests) {
 			if (nestOne.getPart() != null && nestTwo.getPart() != null) {
 				debug("received msgMyNestsReadyForPicture("
 						+ nestOne.getPart().name + "," + nestTwo.getPart().name
 						+ ")");
 				pictureRequests.add(new PictureRequest(nestOne, nestTwo, feeder));
 			} else {
 
 				debug("received msgMyNestsReadyForPicture(" + nestOne.getPart()
 						+ "," + nestTwo.getPart() + ")");
 			}
 		}
 		this.stateChanged();
 	}
 
 	public void msgVisionClearForPictureInNests(Nest nestOne, Nest nestTwo) {
 		for( PictureRequest pr: pictureRequests) {
 			if(pr.nestOne == nestOne && pr.nestTwo == nestTwo){
 				pr.state = PictureRequestState.PARTS_ROBOT_CLEAR;
 			}
 		}
 		this.stateChanged();
 	}
 
 	public void msgAnalyzeKitAtInspection(Kit kit) {
 		debug("Received msgAnalyzeKitAtInspection() from the kit robot.");
 		kitPictureRequests.add(new KitPicRequest(kit.forceFail));
 		stateChanged();
 	}
 
 
 /** ================================================================================ **/
 /** 									SCHEDULER 									 **/
 /** ================================================================================ **/
 
 	public boolean pickAndExecuteAnAction() {
 
 		synchronized (pictureRequests) {
 			for (PictureRequest pr : pictureRequests) {
 				if (pr.state == PictureRequestState.PARTS_ROBOT_CLEAR) {
 					Nest one = this.nests.get(this.nests.indexOf(pr.nestOne));
 					Nest two = this.nests.get(this.nests.indexOf(pr.nestTwo));
 
 					if (one.getPart().name == pr.nestOne.getPart().name && two.getPart().name == pr.nestTwo.getPart().name) {
 						takePicture(pr);
 					}
 					return true;
 				}
 			}
 			for (PictureRequest pr : pictureRequests) {
 				if (pr.state == PictureRequestState.NESTS_READY) {
 					checkLineOfSight(pr);
 					return true;
 				}
 			}
 		}
 		for(KitPicRequest k: kitPictureRequests){
 			if(k.state == kitPictureRequeststate.NEED_TO_INSPECT){
 				inspectKit(k);
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 /** ================================================================================ **/
 /** 									ACTIONS 									 **/
 /** ================================================================================ **/
 
 	private void inspectKit(KitPicRequest k) {
 
 		try{
 			pictureAllowed.acquire();
 		}
 		catch(Exception ex){
 
 		}
 
 		DoAnimationTakePictureOfInstpectionSlot();
 		
 		if (k.forceFail == true)
 			stand.msgResultsOfKitAtInspection(KitState.FAILED_INSPECTION);
 		else
 			stand.msgResultsOfKitAtInspection(KitState.PASSED_INSPECTION);
 		/*
 		int randomNum = r.nextInt(11);
 		if(randomNum < 4)
 			stand.msgResultsOfKitAtInspection(KitState.PASSED_INSPECTION);
 		else
 			stand.msgResultsOfKitAtInspection(KitState.PASSED_INSPECTION);
 			*/
 
 		pictureAllowed.release();
 
 		k.state = kitPictureRequeststate.INSPECTED;
 	}
 
 	private void takePicture(PictureRequest pr){
 		try{
 			// Get permission to take picture
 			pictureAllowed.acquire();
 			// Take picture
 			DoAnimationTakePictureOfNest(pr.nestOne);
 			// Take PR that picture was taken
 			partsRobot.msgPictureTaken(pr.nestOne, pr.nestTwo);
 			
 			// Process 'picture'
 			
 			// If the nests are null, then just return
 			if(this.nests == null || !this.nests.contains(pr.nestOne) || !this.nests.contains(pr.nestTwo)){
 				pictureRequests.remove(pr);
 				return;
 			}
 			
 			pr.nestOneState = calculateNestState(pr.nestOne);
 			pr.nestTwoState = calculateNestState(pr.nestTwo);
 
 			debug("######################################");
 			debug("######### PICTURE TAKEN #############");
 			debug("State One: " + pr.nestOneState);
 			debug("State Two: " + pr.nestTwoState);
 			debug("######################################");
 			// Check that nests do not contain mixed parts
 			
 			// If all bad parts
 			if(pr.nestOne.getParts().size() > 0){
 				boolean anyGood = false;
 				for(Part p : pr.nestOne.getParts()){
 					if(p.isGoodPart){
 						anyGood = true;
 					}
 				}
 				if(!anyGood){
 					sendMessageToFeederAboutBadNest(pr.nestOne);
 				}
 			}
 			
 			if(pr.nestTwo.getParts().size() > 0){
 				boolean anyGood = false;
 				for(Part p : pr.nestTwo.getParts()){
 					if(p.isGoodPart){
 						anyGood = true;
 					}
 				}
 				if(!anyGood){
 					sendMessageToFeederAboutBadNest(pr.nestTwo);
 				}
 			}
 			
 			
 			// Both nests are unused, ignore
 			if(pr.nestOneState == 0 && pr.nestTwoState == 0 ){
 			}
 			// Unused+Unstable => ignore
 			else if(pr.nestOneState == 0 && pr.nestTwoState == 1 ){
 			}
 			// Unused+f => report Jammed nest 2
 			else if(pr.nestOneState == 0 && pr.nestTwoState == 2 ){
 				sendMessageToFeederAboutJam(pr.nestTwo);
 			}
 			// Unused+OK => grab Part nest 2
 			else if(pr.nestOneState == 0 && pr.nestTwoState == 3 ){
 				tellPartsRobotToGrabPartFromNest(pr.nestTwo);
 			}
 			
 			// Unstable+Unused => ignore
 			else if(pr.nestOneState == 1 && pr.nestTwoState == 0 ){
 			}
 			// Unstable+Unstable => ignore
 			else if(pr.nestOneState == 1 && pr.nestTwoState == 1 ){
 			}
 			// Unstable+Jammed => report Jammed nest 2
 			else if(pr.nestOneState == 1 && pr.nestTwoState == 2 ){
 				sendMessageToFeederAboutJam(pr.nestTwo);
 			}
 			// Unstable+OK => grab PART nest 2
 			else if(pr.nestOneState == 1 && pr.nestTwoState == 3 ){
 				tellPartsRobotToGrabPartFromNest(pr.nestTwo);
 			}
 			
 
 			
 			// Jammed+Unused => ignore
 			else if(pr.nestOneState == 2 && pr.nestTwoState == 0 ){
 				sendMessageToFeederAboutJam(pr.nestOne);
 			}
 			// Jammed+Unstable => ignore
 			else if(pr.nestOneState == 2 && pr.nestTwoState == 1 ){
 				sendMessageToFeederAboutJam(pr.nestOne);
 			}
 			// Jammed+Jammed => report Jammed nest 2
 			else if(pr.nestOneState == 2 && pr.nestTwoState == 2 ){
 				sendMessageToFeederAboutJam(pr.nestOne);
 				sendMessageToFeederAboutJam(pr.nestTwo);
 			}
 			// Jammed+OK => grab PART nest 2
 			else if(pr.nestOneState == 2 && pr.nestTwoState == 3 ){
 				sendMessageToFeederAboutJam(pr.nestOne);
 				tellPartsRobotToGrabPartFromNest(pr.nestTwo);
 			}
 			// Jammed+OK => grab PART nest 2
 			else if(pr.nestOneState == 2 && pr.nestTwoState == 9 ){
 				sendMessageToFeederAboutJam(pr.nestOne);
 			}
 			
 
 			
 			// OK+Unused => ignore
 			else if(pr.nestOneState == 3 && pr.nestTwoState == 0 ){
 				tellPartsRobotToGrabPartFromNest(pr.nestOne);
 			}
 			// OK+Unstable => ignore
 			else if(pr.nestOneState == 3 && pr.nestTwoState == 1 ){
 				tellPartsRobotToGrabPartFromNest(pr.nestOne);
 			}
 			// OK+Jammed => report Jammed nest 2
 			else if(pr.nestOneState == 3 && pr.nestTwoState == 2 ){
 				tellPartsRobotToGrabPartFromNest(pr.nestOne);
 				sendMessageToFeederAboutJam(pr.nestTwo);
 			}
 			// OK+OK => grab PART nest 2
 			else if(pr.nestOneState == 3 && pr.nestTwoState == 3 ){
 				tellPartsRobotToGrabPartFromNest(pr.nestOne);
 				tellPartsRobotToGrabPartFromNest(pr.nestTwo);
 			}
 			// OK+OK => grab PART nest 2
 			else if(pr.nestOneState == 3 && pr.nestTwoState == 9 ){
 				tellPartsRobotToGrabPartFromNest(pr.nestOne);
 			}
 			
 
 			// OK+OK => grab PART nest 2
 			else if(pr.nestOneState == 9 && pr.nestTwoState == 2 ){
 				tellPartsRobotToGrabPartFromNest(pr.nestTwo);
 			}
 			// OK+OK => grab PART nest 2
 			else if(pr.nestOneState == 9 && pr.nestTwoState == 3){
 				tellPartsRobotToGrabPartFromNest(pr.nestTwo);
 			}
 			// OK+OK => grab PART nest 2
 			else if(pr.nestOneState == 9 && pr.nestTwoState == 9){
 			}
 			
 			else {
 				debug(""+pr.nestOneState+" -- "+pr.nestTwoState);
 				System.exit(0);
 			}
 			
 
 			pictureRequests.remove(pr);;
 			pictureAllowed.release();
 			stateChanged();
 		}
 		catch(Exception ex){}
 	}
 
 	private void checkLineOfSight(PictureRequest pr){
 		debug("Executing checkLineOfSight()");
 		partsRobot.msgClearLineOfSight(pr.nestOne, pr.nestTwo);
 		pr.state = PictureRequestState.ASKED_PARTS_ROBOT;
 	}
 
 /** ================================================================================ **/
 /** 									ANIMATIONS 									 **/
 /** ================================================================================ **/
 	
 	private void DoAnimationTakePictureOfInstpectionSlot() {
 		debug("Executing DoAnimationTakePictureOfInstpectionSlot()");
 		server.command(this,"va kam cmd takepictureofinspection");
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 
 	}
 	
 	private void DoAnimationTakePictureOfNest(Nest nest) {
 
 		debug("Executing DoAnimationTakePicture()");
 		server.command(this,"va lm cmd takepictureofnest " + nest.getPosition()/2);
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 /** ================================================================================ **/
 /** 									HELPERS 									 **/
 /** ================================================================================ **/
 
 	public void tellPartsRobotToGrabPartFromNest(Nest n){
 
 		debug("##################################");
 		debug("Executing tellPartsRobotToGrabPartFromNest("+n.getPart().name+") => Parts: "+ n.getParts().size());
 		debug("######################################");
 		if(n.getParts().size() > 0){
 			partsRobot.msgGrabGoodPartFromNest(n, n.getPart());
 		}
 	}
 	
 	private int calculateNestState(Nest nest){
 		// Get index of nest to use the arrays to store the number of jams etc.
 		int nestIndex = this.nests.indexOf(nest);
 		
 		boolean pure = true;
 		for(Part p : nest.getParts()){
 			if(!nest.getPart().name.equals(p.name)){
 				pure = false;
 
 				debug("######### JAMMED #############");
				debug(nest.getPart().name + " != " + p.name);
 				debug("######################################");
 				break;
 			}
 		}
 		if(!pure && nest.getParts().size() > 0){
 			
 			sendMessageToFeederAboutMixedParts(nest);
 			return 9;
 		}
 		
 		// Check if the nest is not used
 		if(!nest.isBeingUsed()) {
 			nestJammedWaiting[nestIndex] = 0;
 			return 0; // Nest is unused
 		}
 		
 		// If the nest is unstable
 		else if(nest.getState() == NestState.UNSTABLE) {
 			nestJammedWaiting[nestIndex] = 0;
 			return 1; // Nest is unstable
 		}
 
 		// if nest is empty
 		
 		synchronized (nest.getParts()) {
 			if (nest.getParts().size() == 0) {
 
 				nestJammedWaiting[nestIndex]++;
 				if (nestJammedWaiting[nestIndex] > 1) {
 					nestJammedWaiting[nestIndex] =0;
 					return 2; //JAMMED
 				}
 				return 3; //OK
 
 			} else {
 				nestJammedWaiting[nestIndex] = 0;
 				return 3; // OK
 			}
 		}
 		
 	}
 	
 	public void sendMessageToFeederAboutJam(Nest n){
 		
 		int index = this.nests.indexOf(n);
 		
 		if(n.getParts().size() == 0 && n.getLane().getParts().size() == 0){
 
 			debug("######### NEED PART #############");
 			debug("NEST: "+n+" PART: "+n.getPart().name);
 			debug("######################################");
 			n.msgYouNeedPart(n.getPart());
 		}
 		switch(index){
 			case 0: feeder_zero.msgLaneMightBeJammed(0);
 			debug("######### JAMMED #############");
 			debug("FEEDER: 0 PART:");
 			debug("######################################");
 			 break;
 			case 1: feeder_zero.msgLaneMightBeJammed(1);
 			debug("######### JAMMED #############");
 			debug("FEEDER: 0 PART:");
 			debug("######################################");
 			 break;
 			case 2: feeder_one.msgLaneMightBeJammed(0);
 			debug("######### JAMMED #############");
 			debug("FEEDER: 1 PART:");
 			debug("######################################");
 			 break;
 			case 3: feeder_one.msgLaneMightBeJammed(1);
 			debug("######### JAMMED #############");
 			debug("FEEDER: 1 PART:");
 			debug("######################################");
 			 break;
 			case 4: feeder_two.msgLaneMightBeJammed(0);
 			debug("######### JAMMED #############");
 			debug("FEEDER: 2 PART:");
 			debug("######################################");
 			 break;
 			case 5: feeder_two.msgLaneMightBeJammed(1);
 			debug("######### JAMMED #############");
 			debug("FEEDER: 2 PART:");
 			debug("######################################");
 			 break;
 			case 6: feeder_three.msgLaneMightBeJammed(0);
 			debug("######### JAMMED #############");
 			debug("FEEDER: 3 PART:");
 			debug("######################################");
 			 break;
 			case 7: feeder_three.msgLaneMightBeJammed(1);
 			debug("######### JAMMED #############");
 			debug("FEEDER: 3 PART:");
 			debug("#####################################");
 			 break;
 		}
 		
 	}
 
 	public void sendMessageToFeederAboutMixedParts(Nest n){
 		int index = this.nests.indexOf(n);
 		
 		switch(index){
 			case 0: feeder_zero.msgNestHasMixedParts(0); break;
 			case 1: feeder_zero.msgNestHasMixedParts(1); break;
 			case 2: feeder_one.msgNestHasMixedParts(0); break;
 			case 3: feeder_one.msgNestHasMixedParts(1); break;
 			case 4: feeder_two.msgNestHasMixedParts(0); break;
 			case 5: feeder_two.msgNestHasMixedParts(1); break;
 			case 6: feeder_three.msgNestHasMixedParts(0); break;
 			case 7: feeder_three.msgNestHasMixedParts(1); break;
 		}
 	}
 	
 	public void sendMessageToFeederAboutBadNest(Nest n){
 		
 		int index = this.nests.indexOf(n);
 
 		debug("################################");
 		debug("        BAD NEST("+index+")      ");
 		debug("################################");
 		switch(index){
 			case 0: feeder_zero.msgBadNest(0); break;
 			case 1: feeder_zero.msgBadNest(1); break;
 			case 2: feeder_one.msgBadNest(0); break;
 			case 3: feeder_one.msgBadNest(1); break;
 			case 4: feeder_two.msgBadNest(0); break;
 			case 5: feeder_two.msgBadNest(1); break;
 			case 6: feeder_three.msgBadNest(0); break;
 			case 7: feeder_three.msgBadNest(1); break;
 		}
 		if(n.getParts().size() + n.getLane().getParts().size() < 6){
 			debug("################################");
 			debug("        MORE PARTS      ");
 			debug("################################");
 			n.msgYouNeedPart(n.getPart());
 		}
 	}
 
 	public void setFeeder(Feeder feeder, int feederNum){
 		if (feederNum == 0){
 			this.feeder_zero = feeder;
 		}
 		if (feederNum == 1){
 			this.feeder_one = feeder;
 		}
 		if (feederNum == 2){
 			this.feeder_two = feeder;
 		}
 		if (feederNum == 3){
 			this.feeder_three = feeder;
 		}
 	}
 
 	public void setPartsRobot(PartsRobot pr) {
 		this.partsRobot = pr;
 	}
 
 	protected void debug(String msg) {
 		if(true) {
 			print(msg, null);
 		}
 	}
 
 	
 
 }
