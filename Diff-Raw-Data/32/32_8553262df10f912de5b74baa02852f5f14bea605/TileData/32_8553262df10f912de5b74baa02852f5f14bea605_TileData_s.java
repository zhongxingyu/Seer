 package com.lunasoft.dynasty.tools.mapeditor;
 
 import java.io.Serializable;
 
 public class TileData implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	public enum ReliefType {
 		WATER,
 		PLAINS,
 		HILLS,
 		MOUNTAINS
 	}
 
 	public enum VegetationType {
 		DESERT,
 		FLOOD_PLAIN,
 		FOREST,
 		GRASSLAND,
 		MARSH,
 		NONE
 	}
 
 	private final ReliefType reliefType;
 	private final VegetationType vegetationType;
 	private final StrongholdData stronghold;
 	private final boolean developed;
 
 	public TileData(ReliefType reliefType, VegetationType vegetationType,
 			StrongholdData stronghold, boolean developed) {
 		this.reliefType = reliefType;
 		this.vegetationType = vegetationType;
 		this.stronghold = stronghold;
 		this.developed = developed;
 	}
 
	public TileData(ReliefType reliefType) {
		this(reliefType, null, null, false);
 	}
 
 	public ReliefType getReliefType() {
 		return reliefType;
 	}
 
 	public VegetationType getVegetationType() {
 		return vegetationType;
 	}
 
 	public StrongholdData getStronghold() {
 		return stronghold;
 	}
 
 	public boolean isDeveloped() {
 		return developed;
 	}
 }
