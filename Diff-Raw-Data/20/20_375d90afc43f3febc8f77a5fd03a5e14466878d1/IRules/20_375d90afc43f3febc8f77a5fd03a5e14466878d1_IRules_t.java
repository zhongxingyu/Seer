 package risiko.net.script;
 
 import java.util.List;
 
 import risiko.net.data.PlayerColor;
 import risiko.net.data.TerritoryInfo;
 import risiko.net.data.sendable.AttackData;
 import risiko.net.data.sendable.PlayerInfo;
 import risiko.net.data.sendable.TerritoriesLayout;
 
 public interface IRules {
 
 	public PlayerColor getWinner(TerritoriesLayout layout);
 	
 	public boolean checkTerritoriesLayout(TerritoriesLayout oldlayout, TerritoriesLayout newLayout, PlayerInfo owner);
 
	public boolean isValidAttack(AttackData attack, TerritoriesLayout layout, PlayerColor player);
 
 	public boolean isValidDefence(AttackData attack, TerritoriesLayout layout);
 
 	public int attackedUnitsDestroyed(AttackData attack);
 	
 	public int attackerUnitsDestroyed(AttackData attack);
 	
 	public int getDiceValue();
 
 	public int getInitUnits(int playerNumber);
 
 	public int getReinforcementUnits(int turnCounter, TerritoriesLayout layout);
 	
 	public int getRequiredPlayerNumber();
 
 }
