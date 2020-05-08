 package roborally.model;
 
 import roborally.model.Board;
 import roborally.property.Position;
 import roborally.utils.EntityNotOnBoardException;
 import roborally.utils.IllegalPositionException;
 import be.kuleuven.cs.som.annotate.Basic;
 import be.kuleuven.cs.som.annotate.Raw;
 
 /**
  * Deze klasse houdt een object bij dat op een board kan staan en een positie kan hebben.
  * 
  * @invar	Indien bord of positie null is, moet de andere van de 2 ook null zijn. Een positie op een bord moet geldig zijn.
  * 			|isValidEntity()
  * 
  * @author 	Bavo Goosens (1e bachelor informatica, r0297884), Samuel Debruyn (1e bachelor informatica, r0305472)
  * 
  * @version 1.0
  */
 public abstract class Entity {
 
 	/**
 	 * Methode die het board instelt waartoe dit object behoort.
 	 * 
 	 * @param 	board
 	 * 			Het board waarop dit object zich bevindt.
 	 * 
 	 * @throws 	IllegalStateException
 	 * 			Het object is getermineerd.
 	 * 			|isTerminated()
 	 * 
 	 * @post	Het opgegeven bord is nu ingesteld voor dit object.
 	 * 			|new.board == board
 	 */
 	@Raw
 	protected void setBoard(Board board) throws IllegalStateException{
 		if(isTerminated()){
 			throw new IllegalStateException("Het object is getermineerd.");
 		}
 		this.board = board;
 	}
 
 	/**
 	 * Methode die het board teruggeeft waarop dit object zich bevindt. Deze methode kan ook null teruggeven wat wil zeggen dat het object zich niet op een board bevindt.
 	 * 
 	 * @return	Het board waarop dit object zich bevindt of null als het object niet op een board staat.
 	 * 			|board
 	 */
 	@Basic
 	public Board getBoard(){
 		return board;
 	}
 
 	/**
 	 * Het bord waarop dit object staat (niet noodzakelijk).
 	 */
 	private Board board;
 
 	/**
 	 * Kijkt na of het object op het bord staat.
 	 * 
 	 * @return	Boolean die true is als het object op een bord staat.
 	 * 			|(getBoard() != null && isValidEntity())
 	 */
 	public boolean isOnBoard(){
 		return (getBoard() != null && isValidEntity());
 	}
 
 	/**
 	 * Geeft de positie van dit object terug.
 	 * 
 	 * @return	De positie van het object.
 	 * 			|position
 	 */
 	@Basic
 	public Position getPosition() {
 		return position;
 	}
 
 	/**
 	 * Wijzigt de positie van dit object naar de nieuwe positie.
 	 * 
 	 * @param	position
 	 * 			De nieuwe positie. Null indien deze buiten het bord is.
 	 * 
 	 * @throws	IllegalPositionException
 	 * 			|isOnBoard() && position != null
 	 * 
 	 * @throws	IllegalStateException 
 	 * 			Het object is getermineerd.
 	 * 			|isTerminated()
 	 * 
 	 * @throws	EntityNotOnBoardException
 	 * 			Deze positie is niet geldig voor het huidige bord.
 	 * 			|(getBoard().isValidBoardPosition(position) && position != null)
 	 * 
 	 * @post	De positie van dit object is nu gelijk aan de gegeven positie.
 	 * 			|new.getPosition() == position
 	 */
 	@Raw
 	public void setPosition(Position position) throws IllegalPositionException, IllegalStateException, EntityNotOnBoardException{
 		if(isTerminated()){
 			throw new IllegalStateException("Het object is getermineerd.");
 		}else if(position != null){
 			if(getBoard() == null){
 				throw new EntityNotOnBoardException();
 			}else if(!getBoard().isValidBoardPosition(position)){
 				throw new IllegalPositionException(position);
 			}
 		}
 		this.position = position;
 	}
 
 	/**
 	 * Positie van dit object (niet noodzakelijk).
 	 */
 	private Position position;
 
 	/**
 	 * Deze methode vernietigt het object.
 	 * 
 	 * @post	Het object heeft geen positie meer.
 	 * 			|new.getPosition() == null
 	 * 
 	 * @post	Het object is vernietigd.
 	 * 			|new.isTerminated()
 	 * 
 	 * @post	Het object staat niet meer op een bord.
 	 * 			|new.getBoard() == null
 	 */
 	public void destroy(){
 		if(isOnBoard())
 			removeFromBoard();
 		isTerminated = true;
 	}
 
 	/**
 	 * Deze methode geeft true indien het object vernietigd is, anders false.
 	 * 
 	 * @return	Een boolean die true is indien het object vernietigd is, anders false.
 	 * 			|isTerminated
 	 */
 	@Basic
 	public boolean isTerminated(){
 		return isTerminated;
 	}
 
 	/**
 	 * Indien het object vernietigd is wordt dit true.
 	 */
 	private boolean isTerminated;
 
 	/**
 	 * Plaats het object op een bord met een geldige positie.
 	 * 
 	 * @param	board
 	 * 			Het bord waarop het object geplaatst moet worden.
 	 * 
 	 * @param	position
 	 * 			De plaats waar het object moet komen.
 	 * 
 	 * @post	Het object staat nu op het gegeven bord.
 	 * 			|new.getBoard() == board
 	 * 
 	 * @post	Het object staat nu op de gegeven positie.
	 * 			|new.getPosition() == position
 	 */
 	@Raw
 	public void putOnBoard(Board board, Position position){
 		board.putEntity(position, this);
 		this.setBoard(board);
 		this.setPosition(position);
 	}
 
 	/**
 	 * Verwijdert het object van een bord en haalt de opgeslagen positie weg.
 	 * 
 	 * @post	Het object bevindt zich niet langer op een bord.
 	 * 			|new.isOnBoard() == false
 	 */
 	@Raw
 	public void removeFromBoard(){
 		this.getBoard().removeEntity(this);
 		this.setBoard(null);
 		this.setPosition(null);
 	}
 
 	/**
 	 * Kijk na of het object geldig is.
 	 * 
 	 * @return 	Indien bord of positie null is, moet de andere van de 2 ook null zijn. Indien het object op een bord staat moet de positie geldig zijn voor dat bord.
 	 * 			|if(!(getBoard() == null ^ getPosition() == null))
 	 * 			|	false
 	 * 			|if(isOnBoard() && getBoard().isValidBoardPosition(getPosition()))
 	 * 			|	false
 	 * 			|true
 	 */
 	public boolean isValidEntity(){
 		if(!(getBoard() == null ^ getPosition() == null))
 			return false;
 		if(isOnBoard() && getBoard().isValidBoardPosition(getPosition()))
 			return false;
 		return true;
 	}
 
 	/**
 	 * Wanneer het object geraakt wordt door een robot die schiet of door een surprise box wordt deze methode opgeroepen.
 	 */
 	protected void damage() {
 		//NOP
 	}
 	
 	/*
 	 * Deze methode zet het object om naar een String.
 	 * 
 	 * @return	Een textuele representatie van dit object waarbij duidelijk wordt wat de eigenschappen van dit object zijn.
 	 * 			|if(isOnBoard())
 	 * 			|	"positie: " + getPosition().toString()
 	 * 			|"positie: staat niet op een bord"
 	 */
 	@Override
 	public String toString() {
 		if(!isOnBoard())
 			return "positie: " + getPosition().toString();
 		return "positie: staat niet op een bord";
 	}
 }
