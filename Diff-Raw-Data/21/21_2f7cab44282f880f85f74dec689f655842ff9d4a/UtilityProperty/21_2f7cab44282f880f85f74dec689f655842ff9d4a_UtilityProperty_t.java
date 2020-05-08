 package edu.brown.cs32.MFTG.monopoly;
 
 public class UtilityProperty extends Property {
 	private Game _game;
 	public UtilityProperty(String name, Game game) {
 		super(name, "white", 150, 75, 0, 0, 0,0,0,0, Integer.MAX_VALUE);
 		_game=game;
 	}
 
 	@Override
 	/**
 	 * Cannot build a house on utility
 	 */
	public void buildHouse(Game game) throws Exception{
 		throw new Exception ("Cannot add house to a utility");
 	}
 	
 	@Override
 	/**
 	 * can never build on utility
 	 */
	public boolean canBuildHouse(Game game){
 		return false;
 	}
 	
 	@Override
 	/**
 	 * can never sell on utility
 	 */
	public int sellHouse(Game game) throws Exception{
 		throw new Exception("Cannot sell house on a utility");
 	}
 	
 	@Override
 	/**
 	 * can never sell house on utility
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
 		int numUtil=this.getOwner().getNumUtilities();
 		Dice dice = _game.getDice();
 		switch(numUtil){
 		case 1:  return dice.rollDice()*4;
 		case 2:  return dice.rollDice()*10;
 		default: throw new Exception ("Cannot have "+numUtil+" utilities");
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
