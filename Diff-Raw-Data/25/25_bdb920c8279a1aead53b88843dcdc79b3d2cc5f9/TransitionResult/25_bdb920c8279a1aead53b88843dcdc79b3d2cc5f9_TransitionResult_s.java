 package org.aimas.craftingquest.state;
 
 import java.io.Serializable;
 
 /**
  * 
  * @author Razvan
  */
 @SuppressWarnings("serial")
 public class TransitionResult implements Serializable {
 
 	public enum TransitionError implements Serializable {
 		NoError, UnitIdError, UnknownActionError, UnknownError, OperandError, MoveError, 
 		TerrainError, ObstacleError, NoEnergyError, PickupError, BuildError, CraftingError, 
 		ResourceMissingError, NoCreditError, BuyRequestError, SellRequestError, GuardError,
 		EquipmentMissingError, TrapMisssingError, BlueprintError, NoUpgradeError,
		MissingBlueprintError, Frozen, PlayerIdError, ProximityError,
 	};
 
 	public int id; // egal cu cel trimis
 	public TransitionError errorType;
 	public String errorReason;
 	
 	public TransitionResult(int id, TransitionError errorType, String errorReason) {
 		this.id = id;
 		this.errorType = errorType;
 		this.errorReason = errorReason;
 	}
 	
 	public TransitionResult(int id) {
 		this(id, TransitionError.UnknownError, "Unknown");
 	}
 	
 	public boolean valid() {
 		if (errorType == TransitionError.NoError) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public String toString() {
 		String info = "";
 		info += "errorType: " + errorType.name() + "\n";
 		info += "errorReason: " + errorReason + "\n";
 		return info;
 	}
 }
