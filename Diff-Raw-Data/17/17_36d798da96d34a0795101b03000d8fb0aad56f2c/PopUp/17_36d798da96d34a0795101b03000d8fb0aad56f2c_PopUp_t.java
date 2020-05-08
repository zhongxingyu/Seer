 package engine.agent.tim.interfaces;
 
 import java.util.List;
 import engine.agent.tim.misc.MyGlassPopUp;
 import shared.Glass;
 import shared.interfaces.NonnormBreakInteraction;
 import shared.interfaces.OfflineConveyorFamily;
 import shared.interfaces.PopupWorkstationInteraction;
 
 public interface PopUp extends PopupWorkstationInteraction {
 	public abstract void msgGiveGlassToPopUp(Glass glass);
 	public abstract int getFreeChannels();
 	public abstract boolean doesGlassNeedProcessing(Glass glass); // This method will be used by the conveyor to see if a piece of glass needs to processing from a machine
 	public abstract void msgPositionFree();
 	
 	// Getters/Setters
 	public abstract boolean isPopUpDown();
 	public abstract List<MyGlassPopUp> getGlassToBeProcessed();
 	public abstract void setCF(OfflineConveyorFamily conveyorFamilyImp);
 	public abstract boolean isGlassOnPopUp();
 
 	// These methods will specifically be used for testing purposes -- do not have to be always be implemented
 	public abstract void runScheduler();
	public abstract int getGuiIndex();
 }
