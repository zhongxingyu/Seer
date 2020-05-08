 package ai.cs4730;
 
 import rts.units.Unit;
/**
 * Represents a genreal unit
 * including different unit types and buildings
 */
public abstract class UnitController {
 
 	private Unit unit;
 	
 	public UnitController(Unit unit) {
 		this.unit = unit;
 	}
 
 }
