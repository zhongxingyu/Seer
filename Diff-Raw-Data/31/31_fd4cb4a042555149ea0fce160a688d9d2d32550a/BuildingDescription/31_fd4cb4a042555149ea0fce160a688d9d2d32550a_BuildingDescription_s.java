 package net.lotrcraft.strategycraft.loader;
 
 import net.lotrcraft.strategycraft.buildings.Building;
 import net.lotrcraft.strategycraft.units.Unit;
 
 public class BuildingDescription {
 
 	private Class<? extends Building> building = null;
 	private Class<? extends Unit> unit = null;
 	private String name;
 	
 	public BuildingDescription(String name, Class<? extends Building> buildingClass, Class<? extends Unit> unitClass) {
 		this.building = buildingClass;
 		this.unit = unitClass;
 	}
 	
 	public Class<? extends Building> getBuilding(){
 		return building;
 	}
 
 	public Class<? extends Unit> getUnit(){
 		return unit;
 	}
 	
 	public String getName(){
 		return name;
 	}
 }
