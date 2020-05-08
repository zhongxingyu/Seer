 package roborally.model;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import be.kuleuven.cs.som.annotate.Basic;
 import be.kuleuven.cs.som.annotate.Immutable;
 
 import roborally.property.Position;
 import roborally.utils.Calculator;
 
 /**
  * Een klasse om een bord voor te stellen.
  * 
  * @author 	Bavo Goosens (1e bachelor informatica, r0297884), Samuel Debruyn (1e bachelor informatica, r0305472)
  * 
  * @invar 	Een bord moet ten alle tijden een geldige hoogte en breedte hebben.
  * 			|isValidHeight(getHeight())
  *  		|isValidWidth(getWidth())
  *  
  * @version 1.1
  */
 public class Board{
 
 	/**
 	 * De breedte van het bord.
 	 */
 	private final long width;
 	/**
 	 * De hoogte van het bord.
 	 */
 	private final long height;
 	/**
 	 * Indien het object vernietigd is wordt dit true.
 	 */
 	private boolean isTerminated = false;
 	/**
 	 * Deze HashMap houdt een lijst van HashSets bij per Position (als String) met in elke HashSet alle objecten op die plaats.
 	 */
 	private final HashMap <String, HashSet<Entity>> map;
 	/**
 	 * De maximale breedte die een bord kan hebben.
 	 */
 	private static final long UPPER_BOUND_WIDTH = Long.MAX_VALUE;
 	/**
 	 * De minimale breedte die een bord kan hebben.
 	 */
 	private static final long LOWER_BOUND_WIDTH = 0;
 	/**
 	 * De maximale hoogte die een bord kan hebben.
 	 */
 	private static final long UPPER_BOUND_HEIGHT = Long.MAX_VALUE;
 	/**
 	 * De minimale hoogte die een bord kan hebben.
 	 */
 	private static final long LOWER_BOUND_HEIGHT = 0;
 
 	/**
 	 * Deze constructor maakt een nieuw bord aan.
 	 * @pre 	...
 	 * 			| isValidWidth(width)
 	 * 
 	 * @pre 	...
 	 * 			| isValidHeight(height) 
 	 * 
 	 * @param	height
 	 * 			De hoogte van het nieuwe bord.
 	 * 
 	 * @param 	width
 	 * 			De breedte van het nieuwe bord.
 	 * 
 	 * @post	Het bord heeft een map gekregen.
 	 * 			|new.getMap() != null
 	 * 
 	 * @post	Het bord heeft een hoogte gekregen.
 	 * 			|new.getHeight() == height
 	 * 
 	 * @post	Het bord heeft een breedte gekregen.
 	 * 			|new.getWidth() == width
 	 */
 	public Board (long width , long height){
 		this.height = height;
 		this.width = width;
 		this.map = new HashMap<>();
 	}
 
 	/**
 	 * Deze methode geeft alle objecten terug op een positie.
 	 * 
 	 * @param	pos
 	 * 			De positie waar gekeken moet worden.
 	 * 
 	 * @return	De HashSet met alle objecten op deze positie.
 	 * 			|this.getMap().get(pos.toString())
 	 */
 	public HashSet<Entity> getEntityOnPosition(Position pos){
 		return this.getMap().get(pos.toString());
 	}
 
 	/**
 	 * Deze methode verwijdert een object uit het bord.
 	 * 
 	 * @param 	ent
 	 * 			De entity die verwijderd moet worden.
 	 * 
 	 * @throws	IllegalStateException
 	 * 			Het object bevindt zich niet op een juiste positie in dit bord.
 	 * 			|ent.getBoard().isValidBoardPosition(ent.getPosition())
 	 * 
 	 * @post	De gegeven entity bevindt zich niet langer op een bord.
 	 * 			|new.getEntityOnPosition(ent.getPosition()) == null || new.getMap().containsKey(pos.toString()) == false
 	 */
 	public void removeEntity(Entity ent) throws IllegalStateException{
 		Position pos = ent.getPosition();
 		if(!ent.getBoard().isValidBoardPosition(pos)){
 			throw new IllegalStateException("Dit object bevindt zich niet op een geldige positie.");
 		}
 		this.getMap().get(pos.toString()).remove(ent);
 		this.cleanBoardPosition(pos);
 	}
 
 	/**
 	 * Deze methode kijkt na of het bord leeg is op deze positie, indien dat zo is wordt een mogelijke opgeslagen lege HashSet verwijderd.
 	 * 
 	 * @param	pos
 	 * 			De positie in het bord die moet nagekeken worden.
 	 * 
 	 * @post	De positie is leeg indien er geen objecten meer stonden.
 	 * 			|if(this.getMap().containsKey(pos.toString()) && !this.getMap().get(pos.toString()).isEmpty() && this.getMap().get(pos.toString()) != null)
 	 * 			|	new.getMap().containsKey(pos.toString()) == false
 	 */
 	public void cleanBoardPosition(Position pos){
 		if(this.getMap().containsKey(pos.toString())){
 			if(this.getMap().get(pos.toString()) == null){
 				this.getMap().remove(pos.toString());
 			}else if(this.getMap().get(pos.toString()).isEmpty()){
 				this.getMap().remove(pos.toString());
 			}
 		}
 	}
 
 	/**
 	 * Deze methode kijkt na of een object op een plaats geplaatst kan worden.
 	 * 
 	 * @param	pos
 	 * 			De positie die moet nagekeken worden.
 	 * 
 	 * @return	Boolean die true is als de positie niet door een muur ingenomen is.
 	 * 			|if(!this.isValidBoardPosition(pos))
 	 * 			|	false
 	 * 			|if(!this.getMap().containsKey(pos.toString()))
 	 * 			|	true
 	 * 			|if(this.getMap().get(pos.toString()).isEmpty())
 	 * 			|	true
 	 * 			|if (this.getEntityOnPosition(pos).size() == 1){
 	 * 			|	if(this.getEntityOnPosition(pos).iterator().next() instanceof Wall)
 	 * 			|		false
 	 * 			|true
 	 * 			
 	 */
 	public boolean isPlacableOnPosition(Position pos, Entity entity){
 		if (!isValidBoardPosition(pos))
 			return false;
 		if (entity instanceof Wall){
 			try{
 				HashSet<Entity> place = this.getEntityOnPosition(pos);
 				if (place.isEmpty())
 					return true;
 				return false;
 			}catch (NullPointerException esc){
 				return true;
 			}
 		}
 		if (entity instanceof Robot){
 			try {
 				HashSet<Entity> place = this.getEntityOnPosition(pos);
 				if(place.size() == 1){
 					Entity ent = place.iterator().next();
 					if (ent instanceof Robot || ent instanceof Wall)
 						return false;
 					return true;
 				}
 				return true;
 			} catch (NullPointerException esc){
 				return true;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Deze methode plaatst een object op het bord.
 	 * 
 	 * @param	key
 	 * 			De positie waar het object geplaatst moet worden.
 	 * 			
 	 * @param 	entity
 	 * 			Het object dat op het bord geplaatst moet worden.
 	 * 
 	 * @throws 	IllegalArgumentException
 	 * 			De plaats in het bord is niet beschikbaar.
 	 * 			|!this.isPlacableOnPosition(key)
 	 * 
 	 * @post	Als de positie op het bord beschikbaar was staat het object op deze positie.
 	 * 			|if(this.isPlacableOnPosition(key))
 	 * 			|	new.get(key.toString()) == entity
 	 * 			
 	 */
 	public void putEntity(Position key, Entity entity) throws IllegalArgumentException{
 		if (this.isPlacableOnPosition(key, entity)){
 			if (entity instanceof Battery || entity instanceof Robot){
 				HashSet<Entity> set = this.getMap().get(key.toString());
 				if (set == null){
 					HashSet<Entity> input = new HashSet<>();
 					input.add(entity);
 					this.getMap().put(key.toString(), input);
 				}else{
 					set.add(entity);
				}
 			}else if(entity instanceof Wall){
 				HashSet<Entity> input = new HashSet<>();
 				input.add(entity);
 				this.getMap().put(key.toString(), input);
 			}
 		}else{
 			throw new IllegalArgumentException("Op deze plaats in het bord kan geen object meer geplaatst worden.");
 		}
 	}
 
 	/**
 	 * Deze methode geeft alle robots terug die op dit bord staan.
 	 * 
 	 * @return	Set<Robot>
 	 * 			Voor iedere robot die een element is van this.getMap() geldt dat deze op het einde van deze methode in de set zal zitten die teruggegeven wordt. Deze set is null als er geen robots op het bord staan.
 	 * 			
 	 */
 	public Set<Robot> getRobots() {
 		Collection<HashSet<Entity>> c = this.getMap().values();
 		HashSet<Robot> rob = new HashSet<>();
 		for (Set<Entity> values : c ){
 			Iterator<Entity> i = values.iterator();
 			while (i.hasNext()){
 				Entity obj = i.next();
 				if (obj instanceof Robot){
 					rob.add((Robot) obj);
 				}
 			}
 		}
 		return rob;
 	}
 
 	/**
 	 * Deze methode geeft alle batterijen terug die op dit bord staan.
 	 * 
 	 * @return	Set<Battery>
 	 * 			Voor iedere batterij die een element is van this.getMap() geldt dat deze op het einde van deze methode in de set zal zitten die teruggegeven wordt. Deze set is null als er geen batterijen op het bord staan.
 	 * 			
 	 */
 	public Set<Battery> getBatteries() {
 		Collection<HashSet<Entity>> c = this.getMap().values();
 		HashSet<Battery> bat = new HashSet<>();
 		for (Set<Entity> values : c ){
 			Iterator<Entity> i = values.iterator();
 			while (i.hasNext()){
 				Entity obj = i.next();
 				if (obj instanceof Battery){
 					bat.add((Battery) obj);
 				}
 			}
 		}
 		return bat;
 	}
 
 	/**
 	 * Deze methode geeft alle muren terug die op dit bord staan.
 	 * 
 	 * @return	Set<Wall>
 	 * 			Voor iedere muur die een element is van this.getMap() geldt dat deze op het einde van deze methode in de set zal zitten die teruggegeven wordt. Deze set is null als er geen muren op het bord staan.
 	 * 			
 	 */
 	public Set<Wall> getWalls() {
 		Collection<HashSet<Entity>> c = this.getMap().values();
 		HashSet<Wall> wall = new HashSet<>();
 		for (Set<Entity> values : c ){
 			Iterator<Entity> i = values.iterator();
 			while (i.hasNext()){
 				Entity obj = i.next();
 				if (obj instanceof Wall){
 					wall.add((Wall) obj);
 				}
 			}
 		}
 		return wall;
 	}
 
 	/**
 	 * Deze inspector geeft een boolean die true is wanneer de hoogte toegestaan is en in het andere geval false.
 	 * 
 	 * @param 	height
 	 * 			De hoogte die nagekeken moet worden.
 	 * 	
 	 * @return	Boolean die true is wanneer de hoogte toegestaan is en in het andere geval false.
 	 * 			|if((height > Board.LOWER_BOUND_HEIGHT) && (height < Board.UPPER_BOUND_HEIGHT))
 	 * 			|	true
 	 */
 	@Basic
 	public static boolean isValidHeight(long height){
 		return (height > LOWER_BOUND_HEIGHT) && (height <= UPPER_BOUND_HEIGHT);
 	}
 
 	/**
 	 * Inspector om na te gaan of een gegeven breedte width een toegestane waarde is.
 	 * 
 	 * @param 	width
 	 * 			De breedte die nagekeken moet worden.
 	 * 		
 	 * @return	Boolean die true is wanneer de breedte toegestaan is en in het andere geval false.
 	 * 			|if((width > Board.LOWER_BOUND_WIDTH) && (width < Board.UPPER_BOUND_WIDTH))
 	 * 			|	true
 	 */
 	@Basic
 	public static boolean isValidWidth(long width){
 		return (width > LOWER_BOUND_WIDTH) && (width <= UPPER_BOUND_WIDTH);
 	}
 
 	/**
 	 * Inspector die de breedte van dit bord teruggeeft.
 	 * 
 	 * @return	De breedte van dit bord.
 	 * 			|this.width
 	 */
 	@Basic @Immutable
 	public long getWidth() {
 		return this.width;
 	}
 
 	/**
 	 * Inspector die de hoogte van dit bord teruggeeft.
 	 * 
 	 * @return	De hoogte van dit bord.
 	 * 			|this.height
 	 */
 	@Basic @Immutable
 	public long getHeight() {
 		return this.height;
 	}
 
 	/**
 	 * Deze methode kijkt na of een positie op het bord geldig is.
 	 * 
 	 * @param	position
 	 * 			De positie waarop nagekeken moet worden.
 	 * 
 	 * @return	Boolean die true is als de positie geldig is, false indien niet.
 	 * 			|if (position.getX() > this.getWidth() || position.getX() < LOWER_BOUND_WIDTH || position.getY() > this.getHeight() || position.getY() < LOWER_BOUND_HEIGHT)
 	 * 			|	false
 	 * 			|true
 	 */
 	public boolean isValidBoardPosition(Position position){
 		if(position == null)
 			return false;
 		if (position.getX() > this.getWidth() || position.getX() < LOWER_BOUND_WIDTH || position.getY() > this.getHeight() || position.getY() < LOWER_BOUND_HEIGHT)
 			return false;
 		return true;
 	}
 
 	/**
 	 * Deze methode geeft de HashMap terug waarop dit bord gebaseerd is.
 	 * 
 	 * @return	De HashMap waarop dit bord gebaseerd is.
 	 * 			|this.map
 	 */
 	@Basic @Immutable
 	public HashMap<String, HashSet<Entity>> getMap() {
 		return this.map;
 	}
 
 	/**
 	 * Deze methode voegt 2 borden samen. Elementen uit het 2de bord die niet op dit bord geplaatst kunnen worden, worden getermineerd.
 	 * 
 	 * @param	board2
 	 * 			Het bord dat moet samengevoegd worden met dit bord.
 	 * 
 	 * @post	Het 2de bord is getermineerd.
 	 * 			|(new board2).isTerminated() == true
 	 */
 	//TODO: hier moeten we alles opnieuw doen aangezien ze nu naast elkaar moeten uitkomen.
 	
 	public void merge(Board board2) {
 		Set<String> b2PosStrings = board2.getMap().keySet();
 		for (String b2PosString : b2PosStrings){
 			Position b2Pos = Calculator.getPositionFromString(b2PosString);
 			if(this.isValidBoardPosition(b2Pos)){
 				HashSet<Entity> b2EntitiesOnPos = board2.getEntityOnPosition(b2Pos);
 				for(Entity ent: b2EntitiesOnPos){
 					if(this.isPlacableOnPosition(b2Pos, ent)){
 						ent.removeFromBoard();
 						ent.putOnBoard(this, b2Pos);
 					}
 				}
 			}
 		}
 		board2.terminate();
 	}
 
 	/**
 	 * Deze methode kijkt na of het bord getermineerd is of niet.
 	 * 
 	 * @return	Boolean die true is als het bord getermineerd is.
 	 * 			|this.isTerminated
 	 */
 	public boolean isTerminated(){
 		return this.isTerminated;
 	}
 
 	/**
 	 * Deze methode termineert het bord en alle objecten op het bord.
 	 * 
 	 * @post	Het bord is getermineerd.
 	 * 			|new.isTerminated() == true
 	 * 
 	 * @post	Het bord is leeg.
 	 * 			|new.getMap().values() == null
 	 */
 	public void terminate(){
 		this.isTerminated = true;
 		Collection<HashSet<Entity>> c = this.getMap().values();
 		if(c != null){
 			for (HashSet<Entity> ents : c){
 				if(ents != null){
 					Position pos = ents.iterator().next().getPosition();
 					for (Entity ent : ents){
 						ent.destroy();
 					}
 					this.cleanBoardPosition(pos);
 				}
 			}
 		}
 	}
 
 	public boolean containsWall(Position pos) {
 		try{
 			HashSet<Entity> ents = this.getEntityOnPosition(pos);
 			boolean containsRobotsOrWalls = false;
 			for (Entity ent : ents){
 				if (ent instanceof Robot || ent instanceof Wall)
 					containsRobotsOrWalls = true;
 			}
 			return containsRobotsOrWalls;
 		}catch (NullPointerException esc){
 			return true;
 		}
 	}
 
 	public Set<SurpriseBox> getSurpriseBoxes() {
 		Collection<HashSet<Entity>> c = this.getMap().values();
 		HashSet<SurpriseBox> surp = new HashSet<>();
 		for (Set<Entity> values : c ){
 			Iterator<Entity> i = values.iterator();
 			while (i.hasNext()){
 				Entity obj = i.next();
 				if (obj instanceof SurpriseBox){
 					surp.add((SurpriseBox)obj);
 				}
 			}
 		}
 		return surp;
 	}
 
 	public Set<RepairKit> getRepairkits() {
 		Collection<HashSet<Entity>> c = this.getMap().values();
 		HashSet<RepairKit> rep = new HashSet<>();
 		for (Set<Entity> values : c ){
 			Iterator<Entity> i = values.iterator();
 			while (i.hasNext()){
 				Entity obj = i.next();
 				if (obj instanceof RepairKit){
 					rep.add((RepairKit)obj);
 				}
 			}
 		}
 		return rep;
 	}
 }
