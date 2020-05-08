 package edu.brown.cs32.MFTG.monopoly;
 
 public class RailroadProperty extends Property {
 
 	public RailroadProperty(String name) {
 		super(name, "black", 200, 100, 25, 0, 0,0,0,0, Integer.MAX_VALUE);
 	}
 
 	@Override
 	/**
 	 * Cannot build a house on railroad
 	 */
	public void buildHouse() throws Exception{
 		throw new Exception ("Cannot add house to a railroad");
 	}
 	
 	@Override
 	/**
 	 * can never build on railroad
 	 */
	public boolean canBuildHouse(){
 		return false;
 	}
 	
 	@Override
 	/**
 	 * can never sell on railroad
 	 */
	public int sellHouse() throws Exception{
 		throw new Exception("Cannot sell house on a railroad");
 	}
 	
 	@Override
 	/**
 	 * can never sell house on railroad
 	 */
 	public boolean canSellHouse(){
 		return false;
 	}
 	@Override
 	/**
 	 * 
 	 * @return rent owed
 	 */
 	public int getRent() throws Exception{
 		if(this.getMortgagedState()){
 			return 0;
 		}
 		int numRail=this.getOwner().getNumRailroads();
 		switch(numRail){
 		case 1:  return 25;
 		case 2:  return 50;
 		case 3:  return 100;
 		case 4:  return 200;
 		default: throw new Exception (this.toString()+": Cannot have "+numRail+" railroads");
 		}
 	}
 	
 	@Override
 	/**
 	 *@return that it's not part of a monopoly
 	 */
 	public boolean getMonopolyState(){
 		return false;
 	}
 }
