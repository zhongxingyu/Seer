 package agent.interfaces;
 
 import GraphicsInterfaces.NestGraphics;
 import agent.data.Part;
 import factory.PartType;
 
 public interface Nest {
 
 	public NestGraphics guiNest = null;
 
 	// MESSAGES
 	public abstract void msgHereIsPartType(PartType type);
 
 	public abstract void msgHereIsPart(Part p);
 	
 	public abstract void msgPartReady();
 
 	public abstract void msgTakingPart(Part p);
 
 	public abstract void msgDoneTakingParts();
 
 	public abstract void msgLanePurgeDone();
 
 	public abstract void msgReceivePartDone();
 
 	public abstract void msgGivePartToPartsRobotDone();
 
 	public abstract void msgPurgingDone();
 
 	public abstract void msgPurgeSelf();
 
     public abstract void msgTellLaneToIncreaseAmplitude();
 
     public abstract void msgTellFeederToFixThisLane();
 	public abstract boolean pickAndExecuteAnAction();
 
 
 }
