 package bot;
 
 import java.util.HashSet;
 
 import util.Scoring;
 
 public class Map {
 
 	private Position myPosition;
 	private int height;
 	private int width;
 	private Square[][] content;
 	private int nbCompleteSearches;
 	public boolean needUpdate;
 
 	public Map(){
 		this.height = 0;
 		this.width = 0;
 		myPosition = null;
 		content = null;
 		needUpdate = false;
 	}
 	
 	public Map(char[][] map) throws UnknownPositionException{
 		this(map.length, map[0].length);
 		content = new Square[map.length][map[0].length];
 		for (int line = 0; line < map.length ; line++){
 			for (int column = 0; column < map[line].length; column++){
 				char c = map[line][column];
 				Position p = new Position(line,column);
 				if (c == Protocole.PLAYER_TOKEN)
 					myPosition = p;
 				content[line][column] = new Square(c, p);
 			}
 		}
 		if (myPosition == null)
 			throw new UnknownPositionException("Position not specified in message");
 	}
 
 	public Map(int height, int width){
 		this.height = height;
 		this.width = width;
 		myPosition = null;
 		needUpdate = false;
 		initMap();
 	}
 	
 	// initialize the Map according to a lazy mechanism
 	private void initMap(){
 		if (height < 0 || width < 0)
 			throw new RuntimeException("Map size indicators are not valid");
 		if (content != null)
 			return;
 		content = new Square[height][width];
 		for (int line = 0; line < height ; line++){
 			for (int column = 0; column < width; column++){
 				Position p = new Position(line, column);
 				content[line][column] = new Square(SquareType.UNKNOWN.getToken(), p);
 			}
 		}
 		linkMap();
 		myPosition = null;
 	}
 	
 	private void linkMap(){
 		for (int line = 0; line < height ; line++){
 			for (int column = 0; column < width; column++){
 				Square s = content[line][column];
 				for (Direction d : Direction.values()){
 					Square neighbor = getDest(s.getPosition(), d);
 					// null case is handled in addNeighbor
 					s.addNeighbor(neighbor);
 				}
 			}
 		}		
 	}
 	
 	public int getHeight(){
 		return height;
 	}
 	
 	public int getWidth(){
 		return width;
 	}
 	
 	public boolean fullySearched(){
 		for (int line = 0; line < height; line++){
 			for (int column = 0; column < width; column++){
 				if (content[line][column].getSearchScore() != 0)
 					return false;
 			}
 		}
 		return true;
 	}
 	
 	public void updateAllSearch(){
 		for (int line = 0; line < height; line++){
 			for (int column = 0; column < width; column++){
 				content[line][column].updateLocalSearchScore(this);
 			}
 		}
 	}
 	
 	public Square getDest(Direction d){
 		return getDest(myPosition, d);
 	}
 		
 	public Square getDest(Position p, Direction d){	
 		return getSquare(Position.add(p, d));
 	}
 	
 	public boolean isAllowedMove(Position src, Direction d){
 		Square s = getSquare(src);
 		Square dest = getDest(src, d);
 		// if dest is out of map, is a door or is out of sight, move is forbidden
 		if (dest == null ||
 		    s.getType() == SquareType.CLOSED_DOOR ||
 			s.getType() == SquareType.UNKNOWN ||
 			dest.getType() == SquareType.CLOSED_DOOR ||
 			dest.getType() == SquareType.UNKNOWN)
 			return false;
 		switch (d){
 		case NORTH_EAST:
 		case NORTH_WEST:
 		case SOUTH_WEST:
 		case SOUTH_EAST:
 			Square alt1 = getSquare(src.getLine(), src.getColumn() + d.getDeltaColumn());
 			Square alt2 = getSquare(src.getLine() + d.getDeltaLine(), src.getColumn());
			if ((!alt1.getType().diagonalyPassable() &&
				 alt2.getType().diagonalyPassable()) ||
				(alt1.getType().diagonalyPassable() &&
				 !alt2.getType().diagonalyPassable()))
 				return false;
 		case NORTH:
 		case SOUTH:
 		case EAST:
 		case WEST:
 			return (s.getType() != SquareType.VERTICAL_WALL &&
 					s.getType() != SquareType.HORIZONTAL_WALL &&
 					dest.getType() != SquareType.VERTICAL_WALL &&
 					dest.getType() != SquareType.HORIZONTAL_WALL);
 		default: return false;
 		}		
 	}
 	
 	public boolean isAllowedMove(Direction d){
 		return isAllowedMove(myPosition,d);
 	}
 	
 	public boolean isAllowedOpen(Direction d){
 		return isAllowedOpen(myPosition, d);
 	}
 	
 	public boolean isAllowedOpen(Position src, Direction d){
 		Square dest = getDest(src, d);
 		// if dest must not be out of the map and must be a door
 		return (dest != null &&
 				dest.getType() == SquareType.CLOSED_DOOR);
 	}
 	
 	public Square getSquare(Position p){
 		return getSquare(p.getLine(), p.getColumn());
 	}
 	
 	public Square getSquare(int line, int col){
 		if (line >= content.length ||
 		    line < 0)
 			return null;
 		if (col >= content[line].length ||
 			col < 0)
 			return null;
 		return content[line][col];
 	}
 	
 	public Position getPlayerPosition(){
 		return myPosition;
 	}
 	
 	public void updateSquare(int line, int col, char newVal){
 		if (newVal == SquareType.UNKNOWN.getToken())
 			return;//Keeping memory of visited square in dark rooms
 		Position p = new Position(line, col);
 		Square s = getSquare(p);
 		SquareType oldType = s.getType();
 		SquareType newType;
 		if (newVal == Protocole.PLAYER_TOKEN){
 			myPosition = p;
 			if (actualSquare().getType() == SquareType.UNKNOWN)
 				newType = SquareType.EMPTY;
 			else
 				newType = actualSquare().getType();
 		}
 		else{
 			newType = SquareType.tokenToVariables(newVal);
 		}
 		s.setType(newType, this);
 		if (oldType != newType)
 			needUpdate = true;
 	}
 	
 	public void updateSize(int height, int width){
 		this.width = width;
 		this.height = height;
 		initMap();
 	}
 	
 
 	public void updateScores(){
 		// Screwing it, now getting on O(n^2), should be improved with a priority queue
 		// Reseting score and adding all the Squares.
 		HashSet<Square> toTreat = new HashSet<Square>();
 		for (int line = 0; line < height; line++){
 			for (int col = 0; col < width; col++){
 				Square s = getSquare(line, col);
 				s.setScore(s.getInternScore());
 				toTreat.add(getSquare(line, col));
 			}
 		}
 		while(!toTreat.isEmpty()){
 			// finding best square
 			Square bestSquare = null;
 			double bestScore = Double.NEGATIVE_INFINITY;
 			for (Square s : toTreat){
 				if (s.getScore() > bestScore){
 					bestSquare = s;
 					bestScore = s.getScore();
 				}
 			}
 			// if bestScore is 0, no update will be needed on all future squares
 			if (bestScore == 0)
 				break;
 			// updating neighbors
 			double newScore = Scoring.afterMoveScore(bestScore);
 			for (Direction d : Direction.values()){
 				Square neighbor = getDest(bestSquare.getPosition(), d);
 				if (isAllowedMove(bestSquare.getPosition(), d) &&
 				    neighbor.getScore() < newScore)
 					neighbor.setScore(newScore);
 			}
 			toTreat.remove(bestSquare);
 		}
 		needUpdate = false;
 	}
 	
 	public boolean isKnownPosition(){
 		return myPosition != null;
 	}
 	
 	public String toString(){
 		StringBuffer sb = new StringBuffer();
 		sb.append("myPosition : " + myPosition + '\n');
 		for (int line = 0; line < content.length ; line++){
 			for (int column = 0; column < content[line].length; column++)
 				if (myPosition.getLine() == line &&
 					myPosition.getColumn() == column)
 					sb.append("@");
 				else
 					sb.append(content[line][column].toString());
 			sb.append('\n');
 		}
 		return sb.toString();
 	}
 	
 	public String searchMapAsString(){
 		StringBuffer sb = new StringBuffer();
 		sb.append("myPosition : " + myPosition + '\n');
 		for (int line = 0; line < content.length ; line++){
 			for (int column = 0; column < content[line].length; column++)
 				if (myPosition.getLine() == line &&
 					myPosition.getColumn() == column)
 					sb.append(" @|");
 				else{
 					if (content[line][column].getNbSearch() < 10)
 						sb.append(" ");
 					sb.append(content[line][column].getNbSearch());
 					sb.append('|');
 				}
 			sb.append('\n');
 		}
 		return sb.toString();
 	}
 	
 	public Square actualSquare(){
 		return getSquare(myPosition);
 	}
 
 	public void updateNeighborsSearchScore(Position p) {
 		for (Direction d : Direction.values()){
 			Position neighborPosition = Position.add(p, d);
 			Square neighbor = getSquare(neighborPosition);
 			if (neighbor != null)
 				neighbor.updateSearchScore(this);
 		}
 	}
 	
 	public int getNbCompleteSearches(){
 		return nbCompleteSearches;
 	}
 	
 	public void increaseNbCompleteSearches(){
 		nbCompleteSearches++;
 		updateAllSearch();
 	}
 }
