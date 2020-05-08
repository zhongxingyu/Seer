 package dk.stacktrace.risk.game_logic.battle;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import dk.stacktrace.risk.game_logic.Player;
 import dk.stacktrace.risk.game_logic.Territory;
 
 public class Battle
 {
 	private int attackingArmy;
 	private Territory attackingTerritory, defendingTerritory;
 	private ArrayList<Dice> winnerDices, attackDices, defendDices;
 	private Player winner;
 
 	public Battle(Territory attackingTerritory, Territory defendingTerritory, int attackingArmy)
 	{
 		this.attackingTerritory = attackingTerritory;
 		this.defendingTerritory = defendingTerritory;
 		this.attackingArmy = attackingArmy;
 		winner = null;
		
		attackingTerritory.moveTroops(attackingArmy);
 	}
 
 	public void fight()
 	{
 		attackDices = new ArrayList<Dice>();
 		defendDices = new ArrayList<Dice>();
 
 		for (int dice = 0; dice < getNumOfAttackDices(); dice++)
 		{
 			attackDices.add(new Dice(true));
 		}
 
 		for (int dice = 0; dice < getNumOfDefendDices(); dice++)
 		{
 			defendDices.add(new Dice(false));
 		}
 
 		for (Dice dice : attackDices)
 		{
 			dice.roll();
 		}
 		Collections.sort(attackDices,Collections.reverseOrder());
 
 		for (Dice dice : defendDices)
 		{
 			dice.roll();
 		}
 		Collections.sort(defendDices,Collections.reverseOrder());
 
 		winnerDices = new ArrayList<Dice>();
 		for (int i = 0; i < getNumOfDicesToComapre(); i++)
 		{
 			if (defendDices.get(i).compareTo(attackDices.get(i)) > 0)
 			{
 				winnerDices.add(defendDices.get(i));
 				--attackingArmy;
 			}
 			else
 			{
 				winnerDices.add(attackDices.get(i));
 				defendingTerritory.kill();
 			}
 		}
 	}
 
 	public boolean battleIsOver()
 	{
 		return (attackingArmy == 0 || defendingTerritory.getArmySize() == 0);
 	}
 	
 	public Player getLoser()
 	{
 		if (battleIsOver())
 		{
 			return (attackingArmy == 0) ? attackingTerritory.getOwner() : defendingTerritory.getOwner();
 		}
 		else 
 		{
 			return null;
 		}
 	}
 	
 	public Player getWinner()
 	{
 		if (battleIsOver() && winner == null)
 		{
 			winner = (attackingArmy > 0) ? attackingTerritory.getOwner() : defendingTerritory.getOwner();
 		}
 		return winner;
 	}
 	
 	
 	
 	public boolean attackerWon()
 	{
 		return (attackingTerritory.getOwner().equals(getWinner()));
 	}
 	
 	public ArrayList<Dice> getInitialAttackDices()
 	{
 		ArrayList<Dice> attackDices = new ArrayList<Dice>();
 		for (int i = 0; i < getNumOfAttackDices(); i++)
 		{
 			attackDices.add(new Dice(true));
 			attackDices.get(i).roll();
 		}
 		return attackDices;
 	}
 	
 	public ArrayList<Dice> getInitialDefendDices()
 	{
 		ArrayList<Dice> defendDices = new ArrayList<Dice>();
 		for (int i = 0; i < getNumOfDefendDices(); i++)
 		{
 			defendDices.add(new Dice(false));
 			defendDices.get(i).roll();
 		}
 		return defendDices;
 	}
 
 	public void retreat()
 	{
 		attackingTerritory.reinforce(attackingArmy);
 	}
 
 	public int getNumOfAttackDices()
 	{
 		if (attackingArmy == 1)
 		{
 			return 1;
 		}
 		else if (attackingArmy == 2)
 		{
 			return 2;
 		}
 		else if (attackingArmy >= 3)
 		{
 			return 3;
 		}
 		else
 		{
 			return 0;
 		}
 	}
 
 	public int getNumOfDefendDices()
 	{
 		if (defendingTerritory.getArmySize() == 1)
 		{
 			return 1;
 		}
 		else if (defendingTerritory.getArmySize() >= 2)
 		{
 			return 2;
 		}
 		else
 		{
 			return 0;
 		}
 	}
 
 	public int getAttackingArmy()
 	{
 		return attackingArmy;
 	}
 
 	public void setAttackingArmy(int attackingArmy)
 	{
 		this.attackingArmy = attackingArmy;
 	}
 	
 	public Territory getAttackingTerritory()
 	{
 		return attackingTerritory;
 	}
 
 	public Territory getDefendingTerritory()
 	{
 		return defendingTerritory;
 	}
 	
 	public ArrayList<Dice> getAttackDices()
 	{
 		return attackDices;
 	}
 	
 	public ArrayList<Dice> getDefendDices()
 	{
 		return defendDices;
 	}
 	public ArrayList<Dice> getWinnerDices()
 	{
 		return winnerDices;
 	}
 
 	private int getNumOfDicesToComapre()
 	{
 		if (getNumOfAttackDices() < getNumOfDefendDices())
 		{
 			return getNumOfAttackDices();
 		}
 		else if (getNumOfAttackDices() == getNumOfDefendDices()) 
 		{
 			return getNumOfAttackDices();
 		}
 		else if (getNumOfAttackDices() > getNumOfDefendDices()) 
 		{
 			return getNumOfDefendDices();
 		}
 		return 0;
 	}
 }
