 package roborally.property;
 
 import be.kuleuven.cs.som.annotate.Basic;
 import be.kuleuven.cs.som.annotate.Immutable;
 import be.kuleuven.cs.som.annotate.Value;
 
 import java.util.ArrayList;
 
 import roborally.model.Board;
 import roborally.utils.IllegalPositionException;
 
 /**
  * Deze klasse houdt een positie bij. Deze bestaat uit een x- en een y-cordinaat.
  * 
  * @invar	Een positie is altijd geldig (de cordinaten liggen op of tussen hun minima en maxima).
  * 			|isValidPosition(new.getX(), new.getY())
  * 
  * @author 	Bavo Goosens (1e bachelor informatica, r0297884), Samuel Debruyn (1e bachelor informatica, r0305472)
  * 
  * @version 2.0
  */
 @Value
 public class Position implements Comparable<Position>{
 
 	/**
 	 * Initialiseer een positie.
 	 * 
 	 * @param 	xpos
 	 * 			X-waarde van de in te stellen positie
 	 * 
 	 * @param 	ypos
 	 * 			Y-waarde van de in te stellen positie
 	 * 
 	 * @throws	IllegalPositionException
 	 * 			De gegeven positie is ongeldig.
 	 * 			|!isValidPosition(xpos, ypos)
 	 * 
 	 * @post	De x-waarde van de nieuwe positie is gelijk aan de gegeven x-waarde.
 	 * 			|new.x == xpos
 	 * 
 	 * @post	De y-waarde van de nieuwe positie is gelijk aan de gegeven y-waarde.
 	 * 			|new.y == ypos
 	 */
 	public Position(long xpos, long ypos) throws IllegalPositionException{
 		if(!isValidPosition(xpos, ypos)) throw new IllegalPositionException(xpos, ypos);
 		x = xpos;
 		y = ypos;
 	}
 
 	/**
 	 * Deze methode geeft de x-cordinaat van de positie terug.
 	 * 
 	 * @return 	x-waarde van deze positie
 	 * 			|x
 	 */
 	@Basic @Immutable
 	public long getX() {
 		return x;
 	}
 
 	/**
 	 * Deze methode geeft de y-cordinaat van de positie terug.
 	 * 
 	 * @return 	y-waarde van deze positie
 	 * 			|y
 	 */
 	@Basic @Immutable
 	public long getY() {
 		return y;
 	}
 	
 	/**
 	 * X-cordinaat van de positie.
 	 */
 	private final long x;
 	/**
 	 * Y-cordinaat van de positie.
 	 */
 	private final long y;
 
 	/**
 	 * Kijkt na of de positie een geldige positie is.
 	 * 
 	 * @param	xpos
 	 * 			X-waarde van de positie die nagekeken moet worden.
 	 * 
 	 * @param	ypos
 	 * 			Y-waarde van de positie die nagekeken moet worden.
 	 * 
 	 * @return	Boolean die true is als de positie geldig is.
 	 * 			|if(xpos > UPPER_BOUND_X || xpos < LOWER_BOUND_X || ypos > UPPER_BOUND_Y || ypos < LOWER_BOUND_Y)
 	 * 			|	false
 	 * 			|true
 	 */
 	@Basic
 	public static boolean isValidPosition(long xpos, long ypos){
 		if (xpos > UPPER_BOUND_X || xpos < LOWER_BOUND_X || ypos > UPPER_BOUND_Y || ypos < LOWER_BOUND_Y)
 			return false;
 		return true;
 	}
 	
 	/**
 	 * Maximale x-waarde.
 	 */
 	private final static long UPPER_BOUND_X = Long.MAX_VALUE;
 	/**
 	 * Minimale x-waarde.
 	 */
 	private final static long LOWER_BOUND_X = 0;
 	/**
 	 * Maximale y-waarde
 	 */
 	private final static long UPPER_BOUND_Y = Long.MAX_VALUE;
 	/**
 	 * Minimale y-waarde.
 	 */
 	private final static long LOWER_BOUND_Y = 0;
 
 	/**
 	 * Deze methode dient om de posities te vinden naast de huidige positie.
 	 * 
 	 * @param	board
 	 * 			Het bord is nodig om na te kijken of de posities ernaast geldig zijn voor dit bord.
 	 * 
 	 * @return	Dit is een lijst van posities rond de huidige positie.
 	 */
 	public ArrayList<Position> getNeighbours(Board board){
 		ArrayList<Position> positions = new ArrayList<Position>();
 		ArrayList<Position> validPositions = new ArrayList<Position>();
 		try{
 			positions.add(new Position(getX() - 1, getY()));
 		}catch(IllegalPositionException e){
 			// NOP
 		}
 		try{
 			positions.add(new Position(getX() + 1, getY()));
 		}catch(IllegalPositionException e){
 			// NOP
 		}
 		try{
 			positions.add(new Position(getX(), getY() - 1));
 		}catch(IllegalPositionException e){
 			// NOP
 		}
 		try{
 			positions.add(new Position(getX(), getY() + 1));
 		}catch(IllegalPositionException e){
 			// NOP
 		}
 		for(Position pos: positions){
 			if(board.isValidBoardPosition(pos)){
 				validPositions.add(pos);
 			}
 		}
 		return validPositions;
 	}
 
 	/* 
 	 * Deze methode maakt een String op basis van een Position.
 	 * 
 	 * @return	Een textuele representatie van dit object waarbij duidelijk wordt welke positie dit object bevat.
	 * 			|getX() + "," + getY()
 	 */
 	@Override
 	public String toString() {
		return getX() + "," + getY();
 	}
 	
 	/**
 	 * Deze methode vergelijkt 2 posities.
 	 * 
 	 * @param	other
 	 * 			De positie waarmee vergeleken moet worden.
 	 * 
 	 * @throws	IllegalArgumentException
 	 * 			De 2de positie is null.
 	 * 			|other == null
 	 * 
 	 * @return	|if(getX() == other.getX())
 	 *			|	(int) (getY() - other.getY())
 	 *			|(int) (getX() - other.getX())
 	 */
 	@Override
 	public int compareTo(Position other) throws	IllegalArgumentException{
 		if(other == null)
 			throw new IllegalArgumentException("Andere positie mag niet null zijn.");
 		if(getX() == other.getX())
 			return (int) (getY() - other.getY());
 		return (int) (getX() - other.getX()); 
 	}	
 
 	/*
 	 * Deze methode kijk na of 2 posities aan elkaar gelijk zijn.
 	 * 
 	 * @param	other
 	 * 			De andere positie waarmee vergeleken moet worden.
 	 * 
 	 * @return	Dit is true wanneer de 2 objecten dezelfde positie bevatten.
 	 * 			|if(other == null)
 	 * 			|	false
 	 * 			|if(getClass() != other.getClass())
 	 * 			|	false
 	 * 			|(this.getX() == ((Position) other).getX() && this.getY() == ((Position) other).getY())
 	 */
 	@Override
 	public boolean equals(Object other){
 		if(other == null)
 			return false;
 		if(getClass() != other.getClass())
 			return false;
 		return (this.getX() == ((Position) other).getX() && this.getY() == ((Position) other).getY());
 	}
 
 	/*
 	 * Deze methode berekent de hashcode van een object van deze klasse.
 	 * 
 	 * @return	De hashcode van dit object.
 	 * 			|Integer.parseInt(Long.toString(getX()) + Long.toString(getY()))
 	 */
 	@Override
 	public int hashCode() {
 		return Integer.parseInt(Long.toString(getX()) + Long.toString(getY()));
 	}
 }
